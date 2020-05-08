 package burrito.client.crud.custom;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import burrito.client.crud.CrudFieldResolver;
 import burrito.client.crud.CrudGenericException;
 import burrito.client.crud.CrudService;
 import burrito.client.crud.CrudServiceAsync;
 import burrito.client.crud.FieldValueNotUniqueException;
 import burrito.client.crud.generic.CrudEntityDescription;
 import burrito.client.crud.generic.CrudField;
 import burrito.client.crud.input.CrudInputField;
 import burrito.client.crud.input.CrudInputFieldWrapper;
 import burrito.client.crud.labels.CrudLabelHelper;
 import burrito.client.widgets.form.EditForm.SaveCallback;
 import burrito.client.widgets.form.EditFormMessages;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * This helper allows you to easily create custom CRUD edit forms. Create an
  * instance of this type based on a {@link CrudEntityDescription} and use it to
  * register fields, validate and save.
  * 
  * @author henper
  * 
  */
 public class CrudEditFormHelper {
 
 	private CrudEntityDescription desc;
 	private CrudServiceAsync service = GWT.create(CrudService.class);
 	private List<CrudInputFieldWrapper> inputFields = new ArrayList<CrudInputFieldWrapper>();
 	private EditFormMessages messages = GWT.create(EditFormMessages.class);
 
 	public CrudEditFormHelper(CrudEntityDescription desc) {
 		this.desc = desc;
 	}
 
 	/**
 	 * Gets the model used by this helper
 	 * 
 	 * @return
 	 */
 	public CrudEntityDescription getDesc() {
 		return desc;
 	}
 
 	public void setDesc(CrudEntityDescription desc) {
 		this.desc = desc;
 	}
 
 	/**
 	 * Registers a field. Sets its value and adds it to the list of input fields
 	 * being validated. If you try to add a field which is not in the list of
 	 * fields described by the {@link CrudEntityDescription} model, an error
 	 * message will be displayed in the field wrapper.
 	 * 
 	 * @param fieldLoader
 	 */
 	public void register(CrudInputFieldWrapper fieldLoader) {
 		CrudField field = desc.getField(fieldLoader.getFieldName());
 		if (field == null) {
 			fieldLoader.flagAsMissingField();
 		} else {
 			CrudInputField<?> input = CrudFieldResolver.createInputField(field,
 					service);
 			fieldLoader.init(input);
 			inputFields.add(fieldLoader);
 		}
 	}
 	/**
 	 * Registers a bunch of fields
 	 * 
 	 * @param wrappers
 	 */
 	public void register(CrudInputFieldWrapper... wrappers) {
 		for (CrudInputFieldWrapper wrapper : wrappers) {
 			register(wrapper);
 		}
 	}
 
 	private void updateCrudDescription() {
 		desc.getFields().clear();
 		for(CrudInputFieldWrapper field : inputFields) {
 			CrudField cf = field.getCrudField();
			if (!cf.isReadOnly()) {
				cf.setValue(field.getValue());
				desc.add(cf);
			}
 		}
 	}
 
 	/**
 	 * Saves the entity as described by its fields
 	 * 
 	 * @param callback
 	 */
 	public void save(final SaveCallback saveCallback) {
 		if (!validate()) {
 			saveCallback.failed(messages.thereAreValidationErrors());
 			return;
 		}
 		updateCrudDescription();
 		service.save(desc, desc.getClonedFromId(), new AsyncCallback<Long>() {
 
 			@Override
 			public void onSuccess(Long result) {
 				desc.setId(result);
 				saveCallback.success();
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				String errorMessage;
 				if (caught instanceof FieldValueNotUniqueException) {
 					errorMessage = messages
 							.fieldValueNotUniqueError(getDisplayLabel(
 									((FieldValueNotUniqueException) caught)
 											.getFieldName(), desc
 											.getEntityName()));
 				} else if (caught instanceof CrudGenericException) {
 					errorMessage = ((CrudGenericException) caught).getMessage();
 				} else {
 					errorMessage = "Failed to save entity "
 							+ desc.getEntityName();
 				}
 				saveCallback.failed(errorMessage);
 			}
 		});
 	}
 
 	/**
 	 * Steps one step up in the hierarchy
 	 */
 	public void returnToEntityIndex() {
 		History.newItem(desc.getEntityName());
 	}
 
 	private boolean validate() {
 		for (CrudInputFieldWrapper field : inputFields) {
 			if (!field.validate()) {
 				field.highlight();
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private String getDisplayLabel(String fieldName, String entityName) {
 		String fullFieldName = entityName.replace('.', '_') + "_" + fieldName;
 		return CrudLabelHelper.getString(fullFieldName, fieldName);
 	}
 
 }
