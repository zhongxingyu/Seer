 /**
  * 
  */
 package simplejpa.core.repository.jpa;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Order;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.persistence.metamodel.SingularAttribute;
 
 import simplejpa.core.repository.NotFoundException;
 import simplejpa.core.pagination.DefaultPagination;
 import simplejpa.core.pagination.Pagination;
 import simplejpa.core.repository.DefaultRepository;
 
 /**
  * @author Pau Kiat Wee (mailto:paukiatwee@gmail.com)
  * @since 0.1
  */
 class SimpleDefaultRepository implements DefaultRepository {
 
     /*
      * Default items per page
      */
     protected int itemsPerPage = 12;
     
     private static final Logger LOGGER = Logger.getLogger(SimpleDefaultRepository.class.getName());
 
     protected EntityManager entityManager;
 
     public SimpleDefaultRepository() {
 
     }
 
     @PersistenceContext
     public void setEntityManager(EntityManager entityManager) {
         this.entityManager = entityManager;
     }
 
     @Override
     public <T> T create(T model) {
         entityManager.persist(model);
         return model;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> T read(T model) throws NotFoundException {
         // we do not update model, since we might use model to
         // update other fields that should update
         Object id = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(model);
         T result = (T) entityManager.find(model.getClass(), id);
         if(result == null) {
             throw new NotFoundException("Model[" + model.getClass().getName() + "] with id " + id + " not found");
         }
         return result;
     }
 
     @Override
     public <T> List<T> read(List<T> models) throws NotFoundException {
         models = safe(models);
         List<T> results = new ArrayList<T>();
         for(T model: models) {
             results.add(read(model));
         }
         return results;
     }
 
     @Override
     public <T> T update(T model) {
         T merged = entityManager.merge(model);
         entityManager.flush();
         return merged;
     }
 
     @Override
     public <T> void delete(T model) throws NotFoundException {
         if (entityManager.contains(model)) {
             entityManager.remove(model);
         } else {
             model = read(model);
             entityManager.remove(model);
         }
     }
 
     @Override
     public <T> void delete(List<T> models) throws NotFoundException {
         for (T model : models) {
             delete(model);
         }
     }
 
     @Override
     public <T> List<T> getAll(Class<T> type) {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<T> q = cb.createQuery(type);
         q.from(type);
         return safe(entityManager.createQuery(q).getResultList());
     }
 
     @Override
     public <T> List<T> getSlice(Class<T> type, int page) {
         return getSlice(type, page, true, null);
     }
 
     @Override
     public <E, T> List<E> getSlice(Class<E> type, int page,
             boolean asc, List<SingularAttribute<E, T>> attributes) {
         return getSlice(type, page, asc, attributes, getItemsPerPage());
     }
 
     @Override
     public <E, T> List<E> getSlice(Class<E> type, int page, boolean asc, List<SingularAttribute<E, T>> attributes, int limit) {
         List<E> models;
         int startPosition = 0;
         if (page > 0) {
             startPosition = (page - 1) * limit;
         }
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<E> q = cb.createQuery(type);
         Root<E> from = q.from(type);
         if(attributes != null && attributes.size() > 0) {
             List<Order> orders = new ArrayList<Order>();
             for(SingularAttribute<E, T> attribute: attributes) {
                 orders.add(asc? cb.asc(from.get(attribute)): cb.desc(from.get(attribute)));
             }
             q.orderBy(orders);
         }
 
         models = entityManager.createQuery(q).setMaxResults(limit).setFirstResult(startPosition).getResultList();
         return safe(models);
     }
 
     @Override
     public <T> List<T> search(Class<T> type, String property, String value) {
         throw new UnsupportedOperationException();
     }
     @Override
     public <E> Pagination<E> search(Class<E> type, String keyword, int page,
             SingularAttribute<E, String>... attributes) {
 
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<E> q = cb.createQuery(type);
         CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
 
         Root<E> from = q.from(type);
         Root<E> countFrom = countQuery.from(type);
 
         if(!(keyword == null || keyword.trim().isEmpty()) && attributes.length>0){
             if(!keyword.startsWith("%")) {
                 keyword = "%" + keyword;
             }
             if(!keyword.endsWith("%")) {
                 keyword = keyword + "%";
             }
 
 
             List<Predicate> criteria = new ArrayList<Predicate>();
             List<Predicate> countCriteria = new ArrayList<Predicate>();
 
             for(SingularAttribute<E, String> attribute: attributes) {
                 criteria.add(cb.like(cb.upper(from.get(attribute)), keyword.toUpperCase()));
                 countCriteria.add(cb.like(cb.upper(countFrom.get(attribute)), keyword.toUpperCase()));
             }
 
             if(criteria.size() == 1) {
                 q.where(criteria.get(0));
                 countQuery.where(countCriteria.get(0));
             } else {
                 q.where(cb.or(criteria.toArray(new Predicate[0])));
                 countQuery.where(cb.or(countCriteria.toArray(new Predicate[0])));
             }
         }
         countQuery.select(cb.count(countFrom));
         Long totalCount = entityManager.createQuery(countQuery).getSingleResult();
 
         int startPosition = 0;
         if (page > 0) {
             startPosition = (page - 1) * getItemsPerPage();
         }
         List<E> models = entityManager.createQuery(q).setMaxResults(getItemsPerPage()).setFirstResult(startPosition).getResultList();
         
         if(models == null || models.isEmpty()) {
             return new DefaultPagination<E>();
         } else {
             return new DefaultPagination<E>(page, getItemsPerPage(), totalCount.intValue(), models);
         }
     }
 
     @Override
     public <E, T> Pagination<E> search(Class<E> type, T keyword, int page,
                                                      SingularAttribute<E, T>... attributes) {
 
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<E> q = cb.createQuery(type);
         Root<E> from = q.from(type);
         List<Predicate> criteria = new ArrayList<Predicate>();
 
         for(SingularAttribute<E, T> attribute: attributes) {
             criteria.add(cb.equal(from.get(attribute), keyword));
         }
         if(criteria.size() == 1) {
             q.where(criteria.get(0));
         } else {
             q.where(cb.or(criteria.toArray(new Predicate[0])));
         }
 
         Long totalCount = getSearchCount(type, keyword, page, attributes);
         int startPosition = 0;
         if (page > 0) {
             startPosition = (page - 1) * getItemsPerPage();
         }
         List<E> models = entityManager.createQuery(q).setMaxResults(getItemsPerPage()).setFirstResult(startPosition).getResultList();
 
         if(models == null || models.isEmpty()) {
             return new DefaultPagination<E>();
         } else {
             return new DefaultPagination<E>(page, getItemsPerPage(), totalCount.intValue(), models);
         }
     }
 
     private <E, T> Long getSearchCount(Class<E> type, T keyword, int page,
                                                         SingularAttribute<E, T>... attributes){
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
         Root<E> from = countQuery.from(type);
         List<Predicate> criteria = new ArrayList<Predicate>();
         for(SingularAttribute<E, T> attribute: attributes) {
             criteria.add(cb.equal(from.get(attribute), keyword));
         }
         if(criteria.size() == 1) {
             countQuery.where(criteria.get(0));
         } else {
             countQuery.where(cb.or(criteria.toArray(new Predicate[0])));
         }
         countQuery.select(cb.count(from));
         return entityManager.createQuery(countQuery).getSingleResult();
     }
 
     @Override
     public void setItemsPerPage(int itemPerPage) {
         this.itemsPerPage = itemPerPage;
 
     }
 
     @Override
     public int getItemsPerPage() {
         return itemsPerPage;
     }
 
     @Override
     public <T> long getCount(Class<T> type) {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<Long> q = cb.createQuery(Long.class);
         q.select(cb.count(q.from(type)));
         Long result = entityManager.createQuery(q).getSingleResult();
         if (result == null) {
             return 0l;
         } else {
             return result.longValue();
         }
     }
 
     @Override
     public long getCount(String count, Map<String, Object> parameters) {
         TypedQuery<Long> q = entityManager.createQuery(count, Long.class);
         if(parameters != null && !parameters.isEmpty()) {
             for(Map.Entry<String, Object> entry: parameters.entrySet()) {
                 q.setParameter(entry.getKey(), entry.getValue());
             }
         }
         return q.getSingleResult();
     }
 
     @Override
     public <T> Pagination<T> getPage(Class<T> type, int page) {
         return getPage(type, page, true, null);
     }
 
     @Override
     public <E, T> Pagination<E> getPage(Class<E> type,
             int page, boolean asc, List<SingularAttribute<E, T>> attributes) {
         return getPage(type, page, asc, attributes, getItemsPerPage());
     }
 
     @Override
     public <E, T> Pagination<E> getPage(Class<E> type, int page, boolean asc, List<SingularAttribute<E, T>> attributes, int limit) {
         List<E> models = getSlice(type, page, asc, attributes, limit);
         if (models.isEmpty() && page > 1) {
             page = 1;
             models = getSlice(type, 1, asc, attributes, limit);
         }
         if(models == null || models.isEmpty()) {
             return new DefaultPagination<E>();
         } else {
             return new DefaultPagination<E>(page, limit, (int) getCount(type), models);
         }
     }
 
     @Override
     public <E, T> Pagination<E> getPage(
             SingularAttribute<E, T> attribute, T value, int page) {
         List<E> models = getMore(attribute, value, getItemsPerPage());
         if(models.isEmpty() && page > 1) {
             page = 1;
             models = getMore(attribute, value, getItemsPerPage());
         }
         if(models == null || models.isEmpty()) {
             return new DefaultPagination<E>();
         }
         long count = getCount(attribute, value);
         
         return new DefaultPagination<E>(page, getItemsPerPage(), (int)count, models);
     }
 
     @Override
     public <E, T> Pagination<E> getPage(Class<E> type, int page, String query, String count, int limit, Map<String, Object> parameters) {
         List<E> models = getListOf(type, query, page, limit, parameters);
         if(models.isEmpty() && page > 1) {
             page = 1;
             models = getListOf(type, query, page, limit, parameters);
         }
         if(models == null || models.isEmpty()) {
             return new DefaultPagination<E>();
         }
        return new DefaultPagination<E>(page, getItemsPerPage(), (int)getCount(count, parameters), models);
     }
     
     @Override
     public <E, T> List<E> getMore(SingularAttribute<E, T> attribute, T value) {
         return generateTypedQuery(attribute, attribute.getDeclaringType().getJavaType(), value).getResultList();
     }
     
     @Override
     public <E, T> List<E> getMore(SingularAttribute<E, T> attribute, T value, int limit) {
         return generateTypedQuery(attribute, attribute.getDeclaringType().getJavaType(), value).setMaxResults(limit).getResultList();
     }
     
     @Override
     public <E, T> List<E> getMore(SingularAttribute<E, T> attribute, T value, int limit, int page) {
         int startPosition = 0;
         if (page > 0) {
             startPosition = (page - 1) * limit;
         }
         return generateTypedQuery(attribute, attribute.getDeclaringType().getJavaType(), value).setMaxResults(limit).setFirstResult(startPosition).getResultList();
     }
 
     @Override
     public <E, T> List<E> getMore(SingularAttribute<E, T> attribute, int limit) {
 
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<E> q = cb.createQuery(attribute.getDeclaringType().getJavaType());
         Root<E> root = q.from(attribute.getDeclaringType().getJavaType());
         q.orderBy(cb.desc(root.get(attribute)));
         return entityManager.createQuery(q).setMaxResults(limit).getResultList();
     }
 
     @Override
     public <E, T> List<E> getOrderBy(SingularAttribute<E, T> attribute, int limit, boolean asc) {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         Class<E> entityClass = attribute.getDeclaringType().getJavaType();
         CriteriaQuery<E> q = cb.createQuery(entityClass);
         Root<E> root = q.from(entityClass);
         q.orderBy(asc ? cb.asc(root.get(attribute)) : cb.desc(root.get(attribute)));
         return entityManager.createQuery(q).setMaxResults(limit).getResultList();
     }
 
     @Override
     public <E, T> List<E> getGroupBy(SingularAttribute<E, T> attribute) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public <E, T> E getOne(SingularAttribute<E, T> attribute, T value) throws NotFoundException {
         E result = null;
         try {
             result = generateTypedQuery(attribute, attribute.getDeclaringType().getJavaType(), value).getSingleResult();
         } catch (NoResultException e) {
             throw new NotFoundException(e.getMessage(), e);
         }
         return result;
     }
 
     @Override
     public <E, T> long getCount(SingularAttribute<E, T> attribute, T value) {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<Long> q = cb.createQuery(Long.class);
         Root<E> from = q.from(attribute.getDeclaringType().getJavaType());
         q.select(cb.count(from));
         Predicate predicate = cb.equal(from.get(attribute), value);
         q.where(predicate);
         Long result = entityManager.createQuery(q).getSingleResult();
         return result == null ? 0l : result.longValue();
     }
 
     protected <E, T> TypedQuery<E> generateTypedQuery(SingularAttribute<E, T> attribute, Class<E> type,
             T value) {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<E> q = cb.createQuery(type);
         Root<E> from = q.from(type);
         Predicate predicate = cb.equal(from.get(attribute), value);
         q.where(predicate);
         return entityManager.createQuery(q);
     }
 
     @Override
     public <E, T> List<E> getOrderBy(
             SingularAttribute<E, T> attribute, boolean asc) {
         return getOrderBy(attribute, getItemsPerPage(), asc);
     }
     
     private <T> List<T> safe(List<T> list) {
         if(list == null) {
             list = new ArrayList<T>();
         }
         
         return list;
     }
 
     @Override
     public <T> List<T> getListOf(Class<T> type, String query) {
         return entityManager.createQuery(query, type).getResultList();
     }
 
     @Override
     public <T> List<T> getListOf(Class<T> type, String query, int page, Map<String, Object> parameters) {
         return getListOf(type, query, page, getItemsPerPage(), parameters);
     }
     
     @Override
     public <T> List<T> getListOf(Class<T> type, String query, int page, int limit, Map<String, Object> parameters) {
         int startPosition = 0;
         if (page > 0) {
            startPosition = (page - 1) * getItemsPerPage();
         }
         TypedQuery<T> q = entityManager.createQuery(query, type);
         if(parameters != null && !parameters.isEmpty()) {
             for(Map.Entry<String, Object> entry: parameters.entrySet()) {
                 q.setParameter(entry.getKey(), entry.getValue());
             }
         }
        return q.setFirstResult(startPosition).getResultList();
     }
 }
