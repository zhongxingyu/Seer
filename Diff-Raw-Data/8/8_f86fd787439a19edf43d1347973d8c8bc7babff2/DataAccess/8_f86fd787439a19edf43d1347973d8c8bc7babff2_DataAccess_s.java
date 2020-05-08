 package jpaoletti.jpm.hibernate.core;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import jpaoletti.jpm.core.*;
 import jpaoletti.jpm.core.exception.EntityClassNotFoundException;
 import org.apache.commons.lang.reflect.FieldUtils;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.*;
 
 /**
  * Data access using an hibernate session
  */
 public class DataAccess extends AbstractDataAccess implements PMCoreConstants {
 
     @Override
     public Object getItem(PMContext ctx, String property, String value) throws PMException {
         try {
             /*
              * To avoid the use of SQL restriction that use column name we
              * introspect the property type and parse it. It work for some
              * basics for now until we find a better way.
              *
              * If we get an error or the type is not a Long, Integer, Boolean
              * nor String, we try the old way.
              */
             final Session db = getDb(ctx);
             final Class<?> clazz = Class.forName(getEntity().getClazz());
             final Criteria c = db.createCriteria(clazz);
             c.setMaxResults(1);
             Criterion criterion = null;
             try {
                 final Field f = FieldUtils.getField(clazz, property, true);
                 final Class<?> declaringClass = f.getType();
                 if (declaringClass.equals(Long.class)) {
                     criterion = Restrictions.eq(property, Long.parseLong(value));
                 } else if (declaringClass.equals(Integer.class)) {
                     criterion = Restrictions.eq(property, Integer.parseInt(value));
                 } else if (declaringClass.equals(String.class)) {
                     criterion = Restrictions.eq(property, value);
                 } else if (declaringClass.equals(Boolean.class)) {
                     criterion = Restrictions.eq(property, Boolean.parseBoolean(value));
                 }
             } catch (Exception e) {
                 criterion = Restrictions.sqlRestriction(property + "=" + value);
             }
             if (criterion == null) {
                 criterion = Restrictions.sqlRestriction(property + "=" + value);
             }
             c.add(criterion);
             return c.uniqueResult();
         } catch (Exception e) {
             throw new PMException(e);
         }
     }
 
     /**
      * Get hibernate session from the context
      */
     protected Session getDb(PMContext ctx) {
         return (Session) ctx.getPersistenceManager().getConnection();
     }
 
     @Override
     public List<?> list(PMContext ctx, jpaoletti.jpm.core.EntityFilter filter, ListFilter lfilter, ListSort sort, Integer from, Integer count) throws PMException {
         //We use the filter only if the entity we use is the container one.
         final Criteria list = createCriteria(ctx, getEntity(), filter, lfilter, sort);
         if (count != null) {
             list.setMaxResults(count);
         }
         if (from != null) {
             list.setFirstResult(from);
         }
         return list.list();
     }
 
     @Override
     public void delete(PMContext ctx, Object object) throws PMException {
         getDb(ctx).delete(object);
     }
 
     @Override
     public void update(PMContext ctx, Object object) throws PMException {
         getDb(ctx).update(object);
     }
 
     @Override
     public void add(PMContext ctx, Object object) throws PMException {
         try {
             getDb(ctx).save(object);
        } catch (org.hibernate.exception.ConstraintViolationException e) {
             throw new PMException("constraint.violation.exception", e);
         }
     }
 
     @Override
     public Long count(PMContext ctx) throws PMException {
         final Criteria count = createCriteria(ctx, getEntity(), ctx.getEntityContainer().getFilter(), ctx.getEntityContainer().getList().getListFilter(), null);
         count.setProjection(Projections.rowCount());
         count.setMaxResults(1);
         return (Long) count.uniqueResult();
     }
 
     protected Criteria createCriteria(PMContext ctx, Entity entity, jpaoletti.jpm.core.EntityFilter filter, ListFilter lfilter, ListSort sort) throws PMException {
         final List<String> aliases = new ArrayList<String>();
         Criteria c;
         try {
             c = getDb(ctx).createCriteria(Class.forName(entity.getClazz()));
         } catch (ClassNotFoundException e) {
             throw new EntityClassNotFoundException();
         }
         c.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
         final String order = (sort != null && sort.isSorted() && entity.getFieldById(sort.getFieldId()) != null) ? entity.getFieldById(sort.getFieldId()).getProperty() : null;
         final boolean asc = (sort == null) ? true : sort.getDirection().equals(ListSort.SortDirection.ASC);
         if (order != null) {
             final String[] splitorder = order.split("[.]");
             for (int i = 0; i < splitorder.length - 1; i++) {
                 final String so = splitorder[i];
                 if (!aliases.contains(so)) {
                     c = c.createAlias(so, so);
                     aliases.add(so);
                 }
             }
             if (asc) {
                 c.addOrder(Order.asc(order));
             } else {
                 c.addOrder(Order.desc(order));
             }
         }
         if (lfilter != null) {
             final Object lf = lfilter.getListFilter(ctx);
             if (lf instanceof Criterion) {
                 c.add((Criterion) lf);
             } else if (lf instanceof Map) {
                 Map<String, Object> map = (Map<String, Object>) lf;
                 for (Map.Entry<String, Object> entry : map.entrySet()) {
                     if (entry.getValue() instanceof String) {
                         final String alias = entry.getKey();
                         if (!aliases.contains(alias)) {
                             c = c.createAlias(alias, (String) entry.getValue());
                             aliases.add(alias);
                         }
                     } else if (entry.getValue() instanceof Criterion) {
                         c.add((Criterion) entry.getValue());
                     }
                 }
             }
         }
 
         if (filter != null) {
             c = ((EntityFilter) filter).applyFilters(c, aliases);
         }
         //Weak entities must filter the parent
         if (entity.isWeak()) {
             if (ctx.getEntityContainer(true) != null && ctx.getEntityContainer().getOwner() != null) {
                 if (ctx.getEntityContainer().getOwner().getId().equals(entity.getOwner().getEntityId())) {
                     if (ctx.getEntityContainer().getOwner().getSelected() != null) {
                         final Object instance = ctx.getEntityContainer().getOwner().getSelected().getInstance();
                         final String localProperty = entity.getOwner().getLocalProperty();
                         c.add(Restrictions.eq(localProperty, instance));
                     }
                 }
             }
         }
 
         return c;
     }
 
     @Override
     public Object refresh(PMContext ctx, Object o) throws PMException {
         final Session db = getDb(ctx);
         final Object merged = db.merge(o);
         db.refresh(merged);
         return merged;
     }
 
     @Override
     public EntityFilter createFilter(PMContext ctx) throws PMException {
         return new EntityFilter();
     }
 }
