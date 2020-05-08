 package ee.ut.math.tvt.salessystem.ui.panels;
 
 import ee.ut.math.tvt.salessystem.domain.data.SoldItem;
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import javax.swing.BorderFactory;
 import javax.swing.ComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.plaf.basic.BasicComboBoxRenderer;
 
 /**
  * Purchase pane + shopping cart tabel UI.
  */
 public class PurchaseItemPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	// Product dropdown field
 	public static JComboBox<StockItem> productField;
 
 	// Text field on the dialogPane
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
 
 		// Initialize the dropdown
		productField = new JComboBox<StockItem>();
		productField.addItem(null);
		for (StockItem x : model.getWarehouseTableModel().getTableRows()
				.toArray(new StockItem[0])) {
			productField.addItem(x);
		}
 		productField.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					StockItem selectedItem = (StockItem) productField
 							.getSelectedItem();
 					barCodeField.setText(selectedItem.getId().toString());
 					fillDialogFields();
 				}
 			}
 		});
 
 		// Initialize the textfields
 		barCodeField = new JTextField();
 		quantityField = new JTextField("1");
 		nameField = new JTextField();
 		priceField = new JTextField();
 
 		// Fill the dialog fields if the bar code text field loses focus
 		barCodeField.addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 			}
 
 			public void focusLost(FocusEvent e) {
 				// fillDialogFields();
 				ComboBoxModel<StockItem> elems = productField.getModel();
 				for (int i = 0; i < elems.getSize(); i++) {
 					StockItem sItem = elems.getElementAt(i);
 					try {
 						if (sItem.getId() == Long.valueOf(barCodeField
 								.getText())) {
 							productField.setSelectedIndex(i);
 						}
 					} catch (NumberFormatException ex) {
 					}
 				}
 			}
 		});
 
 		nameField.setEditable(false);
 		priceField.setEditable(false);
 
 		// == Add components to the panel
 
 		// - list of products
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
 
 	/**
 	 * Add new item to the cart.
 	 */
 	public void addItemEventHandler() {
 		// add chosen item to the shopping cart.
 		StockItem stockItem = getStockItemByBarcode();
 
 		if (stockItem != null) {
 			int quantity;
 			try {
 				quantity = Integer.parseInt(quantityField.getText());
 			} catch (NumberFormatException ex) {
 				quantity = 1;
 			}
 			int totalQuantity = quantity;
 			for (SoldItem sItem : model.getCurrentPurchaseTableModel()
 					.getTableRows()) {
 				if (sItem.getStockItem().getId() == stockItem.getId()) {
 					totalQuantity += sItem.getQuantity();
 				}
 			}
 			if (stockItem.getQuantity() < totalQuantity) {
 				String triedToAdd = String.valueOf(quantity);
 				String leftInStock = String.valueOf(stockItem.getQuantity()
 						- totalQuantity + quantity);
 				String messageText = "Item out of stock! Attempted to add "
 						+ triedToAdd + "; " + leftInStock + " left in stock.";
 				JOptionPane.showMessageDialog(this, messageText,
 						"Warning message", JOptionPane.PLAIN_MESSAGE);
 			} else {
 				model.getCurrentPurchaseTableModel().addItem(
 						new SoldItem(stockItem, quantity));
 			}
 		}
 	}
 
 	/**
 	 * Sets whether or not this component is enabled.
 	 */
 	@Override
 	public void setEnabled(boolean enabled) {
 		this.addItemButton.setEnabled(enabled);
 		this.productField.setEnabled(enabled);
 		this.barCodeField.setEnabled(enabled);
 		this.quantityField.setEnabled(enabled);
 	}
 
 	/**
 	 * Reset dialog fields.
 	 */
 	public void reset() {
 		barCodeField.setText("");
 		productField.setSelectedIndex(0);
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
