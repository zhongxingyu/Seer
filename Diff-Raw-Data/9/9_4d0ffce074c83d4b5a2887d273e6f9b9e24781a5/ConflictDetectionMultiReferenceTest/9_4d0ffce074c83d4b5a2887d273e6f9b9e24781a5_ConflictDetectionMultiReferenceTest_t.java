 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  */
 package org.eclipse.emf.emfstore.client.test.conflictDetection;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.client.test.model.document.DocumentFactory;
 import org.eclipse.emf.emfstore.client.test.model.document.LeafSection;
 import org.eclipse.emf.emfstore.client.test.model.requirement.Actor;
 import org.eclipse.emf.emfstore.client.test.model.requirement.RequirementFactory;
 import org.eclipse.emf.emfstore.client.test.model.requirement.UseCase;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.server.conflictDetection.ConflictDetector;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.junit.Test;
 
 /**
  * Tests conflict detection behaviour on attributes.
  * 
  * @author chodnick
  */
 public class ConflictDetectionMultiReferenceTest extends ConflictDetectionTest {
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictAddAddSameObjectSameIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				section2.getModelElements().add(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// same operations going on in both working copies, no conflicts expected
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddAddSameObjectSameIndexNonZero() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				section.getModelElements().add(dummy);
 				getProject().addModelElement(actor);
 				clearOperations();
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				section2.getModelElements().add(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// same operations going on in both working copies, no conflicts expected
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddAddSameObjectDifferentIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(actor);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				section2.getModelElements().add(dummy2);
 				section2.getModelElements().add(actor2);
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 
 		// index conflicts expected: op from project1 index-conflicts with both ops from project2
 		assertEquals(2, cd.getConflictingIndexIntegrity(ops1, ops2).size());
 		assertEquals(1, cd.getConflictingIndexIntegrity(ops2, ops1).size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetAddSameObjectSameIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId section1Id = getProject().getModelElementId(section);
 		ModelElementId section2Id = project2.getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(section1Id);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(section2Id);
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				actor2.setLeafSection(section2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 
 		// no index conflict expected: the operations are perfect opposites
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		assertEquals(0, cd.getConflictingIndexIntegrity(ops1, ops2).size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetParentSetSameObjectSameIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId section1Id = getProject().getModelElementId(section);
 		ModelElementId section2Id = project2.getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(section1Id);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(section2Id);
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				actor2.setLeafSection(section2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 
 		// no index conflict expected: operations are identical
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		assertEquals(0, cd.getConflictingIndexIntegrity(ops1, ops2).size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetAddSameObjectDifferentIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				section2.getModelElements().add(dummy2);
 				actor2.setLeafSection(section2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 
 		// ops1,ops2 not same as ops1,ops2
 		// assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 		// .size());
 		assertEquals(2, cd.getConflictingIndexIntegrity(ops1, ops2).size());
 		assertEquals(1, cd.getConflictingIndexIntegrity(ops2, ops1).size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetParentSetSameObjectDifferentIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 
 				clearOperations();
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 
 				section2.getModelElements().add(dummy2);
 				actor2.setLeafSection(section2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 
 		// assertEquals(cd.getConflictingIndexIntegrity(ops2, ops1).size(), cd.getConflictingIndexIntegrity(ops1, ops2)
 		// .size());
 		assertEquals(2, cd.getConflictingIndexIntegrity(ops1, ops2).size());
 		assertEquals(1, cd.getConflictingIndexIntegrity(ops2, ops1).size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddAddDifferentObjectSameIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				section2.getModelElements().add(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// obviously an index-integrity conflict
 		assertEquals(conflicts.size(), 1);
 
 		assertEquals(cd.getConflicting(ops1, ops2).size(), cd.getConflicting(ops2, ops1).size());
 		assertEquals(0, cd.getConflicting(ops2, ops1).size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddAddDifferentObjectDifferentIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				section2.getModelElements().add(actor2);
 				section2.getModelElements().add(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// obviously an index-integrity conflict
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetAddDifferentObjectSameIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				section2.getModelElements().add(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetAddDifferentObjectDifferentIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				section2.getModelElements().add(actor2);
 				section2.getModelElements().add(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		assertEquals(1, cd.getConflictingIndexIntegrity(ops2, ops1).size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetParentSetDifferentObjectSameIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				dummy2.setLeafSection(section2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// obviously an index-integrity conflict
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetParentSetDifferentObjectDifferentIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				section2.getModelElements().add(actor2);
 				section2.getModelElements().add(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		assertEquals(cd.getConflictingIndexIntegrity(ops2, ops1).size(), cd.getConflictingIndexIntegrity(ops1, ops2)
 			.size());
 		assertEquals(1, cd.getConflictingIndexIntegrity(ops1, ops2).size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddRemoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				section2.getModelElements().add(actor2);
 				section2.getModelElements().remove(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// hard conflict between add and remove, serialization matters
 		Set<AbstractOperation> conflicts = cd.getConflicting(ops1, ops2);
 		assertEquals(cd.getConflicting(ops1, ops2).size(), cd.getConflicting(ops2, ops1).size());
 
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddParentSetRemoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				section2.getModelElements().add(actor2);
 				actor2.setLeafSection(otherSection2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// hard conflict between add and remove, serialization matters
 		Set<AbstractOperation> conflicts = cd.getConflicting(ops1, ops2);
 		assertEquals(cd.getConflicting(ops1, ops2).size(), cd.getConflicting(ops2, ops1).size());
 
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddRemoveIndirectlySameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				section2.getModelElements().add(actor2);
 				otherSection2.getModelElements().add(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// hard conflict between add and remove, serialization matters
 		Set<AbstractOperation> conflicts = cd.getConflicting(ops1, ops2);
 		assertEquals(cd.getConflicting(ops1, ops2).size(), cd.getConflicting(ops2, ops1).size());
 
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetAddParentSetRemoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId section1Id = getProject().getModelElementId(section);
 		ModelElementId section2Id = project2.getModelElementId(section);
 		ModelElementId otherSection2Id = project2.getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(section1Id);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(section2Id);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSection2Id);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				actor2.setLeafSection(section2);
 				actor2.setLeafSection(otherSection2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// hard conflict between add and remove, serialization matters
 		Set<AbstractOperation> conflicts = cd.getConflicting(ops1, ops2);
 		assertEquals(cd.getConflicting(ops1, ops2).size(), cd.getConflicting(ops2, ops1).size());
 
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetAddRemoveIndirectlySameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				actor2.setLeafSection(section2);
 				otherSection2.getModelElements().add(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// hard conflict between add and remove, serialization matters
 		Set<AbstractOperation> conflicts = cd.getConflicting(ops1, ops2);
 		assertEquals(cd.getConflicting(ops1, ops2).size(), cd.getConflicting(ops2, ops1).size());
 
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetAddRemoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				actor2.setLeafSection(section2);
 				section2.getModelElements().remove(actor2);
 
 			}
 		}.run(false);
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// hard conflict between add and remove, serialization matters
 		Set<AbstractOperation> conflicts = cd.getConflicting(ops1, ops2);
 		assertEquals(cd.getConflicting(ops1, ops2).size(), cd.getConflicting(ops2, ops1).size());
 
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetAddIndirectRemoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				actor2.setLeafSection(section2);
 				otherSection2.getModelElements().add(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// hard conflict between add and remove, serialization matters
 		Set<AbstractOperation> conflicts = cd.getConflicting(ops1, ops2);
 		assertEquals(cd.getConflicting(ops1, ops2).size(), cd.getConflicting(ops2, ops1).size());
 
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddIndirectRemoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				section2.getModelElements().add(actor2);
 				otherSection2.getModelElements().add(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// hard conflict between add and remove, serialization matters
 		Set<AbstractOperation> conflicts = cd.getConflicting(ops1, ops2);
 		assertEquals(cd.getConflicting(ops1, ops2).size(), cd.getConflicting(ops2, ops1).size());
 
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictRemoveRemoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				actor.setLeafSection(section);
 				clearOperations();
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().remove(actor1);
 				section2.getModelElements().remove(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 
 		// no index conflict
 		Set<AbstractOperation> indexConflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		assertEquals(0, indexConflicts.size());
 		// no hard conflict
 		Set<AbstractOperation> hardConflicts = cd.getConflicting(ops1, ops2);
 		assertEquals(cd.getConflicting(ops1, ops2).size(), cd.getConflicting(ops2, ops1).size());
 
		assertEquals(1, hardConflicts.size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictRemoveRemoveIndirectlySameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				actor.setLeafSection(section);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().remove(actor1);
 				otherSection2.getModelElements().add(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// no index conflict
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		assertEquals(0, conflicts.size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetRemoveRemoveIndirectlySameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				actor.setLeafSection(section);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				otherSection2.getModelElements().add(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// no index conflict
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetRemoveParentSetRemoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection anotherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(anotherSection);
 				getProject().addModelElement(actor);
 
 				actor.setLeafSection(section);
 
 				clearOperations();
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId otherSection1Id = getProject().getModelElementId(otherSection);
 		ModelElementId anotherSection2Id = project2.getModelElementId(anotherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection otherSection1 = (LeafSection) getProject().getModelElement(otherSection1Id);
 		final LeafSection anotherSection2 = (LeafSection) project2.getModelElement(anotherSection2Id);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(otherSection1);
 				actor2.setLeafSection(anotherSection2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// no index conflict
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		assertEquals(conflicts.size(), 0);
 
 		// a hard conflict, though. serialization matters
 		Set<AbstractOperation> hardConflicts = cd.getConflicting(ops1, ops2);
 		assertEquals(cd.getConflicting(ops1, ops2).size(), cd.getConflicting(ops2, ops1).size());
 
 		assertEquals(hardConflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictRemoveIndirectlyRemoveIndirectlySameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				actor.setLeafSection(section);
 
 				clearOperations();
 
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection otherSection1 = (LeafSection) getProject().getModelElement(otherSectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				otherSection1.getModelElements().add(actor1);
 				otherSection2.getModelElements().add(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// index conflict expected, implicitly actor gets a new parent in each copy
 		// since that op has no index
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetRemoveRemoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				actor.setLeafSection(section);
 
 				clearOperations();
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection otherSection1 = (LeafSection) getProject().getModelElement(otherSectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(otherSection1);
 				otherSection2.getModelElements().remove(actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// no index conflict
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddMoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				section.getModelElements().add(dummy);
 				getProject().addModelElement(actor);
 				clearOperations();
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				section2.getModelElements().add(actor2);
 				section2.getModelElements().move(0, actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// no index conflict
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		// index conflict arises: if the add happens before the move, the move will work
 		// if it does after the move, the move could be ineffective
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetAddMoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				section.getModelElements().add(dummy);
 				clearOperations();
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				section2.getModelElements().add(actor2);
 				section2.getModelElements().move(0, actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// no index conflict
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		// index conflict arises: if the add happens before the move, the move will work
 		// if it does after the move, the move could be ineffective
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictRemoveMoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(actor);
 				clearOperations();
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().remove(actor1);
 				section2.getModelElements().move(0, actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// no index conflict
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		// no index conflict arises: the element is gone in any serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetRemoveMoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(actor);
 				clearOperations();
 			}
 		}.run(false);
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId otherSection1Id = getProject().getModelElementId(otherSection);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection otherSection1 = (LeafSection) getProject().getModelElement(otherSection1Id);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(otherSection1);
 				section2.getModelElements().move(0, actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// no index conflict
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		// no index conflict arises: if the section change happens before the move, the move will work
 		// if it does after the move, the move could be ineffective. In either case the item is gone.
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictRemoveIndirectlyMoveSameObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(actor);
 				clearOperations();
 			}
 		}.run(false);
 
 		// start from here!
 
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId otherSection1Id = getProject().getModelElementId(otherSection);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection otherSection1 = (LeafSection) getProject().getModelElement(otherSection1Id);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				otherSection1.getModelElements().add(actor1);
 				section2.getModelElements().move(0, actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// no index conflict
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		// no index conflict arises: if the section change happens before the move, the move will work
 		// if it does after the move, the move could be ineffective. In either case the change is gone.
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictMoveMoveSameObjectDifferentIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy1 = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy2 = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				section.getModelElements().add(dummy1);
 				section.getModelElements().add(dummy2);
 
 				section.getModelElements().add(actor);
 				assertEquals(section.getModelElements().get(2), actor);
 				clearOperations();
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().move(1, actor1);
 				section2.getModelElements().move(0, actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// no index conflict
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		// an index conflict arises: result depends on which move comes last
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictMoveMoveSameObjectSameIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy1 = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy2 = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 
 				section.getModelElements().add(dummy1);
 				section.getModelElements().add(dummy2);
 
 				section.getModelElements().add(actor);
 				assertEquals(section.getModelElements().get(2), actor);
 				clearOperations();
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().move(1, actor1);
 				section2.getModelElements().move(1, actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// no index conflict
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 
 		// no index conflict arises: operations are identical
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddRemoveDifferentObject() {
 
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(useCase);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				useCase.getParticipatingActors().add(anotherDummy);
 				useCase.getParticipatingActors().add(otherDummy);
 				useCase.getParticipatingActors().add(dummy);
 				clearOperations();
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId otherDummyId = getProject().getModelElementId(otherDummy);
 		ModelElementId useCaseId = getProject().getModelElementId(useCase);
 
 		final UseCase useCase1 = (UseCase) getProject().getModelElement(useCaseId);
 		final UseCase useCase2 = (UseCase) project2.getModelElement(useCaseId);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor otherDummy2 = (Actor) project2.getModelElement(otherDummyId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				useCase1.getParticipatingActors().add(2, actor1);
 				useCase2.getParticipatingActors().remove(otherDummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// an index-integrity conflict (the remove index:1 is smaller than the add index:2, thus the added item
 		// ends up somewhere else, depending on serialization)
 		assertEquals(1, conflicts.size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictAddRemoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId dummy2Id = project2.getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummy2Id);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(0, actor1);
 				section2.getModelElements().remove(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict (the change happens at the boundary)
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddParentSetRemoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId otherSection2Id = project2.getModelElementId(otherSection);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSection2Id);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(1, actor1);
 				dummy2.setLeafSection(otherSection2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictAddParentSetRemoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId section1Id = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(section1Id);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(1, actor1);
 				dummy2.setLeafSection(otherSection2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict (the change happens at the boundary)
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictParentSetAddRemoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId dummy2Id = project2.getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummy2Id);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				section2.getModelElements().remove(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict (outcome does not depend on serialization)
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictParentSetAddRemoveIndirectlyDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId section1Id = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(section1Id);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				otherSection2.getModelElements().add(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict (outcome does not depend on serialization)
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictParentSetAddParentSetRemoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				dummy2.setLeafSection(otherSection2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict (outcome does not depend on serialization)
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void nnoConflictAddParentSetRemoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(otherSection);
 
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId section1Id = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(section1Id);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				dummy2.setLeafSection(otherSection2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// potential index-integrity conflict: it is unknown where dummy was located, worst case (before actor1
 		// insertion point) anticipated
 		assertEquals(1, conflicts.size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictAddRemoveIndirectlyDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(otherSection);
 
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId section1Id = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(section1Id);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(actor1);
 				otherSection2.getModelElements().add(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// potential index-integrity conflict: it is unknown where dummy was located, worst case (before actor1
 		// insertion point) anticipated
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictRemoveRemoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(otherSection);
 
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId dummy2Id = project2.getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummy2Id);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().remove(actor1);
 				section2.getModelElements().remove(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictRemoveParentSetRemoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(otherSection);
 
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId section1Id = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(section1Id);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().remove(actor1);
 				otherSection2.getModelElements().add(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictRemoveRemoveIndirectlyDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(otherSection);
 
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId section1Id = getProject().getModelElementId(section);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(section1Id);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().remove(actor1);
 				otherSection2.getModelElements().add(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictParentSetRemoveRemoveIndirectlyDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection anotherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(anotherSection);
 
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 		ModelElementId anotherSection1Id = getProject().getModelElementId(anotherSection);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection anotherSection1 = (LeafSection) getProject().getModelElement(anotherSection1Id);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(anotherSection1);
 				otherSection2.getModelElements().add(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictParentSetRemoveParentSetRemoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection anotherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(anotherSection);
 
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 		ModelElementId anotherSectionId = getProject().getModelElementId(anotherSection);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection anotherSection1 = (LeafSection) getProject().getModelElement(anotherSectionId);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(anotherSection1);
 				dummy2.setLeafSection(otherSection2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictRemoveIndirectlyRemoveIndirectlyDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection anotherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(anotherSection);
 
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 		ModelElementId anotherSection1Id = getProject().getModelElementId(anotherSection);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor dummy2 = (Actor) project2.getModelElement(dummyId);
 
 		final LeafSection anotherSection1 = (LeafSection) getProject().getModelElement(anotherSection1Id);
 		final LeafSection otherSection2 = (LeafSection) project2.getModelElement(otherSectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 
 				anotherSection1.getModelElements().add(actor1);
 				otherSection2.getModelElements().add(dummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictAddMoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(anotherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(1, actor1);
 				section2.getModelElements().move(1, anotherDummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// index-integrity conflict: result dependent on serialization
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictAddMoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(anotherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().add(0, actor1);
 				section2.getModelElements().move(1, anotherDummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetAddMoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(anotherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(section1);
 				section2.getModelElements().move(1, anotherDummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// potential index-integrity conflict: if the move went to the index where actor1 ends up in
 		// there would be a problem. The index is unknown, since the single ref op does not save it.
 		// Since relation of these indices cannot be found prior to looking at the feature, a conflict is assumed
 		assertEquals(conflicts.size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetAddMoveDifferentObjectBoundary() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				// section.getModelElements().add(dummy);
 				section.getModelElements().add(actor);
 				section.getModelElements().add(otherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 		ModelElementId dummy1Id = getProject().getModelElementId(dummy);
 
 		final Actor dummy1 = (Actor) getProject().getModelElement(dummy1Id);
 		final Actor actor2 = (Actor) project2.getModelElement(actorId);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				dummy1.setLeafSection(section1);
 				anotherDummy2.setLeafSection(section2);
 				section2.getModelElements().move(section2.getModelElements().size() - 1, actor2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		// two reasons to conflict: 1. two competing adds by parent, 2. add and move might (actually do) affect the same
 		// index
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), 2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops2, ops1).size(), 1);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictRemoveMoveDifferentObject() {
 
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(useCase);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				useCase.getParticipatingActors().add(actor);
 				useCase.getParticipatingActors().add(dummy);
 				useCase.getParticipatingActors().add(otherDummy);
 				useCase.getParticipatingActors().add(anotherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actorId = getProject().getModelElementId(actor);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 		ModelElementId useCaseId = getProject().getModelElementId(useCase);
 
 		final UseCase useCase1 = (UseCase) getProject().getModelElement(useCaseId);
 		final UseCase useCase2 = (UseCase) project2.getModelElement(useCaseId);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actorId);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				useCase1.getParticipatingActors().remove(actor1);
 				useCase2.getParticipatingActors().move(1, anotherDummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// index-integrity conflict: result dependent on serialization
 		assertEquals(1, conflicts.size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictRemoveMoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(anotherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().remove(actor1);
 				section2.getModelElements().move(0, anotherDummy2);
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictRemoveMoveDifferentObjectSameIndex() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(anotherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 
 		final Actor dummy1 = (Actor) getProject().getModelElement(dummyId);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().remove(dummy1);
 				section2.getModelElements().move(1, anotherDummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictParentSetRemoveMoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(anotherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 		ModelElementId dummyId = getProject().getModelElementId(dummy);
 		ModelElementId otherSectionId = getProject().getModelElementId(otherSection);
 
 		final Actor dummy1 = (Actor) getProject().getModelElement(dummyId);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection otherSection1 = (LeafSection) getProject().getModelElement(otherSectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				dummy1.setLeafSection(otherSection1);
 				section2.getModelElements().move(1, anotherDummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictParentSetRemoveMoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 				getProject().addModelElement(otherSection);
 
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(anotherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 		ModelElementId otherSection1Id = getProject().getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection otherSection1 = (LeafSection) getProject().getModelElement(otherSection1Id);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				actor1.setLeafSection(otherSection1);
 				section2.getModelElements().move(1, anotherDummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// index-integrity conflict: result dependent of serialization (anotherDummy could end up on 1 or on 0)
 		assertEquals(1, conflicts.size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictRemoveIndirectlyMoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(anotherDummy);
 
 				clearOperations();
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 		ModelElementId dummy1Id = getProject().getModelElementId(dummy);
 		ModelElementId otherSection1Id = getProject().getModelElementId(otherSection);
 
 		final Actor dummy1 = (Actor) getProject().getModelElement(dummy1Id);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection otherSection1 = (LeafSection) getProject().getModelElement(otherSection1Id);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				otherSection1.getModelElements().add(dummy1);
 				section2.getModelElements().move(1, anotherDummy2);
 
 			}
 		}.run(false);
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(conflicts.size(), 0);
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictRemoveIndirectlyMoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(anotherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 		ModelElementId otherSection1Id = getProject().getModelElementId(otherSection);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection otherSection1 = (LeafSection) getProject().getModelElement(otherSection1Id);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				otherSection1.getModelElements().add(actor1);
 				section2.getModelElements().move(1, anotherDummy2);
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// index-integrity conflict: result dependent on serialization
 		assertEquals(1, conflicts.size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void conflictMoveMoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(anotherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().move(1, actor1);
 				section2.getModelElements().move(1, anotherDummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// index-integrity conflict: result dependent on serialization
 		assertEquals(1, conflicts.size());
 
 	}
 
 	/**
 	 * Tests if manipulating multi-features is detected as a conflict.
 	 */
 	@Test
 	public void noConflictMoveMoveDifferentObject() {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection otherSection = DocumentFactory.eINSTANCE.createLeafSection();
 
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Actor dummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherDummy = RequirementFactory.eINSTANCE.createActor();
 		final Actor anotherDummy = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				getProject().addModelElement(otherSection);
 				getProject().addModelElement(actor);
 				getProject().addModelElement(dummy);
 				getProject().addModelElement(otherDummy);
 				getProject().addModelElement(anotherDummy);
 
 				section.getModelElements().add(actor);
 				section.getModelElements().add(dummy);
 				section.getModelElements().add(otherDummy);
 				section.getModelElements().add(anotherDummy);
 
 				clearOperations();
 
 			}
 		}.run(false);
 		ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		Project project2 = ps2.getProject();
 
 		ModelElementId actor1Id = getProject().getModelElementId(actor);
 		ModelElementId sectionId = getProject().getModelElementId(section);
 		ModelElementId anotherDummyId = getProject().getModelElementId(anotherDummy);
 
 		final Actor actor1 = (Actor) getProject().getModelElement(actor1Id);
 		final Actor anotherDummy2 = (Actor) project2.getModelElement(anotherDummyId);
 
 		final LeafSection section1 = (LeafSection) getProject().getModelElement(sectionId);
 		final LeafSection section2 = (LeafSection) project2.getModelElement(sectionId);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				section1.getModelElements().move(2, actor1);
 				section2.getModelElements().move(0, anotherDummy2);
 
 			}
 		}.run(false);
 
 		List<AbstractOperation> ops1 = getProjectSpace().getOperations();
 		List<AbstractOperation> ops2 = ps2.getOperations();
 
 		ConflictDetector cd = new ConflictDetector(getConflictDetectionStrategy());
 		Set<AbstractOperation> conflicts = cd.getConflictingIndexIntegrity(ops1, ops2);
 		assertEquals(cd.getConflictingIndexIntegrity(ops1, ops2).size(), cd.getConflictingIndexIntegrity(ops2, ops1)
 			.size());
 		// no index-integrity conflict: result independent of serialization
 		assertEquals(0, conflicts.size());
 
 	}
 
 }
