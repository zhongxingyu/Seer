 package core.web.controller;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import core.browse.Node;
 import core.config.ResourceConfig;
 import core.model.Resource;
 import core.model.ResourceKind;
 import core.model.ResourceUtil;
 import core.security.AccessRight;
 import core.security.ResourceGrants;
 import core.util.CollectionUtil;
 import core.util.Validator;
 import core.util.WebUtil;
 import core.web.util.Action;
 import core.web.util.Breadcrumbs;
 import core.web.util.PostState;
 import core.web.util.StatusMessage;
 import core.web.util.FileUpload;
 import core.web.util.StatusMessage.Type;
 
 /**
  * Controller for repository browsing.
  * 
  * @author jani
  * 
  */
 @ControllerName("browse")
 public class BrowseController extends Controller {
 	private static final String RESOURCE_EDIT_KEY = "resourceedit";
 
 	private Map<String, String> post;
 	private Map<String, FileUpload> files;
 	private Breadcrumbs breadcrumbs;
 	private Node node;
 
 	@Override
 	public void init() {
 		post = new HashMap<String, String>();
 		files = new HashMap<String, FileUpload>();
 
 		String path = (String) getRequest().getAttribute("path");
 		String login = getServlet().getCurrentLogin();
 		node = new Node(getServlet().getConfig().getSeriesRoot(), path, login);
 
 		breadcrumbs = new Breadcrumbs();
 		breadcrumbs.add("browse", "browse");
 		breadcrumbs.add(node.getParts());
 
 	}
 
 	@Override
 	public void get() {
 		Action action = Action.fromString(getRequest().getParameter("action"));
 		PostState state = PostState.get(getSession());
 
 		switch (action) {
 		case EDIT:
 		case VIEW:
 			if (node.isRoot()) {
 				viewRoot();
 			} else {
 				Resource edit = state.get(RESOURCE_EDIT_KEY);
 				viewResource(action, edit);
 			}
 			break;
 		case DOWNLOAD:
 			download();
 			break;
 		}
 
 		getView().put("template", "browse");
 		getView().put("action", action.getName());
 		getView().put(state.getAll());
 		getView().put("breadcrumbs", breadcrumbs);
 	}
 
 	@Override
 	public void post() {
 		WebUtil.parseRequest(getRequest(), post, files);
 
 		Action action = Action.fromString(getRequest().getParameter("action"));
 
 		if (post.containsKey("action")) {
 			action = Action.fromString(post.get("action"));
 		}
 
 		PostState state = new PostState();
 		StatusMessage status = new StatusMessage(Type.SUCCESS, "");
 		String destination = "";
 		switch (action) {
 		case ADD:
 			destination = addResource(state, status);
 			break;
 		case EDIT:
 			destination = edit(state, status);
 			break;
 		case DELETE:
 			destination = delete(status);
 			break;
 		case ADD_DIR:
 			destination = addDirectory(state, status);
 			break;
 		case ADD_FILE:
 			destination = addFile(status);
 			break;
 		case VALIDATE:
 			destination = validate(status);
 			break;
 		}
 
 		state.put("status", status);
 		state.save(getSession());
 
 		if (!destination.isEmpty()) {
 			destination = "/" + destination;
 		}
 		setRedirect("/browse" + destination);
 	}
 
 	private void download() {
 		boolean redirect = true;
 		byte[] file = node.getDownload();
 		String filename = node.getDownloadFilename();
 		if (file != null) {
 			try {
 				WebUtil.sendFile(file, filename, getResponse());
 				setRenderView(false);
 				redirect = false;
 			} catch (IOException e) {
 				// nothing
 			}
 		}
 		if (redirect) {
 			setRedirect("/browse/" + node.getPath());
 		}
 	}
 
 	private String addResource(PostState state, StatusMessage status) {
 		String name = post.get("newresource");
 		Validator validator = node.addResource(name);
 		status.setFromValidator(validator, "Resource successfully added");
 		if (!validator.isValid()) {
 			state.put("newresource", name);
 		}
 		return node.getPath();
 	}
 
 	private String addDirectory(PostState state, StatusMessage status) {
 		String name = post.get("newdir");
 		Validator validator = node.addDirectory(name);
 		status.setFromValidator(validator, "Directory successfully added");
 		if (!validator.isValid()) {
 			state.put("newdir", name);
 		}
 		return node.getPath();
 	}
 
 	private String addFile(StatusMessage status) {
 		FileUpload file = files.get("newfile");
 		Validator validator = node.addFile(file, false);
 		status.setFromValidator(validator, "File successfully added");
 		return node.getPath();
 	}
 
 	private String validate(StatusMessage status) {
 		Validator validator = node.validateResource();
 		status.setFromValidator(validator, "Resource is valid");
 		return node.getPath();
 	}
 
 	private String edit(PostState state, StatusMessage status) {
 		List<String> params = ResourceUtil.getResourceFormFields(node
 				.getResouceKind());
 		Map<String, Object> oldValues = CollectionUtil.filterMap(post, params,
 				"old_");
 		Map<String, Object> newValues = CollectionUtil.filterMap(post, params,
 				"new_");
 
		Validator validator = node.updateResource(oldValues, newValues);
 
 		String res = node.getResourcePath();
 
 		if (!validator.isValid()) {
 			res += "?action=edit";
 		}
 		status.setFromValidator(validator, "Resource successfully edited");
 		state.put(RESOURCE_EDIT_KEY, node.getResouce());
 		return res;
 	}
 
 	private String delete(StatusMessage status) {
 		String path = post.get("path");
 		Node node = new Node(getServlet().getConfig().getSeriesRoot(), path,
 				getServlet().getCurrentLogin());
 		Validator validator = node.delete();
 		status.setFromValidator(validator, "Resource successfully deleted");
 		if (validator.isValid()) {
 			return node.getParentPath();
 		}
 		return node.getPath();
 	}
 
 	private void viewRoot() {
 		getView().put("resourceTemplate", "root");
 
 		AccessRight access = node.getAccess();
 		boolean viewFull = access.includes(AccessRight.VIEW_FULL);
 
 		List<Resource> list = node.loadChildResources();
 
 		Map<String, ResourceGrants> grantsMap = ResourceGrants.createMap(
 				getServlet().getConfig().getSeriesRoot(), node
 						.getResourceDirectory(), list, getServlet()
 						.getCurrentLogin());
 
 		getView().put("hasViewFull", viewFull);
 		getView().put("list", list);
 		getView().put("grants", grantsMap);
 	}
 
 	private void viewResource(Action action, Resource posted) {
 		getView().put("resourceTemplate", node.getResouceKind().getName());
 
 		AccessRight access = node.getAccess();
 		boolean view = access.includes(AccessRight.VIEW);
 		boolean viewFull = access.includes(AccessRight.VIEW_FULL);
 
 		if (action == Action.EDIT && !viewFull) {
 			setRedirect("/browse/" + node.getPath());
 		}
 
 		Resource current = node.getResouce();
 		Resource resource = (posted == null ? current : posted);
 
 		if (view && resource != null) {
 			List<Resource> list = node.loadChildResources();
 
 			Map<String, ResourceGrants> grantsMap = ResourceGrants.createMap(
 					getServlet().getConfig().getSeriesRoot(), node
 							.getResourceDirectory(), list, getServlet()
 							.getCurrentLogin());
 
 			getView().put("list", list);
 			getView().put("grants", grantsMap);
 			getView().put("resourcePath", node.getResourcePath());
 
 			getView().put("files", node.getFiles());
 			getView().put("node", node);
 		}
 		getView().put("hasView", view);
 		getView().put("hasViewFull", viewFull);
 		getView().put("resource", resource);
 		getView().put("current", current);
 		getView().put("gradingStyles", ResourceConfig.GRADING_STYLES);
 		getView().put("problemCheckers", ResourceConfig.PROBLEM_CHECKERS);
 		getView().put("isProblem",
 				node.getResouceKind() == ResourceKind.PROBLEM);
 
 	}
 
 	@Override
 	public String getPageTitle() {
 		return "Browse";
 	}
 }
