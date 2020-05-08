 package org.makumba.parade.access;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.util.logging.LogRecord;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.spi.LoggingEvent;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.aether.AetherEvent;
 import org.makumba.commons.StringUtils;
 import org.makumba.parade.aether.ActionTypes;
 import org.makumba.parade.aether.ObjectTypes;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.init.ParadeProperties;
 import org.makumba.parade.init.RowProperties;
 import org.makumba.parade.model.ActionLog;
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.Log;
 import org.makumba.parade.model.User;
 import org.makumba.parade.tools.PerThreadPrintStreamLogRecord;
 import org.makumba.parade.tools.TriggerFilter;
 import org.makumba.parade.view.TickerTapeData;
 import org.makumba.parade.view.TickerTapeServlet;
 
 /**
  * This servlet makes it possible to log events from various sources into the database. It persists two kinds of logs:
  * <ul>
  * <li>ActionLogs, generated at each access</li>
  * <li>Logs, which are representing one log "line" and link to the ActionLog which led to their generation</li>
  * </ul>
  * 
  * TODO improve filtering
  * 
  * @author Manuel Gay
  * 
  */
 public class DatabaseLogServlet extends HttpServlet {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     private Logger logger = Logger.getLogger(DatabaseLogServlet.class);
 
     private RowProperties rp;
     
     private HashMap<String, ActionLogDTO> commits = new HashMap<String, ActionLogDTO>();
     
     public void init(ServletConfig conf) {
         rp = new RowProperties();
     }
 
     private final static boolean STRICT_ACTIONLOG_FILTER = true;
 
     public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 
         try {
 
             // when we start tomcat we are not ready yet to log
             // we first need a Hibernate SessionFactory to be initalised
             // this happens only after the Initservlet was loaded
             req.removeAttribute("org.makumba.parade.servletSuccess");
             if (InitServlet.getSessionFactory() == null)
                 return;
             req.setAttribute("org.makumba.parade.servletSuccess", true);
 
             // retrieve the record guy
             Object record = req.getAttribute("org.makumba.parade.servletParam");
             if (record == null)
                 return;
 
             handleIncomingLog(record);
 
         } catch (NullPointerException npe) {
             // throw(npe);
             StringWriter sw = new StringWriter();
             PrintWriter p = new PrintWriter(sw);
             npe.printStackTrace(p);
             sw.flush();
             logger.error("\n***********************************************************************\n"
                     + "NPE in database log servlet. please tell developers!\n" + "error message is:\n" + sw.toString()
                     + "***********************************************************************");
 
         }
     }
 
     public void handleIncomingLog(Object record) {
         // first check if this should be logged at all
         if (record instanceof ActionLogDTO) {
             if (!shouldLog((ActionLogDTO) record))
                 return;
         }
 
         // open a new session, which we need to perform extraction
         Session s = null;
         try {
 
             s = InitServlet.getSessionFactory().openSession();
 
             boolean further = true;
             
             
             if (record instanceof ActionLogDTO) {
                 further = handleActionLog(record, s);
             } else {
                 handleLog(record, s);
             }
 
             // now we pass the event to Aether
             if (further && record instanceof ActionLogDTO && InitServlet.aetherEnabled) {
 
                 ActionLogDTO a = (ActionLogDTO) record;
 
                 if (!a.getAction().equals(ActionTypes.LOGIN.action())) {
                     AetherEvent e = buildAetherEventFromLog(a, s);
 
                     if (e != null) {
                         InitServlet.getAether().registerEvent(e, false);
                     }
                 }
             }
 
         } finally {
             // close the session in any case
             s.close();
         }
     }
 
     private AetherEvent buildAetherEventFromLog(ActionLogDTO log, Session s) {
 
         if (log.getObjectType() == null) {
             logger.error("**********************************\n" + "Object type for ActionLog not set: "
                     + log.toString());
             return null;
         }
 
         Double initialLevelCoefficient = 1.0;
         String objectURL = log.getObjectType().prefix();
         switch (log.getObjectType()) {
         case ROW:
             objectURL += log.getParadecontext();
             break;
         case USER:
             objectURL += log.getUser();
             break;
         case FILE:
             objectURL += log.getParadecontext() + log.getFile();
             break;
         case DIR:
             objectURL += log.getParadecontext() + (log.getFile().length() > 0 ? log.getFile() : "/");
             break;
         case CVSFILE:
             objectURL += log.getParadecontext() + log.getFile();
             break;
         }
 
         if (log.getAction().equals(ActionTypes.SAVE.action())) {
             Transaction tx = s.beginTransaction();
             String rowName = log.getParadecontext() == null ? log.getContext() : log.getParadecontext();
             if (rowName == null)
                 rowName = "";
             File f = (File) s
                     .createQuery(
                             "select f as file from File f join f.row r where substring(f.path, 1 + length(r.rowpath) + length(r.webappPath) + case when length(r.webappPath) = 0 then 0 else 1 end, length(f.path)) = :path and r.rowname = :rowname")
                     .setString("path", log.getFile()).setString("rowname", rowName).uniqueResult();
 
             if (f != null) {
                 initialLevelCoefficient = Math.abs(f.getPreviousChars() - f.getCurrentChars() + 0.00) / ((f.getCurrentChars() + f.getPreviousChars()) / 2 + 0.00);
                 
                 if(Double.isNaN(initialLevelCoefficient))
                     initialLevelCoefficient = 0.00;
             }
 
             tx.commit();
         }
 
         if (log.getObjectType().equals(ObjectTypes.FILE) && isExcluded(log.getFile())) {
             initialLevelCoefficient = 0.00;
 
         }
 
         return new AetherEvent(objectURL, log.getObjectType().toString(), log.getUser(), log.getAction(),
                 log.getDate(), initialLevelCoefficient);
     }
 
     private Vector<String> aetherExcludedFiles;
 
     private boolean isExcluded(String file) {
         if (aetherExcludedFiles == null) {
             aetherExcludedFiles = new Vector<String>();
             String excluded = ParadeProperties.getParadeProperty("aether.excludedFiles");
             if (excluded != null) {
                 StringTokenizer st = new StringTokenizer(excluded, ",");
                 while (st.hasMoreTokens()) {
                     aetherExcludedFiles.add(st.nextToken().trim());
                 }
             }
         }
         return aetherExcludedFiles == null ? false : aetherExcludedFiles.contains(file);
     }
 
     private boolean handleActionLog(Object record, Session s) {
         ActionLogDTO log = (ActionLogDTO) record;
 
         // filter the log, generate additional information and give some meaning
         filterLog(log, s);
 
         // sometimes we just don't log (like for commits)
         if (log.getAction().equals("paradeCvsCommit"))
             return false;
 
         // let's see if we have already someone. if not, we create one
         Transaction tx = s.beginTransaction();
         ActionLog actionLog = null;
         if (log.getId() == null) {
             actionLog = new ActionLog();
         } else {
             actionLog = (ActionLog) s.get(ActionLog.class, log.getId());
         }
         log.populate(actionLog);
         s.saveOrUpdate(actionLog);
         tx.commit();
 
         // if we didn't have a brand new actionLog (meaning, a log with some info)
         // we add the populated actionLog as an event to the tickertape
         // TODO refactor me
         if (log.getId() != null) {
             String row = (log.getParadecontext() == null || log.getParadecontext().equals("null")) ? ((log.getContext() == null || log
                     .getContext().equals("null")) ? "parade2" : log.getContext())
                     : log.getParadecontext();
             String actionText = "";
             if (log.getAction() != null && !log.getAction().equals("null"))
                 actionText = "user " + log.getUser() + " in row " + row + " did action: " + log.getAction();
             TickerTapeData data = new TickerTapeData(actionText, "", log.getDate().toString());
             TickerTapeServlet.addItem(data);
         }
         // finally we also need to update the ActionLog in the thread
         log.setId(actionLog.getId());
         TriggerFilter.actionLog.set(log);
         
         return true;
 
     }
 
     /**
      * Filter that does some "cosmetics" on the log and gives it meaning
      * 
      * @param log
      *            the original log to be altered
      */
     private void filterLog(ActionLogDTO log, Session s) {
         
         String queryString = log.getQueryString();
         
         String uri = log.getUrl();
         
         if (uri == null)
             uri = "";
 
         if (queryString == null)
             queryString = "";
 
         if (log.getAction() == null)
             log.setAction("");
 
         if (log.getParadecontext() == null) {
             log.setParadecontext(getParam("context", queryString));
         }
         if (log.getParadecontext() == null) {
             log.setParadecontext(log.getContext());
         }
 
         String webapp = "";
 
         // fetch the webapp root in a hackish way
         String ctx = log.getParadecontext() == null ? log.getContext() : log.getParadecontext();
         if (ctx != null) {
             if (ctx.equals("parade2")) {
                 webapp = "";
             } else {
                 Map<String, String> rowDef = rp.getRowDefinitions().get(ctx);
                 if (rowDef != null) {
                     webapp = rowDef.get("webapp");
                 } else {
                     logger.warn("Context " + ctx + " has invalid webapp path for actionLogDTO " + log.toString());
                 }
             }
         }
 
         String actionType = "", op = "", params = "", display = "", path = "", file = "", view = "";
 
         if (uri.equals("/") || uri.equals("/index.jsp"))
             actionType = "browseParade";
         if (uri.indexOf("browse.jsp") > -1)
             actionType = "browseRow";
         if (uri.indexOf("/servlet/browse") > -1)
             actionType = "browse";
         if (uri.indexOf("File.do") > -1)
             actionType = "file";
         if (uri.indexOf("File.do") > -1 && queryString.indexOf("browse&") > -1)
             actionType = "fileBrowse";
         if (uri.indexOf("Cvs.do") > -1)
             actionType = "cvs";
         if (uri.indexOf("Webapp.do") > -1)
             actionType = "webapp";
         if (uri.indexOf("Command.do") > -1)
             actionType = "command";
 
         op = getParam("op", queryString);
         params = getParam("params", queryString);
         display = getParam("display", queryString);
         path = getParam("path", queryString);
         file = getParam("file", queryString);
         view = getParam("view", queryString);
 
         if (op == null)
             op = "";
         if (params == null)
             params = "";
         if (display == null)
             display = "";
         if (path == null)
             path = "";
         if (file == null)
             file = "";
         if (view == null)
             view = "";
 
         // browse actions
         if (actionType.equals("browseParade")) {
             log.setContext("parade2");
             log.setParadecontext("parade2");
             log.setAction(ActionTypes.VIEW.action());
             log.setObjectType(ObjectTypes.PARADE);
         } else
 
         if (actionType.equals("browseRow")) {
             log.setAction(ActionTypes.VIEW.action());
             log.setObjectType(ObjectTypes.ROW);
         } else
 
         if (actionType.equals("browse") || actionType.equals("fileBrowse")) {
             log.setAction(ActionTypes.VIEW.action());
             log.setFile(nicePath(path, "", ""));
             log.setObjectType(ObjectTypes.DIR);
         } else
 
         // view actions
         if (uri.endsWith(".jspx")) {
             log.setAction(ActionTypes.VIEW.action());
             if (webapp.length() > 0) {
                 log.setFile("/" + webapp + uri.substring(0, uri.length() - 1));
                log.setObjectType(ObjectTypes.FILE);
             }
         } else
 
         // execute actions
         if (uri.endsWith(".jsp")) {
             log.setAction(ActionTypes.EXECUTE.action());
             if (webapp.length() > 0) {
                 log.setFile("/" + webapp + uri.substring(0, uri.length()));
                 log.setObjectType(ObjectTypes.FILE);
             }
         } else
 
         if (uri.indexOf("servlet") > -1 && !log.getContext().equals("parade2")) {
             log.setAction(ActionTypes.EXECUTE.action());
             if (webapp.length() > 0) {
                 log.setFile("/" + webapp + uri.substring(0, uri.length()));
                 log.setObjectType(ObjectTypes.FILE);
             }
         } else
 
         if (uri.endsWith("/") && !log.getContext().equals("parade2")) {
             log.setAction(ActionTypes.EXECUTE.action());
             if (webapp.length() > 0) {
                 log.setFile("/" + webapp + uri.substring(0, uri.length()));
                 log.setObjectType(ObjectTypes.DIR);
             }
         } else
 
         // edit (open editor)
         if (actionType.equals("file") && op.equals("editFile")) {
             log.setAction(ActionTypes.EDIT.action());
             log.setFile(nicePath(path, file, webapp));
             log.setObjectType(ObjectTypes.FILE);
         } else
 
         // save
         if (actionType.equals("file") && op.equals("saveFile")) {
             log.setAction(ActionTypes.SAVE.action());
             log.setFile(nicePath(path, file, webapp));
             log.setObjectType(ObjectTypes.FILE);
         } else
 
         // delete
         if (actionType.equals("file") && op.equals("deleteFile")) {
             log.setAction(ActionTypes.DELETE.action());
             log.setFile(nicePath(path, params, webapp));
             log.setObjectType(ObjectTypes.FILE);
         } else
 
         if (actionType.equals("command") && op.equals("newFile")) {
             log.setAction(ActionTypes.CREATE.action());
             log.setFile(nicePath(path, params, webapp));
             log.setObjectType(ObjectTypes.FILE);
 
         } else
 
         if (actionType.equals("command") && op.equals("newDir")) {
             log.setAction(ActionTypes.CREATE.action());
             log.setFile(nicePath(path, params, webapp));
             log.setObjectType(ObjectTypes.DIR);
 
         } else
 
         // CVS
         if (actionType.equals("cvs")) {
 
             if (op.equals("check")) {
                 log.setAction(ActionTypes.CVS_CHECK.action());
                 log.setFile("/" + params);
                 log.setObjectType(ObjectTypes.CVSFILE);
             }
             if (op.equals("update")) {
                 log.setAction(ActionTypes.CVS_UPDATE_DIR_LOCAL.action());
                 log.setFile("/" + params);
                 log.setObjectType(ObjectTypes.CVSFILE);
 
             }
             if (op.equals("rupdate")) {
                 log.setAction(ActionTypes.CVS_UPDATE_DIR_RECURSIVE.action());
                 log.setFile("/" + params);
                 log.setObjectType(ObjectTypes.CVSFILE);
             }
             if (op.equals("commit")) {
                 
                 // this action won't get logged, since we will get another log from the cvs hook
                 // we just store it in a variable
                 // since we can have multiple commits we generate one new log per file
                 
                 try {
                     queryString = URLDecoder.decode(queryString, "UTF-8");
                 } catch (UnsupportedEncodingException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 
                 String[] fileURLs = getParamValues("file", queryString, null, 0);
                 for (String URL : fileURLs) {
                     if(URL == null)
                         break;
                     ActionLogDTO dto = new ActionLogDTO();
                     dto.setUser(log.getUser());
                     dto.setParadecontext(ObjectTypes.rowNameFromURL(URL));
                     dto.setContext(ObjectTypes.rowNameFromURL(URL));
                     dto.setAction(ActionTypes.CVS_COMMIT.action());
                     dto.setDate(log.getDate());
                     dto.setFile(ObjectTypes.fileOrDirPathFromFileOrDirURL(URL));
                     dto.setObjectType(ObjectTypes.CVSFILE);
                     
                     commits.put("/" + ObjectTypes.pathFromFileOrDirURL(URL), dto);
                 }
                 
                 // "paradeCvsCommit" is not logged
                 log.setAction("paradeCvsCommit");
 
             }
             if (op.equals("diff")) {
                 log.setAction(ActionTypes.CVS_DIFF.action());
                 log.setFile("/" + file);
                 log.setObjectType(ObjectTypes.CVSFILE);
             }
             if (op.equals("add") || op.equals("addbin")) {
                 log.setAction(ActionTypes.CVS_ADD.action());
                 log.setFile("/" + file);
                 log.setObjectType(ObjectTypes.CVSFILE);
             }
             if (op.equals("updatefile")) {
                 log.setAction(ActionTypes.CVS_UPDATE_FILE.action());
                 log.setFile("/" + file);
                 log.setObjectType(ObjectTypes.CVSFILE);
             }
             if (op.equals("overridefile")) {
                 log.setAction(ActionTypes.CVS_OVERRIDE_FILE.action());
                 log.setFile("/" + file);
                 log.setObjectType(ObjectTypes.CVSFILE);
             }
             if (op.equals("deletefile")) {
                 log.setAction(ActionTypes.CVS_DELETE_FILE.action());
                 log.setFile("/" + file);
                 log.setObjectType(ObjectTypes.CVSFILE);
             }
         }
 
         // CVS commit (hook)
         if (log.getAction().equals("cvsCommitRepository")) {
             log.setAction(ActionTypes.CVS_COMMIT.action());
 
             if (commits.get(log.getFile()) != null) {
                 // the user commited through parade
                 // we can enrich the file with information
                 log = commits.get(log.getFile());
                 commits.remove(log.getFile());
                 
             } else {
                 // this is an external commit that doesn't come thru parade
                 // let's try to see if we know the user who did the commit
                 Transaction tx = s.beginTransaction();
                 Query q = s.createQuery("from User u where u.cvsuser = ?");
                 q.setString(0, log.getUser());
                 List<User> results = q.list();
                 if (results.size() > 0) {
                     User u = results.get(0);
                     if (u != null) {
                         log.setUser(u.getLogin());
                     } else {
                         log.setUser(User.getUnknownUser().getLogin());
                     }
                 } else {
                     log.setUser(User.getUnknownUser().getLogin());
                 }
                 tx.commit();
             }
         }
 
         // webapp matters
         if (actionType.equals("webapp")) {
             log.setObjectType(ObjectTypes.ROW);
 
             if (op.equals("servletContextInstall")) {
                 log.setAction(ActionTypes.WEBAPP_INSTALL.action());
             }
 
             if (op.equals("servletContextRemove")) {
                 log.setAction(ActionTypes.WEBAPP_UNINSTALL.action());
             }
 
             if (op.equals("servletContextReload")) {
                 log.setAction(ActionTypes.WEBAPP_RELOAD.action());
             }
 
             if (op.equals("servletContextRedeploy")) {
                 log.setAction(ActionTypes.WEBAPP_REDEPLOY.action());
             }
 
             if (op.equals("servletContextStop")) {
                 log.setAction(ActionTypes.WEBAPP_STOP.action());
             }
 
             if (op.equals("servletContextStart")) {
                 log.setAction(ActionTypes.WEBAPP_START.action());
             }
         }
     }
 
     private String nicePath(String path, String file, String webapp) {
         try {
             // path doesn't end with /
             path = path.indexOf("%") > -1 ? URLDecoder.decode(path, "UTF-8") : path;
             path = path.startsWith("/") ? "" : "/" + (path.endsWith("/") ? path.substring(0, path.length() - 1) : path);
 
             // file starts with /
             file = file.indexOf("%") > -1 ? URLDecoder.decode(file, "UTF-8") : file;
             file = file.startsWith("/") ? file : (file.length() == 0 ? "" : "/" + file);
 
             // remove webapp path
             if (webapp.length() > 0) {
                 path = path.startsWith("/" + webapp) ? path.substring(("/" + webapp).length()) : path;
                 file = file.startsWith("/" + webapp) ? file.substring(("/" + webapp).length()) : file;
 
             }
 
         } catch (UnsupportedEncodingException e) {
             // shouldn't happen
         }
 
         return (path.endsWith("/") && file.startsWith("/") ? path + file.substring(1) : path + file);
     }
 
     private String getParam(String paramName, String queryString) {
         int n = queryString.indexOf(paramName + "=");
         String param = null;
         if (n > -1) {
             param = queryString.substring(n + paramName.length() + 1);
             if (param.indexOf("&") > -1) {
                 param = param.substring(0, param.indexOf("&"));
             }
         }
         return param;
     }
 
     private String[] getParamValues(String paramName, String queryString, String[] paramValues, int pos) {
 
         if (pos == 0) {
             paramValues = new String[15];
         }
 
         int n = queryString.indexOf(paramName + "=");
         String param = null;
         if (n > -1) {
             param = queryString.substring(n + paramName.length() + 1);
             if (param.indexOf("&") > -1) {
                 param = param.substring(0, param.indexOf("&"));
             }
             paramValues[pos] = param;
 
             String qs = queryString.substring(0, n)
                     + queryString.substring(n + paramName.length() + 1 + param.length());
             pos++;
             return getParamValues(paramName, qs, paramValues, pos);
         }
         return paramValues;
 
     }
 
     String[] endFilter = { ".ico", ".css", ".gif", ".jpg", ".png", ".js" };
 
     String[] startFilter = { "/logs", "/admin", "/aether", "/playground/", "/logic", "/dataDefinitions",
             "/scripts/codepress/", "/cewolf", "/servlet/cvscommit" };
 
     String[] equalFilter = { "/logout.jsp", "/userView.jsp", "/userEdit.jsp", "/showImage.jsp", "/log.jsp",
             "/actionLog.jsp", "/actionLogList.jsp", "/logHeader.jsp", "browserHeader.jsp", "fileBrowser.jsp",
             "/todo.jsp", "/error.jsp", "/tipOfTheDay.jsp", "/Admin.do", "/User.do", "/servlet/ticker", "/servlet/logs",
             "/reload", "/unauthorized/index.jsp", "/cvsCommit.jsp" };
 
     /**
      * Checks whether this access should be logged or not.<br>
      * Note that since each Log needs an attached ActionLog this mechanism won't always work.
      * 
      * @param log
      *            the DTO containing the log entry
      * @return <code>true</code> if this is worth logging, <code>false</code> otherwise
      */
     private boolean shouldLog(ActionLogDTO log) {
 
         if (log.getUrl() != null
                 && (
 
                 StringUtils.startsWith(log.getUrl(), startFilter)
                         || StringUtils.endsWith(log.getUrl(), endFilter)
                         || StringUtils.equalsAny(log.getUrl(), equalFilter)
 
                         || (log.getUrl().equals("/servlet/browse") && log.getQueryString().indexOf("display=header") > -1)
                         || (log.getUrl().equals("/servlet/browse") && log.getQueryString().indexOf("display=tree") > -1)
                         || (log.getUrl().equals("/servlet/browse") && log.getQueryString().indexOf("display=command") > -1) 
                         || (log.getUrl().equals("/File.do")
                         && log.getQueryString().indexOf("display=command") > -1 && log.getQueryString().indexOf(
                         "view=new") > -1)
 
                 )
 
                 || log.getUser() == null
                 || (log.getOrigin() != null && log.getOrigin().equals("tomcat"))
                 || (log.getUser().equals("system-u") && log.getContext().equals("parade2") && log.getUrl() == null
                         && log.getAction() == null && log.getOrigin() == null)
 
         ) {
             return false;
         }
 
         return true;
     }
 
     private void handleLog(Object record, Session s) {
         // extract useful information from the record
 
         Log log = new Log();
         ActionLog actionLog = retrieveActionLog(s);
 
         if (actionLog == null) {
             return;
         }
 
         log.setActionLog(actionLog);
 
         // this is a java.util.logging.LogRecord
         if (record instanceof LogRecord) {
             LogRecord logrecord = (LogRecord) record;
             log.setLogDate(new Date(logrecord.getMillis()));
             log.setLevel(logrecord.getLevel().getName());
             log.setMessage(logrecord.getMessage());
             log.setOrigin("java.util.Logging");
             // log.setThrowable(logrecord.getThrown());
         } else if (record instanceof LoggingEvent) {
             LoggingEvent logevent = (LoggingEvent) record;
             log.setOrigin("log4j");
             log.setLogDate(new Date(logevent.timeStamp));
             log.setLevel(logevent.getLevel().toString());
             log.setMessage(logevent.getRenderedMessage());
             // if(logevent.getThrowableInformation() != null)
             // log.setThrowable(logevent.getThrowableInformation().getThrowable());
             // else
             // log.setThrowable(null);
         } else if (record instanceof PerThreadPrintStreamLogRecord) {
             PerThreadPrintStreamLogRecord pRecord = (PerThreadPrintStreamLogRecord) record;
             log.setLogDate(pRecord.getDate());
             log.setOrigin("stdout");
             log.setLevel("INFO");
             log.setMessage(pRecord.getMessage());
             // log.setThrowable(null);
         } else if (record instanceof Object[]) {
             Object[] rec = (Object[]) record;
             log.setLogDate((Date) rec[0]);
             log.setOrigin("TriggerFilter");
             log.setLevel("INFO");
             log.setMessage((String) rec[1]);
         }
 
         Transaction tx = s.beginTransaction();
 
         // write the guy to the db
         s.saveOrUpdate(log);
         tx.commit();
     }
 
     /**
      * Retrieves the current ActionLog and persists if it necessary.
      * 
      * @param s
      *            a Hibernate {@link Session}
      * @return the current {@link ActionLog}, null if it should not be persisted.
      */
     private ActionLog retrieveActionLog(Session s) {
         ActionLogDTO actionLogDTO = TriggerFilter.actionLog.get();
 
         if (!shouldLog(actionLogDTO) && STRICT_ACTIONLOG_FILTER) {
             return null;
         }
 
         ActionLog actionLog = new ActionLog();
         actionLogDTO.populate(actionLog);
 
         // if the actionLog is there but not persisted, we persist it first
         if (actionLog.getId() == null) {
             Transaction tx = s.beginTransaction();
             s.save(actionLog);
             tx.commit();
             actionLogDTO.setId(actionLog.getId());
             TriggerFilter.actionLog.set(actionLogDTO);
         } else {
             actionLog = (ActionLog) s.get(ActionLog.class, actionLogDTO.getId());
         }
         return actionLog;
     }
 
 }
