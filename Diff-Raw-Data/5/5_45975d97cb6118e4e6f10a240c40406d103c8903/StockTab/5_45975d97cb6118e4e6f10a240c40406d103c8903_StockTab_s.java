 package ee.ut.math.tvt.salessystem.ui.tabs;
 
 import ee.ut.math.tvt.salessystem.domain.controller.SalesDomainController;
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.JTableHeader;
 
 public class StockTab {
 
 	private JButton addItem;
 
 	private JButton modifyItem;
 
 	private JButton confirmItemAdd;
 
 	private JButton cancelItemAdd;
 
 	private JButton confirmItemModify;
 
 	private JButton cancelItemModify;
 
 	private JFrame addItemFrame;
 
 	private JFrame modifyItemFrame;
 
 	private JPanel addItemPanel;
 
 	private JPanel modifyItemPanel;
 
 	private JTextField idField;
 
 	private JTextField nameField;
 
 	private JTextField descField;
 
 	private JTextField priceField;
 
 	private JTextField quantityField;
 
 	private final SalesSystemModel model;
 
 	private final SalesDomainController domainController;
 
 	public StockTab(SalesDomainController controller, SalesSystemModel model) {
 		this.domainController = controller;
 		this.model = model;
 	}
 
 	// warehouse stock tab - consists of a menu and a table
 	public Component draw() {
 		JPanel panel = new JPanel();
 		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		GridBagLayout gb = new GridBagLayout();
 		GridBagConstraints gc = new GridBagConstraints();
 		panel.setLayout(gb);
 		gc.anchor = GridBagConstraints.NORTHWEST;
 		gc.gridwidth = GridBagConstraints.REMAINDER;
 		gc.weightx = 0;
 		gc.weighty = 0;
 		panel.add(drawStockMenuPane(), gc);
 		gc.weightx = 1.0d;
 		gc.weighty = 1.0;
 		gc.fill = GridBagConstraints.BOTH;
 		panel.add(drawStockMainPane(), gc);
 		return panel;
 	}
 
 	// warehouse menu
 	private Component drawStockMenuPane() {
 		JPanel panel = new JPanel();
 		GridBagConstraints gc = getConstraintsForMenuButtons();
 		GridBagLayout gb = new GridBagLayout();
 		panel.setLayout(gb);
 		addItem = createAddItemButton();
 		modifyItem = createModifyItemButton();
 		panel.add(addItem, gc);
 		panel.add(modifyItem, gc);
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
 
 	// Creates the button "Add"
 	private JButton createAddItemButton() {
 		JButton b = new JButton("Add");
 		b.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addItemButtonClicked();
 			}
 		});
 		return b;
 	}
 
 	// Creates the button "Modify"
 	private JButton createModifyItemButton() {
 		JButton b = new JButton("Modify");
 		b.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				modifyItemButtonClicked();
 			}
 		});
 		return b;
 	}
 
 	// Creates the button "Confirm" for item adding
 	private JButton createItemAddConfirmButton() {
 		JButton b = new JButton("Confirm");
 		b.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				confirmItemAddButtonClicked();
 			}
 		});
 		return b;
 	}
 
 	// Creates the button "Cancel" for item adding
 	private JButton createItemAddCancelButton() {
 		JButton b = new JButton("Cancel");
 		b.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toggleButtonsEnable(true);
 				addItemFrame.dispose();
 			}
 		});
 		return b;
 	}
 
 	// Creates the button "Confirm" for item modifying
 	private JButton createItemModifyConfirmButton() {
 		JButton b = new JButton("Confirm");
 		b.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				confirmItemModifyButtonClicked();
 			}
 		});
 		return b;
 	}
 
 	// Creates the button "Cancel" for item modifying
 	private JButton createItemModifyCancelButton() {
 		JButton b = new JButton("Cancel");
 		b.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				toggleButtonsEnable(true);
 				modifyItemFrame.dispose();
 			}
 		});
 		return b;
 	}
 
 	/** Event handler for the <code>add item</code> event. */
 	protected void addItemButtonClicked() {
 		try {
 			popAddItemBox();
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "Incorrect input, try again", "Warning", JOptionPane.WARNING_MESSAGE);
 		}
 	}
 
 	/** Event handler for the <code>modify item</code> event. */
 	protected void modifyItemButtonClicked() {
 		try {
 			popModifyItemBox();
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "Incorrect input, try again", "Warning", JOptionPane.WARNING_MESSAGE);
 		}
 	}
 
 	/** Event handler for the <code>confirm item add</code> event. */
 	protected void confirmItemAddButtonClicked() {
 		try {
 			String itemName = nameField.getText();
			String itemDesc = nameField.getText();
 			Double itemPrice = Double.parseDouble(priceField.getText());
 			int itemQuantity = Integer.parseInt(quantityField.getText());
 
 			if (itemName.isEmpty() || itemDesc.isEmpty() || itemPrice < 0 || itemQuantity <= 0) {
 				throw new Exception();
 			}
 			StockItem newItem = new StockItem(itemName, itemDesc, itemPrice, itemQuantity);
 
 			model.getWarehouseTableModel().addItem(newItem);
 			domainController.addNewStockItem(newItem);
 			toggleButtonsEnable(true);
 			addItemFrame.dispose();
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "Incorrect input, try again", "Warning", JOptionPane.WARNING_MESSAGE);
 		}
 	}
 
 	/** Event handler for the <code>confirm item modify</code> event. */
 	protected void confirmItemModifyButtonClicked() {
 		try {
 			Long itemId = Long.parseLong(idField.getText());
 			String itemName = nameField.getText();
			String itemDesc = nameField.getText();
 			Double itemPrice = Double.parseDouble(priceField.getText());
 			int itemQuantity = Integer.parseInt(quantityField.getText());
 
 			if (itemId < 0 || itemName.isEmpty() || itemDesc.isEmpty() || itemPrice < 0 || itemQuantity <= 0) {
 				throw new Exception();
 			}
 			StockItem item = new StockItem(itemId, itemName, itemDesc, itemPrice, itemQuantity);
 
 			model.getWarehouseTableModel().modifyItem(item);
 			domainController.modifyStockItem(item);
 			toggleButtonsEnable(true);
 			modifyItemFrame.dispose();
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "Incorrect input, try again", "Warning", JOptionPane.WARNING_MESSAGE);
 		}
 	}
 
 	// Add item popup screen
 	private void popAddItemBox() {
 		addItemPanel = new JPanel(new GridLayout(5, 2));
 
 		nameField = new JTextField();
 		descField = new JTextField();
 		priceField = new JTextField();
 		quantityField = new JTextField();
 
 		addItemFrame = new JFrame("Add item");
 		addItemFrame.setSize(new Dimension(320, 140));
 		addItemFrame.setLocationRelativeTo(null);
 		addItemFrame.setResizable(false);
 		addItemFrame.add(addItemPanel);
 		addItemFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 		// add item name textlabel and textfield to panel
 		addItemPanel.add(new JLabel("Name: "));
 		addItemPanel.add(nameField);
 		// add item description textlabel and textfield to panel
 		addItemPanel.add(new JLabel("Description: "));
 		addItemPanel.add(descField);
 		// add item price textlabel and textfield to panel
 		addItemPanel.add(new JLabel("Price: "));
 		addItemPanel.add(priceField);
 		// add item amount textlabel and textfield to panel
 		addItemPanel.add(new JLabel("Quantity: "));
 		addItemPanel.add(quantityField);
 
 		// Initializing confirm and cancel buttons
 		confirmItemAdd = createItemAddConfirmButton();
 		cancelItemAdd = createItemAddCancelButton();
 
 		// Adding the buttons
 		addItemPanel.add(confirmItemAdd);
 		addItemPanel.add(cancelItemAdd);
 
 		addItemFrame.setVisible(true);
 		toggleButtonsEnable(true);
 	}
 
 	// Modify item popup screen
 	private void popModifyItemBox() {
 		modifyItemPanel = new JPanel(new GridLayout(6, 2));
 
 		idField = new JTextField();
 		nameField = new JTextField();
 		descField = new JTextField();
 		priceField = new JTextField();
 		quantityField = new JTextField();
 
 		modifyItemFrame = new JFrame("Modify item");
 		modifyItemFrame.setSize(new Dimension(320, 160));
 		modifyItemFrame.setLocationRelativeTo(null);
 		modifyItemFrame.setResizable(false);
 		modifyItemFrame.add(modifyItemPanel);
 		modifyItemFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 		// modify item id textlabel and textfield to panel
 		modifyItemPanel.add(new JLabel("Id: "));
 		modifyItemPanel.add(idField);
 		// modify item name textlabel and textfield to panel
 		modifyItemPanel.add(new JLabel("Name: "));
 		modifyItemPanel.add(nameField);
 		// modify item description textlabel and textfield to panel
 		modifyItemPanel.add(new JLabel("Description: "));
 		modifyItemPanel.add(descField);
 		// modify item price textlabel and textfield to panel
 		modifyItemPanel.add(new JLabel("Price: "));
 		modifyItemPanel.add(priceField);
 		// modify item amount textlabel and textfield to panel
 		modifyItemPanel.add(new JLabel("Quantity: "));
 		modifyItemPanel.add(quantityField);
 
 		// Initializing confirm and cancel buttons
 		confirmItemModify = createItemModifyConfirmButton();
 		cancelItemModify = createItemModifyCancelButton();
 
 		// Adding the buttons
 		modifyItemPanel.add(confirmItemModify);
 		modifyItemPanel.add(cancelItemModify);
 
 		modifyItemFrame.setVisible(true);
 		toggleButtonsEnable(true);
 	}
 
 	// toggle buttons enable
 	private void toggleButtonsEnable(boolean enable) {
 		addItem.setEnabled(enable);
 		modifyItem.setEnabled(enable);
 	}
 
 	// The constraints that control the layout of the buttons in the purchase menu
 	private GridBagConstraints getConstraintsForMenuButtons() {
 		GridBagConstraints gc = new GridBagConstraints();
 		gc.weightx = 0.5;
 		gc.anchor = GridBagConstraints.NORTHWEST;
 		gc.gridwidth = GridBagConstraints.RELATIVE;
 		return gc;
 	}
 }
