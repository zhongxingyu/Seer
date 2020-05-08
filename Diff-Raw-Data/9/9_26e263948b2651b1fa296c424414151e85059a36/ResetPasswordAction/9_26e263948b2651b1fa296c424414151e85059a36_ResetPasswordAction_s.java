 package org.jpos.ee.pm.security.ui.actions;
 
 import org.jpos.ee.pm.core.PMException;
 import org.jpos.ee.pm.security.core.operations.ResetPassword;
 import org.jpos.ee.pm.struts.PMStrutsContext;
 import org.jpos.ee.pm.struts.actions.ActionSupport;
 
 public class ResetPasswordAction extends ActionSupport {
 
     @Override
     protected void doExecute(PMStrutsContext ctx) throws PMException {
         final ResetPassword op = new ResetPassword("resetpsw");
        op.excecute(ctx);
     }
 }
