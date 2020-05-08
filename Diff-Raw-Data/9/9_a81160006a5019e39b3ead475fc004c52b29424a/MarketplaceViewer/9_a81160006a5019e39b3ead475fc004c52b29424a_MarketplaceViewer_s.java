 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	  The Eclipse Foundation - initial API and implementation
  *    Yatta Solutions - error handling (bug 374105), header layout (bug 341014),
  *                      news (bug 401721)
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.epp.internal.mpc.core.service.Category;
 import org.eclipse.epp.internal.mpc.core.service.Identifiable;
 import org.eclipse.epp.internal.mpc.core.service.Market;
 import org.eclipse.epp.internal.mpc.core.service.Node;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCatalog;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCategory;
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceCategory.Contents;
 import org.eclipse.epp.mpc.ui.CatalogDescriptor;
 import org.eclipse.equinox.internal.p2.discovery.Catalog;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogCategory;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 import org.eclipse.equinox.internal.p2.discovery.model.Tag;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.ControlListItem;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.FilteredViewer;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.GradientCanvas;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.PatternFilter;
 import org.eclipse.equinox.internal.p2.ui.discovery.util.TextSearchControl;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogFilter;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogViewer;
 import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CategoryItem;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 /**
  * @author Steffen Pingel
  * @author David Green
  * @author Benjamin Muskalla
  * @author Carsten Reckord
  */
 public class MarketplaceViewer extends CatalogViewer {
 
 	public enum ContentType {
 		SEARCH, RECENT, POPULAR, INSTALLED, SELECTION
 	}
 
 	public static class MarketplaceCatalogContentProvider extends CatalogContentProvider {
 
 		private static final Object[] NO_ELEMENTS = new Object[0];
 
 		@Override
 		public Catalog getCatalog() {
 			return super.getCatalog();
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			if (getCatalog() != null) {
 				// don't provide any categories unless it's featured
 				List<Object> items = new ArrayList<Object>(getCatalog().getItems());
 				for (CatalogCategory category : getCatalog().getCategories()) {
 					if (category instanceof MarketplaceCategory) {
 						MarketplaceCategory marketplaceCategory = (MarketplaceCategory) category;
 						if (marketplaceCategory.getContents() == Contents.FEATURED) {
 							items.add(0, category);
 						}
 					}
 				}
 				return items.toArray();
 			}
 			return NO_ELEMENTS;
 		}
 
 	}
 
 	private ViewerFilter[] filters;
 
 	private ContentType contentType = ContentType.SEARCH;
 
 	public static String CONTENT_TYPE_PROPERTY = "contentType"; //$NON-NLS-1$
 
 	private final SelectionModel selectionModel;
 
 	private String queryText;
 
 	private Market queryMarket;
 
 	private Category queryCategory;
 
 	private ContentType queryContentType;
 
 	private final IMarketplaceWebBrowser browser;
 
 	private String findText;
 
 	private final MarketplaceWizard wizard;
 
 	private final List<IPropertyChangeListener> listeners = new LinkedList<IPropertyChangeListener>();
 
 	public MarketplaceViewer(Catalog catalog, IShellProvider shellProvider, MarketplaceWizard wizard) {
 		super(catalog, shellProvider, wizard.getContainer(), wizard.getConfiguration());
 		this.browser = wizard;
 		this.selectionModel = wizard.getSelectionModel();
 		this.wizard = wizard;
 		setAutomaticFind(false);
 	}
 
 	@Override
 	protected void doCreateHeaderControls(Composite parent) {
 		final int originalChildCount = parent.getChildren().length;
 		for (CatalogFilter filter : getConfiguration().getFilters()) {
 			if (filter instanceof MarketplaceFilter) {
 				MarketplaceFilter marketplaceFilter = (MarketplaceFilter) filter;
 				marketplaceFilter.createControl(parent);
 				marketplaceFilter.addPropertyChangeListener(new IPropertyChangeListener() {
 					public void propertyChange(PropertyChangeEvent event) {
 						if (AbstractTagFilter.PROP_SELECTED.equals(event.getProperty())) {
 							doQuery();
 						}
 					}
 				});
 			}
 		}
 		Control[] children = parent.getChildren();
 		for (int x = originalChildCount; x < children.length; ++x) {
 			Control child = children[x];
 			GridDataFactory.swtDefaults().hint(135, SWT.DEFAULT).applyTo(child);
 		}
 		Button goButton = new Button(parent, SWT.PUSH);
 		goButton.setText(Messages.MarketplaceViewer_go);
 		goButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				doQuery();
 			}
 		});
 	}
 
 	@Override
 	protected CatalogContentProvider doCreateContentProvider() {
 		return new MarketplaceCatalogContentProvider();
 	}
 
 	@Override
 	protected void catalogUpdated(boolean wasCancelled, boolean wasError) {
 		super.catalogUpdated(wasCancelled, wasError);
 
 		for (CatalogFilter filter : getConfiguration().getFilters()) {
 			if (filter instanceof MarketplaceFilter) {
 				((MarketplaceFilter) filter).catalogUpdated(wasCancelled);
 			}
 		}
 	}
 
 	@Override
 	protected void filterTextChanged() {
 		doFind(getFilterText());
 	}
 
 	@Override
 	protected void doFind(String text) {
 		findText = text;
 		doQuery();
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	protected ControlListItem<?> doCreateViewerItem(Composite parent, Object element) {
 		if (element instanceof CatalogItem) {
 			CatalogItem catalogItem = (CatalogItem) element;
 			if (catalogItem.getData() instanceof CatalogDescriptor) {
 				CatalogDescriptor catalogDescriptor = (CatalogDescriptor) catalogItem.getData();
 				return new BrowseCatalogItem(parent, getResources(), shellProvider, browser,
 						(MarketplaceCategory) catalogItem.getCategory(), catalogDescriptor, this);
 			} else {
 				DiscoveryItem discoveryItem = new DiscoveryItem(parent, SWT.NONE, getResources(), shellProvider,
 						browser, catalogItem, this);
 				discoveryItem.setSelected(getCheckedItems().contains(catalogItem));
 				return discoveryItem;
 			}
 		} else if (element instanceof MarketplaceCategory) {
 			MarketplaceCategory category = (MarketplaceCategory) element;
 			if (category.getContents() == Contents.FEATURED) {
 				category.setName(Messages.MarketplaceViewer_featured);
 			} else {
 				throw new IllegalStateException();
 			}
 			CategoryItem<?> categoryItem = (CategoryItem<?>) super.doCreateViewerItem(parent, element);
 			setSeparatorVisible(categoryItem, false);
 			fixLayout(categoryItem);
 			return categoryItem;
 		}
 		return super.doCreateViewerItem(parent, element);
 	}
 
 	public void show(Set<Node> nodes) {
 		ContentType newContentType = ContentType.SEARCH;
 		ContentType oldContentType = contentType;
 		contentType = newContentType;
 		fireContentTypeChange(oldContentType, newContentType);
 
 		doQuery(null, null, null, nodes);
 	}
 
 	public void search(Market market, Category category, String query) {
 		ContentType newContentType = ContentType.SEARCH;
 		ContentType oldContentType = contentType;
 		contentType = newContentType;
 		fireContentTypeChange(oldContentType, newContentType);
 
 		setFilters(market, category, query);
 		queryMarket = market;
 		queryCategory = category;
 		queryText = query;
 
 		doQuery(market, category, query, null);
 	}
 
 	private void setFilters(Market market, Category category, String query) {
 		setFindText(query);
 		for (CatalogFilter filter : getConfiguration().getFilters()) {
 			if (filter instanceof AbstractTagFilter) {
 				AbstractTagFilter tagFilter = (AbstractTagFilter) filter;
 				if (tagFilter.getTagClassification() == Category.class) {
 					List<Tag> choices = tagFilter.getChoices();
 					Tag tag = choices.isEmpty() ? null : choices.get(0);
 					if (tag != null) {
 						Identifiable data = null;
 						if (tag.getTagClassifier() == Market.class) {
 							data = market;
 						} else if (tag.getTagClassifier() == Category.class) {
 							data = category;
 						} else {
 							continue;
 						}
 						tag = null;
 						if (data != null) {
 							for (Tag choice : choices) {
 								final Object choiceData = choice.getData();
 								if (data == choiceData || data.equalsId(choiceData)) {
 									tag = choice;
 									break;
 								}
 							}
 						}
 						tagFilter.setSelected(tag == null ? null : Collections.singleton(tag));
 						//we expect a query to happen next, so don't fire a property change resulting in an additional query
 						tagFilter.updateUi();
 					}
 				}
 			}
 		}
 	}
 
 	private void doQuery() {
 		queryMarket = null;
 		queryCategory = null;
 		queryText = null;
 		findText = getFilterText();
 
 		for (CatalogFilter filter : getConfiguration().getFilters()) {
 			if (filter instanceof AbstractTagFilter) {
 				AbstractTagFilter tagFilter = (AbstractTagFilter) filter;
 				if (tagFilter.getTagClassification() == Category.class) {
 					Tag tag = tagFilter.getSelected().isEmpty() ? null : tagFilter.getSelected().iterator().next();
 					if (tag != null) {
 						if (tag.getTagClassifier() == Market.class) {
 							queryMarket = (Market) tag.getData();
 						} else if (tag.getTagClassifier() == Category.class) {
 							queryCategory = (Category) tag.getData();
 						}
 					}
 				}
 			}
 		}
 		queryText = findText;
 		doQuery(queryMarket, queryCategory, queryText, null);
 	}
 
 	public void doQueryForTag(String tag) {
 		ContentType newContentType = ContentType.SEARCH;
 		ContentType oldContentType = contentType;
 		contentType = newContentType;
 		fireContentTypeChange(oldContentType, newContentType);
 		setFindText(tag);
 		doQuery(null, null, tag, null);
 	}
 
 	private void setFindText(String tag) {
 		try {
 			Field filterTextField = FilteredViewer.class.getDeclaredField("filterText"); //$NON-NLS-1$
 			filterTextField.setAccessible(true);
 			TextSearchControl textSearchControl = (TextSearchControl) filterTextField.get(this);
 			textSearchControl.getTextControl().setText(tag);
 		} catch (Exception e) {
 			StatusManager.getManager()
 			.handle(new Status(IStatus.WARNING, MarketplaceClientUi.BUNDLE_ID,
 					Messages.MarketplaceViewer_Could_not_change_find_text, e));
 		}
 	}
 
 	private void fireContentTypeChange(ContentType oldValue, ContentType newValue) {
 		Object source = this;
 		String property = CONTENT_TYPE_PROPERTY;
 		firePropertyChangeEvent(new PropertyChangeEvent(source, property, oldValue, newValue));
 	}
 
 	private void doQuery(final Market market, final Category category, final String queryText, final Set<Node> nodes) {
 		try {
 			final ContentType queryType = contentType;
 			queryContentType = queryType;
 			final IStatus[] result = new IStatus[1];
 			context.run(true, true, new IRunnableWithProgress() {
 				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 					switch (queryType) {
 					case POPULAR:
 						result[0] = getCatalog().popular(monitor);
 						break;
 					case RECENT:
 						result[0] = getCatalog().recent(monitor);
 						break;
 					case INSTALLED:
 						result[0] = getCatalog().installed(monitor);
 						break;
 					case SELECTION:
 						Set<String> nodeIds = new HashSet<String>();
 						for (CatalogItem item : getSelectionModel().getItemToOperation().keySet()) {
 							nodeIds.add(((Node) item.getData()).getId());
 						}
 						result[0] = getCatalog().performQuery(monitor, nodeIds);
 						break;
 					case SEARCH:
 					default:
 						if (nodes != null && !nodes.isEmpty()) {
 							result[0] = getCatalog().performNodeQuery(monitor, nodes);
 						} else if (queryText != null && queryText.length() > 0) {
 							result[0] = getCatalog().performQuery(market, category, queryText, monitor);
 						} else {
 							result[0] = getCatalog().featured(monitor, market, category);
 						}
 						break;
 					}
 					if (!monitor.isCanceled() && result[0] != null && result[0].getSeverity() != IStatus.CANCEL) {
 						getCatalog().checkForUpdates(monitor);
 					}
 				}
 			});
 
 			if (result[0] != null && !result[0].isOK() && result[0].getSeverity() != IStatus.CANCEL) {
 				StatusManager.getManager().handle(result[0],
 						StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
 			} else {
 				verifyUpdateSiteAvailability();
 			}
 		} catch (InvocationTargetException e) {
 			IStatus status = computeStatus(e, Messages.MarketplaceViewer_unexpectedException);
 			StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
 		} catch (InterruptedException e) {
 			// cancelled by user so nothing to do here.
 		}
 		if (contentType == ContentType.INSTALLED) {
 			getViewer().setSorter(new MarketplaceViewerSorter());
 		} else {
 			getViewer().setSorter(null);
 		}
 
 		super.doFind(queryText);
 		// bug 305274: scrollbars don't always appear after switching tabs, so we re-do the layout
 		getViewer().getControl().getParent().layout(true, true);
 	}
 
 	public void showSelected() {
 		contentType = ContentType.SELECTION;
 		queryMarket = null;
 		queryCategory = null;
 		queryText = null;
 		findText = null;
 		setHeaderVisible(true);
 		doQuery(null, null, findText, null);
 		contentType = ContentType.SEARCH;
 	}
 
 	@Override
 	public MarketplaceCatalogConfiguration getConfiguration() {
 		return (MarketplaceCatalogConfiguration) super.getConfiguration();
 	}
 
 	@Override
 	public MarketplaceCatalog getCatalog() {
 		return (MarketplaceCatalog) super.getCatalog();
 	}
 
 	public ContentType getContentType() {
 		return contentType;
 	}
 
 	@Override
 	protected PatternFilter doCreateFilter() {
 		return new MarketplacePatternFilter();
 	}
 
 	public void setContentType(ContentType contentType) {
 		if (this.contentType != contentType) {
 			ContentType oldContentType = this.contentType;
 			this.contentType = contentType;
 			fireContentTypeChange(oldContentType, contentType);
 			setHeaderVisible(contentType == ContentType.SEARCH || contentType == ContentType.SELECTION);
 			doQuery();
 		}
 	}
 
 	public void addPropertyChangeListener(IPropertyChangeListener listener) {
 		listeners.add(listener);
 	}
 
 	public void removePropertyChangeListener(IPropertyChangeListener listener) {
 		listeners.remove(listener);
 	}
 
 	private void firePropertyChangeEvent(PropertyChangeEvent event) {
 		for (IPropertyChangeListener listener : listeners) {
 			listener.propertyChange(event);
 		}
 	}
 
 	@Override
 	public void setHeaderVisible(boolean visible) {
 		if (visible != isHeaderVisible()) {
 			if (!visible) {
 				filters = getViewer().getFilters();
 				getViewer().resetFilters();
 			} else {
 				if (filters != null) {
 					getViewer().setFilters(filters);
 					filters = null;
 				}
 			}
 			super.setHeaderVisible(visible);
 		}
 	}
 
 	@Override
 	protected boolean doFilter(CatalogItem item) {
 		// all filtering is done server-side, so never filter here
 		return true;
 	}
 
 	@Override
 	protected StructuredViewer doCreateViewer(Composite container) {
 		StructuredViewer viewer = super.doCreateViewer(container);
 		viewer.setSorter(null);
 		return viewer;
 	}
 
 	/**
 	 * not supported, instead usee {@link #modifySelection(CatalogItem, Operation)}
 	 */
 	@Override
 	protected void modifySelection(CatalogItem connector, boolean selected) {
 		throw new UnsupportedOperationException();
 	}
 
 	protected void modifySelection(CatalogItem connector, Operation operation) {
 		if (operation == null) {
 			throw new IllegalArgumentException();
 		}
 
 		selectionModel.select(connector, operation);
 		super.modifySelection(connector, operation != Operation.NONE);
 	}
 
 	@Override
 	protected void postDiscovery() {
 		// nothing to do
 	}
 
 	@Override
 	public void updateCatalog() {
 		if (getWizard().wantInitializeInitialSelection()) {
 			try {
 				getWizard().initializeInitialSelection();
 				catalogUpdated(false, false);
 			} catch (CoreException e) {
 				boolean wasCancelled = e.getStatus().getSeverity() == IStatus.CANCEL;
 				if (!wasCancelled) {
 					StatusManager.getManager().handle(e.getStatus(),
 							StatusManager.SHOW | StatusManager.BLOCK | StatusManager.LOG);
 				}
 				catalogUpdated(wasCancelled, !wasCancelled);
 			}
 		} else {
 			super.updateCatalog();
 		}
 		refresh();
 	}
 
 	@Override
 	protected IStatus computeStatus(InvocationTargetException e, String message) {
 		return MarketplaceClientUi.computeStatus(e, message);
 	}
 
 	private MarketplaceWizard getWizard() {
 		return wizard;
 	}
 
 	@Override
 	public List<CatalogItem> getCheckedItems() {
 		List<CatalogItem> items = new ArrayList<CatalogItem>();
 		for (Entry<CatalogItem, Operation> entry : getSelectionModel().getItemToOperation().entrySet()) {
 			if (entry.getValue() != Operation.NONE) {
 				items.add(entry.getKey());
 			}
 		}
 		return items;
 	}
 
 	@Override
 	public IStructuredSelection getSelection() {
 		return new StructuredSelection(getCheckedItems());
 	}
 
 	public SelectionModel getSelectionModel() {
 		return selectionModel;
 	}
 
 	/**
 	 * the text for the current query
 	 */
 	String getQueryText() {
 		return queryText;
 	}
 
 	/**
 	 * the category for the current query
 	 * 
 	 * @return the market or null if no category has been selected
 	 */
 	Category getQueryCategory() {
 		return queryCategory;
 	}
 
 	/**
 	 * the market for the current query
 	 * 
 	 * @return the market or null if no market has been selected
 	 */
 	Market getQueryMarket() {
 		return queryMarket;
 	}
 
 	/**
 	 * the content type for the current query
 	 * 
 	 * @return the content type or null if it's unknown
 	 */
 	ContentType getQueryContentType() {
 		return queryContentType;
 	}
 
 	@Override
 	protected Set<String> getInstalledFeatures(IProgressMonitor monitor) {
 		return MarketplaceClientUi.computeInstalledFeatures(monitor);
 	}
 
 	protected static void fixLayout(CategoryItem<?> categoryItem) {
 		//FIXME remove once the layout has been fixed upstream
 
 		CatalogCategory category = categoryItem.getData();
 		boolean hasDescription = category.getDescription() != null;
 		int valignTitle = hasDescription ? SWT.BEGINNING : SWT.CENTER;
 		int totalRows = hasDescription ? 2 : 1;
 
 		final Control[] children = categoryItem.getChildren();
 		Composite categoryHeaderContainer = (Composite) children[0];
 		GridLayoutFactory.fillDefaults()
 		.numColumns(3)
 		.margins(5, hasDescription ? 5 : 10)
 		.equalWidth(false)
 		.applyTo(categoryHeaderContainer);
 
 		final Control[] headerChildren = categoryHeaderContainer.getChildren();
 		final Control iconLabel = headerChildren[0];
 		final Control nameLabel = headerChildren[1];
 		final Control tooltip = headerChildren[2];
 
 		GridDataFactory.swtDefaults().align(SWT.CENTER, valignTitle).span(1, totalRows).applyTo(iconLabel);
 		GridDataFactory.fillDefaults().grab(true, false).align(SWT.BEGINNING, valignTitle).applyTo(nameLabel);
 
 		if (tooltip instanceof Label) {
 			GridDataFactory.fillDefaults().align(SWT.END, valignTitle).applyTo(tooltip);
 		}
 	}
 
 	protected static void setSeparatorVisible(CategoryItem<?> categoryItem, boolean visible) {
 		//FIXME introduce API in CategoryItem and then get rid of this
 		final Control childControl = categoryItem.getChildren()[0];
 		if (childControl instanceof GradientCanvas) {
 			GradientCanvas canvas = (GradientCanvas) childControl;
 			canvas.setSeparatorVisible(visible);
 		}
 	}
 }
