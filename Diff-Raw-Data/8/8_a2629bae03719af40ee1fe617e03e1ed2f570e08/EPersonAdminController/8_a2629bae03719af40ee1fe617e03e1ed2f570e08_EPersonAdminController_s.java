 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.dspace.webmvc.controller;
 
 import org.springframework.stereotype.Controller;
 import org.apache.log4j.Logger;
 import org.springframework.web.bind.annotation.RequestMapping;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.dspace.webmvc.bind.annotation.RequestAttribute;
 import org.dspace.core.Context;
 import org.springframework.ui.ModelMap;
 import org.dspace.authorize.AuthorizeException;
 import javax.servlet.ServletException;
 import java.sql.SQLException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import org.dspace.eperson.EPerson;
 import org.dspace.app.util.Util;
 import org.dspace.core.ConfigurationManager;
 import org.dspace.core.I18nUtil;
 import org.dspace.core.Utils;
 import org.dspace.eperson.Group;
 import org.dspace.eperson.EPersonDeletionException;
 
 /**
  *
  * @author Robert Qin
  */
 @Controller
 public class EPersonAdminController {
 
     /** Logger */
     private static Logger log = Logger.getLogger(EPersonAdminController.class);
     private Locale[] supportedLocales = I18nUtil.getSupportedLocales();
     private boolean ldap_enabled = ConfigurationManager.getBooleanProperty("ldap.enable");
     
 
     //Display Admin Tools
     @RequestMapping
     protected String showAdminView() {
 
         return "pages/admin/navbar";
 
     }//end showAdminView
 
     @RequestMapping("/admin/eperson/edit-epeople")
     protected String displayEPersonView() {
 
         return "pages/admin/eperson-main";
     }
 
     @RequestMapping("/admin/eperson/browse-epeople")
     protected String listEPeople(@RequestAttribute Context context, ModelMap model, HttpServletRequest request,
             HttpServletResponse response) throws ServletException, IOException,
             SQLException, AuthorizeException {
         // Are we for selecting a single or multiple epeople?
         boolean multiple = Util.getBoolParameter(request, "multiple");
 
         // What are we sorting by. Lastname is default
         int sortBy = EPerson.LASTNAME;
         String clearList;
         String closeWindow;
         int PAGESIZE = 50;
         int last;
 
 
         String sbParam = request.getParameter("sortby");
 
         if ((sbParam != null) && sbParam.equals("lastname")) {
             sortBy = EPerson.LASTNAME;
         } else if ((sbParam != null) && sbParam.equals("email")) {
             sortBy = EPerson.EMAIL;
         } else if ((sbParam != null) && sbParam.equals("id")) {
             sortBy = EPerson.ID;
         } else if ((sbParam != null) && sbParam.equals("language")) {
             sortBy = EPerson.LANGUAGE;
         }
 
         // What's the index of the first eperson to show? Default is 0 getINPara will return -1 if error in param
         int first = Util.getIntParameter(request, "first");
         int offset = Util.getIntParameter(request, "offset");
         
         if (first == -1) {
             first = 0;
         }
         if (offset == -1) {
             offset = 0;
         }
 
 
         EPerson[] epeople;
         String search = request.getParameter("search");
         if (search != null && !search.equals("")) {
             //i = offset;
             last = offset + PAGESIZE;
             epeople = EPerson.search(context, search);
             model.addAttribute("offset", Integer.valueOf(offset));
 
         } else {
             // Retrieve the e-people in the specified order
            
             search = "";
             last = first + PAGESIZE;
             epeople = EPerson.findAll(context, sortBy);
             model.addAttribute("offset", Integer.valueOf(0));
 
         }
 
         if (last >= epeople.length) {
             last = epeople.length - 1;
         }
 
         // Index of first eperson on last page
         int jumpEnd = ((epeople.length - 1) / PAGESIZE) * PAGESIZE;
 
         // Now work out values for next/prev page buttons
         int jumpFiveBack;
         if (search != null && !search.equals("")) {
             jumpFiveBack = offset - PAGESIZE * 5;
         } else {
             jumpFiveBack = first - PAGESIZE * 5;
         }
         if (jumpFiveBack < 0) {
             jumpFiveBack = 0;
         }
 
         int jumpOneBack;
         if (search != null && !search.equals("")) {
             jumpOneBack = offset - PAGESIZE;
         } else {
             jumpOneBack = first - PAGESIZE;
         }
         if (jumpOneBack < 0) {
             jumpOneBack = 0;
         }
 
         int jumpOneForward;
         if (search != null && !search.equals("")) {
             jumpOneForward = offset + PAGESIZE;
         } else {
             jumpOneForward = first + PAGESIZE;
         }
         if (jumpOneForward > epeople.length) {
             jumpOneForward = jumpEnd;
         }
 
         int jumpFiveForward;
         if (search != null && !search.trim().equals("")) {
             jumpFiveForward = offset + PAGESIZE * 5;
         } else {
             jumpFiveForward = first + PAGESIZE * 5;
         }
         if (jumpFiveForward > epeople.length) {
             jumpFiveForward = jumpEnd;
         }
 
         // What's the link?
         String sortByParam = "lastname";
         if (sortBy == EPerson.EMAIL) {
             sortByParam = "email";
         }
         if (sortBy == EPerson.ID) {
             sortByParam = "id";
         }
         if (sortBy == EPerson.LANGUAGE) {
             sortByParam = "language";
         }
 
         String jumpLink;
         if (search != null && !search.equals("")) {
            jumpLink = request.getContextPath() + "/administer/browse-epeople?multiple=" + multiple + "&sortby=" + sortByParam + "&first=" + first + "&search=" + search + "&offset=";
         } else {
            jumpLink = request.getContextPath() + "/administer/browse-epeople?multiple=" + multiple + "&sortby=" + sortByParam + "&first=";
         }
        String sortLink = request.getContextPath() + "/administer/browse-epeople?multiple=" + multiple + "&first=" + first + "&sortby=";
 
         model.addAttribute("sortby", Integer.valueOf(sortBy));
         model.addAttribute("first", Integer.valueOf(first));
         model.addAttribute("epeople", epeople);
         model.addAttribute("epeoplelength", epeople.length);
         model.addAttribute("search", search);
         model.addAttribute("sortLink", sortLink);
         model.addAttribute("jumpLink", jumpLink);
         model.addAttribute("jumpOneForward", jumpOneForward);
         model.addAttribute("jumpFiveForward", jumpFiveForward);
         model.addAttribute("jumpOneBack", jumpOneBack);
         model.addAttribute("jumpFiveBack", jumpFiveBack);
         model.addAttribute("jumpEnd", jumpEnd);
         model.addAttribute("last", last);
         
         List<String> nameList = new ArrayList<String>();
         List<String> emailList = new ArrayList<String>();
         List<EPerson> epersonList = new ArrayList<EPerson>();
 
         for (int i = (search != null && !search.equals("")) ? offset : first; i <= last; i++) {
 
             EPerson e = epeople[i];
             String fullname = e.getFullName().replace('\'', ' ');
             fullname = Utils.addEntities(fullname);
             String email = e.getEmail().replaceAll("'", "\\\\'");
             nameList.add(fullname);
             emailList.add(email);
             epersonList.add(e);
 
         }//end for
 
         model.addAttribute("nameList", nameList);
         model.addAttribute("emailList", emailList);
         model.addAttribute("epersonList", epersonList);
 
         if (multiple) {
             
             clearList = "";
             closeWindow = "";
             model.addAttribute("multiple", Boolean.TRUE);
 
         } else {
 
             clearList = "clearEPeople();";
             closeWindow = "window.close();";
 
         }//end else
         model.addAttribute("clearList", clearList);
         model.addAttribute("closeWindow", closeWindow);
 
 
         return "pages/admin/eperson-list";
         
     }//end doListEPeople
  
     @RequestMapping(params = "submit_add")
     protected String submitAddEPersonRequest(Context context, HttpServletRequest request,
             HttpServletResponse response, ModelMap model) throws ServletException, IOException,
             SQLException, AuthorizeException {
 
         EPerson e = EPerson.create(context);
 
         // create clever name and do update before continuing
         e.setEmail("newuser" + e.getID());
         e.update();
 
         model.addAttribute("eperson", e);        
         model.addAttribute("supportedLocales", supportedLocales);
         Locale valueOne = I18nUtil.getSupportedLocale(request.getLocale());       
         model.addAttribute("valueOne", valueOne);
         String localeCompare = I18nUtil.getSupportedLocale(request.getLocale()).getLanguage();
         model.addAttribute("localeCompare", localeCompare);       
         model.addAttribute("ldap_enabled", ldap_enabled);
         context.complete();
 
         return "pages/admin/eperson-edit";
 
     }//end submitAddEPersonRequest
 
     @RequestMapping(params = "submit_edit")
     protected String submitEditEPersonRequest(Context context, HttpServletRequest request,
             HttpServletResponse response, ModelMap model) throws ServletException, IOException,
             SQLException, AuthorizeException {
 
 
         // edit an eperson
         EPerson eperson = EPerson.find(context, Util.getIntParameter(request,
                 "eperson_id"));
 
         // Check the EPerson exists
         if (eperson == null) {
             model.addAttribute("no_eperson_selected", Boolean.TRUE);
             return "pages/admin/eperson-main";
         } else {
             // what groups is this person a member of?
             Group[] groupMemberships = Group.allMemberGroups(context, eperson);
 
             model.addAttribute("eperson", eperson);
             model.addAttribute("groupMemberships", groupMemberships);
 
             //setfields
             String language = eperson.getMetadata("language");
             String email = eperson.getEmail();
             String phone = eperson.getMetadata("phone");
             phone = Utils.addEntities(phone);
             String netid = eperson.getNetid();
             netid = Utils.addEntities(netid);
             String firstName = eperson.getFirstName();
             firstName = Utils.addEntities(firstName);
             String lastName = eperson.getLastName();
             lastName = Utils.addEntities(lastName);
 
             model.addAttribute("email", email);
             model.addAttribute("language", language);
             model.addAttribute("firstName", firstName);
             model.addAttribute("lastName", lastName);
             model.addAttribute("phone", phone);
             model.addAttribute("netid", netid);
             model.addAttribute("ldap_enabled", ldap_enabled);
             model.addAttribute("supportedLocales", supportedLocales);
 
             Locale valueOne = I18nUtil.getSupportedLocale(request.getLocale());
             model.addAttribute("valueOne", valueOne);
             //end setfields
 
             String localeCompare = I18nUtil.getSupportedLocale(request.getLocale()).getLanguage();
             model.addAttribute("localeCompare", localeCompare);
 
             context.complete();
 
             return "pages/admin/eperson-edit";
         }
     }//end submitEditEPersonRequest
 
     @RequestMapping(params = "submit_save")
     protected String submitSaveEPersonRequest(Context context, HttpServletRequest request,
             HttpServletResponse response, ModelMap model) throws ServletException, IOException,
             SQLException, AuthorizeException {
 
 
         // Update the metadata for an e-person
         EPerson eperson = EPerson.find(context, Util.getIntParameter(request, "eperson_id"));
 
         // see if the user changed the email - if so, make sure
         // the new email is unique
         String oldEmail = eperson.getEmail();
         String newEmail = request.getParameter("email").trim();
         String netid = request.getParameter("netid");
 
         if (!newEmail.equals(oldEmail)) {
             // change to email, now see if it's unique
             if (EPerson.findByEmail(context, newEmail) == null) {
                 // it's unique - proceed!
                 eperson.setEmail(newEmail);
 
                 eperson.setFirstName(request.getParameter("firstname").equals("") ? null : request.getParameter("firstname"));
 
                 eperson.setLastName(request.getParameter("lastname").equals("") ? null : request.getParameter("lastname"));
 
                 if (netid != null) {
                     eperson.setNetid(netid.equals("") ? null : netid.toLowerCase());
                 } else {
                     eperson.setNetid(null);
                 }
 
                 // FIXME: More data-driven?
                 eperson.setMetadata("phone", request.getParameter("phone").equals("") ? null : request.getParameter("phone"));
 
                 eperson.setMetadata("language", request.getParameter("language").equals("") ? null : request.getParameter("language"));
 
                 eperson.setCanLogIn((request.getParameter("can_log_in") != null) && request.getParameter("can_log_in").equals("true"));
 
                 eperson.setRequireCertificate((request.getParameter("require_certificate") != null)
                         && request.getParameter("require_certificate").equals("true"));
 
                 eperson.update();
                 
                 context.complete();
 
                 return "pages/admin/eperson-main";
 
             } else {
                 // not unique - send error message & let try again
                 model.addAttribute("eperson", eperson);
                 model.addAttribute("email_exists", Boolean.TRUE);
                 //setfields
                 String language = eperson.getMetadata("language");
                 String email = eperson.getEmail();
                 String phone = eperson.getMetadata("phone");
                 phone = Utils.addEntities(phone);                
                 netid = Utils.addEntities(netid);
                 String firstName = eperson.getFirstName();
                 firstName = Utils.addEntities(firstName);
                 String lastName = eperson.getLastName();
                 lastName = Utils.addEntities(lastName);
 
                 model.addAttribute("email", email);
                 model.addAttribute("language", language);
                 model.addAttribute("firstName", firstName);
                 model.addAttribute("lastName", lastName);
                 model.addAttribute("phone", phone);
                 model.addAttribute("netid", netid);
                 model.addAttribute("ldap_enabled", ldap_enabled);               
                 model.addAttribute("supportedLocales", supportedLocales);
 
                 String localeCompare = I18nUtil.getSupportedLocale(request.getLocale()).getLanguage();
                 model.addAttribute("localeCompare", localeCompare);
 
                 Locale valueOne = I18nUtil.getSupportedLocale(request.getLocale());
                 model.addAttribute("valueOne", valueOne);
                 //end setfields
                 context.complete();
 
                 return "pages/admin/eperson-edit";
             }
         } else {
             // no change to email
             if (netid != null) {
                 eperson.setNetid(netid.equals("") ? null : netid.toLowerCase());
             } else {
                 eperson.setNetid(null);
             }
 
             eperson.setFirstName(request.getParameter("firstname").equals(
                     "") ? null : request.getParameter("firstname"));
 
             eperson.setLastName(request.getParameter("lastname").equals("") ? null : request.getParameter("lastname"));
 
             // FIXME: More data-driven?
             eperson.setMetadata("phone",
                     request.getParameter("phone").equals("") ? null
                     : request.getParameter("phone"));
 
             eperson.setMetadata("language", request.getParameter("language").equals("") ? null : request.getParameter("language"));
 
             eperson.setCanLogIn((request.getParameter("can_log_in") != null)
                     && request.getParameter("can_log_in").equals("true"));
 
             eperson.setRequireCertificate((request.getParameter("require_certificate") != null)
                     && request.getParameter("require_certificate").equals(
                     "true"));
 
             eperson.update();            
             context.complete();
 
             return "pages/admin/eperson-main";
         }
 
     }//end submitSaveEPersonRequest
 
     @RequestMapping(params = "submit_delete")
     protected String submitDeleteEPersonRequest(Context context, HttpServletRequest request,
             HttpServletResponse response, ModelMap model) throws ServletException, IOException,
             SQLException, AuthorizeException {
 
         // Start delete process - go through verification step
 
         EPerson e = EPerson.find(context, Util.getIntParameter(request,
                 "eperson_id"));
 
         // Check the EPerson exists
         if (e == null) {
 
             model.addAttribute("no_eperson_selected", Boolean.TRUE);
             
             return "pages/admin/eperson-main";
 
         } else {
 
             model.addAttribute("eperson", e);
             return "pages/admin/eperson-confirm-delete";
         }
 
     }//end submitEPersonRequest
 
     @RequestMapping(params = "submit_confirm_delete")
     protected String submitConfirmDeleteEPersonRequest(Context context, HttpServletRequest request,
             HttpServletResponse response, ModelMap model) throws ServletException, IOException,
             SQLException, AuthorizeException {
 
         // User confirms deletion of type
         EPerson e = EPerson.find(context, Util.getIntParameter(request, "eperson_id"));
         try {
             e.delete();
         } catch (EPersonDeletionException ex) {
             model.addAttribute("eperson", e);
             model.addAttribute("tableList", ex.getTables());
             return "pages/admin/eperson-deletion-error";
         }
         context.complete();
         return "pages/admin/eperson-main";
     }//end submitConfirmDeleteEPersonRequest
 }//end EPersonAdminController
