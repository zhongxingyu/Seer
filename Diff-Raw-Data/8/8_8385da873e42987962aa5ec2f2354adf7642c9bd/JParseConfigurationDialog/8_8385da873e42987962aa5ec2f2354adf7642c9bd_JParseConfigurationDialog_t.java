 package confdb.gui;
 
 import javax.swing.*;
 import javax.swing.filechooser.*;
 import java.awt.event.*;
 
 import confdb.db.ConfDB;
 import confdb.db.DatabaseException;
 
 import java.io.File;
 
 
 /**
  * JParseConfigurationDialog
  * ------------------------
  * @author Philipp Schieferdecker
  *
  */
 public class JParseConfigurationDialog extends JDialog
 {
     //
     // member data
     //
     
     /** reference to the main frame */
     private JFrame frame = null;
     
     /** reference to the database */
     private ConfDB database = null;
     
     /** the name of the *.py file to be parsed */
     private String fileName = null;
     
     /** the release tag to be associated with the new configuration */
     private String releaseTag = null;
     
     /** was a valid choice made? */
     private boolean validChoice = false;
 
     /** GUI components */
     JTextField jTextFieldFileName  = new javax.swing.JTextField();
     JComboBox  jComboBoxReleaseTag = null;
     JButton    jButtonBrowse       = new javax.swing.JButton();
     JButton    jButtonOK           = new javax.swing.JButton();
     JButton    jButtonCancel       = new javax.swing.JButton();
 
     /** label of the 'OK' button */
     private static final String ok = new String("OK");
     
     /** label of the 'Cancel' button */
     private static final String cancel = new String("cancel");
     
     
     //
     // construction
     //
     
     /** standard constructor */
     public JParseConfigurationDialog(JFrame frame,ConfDB database)
     {
 	super(frame,true);
 	this.frame = frame;
 	this.database = database;
 	
 	setTitle("Parse configuration file");
 
 	setContentPane(initComponents());
 	
 	// ensure that the text field gets focus
 	addComponentListener(new ComponentAdapter() {
 	    public void componentShown(ComponentEvent e) {
 		jTextFieldFileName.requestFocusInWindow();
 	    }
 	});
     }
 
     
     //
     // member functions
     //
 
     /** was a valid choice made? */
     public boolean validChoice() { return validChoice; }
     
     /** return the name of the file to be parsed */
     public String fileName() { return fileName; }
     
     /** return the choosen release tag */
     public String releaseTag() { return releaseTag; }
     
     /** set the release tag, by making it the selected item in the combo box */
     public boolean setReleaseTag(String releaseTag)
     {
 	for (int i=0;i<jComboBoxReleaseTag.getItemCount();i++) {
 	    String itemString = (String)jComboBoxReleaseTag.getItemAt(i);
 	    if (itemString.equals(releaseTag)) {
 		jComboBoxReleaseTag.setSelectedIndex(i);
 		return true;
 	    }
 	}
 	return false;
     }
 
     /** fileName entered callback */
     public void jTextFieldFileNameActionPerformed(ActionEvent e)
     {
 	if (jTextFieldFileName.getText().length()>0&&
 	    jComboBoxReleaseTag.getSelectedIndex()>0) {
 	    jButtonOK.setEnabled(true);
 	}
 	else {
 	    validChoice = false;
 	    jButtonOK.setEnabled(false);
 	}
     }
     
     /** releaseTag choosen callback */
     public void jComboBoxReleaseTagActionPerformed(ActionEvent e)
     {
 	if (jTextFieldFileName.getText().length()>0&&
 	    jComboBoxReleaseTag.getSelectedIndex()>0) {
 	    jButtonOK.setEnabled(true);
 	}
 	else {
 	    validChoice = false;
 	    jButtonOK.setEnabled(false);
 	}
     }
 
     /** "Browse ..." button callback */
     public void jButtonBrowseActionPerformed(ActionEvent e)
     {
 	JFileChooser fileChooser = new JFileChooser();
	fileChooser.addChoosableFileFilter(new JPythonFileFilter());
 	fileChooser.setAcceptAllFileFilterUsed(false);
 	
 	int result = fileChooser.showOpenDialog(this);
 	if (result == JFileChooser.APPROVE_OPTION) {
 	    File file = fileChooser.getSelectedFile();
 	    jTextFieldFileName.setText(file.getAbsolutePath());
 	    jComboBoxReleaseTagActionPerformed(null);
 	}
     }
     
     /** "OK" button callback */
     public void jButtonOKActionPerformed(ActionEvent e)
     {
 	fileName    = jTextFieldFileName.getText();
 	releaseTag  = (String)jComboBoxReleaseTag.getSelectedItem();
 	validChoice = true;
 	setVisible(false);
     }
 
     /** "Cancel" button callback */
     public void jButtonCancelActionPerformed(ActionEvent e)
     {
 	validChoice = false;
 	setVisible(false);
     }
 
 
     //
     // private member functions
     //
 
     /** init GUI components */
     private JPanel initComponents()
     {
 	JPanel jPanel = new JPanel();
         JLabel jLabel1 = new javax.swing.JLabel();
         JLabel jLabel2 = new javax.swing.JLabel();
 
         jLabel1.setText("File Name:");
         jLabel2.setText("Release Tag:");
 
 	try {
 	    jComboBoxReleaseTag = new JComboBox(database.getReleaseTags());
 	    jComboBoxReleaseTag.setBackground(new java.awt.Color(255, 255, 255));
 	}
 	catch (DatabaseException e) {
 	    System.err.println(e.getMessage());
 	    jComboBoxReleaseTag = new JComboBox();
 	}
 
         jButtonBrowse.setText("Browse ...");
         jButtonOK.setText("OK");
         jButtonCancel.setText("Cancel");
 
 	jButtonOK.setEnabled(false);
 
 	jTextFieldFileName.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTextFieldFileNameActionPerformed(e);
 		}
 	 });
 
 	jComboBoxReleaseTag.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jComboBoxReleaseTagActionPerformed(e);
 		}
 	 });
 
 	jButtonBrowse.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonBrowseActionPerformed(e);
 		}
 	 });
 
 	jButtonOK.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonOKActionPerformed(e);
 		}
 	 });
 
 	jButtonCancel.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonCancelActionPerformed(e);
 		}
 	 });
 
 
         org.jdesktop.layout.GroupLayout layout =
 	    new org.jdesktop.layout.GroupLayout(jPanel);
         jPanel.setLayout(layout);
         layout
 	    .setHorizontalGroup(layout
 				.createParallelGroup(org.jdesktop
 						     .layout.GroupLayout
 						     .LEADING)
 				.add(layout
 				     .createSequentialGroup()
 				     .add(layout
 					  .createParallelGroup(org.jdesktop
 							       .layout.GroupLayout
 							       .LEADING)
 					  .add(layout
 					       .createSequentialGroup()
 					       .addContainerGap()
 					       .add(layout
 						    .createParallelGroup(org.jdesktop
 									 .layout
 									 .GroupLayout
 									 .LEADING)
 						    .add(layout
 							 .createSequentialGroup()
 							 .addPreferredGap(org
 									  .jdesktop
 									  .layout
 									  .LayoutStyle.RELATED)
 							   .add(jLabel2))
 						    .add(jLabel1))
 					       .addPreferredGap(org.jdesktop
 								.layout.LayoutStyle
 								.RELATED)
 					       .add(layout
 						    .createParallelGroup(org.jdesktop
 									 .layout
 									 .GroupLayout
 									 .LEADING)
 						    .add(layout
 							 .createSequentialGroup()
 							 .add(jTextFieldFileName,
 							      org.jdesktop
 							      .layout.GroupLayout
 							      .DEFAULT_SIZE, 245,
 							      Short.MAX_VALUE)
 							 .addPreferredGap(org
 									  .jdesktop
 									  .layout
 									  .LayoutStyle
 									  .RELATED)
 							 .add(jButtonBrowse,
 							      org.jdesktop
 							      .layout.GroupLayout
 							      .PREFERRED_SIZE, 104,
 							      org.jdesktop
 							      .layout.GroupLayout
 							      .PREFERRED_SIZE))
 						    .add(jComboBoxReleaseTag, 0, 355,
 							 Short.MAX_VALUE)))
 					  .add(layout
 					       .createSequentialGroup()
 					       .add(139, 139, 139)
 					       .add(jButtonOK,
 						    org.jdesktop
 						    .layout.GroupLayout.DEFAULT_SIZE,
 						    92, Short.MAX_VALUE)
 					       .addPreferredGap(org.jdesktop
 								.layout.LayoutStyle
 								.RELATED)
 					       .add(jButtonCancel,
 						    org.jdesktop
 						    .layout.GroupLayout
 						    .DEFAULT_SIZE, 92,
 						    Short.MAX_VALUE)
 					       .add(127, 127, 127)))
 				     .addContainerGap())
 				);
 
         layout
 	    .setVerticalGroup(layout
 			      .createParallelGroup(org.jdesktop
 						   .layout.GroupLayout.LEADING)
 			      .add(layout.createSequentialGroup()
 				   .addContainerGap()
 				   .add(layout.createParallelGroup(org.jdesktop
 								   .layout
 								   .GroupLayout
 								   .BASELINE)
 					.add(jLabel1)
 					.add(jTextFieldFileName, org.jdesktop
 					     .layout.GroupLayout.PREFERRED_SIZE,
 					     org.jdesktop
 					     .layout.GroupLayout.DEFAULT_SIZE,
 					     org.jdesktop
 					     .layout.GroupLayout.PREFERRED_SIZE)
 					.add(jButtonBrowse))
 				   .addPreferredGap(org.jdesktop
 						    .layout.LayoutStyle.RELATED)
 				   .add(layout
 					.createParallelGroup(org.jdesktop
 							     .layout.GroupLayout
 							     .BASELINE)
 					.add(jLabel2)
 					.add(jComboBoxReleaseTag,
 					     org.jdesktop
 					     .layout.GroupLayout.PREFERRED_SIZE,
 					     org.jdesktop
 					     .layout.GroupLayout.DEFAULT_SIZE,
 					     org.jdesktop
 					     .layout.GroupLayout.PREFERRED_SIZE))
 				   .addPreferredGap(org.jdesktop
 						    .layout.LayoutStyle.RELATED, 24,
 						    Short.MAX_VALUE)
 				   .add(layout.createParallelGroup(org.jdesktop
 								   .layout
 								   .GroupLayout
 								   .BASELINE)
 					.add(jButtonOK)
 					.add(jButtonCancel))
 				   .addContainerGap())
 			      );
 	
 	layout.linkSize(new java.awt.Component[] { jButtonBrowse,
 						   jComboBoxReleaseTag,
 						   jTextFieldFileName },
 			org.jdesktop.layout.GroupLayout.VERTICAL);
 	
 
 	return jPanel;
     }
 
 }
 
 
 /**
 * JPythonFileFilter
  * ----------------
  * @author Philipp Schieferdecker
  */
class JPythonFileFilter extends FileFilter
 {
     /** FileFilter.accept() */
     public boolean accept(File f)
     {
         if (f.isDirectory()) return true;
 	
         String extension = getExtension(f);
         if (extension != null) {
             if (extension.equals("py"))
 		return true;
 	    else
                 return false;
 	}
         return false;
     }
     
     /* get description of this filter */
     public String getDescription()
     {
 	return "CMSSW Python configuration files (*.py)";
     }
     
     /** get extension of a file name */
     public String getExtension(File f)
     {
         String ext = null;
         String s = f.getName();
         int i = s.lastIndexOf('.');
 	
         if (i>0 && i<s.length()-1) ext = s.substring(i+1).toLowerCase();
         return ext;
     }
 }
 
