 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package com.evidence.fe.annotation;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 import lombok.extern.slf4j.Slf4j;
 
 import com.evidence.fe.form.Model;
 import com.vaadin.ui.FormFieldFactory;
 
 /**
  * @author Michal Bocek
  * @since 1.0.0
  */
 @Slf4j
 public class AnnotationHelper {
 
 	private static final String NESTED_SEPARATOR = ".";
 
 	public static boolean isAutomaticForm(Model model) {
 		return model.getClass().isAnnotationPresent(AutomaticForm.class);
 	}
 
 	public static Class<? extends FormFieldFactory> getFormFieldFactory(Model model) {
 		AutomaticForm automaticForm = model.getClass().getAnnotation(AutomaticForm.class);
 		Class<? extends FormFieldFactory> formFieldFactory = automaticForm.formFieldFactory();
 		if (FormFieldFactory.class.isAssignableFrom(formFieldFactory)) {
 			return formFieldFactory;
 		} else {
 			return null;
 		}
 	}
 	
 	public static void buildData(Model model, List<FieldInfo> fieldInfos, Map<String, Double> orderMap, Map<String, String> captionMap, Map<String, Boolean> requiredMap, Map<String, Boolean> validatedMap) {
 		List<FieldInfo> fields = null;
 		try {
 			fields = getAllAutomaticFormFields(model.getClass());
 		} catch (ClassNotFoundException e) {
 			log.warn("Some problems in getting fields!", e);
 		}
		
		if (fields != null) {
			for (FieldInfo fieldInfo : fields) {
				buildOrderMapRecursively(orderMap, fieldInfo, 0L, 0.1);
				buildCaptionMapRecursively(captionMap, fieldInfo);
				buildRequiredMapRecursively(requiredMap, fieldInfo);
				buildValidatedMapRecursively(validatedMap, fieldInfo);
				buildFieldInfoRecursively(fieldInfos, fieldInfo);
			}
 		}
 	}
 
 	private static void buildFieldInfoRecursively(List<FieldInfo> fieldInfos, FieldInfo fieldInfo) {
 		if (fieldInfo != null) {
 			if (fieldInfo.getSubFieldInfo() != null) {
 				for (FieldInfo subFieldInfo : fieldInfo.getSubFieldInfo()) {
 					buildFieldInfoRecursively(fieldInfos, subFieldInfo);
 				}
 			} else {
 				fieldInfos.add(fieldInfo);
 			}
 		}
 	}
 
 	private static List<FieldInfo> getAllAutomaticFormFields(Class<? extends Model> modelClass) throws ClassNotFoundException {
 		return getAllAutomaticFormFields(modelClass, null, null);
 	}
 
 	private static void buildValidatedMapRecursively(Map<String, Boolean> validatedMap, FieldInfo fieldInfo) {
 		if (fieldInfo.getValidated() != null) {
 			if (fieldInfo.getSubFieldInfo() == null) {
 				validatedMap.put(fieldInfo.getFieldNestedName(), fieldInfo.getValidated());
 			} else {
 				for (FieldInfo subfield : fieldInfo.getSubFieldInfo()) {
 					buildRequiredMapRecursively(validatedMap, subfield);
 				}
 			}
 		} else {
 			if (fieldInfo.getSubFieldInfo() != null) {
 				for (FieldInfo subfield : fieldInfo.getSubFieldInfo()) {
 					buildRequiredMapRecursively(validatedMap, subfield);
 				}
 			}			
 		}
 	}
 
 	private static void buildRequiredMapRecursively(Map<String, Boolean> requiredMap, FieldInfo fieldInfo) {
 		if (fieldInfo.getRequired() != null) {
 			if (fieldInfo.getSubFieldInfo() == null) {
 				requiredMap.put(fieldInfo.getFieldNestedName(), fieldInfo.getRequired());
 			} else {
 				for (FieldInfo subfield : fieldInfo.getSubFieldInfo()) {
 					buildRequiredMapRecursively(requiredMap, subfield);
 				}
 			}
 		} else {
 			if (fieldInfo.getSubFieldInfo() != null) {
 				for (FieldInfo subfield : fieldInfo.getSubFieldInfo()) {
 					buildRequiredMapRecursively(requiredMap, subfield);
 				}
 			}			
 		}
 	}
 
 	private static void buildCaptionMapRecursively(Map<String, String> captionMap, FieldInfo fieldInfo) {
 		if (fieldInfo.getCaption() != null) {
 			if (fieldInfo.getSubFieldInfo() == null) {
 				captionMap.put(fieldInfo.getFieldNestedName(), fieldInfo.getCaption());
 			} else {
 				for (FieldInfo subfield : fieldInfo.getSubFieldInfo()) {
 					buildCaptionMapRecursively(captionMap, subfield);
 				}
 			}
 		} else {
 			if (fieldInfo.getSubFieldInfo() != null) {
 				for (FieldInfo subfield : fieldInfo.getSubFieldInfo()) {
 					buildCaptionMapRecursively(captionMap, subfield);
 				}
 			}			
 		}
 	}
 
 	private static void buildOrderMapRecursively(Map<String, Double> orderMap, FieldInfo fieldInfo, Long base,  double multiplier) {
 		if (fieldInfo.getOrder() != null) {
 			if (fieldInfo.getSubFieldInfo() == null) {
 				if (base == 0) {
 					orderMap.put(fieldInfo.getFieldNestedName(), Double.valueOf(fieldInfo.getOrder()));
 				} else {
 					orderMap.put(fieldInfo.getFieldNestedName(), base + Double.valueOf(fieldInfo.getOrder()) * multiplier);
 				}
 			} else {
 				for (FieldInfo subfield : fieldInfo.getSubFieldInfo()) {
 					buildOrderMapRecursively(orderMap, subfield, fieldInfo.getOrder(), multiplier / 10);
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static List<FieldInfo> getAllAutomaticFormFields(Class modelClass, FieldInfo parent, String parentFullName) throws ClassNotFoundException {
 		List<FieldInfo> allClassFieldInfos = parent != null ? parent.getSubFieldInfo() : null;
 		if (modelClass.isAnnotationPresent(AutomaticForm.class)) {
 			Field[] allClassFields = FieldUtils.getAllFields(modelClass, null);
 			if (allClassFields.length > 0) {
 				allClassFieldInfos = allClassFieldInfos == null ? new ArrayList<FieldInfo>() : allClassFieldInfos;
 				for (Field field : allClassFields) {
 					Order order = field.getAnnotation(Order.class);
 					Caption caption = field.getAnnotation(Caption.class);
 					if (order != null || caption != null) {
 						Class fieldClass = field.getType();
 						Long orderNumber = null;
 						String captionString = null;
 						Boolean requiredBoolean = Boolean.FALSE;
 						Boolean validatedBoolean = Boolean.FALSE;
 						
 						if (order != null) {
 							orderNumber = order.value();
 						}						
 						
 						if (caption != null) {
 							captionString = caption.value();
 						}						
 						
 						if (field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(NotEmpty.class)) {
 							requiredBoolean = Boolean.TRUE;
 						}						
 						
 						for (Annotation annotation : field.getAnnotations()) {
 							if (annotation.getClass().getCanonicalName() != null && annotation.getClass().getCanonicalName().contains("valid")) { 
 								validatedBoolean = Boolean.TRUE;
 							}
 						}						
 						
 						FieldInfo fieldInfo = null;
 						String fieldName = field.getName();
 						String fieldNestedName = (parentFullName !=null ) ? parentFullName + NESTED_SEPARATOR + fieldName : fieldName;
 						Boolean nestedField = (parentFullName !=null ) ? Boolean.TRUE : Boolean.FALSE;
 						if (fieldClass.isAnnotationPresent(AutomaticForm.class)) {
 							List<FieldInfo> formFields = getAllAutomaticFormFields(fieldClass, fieldInfo, fieldNestedName);
 							fieldInfo = new FieldInfo(fieldName, fieldNestedName, orderNumber, captionString, requiredBoolean, validatedBoolean, nestedField, formFields);
 						} else {
 							fieldInfo = new FieldInfo(fieldName, fieldNestedName, orderNumber, captionString, requiredBoolean, validatedBoolean, nestedField, null);
 						}
 						allClassFieldInfos.add(fieldInfo);						
 					}
 				}
 			}
 		}
 		return allClassFieldInfos;
 	}
 }
