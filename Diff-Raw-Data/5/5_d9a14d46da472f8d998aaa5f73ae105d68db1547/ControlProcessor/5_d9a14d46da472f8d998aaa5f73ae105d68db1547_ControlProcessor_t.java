 /**
  *<p>Title: ControlProcessor</p>
  *<p>Description:  This class acts as a utility class which processes tne control in various ways as needed
  *and provides methods to the UI layer.This processor class is a POJO and not a framework specific class so
  *it can be used by all types of presentation layers.  </p>
  *<p>Copyright:TODO</p>
  *@author Deepti Shelar
  *@version 1.0
  */
 
 package edu.common.dynamicextensions.processor;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.userinterface.ContainmentAssociationControl;
 import edu.common.dynamicextensions.domain.userinterface.SelectControl;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationDisplayAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.CheckBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ComboBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.DatePickerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.FileUploadInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ListBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.RadioButtonInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextAreaInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextFieldInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.interfaces.ControlUIBeanInterface;
 import edu.wustl.common.beans.NameValueBean;
 
 /**
  * This class processes all the information needed for Control.
  * @author deepti_shelar
  *
  */
 public class ControlProcessor extends BaseDynamicExtensionsProcessor
 {
 
 	/**
 	 * Protected constructor for ControlProcessor
 	 *
 	 */
 	protected ControlProcessor()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * this method gets the new instance of the ControlProcessor to the caller.
 	 * @return ControlProcessor ControlProcessor instance
 	 */
 	public static ControlProcessor getInstance()
 	{
 		return new ControlProcessor();
 	}
 
 	/**
 	 *
 	 * @param userSelectedControlName : Name of the User Selected Control
 	 * @param controlUIBeanInterface : Control UI Information interface containing information added by user on UI
 	 * @return : Control interface populated with required information
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public ControlInterface createAndPopulateControl(String userSelectedControlName,
 			ControlUIBeanInterface controlUIBeanInterface, EntityGroupInterface... entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ControlInterface controlInterface = populateControlInterface(userSelectedControlName, null,
 				controlUIBeanInterface, entityGroup);
 		return controlInterface;
 	}
 
 	/**
 	 *
 	 * @param userSelectedControlName : Name of the User Selected Control
 	 * @param controlIntf : Control Interface (Domain Object Interface)
 	 * @param controlUIBeanInterface : Control UI Information interface containing information added by user on UI
 	 * @return : Control interface populated with required information
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public ControlInterface populateControlInterface(String userSelectedControlName,
 			ControlInterface controlIntf, ControlUIBeanInterface controlUIBeanInterface,
 			EntityGroupInterface... entityGroup) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ControlInterface controlInterface = null;
 		if ((userSelectedControlName != null) && (controlUIBeanInterface != null))
 		{
 			if (userSelectedControlName.equalsIgnoreCase(ProcessorConstants.TEXT_CONTROL))
 			{
 				if (controlUIBeanInterface.getLinesType() != null
 						&& controlUIBeanInterface.getLinesType().equalsIgnoreCase("MultiLine"))
 				{
 					controlInterface = getMultiLineControl(controlIntf, controlUIBeanInterface);
 				}
 				else
 				{
 					controlInterface = getTextControl(controlIntf, controlUIBeanInterface);
 				}
 			}
 			else if (userSelectedControlName.equalsIgnoreCase(ProcessorConstants.COMBOBOX_CONTROL))
 			{
 				if ((controlUIBeanInterface.getIsMultiSelect() != null)
 						&& (controlUIBeanInterface.getIsMultiSelect().booleanValue() == true))
 				{
 					controlInterface = getListBoxControl(controlIntf, controlUIBeanInterface,
 							entityGroup);
 				}
 				else
 				{
 					controlInterface = getComboBoxControl(controlIntf, controlUIBeanInterface,
 							entityGroup);
 				}
 			}
 			else if (userSelectedControlName.equalsIgnoreCase(ProcessorConstants.LISTBOX_CONTROL))
 			{
 				controlInterface = getListBoxControl(controlIntf, controlUIBeanInterface,
 						entityGroup);
 			}
 			else if (userSelectedControlName.equalsIgnoreCase(ProcessorConstants.CHECKBOX_CONTROL))
 			{
 				controlInterface = getCheckBoxControl(controlIntf, controlUIBeanInterface);
 			}
 			else if (userSelectedControlName
 					.equalsIgnoreCase(ProcessorConstants.RADIOBUTTON_CONTROL))
 			{
 				controlInterface = getRadioButtonControl(controlIntf, controlUIBeanInterface);
 			}
 			else if (userSelectedControlName
 					.equalsIgnoreCase(ProcessorConstants.DATEPICKER_CONTROL))
 			{
 				controlInterface = getDatePickerControl(controlIntf, controlUIBeanInterface);
 			}
 			else if (userSelectedControlName
 					.equalsIgnoreCase(ProcessorConstants.FILEUPLOAD_CONTROL))
 			{
 				controlInterface = getFileUploadControl(controlIntf, controlUIBeanInterface);
 			}
 		}
 		//Load common properties for controls
 		if (controlUIBeanInterface != null && controlInterface != null)
 		{
 			controlInterface
 					.setBaseAbstractAttribute(controlUIBeanInterface.getAbstractAttribute());
 			//controlUIBeanInterface.getAbstractAttribute().setControl((Control) controlInterface);
 			controlInterface.setCaption(controlUIBeanInterface.getCaption());
 			controlInterface.setIsHidden(controlUIBeanInterface.getIsHidden());
 		}
 		return controlInterface;
 
 	}
 
 	/**
 	 * @param controlIntf : Control Interface (Domain Object Interface)
 	 * @param controlUIBeanInterface : Control UI Information interface containing information added by user on UI
 	 * @return : Control interface populated with required information for File upload
 	 */
 	private ControlInterface getFileUploadControl(ControlInterface controlInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		FileUploadInterface fileUploadInterface = null;
 		if (controlInterface == null)
 		{
 			fileUploadInterface = DomainObjectFactory.getInstance().createFileUploadControl();
 		}
 		else
 		{
 			fileUploadInterface = (FileUploadInterface) controlInterface;
 		}
 		fileUploadInterface.setColumns(controlUIBeanInterface.getColumns());
 
 		return fileUploadInterface;
 	}
 
 	/**
 	 * @param controlInterface : Control Interface (Domain Object Interface)
 	 * @param controlUIBeanInterface : Control UI Information interface containing information added by user on UI
 	 * @return : Control interface populated with required information for date
 	 */
 	private ControlInterface getDatePickerControl(ControlInterface controlInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		DatePickerInterface datePickerIntf = null;
 		if (controlInterface == null)
 		{
 			datePickerIntf = DomainObjectFactory.getInstance().createDatePicker();
 		}
 		else
 		{
 			datePickerIntf = (DatePickerInterface) controlInterface;
 		}
 		datePickerIntf.setDateValueType(controlUIBeanInterface.getDateValueType());
 		return datePickerIntf;
 	}
 
 	/**
 	 * @param controlInterface : Control Interface (Domain Object Interface)
 	 * @param controlUIBeanInterface : Control UI Information interface containing information added by user on UI
 	 * @return : Control interface populated with required information for radiobutton
 	 */
 	private ControlInterface getRadioButtonControl(ControlInterface controlInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		RadioButtonInterface radioButtonIntf = null;
 		if (controlInterface == null)
 		{
 			radioButtonIntf = DomainObjectFactory.getInstance().createRadioButton();
 		}
 		else
 		{
 			radioButtonIntf = (RadioButtonInterface) controlInterface;
 		}
 
 		return radioButtonIntf;
 	}
 
 	/**
 	 * @param controlInterface : Control Interface (Domain Object Interface)
 	 * @param controlUIBeanInterface : Control UI Information interface containing information added by user on UI
 	 * @return : Control interface populated with required information for checkbox
 	 */
 	private ControlInterface getCheckBoxControl(ControlInterface controlInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		CheckBoxInterface checkBox = null;
 		if (controlInterface == null)
 		{
 			checkBox = DomainObjectFactory.getInstance().createCheckBox();
 		}
 		else
 		{
 			checkBox = (CheckBoxInterface) controlInterface;
 		}
 
 		return checkBox;
 	}
 
 	/**
 	 * @param controlInterface : Control Interface (Domain Object Interface)
 	 * @param controlUIBeanInterface : Control UI Information interface containing information added by user on UI
 	 * @return : Control interface populated with required information for list box
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public ControlInterface getListBoxControl(ControlInterface controlInterface,
 			ControlUIBeanInterface controlUIBeanInterface, EntityGroupInterface... entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ListBoxInterface listBoxIntf = null;
 		if (controlInterface == null) //If does not exist create it
 		{
 			listBoxIntf = DomainObjectFactory.getInstance().createListBox();
 		}
 		else
 		{
 			if (controlInterface instanceof ListBoxInterface)
 			{
 				listBoxIntf = (ListBoxInterface) controlInterface;
 			}
 			else
 			{
 				listBoxIntf = DomainObjectFactory.getInstance().createListBox();
 			}
 		}
 		listBoxIntf.setIsMultiSelect(controlUIBeanInterface.getIsMultiSelect());
 		listBoxIntf.setNoOfRows(controlUIBeanInterface.getRows());
 		//Set isCollection=true in the attribute
 		AbstractAttributeInterface controlAttribute = controlUIBeanInterface.getAbstractAttribute();
 		if ((controlAttribute != null) && (controlAttribute instanceof AttributeInterface))
 		{
 			//((AttributeInterface) controlAttribute).setIsCollection(new Boolean(true));
 		}
 		if (listBoxIntf instanceof SelectControl)
 		{
 			initializeSelectControl((SelectControl) listBoxIntf, controlUIBeanInterface,
 					entityGroup);
 		}
 		return listBoxIntf;
 	}
 
 	/**
 	 * @param controlInterface : Control Interface (Domain Object Interface)
 	 * @param controlUIBeanInterface : Control UI Information interface containing information added by user on UI
 	 * @return : Control interface populated with required information for Combobox
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public ControlInterface getComboBoxControl(ControlInterface controlInterface,
 			ControlUIBeanInterface controlUIBeanInterface, EntityGroupInterface... entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ComboBoxInterface comboBoxIntf = null;
 		if (controlInterface == null) //If does not exist create it
 		{
 			comboBoxIntf = DomainObjectFactory.getInstance().createComboBox();
 		}
 		else
 		{
 			if (controlInterface instanceof ComboBoxInterface)
 			{
 				comboBoxIntf = (ComboBoxInterface) controlInterface;
 			}
 			else
 			{
 				comboBoxIntf = DomainObjectFactory.getInstance().createComboBox();
 			}
 		}
 		if (comboBoxIntf instanceof SelectControl)
 		{
 			initializeSelectControl((SelectControl) comboBoxIntf, controlUIBeanInterface,
 					entityGroup);
 		}
 		return comboBoxIntf;
 
 	}
 
 	/**
 	 * @param controlUIBeanInterface
 	 * @param control
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void initializeSelectControl(SelectControl selectControl,
 			ControlUIBeanInterface controlUIBeanInterface, EntityGroupInterface... entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		//initialize the select control object with the separator etc properties
 		if (selectControl != null)
 		{
 			AbstractAttributeInterface attribute = controlUIBeanInterface.getAbstractAttribute();
 			if ((attribute != null) && (attribute instanceof AssociationInterface))
 			{
 				selectControl.setSeparator(controlUIBeanInterface.getSeparator());
 				String[] associationControlIds = controlUIBeanInterface.getSelectedAttributeIds();
 				selectControl.removeAllAssociationDisplayAttributes();
 				if (associationControlIds != null)
 				{
 					int noOfIds = associationControlIds.length;
 					for (int i = 0; i < noOfIds; i++)
 					{
 						selectControl
 								.addAssociationDisplayAttribute(getAssociationDisplayAttribute(
 										associationControlIds[i], i + 1, entityGroup));
 					}
 				}
 			}
 			else
 			{
 				selectControl.setSeparator(null);
 				selectControl.removeAllAssociationDisplayAttributes();
 			}
 		}
 	}
 
 	/**
 	 * @param controlUIBeanInterface
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private AssociationDisplayAttributeInterface getAssociationDisplayAttribute(String controlId,
 			int sequenceNo, EntityGroupInterface... entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		AssociationDisplayAttributeInterface associationDisplayAttribute = null;
 		if (controlId != null)
 		{
 			DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 
 			associationDisplayAttribute = domainObjectFactory.createAssociationDisplayAttribute();
 			associationDisplayAttribute.setSequenceNumber(sequenceNo);
 			associationDisplayAttribute.setAttribute(getAttributeForId(controlId, entityGroup[0]));
 		}
 		return associationDisplayAttribute;
 	}
 
 	/**
 	 * @param string
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private AttributeInterface getAttributeForId(String controlId, EntityGroupInterface entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ControlInterface control = null;
 
 		for (EntityInterface entity : entityGroup.getEntityCollection())
 		{
 			Collection<ContainerInterface> containerCollection = entity.getContainerCollection();
 			for (ContainerInterface container : containerCollection)
 			{
 				Collection<ControlInterface> controlCollection = container.getControlCollection();
 				for (ControlInterface currentControl : controlCollection)
 				{
 					if (currentControl.getId() != null
 							&& currentControl.getId().toString().equals(controlId))
 					{
 						control = currentControl;
 					}
 				}
 			}
 		}
 
 		AttributeInterface attribute = null;
 		if (controlId != null)
 		{
 			if (control != null)
 			{
 				if ((control.getBaseAbstractAttribute() != null)
 						&& (control.getBaseAbstractAttribute() instanceof AttributeInterface))
 				{
 					attribute = (AttributeInterface) control.getBaseAbstractAttribute();
 				}
 			}
 		}
 		return attribute;
 	}
 
 	/**
 	 * @param controlInterface : Control Interface (Domain Object Interface)
 	 * @param controlUIBeanInterface : Control UI Information interface containing information added by user on UI
 	 * @return : Control interface populated with required information for Text area(Multiline textbox)
 	 */
 	public ControlInterface getMultiLineControl(ControlInterface controlInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		TextAreaInterface textAreaIntf = null;
 		if (controlInterface == null) //If does not exist create it
 		{
 			textAreaIntf = DomainObjectFactory.getInstance().createTextArea();
 		}
 		else
 		{
 			if (controlInterface instanceof TextAreaInterface)
 			{
 				textAreaIntf = (TextAreaInterface) controlInterface;
 			}
 			else
 			{
 				textAreaIntf = DomainObjectFactory.getInstance().createTextArea();
 			}
 		}
 		textAreaIntf.setColumns(controlUIBeanInterface.getColumns());
 		textAreaIntf.setRows(controlUIBeanInterface.getRows());
 		return textAreaIntf;
 	}
 
 	/**
 	 * Creates a new TextControl if control interface is null
 	 * Updates the existing if not null
 	 * @param controlInterface : Control Interface (Domain Object Interface)
 	 * @param controlUIBeanInterface : Control UI Information interface containing information added by user on UI
 	 * @return : Control interface populated with required information for text box
 	 */
 	public ControlInterface getTextControl(ControlInterface controlInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		TextFieldInterface textFldIntf = null;
 		if (controlInterface == null) //If does not exist create it
 		{
 			textFldIntf = DomainObjectFactory.getInstance().createTextField();
 		}
 		else
 		{
 			if (controlInterface instanceof TextFieldInterface)
 			{
 				textFldIntf = (TextFieldInterface) controlInterface;
 			}
 			else
 			{
 				textFldIntf = DomainObjectFactory.getInstance().createTextField();
 			}
 		}
 		textFldIntf.setIsPassword(controlUIBeanInterface.getIsPassword());
 		textFldIntf.setIsUrl(controlUIBeanInterface.getIsUrl());
 		textFldIntf.setColumns(controlUIBeanInterface.getColumns());
 
 		return textFldIntf;
 	}
 
 	/**
 	 * This method will populate the ControlUIBeanInterface using the controlInterface so that the
 	 * information of the Control can be shown on the user page using the ControlUIBeanInterface.
 	 * @param controlInterface Instance of controlInterface from which to populate the informationInterface.
 	 * @param controlUIBeanInterface Instance of ControlUIBeanInterface which will be populated using
 	 * the first parameter that is controlInterface.
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public void populateControlUIBeanInterface(ControlInterface controlInterface,
 			ControlUIBeanInterface controlUIBeanInterface) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		if (controlInterface != null && controlUIBeanInterface != null)
 		{
 			populateControlCommonAttributes(controlInterface, controlUIBeanInterface);
 			populateControlSpecificAttributes(controlInterface, controlUIBeanInterface);
 		}
 	}
 
 	/**
 	 * @param controlInterface
 	 * @param controlUIBeanInterface
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void populateControlSpecificAttributes(ControlInterface controlInterface,
 			ControlUIBeanInterface controlUIBeanInterface) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		if (controlInterface instanceof TextFieldInterface)
 		{
 			populateTextControlAttributesInUIBean((TextFieldInterface) controlInterface,
 					controlUIBeanInterface);
 		}
 		else if (controlInterface instanceof DatePickerInterface)
 		{
 			populateDateControlAttributesInUIBean((DatePickerInterface) controlInterface,
 					controlUIBeanInterface);
 		}
 		else if (controlInterface instanceof TextAreaInterface)
 		{
 			populateTextAreaAttributesInUIBean((TextAreaInterface) controlInterface,
 					controlUIBeanInterface);
 		}
 		else if (controlInterface instanceof ListBoxInterface)
 		{
 			populateListBoxAttributesInUIBean((ListBoxInterface) controlInterface,
 					controlUIBeanInterface);
 		}
 		else if (controlInterface instanceof FileUploadInterface)
 		{
 			populateFileUploadAttributesInUIBean((FileUploadInterface) controlInterface,
 					controlUIBeanInterface);
 		}
 		if (controlInterface instanceof SelectControl)
 		{
 			populateSelectControlAttributesInUIBean((SelectControl) controlInterface,
 					controlUIBeanInterface);
 		}
 		if (controlInterface instanceof ContainmentAssociationControl)
 		{
 			populateContainmentAssociationAttributesInUIBean(
 					(ContainmentAssociationControl) controlInterface, controlUIBeanInterface);
 		}
 	}
 
 	/**
 	 * @param control
 	 * @param controlUIBeanInterface
 	 */
 	private void populateContainmentAssociationAttributesInUIBean(
 			ContainmentAssociationControl control, ControlUIBeanInterface controlUIBeanInterface)
 	{
 		// TODO This method will be provided if required.
 	}
 
 	/**
 	 * @param selectControl
 	 * @param controlUIBeanInterface
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void populateSelectControlAttributesInUIBean(SelectControl selectControl,
 			ControlUIBeanInterface controlUIBeanInterface) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		controlUIBeanInterface.setSeparator(selectControl.getSeparator());
 
 		ArrayList<NameValueBean> selectedAttributesList = new ArrayList<NameValueBean>();
 		NameValueBean selectedAttribute = null;
 
 		Collection<AssociationDisplayAttributeInterface> associationAttributeCollection = selectControl
 				.getAssociationDisplayAttributeCollection();
 		ControlInterface control = null;
 		AttributeInterface attribute = null;
 		if (associationAttributeCollection != null)
 		{
 			for (AssociationDisplayAttributeInterface assocnDisplayAttribute : associationAttributeCollection)
 			{
 				if (assocnDisplayAttribute != null)
 				{
 					attribute = assocnDisplayAttribute.getAttribute();
 					if (attribute != null)
 					{
 						EntityManagerInterface entityManager = EntityManager.getInstance();
 						control = entityManager.getControlByAbstractAttributeIdentifier(attribute
 								.getId());
 						if (control != null)
 						{
 							selectedAttribute = new NameValueBean(control.getCaption(), control
 									.getId());
 							selectedAttributesList.add(selectedAttribute);
 						}
 					}
 				}
 			}
 		}
 		controlUIBeanInterface.setSelectedAttributes(selectedAttributesList);
 	}
 
 	/**
 	 * @param fileUploadInterface
 	 * @param controlUIBeanInterface
 	 */
 	private void populateFileUploadAttributesInUIBean(FileUploadInterface fileUploadInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		controlUIBeanInterface.setColumns(fileUploadInterface.getColumns());
 	}
 
 	/**
 	 * @param listBoxInterface
 	 * @param controlUIBeanInterface
 	 */
 	private void populateListBoxAttributesInUIBean(ListBoxInterface listBoxInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		controlUIBeanInterface.setIsMultiSelect(listBoxInterface.getIsMultiSelect());
 		controlUIBeanInterface.setRows(listBoxInterface.getNoOfRows());
 	}
 
 	/**
 	 * @param textAreaInterface
 	 * @param controlUIBeanInterface
 	 */
 	private void populateTextAreaAttributesInUIBean(TextAreaInterface textAreaInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		controlUIBeanInterface.setColumns(textAreaInterface.getColumns());
 		controlUIBeanInterface.setRows(textAreaInterface.getRows());
 		controlUIBeanInterface.setLinesType(ProcessorConstants.LINE_TYPE_MULTILINE);
 
 	}
 
 	/**
 	 * @param dateControlInterface
 	 * @param controlUIBeanInterface
 	 */
 	private void populateDateControlAttributesInUIBean(DatePickerInterface dateControlInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		controlUIBeanInterface.setDateValueType(dateControlInterface.getDateValueType());
 	}
 
 	/**
 	 * @param textInterface
 	 * @param controlUIBeanInterface
 	 */
 	private void populateTextControlAttributesInUIBean(TextFieldInterface textInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		controlUIBeanInterface.setColumns(textInterface.getColumns());
 		controlUIBeanInterface.setIsPassword(textInterface.getIsPassword());
 		controlUIBeanInterface.setLinesType(ProcessorConstants.LINE_TYPE_SINGLELINE);
 		controlUIBeanInterface.setIsUrl(textInterface.getIsUrl());
 	}
 
 	/**
 	 * @param controlInterface
 	 * @param controlUIBeanInterface
 	 */
 	private void populateControlCommonAttributes(ControlInterface controlInterface,
 			ControlUIBeanInterface controlUIBeanInterface)
 	{
 		controlUIBeanInterface.setAbstractAttribute((AbstractAttributeInterface) controlInterface
 				.getBaseAbstractAttribute());
 		controlUIBeanInterface.setCaption(controlInterface.getCaption());
 		controlUIBeanInterface.setIsHidden(controlInterface.getIsHidden());
 		controlUIBeanInterface.setSequenceNumber(controlInterface.getSequenceNumber());
 	}
 
 	/**
 	 * @param subFormContainer
 	 */
 	public ControlInterface createContainmentAssociationControl(ContainerInterface container,
 			AbstractAttributeInterface attributeIntf)
 	{
 		ContainmentAssociationControlInterface containmentAssociationControl = DomainObjectFactory
 				.getInstance().createContainmentAssociationControl();
 		containmentAssociationControl.setCaption(container.getCaption());
 		containmentAssociationControl.setContainer(container);
 		containmentAssociationControl.setBaseAbstractAttribute(attributeIntf);
 		//attributeIntf.setControl((Control) containmentAssociationControl);
		Integer containerSequenceNumber = Integer.valueOf(1);
 		if (container.getControlCollection() != null)
 		{
			containerSequenceNumber = Integer.valueOf(container.getControlCollection().size() + 1);
 		}
 		containmentAssociationControl.setSequenceNumber(containerSequenceNumber);
 
 		return containmentAssociationControl;
 
 	}
 }
