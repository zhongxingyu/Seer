 /*
  * Copyright 2006 Giordano Maestro (giordano.maestro--at--assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.romaframework.aspect.view.html;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.TreeMap;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.flow.FlowAspect;
 import org.romaframework.aspect.validation.feature.ValidationActionFeatures;
 import org.romaframework.aspect.view.ViewAspect;
 import org.romaframework.aspect.view.feature.ViewActionFeatures;
 import org.romaframework.aspect.view.form.ViewComponent;
 import org.romaframework.aspect.view.html.actionhandler.EventHelper;
 import org.romaframework.aspect.view.html.area.HtmlViewBinder;
 import org.romaframework.aspect.view.html.area.HtmlViewRenderable;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenPopupAreaInstance;
 import org.romaframework.aspect.view.html.component.HtmlViewAbstractComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewActionComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewContentComponentImpl;
 import org.romaframework.aspect.view.html.component.HtmlViewGenericComponent;
 import org.romaframework.aspect.view.html.screen.HtmlViewScreen;
 import org.romaframework.aspect.view.html.transformer.helper.TransformerHelper;
 import org.romaframework.core.Roma;
 import org.romaframework.core.domain.type.Stream;
 import org.romaframework.core.flow.Controller;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaHelper;
 
 public class RequestParserImpl implements RequestParser {
 	protected static final int	ACTION_PART_CURRENT_ACTION_AREA	= 3;
 	protected static final int	ACTION_PART_ACTION_NAME					= 2;
 	protected static final int	ACTION_PART_OBJECT_ID						= 1;
 	public static final String	CLOSE_POPUP_EVENT_NAME					= "ClosePopup";
 
 	protected static final Log	log															= LogFactory.getLog(RequestParserImpl.class);
 
 	public boolean parseRequest(final HttpServletRequest request) throws Throwable {
 		final Map<String, Map<String, Object>> reqParams = groupParameters(request);
 		boolean result = false;
 		HtmlViewScreen screen = getScreen();
 		synchronized (screen) {
 			if (isBindableAction(reqParams)) {
 				bindValues(reqParams);
 				result = true;
 			}
 			screen.resetValidation();
 			invokeEvent(reqParams);
 			invokeAction(reqParams);
 		}
 		return result;
 	}
 
 	private boolean isBindableAction(Map<String, Map<String, Object>> reqParams) {
 		final HtmlViewSession session = HtmlViewAspectHelper.getHtmlViewSession();
 		final Map<String, Object> actionGroup = reqParams.get(TransformerHelper.POJO_ACTION_PREFIX);
 		if (actionGroup != null && actionGroup.size() > 0) {
 			final String action = actionGroup.entrySet().iterator().next().getKey();
 			final String[] actionParts = action.split(TransformerHelper.SEPARATOR);
 			final HtmlViewRenderable renderable = session.getRenderableById(Long.parseLong(actionParts[ACTION_PART_OBJECT_ID]));
 			if (renderable instanceof HtmlViewActionComponent) {
 				HtmlViewActionComponent actionComponent = (HtmlViewActionComponent) renderable;
 				final SchemaAction iAction = actionComponent.getActionField();
 				final Object bind = iAction.getFeature(ViewActionFeatures.BIND);
 				if (Boolean.FALSE.equals(bind)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	private void bindValues(final Map<String, Map<String, Object>> reqParams) {
 		final HtmlViewSession session = HtmlViewAspectHelper.getHtmlViewSession();
 		for (final String groupName : reqParams.keySet()) {
 			if (!(TransformerHelper.POJO_ACTION_PREFIX.equals(groupName) || TransformerHelper.POJO_EVENT_PREFIX.equals(groupName))) {
 				Scanner s = new Scanner(groupName);
 				if (s.hasNextLong()) {
 					final Long fieldId = s.nextLong();
 					final HtmlViewRenderable renderable = session.getRenderableById(fieldId);
 					if (renderable != null) {
 						final HtmlViewBinder binder = renderable.getTransformer().getBinder(renderable);
 						if (binder != null) {
 							// TODO handle BindingException
 							binder.bind(renderable, reqParams.get(groupName));
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void invokeAction(final Map<String, Map<String, Object>> reqParams) throws Throwable {
 		final HtmlViewSession session = HtmlViewAspectHelper.getHtmlViewSession();
 		final Map<String, Object> actionGroup = reqParams.get(TransformerHelper.POJO_ACTION_PREFIX);
 		if (actionGroup != null && actionGroup.size() > 0) {
 			final String action = actionGroup.entrySet().iterator().next().getKey();
 			final String[] actionParts = action.split(TransformerHelper.SEPARATOR);
 			final HtmlViewRenderable renderable = session.getRenderableById(Long.parseLong(actionParts[ACTION_PART_OBJECT_ID]));
 			if (renderable instanceof HtmlViewActionComponent) {
 				invokeSimpleAction(actionParts, (HtmlViewActionComponent) renderable);
 			} else if (renderable instanceof HtmlViewScreenPopupAreaInstance) {
 				Roma.aspect(FlowAspect.class).back();
 			} else if (renderable instanceof ViewComponent) {
 				// TODO remove this!!
 				// invokeAggregateAction(actionParts, (ViewComponent) renderable);
 			}
 		}
 	}
 
 	private HtmlViewRenderable invokeEvent(final Map<String, Map<String, Object>> reqParams) throws Throwable {
 		HtmlViewRenderable renderable = null;
 		final HtmlViewSession session = HtmlViewAspectHelper.getHtmlViewSession();
 		final Map<String, Object> actionGroup = reqParams.get(TransformerHelper.POJO_EVENT_PREFIX);
 		if (actionGroup != null && actionGroup.size() > 0) {
 			Map.Entry<String, Object> eventEntry = actionGroup.entrySet().iterator().next();
 			final String eventHtmlName = eventEntry.getKey();
 			String componentId = EventHelper.getComponentId(eventHtmlName);
 			renderable = session.getRenderableById(Long.parseLong(componentId));
 			String event = EventHelper.getEvent(eventHtmlName);
 			if (renderable instanceof HtmlViewScreenPopupAreaInstance && CLOSE_POPUP_EVENT_NAME.equals(event)) {
 				Roma.flow().back();
 			} else if (renderable instanceof HtmlViewGenericComponent) {
 				HtmlViewGenericComponent component = (HtmlViewGenericComponent) renderable;
 				if (event != null && event.endsWith("[]"))
 					event = event.substring(0, event.length() - 2);
 				Object result = null;
 				if (component.getSchemaField() != null) {
					result = SchemaHelper.invokeEvent(component.getContainerComponent().getContent(), component.getSchemaField().getName(), event,
							(Object[]) eventEntry.getValue());
 				} else {
 					result = SchemaHelper.invokeEvent(component.getContent(), event);
 				}
 				if (component instanceof HtmlViewContentComponentImpl) {
 					((HtmlViewContentComponentImpl) component).setAdditionalInfo(result);
 				}
 			}
 		}
 		return renderable;
 	}
 
 	/**
 	 * invokes an action that represents a class
 	 * 
 	 * @param actionParts
 	 * @param actionComponent
 	 * @throws Throwable
 	 */
 	private void invokeSimpleAction(final String[] actionParts, final HtmlViewActionComponent actionComponent) throws Throwable {
 		boolean valid = true;
 		ViewComponent form = actionComponent.getContainerComponent();
 		final Object iContent = form.getContent();
 		final SchemaAction iAction = actionComponent.getActionField();
 		final Object enabled = iAction.getFeature(ViewActionFeatures.ENABLED);
 		if (enabled != null && enabled.equals(Boolean.FALSE)) {
 			return;
 		}
 		final Object validate = iAction.getFeature(ValidationActionFeatures.VALIDATE);
 
 		if (validate != null && (Boolean) validate && form instanceof HtmlViewAbstractComponent) {
 			valid = ((HtmlViewAbstractComponent) form).validate();
 		}
 		if (valid) {
 			Roma.view().getScreen().setActiveArea(actionParts[ACTION_PART_CURRENT_ACTION_AREA]);
 			Controller.getInstance().executeAction(iContent, iAction);
 		}
 	}
 
 	private Map<String, Map<String, Object>> groupParameters(final HttpServletRequest request) throws FileUploadException, IOException {
 		final Map<String, Map<String, Object>> paramGroups = new HashMap<String, Map<String, Object>>();
 		final Map<String, Object> reqParams = new HashMap<String, Object>(request.getParameterMap());
 
 		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 		if (isMultipart) {
 			FileItemFactory factory = new DiskFileItemFactory();
 			ServletFileUpload upload = new ServletFileUpload(factory);
 			List<FileItem> items = upload.parseRequest(request);
 			if (items != null) {
 				for (FileItem item : items) {
 					Stream stream = new Stream(item.getInputStream(), (int) item.getSize(), item.getName(), item.getContentType());
 					reqParams.put(item.getFieldName(), stream);
 				}
 			}
 		}
 
 		for (final Map.Entry<String, Object> param : reqParams.entrySet()) {
 			final String[] splittedName = param.getKey().split(TransformerHelper.SEPARATOR);
 			Map<String, Object> group = paramGroups.get(splittedName[0]);
 			if (group == null) {
 				group = new HashMap<String, Object>();
 				paramGroups.put(splittedName[0], group);
 			}
 			// param.getValue().getClass();
 			if (String.class.equals(param.getValue().getClass())) {
 				group.put(param.getKey(), param.getValue());
 			} else if (Stream.class.isAssignableFrom(param.getValue().getClass())) {
 				group.put(param.getKey(), param.getValue());
 			} else if (TransformerHelper.POJO_EVENT_PREFIX.equals(splittedName[0])) {
 
 				group.put(param.getKey(), resolveEventParameter(param.getValue()));
 			} else {
 				group.put(param.getKey(), ((String[]) param.getValue())[0]);
 			}
 		}
 		return paramGroups;
 	}
 
 	private static Object[] resolveEventParameter(Object sendedValue) {
 		// Used TreeMap for order parameters
 		Map<Integer, Object> l = new TreeMap<Integer, Object>();
 		if (sendedValue instanceof String[]) {
 			for (String value : (String[]) sendedValue) {
 				int pos = value.indexOf('_');
 				if (pos != -1) {
 					try {
 						int param_pos = Integer.parseInt(value.substring(0, pos));
 						l.put(param_pos, value.substring(pos + 1));
 					} catch (Exception e) {
 					}
 				}
 			}
 		} else if (sendedValue instanceof String) {
 			String value = (String) sendedValue;
 			int pos = value.indexOf('_');
 			if (pos != -1) {
 				try {
 					int param_pos = Integer.parseInt(value.substring(0, pos));
 					l.put(param_pos, value.substring(pos + 1));
 				} catch (Exception e) {
 				}
 			}
 		}
 		return l.values().toArray();
 	}
 
 	public HtmlViewRenderable invokeEvent(HttpServletRequest request) throws Throwable {
 		final Map<String, Map<String, Object>> reqParams = groupParameters(request);
 		HtmlViewScreen screen = getScreen();
 		HtmlViewRenderable result;
 		synchronized (screen) {
 			result = invokeEvent(reqParams);
 		}
 		return result;
 	}
 
 	private HtmlViewScreen getScreen() {
 		return (HtmlViewScreen) Roma.aspect(ViewAspect.class).getScreen();
 	}
 }
