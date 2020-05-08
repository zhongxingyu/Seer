 /*
  * Jamm
  * Copyright (C) 2002 Dave Dribin and Keith Garner
  *  
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package jamm.webapp;
 
 import java.util.List;
 import java.util.ArrayList;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 
 import jamm.backend.MailManager;
 import jamm.backend.AliasInfo;
 
 /**
  * Updates the catch-all.
  * @struts.action validate="false" path="/private/catch_all_admin"
  *                roles="Site Administrator, Domain Administrator"
  */
 public class CatchAllAdminAction extends JammAction
 {
     /**
      * Performs the action.
      *
      * @param mapping Where we get our forwards from.
      * @param actionForm This action does not depend on a form,
      *                   so this should be null.
      * @param request The http request that caused this action.
      * @param response The http response that will be returned from
      *                 this action.
      *
      * @return the ActionForward of the next page to show.
      *
      * @exception Exception if an error occurs
      */
     public ActionForward execute(ActionMapping mapping,
                                  ActionForm actionForm,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
         throws Exception
     {
         User user = getUser(request);
         MailManager manager = getMailManager(user);
         String domain = request.getParameter("domain");
 
         // Create the bread crumbs
         List breadCrumbs = new ArrayList();
         BreadCrumb breadCrumb;
         if (user.isUserInRole(User.SITE_ADMIN_ROLE))
         {
             breadCrumb = new BreadCrumb(
                 findForward(mapping, "site_admin", request).getPath(),
                 "Site Admin");
             breadCrumbs.add(breadCrumb);
         }
 
         breadCrumb = new BreadCrumb(
             getDomainAdminForward(mapping, domain).getPath(),
             "Domain Admin");
         breadCrumbs.add(breadCrumb);
         request.setAttribute("breadCrumbs", breadCrumbs);
 
         // Populate the form
         AliasInfo catchAll = manager.getAlias("@" + domain);
         UpdateCatchAllForm ucaf = new UpdateCatchAllForm();
         if (catchAll != null)
         {
             ucaf.setCatchAllOn();
             List destinations = catchAll.getMailDestinations();
             ucaf.setDestination((String) destinations.get(0));
         }
         else
         {
             ucaf.setCatchAllOff();
         }
         ucaf.setDomain(domain);
         request.setAttribute("updateCatchAllForm", ucaf);
         request.setAttribute("domain", domain);
 
         return (mapping.findForward("view"));
     }
 }
