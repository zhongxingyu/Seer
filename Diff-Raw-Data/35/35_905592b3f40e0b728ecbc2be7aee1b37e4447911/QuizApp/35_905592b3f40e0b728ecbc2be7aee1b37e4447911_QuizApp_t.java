 package client.main;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import network.Client;
 
 import Protocol.responses.LoginResponse;
 import Protocol.responses.LoginResponse.UserType;
 import Protocol.responses.TeamLoginResponse;
 import javaFXpanels.Login.LoginPanel;
 import javaFXpanels.MessageProvider.MessageProvider;
 import javaFXpanels.Questions.QuestionDisplayController;
 import javaFXpanels.TeamLogin.TeamLoginController;
 
 import javafFXpanels.ConnectToQuiz.ConnectToQuizController;
 import javafx.application.Application;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.event.EventHandler;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.Initializable;
 import javafx.scene.Scene;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.Pane;
 import javafx.stage.Stage;
 import javafx.stage.WindowEvent;
 
 public class QuizApp extends Application {
 	private static URL loginLocation;
 	private static URL teamLoginLocation;
 	private static URL connectToQuizLocation;
 	private static URL quizLocation;
 	private AnchorPane root;
 	private LoginResponse login;
 	private MessageProvider messageMaker;
 
 
 	static {
 		loginLocation = LoginPanel.class.getResource("Login.fxml");
 		teamLoginLocation = TeamLoginController.class.getResource("teamLogin.fxml");
 		quizLocation = QuestionDisplayController.class.getResource("QuestionDisplay.fxml");
 		connectToQuizLocation = ConnectToQuizController.class.getResource("connectToQuiz.fxml");
 	}
 
 	@Override
 	public void start(Stage primaryStage) {
 		root = new AnchorPane();
 		System.out.println(root);
 
 		primaryStage.setMinHeight(600.0);
 		primaryStage.setMinWidth(700.0);
 		Scene s = new Scene(root, 600, 700);
 		primaryStage.setScene(s);
 		
 		primaryStage.show();
 		
 		System.out.println(root);
 		//TODO: fatsoenlijk afsluiten
 		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
 
 			@Override
 			public void handle(WindowEvent event) {
 				//TODO: om het koosjer te doen zouden hier de cleint thread enzo gestop moeten worden
 
 				System.exit(0);
 
 			}
 		});
 		
 		messageMaker = new MessageProvider(root);
 		
 		startDetectServer();
 		startLoginPane();
 
 
 	}
 
 	private void startDetectServer() {
 		try {
 			Client.getInstance();
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * loads every kind of pane from fxml
 	 * @param location
 	 * @return
 	 * @throws IOException
 	 */
 	private Initializable setFxml(URL location) throws IOException {
 		FXMLLoader loader = new FXMLLoader();
 		loader.setLocation(location);
 		Pane content = (Pane) loader.load();
 
 		AnchorPane.setBottomAnchor(content, 0.0);
 		AnchorPane.setTopAnchor(content, 0.0);
 		AnchorPane.setRightAnchor(content, 0.0);
 		AnchorPane.setLeftAnchor(content, 0.0);
 
 		root.getChildren().setAll(content);
 
 		return loader.getController();	
 	}
 
 	private void startLoginPane() {
 		try {
 			final LoginPanel controller = (LoginPanel) setFxml(loginLocation);
 
 			controller.loginResponseProperty().addListener(new ChangeListener<LoginResponse>() {
 				@Override
 				public void changed(
 						ObservableValue<? extends LoginResponse> observable,
 						LoginResponse oldValue, LoginResponse newValue) {
 					if(newValue!= null) {
 						login = newValue;
 						if(newValue.getUserType().equals(UserType.JURY)) {
 							startJurry(newValue);
 						} else if(newValue.getUserType().equals(UserType.PLAYER)) {
 							startTeamLoginPane(newValue);
 						}
 					}
 					controller.loginResponseProperty().removeListener(this);
 				}
 			});
 			// en er is ingelogd
 
 		} catch (IOException e) {
 			//fatale fout niet te recepureren
 			e.printStackTrace();
 			System.exit(99999);
 		}
 	}
 
 	private void startJurry(LoginResponse newValue) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void startTeamLoginPane(LoginResponse r) {
 		try {
 			final TeamLoginController controller = (TeamLoginController) setFxml(teamLoginLocation);
 
 			controller.setplayerLoginResponse(r);
 			controller.teamResponseProperty().addListener(new ChangeListener<TeamLoginResponse>() {
 				@Override
 				public void changed(
 						ObservableValue<? extends TeamLoginResponse> observable,
 						TeamLoginResponse oldValue, TeamLoginResponse newValue) {
 					if(newValue != null) {
 						connectToQuiz(controller.getTeamResponse());
 					}
 				}
 			});
 
 		} catch (IOException e) {
 			messageMaker.showError("Fatale fout");
 		}
 	}
 
 	public void connectToQuiz(TeamLoginResponse r) {
 		try {
 			ConnectToQuizController c = (ConnectToQuizController) setFxml(connectToQuizLocation);
 			System.out.println(login.getUserType());
 			c.setMode(login.getUserType());
 			c.setTeamLogin(r);
 			c.readyProperty().addListener(new ChangeListener<Boolean>() {
 				@Override
 				public void changed(ObservableValue<? extends Boolean> observable,
 						Boolean oldValue, Boolean newValue) {
 					if(newValue == true) {
 						System.out.println(login.getUserType());
 						startQuiz();
 					}
 				}
 			});
 
 
 		} catch (IOException e) {
 			messageMaker.showError("Fatale fout");
 		}
 	}
 
 	private void startQuiz() {
 		try {
 			setFxml(quizLocation);
 
 
 		} catch (IOException e) {
 			messageMaker.showError("Fatale fout");
 		}
 
 	}
 
 	public static void main(String[] args) {
 		launch(args);
 	}
 }
