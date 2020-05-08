 
 package edu.common.dynamicextensions.ui.webui.actionform;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.interfaces.AbstractAttributeUIBeanInterface;
 import edu.common.dynamicextensions.ui.interfaces.ControlUIBeanInterface;
 import edu.common.dynamicextensions.ui.webui.util.CommonControlModel;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.global.Validator;
 
 /**
  * This represents a ActionForm for BuildForm.jsp
  * @author deepti_shelar
  *
  */
 public class ControlsForm extends CommonControlModel
 		implements
 			ControlUIBeanInterface,
 			AbstractAttributeUIBeanInterface
 {
 
 	/**
 	 * Overrides the validate method of ActionForm.
 	 * @param mapping ActionMapping mapping
 	 * @param request HttpServletRequest request
 	 * @return ActionErrors ActionErrors
 	 */
 	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request)
 	{
 		ActionErrors errors = new ActionErrors();
 		Validator validator = new Validator();
 
 		if (caption == null || validator.isEmpty(String.valueOf(caption)))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 					ApplicationProperties.getValue("eav.att.Label")));
 		}
 
 		if (caption != null && caption.contains(","))
 		{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.label.containsComma",
 						ApplicationProperties.getValue("eav.att.Label")));
 		}
 
 		validateControlFields(validator, errors);
 		return errors;
 	}
 
 	/**
 	 * @param validator :validator
 	 * @param errors : action errors
 	 *
 	 */
 	private void validateControlFields(Validator validator, ActionErrors errors)
 	{
 		if (userSelectedTool != null)
 		{
 			//Special case for text Control
 			if (userSelectedTool.equalsIgnoreCase(ProcessorConstants.TEXT_CONTROL))
 			{
 				getErrorsForTextControl(validator, errors);
 			}
 			else if (userSelectedTool.equalsIgnoreCase(ProcessorConstants.COMBOBOX_CONTROL))
 			{
 				//Special case for combobox Control
 				getErrorsForComboboxControl(validator, errors);
 			}
 			else if (userSelectedTool.equalsIgnoreCase(ProcessorConstants.DATEPICKER_CONTROL))
 			{
 				//Special case for Date picker Control
 				getErrorsForDatePickerControl(errors);
 			}
 			else if (userSelectedTool.equalsIgnoreCase(ProcessorConstants.FILEUPLOAD_CONTROL))
 			{
 				//Special case for File upload Control
 				getErrorsForFileUploadControl(validator, errors);
 			}
 			else if (userSelectedTool.equalsIgnoreCase(ProcessorConstants.CHECKBOX_CONTROL))
 			{
 				//Special case for Check box control
 				getErrorsForCheckBoxControl(errors);
 			}
 			else if (userSelectedTool.equalsIgnoreCase(ProcessorConstants.RADIOBUTTON_CONTROL))
 			{
 				getErrorsForRadioButtonControl(errors);
 			}
 		}
 	}
 
 	/**
 	 * @param errors
 	 */
 	private void getErrorsForRadioButtonControl(ActionErrors errors)
 	{
 		if (displayChoice != null
 				&& displayChoice.equalsIgnoreCase(ProcessorConstants.DISPLAY_CHOICE_USER_DEFINED)
 				&& csvString.length() == 0 || csvString.equalsIgnoreCase("0\t0\t\t\t\r\n"))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 					ApplicationProperties.getValue("dynExtn.validation.radio.NoPV")));
 		}
 	}
 
 	/**
 	 * @param errors : action errors
 	 */
 	private void getErrorsForCheckBoxControl(ActionErrors errors)
 	{
 		// Radio button checked status 
 		if (attributeDefaultValue == null)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 					ApplicationProperties.getValue("dynExtn.validation.radio.notChecked")));
 		}
 
 	}
 
 	/**
 	 * @param validator :validator
 	 * @param errors : action errors
 	 */
 	private void getErrorsForFileUploadControl(Validator validator, ActionErrors errors)
 	{
 		// Numeric default value.
 		if (!(isNaturalNumber(attributeSize) || (validator.isDouble(attributeSize))))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 					"errors.item.naturalNumericField", ApplicationProperties
 							.getValue("eav.att.MaximumFileSize")));
 		}
 		if (!isNaturalNumber(attributenoOfCols))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 					"errors.item.naturalNumericField", ApplicationProperties
 							.getValue("eav.att.TextFieldWidth")));
 		}
 	}
 
 	/**
 	 * @param errors : action errors
 	 */
 	private void getErrorsForDatePickerControl(ActionErrors errors)
 	{
 		String dateFormat = DynamicExtensionsUtility.getDateFormat(this.format);
 
 		//check for date format of default value field
 		//if dateValueType is "Select" then default value cannot be blank and should be valid date
 		if (dateValueType != null)
 		{
 			if (dateValueType.trim().equalsIgnoreCase(ProcessorConstants.DATE_VALUE_SELECT)
 					&& (attributeDefaultValue == null
							|| !DynamicExtensionsUtility.isDateValid(dateFormat, attributeDefaultValue)))
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.date.format",
 							ApplicationProperties.getValue("eav.att.DefaultValue")));
 			}
 		}
 		else
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 					ApplicationProperties.getValue("eav.att.DefaultValue")));
 		}
 
 		// Perform validation checks on date range.
 		dateRangeValidion(dateFormat, errors);
 
 	}
 
 	private void dateRangeValidion(String dateFormat, ActionErrors errors)
 	{
 		boolean isValid = true;
 
 		if (((min != null) && !(min.equals("")) || (max != null) && !(max.equals("")))
 				&& (validationRules == null
 						|| (validationRules.length == 1 && validationRules[0].length() == 0)))
 		{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 						"errors.dateRange.EnteredNotChecked", ApplicationProperties
 								.getValue("eav.att.Range")));
 		}
 
 		for (String validationName : validationRules)
 		{
 			if ("dateRange".equals(validationName))
 			{
 				if ((min != null) && !(min.equals("")) && (max != null) && !(max.equals("")))
 				{
					if ((!DynamicExtensionsUtility.isDateValid(dateFormat, this.min)))
 					{
 						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.date.format",
 								ApplicationProperties.getValue("eav.att.Minimum")));
 						isValid = false;
 					}
 
					if ((!DynamicExtensionsUtility.isDateValid(dateFormat, this.max)))
 					{
 						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.date.format",
 								ApplicationProperties.getValue("eav.att.Maximum")));
 						isValid = false;
 					}
 
 					if (DynamicExtensionsUtility.compareDates(this.min, this.max, dateFormat) > 0
 							&& isValid)
 					{
 						errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.date.range",
 								ApplicationProperties.getValue("eav.att.Range")));
 					}
 				}
 				else
 				{
 					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 							ApplicationProperties.getValue("eav.att.DateRange")));
 				}
 				break;
 			}
 		}
 	}
 
 	/**
 	 * @param validator :validator
 	 * @param errors : action errors
 	 */
 	private void getErrorsForComboboxControl(Validator validator, ActionErrors errors)
 	{
 		if (attributeMultiSelect == null || validator.isEmpty(String.valueOf(attributeMultiSelect)))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 					ApplicationProperties.getValue("eav.att.ListBoxType")));
 		}
 
 		if ((attributeMultiSelect != null && attributeMultiSelect.equalsIgnoreCase("SingleSelect")
 				|| attributeMultiSelect.equalsIgnoreCase("MultiSelect"))&&
 				(displayChoice != null
 						&& displayChoice
 								.equalsIgnoreCase(ProcessorConstants.DISPLAY_CHOICE_USER_DEFINED)
 						&& csvString.length() == 0 || csvString.equalsIgnoreCase("0\t0\t\t\t\r\n")))
 		{
 			
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 						ApplicationProperties.getValue("dynExtn.validation.listCombo.NoPV")));
 		}
 
 		if (dataType == null || validator.isEmpty(String.valueOf(dataType)))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 					ApplicationProperties.getValue("eav.att.DataInput")));
 		}
 		//NUMBER OF ROWS SHLD BE NUMERIC
 		if ((attributeMultiSelect != null)
 				&& (attributeMultiSelect.equals(ProcessorConstants.LIST_TYPE_MULTI_SELECT))
 				&& !isNaturalNumber(attributeNoOfRows))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 						"errors.item.naturalNumericField", ApplicationProperties
 								.getValue("eav.att.ListBoxDisplayLines")));
 		}
 	}
 
 	/**
 	 * @param validator :validator
 	 * @param errors : action errors
 	 */
 	private void getErrorsForTextControl(Validator validator, ActionErrors errors)
 	{
 		//REQUIRED FIELDS VALIDATION
 		checkRequiredFieldsForTextControl(validator, errors);
 
 		//1. Check for text field width
 		if (!isNaturalNumber(attributenoOfCols))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 					"errors.item.naturalNumericField", ApplicationProperties
 							.getValue("eav.att.TextFieldWidth")));
 		}
 		//Text field width cannot be more than 3 characters i.e 999
 		if ((attributenoOfCols != null)
 				&& (attributenoOfCols.length() > ProcessorConstants.MAX_LENGTH_DISPLAY_WIDTH))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.maxlength.exceeded",
 					ApplicationProperties.getValue("eav.att.Description"),
 					ProcessorConstants.MAX_LENGTH_DISPLAY_WIDTH));
 		}
 
 		//max number of characters cannot be more than 3 digits long : max value 999
 		if ((attributeSize != null)
 				&& (attributeSize.length() > ProcessorConstants.MAX_LENGTH_MAX_CHARACTERS))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.maxlength.exceeded",
 					ApplicationProperties.getValue("eav.att.Description"),
 					ProcessorConstants.MAX_LENGTH_MAX_CHARACTERS));
 		}
 		//check errors if datatype is String
 		if ((dataType != null) && (dataType.equals(ProcessorConstants.DATATYPE_STRING)))
 		{
 			getErrorsForStringDatatype(validator, errors);
 		}
 
 		//Check errors if datatype is number
 		if ((dataType != null) && DynamicExtensionsUtility.isDataTypeNumeric(dataType))
 		{
 			getErrorsForNumericDatatype(validator, errors);
 		}
 	}
 
 	/** @param validator
 	 * @param errors
 	 */
 	private void getErrorsForNumericDatatype(Validator validator, ActionErrors errors)
 	{
 		if (this.attributeDecimalPlaces.trim().length() == 0)
 		{
 			this.attributeDecimalPlaces = "0";
 		}
 		if (!isNaturalNumber(attributeDecimalPlaces))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 					"errors.item.naturalNumericField", ApplicationProperties
 							.getValue("eav.att.AttributeDecimalPlaces")));
 		}
 		else if (Integer.parseInt(this.attributeDecimalPlaces) > edu.common.dynamicextensions.ui.util.Constants.DOUBLE_PRECISION)
 		{
 			errors
 					.add(
 							ActionErrors.GLOBAL_ERROR,
 							new ActionError(
 									"errors.item.maximumPrecision",
 									String
 											.valueOf(edu.common.dynamicextensions.ui.util.Constants.DOUBLE_PRECISION)));
 		}
 
 		/*
 		 *	For number data type validate:
 		 *	1) Precision : numeric
 		 *	2) Default value is numeric
 		 */
 		if (attributeDecimalPlaces.contains("."))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 					"errors.item.precisionNotInteger", ApplicationProperties
 							.getValue("eav.att.AttributeDecimalPlaces")));
 		}
 
 		//Numeric default value
 		if (!(isNumeric(attributeDefaultValue) || (validator
 				.isDouble(attributeDefaultValue))))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 					"errors.item.naturalNumericField", ApplicationProperties
 							.getValue("eav.att.DefaultValue")));
 		}
 
 		boolean isMinValid = isNumeric(min);
 		if (!isMinValid)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.numericField",
 					ApplicationProperties.getValue("eav.att.Minimum")));
 		}
 
 		boolean isMaxValid = isNumeric(max);
 		if (!isMaxValid)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.numericField",
 					ApplicationProperties.getValue("eav.att.Maximum")));
 		}
 
 		if (isMinValid && isMaxValid && !isRangeValid(min, max))
 		{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.numericRange",
 						ApplicationProperties.getValue("eav.att.Range")));
 		}
 	}
 
 	/**
 	 * @param validator
 	 * @param errors
 	 */
 	private void getErrorsForStringDatatype(Validator validator, ActionErrors errors)
 	{
 		/*
 		 *	For string datatype validate:
 		 *  1) Lines type : Singleline/Multiline specified  : REQUIRED
 		 *	2) Max number of chars(size)  : Numeric
 		 *	3) If linesType is multiline the Number of rows()  : Numeric
 		 */
 
 		//Atleast one of singleline/ multiline should be selected
 		if (linesType == null || validator.isEmpty(String.valueOf(linesType)))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 					ApplicationProperties.getValue("eav.control.type")));
 		}
 
 		//Size : maximum characters shld be numeric
 		if (!isNaturalNumber(attributeSize))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 					"errors.item.naturalNumericField", ApplicationProperties
 							.getValue("eav.att.MaxCharacters")));
 		}
 
 		//Number of lines for multiline textbox shld be numeric
 		if ((linesType != null) && (linesType.equals(ProcessorConstants.LINE_TYPE_MULTILINE))
 				&& !isNaturalNumber(attributeNoOfRows))
 		{
 		
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
 						"errors.item.naturalNumericField", ApplicationProperties
 								.getValue("eav.text.noOfLines")));
 		
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void checkRequiredFieldsForTextControl(Validator validator, ActionErrors errors)
 	{
 		//Data type either numeric or string should be selected
 		if (dataType == null || validator.isEmpty(String.valueOf(dataType)))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 					ApplicationProperties.getValue("eav.att.DataInput")));
 		}
 		//If displayasURL is checked then default value is mandatory
 		if ((attributeDisplayAsURL != null && attributeDisplayAsURL.equals("true"))
 			&& (attributeDefaultValue == null || validator.isEmpty(attributeDefaultValue)))
 		{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 						ApplicationProperties.getValue("eav.att.defaultValue")));
 		}
 
 		if (dataType != null && dataType.equals("Number") && attributeDefaultValue.length() != 0)
 		{
 			if (!DynamicExtensionsUtility.isNumeric(attributeDefaultValue))
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.numericField",
 						ApplicationProperties.getValue("eav.att.DefaultValue")));
 			}
 			else
 			{
 				if (min != null
 						&& !min.equals("")
 						&& max != null
 						&& !max.equals("")
 						&& (Float.parseFloat(attributeDefaultValue) < Float.parseFloat(min) || Float
 								.parseFloat(attributeDefaultValue) > Float.parseFloat(max)))
 				{
 					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item",
 							ApplicationProperties.getValue("eav.att.defaultValueExceedsRange")));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Test if a string represents a numeric fld.
 	 * Will return false if fld is null or is not a numeric fld. Blank fld will be considered as valid
 	 * @param stringFld
 	 * @return
 	 */
 	private boolean isNumeric(String stringFld)
 	{
 		if (stringFld != null)
 		{
 			if (stringFld.trim().equals(""))
 			{
 				return true; //Blank fields are considered valid. Assume their value as 0
 			}
 			else
 			{
 				return DynamicExtensionsUtility.isNumeric(stringFld);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param stringFld
 	 * @return
 	 */
 	private boolean isNaturalNumber(String stringFld)
 	{
 		if (stringFld != null)
 		{
 			if (stringFld.trim().equals(""))
 			{
 				return true; //Blank fields are considered valid. Assume their value as 0
 			}
 			else
 			{
 				return DynamicExtensionsUtility.isNaturalNumber(stringFld);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 * @return
 	 */
 	private boolean isRangeValid(String min, String max)
 	{
 		boolean isValid = true;
 		String rangeRule = null;
 
 		for (String validationName : validationRules)
 		{
 			if ("range".equals(validationName))
 			{
 				rangeRule = validationName;
 				break;
 			}
 		}
 
 		if (rangeRule != null && rangeRule.equals("range"))
 		{
 			if (min != null && max != null && !min.equals("") && !max.equals(""))
 			{
 				double doubleMin = Double.parseDouble(min);
 				double doubleMax = Double.parseDouble(max);
 
 				if (doubleMin > doubleMax)
 				{
 					isValid = false;
 				}
 			}
 			else if (min.length() == 0 || max.length() == 0)
 			{
 				isValid = false;
 			}
 		}
 		return isValid;
 	}
 
 }
