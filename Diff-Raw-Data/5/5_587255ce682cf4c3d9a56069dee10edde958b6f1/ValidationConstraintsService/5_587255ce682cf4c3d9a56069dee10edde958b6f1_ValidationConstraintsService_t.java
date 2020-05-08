 package net.vivin.regula.validation.service;
 
 import net.vivin.regula.validation.constraint.ConstraintDefinition;
 import net.vivin.regula.validation.constraint.ConstraintInstance;
 import net.vivin.regula.validation.constraint.ConstraintParameter;
 
 import org.apache.commons.lang.StringUtils;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.type.ClassMetadata;
 import org.springframework.stereotype.Service;
 
 import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
 
 import javax.validation.groups.Default;
 import javax.validation.metadata.BeanDescriptor;
 import javax.validation.metadata.ConstraintDescriptor;
 import javax.validation.metadata.PropertyDescriptor;
 
 import java.lang.annotation.Annotation;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 @Service
 public class ValidationConstraintsService {
 
     @Autowired
     private LocalValidatorFactoryBean validator;
 
     private Map<Class, Map<String, Set<ConstraintInstance>>> classToPropertyToConstraintInstancesMap = new LinkedHashMap<Class, Map<String, Set<ConstraintInstance>>>();
    private Map<Class, Map<String, ConstraintDefinition>> classToCompoundConstraintDefinitionMap = new HashMap<Class, Map<String, ConstraintDefinition>>();
 
     private String getFriendlyNameForProperty(String propertyName) {
         String[] parts = StringUtils.splitByCharacterTypeCamelCase(propertyName);
 
         for(int i = 0; i < parts.length ; i++) {
             parts[i] = parts[i].toLowerCase().replaceAll("\\.", " ");
         }
 
         parts[0] = StringUtils.capitalize(parts[0]);
 
         return StringUtils.join(parts, " ").replaceAll("\\s+", " ");
     }
 
      /*
     The following method uses a recursive algorithm that inspects a class and identifies any annotations
     (and their parameters) that are attached to a member field. If any field is annotated with a @Valid
     annotation, the method figures out the type of that field (which should be a complex type) and performs
     the same inspection on the class that represents that type.
 
     The clazz parameter is the current class we're working on i.e., the class who's validation constraints we want
     The second parameter holds the prefix. This is used to build a path. So that can get something like "object.fieldName"
     The third parameter (propertyConstraintInstancesMap) is a map that is keyed by property. The value is a set of
     ConstraintInstance objects.
     The last parameter maintains a set of compound-constraint definitions that we encounter in this class.
      */
 
     private void _getConstraints(Class clazz, String prefix, Map<String, Set<ConstraintInstance>> propertyToConstraintInstancesMap, Map<String, ConstraintDefinition> compoundConstraintDefinitionMap) {
         BeanDescriptor classDescriptor = validator.getConstraintsForClass(clazz);
 
         for(PropertyDescriptor propertyDescriptor : classDescriptor.getConstrainedProperties()) {
 
             Set<ConstraintInstance> constraintInstances = new HashSet<ConstraintInstance>();
 
             String propertyName;
 
             if(StringUtils.isEmpty(prefix)) {
                 propertyName = propertyDescriptor.getPropertyName();
             }
 
             else {
                 propertyName = prefix + "." + propertyDescriptor.getPropertyName();
             }
 
 
             String friendlyName = getFriendlyNameForProperty(propertyName);
 
             if(propertyDescriptor.isCascaded()) {
                 String newPrefix;
 
                 if(StringUtils.isEmpty(prefix)) {
                     newPrefix = StringUtils.uncapitalize(propertyDescriptor.getPropertyName());
                 }
 
                 else {
                     newPrefix = prefix + "." + propertyDescriptor.getPropertyName();
                 }
 
                 _getConstraints(propertyDescriptor.getElementClass(), newPrefix, propertyToConstraintInstancesMap, compoundConstraintDefinitionMap);
             }
 
             else {
                 for(ConstraintDescriptor<? extends Annotation> constraintDescriptor : propertyDescriptor.getConstraintDescriptors()) {
 
                     ConstraintInstance validationConstraintInstance = createConstraintFromDescriptor(constraintDescriptor);
 
 
                     if(constraintDescriptor.getComposingConstraints().size() > 0) {
                         handleComposingConstraints(validationConstraintInstance, constraintDescriptor, compoundConstraintDefinitionMap);
                     }
 
                     validationConstraintInstance.addParameter(new ConstraintParameter("label", friendlyName, "String"));
                     constraintInstances.add(validationConstraintInstance);
                 }
             }
 
             if(constraintInstances.size() > 0) {
                 propertyToConstraintInstancesMap.put(propertyName, constraintInstances);
             }
         }
     }
 
     /* this method uses an algorithm similar to the one above to get class-level constraints (i.e., validation constraints attached to a bean */
     private void getClassLevelConstraints(Class clazz, Map<String, Set<ConstraintInstance>> propertyToConstraintInstancesMap, Map<String, ConstraintDefinition> compoundConstraintDefinitionMap) {
         String friendlyName = StringUtils.uncapitalize(clazz.getSimpleName());
         Set<ConstraintInstance> constraintInstances = new HashSet<ConstraintInstance>();
 
         BeanDescriptor classDescriptor = validator.getConstraintsForClass(clazz);
         Set<ConstraintDescriptor<?>> classLevelConstraintDescriptors = classDescriptor.getConstraintDescriptors();
 
         for(ConstraintDescriptor<?> constraintDescriptor : classLevelConstraintDescriptors) {
 
             ConstraintInstance validationConstraintInstance = createConstraintFromDescriptor(constraintDescriptor);
 
             if(constraintDescriptor.getComposingConstraints().size() > 0) {
                 handleComposingConstraints(validationConstraintInstance, constraintDescriptor, compoundConstraintDefinitionMap);
             }
 
             validationConstraintInstance.addParameter(new ConstraintParameter("label", friendlyName, "String"));
             validationConstraintInstance.setClassLevelConstraint(true);
 
             constraintInstances.add(validationConstraintInstance);
         }
 
         if(constraintInstances.size() > 0) {
             propertyToConstraintInstancesMap.put(friendlyName, constraintInstances);           
         }
 
     }
 
     /* this method creates a constraint instance from a ConstraintDescriptor object */
     private ConstraintInstance createConstraintFromDescriptor(ConstraintDescriptor<? extends Annotation> constraintDescriptor) {
 
         Annotation annotation = constraintDescriptor.getAnnotation();
         ConstraintInstance validationConstraintInstance = new ConstraintInstance(annotation.annotationType().getSimpleName());
         Map<String, Object> attributes = new LinkedHashMap<String, Object>(constraintDescriptor.getAttributes());
 
         for(Map.Entry<String, Object> entry : attributes.entrySet()) {
             if(!"payload".equals(entry.getKey())) {
                 validationConstraintInstance.addParameter(createConstraintParameter(annotation, entry));
             }
         }
 
         return validationConstraintInstance;
     }
 
     /* This method recursively figures out the composing constraints of a constraint, and adds it to the compound-constraint definition set  */
     private void handleComposingConstraints(ConstraintInstance parentConstraintInstance, ConstraintDescriptor<? extends Annotation> parentDescriptor, Map<String, ConstraintDefinition> compoundConstraintDefinitionMap) {
 
         ConstraintDefinition parentConstraintDefinition = new ConstraintDefinition(parentConstraintInstance);
 
         /* definitely not the way I would like to do it, but there seems to be no way other way to identify hibernate or java
            validation-constraints that are compound-constraints themselves
          */
         if(parentDescriptor.getAnnotation().annotationType().getName().indexOf("javax.validation") < 0 &&
             parentDescriptor.getAnnotation().annotationType().getName().indexOf("org.hibernate") < 0) {
 
             parentConstraintDefinition.setReportAsSingleViolation(parentDescriptor.isReportAsSingleViolation());
 
             for(ConstraintDescriptor<? extends Annotation> composingDescriptor : parentDescriptor.getComposingConstraints()) {
 
                 ConstraintInstance composingConstraintInstance = createConstraintFromDescriptor(composingDescriptor);
 
                 if(composingDescriptor.getComposingConstraints().size() > 0) {
                     handleComposingConstraints(composingConstraintInstance, composingDescriptor, compoundConstraintDefinitionMap);
                 }
 
                 parentConstraintDefinition.addComposingConstraint(composingConstraintInstance);
             }
 
             if(compoundConstraintDefinitionMap.get(parentConstraintDefinition.getName()) != null) {
                 ConstraintDefinition existing = compoundConstraintDefinitionMap.get(parentConstraintDefinition.getName());
 
                 for(ConstraintInstance composing : existing.getComposingConstraints()) {
                     parentConstraintDefinition.addComposingConstraint(composing);
                 }
             }           
 
             compoundConstraintDefinitionMap.put(parentConstraintDefinition.getName(), parentConstraintDefinition);
         }
     }
 
     //There is no straightforward way to translate some parameter values into a string. This is especially true in the case of the @Pattern annotation where
     //the flags parameter is of type Pattern.Flag[] and the value is a array of Pattern.Flag[] objects (enums actually). So here, we inspect the parameters
     //and their values to perform specific translations if needed
     private ConstraintParameter createConstraintParameter(Annotation annotation, Map.Entry<String, Object> entry) {
         String parameterName = entry.getKey();
         Object parameterValue = entry.getValue();
         String parameterType = parameterValue.getClass().getSimpleName();
 
         String annotationName = annotation.annotationType().getSimpleName();
 
         if("Pattern".equals(annotationName) && "flags".equals(parameterName)) {
 
             String flags = "";
 
             for(javax.validation.constraints.Pattern.Flag flag : (javax.validation.constraints.Pattern.Flag[]) parameterValue) {
                 //The only flags that make sense in Javascript are i, m, and g. i and m map to CASE_INSENSITIVE
                 //and MULTILINE. g doesn't map to a flag enum in the Java world, and the remaining Java enums
                 //don't map to anything in the Javascript world
                 switch(flag) {
                     case CASE_INSENSITIVE:
                         flags += "i";
                         break;
 
                     case MULTILINE:
                         flags += "m";
                         break;
                 }
             }
 
             parameterValue = flags;
             parameterType = "String";
 
         }
 
         else if("message".equals(parameterName)) {
 
             if(parameterValue.toString().startsWith("{") && parameterValue.toString().endsWith("}")) {
                 parameterValue = parameterValue.toString().replace("{", "").replace("}", "");
             }
         }
 
         else if("groups".equals(parameterName)) {
             String groups = "";
 
             for(Class clazz : (Class[]) parameterValue) {
                 groups += clazz.getSimpleName() + ",";
             }
 
             Pattern pattern = Pattern.compile(",$", Pattern.DOTALL);
             Matcher matcher = pattern.matcher(groups);
             groups = matcher.replaceFirst("");
 
             if(StringUtils.isEmpty(groups)) {
                 groups = Default.class.getSimpleName();
             }
 
             parameterValue = "[" + groups + "]";
             parameterType = "Array";
         }
 
         return new ConstraintParameter(parameterName, parameterValue.toString(), parameterType);
     }
 
     public ClassConstraintInformation getValidationConstraints(Class clazz) {
         Map<String, Set<ConstraintInstance>> propertyToConstraintInstancesMap = classToPropertyToConstraintInstancesMap.get(clazz);
        Map<String, ConstraintDefinition> compoundConstraintDefinitionMap = classToCompoundConstraintDefinitionMap.get(clazz);
 
         if(propertyToConstraintInstancesMap == null) {
             propertyToConstraintInstancesMap = new LinkedHashMap<String, Set<ConstraintInstance>>();
             compoundConstraintDefinitionMap = new LinkedHashMap<String, ConstraintDefinition>();
 
             _getConstraints(clazz, "", propertyToConstraintInstancesMap, compoundConstraintDefinitionMap);
             getClassLevelConstraints(clazz, propertyToConstraintInstancesMap, compoundConstraintDefinitionMap);
 
             classToPropertyToConstraintInstancesMap.put(clazz, propertyToConstraintInstancesMap);
         }
 
         if(compoundConstraintDefinitionMap == null) {
             compoundConstraintDefinitionMap = new LinkedHashMap<String, ConstraintDefinition>();
         }
 
         return new ClassConstraintInformation(
                 new HashMap<String, Set<ConstraintInstance>>(propertyToConstraintInstancesMap),
                 new HashSet<ConstraintDefinition>(compoundConstraintDefinitionMap.values())
         );
     }
 }
