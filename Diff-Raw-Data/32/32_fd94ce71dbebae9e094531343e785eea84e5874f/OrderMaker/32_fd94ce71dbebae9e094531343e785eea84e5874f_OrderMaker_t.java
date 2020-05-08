 /**
  * 
  */
 package ntnu.it1901.gruppe4.db;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Object class used to create or modify an order
  * 
  * @author David M.
  * 
  */
 public class OrderMaker {
 
 	private Order order;
 	private List<OrderItem> orderItems;
 
 	private List<OrderItem> addQue; // items to be added next time we save
 	private List<OrderItem> remQue; // items to be removed next time we save
 	private List<OrderItem> updateQue; // items to be updated next time we save
 
 	private boolean hasBeenSaved;
 	private boolean hasBeenModified;
 
 	/**
 	 * Creates a new OrderMaker and a new unsaved Order
 	 */
 	public OrderMaker() {
 		order = new Order();
 		orderItems = new ArrayList<OrderItem>();
 		addQue = new ArrayList<OrderItem>();
 		remQue = new ArrayList<OrderItem>();
 		updateQue = new ArrayList<OrderItem>();
 		order.setState(Order.NOT_SAVED);
 		hasBeenSaved = false;
 		hasBeenModified = true;
 		calculatePrice();
 	}
 
 	/**
 	 * Creates a new OrderMaker to modify an existing order
 	 * 
 	 * @param order
 	 *            The order to be modified using this OrderMaker object
 	 */
 	public OrderMaker(Order order) {
 		this.order = order;
 		orderItems = DataAPI.getOrderItems(order);
 		addQue = new ArrayList<OrderItem>();
 		remQue = new ArrayList<OrderItem>();
 		updateQue = new ArrayList<OrderItem>();
 		hasBeenSaved = true;
 		hasBeenModified = false;
 		if (canBeChanged()) {
 			calculatePrice();
 		}
 	}
 
 	/**
 	 * Save all changes to the order, either it is new or not. Adds and removes
 	 * orderItems that have been added and removed since the last save.
 	 * 
 	 */
 	public void save() {
 		if (isValid()) {
 			if (order.getState() == Order.NOT_SAVED) {
 				order.setState(Order.SAVED); // Placed, ready for chef review
 			}
 			if (!hasBeenSaved) {
 				hasBeenSaved = true;
 
 				Calendar cal = Calendar.getInstance();
 				order.setOrderTime(cal.getTime());
 			}
 			DataAPI.saveOrder(order);
 			for (OrderItem item : addQue) {
 				DataAPI.saveOrderItem(item);
 			}
 			for (OrderItem item : remQue) {
 				DataAPI.remOrderItem(item);
 			}
 			for (OrderItem item : updateQue) {
 				DataAPI.saveOrderItem(item);
 			}
 			addQue.clear();
 			remQue.clear();
 			updateQue.clear();
 			hasBeenModified = false;
 		} else {
 			throw new RuntimeException("Order is invalid.");
 		}
 	}
 
 	/**
 	 * Calculates the total price
 	 */
 	private void calculatePrice() {
 		float total = 0.0f;
 		for (OrderItem item : orderItems) {
 			total += item.getAmount();
 		}
 
 		// TODO: beregn frakt
 		
 		float delivery = 0.0f;
 		float max_delivery = Settings.getDeliveryFee();
 		
 		delivery = Settings.getFreeDeliveryLimit() - total;
 		if (delivery > max_delivery) delivery = max_delivery;
 		if (delivery < 0.0f) delivery = 0.0f;
 		
 		total += delivery;
 
 		order.setTotalAmount(total);
		order.setDeliveryFee(delivery);
 	}
 
 	/**
 	 * Determines whether the order is valid in it's current state. To be valid,
 	 * an order needs to (1) have at least one item, (2) have a valid delivery
 	 * address, and (3) have a valid customer (this is inherent in
 	 * address.isValid())
 	 * 
 	 * @return TRUE if it's valid, FALSE if not
 	 */
 	public boolean isValid() {
 		if (getItemCount() == 0)
 			return false;
 		if (order.getIdAddress() == null)
 			return false;
 		if (!order.getIdAddress().isValid())
 			return false;
 		return true;
 	}
 
 	/**
 	 * Adds a new dish as OrderItem to the order, and recalculate the price.
 	 * 
 	 * @param dish
 	 *            the dish to be added
 	 * @return a reference to a new OrderItem that contains the name, price and
 	 *         a reference to the dish
 	 */
 	public OrderItem addItem(Dish dish) {
 		if (canBeChanged()) {
 			OrderItem newOrderItem = new OrderItem(order, dish);
 			orderItems.add(newOrderItem);
 			addQue.add(newOrderItem);
 			calculatePrice();
 			hasBeenModified = true;
 			return newOrderItem;
 		} else {
 			throw new RuntimeException("Order cannot be changed at this time.");
 		}
 	}
 
 	/**
 	 * Determines whether the order can be changed. To be changeable, the order
 	 * must not have been delivered and paid for.
 	 * 
 	 * @return TRUE if it can be changed, FALSE if not.
 	 */
 	public boolean canBeChanged() {
 		// PS: State numbers are described in comments in the Order class
 		return order.getState() < Order.DELIVERED_AND_PAID;
 	}
 
 	/**
 	 * Returns the number of items in this order
 	 * 
 	 * @return the number of items in this order
 	 */
 	public int getItemCount() {
 		return orderItems.size();
 	}
 
 	/**
 	 * Returns an item by index
 	 * 
 	 * @param index
 	 *            the index of the item
 	 * @return an OrderItem from this order
 	 */
 	public OrderItem getItem(int index) {
 		return orderItems.get(index);
 	}
 
 	/**
 	 * Removes an item by index
 	 * 
 	 * @param index
 	 *            the index of the item
 	 */
 	public void remItem(int index) {
 		if (canBeChanged()) {
 			remQue.add(orderItems.get(index));
 			orderItems.remove(index);
 			calculatePrice();
 			hasBeenModified = true;
 		} else {
 			throw new RuntimeException("Order cannot be changed at this time.");
 		}
 	}
 
 	/**
 	 * Removes an item by reference
 	 * 
 	 * @param item
 	 *            a reference to the item to be removed
 	 */
 	public void remItem(OrderItem item) {
 		if (canBeChanged()) {
 			orderItems.remove(item);
 			remQue.add(item);
 			calculatePrice();
 			hasBeenModified = true;
 		} else {
 			throw new RuntimeException("Order cannot be changed at this time.");
 		}
 	}
 
 	/**
 	 * Updates an orderItem. Always use this when modifying an order item,
 	 * especially when the price is changed
 	 * 
 	 * @param item
 	 *            a reference to the OrderItem that has been modified
 	 */
 	public void updateItem(OrderItem item) {
 		if (canBeChanged()) {
 			if (orderItems.contains(item)) {
 				updateQue.add(item);
 				calculatePrice();
 				hasBeenModified = true;
 			}
 		} else {
 			throw new RuntimeException("Order cannot be changed at this time.");
 		}
 	}
 
 	/**
 	 * Sets the address to deliver to. The object will be re-instantiated and
 	 * assigned.
 	 * 
 	 * @param address
 	 *            A reference to the Address-object.
 	 */
 	public void setAddress(Address address) {
 		if (canBeChanged()) {
 			if (address == null) {
 				order.setIdAddress(null);
 			} else {
 				order.setIdAddress(DataAPI.getAddress(address.getIdAddress()));
 			}
 			hasBeenModified = true;
 		}
 	}
 
 	/**
 	 * Returns the address assigned to this order, or null if none is assigned.
 	 * 
 	 * @return an Address-object or null
 	 */
 	public Address getAddress() {
 		if (order.getIdAddress() == null) {
 			return null;
 		} else {
 			return order.getIdAddress();
 		}
 	}
 
 	/**
 	 * Returns an unmodifiable list of all the order items. If you wish to
 	 * modify the list, use addItem, remItem and updateItem.
 	 * 
 	 * @return a reference to a List<OrderItem> containing the order items. The
 	 *         list is unmodifiable.
 	 */
 	public List<OrderItem> getItemList() {
 		return Collections.unmodifiableList(orderItems);
 	}
 
 	/**
 	 * Returns a reference to the Order object.
 	 * 
 	 * @return a reference to the Order object.
 	 */
 	public Order getOrder() {
 		return order;
 	}
 
 	/**
 	 * Determines if the order has been modified.
 	 * 
 	 * @return TRUE if it has been modified, FALSE if not.
 	 */
 	public boolean isModified() {
 		return hasBeenModified;
 	}
 
 	/**
 	 * Determines if the order has been modified.
 	 * 
 	 * @return TRUE if it has been modified, FALSE if not.
 	 */
 	public void setState(int state) {
 		if (canBeChanged()) {
 			order.setState(state);
 			hasBeenModified = true;
 		}
 	}
 }
