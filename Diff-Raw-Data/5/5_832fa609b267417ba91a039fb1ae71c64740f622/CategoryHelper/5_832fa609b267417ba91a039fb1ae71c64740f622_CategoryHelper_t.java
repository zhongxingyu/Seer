 
 package edu.common.dynamicextensions.util;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.common.dynamicextensions.domain.Category;
 import edu.common.dynamicextensions.domain.CategoryAssociation;
 import edu.common.dynamicextensions.domain.CategoryAttribute;
 import edu.common.dynamicextensions.domain.CategoryEntity;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.PathAssociationRelationInterface;
 import edu.common.dynamicextensions.domain.UserDefinedDE;
 import edu.common.dynamicextensions.domain.userinterface.Container;
 import edu.common.dynamicextensions.domaininterface.AbstractEntityInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PathInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.CategoryAssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.CheckBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.DatePickerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.FileUploadInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ListBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.RadioButtonInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextAreaInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextFieldInterface;
 import edu.common.dynamicextensions.entitymanager.AbstractMetadataManager;
 import edu.common.dynamicextensions.entitymanager.CategoryManager;
 import edu.common.dynamicextensions.entitymanager.CategoryManagerInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 
 /**
  * @author kunal_kamble
  * @author mandar_shidhore
  *
  */
 public class CategoryHelper implements CategoryHelperInterface
 {
 	CategoryManagerInterface categoryManager = CategoryManager.getInstance();
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#createCtaegory(java.lang.String)
 	 */
 	public CategoryInterface getCategory(String name) throws DynamicExtensionsSystemException
 	{
 		
 		CategoryInterface category = (CategoryInterface) ((AbstractMetadataManager)categoryManager).getObjectByName
 			(Category.class.getName(), name);
 		
 		if(category == null)
 		{
 			category = DomainObjectFactory.getInstance().createCategory();
 			category.setName(name);	
 		}
 		
 		return category;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#saveCategory(edu.common.dynamicextensions.domaininterface.CategoryInterface)
 	 */
 	public void saveCategory(CategoryInterface category) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		try
 		{
 			CategoryManagerInterface categoryManager = CategoryManager.getInstance();
 			categoryManager.persistCategory(category);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			e.printStackTrace();
 			throw new DynamicExtensionsSystemException("Error while saving a category");
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			e.printStackTrace();
 			throw new DynamicExtensionsApplicationException("Error while saving a category");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#createCategoryEntity(java.lang.String, edu.common.dynamicextensions.domaininterface.CategoryInterface[])
 	 */
 	public ContainerInterface createOrUpdateCategoryEntityAndContainer(EntityInterface entity,
 			String containerCaption, CategoryInterface category, String... categoryEntityName)
 	{
 		CategoryEntityInterface categoryEntity = null;
 		String newCategoryEntityName = null;
 		if(categoryEntityName.length > 0)
 		{
 			newCategoryEntityName = categoryEntityName[0]; 
 			categoryEntity = category.getCategoryEntityByName(newCategoryEntityName);
 		}
 		
 		
 		if(categoryEntity == null)
 		{
 			categoryEntity = DomainObjectFactory.getInstance().createCategoryEntity();
 			categoryEntity.setName(newCategoryEntityName);
 			categoryEntity.setEntity(entity);
 		}
 		
 		ContainerInterface container = createContainer(categoryEntity, containerCaption);
 		return container;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#setRootCategoryEntity(edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface, edu.common.dynamicextensions.domaininterface.CategoryInterface)
 	 */
 	public void setRootCategoryEntity(ContainerInterface container, CategoryInterface category)
 	{
 		category.setRootCategoryElement((CategoryEntityInterface) container.getAbstractEntity());
 		((CategoryEntityInterface) container.getAbstractEntity()).setCategory(category);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#addControl(edu.common.dynamicextensions.domaininterface.AttributeInterface, edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface, edu.common.dynamicextensions.categoryManager.CategoryHelperInterface.ControlEnum, java.util.List<edu.common.dynamicextensions.domaininterface.PermissibleValueInterface>[])
 	 */
 	public ControlInterface addOrUpdateControl(EntityInterface entity, String attributeName, ContainerInterface container, ControlEnum controlType,
 			String controlCaption, List<String>... permissibleValueList) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		CategoryAttributeInterface categoryAttribute = (CategoryAttributeInterface) getCategoryAttribute(attributeName, container);
 		if(categoryAttribute == null)
 		{
 			categoryAttribute = DomainObjectFactory.getInstance().createCategoryAttribute();
 			categoryAttribute.setName(attributeName + " Category Attribute");
 			categoryAttribute.setAttribute(entity.getAttributeByName(attributeName));
 
 			CategoryEntity categoryEntity = (CategoryEntity) container.getAbstractEntity();
 			categoryEntity.addCategoryAttribute(categoryAttribute);
 			categoryAttribute.setCategoryEntity(categoryEntity);
 
 			categoryEntity.addCategoryAttribute(categoryAttribute);
 			categoryAttribute.setCategoryEntity(categoryEntity);
 		}
 
 		ControlInterface control = null;
 		List<String> permissibleValueNameList = (permissibleValueList.length==0?null:permissibleValueList[0]);
 		control = createOrUpdateControl(controlType,controlCaption,container,categoryAttribute,permissibleValueNameList);
 		
 
 		control.setCaption(controlCaption);
 
 		
 		return control;
 	}
 
 	private ControlInterface createOrUpdateControl(ControlEnum controlType, String controlCaption, ContainerInterface container, 
 			CategoryAttributeInterface categoryAttribute,List<String> permissibleValueNameList) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		ControlInterface control = null;
 		EntityInterface entity = categoryAttribute.getCategoryEntity().getEntity();
 		String attributeName = categoryAttribute.getAttribute().getName();
 		switch (controlType)
 		{
 			case TEXT_FIELD_CONTROL :
 				control = 
 					createOrUpdateTextFieldControl(container, categoryAttribute);
 				break;
 			case LIST_BOX_CONTROL :
 				control = createOrUpdateListBoxControl(container, categoryAttribute, createPermissibleValuesList(entity, attributeName,
 						permissibleValueNameList));
 				break;
 			case DATE_PICKER_CONTROL :
 				control = createOrUpdateDatePickerControl(container, categoryAttribute);
 				break;
 			case FILE_UPLOAD_CONTROL :
 				control = createOrUpdateFileUploadControl(container, categoryAttribute);
 				break;
 			case TEXT_AREA_CONTROL :
 				control = createOrUpdateTextAreaControl(container, categoryAttribute);
 				break;
 			case RADIO_BUTTON_CONTROL :
 				control = createOrUpdateRadioButtonControl(container, categoryAttribute, createPermissibleValuesList(entity, attributeName,
 						permissibleValueNameList));
 				break;
 			case CHECK_BOX_CONTROL :
 				control = createOrUpdateCheckBoxControl(container, categoryAttribute);
 				break;
 		}
 		
 		control.setCaption(controlCaption);
 		return control;
 		
 	}
 
 	private BaseAbstractAttributeInterface getCategoryAttribute(String attributeName, ContainerInterface container)
 	{
 		BaseAbstractAttributeInterface categoryAttribute = null;
 		for(ControlInterface control : container.getControlCollection())
 		{
 			if(control.getBaseAbstractAttribute() instanceof CategoryAssociation)
 			{
 					
 			}
 			else{
 				if(((CategoryAttributeInterface)control.getBaseAbstractAttribute()).
 						getAttribute().getName().equals(attributeName))
 				{
 					categoryAttribute = control.getBaseAbstractAttribute();
 					break;
 				}	
 			}
 			
 		}
 		return categoryAttribute;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.categoryManager.CategoryHelperInterface#setParentCategoryEntity(edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface, edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface)
 	 */
 	public void setParentContainer(ContainerInterface parentContainer, ContainerInterface childContainer)
 	{
 		CategoryEntityInterface parentCategoryEntity = (CategoryEntity) parentContainer.getAbstractEntity();
 		CategoryEntityInterface childCategoryEntity = (CategoryEntity) childContainer.getAbstractEntity();
 		childCategoryEntity.setParentCategoryEntity(parentCategoryEntity);
 
 		childContainer.setBaseContainer(parentContainer);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.wustl.catissuecore.test.CategoryHelperInterface#associateCategoryContainers(edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface, edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface, java.util.List, int)
 	 */
 	public CategoryAssociationControlInterface associateCategoryContainers(ContainerInterface sourceContainer, ContainerInterface targetContainer,
 			List<String> associationNamesList, int noOfEntries) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ControlInterface controlInterface = getControl(sourceContainer, targetContainer.getCaption());
 		if(controlInterface != null)
 		{
 			return (CategoryAssociationControlInterface) controlInterface;
 		}
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		PathInterface path = factory.createPath();
 
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 
 		int pathSequenceNumber = 1;
 
 		for (String associationName : associationNamesList)
 		{
 			PathAssociationRelationInterface pathAssociationRelation = factory.createPathAssociationRelation();
 			pathAssociationRelation.setPathSequenceNumber(pathSequenceNumber++);
 			pathAssociationRelation.setAssociation(entityManager.getAssociationByName(associationName));
 
 			pathAssociationRelation.setPath(path);
 			path.addPathAssociationRelation(pathAssociationRelation);
 		}
 
 		CategoryEntityInterface sourceCategoryEntity = (CategoryEntityInterface) sourceContainer.getAbstractEntity();
 		CategoryEntityInterface targetCategoryEntity = (CategoryEntityInterface) targetContainer.getAbstractEntity();
 
 		// Add path information.
 		addPathBetweenCategoryEntities(sourceCategoryEntity, targetCategoryEntity, path);
 		targetCategoryEntity.setNumberOfEntries(noOfEntries);
 
 		CategoryAssociationInterface categoryAssociation = associateCategoryEntities(sourceCategoryEntity, targetCategoryEntity, sourceCategoryEntity
 				.getName()
 				+ " to " + targetCategoryEntity.getName() + " category association");
 
 		CategoryAssociationControlInterface categoryAssociationControl = createCategoryAssociationControl(sourceContainer, targetContainer,
 				categoryAssociation, targetContainer.getCaption());
 
 		return categoryAssociationControl;
 	}
 
 	private ControlInterface getControl(ContainerInterface sourceContainer, String caption)
 	{
 		ControlInterface control = null;
 		
 		for(ControlInterface controlInterface : sourceContainer.getControlCollection() )
 		{
 			if(caption.equals(controlInterface.getCaption()))
 			{
 				control = controlInterface;
 				break;
 			}
 		}
 		return control;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.wustl.catissuecore.test.CategoryHelperInterface#getNextSequenceNumber(edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface)
 	 */
 	public int getNextSequenceNumber(ContainerInterface container)
 	{
 		int nextSequenceNumber = 1;
 
 		if (container.getAllControls() != null)
 		{
 			nextSequenceNumber = container.getAllControls().size() + 1;
 		}
 
 		return nextSequenceNumber;
 	}
 
 	/**
 	 * @param sourceCategoryEntity source category entity 
 	 * @param targetCategoryEntity target category entity 
 	 * @param path path information between the category entities
 	 */
 	private void addPathBetweenCategoryEntities(CategoryEntityInterface sourceCategoryEntity, CategoryEntityInterface targetCategoryEntity,
 			PathInterface path)
 	{
 		targetCategoryEntity.setPath(path);
 		sourceCategoryEntity.addChildCategory(targetCategoryEntity);
 		targetCategoryEntity.setParentCategoryEntity(sourceCategoryEntity);
 	}
 
 	/**
 	 * Method associates the source and the target category entity
 	 * @param sourceCategoryEntity source category entity 
 	 * @param targetCategoryEntity target category entity 
 	 * @param name name of the category association
 	 * @return CategoryAssociationInterface category association object
 	 */
 	private CategoryAssociationInterface associateCategoryEntities(CategoryEntityInterface sourceCategoryEntity,
 			CategoryEntityInterface targetCategoryEntity, String name)
 	{
 		CategoryAssociationInterface categoryAssociation = DomainObjectFactory.getInstance().createCategoryAssociation();
 		categoryAssociation.setName(name);
 		categoryAssociation.setTargetCategoryEntity(targetCategoryEntity);
 		categoryAssociation.setCategoryEntity(sourceCategoryEntity);
 
 		sourceCategoryEntity.getCategoryAssociationCollection().add(categoryAssociation);
 
 		return categoryAssociation;
 	}
 
 	/**
 	 * Method creates the association between the given parent and the target container 
 	 * @param parentContainer main form
 	 * @param targetContainer sub form
 	 * @param categoryAssociation association between category entities
 	 * @param caption name to be displayed on UI
 	 * @return CategoryAssociationControlInterface category association control object
 	 */
 	private CategoryAssociationControlInterface createCategoryAssociationControl(ContainerInterface parentContainer,
 			ContainerInterface targetContainer, CategoryAssociationInterface categoryAssociation, String caption)
 	{
 		CategoryAssociationControlInterface categoryAssociationControl = DomainObjectFactory.getInstance().createCategoryAssociationControl();
 		categoryAssociationControl.setCaption(caption);
 		categoryAssociationControl.setContainer(targetContainer);
 		categoryAssociationControl.setBaseAbstractAttribute(categoryAssociation);
 		categoryAssociationControl.setSequenceNumber(getNextSequenceNumber(parentContainer));
 		categoryAssociationControl.setParentContainer((Container) parentContainer);
 
 		parentContainer.addControl(categoryAssociationControl);
 
 		return categoryAssociationControl;
 	}
 
 	/**
 	 * @param abstractEntity category entity
 	 * @return container object for category entity
 	 */
 	private ContainerInterface createContainer(AbstractEntityInterface abstractEntity, String caption)
 	{
 		ContainerInterface container = null;
 		if(abstractEntity.getContainerCollection().size() > 0)
 		{
 			container = new ArrayList<ContainerInterface>(abstractEntity.getContainerCollection()).get(0);
 		}
 		if(container == null)
 		{
 			container = DomainObjectFactory.getInstance().createContainer(); 
 			container.setMainTableCss("formRequiredLabel");
 			container.setRequiredFieldIndicatior("*");
 			container.setRequiredFieldWarningMessage("indicates mandatory fields.");
 			container.setAbstractEntity(abstractEntity);
 			abstractEntity.addContainer(container);
 		}
 		
 		if(caption == null)
 		{
 			caption = abstractEntity.getName() + " category container";
 		}
 		
 		container.setCaption(caption);
 
 		return container;
 	}
 
 	/**
 	 * 
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @return text field object
 	 */
 	private TextFieldInterface createOrUpdateTextFieldControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		TextFieldInterface textField = (TextFieldInterface) getControl(container,baseAbstractAttribute);
 		if(textField == null)
 		{
 			textField = DomainObjectFactory.getInstance().createTextField();
 			textField.setColumns(50);
 			textField.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, textField, baseAbstractAttribute);
 		}
 
 		return textField;
 	}
 
 	private ControlInterface getControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		ControlInterface controlInterface = null;
 		for(ControlInterface control: container.getControlCollection())
 		{
 			if(baseAbstractAttribute.equals(control.getBaseAbstractAttribute()))
 			{
 				controlInterface = control;
 			}
 		}
 		return controlInterface;
 	}
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @param permissibleValues list of permissible values
 	 * @return list box object
 	 */
 	private ListBoxInterface createOrUpdateListBoxControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute,
 			List<PermissibleValueInterface> permissibleValues)
 	{
 		ListBoxInterface listBox = (ListBoxInterface) getControl(container,baseAbstractAttribute);
 		if(listBox == null)
 		{
 			listBox = DomainObjectFactory.getInstance().createListBox();
 			listBox.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, listBox, baseAbstractAttribute);
 		}
 		//clear old permissible values
 		((CategoryAttribute) baseAbstractAttribute).clearDataElementCollection();
 		
 		UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
 		for (PermissibleValueInterface pv : permissibleValues)
 		{
 			userDefinedDE.addPermissibleValue(pv);
 		}
 		
 		//add new permissible values
 		((CategoryAttribute) baseAbstractAttribute).setDataElement(userDefinedDE);
 		AttributeTypeInformationInterface attributeTypeInformation = ((CategoryAttribute) baseAbstractAttribute).getAttribute()
 				.getAttributeTypeInformation();
		if(attributeTypeInformation.getDefaultValue() != null)
		{
			((CategoryAttribute) baseAbstractAttribute).setDefaultValue(attributeTypeInformation.getDefaultValue());
		}
 		return listBox;
 	}
 
 	/**
 	 * @param container
 	 * @param control
 	 * @param baseAbstractAttribute
 	 */
 	private void updateContainerAndControl(ContainerInterface container, ControlInterface control,
 			BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		container.addControl(control);
 		control.setParentContainer((Container)container);
 		control.setBaseAbstractAttribute(baseAbstractAttribute);		
 	}
 
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @return date picker object
 	 */
 	private DatePickerInterface createOrUpdateDatePickerControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		DatePickerInterface datePicker = (DatePickerInterface) getControl(container,baseAbstractAttribute);
 		if(datePicker == null)
 		{
 			datePicker = DomainObjectFactory.getInstance().createDatePicker();
 			datePicker.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, datePicker, baseAbstractAttribute);
 		}
 		
 		datePicker.setDateValueType(ProcessorConstants.DATE_ONLY_FORMAT);
 
 		return datePicker;
 	}
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @return file upload object
 	 */
 	private FileUploadInterface createOrUpdateFileUploadControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		FileUploadInterface fileUpload = (FileUploadInterface) getControl(container,baseAbstractAttribute);
 		if(fileUpload == null)
 		{
 			fileUpload = DomainObjectFactory.getInstance().createFileUploadControl();
 			fileUpload.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, fileUpload, baseAbstractAttribute);
 		}
 		
 		return fileUpload;
 	}
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @return text area object
 	 */
 	private TextAreaInterface createOrUpdateTextAreaControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		TextAreaInterface textArea = (TextAreaInterface) getControl(container, baseAbstractAttribute);
 		if (textArea == null)
 		{
 			textArea = DomainObjectFactory.getInstance().createTextArea();
 			textArea.setSequenceNumber(getNextSequenceNumber(container));
 			textArea.setColumns(50);
 			textArea.setRows(5);
 			updateContainerAndControl(container, textArea, baseAbstractAttribute);
 		}
 		
 		return textArea;
 	}
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @param permissibleValues list of permissible values
 	 * @return RadioButtonInterface radio button object
 	 */
 	private RadioButtonInterface createOrUpdateRadioButtonControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute,
 			List<PermissibleValueInterface> permissibleValues)
 	{
 		RadioButtonInterface radioButton = (RadioButtonInterface) getControl(container,baseAbstractAttribute);
 		if(radioButton == null)
 		{
 			radioButton = DomainObjectFactory.getInstance().createRadioButton();
 			radioButton.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, radioButton, baseAbstractAttribute);
 		}
 
 		UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
 		for (PermissibleValueInterface pv : permissibleValues)
 		{
 			userDefinedDE.addPermissibleValue(pv);
 		}
 
 		((CategoryAttribute) baseAbstractAttribute).setDataElement(userDefinedDE);
 
 
 		return radioButton;
 	}
 
 	/**
 	 * @param container category entity container
 	 * @param baseAbstractAttribute category attribute
 	 * @return check box object
 	 */
 	private CheckBoxInterface createOrUpdateCheckBoxControl(ContainerInterface container, BaseAbstractAttributeInterface baseAbstractAttribute)
 	{
 		CheckBoxInterface checkBox = (CheckBoxInterface) getControl(container,baseAbstractAttribute); 
 		if(checkBox == null)
 		{
 			checkBox = DomainObjectFactory.getInstance().createCheckBox();
 			checkBox.setSequenceNumber(getNextSequenceNumber(container));
 			updateContainerAndControl(container, checkBox, baseAbstractAttribute);
 		}
 		
 		return checkBox;
 	}
 
 	/**
 	 * This method creates a list of permissible values for a category attribute
 	 * @param entity entity which contains attribute by the given name
 	 * @param attributeName name of the attribute
 	 * @param desiredPermissibleValues subset of permissible values for this category attribute
 	 * @return list of permissible values for category attribute
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public List<PermissibleValueInterface> createPermissibleValuesList(EntityInterface entity, String attributeName,
 			List<String> desiredPermissibleValues) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		List<PermissibleValueInterface> permissibleValues = null;
 		
 		try
 		{
 			AttributeInterface attribute = entity.getAttributeByName(attributeName);
 			AttributeTypeInformationInterface attributeTypeInformation = attribute.getAttributeTypeInformation();
 			UserDefinedDEInterface userDefinedDE = (UserDefinedDE) attributeTypeInformation.getDataElement();
 			
 			if (userDefinedDE == null || userDefinedDE.getPermissibleValueCollection() == null || userDefinedDE.getPermissibleValueCollection().size() == 0)
 			{
 				permissibleValues = addNewPermissibleValues(attributeTypeInformation,desiredPermissibleValues);
 			}
 			else
 			{
 				permissibleValues = getSubsetOfPermissibleValues(attribute,desiredPermissibleValues);
 			}
 		}
 		catch(ParseException parseException)
 		{
 			throw new DynamicExtensionsSystemException("Parse Exception",parseException);
 		}
 		return permissibleValues;
 	}
 
 	/**
 	 * 
 	 * @param attributeTypeInformation
 	 * @param desiredPermissibleValues
 	 * @return
 	 * @throws DynamicExtensionsApplicationException 
 	 */
 	private List<PermissibleValueInterface> getSubsetOfPermissibleValues(AttributeInterface attributeInterface, List<String> desiredPermissibleValues) throws DynamicExtensionsApplicationException 
 	 {
 		List<PermissibleValueInterface> permissibleValues = new ArrayList<PermissibleValueInterface>();
 		
 		AttributeTypeInformationInterface attributeTypeInformation = attributeInterface.getAttributeTypeInformation();
 		UserDefinedDEInterface userDefinedDE = (UserDefinedDEInterface) attributeTypeInformation.getDataElement();
 		
 		CategoryManagerInterface categoryManager = CategoryManager.getInstance();
 		
 		//if no prmissible values are defined, copy  the all the permissible values 
 		//of the original attribute
 		if(desiredPermissibleValues == null)
 		{
 			permissibleValues.addAll(userDefinedDE.getPermissibleValueCollection());
 		}		
 		else if (categoryManager.isPermissibleValuesSubsetValid(userDefinedDE, desiredPermissibleValues))
 		{
 			permissibleValues = new ArrayList<PermissibleValueInterface>();
 			for (PermissibleValueInterface pv : userDefinedDE.getPermissibleValueCollection())
 			{
 				if (desiredPermissibleValues.contains(pv.getValueAsObject().toString()))
 				{
 					permissibleValues.add(pv);
 				}
 			}
 		}
 		else
 		{
 			throw new DynamicExtensionsApplicationException("Invalid subset of persmissible values. Original set of permissible values for the attribute "+ attributeInterface.getName() +" of the entity "+attributeInterface.getEntity().getName() + "is different.");
 		}
 		return permissibleValues;
 		
 	}
 
 	/**
 	 * 
 	 * @param attributeTypeInformation
 	 * @param desiredPermissibleValues
 	 * @return
 	 * @throws DynamicExtensionsSystemException 
 	 * @throws ParseException 
 	 */
 	private List<PermissibleValueInterface> addNewPermissibleValues(AttributeTypeInformationInterface attributeTypeInformation, List<String> desiredPermissibleValues) throws DynamicExtensionsSystemException, ParseException {
 		
 		List<PermissibleValueInterface> permissibleValues = new ArrayList<PermissibleValueInterface>();
 		PermissibleValueInterface permissibleValueInterface = null;
 			
 		for(String value : desiredPermissibleValues)
 		{
 			permissibleValueInterface = attributeTypeInformation.getPermissibleValueForString(value);
 			permissibleValues.add(permissibleValueInterface);
 		}
 		return permissibleValues;
 	}
 
 	
 }
