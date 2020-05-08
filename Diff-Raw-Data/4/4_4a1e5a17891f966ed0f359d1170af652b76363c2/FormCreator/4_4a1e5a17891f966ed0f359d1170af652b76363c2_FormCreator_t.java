 package ch.exmachina.vaadin.autoforms;
 
 import ch.exmachina.vaadin.autoforms.containers.utils.ContainerUtils;
 
 import com.vaadin.data.fieldgroup.*;
 import com.vaadin.ui.*;
 
 /**
  * @autor Marco Manzi
  */
 public abstract class FormCreator<T> extends UnbindedFormCreator {
 
 	private BeanFieldGroup<T> binder;
 
 	private T bean;
 
 	private ValidationManager validationManager = new ValidationManager();
 
 	public FormCreator(GridRender gridRender) {
 		super(gridRender);
 	}
 
 	public FormCreator() {
 		super();
 	}
 
 	protected final void initWithBean(T bean) {
 		this.bean = bean;
 		bindToBean();
 	}
 
 	/**
 	 * Commit the current values on form, updating the bean
 	 *
 	 * @return the bean with values of form
 	 * @throws IllegalArgumentException
 	 */
 	public T commit() {
 		try {
 			binder.commit();
 			return binder.getItemDataSource().getBean();
 		} catch (FieldGroup.CommitException e) {
 //			throw new IllegalArgumentException(e);
 			return null;
 		}
 	}
 
 	private void setValueOnSelectLazyLoadedValues(T bean) {
 		for (String fieldName : fieldsOfForm.keySet()) {
 			FormField formField = fieldsOfForm.get(fieldName);
 			ContainerUtils.setOnSelectContainerBeanValue(bean, fieldName, formField.getField());
 		}
 	}
 
 	/**
 	 * Return the backed bean of the form, this will we used with the BeanFieldGroup
 	 * to provide auto validation, and will fill the fields with the model value.
 	 * Field name and component properties have to be the same
 	 *
 	 * @return Model of the form
 	 */
 	public T getBean() {
 		return binder.getItemDataSource().getBean();
 	}
 
 	/**
 	 * Update the form with the values of bean
 	 *
 	 * @param bean the bean with values to show in form
 	 */
 	public void setBean(T bean) {
 		setValueOnSelectLazyLoadedValues(bean);
 		binder.setItemDataSource(bean);
 		for (String fieldName : fieldsOfForm.keySet()) {
 			FormField formField = fieldsOfForm.get(fieldName);
 			if (formField.isReadOnly()) {
 				formField.getField().setReadOnly(true);
 			}
 		}
 	}
 
 	protected T getNotBindedBean() {
 		return bean;
 	}
 
 	/**
 	 * Setup the binder to the bean
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void bindToBean() {
 		binder = new BeanFieldGroup(bean.getClass());
 		validationManager.addRequiredValidationAndValidateOnBlur(binder);
 		validationManager.setErrorMessage(getLabelFor(ValidationManager.KEY_REQUIRED_ERROR));
 
 		for (String fieldName : fieldsOfForm.keySet()) {
 			FormField formField = fieldsOfForm.get(fieldName);

			//clean all previous validation errors, if present
			formField.getField().setComponentError(null);

 			if (formField.useBinder()) {
 				Field<?> field = formField.getField();
 				binder.bind(field, fieldName);
 			}
 		}
 		setBean(bean);
 	}
 }
