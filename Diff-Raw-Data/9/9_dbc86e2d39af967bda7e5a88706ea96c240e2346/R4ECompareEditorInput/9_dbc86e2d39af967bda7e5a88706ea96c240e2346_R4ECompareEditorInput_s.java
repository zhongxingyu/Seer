 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, explicitThisUsage
 /*******************************************************************************
  * Copyright (c) 2010, 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class is used as the input class that feeds the eclipse compare
  * editor
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.editors;
 
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.compare.CompareViewerPane;
 import org.eclipse.compare.ICompareNavigator;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.internal.CompareContentViewerSwitchingPane;
 import org.eclipse.compare.internal.CompareEditorInputNavigator;
 import org.eclipse.compare.structuremergeviewer.Differencer;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotationModel;
 import org.eclipse.mylyn.reviews.frame.ui.annotation.IReviewAnnotationSupport;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.annotation.commands.R4EAnnotationContributionItems;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.annotation.content.R4EAnnotation;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.annotation.content.R4EAnnotationModel;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIContent;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.team.ui.synchronize.SaveableCompareEditorInput;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class R4ECompareEditorInput extends SaveableCompareEditorInput {
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fConfig - the compare configuration
 	 */
 	private final CompareConfiguration fConfig;
 
 	/**
 	 * Field fAncestor - the optional element that will appear on the top of the compare editor
 	 */
 	private final ITypedElement fAncestor;
 
 	/**
 	 * Field fLeft - the element that will appear on the left side of the compare editor
 	 */
 	private final ITypedElement fLeft;
 
 	/**
 	 * Field fRight - the element that will appear on the right side of the compare editor
 	 */
 	private final ITypedElement fRight;
 
 	/**
 	 * Field fAnnotationSupport.
 	 */
 	private IReviewAnnotationSupport fAnnotationSupport = null;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4ECompareEditorInput.
 	 * 
 	 * @param aConfig
 	 *            CompareConfiguration
 	 * @param aAncestor
 	 *            ITypedElement
 	 * @param aLeft
 	 *            ITypedElement
 	 * @param aRight
 	 *            ITypedElement
 	 */
 	public R4ECompareEditorInput(CompareConfiguration aConfig, ITypedElement aAncestor, ITypedElement aLeft,
 			ITypedElement aRight) {
 		super(aConfig, null);
 		fConfig = aConfig;
 		fAncestor = aAncestor;
 		fLeft = aLeft;
 		fRight = aRight;
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method getAncestorElement.
 	 * 
 	 * @return ITypedElement
 	 */
 	public ITypedElement getAncestorElement() {
 		return fAncestor;
 	}
 
 	/**
 	 * Method getLeftElement.
 	 * 
 	 * @return ITypedElement
 	 */
 	public ITypedElement getLeftElement() {
 		return fLeft;
 	}
 
 	/**
 	 * Method getRightElement.
 	 * 
 	 * @return ITypedElement
 	 */
 	public ITypedElement getRightElement() {
 		return fRight;
 	}
 
 	/**
 	 * Method prepareCompareInputNoEditor.
 	 */
 	public void prepareCompareInputNoEditor() {
 		//Build the diff node to compare the files		
 		final Differencer differencer = new Differencer();
 		differencer.findDifferences(false, null, null, fAncestor, fLeft, fRight);
 	}
 
 	/**
 	 * Method getToolTipText.
 	 * 
 	 * @return String
 	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
 	 */
 	@Override
 	public String getToolTipText() {
 		if ((null != fLeft) && (null != fRight)) {
 			String format = null;
 
 			// Set the label values for the compare editor
 			StringBuilder leftLabel = null;
 			if (null != fLeft) {
 				leftLabel = new StringBuilder("Target: " + fLeft.getName());
 				if (fLeft instanceof R4EFileRevisionTypedElement) {
 					leftLabel.append("_" + ((R4EFileRevisionTypedElement) fLeft).getFileVersion().getVersionID());
 				}
 				fConfig.setLeftLabel(leftLabel.toString());
 			}
 			StringBuilder rightLabel = null;
 			if (null != fRight) {
 				rightLabel = new StringBuilder("Base: " + fRight.getName());
 				if (fRight instanceof R4EFileRevisionTypedElement) {
 					rightLabel.append("_" + ((R4EFileRevisionTypedElement) fRight).getFileVersion().getVersionID());
 				}
 				fConfig.setRightLabel(rightLabel.toString());
 			}
 
 			if (null != fAncestor) {
 				format = CompareUI.getResourceBundle().getString("ResourceCompare.threeWay.tooltip"); //$NON-NLS-1$
 				final String ancestorLabel = "";
 				return MessageFormat.format(format, new Object[] { ancestorLabel, leftLabel, rightLabel });
 			}
 			format = CompareUI.getResourceBundle().getString("ResourceCompare.twoWay.tooltip"); //$NON-NLS-1$
 			return MessageFormat.format(format, new Object[] { leftLabel, rightLabel });
 		}
 		// fall back
 		return super.getToolTipText();
 	}
 
 	/**
 	 * Method getAdapter.
 	 * 
 	 * @param aAdapter
 	 *            Class
 	 * @return Object
 	 * @see org.eclipse.compare.CompareEditorInput#getAdapter(java.lang.Class)
 	 */
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes")
 	Class aAdapter) {
 		if (IFile.class.equals(aAdapter)) {
 			if (null != getWorkspaceElement()) {
 				return getWorkspaceElement().getResource();
 			}
 			return null;
 		}
 		return super.getAdapter(aAdapter);
 	}
 
 	/**
 	 * Method fireInputChange.
 	 */
 	@Override
 	protected void fireInputChange() { // $codepro.audit.disable emptyMethod
 		// Not implemented for now
 	}
 
 	/**
 	 * Method getWorkspaceElement.
 	 * 
 	 * @return R4EFileTypedElement
 	 */
 	private R4EFileTypedElement getWorkspaceElement() {
 		if (fLeft instanceof R4EFileTypedElement) {
 			return (R4EFileTypedElement) fLeft;
 		}
 		return null;
 	}
 
 	/**
 	 * Method prepareCompareInput.
 	 * 
 	 * @param aMonitor
 	 *            IProgressMonitor
 	 * @return ICompareInput
 	 * @throws InvocationTargetException
 	 * @throws InterruptedException
 	 * @see org.eclipse.compare.CompareEditorInput#prepareCompareInput(IProgressMonitor)
 	 */
 	@Override
 	protected ICompareInput prepareCompareInput(IProgressMonitor aMonitor) {
 
 		if (null != aMonitor) {
 			aMonitor.beginTask("R4E Compare", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
 		}
 
 		// Set the label values for the compare editor
 		initLabels();
 
 		return new R4EFileContextNode(fLeft, fRight);
 	}
 
 	/**
 	 * Method initLabels.
 	 */
 	private void initLabels() {
 		// Set the label values for the compare editor
 		if (null != fLeft) {
 			final StringBuilder leftLabel = new StringBuilder("Target: " + fLeft.getName());
 			if (fLeft instanceof R4EFileRevisionTypedElement) {
 				leftLabel.append(" " + ((R4EFileRevisionTypedElement) fLeft).getFileVersion().getVersionID());
 			}
 			fConfig.setLeftLabel(leftLabel.toString());
 		}
 		if (null != fRight) {
 			final StringBuilder rightLabel = new StringBuilder("Base: " + fRight.getName());
 			if (fRight instanceof R4EFileRevisionTypedElement) {
 				rightLabel.append("_" + ((R4EFileRevisionTypedElement) fRight).getFileVersion().getVersionID());
 			}
 			fConfig.setRightLabel(rightLabel.toString());
 		}
 
 		// If the ancestor is not null, just put the file name as the workspace label
 		if (null != fAncestor) {
 			fConfig.setAncestorLabel(fAncestor.getName());
 		}
 	}
 
 	/**
 	 * Method createContents.
 	 * 
 	 * @param aParent
 	 *            Composite
 	 * @return Control
 	 * @see org.eclipse.compare.CompareEditorInput#createContents(Composite)
 	 */
 	@Override
 	public Control createContents(Composite aParent) {
 		final Control control = super.createContents(aParent);
 
 		//Go to the correct element in the compare editor
 		UIUtils.selectElementInEditor(this);
 
 		//TODO:  This is needed to show annotation highlighting whne opening the compare editor.
 		//		 It should not be needed so this could be investigated in the future.
		fAnnotationSupport.getTargetAnnotationModel().refreshAnnotations();
 
 		return control;
 	}
 
 	/**
 	 * Method findContentViewer.
 	 * 
 	 * @param aOldViewer
 	 *            Viewer
 	 * @param aInput
 	 *            ICompareInput
 	 * @param aParent
 	 *            Composite
 	 * @return Viewer
 	 */
 	@Override
 	public Viewer findContentViewer(Viewer aOldViewer, ICompareInput aInput, Composite aParent) {
 		final Viewer contentViewer = super.findContentViewer(aOldViewer, aInput, aParent);
 		//TODO lmcdubo: ideally we would like to get the file context from the FileContextNode element.  Need refactoring
 		if (aInput instanceof R4EFileContextNode) {
 			final ISelection selection = R4EUIModelController.getNavigatorView().getTreeViewer().getSelection();
 			if (selection instanceof IStructuredSelection) {
 				final Object element = ((IStructuredSelection) selection).getFirstElement();
 				if (element instanceof R4EUIFileContext) {
 					fAnnotationSupport = UIUtils.getCompareAnnotationSupport(contentViewer, element);
 					insertAnnotationNavigationCommands(CompareViewerPane.getToolBarManager(aParent), fAnnotationSupport);
 				} else if (element instanceof R4EUIContent || element instanceof R4EUIAnomalyBasic) {
 					final IReviewAnnotationSupport support = UIUtils.getCompareAnnotationSupport(contentViewer,
 							((IR4EUIModelElement) element).getParent().getParent());
 					fAnnotationSupport = UIUtils.getCompareAnnotationSupport(contentViewer, element);
 					insertAnnotationNavigationCommands(CompareViewerPane.getToolBarManager(aParent), support);
 				}
 			}
 		}
 		return contentViewer;
 	}
 
 	/**
 	 * Method getContentViewer.
 	 * 
 	 * @return Viewer
 	 */
 	public Viewer getContentViewer() {
 		final ICompareNavigator navigator = getNavigator();
 		if (navigator instanceof CompareEditorInputNavigator) {
 			final Object[] panes = ((CompareEditorInputNavigator) navigator).getPanes();
 			for (Object pane : panes) {
 				if (pane instanceof CompareContentViewerSwitchingPane) {
 					return ((CompareContentViewerSwitchingPane) pane).getViewer();
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Method insertAnnotationNavigationCommands.
 	 * 
 	 * @param aManager
 	 *            IToolBarManager
 	 * @param aSupport
 	 *            IReviewAnnotationSupport
 	 */
 	private void insertAnnotationNavigationCommands(IToolBarManager aManager, IReviewAnnotationSupport aSupport) {
 		aManager.add(new Separator());
 		final R4EAnnotationContributionItems r4eItemsManager = new R4EAnnotationContributionItems();
 		final IContributionItem[] items = r4eItemsManager.getR4EContributionItems();
 		for (IContributionItem item : items) {
 			aManager.add(item);
 		}
 		aManager.update(true);
 	}
 
 	/**
 	 * Method isAnnotationsAvailable.
 	 * 
 	 * @param aType
 	 *            String
 	 * @return boolean
 	 */
 	public boolean isAnnotationsAvailable(String aType) {
 		if (null != fAnnotationSupport) {
 			IReviewAnnotationModel model = fAnnotationSupport.getTargetAnnotationModel();
 			if (null != model) {
 				if (model.isAnnotationsAvailable(aType)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method gotoNextAnnotation.
 	 * 
 	 * @param aType
 	 *            String
 	 * @return R4EAnnotation
 	 */
 	public R4EAnnotation gotoNextAnnotation(String aType) {
 		R4EAnnotation annotation = null;
 		if (null != fAnnotationSupport) {
 			IReviewAnnotationModel model = fAnnotationSupport.getTargetAnnotationModel();
 			if (null != model) {
 				annotation = (R4EAnnotation) model.getNextAnnotation(aType);
 				if (null != annotation) {
 					final Position annotationPositon = annotation.getPosition();
 					ITextEditor editor = fAnnotationSupport.getTargetEditor();
 					if (null != editor) {
 						editor.selectAndReveal(annotationPositon.getOffset(), annotationPositon.getLength());
 					}
 				}
 			}
 		}
 		return annotation;
 	}
 
 	/**
 	 * Method gotoPreviousAnnotation.
 	 * 
 	 * @param aType
 	 *            String
 	 * @return R4EAnnotation
 	 */
 	public R4EAnnotation gotoPreviousAnnotation(String aType) {
 		R4EAnnotation annotation = null;
 		if (null != fAnnotationSupport) {
 			IReviewAnnotationModel model = fAnnotationSupport.getTargetAnnotationModel();
 			if (null != model) {
 				annotation = (R4EAnnotation) model.getPreviousAnnotation(aType);
 				if (null != annotation) {
 					final Position annotationPositon = annotation.getPosition();
 					ITextEditor editor = fAnnotationSupport.getTargetEditor();
 					if (null != editor) {
 						editor.selectAndReveal(annotationPositon.getOffset(), annotationPositon.getLength());
 					}
 				}
 			}
 		}
 		return annotation;
 	}
 
 	//Test Methods
 
 	/**
 	 * Method getAnnotationModel.
 	 * 
 	 * @return R4EAnnotationModel
 	 */
 	public R4EAnnotationModel getAnnotationModel() {
 		return (R4EAnnotationModel) fAnnotationSupport.getTargetAnnotationModel();
 	}
 }
