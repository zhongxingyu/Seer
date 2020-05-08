 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.editor;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareEditorInput;
 import org.eclipse.compare.CompareViewerPane;
 import org.eclipse.compare.Splitter;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.metamodel.ModelInputSnapshot;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.ui.ModelCompareInput;
 import org.eclipse.emf.compare.ui.contentmergeviewer.ModelContentMergeViewer;
 import org.eclipse.emf.compare.ui.structuremergeviewer.ModelStructureMergeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * This will be used as input for the {@link CompareEditor} used for the edition of 
  * emfdiff files.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class ModelCompareEditorInput extends CompareEditorInput {
 	private DiffModel diff;
 	private MatchModel match;
 	private ModelInputSnapshot inputSnapshot;
 	private ICompareInputChangeListener inputListener;
 	
 	private ModelStructureMergeViewer structureMergeViewer;
 	private ModelContentMergeViewer contentMergeViewer;
 	
 	/**
 	 * This constructor takes a {@link ModelInputSnapshot} as input.
 	 * 
 	 * @param snapshot
 	 * 			The {@link ModelInputSnapshot} loaded from an emfdiff.
 	 */
 	public ModelCompareEditorInput(ModelInputSnapshot snapshot) {
 		super(new CompareConfiguration());
 		diff = snapshot.getDiff();
 		match = snapshot.getMatch();
 		inputSnapshot = snapshot;
 		
 		inputListener = new ICompareInputChangeListener() {
 			public void compareInputChanged(ICompareInput source) {
 				structureMergeViewer.setInput(source);
 				contentMergeViewer.setInput(source);
 			}
 		};
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see CompareEditorInput#prepareInput(IProgreeMonitor)
 	 */
 	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 		final ModelCompareInput input = new ModelCompareInput(match, diff);
 		input.addCompareInputChangeListener(inputListener);
 		return input;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see CompareEditorInput#createContents(Composite)
 	 */
 	@Override
 	public Control createContents(Composite parent) {
 		final Splitter fComposite = new Splitter(parent, SWT.VERTICAL);
 
 		createOutlineContents(fComposite, SWT.HORIZONTAL);
 
 		final CompareViewerPane pane = new CompareViewerPane(fComposite, SWT.NONE);
 
 		contentMergeViewer = new ModelContentMergeViewer(pane, getCompareConfiguration());
 		pane.setContent(contentMergeViewer.getControl());
 
 		contentMergeViewer.setInput(inputSnapshot);
 		
 		final int structureWeight = 30;
 		final int contentWeight = 70;
 		fComposite.setWeights(new int[] {structureWeight, contentWeight});
 		
 		return fComposite;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see CompareEditorInput#createOutlineContents(Composite, int)
 	 */
 	@Override
 	public Control createOutlineContents(Composite parent, int direction) {
 		final Splitter splitter = new Splitter(parent, direction);
 
 		final CompareViewerPane pane = new CompareViewerPane(splitter, SWT.NONE);
 
 		structureMergeViewer = new ModelStructureMergeViewer(pane, getCompareConfiguration());
 		pane.setContent(structureMergeViewer.getTree());
 
 		structureMergeViewer.setInput(inputSnapshot);
 
 		return splitter;
 	}
 }
