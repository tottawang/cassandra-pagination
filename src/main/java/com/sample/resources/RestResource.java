package com.sample.resources;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Row;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sample.domain.PagedList;
import com.sample.domain.Product;
import com.sample.domain.ProductById;
import com.sample.repository.ProductRepository;

@Component
@Produces(MediaType.APPLICATION_JSON)
@Path("/api")
public class RestResource {


  @Autowired
  private ProductRepository repository;

  @GET
  @Path("count")
  public String count() {
    return Long.valueOf(repository.count()).toString() + " item(s) returned";
  }

  @GET
  @Path("products/{productId}")
  public ProductById get(@PathParam("productId") UUID productId) {
    ProductById result = repository.getProduct(productId);
    return result;
  }

  /**
   * Get all paged results by automatic pagination feature.
   * 
   * @return
   */
  @GET
  @Path("all-products")
  public List<Row> get() {
    return repository.getAllPagedProducts();
  }

  @GET
  @Path("paged-products")
  public PagedList<Product> getPagedProducts(@QueryParam("pagingState") String pagingState) {
    return repository.getPagedProduct(pagingState);
  }

  @POST
  @Path("insert")
  public void insert() throws JsonProcessingException, IOException {
    repository.insert();
  }

  @POST
  @Path("batch-insert")
  public void batchInsert() throws JsonProcessingException, IOException {
    repository.batchInsert();
  }
}
