 
 package edu.common.dynamicextensions.ui.webui.actionform;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManagerConstants;
 import edu.wustl.common.actionForm.AbstractActionForm;
 import edu.wustl.common.domain.AbstractDomainObject;
 
 /**
  * @author sujay_narkar
  *
  */
 public class DataEntryForm extends AbstractActionForm
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7828307676065035418L;
 
 	/**
 	 * 
 	 */
 	protected String entitySaved;
 	/**
 	 * 
 	 */
 	protected String showFormPreview = "false";
 
 	/**
 	 * 
 	 */
 	protected ContainerInterface containerInterface;
 	/**
 	 * 
 	 */
 	protected String recordIdentifier;
 	/**
 	 * 
 	 */
 	protected List<String> errorList;
 	/**
 	 * 
 	 */
 	protected String mode = WebUIManagerConstants.EDIT_MODE;
 	/**
 	 * 
 	 */
 	protected Map<String, Object> valueMap = new HashMap<String, Object>();
 	/**
 	 * 
 	 */
 	protected String childContainerId;
 	/**
 	 * 
 	 */
 	protected String childRowId;
 	/**
 	 * 
 	 */
 	protected String dataEntryOperation = "";
 	/*
 	 * 
 	 */
 	protected boolean isTopLevelEntity = true;
 	
 	protected String previewBack;
 	
	protected String containerId;
	
	/**
	 * @return the containerId
	 */
	public String getContainerId() {
		return containerId;
	}

	/**
	 * @param containerId the containerId to set
	 */
	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

 	/**
 	 * 
 	 * @param key
 	 * @param value
 	 */
 	public void setValue(String key, Object value)
 	{
 		valueMap.put(key, value);
 	}
 
 	/**
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public Object getValue(String key)
 	{
 		return valueMap.get(key);
 	}
 
 	/**
 	 * @return Returns the valueMap.
 	 */
 	public Map<String, Object> getValueMap()
 	{
 		return valueMap;
 	}
 
 	/**
 	 * @param valueMap The valueMap to set.
 	 */
 	public void setValueMap(Map<String, Object> valueMap)
 	{
 		this.valueMap = valueMap;
 	}
 
 	/**
 	 * @return int formId
 	 */
 	public int getFormId()
 	{
 		return 0;
 	}
 
 	/**
 	 * @param arg0 abstractDomainObject
 	 */
 	public void setAllValues(AbstractDomainObject arg0)
 	{
 	}
 
 	/**
 	 * 
 	 */
 	protected void reset()
 	{
 	}
 
 	/**
 	 * @return Returns the container.
 	 */
 	public ContainerInterface getContainerInterface()
 	{
 		return containerInterface;
 	}
 
 	/**
 	 * @param containerInterface The container to set.
 	 */
 	public void setContainerInterface(ContainerInterface containerInterface)
 	{
 		this.containerInterface = containerInterface;
 	}
 
 	/**
 	 * 
 	 * @return entitySaved
 	 */
 	public String getEntitySaved()
 	{
 		return entitySaved;
 	}
 
 	/**
 	 * 
 	 * @param entitySaved entitySaved
 	 */
 	public void setEntitySaved(String entitySaved)
 	{
 		this.entitySaved = entitySaved;
 	}
 
 	/**
 	 * 
 	 * @return String showFormPreview 
 	 */
 	public String getShowFormPreview()
 	{
 		return showFormPreview;
 	}
 
 	/**
 	 * 
 	 * @param showFormPreview String showFormPreview
 	 */
 	public void setShowFormPreview(String showFormPreview)
 	{
 		this.showFormPreview = showFormPreview;
 	}
 
 	public String getRecordIdentifier()
 	{
 		return recordIdentifier;
 	}
 
 	public void setRecordIdentifier(String recordIdentifier)
 	{
 		this.recordIdentifier = recordIdentifier;
 	}
 
 	/**
 	 * @return the errorList
 	 */
 	public List<String> getErrorList()
 	{
 		return errorList;
 	}
 
 	/**
 	 * @param errorList the errorList to set
 	 */
 	public void setErrorList(List<String> errorList)
 	{
 		this.errorList = errorList;
 	}
 	
 	/**
 	 * @return Returns the mode.
 	 */
 	public String getMode()
 	{
 		return mode;
 	}
 	
 	/**
 	 * @param mode The mode to set.
 	 */
 	public void setMode(String mode)
 	{
 		this.mode = mode;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getChildContainerId()
 	{
 		return childContainerId;
 	}
 
 	/**
 	 * 
 	 * @param childContainerId
 	 */
 	public void setChildContainerId(String childContainerId)
 	{
 		this.childContainerId = childContainerId;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getChildRowId()
 	{
 		return childRowId;
 	}
 
 	/**
 	 * 
 	 * @param childRowId
 	 */
 	public void setChildRowId(String childRowId)
 	{
 		this.childRowId = childRowId;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getDataEntryOperation()
 	{
 		return dataEntryOperation;
 	}
 
 	/**
 	 * 
 	 * @param dataEntryOperation
 	 */
 	public void setDataEntryOperation(String dataEntryOperation)
 	{
 		this.dataEntryOperation = dataEntryOperation;
 	}
 
 	/**
 	 * @return Returns the isTopLevelEntity.
 	 */
 	public boolean getIsTopLevelEntity()
 	{
 		return isTopLevelEntity;
 	}
 	
 	/**
 	 * @param isTopLevelEntity The isTopLevelEntity to set.
 	 */
 	public void setIsTopLevelEntity(boolean isTopLevelEntity)
 	{
 		this.isTopLevelEntity = isTopLevelEntity;
 	}
 
 	
 	/**
 	 * @return the previewBack
 	 */
 	public String getPreviewBack()
 	{
 		return previewBack;
 	}
 
 	
 	/**
 	 * @param previewBack the previewBack to set
 	 */
 	public void setPreviewBack(String previewBack)
 	{
 		this.previewBack = previewBack;
 	}
 
 }
