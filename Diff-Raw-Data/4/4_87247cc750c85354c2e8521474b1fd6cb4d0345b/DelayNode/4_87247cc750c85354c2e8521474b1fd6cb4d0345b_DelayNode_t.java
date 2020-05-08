 package menu;
 
 import repository.DelayCom;
 import java.io.IOException;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Pos;
 import javafx.scene.control.Button;
 import javafx.scene.control.TextField;
 import javafx.scene.control.Tooltip;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.layout.GridPane;
 import javafx.scene.paint.Color;
 import javafx.scene.text.Font;
 import javafx.scene.text.FontWeight;
 import javafx.scene.text.Text;
 
 /**
  * This class is a GridPane that contains all the text and
  * buttons for setting delay.
  * @author Stian
  */
 public class DelayNode extends GridPane {
 
     private Text header, unit, error, confirm;
     private TextField delayField;
     private Button setButton;
     DelayCom action;
 
     public DelayNode() {
         super();
         action = new DelayCom();
 
         header = new Text("Slideshow Intervall");
         header.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
 
         unit = new Text("Sekunder");
         unit.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
 
         error = new Text("");
         error.setVisible(false);
         error.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
         error.setFill(Color.RED);
 
         confirm = new Text("Nytt Intervall Satt!");
         confirm.setVisible(false);
         confirm.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
 
        delayField = new TextField();
         if (getDelay() == 0) {
             error.setVisible(true);
         } else {
            delayField.setText(getDelay() + "");
         }
         delayField.setPrefSize(80, 30);
         delayField.setAlignment(Pos.CENTER_RIGHT);
         delayField.setTooltip(new Tooltip("Delay må være 1 sekund eller mer"));
         delayField.setOnKeyReleased(new EventHandler<KeyEvent>() {
             @Override
             public void handle(KeyEvent event) {
                 confirm.setVisible(false);
                 String input = delayField.getText();
                 if (testInput(input)) {
                     error.setVisible(false);
                 } else {
                     error.setVisible(true);
                 }
             }
         });
 
         setButton = new Button("Sett Intervall");
         setButton.setPrefSize(180, 30);
         setButton.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent event) {
                 error.setVisible(false);
                 confirm.setVisible(false);
                 String input = delayField.getText();
                 if (testInput(input)) {
                     int ok = setDelay(Integer.parseInt(input));
                     if (ok != 0) {
                         confirm.setVisible(true);
                         delayField.setText(ok + "");
                     } else {
                         error.setVisible(true);
                     }
                 } else {
                     error.setVisible(true);
                 }
             }
         });
 
         this.setTranslateX(10);
         this.setVgap(5);
         this.setHgap(5);
         this.add(header, 0, 0, 2, 1);
         this.add(delayField, 0, 1);
         this.add(unit, 1, 1);
         this.add(error, 0, 2, 2, 1);
         this.add(confirm, 0, 2, 2, 1);
         this.add(setButton, 0, 3, 2, 1);
     }
 
     //Tests if the input is valid
     private boolean testInput(String input) {
         int i;
         try {
             i = Integer.parseInt(input);
         } catch (NumberFormatException e) {
             //e.printStackTrace();
             error.setText("Ugyldig input");
             return false;
         }
         if (i < 1) {
             error.setText("Minimum delay er 1");
             return false;
         }
         return true;
     }
 
     private int getDelay() {
         int i;
         try {
             i = action.getDelay();
         } catch (IOException ex) {
             //ex.printStackTrace();
             error.setText("Server utilgjengelig");
             return 0;
         }
         return i;
     }
 
     private int setDelay(int delay) {
         int newDelay;
         try {
             newDelay = action.setDelay(delay);
         } catch (IOException ex) {
             //ex.printStackTrace();    
             error.setText("Server utilgjengelig");
             return 0;
         }
         return newDelay;
     }
 }
