package com.sample.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.util.StringUtils;
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
import com.sample.domain.Links;
import com.sample.domain.PagedList;
import com.sample.domain.Product;
import com.sample.domain.ProductById;

@Component
public class ProductRepository {

  private static final String attributes = "{\"level\":\"1\",\"category\":\"test\"}";
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${DEFAULT_PAGE_SIZE:2}")
  private int defaultPageSize;

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
    statement.setFetchSize(defaultPageSize);
    ResultSet rs = session.execute(statement);
    // ResultSet.all will fetch all paged results by ArrayBackedResultSet.iterator, which won't stop
    // querying next paged results until next page is null
    List<Row> allResults = rs.all();
    for (Row row : allResults) {
      System.out.println(row);
    }
    return null;
  }

  public PagedList<Product> getPagedProduct(String pagingState) {
    ProductAccessor accessor = manager.createAccessor(ProductAccessor.class);
    Statement statement = accessor.selectbyItemAndVersion();
    statement.setFetchSize(defaultPageSize);
    if (!StringUtils.isNullOrEmpty(pagingState)) {
      statement.setPagingState(PagingState.fromString(pagingState));
    }

    ResultSet rs = session.execute(statement);
    Mapper<Product> mapper = manager.mapper(Product.class);
    Result<Product> results = mapper.map(rs);
    List<Product> products = new ArrayList<>();

    // avoid loading all records by automatic pagination (rs.all()), load one page only with
    // getAvailableWithoutFetching value
    int remaining = rs.getAvailableWithoutFetching();
    for (Product product : results) {
      products.add(product);
      if (--remaining == 0) {
        break;
      }
    }

    PagingState nextPage = rs.getExecutionInfo().getPagingState();
    return new PagedList<Product>(products,
        nextPage == null ? null : String.format(Links.BASE_URL_PAGED_LISTING, nextPage.toString()));
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
    String type = "any type";
    String category = "any category";
    Product product = new Product("new item id", 1, productId, scopes, type, payload, category);
    Mapper<Product> mapper = manager.mapper(Product.class);
    mapper.save(product);

    ProductById productById =
        new ProductById("new item id", 1, productId, scopes, type, payload, category);
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
    Product product =
        new Product("new item id", 1, productId, scopes, "any type", payload, "any category");
    ProductById productById =
        new ProductById("new item id", 1, productId, scopes, "any type", payload, "any category");
    Collection<?> secondaryTables = new HashSet<>(Arrays.asList(productById));
    batchProcessor.executeBatchInsert(product, secondaryTables);
  }
}
