 package net.milanaleksic.guitransformer;
 
 import com.google.common.base.*;
 import com.google.common.collect.Lists;
 import net.milanaleksic.guitransformer.model.*;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.*;
 
 import java.lang.reflect.*;
 import java.util.*;
 import java.util.List;
 
 class EmbeddingService {
 
     private MethodEventListenerExceptionHandler methodEventListenerExceptionHandler;
 
     public void setMethodEventListenerExceptionHandler(MethodEventListenerExceptionHandler methodEventListenerExceptionHandler) {
         this.methodEventListenerExceptionHandler = methodEventListenerExceptionHandler;
     }
 
     void embed(Object formObject, TransformationWorkingContext transformationContext) throws TransformerException {
         embedComponents(formObject, transformationContext);
         embedEventListenersAsFields(formObject, transformationContext);
         embedEventListenersAsMethods(formObject, transformationContext);
         embedModels(formObject, transformationContext);
     }
 
     private interface OperationOnField {
         void operate(Field field) throws ReflectiveOperationException, TransformerException;
     }
 
     private void allowOperationOnField(Field field, OperationOnField operation) throws TransformerException {
         boolean wasPublic = Modifier.isPublic(field.getModifiers());
         if (!wasPublic)
             field.setAccessible(true);
         try {
             operation.operate(field);
         } catch (TransformerException e) {
             throw e;
         } catch (Exception e) {
             throw new TransformerException("Error while operating on field named " + field.getName(), e);
         } finally {
             if (!wasPublic)
                 field.setAccessible(false);
         }
     }
 
     private void embedComponents(final Object targetObject, TransformationWorkingContext transformationContext) throws TransformerException {
         Field[] fields = targetObject.getClass().getDeclaredFields();
         for (Field field : fields) {
             EmbeddedComponent annotation = field.getAnnotation(EmbeddedComponent.class);
             if (annotation == null)
                 continue;
             String name = annotation.name();
             if (name.isEmpty())
                 name = field.getName();
             final Object mappedObject = transformationContext.getMappedObject(name);
             if (mappedObject == null)
                 throw new IllegalStateException("Field marked as embedded could not be found: " + targetObject.getClass().getName() + "." + field.getName());
             allowOperationOnField(field, new OperationOnField() {
                 @Override
                 public void operate(Field field) throws ReflectiveOperationException {
                     field.set(targetObject, mappedObject);
                 }
             });
         }
     }
 
     private void embedEventListenersAsFields(final Object targetObject, TransformationWorkingContext transformationContext) throws TransformerException {
         Field[] fields = targetObject.getClass().getDeclaredFields();
         for (Field field : fields) {
             List<EmbeddedEventListener> allListeners = Lists.newArrayList();
             EmbeddedEventListeners annotations = field.getAnnotation(EmbeddedEventListeners.class);
             if (annotations != null)
                 allListeners.addAll(Arrays.asList(annotations.value()));
             else {
                 EmbeddedEventListener annotation = field.getAnnotation(EmbeddedEventListener.class);
                 if (annotation != null)
                     allListeners.add(annotation);
             }
             for (final EmbeddedEventListener listenerAnnotation : allListeners) {
                 String componentName = listenerAnnotation.component();
                 final Object mappedObject = componentName.isEmpty()
                         ? Optional.of(transformationContext.getWorkItem())
                         : transformationContext.getMappedObject(componentName);
                 if (mappedObject == null)
                     throw new IllegalStateException("Event source could not be found in the GUI definition: " + targetObject.getClass().getName() + "." + field.getName());
                 allowOperationOnField(field, new OperationOnField() {
                     @Override
                     public void operate(Field field) throws ReflectiveOperationException {
                         ((Widget) mappedObject).addListener(listenerAnnotation.event(), (Listener) field.get(targetObject));
                     }
                 });
             }
         }
     }
 
     private void embedEventListenersAsMethods(Object targetObject, TransformationWorkingContext transformationContext) throws TransformerException {
         Method[] methods = targetObject.getClass().getDeclaredMethods();
         for (Method method : methods) {
             List<EmbeddedEventListener> allListeners = Lists.newArrayList();
             EmbeddedEventListeners annotations = method.getAnnotation(EmbeddedEventListeners.class);
             if (annotations != null)
                 allListeners.addAll(Arrays.asList(annotations.value()));
             else {
                 EmbeddedEventListener annotation = method.getAnnotation(EmbeddedEventListener.class);
                 if (annotation != null)
                     allListeners.add(annotation);
             }
             for (EmbeddedEventListener listenerAnnotation : allListeners) {
                 String componentName = listenerAnnotation.component();
                 Object mappedObject = componentName.isEmpty()
                         ? transformationContext.getWorkItem()
                         : transformationContext.getMappedObject(componentName);
                 if (mappedObject == null)
                     throw new IllegalStateException("Event source could not be found in the GUI definition: " + targetObject.getClass().getName() + "." + method.getName());
                 if (!void.class.equals(method.getReturnType()))
                     throw new IllegalStateException("Method event listeners must be with void return type " + targetObject.getClass().getName() + "." + method.getName());
                 final Class<?>[] parameterTypes = method.getParameterTypes();
                 if (parameterTypes.length > 0) {
                     if (parameterTypes.length != 1 || !Event.class.isAssignableFrom(parameterTypes[0]))
                         throw new IllegalStateException("Method event listeners must have exactly one parameter, of type org.eclipse.swt.widgets.Event: " + targetObject.getClass().getName() + "." + method.getName());
                 }
                 handleSingleEventToMethodListenerDelegation(transformationContext, targetObject, method, listenerAnnotation.event(), (Widget) mappedObject);
             }
         }
     }
 
     private void handleSingleEventToMethodListenerDelegation(final TransformationWorkingContext transformationContext, final Object targetObject, final Method method, int event, Widget mappedObject) {
         mappedObject.addListener(event, new Listener() {
             @Override
             public void handleEvent(Event event) {
                 final boolean wasPublic = Modifier.isPublic(method.getModifiers());
                 try {
                     if (!wasPublic)
                         method.setAccessible(true);
                     if (method.getParameterTypes().length > 0)
                         method.invoke(targetObject, event);
                     else
                         method.invoke(targetObject);
                 } catch (Exception e) {
                     if (methodEventListenerExceptionHandler != null)
                        methodEventListenerExceptionHandler.handleException(transformationContext.getWorkItem(), e);
                     else
                         throw new RuntimeException("Transformer event delegation got an exception: " + e.getMessage(), e);
                 } finally {
                     if (!wasPublic)
                         method.setAccessible(false);
                 }
             }
         });
     }
 
     private void embedModels(final Object formObject, final TransformationWorkingContext transformationContext) throws TransformerException {
         Field[] fields = formObject.getClass().getDeclaredFields();
         for (Field field : fields) {
             TransformerModel annotation = field.getAnnotation(TransformerModel.class);
             if (annotation == null)
                 continue;
             allowOperationOnField(field, new OperationOnField() {
                 @Override
                 public void operate(Field field) throws ReflectiveOperationException, TransformerException {
                     Object model = field.getType().newInstance();
                     bindModel(model, transformationContext);
                     field.set(formObject, model);
                 }
             });
         }
     }
 
     private void bindModel(Object model, TransformationWorkingContext transformationContext) throws TransformerException {
         transformationContext.putModelBinding(model, createBindingMetaData(model, transformationContext));
         try {
             mapOnChangeListeners(model, transformationContext);
             updateModelFromForm(model, transformationContext);
         } catch (ReflectiveOperationException e) {
             throw new TransformerException("Reflection problem while binding model", e);
         }
     }
 
     private void mapOnChangeListeners(final Object model, final TransformationWorkingContext transformationContext) throws ReflectiveOperationException {
         final ModelBindingMetaData modelBindingMetaData = transformationContext.getModelBinding(model);
         for (Map.Entry<Field, FieldMapping> mapping : modelBindingMetaData.getFieldMapping().entrySet()) {
             final Field field = mapping.getKey();
             final Object component = mapping.getValue().getComponent();
             Method addListener = component.getClass().getMethod("addListener", new Class[]{int.class, Listener.class});
 
             addListener.invoke(component, SWT.Modify, new Listener() {
                 @Override
                 public void handleEvent(Event event) {
                     if (modelBindingMetaData.isFormIsBeingUpdatedFromModelRightNow())
                         return;
                     setModelFieldValue(model, field, component, modelBindingMetaData, transformationContext);
                 }
             });
         }
     }
 
     private void setModelFieldValue(final Object model, Field field, final Object component, final ModelBindingMetaData modelBindingMetaData, final TransformationWorkingContext transformationContext) {
         try {
             allowOperationOnField(field, new OperationOnField() {
                 @Override
                 public void operate(Field field) throws ReflectiveOperationException, TransformerException {
                     FieldMapping fieldMapping = modelBindingMetaData.getFieldMapping().get(field);
                     final Method getterMethod = fieldMapping.getGetterMethod();
                     if (fieldMapping.getBindingType().equals(FieldMapping.BindingType.BY_REFERENCE))
                         field.set(model, getterMethod.invoke(component));
                     else
                         field.set(model, convertFromComponentToModelValue((String) getterMethod.invoke(component), field.getType()));
                 }
             });
         } catch (Exception e) {
             handleException(e, transformationContext);
         }
     }
 
     private void handleException(Exception e, TransformationWorkingContext transformationContext) {
         if (methodEventListenerExceptionHandler != null) {
             Object workItem = transformationContext.getWorkItem();
             Shell parentShell = (workItem != null && workItem instanceof Shell) ? (Shell) workItem : null;
             methodEventListenerExceptionHandler.handleException(parentShell, e);
         } else
             throw new RuntimeException("Transformer event delegation got an exception: " + e.getMessage(), e);
     }
 
     private ModelBindingMetaData createBindingMetaData(Object model, TransformationWorkingContext transformationContext) throws TransformerException {
         ModelBindingMetaData bindingData = new ModelBindingMetaData();
         Field[] fields = model.getClass().getDeclaredFields();
         for (Field field : fields) {
             TransformerProperty annotation = field.getAnnotation(TransformerProperty.class);
             String propertyNameSentenceCase = getPropertyNameSentenceCaseForModelField(annotation);
             String name = annotation == null ? null : annotation.component();
             if (Strings.isNullOrEmpty(name))
                 name = field.getName();
             try {
                 createSingleBindingMetaData(model, transformationContext, bindingData, field, propertyNameSentenceCase, name);
             } catch (TransformerException e) {
                 throw e;
             } catch (Exception e) {
                 throw new TransformerException("Error while creating binding metadata for component field named " + name, e);
             }
         }
         return bindingData;
     }
 
     private void createSingleBindingMetaData(Object model, TransformationWorkingContext transformationContext, ModelBindingMetaData bindingData, Field field, String propertyNameSentenceCase, String name) throws TransformerException, NoSuchMethodException {
         FieldMapping.FieldMappingBuilder builder = FieldMapping.builder();
         Object mappedObject = transformationContext.getMappedObject(name);
         if (mappedObject == null)
             throw new IllegalStateException("Field could not be found in form: " + model.getClass().getName() + "." + name);
         builder.setComponent(mappedObject);
 
         Method getterMethod = mappedObject.getClass().getMethod("get" + propertyNameSentenceCase, new Class[0]);
         if (field.getType().isAssignableFrom(getterMethod.getReturnType()))
             builder.setBindingType(FieldMapping.BindingType.BY_REFERENCE);
         else
             builder.setBindingType(FieldMapping.BindingType.CONVERSION);
 
         builder.setGetterMethod(getterMethod);
         try {
             builder.setSetterMethod(mappedObject.getClass().getMethod("set" + propertyNameSentenceCase, new Class[]{field.getType()}));
         } catch (NoSuchMethodException e) {
             builder.setSetterMethod(mappedObject.getClass().getMethod("set" + propertyNameSentenceCase, new Class[]{String.class}));
         }
         bindingData.getFieldMapping().put(field, builder.build());
     }
 
     private String getPropertyNameSentenceCaseForModelField(TransformerProperty annotation) {
         String propertyName = getPropertyNameForModelField(annotation);
         Preconditions.checkArgument(!Strings.isNullOrEmpty(propertyName));
         return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
     }
 
     private String getPropertyNameForModelField(TransformerProperty annotation) {
         if (annotation == null)
             return TransformerProperty.DEFAULT_PROPERTY_NAME;
         return annotation.value();
     }
 
     private void updateModelFromForm(Object model, TransformationWorkingContext transformationContext) throws ReflectiveOperationException, TransformerException {
         ModelBindingMetaData modelBindingMetaData = transformationContext.getModelBinding(model);
         for (Map.Entry<Field, FieldMapping> binding : modelBindingMetaData.getFieldMapping().entrySet()) {
             Field field = binding.getKey();
             Object component = binding.getValue().getComponent();
             setModelFieldValue(model, field, component, modelBindingMetaData, transformationContext);
         }
     }
 
     private Object convertFromComponentToModelValue(String value, Class<?> targetClass) throws ReflectiveOperationException, TransformerException {
         if (String.class.isAssignableFrom(targetClass))
             return value;
         else if (Long.class.isAssignableFrom(targetClass) || long.class.isAssignableFrom(targetClass))
             return Long.parseLong(value, 10);
         else if (Integer.class.isAssignableFrom(targetClass) || int.class.isAssignableFrom(targetClass))
             return Integer.parseInt(value, 10);
         throw new TransformerException("Value transformation to model class " + targetClass + " not supported");
     }
 
     public void updateFormFromModel(final Object model, TransformationContext transformationContext) {
         ModelBindingMetaData modelBindingMetaData = transformationContext.getModelBindingFor(model);
         try {
             modelBindingMetaData.setFormIsBeingUpdatedFromModelRightNow(true);
             for (Map.Entry<Field, FieldMapping> binding : modelBindingMetaData.getFieldMapping().entrySet()) {
                 final FieldMapping fieldMapping = binding.getValue();
                 final Object component = fieldMapping.getComponent();
                 allowOperationOnField(binding.getKey(), new OperationOnField() {
                     @Override
                     public void operate(Field field) throws ReflectiveOperationException, TransformerException {
                         Object modelValue = field.get(model);
                         if (fieldMapping.getBindingType().equals(FieldMapping.BindingType.BY_REFERENCE))
                             fieldMapping.getSetterMethod().invoke(component, modelValue);
                         else
                             fieldMapping.getSetterMethod().invoke(component, modelValue.toString());
                     }
                 });
             }
         } catch (TransformerException e) {
             throw new IllegalStateException("Unexpected error occurred when rebinding model", e);
         } finally {
             modelBindingMetaData.setFormIsBeingUpdatedFromModelRightNow(false);
         }
     }
 
 }
