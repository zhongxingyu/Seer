 package org.dianexus.triceps;
 
 import org.dialogix.util.XMLAttrEncoder;
 
 import java.io.PrintWriter;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Vector;
 import java.util.Enumeration;
 import java.util.StringTokenizer;
 import java.util.Hashtable;
 import java.util.logging.*;
 import java.util.HashMap;
 import org.dialogix.timing.DialogixTimingCalculator;
 
 /**
 This is effectively the FrontController (or should have been) which manages all actions but is tightly coupled with HTML
  */
 public class TricepsEngine implements VersionIF {
 
     private static final String LoggerName = "org.dianexus.triceps.TricepsEngine";
     static final String CONTENT_TYPE = "text/html; charset=UTF-8";	// can make UTF-8 by default?
     private String userAgent = "";
     private StringBuffer errors;
     private StringBuffer info;
     private HashMap<String,String> requestParameters = null;
     private String hiddenLoginToken = null;
     private String restoreFile = null;
     private String firstFocus = null;
     private String scheduleSrcDir = "";
     private String workingFilesDir = "";
     private String completedFilesDir = "";
     private String imageFilesDir = "";
     private String logoIcon = "";
     private String floppyDir = "";
     private String helpURL = "";
     private String activePrefix = "";
     private String activeSuffix = "";
     private String inactivePrefix = "";
     private String inactiveSuffix = "";
     private String dialogix_dir = "";
     private String formActionURL = null;
     private String ipAddress = null;
     private boolean isSecure = false;
 
     /* hidden variables */
     private boolean debugMode = false;
     private boolean developerMode = false;
     private boolean okToShowAdminModeIcons = false;	// allows AdminModeIcons to be visible
     private boolean okPasswordForTempAdminMode = false;	// allows AdminModeIcon values to be accepted
     private boolean showQuestionNum = false;
     private boolean showAdminModeIcons = false;
     private boolean autogenOptionNums = true;	// default is to make reading options easy
     private boolean isSplashScreen = false;
     private boolean allowEasyBypass = false;	// means that a special value is present, so enable the possibility of okPasswordForTempAdminMode
     private boolean allowComments = false;		// lets comments be shown always, and rest of buttons be shown with password
     private boolean allowRecordEvents = false;
     private boolean allowRefused = false;
     private boolean allowUnknown = false;
     private boolean allowNotUnderstood = false;
     private boolean allowLanguageSwitching = false;
     private boolean allowJumpTo = false;
     private boolean alwaysShowAdminIcons = false;
     private boolean showSaveToFloppyInAdminMode = false;
     private boolean wrapAdminIcons = true;
     private boolean disallowComments = false;	// prevents comments from ever being shown
     private boolean displayWorking = false;	// whether to allow the working files to be visible - even in Web-server versions
     private String directive = null;	// the default
     private Triceps triceps;    
     private Schedule schedule;  // FIXME = new Schedule(null, null, false);	// triceps.getSchedule()
     private int colpad = 2;
     private boolean isActive = true;	// default is active -- only becomes inactive when times out, or reaches "finished" state
     private ArrayList<String> dlxObjects = null; // a Javascript hashmap;
     
     public TricepsEngine() {
         // Need blank constructor for JSP
     }
 
     /**
     Constructor,initializing all context
      */
     public TricepsEngine(HashMap<String,String> initParams) {
         init(initParams);
     }
 
     /**
     init, setting all global parameters
      */
     public void init(HashMap<String,String> config) {
         dialogix_dir = getInitParam(config, "dialogix.dir");	// must be first
         scheduleSrcDir = getInitParam(config, "scheduleSrcDir");
         workingFilesDir = getInitParam(config, "workingFilesDir");
         completedFilesDir = getInitParam(config, "completedFilesDir");
         imageFilesDir = getInitParam(config, "imageFilesDir");
         logoIcon = getInitParam(config, "logoIcon");
         floppyDir = getInitParam(config, "floppyDir");
         helpURL = getInitParam(config, "helpURL");
         displayWorking = Boolean.valueOf(getInitParam(config,"displayWorking")).booleanValue();
     }
 
     /**
     Set initialization parameters (what does this actually do)?
      */
     private String getInitParam(HashMap<String,String> config,
                                  String which) {
         String s = config.get(which);
 
         // use ant-like variable name substitution?  No -- too hard for now -- simply assume that all dirs can be relative to deployment location
 
         if (s == null) {
             return "";
         }
         s = s.trim();
         s = s.replace('\\', '/');	// so that uses unix file separators
         if (s.charAt(0) == '/' || which.indexOf("Dir") == -1 || s.charAt(1) == ':') {
             return s;
         } else {
             // relative to dialogix.dir
             return dialogix_dir + s;
         }
     }
 
     /**
     getIcon for display
      */
     String getIcon(int which) {
         return schedule.getReserved(Schedule.IMAGE_FILES_DIR) + schedule.getReserved(which);
     }
 
     /**
     Process all Actions, writing out new page.
      */
     public void doPost(HashMap<String,String> requestParameters,
                         String formActionURL,
                         PrintWriter out,
                         String hiddenLoginToken,
                         String ipAddress,
                         boolean isSecure,
                         String userAgent,
                         String restoreFile) {
         try {
             this.errors = new StringBuffer();
             this.info = new StringBuffer();
 
             this.requestParameters = requestParameters;
             this.formActionURL = formActionURL;
             this.hiddenLoginToken = hiddenLoginToken;
             this.restoreFile = restoreFile;
             this.ipAddress = ipAddress;
             this.isSecure = isSecure;
             XmlString form = null;
             firstFocus = null; // reset it each time
             directive = requestParameters.get("DIRECTIVE");	// XXX: directive must be set before calling processHidden
             this.userAgent = userAgent;
 
             if (DB_LOG_FULL) {
                 if (!"RESTORE".equals(directive)) {
                     if (triceps != null) {
                         DialogixTimingCalculator dtc = triceps.getDtc();
                         if (dtc != null) {
                             dtc.setLastAction(directive);
                             dtc.beginServerProcessing();
                         }
                     }
                 }
             }
 
             if (directive != null && directive.trim().length() == 0) {
                 directive = null;
             }
 
             /* Hack to support authenticated access to instruments */
             if (restoreFile != null) {
                 /* means that this is the name of the file to restore, but it has already been restored, so second hack below */
                 directive = "RESTORE";
             } else {
                 if (DEPLOYABLE) {
                     if (triceps != null) {
                         triceps.receivedResponseFromUser();
                     }
                 }
             }
             // end code addition
             setGlobalVariables();
 
             processPreFormDirectives();
             processHidden();
 
             form = new XmlString(createForm(hiddenLoginToken));
 
 
             out.println(header());	// must be processed AFTER createForm, otherwise setFocus() doesn't work
             new XmlString(getCustomHeader(), out);
             
             if (info.length() > 0) {
                 out.println("<b>");
                 new XmlString(info.toString(), out);
                 out.println("</b><hr>");
             }
             if (errors.length() > 0) {
                 out.println("<b>");
                 new XmlString(errors.toString(), out);
                 out.println("</b><hr>");
             }
 
             if (form.hasErrors()) {	// do I want to show HTML errors to users?
                 String errs = form.getErrors();
                 if (AUTHORABLE) {
                     new XmlString("<b>" + errs + "</b>", out);
                 }
                 Logger.getLogger(LoggerName).log(Level.FINE, "##" + errs);
             }
 
             out.println(form.toString());
 
             if (!isSplashScreen) {
                 new XmlString(generateDebugInfo(), out);
             }
 
             triceps.sentRequestToUser();	// XXX when should this be set? before, during, or near end of writing to out buffer?
 
             if (DB_LOG_FULL) {
                 if (triceps.existsDtc()) {
                     triceps.getDtc().logBrowserInfo(ipAddress, userAgent);
                     triceps.getDtc().setToVarNum(triceps.getCurrentStep());
                     triceps.getDtc().finishServerProcessing(requestParameters.get("EVENT_TIMINGS"));
                 }
             }
 
             out.println(footer());	// should not be parsed
 
             // set as finished if needed
             if ("finished".equals(directive)) {
                 this.isActive = false;
             }
         } catch (Exception t) {
             Logger.getLogger(LoggerName).log(Level.SEVERE, "Unexpected Error", t);
         }
     }
 
     /**
     set language
      */
     private void processPreFormDirectives() {
         /* setting language doesn't use directive parameter */
         if (triceps != null && triceps.isValid()) { // FIXME 6/25/08
             String language = requestParameters.get("LANGUAGE"); // FIXME - this might be why language not being set to English?
             if (language != null && language.trim().length() > 0) {
                 if (DB_LOG_FULL) {
                     triceps.getDtc().setLangCode(language.trim());
                 }
                 triceps.setLanguage(language.trim());
                 directive = "refresh current";
             }
         } else {
             return;
         }
 
         if (AUTHORABLE) {
             /* Want to evaluate expression before doing rest so can see results of changing global variable values */
             if (directive != null && directive.equals("evaluate_expr")) {
                 String expr = requestParameters.get("evaluate_expr_data");
                 if (expr != null) {
                     Datum datum = triceps.evaluateExpr(expr);
 
                     errors.append("<table width='100%' cellpadding='2' cellspacing='1' border='1'>");
                     errors.append("<tr><td>Equation</td><td><b>" + expr + "</b></td><td>Type</td><td><b>" + datum.getTypeName() + "</b></td></tr>");
                     errors.append("<tr><td>String</td><td><b>" + datum.stringVal(true) +
                         "</b></td><td>boolean</td><td><b>" + datum.booleanVal() +
                         "</b></td></tr>" + "<tr><td>double</td><td><b>" +
                         datum.doubleVal() + "</b></td><td>&nbsp;</td><td>&nbsp;</td></tr>");
                     errors.append("<tr><td>date</td><td><b>" + datum.dateVal() + "</b></td><td>month</td><td><b>" + datum.monthVal() + "</b></td></tr>");
                     errors.append("</table>");
 
                     errors.append(triceps.getParser().getErrors());
                 } else {
                     errors.append("empty expression");
                 }
             }
         }
     }
 
     /**
     set global variables - effectively restore the session state, creating local variable names rather the repeating get calls
      */
     private void setGlobalVariables() {
         if (triceps != null && triceps.isValid()) { // FIXME 6/25/08
             debugMode = schedule.getBooleanReserved(Schedule.DEBUG_MODE);
             developerMode = schedule.getBooleanReserved(Schedule.DEVELOPER_MODE);
             showQuestionNum = schedule.getBooleanReserved(Schedule.SHOW_QUESTION_REF);
             showAdminModeIcons = schedule.getBooleanReserved(Schedule.SHOW_ADMIN_ICONS);
             autogenOptionNums = schedule.getBooleanReserved(Schedule.AUTOGEN_OPTION_NUM);
             allowComments = schedule.getBooleanReserved(Schedule.ALLOW_COMMENTS);
             allowRecordEvents = schedule.getBooleanReserved(Schedule.RECORD_EVENTS);
             allowRefused = schedule.getBooleanReserved(Schedule.ALLOW_REFUSED);
             allowUnknown = schedule.getBooleanReserved(Schedule.ALLOW_UNKNOWN);
             allowNotUnderstood = schedule.getBooleanReserved(Schedule.ALLOW_DONT_UNDERSTAND);
             allowLanguageSwitching = schedule.getBooleanReserved(Schedule.ALLOW_LANGUAGE_SWITCHING);
             allowJumpTo = schedule.getBooleanReserved(Schedule.ALLOW_JUMP_TO);
             alwaysShowAdminIcons = schedule.getBooleanReserved(Schedule.ALWAYS_SHOW_ADMIN_ICONS);
             showSaveToFloppyInAdminMode = schedule.getBooleanReserved(Schedule.SHOW_SAVE_TO_FLOPPY_IN_ADMIN_MODE);
             wrapAdminIcons = schedule.getBooleanReserved(Schedule.WRAP_ADMIN_ICONS);
             disallowComments = schedule.getBooleanReserved(Schedule.DISALLOW_COMMENTS);
             
             activePrefix = schedule.getReserved(Schedule.ACTIVE_BUTTON_PREFIX);
             activeSuffix = schedule.getReserved(Schedule.ACTIVE_BUTTON_SUFFIX);
             inactivePrefix = spaces(activePrefix);
             inactiveSuffix = spaces(activeSuffix);            
         } else {
             debugMode = false;
             developerMode = false;
             showQuestionNum = false;
             showAdminModeIcons = false;
             autogenOptionNums = true;
             allowComments = false;
             allowRecordEvents = true;	// so captures info about opening screens
             allowRefused = false;
             allowUnknown = false;
             allowNotUnderstood = false;
             allowLanguageSwitching = false;
             allowJumpTo = false;
             alwaysShowAdminIcons = false;
             showSaveToFloppyInAdminMode = false;
             wrapAdminIcons = true;
             disallowComments = false;
         }
         allowEasyBypass = false;
         okPasswordForTempAdminMode = false;
         okToShowAdminModeIcons = alwaysShowAdminIcons;	// the default -- either on or off.
         isSplashScreen = false;
     }
 
     /**
     Not sure what this does
      */
     private String spaces(String src) {
         StringBuffer sb = new StringBuffer();
         if (src == null) {
             return "";
         }
         for (int i = 0; i < src.length(); ++i) {
             sb.append("  ");
         }
         return sb.toString();
     }
 
     /**
     Helper function for processing several classes of functions
      */
     private void processHidden() {
         /* Has side-effects - so must occur before createForm() */
         if (triceps == null || !triceps.isValid()) {    // FIXME 6/25/08
             return;
         }
 
         String settingAdminMode = requestParameters.get("PASSWORD_FOR_ADMIN_MODE");
         if (settingAdminMode != null && settingAdminMode.trim().length() > 0) {
             /* if try to enter a password, make sure that doesn't reset the form if password fails */
             String passwd = triceps.getPasswordForAdminMode();
             if (passwd != null) {
                 if (passwd.trim().equals(settingAdminMode.trim())) {
                     okToShowAdminModeIcons = true;	// so allow AdminModeIcons to be displayed
                 } else {
                     info.append(triceps.get("incorrect_password_for_admin_mode"));
                 }
             }
             directive = "refresh current";	// so that will set the admin mode password
         }
 
         if (triceps.isTempPassword(requestParameters.get("TEMP_ADMIN_MODE_PASSWORD"))) {
             // enables the password for this session only
             okPasswordForTempAdminMode = true;	// allow AdminModeIcon values to be accepted
         }
 
         if (AUTHORABLE) {
             /** Process requests to change developerMode-type status **/
             if (directive != null) {
                 /* Toggle these values, as requested */
                 if (directive.equals("turn_developerMode")) {
                     developerMode = !developerMode;
                     schedule.setReserved(Schedule.DEVELOPER_MODE, String.valueOf(developerMode));
                     directive = "refresh current";
                 } else if (directive.equals("turn_debugMode")) {
                     debugMode = !debugMode;
                     schedule.setReserved(Schedule.DEBUG_MODE, String.valueOf(debugMode));
                     directive = "refresh current";
                 } else if (directive.equals("turn_showQuestionNum")) {
                     showQuestionNum = !showQuestionNum;
                     schedule.setReserved(Schedule.SHOW_QUESTION_REF, String.valueOf(showQuestionNum));
                     directive = "refresh current";
                 } else if (directive.equals("sign_schedule")) { // FIXME - now that databasing, just flag the schedule as deployed
 //                    String name = schedule.signAndSaveAsJar();
 //                    if (name != null) {
 //                        errors.append(triceps.get("signed_schedule_saved_as") + name);
 //                    } else {
                         errors.append(triceps.get("unable_to_save_signed_schedule"));
 //                    }
                     directive = "refresh current";
                 } else if (directive.equals("toggle_EventCollection")) {
                     allowRecordEvents = !allowRecordEvents;
                     schedule.setReserved(Schedule.RECORD_EVENTS, String.valueOf(allowRecordEvents));
                     directive = "refresh current";
                 }
             }
         }
     }
 
     /**
     Create HTML headers to show icon and header message.<br>
     Should decouple the HTML
      */
     private String getCustomHeader() {
         StringBuffer sb = new StringBuffer();
 
         sb.append("<table border='0' cellpadding='0' cellspacing='3' width='100%'>");
         sb.append("<tr>");
         sb.append("<td width='1%'>");
 
         String logo = (!isSplashScreen && triceps.isValid()) ? triceps.getIcon() : logoIcon;
         if (logo.trim().length() == 0) {
             sb.append("&nbsp;");
         } else {
             sb.append("<img name='icon' id='icon' src='" + (imageFilesDir + logo) + "' align='top' border='0'" +
                 ((!isSplashScreen) ? " onmouseup='setAdminModePassword();'" : "") +
                 ((!isSplashScreen) ? (" alt='" + triceps.get("LogoMessage") + "'") : "") +
                 ">");
         }
         sb.append("	</td>");
         sb.append("	<td align='left'><font SIZE='4'>" + ((triceps.isValid() && !isSplashScreen) ? triceps.getHeaderMsg() : LICENSE_MSG) + "</font></td>");
 
         String globalHelp = null;
         if (triceps.isValid() && !isSplashScreen) {
             globalHelp = schedule.getReserved(Schedule.SCHED_HELP_URL);
         } else {
             globalHelp = helpURL;
         }
 
         sb.append("	<td width='1%'>");
         if (globalHelp != null && globalHelp.trim().length() != 0) {
             sb.append("<img src='" + getIcon(Schedule.HELP_ICON) + "' alt='" + triceps.get("Help") + "' align='top' border='0' onmouseup='help(\"_TOP_\",\"" + globalHelp + "\");'>");
         } else {
             sb.append("&nbsp;");
         }
         sb.append("</td></tr>");
         sb.append("</table>");
 
         return sb.toString();
     }
 
     /**
     Create footer of HTML page
      */
     private String footer() {
         StringBuffer sb = new StringBuffer();
 
         sb.append("</body>\n");
         sb.append("</html>\n");
         return sb.toString();
     }
 
     /**
     Get sorted list of names of instruments, optionally included suspended sessions which could be restored
      */
 //    private Hashtable getSortedNames(String dir,
 //                                      boolean isSuspended) {
 //        Hashtable names = new Hashtable();
 //        Schedule sched = null;
 //        Object prevVal = null;
 //        String defaultTitle = null;
 //        String title = null;
 //
 //        try {
 //            ScheduleList interviews = new ScheduleList(triceps, dir, isSuspended);
 //
 //            if (interviews.hasErrors()) {
 ////				errors.append(triceps.get("error_getting_list_of_available_interviews"));
 //                errors.append(interviews.getErrors());
 //            }
 ////			else {
 //            Vector schedules = interviews.getSchedules();
 //            for (int i = 0; i < schedules.size(); ++i) {
 //                sched = (Schedule) schedules.elementAt(i);
 //
 //                try {
 //                    defaultTitle = getScheduleInfo(sched, isSuspended);
 //                    title = defaultTitle;
 //                    if (title == null) {
 //                        title = "";
 //                    }
 //                    for (int count = 2; true; ++count) {
 //                        prevVal = names.put(title, sched.getLoadedFrom());
 //                        if (prevVal != null) {
 //                            names.put(title, prevVal);
 //                            title = defaultTitle + " (# " + count + ")";
 //                        } else {
 //                            break;
 //                        }
 //                    }
 //                } catch (Exception t) {
 //                    logger.log(Level.SEVERE, "unexpected_error", t);
 //                }
 //            }
 ////			}
 //        } catch (Exception t) {
 //            logger.log(Level.SEVERE, "unexpected_error", t);
 //        }
 //        return names;
 //    }
 
     /** 
     Show name and step# of current state within schedule 
      */
     String getScheduleStatus() {
         if (schedule == null || !schedule.isLoaded()) {
             return " null";
         } else {
             String token = null;
             StringTokenizer st = new StringTokenizer(schedule.getReserved(Schedule.SCHEDULE_SOURCE), "/\\:");	// for *n*x, DOS, and Mac
             int count = st.countTokens();
             StringBuffer sb = new StringBuffer(" ");
 
             for (int i = 1; i <= count; ++i) {
                 token = st.nextToken();
                 if (i == (count - 3)) {
                     sb.append(token).append("/");
                 }
                 if (i == count) {
                     sb.append(token);
                 }
             }
             sb.append("(").append(schedule.getReserved(Schedule.STARTING_STEP)).append(")");
             return sb.toString();
         }
     }
 
     /**
     Get name of current instrument
      */
 //    String getInstrumentName() {
 //        if (schedule == null || !schedule.isLoaded()) {
 //            return " null";
 //        } else {
 //            String token = null;
 //            StringTokenizer st = new StringTokenizer(schedule.getReserved(Schedule.SCHEDULE_SOURCE), "/\\:");	// for *n*x, DOS, and Mac
 //            int count = st.countTokens();
 //            StringBuffer sb = new StringBuffer(" ");
 //
 //            for (int i = 1; i <= count; ++i) {
 //                token = st.nextToken();
 //                if (i == (count - 3)) {
 //                    sb.append(token).append("/");
 //                }
 //                if (i == count) {
 //                    sb.append(token);
 //                }
 //            }
 //            return sb.toString();
 //        }
 //    }
 
     String getHashCode() {
         return Integer.toHexString(this.hashCode()) + "." +
             Integer.toHexString(triceps.hashCode());
     }
 
     /**
     Get current step number
      */
     String getCurrentStep() {
         if (schedule == null || !schedule.isLoaded()) {
             return " null";
         } else {
             return schedule.getReserved(Schedule.STARTING_STEP);
         }
     }
 
     /**
     Get instrument title
      */
 //    private String getScheduleInfo(Schedule sched,
 //                                    boolean isSuspended) {
 //        if (sched == null) {
 //            return null;
 //        }
 //
 //        StringBuffer sb = new StringBuffer();
 //        String s = null;
 //
 //        if (isSuspended) {
 //            sb.append(sched.getReserved(Schedule.TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS));
 //        } else {
 //            s = sched.getReserved(Schedule.TITLE);
 //            if (s != null && s.trim().length() > 0) {
 //                sb.append(s);
 //            } else {
 //                sb.append("NO_TITLE");
 //            }
 //        /*
 //        Vector v = sched.getLanguages();
 //        if (v.size() > 1) {
 //        sb.append("[");
 //        for (int i=0;i<v.size();++i) {
 //        sb.append((String) v.elementAt(i));
 //        if (i != v.size()-1) {
 //        sb.append(",");
 //        }
 //        }
 //        sb.append("]");
 //        }
 //         */
 //        }
 //
 //        return sb.toString();
 //    }
 
     /**
     Helper to sort list of instrument names
      */
 //    private ArrayList<String> getSortedKeys(Hashtable ht) {
 //        /* alloate array */
 //        ArrayList<String> array = new ArrayList<String>();
 //
 //        /* fill array */
 //        Enumeration _enum = ht.keys();
 //        while (_enum.hasMoreElements()) {
 //            array.add((String) _enum.nextElement());
 //        }
 //
 //        /* sort them */
 //        Collections.sort(array);
 //        return array;
 //    }
 
     /**
     Helper to find files within Instrument directory and create HTML list of those which are loadable instruments
      */
 //    private String selectFromInterviewsInDir(String selectTarget,
 //                                              String dir,
 //                                              boolean isSuspended) {
 //        StringBuffer sb = new StringBuffer();
 //
 //        try {
 //            Hashtable names = getSortedNames(dir, isSuspended);
 //
 //            if (names.size() > 0) {
 //                sb.append("<select name='" + selectTarget + "' id='" + selectTarget + "'>");
 //                if (isSuspended) {
 //                    /* add a blank line so don't accidentally resume a file instead of starting one */
 //                    sb.append("<option value=''>&nbsp;</option>");
 //                }
 //                /* get sorted names */
 //                ArrayList<String> sortedNames = getSortedKeys(names);
 //                for (int c = 0; c < sortedNames.size(); ++c) {
 //                    String title = sortedNames.get(c);
 //                    String target = (String) names.get(title);
 //                    File file = new File(target);
 //                    String name = file.getName();
 //                    boolean local = name.startsWith("tri");
 //                    String message = null;
 //
 //                    if (isSuspended) {
 //                        message = title + "<br>(from " + ((local) ? "working file " : "suspended file ") + file.getName() + ")";
 //                    } else {
 //                        message = title + "<br>(from " + file.getName() + ")";
 //                    }
 //                    int max_text_len = Integer.parseInt(triceps.getSchedule().getReserved(Schedule.MAX_TEXT_LEN_FOR_COMBO));
 //
 //                    Vector v = subdivideMessage(message, max_text_len);
 //                    for (int i = 0; i < v.size(); ++i) {
 //                        sb.append("	<option value='" + target + "'>");
 //                        if (i > 0) {
 //                            sb.append("&nbsp;&nbsp;&nbsp;");
 //                        }
 //                        sb.append((String) v.elementAt(i));
 //                        sb.append("</option>");
 //                    }
 //                }
 //                sb.append("</select>");
 //            }
 //        } catch (Exception t) {
 //            logger.log(Level.SEVERE, "error_building_sorted_list_of_interviews", t);
 //        }
 //
 //        if (sb.length() == 0) {
 //            return "&nbsp;";
 //        } else {
 //            return sb.toString();
 //        }
 //    }
 
     /**
     Helper to create form which conditionally shows errors, items, directives, and debug.  
     Calls Node and other helpers to compose HTML fragments for sub-elements.
      */
     private String createForm(String hiddenLoginToken) {
 //        logger.log(Level.FINER, "in triceps engine create form");
         StringBuffer sb = new StringBuffer();
         String formStr = null;
         dlxObjects = new ArrayList<String>();
 
         sb.append("<FORM method='POST' name='dialogixForm' id='dialogixForm' action='");
         sb.append(this.formActionURL);
         sb.append("'>");
 
         formStr = processDirective();	// since this sets isSplashScreen, which is needed to decide whether to display language buttons
 
         if ("finished".equals(directive)) {
             return formStr;
         }
 
         sb.append(languageButtons());
 
         sb.append(formStr);
 
         if (hiddenLoginToken != null) {
             sb.append(hiddenLoginToken);
         }
         sb.append("<input type='hidden' name='PASSWORD_FOR_ADMIN_MODE' id='PASSWORD_FOR_ADMIN_MODE' value=''>");	// must manually bypass each time
         sb.append("<input type='hidden' name='LANGUAGE' id='LANGUAGE' value=''>");
         if (DEPLOYABLE) {
             sb.append("<input type='hidden' name='EVENT_TIMINGS' id='EVENT_TIMINGS' value=''>");	// list of event timings
         }
         if (directive == null || directive.equals("select_new_interview")) {
             sb.append("<input type='hidden' name='DIRECTIVE' id='DIRECTIVE' value='START'>");	// so that ENTER tries to go next, and will be trapped if needed
         } else {
             sb.append("<input type='hidden' name='DIRECTIVE' id='DIRECTIVE' value='next'>");
         }
 
 
         sb.append("</FORM>");
 
         return sb.toString();
     }
 
     /**
     Helper to create HTML for buttons to change language
      */
     private String languageButtons() {
         if (isSplashScreen || !triceps.isValid() || !allowLanguageSwitching) {
             return "";
         }
 
         StringBuffer sb = new StringBuffer();
 
         /* language switching section */
 //		if (!isSplashScreen && triceps.isValid()) {
         if (triceps.isValid()) {
             Vector languages = schedule.getLanguages();
             Vector languagesISO = schedule.getLanguagesISO();
             if (languages.size() > 1) {
                 sb.append("<table width='100%' border='0'><tr><td align='center'>");
                 for (int i = 0; i < languages.size(); ++i) {
                     String language = (String) languages.elementAt(i);
                     String languageISO = (String) languagesISO.elementAt(i);
                     boolean selected = (i == triceps.getLanguage());
                     sb.append(((selected) ? "\n<u>" : "") +
                         "<input type='button' onclick='setLanguage(\"" + languageISO + "\");' name='select_" + languageISO + "' id='select_" + languageISO + "' value='" + language + "'>" +
                         ((selected) ? "</u>" : ""));
                 }
                 sb.append("</td></tr></table>");
             }
         }
         return sb.toString();
     }
 
     /**
     Master switch statement to handle Actions (Directives).  Returns HTML String of final form
      */
     private String processDirective() {
         boolean ok = true;
         int gotoMsg = Triceps.OK;
         StringBuffer sb = new StringBuffer();
         Enumeration nodes;
 
         // get the POSTed directive (start, back, next, help, suspend, etc.)	- default is opening screen
         if (directive == null || directive.equals("select_new_interview")) {
 //            if (hiddenLoginToken != null) {
                 /* means that not allowed to have access to a general list of instruments */
                 return "";
 //            }
 
 //            /* Construct splash screen */
 //            if (DISPLAY_SPLASH) {
 //                isSplashScreen = true;
 //                triceps.setLanguage(null);	// the default
 //
 //                sb.append("<table cellpadding='2' cellspacing='2' border='1'>");
 //                sb.append("<tr><td>" + triceps.get("please_select_an_interview") + "</td>");
 //                sb.append("<td>");
 //
 //                /* Build the list of available interviews */
 //                sb.append(selectFromInterviewsInDir("schedule", scheduleSrcDir, false));
 //
 //                sb.append("</td><td>");
 //                sb.append(buildSubmit("START"));
 //                sb.append("</td></tr>");
 //
 //                if (DISPLAY_WORKING || displayWorking) {
 //                    /* Build the list of suspended interviews */
 //                    sb.append("<tr><td>");
 //                    sb.append(triceps.get("or_restore_an_interview_in_progress"));
 //                    sb.append("</td><td>");
 //
 //                    sb.append(selectFromInterviewsInDir("RestoreSuspended", workingFilesDir, true));
 //
 //                    sb.append("</td><td>");
 //                    sb.append(buildSubmit("RESTORE"));
 //                    sb.append("</td></tr>");
 //                }
 //
 //                if (!WEB_SERVER) {
 //                    sb.append("<tr><td>");
 //                    sb.append(triceps.get("or_restore_from_floppy"));
 //                    sb.append("</td><td colspan='2' align='center'>");
 //                    sb.append(buildSubmit("RESTORE_FROM_FLOPPY"));
 //                    sb.append("</td></tr>");
 //                }
 //                sb.append("</table>");
 //            }
 //
 //            return sb.toString();
         } else if (directive.equals("START")) {
             // load schedule
             ok = getNewTricepsInstance(getCanonicalPath(requestParameters.get("schedule")), requestParameters, false);
 
             if (!ok) {
                 directive = null;
                 return processDirective();
             }
 
             // re-check developerMode options - they aren't set via the hidden options, since a new copy of Triceps created
             setGlobalVariables();
 
             ok = ok && ((gotoMsg = triceps.gotoStarting()) == Triceps.OK);	// don't proceed if prior error
         // ask question
         } else if (directive.equals("RESTORE")) {
             String restore;
 
             if (restoreFile != null) {
                 restore = restoreFile;
             } else {
                 restore = getCanonicalPath(requestParameters.get("RestoreSuspended"));
             }
             if (restore == null || restore.trim().length() == 0) {
 //                String ss = requestParameters.get("ss");    // HACK 7/14/08 - to allow for starting a session
 //                if (ss == null || ss.trim().length() == 0) {
                     directive = null;
                     return processDirective();
 //                }
             }
 
             if (restoreFile == null) {
                 ok = getNewTricepsInstance(restore, requestParameters, true);
                 if (!ok) {
                     directive = null;
 
                     errors.append(triceps.get("unable_to_find_or_access_schedule") + " @'" + restore + "'");
                     return processDirective();
                 }
             }
 
             setGlobalVariables();
 
             ok = ok && ((gotoMsg = triceps.gotoStarting()) == Triceps.OK);	// don't proceed if prior error
 
         // ask question
         } else if (directive.equals("jump_to")) {
             if ((AUTHORABLE && developerMode) || allowJumpTo) {
                 gotoMsg = triceps.gotoNode(requestParameters.get("jump_to_data"));
                 ok = (gotoMsg == Triceps.OK);
             // ask this question
             }
         } else if ("refresh current".equals(directive)) {
             ok = true;
         // re-ask the current question
         } else if (directive.equals("restart_clean")) { // restart from scratch
             if (AUTHORABLE) {
                 triceps.resetEvidence();
                 ok = ((gotoMsg = triceps.gotoFirst()) == Triceps.OK);	// don't proceed if prior error
             // ask first question
             }
         } else if (directive.equals("reload_questions")) { // debugging option
             if (AUTHORABLE) {
                 ok = triceps.reloadSchedule();
                 if (ok) {
                     info.append(triceps.get("schedule_restored_successfully"));
                 }
                 schedule = triceps.getSchedule();	// so that update the local pointer
             // re-ask current question
             }
         } else if (directive.equals("save_to")) {
             if (AUTHORABLE) {
                 String name = requestParameters.get("save_to_data");
                 if (ok) {
                     info.append(triceps.get("interview_saved_successfully_as") + (workingFilesDir + name));
                 }
             }
         } else if (directive.equals("show_Syntax_Errors")) {
             if (AUTHORABLE) {
                 Vector pes = triceps.collectParseErrors();
                 if (pes == null || pes.size() == 0) {
                     info.append(triceps.get("no_syntax_errors_found"));
                 } else {
                     Vector syntaxErrors = new Vector();
                     syntaxErrors = pes;
                     for (int i = 0; i < syntaxErrors.size(); ++i) {
                         ParseError pe = (ParseError) syntaxErrors.elementAt(i);
                         Node n = pe.getNode();
 
                         if (i == 0) {
                             errors.append("<font color='red'>" +
                                 triceps.get("The_following_syntax_errors_were_found") + (n.getSourceFile()) + "</font>");
                             errors.append("<table cellpadding='2' cellspacing='1' width='100%' border='1'>");
                             errors.append("<tr><td>line#</td><td>name</td><td>Dependencies</td><td><b>Dependency Errors</b></td><td>Action Type</td><td>Action</td><td><b>Action Errors</b></td><td><b>Node Errors</b></td><td><b>Naming Errors</b></td><td><b>AnswerChoices Errors</b></td><td><b>Readback Errors</b></td></tr>");
                         }
 
                         errors.append("<tr><td>" + n.getSourceLine() + "</td><td>" + (n.getLocalName()) + "</td>");
                         errors.append("<td>" + n.getDependencies() + "</td><td>");
 
                         errors.append(pe.hasDependenciesErrors() ? ("<font color='red'>" + pe.getDependenciesErrors() + "</font>") : "&nbsp;");
                         errors.append("</td><td>" + Node.ACTION_TYPES[n.getQuestionOrEvalType()] + "</td><td>" + n.getQuestionOrEval() + "</td><td>");
 
                         errors.append(pe.hasQuestionOrEvalErrors() ? ("<font color='red'>" + pe.getQuestionOrEvalErrors() + "</font>") : "&nbsp;");
                         errors.append("</td><td>");
 
                         if (!pe.hasNodeParseErrors()) {
                             errors.append("&nbsp;");
                         } else {
                             errors.append("<font color='red'>" + pe.getNodeParseErrors() + "</font>");
                         }
                         errors.append("</td><td>");
 
                         if (!pe.hasNodeNamingErrors()) {
                             errors.append("&nbsp;");
                         } else {
                             errors.append("<font color='red'>" + pe.getNodeNamingErrors() + "</font>");
                         }
 
                         errors.append("<td>" + ((pe.hasAnswerChoicesErrors()) ? ("<font color='red'>" + pe.getAnswerChoicesErrors() + "</font>") : "&nbsp;") + "</td>");
                         errors.append("<td>" + ((pe.hasReadbackErrors()) ? ("<font color='red'>" + pe.getReadbackErrors() + "</font>") : "&nbsp;") + "</td>");
 
                         errors.append("</tr>");
                     }
                     errors.append("</table><hr>");
                 }
                 if (schedule.hasErrors()) {
                     errors.append("<font color='red'>" +
                         triceps.get("The_following_flow_errors_were_found") + "</font>");
                     errors.append("<table cellpadding='2' cellspacing='1' width='100%' border='1'><tr><td>");
                     errors.append("<font color='red'>" + schedule.getErrors() + "</font>");
                     errors.append("</td></tr></table>");
                 }
                 if (triceps.getEvidence().hasErrors()) {
                     errors.append("<font color='red'>" +
                         triceps.get("The_following_data_access_errors_were") + "</font>");
                     errors.append("<table cellpadding='2' cellspacing='1' width='100%' border='1'><tr><td>");
                     errors.append("<font color='red'>" + triceps.getEvidence().getErrors() + "</font>");
                     errors.append("</td></tr></table>");
                 }
             }
         } else if (directive.equals("next")) {
             // store current answer(s)
             Enumeration questionNames = triceps.getQuestions();
             // XXX For each question which was on a page, validate the entries and store the results.
             while (questionNames.hasMoreElements()) {
                 Node q = (Node) questionNames.nextElement();
                 boolean status;
                 String answer = requestParameters.get(q.getLocalName());
                 String comment = requestParameters.get(q.getLocalName() + "_COMMENT");
                 String special = requestParameters.get(q.getLocalName() + "_SPECIAL");
 
                 status = triceps.storeValue(q, answer, comment, special, (okPasswordForTempAdminMode || showAdminModeIcons));
                 ok = status && ok;
 
             }   // XXX Once all values stored (locally?)  Persist as a single database call?
             // goto next
             ok = ok && ((gotoMsg = triceps.gotoNext()) == Triceps.OK);	// don't proceed if prior errors - e.g. unanswered questions
 
             if (gotoMsg == Triceps.AT_END) {
                 directive = "finished";
                 return processDirective();
             }
 
         // don't goto next if errors
         // ask question
         } else if (directive.equals("finished")) {
             // save the file, but still give the option to go back and change answers
             String savedName = null;
 
             info.append(triceps.get("the_interview_is_completed"));
             if (DEPLOYABLE) {
                 savedName = triceps.saveCompletedInfo("");
                 if (savedName != null) {
                     if (!WEB_SERVER) {
                         info.append(triceps.get("interview_saved_successfully_as") + savedName);
                     }
 
                     savedName = triceps.copyCompletedToFloppy("");
                     if (savedName != null) {
                         if (!WEB_SERVER) {
                             info.append(triceps.get("interview_saved_successfully_as") + savedName);
                         }
                         triceps.deleteDataLoggers();	// only if saved both to completed and floppy
                     } else {
                         if (!WEB_SERVER) {
                             info.append(triceps.get("error_saving_data_to_floppy_dir"));
                         }
                     }
                 } else {
                     if (!WEB_SERVER) {
                         info.append(triceps.get("error_saving_data_to_completed_dir"));
                     }
                 }
             }
 
             if (triceps.hasErrors()) {
                 sb.append(triceps.getErrors());
             }
 
             String url = schedule.getReserved(Schedule.REDIRECT_ON_FINISH_URL);
             if (url.length() > 0) {
                 String msg = schedule.getReserved(Schedule.REDIRECT_ON_FINISH_MSG);
                 sb.append("<p></p><p><font size='+1'><b>");
                 sb.append(triceps.get("your_browser_will_be_redirected_to"));
                 sb.append(" <i>");
                 if (msg.length() > 0) {
                     sb.append(msg);
                 } else {
                     sb.append(url);
                 }
                 sb.append("</i> ");
                 sb.append(triceps.get("in_three_seconds"));
                 sb.append("<br/><br/>");
                 sb.append(triceps.get("or_you_can_click_here"));
                 sb.append("<a href='http://" + url + "'>http://" + url + "</a></b></font></p>\n");
             }
 
             return sb.toString();
         } else if (directive.equals("previous")) {
             // don't store current
             // goto previous
             gotoMsg = triceps.gotoPrevious();
             ok = ok && (gotoMsg == Triceps.OK);
         // ask question
         } else if (directive.equals("jumpToFirstUnasked")) {
             ok = ok && (triceps.jumpToFirstUnasked() == Triceps.OK);
         } else if (directive.equals("RESTORE_FROM_FLOPPY")) {
             // find all jar files in A:\suspended directory, copy them into working diretory; then re-fresh screen to make it available
             try {
                 String sourceDir;
                 File hd_suspend;
                 String[] list;
                 ok = false;
                 for (int d = 0; d < 2; ++d) {
                     if (d == 0) {
                         sourceDir = completedFilesDir + Triceps.SUSPEND_DIR;
                     } else {
                         sourceDir = floppyDir + Triceps.SUSPEND_DIR;
                     }
                     hd_suspend = new File(sourceDir);
                     list = hd_suspend.list();
                     if (list == null) {
                         continue;	// indicates empty directory
                     }
 
                     for (int i = 0; i < list.length; ++i) {
                         File file = new File(list[i]);
                         if (file.getName().toLowerCase().endsWith(".jar")) {
                             String name = file.getName();
                             JarWriter jw = new JarWriter();
                             ok = jw.copyFile(sourceDir + name, workingFilesDir + name);
                             if (jw.hasErrors()) {
                                 triceps.setError(jw.getErrors());
                             }
                             if (!ok) {
                                 triceps.setError(triceps.get("error_saving_data_to") + workingFilesDir + name);
                             }
                         }
                     }
                 }
             } catch (Exception e) {
                 errors.append(e.getMessage());
                 Logger.getLogger(LoggerName).log(Level.SEVERE, "", e);
             }
             // set directive so that reloads screen
             directive = null;
             return processDirective();
         } else if (directive.equals("suspendToFloppy")) {
             String savedName = triceps.suspendToFloppy();
             if (savedName != null) {
                 info.append(triceps.get("interview_saved_successfully_as") + savedName);
             } else {
                 info.append(triceps.get("error_saving_data_to_floppy_dir"));
             }
         }
 
         /* Show any accumulated errors */
         if (triceps.hasErrors()) {
             errors.append(triceps.getErrors());
         }
 
         nodes = triceps.getQuestions();
         int errCount = 0;
         while (nodes.hasMoreElements()) {
             Node n = (Node) nodes.nextElement();
             if (n.hasRuntimeErrors()) {
                 if (++errCount == 1) {
                     info.append(triceps.get("please_answer_the_questions_listed_in") + "<font color='red'>" + triceps.get("RED") + "</font>" + triceps.get("before_proceeding"));
                 }
                 if (n.focusableArray()) {
                     firstFocus = n.getLocalName() + "[0]";
                     break;
                 } else if (n.focusable()) {
                     firstFocus = n.getLocalName();
                     break;
                 }
             }
         }
 
         if (firstFocus == null) {
             nodes = triceps.getQuestions();
             while (nodes.hasMoreElements()) {
                 Node n = (Node) nodes.nextElement();
                 if (n.focusableArray()) {
                     firstFocus = n.getLocalName() + "[0]";
                     break;
                 } else if (n.focusable()) {
                     firstFocus = n.getLocalName();
                     break;
                 }
             }
         }
         if (firstFocus == null) {
             if (schedule.getBooleanReserved(Schedule.SET_DEFAULT_FOCUS)) {
                 firstFocus = "next";	// try to focus on Next button if nothing else available
             }
         }
 
         if (firstFocus != null) {
             firstFocus = (new XmlString(firstFocus)).toString();	// make sure properly formatted
         }
 
         sb.append(queryUser());
 
         return sb.toString();
     }
 
     /**
     Create a new context (Triceps), setting needed startup variables and loading instrument
      */
     boolean getNewTricepsInstance(String name,
                                   HashMap<String,String> requestParameters, boolean isRestore) {
         if (requestParameters != null) {
             this.requestParameters = requestParameters;
         }
 
         if (triceps != null) {
             triceps.closeDataLogger();
         }
         
         triceps = new Triceps(name, workingFilesDir, completedFilesDir, floppyDir, isRestore, requestParameters);
         if (triceps.hasErrors()) {
             errors.append(triceps.getErrors());
         }
         schedule = triceps.getSchedule();
         
 //        if (DB_LOG_FULL) {
 //            triceps.getDtc().setPerson(requestParameters.get("p"));
 //            triceps.getDtc().setStudy(requestParameters.get("s"));
 //            triceps.getDtc().setSubjectSession(requestParameters.get("ss"));
 //        }        
 
         if (!AUTHORABLE && !schedule.isLoaded()) {
         }
         schedule.setReserved(Schedule.IMAGE_FILES_DIR, imageFilesDir);
         schedule.setReserved(Schedule.SCHEDULE_DIR, scheduleSrcDir);
         schedule.setReserved(Schedule.BROWSER_TYPE, userAgent);
         schedule.setReserved(Schedule.IP_ADDRESS, ipAddress);
         schedule.setReserved(Schedule.CONNECTION_TYPE, ((requestParameters == null) ? null : (isSecure ? "HTTPS" : "HTTP")));
         String message = "***\t" + ipAddress + "\t" + userAgent + "\t" + ((requestParameters == null) ? "null" : (isSecure ? "HTTPS" : "HTTP"));
         Logger.getLogger(LoggerName).info(message);
         triceps.getEventLogger().println(message);
         return triceps.isValid();
     }
 
     /**
     Set additional reserved words
      */
     boolean setExtraParameters(String strStartingStep,
                                Hashtable mappings) {
         int startingStep = -1;
         Evidence evidence = triceps.getEvidence();
         boolean status = true;
 
         try {
             startingStep = Integer.parseInt(strStartingStep);
             if (startingStep >= 0) {
                 schedule.setReserved(Schedule.STARTING_STEP, strStartingStep);
             }
         } catch (Exception e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE, "", e);
         }
 
         if (mappings != null) {
             Enumeration keys = mappings.keys();
             while (keys.hasMoreElements()) {
                 String key = (String) keys.nextElement();
                 String value = (String) mappings.get(key);
 
                 Node node = evidence.getNode(key);
                 status = triceps.storeValue(node, value, "", "", true) && status;
             }
         }
         if (triceps.hasErrors()) {
             errors.append(triceps.getErrors());
         }
 
         return status;
     }
 
     /**
      * This method assembles the displayed question and answer options
      * and formats them in HTML for return to the client browser.
      */
     private String queryUser() {
         // if parser internal to Schedule, should have method access it, not directly
         StringBuffer sb = new StringBuffer();
 
         if (!triceps.isValid()) {
             return "";
         }
 
         if (debugMode && developerMode && AUTHORABLE) {
             sb.append(triceps.get("QUESTION_AREA"));
         }
 
         Enumeration questionNames = null;
         String color;
         String errMsg;
 
         /* pre-assess whether there are any special icons needed for the final column */
         boolean needSpecialOptions = false;
         boolean needQuestionNumColumn = false;
 
         questionNames = triceps.getQuestions();
         for (int count = 0; questionNames.hasMoreElements(); ++count) {
             Node node = (Node) questionNames.nextElement();
             Datum datum = triceps.getDatum(node);
             if (datum.isRefused() || datum.isUnknown() || datum.isNotUnderstood() || (node.getHelpURL() != null && node.getHelpURL().trim().length() > 0)) {
                 needSpecialOptions = true;
             }
             if (node.getExternalName().trim().length() > 0) {
                 needQuestionNumColumn = true;
             }
             if (node.getAnswerType() != Node.NOTHING) {
                 dlxObjects.add("{ id: '" + node.getLocalName() + "', type: '" + node.getJavascriptType() + "' }");
             }
         }
         if (!showQuestionNum) {
             needQuestionNumColumn = false;
         }
         if (okToShowAdminModeIcons || allowComments) {
             needSpecialOptions = true;
         }
 
         colpad = (needQuestionNumColumn ? 1 : 0) + (needSpecialOptions ? 1 : 0);
 
         questionNames = triceps.getQuestions();
 
         int answerOptionFieldWidth = 0;
         try {
             answerOptionFieldWidth = Integer.parseInt(schedule.getReserved(Schedule.ANSWER_OPTION_FIELD_WIDTH));
         } catch (Exception e) {
             Logger.getLogger(LoggerName).log(Level.SEVERE, "", e);
         }
 
         sb.append("<table cellpadding='2' cellspacing='1' width='100%' border='1'>");
         for (int count = 0; questionNames.hasMoreElements(); ++count) {
             Node node = (Node) questionNames.nextElement();
             Datum datum = triceps.getDatum(node);
 
             if (node.hasRuntimeErrors()) {
                 color = " color='red'";
                 StringBuffer errStr = new StringBuffer("<font color='red'>");
                 errStr.append(node.getRuntimeErrors());
                 errStr.append("</font>");
                 errMsg = errStr.toString();
             } else {
                 color = null;
                 errMsg = "";
             }
 
             sb.append("<tr>");
 
             if (needQuestionNumColumn) {
                 if (color != null) {
                     sb.append("<td><font" + color + "><b>" + node.getExternalName() + "</b></font></td>");
                 } else {
                     sb.append("<td>" + node.getExternalName() + "</td>");
                 }
             }
 
             String inputName = node.getLocalName();
 
             boolean isSpecial = (datum.isRefused() || datum.isUnknown() || datum.isNotUnderstood());
             allowEasyBypass = allowEasyBypass || isSpecial;	// if a value has already been refused, make it easy to re-refuse it
 
             String clickableOptions = buildClickableOptions(node, inputName, isSpecial);
 
             switch (node.getAnswerType()) {
                 case Node.RADIO_HORIZONTAL:
                     sb.append("<td colspan='" + (2 + (needSpecialOptions ? 1 : 0)) + "'>");
                     sb.append("<input type='hidden' name='" + (inputName + "_COMMENT") + "' id='" + (inputName + "_COMMENT") + "' value='" + (new XMLAttrEncoder()).encode(node.getComment()) + "'>");
                     sb.append("<input type='hidden' name='" + (inputName + "_SPECIAL") + "' id='" + (inputName + "_SPECIAL") + "' value='" +
                         ((isSpecial) ? (triceps.toString(node, true)) : "") +
                         "'>");
                     sb.append("<input type='hidden' name='" + (inputName + "_HELP") + "' id='" + (inputName + "_HELP") + "' value='" + node.getHelpURL() + "'>");
                     if (color != null) {
                         sb.append("<font" + color + ">" + triceps.getQuestionStr(node) + "</font>");
                     } else {
                         sb.append(triceps.getQuestionStr(node));
                     }
                     sb.append("</td></tr><tr>");
 
                     if (needQuestionNumColumn) {
                         sb.append("<td>&nbsp;</td>");
                     }
                     sb.append("<td colspan='2' bgcolor='lightgrey'>");
                     sb.append(errMsg);
                     sb.append(node.prepareChoicesAsHTML(datum, errMsg, autogenOptionNums));
                     sb.append("</td>");
                     if (needSpecialOptions) {
                         sb.append("<td width='1%'");
                         if (!wrapAdminIcons) {
                             sb.append(" NOWRAP");
                         }
                         sb.append(">");
                         sb.append(clickableOptions);
                         sb.append("</td>");
                     }
                     break;
                 default:
                     if (node.getAnswerType() == Node.NOTHING) {
                         sb.append("<td colspan='2'>");
                     } else {
                         sb.append("<td>");
                     }
                     sb.append("<input type='hidden' name='" + (inputName + "_COMMENT") + "' id='" + (inputName + "_COMMENT") + "' value='" + (new XMLAttrEncoder()).encode(node.getComment()) + "'>");
                     sb.append("<input type='hidden' name='" + (inputName + "_SPECIAL") + "' id='" + (inputName + "_SPECIAL") + "' value='" +
                         ((isSpecial) ? (triceps.toString(node, true)) : "") +
                         "'>");
                     sb.append("<input type='hidden' name='" + (inputName + "_HELP") + "' id='" + (inputName + "_HELP") + "' value='" + node.getHelpURL() + "'>");
                     if (color != null) {
                         sb.append("<font" + color + ">" + triceps.getQuestionStr(node) + "</font>");
                     } else {
                         sb.append(triceps.getQuestionStr(node));
                     }
                     sb.append("</td>");
                     if (node.getAnswerType() != Node.NOTHING) {
                         if (answerOptionFieldWidth > 0) {
                             sb.append("<td width='" + answerOptionFieldWidth + "%' NOWRAP>" + errMsg + node.prepareChoicesAsHTML(datum, autogenOptionNums) + "</td>");
                         } else {
                             sb.append("<td>" + errMsg + node.prepareChoicesAsHTML(datum, autogenOptionNums) + "</td>");
                         }
                     }
                     if (needSpecialOptions) {
                         sb.append("<td width='1%'");
                         if (!wrapAdminIcons) {
                             sb.append(" NOWRAP");
                         }
                         sb.append(">");
                         sb.append(clickableOptions);
                         sb.append("</td>");
                     }
                     break;
             }
 
             sb.append("</tr>");
         }
 
         sb.append("<tr><td colspan='" + (colpad + 2) + "' align='center'>");
 
         if (schedule.getBooleanReserved(Schedule.SWAP_NEXT_AND_PREVIOUS)) {
             if (!triceps.isAtBeginning()) {
                 sb.append(buildSubmit("previous"));
             }
             if (!triceps.isAtEnd()) {
                 sb.append(buildSubmit("next"));
             }
         } else {
             if (!triceps.isAtEnd()) {
                 sb.append(buildSubmit("next"));
             }
 
             if (!triceps.isAtBeginning()) {
                 sb.append(buildSubmit("previous"));
             }
         }
         if (allowJumpTo || (developerMode && AUTHORABLE)) {
             sb.append(buildSubmit("jump_to"));
             sb.append("<input type='text' name='jump_to_data' id='jump_to_data' size='10' " + ">");
         }
         if (schedule.getBooleanReserved(Schedule.JUMP_TO_FIRST_UNASKED)) {
             sb.append(buildSubmit("jumpToFirstUnasked"));
         }
         if (schedule.getBooleanReserved(Schedule.SUSPEND_TO_FLOPPY) || okToShowAdminModeIcons) {
             if (schedule.getBooleanReserved(Schedule.SUSPEND_TO_FLOPPY)) {
                 sb.append(buildSubmit("suspendToFloppy"));
             } else {
                 if (okToShowAdminModeIcons) {
                     if (showSaveToFloppyInAdminMode) {
                         sb.append(buildSubmit("suspendToFloppy"));
                     }
                 }
             }
         }
 
         if (allowEasyBypass || okToShowAdminModeIcons) {
             /* enables TEMP_ADMIN_MODE going forward for one screen */
             sb.append("<input type='hidden' name='TEMP_ADMIN_MODE_PASSWORD' id='TEMP_ADMIN_MODE_PASSWORD' value='" + triceps.createTempPassword() + "'>");
         }
 
         sb.append("</td></tr>");
 
         if (AUTHORABLE) {
             if (developerMode) {
                 sb.append("<tr><td colspan='" + (colpad + 2) + "' align='center'>");
                 sb.append(buildSubmit("select_new_interview"));
                 sb.append(buildSubmit("restart_clean"));
                 sb.append(buildSubmit("save_to"));
                 sb.append("<input type='text' name='save_to_data' id='save_to_data' size='10' " + ">");
                 sb.append("</td></tr>");
                 sb.append("<tr><td colspan='" + (colpad + 2) + "' align='center'>");
                 sb.append(buildSubmit("reload_questions"));
                 sb.append(buildSubmit("show_Syntax_Errors"));
                 sb.append(buildSubmit("evaluate_expr"));
                 sb.append("<input type='text' name='evaluate_expr_data' id='evaluate_expr_data' " + ">");
                 sb.append("</td></tr>");
             }
         }
 
         sb.append(showOptions());
 
         sb.append("</table>");
 
         if (DB_LOG_MINIMAL || DB_LOG_FULL) {
             questionNames = triceps.getQuestions();
             for (int count = 0; questionNames.hasMoreElements(); ++count) {
                 Node q = (Node) questionNames.nextElement();
                 Datum d = triceps.getDatum(q);
 
                 if (q == null || d == null) {
                     continue;
                 }
                 String questionAsAsked = (new InputEncoder()).encode(q.getQuestionAsAsked());
                 String varNameString = q.getLocalName();
                 Date timestamp = q.getTimeStamp();
 
                 if (DB_LOG_FULL) {
                     triceps.getDtc().writeNodePreAsking(varNameString, questionAsAsked, timestamp);
                 }
             }
         }
 
         return sb.toString();
     }
 
     /**
     Helper to create HTML submit button for each visible reserved action button
      */
     private String buildSubmit(String name) {
         StringBuffer sb = new StringBuffer();
 
         sb.append("<input type='submit' name='");
         sb.append(name);
         sb.append("' id='").append(name);
         sb.append("' value='");
         sb.append(inactivePrefix);
         sb.append(triceps.get(name));
         sb.append(inactiveSuffix);
         sb.append("'>");
 
         sb.append("<input type='hidden' name='DIRECTIVE_");
         sb.append(name);
         sb.append("' id='DIRECTIVE_").append(name);
         sb.append("' value='");
         sb.append(triceps.get(name));
         sb.append("'>");
 
         dlxObjects.add("{ id: '" + name + "', type: 'submit' }");
 
         return sb.toString();
     }
 
     /**
     Helper to create HTML for clickable/changeable icons for each nullFlavor or Comment
      */
     private String buildClickableOptions(Node node,
                                           String inputName,
                                           boolean isSpecial) {
         StringBuffer sb = new StringBuffer();
 
         if (!triceps.isValid()) {
             return "";
         }
 
         Datum datum = triceps.getDatum(node);
 
         if (datum == null) {
             return "&nbsp;";
         }
 
         boolean isRefused = false;
         boolean isUnknown = false;
         boolean isNotUnderstood = false;
 
         if (datum.isRefused()) {
             isRefused = true;
         } else if (datum.isUnknown()) {
             isUnknown = true;
         } else if (datum.isNotUnderstood()) {
             isNotUnderstood = true;
         }
 
         String localHelpURL = node.getHelpURL();
         if (localHelpURL != null && localHelpURL.trim().length() != 0) {
             sb.append("<img src='" + getIcon(Schedule.HELP_ICON) +
                 "' align='top' border='0' alt='" + triceps.get("Help") + "' onmouseup='help(\"" + inputName + "\",\"" + localHelpURL + "\");'>");
         } else {
         // don't show help icon if no help is available?
         }
 
         String comment = node.getComment();
         if ((showAdminModeIcons || okToShowAdminModeIcons || allowComments) && (!disallowComments || (comment != null && comment.trim().length() != 0))) {
             if (comment != null && comment.trim().length() != 0) {
                 sb.append("<img name='" + inputName + "_COMMENT_ICON" + "' id='" + inputName + "_COMMENT_ICON" + "' src='" + getIcon(Schedule.COMMENT_ICON_ON) +
                     "' align='top' border='0' alt='" + triceps.get("Add_a_Comment") + "' onmouseup='comment(\"" + inputName + "\");'>");
             } else {
                 sb.append("<img name='" + inputName + "_COMMENT_ICON" + "' id='" + inputName + "_COMMENT_ICON" + "' src='" + getIcon(Schedule.COMMENT_ICON_OFF) +
                     "' align='top' border='0' alt='" + triceps.get("Add_a_Comment") + "' onmouseup='comment(\"" + inputName + "\");'>");
             }
         }
 
         /* If something has been set as Refused, Unknown, etc, allow going forward without additional headache */
         /* Don't want to be able to refuse Nothing nodes, since can be used to prevent advancement */
 
         if (!(node.getAnswerType() == Node.NOTHING) && (showAdminModeIcons || okToShowAdminModeIcons || isSpecial)) {
             if (allowRefused || isRefused) {
                 sb.append("<img name='" + inputName + "_REFUSED_ICON" + "' id='" + inputName + "_REFUSED_ICON" + "' src='" + ((isRefused) ? getIcon(Schedule.REFUSED_ICON_ON) : getIcon(Schedule.REFUSED_ICON_OFF)) +
                     "' align='top' border='0' alt='" + triceps.get("Set_as_Refused") + "' onmouseup='markAsRefused(\"" + inputName + "\");'>");
             }
             if (allowUnknown || isUnknown) {
                 sb.append("<img name='" + inputName + "_UNKNOWN_ICON" + "' id='" + inputName + "_UNKNOWN_ICON" + "' src='" + ((isUnknown) ? getIcon(Schedule.UNKNOWN_ICON_ON) : getIcon(Schedule.UNKNOWN_ICON_OFF)) +
                     "' align='top' border='0' alt='" + triceps.get("Set_as_Unknown") + "' onmouseup='markAsUnknown(\"" + inputName + "\");'>");
             }
             if (allowNotUnderstood || isNotUnderstood) {
                 sb.append("<img name='" + inputName + "_NOT_UNDERSTOOD_ICON" + "' id='" + inputName + "_NOT_UNDERSTOOD_ICON" + "' src='" + ((isNotUnderstood) ? getIcon(Schedule.DONT_UNDERSTAND_ICON_ON) : getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF)) +
                     "' align='top' border='0' alt='" + triceps.get("Set_as_Not_Understood") + "' onmouseup='markAsNotUnderstood(\"" + inputName + "\");'>");
             }
         }
 
         if (sb.length() == 0) {
             return "&nbsp;";
         } else {
             return sb.toString();
         }
     }
 
     /**
     Helper to create DebugMode display of current status and data
      */
     private String generateDebugInfo() {
         StringBuffer sb = new StringBuffer();
         if (AUTHORABLE) {
             // Complete printout of what's been collected per node
             if (!triceps.isValid()) {
                 return "";
             }
 
             if (developerMode && debugMode) {
                 sb.append("<hr>");
                 sb.append(triceps.get("CURRENT_QUESTIONS"));
                 sb.append("<table cellpadding='2' cellspacing='1'  width='100%' border='1'>");
                 Enumeration questionNames = triceps.getQuestions();
                 Evidence evidence = triceps.getEvidence();
 
                 while (questionNames.hasMoreElements()) {
                     Node n = (Node) questionNames.nextElement();
                     Datum d = evidence.getDatum(n);
                     sb.append("<tr>");
                     sb.append("<td>" + n.getExternalName() + "</td>");
                     if (d.isSpecial()) {
                         sb.append("<td><b><i>" + triceps.toString(n, true) + "</i></b></td>");
                     } else {
                         sb.append("<td><b>" + triceps.toString(n, true) + "</b></td>");
                     }
                     sb.append("<td>" + Datum.getTypeName(triceps, n.getDatumType()) + "</td>");
                     sb.append("<td>" + n.getLocalName() + "</td>");
                     sb.append("<td>" + n.getConcept() + "</td>");
                     sb.append("<td>" + n.getDependencies() + "</td>");
                     sb.append("<td>" + n.getQuestionOrEvalTypeField() + "</td>");
                     sb.append("<td>" + n.getQuestionOrEval() + "</td>");
                     sb.append("</tr>");
                 }
                 sb.append("</table>");
 
 
                 sb.append("<hr>");
                 sb.append(triceps.get("EVIDENCE_AREA"));
                 sb.append("<table cellpadding='2' cellspacing='1'  width='100%' border='1'>");
 
                 for (int i = schedule.size() - 1; i >= 0; i--) {
                     Node n = schedule.getNode(i);
                     Datum d = triceps.getDatum(n);
                     if (!triceps.isSet(n)) {
                         continue;
                     }
                     sb.append("<tr>");
                     sb.append("<td>" + (i + 1) + "</td>");
                     sb.append("<td>" + n.getExternalName() + "</td>");
                     if (d.isSpecial()) {
                         sb.append("<td><b><i>" + triceps.toString(n, true) + "</i></b></td>");
                     } else {
                         sb.append("<td><b>" + triceps.toString(n, true) + "</b></td>");
                     }
                     sb.append("<td>" + Datum.getTypeName(triceps, n.getDatumType()) + "</td>");
                     sb.append("<td>" + n.getLocalName() + "</td>");
                     sb.append("<td>" + n.getConcept() + "</td>");
                     sb.append("<td>" + n.getDependencies() + "</td>");
                     sb.append("<td>" + n.getQuestionOrEvalTypeField() + "</td>");
                     sb.append("<td>" + n.getQuestionOrEval(triceps.getLanguage()) + "</td>");
                     sb.append("</tr>");
                 }
                 sb.append("</table>");
             }
         }
         return sb.toString();
     }
 
     /**
     Helper to build HTML for all active development buttons
      */
     private String showOptions() {
         if (AUTHORABLE) {
             if (developerMode) {
                 StringBuffer sb = new StringBuffer();
 
                 sb.append("<tr><td colspan='" + (colpad + 2) + "' align='center'>");
                 sb.append(buildSubmit("turn_developerMode"));
                 sb.append(buildSubmit("turn_debugMode"));
                 sb.append(buildSubmit("turn_showQuestionNum"));
                 sb.append(buildSubmit("sign_schedule"));
                 sb.append(buildSubmit("toggle_EventCollection"));
                 sb.append("</td></tr>");
                 return sb.toString();
             } else {
                 return "";
             }
         } else {
             return "";
         }
     }
 
     private StringBuffer createReusableJavascript() {
         StringBuffer sb = new StringBuffer();
 
         sb.append("var now=null;\n");
         sb.append("var val=null;\n");
         sb.append("var msg=null;\n");
         sb.append("var target=null;\n");
         sb.append("var targetType=null;\n");
         sb.append("var targetName=null;\n");
         sb.append("var targetText=null;\n");
         sb.append("var startTime=new Date();\n");
         sb.append("var loadTime=null;\n");
         sb.append("\n");
         sb.append("function addListener(towhom) {\n");
         sb.append("	if (!document.getElementById) {\n");
         sb.append("		return;\n");
         sb.append("	}\n");
         sb.append("	\n");
         sb.append("	tome = document.getElementById(towhom.id);\n");
         sb.append("	tome._dlxType = towhom.type;\n");
         sb.append("	tome._dlxName = towhom.id;\n");
         sb.append("		\n");
         sb.append("	if (window.addEventListener) { //DOM method for binding an event\n");
         sb.append("		tome.addEventListener('blur', eventHandler, false);\n");
         sb.append("		tome.addEventListener('change', eventHandler, false);\n");
         sb.append("		tome.addEventListener('click', eventHandler, false);\n");
         sb.append("		tome.addEventListener('focus', eventHandler, false);\n");
         sb.append("		tome.addEventListener('load', eventHandler, false);\n");
         sb.append("		tome.addEventListener('mouseup', eventHandler, false);\n");
         sb.append("		tome.addEventListener('submit', eventHandler, false);\n");
         sb.append("	}\n");
         sb.append("	else if (window.attachEvent) { //IE exclusive method for binding an event\n");
         sb.append("		tome.attachEvent('onblur', eventHandler);\n");
         sb.append("		tome.attachEvent('onchange', eventHandler);\n");
         sb.append("		tome.attachEvent('onclick', eventHandler);\n");
         sb.append("		tome.attachEvent('onfocus', eventHandler);\n");
         sb.append("		tome.attachEvent('onload', eventHandler);\n");
         sb.append("		tome.attachEvent('onmouseup', eventHandler);\n");
         sb.append("		tome.attachEvent('onsubmit', eventHandler);\n");
         sb.append("	}\n");
         sb.append("	else {\n");
         sb.append("		tome.onblur=eventHandler;\n");
         sb.append("		tome.onchange=eventHandler;\n");
         sb.append("		tome.onclick=eventHandler;\n");
         sb.append("		tome.onfocus=eventHandler;\n");
         sb.append("		tome.onload=eventHandler;\n");
         sb.append("		tome.onmouseup=eventHandler;\n");
         sb.append("		tome.onsubmit=eventHandler;\n");
         sb.append("	}\n");
         sb.append("}\n");
         sb.append("\n");
         sb.append("\n");
         sb.append("function init() {\n");
         sb.append("	for (i =0; i< _dlxObjects.length; ++i) {\n");
         sb.append("		addListener(_dlxObjects[i]);\n");
         sb.append("	}\n");
         sb.append("	loadTime = new Date();\n");
         sb.append("}\n");
         sb.append("\n");
         sb.append("function eventHandler(evt) {\n");
         sb.append("	now = new Date();\n");
         sb.append("	target = (evt.target || evt.srcElement);\n");
         sb.append("	if (target) {\n");
         sb.append("		targetType = target._dlxType;\n");
         sb.append("		targetName = target._dlxName;\n");
         sb.append("		targetText = target.text;\n");
         sb.append("		if (target.options && target.selectedIndex && target.options[target.selectedIndex] && target.options[target.selectedIndex].text) {\n");
         sb.append("			targetText = target.options[target.selectedIndex].text;\n");
         sb.append("		}\n");
         sb.append("	}\n");
         sb.append("	\n");
         sb.append("	if (evt && evt.type) {\n");
         sb.append("		switch (evt.type) {\n");
         sb.append("			default: {\n");
         sb.append("  			msg = targetName + ',Default-' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("  			break;\n");
         sb.append("  		}\n");
         sb.append("		  case ('blur'): { \n");
         sb.append("		  	switch (targetType) {\n");
         sb.append("		  		case ('button'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('option'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('checkbox'): {\n");
         sb.append("                                     if (target.checked) { val = target.value; } else { val = 'null'; }\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + val + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('radio'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('select-one'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('submit'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			var name = document.dialogixForm.elements['DIRECTIVE_' + targetName].value;\n");
         sb.append("		  			target.value='    ' + name + '    ';\n");
         sb.append("		  			document.dialogixForm.DIRECTIVE.value = targetName;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('text'): case ('password'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('textarea'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}		  		\n");
         sb.append("		  		default: {\n");
         sb.append("		  			msg = targetName + ',default-' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  	}\n");
         sb.append("		  	break;\n");
         sb.append("		  	}								\n");
         sb.append("		  case ('change'): { \n");
         sb.append("		  	switch (targetType) {\n");
         sb.append("		  		case ('button'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('option'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('checkbox'): {\n");
         sb.append("                                     if (target.checked) { val = target.value; } else { val = 'null'; }\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + val + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('radio'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('select-one'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('submit'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			document.dialogixForm.DIRECTIVE.value = targetName;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('text'): case ('password'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('textarea'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}		  		\n");
         sb.append("		  		default: {\n");
         sb.append("		  			msg = targetName + ',default-' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  	}\n");
         sb.append("		  		break;		  	\n");
         sb.append("		  	}						\n");
         sb.append("		  case ('click'): { \n");
         sb.append("		  	switch (targetType) {\n");
         sb.append("		  		case ('button'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('option'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('radio'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('checkbox'): {\n");
         sb.append("                                     if (target.checked) { val = target.value; } else { val = 'null'; }\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + val + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('select-one'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('submit'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			document.dialogixForm.DIRECTIVE.value = targetName;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('text'): case ('password'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("	  			case ('textarea'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}		  		\n");
         sb.append("		  		default: {\n");
         sb.append("		  			msg = targetName + ',default-' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  	}\n");
         sb.append("				break;		  	\n");
         sb.append("		  	}													\n");
         sb.append("		  case ('focus'): { \n");
         sb.append("		  	switch (targetType) {\n");
         sb.append("		  		case ('button'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('option'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('radio'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('checkbox'): {\n");
         sb.append("                                     if (target.checked) { val = target.value; } else { val = 'null'; }\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + val + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('select-one'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('submit'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			var name = document.dialogixForm.elements['DIRECTIVE_' + targetName].value;\n");
         sb.append("		  			if (targetName == 'next') {\n");
         sb.append("		  				target.value='    ' + name + '>>';\n");
         sb.append("		  			}\n");
         sb.append("		  			else if (targetName == 'previous') {\n");
         sb.append("		  				target.value='<<' + name + '    ';\n");
         sb.append("		  			}\n");
         sb.append("		  			else {\n");
         sb.append("		  				target.value='<<' + name + '>>';\n");
         sb.append("		  			}\n");
         sb.append("		  			document.dialogixForm.DIRECTIVE.value = targetName;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('text'): case ('password'): {\n");
         sb.append("		  			target.select();\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('textarea'): {\n");
         sb.append("		  			target.select();\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}		  		\n");
         sb.append("		  		default: {\n");
         sb.append("		  			msg = targetName + ',default-' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  	}\n");
         sb.append("				break;		  	\n");
         sb.append("		  	}													\n");
         sb.append("		  case ('keyup'): { \n");
         sb.append("		  	switch (targetType) {\n");
         sb.append("		  		case ('button'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('option'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('radio'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('checkbox'): {\n");
         sb.append("                                     if (target.checked) { val = target.value; } else { val = 'null'; }\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + val + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('select-one'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('submit'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			document.dialogixForm.DIRECTIVE.value = targetName;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('text'): case ('password'): {\n");
         sb.append("		  			var val = null;\n");
         sb.append("		  			if (evt.keyCode) {\n");
         sb.append("		  				val = String.fromCharCode(evt.keyCode);\n");
         sb.append("		  			}\n");
         sb.append("		  			else if (evt.which) {\n");
         sb.append("		  				val = String.fromCharCode(evt.which);\n");
         sb.append("		  			}\n");
         sb.append("		  			else {\n");
         sb.append("		  				val = 'null';\n");
         sb.append("		  			}\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + val + ',' + target.value;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('textarea'): {\n");
         sb.append("		  			var val = null;\n");
         sb.append("		  			if (evt.keyCode) {\n");
         sb.append("		  				val = String.fromCharCode(evt.keyCode);\n");
         sb.append("		  			}\n");
         sb.append("		  			else if (evt.which) {\n");
         sb.append("		  				val = String.fromCharCode(evt.which);\n");
         sb.append("		  			}\n");
         sb.append("		  			else {\n");
         sb.append("		  				val = 'null';\n");
         sb.append("		  			}\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + val + ',' + target.value;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}		  		\n");
         sb.append("		  		default: {\n");
         sb.append("		  			msg = targetName + ',default-' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  	}\n");
         sb.append("				break;		  	\n");
         sb.append("		  	}													\n");
         sb.append("		  case ('load'): { \n");
         sb.append("		  	msg = 'load' + ',' + evt.type + ',' + 'load' + ',' + now.getTime() + ',' + (now.getTime() - startTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("				break;		  	\n");
         sb.append("		  	}													\n");
         sb.append("		  case ('mouseup'): { \n");
         sb.append("		  	switch (targetType) {\n");
         sb.append("		  		case ('button'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('option'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('radio'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('checkbox'): {\n");
         sb.append("                                     if (target.checked) { val = target.value; } else { val = 'null'; }\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + val + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('select-one'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('submit'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			document.dialogixForm.DIRECTIVE.value = targetName;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('text'): case ('password'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('textarea'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}		  		\n");
         sb.append("		  		default: {\n");
         sb.append("		  			msg = targetName + ',default-' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  	}\n");
         sb.append("				break;		  	\n");
         sb.append("		  	}													\n");
         sb.append("		  case ('submit'): { \n");
         sb.append("		  	switch (targetType) {\n");
         sb.append("		  		case ('button'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('option'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('radio'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('checkbox'): {\n");
         sb.append("                                     if (target.checked) { val = target.value; } else { val = 'null'; }\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + val + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('select-one'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + targetText;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('submit'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			document.dialogixForm.DIRECTIVE.value = targetName;\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('text'): case ('password'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  		case ('textarea'): {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + target.value + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}		  		\n");
         sb.append("		  		default: {\n");
         sb.append("		  			msg = targetName + ',' + evt.type + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("		  			break;\n");
         sb.append("		  		}\n");
         sb.append("		  	}\n");
         sb.append("				break;		  	\n");
         sb.append("		  	}														\n");
         sb.append("  	}\n");
         sb.append("	}\n");
         sb.append("	else {\n");
         sb.append("		msg = targetName + ',' + 'null' + ',' + targetType + ',' + now.getTime() + ',' + (now.getTime() - loadTime.getTime()) + ',' + 'null' + ',' + 'null';\n");
         sb.append("	}\n");
         sb.append("	msg += '	';\n");
         sb.append("	document.dialogixForm.EVENT_TIMINGS.value += msg;\n");
         sb.append("	return true;\n");
         sb.append("		\n");
         sb.append("}	\n");
 
         return sb;
     }
 
     /**
     Helper to create HTML for Javascript which logs events to EVENT_TIMINGS
      */
     private String createJavaScript() {
         StringBuffer sb = new StringBuffer();
 //                sb.append("<script  type=\"text/javascript\" src=\"DialogixJavascript.js\" charset=\"utf-8\"></script>\n");
         sb.append("<script  type=\"text/javascript\"> \n<!--\n");
 
         if (dlxObjects.size() > 0) {
             sb.append("var _dlxObjects = [\n");
             for (int i = 0; i < dlxObjects.size(); ++i) {
                 if (i > 0) {
                     sb.append(",\n");
                 }
                 sb.append(dlxObjects.get(i));
             }
             sb.append("\n];\n");
         }
 
         sb.append(createReusableJavascript());
 
         sb.append("function setAdminModePassword(name) {\n");
         sb.append("	ans = prompt('").append(triceps.get("Enter_password_for_Administrative_Mode")).append("','');\n");
         sb.append("	if (ans === null || ans === '') { return; } \n");
         sb.append("	document.dialogixForm.PASSWORD_FOR_ADMIN_MODE.value = ans;\n");
         sb.append("	document.dialogixForm.submit();\n");
         sb.append("\n");
         sb.append("}\n");
         sb.append("\n");
         sb.append("function markAsRefused(name) {\n");
         sb.append("	if (name === null) { return; }\n");
         sb.append("	val = document.dialogixForm.elements[name + '_SPECIAL'];\n");
         sb.append("	if (val.value === '").append(Datum.getSpecialName(Datum.REFUSED)).append("') {\n");
         sb.append("		val.value = '';\n");
         sb.append("		if (document.getElementById) { \n");
         sb.append("			document.getElementById(name + '_REFUSED_ICON').src='").append(getIcon(Schedule.REFUSED_ICON_OFF)).append("';\n");
         sb.append("		} else {\n");
         sb.append("			document.dialogixForm.elements[name + '_REFUSED_ICON'].src='").append(getIcon(Schedule.REFUSED_ICON_OFF)).append("';\n");
         sb.append("		}\n");
         sb.append("	} else {\n");
         sb.append("		val.value = '").append(Datum.getSpecialName(Datum.REFUSED)).append("';\n");
         sb.append("		if (document.getElementById) { \n");
         sb.append("			document.getElementById(name + '_REFUSED_ICON').src='").append(getIcon(Schedule.REFUSED_ICON_ON)).append("';\n");
         sb.append("			document.getElementById(name + '_UNKNOWN_ICON').src='").append(getIcon(Schedule.UNKNOWN_ICON_OFF)).append("';\n");
         sb.append("			document.getElementById(name + '_NOT_UNDERSTOOD_ICON').src='").append(getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF)).append("';\n");
         sb.append("		} else {\n");
         sb.append("			document.dialogixForm.elements[name + '_REFUSED_ICON'].src='").append(getIcon(Schedule.REFUSED_ICON_ON)).append("';\n");
         sb.append("			document.dialogixForm.elements[name + '_UNKNOWN_ICON'].src='").append(getIcon(Schedule.UNKNOWN_ICON_OFF)).append("';\n");
         sb.append("			document.dialogixForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src='").append(getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF)).append("';\n");
         sb.append("		}\n");
         sb.append("	}\n");
         sb.append("}\n");
         sb.append("\n");
         sb.append("function markAsUnknown(name) {\n");
         sb.append("	if (name === null) { return; }\n");
         sb.append("	val = document.dialogixForm.elements[name + '_SPECIAL'];\n");
         sb.append("	if (val.value === '").append(Datum.getSpecialName(Datum.UNKNOWN)).append("') {\n");
         sb.append("		val.value = '';\n");
         sb.append("		if (document.getElementById) { \n");
         sb.append("			document.getElementById(name + '_UNKNOWN_ICON').src='").append(getIcon(Schedule.UNKNOWN_ICON_OFF)).append("';\n");
         sb.append("		} else {\n");
         sb.append("			document.dialogixForm.elements[name + '_UNKNOWN_ICON'].src='").append(getIcon(Schedule.UNKNOWN_ICON_OFF)).append("';\n");
         sb.append("		}\n");
         sb.append("	} else {\n");
         sb.append("		val.value = '").append(Datum.getSpecialName(Datum.UNKNOWN)).append("';\n");
         sb.append("		if (document.getElementById) { \n");
         sb.append("			document.getElementById(name + '_REFUSED_ICON').src='").append(getIcon(Schedule.REFUSED_ICON_OFF)).append("';\n");
         sb.append("			document.getElementById(name + '_UNKNOWN_ICON').src='").append(getIcon(Schedule.UNKNOWN_ICON_ON)).append("';\n");
         sb.append("			document.getElementById(name + '_NOT_UNDERSTOOD_ICON').src='").append(getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF)).append("';\n");
         sb.append("		}\n");
         sb.append("		else {\n");
         sb.append("			document.dialogixForm.elements[name + '_REFUSED_ICON'].src='").append(getIcon(Schedule.REFUSED_ICON_OFF)).append("';\n");
         sb.append("			document.dialogixForm.elements[name + '_UNKNOWN_ICON'].src='").append(getIcon(Schedule.UNKNOWN_ICON_ON)).append("';\n");
         sb.append("			document.dialogixForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src='").append(getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF)).append("';\n");
         sb.append("		}\n");
         sb.append("	}\n");
         sb.append("}\n");
         sb.append("\n");
         sb.append("function markAsNotUnderstood(name) {\n");
         sb.append("	if (name === null) { return; }\n");
         sb.append("	val = document.dialogixForm.elements[name + '_SPECIAL'];\n");
         sb.append("	if (val.value === '").append(Datum.getSpecialName(Datum.NOT_UNDERSTOOD)).append("') {\n");
         sb.append("		val.value = '';\n");
         sb.append("		if (document.getElementById) { \n");
         sb.append("			document.getElementById(name + '_NOT_UNDERSTOOD_ICON').src='").append(getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF)).append("';\n");
         sb.append("		} else {\n");
         sb.append("			document.dialogixForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src='").append(getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF)).append("';\n");
         sb.append("		}\n");
         sb.append("	} else {\n");
         sb.append("		val.value = '").append(Datum.getSpecialName(Datum.NOT_UNDERSTOOD)).append("';\n");
         sb.append("		if (document.getElementById) { \n");
         sb.append("			document.getElementById(name + '_REFUSED_ICON').src='").append(getIcon(Schedule.REFUSED_ICON_OFF)).append("';\n");
         sb.append("			document.getElementById(name + '_UNKNOWN_ICON').src='").append(getIcon(Schedule.UNKNOWN_ICON_OFF)).append("';\n");
         sb.append("			document.getElementById(name + '_NOT_UNDERSTOOD_ICON').src='").append(getIcon(Schedule.DONT_UNDERSTAND_ICON_ON)).append("';\n");
         sb.append("		} else {\n");
         sb.append("			document.dialogixForm.elements[name + '_REFUSED_ICON'].src='").append(getIcon(Schedule.REFUSED_ICON_OFF)).append("';\n");
         sb.append("			document.dialogixForm.elements[name + '_UNKNOWN_ICON'].src='").append(getIcon(Schedule.UNKNOWN_ICON_OFF)).append("';\n");
         sb.append("			document.dialogixForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src='").append(getIcon(Schedule.DONT_UNDERSTAND_ICON_ON)).append("';\n");
         sb.append("		}\n");
         sb.append("	}\n");
         sb.append("}\n");
         sb.append("\n");
         sb.append("function help(nam,targ) {\n");
         sb.append("	if (targ !== null && targ.length !== 0) { window.open(targ,'__HELP__'); }\n");
         sb.append("}\n");
         sb.append("\n");
         sb.append("function comment(name) {\n");
         sb.append("	if (name === null) { return; }\n");
         sb.append("	ans = prompt('Enter a comment for this question  ',document.dialogixForm.elements[name + '_COMMENT'].value);\n");
         sb.append("	if (ans === null) { return;}\n");
         sb.append("	document.dialogixForm.elements[name + '_COMMENT'].value = ans;\n");
         sb.append("	if (ans !== null && ans.length > 0) {\n");
         sb.append("		if (document.getElementById) { \n");
         sb.append("			document.getElementById(name + '_COMMENT_ICON').src='").append(getIcon(Schedule.COMMENT_ICON_ON)).append("';\n");
         sb.append("		} else {\n");
         sb.append("			document.dialogixForm.elements[name + '_COMMENT_ICON'].src='").append(getIcon(Schedule.COMMENT_ICON_ON)).append("';\n");
         sb.append("		}\n");
         sb.append("	} else { \n");
         sb.append("		if (document.getElementById) { \n");
         sb.append("			document.getElementById(name + '_COMMENT_ICON').src='").append(getIcon(Schedule.COMMENT_ICON_OFF)).append("'; \n");
         sb.append("		} else {\n");
         sb.append("			document.dialogixForm.elements[name + '_COMMENT_ICON'].src='").append(getIcon(Schedule.COMMENT_ICON_OFF)).append("'; \n");
         sb.append("		}\n");
         sb.append("	}\n");
         sb.append("}\n");
         sb.append("\n");
         sb.append("function setLanguage(lang) {\n");
         sb.append("	document.dialogixForm.LANGUAGE.value = lang;\n");
         sb.append("	document.dialogixForm.submit();\n");
         sb.append("}\n");
         sb.append("// --> </script>\n");
 
         return sb.toString();
     }
 
     /**
     Helper to create HTML headers
      */
     private String header() {
         StringBuffer sb = new StringBuffer();
         String title = null;
 
         if (isSplashScreen || !triceps.isValid()) {
             title = VERSION_NAME;
         } else {
             title = triceps.getTitle();
         }
 
         sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");
         sb.append("<html DIR='" + triceps.getLocaleDirectionality() + "'>\n");
         sb.append("<head>\n");
         sb.append("<META HTTP-EQUIV='Content-Type' CONTENT='" + CONTENT_TYPE + "'>\n");
         if ("finished".equals(directive) && schedule != null) {
             String s = schedule.getReserved(Schedule.REDIRECT_ON_FINISH_URL);
             String delay = schedule.getReserved(Schedule.REDIRECT_ON_FINISH_DELAY);
             if (s.length() > 0) {
                 sb.append("<META HTTP-EQUIV='refresh' CONTENT='" + delay + ";url=http://" + s + "'>\n");
             }
         }
 
         sb.append("<title>" + title + "</title>\n");
 
         if (!"finished".equals(directive)) {
             sb.append(createJavaScript());
         }
 
         sb.append("</head>\n");
         sb.append("<body bgcolor='white'");
         if (!"finished".equals(directive)) {
             sb.append(" onload='init()");
         }
         if (firstFocus != null) {
             sb.append(";document.dialogixForm." + firstFocus + ".focus()");
         }
         sb.append("'>");
 
         return sb.toString();
     }
 
     /**
     Is instrument already completed?
      */
     public boolean isFinished() {
         return (!isActive);
     }
 
     /**
     Get absolute path name for a file
      */
     String getCanonicalPath(String which) {
         if (which == null || which.trim().equals("")) {
             return null;
         }
         if (which.matches("^\\d+$")) {
             return which;
         }
         String s = which.replace('\\', '/');	// use unix separators
 
         if (s.indexOf(dialogix_dir) == -1) {
             return dialogix_dir + s;	// assumes proper separator characters, and that all schedule names converted properly
         } else {
             return s;	// already includes full path
         }
     }
 
     public Triceps getTriceps() {
         return triceps;
     }
     // added by Gary Lyons 12/12/06 for memory leak fix
     boolean releaseTriceps() {
         if (triceps != null) {
             triceps = null;
 
             return true;
         } else {
             return false;
         }
     }
        
 }
