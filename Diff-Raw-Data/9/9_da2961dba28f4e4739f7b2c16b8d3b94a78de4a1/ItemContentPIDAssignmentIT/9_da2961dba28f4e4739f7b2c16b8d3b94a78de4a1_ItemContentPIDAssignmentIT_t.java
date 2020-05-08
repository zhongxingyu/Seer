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
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.om.item;
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.remote.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.remote.application.violated.OptimisticLockingException;
 import de.escidoc.core.test.Constants;
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.TaskParamFactory;
 import de.escidoc.core.test.common.AssignParam;
 import de.escidoc.core.test.security.client.PWCallback;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.Test;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import java.net.URL;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 /**
  * Test the Persistent Identifier implementation of the item content.
  *
  * @author André Schenk
  */
 public class ItemContentPIDAssignmentIT extends ItemTestBase {
 
     private final String itemUrl;
 
     public ItemContentPIDAssignmentIT() {
         itemUrl = getBaseUrl() + "/ir/item/";
     }
 
     /**
      * Test assignment of PID to a component.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testAssignContentPid() throws Exception {
         final int componentNo = 2;
         Document itemDoc = EscidocAbstractTest.getDocument(createItem());
         final String componentId = getComponentObjidValue(itemDoc, componentNo);
         final String itemId = getObjidValue(itemDoc);
 
         assertNull(itemDoc.getElementById(NAME_PID));
         assignAndCheckContentPid(itemId, componentId);
 
         Class<?> ec = InvalidStatusException.class;
 
         try {
             AssignParam assignPidParam = new AssignParam();
             assignPidParam.setUrl(new URL(this.itemUrl + itemId));
             String pidParam =
                 TaskParamFactory.getAssignPidTaskParam(assignPidParam,
                     getLastModificationDateValue2(getDocument(retrieve(itemId))));
 
             assignContentPid(itemId, componentId, pidParam);
             fail("Missing InvalidStatusException");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Check if the last-modification-date of the PID result is equal to the last-modification-date of the retrieved
      * Item.
      *
      * @throws Exception Thrown in case of failure.
      */
     @Test
     public void testCompareLastModDateContentPid() throws Exception {
         Document itemDoc = EscidocAbstractTest.getDocument(createItem());
         String lmdCreate = getLastModificationDateValue(itemDoc);
         String itemId = getObjidValue(itemDoc);
         String componentId = getComponentObjidValue(itemDoc, 1);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam, new DateTime(lmdCreate, DateTimeZone.UTC));
 
         String pidXML = assignContentPid(itemId, componentId, pidParam);
         Document pidDoc = EscidocAbstractTest.getDocument(pidXML);
         String lmdPid = getLastModificationDateValue(pidDoc);
 
         assertTimestampIsEqualOrAfter("Last modification timestamp was not updated.", lmdPid, lmdCreate);
 
         Document itemDocRetrieve = EscidocAbstractTest.getDocument(retrieve(itemId));
 
         assertEquals("", lmdPid, getLastModificationDateValue(itemDocRetrieve));
     }
 
     /**
      * Check PID assignment with lower user permissions.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testContentPidAssignmentPermission1() throws Exception {
         try {
             PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
             testAssignContentPid();
         }
         finally {
             PWCallback.resetHandle();
         }
     }
 
     /**
      * https://www.escidoc.org/jira/browse/INFR-1023
      * <p/>
      * Assign a content PID when the item already has an object PID assigned.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testIssue1023() throws Exception {
         final int componentNo = 3;
 
         // create item
         Document itemDoc = EscidocAbstractTest.getDocument(createItem());
         final String itemId = getObjidValue(itemDoc);
 
         // release item
         submit(itemId, TaskParamFactory.getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(itemId))), null));
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam,
                 getLastModificationDateValue2(getDocument(retrieve(itemId))));
 
         assignObjectPid(itemId, pidParam);
         pidParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam,
                 getLastModificationDateValue2(getDocument(retrieve(itemId))));
 
         assignVersionPid(itemId, pidParam);
         release(itemId, TaskParamFactory.getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(itemId))), null));
 
         // create component
         itemDoc = EscidocAbstractTest.getDocument(update(itemId, addComponent(retrieve(itemId))));
 
         final String componentId = getComponentObjidValue(itemDoc, componentNo);
 
         // assign content PID
         pidParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam,
                 getLastModificationDateValue2(getDocument(retrieve(itemId))));
 
         String pidXML = assignContentPid(itemId, componentId, pidParam);
 
         // check if returned content PID equals RELS-EXT entry
         String itemXml = retrieveComponent(itemId, componentId);
 
         assertXmlValidItem(itemXml);
 
         Node contentPid = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_CONTENT_PID);
 
         assertNotNull(contentPid);
 
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pidXML), XPATH_RESULT_PID);
 
         assertEquals(returnedPid.getTextContent(), contentPid.getTextContent());
     }
 
     /**
      * https://www.escidoc.org/jira/browse/INFR-1024
      * <p/>
      * Deliver the content PID even if the item goes into a new version.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testIssue1024() throws Exception {
         final int componentNo = 2;
 
         // create item
         Document itemDoc = EscidocAbstractTest.getDocument(createItem());
         final String componentId = getComponentObjidValue(itemDoc, componentNo);
         final String itemId = getObjidValue(itemDoc);
 
         // assign PIDs, release item
         submit(itemId, TaskParamFactory.getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(itemId))), null));
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam,
                 getLastModificationDateValue2(getDocument(retrieve(itemId))));
 
         assignObjectPid(itemId, pidParam);
 
         pidParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam,
                 getLastModificationDateValue2(getDocument(retrieve(itemId))));
 
         assignVersionPid(itemId, pidParam);
 
         pidParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam,
                 getLastModificationDateValue2(getDocument(retrieve(itemId))));
 
         assignContentPid(itemId, componentId, pidParam);
 
         String contentPid1 =
             selectSingleNode(EscidocAbstractTest.getDocument(retrieveComponent(itemId, componentId)), XPATH_CONTENT_PID)
                 .getTextContent();
 
         assertNotNull(contentPid1);
         release(itemId, TaskParamFactory.getStatusTaskParam(
             getLastModificationDateValue2(getDocument(retrieve(itemId))), null));
 
         // check if returned content PID equals RELS-EXT entry
         String contentPid2 =
             selectSingleNode(EscidocAbstractTest.getDocument(retrieveComponent(itemId, componentId)), XPATH_CONTENT_PID)
                 .getTextContent();
 
         assertEquals(contentPid1, contentPid2);
 
         // change md-record data of the item
         itemDoc = EscidocAbstractTest.getDocument(retrieve(itemId));
 
         String newName = "new name";
         String mdXPath = "/item/md-records/md-record[@name='escidoc']/publication/creator[1]/" + "person/family-name";
         Document newItemDoc = (Document) substitute(itemDoc, mdXPath, newName);
 
         update(itemId, toString(newItemDoc, false));
 
         // check if content PID still exists
         Node contentPid3 =
             selectSingleNode(EscidocAbstractTest.getDocument(retrieveComponent(itemId, componentId)), XPATH_CONTENT_PID);
 
         assertNotNull("missing content PID after item update", contentPid3);
         assertEquals(contentPid2, contentPid3.getTextContent());
     }
 
     /**
      * Check if a content PID will be removed from the infrastructure if the content has changed.
      *
      * @throws Exception If anything fails.
      */
     @Test
     public void testCheckNoPidAfterContentUpdate() throws Exception {
         final String componentXpath = "//components/component[properties/mime-type = 'image/jpeg']";
 
         // create item
         Document itemDoc = EscidocAbstractTest.getDocument(createItem());
         final String componentId = getComponentObjidValue(itemDoc, componentXpath);
         final String itemId = getObjidValue(itemDoc);
 
         // assign content PID
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam,
                 getLastModificationDateValue2(getDocument(retrieve(itemId))));
 
         String pidXML = assignContentPid(itemId, componentId, pidParam);
 
         // check if returned content PID equals RELS-EXT entry
         String itemXml = retrieveComponent(itemId, componentId);
 
         assertXmlValidItem(itemXml);
 
         Node contentPid = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_CONTENT_PID);
 
         assertNotNull(contentPid);
 
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pidXML), XPATH_RESULT_PID);
 
         assertEquals(returnedPid.getTextContent(), contentPid.getTextContent());
 
         // change content of the component
         itemDoc = EscidocAbstractTest.getDocument(retrieve(itemId));
 
         Element contentNode = (Element) selectSingleNode(itemDoc, componentXpath + "/content");
         String xlinkNamespacePrefix = determineXlinkNamespacePrefix(itemDoc);
         Attr attr = itemDoc.createAttributeNS(Constants.NS_EXTERNAL_XLINK, xlinkNamespacePrefix + ":href");
        String imageUrl = Constants.WEB_APP_URI_ESCIDOC + "/images/escidoc-logo.jpg";
 
         attr.setValue(imageUrl);
         contentNode.setAttributeNode(attr);
         update(itemId, toString(itemDoc, false));
 
         // check if content PID still exists
         Node contentPid2 =
             selectSingleNode(EscidocAbstractTest.getDocument(retrieveComponent(itemId, componentId)), XPATH_CONTENT_PID);
 
         assertNull("content PID still exists after component update", contentPid2);
     }
 
     /**
      * Check if the last-modification-date timestamp is checked and handled correctly for assignContentPid() method.
      *
      * @throws Exception Thrown if last-modification-date is not checked as required.
      */
     @Test
     public void testOptimisticalLocking03() throws Exception {
 
         Class<?> ec = OptimisticLockingException.class;
         String wrongLmd = "2008-06-17T18:06:01.515Z";
         final int componentNo = 1;
         Document itemDoc = EscidocAbstractTest.getDocument(createItem());
         String itemId = getObjidValue(itemDoc);
         String componentId = getComponentObjidValue(itemDoc, componentNo);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam, new DateTime(wrongLmd, DateTimeZone.UTC));
 
         try {
             assignContentPid(itemId, componentId, pidParam);
             fail("Missing OptimisticalLockingException");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test if value of the PID element within the taskParam XML is used to register the PID. Usually is a new PID
      * identifier is created but this could be skipped to provided register existing PIDs to a resource.
      *
      * @throws Exception Thrown if PID element is not considered.
      */
     @Test
     public void testPidParameter03() throws Exception {
         final int componentNo = 1;
         Document itemDoc = EscidocAbstractTest.getDocument(createItem());
         String itemId = getObjidValue(itemDoc);
         String componentId = getComponentObjidValue(itemDoc, componentNo);
         String pidToRegister = "hdl:testPrefix/" + componentId;
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setPid(pidToRegister);
         String taskParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam, getLastModificationDateValue2(itemDoc));
         String pidXML = assignContentPid(itemId, componentId, taskParam);
         Document pidDoc = getDocument(pidXML);
         Node returnedPid = selectSingleNode(pidDoc, XPATH_RESULT_PID);
 
         assertEquals(pidToRegister, returnedPid.getTextContent());
 
         // check if contentPid has the same value
         Node contentPidNode =
             selectSingleNode(EscidocAbstractTest.getDocument(retrieve(itemId)), XPATH_ITEM_COMPONENTS + "["
                 + componentNo + "]" + XPATH_CONTENT_PID);
 
         assertEquals(returnedPid.getTextContent(), contentPidNode.getTextContent());
     }
 
     /**
      * Test if an empty value of the PID element within the taskParam XML is handled correctly.
      *
      * @throws Exception Thrown if PID element is not considered.
      */
     @Test
     public void testPidParameter04() throws Exception {
         final int componentNo = 1;
         Document itemDoc = EscidocAbstractTest.getDocument(createItem());
         String itemId = getObjidValue(itemDoc);
         String componentId = getComponentObjidValue(itemDoc, componentNo);
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setPid("");
         String taskParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam, getLastModificationDateValue2(itemDoc));
 
         Class<?> ec = XmlCorruptedException.class;
 
         try {
             assignContentPid(itemId, componentId, taskParam);
             fail("Expect exception if pid element in taskParam is empty.");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test the last-modification-date in return value of assignContentPid().
      *
      * @throws Exception Thrown if the last-modification-date in the return value differs from the
      *                   last-modification-date of the resource.
      */
     @Test
     public void testReturnValue01() throws Exception {
         Document itemDoc = EscidocAbstractTest.getDocument(createItem());
         String componentId = getComponentObjidValue(itemDoc, 2);
         String itemId = getObjidValue(itemDoc);
 
         assertNull(itemDoc.getElementById(NAME_PID));
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam,
                 getLastModificationDateValue2(getDocument(retrieve(itemId))));
 
         String resultXml = assignContentPid(itemId, componentId, pidParam);
 
         assertXmlValidResult(resultXml);
 
         Document pidDoc = EscidocAbstractTest.getDocument(resultXml);
         String lmdResult = getLastModificationDateValue(pidDoc);
 
         assertTimestampIsEqualOrAfter("assignContentPid does not create a new timestamp", lmdResult,
             getLastModificationDateValue(itemDoc));
 
         itemDoc = EscidocAbstractTest.getDocument(retrieve(itemId));
         assertEquals("Last modification date of result and item not equal", lmdResult,
             getLastModificationDateValue(itemDoc));
     }
 
     /**
      * Check the assignment of an contentPid.
      *
      * @param itemId      The object id of the item.
      * @param componentId The id of the component.
      * @throws Exception Thrown if anything fails.
      */
     private void assignAndCheckContentPid(final String itemId, final String componentId) throws Exception {
         // assign PID to Component
 
         AssignParam assignPidParam = new AssignParam();
         assignPidParam.setUrl(new URL(this.itemUrl + itemId));
         String pidParam =
             TaskParamFactory.getAssignPidTaskParam(assignPidParam,
                 getLastModificationDateValue2(getDocument(retrieve(itemId))));
 
         String pidXML = assignContentPid(itemId, componentId, pidParam);
 
         // check if returned PID equals RELS-EXT entry
         String itemXml = retrieveComponent(itemId, componentId);
 
         assertXmlValidItem(itemXml);
 
         Node contentPid = selectSingleNode(EscidocAbstractTest.getDocument(itemXml), XPATH_CONTENT_PID);
 
         assertNotNull(contentPid);
 
         Node returnedPid = selectSingleNode(EscidocAbstractTest.getDocument(pidXML), XPATH_RESULT_PID);
 
         assertEquals(returnedPid.getTextContent(), contentPid.getTextContent());
     }
 
     /**
      * Create a new Item.
      *
      * @return The Item XML representation.
      * @throws Exception Thrown if creation of item fails.
      */
     private String createItem() throws Exception {
         String xmlData =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_ITEM_PATH + "/rest", "escidoc_item_198_for_create.xml");
 
         return (create(xmlData));
     }
 }
