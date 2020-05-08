 package org.jails.form.constructor;
 
 import org.jails.form.input.FormInput;
 import org.jails.form.input.FormTag;
 import org.jails.form.input.Repeater;
 import org.jails.form.SimpleForm;
 import org.jails.form.SimpleFormParams;
 import org.jails.property.Mapper;
 import org.jails.property.SimpleMapper;
 import org.jails.validation.client.ClientConstraintInfo;
 import org.jails.validation.client.ClientConstraintInfoRegistry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.ServletRequest;
 import java.util.List;
 
 /**
  * InputConstructor initializes a set of Strings to be used to construct
  * the HTML for associated FormInput.
  *
  * @param <T>
  */
 public abstract class InputConstructor<T extends FormInput> {
 	protected static Logger logger = LoggerFactory.getLogger(InputConstructor.class);
 
 	protected static SimpleFormParams simpleFormParams = new SimpleFormParams();
 
 	protected T tag;
 	protected FormTag formTag;
 	protected SimpleForm simpleForm;
 	protected Repeater repeater;
 	protected String fieldName;
 	protected String[] fieldValues;
 	protected String labelCss;
 	protected String inputId;
 	protected String validation;
 
 	protected Mapper beanMapper;
 
 	public InputConstructor(T tag, FormTag formTag, Repeater repeatTag, ServletRequest request) {
 		this.tag = tag;
 		this.formTag = formTag;
 		this.repeater = repeatTag;
 		this.beanMapper = new SimpleMapper();
 		initFormTag();
 		initFieldName();
 		initCssClass();
 		initFieldValues(request);
 		initInputId();
 		initClientValidation();
 	}
 
 	protected void initFormTag() {
 		simpleForm = formTag.getSimpleForm();
 		logger.info("adding element: " + tag.getName() + " of type "
 				+ tag.getClass().getSimpleName() + " to form: " + formTag.getClass().getSimpleName());
 		logger.info("adding to element index: " + getIndex());
 		formTag.addElement(tag.getName(), getIndex());
 		logger.info("setting element label: " + tag.getLabel());
 		formTag.addLabel(tag.getName(), tag.getLabel());
 		logger.info("added element: " + tag.getName());
 	}
 
 	public FormTag getFormTag() {
 		return formTag;
 	}
 
 	protected void initFieldName() {
 		if (repeater != null) {
 			fieldName = simpleFormParams.getFormIndexedParameterName(
 					formTag.getName(), tag.getName(),
 					repeater.getIndex());
 		} else {
			fieldName = simpleFormParams.getParameterName(tag.getName());
 		}
 		logger.info("constructing " + fieldName);
 	}
 
 	public String getFieldName() {
 		return fieldName;
 	}
 
 	private int getIndex() {
 		return (repeater != null) ? repeater.getIndex() : 0;
 	}
 
 	protected void initCssClass() {
 		if (simpleForm != null && simpleForm.hasError()
 				&& simpleForm.fieldHasError(fieldName, getIndex())) {
 			labelCss = "error";
 		} else {
 			labelCss = "formField";
 		}
 		logger.info("set css class" + labelCss);
 	}
 
 	public String getLabelCss() {
 		return labelCss;
 	}
 
 	public String getLabelCssAttr() {
 		return getAttribute("class", labelCss);
 	}
 
 	/**
 	 * The fieldValue is decided in order of precedence:
 	 * 1. from the request
 	 * 2. from an object bound to the form
 	 * 3. from the default value specified from the form tag
 	 * 4. empty
 	 *
 	 * @param request
 	 */
 	protected void initFieldValues(ServletRequest request) {
 
 		String fieldName = getFieldName();
 		if (request.getParameter(fieldName) != null) {
 			fieldValues = new String[]{request.getParameter(fieldName)};
 		} else if (simpleForm != null && simpleForm.isBound()) {
 			if (repeater != null) {
 				SimpleForm beanForm = simpleForm;
 				fieldValues = beanMapper.getValues(
 						beanForm.getObject(repeater.getIndex()), fieldName);
 			} else {
 				SimpleForm beanForm = (SimpleForm) simpleForm;
 				fieldValues = beanMapper.getValues(beanForm.getObject(), fieldName);
 			}
 		} else if (tag.getDefaultValue() != null) {
 			fieldValues = new String[]{tag.getDefaultValue()};
 		}
 		if (fieldValues == null) fieldValues = new String[]{};
 		logger.info("fieldValues initialized");
 	}
 
 	public String[] getFieldValues() {
 		return fieldValues;
 	}
 
 	public String getFieldValue(int index) {
 		return (fieldValues != null && fieldValues.length > 0 && fieldValues[index] != null)
 				? fieldValues[index] : "";
 	}
 
 	protected void initClientValidation() {
 		if (simpleForm != null && simpleForm.getClassType() != null) {
 			StringBuffer validationBuffer = null;
 
 			Class classType = simpleForm.getClassType();
 			String property = tag.getName();
 			logger.warn("Setting ClientConstraints for " + classType.getSimpleName() + ": " + property);
 
 			ClientConstraintInfoRegistry constraintInfoRegistry = ClientConstraintInfoRegistry.getInstance();
 			List<ClientConstraintInfo> clientConstraints = constraintInfoRegistry
 					.getClientConstraints(classType, property);
 			validation = constraintInfoRegistry.getValidationConstructor()
 					.getValidationHtml(clientConstraints, classType, property);
 			logger.info("clientValidation: " + validation);
 		}
 	}
 
 	public String getClientValidationAttr() {
 		return (validation != null)
 				? validation
 				: "";
 	}
 
 	protected void initInputId() {
 		inputId = (fieldName != null) ? fieldName.replaceAll("[^a-zA-Z0-9]+", "_") : "";
 		logger.info("inputId init: " + inputId);
 	}
 
 	public String getInputId() {
 		return inputId;
 	}
 
 	public String getAttribute(String attrName) {
 		String attr = (tag.getAttributes() != null) ? tag.getAttributes().get(attrName) : null;
 		if (attr != null) logger.info("found attribute " + attrName + " with value " + attr);
 		return attr;
 	}
 
 	public String getAttribute(String attrName, String attrValue) {
 		if (getAttribute(attrName) != null) attrValue = getAttribute(attrName);
 		return (attrValue != null) ? " " + attrName + "=\"" + attrValue + "\"" : "";
 	}
 
 	public String getAttributes() {
 		StringBuffer attrString = new StringBuffer();
 		for (String attrName : tag.getAttributes().keySet()) {
 			getAttribute(attrName, tag.getAttributes().get(attrName));
 		}
 		return attrString.toString();
 	}
 
 	public String getFieldNameAttr() {
 		return getAttribute("name", fieldName);
 	}
 
 	public String getInputIdAttr() {
 		return getAttribute("id", inputId);
 	}
 
 	public String getTypeAttr(String type) {
 		return getAttribute("type", type);
 	}
 
 	public String getValueAttr(String value) {
 		return getAttribute("value", value);
 	}
 
 	public String wrapInputHtml(FormInput tag, String inputTagHtml) {
 		StringBuffer tagHtml = new StringBuffer();
 
 		tagHtml.append("<p" +
 				getLabelCssAttr() + "><label" +
 				getAttribute("forName", tag.getName()) + ">");
 
 		tagHtml.append(tag.getLabel());
 		if (getFormTag().isStacked()) tagHtml.append("<br />");
 		else tagHtml.append(": ");
 
 		tagHtml.append(inputTagHtml);
 
 		if (getFormTag().getSimpleForm() != null
 				&& getFormTag().getSimpleForm().isFieldRequired(tag.getName())) {
 			tagHtml.append(" *");
 		}
 
 		tagHtml.append("</label></p>");
 
 		return tagHtml.toString();
 	}
 }
