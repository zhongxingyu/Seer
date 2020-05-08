 package org.example.exchange.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.example.exchange.api.Exchange;
 import org.example.exchange.api.ExchangeListener;
 import org.example.model.orders.Orders.Order;
 
 import aQute.bnd.annotation.component.Activate;
 import aQute.bnd.annotation.component.Component;
 import aQute.bnd.annotation.component.Reference;
 import aQute.bnd.annotation.metatype.Configurable;
 import aQute.bnd.annotation.metatype.Meta;
 
 @Component(name = "org.example.exchange.server", designateFactory = ExchangeImpl.Config.class)
 public class ExchangeImpl implements Exchange {
 	
 	static interface Config {
 		@Meta.AD(description = "The name of the exchange.", required = false, deflt = "NYSE")
 		String name();
 		
 		@Meta.AD(description = "The filename for persisting orders", required = false, deflt = "orders.pb")
 		File databaseFile();
 	}
 	
 	private final List<Order> orders = new LinkedList<Order>(); 
 	private File ordersFile;
 	
 	private final List<ExchangeListener> listeners = new CopyOnWriteArrayList<ExchangeListener>();
 	
 	@Activate
 	public void activate(Map<String, Object> configProps) throws Exception {
 		Config config = Configurable.createConfigurable(Config.class, configProps);
 		
 		ordersFile = config.databaseFile();
 		loadOrders(ordersFile);
 	}
 
 	private void loadOrders(File file) throws IOException {
 		orders.clear();
 		if (file.isFile()) {
 			FileInputStream stream = new FileInputStream(file);
 			try {
 				for (;;) {
 					Order order = Order.parseDelimitedFrom(stream);
 					if (order == null)
 						break;
 					orders.add(order);
 				}
 			} finally {
 				stream.close();
 			}
 		}
 	}
 	
 	private void saveOrders(File file) throws IOException {
 		FileOutputStream stream = new FileOutputStream(file);
 		try {
 			for (Order order : orders) {
 				order.writeDelimitedTo(stream);
 			}
 		} finally {
 			stream.close();
 		}
 	}
 	
 	@Reference(multiple = true, optional = true, dynamic = true)
 	public void addExchangeListener(ExchangeListener l) {
 		listeners.add(l);
 	}
 	public void removeExchangeListener(ExchangeListener l) {
 		listeners.remove(l);
 	}
 
 	public void submitOrder(Order order) throws IOException {
 		orders.add(order);
 		saveOrders(ordersFile);
 
 		fireOrderSubmitted(order);
 	}
 	
 	private void fireOrderSubmitted(Order order) {
 		for (ExchangeListener l : listeners) {
 			l.orderSubmitted(this, order);
 		}
 	}
 
 	public Collection<Order> getAllOrders() {
 		return Collections.unmodifiableList(orders);
 	}
 
 }
