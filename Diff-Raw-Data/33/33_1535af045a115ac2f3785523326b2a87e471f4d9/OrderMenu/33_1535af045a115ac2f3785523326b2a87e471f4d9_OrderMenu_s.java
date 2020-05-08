 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.text.*;
 import java.util.*;
 
 public class OrderMenu extends BasicDialog {
 	private DefaultListModel<Dish> dishListModel = new DefaultListModel<Dish>();
 	private JList<Dish> list = new JList<Dish>(dishListModel);
 	private JButton newDish = new JButton("Add dish");
 	private JButton removeDish = new JButton("Remove dish");
 	private JFrame parent;
 	private ArrayList<Dish> dish = new ArrayList<Dish>();
 	private JTextField deliveryAdress = new JTextField();
 	private JTextField day = new JTextField(2);
 	private JTextField month = new JTextField(2);
 	private JTextField year = new JTextField(4);
 	private JTextField hour = new JTextField(2);
 	private JTextField minutes = new JTextField(2);
 	private JTextField priceField = new JTextField();
 	private double price = 0;
 	
 	public OrderMenu(JFrame parent) {
 		super(parent, "Order menu");
 	    add(new ListPanel(), BorderLayout.CENTER);
 	    add(getButtonpanel(), BorderLayout.SOUTH);
 	    add(new newDish(), BorderLayout.NORTH);
 	    pack();
 	}
	private class CustomerDatapanel extends JPanel {
		public CustomerDatapanel() {
			setLayout(new GridLayout(4, 6, 5, 5));
			add(new JLabel("Delivery date (dd.mm.yyyy): ", JLabel.RIGHT));
 		    add(year);
 		    add(new JLabel("-", JLabel.RIGHT));
 		    add(month);
 		    add(new JLabel("-", JLabel.RIGHT));
 		    add(day);
 		    add(new JLabel("Time:", JLabel.RIGHT));
 		    add(hour);
 		    add(new JLabel(":", JLabel.RIGHT));
 		    add(minutes);
 		    add(new JLabel("", JLabel.RIGHT));
 		    add(new JLabel("", JLabel.RIGHT));//moves deliveryAdress to the next line
 		    add(new JLabel("Delivery adress: ", JLabel.RIGHT));
 		    add(deliveryAdress);
 		    add(new JLabel("Price: ", JLabel.RIGHT));
 		    add(priceField);
 		}
 	}
 	private class newDish extends JPanel {
 		public newDish() {
 			setLayout(new BorderLayout());
 			add(new JLabel(" "), BorderLayout.NORTH);
 			add(newDish, BorderLayout.WEST);
 			add(removeDish, BorderLayout.EAST);
 			ButtonListener listener = new ButtonListener();
 			newDish.addActionListener(listener);
 			removeDish.addActionListener(listener);
 		}
 	}
 	private class ListPanel extends JPanel {
 		public ListPanel() {
 			setLayout(new BorderLayout());
 			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			JScrollPane listScroller = new JScrollPane(list);
 			add(listScroller, BorderLayout.CENTER);
 		}
 	}
 	private class ButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent event) {
 			String button = event.getActionCommand();
 			if(event.getSource() == newDish) {
 				DishChooser dishChooser = new DishChooser(parent);
 				ArrayList <Dish> newDish = dishChooser.getDish();
 				for(Dish aDish : newDish) {
 					dishListModel.addElement(aDish);
 					dish.add(aDish);
 				}
 				list.clearSelection();
 				for(Dish aDish : dish) {
 					price += aDish.getPrice();
 				}
 				priceField.setText(Double.toString(price));
 			} else if(event.getSource() == removeDish) {
 				int index = list.getSelectedIndex();
 				dishListModel.remove(index);
 				dish.remove(index);
 			}
 		}
 	}
 	public Order editOrder(Order order) {
 		String deliveryTime;
 		StringTokenizer time = new StringTokenizer(order.getDeliveryTime(), "- :");
 		deliveryAdress.setText(order.getDeliveryAdress());
 		day.setText(time.nextToken());
 		month.setText(time.nextToken());
 		year.setText(time.nextToken());
 		hour.setText(time.nextToken());
 		minutes.setText(time.nextToken());
 		priceField.setText(Double.toString(order.getPrice()));
 		price = order.getPrice();
 		ArrayList <Dish> newDish = order.getOrderContent();
 		for(Dish aDish : newDish) {
 			dishListModel.addElement(aDish);
 			dish.add(aDish);
 		}
 		setOk(false);
 		pack();
 	    setVisible(true);
 	    if (isOk()) {
	    	deliveryTime = "" + day.getText() + "-" + month.getText() + "-" + year.getText() + " " + hour.getText() + ":" + minutes.getText() + ":00";
 	    	order = new Order(1, 1, "Registered", "Placeholder", deliveryTime, deliveryAdress.getText(), dish, price); //orderid, customer id and ordertime placeholders
 	    	return order;
 	    } else {
 	    	return  null;
 	    }
 	}
 	public Order getOrder() {
 		setOk(false);
 		pack();
 	    setVisible(true);
 		Order order;
 	    if (isOk()) {
	    	String deliveryTime = "" + day.getText() + "-" + month.getText() + "-" + year.getText() + " " + hour.getText() + ":" + minutes.getText() + ":00";
 	    	order = new Order(1, 1, "Registered", "Placeholder", deliveryTime, deliveryAdress.getText(), dish, price); //orderid, customer id and ordertime placeholders
 	    	return order;
 	    } else {
 	    	return  null;
 	    }
 	}
 }
