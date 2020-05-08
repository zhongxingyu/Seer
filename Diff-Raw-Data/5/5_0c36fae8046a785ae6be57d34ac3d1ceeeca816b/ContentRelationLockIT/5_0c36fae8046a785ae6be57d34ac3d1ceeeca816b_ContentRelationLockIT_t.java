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
 package de.escidoc.core.test.om.contentRelation;
 
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.remote.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.remote.application.violated.OptimisticLockingException;
 import de.escidoc.core.test.EscidocAbstractTest;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.security.client.PWCallback;
 
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 /**
  * Test content relation create implementation.
  * 
  * @author Steffen Wagner
  */
 public class ContentRelationLockIT extends ContentRelationTestBase {
 
     private String theContentRelationXml;
 
     private String theContentRelationId;
 
     private String[] user = null;
 
     /**
      * Set up servlet test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Before
     public void setUp() throws Exception {
         this.user = createUserWithContentRelationRole("escidoc_useraccount_for_create.xml");
         addContentRelationManagerGrant(this.user[0]);
         addContentRelationManagerGrant("escidoc:testdepositor");
 
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
         String contentRelationXml = getExampleTemplate("content-relation-01.xml");
         theContentRelationXml = create(contentRelationXml);
         theContentRelationId = getObjidValue(EscidocAbstractTest.getDocument(theContentRelationXml));
 
     }
 
     /**
      * Clean up after test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Override
     @After
     public void tearDown() throws Exception {
 
         super.tearDown();
         PWCallback.resetHandle();
         try {
             delete(theContentRelationId);
         }
         catch (final LockingException e) {
         }
     }
 
     /**
      * Successfully lock of container.
      */
     @Test
     public void testOM_C_lock() throws Exception {
 
         String param = getLockTaskParam(theContentRelationId);
         lock(theContentRelationId, param);
 
         String contentRelationXml = retrieve(theContentRelationId);
         Document contentRelationDoc = EscidocAbstractTest.getDocument(contentRelationXml);
         assertXmlEquals("Content relation lock status not as expected", contentRelationDoc,
             "/content-relation/properties/lock-status", "locked");
         assertXmlNotNull("lock-date", contentRelationDoc, "/content-relation/properties/lock-date");
 
         String lockOwner =
             getObjidFromHref(selectSingleNode(contentRelationDoc, "/content-relation/properties/lock-owner/@href")
                 .getTextContent());
 
         assertNotNull(lockOwner);
         assertXmlValidContentRelation(contentRelationXml);
 
         PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         param = getLockTaskParam(theContentRelationId);
         try {
             update(theContentRelationId, contentRelationXml);
             fail("No exception on update after lock.");
         }
         catch (final Exception e) {
             Class<?> ec = LockingException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
         PWCallback.setHandle(PWCallback.DEPOSITOR_HANDLE);
         unlock(theContentRelationId, param);
     }
 
     @Test
     public void testOM_C_lockSelfUpdate() throws Exception {
 
         String param = getLockTaskParam(theContentRelationId);
         lock(theContentRelationId, param);
 
         String contentRelationXml = retrieve(theContentRelationId);
         Document contentRelationDoc = EscidocAbstractTest.getDocument(contentRelationXml);
         assertXmlEquals("content Relation lock status not as expected", contentRelationDoc,
             "/content-relation/properties/lock-status", "locked");
         assertXmlNotNull("lock-date", contentRelationDoc, "/content-relation/properties/lock-date");
 
         String lockOwner =
             getObjidFromHref(selectSingleNode(contentRelationDoc, "/content-relation/properties/lock-owner/@href")
                 .getTextContent());
         assertNotNull(lockOwner);
 
         assertXmlValidContentRelation(contentRelationXml);
 
         param = getLockTaskParam(theContentRelationId);
         update(theContentRelationId, contentRelationXml);
         param = getLockTaskParam(theContentRelationId);
         unlock(theContentRelationId, param);
 
     }
 
     /**
      * Succesfully unlock item by the lock owner.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOM_ULI_1_1() throws Exception {
 
         String param = getLockTaskParam(theContentRelationId);
         try {
             lock(theContentRelationId, param);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Unlocking content relation can not be tested, locking failed"
                 + " with exception.", e);
         }
 
         try {
             unlock(theContentRelationId, param);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Unlocking item failed with exception. ", e);
         }
 
         String contentRelationXml = retrieve(theContentRelationId);
 
         Document contentRelationDoc = EscidocAbstractTest.getDocument(contentRelationXml);
         assertXmlEquals("content-relation lock status not as expected", contentRelationDoc,
             "/content-relation/properties/lock-status", "unlocked");
 
         assertXmlNotExists("Unexpected element lock-date in " + "unlocked content-relation.", contentRelationDoc,
             "/content-relation/properties/lock-date");
         assertXmlNotExists("Unexpected element lock-owner in " + "unlocked content-relation.", contentRelationDoc,
             "/content-relation/properties/lock-owner");
 
         // try to call update by System-Administrator
         PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
         param = getLockTaskParam(theContentRelationId);
 
         try {
             update(theContentRelationId, contentRelationXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Updating unlocked content relation failed with exception. ", e);
         }
 
     }
 
     /**
      * Succesfully unlock item by a system administrator.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOM_ULI_1_2() throws Exception {
 
         String param = getLockTaskParam(theContentRelationId);
         try {
             lock(theContentRelationId, param);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Unlocking item can not be tested, locking failed" + " with exception.",
                 e);
         }
 
         PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
 
         try {
             unlock(theContentRelationId, param);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Unlocking item failed with exception. ", e);
         }
 
         String contentRelationXml = retrieve(theContentRelationId);
 
         Document contentRelationDoc = EscidocAbstractTest.getDocument(contentRelationXml);
         assertXmlEquals("content-relation lock status not as expected", contentRelationDoc,
             "/content-relation/properties/lock-status", "unlocked");
 
         assertXmlNotExists("Unexpected element lock-date in unlocked content-relation.", contentRelationDoc,
             "/content-relation/properties/lock-date");
         assertXmlNotExists("Unexpected element lock-owner in unlocked content-relation.", contentRelationDoc,
             "/content-relation/properties/lock-owner");
 
         // try to call update by System-Administrator
         param = getLockTaskParam(theContentRelationId);
 
         try {
             update(theContentRelationId, contentRelationXml);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Updating unlocked content relation failed with exception. ", e);
         }
 
     }
 
     /**
      * Declining unlock item by a user that is not the lock owner.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOM_ULI_2() throws Exception {
 
         PWCallback.setHandle(PWCallback.DEFAULT_HANDLE);
 
         String param = getLockTaskParam(theContentRelationId);
         try {
             lock(theContentRelationId, param);
         }
         catch (final Exception e) {
             EscidocAbstractTest.failException("Unlocking content relation can not be tested, locking failed"
                 + " with exception.", e);
         }
 
         String handle = login(this.user[1], Constants.DEFAULT_USER_PASSWORD, true);
         PWCallback.setHandle(handle);
 
         try {
             unlock(theContentRelationId, param);
             EscidocAbstractTest.failMissingException(AuthorizationException.class);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(AuthorizationException.class, e);
         }
     }
 
     /**
      * Unsuccessfully lock container with wrong container objid.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testOM_C_lockWrongID() throws Exception {
 
         Class<?> ec = ContentRelationNotFoundException.class;
 
         String param = getLockTaskParam(theContentRelationId);
 
         try {
             lock("escidoc:noExist", param);
             EscidocAbstractTest.failMissingException("No exception after lock with non existing id.", ec);
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
     }
 
     /**
      * unsuccessfully lock container with wrong last-modification-date
      */
     @Test
     public void testOM_C_lockOptimisicLocking() throws Exception {
 
        String param =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<param xmlns=\"http://www.escidoc.org/schemas/lock-task-param/0.1\" "
                + "last-modification-date=\"1970-01-01T00:00:00.000Z\" />";
 
         try {
             lock(theContentRelationId, param);
             fail("No exception after lock with wrong last-modification-date.");
         }
         catch (final Exception e) {
             Class<?> ec = OptimisticLockingException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * unsuccessfully lock container without container id
      */
     @Test
     public void testOM_C_lockWithoutID() throws Exception {
 
         String param = getLockTaskParam(theContentRelationId);
 
         try {
             lock(null, param);
             fail("No exception after lock without id.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName() + " expected.", ec, e);
         }
     }
 
     /**
      * Test the last modification date timestamp of the lock/unlock method.
      * 
      * @throws Exception
      *             Thrown if anything failed.
      */
     @Test
     public void testLockReturnValue01() throws Exception {
 
         String param = getLockTaskParam(theContentRelationId);
         String resultXml = lock(theContentRelationId, param);
         assertXmlValidResult(resultXml);
 
         Document resultDoc = EscidocAbstractTest.getDocument(resultXml);
         String lmdResultLock = getLastModificationDateValue(resultDoc);
 
         String contentRelationXml = retrieve(theContentRelationId);
         Document contentRelationDoc = EscidocAbstractTest.getDocument(contentRelationXml);
         String lmdRetrieve = getLastModificationDateValue(contentRelationDoc);
 
         assertEquals("Last modification date of result and content relation " + "not equal", lmdResultLock, lmdRetrieve);
 
         // now check unlock
         resultXml = unlock(theContentRelationId, param);
         assertXmlValidResult(resultXml);
         resultDoc = EscidocAbstractTest.getDocument(resultXml);
         String lmdResultUnlock = getLastModificationDateValue(resultDoc);
 
         contentRelationXml = retrieve(theContentRelationId);
         contentRelationDoc = EscidocAbstractTest.getDocument(contentRelationXml);
         lmdRetrieve = getLastModificationDateValue(contentRelationDoc);
 
         assertEquals("Last modification date of result and content relation not " + "equal", lmdResultUnlock,
             lmdRetrieve);
 
         // assert that the last-modification-date of item hasn't changed
         assertEquals("Last modification date of result and content relation not" + " equal", lmdResultUnlock,
             lmdResultLock);
     }
 
 }
