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
 package de.escidoc.core.test.om.ingest;
 
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidResourceException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.junit.Assert.fail;
 
 /**
  * Test ingesting resource via ingest interface.<br>
  * By default, the tests are executed using a depositor user.
  * 
  * @author Steffen Wagner, KST
  */
 @RunWith(value = Parameterized.class)
 public class IngestTest extends IngestTestBase {
 
     private static final Pattern OBJECT_PATTERN =
         Pattern.compile("<objid resourceType=\"([^\"][^\"]*)\">(escidoc:\\d+)</objid>", Pattern.MULTILINE);
 
     /**
      * @param transport
      *            The transport identifier.
      */
     public IngestTest(final int transport) {
         super(transport);
     }
 
     /**
      * Test if a valid item gets ingested. The return value must be a xml fragment containing the object id. The return
      * value gets first parsed to check if it is well formed xml. Then the xml gets matched against a pattern which
      * looks for an object id.
      * 
      * @throws Exception
      *             the Exception gets thrown in the following cases: <ul <li>The ingest fails due to internal reasons
      *             (Fedora, eSciDoc) <li>The return value is not well formed <li>The return value does not contain a
      *             vaild object id. </ul>
      */
     @Test
     public void testIngestItemValid() throws Exception {
 
         String toBeCreatedXml =
             EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH, getTransport(false)
                 + "/escidoc_item_198_for_create_2_Component_Md-Records.xml");
 
         String createdXml = ingest(toBeCreatedXml);
 
         // Document is well formed and valid
         assertXmlValidResult(createdXml);
 
         Matcher matcher = OBJECT_PATTERN.matcher(createdXml);
 
         if (matcher.find()) {
             String resourceType = matcher.group(1);
             String objectId = matcher.group(2);
 
             // Have we just ingested an item ?
             assert (resourceType.equals("ITEM"));
 
             // We can't assume anything about the object's id except not being
             // null, can we ?
             assert (objectId != null);
         }
         else {
             fail("no match for item found, return value of ingest could not " + "be matched successfully.");
         }
     }
 
     /**
      * Test if a valid Item in public-status 'released' gets ingested. The return value must be a xml fragment
      * containing the object id. The return value gets first parsed to check if it is well formed xml. Then the xml gets
      * matched against a pattern which looks for an object id.
      * 
      * @throws Exception
      *             the Exception gets thrown in the following cases:
      *             <ul>
      *             <li>The ingest fails due to internal reasons (Fedora, eSciDoc)</li>
      *             <li>The return value is not well formed</li>
      *             <li>The return value does not contain a vaild object id.</li>
      *             <li>No exception is thrown because object PID is missing</li>
      *             </ul>
      */
     @Test
     public void testIngestReleasedItem01() throws Exception {
 
         Document toBeCreatedDocument =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH, getTransport(false)
                 + "/item_without_component.xml");
 
         Element publicStatus =
             createElementNode(toBeCreatedDocument, "http://escidoc.de/core/01/properties/", "prop", "public-status",
                 "released");
         Node parent = selectSingleNode(toBeCreatedDocument, "/item/properties");
         Node refNode = selectSingleNode(toBeCreatedDocument, "/item/properties/context");
         parent.insertBefore(publicStatus, refNode);
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             ingest(toBeCreatedXml);
             fail("Exception for missing object PID wasn't thrown.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocRestSoapTestBase.assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test if a valid Item in public-status 'released' gets ingested. The return value must be a xml fragment
      * containing the object id. The return value gets first parsed to check if it is well formed xml. Then the xml gets
      * matched against a pattern which looks for an object id.
      * 
      * @throws Exception
      *             the Exception gets thrown in the following cases:
      *             <ul>
      *             <li>The ingest fails due to internal reasons (Fedora, eSciDoc)</li>
      *             <li>The return value is not well formed</li>
      *             <li>The return value does not contain a vaild object id.</li>
      *             <li>The public-status 'released' is not copied</li>
      *             <li>The object pid is not copied.</li>
      *             </ul>
      */
     @Test
     public void testIngestReleasedItem02() throws Exception {
 
         Document toBeCreatedDocument =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH, getTransport(false)
                 + "/item_without_component.xml");
 
         Element publicStatus =
             createElementNode(toBeCreatedDocument, "http://escidoc.de/core/01/properties/", "prop", "public-status",
                 "released");
         Node parent = selectSingleNode(toBeCreatedDocument, "/item/properties");
         Node refNode = selectSingleNode(toBeCreatedDocument, "/item/properties/context");
         parent.insertBefore(publicStatus, refNode);
 
         // if pid is required for release than add pid
         if (!getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "false")) {
 
             Element objectPid =
                 createElementNode(toBeCreatedDocument, "http://escidoc.de/core/01/properties/", "prop", "pid",
                     "hdl:escidoc-dummy-pid");
             // this reference based on the template
             Node refNodePid = selectSingleNode(toBeCreatedDocument, "/item/properties/content-model-specific");
             parent.insertBefore(objectPid, refNodePid);
 
         }
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
         String createdXml = ingest(toBeCreatedXml);
 
         // Document is well formed and valid
         assertXmlValidResult(createdXml);
 
         Matcher matcher = OBJECT_PATTERN.matcher(createdXml);
         String objectId = null;
 
         if (matcher.find()) {
             String resourceType = matcher.group(1);
             objectId = matcher.group(2);
 
             // Have we just ingested an item ?
             assert (resourceType.equals("ITEM"));
 
             // We can't assume anything about the object's id except not being
             // null, can we ?
             assert (objectId != null);
         }
         else {
             fail("no match for item found, return value of ingest " + "could not be matched successfully.");
         }
 
         // assert at least public-status and objectPID
         String createdItemXml = handleXmlResult(getItemClient().retrieve(objectId));
 
         assertXmlExists("Wrong public-status", createdItemXml, "/item/properties/public-status[text() = 'released']");
         assertXmlExists("Wrong version status", createdItemXml, "/item/properties/version/status[text() = 'released']");
         assertXmlNotNull("pid", getDocument(createdItemXml), "/item/properties/pid");
     }
 
     /**
      * Test if a valid Item in public-status 'released' gets ingested. The return value must be a xml fragment
      * containing the object id. The return value gets first parsed to check if it is well formed xml. Then the xml gets
      * matched against a pattern which looks for an object id.
      * 
      * @throws Exception
      *             the Exception gets thrown in the following cases:
      *             <ul>
      *             <li>The ingest fails due to internal reasons (Fedora, eSciDoc)</li>
      *             <li>The return value is not well formed</li>
      *             <li>The return value does not contain a vaild object id.</li>
      *             <li>No exception is thrown because object PID is missing</li>
      *             </ul>
      */
     @Test(expected = ContextNotFoundException.class)
     public void ingestItemWithWrongContextReference() throws Exception {
 
         Document toBeCreatedDocument =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_ITEM_PATH, getTransport(false)
                 + "/item_without_component.xml");
 
         if (getTransport() == Constants.TRANSPORT_REST) {
 
            substitute(toBeCreatedDocument, "/item/properties/context/@href", "/ir/context/" + UNKNOWN_ID);
         }
         else {
            substitute(toBeCreatedDocument, "/item/properties/context/@id", UNKNOWN_ID);
         }
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         ingest(toBeCreatedXml);
     }
 
     /**
      * Test what happens if an invalid but well formed XML fragment gets ingested. An InvalidResourceException has to be
      * thrown as a result.
      * 
      * @throws Exception
      *             the Exception, in this case InvalidResourceException
      */
     @Test(expected = InvalidResourceException.class)
     public void testIngestXmlNotValid() throws Exception {
 
         String toBeCreatedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><a/></root>";
         ingest(toBeCreatedXml);
     }
 
     /**
      * Tests what happens if a not well formed xml fragment gets ingested. First the exception type gets checked, then
      * the content of the exception message gets checked. If either fail the test fails.
      */
     @Test(expected = InvalidResourceException.class)
     public void testIngestXmlNotWellFormed() throws Exception {
 
         String toBeCreatedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><roo><a/></root>";
 
         ingest(toBeCreatedXml);
     }
 
     /**
      * Test if a valid context gets ingested. The return value must be a xml fragment containing the object id and
      * conforming the the result.xsd schema.
      */
     @Test
     public void testIngestContextValid() throws Exception {
         Document toBeCreatedDocument =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_CONTEXT_PATH, getTransport(false)
                 + "/context_create.xml");
         substitute(toBeCreatedDocument, XPATH_CONTEXT_PROPERTIES_NAME, getUniqueName("Unique Name "));
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         String createdXml = ingest(toBeCreatedXml);
 
         // Document is well formed and valid
         assertXmlValidResult(createdXml);
 
         Matcher matcher = OBJECT_PATTERN.matcher(createdXml);
 
         if (matcher.find()) {
             String resourceType = matcher.group(1);
             String objectId = matcher.group(2);
             // immediately delete to avoid naming conflicts in later tests...
             deleteContext(objectId);
 
             // Have we just ingested an item ?
             assert (resourceType.equals("CONTEXT"));
 
             // We can't assume anything about the object's id except not being
             // null, can we ?
             assert (objectId != null);
         }
         else {
             fail("no match for context found, return value of ingest " + "could not be matched successfully.");
         }
 
     }
 
     /**
      * Test if a valid container gets ingested. The return value must be a xml fragment containing the object id and
      * conforming the the result.xsd schema.
      */
     @Test
     public void testIngestContainerValid() throws Exception {
         Document toBeCreatedDocument =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_CONTAINER_PATH, getTransport(false)
                 + "/create_container.xml");
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
         String createdXml = ingest(toBeCreatedXml);
 
         // Document is well formed and valid
         assertXmlValidResult(createdXml);
 
         Matcher matcher = OBJECT_PATTERN.matcher(createdXml);
 
         if (matcher.find()) {
             String resourceType = matcher.group(1);
             String objectId = matcher.group(2);
 
             // Have we just ingested an item ?
             assert (resourceType.equals("CONTAINER"));
 
             // We can't assume anything about the object's id except not being
             // null, can we ?
             assert (objectId != null);
         }
         else {
             fail("no match for container found, return value of ingest " + "could not be matched successfully.");
         }
     }
 
     /**
      * Test if a valid Container in public-status 'released' gets ingested. The return value must be a xml fragment
      * containing the object id. The return value gets first parsed to check if it is well formed xml. Then the xml gets
      * matched against a pattern which looks for an object id.
      * 
      * @throws Exception
      *             the Exception gets thrown in the following cases:
      *             <ul>
      *             <li>The ingest fails due to internal reasons (Fedora, eSciDoc)</li>
      *             <li>The return value is not well formed</li>
      *             <li>The return value does not contain a vaild object id.</li>
      *             <li>No exception is thrown because object PID is missing</li>
      *             </ul>
      */
     @Test
     public void testIngestReleasedContainer01() throws Exception {
 
         Document toBeCreatedDocument =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_CONTAINER_PATH, getTransport(false)
                 + "/create_container.xml");
 
         Element publicStatus =
             createElementNode(toBeCreatedDocument, "http://escidoc.de/core/01/properties/", "prop", "public-status",
                 "released");
         Node parent = selectSingleNode(toBeCreatedDocument, "/container/properties");
         Node refNode = selectSingleNode(toBeCreatedDocument, "/container/properties/name");
         parent.insertBefore(publicStatus, refNode);
 
         // delete object pid node
         parent.removeChild(selectSingleNode(toBeCreatedDocument, "/container/properties/pid"));
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
 
         try {
             ingest(toBeCreatedXml);
             fail("Exception for missing object PID wasn't thrown.");
         }
         catch (final Exception e) {
             Class<?> ec = InvalidStatusException.class;
             EscidocRestSoapTestBase.assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test if a valid Container in public-status 'released' gets ingested. The return value must be a xml fragment
      * containing the object id. The return value gets first parsed to check if it is well formed xml. Then the xml gets
      * matched against a pattern which looks for an object id.
      * 
      * @throws Exception
      *             the Exception gets thrown in the following cases:
      *             <ul>
      *             <li>The ingest fails due to internal reasons (Fedora, eSciDoc)</li>
      *             <li>The return value is not well formed</li>
      *             <li>The return value does not contain a valid object id.</li>
      *             <li>The public-status 'released' is not copied</li>
      *             <li>The object PID is not copied.</li>
      *             </ul>
      */
     @Test
     public void testIngestReleasedContainer02() throws Exception {
 
         Document toBeCreatedDocument =
             EscidocRestSoapTestBase.getTemplateAsDocument(TEMPLATE_CONTAINER_PATH, getTransport(false)
                 + "/create_container.xml");
 
         Element publicStatus =
             createElementNode(toBeCreatedDocument, "http://escidoc.de/core/01/properties/", "prop", "public-status",
                 "released");
         Node parent = selectSingleNode(toBeCreatedDocument, "/container/properties");
         Node refNode = selectSingleNode(toBeCreatedDocument, "/container/properties/name");
         parent.insertBefore(publicStatus, refNode);
 
         // if pid is required for release than add pid
         if (!getItemClient().getPidConfig("cmm.Item.objectPid.releaseWithoutPid", "false")) {
 
             // add object PID if it not exists
             if (selectSingleNode(toBeCreatedDocument, "/container/properties/pid") == null) {
 
                 Element objectPid =
                     createElementNode(toBeCreatedDocument, "http://escidoc.de/core/01/properties/", "prop", "pid",
                         "hdl:escidoc-dummy-pid");
                 // this reference based on the template
                 Node refNodePid = selectSingleNode(toBeCreatedDocument, "/container/properties/content-model-specific");
                 parent.insertBefore(objectPid, refNodePid);
             }
         }
 
         String toBeCreatedXml = toString(toBeCreatedDocument, false);
         String createdXml = ingest(toBeCreatedXml);
 
         // Document is well formed and valid
         assertXmlValidResult(createdXml);
 
         Matcher matcher = OBJECT_PATTERN.matcher(createdXml);
         String objectId = null;
 
         if (matcher.find()) {
             String resourceType = matcher.group(1);
             objectId = matcher.group(2);
 
             // Have we just ingested a container?
             assert resourceType.equals("CONTAINER") : "wrong resource type: " + resourceType;
 
             // We can't assume anything about the object's id except not being
             // null, can we ?
             assert (objectId != null);
         }
         else {
             fail("no match for item found, return value of ingest could " + "not be matched successfully.");
         }
 
         // assert at least public-status and objectPID
         String createdContainerXml = handleXmlResult(getContainerClient().retrieve(objectId));
 
         assertXmlExists("Wrong public-status", createdContainerXml,
             "/container/properties/public-status[text() = 'released']");
         assertXmlExists("Wrong version status", createdContainerXml,
             "/container/properties/version/status[text() = 'released']");
         assertXmlNotNull("pid", getDocument(createdContainerXml), "/container/properties/pid");
     }
 
     /**
      * Test if a valid ou gets ingested. The return value must be a xml fragment containing the object id and conforming
      * the the result.xsd schema.
      * 
      * @throws Exception
      *             Throws Exception if test failes.
      */
     @Test
     public void testIngestOuValid() throws Exception {
         Document ou = getTemplateAsDocument(TEMPLATE_ORGANIZATIONAL_UNIT_PATH, "escidoc_ou_create.xml");
 
         setUniqueValue(ou, XPATH_ORGANIZATIONAL_UNIT_TITLE);
         String toBeCreatedXml = toString(ou, false);
         assertXmlValidOrganizationalUnit(toBeCreatedXml);
 
         String createdXml = ingest(toBeCreatedXml);
         // Document is well formed and valid
         assertXmlValidResult(createdXml);
 
         Matcher matcher = OBJECT_PATTERN.matcher(createdXml);
 
         if (matcher.find()) {
             String resourceType = matcher.group(1);
             String objectId = matcher.group(2);
 
             // Have we just ingested an item ?
             assert (resourceType.equals("OU"));
 
             // We can't assume anything about the object's id except not being
             // null, can we ?
             assert (objectId != null);
         }
         else {
             fail("no match for organizational unit found, return value "
                 + "of ingest could not be matched successfully.");
         }
     }
 
     /**
      * Test if a valid Content Model gets ingested. The return value must be a XML fragment containing the object id and
      * conforming the the result.xsd schema.
      * 
      * @throws Exception
      *             Throws Exception if test fail.
      */
     @Test
     public void ingestContentModel() throws Exception {
         String cmmTempl =
             getTemplateAsString(TEMPLATE_CONTENT_MODEL_PATH, getTransport(false)
                 + "/content-model-minimal-for-create.xml");
 
         String createdXml = ingest(cmmTempl);
 
         // assert document is well formed and valid
         assertXmlValidResult(createdXml);
 
         Matcher matcher = OBJECT_PATTERN.matcher(createdXml);
 
         if (matcher.find()) {
             String resourceType = matcher.group(1);
             String objectId = matcher.group(2);
 
             // Have we just ingested a content model ?
             assert resourceType.equals("CONTENT_MODEL") : "expected resource type \"CONTENT_MODEL\" but got \""
                 + resourceType + "\"";
 
             // We can't assume anything about the object's id except not being
             // null, can we ?
             assert (objectId != null);
         }
         else {
             fail("no match for content model found, return value " + "of ingest could not be matched successfully.");
         }
     }
 
     /**
      * Test unexpected parser exception instead of XmlCorruptedException during create (see issue INFR-911).
      * 
      * @throws Exception
      *             Thrown if behavior is not as expected.
      */
     // should be activated when SOAP is dropped
     @Ignore("Mapping in SOAP seem on this way impossible")
     @Test(expected = XmlCorruptedException.class)
     public void testInvalidXml() throws Exception {
 
         ingest("laber-rababer");
     }
 
 }
