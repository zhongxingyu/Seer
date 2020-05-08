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
 package de.escidoc.core.test.om;
 
 import static org.junit.Assert.assertEquals;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.common.AssignParam;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.om.ContainerClient;
 import de.escidoc.core.test.common.client.servlet.om.ContentRelationClient;
 import de.escidoc.core.test.common.client.servlet.om.ContextClient;
 import de.escidoc.core.test.common.client.servlet.om.DeviationClient;
 import de.escidoc.core.test.common.client.servlet.om.IngestClient;
 import de.escidoc.core.test.common.client.servlet.om.ItemClient;
 import de.escidoc.core.test.sb.SearchTestBase;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import org.apache.xpath.XPathAPI;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import javax.xml.transform.TransformerException;
 
 /**
  * Base class for tests of the mock implementation of the OM resources.
  * 
  * @author Michael Schneider
  */
 public class OmTestBase extends EscidocAbstractTest {
 
     public static final String NAME_CONTENT_MODEL = "content-model";
 
     public static final String XPATH_ITEM = "/" + NAME_ITEM;
 
     public static final String XPATH_ITEM_COMPONENTS = XPATH_ITEM + "/" + NAME_COMPONENTS;
 
     public static final String XPATH_ITEM_COMPONENT =
         XPATH_ITEM_COMPONENTS + "/" + NAME_COMPONENT + "[@objid=\"${COMPONENT_ID}\"]";
 
     public static final String XPATH_ITEM_MD_RECORDS = XPATH_ITEM + "/" + "md-records";
 
     public static final String XPATH_ITEM_MD_RECORD = XPATH_ITEM + "/" + "md-records/md-record[1]";
 
     public static final String XPATH_ITEM_PROPERTIES = XPATH_ITEM + "/" + NAME_PROPERTIES;
 
     public static final String XPATH_ITEM_CONTEXT = XPATH_ITEM_PROPERTIES + "/" + NAME_CONTEXT;
 
     public static final String XPATH_ITEM_CONTENT_MODEL = XPATH_ITEM_PROPERTIES + "/" + NAME_CONTENT_MODEL;
 
     public static final String XPATH_CONTAINER_PROPERTIES = "/container/properties";
 
     public static final String XPATH_CONTENT_RELATION_PROPERTIES = "/content-relation/properties";
 
     public static final String XPATH_CONTENT_RELATION_STATUS =
         XPATH_CONTENT_RELATION_PROPERTIES + "/" + NAME_PUBLIC_STATUS;
 
     public static final String XPATH_CONTENT_RELATION_STATUS_COMMENT =
         XPATH_CONTENT_RELATION_PROPERTIES + "/" + NAME_PUBLIC_STATUS_COMMENT;
 
     public static final String XPATH_ITEM_RESOURCES = XPATH_ITEM + "/" + NAME_RESOURCES;
 
     public static final String XPATH_ITEM_STATUS = XPATH_ITEM_PROPERTIES + "/" + NAME_PUBLIC_STATUS;
 
     public static final String XPATH_ITEM_CURRENT_VERSION = XPATH_ITEM_PROPERTIES + "/" + NAME_VERSION;
 
     public static final String XPATH_ITEM_LATEST_RELEASE = XPATH_ITEM_PROPERTIES + "/" + NAME_LATEST_RELEASE;
 
     public static final String XPATH_ITEM_LATEST_RELEASE_PID =
         XPATH_ITEM_PROPERTIES + "/" + NAME_LATEST_RELEASE + "/pid";
 
     public static final String XPATH_ITEM_LATEST_VERSION = XPATH_ITEM_PROPERTIES + "/" + NAME_LATEST_VERSION;
 
     public static final String XPATH_ITEM_CURRENT_VERSION_STATUS =
         XPATH_ITEM_CURRENT_VERSION + "/" + NAME_VERSION_STATUS;
 
     public static final String XPATH_CONTAINER_CONTENT_MODEL = XPATH_CONTAINER_PROPERTIES + "/" + NAME_CONTENT_MODEL;
 
     public static final String XPATH_CONTAINER = "/" + NAME_CONTAINER;
 
     public static final String XPATH_ITEM_VERSION = XPATH_ITEM_PROPERTIES + "/" + NAME_VERSION;
 
     public static final String XPATH_ITEM_VERSION_NUMBER = XPATH_ITEM_PROPERTIES + "/" + NAME_VERSION + "/number";
 
     public static final String XPATH_ITEM_VERSION_PID = XPATH_ITEM_VERSION + "/" + NAME_PID;
 
     public static final String XPATH_ITEM_OBJECT_PID = XPATH_ITEM_PROPERTIES + "/" + NAME_PID;
 
     public static final String XPATH_CONTENT_PID = "/component/properties/pid";
 
     public static final String XPATH_CONTAINER_OBJECT_PID = XPATH_CONTAINER_PROPERTIES + "/" + NAME_PID;
 
     public static final String XPATH_CONTAINER_VERSION = XPATH_CONTAINER_PROPERTIES + "/" + NAME_VERSION;
 
     public static final String XPATH_CONTAINER_VERSION_PID = XPATH_CONTAINER_VERSION + "/" + NAME_PID;
 
     public static final String XPATH_PARAM_PID = XPATH_PARAM + "/" + NAME_PID;
 
     public static final String XPATH_RESULT_PID = XPATH_RESULT + "/" + NAME_PID;
 
     private SearchTestBase searchTestBase = null;
 
     /**
      * @return Returns the searchTestBase.
      */
     public SearchTestBase getSearchTestBase() {
         if (this.searchTestBase == null) {
             this.searchTestBase = new SearchTestBase();
         }
         return searchTestBase;
     }
 
     /**
      * Get the Id of the Context from the object.
      * 
      * @param doc
      *            The Document of the resource.
      * @return Id of Context
      * @throws TransformerException
      *             Thrown in case of XML Parser failure.
      */
     public String getContextId(final Document doc) throws TransformerException {
 
         String containerContextId = null;
         Node contextNode = null;
         contextNode = XPathAPI.selectSingleNode(doc, "//properties/context/@href");
         containerContextId = getObjidFromHref(contextNode.getNodeValue());
         return containerContextId;
     }
 
     /**
      * Set the Id of the Context for a resource.
      * 
      * @param resDoc
      *            The resource Document.
      * @param contextId
      *            The new id of the Context.
      * @return The Document with the new Id.
      * @throws Exception
      *             Thrown in case of substitution failure.
      */
     public Document setContextId(final Document resDoc, final String contextId) throws Exception {
         return (Document) substitute(resDoc, "//properties/context/@href", "/ir/context/" + contextId);
     }
 
     /**
      * Get the status of retrieved version of object.
      * 
      * @param doc
      *            The Document of the resource.
      * @return version status
      * @throws TransformerException
      *             Thrown in case of XML Parser failure.
      */
     public String getVersionStatus(final Document doc) throws TransformerException {
 
         Node statusNode = null;
 
         statusNode = XPathAPI.selectSingleNode(doc, "//properties/version/status");
 
         return statusNode.getTextContent();
     }
 
     /**
      * Get the public-status of object.
      * 
      * @param doc
      *            The Document of the resource.
      * @return public-status
      * @throws TransformerException
      *             Thrown in case of XML Parser failure.
      */
     public String getPublicStatus(final Document doc) throws TransformerException {
 
         Node statusNode = null;
 
         statusNode = XPathAPI.selectSingleNode(doc, "//properties/public-status");
 
         return statusNode.getTextContent();
     }
 
     /**
      * Get the Id of the latest version of object.
      * 
      * @param doc
      *            The Document of the resource.
      * @return Id of the latest version
      * @throws TransformerException
      *             Thrown in case of XML Parser failure.
      */
     public String getLatestVersionId(final Document doc) throws TransformerException {
         String latestVersion = null;
         Node latestVersionNode = null;
         latestVersionNode = XPathAPI.selectSingleNode(doc, "//properties/latest-version/@href");
         latestVersion = getObjidFromHref(latestVersionNode.getNodeValue());
         return latestVersion;
     }
 
     /**
      * Get the number of the latest version of object.
      * 
      * @param doc
      *            The Document of the resource.
      * @return Number of the latest version
      * @throws TransformerException
      *             Thrown in case of XML Parser failure.
      */
     public int getLatestVersionNumber(final Document doc) throws TransformerException {
 
         Node latestVersionNode = null;
 
         latestVersionNode = XPathAPI.selectSingleNode(doc, "//properties/latest-version/number");
 
         return Integer.valueOf(latestVersionNode.getTextContent());
     }
 
     /**
      * Assert that the created MdRecord has all required elements.
      * 
      * @param name
      *            The name of the md-record.
      * @param resourceId
      *            The id of the resource.
      * @param resourceType
      *            /ir/&lt;type of resource&gt;/..
      * @param xmlCreatedMdRecord
      *            The created md-record.
      * @param xmlTemplateResource
      *            The template resource used to create the context.
      * @param timestampBeforeCreation
      *            A timestamp before the creation of the context.
      * @throws Exception
      *             If anything fails.
      */
     public void assertCreatedMdRecord(
         final String name, final String resourceId, final String resourceType, final String xmlCreatedMdRecord,
         final String xmlTemplateResource, final String timestampBeforeCreation) throws Exception {
 
         final String msg = "Asserting retrieved md-record failed. ";
 
         Document toBeAssertedDocument = EscidocAbstractTest.getDocument(xmlCreatedMdRecord);
         Document template = EscidocAbstractTest.getDocument(xmlTemplateResource);
 
         // assert root element
         // String[] values =
         assertXlinkElement(msg + " xlink not as expected.", toBeAssertedDocument, "/md-record", "/ir/" + resourceType
             + "/" + resourceId + "/" + Constants.SUB_MD_RECORD + "/" + name);
         assertLastModificationDate(msg + " last-modification-date not as expected.", toBeAssertedDocument,
             "/md-record", timestampBeforeCreation);
         // final String id = values[0];
 
         // assert md-record content
         Node toBeAssertedMdRecordContent = selectSingleNode(toBeAssertedDocument, "/md-record/*[1]");
         Node mdRecordContentTemplate =
             selectSingleNode(template, "/" + resourceType + "/md-records/md-record[@name = '" + name + "']/*[1]");
 
         assertXmlEquals(msg + "Content not equal.", toBeAssertedMdRecordContent, mdRecordContentTemplate);
     }
 
     /**
      * Assert that the objectId is indexed in given index.
      * 
      * @param indexName
      *            The name of the index.
      * @param resourceId
      *            The id of the resource.
      * @throws Exception
      *             If anything fails.
      */
     public void assertIndexed(final String indexName, final String resourceId) throws Exception {
 
         HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put(FILTER_PARAMETER_QUERY, "PID=" + resourceId + " or distinction.rootPid=" + resourceId);
         String response = getSearchTestBase().search(parameters, indexName);
         assertXmlValidSearchResult(response);
         assertEquals("1", getSearchTestBase().getNumberOfHits(response));
     }
 
     /**
      * @param lastModDate
      *            The last modification date of the source.
      * @param targets
      *            List of target ids. As much relations are added as there are tagets.
      * @param predicate
      *            The predicate of the relation.
      * @return The task parameter according to the given values.
      */
     public String getRelationTaskParameter(
         final String lastModDate, final Vector<String> targets, final String predicate) {
         String taskParam = null;
         if ((targets != null) && (targets.size() > 0)) {
             taskParam =
                 de.escidoc.core.test.Constants.XML_HEADER
                     + "<param xmlns=\"http://www.escidoc.org/schemas/relation-task-param/0.1\" "
                     + "last-modification-date=\"" + lastModDate + "\">\n";
             Iterator<String> it = targets.iterator();
             while (it.hasNext()) {
                 taskParam += "<relation><targetId>" + it.next() + "</targetId><predicate>";
                 if (predicate != null) {
                     taskParam += predicate;
                 }
                 else {
                     taskParam += "http://www.escidoc.de/ontologies/mpdl-ontologies/content-relations#isPartOf";
 
                 }
                 taskParam += "</predicate></relation>\n";
             }
             taskParam += "</param>";
         }
         return taskParam;
     }
 
     /**
      * Prepare the PIDs of Container. Depending on configuration must have a Container an object and a version Pid
      * before you can release it.
      *
      * @param containerId The id of the Container.
      * @param lmd         The last modification date of the Container.
      * @return The new last modification date of the Container. The return last modification date equals the param last
      *         modification date if the Container resource was not altered.
      * @throws Exception Thrown if pid assignment failed.
      */
     public String prepareContainerPid(final String containerId, final String lmd) throws Exception {
 
         String newLmd = lmd;
         String objectPidXml = null;
         String versionPidXml = null;
        String containerXml = retrieve(containerId);
 
         String pidParam;
         AssignParam assignPidParam = new AssignParam();
 
         // assign pid to member (Container)
         if (getContainerClient().getPidConfig("cmm.Container.objectPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.objectPid.releaseWithoutPid", "false")) {
 
            // check if container has already object pid
            if (selectSingleNode(getDocument(containerXml), "/container/properties/pid") == null) {
 
                assignPidParam.setUrl(new URL("http://somewhere/" + containerId));
                pidParam = getAssignPidTaskParam(new DateTime(newLmd, DateTimeZone.UTC), assignPidParam);

                objectPidXml = handleXmlResult(getContainerClient().assignObjectPid(containerId, pidParam));
                assertXmlValidResult(objectPidXml);
                newLmd = getLastModificationDateValue(getDocument(objectPidXml));
            }
         }
         if (getContainerClient().getPidConfig("cmm.Container.versionPid.setPidBeforeRelease", "true")
             && !getContainerClient().getPidConfig("cmm.Container.versionPid.releaseWithoutPid", "false")) {
 
            // check if this version of container has already version pid
            if (selectSingleNode(getDocument(containerXml), "/container/properties/version/pid") == null) {
 
                assignPidParam.setUrl(new URL("http://somewhere/" + containerId));
                pidParam = getAssignPidTaskParam(new DateTime(newLmd, DateTimeZone.UTC), assignPidParam);

                versionPidXml = handleXmlResult(getContainerClient().assignVersionPid(containerId, pidParam));
                assertXmlValidResult(versionPidXml);
                newLmd = getLastModificationDateValue(getDocument(versionPidXml));
            }
         }
 
         return newLmd;
     }
 
     /**
      * Prepare the release of Item. Depending on configuration must have a Item an object and a version Pid before you
      * can release it.
      *
      * @param itemId The id of the Item.
      * @param lmd    The lastmodification date of the Item.
      * @return The new last modification date of the Item. The return last modification date equals the param last
      *         modification date if the Item resource was not altered.
      * @throws Exception Thrown if pid assignment failed.
      */
     public String prepareItemPid(final String itemId, final String lmd) throws Exception {
 
         String newLmd = lmd;
         String objectPidXml = null;
         String versionPidXml = null;
 
         String pidParam;
         AssignParam assignPidParam = new AssignParam();
 
         // assign pid to member (item)
         if (getItemClient().getPidConfig("cmm.Item.objectPid.setPidBeforeRelease", "true")
             && !getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "false")) {
 
             assignPidParam.setUrl(new URL("http://somewhere/" + itemId));
             pidParam = getAssignPidTaskParam(new DateTime(newLmd, DateTimeZone.UTC), assignPidParam);
 
             objectPidXml = handleXmlResult(getItemClient().assignObjectPid(itemId, pidParam));
             assertXmlValidResult(objectPidXml);
             newLmd = getLastModificationDateValue(getDocument(objectPidXml));
         }
         if (getItemClient().getPidConfig("cmm.Item.versionPid.setPidBeforeRelease", "true")
             && !getItemClient().getPidConfig("cmm.Item.versionPid.releaseWithoutPid", "false")) {
 
             assignPidParam.setUrl(new URL("http://somewhere/" + itemId));
             pidParam = getAssignPidTaskParam(new DateTime(newLmd, DateTimeZone.UTC), assignPidParam);
 
             versionPidXml = handleXmlResult(getItemClient().assignVersionPid(itemId, pidParam));
             assertXmlValidResult(versionPidXml);
             newLmd = getLastModificationDateValue(getDocument(versionPidXml));
         }
 
         return newLmd;
     }
 
 }
