 package asrs;
 
 import java.awt.Dimension;
import java.util.Date;
 import java.util.LinkedList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 
import order.Customer;
import order.Location;
 import order.Order;
 import order.Product;
 
 public class OrderPanel extends JPanel {
 
 	private ProductModel productModel = new ProductModel();
 
 	/**
 	 * ctor
 	 * 
 	 * @author Luuk
 	 */
 	public OrderPanel() {
 		setBorder(BorderFactory.createTitledBorder("Order"));
 
 		setPreferredSize(new Dimension(670, 650));
 
 		buildUI();
 	}
 
 	/**
 	 * Bouwt de ui
 	 * 
 	 * @author Luuk
 	 */
 	private void buildUI() {
 		// maak een tabel op basis van de nestedclass product model
 		JTable table = new JTable(productModel);
 
 		// zet de afmetingen
 		table.getTableHeader().setPreferredSize(new Dimension(620, 20));
 		table.setPreferredSize(new Dimension(620, 580));
 		table.setPreferredScrollableViewportSize(new Dimension(650, 580));
 
 		// cellen zijn niet aanpasbaar
 		table.setEnabled(false);
 
 		add(table.getTableHeader());
 		add(table);
 	}
 
 	/**
 	 * Zet de order die wordt weergegeven in de tabel, wordt automatisch
 	 * geupdate
 	 * 
 	 * @author Luuk
 	 * 
 	 * @param order
 	 */
 	public void setOrder(Order order) {
 		for (Product product : order.getProducts()) {
 			productModel.addElement(product);
 
 			productModel.fireTableRowsUpdated(
 					productModel.products.indexOf(product),
 					productModel.products.indexOf(product));
 		}
 	}
 
 	/**
 	 * Update een status van een product, tabel wordt automatisch geupdate
 	 * 
 	 * @author Luuk
 	 * 
 	 * @param product
 	 * @param status
 	 */
 	public void updateStatus(Product product, String status) {
 		int index = productModel.products.indexOf(product);
 
 		product.setStatus(status);
 
 		productModel.fireTableRowsUpdated(index, index);
 	}
 
 	/**
 	 * Nested class die wordt gebruikt door de tabel om hem dynamisch te maken
 	 * 
 	 * @author Luuk
 	 * 
 	 */
 	private class ProductModel extends AbstractTableModel {
 
 		// De kolomnamen
 		private final String[] columnNames = { "#", "Omschrijving", "Prijs",
 				"Grootte", "Status" };
 
 		// Lijst van weer te geven producten
 		private final LinkedList<Product> products;
 
 		/**
 		 * Ctor
 		 * 
 		 * @author Luuk
 		 */
 		private ProductModel() {
 			products = new LinkedList<Product>();
 		}
 
 		/**
 		 * Voeg een product toe aan de tabel, wordt automatisch geupdate
 		 * 
 		 * @author Luuk
 		 * 
 		 * @param product
 		 */
 		public void addElement(Product product) {
 			// Adds the element in the last position in the list
 			products.add(product);
 			fireTableRowsInserted(products.size() - 1, products.size() - 1);
 		}
 
 		@Override
 		/**
 		 * Returnt aantal kollommen
 		 * 
 		 * @author Luuk
 		 */
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 
 		@Override
 		/**
 		 * Returnt aantal rijen
 		 * 
 		 * @author Luuk
 		 */
 		public int getRowCount() {
 			return products.size();
 		}
 
 		@Override
 		/**
 		 * De waardes per kolom en rij
 		 * 
 		 * @author Luuk
 		 * 
 		 * @return mixed
 		 */
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			switch (columnIndex) {
 			case 0:
 				return products.get(rowIndex).getId();
 			case 1:
 				return products.get(rowIndex).getDescription();
 			case 2:
 				return products.get(rowIndex).getPrice();
 			case 3:
 				return products.get(rowIndex).getSize();
 			case 4:
 				return products.get(rowIndex).getStatus();
 			}
 			return null;
 		}
 
 	}
 }
