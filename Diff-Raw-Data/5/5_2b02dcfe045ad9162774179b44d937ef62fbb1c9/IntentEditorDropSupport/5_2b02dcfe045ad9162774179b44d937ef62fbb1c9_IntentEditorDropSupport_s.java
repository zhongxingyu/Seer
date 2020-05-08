 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.editor;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IIntentLogger.LogType;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IntentLogger;
 import org.eclipse.mylyn.docs.intent.collab.common.query.CompilationStatusQuery;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.ModelElementChangeStatus;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;
 import org.eclipse.mylyn.docs.intent.modelingunit.update.ModelingUnitUpdater;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 
 /**
  * Implementation of the drop support in intent editor.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IntentEditorDropSupport extends DropTargetAdapter {
 
 	private IntentEditor editor;
 
 	private IntentEditorDocument document;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param editor
 	 *            the intent editor
 	 */
 	public IntentEditorDropSupport(IntentEditor editor) {
 		this.editor = editor;
 		this.document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(
 				editor.getEditorInput());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.swt.dnd.DropTargetAdapter#drop(org.eclipse.swt.dnd.DropTargetEvent)
 	 */
 	public void drop(DropTargetEvent event) {
 		if (event.data instanceof IStructuredSelection) {
 			// TODO support resource drop ?
 			List<EObject> droppedEObjects = new ArrayList<EObject>();
 			for (Iterator<?> iterator = ((IStructuredSelection)event.data).iterator(); iterator.hasNext();) {
 				Object data = iterator.next();
 				if (data instanceof EObject) {
 					droppedEObjects.add((EObject)data);
 				}
 			}
 
 			EObject intentElement = document.getElementAtOffset(editor.getProjectionViewer().getTextWidget()
 					.getCaretOffset());
 
 			// TODO accurate drop, currently parent modeling unit lookup
 			// TODO we should be able to create a new modeling unit
			EObject parent = intentElement.eContainer();
 			while (parent != null && !(parent instanceof ModelingUnit)) {
 				parent = parent.eContainer();
 			}
			if (parent != null) {
 				updateModelingUnit((ModelingUnit)parent, droppedEObjects);
 			} else {
 				// TODO manage modeling units creation
 				IntentLogger.getInstance().log(LogType.ERROR, "Only modeling units support drops.");
 			}
 
 		}
 	}
 
 	/**
 	 * Updates the given modeling unit with the addition of the given objects.
 	 * 
 	 * @param modelingUnit
 	 *            the modeling unit to update
 	 * @param elements
 	 *            the elements to add or update
 	 */
 	private void updateModelingUnit(final ModelingUnit modelingUnit, List<EObject> elements) {
 		// computes the elements uris
 		List<String> uriFragments = new ArrayList<String>(elements.size());
 		for (EObject eObject : elements) {
 			uriFragments.add(EcoreUtil.getURI(eObject).toString());
 		}
 
 		IntentDocumentProvider documentProvider = (IntentDocumentProvider)editor.getDocumentProvider();
 		final RepositoryAdapter repositoryAdapter = documentProvider.getListenedElementsHandler()
 				.getRepositoryAdapter();
 		ModelingUnitUpdater updater = new ModelingUnitUpdater(repositoryAdapter);
 
 		boolean astChanged = false;
 		CompilationStatusQuery query = new CompilationStatusQuery(repositoryAdapter);
 		for (CompilationStatus compilationStatus : query.getOrCreateCompilationStatusManager()
 				.getCompilationStatusList()) {
 			if (compilationStatus instanceof ModelElementChangeStatus) {
 				ModelElementChangeStatus status = (ModelElementChangeStatus)compilationStatus;
 				if (uriFragments.contains(status.getWorkingCopyElementURIFragment())) {
 					updater.fixSynchronizationStatus(modelingUnit, status);
 					astChanged = true;
 				}
 			}
 		}
 
 		// finally, reload the document if necessary
 		if (astChanged) {
 			document.reloadFromAST();
 		}
 	}
 }
