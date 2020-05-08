 package com.lsy.vehicle.security.dao.spi;
 
 import java.util.EnumSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Expression;
 import javax.persistence.criteria.Order;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.persistence.metamodel.EntityType;
 import javax.persistence.metamodel.Metamodel;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.stereotype.Repository;
 
 import com.lsy.vehicle.security.dao.UserDao;
 import com.lsy.vehicle.security.domain.Role;
 import com.lsy.vehicle.security.domain.User;
 import com.lsy.vehicle.security.filter.SortOrder;
 import com.lsy.vehicle.security.filter.UserColumn;
 import com.lsy.vehicle.security.filter.UserFilterParameters;
 import com.lsy.vehicle.security.filter.UserFilterParameters.ColumnEntry;
 
 @Repository
 public class UserDaoBean implements UserDao {
     
     @PersistenceContext
     private EntityManager em;
 
     @Override
     public User find(Long id) {
         return em.find(User.class,id);
     }
 
     @Override
     public void create(User user) {
         em.persist(user);
     }
 
     @Override
     public void update(User user) {
         em.merge(user);
     }
 
     @Override
     public void delete(User user) {
         em.remove(user);
     }
 
     @Override
     public User findByUsername(String username) {
         TypedQuery<User> query = em.createNamedQuery(User.FIND_BY_USERNAME, User.class);
         query.setParameter("username", username);
         return query.getSingleResult();
     }
 
     @Override
     public List<User> findAll() {
         return em.createNamedQuery(User.FIND_ALL, User.class).getResultList();
     }
 
     @Override
     public List<User> findAllOfRole(Role role) {
         TypedQuery<User> query = em.createNamedQuery(User.FIND_BY_ROLES, User.class);
         query.setParameter("role", role);
         return query.getResultList();
     }
 
     
     
     @Override
     public List<User> findAllCustomersNotMemberOfCompany(String companyName) {
         TypedQuery<User> query = em.createNamedQuery(User.FIND_NO_CUSTOMER_MEMBER_BY_COMPANY_NAME, User.class);
         query.setParameter("companyName", companyName);
         return query.getResultList();
     }
     
     
     @Override
     public List<User> findByFilter(String username, String email, String firstname,
                     String surename, Role role) {
         
         CriteriaBuilder builder = em.getCriteriaBuilder();
         Metamodel metaModel = em.getMetamodel();
         EntityType<User> user_ = metaModel.entity(User.class);
         
         // SELECT u FROM User u
         CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
         Root<User> user = criteriaQuery.from(user_);
         
         // .. WHERE (filter = ?) ...
         List<Predicate> predicates = buildPredicates(username, email, firstname, surename, role, builder, user);
         
         Predicate conjunction = conjunction(builder, predicates);
         if (conjunction != null) {
             criteriaQuery.where(conjunction);
         }
         
         TypedQuery<User> query = em.createQuery(criteriaQuery);
         if (StringUtils.isNotBlank(firstname))
             query.setParameter("firstname", firstname);
         return query.getResultList();
     }
 
     private List<Predicate> buildPredicates(String username, String email, String firstname, String surename,
                     Role role, CriteriaBuilder builder, Root<User> user) {
         List<Predicate> predicates = new LinkedList<>();
         if (StringUtils.isNotBlank(username)) {
             Expression<String> expression = user.get("username");
             predicates.add(builder.like(expression, username));
         }
         if (StringUtils.isNotBlank(email)) {
             Expression<String> expression = user.get("email");
             predicates.add(builder.like(expression, email));
         }
         if (StringUtils.isNotBlank(firstname)) {
             Expression<String> expression = user.get("firstname");
             Expression<String> parameter = builder.parameter(String.class,"firstname");
             predicates.add(builder.like(expression, parameter));
 //            predicates.add(builder.like(expression, firstname));
         }
         if (StringUtils.isNotBlank(surename)) {
             Expression<String> expression = user.get("surename");
             predicates.add(builder.like(expression, surename));
         }
         if (role != null) {
             Expression<Role> expression = user.get("role");
             predicates.add(builder.equal(expression, role));
         }
         return predicates;
     }
     @Override
     public List<User> findByFilter(UserFilterParameters userFilter) {
         CriteriaBuilder builder = em.getCriteriaBuilder();
         Metamodel metaModel = em.getMetamodel();
         EntityType<User> user_ = metaModel.entity(User.class);
         
         // SELECT u FROM User u
         CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
         Root<User> user = criteriaQuery.from(user_);
         
         // ... WHERE (filter = ?) ...
         List<Predicate> predicates = buildPredicates(userFilter, builder, user);
         Predicate junction = junction(userFilter, builder, predicates);
         if (junction != null) {
             criteriaQuery.where(junction);
         }
 
         // ... ORDER BY ...
         List<Order> orders = buildOrderBy(userFilter, builder, user);
         if (!orders.isEmpty()) {
             criteriaQuery.orderBy(orders);
         }
         
         TypedQuery<User> query = em.createQuery(criteriaQuery);
        query.setFirstResult(0);
         query.setMaxResults(50);
         return query.getResultList();
     }
 
     private List<Order> buildOrderBy(UserFilterParameters userFilter, CriteriaBuilder builder, Root<User> user) {
         List<Order> orders = new LinkedList<>();
         for (ColumnEntry entry : userFilter.getSortedColumns()) {
             if (entry.getSortOrder() == SortOrder.ASCENDING) {
                 orders.add(builder.asc(user.get(entry.getColumn().columnName())));
             } else {
                 orders.add(builder.desc(user.get(entry.getColumn().columnName())));
             }
         }
         return orders;
     }
 
     private Predicate junction(UserFilterParameters userFilter, CriteriaBuilder builder, List<Predicate> predicates) {
         if (userFilter.isDisjunction()) {
             return disjunction(builder, predicates);
         } else {
             return conjunction(builder, predicates);
         }
     }
     
     private Predicate conjunction(CriteriaBuilder builder, List<Predicate> predicates) {
         Predicate current = null;
         for (Predicate item: predicates) {
             if (current == null) {
                 current = item;
             } else {
                 current = builder.and(current, item);
             }
         }
         return current;
     }
     
     private Predicate disjunction(CriteriaBuilder builder, List<Predicate> predicates) {
         Predicate current = null;
         for (Predicate item: predicates) {
             if (current == null) {
                 current = item;
             } else {
                 current = builder.or(current, item);
             }
         }
         return current;
     }
     
 
     private List<Predicate> buildPredicates(UserFilterParameters userFilter, CriteriaBuilder builder, Root<User> user) {
         List<Predicate> predicates = new LinkedList<>();
         for (UserColumn column : EnumSet.allOf(UserColumn.class)) {
             ColumnEntry entry = userFilter.getColumn(column);
             if (StringUtils.isNotBlank(entry.getFilter())) {
                 // ATTENTION: Convention enum names and column names are equal 
                 Expression<String> expression = user.get(column.toString().toLowerCase());
                 if (column == UserColumn.ROLE) {
                     predicates.add(builder.equal(expression, Role.valueOf(entry.getFilter())));
                 } else {
                     predicates.add(builder.like(expression, entry.wildcharFilter()));
                 }
             }
         }
         return predicates;
     }
 }
