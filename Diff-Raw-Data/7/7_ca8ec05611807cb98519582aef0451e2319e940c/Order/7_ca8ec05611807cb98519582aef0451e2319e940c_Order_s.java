 package uk.ac.aber.dcs.cs12420.aberpizza.data;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * A list of {@link OrderItem} objects. There are methods
  * to add items, use the customer's name and to get information
  * about the subtotal and discounts for a particular order.
  * 
  * The order should also return a receipt. This is specified to
  * return a string, plain text or html.
  * 
  * @author Jacob Smith
  */
 public class Order {
 	
 	/**
 	 * The date the order was created. This is automatically generated
 	 * when the order is constructed to match today's date. This field
 	 * is used to sort and filter the orders when they are complete and stored.
 	 */
 	private Date date = null;
 	private String customerName = null;
 	
 	/**
 	 * A map containing each row of the order
 	 * ordered according to the item. This can be thought of as
 	 * a table where each item may appear once (or not at all).
 	 */
 	private Map<Item,OrderItem> orderTable = null;
 	
 	public Order() {
 		orderTable = new HashMap<Item,OrderItem>();
 		date = new Date();
 	}
 	
 	public void setCustomerName(String name) {
 		this.customerName = name;
 	}
 	
 	public String getCustomerName() {
 		return customerName;
 	}
 	
 	/**
 	 * Adds an item to the table or sets an existing item's quantity.
 	 * <p>
 	 * Removing an item can be achieved by 'adding' (or setting) it with
 	 * quantity '0'.
 	 * 
 	 * @param item The item to be set in the table.
 	 * @param quantity The number of copies of the item required in the order.
	 * @see updateItemQuantity
	 * @see orderTable
 	 * 
 	 */
 	public void addItem(Item item, int quantity) {
 		if (quantity > 0) {
 			orderTable.put(item, new OrderItem(item, quantity) );
 		} else {
 			orderTable.remove(item);
 		}
 	}
 	
 	/**
 	 * Change an item's quantity. This increments or decrements the quantity.
 	 * <p>
 	 * Removing an item can be achieved by reducing its quantity to 0.
 	 * 
 	 * @param item The item to be modified in the table.
 	 * @param quantity The number of copies to add (if positive) or remove (if negative)
 	 * @see #addItem
 	 * @see #orderTable
 	 */
 	public void updateItemQuantity(Item item, int quantity) {
 		quantity += orderTable.get(item).getQuantity();
 
 		addItem(item,quantity);
 	}
 	
 	/**
 	 * Return the subtotal (before discounts) of this order.
 	 * <p>
 	 * This is calculated by iterating through each row of orderTable and
 	 * obtaining the price associated with that row.
 	 * 
 	 * @return The price, before discounts, of this order.
 	 * @see #orderTable
	 * @see #OrderItem.getOrderItemTotal
 	 */
 	public BigDecimal getSubtotal() {
 		BigDecimal subtotal = new BigDecimal(""+0);
 		for (OrderItem item : orderTable.values() ) {
 			subtotal = subtotal.add(item.getOrderItemTotal() );
 		}
 		return subtotal;
 	}
 	
 	//TODO: Implement
 	public BigDecimal getDiscount() {
 		return null;}
 	
 	//TODO: Implement
 	public String getReceipt() {
 		return null;}
 
 	/**
 	 * Produces the date that this order was originally constructed.
 	 * 
 	 * @return A Date object referring to the time, in milliseconds, that this object was instantiated.
 	 */
 	public Date getDate() {
 		return date;
 	}
 }
