 /*
 * Copyright 2012, CMM, University of Queensland.
 *
 * This file is part of Paul.
 *
 * Paul is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paul is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paul. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.edu.uq.cmm.paul.servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.catalina.realm.GenericPrincipal;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.joda.time.Hours;
 import org.joda.time.Minutes;
 import org.joda.time.Months;
 import org.joda.time.Weeks;
 import org.joda.time.Years;
 import org.joda.time.base.BaseSingleFieldPeriod;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.ServletContextAware;
 
 import au.edu.uq.cmm.aclslib.config.ConfigurationException;
 import au.edu.uq.cmm.aclslib.config.FacilityConfig;
 import au.edu.uq.cmm.aclslib.proxy.AclsAuthenticationException;
 import au.edu.uq.cmm.aclslib.proxy.AclsInUseException;
 import au.edu.uq.cmm.aclslib.service.Service.State;
 import au.edu.uq.cmm.eccles.FacilitySession;
 import au.edu.uq.cmm.eccles.UserDetailsManager;
 import au.edu.uq.cmm.eccles.UserDetails;
 import au.edu.uq.cmm.eccles.UserDetailsException;
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.PaulConfiguration;
 import au.edu.uq.cmm.paul.grabber.Analyser;
 import au.edu.uq.cmm.paul.grabber.DatafileMetadata;
 import au.edu.uq.cmm.paul.grabber.DatasetGrabber;
 import au.edu.uq.cmm.paul.grabber.DatasetMetadata;
 import au.edu.uq.cmm.paul.queue.AtomFeed;
 import au.edu.uq.cmm.paul.queue.QueueFileException;
 import au.edu.uq.cmm.paul.queue.QueueManager;
 import au.edu.uq.cmm.paul.queue.QueueManager.DateRange;
 import au.edu.uq.cmm.paul.queue.QueueManager.Removal;
 import au.edu.uq.cmm.paul.queue.QueueManager.Slice;
 import au.edu.uq.cmm.paul.status.Facility;
 import au.edu.uq.cmm.paul.status.FacilityStatus;
 import au.edu.uq.cmm.paul.status.FacilityStatusManager;
 import au.edu.uq.cmm.paul.watcher.FileWatcher;
 
 /**
  * The MVC controller for Paul's web UI.  This supports the status and configuration
  * pages and also implements GET access to the files in the queue area.
  * 
  * @author scrawley
  */
 @Controller
 public class WebUIController implements ServletContextAware {
     private static final Logger LOG = 
             LoggerFactory.getLogger(WebUIController.class);
 
     private static DateTimeFormatter[] FORMATS = new DateTimeFormatter[] {
         ISODateTimeFormat.dateHourMinuteSecond(),
         ISODateTimeFormat.localTimeParser(),
         ISODateTimeFormat.localDateOptionalTimeParser(),
         ISODateTimeFormat.dateTimeParser()
     };
     
     @Autowired(required=true)
     Paul services;
 
     
     @Override
     public void setServletContext(ServletContext servletContext) {
         LOG.debug("Setting the timezone (" + TimeZone.getDefault().getID() + 
                 ") in the servlet context");
         servletContext.setAttribute("javax.servlet.jsp.jstl.fmt.timeZone", 
                 TimeZone.getDefault());
     }
 
     @RequestMapping(value="/control", method=RequestMethod.GET)
     public String control(Model model) {
         addStateAndStatus(model);
         return "control";
     }
 
     @RequestMapping(value="/control", method=RequestMethod.POST)
     public String controlAction(Model model, HttpServletRequest request) {
         processStatusChange("watcher", request.getParameter("watcher"));
         processStatusChange("atomFeed", request.getParameter("atomFeed"));
         addStateAndStatus(model);
         return "control";
     }
     
     private void processStatusChange(String serviceName, String param) {
         if (param != null) {
             services.processStatusChange(serviceName, param);
         }
     }
     
     private void addStateAndStatus(Model model) {
         State ws = getFileWatcher().getState();
         model.addAttribute("watcherState", ws);
         model.addAttribute("watcherStatus", Status.forState(ws));
         State as = getAtomFeed().getState();
         model.addAttribute("atomFeedState", as);
         model.addAttribute("atomFeedStatus", Status.forState(as));
         model.addAttribute("resetRequired", getLatestConfig() != getConfig());
     }
     
     @RequestMapping(value="/sessions", method=RequestMethod.GET)
     public String status(Model model) {
         model.addAttribute("sessions", getFacilityStatusManager().getLatestSessions());
         return "sessions";
     }
     
     @RequestMapping(value="/sessions", method=RequestMethod.POST,
             params={"endSession"})
     public String endSession(Model model, 
             @RequestParam String sessionUuid, 
             HttpServletResponse response, HttpServletRequest request) 
     throws IOException, AclsAuthenticationException {
         getFacilityStatusManager().logoutSession(sessionUuid);
         response.sendRedirect(response.encodeRedirectURL(
                 request.getContextPath() + "/sessions"));
         return null;
     }
     
     @RequestMapping(value="/facilities", method=RequestMethod.GET)
     public String facilities(Model model) {
         Collection<FacilityConfig> facilities = getFacilities();
         for (FacilityConfig fc : facilities) {
             getFacilityStatusManager().attachStatus((Facility) fc);
         }
         model.addAttribute("facilities", facilities);
         return "facilities";
     }
     
     @RequestMapping(value="/facilities", method=RequestMethod.GET,
             params="newForm")
     public String newFacilityForm(Model model, HttpServletRequest request) {
         model.addAttribute("facility", new Facility());  // (just for the defaults ...)
         model.addAttribute("edit", true);
         model.addAttribute("create", true);
         model.addAttribute("message", 
                 "Please fill in the form and click 'Save New Facility'");
         model.addAttribute("returnTo", inferReturnTo(request, "/facilities"));
         return "facility";
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.GET)
     public String facilityConfig(@PathVariable String facilityName, 
             Model model, HttpServletRequest request,
             @RequestParam(required=false) String edit) 
             throws ConfigurationException {
         Facility facility = lookupFacilityByName(facilityName);
         model.addAttribute("facility", facility);
         if (edit != null) {
             model.addAttribute("edit", true);
             model.addAttribute("message", 
                     "Please fill in the form and click 'Save Facility Changes'");
         }
         return "facility";
     }
     
     @RequestMapping(value="/facilities", method=RequestMethod.POST,
             params={"create"})
     public String createFacilityConfig(
             Model model, HttpServletRequest request) 
             throws ConfigurationException {
         ValidationResult<Facility> res = getConfigManager().
                 createFacility(request.getParameterMap());
         model.addAttribute("returnTo", inferReturnTo(request, "/facilities"));
         if (!res.isValid()) {
             model.addAttribute("edit", true);
             model.addAttribute("create", true);
             model.addAttribute("facility", res.getTarget());
             model.addAttribute("diags", res.getDiags());
             model.addAttribute("message", "Please correct the errors and try again");
             return "facility";
         } else {
             model.addAttribute("message", "Facility configuration created");
             return "ok";
         }
     }
     
     @RequestMapping(value = "/facilities/{facilityName:.+}", method = RequestMethod.POST, 
             params = {"copy"})
     public String copyFacilityConfig(@PathVariable String facilityName,
             Model model, HttpServletRequest request) 
             throws ConfigurationException {
         Facility facility = lookupFacilityByName(facilityName);
         facility.setFacilityName(null);
         facility.setId(null);
         model.addAttribute("edit", true);
         model.addAttribute("create", true);
         model.addAttribute("facility", facility);
         model.addAttribute("diags", Collections.emptyMap());
         model.addAttribute("message", "Fill in the new facility name, "
                 + "edit the other details and click 'Save New Facility'");
         model.addAttribute("returnTo", inferReturnTo(request, "/facilities"));
         return "facility";
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.POST,
             params={"update"})
     public String updateFacilityConfig(
             @PathVariable String facilityName, Model model,
             HttpServletRequest request) 
             throws ConfigurationException {
         ValidationResult<Facility> res = getConfigManager().
                 updateFacility(facilityName, request.getParameterMap());
         model.addAttribute("returnTo", inferReturnTo(request, "/facilities"));
         if (!res.isValid()) {
             model.addAttribute("edit", true);
             model.addAttribute("facility", res.getTarget());
             model.addAttribute("diags", res.getDiags());
             model.addAttribute("message", "Please correct the errors and try again");
             return "facility";
         } else {
             model.addAttribute("message", "Facility configuration updated");
             return "ok";
         }
     }
     
     @RequestMapping(value = "/facilities/{facilityName:.+}", method = RequestMethod.POST, 
             params = {"delete"})
     public String deleteFacilityConfig(@PathVariable String facilityName,
             Model model, HttpServletRequest request,
             @RequestParam(required = false) String confirmed) {
         model.addAttribute("returnTo", inferReturnTo(request, "/facilities"));
         model.addAttribute("facilityName", facilityName);
         if (confirmed == null) {
             return "facilityDeleteConfirmation";
         } 
         Facility facility = lookupFacilityByName(facilityName);
         if (facility == null) {
             model.addAttribute("message", 
                     "Can't find facility configuration for '" + facilityName + "'");
             return "failed";
         }
         getFileWatcher().stopFileWatching(facility);
         getConfigManager().deleteFacility(facilityName);
         model.addAttribute("message", "Facility configuration deleted");
         return "ok";
     }
     
     @RequestMapping(value="/sessions/{facilityName:.+}")
     public String facilitySessions(@PathVariable String facilityName, Model model) 
             throws ConfigurationException {
         model.addAttribute("sessions", 
                 getFacilityStatusManager().sessionsForFacility(facilityName));
         model.addAttribute("facilityName", facilityName);
         return "facilitySessions";
     }
     
     @RequestMapping(value="/queueDiagnostics/{facilityName:.+}",
             method=RequestMethod.GET)
     public String queueDiagnostics(@PathVariable String facilityName, Model model,
             @RequestParam(required=false) String hwmTimestamp, 
             @RequestParam(required=false) String lwmTimestamp, 
             @RequestParam(required=false) String checkHashes) 
             throws ConfigurationException {
         return doCollectDiagnostics(facilityName, model, hwmTimestamp,
                 lwmTimestamp, toBoolean(checkHashes));
     }
 
     private String doCollectDiagnostics(String facilityName, Model model,
             String hwmTimestamp, String lwmTimestamp, boolean checkHashes) {
         Facility facility = lookupFacilityByName(facilityName);
         FacilityStatus status = getFacilityStatusManager().getStatus(facility);
         DateRange range = getQueueManager().getQueueDateRange(facility);
         model.addAttribute("facilityName", facilityName);
         model.addAttribute("status", status);
         Date hwm = status.getGrabberHWMTimestamp();
         if (!tidy(hwmTimestamp).isEmpty()) {
             DateTime tmp = parseTimestamp(hwmTimestamp);
             if (tmp != null) {
                 hwm = tmp.toDate();
             }
         }
         Date lwm = status.getGrabberLWMTimestamp();
         if (!tidy(lwmTimestamp).isEmpty()) {
             DateTime tmp = parseTimestamp(lwmTimestamp);
             if (tmp != null) {
                 lwm = tmp.toDate();
             }
         }
         model.addAttribute("lwmTimestamp", lwm);
         model.addAttribute("hwmTimestamp", hwm);
         model.addAttribute("intertidal", true);
         model.addAttribute("checkHashes", checkHashes);
         model.addAttribute("analysis", 
                 new Analyser(services, facility).analyse(lwm, hwm, range, checkHashes));
         return "catchupControl";
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", params={"reanalyse"},
             method=RequestMethod.POST)
     public String reanalyse(@PathVariable String facilityName, Model model,
             @RequestParam(required=false) String lwmTimestamp,
             @RequestParam(required=false) String hwmTimestamp,
             @RequestParam(required=false) String checkHashes) 
             throws ConfigurationException {
         return doCollectDiagnostics(facilityName, model, hwmTimestamp, lwmTimestamp, 
                 toBoolean(checkHashes));
     }
     
     private boolean toBoolean(String option) {
         return (option != null && option.equals("true"));
     }
 
     @RequestMapping(value="/facilities/{facilityName:.+}", params={"setIntertidal"},
             method=RequestMethod.POST)
     public String setFacilityHWM(@PathVariable String facilityName, Model model,
             HttpServletRequest request, 
             @RequestParam String lwmTimestamp, 
             @RequestParam String hwmTimestamp) 
             throws ConfigurationException {
         model.addAttribute("returnTo", inferReturnTo(request, "/facilities"));
         Facility facility = lookupFacilityByName(facilityName);
         FacilityStatus status = getFacilityStatusManager().getStatus(facility);
         if (status.getStatus() == FacilityStatusManager.Status.ON) {
             model.addAttribute("message", "Cannot change LWM and HWM while the Grabber is running.");
             return "failed";
         }
         Date oldLWM = status.getGrabberLWMTimestamp();
         Date oldHWM = status.getGrabberHWMTimestamp();
         Date lwm = parseDate(lwmTimestamp);
         Date hwm = parseDate(hwmTimestamp);
         Date now = new Date();
         if (lwm != null && hwm != null && !lwm.after(hwm) && hwm.before(now)) {
             getFacilityStatusManager().updateIntertidalTimestamp(facility, lwm, hwm);
             model.addAttribute("message", "Changed LWM / HWM for '" + facilityName + "' from " +
                     oldLWM + " / " + oldHWM + " to " + lwm + " / " + hwm);
             return "ok";
         } else {
             if (lwm == null) {
                 model.addAttribute("message", "Invalid LWM timestamp");
             } else if (hwm == null) {
                 model.addAttribute("message", "Invalid HWM timestamp");
             } else if (lwm.after(hwm)) {
                 model.addAttribute("message", "Inconsistent timestamps: LWM &gt; HWM");
             } else if (!hwm.before(now)) {
                 model.addAttribute("message", "Inconsistent timestamps: HWM in the future");
             }
             return "failed";
         }
     }
     
     private Date parseDate(String str) {
         str = tidy(str);
         if (!str.isEmpty()) {
             DateTime tmp = parseTimestamp(str);
             if (tmp != null) {
                 return tmp.toDate();
             }
         }
         return null;
     }
 
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.POST, 
             params={"start"})
     public String startWatcher(@PathVariable String facilityName, Model model,
             HttpServletRequest request, HttpServletResponse response) 
             throws IOException {
         Facility facility = lookupFacilityByName(facilityName);
         if (facility != null) {
             getFileWatcher().startFileWatching(facility);
         }
         response.sendRedirect(inferReturnTo(request, "/facilities"));
         return null;
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.POST, 
             params={"stop"})
     public String stopWatcher(@PathVariable String facilityName, Model model,
             HttpServletRequest request, HttpServletResponse response) 
             throws IOException {
         Facility facility = lookupFacilityByName(facilityName);
         if (facility != null) {
             getFileWatcher().stopFileWatching(facility);
         }
         response.sendRedirect(inferReturnTo(request, "/facilities"));
         return null;
     }
     
     @RequestMapping(value="/mirage", method=RequestMethod.GET)
     public String mirage(Model model, HttpServletResponse response) 
             throws IOException {
         response.sendRedirect(getConfig().getPrimaryRepositoryUrl());
         return null;
     }
     
     @RequestMapping(value="/acls", method=RequestMethod.GET)
     public String acls(Model model, HttpServletResponse response) 
             throws IOException {
         response.sendRedirect(getConfig().getAclsUrl());
         return null;
     }
     
     @RequestMapping(value="/facilitySelect", method=RequestMethod.GET)
     public String facilitySelector(Model model,
             HttpServletRequest request, 
             @RequestParam String next,
             @RequestParam(required=false) String slice) {
         model.addAttribute("next", next);
         model.addAttribute("slice", inferSlice(slice));
         model.addAttribute("returnTo", inferReturnTo(request));
         model.addAttribute("facilities", getFacilities());
         return "facilitySelect";
     }
     
     @RequestMapping(value="/facilitySelect", method=RequestMethod.POST)
     public String facilitySelect(Model model, 
             @RequestParam String next,
             @RequestParam(required=false) String slice,
             @RequestParam(required=false) String zz,
             HttpServletRequest request, HttpServletResponse response,
             @RequestParam(required=false) String facilityName) 
     throws UnsupportedEncodingException, IOException {
         if (facilityName == null) {
             model.addAttribute("slice", inferSlice(slice));
             model.addAttribute("returnTo", inferReturnTo(request));
             model.addAttribute("facilities", getFacilities());
             model.addAttribute("message", "Select a facility from the pulldown");
             model.addAttribute("next", next);
             return "facilitySelect";
         } else {
             response.sendRedirect(request.getContextPath() + 
                     "/" + next +
                     "?facilityName=" + URLEncoder.encode(facilityName, "UTF-8") + 
                     "&slice=" + slice +
                     "&returnTo=" + inferReturnTo(request));
             return null;
         }
     }
 
     @RequestMapping(value="/facilityLogout")
     public String facilityLogout(Model model, HttpServletRequest request,
             @RequestParam String facilityName) {
         model.addAttribute("returnTo", inferReturnTo(request));
         GenericPrincipal principal = (GenericPrincipal) request.getUserPrincipal();
         if (principal == null || !principal.hasRole("ROLE_ACLS_USER")) {
             model.addAttribute("message", "I don't know your ACLS userName");
             return "failed";
         }
         String userName = principal.getName();
         FacilityStatusManager fsm = getFacilityStatusManager();
         FacilitySession session = fsm.getSession(
         		lookupFacilityByName(facilityName), 
         		System.currentTimeMillis());
         if (session == null || !session.getUserName().equals(userName)) {
             model.addAttribute("message", "You are not logged in on '" + facilityName + "'");
             return "failed";
         }
         try {
             fsm.logoutSession(session.getSessionUuid());
             model.addAttribute("message", "Your session has been logged out");
             return "ok";
         } catch (AclsAuthenticationException ex) {
             LOG.error("Session logout failed", ex);
             model.addAttribute("message", "Session logout failed due to an internal error");
             return "failed";
         }
     }
     
     @RequestMapping(value="/facilityLogin")
     public String facilityLogin(@RequestParam String facilityName, 
             @RequestParam(required=false) String startSession,  
             @RequestParam(required=false) String endOldSession, 
             @RequestParam(required=false) String userName, 
             @RequestParam(required=false) String account,
             Model model, HttpServletResponse response, HttpServletRequest request) 
                     throws IOException {
         FacilityStatusManager fsm = getFacilityStatusManager();
         facilityName = tidy(facilityName);
         model.addAttribute("facilityName", facilityName);
         model.addAttribute("facilities", getFacilities());
         model.addAttribute("returnTo", inferReturnTo(request));
 
 
         userName = tidy(userName);
         String password = tidy(request.getParameter("password"));
         if (startSession == null) {
             GenericPrincipal principal = (GenericPrincipal) request.getUserPrincipal();
             if (principal != null && principal.hasRole("ROLE_ACLS_USER") && 
                     principal.getPassword() != null &&
                     !principal.getPassword().isEmpty()) {
                 userName = principal.getName();
                 password = principal.getPassword();
             }
         }
         model.addAttribute("userName", userName);
         model.addAttribute("password", password);
         if (userName.isEmpty() || password.isEmpty()) {
             // Phase 1 - user must fill in user name and password
             model.addAttribute("message", "Fill in the username and password fields");
             return "facilityLogin";
         }
         try {
             if (account == null) {
                 // Phase 2 - validate user credentials and get accounts list
                 List<String> accounts = null;
                 if (endOldSession != null) {
                     LOG.debug("Attempting old session logout");
                     fsm.logoutFacility(facilityName);
                     LOG.debug("Logout succeeded");
                 }
                 LOG.debug("Attempting login");
                 accounts = fsm.login(facilityName, userName, password);
                 LOG.debug("Login succeeded");
                 // If there is only one account, select immediately.
                 if (accounts != null) {
                     if (accounts.size() == 1) {
                         fsm.selectAccount(facilityName, userName, accounts.get(0));
                         LOG.debug("Account selection succeeded");;
                         return "facilityLoggedIn";
                     } else {
                         model.addAttribute("accounts", accounts);
                         model.addAttribute("message", 
                                 "Select an account to complete the login");
                     }
                 }
             } else {
                 // Phase 3 - after user has selected an account
                 fsm.selectAccount(facilityName, userName, account);
                 LOG.debug("Account selection succeeded");
                 return "facilityLoggedIn";
             }
         } catch (AclsAuthenticationException ex) {
             model.addAttribute("message", "Login failed: " + ex.getMessage());
         } catch (AclsInUseException ex) {
             model.addAttribute("message", 
                     "Instrument " + ex.getFacilityName() + 
                     " is currently logged in under the name of " + ex.getUserName());
             model.addAttribute("inUse", true);
         }
         return "facilityLogin";
     }
     
     @RequestMapping(value="/login", method=RequestMethod.GET)
     public String login(Model model) {
         return "login";
     }
     
     @RequestMapping(value="/loginFailed")
     public String loginFailed(Model model) {
         return "loginFailed";
     }
     
     @RequestMapping(value="/loggedIn", method=RequestMethod.GET)
     public String loggedIn(Model model) {
         return "loggedIn";
     }
     
     @RequestMapping(value="/logout", method=RequestMethod.GET)
     public String logout(Model model) {
         return "logout";
     }
     
     @RequestMapping(value="/admin", method=RequestMethod.GET)
     public String admin(Model model) {
         return "admin";
     }
     
     @RequestMapping(value="/noAccess", method=RequestMethod.GET)
     public String noAccess(Model model) {
         return "noAccess";
     }
     
     @RequestMapping(value="/unavailable", method=RequestMethod.GET)
     public String unavailable(Model model) {
         return "unavailable";
     }
     
     @RequestMapping(value="/config", method=RequestMethod.GET)
     public String config(Model model) {
         model.addAttribute("config", getLatestConfig());
         return "config";
     }
     
     @RequestMapping(value="/config", method=RequestMethod.POST, params={"reset"})
     public String configReset(Model model, HttpServletRequest request,
             @RequestParam(required=false) String confirmed) {
         model.addAttribute("returnTo", inferReturnTo(request, "config"));
         if (confirmed == null) {
             return "resetConfirmation";
         } else {
             getConfigManager().resetConfiguration();
             model.addAttribute("message", 
                     "Configuration reset succeeded.  " +
                     "Please restart the webapp to use the updated configs");
             return "ok";
         }
     }
     
     @RequestMapping(value="/queue/held", method=RequestMethod.GET)
     public String held(Model model) {
         model.addAttribute("queue", 
                 getQueueManager().getSnapshot(Slice.HELD));
         return "held";
     }
     
     @RequestMapping(value="/queue/ingestible", method=RequestMethod.GET)
     public String queue(Model model) {
         model.addAttribute("queue", 
                 getQueueManager().getSnapshot(Slice.INGESTIBLE));
         return "queue";
     }
     
     @RequestMapping(value="/claimDatasets", method=RequestMethod.GET)
     public String showClaimDatasets(Model model, 
             HttpServletRequest request, HttpServletResponse response,
             @RequestParam String facilityName) 
     throws IOException {
         model.addAttribute("facilityName", facilityName);
         model.addAttribute("returnTo", inferReturnTo(request));
         model.addAttribute("datasets", 
                 getQueueManager().getSnapshot(Slice.HELD, facilityName));
         return "claimDatasets";
     }
     
     @RequestMapping(value="/claimDatasets", 
             method=RequestMethod.POST, params={"claim"})
     public String claimDatasets(Model model,
             HttpServletRequest request, HttpServletResponse response,
             @RequestParam(required=false) String[] ids,
             @RequestParam String facilityName) 
     throws IOException, QueueFileException, InterruptedException {
         GenericPrincipal principal = (GenericPrincipal) request.getUserPrincipal();
         if (principal == null) {
             LOG.error("No principal ... can't proceed");
             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             return null;
         }
         model.addAttribute("returnTo", inferReturnTo(request));
         if (ids == null) {
             model.addAttribute("facilityName", facilityName);
             model.addAttribute("datasets", 
                     getQueueManager().getSnapshot(Slice.HELD, facilityName));
             model.addAttribute("message", "Check the checkboxes for the " +
                     "Datasets you want to claim");
             return "claimDatasets";
         }
         if (!principal.hasRole("ROLE_ACLS_USER")) {
                 model.addAttribute("message", "You must be logged in using " +
                         "ACLS credentials to claim files");
                 return "failed";
             }
         String userName = principal.getName();
         try {
             int nosChanged = getQueueManager().changeUser(ids, userName, false);
             model.addAttribute("message", 
                     verbiage(nosChanged, "dataset", "datasets", "claimed"));
             return "ok";
         } catch (NumberFormatException ex) {
             LOG.debug("Rejected request with bad entry id(s)");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
     }
     
     @RequestMapping(value="/manageDatasets", method=RequestMethod.GET)
     public String showManageDatasets(Model model, 
             HttpServletRequest request, HttpServletResponse response,
             @RequestParam(required=false) String slice,
             @RequestParam String facilityName) 
     throws IOException {
         model.addAttribute("facilityName", facilityName);
         model.addAttribute("returnTo", inferReturnTo(request));
         Slice s = inferSlice(slice);
         model.addAttribute("slice", s);
         model.addAttribute("datasets", 
                 getQueueManager().getSnapshot(s, facilityName));
         model.addAttribute("userNames", getUserDetailsManager().getUserNames());
         return "manageDatasets";
     }
     
     private Slice inferSlice(String sliceName) {
    	if (sliceName == null) {
            return null;
         } else {
             try {
                 return Slice.valueOf(sliceName.toUpperCase());
             } catch (IllegalArgumentException ex) {
                 LOG.debug("unrecognized slice - ignoring it");
                 return Slice.ALL;
             }
         }
     }
     
     @RequestMapping(value="/manageDatasets", method=RequestMethod.POST)
     public String manageDatasets(Model model,
             HttpServletRequest request, HttpServletResponse response,
             @RequestParam(required=false) String[] ids,
             @RequestParam(required=false) String userName,
             @RequestParam(required=false) String slice,
             @RequestParam(required=false) String confirmed,
             @RequestParam String action,
             @RequestParam(required=false) String facilityName) 
     throws IOException, QueueFileException, InterruptedException {
         GenericPrincipal principal = (GenericPrincipal) request.getUserPrincipal();
         if (principal == null) {
             LOG.error("No principal ... can't proceed");
             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             return null;
         }
         if (!principal.hasRole("ROLE_ADMIN")) {
             model.addAttribute("message", "Only an administrator can manage datasets");
             return "failed";
         }
         Slice s = inferSlice(slice);
         model.addAttribute("facilityName", facilityName);
         model.addAttribute("slice", s);
         model.addAttribute("returnTo", inferReturnTo(request));
         if (action.equals("deleteAll")) {
             return deleteAll(model, request, s, facilityName, true, confirmed);
         } else if (action.equals("archiveAll")) {
             return deleteAll(model, request, s, facilityName, false, confirmed);
         } else if (action.equals("expire")) {
             return expire(model, request, s, facilityName, confirmed);
         }
         QueueManager qm = getQueueManager();
         if (ids == null) {
             model.addAttribute("datasets", qm.getSnapshot(s, facilityName));
             model.addAttribute("userNames", getUserDetailsManager().getUserNames());
             model.addAttribute("message", 
                     "Check the checkboxes for the Datasets you want to manage");
             return "manageDatasets";
         } 
         try {
             int nosChanged;
             switch (action) {
             case "archive":
                 nosChanged = qm.delete(ids, Removal.ARCHIVE);
                 model.addAttribute("message", 
                         verbiage(nosChanged, "dataset", "datasets", "archived"));
                 return "ok";
             case "delete":
                 nosChanged = qm.delete(ids, Removal.DELETE);
                 model.addAttribute("message",
                         verbiage(nosChanged, "dataset", "datasets", "deleted"));
                 return "ok";
             case "assign":
                 nosChanged = qm.changeUser(ids, userName, true);
                 model.addAttribute("message", 
                         verbiage(nosChanged, "dataset", "datasets", "assigned"));
                 return "ok";
             default:
                 LOG.debug("Rejected request with unrecognized action");
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                 return null;
             }
         } catch (NumberFormatException ex) {
             LOG.debug("Rejected request with bad entry id(s)");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
     }
     
     private String verbiage(int count, String singular, String plural, String verbed) {
         if (count == 0) {
             return "No " + plural + " " + verbed;
         } else if (count == 1) {
             return "1 " + singular + " " + verbed;
         } else {
             return count + " " + plural + " " + verbed;
         }
     }
 
     private String deleteAll(Model model, HttpServletRequest request, 
             Slice slice, String facilityName, boolean discard, String confirmed) 
                     throws InterruptedException {
         if (confirmed == null) {
             model.addAttribute("discard", discard);
             return "queueDeleteConfirmation";
         }
         Removal removal = discard ? Removal.DELETE : Removal.ARCHIVE;
         int count = getQueueManager().deleteAll(removal, facilityName, slice);
         model.addAttribute("message", 
                 verbiage(count, "queue entry", "queue entries", 
                         discard ? "deleted" : "archived"));
         return "ok";
     }
     
     private String expire(Model model, HttpServletRequest request, 
             Slice slice, String facilityName, String confirmed) throws InterruptedException {
         String mode = request.getParameter("mode");
         String olderThan = request.getParameter("olderThan");
         String age = request.getParameter("age");
         Date cutoff = determineCutoff(model, tidy(olderThan), tidy(age));
         if (cutoff == null || confirmed == null) {
             return "queueExpiryForm";
         }
         Removal removal = mode.equals("discard") ? Removal.DELETE : Removal.ARCHIVE;
         int count = getQueueManager().expireAll(removal, facilityName, slice, cutoff);
 
         model.addAttribute("message", 
                 verbiage(count, "queue entry", "queue entries", "expired"));
         return "ok";
     }
 
     @RequestMapping(value="/datasets/{entry:.+}", 
             method=RequestMethod.GET)
     public String queueEntry(@PathVariable String entry, Model model, 
             HttpServletRequest request, HttpServletResponse response) 
             throws IOException {
         DatasetMetadata metadata = findDataset(entry, response);
         if (metadata != null) {
             model.addAttribute("entry", metadata);
             model.addAttribute("returnTo", inferReturnTo(request));
             return "dataset";
         } else {
             return null;
         }
     }
     
     @RequestMapping(value="/datasets/{entry:.+}", params={"regrab"},
             method=RequestMethod.POST)
     public String regrabPrepare(@PathVariable String entry, Model model, 
             HttpServletRequest request, HttpServletResponse response) 
             throws IOException {
         DatasetMetadata dataset = findDataset(entry, response);
         if (dataset != null) {
             DatasetMetadata grabbedMetadata = new DatasetGrabber(services, dataset).getCandidateDataset();
             model.addAttribute("returnTo", inferReturnTo(request));
             if (grabbedMetadata != null) {
                 grabbedMetadata.updateDatasetHash();
                 dataset.updateDatasetHash();
                 model.addAttribute("oldEntry", dataset);
                 model.addAttribute("newEntry", grabbedMetadata);
                 return "regrabConfirmation";
             } else {
                 model.addAttribute("message", "None of the original dataset files still exist");
                 return "failed";
             }
         } else {
             return null;
         }
     }
     
     @RequestMapping(value="/datasets/", params={"grab"},
             method=RequestMethod.POST)
     public String grab(Model model, 
             @RequestParam String pathnameBase,
             @RequestParam String facilityName,
             HttpServletRequest request, HttpServletResponse response) 
                     throws IOException, InterruptedException, QueueFileException {
         model.addAttribute("returnTo", inferReturnTo(request));
         Facility facility = lookupFacilityByName(facilityName);
         DatasetGrabber dsr = new DatasetGrabber(services, new File(pathnameBase), facility);
         DatasetMetadata grabbedDataset = dsr.grabDataset();
         if (grabbedDataset != null) {
             grabbedDataset.updateDatasetHash();
             model.addAttribute("message", "Dataset grab succeeded");
             return "ok";
         } else {
             model.addAttribute("message", "Dataset grab failed");
             return "failed";
         }
     }
 
     @RequestMapping(value="/datasets/{entry:.+}", params={"regrabNew"},
             method=RequestMethod.POST)
     public String regrab(@PathVariable String entry, Model model, 
             @RequestParam String hash,
             @RequestParam String regrabNew,
             HttpServletRequest request, HttpServletResponse response) 
             throws IOException, InterruptedException, QueueFileException {
         DatasetMetadata dataset = findDataset(entry, response);
         if (dataset != null) {
             model.addAttribute("returnTo", inferReturnTo(request));
             DatasetGrabber dsr = new DatasetGrabber(services, dataset);
             DatasetMetadata grabbedDataset = dsr.regrabDataset(regrabNew.equalsIgnoreCase("yes"));
             if (grabbedDataset == null) {
                 model.addAttribute("message", "Dataset regrab failed - see logs");
                 return "failed";
             }
             grabbedDataset.updateDatasetHash();
             if (!hash.equals(grabbedDataset.getCombinedDatafileHash())) {
                 LOG.debug("supplied hash is " + hash);
                 LOG.debug("actual hash is   " + grabbedDataset.getCombinedDatafileHash());
                 model.addAttribute("message", "Dataset files were apparently changed");
                 return "failed";
             } else {
                 dsr.commitRegrabbedDataset(dataset, grabbedDataset, regrabNew.equalsIgnoreCase("yes"));
                 model.addAttribute("message", "Dataset regrab succeeded");
                 return "ok";
             }
         } else {
             return null;
         }
     }
 
     @RequestMapping(value="/datasets/{entry:.+}", params={"delete"},
             method=RequestMethod.POST)
     public String delete(@PathVariable String entry, Model model, 
             HttpServletRequest request, HttpServletResponse response) 
             throws IOException, InterruptedException {
         return doDelete(entry, model, request, response, true);
     }
 
     @RequestMapping(value="/datasets/{entry:.+}", params={"archive"},
             method=RequestMethod.POST)
     public String archive(@PathVariable String entry, Model model, 
             HttpServletRequest request, HttpServletResponse response) 
             throws IOException, InterruptedException {
         return doDelete(entry, model, request, response, false);
     }
 
     private String doDelete(String entry, Model model,
             HttpServletRequest request, HttpServletResponse response, boolean discard)
             throws IOException, InterruptedException {
         DatasetMetadata dataset = findDataset(entry, response);
         if (dataset != null) {
             model.addAttribute("returnTo", inferReturnTo(request));
             QueueManager qm = getQueueManager();
             Removal removal = discard ? Removal.DELETE : Removal.ARCHIVE;
             int nosDeleted = qm.delete(new String[]{entry}, removal);
             if (nosDeleted == 0) {
                 model.addAttribute("message", "Could not find that dataset");
                 return "failed";
             } else {
                 model.addAttribute("message", "Dataset #" + entry + 
                         (discard ? " deleted" : " archived"));
                 return "ok";
             }
         } else {
             return null;
         }
     }
 
     private DatasetMetadata findDataset(String entry,
             HttpServletResponse response) throws IOException {
         long id;
         try {
             id = Long.parseLong(entry);
         } catch (NumberFormatException ex) {
             LOG.debug("Rejected request with bad entry id");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         DatasetMetadata dataset = getQueueManager().fetchDataset(id);
         if (dataset == null) {
             LOG.debug("Rejected request for unknown entry");
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             return null;
         }
         return dataset;
     }
     
     @RequestMapping(value="/files/{fileName:.+}", method=RequestMethod.GET)
     public String file(@PathVariable String fileName, Model model, 
             HttpServletResponse response) 
             throws IOException {
         LOG.debug("Request to fetch file " + fileName);
         // This aims to prevent requests from reading files outside of the queue directory.
         // FIXME - this assumes that the directory for the queue is flat ...
         if (fileName.contains("/") || fileName.equals("..")) {
             LOG.debug("Rejected request for security reasons");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         File file = new File(getConfig().getCaptureDirectory(), fileName);
         DatafileMetadata metadata = fetchMetadata(file);
         if (metadata == null) {
             LOG.debug("No metadata for file " + fileName);
         } else {
             LOG.debug("Found metadata for file " + fileName);
         }
         model.addAttribute("file", file);
         model.addAttribute("contentType", 
                 metadata == null ? "application/octet-stream" : metadata.getMimeType());
         return "fileView";
     }
     
     @RequestMapping(value="/users", method=RequestMethod.GET)
     public String users(Model model) {
         model.addAttribute("userNames", getUserDetailsManager().getUserNames());
         return "users";
     }
 
     @RequestMapping(value="/users", params={"add"},	 
             method=RequestMethod.POST)
     public String userAdd(
     		@RequestParam() String userName, 
             Model model) {
     	UserDetailsManager um = getUserDetailsManager();
     	try {
     		um.addUser(new UserDetails(userName));
     		model.addAttribute("message", "User '" + userName + "' added");
     	} catch (UserDetailsException ex) {
     		model.addAttribute("message", ex.getMessage());
     	}
         model.addAttribute("userNames", um.getUserNames());
         return "users";
     }
 
     @RequestMapping(value="/users", params={"remove"},	 
             method=RequestMethod.POST)
     public String userRemove(
     		@RequestParam() String userName, 
             Model model) {
     	UserDetailsManager um = getUserDetailsManager();
     	try {
     		um.removeUser(userName);
     		model.addAttribute("message", "User '" + userName + "' removed");
     	} catch (UserDetailsException ex) {
     		model.addAttribute("message", ex.getMessage());
     	}
         model.addAttribute("userNames", um.getUserNames());
         return "users";
     }
 
     @RequestMapping(value="/users/{userName:.+}", method=RequestMethod.GET)
     public String user(@PathVariable String userName, Model model,
             HttpServletResponse response) 
             throws IOException {
         try {
             UserDetails userDetails = getUserDetailsManager().lookupUser(userName, true);
             model.addAttribute("user", userDetails);
         } catch (UserDetailsException e) {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         return "user";
     }
     
     private String tidy(String str) {
         return str == null ? "" : str.trim();
     }
     
     private String inferReturnTo(HttpServletRequest request) {
         return inferReturnTo(request, "");
     }
     
     private String inferReturnTo(HttpServletRequest request, String dflt) {
         String param = request.getParameter("returnTo");
         if (param == null) {
             param = dflt;
         } else {
             param = param.trim();
         }
         if (param.startsWith(request.getContextPath())) {
             return param;
         } else if (param.startsWith("/")) {
             return request.getContextPath() + param;
         } else {
             return request.getContextPath() + "/" + param;
         }
     }
     
     private DatafileMetadata fetchMetadata(File file) {
         EntityManager entityManager = createEntityManager();
         try {
             TypedQuery<DatafileMetadata> query = entityManager.createQuery(
                     "from DatafileMetadata d where d.capturedFilePathname = :pathName", 
                     DatafileMetadata.class);
             query.setParameter("pathName", file.getAbsolutePath());
             return query.getSingleResult();
         } catch (NoResultException ex) {
             return null;
         } finally {
             entityManager.close();
         }
     }
     
     private FileWatcher getFileWatcher() {
         return services.getFileWatcher();
     }
     
     private FacilityStatusManager getFacilityStatusManager() {
         return services.getFacilityStatusManager();
     }
     
     private QueueManager getQueueManager() {
         return services.getQueueManager();
     }
     
     private UserDetailsManager getUserDetailsManager() {
         return services.getUserDetailsManager();
     }
     
     private AtomFeed getAtomFeed() {
         return services.getAtomFeed();
     }
     
     private ConfigurationManager getConfigManager() {
         return services.getConfigManager();
     }
     
     private PaulConfiguration getLatestConfig() {
         return getConfigManager().getLatestConfig();
     }
     
     private PaulConfiguration getConfig() {
         return getConfigManager().getActiveConfig();
     }
     
     private Facility lookupFacilityByName(String facilityName) {
         return (Facility) services.getFacilityMapper().lookup(null, facilityName, null);
     }
     
     private Collection<FacilityConfig> getFacilities() {
         return services.getFacilityMapper().allFacilities();
     }
     
     private EntityManager createEntityManager() {
         return services.getEntityManagerFactory().createEntityManager();
     }
     
     private Date determineCutoff(Model model, String olderThan, String age) {
         
         if (olderThan.isEmpty() && age.isEmpty()) {
             model.addAttribute("message", 
                     "Either an expiry date or an age must be supplied");
             return null;
         }
         String[] parts = age.split("\\s", 2);
         DateTime cutoff;
         if (olderThan.isEmpty()) {
             int value;
             try {
                 value = Integer.parseInt(parts[0]);
             } catch (NumberFormatException ex) {
                 model.addAttribute("message", "Age quantity is not an integer");
                 return null;
             }
             BaseSingleFieldPeriod p;
             switch (parts.length == 1 ? "" : parts[1]) {
             case "minute" : case "minutes" :
                 p = Minutes.minutes(value);
                 break;
             case "hour" : case "hours" :
                 p = Hours.hours(value);
                 break;
             case "day" : case "days" :
                 p = Days.days(value);
                 break;
             case "week" : case "weeks" :
                 p = Weeks.weeks(value);
                 break;
             case "month" : case "months" :
                 p = Months.months(value);
                 break;
             case "year" : case "years" :
                 p = Years.years(value);
                 break;
             default :
                 model.addAttribute("message", "Unrecognized age time-unit");
                 return null;
             }
             cutoff = DateTime.now().minus(p);
         } else {
             cutoff = parseTimestamp(olderThan);
             if (cutoff == null) {
                 model.addAttribute("message", "Unrecognizable expiry date");
                 return null;
             }
         }
         if (cutoff.isAfter(new DateTime())) {
             model.addAttribute("message", "Supplied or computed expiry date is in the future!");
             return null;
         }
         model.addAttribute("computedDate", FORMATS[0].print(cutoff));
         return cutoff.toDate();
     }
 
     private DateTime parseTimestamp(String stamp) {
         for (DateTimeFormatter format : FORMATS) {
             try {
                 return format.parseDateTime(stamp);
             } catch (IllegalArgumentException ex) {
                 continue;
             }
         }
         return null;
     }
 }
