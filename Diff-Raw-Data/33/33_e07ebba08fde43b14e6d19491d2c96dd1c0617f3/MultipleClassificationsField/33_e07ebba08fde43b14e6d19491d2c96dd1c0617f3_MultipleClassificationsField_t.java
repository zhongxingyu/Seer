 package infodoc.basic.field;
 
 import infodoc.basic.BasicConstants;
 import infodoc.core.container.ClassificationValueContainer;
import infodoc.core.container.InfodocContainerFactory;
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
						
						if(classificationValue instanceof String) {
							classificationValues.add(getClassificationValue(value.toString()));
						} else {
							classificationValues.add((ClassificationValue) classificationValue);
						}
 					}
 					
 					value = classificationValues;
 				}
 				
 				return value;
 			}
			
			private ClassificationValue getClassificationValue(String name) {
				ClassificationValueContainer classificationValueContainer = InfodocContainerFactory.getClassificationValueContainer();
				ClassificationValue classificationValue = classificationValueContainer.getByNameAndClassificationId(name, classificationId);
				
				if(classificationValue == null) {
					classificationValue = new ClassificationValue();
					classificationValue.setName(name);
					classificationValue.setClassification(InfodocContainerFactory.getClassificationContainer().getEntity(classificationId));
					classificationValue.setId(classificationValueContainer.saveEntity(classificationValue));
					classificationValue = classificationValueContainer.getEntity(classificationValue.getId());
				}
				
				return classificationValue;
			}
 		};
 	}
 
 	@Override
 	public String getHelp() {
 		return BasicConstants.uiHelpMultipleClassificationsField;
 	}
 
 }
