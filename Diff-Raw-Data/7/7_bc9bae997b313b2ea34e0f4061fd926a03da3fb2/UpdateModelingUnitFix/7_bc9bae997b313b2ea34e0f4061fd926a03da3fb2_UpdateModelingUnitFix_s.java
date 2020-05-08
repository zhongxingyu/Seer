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
 package org.eclipse.mylyn.docs.intent.client.ui.editor.quickfix;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorDocument;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.IntentCommand;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.SaveException;
 import org.eclipse.mylyn.docs.intent.core.compiler.SynchronizerCompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ContributionInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;
 import org.eclipse.mylyn.docs.intent.modelingunit.gen.ModelingUnitGenerator;
 
 /**
  * Proposal used to fix a Synchronization issue by opening the compare Editor.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class UpdateModelingUnitFix extends AbstractIntentFix {
 
 	/**
 	 * The root eObject to generate.
 	 */
 	private EObject root;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param annotation
 	 *            the annotation describing the synchronization issue.
 	 */
 	public UpdateModelingUnitFix(Annotation annotation) {
 		super(annotation);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.editor.quickfix.AbstractIntentFix#applyFix(org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter,
 	 *      org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorDocument)
 	 */
 	@Override
 	protected void applyFix(final RepositoryAdapter repositoryAdapter, IntentEditorDocument document) {
 		EObject target = syncAnnotation.getCompilationStatus().getTarget();
 		if (target instanceof InstanciationInstruction) {
 			InstanciationInstruction instanciation = (InstanciationInstruction)target;
 
 			while (target != null && !(target instanceof ModelingUnit)) {
 				target = target.eContainer();
 			}
 
 			if (target != null) {
 				// generates the addition
 
 				final ModelingUnit container = (ModelingUnit)target;
 				ModelingUnitGenerator generator = new ModelingUnitGenerator();
 
 				final ContributionInstruction contribution = generator.generateContribution(instanciation,
 						getRootEObjectToGenerate());
 
 				repositoryAdapter.execute(new IntentCommand() {
 
 					public void execute() {
 						try {
							repositoryAdapter.openSaveContext();
 							container.getInstructions().add(contribution);

 							repositoryAdapter.save();
 						} catch (ReadOnlyException e) {
 							IntentUiLogger.logError(e);
 						} catch (SaveException e) {
 							IntentUiLogger.logError(e);
 						}
						repositoryAdapter.closeContext();
 					}
 
 				});
 
				repositoryAdapter.closeContext();
 				((IntentEditorDocument)document).reloadFromAST();
 			}
 		}
 	}
 
 	/**
 	 * Returns the root EObject to generate.
 	 * 
 	 * @return the root EObject to generate
 	 */
 	private EObject getRootEObjectToGenerate() {
 		if (root == null) {
 			SynchronizerCompilationStatus synchronizerCompilationStatus = (SynchronizerCompilationStatus)syncAnnotation
 					.getCompilationStatus();
 			String workingCopyResourceURI = synchronizerCompilationStatus.getWorkingCopyResourceURI()
 					.replace("\"", "");
 			ResourceSetImpl rs = new ResourceSetImpl();
 			Resource workingCopyResource = rs.getResource(URI.createURI(workingCopyResourceURI), true);
 			String workingCopyElementURIFragment = synchronizerCompilationStatus
 					.getWorkingCopyElementURIFragment().replace("\"", "");
 			root = workingCopyResource.getEObject(workingCopyElementURIFragment);
 		}
 		return root;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
 	 */
 	public String getDisplayString() {
 		return "Update modeling unit by generating missing " + getRootEObjectToGenerate().eClass().getName()
 				+ " element (experimental)";
 	}
 
 }
