 /*
  * Copyright (c) 2008-2011 Ivan Khalopik.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.greatage.domain;
 
 import org.greatage.domain.internal.AllCriteria;
 import org.greatage.domain.internal.ChildCriteria;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class EntityMapper<PK extends Serializable, E extends Entity<PK>> implements Query.Property {
     private static final String DEFAULT_ID_PROPERTY = "id";
 
     public final PropertyMapper<PK> id$;
 
     private final String path;
     private final String property;
     private final String cachedPath;
 
     public EntityMapper(final String property) {
         this(null, property);
     }
 
     public EntityMapper(final String path, final String property) {
         this(path, property, DEFAULT_ID_PROPERTY);
     }
 
     public EntityMapper(final String path, final String property, final String idProperty) {
         this.path = path;
         this.property = property;
         this.cachedPath = toPath(path, property);
 
         id$ = property(idProperty);
     }
 
     public String getPath() {
         return path;
     }
 
     public String getProperty() {
         return property;
     }
 
     public AllCriteria all() {
         return new AllCriteria();
     }
 
     public Query.Criteria is(final Query.Criteria criteria) {
         return new ChildCriteria(cachedPath, null, criteria);
     }
 
     public Query.Criteria isNull() {
         return equal(null);
     }
 
     public Query.Criteria notNull() {
         return notEqual(null);
     }
 
     public Query.Criteria eq(final E entity) {
         return equal(entity);
     }
 
     public Query.Criteria equal(final E entity) {
         return id$.equal(toId(entity));
     }
 
     public Query.Criteria ne(final E entity) {
         return notEqual(entity);
     }
 
     public Query.Criteria notEqual(final E entity) {
         return id$.notEqual(toId(entity));
     }
 
     public Query.Criteria in(final E... entities) {
         final List<PK> pks = new ArrayList<PK>(entities.length);
         for (E entity : entities) {
             pks.add(toId(entity));
         }
         return id$.in(pks);
     }
 
     public Query.Criteria in(final Collection<E> entities) {
         final List<PK> pks = new ArrayList<PK>(entities.size());
         for (E entity : entities) {
             pks.add(toId(entity));
         }
         return id$.in(pks);
     }
 
     protected <V> PropertyMapper<V> property(final String property) {
         return new PropertyMapper<V>(cachedPath, property);
     }
 
     protected <V> EmbedMapper<V> embed(final String property) {
         return new EmbedMapper<V>(cachedPath, property);
     }
 
     protected <VPK extends Serializable, V extends Entity<VPK>>
     EntityMapper<VPK, V> entity(final String property) {
         return new EntityMapper<VPK, V>(cachedPath, property);
     }
 
    protected String getCachedPath() {
        return cachedPath;
    }

     private String toPath(final String path, final String property) {
         return path != null ?
                 property != null ?
                         path + "." + property :
                         path :
                 property;
     }
 
     private PK toId(final E entity) {
         return entity != null ? entity.getId() : null;
     }
 }
