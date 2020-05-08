 package com.physion.ovation.gui.ebuilder;
 
 import java.util.EventObject;
 import java.util.ArrayList;
 import java.awt.Color;
 import java.awt.Insets;
 import java.awt.Component;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableCellEditor;
 import javax.swing.event.CellEditorListener;
 import javax.swing.DefaultCellEditor;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.JLabel;
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.text.BadLocationException;
 import javax.swing.BorderFactory;
 
 import com.physion.ovation.gui.ebuilder.datamodel.RowData;
 import com.physion.ovation.gui.ebuilder.datamodel.DataModel;
 import com.physion.ovation.gui.ebuilder.datamodel.CollectionOperator;
 import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
 import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;
 import com.physion.ovation.gui.ebuilder.datatypes.Type;
 import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
 
 
 /**
  * This class creates all the widgets that are used to render (draw)
  * a row and edit a row.  A JTable uses the same row renderer like a
  * "rubber stamp" to draw each row.  In a similar way, the same
  * row editor is reused to edit whichever row the user is currently
  * editing.
  *
  * We create all the widgets we will need in our constructor, and
  * thereafter we simply add or remove them from the JPanel based on
  * the RowData value for that row.
  *
  * TODO:  Pull parts of this code out into utility methods.
  */
 class ExpressionCellRenderer
     extends JPanel
     implements TableCellRenderer, TableCellEditor,
     ActionListener, ItemListener, DocumentListener {
 
     /**
      * TODO: Change to an ArrayList of these to allow an infinite
      * number.
      */
     private static final int MAX_NUM_COMBOBOXES = 20;
 
     private int modelRow; // Temp hack.
 
     //private JLabel label;
     private JButton deleteButton;
     private JButton createCompoundRowButton;
     private JButton createAttributeRowButton;
     private JComboBox[] comboBoxes = new JComboBox[MAX_NUM_COMBOBOXES];
     private JTextField textField;
     private JLabel indentWidget;
     private JPanel buttonPanel;
     private JLabel ofTheFollowingLabel;
 
     private ExpressionTable table;
 
 
     /**
      * Create whatever components this renderer will need.
      * Other methods add or remove the components depending on
      * what RowData this row is displaying/editing.
      */
     public ExpressionCellRenderer() {
 
         setBorder(BorderFactory.createEmptyBorder(3,10,3,10));
 
         setOpaque(true);
 
         GridBagLayout layout = new GridBagLayout();
         setLayout(layout);
 
         /**
          * TODO:  Perhaps change this to a JPanel that has a minimum size?
          */
         indentWidget = new JLabel();
 
         //label = new JLabel();
         //label.setOpaque(true);
         ofTheFollowingLabel = new JLabel(" of the following");
 
         deleteButton = new JButton("-");
         deleteButton.addActionListener(this);
 
         createAttributeRowButton = new JButton("+");
         createAttributeRowButton.addActionListener(this);
 
         createCompoundRowButton = new JButton("++");
         createCompoundRowButton.addActionListener(this);
 
         for (int count = 0; count < MAX_NUM_COMBOBOXES; count++) {
             JComboBox comboBox = new JComboBox();
             comboBoxes[count] = comboBox;
             comboBox.setEditable(false);
             comboBox.setMaximumRowCount(20);  // Number of items before scroll.
             comboBox.addItemListener(this);
             //comboBox.addActionListener(this);
             comboBox.setBorder(BorderFactory.createEmptyBorder(0,7,0,0));
         }
 
         textField = new JTextField();
         textField.getDocument().addDocumentListener(this);
 
         buttonPanel = new JPanel(new GridBagLayout());
         GridBagConstraints gc;
         gc = new GridBagConstraints();
         gc.gridx = 0;
         gc.fill = GridBagConstraints.VERTICAL;
         gc.insets = new Insets(0,7,0,0);
         buttonPanel.add(createAttributeRowButton, gc);
 
         gc = new GridBagConstraints();
         gc.gridx = 1;
         gc.fill = GridBagConstraints.VERTICAL;
         gc.insets = new Insets(0,7,0,0);
         buttonPanel.add(createCompoundRowButton, gc);
 
         gc = new GridBagConstraints();
         gc.gridx = 2;
         gc.fill = GridBagConstraints.VERTICAL;
         gc.insets = new Insets(0,7,0,0);
         buttonPanel.add(deleteButton, gc);
     }
 
 
     /**
      * Layout whatever components the RowData for this row needs.
      * This method places the assorted comboBoxes, text fields, labels,
      * and buttons in a panel using the GridBagLayout layout manager.
      *
      * @param rowData - The RowData object this row will display and/or edit.
      *
      * @param row - Row index of this row.  The first row, (the root row),
      * is row 0.
      */
     private void layoutNeededComponents(RowData rowData, int row) {
 
         GridBagConstraints gc;
 
 
         /**
          * First, remove whatever components used to be in the
          * row.
          */
         removeAll();
 
         /**
          * The widget we use to indent rows is always the first/leftmost
          * widget.
          */
 
         int gridx = 0;
 
         gc = new GridBagConstraints();
         gc.gridx = gridx++;
         add(indentWidget, gc);
         if (rowData != null)
             indentWidget.setText(rowData.getIndentString());
         else
             indentWidget.setText("");
 
         /**
          * Now add the components that are needed.
          */
 
         System.out.print("Laying out components for row "+row+",");
 
         /**
          * If a row is filled with widgets that do
          * not stretch, we need to have the cell that
          * holds the buttons on the right side of the
          * row use the empty space.
          *
          * This gets set to true if some other widget
          * uses the extra space.  E.g. the textField
          * will use the extra space if it exists in this row.
          */
         boolean someWidgetFillingEmptySpace = false;
 
         if (row == 0) {
 
             System.out.println(" which is the root row.");
 
             /**
              * The first/topmost row always has the
              * Class Under Qualification comboBox and the
              * Collection Operator comboBox, (and only those comboBoxes).
              */
 
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             add(comboBoxes[0], gc);
 
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             add(comboBoxes[1], gc);
 
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             gc.weightx = 1;
             someWidgetFillingEmptySpace = true;
             gc.anchor = GridBagConstraints.WEST;
             //add(new JLabel(" of the following"), gc);
             add(ofTheFollowingLabel, gc);
         }
         else if (rowData.isSimpleCompoundRow()) {
 
             /**
              * A "simple" compound row is a row that
              * only contains a Collection Operator comboBox,
              * and the -, +, ++ buttons on the right side.
              */
             System.out.println(" which is a simple Compound Row.");
 
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             add(comboBoxes[0], gc);
 
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             gc.weightx = 1;
             someWidgetFillingEmptySpace = true;
             gc.anchor = GridBagConstraints.WEST;
             add(ofTheFollowingLabel, gc);
         }
         else {
             /**
              * This is an Attribute Row that contains one
              * or more comboBoxes for selecting attributes,
              * possibly a Collection Operator comboBox,
              * possibly a true/false comboBox, possibly
              * an Attribute Operator (==, !=, <, >, ...) comboBox,
              * or any number of other widgets.  It also contains
              * the +, ++, - buttons.
              */
             System.out.println(" which is an Attribute Row.");
 
             ArrayList<Attribute> attributes = rowData.getAttributePath();
             System.out.println("Add comboboxes for: "+rowData.getRowString());
 
             /**
              * We are an Attribute Row, so the widgets we contain
              * are based on the values in this row's RowData object.
              *
              * Add one comboBox for every Attribute on this row's
              * attributePath.
              */
             int comboBoxIndex = 0;
             for (Attribute attribute : attributes) {
 
                 //System.out.println("Adding comboBox at gridx "+gridx);
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 add(comboBoxes[comboBoxIndex++], gc);
             }
 
             /**
              * We have inserted comboBoxes for every Attribute on this
              * RowData's attributePath.  No insert any other widgets
              * that are needed.
              */
 
             Attribute rightmostAttribute = rowData.getChildmostAttribute();
             if (!rightmostAttribute.isPrimitive() &&
                 !rightmostAttribute.equals(Attribute.SELECT_ATTRIBUTE) &&
                 !rightmostAttribute.equals(Attribute.IS_NULL) &&
                 !rightmostAttribute.equals(Attribute.IS_NOT_NULL) &&
                 (rowData.getCollectionOperator() == null)) {
                 /**
                  * The rightmost Attribute is a class, as opposed
                  * to a "primitive" type such as int, float, string,
                  * so we need to display another comboBox to its right
                  * that the user can use to choose an Attribute of
                  * that class or choose a special item such as "is null",
                  * "is not null", "Any Property", "My Property".
                  */
                 System.out.println("Adding Select Attribute comboBox at gridx "+
                     gridx);
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 add(comboBoxes[comboBoxIndex++], gc);
             }
             else if (rightmostAttribute.isPrimitive()) {
                 /**
                  * The rightmost Attribute is a primitive Attribute
                  * such as an int, float, string, so now create a
                  * comboBox that will hold operators such
                  * as ==, !=, >, is true.
                  */
                 System.out.println("Adding operator comboBox at gridx "+gridx);
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 add(comboBoxes[comboBoxIndex++], gc);
 
                 if (rightmostAttribute.getType() != Type.BOOLEAN) {
                     /**
                      * Create a text field into which the user can enter a
                      * value of some sort.
                      */
                     System.out.println("Adding text field at gridx "+gridx);
                     gc = new GridBagConstraints();
                     gc.gridx = gridx++;
                     gc.weightx = 1;
                     someWidgetFillingEmptySpace = true;
                     gc.fill = GridBagConstraints.BOTH;
                     gc.insets = new Insets(0,7,0,0);
                     //textField.setText("<Enter Value>");
                     //System.out.println("Calling setText1");
                     //textField.setText("");
                     add(textField, gc);
                 }
             }
             else if (rowData.getCollectionOperator() ==
                      CollectionOperator.COUNT) {
 
                 /**
                  * This row says something like:
                  *
                  *      epochGroups.epochs Count == 5
                  */
 
                 /** 
                  * Add comboBox for the Collection Operator.
                  */
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 add(comboBoxes[comboBoxIndex++], gc);
 
                 /** 
                  * Add comboBox for the Attribute Operator.
                  */
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 add(comboBoxes[comboBoxIndex++], gc);
 
                 /** 
                  * Add count text field.
                  *
                  * TODO: This should be a clicker of some sort so
                  * the user cannot enter an illegal value?
                  */
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 gc.weightx = 1;
                 someWidgetFillingEmptySpace = true;
                 gc.fill = GridBagConstraints.BOTH;
                 gc.insets = new Insets(0,7,0,0);
                 //textField.setText("<Enter Value>");
                 //System.out.println("Calling setText2");
                 //textField.setText("");
                 add(textField, gc);
             }
             else if (rowData.getCollectionOperator() != null) {
 
                 /**
                  * This row says something like:
                  *
                  *      epochGroups.epochs Any of the following
                  */
 
                 /** 
                  * Add comboBox for the Collection Operator.
                  */
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 add(comboBoxes[comboBoxIndex++], gc);
             }
             else {
                 /**
                  * The last Attribute on the right is a primitive
                  * or it is the special "Select Attribute" Attribute.
                  * So we don't need any more comboBoxes to the right
                  * of the last one in this row.
                  */
             }
 
             //if ((rowData.getCollectionOperator() != null) && 
             if (rowData.isCompoundRow()) {
 
                 gc = new GridBagConstraints();
                 gc.gridx = gridx++;
                 gc.weightx = 1;
                 someWidgetFillingEmptySpace = true;
                 gc.anchor = GridBagConstraints.WEST;
                 add(ofTheFollowingLabel, gc);
             }
 
             /**
              * Use a JLabel until such time as I have implemented
              * the other components for this RowData type.
              */
             /*
             System.out.println("Adding label at gridx "+gridx);
             gc = new GridBagConstraints();
             gc.gridx = gridx++;
             gc.weightx = 1;
             someWidgetFillingEmptySpace = true;
             gc.anchor = GridBagConstraints.WEST;
             add(label, gc);
             */
         }
 
         /**
          * Add the panel that holds the -/+/++ buttons to
          * the far right side of this row.
          * If there is no other widget that will fill
          * the extra space in the row, tell the GridBagLayout
          * manager that the buttonPanel will do it.
          */
         gc = new GridBagConstraints();
         gc.gridx = gridx++;
         gc.anchor = GridBagConstraints.EAST;
         if (someWidgetFillingEmptySpace == false)
             gc.weightx = 1;
         add(buttonPanel, gc);
     }
 
 
     /**
      * This method returns the Component that the JTable should
      * use to draw the specified cell in the table.
      * This method is called by the JTable when it wants to draw
      * a cell.  (In our case, because we only have one column,
      * you can think of this as drawing a row.)
      */
     @Override
     public Component getTableCellRendererComponent(JTable table,
                                                    Object value,
                                                    boolean isSelected,
                                                    boolean cellHasFocus,
                                                    int row,
                                                    int column) {
 
         /**
          * Cast the generic value Object that is passed in to
          * be a RowData object.
          */
         RowData rowData = (RowData)value;
 
         /**
          * Add/Remove whatever GUI components this row should have
          * based on the data it will be displaying.
          */
         layoutNeededComponents(rowData, row);
 
         /**
          * Now set the values of the components in this row.
          */
 
         String stringValue;
 
         /**
          * Temporarily using a JLabel until we implement all the
          * other components in the row.
          */
         /*
         if (rowData != null) {
             stringValue = rowData.getIndentString()+rowData.getRowString();
         }
         else {
             stringValue = "";
         }
         label.setText(stringValue);
         */
 
         this.table = (ExpressionTable)table;
 
         Color background;
         Color foreground;
 
         // check if this cell represents the current DnD drop location
         JTable.DropLocation dropLocation = table.getDropLocation();
         if (dropLocation != null
                 && !dropLocation.isInsertRow()
                 && dropLocation.getRow() == row) {
 
             background = Color.BLUE;
             foreground = Color.WHITE;
 
         // check if this cell is selected
         } else if (isSelected) {
 
             background = Color.RED;
             foreground = Color.WHITE;
 
         // unselected, and not the DnD drop location
         } else {
             background = Color.WHITE;
             foreground = Color.BLACK;
         };
 /*
         if (isSelected) {
             background = Color.RED;
             foreground = Color.WHITE;
         }
         else {
             background = Color.WHITE;
             foreground = Color.BLACK;
         }
 */
 
         /**
          * Enable/disable the +,++,- buttons.
          */
 
         /**
          * The very first row cannot be deleted.
          * All other rows can always be deleted.
          */
         if (row == 0) {
             deleteButton.setEnabled(false);
         }
         else {
             deleteButton.setEnabled(true);
         }
 
         /**
          * See if this row can have child rows.
          * Based on that, enable/disable the +, ++ buttons.
          */
         if ((rowData != null) && rowData.isCompoundRow()) {
             createCompoundRowButton.setEnabled(true);
             createAttributeRowButton.setEnabled(true);
         }
         else {
             createCompoundRowButton.setEnabled(false);
             createAttributeRowButton.setEnabled(false);
         }
 
         /**
          * TODO: The setting of the models and selected values in the
          * attributePath comboboxes is the same for all row types,
          * so combine all that code into one location.  Only the
          * operators and value setting code is different.
          */
 
         /**
          * Initialize the comboBoxes for this row.
          */
         System.out.println("Setting the model for row "+row);
         modelRow = row;
         if (row == 0) {
 
             /**
              * This is the first row, so it has two comboBoxes.
              * The leftmost comboBox contains the list of possible choices
              * for the Class Under Qualification.  The comboBox on the
              * right contains the Any/All/None CollectionOperator.
              *
              * TODO:  Create the comboBox models only once and reuse them?
              * Create a cache of them?
              */
             
             ClassDescription[] values =
                 DataModel.getInstance().getPossibleCUQs().
                 toArray(new ClassDescription[0]);
 
             //DefaultComboBoxModel model = new DefaultComboBoxModel(values);
             //comboBoxes[0].setModel(model);
             setComboBoxModel(comboBoxes[0], values,
                              RowData.getClassUnderQualification());
 
             /**
              * We have set the data model for the Class Under Qualification
              * comboBox, i.e. what choices in contains,
              * now set the currently selected value to be the
              * value in this row's RowData object.
              */
 
             //comboBoxes[0].setSelectedItem(
             //    RowData.getClassUnderQualification());
 
             /**
              * Now set the model and selected value of the 
              * Collection Operator combobox.
              */
             /*
             model = new DefaultComboBoxModel(CollectionOperator.
                                              getCompoundCollectionOperators());
             comboBoxes[1].setModel(model);
             */
             setComboBoxModel(comboBoxes[1], CollectionOperator.
                              getCompoundCollectionOperators(),
                              RowData.getRootRow().getCollectionOperator());
 
             //comboBoxes[1].setSelectedItem(RowData.getRootRow().
             //    getCollectionOperator());
         }
         else if (rowData.isSimpleCompoundRow()) {
 
             /**
              * This is a "simple" Compound Row.  I.e. it only has
              * the Collection Operator comboBox in it.
              * Set the comboBox model.
              */
             /*
             DefaultComboBoxModel model = new DefaultComboBoxModel(
                 CollectionOperator.getCompoundCollectionOperators());
             comboBoxes[0].setModel(model);
             */
             setComboBoxModel(comboBoxes[0], CollectionOperator.
                              getCompoundCollectionOperators(),
                              rowData.getCollectionOperator());
 
             /**
              * Set the selected item of the Collection Operator comboBox
              * to be this row's value.
              */
             //comboBoxes[0].setSelectedItem(rowData.getCollectionOperator());
         }
         else {
 
             /**
              * This isn't the first row, nor is it a simple Compound Row,
              * so we have to do alot more work to set up this row's
              * widgets.  The code below basically works its way from
              * left to right initializing the values of the widgets
              * in this row.
              */
 
             /**
              * The leftmost widgets are one or more comboBoxes displaying
              * this RowData's attributePath.
              * Set the model for each comboBox displaying a list of attributes
              * in a class.
              * The leftmost comboBox shows the list of attributes of
              * this row's "parent" class.
              *
              * Here we iterate through the list of Attributes in this
              * row, setting the data model for each comboBox.
              */
             ArrayList<Attribute> attributes = rowData.getAttributePath();
             for (int index = 0; index < attributes.size(); index++) {
                 if (index == 0) {
                     /**
                      * The leftmost comboBox is filled with the
                      * attributes of the parentClass.  I.e. the
                      * class of its parent row.
                      * Also set the selected item in the comboBox.
                      */
                     ClassDescription parentClass = rowData.getParentClass();
                     System.out.println("Set model for comboBox "+index+
                         " to be "+parentClass);
                     setComboBoxModel(comboBoxes[index], parentClass,
                                      true, false, attributes.get(index));
                 }
                 else {
                     /**
                      * This is NOT the leftmost comboBox.
                      * Each comboBox is filled with the attributes of
                      * the class of the comboBox to its left.
                      * Also set the selected item in the comboBox.
                      */
                     Attribute att = attributes.get(index-1);
                     System.out.println("Set model for comboBox "+index+
                         " to be "+att.getClassDescription());
                     setComboBoxModel(comboBoxes[index],
                                      att.getClassDescription(), true, true,
                                      attributes.get(index));
                 }
 
                 /**
                  * Now set the selected item in the comboBox.
                  */
                 //comboBoxes[index].setSelectedItem(attributes.get(index));
             }
 
             /**
              * By this point, all the models and values of the comboBoxes
              * that correspond to Attributes on this RowData's attributePath
              * have been set.  E.g. if the row looks like this:
              *
              *      epochGroup.epochs Count == 5
              *
              * we have set the model and selected item in the epochGroup and 
              * epochs comboBoxes.
              *
              * Now we need to handle the comboBoxes and other widgets
              * that hold things like collection operators, attribute operators,
              * text fields, and so forth.
              */
 
             int widgetIndex = attributes.size();
 
             Attribute childmostAttribute = rowData.getChildmostAttribute();
             if (childmostAttribute.getCardinality() == Cardinality.TO_MANY) {
 
                 /**
                  * The item selected in the "childmost" (i.e. last)
                  * Attribute in this RowData's attributePath is an
                  * Attribute that has a to-many relationship with the
                  * class that contains it.  So, there is a comboBox
                  * to the right of it that the user can use to select
                  * the Collection Operator to use.
                  *
                  * Set that comboBox's model to the list of all the
                  * Collection Operators: Any, All, None, Count.
                  */
                 DefaultComboBoxModel model = new DefaultComboBoxModel(
                     CollectionOperator.values());
                 comboBoxes[widgetIndex].setModel(model);
 
                 /**
                  * Set the value of the Collection Operator comboBox
                  * to be this row's value.
                  */
                 comboBoxes[widgetIndex].setSelectedItem(
                     rowData.getCollectionOperator());
                 widgetIndex++;
 
                 if (rowData.getCollectionOperator() ==
                     CollectionOperator.COUNT) {
 
                     /**
                      * This row is something like:
                      *
                      *      epochGroup.epochs Count == 5
                      *
                      * Set the operator that is used for the Count.
                      * E.g. ==, >, <=
                      */
                     model = new DefaultComboBoxModel(
                         DataModel.OPERATORS_ARITHMATIC);
                     System.out.println("Set model for comboBox "+
                         widgetIndex+" to be numeric operator.");
                     comboBoxes[widgetIndex].setModel(model);
 
                     comboBoxes[widgetIndex].setSelectedItem(
                         rowData.getAttributeOperator());
                     widgetIndex++;
 
                     String attributeValue = rowData.getAttributeValue();
                     if (attributeValue == null)
                         attributeValue = "";
                     textField.setText(attributeValue);
                 }
             }
             else if (childmostAttribute.isPrimitive()) {
                 /**
                  * The rightmost Attribute is a primitive type,
                  * so we need to display a comboBox that has a
                  * selection of operators such as =, !=, <, >=, etc.
                  *
                  * Set the comboBox model to hold operators appropriate
                  * for the Type (int, string, float, boolean) of the
                  * Attribute.
                  */
                 DefaultComboBoxModel model;
                 if (childmostAttribute.getType() == Type.BOOLEAN)
                     model = new DefaultComboBoxModel(
                         DataModel.OPERATORS_BOOLEAN);
                 else if (childmostAttribute.getType() == Type.UTF_8_STRING)
                     model = new DefaultComboBoxModel(
                         DataModel.OPERATORS_STRING);
                 else
                     model = new DefaultComboBoxModel(
                         DataModel.OPERATORS_ARITHMATIC);
                 //System.out.println("Set model for comboBox "+
                 //                 widgetIndex+" to be operator of some type.");
                 comboBoxes[widgetIndex].setModel(model);
 
                 /*
                 System.out.println("childmostAttribute.getType() = "+
                                    childmostAttribute.getType());
                 System.out.println("rowData.getAttributeValue() = "+
                                    rowData.getAttributeValue());
                 */
 
                 if (childmostAttribute.getType() == Type.BOOLEAN) {
                     if (DataModel.OPERATOR_TRUE.equals(
                         rowData.getAttributeOperator()))
                         comboBoxes[widgetIndex].setSelectedItem(
                             DataModel.OPERATOR_TRUE);
                     else
                         comboBoxes[widgetIndex].setSelectedItem(
                             DataModel.OPERATOR_FALSE);
                 }
                 else {
                     comboBoxes[widgetIndex].setSelectedItem(
                         rowData.getAttributeOperator());
 
                     String attributeValue = rowData.getAttributeValue();
                     if (attributeValue == null)
                         attributeValue = "";
                     textField.setText(attributeValue);
                 }
                 widgetIndex++;
             }
             else if (!childmostAttribute.isPrimitive() &&
                 !childmostAttribute.equals(Attribute.SELECT_ATTRIBUTE) &&
                 !childmostAttribute.equals(Attribute.IS_NULL) &&
                 !childmostAttribute.equals(Attribute.IS_NOT_NULL) &&
                 rowData.getCollectionOperator() == null) {
 
                 /**
                  * The rightmost Attribute is a class, as opposed
                  * to a "primitive" type such as int, float, string,
                  * so we need to display another comboBox to its right
                  * that the user can use to choose an Attribute of
                  * that class or choose a special item such as "is null",
                  * "is not null", "Any Property", "My Property".
                  */
 
                 /**
                  * Set the comboBox model to hold attributes of
                  * the class that is selected in the comboBox to our left.
                  */
                 setComboBoxModel(comboBoxes[widgetIndex],
                                  childmostAttribute.getClassDescription(),
                                  true, true, Attribute.SELECT_ATTRIBUTE);
             }
 
             /**
              * If this RowData's rightmost child attribute is a
              * primitive type such as int, float, string,
              * we need to set the value of the operator
              * to be whatever this RowData's value is currently
              * set to.
              *
              * If the user can enter a value, set the textField.
              */
             /*
             if (rowData.getChildmostAttribute().isPrimitive()) {
 
                 if (rowData.getChildmostAttribute().getType() != 
                     Type.BOOLEAN) {
                     String attributeValue = rowData.getAttributeValue();
                     if (attributeValue == null)
                         attributeValue = "";
                     textField.setText(attributeValue);
                 }
             }
             */
         }
 
         return this;
     }
 
 
     /**
      * Set the model for the passed in comboBox to be the attributes
      * of the passed in classDescription.
      *
      * In addition, we will (optionally) prepend the special
      * Attribute.SELECT_ATTRIBUTE attribute, and we will append
      * the special Attribute.IS_NULL and Attribute.IS_NOT_NULL.
      *
      * @param hasSelectAttribute - If this is true, we will prepend the
      * special Attribute.SELECT_ATTRIBUTE to the list of the choices.
      *
      * @param appendNulls - If this is true, we will append the
      * special Attribute.IS_NULL and IS_NOT_NULL to the end of the
      * list of the choices.
      *
      * @param selectedItem - After setting the model, this method sets
      * the selected item to this value.  Pass null if you do not want to
      * set the selected item.
      */
     private void setComboBoxModel(JComboBox comboBox,
                                   ClassDescription classDescription,
                                   boolean hasSelectAttribute,
                                   boolean appendNulls, Object selectedItem) {
 
         ArrayList<Attribute> attributes = classDescription.getAllAttributes();
         Attribute[] values;
 
         ArrayList<Attribute> copy = new ArrayList<Attribute>(attributes);
 
         if (appendNulls) {
             copy.add(Attribute.IS_NULL);
             copy.add(Attribute.IS_NOT_NULL);
         }
 
         if (hasSelectAttribute)
             copy.add(0, Attribute.SELECT_ATTRIBUTE);
 
         /**
          * All the monkey business with the list of Attributes is
          * finished, so create a DefaultComboBoxModel out of the list
          * Attributes and install it in the comboBox.
          */
         values = copy.toArray(new Attribute[0]);
         setComboBoxModel(comboBox, values, selectedItem);
     }
 
 
     /**
      * @param selectedItem - After setting the model, this method sets
      * the selected item to this value.  Pass null if you do not want to
      * set the selected item.
      */
     private void setComboBoxModel(JComboBox comboBox, Object[] items,
                                   Object selectedItem) {
 
         DefaultComboBoxModel model = new DefaultComboBoxModel(items);
         comboBox.setModel(model);
         if (selectedItem != null)
             comboBox.setSelectedItem(selectedItem);
     }
 
 
     /**
      */
     @Override
     public Component getTableCellEditorComponent(JTable table,
                                                  Object value,
                                                  boolean isSelected,
                                                  int row,
                                                  int column) {
 
         System.out.println("Enter getTableCellEditorComponent()");
 
         //return(getTableCellRendererComponent(table, value, isSelected,
         return(getTableCellRendererComponent(table, value, true,
                                              true, row, column));
     }
 
     @Override
     public boolean isCellEditable(EventObject event) {
         return(true);
     }
     @Override
     public Object getCellEditorValue() {
         return(true);
     }
 
     @Override
     public void removeCellEditorListener(CellEditorListener listener) {
     }
     @Override
     public void addCellEditorListener(CellEditorListener listener) {
     }
     @Override
     public void cancelCellEditing() {
     }
     @Override
     public boolean stopCellEditing() {
         return(true);
     }
     @Override
     public boolean shouldSelectCell(EventObject event) {
         return(true);
     }
 
 
     @Override
     public void actionPerformed(ActionEvent e) {
 
         //System.out.println("Enter actionPerformed = "+e);
         if (e.getSource() == createCompoundRowButton) {
             table.createCompoundRow();
         }
         else if (e.getSource() == createAttributeRowButton) {
             table.createAttributeRow();
         }
         else if (e.getSource() == deleteButton) {
             //System.out.println("deleteButton pressed");
             table.deleteSelectedRow();
             //table.tableChanged(null);
         }
         else if (e.getSource() instanceof JComboBox) {
             comboBoxChanged((JComboBox)e.getSource());
         }
     }
 
 
     @Override
     public void itemStateChanged(ItemEvent e) {
         //System.out.println("Enter itemStateChanged = "+e);
         if (e.getStateChange() != ItemEvent.SELECTED)
             return;
         comboBoxChanged((JComboBox)e.getSource());
     }
 
 
     /**
      * This method is called when the user changes the selected
      * item in a comboBox.
      *
      * TODO: Clean up and comment this.
      */
     private void comboBoxChanged(JComboBox comboBox) {
 
         System.out.println("Enter comboBoxChanged");
         //int selectedRow = table.getSelectedRow();
         /*
         if (selectedRow < 0) {
             return;
         }
         */
         int editingRow = table.getEditingRow();
         if (editingRow < 0) {
             return;
         }
 
         /**
          * Figure out which comboBox was changed.
          */
         int comboBoxIndex = -1;
         for (int index = 0; index < comboBoxes.length; index++) {
             //if (e.getSource() == comboBoxes[index]) {
             if (comboBox == comboBoxes[index]) {
                 comboBoxIndex = index;
             }
         }
 
         if (comboBoxIndex < 0) {
             System.err.println("ERROR:  In comboBoxChanged.  "+
                 "comboBoxIndex = "+comboBoxIndex+
                 ".  This should never happen.");
             return;
         }
 
         /*
         if (selectedRow < 0) {
             System.err.println("POSSIBLE ERROR:  In comboBoxChanged.  "+
                 "selectedRow = "+selectedRow+
                 ".  Should this ever happen???");
             //return;
         }
         */
 
         if (editingRow < 0) {
             System.err.println("ERROR:  In comboBoxChanged.  "+
                 "editingRow = "+editingRow+
                 ".  This should never happen.");
             return;
         }
         if (editingRow != modelRow)
             return;
 
         /**
          * At this point we know which row is being edited by the user,
          * and we know which comboBox within that row was changed.
          *
          * So, now change the appropriate value in this row's RowData
          * object.
          */
 
         System.out.println("comboBoxIndex = "+comboBoxIndex);
         //System.out.println("selectedRow = "+selectedRow);
         System.out.println("editingRow = "+editingRow);
 
         RowData rowData = RowData.getRootRow().getChild(editingRow);
         if (editingRow == 0) {
             /**
              * The first/topmost row is being edited.  So we need to
              * adjust the value of the "root" row.  Also known as the
              * Class Under Qualification.
              */
             //RowData rowData = RowData.getRootRow();
 
             if (comboBoxIndex == 0) {
                 /**
                  * User is changing the value of the Class Under Qualification.
                  */
                 ClassDescription classDescription =
                     (ClassDescription)comboBox.getSelectedItem();
                 System.out.println("selected classDescription = "+
                     classDescription);
                 System.out.println("selected classDescription = "+
                     classDescription.hashCode());
                 if (!rowData.getClassUnderQualification().equals(
                     classDescription)) {
                     rowData.setClassUnderQualification(classDescription);
                 }
             }
             else if (comboBoxIndex == 1) {
                 /**
                  * User is changing the value of the Collection Operator.
                  */
                 CollectionOperator collectionOperator =
                     (CollectionOperator)comboBox.getSelectedItem();
                 System.out.println("selected collectionOperator = "+
                     collectionOperator);
                 System.out.println("selected collectionOperator = "+
                     collectionOperator.hashCode());
                 if (!rowData.getCollectionOperator().equals(
                     collectionOperator)) {
                     rowData.setCollectionOperator(collectionOperator);
                 }
             }
         }
         else {
             /**
              * User is editing a row other than the first row.
              */
 
             ArrayList<Attribute> attributes = rowData.getAttributePath();
 
             Object selectedObject = comboBox.getSelectedItem();
             if (selectedObject instanceof Attribute) {
 
                 Attribute selectedAttribute = (Attribute)selectedObject;
                 System.out.println("selectedAttribute = "+selectedAttribute);
                 /*
                 System.out.println("Attribute.IS_NULL = "+Attribute.IS_NULL);
                 System.out.println(
                     "selectedAttribute.equals(Attribute.IS_NULL) = "+
                     selectedAttribute.equals(Attribute.IS_NULL));
                 */
 
                 if (selectedAttribute.getCardinality() !=
                     Cardinality.TO_MANY) {
                     rowData.setCollectionOperator(null);
                 }
                 else if (selectedAttribute.getCardinality() ==
                          Cardinality.TO_MANY) {
                     rowData.setCollectionOperator(CollectionOperator.COUNT);
                     rowData.setAttributeOperator(
                         DataModel.OPERATORS_ARITHMATIC[0]);
                     rowData.setAttributeValue("0");
                 }
 
                 if ((selectedAttribute.equals(Attribute.IS_NULL)) ||
                     (selectedAttribute.equals(Attribute.IS_NOT_NULL))) {
                     //System.out.println("Setting attributeOperator to: "+
                     //    selectedAttribute.getName());
                     rowData.setAttributeOperator(selectedAttribute.getName());
                     //rowData.setAttributeValue(null);
                 }
                 else if (selectedAttribute.getType() == Type.BOOLEAN) {
                     rowData.setAttributeOperator(DataModel.OPERATOR_TRUE);
                     //rowData.setAttributeValue(null);
                 }
 
                 System.out.println("After op rowData: "+rowData.getRowString());
 
                 if (attributes.size() > comboBoxIndex) {
                     /**
                      * The user is setting the value of an Attribute
                      * that is already in this RowData's attributePath.
                      */
                     attributes.set(comboBoxIndex, selectedAttribute);
                 }
                 else if (attributes.size() == comboBoxIndex) {
                     /**
                      * This is the rightmost comboBox and this RowData
                      * is having this entry in its attributePath set
                      * to an "initial" value.  I.e. the comboBox used
                      * to say "Select Attribute" before the user selected
                      * a value for the first time.
                      */
                     attributes.add(selectedAttribute);
                 }
                 else if (attributes.size() < comboBoxIndex) {
                     /**
                      * This should never happen.
                      */
                     System.err.println("ERROR: Coding error.  "+
                         "Too many comboBoxes "+
                         "or too few Attributes in the class's attributePath.");
                 }
                 System.out.println("After at rowData: "+rowData.getRowString());
 
                 /**
                  * Remove Attributes that are "after" the one being changed.
                  */
                 attributes.subList(comboBoxIndex+1, attributes.size()).clear();
 
                 /**
                  * If the user set the value of a primitive type,
                  * that means we need to be sure the operator is
                  * initialized to an appropriate value for that
                  * type.  E.g. "==" for an int or string, "is true" for
                  * a boolean.
                  *
                  * TODO:  Add methods to the RowData class that are
                  * used to access the attributePath that automatically
                  * handle this sort of business logic.
                  */
                 Attribute childmostAttribute = rowData.getChildmostAttribute();
                 if (childmostAttribute.isPrimitive()) {
 
                     String attributeOperator;
                     switch (childmostAttribute.getType()) {
                         case BOOLEAN:
                             attributeOperator = DataModel.OPERATORS_BOOLEAN[0];
                         break;
                         case UTF_8_STRING:
                             attributeOperator = DataModel.OPERATORS_STRING[0];
                         break;
                         case INT_16:
                         case INT_32:
                         //case FLOAT_32:
                         case FLOAT_64:
                         case DATE_TIME:
                             attributeOperator = 
                                 DataModel.OPERATORS_ARITHMATIC[0];
                         break;
                         default:
                             System.err.println("ERROR: Unhandled operator.");
                             attributeOperator = "ERROR";
                     }
 
                     rowData.setAttributeOperator(attributeOperator);
                 }
 
             }
             else if ((selectedObject instanceof String) &&
                      rowData.getChildmostAttribute().isPrimitive()) {
 
                 System.out.println("User selected primitive operator "+
                                    selectedObject);
                 /**
                  * The user has selected a value in primitive operator
                  * comboBox.  E.g. ==, !=, >.
                  */
                 rowData.setAttributeOperator((String)selectedObject);
             }
             else if (selectedObject instanceof CollectionOperator) {
                 /**
                  * User is changing the value of the Collection Operator.
                  */
                 CollectionOperator collectionOperator =
                     (CollectionOperator)selectedObject;
                 System.out.println("selected collectionOperator = "+
                     collectionOperator);
                 if (!rowData.getCollectionOperator().equals(
                     collectionOperator)) {
                     rowData.setCollectionOperator(collectionOperator);
                 }
             }
 
             System.out.println("rowData's new value: "+rowData.getRowString());
 
             /**
              * 
              */
 
            //table.tableChanged(null);
         }
 
        table.tableChanged(null);
         System.out.println("rootRow:\n"+RowData.getRootRow());
     }
 
 
     public void insertUpdate(DocumentEvent event) {
         valueTextFieldChanged();
     }
 
     public void removeUpdate(DocumentEvent event) {
         valueTextFieldChanged();
     }
 
     public void changedUpdate(DocumentEvent event) {
         //valueTextFieldChanged();
     }
 
     private void valueTextFieldChanged() {
 
         int editingRow = table.getEditingRow();
         if (editingRow < 0) {
             return;
         }
         if (editingRow != modelRow)
             return;
 
         RowData rowData = RowData.getRootRow().getChild(editingRow);
         rowData.setAttributeValue(textField.getText());
 
         if (editingRow == 0) {
             System.out.println("Changed Text Of Root Row");
             System.out.println("textField.isVisible() = "+textField.isVisible());
             System.exit(0);
         }
     }
 }
