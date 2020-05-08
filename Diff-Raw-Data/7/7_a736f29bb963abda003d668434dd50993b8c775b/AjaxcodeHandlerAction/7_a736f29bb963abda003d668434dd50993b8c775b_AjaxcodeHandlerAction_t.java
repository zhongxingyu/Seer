 /*
  * Created on Dec 19, 2006
  * @author
  *
  */
 
 package edu.common.dynamicextensions.ui.webui.action;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractEntityInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AbstractContainmentControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.entitymanager.DynamicExtensionsQueryBuilderConstantsInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.GroupProcessor;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.util.SemanticPropertyBuilderUtil;
 import edu.common.dynamicextensions.ui.webui.util.CacheManager;
 import edu.common.dynamicextensions.ui.webui.util.UserInterfaceiUtility;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManager;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManagerConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.DEConstants;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * @author preeti_munot
  *
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class AjaxcodeHandlerAction extends BaseDynamicExtensionsAction
 {
 
 	/**
 	 * @param mapping ActionMapping mapping
 	 * @param form ActionForm form
 	 * @param  request HttpServletRequest request
 	 * @param response HttpServletResponse response
 	 * @return ActionForward forward to next action
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws DynamicExtensionsApplicationException
 	{
 		String returnXML = null;
 		try
 		{
 			String operation = request.getParameter("ajaxOperation");
 			if (operation != null)
 			{
 				if (operation.trim().equals("selectFormNameFromTree"))
 				{
 					String selectedFormName = request.getParameter("selectedFormName");
 					String rectifySelectName=rectifySelectedName(selectedFormName);
 					if (rectifySelectName != null)
 					{
 						returnXML = getSelectedFormDetails(request, rectifySelectName);
 					}
 				}
 				else if (operation.trim().equals("selectFormNameFromAssociationTree"))
 				{
 					String selectedFormId = request.getParameter("selectedFormId");
 					if (selectedFormId != null)
 					{
 						returnXML = getSelectedFormDetailsById(selectedFormId);
 					}
 				}
 				else if (operation.trim().equals("selectGroup"))
 				{
 					String selectedGroupName = request.getParameter("selectedGroupName");
 					if (selectedGroupName != null)
 					{
						returnXML = getSelectedGroupDetails(selectedGroupName);
 					}
 				}
 				else if (operation.trim().equals("deleteRowsForContainment"))
 				{
 					String deletedRowIds = request.getParameter("deletedRowIds");
 					String containerId = request.getParameter("containerId");
 					returnXML = deleteRowsForContainment(request, deletedRowIds, containerId);
 				}
 				else if (operation.trim().equals("updateControlsSequence"))
 				{
 					String gridControlsIds = request.getParameter("gridControlIds");
 					returnXML = updateControlsSequence(request, gridControlsIds);
 				}
 				else if (operation.trim().equals("changeGroup"))
 				{
 					returnXML = changeGroup(request);
 				}
 				else if (operation.trim().equals("changeForm"))
 				{
 					returnXML = changeForm(request);
 				}
 
 			}
 			sendResponse(returnXML, response);
 			return null;
 		}
 		catch (Exception e)
 		{
 			String actionForwardString = catchException(e, request);
 			if ((actionForwardString == null) || (actionForwardString.equals("")))
 			{
 				return mapping.getInputForward();
 			}
 			return mapping.findForward(actionForwardString);
 		}
 	}
 
 	/**
 	 *
 	 * @param selectedName
 	 * @return
 	 */
 	private String rectifySelectedName(String selectedName)
 	{
 		String[] subStrings = selectedName.split("_");
 		StringBuffer rectifiedName = new StringBuffer();
 		for (int i = 1; i < subStrings.length; i++)
 		{
 			rectifiedName.append(subStrings[i]);
 		}
 
 		if (rectifiedName.length() == 0)
 		{
 			rectifiedName.append(subStrings[0]);
 		}
 		return rectifiedName.toString();
 	}
 
 	/**
 	 * @param selectedFormId
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private String getSelectedFormDetailsById(String selectedFormId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerInterface containerForSelectedForm = null;
 		String formName = "", formDescription = "", formConceptCode = "";
 		boolean isAbstract = false;
 		if (selectedFormId != null)
 		{
 			containerForSelectedForm = DynamicExtensionsUtility
 					.getContainerByIdentifier(selectedFormId);
 			if (containerForSelectedForm != null)
 			{
 				formName = containerForSelectedForm.getCaption();
 				AbstractEntityInterface entity = (AbstractEntityInterface) containerForSelectedForm
 						.getAbstractEntity();
 				if (entity != null)
 				{
 					formDescription = entity.getDescription();
 					if (formDescription == null)
 					{
 						formDescription = "";
 					}
 					formConceptCode = SemanticPropertyBuilderUtil.getConceptCodeString(entity);
 					if (formConceptCode == null)
 					{
 						formConceptCode = "";
 					}
 					isAbstract = entity.isAbstract();
 				}
 			}
 		}
 		String formDetailsXML = createFormDetailsXML(formName, formDescription, formConceptCode,
 				DEConstants.ADD_SUB_FORM_OPR, isAbstract);
 		if (formDetailsXML == null)
 		{
 			formDetailsXML = "";
 		}
 		return formDetailsXML;
 	}
 
 	/**
 	 * @param request
 	 * @param controlsSeqNumbers
 	 * @return
 	 */
 	private String updateControlsSequence(HttpServletRequest request, String controlsSeqNumbers)
 	{
 		ContainerInterface container = WebUIManager.getCurrentContainer(request);
 		if (container != null)
 		{
 			Collection<ControlInterface> oldControlsCollection = container.getControlCollection();
 
 			if (oldControlsCollection != null)
 			{
 				Integer[] sequenceNumbers = DynamicExtensionsUtility.convertToIntegerArray(
 						controlsSeqNumbers, ProcessorConstants.CONTROLS_SEQUENCE_NUMBER_SEPARATOR);
 				ControlInterface[] oldControls = oldControlsCollection
 						.toArray(new ControlInterface[oldControlsCollection.size()]);
 
 				// Adding id attribute to attribute collection.
 				AttributeInterface idAttribute = null;
 				Collection<AttributeInterface> attributeCollection = ((EntityInterface) container
 						.getAbstractEntity()).getAllAttributes();
 				for (AttributeInterface attributeIterator : attributeCollection)
 				{
 					if (attributeIterator.getColumnProperties().getName() != null
 							&& attributeIterator.getColumnProperties().getName().equals(
 									DynamicExtensionsQueryBuilderConstantsInterface.IDENTIFIER))
 					{
 						idAttribute = attributeIterator;
 						break;
 					}
 				}
 
 				// Remove old controls from collection.
 				container.removeAllControls();
 				((AbstractEntityInterface) container.getAbstractEntity()).removeAllAttributes();
 
 				ControlInterface control = null;
 				if (sequenceNumbers != null)
 				{
 					for (int i = 0; i < sequenceNumbers.length; i++)
 					{
 						control = DynamicExtensionsUtility.getControlBySequenceNumber(oldControls,
 								sequenceNumbers[i].intValue());
 						if (control != null)
 						{
 							container.addControl(control);
 							((EntityInterface) container.getAbstractEntity())
 									.addAbstractAttribute((AbstractAttributeInterface) control
 											.getBaseAbstractAttribute());
 						}
 					}
 				}
 
 				if (idAttribute != null)
 				{
 					((EntityInterface) container.getAbstractEntity())
 							.addAbstractAttribute(idAttribute);
 				}
 				//Added by Rajesh for removing deleted associations and it's path from path tables.
 				List<Long> deletedIdList = (List<Long>) CacheManager.getObjectFromCache(request,
 						WebUIManagerConstants.DELETED_ASSOCIATION_IDS);
 				List<Long> listOfIds = DynamicExtensionsUtility.getDeletedAssociationIds(
 						oldControls, sequenceNumbers);
 				if (deletedIdList == null)
 				{
 					deletedIdList = listOfIds;
 				}
 				else
 				{
 					deletedIdList.addAll(listOfIds);
 				}
 				CacheManager.addObjectToCache(request,
 						WebUIManagerConstants.DELETED_ASSOCIATION_IDS, deletedIdList);
 				//				System.out.println("deletedIdList : " + deletedIdList.size());
 			}
 		}
 
 		Collection<ControlInterface> controlCollection = container.getControlCollection();
 		for (ControlInterface control : controlCollection)
 		{
 			Logger.out.info("[" + control.getSequenceNumber() + "] = [" + control.getCaption()
 					+ "]");
 		}
 
 		return "";
 	}
 
 	/**
 	 * @param request
 	 * @param deletedRowIds
 	 * @param childContainerId
 	 * @return
 	 */
 	private String deleteRowsForContainment(HttpServletRequest request, String deletedRowIds,
 			String childContainerId)
 	{
 		Stack containerStack = (Stack) CacheManager.getObjectFromCache(request,
 				DEConstants.CONTAINER_STACK);
 		Stack valueMapStack = (Stack) CacheManager.getObjectFromCache(request,
 				DEConstants.VALUE_MAP_STACK);
 
 		Map<AbstractAttributeInterface, Object> valueMap = (Map<AbstractAttributeInterface, Object>) valueMapStack
 				.peek();
 		ContainerInterface containerInterface = (ContainerInterface) containerStack.peek();
 
 		AbstractContainmentControlInterface associationControl = UserInterfaceiUtility
 				.getAssociationControl(containerInterface, childContainerId);
 
 		AssociationMetadataInterface association = (AssociationMetadataInterface) associationControl
 				.getBaseAbstractAttribute();
 
 		List<Map<AbstractAttributeInterface, Object>> associationValueMapList = (List<Map<AbstractAttributeInterface, Object>>) valueMap
 				.get(association);
 
 		String[] deletedRows = deletedRowIds.split(",");
 
 		for (int i = 0; i < deletedRows.length; i++)
 		{
 			int removeIndex = Integer.valueOf(deletedRows[i]) - 1;
 
 			if (associationValueMapList.size() > removeIndex)
 			{
 				associationValueMapList.remove(removeIndex);
 			}
 
 		}
 
 		return "";
 	}
 
 	/**
 	 * @param selectedGroupName
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
	private String getSelectedGroupDetails(String selectedGroupName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		EntityGroupInterface entityGroup = null;
 		if ((selectedGroupName != null) && (!selectedGroupName.trim().equals("")))
 		{
 			GroupProcessor groupProcessor = GroupProcessor.getInstance();
 			entityGroup = groupProcessor.getEntityGroupByIdentifier(selectedGroupName);
 		}
 		return getGroupDetailsXML(entityGroup);
 	}
 
 	/**
 	 * @param entityGroup
 	 * @return
 	 */
 	private String getGroupDetailsXML(EntityGroupInterface entityGroup)
 	{
 		String groupDescription = null;
 		if (entityGroup != null)
 		{
 			groupDescription = entityGroup.getDescription();
 		}
 		if (groupDescription == null)
 		{
 			groupDescription = "";
 		}
 		String groupDetailsXML = createGroupDetailsXML(groupDescription);
 		if (groupDetailsXML == null)
 		{
 			groupDetailsXML = "";
 		}
 		return groupDetailsXML;
 	}
 
 	/**
 	 * @param groupDescription
 	 * @return
 	 */
 	private String createGroupDetailsXML(String groupDescription)
 	{
 		StringBuffer responseXML = new StringBuffer();
 		responseXML.append("<group><group-description>");
 		responseXML.append(groupDescription);
 		responseXML.append("</group-description></group>");
 		return responseXML.toString();
 
 	}
 
 	/**
 	 * @param request
 	 * @param selectedFormName
 	 */
 	private String getSelectedFormDetails(HttpServletRequest request, String selectedFormName)
 	{
 		ContainerInterface containerForSelectedForm = null;
 		if ((request != null) && (selectedFormName != null))
 		{
 			containerForSelectedForm = (ContainerInterface) CacheManager.getObjectFromCache(
 					request, selectedFormName);
 			if (containerForSelectedForm != null)
 			{
 				updateCacheRefernces(request, selectedFormName, containerForSelectedForm);
 			}
 		}
 		String formDetailsXML = getFormDetailsXML(request, selectedFormName,
 				containerForSelectedForm);
 		return formDetailsXML;
 	}
 
 	/**
 	 * @param containerForSelectedForm
 	 * @return
 	 */
 	private String getFormDetailsXML(HttpServletRequest request, String selectedFormName,
 			ContainerInterface containerForSelectedForm)
 	{
 		String formName = selectedFormName;
 		String formDescription = "";
 		String formConceptCode = "";
 		String operationMode = DEConstants.ADD_SUB_FORM_OPR;
 		boolean isAbstract = false;
 		if (containerForSelectedForm != null)
 		{
 			formName = containerForSelectedForm.getCaption();
 			AbstractEntityInterface entity = (AbstractEntityInterface) containerForSelectedForm
 					.getAbstractEntity();
 			if (entity != null)
 			{
 				formDescription = entity.getDescription();
 				formConceptCode = SemanticPropertyBuilderUtil.getConceptCodeString(entity);
 				isAbstract = entity.isAbstract();
 			}
 			operationMode = DEConstants.EDIT_FORM;
 		}
 		//If selected form container is null and cache container interface is also null,
 		// it means that there is no container in cache and a new form is to be created.
 
 		if (containerForSelectedForm == null)
 		{
 			ContainerInterface mainContainerInterface = (ContainerInterface) CacheManager
 					.getObjectFromCache(request, DEConstants.CONTAINER_INTERFACE);
 			if (mainContainerInterface == null)
 			{
 				operationMode = DEConstants.ADD_NEW_FORM;
 			}
 		}
 		String formDetailsXML = createFormDetailsXML(formName, formDescription, formConceptCode,
 				operationMode, isAbstract);
 		if (formDetailsXML == null)
 		{
 			formDetailsXML = "";
 		}
 		return formDetailsXML;
 	}
 
 	/**
 	 * @param formName
 	 * @param formDescription
 	 * @param formConceptCode
 	 * @return
 	 */
 	private String createFormDetailsXML(String formName, String formDescription,
 			String formConceptCode, String operationMode, boolean isAbstract)
 	{
 		StringBuffer responseXML = new StringBuffer();
 		responseXML.append("<form><form-name>");
 		responseXML.append(formName);
 		responseXML.append("</form-name><form-description>");
 		responseXML.append(formDescription);
 		responseXML.append("</form-description><form-conceptcode>");
 		responseXML.append(formConceptCode);
 		responseXML.append("</form-conceptcode><operationMode>");
 		responseXML.append(operationMode);
 		responseXML.append("</operationMode><isAbstract>");
 		responseXML.append(isAbstract);
 		responseXML.append("</isAbstract></form>");
 		return responseXML.toString();
 	}
 
 	/**
 	 * @param selectedFormName
 	 */
 	private void updateCacheRefernces(HttpServletRequest request, String selectedFormName,
 			ContainerInterface containerForSelectedForm)
 	{
 		CacheManager.addObjectToCache(request, DEConstants.CURRENT_CONTAINER_NAME, selectedFormName);
 		CacheManager.addObjectToCache(request, selectedFormName, containerForSelectedForm);
 	}
 
 	/**
 	 * @throws IOException
 	 *
 	 */
 	private void sendResponse(String responseXML, HttpServletResponse response) throws IOException
 	{
 		PrintWriter out = response.getWriter();
 		response.setContentType("text/xml");
 		out.write(responseXML);
 	}
 
 	/**
 	 * @param request
 	 * @return response xml string
 	 * @throws IOException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private String changeGroup(HttpServletRequest request)
 			throws IOException, DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		List<NameValueBean> formNames = getFormNamesForGroup(request.getParameter("grpName"));
 		DynamicExtensionsUtility.sortNameValueBeanListByName(formNames);
 		String xmlParentNode = "forms";
 		String xmlIdNode = "form-id";
 		String xmlNameNode = "form-name";
 		return getResponseXMLString(xmlParentNode, xmlIdNode, xmlNameNode, formNames);
 	}
 
 	/**
 	 * @param groupId group identifier
 	 * @return list of name value beans of groups and forms in those groups.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private List<NameValueBean> getFormNamesForGroup(String groupId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ArrayList<NameValueBean> formNames = new ArrayList<NameValueBean>();
 
 		if (groupId != null)
 		{
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			Long groupIdentifier = null;
 			try
 			{
 				groupIdentifier = Long.parseLong(groupId);
 
 				Collection<ContainerInterface> containersCollection = entityManager
 						.getAllContainersByEntityGroupId(groupIdentifier);
 				if (containersCollection != null)
 				{
 					NameValueBean formName = null;
 					for (ContainerInterface container : containersCollection)
 					{
 						if (container != null)
 						{
 							formName = new NameValueBean(container.getCaption(), container.getId());
 							formNames.add(formName);
 						}
 					}
 				}
 			}
 			catch (NumberFormatException e)
 			{
 				Logger.out.error("Group Id is null. Please provide correct Group Id");
 			}
 		}
 		return formNames;
 	}
 
 	/**
 	 * @param xmlParentNode
 	 * @param xmlNameNode
 	 * @param listValues
 	 * @return
 	 */
 	private String getResponseXMLString(String xmlParentNode, String xmlIdNode, String xmlNameNode,
 			List<NameValueBean> listValues)
 	{
 		StringBuffer responseXML = new StringBuffer();
 		NameValueBean bean = null;
 		if ((xmlParentNode != null) && (xmlNameNode != null) && (listValues != null))
 		{
 			responseXML.append("<node>");
 			int noOfValues = listValues.size();
 			for (int i = 0; i < noOfValues; i++)
 			{
 				bean = listValues.get(i);
 				if (bean != null)
 				{
 					responseXML.append("<");
 					responseXML.append(xmlParentNode);
 					responseXML.append("><");
 					responseXML.append(xmlIdNode);
 					responseXML.append(">");
 					responseXML.append(bean.getValue());
 					responseXML.append("</");
 					responseXML.append(xmlIdNode);
 					responseXML.append("><");
 					responseXML.append(xmlNameNode);
 					responseXML.append(">");
 					responseXML.append(bean.getName());
 					responseXML.append("</");
 					responseXML.append(xmlNameNode);
 					responseXML.append("></");
 					responseXML.append(xmlParentNode);
 					responseXML.append(">");
 				}
 			}
 			responseXML.append("</node>");
 
 		}
 		return responseXML.toString();
 	}
 
 	/**
 	 * @param request
 	 * @param actionForm
 	 * @throws IOException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private String changeForm(HttpServletRequest request)
 			throws IOException, DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		List<NameValueBean> formAttributes = getAttributesForForm(request.getParameter("frmName"));
 		DynamicExtensionsUtility.sortNameValueBeanListByName(formAttributes);
 		String xmlParentNode = "formAttributes";
 		String xmlNodeId = "form-attribute-id";
 		String xmlNodeName = "form-attribute-name";
 		String responseXML = getResponseXMLString(xmlParentNode, xmlNodeId, xmlNodeName,
 				formAttributes);
 		return responseXML;
 	}
 
 	/**
 	 * @param parameter
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private List<NameValueBean> getAttributesForForm(String formId)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ArrayList<NameValueBean> formAttributesList = new ArrayList<NameValueBean>();
 		if (formId != null)
 		{
 			Logger.out.debug("Fetching attributes for [" + formId + "]");
 			ContainerInterface container = DynamicExtensionsUtility
 					.getContainerByIdentifier(formId);
 			if (container != null)
 			{
 				Collection<ControlInterface> controlCollection = container.getAllControls();
 				if (controlCollection != null)
 				{
 					NameValueBean controlName = null;
 					AbstractAttributeInterface abstractAttribute = null;
 					AttributeInterface attribute = null;
 					for (ControlInterface control : controlCollection)
 					{
 						if (control != null)
 						{
 							//if control contains Attribute interface object then only show on UI.
 							//If control contains association objects do not show in attribute list
 							abstractAttribute = (AbstractAttributeInterface) control
 									.getBaseAbstractAttribute();
 							if (abstractAttribute != null
 									&& (abstractAttribute instanceof AttributeInterface))
 							{
 								attribute = (AttributeInterface) abstractAttribute;
 								if (!(attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation))
 								{
 									controlName = new NameValueBean(control.getCaption(), control
 											.getId());
 									formAttributesList.add(controlName);
 								}
 							}
 						}
 					}
 				}
 			}
 
 		}
 
 		return formAttributesList;
 	}
 }
