 package jpaoletti.jpm.struts.actions;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import jpaoletti.jpm.core.PMCoreConstants;
 import jpaoletti.jpm.core.PMException;
 import jpaoletti.jpm.core.exception.NotAuthorizedException;
 import jpaoletti.jpm.core.PresentationManager;
 import jpaoletti.jpm.core.exception.NotAuthenticatedException;
 import jpaoletti.jpm.core.message.MessageFactory;
 import jpaoletti.jpm.struts.PMEntitySupport;
 import jpaoletti.jpm.struts.PMForwardException;
 import jpaoletti.jpm.struts.PMStrutsConstants;
 import jpaoletti.jpm.struts.PMStrutsContext;
 
 /**
  * A super class for all actions with some helpers and generic stuff
  *
  * @author jpaoletti
  */
 public abstract class ActionSupport extends Action implements PMCoreConstants, PMStrutsConstants {
 
     protected abstract void doExecute(PMStrutsContext ctx) throws PMException;
 
     /**Forces execute to check if any user is logged in*/
     protected boolean checkUser() {
         return true;
     }
 
     protected boolean prepare(PMStrutsContext ctx) throws PMException {
         if (checkUser() && ctx.getPmsession() == null) {
             //Force logout
             final PMEntitySupport es = PMEntitySupport.getInstance();
             ctx.getSession().invalidate();
             es.setContext_path(ctx.getRequest().getContextPath());
             ctx.getSession().setAttribute(ENTITY_SUPPORT, es);
             ctx.getRequest().setAttribute("reload", 1);
             throw new NotAuthenticatedException();
         }
         return true;
     }
 
     @Override
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
         final PMStrutsContext ctx = (PMStrutsContext) request.getAttribute("ctx");
         ctx.setMapping(mapping);
         ctx.setForm(form);
         try {
             boolean step = prepare(ctx);
             if (step) {
                 excecute(ctx);
                 if (ctx.getOperation() != null && ctx.getOperation().getFollows() != null) {
                    return new ActionForward("/" + ctx.getOperation().getFollows() + ".do");
                 }
             }
             return mapping.findForward(SUCCESS);
         } catch (PMForwardException e) {
             if (e.getActionForward() != null) {
                 return e.getActionForward();
             } else {
                 return mapping.findForward(e.getKey());
             }
         } catch (NotAuthenticatedException e) {
             return ctx.fwdLogin();
         } catch (NotAuthorizedException e) {
             return ctx.fwdDeny();
         } catch (PMException e) {
             ctx.getPresentationManager().debug(this, e);
             if (e.getKey() != null) {
                 ctx.addMessage(MessageFactory.error(e.getKey()));
             }
             return mapping.findForward(FAILURE);
         }
     }
 
     /**
      * Return the context path of the application
      */
     protected String getContextPath() {
         return PMEntitySupport.getInstance().getContext_path();
     }
 
     protected void excecute(PMStrutsContext ctx) throws PMException {
         doExecute(ctx);
     }
 }
