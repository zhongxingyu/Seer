 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.middleware.dao;
 
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Criteria;
 import org.hibernate.LockOptions;
 import org.hibernate.Session;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Projections;
 
 public abstract class GenericDAO<T, ID extends Serializable> {
 
     private Class<T> persistentClass;
     private Session session;
 
     public GenericDAO() {
         this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
     }
 
     public void setSession(Session session) {
         this.session = session;
     }
 
     protected Session getSession() {
         return this.session;
     }
 
     public Class<T> getPersistentClass() {
         return this.persistentClass;
     }
 
     @SuppressWarnings("unchecked")
     public T findById(ID id, boolean lock) {
         T entity;
 
         if (lock) {
             entity = (T) getSession().get(getPersistentClass(), id, LockOptions.UPGRADE);
         } else {
             entity = (T) getSession().get(getPersistentClass(), id);
         }
 
         return entity;
     }
 
     @SuppressWarnings("unchecked")
     protected List<T> findByCriteria(List<Criterion> criterion) {
         Criteria crit = getSession().createCriteria(getPersistentClass());
         for (Criterion c : criterion) {
             crit.add(c);
         }
 
         return crit.list();
     }
 
    @SuppressWarnings("unchecked")
     protected Criteria findByCriteriaWithAliases(List<Criterion> criterion, Map<String, String> aliases) {
         Criteria crit = getSession().createCriteria(getPersistentClass());
 
         for (String field : aliases.keySet()) {
             String alias = aliases.get(field);
             crit.createAlias(field, alias);
         }
 
         for (Criterion c : criterion) {
             crit.add(c);
         }
 
         return crit;
     }
     
/*    @SuppressWarnings("unchecked")
     public List<T> getAll() {
         Criteria criteria = getSession().createCriteria(getPersistentClass());
         return criteria.list();
    }*/
 
     @SuppressWarnings("unchecked")
     public List<T> getAll(int start, int numOfRows) {
         Criteria criteria = getSession().createCriteria(getPersistentClass());
         criteria.setFirstResult(start);
         criteria.setMaxResults(numOfRows);
         return criteria.list();
     }
 
     public Long countAll() {
         Criteria criteria = getSession().createCriteria(getPersistentClass());
         criteria.setProjection(Projections.rowCount());
         Long count = (Long) criteria.uniqueResult();
         return count;
     }
 
     public T saveOrUpdate(T entity) {
         getSession().saveOrUpdate(entity);
         return entity;
     }
 
     public void makeTransient(T entity) {
         getSession().delete(entity);
     }
 
     public Integer getNegativeId(String idName) {
         Criteria crit = getSession().createCriteria(getPersistentClass());
         crit.setProjection(Projections.min(idName));
         Integer minId = (Integer) crit.uniqueResult();
         if (minId != null) {
             if (minId.intValue() >= 0) {
                 minId = new Integer(-1);
             } else {
                 minId = new Integer(minId.intValue() - 1);
             }
         } else {
             minId = new Integer(-1);
         }
 
         return minId;
     }
 
     public void flush() {
         getSession().flush();
     }
 
     public void clear() {
         getSession().clear();
     }
 }
