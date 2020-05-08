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
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 
 import jamm.backend.MailManager;
 import jamm.backend.MailManagerException;
 import jamm.backend.AccountInfo;
 import jamm.backend.AliasInfo;
 import jamm.backend.MailAddress;
 import jamm.backend.DomainInfo;
 
 /**
  * Loads data via Mail Manager needed for the domain administration
  * page.  It puts the following into the request's attributes after
  * seeding them from the MailManager: domain, accounts,
  * domainAccountForm (a DomainConfigForm), aliases, and
  * domainAliasForm (a DomainConfigForm).  It then forwards to the
  * domain_admin page.
  *
  * @see jamm.backend.MailManager
  * @see jamm.webapp.DomainConfigForm
  * 
 * @struts.action validate="false" path="/private/domain_admin"
  *                roles="Site Administrator, Domain Administrator"
  * @struts.action-forward name="view" path="/private/domain_admin.jsp"
  */
 public class DomainAdminAction extends JammAction
 {
     /**
      * Performs the action.
      *
      * @param mapping The action mapping with possible destinations.
      * @param actionForm Not used in this action.  Is ignored.
      * @param request the http request that caused this action.
      * @param response the http response
      *
      * @return an <code>ActionForward</code>
      *
      * @exception Exception if an error occurs
      */
     public ActionForward execute(ActionMapping mapping,
                                  ActionForm actionForm,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
         throws Exception
     {
         ActionErrors errors = new ActionErrors();
         User user  = getUser(request);
         MailManager manager = getMailManager(user);
         
         String domain = request.getParameter("domain");
         if (domain == null)
         {
             domain = MailAddress.hostFromAddress(user.getUsername());
         }
         if (domain == null)
         {
             errors.add(ActionErrors.GLOBAL_ERROR,
                        new ActionError("general.error.domain.is.null"));
             saveErrors(request, errors);
             return mapping.findForward("general_error");
         }
 
         Map extraInfo = new HashMap();
         extraInfo.put("domain", domain);
         
         request.setAttribute("domain", domain);
 
         Map postmasterPasswordParameters = new HashMap();
         postmasterPasswordParameters.put(
             "mail", MailAddress.addressFromParts("postmaster", domain));
         postmasterPasswordParameters.put("done", "domain_admin");
         request.setAttribute("postmasterPasswordParameters",
                              postmasterPasswordParameters);
 
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
             getDomainAdminForward(mapping, domain).getPath(), "Domain Admin");
         breadCrumbs.add(breadCrumb);
         request.setAttribute("breadCrumbs", breadCrumbs);
 
         doAccounts(request, manager, domain);
         doAliases(request, manager, domain);
         doCatchAll(request, manager, domain);
         doDomainInfo(request, manager, domain);
 
         return (mapping.findForward("view"));
     }
 
     /**
      * Prepares the account information and adds it to the web page.
      *
      * @param request The request we're servicing
      * @param manager a mail manager instance to use
      * @param domain The domain we're manipulating
      * @exception MailManagerException if an error occurs
      */
     private void doAccounts(HttpServletRequest request, MailManager manager,
                             String domain)
         throws MailManagerException
     {
         List accounts = manager.getAccounts(domain);
         request.setAttribute("accounts", accounts);
 
         List activeAccounts = new ArrayList();
         List adminAccounts = new ArrayList();
         List deleteAccounts = new ArrayList();
         Iterator i = accounts.iterator();
         while (i.hasNext())
         {
             AccountInfo account = (AccountInfo) i.next();
             String name = account.getName();
             if (account.isActive())
             {
                 activeAccounts.add(name);
             }
             if (account.isAdministrator())
             {
                 adminAccounts.add(name);
             }
             if (account.getDelete())
             {
                 deleteAccounts.add(name);
             }
         }
 
         String[] activeAccountsArray =
             (String []) activeAccounts.toArray(new String[0]);
         String[] adminAccountsArray =
             (String []) adminAccounts.toArray(new String[0]);
         String[] deleteAccountsArray =
             (String []) deleteAccounts.toArray(new String[0]);
         DomainConfigForm dcf = new DomainConfigForm();
         dcf.setOriginalActiveItems(activeAccountsArray);
         dcf.setActiveItems(activeAccountsArray);
         dcf.setOriginalAdminItems(adminAccountsArray);
         dcf.setAdminItems(adminAccountsArray);
         dcf.setOriginalItemsToDelete(deleteAccountsArray);
         dcf.setItemsToDelete(deleteAccountsArray);
         dcf.setDomain(domain);
         request.setAttribute("domainAccountForm", dcf);
     }
 
     /**
      * Prepares the aliases for the page.
      *
      * @param request the request being serviced
      * @param manager The mail manager to use
      * @param domain which domain are we manipulating
      * @exception MailManagerException if an error occurs
      */
     private void doAliases(HttpServletRequest request, MailManager manager,
                            String domain)
         throws MailManagerException
     {
         List aliases = manager.getAliases(domain);
         request.setAttribute("aliases", aliases);
 
         List activeAliases = new ArrayList();
         List adminAliases = new ArrayList();
         Iterator i = aliases.iterator();
         while (i.hasNext())
         {
             AliasInfo alias = (AliasInfo) i.next();
             if (alias.isActive())
             {
                 activeAliases.add(alias.getName());
             }
             if (alias.isAdministrator())
             {
                 adminAliases.add(alias.getName());
             }
         }
 
         String[] activeAliasesArray =
             (String []) activeAliases.toArray(new String[0]);
         String[] adminAliasesArray =
             (String []) adminAliases.toArray(new String[0]);
         DomainConfigForm dcf = new DomainConfigForm();
         dcf.setOriginalActiveItems(activeAliasesArray);
         dcf.setActiveItems(activeAliasesArray);
         dcf.setOriginalAdminItems(adminAliasesArray);
         dcf.setAdminItems(adminAliasesArray);
         dcf.setDomain(domain);
         request.setAttribute("domainAliasForm", dcf);
     }
 
     /**
      * Prepares the info for the CatchAll.
      *
      * @param request the request being serviced
      * @param manager the mail manager
      * @param domain the domain
      * @exception MailManagerException if an error occurs
      */
     private void doCatchAll(HttpServletRequest request, MailManager manager,
                             String domain)
         throws MailManagerException
     {
         AliasInfo catchAllAlias = manager.getAlias("@" + domain);
         if (catchAllAlias != null)
         {
             List destinations = catchAllAlias.getMailDestinations();
             request.setAttribute("catchAllAlias", destinations.get(0));
         }
         else
         {
             request.setAttribute("catchAllAlias", "");
         }
     }
 
     /**
      * Prepares the domain info
      *
      * @param request the request being serviced
      * @param manager the mail manager
      * @param domain the domain
      * @exception MailManagerException if an error occurs
      */
     private void doDomainInfo(HttpServletRequest request, MailManager manager,
                               String domain)
         throws MailManagerException
     {
         User user = getUser(request);
         
         DomainInfo domainInfo = manager.getDomain(domain);
         if (user.isUserInRole(User.SITE_ADMIN_ROLE) ||
             domainInfo.getCanEditPostmasters())
         {
             request.setAttribute("canEditPostmasters", Boolean.TRUE);
         }
         else
         {
             request.setAttribute("canEditPostmasters", Boolean.FALSE);
         }
 
         if (user.isUserInRole(User.SITE_ADMIN_ROLE) ||
             domainInfo.getCanEditAccounts())
         {
             request.setAttribute("canEditAccounts", Boolean.TRUE);
         }
         else
         {
             request.setAttribute("canEditAccounts", Boolean.FALSE);
         }
     }
 }
