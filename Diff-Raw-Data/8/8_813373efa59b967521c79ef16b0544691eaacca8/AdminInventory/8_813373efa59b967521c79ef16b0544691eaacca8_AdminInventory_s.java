 package theatreProject.client;
 
 import theatreProject.shared.PersistenceAsync;
 import theatreProject.shared.InventoryObject;
 import theatreProject.shared.Persistence;
 
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 //import com.google.apphosting.api.ApiProxy;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Image;
 
 public class AdminInventory {
 	private static final int RETRY_MS = 1000;
 	public final static PersistenceAsync persistence = GWT.create(Persistence.class);
 	public static InventoryObject thisObject;
 
 
 	/**
 	 * @wbp.parser.entryPoint
 	 */
 	public static void adminOnlyInventory(final String ID){		
 		final TextArea txtDescription = new TextArea();
 		final TextArea txtDisclaimers = new TextArea();
 		final TextBox txtShowDate = new TextBox();
 		final TextBox txtLocation = new TextBox();
 		final TextBox lblStatus = new TextBox();
 		final TextBox txtbxEmailAddress = new TextBox();
 		final TextBox nameOfObject = new TextBox();
 		final Hidden hiddenID = new Hidden();
 		final TextBox txtStroageArea = new TextBox();
 
 		Timer loadFields = new Timer(){
 
 			@Override
 			public void run() {
 				persistence.getInventoryObject(ID, new AsyncCallback<InventoryObject>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						thisObject = null;
 					}
 					@Override
 					public void onSuccess(InventoryObject object) {
 						thisObject = object;
 						if(thisObject == null){
 							schedule(RETRY_MS);
 						} else {
 							txtDescription.setText(thisObject.getDescription());
 							txtDisclaimers.setText(thisObject.getDisclaimers());
 							txtShowDate.setText(thisObject.getStatus().getShowDay());
 							txtLocation.setText(thisObject.getStatus().getLocation());
 							if (thisObject.getStatus().getLocation() == "warehouse") lblStatus.setText("IN");
 							else lblStatus.setText("OUT");
 							txtbxEmailAddress.setText(thisObject.getStatus().getRenter());
 							nameOfObject.setText(thisObject.getName());
 							hiddenID.setValue(thisObject.getID());
 							txtStroageArea.setText(thisObject.getStorageArea());
 						}
 					}
 				});
 			}
 
 		};
 
 		loadFields.run();
 
 		final RootPanel rootPanel = RootPanel.get();
 		rootPanel.setStyleName("gwt-Root");
 
 		final VerticalPanel manguageUserPanel = new VerticalPanel();
 		rootPanel.add(manguageUserPanel, 10 ,10 );
 		manguageUserPanel.setSize("498px", "627px");
 
 		final Label title = new Label("Admin Inventory Page");
 		title.setStyleName("gwt-Header");
 		manguageUserPanel.add(title);
 		title.setSize("338px","25px");
 
 		AbsolutePanel absolutePanel = new AbsolutePanel();
 		manguageUserPanel.add(absolutePanel);
 		absolutePanel.setSize("472px", "582px");
 
 		//Error Label
 		final Label lblCouldNotFind = new Label("COULD NOT SAVE OBJECT. PLEASE CONTACT SITE ADMINISTRATOR.");
 		lblCouldNotFind.setVisible(false);
 		absolutePanel.add(lblCouldNotFind, 24, 75);
 		lblCouldNotFind.setSize("158px", "82px");
 
 		HorizontalPanel horizontalPanel_4 = new HorizontalPanel();
 		absolutePanel.add(horizontalPanel_4, 10, 291);
 		horizontalPanel_4.setSize("434px", "98px");
 
 		//Description Box
 		Label lblNewLabel = new Label("Description: ");
 		horizontalPanel_4.add(lblNewLabel);
 
 		horizontalPanel_4.add(txtDescription);
 		txtDescription.setSize("342px", "96px");
 
 		HorizontalPanel horizontalPanel_5 = new HorizontalPanel();
 		absolutePanel.add(horizontalPanel_5, 10, 418);
 		horizontalPanel_5.setSize("433px", "110px");
 
 		Label lblDisclaimers = new Label("Disclaimers: ");
 		horizontalPanel_5.add(lblDisclaimers);
 
 		horizontalPanel_5.add(txtDisclaimers);
 		txtDisclaimers.setSize("339px", "96px");
 
 		HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
 		absolutePanel.add(horizontalPanel_1, 232, 119);
 		horizontalPanel_1.setSize("211px", "34px");
 
 		Label lblLocation = new Label("Location: ");
 		horizontalPanel_1.add(lblLocation);
 
 		horizontalPanel_1.add(txtLocation);
 		txtLocation.setWidth("136px");
 
 		HorizontalPanel horizontalPanel_7 = new HorizontalPanel();
 		absolutePanel.add(horizontalPanel_7, 232, 179);
 		horizontalPanel_7.setSize("211px", "34px");
 
 		Label lblShowEndDate = new Label("Show End Date: ");
 		horizontalPanel_7.add(lblShowEndDate);
 
 		horizontalPanel_7.add(txtShowDate);
 		txtShowDate.setWidth("130px");
 
 		HorizontalPanel horizontalPanel_2 = new HorizontalPanel();
 		absolutePanel.add(horizontalPanel_2, 233, 47);
 		horizontalPanel_2.setSize("211px", "49px");
 
 		Label lblCheckInoutStatus = new Label("Check in/out Status: ");
 		horizontalPanel_2.add(lblCheckInoutStatus);
 		lblCheckInoutStatus.setWidth("92px");
 
 		horizontalPanel_2.add(lblStatus);
 		lblStatus.setSize("97px", "22px");
 
 		HorizontalPanel horizontalPanel_6 = new HorizontalPanel();
 		absolutePanel.add(horizontalPanel_6, 232, 237);
 		horizontalPanel_6.setSize("211px", "34px");
 
 		Label lblEmailOfRenter = new Label("Location in Warehouse: ");
 		horizontalPanel_6.add(lblEmailOfRenter);
 
 		horizontalPanel_6.add(txtStroageArea);
 		txtbxEmailAddress.setWidth("111px");
 
 		HorizontalPanel horizontalPanel_8 = new HorizontalPanel();
 		absolutePanel.add(horizontalPanel_8, 10, 219);
 		horizontalPanel_8.setSize("196px", "34px");
 
 
 		final Label lblNameOfRenter = new Label("Name of Renter: ");
 		horizontalPanel_8.add(lblNameOfRenter);
 		lblNameOfRenter.setWidth("55px");
 
 		horizontalPanel_8.add(txtbxEmailAddress);
 		txtbxEmailAddress.setWidth("133px");
 
 		nameOfObject.setStyleName("gwt-Heading2");
 		absolutePanel.add(nameOfObject, 10, 10);
 		nameOfObject.setSize("419px", "18px");
 		
 		Button btnUploadImage = new Button("Upload Image");
 		absolutePanel.add(btnUploadImage, 10, 179);
 
		Image image = new Image((String) null);
		image.setAltText("Image of Item");
		absolutePanel.add(image, 10, 47);
		image.setSize("198px", "126px");
 
 		//Main Menu Button
 		Button btnMainMenu_1 = new Button("Main Menu");
 		btnMainMenu_1.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				rootPanel.clear();
 				TheatreProjectWeb.mainPage();
 			}
 		});
 		absolutePanel.add(btnMainMenu_1, 364, 545);
 
 
 		//save buttons
 		Button btnNewButton = new Button("New button");
 		btnNewButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				//start by setting thisObject parameters to current txtbxs
 				thisObject.setName(nameOfObject.getText());
 				thisObject.setDescription(txtDescription.getText());
 				thisObject.setDisclaimers(txtDisclaimers.getText());
 				//TODO
 				//thisObject.setImage(newImageURL);
 				//thisObject.setStorageArea(newStorageArea);
 				thisObject.getStatus().setLocation(txtLocation.getText());
 				thisObject.getStatus().setRenter(lblNameOfRenter.getText());
 				thisObject.getStatus().setShowDay(txtShowDate.getText());
 
 				//then save out the changes
 				persistence.saveObject(thisObject, new AsyncCallback<Void>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						lblCouldNotFind.setVisible(true);								
 					}
 					@Override
 					public void onSuccess(Void result) {
 						lblCouldNotFind.setVisible(false);
 						//when saved, it returns to the main page
 						rootPanel.clear();
 						TheatreProjectWeb.mainPage();
 					}
 				});
 			}
 		});
 
 		btnNewButton.setText("Save");
 		absolutePanel.add(btnNewButton, 10, 545);
 
 		//Delete button
 		Button btnDeleteItem = new Button("Delete Item");
 		btnDeleteItem.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				persistence.deleteObject(thisObject, new AsyncCallback<Void>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						lblCouldNotFind.setVisible(true);								
 					}
 					@Override
 					public void onSuccess(Void result) {
 						lblCouldNotFind.setVisible(false);
 						//when saved, it returns to the main page
 						rootPanel.clear();
 						TheatreProjectWeb.mainPage();
 					}
 
 				});
 			}
 		});
 				absolutePanel.add(btnDeleteItem, 73, 545);
 
 
 	}
 }
