 package jpaoletti.jpm.struts.converter;
 
 import jpaoletti.jpm.converter.ConverterException;
 import jpaoletti.jpm.core.*;
 import jpaoletti.jpm.struts.CollectionHelper;
 
 /**
  * Converter for integer <br>
  * <pre>
  * {@code
  * <converter class="jpaoletti.jpm.converter.ObjectConverter">
  *     <operationId>edit</operationId>
  *         <properties>
  *             <property name="entity"          value="other_entity" />
  *             <property name="display"         value="other_entity_display" />
  *             <property name="with-null"       value="true" />
  *             <property name="filter"          value="jpaoletti.jpm.core.ListFilterXX" />
  *             <property name="sort-field"      value="xxx" />
  *             <property name="sort-direction"  value="asc | desc" />
  *             <property name="min-search-size" value="0" />
  *         </properties>
  * </converter>
  * }
  * </pre>
  *
  * @author jpaoletti
  *
  */
 public class ObjectConverter extends StrutsEditConverter {
 
     @Override
     public Object build(PMContext ctx) throws ConverterException {
         try {
             final String _entity = getConfig("entity");
             final Entity entity = ctx.getPresentationManager().getEntity(_entity);
             final String newFieldValue = (String) ctx.getFieldValue();
             if (newFieldValue == null || newFieldValue.trim().compareTo("-1") == 0) {
                 return null;
             }
             return entity.getDataAccess().getItem(ctx, new InstanceId(newFieldValue));
         } catch (PMException ex) {
            throw new ConverterException(ex);
         }
     }
 
     @Override
     public Object visualize(PMContext ctx) throws ConverterException {
         final String _entity = getConfig("entity");
         final Entity entity = ctx.getPresentationManager().getEntity(_entity);
         if (entity == null) {
             throw new ConverterException("object.converter.entity.cannot.be.null");
         }
         if (!entity.isIdentified()) {
             throw new ConverterException("object.converter.id.cannot.be.null");
         }
         final Object fieldValue = ctx.getFieldValue();
         if (fieldValue == null) {
             ctx.put("_selected_value", "");
             ctx.put("_selected_id", "-1");
             ctx.put("_with_null", getConfig("with-null", "false")); //false because selected is already null
         } else {
             final CollectionHelper helper = new CollectionHelper(getConfig("display"));
             try {
                 ctx.put("_selected_value", helper.getObjectDisplay(fieldValue));
                 ctx.put("_selected_id", entity.getDataAccess().getInstanceId(ctx, new EntityInstanceWrapper(fieldValue)).getValue());
                 ctx.put("_with_null", getConfig("with-null", "false"));
             } catch (PMException ex) {
                 throw new ConverterException("object.converter.cannot.get.id");
             }
         }
         ctx.put("_min_search_size", getConfig("min-search-size", "0"));
         ctx.put("_entity", _entity);
         ctx.put("_display", getConfig("display"));
         ctx.put("_filter", getConfig("filter"));
         ctx.put("_filter", getConfig("filter"));
         ctx.put("_sortField", getConfig("sort-field"));
         final String sd = getConfig("sort-direction");
         if (sd != null && !"".equals(sd.trim()) && "desc".equalsIgnoreCase(sd)) {
             ctx.put("_sortDir", "1");
         }
         return super.visualize("object_converter.jsp?");
     }
 }
