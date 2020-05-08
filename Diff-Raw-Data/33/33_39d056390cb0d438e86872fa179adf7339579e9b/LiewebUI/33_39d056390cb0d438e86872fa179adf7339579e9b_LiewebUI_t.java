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
 
 package lielas;
 
 import lielas.ui.LoginScreen;
 import lielas.ui.HeaderScreen;
 import lielas.ui.DeviceManagerScreen;
 import lielas.ui.OptionsScreen;
 import lielas.ui.DownloadScreen;
 import lielas.ui.AnalyseScreen;
 import lielas.ui.HelpScreen;
 import com.vaadin.ui.UI;
import com.vaadin.annotations.PreserveOnRefresh;
 import com.vaadin.annotations.Theme;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.terminal.*;
 import com.vaadin.ui.*;
 import com.vaadin.ui.Button.ClickEvent;
 
 import lielas.core.CoapHelper;
 import lielas.core.Config;
 import lielas.core.Device;
 import lielas.core.DeviceContainer;
 import lielas.core.LBus;
 import lielas.core.LBusSender;
 import lielas.core.NewDeviceContainer;
 import lielas.core.SQLHelper;
 import lielas.core.User;
 import lielas.core.UserContainer;
 import lielas.core.XMLHelper;
 import lielas.core.LanguageHelper;
 
 @SuppressWarnings({ "serial", "unused" })
 
@Theme("reder")	
@PreserveOnRefresh
 public class LiewebUI extends UI {
 	
 	/**
 	 * 
 	 */
 	
 	// Header
 	public HeaderScreen headerScreen = null;
 	private VerticalSplitPanel verticalSplit = new VerticalSplitPanel();
 	
 	public LoginScreen loginScreen = null;
 	public DeviceManagerScreen deviceManagerScreen = null;
 	public DownloadScreen downloadScreen = null;
 	public AnalyseScreen analyseScreen = null;
 	public OptionsScreen optionsScreen = null;
 	public HelpScreen helpScreen = null;
 	
 	public DeviceContainer deviceContainer = null;
 	public NewDeviceContainer newDeviceContainer = null;
 
 	public LanguageHelper langHelper;
 	
 	public SQLHelper sql;
 	
 	public UserContainer userContainer = null;
 	
 	public CoapHelper coap;
 	public User user;
 	
 	public Config config;


 	@Override
 	protected void init(VaadinRequest request) {
 		
 		sql = new SQLHelper();
 		sql.Connect();
 		
 		
 		deviceContainer = DeviceContainer.loadDevices(sql);
 		
 		userContainer = UserContainer.loadUsers(sql);
 		
 		newDeviceContainer = NewDeviceContainer.createWithTestData();
 
 		langHelper = new LanguageHelper("lang_en.properties");
 		
 		config = new Config();
 		config.LoadSettings();
 		
 		BuildMainLayout();
 		
 		loginScreen = new LoginScreen(this);
 		setMainComponent(loginScreen);
 		user = userContainer.getIdByIndex(0);
 		//setMainComponent(getDeviceManagerScreen());
 		//headerScreen.setPermisson(1);
 		
 	}
 	
 	public void Authenticate(String username, String pw){
 		boolean loggedIn = false;
 		
 		for(int i = 0; i < userContainer.size(); i++){
 			user = userContainer.getIdByIndex(i);
 			if(user != null){
 				if(user.getLogin().equals(username)){
 					if(user.getPassword().equals(SQLHelper.getMD5Hash(pw))){
 						setMainComponent(getDeviceManagerScreen());
 						headerScreen.setPermisson(1);
 						Notification.show("Welcome " + user.getForename() + " " + user.getName(),Notification.Type.WARNING_MESSAGE);
 						loggedIn = true;
 						LBusSender lbus = new LBusSender(this.config.getLbusServerAddress(), this.config.getLbusServerPort(), "lbus");
 						lbus.setCmd(lbus.LBUS_CMD_LOGIN);
 						lbus.setUser(this.user.getID());
 						lbus.setAddress("liegw");
 						lbus.setPayload("");
 						lbus.send();
 					}
 				}
 			}
 		}
 		
 		if(!loggedIn){
 			Notification.show("Username or password not correct" ,Notification.Type.TRAY_NOTIFICATION);
 		}
 		
 	}
 	
 	public void Logout(){
		getUI().getPage().setLocation("lieweb");
		getUI().getSession().close();
 	}
 	
 	public void setMainComponent(Component c){
 		verticalSplit.setSecondComponent(c);
 	}
 
 	private void BuildMainLayout(){
 		this.getPage().setTitle(langHelper.GetString("PROGRAM_TITLE"));
 		
 		setContent(verticalSplit);
 		getContent().setSizeFull();
 		
 		verticalSplit.setLocked(true);
 		verticalSplit.setSplitPosition(73, Unit.PIXELS);
 		verticalSplit.setFirstComponent(getHeaderScreen());
 		verticalSplit.setSizeFull();
 	}
 	
 	public void Update(){
 		headerScreen.Update();
 		if(deviceManagerScreen != null)
 			deviceManagerScreen.Update();
 		if(downloadScreen != null)
 			downloadScreen.Update();
 		if(optionsScreen != null){
 			optionsScreen.Update();
 		}if(loginScreen != null)
 			loginScreen.Update();
 	}
 	
 	public HeaderScreen getHeaderScreen(){
 		if(headerScreen == null){
 			headerScreen = new HeaderScreen(this);
 		}
 		return headerScreen;
 	}
 	
 	public LoginScreen getLoginScreen(){
 		if(loginScreen == null){
 			loginScreen = new LoginScreen(this);
 		}
 		return loginScreen;
 	}
 	
 	public DeviceManagerScreen getDeviceManagerScreen(){
 		if(deviceManagerScreen == null){
 			deviceManagerScreen = new DeviceManagerScreen(this);
 		}
 		return deviceManagerScreen;
 	}
 	
 	public DownloadScreen getDownloadScreen(){
 		if(downloadScreen == null){
 			downloadScreen = new DownloadScreen(this);
 		}
 		return downloadScreen;
 	}
 	
 	public AnalyseScreen getAnalyseScreen(){
 		if( analyseScreen == null){
 			analyseScreen = new AnalyseScreen(this);
 		}
 		return analyseScreen;
 	}
 	
 	public OptionsScreen getOptionsScreen(){
 		if(optionsScreen == null){
 			optionsScreen = new OptionsScreen(this);
 		}
 		return optionsScreen;
 	}
 	
 	public HelpScreen getHelpScreen(){
 		if( helpScreen == null){
 			helpScreen = new HelpScreen(this);
 		}
 		return helpScreen;
 	}
 
 }
