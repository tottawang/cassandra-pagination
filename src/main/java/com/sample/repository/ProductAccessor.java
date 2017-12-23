package com.sample.repository;

import java.util.UUID;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import com.sample.domain.ProductById;

@Accessor
public interface ProductAccessor {

  @Query("SELECT * FROM product_by_id where productid=:productid and itemid='new item id' and version=1")
  Result<ProductById> selectbyId(@Param("productId") UUID productid);

}
