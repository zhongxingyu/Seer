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
 
 package org.greatage.domain.hibernate;
 
 import org.greatage.domain.Entity;
 import org.greatage.domain.Query;
 import org.greatage.domain.internal.AbstractQueryVisitor;
 import org.greatage.domain.internal.ChildCriteria;
 import org.greatage.domain.internal.JunctionCriteria;
 import org.greatage.domain.internal.PropertyCriteria;
 import org.greatage.util.NameAllocator;
 import org.greatage.util.StringUtils;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Junction;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Property;
 import org.hibernate.criterion.Restrictions;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class HibernateQueryVisitor<PK extends Serializable, E extends Entity<PK>>
         extends AbstractQueryVisitor<PK, E> {
 
     private final Map<String, org.hibernate.Criteria> children = new HashMap<String, org.hibernate.Criteria>();
     private final NameAllocator names = new NameAllocator();
 
     private final org.hibernate.Criteria root;
 
     private Junction junction;
     private String path;
     private String property;
 
     HibernateQueryVisitor(final org.hibernate.Criteria root) {
         this.root = root;
     }
 
     @Override
     protected void visitJunction(final JunctionCriteria criteria) {
         // backup previous junction
         final Junction parent = junction;
         // create new junction
         final Junction current = criteria.getOperator() == JunctionCriteria.Operator.AND ?
                 Restrictions.conjunction() :
                 Restrictions.disjunction();
 
         // replace current junction
         junction = current;
         // process child criteria
         for (Query.Criteria child : criteria.getChildren()) {
             visitCriteria(child);
         }
         // restore previous junction
         junction = parent;
 
         // add junction to the criteria
         addCriterion(current, criteria.isNegative());
     }
 
     @Override
     protected void visitChild(final ChildCriteria criteria) {
         // backup previous path
         final String parentPath = path;
         final String parentProperty = property;
 
         // replace current path
        path = criteria.getPath();
        property = criteria.getProperty();
         // visit child criteria
         visitCriteria(criteria.getCriteria());
         // restore previous path
         path = parentPath;
         property = parentProperty;
     }
 
     @Override
     protected void visitEqual(final PropertyCriteria criteria) {
         final Property property = getProperty(criteria);
 
         if (criteria.getValue() == null) {
             addCriterion(property.isNull(), criteria.isNegative());
         } else {
             addCriterion(property.eq(criteria.getValue()), criteria.isNegative());
         }
     }
 
     @Override
     protected void visitNotEqual(final PropertyCriteria criteria) {
         final Property property = getProperty(criteria);
 
         if (criteria.getValue() == null) {
             addCriterion(property.isNotNull(), criteria.isNegative());
         } else {
             addCriterion(property.ne(criteria.getValue()), criteria.isNegative());
         }
     }
 
     @Override
     protected void visitGreaterThan(final PropertyCriteria criteria) {
         final Property property = getProperty(criteria);
 
         addCriterion(property.gt(criteria.getValue()), criteria.isNegative());
     }
 
     @Override
     protected void visitGreaterOrEqual(final PropertyCriteria criteria) {
         final Property property = getProperty(criteria);
 
         addCriterion(property.ge(criteria.getValue()), criteria.isNegative());
     }
 
     @Override
     protected void visitLessThan(final PropertyCriteria criteria) {
         final Property property = getProperty(criteria);
 
         addCriterion(property.lt(criteria.getValue()), criteria.isNegative());
     }
 
     @Override
     protected void visitLessOrEqual(final PropertyCriteria criteria) {
         final Property property = getProperty(criteria);
 
         addCriterion(property.le(criteria.getValue()), criteria.isNegative());
     }
 
     @Override
     protected void visitIn(final PropertyCriteria criteria) {
         final Property property = getProperty(criteria);
 
         final List<?> value = (List<?>) criteria.getValue();
         if (value == null || value.isEmpty()) {
             addCriterion(Restrictions.sqlRestriction("1=2"), criteria.isNegative());
         } else {
             addCriterion(property.in(value), criteria.isNegative());
         }
     }
 
     @Override
     protected void visitLike(final PropertyCriteria criteria) {
         final Property property = getProperty(criteria);
 
         addCriterion(property.like(criteria.getValue()), criteria.isNegative());
     }
 
     @Override
     protected void visitFetch(final Query.Property fetch) {
         //todo: implement this
     }
 
     @Override
     protected void visitSort(final Query.Property property, final boolean ascending, final boolean ignoreCase) {
         final Order order = ascending ?
                 Order.asc(property.getProperty()) :
                 Order.desc(property.getProperty());
 
         if (ignoreCase) {
             order.ignoreCase();
         }
         getCriteria(property.getPath()).addOrder(order);
     }
 
     @Override
     protected void visitPagination(final int start, final int count) {
         if (start > 0) {
             root.setFirstResult(start);
         }
         if (count >= 0) {
             root.setMaxResults(count);
         }
     }
 
     private void addCriterion(final Criterion criterion, final boolean negative) {
         if (junction != null) {
             junction.add(negative ? Restrictions.not(criterion) : criterion);
         } else {
             root.add(negative ? Restrictions.not(criterion) : criterion);
         }
     }
 
     private Property getProperty(final PropertyCriteria criteria) {
         final String path = toPath(this.path, criteria.getPath());
         final String property = toPath(this.property, criteria.getProperty());
         final String alias = getCriteria(path).getAlias();
         return Property.forName(alias + "." + property);
     }
 
     private String toPath(final String path, final String property) {
         return path != null ?
                 property != null ?
                         path + "." + property :
                         path :
                 property;
     }
 
     private org.hibernate.Criteria getCriteria(final String path) {
         if (path == null) {
             return root;
         }
         if (!children.containsKey(path)) {
             children.put(path, createCriteria(path));
         }
         return children.get(path);
     }
 
     private org.hibernate.Criteria createCriteria(final String path) {
         if (StringUtils.isEmpty(path)) {
             throw new IllegalArgumentException("Empty path");
         }
         final int i = path.lastIndexOf('.');
         final String property = i > 0 ? path.substring(i + 1) : path;
         return root.createCriteria(path, names.allocate(property));
     }
 }
