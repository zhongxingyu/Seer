 package azkaban.webapp.servlet;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.security.AccessControlException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.ZipFile;
 
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 
 import azkaban.executor.ExecutableFlow;
 import azkaban.executor.ExecutorManager;
 import azkaban.flow.Edge;
 import azkaban.flow.Flow;
 import azkaban.flow.Node;
 import azkaban.project.Project;
 import azkaban.project.ProjectManager;
 import azkaban.project.ProjectManagerException;
 import azkaban.scheduler.ScheduleManager;
 import azkaban.scheduler.ScheduledFlow;
 import azkaban.user.Permission;
 import azkaban.user.UserManager;
 import azkaban.user.Permission.Type;
 import azkaban.user.User;
 import azkaban.utils.Pair;
 import azkaban.utils.Props;
 import azkaban.utils.Utils;
 import azkaban.webapp.session.Session;
 import azkaban.webapp.servlet.MultipartParser;
 
 public class ProjectManagerServlet extends LoginAbstractAzkabanServlet {
 	private static final long serialVersionUID = 1;
 	private static final Logger logger = Logger.getLogger(ProjectManagerServlet.class);
 	private static final int DEFAULT_UPLOAD_DISK_SPOOL_SIZE = 20 * 1024 * 1024;
 	private static final NodeLevelComparator NODE_LEVEL_COMPARATOR = new NodeLevelComparator();
 	
 	private ProjectManager projectManager;
 	private ExecutorManager executorManager;
 	private ScheduleManager scheduleManager;
 	private MultipartParser multipartParser;
 	private File tempDir;
 	private static Comparator<Flow> FLOW_ID_COMPARATOR = new Comparator<Flow>() {
 		@Override
 		public int compare(Flow f1, Flow f2) {
 			return f1.getId().compareTo(f2.getId());
 		}
 	};
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		projectManager = this.getApplication().getProjectManager();
 		executorManager = this.getApplication().getExecutorManager();
 		scheduleManager = this.getApplication().getScheduleManager();
 		
 		tempDir = this.getApplication().getTempDirectory();
 		multipartParser = new MultipartParser(DEFAULT_UPLOAD_DISK_SPOOL_SIZE);
 	}
 
 	@Override
 	protected void handleGet(HttpServletRequest req, HttpServletResponse resp, Session session) throws ServletException, IOException {
 		if ( hasParam(req, "project") ) {
 			if (hasParam(req, "ajax")) {
 				handleAJAXAction(req, resp, session);
 			}
 			else if (hasParam(req, "logs")) {
 				handleProjectLogsPage(req, resp, session);
 			}
 			else if (hasParam(req, "permissions")) {
 				handlePermissionPage(req, resp, session);
 			}
 			else if (hasParam(req, "job")) {
 				handleJobPage(req, resp, session);
 			}
 			else if (hasParam(req, "flow")) {
 				handleFlowPage(req, resp, session);
 			}
 			else if (hasParam(req, "delete")) {
 				handleRemoveProject(req, resp, session);
 			}
 			else {
 				handleProjectPage(req, resp, session);
 			}
 			return;
 		}
 		
 		Page page = newPage(req, resp, session, "azkaban/webapp/servlet/velocity/projectpage.vm");
 		page.add("errorMsg", "No project set.");
 		page.render();
 	}
 
 	@Override
 	protected void handlePost(HttpServletRequest req, HttpServletResponse resp, Session session) throws ServletException, IOException {
 		if (ServletFileUpload.isMultipartContent(req)) {
 			logger.info("Post is multipart");
 			Map<String, Object> params = multipartParser.parseMultipart(req);
 			if (params.containsKey("action")) {
 				String action = (String)params.get("action");
 				if (action.equals("upload")) {
 					handleUpload(req, resp, params, session);
 				}
 			}
 		}
 		else if (hasParam(req, "action")) {
 			String action = getParam(req, "action");
 			if (action.equals("create")) {
 				handleCreate(req, resp, session);
 			}
 		}
 	}
 	
 	private void handleAJAXAction(HttpServletRequest req, HttpServletResponse resp, Session session) throws ServletException, IOException {
 		String projectName = getParam(req, "project");
 		User user = session.getUser();
 		
 		HashMap<String, Object> ret = new HashMap<String, Object>();
 		ret.put("project", projectName);
 		
 		Project project = projectManager.getProject(projectName);
 		if (project == null) {
 			ret.put("error", "Project " + projectName + " doesn't exist.");
 			return; 
 		}
 		
 		String ajaxName = getParam(req, "ajax");
 		if (ajaxName.equals("fetchProjectLogs")) {
 			ajaxFetchProjectLogs(project, req, resp, ret, user);
 			ret = null;
 		}
 		else if (ajaxName.equals("fetchflowjobs")) {
 			if (handleAjaxPermission(project, user, Type.READ, ret)) {
 				ajaxFetchFlow(project, ret, req, resp);
 			}
 		}
 		else if (ajaxName.equals("fetchflowgraph")) {
 			if (handleAjaxPermission(project, user, Type.READ, ret)) {
 				ajaxFetchFlowGraph(project, ret, req);
 			}
 		}
 		else if (ajaxName.equals("fetchprojectflows")) {
 			if (handleAjaxPermission(project, user, Type.READ, ret)) {
 				ajaxFetchProjectFlows(project, ret, req);
 			}
 		}
 		else if (ajaxName.equals("changeDescription")) {
 			if (handleAjaxPermission(project, user, Type.WRITE, ret)) {
 				ajaxChangeDescription(project, ret, req, user);
 			}
 		}
 		else if (ajaxName.equals("getPermissions")) {
 			if (handleAjaxPermission(project, user, Type.READ, ret)) {
 				ajaxGetPermissions(project, ret);
 			}
 		}
 		else if (ajaxName.equals("changeUserPermission")) {
 			if (handleAjaxPermission(project, user, Type.ADMIN, ret)) {
 				ajaxChangePermissions(project, ret, req, user);
 			}
 		}
 		else if (ajaxName.equals("addUserPermission")) {
 			if (handleAjaxPermission(project, user, Type.ADMIN, ret)) {
 				ajaxAddUserPermission(project, ret, req, user);
 			}
 		}
 		else if (ajaxName.equals("fetchFlowExecutions")) {
 			if (handleAjaxPermission(project, user, Type.READ, ret)) {
 				ajaxFetchFlowExecutions(project, ret, req);
 			}
 		}
 		else {
 			ret.put("error", "Cannot execute command " + ajaxName);
 		}
 		
 		if (ret != null) {
 			this.writeJSON(resp, ret);
 		}
 	}
 	
 	private boolean handleAjaxPermission(Project project, User user, Type type, Map<String, Object> ret) {
 		if (project.hasPermission(user, type)) {
 			return true;
 		}
 		
 		ret.put("error", "Permission denied. Need " + type.toString() + " access.");
 		return false;
 	}
 	
 	/**
 	 * Gets the logs through plain text stream to reduce memory overhead.
 	 * 
 	 * @param req
 	 * @param resp
 	 * @param user
 	 * @param exFlow
 	 * @throws ServletException
 	 */
 	private void ajaxFetchProjectLogs(Project project, HttpServletRequest req, HttpServletResponse resp, HashMap<String, Object> ret,  User user) throws ServletException {
 		if (!project.hasPermission(user, Type.READ)) {
 			return;
 		}
 		
 		int tailBytes = this.getIntParam(req, "tail");
 		
 		resp.setContentType("text/plain");
 		resp.setCharacterEncoding("utf-8");
 		PrintWriter writer;
 		try {
 			writer = resp.getWriter();
 		} catch (IOException e) {
 			throw new ServletException(e);
 		}
 
 		try {
 			projectManager.getProjectLogs(project.getName(), tailBytes, 0, writer);
 		} catch (IOException e) {
 			throw new ServletException(e);
 		}
 		finally {
 			writer.close();
 		}
 	}
 	
 	private void ajaxFetchFlowExecutions(Project project, HashMap<String, Object> ret, HttpServletRequest req) throws ServletException {
 		String flowId = getParam(req, "flow");
 		int from = Integer.valueOf(getParam(req, "start"));
 		int length = Integer.valueOf(getParam(req, "length"));
 		
 		ArrayList<ExecutableFlow> exFlows = new ArrayList<ExecutableFlow>();
 		int total = executorManager.getExecutableFlows(project.getName(),  flowId, from, length, exFlows);
 		
 		ret.put("flow", flowId);
 		ret.put("total", total);
 		ret.put("from", from);
 		ret.put("length", length);
 		
 		ArrayList<Object> history = new ArrayList<Object>();
 		for (ExecutableFlow flow: exFlows) {
 			HashMap<String, Object> flowInfo = new HashMap<String, Object>();
 			flowInfo.put("execId", flow.getExecutionId());
 			flowInfo.put("flowId", flow.getFlowId());
 			flowInfo.put("projectId", flow.getProjectId());
 			flowInfo.put("status", flow.getStatus().toString());
 			flowInfo.put("submitTime", flow.getSubmitTime());
 			flowInfo.put("startTime", flow.getStartTime());
 			flowInfo.put("endTime", flow.getEndTime());
 			flowInfo.put("submitUser", flow.getSubmitUser());
 			
 			history.add(flowInfo);
 		}
 		
 		ret.put("executions", history);
 	}
 	
 	private void handleRemoveProject(HttpServletRequest req, HttpServletResponse resp, Session session) throws ServletException, IOException {
 		User user = session.getUser();
 		String projectName = getParam(req, "project");
 		
 		Project project = projectManager.getProject(projectName);
 		if (project == null) {
 			this.setErrorMessageInCookie(resp, "Project " + projectName + " doesn't exist.");
 			resp.sendRedirect(req.getContextPath());
 			return;
 		}
 		
 		if (!project.hasPermission(user, Type.ADMIN)) {
 			this.setErrorMessageInCookie(resp, "Cannot delete. User '" + user.getUserId() + "' is not an ADMIN.");
 			resp.sendRedirect(req.getRequestURI() + "?project=" + projectName);
 			return;
 		}
 		
 		// Check if scheduled
 		ScheduledFlow sflow = null;
 		for (ScheduledFlow flow: scheduleManager.getSchedule()) {
 			if (flow.getProjectId().equals(projectName)) {
 				sflow = flow;
 				break;
 			}
 		}
 		if (sflow != null) {
 			this.setErrorMessageInCookie(resp, "Cannot delete. Please unschedule " + sflow.getScheduleId() + ".");
 			resp.sendRedirect(req.getRequestURI() + "?project=" + projectName);
 			return;
 		}
 
 		// Check if executing
 		ExecutableFlow exflow = null;
 		for (ExecutableFlow flow: executorManager.getRunningFlows()) {
 			if (flow.getProjectId() == projectName) {
 				exflow = flow;
 				break;
 			}
 		}
 		if (exflow != null) {
 			this.setErrorMessageInCookie(resp, "Cannot delete. Executable flow " + exflow.getExecutionId() + " is still running.");
 			resp.sendRedirect(req.getRequestURI() + "?project=" + projectName);
 			return;
 		}
 
 		project.info("Project removing by '" + user.getUserId() + "'");
 		try {
 			projectManager.removeProject(projectName);
 		} catch (ProjectManagerException e) {
 			this.setErrorMessageInCookie(resp, e.getMessage());
 			resp.sendRedirect(req.getRequestURI() + "?project=" + projectName);
 			return;
 		}
 		
 		this.setSuccessMessageInCookie(resp, "Project '" + projectName + "' was successfully deleted.");
 		resp.sendRedirect(req.getContextPath());
 	}
 	
 	private void ajaxChangeDescription(Project project, HashMap<String, Object> ret, HttpServletRequest req, User user) throws ServletException {
 		String description = getParam(req, "description");
 		project.setDescription(description);
 		
 		try {
 			projectManager.commitProject(project.getName());
 			project.info("Project description changed to '" + description + "' by " + user.getUserId());
 		} catch (ProjectManagerException e) {
 			ret.put("error", e.getMessage());
 		}
 	}
 	
 	private void ajaxFetchProjectFlows(Project project, HashMap<String, Object> ret, HttpServletRequest req) throws ServletException {
 		ArrayList<Map<String,Object>> flowList = new ArrayList<Map<String,Object>>();
 		for (Flow flow: project.getFlows()) {
 			HashMap<String, Object> flowObj = new HashMap<String, Object>();
 			flowObj.put("flowId", flow.getId());
 			flowList.add(flowObj);
 		}
 		
 		ret.put("flows", flowList); 
 	}
 	
 	private void ajaxFetchFlowGraph(Project project, HashMap<String, Object> ret, HttpServletRequest req) throws ServletException {
 		String flowId = getParam(req, "flow");
 		Flow flow = project.getFlow(flowId);
 		
 		//Collections.sort(flowNodes, NODE_LEVEL_COMPARATOR);
 		ArrayList<Map<String, Object>> nodeList = new ArrayList<Map<String, Object>>();
 		for (Node node: flow.getNodes()) {
 			HashMap<String, Object> nodeObj = new HashMap<String,Object>();
 			nodeObj.put("id", node.getId());
 			nodeObj.put("level", node.getLevel());
 
 			nodeList.add(nodeObj);
 		}
 		
 		ArrayList<Map<String, Object>> edgeList = new ArrayList<Map<String, Object>>();
 		for (Edge edge: flow.getEdges()) {
 			HashMap<String, Object> edgeObj = new HashMap<String,Object>();
 			edgeObj.put("from", edge.getSourceId());
 			edgeObj.put("target", edge.getTargetId());
 			
 			if (edge.hasError()) {
 				edgeObj.put("error", edge.getError());
 			}
 //			if (edge.getGuideValues() != null) {
 //				List<Point2D> guides = edge.getGuideValues();
 //				ArrayList<Object> guideOutput = new ArrayList<Object>();
 //				for (Point2D guide: guides) {
 //					double x = guide.getX();
 //					double y = guide.getY();
 //					HashMap<String, Double> point = new HashMap<String, Double>();
 //					point.put("x", x);
 //					point.put("y", y);
 //					guideOutput.add(point);
 //				}
 //				
 //				edgeObj.put("guides", guideOutput);
 //			}
 			
 			edgeList.add(edgeObj);
 		}
 		
 		ret.put("flowId", flowId);
 		ret.put("nodes", nodeList);
 		ret.put("edges", edgeList);
 	}
 	
 	private void ajaxFetchFlow(Project project, HashMap<String, Object> ret, HttpServletRequest req, HttpServletResponse resp) throws ServletException {
 		String flowId = getParam(req, "flow");
 		Flow flow = project.getFlow(flowId);
 
 		ArrayList<Node> flowNodes = new ArrayList<Node>(flow.getNodes());
 		Collections.sort(flowNodes, NODE_LEVEL_COMPARATOR);
 
 		ArrayList<Object> nodeList = new ArrayList<Object>();
 		for (Node node: flowNodes) {
 			HashMap<String, Object> nodeObj = new HashMap<String, Object>();
 			nodeObj.put("id", node.getId());
 			
 			ArrayList<String> dependencies = new ArrayList<String>();
 			Collection<Edge> collection = flow.getInEdges(node.getId());
 			if (collection != null) {
 				for (Edge edge: collection) {
 					dependencies.add(edge.getSourceId());
 				}
 			}
 			
 			ArrayList<String> dependents = new ArrayList<String>();
 			collection = flow.getOutEdges(node.getId());
 			if (collection != null) {
 				for (Edge edge: collection) {
 					dependents.add(edge.getTargetId());
 				}
 			}
 			
 			nodeObj.put("dependencies", dependencies);
 			nodeObj.put("dependents", dependents);
 			nodeObj.put("level", node.getLevel());
 			nodeList.add(nodeObj);
 		}
 		
 		ret.put("flowId", flowId);
 		ret.put("nodes", nodeList);
 	}
 	
 	private void ajaxAddUserPermission(Project project, HashMap<String, Object> ret, HttpServletRequest req, User user) throws ServletException {
 		String username = getParam(req, "username");
 		UserManager userManager = getApplication().getUserManager();
 		if (!userManager.validateUser(username)) {
 			ret.put("error", "User is invalid.");
 			return;
 		}
 		if (project.getUserPermission(username) != null) {
 			ret.put("error", "User permission already exists.");
 			return;
 		}
 		
 		boolean admin = Boolean.parseBoolean(getParam(req, "permissions[admin]"));
 		boolean read = Boolean.parseBoolean(getParam(req, "permissions[read]"));
 		boolean write = Boolean.parseBoolean(getParam(req, "permissions[write]"));
 		boolean execute = Boolean.parseBoolean(getParam(req, "permissions[execute]"));
 		boolean schedule = Boolean.parseBoolean(getParam(req, "permissions[schedule]"));
 		
 		Permission perm = new Permission();
 		if (admin) {
 			perm.setPermission(Type.ADMIN, true);
 		}
 		else {
 			perm.setPermission(Type.READ, read);
 			perm.setPermission(Type.WRITE, write);
 			perm.setPermission(Type.EXECUTE, execute);
 			perm.setPermission(Type.SCHEDULE, schedule);
 		}
 		
 		project.setUserPermission(username, perm);
 		project.info("User '" + user.getUserId() + "' has added user '" + username + "' to the project with permission " + perm.toString());
 		try {
 			projectManager.commitProject(project.getName());
 		} catch (ProjectManagerException e) {
 			ret.put("error", e.getMessage());
 		}
 	}
 
 	
 	private void ajaxChangePermissions(Project project, HashMap<String, Object> ret, HttpServletRequest req, User user) throws ServletException {
 		boolean admin = Boolean.parseBoolean(getParam(req, "permissions[admin]"));
 		boolean read = Boolean.parseBoolean(getParam(req, "permissions[read]"));
 		boolean write = Boolean.parseBoolean(getParam(req, "permissions[write]"));
 		boolean execute = Boolean.parseBoolean(getParam(req, "permissions[execute]"));
 		boolean schedule = Boolean.parseBoolean(getParam(req, "permissions[schedule]"));
 		
 		String username = getParam(req, "username");
 		Permission perm = project.getUserPermission(username);
 		if (perm == null) {
 			ret.put("error", "Permissions for " + username + " cannot be found.");
 			return;
 		}
 		
 		if (admin) {
 			perm.setPermission(Type.ADMIN, true);
 		}
 		else {
 			perm.setPermission(Type.READ, read);
 			perm.setPermission(Type.WRITE, write);
 			perm.setPermission(Type.EXECUTE, execute);
 			perm.setPermission(Type.SCHEDULE, schedule);
 		}
 		project.info("User '" + user.getUserId() + "' has changed permissions for '" + username + "' to " + perm.toString());
 
 		try {
 			projectManager.commitProject(project.getName());
 		} catch (ProjectManagerException e) {
 			ret.put("error", e.getMessage());
 		}
 	}
 	
 	private void ajaxGetPermissions(Project project, HashMap<String, Object> ret) {
 		ArrayList<HashMap<String, Object>> permissions = new ArrayList<HashMap<String, Object>>();
 		for(Pair<String, Permission> perm: project.getUserPermissions()) {
 			HashMap<String, Object> permObj = new HashMap<String, Object>();
 			String userId = perm.getFirst();
 			permObj.put("username", userId);
 			permObj.put("permission", perm.getSecond().toStringArray());
 			
 			permissions.add(permObj);
 		}
 		
 		ret.put("permissions", permissions);
 	}
 	
 	private void handleProjectLogsPage(HttpServletRequest req, HttpServletResponse resp, Session session) throws ServletException, IOException {
 		Page page = newPage(req, resp, session, "azkaban/webapp/servlet/velocity/projectlogpage.vm");
 		User user = session.getUser();
 		String projectName = getParam(req, "project");
 		
 		Project project = projectManager.getProject(projectName);
 		if (project == null) {
 			page.add("errorMsg", "Project " + projectName + " doesn't exist.");
 		}
 		page.add("projectName", projectName);
 		//page.add("projectManager", projectManager);
 		int bytesSkip = 0;
 		int numBytes = 1024;
 
 		// Really sucks if we do a lot of these because it'll eat up memory fast. But it's expected
 		// that this won't be a heavily used thing. If it is, then we'll revisit it to make it more stream
 		// friendly.
 		StringBuffer buffer = new StringBuffer(numBytes);
 		page.add("log", buffer.toString());
 
 		page.render();
 	}
 	
 	private void handlePermissionPage(HttpServletRequest req, HttpServletResponse resp, Session session) throws ServletException {
 		Page page = newPage(req, resp, session, "azkaban/webapp/servlet/velocity/permissionspage.vm");
 		String projectName = getParam(req, "project");
 		User user = session.getUser();
 		
 		Project project = null;
 		try {
 			project = projectManager.getProject(projectName);
 			if (project == null) {
 				page.add("errorMsg", "Project " + projectName + " not found.");
 			}
 			else {
 				page.add("project", project);
 				page.add("username", user.getUserId());
 				page.add("admins", Utils.flattenToString(project.getUsersWithPermission(Type.ADMIN), ","));
 				page.add("userpermission", project.getUserPermission(user));
 				page.add("permissions", project.getUserPermissions());
 				
 				if(project.hasPermission(user, Type.ADMIN)) {
 					page.add("isAdmin", true);
 				}
 			}
 		}
 		catch(AccessControlException e) {
 			page.add("errorMsg", e.getMessage());
 		}
 		
 		page.render();
 	}
 	
 	private void handleJobPage(HttpServletRequest req, HttpServletResponse resp, Session session) throws ServletException {
 		Page page = newPage(req, resp, session, "azkaban/webapp/servlet/velocity/jobpage.vm");
 		String projectName = getParam(req, "project");
 		String flowName = getParam(req, "flow");
 		String jobName = getParam(req, "job");
 		
 		User user = session.getUser();
 		Project project = null;
 		Flow flow = null;
 		try {
 			project = projectManager.getProject(projectName);
 			
 			if (project == null) {
 				page.add("errorMsg", "Project " + projectName + " not found.");
 			}
 			else {
 				if (!project.hasPermission(user, Type.READ)) {
 					throw new AccessControlException( "No permission to view project " + projectName + ".");
 				}
 				
 				page.add("project", project);
 				
 				flow = project.getFlow(flowName);
 				if (flow == null) {
 					page.add("errorMsg", "Flow " + flowName + " not found.");
 				}
 				else {
 					page.add("flowid", flow.getId());
 					
 					Node node = flow.getNode(jobName);
 					
 					if (node == null) {
 						page.add("errorMsg", "Job " + jobName + " not found.");
 					}
 					else {
 						Props prop = projectManager.getProperties(projectName, node.getJobSource());
 						page.add("jobid", node.getId());
 						page.add("jobtype", node.getType());
 						
 						ArrayList<String> dependencies = new ArrayList<String>();
 						Set<Edge> inEdges = flow.getInEdges(node.getId());
 						if (inEdges != null) {
 							for ( Edge dependency: inEdges ) {
 								dependencies.add(dependency.getSourceId());
 							}
 						}
 						if (!dependencies.isEmpty()) {
 							page.add("dependencies", dependencies);
 						}
 						
 						ArrayList<String> dependents = new ArrayList<String>();
 						Set<Edge> outEdges = flow.getOutEdges(node.getId());
 						if (outEdges != null) {
 							for ( Edge dependent: outEdges ) {
 								dependents.add(dependent.getTargetId());
 							}
 						}
 						if (!dependents.isEmpty()) {
 							page.add("dependents", dependents);
 						}
 						
 						// Resolve property dependencies
 						String source = node.getPropsSource();
 						page.add("properties", source);
 
 						ArrayList<Pair<String,String>> parameters = new ArrayList<Pair<String, String>>();
 						// Parameter
 						for (String key : prop.getKeySet()) {
 							String value = prop.get(key);
 							parameters.add(new Pair<String,String>(key, value));
 						}
 						
 						page.add("parameters", parameters);
 					}
 				}
 			}
 		}
 		catch (AccessControlException e) {
 			page.add("errorMsg", e.getMessage());
 		} catch (ProjectManagerException e) {
 			page.add("errorMsg", e.getMessage());
 		}
 		
 		page.render();
 	}
 	
 	private void handleFlowPage(HttpServletRequest req, HttpServletResponse resp, Session session) throws ServletException {
 		Page page = newPage(req, resp, session, "azkaban/webapp/servlet/velocity/flowpage.vm");
 		String projectName = getParam(req, "project");
 		String flowName = getParam(req, "flow");
 
 		User user = session.getUser();
 		Project project = null;
 		Flow flow = null;
 		try {
 			project = projectManager.getProject(projectName);
 			
 			if (project == null) {
 				page.add("errorMsg", "Project " + projectName + " not found.");
 			}
 			else {
 				if (!project.hasPermission(user, Type.READ)) {
 					throw new AccessControlException( "No permission Project " + projectName + ".");
 				}
 				
 				page.add("project", project);
 				
 				flow = project.getFlow(flowName);
 				if (flow == null) {
 					page.add("errorMsg", "Flow " + flowName + " not found.");
 				}
 				
 				page.add("flowid", flow.getId());
 			}
 		}
 		catch (AccessControlException e) {
 			page.add("errorMsg", e.getMessage());
 		}
 		
 		page.render();
 	}
 
 	private void handleProjectPage(HttpServletRequest req, HttpServletResponse resp, Session session) throws ServletException {
 		Page page = newPage(req, resp, session, "azkaban/webapp/servlet/velocity/projectpage.vm");
 		String projectName = getParam(req, "project");
 		
 		User user = session.getUser();
 		Project project = null;
 		try {
 			project = projectManager.getProject(projectName);
 			if (project == null) {
 				page.add("errorMsg", "Project " + projectName + " not found.");
 			}
 			else {
 				if (project.hasPermission(user, Type.ADMIN)) {
 					page.add("admin", true);
 				}
 
 				if (!project.hasPermission(user, Type.READ)) {
 					throw new AccessControlException( "No permission to view project " + projectName + ".");
 				}
 				
 				page.add("project", project);
 				page.add("admins", Utils.flattenToString(project.getUsersWithPermission(Type.ADMIN), ","));
 				page.add("userpermission", project.getUserPermission(user));
 	
 				List<Flow> flows = project.getFlows();
 				if (!flows.isEmpty()) {
 					Collections.sort(flows, FLOW_ID_COMPARATOR);
 					page.add("flows", flows);
 				}
 			}
 		}
 		catch (AccessControlException e) {
 			page.add("errorMsg", e.getMessage());
 		}
 		page.render();
 	}
 
 	private void handleCreate(HttpServletRequest req, HttpServletResponse resp, Session session) throws ServletException {
 		String projectName = hasParam(req, "name") ? getParam(req, "name") : null;
 		String projectDescription = hasParam(req, "description") ? getParam(req, "description") : null;
 		logger.info("Create project " + projectName);
 		
 		User user = session.getUser();
 		
 		String status = null;
 		String action = null;
 		String message = null;
 		HashMap<String, Object> params = null;
 		try {
 			projectManager.createProject(projectName, projectDescription, user);
 			status = "success";
 			action = "redirect";
 			String redirect = "manager?project=" + projectName;
 			params = new HashMap<String, Object>();
 			params.put("path", redirect);
 		} catch (ProjectManagerException e) {
 			message = e.getMessage();
 			status = "error";
 		}
 
 		String response = createJsonResponse(status, message, action, params);
 		try {
 			Writer write = resp.getWriter();
 			write.append(response);
 			write.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void handleUpload(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> multipart, Session session) throws ServletException, IOException {
 		User user = session.getUser();
 		String projectName = (String) multipart.get("project");
 		Project project = projectManager.getProject(projectName);
 		
 		if (projectName == null || projectName.isEmpty()) {
 			setErrorMessageInCookie(resp, "No project name found.");
 		}
 		else if (project == null) {
 			setErrorMessageInCookie(resp, "Installation Failed. Project '" + projectName + "' doesn't exist.");
 		}
 		else if (!project.hasPermission(user, Type.WRITE)) {
 			setErrorMessageInCookie(resp, "Installation Failed. User '" + user.getUserId() + "' does not have write access.");
 		}
 		else {
 			FileItem item = (FileItem) multipart.get("file");
 			String forceStr = (String) multipart.get("force");
 			boolean force = forceStr == null ? false : Boolean.parseBoolean(forceStr);
 			File projectDir = null;
 			if (projectName == null || projectName.isEmpty()) {
 				setErrorMessageInCookie(resp, "No project name found.");
 			}
 			else if (item == null) {
 				setErrorMessageInCookie(resp, "No file found.");
 			}
 			else {
 				try {
 					projectDir = extractFile(item);
 					projectManager.uploadProject(projectName, projectDir, user, force);
 					setSuccessMessageInCookie(resp, "Project Uploaded");
 				} 
 				catch (Exception e) {
 					logger.info("Installation Failed.", e);
 					project.error("Upload by '" + user.getUserId() + "' failed: " + e.getMessage());
 					setErrorMessageInCookie(resp, "Installation Failed.\n" + e.getMessage());
 				}
 				
 				if (projectDir != null && projectDir.exists() ) {
 					FileUtils.deleteDirectory(projectDir);
 				}
 				project.error("New project files uploaded by '" + user.getUserId() + "'");
 			}
 		}
 		resp.sendRedirect(req.getRequestURI() + "?project=" + projectName);
 	}
 
 	private File extractFile(FileItem item) throws IOException, ServletException {
 		final String contentType = item.getContentType();
 		if (contentType.startsWith("application/zip")) {
 			return unzipFile(item);
 		}
 		
 		throw new ServletException(String.format("Unsupported file type[%s].", contentType));
 	}
 
 	private File unzipFile(FileItem item) throws ServletException, IOException {
 		File temp = File.createTempFile("job-temp", ".zip");
 		temp.deleteOnExit();
 		OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
 		IOUtils.copy(item.getInputStream(), out);
 		out.close();
 		ZipFile zipfile = new ZipFile(temp);
 		File unzipped = Utils.createTempDir(tempDir);
 		Utils.unzip(zipfile, unzipped);
 		temp.delete();
 		return unzipped;
 	}
 
 	private static class NodeLevelComparator implements Comparator<Node> {
 		@Override
 		public int compare(Node node1, Node node2) {
 			return node1.getLevel() - node2.getLevel();
 		}
 	}
 }
