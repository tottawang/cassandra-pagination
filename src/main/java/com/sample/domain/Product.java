package com.sample.domain;

import java.util.Map;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import com.fasterxml.jackson.databind.JsonNode;
import com.sample.conf.EnableBatch;
import com.sample.conf.JsonCodec;

@Table(name = "products")
@EnableBatch
public class Product {

  @PartitionKey(0)
  private String itemid;

  @PartitionKey(1)
  private int version;

  @ClusteringColumn(0)
  private String type;

  @ClusteringColumn(1)
  private UUID productid;

  @ClusteringColumn(2)
  private String category;

  @Column(name = "scopes")
  private Map<String, String> scopes;

  @Column(name = "attributes", codec = JsonCodec.class)
  private JsonNode attributes;

  @Transient
  private String text;

  public Product() {}

  public Product(String itemid, int version, UUID productid, Map<String, String> scopes,
      String type, JsonNode attributes, String category) {
    this.itemid = itemid;
    this.version = version;
    this.productid = productid;
    this.text = "dummy";
    this.scopes = scopes;
    this.type = type;
    this.attributes = attributes;
    this.category = category;
  }

  public UUID getProductid() {
    return productid;
  }

  public void setProductid(UUID _productid) {
    this.productid = _productid;
  }

  public int getVersion() {
    return this.version;
  }

  public void setVersion(int _version) {
    this.version = _version;
  }

  public String getItemid() {
    return itemid;
  }

  public void setItemid(String _itemid) {
    this.itemid = _itemid;
  }

  public Map<String, String> getScopes() {
    if (scopes.isEmpty())
      return null;
    else
      return scopes;
  }

  public void setScopes(Map<String, String> scopes) {
    this.scopes = scopes;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public ProductKey getProductKey() {
    return new ProductKey(this.itemid, this.version, this.productid);
  }

  public void setProductKey(ProductKey productKey) {
    this.productid = productKey.getProductid();
    this.version = productKey.getVersion();
    this.itemid = productKey.getItemid();
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public JsonNode getAttributes() {
    return attributes;
  }

  public void setAttributes(JsonNode attributes) {
    this.attributes = attributes;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  @Override
  public String toString() {
    return "Product [itemid=" + itemid + ", version=" + version + ", type=" + type + ", productid="
        + productid + ", category=" + category + ", scopes=" + scopes + ", attributes=" + attributes
        + ", text=" + text + "]";
  }


}
