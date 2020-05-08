 package gov.nih.nci.ncicb.cadsr.loader.ui;
 import gov.nih.nci.ncicb.cadsr.domain.Concept;
 import gov.nih.nci.ncicb.cadsr.domain.DataElement;
 import gov.nih.nci.ncicb.cadsr.domain.ObjectClass;
 import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewEvent;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewEventType;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewListener;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.NavigationEvent;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.NavigationListener;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.ReviewableUMLNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.UMLNode;
 import gov.nih.nci.ncicb.cadsr.loader.util.RunMode;
 import gov.nih.nci.ncicb.cadsr.loader.util.StringUtil;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.AbstractButton;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 public class ButtonPanel extends JPanel implements ActionListener, 
   PropertyChangeListener
 {
   private JButton addButton, deleteButton, saveButton, switchButton;
   private JButton previousButton, nextButton;
   private JCheckBox reviewButton;
   
   private List<ReviewListener> reviewListeners 
     = new ArrayList<ReviewListener>();
     
   private List<NavigationListener> navigationListeners 
     = new ArrayList<NavigationListener>();
     
   private List<PropertyChangeListener> propChangeListeners 
     = new ArrayList<PropertyChangeListener>();
   
   private ConceptEditorPanel conceptEditorPanel;
   private Editable viewPanel;
   private Editable editable;
   
   static final String ADD = "ADD",
     DELETE = "DELETE",
     SAVE = "APPLY", 
     PREVIOUS = "PREVIOUS",
     NEXT = "NEXT",
     PREVIEW = "PREVIEW",
     REVIEW = "REVIEW",
     SETUP = "SETUP",
     SWITCH = "SWITCH",
     DELETEBUTTON = "DELETEBUTTON";
 
   static final String 
     SWITCH_TO_DE = "Map to DE",
     SWITCH_TO_OC = "Map to OC",
     SWITCH_TO_CONCEPT = "Map to Concepts";
     
   private RunMode runMode = null;
     
   
   public void addPropertyChangeListener(PropertyChangeListener l) {
     propChangeListeners.add(l);
   }
   
   public ButtonPanel(ConceptEditorPanel conceptEditorPanel, 
     Editable viewPanel, Editable editable) 
   {
 
     this.conceptEditorPanel = conceptEditorPanel;
     this.viewPanel = viewPanel;
     this.editable = editable;
     
     UserSelections selections = UserSelections.getInstance();
     
     runMode = (RunMode)(selections.getProperty("MODE"));
     
     addButton = new JButton("Add");
     deleteButton = new JButton("Remove");
     saveButton = new JButton("Apply");
     switchButton = new JButton();
     if(runMode.equals(RunMode.Reviewer))
       reviewButton = new JCheckBox("<html> Model <br> Owner <br> Verified</html");
     else if(runMode.equals(RunMode.Curator))
       reviewButton = new JCheckBox("<html>Human<br>Verified</html>");
     else { //if(runMode.equals(RunMode.UnannotatedXmi)) {
       reviewButton = new JCheckBox("<html>Not<br>Available</html>");
       reviewButton.setVisible(false);
     }
 
 
     previousButton = new JButton("Previous");
     nextButton = new JButton("Next");
 
 
     reviewButton.setSelected(isReviewed());
 
     reviewButton.setActionCommand(REVIEW);
     addButton.setActionCommand(ADD);
     deleteButton.setActionCommand(DELETE);
     saveButton.setActionCommand(SAVE);
     previousButton.setActionCommand(PREVIOUS);
     nextButton.setActionCommand(NEXT);
     switchButton.setActionCommand(SWITCH);
     
     addButton.addActionListener(this);
     deleteButton.addActionListener(this);
     saveButton.addActionListener(this);
     reviewButton.addActionListener(this);
     previousButton.addActionListener(this);
     nextButton.addActionListener(this);
     switchButton.addActionListener(this);
     
     this.add(addButton);
     //this.add(deleteButton);
     this.add(saveButton);
     this.add(reviewButton);
     this.add(previousButton);
     this.add(nextButton);
     this.add(switchButton);
 
   }
   
   public void update() 
   {
     reviewButton.setSelected(isReviewed());
     if(editable instanceof DEPanel) {
       DataElement de = (DataElement)conceptEditorPanel.getNode().getUserObject();
       if(!StringUtil.isEmpty(de.getPublicId())) 
       {
         setSwitchButtonText(ButtonPanel.SWITCH_TO_CONCEPT);
       } else {
         setSwitchButtonText(ButtonPanel.SWITCH_TO_DE);
       }
     } else if(editable instanceof OCPanel) {
       ObjectClass oc = (ObjectClass)conceptEditorPanel.getNode().getUserObject();
       if(!StringUtil.isEmpty(oc.getPublicId())) 
       {
         setSwitchButtonText(ButtonPanel.SWITCH_TO_CONCEPT);
       }
       else 
       {
         setSwitchButtonText(ButtonPanel.SWITCH_TO_OC);
       }
     } else if(editable == null) { 
       return;
     }
   }
   
   public void addReviewListener(ReviewListener listener) {
     reviewListeners.add(listener);
   }
   
   public void addNavigationListener(NavigationListener listener) 
   {
     navigationListeners.add(listener);
   }
   
   public void propertyChange(PropertyChangeEvent e) 
   {
     if(e.getPropertyName().equals(DELETE)) {
       deleteButton.setEnabled((Boolean)e.getNewValue());      
     } else if(e.getPropertyName().equals(ADD)) {
       addButton.setEnabled((Boolean)e.getNewValue());
     } else if(e.getPropertyName().equals(SAVE)) {
       setSaveButtonState((Boolean)e.getNewValue());
     } else if(e.getPropertyName().equals(REVIEW)) {
       reviewButton.setEnabled((Boolean)e.getNewValue());
     } else if (e.getPropertyName().equals(SETUP)) {
       initButtonPanel();
     } else if (e.getPropertyName().equals(SWITCH)) {
       switchButton.setEnabled((Boolean)e.getNewValue());
     }
   }
   
   private void fireNavigationEvent(NavigationEvent event) 
   {
     for(NavigationListener l : navigationListeners)
       l.navigate(event);
   }
   
   private void setSwitchButtonText(String text) 
   {
     switchButton.setText(text);
   }
   
   private void initButtonPanel() {  
     Concept[] concepts = conceptEditorPanel.getConcepts();
     if(concepts.length < 2)
       deleteButton.setEnabled(false);
     else
       deleteButton.setEnabled(true);
     
     if(conceptEditorPanel.areAllFieldEntered()) {
       reviewButton.setEnabled(true);
       addButton.setEnabled(true);
     } else {
       addButton.setEnabled(false);
       reviewButton.setEnabled(false);
     }
     setSaveButtonState(false);
     
     if(concepts.length == 0)
       reviewButton.setEnabled(false);
     
     switchButton.setVisible(editable instanceof DEPanel);
     
     if(editable instanceof DEPanel && conceptEditorPanel.getVDPanel().isMappedToLocalVD())
       switchButton.setEnabled(false);
     
     // disable add if DEPanel is showing
     if(editable instanceof DEPanel) {
       DataElement de = (DataElement)conceptEditorPanel.getNode().getUserObject();
       if(!StringUtil.isEmpty(de.getPublicId())) {
         addButton.setVisible(false);
         deleteButton.setVisible(false);
       } else {
         addButton.setVisible(true);
         deleteButton.setVisible(true);
       }
     } else if(editable instanceof OCPanel) {
       ObjectClass oc = (ObjectClass)conceptEditorPanel.getNode().getUserObject();
       if(!StringUtil.isEmpty(oc.getPublicId())) {
         addButton.setVisible(false);
         deleteButton.setVisible(false);
       } else {
         addButton.setVisible(true);
         deleteButton.setVisible(true);
       }
     } else if(editable == null) {
       addButton.setVisible(true);
       deleteButton.setVisible(true);
     }
     
   }
   
   private void setSaveButtonState(boolean b) {
     saveButton.setEnabled(b);
 
     PropertyChangeEvent evt = new PropertyChangeEvent(this, SAVE, null, b);
     firePropertyChangeEvent(evt);
   }
   
   public void setEditablePanel(Editable editable) {
     this.editable = editable;
   }
 
   public void navigate(NavigationEvent evt) {  
     if(saveButton.isEnabled()) {
       if(JOptionPane.showConfirmDialog(this, "There are unsaved changes in this concept, would you like to apply the changes now?", "Unsaved Changes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) 
       {
         try {
           viewPanel.applyPressed();
         } catch (ApplyException e){
         } // end of try-catch
       }
     }
   }
   
   private void firePropertyChangeEvent(PropertyChangeEvent evt) {
     for(PropertyChangeListener l : propChangeListeners) 
       l.propertyChange(evt);
   }
   
   public void actionPerformed(ActionEvent evt) {
     AbstractButton button = (AbstractButton)evt.getSource();
     if(button.getActionCommand().equals(SAVE)) {
       try {
         viewPanel.applyPressed();
 
        //reviewButton.setEnabled(false);
        //saveButton.setEnabled(false);
 
         ReviewEvent event = new ReviewEvent();
         event.setUserObject(conceptEditorPanel.getNode()); 
         event.setReviewed(reviewButton.isSelected());
         
         if(runMode.equals(RunMode.Reviewer)) {
           event.setType(ReviewEventType.Owner);
         } else if (runMode.equals(RunMode.Curator)) {
           event.setType(ReviewEventType.Curator);
         } else return;
         
         fireReviewEvent(event);
       } catch (ApplyException e){
       } // end of try-catch
     } else if(button.getActionCommand().equals(ADD)) {
         conceptEditorPanel.addPressed();
  
     } else if(button.getActionCommand().equals(DELETE)) {
         conceptEditorPanel.removePressed();
     } else if(button.getActionCommand().equals(PREVIOUS)) {
       NavigationEvent event = new NavigationEvent(NavigationEvent.NAVIGATE_PREVIOUS);
       fireNavigationEvent(event);
       conceptEditorPanel.setRemove(false);
       //remove = false;
     } else if(button.getActionCommand().equals(NEXT)) {
       NavigationEvent event = new NavigationEvent(NavigationEvent.NAVIGATE_NEXT);
       fireNavigationEvent(event);
       conceptEditorPanel.setRemove(false);
       //remove = false;
     } else if(button.getActionCommand().equals(SWITCH)) {
       if(switchButton.getText().equals(SWITCH_TO_DE)) {
         ((UMLElementViewPanel)viewPanel).switchCards(UMLElementViewPanel.DE_PANEL_KEY);
         switchButton.setText(SWITCH_TO_CONCEPT);
         addButton.setVisible(false);
         deleteButton.setVisible(false);
       } else if (switchButton.getText().equals(SWITCH_TO_CONCEPT)) {
         ((UMLElementViewPanel)viewPanel).switchCards(UMLElementViewPanel.CONCEPT_PANEL_KEY);
         if(editable instanceof DEPanel) {
           switchButton.setText(SWITCH_TO_DE);
         } else if(editable instanceof OCPanel) {
           switchButton.setText(SWITCH_TO_OC);
         } else if(editable == null) {
         
         }
         addButton.setVisible(true);
         deleteButton.setVisible(true);
       } else if(switchButton.getText().equals(SWITCH_TO_OC)) {
          ((UMLElementViewPanel)viewPanel).switchCards(UMLElementViewPanel.OC_PANEL_KEY);
          switchButton.setText(SWITCH_TO_CONCEPT);
       }
     }
 
     else if(button.getActionCommand().equals(REVIEW)) {
 
       ReviewEvent event = new ReviewEvent();
       event.setUserObject(conceptEditorPanel.getNode()); 
       event.setReviewed(reviewButton.isSelected());
 
       if(runMode.equals(RunMode.Reviewer)) {
         event.setType(ReviewEventType.Owner);
       } else if (runMode.equals(RunMode.Curator)) {
         event.setType(ReviewEventType.Curator);
       } else return;
 
       fireReviewEvent(event);
       
       //if item is reviewed go to next item in the tree
       if(reviewButton.isSelected()) 
       {
         NavigationEvent goToNext = new NavigationEvent(NavigationEvent.NAVIGATE_NEXT);
         fireNavigationEvent(goToNext);
       }
         
     }
     
   }
  
   
   private void fireReviewEvent(ReviewEvent event) {
     for(ReviewListener l : reviewListeners)
       l.reviewChanged(event);
   }
   
   private boolean isReviewed() 
   {
     UMLNode node = conceptEditorPanel.getNode();
     return ((ReviewableUMLNode)node).isReviewed();
   }
   
 }
