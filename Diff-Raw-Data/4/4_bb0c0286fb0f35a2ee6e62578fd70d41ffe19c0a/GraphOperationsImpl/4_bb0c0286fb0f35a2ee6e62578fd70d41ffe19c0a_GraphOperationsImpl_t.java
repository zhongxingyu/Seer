 /*
  * Copyright 2012 jccastrejon
  *  
  * This file is part of Model2Roo.
  *
  * Model2Roo is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * Model2Roo is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with Model2Roo.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.imag.model2roo.addon.graph;
 
 import static org.springframework.roo.model.JavaType.OBJECT;
 import static org.springframework.roo.model.RooJavaType.ROO_JAVA_BEAN;
 import static org.springframework.roo.model.RooJavaType.ROO_TO_STRING;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
import java.util.Locale;
 import java.util.Set;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.jvnet.inflector.Noun;
 import org.springframework.roo.addon.propfiles.PropFileOperations;
 import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
 import org.springframework.roo.classpath.PhysicalTypeCategory;
 import org.springframework.roo.classpath.PhysicalTypeIdentifier;
 import org.springframework.roo.classpath.TypeLocationService;
 import org.springframework.roo.classpath.TypeManagementService;
 import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
 import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
 import org.springframework.roo.classpath.details.FieldMetadataBuilder;
 import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
 import org.springframework.roo.file.monitor.event.FileDetails;
 import org.springframework.roo.model.DataType;
 import org.springframework.roo.model.JavaSymbolName;
 import org.springframework.roo.model.JavaType;
 import org.springframework.roo.process.manager.FileManager;
 import org.springframework.roo.project.Dependency;
 import org.springframework.roo.project.LogicalPath;
 import org.springframework.roo.project.Path;
 import org.springframework.roo.project.PathResolver;
 import org.springframework.roo.project.ProjectOperations;
 import org.springframework.roo.support.util.FileUtils;
 import org.springframework.roo.support.util.XmlUtils;
 import org.w3c.dom.Element;
 
 /**
  * Implementation of the operations defined in the graph add-on.
  * 
  * @author jccastrejon
  * 
  */
 @Component
 @Service
 public class GraphOperationsImpl implements GraphOperations {
 
     /**
      * Annotation that defines Roo java beans.
      */
     private static final AnnotationMetadataBuilder ROO_JAVA_BEAN_BUILDER = new AnnotationMetadataBuilder(ROO_JAVA_BEAN);
 
     /**
      * Annotation that defines Roo string builders.
      */
     private static final AnnotationMetadataBuilder ROO_TO_STRING_BUILDER = new AnnotationMetadataBuilder(ROO_TO_STRING);
 
     /**
      * Roo File manager.
      */
     @Reference
     private FileManager fileManager;
 
     /**
      * Roo path resolver.
      */
     @Reference
     private PathResolver pathResolver;
 
     /**
      * Roo project operations.
      */
     @Reference
     private ProjectOperations projectOperations;
 
     /**
      * Roo type management service.
      */
     @Reference
     TypeManagementService typeManagementService;
 
     /**
      * Roo type location service.
      */
     @Reference
     private TypeLocationService typeLocationService;
 
     @Reference
     private PropFileOperations propFileOperations;
 
     @Reference
     private MenuOperations menuOperations;
 
     @Override
     public boolean isGraphSetupAvailable() {
         return (!fileManager.exists(this.getContextPath()));
     }
 
     @Override
     public void graphSetup(final GraphProvider graphProvider, final String dataStoreLocation) {
         this.addDependencies(graphProvider);
         this.addConfiguration(graphProvider, dataStoreLocation);
     }
 
     @Override
     public boolean isNewEntityAvailable() {
         return (fileManager.exists(this.getContextPath()));
     }
 
     @Override
     public void newEntity(final JavaType name, final JavaType superClass, final boolean isAbstract) {
         int modifier;
         String entityId;
         GraphProvider graphProvider;
         FieldMetadataBuilder fieldBuilder;
         ClassOrInterfaceTypeDetails entityDetails;
         ClassOrInterfaceTypeDetails superClassDetails;
         ClassOrInterfaceTypeDetailsBuilder entityBuilder;
 
         // Class modifier
         modifier = Modifier.PUBLIC;
         if (isAbstract) {
             modifier = Modifier.ABSTRACT;
         }
 
         // Create entity class
         entityId = PhysicalTypeIdentifier.createIdentifier(name, pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
         entityBuilder = new ClassOrInterfaceTypeDetailsBuilder(entityId, modifier, name, PhysicalTypeCategory.CLASS);
 
         // Base class
         if (!superClass.equals(OBJECT)) {
             superClassDetails = typeLocationService.getTypeDetails(superClass);
             if (superClassDetails != null) {
                 entityBuilder.setSuperclass(new ClassOrInterfaceTypeDetailsBuilder(superClassDetails));
             }
         }
         entityBuilder.setExtendsTypes(Arrays.asList(superClass));
 
         // Associate appropriate annotations
         graphProvider = this.getCurrentGraphProvider();
         entityBuilder.setAnnotations(graphProvider.getClassAnnotations());
         entityBuilder.addAnnotation(ROO_JAVA_BEAN_BUILDER);
         entityBuilder.addAnnotation(ROO_TO_STRING_BUILDER);
         typeManagementService.createOrUpdateTypeOnDisk(entityBuilder.build());
 
         // Add id field
         entityDetails = typeLocationService.getTypeDetails(name);
         fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                 graphProvider.getIdAnnotations(), new JavaSymbolName("nodeId"), JavaType.LONG_OBJECT);
         typeManagementService.addField(fieldBuilder.build());
     }
 
     @Override
     public boolean isNewRepositoryAvailable() {
         return (fileManager.exists(this.getContextPath()));
     }
 
     @Override
     public void newRepository(final JavaType name, final JavaType domainType) {
         String entityId;
         GraphProvider graphProvider;
         List<JavaType> repositoryParameters;
         ClassOrInterfaceTypeDetailsBuilder entityBuilder;
 
         // Create repository class
         entityId = PhysicalTypeIdentifier.createIdentifier(name, pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
         entityBuilder = new ClassOrInterfaceTypeDetailsBuilder(entityId, Modifier.PUBLIC, name,
                 PhysicalTypeCategory.INTERFACE);
 
         // Add neo4j repository base class
         repositoryParameters = new ArrayList<JavaType>();
         repositoryParameters.add(domainType);
         graphProvider = this.getCurrentGraphProvider();
         for (String baseClass : graphProvider.getRepositoryBaseClasses()) {
             entityBuilder.addExtendsTypes(new JavaType(baseClass, 0, DataType.TYPE, null, repositoryParameters));
         }
 
         // Save repository
         typeManagementService.createOrUpdateTypeOnDisk(entityBuilder.build());
     }
 
     @Override
     public boolean isNewRelationshipEntityAvailable() {
         return (fileManager.exists(this.getContextPath()));
     }
 
     @Override
     public void newRelationshipEntity(final JavaType name, final String type, final JavaType startNode,
             final JavaType endNode, List<String> properties) {
         String entityId;
         GraphProvider graphProvider;
         FieldMetadataBuilder fieldBuilder;
         ClassOrInterfaceTypeDetails entityDetails;
         ClassOrInterfaceTypeDetailsBuilder entityBuilder;
         List<AnnotationMetadataBuilder> relationshipAnnotations;
 
         // Create relationship class
         entityId = PhysicalTypeIdentifier.createIdentifier(name, pathResolver.getFocusedPath(Path.SRC_MAIN_JAVA));
         entityBuilder = new ClassOrInterfaceTypeDetailsBuilder(entityId, Modifier.PUBLIC, name,
                 PhysicalTypeCategory.CLASS);
 
         // Associate appropriate annotations
         graphProvider = this.getCurrentGraphProvider();
         relationshipAnnotations = graphProvider.getRelationshipEntityAnnotations();
         for (AnnotationMetadataBuilder annotation : relationshipAnnotations) {
             annotation.addStringAttribute("type", type);
         }
         entityBuilder.setAnnotations(relationshipAnnotations);
         entityBuilder.addAnnotation(ROO_JAVA_BEAN_BUILDER);
         entityBuilder.addAnnotation(ROO_TO_STRING_BUILDER);
         typeManagementService.createOrUpdateTypeOnDisk(entityBuilder.build());
 
         // Id
         entityDetails = typeLocationService.getTypeDetails(name);
         fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                 graphProvider.getIdAnnotations(), new JavaSymbolName("relationshipId"), JavaType.LONG_OBJECT);
         entityBuilder.addField(fieldBuilder.build());
 
         // Start node
         fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                 graphProvider.getRelationshipStartNodeAnnotations(), new JavaSymbolName("startNode"), startNode);
         entityBuilder.addField(fieldBuilder.build());
 
         // End node
         fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                 graphProvider.getRelationshipEndNodeAnnotations(), new JavaSymbolName("endNode"), endNode);
         entityBuilder.addField(fieldBuilder.build());
 
         // Properties
         if (properties != null) {
             // One field of type String for each property
             for (String property : properties) {
                 fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                         new ArrayList<AnnotationMetadataBuilder>(), new JavaSymbolName(property), JavaType.STRING);
                 entityBuilder.addField(fieldBuilder.build());
             }
         }
 
         typeManagementService.createOrUpdateTypeOnDisk(entityBuilder.build());
     }
 
     @Override
     public boolean isNewRelationshipAvailable() {
         return (fileManager.exists(this.getContextPath()));
     }
 
     @Override
     public void newRelationship(final JavaType fromNode, final JavaType relationNode, final boolean isVia,
             final String type, final Direction direction, final String fieldName,
             final RelationshipType relationshipType) {
         List<JavaType> parameters;
         GraphProvider graphProvider;
         FieldMetadataBuilder fieldBuilder;
         ClassOrInterfaceTypeDetails entityDetails;
         List<AnnotationMetadataBuilder> fieldAnnotations;
 
         fieldBuilder = null;
         entityDetails = typeLocationService.getTypeDetails(fromNode);
         switch (relationshipType) {
         case SINGLE:
             fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                     new ArrayList<AnnotationMetadataBuilder>(), new JavaSymbolName(fieldName), relationNode);
             break;
         case MODIFIABLE:
             parameters = new ArrayList<JavaType>();
             parameters.add(relationNode);
             fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                     new ArrayList<AnnotationMetadataBuilder>(), new JavaSymbolName(fieldName), new JavaType(
                             "java.util.Set", 0, DataType.TYPE, null, parameters));
             break;
         case READ_ONLY:
             parameters = new ArrayList<JavaType>();
             parameters.add(relationNode);
             fieldBuilder = new FieldMetadataBuilder(entityDetails.getDeclaredByMetadataId(), Modifier.PRIVATE,
                     new ArrayList<AnnotationMetadataBuilder>(), new JavaSymbolName(fieldName), new JavaType(
                             "java.lang.Iterable", 0, DataType.TYPE, null, parameters));
             break;
         }
 
         if (fieldBuilder != null) {
             // Associate appropriate annotations
             graphProvider = this.getCurrentGraphProvider();
             fieldAnnotations = graphProvider.getRelationshipAnnotations();
             if (isVia) {
                 fieldAnnotations = graphProvider.getRelationshipViaAnnotations();
             }
             for (AnnotationMetadataBuilder annotation : fieldAnnotations) {
                 if (type != null) {
                     annotation.addStringAttribute("type", type);
                 }
                 if (direction != null) {
                     annotation.addEnumAttribute("direction", Direction.class.getCanonicalName(), direction.toString());
                 }
                 fieldBuilder.addAnnotation(annotation);
             }
 
             typeManagementService.addField(fieldBuilder.build());
         }
     }
 
     @Override
     public boolean isMvcSetupAvailable() {
         return (fileManager.exists(this.getContextPath()));
     }
 
     // TODO: Avoid roo restoring the files we modify (This happens to the
     // menu.jspx when we open the roo console after a successful execution).
     @Override
     public void mvcSetup() {
         int startIndex;
         String rootPath;
         String tmpString;
         String entityName;
         Set<String> entities;
         String outputContents;
         String converterMethod;
         InputStream inputStream;
         String entityNamePlural;
         OutputStream outputStream;
         String entityNameLowerCase;
         Set<FileDetails> matchingFiles;
 
         // Erase temporary graph aspect files
         // These aspect files were used to generate the mvc scaffolding, but are
         // not really required by the spring data graph add-on
         rootPath = this.getRootPath();
         matchingFiles = this.fileManager.findMatchingAntPath(rootPath + "*"
                 + new NodeEntityMetadataProviderImpl().getItdUniquenessFilenameSuffix() + ".aj");
 
         // Update mvc configuration
         if (matchingFiles != null) {
             entities = new HashSet<String>(matchingFiles.size());
             for (FileDetails typeDetails : matchingFiles) {
                 entityName = typeDetails.getFile().getName();
                 entityName = entityName.substring(0, entityName.indexOf('_'));
                 entities.add(entityName);
 
                 // TODO: Find a workaround, because even if we erase the file,
                 // roo creates it again at the end
                 // this.fileManager.delete(typeDetails.getCanonicalPath());
             }
 
             // Add a basic controller with mvc operations
             matchingFiles = new HashSet<FileDetails>();
             for (String entity : entities) {
                 entityNameLowerCase = entity.toLowerCase();
                 entityNamePlural = this.getPlural(entity).toLowerCase();
                 matchingFiles = this.fileManager
                         .findMatchingAntPath(rootPath + entity + "Controller_Roo_Controller.aj");
                 for (FileDetails typeDetails : matchingFiles) {
                     // Update controllers content
                     this.updateControllers(typeDetails, entity, entityNameLowerCase, entityNamePlural);
                 }
 
                 // Add properties labels
                 this.addPropertiesLabels(entity, entityNamePlural);
 
                 // Add listing links in main jsp menu
                 this.addMenuListingLinks(entity, entityNamePlural);
             }
         }
 
         // TODO: Only update files of graph entities
         // Update list.jspx
         matchingFiles = this.fileManager.findMatchingAntPath(rootPath + "list.jspx");
         if (matchingFiles != null) {
             inputStream = null;
             outputStream = null;
             for (FileDetails typeDetails : matchingFiles) {
                 try {
                     inputStream = new FileInputStream(typeDetails.getFile());
                     outputContents = IOUtils.toString(inputStream);
                     outputContents = outputContents.replace("<table:table", "<table:table typeIdFieldName=\"nodeId\"");
 
                     outputStream = new FileOutputStream(typeDetails.getFile());
                     IOUtils.write(outputContents, outputStream);
                 } catch (IOException e) {
                     throw new IllegalStateException(e);
                 } finally {
                     IOUtils.closeQuietly(inputStream);
                     IOUtils.closeQuietly(outputStream);
                 }
             }
         }
 
         // Add converters from ids to graph entities
         matchingFiles = this.fileManager.findMatchingAntPath(rootPath + "domain/*.java");
         if (matchingFiles != null) {
             for (FileDetails typeDetails : matchingFiles) {
                 inputStream = null;
                 outputStream = null;
                 try {
                     inputStream = FileUtils.getInputStream(this.getClass(), "ConversionService-template.java");
                     converterMethod = IOUtils.toString(inputStream);
                     entityName = typeDetails.getFile().getName().replace(".java", "");
                     converterMethod = converterMethod.replace("__ENTITY__", entityName);
                     converterMethod = converterMethod.replace("__ENTITY_LOWER__", entityName.toLowerCase());
 
                     for (FileDetails outputDetails : this.fileManager.findMatchingAntPath(rootPath
                             + "ApplicationConversionServiceFactoryBean.java")) {
                         inputStream = new FileInputStream(outputDetails.getFile());
                         outputContents = IOUtils.toString(inputStream);
                         startIndex = outputContents.indexOf("package") + "package".length();
                         tmpString = outputContents.substring(startIndex, outputContents.indexOf(".web", startIndex))
                                 .trim();
                         converterMethod = converterMethod.replace("__TOP_PACKAGE__", tmpString);
 
                         outputContents = outputContents.replace("FormattingConversionServiceFactoryBean {",
                                 "FormattingConversionServiceFactoryBean {\n" + converterMethod + "\n");
                         outputContents = outputContents.replace("super.installFormatters(registry);",
                                 "registry.addConverter(getLongTo" + entityName
                                         + "Converter());\nsuper.installFormatters(registry);");
                         outputStream = new FileOutputStream(outputDetails.getFile());
                         IOUtils.write(outputContents, outputStream);
                     }
                 } catch (IOException e) {
                     throw new IllegalStateException(e);
                 } finally {
                     IOUtils.closeQuietly(inputStream);
                     IOUtils.closeQuietly(outputStream);
                 }
             }
         }
 
         // Remove nodeId's form field in create.jspx
         matchingFiles = this.fileManager.findMatchingAntPath(rootPath + "create.jspx");
         matchingFiles.addAll(this.fileManager.findMatchingAntPath(rootPath + "update.jspx"));
         if (matchingFiles != null) {
             for (FileDetails typeDetails : matchingFiles) {
                 inputStream = null;
                 outputStream = null;
                 try {
                     inputStream = new FileInputStream(typeDetails.getFile());
                     outputContents = IOUtils.toString(inputStream);
 
                     if (outputContents.contains("<field:input field=\"nodeId\"")) {
                         startIndex = outputContents.indexOf("<field:input field=\"nodeId\"");
                         tmpString = outputContents.substring(startIndex, outputContents.indexOf(">", startIndex) + 1);
                         outputContents = outputContents.replace(tmpString, "");
 
                         outputStream = new FileOutputStream(typeDetails.getFile());
                         IOUtils.write(outputContents, outputStream);
                     }
                 } catch (IOException e) {
                     throw new IllegalStateException(e);
                 } finally {
                     IOUtils.closeQuietly(inputStream);
                     IOUtils.closeQuietly(outputStream);
                 }
             }
         }
 
     }
 
     /**
      * Add listing labels for the specified entity.
      * 
      * @param entityNamePlural
      */
     private void addPropertiesLabels(final String entity, final String entityNamePlural) {
         LogicalPath webappPath;
         String applicationPropertiesPath;
 
         webappPath = pathResolver.getFocusedPath(Path.SRC_MAIN_WEBAPP);
         applicationPropertiesPath = "WEB-INF/i18n/application.properties";
         this.propFileOperations.addPropertyIfNotExists(webappPath, applicationPropertiesPath,
                 "menu_item_" + entity.toLowerCase() + "_list_label", entityNamePlural);
     }
 
     /**
      * Add a link to the entity listing in the main menu jsp file.
      * 
      * @param entity
      * @param entityNamePlural
      */
     private void addMenuListingLinks(final String entity, final String entityNamePlural) {
         this.menuOperations.addMenuItem(new JavaSymbolName(entity), new JavaSymbolName("list"), "global_menu_list", "/"
                 + entityNamePlural + "?page=1&amp;size=${empty param.size ? 10 : param.size}", "",
                 pathResolver.getFocusedPath(Path.SRC_MAIN_WEBAPP));
     }
 
     /**
      * Update the mvc controller associated to the specified entity, with crud
      * operations.
      * 
      * @param typeDetails
      * @param entityName
      * @param entityNameLowerCase
      * @param entityNamePlural
      */
     private void updateControllers(final FileDetails typeDetails, final String entityName,
             final String entityNameLowerCase, final String entityNamePlural) {
         String outputContents;
         InputStream inputStream;
         OutputStream outputStream;
 
         inputStream = null;
         outputStream = null;
         try {
             inputStream = FileUtils.getInputStream(this.getClass(), "Controller-template.java");
             outputContents = IOUtils.toString(inputStream);
             outputContents = outputContents.replace("__TOP_PACKAGE__",
                     this.projectOperations.getTopLevelPackage(this.projectOperations.getFocusedModuleName())
                             .getFullyQualifiedPackageName());
             outputContents = outputContents.replace("__ENTITY__", entityName);
             outputContents = outputContents.replace("__ENTITY_LOWER_CASE__", entityNameLowerCase);
             outputContents = outputContents.replace("__ENTITY_PLURAL_LOWER_CASE__", entityNamePlural);
 
             outputStream = this.fileManager.createFile(typeDetails.getCanonicalPath().replace("_Roo_", "_Graph_"))
                     .getOutputStream();
             IOUtils.write(outputContents, outputStream);
 
         } catch (IOException e) {
             throw new IllegalStateException(e);
         } finally {
             IOUtils.closeQuietly(inputStream);
             IOUtils.closeQuietly(outputStream);
         }
     }
 
     /**
      * Add the project dependencies associated to the specified graph provider.
      * 
      * @param provider
      */
     private void addDependencies(final GraphProvider graphProvider) {
         Element configuration;
         List<Dependency> dependencies;
         List<Element> providerDependences;
 
         dependencies = new ArrayList<Dependency>();
         configuration = XmlUtils.getConfiguration(this.getClass());
 
         // Recover the dependencies associated to the specified provider
         providerDependences = XmlUtils.findElements(graphProvider.getConfigPrefix(), configuration);
         for (Element dependency : providerDependences) {
             dependencies.add(new Dependency(dependency));
         }
 
         // Add all new dependencies to pom.xml
         projectOperations.addDependencies("", dependencies);
     }
 
     /**
      * Add the configuration file associated to the specified graph provider, in
      * the specified location.
      * 
      * @param provider
      * @param dataStoreLocation
      */
     private void addConfiguration(final GraphProvider graphProvider, final String dataStoreLocation) {
         String contextPath;
         String outputContents;
         InputStream templateStream;
         OutputStream configurationStream;
 
         // Create a new graph configuration file every time
         contextPath = this.getContextPath();
         if (this.fileManager.exists(contextPath)) {
             this.fileManager.delete(contextPath);
         }
 
         // The new file content is based on the appropriate provider template
         templateStream = null;
         configurationStream = null;
         try {
             // Read template contents and update required variables
             templateStream = FileUtils.getInputStream(this.getClass(), "applicationContext-graph-"
                     + graphProvider.name().toLowerCase() + ".xml");
             outputContents = IOUtils.toString(templateStream);
             outputContents = outputContents.replace("${store.location}", dataStoreLocation);
             outputContents = outputContents.replace("TO_BE_CHANGED_BY_ADDON",
                     projectOperations.getTopLevelPackage(projectOperations.getFocusedModuleName())
                             .getFullyQualifiedPackageName());
             configurationStream = this.fileManager.createFile(contextPath).getOutputStream();
             IOUtils.write(outputContents, configurationStream);
         } catch (IOException e) {
             throw new IllegalStateException(e);
         } finally {
             IOUtils.closeQuietly(templateStream);
             IOUtils.closeQuietly(configurationStream);
         }
     }
 
     /**
      * Get the graph provider associated to the current project.
      * 
      * @return
      */
     private GraphProvider getCurrentGraphProvider() {
         int providerIndex;
         String graphConfiguration;
         GraphProvider returnValue;
 
         returnValue = GraphProvider.Neo4j;
         try {
             graphConfiguration = IOUtils.toString(new FileInputStream(this.fileManager.readFile(this.getContextPath())
                     .getFile()));
             providerIndex = graphConfiguration.indexOf("GraphProvider:") + "GraphProvider:".length();
             returnValue = GraphProvider.valueOf(graphConfiguration.substring(providerIndex,
                     graphConfiguration.indexOf("-", providerIndex)).trim());
         } catch (IOException e) {
         }
 
         return returnValue;
     }
 
     /**
      * Get the configuration context path.
      * 
      * @return
      */
     private String getContextPath() {
         return pathResolver.getFocusedIdentifier(Path.SPRING_CONFIG_ROOT, "applicationContext-graph.xml");
     }
 
     /**
      * Project root path.
      * 
      * @return
      */
     private String getRootPath() {
         return this.projectOperations.getFocusedModule().getRoot() + "**" + File.separator;
     }
 
     /**
      * Get the plural name for an entity.
      * 
      * @return
      */
     private String getPlural(final String entityName) {
         String returnValue;
 
         try {
            returnValue = Noun.pluralOf(entityName, Locale.US);
         } catch (RuntimeException e) {
             returnValue = null;
         }
 
         return returnValue;
     }
 }
