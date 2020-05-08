 package org.romaframework.aspect.view.html.transformer.helper;
 
 import java.util.List;
 
import org.romaframework.aspect.view.feature.ViewActionFeatures;
 import org.romaframework.aspect.view.feature.ViewClassFeatures;
 import org.romaframework.aspect.view.form.ViewComponent;
 import org.romaframework.aspect.view.html.area.HtmlViewRenderable;
 import org.romaframework.aspect.view.html.area.mode.HtmlViewAreaMode;
 import org.romaframework.aspect.view.html.component.HtmlViewAbstractContentComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewActionComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewContentComponent;
 import org.romaframework.aspect.view.html.component.HtmlViewContentForm;
 import org.romaframework.aspect.view.html.constants.TransformerConstants;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaObject;
 
 public class TransformerHelper {
 
 	private static final String			RENDER_NAME_	= " render_";
 	private static final String			CLASS_NAME_		= " class_";
 
 	private static final String			FIELD_NAME_		= " field_";
 
 	private static final String			ACTION_NAME_	= " action_";
 	private static final String			SCREEN_NAME_	= " screen_";
 	private static final String			AREA_NAME_		= " area_";
 
 	public static TransformerHelper	instance			= new TransformerHelper();
 
 	public static final String			SEPARATOR			= "_";
 
 	public static TransformerHelper getInstance() {
 		return instance;
 	}
 
 	public static final String	POJO_ACTION_PREFIX	= "(PojoAction)";
 	public static final String	POJO_EVENT_PREFIX		= "(PojoEvent)";
 	private static final String	CSS_INVALID_CLASS	= "invalid";
 
 	/**
 	 * Return the id to use in the HTML chunk
 	 * 
 	 * @param contentComponent
 	 * @param part
 	 * @return
 	 */
 	public String getHtmlId(final HtmlViewRenderable contentComponent, final String part) {
 		final String idPrefix = contentComponent.getHtmlId();
 		String result = null;
 		if (part == null || part.equals("") || part.equals(TransformerConstants.PART_ALL)) {
 			result = idPrefix;
 		} else {
 			result = idPrefix + SEPARATOR + part;
 		}
 		return result;
 	}
 
 	/**
 	 * Return the class attribute to use in the HTML code
 	 * 
 	 * @param transformer
 	 * @param part
 	 * @return
 	 */
 	public String getHtmlClass(final String transformerName, final String part, final HtmlViewRenderable iGenericComponent) {
 
 		String result = "";
 		if (transformerName != null) {
 			final String classPrefix = RENDER_NAME_ + transformerName;
 
 			if (part == null) {
 				result = classPrefix;
 			} else {
 				result = classPrefix + SEPARATOR + part;
 			}
 		}
 		if (iGenericComponent != null) {
 			result = result + " " + getMultiClass(iGenericComponent);
 		}
 		if(iGenericComponent instanceof HtmlViewAbstractContentComponent) {
 			if(!((HtmlViewAbstractContentComponent) iGenericComponent).isValid()) {
 				result = result + " " + CSS_INVALID_CLASS;
 			}
 		}
 
 		return result.trim();
 	}
 
 	private String getMultiClass(final HtmlViewRenderable genericComponent) {
 		String result = "";
 
 		String style = null;
 		if (genericComponent instanceof HtmlViewActionComponent) {
 			final SchemaAction schemaAction = (SchemaAction) ((HtmlViewActionComponent) genericComponent).getSchemaElement();
 			if (schemaAction != null) {
 				result = result + ACTION_NAME_ + schemaAction.getName();
				style = schemaAction.getFeature(ViewActionFeatures.STYLE);
 			}
 		} else if (genericComponent instanceof HtmlViewContentForm) {
 			final SchemaObject schemaObject = ((HtmlViewContentForm) genericComponent).getSchemaObject();
 			if (schemaObject != null) {
 				result = result + " " + getAllClassHierarchy(schemaObject.getSchemaClass());
 				style = schemaObject.getFeature(ViewClassFeatures.STYLE);
 			}
 
 		} else if (genericComponent instanceof HtmlViewContentComponent) {
 			final SchemaField schemaField = (SchemaField) ((HtmlViewContentComponent) genericComponent).getSchemaElement();
 			result = result + " " + FIELD_NAME_ + schemaField.getName();
 			style = schemaField.getFeature(ViewClassFeatures.STYLE);
 		} else if (genericComponent instanceof HtmlViewAreaMode) {
 			style = ((HtmlViewAreaMode) genericComponent).getContainer().getStyle();
 			String areaName = ((HtmlViewAreaMode) genericComponent).getAreaName();
 			if (areaName != null) {
 				if (((HtmlViewAreaMode) genericComponent).isScreenArea()) {
 					return SCREEN_NAME_ + areaName;
 				} else {
 					return AREA_NAME_ + areaName;
 				}
 			}
 		}
 		if (style != null && !style.startsWith("{")) {
 			result += " " + style;
 		}
 		return result;
 	}
 
 	private String getAllClassHierarchy(final SchemaClass contentClass) {
 		String result = "";
 
 		if (contentClass != null) {
 			result = getAllClassHierarchy(contentClass.getSuperClass()) + " " + CLASS_NAME_ + contentClass.getName();
 		}
 
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param contentComponent
 	 * @param type
 	 * @return
 	 */
 	public String actions(final ViewComponent contentComponent, final String type) {
 		final StringBuffer result = new StringBuffer();
 		result.append("<div class=\"" + type + "_actions\">\n");
 		final String contentId = getHtmlId((HtmlViewRenderable) contentComponent, TransformerConstants.PART_CONTENT);
 		final long componentName = ((HtmlViewRenderable) contentComponent).getId();
 		result.append(action(type, contentId, componentName, "add", contentComponent.getScreenArea()));
 		result.append(action(type, contentId, componentName, "edit", contentComponent.getScreenArea()));
 		result.append(action(type, contentId, componentName, "view", contentComponent.getScreenArea()));
 		result.append(action(type, contentId, componentName, "remove", contentComponent.getScreenArea()));
 		if (contentComponent.getContent() instanceof List<?>) {
 			result.append(action(type, contentId, componentName, "up", contentComponent.getScreenArea()));
 			result.append(action(type, contentId, componentName, "down", contentComponent.getScreenArea()));
 		}
 		result.append("</div>\n");
 		return result.toString();
 	}
 
 	private StringBuffer action(final String className, final String contentId, final Long componentName, final String action, final String screenArea) {
 		final StringBuffer result = new StringBuffer();
 		final String upperCaseAction = action.substring(0, 1).toUpperCase() + action.substring(1);
 		result.append("<span>\n");
 		result.append("<input class=\"" + className + "_actions_" + action + "\" id=\"" + contentId + "_" + action + "_button\" type=\"submit\" value=\"" + action
 				+ "\" name=\"" + POJO_ACTION_PREFIX + "_" + componentName + "_on" + upperCaseAction + SEPARATOR + screenArea + "\" />\n");
 		result.append("</span>\n");
 		return result;
 	}
 
 }
