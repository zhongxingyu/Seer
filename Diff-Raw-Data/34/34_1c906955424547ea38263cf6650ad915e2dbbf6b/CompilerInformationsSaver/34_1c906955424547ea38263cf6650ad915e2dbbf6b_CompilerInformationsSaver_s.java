 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.compiler.saver;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.cdo.util.ObjectNotFoundException;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.client.compiler.utils.IntentCompilerInformationHolder;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationMessageType;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusManager;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusSeverity;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerFactory;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndex;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndexEntry;
 import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
 import org.eclipse.mylyn.docs.intent.core.genericunit.UnitInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ResourceDeclaration;
 
 /**
  * Save all the compilation informations on the repository.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class CompilerInformationsSaver {
 
 	private Map<ResourceDeclaration, Map<EObject, IntentGenericElement>> resourceToTraceabilityElementIndexEntry;
 
 	/**
 	 * Progress monitor allowing to cancel a save operation if another notification has been received by the
 	 * CompilerRepositoryClient.
 	 */
 	private IProgressMonitor progressMonitor;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param progressMonitor
 	 *            the progress monitor to use
 	 */
 	public CompilerInformationsSaver(IProgressMonitor progressMonitor) {
 		this.progressMonitor = progressMonitor;
 	}
 
 	/**
 	 * Save the useful informations contained in the given IntentCompilerInformationHolder on the repository,
 	 * merging with the current repository content.
 	 * 
 	 * @param informationHolder
 	 *            the informationHolder containing the informations to put on the repository
 	 * @param handler
 	 *            the repositoryObjectHandler to use for communicating with the repository
 	 */
 	public void saveOnRepository(IntentCompilerInformationHolder informationHolder,
 			RepositoryObjectHandler handler) {
 		resourceToTraceabilityElementIndexEntry = new HashMap<ResourceDeclaration, Map<EObject, IntentGenericElement>>();
 		try {
 
 			// We first save the generated elements
 			if (!progressMonitor.isCanceled()) {
 				Map<ResourceDeclaration, String> resourceToGeneratedPath = saveGeneratedResources(
 						informationHolder, handler);
 
 				// We also save the status informations.
 				if (!progressMonitor.isCanceled()) {
 					saveStatusInformations(informationHolder, handler);
 				}
 
 				// We finally save the traceability informations
 				if (!progressMonitor.isCanceled()) {
 					saveTraceabilityInformations(resourceToGeneratedPath, handler);
 				}
 			}
 			// CHECKSTYLE:OFF : for now on we would like to print any exception
 		} catch (Exception e) {
 			e.printStackTrace();
 			try {
 				handler.getRepositoryAdapter().undo();
 			} catch (ReadOnlyException e1) {
 				// FIXME handle this exception
 			}
 		}
 	}
 
 	/**
 	 * Save all the generated elements into internal Repository resources.
 	 * 
 	 * @param informationHolder
 	 *            the informationHolder containing the information to save
 	 * @param handler
 	 *            the handler to use for saving on the repository
 	 * @return a mapping between each resource declaration and the path where it has been saved.
 	 * @throws ReadOnlyException
 	 *             if the current context is read-only
 	 */
 	private Map<ResourceDeclaration, String> saveGeneratedResources(
 			IntentCompilerInformationHolder informationHolder, RepositoryObjectHandler handler)
 			throws ReadOnlyException {
 
 		Map<ResourceDeclaration, String> resourceInfos = new HashMap<ResourceDeclaration, String>();
 		// We save each generated resource in a CDOResource
 		for (ResourceDeclaration resource : informationHolder.getDeclaredResources()) {
 			if (!progressMonitor.isCanceled()) {
 				String internalResourcePath = getInternalResourcePath(resource);
 				if (internalResourcePath != null) {
 					Resource generatedResource = handler.getRepositoryAdapter().getOrCreateResource(
 							internalResourcePath);
 					resourceInfos.put(resource, internalResourcePath);
 					generatedResource.getContents().clear();
 
 					// We update the resourceToTraceabilityElementIndexEntry map using this resource content
 					updateTraceabilityFromResourceContent(resource, informationHolder,
 							informationHolder.getResourceContent(resource));
 					generatedResource.getContents().addAll(informationHolder.getResourceContent(resource));
 				}
 			}
 		}
 		return resourceInfos;
 	}
 
 	/**
 	 * Associate the given resource with a mapping associating its contained compiled element with the
 	 * instruction that created those elements.
 	 * 
 	 * @param resource
 	 *            the resource to consider
 	 * @param informationHolder
 	 *            the informationHolder containing the informations needed
 	 * @param elementsToConsider
 	 *            a list of generated element contained in the given resource
 	 */
 	private void updateTraceabilityFromResourceContent(ResourceDeclaration resource,
 			IntentCompilerInformationHolder informationHolder, EList<EObject> elementsToConsider) {
 
 		// For each element contained in the resource
 		for (EObject element : elementsToConsider) {
 			if (!progressMonitor.isCanceled()) {
 				// We get the instruction that defined this element
 				UnitInstruction instruction = informationHolder.getInstructionByCreatedElement(element);
 
 				if (instruction != null) {
 					// We add an entry to the traceability map
 					if (resourceToTraceabilityElementIndexEntry.get(resource) == null) {
 						resourceToTraceabilityElementIndexEntry.put(resource,
 								new HashMap<EObject, IntentGenericElement>());
 					}
 					resourceToTraceabilityElementIndexEntry.get(resource).put(element, instruction);
 
 					// We do the same for each contained element
 					updateTraceabilityFromResourceContent(resource, informationHolder, element.eContents());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Save all the status informations related to the generated elements.
 	 * 
 	 * @param informationHolder
 	 *            the informationHolder containing the information to save
 	 * @param handler
 	 *            the handler to use for saving on the repository
 	 * @throws ReadOnlyException
 	 *             if the current context is read-only
 	 */
 	private void saveStatusInformations(IntentCompilerInformationHolder informationHolder,
 			RepositoryObjectHandler handler) throws ReadOnlyException {
 		Resource resourceForCompilationStatusList = handler.getRepositoryAdapter().getOrCreateResource(
 				IntentLocations.COMPILATION_STATUS_INDEX_PATH);
 
 		if (resourceForCompilationStatusList.getContents().isEmpty()) {
 			resourceForCompilationStatusList.getContents().add(
 					CompilerFactory.eINSTANCE.createCompilationStatusManager());
 		}
 		CompilationStatusManager manager = (CompilationStatusManager)resourceForCompilationStatusList
 				.getContents().get(0);
 		if (!progressMonitor.isCanceled()) {
 			mergeCompilationStatusManager(informationHolder.getStatusManager(), manager,
 					resourceForCompilationStatusList);
 		}
 	}
 
 	/**
 	 * Save all the traceability informations.
 	 * 
 	 * @param resourceToGeneratedPath
 	 *            the list of the resourceDeclaration associate to the internal path where it has been
 	 *            generated
 	 * @param handler
 	 *            the handler to use for saving on the repository
 	 * @throws ReadOnlyException
 	 *             if the current context is read-only
 	 */
 	private void saveTraceabilityInformations(Map<ResourceDeclaration, String> resourceToGeneratedPath,
 			RepositoryObjectHandler handler) throws ReadOnlyException {
 
 		// We first get the Traceability index
 		final Resource traceabilityResource = handler.getRepositoryAdapter().getResource(
 				IntentLocations.TRACEABILITY_INFOS_INDEX_PATH);
 
 		if (traceabilityResource.getContents().isEmpty()) {
 			traceabilityResource.getContents().add(CompilerFactory.eINSTANCE.createTraceabilityIndex());
 		}
 		TraceabilityIndex traceIndex = (TraceabilityIndex)traceabilityResource.getContents().get(0);
 
 		List<TraceabilityIndexEntry> newTraceabilityEntries = new ArrayList<TraceabilityIndexEntry>();
 		// For each compiled resource
 		for (ResourceDeclaration resourceDeclaration : resourceToGeneratedPath.keySet()) {
 			// We create a tracability entry
 			TraceabilityIndexEntry entry = CompilerFactory.eINSTANCE.createTraceabilityIndexEntry();
 			entry.setCompilationTime(BigInteger.valueOf(System.currentTimeMillis()));
 			entry.setGeneratedResourcePath(resourceToGeneratedPath.get(resourceDeclaration));
 			entry.setResourceDeclaration(resourceDeclaration);
 
 			// For each entry, we define a mapping between contained elements and instructions
 			entry.getContainedElementToInstructions().putAll(
 					resourceToTraceabilityElementIndexEntry.get(resourceDeclaration));
 			newTraceabilityEntries.add(entry);
 		}
 		traceIndex.getEntries().clear();
 		traceIndex.getEntries().addAll(newTraceabilityEntries);
 	}
 
 	/**
 	 * Returns the internal path corresponding to the given resource (i.e the path where the generated
 	 * resource will be located).
 	 * 
 	 * @param resource
 	 *            is the resource
 	 * @return the internal path corresponding to the given resource (i.e the path where the generated
 	 *         resource will be located)
 	 */
 	private String getInternalResourcePath(ResourceDeclaration resource) {
 		String resourcePath = null;
 		// If the resource is not associated to any URI
 		if (resource.getUri() == null) {
 			// We try to use its name to compute the internal path
 			String resourceName = resource.getName();
 			if (resourceName != null && resourceName.length() > 0) {
 				resourcePath = resourceName;
 			} else {
 				// We add an error and return a sample name
 				CompilationStatus status = CompilerFactory.eINSTANCE.createCompilationStatus();
 				status.setSeverity(CompilationStatusSeverity.ERROR);
 				status.setTarget(resource);
 				status.setType(CompilationMessageType.VALIDATION_ERROR);
 				status.setMessage("As this resource has no URI, it should have a name to be identifed.");
 				resource.getCompilationStatus().add(status);
 				return "unnamed-resource.xmi";
 			}
 		} else {
 			// Here a concrete URI has been associated to this resource
 			resourcePath = ((String)resource.getUri()).replace("\"", "");
 			// we get the last Segment of this URI to compute the internal path
 			if (resourcePath.contains("/")) {
 				resourcePath = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
 			}
 		}
 		resourcePath = IntentLocations.GENERATED_RESOURCES_FOLDER_PATH + resourcePath;
 		return resourcePath;
 	}
 
 	/**
 	 * Returns true if the given compilationSatus is contained in the given CompilationStatus list (a status
 	 * is contained in a status list if this list has a status with the same target, type and message).
 	 * 
 	 * @param statusList
 	 *            the list of status to inspect
 	 * @param status
 	 *            the status which we want to know wether is contained in this list or not
 	 * @return true if the given status is contained in the given list, false otherwise
 	 */
 	private boolean isContainedCompilationStatus(EList<CompilationStatus> statusList, CompilationStatus status) {
 		boolean statusIsContainedInList = false;
 		if (statusList != null) {
 			Iterator<CompilationStatus> iterator = statusList.iterator();
 			while (iterator.hasNext() && !statusIsContainedInList) {
 				CompilationStatus containedStatus = iterator.next();
 				statusIsContainedInList = isSimilarStatus(containedStatus, status);
 			}
 		}
 		return statusIsContainedInList;
 	}
 
 	/**
 	 * Indicates if the two given status have the same value for each attribute.
 	 * 
 	 * @param containedStatus
 	 *            the first status to compare with the second one
 	 * @param status
 	 *            the second status to compare with the first one
 	 * @return true if the two given status have the same value for each attribute, false otherwise.
 	 */
 	private boolean isSimilarStatus(CompilationStatus containedStatus, CompilationStatus status) {
 		try {
 			// FIXME do not use cdoID but repositoryAdapter.getIDForElement
 			return containedStatus.getMessage().equals(status.getMessage())
 					&& containedStatus.getSeverity().equals(status.getSeverity())
 					&& containedStatus.getTarget().cdoID() == status.getTarget().cdoID()
 					&& containedStatus.getType().equals(status.getType());
 		} catch (NullPointerException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Merge the local statusManager with the repository statusManager.
 	 * 
 	 * @param localStatusManager
 	 *            the local statusManager
 	 * @param repositoryStatusManager
 	 *            the repository StatusManager
 	 * @param statusManagerResource
 	 *            the resource containing the repositoryStatusManager
 	 */
 	private void mergeCompilationStatusManager(CompilationStatusManager localStatusManager,
 			CompilationStatusManager repositoryStatusManager, Resource statusManagerResource) {
 
 		// Step 1 : Cleaning repositoryRootModel
 		// Step 1.1 : removing dangling references
 		removeDanglingReferences(repositoryStatusManager, statusManagerResource);
 
 		List<IntentGenericElement> genEl = new ArrayList<IntentGenericElement>();
 		for (CompilationStatus compilationStatus : repositoryStatusManager.getCompilationStatusList()) {
			// System.err.println(compilationStatus.getTarget());
 			genEl.add(compilationStatus.getTarget());
 		}
 
 		List<EObject> cleanTargets = new ArrayList<EObject>();
 		// Step 2 : adding all the new compilation status
 		for (ModelingUnit mu : localStatusManager.getModelingUnitToStatusList().keySet()) {
 			for (CompilationStatus status : localStatusManager.getModelingUnitToStatusList().get(mu)) {
 				if (!progressMonitor.isCanceled()) {
 					if (!isContainedCompilationStatus(repositoryStatusManager.getModelingUnitToStatusList()
 							.get(mu), status)) {
 
 						if (!repositoryStatusManager.getCompilationStatusList().contains(status)) {
 							repositoryStatusManager.getCompilationStatusList().add(status);
 							if (!status.getTarget().getCompilationStatus().contains(status)) {
 								if (!cleanTargets.contains(status.getTarget())) {
 									cleanTargets.add(status.getTarget());
 									status.getTarget().getCompilationStatus().clear();
 								}
 								status.getTarget().getCompilationStatus().add(status);
 							}
 						}
 						if (repositoryStatusManager.getModelingUnitToStatusList().get(mu) == null) {
 							repositoryStatusManager.getModelingUnitToStatusList().put(mu,
 									new BasicEList<CompilationStatus>());
 						}
 						try {
 							repositoryStatusManager.getModelingUnitToStatusList().get(mu).add(status);
 						} catch (NullPointerException e) {
 							// TODO remove this catch, only for test
 							e.printStackTrace();
 						}
 
 					}
 				}
 			}
 		}
 
 		genEl = new ArrayList<IntentGenericElement>();
 		for (CompilationStatus compilationStatus : repositoryStatusManager.getCompilationStatusList()) {
 			genEl.add(compilationStatus.getTarget());
 
 		}
 	}
 
 	/**
 	 * Removes all the dangling references contained in the given CompilationStatusManger.
 	 * 
 	 * @param repositoryStatusManager
 	 *            the compilationSatusManager from which remove the dangling references
 	 * @param statusManagerResource
 	 *            the resource containing the repositoryStatusManager
 	 * @return the modified CompilationStatusManager
 	 */
 	private CompilationStatusManager removeDanglingReferences(
 			CompilationStatusManager repositoryStatusManager, Resource statusManagerResource) {
 		CompilationStatusManager manager = repositoryStatusManager;
 		try {
 			manager.getCompilationStatusList().clear();
 			manager.getModelingUnitToStatusList().clear();
 		} catch (ObjectNotFoundException notFoundException) {
 			statusManagerResource.getContents().clear();
 			manager = CompilerFactory.eINSTANCE.createCompilationStatusManager();
 			statusManagerResource.getContents().add(manager);
 		}
 		return manager;
 	}
 
 }
