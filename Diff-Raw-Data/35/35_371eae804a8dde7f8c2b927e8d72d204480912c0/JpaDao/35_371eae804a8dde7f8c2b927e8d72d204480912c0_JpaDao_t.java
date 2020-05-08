 package com.dottydingo.hyperion.service.persistence.dao;
 
 import com.dottydingo.hyperion.service.model.PersistentObject;
 import com.dottydingo.hyperion.service.persistence.query.PredicateBuilder;
 import com.dottydingo.hyperion.service.persistence.sort.OrderBuilder;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: mark
  * Date: 1/27/13
  * Time: 9:38 AM
  */
 public class JpaDao<P extends PersistentObject,ID extends Serializable> implements Dao<P,ID>
 {
     @PersistenceContext
    protected EntityManager em;
 
     @Override
     public List<P> findAll(Class<P> entityClass, List<ID> ids)
     {
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<P> criteriaQuery = cb.createQuery(entityClass);
         Root<P> root = criteriaQuery.from(entityClass);
         Path<?> id = root.get("id");
         Predicate predicate = id.in(ids);
         criteriaQuery.where(predicate).orderBy(cb.asc(id));
 
         TypedQuery<P> query = em.createQuery(criteriaQuery);
         return query.getResultList();
     }
 
     @Override
     public PersistentQueryResult<P> query(Class<P> entityClass, Integer start, Integer limit, OrderBuilder orderBuilder,
                          List<PredicateBuilder<P>> predicateBuilders)
     {
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<P> criteriaQuery = cb.createQuery(entityClass);
         Root<P> root = criteriaQuery.from(entityClass);
 
         Predicate[] predicateArray = null;
         if(predicateBuilders.size() > 0)
         {
             List<Predicate> predicates = new ArrayList<Predicate>();
             for (PredicateBuilder query : predicateBuilders)
             {
                 predicates.add(query.buildPredicate(root,criteriaQuery,cb));
             }
 
             predicateArray = predicates.toArray(new Predicate[predicates.size()]);
         }
 
         PersistentQueryResult<P> result = new PersistentQueryResult<P>();
         Long totalCount = getCount(entityClass,predicateBuilders);
         result.setTotalCount(totalCount);
         if(totalCount == 0)
             return result;
 
         if(predicateArray != null)
             criteriaQuery.where(predicateArray);
 
         if(orderBuilder != null)
         {
             List<Order> orders = new ArrayList<Order>();
             List<Order> orderList = orderBuilder.buildOrders(root, cb);
             orders.addAll(orderList);
             if(orders.size() > 0)
                 criteriaQuery.orderBy(orders.toArray(new Order[orders.size()]));
         }
 
 
         TypedQuery<P> query = em.createQuery(criteriaQuery);
         if(start != null)
             query.setFirstResult(start);
         if(limit != null)
             query.setMaxResults(limit);
 
         result.setResults(query.getResultList());
         return result;
     }
 
     private Long getCount(Class<P> entityClass,List<PredicateBuilder<P>> predicateBuilders)
     {
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<Long> cq = cb.createQuery(Long.class);
         Root<P> root = cq.from(entityClass);
         cq.select(cb.count(root));
 
         Predicate[] predicateArray = null;
         if(predicateBuilders.size() > 0)
         {
             List<Predicate> predicates = new ArrayList<Predicate>();
             for (PredicateBuilder query : predicateBuilders)
             {
                 predicates.add(query.buildPredicate(root,cq,cb));
             }
 
             predicateArray = predicates.toArray(new Predicate[predicates.size()]);
         }
 
         if(predicateArray != null)
             cq.where(predicateArray);
 
         return em.createQuery(cq).getSingleResult();
     }
 
     @Override
     public P find(Class<P> entityClass, ID id)
     {
         return em.find(entityClass,id);
     }
 
     @Override
     public P create(P entity)
     {
         em.persist(entity);
         return entity;
     }
 
     @Override
     public P update(P entity)
     {
         em.merge(entity);
         return entity;
     }
 
     @Override
     public void delete(P entity)
     {
         em.remove(entity);
     }
 }
