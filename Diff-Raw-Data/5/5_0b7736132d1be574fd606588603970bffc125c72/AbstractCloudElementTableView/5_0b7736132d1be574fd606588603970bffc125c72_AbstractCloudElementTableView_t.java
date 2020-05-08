 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc..
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Incorporated - initial API and implementation
  *******************************************************************************/
 package org.jboss.tools.deltacloud.ui.views.cloudelements;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.jboss.tools.deltacloud.core.DeltaCloud;
 import org.jboss.tools.deltacloud.core.DeltaCloudException;
 import org.jboss.tools.deltacloud.core.DeltaCloudManager;
 import org.jboss.tools.deltacloud.core.IDeltaCloudElement;
 import org.jboss.tools.deltacloud.core.IDeltaCloudManagerListener;
 import org.jboss.tools.deltacloud.core.IInstanceFilter;
 import org.jboss.tools.deltacloud.ui.Activator;
 import org.jboss.tools.deltacloud.ui.ErrorUtils;
 import org.jboss.tools.deltacloud.ui.views.CVMessages;
 import org.jboss.tools.deltacloud.ui.views.Columns;
 import org.jboss.tools.deltacloud.ui.views.Columns.Column;
 import org.jboss.tools.internal.deltacloud.ui.preferences.StringPreferenceValue;
 import org.jboss.tools.internal.deltacloud.ui.utils.UIUtils;
 
 /**
  * A common superclass for viewers that operate on IDeltaCloudElements
  * (currently DeltaCloudImage and DeltaCloudInstance)
  * 
  * @see InstanceView
  * @see ImageView
  * 
  * @author Jeff Johnston
  * @author Andre Dietisheim
  */
 public abstract class AbstractCloudElementTableView<CLOUDELEMENT extends IDeltaCloudElement> extends ViewPart implements
 		IDeltaCloudManagerListener, PropertyChangeListener, IAdaptable {
 
 	private final static String CLOUD_SELECTOR_LABEL = "CloudSelector.label"; //$NON-NLS-1$
 
 	private static final String FILTERED_LABEL = "Filtered.label"; //$NON-NLS-1$
 	private static final String FILTERED_TOOLTIP = "FilteredImages.tooltip"; //$NON-NLS-1$	
 
 	private Combo currentCloudSelector;
 	private Label currentCloudSelectorLabel;
 	private TableViewer viewer;
 	private DeltaCloud currentCloud;
 	private StringPreferenceValue lastSelectedCloudPref;
 	private Composite container;
 
 	private ModifyListener currentCloudModifyListener = new ModifyListener() {
 
 		@Override
 		public void modifyText(ModifyEvent e) {
 			int index = currentCloudSelector.getSelectionIndex();
 			if (index < 0) {
 				return;
 			}
 
 			DeltaCloud newSelectedCloud = getCloud(index, getClouds());
 			if (isNewCloud(newSelectedCloud)) {
 				removePropertyChangeListener(currentCloud);
 				currentCloud = newSelectedCloud;
 				addPropertyChangeListener(currentCloud);
 				lastSelectedCloudPref.store(currentCloud.getName());
 				Display.getCurrent().asyncExec(new Runnable() {
 
 					@Override
 					public void run() {
 						setViewerInput(currentCloud);
 					}
 				});
 			}
 		}
 	};
 
 	private ISelectionListener workbenchSelectionListener = new ISelectionListener() {
 
 		@Override
 		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 			// we want to listen to selection changes in the deltacloud view
 			// only
 			DeltaCloud cloud = UIUtils.getFirstAdaptedElement(selection, DeltaCloud.class);
 			if (isNewCloud(cloud)) {
 				int index = getCloudIndex(cloud, getClouds());
 				currentCloudSelector.select(index);
 			}
 		}
 	};
 
 	private class ColumnListener extends SelectionAdapter {
 
 		private int column;
 
 		public ColumnListener(int column) {
 			this.column = column;
 		}
 
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			TableViewerColumnComparator comparator = (TableViewerColumnComparator) viewer.getComparator();
 			Table t = viewer.getTable();
 			if (comparator.getColumn() == column) {
 				comparator.reverseDirection();
 			}
 			comparator.setColumn(column);
 			TableColumn tc = (TableColumn) e.getSource();
 			t.setSortColumn(tc);
 			t.setSortDirection(SWT.NONE);
 			viewer.refresh();
 		}
 	};
 
 	public AbstractCloudElementTableView() {
 		lastSelectedCloudPref = new StringPreferenceValue(getSelectedCloudPrefsKey(), Activator.PLUGIN_ID);
 	}
 
 	private boolean isNewCloud(DeltaCloud cloud) {
 		if (cloud == null) {
 			return false;
 		}
 
 		return currentCloud == null
 				|| !currentCloud.equals(cloud);
 	}
 
 	protected abstract String getSelectedCloudPrefsKey();
 
 	@Override
 	public void createPartControl(Composite parent) {
 		this.container = new Composite(parent, SWT.NULL);
 		FormLayout layout = new FormLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		container.setLayout(layout);
 
 		DeltaCloud[] clouds = getClouds();
 
 		createCloudSelector(container);
 		initCloudSelector(lastSelectedCloudPref.get(), currentCloudSelector, clouds);
 
 		Label filterLabel = new Label(container, SWT.NULL);
 		filterLabel.setText(CVMessages.getString(FILTERED_LABEL));
 		filterLabel.setToolTipText(CVMessages.getString(FILTERED_TOOLTIP));
 
 		Composite tableArea = new Composite(container, SWT.NULL);
 		viewer = createTableViewer(tableArea);
 
 		currentCloud = getCloud(currentCloudSelector.getSelectionIndex(), clouds);
 		addPropertyChangeListener(currentCloud);
 		setViewerInput(currentCloud);
 		setFilterLabelVisible(currentCloud, filterLabel);
 
 		Point p1 = currentCloudSelectorLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 		Point p2 = currentCloudSelector.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 		int centering = (p2.y - p1.y + 1) / 2;
 
 		FormData f = new FormData();
 		f.top = new FormAttachment(0, 5 + centering);
 		f.left = new FormAttachment(0, 30);
 		currentCloudSelectorLabel.setLayoutData(f);
 
 		f = new FormData();
 		f.top = new FormAttachment(0, 5);
 		f.left = new FormAttachment(currentCloudSelectorLabel, 5);
 		currentCloudSelector.setLayoutData(f);
 
 		f = new FormData();
 		f.top = new FormAttachment(0, 5 + centering);
 		f.right = new FormAttachment(100, -10);
 		filterLabel.setLayoutData(f);
 
 		f = new FormData();
 		f.top = new FormAttachment(currentCloudSelector, 8);
 		f.left = new FormAttachment(0, 0);
 		f.right = new FormAttachment(100, 0);
 		f.bottom = new FormAttachment(100, 0);
 		tableArea.setLayoutData(f);
 
 		// Create the help context id for the viewer's control
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "org.jboss.tools.deltacloud.ui.viewer");
 		hookContextMenu(viewer.getControl());
 		getSite().setSelectionProvider(viewer);
 		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(workbenchSelectionListener);
 
 		DeltaCloudManager.getDefault().addCloudManagerListener(this);
 	}
 
 	private TableViewer createTableViewer(Composite tableArea) {
 		TableColumnLayout tableLayout = new TableColumnLayout();
 		tableArea.setLayout(tableLayout);
 
 		TableViewer viewer = new TableViewer(tableArea,
 				SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
 		Table table = viewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		ITableContentAndLabelProvider<CLOUDELEMENT> provider = getContentAndLabelProvider();
 		viewer.setContentProvider(provider);
 		viewer.setLabelProvider(provider);
 		createColumns(provider, tableLayout, table);
 
 		viewer.setComparator(new TableViewerColumnComparator());
 		table.setSortDirection(SWT.NONE);
 
 		return viewer;
 	}
 
 	protected abstract ITableContentAndLabelProvider<CLOUDELEMENT> getContentAndLabelProvider();
 
 	private void setViewerInput(DeltaCloud cloud) {
 		viewer.setInput(cloud);
 	}
 
 	/**
 	 * Gets the clouds that are available in the model.
 	 * 
 	 * @return the clouds
 	 */
 	private DeltaCloud[] getClouds() {
 		DeltaCloud[] clouds = new DeltaCloud[] {};
 		try {
 			clouds = DeltaCloudManager.getDefault().getClouds();
 		} catch (DeltaCloudException e) {
 			// TODO: internationalize strings
 			ErrorUtils.handleError(
 					"Error",
 					"Could not get all clouds",
 					e, Display.getDefault().getActiveShell());
 		}
 		return clouds;
 	}
 
 	private int getCloudIndex(DeltaCloud cloud, DeltaCloud[] clouds) {
 		if (cloud == null) {
 			return 0;
 		}
 		return getCloudIndex(cloud.getName(), clouds);
 	}
 
 	private int getCloudIndex(String cloudName, DeltaCloud[] clouds) {
 		int index = 0;
 		if (cloudName != null
 				&& cloudName.length() > 0
 				&& clouds.length > 0) {
 			for (int i = 0; i < clouds.length; i++) {
 				DeltaCloud cloud = clouds[i];
 				if (cloudName != null && cloudName.equals(cloud.getName())) {
 					index = i;
 					break;
 				}
 			}
 		}
 		return index;
 	}
 
 	private void setFilterLabelVisible(DeltaCloud currentCloud, Label filterLabel) {
 		if (currentCloud == null) {
 			filterLabel.setVisible(false);
 			return;
 		}
 
 		IInstanceFilter filter = currentCloud.getInstanceFilter();
		filterLabel.setVisible(!filter.isFiltering());
 	}
 
 	private DeltaCloud getCloud(int cloudIndex, DeltaCloud[] clouds) {
 		if (cloudIndex < 0 || cloudIndex >= clouds.length) {
 			return null;
 		}
 
 		return clouds[cloudIndex];
 	}
 
 	private void createColumns(ITableContentAndLabelProvider<CLOUDELEMENT> provider, TableColumnLayout tableLayout,
 			Table table) {
 		Columns<CLOUDELEMENT> columns = provider.getColumns();
 
 		for (int i = 0; i < columns.getSize(); ++i) {
 			Column<CLOUDELEMENT> c = columns.getColumn(i);
 			TableColumn tc = new TableColumn(table, SWT.NONE);
 			if (i == 0) {
 				table.setSortColumn(tc);
 			}
 			tc.setText(CVMessages.getString(c.getName()));
 			tableLayout.setColumnData(tc, new ColumnWeightData(c.getWeight(), true));
 			tc.addSelectionListener(new ColumnListener(i));
 		}
 	}
 
 	private void createCloudSelector(Composite parent) {
 		this.currentCloudSelectorLabel = new Label(parent, SWT.NULL);
 		currentCloudSelectorLabel.setText(CVMessages.getString(CLOUD_SELECTOR_LABEL));
 
 		this.currentCloudSelector = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
 		currentCloudSelector.addModifyListener(currentCloudModifyListener);
 		// Following is a kludge so that on Linux the Combo is read-only but
 		// has a white background.
 		currentCloudSelector.addVerifyListener(new VerifyListener() {
 			@Override
 			public void verifyText(VerifyEvent e) {
 				e.doit = false;
 			}
 		});
 	}
 
 	private void hookContextMenu(Control control) {
 		IMenuManager contextMenu = UIUtils.createContextMenu(control);
 		UIUtils.registerContributionManager(UIUtils.getContextMenuId(getViewID()), contextMenu, control);
 	}
 
 	protected abstract String getViewID();
 
 	private void initCloudSelector(String cloudNameToSelect, Combo cloudSelector, DeltaCloud[] clouds) {
 		if (clouds.length > 0) {
 			setCloudSelectorItems(toCloudNames(clouds), cloudSelector);
 			int index = getCloudIndex(cloudNameToSelect, clouds);
 			cloudSelector.select(index);
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		if (DeltaCloud.PROP_NAME.equals(event.getPropertyName())) {
 			DeltaCloud cloud = (DeltaCloud) event.getSource();
 			updateCloudSelector(cloud);
 		}
 	}
 
 	private void updateCloudSelector(DeltaCloud cloud) {
 		DeltaCloud[] clouds = getClouds();
 		int index = getCloudIndex(cloud, clouds);
 		if (index >= 0) {
 			int selectionIndex = currentCloudSelector.getSelectionIndex();
 			currentCloudSelector.removeModifyListener(currentCloudModifyListener);
 			currentCloudSelector.setItem(index, cloud.getName());
 			currentCloudSelector.select(selectionIndex);
 			currentCloudSelector.addModifyListener(currentCloudModifyListener);
 		}
 		container.layout(true, true);
 	}
 
 	public void cloudsChanged(final int type, final DeltaCloud cloud) {
		viewer.getControl().getDisplay().syncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				DeltaCloud[] clouds = getClouds();
 				switch (type) {
 				case IDeltaCloudManagerListener.REMOVE_EVENT:
 					onCloudRemoved(cloud, clouds);
 					break;
 				default:
 				}
 
 			int index = getCloudIndex(currentCloud, clouds);
 			String[] cloudNames = toCloudNames(clouds);
 			setCloudSelectorItems(cloudNames, currentCloudSelector);
 
 			if (cloudNames.length > 0) {
 				currentCloudSelector.setText(cloudNames[index]);
 				setViewerInput(currentCloud);
 			} else {
 				currentCloudSelector.setText("");
 				setViewerInput(null);
 			}
 			container.layout(true, true);
 		}
 		});
 	}
 
 	private void onCloudRemoved(DeltaCloud cloud, DeltaCloud[] clouds) {
 		if (cloud == currentCloud) {
 			removePropertyChangeListener(cloud);
 			this.currentCloud = getCloud(0, clouds);
 		}
 	}
 
 	protected void addPropertyChangeListener(DeltaCloud cloud) {
 		if (cloud != null) {
 			cloud.addPropertyChangeListener(DeltaCloud.PROP_NAME, this);
 		}
 	}
 
 	protected void removePropertyChangeListener(DeltaCloud cloud) {
 		if (cloud != null) {
 			cloud.removePropertyChangeListener(this);
 		}
 	}
 
 	private String[] toCloudNames(DeltaCloud[] clouds) {
 		List<String> cloudNames = new ArrayList<String>();
 		for (DeltaCloud cloud : clouds) {
 			if (cloud != null) {
 				cloudNames.add(cloud.getName());
 			}
 		}
 		return (String[]) cloudNames.toArray(new String[cloudNames.size()]);
 	}
 
 	private void setCloudSelectorItems(String[] cloudNames, Combo cloudSelector) {
 		cloudSelector.removeModifyListener(currentCloudModifyListener);
 		cloudSelector.setItems(cloudNames);
 		cloudSelector.addModifyListener(currentCloudModifyListener);
 	}
 
 	/**
 	 * Refresh the states of the commands in the toolbar.
 	 */
 	protected abstract void refreshToolbarCommandStates();
 
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	@Override
 	public void dispose() {
 		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(workbenchSelectionListener);
 		DeltaCloudManager.getDefault().removeCloudManagerListener(this);
 		super.dispose();
 	}
 
 	@SuppressWarnings("rawtypes")
 	public Object getAdapter(Class adapter) {
 		if (adapter == DeltaCloud.class) {
 			return currentCloud;
 		} else {
 			return super.getAdapter(adapter);
 		}
 	}
 }
