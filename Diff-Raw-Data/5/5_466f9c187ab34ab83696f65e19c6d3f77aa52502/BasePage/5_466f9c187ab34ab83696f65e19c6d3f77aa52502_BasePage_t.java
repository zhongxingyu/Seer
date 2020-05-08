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
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 
 import org.apache.tapestry5.Block;
 import org.apache.tapestry5.ComponentResources;
 import org.apache.tapestry5.EventContext;
 import org.apache.tapestry5.PersistenceConstants;
 import org.apache.tapestry5.PrimaryKeyEncoder;
 import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.IncludeStylesheet;
 import org.apache.tapestry5.annotations.OnEvent;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Retain;
 import org.apache.tapestry5.corelib.components.Zone;
 import org.apache.tapestry5.ioc.Messages;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.ComponentSource;
 import org.apache.tapestry5.services.ValueEncoderSource;
 
 import br.com.arsmachina.authorization.Authorizer;
 import br.com.arsmachina.controller.Controller;
 import br.com.arsmachina.module.service.ControllerSource;
 import br.com.arsmachina.module.service.PrimaryKeyTypeService;
 import br.com.arsmachina.tapestrycrud.Constants;
 import br.com.arsmachina.tapestrycrud.CrudPage;
 import br.com.arsmachina.tapestrycrud.encoder.ActivationContextEncoder;
 import br.com.arsmachina.tapestrycrud.encoder.LabelEncoder;
 import br.com.arsmachina.tapestrycrud.selectmodel.SelectModelFactory;
 import br.com.arsmachina.tapestrycrud.services.ActivationContextEncoderSource;
 import br.com.arsmachina.tapestrycrud.services.LabelEncoderSource;
 import br.com.arsmachina.tapestrycrud.services.PrimaryKeyEncoderSource;
 import br.com.arsmachina.tapestrycrud.services.TapestryCrudModuleService;
 
 /**
  * Class that implements some common infrastructure for listing and editing
  * pages. This class is not
  * 
  * @author Thiago H. de Paula Figueiredo
  * @param <T> the entity class related to this encoder.
  * @param <K> the type of the class' primary key property.
  */
