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
 
 
 import java.util.List;
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 import org.apache.commons.beanutils.DynaBean;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.jrecruiter.service.JobServiceInterface;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 /**
  *
  * @author Gunnar Hillert
  * @version $Id$
  */
 public class JobSearchAction extends Action {
     public ActionForward execute(ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
         ApplicationContext context = WebApplicationContextUtils.
         getRequiredWebApplicationContext(servlet.getServletContext());
 
         JobServiceInterface service = (JobServiceInterface) context.
         getBean("jobService");
 
         DynaBean dynaForm = (DynaBean) form;
 
         String keyword = (String) dynaForm.get("keyword");
 
         List jobs = service.searchByKeyword(keyword.toLowerCase());
 
         request.setAttribute("JobList", jobs);
         return mapping.findForward("success");
     }
 
 }
