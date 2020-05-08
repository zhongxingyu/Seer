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
  * Copyright 2006-2009 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.adm.business.admin;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.MessageFormat;
 import java.util.Date;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.w3c.dom.Document;
 
 import de.escidoc.core.cmm.service.interfaces.ContentModelHandlerInterface;
 import de.escidoc.core.common.business.fedora.resources.ResourceType;
 import de.escidoc.core.common.util.service.BeanLocator;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.om.service.interfaces.ContainerHandlerInterface;
 import de.escidoc.core.om.service.interfaces.ContextHandlerInterface;
 import de.escidoc.core.om.service.interfaces.ItemHandlerInterface;
 import de.escidoc.core.oum.service.interfaces.OrganizationalUnitHandlerInterface;
 
 /**
  * Load some example objects as resource XMLs into the eSciDoc repository.
  * 
  * @author sche
  */
 public class Examples {
     private static final String EXAMPLE_CONTAINER = "container.xml";
 
     private static final String EXAMPLE_CONTENT_MODEL = "content-model.xml";
 
     private static final String EXAMPLE_CONTEXT = "context.xml";
 
     private static final String EXAMPLE_ITEM = "item.xml";
 
     private static final String EXAMPLE_OU = "organizational-unit.xml";
 
     private final String directory;
 
     /**
      * Create a new Examples object.
      * 
      * @param directory
      *            URL to the directory which contains the eSciDoc XML files
      *            (including the trailing slash).
      */
     public Examples(final String directory) {
         this.directory = directory;
     }
 
     /**
      * Create an XML snippet for a message that can be displayed on the Web
      * page.
      * 
      * @param message
      *            message text
      * @return XML snippet
      */
     private static String createMessage(final String message) {
         return "<message>" + message + "</message>";
     }
 
     /**
      * Create a snippet for a task parameter XML including the last modification
      * date.
      * 
      * @param lastModificationDate
      *            the last modification date
      * @return XML snippet
      */
     private static String createTaskParam(final String lastModificationDate) {
         return "<param last-modification-date=\"" + lastModificationDate
             + "\"/>";
     }
 
     /**
      * Extract the last modification date from the given result XML.
      * 
      * @param xml
      *            result XML
      * @return last modification date
      * @throws Exception
      *             thrown if the XPath evaluation failed
      */
     private static String getLastModificationDate(final String xml)
         throws Exception {
         String result = null;
 
         if (xml != null) {
             ByteArrayInputStream input = null;
 
             try {
                 input =
                     new ByteArrayInputStream(
                         xml.getBytes(XmlUtility.CHARACTER_ENCODING));
 
                 DocumentBuilder db =
                     DocumentBuilderFactory.newInstance().newDocumentBuilder();
                 Document xmlDom = db.parse(input);
                 XPath xpath = XPathFactory.newInstance().newXPath();
 
                 result =
                     xpath.evaluate("/result/@last-modification-date", xmlDom);
             }
             finally {
                 if (input != null) {
                     input.close();
                 }
             }
         }
         return result;
     }
 
     /**
      * Extract the last modification date from the given resource XML.
      * 
      * @param xml
      *            resource XML (item XML, container XML, ...)
      * @param type
      *            resource type
      * @return last modification date
      * @throws Exception
      *             thrown if the XPath evaluation failed
      */
     private static String getLastModificationDate(
         final String xml, final ResourceType type) throws Exception {
         String result = null;
 
         if (xml != null) {
             ByteArrayInputStream input = null;
 
             try {
                 input =
                     new ByteArrayInputStream(
                         xml.getBytes(XmlUtility.CHARACTER_ENCODING));
 
                 DocumentBuilder db =
                     DocumentBuilderFactory.newInstance().newDocumentBuilder();
                 Document xmlDom = db.parse(input);
                 XPath xpath = XPathFactory.newInstance().newXPath();
 
                 result =
                     xpath.evaluate("/" + type.getLabel()
                         + "/@last-modification-date", xmlDom);
             }
             finally {
                 if (input != null) {
                     input.close();
                 }
             }
         }
         return result;
     }
 
     /**
      * Extract the object id from the given resource XML.
      * 
      * @param xml
      *            resource XML (item XML, container XML, ...)
      * @param type
      *            resource type
      * @return object id
      * @throws Exception
      *             thrown if the XPath evaluation failed
      */
     private static String getObjectId(final String xml, final ResourceType type)
         throws Exception {
         String result = null;
 
         if (xml != null) {
             ByteArrayInputStream input = null;
 
             try {
                 input =
                     new ByteArrayInputStream(
                         xml.getBytes(XmlUtility.CHARACTER_ENCODING));
 
                 DocumentBuilder db =
                     DocumentBuilderFactory.newInstance().newDocumentBuilder();
                 Document xmlDom = db.parse(input);
                 XPath xpath = XPathFactory.newInstance().newXPath();
                 String href =
                    xpath.evaluate(
                        "/" + type.getLabel() + "/@href|/" + type.getLabel()
                            + "/@objid", xmlDom);
 
                 result = href.substring(href.lastIndexOf('/') + 1);
             }
             finally {
                 if (input != null) {
                     input.close();
                 }
             }
 
         }
         return result;
     }
 
     /**
      * Load all example objects.
      * 
      * @return some useful information to the user which objects were loaded
      * @throws Exception
      *             thrown in case of an internal error
      */
     public String load() throws Exception {
         StringBuffer result = new StringBuffer();
 
         String ouId = loadOrganizationalUnit(loadFile(directory + EXAMPLE_OU));
 
         result.append(createMessage("created " + ResourceType.OU.getLabel()
             + ": " + ouId));
 
         String contextId =
             loadContext(loadFile(directory + EXAMPLE_CONTEXT), ouId);
 
         result.append(createMessage("created "
             + ResourceType.CONTEXT.getLabel() + ": " + contextId));
 
         String contentModelId =
             loadContentModel(loadFile(directory + EXAMPLE_CONTENT_MODEL));
 
         result.append(createMessage("created "
             + ResourceType.CONTENT_MODEL.getLabel() + ": " + contentModelId));
 
         String containerId =
             loadContainer(loadFile(directory + EXAMPLE_CONTAINER), contextId,
                 contentModelId);
 
         result.append(createMessage("created "
             + ResourceType.CONTAINER.getLabel() + ": " + containerId));
 
         String itemId =
             loadItem(loadFile(directory + EXAMPLE_ITEM), contextId,
                 contentModelId, containerId);
 
         result.append(createMessage("created " + ResourceType.ITEM.getLabel()
             + ": " + itemId));
 
         return result.toString();
     }
 
     /**
      * Load the example container.
      * 
      * @param xml
      *            container XML
      * @param contextId
      *            context id
      * @param contentModelId
      *            content model id
      * @return object id of the newly created container
      * @throws Exception
      *             thrown in case of an internal error
      */
     private String loadContainer(
         final String xml, final String contextId, final String contentModelId)
         throws Exception {
         String result = null;
         ContainerHandlerInterface handler =
             BeanLocator.locateContainerHandler();
 
         if (handler != null) {
             String createXml =
                 handler.create(MessageFormat.format(xml, new Object[] {
                     contextId, contentModelId }));
 
             result = getObjectId(createXml, ResourceType.CONTAINER);
         }
         return result;
     }
 
     /**
      * Load the example content model.
      * 
      * @param xml
      *            content model XML
      * @return object id of the newly created content model
      * @throws Exception
      *             thrown in case of an internal error
      */
     private String loadContentModel(final String xml) throws Exception {
         String result = null;
         ContentModelHandlerInterface handler =
             BeanLocator.locateContentModelHandler();
 
         if (handler != null) {
             String createXml = handler.create(xml);
 
             result = getObjectId(createXml, ResourceType.CONTENT_MODEL);
         }
         return result;
     }
 
     /**
      * Load the example context.
      * 
      * @param xml
      *            context XML
      * @param ouId
      *            organizational unit id
      * @return object id of the newly created context
      * @throws Exception
      *             thrown in case of an internal error
      */
     private String loadContext(final String xml, final String ouId)
         throws Exception {
         String result = null;
         ContextHandlerInterface handler = BeanLocator.locateContextHandler();
 
         if (handler != null) {
             String createXml =
                 handler.create(MessageFormat.format(xml, new Object[] {
                     new Date().getTime(), ouId }));
 
             result = getObjectId(createXml, ResourceType.CONTEXT);
             handler.open(
                 result,
                 createTaskParam(getLastModificationDate(createXml,
                     ResourceType.CONTEXT)));
         }
         return result;
     }
 
     /**
      * Load the file from the given URL into a string.
      * 
      * @param url
      *            file URL
      * @return string which contains the file content
      * @throws IOException
      *             thrown if the file couldn't be loaded
      */
     private String loadFile(final String url) throws IOException {
        
     	String result="";
     	
         if (url != null) {
             HttpGet method = null;
             method = new HttpGet(url);
 
             HttpClient client = new DefaultHttpClient();
             HttpResponse res = client.execute(method);
             HttpEntity entity = res.getEntity();
             result = EntityUtils.toString(entity);
             if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                 throw new IOException(EntityUtils.toString(entity));
             }
 
         }
         return result;
     }
 
     /**
      * Load the example item.
      * 
      * @param xml
      *            item XML
      * @param contextId
      *            context id
      * @param contentModelId
      *            content model id
      * @param containerId
      *            container id
      * @return object id of the newly created item
      * @throws Exception
      *             thrown in case of an internal error
      */
     private String loadItem(
         final String xml, final String contextId, final String contentModelId,
         final String containerId) throws Exception {
         String result = null;
         ContainerHandlerInterface containerHandler =
             BeanLocator.locateContainerHandler();
         ItemHandlerInterface itemHandler = BeanLocator.locateItemHandler();
 
         if ((containerHandler != null) && (itemHandler != null)) {
             String createXml =
                 containerHandler.createItem(
                     containerId,
                     MessageFormat.format(xml, new Object[] { contextId,
                         contentModelId }));
 
             result = getObjectId(createXml, ResourceType.ITEM);
 
             String submitXml =
                 itemHandler.submit(
                     result,
                     createTaskParam(getLastModificationDate(createXml,
                         ResourceType.ITEM)));
             String objectPidXml =
                 itemHandler.assignObjectPid(result,
                     createTaskParam(getLastModificationDate(submitXml)));
             String versionPidXml =
                 itemHandler.assignVersionPid(result,
                     createTaskParam(getLastModificationDate(objectPidXml)));
 
             itemHandler.release(result,
                 createTaskParam(getLastModificationDate(versionPidXml)));
         }
         return result;
     }
 
     /**
      * Load the example organizational unit.
      * 
      * @param xml
      *            organizational unit XML
      * @return object id of the newly created organizational unit
      * @throws Exception
      *             thrown in case of an internal error
      */
     private String loadOrganizationalUnit(final String xml) throws Exception {
         String result = null;
         OrganizationalUnitHandlerInterface handler =
             BeanLocator.locateOrganizationalUnitHandler();
 
         if (handler != null) {
             String createXml = handler.create(xml);
 
             result = getObjectId(createXml, ResourceType.OU);
             handler.open(
                 result,
                 createTaskParam(getLastModificationDate(createXml,
                     ResourceType.OU)));
         }
         return result;
     }
}
