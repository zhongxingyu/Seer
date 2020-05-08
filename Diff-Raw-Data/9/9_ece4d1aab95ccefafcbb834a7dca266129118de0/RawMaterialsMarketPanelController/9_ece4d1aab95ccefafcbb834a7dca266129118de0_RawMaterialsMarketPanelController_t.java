 package controller.game;
 
 import static model.utils.ArgumentUtils.checkNotNull;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Map.Entry;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.table.DefaultTableModel;
 
 import model.game.Budget;
 import model.game.Player;
 import model.production.RawMaterialType;
 import model.production.StorageArea;
 import model.warehouse.MarketPrices;
 import view.game.GamePanel;
 import view.game.RawMaterialsMarketPanel;
 import view.game.ViewUtils;
 
 public class RawMaterialsMarketPanelController implements Refreshable {
 
 	private Budget budget;
 	private StorageArea storageArea;
 	private List<RawMaterialType> rawMaterialTypes;
 	private MarketPrices rawMaterialPrices;
 
 	// Buying info.
 	private RawMaterialType rawMaterialType;
 	private SpinnerNumberModel rawMaterialQuantityModel;
 	private int rawMaterialPrice;
 
 	private static final String[] columnNames = { "Type", "Quantity" };
 
 	private JSpinner quantitySpinner;
 	private JComboBox buyCombo;
 	private JTable table;
 	private RawMaterialsMarketPanel marketPanel;
 
 	public RawMaterialsMarketPanelController(Player game,
 			final RawMaterialsMarketPanel marketPanel, GamePanel gamePanel) {
 
 		checkNotNull(game, "game");
 		checkNotNull(marketPanel, "marketPanel");
 		checkNotNull(gamePanel, "gamePanel");
 
 		this.storageArea = game.getStorageArea();
 		this.rawMaterialTypes = game.getValidRawMaterialTypes();
 		this.rawMaterialPrices = game.getMarketPrices();
 		this.budget = game.getBudget();
 		this.marketPanel = marketPanel;
 
 		this.budget.addObserver(new Observer() {
 
 			@Override
 			public void update(Observable o, Object arg) {
 
 				int price = (Integer) rawMaterialQuantityModel.getValue()
 						* rawMaterialPrice;
 
 				JButton buyButton = marketPanel.getBuyButton();
 				buyButton.setEnabled(budget.canPurchase(price));
 			}
 		});
 
 		this.quantitySpinner = marketPanel.getQuantitySpinner();
 		this.buyCombo = marketPanel.getBuyCombo();
 		this.table = marketPanel.getStockTable();
 
 		initBuyButtonActionListener(marketPanel);
 		initQuantitySpinner(marketPanel);
 		initBuyCombo(marketPanel);
 		updateTableData();
 	}
 
 	private void setBuyButtonEnabled(final RawMaterialsMarketPanel marketPanel) {
 		int price = (Integer) rawMaterialQuantityModel.getValue()
 				* rawMaterialPrice;
 
 		JButton buyButton = marketPanel.getBuyButton();
 		buyButton.setEnabled(budget.canPurchase(price));
 	}
 
 	private void initQuantitySpinner(final RawMaterialsMarketPanel marketPanel) {
 
 		int value = 1;
 		int minimum = 1;
 		int maximum = Integer.MAX_VALUE;
 		int step = 1;
 
 		this.rawMaterialQuantityModel = new SpinnerNumberModel(value, minimum,
 				maximum, step);
 		this.quantitySpinner.setModel(this.rawMaterialQuantityModel);
 		this.quantitySpinner.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 
 				int price = (Integer) rawMaterialQuantityModel.getValue()
 						* rawMaterialPrice;
 				marketPanel.setRawMaterialPrice(price);
 				setBuyButtonEnabled(marketPanel);
 			}
 		});
 	}
 
 	private void initBuyCombo(final RawMaterialsMarketPanel marketPanel) {
 
 		buyCombo.removeAllItems();
 
 		for (RawMaterialType rtype : this.rawMaterialTypes) {
 			buyCombo.addItem(new RawMaterialTypeComboEntry(rtype));
 		}
 
 		buyComboAction(marketPanel);
 
 		buyCombo.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent ie) {
 				buyComboAction(marketPanel);
 			}
 		});
 	}
 
 	private void buyComboAction(final RawMaterialsMarketPanel marketPanel) {
 
 		RawMaterialTypeComboEntry entry = (RawMaterialTypeComboEntry) buyCombo
 				.getSelectedItem();
 		this.rawMaterialType = entry.getRawMaterialType();
 		this.rawMaterialPrice = this.rawMaterialPrices
 				.getPriceByName(this.rawMaterialType.getName());

		int price = (Integer) rawMaterialQuantityModel.getValue()
				* rawMaterialPrice;

		marketPanel.setRawMaterialPrice(price);
 	}
 
 	private void initBuyButtonActionListener(
 			final RawMaterialsMarketPanel marketPanel) {
 
 		marketPanel.setBuyButtonActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 
 				int price = (Integer) rawMaterialQuantityModel.getValue()
 						* rawMaterialPrice;
 
 				if (budget.canPurchase(price)) {
 
 					budget.decrement(price);
 					storageArea.getRawMaterials().store(rawMaterialType,
 							getRawMaterialQuantity());
 					updateTableData();
 				}
 			}
 		});
 	}
 
 	private int getRawMaterialQuantity() {
 		return (Integer) rawMaterialQuantityModel.getValue();
 	}
 
 	private void updateTableData() {
 		DefaultTableModel model = new DefaultTableModel();
 		table.setModel(model);
 
 		model.addColumn(columnNames[0]);
 		model.addColumn(columnNames[1]);
 
 		Map<RawMaterialType, Integer> rawMaterials = this.storageArea
 				.getRawMaterials().getRawMaterials();
 
 		for (Entry<RawMaterialType, Integer> entry : rawMaterials.entrySet())
 			model.addRow(new Object[] { entry.getKey(), entry.getValue() });
 
 		ViewUtils.autoResizeColWidth(table, model);
 	}
 
 	private class RawMaterialTypeComboEntry {
 
 		private RawMaterialType rawMaterialType;
 
 		public RawMaterialTypeComboEntry(RawMaterialType rawMaterialType) {
 			checkNotNull(rawMaterialType, "rawMaterialType");
 			this.rawMaterialType = rawMaterialType;
 		}
 
 		public RawMaterialType getRawMaterialType() {
 			return this.rawMaterialType;
 		}
 
 		@Override
 		public String toString() {
 			return rawMaterialType.getName();
 		}
 	}
 
 	@Override
 	public void refresh() {
 		this.updateTableData();
		this.buyComboAction(marketPanel);
 	}
 }
