 /**
  * Copyright (C) 2000 - 2009 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of
  * the GPL, you may redistribute this Program in connection with Free/Libre
  * Open Source Software ("FLOSS") applications as described in Silverpeas's
  * FLOSS exception.  You should have received a copy of the text describing
  * the FLOSS exception, and it is also available here:
  * "http://repository.silverpeas.com/legal/licensing"
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.silverpeas.applicationbuilder;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.jdom.Content;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 
 /**
  * this descriptor is created in memory. It is filled with the descriptor parts from the WARParts.
  * Finally, it is integrated in the WAR by the means of a stream.
  * @author Silverpeas
  * @version 1.0/B
  * @since 1.0/B
  */
 public class WARDescriptor extends XmlDocument {
 
   /**
    * @since 1.0/B
    */
   private static final String NAME = "web.xml";
   /**
    * @since 1.0/B
    */
   private static final String LOCATION = "WEB-INF";
   private static final String ROOT_ELT = "web-app";
   private static final String SERVLET_VERSION = "2.4";
   private static final String ROOT_NAMESPACE = "http://java.sun.com/xml/ns/j2ee";
   private static final String[][] ROOT_ADDITIONAL_NAMESPACE = { { "xsi",
       "http://www.w3.org/2001/XMLSchema-instance" } };
   private static final String NAME_ELT = "display-name";
   private static final String SERVLET_ELT = "servlet";
   private static final String DESC_ELT = "description";
   private static final String[] TAGS_TO_MERGE =
       { "context-param", "filter", "filter-mapping",
       "listener", SERVLET_ELT, "servlet-mapping", "session-config", "jsp-config",
      "error-page", "security-constraint" };
   private static final String[] TAGS_TO_SORT =
       { NAME_ELT, DESC_ELT, "context-param", "filter",
       "filter-mapping", "listener", SERVLET_ELT, "servlet-mapping", "session-config",
      "jsp-config", "error-page", "security-constraint" };
   private static final String[] SERVLET_TAGS = { "display-name", "servlet-name", "servlet-class",
       "init-param", "load-on-startup", "param-name", "param-value" };
 
   public WARDescriptor() {
     super(LOCATION, NAME);
     setDocument();
   }
 
   /**
    * @roseuid 3AAE4499010D
    */
   public void mergeWARPartDescriptor(XmlDocument descriptor)
       throws AppBuilderException {
     mergeWith(TAGS_TO_MERGE, descriptor);
   }
 
   /**
    * Theorically, XML contents is not sorted. Actually, this descriptor must be sorted to work well
    * in an application server.
    */
   public void sort() throws AppBuilderException {
     /**
      * gets the resulting document from the master document. Cloning the document is important. If
      * you clone or copy an element, the copy keeps his owner and, as a result, the element appears
      * twice in the document
      */
     Element root = getDocument().getRootElement();
 
     Element tempRoot = (Element) root.clone();
     tempRoot.detach();
     tempRoot.removeContent();
 
     /** Makes groups of elements by tag */
     List eltLstLst = new ArrayList(TAGS_TO_SORT.length);
     for (int iTag = 0; iTag < TAGS_TO_SORT.length; iTag++) {
       List children = root.getChildren(TAGS_TO_SORT[iTag], root.getNamespace());
       List eltLst = new ArrayList();
       if (children != null && !children.isEmpty()) {
         for (Object child : children) {
           if (child instanceof Content) {
             Content newElement = (Content) ((Content) child).clone();
             if (newElement instanceof Element
                 && SERVLET_ELT.equalsIgnoreCase(((Element) newElement).getName())) {
               eltLst.add(sort((Element) newElement, SERVLET_TAGS));
             } else {
               newElement.detach();
               eltLst.add(newElement);
             }
           }
         }
       }
       eltLstLst.add(iTag, eltLst);
     }
     /** Orders the content of the resulting document */
     for (int iTag = 0; iTag < TAGS_TO_SORT.length; iTag++) {
 
       if (!((List) eltLstLst.get(iTag)).isEmpty()) {
         tempRoot.addContent((List) eltLstLst.get(iTag));
       }
     }
 
     /** the result */
     setDocument(new Document(tempRoot));
   }
 
   private void setDocument() {
     Namespace nameSpace = Namespace.getNamespace(ROOT_NAMESPACE);
     Element root = new Element(ROOT_ELT, nameSpace);
     root.setAttribute("version", SERVLET_VERSION);
     for (int i = 0; i < ROOT_ADDITIONAL_NAMESPACE.length; i++) {
       root.addNamespaceDeclaration(Namespace.getNamespace(
           ROOT_ADDITIONAL_NAMESPACE[i][0], ROOT_ADDITIONAL_NAMESPACE[i][1]));
     }
     Namespace xsiNamespace = Namespace.getNamespace("xsi",
         "http://www.w3.org/2001/XMLSchema-instance");
     root.setAttribute("schemaLocation",
         "http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd",
         xsiNamespace);
     Element name = new Element(NAME_ELT, nameSpace);
     name.setText(ApplicationBuilder.getApplicationName());
     root.addContent(name);
     Element desc = new Element(DESC_ELT, nameSpace);
     desc.setText(ApplicationBuilder.getApplicationDescription());
     root.addContent(desc);
     Document doc = new Document(root);
 
     super.setDocument(doc);
 
   }
 }
