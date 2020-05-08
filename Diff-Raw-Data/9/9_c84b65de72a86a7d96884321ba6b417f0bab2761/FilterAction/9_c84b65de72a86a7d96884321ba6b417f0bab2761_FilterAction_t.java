 package jpaoletti.jpm.struts.actions;
 
 import jpaoletti.jpm.core.PMException;
 import jpaoletti.jpm.core.operations.PMFilterOperation;
 import jpaoletti.jpm.struts.PMForwardException;
 import jpaoletti.jpm.struts.PMStrutsContext;
 
 public class FilterAction extends ActionSupport {
 
     @Override
     protected void doExecute(PMStrutsContext ctx) throws PMException {
         final boolean finish = ctx.getParameter("finish") != null;
         ctx.put("validate", false);
        ctx.put("editable", true);
 
         PMFilterOperation op = new PMFilterOperation("filter");
         op.execute(ctx);
 
         if (finish) {
             success(ctx, "/list.do", true);
         } else {
             throw new PMForwardException(CONTINUE);
         }
     }
 }
