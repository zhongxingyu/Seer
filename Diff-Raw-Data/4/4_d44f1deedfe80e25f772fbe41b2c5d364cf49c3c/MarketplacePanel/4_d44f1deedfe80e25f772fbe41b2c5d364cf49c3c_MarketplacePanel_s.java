 package view;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import controller.Controller;
 
 import model.TradeGood;
 
 public class MarketplacePanel extends JPanel {
 
 	private static final long serialVersionUID = -455183452223286075L;
 	
 	private Controller data;
 	JLabel[][] labels;
 	JLabel nameLabel, moneyLabel;
 	
 	public MarketplacePanel(Controller data) {
 		this.data = data;
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridLayout(11, 7));
 		setPreferredSize(new Dimension(600, 400));
 		
 		String[][] view = data.getLocation().getMarketplace().getView(data.getShip());
 		labels = new JLabel[view.length][view[0].length];
 		TradeGood[] goods = TradeGood.values();
 		
 		panel.add(new JLabel("Trade Good"));
 		panel.add(new JLabel("Buy Price"));
 		panel.add(new JLabel("Sell Price"));
 		panel.add(new JLabel("Available"));
 		panel.add(new JLabel("Owned"));
 		panel.add(new JLabel("Buy"));
 		panel.add(new JLabel("Sell"));
 		for(int i = 0; i < view.length; i++) {
 			for(int j = 0; j < view[i].length; j++) {
 				labels[i][j] = new JLabel(view[i][j]);
 				panel.add(labels[i][j]);
 			}
 			JButton button = new JButton("Buy");
 			button.addActionListener(new BuyListener(goods[i]));
 			panel.add(button);
 			button = new JButton("Sell");
 			button.addActionListener(new SellListener(goods[i]));
 			panel.add(button);
 		}
 		nameLabel = new JLabel("Planet " + data.getLocation().getName()+ "     " + data.getPlayer().getName());
 		add(nameLabel);
 		moneyLabel = new JLabel("$" + data.getMoney());
 		add(moneyLabel);
 		// This is added on 11/07/12 by An Pham
 		// This is for testing purpose
 		JButton refuelButton = new JButton("Refuel");
 		add(refuelButton);
 		refuelButton.addActionListener(new refuelBtnListener());
 		// End of An Pham's test
 		add(panel);
 	}
 	
 	public void updateGoods() {
 		String[][] view = data.getLocation().getMarketplace().getView(data.getShip());
 		for(int i = 0; i < view.length; i++) {
 			for(int j = 0; j < view[i].length; j++) {
 				labels[i][j].setText(view[i][j]);
 			}
 		}
 		moneyLabel.setText("$" + data.getMoney());
 	}
 	
 	public void changeMarketplace() {
 		updateGoods();
 		nameLabel.setText("Planet " + data.getLocation().getName()+ "     " + data.getPlayer().getName());
 	}
 	
 	private class BuyListener implements ActionListener {
 		
 		TradeGood good;
 		
 		public BuyListener(TradeGood good) {
 			this.good = good;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			try {
 				data.buyGood(good);
 				updateGoods();
 			} catch (Exception ex) {
 				JOptionPane.showMessageDialog(null, ex.getMessage());
 			}
 		}
 	}
 
 	private class SellListener implements ActionListener {
 	
 		TradeGood good;
 		
 		public SellListener(TradeGood good) {
 			this.good = good;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			try {
 				data.sellGood(good);
 				updateGoods();
 			} catch (Exception ex) {
 				JOptionPane.showMessageDialog(null, ex.getMessage());
 			}
 		}
 	}
 	private class refuelBtnListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			data.setMoney(data.getMoney() - 50);
 			updateGoods();
 			repaint();
 			data.getShip().setFuel(data.getShip().getType().MAX_DISTANCE);
 		}
 		
 	}
 }
