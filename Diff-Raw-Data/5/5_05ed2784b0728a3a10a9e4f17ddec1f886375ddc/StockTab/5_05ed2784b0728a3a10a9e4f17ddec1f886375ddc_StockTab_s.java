 package ee.ut.math.tvt.kvaliteetsedideed.ui.tabs;
 
 import ee.ut.math.tvt.kvaliteetsedideed.domain.controller.SalesDomainController;
 import ee.ut.math.tvt.kvaliteetsedideed.domain.data.StockItem;
 import ee.ut.math.tvt.kvaliteetsedideed.ui.model.SalesSystemModel;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.math.BigDecimal;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.JTableHeader;
 
 public class StockTab {
 
   private JButton addItem;
 
   private SalesSystemModel model;
   private SalesDomainController domainController;
 
   public StockTab(SalesDomainController domainController, SalesSystemModel model) {
     this.domainController = domainController;
     this.model = model;
   }
 
   // warehouse stock tab - consists of a menu and a table
   public Component draw() {
     JPanel panel = new JPanel();
     panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
     GridBagLayout gb = new GridBagLayout();
     GridBagConstraints gc = new GridBagConstraints();
     panel.setLayout(gb);
 
     gc.fill = GridBagConstraints.HORIZONTAL;
     gc.anchor = GridBagConstraints.NORTH;
     gc.gridwidth = GridBagConstraints.REMAINDER;
     gc.weightx = 1.0d;
     gc.weighty = 0d;
 
     panel.add(drawStockMenuPane(), gc);
 
     gc.weighty = 1.0;
     gc.fill = GridBagConstraints.BOTH;
     panel.add(drawStockMainPane(), gc);
     return panel;
   }
 
   // warehouse menu
   private Component drawStockMenuPane() {
     JPanel panel = new JPanel();
 
     GridBagConstraints gc = new GridBagConstraints();
     GridBagLayout gb = new GridBagLayout();
 
     panel.setLayout(gb);
 
     gc.anchor = GridBagConstraints.NORTHWEST;
     gc.weightx = 0;
 
     addItem = createAddButton();
     gc.gridwidth = GridBagConstraints.RELATIVE;
     gc.weightx = 1.0;
     panel.add(addItem, gc);
 
     panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
     return panel;
   }
 
   // Creates the button "Add"
   private JButton createAddButton() {
     JButton b = new JButton("Add");
     b.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         addButtonClicked();
       }
     });
     return b;
   }
 
   /** Event handler for the <code>add</code> event. */
   protected void addButtonClicked() {
     try {
       addNewItem();
     } catch (Exception e1) {
       System.err.print(e1.getMessage());
     }
   }
 
   private void addNewItem() {
 
     StockItem newItem = new StockItem();
     setNewItemData(newItem);
 
     if (!newItem.getName().equals(null) && !newItem.getName().trim().isEmpty()) {
      // get id of item in last row (largest id)
      int nrRows = model.getWarehouseTableModel().getRowCount();
      Object newId = model.getWarehouseTableModel().getValueAt(nrRows - 1, 0);

      newItem.setId(((long) newId) + 1);
       domainController.addStockItem(newItem);
       model.getWarehouseTableModel().populateWithData(domainController.loadWarehouseState());
       model.getWarehouseTableModel().fireTableDataChanged();
       model.getStockComboBoxModel().addElement(newItem);
     }
 
   }
 
   private void setNewItemData(StockItem item) {
 
     JTextField name = new JTextField(10);
     JTextField quantity = new JTextField(3);
     JTextField price = new JTextField(3);
 
     JPanel panel = new JPanel();
     panel.add(new JLabel("Product name:"));
     panel.add(name);
     panel.add(Box.createVerticalStrut(15)); // a spacer
     panel.add(new JLabel("Quantity:"));
     panel.add(quantity);
     panel.add(Box.createVerticalStrut(15)); // a spacer
     panel.add(new JLabel("Price:"));
     panel.add(price);
 
     int result = JOptionPane.showConfirmDialog(null, panel, "Adding new product", JOptionPane.OK_CANCEL_OPTION);
 
     if (result == JOptionPane.OK_OPTION) {
       item.setName(name.getText());
       Double inputPrice = Double.parseDouble(price.getText());
       BigDecimal bd = new BigDecimal(inputPrice);
       bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
       item.setPrice(bd.doubleValue());
       item.setQuantity(Integer.parseInt(quantity.getText()));
     }
   }
 
   // table of the warehouse stock
   private Component drawStockMainPane() {
     JPanel panel = new JPanel();
 
     JTable table = new JTable(model.getWarehouseTableModel());
 
     JTableHeader header = table.getTableHeader();
     header.setReorderingAllowed(false);
 
     JScrollPane scrollPane = new JScrollPane(table);
 
     GridBagConstraints gc = new GridBagConstraints();
     GridBagLayout gb = new GridBagLayout();
     gc.fill = GridBagConstraints.BOTH;
     gc.weightx = 1.0;
     gc.weighty = 1.0;
 
     panel.setLayout(gb);
     panel.add(scrollPane, gc);
 
     panel.setBorder(BorderFactory.createTitledBorder("Warehouse status"));
     return panel;
   }
 
 }
