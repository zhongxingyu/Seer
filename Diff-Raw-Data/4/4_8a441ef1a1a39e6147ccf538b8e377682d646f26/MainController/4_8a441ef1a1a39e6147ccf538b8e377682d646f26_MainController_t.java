 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import controller.game.GamePanelController;
 
 import java.awt.Dimension;
 import java.awt.event.ItemListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 
 import persistence.XMLFactory;
 
 import model.exception.BusinessLogicException;
 import model.game.Budget;
 import model.game.Game;
 import model.game.Player;
 import model.lab.TechnologyTree;
 import model.production.RawMaterialType;
 import model.production.ValidProductionSequences;
 import model.production.elements.machine.MachineType;
 import model.warehouse.Ground;
 import model.warehouse.PriceMap;
 import model.warehouse.Warehouse;
 import view.MainFrame;
 import view.game.GamePanel;
 import view.game.GroundPanelContainer;
 import view.game.GroundSelectionPanel;
 import view.game.MainPanel;
 
 public class MainController {
 
 	private MainFrame mainFrame;
 
 	private static final String GROUND_PREFIX = "Ground ";
 	private static final String[] DIFFICULTY_LEVELS = { "Easy", "Normal",
 			"Hard" };
 	private static final int[] INITIAL_MONEY = { 1000000, 1500000, 2000000 };
 	private static final float RENT_FACTOR = 0.01f;
 
 	private static final Dimension DIALOG_SIZE = new Dimension(350, 150);
 	private static final Dimension MAIN_PANEL_SIZE = new Dimension(640, 480);
 	private static final Dimension GROUND_SELECTION_PANEL_SIZE = new Dimension(
 			320, 670);
 	protected static final float WIN_VALUE = 50000;
 
 	public MainController(final MainFrame mainFrame) {
 		this.mainFrame = mainFrame;
 
 		// setGamePanel();
 		setMainPanel();
 		// setGroundSelectionPanel();
 
 		mainFrame.setVisible(true);
 		mainFrame.requestFocus();
 
 		// Give focus to GamePanel when selected.
 		mainFrame.addWindowFocusListener(new WindowAdapter() {
 			@Override
 			public void windowGainedFocus(WindowEvent e) {
 				mainFrame.getContentPane().requestFocusInWindow();
 			}
 		});
 	}
 
 	private void setGamePanel(Game game) {
 
 		GroundPanelContainer groundPanel = new GroundPanelContainer(game
 				.getGround());
 		final GamePanel gamePanel = new GamePanel(groundPanel);
 
 		this.mainFrame.setResizable(true);
 		this.mainFrame.maximize();
 		this.setMainFramePanel(gamePanel);
 
 		GamePanelController gamePanelController = new GamePanelController(
 				game, gamePanel, this);
 		System.out.println("is enabled " + this.mainFrame.isEnabled());
 		this.mainFrame.addContainerListener(gamePanelController
 				.getGamePanelRemovedListener());
 	}
 
 	private void setMainPanel() {
 		MainPanel mainPanel = new MainPanel();
 
 		final JComboBox difficultyCombo = mainPanel.getDifficultyCombo();
 		final JTextField nameTextField = mainPanel.getPlayerNameTextArea();
 
 		// Init combo.
 		difficultyCombo.removeAllItems();
 		for (int i = 0; i < DIFFICULTY_LEVELS.length; i++) {
 			difficultyCombo.addItem(DIFFICULTY_LEVELS[i]);
 		}
 
 		ActionListener al = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				int selectedItem = difficultyCombo.getSelectedIndex();
 				int money = INITIAL_MONEY[selectedItem];
 				String name = nameTextField.getText();
 
 				if (!name.isEmpty()) {
 					Budget budget = new Budget(money);
 					Player player = new Player(name, budget);
 					MainController.this.setGroundSelectionPanel(player);
 				} else
 					JOptionPane.showMessageDialog(
 							MainController.this.mainFrame,
 							"Please insert you name", "Empty name",
 							JOptionPane.ERROR_MESSAGE);
 			}
 		};
 
 		mainPanel.addStartActionListener(al);
 		nameTextField.addActionListener(al);
 
 		this.mainFrame.setResizable(false);
 		this.mainFrame.setSize(MAIN_PANEL_SIZE);
 		this.mainFrame.setLocationRelativeTo(null);
 		this.setMainFramePanel(mainPanel);
 	}
 
 	public void setGroundSelectionPanel(Player player) {
 
 		final List<Ground> grounds = loadAllGrounds();
 
 		if (grounds.isEmpty())
 			throw new BusinessLogicException("Empty grounds list");
 
 		GroundSelectionPanel selectionPanel = new GroundSelectionPanel();
 
 		this.initBuyComboFromGroundSelectionPanel(grounds, selectionPanel);
 		this.initGroundSelectionPanelButtons(selectionPanel, player);
 
 		int balance = player.getBudget().getBalance();
 		selectionPanel.getBudgetPanel().setMoneyBalance(balance);
 
 		// Frame configuration.
 		this.mainFrame.setResizable(true);
 		this.mainFrame.setSize(GROUND_SELECTION_PANEL_SIZE);
 		this.mainFrame.setLocationRelativeTo(null);
 		this.setMainFramePanel(selectionPanel);
 	}
 
 	private List<Ground> loadAllGrounds() {
 		try {
 			return new XMLFactory()
 					.loadGrounds("xml/ValidGroundList.xml");
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void initBuyComboFromGroundSelectionPanel(
 			final List<Ground> grounds,
 			final GroundSelectionPanel selectionPanel) {
 
 		final JComboBox buyCombo = selectionPanel.getGroundCombo();
 		buyCombo.removeAllItems();
 
 		// Adds grounds names to the combo.
		for (int i = 0; i < grounds.size(); i++)
			buyCombo.addItem(GROUND_PREFIX + i+1);
 
 		this.buyComboGroundSelectionAction(buyCombo, grounds, selectionPanel);
 
 		// Buy buttons action listener.
 		buyCombo.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent ie) {
 				MainController.this.buyComboGroundSelectionAction(buyCombo,
 						grounds, selectionPanel);
 			}
 		});
 	}
 
 	private void buyComboGroundSelectionAction(final JComboBox buyCombo,
 			final List<Ground> grounds,
 			final GroundSelectionPanel selectionPanel) {
 		// Gets ground selected.
 		int comboIndex = buyCombo.getSelectedIndex();
 		Ground ground = grounds.get(comboIndex);
 
 		// Set ground in ground panel container and show prices.
 		GroundPanelContainer groundPanelContainer = new GroundPanelContainer(
 				ground);
 
 		selectionPanel.setGroundPanelContainer(groundPanelContainer);
 		selectionPanel.setPurchasePrice(ground.getPrice());
 		selectionPanel.setRentPrice((int) (ground.getPrice() * RENT_FACTOR));
 	}
 
 	private void initGroundSelectionPanelButtons(
 			final GroundSelectionPanel selectionPanel, final Player player) {
 
 		selectionPanel.addBuyButtonActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 
 				Ground ground = selectionPanel.getGroundPanelContainer()
 						.getGroundPanel().getGround();
 
 				player.getBudget().decrement(ground.getPrice());
 				Game game = createGameByBuyingGround(ground, player);
 
 				MainController.this.setGamePanel(game);
 			}
 		});
 
 		selectionPanel.addRentButtonActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 
 				Ground ground = selectionPanel.getGroundPanelContainer()
 						.getGroundPanel().getGround();
 				Game game = createGameByRentingGround(ground, player);
 				MainController.this.setGamePanel(game);
 			}
 		});
 
 		selectionPanel.addBackButtonActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				MainController.this.setMainPanel();
 			}
 		});
 	}
 
 	protected Game createGameByRentingGround(Ground ground, Player player) {
 		XMLFactory xmlFactory = new XMLFactory();
 
 		ValidProductionSequences validProductionSequences = new ValidProductionSequences();
 		try {
 			TechnologyTree technologyTree = xmlFactory.loadTechnologies(
 					"xml/ValidProductionSequencesTechnologyList.xml",
 					validProductionSequences);
 			List<MachineType> validProductionMachineTypes = xmlFactory
 					.loadProductionMachines("xml/ValidProductionMachineList.xml");
 			List<MachineType> validQualityControlMachineTypes = xmlFactory
 					.loadQualityControlMachines("xml/ValidQualityControlMachineList.xml");
 			List<RawMaterialType> validRawMaterialTypes = xmlFactory
 					.loadRawMaterialTypes("xml/ValidRawMaterialTypeList.xml");
 			Map<String, Integer> prices = xmlFactory.loadPrices("xml/prices/prices0.xml");
 
 			
 			// TODO: ver priceMap y el null
 			Warehouse warehouse = Warehouse.createRentedWarehouse(ground,
 					player.getBudget(), new PriceMap(prices),
 					validProductionSequences, null);
 
 			return new Game(player, validProductionSequences,
 					validProductionMachineTypes,
 					validQualityControlMachineTypes, validRawMaterialTypes,
 					warehouse, technologyTree);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		
 	}
 
 	private Game createGameByBuyingGround(Ground ground, Player player) {
 		XMLFactory xmlFactory = new XMLFactory();
 
 		ValidProductionSequences validProductionSequences = new ValidProductionSequences();
 		try {
 			TechnologyTree technologyTree = xmlFactory.loadTechnologies(
 					"xml/ValidProductionSequencesTechnologyList.xml",
 					validProductionSequences);
 			List<MachineType> validProductionMachineTypes = xmlFactory
 					.loadProductionMachines("xml/ValidProductionMachineList.xml");
 			List<MachineType> validQualityControlMachineTypes = xmlFactory
 					.loadQualityControlMachines("xml/ValidQualityControlMachineList.xml");
 			List<RawMaterialType> validRawMaterialTypes = xmlFactory
 					.loadRawMaterialTypes("xml/ValidRawMaterialTypeList.xml");
 			Map<String, Integer> prices = xmlFactory.loadPrices("xml/prices/prices0.xml");
 			
 			// TODO: ver priceMap y el null
 			Warehouse warehouse = Warehouse.createPurchasedWarehouse(ground,
 					player.getBudget(), new PriceMap(prices),
 					validProductionSequences, null);
 
 			return new Game(player, validProductionSequences,
 					validProductionMachineTypes,
 					validQualityControlMachineTypes, validRawMaterialTypes,
 					warehouse, technologyTree);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void setMainFramePanel(JPanel panel) {
 		panel.setOpaque(true);
 		this.mainFrame.setContentPane(panel);
 		this.mainFrame.invalidate();
 		this.mainFrame.validate();
 		this.mainFrame.repaint();
 	}
 
 	public static boolean showDialog(String title, String message) {
 		final JDialog dialog = new JDialog((JFrame) null, title, true);
 		final JOptionPane optionPane = new JOptionPane(message,
 				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
 
 		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
 
 			@Override
 			public void propertyChange(PropertyChangeEvent e) {
 				String prop = e.getPropertyName();
 
 				if (dialog.isVisible() && (e.getSource() == optionPane)
 						&& (prop.equals(JOptionPane.VALUE_PROPERTY))) {
 					dialog.setVisible(false);
 				}
 			}
 		});
 
 		dialog.setContentPane(optionPane);
 		dialog.setSize(DIALOG_SIZE);
 		dialog.setLocationRelativeTo(null);
 		dialog.setVisible(true);
 
 		return ((Integer) optionPane.getValue()).intValue() == JOptionPane.YES_OPTION;
 	}
 }
