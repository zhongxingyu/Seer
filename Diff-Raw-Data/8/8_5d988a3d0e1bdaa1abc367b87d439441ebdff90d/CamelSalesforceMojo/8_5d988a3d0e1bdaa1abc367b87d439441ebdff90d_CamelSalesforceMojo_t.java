 /*
  * Copyright 2001-2005 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.fusesource.camel.maven;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.log4j.Logger;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.runtime.RuntimeConstants;
 import org.apache.velocity.runtime.log.Log4JLogChute;
 import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.fusesource.camel.component.salesforce.SalesforceComponent;
 import org.fusesource.camel.component.salesforce.api.RestException;
 import org.fusesource.camel.component.salesforce.api.dto.*;
import org.fusesource.camel.component.salesforce.internal.SalesforceSession;
import org.fusesource.camel.component.salesforce.internal.client.DefaultRestClient;
import org.fusesource.camel.component.salesforce.internal.client.RestClient;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.*;
 import java.util.regex.Pattern;
 
 /**
  * Goal which generates POJOs for Salesforce SObjects
  *
  * @goal generate
  * 
  * @phase generate-sources
  *
  */
 public class CamelSalesforceMojo extends AbstractMojo
 {
     private static final String JAVA_EXT = ".java";
     private static final String PACKAGE_NAME_PATTERN = "^[a-z]+(\\.[a-z][a-z0-9]*)*$";
     private static final String SOBJECT_POJO_VM = "/sobject-pojo.vm";
     private static final String SOBJECT_QUERY_RECORDS_VM = "/sobject-query-records.vm";
     private static final String SOBJECT_PICKLIST_VM = "/sobject-picklist.vm";
 
     // used for velocity logging, to avoid creating velocity.log
     private static final Logger LOG = Logger.getLogger(CamelSalesforceMojo.class.getName());
 
     /**
      * Salesforce client id
      * @parameter expression="${clientId}"
      * @required
      */
     protected String clientId;
 
     /**
      * Salesforce client secret
      * @parameter expression="${clientSecret}"
      * @required
      */
     protected String clientSecret;
 
     /**
      * Salesforce user name
      * @parameter expression="${userName}"
      * @required
      */
     protected String userName;
 
     /**
      * Salesforce password
      * @parameter expression="${password}"
      * @required
      */
     protected String password;
 
     /**
      * Salesforce version
      * @parameter expression="${version}" default-value="25.0"
      */
     protected String version;
 
     /**
      * Location of the file.
      * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/generated-sources/camel-salesforce"
      * @required
      */
     protected File outputDirectory;
 
     /**
      * Names of Salesforce SObject for which POJOs must be generated
      * @parameter
      */
     protected String[] includes;
 
     /**
      * Do NOT generate POJOs for these Salesforce SObjects
      * @parameter
      */
     protected String[] excludes;
 
     /**
      * Include Salesforce SObjects that match pattern
      * @parameter expression="${includePattern}"
      */
     protected String includePattern;
 
     /**
      * Exclude Salesforce SObjects that match pattern
      * @parameter expression="${excludePattern}"
      */
     protected String excludePattern;
 
     /**
      * Java package name for generated POJOs
      * @parameter expression="${packageName}" default-value="org.fusesource.camel.salesforce.dto"
      */
     protected String packageName;
 
     private VelocityEngine engine;
 
     /**
      * Execute the mojo to generate SObject POJOs
      * @throws MojoExecutionException
      */
     public void execute()
         throws MojoExecutionException
     {
         // initialize velocity to load resources from class loader and use Log4J
         Properties velocityProperties = new Properties();
         velocityProperties.setProperty(RuntimeConstants.RESOURCE_LOADER, "cloader");
         velocityProperties.setProperty("cloader.resource.loader.class", ClasspathResourceLoader.class.getName());
         velocityProperties.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4JLogChute.class.getName());
         velocityProperties.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM + ".log4j.logger", LOG.getName());
         engine = new VelocityEngine(velocityProperties);
         engine.init();
 
         // make sure we can load both templates
         if (!engine.resourceExists(SOBJECT_POJO_VM) ||
             !engine.resourceExists(SOBJECT_QUERY_RECORDS_VM)) {
             throw new MojoExecutionException("Velocity templates not found");
         }
 
         // connect to Salesforce
         final DefaultHttpClient httpClient = new DefaultHttpClient();
         final SalesforceSession session = new SalesforceSession(httpClient,
             SalesforceComponent.DEFAULT_LOGIN_URL,
             clientId, clientSecret, userName, password);
         getLog().info("Salesforce login...");
         try {
             session.login(null);
         } catch (RestException e) {
             String msg = "Salesforce login error " + e.getMessage();
             getLog().error(msg, e);
             throw new MojoExecutionException(msg, e);
         }
         getLog().info("Salesforce login successful");
 
         try {
             // create rest client
             final RestClient restClient = new DefaultRestClient(httpClient,
                 version, "json", session);
 
             // use Jackson json
             final ObjectMapper mapper = new ObjectMapper();
 
             // call getGlobalObjects to get all SObjects
             final Set<String> objectNames = new HashSet<String>();
             try {
                 getLog().info("Getting Salesforce Objects...");
                 final GlobalObjects globalObjects = mapper.readValue(restClient.getGlobalObjects(),
                     GlobalObjects.class);
 
                 // create a list of object names
                 for (SObject sObject : globalObjects.getSobjects()) {
                     objectNames.add(sObject.getName());
                 }
             } catch (Exception e) {
                 String msg = "Error getting global Objects " + e.getMessage();
                 getLog().error(msg, e);
                 throw new MojoExecutionException(msg, e);
             }
 
             // check if we are generating POJOs for all objects or not
             if ((includes != null && includes.length > 0) ||
                 (excludes != null && excludes.length > 0) ||
                 (includePattern != null && !includePattern.trim().isEmpty()) ||
                 (excludePattern != null && !excludePattern.trim().isEmpty())) {
 
                 getLog().info("Looking for matching Object names...");
                 // create a list of accepted names
                 final Set<String> includedNames = new HashSet<String>();
                 if (includes != null && includes.length > 0) {
                     for (String name : includes) {
                         name = name.trim();
                         if (name.isEmpty()) {
                             throw new MojoExecutionException("Invalid empty name in includes");
                         }
                         includedNames.add(name);
                     }
                 }
 
                 final Set<String> excludedNames = new HashSet<String>();
                 if (excludes != null && excludes.length > 0) {
                     for (String name : excludes) {
                         name = name.trim();
                         if (name.isEmpty()) {
                             throw new MojoExecutionException("Invalid empty name in excludes");
                         }
                         excludedNames.add(name);
                     }
                 }
 
                 // check whether a pattern is in effect
                 Pattern incPattern;
                 if (includePattern != null && !includePattern.trim().isEmpty()) {
                     incPattern = Pattern.compile(includePattern.trim());
                 } else if (includedNames.isEmpty()) {
                     // include everything by default if no include names are set
                     incPattern = Pattern.compile(".*");
                 } else {
                     // include nothing by default if include names are set
                     incPattern = Pattern.compile("^$");
                 }
 
                 // check whether a pattern is in effect
                 Pattern excPattern;
                 if (excludePattern != null && excludePattern.trim().isEmpty()) {
                     excPattern = Pattern.compile(excludePattern.trim());
                 } else {
                     // exclude nothing by default
                     excPattern = Pattern.compile("^$");
                 }
 
                 final Set<String> acceptedNames = new HashSet<String>();
                 for (String name : objectNames) {
                     // name is included, or matches include pattern
                     // and is not excluded and does not match exclude pattern
                     if ((includedNames.contains(name) || incPattern.matcher(name).matches()) &&
                         !excludedNames.contains(name) &&
                         !excPattern.matcher(name).matches()) {
                         acceptedNames.add(name);
                     }
                 }
                 objectNames.clear();
                 objectNames.addAll(acceptedNames);
 
                 getLog().info("Found " + objectNames.size() + " matching Objects");
 
             } else {
                 getLog().warn("Generating Java classes for all " + objectNames.size() + " Objects, this may take a while...");
             }
 
             // for every accepted name, get SObject description
             final Set<SObjectDescription> descriptions =
                 new HashSet<SObjectDescription>();
 
             try {
                 getLog().info("Retrieving Object descriptions...");
                 for (String name : objectNames) {
                     descriptions.add(mapper.readValue(restClient.getDescription(name),
                             SObjectDescription.class));
                 }
             } catch (Exception e) {
                 String msg = "Error getting SObject description " + e.getMessage();
                 getLog().error(msg, e);
                 throw new MojoExecutionException(msg, e);
             }
 
             // create package directory
             // validate package name
             if (!packageName.matches(PACKAGE_NAME_PATTERN)) {
                 throw new MojoExecutionException("Invalid package name " + packageName);
             }
             final File pkgDir = new File(outputDirectory, packageName.trim().replace('.', File.separatorChar));
             if (!pkgDir.exists()) {
                 if (!pkgDir.mkdirs()) {
                     throw new MojoExecutionException("Unable to create " + pkgDir);
                 }
             }
 
             getLog().info("Generating Java Classes...");
             // generate POJOs for every object description
             final GeneratorUtility utility = new GeneratorUtility();
             // should we provide a flag to control timestamp generation?
             final String generatedDate = new Date().toString();
             for (SObjectDescription description : descriptions) {
                 processDescription(pkgDir, description, utility, generatedDate);
             }
 
             getLog().info("Successfully generated " + (descriptions.size() * 2) + " Java Classes");
 
         } finally {
             // Salesforce logout
             try {
                 session.logout();
             } catch (RestException e) {
                 // ignore
             }
 
             // release HttpConnections
             httpClient.getConnectionManager().shutdown();
         }
     }
 
     private void processDescription(File pkgDir, SObjectDescription description, GeneratorUtility utility, String generatedDate) throws MojoExecutionException {
         // generate a source file for SObject
         String fileName = description.getName() + JAVA_EXT;
         BufferedWriter writer = null;
         try {
             final File pojoFile = new File(pkgDir, fileName);
             writer = new BufferedWriter(new FileWriter(pojoFile));
 
             VelocityContext context = new VelocityContext();
             context.put("packageName", packageName);
             context.put("utility", utility);
             context.put("desc", description);
             context.put("generatedDate", generatedDate);
 
             Template pojoTemplate = engine.getTemplate(SOBJECT_POJO_VM);
             pojoTemplate.merge(context, writer);
             // close pojoFile
             writer.close();
 
             // write required Enumerations for any picklists
             for (SObjectField field : description.getFields()) {
                 if (utility.isPicklist(field)) {
                     fileName = utility.enumTypeName(field.getName()) + JAVA_EXT;
                     File enumFile = new File(pkgDir, fileName);
                     writer = new BufferedWriter(new FileWriter(enumFile));
 
                     context = new VelocityContext();
                     context.put("packageName", packageName);
                     context.put("utility", utility);
                     context.put("field", field);
                     context.put("generatedDate", generatedDate);
 
                     Template queryTemplate = engine.getTemplate(SOBJECT_PICKLIST_VM);
                     queryTemplate.merge(context, writer);
 
                     // close Enum file
                     writer.close();
                 }
             }
 
             // write the QueryRecords class
             fileName = "QueryRecords" + description.getName() + JAVA_EXT;
             File queryFile = new File(pkgDir, fileName);
             writer = new BufferedWriter(new FileWriter(queryFile));
 
             context = new VelocityContext();
             context.put("packageName", packageName);
             context.put("desc", description);
             context.put("generatedDate", generatedDate);
 
             Template queryTemplate = engine.getTemplate(SOBJECT_QUERY_RECORDS_VM);
             queryTemplate.merge(context, writer);
 
             // close QueryRecords file
             writer.close();
 
         } catch (IOException e) {
             String msg = "Error creating " + fileName + ": " + e.getMessage();
             getLog().error(msg, e);
             throw new MojoExecutionException(msg, e);
         } finally {
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException e) {
                     // ignore
                 }
             }
         }
     }
 
     public static class GeneratorUtility {
 
         private static final Set<String> baseFields;
         private static final Map<String, String> lookupMap;
 
         static {
             baseFields = new HashSet<String>();
             for (Field field : AbstractSObjectBase.class.getDeclaredFields()) {
                 baseFields.add(field.getName());
             }
 
             // create a type map
             // using JAXB mapping, for the most part
             // uses Joda time instead of XmlGregorianCalendar
             // TODO do we need support for commented types???
             final String[][] typeMap = new String[][] {
                 {"ID", "String"}, // mapping for tns:ID SOAP type
                 {"string", "String"},
                 {"integer", "java.math.BigInteger"},
                 {"int", "Integer"},
                 {"long", "Long"},
                 {"short", "Short"},
                 {"decimal", "java.math.BigDecimal"},
                 {"float", "Float"},
                 {"double", "Double"},
                 {"boolean", "Boolean"},
                 {"byte", "Byte"},
 //                {"QName", "javax.xml.namespace.QName"},
 
 //                {"dateTime", "javax.xml.datatype.XMLGregorianCalendar"},
                 {"dateTime", "org.joda.time.DateTime"},
 
 //                {"base64Binary", "byte[]"},
 //                {"hexBinary", "byte[]"},
 
                 {"unsignedInt", "Long"},
                 {"unsignedShort", "Integer"},
                 {"unsignedByte", "Short"},
 
 //                {"time", "javax.xml.datatype.XMLGregorianCalendar"},
                 {"time", "org.joda.time.DateTime"},
 //                {"date", "javax.xml.datatype.XMLGregorianCalendar"},
                 {"date", "org.joda.time.DateTime"},
 //                {"g", "javax.xml.datatype.XMLGregorianCalendar"},
                 {"g", "org.joda.time.DateTime"},
 
 /*
                 {"anySimpleType", "java.lang.Object"},
                 {"anySimpleType", "java.lang.String"},
                 {"duration", "javax.xml.datatype.Duration"},
                 {"NOTATION", "javax.xml.namespace.QName"}
 */
             };
             lookupMap = new HashMap<String, String>();
             for (String[] entry : typeMap) {
                 lookupMap.put(entry[0], entry[1]);
             }
         }
 
         public boolean notBaseField(String name) {
             return !baseFields.contains(name);
         }
 
         public String getFieldType(SObjectField field) throws MojoExecutionException {
             // check if this is a picklist
             if (isPicklist(field)) {
                 // use a pick list enum, which will be created after generating the SObject class
                 return enumTypeName(field.getName());
             } else {
                 // map field to Java type
                 final String soapType = field.getSoapType();
                 final String type = lookupMap.get(soapType.substring(soapType.indexOf(':')+1));
                 if (type == null) {
                     String msg = String.format("Unsupported type %s for field %s", soapType, field.getName());
                     throw new MojoExecutionException(msg);
                 }
                 return type;
             }
         }
 
         public boolean hasPicklists(SObjectDescription desc) {
             for (SObjectField field : desc.getFields()) {
                 if (isPicklist(field)) {
                     return true;
                 }
             }
             return false;
         }
 
         public PickListValue getLastEntry(SObjectField field) {
             final List<PickListValue> values = field.getPicklistValues();
             return values.get(values.size() - 1);
         }
 
         public boolean isPicklist(SObjectField field) {
             return field.getPicklistValues() != null && !field.getPicklistValues().isEmpty();
         }
 
         public String enumTypeName(String name) {
             name = name.endsWith("__c") ? name.substring(0, name.length() - 3) : name;
             return name + "Enum";
         }
 
         public String getEnumConstant(String value) {
 
             // TODO add support for supplementary characters
             final StringBuffer result = new StringBuffer();
             boolean changed = false;
             if (!Character.isJavaIdentifierStart(value.charAt(0))) {
                 result.append("_");
                 changed = true;
             }
             for (char c : value.toCharArray()) {
                 if (Character.isJavaIdentifierPart(c)) {
                     result.append(c);
                 } else {
                     // replace non Java identifier character with '_'
                     result.append('_');
                     changed = true;
                 }
             }
 
             return changed ? result.toString().toUpperCase() : value.toUpperCase();
         }
     }
 
 }
