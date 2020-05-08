 package wingscms;
 
 import org.wingx.XTable;
 import org.wingx.table.EditableTableCellRenderer;
 import org.wings.*;
 import org.wings.table.STableCellRenderer;
 
 import javax.swing.table.DefaultTableModel;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 import java.util.LinkedList;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.*;
 
 /**
  * <code>JoomlaExample<code>.
  * <p/>
  * User: rrd
  * Date: 08.08.2007
  * Time: 09:41:33
  *
  * @author rrd
  * @version $Id
  */
 public class JoomlaExample {
 
    private SFrame rootFrame = new SFrame();
 
     public JoomlaExample() {
         // Shoppingcart contents (Key = product index in PLCONTENT, Value = amount)
         //final HashMap<Integer, Integer> SCCONTENT = new HashMap<Integer, Integer>();
         //  SessionManager.getSession().setProperty("shoppingcart", SCCONTENT);
         final LinkedList<ShoppingCartItem> SCLIST = new LinkedList<ShoppingCartItem>();
 
         // Pricelist panel contains the pricelist table,
         // an addtoshoppingcart Button and a message label.
         final SPanel ppricelist = new SPanel(new SFlowDownLayout());
 
         // Result label
         final SLabel message = new SLabel("Test Message");
 
         // Products
         final LinkedList<Product> PRODUCTS = new LinkedList<Product>();
         PRODUCTS.add(new Product(1001, "Rotes Auto", 50000d));
         PRODUCTS.add(new Product(1002, "Blaues Auto", 75000d));
         PRODUCTS.add(new Product(1003, "Schwarzes Auto", 100000d));
 
         // Productlist table
         final String[] PLCOLNAMES = { "Art. Nr.", "Artikel", "Stückpreis (in €)" };
         final XTable productlist = new XTable(new DefaultTableModel() {
             public Object getValueAt(int row, int col) {
                 Product product = PRODUCTS.get(row);
                 switch (col) {
                     case 0:
                         return product.getItemnumber();
                     case 1:
                         return product.getDescription();
                     case 2:
                         return product.getPrice();
                     default:
                         return null;
                 }
             }
 
             @Override
             public Class<?> getColumnClass(int columnIndex) {
                 switch (columnIndex) {
                     case 0:
                         return Integer.class;
                     default:
                         return String.class;
                 }
             }
 
             public int getRowCount() {
                 return PRODUCTS.size();
             }
 
             public int getColumnCount() {
                 return PLCOLNAMES.length;
             }
 
             public String getColumnName(int i) {
                 return PLCOLNAMES[i];
             }
         });
         productlist.addSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 if (productlist.getSelectedRow() != -1) {
                     message.setText("");
                     ppricelist.reload();
                 }
             }
         });
 
         JoomlaTableCellRenderer renderer = new JoomlaTableCellRenderer();
         renderer.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 System.out.println("JoomlaExample.actionPerformed");
 
                 int row = productlist.getSelectedRow();
                 Object value = productlist.getValueAt(row, 1);
 
                 message.setText("Product: " + value + " has been selected.");
                 message.reload();
             }
         });
 
         productlist.setDefaultRenderer(Integer.class, renderer);
         productlist.setEditable(false);
 
         // Shoppingcart table
         final String[] SCCOLNAMES = { "Art. Nr.", "Artikel", "Stückpreis (in €)", "Anzahl", "Gesamtpreis (in €)" };
         final STable shoppingcart = new STable(new DefaultTableModel() {
             public Object getValueAt(int row, int col) {
                 ShoppingCartItem item = SCLIST.get(row);
                 switch (col) {
                     case 0:
                         return item.getProduct().getItemnumber();
                     case 1:
                         return item.getProduct().getDescription();
                     case 2:
                         return item.getProduct().getPrice();
                     case 3:
                         return item.getAmount();
                     case 4:
                         return item.getAllRoundPrice();
                     default:
                         return null;
                 }
             }
 
             public int getRowCount() {
                 return SCLIST.size();
             }
 
             public int getColumnCount() {
                 return SCCOLNAMES.length;
             }
 
             public String getColumnName(int i) {
                 return SCCOLNAMES[i];
             }
         });
         shoppingcart.setEditable(false);
 
         // Addtoshoppingcart button
         SButton addtoshoppingcart = new SButton("In den Warenkorb");
         addtoshoppingcart.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 if (productlist.getSelectedRow() != -1) {
                     Product selection = PRODUCTS.get(productlist.getSelectedRow());
 
                     // If the selected item is already in the shoppingcart just increase the amount
                     boolean isinlist = false;
                     for (ShoppingCartItem item : SCLIST) {
                         if (item.getProduct().equals(selection)) {
                             item.setAmount(item.getAmount() + 1);
                             isinlist = true;
                             break;
                         }
                     }
                     if (!isinlist) {
                         SCLIST.add(new ShoppingCartItem(selection));
                         shoppingcart.reload();
                     }
 
                     message.setForeground(Color.BLACK);
                     message.setText(
                             "Das Produkt \"" + selection.getDescription() + "\" wurde zum Warenkorb hinzugefügt.");
                     productlist.clearSelection();
                     ppricelist.reload();
                 } else {
                     message.setForeground(Color.RED);
                     message.setText("Fehler: Kein Produkt ausgewählt.");
                 }
             }
         });
 
         // Pricelist panel
         ppricelist.add(productlist);
         ppricelist.add(addtoshoppingcart);
 
         rootFrame.add(productlist, "PRODUCTLIST");
         rootFrame.add(addtoshoppingcart, "ADDTOSHOPPINGCART");
         rootFrame.add(ppricelist, "PRICELIST");
         rootFrame.add(shoppingcart, "SHOPPINGCART");
         rootFrame.add(message, "MESSAGE");
 
         rootFrame.getContentPane().setPreferredSize(SDimension.FULLAREA);
         rootFrame.getContentPane().setVerticalAlignment(SConstants.TOP_ALIGN);
         rootFrame.setVisible(true);
     }
 
     class JoomlaTableCellRenderer extends SButton implements STableCellRenderer, EditableTableCellRenderer {
 
         public Object getValue() {
             return getText();
         }
 
         public LowLevelEventListener getLowLevelEventListener(STable table, int row, int column) {
             return this;
         }
 
         public SComponent getTableCellRendererComponent(final STable table, final Object value, boolean isSelected, final int row, final int column) {
             setText(String.valueOf(value));
 
             return this;
         }
     }
 }
