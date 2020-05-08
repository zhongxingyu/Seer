 package org.filterlib.dao;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.reflect.FieldUtils;
 import org.filterlib.dao.defaultprocessors.ClassProcessor;
 import org.filterlib.dao.defaultprocessors.CollectionProcessor;
 import org.filterlib.dao.defaultprocessors.DefaultProcessor;
 import org.filterlib.dao.defaultprocessors.Interval;
 import org.filterlib.dao.defaultprocessors.IntervalProcessor;
 import org.filterlib.dao.defaultprocessors.OrderProcessor;
 import org.filterlib.dao.defaultprocessors.SortProcessor;
 import org.filterlib.dao.defaultprocessors.StringLikeProcessor;
 import org.filterlib.dao.extension.PreFilterAccessor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageImpl;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.domain.Sort.Order;
 
 /**
  *
  * @author Ondrej.Bozek
  */
 public abstract class AbstractFilteringRepository<T, U extends Pageable> implements FilteringRepository<T, U, Page<T>>
 {
 
     private static final Logger LOG = LoggerFactory.getLogger(AbstractFilteringRepository.class);
     private static Set<String> ignoredFields;
     public static final String ID_FIELD = "id";
     /**
      * by adding custom field processors to this Map, particular filter fields
      * can be individually treated
      *
      */
     protected final Map<Class<CustomFieldProcessor<T, ?>>, CustomFieldProcessor<T, ?>> registeredProcessors =
             new HashMap<Class<CustomFieldProcessor<T, ?>>, CustomFieldProcessor<T, ?>>();
     /**
      * by adding custom field processors to this Map, particular filter fields
      * can be individually treated
      *
      */
     protected static final Map<Class<?>, CustomFieldProcessor> classProcessors;
     /**
      * by adding custom field processors to this Map, particular filter fields
      * can be individually treated
      *
      */
     protected static final Map<Class<?>, CustomFieldProcessor> customClassProcessors = new HashMap<Class<?>, CustomFieldProcessor>();
     /**
      * Pre filters are used for pre-filtering of results, can be used for row
      * level security, multitenancy, shared tables, soft deletes, data history,
      * temporal filtering etc...
      */
     private PreFilterAccessor preFilterAccessor;
 //    protected static final Map<Class<?>, Pageable> preFilters = new HashMap<Class<?>, Pageable>();
 
     static {
         Set<String> fields = new HashSet<String>();
         // fix this it doesn't have to be PageRequest !!
         Field[] criteriaFields = PageRequest.class.getDeclaredFields();
         for (Field field : criteriaFields) {
             fields.add(field.getName());
         }
         // TODO! improve this Hack
         fields.remove("order");
         fields.remove("sort");
         ignoredFields = fields;
 
         Map<Class<?>, CustomFieldProcessor> processors = new HashMap<Class<?>, CustomFieldProcessor>();
         processors.put(Collection.class, new CollectionProcessor());
         processors.put(String.class, new StringLikeProcessor());
         processors.put(Interval.class, new IntervalProcessor());
         processors.put(Object.class, new DefaultProcessor());
         classProcessors = processors;
 
         OrderProcessor orderProcessor = new OrderProcessor();
         addCustomClassProcessor(Order.class, orderProcessor);
         addCustomClassProcessor(Sort.class, new SortProcessor(orderProcessor));
     }
 
     public AbstractFilteringRepository()
     {
     }
 
     public static void setIgnoredFields(Set<String> fields)
     {
         ignoredFields = fields;
     }
 
     public static <C extends Object> void addCustomClassProcessor(Class<C> clazz, ClassProcessor<C> processor)
     {
         customClassProcessors.put(clazz, processor);
     }
 
     /**
      * Generic filtering of Entities using filter fields with names equal to
      * names of fields in Entity, filter fields which do not match fields in
      * Entity, can be individually treated by ad ding entries in to
      * registeredProcessors Map
      *
      * TODO add cache for pre-filter Predicates
      *
      * @param filter
      * @param T
      * @return
      * @throws IllegalAccessException
      */
     @Override
     public Page<T> filter(U filter)
     {
         Class<T> T = returnedClass();
         CriteriaBuilder cb = getEm().getCriteriaBuilder();
         CriteriaQuery<T> criteriaQuery = cb.createQuery(T);
         CriteriaQuery<Long> criteriaQueryCount = cb.createQuery(Long.class);
         Root<T> entity = criteriaQuery.from(T);
         criteriaQueryCount.select(cb.count(entity));
         criteriaQuery.select(entity);
 
         // collect all filters relevant for affected entity
         List<Object> filters = new ArrayList<Object>();
         getPreFilters(filters, T, filter);
 
         filters.add(filter);
         List<Hint> hints = new ArrayList<Hint>();
 
         if (!filters.isEmpty()) {
             List<Predicate> filterPredicates = new ArrayList<Predicate>();
             for (Object queryCriteria : filters) {
 
                 List<Predicate> orPredicates = new ArrayList<Predicate>();
                 List<Predicate> andPredicates = new ArrayList<Predicate>();
                 FilterContextImpl<T> filterContext = new FilterContextImpl<T>(entity, criteriaQuery, getEm(), queryCriteria);
                 hints.addAll(filterContext.getHints());
 
                 List<Field> fields = AbstractFilteringRepository.getInheritedPrivateFields(queryCriteria.getClass());
                 for (Field field : fields) {
                     // I want to skip static fields and fields which are cared of in different(specific way)
                     if (!Modifier.isStatic(field.getModifiers()) && !ignoredFields.contains(field.getName())) {
                         if (!field.isAccessible()) {
                             field.setAccessible(true);
                         }
 
                         /**
                          * Determine field path
                          */
                         // anottaion specified path has always highest priority, so is processed in the first place processing
                         FieldPath fieldPathAnnotation = field.getAnnotation(FieldPath.class);
                         Field f;
                         if (fieldPathAnnotation != null && StringUtils.isNotBlank(fieldPathAnnotation.value())) {
                             f = FieldUtils.getField(T, StringUtils.substringBefore(fieldPathAnnotation.value(), FieldPath.FIELD_PATH_SEPARATOR), true);
                         } else {
                             f = FieldUtils.getField(T, StringUtils.substringBefore(field.getName(), StructuredPathFactory.FILTER_PATH_SEPARATOR), true);
                         }
 
                         // tries to find CustmoProcessor annotation or some annotation metaannotated by custom processor
                         CustomProcessor processor = field.getAnnotation(CustomProcessor.class);
                         if (processor == null) {
                             processor = getMetaAnnotation(CustomProcessor.class, field);
                         }
 
                         ProcessorContext<T> processorContext = filterContext.getProcessorContext(andPredicates, orPredicates, field);
                         Object filterFieldValue = getFilterFieldValue(field, queryCriteria);
                         if (processor == null && f != null) {
                             processTypes(filterFieldValue, processorContext);
                             // If field is not pressent in Entity, it needs special care
                         } else {
                             Class<CustomFieldProcessor<T, ?>> processorClass = null;
                             if (processor != null) {
                                 processorClass = (Class<CustomFieldProcessor<T, ?>>) processor.value();
                                 processCustomFields(filterFieldValue, processorContext, processorClass);
                             } else {
                                 if (!processCustomTypes(filterFieldValue, processorContext)) {
                                     if (shouldCheck(processorContext.getField())) {
                                         LOG.info("Field \'" + processorContext.getField().getName() + "\' from "
                                                 + processorContext.getField().getDeclaringClass().getSimpleName()
                                                 + " wasn't handled. ");
                                         throw new UnsupportedOperationException("Custom filter fields not supported in "
                                                 + processorContext.getField().getDeclaringClass().getSimpleName()
                                                 + ", required field: " + processorContext.getField().getName());
                                     } else {
                                         LOG.info("Field \'" + processorContext.getField().getName() + "\' from "
                                                 + processorContext.getField().getDeclaringClass().getSimpleName()
                                                 + " marked with @Unchecked annotation wasn't handled. ");
                                     }
                                 }
                             }
                         }
                     }
                 }
                 if (!andPredicates.isEmpty() || !orPredicates.isEmpty()) {
                     Predicate filterPredicate = null;
                     if (!andPredicates.isEmpty()) {
                         Predicate andPredicate = cb.and(andPredicates.toArray(new Predicate[1]));
                         filterPredicate = andPredicate;
                     }
                     if (!orPredicates.isEmpty()) {
                         Predicate orPredicate = cb.or(orPredicates.toArray(new Predicate[1]));
                         if (filterPredicate != null) {
                             filterPredicate = cb.and(filterPredicate, orPredicate);
                         } else {
                             filterPredicate = orPredicate;
                         }
                     }
                     filterPredicates.add(filterPredicate);
                 }
             }
             if (!filterPredicates.isEmpty()) {
                 Predicate finalPredicate = cb.and(filterPredicates.toArray(new Predicate[1]));
                 criteriaQuery.where(finalPredicate);
                 criteriaQueryCount.where(finalPredicate);
             }
         }
 
 
         TypedQuery<T> query = getEm().createQuery(criteriaQuery);
         TypedQuery<Long> queryCount = getEm().createQuery(criteriaQueryCount);
         if (filter != null && filter.getPageSize() > 0) {
             query = query.setFirstResult(filter.getOffset());
             query = query.setMaxResults(filter.getPageSize());
         }
         // add hints
         if (!hints.isEmpty()) {
             for (Hint hint : hints) {
                 query.setHint(hint.getName(), hint.getValue());
                 queryCount.setHint(hint.getName(), hint.getValue());
             }
         }
 
 
         PageImpl<T> result = new PageImpl<T>(query.getResultList(), filter, queryCount.getSingleResult().intValue());
         return result;
     }
 
     /**
      * Method for accessing PreFilters, particular implementation is left for
      * users of this library - retrieving of filters from provided filter, or
      * i.e. some session object
      *
      * <p>Pre filters are used for pre-filtering of results. These filters are
      * always utilized for all searches for matching entities.</p> Can be used
      * for: <ul> <li>row level security, </li> <li>multitenancy, </li>
      * <li>shared tables, </li> <li>soft deletes, </li> <li>data history,</li>
      * <li>temporal filtering </li> <li>etc...</li> </ul>
      *
      * If filter for provided class already existed, it will be replaced
      *
      * TODO QueryCriteria is not ideal preFilter ancestor should be replaced
      * with different class TODO add cache for pre-filter Predicates
      *
      * @return
      */
     protected List<Object> getPreFilters(List<Object> list, Class<T> entityClass, Pageable filter)
     {
         if (preFilterAccessor != null) {
            list.addAll(preFilterAccessor.getPreFilters(entityClass, filter));
         }
         return list;
     }
 
     /**
      * Method searches for first annotation on field which is meta-annotated by
      * specified annotation
      *
      * @param metaAnnotation
      * @return first Annotation annotated by specified annotation
      */
     private <T extends Annotation> T getMetaAnnotation(Class<T> metaAnnotation, Field field)
     {
         Annotation[] allAnnotations = field.getAnnotations();
         for (Annotation annotation : allAnnotations) {
             if (annotation.annotationType().isAnnotationPresent(metaAnnotation)) {
                 return annotation.annotationType().getAnnotation(metaAnnotation);
             }
         }
         return null;
     }
 
     /**
      * Method checks if exception should be thrown when there is no proper
      * handling for field
      *
      * @param f
      * @return
      */
     private boolean shouldCheck(Field f)
     {
         boolean fieldChecked = f.getAnnotation(Unchecked.class) == null;
         return fieldChecked && f.getDeclaringClass().getAnnotation(Unchecked.class) == null;
     }
 
     /**
      * <p>Method adds Filter to the Map of pre-filters</p>
      *
      * <p>Pre filters are used for pre-filtering of results. These filters are
      * always utilized for all searches for matching entities.</p> Can be used
      * for: <ul> <li>row level security, </li> <li>multitenancy, </li>
      * <li>shared tables, </li> <li>soft deletes, </li> <li>data history,</li>
      * <li>temporal filtering </li> <li>etc...</li> </ul>
      *
      * If filter for provided class already existed, it will be replaced
      *
      * TODO QueryCriteria is not ideal preFilter ancestor should be replaced
      * with different class TODO add cache for pre-filter Predicates
      *
      * @param entityClass
      * @param criteria
      */
 //    @Override
 //    public <C extends Pageable> void addPreFilter(Class<?> entityClass, C criteria)
 //    {
 //        preFilters.put(entityClass, criteria);
 //    }
 //
 //    /**
 //     *
 //     * @param entityClass
 //     */
 //    @Override
 //    public void removePreFilter(Class<?> entityClass)
 //    {
 //        preFilters.remove(entityClass);
 //    }
     /**
      * Retrieve field value from object (usually Filter)
      *
      * @param field
      * @param filter
      * @return
      */
     private Object getFilterFieldValue(Field field, Object filter)
     {
         Object filterFieldValue = null;
         try {
             filterFieldValue = field.get(filter);
         } catch (IllegalAccessException exception) {
             throw new FieldAccessException(field, filter, exception);
         } catch (IllegalArgumentException exception) {
             throw new FieldAccessException(field, filter, exception);
         }
         return filterFieldValue;
     }
 
     /**
      * Method finds field with specified annotation in provided class
      *
      * @param clazz
      * @param annotation
      * @return
      */
     protected Field getFieldWithAnnotation(Class clazz, Class annotation)
     {
         Field result = null;
         Field fields[] = clazz.getDeclaredFields();
         for (Field field : fields) {
             Annotation an = field.getAnnotation(annotation);
             if (an != null) {
                 return field;
             }
         }
         return result;
     }
 
     /**
      * Method returns all fields from class hierarchy
      *
      * @param type
      * @return
      */
     public static List<Field> getInheritedPrivateFields(Class<?> type)
     {
         List<Field> result = new ArrayList<Field>();
 
         Class<?> i = type;
         while (i != null && i != Object.class) {
             for (Field field : i.getDeclaredFields()) {
                 if (!field.isSynthetic()) {
                     result.add(field);
                 }
             }
             i = i.getSuperclass();
         }
 
         return result;
     }
 
     /**
      * Method returns all fields from class hierarchy
      *
      * @param type
      * @return
      */
     public static Field getInheritedPrivateField(Class<?> type, String fieldName) throws NoSuchFieldException
     {
         Field result = null;
         Class<?> i = type;
         while (i != null && i != Object.class && result == null) {
             try {
                 result = i.getDeclaredField(fieldName);
             } catch (NoSuchFieldException exception) {
                 result = null;
             }
             i = i.getSuperclass();
         }
         if (result == null) {
             throw new NoSuchFieldException("Field " + fieldName + " not found on class " + type);
         }
         return result;
     }
 
     /**
      * This method is used for handling of custom filter fields registered in
      * registeredProcessors Map
      *
      * @param predicates list in which custom predicate should be added
      * @param field custom filter field which can't be cared about generically
      */
     private void processTypes(Object value, ProcessorContext<T> processorContext)
     {
         CustomFieldProcessor cfp = getTypeProcessor(processorContext.getField().getType());
         LOG.debug("processTypes() - CustomFieldProcessor: " + cfp);
         if (cfp == null) {
             cfp = getTypeProcessor(Object.class);
         }
         cfp.processCustomField(value, processorContext);
     }
 
     /**
      * This method is used for handling of custom filter fields registered in
      * registeredProcessors Map
      *
      * @param predicates list in which custom predicate should be added
      * @param field custom filter field which can't be cared about generically
      */
     private boolean processCustomTypes(Object value, ProcessorContext<T> processorContext)
     {
         CustomFieldProcessor cfp = getCustomTypeProcessor(processorContext.getField().getType());
         LOG.debug("processCustomTypes() - CustomFieldProcessor: " + cfp);
         if (cfp != null) {
             cfp.processCustomField(value, processorContext);
             return true;
         }
         return false;
 
     }
 
     /**
      * This method is used for handling of custom filter fields registered in
      * registeredProcessors Map
      *
      * @param predicates list in which custom predicate should be added
      * @param field custom filter field which can't be cared about generically
      */
     private void processCustomFields(Object value, ProcessorContext<T> processorContext,
             Class<CustomFieldProcessor<T, ?>> processorClass)
     {
         CustomFieldProcessor cfp = getFieldProcessor(processorClass);
         if (cfp != null) {
             cfp.processCustomField(value, processorContext);
         }
     }
 
     /**
      * Method retrieves class processor by class
      *
      * @param processorClass
      * @return
      * @throws InstantiationException
      * @throws IllegalAccessException
      */
     public CustomFieldProcessor<T, ?> getTypeProcessor(Class clazz)
     {
         CustomFieldProcessor<T, ?> cfp = classProcessors.get(clazz);
         return cfp;
     }
 
     /**
      * Method retrieves class processor by class
      *
      * @param processorClass
      * @return
      * @throws InstantiationException
      * @throws IllegalAccessException
      */
     public CustomFieldProcessor<T, ?> getCustomTypeProcessor(Class clazz)
     {
         CustomFieldProcessor<T, ?> cfp = customClassProcessors.get(clazz);
         return cfp;
     }
 
     /**
      * Method retrieves field processor by class
      *
      * @param processorClass
      * @return
      * @throws InstantiationException
      * @throws IllegalAccessException
      */
     public CustomFieldProcessor<T, ?> getFieldProcessor(Class<CustomFieldProcessor<T, ?>> processorClass)
     {
         CustomFieldProcessor<T, ?> cfp = registeredProcessors.get(processorClass);
         if (cfp == null) {
             if (processorClass != null) {
                 try {
                     cfp = processorClass.newInstance();
                 } catch (InstantiationException exception) {
                     throw new ProcessorException(processorClass, exception);
                 } catch (IllegalAccessException exception) {
                     throw new ProcessorException(processorClass, exception);
                 }
                 registeredProcessors.put(((Class<CustomFieldProcessor<T, ?>>) processorClass), cfp);
             }
         }
         return cfp;
     }
 
     protected abstract Class<T> returnedClass();
 
     protected abstract EntityManager getEm();
 
     public PreFilterAccessor getPreFilterAccessor()
     {
         return preFilterAccessor;
     }
 
     public void setPreFilterAccessor(PreFilterAccessor preFilterAccessor)
     {
         this.preFilterAccessor = preFilterAccessor;
     }
 }
