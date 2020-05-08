 package ee.ut.math.tvt.salessystem.ui.panels;
 
 import ee.ut.math.tvt.salessystem.domain.data.SoldItem;
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.NoSuchElementException;
 
 /**
  * Purchase pane + shopping cart tabel UI.
  */
 public class PurchaseItemPanel extends JPanel {
 
     private static final long serialVersionUID = 1L;
 
     // Text field on the dialogPane
     private JComboBox<Object> productField;
     private JTextField barCodeField;
     private JTextField quantityField;
     private JTextField nameField;
     private JTextField priceField;
 
     private JButton addItemButton;
 
     // Warehouse model
     private SalesSystemModel model;
 
     /**
      * Constructs new purchase item panel.
      *
      * @param model
      *            composite model of the warehouse and the shopping cart.
      */
     public PurchaseItemPanel(SalesSystemModel model) {
         this.model = model;
 
         setLayout(new GridBagLayout());
 
         add(drawDialogPane(), getDialogPaneConstraints());
         add(drawBasketPane(), getBasketPaneConstraints());
 
         setEnabled(false);
     }
 
     // shopping cart pane
     private JComponent drawBasketPane() {
 
         // Create the basketPane
         JPanel basketPane = new JPanel();
         basketPane.setLayout(new GridBagLayout());
         basketPane.setBorder(BorderFactory.createTitledBorder("Shopping cart"));
 
         // Create the table, put it inside a scollPane,
         // and add the scrollPane to the basketPanel.
         JTable table = new JTable(model.getCurrentPurchaseTableModel());
         JScrollPane scrollPane = new JScrollPane(table);
 
         basketPane.add(scrollPane, getBacketScrollPaneConstraints());
 
         return basketPane;
     }
 
     // purchase dialog
     private JComponent drawDialogPane() {
 
         // Create the panel
         JPanel panel = new JPanel();
         panel.setLayout(new GridLayout(6, 2));
         panel.setBorder(BorderFactory.createTitledBorder("Product"));
 
         // Initialize the textfields
         productField = new JComboBox<Object>(makeListOfStockNames());
         barCodeField = new JTextField();
         quantityField = new JTextField("1");
         nameField = new JTextField();
         priceField = new JTextField();
 
 //getSelectedItem()
         
 
 
         // Fill the dialog fields if the bar code text field loses focus
         productField.addItemListener(new ItemListener() {
 
             @Override
             public void itemStateChanged(ItemEvent arg0) {
                 int j = (int)findBarCode(productField.getSelectedItem());
                 barCodeField.setText(Integer.toString(j));
                 fillDialogFields();
 
             }
         });
         productField.addFocusListener(new FocusListener() {
             public void focusGained(FocusEvent e) {
             }
 
             public void focusLost(FocusEvent e) {
                 int j = (int)findBarCode(productField.getSelectedItem());
                 barCodeField.setText(Integer.toString(j));
                 fillDialogFields();
             }
         });
         barCodeField.setEditable(false);
         nameField.setEditable(false);
         priceField.setEditable(false);
 
         // == Add components to the panel
 
         // - product name
         panel.add(new JLabel("Product:"));
         panel.add(productField);
 
         // - bar code
         panel.add(new JLabel("Bar code:"));
         panel.add(barCodeField);
 
         // - amount
         panel.add(new JLabel("Amount:"));
         panel.add(quantityField);
 
         // - name
         panel.add(new JLabel("Name:"));
         panel.add(nameField);
 
         // - price
         panel.add(new JLabel("Price:"));
         panel.add(priceField);
 
         // Create and add the button
         addItemButton = new JButton("Add to cart");
         addItemButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 addItemEventHandler();
             }
         });
 
         panel.add(addItemButton);
 
         return panel;
     }
     public Object[] makeListOfStockNames(){
         int i = model.getWarehouseTableModel().getRowCount();
         Object[] e = new String[i];
         for(int j=1;j<=i;j++){
             e[j-1] = model.getWarehouseTableModel().getItemById(j).getColumn(1);
         }
         return e;
     }
 
 
     // Fill dialog with data from the "database".
     public void fillDialogFields() {
         StockItem stockItem = getStockItemByBarcode();
 
         if (stockItem != null) {
             nameField.setText(stockItem.getName());
             String priceString = String.valueOf(stockItem.getPrice());
             priceField.setText(priceString);
         } else {
             reset();
         }
     }
 
 
     // Search the warehouse for a StockItem with the bar code entered
     // to the barCode textfield.
     private StockItem getStockItemByBarcode() {
         try {
             int code = Integer.parseInt(barCodeField.getText());
             return model.getWarehouseTableModel().getItemById(code);
         } catch (NumberFormatException ex) {
             return null;
         } catch (NoSuchElementException ex) {
             return null;
         }
     }
 
 
     private long findBarCode(Object name) {
         int i = model.getWarehouseTableModel().getRowCount();
         Object[] e = new String[i];
         long[] k = new long[i];
         long vastus = 0;
         for(int j=1;j<=i;j++){
             e[j-1] = model.getWarehouseTableModel().getItemById(j).getColumn(1);
         }
 
         for(int j=1;j<=i;j++){
             k[j-1] = Long.valueOf(String.valueOf(model.getWarehouseTableModel().getItemById(j).getColumn(0)));
         }
 
         for(int j=0; j<i; j++){
             if (e[j] == name) {
                 vastus = k[j];
             }
         }
         return vastus;
 
     }
     /**
      * Add new item to the cart.
      */
     public void addItemEventHandler() {
         // add chosen item to the shopping cart.
         StockItem stockItem = getStockItemByBarcode();
         if (stockItem != null) {
             int quantity;
             long ID = 0;
             try {
                 quantity = Integer.parseInt(quantityField.getText());
                 ID = Long.parseLong(barCodeField.getText());
             } catch (NumberFormatException ex) {
                 quantity = 1;
             }
             model.getCurrentPurchaseTableModel()
                     .addItem(new SoldItem(ID ,stockItem, quantity));
         }
     }
 
     /**
      * Sets whether or not this component is enabled.
      */
     @Override
    public void setEnabled(boolean enabled) {    	
         this.productField.setEnabled(enabled);
         this.addItemButton.setEnabled(enabled);
         this.barCodeField.setEnabled(enabled);
         this.quantityField.setEnabled(enabled);
     }
 
     /**
      * Reset dialog fields.
      */
     public void reset() {
         barCodeField.setText("");
         quantityField.setText("1");
         nameField.setText("");
         priceField.setText("");
     }
 
     /*
      * === Ideally, UI's layout and behavior should be kept as separated as
      * possible. If you work on the behavior of the application, you don't want
      * the layout details to get on your way all the time, and vice versa. This
      * separation leads to cleaner, more readable and better maintainable code.
      * 
      * In a Swing application, the layout is also defined as Java code and this
      * separation is more difficult to make. One thing that can still be done is
      * moving the layout-defining code out into separate methods, leaving the
      * more important methods unburdened of the messy layout code. This is done
      * in the following methods.
      */
 
     // Formatting constraints for the dialogPane
     private GridBagConstraints getDialogPaneConstraints() {
         GridBagConstraints gc = new GridBagConstraints();
 
         gc.anchor = GridBagConstraints.WEST;
         gc.weightx = 0.2;
         gc.weighty = 0d;
         gc.gridwidth = GridBagConstraints.REMAINDER;
         gc.fill = GridBagConstraints.NONE;
 
         return gc;
     }
 
     // Formatting constraints for the basketPane
     private GridBagConstraints getBasketPaneConstraints() {
         GridBagConstraints gc = new GridBagConstraints();
 
         gc.anchor = GridBagConstraints.WEST;
         gc.weightx = 0.2;
         gc.weighty = 1.0;
         gc.gridwidth = GridBagConstraints.REMAINDER;
         gc.fill = GridBagConstraints.BOTH;
 
         return gc;
     }
 
     private GridBagConstraints getBacketScrollPaneConstraints() {
         GridBagConstraints gc = new GridBagConstraints();
 
         gc.fill = GridBagConstraints.BOTH;
         gc.weightx = 1.0;
         gc.weighty = 1.0;
 
         return gc;
     }
 
 }
