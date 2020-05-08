 
 package edu.common.dynamicextensions.ui.webui.action;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AbstractContainmentControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManagerExceptionConstantsInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ApplyFormDefinitionProcessor;
 import edu.common.dynamicextensions.processor.ContainerProcessor;
 import edu.common.dynamicextensions.processor.EntityProcessor;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.webui.actionform.FormDefinitionForm;
 import edu.common.dynamicextensions.ui.webui.util.CacheManager;
 import edu.common.dynamicextensions.ui.webui.util.UserInterfaceiUtility;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManager;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManagerConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.DEConstants;
 
 /**
  * This Action class handles two situations ,
  * 1. When user selects 'Next' from createForm.jsp. This time a call to ApplyFormDefinitionProcessor
  * will just create an entity and populate it with actionform's data. This entity is then saved to cache.
  * 2. When user selects 'Save' from createForm.jsp. This time a call to ApplyFormDefinitionProcessor
  * will create an entity and will save it to database. This entity is then saved to cache.
  * The exception thrown can be of 'Application' type ,in this case the same Screen will be displayed
  * added with error messages .
  * And The exception thrown can be of 'System' type, in this case user will be directed to Error Page.
  * @author deepti_shelar
  */
 public class ApplyFormDefinitionAction extends BaseDynamicExtensionsAction
 {
 
 	/**
 	 * This method will call ApplyFormDefinitionProcessor for actually updating the cache and then
 	 * forwards the action to either BuildForm.jsp or CreateForm.jsp depending on the Operation.
 	 *
 	 * @param mapping ActionMapping mapping
 	 * @param form ActionForm form
 	 * @param  request HttpServletRequest request
 	 * @param response HttpServletResponse response
 	 * @return ActionForward forward to next action
 	 * @throws Exception
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionForward actionForward = null;
 
 		String target = null;
 		FormDefinitionForm formDefinitionForm = (FormDefinitionForm) form;
 
 		String operationMode = formDefinitionForm.getOperationMode();
 		try
 		{
 			if (operationMode != null)
 			{
 				if (operationMode.equals(DEConstants.ADD_SUB_FORM_OPR))
 				{
 					addSubForm(request, formDefinitionForm);
 				}
 				else if (operationMode.equals(DEConstants.EDIT_SUB_FORM_OPR))
 				{
 					editSubForm(request, formDefinitionForm);
 				}
 				else
 				//called when "Add form" or "edit form" operation performed.
 				{
 					applyFormDefinition(request, formDefinitionForm);
 				}
 			}
 
 			String operation = formDefinitionForm.getOperation();
 			if (operation.equals(DEConstants.SAVE_FORM))
 			{
				saveContainer(request);
 				saveMessages(request, getSuccessMessage(formDefinitionForm));
 			}
 
 			target = findForwardTarget(operation);
 			String callbackURL = null;
 			if (target.equals(DEConstants.SHOW_DYEXTN_HOMEPAGE))
 			{
 				callbackURL = redirectCallbackURL(request, WebUIManagerConstants.SUCCESS);
 				if (callbackURL != null && !callbackURL.equals(""))
 				{
 					response.sendRedirect(callbackURL);
 					target = null;
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			target = catchException(e, request);
 			if ((target == null) || (target.equals("")))
 			{
 				actionForward = mapping.getInputForward();
 			}
 		}
 
 		if (target != null && actionForward == null)
 		{
 			actionForward = mapping.findForward(target);
 		}
 		return actionForward;
 	}
 
 	/**
 	 * This method saves the Container in the Database, that is currently in the Cache.
 	 * @param request
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
	private void saveContainer(HttpServletRequest request)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		ContainerProcessor containerProcessor = ContainerProcessor.getInstance();
 
 		//ContainerInterface currentContainer = WebUIManager.getCurrentContainer(request);
 		ContainerInterface currentContainer = (ContainerInterface) CacheManager.getObjectFromCache(
 				request, DEConstants.CONTAINER_INTERFACE);
 		if (currentContainer != null)
 		{
 			containerProcessor.saveContainer(currentContainer);
 		}
 	}
 
 	/**
 	 * @param mapping
 	 * @param operation
 	 * @return
 	 */
 	private String findForwardTarget(String operation)
 	{
 		if (operation != null)
 		{
 			if (operation.equals(DEConstants.BUILD_FORM))
 			{
 				return DEConstants.SHOW_BUILD_FORM_JSP;
 			}
 			else if (operation.equals(DEConstants.SAVE_FORM))
 			{
 				return DEConstants.SHOW_DYEXTN_HOMEPAGE;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 *
 	 * @param request
 	 * @param formDefinitionForm
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void editSubForm(HttpServletRequest request, FormDefinitionForm formDefinitionForm)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if ((request != null) && (formDefinitionForm != null))
 		{
 			ContainerInterface currentContainer = WebUIManager.getCurrentContainer(request);
 			//update container
 
 			EntityGroupInterface entityGroup = (EntityGroup) CacheManager.getObjectFromCache(
 					request, DEConstants.ENTITYGROUP_INTERFACE);
 
 			DynamicExtensionsUtility.checkIfEntityPreExists(entityGroup, currentContainer,
 					formDefinitionForm.getFormName());
 
 			ContainerProcessor containerProcessor = ContainerProcessor.getInstance();
 			containerProcessor.populateContainer(currentContainer, formDefinitionForm, entityGroup);
 
 			//update entity
 			EntityProcessor entityProcessor = EntityProcessor.getInstance();
 			entityProcessor.addEntity(formDefinitionForm, (EntityInterface) currentContainer
 					.getAbstractEntity());
 
 			//Update Associations
 			//Get parent container
 			ContainerInterface parentContainer = null;
 			String parentContainerName = formDefinitionForm.getCurrentContainerName();
 			if (parentContainerName != null)
 			{
 				parentContainer = (ContainerInterface) CacheManager.getObjectFromCache(request,
 						parentContainerName);
 				updateAssociation(parentContainer, currentContainer, formDefinitionForm);
 			}
 		}
 	}
 
 	/**
 	 * @param parentContainer
 	 * @param childContainer
 	 * @param formDefinitionForm
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void updateAssociation(ContainerInterface parentContainer,
 			ContainerInterface childContainer, FormDefinitionForm formDefinitionForm)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if (parentContainer != null && childContainer != null && childContainer.getId() != null)
 		{
 			
 				AbstractContainmentControlInterface containmentAssociationControl = UserInterfaceiUtility
 						.getAssociationControl(parentContainer, childContainer.getId().toString());
 
 				if (containmentAssociationControl != null)
 				{
 					containmentAssociationControl.setCaption(childContainer.getCaption());
 					AssociationInterface association = null;
 					AbstractAttributeInterface abstractAttributeInterface = (AbstractAttributeInterface) containmentAssociationControl
 							.getBaseAbstractAttribute();
 					if ((abstractAttributeInterface != null)
 							&& (abstractAttributeInterface instanceof AssociationInterface))
 					{
 						association = (AssociationInterface) abstractAttributeInterface;
 						ApplyFormDefinitionProcessor applyFormDefinitionProcessor = ApplyFormDefinitionProcessor
 								.getInstance();
 						association = applyFormDefinitionProcessor.associateEntity(association,
 								parentContainer, childContainer, formDefinitionForm);
 					}
 				}
 			
 		}
 	}
 
 	/**
 	 * @param request
 	 * @param formDefinitionForm
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void addSubForm(HttpServletRequest request, FormDefinitionForm formDefinitionForm)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerInterface mainFormContainer = WebUIManager.getCurrentContainer(request);
 
 		EntityGroupInterface entityGroup = (EntityGroup) CacheManager.getObjectFromCache(request,
 				DEConstants.ENTITYGROUP_INTERFACE);
 
 		ApplyFormDefinitionProcessor applyFormDefinitionProcessor = ApplyFormDefinitionProcessor
 				.getInstance();
 		ContainerInterface subFormContainer = applyFormDefinitionProcessor.getSubFormContainer(
 				formDefinitionForm, mainFormContainer, entityGroup);
 
 		DynamicExtensionsUtility.checkIfEntityPreExists(entityGroup, subFormContainer,
 				formDefinitionForm.getFormName(), mainFormContainer);
 
 		AssociationInterface association = applyFormDefinitionProcessor.createAssociation();
 		association = applyFormDefinitionProcessor.associateEntity(association, mainFormContainer,
 				subFormContainer, formDefinitionForm);
 		if (association.getTargetEntity() != null && association.getTargetEntity().getId() != null)
 		{
 			List<AssociationInterface> traversedAssocioations = new ArrayList<AssociationInterface>();
 			traversedAssocioations.add(association);
 			isValidSubForm(association, traversedAssocioations);
 		}
 		applyFormDefinitionProcessor.addSubFormControlToContainer(mainFormContainer,
 				subFormContainer, association);
 
 		if (isNewEnityCreated(formDefinitionForm) && subFormContainer != null)
 		{
 			//if new entity is created, set its container id in form and container interface in cache.
 			applyFormDefinitionProcessor.associateParentGroupToNewEntity(subFormContainer,
 						mainFormContainer);
 				updateCacheReferences(request, subFormContainer);
 		}
 	}
 
 	private void isValidSubForm(AssociationInterface association,
 			List<AssociationInterface> traversedAssocioations)
 			throws DynamicExtensionsApplicationException
 	{
 		if (!traversedAssocioations.isEmpty())
 		{
 			Collection<AssociationInterface> allAssociations = traversedAssocioations.get(0)
 					.getTargetEntity().getAssociationCollection();
 			Iterator<AssociationInterface> iterator = allAssociations.iterator();
 			while (iterator.hasNext())
 			{
 				AssociationInterface tempassociation = iterator.next();
 				traversedAssocioations.add(tempassociation);
 				if (tempassociation.getTargetEntity().getName().equals(
 						association.getEntity().getName()))
 				{
 					throw new DynamicExtensionsApplicationException(
 							"Cannot add Sub Form as it created Circular heirachy.", null,
 							EntityManagerExceptionConstantsInterface.DYEXTN_A_020);
 				}
 			}
 			if (!traversedAssocioations.isEmpty())
 			{
 				traversedAssocioations.remove(0);
 			}
 			isValidSubForm(association, traversedAssocioations);
 		}
 		else
 		{
 			return;
 		}
 	}
 
 	/**
 	 * @param request
 	 * @param container
 	 */
 	private void updateCacheReferences(HttpServletRequest request, ContainerInterface container)
 	{
 		if (container != null)
 		{
 			CacheManager.addObjectToCache(request, DEConstants.CURRENT_CONTAINER_NAME, container
 					.getCaption());
 			CacheManager.addObjectToCache(request, container.getCaption(), container);
 		}
 	}
 
 	/**
 	 * @param formDefinitionForm
 	 * @return
 	 */
 	private boolean isNewEnityCreated(FormDefinitionForm formDefinitionForm)
 	{
 		if (formDefinitionForm != null && ProcessorConstants.CREATE_AS_NEW.equals(formDefinitionForm.getCreateAs()))
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @param request
 	 * @param formDefinitionForm
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void applyFormDefinition(HttpServletRequest request,
 			FormDefinitionForm formDefinitionForm) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ContainerInterface container = WebUIManager.getCurrentContainer(request);
 		EntityGroupInterface entityGroup = (EntityGroupInterface) CacheManager.getObjectFromCache(
 				request, DEConstants.ENTITYGROUP_INTERFACE);
 
 		ApplyFormDefinitionProcessor applyFormDefinitionProcessor = ApplyFormDefinitionProcessor
 				.getInstance();
 
 		DynamicExtensionsUtility.checkIfEntityPreExists(entityGroup, container, formDefinitionForm
 				.getFormName());
 
 		container = applyFormDefinitionProcessor.addEntityToContainer(container,
 				formDefinitionForm, entityGroup);
 
 		updateCacheReferences(request, container);
 		if (CacheManager.getObjectFromCache(request, DEConstants.CONTAINER_INTERFACE) == null)
 		{
 			CacheManager.addObjectToCache(request, DEConstants.CONTAINER_INTERFACE, container);
 		}
 	}
 
 	/**
 	 *
 	 * @param formDefinitionForm actionform
 	 * @return ActionMessages Messages
 	 */
 	private ActionMessages getSuccessMessage(FormDefinitionForm formDefinitionForm)
 	{
 		ActionMessages actionMessages = new ActionMessages();
 		String formName = formDefinitionForm.getFormName();
 		actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
 				"app.entitySaveSuccessMessage", formName));
 		return actionMessages;
 	}
 
 	/**
 	 * This method gets the Callback URL from cache, reforms it and redirect the response to it.
 	 * @param request HttpServletRequest to obtain session
 	 * @param response HttpServletResponse to redirect the CallbackURL
 	 * @param recordIdentifier Identifier of the record to reconstruct the CallbackURL
 	 * @return true if CallbackURL is redirected, false otherwise
 	 * @throws IOException
 	 */
 	private String redirectCallbackURL(HttpServletRequest request, String webUIManagerConstant)
 			throws IOException
 	{
 		String calllbackURL = (String) CacheManager.getObjectFromCache(request,
 				DEConstants.CALLBACK_URL);
 		if (calllbackURL != null && !calllbackURL.equals(""))
 		{
 			String associationIds = CacheManager.getAssociationIds(request);
 			ContainerInterface containerInterface = (ContainerInterface) CacheManager
 					.getObjectFromCache(request, DEConstants.CONTAINER_INTERFACE);
 			calllbackURL = calllbackURL + "?" + WebUIManager.getOperationStatusParameterName()
 					+ "=" + webUIManagerConstant + "&"
 					+ WebUIManagerConstants.DELETED_ASSOCIATION_IDS + "=" + associationIds;
 			//Fix for bug 5176
 			if (containerInterface != null && containerInterface.getId() != null)
 			{
 				String containerIdUrl = "&" + WebUIManager.getContainerIdentifierParameterName()
 						+ "=" + containerInterface.getId().toString();
 				calllbackURL = calllbackURL + containerIdUrl;
 			}
 
 			CacheManager.clearCache(request);
 		}
 		return calllbackURL;
 	}
 
 }
