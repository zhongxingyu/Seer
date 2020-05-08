 package jpaoletti.jpm.hibernate.core;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import jpaoletti.jpm.core.Entity;
 import jpaoletti.jpm.core.Field;
 import jpaoletti.jpm.core.PMException;
 import org.hibernate.Criteria;

 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Junction;
 import org.hibernate.criterion.Restrictions;
 
 public class EntityFilter extends jpaoletti.jpm.core.EntityFilter {
 
     private List<Criterion> filters;
     private Entity entity;
 
     public EntityFilter() {
         super();
         this.setFilters(new ArrayList<Criterion>());
     }
 
     @Override
     public void clear() {
         filters.clear();
     }
 
     @Override
     public void process(Entity entity) {
         this.entity = entity;
     }
 
     protected Criterion getCompareCriterion(String fieldid, String fieldProperty, List<Object> values) {
         Object value_0 = values.get(0);
         switch (getFilterOperation(fieldid)) {
             case LIKE:
                 if (value_0 instanceof String) {
                     return Restrictions.ilike(fieldProperty, "%" + value_0 + "%");
                 } else {
                     return Restrictions.eq(fieldProperty, value_0);
                 }
             case BETWEEN:
                 return Restrictions.between(fieldProperty, value_0, values.get(1));
             case GE:
                 return Restrictions.ge(fieldProperty, value_0);
             case GT:
                 return Restrictions.gt(fieldProperty, value_0);
             case LE:
                 return Restrictions.le(fieldProperty, value_0);
             case LT:
                 return Restrictions.lt(fieldProperty, value_0);
             case NE:
                 return Restrictions.not(Restrictions.eq(fieldProperty, value_0));
             default:
                 return Restrictions.eq(fieldProperty, value_0);
         }
     }
 
     public final void setFilters(List<Criterion> filters) {
         this.filters = filters;
     }
 
     public Criteria applyFilters(Criteria criteria, List<String> aliases) throws PMException {
         Criteria tmpCriteria = criteria;
         //First we create all the needed aliases
         for (Entry<String, List<Object>> entry : getFilterValues().entrySet()) {
             final Field field = entity.getFieldById(entry.getKey());
             if (field == null) {
                 throw new PMException("Undefined field " + entry.getKey());
             }
             final List<Object> values = entry.getValue();
            if (values.get(0) != null) {
                 final String[] splitorder = field.getProperty().split("[.]");
                 for (int i = 0; i < splitorder.length - 1; i++) {
                     final String s = splitorder[i];
                     if (!aliases.contains(s)) {
                         tmpCriteria = tmpCriteria.createAlias(s, s);
                         aliases.add(s);
                     }
                 }
             }
         }
         //Then we add the restrictions
         Junction c = null;
         switch (getBehavior()) {
             case OR:
                 c = Restrictions.disjunction();
                 break;
             default:
                 c = Restrictions.conjunction();
         }
         for (Entry<String, List<Object>> entry : getFilterValues().entrySet()) {
             final Field field = entity.getFieldById(entry.getKey());
             final List<Object> values = entry.getValue();
            if (values.get(0) != null) {
                 c.add(getCompareCriterion(field.getId(), field.getProperty(), values));
             }
         }
         tmpCriteria.add(c);
         return tmpCriteria;
     }
 }
