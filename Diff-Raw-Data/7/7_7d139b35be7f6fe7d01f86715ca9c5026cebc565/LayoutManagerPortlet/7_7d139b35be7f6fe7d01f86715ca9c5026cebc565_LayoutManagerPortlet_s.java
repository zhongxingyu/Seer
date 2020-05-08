 /*
  * @author <a href="mailto:michael.russell@aei.mpg.de">Michael Russell</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.admin.layout;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.provider.portletui.model.DefaultTableModel;
 import org.gridlab.gridsphere.services.core.layout.LayoutManagerService;
 import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
 import org.gridlab.gridsphere.layout.*;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.xml.sax.InputSource;
 
 import javax.servlet.UnavailableException;
 import java.util.*;
 import java.io.*;
 
 public class LayoutManagerPortlet extends ActionPortlet {
 
     // JSP pages used by this portlet
     public static final String VIEW_JSP = "admin/layout/view.jsp";
     public static final String EDIT_JSP = "admin/layout/edit.jsp";
 
     // Portlet services
     private LayoutManagerService layoutMgr = null;
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         this.log.debug("Entering initServices()");
         try {
             this.layoutMgr = (LayoutManagerService)config.getContext().getService(LayoutManagerService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to initialize services!", e);
         }
         this.log.debug("Exiting initServices()");
         //portletMgr = PortletManager.getInstance();
 
         DEFAULT_VIEW_PAGE = "doShowLayout";
         DEFAULT_HELP_PAGE = "admin/layout/help.jsp";
 
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
     }
 
     public void refreshLayout(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         layoutMgr.refreshPage(req);
     }
 
     public void doShowLayout(FormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
 
         TextAreaBean ta = event.getTextAreaBean("bannerTA");
 
         String filename = this.getPortletConfig().getContext().getRealPath("/html/pagehead.html");
         BufferedReader reader = new BufferedReader(new FileReader(filename));
         String line = null;
         StringBuffer sb = new StringBuffer();
         while ((line = reader.readLine()) != null) {
             sb.append(line);
             sb.append("\n");
         }
         ta.setValue(sb.toString());
 
         Map tabs = PortletTabRegistry.getApplicationTabs();
 
         List tabNames = new ArrayList();
         Iterator it = tabs.keySet().iterator();
         while (it.hasNext()) {
             tabNames.add((String)it.next());
         }
 
         req.setAttribute("tabNames", tabNames);
 
         Map groups = PortletTabRegistry.getGroupTabs();
         it = groups.keySet().iterator();
         List groupNames = new ArrayList();
         while (it.hasNext()) {
             groupNames.add((String)it.next());
         }
         req.setAttribute("groupNames", groupNames);
 
         setNextState(req, VIEW_JSP);
     }
 
     public AccessControlManagerService getACLService(User user) {
         AccessControlManagerService aclManagerService = null;
         try {
             aclManagerService = (AccessControlManagerService)this.getConfig().getContext().getService(AccessControlManagerService.class, user);
         } catch (PortletServiceException e) {
             log.error("Unable to initialize services!", e);
         }
         return aclManagerService;
     }
 
     public void saveBanner(FormEvent event) throws PortletException, IOException {
         this.checkSuperRole(event);
         TextAreaBean ta = event.getTextAreaBean("bannerTA");
         String newText = ta.getValue();
         String filename = this.getPortletConfig().getContext().getRealPath("/html/pagehead.html");
         BufferedReader reader = new BufferedReader(new FileReader(filename));
 
         FileWriter f = new FileWriter(filename);
 
         f.write(newText);
         f.close();
 
     }
 
     public void editGroupLayout(FormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getPortletRequest();
 
         String group = event.getAction().getParameter("group");
 
         String groupLayoutPath = PortletTabRegistry.getTabDescriptorPath(group);
 
         Boolean allowImport = Boolean.TRUE;
 
         if (PortletTabRegistry.getApplicationTabs(group) != null) {
             allowImport = Boolean.FALSE;
         }
 
         TextAreaBean ta = event.getTextAreaBean("layoutFile");
         HiddenFieldBean hf = event.getHiddenFieldBean("layoutHF");
         hf.setName("group");
         hf.setValue(group);
         req.setAttribute("name", group);
         req.setAttribute("allowImport", allowImport);
 
         ListBoxBean appsLB = event.getListBoxBean("appsLB");
 
         Map tabs = PortletTabRegistry.getApplicationTabs();
 
         List tabNames = new ArrayList();
         Iterator it = tabs.keySet().iterator();
         while (it.hasNext()) {
             ListBoxItemBean item = new ListBoxItemBean();
             item.setName((String)it.next());
             appsLB.addBean(item);
         }
 
         BufferedReader reader = new BufferedReader(
                 new InputStreamReader(new FileInputStream(groupLayoutPath), "UTF8"));
 
         String line = null;
         StringBuffer sb = new StringBuffer();
         while ((line = reader.readLine()) != null) {
             sb.append(line);
             sb.append("\n");
         }
         ta.setValue(sb.toString());
 
         setNextState(req, EDIT_JSP);
     }
 
     public void cancelLayout(FormEvent event) throws PortletException, IOException {
         setNextState(event.getPortletRequest(), DEFAULT_VIEW_PAGE);
     }
 
     public void saveLayout(FormEvent event) throws PortletException, IOException {
         //this.checkSuperRole(event);
 
         User user = event.getPortletRequest().getUser();
         AccessControlManagerService aclService = getACLService(user);
         HiddenFieldBean groupHF = event.getHiddenFieldBean("layoutHF");
         TextAreaBean ta = event.getTextAreaBean("layoutFile");
         String newText = ta.getValue();
 
         if (groupHF.getValue().equals("guest")) {
             this.checkSuperRole(event);
             String guestFile = PortletTabRegistry.getGuestLayoutFile();
             Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(guestFile), "UTF-8"));
             byte[] text = newText.getBytes("iso-8859-1");
             String newstring = new String(text, "UTF-8");
             out.write(newstring);
             out.close();
             try {
                 PortletTabRegistry.reloadGuestLayout();
             } catch (PersistenceManagerException e) {
 
             }
             return;
         }
 
         PortletGroup group = aclService.getGroupByName(groupHF.getValue());
         if (!aclService.hasSuperRole(user) && !aclService.hasAdminRoleInGroup(user, group)) {
             return;
         }

         String groupFile = PortletTabRegistry.getTabDescriptorPath(group.getName());
         System.err.println("saving group layout: " + group.getName());
         Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(groupFile), "UTF-8"));
        out.write(newText);
         out.close();
 
         PortletTabRegistry.reloadTab(groupHF.getValue(), groupFile);
         //PortletTabRegistry.addApplicationTab(group.getName(), groupFile);
         //userStorage.storeFile(user, tmpFile, fname);
         //tmpFile.delete();
 
     }
 
     public void editGuestLayout(FormEvent event)  throws PortletException, IOException {
         PortletRequest req = event.getPortletRequest();
         req.setAttribute("name", "GuestUserLayout.xml");
         req.setAttribute("allowImport", Boolean.FALSE);
 
         TextAreaBean ta = event.getTextAreaBean("layoutFile");
         HiddenFieldBean hf = event.getHiddenFieldBean("layoutHF");
         hf.setName("group");
         hf.setValue("guest");
         String guestLayoutPath = PortletTabRegistry.getGuestLayoutFile();
         BufferedReader reader = new BufferedReader(
                 new InputStreamReader(new FileInputStream(guestLayoutPath), "UTF-8"));
 
         String line = null;
         StringBuffer sb = new StringBuffer();
         while ((line = reader.readLine()) != null) {
             sb.append(line);
             sb.append("\n");
         }
         ta.setValue(sb.toString());
 
         setNextState(req, EDIT_JSP);
     }
 
 
     public void deleteGroupLayout(FormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getPortletRequest();
 
         String group = event.getAction().getParameter("group");
 
         PortletTabRegistry.removeGroupTab(group);
         PortletTabRegistry.removeApplicationTab(group);
         setNextState(req, VIEW_JSP);
     }
 }
