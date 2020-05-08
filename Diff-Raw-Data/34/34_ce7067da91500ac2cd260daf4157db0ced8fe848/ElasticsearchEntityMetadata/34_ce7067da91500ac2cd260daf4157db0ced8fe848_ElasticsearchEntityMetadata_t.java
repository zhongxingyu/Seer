 package com.github.lbroudoux.roo.addon.layers.repository.elasticsearch;
 
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.lang3.Validate;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
 import org.springframework.roo.classpath.PhysicalTypeMetadata;
 import org.springframework.roo.classpath.details.BeanInfoUtils;
 import org.springframework.roo.classpath.details.FieldMetadata;
 import org.springframework.roo.classpath.details.FieldMetadataBuilder;
 import org.springframework.roo.classpath.details.MethodMetadata;
 import org.springframework.roo.classpath.details.MethodMetadataBuilder;
 import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
 import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
 import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
 import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
 import org.springframework.roo.metadata.MetadataIdentificationUtils;
 import org.springframework.roo.model.JavaSymbolName;
 import org.springframework.roo.model.JavaType;
 import org.springframework.roo.model.SpringJavaType;
 import org.springframework.roo.project.LogicalPath;
 
 /**
  * Creates metadata for domain entity ITDs (annotated with {@link RooElasticsearchEntity}.
  * @author Laurent Broudoux
  * @since 1.2
  */
 public class ElasticsearchEntityMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {
 
     // Constants
     private static final String PROVIDES_TYPE_STRING = ElasticsearchEntityMetadata.class.getName();
     private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);
 
     private FieldMetadata idField;
     
     public static final String getMetadataIdentiferType() {
         return PROVIDES_TYPE;
     }
     
     public static final String createIdentifier(JavaType javaType, LogicalPath path) {
         return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, javaType, path);
     }
 
     public static final JavaType getJavaType(String metadataIdentificationString) {
         return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING, metadataIdentificationString);
     }
 
     public static final LogicalPath getPath(String metadataIdentificationString) {
         return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, metadataIdentificationString);
     }
 
     public static boolean isValid(String metadataIdentificationString) {
         return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, metadataIdentificationString);
     }
     
     public ElasticsearchEntityMetadata(String identifier, JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata) {
         super(identifier, aspectName, governorPhysicalTypeMetadata);
         
         Validate.isTrue(isValid(identifier), "Metadata identification string '" + identifier + "' does not appear to be a valid");
         
         if (!isValid()){
             return;
         }
 
        // Add spring data Persistent annotation.
         builder.addAnnotation(getTypeAnnotation(SpringJavaType.PERSISTENT));
        // Add spring data elasticsearch Document annotation.
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(new JavaType("org.springframework.data.elasticsearch.annotations.Document"));
        annotationBuilder.addStringAttribute("indexName", getJavaType(identifier).getSimpleTypeName().toLowerCase() + "s");
        annotationBuilder.addStringAttribute("type", getJavaType(identifier).getSimpleTypeName().toLowerCase());
        builder.addAnnotation(annotationBuilder.build());
 
         idField = getIdentifierField();
         if (idField != null) {
             builder.addField(idField);
             builder.addMethod(getIdentifierAccessor(idField));
             builder.addMethod(getIdentifierMutator(idField));
         }
 
         // Build the ITD
         itdTypeDetails = builder.build();
     }
     
     /**
      * Create metadata for an identifier field definition. 
      * @return a FieldMetadata object
      */
     private FieldMetadata getIdentifierField() {
        
        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(getId(), 
              Modifier.PRIVATE, new JavaSymbolName("id"), JavaType.STRING, null);
        fieldBuilder.addAnnotation(new AnnotationMetadataBuilder(
                SpringJavaType.DATA_ID));
        return fieldBuilder.build();
     }
     
     private MethodMetadataBuilder getIdentifierAccessor(FieldMetadata idField){
        // Specify the desired method name
        JavaSymbolName requiredAccessorName = BeanInfoUtils.getAccessorMethodName(idField);
         
        // We declared the field in this ITD, so produce a public accessor for it.
        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return this." + idField.getFieldName().getSymbolName() + ";");
 
        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC, 
              requiredAccessorName, idField.getFieldType(), bodyBuilder);
     }
     
     private MethodMetadataBuilder getIdentifierMutator(FieldMetadata idField){
        // Specify the desired method name
        JavaSymbolName requiredMutatorName = BeanInfoUtils.getMutatorMethodName(idField);
        
        final List<JavaType> parameterTypes = Arrays.asList(idField.getFieldType());
        final List<JavaSymbolName> parameterNames = Arrays.asList(new JavaSymbolName("id"));
        
        // We declared the field in this ITD, so produce a public mutator for it.
        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("this." + idField.getFieldName().getSymbolName() + " = id;");
        
        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC, requiredMutatorName, JavaType.VOID_PRIMITIVE,
              AnnotatedJavaType.convertFromJavaTypes(parameterTypes), parameterNames, bodyBuilder);
     }
         
     private MethodMetadata methodExists(JavaSymbolName methodName, List<AnnotatedJavaType> paramTypes) {
         // We have no access to method parameter information, so we scan by name alone and treat any match as authoritative
         // We do not scan the superclass, as the caller is expected to know we'll only scan the current class
         for (MethodMetadata method : governorTypeDetails.getDeclaredMethods()) {
             if (method.getMethodName().equals(methodName) && method.getParameterTypes().equals(paramTypes)) {
                 // Found a method of the expected name; we won't check method parameters though
                 return method;
             }
         }
         return null;
     }
     
     // Typically, no changes are required beyond this point
     
     public String toString() {
         final ToStringBuilder builder = new ToStringBuilder(this);
         builder.append("identifier", getId());
         builder.append("valid", valid);
         builder.append("aspectName", aspectName);
         builder.append("destinationType", destination);
         builder.append("governor", governorPhysicalTypeMetadata.getId());
         builder.append("itdTypeDetails", itdTypeDetails);
         return builder.toString();
     }
 }
