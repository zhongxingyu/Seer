 package gui.main;
 
 import gui.common.DialogBox;
 import gui.common.View;
 import gui.inventory.InventoryView;
 import gui.reports.expired.ExpiredReportView;
 import gui.reports.notices.NoticesReportView;
 import gui.reports.productstats.ProductStatsReportView;
 import gui.reports.removed.RemovedReportView;
 import gui.reports.supply.SupplyReportView;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 
 import model.ConcreteItemManager;
 import model.ConcreteProductContainerManager;
 import model.ConcreteProductManager;
 import model.ItemManager;
 import model.ProductContainerManager;
 import model.ProductManager;
 
 @SuppressWarnings("serial")
 public final class GUI extends JFrame implements IMainView {
 
 	public static void main(final String[] args) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				new GUI(args);
 			}
 		});
 	}
 
 	private final IMainController _controller;
 	private JMenuBar _menuBar;
 	private SessionMenu _sessionMenu;
 	private ReportsMenu _reportsMenu;
 
 	private InventoryView _inventoryView;
 	private ItemManager _itemManager;
 	private ProductManager _productManager;
 	private ProductContainerManager _productContainerManager;
 
 	public GUI(String[] args) {
 		super("Home Inventory Tracker");
 
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent evt) {
 				_sessionMenu.exit();
 			}
 		});
 
 		createMenus();
 		createInventoryView();
 
 		_controller = new MainController(this);
		_itemManager = new ConcreteItemManager();
		_productManager = new ConcreteProductManager();
		_productContainerManager = new ConcreteProductContainerManager();
 
 		_sessionMenu.setController(_controller);
 		_reportsMenu.setController(_controller);
 
 		display();
 	}
 
 	@Override
 	public void displayErrorMessage(String message) {
 		displayMessage(message, JOptionPane.ERROR_MESSAGE);
 	}
 
 	@Override
 	public void displayExpiredReportView() {
 		DialogBox dialog = new DialogBox(this, "Expired Items Report");
 		dialog.display(new ExpiredReportView(this, dialog), false);
 	}
 
 	@Override
 	public void displayInformationMessage(String message) {
 		displayMessage(message, JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	@Override
 	public void displayNoticesReportView() {
 		DialogBox dialog = new DialogBox(this, "Notices Report");
 		dialog.display(new NoticesReportView(this, dialog), false);
 	}
 
 	@Override
 	public void displayProductReportView() {
 		DialogBox dialog = new DialogBox(this, "Product Statistics Report");
 		dialog.display(new ProductStatsReportView(this, dialog), false);
 	}
 
 	@Override
 	public void displayRemovedReportView() {
 		DialogBox dialog = new DialogBox(this, "Removed Items Report");
 		dialog.display(new RemovedReportView(this, dialog), false);
 
 	}
 
 	@Override
 	public void displaySupplyReportView() {
 		DialogBox dialog = new DialogBox(this, "N-Month Supply Report");
 		dialog.display(new SupplyReportView(this, dialog), false);
 	}
 
 	@Override
 	public void displayWarningMessage(String message) {
 		displayMessage(message, JOptionPane.WARNING_MESSAGE);
 	}
 
 	public IMainController getController() {
 		return _controller;
 	}
 
 	@Override
 	public ItemManager getItemManager() {
 		return _itemManager;
 	}
 
 	@Override
 	public ProductContainerManager getProductContainerManager() {
 		return _productContainerManager;
 	}
 
 	@Override
 	public ProductManager getProductManager() {
 		return _productManager;
 	}
 
 	private void createInventoryView() {
 		_inventoryView = new InventoryView(this);
 		setContentPane(_inventoryView);
 	}
 
 	private void createMenus() {
 		_sessionMenu = new SessionMenu(this);
 		_sessionMenu.setFont(View.createFont(_sessionMenu.getFont(), View.MenuFontSize));
 
 		_reportsMenu = new ReportsMenu(this);
 		_reportsMenu.setFont(View.createFont(_reportsMenu.getFont(), View.MenuFontSize));
 
 		_menuBar = new JMenuBar();
 		_menuBar.setFont(View.createFont(_menuBar.getFont(), View.MenuFontSize));
 
 		_menuBar.add(_sessionMenu);
 		_menuBar.add(_reportsMenu);
 
 		setJMenuBar(_menuBar);
 	}
 
 	private void display() {
 		pack();
 		setVisible(true);
 	}
 
 	private void displayMessage(final String message, final int messageType) {
 
 		// NOTE: Calling JOptionPane.showMessageDialog from an InputVerifier
 		// does not work (Swing's keyboard focus handling goes bonkers), so
 		// here we call JOptionPane.showMessageDialog using
 		// SwingUtilities.invokeLater
 		// to circumvent this problem in the case displayErrorMessage is
 		// invoked from an InputVerifier.
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				JOptionPane.showMessageDialog(GUI.this, message, "Inventory Tracker",
 						messageType);
 			}
 		});
 	}
 
 }
