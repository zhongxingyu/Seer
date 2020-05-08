 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  */
 package org.eclipse.emf.emfstore.client.test.changeTracking.operations;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.emfstore.client.model.ModelPackage;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.exceptions.UnsupportedNotificationException;
 import org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl;
 import org.eclipse.emf.emfstore.client.model.observers.PostCreationObserver;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.client.test.WorkspaceTest;
 import org.eclipse.emf.emfstore.client.test.model.document.CompositeSection;
 import org.eclipse.emf.emfstore.client.test.model.document.DocumentFactory;
 import org.eclipse.emf.emfstore.client.test.model.document.LeafSection;
 import org.eclipse.emf.emfstore.client.test.model.meeting.CompositeMeetingSection;
 import org.eclipse.emf.emfstore.client.test.model.meeting.IssueMeetingSection;
 import org.eclipse.emf.emfstore.client.test.model.meeting.Meeting;
 import org.eclipse.emf.emfstore.client.test.model.meeting.MeetingFactory;
 import org.eclipse.emf.emfstore.client.test.model.meeting.WorkItemMeetingSection;
 import org.eclipse.emf.emfstore.client.test.model.rationale.Issue;
 import org.eclipse.emf.emfstore.client.test.model.rationale.RationaleFactory;
 import org.eclipse.emf.emfstore.client.test.model.rationale.Solution;
 import org.eclipse.emf.emfstore.client.test.model.requirement.Actor;
 import org.eclipse.emf.emfstore.client.test.model.requirement.FunctionalRequirement;
 import org.eclipse.emf.emfstore.client.test.model.requirement.RequirementFactory;
 import org.eclipse.emf.emfstore.client.test.model.requirement.UseCase;
 import org.eclipse.emf.emfstore.client.test.model.task.ActionItem;
 import org.eclipse.emf.emfstore.client.test.model.task.TaskFactory;
 import org.eclipse.emf.emfstore.client.test.testmodel.TestElement;
 import org.eclipse.emf.emfstore.common.CommonUtil;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.impl.ProjectImpl;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.CreateDeleteOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.MultiReferenceOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.ReferenceOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.SingleReferenceOperation;
 import org.junit.Test;
 
 /**
  * Test creating an deleting elements.
  * 
  * @author koegel
  */
 public class CreateDeleteOperationTest extends WorkspaceTest {
 
 	/**
 	 * Test element creation tracking.
 	 * 
 	 * @throws UnsupportedOperationException on test fail
 	 * @throws UnsupportedNotificationException on test fail
 	 */
 	@Test
 	public void createElementTest() throws UnsupportedOperationException, UnsupportedNotificationException {
 
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(useCase);
 			}
 		}.run(false);
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 
 		assertEquals(1, operations.size());
 		AbstractOperation operation = operations.get(0);
 		assertEquals(true, operation instanceof CreateDeleteOperation);
 		CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation;
 
 		ModelElementId useCaseId = ModelUtil.getProject(useCase).getModelElementId(useCase);
 
 		assertEquals(useCaseId, createDeleteOperation.getModelElementId());
 		assertEquals(0, createDeleteOperation.getSubOperations().size());
 		assertEquals(false, createDeleteOperation.isDelete());
 	}
 
 	/**
 	 * Test element creation tracking.
 	 * 
 	 * @throws UnsupportedOperationException on test fail
 	 * @throws UnsupportedNotificationException on test fail
 	 */
 	@Test
 	public void createElementWithPostCreationObserverTest() throws UnsupportedOperationException,
 		UnsupportedNotificationException {
 
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 		useCase.setName("oldName");
 		PostCreationObserver observer = new PostCreationObserver() {
 
 			public void onCreation(EObject modelElement) {
 				if (modelElement instanceof UseCase) {
 					UseCase useCase = (UseCase) modelElement;
 					useCase.setName("postCreationChangedName");
 				}
 			}
 		};
 		WorkspaceManager.getObserverBus().register(observer);
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(useCase);
 			}
 		}.run(false);
 
 		WorkspaceManager.getObserverBus().unregister(observer);
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 
 		assertEquals(1, operations.size());
 		AbstractOperation operation = operations.get(0);
 		assertEquals(true, operation instanceof CreateDeleteOperation);
 		CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation;
 
 		ModelElementId useCaseId = ModelUtil.getProject(useCase).getModelElementId(useCase);
 
 		assertEquals(useCaseId, createDeleteOperation.getModelElementId());
 		assertEquals("postCreationChangedName", ((UseCase) createDeleteOperation.getModelElement()).getName());
 		assertEquals(0, createDeleteOperation.getSubOperations().size());
 		assertEquals(false, createDeleteOperation.isDelete());
 	}
 
 	/**
 	 * Test adding an element with cross references to an existing element.
 	 * 
 	 * @throws UnsupportedOperationException on test fail
 	 * @throws UnsupportedNotificationException on test fail
 	 */
 	@Test
 	public void createElementwithCrossreferencesTest() throws UnsupportedOperationException,
 		UnsupportedNotificationException {
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(useCase);
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().containsInstance(useCase));
 		assertEquals(getProject(), ModelUtil.getProject(useCase));
 
 		final FunctionalRequirement functionalRequirement = RequirementFactory.eINSTANCE.createFunctionalRequirement();
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				useCase.getFunctionalRequirements().add(functionalRequirement);
 				clearOperations();
 			}
 		}.run(false);
 
 		assertEquals(functionalRequirement, useCase.getFunctionalRequirements().get(0));
 		assertEquals(useCase, functionalRequirement.getUseCases().get(0));
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(functionalRequirement);
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().containsInstance(functionalRequirement));
 		assertEquals(getProject(), ModelUtil.getProject(functionalRequirement));
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 
 		assertEquals(1, operations.size());
 		AbstractOperation operation = operations.get(0);
 		assertEquals(true, operation instanceof CreateDeleteOperation);
 		CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation;
 
 		ModelElementId funtionalRQID = ModelUtil.getProject(useCase).getModelElementId(functionalRequirement);
 
 		assertEquals(funtionalRQID, createDeleteOperation.getModelElementId());
 		assertEquals(2, createDeleteOperation.getSubOperations().size());
 		assertEquals(false, createDeleteOperation.isDelete());
 
 		MultiReferenceOperation subOperation1 = (MultiReferenceOperation) createDeleteOperation.getSubOperations().get(
 			0);
 		assertEquals(functionalRequirement, getProject().getModelElement(subOperation1.getModelElementId()));
 		assertEquals(useCase, getProject().getModelElement(subOperation1.getReferencedModelElements().get(0)));
 
 		MultiReferenceOperation subOperation2 = (MultiReferenceOperation) createDeleteOperation.getSubOperations().get(
 			1);
 		assertEquals(useCase, getProject().getModelElement(subOperation2.getModelElementId()));
 		assertEquals(functionalRequirement,
 			getProject().getModelElement(subOperation2.getReferencedModelElements().get(0)));
 	}
 
 	/**
 	 * check element deletion tracking.
 	 * 
 	 * @throws UnsupportedOperationException on test fail
 	 * @throws UnsupportedNotificationException on test fail
 	 */
 	@Test
 	public void deleteElementTest() throws UnsupportedOperationException, UnsupportedNotificationException {
 
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(useCase);
 				clearOperations();
 			}
 		}.run(false);
 
 		ModelElementId useCaseId = ModelUtil.getProject(useCase).getModelElementId(useCase);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().deleteModelElement(useCase);
 			}
 		}.run(false);
 
		List<AbstractOperation> operations = getProjectSpace().getOperations();
 
 		assertEquals(1, operations.size());
 		AbstractOperation operation = operations.get(0);
 		assertEquals(true, operation instanceof CreateDeleteOperation);
 		CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation;
 
 		assertEquals(useCaseId, createDeleteOperation.getModelElementId());
 		assertEquals(0, createDeleteOperation.getSubOperations().size());
 		assertEquals(true, createDeleteOperation.isDelete());
 	}
 
 	/**
 	 * check complex element deletion tracking.
 	 * 
 	 * @throws UnsupportedOperationException on test fail
 	 * @throws UnsupportedNotificationException on test fail
 	 * @throws IOException
 	 */
 	@Test
 	// BEGIN COMPLEX CODE
 	public void complexDeleteElementTest() throws UnsupportedOperationException, UnsupportedNotificationException,
 		IOException {
 
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 		final Actor oldActor = RequirementFactory.eINSTANCE.createActor();
 		final Actor newActor = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherActor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				section.getModelElements().add(useCase);
 				section.getModelElements().add(oldActor);
 				getProject().addModelElement(newActor);
 				getProject().addModelElement(otherActor);
 				useCase.setInitiatingActor(oldActor);
 				useCase.getParticipatingActors().add(newActor);
 				useCase.getParticipatingActors().add(otherActor);
 				assertEquals(true, getProject().containsInstance(useCase));
 				assertEquals(getProject(), ModelUtil.getProject(useCase));
 				clearOperations();
 			}
 		}.run(false);
 
 		ModelElementId useCaseId = ModelUtil.getProject(useCase).getModelElementId(useCase);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().deleteModelElement(useCase);
 			}
 		}.run(false);
 
 		assertEquals(false, getProject().containsInstance(useCase));
 		// assertEquals(null, useCase.eContainer());
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 
 		assertEquals(1, operations.size());
 		AbstractOperation operation = operations.get(0);
 		assertEquals(true, operation instanceof CreateDeleteOperation);
 		CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation;
 		assertEquals(true, createDeleteOperation.isDelete());
 
 		assertEquals(useCaseId, createDeleteOperation.getModelElementId());
 		EList<ReferenceOperation> subOperations = createDeleteOperation.getSubOperations();
 
 		assertEquals(8, subOperations.size());
 		AbstractOperation subOperation0 = subOperations.get(0);
 		AbstractOperation subOperation1 = subOperations.get(1);
 		AbstractOperation subOperation2 = subOperations.get(2);
 		AbstractOperation subOperation3 = subOperations.get(3);
 		AbstractOperation subOperation4 = subOperations.get(4);
 		AbstractOperation subOperation5 = subOperations.get(5);
 		AbstractOperation subOperation6 = subOperations.get(6);
 		AbstractOperation subOperation7 = subOperations.get(7);
 
 		assertEquals(true, subOperation0 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation1 instanceof SingleReferenceOperation);
 		assertEquals(true, subOperation2 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation3 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation4 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation5 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation6 instanceof SingleReferenceOperation);
 		assertEquals(true, subOperation7 instanceof MultiReferenceOperation);
 
 		MultiReferenceOperation mrSubOperation0 = (MultiReferenceOperation) subOperation0;
 		SingleReferenceOperation mrSubOperation1 = (SingleReferenceOperation) subOperation1;
 		MultiReferenceOperation mrSubOperation2 = (MultiReferenceOperation) subOperation2;
 		MultiReferenceOperation mrSubOperation3 = (MultiReferenceOperation) subOperation3;
 		MultiReferenceOperation mrSubOperation4 = (MultiReferenceOperation) subOperation4;
 		MultiReferenceOperation mrSubOperation5 = (MultiReferenceOperation) subOperation5;
 		SingleReferenceOperation mrSubOperation6 = (SingleReferenceOperation) subOperation6;
 		MultiReferenceOperation mrSubOperation7 = (MultiReferenceOperation) subOperation7;
 
 		assertEquals("initiatedUseCases", mrSubOperation0.getFeatureName());
 		assertEquals(0, mrSubOperation0.getIndex());
 
 		ModelElementId oldActorId = ModelUtil.getProject(oldActor).getModelElementId(oldActor);
 		ModelElementId otherActorId = ModelUtil.getProject(otherActor).getModelElementId(otherActor);
 		ModelElementId newActorId = ModelUtil.getProject(newActor).getModelElementId(newActor);
 		ModelElementId sectionId = ModelUtil.getProject(section).getModelElementId(section);
 
 		assertEquals(oldActorId, mrSubOperation0.getModelElementId());
 		assertEquals("initiatingActor", mrSubOperation0.getOppositeFeatureName());
 		assertEquals(false, mrSubOperation0.isAdd());
 		assertEquals(true, mrSubOperation0.isBidirectional());
 		Set<ModelElementId> otherInvolvedModelElements0 = mrSubOperation0.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements0.size());
 		EList<ModelElementId> referencedModelElements0 = mrSubOperation0.getReferencedModelElements();
 		assertEquals(1, referencedModelElements0.size());
 		assertEquals(useCaseId, referencedModelElements0.get(0));
 
 		assertEquals(oldActorId, mrSubOperation1.getOldValue());
 		assertEquals(null, mrSubOperation1.getNewValue());
 		assertEquals("initiatingActor", mrSubOperation1.getFeatureName());
 		assertEquals(useCaseId, mrSubOperation1.getModelElementId());
 		assertEquals("initiatedUseCases", mrSubOperation1.getOppositeFeatureName());
 		assertEquals(true, mrSubOperation1.isBidirectional());
 		Set<ModelElementId> otherInvolvedModelElements = mrSubOperation1.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements.size());
 		assertEquals(oldActorId, otherInvolvedModelElements.iterator().next());
 
 		assertEquals(newActorId, mrSubOperation2.getModelElementId());
 		assertEquals("participatedUseCases", mrSubOperation2.getFeatureName());
 		assertEquals(false, mrSubOperation2.isAdd());
 		assertEquals(1, mrSubOperation2.getReferencedModelElements().size());
 		assertEquals(useCaseId, mrSubOperation2.getReferencedModelElements().get(0));
 
 		assertEquals(useCaseId, mrSubOperation3.getModelElementId());
 		assertEquals("participatingActors", mrSubOperation3.getFeatureName());
 		assertEquals(false, mrSubOperation3.isAdd());
 		assertEquals(1, mrSubOperation3.getReferencedModelElements().size());
 		assertEquals(newActorId, mrSubOperation3.getReferencedModelElements().get(0));
 
 		assertEquals(otherActorId, mrSubOperation4.getModelElementId());
 		assertEquals("participatedUseCases", mrSubOperation4.getFeatureName());
 		assertEquals(false, mrSubOperation4.isAdd());
 		assertEquals(1, mrSubOperation4.getReferencedModelElements().size());
 		assertEquals(useCaseId, mrSubOperation4.getReferencedModelElements().get(0));
 
 		assertEquals("participatingActors", mrSubOperation5.getFeatureName());
 		assertEquals(0, mrSubOperation5.getIndex());
 		assertEquals(useCaseId, mrSubOperation5.getModelElementId());
 		assertEquals("participatedUseCases", mrSubOperation5.getOppositeFeatureName());
 		assertEquals(false, mrSubOperation5.isAdd());
 		assertEquals(true, mrSubOperation5.isBidirectional());
 		Set<ModelElementId> otherInvolvedModelElements2 = mrSubOperation5.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements2.size());
 		EList<ModelElementId> referencedModelElements = mrSubOperation5.getReferencedModelElements();
 		assertEquals(1, referencedModelElements.size());
 		assertEquals(otherActorId, referencedModelElements.get(0));
 
 		assertEquals(useCaseId, mrSubOperation6.getModelElementId());
 		assertEquals("leafSection", mrSubOperation6.getFeatureName());
 		assertEquals(sectionId, mrSubOperation6.getOldValue());
 		assertEquals(null, mrSubOperation6.getNewValue());
 
 		assertEquals("modelElements", mrSubOperation7.getFeatureName());
 		assertEquals(0, mrSubOperation7.getIndex());
 		assertEquals(sectionId, mrSubOperation7.getModelElementId());
 		assertEquals("leafSection", mrSubOperation7.getOppositeFeatureName());
 		assertEquals(false, mrSubOperation7.isAdd());
 		assertEquals(true, mrSubOperation7.isBidirectional());
 		Set<ModelElementId> otherInvolvedModelElements3 = mrSubOperation7.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements3.size());
 		EList<ModelElementId> referencedModelElements3 = mrSubOperation7.getReferencedModelElements();
 		assertEquals(1, referencedModelElements3.size());
 		assertEquals(useCaseId, referencedModelElements3.get(0));
 
 		// ((ProjectSpaceImpl) getProjectSpace()).saveProjectSpaceOnly();
 		ProjectSpace loadedProjectSpace = ModelUtil.loadEObjectFromResource(ModelPackage.eINSTANCE.getProjectSpace(),
 			getProjectSpace().eResource().getURI(), false);
 		Project loadedProject = loadedProjectSpace.getProject();
 
 		assertEquals(false, loadedProject.containsInstance(useCase));
		operations = loadedProjectSpace.getOperations();
 
 		assertEquals(1, operations.size());
 		operation = operations.get(0);
 		assertEquals(true, operation instanceof CreateDeleteOperation);
 		createDeleteOperation = (CreateDeleteOperation) operation;
 		assertEquals(true, createDeleteOperation.isDelete());
 
 		assertEquals(useCaseId, createDeleteOperation.getModelElementId());
 		subOperations = createDeleteOperation.getSubOperations();
 
 		assertEquals(8, subOperations.size());
 		subOperation0 = subOperations.get(0);
 		subOperation1 = subOperations.get(1);
 		subOperation2 = subOperations.get(2);
 		subOperation3 = subOperations.get(3);
 		subOperation4 = subOperations.get(4);
 		subOperation5 = subOperations.get(5);
 		subOperation6 = subOperations.get(6);
 		subOperation7 = subOperations.get(7);
 
 		assertEquals(true, subOperation0 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation1 instanceof SingleReferenceOperation);
 		assertEquals(true, subOperation2 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation3 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation4 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation5 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation6 instanceof SingleReferenceOperation);
 		assertEquals(true, subOperation7 instanceof MultiReferenceOperation);
 
 		mrSubOperation0 = (MultiReferenceOperation) subOperation0;
 		mrSubOperation1 = (SingleReferenceOperation) subOperation1;
 		mrSubOperation2 = (MultiReferenceOperation) subOperation2;
 		mrSubOperation3 = (MultiReferenceOperation) subOperation3;
 		mrSubOperation4 = (MultiReferenceOperation) subOperation4;
 		mrSubOperation5 = (MultiReferenceOperation) subOperation5;
 		mrSubOperation6 = (SingleReferenceOperation) subOperation6;
 		mrSubOperation7 = (MultiReferenceOperation) subOperation7;
 
 		assertEquals("initiatedUseCases", mrSubOperation0.getFeatureName());
 		assertEquals(0, mrSubOperation0.getIndex());
 
 		assertEquals(oldActorId, mrSubOperation0.getModelElementId());
 		assertEquals("initiatingActor", mrSubOperation0.getOppositeFeatureName());
 		assertEquals(false, mrSubOperation0.isAdd());
 		assertEquals(true, mrSubOperation0.isBidirectional());
 		otherInvolvedModelElements0 = mrSubOperation0.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements0.size());
 		referencedModelElements0 = mrSubOperation0.getReferencedModelElements();
 		assertEquals(1, referencedModelElements0.size());
 		assertEquals(useCaseId, referencedModelElements0.get(0));
 
 		assertEquals(oldActorId, mrSubOperation1.getOldValue());
 		assertEquals(null, mrSubOperation1.getNewValue());
 		assertEquals("initiatingActor", mrSubOperation1.getFeatureName());
 		assertEquals(useCaseId, mrSubOperation1.getModelElementId());
 		assertEquals("initiatedUseCases", mrSubOperation1.getOppositeFeatureName());
 		assertEquals(true, mrSubOperation1.isBidirectional());
 		otherInvolvedModelElements = mrSubOperation1.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements.size());
 		assertEquals(oldActorId, otherInvolvedModelElements.iterator().next());
 
 		assertEquals(newActorId, mrSubOperation2.getModelElementId());
 		assertEquals("participatedUseCases", mrSubOperation2.getFeatureName());
 		assertEquals(false, mrSubOperation2.isAdd());
 		assertEquals(1, mrSubOperation2.getReferencedModelElements().size());
 		assertEquals(useCaseId, mrSubOperation2.getReferencedModelElements().get(0));
 
 		assertEquals(useCaseId, mrSubOperation3.getModelElementId());
 		assertEquals("participatingActors", mrSubOperation3.getFeatureName());
 		assertEquals(false, mrSubOperation3.isAdd());
 		assertEquals(1, mrSubOperation3.getReferencedModelElements().size());
 		assertEquals(newActorId, mrSubOperation3.getReferencedModelElements().get(0));
 
 		assertEquals("participatedUseCases", mrSubOperation4.getFeatureName());
 		assertEquals(0, mrSubOperation4.getIndex());
 		assertEquals(otherActorId, mrSubOperation4.getModelElementId());
 		assertEquals("participatingActors", mrSubOperation4.getOppositeFeatureName());
 		assertEquals(false, mrSubOperation4.isAdd());
 		assertEquals(true, mrSubOperation4.isBidirectional());
 		otherInvolvedModelElements2 = mrSubOperation4.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements2.size());
 		referencedModelElements = mrSubOperation4.getReferencedModelElements();
 		assertEquals(1, referencedModelElements.size());
 		assertEquals(useCaseId, referencedModelElements.get(0));
 
 		assertEquals("participatingActors", mrSubOperation5.getFeatureName());
 		assertEquals(0, mrSubOperation5.getIndex());
 		assertEquals(useCaseId, mrSubOperation5.getModelElementId());
 		assertEquals("participatedUseCases", mrSubOperation5.getOppositeFeatureName());
 		assertEquals(false, mrSubOperation5.isAdd());
 		assertEquals(true, mrSubOperation5.isBidirectional());
 		otherInvolvedModelElements2 = mrSubOperation5.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements2.size());
 		referencedModelElements = mrSubOperation5.getReferencedModelElements();
 		assertEquals(1, referencedModelElements.size());
 		assertEquals(otherActorId, referencedModelElements.get(0));
 
 		assertEquals(useCaseId, mrSubOperation6.getModelElementId());
 		assertEquals("leafSection", mrSubOperation6.getFeatureName());
 		assertEquals(sectionId, mrSubOperation6.getOldValue());
 		assertEquals(null, mrSubOperation6.getNewValue());
 
 		assertEquals("modelElements", mrSubOperation7.getFeatureName());
 		assertEquals(0, mrSubOperation7.getIndex());
 		assertEquals(sectionId, mrSubOperation7.getModelElementId());
 		assertEquals("leafSection", mrSubOperation7.getOppositeFeatureName());
 		assertEquals(false, mrSubOperation7.isAdd());
 		assertEquals(true, mrSubOperation7.isBidirectional());
 		otherInvolvedModelElements3 = mrSubOperation7.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements3.size());
 		referencedModelElements3 = mrSubOperation7.getReferencedModelElements();
 		assertEquals(1, referencedModelElements3.size());
 		assertEquals(useCaseId, referencedModelElements3.get(0));
 	}
 
 	/**
 	 * check complex element deletion tracking.
 	 * 
 	 * @throws UnsupportedOperationException on test fail
 	 * @throws UnsupportedNotificationException on test fail
 	 * @throws IOException
 	 */
 	@Test
 	public void complexDeleteElementReverseTest() throws UnsupportedOperationException,
 		UnsupportedNotificationException, IOException {
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 		final Actor oldActor = RequirementFactory.eINSTANCE.createActor();
 		final Actor newActor = RequirementFactory.eINSTANCE.createActor();
 		final Actor otherActor = RequirementFactory.eINSTANCE.createActor();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 				section.getModelElements().add(useCase);
 				section.getModelElements().add(oldActor);
 				getProject().addModelElement(newActor);
 				getProject().addModelElement(otherActor);
 				useCase.setInitiatingActor(oldActor);
 				useCase.getParticipatingActors().add(newActor);
 				useCase.getParticipatingActors().add(otherActor);
 				assertEquals(true, getProject().containsInstance(useCase));
 				assertEquals(true, getProject().containsInstance(oldActor));
 				assertEquals(true, getProject().containsInstance(newActor));
 				assertEquals(true, getProject().containsInstance(otherActor));
 				assertEquals(1, oldActor.getInitiatedUseCases().size());
 				assertEquals(1, newActor.getParticipatedUseCases().size());
 				assertEquals(1, otherActor.getParticipatedUseCases().size());
 				assertEquals(useCase, oldActor.getInitiatedUseCases().get(0));
 				assertEquals(useCase, newActor.getParticipatedUseCases().get(0));
 				assertEquals(useCase, otherActor.getParticipatedUseCases().get(0));
 
 				clearOperations();
 			}
 		}.run(false);
 
 		ModelElementId useCaseId = getProject().getModelElementId(useCase);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().deleteModelElement(useCase);
 			}
 		}.run(false);
 
 		assertEquals(false, getProject().containsInstance(useCase));
 		assertEquals(0, oldActor.getInitiatedUseCases().size());
 		assertEquals(0, newActor.getParticipatedUseCases().size());
 		assertEquals(0, otherActor.getParticipatedUseCases().size());
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 
 		assertEquals(1, operations.size());
 		AbstractOperation operation = operations.get(0);
 
 		final AbstractOperation reverse = operation.reverse();
 
 		assertEquals(true, reverse instanceof CreateDeleteOperation);
 		CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) reverse;
 		assertEquals(false, createDeleteOperation.isDelete());
 
 		assertEquals(useCaseId, createDeleteOperation.getModelElementId());
 		EList<ReferenceOperation> subOperations = createDeleteOperation.getSubOperations();
 
 		assertEquals(8, subOperations.size());
 		AbstractOperation subOperation0 = subOperations.get(7);
 		AbstractOperation subOperation1 = subOperations.get(6);
 		AbstractOperation subOperation2 = subOperations.get(5);
 		AbstractOperation subOperation3 = subOperations.get(4);
 		AbstractOperation subOperation4 = subOperations.get(3);
 		AbstractOperation subOperation5 = subOperations.get(2);
 		AbstractOperation subOperation6 = subOperations.get(1);
 		AbstractOperation subOperation7 = subOperations.get(0);
 
 		assertEquals(true, subOperation0 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation1 instanceof SingleReferenceOperation);
 		assertEquals(true, subOperation2 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation3 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation4 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation5 instanceof MultiReferenceOperation);
 		assertEquals(true, subOperation6 instanceof SingleReferenceOperation);
 		assertEquals(true, subOperation7 instanceof MultiReferenceOperation);
 
 		MultiReferenceOperation mrSubOperation0 = (MultiReferenceOperation) subOperation0;
 		SingleReferenceOperation mrSubOperation1 = (SingleReferenceOperation) subOperation1;
 		MultiReferenceOperation mrSubOperation2 = (MultiReferenceOperation) subOperation2;
 		MultiReferenceOperation mrSubOperation3 = (MultiReferenceOperation) subOperation3;
 		MultiReferenceOperation mrSubOperation4 = (MultiReferenceOperation) subOperation4;
 		MultiReferenceOperation mrSubOperation5 = (MultiReferenceOperation) subOperation5;
 		SingleReferenceOperation mrSubOperation6 = (SingleReferenceOperation) subOperation6;
 		MultiReferenceOperation mrSubOperation7 = (MultiReferenceOperation) subOperation7;
 
 		ModelElementId oldActorId = ModelUtil.getProject(oldActor).getModelElementId(oldActor);
 		ModelElementId newActorId = ModelUtil.getProject(newActor).getModelElementId(newActor);
 		ModelElementId otherActorId = ModelUtil.getProject(otherActor).getModelElementId(otherActor);
 		ModelElementId sectionId = ModelUtil.getProject(section).getModelElementId(section);
 
 		assertEquals("initiatedUseCases", mrSubOperation0.getFeatureName());
 		assertEquals(0, mrSubOperation0.getIndex());
 		assertEquals(oldActorId, mrSubOperation0.getModelElementId());
 		assertEquals("initiatingActor", mrSubOperation0.getOppositeFeatureName());
 		assertEquals(true, mrSubOperation0.isAdd());
 		assertEquals(true, mrSubOperation0.isBidirectional());
 		Set<ModelElementId> otherInvolvedModelElements0 = mrSubOperation0.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements0.size());
 		EList<ModelElementId> referencedModelElements0 = mrSubOperation0.getReferencedModelElements();
 		assertEquals(1, referencedModelElements0.size());
 		assertEquals(useCaseId, referencedModelElements0.get(0));
 
 		assertEquals(oldActorId, mrSubOperation1.getNewValue());
 		assertEquals(null, mrSubOperation1.getOldValue());
 		assertEquals("initiatingActor", mrSubOperation1.getFeatureName());
 		assertEquals(useCaseId, mrSubOperation1.getModelElementId());
 		assertEquals("initiatedUseCases", mrSubOperation1.getOppositeFeatureName());
 		assertEquals(true, mrSubOperation1.isBidirectional());
 		Set<ModelElementId> otherInvolvedModelElements = mrSubOperation1.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements.size());
 		assertEquals(oldActorId, otherInvolvedModelElements.iterator().next());
 
 		assertEquals(newActorId, mrSubOperation2.getModelElementId());
 		assertEquals("participatedUseCases", mrSubOperation2.getFeatureName());
 		assertEquals(true, mrSubOperation2.isAdd());
 		assertEquals(1, mrSubOperation2.getReferencedModelElements().size());
 		assertEquals(useCaseId, mrSubOperation2.getReferencedModelElements().get(0));
 
 		assertEquals(useCaseId, mrSubOperation3.getModelElementId());
 		assertEquals("participatingActors", mrSubOperation3.getFeatureName());
 		assertEquals(true, mrSubOperation3.isAdd());
 		assertEquals(1, mrSubOperation3.getReferencedModelElements().size());
 		assertEquals(newActorId, mrSubOperation3.getReferencedModelElements().get(0));
 
 		assertEquals("participatedUseCases", mrSubOperation4.getFeatureName());
 		assertEquals(0, mrSubOperation4.getIndex());
 		assertEquals(otherActorId, mrSubOperation4.getModelElementId());
 		assertEquals("participatingActors", mrSubOperation4.getOppositeFeatureName());
 		assertEquals(true, mrSubOperation4.isAdd());
 		assertEquals(true, mrSubOperation4.isBidirectional());
 		Set<ModelElementId> otherInvolvedModelElements2 = mrSubOperation4.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements2.size());
 		EList<ModelElementId> referencedModelElements = mrSubOperation4.getReferencedModelElements();
 		assertEquals(1, referencedModelElements.size());
 		assertEquals(useCaseId, referencedModelElements.get(0));
 
 		assertEquals("participatingActors", mrSubOperation5.getFeatureName());
 		assertEquals(0, mrSubOperation5.getIndex());
 		assertEquals(useCaseId, mrSubOperation5.getModelElementId());
 		assertEquals("participatedUseCases", mrSubOperation5.getOppositeFeatureName());
 		assertEquals(true, mrSubOperation5.isAdd());
 		assertEquals(true, mrSubOperation5.isBidirectional());
 		Set<ModelElementId> otherInvolvedModelElements3 = mrSubOperation5.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements3.size());
 		EList<ModelElementId> referencedModelElements2 = mrSubOperation5.getReferencedModelElements();
 		assertEquals(1, referencedModelElements2.size());
 		assertEquals(otherActorId, referencedModelElements2.get(0));
 
 		assertEquals(useCaseId, mrSubOperation6.getModelElementId());
 		assertEquals("leafSection", mrSubOperation6.getFeatureName());
 		assertEquals(sectionId, mrSubOperation6.getNewValue());
 		assertEquals(null, mrSubOperation6.getOldValue());
 
 		assertEquals("modelElements", mrSubOperation7.getFeatureName());
 		assertEquals(0, mrSubOperation7.getIndex());
 		assertEquals(sectionId, mrSubOperation7.getModelElementId());
 		assertEquals("leafSection", mrSubOperation7.getOppositeFeatureName());
 		assertEquals(true, mrSubOperation7.isAdd());
 		assertEquals(true, mrSubOperation7.isBidirectional());
 		Set<ModelElementId> otherInvolvedModelElements4 = mrSubOperation7.getOtherInvolvedModelElements();
 		assertEquals(1, otherInvolvedModelElements4.size());
 		EList<ModelElementId> referencedModelElements3 = mrSubOperation7.getReferencedModelElements();
 		assertEquals(1, referencedModelElements3.size());
 		assertEquals(useCaseId, referencedModelElements3.get(0));
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				reverse.apply(getProject());
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().contains(useCaseId));
 		assertEquals(true, getProject().containsInstance(oldActor));
 		assertEquals(true, getProject().containsInstance(newActor));
 		assertEquals(true, getProject().containsInstance(otherActor));
 		assertEquals(1, oldActor.getInitiatedUseCases().size());
 		assertEquals(1, newActor.getParticipatedUseCases().size());
 		assertEquals(1, otherActor.getParticipatedUseCases().size());
 		EObject useCaseClone = getProject().getModelElement(useCaseId);
 		assertEquals(useCaseClone, oldActor.getInitiatedUseCases().get(0));
 		assertEquals(useCaseClone, newActor.getParticipatedUseCases().get(0));
 		assertEquals(useCaseClone, otherActor.getParticipatedUseCases().get(0));
 
 		Project loadedProject = ModelUtil.loadEObjectFromResource(
 			org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE.getModelPackage().getProject(), getProject()
 				.eResource().getURI(), false);
 
 		assertTrue(ModelUtil.areEqual(loadedProject, getProject()));
 		assertEquals(true, loadedProject.contains(useCaseId));
 		assertEquals(true, loadedProject.contains(oldActorId));
 		assertEquals(true, loadedProject.contains(newActorId));
 		assertEquals(true, loadedProject.contains(otherActorId));
 	}
 
 	/**
 	 * check complex element deletion tracking.
 	 * 
 	 * @throws UnsupportedOperationException on test fail
 	 * @throws UnsupportedNotificationException on test fail
 	 * @throws IOException
 	 */
 	@Test
 	public void complexCreateTest() throws UnsupportedOperationException, UnsupportedNotificationException, IOException {
 		for (int i = 0; i < 10; i++) {
 			final CompositeSection createCompositeSection = DocumentFactory.eINSTANCE.createCompositeSection();
 			createCompositeSection.setName("Helmut" + i);
 			new EMFStoreCommand() {
 				@Override
 				protected void doRun() {
 					getProject().addModelElement(createCompositeSection);
 					LeafSection createLeafSection = DocumentFactory.eINSTANCE.createLeafSection();
 					createCompositeSection.getSubsections().add(createLeafSection);
 
 					for (int j = 0; j < 10; j++) {
 						ActionItem createActionItem = TaskFactory.eINSTANCE.createActionItem();
 						createActionItem.setName("Max tu dies" + j);
 						createLeafSection.getModelElements().add(createActionItem);
 					}
 				}
 			}.run(false);
 		}
 		assertEquals(230, getProjectSpace().getOperations().size());
 
 		((ProjectSpaceImpl) getProjectSpace()).saveProjectSpaceOnly();
 		ProjectSpace loadedProjectSpace = ModelUtil.loadEObjectFromResource(ModelPackage.eINSTANCE.getProjectSpace(),
 			getProjectSpace().eResource().getURI(), false);
 
 		assertTrue(ModelUtil.areEqual(getProjectSpace(), loadedProjectSpace));
 	}
 
 	/**
 	 * Delete a parent with a child contained in a single reference.
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void deleteWithSingleReferenceChildTest() throws IOException {
 		final Issue issue = RationaleFactory.eINSTANCE.createIssue();
 		final Solution solution = RationaleFactory.eINSTANCE.createSolution();
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				issue.setSolution(solution);
 				getProject().addModelElement(issue);
 
 				assertEquals(true, getProject().containsInstance(issue));
 				assertEquals(true, getProject().containsInstance(solution));
 				assertEquals(solution, issue.getSolution());
 				assertEquals(issue, solution.getIssue());
 
 				clearOperations();
 			}
 		}.run(false);
 
 		ModelElementId solutionId = ModelUtil.getProject(solution).getModelElementId(solution);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().deleteModelElement(solution);
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().containsInstance(issue));
 		assertEquals(false, getProject().containsInstance(solution));
 		assertEquals(null, issue.getSolution());
 		assertEquals(null, solution.getIssue());
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 		assertEquals(1, operations.size());
 		AbstractOperation operation = operations.get(0);
 		assertEquals(true, operation instanceof CreateDeleteOperation);
 		CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation;
 		assertEquals(true, createDeleteOperation.isDelete());
 
 		assertEquals(solutionId, createDeleteOperation.getModelElementId());
 
 		EList<ReferenceOperation> subOperations = createDeleteOperation.getSubOperations();
 		assertEquals(2, subOperations.size());
 
 		((ProjectSpaceImpl) getProjectSpace()).saveProjectSpaceOnly();
 		ProjectSpace loadedProjectSpace = ModelUtil.loadEObjectFromResource(ModelPackage.eINSTANCE.getProjectSpace(),
 			getProjectSpace().eResource().getURI(), false);
 
 		// perform asserts with loaded project space
 		assertTrue(ModelUtil.areEqual(getProjectSpace(), loadedProjectSpace));
 		operations = loadedProjectSpace.getOperations();
 		assertEquals(1, operations.size());
 		operation = operations.get(0);
 		assertEquals(true, operation instanceof CreateDeleteOperation);
 		createDeleteOperation = (CreateDeleteOperation) operation;
 		assertEquals(true, createDeleteOperation.isDelete());
 	}
 
 	/**
 	 * Test creation of element with cross references.
 	 * 
 	 * @throws UnsupportedOperationException on test fail
 	 * @throws UnsupportedNotificationException on test fail
 	 */
 	public void createWithCrossReferencesTest() throws UnsupportedOperationException, UnsupportedNotificationException {
 
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 		final UseCase useCase2 = RequirementFactory.eINSTANCE.createUseCase();
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(useCase2);
 				useCase.getIncludedUseCases().add(useCase2);
 				clearOperations();
 			}
 		}.run(false);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(useCase);
 				assertEquals(true, getProject().containsInstance(useCase));
 				assertEquals(true, getProject().containsInstance(useCase2));
 				assertEquals(1, getProjectSpace().getOperations().size());
 				assertEquals(true, getProjectSpace().getOperations().get(0) instanceof CreateDeleteOperation);
 				CreateDeleteOperation operation = (CreateDeleteOperation) getProjectSpace().getOperations().get(0);
 				assertEquals(getProject().getModelElementId(useCase), operation.getModelElementId());
 				assertEquals(2, operation.getSubOperations().size());
 				assertEquals(true, operation.getSubOperations().get(0) instanceof MultiReferenceOperation);
 			}
 		}.run(false);
 
 	}
 
 	/**
 	 * Test creating an element in a non project containment.
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void createInNonProjectContainmentTest() throws IOException {
 		final LeafSection section = DocumentFactory.eINSTANCE.createLeafSection();
 		final UseCase useCase = RequirementFactory.eINSTANCE.createUseCase();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(section);
 
 				assertEquals(true, getProject().containsInstance(section));
 
 				clearOperations();
 
 				section.getModelElements().add(useCase);
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().containsInstance(useCase));
 		assertEquals(true, getProject().containsInstance(section));
 		assertEquals(1, section.getModelElements().size());
 		assertEquals(section, useCase.getLeafSection());
 		assertEquals(useCase, section.getModelElements().iterator().next());
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 
 		assertEquals(2, operations.size());
 
 		AbstractOperation operation1 = operations.get(0);
 		AbstractOperation operation2 = operations.get(1);
 		assertEquals(true, operation1 instanceof CreateDeleteOperation);
 		assertEquals(true, operation2 instanceof MultiReferenceOperation);
 		CreateDeleteOperation createOperation = (CreateDeleteOperation) operation1;
 		MultiReferenceOperation multiReferenceOperation = (MultiReferenceOperation) operation2;
 		assertEquals(false, createOperation.isDelete());
 
 		ModelElementId useCaseId = ModelUtil.getProject(useCase).getModelElementId(useCase);
 		ModelElementId sectionId = ModelUtil.getProject(section).getModelElementId(section);
 
 		assertEquals(useCaseId, createOperation.getModelElementId());
 		assertEquals(sectionId, multiReferenceOperation.getModelElementId());
 
 		((ProjectSpaceImpl) getProjectSpace()).saveProjectSpaceOnly();
 		ProjectSpace loadedProjectSpace = ModelUtil.loadEObjectFromResource(ModelPackage.eINSTANCE.getProjectSpace(),
 			getProjectSpace().eResource().getURI(), false);
 
 		// perform asserts with loaded project space
 		assertTrue(ModelUtil.areEqual(getProjectSpace(), loadedProjectSpace));
 		operations = loadedProjectSpace.getOperations();
 		assertEquals(2, operations.size());
 
 		operation1 = operations.get(0);
 		operation2 = operations.get(1);
 		assertEquals(true, operation1 instanceof CreateDeleteOperation);
 		assertEquals(true, operation2 instanceof MultiReferenceOperation);
 		createOperation = (CreateDeleteOperation) operation1;
 		multiReferenceOperation = (MultiReferenceOperation) operation2;
 		assertEquals(false, createOperation.isDelete());
 
 	}
 
 	@Test
 	public void createEAttributes() throws IOException {
 		final EClass clazz = EcoreFactory.eINSTANCE.createEClass();
 		EStructuralFeature attribute = EcoreFactory.eINSTANCE.createEAttribute();
 		EStructuralFeature attribute2 = EcoreFactory.eINSTANCE.createEAttribute();
 		attribute.setName("attribute1");
 		attribute2.setName("attribute2");
 		clazz.getEStructuralFeatures().add(attribute);
 		clazz.getEStructuralFeatures().add(attribute2);
 
 		assertEquals(2, clazz.eContents().size());
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(clazz);
 			}
 		}.run(false);
 
 		((ProjectSpaceImpl) getProjectSpace()).saveProjectSpaceOnly();
 		ProjectSpace loadedProjectSpace = ModelUtil.loadEObjectFromResource(ModelPackage.eINSTANCE.getProjectSpace(),
 			getProjectSpace().eResource().getURI(), false);
 
 		// perform asserts with loaded project space
 		assertTrue(ModelUtil.areEqual(getProjectSpace(), loadedProjectSpace));
 	}
 
 	@Test
 	public void checkPersistentModelElementDeletion() throws IOException {
 		final EClass clazz = EcoreFactory.eINSTANCE.createEClass();
 		clazz.setName("clazz");
 		final EStructuralFeature attribute = EcoreFactory.eINSTANCE.createEAttribute();
 		final EStructuralFeature attribute2 = EcoreFactory.eINSTANCE.createEAttribute();
 		attribute.setName("attribute1");
 		attribute2.setName("attribute2");
 
 		clazz.getEStructuralFeatures().add(attribute2);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(attribute);
 				getProject().addModelElement(clazz);
 			}
 		}.run(false);
 
 		assertEquals(2, getProject().getModelElements().size()); // clazz, attribute
 		assertEquals(attribute, getProject().getModelElements().get(0));
 		assertEquals(clazz, getProject().getModelElements().get(1));
 		assertEquals(3, getProject().getAllModelElements().size()); // clazz, attribute, attribute2
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				clearOperations();
 			}
 		}.run(false);
 
 		// delete one ModelElement
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				EcoreUtil.delete(attribute);
 			}
 		}.run(false);
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 		assertEquals(1, operations.size()); // one delete operation
 		assertEquals(true, operations.get(0) instanceof CreateDeleteOperation);
 		CreateDeleteOperation operation = (CreateDeleteOperation) operations.get(0);
 		assertEquals(true, operation.isDelete());
 		// assertEquals(getProject().getDeletedModelElementId(attribute), operation.getModelElementId());
 
 		assertEquals(1, getProject().getModelElements().size()); // clazz
 		assertEquals(clazz, getProject().getModelElements().get(0));
 		assertEquals(2, getProject().getAllModelElements().size()); // clazz, attribute2
 
 		// load ProjectSpace from Resource and initialize
 		((ProjectSpaceImpl) getProjectSpace()).saveProjectSpaceOnly();
 		ProjectSpace loadedProjectSpace = ModelUtil.loadEObjectFromResource(ModelPackage.eINSTANCE.getProjectSpace(),
 			getProjectSpace().eResource().getURI(), false);
 		loadedProjectSpace.init();
 
 		// perform asserts with loaded project space
 		assertTrue(ModelUtil.areEqual(getProjectSpace(), loadedProjectSpace));
 		Project loadedProject = loadedProjectSpace.getProject();
 		assertEquals(1, loadedProject.getModelElements().size()); // clazz
 		assertEquals(getProject().getModelElementId(clazz),
 			loadedProject.getModelElementId(loadedProject.getModelElements().get(0)));
 		assertEquals(2, loadedProject.getAllModelElements().size()); // clazz, attribute2
 	}
 
 	@Test
 	public void testECoreUtilCopyWithMeetings() {
 		// create a meeting with composite and subsections including intra - cross references
 		CompositeMeetingSection compMeetingSection = MeetingFactory.eINSTANCE.createCompositeMeetingSection();
 		IssueMeetingSection issueMeeting = MeetingFactory.eINSTANCE.createIssueMeetingSection();
 		WorkItemMeetingSection workItemMeetingSecion = MeetingFactory.eINSTANCE.createWorkItemMeetingSection();
 		compMeetingSection.getSubsections().add(issueMeeting);
 		compMeetingSection.getSubsections().add(workItemMeetingSecion);
 		final Meeting meeting = MeetingFactory.eINSTANCE.createMeeting();
 		meeting.getSections().add(compMeetingSection);
 		meeting.setIdentifiedIssuesSection(issueMeeting);
 		meeting.setIdentifiedWorkItemsSection(workItemMeetingSecion);
 
 		// copy meeting and check if the intra cross references were actually copied
 		Meeting copiedMeeting = ModelUtil.clone(meeting);
 		assertFalse(copiedMeeting.getIdentifiedIssuesSection() == meeting.getIdentifiedIssuesSection());
 		assertFalse(copiedMeeting.getIdentifiedWorkItemsSection() == meeting.getIdentifiedWorkItemsSection());
 
 		// add original element to project
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(meeting);
 			}
 		}.run(false);
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 		assertEquals(1, operations.size());
 
 		AbstractOperation operation1 = operations.get(0);
 		assertTrue(operation1 instanceof CreateDeleteOperation);
 		CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation1;
 		assertFalse(createDeleteOperation.isDelete());
 
 		Meeting meetingSection = (Meeting) createDeleteOperation.getModelElement();
 		assertFalse(meeting.getIdentifiedIssuesSection() == meetingSection.getIdentifiedIssuesSection());
 
 	}
 
 	@Test
 	public void testCopyProject() {
 		final CompositeMeetingSection compMeetingSection = MeetingFactory.eINSTANCE.createCompositeMeetingSection();
 		IssueMeetingSection issueMeeting = MeetingFactory.eINSTANCE.createIssueMeetingSection();
 		WorkItemMeetingSection workItemMeetingSecion = MeetingFactory.eINSTANCE.createWorkItemMeetingSection();
 		compMeetingSection.getSubsections().add(issueMeeting);
 		compMeetingSection.getSubsections().add(workItemMeetingSecion);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(compMeetingSection);
 			}
 		}.run(false);
 
 		ModelElementId compMeetingSectionId = getProject().getModelElementId(compMeetingSection);
 
 		Project copiedProject = ((ProjectImpl) getProject()).copy();
 
 		EObject copiedMeetingSection = copiedProject.getModelElement(compMeetingSectionId);
 
 		assertNotNull(copiedMeetingSection);
 	}
 
 	@Test
 	public void testRemoveChildFromParentWithSplittedResource() throws IOException {
 		final CompositeSection compositeSection = DocumentFactory.eINSTANCE.createCompositeSection();
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(compositeSection);
 			}
 		}.run(false);
 
 		final LeafSection leafSection = DocumentFactory.eINSTANCE.createLeafSection();
 		ResourceSet rs = getProjectSpace().eResource().getResourceSet();
 		Resource compositeResource = compositeSection.eResource();
 		final Resource leafResource = rs.createResource(URI.createFileURI(compositeResource.getURI().toFileString()
 			+ "leaf"));
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				leafResource.getContents().add(leafSection);
 			}
 		}.run(true);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				compositeSection.getSubsections().add(leafSection);
 			}
 		}.run(false);
 
 		assertTrue(compositeSection.eResource() != leafSection.eResource());
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProject().deleteModelElement(compositeSection);
 			}
 		}.run(false);
 
 		assertTrue(getProject().getModelElements().size() == 0);
 		assertTrue(leafResource.getContents().size() == 0);
 	}
 
 	@Test
 	public void testCreateWithOneIngoingReference() {
 
 		final TestElement parentTestElement = getTestElement("parent");
 		final TestElement testElement = getTestElement("test1");
 		final TestElement testElement2 = getTestElement("test2");
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(parentTestElement);
 				testElement.getReferences().add(testElement2);
 				parentTestElement.getContainedElements().add(testElement);
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().containsInstance(parentTestElement));
 		assertEquals(getProject(), ModelUtil.getProject(parentTestElement));
 		assertEquals(true, getProject().containsInstance(testElement));
 		assertEquals(getProject(), ModelUtil.getProject(testElement));
 		assertEquals(testElement, parentTestElement.getContainedElements().get(0));
 		assertEquals(testElement2, testElement.getReferences().get(0));
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				clearOperations();
 			}
 		}.run(false);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				parentTestElement.getContainedElements().add(testElement2);
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().containsInstance(parentTestElement));
 		assertEquals(getProject(), ModelUtil.getProject(parentTestElement));
 		assertEquals(true, getProject().containsInstance(testElement));
 		assertEquals(getProject(), ModelUtil.getProject(testElement));
 		assertEquals(testElement, parentTestElement.getContainedElements().get(0));
 		assertEquals(testElement2, testElement.getReferences().get(0));
 
 		assertEquals(true, getProject().containsInstance(testElement2));
 		assertEquals(getProject(), ModelUtil.getProject(testElement2));
 		assertEquals(testElement2, parentTestElement.getContainedElements().get(1));
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 		assertEquals(2, operations.size());
 		AbstractOperation operation = operations.get(0);
 		assertEquals(true, operation instanceof CreateDeleteOperation);
 		CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation;
 
 		// ModelElementId testElementId = ModelUtil.getProject(testElement).getModelElementId(testElement);
 		ModelElementId testElement2Id = ModelUtil.getProject(testElement2).getModelElementId(testElement2);
 		// ModelElementId parentTestElementId = ModelUtil.getProject(parentTestElement).getModelElementId(
 		// parentTestElement);
 
 		assertEquals(testElement2Id, createDeleteOperation.getModelElementId());
 		assertEquals(1, createDeleteOperation.getSubOperations().size());
 		assertEquals(false, createDeleteOperation.isDelete());
 		assertTrue(CommonUtil.isSelfContained(createDeleteOperation, true));
 
 		MultiReferenceOperation subOperation1 = (MultiReferenceOperation) createDeleteOperation.getSubOperations().get(
 			0);
 
 		assertEquals(testElement, getProject().getModelElement(subOperation1.getModelElementId()));
 		assertEquals(testElement2, getProject().getModelElement(subOperation1.getReferencedModelElements().get(0)));
 
 		MultiReferenceOperation operation2 = (MultiReferenceOperation) operations.get(1);
 
 		assertEquals(parentTestElement, getProject().getModelElement(operation2.getModelElementId()));
 		assertEquals(testElement2, getProject().getModelElement(operation2.getReferencedModelElements().get(0)));
 	}
 
 	@Test
 	public void testCreateWithReferencesAndChildrenComplex() {
 
 		final TestElement parentTestElement = getTestElement("parentTestElement");
 		final TestElement testElement = getTestElement("testElement");
 		final TestElement newTestElement = getTestElement("newTestElement");
 		final TestElement newChildElement1 = getTestElement("newChildElement1");
 		final TestElement newChildElement2 = getTestElement("newChildElement2");
 		final TestElement newChildElement3 = getTestElement("newChildElement3");
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(parentTestElement);
 				parentTestElement.getContainedElements().add(testElement);
 				newTestElement.getContainedElements().add(newChildElement1);
 				newTestElement.getContainedElements().add(newChildElement2);
 				newTestElement.getContainedElements().add(newChildElement3);
 				newChildElement1.getReferences().add(newTestElement);
 				newChildElement2.getReferences().add(newChildElement1);
 				newChildElement2.getReferences().add(testElement);
 				testElement.getReferences().add(newChildElement3);
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().containsInstance(parentTestElement));
 		assertEquals(getProject(), ModelUtil.getProject(parentTestElement));
 		assertEquals(true, getProject().containsInstance(testElement));
 		assertEquals(getProject(), ModelUtil.getProject(testElement));
 		assertEquals(false, getProject().containsInstance(newTestElement));
 		assertEquals(false, getProject().containsInstance(newChildElement1));
 		assertEquals(false, getProject().containsInstance(newChildElement2));
 		assertEquals(false, getProject().containsInstance(newChildElement3));
 
 		assertEquals(3, newTestElement.getContainedElements().size());
 		assertEquals(newChildElement1, newTestElement.getContainedElements().get(0));
 		assertEquals(newChildElement2, newTestElement.getContainedElements().get(1));
 		assertEquals(newChildElement3, newTestElement.getContainedElements().get(2));
 
 		assertEquals(newTestElement, newChildElement1.getReferences().get(0));
 		assertEquals(newChildElement1, newChildElement2.getReferences().get(0));
 		assertEquals(testElement, newChildElement2.getReferences().get(1));
 		assertEquals(newChildElement3, testElement.getReferences().get(0));
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				clearOperations();
 			}
 		}.run(false);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				parentTestElement.getContainedElements().add(newTestElement);
 			}
 		}.run(false);
 
 		assertEquals(true, getProject().containsInstance(parentTestElement));
 		assertEquals(getProject(), ModelUtil.getProject(parentTestElement));
 		assertEquals(true, getProject().containsInstance(testElement));
 		assertEquals(getProject(), ModelUtil.getProject(testElement));
 		assertEquals(true, getProject().containsInstance(newTestElement));
 		assertEquals(true, getProject().containsInstance(newChildElement1));
 		assertEquals(true, getProject().containsInstance(newChildElement2));
 		assertEquals(true, getProject().containsInstance(newChildElement3));
 
 		assertEquals(3, newTestElement.getContainedElements().size());
 		assertEquals(newChildElement1, newTestElement.getContainedElements().get(0));
 		assertEquals(newChildElement2, newTestElement.getContainedElements().get(1));
 		assertEquals(newChildElement3, newTestElement.getContainedElements().get(2));
 
 		assertEquals(newTestElement, newChildElement1.getReferences().get(0));
 		assertEquals(newChildElement1, newChildElement2.getReferences().get(0));
 		assertEquals(testElement, newChildElement2.getReferences().get(1));
 		assertEquals(newChildElement3, testElement.getReferences().get(0));
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 		assertEquals(2, operations.size());
 		AbstractOperation operation = operations.get(0);
 		assertEquals(true, operation instanceof CreateDeleteOperation);
 		CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation;
 
 		ModelElementId newTestElementId = ModelUtil.getProject(newTestElement).getModelElementId(newTestElement);
 		TestElement copiedNewTestElement = (TestElement) createDeleteOperation.getModelElement();
 		TestElement copiedNewChildElement1 = copiedNewTestElement.getContainedElements().get(0);
 		TestElement copiedNewChildElement2 = copiedNewTestElement.getContainedElements().get(1);
 
 		assertEquals(3, copiedNewTestElement.getContainedElements().size());
 		assertEquals(copiedNewTestElement, copiedNewChildElement1.getReferences().get(0));
 		assertEquals(copiedNewChildElement1, copiedNewChildElement2.getReferences().get(0));
 		assertEquals(1, copiedNewChildElement2.getReferences().size());
 
 		assertEquals(newTestElementId, createDeleteOperation.getModelElementId());
 		assertEquals(2, createDeleteOperation.getSubOperations().size());
 		assertEquals(false, createDeleteOperation.isDelete());
 		assertTrue(CommonUtil.isSelfContained(createDeleteOperation, true));
 
 		// check sub-operations of 1st operation
 		MultiReferenceOperation subOperation1 = (MultiReferenceOperation) createDeleteOperation.getSubOperations().get(
 			0);
 
 		// sub-operation 1
 		assertEquals(newChildElement2, getProject().getModelElement(subOperation1.getModelElementId()));
 		assertEquals("references", subOperation1.getFeatureName());
 		assertEquals(testElement, getProject().getModelElement(subOperation1.getReferencedModelElements().get(0)));
 
 		MultiReferenceOperation subOperation2 = (MultiReferenceOperation) createDeleteOperation.getSubOperations().get(
 			1);
 
 		// sub-operation 2
 		assertEquals(testElement, getProject().getModelElement(subOperation2.getModelElementId()));
 		assertEquals("references", subOperation2.getFeatureName());
 		assertEquals(newChildElement3, getProject().getModelElement(subOperation2.getReferencedModelElements().get(0)));
 
 		// check 2nd operation
 		MultiReferenceOperation operation2 = (MultiReferenceOperation) operations.get(1);
 
 		assertEquals(parentTestElement, getProject().getModelElement(operation2.getModelElementId()));
 		assertEquals(newTestElement, getProject().getModelElement(operation2.getReferencedModelElements().get(0)));
 
 		assertEquals(true, operations.get(1) instanceof MultiReferenceOperation);
 		MultiReferenceOperation multiRefOp = (MultiReferenceOperation) operations.get(1);
 
 		assertEquals(parentTestElement, getProject().getModelElement(multiRefOp.getModelElementId()));
 		assertEquals("containedElements", multiRefOp.getFeatureName());
 		assertEquals(newTestElement, getProject().getModelElement(multiRefOp.getReferencedModelElements().get(0)));
 		assertEquals(true, multiRefOp.isAdd());
 	}
 
 	@Test
 	public void testClearContainmentTree() {
 		final TestElement parentTestElement = getTestElement("parentTestElement");
 		final TestElement testElement = getTestElement("testElement");
 		final TestElement subTestElement = getTestElement("subTestElement");
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(parentTestElement);
 				parentTestElement.getContainedElements().add(testElement);
 				testElement.getContainedElements().add(subTestElement);
 				clearOperations();
 			}
 		}.run(false);
 
 		ModelElementId parentElementId = getProject().getModelElementId(parentTestElement);
 		ModelElementId elementId = getProject().getModelElementId(testElement);
 		ModelElementId subElementId = getProject().getModelElementId(subTestElement);
 
 		assertNotNull(parentElementId);
 		assertNotNull(elementId);
 		assertNotNull(subElementId);
 
 		assertEquals(1, getProject().getModelElements().size());
 		assertEquals(parentTestElement, getProject().getModelElements().get(0));
 
 		assertEquals(1, parentTestElement.getContainedElements().size());
 		assertEquals(testElement, parentTestElement.getContainedElements().get(0));
 		assertEquals(1, testElement.getContainedElements().size());
 		assertEquals(subTestElement, testElement.getContainedElements().get(0));
 
 		assertEquals(getProject(), ModelUtil.getProject(parentTestElement));
 		assertEquals(getProject(), ModelUtil.getProject(testElement));
 		assertEquals(getProject(), ModelUtil.getProject(subTestElement));
 
 		assertEquals(true, getProject().containsInstance(parentTestElement));
 		assertEquals(true, getProject().containsInstance(testElement));
 		assertEquals(true, getProject().containsInstance(subTestElement));
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				testElement.getContainedElements().clear();
 				parentTestElement.getContainedElements().clear();
 			}
 		}.run(false);
 
 		List<AbstractOperation> operations = getProjectSpace().getOperations();
 		assertEquals(4, operations.size());
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProjectSpace().revert();
 			}
 		}.run(false);
 
 		assertEquals(1, getProject().getModelElements().size());
 		assertEquals(parentTestElement, getProject().getModelElements().get(0));
 
 		assertEquals(1, parentTestElement.getContainedElements().size());
 		TestElement copiedTestElement = parentTestElement.getContainedElements().get(0);
 		assertEquals(1, copiedTestElement.getContainedElements().size());
 		TestElement copiedSubTestElement = copiedTestElement.getContainedElements().get(0);
 
 		assertEquals(getProject(), ModelUtil.getProject(parentTestElement));
 		assertEquals(getProject(), ModelUtil.getProject(copiedTestElement));
 		assertEquals(getProject(), ModelUtil.getProject(copiedSubTestElement));
 
 		assertEquals(true, getProject().containsInstance(parentTestElement));
 		assertEquals(true, getProject().containsInstance(copiedTestElement));
 		assertEquals(true, getProject().containsInstance(copiedSubTestElement));
 
 		assertEquals(parentElementId, getProject().getModelElementId(parentTestElement));
 		assertEquals(elementId, getProject().getModelElementId(copiedTestElement));
 		assertEquals(subElementId, getProject().getModelElementId(copiedSubTestElement));
 	}
 
 	@Test
 	public void testClearContainmentTreeReverse() {
 		final TestElement parentTestElement = getTestElement("parentTestElement");
 		final TestElement testElement = getTestElement("testElement");
 		final TestElement subTestElement = getTestElement("subTestElement");
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(parentTestElement);
 				parentTestElement.getContainedElements().add(testElement);
 				testElement.getContainedElements().add(subTestElement);
 				clearOperations();
 			}
 		}.run(false);
 
 		ModelElementId parentElementId = getProject().getModelElementId(parentTestElement);
 		ModelElementId elementId = getProject().getModelElementId(testElement);
 		ModelElementId subElementId = getProject().getModelElementId(subTestElement);
 
 		assertNotNull(parentElementId);
 		assertNotNull(elementId);
 		assertNotNull(subElementId);
 
 		assertEquals(1, getProject().getModelElements().size());
 		assertEquals(parentTestElement, getProject().getModelElements().get(0));
 
 		assertEquals(1, parentTestElement.getContainedElements().size());
 		assertEquals(testElement, parentTestElement.getContainedElements().get(0));
 		assertEquals(1, testElement.getContainedElements().size());
 		assertEquals(subTestElement, testElement.getContainedElements().get(0));
 
 		assertEquals(getProject(), ModelUtil.getProject(parentTestElement));
 		assertEquals(getProject(), ModelUtil.getProject(testElement));
 		assertEquals(getProject(), ModelUtil.getProject(subTestElement));
 
 		assertEquals(true, getProject().containsInstance(parentTestElement));
 		assertEquals(true, getProject().containsInstance(testElement));
 		assertEquals(true, getProject().containsInstance(subTestElement));
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				parentTestElement.getContainedElements().clear();
 				testElement.getContainedElements().clear();
 			}
 		}.run(false);
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				getProjectSpace().revert();
 			}
 		}.run(false);
 
 		assertEquals(1, getProject().getModelElements().size());
 		assertEquals(parentTestElement, getProject().getModelElements().get(0));
 
 		assertEquals(1, parentTestElement.getContainedElements().size());
 		TestElement copiedTestElement = parentTestElement.getContainedElements().get(0);
 		assertEquals(1, copiedTestElement.getContainedElements().size());
 		TestElement copiedSubTestElement = copiedTestElement.getContainedElements().get(0);
 
 		assertEquals(getProject(), ModelUtil.getProject(parentTestElement));
 		assertEquals(getProject(), ModelUtil.getProject(copiedTestElement));
 		assertEquals(getProject(), ModelUtil.getProject(copiedSubTestElement));
 
 		assertEquals(true, getProject().containsInstance(parentTestElement));
 		assertEquals(true, getProject().containsInstance(copiedTestElement));
 		assertEquals(true, getProject().containsInstance(copiedSubTestElement));
 
 		assertEquals(parentElementId, getProject().getModelElementId(parentTestElement));
 		assertEquals(elementId, getProject().getModelElementId(copiedTestElement));
 		assertEquals(subElementId, getProject().getModelElementId(copiedSubTestElement));
 	}
 
 	// commenting out, too exotic to happen
 	/*
 	 * @Test public void createTreeAndAddNonRootToProject() { WorkPackage root =
 	 * TaskFactory.eINSTANCE.createWorkPackage(); WorkPackage child = TaskFactory.eINSTANCE.createWorkPackage();
 	 * WorkPackage existing = TaskFactory.eINSTANCE.createWorkPackage(); root.getContainedWorkItems().add(child);
 	 * getProject().getModelElements().add(existing); child.getContainedWorkItems().add(existing);
 	 * getProject().getModelElements().add(root); assertTrue(getProject().contains(child));
 	 * assertTrue(getProject().contains(root)); assertTrue(getProject().contains(existing)); assertSame(root,
 	 * child.getContainingWorkpackage()); assertSame(child, existing.getContainingWorkpackage());
 	 * assertEquals(getProject().getAllModelElements().size(), 3); }
 	 */
 }
