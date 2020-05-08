 package eu.hansolo.fxgtools.javafx;
 
 import java.util.Map;
 import javafx.application.Application;
 import javafx.scene.Group;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.stage.Stage;
 
 /**
  * User: han.solo at muenster.de
  * Date: 02.09.11
  * Time: 09:22
  */
 public class FxgTest extends Application
 {
     @Override
     public void start(Stage stage) {
        Test component = new Test(400, 400);
        Scene scene = new Scene(component, 400, 400);
         stage.setTitle("FXG -> JavaFX (live)");
         stage.setScene(scene);
         stage.show();
     }
 
     public class Test extends Parent {
         public Test(int width, int height) {
             String fxgFile = "/Volumes/Macintosh HD/Users/hansolo/Desktop/InSync/Java Apps/FXG Converter/fxg files/gradients2.fxg";
             FxgFxParser parser = new FxgFxParser();
             long start = System.currentTimeMillis();
             Map<String, Group> groups = parser.parse(fxgFile, width, height, true);
             System.out.println("Parsing and converting: " + (System.currentTimeMillis() - start) + " ms");
             getChildren().addAll(groups.values());
         }
     }
 
     public static void main(String[] args) {
         Application.launch(args);
     }
 }
