 
 package edu.common.dynamicextensions.domain.userinterface;
 
 import java.util.List;
 
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.DoubleTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.FloatTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.LongTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.RadioButtonInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.util.ControlsUtility;
 import edu.wustl.common.beans.NameValueBean;
 
 /**
  * @version 1.0
  * @created 28-Sep-2006 12:20:08 PM
  * @hibernate.joined-subclass table="DYEXTN_RADIOBUTTON"
  * @hibernate.joined-subclass-key column="IDENTIFIER"
  */
 public class RadioButton extends Control implements RadioButtonInterface
 {
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Empty Constructor
 	 */
 	public RadioButton()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * This method generates the HTML code for RadioButton control on the HTML form
 	 * @return HTML code for RadioButton
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String generateEditModeHTML() throws DynamicExtensionsSystemException
 	{
 		List<NameValueBean> nameValueBeanList = null;
 		String htmlString = "";
 
 		String defaultValue = (String) this.value;
 		if (defaultValue == null)
 		{
 			defaultValue = this.getAttibuteMetadataInterface().getDefaultValue();
 			if (defaultValue == null || defaultValue.length() == 0)
 			{
 				defaultValue = "";
 			}
 		}
 		if (defaultValue.length() > 0
 				&& this.getAttibuteMetadataInterface().getAttributeTypeInformation() instanceof DoubleTypeInformationInterface)
 		{
 			double doubleValue = Double.parseDouble(defaultValue);
			defaultValue = doubleValue + "";
 		}
 		else if (defaultValue.length() > 0
 				&& this.getAttibuteMetadataInterface().getAttributeTypeInformation() instanceof LongTypeInformationInterface)
 		{
 			long longValue = Long.parseLong(defaultValue);
			defaultValue = longValue + "";
 
 		}
 		else if (defaultValue.length() > 0
 				&& this.getAttibuteMetadataInterface().getAttributeTypeInformation() instanceof FloatTypeInformationInterface)
 		{
 			float floatValue = Float.parseFloat(defaultValue);
			defaultValue = floatValue + "";
 
 		}
 		String disabled = "";
 		//If control is defined as readonly through category CSV file,make it Disabled
 		if (this.isReadOnly != null && getIsReadOnly())
 		{
 			disabled = ProcessorConstants.DISABLED;
 		}
 
 		nameValueBeanList = ControlsUtility.populateListOfValues(this);
 
 		String htmlComponentName = getHTMLComponentName();
 		if (nameValueBeanList != null)
 		{
 			for (NameValueBean nameValueBean : nameValueBeanList)
 			{
 				String optionName = nameValueBean.getName();
 				String optionValue = nameValueBean.getValue();
 				if (optionValue.equals(defaultValue))
 				{
 					htmlString += "<input type='radio' " + "class='font_bl_nor' " + "name='"
 							+ htmlComponentName + "' " + "value='" + optionValue + "' " + "id='"
 							+ optionName + "' checked " + disabled + "  >" + "<label for=\""
 							+ htmlComponentName + "\">" + optionName
 							+ "</label> <img src='images/spacer.gif' width='2' height='2'>";
 				}
 				else
 				{
 					htmlString += "<input type='radio' " + "class='font_bl_nor' " + "name='"
 							+ htmlComponentName + "' " + "value='" + optionValue + "' " + "id='"
 							+ optionName + "' " + disabled + " >" + "<label for=\""
 							+ htmlComponentName + "\">" + optionName
 							+ "</label> <img src='images/spacer.gif' width='2' height='2'>";
 				}
 			}
 		}
 
 		return htmlString;
 	}
 
 	/**
 	 * This method sets the corresponding Attribute of this control.
 	 * @param abstractAttribute the AbstractAttribute to be set.
 	 */
 	public void setAttribute(AbstractAttributeInterface abstractAttribute)
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	public String generateViewModeHTML() throws DynamicExtensionsSystemException
 	{
 		String htmlString = "&nbsp;";
 		if (value != null)
 		{
 			htmlString = "<span class = '" + cssClass + "'> " + this.value.toString() + "</span>";
 		}
 		return htmlString;
 	}
 
 }