@IncludeStylesheet(Constants.TAPESTRY_CRUD_CSS_ASSET)
 @SuppressWarnings("deprecation")
 public abstract class BasePage<T, K extends Serializable> implements
 		CrudPage<T, K> {
 
 	@Retain
 	private PrimaryKeyEncoder<K, T> primaryKeyEncoder;
 
 	@Inject
 	private ActivationContextEncoderSource activationContextEncoderSource;
 
 	@Inject
 	private Authorizer authorizer;
 
 	@Inject
 	private ControllerSource controllerSource;
 
 	@Inject
 	private LabelEncoderSource labelEncoderSource;
 
 	@Inject
 	private ValueEncoderSource valueEncoderSource;
 
 	@Inject
 	private SelectModelFactory selectModelFactory;
 
 	@Inject
 	private PrimaryKeyEncoderSource primaryKeyEncoderSource;
 
 	@Inject
 	private PrimaryKeyTypeService primaryKeyTypeService;
 
 	@Inject
 	private TapestryCrudModuleService tapestryCrudModuleService;
 
 	@Retain
 	private Class<T> entityClass;
 
 	@Retain
 	private Controller<T, K> controller;
 
 	@Persist(PersistenceConstants.FLASH)
 	private String message;
 
 	@Inject
 	private ComponentResources componentResources;
 
 	@Inject
 	private Messages messages;
 
 	@Inject
 	private ComponentSource componentSource;
 
 	@Retain
 	private Class<K> primaryKeyClass;
 
 	private boolean removedObjectNotFound;
 
 	/**
 	 * Single constructor of this class.
 	 */
 	@SuppressWarnings("unchecked")
 	public BasePage() {
 
 		final Type genericSuperclass = getClass().getGenericSuperclass();
 		final ParameterizedType parameterizedType =
 			((ParameterizedType) genericSuperclass);
 		entityClass = (Class<T>) parameterizedType.getActualTypeArguments()[0];
 		primaryKeyClass = primaryKeyTypeService.getPrimaryKeyType(entityClass);
 
 		controller = controllerSource.get(entityClass);
 
 		primaryKeyEncoder =
 			(PrimaryKeyEncoder<K, T>) primaryKeyEncoderSource.get(entityClass);
 
 		assert entityClass != null;
 		assert primaryKeyClass != null;
 		assert controller != null;
 		assert primaryKeyEncoder != null;
 
 	}
 
 	/**
 	 * Returns the {@link ValueEncoder} for a given class.
 	 * 
 	 * @param <V> the class.
 	 * @param clasz a {@link Class};
 	 * @return a {@link ValueEncoder}.
 	 */
 	protected <V> ValueEncoder<V> getValueEncoder(Class<V> clasz) {
 		return valueEncoderSource.getValueEncoder(clasz);
 	}
 
 	/**
 	 * Returns the {@link LabelEncoder} for a given class.
 	 * 
 	 * @param <V> the class.
 	 * @param clasz a {@link Class};
 	 * @return a {@link LabelEncoder}.
 	 */
 	protected <V> LabelEncoder<V> getLabelEncoder(Class<V> clasz) {
 		return labelEncoderSource.get(clasz);
 	}
 
 	/**
 	 * Returns the {@link ActivationContextEncoder} for a given class.
 	 * 
 	 * @param <V> the class.
 	 * @param clasz a {@link Class};
 	 * @return an {@link ActivationContextEncoder}.
 	 */
 	protected <V, X extends Serializable> ActivationContextEncoder<V> getActivationContextEncoder(
 			Class<V> clasz) {
 		return activationContextEncoderSource.get(clasz);
 	}
 
 	/**
 	 * Returns the {@link PrimaryKeyEncoder} for a given class.
 	 * 
 	 * @param <V> the class.
 	 * @param <X> the class' primary key field type.
 	 * @param clasz a {@link Class};
 	 * @return a {@link PrimaryKeyEncoder}.
 	 */
 	protected <V, X extends Serializable> PrimaryKeyEncoder<X, V> getPrimaryKeyEncoder(
 			Class<V> clasz) {
 		return primaryKeyEncoderSource.get(clasz);
 	}
 
 	/**
 	 * @see br.com.arsmachina.tapestrycrud.CrudPage#getMessage()
 	 */
 	public String getMessage() {
 		return message;
 	}
 
 	/**
 	 * @see br.com.arsmachina.tapestrycrud.CrudPage#setMessage(java.lang.String)
 	 */
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	/**
 	 * Returns the value of the <code>controller</code> property.
 	 * 
 	 * @return a {@link Controller<T,K>}.
 	 */
 	public final Controller<T, K> getController() {
 		return controller;
 	}
 
 	public final Class<T> getEntityClass() {
 		return entityClass;
 	}
 
 	public final Class<K> getPrimaryKeyClass() {
 		return primaryKeyClass;
 	}
 
 	/**
 	 * Returns the value of the <code>messages</code> property.
 	 * 
 	 * @return a {@link Messages}.
 	 */
 	public final Messages getMessages() {
 		return messages;
 	}
 
 	/**
 	 * Returns the {@link Zone} that surrounds the form.
 	 * 
 	 * @return a {@link Zone}.
 	 */
 	public Zone getFormZone() {
 		return (Zone) componentResources.getEmbeddedComponent(getFormZoneId());
 	}
 
 	/**
 	 * ID of the {@link Block} that will be returned when a form is submitted
 	 * via AJAX. This implementation returns {@link #DEFAULT_FORM_BLOCK_ID} (
 	 * <code>block</code>).
 	 * 
 	 * @return a {@link String}.
 	 */
 	String getFormBlockId() {
 		return Constants.DEFAULT_FORM_BLOCK_ID;
 	}
 
 	/**
 	 * ID of the {@link Zone} that will be returned when a form is submitted via
 	 * AJAX. This implementation returns {@link #DEFAULT_FORM_ZONE_ID} (
 	 * <code>zone</code>).
 	 * 
 	 * @return a {@link String}.
 	 */
 	String getFormZoneId() {
 		return Constants.DEFAULT_FORM_ZONE_ID;
 	}
 
 	/**
 	 * Used by {@link #returnFromRemove()} to know whether it must return a
 	 * {@link Zone} or a {@link Block}. This implementation returns
 	 * <code>true</code>.
 	 * 
 	 * @return a <code>boolean</code>.
 	 */
 	protected boolean returnZoneOnXHR() {
 		return true;
 	}
 
 	/**
 	 * Returns the value of the <code>selectModelFactory</code> property.
 	 * 
 	 * @return a {@link SelectModelFactory}.
 	 */
 	final protected SelectModelFactory getSelectModelFactory() {
 		return selectModelFactory;
 	}
 
 	/**
 	 * Returns the value of the <code>authorizer</code> property.
 	 * 
 	 * @return a {@link Authorizer}.
 	 */
 	final public Authorizer getAuthorizer() {
 		return authorizer;
 	}
 
 	/**
 	 * This method listens to the {@link Constants#REMOVE_OBJECT_ACTION} event
 	 * and removes the corresponding object.
 	 * 
 	 * @param context an {@link EventContext}.
 	 */
 	@OnEvent(Constants.REMOVE_OBJECT_ACTION)
 	protected Object remove(EventContext context) {
 
 		getAuthorizer().checkRemove(getEntityClass());
 
 		K id = context.get(getPrimaryKeyClass(), 0);
 		final T toBeRemoved = primaryKeyEncoder.toValue(id);
 
 		if (toBeRemoved != null) {
 			getAuthorizer().checkRemove(toBeRemoved);
 		}
 
 		return remove(toBeRemoved);
 
 	}
 
 	/**
 	 * Removes or not a given object.
 	 * 
 	 * @param object a {@link K}.
 	 */
 	protected Object remove(T object) {
 
 		if (object != null) {
 			getController().delete(object);
 		} else {
 			removedObjectNotFound = true;
 		}
 
 		return returnFromDoRemove();
 
 	}
 
 	/**
 	 * Defines what {@link #doRemove()} will return.
 	 * 
 	 * @return an {@link Object} or <code>null</code>.
 	 */
 	@SuppressWarnings("unchecked")
 	protected Object returnFromDoRemove() {
 
 		Class<?> listPageClass =
 			tapestryCrudModuleService.getListPageClass(getEntityClass());
 		BaseListPage<T, K> listPage =
 			(BaseListPage<T, K>) componentSource.getPage(listPageClass);
 
 		if (removedObjectNotFound) {
 			listPage.setMessage(getRemoveErrorNotFoundMessage());
 		} else {
 			listPage.setMessage(getRemoveSuccessMessage());
 		}
 
 		return listPage;
 
 	}
 
 	/**
 	 * Returns the remove success message.
 	 * 
 	 * @return a {@link String}.
 	 */
 	protected String getRemoveSuccessMessage() {
 		return getMessages().get(Constants.MESSAGE_SUCCESS_REMOVE);
 	}
 
 	/**
 	 * Returns the remove success message.
 	 * 
 	 * @return a {@link String}.
 	 */
 	protected String getRemoveErrorNotFoundMessage() {
 		return getMessages().get(Constants.MESSAGE_ERROR_REMOVE_NOT_FOUND);
 	}
 
 	/**
 	 * Returns the value of the <code>tapestryCrudModuleService</code> property.
 	 * 
 	 * @return a {@link TapestryCrudModuleService}.
 	 */
 	final protected TapestryCrudModuleService getTapestryCrudModuleService() {
 		return tapestryCrudModuleService;
 	}
 
 	/**
 	 * Returns the value of the <code>removedObjectNotFound</code> property.
 	 * 
 	 * @return a <code>boolean</code>.
 	 */
 	public boolean isRemovedObjectNotFound() {
 		return removedObjectNotFound;
 	}
 
 }
