 package com.szas.server.gwt.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.szas.data.UserTuple;
 import com.szas.sync.ContentObserver;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ClickEvent;
 
 public class UsersList extends Composite  {
 	
 	private static class UserTupleKeyProvider implements ProvidesKey<UserTuple> {
 
 		@Override
 		public Object getKey(UserTuple item) {
 			return (item == null) ? null : item.getId();
 		}
 
 	}
 
 	private static UsersListUiBinder uiBinder = GWT
 	.create(UsersListUiBinder.class);
 	@UiField(provided=true) CellTable<UserTuple> cellTable = createTable();
 	@UiField Button addButton;
 	private SingleSelectionModel<UserTuple> selectionModel;
 	private TextColumn<UserTuple> nameColumn;
 	private ContentObserver contentObserver;
 	private List<UserTuple> list;
 	
 	protected CellTable<UserTuple> createTable() {
 		CellTable<UserTuple> cellTable;
 		UserTupleKeyProvider userTupleKeyProvider = new UserTupleKeyProvider();
 		cellTable = new CellTable<UserTuple>(userTupleKeyProvider);
 		
 		selectionModel = new SingleSelectionModel<UserTuple>(
 				userTupleKeyProvider);
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				changeSellection();
 			}
 		});
 		nameColumn = new TextColumn<UserTuple>() {
 			@Override
 			public String getValue(UserTuple userTuple) {
 				return userTuple.getName();
 			}
 		};
 		nameColumn.setSortable(true);
 		cellTable.addColumn(nameColumn, "User name");
 		cellTable.setSelectionModel(selectionModel);	
 		
 		ListDataProvider<UserTuple> dataProvider = new ListDataProvider<UserTuple>();
 		dataProvider.addDataDisplay(cellTable);
 
 		list = dataProvider.getList();
 		
 		return cellTable;
 	}
 
 	protected void changeSellection() {
 		UserTuple userTuple = selectionModel.getSelectedObject();
 		if (userTuple == null)
 			return;
 		History.newItem("user," + userTuple.getId(),true);
 	}
 
 	interface UsersListUiBinder extends UiBinder<Widget, UsersList> {
 	}
 	
 	@Override
 	protected void onAttach() {
 		super.onAttach();
 		contentObserver = new ContentObserver() {
 
 			@Override
 			public void onChange() {
 				usersUpdated();
 			}
 		};
 		StaticGWTSyncer.getUsersdao().addContentObserver(contentObserver);
 	}
 	
 	protected void usersUpdated() {
 		ArrayList<UserTuple> users = StaticGWTSyncer.getUsersdao().getAll();
 		cellTable.setRowCount(users.size(), true);
 		while (list.size() != 0)
 			list.remove(0);
 		for (UserTuple user : users) {
 			list.add(user);
 		}
 	}
 
 	@Override
 	protected void onDetach() {
 		if (contentObserver != null)
 			StaticGWTSyncer.getUsersdao().removeContentObserver(contentObserver);
 		contentObserver = null;
 		super.onDetach();
 	}
 
 	public UsersList() {
 		initWidget(uiBinder.createAndBindUi(this));
 		usersUpdated();
 	}
	
	@UiHandler("addButton")
	void onAddButtonClick(ClickEvent event) {
		History.newItem("user",true);
	}
 }
