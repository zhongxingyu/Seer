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
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareEditorInput;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.compare.structuremergeviewer.IDiffElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareConstants;
 import org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.EMFCompareStructureMergeViewer;
 import org.eclipse.emf.compare.ide.ui.internal.util.EMFCompareEditingDomain;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorDocument;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.compare.utils.EMFCompareUtils;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.StructuralFeatureChangeStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.SynchronizerCompilationStatus;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * Proposal used to fix a Synchronization issue by opening the compare Editor.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class EMFCompareFix extends AbstractIntentFix {
 
 	private static final String COMPARE_EDITOR_TITLE = "Comparing Intent Document and Working Copy";
 
	private ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
 			ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param annotation
 	 *            the annotation describing the synchronization issue.
 	 */
 	public EMFCompareFix(Annotation annotation) {
 		super(annotation);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.editor.quickfix.AbstractIntentFix#applyFix(org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter,
 	 *      org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorDocument)
 	 */
 	@Override
 	protected void applyFix(RepositoryAdapter repositoryAdapter, IntentEditorDocument document) {
 		String workingCopyResourceURI = ((SynchronizerCompilationStatus)syncAnnotation.getCompilationStatus())
 				.getWorkingCopyResourceURI().replace("\"", "");
 		String generatedResourceURI = ((SynchronizerCompilationStatus)syncAnnotation.getCompilationStatus())
 				.getCompiledResourceURI().replace("\"", "");
 
 		// launch comparison
 		Resource generatedResource = repositoryAdapter.getResource(generatedResourceURI);
 		ResourceSetImpl rs = new ResourceSetImpl();
 		Resource workingCopyResource = rs.getResource(URI.createURI(workingCopyResourceURI), true);
 		final Comparison comparison = EMFCompareUtils.compare(generatedResource, workingCopyResource);
 
 		// prepare configuration & open dialog
 		final CompareConfiguration compareConfig = new IntentCompareConfiguration(generatedResource,
 				workingCopyResource);
 		compareConfig.setProperty(EMFCompareConstants.COMPARE_RESULT, comparison);
 		compareConfig.setProperty(EMFCompareConstants.EDITING_DOMAIN, new EMFCompareEditingDomain(comparison,
 				generatedResource, workingCopyResource, null));
 		CompareEditorInput input = new IntentCompareEditorInput(compareConfig, comparison);
 		compareConfig.setContainer(input);
 		input.setTitle(COMPARE_EDITOR_TITLE + " (" + workingCopyResourceURI + ")");
 		CompareUI.openCompareDialog(input);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
 	 */
 	public String getDisplayString() {
 		return "See differences in Compare Editor";
 	}
 
 	/**
 	 * A custom implementation of the editor input.
 	 */
 	class IntentCompareEditorInput extends CompareEditorInput {
 		private Object selection;
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param configuration
 		 *            the compare configuration
 		 * @param comparison
 		 *            the comparison
 		 */
 		public IntentCompareEditorInput(CompareConfiguration configuration, Comparison comparison) {
 			super(configuration);
 			this.selection = comparison;
 			CompilationStatus status = syncAnnotation.getCompilationStatus();
 			if (status instanceof StructuralFeatureChangeStatus) {
 				EObject element = ((StructuralFeatureChangeStatus)status).getCompiledElement();
 				Match match = comparison.getMatch(element);
 				if (match != null && !match.getDifferences().isEmpty()) {
 					// TODO improve, find a way to accurately rely status original difference with comparison
 					// (use message ?)
 					this.selection = match.getDifferences().get(0);
 				}
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
 		 */
 		@Override
 		protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException,
 				InterruptedException {
			return (IDiffElement)adapterFactory.adapt(this.selection, IDiffElement.class);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.compare.CompareEditorInput#createDiffViewer(org.eclipse.swt.widgets.Composite)
 		 */
 		@Override
 		public Viewer createDiffViewer(Composite parent) {
 			return new EMFCompareStructureMergeViewer(parent, getCompareConfiguration());
 		};
 	}
 }
