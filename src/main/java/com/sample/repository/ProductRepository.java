package com.sample.repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
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
