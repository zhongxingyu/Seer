 package ee.ut.math.tvt.salessystem.ui.panels;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 
 import ee.ut.math.tvt.salessystem.domain.data.SoldItem;
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 
 /**
  * Purchase pane + shopping cart tabel UI.
  */
 public class PurchaseItemPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	private JComboBox<Object> barCodeComboBox;
 
 	private JTextField quantityField;
 
 	private JTextField nameField;
 
 	private JTextField priceField;
 
 	private JButton addItemButton;
 
 	// Warehouse model
 	private final SalesSystemModel model;
 
 	/**
 	 * Constructs new purchase item panel.
 	 * 
 	 * @param model
 	 *          composite model of the warehouse and the shopping cart.
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
 		panel.setLayout(new GridLayout(5, 2));
 		panel.setBorder(BorderFactory.createTitledBorder("Product"));
 		// Initialize combobox
 		barCodeComboBox = new JComboBox<Object>(getStockList());
 		// Initialize the textfields
 		quantityField = new JTextField("1");
 		nameField = new JTextField();
 		priceField = new JTextField();
 		// Fill the dialog fields after a product has been selected
 		barCodeComboBox.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fillDialogFields();
 			}
 		});
 		nameField.setEditable(false);
 		priceField.setEditable(false);
 		// == Add components to the panel
 		// - bar code
 		panel.add(new JLabel("Bar code:"));
 		panel.add(barCodeComboBox);
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
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addItemEventHandler();
 			}
 		});
 		panel.add(addItemButton);
 		return panel;
 	}
 
 	// Format a StringArray for the combobox
 	private String[] getStockList() {
 		List<String> warehouseItems = new ArrayList<>();
 		for (StockItem each : model.getWarehouseTableModel().getTableRows()) {
 			warehouseItems.add(each.getId() + " - " + each.getName());
 		}
 		return warehouseItems.toArray(new String[warehouseItems.size()]);
 	}
 
 	// Fill dialog with data from the "database".
 	public void fillDialogFields() {
 		StockItem stockItem = getStockItemByBarcode();
 		if (stockItem != null) {
 			nameField.setText(stockItem.getName());
 			String priceString = String.valueOf(stockItem.getPrice());
 			priceField.setText(priceString);
 		}
 		else {
 			reset();
 		}
 	}
 
 	// Search the warehouse for a StockItem with the bar code entered
 	// to the barCode textfield.
 	private StockItem getStockItemByBarcode() {
 		try {
 			int code = model.getWarehouseTableModel().getTableRows().get(barCodeComboBox.getSelectedIndex()).getId().intValue();
 			return model.getWarehouseTableModel().getItemById(code);
 		}
 		catch (NumberFormatException ex) {
 			return null;
 		}
 		catch (NoSuchElementException ex) {
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
 			}
 			catch (NumberFormatException ex) {
 				quantity = 1;
 			}
 			SoldItem existingItem = getExistingSoldItem(stockItem);
 
			if (existingItem != null && existingItem.getQuantity() + quantity <= 0 || quantity <= 0) {
 				JOptionPane.showMessageDialog(
 					new JFrame(),
					"Item \"" + stockItem.getName() + "\" amount can't go to negative",
 					"Warning",
 					JOptionPane.WARNING_MESSAGE);
 			} else {
 				int quantityToCheck = existingItem != null ? existingItem.getQuantity() + quantity : quantity;
 
 				if (checkWareHouseInventory(stockItem, quantityToCheck)) {
 					model.getCurrentPurchaseTableModel().addItem(new SoldItem(stockItem, quantity), existingItem);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets whether or not this component is enabled.
 	 */
 	@Override
 	public void setEnabled(boolean enabled) {
 		this.addItemButton.setEnabled(enabled);
 		this.barCodeComboBox.setEnabled(enabled);
 		this.quantityField.setEnabled(enabled);
 	}
 
 	/**
 	 * Reset dialog fields.
 	 */
 	public void reset() {
 		quantityField.setText("1");
 		nameField.setText("");
 		priceField.setText("");
 	}
 
 	private SoldItem getExistingSoldItem(StockItem stockItem) {
 		try {
 			return model.getCurrentPurchaseTableModel().getItemById(stockItem.getId());
 		}
 		catch (NoSuchElementException e) {
 			return null;
 		}
 	}
 
 	private boolean checkWareHouseInventory(StockItem stockItem, int quantity) {
 		StockItem item = model.getWarehouseTableModel().getItemById(stockItem.getId());
 
 		if (item.getQuantity() < quantity) {
 			JOptionPane.showMessageDialog(
 				new JFrame(),
 				"Item \"" + item.getName() + "\" amount exceeds item quantity in the Warehouse",
 				"Warning",
 				JOptionPane.WARNING_MESSAGE);
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * === Ideally, UI's layout and behavior should be kept as separated as
 	 * possible. If you work on the behavior of the application, you don't want
 	 * the layout details to get on your way all the time, and vice versa. This
 	 * separation leads to cleaner, more readable and better maintainable code. In
 	 * a Swing application, the layout is also defined as Java code and this
 	 * separation is more difficult to make. One thing that can still be done is
 	 * moving the layout-defining code out into separate methods, leaving the more
 	 * important methods unburdened of the messy layout code. This is done in the
 	 * following methods.
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
