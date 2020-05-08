 package net.managers;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.Timer;
 
 import state.KitConfig;
 import state.ManagerType;
 import state.OrderConfig;
 import net.GraphicsPanel;
 import net.SyncManager;
 
 @SuppressWarnings("serial")
 public class OrderPanel extends SyncManager implements ActionListener {
 	Map<String, Object> inMap, outMap;
 	FPMControllerPanel cPanel;
 	
 	public OrderPanel() {
 		super(ManagerType.FPMOrder);
 		
 		inMap = new HashMap<String, Object>();
 		outMap = new HashMap<String, Object>();
 		cPanel = new FPMControllerPanel();
 		
 		this.add(cPanel);
 		
 		new Timer(GraphicsPanel.SYNCRATE, this).start();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void actionPerformed(ActionEvent ae) {
 		inMap.clear();
 		inMap.putAll(sendAndReceive(outMap));
 		outMap.clear();
 		
 		cPanel.orderList.removeAll();
 		List<OrderConfig> orderList = (List<OrderConfig>)inMap.get("orders");
 		if(orderList != null)
 			cPanel.orderList.setListData(orderList.toArray());
 		
 		String console = (String)inMap.get("console");
 		if(console != null)
 			cPanel.consoleBox.append(console);
 	}
 	
 	class FPMControllerPanel extends Box implements ActionListener {
 		public JList orderList;
 		
 		List<KitConfig> configList;
 		JComboBox kitConfigBox;
 		JTextField numberField;
 		JButton addOrderButton, refreshButton;
 		JTextArea consoleBox;
 		JScrollPane consoleScroll;
 		
 		public FPMControllerPanel() {
 			super(BoxLayout.Y_AXIS);
 			
 			orderList = new JList();
 			configList = new ArrayList<KitConfig>();
 			kitConfigBox = new JComboBox();
 			numberField = new JTextField();
 			addOrderButton = new JButton("Add Order");
 			refreshButton = new JButton("Refresh Kit Configs");
 			consoleBox = new JTextArea(8, 25);
 			consoleScroll = new JScrollPane(consoleBox);
 			
 			addOrderButton.addActionListener(this);
 			refreshButton.addActionListener(this);
 
 			this.add(new JLabel("Production Pane"));
 			this.add(orderList);
 			
 			Box holdingPane;
 
 			holdingPane = new Box(BoxLayout.X_AXIS);
 			holdingPane.add(new JLabel("Kit Configs"));
			kitConfigBox.setPrototypeDisplayValue("Kit Config");
 			holdingPane.add(kitConfigBox);
 			holdingPane.setMaximumSize(new Dimension(200, 20));
 			this.add(holdingPane);
 			
 			this.add(refreshButton);
 			
 			holdingPane = new Box(BoxLayout.X_AXIS);
 			holdingPane.add(new JLabel("Number of Kits"));
 			holdingPane.add(numberField);
 			holdingPane.setMaximumSize(new Dimension(200, 20));
 			this.add(holdingPane);
 			
 			this.add(addOrderButton);
 			
 			consoleBox.setEditable(false);
 			this.add(consoleScroll);
 			
 			this.add(Box.createVerticalGlue());
 		}
 
 		
 		@SuppressWarnings("unchecked")
 		public void actionPerformed(ActionEvent ae) {
 			String cmd = ae.getActionCommand();
 			
 			if(cmd.equals("Add Order")) {
 				//Send order here.
 				int number = 0;
 				try {
 					number = Integer.parseInt(numberField.getText());
 				} catch(NumberFormatException e) {
 					JOptionPane.showMessageDialog(null,
 							"You need to enter a number in the number box",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				String kitName = (String)kitConfigBox.getSelectedItem();
 				KitConfig selected = null;
 				for(KitConfig k : configList)
 					if(k.kitName.equals(kitName))
 						selected = k;
 				
 				outMap.put("order", new OrderConfig(selected, number));
 				
 			} else if(cmd.equals("Refresh Kit Configs")) {
 				kitConfigBox.removeAllItems();
 				configList = (List<KitConfig>)inMap.get("configlist");
 				
 				if(configList != null)
 					for(KitConfig k : configList)
 						kitConfigBox.addItem(k.kitName);
 				
 				this.revalidate();
 				this.repaint();
 			} else {
 				System.out.println("Undefined action.");
 			}
 		}
 	}
 }
