 /*
  * Created on Nov 15, 2006
  * @author
  *
  */
 
 package edu.common.dynamicextensions.ui.webui.actionform;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.interfaces.GroupUIBeanInterface;
 import edu.wustl.common.actionForm.AbstractActionForm;
 import edu.wustl.common.domain.AbstractDomainObject;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.global.Validator;
 
 /**
  * @author preeti_munot
  */
 public class GroupForm extends AbstractActionForm implements GroupUIBeanInterface
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	/**
 	 * 
 	 */
 	protected String createGroupAs;
 	/**
 	 * 
 	 */
 	protected String groupName;
 	/**
 	 * 
 	 */
 	protected String groupNameText;
 	/**
 	 * 
 	 */
 	protected String groupDescription;
 	/**
 	 * 
 	 */
 	protected List groupList;
 	/**
 	 * 
 	 */
 	protected String groupOperation;
 	/**
 	 * 
 	 */
 	protected String operationMode;
 	/**
 	 * 
 	 */
 	protected String containerIdentifier;
 	/**
 	 * 
 	 */
 	protected String tempgroupNameText;
 	/**
 	 * 
 	 */
 	protected String tempgroupDescription;
 	/**
 	 * 
 	 */
 	protected String tempgroupName;
 
 	/**
 	 * 
 	 */
 	public String getGroupOperation()
 	{
 		return this.groupOperation;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.GroupUIBeanInterface#setGroupOperation(java.lang.String)
 	 */
 	public void setGroupOperation(String groupOperation)
 	{
 		this.groupOperation = groupOperation;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public List getGroupList()
 	{
 		return this.groupList;
 	}
 
 	/**
 	 * 
 	 * @param groupList
 	 */
 	public void setGroupList(List groupList)
 	{
 		this.groupList = groupList;
 	}
 
 	/**
 	 * 
 	 *
 	 */
 	public GroupForm()
 	{
 		this.createGroupAs = ProcessorConstants.DEFAULT_GROUP_CREATEAS;
 	}
 
 	/**
 	 * @return 
 	 */
 	public String getCreateGroupAs()
 	{
 		return this.createGroupAs;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.GroupUIBeanInterface#setCreateGroupAs(java.lang.String)
 	 */
 	public void setCreateGroupAs(String createGroupAs)
 	{
 		this.createGroupAs = createGroupAs;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getGroupDescription()
 	{
 		return this.groupDescription;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.GroupUIBeanInterface#setGroupDescription(java.lang.String)
 	 */
 	public void setGroupDescription(String groupDescription)
 	{
 		this.groupDescription = groupDescription;
 	}
 
 	/**
 	 * @return 
 	 */
 	public String getGroupName()
 	{
 		return this.groupName;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.ui.interfaces.GroupUIBeanInterface#setGroupName(java.lang.String)
 	 */
 	public void setGroupName(String groupName)
 	{
 		this.groupName = groupName;
 		this.tempgroupName = groupName;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.wustl.common.actionForm.AbstractActionForm#getFormId()
 	 */
 	@Override
 	public int getFormId()
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.wustl.common.actionForm.AbstractActionForm#reset()
 	 */
 	@Override
 	protected void reset()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.wustl.common.actionForm.AbstractActionForm#setAllValues(edu.wustl.common.domain.AbstractDomainObject)
 	 */
 	@Override
 	//public void setAllValues(AbstractDomainObject arg0)
 	//{
 	// TODO Auto-generated method stub
 	//}
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
 
 		if (createGroupAs != null && createGroupAs.equals(ProcessorConstants.GROUP_CREATEAS_NEW)
 				&& (groupNameText == null || validator.isEmpty(String.valueOf(groupNameText))))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 					ApplicationProperties.getValue("eav.att.GroupTitle")));
 		}
 		else if (createGroupAs != null
 				&& createGroupAs.equals(ProcessorConstants.GROUP_CREATEFROM_EXISTING)
 				&& (groupName == null || validator.isEmpty(String.valueOf(groupName))))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 					ApplicationProperties.getValue("eav.att.GroupTitle")));
 		}
 		else if (createGroupAs == null)
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.item.required",
 					ApplicationProperties.getValue("eav.att.NewGroup")));
 		}
 
 		if ((groupDescription != null)
 				&& (groupDescription.length() > ProcessorConstants.MAX_LENGTH_DESCRIPTION))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.maxlength.exceeded",
 					ApplicationProperties.getValue("eav.att.Description"),
 					ProcessorConstants.MAX_LENGTH_DESCRIPTION));
 		}
 
 		if ((groupNameText != null)
 				&& (groupNameText.length() > ProcessorConstants.MAX_LENGTH_NAME))
 		{
 			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("errors.maxlength.exceeded",
 					ApplicationProperties.getValue("eav.att.GroupTitle"),
 					ProcessorConstants.MAX_LENGTH_NAME));
 		}
 		return errors;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getContainerIdentifier()
 	{
 		return this.containerIdentifier;
 	}
 
 	/**
 	 * 
 	 * @param containerIdentifier
 	 */
 	public void setContainerIdentifier(String containerIdentifier)
 	{
 		this.containerIdentifier = containerIdentifier;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 
 	public String getOperationMode()
 	{
 		return this.operationMode;
 	}
 
 	/**
 	 * 
 	 * @param operationMode
 	 */
 	public void setOperationMode(String operationMode)
 	{
 		this.operationMode = operationMode;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getGroupNameText()
 	{
 		return groupNameText;
 	}
 
 	/**
 	 * 
 	 * @param groupNameText
 	 */
 	public void setGroupNameText(String groupNameText)
 	{
 		this.groupNameText = groupNameText;
 	}
 
 	/**
 	 * @return the tempgroupDescription
 	 */
 	public String getTempgroupDescription()
 	{
 		return tempgroupDescription;
 	}
 
 	/**
 	 * @param tempgroupDescription the tempgroupDescription to set
 	 */
 	public void setTempgroupDescription(String tempgroupDescription)
 	{
 		this.tempgroupDescription = tempgroupDescription;
 	}
 
 	/**
 	 * @return the tempgroupName
 	 */
 	public String getTempgroupName()
 	{
 		return tempgroupName;
 	}
 
 	/**
 	 * @param tempgroupName the tempgroupName to set
 	 */
 	public void setTempgroupName(String tempgroupName)
 	{
 		this.tempgroupName = tempgroupName;
 	}
 
 	/**
 	 * @return the tempgroupNameText
 	 */
 	public String getTempgroupNameText()
 	{
 		return tempgroupNameText;
 	}
 
 	/**
 	 * @param tempgroupNameText the tempgroupNameText to set
 	 */
 	public void setTempgroupNameText(String tempgroupNameText)
 	{
 		this.tempgroupNameText = tempgroupNameText;
 	}
 
 	public void setAllValues(AbstractDomainObject arg0)
 	{
 		// TODO Auto-generated method stub
 
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
