 package com.sk83rsplace.arkane.menus;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 
 import com.sk83rsplace.arkane.GUI.Menu;
 import com.sk83rsplace.arkane.GUI.components.ButtonComponent;
 import com.sk83rsplace.arkane.GUI.components.CheckBoxComponent;
 import com.sk83rsplace.arkane.GUI.components.LabelComponent;
 import com.sk83rsplace.arkane.GUI.components.TextComponent;
 import com.sk83rsplace.arkane.GUI.components.TextInputComponent;
 import com.sk83rsplace.arkane.HTTP.HTTP;
 import com.sk83rsplace.arkane.client.Board;
 
 public class LoginMenu extends Menu {
 	private TextInputComponent usernameField;
 	private TextInputComponent passwordField;
 	private TextComponent errorMessage;
 	private CheckBoxComponent rememberMe;
 	
 	public LoginMenu() {		
		usernameField = new TextInputComponent("Username", (Board.properties.getProperty("username") == null ? "" : Board.properties.getProperty("username")), -1, 200);
		passwordField = new TextInputComponent("Password", (Board.properties.getProperty("password") == null ? "" : Board.properties.getProperty("password")), -1, 250, true);
 		errorMessage = new TextComponent("", Color.yellow, -1, 360);
		rememberMe = new CheckBoxComponent(((Board.properties.getProperty("remember_me") == null ? false : Board.properties.getProperty("remember_me").equals("1")) ? true : false), false, 370, 392) {
 			public void onInitialization(GameContainer container) {
 				addComponent(new LabelComponent("Remember Me", this));
 			}
 		};
 		
 		addComponent(new TextComponent("Please Log-In!", Color.white, -1, 150));
 		addComponent(usernameField);
 		addComponent(passwordField);
 		addComponent(new ButtonComponent("Login", -1, 300) {
 			public void onClick() {
 				checkLogin();
 			}
 		});
 		addComponent(errorMessage);
 		addComponent(rememberMe);
 	}
 	
 	private void checkLogin() {		
 		Map<String, String> params = new HashMap<String, String>();
 		
 		params.put("username", usernameField.getValue());
 		params.put("password", HTTP.MD5(passwordField.getValue()));
 		
 		HTTP httpConnection = new HTTP();
 		String JSONResult = httpConnection.post("http://vps.kieraan.co.uk/~Josh/login.php", params);
 		
 		try {
 			JSONObject JSONParser = new JSONObject(JSONResult);
 
 			if(JSONParser.getInt("status") == 0) {
 				errorMessage.setValue("Log-In Details Incorrect!");
 			} else {
 				errorMessage.setValue("");
 				Board.username = usernameField.getValue();
 				Board.userID = JSONParser.getInt("player_id");
 				
 				if(rememberMe.getValue()) {
 					Board.properties.setProperty("username", usernameField.getValue());
 					Board.properties.setProperty("password", passwordField.getValue());
 					Board.properties.setProperty("remember_me", "1");
 				} else {
 					Board.properties.setProperty("username", "");
 					Board.properties.setProperty("password", "");
 					Board.properties.setProperty("remember_me", "0");
 				}
 				
 				Board.saveProperties();
 				
 				Board.menuStack.pop();
 				Board.menuStack.add(new SplashScreen());
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			errorMessage.setValue("Unexpected Error!");
 		}
 	}
 }
