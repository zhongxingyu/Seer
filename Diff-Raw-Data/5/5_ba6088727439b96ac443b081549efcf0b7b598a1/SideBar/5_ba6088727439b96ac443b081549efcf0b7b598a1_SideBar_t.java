 package com.game.rania.screen.part;
 
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.Color;
 import com.game.rania.RaniaGame;
 import com.game.rania.controller.Controllers;
 import com.game.rania.controller.LocationController;
 import com.game.rania.model.MultilineText;
 import com.game.rania.model.Text;
 import com.game.rania.model.element.Font;
 import com.game.rania.model.element.Group;
 import com.game.rania.model.element.RegionID;
 import com.game.rania.model.ui.ChatList;
 import com.game.rania.model.ui.Edit;
 import com.game.rania.model.ui.EditAction;
 import com.game.rania.model.ui.PressedButton;
 import com.game.rania.model.ui.RadioButton;
 import com.game.rania.model.ui.RadioGroup;
 import com.game.rania.model.ui.TouchAction;
 import com.game.rania.view.MainView;
 
 public class SideBar extends Group implements Part{
 
 	private MainView 		   mView 	   = RaniaGame.mView;
 	private LocationController lController = Controllers.locController;
 
 	private Group		  menu       	= new Group();
 	private PressedButton panelBlank 	= null;
 	private PressedButton btnCancel 	= null;
 	private PressedButton btnDisconnect = null;
 
 	private Group		  panel       	= new Group();
 	private PressedButton btnMenu 		= null;
 	private PressedButton btnChat 		= null;
 
 	public Group		  chat       	= new Group();
 	public boolean		  chatVisible	= false;
 	public ChatList	      fieldChat		= null;
 	public Edit		      editUser		= null;
 	public PressedButton  btnSend 		= null;
 	
 	public RadioGroup	  grpButtons	= null;
 	public RadioButton    btnMain 		= null;
 	public RadioButton    btnLocation   = null;
 	public RadioButton    btnPlanet     = null;
 	public RadioButton    btnDomain     = null;
 	public RadioButton    btnPrivate    = null;
 	
 	public SideBar(){
 		loadPart();
 	}
 
 	@Override
 	public void loadPart(){
 		mView.loadTexture("data/gui/blank.png", RegionID.BLANK, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHAT_OFF, 192, 192, 96, 96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHAT_ON,  192, 288, 96, 96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_MENU_OFF, 288, 192, 96,  96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_MENU_ON,  288, 288, 96,  96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHANNEL_MAIN_OFF,  0, 0, 96,  96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHANNEL_MAIN_ON, 0, 96, 96,  96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHANNEL_LOCATION_OFF,  96, 0, 96,  96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHANNEL_LOCATION_ON, 96, 96, 96,  96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHANNEL_PLANET_OFF,  192, 0, 96,  96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHANNEL_PLANET_ON, 192, 96, 96,  96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHANNEL_DOMAIN_OFF,  288, 0, 96,  96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHANNEL_DOMAIN_ON, 288, 96, 96,  96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHANNEL_PRIVATE_OFF,  0, 192, 96,  96, false);
 		mView.loadTexture("data/gui/icon_collect.png", RegionID.BTN_UI_CHANNEL_PRIVATE_ON, 0, 288, 96,  96, false);
 		mView.loadTexture("data/gui/ui_back.png", RegionID.BTN_UI_BACK_OFF, 0,   0, 450, 100, false);
 		mView.loadTexture("data/gui/ui_back.png", RegionID.BTN_UI_BACK_ON,  0, 100, 450, 100, false);
 		mView.loadTexture("data/gui/ui_exit.png", RegionID.BTN_UI_EXIT_OFF, 0,   0, 450, 100, false);
 		mView.loadTexture("data/gui/ui_exit.png", RegionID.BTN_UI_EXIT_ON,  0, 100, 450, 100, false);
 		mView.loadTexture("data/gui/chat_field.png", RegionID.FIELD_CHAT, false);
 		mView.loadTexture("data/gui/chat_edit.png", RegionID.EDIT_CHAT, false);
 		mView.loadTexture("data/gui/btn_send.png", RegionID.BTN_UI_SEND_OFF,  0, 0, 128, 64, false);
 		mView.loadTexture("data/gui/btn_send.png", RegionID.BTN_UI_SEND_ON,  0, 64, 128, 64, false);
 
 		float halfWidth = mView.getHUDCamera().getWidth() * 0.5f;
 		float halfHeight  = mView.getHUDCamera().getHeight() * 0.5f;
 		
 		panelBlank = new PressedButton(RegionID.BLANK, RegionID.BLANK, 0.0f, 0.0f);
 		
 		btnDisconnect = new PressedButton(RegionID.BTN_UI_EXIT_OFF,
 				  						  RegionID.BTN_UI_EXIT_ON,
 				  						  halfWidth * 0.0f,
 				  						  -halfHeight * 0.137f,
 				  						  new TouchAction() {
 											  @Override
 											  public void execute(boolean touch) {
 												  Controllers.netController.disconnect();
 											  }
 										 });
 
 		btnCancel = new PressedButton(RegionID.BTN_UI_BACK_OFF,
 				  					  RegionID.BTN_UI_BACK_ON,
 				  					  halfWidth * 0.0f, halfHeight * 0.137f,
 				  					  new TouchAction() {
 										  @Override
 										  public void execute(boolean touch) {
 											  	setVisible(panel);
 											  	lController.getRadar().visible = true;
 											  	Parts.getInfoPanel().setVisible(true);
											  	Parts.getSkillsPanel().setVisible(true);
 					  							RaniaGame.mController.addProcessor(lController.getPlayerController());
 											  }
 										  });
 
 		btnMenu = new PressedButton(RegionID.BTN_UI_MENU_OFF,
 				  					RegionID.BTN_UI_MENU_ON,
 				  					halfWidth * 0.9396f, -halfHeight * 0.8926f,
 				  					new TouchAction() {
 					  					@Override
 					  					public void execute(boolean touch) {
 					  						setVisible(menu);
 					  						lController.getRadar().visible = false;
 										  	Parts.getInfoPanel().setVisible(false);
										  	Parts.getSkillsPanel().setVisible(false);
 				  							RaniaGame.mController.removeProcessor(lController.getPlayerController());
 					  					}
 					  				});
 		
 		btnChat = new PressedButton(RegionID.BTN_UI_CHAT_OFF,
 				  					RegionID.BTN_UI_CHAT_ON,
 				  					halfWidth * 0.9396f, -halfHeight * 0.6963f,
 				  					new TouchAction() {
 										@Override
 										public void execute(boolean touch) {
 											chatVisible = !chatVisible;
 											chat.setVisible(chatVisible);
 										  	Parts.getInfoPanel().setVisible(!chatVisible);
										  	Parts.getSkillsPanel().setVisible(!chatVisible);
 					  						lController.getRadar().visible = !chatVisible;
 					  						if (chatVisible)
 					  							RaniaGame.mController.removeProcessor(lController.getPlayerController());
 					  						else
 					  							RaniaGame.mController.addProcessor(lController.getPlayerController());
 											editUser.setFocus();
 										}
 									});
 		
 
 		fieldChat = new ChatList(RegionID.FIELD_CHAT,
 								 -halfWidth + mView.getTextureRegion(RegionID.FIELD_CHAT).getRegionWidth() * 0.5f,
 								  halfHeight - mView.getTextureRegion(RegionID.FIELD_CHAT).getRegionHeight() * 0.5f,
 								 new MultilineText("", Font.getFont("data/fonts/Arial.ttf", 20), 
 										 		   new Color(1, 1, 1, 1),
 										 		   -mView.getTextureRegion(RegionID.FIELD_CHAT).getRegionWidth() * 0.5f + 20.0f,
 										 		   mView.getTextureRegion(RegionID.FIELD_CHAT).getRegionHeight() * 0.5f - 10.0f, 
 										 		   Text.Align.LEFT, Text.Align.TOP),
 							     mView.getTextureRegion(RegionID.FIELD_CHAT).getRegionWidth() * 0.95f,
 								 mView.getTextureRegion(RegionID.FIELD_CHAT).getRegionHeight() * 0.95f);
 		
 		editUser = new Edit(RegionID.EDIT_CHAT,
 							RegionID.EDIT_CHAT,
 							-halfWidth + mView.getTextureRegion(RegionID.EDIT_CHAT).getRegionWidth() * 0.5f,
 							-halfHeight	+ mView.getTextureRegion(RegionID.EDIT_CHAT).getRegionHeight() * 0.5f,
 							new Text("", Font.getFont("data/fonts/Arial.ttf", 20),
 							         new Color(1, 1, 1, 1),
 							         -mView.getTextureRegion(RegionID.EDIT_CHAT).getRegionWidth() * 0.5f + 20.0f,
 									 0,
 									 Text.Align.LEFT, Text.Align.CENTER),
 							255,
 							new EditAction() {
 								@Override
 								public void execute(Edit edit) {
 									Controllers.netController.sendChatMessage(edit.getText(), fieldChat.getCurrentChannel());
 									edit.setText("");
 									edit.setFocus();
 								}
 							});
 		
 		btnSend = new PressedButton(RegionID.BTN_UI_SEND_OFF,
 									RegionID.BTN_UI_SEND_ON,
 									-halfWidth + mView.getTextureRegion(RegionID.EDIT_CHAT).getRegionWidth()
 									+ mView.getTextureRegion(RegionID.BTN_UI_SEND_OFF).getRegionWidth() * 0.5f,
 									editUser.position.y,
 									new TouchAction() {
 										@Override
 										public void execute(boolean touch) {
 											editUser.keyUp(Input.Keys.ENTER);
 										}
 									});
 		
 		grpButtons = new RadioGroup();
 		btnMain = new RadioButton(RegionID.BTN_UI_CHANNEL_MAIN_OFF,
 								  RegionID.BTN_UI_CHANNEL_MAIN_ON,
 								  halfWidth * 0.9396f,
 								  halfHeight - mView.getTextureRegion(RegionID.BTN_UI_CHANNEL_MAIN_OFF).getRegionHeight() * 0.75f,
 								  new TouchAction() {
 								      @Override
 									  public void execute(boolean touch) {
 										  fieldChat.setCurrentChannel(ChatList.mainChannel);
 									  }
 								  });
 		btnMain.setCheck(true);
 		grpButtons.addButton(btnMain);
 		
 		btnLocation = new RadioButton(RegionID.BTN_UI_CHANNEL_LOCATION_OFF,
 									  RegionID.BTN_UI_CHANNEL_LOCATION_ON,
 									  halfWidth * 0.9396f,
 									  halfHeight - mView.getTextureRegion(RegionID.BTN_UI_CHANNEL_MAIN_OFF).getRegionHeight() * 2.0f ,
 									  new TouchAction() {
 									  @Override
 									  public void execute(boolean touch) {
 									          fieldChat.setCurrentChannel(ChatList.locationChannel);
 									      }
 									  });
 		grpButtons.addButton(btnLocation);
 		
 		btnPlanet = new RadioButton(RegionID.BTN_UI_CHANNEL_PLANET_OFF,
 									RegionID.BTN_UI_CHANNEL_PLANET_ON,
 									halfWidth * 0.9396f,
 									halfHeight - mView.getTextureRegion(RegionID.BTN_UI_CHANNEL_MAIN_OFF).getRegionHeight() * 3.25f ,
 									new TouchAction() {
 									    @Override
 										public void execute(boolean touch) {
 										    fieldChat.setCurrentChannel(ChatList.planetChannel);
 										}
 									});
 		grpButtons.addButton(btnPlanet);
 		
 		btnDomain = new RadioButton(RegionID.BTN_UI_CHANNEL_DOMAIN_OFF,
 									RegionID.BTN_UI_CHANNEL_DOMAIN_ON,
 									halfWidth * 0.9396f,
 									halfHeight - mView.getTextureRegion(RegionID.BTN_UI_CHANNEL_MAIN_OFF).getRegionHeight() * 4.5f ,
 									new TouchAction() {
 									    @Override
 										public void execute(boolean touch) {
 										    fieldChat.setCurrentChannel(ChatList.domainChannel);
 										}
 									});
 		grpButtons.addButton(btnDomain);
 		
 		btnPrivate = new RadioButton(RegionID.BTN_UI_CHANNEL_PRIVATE_OFF,
 									 RegionID.BTN_UI_CHANNEL_PRIVATE_ON,
 									 halfWidth * 0.9396f,
 									 halfHeight - mView.getTextureRegion(RegionID.BTN_UI_CHANNEL_MAIN_OFF).getRegionHeight() * 5.75f ,
 									 new TouchAction() {
 									     @Override
 										 public void execute(boolean touch) {
 										     fieldChat.setCurrentChannel(ChatList.privateChannel);
 										 }
 									 });
 		grpButtons.addButton(btnPrivate);
 
 		menu.addElement(panelBlank);
 		menu.addElement(btnDisconnect);
 		menu.addElement(btnCancel);
 		addElement(menu);
 		
 		panel.addElement(btnMenu);
 		panel.addElement(btnChat);
 		addElement(panel);
 		
 		chat.addElement(fieldChat);
 		chat.addElement(editUser);
 		chat.addElement(btnSend);
 		chat.addElement(btnMain);
 		chat.addElement(btnLocation);
 		chat.addElement(btnPlanet);
 		chat.addElement(btnDomain);
 		chat.addElement(btnPrivate);
 		addElement(chat);
 	}
 
 	@Override
 	public void unloadPart(){
 		mView.unloadTexture("data/gui/blank.png");
 		mView.unloadTexture("data/gui/ui_menu.png");
 		mView.unloadTexture("data/gui/ui_menu.png");
 		mView.unloadTexture("data/gui/ui_chat.png");
 		mView.unloadTexture("data/gui/ui_chat.png");
 		mView.unloadTexture("data/gui/ui_back.png");
 		mView.unloadTexture("data/gui/ui_back.png");
 		mView.unloadTexture("data/gui/ui_exit.png");
 		mView.unloadTexture("data/gui/ui_exit.png");
 		mView.unloadTexture("data/gui/chat_field.png");
 		mView.unloadTexture("data/gui/chat_edit.png");
 		mView.unloadTexture("data/gui/btn_send.png");
 		mView.unloadTexture("data/gui/btn_send.png");
 	}
 
 	@Override
 	public void addPart() {
 		setVisible(panel);
 		RaniaGame.mController.addHUDObject(this);
 	}
 
 	@Override
 	public void removePart() {
 		RaniaGame.mController.removeHUDObject(this);
 	}
 }
