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
 import de.escidoc.core.test.TaskParamFactory;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
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
      * 
      * @throws Exception
      */
     @Test
     public void testIssueInfr1007() throws Exception {
         addRelation(this.containerId, TaskParamFactory.ONTOLOGY_IS_REVISION_OF);
         addRelationToLatestVersion(this.containerId, "http://escidoc.org/examples/test1");
         addRelationToLatestVersion(this.containerId, "http://escidoc.org/examples/#test2");
 
         String relationsElementXml = retrieveRelations(this.containerId);
         Document relationsElementDocument = EscidocAbstractTest.getDocument(relationsElementXml);
         selectSingleNodeAsserted(relationsElementDocument, "/relations");
         selectSingleNodeAsserted(relationsElementDocument, "/relations/relation[@predicate = '"
             + TaskParamFactory.ONTOLOGY_IS_REVISION_OF + "']");
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
         addContentRelations(this.containerId + ":" + 1, taskParam);
         String containerWithRelations = retrieve(this.containerId);
         Document containerWithRelationsDocument = EscidocAbstractTest.getDocument(containerWithRelations);
         NodeList relationsNode = selectNodeList(containerWithRelationsDocument, "/container/relations/relation");
         assertEquals("Number of relations is wrong ", relationsNode.getLength(), 1);
 
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
         addContentRelations(this.containerId, taskParam);
         lastModDate = getTheLastModificationParam(this.containerId);
         taskParam = TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = TaskParamFactory.getRelationTaskParam(relations, lastModDate, "bla");
 
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
 
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
 
         String taskParam = TaskParamFactory.getRelationTaskParam(relations, lastModDate, "bla");
 
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
 
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation("escidoc:123:1", null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
 
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
         addContentRelations(this.containerId, taskParam);
 
         lastModDate = getTheLastModificationParam(this.containerId);
 
         taskParam = TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
         removeContentRelations(this.containerId, taskParam);
         String containerWithoutContentRelations = retrieve(this.containerId);
         assertXmlValidContainer(containerWithoutContentRelations);
         Document containerWithoutContentRelationsDoc =
             EscidocAbstractTest.getDocument(containerWithoutContentRelations);
         // assert that the /relations element is still delivered (even if it is
         // empty)
         Node relationsNode = selectSingleNode(containerWithoutContentRelationsDoc, "/container/relations");
         assertNotNull("/relations elements has to exist", relationsNode);
 
         relationsNode = selectSingleNode(containerWithoutContentRelationsDoc, "/container/relations/relation");
         assertNull("relations may not exist", relationsNode);
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
         addContentRelations(this.containerId, taskParam);
 
         lastModDate = getTheLastModificationParam(this.containerId);
 
         taskParam = TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
         removeContentRelations(this.containerId, taskParam);
         lastModDate = getTheLastModificationParam(this.containerId);
         taskParam = TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(sourceId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
         addContentRelations(sourceId, taskParam);
         lastModDate = getTheLastModificationParam(this.containerId);
         taskParam = TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
         String addedRelations = addContentRelations(this.containerId, taskParam);
 
         String relationId = "/ir/container/" + targetId;
         String xmlWithRelation = retrieve(this.containerId);
         Document container = getDocument(xmlWithRelation);
 
         // assert relation exist
         assertXmlExists("relation missing", container, "/container/relations[count(./relation) = '1']");
         assertXmlExists("relation missing", container, "/container/relations/relation[@href = '" + relationId + "']");
 
         Node xmlContainerWithoutFirstRelations = deleteElement(container, "/container/relations");
         String updatedXml = update(this.containerId, toString(xmlContainerWithoutFirstRelations, true));
 
         // assert relation was deleted
         container = EscidocAbstractTest.getDocument(updatedXml);
         assertXmlExists("relation missing", container, "/container/relations[count(./relation) = '0']");
 
         lastModDate = getLastModificationDateValue(container);
         taskParam = TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
         addContentRelations(this.containerId, taskParam);
         String containerXml = retrieve(this.containerId);
 
         // assert relation exist
         container = EscidocAbstractTest.getDocument(containerXml);
         assertXmlExists("relation missing", container, "/container/relations[count(./relation) = '1']");
         assertXmlExists("relation missing", container, "/container/relations/relation[@href = '" + relationId + "']");
     }
 
     /**
      * Test declining adding of an existing "active" relation to the container.
      * 
      * @throws Exception
      */
     @Test
     public void testAddExistingRelation2() throws Exception {
         String xmlContainer =
             getTemplateAsString(TEMPLATE_CONTAINER_PATH + "/rest", "create_container_WithoutMembers_v1.1.xml");
 
         String xml = create(xmlContainer);
 
         String targetId = getObjidValue(getDocument(xml));
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
         String addedRelations = addContentRelations(this.containerId, taskParam);
         lastModDate = getTheLastModificationParam(this.containerId);
         taskParam = TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation("bla", null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
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
 
         String param =
             TaskParamFactory.getStatusTaskParam(getLastModificationDateValue2(getDocument(this.containerXml)), null);
 
         submit(this.containerId, param);
 
         String targetId =
             getObjidValue(create(getTemplateAsString(TEMPLATE_CONTAINER_PATH + "/rest",
                 "create_container_WithoutMembers_v1.1.xml")));
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
 
         // add cr (updated to version 2)
         addContentRelations(this.containerId, taskParam);
 
         String relationId = "/ir/container/" + targetId;
         String submittedWithRelations = retrieve(this.containerId);
 
         // assert relation exist
         assertXmlExists("relation missing", getDocument(submittedWithRelations),
             "/container/relations/relation[@href = '" + relationId + "']");
 
         String newcontainerXml = addCtsElement(submittedWithRelations);
 
         String updatedcontainer = update(containerId, newcontainerXml);
 
         // check that version 1 has still no content relations
         assertXmlExists("wrong relations", getDocument(retrieve(this.containerId + ":1")),
             "/container/relations[count(./relation) = 0]");
 
         // test if relation still exits in version 2 if container is updated to version 3
         Document containerV2 = getDocument(retrieve(this.containerId + ":2"));
         assertXmlExists("wrong relations", containerV2, "/container/relations[count(./relation) = 1]");
         assertXmlExists("relation missing", containerV2, "/container/relations/relation[@href = '" + relationId + "']");
 
         Document containerV3 = getDocument(retrieve(this.containerId));
         assertXmlExists("wrong relations", containerV3, "/container/relations[count(./relation) = 1]");
         assertXmlExists("relation missing", containerV3, "/container/relations/relation[@href = '" + relationId + "']");
 
         containerV3 = getDocument(retrieve(this.containerId + ":3"));
         assertXmlExists("wrong relations", containerV3, "/container/relations[count(./relation) = 1]");
         assertXmlExists("relation missing", containerV3, "/container/relations/relation[@href = '" + relationId + "']");
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
 
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lastModDate, TaskParamFactory.ONTOLOGY_IS_PART_OF);
 
         String resultXml = addContentRelations(this.containerId + ":" + 1, taskParam);
         assertXmlValidResult(resultXml);
         Document resultDoc = EscidocAbstractTest.getDocument(resultXml);
         String lmdAddContent = getLastModificationDateValue(resultDoc);
 
         taskParam =
             TaskParamFactory.getRelationTaskParam(relations, lmdAddContent, TaskParamFactory.ONTOLOGY_IS_PART_OF);
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, predicate));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
        String taskParam = TaskParamFactory.getRelationTaskParam(relations, lastModDate,
                TaskParamFactory.ONTOLOGY_IS_PART_OF);
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
 
         List<TaskParamFactory.Relation> relations = new ArrayList<TaskParamFactory.Relation>(1);
         relations.add(new TaskParamFactory.Relation(targetId, null));
 
         String lastModDate = getTheLastModificationParam(this.containerId);
         String taskParam = TaskParamFactory.getRelationTaskParam(relations, lastModDate, predicate);
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
