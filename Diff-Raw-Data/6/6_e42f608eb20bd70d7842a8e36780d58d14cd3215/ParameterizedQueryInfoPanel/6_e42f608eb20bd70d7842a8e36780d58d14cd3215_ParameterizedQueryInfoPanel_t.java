 /**
  * 
  */
 package edu.wustl.cab2b.client.ui.parameterizedQuery;
 
 import java.awt.Dimension;
 
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import edu.wustl.cab2b.client.ui.RiverLayout;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTextField;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTitledPanel;
 
 /**
  * Class to generate Parameterized Query Information GUI 
  * @author Deepak_Shingan
  *
  */
 public class ParameterizedQueryInfoPanel extends Cab2bTitledPanel {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     private Cab2bPanel containerPanel;
 
     private Cab2bLabel titleLabel;
 
     private Cab2bLabel descriptionLabel;
 
     private Cab2bTextField titleTextField;
 
     private JTextArea queryTextArea;
 
     public ParameterizedQueryInfoPanel() {
         initGUI();
     }
 
     /**
      * Method to create informationQueryPanel 
      */
     private void initGUI() {
         containerPanel = new Cab2bPanel(new RiverLayout(5, 10));
         titleLabel = new Cab2bLabel("Title");
         descriptionLabel = new Cab2bLabel("Description");
         titleTextField = new Cab2bTextField("", new Dimension(225, 22));
         queryTextArea = new JTextArea();
         queryTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(queryTextArea);
        scrollPane.setPreferredSize(new Dimension(225, 60));
 
         containerPanel.add(titleLabel);
         containerPanel.add("tab ", titleTextField);
         containerPanel.add("br ", descriptionLabel);
         containerPanel.add("tab ", scrollPane);
         containerPanel.add("br ", new Cab2bLabel());
         this.setTitle("Information");
         this.setContentContainer(containerPanel);
     }
 
     /**
      * @return query name written in "Title" TextBox
      */
     public String getQueryName() {
         return titleTextField.getText();
     }
 
     /**
      * @return query description written in "Description" TextBox
      */
     public String getQueryDecription() {
         return queryTextArea.getText();
     }
 
     public void setQueryName(String name) {
         titleTextField.setText(name);
     }
 
     public void setQueryDecription(String desc) {
         queryTextArea.setText(desc);
     }
 
 }
