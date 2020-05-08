 package net.thesocialos.client.view;
 
 import java.util.Map;
 
 import net.thesocialos.client.CacheLayer;
 import net.thesocialos.client.helper.SearchArrayList;
 import net.thesocialos.shared.model.SharedHistory.SHARETYPE;
 import net.thesocialos.shared.model.User;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 public class ShareSend extends Composite {
 	
 	private static ShareSendUiBinder uiBinder = GWT.create(ShareSendUiBinder.class);
 	@UiField Label lblType;
 	@UiField Label lblTypeValue;
 	@UiField(provided = true) CellList<User> cellList = new CellList<User>(new AbstractCell<User>() {
 		@Override
 		public void render(Context context, User value, SafeHtmlBuilder sb) {
 			if (value == null) return;
 			
 			sb.appendHtmlConstant("<table class='chat_state_offline'  width= '100%'>");
 			
 			// Add the contact image.
 			
 			sb.appendHtmlConstant("<tr><td rowspan='3'>");
 			if (value.getUrlAvatar() == null) sb
 					.appendHtmlConstant("<img src='./images/anonymous_avatar.png' width='30' height='35' />");
 			else
 				sb.appendHtmlConstant("<img src=" + value.getUrlAvatar() + " width='30' height='35' />");
 			
 			sb.appendHtmlConstant("</td>");
 			
 			// Add the name and address.
 			sb.appendHtmlConstant("<td style='font-size:95%;'>");
 			sb.appendEscaped(value.getName() + " " + value.getLastName());
 			sb.appendHtmlConstant("</td></tr><tr><td>");
 			sb.appendEscaped(value.getEmail());
 			sb.appendHtmlConstant("</td></tr></table>");
 		}
 	});
 	@UiField Button buttonSend;
 	@UiField TextBox searchBox;
 	@UiField TextBox txtTitle;
 	
 	SingleSelectionModel<User> selectionModel;
 	
 	ListDataProvider<User> dataProvider;
 	/*
 	 * Los modelos de la cajas de seleccion de los usuarios
 	 */
 	ProvidesKey<User> KEY_USERS_PROVIDER;
 	
 	SearchArrayList usersList = new SearchArrayList();
 	
 	private SHARETYPE shareType;
 	
 	private String url;
 	
 	interface ShareSendUiBinder extends UiBinder<Widget, ShareSend> {
 	}
 	
 	public ShareSend() {
 		initWidget(uiBinder.createAndBindUi(this));
 		KEY_USERS_PROVIDER = new ProvidesKey<User>() {
 			@Override
 			public Object getKey(User item) {
 				return item == null ? null : item.getEmail();
 			}
 		};
 		/*
 		 * Inicializado el adaptador de la cellList de usuario
 		 */
 		selectionModel = new SingleSelectionModel<User>(KEY_USERS_PROVIDER);
 		cellList.setSelectionModel(selectionModel);
 		dataProvider = new ListDataProvider<User>(usersList);
 		dataProvider.addDataDisplay(cellList);
 		init();
 		handlers();
 	}
 	
 	public ShareSend(SHARETYPE shareType, String url) {
 		this();
 		lblTypeValue.setText(shareType.name());
 		this.shareType = shareType;
 		this.url = url;
 	}
 	
 	private void init() {
 		buttonSend.setEnabled(false);
 		usersList.clear();
 		dataProvider.flush();
 		dataProvider.refresh();
		CacheLayer.ContactCalls.getContactsWithoutKey(true, new AsyncCallback<Map<String, User>>() {
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onSuccess(Map<String, User> result) {
 				
 				result.remove(CacheLayer.UserCalls.getUser().getEmail());
 				usersList.addAll(result.values());
 				dataProvider.flush();
 				dataProvider.refresh();
 				
 			}
 		});
 	}
 	
 	private void handlers() {
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				buttonSend.setEnabled(true);
 			}
 		});
 		buttonSend.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				// TODO Auto-generated method stub
 				if (selectionModel.getSelectedObject() != null)
 					CacheLayer.ContactCalls.sendShareToContact(selectionModel.getSelectedObject().getOwnKey(),
 							shareType, txtTitle.getText(), url, new AsyncCallback<Boolean>() {
 								
 								@Override
 								public void onFailure(Throwable caught) {
 									// TODO Auto-generated method stub
 									
 								}
 								
 								@Override
 								public void onSuccess(Boolean result) {
 									/*
 									 * Aqui haces lo que quieras. Es al terminar la subida del mensaje
 									 */
 									
 								}
 							});
 			}
 		});
 		searchBox.addKeyUpHandler(new KeyUpHandler() {
 			
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				// TODO Auto-generated method stub
 				// System.out.println(display.getSearchBox().getText());
 				// ArrayList<User> lista = ;
 				dataProvider.setList(usersList.getSearchUsers(searchBox.getText()));
 				// System.out.println(lista.size());
 				dataProvider.flush();
 				dataProvider.refresh();
 			}
 		});
 	}
 }
