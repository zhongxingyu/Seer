 /*******************************************************************************
  * Copyright (c) 2004, 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.jsf.facesconfig.ui.section;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigPackage;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.emf.NavigationCaseType;
 import org.eclipse.jst.jsf.facesconfig.emf.NavigationRuleType;
 import org.eclipse.jst.jsf.facesconfig.ui.NewEditorResourcesNLS;
 import org.eclipse.jst.jsf.facesconfig.ui.page.IFacesConfigPage;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.PageflowEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * @author sfshi
  * 
  */
 public class OverviewNavigationSection extends AbstractOverviewSection {
 
 	private static final int COLUMN_WITH = 70;
 
 	private OverviewNavigationSectionAdapter overviewNavigationSectionAdapter;
 
 	/**
 	 * 
 	 * @param parent
 	 * @param managedForm
 	 * @param page
 	 * @param toolkit
 	 */
 	public OverviewNavigationSection(Composite parent,
 			IManagedForm managedForm, IFacesConfigPage page, FormToolkit toolkit) {
 		super(parent, managedForm, page, toolkit, PageflowEditor.PAGE_ID,
 				NewEditorResourcesNLS.OverviewPage_NavigationSection_name,
 				NewEditorResourcesNLS.OverviewPage_NavigationSection_description,
 				null, null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.facesconfig.ui.section.AbstractOverviewSection#configTableViewer(org.eclipse.jface.viewers.TableViewer)
 	 */
 	protected void configTableViewer(TableViewer tableViewer) {
 		tableViewer.setContentProvider(new IStructuredContentProvider() {
 
 			public Object[] getElements(Object inputElement) {
 				List navigationCaseList = (List) inputElement;
 				return navigationCaseList.toArray();
 			}
 
 			public void dispose() {
 
 			}
 
 			public void inputChanged(Viewer viewer, Object oldInput,
 					Object newInput) {
 
 			}
 		});
 
 		tableViewer.setLabelProvider(new ITableLabelProvider() {
 
 			public Image getColumnImage(Object element, int columnIndex) {
 				return null;
 			}
 
 			public String getColumnText(Object element, int columnIndex) {
 				NavigationCaseType navigationCase = (NavigationCaseType) element;
 				switch (columnIndex) {
 
 				case 0:
 					return ((NavigationRuleType) navigationCase.eContainer())
 							.getFromViewId() == null ? ""
 							: ((NavigationRuleType) navigationCase.eContainer())
 									.getFromViewId().getTextContent();
 
 				case 1:
 					return navigationCase.getFromOutcome() == null ? ""
 							: navigationCase.getFromOutcome().getTextContent();
 
 				case 2:
 					return navigationCase.getToViewId() == null ? ""
 							: navigationCase.getToViewId().getTextContent();
 				}
 				return null;
 			}
 
 			public void addListener(ILabelProviderListener listener) {
 
 			}
 
 			public void dispose() {
 
 			}
 
 			public boolean isLabelProperty(Object element, String property) {
 				return false;
 			}
 
 			public void removeListener(ILabelProviderListener listener) {
 
 			}
 		});
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.facesconfig.ui.section.AbstractOverviewSection#createTable(org.eclipse.swt.widgets.Composite)
 	 */
 	protected Table createTable(Composite container) {
 		Table table = new Table(container, SWT.H_SCROLL | SWT.V_SCROLL
 				| SWT.FULL_SELECTION | SWT.BORDER | SWT.BORDER);
 
 		GridData gd = new GridData(GridData.FILL_BOTH);
 		gd.horizontalSpan = 1;
 		gd.heightHint = 100;
 		table.setLayoutData(gd);
 
 		table.setLinesVisible(true);
 		table.setHeaderVisible(true);
 		TableLayout tablelayout = new TableLayout();
 		table.setLayout(tablelayout);
 
 		TableColumn column1 = new TableColumn(table, SWT.LEFT);
 		column1
 				.setText(NewEditorResourcesNLS.OverviewPage_NavigationSection_table_col1);
 		// column1.setWidth(COLUMN_WITH);
 		tablelayout.addColumnData(new ColumnWeightData(1, COLUMN_WITH, true));
 		// tablelayout.addColumnData(new ColumnWeightData(1, true));
 		column1.setResizable(true);
 
 		TableColumn column2 = new TableColumn(table, SWT.LEFT);
 		column2
 				.setText(NewEditorResourcesNLS.OverviewPage_NavigationSection_table_col2);
 		tablelayout.addColumnData(new ColumnWeightData(1, COLUMN_WITH, true));
 		// tablelayout.addColumnData(new ColumnWeightData(1, true));
 		column2.setResizable(true);
 		// column2.setWidth(COLUMN_WITH);
 
 		TableColumn column3 = new TableColumn(table, SWT.LEFT);
 		column3
 				.setText(NewEditorResourcesNLS.OverviewPage_NavigationSection_table_col3);
 		tablelayout.addColumnData(new ColumnWeightData(1, COLUMN_WITH, true));
 		// tablelayout.addColumnData(new ColumnWeightData(1, true));
 		column3.setResizable(true);
 		// column3.setWidth(COLUMN_WITH);
 
 		return table;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.facesconfig.ui.section.IFacesConfigSection#refreshAll()
 	 */
 	public void refreshAll() {
 		List navigationCaseList = new ArrayList();
 		if (getInput() instanceof FacesConfigType) {
 			List navigationRules = ((FacesConfigType) getInput())
 					.getNavigationRule();
 			for (int i = 0, n = navigationRules.size(); i < n; i++) {
 				NavigationRuleType navigationRule = (NavigationRuleType) navigationRules
 						.get(i);
 				navigationCaseList.addAll(navigationRule.getNavigationCase());
 			}
 		}
 
 		tableViewer.setInput(navigationCaseList);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.facesconfig.ui.section.AbstractFacesConfigSection#addAdaptersOntoInput(java.lang.Object)
 	 */
 	protected void addAdaptersOntoInput(Object newInput) {
 		FacesConfigType facesConfig = (FacesConfigType) newInput;
 		addOverviewNavigationSectionAdapter(facesConfig);
 
 		for (Iterator it = facesConfig.getNavigationRule().iterator(); it
 				.hasNext();) {
 			NavigationRuleType navigationRule = (NavigationRuleType) it.next();
 
 			addOverviewNavigationSectionAdapter(navigationRule);
 
 			if (navigationRule.getFromViewId() != null) {
 				addOverviewNavigationSectionAdapter(navigationRule
 						.getFromViewId());
 			}
 
 			for (Iterator it2 = navigationRule.getNavigationCase().iterator(); it2
 					.hasNext();) {
 				NavigationCaseType navigationCase = (NavigationCaseType) it2
 						.next();
 				addOverviewNavigationSectionAdapter(navigationCase);
 
 				if (navigationCase.getFromOutcome() != null) {
 					addOverviewNavigationSectionAdapter(navigationCase
 							.getFromOutcome());
 				}
 
 				if (navigationCase.getToViewId() != null) {
 					addOverviewNavigationSectionAdapter(navigationCase
 							.getToViewId());
 				}
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.jsf.facesconfig.ui.section.AbstractFacesConfigSection#removeAdaptersFromInput(java.lang.Object)
 	 */
 	protected void removeAdaptersFromInput(Object oldInput) {
 		super.removeAdaptersFromInput(oldInput);
 
 		FacesConfigType facesConfig = (FacesConfigType) oldInput;
 		removeOverviewNavigationSectionAdapter(facesConfig);
 
 		for (Iterator it = facesConfig.getNavigationRule().iterator(); it
 				.hasNext();) {
 			NavigationRuleType navigationRule = (NavigationRuleType) it.next();
 
 			removeOverviewNavigationSectionAdapter(navigationRule);
 
 			if (navigationRule.getFromViewId() != null) {
 				removeOverviewNavigationSectionAdapter(navigationRule
 						.getFromViewId());
 			}
 
 			for (Iterator it2 = navigationRule.getNavigationCase().iterator(); it2
 					.hasNext();) {
 				NavigationCaseType navigationCase = (NavigationCaseType) it2
 						.next();
 				removeOverviewNavigationSectionAdapter(navigationCase);
 
 				if (navigationCase.getFromOutcome() != null) {
 					removeOverviewNavigationSectionAdapter(navigationCase
 							.getFromOutcome());
 				}
 
 				if (navigationCase.getToViewId() != null) {
 					removeOverviewNavigationSectionAdapter(navigationCase
 							.getToViewId());
 				}
 			}
 		}
 
 	}
 
 	protected void addOverviewNavigationSectionAdapter(EObject object) {
 		if (EcoreUtil.getExistingAdapter(object,
 				OverviewNavigationSection.class) == null) {
 			object.eAdapters().add(getOverviewNavigationSectionAdapter());
 		}
 
 	}
 
 	protected void removeOverviewNavigationSectionAdapter(EObject object) {
 		if (EcoreUtil.getExistingAdapter(object,
 				OverviewNavigationSection.class) != null) {
 			object.eAdapters().remove(getOverviewNavigationSectionAdapter());
 		}
 	}
 
 	protected OverviewNavigationSectionAdapter getOverviewNavigationSectionAdapter() {
 		if (overviewNavigationSectionAdapter == null)
 			overviewNavigationSectionAdapter = new OverviewNavigationSectionAdapter();
 		return overviewNavigationSectionAdapter;
 	}
 
 	class OverviewNavigationSectionAdapter extends AdapterImpl {
 
 		public boolean isAdapterForType(Object type) {
 			if (type == OverviewNavigationSection.class)
 				return true;
 			return false;
 		}
 
 		public void notifyChanged(Notification msg) {
 
 			if ((msg.getFeature() == FacesConfigPackage.eINSTANCE
 					.getFacesConfigType_NavigationRule()
 					|| msg.getFeature() == FacesConfigPackage.eINSTANCE
 							.getNavigationRuleType_NavigationCase()
 					|| msg.getFeature() == FacesConfigPackage.eINSTANCE
 							.getNavigationRuleType_FromViewId() || msg
 					.getFeature() == FacesConfigPackage.eINSTANCE
 					.getFromViewIdType_TextContent())) {
 				if (msg.getEventType() == Notification.ADD
 						|| msg.getEventType() == Notification.SET) {
 					if (msg.getNewValue() instanceof EObject) {
 						EObject newObject = (EObject) msg.getNewValue();
 						addOverviewNavigationSectionAdapter(newObject);
 					}
 				}
 				if (msg.getEventType() == Notification.ADD
 						|| msg.getEventType() == Notification.REMOVE
 						|| msg.getEventType() == Notification.SET)
 					refreshAll();
 				return;
 			}
 
 			if (msg.getFeature() == FacesConfigPackage.eINSTANCE
 					.getNavigationCaseType_FromOutcome()
 					|| msg.getFeature() == FacesConfigPackage.eINSTANCE
 							.getNavigationCaseType_ToViewId()) {
 
 				if (msg.getEventType() == Notification.ADD
 						|| msg.getEventType() == Notification.SET) {
					if (msg.getNewValue() instanceof EObject) {
						EObject newObject = (EObject) msg.getNewValue();
						addOverviewNavigationSectionAdapter(newObject);
					}
 				}
 				if (msg.getEventType() == Notification.ADD
 
 				|| msg.getEventType() == Notification.REMOVE
 						|| msg.getEventType() == Notification.SET) {
 
 					NavigationCaseType navigationCase = (NavigationCaseType) msg
 							.getNotifier();
 					tableViewer.refresh(navigationCase);
 				}
 				return;
 			}
 
 			if (msg.getFeature() == FacesConfigPackage.eINSTANCE
 					.getFromOutcomeType_TextContent()
 					|| msg.getFeature() == FacesConfigPackage.eINSTANCE
 							.getToViewIdType_TextContent()) {
 
 				if (msg.getEventType() == Notification.ADD
 						|| msg.getEventType() == Notification.REMOVE
 						|| msg.getEventType() == Notification.SET) {
 					NavigationCaseType navigationCase = (NavigationCaseType) ((EObject) msg
 							.getNotifier()).eContainer();
 					tableViewer.refresh(navigationCase);
 				}
 				return;
 			}
 
 		}
 	}
 }
