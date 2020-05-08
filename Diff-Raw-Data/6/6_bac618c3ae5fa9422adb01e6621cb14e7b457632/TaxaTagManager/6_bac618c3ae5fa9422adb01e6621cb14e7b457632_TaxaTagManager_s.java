 package org.iucn.sis.client.panels.taxa.tagging;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.shared.api.models.Taxon;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.DataList;
 import com.extjs.gxt.ui.client.widget.DataListItem;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 public class TaxaTagManager extends LayoutContainer implements DrawsLazily {
 	
 	private final DataList list;
 	
 	private String currentTag = null;
 	private TaxaTaggingBrowser browser;
 	private boolean isDrawn = false;
 	
 	public TaxaTagManager() {
 		super(new FillLayout());
 		
 		list = new DataList();
 		list.setCheckable(true);
 		list.setSelectionMode(SelectionMode.MULTI);
 	}
 
 	@Override
 	public void draw(DoneDrawingCallback callback) {
 		if (isDrawn) {
 			callback.isDrawn();
 			return;
 		}
 		
 		isDrawn = true;
 		
 		browser = new TaxaTaggingBrowser(new SimpleListener() {
 			public void handleEvent() {
 				refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 			}
 		});
 		browser.setCurrentTag("invasive");
 		
 		final LayoutContainer left = new LayoutContainer(new BorderLayout());
 		left.add(createToolBar(), new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
 		left.add(list, new BorderLayoutData(LayoutRegion.CENTER));
 		
 		final LayoutContainer container = new LayoutContainer(new BorderLayout());
 		container.add(browser, new BorderLayoutData(LayoutRegion.EAST, 250));
 		container.add(left, new BorderLayoutData(LayoutRegion.CENTER));
 		
 		add(container);
 		
 		currentTag = "invasive";
 		
 		refresh(callback);
 	}
 
 	private ToolBar createToolBar() {
 		final ToolBar bar = new ToolBar();
 		
 		TagType defaultTag;
 		
 		final ListStore<TagType> store = new ListStore<TagType>();
 		store.add(defaultTag = new TagType("Invasive", "invasive"));
 		store.add(new TagType("Feral", "feral"));
 		
 		final ComboBox<TagType> type = new ComboBox<TagType>();
 		type.setStore(store);
 		type.setForceSelection(true);
 		type.setValue(defaultTag);
 		type.addSelectionChangedListener(new SelectionChangedListener<TagType>() {
 			public void selectionChanged(SelectionChangedEvent<TagType> se) {
 				if (se.getSelectedItem() != null) {
 					currentTag = se.getSelectedItem().getValue();
 					refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 				}
 			}
 		});
 		
 		bar.add(new Html("Select Tag: "));
 		bar.add(type);
 		bar.add(new SeparatorToolItem());
 		bar.add(new Button("Untag Selection", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				final List<DataListItem> selected = list.getSelectedItems();
 				if (!selected.isEmpty()) {
 					WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove the " 
 							+ currentTag + " tag from the " + selected.size() + 
 							" selected species?", new WindowUtils.SimpleMessageBoxListener() {
 						public void onYes() {
 							untag(selected);
 						}
 					});
 				}
 			}
 		}));
 		
 		return bar;
 	}
 	
 	private void untag(List<DataListItem> selection) {
 		final List<Taxon> taxa = new ArrayList<Taxon>();
 		for (DataListItem item : selection) {
 			Taxon taxon = item.getData("taxon");
 			taxa.add(taxon);
 		}
 		TaxonomyCache.impl.untagTaxa(currentTag, taxa, new GenericCallback<Object>() {
 			public void onSuccess(Object result) {
 				refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 			}
 			public void onFailure(Throwable caught) {
 				WindowUtils.errorAlert("Could not save, please try again later.");
 			}
 		});
 	}
 	
 	public void refresh(final DrawsLazily.DoneDrawingCallback callback) {
 		TaxonomyCache.impl.getTaggedTaxa(currentTag, new GenericCallback<List<Taxon>>() {
 			public void onSuccess(List<Taxon> result) {
 				list.removeAll();
 				
 				for (Taxon taxon : result) {
 					DataListItem item = new DataListItem();
 					item.setText(taxon.getName());
 					item.setData("taxon", taxon);
 					
 					list.add(item);
 				}
 				
 				callback.isDrawn();
 			}
 			public void onFailure(Throwable caught) {
 				WindowUtils.errorAlert("Could not load list.");
 				
 				callback.isDrawn();
 			}
 		});
 	}
 	
 	private static class TagType extends BaseModelData {
 		private static final long serialVersionUID = 1L;
 		
 		public TagType(String name, String value) {
 			super();
 			set("text", name);
 			set("value", value);
 		}
 		
 		public String getValue() {
 			return get("value");
 		}
 	}
 	
 }
