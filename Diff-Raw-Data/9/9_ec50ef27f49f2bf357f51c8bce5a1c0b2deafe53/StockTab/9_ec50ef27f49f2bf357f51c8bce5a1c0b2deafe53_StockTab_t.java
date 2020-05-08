 package ee.ut.math.tvt.salessystem.ui.tabs;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.JTableHeader;
 
 import org.apache.log4j.Logger;
 
 import ee.ut.math.tvt.salessystem.domain.controller.SalesDomainController;
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 
 public class StockTab {
 
     private static final Logger log = Logger.getLogger(StockTab.class);
 
     private final SalesDomainController controller;
 
     private SalesSystemModel model;
 
     private JButton addItem;
 
     public StockTab(SalesSystemModel model, SalesDomainController controller) {
         this.model = model;
         this.controller = controller;
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
 
         addItem = new JButton("Add");
         addItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 addStockItemEventHandler();
             }
         });
 
         gc.gridwidth = GridBagConstraints.RELATIVE;
         gc.weightx = 1.0;
         panel.add(addItem, gc);
 
         panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
         return panel;
     }
 
     // table of the wareshouse stock
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
 
     /**
      * Add new item to the stock.
      */
     public void addStockItemEventHandler() {
         JTextField itemName = new JTextField();
         JTextField itemPrice = new JTextField();
         JTextField itemQuantity = new JTextField();
 
         JOptionPane op = new JOptionPane(
             new Object[] {"Name:", itemName, "Price:", itemPrice, "Quantity: ", itemQuantity},
             JOptionPane.QUESTION_MESSAGE,
             JOptionPane.OK_CANCEL_OPTION,
             null,
             null);
 
         JDialog dialog = op.createDialog(null, "Enter new item ...");
         dialog.setDefaultCloseOperation(
                 JDialog.DO_NOTHING_ON_CLOSE);
         dialog.setVisible(true);
 
         int result = ((Integer)op.getValue()).intValue();
 
         if (result == JOptionPane.OK_OPTION) {
             addItemOkPressed(itemName.getText(), itemPrice.getText(), itemQuantity.getText());
         } else {
             log.debug("Cancelled");
         }
     }
 
     /**
      * Add stock item
      */
     private void addItemOkPressed(String itemName, String itemPrice, String itemQuantity) {
         StringBuffer errorMessage = new StringBuffer();
         double price = 0.0;
         int quantity = 0;
         boolean nameValid = false;
         boolean priceValid = false;
         boolean quantityValid = false;
 
         nameValid = itemName.length() > 0;
         if (!nameValid) {
             errorMessage.append("A non-empty name must be entered for the product!\n");
         } else if (!model.getWarehouseTableModel().validateNameUniqueness(itemName)) {
             errorMessage.append("Entered name already exists!\n");
             nameValid = false;
         }
 
         try {
             price = Double.parseDouble(itemPrice);
             priceValid = (price >= 0.0);
         } catch (NumberFormatException ex) {}
 
         if (!priceValid) {
             errorMessage.append("Entered price is not valid!\n");
         }
 
         try {
             quantity = Integer.parseInt(itemQuantity);
             quantityValid = (quantity >= 0);
         } catch (NumberFormatException ex) {}
 
         if (!quantityValid) {
             errorMessage.append("Entered quantity is not valid!");
         }
 
         if (nameValid && priceValid && quantityValid) {
             StockItem newItem = new StockItem(itemName, "", price, quantity);
             controller.createStockItem(newItem);
 
         // Show the error messages
         } else {
             JOptionPane.showMessageDialog(
                     null,
                     errorMessage.toString(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE
             );
         }
     }
 
 	public void refresh() {
 		log.info("Stock tab refresh");
 		this.model.refreshStock();
 	}
 
 }
