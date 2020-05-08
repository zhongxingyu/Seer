 /*
  * jPOS Presentation Manager [http://jpospm.blogspot.com]
  * Copyright (C) 2010 Jeronimo Paoletti [jeronimo.paoletti@gmail.com]
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.struts.actions;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.jpos.ee.Constants;
 import org.jpos.ee.pm.core.PMException;
 import org.jpos.ee.pm.core.PMLogger;
 import org.jpos.ee.pm.core.PMMessage;
 import org.jpos.ee.pm.core.PMUnauthorizedException;
 import org.jpos.ee.pm.struts.PMEntitySupport;
 import org.jpos.ee.pm.struts.PMForwardException;
 import org.jpos.ee.pm.struts.PMStrutsContext;
 import org.jpos.ee.pm.struts.PMStrutsService;
 
 /**A super class for all actions with some helpers and generic stuff*/
 public abstract class ActionSupport extends Action implements Constants{
 
     protected abstract void doExecute(PMStrutsContext ctx)throws PMException;
     
     /**Forces execute to check if any user is logged in*/
     protected boolean checkUser(){     return true;}
     
     protected boolean prepare(PMStrutsContext ctx) throws PMException {
         if(checkUser() && ctx.getUser() == null){
             ctx.getRequest().setAttribute("reload", 1);
             throw new PMUnauthorizedException();
         }
         return true;
     }
 
     public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response)throws Exception {
     	PMStrutsContext ctx = (PMStrutsContext) request.getAttribute(PM_CONTEXT);
         ctx.setMapping(mapping);
         ctx.setForm(form);
         try {
             boolean step = prepare(ctx);
             if(step) excecute(ctx);
             return mapping.findForward(SUCCESS);
         } catch (PMForwardException e){
             return mapping.findForward(e.getKey());
         } catch (PMUnauthorizedException e){
             return mapping.findForward(STRUTS_LOGIN);
         } catch (PMException e) {
            PMLogger.debug(this,e);
             if(e.getKey()!=null) ctx.getErrors().add(new PMMessage(ActionMessages.GLOBAL_MESSAGE, e.getKey()));
             ActionErrors errors = new ActionErrors();
             for(PMMessage msg : ctx.getErrors()){
                 errors.add(msg.getKey(), new ActionMessage(msg.getMessage(), msg.getArg0(), msg.getArg1(), msg.getArg2(), msg.getArg3()));
             }
             saveErrors(request, errors);
             return mapping.findForward(FAILURE);
         }
     }
     
     protected void excecute(PMStrutsContext ctx) throws PMException {
         doExecute(ctx);
     }
 
     protected PMStrutsService getPMService()throws PMException{
         try {
             return (PMStrutsService) PMEntitySupport.staticPmservice();
         } catch (Exception e) {
             throw new PMException();
         }
     }   
 }
