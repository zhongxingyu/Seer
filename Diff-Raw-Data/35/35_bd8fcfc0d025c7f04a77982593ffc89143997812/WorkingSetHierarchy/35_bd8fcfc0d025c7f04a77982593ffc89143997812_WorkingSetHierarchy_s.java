 package org.iucn.sis.client.panels.workingsets;
 
 import java.util.Comparator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.ui.models.workingset.WSModel;
 import org.iucn.sis.client.api.ui.models.workingset.WSStore;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.client.panels.PanelManager;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.iucn.sis.shared.api.workflow.WorkflowStatus;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.binder.DataListBinder;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.Store;
 import com.extjs.gxt.ui.client.store.StoreEvent;
 import com.extjs.gxt.ui.client.store.StoreFilter;
 import com.extjs.gxt.ui.client.store.StoreListener;
 import com.extjs.gxt.ui.client.widget.DataList;
 import com.extjs.gxt.ui.client.widget.DataListItem;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 
 /**
  * The class which is displayed on the left hand side on the working set page.
  * Shows a list of all working sets, where the store which holds the working
  * sets is drawn from WSStore, and is used by other working set panels as well.
  * 
  * @author liz.schwartz
  * 
  */
 public class WorkingSetHierarchy extends LayoutContainer {
 
 	private final DataList tree;
 	private final DataListBinder<WSModel> binder;
 	private final ComboBox<BaseModelData> box; 
 
 	private PanelManager panelManager = null;
 	private boolean isBuilt;
 
 	public WorkingSetHierarchy(PanelManager manager) {
 		super();
 		setLayout(new RowLayout(Orientation.VERTICAL));
 		panelManager = manager;
 		isBuilt = false;
 		
 		
 		tree = new DataList();
 		tree.addStyleName("gwt-background");
 		tree.setBorders(false);
 		
 		final StoreFilter<WSModel> filter = new StoreFilter<WSModel>() {
 			public boolean select(Store<WSModel> store, WSModel parent, WSModel item, String property) {
 				String value = item.get("workflow_status");
 				boolean ret = property.equalsIgnoreCase(value) || (value == null && "draft".equals(property));
 				return ret;
 			}
 		};
 		
 		final WSStore store = WSStore.getStore();
 		store.addFilter(filter);
 		store.addStoreListener(new StoreListener<WSModel>() {
 			public void storeFilter(StoreEvent<WSModel> se) {
 				binder.init();
 			}
 		});
 		
 		binder = new DataListBinder<WSModel>(tree, store);
 		binder.setDisplayProperty("name");
 		binder.addSelectionChangedListener(new SelectionChangedListener<WSModel>() {
 			public void selectionChanged(SelectionChangedEvent<WSModel> se) {
 				WSModel wsModel = se.getSelectedItem();
 				if (wsModel != null) {
 					if (WorkingSetCache.impl.getCurrentWorkingSet() == null || !Integer.valueOf(WorkingSetCache.impl.getCurrentWorkingSet().getId()).equals(wsModel.getID())) {
 						WorkingSetCache.impl.setCurrentWorkingSet(wsModel.getID(), true, new SimpleListener() {
 							public void handleEvent() {
 								panelManager.workingSetBrowser.refresh();
 							}
 						});
 					}
 				}
 
 //				panelManager.workingSetBrowser.refresh();
 			}
 		});
 		binder.init();
 		
 		box = new ComboBox<BaseModelData>();
 		box.setEmptyText("Choose Status");
 		box.setForceSelection(true);
 		
 		final Map<String, String> status = new LinkedHashMap<String, String>();
 		status.put("all", "All");
 		status.put("draft", "Draft");
 		status.put("review", "Review"); 
 		status.put("consistency-check", "Consistency Check");
 		status.put("final", "Final");
 		status.put("publish", "Publish");
 		
 		final ListStore<BaseModelData> combo = new ListStore<BaseModelData>();
 		BaseModelData sel = null;
 		for (Map.Entry<String, String> entry : status.entrySet()) {
 			final BaseModelData model = new BaseModelData();
 			model.set("value", entry.getKey());
 			model.set("text", entry.getValue());
 			combo.add(model);
 			if ("all".equals(entry.getKey()))
 				sel = model;
 		}
 		
 		box.setStore(combo);
 		box.setValue(sel);
 		box.addSelectionChangedListener(new SelectionChangedListener<BaseModelData>() {
 			public void selectionChanged(SelectionChangedEvent<BaseModelData> se) {
 				if (se.getSelectedItem() != null) {
 					String filterBy = (String)se.getSelectedItem().get("value");
 					if( !filterBy.equalsIgnoreCase("all") ) {
 						store.setFilterProperty(filterBy);
 						store.applyFilters(filterBy);
 					} else {
 						store.clearFilters();
 					}
 				}
 			}
 		});
 		
 		
 		WorkingSetCache.impl.update(new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				WindowUtils.errorAlert("Error loading working sets ...");
 				//content.removeAll();
 				//content.add(new HTML("Error loading working sets ..."));
 			}
 
 			public void onSuccess(String arg0) {
 				buildPanel();
 			}
 		});
 	}
 	
 	public void refresh(final GenericCallback<Object> callback) {
 		WorkingSetCache.impl.update(new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				WSStore.getStore().update();
 				//ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.updateWorkflowStatus();
 				callback.onSuccess(null);
 			}
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 	}
 
 	private void buildPanel() {
 		if (!isBuilt) {
 			isBuilt = true;
 			
 			add(tree, new RowData(1, 1));
 			add(box, new RowData(1, 20));
 		}
 		
 		layout();
 	}
 
 	public Integer getCurrentlySelectedWorkingSetID() {
 		List<WSModel> selected = binder.getSelection();
 		if (selected.size() == 0)
 			return null;
 		else
 			return selected.get(0).getID();
 	}
 
 	public String getCurrentlySelectedWorkingSetName() {
 		List<WSModel> selected = binder.getSelection();
 		if (selected.size() == 0)
 			return null;
 		else
 			return selected.get(0).getName();
 	}
 
 	public void setCurrentlySelected(Integer workingSetID) {
 		if (workingSetID == null) {
 			binder.setSelection((WSModel) null);
 		} else {
 			// MIGHT HAVE TO MANUALY FIRE BINDER SELECTION CHANGED EVENT
 			for (WSModel model : WSStore.getStore().getModels()) {
 				if (model.getID().equals(workingSetID)) {
 					binder.setSelection(model);
 					tree.setSelectedItem((DataListItem) binder.findItem(model));
 					WorkingSetCache.impl.setCurrentWorkingSet(model.getID(), false);
 					break;
 				}
 			}
 		}
 		panelManager.workingSetBrowser.refresh();
 
 	}
 
 	public void updateSelected() {
 		buildPanel();
 		WorkingSet ws = WorkingSetCache.impl.getCurrentWorkingSet();
 		if (ws != null) {
 			setCurrentlySelected(ws.getId());
 		}
 	}
 	
 	public static class WorkflowComparator implements Comparator<WSModel> {
 		
 		/**
 		 * Expects a string with the workflow status ... could be null.
 		 */
 		public int compare(WSModel o1, WSModel o2) {
 			if (o1 == null)
 				return -1;
 			if (o2 == null)
 				return 1;
 			
 			//Both are non-null, compare...
 			WorkflowStatus s1 = WorkflowStatus.getStatus((String)o1.get("workflow_status"));
 			WorkflowStatus s2 = WorkflowStatus.getStatus((String)o2.get("workflow_status"));
 			
 			if (s1 == null)
 				return -1;
 			if (s2 == null)
 				return 1;
 			
 			//Both are valid status, compare...
 			if (s1.equals(s2))
 				return o1.getName().compareTo(o2.getName());
 			
 			WorkflowStatus prev;
 			while ((prev = s2.getPreviousStatus()) != null && !s1.equals(prev));
 			
 			return prev.equals(s1) ? -1 : 1;
 		}
 		
 	}
 
 }
