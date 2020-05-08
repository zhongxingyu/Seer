 /**
  * Mule Morphia Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.morphia;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.query.Query;
 import org.bson.types.ObjectId;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class QueryBuilder {
 
     private Query<?> query;
     private Map<String, Object> filters;
     private Integer offset;
     private Integer limit;
     private String order;
     private List<String> fields;
     private Boolean disableCursorTimeout;
     private Boolean disableSnapshotMode;
     private Boolean disableValidation;
 
     private QueryBuilder(Datastore datastore, String className) throws ClassNotFoundException {
         query = datastore.createQuery(Class.forName(className));
     }
 
     public static QueryBuilder newBuilder(Datastore datastore, String className) throws ClassNotFoundException {
         return new QueryBuilder(datastore, className);
     }
 
     public QueryBuilder setFilters(Map<String, Object> filters) {
         this.filters = filters;
         return this;
     }
 
     public QueryBuilder addFilter(String key, Object value) {
         if(filters == null) {
             filters = new HashMap<String, Object>();
         }
         filters.put(key, value);
         return this;
     }
 
     public QueryBuilder setOffset(Integer offset) {
         this.offset = offset;
         return this;
     }
 
     public QueryBuilder setLimit(Integer limit) {
         this.limit = limit;
         return this;
     }
 
     public QueryBuilder setOrder(String order) {
         this.order = order;
         return this;
     }
 
     public QueryBuilder setFields(List<String> fields) {
         this.fields = fields;
         return this;
     }
 
     public QueryBuilder addField(String field) {
         if(fields == null) {
             fields = new ArrayList<String>();
         }
         fields.add(field);
         return this;
     }
 
     public QueryBuilder setDisableCursorTimeout(Boolean disableCursorTimeout) {
         this.disableCursorTimeout = disableCursorTimeout;
         return this;
     }
 
     public QueryBuilder setDisableSnapshotMode(Boolean disableSnapshotMode) {
         this.disableSnapshotMode = disableSnapshotMode;
         return this;
     }
 
     public QueryBuilder setDisableValidation(Boolean disableValidation) {
         this.disableValidation = disableValidation;
         return this;
     }
 
     public Query<?> getQuery() throws ClassNotFoundException {
         if( disableCursorTimeout != null && disableValidation.booleanValue() ) {
             query.disableCursorTimeout();
         }
         if( disableSnapshotMode != null && disableSnapshotMode.booleanValue() ) {
             query.disableSnapshotMode();
         }
         if( disableValidation != null && disableValidation.booleanValue() ) {
             query.disableValidation();
         }
 
         if (filters != null) {
             addFilters(query, filters);
         }
 
         if (offset != null) {
             query.offset(offset);
         }
         if (limit != null) {
             query.limit(limit);
         }
         if (order != null) {
             query.order(order);
         }
 
         if (fields != null) {
            query.retrievedFields(true, fields.toArray(new String[]{}));
         }
 
         return query;
     }
 
     private void addFilters(Query<?> query, Map<String, Object> filters) throws ClassNotFoundException {
         for (String condition : filters.keySet()) {
             query.filter(condition, filters.get(condition));
         }
     }
 
 }
