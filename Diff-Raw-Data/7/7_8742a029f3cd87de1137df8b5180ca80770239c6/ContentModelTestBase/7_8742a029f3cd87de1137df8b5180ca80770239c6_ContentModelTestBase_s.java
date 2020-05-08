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
 package de.escidoc.core.test.cmm.contentmodel;
 
 import de.escidoc.core.test.EntityUtil;
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.cmm.CmmTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.HttpHelper;
 import de.escidoc.core.test.om.OmTestBase;
 import org.apache.http.HttpResponse;
 import org.apache.http.protocol.HTTP;
 import org.springframework.http.MediaType;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Test the implementation of the Content Model.
  * 
  * @author Frank Schwichtenberg
  */
 public class ContentModelTestBase extends CmmTestBase {
 
     /**
      * Test retrieving an organizational unit from the framework.
      * 
      * @param id
      *            The id of the organizational unit.
      * @return The retrieved organizational unit.
      * @throws Exception
      *             If anything fails.
      */
     public String retrieve(final String id) throws Exception {
         return handleXmlResult(getContentModelClient().retrieve(id));
     }
 
     public String create(final String xml) throws Exception {
         return handleXmlResult(getContentModelClient().create(xml));
     }
 
     public String update(final String id, final String xml) throws Exception {
         return handleXmlResult(getContentModelClient().update(id, xml));
     }
 
     public void delete(final String id) throws Exception {
         getContentModelClient().delete(id);
     }
 
     public String getContentModelTitle(Document contentModel) throws Exception {
         return selectSingleNode(contentModel, "/content-model/properties/name/text()").getNodeValue();
     }
 
     public String getContentModelDescription(Document contentModel) throws Exception {
         return selectSingleNode(contentModel, "/content-model/properties/description/text()").getNodeValue();
     }
 
     public Map<String, String> getContentModelMdRecordDefinitions(Document contentModel) throws Exception {
 
         NodeList mdRecordDefinitionNames =
             selectNodeList(contentModel, "/content-model/md-record-definitions/md-record-definition/@name");
         Map<String, String> mdRecordDefinitions = new HashMap<String, String>();
         int c = mdRecordDefinitionNames.getLength();
         for (int i = 0; i < c; i++) {
             String name = mdRecordDefinitionNames.item(i).getNodeValue();
             String xsd =
                 selectSingleNode(
                     contentModel,
                     "/content-model/md-record-definitions/" + "md-record-definition[@name = '" + name
                         + "']/schema/@href").getNodeValue();
             mdRecordDefinitions.put(name, xsd);
         }
 
         return mdRecordDefinitions;
     }
 
     public List<String> getContentModelResourceDefinitions(Document contentModel) throws Exception {
 
         NodeList resourceDefinitionNames =
             selectNodeList(contentModel, "/content-model/resource-definitions/resource-definition/@name");
         List<String> resourceDefinitions = new Vector<String>();
         int c = resourceDefinitionNames.getLength();
         for (int i = 0; i < c; i++) {
             resourceDefinitions.add(resourceDefinitionNames.item(i).getNodeValue());
         }
 
         return resourceDefinitions;
     }
 
     public List<List<String>> getContentModelContentStreamDefinitions(Document contentModel) throws Exception {
 
         NodeList contentStreamNames =
             selectNodeList(contentModel, "/content-model/content-streams/content-stream/@name");
         List<List<String>> contentStreamDefinitions = new Vector<List<String>>();
         int c = contentStreamNames.getLength();
         for (int i = 0; i < c; i++) {
             String name = contentStreamNames.item(i).getNodeValue();
             List<String> contentStreamDefinition = new Vector<String>();
             contentStreamDefinition.add(0, name);
             contentStreamDefinition.add(1, selectSingleNode(contentModel,
                 "/content-model/content-streams/content-stream[@name = '" + name + "']/@mime-type").getNodeValue());
             contentStreamDefinition.add(2, selectSingleNode(contentModel,
                 "/content-model/content-streams/content-stream[@name = '" + name + "']/@storage").getNodeValue());
             contentStreamDefinitions.add(contentStreamDefinition);
         }
 
         return contentStreamDefinitions;
     }
 
     /**
      * Test retrieving resources from the framework.
      * 
      * @param id
      *            The id of the organizational unit.
      * @return The retrieved list of resources.
      * @throws Exception
      *             If anything fails.
      */
     public String retrieveResources(final String id) throws Exception {
         return handleXmlResult(getContentModelClient().retrieveResources(id));
     }
 
     public String retrieveProperties(final String id) throws Exception {
         return handleXmlResult(getContentModelClient().retrieveProperties(id));
     }
 
     /**
      * Retrieve the list of content models.
      * 
      * @param filter
      *            CQL filter
      * @return The retrieved content models.
      * @throws Exception
      *             If anything fails.
      */
     public String retrieveContentModels(final Map<String, String[]> filter) throws Exception {
 
         return handleXmlResult(getContentModelClient().retrieveContentModels(filter));
     }
 
     public String retrieveContentStreams(final String id) throws Exception {
         return handleXmlResult(getContentModelClient().retrieveContentStreams(id));
     }
 
     public String retrieveContentStream(final String id, final String name) throws Exception {
         return handleXmlResult(getContentModelClient().retrieveContentStream(id, name));
     }
 
     /**
      * Test retrieving the version history of a Content Model.
      * 
      * @param id
      *            The id of the Content Model.
      * @return The retrieved version history.
      * @throws Exception
      *             If anything fails.
      */
     public String retrieveVersionHistory(final String id) throws Exception {
 
         return handleXmlResult(getContentModelClient().retrieveVersionHistory(id));
     }
 
     /**
      * Validate Content Model XML.
      * 
      * @param createdXML
      *            The XML to be validated.
      * @param title
      *            Expected titel.
      * @param description
      *            Expected description.
      * @param mdRecordDefinitions
      *            Expected Content Stream definitions.
      * @param resourceDefinitions
      *            Expected Content Stream definitions.
      * @param contentStreamDefinitions
      *            Expected Content Stream definitions.
      * @param checkBehavior
      *            Wether behavior should be checked or not.
      * @throws Exception
      *             If anything fails.
      */
     protected void validateContentModel(
         String createdXML, String title, String description, Map<String, String> mdRecordDefinitions,
         List<String> resourceDefinitions, List<List<String>> contentStreamDefinitions, boolean checkBehavior)
         throws Exception {
         assertXmlValidContentModel(createdXML);
 
         Document contentModel = getDocument(createdXML);
         String id = getObjidValue(contentModel);
 
         String lastModificationDate = getLastModificationDateValue(contentModel);
         validateContentModelProperties(createdXML, id, "/content-model/properties", title, description,
             lastModificationDate);
 
         Iterator<String> it = mdRecordDefinitions.keySet().iterator();
         while (it.hasNext()) {
             String mdRecordName = it.next();
             selectSingleNodeAsserted(contentModel, "/content-model/md-record-definitions/"
                 + "md-record-definition[@name = '" + mdRecordName + "']");
             assertEquals("Metadata definition has not expected Schema Href.", "/cmm/content-model/" + id
                 + "/md-record-definitions/md-record-definition/" + mdRecordName + "/schema/content", selectSingleNode(
                 contentModel,
                 "/content-model/md-record-definitions/" + "md-record-definition[@name = '" + mdRecordName
                     + "']/schema/@href").getNodeValue());
         }
 
         it = resourceDefinitions.iterator();
         while (it.hasNext()) {
             String methodName = it.next();
             selectSingleNodeAsserted(contentModel, "/content-model/resource-definitions/resource-definition[@name = '"
                 + methodName + "']");
             selectSingleNodeAsserted(contentModel, "/content-model/resource-definitions/resource-definition[@name = '"
                 + methodName + "']/xslt[@href = '" + "/cmm/content-model/" + id
                 + "/resource-definitions/resource-definition/" + methodName + "/xslt/content']");
         }
 
         Iterator<List<String>> lit = contentStreamDefinitions.iterator();
         while (lit.hasNext()) {
             List<String> contentStreamDefinition = lit.next();
             String contentStreamName = contentStreamDefinition.get(0);
             selectSingleNodeAsserted(contentModel, "/content-model/content-streams/content-stream[@name = '"
                 + contentStreamName + "']");
             assertEquals("Content Stream definition has not expected href.", "/cmm/content-model/" + id
                 + "/content-streams/content-stream/" + contentStreamName + "/content", selectSingleNode(contentModel,
                 "/content-model/content-streams/content-stream[@name = '" + contentStreamName + "']/@href")
                 .getNodeValue());
             assertEquals("Content Stream definition has not expected mime-type.", contentStreamDefinition.get(1),
                 selectSingleNode(contentModel,
                     "/content-model/content-streams/content-stream[@name = '" + contentStreamName + "']/@mime-type")
                     .getNodeValue());
             assertEquals("Content Stream definition has not expected storage type.", contentStreamDefinition.get(2),
                 selectSingleNode(contentModel,
                     "/content-model/content-streams/content-stream[@name = '" + contentStreamName + "']/@storage")
                     .getNodeValue());
         }
 
         if (checkBehavior) {
 
             // if(contentModel.isItemContentModel)
             {
 
                 // create item with this content model and check for dynamic
                 // behavior
                 String itemXml =
                     EscidocAbstractTest.getTemplateAsString(TEMPLATE_CONTENT_MODEL_PATH + "/rest",
                         "item-minimal-for-content-model.xml");
                 itemXml = itemXml.replace("##CONTENT_MODEL_ID##", id);
                 OmTestBase omBase = new OmTestBase();
                 itemXml = handleXmlResult(omBase.getItemClient().create(itemXml));
                 Document itemResources = null;
                 String itemResourcesXml =
                     handleXmlResult(omBase.getItemClient().retrieveResources(getObjidValue(getDocument(itemXml))));
                 itemResources = getDocument(itemResourcesXml);
                 it = resourceDefinitions.iterator();
                 while (it.hasNext()) {
                     String methodName = it.next();
                     selectSingleNodeAsserted(itemResources, "/resources/" + methodName);
                 }
 
                 // check behavior
                 HttpResponse httpRes =
                    HttpHelper.executeHttpRequest(Constants.HTTP_METHOD_GET, getBaseUrl() + "/ir/item/"
                         + getObjidValue(getDocument(itemXml)) + "/resources/trans", null,
                         MediaType.TEXT_XML.toString(), null);
                 String resultCheckString = EntityUtil.toString(httpRes.getEntity(), HTTP.UTF_8);
 
                 Document resultCheckDoc = getDocument(resultCheckString);
                 selectSingleNodeAsserted(resultCheckDoc, "/result[. = 'check']");
             }
             // else if(contentModel.isContainerContentModel)
             {
 
                 // create container with this content model and check for
                 // dynamic
                 // behavior
                 String containerXml =
                     EscidocAbstractTest.getTemplateAsString(TEMPLATE_CONTENT_MODEL_PATH + "/rest",
                         "container-minimal-for-content-model.xml");
                 containerXml = containerXml.replace("##CONTENT_MODEL_ID##", id);
                 OmTestBase omBase = new OmTestBase();
                 containerXml = handleXmlResult(omBase.getContainerClient().create(containerXml));
                 Document containerResources = null;
                 String containerResourcesXml =
                     handleXmlResult(omBase.getContainerClient().retrieveResources(
                         getObjidValue(getDocument(containerXml))));
                 containerResources = getDocument(containerResourcesXml);
 
                 it = resourceDefinitions.iterator();
                 while (it.hasNext()) {
                     String methodName = it.next();
                     selectSingleNodeAsserted(containerResources, "/resources/" + methodName);
                 }
 
                 // check behavior
                 HttpResponse httpRes =
                    HttpHelper.executeHttpRequest(Constants.HTTP_METHOD_GET, getBaseUrl() + "/ir/container/"
                         + getObjidValue(getDocument(containerXml)) + "/resources/trans", null, MediaType.TEXT_XML
                         .toString(), null);
                 String resultCheckString = EntityUtil.toString(httpRes.getEntity(), HTTP.UTF_8);
                 Document resultCheckDoc = getDocument(resultCheckString);
                 selectSingleNodeAsserted(resultCheckDoc, "/result[. = 'check']");
 
             }
         }
     }
 
     /**
      * Validate Content Model properties XML.
      * 
      * @param createdXML
      *            The XML to be validated.
      * @param id
      *            The objid of the Content Model.
      * @param propertiesXpath
      *            The path to the properties element.
      * @param title
      *            Expected title.
      * @param description
      *            Expected description.
      * @param lastModificationDate
      *            Expected date-time of latest modification.
      * @throws Exception
      *             If anything fails.
      */
     protected void validateContentModelProperties(
         String createdXML, String id, String propertiesXpath, String title, String description,
         String lastModificationDate) throws Exception {
 
         Document contentModel = getDocument(createdXML);
         if (id == null) {
             id = getObjidValue(contentModel);
         }
         boolean isRoot = true;
         if (propertiesXpath.lastIndexOf('/') > 0) {
             isRoot = false;
         }
 
         // TODO improve
 
         assertEquals("Name of content model not as expected.", title, selectSingleNode(contentModel,
             propertiesXpath + "/name/text()").getNodeValue());
         assertEquals("Description of content model not as expected.", description, selectSingleNode(contentModel,
             propertiesXpath + "/description/text()").getNodeValue());
 
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/created-by");
         validateObjidForm(contentModel, propertiesXpath + "/created-by");
 
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/creation-date");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/version");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/version/number");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/version/date");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/version/status");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/version/status[text() = 'pending']");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/version/modified-by");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/version/comment");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/latest-version");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/latest-version/number");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/latest-version/date");
 
         String latestVersionNumber =
             selectSingleNodeAsserted(contentModel, propertiesXpath + "/latest-version/number/text()").getNodeValue();
         String versionNumber =
             selectSingleNodeAsserted(contentModel, propertiesXpath + "/version/number/text()").getNodeValue();
         assertTrue("Version number of latest version must be greater or equal to version number of current version.",
             (Integer.parseInt(latestVersionNumber) >= Integer.parseInt(versionNumber)));
         if (isRoot) {
             selectSingleNodeAsserted(contentModel, propertiesXpath + "/@last-modification-date");
             selectSingleNodeAsserted(contentModel, propertiesXpath + "/@base");
         }
         selectSingleNodeAsserted(contentModel, propertiesXpath + "[@href = '/cmm/content-model/" + id + "/properties']");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/version" + "[@href = '/cmm/content-model/" + id
             + ":" + versionNumber + "']");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/latest-version" + "[@href = '/cmm/content-model/"
             + id + ":" + latestVersionNumber + "']");
 
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/created-by[starts-with(@href, '/aa/user-account/')]");
         selectSingleNodeAsserted(contentModel, propertiesXpath + "/version/modified-by"
             + "[starts-with(@href, '/aa/user-account/')]");
         validateObjidForm(contentModel, propertiesXpath + "/version/modified-by");
 
         if (lastModificationDate != null) {
             // timestamps
             assertEquals(lastModificationDate, selectSingleNodeAsserted(contentModel,
                 propertiesXpath + "/latest-version/date/text()").getNodeValue());
             if (latestVersionNumber.equals(versionNumber)) {
                 assertEquals(lastModificationDate, selectSingleNodeAsserted(contentModel,
                     propertiesXpath + "/version/date/text()").getNodeValue());
             }
             else {
                 assertDateBeforeAfter(selectSingleNodeAsserted(contentModel, propertiesXpath + "/version/date/text()")
                     .getNodeValue(), lastModificationDate);
             }
         }
     }
 
     /**
      * Validate the form of an objid appearing in an element.
      * 
      * @param document
      *            The XML document.
      * @param elementPath
      *            The path to the element.
      * @throws Exception
      *             If anything fails.
      */
     protected void validateObjidForm(Document document, String elementPath) throws Exception {
         String refObjid = null;
         String href = selectSingleNodeAsserted(document, elementPath + "/@href").getNodeValue();
         refObjid = href.substring(href.lastIndexOf('/'));
         assertObjid(refObjid);
 
     }
 }
