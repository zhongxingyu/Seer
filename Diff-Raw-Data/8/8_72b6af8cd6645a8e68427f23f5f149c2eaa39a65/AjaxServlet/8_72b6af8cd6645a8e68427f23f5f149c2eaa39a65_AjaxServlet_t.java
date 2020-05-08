 /*
  * Copyright 2009 Luigi Dell'Aquila (luigi.dellaquila--at--assetdata.it)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.romaframework.aspect.view.html;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.xml.bind.ValidationException;
 
import org.json.CssJSONObject;
 import org.json.JSONException;
 import org.json.JSONObject;
import org.json.JsJSONObject;
 import org.romaframework.aspect.view.ViewAspect;
 import org.romaframework.aspect.view.command.impl.DownloadReaderViewCommand;
 import org.romaframework.aspect.view.command.impl.DownloadStreamViewCommand;
 import org.romaframework.aspect.view.command.impl.OpenWindowViewCommand;
 import org.romaframework.aspect.view.command.impl.RedirectViewCommand;
 import org.romaframework.aspect.view.command.impl.ReportingDownloadViewCommand;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenAreaInstance;
 import org.romaframework.aspect.view.html.component.HtmlViewAbstractComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewConfigurableEntityForm;
 import org.romaframework.aspect.view.html.component.HtmlViewContentForm;
 import org.romaframework.aspect.view.html.component.HtmlViewGenericComponent;
 import org.romaframework.aspect.view.html.screen.HtmlViewScreen;
 import org.romaframework.core.Roma;
 import org.romaframework.core.binding.BindingException;
 import org.romaframework.core.domain.type.TreeNode;
 import org.romaframework.core.exception.ExceptionHelper;
 import org.romaframework.core.exception.UserException;
 import org.romaframework.frontend.domain.message.ErrorMessageTextDetail;
 
 public class AjaxServlet extends HtmlServlet {
 	@Override
 	public void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
 		// long time = System.currentTimeMillis();
 		try {
 			HttpSession httpSession = request.getSession(true);
 
 			if (!isStarted(httpSession)) {
 				try {
 					log.info("trying to navigate on expired session!!!");
 					// toDeInit = false;
 					sendStopAjaxResponse(response, "");
 					httpSession.invalidate();
 
 				} catch (Exception e) {
 					// log.error("unable to redirect ", e);
 				}
 				return;
 				// httpSession = request.getSession(true);
 				// startUserSession(request, response);
 				//
 				// setStarted(httpSession);
 			}
 
 			HtmlViewScreen screen = (HtmlViewScreen) Roma.aspect(ViewAspect.class).getScreen();
 			String oldScreenName = screen.getName();
 
 			final String pageId = (String) request.getSession().getAttribute(PAGE_ID_PARAM);
 			request.setAttribute(PAGE_ID_PARAM, pageId);
 
 			HtmlViewAspectHelper.getCssBuffer().open();
 
 			removePreviousPushCommands(request);
 
 			// Add the request and the response to the ThreadLocal
 
 			final RequestParser requestParser = Roma.component(RequestParser.class);
 
 			boolean fieldsBound = false;
 			try {
 
 				response.setContentType("text/html");
 				response.setCharacterEncoding("UTF-8");
 				// response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate"); //HTTP 1.1
 				response.addHeader("Cache-Control", "must-revalidate");
 				response.addHeader("Cache-Control", "no-cache");
 				response.addHeader("Cache-Control", "no-store");
 				response.setHeader("Pragma", "no-cache"); // HTTP 1.0
 				response.setDateHeader("Expires", 0); // prevents caching at the proxy server
 				fieldsBound = requestParser.parseRequest(request);
 			} catch (final ValidationException e) {
 				ErrorMessageTextDetail toShow = new ErrorMessageTextDetail("application.error", "Application Error", null, e.getMessage(), e);
 				toShow.setDetail(ExceptionHelper.toString(e));
 				log.info(e.getMessage(), e);
 				Roma.flow().forward(toShow, "screen:popup:error");
 			} catch (final BindingException e) {
 				ErrorMessageTextDetail toShow = new ErrorMessageTextDetail("application.error", "Application Error", null, e.getMessage(), e);
 				toShow.setDetail(ExceptionHelper.toString(e));
 				log.warn(e.getMessage(), e);
 				Roma.flow().forward(toShow, "screen:popup:error");
 			} catch (UserException e) {
 				ErrorMessageTextDetail toShow = new ErrorMessageTextDetail("application.error", "Application Error", null, e.getMessage(), e);
 				toShow.setDetail(ExceptionHelper.toString(e));
 				log.debug(e.getMessage(), e);
 				Roma.flow().forward(toShow, "screen:popup:error");
 			} catch (final Throwable e) {
 				ErrorMessageTextDetail toShow = new ErrorMessageTextDetail("application.error", "Application Error", e);
 				toShow.setDetail(ExceptionHelper.toString(e));
 				log.warn(e.getMessage(), e);
 				Roma.flow().forward(toShow, "screen:popup:error");
 				// throw new ServletException(e);
 			}
 
 			Boolean redirected = (Boolean) request.getAttribute(HtmlViewAspectHelper.REDIRECTED);
 			if (redirected != null && redirected) {
 				request.setAttribute(HtmlViewAspectHelper.REDIRECTED, Boolean.FALSE);
 				return;
 			}
 
 			screen = (HtmlViewScreen) Roma.view().getScreen();
 
 			try {
 
 				if (mustReload(screen, oldScreenName)) {
 					sendReloadAjaxResponse(response, pageId);
 				} else {
					JSONObject obj = new JsJSONObject();
					JSONObject changes = new CssJSONObject();
 					obj.put("bindingExecuted", fieldsBound);
 					obj.put("status", "ok");
 					obj.put("pageId", pageId);
 					// StringBuffer buffer = new StringBuffer();
 
 					for (Map.Entry<String, ComponentWritable> entry : getChanges(screen).entrySet()) {
 						changes.put(entry.getKey(), entry.getValue());
 					}
 					obj.put("changes", changes);
 
 					addPushCommands(obj, request);
 
 					obj.write(response.getWriter());
 
 				}
 
 				((HtmlViewAspect) Roma.view()).cleanDirtyComponents();
 
 			} catch (JSONException jsonx) {
 				jsonx.printStackTrace();
 			}
 		} finally {
 			HtmlViewAspectHelper.removeCssBuffer();
 			HtmlViewAspectHelper.removeJsBuffer();
 		}
 		// log.info("request executed in (millis): " + (System.currentTimeMillis() - time));
 	}
 
 	private void sendReloadAjaxResponse(final HttpServletResponse response, final String pageId) throws JSONException, IOException {
 		JSONObject obj = new JSONObject();
 		obj.put("status", "reload");
 		obj.put("pageId", pageId);
 		response.getWriter().write(obj.toString());
 	}
 
 	private void sendStopAjaxResponse(final HttpServletResponse response, final String pageId) throws JSONException, IOException {
 		JSONObject obj = new JSONObject();
 		obj.put("status", "stop");
 		obj.put("pageId", pageId);
 		response.getWriter().write(obj.toString());
 	}
 
 	private void addPushCommands(JSONObject obj, final HttpServletRequest request) throws JSONException {
 		DownloadStreamViewCommand command = (DownloadStreamViewCommand) request.getSession().getAttribute(DownloadStreamViewCommand.class.getSimpleName());
 		if (command != null) {
 			if (command.getFileName() != null) {
 				obj.put("pushDownloadStream", command.getFileName());
 			} else {
 				obj.put("pushDownloadStream", "download");
 			}
 		}
 
 		DownloadReaderViewCommand pushReaderCommand = (DownloadReaderViewCommand) request.getSession()
 				.getAttribute(DownloadReaderViewCommand.class.getSimpleName());
 		if (pushReaderCommand != null) {
 			if (pushReaderCommand.getFileName() != null) {
 				obj.put("pushDownloadReader", pushReaderCommand.getFileName());
 			} else {
 				obj.put("pushDownloadReader", "download");
 			}
 		}
 
 		ReportingDownloadViewCommand pushDownloadReport = (ReportingDownloadViewCommand) request.getSession().getAttribute(
 				ReportingDownloadViewCommand.class.getSimpleName());
 		if (pushDownloadReport != null) {
 			if (pushDownloadReport.getFileName() != null) {
 				obj.put("pushDownloadReport", pushDownloadReport.getFileName());
 			} else {
 				obj.put("pushDownloadReport", "download");
 			}
 		}
 
 		OpenWindowViewCommand pushOpenWindowCommand = (OpenWindowViewCommand) request.getSession().getAttribute(OpenWindowViewCommand.class.getSimpleName());
 		if (pushOpenWindowCommand != null) {
 			JSONObject pushOpenJSONObject = new JSONObject();
 			pushOpenJSONObject.put("location", pushOpenWindowCommand.getLocation());
 			pushOpenJSONObject.put("name", pushOpenWindowCommand.getName());
 			pushOpenJSONObject.put("options", pushOpenWindowCommand.getOptions());
 			obj.put("pushOpenWindow", pushOpenJSONObject);
 		}
 
 		RedirectViewCommand pushRedirectViewCommand = (RedirectViewCommand) request.getSession().getAttribute(RedirectViewCommand.class.getSimpleName());
 		if (pushRedirectViewCommand != null) {
 			JSONObject pushOpenJSONObject = new JSONObject();
 			pushOpenJSONObject.put("location", pushRedirectViewCommand.getLocation());
 			pushOpenJSONObject.put("name", pushRedirectViewCommand.getName());
 			pushOpenJSONObject.put("options", pushRedirectViewCommand.getOptions());
 			obj.put("pushRedirectView", pushOpenJSONObject);
 		}
 	}
 
 	private boolean mustReload(HtmlViewScreen currentScreen, String oldScreenName) {
 		if (!currentScreen.getName().equals(oldScreenName)) {
 			return true;
 		}
 		return false;
 	}
 
 	protected Map<String, ComponentWritable> getChanges(HtmlViewScreen screen) throws IOException {
 		Map<String, ComponentWritable> buffer = new HashMap<String, ComponentWritable>();
 		synchronized (screen) {
 			for (TreeNode child : screen.getRootArea().getChildren()) {
 				if (child instanceof HtmlViewScreenAreaInstance) {
 					buffer.putAll(getScreenAreaChanges((HtmlViewScreenAreaInstance) child));
 				} else if (child instanceof HtmlViewAbstractComponent) {
 					buffer.putAll(getComponentChanges((HtmlViewAbstractComponent) child));
 				}
 			}
 		}
 		return buffer;
 	}
 
 	protected Map<String, ComponentWritable> getScreenAreaChanges(HtmlViewScreenAreaInstance area) throws IOException {
 		Map<String, ComponentWritable> buffer = new HashMap<String, ComponentWritable>();
 		if (area.isDirty()) {
 			buffer.put(area.getHtmlId(), new ComponentWritable(area));
 		} else {
 			HtmlViewContentForm form = area.getForm();
 			if (form != null && form instanceof HtmlViewConfigurableEntityForm) {
 				buffer.putAll(getFormChanges((HtmlViewConfigurableEntityForm) form));
 			} else if (area.getChildren() != null) {
 				for (Object child : area.getChildren()) {
 					HtmlViewScreenAreaInstance childArea = (HtmlViewScreenAreaInstance) child;
 					buffer.putAll(getScreenAreaChanges(childArea));
 				}
 			}
 		}
 		return buffer;
 	}
 
 	protected Map<String, ComponentWritable> getComponentChanges(HtmlViewAbstractComponent component) throws IOException {
 		Map<String, ComponentWritable> buffer = new HashMap<String, ComponentWritable>();
 		if (component.isDirty() && component.getTransformer() != null) {
 			buffer.put(component.getHtmlId(), new ComponentWritable(component));
 		} else if (component.getChildren() != null) {
 			for (HtmlViewGenericComponent child : component.getChildren()) {
 				if (child instanceof HtmlViewAbstractComponent)
 					buffer.putAll(getComponentChanges((HtmlViewAbstractComponent) child));
 				else if (child instanceof HtmlViewConfigurableEntityForm)
 					buffer.putAll(getFormChanges((HtmlViewConfigurableEntityForm) child));
 			}
 		}
 		return buffer;
 	}
 
 	protected Map<String, ComponentWritable> getFormChanges(HtmlViewConfigurableEntityForm component) throws IOException {
 		Map<String, ComponentWritable> buffer = new HashMap<String, ComponentWritable>();
 		if (component.isDirty()) {
 			buffer.put(component.getHtmlId(), new ComponentWritable(component));
 		} else if (component.getChildren() != null) {
 			for (HtmlViewGenericComponent child : component.getChildren()) {
 				if (child instanceof HtmlViewAbstractComponent)
 					buffer.putAll(getComponentChanges((HtmlViewAbstractComponent) child));
 				else if (child instanceof HtmlViewConfigurableEntityForm)
 					buffer.putAll(getFormChanges((HtmlViewConfigurableEntityForm) child));
 			}
 		}
 		return buffer;
 	}
 
 }
