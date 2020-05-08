 package manager.panel;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 
 import manager.FactoryProductionManager;
 import manager.panel.KitsListPanel.KitSelectHandler;
 import manager.util.ClickablePanel;
 import manager.util.ClickablePanelClickHandler;
 import manager.util.CustomButton;
 import manager.util.ListPanel;
 import manager.util.OverlayInternalFrame;
 import manager.util.WhiteLabel;
 import factory.KitConfig;
 import factory.Order;
 
 /**
 *
 * @author Shalynn Ho, Harry Trieu, Matt Zecchini, Peter Zhang
 */
 
 public class FactoryProductionManagerPanel extends OverlayInternalFrame {
 	// Width of the JPanel
 	private static final int PANEL_WIDTH = 300;
 	
 	private static final String FULL_PANEL = "full";
 	private static final String SIMPLE_PANEL = "simple";
 
 	// A reference to the FactoryProductionManager client
 	private FactoryProductionManager fpmClient;
 	
 	// Stores the selected kitConfig for a new order
 	private KitConfig selectedKit;
 	// Stores the selected Order
 	private Order selectedOrder;
 	
 	/** JComponents **/
 	// Displays current schedule of orders
 	private SpinnerNumberModel spinnerModel; 
 	private JSpinner quantitySpinner;
 	private CustomButton orderButton;
 	private KitsListPanel kitsPanel;
 	private JScrollPane kitsScrollPane;
 	private OrdersListPanel ordersPanel;
 	private JScrollPane ordersScrollPane;
 	private JPanel quantityOrderPanel;
 	
 	private WhiteLabel currentKitCount;
 	private WhiteLabel currentOrderCount;
 
 	private JPanel fullPanel = new JPanel();
 	private JPanel simplePanel = new JPanel();
 	private JPanel wrapperPanel = new JPanel();
 	private CardLayout cl = new CardLayout();
 
 	private PanelMouseListener panelListener = new PanelMouseListener();
 	
 	private boolean visible = true;
 	private int height;
 	
 	/**
 	 * Constructor
 	 * @param f a reference to the FactoryProductionManager client.
 	 * @param height of the JFrame
 	 */
 	public FactoryProductionManagerPanel(FactoryProductionManager f, int height) {
 		super();
 		fpmClient = f;
 		
 		this.height = height;
 		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		addMouseListener(panelListener);
 		
 		fullPanel.setLayout(new BoxLayout(fullPanel, BoxLayout.PAGE_AXIS));
 		fullPanel.setOpaque(false);
 		fullPanel.setVisible(true);
 
 		simplePanel.setLayout(new BoxLayout(simplePanel, BoxLayout.PAGE_AXIS));
 		simplePanel.setOpaque(false);
 		simplePanel.setVisible(true);
 
 		wrapperPanel.setLayout(cl);
 		wrapperPanel.add(fullPanel, FULL_PANEL);
 		wrapperPanel.add(simplePanel, SIMPLE_PANEL);
 		wrapperPanel.setOpaque(false);
 		wrapperPanel.setVisible(true);
 		add(wrapperPanel);
 
 		// default to show simple Panel on start up
 		panelListener.mouseExited(null);
 
 		// Setup KitsListPanel
 		kitsPanel = new KitsListPanel("Select a Kit to order... ", new KitSelectHandler() {
 			@Override
 			public void onKitSelect(KitConfig kc) {
 				selectedKit = kc;
 			}
 		});
 		
 		kitsPanel.setVisible(true);
 		kitsPanel.setBackground(new Color(0, 0, 0, 30));
 		addMouseListeners(kitsPanel);
 		
 		kitsScrollPane = new JScrollPane(kitsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		kitsScrollPane.setOpaque(false);
 		kitsScrollPane.getViewport().setOpaque(false);
 		for (int i = 0; i < kitsScrollPane.getComponentCount(); i++) {
 			kitsScrollPane.getComponents()[i].addMouseListener(new PanelMouseListener());
 		}
 		kitsScrollPane.setPreferredSize(new Dimension(PANEL_WIDTH,height/2));
 		
 		
 		fullPanel.add(kitsScrollPane);
 		
 		//Setup new panel to hold the Spinner and the OrderButton
 		quantityOrderPanel = new JPanel();
 		quantityOrderPanel.setLayout(new BoxLayout(quantityOrderPanel, BoxLayout.X_AXIS));
 		
 		
 		// Setup JSpinner
 		spinnerModel = new SpinnerNumberModel(0, 0, 1000, 1);
 	    quantitySpinner = new JSpinner(spinnerModel);
 	    
 		quantitySpinner.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
 	    
 	    //add spinner to quantityOrderPanel
 		quantityOrderPanel.add(quantitySpinner);
 		quantityOrderPanel.add(Box.createHorizontalStrut(8));
 		quantityOrderPanel.setOpaque(false);
 		
 	    
 	    // Add listener to nested components - for disappearing panel
 		for (int i = 0; i < quantitySpinner.getComponentCount(); i++) {
 			quantitySpinner.getComponents()[i].addMouseListener(panelListener);
 		}
 		((JSpinner.DefaultEditor)quantitySpinner.getEditor()).getTextField().addMouseListener(panelListener);
 		
 		// Setup order button
 		orderButton = new CustomButton("Order Kits >", new OrderButtonListener());
 		orderButton.addMouseListener(panelListener);
 		
 		//add OrderButton to quantityOrderPanel adjacent to Spinner
 		quantityOrderPanel.add(orderButton);
 		
 		
 		fullPanel.add(quantityOrderPanel);
 		
 		// Setup OrdersListPanel
 		OrdersListPanel.OrderSelectHandler selectHandler = new OrdersListPanel.OrderSelectHandler() {
 			@Override
 			public void onOrderSelect(Order o) {
 				selectedOrder = o;
 			}
 		};
 		ordersPanel = new OrdersListPanel("Current order queue:", selectHandler);
 		ordersPanel.setVisible(true);
 		ordersPanel.setBackground(new Color(0, 0, 0, 30));
 		
 		ordersScrollPane = new JScrollPane(ordersPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		ordersScrollPane.setOpaque(false);
 		ordersScrollPane.getViewport().setOpaque(false);
 		for (int i = 0; i < ordersScrollPane.getComponentCount(); i++) {
 			ordersScrollPane.getComponents()[i].addMouseListener(new PanelMouseListener());
 		}		
 		
 		
 		ordersScrollPane.setPreferredSize(new Dimension(PANEL_WIDTH, height/2));
 		
 		
 		fullPanel.add(ordersScrollPane);
 		
 		// Add mouseListener to second-level components
 		for (int i = 0; i < getComponentCount(); i++) {
 			fullPanel.getComponents()[i].addMouseListener(panelListener);
 		}
 		
 		currentKitCount = new WhiteLabel("0");
 		currentKitCount.setFont(new Font("Arial", Font.PLAIN, 40));
 		WhiteLabel currentKitText = new WhiteLabel(
 				"<html>kits<br />remaining<br />in the order</html>");
 
 		currentOrderCount = new WhiteLabel("0");
 		currentOrderCount.setFont(new Font("Arial", Font.PLAIN, 40));
 		WhiteLabel currentOrderText = new WhiteLabel(
 				"<html>orders<br />remaining</html>");
 
 		WhiteLabel rollOverText = new WhiteLabel(
 				"<html><br /><br /><br />&lt;&lt;&lt;<br />mouseover<br />for more<br />info</html>");
 		rollOverText.setForeground(new Color(220, 220, 220));
 
 		simplePanel.add(currentKitCount);
 		simplePanel.add(currentKitText);
 		simplePanel.add(currentOrderCount);
 		simplePanel.add(currentOrderText);
 		simplePanel.add(rollOverText);
 	}
 	
 	/**
 	 * This function is called by FactoryProductionManager whenever KitConfigs are updated.
 	 * @param kc ArrayList of current KitConfigs
 	 */
 	public void updateKitConfigs(ArrayList<KitConfig> kc) {
 		kitsPanel.updateList(kc);
 		addMouseListeners(kitsPanel);
 	}
 	
 	/**
 	 * This function is called by FactoryProductionManager whenever orders are updated.
 	 * @param o ArrayList of orders
 	 */
 	public void updateOrders(ArrayList<Order> o) {
		if (o.size() > 0) {
			updateSimplePanelCount(o.size(), o.get(0).getNumKits());
		} else {
			// order is 0
			updateSimplePanelCount(0, 0);
		}
 		ordersPanel.updateList(o);
 		addMouseListeners(ordersPanel);
 	}
 	
 	public void updateSimplePanelCount(int orderCount, int kitCount) {
 		currentOrderCount.setText(String.valueOf(orderCount));
 		if (Integer.valueOf(currentKitCount.getText()) == 0) {
 			currentKitCount.setText(String.valueOf(kitCount));
 		}
 	}
 
 	public void decreaseCurrentKitCount() {
 		int kitCount = Integer.valueOf(currentKitCount.getText());
 		kitCount--;
 		currentKitCount.setText(String.valueOf(kitCount));
 	}
 
 	/**
 	 * Adds this FPMPanel as the mouseListener for the components of the ListPanel
 	 * @param panel - KitsListPanel or OrdersListPanel
 	 */
 	private void addMouseListeners(ListPanel<?> panel) {
 		for(ClickablePanel p : panel.getPanels().values()) {
 			p.removeMouseListener(panelListener);
 			p.addMouseListener(panelListener);
 		}
 	}
 	
 	private class OrderButtonListener implements ClickablePanelClickHandler {
 		@Override
 		public void mouseClicked() {
 			KitConfig kitToMake;
 			if(selectedKit != null) {
 				kitToMake = selectedKit;
 				
 				//Set variable quantityToMake equal to number user enters in spinnerModel
 				int quantityToMake = spinnerModel.getNumber().intValue();
 				
 				//Creates new Order and passes it the kit the User selects and the quantity to make
 				Order newOrder = new Order(kitToMake, quantityToMake);
 				
 				//sends message to FCS with order info
 				fpmClient.createOrder(newOrder);
 			}
 		}
 	}
 	
 	private class PanelMouseListener implements MouseListener {
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			cl.show(wrapperPanel, FULL_PANEL);
 			setPanelSize(PANEL_WIDTH, height);
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			cl.show(wrapperPanel, SIMPLE_PANEL);
 			setPanelSize(PANEL_WIDTH / 3, height);
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent e) { }
 
 		@Override
 		public void mousePressed(MouseEvent e) { }
 
 		@Override
 		public void mouseReleased(MouseEvent e) { }
 	}
 }
