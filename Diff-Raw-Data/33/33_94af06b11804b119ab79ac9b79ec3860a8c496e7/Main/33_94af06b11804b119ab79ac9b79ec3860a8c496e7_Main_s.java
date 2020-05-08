 package app;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javafx.application.Application;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Scene;
 import javafx.scene.layout.Pane;
 import javafx.stage.Stage;
 import model.ModelImpl;
 import view.View;
 import db.impl.Db;
 
 public class Main extends Application {
 
 	private ModelImpl model;
 	private View view;
 	private Db db;
 	private TimelineFactory timeline;
 
 	public Main() {
 	}
 
 	@Override
 	public void start(Stage primaryStage) {
 		model = new ModelImpl();
 		// TODO - need different way of sourcing the data
 		db = new Db(Main.class.getResource("/db/data2.csv"));
 		timeline = new TimelineFactory(db);
 		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fx/new.fxml"));
 		Pane page = null;
 		try {
 			page = (Pane) fxmlLoader.load();// (StackPane)
 		} catch (IOException ex) {
 			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		Scene scene = new Scene(page);
         scene.getStylesheets().add("/styles/styles.css");
 
 		primaryStage.setScene(scene);
 		primaryStage.setTitle("Calendar Timeline");
 		primaryStage.show();
 
 		View controller = (View) fxmlLoader.getController();
 		controller.setModel(model);
 		controller.setTimeline(timeline);
 		controller.go();
 	}
 
 	public static void main(String[] args) {
 		launch(args);
 	}
 }
