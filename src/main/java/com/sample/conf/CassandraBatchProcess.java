package com.sample.conf;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.Table;

@Component
public class CassandraBatchProcess {

  @Autowired
  @Qualifier(CassandraConfiguration.CASSANDRA_SESSION)
  private Session session;

  @Autowired
  @Qualifier(CassandraConfiguration.CASSANDRA_MAPPINGMANAGER)
  private MappingManager manager;

  public <T> void executeBatchInsert(T entity, Collection<?> secondaryTables) {
    if (isBatchEnabled(entity) && !CollectionUtils.isEmpty(secondaryTables)) {
      BatchStatement batchStatement = new BatchStatement();
      Mapper mapper = manager.mapper(entity.getClass());
      batchStatement.add(mapper.saveQuery(entity));
      for (Object secondary : secondaryTables) {
        if (isSecondaryMatch(entity, secondary)) {
          Mapper mapperForSecondary = manager.mapper(secondary.getClass());
          batchStatement.add(mapperForSecondary.saveQuery(secondary));
        } else {
          throw new RuntimeException(
              "@SecondaryTable attribute 'masterEntity' value is not matching the parent entity");
        }
      }
      session.execute(batchStatement);
    } else {
      throw new RuntimeException("expect to run batch only this sample application");
    }
  }

  private boolean isBatchEnabled(Object entity) {
    return !ObjectUtils
        .isEmpty(AnnotationUtils.findAnnotation(entity.getClass(), EnableBatch.class));
  }

  private boolean isSecondaryMatch(Object entity, Object secondary) {
    if (null == secondary) {
      return false;
    }
    SecondaryTable secondaryTable =
        AnnotationUtils.findAnnotation(secondary.getClass(), SecondaryTable.class);
    if (!ObjectUtils.isEmpty(secondaryTable)) {
      // Qualified for a Secondary Table when the @SecondaryTable annotation matches and has a
      // @Table annotation
      return secondary.getClass().isAnnotationPresent(Table.class)
          && secondaryTable.masterEntity().equals(entity.getClass());
    }
    return false;
  }
}
