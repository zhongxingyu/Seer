 package au.edu.uq.cmm.paul.servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
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
 
 import au.edu.uq.cmm.aclslib.config.ConfigurationException;
 import au.edu.uq.cmm.aclslib.config.FacilityConfig;
 import au.edu.uq.cmm.aclslib.proxy.AclsAuthenticationException;
 import au.edu.uq.cmm.aclslib.proxy.AclsInUseException;
 import au.edu.uq.cmm.aclslib.service.Service;
 import au.edu.uq.cmm.aclslib.service.Service.State;
 import au.edu.uq.cmm.eccles.UnknownUserException;
 import au.edu.uq.cmm.eccles.UserDetails;
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.PaulConfiguration;
 import au.edu.uq.cmm.paul.grabber.DatafileMetadata;
 import au.edu.uq.cmm.paul.grabber.DatasetMetadata;
 import au.edu.uq.cmm.paul.queue.QueueManager;
 import au.edu.uq.cmm.paul.queue.QueueManager.Slice;
 import au.edu.uq.cmm.paul.status.Facility;
 import au.edu.uq.cmm.paul.status.FacilityStatusManager;
 import au.edu.uq.cmm.paul.watcher.FileWatcher;
 
 /**
  * The MVC controller for Paul's web UI.  This supports the status and configuration
  * pages and also implements GET access to the files in the queue area.
  * 
  * @author scrawley
  */
 @Controller
 public class WebUIController {
     public enum Status {
         ON, OFF, TRANSITIONAL
     }
     
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
 
     @RequestMapping(value="/control", method=RequestMethod.GET)
     public String control(Model model) {
         addStateAndStatus(model);
         return "control";
     }
 
     @RequestMapping(value="/control", method=RequestMethod.POST)
     public String controlAction(Model model, HttpServletRequest request) {
         processStatusChange(getFileWatcher(), request.getParameter("watcher"));
         addStateAndStatus(model);
         return "control";
     }
     
     private void processStatusChange(Service service, String param) {
         Service.State current = service.getState();
         if (param == null) {
             return;
         }
         Status target = Status.valueOf(param);
         if (target == stateToStatus(current) || 
                 stateToStatus(current) == Status.TRANSITIONAL) {
             return;
         }
         if (target == Status.ON) {
             service.startStartup();
         } else {
             service.startShutdown();
         }
     }
     
     private void addStateAndStatus(Model model) {
         State ws = getFileWatcher().getState();
         model.addAttribute("watcherState", ws);
         model.addAttribute("watcherStatus", stateToStatus(ws));
         model.addAttribute("resetRequired", getLatestConfig() != getConfig());
     }
     
     private Status stateToStatus(State state) {
         switch (state) {
         case STARTED:
            return Status.ON;
         case FAILED:
         case STOPPED:
         case INITIAL:
             return Status.OFF;
         default:
             return Status.TRANSITIONAL;
         }
     }
     
     @RequestMapping(value="/sessions", method=RequestMethod.GET)
     public String status(Model model) {
         model.addAttribute("sessions", services.getFacilitySessionManager().getLatestSessions());
         return "sessions";
     }
     
     @RequestMapping(value="/sessions", method=RequestMethod.POST,
             params={"endSession"})
     public String endSession(Model model, 
             @RequestParam String sessionUuid, 
             HttpServletResponse response, HttpServletRequest request) 
     throws IOException, AclsAuthenticationException {
         services.getFacilitySessionManager().logoutSession(sessionUuid);
         response.sendRedirect(response.encodeRedirectURL(
                 request.getContextPath() + "/sessions"));
         return null;
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.GET)
     public String facilityConfig(@PathVariable String facilityName, Model model) 
             throws ConfigurationException {
         Facility facility = lookupFacilityByName(facilityName);
         model.addAttribute("facility", facility);
         return "facility";
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", 
             method=RequestMethod.GET, params={"sessionLog"})
     public String facilitySessions(@PathVariable String facilityName, Model model) 
             throws ConfigurationException {
         model.addAttribute("sessions", 
                 services.getFacilitySessionManager().sessionsForFacility(facilityName));
         model.addAttribute("facilityName", facilityName);
         return "facilitySessions";
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.POST, 
             params={"enableWatcher"})
     public String enableWatcher(@PathVariable String facilityName, Model model) 
             throws ConfigurationException {
         Facility facility = lookupFacilityByName(facilityName);
         if (facility != null) {
             getFileWatcher().startFileWatching(facility, true);
         }
         model.addAttribute("facility", facility);
         return "facility";
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.POST, 
             params={"disableWatcher"})
     public String disableWatcher(@PathVariable String facilityName, Model model) 
             throws ConfigurationException {
         Facility facility = lookupFacilityByName(facilityName);
         if (facility != null) {
             getFileWatcher().stopFileWatching(facility, true);
         }
         model.addAttribute("facility", facility);
         return "facility";
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.POST, 
             params={"pauseWatcher"})
     public String pauseWatcher(@PathVariable String facilityName, Model model) 
             throws ConfigurationException {
         Facility facility = lookupFacilityByName(facilityName);
         if (facility != null) {
             getFileWatcher().stopFileWatching(facility, false);
         }
         model.addAttribute("facility", facility);
         return "facility";
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
             @RequestParam String next) {
         model.addAttribute("message", "Select a facility from the pulldown");
         model.addAttribute("next", next);
         model.addAttribute("facilities", getFacilities());
         return "facilitySelect";
     }
     
     @RequestMapping(value="/facilitySelect", method=RequestMethod.POST)
     public String facilitySelect(Model model, 
             @RequestParam String next,
             HttpServletRequest request, HttpServletResponse response,
             @RequestParam(required=false) String facilityName) 
     throws UnsupportedEncodingException, IOException {
         if (facilityName == null) {
             model.addAttribute("facilities", getFacilities());
             model.addAttribute("message", "Select a facility from the pulldown");
             model.addAttribute("next", next);
             return "facilitySelect";
         } else {
             response.sendRedirect(request.getContextPath() + 
                     "/" + next +
                     "?facilityName=" + URLEncoder.encode(facilityName, "UTF-8") + 
                     "&returnTo=" + request.getContextPath());
             return null;
         }
     }
     
     @RequestMapping(value="/facilityLogin")
     public String startSession(@RequestParam String facilityName, 
             @RequestParam(required=false) String startSession,  
             @RequestParam(required=false) String endOldSession, 
             @RequestParam(required=false) String userName, 
             @RequestParam(required=false) String account,
             @RequestParam(required=false) String returnTo,
             Model model, HttpServletResponse response, HttpServletRequest request) 
                     throws IOException {
         FacilityStatusManager fsm = services.getFacilitySessionManager();
         facilityName = tidy(facilityName);
         returnTo = tidy(returnTo);
         if (!returnTo.startsWith(request.getContextPath())) {
             returnTo = request.getContextPath() + returnTo;
         }
         model.addAttribute("facilityName", facilityName);
         model.addAttribute("returnTo", returnTo);
 
 
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
             model.addAttribute("message", ex.getMessage());
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
    
     @RequestMapping(value="/config", method=RequestMethod.GET)
     public String config(Model model) {
         model.addAttribute("config", getLatestConfig());
         model.addAttribute("facilities", getFacilities());
         return "config";
     }
     
     @RequestMapping(value="/config", method=RequestMethod.POST, params={"reset"})
     public String configReset(Model model,
             @RequestParam(required=false) String confirmed) {
         model.addAttribute("returnTo", "config");
         if (confirmed == null) {
             return "resetConfirmation";
         } else {
             services.getConfigManager().resetConfiguration();
             model.addAttribute("message", 
                     "Configuration reset succeeded.  " +
                     "Please restart the webapp to use the updated configs");
             return "ok";
         }
     }
     
     @RequestMapping(value="/queue/held", method=RequestMethod.GET)
     public String held(Model model) {
         model.addAttribute("queue", 
                 services.getQueueManager().getSnapshot(Slice.HELD));
         return "held";
     }
     
     @RequestMapping(value="/queue/ingestible", method=RequestMethod.GET)
     public String queue(Model model) {
         model.addAttribute("queue", 
                 services.getQueueManager().getSnapshot(Slice.INGESTIBLE));
         return "queue";
     }
     
     @RequestMapping(value="/claimDatasets", method=RequestMethod.GET)
     public String showClaimDatasets(Model model,
             @RequestParam String facilityName) {
         model.addAttribute("facilityName", facilityName);
         model.addAttribute("datasets", 
                 services.getQueueManager().getSnapshot(Slice.HELD, facilityName));
         return "claimDatasets";
     }
     
     @RequestMapping(value="/claimDatasets", 
             method=RequestMethod.POST, params={"claim"})
     public String claimDatasets(Model model,
             HttpServletRequest request, HttpServletResponse response,
             @RequestParam(required=false) String[] ids,
             @RequestParam(required=false) String userName,
             @RequestParam String facilityName) 
     throws IOException {
         if (ids == null) {
             model.addAttribute("facilityName", facilityName);
             model.addAttribute("datasets", 
                     services.getQueueManager().getSnapshot(Slice.HELD, facilityName));
             model.addAttribute("message", "Check the checkboxes for the " +
                     "Datasets you want to claim");
             return "claimDatasets";
         }
         GenericPrincipal principal = (GenericPrincipal) request.getUserPrincipal();
         if (principal == null) {
             LOG.error("No principal ... can't proceed");
             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             return null;
         }
         if (userName == null) {
             if (!principal.hasRole("ROLE_ACLS_USER")) {
                 model.addAttribute("message", "You must be logged in using " +
                 		"your ACLS credentials to claim files");
                 model.addAttribute("returnTo", request.getContextPath());
                 return "failed";
             }
             userName = principal.getName();
         } else {
             if (!principal.hasRole("ROLE_ADMIN")) {
                 model.addAttribute("message", "Only an administrator can claim " +
                         "files for someone else");
                 model.addAttribute("returnTo", request.getContextPath());
                 return "failed";
             }
         }
         try {
             services.getQueueManager().changeUser(ids, userName);
         } catch (NumberFormatException ex) {
             LOG.debug("Rejected request with bad entry id(s)");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         model.addAttribute("message",
                 (ids.length == 0 ? "No datasets " :
                     ids.length == 1 ? "1 datasets " :
                         (ids.length + " datasets ")) + " claimed");
         return "ok";
     }
 
     @RequestMapping(value="/queue/{sliceName:held|ingestible}", 
             method=RequestMethod.POST, params={"deleteAll"})
     public String deleteAll(Model model, 
             @PathVariable String sliceName,
             @RequestParam(required=false) String mode, 
             @RequestParam(required=false) String confirmed) {
         model.addAttribute("returnTo", sliceName);
         if (confirmed == null) {
             return "queueDeleteConfirmation";
         }
         boolean discard = mode.equals("discard");
         QueueManager.Slice slice = QueueManager.Slice.valueOf(sliceName.toUpperCase());
         int count = services.getQueueManager().deleteAll(discard, slice);
         String verb = discard ? "deleted" : "archived";
         model.addAttribute("message",
                 (count == 0 ? "No queue entries " :
                     count == 1 ? "1 queue entry " :
                         (count + " queue entries ")) + verb);
         return "ok";
     }
     
     @RequestMapping(value="/queue/{sliceName:held|ingestible}", 
             method=RequestMethod.POST, 
             params={"expire"})
     public String expire(Model model, 
             @PathVariable String sliceName,
             @RequestParam(required=false) String mode, 
             @RequestParam(required=false) String confirmed,
             @RequestParam(required=false) String olderThan,
             @RequestParam(required=false) String period,
             @RequestParam(required=false) String unit) {
         Date cutoff = determineCutoff(model, tidy(olderThan), tidy(period), tidy(unit));
         model.addAttribute("returnTo", sliceName);
         if (cutoff == null || confirmed == null) {
             return "queueExpiryForm";
         }
         QueueManager.Slice slice = QueueManager.Slice.valueOf(sliceName.toUpperCase());
         int count = services.getQueueManager().expireAll(
                 mode.equals("discard"), slice, cutoff);
         model.addAttribute("message",
                 count == 0 ? "No queue entries expired" :
                     count == 1 ? "1 queue entry expired" :
                         (count + " queue entries expired"));
         return "ok";
     }
 
     @RequestMapping(value="/queue/{sliceName:held|ingestible}/{entry:.+}", 
             method=RequestMethod.GET)
     public String queueEntry(@PathVariable String entry, Model model, 
             @PathVariable String sliceName,
             HttpServletResponse response) 
             throws IOException {
         long id;
         try {
             id = Long.parseLong(entry);
         } catch (NumberFormatException ex) {
             LOG.debug("Rejected request with bad entry id");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         DatasetMetadata metadata = fetchMetadata(id);
         if (metadata == null) {
             LOG.debug("Rejected request for unknown entry");
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             return null;
         }
         model.addAttribute("entry", metadata);
         model.addAttribute("returnTo", sliceName);
         model.addAttribute("userNames", services.getUserDetailsManager().getUserNames());
         return "queueEntry";
     }
     
     @RequestMapping(value="/queue/{sliceName:held|ingestible}/{entry:.+}",
             method=RequestMethod.POST, 
             params={"delete"})
     public String deleteQueueEntry(@PathVariable String entry, Model model, 
             HttpServletResponse response,
             @PathVariable String sliceName,
             @RequestParam(required=false) String mode, 
             @RequestParam(required=false) String confirmed) 
             throws IOException {
         long id;
         try {
             id = Long.parseLong(entry);
         } catch (NumberFormatException ex) {
             LOG.debug("Rejected request with bad entry id");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         boolean discard = mode.equals("discard");
         services.getQueueManager().delete(id, discard);
         model.addAttribute("message",
                 "Queue entry " + (discard ? "deleted" : "archived"));
         model.addAttribute("returnTo", sliceName);
         return "ok";
     }
     
     @RequestMapping(value="/queue/{sliceName:held|ingestible}/{entry:.+}", 
             method=RequestMethod.POST, 
             params={"assign"})
     public String assignQueueEntry(@PathVariable String entry, Model model, 
             @PathVariable String sliceName,
             HttpServletResponse response, @RequestParam String userName) throws IOException {
         if (userName.trim().isEmpty()) {
             model.addAttribute("message", "Set the user name");
             return "queueEntry";
         }
         try {
             services.getQueueManager().changeUser(new String[]{entry}, userName);
         } catch (NumberFormatException ex) {
             LOG.debug("Rejected request with bad entry id");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         
         model.addAttribute("message", "Queue entry assigned to " + userName);
         model.addAttribute("returnTo", ".");
         return "ok";
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
         model.addAttribute("userNames", services.getUserDetailsManager().getUserNames());
         return "users";
     }
 
     @RequestMapping(value="/users/{userName:.+}", method=RequestMethod.GET)
     public String user(@PathVariable String userName, Model model,
             HttpServletResponse response) 
             throws IOException {
         try {
             UserDetails userDetails = services.getUserDetailsManager().lookupUser(userName, true);
             model.addAttribute("user", userDetails);
         } catch (UnknownUserException e) {
             LOG.debug("Rejected request for security reasons");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         return "user";
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
 
     private DatasetMetadata fetchMetadata(long id) {
         EntityManager entityManager = createEntityManager();
         try {
             TypedQuery<DatasetMetadata> query = entityManager.createQuery(
                     "from DatasetMetadata d where d.id = :id", 
                     DatasetMetadata.class);
             query.setParameter("id", id);
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
     
     private PaulConfiguration getLatestConfig() {
         return services.getConfigManager().getLatestConfig();
     }
     
     private PaulConfiguration getConfig() {
         return services.getConfigManager().getActiveConfig();
     }
     
     private Facility lookupFacilityByName(String facilityName) 
             throws ConfigurationException {
         return (Facility) services.getFacilityMapper().lookup(null, facilityName, null);
     }
     
     private Collection<FacilityConfig> getFacilities() {
         return services.getFacilityMapper().allFacilities();
     }
     
     private EntityManager createEntityManager() {
         return services.getEntityManagerFactory().createEntityManager();
     }
     
     private String tidy(String str) {
         return str == null ? "" : str.trim();
     }
     
     private Date determineCutoff(Model model, String olderThan, 
             String period, String unit) {
         if (olderThan.isEmpty() && period.isEmpty()) {
             model.addAttribute("message", 
                     "Either an expiry date or period must be supplied");
             return null;
         }
         DateTime cutoff;
         if (olderThan.isEmpty()) {
             int value;
             try {
                 value = Integer.parseInt(period);
             } catch (NumberFormatException ex) {
                 model.addAttribute("message", "Malformed period");
                 return null;
             }
             BaseSingleFieldPeriod p;
             switch (unit) {
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
                 model.addAttribute("message", "Unrecognized unit");
                 return null;
             }
             cutoff = DateTime.now().minus(p);
         } else {
             cutoff = null;
             for (DateTimeFormatter format : FORMATS) {
                 try {
                     cutoff = format.parseDateTime(olderThan);
                     break;
                 } catch (IllegalArgumentException ex) {
                     continue;
                 }
             }
             if (cutoff == null) {
                 model.addAttribute("message", "Unrecognizable expiry date");
                 return null;
             }
         }
         if (cutoff.isAfter(new DateTime())) {
             model.addAttribute("message", "Expiry date is in the future");
             return null;
         }
         model.addAttribute("computedDate", FORMATS[0].print(cutoff));
         return cutoff.toDate();
     }
 }
