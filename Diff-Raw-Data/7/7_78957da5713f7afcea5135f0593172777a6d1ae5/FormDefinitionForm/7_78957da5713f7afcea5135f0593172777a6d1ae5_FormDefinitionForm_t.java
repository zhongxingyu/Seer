 
 package edu.common.dynamicextensions.ui.webui.actionform;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface;
 import edu.common.dynamicextensions.ui.interfaces.EntityUIBeanInterface;
 import edu.common.dynamicextensions.util.global.DEConstants;
 import edu.wustl.common.actionForm.AbstractActionForm;
 import edu.wustl.common.domain.AbstractDomainObject;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.global.Validator;
 
 /**
  * This actionForm stores information about the Container and the Entity.
  * @author deepti_shelar
  *
  */
 public class FormDefinitionForm extends AbstractActionForm
 		implements
 			EntityUIBeanInterface,
 			ContainerUIBeanInterface
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Name : 
 	 */
 	protected String formName;
 
 	/**
 	 * Description
 	 */
 	protected String formDescription;
 	/**
 	 * Entity Identifier
 	 */
 	//protected String entityIdentifier;
 	/**
 	 * CreateAs
 	 */
 	protected String createAs;
 	/**
 	 * view as 
 	 */
 	protected String viewAs;
 
 	/**
 	 * 
 	 */
 	protected String formCaption;
 
 	/**
 	 * 
 	 */
 	protected String conceptCode;
 
 	/**
 	 * operation mode
 	 */
 	protected String operationMode;
 
 	/**
 	 * Current container identifier
 	 */
 	//protected String containerIdentifier;
 	/**
 	 * current group name
 	 */
 	protected String groupName;
 
 	//protected TreeData treeData;
 	//protected TreeData associationTree;
 	/**
 	 * XML String for current entity tree representation
 	 */
 	protected String currentEntityTreeXML;
 	/**
 	 * XML String for already defined entities
 	 */
 	protected String definedEntitiesTreeXML;
 	/**
 	 * Selected object id
 	 * 
 	 */
 	protected String selectedObjectId;
 
 	/*
 	 * This stores the parent container id for edit sub-form operation
 	 */
 	protected String currentContainerName;
 	/**
 	 * 
 	 */
 	protected List formList;
 	/**
 	 * 
 	 */
 	protected String parentForm;
 	/**
 	 * 
 	 */
 	protected String isAbstract;
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getCurrentContainerName()
 	{
 		return this.currentContainerName;
 	}
 
 	/**
 	 * 
 	 * @param currentContainerName
 	 */
 	public void setCurrentContainerName(String currentContainerName)
 	{
 		this.currentContainerName = currentContainerName;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getSelectedObjectId()
 	{
 		return this.selectedObjectId;
 	}
 
 	/**
 	 * @param selectedObjectId
 	 */
 	public void setSelectedObjectId(String selectedObjectId)
 	{
 		this.selectedObjectId = selectedObjectId;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getGroupName()
 	{
 		return this.groupName;
 	}
 
 	/**
 	 * @param groupName
 	 */
 	public void setGroupName(String groupName)
 	{
 		this.groupName = groupName;
 	}
 
 	/**
 	 * default constructor
 	 *
 	 */
 	public FormDefinitionForm()
 	{
		super();
 		createAs = ProcessorConstants.DEFAULT_FORM_CREATEAS;
 	}
 
 	/**
 	 * @return the mode
 	 */
 	/*
 	 public String getMode()
 	 {
 	 return mode;
 	 }
 
 	 *//**
 			 * @param mode the mode to set
 			 */
 	/*
 	 public void setMode(String mode)
 	 {
 	 this.mode = mode;
 	 }*/
 
 	/**
 	 * Returns the id assigned to form bean.
 	 * @return the id assigned to form bean.
 	 */
 	public int getFormId()
 	{
 		return DEConstants.ENTITY_FORM_ID;
 	}
 
 	/**
 	 * @param abstractDomain abstractDomain
 	 */
 	public void setAllValues(AbstractDomainObject abstractDomain)
 	{
 		// TODO empty method
 	}
 
 	/**
 	 * @return Returns the description.
 	 */
 	public String getFormDescription()
 	{
 		return formDescription;
 	}
 
 	/**
 	 * @param description The description to set.
 	 */
 	public void setFormDescription(String description)
 	{
 		this.formDescription = description;
 	}
 
 	/**
 	 * @return Returns the name.
 	 */
 	public String getFormName()
 	{
 		return formName;
 	}
 
 	/**
 	 * @param formName The name to set.
 	 */
 	public void setFormName(String formName)
 	{
 		this.formName = formName;
 	}
 
 	/*
 	 *//**
 			 * @return Returns the entityIdentifier.
 			 */
 	/*
 	 public String getEntityIdentifier()
 	 {
 	 return entityIdentifier;
 	 }
 
 	 *//**
 			 * @param entityIdentifier The entityIdentifier to set.
 			 */
 	/*
 	 public void setEntityIdentifier(String entityIdentifier)
 	 {
 	 this.entityIdentifier = entityIdentifier;
 	 }*/
 
 	/**
 	 * gets CreateAs
 	 * @return String createAs
 	 */
 	public String getCreateAs()
 	{
 		return createAs;
 	}
 
 	/**
 	 * @param createAs createAs
 	 */
 	public void setCreateAs(String createAs)
 	{
 		this.createAs = createAs;
 	}
 
 	/**
 	 * @return formCaption
 	 */
 	public String getFormCaption()
 	{
 		return formCaption;
 	}
 
 	/**
 	 * @param caption FormCaption
 	 */
 	public void setFormCaption(String caption)
 	{
 		this.formCaption = caption;
 	}
 
 	/**
 	 * 
 	 */
 	public void reset()
 	{
 		formName = "";
 		formDescription = "";
 		createAs = "";
 	}
 
 	/**
 	 * Overrides the validate method of ActionForm.
 	 * @param mapping ActionMapping mapping
 	 * @param request HttpServletRequest request
 	 * @return ActionErrors
 	 * */
 	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request)
 	{
 		ActionErrors errors = new ActionErrors();
 		Validator validator = new Validator();
 		if ((operationMode != null) && (operationMode.equals(DEConstants.ADD_SUB_FORM_OPR)))
 		{
 			if ((!ProcessorConstants.CREATE_FROM_EXISTING.equals(createAs))
 					&& ((formName == null) || (validator.isEmpty(String.valueOf(formName)))))
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 						ApplicationProperties.getValue("eav.form.title")));
 			}
 
 			if ((createAs.equals(ProcessorConstants.CREATE_FROM_EXISTING))
 					&& ((getSelectedObjectId() == null) || (getSelectedObjectId().trim().equals(""))))
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 						ApplicationProperties.getValue("eav.form.title")));
 			}
 
 			if ((createAs == null) || (validator.isEmpty(String.valueOf(createAs))))
 			{
 				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 						ApplicationProperties.getValue("eav.form.createAs")));
 			}
 		}
 		return errors;
 	}
 
 	/**
 	 * @return the conceptCode
 	 */
 	public String getConceptCode()
 	{
 		return conceptCode;
 	}
 
 	/**
 	 * @param conceptCode the conceptCode to set
 	 */
 	public void setConceptCode(String conceptCode)
 	{
 		this.conceptCode = conceptCode;
 	}
 
 	/**
 	 * @return the containerIdentifier
 	 */
 	/*
 	 public String getContainerIdentifier()
 	 {
 	 return containerIdentifier;
 	 }
 
 	 *//**
 			 * @param containerIdentifier the containerIdentifier to set
 			 */
 	/*
 	 public void setContainerIdentifier(String containerIdentifier)
 	 {
 	 this.containerIdentifier = containerIdentifier;
 	 }
 	 */
 	/**
 	 * @return the operationMode
 	 */
 	public String getOperationMode()
 	{
 		return operationMode;
 	}
 
 	/**
 	 * @param operationMode the operationMode to set
 	 */
 	public void setOperationMode(String operationMode)
 	{
 		this.operationMode = operationMode;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getViewAs()
 	{
 		return this.viewAs;
 	}
 
 	/**
 	 * @param viewAs
 	 */
 	public void setViewAs(String viewAs)
 	{
 		this.viewAs = viewAs;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#getButtonCss()
 	 */
 	public String getButtonCss()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#getMainTableCss()
 	 */
 	public String getMainTableCss()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#getRequiredFieldIndicatior()
 	 */
 	public String getRequiredFieldIndicatior()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#getRequiredFieldWarningMessage()
 	 */
 	public String getRequiredFieldWarningMessage()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#getTitleCss()
 	 */
 	public String getTitleCss()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#setButtonCss(java.lang.String)
 	 */
 	public void setButtonCss(String buttonCss)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#setMainTableCss(java.lang.String)
 	 */
 	public void setMainTableCss(String mainTableCss)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#setRequiredFieldIndicatior(java.lang.String)
 	 */
 	public void setRequiredFieldIndicatior(String requiredFieldIndicatior)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#setRequiredFieldWarningMessage(java.lang.String)
 	 */
 	public void setRequiredFieldWarningMessage(String requiredFieldWarningMessage)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#setTitleCss(java.lang.String)
 	 */
 	public void setTitleCss(String titleCss)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * @return
 	 */
 	public String getCurrentEntityTreeXML()
 	{
 		return this.currentEntityTreeXML;
 	}
 
 	/**
 	 * @param currentEntityTreeXML
 	 */
 	public void setCurrentEntityTreeXML(String currentEntityTreeXML)
 	{
 		this.currentEntityTreeXML = currentEntityTreeXML;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getDefinedEntitiesTreeXML()
 	{
 		return this.definedEntitiesTreeXML;
 	}
 
 	/**
 	 * @param definedEntitiesTreeXML
 	 */
 	public void setDefinedEntitiesTreeXML(String definedEntitiesTreeXML)
 	{
 		this.definedEntitiesTreeXML = definedEntitiesTreeXML;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#getFormList()
 	 */
 	public List getFormList()
 	{
 		return formList;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#setFormList(java.util.List)
 	 */
 	public void setFormList(List formList)
 	{
 		this.formList = formList;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.EntityUIBeanInterface#getIsAbstract()
 	 */
 	public String getIsAbstract()
 	{
 		return isAbstract;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.EntityUIBeanInterface#setIsAbstract(java.lang.String)
 	 */
 	public void setIsAbstract(String isAbstract)
 	{
 		this.isAbstract = isAbstract;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#getParentForm()
 	 */
 	public String getParentForm()
 	{
 		return parentForm;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.ContainerUIBeanInterface#setParentForm(java.lang.String)
 	 */
 	public void setParentForm(String parentForm)
 	{
 		this.parentForm = parentForm;
 	}
 
 	/**
 	 * This method set Identifier of newly added object by AddNew operation into FormBean
 	 * which initialized AddNew operation.
 	 * @param addNewFor - add New For.
 	 * @param addObjectIdentifier - Identifier of newly added object by AddNew operation
 	 */
 	@Override
 	public void setAddNewObjectIdentifier(String addNewFor, Long addObjectIdentifier)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 }
