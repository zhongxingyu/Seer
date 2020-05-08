 package arcade.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import arcade.model.Model;
 
 
 @SuppressWarnings("serial")
 public class PublishView extends Form {
     private JTextField myNameTextField;
     private JTextField myGenreTextField;
 
     /**
      * Constructs the publish view dialog box with a Model and ResourceBundle.
      * 
      * @param model
      * @param resources
      */
     public PublishView (Model model, ResourceBundle resources) {
         super(model, resources);
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         pack();
         setLocationRelativeTo(null);
     }
 
     @Override
     protected List<JComponent> makeComponents () {
         List<JComponent> components = new ArrayList<JComponent>();
         components.add(createNameField());
         components.add(createGenreField());
         components.add(createButton());
 
         return components;
     }
 
     /**
      * Creates the field to enter the name of the game.
      * @return
      */
     private JComponent createNameField () {
         myNameTextField = new JTextField();
         return createTextPanel(TextKeywords.GAME_NAME, myNameTextField);
     }
 
     /**
      * Creates the field to enter the genre for the game.
      * @return
      */
     private JComponent createGenreField () {
         myGenreTextField = new JTextField();
         return createTextPanel(TextKeywords.GENRE, myGenreTextField);
     }
 
     /**
      * Creates the button to publish and tell the Model
      * @return
      */
     private JComponent createButton () {
         JPanel panel = new JPanel();
         JButton publish = new JButton(getResources().getString(TextKeywords.PUBLISH));
         publish.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed (ActionEvent arg0) {
                //getModel().publish(myNameTextField.getText(),
                //                   myGenreTextField.getText());
                
                 dispose();
             }
         });
         panel.add(publish);
         return panel;
     }
 }
