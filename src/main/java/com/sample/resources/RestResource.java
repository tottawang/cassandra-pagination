package com.sample.resources;

import java.io.IOException;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
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
  @Path("get/{productId}")
  public ProductById get(@PathParam("productId") UUID productId) {
    return repository.getProduct(productId);
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
