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
 package org.eclipse.emf.compare.ui.viewer.content.part;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.compare.diff.metamodel.AddModelElement;
 import org.eclipse.emf.compare.diff.metamodel.AttributeChange;
 import org.eclipse.emf.compare.diff.metamodel.ConflictingDiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChange;
 import org.eclipse.emf.compare.diff.metamodel.RemoteAddModelElement;
 import org.eclipse.emf.compare.diff.metamodel.RemoteRemoveModelElement;
 import org.eclipse.emf.compare.diff.metamodel.RemoveModelElement;
 import org.eclipse.emf.compare.diff.metamodel.util.DiffAdapterFactory;
 import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.metamodel.UnMatchElement;
 import org.eclipse.emf.compare.ui.EMFCompareUIMessages;
 import org.eclipse.emf.compare.ui.ICompareEditorPartListener;
 import org.eclipse.emf.compare.ui.ModelCompareInput;
 import org.eclipse.emf.compare.ui.util.EMFAdapterFactoryProvider;
 import org.eclipse.emf.compare.ui.util.EMFCompareConstants;
 import org.eclipse.emf.compare.ui.util.EMFCompareEObjectUtils;
 import org.eclipse.emf.compare.ui.viewer.content.ModelContentMergeViewer;
 import org.eclipse.emf.compare.ui.viewer.content.part.property.ModelContentMergePropertyPart;
 import org.eclipse.emf.compare.ui.viewer.content.part.property.PropertyContentProvider;
 import org.eclipse.emf.compare.ui.viewer.content.part.tree.ModelContentMergeTreePart;
 import org.eclipse.emf.compare.util.EMFCompareMap;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.TreeEvent;
 import org.eclipse.swt.events.TreeListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * Describes a part of a {@link ModelContentMergeViewer}.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class ModelContentMergeViewerPart {
 	/** This {@link String} is used as an error message when an unexisting tab is accessed. */
 	private static final String INVALID_TAB = EMFCompareUIMessages.getString("IllegalTab"); //$NON-NLS-1$
 
 	/** This keeps track of the parent viewer of this viewer part. */
 	protected final ModelContentMergeViewer parentViewer;
 
 	/**
 	 * This <code>int</code> represents the side of this viewer part. Must be one of
 	 * <ul>
 	 * <li>{@link EMFCompareConstants#RIGHT}</li>
 	 * <li>{@link EMFCompareConstants#LEFT}</li>
 	 * <li>{@link EMFCompareConstants#ANCESTOR}</li>
 	 * </ul>
 	 */
 	protected final int partSide;
 
 	/** This is the content of the properties tab for this viewer part. */
 	protected ModelContentMergePropertyPart properties;
 
 	/** This is the view displayed by this viewer part. */
 	protected CTabFolder tabFolder;
 
 	/** This is the content of the tree tab for this viewer part. */
 	protected ModelContentMergeTreePart tree;
 
 	/** Indicates that the tree has been expanded since last time we mapped the TreeItems. */
 	/* package */boolean expanded = true;
 
 	/** This contains all the listeners registered for this viewer part. */
 	private final List<ICompareEditorPartListener> editorPartListeners = new ArrayList<ICompareEditorPartListener>();
 
 	/**
 	 * Currently selected tab for this viewer. Must be one of
 	 * <ul>
 	 * <li>{@link ModelContentMergeViewer#TREE_TAB}</li>
 	 * <li>{@link ModelContentMergeViewer#PROPERTIES_TAB}</li>
 	 * </ul>
 	 */
 	private int selectedTab;
 
 	/** This Map will allow us to avoid iteration through all diffs for each paint operation. */
 	private final Map<String, DiffElement> treeItemToDiff = new EMFCompareMap<String, DiffElement>(101);
 
 	/**
 	 * Instantiates a {@link ModelContentMergeViewerPart} given its parent {@link Composite} and its side.
 	 * 
 	 * @param viewer
 	 *            Parent viewer of this viewer part.
 	 * @param composite
 	 *            Parent {@link Composite} for this part.
 	 * @param side
 	 *            Comparison side of this part. Must be one of
 	 *            {@link EMFCompareConstants#LEFT EMFCompareConstants.RIGHT},
 	 *            {@link EMFCompareConstants#RIGHT EMFCompareConstants.LEFT} or
 	 *            {@link EMFCompareConstants#ANCESTOR EMFCompareConstants.ANCESTOR}.
 	 */
 	public ModelContentMergeViewerPart(ModelContentMergeViewer viewer, Composite composite, int side) {
 		if (side != EMFCompareConstants.RIGHT && side != EMFCompareConstants.LEFT
 				&& side != EMFCompareConstants.ANCESTOR)
 			throw new IllegalArgumentException(EMFCompareUIMessages.getString("IllegalSide", side)); //$NON-NLS-1$
 
 		parentViewer = viewer;
 		selectedTab = ModelContentMergeViewer.TREE_TAB;
 		partSide = side;
 		createContents(composite);
 	}
 
 	/**
 	 * Registers the given listener for notification. If the identical listener is already registered the
 	 * method has no effect.
 	 * 
 	 * @param listener
 	 *            The listener to register for changes of this input.
 	 */
 	public void addCompareEditorPartListener(ICompareEditorPartListener listener) {
 		editorPartListeners.add(listener);
 	}
 
 	/**
 	 * Creates the contents of this viewer part given its parent composite.
 	 * 
 	 * @param composite
 	 *            Parent composite of this viewer parts's widgets.
 	 */
 	public void createContents(Composite composite) {
 		tabFolder = new CTabFolder(composite, SWT.BOTTOM);
 		final CTabItem treeTab = new CTabItem(tabFolder, SWT.NONE);
 		treeTab.setText(EMFCompareUIMessages.getString("ModelContentMergeViewerPart.tab1.name")); //$NON-NLS-1$
 
 		final CTabItem propertiesTab = new CTabItem(tabFolder, SWT.NONE);
 		propertiesTab.setText(EMFCompareUIMessages.getString("ModelContentMergeViewerPart.tab2.name")); //$NON-NLS-1$
 
 		final Composite treePanel = new Composite(tabFolder, SWT.NONE);
 		treePanel.setLayout(new GridLayout());
 		treePanel.setLayoutData(new GridData(GridData.FILL_BOTH));
 		treePanel.setFont(composite.getFont());
 		tree = createTreePart(treePanel);
 		treeTab.setControl(treePanel);
 
 		final Composite propertyPanel = new Composite(tabFolder, SWT.NONE);
 		propertyPanel.setLayout(new GridLayout());
 		propertyPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
 		propertyPanel.setFont(composite.getFont());
 		properties = createPropertiesPart(propertyPanel);
 		propertiesTab.setControl(propertyPanel);
 
 		tabFolder.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				if (e.item.equals(treeTab)) {
 					setSelectedTab(ModelContentMergeViewer.TREE_TAB);
 				} else {
 					if (e.item.equals(propertiesTab)) {
 						setSelectedTab(ModelContentMergeViewer.PROPERTIES_TAB);
 					}
 				}
 				fireSelectedtabChanged();
 			}
 		});
 
 		tabFolder.setSelection(treeTab);
 	}
 
 	/**
 	 * Returns the {@link Widget} representing the given element or <code>null</code> if it cannot be found.
 	 * 
 	 * @param element
 	 *            Element to find the {@link Widget} for.
 	 * @return The {@link Widget} representing the given element.
 	 */
 	public Widget find(EObject element) {
 		Widget widget = null;
 		if (element != null) {
 			if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 				widget = tree.find(element);
 			} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 				if (element instanceof DiffElement)
 					widget = properties.find(((PropertyContentProvider)properties.getContentProvider())
 							.getInputEObject(), (DiffElement)element);
 			} else {
 				throw new IllegalStateException(EMFCompareUIMessages.getString(INVALID_TAB, selectedTab));
 			}
 		}
 		return widget;
 	}
 
 	/**
 	 * This will return the {@link DiffElement} for a given {@link Item} or <code>null</code> if that
 	 * particular item doesn't map to a difference.
 	 * 
 	 * @param item
 	 *            {@link Item} we seek the {@link DiffElement} for.
 	 * @return The {@link DiffElement} for a given {@link Item} or <code>null</code> if that particular item
 	 *         doesn't map to a difference.
 	 */
 	public DiffElement findDiffForTreeItem(Item item) {
 		final StringBuilder itemKey = new StringBuilder();
 		itemKey.append(partSide).append(item.getText()).append(item.hashCode());
 
 		if (expanded)
 			mapTreeItemToDiff();
 
 		return treeItemToDiff.get(itemKey.toString());
 	}
 
 	/**
 	 * Returns the properties part of this viewer part.
 	 * 
 	 * @return The properties part of this viewer part.
 	 */
 	public ModelContentMergePropertyPart getPropertyPart() {
 		return properties;
 	}
 
 	/**
 	 * Returns the height of the tab control's header.
 	 * 
 	 * @return The height of the tab control's header.
 	 */
 	public int getHeaderHeight() {
 		int headerHeight = 0;
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			headerHeight = tree.getTree().getHeaderHeight();
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			headerHeight = properties.getTable().getHeaderHeight();
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 		return headerHeight;
 	}
 
 	/**
 	 * Returns a list of the selected tab's selected Elements.
 	 * 
 	 * @return The selected tab's selected Elements.
 	 */
 	public List<TreeItem> getSelectedElements() {
 		List<TreeItem> selectedElements = null;
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			selectedElements = tree.getSelectedElements();
 		} else if (selectedTab != ModelContentMergeViewer.PROPERTIES_TAB) {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 		return selectedElements;
 	}
 
 	/**
 	 * Returns the width of the columns shown on the properties tab.
 	 * 
 	 * @return The width of the columns shown on the properties tab.
 	 */
 	public int getTotalColumnsWidth() {
 		int width = 0;
 		if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			for (final TableColumn col : properties.getTable().getColumns()) {
 				width += col.getWidth();
 			}
 		}
 		return width;
 	}
 
 	/**
 	 * Returns the tree part of this viewer part.
 	 * 
 	 * @return The tree part of this viewer part.
 	 */
 	public ModelContentMergeTreePart getTreePart() {
 		return tree;
 	}
 
 	/**
 	 * Returns the first root of the tree.
 	 * 
 	 * @return The first root of the tree.
 	 */
 	public TreeItem getTreeRoot() {
 		if (tree.getVisibleElements().size() > 0)
 			return tree.getVisibleElements().get(0);
 		return null;
 	}
 
 	/**
 	 * Checks wether a given {@link Item} is visible.
 	 * 
 	 * @param item
 	 *            Item to check.
 	 * @return <code>True</code> if the item is visible, <code>False</code> otherwise.
 	 */
 	public boolean isVisible(Item item) {
 		boolean visible = false;
 		if (item instanceof TreeItem) {
 			final TreeItem treeItem = (TreeItem)item;
 			// First we check that the treeItem is contained within the tree's client area
 			visible = tree.getTree().getClientArea().contains(treeItem.getBounds().x, treeItem.getBounds().y);
 			// Then we check that the treeItem's parent is expanded
 			if (visible && treeItem.getParentItem() != null)
 				visible = treeItem.getParentItem().getExpanded();
 		}
 		return visible;
 	}
 
 	/**
 	 * Redraws this viewer part.
 	 */
 	public void layout() {
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			tree.getTree().redraw();
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			properties.getTable().redraw();
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 	}
 
 	/**
 	 * Shows the given item on the tree tab or its properties on the property tab.
 	 * 
 	 * @param diff
 	 *            Item to scroll to.
 	 */
 	public void navigateToDiff(DiffElement diff) {
 		final List<DiffElement> diffs = new ArrayList<DiffElement>();
 		diffs.add(diff);
 		navigateToDiff(diffs);
 	}
 	
 	/**
 	 * Ensures the first item of the given list of {@link DiffElement}s is visible, and sets the selection of the tree to all those items.
 	 * 
 	 * @param diffs
 	 *            Items to select.
 	 */
 	public void navigateToDiff(List<DiffElement> diffs) {
 		EObject target = null;
 		if (partSide == EMFCompareConstants.LEFT) {
 			target = EMFCompareEObjectUtils.getLeftElement(diffs.get(0));
 			final TreeItem treeItem = (TreeItem)find(target);
 			if (diffs.get(0) instanceof AddModelElement && treeItem != null)
 				treeItem.setExpanded(true);
 		} else if (partSide == EMFCompareConstants.RIGHT) {
 			target = EMFCompareEObjectUtils.getRightElement(diffs.get(0));
 			final TreeItem treeItem = (TreeItem)find(target);
 			if (diffs.get(0) instanceof RemoveModelElement && treeItem != null)
 				treeItem.setExpanded(true);
 		} else if (partSide == EMFCompareConstants.ANCESTOR) {
 			target = EMFCompareEObjectUtils.getAncestorElement(diffs.get(0).eContainer());
 		}
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			tree.showItem(getTreeItemsDataFor(diffs));
 			properties.setInput(findMatchFromElement(target));
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			properties.setInput(findMatchFromElement(target));
 			properties.showItem(((PropertyContentProvider)properties.getContentProvider()).getInputEObject(),
 					diffs.get(0));
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 		parentViewer.getConfiguration().setProperty(EMFCompareConstants.PROPERTY_CONTENT_SELECTION, diffs.get(0));
 		parentViewer.updateCenter();
 		// We'll assume the tree has been expanded or collapsed during the process
 		expanded = true;
 	}
 	
 	/**
 	 * Retrieve the list of tree items data corresponding to the given {@link DiffElement}s.
 	 * @param diffs {@link DiffElement}s we seek the tree items for.
 	 * @return The list of tree items corresponding to the given {@link DiffElement}s.
 	 */
 	public List<EObject> getTreeItemsDataFor(List<DiffElement> diffs) {
 		final List<EObject> result = new ArrayList<EObject>(diffs.size());
 		for (DiffElement diff : diffs) {
 			if (partSide == EMFCompareConstants.RIGHT) {
 				final EObject data = EMFCompareEObjectUtils.getRightElement(diff);
 				if (data != null && !result.contains(data))
 					result.add(data);
 			} else if (partSide == EMFCompareConstants.LEFT) {
 				final EObject data = EMFCompareEObjectUtils.getLeftElement(diff);
 				if (data != null && !result.contains(data))
 					result.add(data);
 			} else if (partSide == EMFCompareConstants.ANCESTOR) {
 				final EObject data = EMFCompareEObjectUtils.getRightElement(diff);
 				if (data != null && !result.contains(data))
 					result.add(data);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Sets the receiver's size and location to the rectangular area specified by the arguments.
 	 * 
 	 * @param x
 	 *            Desired x coordinate of the part.
 	 * @param y
 	 *            Desired y coordinate of the part.
 	 * @param width
 	 *            Desired width of the part.
 	 * @param height
 	 *            Desired height of the part.
 	 */
 	public void setBounds(int x, int y, int width, int height) {
 		setBounds(new Rectangle(x, y, width, height));
 	}
 
 	/**
 	 * Sets the receiver's size and location to given rectangular area.
 	 * 
 	 * @param bounds
 	 *            Desired bounds for this receiver.
 	 */
 	public void setBounds(Rectangle bounds) {
 		tabFolder.setBounds(bounds);
 		resizeBounds();
 	}
 
 	/**
 	 * Sets the input of this viewer part.
 	 * 
 	 * @param input
 	 *            New input of this viewer part.
 	 */
 	public void setInput(Object input) {
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			tree.setReflectiveInput((EObject)input);
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			properties.setInput(input);
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 	}
 
 	/**
 	 * Changes the current tab.
 	 * 
 	 * @param index
 	 *            New tab to set selected.
 	 */
 	public void setSelectedTab(int index) {
 		selectedTab = index;
 		tabFolder.setSelection(selectedTab);
 		resizeBounds();
 	}
 
 	/**
 	 * Returns the {@link Match2Elements} containing the given {@link EObject} as its left or right element.
 	 * 
 	 * @param element
 	 *            Element we seek the {@link Match2Elements} for.
 	 * @return The {@link Match2Elements} containing the given {@link EObject} as its left or right element.
 	 */
 	protected Object findMatchFromElement(EObject element) {
 		Object theElement = null;
 		final MatchModel match = ((ModelCompareInput)parentViewer.getInput()).getMatch();
 
 		for (final TreeIterator iterator = match.eAllContents(); iterator.hasNext(); ) {
 			final Object object = iterator.next();
 
 			if (object instanceof Match2Elements) {
 				final Match2Elements matchElement = (Match2Elements)object;
 				if (matchElement.getLeftElement().equals(element)
 						|| matchElement.getRightElement().equals(element)) {
 					theElement = matchElement;
 				}
 			} else if (object instanceof UnMatchElement) {
 				final UnMatchElement matchElement = (UnMatchElement)object;
 				if (matchElement.getElement().equals(element)) {
 					theElement = matchElement;
 				}
 			}
 		}
 
 		return theElement;
 	}
 
 	/**
 	 * Notifies All {@link ICompareEditorPartListener listeners} registered for this viewer part that the tab
 	 * selection has been changed.
 	 */
 	protected void fireSelectedtabChanged() {
 		for (ICompareEditorPartListener listener : editorPartListeners) {
 			listener.selectedTabChanged(selectedTab);
 		}
 	}
 
 	/**
 	 * Notifies All {@link ICompareEditorPartListener listeners} registered for this viewer part that the user
 	 * selection has changed on the properties or tree tab.
 	 * 
 	 * @param event
 	 *            Source {@link SelectionChangedEvent Selection changed event} of the notification.
 	 */
 	protected void fireSelectionChanged(SelectionChangedEvent event) {
 		for (ICompareEditorPartListener listener : editorPartListeners) {
 			listener.selectionChanged(event);
 		}
 	}
 
 	/**
 	 * Notifies All {@link ICompareEditorPartListener listeners} registered for this viewer part that the
 	 * center part needs to be refreshed.
 	 */
 	protected void fireUpdateCenter() {
 		for (ICompareEditorPartListener listener : editorPartListeners) {
 			listener.updateCenter();
 		}
 	}
 
 	/**
 	 * Maps TreeItems to DiffElements. Called each time a {@link TreeItem} is expanded.
 	 */
 	protected void mapTreeItemToDiff() {
 		final List<DiffElement> diffList = ((ModelCompareInput)parentViewer.getInput()).getDiffAsList();
 		treeItemToDiff.clear();
 
 		for (final DiffElement diff : diffList) {
 			final Item storedItem;
 			if (partSide == EMFCompareConstants.LEFT)
 				storedItem = parentViewer.getLeftItem(diff);
 			else if (partSide == EMFCompareConstants.RIGHT)
 				storedItem = parentViewer.getRightItem(diff);
 			else
 				storedItem = parentViewer.getAncestorItem(diff);
 
 			final StringBuilder diffItemKey = new StringBuilder();
 			diffItemKey.append(partSide).append(storedItem.getText()).append(storedItem.hashCode());
 			treeItemToDiff.put(diffItemKey.toString(), diff);
 		}
 
 		expanded = false;
 	}
 
 	/**
 	 * This will resize the tabs displayed by this content merge viewer.
 	 */
 	protected void resizeBounds() {
 		if (selectedTab == ModelContentMergeViewer.TREE_TAB) {
 			tree.getTree().setBounds(tabFolder.getClientArea());
 		} else if (selectedTab == ModelContentMergeViewer.PROPERTIES_TAB) {
 			properties.getTable().setBounds(tabFolder.getClientArea());
 		} else {
 			throw new IllegalStateException(INVALID_TAB);
 		}
 	}
 
 	/**
 	 * Handles the creation of the properties tab of this viewer part given the parent {@link Composite} under
 	 * which to create it.
 	 * 
 	 * @param composite
 	 *            Parent {@link Composite} of the table to create.
 	 * @return The properties part displayed by this viewer part's properties tab.
 	 */
 	private ModelContentMergePropertyPart createPropertiesPart(Composite composite) {
 		final ModelContentMergePropertyPart propertiesPart = new ModelContentMergePropertyPart(composite,
 				SWT.NONE, partSide);
 
 		propertiesPart.setContentProvider(new PropertyContentProvider());
 		propertiesPart.getTable().setHeaderVisible(true);
 		propertiesPart.getTable().addPaintListener(new PropertyPaintListener());
 
 		propertiesPart.getTable().getVerticalBar().addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				parentViewer.updateCenter();
 			}
 		});
 
 		propertiesPart.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				fireSelectionChanged(event);
 			}
 		});
 
 		return propertiesPart;
 
 	}
 
 	/**
 	 * Handles the creation of the tree tab of this viewer part given the parent {@link Composite} under which
 	 * to create it.
 	 * 
 	 * @param composite
 	 *            Parent {@link Composite} of the tree to create.
 	 * @return The tree part displayed by this viewer part's tree tab.
 	 */
 	private ModelContentMergeTreePart createTreePart(Composite composite) {
 		final ModelContentMergeTreePart treePart = new ModelContentMergeTreePart(composite);
 
 		treePart.setContentProvider(new AdapterFactoryContentProvider(EMFAdapterFactoryProvider
 				.getAdapterFactory()));
 
 		treePart.getTree().addPaintListener(new TreePaintListener());
 
 		treePart.getTree().getVerticalBar().addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				fireUpdateCenter();
 			}
 		});
 
 		treePart.getTree().addTreeListener(new TreeListener() {
 			public void treeCollapsed(TreeEvent e) {
 				((TreeItem)e.item).setExpanded(false);
 				e.doit = false;
 				parentViewer.update();
 				expanded = true;
 			}
 
 			public void treeExpanded(TreeEvent e) {
 				((TreeItem)e.item).setExpanded(true);
 				e.doit = false;
 				parentViewer.update();
 				expanded = true;
 			}
 		});
 
 		treePart.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				fireSelectionChanged(event);
 			}
 		});
 
 		treePart.getTree().addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (tree.getSelectedElements().size() > 0) {
 					final TreeItem selected = tree.getSelectedElements().get(0);
 					for (final DiffElement diff : ((ModelCompareInput)parentViewer.getInput())
 							.getDiffAsList()) {
 						if (!(diff instanceof DiffGroup) && partSide == EMFCompareConstants.LEFT) {
 							if (selected.getData().equals(EMFCompareEObjectUtils.getLeftElement(diff)))
 								parentViewer.setSelection(diff);
 						} else if (!(diff instanceof DiffGroup) && partSide == EMFCompareConstants.RIGHT) {
 							if (selected.getData().equals(EMFCompareEObjectUtils.getRightElement(diff)))
 								parentViewer.setSelection(diff);
 						}
 					}
 					if (!selected.isDisposed() && selected.getData() instanceof EObject)
 						properties.setInput(findMatchFromElement((EObject)selected.getData()));
 				}
 			}
 		});
 
 		return treePart;
 	}
 
 	/**
 	 * This implementation of {@link PaintListener} handles the drawing of blocks around modified members in
 	 * the properties tab.
 	 */
 	class PropertyPaintListener implements PaintListener {
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
 		 */
 		public void paintControl(PaintEvent event) {
 			if (parentViewer.shouldDrawDiffMarkers()) {
 				for (final DiffElement diff : ((ModelCompareInput)parentViewer.getInput()).getDiffAsList()) {
 					if ((diff instanceof AttributeChange || diff instanceof ReferenceChange)
 							&& find(diff) != null && partSide == EMFCompareConstants.LEFT) {
 						drawLine(event, (TableItem)parentViewer.getLeftItem(diff));
 					}
 				}
 			}
 		}
 
 		/**
 		 * Handles the drawing itself.
 		 * 
 		 * @param event
 		 *            {@link PaintEvent} that triggered this operation.
 		 * @param tableItem
 		 *            Item we want connected to the center part.
 		 */
 		private void drawLine(PaintEvent event, TableItem tableItem) {
 			final Rectangle tableBounds = properties.getTable().getBounds();
 			final Rectangle tableItemBounds = tableItem.getBounds();
 			tableItem.setBackground(new Color(tableItem.getDisplay(), parentViewer.getHighlightColor()));
 
 			final int lineY = tableItemBounds.y + tableItemBounds.height / 2;
 
 			event.gc.setLineWidth(2);
 			event.gc.setForeground(new Color(tableItem.getDisplay(), parentViewer.getChangedColor()));
 			event.gc.drawLine(getTotalColumnsWidth(), lineY, tableBounds.width, lineY);
 		}
 	}
 
 	/**
 	 * This implementation of {@link PaintListener} handles the drawing of blocks around modified members in
 	 * the tree tab.
 	 */
 	class TreePaintListener implements PaintListener {
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
 		 */
 		public void paintControl(PaintEvent event) {
 			// This will avoid strange random resize behavior on linux OS
 			if (tree.getTree().getBounds() != tabFolder.getClientArea())
 				resizeBounds();
 			if (parentViewer.shouldDrawDiffMarkers()) {
 				for (TreeItem item : tree.getVisibleElements()) {
 					final DiffElement element = findDiffForTreeItem(item);
 					if (element != null)
 						drawRectangle(event, item, element);
 				}
 			}
 		}
 
 		/**
 		 * Handles the drawing itself.
 		 * 
 		 * @param event
 		 *            {@link PaintEvent} that triggered this operation.
 		 * @param treeItem
 		 *            {@link TreeItem} that need to be circled and connected to the center part.
 		 * @param diff
 		 *            {@link DiffElement} we're circling this {@link TreeItem} for.
 		 */
 		private void drawRectangle(PaintEvent event, TreeItem treeItem, DiffElement diff) {
 			final Rectangle treeBounds = tree.getTree().getClientArea();
 			final Rectangle treeItemBounds = treeItem.getBounds();
 
 			if (DiffAdapterFactory.shouldBeHidden(diff))
 				return;
 
 			// Defines the circling Color
 			RGB color = parentViewer.getChangedColor();
 			if (diff instanceof ConflictingDiffElement || diff.eContainer() instanceof ConflictingDiffElement) {
 				color = parentViewer.getConflictingColor();
 			} else if (diff instanceof AddModelElement || diff instanceof RemoteAddModelElement) {
 				color = parentViewer.getAddedColor();
 			} else if (diff instanceof RemoveModelElement || diff instanceof RemoteRemoveModelElement) {
 				color = parentViewer.getRemovedColor();
 			}
 
 			/*
 			 * We add a margin before the rectangle to circle the "+" as well as the tree line.
 			 */
 			final int margin = 60;
 
 			// Defines all variables needed for drawing the rectangle.
 			final int rectangleX = treeItemBounds.x - margin;
 			final int rectangleY = treeItemBounds.y;
 			final int rectangleWidth = treeItemBounds.width + margin;
 			final int rectangleHeight = treeItemBounds.height - 1;
 			final int rectangleArcWidth = 5;
 			final int rectangleArcHeight = 5;
 
 			int lineWidth = 1;
 			// if the item is selected, we set a bigger line width
 			if (getSelectedElements().contains(treeItem)) {
 				lineWidth = 2;
 			}
 
 			// Performs the actual drawing
 			event.gc.setLineWidth(lineWidth);
 			event.gc.setForeground(new Color(treeItem.getDisplay(), color));
 			if (partSide == EMFCompareConstants.LEFT) {
 				if (!treeItem.getData().equals(EMFCompareEObjectUtils.getLeftElement(diff))
 						|| diff instanceof AddModelElement || diff instanceof RemoteRemoveModelElement) {
 					event.gc.setLineStyle(SWT.LINE_SOLID);
					event.gc.drawLine(rectangleX, rectangleY + rectangleHeight, treeBounds.width, rectangleY
 							+ rectangleHeight);
 				} else {
 					event.gc.setLineStyle(SWT.LINE_DASHDOT);
 					event.gc.drawRoundRectangle(rectangleX, rectangleY, rectangleWidth, rectangleHeight,
 							rectangleArcWidth, rectangleArcHeight);
 					event.gc.setLineStyle(SWT.LINE_SOLID);
 					event.gc.drawLine(rectangleX + rectangleWidth, rectangleY + rectangleHeight / 2,
							treeBounds.width, rectangleY + rectangleHeight / 2);
 				}
 			} else if (partSide == EMFCompareConstants.RIGHT) {
 				if (!treeItem.getData().equals(EMFCompareEObjectUtils.getRightElement(diff))
 						|| diff instanceof RemoveModelElement || diff instanceof RemoteAddModelElement) {
 					event.gc.setLineStyle(SWT.LINE_SOLID);
 					event.gc.drawLine(rectangleX + rectangleWidth, rectangleY + rectangleHeight,
 							treeBounds.x, rectangleY + rectangleHeight);
 				} else {
 					event.gc.setLineStyle(SWT.LINE_DASHDOT);
 					event.gc.drawRoundRectangle(rectangleX, rectangleY, rectangleWidth, rectangleHeight,
 							rectangleArcWidth, rectangleArcHeight);
 					event.gc.setLineStyle(SWT.LINE_SOLID);
 					event.gc.drawLine(rectangleX, rectangleY + rectangleHeight / 2, treeBounds.x, rectangleY
 							+ rectangleHeight / 2);
 				}
 			} else {
 				if (!treeItem.getData().equals(EMFCompareEObjectUtils.getAncestorElement(diff))) {
 					event.gc.setLineStyle(SWT.LINE_SOLID);
 					event.gc.drawLine(rectangleX + rectangleWidth, rectangleY + rectangleHeight, rectangleX,
 							rectangleY + rectangleHeight);
 				} else {
 					event.gc.setLineStyle(SWT.LINE_DASHDOT);
 					event.gc.drawRoundRectangle(rectangleX, rectangleY, rectangleWidth, rectangleHeight,
 							rectangleArcWidth, rectangleArcHeight);
 				}
 			}
 		}
 	}
 }
