 package infodoc.basic.field;
 
 import infodoc.basic.BasicConstants;
 import infodoc.core.container.ClassificationValueContainer;
 import infodoc.core.dto.ClassificationValue;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import enterpriseapp.ui.crud.EntitySetField;
 
 public class MultipleClassificationsField extends AbstractClassifications {
 
 	@Override
 	public EntitySetField getEntitySetField(ClassificationValueContainer classificationValueContainer) {
 		return new EntitySetField(ClassificationValue.class, classificationValueContainer) {
 			private static final long serialVersionUID = 1L;
 
 			@SuppressWarnings("unchecked")
 			@Override
 			public Object getValue() {
 				Set<ClassificationValue> value = (Set<ClassificationValue>) super.getValue();
 				
 				if(value != null && !value.isEmpty()) {
 					Set<ClassificationValue> classificationValues = new HashSet<ClassificationValue>();
 					
 					for(Object classificationValue : value) {
						classificationValues.add((ClassificationValue) classificationValue);
 					}
 					
 					value = classificationValues;
 				}
 				
 				return value;
 			}
 		};
 	}
 
 	@Override
 	public String getHelp() {
 		return BasicConstants.uiHelpMultipleClassificationsField;
 	}
 
 }
