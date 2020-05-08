 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.common.documentation;
 
 import de.escidoc.core.common.servlet.invocation.XMLBase;
 import de.escidoc.core.common.util.logger.AppLogger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.transform.TransformerException;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 /**
  * 
  * Create documentation for a bundle of resources.
  * 
  * @author schneider
  * 
  */
 public class Resource extends XMLBase {
 
     public static final String INTERFACE_BOTH = "BOTH";
 
     public static final String INTERFACE_REST = "REST";
 
     public static final String INTERFACE_SOAP = "SOAP";
 
     private static final AppLogger LOG =
         new AppLogger(Resource.class.getName());
 
     private static final boolean VISIBILTY_DEFAULT = true;
 
     private static final String TEMPLATE_PATH = "docbook/templates";
 
     private static final String TEMPLATE_CHAPTER_START = "ChapterStart.xml";
 
     private static final String TEMPLATE_CHAPTER_END = "ChapterEnd.xml";
 
     private static final String TEMPLATE_CHAPTER_DESCRIPTION =
         "ChapterDescription.xml";
 
     private static final String TEMPLATE_SECTION_START = "SectionStart.xml";
 
     private static final String TEMPLATE_SECTION_WITH_ID_START =
         "SectionWithIdStart.xml";
 
     private static final String TEMPLATE_SECTION_END = "SectionEnd.xml";
 
     private static final String TEMPLATE_SECTION_DESCRIPTION =
         "SectionDescription.xml";
 
     private static final String TEMPLATE_METHOD_TABLE_START =
         "MethodTableStart.xml";
 
     private static final String TEMPLATE_METHOD_TABLE_ROW_2_COLS =
         "MethodTableRow2Cols.xml";
 
     private static final String TEMPLATE_METHOD_TABLE_END =
         "MethodTableEnd.xml";
 
     private static final String TEMPLATE_PARA_PAGE_BREAK = "ParaPageBreak.xml";
 
     private static final String VAR_ID = "\\$\\{ID\\}";
 
     private static final String VAR_KEY = "\\$\\{KEY\\}";
 
     private static final String VAR_VALUE = "\\$\\{VALUE\\}";
 
     private static final String VAR_TITLE = "\\$\\{TITLE\\}";
 
     private static final String VAR_DESCRIPTION = "\\$\\{DESCRIPTION\\}";
 
     private static final String VAR_HEADER_ROW = "\\$\\{HEADER_ROW\\}";
 
     private static final String CR_LF = "\n";
 
     private static final String DEFAULT_TEXT = "CHANGE ME !!!";
 
     private static final String GREATER_THAN = "&#62;";
 
     private static final String LESS_THAN = "&#60;";
 
     private static final String HANDLER_POSTFIX = "Handler";
 
     private static final String SERVICE_POSTFIX = "Service";
 
     private List<Node> resources = null;
 
     private String name = null;
 
     private boolean includeErrors = false;
 
     private boolean checkVisibility = false;
 
     private String restDocumentation = "";
 
     private String soapDocumentation = "";
 
     /**
      * Constructor.
      * 
      * @param resources
      *            The resources to document. They all must have the same name
      *            but have different base uris.
      * @param name
      *            The resources name.
      */
     public Resource(final List<Node> resources, final String name) {
 
         this.resources = resources;
         this.name = name;
     }
 
     /**
      * Initialize the resulting documentation to the given text.
      * 
      * @param access
      *            If set to <code>Resource.INTERFACE_REST</code>, only the
      *            rest documention is initialized, if set to
      *            <code>Resource.INTERFACE_SOAP</code> only the soap
      *            documention is initialized, and if set to
      *            <code>Resource.INTERFACE_BOTH</code> both documentions are
      *            initialized.
      * @param text
      *            The initial text.
      */
     private void initResult(final String access, final String text) {
 
         if (INTERFACE_REST.equals(access)) {
             this.restDocumentation = text;
         }
         else if (INTERFACE_SOAP.equals(access)) {
             this.soapDocumentation = text;
         }
         else if (INTERFACE_BOTH.equals(access)) {
             this.restDocumentation = text;
             this.soapDocumentation = text;
         }
     }
 
     /**
      * Append the given text to the resulting documentation.
      * 
      * @param access
      *            If set to <code>Resource.INTERFACE_REST</code>, only the
      *            rest documention is extended, if set to
      *            <code>Resource.INTERFACE_SOAP</code> only the soap
      *            documention is extended, and if set to
      *            <code>Resource.INTERFACE_BOTH</code> both documentions are
      *            extended.
      * @param text
      *            The text to append.
      */
     private void appendToResult(final String access, final String text) {
 
         if (INTERFACE_REST.equals(access)) {
             this.restDocumentation += text;
         }
         else if (INTERFACE_SOAP.equals(access)) {
             this.soapDocumentation += text;
         }
         else if (INTERFACE_BOTH.equals(access)) {
             this.restDocumentation += text;
             this.soapDocumentation += text;
         }
     }
 
     /**
      * Create the documentation for all given resources. The output is a docbook
      * chapter.
      * 
      */
     public void toDocbook() {
 
         LOG.info("[Resource " + name
             + "] Creating documentation for REST and SOAP interface.");
         initResult(INTERFACE_REST, getChapterStart(getTitle(INTERFACE_REST)));
         initResult(INTERFACE_SOAP, getChapterStart(getTitle(INTERFACE_SOAP)));
 
         Iterator<Node> resourceIter = resources.iterator();
         StringBuffer resourceOriented = new StringBuffer();
         StringBuffer taskOriented = new StringBuffer();
         StringBuffer soap = new StringBuffer();
         while (resourceIter.hasNext()) {
             Node resource = resourceIter.next();
             Node description =
                 getChild(resource, DOCUMENTATION_ELEMENT + XPATH_DELIMITER
                     + DESCRIPTION_ELEMENT);
 
             if (description != null) {
                 appendToResult(INTERFACE_BOTH,
                     getChapterDescription(description.getTextContent()));
             }
             try {
                 NodeList descriptors = parse(DESCRIPTOR_ELEMENT, resource);
                 String taskOrientedMethodsSelector = "@http=\"POST\"";
                 // first document the resource oriented methods
                 for (int i = 0; i < descriptors.getLength(); ++i) {
                     resourceOriented.append(
                         createDescriptorDocumentation(INTERFACE_REST,
                             descriptors.item(i), INVOKE_ELEMENT + "[not("
                                 + taskOrientedMethodsSelector + ")]"));
                 }
                 // now document the task oriented methods
 
                 descriptors = parse(DESCRIPTOR_ELEMENT, resource);
                 for (int i = 0; i < descriptors.getLength(); ++i) {
                     taskOriented.append(
                         createDescriptorDocumentation(INTERFACE_REST,
                             descriptors.item(i), INVOKE_ELEMENT + "["
                                 + taskOrientedMethodsSelector + "]"));
                 }
                 for (int i = 0; i < descriptors.getLength(); ++i) {
                     soap.append(
                         createDescriptorDocumentation(INTERFACE_SOAP,
                             descriptors.item(i), INVOKE_ELEMENT));
                 }
             }
             catch (TransformerException e) {
                 getLogger().info("No method mapping descriptors found!");
             }
         }
         if (!"".equals(resourceOriented.toString())) {
             appendToResult(INTERFACE_REST, getSectionStart(
                 "Resource oriented Methods", null));
             appendToResult(INTERFACE_REST, resourceOriented.toString());
             appendToResult(INTERFACE_REST, getSectionEnd());
         }
         if (!"".equals(taskOriented.toString())) {
             appendToResult(INTERFACE_REST,
                 getTemplate(TEMPLATE_PARA_PAGE_BREAK));
             appendToResult(INTERFACE_REST, getSectionStart(
                 "Task oriented Methods", "tasks"));
             appendToResult(INTERFACE_REST, taskOriented.toString());
             appendToResult(INTERFACE_REST, getSectionEnd());
         }
         if (!"".equals(soap.toString())) {
             appendToResult(INTERFACE_SOAP,
                 getTemplate(TEMPLATE_PARA_PAGE_BREAK));
             appendToResult(INTERFACE_SOAP, soap.toString());
         }
         appendToResult(INTERFACE_BOTH, getChapterEnd());
     }
 
     /**
      * @param access
      *            If set to <code>Resource.INTERFACE_REST</code>, only the
      *            rest documention is extended, if set to
      *            <code>Resource.INTERFACE_SOAP</code> only the soap
      *            documention is extended, and if set to
      *            <code>Resource.INTERFACE_BOTH</code> both documentions are
      *            extended.
      * @return The title of the documentation chapter.
      */
     private String getTitle(final String access) {
 
         String result = "Methods of Resource " + name;
         if (access.equals(INTERFACE_REST)) {
             result += " for REST Interface ";
         }
         else if (access.equals(INTERFACE_SOAP)) {
             result += " for SOAP Interface ";
         }
         return result;
     }
 
     /**
      * Create the documentation for the given descriptor node. A descriptor
      * contains invoke nodes which contain the mapping between a HTTP method
      * together with a uri to a method call (including the parameters) on the
      * resource handler.
      * 
      * @param access
      *            If set to <code>Resource.INTERFACE_REST</code>, only the
      *            rest documention is extended, if set to
      *            <code>Resource.INTERFACE_SOAP</code> only the soap
      *            documention is extended, and if set to
      *            <code>Resource.INTERFACE_BOTH</code> both documentions are
      *            extended.
      * @param descriptor
      *            The descriptor node.
      * @param xPath
      *            Selects which invoke sections of the descriptor should be
      *            inclluded in the documentation (e.g. all invoke whose
      *            attribute http hast the value 'POST').
      * @return The documentation for the given descriptor.
      */
     private String createDescriptorDocumentation(
         final String access, final Node descriptor, final String xPath) {
 
         String result = "";
         String uri =
             prepareParameter(getAttributeValue(descriptor, DESCRIPTOR_URI_ATTR));
         try {
             NodeList invokes = parse(xPath, descriptor);
             if ((invokes != null) && (invokes.getLength() > 0)) {
                 for (int i = 0; i < invokes.getLength(); ++i) {
                     boolean visible = VISIBILTY_DEFAULT;
                     Node value =
                         getChild(invokes.item(i), DOCUMENTATION_ELEMENT);
                     if (checkVisibility) {
                         if ((value != null)
                             && (getAttributeValue(value,
                                 DOCUMENTATION_VISIBLE_ATTR) != null)) {
                             if (getAttributeValue(value,
                                 DOCUMENTATION_VISIBLE_ATTR)
                                .equalsIgnoreCase("false")) {
                                 visible = false;
                             }
                             else if (getAttributeValue(value,
                                 DOCUMENTATION_VISIBLE_ATTR)
                                .equalsIgnoreCase("true")) {
                                 visible = true;
                             }
                         }
                     }
                     if ((value != null)
                         && (getAttributeValue(value,
                             DOCUMENTATION_AVAILABLE_ATTR) != null)) {
                         if ((getAttributeValue(value,
                             DOCUMENTATION_AVAILABLE_ATTR).toLowerCase()
                             .equals(access.toLowerCase()))
                             || (getAttributeValue(value,
                                 DOCUMENTATION_AVAILABLE_ATTR).toLowerCase()
                                 .equals(INTERFACE_BOTH.toLowerCase()))) {
                             visible = true;
                         }
                         else {
                             visible = false;
                         }
                     }
                     if (visible) {
                         String title = DEFAULT_TEXT;
                         String description = DEFAULT_TEXT;
                         Node child =
                             getChild(invokes.item(i), DOCUMENTATION_ELEMENT
                                 + XPATH_DELIMITER + TITLE_ELEMENT);
                         if (child != null) {
                             title = child.getTextContent();
                             if (title == null) {
                                 title = DEFAULT_TEXT;
                             }
                         }
                         child =
                             getChild(invokes.item(i), DOCUMENTATION_ELEMENT
                                 + XPATH_DELIMITER + DESCRIPTION_ELEMENT);
                         if (child != null) {
                             description = child.getTextContent();
                             if (description == null) {
                                 description = DEFAULT_TEXT;
                             }
                         }
                         result += getSectionStart(title, null);
                         result += getSectionDescription(description);
 
                         if (INTERFACE_REST.equals(access)) {
                             result +=
                                 createRestDocumentation(invokes.item(i), title,
                                     uri);
                         }
                         else if (INTERFACE_SOAP.equals(access)) {
                             result +=
                                 createSoapDocumentation(invokes.item(i), title);
                         }
 
                         result += getTemplate(TEMPLATE_PARA_PAGE_BREAK);
                         result += getSectionEnd();
                     }
                 }
             }
         }
         catch (TransformerException e) {
             result +=
                 "<para>Something went wrong! Caught Exception " + e.getClass()
                     + " with message " + e.getMessage() + "</para>";
             result += getSectionEnd();
         }
         return result;
     }
 
     /**
      * Create the documentation for the REST interface of an invoke node.
      * 
      * @param invoke
      *            The invoke node.
      * @param title
      *            The title.
      * @param uri
      *            The uri of the request.
      * @return The docbook documentation.
      */
     private String createRestDocumentation(
         final Node invoke, final String title, final String uri) {
 
         String result = "";
         if ("".equals(getAttributeValue(invoke, INVOKE_HTTP_ATTR))) {
             result +=
                 "<para/><para/><para><emphasis role=\"bold\">This method is"
                     + " not provided by the REST interface!</emphasis>"
                     + "</para><para/><para/>";
         }
         else {
             String httpMethod = getAttributeValue(invoke, INVOKE_HTTP_ATTR);
             result +=
                 getMethodTableStart(title + " via REST",
                     getMethodTableRow2Cols("HTTP Request", httpMethod + " "
                         + prepareUriParameter(uri)));
             int paramNo = 1;
             result += getMethodTableRow2Cols(" ", " ");
             boolean printedParam = false;
             String c1 = "";
             String attributeValue =
                 getAttributeValue(invoke, INVOKE_PARAM_ATTR + paramNo);
             if (attributeValue != null) {
                 String parameter = "";
                 while (attributeValue != null) {
                     String c2 = prepareParameter(attributeValue);
                     String c3 = DEFAULT_TEXT;
                     Node child =
                         getChild(invoke, DOCUMENTATION_ELEMENT
                             + XPATH_DELIMITER + PARAM_ELEMENT + XPATH_DELIMITER
                             + INVOKE_PARAM_ATTR + paramNo);
                     if (child != null) {
                         c3 = child.getTextContent();
                         if (c3 == null) {
                             c3 = DEFAULT_TEXT;
                         }
                     }
                     if (paramNo == 1) {
                         c1 = "Input from Uri";
                     }
                     else if ("Input from Body".equals(c1)) {
                         c1 = "Input from Uri";
                     }
                     if ((prepareParameter("${" + VAR_BODY + "}")
                         .equalsIgnoreCase(c2))
                         || (prepareParameter("${"
                             + VAR_BODY_LAST_MODIFICATION_DATE + "}")
                             .equalsIgnoreCase(c2))) {
                         if (!"".equals(parameter)) {
                             result += getMethodTableRow2Cols(c1, parameter);
                             parameter = "";
                         }
                         else {
                             result +=
                                 getMethodTableRow2Cols(c1, "No input values");
                         }
                         printedParam = true;
                         result += getMethodTableRow2Cols(" ", " ");
                         c1 = "Input from Body";
                         if (prepareParameter(
                             "${" + VAR_BODY_LAST_MODIFICATION_DATE + "}")
                             .equalsIgnoreCase(c2)) {
                             c3 = "<emphasis>timestamp</emphasis>: " + c3;
                         }
                         result += getMethodTableRow2Cols(c1, c3);
                     }
                     else {
                         String paramVar =
                             prepareParameter(getAttributeValue(invoke,
                                 INVOKE_PARAM_ATTR + paramNo));
                         if (uri.indexOf(paramVar) != -1) {
                             printedParam = true;
                             if (!"".equals(parameter)) {
                                 parameter += "<para/>";
                             }
                             parameter += c2 + ": " + c3;
                         }
                     }
                     paramNo++;
 
                     // prepare for next loop
                     attributeValue =
                         getAttributeValue(invoke, INVOKE_PARAM_ATTR + paramNo);
                 }
                 if (!"".equals(parameter)) {
                     result += getMethodTableRow2Cols(c1, parameter);
                     parameter = "";
                     printedParam = true;
                 }
             }
             if (!printedParam) {
                 result +=
                     getMethodTableRow2Cols("Parameter", "No input values");
             }
             Node child =
                 getChild(invoke, DOCUMENTATION_ELEMENT + XPATH_DELIMITER
                     + RESULT_ELEMENT);
             if (child != null) {
                 String output = child.getTextContent();
                 if (("".equals(output))
                     && ("void".equalsIgnoreCase(getAttributeValue(child,
                         RESULT_ATTR_TYPE)))) {
                     output = "No return value";
                 }
                 if (output != null) {
                     result += getMethodTableRow2Cols(" ", " ");
                     result += getMethodTableRow2Cols("Output", output);
 
                 }
             }
             if (isIncludeErrors()) {
                 result = handleIncludedErrors(invoke, title, result, paramNo);
             }
             result += getMethodTableEnd();
         }
         return result;
     }
 
     /**
      * Handles includeErrors.
      * 
      * @param invoke
      *            The invoke node.
      * @param title
      *            The title.
      * @param result
      *            The current value of a result string. To this value additional
      *            information will be appended and returned.
      * @param paramNo
      *            The parameter number.
      * @return Returns a result string.
      */
     private String handleIncludedErrors(
         final Node invoke, final String title, final String result,
         final int paramNo) {
 
         String ret = result;
 
         StringBuffer c1;
         try {
             Class[] exceptionTypes =
                 getExceptions(getAttributeValue(invoke, INVOKE_METHOD_ATTR),
                     paramNo - 1);
             if ((exceptionTypes != null) && (exceptionTypes.length > 0)) {
                 c1 = new StringBuffer();
                 StringBuffer c2 = new StringBuffer();
                 String msg = "";
                 for (int i = 0; i < exceptionTypes.length; ++i) {
                     try {
                         Method statusLine =
                             exceptionTypes[i].getMethod("getHttpStatusLine",
                                 new Class[0]);
                         msg =
                             (String) statusLine.invoke(exceptionTypes[i]
                                 .newInstance(), new Object[]{})
                                 + " (caused by "
                                 + exceptionTypes[i].getSimpleName() + ")";
                     }
                     catch (Exception e) {
                         msg =
                             HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                                 + " Internal eSciDoc Error";
                         LOG.info("[" + name + "]"
                             + ": Error generating documentation for "
                             + "[Rest: " + title + "] " + exceptionTypes[i]);
                     }
                     if (i == 0) {
                         ret += getMethodTableRow2Cols(" ", " ");
                         c1.append("Possible errors");
                         c2.append("<simplelist type=\"vert\" columns=\"1\">");
                         c2.append("<member>");
                         c2.append(msg);
                         c2.append("</member>");
                     }
                     else {
                         c2.append("<member>");
                         c2.append(msg);
                         c2.append("</member>");
                     }
                     if (i == exceptionTypes.length - 1) {
                         c2.append("</simplelist>");
                     }
                 }
                 ret += getMethodTableRow2Cols(c1.toString(), c2.toString());
             }
             else {
                 ret += getMethodTableRow2Cols(" ", " ");
                 ret +=
                     getMethodTableRow2Cols("Possible errors ",
                         "No errors found.");
             }
         }
         catch (NoSuchMethodException e) {
             ret += getMethodTableRow2Cols(" ", " ");
             ret +=
                 getMethodTableRow2Cols("Possible errors ", "No errors found.");
             ret += getMethodTableRow2Cols(" ", " ");
             ret +=
                 getMethodTableRow2Cols(
                     "<emphasis role=\"bold\">Status</emphasis>",
                     "<emphasis role=\"bold\">Method is not "
                         + "implemented in service interface.</emphasis>");
         }
         return ret;
     }
 
     /**
      * Create the documentation for the SOAP interface of an invoke node.
      * 
      * @param invoke
      *            The invoke node.
      * @param title
      *            The title.
      * @return The docbook documentation.
      */
     private String createSoapDocumentation(final Node invoke, final String title) {
         String result = "";
 
         String method =
             name + HANDLER_POSTFIX + SERVICE_POSTFIX + "."
                 + getAttributeValue(invoke, INVOKE_METHOD_ATTR) + " ( ";
         String paramDocumentation = "";
 
         int paramNo = 1;
 
         String attributeValue =
             getAttributeValue(invoke, INVOKE_PARAM_ATTR + paramNo);
         if (attributeValue != null) {
             String parameter = "";
             String c1 = "";
             while (attributeValue != null) {
                 Node child =
                     getChild(invoke, DOCUMENTATION_ELEMENT + XPATH_DELIMITER
                         + PARAM_ELEMENT + XPATH_DELIMITER + INVOKE_PARAM_ATTR
                         + paramNo);
                 String c2 = prepareParameter(attributeValue);
                 String c3 = DEFAULT_TEXT;
 
                 if (child != null) {
                     if (getAttributeValue(child, PARAM_NAME_ATTR) != null) {
                         c2 = getAttributeValue(child, PARAM_NAME_ATTR);
                     }
                     c3 = child.getTextContent();
                     if (c3 == null) {
                         c3 = DEFAULT_TEXT;
                     }
                 }
                 if (paramNo == 1) {
                     c1 = "Parameter";
                     parameter += "<emphasis>" + c2 + "</emphasis>: " + c3;
                 }
                 else {
                     method += ", ";
                     parameter +=
                         "<para/><emphasis>" + c2 + "</emphasis>: " + c3;
                 }
                 method += "String " + c2;
                 paramNo++;
                 // prepare for next loop
                 attributeValue =
                     getAttributeValue(invoke, INVOKE_PARAM_ATTR + paramNo);
             }
             paramDocumentation += getMethodTableRow2Cols(c1, parameter);
             method += " )";
         }
         else {
             method += ")";
             paramDocumentation +=
                 getMethodTableRow2Cols("Parameter", "No input values");
         }
         Node child =
             getChild(invoke, DOCUMENTATION_ELEMENT + XPATH_DELIMITER
                 + RESULT_ELEMENT);
         if (child != null) {
             String output = child.getTextContent();
 
             if (("".equals(output))
                 && ("void".equalsIgnoreCase(getAttributeValue(child,
                     RESULT_ATTR_TYPE)))) {
                 output = "No return value";
             }
             if (output != null) {
                 paramDocumentation += getMethodTableRow2Cols(" ", " ");
                 method =
                     getAttributeValue(child, RESULT_ATTR_TYPE) + " " + method;
                 paramDocumentation += getMethodTableRow2Cols("Output", output);
 
             }
         }
         result +=
             getMethodTableStart(title + " via Soap", getMethodTableRow2Cols(
                 "Method Signature", method));
         result += getMethodTableRow2Cols(" ", " ");
         result += paramDocumentation;
         if (isIncludeErrors()) {
             try {
                 Class[] exceptionTypes =
                     getExceptions(
                         getAttributeValue(invoke, INVOKE_METHOD_ATTR),
                         paramNo - 1);
                 if ((exceptionTypes != null) && (exceptionTypes.length > 0)) {
                     String c1 = "";
                     String c2 = "";
                     for (int i = 0; i < exceptionTypes.length; ++i) {
                         if (i == 0) {
                             result += getMethodTableRow2Cols(" ", " ");
                             c1 = "Possible errors";
                             c2 =
                                 "<simplelist type=\"vert\" columns=\"1\"><member>"
                                     + exceptionTypes[i].getSimpleName()
                                     + "</member>";
                         }
                         else {
                             c2 +=
                                 "<member>" + exceptionTypes[i].getSimpleName()
                                     + "</member>";
                         }
                         if (i == exceptionTypes.length - 1) {
                             c2 += "</simplelist>";
                         }
                     }
                     result += getMethodTableRow2Cols(c1, c2);
                 }
                 else {
                     result += getMethodTableRow2Cols(" ", " ");
                     result +=
                         getMethodTableRow2Cols("Possible errors ",
                             "No errors found.");
                 }
             }
             catch (NoSuchMethodException e) {
                 result += getMethodTableRow2Cols(" ", " ");
                 result +=
                     getMethodTableRow2Cols("Possible errors ",
                         "No errors found.");
                 result += getMethodTableRow2Cols(" ", " ");
                 result +=
                     getMethodTableRow2Cols(
                         "<emphasis role=\"bold\">Status</emphasis>",
                         "<emphasis role=\"bold\">Method is not implemented "
                             + "in service interface.</emphasis>");
             }
         }
         // child =
         // getChild(invoke, DOCUMENTATION_ELEMENT + XPATH_DELIMITER
         // + PREVIOUS_ELEMENT);
         // if (child != null) {
         // String previous = child.getTextContent();
         // if (previous != null) {
         // result += getMethodTableRow2Cols(" ", " ");
         // result += getMethodTableRow2Cols("Previous", previous);
         // }
         // }
         result += getMethodTableEnd();
         return result;
     }
 
     /**
      * Get an array containing the Exceptions declared in the throws section of
      * the given method with the matching number of parameters in its signature.
      * 
      * @param method
      *            The method name.
      * @param noOfParams
      *            The numbe of params of teh method.
      * @return Thearray containing the Exceptions.
      * @throws NoSuchMethodException
      *             If the method was not found in the resources handler.
      */
     private Class[] getExceptions(final String method, final int noOfParams)
         throws NoSuchMethodException {
 
         Class[] parameterTypes = null;
         if (noOfParams > 0) {
             parameterTypes = new Class[noOfParams];
             for (int i = 0; i < noOfParams; ++i) {
                 parameterTypes[i] = String.class;
             }
         }
         java.lang.reflect.Method invoked =
             getInstance().getMethod(method, parameterTypes);
         return invoked.getExceptionTypes();
     }
 
     /**
      * Creates the docbook representation of a parameter. Replaces every
      * occurence of '${' in the given param with '<emphasis>' and every
      * occurence of '}' with '</emphasis>'.
      * 
      * @param parameter
      *            The parameter.
      * @return The docbook representation of a parameter.
      */
     private String prepareParameter(final String parameter) {
 
         String result = parameter.toLowerCase();
         result = result.replaceAll("\\$\\{", "<emphasis>");
         result = result.replaceAll("\\}", "</emphasis>");
         return result;
     }
 
     /**
      * Creates the docbook representation of an uri parameter for all text
      * fragments surrounded with an <emphasis></emphasis> node. The content 
      * of the emphasis element is surrounded with &#60; and &#62;.
      * @param text The text.
      * @return The text with the replaced text fragments.
      */
     private String prepareUriParameter(final String text) {
 
         String result = text.toLowerCase();
         result = result.replaceAll("<emphasis>", "<emphasis>" + LESS_THAN);
         result = result.replaceAll("</emphasis>", GREATER_THAN + "</emphasis>");
         return result;
     }
 
     /**
      * Get the start of a docbook chapter.
      * 
      * @param title
      *            The title of the chapter.
      * @return The start of the docbook chapter.
      */
     private String getChapterStart(final String title) {
 
         String result = getTemplate(TEMPLATE_CHAPTER_START);
         result = result.replaceAll(VAR_TITLE, title);
         return result;
     }
 
     /**
      * Get the end of a docbook chapter.
      * 
      * @return The end of the docbook chapter.
      */
     private String getChapterEnd() {
 
         return getTemplate(TEMPLATE_CHAPTER_END);
     }
 
     /**
      * Get a description node for a docbook chapter.
      * 
      * @param text
      *            The description text.
      * @return The description node for a docbook chapter.
      */
     private String getChapterDescription(final String text) {
         String result = getTemplate(TEMPLATE_CHAPTER_DESCRIPTION);
         result = result.replaceAll(VAR_DESCRIPTION, text);
         return result;
     }
 
     /**
      * Get the start of a docbook section.
      * 
      * @param title
      *            The title of the section.
      * @param id
      *            If not null the id is set as section id.
      * @return The start of the docbook section.
      */
     private String getSectionStart(final String title, final String id) {
 
         String result = getTemplate(TEMPLATE_SECTION_START);
         if (id == null) {
             result = result.replaceAll(VAR_TITLE, title);
         }
         else {
             result = getTemplate(TEMPLATE_SECTION_WITH_ID_START);
             result = result.replaceAll(VAR_TITLE, title);
             result = result.replaceAll(VAR_ID, id);
         }
         return result;
     }
 
     /**
      * Get the end of a docbook section.
      * 
      * @return The end of the docbook section.
      */
     private String getSectionEnd() {
 
         return getTemplate(TEMPLATE_SECTION_END);
     }
 
     /**
      * Get a description node for a docbook section.
      * 
      * @param description
      *            The description text.
      * @return The description node for a docbook section.
      */
     private String getSectionDescription(final String description) {
         String result = getTemplate(TEMPLATE_SECTION_DESCRIPTION);
         result = result.replaceAll(VAR_DESCRIPTION, description);
         return result;
     }
 
     /**
      * Get the start of a method table.
      * 
      * @param title
      *            The title of the table.
      * @param headerRowText
      *            The text of the header row.
      * @return The start of the method table.
      */
     private String getMethodTableStart(
         final String title, final String headerRowText) {
         String result = getTemplate(TEMPLATE_METHOD_TABLE_START);
         result = result.replaceAll(VAR_TITLE, title);
         result = result.replaceAll(VAR_HEADER_ROW, headerRowText);
         return result;
     }
 
     /**
      * Get a method table row with two columns.
      * 
      * @param col1Text
      *            The text for the 1st column.
      * @param col2Text
      *            The text for the 2nd column.
      * @return The method table row with two columns.
      */
     private String getMethodTableRow2Cols(
         final String col1Text, final String col2Text) {
         String result = getTemplate(TEMPLATE_METHOD_TABLE_ROW_2_COLS);
         result = result.replaceAll(VAR_KEY, col1Text);
         result = result.replaceAll(VAR_VALUE, col2Text);
         return result;
     }
 
     /**
      * @return The end of a method table.
      */
     private String getMethodTableEnd() {
 
         return getTemplate(TEMPLATE_METHOD_TABLE_END);
     }
 
     /**
      * Get a template from a file.
      * 
      * @param filename
      *            The name of the template file.
      * @return The template.
      */
     private String getTemplate(final String filename) {
 
         String result = null;
         try {
             result = getFileContents(TEMPLATE_PATH + "/" + filename) + CR_LF;
         }
         catch (IOException e) {
             getLogger().error(
                 "Template '" + TEMPLATE_PATH + "/" + filename + "' not found!");
         }
 
         return result;
     }
 
     /**
      * Get an instance of the resource handler for this resource. The resources
      * name is save in the filed <code>name</code>.
      * 
      * @return The instance of the resource handler.
      */
     final Class getInstance() {
 
         String beanName = "service." + name + "HandlerBean";
         return getConfiguredClass(beanName);
     }
 
     /**
      * Get the configured class for the given springBeanName.
      * 
      * @param springBeanName
      *            The name of teh spring bean.
      * @return The class.
      */
     private Class getConfiguredClass(final String springBeanName) {
 
         Class result = null;
         try {
             String definitionsFile =
                 "/de/escidoc/core/common/documentation/definitions.xml";
             Document beans = getDocument(definitionsFile);
 
             Node resultNode =
                 getChild(beans, "/beans/bean[@id=\"" + springBeanName + "\"]");
             String className = getAttributeValue(resultNode, "class");
             result = Class.forName(className);
 
         }
         catch (Exception e) {
             e.printStackTrace();
         }
 
         return result;
     }
 
     /**
      * @return Returns the includeErrors.
      */
     public boolean isIncludeErrors() {
         return includeErrors;
     }
 
     /**
      * @param includeErrors
      *            The includeErrors to set.
      */
     public void setIncludeErrors(final boolean includeErrors) {
         this.includeErrors = includeErrors;
     }
 
     /**
      * @return Returns the checkVisibility.
      */
     public boolean isCheckVisibility() {
         return checkVisibility;
     }
 
     /**
      * @param checkVisibility
      *            The checkVisibility to set.
      */
     public void setCheckVisibility(final boolean checkVisibility) {
         this.checkVisibility = checkVisibility;
     }
 
     /**
      * @return the restDocumentation
      */
     public String getRestDocumentation() {
         return restDocumentation;
     }
 
     /**
      * @return the soapDocumentation
      */
     public String getSoapDocumentation() {
         return soapDocumentation;
     }
 }
