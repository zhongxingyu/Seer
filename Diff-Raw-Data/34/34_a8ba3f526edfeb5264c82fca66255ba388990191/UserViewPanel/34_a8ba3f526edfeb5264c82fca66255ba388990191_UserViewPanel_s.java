 /**
  *
  */
 package org.iucn.sis.client.panels.users;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.UserStore;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.models.ClientUser;
 import org.iucn.sis.client.api.ui.models.users.UserModelData;
 import org.iucn.sis.client.api.utils.PagingPanel;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.panels.users.UserViewToolBar.UserViewToolbarAPI;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
 import org.iucn.sis.shared.api.models.User;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.StoreEvent;
 import com.extjs.gxt.ui.client.store.StoreListener;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.grid.CellEditor;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
 import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
 import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
 import com.extjs.gxt.ui.client.widget.grid.filters.ListFilter;
 import com.extjs.gxt.ui.client.widget.grid.filters.NumericFilter;
 import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.HTML;
 import com.solertium.lwxml.shared.GWTConflictException;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 /**
  * UserViewPanel.java
  * 
  * Panel that allows for perusing of users to view and in-line edit their
  * profile information.
  * 
  * @author carl.scott <carl.scott@solertium.com>
  * 
  */
 public class UserViewPanel extends PagingPanel<UserModelData> implements DrawsLazily, UserViewToolbarAPI {
 	
 	private final HasStore store;
 	
 	private EditorGrid<UserModelData> userGrid;
 	private UserPermissionPanel permissionGroups;
 	
 	private boolean isDrawn = false;
 	
 	private StoreListener<UserModelData> listener;
 
 	public UserViewPanel(HasStore store) {
 		super();
 		setLayout(new BorderLayout());
 		setScrollMode(Scroll.NONE);
 		
 		this.store = store;
 
 		permissionGroups = new UserPermissionPanel();
 		
 		setPageCount(50);
 		getProxy().setSort(false);
 		
 		listener = new StoreListener<UserModelData>() {
 			public void storeAdd(StoreEvent<UserModelData> se) {
 				refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 			}
 			public void storeRemove(StoreEvent<UserModelData> se) {
 				refresh(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 			}
 		};
 	}
 	
 	public void draw(final DrawsLazily.DoneDrawingCallback callback) {
 		if (isDrawn) {
 			refresh(callback);
 			return;
 		}
 		
 		final GridSelectionModel<UserModelData> sm = new GridSelectionModel<UserModelData>();
 		sm.setSelectionMode(SelectionMode.SINGLE);
 
 		userGrid = new EditorGrid<UserModelData>(getStoreInstance(), getColumnModel());
 		userGrid.setSelectionModel(sm);
 		userGrid.setBorders(false);
 		userGrid.setClicksToEdit(ClicksToEdit.TWO);
 		userGrid.addPlugin(getGridFilters());
 		userGrid.addListener(Events.AfterEdit, new Listener<GridEvent<UserModelData>>() {
 			public void handleEvent(final GridEvent<UserModelData> be) {
 				final UserModelData model = be.getGrid().getStore().getAt(be.getRowIndex());
 				
 				final String col = be.getGrid().getColumnModel().getColumnId(be.getColIndex());
 				final String value;
 				final String originalValue;
 				
 				// Check Nickname edits
 				if ("nickname".equals(col)){
 					if (be.getValue() != null) {
 						value = ((String) be.getValue()).trim();
					  	originalValue = (String)be.getStartValue();
 					}else{
 						value = "";
					  	originalValue = (String)be.getStartValue();
 					}
 				}else{
 					if (be.getValue() == null) {
 						be.getGrid().getStore().rejectChanges();
 						return;
 					}
				
 					value = ((String) be.getValue()).trim();
				  	originalValue = (String)be.getStartValue();
 				}

 				if ((value == null && originalValue == null) || value != null && value.equals(originalValue)) {
 					//No changes made.
 					return;
 				}

 				final String username;
 				if ("username".equals(col))
 					username = originalValue;
 				else
 					username = model.get(ClientUser.USERNAME);
 				
 				saveChange(username, (String)model.get(ClientUser.ID), col, value, new GenericCallback<String>() {
 					public void onFailure(Throwable caught) {
 						be.getGrid().getStore().rejectChanges();
 					}
 					public void onSuccess(String result) {
 						be.getGrid().getStore().commitChanges();
 						// After the username change the password should be reset for active user's						
 						if(Boolean.parseBoolean((String)model.get(ClientUser.SIS_USER)) && 
 								Integer.parseInt((String)model.get(ClientUser.STATE)) == User.ACTIVE){
 							if ("username".equals(col)){
 								resetPassword(value);
 							}
 						}
 					}
 				});
 			}
 		});
 		
 		ToolBar bar = new ToolBar();
 		if (AuthorizationCache.impl.hasRight(AuthorizableObject.USE_FEATURE,
 				AuthorizableFeature.USER_MANAGEMENT_FEATURE)) {
 			bar = new UserViewToolBar(this);
 		}
 
 		add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25));
 		add(getPagingToolbar(), new BorderLayoutData(LayoutRegion.SOUTH, 25));
 		add(userGrid, new BorderLayoutData(LayoutRegion.CENTER));
 
 		isDrawn = true;
 		
 		refresh(callback);
 	}
 	
 	private void resetPassword(final String username){
 		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.post(UriBase.getInstance().getSISBase() + "/authn/reset", "<root><u>"
 				+ username + "</u></root>", new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				Info.display("Reset Success!",
 						"A new password for {0} has been sent.", username);
 				WindowUtils.infoAlert("Password Reset", "Password Reset for the account "+username+". A new password for "+username+" has been sent.");
 			}
 			public void onFailure(Throwable caught) {
 				WindowUtils.errorAlert("Reset failed!", "Resetting this "
 						+ "user's password failed. Please check your Internet connection and try again.");
 			}
 		});
 	}
 	
 	@Override
 	protected void getStore(final GenericCallback<ListStore<UserModelData>> callback) {
 		UserStore.impl.load(new SimpleListener() {
 			public void handleEvent() {
 				store.getStore().removeStoreListener(listener);
 				store.getStore().addStoreListener(listener);
 				
 				callback.onSuccess(store.getStore());
 			}
 		});
 	}
 	
 	@Override
 	protected void refreshView() {
 		userGrid.getView().refresh(false);
 	}
 
 	public void addUser(String username) {
 		UserStore.impl.addUser(username);
 	}
 	
 	@Override
 	public UserModelData getSelectedUser() {
 		return userGrid.getSelectionModel().getSelectedItem();
 	}
 	
 	@Override
 	public void removeUser(UserModelData user) {
 		UserStore.impl.removeUser(user, store.getStore());
 	}
 	
 	public void setUserState(final UserModelData user, final int state) {
 		saveChange((String)user.get(ClientUser.USERNAME), (String)user.get(ClientUser.ID), ClientUser.STATE, Integer.toString(state), new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				if (state == ClientUser.ACTIVE)
 					UserStore.impl.activateUser(user);
 				else if (state == ClientUser.DELETED)
 					UserStore.impl.disableUser(user);
 			}
 			public void onFailure(Throwable caught) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	}
 	
 	public void refresh() {
 		permissionGroups.updateStore();
 		/*refresh(new DrawsLazily.DoneDrawingCallback() {
 			public void isDrawn() {
 				layout();
 			}
 		});*/
 	}
 	
 	private GridFilters getGridFilters() {
 		final GridFilters filters = new GridFilters();
 		filters.setLocal(false);
 		
 		filters.addFilter(new NumericFilter(ClientUser.ID));
 		filters.addFilter(new StringFilter(ClientUser.USERNAME));
 		filters.addFilter(new StringFilter(ClientUser.FIRST_NAME));
 		filters.addFilter(new StringFilter(ClientUser.LAST_NAME));
 		filters.addFilter(new StringFilter(ClientUser.NICKNAME));
 		filters.addFilter(new StringFilter(ClientUser.INITIALS));
 		filters.addFilter(new StringFilter(ClientUser.AFFILIATION));
 		
 		BaseModelData yes = new BaseModelData(); yes.set("text", "true");
 		BaseModelData no = new BaseModelData(); no.set("text", "false");
 		
 		ListStore<ModelData> boolStore = new ListStore<ModelData>();
 		boolStore.add(yes);
 		boolStore.add(no);
 		
 		ListFilter sisAccount = new ListFilter(ClientUser.SIS_USER, boolStore);
 		sisAccount.setDisplayProperty("text");
 		
 		filters.addFilter(sisAccount);
 		
 		return filters;
 	}
 
 	private ColumnModel getColumnModel() {
 		final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
 
 		final ColumnConfig id = new ColumnConfig();
 		id.setId(ClientUser.ID);
 		id.setHeader("ID");
 		id.setWidth(50);
 		id.setHidden(true);
 		configs.add(id);
 
 		final ColumnConfig name = new ColumnConfig();
 		name.setId(ClientUser.USERNAME);
 		name.setHeader("User Name");
 		name.setWidth(100);
 		{
 			final TextField<String> text = new TextField<String>();
 			text.setAllowBlank(false);
 			text.setMaxLength(255);
 			name.setEditor(new CellEditor(text));
 		}
 		configs.add(name);
 
 		final ColumnConfig first = new ColumnConfig();
 		first.setId(ClientUser.FIRST_NAME);
 		first.setWidth(100);
 		first.setHeader("First Name");
 		{
 			final TextField<String> text = new TextField<String>();
 			text.setAllowBlank(false);
 			text.setAutoValidate(true);
 			text.setMaxLength(255);
 			first.setEditor(new CellEditor(text));
 		}
 		configs.add(first);
 
 		final ColumnConfig last = new ColumnConfig();
 		last.setId(ClientUser.LAST_NAME);
 		last.setWidth(100);
 		last.setHeader("Last Name");
 		{
 			final TextField<String> text = new TextField<String>();
 			text.setAllowBlank(false);
 			text.setAutoValidate(true);
 			text.setMaxLength(255);
 			last.setEditor(new CellEditor(text));
 		}
 		configs.add(last);
 		
 		final ColumnConfig nickname = new ColumnConfig();
 		nickname.setId(ClientUser.NICKNAME);
 		nickname.setWidth(100);
 		nickname.setHeader("Nickname");
 		{
 			final TextField<String> text = new TextField<String>();
 			text.setAllowBlank(true);
 			text.setAutoValidate(true);
 			text.setMaxLength(255);
 			nickname.setEditor(new CellEditor(text));
 		}
 		configs.add(nickname);
 
 		final ColumnConfig initials = new ColumnConfig();
 		initials.setId(ClientUser.INITIALS);
 		initials.setWidth(60);
 		initials.setHeader("Initials");
 		{
 			final TextField<String> text = new TextField<String>();
 			text.setAllowBlank(false);
 			text.setAutoValidate(true);
 			text.setMaxLength(255);
 			initials.setEditor(new CellEditor(text));
 			initials.getEditor().setToolTip(
 					"If supplied, initials will be used in publications."
 							+ "Otherwise, the first character of your first name will be used.");
 		}
 		configs.add(initials);
 
 		final ColumnConfig affiliation = new ColumnConfig();
 		affiliation.setId(ClientUser.AFFILIATION);
 		affiliation.setWidth(120);
 		affiliation.setHeader("Affiliation");
 		{
 			final TextField<String> text = new TextField<String>();
 			text.setAllowBlank(true);
 			text.setAutoValidate(false);
 			text.setMaxLength(2000);
 			affiliation.setEditor(new CellEditor(text));
 		}
 		configs.add(affiliation);
 
 		if (AuthorizationCache.impl.hasRight(
 				AuthorizableObject.USE_FEATURE, AuthorizableFeature.PERMISSION_MANAGEMENT_FEATURE)) {
 			final ColumnConfig quickGroup = new ColumnConfig();
 			quickGroup.setId(ClientUser.QUICK_GROUP);
 			quickGroup.setWidth(120);
 			quickGroup.setAlignment(HorizontalAlignment.CENTER);
 			quickGroup.setHeader("Permission Groups");
 			quickGroup.setMenuDisabled(true);
 			quickGroup.setRenderer(new GridCellRenderer<UserModelData>() {
 				@Override
 				public Object render(final UserModelData model, final String property,
 						ColumnData config, final int rowIndex, final int colIndex,
 						final ListStore<UserModelData> store, final Grid<UserModelData> grid) {
 					HTML button = new HTML("[Edit]");
 					button.addStyleName("SIS_HyperlinkLookAlike");
 					button.addClickHandler(new ClickHandler() {
 						public void onClick(ClickEvent event) {
 							final List<String> selected = new ArrayList<String>();
 							final String q = model.get(property);
 							if (q != null)
 								for (String value : q.split(","))
 									selected.add(value);
 							
 							permissionGroups.setSaveListener(new ComplexListener<List<String>>() {
 								public void handleEvent(List<String> eventData) {
 									final StringBuilder builder = new StringBuilder();
 									for (Iterator<String> iter = eventData.iterator(); iter.hasNext(); )
 										builder.append(iter.next() + (iter.hasNext() ? "," : ""));
 									
 									model.set(property, builder.toString());
 									
 									final GridEvent<UserModelData> event = 
 										new GridEvent<UserModelData>(grid);
 									event.setProperty(property);
 									event.setValue(builder.toString());
 									event.setStartValue(q);
 									event.setRowIndex(rowIndex);
 									event.setColIndex(colIndex);
 									
 									grid.fireEvent(Events.AfterEdit, event);
 								}
 							});
 							permissionGroups.setSelection(selected);
 							permissionGroups.show();
 						}
 					});
 					
 					return button;
 				}
 			});
 			configs.add(quickGroup);
 		}
 		
 		final SimpleComboBox<String> rapidListUser = new SimpleComboBox<String>();
 		rapidListUser.setForceSelection(true);
 		rapidListUser.setTriggerAction(TriggerAction.ALL);
 		rapidListUser.add("true");
 		rapidListUser.add("false");
 		
 		if (AuthorizationCache.impl.hasRight(
 				AuthorizableObject.USE_FEATURE, AuthorizableFeature.USER_MANAGEMENT_FEATURE)) {
 			final SimpleComboBox<String> sisUser = new SimpleComboBox<String>();
 			sisUser.setForceSelection(true);
 			sisUser.setTriggerAction(TriggerAction.ALL);
 			sisUser.add("true");
 			sisUser.add("false");
 
 			final ColumnConfig sis = new ColumnConfig();
 			sis.setId(ClientUser.SIS_USER);
 			sis.setWidth(100);
 			sis.setHeader("SIS Account");
 			sis.setEditor(new CellEditor(sisUser) {
 				public Object preProcessValue(Object value) {
 					if (value == null)
 						return value;
 					return sisUser.findModel(value.toString());
 				}
 				public Object postProcessValue(Object value) {
 					if (value == null)
 						return value;
 					return ((ModelData) value).get("value");
 				}
 			});
 			
 			configs.add(sis);
 			
 			final ColumnConfig rapidlist = new ColumnConfig();
 			rapidlist.setId(ClientUser.RAPIDLIST_USER);
 			rapidlist.setWidth(120);
 			rapidlist.setHeader("RapidList Account");
 			rapidlist.setEditor(new CellEditor(rapidListUser) {
 				public Object preProcessValue(Object value) {
 					if (value == null)
 						return value;
 					return rapidListUser.findModel(value.toString());
 				}
 				public Object postProcessValue(Object value) {
 					if (value == null)
 						return value;
 					return ((ModelData) value).get("value");
 				}
 			});
 			
 			configs.add(rapidlist);
 		}
 
 		return new ColumnModel(configs);
 	}
 	
 	private void saveChange(final String username, final String userID, final String column, final String value, final GenericCallback<String> callback) {
 		final String body = "<root><field name=\"" + column + "\">" + value + "</field></root>";
 		
 		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
 		document.post(UriBase.getInstance().getUserBase() + "/users/" + userID, body,
 				new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				String error;
 				if (caught instanceof GWTConflictException) {
 					error = "An active user already exists with this username.";
 				}
 				else
 					error = "Could not save changes, please try again later.";
 				
 				WindowUtils.errorAlert(error);
 				
 				if (callback != null)
 					callback.onFailure(caught);
 			}
 			public void onSuccess(String result) {
 				Info.display("Success", "Changes saved.");
 				
 				if (callback != null)
 					callback.onSuccess(result);
 			}
 		});
 	}
 	
 	public interface HasStore {
 		
 		public ListStore<UserModelData> getStore();
 		
 	}
 
 }
