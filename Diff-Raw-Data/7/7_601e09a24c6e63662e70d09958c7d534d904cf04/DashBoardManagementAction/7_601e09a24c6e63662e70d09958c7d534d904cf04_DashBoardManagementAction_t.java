 package module.dashBoard.presentationTier;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import module.dashBoard.WidgetRegister;
 import module.dashBoard.domain.DashBoardColumnBean;
 import module.dashBoard.domain.DashBoardPanel;
 import module.dashBoard.domain.DashBoardWidget;
 import module.dashBoard.domain.UserDashBoardPanel;
 import module.dashBoard.widgets.WidgetController;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.presentationTier.Context;
 import myorg.presentationTier.LayoutContext;
 import myorg.presentationTier.actions.ContextBaseAction;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.GenericChecksumRewriter;
 import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.RequestChecksumFilter;
 import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.RequestChecksumFilter.ChecksumPredicate;
 import pt.ist.fenixWebFramework.servlets.json.JsonObject;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 import pt.ist.fenixframework.pstm.AbstractDomainObject;
 
 @Mapping(path = "/dashBoardManagement")
 public class DashBoardManagementAction extends ContextBaseAction {
 
     static {
 	RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {
 	    public boolean shouldFilter(HttpServletRequest httpServletRequest) {
 		return !(httpServletRequest.getRequestURI().endsWith("/dashBoardManagement.do")
 			&& httpServletRequest.getQueryString() != null && (httpServletRequest.getQueryString().contains(
 			"method=order")
 			|| httpServletRequest.getQueryString().contains("method=requestWidgetHelp")
 			|| httpServletRequest.getQueryString().contains("method=removeWidgetFromColumn")
 			|| httpServletRequest.getQueryString().contains("method=viewWidget") || httpServletRequest
 			.getQueryString().contains("method=editWidget")
 
 		));
 	    }
 	});
     }
 
     public ActionForward order(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws Exception {
 
 	DashBoardPanel panel = getDomainObject(request, "dashBoardId");
 
 	List<DashBoardColumnBean> beans = new ArrayList<DashBoardColumnBean>();
 	String modification = request.getParameter("ordering");
 	String[] columns = modification.split(",");
 
 	for (String column : columns) {
 	    String[] values = column.split(":");
 	    int columnIndex = Integer.valueOf(values[0]);
 	    DashBoardColumnBean dashBoardColumnBean = new DashBoardColumnBean(columnIndex);
 	    if (values.length == 2) {
 		dashBoardColumnBean.setWidgets(getWidgets(values[1]));
 	    }
 	    beans.add(dashBoardColumnBean);
 	}
 
 	panel.edit(beans);
 	return null;
     }
 
     private List<DashBoardWidget> getWidgets(String column) {
 	List<DashBoardWidget> widgetsInColumn = new ArrayList<DashBoardWidget>();
 	for (String externalId : column.substring(0, column.length()).split(" ")) {
 	    DashBoardWidget widget = AbstractDomainObject.fromExternalId(externalId);
 	    widgetsInColumn.add(widget);
 	}
 	return widgetsInColumn;
     }
 
     public static ActionForward forwardToDashBoard(WidgetRequest request) {
 	return forwardToDashBoard(request.getPanel(), request.getRequest());
     }
 
     public static ActionForward forwardToDashBoard(DashBoardPanel panel, HttpServletRequest request) {
 	ActionForward forward = new ActionForward();
 	forward.setRedirect(true);
 	String realPath = "/dashBoardManagement.do?method=viewDashBoardPanel&dashBoardId=" + panel.getExternalId() + "&"
 		+ CONTEXT_PATH + "=" + getContext(request).getPath();
 	forward.setPath(realPath + "&" + GenericChecksumRewriter.CHECKSUM_ATTRIBUTE_NAME + "="
 		+ GenericChecksumRewriter.calculateChecksum(request.getContextPath() + realPath));
 	return forward;
     }
 
     public ActionForward viewWidget(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	DashBoardWidget widget = getDomainObject(request, "dashBoardWidgetId");
 	UserDashBoardPanel panel = (UserDashBoardPanel) widget.getDashBoardPanel();
 	User currentUser = UserView.getCurrentUser();
 
 	if (panel.getUser() != currentUser) {
 	    throw new RuntimeException("go take a hike!");
 	}
 
 	WidgetRequest widgetRequest = new WidgetRequest(request, response, widget, currentUser);
 	widget.getWidgetController().doView(widgetRequest);
 
	response.setHeader("Cache-Control", "no-cache");
	response.setHeader("Pragma", "no-cache");
	response.setDateHeader("Expires", 0);
 	request.setAttribute("widget", widget);
 	WidgetLayoutContext context = new WidgetLayoutContext(widget);
 	return context.forward();
     }
 
     public ActionForward editWidget(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	DashBoardWidget widget = getDomainObject(request, "dashBoardWidgetId");
 	UserDashBoardPanel panel = (UserDashBoardPanel) widget.getDashBoardPanel();
 	User currentUser = UserView.getCurrentUser();
 
 	if (panel.getUser() != currentUser) {
 	    throw new RuntimeException("go take a hike!");
 	}
 
 	WidgetRequest widgetRequest = new WidgetRequest(request, response, widget, currentUser);
 	widget.getWidgetController().doEdit(widgetRequest);
 
	response.setHeader("Cache-Control", "no-cache");
	response.setHeader("Pragma", "no-cache");
	response.setDateHeader("Expires", 0);
 	request.setAttribute("widget", widget);
 	WidgetLayoutContext context = new WidgetLayoutContext(widget);
 	return context.forward();
     }
 
     public ActionForward viewDashBoardPanel(final DashBoardPanel panel, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	request.setAttribute("dashBoard", panel);
 	return forward(request, "/dashBoardPanel/viewDashBoard.jsp");
     }
 
     public ActionForward viewDashBoardPanel(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	DashBoardPanel panel = getDomainObject(request, "dashBoardId");
 	return viewDashBoardPanel(panel, request, response);
     }
 
     public ActionForward removeWidgetFromColumn(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws IOException {
 
 	JsonObject jsonObject = new JsonObject();
 	try {
 	    DashBoardWidget widget = getDomainObject(request, "dashBoardWidgetId");
 	    widget.delete();
 	    jsonObject.addAttribute("status", "OK");
 	} catch (Exception e) {
 	    jsonObject.addAttribute("status", "NOT_OK");
 	}
 	writeJsonReply(response, jsonObject);
 	return null;
     }
 
     public ActionForward prepareAddWidget(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	DashBoardPanel panel = getDomainObject(request, "dashBoardId");
 	request.setAttribute("dashBoard", panel);
 	request.setAttribute("widgets", WidgetRegister.getAvailableWidgets(panel, UserView.getCurrentUser()));
 	return forward(request, "/dashBoardPanel/addWidget.jsp");
     }
 
     public ActionForward addWidget(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	DashBoardPanel panel = getDomainObject(request, "dashBoardId");
 	String widgetClassName = request.getParameter("dashBoardWidgetClass");
 	Class<? extends WidgetController> className = null;
 	try {
 	    className = (Class<? extends WidgetController>) Class.forName(widgetClassName);
 	    DashBoardWidget widget = DashBoardWidget.newWidget(className);
 	    panel.addWidgetToColumn(0, widget);
 	} catch (Exception e) {
 	    addMessage(request, "error.addingWidget");
 	    e.printStackTrace();
 	    return viewDashBoardPanel(panel, request, response);
 	}
 
 	return forwardToDashBoard(panel, request);
     }
 
     public ActionForward widgetSubmition(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	DashBoardWidget widget = getDomainObject(request, "dashBoardWidgetId");
 	WidgetController strutsController = widget.getWidgetController();
 	return strutsController.doSubmit(new WidgetRequest(request, response, widget, UserView.getCurrentUser()));
     }
 
     public ActionForward requestWidgetEdit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	DashBoardWidget widget = getDomainObject(request, "dashBoardWidgetId");
 	DashBoardPanel panel = widget.getDashBoardPanel();
 
 	WidgetController strutsController = widget.getWidgetController();
 	strutsController.doEdit(new WidgetRequest(request, response, widget, UserView.getCurrentUser()));
 
 	return viewDashBoardPanel(panel, request, response);
     }
 
     public ActionForward requestWidgetHelp(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws IOException {
 
 	DashBoardWidget widget = getDomainObject(request, "dashBoardWidgetId");
 	DashBoardPanel panel = widget.getDashBoardPanel();
 
 	JsonObject jsonObject = new JsonObject();
 
 	if (panel.isAccessibleToCurrentUser()) {
 	    jsonObject.addAttribute("helpText", widget.getWidgetController().getHelp());
 	}
 
 	writeJsonReply(response, jsonObject);
 	return null;
     }
 
     public final ActionForward doTest(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	DashBoardPanel panel = Test();
 
 	return forwardToDashBoard(panel, request);
     }
 
     @Service
     private DashBoardPanel Test() {
 
 	User user = UserView.getCurrentUser();
 	if (!user.getUserDashBoards().isEmpty()) {
 	    return user.getUserDashBoards().get(0);
 	} else {
 	    throw new RuntimeException("No DashBoard Panel Found!");
 	}
     }
 }
