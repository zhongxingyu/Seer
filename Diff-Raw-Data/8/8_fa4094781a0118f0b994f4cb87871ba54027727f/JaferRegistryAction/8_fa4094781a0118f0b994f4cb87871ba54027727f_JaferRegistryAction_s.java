 /**
  * JAFER Toolkit Project. Copyright (C) 2002, JAFER Toolkit Project, Oxford
  * University. This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of the License,
  * or (at your option) any later version. This library is distributed in the
  * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
  * the GNU Lesser General Public License for more details. You should have
  * received a copy of the GNU Lesser General Public License along with this
  * library; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package org.jafer.registry.web.struts.action;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.validator.DynaValidatorActionForm;
 import org.jafer.registry.RegistryException;
 import org.jafer.registry.RegistryFactory;
 import org.jafer.registry.RegistryManager;
 import org.jafer.registry.ServiceLocator;
 import org.jafer.registry.ServiceManager;
 import org.jafer.registry.uddi.RegistryExceptionImpl;
 import org.jafer.registry.web.struts.Config;
 import org.jafer.registry.web.struts.ParameterKeys;
 import org.jafer.registry.web.struts.SessionKeys;
 
 /**
  * Super class for all actions providing common functions
  */
 public abstract class JaferRegistryAction extends Action
 {
 
     /**
      * Stores a reference to the session
      */
     protected HttpSession session = null;
 
     /**
      * Stores a reference to the mapping
      */
     protected ActionMapping mapping = null;
 
     /**
      * Stores a reference to the request
      */
     protected HttpServletRequest request = null;
 
     /**
      * Stores a reference to the response
      */
     protected HttpServletResponse response = null;
 
     /**
      * Stores a reference to the DynaValidatorActionForm
      */
     protected DynaValidatorActionForm dynaForm = null;
 
     /**
      * Stores a reference to the SUCCESS MAPPING
      */
     protected final static String SUCCESS = "success";
 
     /**
      * Saves all the paramaters for use
      * 
      * @param mapping The struts action mapping
      * @param form The action form sent in the request
      * @param request The request object
      * @param response The response object
      * @return The action to forward to
      * @throws IOException
      * @throws ServletException
      */
     public void initAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
             throws RegistryException, IOException, ServletException
     {
         this.session = request.getSession();
         this.mapping = mapping;
         this.dynaForm = (DynaValidatorActionForm) form;
         this.request = request;
         this.response = response;
         
     }
 
     /**
      * Stores the current return page into the session
      */
     protected void setReturnPage()
     {
         // Stores the return page into the session
         session.setAttribute(SessionKeys.RETURN_PAGE, dynaForm.get(ParameterKeys.CURRENT_PAGE));
     }
 
     /**
      * Gets the current return page
      * 
      * @return The current return page
      */
     protected String getReturnPage()
     {
         return (String) session.getAttribute(SessionKeys.RETURN_PAGE);
     }
     
     /**
      * Get the registry manager creating it if it does not exist
      * 
      * @return An instance of the the registry manager
     * @RegistryException
      */
     protected RegistryManager getRegistryManager() throws RegistryException
     {
         RegistryManager regMan = (RegistryManager) session.getAttribute(SessionKeys.REGISTRY_MANAGER);
         // if not found in the session create and store it
         if (regMan == null)
         {
             regMan = RegistryFactory.createRegistryManager(Config.INQUIRE_URL, Config.PUBLISH_URL);
             session.setAttribute(SessionKeys.REGISTRY_MANAGER, regMan);
         }
         return regMan;
     }
 
     /**
      * Get the service locator creating it if it does not exist
      * 
      * @return An instance of the the service locator
     * @RegistryException
      */
     protected ServiceLocator getServiceLocator() throws RegistryException
     {
         ServiceLocator loc = (ServiceLocator) session.getAttribute(SessionKeys.SERVICE_LOCATOR);
         // if not found in the session create and store it
         if (loc == null)
         {
             loc = getRegistryManager().getServiceLocator();
             session.setAttribute(SessionKeys.SERVICE_LOCATOR, loc);
         }
         return loc;
     }
     
     /**
      * Get the service manager 
      * 
      * @return An instance of the the service manager
     * @RegistryException
      */
     protected ServiceManager getServiceManager() throws RegistryException
     {
         ServiceManager man = (ServiceManager) session.getAttribute(SessionKeys.SERVICE_MANAGER);
         // if not found in the session create and store it
         if (man == null)
         {
             // throw exception as web should have ensured user is logged on
             throw new RegistryExceptionImpl("Expected to find service manager, user not logged on");
         }
         return man;
     }
     
     /**
      * Get the action forward for the submit action
      * 
      * @return The Action Forward to return
      */
     protected ActionForward getActionForward()
     {
         return getActionForward((String) dynaForm.get(ParameterKeys.SUBMIT_ACTION));
     }
 
     /**
      * Get the action forward for the key
      * 
      * @param key The key to lookup in the action mapping
      * @return The Action Forward to return
      */
     protected ActionForward getActionForward(String key)
     {
         // find the mapping for the key
         ActionForward forward = mapping.findForward(key);
         // if not found go to error
         if (forward == null)
         {
             addErrorMessage("errors.badmapping");
             return getErrorPage();
         }
         return new ActionForward(forward);
     }
     
     /**
      * Returns an action forward to the standard error page
      * @return The standard error page forward path
      */
     protected ActionForward getErrorPage()
     {
         return new ActionForward("/error.do");
     }
 
     /**
      * Adds the message looked up from the resource bundle
      * 
      * @param messageKey The key to lookup
      */
     protected void addInfoMessage(String messageKey)
     {
         // need to add invalid login error message here
         ActionMessages messages = new ActionMessages();
         ActionMessage msg = new ActionMessage(messageKey);
         messages.add(ActionMessages.GLOBAL_MESSAGE, msg);
         saveMessages(request, messages);
     }
 
     /**
      * Adds the message looked up from the resource bundle
      * 
      * @param messageKey The key to lookup
      */
     protected void addErrorMessage(String messageKey)
     {
         // need to add invalid login error message here
         ActionMessages messages = new ActionMessages();
         ActionMessage msg = new ActionMessage(messageKey);
         messages.add(ActionMessages.GLOBAL_MESSAGE, msg);
         saveErrors(request, messages);
     }
 }
