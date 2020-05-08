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
 package org.eclipse.mylyn.docs.intent.client.synchronizer.launcher;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.SynchronizerRepositoryClient;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.listeners.GeneratedElementListener;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.IntentCommand;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.ReadWriteRepositoryObjectHandlerImpl;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.elementList.ElementListAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.elementList.ElementListNotificator;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 import org.eclipse.mylyn.docs.intent.collab.utils.RepositoryCreatorHolder;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerFactory;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndex;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndexEntry;
 
 /**
  * Creates a Synchronizer client.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public final class SynchronizerCreator {
 
 	/**
 	 * SynchronizerCreator constructor.
 	 */
 	private SynchronizerCreator() {
 
 	}
 
 	/**
 	 * Creates a Synchronizer client.
 	 * 
 	 * @param repository
 	 *            is the repository containing the generated models to synchronize
 	 * @param generatedElementListener
 	 *            a listener on the generated elements
 	 * @return the created Synchronizer client
 	 * @throws RepositoryConnectionException
 	 *             if a connection to the given repository cannot be established
 	 */
 	public static SynchronizerRepositoryClient createSynchronizer(Repository repository,
 			GeneratedElementListener generatedElementListener) throws RepositoryConnectionException {
 
 		// Step 1 : we initialize the listened elements
 		final RepositoryAdapter repositoryAdapter = RepositoryCreatorHolder.getCreator()
 				.createRepositoryAdapterForRepository(repository);
 		Set<EObject> listenedElements = new HashSet<EObject>();
 
 		repositoryAdapter.openReadOnlyContext();
 		final Resource traceabilityResource = repositoryAdapter
 				.getResource(IntentLocations.TRACEABILITY_INFOS_INDEX_PATH);
 		if (traceabilityResource.getContents().isEmpty()) {
 			repositoryAdapter.execute(new IntentCommand() {
 
 				public void execute() {
 					traceabilityResource.getContents().add(
 							CompilerFactory.eINSTANCE.createTraceabilityIndex());
 
 				}
 			});
 		}
 		EObject traceabilityIndex = traceabilityResource.getContents().get(0);
 
 		listenedElements.add(traceabilityIndex);
 		// Step 2 : create the adapter and the handler for these types
 
 		RepositoryObjectHandler handler = new ReadWriteRepositoryObjectHandlerImpl(repositoryAdapter);
 
 		ElementListAdapter adapter = new ElementListAdapter();
 
 		Notificator listenedElementsNotificator = new ElementListNotificator(listenedElements, adapter);
 		handler.setNotificator(listenedElementsNotificator);
 
 		// Step 3 : create the synchronizer
 		SynchronizerRepositoryClient synchronizerClient = new SynchronizerRepositoryClient(
 				(TraceabilityIndex)traceabilityIndex);
 		synchronizerClient.addRepositoryObjectHandler(handler);
 		synchronizerClient.setGeneratedElementListener(generatedElementListener);
 
 		// Step 4 : we ask the generatedElementListener to listen to all generated resources
 		Iterator<TraceabilityIndexEntry> indexEntryIterator = ((TraceabilityIndex)traceabilityIndex)
 				.getEntries().iterator();
 		while (indexEntryIterator.hasNext()) {
 			TraceabilityIndexEntry indexEntry = indexEntryIterator.next();
			String resourceURI = indexEntry.getResourceDeclaration().getUri().toString();
			if (resourceURI != null) {
				generatedElementListener.addElementToListen(URI.createURI(resourceURI));
			}
 		}
 
 		// We don't need to launch the synchronizer until the change of the compiler's generatedElement index.
 		// TODO REMOVE THIS CALL
 		repositoryAdapter.closeContext();
 		return synchronizerClient;
 	}
 }
