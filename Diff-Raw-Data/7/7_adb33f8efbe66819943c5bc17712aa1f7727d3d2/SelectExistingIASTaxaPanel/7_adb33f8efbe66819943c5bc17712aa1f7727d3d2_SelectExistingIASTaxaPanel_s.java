 package org.iucn.sis.shared.api.displays.threats;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.utils.PagingPanel;
 import org.iucn.sis.shared.api.displays.threats.ThreatTaggedSpeciesLocator.Selectable;
 import org.iucn.sis.shared.api.models.Taxon;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 public class SelectExistingIASTaxaPanel extends PagingPanel<TaxonFootprintModel> implements DrawsLazily, Selectable {
 
	public static final String[] footprint = new String[] {
 		"Kingdom", "Phylum", "Class", "Order", 
 		"Family", "Genus", "Species"
	};
 	
 	private boolean isDrawn;
 	
 	private Grid<TaxonFootprintModel> grid;
 	
 	public SelectExistingIASTaxaPanel() {
 		super();
 		setLayout(new FillLayout());
 		isDrawn = false;
 	}
 	
 	@Override
 	public void draw(DoneDrawingCallback callback) {
 		if (isDrawn) {
 			callback.isDrawn();
 			return;
 		}
 		isDrawn = true;
 		
 		final CheckBoxSelectionModel<TaxonFootprintModel> sm = 
 			new CheckBoxSelectionModel<TaxonFootprintModel>();
 		
 		grid = 
 			new Grid<TaxonFootprintModel>(getStoreInstance(), getColumnModel(sm.getColumn()));
 		grid.addPlugin(sm);
 		grid.setSelectionModel(sm);
 		
 		final LayoutContainer container = new LayoutContainer(new BorderLayout());
 		container.add(grid, new BorderLayoutData(LayoutRegion.CENTER));
 		container.add(getPagingToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
 		
 		add(container);
 		
 		callback.isDrawn();
 	}
 	
 	public void refresh(DrawsLazily.DoneDrawingCallback callback) {
 		super.refresh(callback);
 	}
 	
 	protected void getStore(final GenericCallback<ListStore<TaxonFootprintModel>> callback) {
 		TaxonomyCache.impl.getTaggedTaxa("invasive", new GenericCallback<List<Taxon>>() {
 			public void onSuccess(List<Taxon> result) {
 				final ListStore<TaxonFootprintModel> store = 
 					new ListStore<TaxonFootprintModel>();
 				for (Taxon taxon : result)
 					store.add(new TaxonFootprintModel(taxon));
 				
 				callback.onSuccess(store);
 			}
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 	}
 	
 	private ColumnModel getColumnModel(ColumnConfig checkColumn) {
 		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();
 		list.add(checkColumn);
 		
 		for (int i = 3; i < footprint.length; i++)
 			list.add(new ColumnConfig(footprint[i], footprint[i], 150));
 		
 		return new ColumnModel(list);
 	}
 	
 	@Override
 	protected void refreshView() {
 		grid.getView().refresh(false);
 	}
 	
 	@Override
 	public Collection<Taxon> getSelection() {
 		final List<Taxon> list = new ArrayList<Taxon>();
 		for (TaxonFootprintModel model : grid.getSelectionModel().getSelectedItems())
 			list.add(model.getTaxon());
 		return list;
 	}
 
 }
