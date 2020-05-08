 package nu.tengstrand.stateguard.guiexample;
 
 import nu.tengstrand.stateguard.Validatable;
 import nu.tengstrand.stateguard.guiexample.person.PersonStateGuard;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ResourceBundle;
 
 public class PersonFrame extends JFrame {
     static ResourceBundle resourceBundle = ResourceBundle.getBundle("validationMessages");
 
     public PersonFrame(final PersonStateGuard person, final SaveCommand saveCommand) {
         setTitle("State Guard example - by Joakim Tengstrand");
         setPreferredSize(new Dimension(500, 190));
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         Container contentPane = getContentPane();
         SpringLayout layout = new SpringLayout();
         contentPane.setLayout(layout);
 
         // Name
         JLabel nameLabel = new JLabel("Name: ");
         JTextField nameTextField = new JTextField("", 15);
         JLabel nameError = new JLabel();
         nameError.setForeground(Color.RED);
         contentPane.add(nameLabel);
         contentPane.add(nameTextField);
         contentPane.add(nameError);
 
         // Age
         JLabel ageLabel = new JLabel("Age: ");
         JTextField ageTextField = new JTextField("", 5);
         JLabel ageError = new JLabel();
         ageError.setForeground(Color.RED);
         contentPane.add(ageLabel);
         contentPane.add(ageTextField);
         contentPane.add(ageError);
 
         // Country
         JLabel countryLabel = new JLabel("Country: ");
         JTextField countryTextField = new JTextField("", 10);
         JLabel countryError = new JLabel();
         countryError.setForeground(Color.RED);
         contentPane.add(countryLabel);
         contentPane.add(countryTextField);
         contentPane.add(countryError);
 
         // Save button
         final JButton saveButton = new JButton("Save");
         saveButton.setEnabled(false);
         saveButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 saveCommand.save();
             }
         });
         contentPane.add(saveButton);
 
         // Validation explanation
         JLabel validationErrorExplanationLabel = new JLabel("* = Mandatory field");
         contentPane.add(validationErrorExplanationLabel);
 
         connectTextFieldToModel(person.name(), nameTextField, nameError, person, saveButton);
         connectTextFieldToModel(person.age(), ageTextField, ageError, person, saveButton);
         connectTextFieldToModel(person.country(), countryTextField, countryError, person, saveButton);
 
         // Spring layout constraints
         layout.putConstraint(SpringLayout.WEST, nameLabel, 5, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, nameLabel, 5, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.WEST, nameTextField, 80, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, nameTextField, 5, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.WEST, nameError, 20, SpringLayout.EAST, nameTextField);
         layout.putConstraint(SpringLayout.NORTH, nameError, 5, SpringLayout.NORTH, contentPane);
 
         layout.putConstraint(SpringLayout.WEST, ageLabel, 5, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, ageLabel, 25, SpringLayout.NORTH, nameTextField);
        layout.putConstraint(SpringLayout.WEST, ageTextField, 80, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, ageTextField, 25, SpringLayout.NORTH, nameTextField);
         layout.putConstraint(SpringLayout.WEST, ageError, 20, SpringLayout.EAST, ageTextField);
         layout.putConstraint(SpringLayout.NORTH, ageError, 25, SpringLayout.NORTH, nameTextField);
 
         layout.putConstraint(SpringLayout.WEST, countryLabel, 5, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, countryLabel, 25, SpringLayout.NORTH, ageTextField);
        layout.putConstraint(SpringLayout.WEST, countryTextField, 80, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, countryTextField, 25, SpringLayout.NORTH, ageTextField);
         layout.putConstraint(SpringLayout.WEST, countryError, 20, SpringLayout.EAST, countryTextField);
         layout.putConstraint(SpringLayout.NORTH, countryError, 25, SpringLayout.NORTH, ageTextField);
 
         layout.putConstraint(SpringLayout.WEST, validationErrorExplanationLabel, 80, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, validationErrorExplanationLabel, 30, SpringLayout.NORTH, countryLabel);
 
         layout.putConstraint(SpringLayout.WEST, saveButton, 80, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, saveButton, 30, SpringLayout.NORTH, validationErrorExplanationLabel);
 
         pack();
         setVisible(true);
     }
 
     private void connectTextFieldToModel(final ValidatableStringValue validatableStringValue, JTextField textField, final JLabel error, final Validatable person, final JButton saveButton) {
         error.setText(validatableStringValue.validationMessages().firstMessage(resourceBundle));
 
         textField.getDocument().addDocumentListener(new UpdateTextListener() {
             public void setText(String text) {
                 validatableStringValue.setValue(text);
                 error.setText(validatableStringValue.validationMessages().firstMessage(resourceBundle));
                 saveButton.setEnabled(person.isValid());
             }
         });
     }
 }
