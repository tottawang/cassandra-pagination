package com.sample.domain;

import java.util.UUID;

public class ProductKey {

  private String itemid;

  private int version;

  private UUID productid;

  public ProductKey(String itemid, int version, UUID productid) {
    this.itemid = itemid;
    this.version = version;
    this.productid = productid;
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

}
