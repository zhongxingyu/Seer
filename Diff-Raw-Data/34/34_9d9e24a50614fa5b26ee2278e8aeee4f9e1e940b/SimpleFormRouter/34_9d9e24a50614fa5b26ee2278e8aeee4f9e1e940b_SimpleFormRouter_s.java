 package org.jails.form.controller;
 
 import org.jails.form.SimpleFormParams;
 import org.jails.util.Strings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SimpleFormRouter {
 	private static Logger logger = LoggerFactory.getLogger(SimpleFormRouter.class);
 
 	public static String ACTION_DELETE = "delete";
 	public static String ACTION_NEW = "new";
 	public static String ACTION_EDIT = "edit";
 	public static String ACTION_SHOW = "show";
 
 	public static String ACTION_SUBMIT = "action_submit";
 	public static String SUBMIT_DELETE = "action_delete";
 	public static String SUBMIT_NEW = "action_new";
 	public static String SUBMIT_EDIT = "action_edit";
 	public static String SUBMIT_SHOW = "action_show";
 	public static String CONFIRM_DELETE = "confirm_delete";
 
 	private static SimpleFormParams simpleFormParams = new SimpleFormParams();
 
 	private String path;
 
 	public SimpleFormRouter(String path) {
 		this.path = path;
 	}
 
 	public String getMethod(HttpServletRequest request) {
 		String method = request.getParameter(simpleFormParams.getMetaParameterName("method"));
 		return (method != null) ? method : request.getMethod();
 	}
 
 	public boolean isShow(HttpServletRequest request) {
 		boolean isShow = ((ACTION_SHOW.equals(getActionPath(request)) || Strings.isEmpty(getActionPath(request)))
 				&& getMethod(request).equalsIgnoreCase("GET"))
 				&& getId(request) != null;
 		logger.info("isShow? " + isShow);
 		return isShow;
 	}
 
 	public boolean isNew(HttpServletRequest request) {
 		boolean isCreate = (ACTION_NEW.equals(getActionPath(request)) || getMethod(request).equalsIgnoreCase("PUT"));
 		logger.info("isCreate? " + isCreate);
 		return isCreate;
 	}
 
 	public boolean isEdit(HttpServletRequest request) {
 		boolean isEdit = (ACTION_EDIT.equals(getActionPath(request)) || getMethod(request).equalsIgnoreCase("POST"));
 		logger.info("isEdit? " + isEdit);
 		return isEdit;
 	}
 
 	public boolean isDelete(HttpServletRequest request) {
 		boolean isDelete = (ACTION_DELETE.equals(getActionPath(request)) || getMethod(request).equalsIgnoreCase("DELETE"));
 		logger.info("isDelete? " + isDelete);
 		return isDelete;
 	}
 
 	public boolean confirmDelete(HttpServletRequest request) {
		boolean isConfirmDelete = Strings.getBoolean(request.getParameter(simpleFormParams.getParameterName(CONFIRM_DELETE)));
 		logger.info("isConfirmDelete? " + isConfirmDelete);
 		return isConfirmDelete;
 	}
 
 	public boolean isSubmitted(HttpServletRequest request) {
 		boolean isSubmitted = (isDelete(request) && (simpleFormParams.getMetaParameterName(SUBMIT_DELETE)).equals(request.getParameter(simpleFormParams.getMetaParameterName(ACTION_SUBMIT))))
 				|| (isEdit(request) && (simpleFormParams.getMetaParameterName(SUBMIT_EDIT)).equals(request.getParameter(simpleFormParams.getMetaParameterName(ACTION_SUBMIT))))
 				|| (isNew(request) && (simpleFormParams.getMetaParameterName(SUBMIT_NEW)).equals(request.getParameter(simpleFormParams.getMetaParameterName(ACTION_SUBMIT))));
 		logger.info("isSubmitted? " + isSubmitted);
 		return isSubmitted;
 	}
 
 	//requestPath = /path/[id],[id].../action
 	public Integer[] getIds(HttpServletRequest request) {
 		Integer[] ids = null;
 		logger.info("path: " + request.getRequestURI());
 		String requestPath = request.getRequestURI().replace("/" + path + "/", "");
 		logger.info("action: " + requestPath);
 		if (requestPath.indexOf(",") > 0) {
 			Pattern idPattern = Pattern.compile("^([0-9,]+)");
 			Matcher idMatcher = idPattern.matcher(requestPath);
 			if (idMatcher.find()) {
 				logger.info("matched: " + idMatcher.group(0));
 				String[] idStrings = idMatcher.group(0).split(",");
 				ids = new Integer[idStrings.length];
 				for (int i = 0; i < idStrings.length; i++) {
 					try {
 						logger.info("parsing " + idStrings[i] + "|");
 						ids[i] = Integer.parseInt(idStrings[i]);
 					} catch (Exception e) {
 						logger.warn(e.toString());
 					}
 				}
 			} else {
 				logger.info("No match");
 			}
 		} else {
 			Pattern idPattern = Pattern.compile("^([0-9]+)");
 			Matcher idMatcher = idPattern.matcher(requestPath);
 			if (idMatcher.find()) {
 				logger.info("matched: " + idMatcher.group(0));
 				String idString = idMatcher.group(0);
 				ids = new Integer[1];
 				try {
 					logger.info("parsing " + idString + "|");
 					ids[0] = Integer.parseInt(idString);
 				} catch (Exception e) {
 					logger.warn(e.toString());
 				}
 			} else {
 				logger.info("No match");
 			}
 		}
 		if (ids == null) ids = new Integer[0];
 		return ids;
 	}
 
 	public Integer getId(HttpServletRequest request) {
 		Integer[] ids = getIds(request);
 		return (ids.length == 0) ? null : ids[0];
 	}
 
 	public int idCount(HttpServletRequest request) {
 		Integer[] ids = getIds(request);
 		return (ids != null) ? ids.length : 0;
 
 	}
 
 	protected String getActionPath(HttpServletRequest request) {
 		String actionPath = (request.getRequestURI().indexOf("/") >= 0)
 				? request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1)
 				: null;
 		logger.info("actionPath: " + actionPath);
 		return actionPath;
 	}
 }
