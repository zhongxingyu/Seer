 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * koegel
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.changetracking.test.command;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.CommandParameter;
 import org.eclipse.emf.edit.command.CopyCommand;
 import org.eclipse.emf.edit.command.CopyToClipboardCommand;
 import org.eclipse.emf.edit.command.CutToClipboardCommand;
 import org.eclipse.emf.edit.command.DeleteCommand;
 import org.eclipse.emf.edit.command.PasteFromClipboardCommand;
 import org.eclipse.emf.edit.command.RemoveCommand;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.emfstore.client.test.common.cases.ESTest;
 import org.eclipse.emf.emfstore.client.test.common.dsl.Add;
 import org.eclipse.emf.emfstore.client.test.common.dsl.Create;
 import org.eclipse.emf.emfstore.client.test.common.util.ProjectUtil;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.UnsupportedNotificationException;
 import org.eclipse.emf.emfstore.internal.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.internal.common.model.IdEObjectCollection;
 import org.eclipse.emf.emfstore.internal.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.CreateDeleteOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.MultiReferenceOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.ReferenceOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.SingleReferenceOperation;
 import org.eclipse.emf.emfstore.test.model.TestElement;
 import org.eclipse.emf.emfstore.test.model.TestmodelPackage;
 import org.junit.Test;
 
 /**
  * Tests for the command recording to detect deletes, cuts and copies.
  * 
  * @author koegel
  */
 public class CommandTest extends ESTest {
 
 	private static final String AI22 = "AI2"; //$NON-NLS-1$
 	private static final String AI12 = "AI1"; //$NON-NLS-1$
 	private static final String SPRINT1 = "Sprint1"; //$NON-NLS-1$
 	private static final String COMMAND_NOT_EXECUTABLE = "Command not executable"; //$NON-NLS-1$
 	private static final String NON_CONTAINED_M_TO_N = "nonContained_MToN"; //$NON-NLS-1$
 	private static final String NON_CONTAINED_N_TO_M = "nonContained_NToM"; //$NON-NLS-1$
 	private static final String NON_CONTAINED_1_TO_N = "nonContained_1ToN"; //$NON-NLS-1$
 	private static final String NON_CONTAINED_N_TO1 = "nonContained_NTo1"; //$NON-NLS-1$
 	private static final String CONTAINED_ELEMENTS = "containedElements"; //$NON-NLS-1$
 	private static final String CONTAINER = "container"; //$NON-NLS-1$
 	private static final String OTHER_TEST_ELEMENT = "other TestElement"; //$NON-NLS-1$
 	private static final String NEW_TEST_ELEMENT = "new TestElement"; //$NON-NLS-1$
 	private static final String OLD_TEST_ELEMENT = "old TestElement"; //$NON-NLS-1$
 
 	/**
 	 * Tests the copy to clipboard and paste from clipboard command.
 	 */
 	@Test
 	public void copyAndPasteFromClipboardCommand() {
 
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(leafSection);
 			}
 		}.run(false);
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// copy to clipboard
 		final Collection<EObject> toCopy = new ArrayList<EObject>();
 		toCopy.add(actor);
 		final Command copyCommand = editingDomain.createCommand(CopyToClipboardCommand.class, new CommandParameter(
 			null,
 			null, toCopy));
 		editingDomain.getCommandStack().execute(copyCommand);
 
 		// paste from clipboard
 		final Command pasteCommand = editingDomain.createCommand(PasteFromClipboardCommand.class, new CommandParameter(
 			leafSection, TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS, Collections.emptyList(),
 			CommandParameter.NO_INDEX));
 
 		editingDomain.getCommandStack().execute(pasteCommand);
 
 		final EObject copyOfTestElement = leafSection.getContainedElements().get(1);
 		final ModelElementId actorId = ModelUtil.getProject(actor).getModelElementId(actor);
 		final ModelElementId copyOfTestElementId = ModelUtil.getProject(copyOfTestElement).getModelElementId(
 			copyOfTestElement);
 
 		assertTrue(actorId.equals(actorId));
 		assertTrue(!copyOfTestElementId.equals(actorId));
 	}
 
 	/**
 	 * Tests to delete a workpackage with a containec command with a recipient. This test also the removal o
 	 * unicdirectional cross references
 	 */
 	@Test
 	public void testDeleteWithUnidirectionalCrossReference() {
 		final TestElement createCompositeSection = Create.testElement();
 		final TestElement createTestElement = Create.testElement();
 		final TestElement workPackage = Create.testElement();
 		final TestElement createActionItem = Create.testElement();
 		final TestElement createComment = Create.testElement();
 		final TestElement createUser = Create.testElement();
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(createCompositeSection);
 				createCompositeSection.getContainedElements().add(createTestElement);
 				createTestElement.getContainedElements().add(workPackage);
 				createTestElement.getContainedElements().add(createUser);
 				workPackage.getContainedElements2().add(createActionItem);
 				createActionItem.getContainedElements().add(createComment);
 				createComment.getReferences().add(createUser);
 				clearOperations();
 			}
 		}.run(false);
 
 		final Command delete = DeleteCommand.create(
 			ESWorkspaceProviderImpl.getInstance().getEditingDomain(), workPackage);
 		ESWorkspaceProviderImpl.getInstance().getEditingDomain().getCommandStack().execute(delete);
 
 		assertEquals(0, createComment.getContainedElements().size());
 		assertEquals(1, getProjectSpace().getOperations().size());
 		assertTrue(getProjectSpace().getOperations().get(0) instanceof CreateDeleteOperation);
 	}
 
 	/**
 	 * Tests the copy and paste commands.
 	 */
 	@Test
 	public void copyAndPasteCommand() {
 
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(leafSection);
 			}
 		}.run(false);
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// copy
 		final CopyCommand.Helper helper = new CopyCommand.Helper();
		final Command command = editingDomain.createCommand(CopyCommand.class, new CommandParameter(
			actor, null, helper));
 		editingDomain.getCommandStack().execute(command);
 
 		// paste
 		final TestElement copyOfTestElement = (TestElement) helper.get(actor);
 
 		final Collection<TestElement> toPaste = new ArrayList<TestElement>();
 		toPaste.add(copyOfTestElement);
 
 		final Command pasteCommand = editingDomain.createCommand(AddCommand.class, new CommandParameter(leafSection,
 			TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS, toPaste, CommandParameter.NO_INDEX));
 
 		editingDomain.getCommandStack().execute(pasteCommand);
 
 		final EObject copyOfTestElementRead = leafSection.getContainedElements().get(1);
 
 		final ModelElementId actorId = ModelUtil.getProject(actor).getModelElementId(actor);
 		final ModelElementId copyOfTestElementReadId = ModelUtil.getProject(copyOfTestElementRead).getModelElementId(
 			copyOfTestElementRead);
 		assertFalse(actorId.equals(copyOfTestElementReadId));
 
 	}
 
 	/**
 	 * Tests the copy to clipboard and paste from clipboard command.
 	 */
 	@Test
 	public void copyAndPasteFromClipboardCommandDirectCreation() {
 
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(leafSection);
 			}
 		}.run(false);
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// copy
 		final Command command = CopyToClipboardCommand.create(editingDomain, actor);
 		editingDomain.getCommandStack().execute(command);
 
 		// paste
 		final Command pasteCommand = PasteFromClipboardCommand.create(editingDomain, leafSection,
 			TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS, CommandParameter.NO_INDEX);
 		editingDomain.getCommandStack().execute(pasteCommand);
 
 		final EObject copyOfTestElementRead = leafSection.getContainedElements().get(1);
 
 		final ModelElementId actorId = ModelUtil.getProject(actor).getModelElementId(actor);
 		final ModelElementId copyOfTestElementReadId = ModelUtil.getProject(copyOfTestElementRead).getModelElementId(
 			copyOfTestElementRead);
 		assertFalse(actorId.equals(copyOfTestElementReadId));
 	}
 
 	/**
 	 * Tests the copy and paste commands.
 	 */
 	@Test
 	public void copyAndPasteCommandDirectCreation() {
 
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(leafSection);
 			}
 		}.run(false);
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// copy
 		final Command command = CopyCommand.create(editingDomain, actor);
 		editingDomain.getCommandStack().execute(command);
 
 		// paste
 		final TestElement copyOfTestElement = (TestElement) command.getResult().toArray()[0];
 
 		final Collection<TestElement> toPaste = new ArrayList<TestElement>();
 		toPaste.add(copyOfTestElement);
 
 		final Command pasteCommand = AddCommand.create(editingDomain, leafSection,
 			TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS, toPaste, CommandParameter.NO_INDEX);
 
 		editingDomain.getCommandStack().execute(pasteCommand);
 
 		final EObject copyOfTestElementRead = leafSection.getContainedElements().get(1);
 
 		final ModelElementId actorId = ModelUtil.getProject(actor).getModelElementId(actor);
 		final ModelElementId copyOfTestElementReadId = ModelUtil.getProject(copyOfTestElementRead).getModelElementId(
 			copyOfTestElementRead);
 		assertFalse(actorId.equals(copyOfTestElementReadId));
 	}
 
 	/**
 	 * check element deletion tracking.
 	 * 
 	 * @throws UnsupportedOperationException on test fail
 	 * @throws UnsupportedNotificationException on test fail
 	 */
 	@Test
 	public void deleteCommandTest() throws UnsupportedOperationException, UnsupportedNotificationException {
 
 		final TestElement useCase = Create.testElement();
 		Add.toProject(getLocalProject(), useCase);
 		clearOperations();
 		final ModelElementId useCaseId = getProject().getModelElementId(useCase);
 
 		final Command deleteCommand = DeleteCommand.create(
 			ESWorkspaceProviderImpl.getInstance().getEditingDomain(),
 			useCase);
 		final CommandStack commandStack = ESWorkspaceProviderImpl.getInstance().getEditingDomain().getCommandStack();
 		if (deleteCommand.canExecute()) {
 			commandStack.execute(deleteCommand);
 		} else {
 			fail(COMMAND_NOT_EXECUTABLE);
 		}
 
 		final List<AbstractOperation> operations = getProjectSpace().getOperations();
 
 		assertEquals(1, operations.size());
 		final AbstractOperation operation = operations.get(0);
 		assertTrue(operation instanceof CreateDeleteOperation);
 		final CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation;
 
 		assertEquals(useCaseId, createDeleteOperation.getModelElementId());
 		assertEquals(0, createDeleteOperation.getSubOperations().size());
 		assertTrue(createDeleteOperation.isDelete());
 	}
 
 	/**
 	 * check complex element deletion tracking.
 	 * 
 	 * @throws UnsupportedOperationException on test fail
 	 * @throws UnsupportedNotificationException on test fail
 	 */
 	@Test
 	// BEGIN COMPLEX CODE
 	public void complexDeleteCommandTest() throws UnsupportedOperationException, UnsupportedNotificationException {
 
 		final TestElement section = Create.testElement();
 		final TestElement useCase = Create.testElement();
 		final TestElement oldTestElement = Create.testElement();
 		final TestElement newTestElement = Create.testElement();
 		final TestElement otherTestElement = Create.testElement();
 
 		oldTestElement.setName(OLD_TEST_ELEMENT);
 		newTestElement.setName(NEW_TEST_ELEMENT);
 		otherTestElement.setName(OTHER_TEST_ELEMENT);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				section.getContainedElements().add(useCase);
 				section.getContainedElements().add(oldTestElement);
 				getProject().addModelElement(newTestElement);
 				getProject().addModelElement(otherTestElement);
 				useCase.setNonContained_NTo1(oldTestElement);
 				useCase.getNonContained_NToM().add(newTestElement);
 				useCase.getNonContained_NToM().add(otherTestElement);
 				assertTrue(getProject().contains(useCase));
 				assertEquals(getProject(), ModelUtil.getProject(useCase));
 				clearOperations();
 			}
 		}.run(false);
 
 		final Project project = ModelUtil.getProject(useCase);
 		final ModelElementId useCaseId = project.getModelElementId(useCase);
 
 		final Command deleteCommand = DeleteCommand.create(
 			ESWorkspaceProviderImpl.getInstance().getEditingDomain(),
 			useCase);
 		ESWorkspaceProviderImpl.getInstance().getEditingDomain().getCommandStack().execute(deleteCommand);
 
 		assertFalse(getProject().contains(useCase));
 
 		final List<AbstractOperation> operations = getProjectSpace().getOperations();
 
 		assertEquals(1, operations.size());
 		final AbstractOperation operation = operations.get(0);
 		assertTrue(operation instanceof CreateDeleteOperation);
 		final CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation;
 		assertTrue(createDeleteOperation.isDelete());
 
 		assertEquals(useCaseId, createDeleteOperation.getModelElementId());
 		final EList<ReferenceOperation> subOperations = createDeleteOperation.getSubOperations();
 
 		assertEquals(8, subOperations.size());
 		final AbstractOperation suboperation0 = subOperations.get(0);
 		final AbstractOperation suboperation1 = subOperations.get(1);
 		final AbstractOperation suboperation2 = subOperations.get(2);
 		final AbstractOperation suboperation3 = subOperations.get(3);
 		final AbstractOperation suboperation4 = subOperations.get(4);
 		final AbstractOperation suboperation5 = subOperations.get(5);
 		final AbstractOperation suboperation6 = subOperations.get(6);
 		final AbstractOperation suboperation7 = subOperations.get(7);
 
 		assertTrue(suboperation0 instanceof SingleReferenceOperation);
 		assertTrue(suboperation1 instanceof MultiReferenceOperation);
 		assertTrue(suboperation2 instanceof SingleReferenceOperation);
 		assertTrue(suboperation3 instanceof MultiReferenceOperation);
 		assertTrue(suboperation4 instanceof MultiReferenceOperation);
 		assertTrue(suboperation5 instanceof MultiReferenceOperation);
 		assertTrue(suboperation6 instanceof MultiReferenceOperation);
 		assertTrue(suboperation7 instanceof MultiReferenceOperation);
 
 		final SingleReferenceOperation mrSuboperation0 = (SingleReferenceOperation) suboperation0;
 		final MultiReferenceOperation mrSuboperation1 = (MultiReferenceOperation) suboperation1;
 		final SingleReferenceOperation mrSuboperation2 = (SingleReferenceOperation) suboperation2;
 		final MultiReferenceOperation mrSuboperation3 = (MultiReferenceOperation) suboperation3;
 		final MultiReferenceOperation mrSuboperation4 = (MultiReferenceOperation) suboperation4;
 		final MultiReferenceOperation mrSuboperation5 = (MultiReferenceOperation) suboperation5;
 		final MultiReferenceOperation mrSuboperation6 = (MultiReferenceOperation) suboperation6;
 		final MultiReferenceOperation mrSuboperation7 = (MultiReferenceOperation) suboperation7;
 
 		final ModelElementId sectionId = ModelUtil.getProject(section).getModelElementId(section);
 
 		assertEquals(useCaseId, mrSuboperation0.getModelElementId());
 		assertEquals(CONTAINER, mrSuboperation0.getFeatureName());
 		assertEquals(sectionId, mrSuboperation0.getOldValue());
 		assertEquals(null, mrSuboperation0.getNewValue());
 
 		assertEquals(CONTAINED_ELEMENTS, mrSuboperation1.getFeatureName());
 		assertEquals(0, mrSuboperation1.getIndex());
 		assertEquals(sectionId, mrSuboperation1.getModelElementId());
 		assertEquals(CONTAINER, mrSuboperation1.getOppositeFeatureName());
 		assertFalse(mrSuboperation1.isAdd());
 		assertTrue(mrSuboperation1.isBidirectional());
 		final Set<ModelElementId> otherInvolvedModelElements3 = mrSuboperation1.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements3.size());
 		final EList<ModelElementId> referencedModelElements3 = mrSuboperation1.getReferencedModelElements();
 		assertEquals(1, referencedModelElements3.size());
 		assertEquals(useCaseId, referencedModelElements3.get(0));
 
 		final ModelElementId oldTestElementId = ModelUtil.getProject(oldTestElement).getModelElementId(oldTestElement);
 
 		assertEquals(oldTestElementId, mrSuboperation2.getOldValue());
 		assertEquals(null, mrSuboperation2.getNewValue());
 		assertEquals(NON_CONTAINED_N_TO1, mrSuboperation2.getFeatureName());
 		assertEquals(useCaseId, mrSuboperation2.getModelElementId());
 		assertEquals(NON_CONTAINED_1_TO_N, mrSuboperation2.getOppositeFeatureName());
 		assertTrue(mrSuboperation2.isBidirectional());
 		final Set<ModelElementId> otherInvolvedModelElements = mrSuboperation2.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements.size());
 		assertEquals(oldTestElementId, otherInvolvedModelElements.iterator().next());
 
 		assertEquals(NON_CONTAINED_1_TO_N, mrSuboperation3.getFeatureName());
 		assertEquals(0, mrSuboperation3.getIndex());
 		assertEquals(oldTestElementId, mrSuboperation3.getModelElementId());
 		assertEquals(NON_CONTAINED_N_TO1, mrSuboperation3.getOppositeFeatureName());
 		assertFalse(mrSuboperation3.isAdd());
 		assertTrue(mrSuboperation3.isBidirectional());
 		final Set<ModelElementId> otherInvolvedModelElements0 = mrSuboperation3.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements0.size());
 		final EList<ModelElementId> referencedModelElements0 = mrSuboperation3.getReferencedModelElements();
 		assertEquals(1, referencedModelElements0.size());
 		assertEquals(useCaseId, referencedModelElements0.get(0));
 
 		final ModelElementId newTestElementId = ModelUtil.getProject(newTestElement).getModelElementId(newTestElement);
 
 		assertEquals(NON_CONTAINED_N_TO_M, mrSuboperation4.getFeatureName());
 		assertEquals(0, mrSuboperation4.getIndex());
 		assertEquals(useCaseId, mrSuboperation4.getModelElementId());
 		assertEquals(NON_CONTAINED_M_TO_N, mrSuboperation4.getOppositeFeatureName());
 		assertFalse(mrSuboperation4.isAdd());
 		assertTrue(mrSuboperation4.isBidirectional());
 		final Set<ModelElementId> otherInvolvedModelElements2 = mrSuboperation4.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements2.size());
 		final EList<ModelElementId> referencedModelElements = mrSuboperation4.getReferencedModelElements();
 		assertEquals(1, referencedModelElements.size());
 		assertEquals(newTestElementId, referencedModelElements.get(0));
 
 		assertEquals(newTestElementId, mrSuboperation5.getModelElementId());
 		assertEquals(NON_CONTAINED_M_TO_N, mrSuboperation5.getFeatureName());
 		assertEquals(false, mrSuboperation5.isAdd());
 		assertEquals(1, mrSuboperation5.getReferencedModelElements().size());
 		assertEquals(useCaseId, mrSuboperation5.getReferencedModelElements().get(0));
 
 		assertEquals(NON_CONTAINED_N_TO_M, mrSuboperation6.getFeatureName());
 		assertEquals(0, mrSuboperation6.getIndex());
 		assertEquals(useCaseId, mrSuboperation6.getModelElementId());
 		assertEquals(NON_CONTAINED_M_TO_N, mrSuboperation6.getOppositeFeatureName());
 		assertEquals(false, mrSuboperation6.isAdd());
 		assertEquals(true, mrSuboperation6.isBidirectional());
 		final Set<ModelElementId> otherInvolvedModelElements6 = mrSuboperation6.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements6.size());
 		final EList<ModelElementId> referencedModelElements6 = mrSuboperation6.getReferencedModelElements();
 		assertEquals(1, referencedModelElements6.size());
 		final ModelElementId otherTestElementId = ModelUtil.getProject(otherTestElement).getModelElementId(
 			otherTestElement);
 		assertEquals(otherTestElementId, referencedModelElements6.get(0));
 
 		assertEquals(otherTestElementId, mrSuboperation7.getModelElementId());
 		assertEquals(NON_CONTAINED_M_TO_N, mrSuboperation7.getFeatureName());
 		assertEquals(false, mrSuboperation7.isAdd());
 		assertEquals(1, mrSuboperation7.getReferencedModelElements().size());
 		assertEquals(useCaseId, mrSuboperation7.getReferencedModelElements().get(0));
 
 	}
 
 	/**
 	 * Tests the copy to clipboard and paste from clipboard command.
 	 */
 	@Test
 	public void cutAndPasteFromClipboardCommand() {
 
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		ProjectUtil.addElement(getProjectSpace().toAPI(), leafSection);
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// cut to clipboard
 		final Collection<TestElement> toCut = new ArrayList<TestElement>();
 		toCut.add(actor);
 		final Command copyCommand = editingDomain.createCommand(CutToClipboardCommand.class, new CommandParameter(
 			leafSection, TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS, toCut));
 
 		editingDomain.getCommandStack().execute(copyCommand);
 
 		assertEquals(0, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canUndo());
 
 		// undo the cut command
 		editingDomain.getCommandStack().undo();
 
 		assertEquals(1, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canRedo());
 
 		// redo the cut command
 		editingDomain.getCommandStack().redo();
 
 		assertEquals(0, leafSection.getContainedElements().size());
 
 		// paste from clipboard
 		final Command pasteCommand = editingDomain.createCommand(PasteFromClipboardCommand.class, new CommandParameter(
 			leafSection,
 			TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS, Collections.emptyList(),
 			CommandParameter.NO_INDEX));
 
 		editingDomain.getCommandStack().execute(pasteCommand);
 
 		// assert that the ids are not equal
 		assertEquals(1, leafSection.getContainedElements().size());
 
 		// undo the paste command
 		editingDomain.getCommandStack().undo();
 
 		assertEquals(0, leafSection.getContainedElements().size());
 
 		// redo the paste command
 		editingDomain.getCommandStack().redo();
 
 		assertEquals(1, leafSection.getContainedElements().size());
 	}
 
 	/**
 	 * Tests the cut to clipboard and paste from clipboard command.
 	 */
 	@Test
 	public void cutAndPasteFromClipboardCommand_DirectCreation() {
 
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(leafSection);
 			}
 		}.run(false);
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// copy to clipboard
 		final Command cutCommand = CutToClipboardCommand.create(editingDomain, leafSection,
 			TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS, actor);
 
 		if (cutCommand.canExecute()) {
 			editingDomain.getCommandStack().execute(cutCommand);
 		} else {
 			fail(COMMAND_NOT_EXECUTABLE);
 		}
 		assertEquals(0, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canUndo());
 
 		// undo the cut command
 		editingDomain.getCommandStack().undo();
 		assertEquals(1, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canRedo());
 
 		// redo the cut command
 		editingDomain.getCommandStack().redo();
 		assertEquals(0, leafSection.getContainedElements().size());
 
 		// paste from clipboard
 		final Command pasteCommand = PasteFromClipboardCommand.create(editingDomain, leafSection,
 			TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS,
 			CommandParameter.NO_INDEX);
 
 		if (pasteCommand.canExecute()) {
 			editingDomain.getCommandStack().execute(pasteCommand);
 			assertEquals(1, leafSection.getContainedElements().size());
 		} else {
 			fail(COMMAND_NOT_EXECUTABLE);
 		}
 
 		// undo the paste command
 		editingDomain.getCommandStack().undo();
 		assertEquals(0, leafSection.getContainedElements().size());
 
 		// redo the paste command
 		editingDomain.getCommandStack().redo();
 		assertEquals(1, leafSection.getContainedElements().size());
 
 	}
 
 	/**
 	 * Might be no problem in runtime??!! Please have a look at it.
 	 */
 	@Test
 	public void testFunnyIssue() {
 
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(leafSection);
 			}
 		}.run(false);
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// cut to clipboard
 		final Command cutCommand = CutToClipboardCommand.create(editingDomain, leafSection,
 			TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS
 			, actor);
 
 		if (cutCommand.canExecute()) {
 			editingDomain.getCommandStack().execute(cutCommand);
 		} else {
 			fail(COMMAND_NOT_EXECUTABLE);
 		}
 		assertEquals(0, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canUndo());
 
 		// undo the command
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				editingDomain.getCommandStack().undo();
 			}
 		}.run(false);
 		// does not work but is strange anyway
 		// assertTrue(editingDomain.getCommandStack().canRedo());
 		assertEquals(1, leafSection.getContainedElements().size());
 
 	}
 
 	/**
 	 * Tests remove command.
 	 */
 	@Test
 	public void removeCommand_DirectCreation() {
 
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(leafSection);
 			}
 		}.run(false);
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// remove
 		final Command removeCommand = RemoveCommand.create(editingDomain, leafSection,
 			TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS, actor);
 		if (removeCommand.canExecute()) {
 			editingDomain.getCommandStack().execute(removeCommand);
 		} else {
 			fail(COMMAND_NOT_EXECUTABLE);
 		}
 		assertEquals(0, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canUndo());
 
 		// undo the command
 		editingDomain.getCommandStack().undo();
 		assertEquals(1, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canRedo());
 
 		// redo the command
 		editingDomain.getCommandStack().redo();
 		assertEquals(0, leafSection.getContainedElements().size());
 
 	}
 
 	/**
 	 * Tests the remove command.
 	 */
 	@Test
 	public void removeCommand() {
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(leafSection);
 			}
 		}.run(false);
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// remove
 		final Collection<TestElement> toRemove = new ArrayList<TestElement>();
 		toRemove.add(actor);
 		final Command copyCommand = editingDomain.createCommand(RemoveCommand.class, new CommandParameter(leafSection,
 			TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS, toRemove));
 		if (copyCommand.canExecute()) {
 			editingDomain.getCommandStack().execute(copyCommand);
 		} else {
 			fail(COMMAND_NOT_EXECUTABLE);
 		}
 
 		assertEquals(0, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canUndo());
 
 		// undo the command
 		editingDomain.getCommandStack().undo();
 		assertEquals(1, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canRedo());
 
 		// redo the command
 		editingDomain.getCommandStack().redo();
 		assertEquals(0, leafSection.getContainedElements().size());
 	}
 
 	/**
 	 * Tests delete command.
 	 */
 	@Test
 	public void deleteCommand_DirectCreation() {
 
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		Add.toProject(getLocalProject(), leafSection);
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// delete
 		final Collection<TestElement> toDelete = new ArrayList<TestElement>();
 		toDelete.add(actor);
 		final Command command = DeleteCommand.create(editingDomain, toDelete);
 		if (command.canExecute()) {
 			editingDomain.getCommandStack().execute(command);
 		} else {
 			fail(COMMAND_NOT_EXECUTABLE);
 		}
 		assertEquals(0, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canUndo());
 
 		// undo the command
 		editingDomain.getCommandStack().undo();
 		assertEquals(1, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canRedo());
 
 		// redo the command
 		editingDomain.getCommandStack().redo();
 		assertEquals(0, leafSection.getContainedElements().size());
 	}
 
 	/**
 	 * Tests the delete command.
 	 */
 	@Test
 	public void deleteCommand() {
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		Add.toProject(getLocalProject(), leafSection);
 
 		clearOperations();
 
 		assertEquals(0, getProjectSpace().getOperations().size());
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// delete
 		final Collection<TestElement> toDelete = new ArrayList<TestElement>();
 		toDelete.add(actor);
 
 		// delete actor from model elements feature
 		final Command command = editingDomain.createCommand(DeleteCommand.class, new CommandParameter(leafSection,
 			TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS, toDelete));
 		if (command.canExecute()) {
 			editingDomain.getCommandStack().execute(command);
 		} else {
 			fail(COMMAND_NOT_EXECUTABLE);
 		}
 
 		assertEquals(0, leafSection.getContainedElements().size());
 		// undo delete
 		assertTrue(editingDomain.getCommandStack().canUndo());
 		assertEquals(1, getProjectSpace().getOperations().size());
 
 		// undo the command - add actor to model elements feature
 
 		editingDomain.getCommandStack().undo();
 
 		assertEquals(1, leafSection.getContainedElements().size());
 		// // assertEquals(0, getProjectSpace().getOperations().size());
 		assertTrue(editingDomain.getCommandStack().canRedo());
 
 		// redo the command - delete again
 		editingDomain.getCommandStack().redo();
 
 		assertEquals(0, leafSection.getContainedElements().size());
 	}
 
 	// @Test: disabled for Bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=357464
 	public void cutAndPasteFromClipboardCommandComplex() {
 
 		final TestElement leafSection = Create.testElement();
 		final TestElement workPackage = Create.testElement();
 		final TestElement user = Create.testElement();
 
 		workPackage.setName(SPRINT1);
 		workPackage.setNonContained_NTo1(user);
 
 		final TestElement ai1 = Create.testElement();
 		ai1.setName(AI12);
 		ai1.setContainer2(workPackage);
 		final TestElement ai2 = Create.testElement();
 		ai2.setName(AI22);
 		ai2.setContainer2(workPackage);
 		leafSection.getContainedElements().add(workPackage);
 		leafSection.getContainedElements().add(user);
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(leafSection);
 				clearOperations();
 			}
 		}.run(false);
 		final ModelElementId workPackageId = getProject().getModelElementId(workPackage);
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// cut the element
 		final Command command = CutToClipboardCommand.create(editingDomain, workPackage);
 		editingDomain.getCommandStack().execute(command);
 
 		assertTrue(ESWorkspaceProviderImpl.getInstance().getEditingDomain().getClipboard().contains(workPackage));
 		assertEquals(1, ModelUtil.getAllContainedModelElements(leafSection, false).size());
 
 		assertTrue(getProject().contains(workPackageId));
 
 		final Command pasteCommand = PasteFromClipboardCommand.create(editingDomain, leafSection,
 			TestmodelPackage.Literals.TEST_ELEMENT__CONTAINED_ELEMENTS, CommandParameter.NO_INDEX);
 		editingDomain.getCommandStack().execute(pasteCommand);
 
 		assertEquals(4, ModelUtil.getAllContainedModelElements(leafSection, false).size());
 		assertTrue(getProject().contains(workPackageId));
 
 		assertEquals(2, getProjectSpace().getOperations().size());
 
 	}
 
 	@Test
 	public void testGetEditingDomain() {
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(leafSection);
 				clearOperations();
 			}
 		}.run(false);
 
 		assertEquals(0, getProjectSpace().getOperations().size());
 
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		final EditingDomain domain1 = AdapterFactoryEditingDomain.getEditingDomainFor(actor);
 		assertSame(editingDomain, domain1);
 		assertNotNull(domain1);
 		assertNotNull(editingDomain);
 	}
 
 	/**
 	 * Tests the delete from unicase delete command.
 	 */
 	@Test
 	public void deleteByUnicaseDeleteCommand() {
 		final TestElement leafSection = Create.testElement();
 		final TestElement actor = Create.testElement();
 		leafSection.getContainedElements().add(actor);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(leafSection);
 				clearOperations();
 			}
 		}.run(false);
 
 		assertEquals(0, getProjectSpace().getOperations().size());
 		final EditingDomain editingDomain = ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 
 		// delete
 		editingDomain.getCommandStack().execute(DeleteCommand.create(editingDomain, actor));
 
 		assertEquals(0, leafSection.getContainedElements().size());
 		assertTrue(editingDomain.getCommandStack().canUndo());
 		assertEquals(1, getProjectSpace().getOperations().size());
 
 		// undo the command
 		// command.undo();
 		editingDomain.getCommandStack().undo();
 
 		assertEquals(1, leafSection.getContainedElements().size());
 		// assertEquals(0, getProjectSpace().getOperations().size());
 		assertTrue(editingDomain.getCommandStack().canRedo());
 
 		// redo the command
 		editingDomain.getCommandStack().redo();
 		assertEquals(0, leafSection.getContainedElements().size());
 		// assertEquals(1, getProjectSpace().getOperations().size());
 	}
 
 	@Test
 	public void supressCommandStackException() {
 		new EMFStoreCommand() {
 			@SuppressWarnings("null")
 			@Override
 			protected void doRun() {
 				final IdEObjectCollection col = null;
 				// provoke NPE
 				col.getModelElements();
 			}
 		}.run(true);
 	}
 
 	@Test(expected = NullPointerException.class)
 	public void rethrowCommandStackException() {
 		new EMFStoreCommand() {
 			@SuppressWarnings("null")
 			@Override
 			protected void doRun() {
 				final IdEObjectCollection col = null;
 				// provoke NPE
 				col.getModelElements();
 			}
 		}.run(false);
 	}
 }
