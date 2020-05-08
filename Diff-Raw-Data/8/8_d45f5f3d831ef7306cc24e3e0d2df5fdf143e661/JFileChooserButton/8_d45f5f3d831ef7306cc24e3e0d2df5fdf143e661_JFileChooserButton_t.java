 package edu.unh.schwartz.parawrap.config;
 
 import org.ciscavate.cjwizard.CustomWizardComponent;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 
 /**
  * Clab.
  */
 final class JFileChooserButton extends JButton implements CustomWizardComponent
 {
     /**
      * The dialog for the file choosers.
      */
     private JDialog dialog;
     
     /**
      * The file chooser that appears when the button clicked.
      */
     private JFileChooser fc;
     
     /**
      * The path of the file selected by the user.
      */
     private String path;
 
     /**
      * Constructs a new button.
      * @param directoriesOnly - allow the user to select directories only iff
      * true. otherwise, user can only select files
      */
     public JFileChooserButton(final boolean directoriesOnly)
     {
         super("...");
         this.dialog = new JDialog();
         this.fc = new JFileChooser();
 
         if (directoriesOnly)
         {
             fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         }
         else
         {
             fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         }
     
         this.addActionListener(new ActionListener()
         {
             /**
              * {@inheritDoc}
              */
             public void actionPerformed(final ActionEvent e)
             {
                 final int returnVal = fc.showOpenDialog(dialog);
                 switch (returnVal)
                 {
                     case JFileChooser.APPROVE_OPTION:
                         final File f = fc.getSelectedFile();
                         setValue(f.getPath());
                         setText(f.getName());
                        JFileChooserButton.this.dialog.dispose();
                         break;
                     default:
                         break;
                 }
             }
         });
     }
 
     /**
      * {@inheritDoc}
      */               
     public Object getValue()
     {
         return this.path;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setValue(final Object o)
     {
         this.path = (String) o;
     }
 }
