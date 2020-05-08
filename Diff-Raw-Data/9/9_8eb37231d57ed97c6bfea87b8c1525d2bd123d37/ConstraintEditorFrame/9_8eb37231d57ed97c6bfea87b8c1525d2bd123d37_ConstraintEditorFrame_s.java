 package interiores.presentation.swing.views;
 
 import interiores.business.controllers.BinaryConstraintController;
 import interiores.business.controllers.FixedElementController;
 import interiores.business.controllers.FurnitureModelController;
 import interiores.business.controllers.FurnitureTypeController;
 import interiores.business.controllers.RoomController;
 import interiores.business.controllers.UnaryConstraintController;
 import interiores.business.events.catalogs.FTModifiedEvent;
 import interiores.business.events.constraints.BinaryConstraintAddedEvent;
 import interiores.business.events.constraints.BinaryConstraintRemovedEvent;
 import interiores.business.events.constraints.UnaryConstraintAddedEvent;
 import interiores.business.events.constraints.UnaryConstraintRemovedEvent;
 import interiores.business.models.Orientation;
 import interiores.business.models.constraints.furniture.UnaryConstraint;
 import interiores.core.Debug;
 import interiores.core.presentation.SwingController;
 import interiores.core.presentation.annotation.Listen;
 import interiores.utils.BinaryConstraintAssociation;
 import interiores.utils.CoolColor;
 import interiores.utils.Material;
 import interiores.utils.Range;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 
 /**
  *
  * @author alvaro
  */
 public class ConstraintEditorFrame extends JFrame {
 
     private String selectedId;
     private FurnitureTypeController furnitureTypeController;
     private FixedElementController fixedElementController;
     private FurnitureModelController furnitureModelController;
     private UnaryConstraintController unaryConstraintController;
     private BinaryConstraintController binaryConstraintController;
     private Collection<String> relatableFurniture;
     private Collection<JComponent> components;
     
     /** Creates new form ConstraintEditorFrame */
     public ConstraintEditorFrame(SwingController presentation) {
         initComponents();
         
         fixedElementController = presentation.getBusinessController(FixedElementController.class);
         furnitureTypeController = presentation.getBusinessController(FurnitureTypeController.class);
         furnitureModelController = presentation.getBusinessController(FurnitureModelController.class);
         unaryConstraintController = presentation.getBusinessController(UnaryConstraintController.class);
         binaryConstraintController = presentation.getBusinessController(BinaryConstraintController.class);
         
         RoomController roomController = presentation.getBusinessController(RoomController.class);
         integerSpinner.setModel(new SpinnerNumberModel(0, 0, 1000, roomController.getResolution()));
         
         components = new ArrayList();
         components.add(integerSpinner);
         components.add(floatSpinner);
         components.add(constPropEditor);
         components.add(relatableCombo);
         components.add(rangePanel);
         components.add(propertyLabel);
         components.add(wallPanel);
         components.add(positionPanel);
         
     }
     
     private String getTypeName(String id) {
         return id.replaceAll("\\d+", "");
     }
     
     public void setSelectedId(String selectedId) {
         this.selectedId = selectedId;
         String title = "";
         title += selectedId + " constraints";
         if (furnitureTypeController.getWallClinging(getTypeName(selectedId))) {
             title += " (Clinged to wall)";
         }
         selectedElementLabel.setText(title);
         updateConstraintEditorFrame();
     }
 
     
     private void updateConstraintEditorFrame() {
         
        // This is done to avoid aliasing
        relatableFurniture = new ArrayList(furnitureTypeController.getRoomFurniture());
        relatableFurniture.addAll(fixedElementController.getFixedNames());
        relatableFurniture.remove(selectedId);
        
        unaryConstraintRadio.setSelected(true);
        updateComboBox(constraintTypeList, unaryConstraintController.getAvailableConstraints());
        
        updateActiveConstraintsList();
     }
     
     private void showOnly(Collection<JComponent> toShowComponents) {
         for (JComponent component : this.components) {
             if (toShowComponents.contains(component))
                 component.setVisible(true);
             else
                 component.setVisible(false);
         }
     }
     
     private void updateComboBox(JComboBox combo, Collection<? extends Object> constraintList) {
         combo.removeAllItems();
         for (Object item : constraintList)
             combo.addItem(item.toString());
     }
     
     private void updateComboBox(JComboBox combo, Object[] objects) {
         combo.removeAllItems();
         for (Object item : objects)
             combo.addItem(item.toString());
     }
     
     private void setListValues(JList jlist, Collection<? extends Object> objects) {
             jlist.setListData(objects.toArray(new Object[objects.size()]));
     }
     
     private Orientation[] getOrientations() {
         ArrayList<Orientation> orientations = new ArrayList();
         if (NCheckBox.isSelected()) orientations.add(Orientation.N);
         if (ECheckBox.isSelected()) orientations.add(Orientation.E);
         if (WCheckBox.isSelected()) orientations.add(Orientation.W);
         if (SCheckBox.isSelected()) orientations.add(Orientation.S);
         return orientations.toArray(new Orientation[orientations.size()]);
     }
     
     @Listen({UnaryConstraintAddedEvent.class, UnaryConstraintRemovedEvent.class,
              BinaryConstraintAddedEvent.class, BinaryConstraintRemovedEvent.class,
              FTModifiedEvent.class})
     public void updateActiveConstraintsList() {      
         setListValues(activePlacement, furnitureTypeController.getPlacementDescriptions(getTypeName(selectedId)));
         setListValues(activeUnaries, unaryConstraintController.getConstraints(selectedId));
         setListValues(activeBinaries, binaryConstraintController.getConstraints(selectedId));
         repaint();
     }
     
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         constraintMultiplicity = new javax.swing.ButtonGroup();
         addConstraintButton = new javax.swing.JButton();
         cancelButton = new javax.swing.JButton();
         constraintTypeList = new javax.swing.JComboBox();
         unaryConstraintRadio = new javax.swing.JRadioButton();
         binaryConstraintRadio = new javax.swing.JRadioButton();
         selectedElementLabel = new javax.swing.JLabel();
         relatableCombo = new javax.swing.JComboBox();
         jScrollPanel2 = new javax.swing.JScrollPane();
         activeUnaries = new javax.swing.JList();
         jScrollPane2 = new javax.swing.JScrollPane();
         activeBinaries = new javax.swing.JList();
         constPropEditor = new javax.swing.JLayeredPane();
         floatSpinner = new javax.swing.JSpinner();
         propertyLabel = new javax.swing.JLabel();
         integerSpinner = new javax.swing.JSpinner();
         rangePanel = new javax.swing.JPanel();
         rangeMinLabel = new javax.swing.JLabel();
         rangeMaxLabel = new javax.swing.JLabel();
         minSpinner = new javax.swing.JSpinner();
         maxSpinner = new javax.swing.JSpinner();
         wallPanel = new javax.swing.JPanel();
         NCheckBox = new javax.swing.JCheckBox();
         WCheckBox = new javax.swing.JCheckBox();
         SCheckBox = new javax.swing.JCheckBox();
         ECheckBox = new javax.swing.JCheckBox();
         positionPanel = new javax.swing.JPanel();
         minXSpinner = new javax.swing.JSpinner();
         minYSpinner = new javax.swing.JSpinner();
         maxXSpinner = new javax.swing.JSpinner();
         maxYSpinner = new javax.swing.JSpinner();
         jLabel3 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jScrollPane3 = new javax.swing.JScrollPane();
         activePlacement = new javax.swing.JList();
 
         jTextArea1.setColumns(20);
         jTextArea1.setRows(5);
         jScrollPane1.setViewportView(jTextArea1);
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setAlwaysOnTop(true);
         setName("Constraint Editor"); // NOI18N
         setResizable(false);
 
         addConstraintButton.setLabel("Add constraint");
         addConstraintButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addConstraintButtonActionPerformed(evt);
             }
         });
 
         cancelButton.setText("Close");
         cancelButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelButtonActionPerformed(evt);
             }
         });
 
         constraintTypeList.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 constraintTypeListActionPerformed(evt);
             }
         });
 
         constraintMultiplicity.add(unaryConstraintRadio);
         unaryConstraintRadio.setLabel("Unary");
         unaryConstraintRadio.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 unaryConstraintRadioActionPerformed(evt);
             }
         });
 
         constraintMultiplicity.add(binaryConstraintRadio);
         binaryConstraintRadio.setLabel("Binary");
         binaryConstraintRadio.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 binaryConstraintRadioActionPerformed(evt);
             }
         });
 
         selectedElementLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         selectedElementLabel.setAlignmentY(0.0F);
         selectedElementLabel.setFocusable(false);
         selectedElementLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 
         relatableCombo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 relatableComboActionPerformed(evt);
             }
         });
 
         activeUnaries.setModel(new javax.swing.AbstractListModel() {
             String[] strings = {};
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         activeUnaries.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 activeUnariesKeyPressed(evt);
             }
         });
         jScrollPanel2.setViewportView(activeUnaries);
 
         activeBinaries.setModel(new javax.swing.AbstractListModel() {
             String[] strings = {};
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         activeBinaries.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 activeBinariesKeyPressed(evt);
             }
         });
         jScrollPane2.setViewportView(activeBinaries);
 
         floatSpinner.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.01f), Float.valueOf(0.0f), null, Float.valueOf(1.0f)));
         floatSpinner.setBounds(130, 40, 80, 20);
         constPropEditor.add(floatSpinner, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         propertyLabel.setText("Distance:");
         propertyLabel.setBounds(30, 40, 80, 15);
         constPropEditor.add(propertyLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
         integerSpinner.setBounds(130, 40, 80, 20);
         constPropEditor.add(integerSpinner, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         rangeMinLabel.setText("min:");
 
         rangeMaxLabel.setText("max:");
 
         javax.swing.GroupLayout rangePanelLayout = new javax.swing.GroupLayout(rangePanel);
         rangePanel.setLayout(rangePanelLayout);
         rangePanelLayout.setHorizontalGroup(
             rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rangePanelLayout.createSequentialGroup()
                 .addGap(24, 24, 24)
                 .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(rangeMinLabel)
                     .addComponent(minSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(53, 53, 53)
                 .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(rangeMaxLabel)
                     .addComponent(maxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(69, Short.MAX_VALUE))
         );
         rangePanelLayout.setVerticalGroup(
             rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(rangePanelLayout.createSequentialGroup()
                 .addGap(22, 22, 22)
                 .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(rangeMinLabel)
                     .addComponent(rangeMaxLabel))
                 .addGap(18, 18, 18)
                 .addGroup(rangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(maxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(minSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(37, Short.MAX_VALUE))
         );
 
         rangePanel.setBounds(0, 0, 266, 112);
         constPropEditor.add(rangePanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         NCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         NCheckBox.setLabel("N");
         NCheckBox.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
 
         WCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         WCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
         WCheckBox.setLabel("W");
 
         SCheckBox.setText("S");
         SCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         SCheckBox.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 
         ECheckBox.setText("E");
 
         javax.swing.GroupLayout wallPanelLayout = new javax.swing.GroupLayout(wallPanel);
         wallPanel.setLayout(wallPanelLayout);
         wallPanelLayout.setHorizontalGroup(
             wallPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(wallPanelLayout.createSequentialGroup()
                 .addGap(29, 29, 29)
                 .addComponent(WCheckBox)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(wallPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(SCheckBox)
                     .addComponent(NCheckBox))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(ECheckBox)
                 .addContainerGap(125, Short.MAX_VALUE))
         );
         wallPanelLayout.setVerticalGroup(
             wallPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(wallPanelLayout.createSequentialGroup()
                 .addGroup(wallPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(wallPanelLayout.createSequentialGroup()
                         .addGap(40, 40, 40)
                         .addGroup(wallPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(wallPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(WCheckBox)
                                 .addComponent(ECheckBox))
                             .addGroup(wallPanelLayout.createSequentialGroup()
                                 .addGap(15, 15, 15)
                                 .addComponent(SCheckBox))))
                     .addGroup(wallPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(NCheckBox)))
                 .addContainerGap(37, Short.MAX_VALUE))
         );
 
         wallPanel.setBounds(0, 0, 247, 132);
         constPropEditor.add(wallPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jLabel3.setText("x:");
 
         jLabel4.setText("y:");
 
         jLabel5.setText("Min:");
 
         jLabel6.setText("Max:");
 
         javax.swing.GroupLayout positionPanelLayout = new javax.swing.GroupLayout(positionPanel);
         positionPanel.setLayout(positionPanelLayout);
         positionPanelLayout.setHorizontalGroup(
             positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, positionPanelLayout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(positionPanelLayout.createSequentialGroup()
                         .addComponent(jLabel4)
                         .addGap(1, 1, 1)))
                 .addGap(16, 16, 16)
                 .addGroup(positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                         .addComponent(minYSpinner)
                         .addComponent(minXSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))
                     .addComponent(jLabel5))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                         .addComponent(maxXSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                         .addComponent(maxYSpinner))
                     .addComponent(jLabel6))
                 .addGap(156, 156, 156))
         );
         positionPanelLayout.setVerticalGroup(
             positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(positionPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, positionPanelLayout.createSequentialGroup()
                         .addGroup(positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel6)
                             .addComponent(jLabel5))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(minXSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel3))
                         .addGap(13, 13, 13)
                         .addGroup(positionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(minYSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel4)))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, positionPanelLayout.createSequentialGroup()
                         .addComponent(maxXSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(13, 13, 13)
                         .addComponent(maxYSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         positionPanel.setBounds(0, 0, 299, 104);
         constPropEditor.add(positionPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jScrollPane3.setViewportView(activePlacement);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(selectedElementLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addGap(29, 29, 29)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(unaryConstraintRadio)
                             .addComponent(binaryConstraintRadio)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                 .addComponent(relatableCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(constraintTypeList, javax.swing.GroupLayout.Alignment.LEADING, 0, 182, Short.MAX_VALUE)))
                         .addGap(55, 55, 55)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                             .addComponent(jScrollPanel2, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                             .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))))
                 .addContainerGap())
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addGap(50, 50, 50)
                 .addComponent(addConstraintButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 151, Short.MAX_VALUE)
                 .addComponent(cancelButton)
                 .addGap(73, 73, 73))
             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(layout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(constPropEditor, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(257, Short.MAX_VALUE)))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(selectedElementLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                 .addGap(2, 2, 2)
                                 .addComponent(unaryConstraintRadio)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(binaryConstraintRadio)
                                 .addGap(18, 18, 18)
                                 .addComponent(constraintTypeList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jScrollPanel2, 0, 0, Short.MAX_VALUE)))
                         .addGap(10, 10, 10)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(relatableCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 120, Short.MAX_VALUE)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(cancelButton)
                             .addComponent(addConstraintButton))
                         .addGap(21, 21, 21))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(57, 57, 57))))
             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(layout.createSequentialGroup()
                     .addGap(184, 184, 184)
                     .addComponent(constPropEditor, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(105, Short.MAX_VALUE)))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
 private void addConstraintButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addConstraintButtonActionPerformed
     if (binaryConstraintRadio.isSelected()) {
         String otherId = (String) relatableCombo.getSelectedItem();
         String type = (String) constraintTypeList.getSelectedItem();
         int dist = (Integer) integerSpinner.getValue();
         addBinaryConstraint(otherId, type, dist);
     }
     else if (unaryConstraintRadio.isSelected()) {
         String type = (String) constraintTypeList.getSelectedItem();
         String value = (String) relatableCombo.getSelectedItem();
         float floatValue = (Float) floatSpinner.getValue();
         Range range = new Range((Integer) minSpinner.getValue(), (Integer) maxSpinner.getValue());
         Range posX = new Range((Integer) minXSpinner.getValue(), (Integer) maxXSpinner.getValue());
         Range posY = new Range((Integer) minYSpinner.getValue(), (Integer) maxYSpinner.getValue());
         
         addUnaryConstraint(type, value, floatValue, range, posX, posY, getOrientations());         
     }
     updateActiveConstraintsList();
 }//GEN-LAST:event_addConstraintButtonActionPerformed
 
 
 private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
     dispose();
 }//GEN-LAST:event_cancelButtonActionPerformed
 
 private void constraintTypeListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_constraintTypeListActionPerformed
     String consName = (String) constraintTypeList.getSelectedItem();
     if (consName != null) {
         if (binaryConstraintRadio.isSelected())
             updateBinaryView(consName);
         else if (unaryConstraintRadio.isSelected())
             updateUnaryView();
     }
 }//GEN-LAST:event_constraintTypeListActionPerformed
 
 private void activeUnariesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_activeUnariesKeyPressed
     int index = activeUnaries.getSelectedIndex();
     if (index >= 0) {
         UnaryConstraint constraint = (UnaryConstraint) activeUnaries.getModel().getElementAt(index);
         switch (evt.getKeyCode()) {
             case KeyEvent.VK_DELETE:  // press delete
                 unaryConstraintController.remove(selectedId, constraint.getClass());
                 break;            
         }
         updateActiveConstraintsList();
    }   
 }//GEN-LAST:event_activeUnariesKeyPressed
 
 private void activeBinariesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_activeBinariesKeyPressed
     int index = activeBinaries.getSelectedIndex();
     if (index >= 0) {
        BinaryConstraintAssociation constraint = (BinaryConstraintAssociation) activeBinaries.getModel().
                 getElementAt(index);
         
         switch (evt.getKeyCode()) {
             case KeyEvent.VK_DELETE:  // press delete
                binaryConstraintController.removeConstraint(constraint.furniture1,
                        constraint.getConstraint().getClass());
                 break;            
         }
         updateActiveConstraintsList();
    }
    
 }//GEN-LAST:event_activeBinariesKeyPressed
 
 private void binaryConstraintRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_binaryConstraintRadioActionPerformed
     if (binaryConstraintRadio.isSelected()) {
         updateComboBox(constraintTypeList, binaryConstraintController.getAvailableConstraints());
         updateComboBox(relatableCombo, relatableFurniture);
     }
 }//GEN-LAST:event_binaryConstraintRadioActionPerformed
 
 private void unaryConstraintRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unaryConstraintRadioActionPerformed
     if (unaryConstraintRadio.isSelected()) {
         updateComboBox(constraintTypeList, unaryConstraintController.getAvailableConstraints());
         updateUnaryView();
     }
 }//GEN-LAST:event_unaryConstraintRadioActionPerformed
 
 private void relatableComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relatableComboActionPerformed
     boolean unarySelected = unaryConstraintRadio.isSelected();
     String type = (String) relatableCombo.getSelectedItem();
     if (unarySelected && type != null) {
         resetSpinners();
         if (type.equals("at")) {
             positionPanel.setVisible(false);
             prepareRangePanel("x:", "y:");
         }
         else if (type.equals("area")) {
             positionPanel.setVisible(true);
             rangePanel.setVisible(false);
         }
     }
 }//GEN-LAST:event_relatableComboActionPerformed
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JCheckBox ECheckBox;
     private javax.swing.JCheckBox NCheckBox;
     private javax.swing.JCheckBox SCheckBox;
     private javax.swing.JCheckBox WCheckBox;
     private javax.swing.JList activeBinaries;
     private javax.swing.JList activePlacement;
     private javax.swing.JList activeUnaries;
     private javax.swing.JButton addConstraintButton;
     private javax.swing.JRadioButton binaryConstraintRadio;
     private javax.swing.JButton cancelButton;
     private javax.swing.JLayeredPane constPropEditor;
     private javax.swing.ButtonGroup constraintMultiplicity;
     private javax.swing.JComboBox constraintTypeList;
     private javax.swing.JSpinner floatSpinner;
     private javax.swing.JSpinner integerSpinner;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPanel2;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JSpinner maxSpinner;
     private javax.swing.JSpinner maxXSpinner;
     private javax.swing.JSpinner maxYSpinner;
     private javax.swing.JSpinner minSpinner;
     private javax.swing.JSpinner minXSpinner;
     private javax.swing.JSpinner minYSpinner;
     private javax.swing.JPanel positionPanel;
     private javax.swing.JLabel propertyLabel;
     private javax.swing.JLabel rangeMaxLabel;
     private javax.swing.JLabel rangeMinLabel;
     private javax.swing.JPanel rangePanel;
     private javax.swing.JComboBox relatableCombo;
     private javax.swing.JLabel selectedElementLabel;
     private javax.swing.JRadioButton unaryConstraintRadio;
     private javax.swing.JPanel wallPanel;
     // End of variables declaration//GEN-END:variables
 
 
     private void prepareRangePanel(String left, String right) {
         rangeMinLabel.setText(left);
         rangeMaxLabel.setText(right);
         rangePanel.setVisible(true);
     }
     
     private void addBinaryConstraint(String otherId, String type, int dist) {
         if (type.equals("distance-max"))
             binaryConstraintController.addMaxDistanceConstraint(selectedId, otherId, dist);
         else if (type.equals("distance-min"))
             binaryConstraintController.addMinDistanceConstraint(selectedId, otherId, dist);
         else if (type.equals("facing-straight"))
             binaryConstraintController.addStraightFacingConstraint(selectedId, otherId);
         else if (type.equals("facing-partial"))
             binaryConstraintController.addPartialFacingConstraint(selectedId, otherId);
         
         // @TODO Add variable distance to facing constraints
     }
 
     private void addUnaryConstraint(String type, String value, float floatValue,
                                     Range range, Range posX, Range posY, Orientation[] orientations) {
         if (type.equals("color"))
             unaryConstraintController.addColorConstraint(selectedId, value);
         else if (type.equals("model"))
             unaryConstraintController.addModelConstraint(selectedId, value);
         else if (type.equals("orientation"))
             unaryConstraintController.addOrientationConstraint(selectedId, value);              
         else if (type.equals("material"))
             unaryConstraintController.addMaterialConstraint(selectedId, value);
         else if (type.equals("price"))
             unaryConstraintController.addPriceConstraint(selectedId, floatValue);
         else if (type.equals("width"))
             unaryConstraintController.addWidthConstraint(selectedId, range.min, range.max);
         else if (type.equals("depth"))
             unaryConstraintController.addDepthConstraint(selectedId, range.min, range.max);
         else if (type.equals("position")) {
             if (value.equals("area"))
                 unaryConstraintController.addPositionRangeConstraint(selectedId, posX.min, posY.min, posX.max,
                         posY.max);
             else if (value.equals("at"))
                 unaryConstraintController.addPositionAtConstraint(selectedId, range.min, range.max);
         }
         else if (type.equals("wall"))
             unaryConstraintController.addWallConstraint(selectedId, orientations);
     }
     
     private void propertyConstraintView(String label, Collection<JComponent> visibleComponents) {
         propertyLabel.setText(label);
         visibleComponents.add(propertyLabel);
         showOnly(visibleComponents);
     }
     
     private void positionConstraintView() {
         updateComboBox(relatableCombo, new String[]{"area", "at"});
         showOnly((Collection) Arrays.asList(constPropEditor, positionPanel, relatableCombo));
     }
     
     private void rangeConstraintView() {
         prepareRangePanel("min:", "max:");
         showOnly((Collection) Arrays.asList(constPropEditor,rangePanel));
     }
     
     private void wallsConstraintView() {
         relatableCombo.setVisible(false);
         showOnly((Collection) Arrays.asList(constPropEditor, wallPanel));
     }
     
     private void simpleUnaryConstraintView(Object[] objects) {
         updateComboBox(relatableCombo, objects);
         showOnly((Collection) Arrays.asList(relatableCombo));
     }
     
     private void simpleUnaryConstraintView(Collection<? extends Object> collection) {
         simpleUnaryConstraintView(collection.toArray());
     }
     
     private void updateUnaryView() {
         String type = (String) constraintTypeList.getSelectedItem();
         if (type != null) {
             String typeName = getTypeName(selectedId);
             if (type.equals("color"))
                 simpleUnaryConstraintView(CoolColor.getNames());
             else if (type.equals("model"))
                 simpleUnaryConstraintView(furnitureModelController.getFurnitureModelNames(typeName));
             else if (type.equals("orientation"))
                 simpleUnaryConstraintView(Orientation.values());
             else if (type.equals("material"))
                 simpleUnaryConstraintView(Material.values());
             else if (type.equals("price")) 
                 propertyConstraintView("Price:", new ArrayList(Arrays.asList(constPropEditor, floatSpinner)));
             else if (type.equals("depth")) 
                 rangeConstraintView();
             else if (type.equals("width")) 
                 rangeConstraintView();
             else if (type.equals("wall"))
                 wallsConstraintView();
             else if (type.equals("position"))
                 positionConstraintView();
         }
     }
 
     private void updateBinaryView(String consName) {
         if (consName.startsWith("dist"))
             propertyConstraintView("Distance:", new ArrayList(Arrays.asList(relatableCombo, constPropEditor,
                     integerSpinner)));
         else
             showOnly((Collection) Arrays.asList(relatableCombo));
     }
 
     private void resetSpinners() {
        JSpinner[] spinners = {minSpinner, maxSpinner, minXSpinner, maxXSpinner, minYSpinner, maxYSpinner};
        for (JSpinner spinner: spinners)  {
            spinner.getModel().setValue(0);
        }
                        
     }
 
 }
