 package gov.nih.nci.ncicb.cadsr.loader.ui;
 
 import gov.nih.nci.ncicb.cadsr.domain.AdminComponent;
 import gov.nih.nci.ncicb.cadsr.domain.DataElement;
 import gov.nih.nci.ncicb.cadsr.domain.ValueDomain;
 import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
 import gov.nih.nci.ncicb.cadsr.domain.ObjectClass;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.UMLNode;
 import gov.nih.nci.ncicb.cadsr.loader.ui.util.UIUtil;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 import gov.nih.nci.ncicb.cadsr.loader.event.*;
 import gov.nih.nci.ncicb.cadsr.loader.util.DEMappingUtil;
 import gov.nih.nci.ncicb.cadsr.loader.util.UserPreferences;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import javax.swing.*;
 
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class DEPanel extends JPanel
   implements Editable {
 
   private JButton searchDeButton = new JButton("Search Data Element");
   private JButton clearButton = new JButton("Clear");
 
   private JLabel deLongNameTitleLabel = new JLabel("Data Element Long Name"),
     deLongNameValueLabel = new JLabel(),
     deIdTitleLabel = new JLabel("Public ID / Version"),
     deIdValueLabel = new JLabel(),
     deContextNameTitleLabel = new JLabel("Data Element Context"),
     deContextNameValueLabel = new JLabel(),
     vdLongNameTitleLabel = new JLabel("Value Domain Long Name"), vdLongNameValueLabel = new JLabel();
   
   private JScrollPane scrollPane;
 
 
   private DataElement tempDE, de;
   private UMLNode node;
 
   private List<PropertyChangeListener> propChangeListeners 
     = new ArrayList<PropertyChangeListener>();  
 
   private List<ElementChangeListener> changeListeners 
     = new ArrayList<ElementChangeListener>();
 
 
   private static final String SEARCH = "SEARCH", CLEAR = "CLEAR";
 
   private boolean modified = false;
 
   private InheritedAttributeList inheritedAttributes = InheritedAttributeList.getInstance();
 
   private UserPreferences userPrefs = UserPreferences.getInstance();
   
   public DEPanel(UMLNode node)  {
     this.node = node;
 
     if((node.getUserObject() instanceof DataElement))
       de = (DataElement)node.getUserObject();
 
     initUI();
 
   }
 
   private void initUI() {
     
     this.setLayout(new BorderLayout());
     JPanel mainPanel = new JPanel(new GridBagLayout());
 
     scrollPane = new JScrollPane(mainPanel);
     
     // scrollPane.getVerticalScrollBar().setUnitIncrement(30);
 
     UIUtil.insertInBag(mainPanel, clearButton, 0, 5, 2 ,1);
     UIUtil.insertInBag(mainPanel, searchDeButton, 1, 5);
     
     
     UIUtil.insertInBag(mainPanel, deLongNameTitleLabel, 0, 1);
     UIUtil.insertInBag(mainPanel, deLongNameValueLabel, 1, 1);
 
     UIUtil.insertInBag(mainPanel, deIdTitleLabel, 0, 2);
     UIUtil.insertInBag(mainPanel, deIdValueLabel, 1, 2);
 
     UIUtil.insertInBag(mainPanel, deContextNameTitleLabel, 0, 3);
     UIUtil.insertInBag(mainPanel, deContextNameValueLabel, 1, 3);
 
     UIUtil.insertInBag(mainPanel, vdLongNameTitleLabel, 0, 4);
     UIUtil.insertInBag(mainPanel, vdLongNameValueLabel, 1, 4);
 
     JPanel titlePanel = new JPanel();
     JLabel title = new JLabel("Map to CDE");
     titlePanel.add(title);
 
     this.add(scrollPane);
     this.add(titlePanel, BorderLayout.NORTH);
     this.setSize(300, 300);
     
     searchDeButton.setActionCommand(SEARCH);
     clearButton.setActionCommand(CLEAR);
     
     searchDeButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           JButton button = (JButton)event.getSource();
           if(button.getActionCommand().equals(SEARCH)) {
             CadsrDialog cd = BeansAccessor.getCadsrDEDialog();
 
             // update dialog with current node
             cd.init(node);
             cd.setAlwaysOnTop(true);
             cd.setVisible(true);
             
            tempDE = (DataElement)cd.getAdminComponent();
 
             if(tempDE != null){
               // Check for conflict
               DataElement confDe = DEMappingUtil.checkConflict(de ,tempDE);
               if(confDe != null) {
                 JOptionPane.showMessageDialog
                   (null, PropertyAccessor.getProperty("de.conflict", new String[] {de.getDataElementConcept().getProperty().getLongName(), confDe.getDataElementConcept().getProperty().getLongName()}), "Conflict", JOptionPane.ERROR_MESSAGE);
                 return;
               }
             if(tempDE != null) {
               AdminComponent ac = DEMappingUtil.checkDuplicate(de,tempDE);
               if(ac != null) 
               {
                 if(ac instanceof ObjectClass)
                 JOptionPane.showMessageDialog(null, "This creates a duplicate mapping with " + LookupUtil.lookupFullName((ObjectClass)ac), "Conflict", JOptionPane.ERROR_MESSAGE);
                 if(ac instanceof DataElement)
                 JOptionPane.showMessageDialog(null, "This creates a duplicate mapping with " + ((DataElement)ac).getDataElementConcept().getProperty().getLongName(), "Conflict", JOptionPane.ERROR_MESSAGE);
                 return;
               }
             }
             updateFields();
                            
             firePropertyChangeEvent(
                                     new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
 
 //             firePropertyChangeEvent(
 //                                     new PropertyChangeEvent(this, ButtonPanel.SWITCH, null, false));
 
             modified = true;
             }
           }
           
         }
       });
       
       clearButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           JButton button = (JButton)event.getSource();
           if(button.getActionCommand().equals(CLEAR)) {
             clear();
             
             firePropertyChangeEvent
               (new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
             
 
             modified = true;
 //               fireElementChangeEvent(new ElementChangeEvent(node));
                   
           }
         }
         });
 
       if((node.getUserObject() instanceof DataElement))
         firePropertyChangeEvent
           (new PropertyChangeEvent(this, ButtonPanel.SWITCH, null, StringUtil.isEmpty(de.getPublicId())));
       
 
   }
 
   public void updateNode(UMLNode node) 
   {
   
     this.node = node;
     if((node.getUserObject() instanceof DataElement)) {
       de = (DataElement)node.getUserObject();
       
       if(de.getPublicId() != null) {
         deLongNameValueLabel.setText("<html><body>" + de.getLongName() + "</body></html>");
         deIdValueLabel.setText(de.getPublicId() + " v" + de.getVersion());
         deContextNameValueLabel.setText(de.getContext().getName());
         vdLongNameValueLabel.setText(de.getValueDomain().getLongName());
       }
       else {
         clear();
       }
       
       firePropertyChangeEvent
         (new PropertyChangeEvent(this, ButtonPanel.SWITCH, null, StringUtil.isEmpty(de.getPublicId())));
       
     }
   }
   
   public void addPropertyChangeListener(PropertyChangeListener l) {
     propChangeListeners.add(l);
   }
 
   public void addElementChangeListener(ElementChangeListener listener) {
     changeListeners.add(listener);
   }
 
   private void firePropertyChangeEvent(PropertyChangeEvent evt) {
     for(PropertyChangeListener l : propChangeListeners) 
       l.propertyChange(evt);
   }
 
   private void clear() {
     tempDE = DomainObjectFactory.newDataElement();
 
     ValueDomain vd = DomainObjectFactory.newValueDomain();
 
     List<AttributeDatatypePair> attTypesPairs = ElementsLists.getInstance().getElements(new AttributeDatatypePair("", ""));
     String datatype = null;
     String attributeName = LookupUtil.lookupFullName(de);
     for(AttributeDatatypePair pair : attTypesPairs) {
       if(pair.getAttributeName().equals(attributeName)) {
         datatype = pair.getDatatype();
       }
     }
 
     if(datatype == null)
       datatype = "";
     else {
       if(DatatypeMapping.getKeys().contains(datatype.toLowerCase())) 
         datatype = DatatypeMapping.getMapping().get(datatype.toLowerCase());
     }
 
     vd.setLongName(datatype);
 
     tempDE.setValueDomain(vd);
 
     deLongNameValueLabel.setText("");
     deIdValueLabel.setText("");
     deContextNameValueLabel.setText("");
     vdLongNameValueLabel.setText("");
     
   }
 
   public void setEnabled(boolean enabled) {
     searchDeButton.setEnabled(enabled);
     clearButton.setEnabled(enabled);
   }
   
   private void updateFields() {
     deLongNameValueLabel.setText(tempDE.getLongName());
     deIdValueLabel.setText(ConventionUtil.publicIdVersion(tempDE));
     if(tempDE.getContext() != null)
       deContextNameValueLabel.setText(tempDE.getContext().getName());
     else 
       deContextNameValueLabel.setText("");
     
     if(tempDE.getValueDomain() != null)
       vdLongNameValueLabel.setText(tempDE.getValueDomain().getLongName());
     else 
       vdLongNameValueLabel.setText("");
 
   }
   
   public void applyPressed() 
   {
     apply();
   }
   
   public void apply() 
   {
     if(!modified)
       return;
 
     // uncomment to enable feature
 
 //     if(inheritedAttributes.isInherited(de)) { 
 //       if(!userPrefs.getBoolean("de.over.vd.mapping.warning")) {
 //         DontWarnMeAgainDialog dontWarnDialog = new DontWarnMeAgainDialog("de.over.vd.mapping.warning");
 //       }
 //     }
 
     modified = false;
     
     de.setLongName(tempDE.getLongName());
     de.setPublicId(tempDE.getPublicId());
     de.setVersion(tempDE.getVersion());
     de.setContext(tempDE.getContext());
     de.setValueDomain(tempDE.getValueDomain());
 
     fireElementChangeEvent(new ElementChangeEvent(node));
 
     // Set the OC ID / Version
     // iterate over all DE sibblings. 
 //     String pubId = null;
 //     Float version = null;
 //     List<DataElement> des = ElementsLists.getInstance().getElements(de);
 
 //     for(DataElement curDe : des) {
 //       if(!StringUtil.isEmpty(curDe.getPublicId())) {
 //         if(de.getDataElementConcept().getObjectClass() == curDe.getDataElementConcept().getObjectClass()) {
 //           pubId = curDe.getDataElementConcept().getObjectClass().getPublicId();
 //           version = curDe.getDataElementConcept().getObjectClass().getVersion();
 //         }
 //       }
 //     }
 
 //     de.getDataElementConcept().getObjectClass().setPublicId(pubId);
 //     de.getDataElementConcept().getObjectClass().setVersion(version);
 
      if(tempDE.getDataElementConcept() != null) {
        if(de.getDataElementConcept().getObjectClass().getPublicId() == null
           || de.getDataElementConcept().getObjectClass().getPublicId().length() == 0
         ) {
          JOptionPane.showMessageDialog(
            null,
            PropertyAccessor.getProperty("oc.mapping.warning"),
            "Please note", JOptionPane.INFORMATION_MESSAGE
          );
        }
          
        de.getDataElementConcept().getObjectClass().setPublicId
          (tempDE.getDataElementConcept().getObjectClass().getPublicId());
        de.getDataElementConcept().getObjectClass().setVersion
          (tempDE.getDataElementConcept().getObjectClass().getVersion());
        de.getDataElementConcept().getObjectClass().setLongName
          (tempDE.getDataElementConcept().getObjectClass().getLongName());
 
        de.getDataElementConcept().getProperty().setPublicId
          (tempDE.getDataElementConcept().getProperty().getPublicId());
        de.getDataElementConcept().getProperty().setVersion
          (tempDE.getDataElementConcept().getProperty().getVersion());
 
      } else {
       boolean found = false;
        List<DataElement> des = ElementsLists.getInstance()
           .getElements(DomainObjectFactory.newDataElement());
         for(DataElement curDe : des) {
           if(curDe.getDataElementConcept().getObjectClass() == de.getDataElementConcept().getObjectClass())
             if(!StringUtil.isEmpty(curDe.getPublicId())) 
             {
               found = true;
             }
       
         }
         if(!found) 
         {
           de.getDataElementConcept().getObjectClass().setPublicId(null);
           de.getDataElementConcept().getObjectClass().setVersion(null);
         }
      }
 
     if(tempDE.getDataElementConcept() != null) {
       firePropertyChangeEvent(new PropertyChangeEvent(this, ButtonPanel.SWITCH, null, false));
       firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.REVIEW, null, true));
     }
     else {
       firePropertyChangeEvent(new PropertyChangeEvent(this, ButtonPanel.SWITCH, null, true));
       firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.REVIEW, null, false));
     }  
     firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, false));
     
       
 
 
   }
 
   private void fireElementChangeEvent(ElementChangeEvent event) {
     for(ElementChangeListener l : changeListeners)
       l.elementChanged(event);
   }
 
 
 //  public static void main(String[] args)
 //  {
 ////    JFrame frame = new JFrame();
 ////    DEPanel dePanel = new DEPanel();
 ////    dePanel.setVisible(true);
 ////    frame.add(dePanel);
 ////    frame.setVisible(true);
 ////    frame.setSize(450, 350);
 //  }
 }
