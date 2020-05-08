 /*******************************************************************************
  * Copyright (c) 2011-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Edgar Mueller - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.recording.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.emfstore.client.ESWorkspaceProvider;
 import org.eclipse.emf.emfstore.client.callbacks.ESUpdateCallback;
 import org.eclipse.emf.emfstore.client.test.common.cases.ESTest;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
 import org.eclipse.emf.emfstore.internal.client.model.controller.UpdateController;
 import org.eclipse.emf.emfstore.internal.client.model.impl.ProjectSpaceBase;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESWorkspaceImpl;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AttributeOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.OperationsFactory;
 import org.junit.Test;
 
 /**
  * @author emueller
  * 
  */
 public class DuplicateOperationsTest extends ESTest {
 
 	private static final String A = "A"; //$NON-NLS-1$
 	private static final String B = "B"; //$NON-NLS-1$
 	private static final String C = "C"; //$NON-NLS-1$
 
 	private UpdateController createDummyUpdateController() {
 		final ProjectSpaceBase p = (ProjectSpaceBase) getProjectSpace();
 		final Usersession u = org.eclipse.emf.emfstore.internal.client.model.ModelFactory.eINSTANCE.createUsersession();
 		final ESWorkspaceImpl workspace = (ESWorkspaceImpl) ESWorkspaceProvider.INSTANCE.getWorkspace();
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				workspace.toInternalAPI().getUsersessions().add(u);
 				p.setUsersession(u);
 				return null;
 			}
 		});
 		return new UpdateController(p, null,
 			ESUpdateCallback.NOCALLBACK, new NullProgressMonitor());
 	}
 
 	private static AbstractOperation createOperation(String identifer) {
 		final AttributeOperation dummyOp = OperationsFactory.eINSTANCE.createAttributeOperation();
 		dummyOp.setIdentifier(identifer);
 		return dummyOp;
 	}
 
 	private static ChangePackage createChangePackage(AbstractOperation... ops) {
 		final ChangePackage changePackage = VersioningFactory.eINSTANCE
 			.createChangePackage();
 		changePackage.getOperations().addAll(Arrays.asList(ops));
 		return changePackage;
 	}
 
 	@Test
 	public void testConflictingChangePackage() {
 		final UpdateController updateController = createDummyUpdateController();
 		final AbstractOperation a = createOperation(A);
 		final AbstractOperation b = createOperation(B);
 		final ChangePackage cp = createChangePackage(a, b);
 		final ChangePackage localCP = createChangePackage(
 			ModelUtil.clone(a), ModelUtil.clone(b));
 		final boolean hasBeenRemoved = updateController.removeDuplicateOperations(cp, localCP);
 		assertEquals(0, localCP.getOperations().size());
 		assertTrue(hasBeenRemoved);
 	}
 
 	@Test
 	public void testConflictingChangePackageWithMoreOpsThanLocal() {
 		final UpdateController updateController = createDummyUpdateController();
 		final AbstractOperation a = createOperation(A);
 		final AbstractOperation b = createOperation(B);
 		final AbstractOperation c = createOperation(C);
 		final ChangePackage cp = createChangePackage(a, b);
 		final ChangePackage localCP = createChangePackage(
 			ModelUtil.clone(a), ModelUtil.clone(b), c);
 		final boolean hasBeenRemoved = updateController.removeDuplicateOperations(cp, localCP);
 		assertEquals(1, localCP.getOperations().size());
 		assertTrue(hasBeenRemoved);
 	}
 
 	@Test
 	public void testNonConflictingChangePackage() {
 		final UpdateController updateController = createDummyUpdateController();
 		final AbstractOperation a = createOperation(A);
 		final AbstractOperation b = createOperation(B);
 		final AbstractOperation c = createOperation(C);
 		final ChangePackage cp = createChangePackage(a, b);
 		final ChangePackage localCP = createChangePackage(c);
 		final boolean hasBeenRemoved = updateController.removeDuplicateOperations(cp, localCP);
 		assertFalse(hasBeenRemoved);
 	}
 
 	@Test
 	public void testNoLocalChanges() {
 		final UpdateController updateController = createDummyUpdateController();
 		final AbstractOperation a = createOperation(A);
 		final AbstractOperation b = createOperation(B);
 		final ChangePackage cp = createChangePackage(a, b);
 		final ChangePackage localCP = createChangePackage();
 		final boolean hasBeenRemoved = updateController.removeDuplicateOperations(cp, localCP);
 		assertFalse(hasBeenRemoved);
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testIllegalIncomingChangePackage() {
 		final UpdateController updateController = createDummyUpdateController();
 		final AbstractOperation a = createOperation(A);
 		final AbstractOperation b = createOperation(B);
 		final AbstractOperation c = createOperation(C);
 		final ChangePackage cp = createChangePackage(a, b, c);
 		final ChangePackage localCP = createChangePackage(
 			ModelUtil.clone(a), ModelUtil.clone(b));
 		updateController.removeDuplicateOperations(cp, localCP);
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testIllegalLocalChangePackage() {
 		final UpdateController updateController = createDummyUpdateController();
 		final AbstractOperation a = createOperation(A);
 		final AbstractOperation b = createOperation(B);
 		final AbstractOperation c = createOperation(C);
 		final ChangePackage cp = createChangePackage(a, b);
 		final ChangePackage localCP = createChangePackage(
 			ModelUtil.clone(a), c);
 		updateController.removeDuplicateOperations(cp, localCP);
 	}
 
 	@Test
 	public void testRemoveChangePackage() {
 		final UpdateController updateController = createDummyUpdateController();
 		final AbstractOperation a = createOperation(A);
 		final AbstractOperation b = createOperation(B);
 		final ChangePackage cp = createChangePackage(a, b);
 		final ChangePackage localCP = createChangePackage(
 			ModelUtil.clone(a), ModelUtil.clone(b));
 		final List<ChangePackage> incoming = new ArrayList<ChangePackage>();
 		incoming.add(cp);
 		final int delta = updateController.removeFromChangePackages(incoming, localCP);
 		assertEquals(1, delta);
 		assertEquals(0, localCP.getOperations().size());
 		assertEquals(0, incoming.size());
 	}
 
 	@Test
 	public void testRemoveChangePackageWithMoreIncomingChanges() {
 		final UpdateController updateController = createDummyUpdateController();
 		final AbstractOperation a = createOperation(A);
 		final AbstractOperation b = createOperation(B);
 		final AbstractOperation c = createOperation(C);
 		final ChangePackage cp = createChangePackage(a, b);
 		final ChangePackage cp2 = createChangePackage(c);
 		final ChangePackage localCP = createChangePackage(
 			ModelUtil.clone(a), ModelUtil.clone(b));
 		final List<ChangePackage> incoming = new ArrayList<ChangePackage>();
 		incoming.add(cp);
 		incoming.add(cp2);
 		final int delta = updateController.removeFromChangePackages(incoming, localCP);
 		assertEquals(1, delta);
 		assertEquals(0, localCP.getOperations().size());
 		assertEquals(1, incoming.size());
 	}
 
 	// @Test
 	// public void testConflictingChangePackageWithLocalChanges() {
 	// final UpdateController updateController = createDummyUpdateController();
 	// updateController.removeDuplicateOperations(incomingChanges, localChanges);
 	// }
 }
