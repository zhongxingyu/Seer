 // Copyright 2008 Thiago H. de Paula Figueiredo
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package br.com.arsmachina.tapestrycrud.base;
 
 import java.io.Serializable;
 
 import org.apache.tapestry5.ComponentResources;
 import org.apache.tapestry5.EventConstants;
 import org.apache.tapestry5.EventContext;
 import org.apache.tapestry5.Field;
 import org.apache.tapestry5.PersistenceConstants;
 import org.apache.tapestry5.annotations.AfterRenderTemplate;
 import org.apache.tapestry5.annotations.Meta;
 import org.apache.tapestry5.annotations.OnEvent;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Retain;
 import org.apache.tapestry5.beaneditor.BeanModel;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.BeanModelSource;
 import org.apache.tapestry5.services.Request;
 
 import br.com.arsmachina.controller.ReadableController;
 import br.com.arsmachina.module.service.PrimaryKeyTypeService;
 import br.com.arsmachina.tapestrycrud.Constants;
 import br.com.arsmachina.tapestrycrud.EditPage;
 import br.com.arsmachina.tapestrycrud.encoder.ActivationContextEncoder;
 
 /**
  * Base class for pages that edit entity objects. One example of its use can be found in the Ars
  * Machina Project Example Application (<a
  * href="http://ars-machina.svn.sourceforge.net/viewvc/ars-machina/example/trunk/src/main/java/br/com/arsmachina/example/web/pages/project/EditProject.java?view=markup"
  * >page class</a>. <a
  * href="http://ars-machina.svn.sourceforge.net/viewvc/ars-machina/example/trunk/src/main/webapp/project/EditProject.tml?view=markup"
  * >template</a>).
  * 
  * @param <T> the entity class related to this encoder.
  * @param <K> the type of the class' primary key property.
  * 
  * @author Thiago H. de Paula Figueiredo
  */
 @Meta("tapestry.persistence-strategy=" + PersistenceConstants.FLASH)
 public abstract class BaseEditPage<T, K extends Serializable> extends BasePage<T, K> implements
 		EditPage<T, K> {
 
 	private static final String DEFAULT_CONSTRUCTOR_NOT_FOUND_MESSAGE = "Class %s does not have a single argument constructor";
 
 	/**
 	 * You can change the persistence strategy from flash to another using
 	 * <code>@Meta("tapestry.persistence-strategy=" + PersistenceConstants.FLASH)</code> in your class.
 	 */
 	@Persist
 	private T object;
 
 	@Inject
 	private Request request;
 
 	@Inject
 	private ComponentResources componentResources;
 
 	@org.apache.tapestry5.annotations.Component(id = Constants.FORM_ID)
 	private Form form;
 
 	@Retain
 	private ActivationContextEncoder<T> activationContextEncoder;
 	
 	@Inject
 	private BeanModelSource beanModelSource;
 	
 	@Inject
 	private PrimaryKeyTypeService primaryKeyTypeService;
 
 	/**
 	 * Single constructor of this class.
 	 */
 	public BaseEditPage() {
 
 		final Class<T> entityClass = getEntityClass();
 		activationContextEncoder = getActivationContextEncoder(entityClass);
 
 	}
 
 	/**
 	 * Ensures the edited object is not null before form rendering and submission. It uses
 	 * {@link #createNewObject()} to create a new entity object if needed. If not,
 	 * <code>getController.reattach(object)</code> is invoked.
 	 * 
 	 * @see ReadableController#reattach(Object)
 	 */
 	@OnEvent(component = Constants.FORM_ID, value = EventConstants.PREPARE)
 	final protected void prepare() {
 
 		final T object = getObject();
 
 		if (object == null) {
 			setObject(createNewObject());
 		}
 		else {
 			if (isObjectPersistent()) {
 				getController().reattach(object);
 			}
 		}
 		
 		prepareObject();
 
 	}
 	
 	/**
 	 * Tells if {@link #onActivate(EventContext)} will ignore the activation or not.
 	 * This implementation returns <code>false</code>. If overriden to return <code>false</code>,
 	 * the edited object is always the one returned by {@link #getObject()}, so it must be overriden
 	 * too. 
 	 * 
 	 * @return a <code>boolean</code>.
 	 */
 	protected boolean ignoreActivationContext() {
 		return false;
 	}
 
 	/**
 	 * Invoked by {@link #prepare()} after it has done its object preparation.
 	 * This implementation does nothing and should be overriden by subclasses that need
 	 * further object preparation.
 	 */
 	protected void prepareObject() {
 	}
 
 	/**
 	 * Validates the object. This method invokes {@link #validateObject(Form)} and then takes care
 	 * of handling AJAX form submissions.
 	 */
 	@OnEvent(component = Constants.FORM_ID, value = EventConstants.VALIDATE_FORM)
 	final protected Object validate() {
 
 		// clear the confirmation message, if set.
 		setMessage(null);
 
 		final T object = getObject();
 		final Form form = getForm();
 
 		validateObject(object, form);
 
 		Object returnValue = null;
 
 		final boolean hasErrors = getForm().getHasErrors();
 
 		if (hasErrors && request.isXHR() && useAjaxForFormSubmits()) {
 			returnValue = getFormZone().getBody();
 		}
 
 		return returnValue;
 
 	}
 
 	/**
 	 * Validates an <code>object</code> and stores the validation erros in a <code>form</code>.
 	 * This implementation does nothing.
 	 * 
 	 * @param object an {@link #T}.
 	 * @param a {@link Form}.
 	 */
 	protected void validateObject(T object, Form form) {
 	}
 
 	/**
 	 * Adds an error to a given field in the form.
 	 * 
 	 * @param fieldId a {@link String}. It cannot be null.
 	 * @param message a {@link String}. It cannot be null.
 	 */
 	final protected void addError(String fieldId, String message) {
 
 		assert fieldId != null;
 		assert message != null;
 
 		Field field = (Field) componentResources.getEmbeddedComponent(fieldId);
 		form.recordError(field, message);
 
 	}
 	
 	/**
 	 * Creates a {@link BeanModel} and removes the primary key property from it.
 	 * 
 	 * @return a {@link BeanModel}.
 	 */
 	public BeanModel<T> getBeanModel() {
 
 		final BeanModel<T> beanModel = beanModelSource.createEditModel(getEntityClass(),
 				getMessages());
 		
 		beanModel.exclude(primaryKeyTypeService.getPrimaryKeyPropertyName(getEntityClass()));
 
 		return beanModel;
 		
 	}	
 
 	/**
 	 * Saves or updates the edited object. This method does the following steps:
 	 * <ol>
 	 * <li>Invokes {@link #prepareObjectForSaveOrUpdate()}</li>.
 	 * <li>Invokes {@link #getController()}<code>.saveOrUpdate(entity);</code>.
 	 * <li>Invokes and returns {@link #returnFromRemove()} </li>
 	 * </ol>
 	 */
 	@OnEvent(component = Constants.FORM_ID, value = EventConstants.SUCCESS)
 	final public Object saveOrUpdate() {
 
 		prepareObjectForSaveOrUpdate();
 		T entity = getObject();
 		entity = saveOrUpdate(entity);
 		setObject(entity);
 
 		return returnFromRemove();
 
 	}
 
 	/**
 	 * Saves or updates an entity object. This implementation returns
 	 * <code>getController().saveOrUpdate(entity)</code>.
 	 * 
 	 * @param entity a <code>T</code>. It cannot be null.
 	 * @return a <code>T</code>.
 	 */
 	protected T saveOrUpdate(T entity) {
 		return getController().saveOrUpdate(entity);
 	}
 
 	/**
 	 * Defines what {@link #saveOrUpdate()} will return and sets the success message.
 	 * 
 	 * @return an {@link Object} or <code>null</code>.
 	 */
 	protected Object returnFromRemove() {
 
 		Object returnValue = null;
 
 		if (request.isXHR() && useAjaxForFormSubmits()) {
 
 			if (returnZoneOnXHR()) {
 				returnValue = getFormZone().getBody();
 			}
 			else {
 				returnValue = componentResources.getBlock(getFormBlockId());
 			}
 
 			setSaveOrUpdateSuccessMessage();
 
 		}
 		else {
 
 			BaseListPage<T, K> page = getListPage();
 
 			if (page != null) {
 
 				page.setMessage(Constants.MESSAGE_SAVEORUPDATE_SUCCESS);
 				returnValue = page;
 
 			}
 			else {
 				setSaveOrUpdateSuccessMessage();
 			}
 
 		}
 
 		return returnValue;
 
 	}
 
 	/**
 	 * Sets the save or update success message in this page.
 	 */
 	protected void setSaveOrUpdateSuccessMessage() {
 		setMessage(getMessages().get(Constants.MESSAGE_SAVEORUPDATE_SUCCESS));
 	}
 
 	/**
 	 * Returns the {@link BaseListPage} instance associated to this object. This is used by
 	 * {@link #returnAfterAction()} to define what will be returned by {@link #saveOrUpdate()}.
 	 * This implementation returns null and must be overriden by pages that want to show the listing
 	 * page after a successful save or update.
 	 * 
 	 * @return a {@link BaseListPage}.
 	 */
 	protected BaseListPage<T, K> getListPage() {
 		return null;
 	}
 
 	/**
 	 * Does any processing that must be done in the object before it is saved or updated.
 	 */
 	protected void prepareObjectForSaveOrUpdate() {
 
 	}
 
 	/**
 	 * Creates a new entity object to be edited. Fields can be prefilled if desired. This method is
 	 * used by {@link #prepare()}. This implementation attempts to instantiate the object using its
 	 * class default constructor.
 	 * 
 	 * @return a {@link T}.
 	 */
 	protected T createNewObject() {
 
 		try {
 			return getEntityClass().newInstance();
 		}
 		catch (InstantiationException e) {
 
 			final String exceptionMessage = String.format(DEFAULT_CONSTRUCTOR_NOT_FOUND_MESSAGE,
 					getEntityClass().getName());
 			
 			throw new RuntimeException(exceptionMessage, e);
 
 		}
 		catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	/**
 	 * Returns the value of the <code>object</code> property.
 	 * 
 	 * @return a {@link T}.
 	 */
 	public T getObject() {
 		return object;
 	}
 
 	/**
 	 * Changes the value of the <code>object</code> property.
 	 * 
 	 * @param object a {@link T}.
 	 */
 	public void setObject(T object) {
 		this.object = object;
 	}
 
 	/**
 	 * Returns the value of the <code>form</code> property.
 	 * 
 	 * @return a {@link Form}.
 	 */
 	protected final Form getForm() {
 		return form;
 	}
 
 	/**
 	 * Returns the current activation context of this page.
 	 * 
 	 * @return an {@link #A}.
 	 */
 	final public Object onPassivate() {
 
 		Object value = null;
 
 		if (ignoreActivationContext() == false) {
 		
 			final T o = getObject();
 			
 			if (o != null && isObjectPersistent()) {
 				value = getActivationContextEncoder(getEntityClass()).toActivationContext(o);
 			}
 			
 		}
 		
 		return value;
 
 	}
 
 	/**
	 * Clears the object, form errors and message after page rendering.
 	 */
 	@AfterRenderTemplate
	final void clearPageState() {
 		form.getDefaultTracker().clear();
 		setMessage(null);
		setObject(null);
 	}
 
 	/**
 	 * Sets the object property from a given activation context value.
 	 * 
 	 * @param value an {@link EventContext}.
 	 */
 	void onActivate(EventContext context) {
 		
 		if (ignoreActivationContext() == false) {
 		
 			if (context.getCount() == 0) {
 				checkStoreTypeAccess();
 			}
 			else {
 				checkUpdateTypeAccess();
 			}
 			
 			if (object == null) {
 			
 				final T activationContextObject = activationContextEncoder.toObject(context);
 				setObject(activationContextObject);
 				
 			}
 			
 		}
 		
 		if (getObject() != null && isObjectPersistent()) {
 			checkUpdateObjectAccess(getObject());
 		}
 		
 	}
 
 	/**
 	 * Checks if the current user has permission to update instances of the page entity class
 	 * and throws an exception if not.
 	 * This method calls <code>getAuthorizer().checkUpdate(getEntityClass())</code>.
 	 */
 	protected void checkUpdateTypeAccess() {
 		getAuthorizer().checkUpdate(getEntityClass());
 	}
 
 	/**
 	 * Checks if the current user has permission to store instances of the page entity class
 	 * and throws an exception if not.
 	 * This method calls <code>getAuthorizer().checkStore(getEntityClass())</code>.
 	 */
 	protected void checkStoreTypeAccess() {
 		getAuthorizer().checkStore(getEntityClass());
 	}
 
 	/**
 	 * Checks if the current user has permission to update a given object.
 	 * and throws an exception if not.
 	 * This method calls <code>getAuthorizer().checkUpdate(activationContextObject)</code>.
 	 * 
 	 * @param activationContextObject a <code>T</code> instance. It cannot be null.
 	 */
 	protected void checkUpdateObjectAccess(T activationContextObject) {
 		getAuthorizer().checkUpdate(activationContextObject);
 	}
 
 	/**
 	 * Tells if the object is persistent.
 	 * 
 	 * @return a <code>boolean</code>.
 	 */
 	final protected boolean isObjectPersistent() {
 		return object != null && getController().isPersistent(object);
 	}
 
 	/**
 	 * Returns <code>null</code> if we are inserting a new object and
 	 * {@link #DEFAULT_FORM_ZONE_ID} (<code>zone</code>) otherwise.
 	 * This method also returns <code>null</code> if {@link #useAjaxForFormSubmits()}
 	 * return <code>false</code>.
 	 * 
 	 * @return a {@link String}.
 	 */
 	public String getZone() {
 		return isObjectPersistent() && useAjaxForFormSubmits() ? Constants.DEFAULT_FORM_ZONE_ID : null;
 	}
 
 	/**
 	 * Sets the object to <code>null</code>.
 	 */
 	@OnEvent(Constants.NEW_OBJECT_EVENT)
 	public final void clearObject() {
 		setObject(null);
 	}
 	
 	/**
 	 * Defines if form submissions will be done through AJAX or not.
 	 * This implementation returns <code>true</code>.
 	 * 
 	 * @return a <code>boolean</code>.
 	 * @return
 	 */
 	protected boolean useAjaxForFormSubmits() {
 		return true;
 	}
 	
 }
