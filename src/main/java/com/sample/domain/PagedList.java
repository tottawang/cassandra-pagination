package com.sample.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"links", "data"})
public class PagedList<T> {

  private final static String NEXT_PAGE_TITLE = "next";
  private final List<T> items;
  private final Map<String, String> links = new HashMap<>();

  /**
   * C* specific paged collection which only works for next page state, offset query is not
   * supported. For more information, please refer to
   * https://docs.datastax.com/en/developer/java-driver/3.0/manual/paging/
   * 
   * @param items
   * @param pageState
   */
  public PagedList(List<T> items, String nextPageState) {
    this.items = items;
    if (!StringUtils.isNullOrEmpty(nextPageState)) {
      this.links.put(NEXT_PAGE_TITLE, nextPageState);
    }
  }

  @JsonProperty("data")
  public List<T> getItems() {
    return Collections.unmodifiableList(items);
  }

  @JsonProperty("links")
  public Map<String, String> getLinks() {
    return Collections.unmodifiableMap(links);
  }

}
