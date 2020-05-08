 package com.customfit.ctg.view.recipes;
 
 import com.customfit.ctg.controller.*;
 import com.customfit.ctg.model.*;
 import com.customfit.ctg.view.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 /**
  * This is the main panel for managing the recipes that are available
  * to select from.
  *
  * @author Ryan Spoon
  */
 public class EditRecipePanel extends CreateEditPanel {
 
     /**
      * Holds the panel that was open right before this panel was instantiated.
      */
     private SubPanel previousPanel;
     
     /**
      * Holds the original recipe the way it was.
      */
     private Recipe recipe;
 
     /** Creates new form OldRecipeListPanel */
     public EditRecipePanel(CreateEditMode createEditMode) {
         initComponents();
         //setup edit mode
         super.setCreateEditMode(createEditMode);
         //customize form for edit mode
         if (this.getCreateEditMode() == CreateEditMode.CREATE)
             this.jLabelTitle.setText("Create a New Recipe");
         else if (this.getCreateEditMode() == CreateEditMode.EDIT)
             this.jLabelTitle.setText("Edit Recipe");
         //save last panel in view
         this.previousPanel = Application.getMainFrame().getPanel();
         //setup the me-menu in the right-top corner
         this.jComboBoxMeMenu.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"Account: " + UserManagement.getCurrentUser().getName(), "Edit User", "Logout" }));
         //clear out the built-in combo box for units
         ArrayList<String> allUnits = new ArrayList<String>();
         allUnits.add("");
         allUnits.addAll(Arrays.asList(Measurement.getAllMeasurementUnits()));
         this.jComboBoxServingSizeUnit.setModel(new javax.swing.DefaultComboBoxModel(allUnits.toArray(new String[] {})));
         //clear out the nutrition info box for reflection
         DefaultTableModel nutritionTableModel = (DefaultTableModel) this.jTableNutritionFacts.getModel();
         for (String nutrient: NutritionFacts.getAllValidNutrients())
         {
             nutritionTableModel.addRow(new Object[] { nutrient , });
         }
         //listen for star rating events
         this.starRatingPanel.addMouseListener(new MouseListener() {
 
             @Override
             public void mouseClicked(MouseEvent e) {
                 validateForm();
                 System.out.println("mouse clicked hit.");
             }
 
             @Override
             public void mousePressed(MouseEvent e) {
 //                throw new UnsupportedOperationException("Not supported yet.");
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
 //                throw new UnsupportedOperationException("Not supported yet.");
             }
 
             @Override
             public void mouseEntered(MouseEvent e) {
 //                throw new UnsupportedOperationException("Not supported yet.");
             }
 
             @Override
             public void mouseExited(MouseEvent e) {
 //                throw new UnsupportedOperationException("Not supported yet.");
             }
         });
         //listen for nutrition table events
         nutritionTableModel.addTableModelListener(new TableModelListener() {
 
             @Override
             public void tableChanged(TableModelEvent e) {
                 //update unit on nutrition table
                 DefaultTableModel nutritionTableModel = (DefaultTableModel) jTableNutritionFacts.getModel();
                 for (int row = e.getFirstRow(); row <= e.getLastRow(); row++)
                 {
                     if (nutritionTableModel.getValueAt(row, e.getColumn()) != null && !((String)nutritionTableModel.getValueAt(row, e.getColumn())).isEmpty())
                     {
                         Measurement measurement = new Measurement((String)nutritionTableModel.getValueAt(row, e.getColumn()));
                         measurement.setUnit(NutritionFacts.getUnitForNutrient((String)nutritionTableModel.getValueAt(row, 0)));
                         if (!measurement.toString().equals((String)nutritionTableModel.getValueAt(row, e.getColumn())))
                             nutritionTableModel.setValueAt(measurement.toString(), row, e.getColumn());
                         if (measurement.getQuantity() <= 0.0)
                             nutritionTableModel.setValueAt("", row, e.getColumn());
                     }
                 }
                 validateForm();
             }
         });
         //listen for ingredient table events
         DefaultTableModel ingredientModel = (DefaultTableModel)jTableIngredients.getModel();
         ingredientModel.addTableModelListener(new TableModelListener() {
 
             @Override
             public void tableChanged(TableModelEvent e) {
                 DefaultTableModel membersModel = (DefaultTableModel) jTableIngredients.getModel();
                 //if column is Amount
                 if (e.getColumn() == 0)
                 {
                     //then make sure we insert the right unit on each one
                     for (int row = e.getFirstRow(); row <= e.getLastRow(); row++)
                     {
                         Measurement measurement = new Measurement((String)membersModel.getValueAt(row, e.getColumn()));
                         if (!measurement.toString().equals((String)membersModel.getValueAt(row, e.getColumn())))
                             membersModel.setValueAt(measurement.toString(), row, e.getColumn());
                     }
                 }
 //                //if column is Member
 //                else if (e.getColumn() == 1)
 //                {
 //                    //check for name identical to this one
 //                    for (int row = e.getFirstRow(); row <= e.getLastRow(); row++)
 //                        if (((String)membersModel.getValueAt(row, e.getColumn())).trim().equals(jTextFieldUser.getText()) ||
 //                                ((String)membersModel.getValueAt(row, e.getColumn())).trim().equals("") ||
 //                                    (getCreateEditMode() == CreateEditMode.EDIT &&
 //                                    ((String)membersModel.getValueAt(row, e.getColumn())).trim().equals(user.getName())))
 //                            membersModel.setValueAt("Member's Name", row, e.getColumn());
 //                }
                 validateForm();
             }
         });
 
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jButtonSave = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
         lblName = new javax.swing.JLabel();
         lblName1 = new javax.swing.JLabel();
         jTextName = new javax.swing.JTextField();
         scrollPaneNutrition = new javax.swing.JScrollPane();
         jTableNutritionFacts = new javax.swing.JTable();
         jLabelTitle = new javax.swing.JLabel();
         jComboBoxMeMenu = new javax.swing.JComboBox();
         jPanel1 = new javax.swing.JPanel();
         scrollPaneIngedients = new javax.swing.JScrollPane();
         jTableIngredients = new javax.swing.JTable();
         jButtonAddIngredient = new javax.swing.JButton();
         jButtonRemoveIngredient = new javax.swing.JButton();
         starRatingPanel = new com.customfit.ctg.view.StarRatingPanel();
         jSpinnerMakes = new javax.swing.JSpinner();
         lblName2 = new javax.swing.JLabel();
         lblName3 = new javax.swing.JLabel();
         jTextFieldServingSize = new javax.swing.JTextField();
         jComboBoxServingSizeUnit = new javax.swing.JComboBox();
         jPanel2 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTextAreaInstructions = new javax.swing.JTextArea();
 
         jButtonSave.setText("Save");
         jButtonSave.setEnabled(false);
         jButtonSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonSaveActionPerformed(evt);
             }
         });
 
         btnCancel.setText("Cancel");
         btnCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCancelActionPerformed(evt);
             }
         });
 
         lblName.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         lblName.setText("Name: ");
 
         lblName1.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblName1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         lblName1.setText("Rating: ");
 
         jTextName.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextNameKeyReleased(evt);
             }
         });
 
         scrollPaneNutrition.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Nutrition Facts", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
         scrollPaneNutrition.setFont(new java.awt.Font("Tahoma", 1, 11));
 
         jTableNutritionFacts.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Name", "Amount"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.String.class
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
         });
         jTableNutritionFacts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jTableNutritionFacts.getTableHeader().setReorderingAllowed(false);
         scrollPaneNutrition.setViewportView(jTableNutritionFacts);
         jTableNutritionFacts.getColumnModel().getColumn(0).setMinWidth(120);
         jTableNutritionFacts.getColumnModel().getColumn(0).setPreferredWidth(120);
         jTableNutritionFacts.getColumnModel().getColumn(0).setMaxWidth(120);
 
         jLabelTitle.setFont(new java.awt.Font("Tahoma", 3, 18));
         jLabelTitle.setText("Edit Recipe");
 
         jComboBoxMeMenu.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Me Menu", "Edit User", "Logout" }));
         jComboBoxMeMenu.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jComboBoxMeMenuActionPerformed(evt);
             }
         });
 
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Ingredients", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
 
         jTableIngredients.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Amount", "Ingredient"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.String.class
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
         });
         jTableIngredients.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jTableIngredients.getTableHeader().setReorderingAllowed(false);
         scrollPaneIngedients.setViewportView(jTableIngredients);
         jTableIngredients.getColumnModel().getColumn(0).setMinWidth(45);
         jTableIngredients.getColumnModel().getColumn(0).setPreferredWidth(55);
         jTableIngredients.getColumnModel().getColumn(0).setMaxWidth(60);
 
         jButtonAddIngredient.setIcon(new javax.swing.ImageIcon(getClass().getResource("/art/export/add-item.png"))); // NOI18N
         jButtonAddIngredient.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonAddIngredientActionPerformed(evt);
             }
         });
 
         jButtonRemoveIngredient.setIcon(new javax.swing.ImageIcon(getClass().getResource("/art/export/subtract-item.png"))); // NOI18N
         jButtonRemoveIngredient.setDefaultCapable(false);
         jButtonRemoveIngredient.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonRemoveIngredientActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(2, 2, 2)
                 .addComponent(jButtonAddIngredient, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButtonRemoveIngredient, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(222, Short.MAX_VALUE))
             .addComponent(scrollPaneIngedients, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jButtonRemoveIngredient, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jButtonAddIngredient, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(scrollPaneIngedients, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
         );
 
         starRatingPanel.setScale(5);
 
         javax.swing.GroupLayout starRatingPanelLayout = new javax.swing.GroupLayout(starRatingPanel);
         starRatingPanel.setLayout(starRatingPanelLayout);
         starRatingPanelLayout.setHorizontalGroup(
             starRatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 100, Short.MAX_VALUE)
         );
         starRatingPanelLayout.setVerticalGroup(
             starRatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 20, Short.MAX_VALUE)
         );
 
         jSpinnerMakes.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(0.0d), null, Double.valueOf(0.25d)));
         jSpinnerMakes.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSpinnerMakesStateChanged(evt);
             }
         });
 
         lblName2.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblName2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         lblName2.setText("Makes:");
 
         lblName3.setFont(new java.awt.Font("Tahoma", 1, 11));
         lblName3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         lblName3.setText("Serving Size:");
 
         jTextFieldServingSize.setText("1.0");
         jTextFieldServingSize.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextFieldServingSizeKeyReleased(evt);
             }
         });
 
         jComboBoxServingSizeUnit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         jComboBoxServingSizeUnit.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 jComboBoxServingSizeUnitItemStateChanged(evt);
             }
         });
 
         jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Instructions", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
 
         jTextAreaInstructions.setColumns(20);
         jTextAreaInstructions.setRows(5);
         jTextAreaInstructions.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextAreaInstructionsKeyReleased(evt);
             }
         });
         jScrollPane1.setViewportView(jTextAreaInstructions);
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jLabelTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                         .addGap(1, 1, 1)
                         .addComponent(jComboBoxMeMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                     .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jTextName))
                                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                     .addComponent(lblName3, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jTextFieldServingSize, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jComboBoxServingSizeUnit, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                     .addComponent(lblName1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(starRatingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(lblName2, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                     .addComponent(jSpinnerMakes, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(scrollPaneNutrition, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jButtonSave)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnCancel)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabelTitle)
                     .addComponent(jComboBoxMeMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jTextName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addGap(59, 59, 59))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addGap(28, 28, 28)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(lblName1)
                                     .addComponent(starRatingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                         .addComponent(lblName2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addComponent(jSpinnerMakes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(jComboBoxServingSizeUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(lblName3, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jTextFieldServingSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(scrollPaneNutrition, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                         .addGap(1, 1, 1)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButtonSave)
                     .addComponent(btnCancel))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void jComboBoxMeMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMeMenuActionPerformed
         // process me menu
 
         //if user hit Edit User
         if (((String)this.jComboBoxMeMenu.getSelectedItem()).equals("Edit User"))
             //then tell the controller about it
             UserManagement.editRegistration(UserManagement.getCurrentUser());
         //if user hit Edit User
         else if (((String)this.jComboBoxMeMenu.getSelectedItem()).equals("Logout"))
             //ask controller for logout
             UserManagement.logout();
         //and no matter what, be sure to reset this combo box back to index 0
         this.jComboBoxMeMenu.setSelectedIndex(0);
 }//GEN-LAST:event_jComboBoxMeMenuActionPerformed
 
     private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
         //if its a new recipe
         if (this.getCreateEditMode() == CreateEditMode.CREATE)
             //then save it and go back to the last panel
             RecipeManagement.createRecipeAndGoBack(this.getRecipe());
         //if its an existing recipe
         else if (this.getCreateEditMode() == CreateEditMode.EDIT)
             RecipeManagement.updateRecipeAndGoBack(this.recipe.getName(), this.getRecipe());
     }//GEN-LAST:event_jButtonSaveActionPerformed
 
     private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
         //go back to the last panel
         Application.getMainFrame().setPanel(this.previousPanel);
     }//GEN-LAST:event_btnCancelActionPerformed
 
     private void jButtonRemoveIngredientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveIngredientActionPerformed
         removeIngredient();
     }//GEN-LAST:event_jButtonRemoveIngredientActionPerformed
 
     private void jButtonAddIngredientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddIngredientActionPerformed
         DefaultTableModel dtm = (DefaultTableModel)jTableIngredients.getModel();
         dtm.addRow(new Object [] {null, null, null});
         jTableIngredients.setModel(dtm);
     }//GEN-LAST:event_jButtonAddIngredientActionPerformed
 
     private void jTextNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextNameKeyReleased
         this.validateForm();
     }//GEN-LAST:event_jTextNameKeyReleased
 
     private void jTextFieldServingSizeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldServingSizeKeyReleased
         this.validateForm();
     }//GEN-LAST:event_jTextFieldServingSizeKeyReleased
 
     private void jTextAreaInstructionsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextAreaInstructionsKeyReleased
         this.validateForm();
     }//GEN-LAST:event_jTextAreaInstructionsKeyReleased
 
     private void jComboBoxServingSizeUnitItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxServingSizeUnitItemStateChanged
         this.validateForm();
     }//GEN-LAST:event_jComboBoxServingSizeUnitItemStateChanged
 
     private void jSpinnerMakesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerMakesStateChanged
         this.validateForm();
     }//GEN-LAST:event_jSpinnerMakesStateChanged
 
     private void removeIngredient() {
         int currentRow = jTableIngredients.getSelectedRow();
         if (currentRow != -1) {
             DefaultTableModel dtm = (DefaultTableModel)jTableIngredients.getModel();
             dtm.removeRow(currentRow);
 
             jTableIngredients.setModel(dtm);
             if (currentRow < jTableIngredients.getRowCount())
                 jTableIngredients.setRowSelectionInterval(currentRow, currentRow);
         }
     }
 
     /**
      * Extends SubPanel functionality by returning the title as being the
      * same as on the panel itself.
      * @return The title in the panel.
      */
     @Override
     public String getTitle()
     {
         //grab title from on the panel
         return this.jLabelTitle.getText();
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCancel;
     private javax.swing.JButton jButtonAddIngredient;
     private javax.swing.JButton jButtonRemoveIngredient;
     private javax.swing.JButton jButtonSave;
     private javax.swing.JComboBox jComboBoxMeMenu;
     private javax.swing.JComboBox jComboBoxServingSizeUnit;
     private javax.swing.JLabel jLabelTitle;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JSpinner jSpinnerMakes;
     private javax.swing.JTable jTableIngredients;
     private javax.swing.JTable jTableNutritionFacts;
     private javax.swing.JTextArea jTextAreaInstructions;
     private javax.swing.JTextField jTextFieldServingSize;
     private javax.swing.JTextField jTextName;
     private javax.swing.JLabel lblName;
     private javax.swing.JLabel lblName1;
     private javax.swing.JLabel lblName2;
     private javax.swing.JLabel lblName3;
     private javax.swing.JScrollPane scrollPaneIngedients;
     private javax.swing.JScrollPane scrollPaneNutrition;
     private com.customfit.ctg.view.StarRatingPanel starRatingPanel;
     // End of variables declaration//GEN-END:variables
 
     /**
      * Sets recipe to edit in view.
      * 
      * @param recipe Recipe to edit.
      */
     public void setRecipe(Recipe recipe)
     {
         this.recipe = recipe;
         this.jTextName.setText(recipe.getName());
         this.starRatingPanel.setRating(recipe.getRating());
         this.jSpinnerMakes.setValue(recipe.getServings());
         this.jTextFieldServingSize.setText(Double.valueOf(recipe.getServingSize().getQuantity()).toString());
         this.jComboBoxServingSizeUnit.setSelectedItem(recipe.getServingSize().getUnit());
         for (RecipeIngredient ingredient: recipe.getIngredients())
         {
             DefaultTableModel dtm = (DefaultTableModel)this.jTableIngredients.getModel();
             Object[] objArray = new Object[]
                     {
                         ingredient.getAmount().toString(),
                         ingredient.getName()
                     };
             dtm.addRow(objArray);
         }
         this.jTextAreaInstructions.setText(recipe.getInstructions());
         for (int nutritionRow = 0; nutritionRow < this.jTableNutritionFacts.getModel().getRowCount(); nutritionRow++)
         {
             String nutrient = (String)this.jTableNutritionFacts.getModel().getValueAt(nutritionRow, 0);
             Measurement measurement = recipe.getNutritionInformation().getNutrient(nutrient);
             if (measurement != null && measurement.getQuantity() > 0)
                 this.jTableNutritionFacts.getModel().setValueAt(measurement.toString(), nutritionRow, 1);
         }
     }
     
     /**
      * Builds Recipe object from view inputs.
      */
     public Recipe getRecipe()
     {
         Recipe recipe = new Recipe();
         recipe.setName(this.jTextName.getText().trim());
         recipe.setRating(this.starRatingPanel.getRating());
        recipe.setServings((Double)this.jSpinnerMakes.getValue());
         if (this.jTextFieldServingSize.getText().isEmpty()) this.jTextFieldServingSize.setText("1.0");
         recipe.getServingSize().setQuantity(Double.parseDouble(this.jTextFieldServingSize.getText()));
         recipe.getServingSize().setUnit((String)this.jComboBoxServingSizeUnit.getSelectedItem());
         for (int ingredientRow = 0; ingredientRow < this.jTableIngredients.getModel().getRowCount(); ingredientRow++)
         {
             String amount = (String)this.jTableIngredients.getModel().getValueAt(ingredientRow, 0);
             String ingredient = (String)this.jTableIngredients.getModel().getValueAt(ingredientRow, 1);
             //add ingredient to recipe
             recipe.getIngredients().add(new RecipeIngredient(ingredient, amount));
         }
         
         recipe.setInstructions(this.jTextAreaInstructions.getText());
         for (int nutritionRow = 0; nutritionRow < this.jTableNutritionFacts.getModel().getRowCount(); nutritionRow++)
         {
             String nutrient = (String)this.jTableNutritionFacts.getModel().getValueAt(nutritionRow, 0);
             if (this.jTableNutritionFacts.getModel().getValueAt(nutritionRow, 1) != null)
                 recipe.getNutritionInformation().setNutrient(nutrient, new Measurement((String)this.jTableNutritionFacts.getModel().getValueAt(nutritionRow, 1)).getQuantity());
         }
         return recipe;
     }
     
     /**
      * Gets the last panel that was open before this one was opened.
      * @return 
      */
     public SubPanel getPreviousPanel() {
         return previousPanel;
     }
     
     /**
      * Makes sure we are good to save before actually enabling the save button.
      */
     private void validateForm()
     {
         boolean disableSaves = false;
         if (this.jTextName.getText().trim().length() < 5)
             disableSaves = true;
         else if (this.jTextFieldServingSize.getText().trim().isEmpty())
             disableSaves = true;
         else if (this.jTableIngredients.getModel().getRowCount() <= 0)
             disableSaves = true;
         else if (this.jTextAreaInstructions.getText().trim().isEmpty())
             disableSaves = true;
         else if (this.getCreateEditMode() == CreateEditMode.EDIT && this.recipe.equals(this.getRecipe()))
             disableSaves = true;
         this.jButtonSave.setEnabled(!disableSaves);
     }
 }
