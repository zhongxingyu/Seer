 import java.awt.event.*;
 import javax.swing.*;
 import java.io.*;
 
 /** Opens a file chooser when the user clicks the 'Select file...' button in 
  * StartupWindow
  * @author Qasim Ali
  */
 public class StartupFileButtonHandler implements ActionListener {
   private JTextField fileName;   // The textfield that will display the file
                                 // name in StartupWindow
   private StartupWindow window;  // Window that contains the file chooser button
                                   
   /** Creates this handler
    * @param field    the textfield that will show the selecte file's name
    */
  public StartupFileButtonHandler(JTextField field, StartupWindow window) {
     this.fileName = field;
     this.window = window;
   }
   
   /** Open a file chooser and have the user choose their file, then display the
    * file name in fileName
    * @param e    the triggered event
    */
   public void actionPerformed(ActionEvent e) {
     JFileChooser chooser = new JFileChooser();
     int result = chooser.showOpenDialog(this.window);  // Disable the 
                                                        // StartupWindow
     // Only alter the text field if the user clicked 'OK' on the file chooser
     if(result == JFileChooser.APPROVE_OPTION)
       this.fileName.setText(chooser.getSelectedFile().getName());
   }
  }
