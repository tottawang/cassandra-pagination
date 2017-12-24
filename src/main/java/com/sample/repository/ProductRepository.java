package com.sample.repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PagingState;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.conf.CassandraBatchProcess;
import com.sample.conf.CassandraConfiguration;
import com.sample.domain.Product;
import com.sample.domain.ProductById;
import com.sample.domain.ProductType;

@Component
public class ProductRepository {

  private static final String attributes = "{\"alias\":\"data\",\"systemId\":\"adsk.wipqa\"}";
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  @Qualifier(CassandraConfiguration.CASSANDRA_SESSION)
  private Session session;

  @Autowired
  @Qualifier(CassandraConfiguration.CASSANDRA_CLUSTER)
  private Cluster cluster;

  @Autowired
  @Qualifier(CassandraConfiguration.CASSANDRA_MAPPINGMANAGER)
  private MappingManager manager;

  @Autowired
  private CassandraBatchProcess batchProcessor;

  public long count() {
    String query = ("SELECT COUNT(*) FROM products");
    ResultSet results = session.execute(query);
    return results.one().getLong("count");
  }

  public ProductById getProduct(UUID productId) {
    ProductAccessor accessor = manager.createAccessor(ProductAccessor.class);
    Result<ProductById> itemResult = accessor.selectbyId(productId);
    return itemResult.one();
  }

  public List<Row> getAllPagedProducts() {
    ProductAccessor accessor = manager.createAccessor(ProductAccessor.class);
    Statement statement = accessor.selectbyItemAndVersion();
    statement.setFetchSize(2);
    ResultSet rs = session.execute(statement);
    List<Row> allResults = rs.all();
    for (Row row : allResults) {
      System.out.println(row);
    }
    return null;
  }

  public List<Row> getProducts() {
    ProductAccessor accessor = manager.createAccessor(ProductAccessor.class);
    Statement statement = accessor.selectbyItemAndVersion();
    statement.setFetchSize(2);
    ResultSet rs = session.execute(statement);

    PagingState nextPage = rs.getExecutionInfo().getPagingState();
    String pagingState = nextPage.toString();
    System.out.println(pagingState);

    // Note that we don't rely on RESULTS_PER_PAGE, since Cassandra might
    // have not respected it, or we might be at the end of the result set
    int remaining = rs.getAvailableWithoutFetching();
    for (Row row : rs) {
      System.out.println(row);
      if (--remaining == 0) {
        break;
      }
    }

    Statement statement2 = accessor.selectbyItemAndVersion();
    statement2.setFetchSize(8);
    statement2.setPagingState(PagingState.fromString(pagingState));
    ResultSet rs2 = session.execute(statement2);

    PagingState nextPage2 = rs2.getExecutionInfo().getPagingState();
    String pagingState2 = nextPage2.toString();
    System.out.println(pagingState2);

    // Note that we don't rely on RESULTS_PER_PAGE, since Cassandra might
    // have not respected it, or we might be at the end of the result set
    int remaining2 = rs2.getAvailableWithoutFetching();
    for (Row row : rs) {
      System.out.println(row);
      if (--remaining2 == 0) {
        break;
      }
    }

    Statement statement3 = accessor.selectbyItemAndVersion();
    statement3.setFetchSize(20);
    statement3.setPagingState(PagingState.fromString(pagingState2));
    ResultSet rs3 = session.execute(statement3);

    PagingState nextPage3 = rs3.getExecutionInfo().getPagingState();
    System.out.println(nextPage3.toString());

    // Note that we don't rely on RESULTS_PER_PAGE, since Cassandra might
    // have not respected it, or we might be at the end of the result set
    int remaining3 = rs3.getAvailableWithoutFetching();
    for (Row row : rs) {
      System.out.println(row);
      if (--remaining3 == 0) {
        break;
      }
    }

    return null;
  }

  /**
   * Insert without using batch.
   * 
   * @throws IOException
   * @throws JsonProcessingException
   */
  public void insert() throws JsonProcessingException, IOException {
    Map<String, String> scopes = new HashMap<String, String>();
    scopes.put("name", "value");
    UUID productId = UUID.randomUUID();
    JsonNode payload = objectMapper.readTree(attributes);
    Product product = new Product("new item id", 1, productId, scopes, ProductType.Rocket, payload);
    Mapper<Product> mapper = manager.mapper(Product.class);
    mapper.save(product);

    ProductById productById =
        new ProductById("new item id", 1, productId, scopes, ProductType.Rocket, payload);
    Mapper<ProductById> mapperForProductById = manager.mapper(ProductById.class);
    mapperForProductById.save(productById);
  }

  /**
   * Batch insert.
   * 
   * @throws IOException
   * @throws JsonProcessingException
   */
  public void batchInsert() throws JsonProcessingException, IOException {
    Map<String, String> scopes = new HashMap<String, String>();
    scopes.put("name", "value");
    UUID productId = UUID.randomUUID();
    JsonNode payload = objectMapper.readTree(attributes);
    Product product = new Product("new item id", 1, productId, scopes, ProductType.Rocket, payload);
    ProductById productById =
        new ProductById("new item id", 1, productId, scopes, ProductType.Rocket, payload);
    Collection<?> secondaryTables = new HashSet<>(Arrays.asList(productById));
    batchProcessor.executeBatchInsert(product, secondaryTables);
  }
}
