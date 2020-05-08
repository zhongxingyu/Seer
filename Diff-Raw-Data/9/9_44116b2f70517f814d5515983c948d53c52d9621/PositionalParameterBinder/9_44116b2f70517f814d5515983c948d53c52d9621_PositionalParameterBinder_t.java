 package org.polyforms.repository.jpa.binder;
 
 import javax.persistence.Parameter;
 import javax.persistence.Query;
 
 /**
  * Binder for positional parameters of JPA.
  * 
  * @author Kuisong Tong
  * @since 1.0
  */
 class PositionalParameterBinder extends AbstractParameterBinder<Integer> {
    protected PositionalParameterBinder() {
         super();
         addParameterMatcher(new PositionalParameterMatcher());
     }
 
     @Override
     protected void setParameter(final Query query, final Integer key, final Object value) {
         query.setParameter(key, value);
     }
 
     /**
      * {@inheritDoc}
      */
     public Integer getKey(final Parameter<?> parameter) {
         return parameter.getPosition();
     }
 }
