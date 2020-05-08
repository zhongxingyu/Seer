 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  */
 package org.eclipse.emf.emfstore.client.test.conflictDetection;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.client.test.model.document.DocumentFactory;
 import org.eclipse.emf.emfstore.client.test.model.document.LeafSection;
 import org.eclipse.emf.emfstore.client.test.model.requirement.Actor;
 import org.eclipse.emf.emfstore.client.test.model.requirement.RequirementFactory;
 import org.eclipse.emf.emfstore.client.test.model.task.ActionItem;
 import org.eclipse.emf.emfstore.client.test.model.task.TaskFactory;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.server.conflictDetection.ConflictDetector;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.CompositeOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.CreateDeleteOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.MultiReferenceOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.SingleReferenceOperation;
 import org.junit.Test;
 
 /**
  * Test conflict detection behaviour during a selective commit.
  * 
  * @author mck
  */
 public class ConflictDetectionSelectiveCommitSpecialCases extends ConflictDetectionTest {
 
 	/**
 	 * Test the special case.
 	 */
 	@Test
 	public void conflictSpecialCase() {
 
 		final LeafSection leafSection1 = DocumentFactory.eINSTANCE.createLeafSection();
 		final LeafSection leafSection2 = DocumentFactory.eINSTANCE.createLeafSection();
 		final ActionItem ai = TaskFactory.eINSTANCE.createActionItem();
 		final Actor actor = RequirementFactory.eINSTANCE.createActor();
 		final Project project1 = getProject();
 		final ProjectSpace ps1 = getProjectSpace();
 
 		// 1. init first project and simulate commit
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				project1.addModelElement(leafSection1);
 				project1.addModelElement(leafSection2);
 				project1.addModelElement(actor);
 
 				ps1.getOperations().clear();
 
 			}
 
 		}.run(false);
 
 		// 2. simulate checkout
 		final ProjectSpace ps2 = cloneProjectSpace(getProjectSpace());
 		final Project project2 = ps2.getProject();
 
 		// 3. simulate create actionItem and a selective commit of the create operation
 		final List<AbstractOperation> commitedOperations = new ArrayList<AbstractOperation>();
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				leafSection1.getModelElements().add(ai);
 
 				assertTrue("Create actionItem failed", ps1.getLocalOperations().getOperations().size() == 2);
 				assertTrue("Create actionItem has wrong operations",
 					ps1.getLocalOperations().getOperations().get(0) instanceof CreateDeleteOperation
 						|| ps1.getLocalOperations().getOperations().get(0) instanceof MultiReferenceOperation);
 				assertTrue("Create actionItem has wrong operations",
 					ps1.getLocalOperations().getOperations().get(1) instanceof CreateDeleteOperation
 						|| ps1.getLocalOperations().getOperations().get(1) instanceof MultiReferenceOperation);
 
 				for (AbstractOperation op : ps1.getOperations()) {
 					if (op instanceof CreateDeleteOperation) {
 						commitedOperations.add(op);
 						ps1.getOperations().remove(op);
 					}
 				}
 				assertTrue("Commit actionItem failed", ps1.getLocalOperations().getOperations().size() == 1);
 				assertTrue("Commit actionItem has wrong operation",
 					ps1.getLocalOperations().getOperations().get(0) instanceof MultiReferenceOperation);
 			}
 
 		}.run(false);
 
 		assertTrue("Commit commitedOperations.size() is wrong", commitedOperations.size() == 1);
 		assertTrue("Commited operation is wrong", commitedOperations.get(0) instanceof CreateDeleteOperation);
 
 		// 4. simulate update of project 2
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 
 				((ProjectSpaceImpl) ps2).applyOperations(commitedOperations, false);
 
 			}
 
 		}.run(false);
 
 		// 5. move actionItem in project 2 from "orphans" to section2
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				assertTrue("Not allowed operations in ps2", ps2.getLocalOperations().getOperations().isEmpty());
				ModelElementId leafSection2Id = project2.getModelElementId(leafSection2);
				ModelElementId aiId = project2.getModelElementId(ai);
 				LeafSection tmpLSection2 = (LeafSection) project2.getModelElement(leafSection2Id);
 				ActionItem tmpAi = (ActionItem) project2.getModelElement(aiId);
 				tmpLSection2.getModelElements().add(tmpAi);
 
 				assertTrue("Wrong number of operations in ps2 afer moving actionItem", ps2.getLocalOperations()
 					.getOperations().size() == 1);
 				assertTrue("Move actionItem has wrong operation",
 					ps2.getLocalOperations().getOperations().get(0) instanceof CompositeOperation);
 				CompositeOperation compOp = (CompositeOperation) ps2.getLocalOperations().getOperations().get(0);
 
 				assertTrue("CompositeOperation contains wrong operations",
 					compOp.getSubOperations().get(0) instanceof MultiReferenceOperation
 						|| compOp.getSubOperations().get(0) instanceof SingleReferenceOperation);
 				assertTrue("CompositeOperation contains wrong operations",
 					compOp.getSubOperations().get(1) instanceof MultiReferenceOperation
 						|| compOp.getSubOperations().get(1) instanceof SingleReferenceOperation);
 
 			}
 
 		}.run(false);
 
 		// 6. commit the multireferenceoperation in ps1
 		// nothing to do here
 
 		// 7. update ps2 -> conflictdetector
 		List<AbstractOperation> endOps1 = ps1.getLocalOperations().getOperations();
 		assertTrue("endOps1.size() != 1", endOps1.size() == 1);
 		assertTrue("endOps1.get(0) !instanceof MultiRefOp", endOps1.get(0) instanceof MultiReferenceOperation);
 
 		List<AbstractOperation> endOps2 = ps2.getLocalOperations().getOperations();
 		assertTrue("endOps2.size() != 1", endOps2.size() == 1);
 		assertTrue("endOps2.get(0) !instanceof CompositeOp", endOps2.get(0) instanceof CompositeOperation);
 
 		CompositeOperation compOp = (CompositeOperation) endOps2.get(0);
 
 		assertTrue("CompositeOperation contains wrong operations",
 			compOp.getSubOperations().get(0) instanceof MultiReferenceOperation
 				|| compOp.getSubOperations().get(0) instanceof SingleReferenceOperation);
 
 		assertTrue("CompositeOperation contains wrong operations",
 			compOp.getSubOperations().get(1) instanceof MultiReferenceOperation
 				|| compOp.getSubOperations().get(1) instanceof SingleReferenceOperation);
 
 		ConflictDetector detec = new ConflictDetector(getConflictDetectionStrategy());
 
 		assertEquals("Different number of conflicts is not allowed", detec.getConflicting(endOps1, endOps2).size(),
 			detec.getConflicting(endOps2, endOps1).size());
 		Set<AbstractOperation> conflicting = detec.getConflicting(endOps1, endOps2);
 		assertFalse("There has to be a conflict in conflicting-Set", conflicting.size() == 0);
 
 		boolean neverConflicted = true;
 		for (AbstractOperation op1 : endOps1) {
 			for (AbstractOperation op2 : endOps2) {
 				if (detec.doConflict(op1, op2)) {
 					neverConflicted = false;
 				}
 			}
 		}
 
 		assertFalse("No conflict after Conflicttest - doConflict", neverConflicted);
 
 	}
 }
