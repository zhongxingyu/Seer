 
 package bohnanza.standard.client;
 
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javafx.application.Application;
 import javafx.application.Platform;
 import javafx.event.EventHandler;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.Initializable;
 import javafx.fxml.JavaFXBuilderFactory;
 import javafx.scene.Scene;
import javafx.scene.image.Image;
 import javafx.scene.layout.AnchorPane;
 import javafx.stage.Stage;
 import javafx.stage.WindowEvent;
 
 import org.json.JSONObject;
 
 import bohnanza.standard.server.Protocol;
 
 
 
 public class ClientGUI extends Application {
 
 	Stage stage;
 	Client client;
 	GameController gameController;
 	LoginController loginController;
 	String username = "";
 
 	public static final int AWAITING_USERNAMECHECK = 0;
 	public static final int AWAITING_GAMEUPDATE = 1;
 	public static final int AWAITING_OFFER = 2;
 	public int state = AWAITING_USERNAMECHECK;
 
 
 	public static void main(String[] args) {
 		Application.launch(ClientGUI.class, (java.lang.String[])null);
 	}
 
 	public Client getClient(){
 		return client;
 	}
 
 	public void setUsername(String username){
 		this.username = username;
 	}
 
 	public String getUsername(){
 		return username;
 	}
 
 	@Override
 	public void start(Stage primaryStage) {
 
 		try{
 			primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>(){
 				@Override
 				public void handle(WindowEvent arg0) {
 					java.lang.System.exit(0);
 				}
 			}));
 
 			stage = primaryStage;
 			gotoLogin();
 			primaryStage.show();
 		} catch (Exception ex) {
 			Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	private void gotoLogin() {
 		try {
 			loginController = (LoginController) replaceSceneContent("Login.fxml");
 			loginController.setApp(this);
 		} catch (Exception ex) {
 			Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	public void goToGame(){
 		try {
 			gameController = (GameController) replaceSceneContent("Game.fxml");
 			gameController.setApp(this);
 		} catch (Exception ex) {
 			Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	public void testLogin(String host, String username){
 		if(client == null){
 			setUsername(username);
 			client = new Client(this, host);
 		}
 		client.sendToServer("NEWPLAYER " + username);
 	}
 
 	/**
 	 * This method is needed to update UI on UI-thread.
 	 * @param update
 	 */
 	public void update(final String update)
 	{
 		Platform.runLater(new Runnable() {
 			@Override public void run() {
 				try{
 					JSONObject response = new JSONObject(update);
 					String action = response.getString("type");
 					if(state == ClientGUI.AWAITING_USERNAMECHECK && action.equals("usernamecheck")){
 						loginController.checkLogin(Protocol.usernameFromJSON(response));
 					} else if(action.equals(Protocol.CHAT))
 						gameController.addChat(Protocol.chatFromJSON(response));
 					else if(action.equals("gameupdate")) {
 						gameController.update(update);
 					}
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}     
 			}
 		});
 	}
 
 	private Initializable replaceSceneContent(String fxml) throws Exception {
 		FXMLLoader loader = new FXMLLoader();
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("res/ui/"+fxml);
 		loader.setBuilderFactory(new JavaFXBuilderFactory());
		loader.setLocation(getClass().getClassLoader().getResource("res/ui/"+fxml));
 		AnchorPane page = null;
 		try {
 			page = (AnchorPane) loader.load(in);
 		} catch(Exception e){
 			e.printStackTrace();
 		} finally {
 			in.close();
 		}
 		// store the stage height in case the user has resized the window
 		double stageWidth = stage.getWidth();
 		if (!Double.isNaN(stageWidth)) {
 			stageWidth -= (stage.getWidth() - stage.getScene().getWidth());
 		}
 		double stageHeight = stage.getHeight();
 		if (!Double.isNaN(stageHeight)) {
 			stageHeight -= (stage.getHeight() - stage.getScene().getHeight());
 		}
 		//        Scene scene = stage.getScene();
 		//        if (scene == null) {
 		Scene scene = new Scene(page);
 		if (!Double.isNaN(stageWidth)) {
 			page.setPrefWidth(stageWidth);
 		}
 		if (!Double.isNaN(stageHeight)) {
 			page.setPrefHeight(stageHeight);
 		}
 		stage.setScene(scene);
 		//        } else {
 		//            stage.getScene().setRoot(page);
 		//        }
 		stage.sizeToScene();
 		return (Initializable) loader.getController();
 	}
 }
