 package net.batkin.forms.server.db.dataModel.form.widget;
 
 import java.util.Map;
 
 import net.batkin.forms.server.db.dataModel.form.FieldData;
 import net.batkin.forms.server.db.dataModel.schema.fields.FieldValidationException;
 import net.batkin.forms.server.db.dataModel.schema.fields.FormField;
 import net.batkin.forms.server.db.dataModel.schema.fields.StringField;
 import net.batkin.forms.server.exception.ServerDataException;
 
 import org.bson.BSONObject;
 
 public class StringTextWidget extends FormWidget<String> {
 
 	public StringTextWidget(BSONObject obj) throws ServerDataException {
 		super(obj);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public FieldData<String> buildFieldData(FormField<?> field) {
 
 		return new FieldData<String>((FormField<String>) field) {
 
 			@Override
 			public String convertObject(Map<String, String[]> params) {
 				String stringValue = getOneValue(params, getName());
 				if (stringValue != null) {
 					return stringValue.trim();
 				}
 				return null;
 			}
 
 			@Override
 			public void validate() throws FieldValidationException {
 				super.validate();
 
 				if (field.getRequired() && "".equals(nativeData)) {
 					throw new FieldValidationException("required", field);
 				}
 
 				if (nativeData == null) {
 					return;
 				}
 
 				String stringValue = (String) nativeData;
 				StringField stringField = (StringField) field;
 
 				Integer maxLength = stringField.getMaxLength();
 				if (maxLength != null) {
 					if (stringValue.length() > maxLength.intValue()) {
 						throw new FieldValidationException("must be less than " + maxLength + " characters", field);
 					}
 				}
 
 				Integer minLength = stringField.getMinLength();
				if (minLength != null) {
					if (!stringValue.equals("") && stringValue.length() < minLength.intValue()) {
						throw new FieldValidationException("must be at least " + minLength + " characters", field);
					}
 				}
 			}
 		};
 	}
 
 	@Override
 	public String getTemplateName() {
 		return "textfield";
 	}
 }
