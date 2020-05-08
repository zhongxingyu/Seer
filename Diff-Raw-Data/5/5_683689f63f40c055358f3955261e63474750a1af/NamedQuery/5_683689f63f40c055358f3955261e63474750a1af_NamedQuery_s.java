 package com.redshape.persistence.dao.query;
 
 import com.redshape.persistence.dao.query.expressions.IExpression;
 import com.redshape.persistence.dao.query.statements.IStatement;
 import com.redshape.persistence.entities.IEntity;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author nikelin
  */
 public class NamedQuery implements IQuery {
 
     private IQuery query;
     private String name;
 
     public NamedQuery( IQuery query, String name ) {
         this.query = query;
         this.name = name;
     }
 
     @Override
     public IQuery where( IExpression expression ) {
         return this.query.where(expression);
     }
 
     @SuppressWarnings("unchecked")
 	@Override
     public <T extends IEntity> Class<T> getEntityClass() {
         return (Class<T>) this.query.getEntityClass();
     }
 
     @Override
     public boolean hasAttribute( String name ) {
         return this.query.hasAttribute(name);
     }
 
     @Override
     public <T> Map<String, T> getAttributes() {
     	return this.query.getAttributes();
     }
     
     @Override
     public <T extends IExpression> T getExpression() {
         return this.query.<T>getExpression();
     }
 
     @Override
     public boolean isStatic() {
         return this.query.isStatic();
     }
 
     public String getName() {
         return this.name;
     }
 
     @Override
     public IQuery setAttribute(String name, Object value) {
         this.query.setAttribute(name, value);
         return this;
     }
 
     @Override
     public <T> T getAttribute(String name) throws QueryExecutorException {
         return this.query.<T>getAttribute(name);
     }
 
     @Override
     public int getOffset() {
         return this.query.getOffset();
     }
 
     @Override
     public IQuery setOffset( int offset ) {
         this.query.setOffset(offset);
         return this;
     }
 
     @Override
     public int getLimit() {
         return this.query.getLimit();
     }
 
     @Override
     public IQuery setLimit( int limit ) {
         this.query.setLimit(limit);
         return this;
     }
 
     @Override
     public IQuery setAttributes(Map<String, Object> attributes) {
         this.query.setAttributes(attributes);
         return this;
     }
 
     @Override
     public List<IStatement> select() {
         return this.query.select();
     }
 
     @Override
     public IQuery select(IStatement... statements) {
         this.query.select(statements);
         return this;
     }
 
     @Override
     public OrderDirection orderDirection() {
         return this.query.orderDirection();
     }
 
     @Override
     public IStatement orderField() {
         return this.query.orderField();
     }
 
     @Override
     public IQuery orderBy(IStatement field, OrderDirection direction) {
         this.query.orderBy(field, direction);
         return this;
     }
 
     @Override
     public List<IStatement> groupBy() {
         return this.query.groupBy();
     }
 
     @Override
     public IQuery groupBy(IStatement... statements) {
         return this.query.groupBy(statements);
     }
 
     @Override
     public IEntity entity() {
         return this.query.entity();
     }
 
     @Override
     public IQuery entity(IEntity entity) {
         this.query.entity(entity);
         return this;
     }
 
     @Override
     public boolean isNative() {
         return this.query.isNative();
     }
 
     @Override
     public IQuery duplicate() {
         return this.query.duplicate();
     }
 
     @Override
     public boolean isUpdate() {
         return query.isUpdate();
     }
 
     @Override
     public boolean isRemove() {
         return query.isRemove();
     }
 
     @Override
     public boolean isCreate() {
         return query.isCreate();
     }
 }
