 package model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import model.visitor.InventoryVisitable;
 import model.visitor.InventoryVisitor;
 
 /**
  * Represents a physical item in the Home Inventory System.
  * 
  * @invariant product != null
  * @invariant barcode != null
  * @invariant entryDate != null
  * @invariant expirationDate != null
  */
 @SuppressWarnings("serial")
 public class Item implements Comparable<Object>, Serializable, InventoryVisitable {
 	/**
 	 * Method that tests if a Date object is a valid entry date.
 	 * 
 	 * @param date
 	 *            the date to test
 	 * @return true if the date is valid
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	@SuppressWarnings("deprecation")
 	public static boolean isValidEntryDate(Date date) {
 		if (date == null)
 			return false;
 
 		return !(date.after(new Date()) || date.before(new Date(100, 0, 0, 0, 0, 0)));
 	}
 
 	private final Product product;
 	private final Barcode barcode;
 	private Date entryDate;
 	private Date expirationDate;
 	private Date exitTime;
 
 	private ProductContainer container;
 
 	/**
 	 * Constructs a new Item with the specified barcode, product, and container.
 	 * 
 	 * @param barcode
 	 *            the Item's barcode
 	 * @param product
 	 *            this Item's corresponding Product
 	 * @param container
 	 *            the ProductContainer this Item is to be stored in
 	 * 
 	 * @pre barcode != null
 	 * @pre product != null
 	 * @pre container != null
 	 * @pre entryDate != null
 	 * @pre manager != null
 	 */
 	public Item(Barcode barcode, Product product, ProductContainer container, Date entryDate,
 			ItemManager manager) {
 		if (barcode == null) {
 			throw new NullPointerException("Null Barcode barcode");
 		}
 		if (product == null) {
 			throw new NullPointerException("Null Product product");
 		}
 		// The container can be null if the item is not contained in any container
 		if (manager == null) {
 			throw new NullPointerException("Null Manager manager");
 		}
 
 		this.product = product;
 		this.barcode = barcode;
 		setEntryDate(entryDate);
 		setExpirationDate();
 		setContainer(container);
 		container.add(this);
 		manager.manage(this);
 	}
 
 	/**
 	 * Constructs a new Item with the specified data.
 	 * 
 	 * @param product
 	 *            This Item's Product
 	 * @param container
 	 *            The ProductContainer this Item is to be stored in
 	 * @param entryDate
 	 *            The date this Item was entered into the system
 	 * @param manager
 	 *            The ItemManager to optimize accesses to this item
 	 * @pre product != null
 	 * @pre container != null
 	 * @pre entryDate != null
 	 * @pre manager != null
 	 * 
 	 * @post this.container != null
 	 * 
 	 */
 	public Item(Product product, ProductContainer container, Date entryDate,
 			ItemManager manager) {
 		this(new Barcode(), product, container, entryDate, manager);
 	}
 
 	@Override
 	public void accept(InventoryVisitor visitor) {
 		visitor.visit(this);
 	}
 
 	/**
 	 * Compares this Item to another object
 	 * 
 	 * @param o
 	 *            the object to compare this Item to
 	 * @return zero if the items are equal, otherwise a number greater than or less than zero
 	 * 
 	 * @pre o != null
 	 * @pre (o instanceof Item)
 	 * @post true
 	 */
 	@Override
 	public int compareTo(Object o) {
 		if (o == null) {
 			throw new NullPointerException("Null Object o");
 		}
 		if (!(o instanceof Item)) {
 			throw new ClassCastException("Illegal object type for Object o");
 		}
 
 		Item other = (Item) o;
 		return barcode.compareTo(other.barcode);
 	}
 
 	/**
 	 * Gets this Item's barcode
 	 * 
 	 * @return this item's barcode
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public String getBarcode() {
 		return barcode.toString();
 	}
 
 	/**
 	 * Gets this Item's parent container
 	 * 
 	 * @return this Item's parent ProductContainer
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public ProductContainer getContainer() {
 		return container;
 	}
 
 	/**
 	 * Gets this Item's entry date
 	 * 
 	 * @return this item's entry date
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public Date getEntryDate() {
 		return entryDate;
 	}
 
 	/**
 	 * Gets this Item's exit time
 	 * 
 	 * @return the Item's exit time
 	 * 
 	 * @pre container == null (indicating item has been removed)
 	 * @post true
 	 */
 	public Date getExitTime() {
 		if (container != null) {
 			throw new IllegalStateException("Container container should be null");
 		}
 		return exitTime;
 	}
 
 	/**
 	 * Gets this Item's expiration date
 	 * 
 	 * @return the Item's expiration date
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public Date getExpirationDate() {
 		return expirationDate;
 	}
 
 	/**
 	 * Gets this Item's Product
 	 * 
 	 * @return this Item's Product
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public Product getProduct() {
 		return product;
 	}
 
 	/**
 	 * Gets this Item's Product barcode
 	 * 
 	 * @return Product (String) barcode
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public String getProductBarcode() {
 		return product.getBarcode();
 	}
 
 	/**
 	 * Gets this Item's ProductGroup's name (or "" if it has none)
 	 * 
 	 * @return this Item's ProductGroup's name (or "" if it has none)
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public String getProductGroupName() {
 		return (container instanceof ProductGroup) ? container.getName() : "";
 	}
 
 	/**
 	 * Gets this Item's StorageUnit's name
 	 * 
 	 * @return this Item's StorageUnit's name
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public String getStorageUnitName() {
 		if (container instanceof ProductGroup) {
 			return ((ProductGroup) container).getRoot().getName();
 		} else {
 			return container.getName();
 		}
 	}
 
 	/**
 	 * Sets exit time to now, container to null
 	 * 
 	 * @pre true
 	 * @post exitTime != null
 	 * @post container == null
 	 */
 	public void remove() {
 		exitTime = new Date();
 		container = null;
 	}
 
 	/**
 	 * Sets this Item's parent ProductContainer
 	 * 
 	 * @param productContainer
 	 *            the new parent container
 	 * @pre true
 	 * @post container = productContainer
 	 */
 	public void setContainer(ProductContainer productContainer) {
 		// productContainer can be null if the item has been removed from the system.
 		container = productContainer;
 	}
 
 	/**
 	 * Sets this item's entry date
 	 * 
 	 * @param date
 	 *            the entry date to set to.
 	 * @throws IllegalArgumentException
 	 *             if the date is not valid
 	 * 
 	 * @pre if(date != null) isValidEntryDate(date)
 	 * @post entryDate = new Date() || entryDate = date
 	 * 
 	 */
 	public void setEntryDate(Date date) {
 		// From the Data Dictionary:
 		// Must be non-empty. Cannot be in the
 		// future or prior to 1/1/2000.
 
 		if (date == null) {
 			entryDate = new Date();
 			return;
 		}
 
 		if (!isValidEntryDate(date))
 			throw new IllegalArgumentException("Date is not valid");
 
 		entryDate = date;
 		setExpirationDate();
 	}
 
 	// private setters
 	@SuppressWarnings("deprecation")
 	private void setExpirationDate() {
 		Date d = entryDate;
		if (product.getShelfLife() < 1)
			expirationDate = null;
		else
			expirationDate = new Date(d.getYear(), d.getMonth() + product.getShelfLife(),
					d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds());
 	}
 }
