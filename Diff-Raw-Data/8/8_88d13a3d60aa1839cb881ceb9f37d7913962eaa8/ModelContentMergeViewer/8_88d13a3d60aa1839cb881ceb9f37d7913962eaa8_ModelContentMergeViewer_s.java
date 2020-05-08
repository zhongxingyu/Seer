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
 package org.eclipse.emf.compare.ui.viewer.content;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareViewerPane;
 import org.eclipse.compare.HistoryItem;
 import org.eclipse.compare.IStreamContentAccessor;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.ResourceNode;
 import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
 import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.EMFCompareException;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.diff.metamodel.AddModelElement;
 import org.eclipse.emf.compare.diff.metamodel.ConflictingDiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffFactory;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.ModelInputSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.RemoteAddModelElement;
 import org.eclipse.emf.compare.diff.metamodel.RemoteRemoveModelElement;
 import org.eclipse.emf.compare.diff.metamodel.RemoveModelElement;
 import org.eclipse.emf.compare.diff.metamodel.util.DiffAdapterFactory;
 import org.eclipse.emf.compare.diff.service.DiffService;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.service.MatchService;
 import org.eclipse.emf.compare.ui.AbstractCompareAction;
 import org.eclipse.emf.compare.ui.EMFCompareUIPlugin;
 import org.eclipse.emf.compare.ui.ICompareEditorPartListener;
 import org.eclipse.emf.compare.ui.Messages;
 import org.eclipse.emf.compare.ui.ModelCompareInput;
 import org.eclipse.emf.compare.ui.TypedElementWrapper;
 import org.eclipse.emf.compare.ui.util.EMFCompareConstants;
 import org.eclipse.emf.compare.ui.util.EMFCompareEObjectUtils;
 import org.eclipse.emf.compare.ui.viewer.content.part.ModelContentMergeViewerPart;
 import org.eclipse.emf.compare.ui.viewer.content.part.property.ModelContentMergePropertyPart;
 import org.eclipse.emf.compare.ui.viewer.content.part.tree.ModelContentMergeTreePart;
 import org.eclipse.emf.compare.util.ModelUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Compare and merge viewer with two side-by-side content areas and an optional content area for the ancestor.
  * getKind
  * 
  * @author Laurent Goubet <a href="mailto:laurent.goubet@obeo.fr">laurent.goubet@obeo.fr</a>
  */
 public class ModelContentMergeViewer extends ContentMergeViewer {
 	/** Name of the bundle resources property file. */
 	public static final String BUNDLE_NAME = "org.eclipse.emf.compare.ui.viewer.content.ModelMergeViewerResources"; //$NON-NLS-1$
 
 	/** Width to affect to the center area. */
 	public static final int CENTER_WIDTH = 34;
 
 	/** ID of the Properties tab of the {@link ModelContentMergeViewerPart}. */
 	public static final int PROPERTIES_TAB = 1;
 
 	/** ID of the Tree tab of the {@link ModelContentMergeViewerPart}. */
 	public static final int TREE_TAB = 0;
 
 	/** Error message displayed when an invalid tab is selected. */
 	/* package */static final String MESSAGE_ILLEGAL_TAB = Messages.getString("IllegalTab"); //$NON-NLS-1$
 
 	/**
 	 * Ancestor model used for the comparison if it takes place here instead of in the structure viewer's
 	 * content provider.
 	 */
 	protected EObject ancestorModel;
 
 	/** Ancestor part of the three possible parts of this content viewer. */
 	protected ModelContentMergeViewerPart ancestorPart;
 
 	/** Keeps track of the currently selected {@link DiffElement}. */
 	protected DiffElement currentDiff;
 
 	/** Indicates that this is a three way comparison. */
 	protected boolean isThreeWay;
 
 	/**
 	 * Left model used for the comparison if it takes place here instead of in the structure viewer's content
 	 * provider.
 	 */
 	protected EObject leftModel;
 
 	/** Left of the three possible parts of this content viewer. */
 	protected ModelContentMergeViewerPart leftPart;
 
 	/**
 	 * Right model used for the comparison if it takes place here instead of in the structure viewer's content
 	 * provider.
 	 */
 	protected EObject rightModel;
 
 	/** Right of the three possible parts of this content viewer. */
 	protected ModelContentMergeViewerPart rightPart;
 
 	/** Keeps track of the currently selected tab for this viewer part. */
 	protected int selectedTab = TREE_TAB;
 
 	/** Color used to circle and draw the lines to added elements. */
 	/* package */RGB addedColor;
 
 	/** Color used to circle and draw the lines between modified elements. */
 	/* package */RGB changedColor;
 
 	/** Color used to circle and draw the lines between conflicting elements. */
 	/* package */RGB conflictingColor;
 
 	/**
 	 * Indicates that the diff markers should be drawn. This allows defining a threshold to avoid too long
 	 * drawing times.
 	 */
 	/* package */boolean drawDiffMarkers;
 
 	/** Color used to highlight the selected elements. */
 	/* package */RGB highlightColor;
 
 	/** Color used to circle and draw the lines from deleted elements. */
 	/* package */RGB removedColor;
 
 	/**
 	 * this is the "center" part of the content merge viewer where we handle all the drawing operations.
 	 */
 	private AbstractBufferedCanvas canvas;
 
 	/**
 	 * {@link CompareConfiguration} controls various aspect of the GUI elements. This will keep track of the
 	 * one used to created this compare editor.
 	 */
 	private final CompareConfiguration configuration;
 
 	/**
 	 * This is the action we instantiate to handle the {@link DiffElement}s merge from the left model to the
 	 * right model.
 	 */
 	private Action copyDiffLeftToRight;
 
 	/**
 	 * This is the action we instantiate to handle the {@link DiffElement}s merge from the right model to the
 	 * left model.
 	 */
 	private Action copyDiffRightToLeft;
 
 	/**
 	 * Used for history comparisons, this will keep track of modification time of the last {@link HistoryItem}
 	 * we compared.
 	 */
 	private long lastHistoryItemDate;
 
 	/**
 	 * Indicates that the left model has been modified since opening. Will allow us to prompt the user to save
 	 * this model.
 	 */
 	private boolean leftDirty;
 
 	/**
 	 * This will listen for changes made on this plug-in's {@link PreferenceStore} to update the GUI colors as
 	 * needed.
 	 */
 	private final IPropertyChangeListener preferenceListener;
 
 	/**
 	 * Indicates that the right model has been modified since opening. Will allow us to prompt the user to
 	 * save this model.
 	 */
 	private boolean rightDirty;
 
 	/**
 	 * This will listen for changes of the {@link CompareConfiguration} concerning the structure's input and
 	 * selection.
 	 */
 	private final IPropertyChangeListener structureSelectionListener;
 
 	/**
 	 * Creates a new model content merge viewer and intializes it.
 	 * 
 	 * @param parent
 	 *            Parent composite for this viewer.
 	 * @param config
 	 *            The configuration object.
 	 */
 	public ModelContentMergeViewer(Composite parent, CompareConfiguration config) {
 		super(SWT.NONE, ResourceBundle.getBundle(BUNDLE_NAME), config);
 		configuration = config;
 		buildControl(parent);
 		updatePreferences();
 		setContentProvider(new ModelContentMergeContentProvider(config));
 
 		// disables diff copy from either side
 		switchCopyState(false);
 
 		structureSelectionListener = new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (event.getProperty().equals(EMFCompareConstants.PROPERTY_STRUCTURE_SELECTION)) {
 					Object selected = null;
 					if (event.getNewValue() instanceof IStructuredSelection) {
 						selected = ((IStructuredSelection)event.getNewValue()).getFirstElement();
 					}
 					if (selected instanceof DiffElement
 							&& !(selected instanceof DiffGroup && ((DiffGroup)selected).getSubDiffElements()
 									.size() == 0)) {
 						setSelection((DiffElement)selected);
 					}
 				}
 			}
 		};
 		configuration.addPropertyChangeListener(structureSelectionListener);
 
 		preferenceListener = new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (event.getProperty().matches(".*(color|differences)")) { //$NON-NLS-1$
 					updatePreferences();
 				}
 			}
 		};
 		EMFCompareUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
 	}
 
 	/**
 	 * Returns the added element color.
 	 * 
 	 * @return The added element color.
 	 */
 	public RGB getAddedColor() {
 		return addedColor;
 	}
 
 	/**
 	 * Returns the item (either {@link TableItem} or {@link TreeItem}) representing the ancestor element of
 	 * the given {@link DiffElement}.
 	 * 
 	 * @param diff
 	 *            Diff we need to find the ancestor item for.
 	 * @return The item representing the ancestor element of the given {@link DiffElement}.
 	 */
 	public Item getAncestorItem(DiffElement diff) {
 		EObject ancestorElement = diff;
 
 		if (selectedTab == TREE_TAB && diff instanceof ConflictingDiffElement) {
 			ancestorElement = ((ConflictingDiffElement)diff).getOriginElement();
 		}
 		Item ancestorItem = (Item)ancestorPart.find(ancestorElement);
 		if (ancestorItem == null)
 			ancestorItem = ancestorPart.getTreeRoot();
 
 		return ancestorItem;
 	}
 
 	/**
 	 * Returns the center {@link Canvas} appearing between the vewer parts.
 	 * 
 	 * @return The center {@link Canvas}.
 	 */
 	public Canvas getCenterPart() {
 		if (canvas == null && !getControl().isDisposed())
 			canvas = new AbstractBufferedCanvas((Composite)getControl()) {
 				@Override
 				public void doPaint(GC gc) {
 					final List<DiffElement> diffList = ((ModelCompareInput)getInput()).getDiffAsList();
 					if (selectedTab == TREE_TAB) {
 						final List<DiffElement> visibleDiffs = new ArrayList<DiffElement>(diffList.size());
 						for (TreeItem item : leftPart.getTreePart().getVisibleElements())
 							visibleDiffs.add(leftPart.findDiffForTreeItem(item));
 						for (TreeItem item : rightPart.getTreePart().getVisibleElements())
 							visibleDiffs.add(rightPart.findDiffForTreeItem(item));
 						diffList.retainAll(visibleDiffs);
 					}
 
 					if (drawDiffMarkers && getInput() != null) {
 						for (final DiffElement diff : diffList) {
 							drawLine(gc, getLeftItem(diff), getRightItem(diff), diff);
 						}
 					}
 				}
 			};
 		if (canvas != null)
 			canvas.moveAbove(null);
 		return canvas;
 	}
 
 	/**
 	 * Returns the changed element color.
 	 * 
 	 * @return The changed element color.
 	 */
 	public RGB getChangedColor() {
 		return changedColor;
 	}
 
 	/**
 	 * Returns the compare configuration of this viewer.
 	 * 
 	 * @return The compare configuration of this viewer.
 	 */
 	public CompareConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	/**
 	 * Returns the conflicting element color.
 	 * 
 	 * @return The conflicting element color.
 	 */
 	public RGB getConflictingColor() {
 		return conflictingColor;
 	}
 
 	/**
 	 * Returns the highlighting color.
 	 * 
 	 * @return The highlighting color.
 	 */
 	public RGB getHighlightColor() {
 		return highlightColor;
 	}
 
 	/**
 	 * Returns the item (either {@link TableItem} or {@link TreeItem}) representing the left element of the
 	 * given {@link DiffElement}.
 	 * 
 	 * @param diff
 	 *            Diff we need to find the left item for.
 	 * @return The item representing the left element of the given {@link DiffElement}.
 	 */
 	public Item getLeftItem(DiffElement diff) {
 		if (selectedTab == PROPERTIES_TAB) {
 			return getLeftVisibleTableItem(diff);
 		} else if (selectedTab == TREE_TAB) {
 			return getLeftVisibleTreeItem(diff);
 		}
 		throw new IllegalStateException(MESSAGE_ILLEGAL_TAB);
 	}
 
 	/**
 	 * Returns the removed element color.
 	 * 
 	 * @return The removed element color.
 	 */
 	public RGB getRemovedColor() {
 		return removedColor;
 	}
 
 	/**
 	 * Returns the item (either {@link TableItem} or {@link TreeItem}) representing the right element of the
 	 * given {@link DiffElement}.
 	 * 
 	 * @param diff
 	 *            Diff we need to find the right item for.
 	 * @return The item representing the right element of the given {@link DiffElement}.
 	 */
 	public Item getRightItem(DiffElement diff) {
 		if (selectedTab == PROPERTIES_TAB) {
 			return getRightVisibleTableItem(diff);
 		} else if (selectedTab == TREE_TAB) {
 			return getRightVisibleTreeItem(diff);
 		}
 		throw new IllegalStateException(MESSAGE_ILLEGAL_TAB);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setInput(Object input) {
 		// We won't compare again if the given input is the same as the last.
 		boolean changed = false;
 		if (input instanceof ICompareInput && ((ICompareInput)input).getRight() instanceof HistoryItem) {
 			changed = lastHistoryItemDate != ((HistoryItem)((ICompareInput)input).getRight())
 					.getModificationDate();
 			if (changed)
 				lastHistoryItemDate = ((HistoryItem)((ICompareInput)input).getRight()).getModificationDate();
 		}
 		if (configuration.getProperty(EMFCompareConstants.PROPERTY_COMPARISON_RESULT) != null && !changed) {
 			final ModelInputSnapshot snapshot = (ModelInputSnapshot)configuration
 					.getProperty(EMFCompareConstants.PROPERTY_COMPARISON_RESULT);
 			super.setInput(new ModelCompareInput(snapshot.getMatch(), snapshot.getDiff()));
 		} else if (input instanceof ModelInputSnapshot) {
 			final ModelInputSnapshot snapshot = (ModelInputSnapshot)input;
 			super.setInput(new ModelCompareInput(snapshot.getMatch(), snapshot.getDiff()));
 		} else if (input instanceof ICompareInput) {
 			final ITypedElement left = ((ICompareInput)input).getLeft();
 			final ITypedElement right = ((ICompareInput)input).getRight();
 			final ITypedElement ancestor = ((ICompareInput)input).getAncestor();
 
 			if (ancestor != null)
 				isThreeWay = true;
 
 			prepareComparison(left, right, ancestor);
 			doCompare();
 		}
 	}
 
 	/**
 	 * Sets the parts' tree selection given the {@link DiffElement} to select and the identifier of the side
 	 * which triggered the selection change.
 	 * 
 	 * @param diff
 	 *            {@link DiffElement} backing the current selection.
 	 */
 	public void setSelection(DiffElement diff) {
 		currentDiff = diff;
 		if (leftPart != null)
 			leftPart.navigateToDiff(currentDiff);
 		if (rightPart != null)
 			rightPart.navigateToDiff(currentDiff);
 		if (ancestorPart != null && currentDiff.eContainer() instanceof ConflictingDiffElement)
 			ancestorPart.navigateToDiff(currentDiff);
 		switchCopyState(true);
 	}
 
 	/**
 	 * Returns <code>True</code> if the trees and center have to draw markers over the differences.
 	 * 
 	 * @return <code>True</code> if the trees and center have to draw markers over the differences,
 	 *         <code>False</code> otherwise.
 	 */
 	public boolean shouldDrawDiffMarkers() {
 		return drawDiffMarkers;
 	}
 
 	/**
 	 * Redraws this viewer.
 	 */
 	public void update() {
 		ancestorPart.layout();
 		rightPart.layout();
 		leftPart.layout();
 		updateCenter();
 		updateToolItems();
 	}
 
 	/**
 	 * Redraws the center Control.
 	 */
 	public void updateCenter() {
 		if (getCenterPart() != null)
 			getCenterPart().redraw();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#copy(boolean)
 	 */
 	@Override
 	protected void copy(boolean leftToRight) {
 		((ModelCompareInput)getInput()).copy(leftToRight);
 		final ModelInputSnapshot snap = DiffFactory.eINSTANCE.createModelInputSnapshot();
 		snap.setDiff(((ModelCompareInput)getInput()).getDiff());
 		snap.setMatch(((ModelCompareInput)getInput()).getMatch());
 		configuration.setProperty(EMFCompareConstants.PROPERTY_CONTENT_INPUT_CHANGED, snap);
 		leftDirty = leftDirty || leftToRight;
 		rightDirty = rightDirty || !leftToRight;
 		setRightDirty(leftDirty);
 		setLeftDirty(rightDirty);
 	}
 
 	/**
 	 * Copies a single {@link DiffElement} or a {@link DiffGroup} in the given direction, then updates the
 	 * toolbar items states as well as the dirty state of both the left and the right models.
 	 * 
 	 * @param diff
 	 *            {@link DiffElement Element} to copy.
 	 * @param leftToRight
 	 *            Direction of the copy.
 	 * @see ModelCompareInput#copy(DiffElement, boolean)
 	 */
 	protected void copy(DiffElement diff, boolean leftToRight) {
 		if (diff != null) {
 			((ModelCompareInput)getInput()).copy(diff, leftToRight);
 			final ModelInputSnapshot snap = DiffFactory.eINSTANCE.createModelInputSnapshot();
 			snap.setDiff(((ModelCompareInput)getInput()).getDiff());
 			snap.setMatch(((ModelCompareInput)getInput()).getMatch());
 			configuration.setProperty(EMFCompareConstants.PROPERTY_CONTENT_INPUT_CHANGED, snap);
 			leftDirty = leftDirty || (leftToRight && configuration.isLeftEditable());
 			rightDirty = rightDirty || (!leftToRight && configuration.isRightEditable());
 			setRightDirty(leftDirty);
 			setLeftDirty(rightDirty);
 			update();
 		}
 	}
 
 	/**
 	 * Undoes the changed implied by the currently selected {@link DiffElement diff}.
 	 */
 	protected void copyDiffLeftToRight() {
 		if (currentDiff != null)
 			copy(currentDiff, true);
 		currentDiff = null;
 		switchCopyState(false);
 	}
 
 	/**
 	 * Applies the changed implied by the currently selected {@link DiffElement diff}.
 	 */
 	protected void copyDiffRightToLeft() {
 		if (currentDiff != null)
 			copy(currentDiff, false);
 		currentDiff = null;
 		switchCopyState(false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#createControls(Composite)
 	 */
 	@Override
 	protected void createControls(Composite composite) {
 		leftPart = new ModelContentMergeViewerPart(this, composite, EMFCompareConstants.RIGHT);
 		rightPart = new ModelContentMergeViewerPart(this, composite, EMFCompareConstants.LEFT);
 		ancestorPart = new ModelContentMergeViewerPart(this, composite, EMFCompareConstants.ANCESTOR);
 
 		final EditorPartListener partListener = new EditorPartListener(leftPart, rightPart, ancestorPart);
 		leftPart.addCompareEditorPartListener(partListener);
 		rightPart.addCompareEditorPartListener(partListener);
 		ancestorPart.addCompareEditorPartListener(partListener);
 
 		// Synchronizes the left part with the two others
 		handleTreeHSync(leftPart.getTreePart(), rightPart.getTreePart(), ancestorPart.getTreePart());
 		handlePropertyHSync(leftPart.getPropertyPart(), rightPart.getPropertyPart(), ancestorPart.getPropertyPart());
 		// Synchronizes the right part with the two others
 		handleTreeHSync(rightPart.getTreePart(), leftPart.getTreePart(), ancestorPart.getTreePart());
 		handlePropertyHSync(rightPart.getPropertyPart(), leftPart.getPropertyPart(), ancestorPart.getPropertyPart());
 		// Synchronizes the ancestor part with the two others
 		handleTreeHSync(ancestorPart.getTreePart(), rightPart.getTreePart(), leftPart.getTreePart());
 		handlePropertyHSync(ancestorPart.getPropertyPart(), rightPart.getPropertyPart(), leftPart.getPropertyPart());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#createToolItems(ToolBarManager)
 	 */
 	@Override
 	protected void createToolItems(ToolBarManager tbm) {
 		// COPY DIFF LEFT TO RIGHT
 		if (getCompareConfiguration().isRightEditable()) {
 			copyDiffLeftToRight = new AbstractCompareAction(ResourceBundle.getBundle(BUNDLE_NAME),
 					"action.CopyDiffLeftToRight.") { //$NON-NLS-1$
 				@Override
 				public void run() {
 					copyDiffLeftToRight();
 				}
 			};
 			final ActionContributionItem copyLeftToRightContribution = new ActionContributionItem(
 					copyDiffLeftToRight);
 			copyLeftToRightContribution.setVisible(true);
 			tbm.appendToGroup("merge", copyLeftToRightContribution); //$NON-NLS-1$
 		}
 		// COPY DIFF RIGHT TO LEFT
 		if (getCompareConfiguration().isLeftEditable()) {
 			copyDiffRightToLeft = new AbstractCompareAction(ResourceBundle.getBundle(BUNDLE_NAME),
 					"action.CopyDiffRightToLeft.") { //$NON-NLS-1$
 				@Override
 				public void run() {
 					copyDiffRightToLeft();
 				}
 			};
 			final ActionContributionItem copyRightToLeftContribution = new ActionContributionItem(
 					copyDiffRightToLeft);
 			copyRightToLeftContribution.setVisible(true);
 			tbm.appendToGroup("merge", copyRightToLeftContribution); //$NON-NLS-1$
 		}
 		// NEXT DIFF
 		final Action nextDiff = new AbstractCompareAction(ResourceBundle.getBundle(BUNDLE_NAME),
 				"action.NextDiff.") { //$NON-NLS-1$
 			@Override
 			public void run() {
 				navigate(true);
 			}
 		};
 		final ActionContributionItem nextDiffContribution = new ActionContributionItem(nextDiff);
 		nextDiffContribution.setVisible(true);
 		tbm.appendToGroup("navigation", nextDiffContribution); //$NON-NLS-1$
 		// PREVIOUS DIFF
 		final Action previousDiff = new AbstractCompareAction(ResourceBundle.getBundle(BUNDLE_NAME),
 				"action.PrevDiff.") { //$NON-NLS-1$
 			@Override
 			public void run() {
 				navigate(false);
 			}
 		};
 		final ActionContributionItem previousDiffContribution = new ActionContributionItem(previousDiff);
 		previousDiffContribution.setVisible(true);
 		tbm.appendToGroup("navigation", previousDiffContribution); //$NON-NLS-1$
 	}
 
 	/**
 	 * Handles the comparison (either two or three ways) of the models.
 	 */
 	protected void doCompare() {
 		try {
 			final Date start = Calendar.getInstance().getTime();
 			final ModelInputSnapshot snapshot = DiffFactory.eINSTANCE.createModelInputSnapshot();
 
 			if (!isThreeWay) {
 				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
 					public void run(IProgressMonitor monitor) throws InterruptedException {
 						final MatchModel match = MatchService.doMatch(leftModel, rightModel, monitor, Collections.<String, Object> emptyMap());
 						final DiffModel diff = DiffService.doDiff(match, isThreeWay);
 
 						snapshot.setDate(Calendar.getInstance().getTime());
 						snapshot.setDiff(diff);
 						snapshot.setMatch(match);
 					}
 				});
 			} else {
 				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
 					public void run(IProgressMonitor monitor) throws InterruptedException {
 						try {
 							final MatchModel match = MatchService.doMatch(leftModel, rightModel,
 									ancestorModel, monitor, Collections.<String, Object> emptyMap());
 							final DiffModel diff = DiffService.doDiff(match, isThreeWay);
 
 							snapshot.setDate(Calendar.getInstance().getTime());
 							snapshot.setDiff(diff);
 							snapshot.setMatch(match);
 						} finally {
 							monitor.done();
 						}
 					}
 				});
 			}
 			final Date end = Calendar.getInstance().getTime();
 			configuration.setProperty(EMFCompareConstants.PROPERTY_COMPARISON_TIME, end.getTime()
 					- start.getTime());
			// TODO debug purposes only. remove this
			System.out.println(end.getTime() - start.getTime() + "ms");
 
 			configuration.setProperty(EMFCompareConstants.PROPERTY_COMPARISON_RESULT, snapshot);
 			super.setInput(new ModelCompareInput(snapshot.getMatch(), snapshot.getDiff()));
 		} catch (InterruptedException e) {
 			throw new EMFCompareException(e);
 		} catch (InvocationTargetException e) {
 			EMFComparePlugin.log(e, true);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.Viewer#fireSelectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
 	 */
 	@Override
 	protected void fireSelectionChanged(final SelectionChangedEvent event) {
 		super.fireSelectionChanged(event);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#getContents(boolean)
 	 */
 	@Override
 	protected byte[] getContents(boolean left) {
 		byte[] contents = null;
 
 		EObject root = ((TypedElementWrapper)((IMergeViewerContentProvider)getContentProvider())
 				.getLeftContent(getInput())).getObject();
 		if (!left)
 			root = ((TypedElementWrapper)((IMergeViewerContentProvider)getContentProvider())
 					.getRightContent(getInput())).getObject();
 
 		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		try {
 			root.eResource().save(stream, null);
 			contents = stream.toByteArray();
 		} catch (IOException e) {
 			EMFComparePlugin.log(e, false);
 		}
 
 		return contents;
 	}
 
 	/**
 	 * This will minimize the list of differences to the visible differences. Differences are considered
 	 * "visible" if {@link DiffAdapterFactory#shouldBeHidden(EObject)} returns false on it.
 	 * 
 	 * @return {@link List} of the visible differences for this comparison.
 	 */
 	protected List<DiffElement> getVisibleDiffs() {
 		final List<DiffElement> diffs = ((ModelCompareInput)getInput()).getDiffAsList();
 		final List<DiffElement> visibleDiffs = new ArrayList<DiffElement>(diffs.size());
 
 		for (int i = 0; i < diffs.size(); i++) {
 			if (!DiffAdapterFactory.shouldBeHidden(diffs.get(i)))
 				visibleDiffs.add(diffs.get(i));
 		}
 
 		return visibleDiffs;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
 	 */
 	@Override
 	protected void handleDispose(DisposeEvent event) {
 		super.handleDispose(event);
 		configuration.removePropertyChangeListener(structureSelectionListener);
 		EMFCompareUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(preferenceListener);
 		leftPart = null;
 		rightPart = null;
 		ancestorPart = null;
 		canvas = null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#handleResizeAncestor(int, int, int, int)
 	 */
 	@Override
 	protected void handleResizeAncestor(int x, int y, int width, int height) {
 		ancestorPart.setBounds(x, y, width, height);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#handleResizeLeftRight(int, int, int, int)
 	 */
 	@Override
 	protected void handleResizeLeftRight(int x, int y, int leftWidth, int centerWidth, int rightWidth,
 			int height) {
 		if (getCenterPart() != null)
 			getCenterPart().setBounds(leftWidth - (CENTER_WIDTH / 2), y, CENTER_WIDTH, height);
 		leftPart.setBounds(x, y, leftWidth - (CENTER_WIDTH / 2), height);
 		rightPart.setBounds(x + leftWidth + (CENTER_WIDTH / 2), y, rightWidth - (CENTER_WIDTH / 2), height);
 		update();
 	}
 
 	/**
 	 * Selects the next or previous {@link DiffElement} as compared to the currently selected one.
 	 * 
 	 * @param down
 	 *            <code>True</code> if we seek the next {@link DiffElement}, <code>False</code> for the
 	 *            previous.
 	 */
 	protected void navigate(boolean down) {
 		final List<DiffElement> diffs = getVisibleDiffs();
 		if (diffs.size() != 0) {
 			final DiffElement theDiff;
 			if (currentDiff != null)
 				theDiff = currentDiff;
 			else if (diffs.size() == 1)
 				theDiff = diffs.get(0);
 			else if (down)
 				theDiff = diffs.get(diffs.size() - 1);
 			else
 				theDiff = diffs.get(1);
 			for (int i = 0; i < diffs.size(); i++) {
 				if (diffs.get(i).equals(theDiff) && down) {
 					DiffElement next = diffs.get(0);
 					if (diffs.size() > i + 1) {
 						next = diffs.get(i + 1);
 					}
 					if (next != null && !DiffAdapterFactory.shouldBeHidden(next)) {
 						setSelection(next);
 						break;
 					}
 				} else if (diffs.get(i).equals(theDiff) && !down) {
 					DiffElement previous = diffs.get(diffs.size() - 1);
 					if (i > 0) {
 						previous = diffs.get(i - 1);
 					}
 					if (previous != null && !DiffAdapterFactory.shouldBeHidden(previous)) {
 						setSelection(previous);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Handles all the loading operations for the three models we need.
 	 * 
 	 * @param left
 	 *            Left resource (either local or remote) to load.
 	 * @param right
 	 *            Right resource (either local or remote) to load.
 	 * @param ancestor
 	 *            Ancestor resource (either local or remote) to load.
 	 */
 	protected void prepareComparison(ITypedElement left, ITypedElement right, ITypedElement ancestor) {
 		try {
 			final ResourceSet modelResourceSet = new ResourceSetImpl();
 			if (left instanceof ResourceNode && right instanceof ResourceNode) {
 				leftModel = ModelUtils.load(((ResourceNode)left).getResource().getFullPath(),
 						modelResourceSet);
 				rightModel = ModelUtils.load(((ResourceNode)right).getResource().getFullPath(),
 						modelResourceSet);
 				if (isThreeWay)
 					ancestorModel = ModelUtils.load(((ResourceNode)ancestor).getResource().getFullPath(),
 							modelResourceSet);
 			} else if (left instanceof ResourceNode && right instanceof IStreamContentAccessor) {
 				// this is the case of SVN/CVS comparison, we invert left
 				// (remote) and right (local).
 				rightModel = ModelUtils.load(((ResourceNode)left).getResource().getFullPath(),
 						modelResourceSet);
 				leftModel = ModelUtils.load(((IStreamContentAccessor)right).getContents(), right.getName(),
 						modelResourceSet);
 				final String leftLabel = configuration.getRightLabel(rightModel);
 				configuration.setRightLabel(configuration.getLeftLabel(leftModel));
 				configuration.setLeftLabel(leftLabel);
 				configuration.setLeftEditable(false);
 				configuration.setProperty(EMFCompareConstants.PROPERTY_LEFT_IS_REMOTE, true);
 				if (isThreeWay)
 					ancestorModel = ModelUtils.load(((IStreamContentAccessor)ancestor).getContents(),
 							ancestor.getName(), modelResourceSet);
 			}
 		} catch (IOException e) {
 			throw new EMFCompareException(e);
 		} catch (CoreException e) {
 			throw new EMFCompareException(e);
 		}
 	}
 
 	/**
 	 * This will enable or disable the toolbar's copy actions according to the given <code>boolean</code>.
 	 * The "copy diff left to right" action will be enabled if <code>enable</code> is <code>True</code>,
 	 * but the "copy diff right to left" action will only be activated if <code>enable</code> is
 	 * <code>True</code> AND the left model isn't a remote model.
 	 * 
 	 * @param enabled
 	 *            <code>True</code> if we seek to enable the actions, <code>False</code> otherwise.
 	 */
 	protected void switchCopyState(boolean enabled) {
 		boolean leftIsRemote = false;
 		if (configuration.getProperty(EMFCompareConstants.PROPERTY_LEFT_IS_REMOTE) != null)
 			leftIsRemote = Boolean.parseBoolean(configuration.getProperty(
 					EMFCompareConstants.PROPERTY_LEFT_IS_REMOTE).toString());
 		if (copyDiffLeftToRight != null)
 			copyDiffLeftToRight.setEnabled(enabled);
 		if (copyDiffRightToLeft != null)
 			copyDiffRightToLeft.setEnabled(!leftIsRemote && enabled);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#updateContent(Object, Object, Object)
 	 */
 	@Override
 	protected void updateContent(Object ancestor, Object left, Object right) {
 		if (getInput() != null) {
 			Object ancestorObject = ancestor;
 			Object leftObject = left;
 			Object rightObject = right;
 			if (ancestorObject instanceof TypedElementWrapper)
 				ancestorObject = ((TypedElementWrapper)ancestorObject).getObject();
 			if (leftObject instanceof TypedElementWrapper)
 				leftObject = ((TypedElementWrapper)leftObject).getObject();
 			if (rightObject instanceof TypedElementWrapper)
 				rightObject = ((TypedElementWrapper)rightObject).getObject();
 
 			if (ancestorObject != null)
 				ancestorPart.setInput(ancestorObject);
 			if (leftObject != null)
 				leftPart.setInput(leftObject);
 			if (rightObject != null)
 				rightPart.setInput(rightObject);
 		}
 		update();
 	}
 
 	/**
 	 * Updates the values of all the variables using preferences values.
 	 */
 	protected void updatePreferences() {
 		final IPreferenceStore comparePreferences = EMFCompareUIPlugin.getDefault().getPreferenceStore();
 		updateColors(comparePreferences);
 		updateDiffThreshold(comparePreferences);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#updateToolItems()
 	 */
 	@Override
 	protected void updateToolItems() {
 		super.updateToolItems();
 		CompareViewerPane.getToolBarManager(getControl().getParent()).update(true);
 	}
 
 	/**
 	 * Returns the {@link TreeItem} representing the left element of the given {@link DiffElement}.
 	 * 
 	 * @param diff
 	 *            Diff we need to find the left item for.
 	 * @return The item representing the left element of the given {@link DiffElement}.
 	 */
 	/* package */TableItem getLeftVisibleTableItem(DiffElement diff) {
 		return (TableItem)leftPart.find(diff);
 	}
 
 	/**
 	 * Returns the {@link TreeItem} representing the left element of the given {@link DiffElement}.
 	 * 
 	 * @param diff
 	 *            Diff we need to find the left item for.
 	 * @return The item representing the left element of the given {@link DiffElement}.
 	 */
 	/* package */TreeItem getLeftVisibleTreeItem(DiffElement diff) {
 		EObject leftData = EMFCompareEObjectUtils.getLeftElement(diff);
 		final EObject rightData = EMFCompareEObjectUtils.getRightElement(diff);
 		TreeItem leftItem = (TreeItem)leftPart.find(leftData);
 		final Item rightItem = (Item)rightPart.find(rightData);
 
 		if (leftItem == null) {
 			leftItem = leftPart.getTreeRoot();
 			if (leftItem != null)
 				leftData = (EObject)leftItem.getData();
 		}
 
 		if (leftItem != null
 				&& (!leftItem.getData().equals(leftData) || diff instanceof ModelElementChangeRightTarget)) {
 			if (rightItem != null && rightItem.getData().equals(rightData)
 					&& ((EObject)rightItem.getData()).eContainer() != null) {
 				final int rightIndex = ((EObject)rightItem.getData()).eContainer().eContents().indexOf(
 						rightItem.getData());
 				final EList<EObject> leftList = ((EObject)leftItem.getData()).eContents();
 				// Ensures we cannot trigger ArrayOutOfBounds exeptions
 				final int leftIndex = Math.min(rightIndex - 1, leftList.size() - 1);
 				if (leftIndex > 0)
 					leftItem = (TreeItem)leftPart.find(leftList.get(leftIndex));
 			}
 		}
 
 		return leftItem;
 	}
 
 	/**
 	 * Returns the {@link TableItem} representing the right element of the given {@link DiffElement}.
 	 * 
 	 * @param diff
 	 *            Diff we need to find the right item for.
 	 * @return The item representing the right element of the given {@link DiffElement}.
 	 */
 	/* package */TableItem getRightVisibleTableItem(DiffElement diff) {
 		return (TableItem)rightPart.find(diff);
 	}
 
 	/**
 	 * Returns the {@link TreeItem} representing the left element of the given {@link DiffElement}.
 	 * 
 	 * @param diff
 	 *            Diff we need to find the left item for.
 	 * @return The item representing the left element of the given {@link DiffElement}.
 	 */
 	/* package */TreeItem getRightVisibleTreeItem(DiffElement diff) {
 		EObject rightData = EMFCompareEObjectUtils.getRightElement(diff);
 		final EObject leftData = EMFCompareEObjectUtils.getLeftElement(diff);
 		TreeItem rightItem = (TreeItem)rightPart.find(rightData);
 		final Item leftItem = (Item)leftPart.find(leftData);
 
 		if (rightItem == null) {
 			rightItem = leftPart.getTreeRoot();
 			if (rightItem != null)
 				rightData = (EObject)rightItem.getData();
 		}
 
 		if (rightItem != null
 				&& (!rightItem.getData().equals(rightData) || diff instanceof ModelElementChangeLeftTarget)) {
 			if (leftItem != null && leftItem.getData().equals(leftData)
 					&& ((EObject)leftItem.getData()).eContainer() != null) {
 				final int leftIndex = ((EObject)leftItem.getData()).eContainer().eContents().indexOf(
 						leftItem.getData());
 				final EList<EObject> rightList = ((EObject)rightItem.getData()).eContents();
 				// Ensures we cannot trigger ArrayOutOfBounds exeptions
 				final int rightIndex = Math.min(leftIndex - 1, rightList.size() - 1);
 				if (rightIndex > 0)
 					rightItem = (TreeItem)rightPart.find(rightList.get(rightIndex));
 			}
 		}
 
 		return rightItem;
 	}
 	
 	/**
 	 * Allows synchronization of the properties viewports horizontal scrolling.
 	 * 
 	 * @param parts
 	 *            The other parts to synchronize with.
 	 */
 	private void handlePropertyHSync(ModelContentMergePropertyPart... parts) {
 		if (parts.length < 2)
 			throw new IllegalArgumentException(Messages.getString("ModelContentMergeViewerPart.illegalSync")); //$NON-NLS-1$
 
 		// inspired from TreeMergeViewer#hsynchViewport
 		final Table table1 = parts[0].getTable();
 		final Table table2 = parts[1].getTable();
 		final Table table3;
 		if (parts.length > 2)
 			table3 = parts[2].getTable();
 		else
 			table3 = null;
 		final ScrollBar scrollBar1 = table1.getHorizontalBar();
 
 		scrollBar1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				final int max = scrollBar1.getMaximum() - scrollBar1.getThumb();
 				double v = 0.0;
 				if (max > 0)
 					v = (double)scrollBar1.getSelection() / (double)max;
 				if (table2.isVisible()) {
 					final ScrollBar scrollBar2 = table2.getHorizontalBar();
 					scrollBar2.setSelection((int)((scrollBar2.getMaximum() - scrollBar2.getThumb()) * v));
 				}
 				if (table3 != null && table3.isVisible()) {
 					final ScrollBar scrollBar3 = table3.getHorizontalBar();
 					scrollBar3.setSelection((int)((scrollBar3.getMaximum() - scrollBar3.getThumb()) * v));
 				}
 				if (SWT.getPlatform().equals("carbon") && getControl() != null && !getControl().isDisposed()) { //$NON-NLS-1$
 					getControl().getDisplay().update();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Allows synchronization of the tree viewports horizontal scrolling.
 	 * 
 	 * @param parts
 	 *            The other parts to synchronize with.
 	 */
 	private void handleTreeHSync(ModelContentMergeTreePart... parts) {
 		if (parts.length < 2)
 			throw new IllegalArgumentException(Messages.getString("ModelContentMergeViewerPart.illegalSync")); //$NON-NLS-1$
 
 		// inspired from TreeMergeViewer#hsynchViewport
 		final Tree tree1 = parts[0].getTree();
 		final Tree tree2 = parts[1].getTree();
 		final Tree tree3;
 		if (parts.length > 2)
 			tree3 = parts[2].getTree();
 		else
 			tree3 = null;
 		final ScrollBar scrollBar1 = tree1.getHorizontalBar();
 
 		scrollBar1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				final int max = scrollBar1.getMaximum() - scrollBar1.getThumb();
 				double v = 0.0;
 				if (max > 0)
 					v = (double)scrollBar1.getSelection() / (double)max;
 				if (tree2.isVisible()) {
 					final ScrollBar scrollBar2 = tree2.getHorizontalBar();
 					scrollBar2.setSelection((int)((scrollBar2.getMaximum() - scrollBar2.getThumb()) * v));
 				}
 				if (tree3 != null && tree3.isVisible()) {
 					final ScrollBar scrollBar3 = tree3.getHorizontalBar();
 					scrollBar3.setSelection((int)((scrollBar3.getMaximum() - scrollBar3.getThumb()) * v));
 				}
 				if (SWT.getPlatform().equals("carbon") && getControl() != null && !getControl().isDisposed()) { //$NON-NLS-1$
 					getControl().getDisplay().update();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Updates the value of the colors as they are changed on the preference page.
 	 * 
 	 * @param comparePreferences
 	 *            Preference store where to retrieve our values.
 	 */
 	private void updateColors(IPreferenceStore comparePreferences) {
 		highlightColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_HIGHLIGHT_COLOR);
 		changedColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_CHANGED_COLOR);
 		conflictingColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_CONFLICTING_COLOR);
 		addedColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_ADDED_COLOR);
 		removedColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_REMOVED_COLOR);
 	}
 
 	/**
 	 * Updates the value of the maximum diff count as it is changed on the preference page.
 	 * 
 	 * @param comparePreferences
 	 *            Preference store where to retrieve our values.
 	 */
 	private void updateDiffThreshold(IPreferenceStore comparePreferences) {
 		drawDiffMarkers = comparePreferences.getBoolean(EMFCompareConstants.PREFERENCES_KEY_DRAW_DIFFERENCES);
 	}
 
 	/**
 	 * We want to avoid flickering as much as possible for our draw operations on the center part, yet we
 	 * can't use double buffering to draw on it. We will then draw on a {@link Canvas} moved above that center
 	 * part.
 	 */
 	private abstract class AbstractBufferedCanvas extends Canvas {
 		/** Buffer used by this {@link Canvas} to smoothly paint its content. */
 		protected Image buffer;
 
 		/**
 		 * This array is used to compute the curve to draw between left and right matching elements.
 		 */
 		private double[] baseCenterCurve;
 
 		/**
 		 * Default constructor, instantiates the canvas given its parent.
 		 * 
 		 * @param parent
 		 *            Parent of the canvas.
 		 */
 		public AbstractBufferedCanvas(Composite parent) {
 			super(parent, SWT.NO_BACKGROUND | SWT.NO_MERGE_PAINTS | SWT.NO_REDRAW_RESIZE);
 
 			final PaintListener paintListener = new PaintListener() {
 				public void paintControl(PaintEvent event) {
 					doubleBufferedPaint(event.gc);
 				}
 			};
 			addPaintListener(paintListener);
 
 			addDisposeListener(new DisposeListener() {
 				public void widgetDisposed(DisposeEvent e) {
 					if (buffer != null) {
 						buffer.dispose();
 						buffer = null;
 					}
 					removePaintListener(paintListener);
 				}
 			});
 		}
 
 		/**
 		 * Reimplement this method for the actual drawing.
 		 * 
 		 * @param gc
 		 *            {@link GC} used for the painting.
 		 */
 		public abstract void doPaint(GC gc);
 
 		/**
 		 * Draws a line connecting the given right and left items.
 		 * 
 		 * @param gc
 		 *            {@link GC graphics configuration} on which to actually draw.
 		 * @param leftItem
 		 *            Left of the two items to connect.
 		 * @param rightItem
 		 *            Right of the items to connect.
 		 * @param diff
 		 *            {@link DiffElement} providing the left and right datas to connect.
 		 */
 		protected void drawLine(GC gc, Item leftItem, Item rightItem, DiffElement diff) {
 			if (leftItem == null || rightItem == null || DiffAdapterFactory.shouldBeHidden(diff))
 				return;
 
 			final Rectangle centerbounds = getCenterPart().getBounds();
 			Rectangle leftBounds = null;
 			Rectangle rightBounds = null;
 			if (selectedTab == TREE_TAB) {
 				leftBounds = ((TreeItem)leftItem).getBounds();
 				rightBounds = ((TreeItem)rightItem).getBounds();
 			} else if (selectedTab == PROPERTIES_TAB) {
 				leftBounds = ((TableItem)leftItem).getBounds();
 				rightBounds = ((TableItem)rightItem).getBounds();
 			} else {
 				throw new IllegalStateException(MESSAGE_ILLEGAL_TAB);
 			}
 
 			// Defines the circling Color
 			RGB color = changedColor;
 			if (diff.eContainer() instanceof ConflictingDiffElement) {
 				color = conflictingColor;
 			} else if (diff instanceof AddModelElement || diff instanceof RemoteAddModelElement) {
 				color = addedColor;
 			} else if (diff instanceof RemoveModelElement || diff instanceof RemoteRemoveModelElement) {
 				color = removedColor;
 			}
 
 			// Defines all variables needed for drawing the line.
 			final int treeTabBorder = 5;
 			final int leftX = 0;
 			final int rightX = centerbounds.width;
 			final int leftRectangleHeight = leftBounds.height - 1;
 			final int rightRectangleHeight = rightBounds.height - 1;
 
 			int leftY = leftBounds.y + leftRectangleHeight / 2 + treeTabBorder
 					+ leftPart.getHeaderHeight();
 			int rightY = rightBounds.y + rightRectangleHeight / 2 + treeTabBorder
 					+ rightPart.getHeaderHeight();
 			if (selectedTab == TREE_TAB
 					&& (!leftItem.getData().equals(EMFCompareEObjectUtils.getLeftElement(diff))
 							|| diff instanceof AddModelElement || diff instanceof RemoteRemoveModelElement)) {
 				leftY += leftRectangleHeight / 2;
 			}
 			if (selectedTab == TREE_TAB
 					&& (!rightItem.getData().equals(EMFCompareEObjectUtils.getRightElement(diff))
 							|| diff instanceof RemoveModelElement || diff instanceof RemoteAddModelElement)) {
 				rightY += rightRectangleHeight / 2;
 			}
 
 			int lineWidth = 1;
 			if (selectedTab == PROPERTIES_TAB || diff == currentDiff) {
 				lineWidth = 2;
 			}
 
 			// Performs the actual drawing
 			gc.setForeground(new Color(getCenterPart().getDisplay(), color));
 			gc.setLineWidth(lineWidth);
 			gc.setLineStyle(SWT.LINE_SOLID);
 			final int[] points = getCenterCurvePoints(leftX, leftY, rightX, rightY);
 			for (int i = 1; i < points.length; i++) {
 				gc.drawLine(leftX + i - 1, points[i - 1], leftX + i, points[i]);
 			}
 		}
 
 		/**
 		 * Paints this component using double-buffering.
 		 * 
 		 * @param dest
 		 *            Destination {@link GC graphics}.
 		 */
 		void doubleBufferedPaint(GC dest) {
 			final Point size = getSize();
 
 			if (size.x <= 0 || size.y <= 0)
 				return;
 
 			if (buffer != null) {
 				final Rectangle bufferBounds = buffer.getBounds();
 				if (bufferBounds.width != size.x || bufferBounds.height != size.y) {
 					buffer.dispose();
 					buffer = null;
 				}
 			}
 
 			if (buffer == null)
 				buffer = new Image(getDisplay(), size.x, size.y);
 
 			final GC gc = new GC(buffer);
 			// Draw lines on the left and right edges
 			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
 			gc.drawLine(0, 0, 0, getBounds().height);
 			gc.drawLine(getBounds().width - 1, 0, getBounds().width - 1, getBounds().height);
 			try {
 				gc.setBackground(getBackground());
 				gc.fillRectangle(0, 0, size.x, size.y);
 				doPaint(gc);
 			} finally {
 				gc.dispose();
 			}
 
 			dest.drawImage(buffer, 0, 0);
 		}
 
 		/**
 		 * Computes the points needed to draw a curve of the given width.
 		 * 
 		 * @param width
 		 *            This is the width of the curve to build.
 		 */
 		private void buildBaseCenterCurve(int width) {
 			final double doubleWidth = width;
 			baseCenterCurve = new double[CENTER_WIDTH];
 			for (int i = 0; i < CENTER_WIDTH; i++) {
 				final double r = i / doubleWidth;
 				baseCenterCurve[i] = Math.cos(Math.PI * r);
 			}
 		}
 
 		/**
 		 * Computes the points to connect for the curve between the two items to connect.
 		 * 
 		 * @param startx
 		 *            X coordinate of the starting point.
 		 * @param starty
 		 *            Y coordinate of the starting point.
 		 * @param endx
 		 *            X coordinate of the ending point.
 		 * @param endy
 		 *            Y coordinate of the ending point.
 		 * @return The points to connect to draw the curve between the two items to connect.
 		 */
 		private int[] getCenterCurvePoints(int startx, int starty, int endx, int endy) {
 			if (baseCenterCurve == null) {
 				buildBaseCenterCurve(endx - startx);
 			}
 			double height = endy - starty;
 			height = height / 2;
 			final int width = endx - startx;
 			final int[] points = new int[width];
 			for (int i = 0; i < width; i++) {
 				points[i] = (int)(-height * baseCenterCurve[i] + height + starty);
 			}
 			return points;
 		}
 	}
 
 	/**
 	 * Basic implementation of an {@link ICompareEditorPartListener}.
 	 */
 	private class EditorPartListener implements ICompareEditorPartListener {
 		/** Viewer parts this listener is registered for. */
 		private final ModelContentMergeViewerPart[] viewerParts;
 
 		/**
 		 * Instantiate this {@link EditorPartListener} given the left, right and ancestor viewer parts.
 		 * 
 		 * @param parts
 		 *            The viewer parts.
 		 */
 		public EditorPartListener(ModelContentMergeViewerPart... parts) {
 			viewerParts = parts;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see ICompareEditorPartListener#selectedTabChanged()
 		 */
 		public void selectedTabChanged(int newIndex) {
 			selectedTab = newIndex;
 			for (int i = 0; i < viewerParts.length; i++) {
 				viewerParts[i].setSelectedTab(newIndex);
 			}
 			updateCenter();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see ICompareEditorPartListener#selectionChanged()
 		 */
 		public void selectionChanged(SelectionChangedEvent event) {
 			fireSelectionChanged(event);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see ICompareEditorPartListener#updateCenter()
 		 */
 		public void updateCenter() {
 			ModelContentMergeViewer.this.updateCenter();
 		}
 	}
 }
