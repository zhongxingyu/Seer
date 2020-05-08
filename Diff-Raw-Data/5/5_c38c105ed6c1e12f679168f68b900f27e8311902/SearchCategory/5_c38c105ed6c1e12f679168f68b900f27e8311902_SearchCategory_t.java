 package edu.wustl.cab2b.admin.searchdata.action;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.admin.action.BaseAction;
 import edu.wustl.cab2b.client.metadatasearch.MetadataSearch;
 import edu.wustl.cab2b.common.beans.MatchedClass;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.cab2b.server.cache.EntityCache;
 import edu.wustl.common.beans.NameValueBean;
 
 /**
  * @author atul_jawale
  * @author chetan_patil
  */
 public class SearchCategory extends BaseAction {
 	private static final long serialVersionUID = 7660830850643731270L;
 
 	/**
 	 * String to store the text field value.
 	 */
 	private String textField = null;
 
 	/**
 	 * String to store the classChecked checkbox's value.
 	 */
 	private String classChecked = null;
 
 	/**
 	 * String to store the attributeChecked checkbox's value.
 	 */
 	private String attributeChecked = null;
 
 	/**
 	 * String to store the permissibleValuesChecked checkbox's value.
 	 */
 	private String permissibleValuesChecked = null;
 
 	/**
 	 * String to store the the IncludeDescription checkbox's value.
 	 */
 	private String includeDescriptionChecked = null;
 
 	/**
 	 * String to store the radio button selected value.
 	 */
 	private String selected = null;
 
 	/**
 	 * String to store the radio button selected value.
 	 */
 	private String options = null;
 
 	/**
 	 * String to store the entityName value.
 	 */
 	private String entityName = null;
 
 	/**
 	 * String to store the string used To Create Query Object.
 	 */
 	private String stringToCreateQueryObject = null;
 	
 	/**
 	 * String to store the searchButton value.
 	 */
 	private String searchButton = null;
 
 	/**
 	 * String to store the nextOperation value.
 	 */
 	private String nextOperation = null;
 
 	/**
 	 * String to store the text field value.
 	 */
 	private String nodeId = null;
 
 	/**
 	 * String to store the text field value.
 	 */
 	private String currentPage = null;
 
 	private String booleanAttribute = null;
 
 	private String[] selectedColumnNames;
 
 	private String[] columnNames;
 
 	private List<NameValueBean> selectedColumnNameValueBeanList;
 
 	/**
 	 * String to store currentSelectedObject
 	 */
 	private String currentSelectedObject = null;
 
 	/**
 	 * String to store currentSelectedObject
 	 */
 	private String currentSelectedNodeInTree = null;
 
 	/**
 	 * @return the currentPage
 	 */
 	public String getCurrentPage() {
 		return currentPage;
 	}
 
 	/**
 	 * @param currentPage
 	 *            the currentPage to set
 	 */
 	public void setCurrentPage(String currentPage) {
 		this.currentPage = currentPage;
 	}
 
 	/**
 	 * @return the nextOperation
 	 */
 	public String getNextOperation() {
 		return nextOperation;
 	}
 
 	/**
 	 * @param nextOperation
 	 *            the nextOperation to set
 	 */
 	public void setNextOperation(String nextOperation) {
 		this.nextOperation = nextOperation;
 	}
 
 	/**
 	 * @return the searchButton
 	 */
 	public String getSearchButton() {
 		return searchButton;
 	}
 
 	/**
 	 * @param searchButton
 	 *            the searchButton to set
 	 */
 	public void setSearchButton(String searchButton) {
 		this.searchButton = searchButton;
 	}
 	/**
 	 * @return the stringToCreateQueryObject
 	 */
 	public String getStringToCreateQueryObject() {
 		return stringToCreateQueryObject;
 	}
 
 	/**
 	 * @param stringToCreateQueryObject the stringToCreateQueryObject to set
 	 */
 	public void setStringToCreateQueryObject(String stringToCreateQueryObject) {
 		this.stringToCreateQueryObject = stringToCreateQueryObject;
 	}
 
 	/**
 	 * @return the selected
 	 */
 	public String getSelected() {
 		return selected;
 	}
 
 	/**
 	 * @param selected the selected to set
 	 */
 	public void setSelected(String selected) {
 		this.selected = selected;
 	}
 
 	/**
 	 * @return the textField
 	 */
 	public String getTextField() {
 		return textField;
 	}
 
 	/**
 	 * @param textField the textField to set
 	 */
 	public void setTextField(String textField) {
 		this.textField = textField;
 	}
 
 	/**
 	 * @return the attributeChecked
 	 */
 	public String getAttributeChecked() {
 		return attributeChecked;
 	}
 
 	/**
 	 * @param attributeChecked the attributeChecked to set
 	 */
 	public void setAttributeChecked(String attributeChecked) {
 		this.attributeChecked = attributeChecked;
 	}
 
 	/**
 	 * @return the classChecked
 	 */
 	public String getClassChecked() {
 		return classChecked;
 	}
 
 	/**
 	 * @param classChecked the classChecked to set
 	 */
 	public void setClassChecked(String classChecked) {
 		this.classChecked = classChecked;
 	}
 
 	/**
 	 * @return the permissibleValuesChecked
 	 */
 	public String getPermissibleValuesChecked() {
 		return permissibleValuesChecked;
 	}
 
 	/**
 	 * @param permissibleValuesChecked the permissibleValuesChecked to set
 	 */
 	public void setPermissibleValuesChecked(String permissibleValuesChecked) {
 		this.permissibleValuesChecked = permissibleValuesChecked;
 	}
 
 	/**
 	 * @return the entityName
 	 */
 	public String getEntityName() {
 		return entityName;
 	}
 
 	/**
 	 * @param entityName the entityName to set
 	 */
 	public void setEntityName(String entityName) {
 		this.entityName = entityName;
 	}
 
 	/**
 	 * @return the nodeId
 	 */
 	public String getNodeId() {
 		return nodeId;
 	}
 
 	/**
 	 * @param nodeId the nodeId to set
 	 */
 	public void setNodeId(String nodeId) {
 		this.nodeId = nodeId;
 	}
 
 	/**
 	 * @return the booleanAttribute
 	 */
 	public String getBooleanAttribute() {
 		return booleanAttribute;
 	}
 
 	/**
 	 * @param booleanAttribute the booleanAttribute to set
 	 */
 	public void setBooleanAttribute(String booleanAttribute) {
 		this.booleanAttribute = booleanAttribute;
 	}
 
 	/**
 	 * @return Returns includeDescriptionChecked
 	 */
 	public String getIncludeDescriptionChecked() {
 		return includeDescriptionChecked;
 	}
 
 	/**
 	 * @param includeDescriptionChecked Sets includeDescriptionChecked
 	 */
 	public void setIncludeDescriptionChecked(String includeDescriptionChecked) {
 		this.includeDescriptionChecked = includeDescriptionChecked;
 	}
 
 	/**
 	 * @return the columnNames
 	 */
 	public String[] getColumnNames() {
 		return columnNames;
 	}
 
 	/**
 	 * @param columnNames the columnNames to set
 	 */
 	public void setColumnNames(String[] columnNames) {
 		this.columnNames = columnNames;
 	}
 
 	/**
 	 * @return the selectedColumnNames
 	 */
 	public String[] getSelectedColumnNames() {
 		return selectedColumnNames;
 	}
 
 	/**
 	 * @param selectedColumnNames the selectedColumnNames to set
 	 */
 	public void setSelectedColumnNames(String[] selectedColumnNames) {
 		this.selectedColumnNames = selectedColumnNames;
 	}
 
 	/**
 	 * @return the currentSelectedObject
 	 */
 	public String getCurrentSelectedObject() {
 		return currentSelectedObject;
 	}
 
 	/**
 	 * @param currentSelectedObject the currentSelectedObject to set
 	 */
 	public void setCurrentSelectedObject(String currentSelectedObject) {
 		this.currentSelectedObject = currentSelectedObject;
 	}
 
 	/**
 	 * @return the currentSelectedNodeInTree
 	 */
 	public String getCurrentSelectedNodeInTree() {
 		return currentSelectedNodeInTree;
 	}
 
 	/**
 	 * @param currentSelectedNodeInTree the currentSelectedNodeInTree to set
 	 */
 	public void setCurrentSelectedNodeInTree(String currentSelectedNodeInTree) {
 		this.currentSelectedNodeInTree = currentSelectedNodeInTree;
 	}
 
 	/**
 	 * @return the selectedColumnNameValueBeanList
 	 */
 	public List<NameValueBean> getSelectedColumnNameValueBeanList() {
 		return selectedColumnNameValueBeanList;
 	}
 
 	/**
 	 * @param selectedColumnNameValueBeanList the selectedColumnNameValueBeanList to set
 	 */
 	public void setSelectedColumnNameValueBeanList(List<NameValueBean> selectedColumnNameValueBeanList) {
 		this.selectedColumnNameValueBeanList = selectedColumnNameValueBeanList;
 	}
 
 	/**
 	 * @return the options
 	 */
 	public String getOptions() {
 		return options;
 	}
 
 	/**
 	 * @param options the options to set
 	 */
 	public void setOptions(String options) {
 		this.options = options;
 	}
 
 	/**
 	 * @return RESULT
 	 * @throws IOException 
 	 * @throws NumberFormatException
 	 * @throws CheckedException
 	 */
 	public String execute() throws IOException, NumberFormatException, CheckedException {
 		String textfieldValue = getTextField();
 		String entityId = request.getParameter("entityId");
 
 		EntityCache cache = EntityCache.getInstance();
 		if (entityId != null) {
 			StringBuffer result = new StringBuffer();
 
 			EntityInterface entity = cache.getEntityById(Long.parseLong(entityId));
 			Collection<AttributeInterface> attributeCollection = entity.getAttributeCollection();
 			for (AttributeInterface attribute : attributeCollection) {
 				result.append(";" + attribute.getName() + ":" + attribute.getId());
 			}
 			response.setContentType("text/html");
 			response.getWriter().write(result.toString());
 			return null;
 		}
 
 		if (currentPage != null && currentPage.equalsIgnoreCase("prevToAddLimits")) {
 			textfieldValue = "";
 		}
 
 		if (textfieldValue != null && !textfieldValue.equals("")) {
 			int[] searchTarget = prepareSearchTarget();
 			int basedOn = prepareBaseOn(getSelected());
 
 			String[] searchString = prepareSearchString(textfieldValue);
 			StringBuffer entitiesString = new StringBuffer();
 
 			MetadataSearch advancedSearch = new MetadataSearch(cache);
 			MatchedClass matchedClass = advancedSearch.search(searchTarget, searchString, basedOn);
 			Set<EntityInterface> entityCollection = matchedClass.getEntityCollection();
 			List<EntityInterface> resultList = new ArrayList<EntityInterface>(entityCollection);
 			for (EntityInterface entity : resultList) {
 				if (!Utility.isCategory(entity) && !Utility.isMultiModelCategory(entity)) {
 					String entityName = Utility.getDisplayName(entity);
 					entityId = entity.getId().toString();
 					String description = entity.getDescription();
 					entitiesString.append("~!@#$~").append(entityName).append("~!@!~")
 					.append(entityId).append("~!@!~").append(description);
 				}
 			}
 
 			if (entitiesString.length() == 0) {
 				entitiesString.append("No Result Found");
 			}
 			response.setContentType("text/html");
 			response.getWriter().write(entitiesString.toString());
 			return null;
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * Prepares a String to be sent to AdvancedSearch logic.
 	 * @param textfieldValue String
 	 * @return String[] array of strings , taken from user.
 	 */
 	private String[] prepareSearchString(String textfieldValue) {
 		int counter = 0;
 		StringTokenizer tokenizer = new StringTokenizer(textfieldValue);
 		String[] searchString = new String[tokenizer.countTokens()];
 		while (tokenizer.hasMoreTokens()) {
			
			searchString[counter] = "\\Q"+tokenizer.nextToken().replaceAll("\\\\E","")+"\\E";  //JJJ guards against reserved regEx chars like '['
 			counter++;

 		}
 		return searchString;
 	}
 
 	/**
 	 * Returns a integer constant for radio option selected by user which represents Based on.
 	 * @param basedOnStr String
 	 * @return integer constant for basedOn
 	 */
 	private int prepareBaseOn(String basedOnStr) {
 		int basedOn = edu.wustl.cab2b.common.util.Constants.BASED_ON_TEXT;
 		if (basedOnStr != null) {
 			if (basedOnStr.equalsIgnoreCase("conceptCode_radioButton")) {
 				basedOn = edu.wustl.cab2b.common.util.Constants.BASED_ON_CONCEPT_CODE;
 			}
 		}
 		return basedOn;
 	}
 
 	/**
 	 * Prepares the array of search targets from the check box values selected by user.
 	 * @return Integer array of selections made by user.
 	 */
 	private int[] prepareSearchTarget() {
 		boolean searchClass = isChecked(getClassChecked());
 		boolean searchAttribute = isChecked(getAttributeChecked());
 		boolean searchPv = isChecked(getPermissibleValuesChecked());
 		boolean includeDesc = isChecked(getIncludeDescriptionChecked());
 		return Utility.prepareSearchTarget(searchClass, searchAttribute, searchPv, includeDesc);
 	}
 
 	private boolean isChecked(String str) {
 		return str != null && (str.equalsIgnoreCase("on") || str.equalsIgnoreCase("true"));
 	}
 }
