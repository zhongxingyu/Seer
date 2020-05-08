 /*
 *
 *	Copyright (c) 2013 Andreas Reder
 *	Author      : Andreas Reder <andreas.reder@lielas.org>
 *	File		: 
 *
 *	This File is part of lielas, see www.lielas.org for more information.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 
 package lielas.ui;
 
 import lielas.LiewebUI;
 import lielas.ui.HeaderScreen;
 import lielas.ui.YesNoPopupScreen.PopupClosedListener;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.NativeButton;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.NativeSelect;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Accordion;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.NativeSelect;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.terminal.*;
 
 import lielas.core.Device;
 import lielas.core.DeviceContainer;
 import lielas.core.Channel;
 import lielas.core.Modul;
 import lielas.core.LanguageHelper;
 import lielas.core.NewDeviceContainer;
 import lielas.core.SQLHelper;
 import lielas.core.User;
 import lielas.core.UserContainer;
 
 @SuppressWarnings({ "serial", "unused" })
 public class OptionsUserDetailsScreen extends VerticalLayout{
 	private LiewebUI app;
 	private User user = null;
 	private User newUser = null;
 	
 	private Label detailListHeaderLbl = null;
 
 	private Label dlDLoginLbl = null;
 	private TextField dlCLoginTx = null;
 	private Label dlDForenameLbl = null;
 	private TextField dlCForenameTx = null;
 	private Label dlDNameLbl = null;
 	private TextField dlCNameTx = null;
 	private Label dlDTimezoneLbl = null;
 	private NativeSelect dlCTimezoneSel = null;
 	private Label dlDPasswordLbl = null;
 	private TextField dlCPasswordTx = null;
 	private Label dlDPasswordTestLbl = null;
 	private TextField dlCPasswordTestTx = null;
 	
 	private NativeButton dlSaveBttn = null;
 	private NativeButton dlDeleteBttn = null;
 	
 	
 	public OptionsUserDetailsScreen(final LiewebUI app){
 		this.app = app;
 		this.setWidth(460, Unit.PIXELS);
 		this.addStyleName("detaillist");
 	}
 	
 	public OptionsUserDetailsScreen(final LiewebUI app, User user){
 		this.app = app;
 		this.user = user;
 		this.setWidth(460, Unit.PIXELS);
 		this.addStyleName("detaillist");
 	}
 	
 	public void setUser(User user){
 		this.user = user;
 	}
 	
 	public void LoadContent(){
 		if(user == null){
 			dlCLoginTx.setValue("");
 			dlCForenameTx.setValue("");
 			dlCNameTx.setValue("");
 			dlCTimezoneSel.select("0");
 			dlCPasswordTx.setValue("");
 			dlCPasswordTestTx.setValue("");
 			
 		}else{
 			dlCLoginTx.setValue(user.getLogin());
 			dlCForenameTx.setValue(user.getForename());
 			dlCNameTx.setValue(user.getName());
 			dlCTimezoneSel.select(user.getTimezone());
 			dlCPasswordTx.setValue("*****");
 			dlCPasswordTestTx.setValue("*****");
 			
 		}
 	}
 	
 	public void Update(){
 		this.removeAllComponents();
 		this.setVisible(true);
 		
 		/************************************************************************************************************************
 		 * 		Header
 		 ************************************************************************************************************************/
 		
 		detailListHeaderLbl = new Label(app.langHelper.GetString(LanguageHelper.DM_TABLE_DL_DETAILS_CAPTION));
 		detailListHeaderLbl.setWidth(460, Unit.PIXELS);
 		detailListHeaderLbl.addStyleName("detaillist-header");
 		detailListHeaderLbl.setHeight(29, Unit.PIXELS);
 		this.addComponent(detailListHeaderLbl);
 		
 		
 		/************************************************************************************************************************
 		 * 		Body
 		 ************************************************************************************************************************/
 		
 		VerticalLayout detailListBodyLayout = new VerticalLayout();
 		detailListBodyLayout.setWidth(460, Unit.PIXELS);
 		detailListBodyLayout.setStyleName("optionsscreen-details-body");	
 		this.addComponent(detailListBodyLayout);
 		
 		//show Login
 		HorizontalLayout dlLoginLo = new HorizontalLayout();
 		dlLoginLo.setWidth(430, Unit.PIXELS);
 		dlLoginLo.setHeight(22, Unit.PIXELS);
 		dlLoginLo.addStyleName("optionsscreen-details-body");
 		detailListBodyLayout.addComponent(dlLoginLo);
 		
 		dlDLoginLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_DETAILS_LOGIN_LBL));
 		dlDLoginLbl.setWidth(215, Unit.PIXELS);
 		dlLoginLo.addComponent(dlDLoginLbl);
 		
 		dlCLoginTx = new TextField();
 		dlCLoginTx.setWidth(215, Unit.PIXELS);
 		dlCLoginTx.setStyleName("detaillist-body");
 		dlLoginLo.addComponent(dlCLoginTx);
 		
 		//show Forename
 		HorizontalLayout dlForenameLo = new HorizontalLayout();
 		dlForenameLo.setWidth(430, Unit.PIXELS);
 		dlForenameLo.setHeight(22, Unit.PIXELS);
 		dlForenameLo.addStyleName("optionsscreen-details-body");
 		detailListBodyLayout.addComponent(dlForenameLo);
 		
 		dlDForenameLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_DETAILS_FORENAME_LBL));
 		dlDForenameLbl.setWidth(215, Unit.PIXELS);
 		dlForenameLo.addComponent(dlDForenameLbl);
 		
 		dlCForenameTx = new TextField();
 		dlCForenameTx.setWidth(215, Unit.PIXELS);
 		dlCForenameTx.setStyleName("detaillist-body");
 		dlForenameLo.addComponent(dlCForenameTx);
 		
 		//show Name
 		HorizontalLayout dlNameLo = new HorizontalLayout();
 		dlNameLo.setWidth(430, Unit.PIXELS);
 		dlNameLo.setHeight(22, Unit.PIXELS);
 		dlNameLo.addStyleName("optionsscreen-details-body");
 		detailListBodyLayout.addComponent(dlNameLo);
 		
 		dlDNameLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_DETAILS_NAME_LBL));
 		dlDNameLbl.setWidth(215, Unit.PIXELS);
 		dlNameLo.addComponent(dlDNameLbl);
 		
 		dlCNameTx = new TextField();
 		dlCNameTx.setWidth(215, Unit.PIXELS);
 		dlCNameTx.setStyleName("detaillist-body");
 		dlNameLo.addComponent(dlCNameTx);
 		
 		//show Timezone
 		HorizontalLayout dlTimezoneLo = new HorizontalLayout();
 		dlTimezoneLo.setWidth(430, Unit.PIXELS);
 		dlTimezoneLo.setHeight(22, Unit.PIXELS);
 		dlTimezoneLo.addStyleName("optionsscreen-details-body");
 		detailListBodyLayout.addComponent(dlTimezoneLo);
 		
 		dlDTimezoneLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_DETAILS_TIMEZONE_LBL));
 		dlDTimezoneLbl.setWidth(215, Unit.PIXELS);
 		dlTimezoneLo.addComponent(dlDTimezoneLbl);
 		
 		dlCTimezoneSel = new NativeSelect();
 		dlCTimezoneSel.setWidth(215, Unit.PIXELS);
 		dlCTimezoneSel.setStyleName("detaillist-body");
 		for(int i = 12; i > 0; i--){
 			dlCTimezoneSel.addItem("-" + i );
 		}
 		dlCTimezoneSel.addItem("0");
 		for(int i = 1; i <= 12; i++){
 			dlCTimezoneSel.addItem("+" + i );
 		}
 		dlCTimezoneSel.setNullSelectionAllowed(false);
 		if(user == null){
 			dlCTimezoneSel.select("0");
 		}else{
 			String tz;
			if(user.getTimezone() > 0){
 				tz = "+" + user.getTimezone().toString();
 			}else{
 				tz = user.getTimezone().toString();
 			}
 			dlCTimezoneSel.select(tz);
 		}
 		dlTimezoneLo.addComponent(dlCTimezoneSel);
 		
 		//show Password
 		HorizontalLayout dlPasswordLo = new HorizontalLayout();
 		dlPasswordLo.setWidth(430, Unit.PIXELS);
 		dlPasswordLo.setHeight(22, Unit.PIXELS);
 		dlPasswordLo.addStyleName("optionsscreen-details-body");
 		detailListBodyLayout.addComponent(dlPasswordLo);
 		
 		dlDPasswordLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_DETAILS_PASSWORD_LBL));
 		dlDPasswordLbl.setWidth(215, Unit.PIXELS);
 		dlPasswordLo.addComponent(dlDPasswordLbl);
 		
 		dlCPasswordTx = new TextField();
 		dlCPasswordTx.setWidth(215, Unit.PIXELS);
 		dlCPasswordTx.setStyleName("detaillist-body");
 		dlPasswordLo.addComponent(dlCPasswordTx);
 		
 		//shot Password Test
 		HorizontalLayout dlPasswordTestLo = new HorizontalLayout();
 		dlPasswordTestLo.setWidth(430, Unit.PIXELS);
 		dlPasswordTestLo.setHeight(22, Unit.PIXELS);
 		dlPasswordTestLo.addStyleName("optionsscreen-details-body");
 		detailListBodyLayout.addComponent(dlPasswordTestLo);
 		
 		dlDPasswordTestLbl = new Label(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_DETAILS_REPEAT_PASSWORD_LBL));
 		dlDPasswordTestLbl.setWidth(215, Unit.PIXELS);
 		dlPasswordTestLo.addComponent(dlDPasswordTestLbl);
 		
 		dlCPasswordTestTx = new TextField();
 		dlCPasswordTestTx.setWidth(215, Unit.PIXELS);
 		dlCPasswordTestTx.setStyleName("detaillist-body");
 		dlPasswordTestLo.addComponent(dlCPasswordTestTx);
 
 		
 		/************************************************************************************************************************
 		 * 		Footer
 		 ************************************************************************************************************************/
 		HorizontalLayout detailListFooterLayout = new HorizontalLayout();
 		detailListFooterLayout.setWidth(460, Unit.PIXELS);
 		detailListFooterLayout.setHeight(40, Unit.PIXELS);
 		detailListFooterLayout.setStyleName("detaillist-footer");
 		detailListFooterLayout.setMargin(false);
 		this.addComponent(detailListFooterLayout);
 		
 		Label dlFooterVSpacer = new Label();
 		dlFooterVSpacer.setWidth(245, Unit.PIXELS);
 		detailListFooterLayout.addComponent(dlFooterVSpacer);
 		detailListFooterLayout.setComponentAlignment(dlFooterVSpacer, Alignment.TOP_RIGHT);
 		
 		if(user == null){
 		dlSaveBttn = new NativeButton(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_DETAILS_BTTN_NEW_USER));
 		}else{
 			dlSaveBttn = new NativeButton(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_DETAILS_BTTN_SAVE_USER));
 		}
 		dlSaveBttn.addStyleName("detaillist-footer");
 		detailListFooterLayout.addComponent(dlSaveBttn);
 		detailListFooterLayout.setComponentAlignment(dlSaveBttn, Alignment.TOP_CENTER);		
 		
 		dlSaveBttn.addClickListener(new ClickListener(){
 			@Override
 			public void buttonClick(ClickEvent event) {
 				DlSaveBttnClicked(event);
 			}
 		});
 		
 		dlDeleteBttn = new NativeButton(app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_DETAILS_BTTN_DELETE_USER));
 		dlDeleteBttn.addStyleName("detaillist-footer");
 		detailListFooterLayout.addComponent(dlDeleteBttn);
 		detailListFooterLayout.setComponentAlignment(dlDeleteBttn, Alignment.TOP_CENTER);		
 		
 		dlDeleteBttn.addClickListener(new ClickListener(){
 			@Override
 			public void buttonClick(ClickEvent event) {
 				DlDeleteBttnClicked(event);
 			}
 		});
 		
 		LoadContent();
 	}
 	
 	public void DlSaveBttnClicked(ClickEvent event){
 		boolean savePassword = false;
 		
 		if(dlCPasswordTx.getValue().toString() != null && dlCPasswordTx.getValue().toString() != ""
 				|| dlCPasswordTestTx.getValue().toString() != null && dlCPasswordTestTx.getValue().toString() != ""){
 			savePassword = true;
 		}
 		
 		if( user == null){		// new user
 			newUser = new User(app.sql);
 			if(newUser.getID() == 0){
 				Notification.show("Error: Couldn't create User", Notification.Type.WARNING_MESSAGE);
 				return;
 			}
 		}else{
 			newUser = new User(user.getID());
 		}
 
 		if( dlCLoginTx.getValue().toString() == ""){
 			Notification.show("Error: You have to specify a login", Notification.Type.WARNING_MESSAGE);
 			return;
 		}
 		
 		newUser.setLogin(dlCLoginTx.getValue().toString());
 		newUser.setForename(dlCForenameTx.getValue().toString());
 		newUser.setName(dlCNameTx.getValue().toString());
 		try{
 			String tz = dlCTimezoneSel.getValue().toString();
 			if(tz.startsWith("+")){
 				tz = tz.substring(1);
 			}
 			newUser.setTimezone(Integer.parseInt(tz));
 		}catch(Exception e){
 			newUser.setTimezone(0);
 		}
 		
 		// Password 
 		String p1 = dlCPasswordTx.getValue().toString();
 		String p2 = dlCPasswordTestTx.getValue().toString(); 
 		
 		if(p1.equals("*****") && p2.equals("*****")){
 			newUser.setPassword(user.getPassword());
 		}else{
 			if(!p1.equals(p2)){
 				Notification.show("Error: Passwords don't match", Notification.Type.WARNING_MESSAGE);
 				return;
 			}
 			newUser.setPassword(SQLHelper.getMD5Hash(dlCPasswordTx.getValue().toString()));
 		}
 
 		if(user != null){
 			if(!user.getLogin().equals(newUser.getLogin())){
 				User registeredUser;
 				for(int i = 0; i < app.userContainer.size();i++){
 					registeredUser = app.userContainer.getIdByIndex(i);
 					if(registeredUser.getLogin().equals(newUser.getLogin())){
 						Notification.show("Error: User allready exists", Notification.Type.WARNING_MESSAGE);
 						return;
 					}
 				}
 			}
 		}
 		
 		
 		// Popup
 		YesNoPopupScreen ackPopup = new YesNoPopupScreen(app, 
 														 app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_DETAILS_POPUP_CAPTION), 
 														 app.langHelper.GetString(LanguageHelper.SET_TABSHEET_TAB_USER_DETAILS_POPUP_TEXT));
 		ackPopup.addListener(new PopupClosedListener(){
 			@Override
 			public void popupClosedEvent(YesNoPopupScreen e) {
 				if(e.isYesClicked()){	
 					app.sql.SaveUser(newUser);
 					app.userContainer = UserContainer.loadUsers(app.sql);
 					app.Update();	
 					app.optionsScreen.SelectUsersTableRow(newUser.getID());
 					Notification.show("Successfully saved", Notification.Type.TRAY_NOTIFICATION);
 				}else if(e.isNoClicked()){
 				}else{
 					Notification.show("Error: Couldn't save user", Notification.Type.WARNING_MESSAGE);
 				}
 				
 			}
 		});
 	}
 	
 	public void DlDeleteBttnClicked(ClickEvent event){
 		app.sql.DeleteUser(user.getID());
 		app.userContainer = UserContainer.loadUsers(app.sql);
 		app.Update();
 	}
 }
 
 
 
 
 
 
 
 
 
