 package org.iucn.sis.client.panels.publication;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.FetchMode;
 import org.iucn.sis.client.api.caches.PublicationCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.api.utils.PagingPanel;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.PublicationData;
 import org.iucn.sis.shared.api.models.PublicationTarget;
 import org.iucn.sis.shared.api.models.Taxon;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.Store;
 import com.extjs.gxt.ui.client.store.StoreFilter;
 import com.extjs.gxt.ui.client.store.StoreSorter;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.WindowManager;
 import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
 import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
 import com.extjs.gxt.ui.client.widget.grid.filters.ListFilter;
 import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 public class PublicationGrid extends PagingPanel<PublicationModelData> implements DrawsLazily {
 	
 	private Grid<PublicationModelData> grid;
 	private CheckBoxSelectionModel<PublicationModelData> sm;
 	private TaxonFilter filter;
 	
 	public PublicationGrid() {
 		super();
 		setLayout(new FillLayout());
 		
 		sm = new CheckBoxSelectionModel<PublicationModelData>();
 		sm.setSelectionMode(SelectionMode.SIMPLE);
 		
 		grid = new Grid<PublicationModelData>(getStoreInstance(), getColumnModel());
 		grid.addPlugin(sm);
 		grid.setSelectionModel(sm);
 		
 		getProxy().setFilter(filter = new TaxonFilter());
 		getProxy().setSort(false);
 	}
 	
 	private ColumnModel getColumnModel() {
 		List<ColumnConfig> cols = new ArrayList<ColumnConfig>();
 		
 		cols.add(sm.getColumn());
 		cols.add(new ColumnConfig("group", "Working Set", 100));
 		cols.add(new ColumnConfig("taxon", "Species Name", 100));
 		cols.add(new ColumnConfig("status", "Status", 100));
		cols.add(new ColumnConfig("date", "Date Submitted", 150));
 		cols.add(new ColumnConfig("submitter", "Submitted By", 100));
 		cols.add(new ColumnConfig("goal", "Publication Target", 150));
 		cols.add(new ColumnConfig("approved", "For Publication", 150));
 		cols.add(new ColumnConfig("notes", "Notes RLU", 200));
 		
 		cols.get(0).setHidden(true);
 		
 		return new ColumnModel(cols);
 	}
 	
 	private GridFilters getGridFilters() {
 		ListStore<BaseModelData> filterStore = new ListStore<BaseModelData>();
 		for (PublicationTarget target : PublicationCache.impl.listTargetsFromCache()) {
 			BaseModelData model = new BaseModelData();
 			model.set("text", target.getName());
 			filterStore.add(model);
 		}
 		
 		final GridFilters filters = new GridFilters();
 		filters.setLocal(false);
 		
 		filters.addFilter(new StringFilter("group"));
 		filters.addFilter(new StringFilter("taxon"));
 		filters.addFilter(new StringFilter("status"));
 		filters.addFilter(new StringFilter("submitter"));
 		filters.addFilter(new ListFilter("goal", filterStore));
 		filters.addFilter(new ListFilter("approved", filterStore));
 		filters.addFilter(new StringFilter("notes"));
 		
 		return filters;
 	}
 
 	@Override
 	protected void getStore(final GenericCallback<ListStore<PublicationModelData>> callback) {
 		PublicationCache.impl.listData(new ComplexListener<List<PublicationData>>() {
 			public void handleEvent(List<PublicationData> eventData) {
 				ListStore<PublicationModelData> store = new ListStore<PublicationModelData>();
 				if (usePriority())
 					store.setStoreSorter(new PublicationPriorityStoreSorter());
 				else
 					store.setStoreSorter(new PublicationStoreSorter());
 				for (PublicationData data : eventData)
 					store.add(new PublicationModelData(data));
 				store.sort("date", SortDir.DESC);
 				callback.onSuccess(store);
 			}
 		});
 	}
 	
 	private void jumpToAssessment(PublicationData data) {
 		WindowManager.get().hideAll();
 		WindowUtils.showLoadingAlert("Loading...");
 		AssessmentCache.impl.fetchAssessment(data.getAssessment().getId(), FetchMode.FULL, new GenericCallback<Assessment>() {
 			public void onSuccess(final Assessment assessment) {
 				TaxonomyCache.impl.fetchTaxon(assessment.getTaxon().getId(), new GenericCallback<Taxon>() {
 					public void onFailure(Throwable caught) {
 						WindowUtils.hideLoadingAlert();
 						WindowUtils.errorAlert("Failed to load the taxon for this assessment.");
 					}
 					public void onSuccess(Taxon result) {
 						WindowUtils.hideLoadingAlert();
 						StateManager.impl.setState(result, assessment);
 					}
 				});
 			}
 			public void onFailure(Throwable caught) {
 				WindowUtils.hideLoadingAlert();
 				WindowUtils.errorAlert("Error loading next assessment");
 			}
 		});
 	}
 	
 	private boolean usePriority() {
 		return AuthorizationCache.impl.canUse(AuthorizableFeature.PUBLICATION_MANAGER_EDITING_FEATURE);
 	}
 	
 	@Override
 	protected void refreshView() {
 		grid.getView().refresh(false);
 	}
 	
 	@Override
 	public void draw(final DoneDrawingCallback callback) {
 		PublicationCache.impl.listTargets(new ComplexListener<List<PublicationTarget>>() {
 			public void handleEvent(List<PublicationTarget> eventData) {
 				removeAll();
 				
 				grid.addPlugin(getGridFilters());
 				grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<PublicationModelData>>() {
 					public void handleEvent(GridEvent<PublicationModelData> be) {
 						if (be.getModel() != null)
 							jumpToAssessment(be.getModel().getModel());
 					}
 				});
 				
 				if (usePriority())
 					grid.getView().setViewConfig(new PriorityGridViewConfig());
 				
 				final LayoutContainer container = new LayoutContainer(new BorderLayout());
 				container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
 				container.add(getPagingToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
 				
 				add(container);
 				
 				refresh(callback);
 			}
 		});
 	}
 	
 	public void refresh() {
 		refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 	}
 	
 	public void hideCheckbox() {
 		grid.getColumnModel().setHidden(0, true);
 	}
 	
 	public void showCheckbox() {
 		grid.getColumnModel().setHidden(0, false);
 	}
 	
 	public void filterByTaxon(Taxon taxon) {
 		filter.setTaxon(taxon);
 		getProxy().filter("taxon", "");
 		getLoader().load();
 	}
 	
 	public List<PublicationModelData> getChecked() {
 		return sm.getSelectedItems();
 	}
 	
 	private static class TaxonFilter implements StoreFilter<PublicationModelData> {
 		
 		private String[] base;
 		
 		public void setTaxon(Taxon taxon) {
 			base = taxon.getFootprint();
 		}
 		
 		@Override
 		public boolean select(Store<PublicationModelData> store,
 				PublicationModelData parent, PublicationModelData item,
 				String property) {
 			String[] test = item.getModel().getAssessment().getTaxon().getFootprint();
 			
 			boolean select = base != null && base.length <= test.length;
 			for (int i = 0; select && i < base.length && i < test.length; i++)
 				select = base[i].equals(test[i]);
 			
 			return select;
 		}
 		
 	}
 	
 	private static class PublicationStoreSorter extends StoreSorter<PublicationModelData> {
 		
 		@Override
 		public int compare(Store<PublicationModelData> store, PublicationModelData m1, PublicationModelData m2,
 				String property) {
 			return m1.getModel().getSubmissionDate().compareTo(m2.getModel().getSubmissionDate());
 		}
 		
 	}
 	
 	private static class PublicationPriorityStoreSorter extends PublicationStoreSorter {
 		
 		@Override
 		public int compare(Store<PublicationModelData> store, PublicationModelData m1, PublicationModelData m2,
 				String property) {
 			Integer p1 = m1.getModel().getPriority() == null ? 0 : m1.getModel().getPriority();
 			Integer p2 = m2.getModel().getPriority() == null ? 0 : m2.getModel().getPriority();
 			
 			int value = p1.compareTo(p2);
 			if (value == 0)
 				value = super.compare(store, m1, m2, property);
 			
 			return value;
 		}
 		
 	}
 	
 	private static class PriorityGridViewConfig extends GridViewConfig {
 		
 		@Override
 		public String getRowStyle(ModelData model, int rowIndex, ListStore<ModelData> ds) {
 			int priority = model.get("priority");
 			if (PublicationData.PRIORITY_HIGH == priority)
 				return "publication_priority_high";
 			else if (PublicationData.PRIORITY_LOW == priority)
 				return "publication_priority_low";
 			else
 				return super.getRowStyle(model, rowIndex, ds);
 		}
 		
 	}
 	
 }
