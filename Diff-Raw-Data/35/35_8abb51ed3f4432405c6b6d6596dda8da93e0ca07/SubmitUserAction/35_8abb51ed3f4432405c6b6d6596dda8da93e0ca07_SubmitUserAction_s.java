 /*
 *	http://www.jrecruiter.org
 *
 *	Disclaimer of Warranty.
 *
 *	Unless required by applicable law or agreed to in writing, Licensor provides
 *	the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
 *	including, without limitation, any warranties or conditions of TITLE,
 *	NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are
 *	solely responsible for determining the appropriateness of using or
 *	redistributing the Work and assume any risks associated with Your exercise of
 *	permissions under this License.
 *
 */
 package org.jrecruiter.webtier.actions;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.log4j.Logger;
import org.apache.struts.action.*;
 import org.apache.struts.actions.DispatchAction;
 import org.jrecruiter.model.User;
import org.jrecruiter.persistent.dao.DAOException;
import org.jrecruiter.persistent.dao.UserDAOHibernate;
 import org.jrecruiter.service.UserServiceInterface;
 import org.jrecruiter.service.exceptions.DuplicateUserException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;

 /**
  * @author Dorota Puchala
  * @version $Id$
  */
 public class SubmitUserAction extends DispatchAction {
 
     /**
      *   Initialize Logging.
      */
     public static final Logger LOGGER = Logger.getLogger(SubmitUserAction.class);
 
     public ActionForward add(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
 
         DynaActionForm dynaForm = (DynaActionForm) form;
         User user = new User();
         BeanUtils.copyProperties(user, dynaForm);
         ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(servlet.getServletContext());
         UserServiceInterface userService = (UserServiceInterface) context.getBean("userService");
 
         ActionMessages messages = new ActionMessages();
         ActionMessages errors = new ActionMessages();
 
         if (isCancelled(request)) {
 
             messages.add("info", new ActionMessage("errors.cancel"));
             saveMessages(request.getSession(), messages);
 
             return mapping.findForward("cancel");
         }
 
         try {
             userService.addUser(user);
         } catch (DuplicateUserException e) {
 
             LOGGER.warn(e.getMessage());
 
                 errors.add("duplicateUsername", new ActionMessage(
                         "error.duplicateUsername"));
                 saveErrors(request, errors);
                 return mapping.getInputForward();
 
         }
 
         messages.add("info", new ActionMessage("user.add.success"));
         saveMessages(request.getSession(), messages);
         return mapping.findForward("successAdd");
     }
 
     public ActionForward edit(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
 
         ActionMessages messages = new ActionMessages();
 
         if (isCancelled(request)) {
 
             messages.add("info", new ActionMessage("errors.cancel"));
             saveMessages(request, messages);
 
             return mapping.findForward("cancelToAdmin");
         }
 
         DynaActionForm dynaForm = (DynaActionForm) form;
         User user = new User();
         BeanUtils.copyProperties(user, dynaForm);
         SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
         if (!((dynaForm.get("registerDateDF")).equals("") || (dynaForm
                 .get("registerDateDF")) == null)) {
             user.setRegisterDate(new java.sql.Date(formatter.parse(
                     (String) dynaForm.get("registerDateDF")).getTime()));
         }
 
         ApplicationContext context = WebApplicationContextUtils
                 .getRequiredWebApplicationContext(servlet.getServletContext());
         UserServiceInterface userService = (UserServiceInterface) context
                 .getBean("userService");
 
         userService.updateUser(user);
         return mapping.findForward("successEdit");
     }
 
 }
