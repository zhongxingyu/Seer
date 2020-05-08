 /*
  * Copyright 2010 jccastrejon
  *  
  * This file is part of MexADL.
  *
  * MexADL is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MexADL is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with MexADL.  If not, see <http://www.gnu.org/licenses/>.
 */
 package mx.itesm.mexadl.util;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.ToolFactory;
 import org.eclipse.jdt.core.formatter.CodeFormatter;
 import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.text.edits.MalformedTreeException;
 import org.eclipse.text.edits.TextEdit;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.xpath.XPath;
 
 /**
  * The Util class provides helper methods for the generation of MexADL
  * artifacts.
  * 
  * @author jccastrejon
  * 
  */
 public class Util {
 
     /**
      * Class logger.
      */
     private static Logger logger = Logger.getLogger(Util.class.getName());
 
     /**
      * XPath expression to identify the description of an xADL architecture.
      */
     private static XPath xArchDescriptionPath;
 
     /**
      * Indicates whether the velocity properties have been initialized or not.
      */
     private static boolean velocityInit = false;
 
     /**
      * 
      */
     public static final String JAVA_EXTENSION = ".java";
 
     /**
      * 
      */
     public static final String ASPECTJ_EXTENSION = ".aj";
 
     /**
      * XML Schema-instance namespace.
      */
     public static final Namespace XSI_NAMESPACE = Namespace.getNamespace("xsi",
             "http://www.w3.org/2001/XMLSchema-instance");
 
     /**
      * xADL Types namespace.
      */
     public static final Namespace XADL_TYPES_NAMESPACE = Namespace.getNamespace("types",
             "http://www.ics.uci.edu/pub/arch/xArch/types.xsd");
 
     /**
      * xADL Instance namespace.
      */
     public static final Namespace XADL_INSTANCE_NAMESPACE = Namespace.getNamespace("types",
             "http://www.ics.uci.edu/pub/arch/xArch/instance.xsd");
 
     /**
      * MexADL properties.
      */
     private static ResourceBundle properties = ResourceBundle.getBundle("mx.itesm.mexadl.configuration");
 
     /**
      * MexADL namespace.
      */
     public static final Namespace MEXADL_NAMESPACE = Namespace.getNamespace("mexadl", "http://mx.itesm/mexadl.xsd");
 
     /**
      * XML XLink namespace.
      */
     public static final Namespace XLINK_NAMESPACE = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
 
     /**
      * xADL Java implementation namespace.
      */
     public static final Namespace XADL_JAVAIMPLEMENTATION_NAMESPACE = Namespace.getNamespace("javaimplementation",
             "http://www.ics.uci.edu/pub/arch/xArch/javaimplementation.xsd");
 
     static {
         try {
             Util.xArchDescriptionPath = XPath.newInstance("/instance:xArch/types:archStructure/types:description");
         } catch (Exception e) {
             Util.logger.log(Level.WARNING, "Error while loading Util: ", e);
         }
     }
 
     /**
      * Get the Java implementation class associated to the specified link.
      * 
      * @param document
      * @param linkId
      * @return
      * @throws JDOMException
      */
     public static String getLinkImplementationClass(final Document document, final String linkId) throws JDOMException {
         String typeId;
         XPath typePath;
         Element element;
         String returnValue;
 
         returnValue = null;
         element = Util.getLinkEndpoint(document, linkId);
         if (element != null) {
 
             // Check if the component/connector has any implementation
             // associated
             typeId = Util.getTypeId(element);
 
             if (typeId != null) {
                 typePath = XPath
                         .newInstance("/instance:xArch/types:archTypes/types:componentType[@types:id=\""
                                 + typeId
                                 + "\"]/implementation:implementation/javaimplementation:mainClass/javaimplementation:javaClassName");
 
                 element = ((Element) typePath.selectSingleNode(document));
                 if (element != null) {
                     returnValue = element.getTextTrim();
                 }
             }
         }
 
         return returnValue;
     }
 
     /**
      * Get the type associated to the specified link id. The type can be either
      * a component or a connector.
      * 
      * @param document
      * @param id
      * @return
      * @throws JDOMException
      */
     public static Element getLinkEndpoint(final Document document, final String id) throws JDOMException {
         return Util.getEndpoint(document, id,
                 "/instance:xArch/types:archStructure/types:$typeName[@types:id=\"$typeId\"]");
     }
 
     /**
      * Get the element associated to the end-point represented by the specified
      * XPath expression.
      * 
      * @param document
      * @param id
      * @param xPathExpression
      * @return
      * @throws JDOMException
      */
     public static Element getEndpoint(final Document document, final String id, String xPathExpression)
             throws JDOMException {
         Element returnValue;
 
         // Component interface
         returnValue = (Element) XPath.newInstance(
                 xPathExpression.replace("$typeName", "component").replace("$typeId", id)).selectSingleNode(document);
 
         // Connector interface
         if (returnValue == null) {
             returnValue = (Element) XPath.newInstance(
                     xPathExpression.replace("$typeName", "connector").replace("$typeId", id))
                     .selectSingleNode(document);
         }
 
         return returnValue;
     }
 
     /**
      * Create a Java file from a velocity template. This methods adds two
      * implied objects, the suffix of the file to be generated, and a reference
      * to this Utility class.
      * 
      * @param document
      * @param xArchFilePath
      * @param aspectTemplate
      * @param properties
      * @param prefix
      * @param suffix
      * @throws IOException
      * @throws JDOMException
      * @throws BadLocationException
      * @throws MalformedTreeException
      */
     public static void createFile(final Document document, final String xArchFilePath, final Template aspectTemplate,
             final Map<String, Object> properties, final String prefix, final String suffix, final String extension)
             throws IOException, JDOMException, MalformedTreeException, BadLocationException {
         Writer writer;
         File outputDir;
         File outputFile;
         File contentFile;
         VelocityContext context;
 
         // Create a temporary file to hold the java content and then save the
         // formatted content in a file next to the xArchFilePath
         outputDir = new File(new File(xArchFilePath).getParent() + "/src_mexadl/mx/itesm/mexadl/");
         outputDir.mkdirs();
         outputFile = new File(outputDir, prefix + "_" + suffix + extension);
         contentFile = File.createTempFile(prefix, suffix + System.currentTimeMillis());
         writer = new BufferedWriter(new FileWriter(contentFile));
 
         // Use the velocity template to generate the aspect content
         context = new VelocityContext();
         context.put("suffix", suffix);
         properties.put("Util", Util.class);
         for (String key : properties.keySet()) {
             context.put(key, properties.get(key));
         }
 
         // Create and format the file contents
         aspectTemplate.merge(context, writer);
         writer.close();
         Util.formatFileContent(outputFile, contentFile);
     }
 
     /**
      * Format the contents of a Java file using Eclipse's built-in code
      * formatter.
      * 
      * @param outputFile
      * @param contentFile
      * @throws IOException
      * @throws MalformedTreeException
      * @throws BadLocationException
      */
     @SuppressWarnings("unchecked")
     public static void formatFileContent(final File outputFile, final File contentFile) throws IOException,
             MalformedTreeException, BadLocationException {
         Map options;
         String source;
         TextEdit edit;
         Writer writer;
         int contentKind;
         IDocument document;
         CodeFormatter codeFormatter;
 
         // use Eclipse's default formatting options
         writer = new BufferedWriter(new FileWriter(outputFile));
         options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
 
         // Compiler settings to be able to format 1.6 code
         options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
         options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
         options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
 
         // Instantiate the default code formatter with the given options
         codeFormatter = ToolFactory.createCodeFormatter(options);
         source = Util.getFileContents(contentFile);
         document = new org.eclipse.jface.text.Document(source);
 
         // Decide code kind and try to apply format
         contentKind = CodeFormatter.K_UNKNOWN;
         if (outputFile.toString().endsWith(Util.JAVA_EXTENSION)) {
             contentKind = CodeFormatter.K_COMPILATION_UNIT;
         }
         edit = codeFormatter.format(contentKind, source, 0, source.length(), 0, System.getProperty("line.separator"));
         if (edit != null) {
             edit.apply(document);
         }
 
         // Save to output file
         writer.write(document.get());
         writer.close();
     }
 
     /**
      * Read the contents of the specified file.
      * 
      * @param file
      * @return
      * @throws java.io.IOException
      */
     private static String getFileContents(final File file) throws java.io.IOException {
         byte[] buffer;
         BufferedInputStream inputStream;
 
         inputStream = null;
         try {
             inputStream = new BufferedInputStream(new FileInputStream(file));
             buffer = new byte[(int) file.length()];
             inputStream.read(buffer);
         } finally {
             if (inputStream != null) {
                 inputStream.close();
             }
         }
 
         return new String(buffer);
     }
 
     /**
      * Get a velocity template from the classpath.
      * 
      * @param clazz
      * @param name
      * @return
      * @throws Exception
      */
     public static Template getVelocityTemplate(final Class<?> clazz, final String name) throws Exception {
         Template returnValue;
         Properties velocityProperties;
 
         if (!Util.velocityInit) {
             velocityProperties = new Properties();
             velocityProperties.put("resource.loader", "class");
             velocityProperties.put("class.resource.loader.class",
                     "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
             Velocity.init(velocityProperties);
             Util.velocityInit = true;
         }
 
         returnValue = Velocity.getTemplate(Util.getConfigurationProperty(clazz, "template." + name));
         return returnValue;
     }
 
     /**
      * Get a valid name that can be used as an identifier.
      * 
      * @param name
      *            Name to be validated.
      * @return Valid identifier.
      */
     public static String getValidName(final String name) {
         return name.replaceAll("[^\\p{Alnum}]", "_");
     }
 
     /**
      * Get a valid XLink Id to be used in the architecture definition.
      * 
      * @param linkId
      *            XLink Id.
      * @return Valid XLink Id.
      */
     public static String getValidLinkId(final String linkId) {
         String returnValue;
 
         returnValue = linkId;
         if ((returnValue != null) && (returnValue.startsWith("#"))) {
             returnValue = returnValue.substring(1);
         }
 
         return returnValue;
     }
 
     /**
      * Get the ID attribute of a given Element.
      * 
      * @param element
      * @return
      */
     public static String getIdValue(final Element element) {
         return Util.getValidLinkId(element.getAttributeValue("id", Util.XADL_TYPES_NAMESPACE));
     }
 
     /**
      * Get the HREF attribute of a given Element.
      * 
      * @param element
      * @return
      */
     public static String getHrefValue(final Element element) {
         return Util.getValidLinkId(element.getAttributeValue("href", Util.XLINK_NAMESPACE));
     }
 
     /**
      * Get the XML Schema-instance type attribute of the given element.
      * 
      * @param element
      * @return
      */
     public static String getXsiType(final Element element) {
         return element.getAttribute("type", Util.XSI_NAMESPACE).getValue();
     }
 
     /**
      * Get the TYPE Id value for a given Element.
      * 
      * @param element
      * @return
      */
     public static String getTypeId(final Element element) {
         String returnValue;
         Element typeElement;
 
         returnValue = null;
         typeElement = element.getChild("type", Util.XADL_TYPES_NAMESPACE);
 
         if (typeElement != null) {
             returnValue = Util.getHrefValue(typeElement);
         }
 
         return returnValue;
     }
 
     /**
      * 
      * @param document
      * @return
      * @throws JDOMException
      */
     public static String getDocumentName(final Document document) throws JDOMException {
         return Util.getValidName(((Element) Util.xArchDescriptionPath.selectSingleNode(document)).getValue());
     }
 
     /**
      * Get the value associated to the specified property in the MexADL
      * configuration file.
      * 
      * @param clazz
      * @param name
      * @return
      */
     public static String getConfigurationProperty(final Class<?> clazz, final String name) {
         return Util.properties.getString(clazz.getName() + "." + name);
     }
     
     /**
      * Verify if an implementing class represents a Java annotation.
      * 
      * @param implementationClass
      * @return
      * @throws JDOMException
      */
     @SuppressWarnings("unchecked")
     public static List<String> getAnnotations(final Document document) throws JDOMException {
         XPath typePath;
         List<Element> elements;
         List<String> returnValue;
 
         typePath = XPath.newInstance("//javaimplementation:mainClass[mexadl:isAnnotation='true']");
         elements = (List<Element>) typePath.selectNodes(document);
 
         returnValue = null;
         if (elements != null) {
             returnValue = new ArrayList<String>(elements.size());
             for (Element element : elements) {
                 returnValue.add(element.getChildTextTrim("javaClassName", Util.XADL_JAVAIMPLEMENTATION_NAMESPACE));
             }
         }
 
         return returnValue;
     }
 }
