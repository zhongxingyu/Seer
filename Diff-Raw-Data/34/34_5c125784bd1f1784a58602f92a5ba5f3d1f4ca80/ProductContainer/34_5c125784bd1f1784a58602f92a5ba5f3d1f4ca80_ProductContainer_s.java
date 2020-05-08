 package model;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 /**
  * Product Container class: Represents an object that can hold various types of items,
  * products, and product groups.
  * 
  * @author Matt Hess
  * @version 1.0 - Snell 340 Group 4 Phase 1
  * 
  * @invariant name != null
  * @invariant !name.equals("")
  * @invariant items != null
  * @invariant products != null
  * @invariant productGroups != null
  */
 @SuppressWarnings("serial")
 public abstract class ProductContainer implements Comparable<ProductContainer>, Serializable {
 	protected NonEmptyString name;
 	protected Map<String, Item> items;
 	protected Map<String, Product> products;
 	protected Map<String, ProductGroup> productGroups;
 	// TODO: Implement this map for all descendant nodes
 	private final Map<Product, Set<Item>> productsToItems;
 	protected ProductContainerManager manager;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param name
 	 *            - the name of the Product Container
 	 * 
 	 * @pre NonEmptyString.isValid(name)
 	 * @pre manager != null
 	 * @post true
 	 * 
 	 */
 	public ProductContainer(String name, ProductContainerManager manager) {
 		if (!NonEmptyString.isValid(name)) {
 			throw new IllegalArgumentException("Invalid Name name");
 		}
 		if (manager == null) {
 			throw new NullPointerException("Null Manager manager");
 		}
 
 		this.name = new NonEmptyString(name);
 		items = new TreeMap<String, Item>();
 		productGroups = new TreeMap<String, ProductGroup>();
 		products = new TreeMap<String, Product>();
 		productsToItems = new TreeMap<Product, Set<Item>>();
 		this.manager = manager;
 		this.manager.manage(this);
 	}
 
 	/**
 	 * Method that adds a Product to the collection.
 	 * 
 	 * @param p
 	 *            - the Product object to add to the collection
 	 * 
 	 * @pre p != null
 	 * @pre canAddProduct(p.getBarcode())
 	 * @post products.size() == products.size()@pre + 1
 	 * @post products.contains(p)
 	 * 
 	 */
 	public void add(Product p) {
 		if (p == null) {
 			throw new NullPointerException("Null Product p");
 		}
 		if (!canAddProduct(p.getBarcode()))
 			throw new IllegalStateException(
 					"Cannot add two products of the same name into a single parent container");
 		products.put(p.getBarcode(), p);
 		productsToItems.put(p, new TreeSet<Item>());
 	}
 
 	/**
 	 * Method that adds a ProductGroup object to the collection.
 	 * 
 	 * @param productGroup
 	 *            - the ProductGroup object to add to the collection
 	 * 
 	 * @pre productGroup != null
 	 * @pre canAddProductGroup(productGroup.getName())
 	 * @post pGroups.size() == pGroups.size()@pre + 1
 	 * @post pGroups.contains(productGroup)
 	 * 
 	 */
 	public void add(ProductGroup productGroup) {
 		if (productGroup == null) {
 			throw new NullPointerException("Null ProductGroup productGroup");
 		}
 		if (!canAddProductGroup(productGroup.getName()))
 			throw new IllegalStateException(
 					"Cannot add two product groups of the same name into a container");
 		productGroups.put(productGroup.getName(), productGroup);
 	}
 
 	/**
 	 * Determines whether the specified Product can be added to this Storage Unit.
 	 * 
 	 * @param productBarcode
 	 *            the Product barcode to check
 	 * @return true if it can be added, false otherwise
 	 * 
 	 * @pre productBarcode != null
 	 * @post true
 	 */
 	public boolean canAddProduct(String productBarcode) {
 		if (productBarcode == null) {
 			throw new NullPointerException("Null String productBarcode");
 		}
 		// A Product may appear at most once in a given Storage Unit.
 		if (containsProduct(productBarcode))
 			return false;
 		for (ProductGroup productGroup : productGroups.values()) {
			if (!productGroup.canAddProduct(productBarcode))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Determines whether the specified ProductGroup can be added to this ProductContainer.
 	 * 
 	 * @param productGroup
 	 *            - the ProductGroup to test
 	 * @return true if the ProductGroup can safely be added, false otherwise.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public boolean canAddProductGroup(ProductGroup productGroup) {
 		return canAddProductGroup(productGroup.getName());
 	}
 
 	/**
 	 * Determines whether the specified ProductGroup can be added to this ProductContainer.
 	 * 
 	 * @param productGroupName
 	 *            - the ProductGroup name to test
 	 * @return true if the ProductGroup can safely be added, false otherwise.
 	 * 
 	 * @pre productGroupName != null
 	 * @post true
 	 */
 	public boolean canAddProductGroup(String productGroupName) {
 		if (productGroupName == null) {
 			throw new NullPointerException("Null String productGroupName");
 		}
 		return !productGroupName.equals("") && !containsProductGroup(productGroupName);
 	}
 
 	/**
 	 * Determines if this ProductContainer can be removed from the system.
 	 * 
 	 * @return true if this ProductContainer can be removed, false otherwise.
 	 */
 	public boolean canRemove() {
 		if (!items.isEmpty()) {
 			return false;
 		}
 
 		for (Product product : products.values()) {
 			if (!product.canRemove()) {
 				return false;
 			}
 		}
 
 		for (ProductGroup productGroup : productGroups.values()) {
 			if (!productGroup.canRemove()) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Determines whether we can remove a given Product from this ProductContainer.
 	 * 
 	 * @param product
 	 *            - the Product object to be removed from the container
 	 * 
 	 * @pre product != null
 	 * @post true
 	 */
 	public boolean canRemove(Product product) {
 		if (product == null) {
 			throw new NullPointerException("Null Product product");
 		}
 		if (productsToItems.get(product) == null)
 			return true;
 		return (productsToItems.get(product).isEmpty());
 	}
 
 	/**
 	 * Compares this ProductContainer to another
 	 * 
 	 * @param su
 	 * @return 0 if equal, some other integer representing the comparison otherwise
 	 * @pre pc != null
 	 * @post true
 	 */
 	@Override
 	public int compareTo(ProductContainer pc) {
 		if (pc == null) {
 			throw new NullPointerException("Null ProductContainer pc");
 		}
 		return name.compareTo(pc.name);
 	}
 
 	/**
 	 * Determines whether this Product Container contains a specific Item
 	 * 
 	 * @param item
 	 *            the Item to check
 	 * @return true if this Product Container contains the Item, false otherwise
 	 * 
 	 * @pre item != null
 	 * @post true
 	 */
 	public boolean contains(Item item) {
 		if (item == null) {
 			throw new NullPointerException("Null Item item");
 		}
 		return items.containsKey(item.getBarcode());
 	}
 
 	/**
 	 * Determines whether this Product Container contains a specific Product
 	 * 
 	 * @param product
 	 *            the Product to check
 	 * @return true if this Product Container contains the Product, false otherwise
 	 * 
 	 * @pre product != null
 	 * @post true
 	 * 
 	 */
 	public boolean contains(Product product) {
 		if (product == null) {
 			throw new NullPointerException("Null Product product");
 		}
 		return products.containsKey(product.getBarcode());
 	}
 
 	/**
 	 * Determines whether the specified ProductGroup is contained in this ProductContainer.
 	 * 
 	 * @param productGroupName
 	 *            - the ProductGroup to test
 	 * @return true if the ProductGroup exists in this ProductContainer, false otherwise.
 	 * 
 	 * @pre productGroup != null
 	 * @post true
 	 */
 	public boolean contains(ProductGroup productGroup) {
 		if (productGroup == null) {
 			throw new NullPointerException("Null ProductGroup productGroup");
 		}
 		return productGroups.containsKey(productGroup.getName());
 	}
 
 	/**
 	 * Determines whether this Product Container contains a specific Item
 	 * 
 	 * @param barcode
 	 *            the barcode of the Item to check
 	 * @return true if this Product Container contains the Item, false otherwise
 	 * 
 	 * @pre barcode != null
 	 * @post true
 	 * 
 	 */
 	public boolean containsItem(String barcode) {
 		if (barcode == null) {
 			throw new NullPointerException("Null String barcode");
 		}
 		return items.containsKey(barcode);
 	}
 
 	/**
 	 * Determines whether this Product Container contains a specific Product
 	 * 
 	 * @param productBarcode
 	 *            the Product's barcode to check
 	 * @return true if this Product Container contains the Product, false otherwise
 	 * 
 	 * @pre productBarcode != null
 	 * @post true
 	 * 
 	 */
 	public boolean containsProduct(String productBarcode) {
 		if (productBarcode == null) {
 			throw new NullPointerException("Null String productBarcode");
 		}
 		return products.containsKey(productBarcode);
 	}
 
 	/**
 	 * Determines whether the specified ProductGroup is contained in this ProductContainer.
 	 * 
 	 * @param productGroupName
 	 *            - the ProductGroup to test
 	 * @return true if the ProductGroup exists in this ProductContainer, false otherwise.
 	 * 
 	 * @pre productGroupName != null
 	 * @post true
 	 */
 	public boolean containsProductGroup(String productGroupName) {
 		if (productGroupName == null) {
 			throw new NullPointerException("Null String productGroupName");
 		}
 		return productGroups.containsKey(productGroupName);
 	}
 
 	/**
 	 * Defines equality with another ProductContainer descendant.
 	 * 
 	 * @param o
 	 *            - the object to be compared to.
 	 * @returns True if the objects are equal, false otherwise.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean equals(Object o) {
 		if (o == null) {
 			return false;
 		}
 
 		if (o instanceof ProductContainer) {
 			ProductContainer other = (ProductContainer) o;
 			return getName().equals(other.getName());
 		} else {
 			return super.equals(o);
 		}
 	}
 
 	/**
 	 * Method that calculates and returns the amount of a product in this container.
 	 * 
 	 * @param p
 	 *            - the Product to be found
 	 * @return ProductQuantity - the current supply of the found product, or null.
 	 * 
 	 * @pre p != null
 	 * @post true
 	 * 
 	 */
 	public ProductQuantity getCurrentSupply(Product p) {
 		if (p == null) {
 			throw new NullPointerException("Null Product p");
 		}
 
 		ProductQuantity pSize = p.getSize();
 
 		// Get sum of all pGroup product quantities
 		Iterator<ProductGroup> it = productGroups.values().iterator();
 		ProductQuantity total = new ProductQuantity(0, p.getSize().getUnits());
 		while (it.hasNext()) {
 			ProductGroup current = it.next();
 			total.add(current.getCurrentSupply(p));
 		}
 
 		// add product quantity of items in this container
 		if (productsToItems.containsKey(p)) {
 			for (Item item : productsToItems.get(p)) {
 				total.add(new ProductQuantity(pSize.getQuantity(), pSize.getUnits()));
 			}
 		}
 
 		return total;
 	}
 
 	/**
 	 * Gets the size of the items collection.
 	 * 
 	 * @return int - the number of elements in the items collection.
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public int getItemsSize() {
 		return items.size();
 	}
 
 	/**
 	 * Attribute getter - name
 	 * 
 	 * @return The String name of the ProductContainer
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public String getName() {
 		return name.toString();
 	}
 
 	/**
 	 * Finds and returns the requested Product object
 	 * 
 	 * @param barcode
 	 *            - the String barcode of the product to find
 	 * @return Product - the requested Product
 	 * 
 	 * @pre barcode != null
 	 * @post true
 	 * 
 	 */
 	public Product getProduct(String barcode) {
 		if (barcode == null) {
 			throw new NullPointerException("Null String barcode");
 		}
 
 		return products.get(barcode);
 	}
 
 	/**
 	 * Finds and returns the requested ProductGroup object
 	 * 
 	 * @param pgToFind
 	 *            - the name of the ProductGroup to find
 	 * @return ProductGroup - the requested ProductGroup
 	 * 
 	 * @pre pgToFind != null
 	 * @post true
 	 */
 	public ProductGroup getProductGroup(String pgToFind) {
 		if (pgToFind == null) {
 			throw new NullPointerException("Null String pgToFind");
 		}
 
 		return productGroups.get(pgToFind);
 	}
 
 	/**
 	 * Gets the size of the productGroups collection
 	 * 
 	 * @return int - the number of elements in the pGroups collection.
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public int getProductGroupsSize() {
 		return productGroups.size();
 	}
 
 	/**
 	 * Gets an iterator over this Container's Products
 	 * 
 	 * @return Iterator<Product> this Container's Products iterator
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public Iterator<Product> getProductsIterator() {
 		return products.values().iterator();
 	}
 
 	/**
 	 * Gets the size of the products collection.
 	 * 
 	 * @return int - the number of elements in the products collection.
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public int getProductsSize() {
 		return products.size();
 	}
 
 	/**
 	 * Internal method: Used to determine whether it is possible to add a Product
 	 * 
 	 * @param productBarcode
 	 *            Product to look for
 	 * @return
 	 */
 	protected boolean hasDescendantProduct(String productBarcode) {
 		if (containsProduct(productBarcode)) {
 			return true;
 		}
 		for (ProductGroup productGroup : productGroups.values()) {
 			if (productGroup.hasDescendantProduct(productBarcode))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Removes the specified item from this ProductContainer. Use this only
 	 * 
 	 * @param item
 	 *            the Item to be moved
 	 * @param destination
 	 *            the ProductContainer to move the item to
 	 * 
 	 * @pre item != null
 	 * @pre destination != null
 	 * @pre contains(item)
 	 * @pre !destination.contains(item)
 	 * @post !items.contains(item)
 	 * @post destination.contains(item)
 	 * 
 	 */
 	public void moveIntoContainer(Item item, ProductContainer destination) {
 		if (item == null) {
 			throw new NullPointerException("Null Item item");
 		}
 		if (destination == null) {
 			throw new NullPointerException("Null ProductContainer destination");
 		}
 		if (!contains(item)) {
 			throw new IllegalStateException(
 					"Item does not exist in this container; cannot move");
 		}
 		if (destination.contains(item)) {
 			throw new IllegalStateException(
 					"Destination container already contains the item to be moved");
 		}
 
 		unregisterItem(item);
 		destination.registerItem(item);
 	}
 
 	/**
 	 * Helper method for adding items
 	 * 
 	 * @param i
 	 *            Item to add
 	 */
 	protected void registerItem(Item i) {
 		items.put(i.getBarcode(), i);
 		Set<Item> newItemsForProduct;
 		if (productsToItems.containsKey(i.getProduct())) {
 			newItemsForProduct = productsToItems.get(i.getProduct());
 		} else {
 			newItemsForProduct = new TreeSet<Item>();
 		}
 		newItemsForProduct.add(i);
 		productsToItems.put(i.getProduct(), newItemsForProduct);
 	}
 	
 	/**
 	 * Helper Method for adding items to this productContainer or
 	 * any of its children (recursively). If no product matching i's
 	 * product is found, no item is registered.
 	 *  
 	 * @param i
 	 * 			  Item to add
 	 * 
 	 * @return
 	 * 			  true if item is registered, false if otherwise.
 	 */
 	protected boolean recursivelyRegisterItem(Item i){
 		if(this.productGroups == null) return false;
 		
 		if(this.containsProduct(i.getProductBarcode())){
 			this.registerItem(i);
 			return true;
 		}
 		
 		if(!this.productGroups.isEmpty()){
 			for(ProductContainer container : this.productGroups.values()){
 				return container.recursivelyRegisterItem(i);
 			}
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Removes the specified item from this ProductContainer.
 	 * 
 	 * @param item
 	 *            the Item to be removed
 	 * @param manager
 	 *            the ItemManager to notify of the removal. If null, no manager is notified.
 	 * 
 	 * @return the removed item
 	 * 
 	 * @pre item != null
 	 * @pre manager != null
 	 * @pre productsToItems.containsKey(item.getProduct())
 	 * @post !containsItem(item.getBarcode())
 	 */
 	public Item remove(Item item, ItemManager manager) {
 		if (item == null) {
 			throw new NullPointerException("Null Item item");
 		}
 		if (manager == null) {
 			throw new NullPointerException("Null Manager manager");
 		}
 		if (!productsToItems.containsKey(item.getProduct())) {
 			throw new IllegalStateException("ProductContainer does not contain Item item");
 		}
 
 		manager.unmanage(item);
 		Set<Item> newItemsForProduct = productsToItems.get(item.getProduct());
 		newItemsForProduct.remove(item);
 		productsToItems.put(item.getProduct(), newItemsForProduct);
 		return items.remove(item.getBarcode());
 	}
 
 	/**
 	 * Method that removes a Product object from the collection.
 	 * 
 	 * @param barcode
 	 *            - the String barcode of the Product object to be removed from the collection
 	 * @return the removed Product
 	 * 
 	 * @pre product != null
 	 * @pre canRemove(product)
 	 * @post !products.contains(product)
 	 */
 	public Product remove(Product product) {
 		if (product == null) {
 			throw new NullPointerException("Null Product product");
 		}
 		if (!canRemove(product))
 			throw new IllegalStateException(
 					"Cannot remove product; product container still has items that refer to it");
 		productsToItems.remove(product);
 		return products.remove(product.getBarcode());
 	}
 
 	/**
 	 * Removes a specified ProductGroup from this container.
 	 * 
 	 * @param productGroup
 	 *            The ProductGroup to remove
 	 * 
 	 * @pre productGroup != null
 	 * @pre productGroup.canRemove()
 	 * @post !contains(productGroup)
 	 */
 	public void remove(ProductGroup productGroup) {
 		if (productGroup == null) {
 			throw new NullPointerException("Null ProductGroup productGroup");
 		}
 		if (!productGroup.canRemove()) {
 			throw new IllegalStateException("Cannot remove child product group");
 		}
 
 		productGroups.remove(productGroup.getName());
 	}
 
 	/**
 	 * Sets this Container's name
 	 * 
 	 * @param name
 	 *            Name to set to
 	 * 
 	 * @pre manager.isValidStorageUnitName(name)
 	 * @post getName() == name
 	 * 
 	 */
 	public void setName(String name) {
 		if (!manager.isValidStorageUnitName(name)) {
 			throw new IllegalStateException(
 					"Invalid Storage Unit name. A Storage Unit with this name may already exist");
 		}
 
 		this.name = new NonEmptyString(name);
 	}
 
 	/**
 	 * Helper method for moving / removing items
 	 * 
 	 * @param i
 	 *            Item to move/remove
 	 */
 	protected void unregisterItem(Item i) {
 		items.remove(i.getBarcode());
 
 		Set<Item> newItemsForProduct;
 		if (productsToItems.containsKey(i.getProduct())) {
 			newItemsForProduct = productsToItems.get(i.getProduct());
 		} else {
 			newItemsForProduct = new TreeSet<Item>();
 		}
 		newItemsForProduct.remove(i);
 		productsToItems.put(i.getProduct(), newItemsForProduct);
 	}
 }
