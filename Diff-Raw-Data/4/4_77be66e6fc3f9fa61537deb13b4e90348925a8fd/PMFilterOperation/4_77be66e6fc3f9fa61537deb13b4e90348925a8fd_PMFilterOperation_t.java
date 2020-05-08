 package jpaoletti.jpm.core.operations;
 
 import java.util.ArrayList;
 import java.util.List;
import jpaoletti.jpm.converter.Converter;
import jpaoletti.jpm.converter.ConverterException;
import jpaoletti.jpm.converter.IgnoreConvertionException;
 import jpaoletti.jpm.core.*;
 import jpaoletti.jpm.core.message.MessageFactory;
 import jpaoletti.jpm.util.DisplacedList;
 
 /**
  *
  * @author jpaoletti
  */
 public class PMFilterOperation extends OperationCommandSupport {
 
     public PMFilterOperation(String operationId) {
         super(operationId);
     }
 
     @Override
     protected boolean prepare(PMContext ctx) throws PMException {
         super.prepare(ctx);
         ctx.getEntityContainer().setSelected(null);
         if (ctx.getParameter("finish") == null) {
             if (ctx.getEntityContainer().getFilter() == null) {
                 //Creates filter bean and put it in session
                 ctx.getEntityContainer().setFilter(ctx.getEntity().getDataAccess().createFilter(ctx));
             }
             return false;
         } else {
             final EntityFilter filter = ctx.getEntityContainer().getFilter();
             filter.clear();
             for (Field field : ctx.getEntity().getAllFields()) {
                 if (field.shouldDisplay(ctx.getOperation().getId())) {
                     filter.addFilter(field.getId(), getFilterValues(ctx, field), getFilterOperation(ctx, field));
                 }
             }
             filter.process(ctx.getEntity());
             return true;
         }
     }
 
     @Override
     protected void doExecute(PMContext ctx) throws PMException {
         super.doExecute(ctx);
         final PaginatedList pmlist = ctx.getList();
         final DisplacedList<Object> contents = new DisplacedList<Object>();
         Long total = null;
         contents.addAll((List<Object>) ctx.getEntity().getList(ctx, ctx.getEntityContainer().getFilter(), pmlist.getSort(), pmlist.from(), pmlist.rpp()));
         if (!ctx.getEntity().getNoCount()) {
             total = ctx.getEntity().getDataAccess().count(ctx);
         }
         PaginatedList pmList = ctx.getList();
         pmList.setContents(contents);
         pmList.setTotal(total);
 
     }
 
     private FilterOperation getFilterOperation(final PMContext ctx, final Field field) {
         String eid = "filter_oper_f_" + field.getId();
         String oper = (String) ctx.getParameter(eid);
         if (oper != null) {
             try {
                 int i = Integer.parseInt(oper);
                 switch (i) {
                     case 0:
                         return FilterOperation.EQ;
                     case 1:
                         return FilterOperation.NE;
                     case 2:
                         return FilterOperation.LIKE;
                     case 3:
                         return FilterOperation.GT;
                     case 4:
                         return FilterOperation.GE;
                     case 5:
                         return FilterOperation.LT;
                     case 6:
                         return FilterOperation.LE;
                     case 7:
                         return FilterOperation.BETWEEN;
                     default:
                         return FilterOperation.EQ;
                 }
             } catch (Exception e) {
                 return FilterOperation.EQ;
             }
         } else {
             return FilterOperation.EQ;
         }
     }
 
     private List<Object> getFilterValues(PMContext ctx, Field field) throws ConverterException {
         final List<Object> parameterValues = getParameterValues(ctx, field);
         final List<Object> values = new ArrayList<Object>();
         int i = 0;
         for (Object value : parameterValues) {
             try {
                 final Converter converter = field.getConverters().getConverterForOperation(ctx.getOperation().getId());
                 Object converted = getConvertedValue(ctx, field, value, null, converter);
                 values.add(converted);
             } catch (IgnoreConvertionException e) {
                 //Do nothing, just ignore conversion.
             } catch (ConverterException e) {
                 ctx.getPresentationManager().error(e);
                 ctx.addMessage(MessageFactory.error(ctx.getEntity(), field, e.getKey()));
             } catch (Exception e) {
                 ctx.getPresentationManager().error(e);
                 ctx.addMessage(MessageFactory.error(ctx.getEntity(), field, UNESPECTED_ERROR));
             }
             i++;
         }
         return values;
     }
 
     @Override
     protected boolean checkEntity() {
         return true;
     }
 }
