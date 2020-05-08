 package org.iucn.sis.client.panels;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
 import org.iucn.sis.client.api.caches.MarkedCache;
 import org.iucn.sis.client.panels.MonkeyNavigator.NavigationChangeEvent;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.extjs.gxt.ui.client.data.ModelKeyProvider;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.GroupingStore;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.gwt.ui.DrawsLazily;
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 public class TaxonMonkeyNavigatorPanel extends GridNonPagingMonkeyNavigatorPanel<Taxon> {
 	
 	private WorkingSet curNavWorkingSet;
 	private Taxon curNavTaxon;
 	
 	public TaxonMonkeyNavigatorPanel() {
 		super();
 		setHeading("Taxon List");
 	}
 	
 	public void refresh(final WorkingSet curNavWorkingSet, final Taxon curNavTaxon) {
 		this.curNavWorkingSet = curNavWorkingSet;
 		this.curNavTaxon = curNavTaxon;
 		
 		refresh(new DrawsLazily.DoneDrawingCallback() {
 			public void isDrawn() {
 				DeferredCommand.addCommand(new Command() {
 					public void execute() {
 						setSelection(curNavTaxon);
 					}
 				});
 			}
 		});
 	}
 	
 	protected void getStore(final GenericCallback<ListStore<NavigationModelData<Taxon>>> callback) {
 		MonkeyNavigator.getSortedTaxa(new ComplexListener<List<Taxon>>() {
 			public void handleEvent(List<Taxon> eventData) {
 				if (curNavWorkingSet == null)
 					getRecentTaxonStore(eventData, callback);
 				else
 					getTaxonForWorkingSetStore(eventData, callback);
 			}
 		});
 	}
 	
 	private void getRecentTaxonStore(List<Taxon> recent, GenericCallback<ListStore<NavigationModelData<Taxon>>> callback) {
 		final GroupingStore<NavigationModelData<Taxon>> store = 
 			new GroupingStore<NavigationModelData<Taxon>>();
 		store.groupBy("familyid");
 		store.setKeyProvider(new ModelKeyProvider<NavigationModelData<Taxon>>() {
 			public String getKey(NavigationModelData<Taxon> model) {
 				if (model.getModel() == null)
 					return "-1";
 				else
 					return Integer.toString(model.getModel().getId());
 			}
 		});
 		
 		if (!recent.isEmpty()) {
 			NavigationModelData<Taxon> header = new NavigationModelData<Taxon>(null);
 			header.set("name", "Recent Taxa");
 			header.set("header", Boolean.TRUE);
 			
 			store.add(header);
 			
 			for (Taxon taxon : recent) {
 				if (taxon != null) {
 					NavigationModelData<Taxon> model = new NavigationModelData<Taxon>(taxon);
 					model.set("name", taxon.getFriendlyName());
 					model.set("family", "Recent Taxa");
 					
 					store.add(model);
 				}
 			}
 		}
 		
 		callback.onSuccess(store);
 	}
 	
 	private void getTaxonForWorkingSetStore(final List<Taxon> result, final GenericCallback<ListStore<NavigationModelData<Taxon>>> callback) {
 		final ListStore<NavigationModelData<Taxon>> store = new ListStore<NavigationModelData<Taxon>>();
 		store.setKeyProvider(new ModelKeyProvider<NavigationModelData<Taxon>>() {
 			public String getKey(NavigationModelData<Taxon> model) {
 				if (model.getModel() == null)
 					return "-1";
 				else
 					return Integer.toString(model.getModel().getId());
 			}
 		});
 		
 		String currentFamily = null;
 		NavigationModelData<Taxon> currentHeader = null;
 		int groupCount = 0;
 		
 		int size = result.size();
 		
 		for (Taxon taxon : result) {
			String family;
			try {
				family = taxon.getFootprint()[TaxonLevel.FAMILY];
			} catch (IndexOutOfBoundsException e) {
				Debug.println("Failed to get footprint for {0}: {1}", taxon.getId(), taxon.getFootprint());
				continue;
			}
			
 			if (!family.equals(currentFamily)) {
 				if (currentHeader != null)
 					updateHeaderCount(currentHeader, groupCount, size);
 				
 				NavigationModelData<Taxon> header = new NavigationModelData<Taxon>(null);
 				header.set("name", family);
 				header.set("header", Boolean.TRUE);
 				
 				store.add(header);
 				
 				currentFamily = family;
 				currentHeader = header;
 				groupCount = 0;
 			}
 			
 			NavigationModelData<Taxon> model = new NavigationModelData<Taxon>(taxon);
 			model.set("name", taxon.getFriendlyName());
 			model.set("family", taxon.getFootprint()[TaxonLevel.FAMILY]);
 			
 			store.add(model);
 			
 			groupCount++;
 		}
 		
 		updateHeaderCount(currentHeader, groupCount, size);
 		
 		callback.onSuccess(store);
 	}
 	
 	private void updateHeaderCount(NavigationModelData<Taxon> header, int count, int size) {
 		if (header != null) {
 			String name = header.get("name");
 			name += " (" + count + "/" + size + ")";
 			header.set("name", name);
 		}
 	}
 	
 	@Override
 	protected ColumnModel getColumnModel() {
 		final List<ColumnConfig> list = new ArrayList<ColumnConfig>();
 		
 		ColumnConfig display = new ColumnConfig("name", "Name", 100);
 		display.setRenderer(new GridCellRenderer<NavigationModelData<Taxon>>() {
 			@Override
 			public Object render(NavigationModelData<Taxon> model, String property, ColumnData config,
 					int rowIndex, int colIndex, ListStore<NavigationModelData<Taxon>> store,
 					Grid<NavigationModelData<Taxon>> grid) {
 				Boolean header = model.get("header");
 				if (header == null)
 					header = Boolean.FALSE;
 				Taxon taxon = model.getModel();
 				String style;
 				String value;
 				if (taxon == null) {
 					style = header ? "monkey_navigation_section_header" : MarkedCache.NONE;
 					value = model.get(property);
 				}
 				else {
 					style = MarkedCache.impl.getTaxaStyle(taxon.getId());
 					value = taxon.getFriendlyName();
 				}
 				
 				return "<div class=\"" + style + "\">" + value + "</div>";
 			}
 		});
 		list.add(display);
 		
 		ColumnConfig family = new ColumnConfig("family", "Family", 100);
 		family.setHidden(true);
 		
 		list.add(family);
 		
 		return new ColumnModel(list);
 	}
 	
 	protected String getEmptyText() {
 		return "No taxa to list.";
 	}
 	
 	@Override
 	protected void mark(NavigationModelData<Taxon> model, String color) {
 		if (model.getModel() == null)
 			return;
 		
 		Integer itemID = model.getModel().getId();
 		
 		MarkedCache.impl.markTaxa(itemID, color);
 		
 		refreshView();
 	}
 	
 	@Override
 	protected void onSelectionChanged(NavigationModelData<Taxon> model) {
 		MonkeyNavigator.NavigationChangeEvent<Taxon> e = 
 			new NavigationChangeEvent<Taxon>(model.getModel());
 		
 		fireEvent(Events.SelectionChange, e);
 	}
 	
 	@Override
 	protected void setSelection(Taxon curNavTaxon) {
 		final NavigationModelData<Taxon> selection;
 		if (curNavTaxon != null)
 			selection = getProxy().getStore().findModel("" + curNavTaxon.getId());
 		else
 			selection = null;
 		
 		Debug.println("Selected taxon from nav is {0}, found {1}", curNavTaxon, selection);
 		if (selection != null) {
 			((NavigationGridSelectionModel<Taxon>)grid.getSelectionModel()).
 				highlight(selection);
 			
 			DeferredCommand.addPause();
 			DeferredCommand.addCommand(new Command() {
 				public void execute() {
 					grid.getView().focusRow(grid.getStore().indexOf(selection));
 				}
 			});
 		}
 	}
 	
 	@Override
 	protected void onLoad() {
 		super.onLoad();
 		
 		setSelection(curNavTaxon);
 	}
 	
 	@Override
 	protected void open(NavigationModelData<Taxon> model) {
 		if (hasSelection())
 			navigate(curNavWorkingSet, getSelected(), null);
 	}
 	
 	@Override
 	protected void setupToolbar() {
 		final Button goToTaxon = new Button();
 		goToTaxon.setIconStyle("icon-go-jump");
 		goToTaxon.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				AssessmentClientSaveUtils.saveIfNecessary(new SimpleListener() {
 					public void handleEvent() {
 						open(grid.getSelectionModel().getSelectedItem());
 					}
 				});
 			}
 		});
 		
 		addTool(goToTaxon);
 	}
 	
 	public static class TaxonComparator implements Comparator<Taxon> {
 		
 		private final PortableAlphanumericComparator comparator;
 		
 		public TaxonComparator() {
 			comparator = new PortableAlphanumericComparator();
 		}
 		
 		@Override
 		public int compare(Taxon o1, Taxon o2) {
 			String f1 = o1.getFootprint()[TaxonLevel.FAMILY];
 			String f2 = o2.getFootprint()[TaxonLevel.FAMILY];
 			
 			int result = comparator.compare(f1, f2);
 			
 			if (result == 0) {
 				String n1 = o1.getFriendlyName();
 				String n2 = o2.getFriendlyName();
 				
 				result = comparator.compare(n1, n2);
 			}
 			
 			return result;
 		}
 		
 	}
 
 }
