 package org.iucn.sis.client.panels.utils;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
 import org.iucn.sis.client.api.utils.TaxonComparator;
 import org.iucn.sis.client.api.utils.TaxonPagingLoader;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.binder.DataListBinder;
 import com.extjs.gxt.ui.client.data.ModelStringProvider;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.Store;
 import com.extjs.gxt.ui.client.store.StoreFilter;
 import com.extjs.gxt.ui.client.store.StoreSorter;
 import com.extjs.gxt.ui.client.widget.DataList;
 import com.extjs.gxt.ui.client.widget.DataListItem;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.solertium.lwxml.gwt.debug.SysDebugger;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.lwxml.shared.utils.ArrayUtils;
 import com.solertium.util.extjs.client.ViewerFilterTextBox;
 
 public class TaxonomyBrowserPanel extends LayoutContainer {
	
 	class OffsetClickListener implements ClickListener {
 		private NativeDocument ndoc;
 		private String footprint;
 		private int offset;
 
 		public OffsetClickListener(NativeDocument doc, String footprint, int offset) {
 			this.ndoc = doc;
 			this.footprint = footprint;
 			this.offset = offset;
 		}
 
 		public void onClick(Widget sender) {
 
 			updateBody(ndoc, footprint, offset);
 
 		}
 	}
 
 	
 
 	protected String[] footprints;
 	protected VerticalPanel footprintPanel;
 	protected VerticalPanel optionsPanel;
 	protected VerticalPanel summaryPanel;
 	protected Image addToWorkingSet;
 	protected int cellWidth = 0;
 	protected int returnCount;
 	protected DataList list = null;
 	protected TaxonPagingLoader loader;
 	protected ListStore<TaxonListElement> store;
 	protected DataListBinder<TaxonListElement> binder;
 	protected PagingToolBar pagingToolBar;
 	protected ViewerFilterTextBox<TaxonListElement> textBox;
 	protected StoreFilter<TaxonListElement> filter;
 	protected Taxon lastTaxon = null;
 	protected boolean asCheckable = false;
 
 	public TaxonomyBrowserPanel() {
 		super();
 		
 		list = new DataList();
 		list.setScrollMode(Scroll.AUTOY);
 
 		loader = new TaxonPagingLoader();
 
 		store = new ListStore<TaxonListElement>(loader.getPagingLoader());
 
 		binder = new DataListBinder<TaxonListElement>(list, store);
 		binder.setDisplayProperty("name");
 		binder.setStyleProvider(new ModelStringProvider<TaxonListElement>() {
 			public String getStringValue(TaxonListElement model, String property) {
 				if( model.getNode().isDeprecated() )
 					return "deleted";
 				else
 					return "color-dark-blue";
 					
 			}
 		});
 		
 		textBox = new ViewerFilterTextBox<TaxonListElement>();
 		textBox.bind(store);
 
 		filter = new StoreFilter<TaxonListElement>() {
 			public boolean select(Store store, TaxonListElement parent, TaxonListElement item, String property) {
 				String txt = textBox.getText();
 				if (txt != null && !txt.equals("")) {
 					String elementString = (item).getNode().getName().toLowerCase();
 					return elementString.toLowerCase().startsWith(txt.toLowerCase());
 				}
 				return true;
 			}
 		};
 
 		final StoreSorter<TaxonListElement> sorter = new StoreSorter(TaxonComparator.getInstance());
 		
 		store.setStoreSorter(sorter);
 		store.addFilter(filter);
 
 		pagingToolBar = new PagingToolBar(40);
 		pagingToolBar.bind(loader.getPagingLoader());
 	}
 
 	protected void addViewButtonToFootprint() {
 		Button view = new Button("View", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				TaxonomyCache.impl.fetchTaxon(Integer.valueOf(footprints[footprints.length - 1]), true, new GenericCallback<Taxon>() {
 					public void onFailure(Throwable caught) {
 						// TODO Auto-generated method stub
 
 					}
 
 					public void onSuccess(Taxon result) {
 						updateNodeSummary();
 
 					}
 				});
 
 			}
 		});
 
 		footprintPanel.add(view);
 	}
 
 	protected void display(NativeDocument ndoc) {
 		String footprint = ndoc.getDocumentElement().getElementByTagName("footprint").getText();
 
 		if (footprint.indexOf("-") > 0)
 			footprints = footprint.split("-");
 		else if (!footprint.equals(""))
 			footprints = new String[] { footprint };
 		else
 			footprints = new String[0];
 
 		updateFootprintPanel();
 		updateBody(ndoc, footprint, 0);
 		resizeList(getOffsetWidth(), getOffsetHeight());
 		onChangedTaxon();
 		layout();
 	}
 
 	/**
 	 * Does nothing by default, but is AFTER a user selects a new taxon, and the hierarchy browser
 	 * has been updated to reflect the new taxon. 
 	 */
 	protected void onChangedTaxon() {
 	}
 	
 	public DataListBinder<TaxonListElement> getBinder() {
 		return binder;
 	}
 
 	public DataList getList() {
 		return list;
 	}
 	
 	public ListStore<TaxonListElement> getStore() {
 		return store;
 	}
 	
 	/**
 	 * Fetches the "children" of the last node in the path.
 	 * 
 	 * @param path
 	 */
 	protected void fetch(String path) {
 		TaxonomyCache.impl.fetchPath(path, new GenericCallback<NativeDocument>() {
 			public void onFailure(Throwable caught) {
 			}
 
 			public void onSuccess(NativeDocument result) {
 				display(result);
 			}
 		});
 
 	}
 
 	protected void fetchWithID(String id) {
 		TaxonomyCache.impl.fetchPathWithID(id, new GenericCallback<NativeDocument>() {
 
 			public void onFailure(Throwable caught) {
 			}
 
 			public void onSuccess(NativeDocument result) {
 				display((NativeDocument) result);
 			}
 
 		});
 	}
 
 	private HorizontalPanel generateFootprintSection() {
 		HTML spacer = new HTML("Kingdoms ->");
 		spacer.setVisible(false);
 
 		HorizontalPanel inner = new HorizontalPanel();
 		inner.addStyleName("SIS_hasSmallerHTML");
 		inner.setSize("auto", "auto");
 		inner.setSpacing(2);
 		inner.add(spacer);
 
 		return inner;
 	}
 
 	protected HTML getClickableHTML(final String label, final String path) {
 		final HTML ret = new HTML(label);
 		ret.addClickListener(new ClickListener() {
 			public void onClick(Widget sender) {
 				fetch(path);
 			}
 		});
 		ret.addStyleName("SIS_HyperlinkLookAlike");
 		ret.addStyleName("inline");
 
 		if (!label.equalsIgnoreCase("Kingdoms")) {
 			TaxonomyCache.impl.fetchTaxon(Integer.valueOf(label), false, new GenericCallback<Taxon>() {
 				public void onFailure(Throwable caught) {
 				}
 
 				public void onSuccess(Taxon result) {
 					ret.setText(result.getName());
 				}
 			});
 		}
 
 		return ret;
 	}
 	
 	public boolean isAsCheckable() {
 		return asCheckable;
 	}
 
 	@Override
 	protected void onResize(int width, int height) {
 		super.onResize(width, height);
 		resizeList(width, height);
 	}
 
 	private void resizeList(int width, int height) {
 		if (list != null && footprintPanel != null && width > 0 && height > 0) {
 			int bodyWidth = width - 10;
 			int bodyHeight = height - footprintPanel.getOffsetHeight();
 
 			optionsPanel.setSize(bodyWidth + "px", bodyHeight + "px");
 
 			// Set the size a bit smaller, to take into account the text box and
 			// label
 			list.setSize(bodyWidth, bodyHeight - 75 - 25);
 		}
 	}
 
 	public void setAsCheckable(boolean asCheckable) {
 		this.asCheckable = asCheckable;
 	}
 
 	public void update() {
 		updateWithoutFetch();
 		fetch("");
 	}
 
 	public void update(String id) {
 		updateWithoutFetch();
 		fetchWithID(id);
 	}
 
 	protected void updateBody(NativeDocument ndoc, String footprint, int offset) {
 		final NativeNodeList options = ndoc.getDocumentElement().getElementsByTagName("option");
 
 		if( list.isCheckable() )
 			for( DataListItem curItem : list.getItems() )
 				curItem.setChecked(false);
 		
 		optionsPanel.clear();
 		binder.removeAllListeners();
 		// store.removeAll();
 		loader.getFullList().clear();
 		loader.getPagingLoader().setOffset(0);
 		list.getChecked().clear();
 
 		if (options.getLength() == 0)
 			optionsPanel.add(new HTML("No " + Taxon.getDisplayableLevel(footprint.split("-").length) + "."));
 		else {
 			if (footprint.equals("")) {
 				optionsPanel.add(new HTML("<b>Kingdom</b><br />"));
 			} else {
 				optionsPanel.add(new HTML("<b>" + Taxon.getDisplayableLevel(footprint.split("-").length)
 						+ "</b><br />"));
 			}
 
 			if (!footprint.equals("") && !footprint.endsWith("-"))
 				footprint += "-";
 			
 			if (asCheckable) {
 				if (footprints.length >= TaxonLevel.ORDER) {
 					list.setSelectionMode(SelectionMode.SIMPLE);
 					list.setCheckable(true);
 				} else {
 					list.setSelectionMode(SelectionMode.SIMPLE);
 					list.setCheckable(false);
 				}
 			}
 
 			returnCount = 0;
 
 			// CREATE CSV TO USE TO FECTH ELEMENTS
 			List<Integer> idList = new ArrayList<Integer>();
 			for (int i = 0; i < options.getLength(); i++) {
 				String id = options.elementAt(i).getText();
 				idList.add(Integer.parseInt(id));
 			}
 			final String foot = footprint;
 			if (idList.size() > 0) {
 				TaxonomyCache.impl.fetchList(idList, new GenericCallback<String>() {
 					public void onFailure(Throwable caught) {
 					}
 
 					public void onSuccess(String arg0) {
						loader.getFullList().clear();
 						for (int i = 0; i < options.getLength(); i++) {
 							Taxon node = TaxonomyCache.impl.getTaxon(options.elementAt(i).getText());
 							if( node != null ) {
 								String path = foot + node.getId();
 								TaxonListElement el = new TaxonListElement(node, path);
 
 								//							if( shouldShow(node) )
 								loader.getFullList().add(el);
 							}
 						}
 
 						ArrayUtils.quicksort((ArrayList) loader.getFullList(), new Comparator<TaxonListElement>() {
 							public int compare(TaxonListElement o1, TaxonListElement o2) {
 								return ((String) o1.get("name")).compareTo((String) o2.get("name"));
 							}
 						});
 						loader.getPagingLoader().load();
 
 						layout();
 					}
 				});
 			}
 
 			binder.addSelectionChangedListener(new SelectionChangedListener<TaxonListElement>() {
 				@Override
 				public void selectionChanged(SelectionChangedEvent<TaxonListElement> se) {
 					if (se.getSelectedItem() != null)
 						fetch(se.getSelectedItem().getFootprint());
 				}
 			});
 
 			// binder.init();
 			optionsPanel.add(textBox);
 			optionsPanel.add(list);
 			optionsPanel.add(pagingToolBar);
 			optionsPanel.add(new Html("* denotes new taxon"));
 
 			layout();
 		}
 
 	}
 
 	private void updateFootprintPanel() {
 		if (footprintPanel == null)
 			return;
 
 		footprintPanel.clear();
 
 		if (footprints.length == 0)
 			footprintPanel.add(new HTML("Kingdoms"));
 		else {
 			SysDebugger.getNamedInstance("force").println("CW: " + cellWidth);
 			cellWidth = getOffsetWidth();
 
 			HorizontalPanel inner = generateFootprintSection();
 			footprintPanel.add(inner);
 
 			HTML curHTML;
 			HTML arrow;
 
 			inner.clear();
 			inner.add(getClickableHTML("Kingdoms", ""));
 
 			String path = "";
 			for (int i = 0; i < footprints.length; i++) {
 				path += footprints[i] + "-";
 				arrow = new HTML("->");
 				if (i < footprints.length - 1)
 					curHTML = getClickableHTML(footprints[i], path.substring(0, path.length() - 1));
 				else
 					curHTML = new HTML(TaxonomyCache.impl.getTaxon(footprints[footprints.length - 1]).getName());
 
 				inner.add(arrow);
 				if ((inner.getOffsetWidth() + arrow.getOffsetWidth() + 5) > cellWidth) {
 					inner.remove(arrow);
 					inner = generateFootprintSection();
 					footprintPanel.add(inner);
 				}
 				inner.add(arrow);
 
 				inner.add(curHTML);
 				if (inner.getOffsetWidth() + curHTML.getOffsetWidth() + 5 > cellWidth) {
 					inner.remove(curHTML);
 					inner = generateFootprintSection();
 					footprintPanel.add(inner);
 				}
 				inner.add(curHTML);
 			}
 
 			addViewButtonToFootprint();
 		}
 	}
 
 	protected void updateNodeSummary() {
 
 		String nodeID;
 
 		if (footprints.length != 0)
 			nodeID = footprints[footprints.length - 1];
 		else
 			nodeID = "";
 
 		if (nodeID != "")
 			ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(Integer.valueOf(nodeID));
 	}
 
 	public void updateWithoutFetch() {
 		removeAll();
 
 		summaryPanel = new VerticalPanel();
 		summaryPanel.addStyleName("padded");
 
 		footprints = new String[0];
 		footprintPanel = new VerticalPanel();
 		footprintPanel.addStyleName("padded");
 		HorizontalPanel inner = new HorizontalPanel();
 		inner.addStyleName("SIS_hasSmallerHTML");
 		footprintPanel.add(inner);
 		footprintPanel.addStyleName("SIS_hasSmallerHTML");
 		footprintPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		optionsPanel = new VerticalPanel();
 		optionsPanel.setSpacing(3);
 
 		add(footprintPanel);
 		add(summaryPanel);
 		add(optionsPanel);
 
 		layout();
 	}
 
 }
