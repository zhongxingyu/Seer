 
 package edu.common.dynamicextensions.ui.webui.action;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.domain.NumericAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AbstractEntityInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AbstractContainmentControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.LoadDataEntryFormProcessor;
 import edu.common.dynamicextensions.ui.webui.actionform.DataEntryForm;
 import edu.common.dynamicextensions.ui.webui.util.CacheManager;
 import edu.common.dynamicextensions.ui.webui.util.UserInterfaceiUtility;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManagerConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsCacheManager;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.wustl.common.actionForm.AbstractActionForm;
 
 /**
  * @author sujay_narkar, chetan_patil
  *
  */
 public class LoadDataEntryFormAction extends BaseDynamicExtensionsAction
 {
 
 	/* (non-Javadoc)
 	 * @see org.apache.struts.actions.DispatchAction#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException, NumberFormatException, SQLException
 	{
 		cacheCallBackURL(request);
 
 		ContainerInterface containerInterface = getConatinerInterface(request);
 
 		String recordId = request.getParameter("recordIdentifier");
 		if (recordId != null && !recordId.equals(""))
 		{
 			CacheManager.addObjectToCache(request, "rootRecordIdentifier", recordId);
 		}
 		else
 		{
 			recordId = (String) CacheManager.getObjectFromCache(request, "rootRecordIdentifier");
 			if (recordId == null)
 			{
 				recordId = "";
 			}
 		}
 
 		LoadDataEntryFormProcessor loadDataEntryFormProcessor = LoadDataEntryFormProcessor.getInstance();
 		Map<BaseAbstractAttributeInterface, Object> recordMap = loadDataEntryFormProcessor.getValueMapFromRecordId((AbstractEntityInterface) containerInterface.getAbstractEntity(),
 				recordId);
 
 		Stack<ContainerInterface> containerStack = (Stack<ContainerInterface>) CacheManager.getObjectFromCache(request, Constants.CONTAINER_STACK);
 		Stack<Map<BaseAbstractAttributeInterface, Object>> valueMapStack = (Stack<Map<BaseAbstractAttributeInterface, Object>>) CacheManager
 				.getObjectFromCache(request, Constants.VALUE_MAP_STACK);
 		if (containerStack == null)
 		{
 			containerStack = new Stack<ContainerInterface>();
 			CacheManager.addObjectToCache(request, Constants.CONTAINER_STACK, containerStack);
 			valueMapStack = new Stack<Map<BaseAbstractAttributeInterface, Object>>();
 			CacheManager.addObjectToCache(request, Constants.VALUE_MAP_STACK, valueMapStack);
 			UserInterfaceiUtility.addContainerInfo(containerStack, containerInterface, valueMapStack, recordMap);
 		}
 
 		DataEntryForm dataEntryForm = (DataEntryForm) form;
 		addPrecisionZeroes(recordMap);
 		updateStacks(request, dataEntryForm, containerInterface, recordMap, containerStack, valueMapStack);
 
 		if ((!containerStack.isEmpty()) && (!valueMapStack.isEmpty()))
 		{
 			String mode = request.getParameter(WebUIManagerConstants.MODE_PARAM_NAME);
 			if (mode == null || !mode.equals(""))
 			{
 				mode = dataEntryForm.getMode();
 			}
 
 			loadDataEntryFormProcessor.loadDataEntryForm((AbstractActionForm) form, containerStack.peek(), valueMapStack.peek(), mode, recordId);
 		}
 
 		updateTopLevelEntitiyInfo(containerStack, dataEntryForm);
 
 		if (dataEntryForm.getErrorList().isEmpty())
 		{
 			clearFormValues(dataEntryForm);
 		}
 		return mapping.findForward("Success");
 	}
 
 	/**
 	 * This method returns the Container Identifier form the givaen request.
 	 * @param request HttpServletRequest
 	 * @return the Container Identifier
 	 */
 	private String getContainerId(HttpServletRequest request)
 	{
 		String id = "";
 		id = request.getParameter("containerIdentifier");
 		if (id == null || id.equals(""))
 		{
 			id = (String) request.getAttribute("containerIdentifier");
 		}
 		return id;
 	}
 
 	/**
 	 * This method flushes the values of the DataEntryForm ActionForm. 
 	 * @param form DataEntryForm ActionForm
 	 */
 	private void clearFormValues(ActionForm form)
 	{
 		DataEntryForm dataEntryForm = (DataEntryForm) form;
 		dataEntryForm.setChildRowId("");
 		dataEntryForm.setChildContainerId("");
 	}
 
 	/**
 	 * 
 	 * @param request
 	 */
 	private void cacheCallBackURL(HttpServletRequest request)
 	{
 		String callBackURL = request.getParameter(WebUIManagerConstants.CALLBACK_URL_PARAM_NAME);
 		String userId = request.getParameter(WebUIManagerConstants.USER_ID);
 		if (callBackURL != null && !callBackURL.equals(""))
 		{
 			CacheManager.clearCache(request);
 			CacheManager.addObjectToCache(request, Constants.CALLBACK_URL, callBackURL);
 			CacheManager.addObjectToCache(request, WebUIManagerConstants.USER_ID, userId);
 		}
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private ContainerInterface getConatinerInterface(HttpServletRequest request) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ContainerInterface containerInterface = (ContainerInterface) CacheManager.getObjectFromCache(request, Constants.CONTAINER_INTERFACE);
 		String containerIdentifier = getContainerId(request);
 		if (containerIdentifier != null || containerInterface == null)
 		{
 			UserInterfaceiUtility.clearContainerStack(request);
 				
 			Long containerId = new Long(containerIdentifier);
 			DynamicExtensionsCacheManager deCacheManager;
 			HashMap containerMap = new HashMap();
 				
 			deCacheManager = DynamicExtensionsCacheManager.getInstance();
 			containerMap = (HashMap) deCacheManager.getObjectFromCache(Constants.LIST_OF_CONTAINER);
 			
 			if(containerMap!=null && containerMap.containsKey(containerId))
 			{
 					containerInterface = (ContainerInterface)containerMap.get(containerId);
 					containerInterface.getContainerValueMap().clear();
 					DynamicExtensionsUtility.cleanContainerControlsValue(containerInterface);					
 					
 			}
 			else
 			{
 				containerInterface = DynamicExtensionsUtility.getContainerByIdentifier(containerIdentifier);
 				
 			}			
 			
 			CacheManager.addObjectToCache(request, Constants.CONTAINER_INTERFACE, containerInterface);
 		}
 
 		return containerInterface;
 	}
 
 	/**
 	 * 
 	 * @param containerStack
 	 * @param dataEntryForm
 	 */
 	private void updateTopLevelEntitiyInfo(Stack<ContainerInterface> containerStack, DataEntryForm dataEntryForm)
 	{
 		if (containerStack.size() > 1)
 		{
 			dataEntryForm.setIsTopLevelEntity(false);
 		}
 		else
 		{
 			dataEntryForm.setIsTopLevelEntity(true);
 		}
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param form
 	 * @param containerInterface
 	 * @param recordMap
 	 * @param containerStack
 	 * @param valueMapStack
 	 */
 	private void updateStacks(HttpServletRequest request, DataEntryForm dataEntryForm, ContainerInterface containerInterface,
 			Map<BaseAbstractAttributeInterface, Object> recordMap, Stack<ContainerInterface> containerStack,
 			Stack<Map<BaseAbstractAttributeInterface, Object>> valueMapStack)
 	{
 		String dataEntryOperation = dataEntryForm.getDataEntryOperation();
 
		if (dataEntryOperation != null && dataEntryOperation.equalsIgnoreCase("insertChildData") && (dataEntryForm.getErrorList().isEmpty()))
 		{
 			String childContainerId = dataEntryForm.getChildContainerId();
 			AbstractContainmentControlInterface associationControl = UserInterfaceiUtility.getAssociationControl(
 					(ContainerInterface) containerStack.peek(), childContainerId);
 
 			Map<BaseAbstractAttributeInterface, Object> containerValueMap = valueMapStack.peek();
 			AssociationMetadataInterface association = (AssociationMetadataInterface) associationControl.getBaseAbstractAttribute();
 			List<Map<BaseAbstractAttributeInterface, Object>> childContainerValueMapList = (List<Map<BaseAbstractAttributeInterface, Object>>) containerValueMap
 					.get(association);
 
 			Map<BaseAbstractAttributeInterface, Object> childContainerValueMap = null;
 			if (associationControl.isCardinalityOneToMany())
 			{
 				childContainerValueMap = childContainerValueMapList.get(Integer.parseInt(dataEntryForm.getChildRowId()) - 1);
 			}
 			else
 			{
 				childContainerValueMap = childContainerValueMapList.get(0);
 			}
 
 			ContainerInterface childContainer = associationControl.getContainer();
 			UserInterfaceiUtility.addContainerInfo(containerStack, childContainer, valueMapStack, childContainerValueMap);
 		}
 		else if (dataEntryOperation != null && dataEntryOperation.equalsIgnoreCase("insertParentData"))
 		{
 			List<String> errorList = dataEntryForm.getErrorList();
 			if (((errorList != null) && (errorList.isEmpty()))
 					&& (((containerStack != null) && !(containerStack.isEmpty())) && ((valueMapStack != null) && !(valueMapStack.isEmpty()))))
 			{
 				UserInterfaceiUtility.removeContainerInfo(containerStack, valueMapStack);
 			}
 		}
 	}
 
 	/**
 	 * Append number of zeroes to the output depending on precision entered while creating the attribute of double type.
 	 * @param recordMap
 	 */
 	private void addPrecisionZeroes(Map<BaseAbstractAttributeInterface, Object> recordMap)
 	{
 		// If the value is 1.48 and precision entered for it is 3, make it appear as 1.480
 		Set<BaseAbstractAttributeInterface> recordMapKeySet = recordMap.keySet();
 		Iterator iter = recordMapKeySet.iterator();
 
 		while (iter.hasNext())
 		{
 			Object tempObject = iter.next();
 
 			if (tempObject instanceof AttributeInterface)
 			{
 				AttributeInterface currentAttribute = (AttributeInterface) tempObject;
 
 				AttributeTypeInformationInterface attributeTypeInformationInterface = ((AttributeInterface) currentAttribute)
 						.getAttributeTypeInformation();
 
 				if (attributeTypeInformationInterface instanceof NumericAttributeTypeInformation)
 				{
 					int decimalPlaces = ((NumericAttributeTypeInformation) attributeTypeInformationInterface).getDecimalPlaces();
 					String value = (String) recordMap.get(currentAttribute);
 					int placesAfterDecimal = value.length() - (value.indexOf(".") + 1);
 
 					if (placesAfterDecimal != decimalPlaces)
 					{
 						for (int j = decimalPlaces; j > placesAfterDecimal; j--)
 						{
 							value = value + "0";
 						}
 						recordMap.put(currentAttribute, value);
 					}
 				}
 			}
 		}
 
 	}
 }
