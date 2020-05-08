 package org.iucn.sis.client.panels.workingsets;
 
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.api.ui.models.workingset.WSStore;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
 import org.iucn.sis.client.tabs.WorkingSetPage;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.DataList;
 import com.extjs.gxt.ui.client.widget.DataListItem;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.InfoConfig;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.user.client.ui.HTML;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.utils.RowParser;
 import com.solertium.util.extjs.client.WindowUtils;
 
 @SuppressWarnings("deprecation")
 public class DeleteWorkingSetPanel extends RefreshLayoutContainer {
 
 	private WorkingSetPage parent = null;
 
 	// private DataListItem selectedItem = null;
 	private DataList list;
 
 	// private ListStore<WSModel> store;
 
 	public DeleteWorkingSetPanel(WorkingSetPage parent) {
 		super();
 		this.parent = parent;
 		list = new DataList();
 		list.addStyleName("gwt-background");
 		list.setScrollMode(Scroll.AUTOY);
 		
 
 		RowLayout layout = new RowLayout();
 		setLayout(layout);
 		addStyleName("gwt-background");
 
 		HTML html = new HTML(
 				"<b> Instructions: </b> To delete a working set, select the working set"
 						+ " you wish to remove, and then click delete.  If you delete a public working set, you are simply unsubscribing to "
 						+ "it, and you may resubscribe later.  Removing a private working set will not delete the taxa or"
 						+ " the assessments associated with the deleted working set.");
 		add(html, new RowData(1d, -1));
 
 		add(list, new RowData(1d, 1d));
 		
 		
 		ButtonBar buttons = new ButtonBar();
 		buttons.setAlignment(HorizontalAlignment.LEFT);
 		buttons.add(new Button("Delete", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				if (list.getSelectedItem() != null) {
 					WindowUtils.confirmAlert("Delete working set?", "Are you sure you want to delete the working set "
 							+ list.getSelectedItem().getText() + "?", new WindowUtils.SimpleMessageBoxListener() {
 						@Override
 						public void onYes() {
 							WorkingSet workingSet = list.getSelectedItem().getData("value");
 							remove(workingSet, true);
 						}
 					});
 				} else {
 					WindowUtils.errorAlert("Error", "Please first select a working set to delete.");
 				}
 			}
 		}));
 		buttons.add(new Button("Unsubscribe", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				if (list.getSelectedItem() != null) {
 					WindowUtils.confirmAlert("Unsubscribe?", "Are you sure you want to unsubscribe " +
 							"to the working set " + list.getSelectedItem().getText() + "? " +
 							"You will be able to subscribe again if your permissions are unchanged.",
 								new WindowUtils.SimpleMessageBoxListener() {
 						public void onYes() {
 							WorkingSet workingSet = list.getSelectedItem().getData("value");
 							remove(workingSet, false);
 						}
 					});
 				} else {
 					WindowUtils.errorAlert("Error", "Please first select a working set to delete.");
 				}
 			}
 		}));
 		buttons.add(new Button("Cancel", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				cancel();
 			}
 		}));
 		add(buttons, new RowData(1d, -1));
 
 	}
 
 	private void cancel() {
 		parent.setManagerTab();
 	}
 
 	private void remove(final WorkingSet ws, final boolean delete) {
 		final GenericCallback<String> wayback = new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				Info.display(new InfoConfig("ERROR", "Failed to delete/unsubscribe to working set " + ws.getWorkingSetName()));
 				//((Button)buttons.getItemByItemId(Dialog.CANCEL)).setText("Done");
 			}
 
 			public void onSuccess(String arg0) {
 				String message = delete ? "Deleted working set " + ws.getWorkingSetName() : 
 					"Unsubscribed from working set " + ws.getWorkingSetName();
 				
 				WindowUtils.infoAlert("Success", message);
 				// manager.workingSetHierarchy.update();
 				WSStore.getStore().update();
				//if (WorkingSetCache.impl.getCurrentWorkingSet() != null && ws.getId() == WorkingSetCache.impl.getCurrentWorkingSet().getId()) {
 					//WorkingSetCache.impl.resetCurrentWorkingSet();
 					StateManager.impl.reset();
 					//manager.workingSetFullPanel.buildInfo();
				//}
 				// ((Button)buttons.getItemByItemId(Dialog.CANCEL)).setText("Done");
 				// update();
 			}
 		};
 		
 		if( delete ) {
 			if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.DELETE, ws)) {
 				ensurePermissionsCleared(ws.getId(), new GenericCallback<String>() {
 					public void onSuccess(String result) {
 						WorkingSetCache.impl.deleteWorkingSet(ws, wayback);
 					}
 					public void onFailure(Throwable caught) {
 						WindowUtils.errorAlert("Permission Error", "There are still users that are granted permissions via this Working Set. " +
 						"Before you can delete, please visit the Permission Manager and remove all of these users.");
 					}
 				});
 			} else
 				WindowUtils.errorAlert("Unauthorized", "You are not authorized to delete this working set.");
 		} else
 			WorkingSetCache.impl.unsubscribeToWorkingSet(ws, wayback);
 	}
 
 
 	private void ensurePermissionsCleared(final Integer wsID, final GenericCallback<String> callback) {
 		final String permGroupName = "ws" + wsID;
 		final String query = "?quickgroup=" + permGroupName + "";
 		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 		document.get(UriBase.getInstance().getUserBase()
 				+ "/browse/profile" + query, new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 			}
 
 			public void onSuccess(String result) {
 				final RowParser parser = new RowParser(document);
 				
 				if( parser.getRows().size() > 0 )
 					callback.onFailure(null);
 				else
 					callback.onSuccess(null);
 			}
 		});
 	}
 	
 	@Override
 	public void show() {
 		// if (selectedItem != null)
 		// list.getSelectionModel().select(selectedItem);
 		super.show();
 	}
 
 	@Override
 	public void refresh() {
 		list.removeAll();
 		for (WorkingSet workingSet : WorkingSetCache.impl.getWorkingSets().values()) {
 			String name = workingSet.getWorkingSetName();
 			DataListItem item = new DataListItem(name);
 			item.setIconStyle("tree-folder");
 			item.setId(workingSet.getId() + "");
 			item.setData("value", workingSet);
 			
 			list.add(item);
 		}
 	}
 	
 }
