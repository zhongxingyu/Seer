 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class extends the default tree viewer to be able to browse the items
  * using user-defined commands
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.navigator;
 
 import org.eclipse.jface.layout.TreeColumnLayout;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.DecoratingCellLabelProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.TreeViewerColumn;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.filters.TreeTableFilter;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIComment;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIContent;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIContentsContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewItem;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRootElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.sorters.TreeTableComparator;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.TreeItem;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class ReviewNavigatorTreeViewer extends TreeViewer {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field REVIEW_COLUMN_LABEL. (value is ""Review: "")
 	 */
 	private static final String REVIEW_COLUMN_LABEL = "Review-> "; //$NON-NLS-1$
 
 	/**
 	 * Field REVIEW_GROUP_COLUMN_TOOLTIP. (value is ""Group: "")
 	 */
 	private static final String REVIEW_GROUP_COLUMN_TOOLTIP = "Group-> "; //$NON-NLS-1$
 
 	/**
 	 * Field INVALID_PATH. (value is ""--"")
 	 */
 	private static final String INVALID_PATH = "--"; //$NON-NLS-1$
 
 	/**
 	 * Field VERSION_TARGET_LABEL. (value is ""Target Version: "")
 	 */
 	private static final String VERSION_TARGET_LABEL = "Target Version: "; //$NON-NLS-1$
 
 	/**
 	 * Field VERSION_BASE_LABEL. (value is ""Base Version: "")
 	 */
 	private static final String VERSION_BASE_LABEL = "Base Version: "; //$NON-NLS-1$
 
 	/**
 	 * Field NUM_CHANGES_ITEM_COLUMN_TOOLTIP. (value is ""Number of Reviewed Changes / Number of Changes for this Review
 	 * Item"")
 	 */
 	private static final String NUM_CHANGES_ITEM_COLUMN_TOOLTIP = "Number of Reviewed Changes / Number of Changes for this Review Item"; //$NON-NLS-1$
 
 	/**
 	 * Field NUM_CHANGES_FILE_COLUMN_TOOLTIP. (value is ""Number of Changes for this File"")
 	 */
 	private static final String NUM_CHANGES_FILE_COLUMN_TOOLTIP = "Number of Changes for this File"; //$NON-NLS-1$
 
 	/**
 	 * Field NUM_ANOMALIES_COLUMN_TOOLTIP. (value is ""Number of Anomalies written for this File"")
 	 */
 	private static final String NUM_ANOMALIES_COLUMN_TOOLTIP = "Number of Anomalies written for this File"; //$NON-NLS-1$
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fIsDefaultDisplay.
 	 */
 	private boolean fIsDefaultDisplay = true;
 
 	/**
 	 * Field fDefaultInput.
 	 */
 	private Object fDefaultInput = null;
 
 	/**
 	 * Field fTreeColumnLayout.
 	 */
 	private TreeColumnLayout fTreeColumnLayout = null;
 
 	/**
 	 * Field fTreeComparator.
 	 */
 	ViewerComparator fTreeComparator = null;
 
 	/**
 	 * Field fTreeTableComparator.
 	 */
 	TreeTableComparator fTreeTableComparator = null;
 
 	/**
 	 * Field fElementColumn.
 	 */
 	private TreeViewerColumn fElementColumn = null;
 
 	/**
 	 * Field fPathColumn.
 	 */
 	private TreeViewerColumn fPathColumn = null;
 
 	/**
 	 * Field fAssignColumn.
 	 */
 	private TreeViewerColumn fAssignColumn = null;
 
 	/**
 	 * Field fNumChangesColumn.
 	 */
 	private TreeViewerColumn fNumChangesColumn = null;
 
 	/**
 	 * Field fNumAnomaliesColumn.
 	 */
 	private TreeViewerColumn fNumAnomaliesColumn = null;
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method isDefaultDisplay.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isDefaultDisplay() {
 		return fIsDefaultDisplay;
 	}
 
 	/**
 	 * Method setDefaultInput.
 	 * 
 	 * @param aInput
 	 *            - Object
 	 */
 	public void setDefaultInput(Object aInput) {
 		fDefaultInput = aInput;
 	}
 
 	/**
 	 * Constructor for ReviewNavigatorTreeViewer.
 	 * 
 	 * @param aParent
 	 *            Composite
 	 * @param aStyle
 	 *            int
 	 */
 	public ReviewNavigatorTreeViewer(Composite aParent, int aStyle) {
 		super(aParent, aStyle);
 		fTreeColumnLayout = new TreeColumnLayout();
 		fTreeTableComparator = new TreeTableComparator();
 		aParent.setLayout(fTreeColumnLayout);
 	}
 
 	/**
 	 * Method getPrevious.
 	 * 
 	 * @param aItem
 	 *            TreeItem
 	 * @return TreeItem
 	 */
 	public TreeItem getPrevious(TreeItem aItem) {
 		final TreeItem newItem = (TreeItem) getPreviousItem(aItem);
 		if (null == newItem) {
 			return aItem;
 		}
 		return newItem;
 	}
 
 	/**
 	 * Method getNext.
 	 * 
 	 * @param aItem
 	 *            TreeItem
 	 * @return TreeItem
 	 */
 	public TreeItem getNext(TreeItem aItem) {
 		final TreeItem newItem = (TreeItem) getNextItem(aItem, true);
 		if (null == newItem) {
 			return aItem;
 		}
 		return newItem;
 	}
 
 	/**
 	 * Method setViewTree.
 	 */
 	public void setViewTree() {
 		createElementsColumn();
 		getTree().setHeaderVisible(false);
 		if (null != fAssignColumn) {
 			fAssignColumn.getColumn().dispose();
 			fAssignColumn = null;
 		}
 		if (null != fPathColumn) {
 			fPathColumn.getColumn().dispose();
 			fPathColumn = null;
 		}
 		if (null != fNumChangesColumn) {
 			fNumChangesColumn.getColumn().dispose();
 			fNumChangesColumn = null;
 		}
 		if (null != fNumAnomaliesColumn) {
 			fNumAnomaliesColumn.getColumn().dispose();
 			fNumAnomaliesColumn = null;
 		}
 		fTreeColumnLayout.setColumnData(fElementColumn.getColumn(), new ColumnWeightData(100, true));
 
 		//Remove Tree Table filters
 		final TreeTableFilter filter = ((ReviewNavigatorActionGroup) R4EUIModelController.getNavigatorView()
 				.getActionSet()).getTreeTableFilter();
 		this.removeFilter(filter);
 
 		//Restore Tree sorters (if any)
 		setComparator(fTreeComparator);
 
 		//Restore Default Tree input
 		this.setInput(fDefaultInput);
 
 		//Refresh Display	
 		this.getTree().getParent().layout();
 		fIsDefaultDisplay = true;
 	}
 
 	/**
 	 * Method setViewTreeTable.
 	 */
 	public void setViewTreeTable() {
 		//Get currently selected element
 		IR4EUIModelElement currentElement = null;
 		final IStructuredSelection selection = (IStructuredSelection) this.getSelection();
 		if (null != selection) {
 			currentElement = (IR4EUIModelElement) selection.getFirstElement();
 		}
 		//Set a default selected element in TreeTable
 		if (currentElement == null) {
 			currentElement = R4EUIModelController.getActiveReview().getChildren()[0];
 		} else if (currentElement instanceof R4EUIReviewItem || currentElement instanceof R4EUIFileContext) {
 			//do nothing
 		} else if (currentElement instanceof R4EUIContentsContainer) {
 			currentElement = currentElement.getParent();
 		} else if (currentElement instanceof R4EUIContent) {
 			currentElement = currentElement.getParent().getParent();
 		} else if (currentElement instanceof R4EUIAnomalyContainer
 				&& currentElement.getParent() instanceof R4EUIFileContext) {
 			currentElement = currentElement.getParent();
 		} else if (currentElement instanceof R4EUIAnomalyBasic
 				&& currentElement.getParent().getParent() instanceof R4EUIFileContext) {
 			currentElement = currentElement.getParent().getParent();
 		} else if (currentElement instanceof R4EUIComment
 				&& currentElement.getParent().getParent().getParent() instanceof R4EUIFileContext) {
 			currentElement = currentElement.getParent().getParent().getParent();
 		} else {
 			//Set first TreeTable element as default
 			currentElement = R4EUIModelController.getActiveReview().getChildren()[0];
 		}
 		Object[] expandedElements = getExpandedElements();
 
 		//Create Columns
 		createPathColumn();
 		createAssignmentColumn();
 		createNumChangesColumn();
 		createNumAnomaliesColumn();
 		getTree().setHeaderVisible(true);
 
 		//Reset Layout to adjust Columns widths
 		fTreeColumnLayout.setColumnData(fElementColumn.getColumn(), new ColumnWeightData(25, true));
 		fTreeColumnLayout.setColumnData(fPathColumn.getColumn(), new ColumnWeightData(50, true));
 		fTreeColumnLayout.setColumnData(fAssignColumn.getColumn(), new ColumnWeightData(9, true));
 		fTreeColumnLayout.setColumnData(fNumChangesColumn.getColumn(), new ColumnWeightData(8, true));
 		fTreeColumnLayout.setColumnData(fNumAnomaliesColumn.getColumn(), new ColumnWeightData(8, true));
 		R4EUIReviewBasic activeReview = R4EUIModelController.getActiveReview();
 		if (null != activeReview) {
			fElementColumn.getColumn().setText(activeReview.getReview().getName());
			fElementColumn.getColumn().setToolTipText(
					REVIEW_GROUP_COLUMN_TOOLTIP + activeReview.getParent().getName() + R4EUIConstants.LINE_FEED
							+ REVIEW_COLUMN_LABEL + activeReview.getName());
 		}
 
 		//Set Tree Table Filters (shows only Review Items and Files for current review
 		final TreeTableFilter filter = ((ReviewNavigatorActionGroup) R4EUIModelController.getNavigatorView()
 				.getActionSet()).getTreeTableFilter();
 
 		//Save Default Tree input and adjust Tree Table input
 		fDefaultInput = this.getInput();
 		if (fDefaultInput instanceof R4EUIRootElement || fDefaultInput instanceof R4EUIReviewGroup) {
 			this.setInput(R4EUIModelController.getActiveReview());
 		}
 		this.addFilter(filter);
 
 		//Set Default sorter
 		fTreeComparator = getComparator();
 		setComparator(fTreeTableComparator);
 
 		//Refresh Display	
 		this.getTree().getParent().layout();
 		UIUtils.setNavigatorViewFocus(currentElement, 1);
 		setExpandedElements(expandedElements);
 		fIsDefaultDisplay = false;
 	}
 
 	/**
 	 * Method createElementsColumn.
 	 */
 	public void createElementsColumn() {
 		if (null == fElementColumn) {
 			final DecoratingCellLabelProvider provider = new DecoratingCellLabelProvider(
 					new ReviewNavigatorLabelProvider(), new ReviewNavigatorDecorator());
 			fElementColumn = new TreeViewerColumn(this, SWT.NONE);
 			fElementColumn.setLabelProvider(provider);
 			fElementColumn.getColumn().setMoveable(false);
 			fElementColumn.getColumn().setResizable(true);
 
 			fElementColumn.getColumn().addListener(SWT.Selection, new Listener() {
 				public void handleEvent(Event event) {
 					if (!isDefaultDisplay()) {
 						fTreeTableComparator.setColumnName(R4EUIConstants.ELEMENTS_LABEL_NAME);
 						getTree().setSortDirection(fTreeTableComparator.getDirection());
 						getTree().setSortColumn(fElementColumn.getColumn());
 						refresh();
 					}
 				}
 			});
 		}
 	}
 
 	/**
 	 * Method createPathColumn.
 	 */
 	private void createPathColumn() {
 		if (null == fPathColumn) {
 			fPathColumn = new TreeViewerColumn(this, SWT.NONE);
 			fPathColumn.getColumn().setText(R4EUIConstants.PATH_LABEL);
 			fTreeColumnLayout.setColumnData(fPathColumn.getColumn(), new ColumnWeightData(0, true));
 			fPathColumn.getColumn().setMoveable(true);
 			fPathColumn.getColumn().setResizable(true);
 			fPathColumn.setLabelProvider(new ColumnLabelProvider() {
 				@Override
 				public String getText(Object element) {
 					if (element instanceof R4EUIFileContext) {
 						//First try target file version
 						R4EFileVersion version = ((R4EUIFileContext) element).getTargetFileVersion();
 						if (null != version) {
 							return UIUtils.getProjectPath(version);
 						} else {
 							//Try base file version
 							version = ((R4EUIFileContext) element).getBaseFileVersion();
 							if (null != version) {
 								return UIUtils.getProjectPath(version);
 							} else {
 								return INVALID_PATH;
 							}
 						}
 					} else {
 						return null;
 					}
 				}
 
 				@Override
 				public String getToolTipText(Object element) {
 					StringBuffer buffer = new StringBuffer();
 					if (element instanceof R4EUIFileContext) {
 						R4EFileVersion targetVersion = ((R4EUIFileContext) element).getTargetFileVersion();
 						buffer.append(VERSION_TARGET_LABEL);
 						if (null == targetVersion) {
 							buffer.append(R4EUIConstants.NO_VERSION_PROPERTY_MESSAGE);
 						} else {
 							buffer.append(targetVersion.getVersionID());
 						}
 						R4EFileVersion baseVersion = ((R4EUIFileContext) element).getBaseFileVersion();
 						buffer.append(System.getProperty("line.separator")); //$NON-NLS-1$
 						buffer.append(VERSION_BASE_LABEL);
 						if (null == baseVersion) {
 							buffer.append(R4EUIConstants.NO_VERSION_PROPERTY_MESSAGE);
 						} else {
 							buffer.append(baseVersion.getVersionID());
 						}
 						return buffer.toString();
 					}
 					return null;
 				}
 
 				@Override
 				public Point getToolTipShift(Object object) {
 					return new Point(R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_X, R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_Y);
 				}
 
 				@Override
 				public int getToolTipDisplayDelayTime(Object object) {
 					return R4EUIConstants.TOOLTIP_DISPLAY_DELAY;
 				}
 
 				@Override
 				public int getToolTipTimeDisplayed(Object object) {
 					return R4EUIConstants.TOOLTIP_DISPLAY_TIME;
 				}
 
 				@Override
 				public void update(ViewerCell cell) {
 					final Object element = cell.getElement();
 					if (element instanceof R4EUIFileContext) {
 						//First try target file version
 						R4EFileVersion version = ((R4EUIFileContext) element).getTargetFileVersion();
 						if (null != version) {
 							cell.setText(UIUtils.getProjectPath(version));
 						} else {
 							//Try base file version
 							version = ((R4EUIFileContext) element).getBaseFileVersion();
 							if (null != version) {
 								cell.setText(UIUtils.getProjectPath(version));
 							} else {
 								cell.setText(INVALID_PATH);
 							}
 						}
 					} else {
 						cell.setText(null);
 					}
 				}
 			});
 
 			fPathColumn.getColumn().addListener(SWT.Selection, new Listener() {
 				public void handleEvent(Event event) {
 					fTreeTableComparator.setColumnName(R4EUIConstants.PATH_LABEL);
 					getTree().setSortDirection(fTreeTableComparator.getDirection());
 					getTree().setSortColumn(fPathColumn.getColumn());
 					refresh();
 				}
 			});
 		}
 	}
 
 	/**
 	 * Method createAssignmentColumn.
 	 */
 	private void createAssignmentColumn() {
 		if (null == fAssignColumn) {
 			fAssignColumn = new TreeViewerColumn(this, SWT.NONE);
 			fAssignColumn.getColumn().setText(R4EUIConstants.ASSIGNED_TO_LABEL2);
 			fTreeColumnLayout.setColumnData(fAssignColumn.getColumn(), new ColumnWeightData(0, true));
 			fAssignColumn.getColumn().setMoveable(true);
 			fAssignColumn.getColumn().setResizable(true);
 			fAssignColumn.setLabelProvider(new ColumnLabelProvider() {
 				@Override
 				public String getText(Object element) {
 					if (element instanceof R4EUIReviewItem) {
 						return UIUtils.formatAssignedParticipants(((R4EUIReviewItem) element).getItem().getAssignedTo());
 					} else if (element instanceof R4EUIFileContext) {
 						return UIUtils.formatAssignedParticipants(((R4EUIFileContext) element).getFileContext()
 								.getAssignedTo());
 					}
 					return null;
 				}
 
 				@Override
 				public String getToolTipText(Object element) {
 					return null;
 				}
 
 				@Override
 				public Point getToolTipShift(Object object) {
 					return new Point(R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_X, R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_Y);
 				}
 
 				@Override
 				public int getToolTipDisplayDelayTime(Object object) {
 					return R4EUIConstants.TOOLTIP_DISPLAY_DELAY;
 				}
 
 				@Override
 				public int getToolTipTimeDisplayed(Object object) {
 					return 0;
 				}
 
 				@Override
 				public void update(ViewerCell cell) {
 					final Object element = cell.getElement();
 					if (element instanceof R4EUIReviewItem) {
 						cell.setText(UIUtils.formatAssignedParticipants(((R4EUIReviewItem) element).getItem()
 								.getAssignedTo()));
 					} else if (element instanceof R4EUIFileContext) {
 						cell.setText(UIUtils.formatAssignedParticipants(((R4EUIFileContext) element).getFileContext()
 								.getAssignedTo()));
 					} else {
 						cell.setText(null);
 					}
 				}
 			});
 
 			fAssignColumn.getColumn().addListener(SWT.Selection, new Listener() {
 				public void handleEvent(Event event) {
 					fTreeTableComparator.setColumnName(R4EUIConstants.ASSIGNED_TO_LABEL2);
 					getTree().setSortDirection(fTreeTableComparator.getDirection());
 					getTree().setSortColumn(fAssignColumn.getColumn());
 					refresh();
 				}
 			});
 		}
 	}
 
 	/**
 	 * Method createNumChangesColumn.
 	 */
 	private void createNumChangesColumn() {
 		if (null == fNumChangesColumn) {
 			fNumChangesColumn = new TreeViewerColumn(this, SWT.NONE);
 			fNumChangesColumn.getColumn().setText(R4EUIConstants.CHANGES_LABEL);
 			fTreeColumnLayout.setColumnData(fNumChangesColumn.getColumn(), new ColumnWeightData(0, true));
 			fNumChangesColumn.getColumn().setMoveable(true);
 			fNumChangesColumn.getColumn().setResizable(true);
 			fNumChangesColumn.setLabelProvider(new ColumnLabelProvider() {
 				@Override
 				public String getText(Object element) {
 					if (element instanceof R4EUIReviewItem) {
 						return Integer.toString(((R4EUIReviewItem) element).getNumReviewedChanges())
 								+ R4EUIConstants.SEPARATOR
 								+ Integer.toString(((R4EUIReviewItem) element).getNumChanges());
 					} else if (element instanceof R4EUIFileContext) {
 						return Integer.toString(((R4EUIFileContext) element).getNumChanges());
 					}
 					return null;
 				}
 
 				@Override
 				public String getToolTipText(Object element) {
 					if (element instanceof R4EUIReviewItem) {
 						return NUM_CHANGES_ITEM_COLUMN_TOOLTIP;
 					} else { //Assume File
 						return NUM_CHANGES_FILE_COLUMN_TOOLTIP;
 					}
 				}
 
 				@Override
 				public Point getToolTipShift(Object object) {
 					return new Point(R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_X, R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_Y);
 				}
 
 				@Override
 				public int getToolTipDisplayDelayTime(Object object) {
 					return R4EUIConstants.TOOLTIP_DISPLAY_DELAY;
 				}
 
 				@Override
 				public int getToolTipTimeDisplayed(Object object) {
 					return R4EUIConstants.TOOLTIP_DISPLAY_TIME;
 				}
 
 				@Override
 				public void update(ViewerCell cell) {
 					final Object element = cell.getElement();
 					if (element instanceof R4EUIReviewItem) {
 						cell.setText(Integer.toString(((R4EUIReviewItem) element).getNumReviewedChanges())
 								+ R4EUIConstants.SEPARATOR
 								+ Integer.toString(((R4EUIReviewItem) element).getNumChanges()));
 					} else if (element instanceof R4EUIFileContext) {
 						cell.setText(Integer.toString(((R4EUIFileContext) element).getNumChanges()));
 					} else {
 						cell.setText(null);
 					}
 				}
 			});
 
 			fNumChangesColumn.getColumn().addListener(SWT.Selection, new Listener() {
 				public void handleEvent(Event event) {
 					fTreeTableComparator.setColumnName(R4EUIConstants.CHANGES_LABEL);
 					getTree().setSortDirection(fTreeTableComparator.getDirection());
 					getTree().setSortColumn(fNumChangesColumn.getColumn());
 					refresh();
 				}
 			});
 		}
 	}
 
 	/**
 	 * Method createNumAnomaliesColumn.
 	 */
 	private void createNumAnomaliesColumn() {
 		if (null == fNumAnomaliesColumn) {
 			fNumAnomaliesColumn = new TreeViewerColumn(this, SWT.NONE);
 			fNumAnomaliesColumn.getColumn().setText(R4EUIConstants.ANOMALIES_LABEL);
 			fNumAnomaliesColumn.getColumn().setMoveable(true);
 			fNumAnomaliesColumn.getColumn().setResizable(true);
 			fTreeColumnLayout.setColumnData(fNumAnomaliesColumn.getColumn(), new ColumnWeightData(0, true));
 			fNumAnomaliesColumn.setLabelProvider(new ColumnLabelProvider() {
 				@Override
 				public String getText(Object element) {
 					if (element instanceof R4EUIReviewItem) {
 						return Integer.toString(((R4EUIReviewItem) element).getNumAnomalies());
 					} else if (element instanceof R4EUIFileContext) {
 						return Integer.toString(((R4EUIFileContext) element).getNumAnomalies());
 					}
 					return null;
 				}
 
 				@Override
 				public String getToolTipText(Object element) {
 					return NUM_ANOMALIES_COLUMN_TOOLTIP;
 				}
 
 				@Override
 				public Point getToolTipShift(Object object) {
 					return new Point(R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_X, R4EUIConstants.TOOLTIP_DISPLAY_OFFSET_Y);
 				}
 
 				@Override
 				public int getToolTipDisplayDelayTime(Object object) {
 					return R4EUIConstants.TOOLTIP_DISPLAY_DELAY;
 				}
 
 				@Override
 				public int getToolTipTimeDisplayed(Object object) {
 					return R4EUIConstants.TOOLTIP_DISPLAY_TIME;
 				}
 
 				@Override
 				public void update(ViewerCell cell) {
 					final Object element = cell.getElement();
 					if (element instanceof R4EUIReviewItem) {
 						cell.setText(Integer.toString(((R4EUIReviewItem) element).getNumAnomalies()));
 					} else if (element instanceof R4EUIFileContext) {
 						cell.setText(Integer.toString(((R4EUIFileContext) element).getNumAnomalies()));
 					} else {
 						cell.setText(null);
 					}
 				}
 			});
 
 			fNumAnomaliesColumn.getColumn().addListener(SWT.Selection, new Listener() {
 				public void handleEvent(Event event) {
 					// ignore
 					fTreeTableComparator.setColumnName(R4EUIConstants.ANOMALIES_LABEL);
 					getTree().setSortDirection(fTreeTableComparator.getDirection());
 					getTree().setSortColumn(fNumAnomaliesColumn.getColumn());
 					refresh();
 				}
 			});
 		}
 	}
 }
