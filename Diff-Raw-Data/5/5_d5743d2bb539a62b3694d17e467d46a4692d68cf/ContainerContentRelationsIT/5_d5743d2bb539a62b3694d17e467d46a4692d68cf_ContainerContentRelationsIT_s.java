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
 package de.escidoc.core.test.om.container;
 
 import de.escidoc.core.common.exceptions.remote.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.remote.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.remote.application.violated.AlreadyExistsException;
 import de.escidoc.core.test.EscidocAbstractTest;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * Test the mock implementation of the item resource.
  * 
  * @author Michael Schneider
  */
 public class ContainerContentRelationsIT extends ContainerTestBase {
 
     private String containerId = null;
 
     private String containerXml = null;
 
     /**
      * Set up servlet test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Before
     public void setUp() throws Exception {
         String xmlContainer =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_CONTAINER_PATH + "/rest",
                 "create_container_WithoutMembers_v1.1.xml");
 
         this.containerXml = create(xmlContainer);
         this.containerId = getObjidValue(containerXml);
     }
 
     /**
      * Clean up after servlet test.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Override
     @After
     public void tearDown() throws Exception {
 
         super.tearDown();
 
         try {
             delete(this.containerId);
         }
         catch (final Exception e) {
             // do nothing
         }
 
     }
 
     @Test
     public void testIssueInfr1007() throws Exception {
         addRelation(this.containerId, "http://www.escidoc.de/ontologies/mpdl-ontologies/content-relations#isRevisionOf");
         addRelationToLatestVersion(this.containerId, "http://escidoc.org/examples/test1");
         addRelationToLatestVersion(this.containerId, "http://escidoc.org/examples/#test2");
 
         String relationsElementXml = retrieveRelations(this.containerId);
         Document relationsElementDocument = EscidocAbstractTest.getDocument(relationsElementXml);
         selectSingleNodeAsserted(relationsElementDocument, "/relations");
         selectSingleNodeAsserted(relationsElementDocument,
             "/relations/relation[@predicate = 'http://www.escidoc.de/ontologies/mpdl-ontologies/content-relations#isRevisionOf']");
         selectSingleNodeAsserted(relationsElementDocument,
             "/relations/relation[@predicate = 'http://escidoc.org/examples/test1']");
         selectSingleNodeAsserted(relationsElementDocument,
             "/relations/relation[@predicate = 'http://escidoc.org/examples/#test2']");
         assertXmlValidContainer(relationsElementXml);
     }
 
     /**
      * Tets successfully adding a new relation to the container.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelation() throws Exception {
 
         String targetId = createContainer();
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets);
         addContentRelations(this.containerId + ":" + 1, taskParam);
         String containerWithRelations = retrieve(this.containerId);
         Document containerWithRelationsDocument = EscidocAbstractTest.getDocument(containerWithRelations);
         NodeList relations = selectNodeList(containerWithRelationsDocument, "/container/relations/relation");
         assertEquals("Number of relations is wrong ", relations.getLength(), 1);
 
         NodeList relationTargets =
             selectNodeList(containerWithRelationsDocument, "/container/relations/relation/@href");
 
         boolean contains = false;
 
         for (int i = relationTargets.getLength() - 1; i >= 0; i--) {
             String id = relationTargets.item(i).getNodeValue();
 
             if (id.matches(".*" + targetId + "$")) {
                 contains = true;
             }
         }
 
         assertTrue("added relation targetId is not in the relation list ", contains);
 
         assertXmlValidContainer(containerWithRelations);
 
         String relationsElementXml = retrieveRelations(this.containerId);
         selectSingleNodeAsserted(EscidocAbstractTest.getDocument(relationsElementXml), "/relations");
         assertXmlValidRelations(relationsElementXml);
 
         // TODO this is a work around until a method exists where the whole
         // Container XML could be validated.
         assertContainerXlinkTitles(containerWithRelations);
     }
 
     /**
      * Test declining adding of an existing relation to the container.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddExistingRelation() throws Exception {
 
         String targetId = createContainer();
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets);
         addContentRelations(this.containerId, taskParam);
         lastModDate = getTheLastModificationParam(this.containerId);
         taskParam = getTaskParameter(lastModDate, targets);
         try {
             addContentRelations(this.containerId, taskParam);
             fail("No exception occurred on added an existing relation to " + "the item");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("AlreadyExistException expected.", AlreadyExistsException.class, e);
         }
 
     }
 
     /**
      * Test declining adding of an relation with a non existing predicate to the container.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelationWithNonExistingPredicate() throws Exception {
 
         String targetId = createContainer();
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets, "bla");
 
         try {
             addContentRelations(this.containerId, taskParam);
             fail("No exception occurred on added an relation with non " + "existing target to the item");
         }
         catch (final Exception e) {
             Class<?> ec = RelationPredicateNotFoundException.class;
             EscidocAbstractTest.assertExceptionType(ec.getName(), ec, e);
         }
     }
 
     /**
      * Test declining adding of an relation with a non existing target to the container.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelationWithNonExistingTarget() throws Exception {
 
         String targetId = "bla";
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets);
 
         try {
             addContentRelations(this.containerId, taskParam);
             fail("No exception occurred on added an relation with non " + "existing target to the item");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("ReferencedResourceNotFoundException.",
                 ReferencedResourceNotFoundException.class, e);
         }
 
     }
 
     /**
      * Test declining adding of an relation with a non existing predicate to the container.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelationWithWrongPredicate() throws Exception {
 
         String targetId = createContainer();
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
 
         String taskParam = getTaskParameter(lastModDate, targets, "bla");
 
         try {
             addContentRelations(this.containerId, taskParam);
             fail("No exception occurred on added an relation with non " + "existing predicate to the item");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("RelationPredicateNotFoundException.",
                 RelationPredicateNotFoundException.class, e);
         }
 
     }
 
     /**
      * Test declining adding of an relation without container id.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelationWithoutId() throws Exception {
 
         String targetId = createContainer();
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets);
 
         try {
             addContentRelations(null, taskParam);
             fail("No exception occurred on adding an relation without source id.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining adding of an relation without container id.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelationWithoutTaskParam() throws Exception {
 
         try {
             addContentRelations(this.containerId, null);
             fail("No exception occurred on adding an relation without " + "task parameter.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
     }
 
     /**
      * Test declining adding of an relation with a target id containing a version number.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testAddRelationWithTargetContainingVersionNumber() throws Exception {
 
         Vector<String> targets = new Vector<String>();
         targets.add("escidoc:123:1");
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets);
 
         try {
             addContentRelations(this.containerId, taskParam);
             fail("No exception occurred on added an relation with target " + "contains version number to the container");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("InvalidContentException.", InvalidContentException.class, e);
         }
 
     }
 
     /**
      * Test successfully removing an existing relation.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRemoveRelation() throws Exception {
 
         String targetId = createContainer();
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets);
         addContentRelations(this.containerId, taskParam);
 
         lastModDate = getTheLastModificationParam(this.containerId);
 
         taskParam = getTaskParameter(lastModDate, targets);
         removeContentRelations(this.containerId, taskParam);
         String containerWithoutContentRelations = retrieve(this.containerId);
         assertXmlValidContainer(containerWithoutContentRelations);
         Document containerWithoutContentRelationsDoc =
             EscidocAbstractTest.getDocument(containerWithoutContentRelations);
         // assert that the /relations element is still delivered (even if it is
         // empty)
         Node relations = selectSingleNode(containerWithoutContentRelationsDoc, "/container/relations");
         assertNotNull("/relations elements has to exist", relations);
 
         relations = selectSingleNode(containerWithoutContentRelationsDoc, "/container/relations/relation");
         assertNull("relations may not exist", relations);
     }
 
     /**
      * Test declining removing of a already deleted relation.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRemoveDeletedRelation() throws Exception {
 
         String targetId = createContainer();
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets);
         addContentRelations(this.containerId, taskParam);
 
         lastModDate = getTheLastModificationParam(this.containerId);
 
         taskParam = getTaskParameter(lastModDate, targets);
         removeContentRelations(this.containerId, taskParam);
         lastModDate = getTheLastModificationParam(this.containerId);
         taskParam = getTaskParameter(lastModDate, targets);
         try {
             removeContentRelations(this.containerId, taskParam);
             fail("No exception occurred on remove a already deleted relation");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("ContentRelationNotFoundException expected.",
                 ContentRelationNotFoundException.class, e);
         }
     }
 
     /**
      * Test declining removing of an existing relation, which belongs to another source resource.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRemoveRelationWithWrongSource() throws Exception {
 
         String targetId = createContainer();
         String sourceId = createContainer();
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(sourceId);
         String taskParam = getTaskParameter(lastModDate, targets);
         addContentRelations(sourceId, taskParam);
         lastModDate = getTheLastModificationParam(this.containerId);
         taskParam = getTaskParameter(lastModDate, targets);
         try {
             removeContentRelations(this.containerId, taskParam);
             fail("No exception occurred on remove an relation with a" + " wrong source");
         }
         catch (final Exception e) {
             EscidocAbstractTest.assertExceptionType("ContentRelationNotFoundException.",
                 ContentRelationNotFoundException.class, e);
         }
 
     }
 
     /**
      * @param id
      *            The id of the resource.
      * @return The date of last modification of the resource as string.
      * @throws Exception
      *             If anything fails.
      */
     private String getTheLastModificationParam(final String id) throws Exception {
         Document container = EscidocAbstractTest.getDocument(retrieve(id));
 
         // get last-modification-date
         NamedNodeMap atts = container.getDocumentElement().getAttributes();
         Node lastModificationDateNode = atts.getNamedItem("last-modification-date");
         String lastModificationDate = lastModificationDateNode.getNodeValue();
 
         return lastModificationDate;
     }
 
     /**
      * @param lastModDate
      *            The last modification date of the source.
      * @param targets
      *            List of target ids. As much relations are added as there are tagets.
      * @return The task parameter according to the given values.
      */
     private String getTaskParameter(final String lastModDate, final Vector<String> targets) {
 
         return getTaskParameter(lastModDate, targets, null);
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
     private String getTaskParameter(final String lastModDate, final Vector<String> targets, final String predicate) {
         String taskParam = null;
         if ((targets != null) && (targets.size() > 0)) {
             taskParam = "<param last-modification-date=\"" + lastModDate + "\">";
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
      * Test successfully adding an existing "inactive" relation to the container.
      * 
      * @throws Exception
      */
     @Test
     public void testAddExistingInvalidRelation() throws Exception {
 
         String xmlContainer =
             getTemplateAsString(TEMPLATE_CONTAINER_PATH + "/rest", "create_container_WithoutMembers_v1.1.xml");
 
         String xml = create(xmlContainer);
 
         String targetId = getObjidValue(getDocument(xml));
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameterForAddRelations(lastModDate, targets);
         String addedRelations = addContentRelations(this.containerId, taskParam);
         String relationId = selectSingleNode(getDocument(addedRelations), "/param/relation[1]/@href").getTextContent();
         String xmlWithRelation = retrieve(this.containerId);
         Document container = getDocument(xmlWithRelation);
         Node xmlContainerWithoutFirstRelations = deleteElement(container, "/container/relations");
         String updatedXml = update(this.containerId, toString(xmlContainerWithoutFirstRelations, true));
         lastModDate = getTheLastModificationParam(this.containerId);
         taskParam = getTaskParameterForAddRelations(lastModDate, targets);
         addedRelations = addContentRelations(this.containerId, taskParam);
         String containerXml = retrieve(this.containerId);
         String retrivedRelationId =
             selectSingleNode(getDocument(containerXml), "/container/relations/relation[1]/@href").getTextContent();
         assertEquals("relation ids are not equal", relationId, retrivedRelationId);
     }
 
     /**
      * Test declining adding of an existing "active" relation to the container.
      * 
      * @throws Exception
      */
     @Test
     public void testAddExistingRelation2() throws Exception {
        String xmlContainer = getTemplateAsString(TEMPLATE_CONTAINER_PATH, "create_container_WithoutMembers_v1.1.xml");
 
         String xml = create(xmlContainer);
 
        String targetId = selectSingleNode(getDocument(xml), "/container/@objid").getTextContent();
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameterForAddRelations(lastModDate, targets);
         String addedRelations = addContentRelations(this.containerId, taskParam);
         lastModDate = getTheLastModificationParam(this.containerId);
         taskParam = getTaskParameterForAddRelations(lastModDate, targets);
         try {
             addedRelations = addContentRelations(this.containerId, taskParam);
             fail("No exception occurred on added an existing relation to the container");
         }
         catch (final Exception e) {
             assertExceptionType("AlreadyExistException expected.", AlreadyExistsException.class, e);
         }
 
     }
 
     /**
      * Test declining removing of an non existing relation.
      */
     @Test
     public void testRemoveNonExistingRelation() throws Exception {
 
         Vector<String> ids = new Vector<String>();
         ids.add("bla");
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, ids);
         try {
             removeContentRelations(this.containerId, taskParam);
             fail("No exception occurred on remove of a nonexising relation.");
         }
         catch (final Exception e) {
             assertExceptionType("ContentRelationNotFoundException expected.", ContentRelationNotFoundException.class, e);
         }
 
     }
 
     /**
      * Test successfully retrieving a last version of an container, which has an active relation in the last version but
      * not in the old version.
      * 
      * @throws Exception
      */
     @Test
     public void testRelationsWithVersionedContainer() throws Exception {
         String param = "<param last-modification-date=\"" + getTheLastModificationParam(this.containerId) + "\" ";
         param += "/>";
 
         submit(this.containerId, param);
         String submittedcontainer = retrieve(this.containerId);
 
         String target =
             create(getTemplateAsString(TEMPLATE_CONTAINER_PATH + "/rest", "create_container_WithoutMembers_v1.1.xml"));
         String targetId = null;
         Pattern PATTERN_OBJID_ATTRIBUTE = Pattern.compile("objid=\"([^\"]*)\"");
         Matcher m = PATTERN_OBJID_ATTRIBUTE.matcher(target);
         if (m.find()) {
             targetId = m.group(1);
         }
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameterForAddRelations(lastModDate, targets);
         String addedRelations = addContentRelations(this.containerId, taskParam);
         String relationId = selectSingleNode(getDocument(addedRelations), "/param/relation[1]/@objid").getTextContent();
         String submittedWithRelations = retrieve(this.containerId);
         String newcontainerXml = addCtsElement(submittedWithRelations);
 
         String updatedcontainer = update(containerId, newcontainerXml);
         String containerVersion1 = retrieve(this.containerId + ":1");
         String container = retrieve(this.containerId);
         Node relations = selectSingleNode(getDocument(containerVersion1), "/container/relations");
         assertNull("relations may not exist", relations);
         String retrievedRelationId =
             selectSingleNode(getDocument(container), "/container/relations/relation[1]/@objid").getTextContent();
         assertEquals("relation ids are not equal", relationId, retrievedRelationId);
 
     }
 
     /**
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveNonexistingRelations() throws Exception {
         try {
             String relationsElementXml = retrieveRelations(this.containerId);
             assertXmlValidRelations(relationsElementXml);
             Node relationsElementDoc = EscidocAbstractTest.getDocument(relationsElementXml);
             // selectSingleNodeAsserted(relationsElementDoc, "/relations");
             assertNull(selectSingleNode(relationsElementDoc, "/relations/relation"));
         }
         catch (final Exception e) {
             Class<?> ec = ResourceNotFoundException.class;
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
     }
 
     /**
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveRelations() throws Exception {
         addRelation(this.containerId, null);
 
         String relationsElementXml = retrieveRelations(this.containerId);
         selectSingleNodeAsserted(EscidocAbstractTest.getDocument(relationsElementXml), "/relations");
         assertXmlValidRelations(relationsElementXml);
     }
 
     /**
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveRelationsWithoutId() throws Exception {
         addRelation(this.containerId, null);
 
         try {
             retrieveRelations(null);
             fail("No exception when retrieveRelations without id.");
         }
         catch (final Exception e) {
             Class<?> ec = MissingMethodParameterException.class;
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
     }
 
     /**
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testRetrieveRelationsWithWrongId() throws Exception {
         addRelation(this.containerId, null);
 
         try {
             retrieveRelations("bla");
             fail("No exception when retrieveRelations with wrong id.");
         }
         catch (final Exception e) {
             Class<?> ec = ContainerNotFoundException.class;
             EscidocAbstractTest.assertExceptionType(ec, e);
         }
     }
 
     /**
      * Tets successfully adding a new relation to the container.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testReturnValueOfAddRelation() throws Exception {
 
         String targetId = createContainer();
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets);
 
         String resultXml = addContentRelations(this.containerId + ":" + 1, taskParam);
         assertXmlValidResult(resultXml);
         Document resultDoc = EscidocAbstractTest.getDocument(resultXml);
         String lmdResult = getLastModificationDateValue(resultDoc);
 
         assertTimestampIsEqualOrAfter("add relation does not create a new timestamp", lmdResult, lastModDate);
 
         lastModDate = getTheLastModificationParam(this.containerId);
         assertEquals("Last modification date of result and Container not equal", lmdResult, lastModDate);
 
     }
 
     /**
      * Tets successfully adding a new relation to the container.
      * 
      * @throws Exception
      *             If anything fails.
      */
     @Test
     public void testReturnValueOfRemoveRelation() throws Exception {
 
         String targetId = createContainer();
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets);
 
         String resultXml = addContentRelations(this.containerId + ":" + 1, taskParam);
         assertXmlValidResult(resultXml);
         Document resultDoc = EscidocAbstractTest.getDocument(resultXml);
         String lmdAddContent = getLastModificationDateValue(resultDoc);
 
         taskParam = getTaskParameter(lmdAddContent, targets);
         resultXml = removeContentRelations(this.containerId, taskParam);
 
         assertXmlValidResult(resultXml);
         resultDoc = EscidocAbstractTest.getDocument(resultXml);
         String lmdResult = getLastModificationDateValue(resultDoc);
 
         assertTimestampIsEqualOrAfter("remove relation does not create a new timestamp", lmdResult, lmdAddContent);
 
         lastModDate = getTheLastModificationParam(this.containerId);
         assertEquals("Last modification date of result and Container not equal", lmdResult, lastModDate);
 
     }
 
     /**
      * @param objectId
      *            The id of the object to which the relation should be added. The source id.
      * @param predicate
      *            The predicate of the relation.
      * @throws Exception
      *             If anything fails.
      */
     private void addRelation(final String objectId, final String predicate) throws Exception {
 
         String targetId = createContainer();
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets, predicate);
         addContentRelations(this.containerId + ":" + 1, taskParam);
     }
 
     /**
      * 
      * @param objectId
      * @param predicate
      * @throws Exception
      */
     private void addRelationToLatestVersion(final String objectId, final String predicate) throws Exception {
 
         String targetId = createContainer();
 
         Vector<String> targets = new Vector<String>();
         targets.add(targetId);
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = getTaskParameter(lastModDate, targets, predicate);
         addContentRelations(this.containerId, taskParam);
     }
 
     /**
      * Create a Container.
      * 
      * @return objid of container.
      * @throws Exception
      *             Thrown if creation or objid exctraction fails.
      */
     private String createContainer() throws Exception {
         String xmlContainer =
             EscidocAbstractTest.getTemplateAsString(TEMPLATE_CONTAINER_PATH + "/rest",
                 "create_container_WithoutMembers_v1.1.xml");
         String xml = create(xmlContainer);
         return getObjidValue(xml);
     }
 }
