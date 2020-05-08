 package ch.unibe.ese.shopnote.core;
 
 import java.math.BigDecimal;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * The ListManager is responsible for access to all {@link ShoppingList
  * shoppinglists}.
  */
 public class ListManager {
 
 	private static final BigDecimal THOUSAND = new BigDecimal("1000");
 	private final List<ShoppingList> shoppingLists;
 	private final PersistenceManager persistenceManager;
 	private List<Recipe> recipeList;
 
 	public ListManager(PersistenceManager persistenceManager) {
 		this.persistenceManager = persistenceManager;
 		this.shoppingLists = persistenceManager.getLists();
 		this.recipeList = persistenceManager.readRecipes();
 	}
 
 	/**
 	 * @return unmodifiable list
 	 */
 	public List<ShoppingList> getShoppingLists() {
 		Collections.sort(shoppingLists, Comparators.LIST_COMPARATOR);
 		return Collections.unmodifiableList(shoppingLists);
 	}
 
 	/**
 	 * Get a specific shopping list with unique id
 	 * 
 	 * @param id
 	 * @return Shopping list with id
 	 */
 	public ShoppingList getShoppingList(long id) {
 		for (ShoppingList list : shoppingLists) {
 			if (list.getId() == id)
 				return list;
 		}
 		return null;
 	}
 
 	/**
 	 * @param list
 	 *            not <code>null</code>
 	 */
 	public long saveShoppingList(ShoppingList list) {
 		if (list == null)
 			throw new IllegalArgumentException("null is not allowed");
 		if (!shoppingLists.contains(list)) {
 			shoppingLists.add(list);
 		}
 		long id = persistenceManager.save(list);
 		return id;
 	}
 
 	public void removeShoppingList(ShoppingList list) {
 		shoppingLists.remove(list);
 		persistenceManager.remove(list);
 	}
 
 	/**
 	 * Adds the item to the shoppinglist
 	 * 
 	 * @param item
 	 *            not null
 	 * @param list
 	 *            not null
 	 * @return true if the item was added (false if it only was updated or not
 	 *         changed at all)
 	 * @throws ItemException thrown if another item with the same name, but different unit is in the list.
 	 */
 	public boolean addItemToList(Item item, ShoppingList list) throws ItemException {
 		if (item == null || list == null)
 			throw new IllegalArgumentException("null is not allowed");
 		
 		List<Item> items = persistenceManager.getItems(list);
 		item = mergeItem(item, items);
 		
 		persistenceManager.save(item, list);
 		return !items.contains(item);
 	}
 	
 	/**
 	 * Updates the item in the shopping list.<p>
 	 * If the item is not yet in the list, nothing is done.
 	 * 
 	 * @param item not null
 	 * @param list not null
 	 */
 	public void updateItemInList(Item item, ShoppingList list) {
 		if (item == null || item.getId() == null || list == null)
 			throw new IllegalArgumentException("null is not allowed");
 
 		List<Item> items = persistenceManager.getItems(list);
		if (!items.contains(items))
 			return;
 		
 		persistenceManager.save(item, list);
 	}
 	
 	/**
 	 * Merges the newItem into the list of existing items.<p>
 	 * If the list contains an item with the same name and the same unit, it will merge this item with the newItem.
 	 * @param newItem
 	 * @param items
 	 * @return the item to persist.
 	 * @throws ItemException thrown if another item with the same name, but different unit is in the list.
 	 */
 	private Item mergeItem(Item newItem, List<Item> items) throws ItemException{
 		for (Item item2 : items) {
 			if (!item2.isBought() && item2.getName().equals(newItem.getName())) {
 				if (newItem.getUnit() == null && item2.getUnit() == null) {
 					// Both have no unit --> item has now 2 pieces.
 					item2.setQuantity(BigDecimal.valueOf(2), ItemUnit.PIECE);
 					return item2;
 				} else if (newItem.getUnit() == null && item2.getUnit() == ItemUnit.PIECE) {
 					// new Item has no unit, existing item has PIECE as unit. add one piece to the existing item.
 					item2.setQuantity(item2.getQuantity().add(BigDecimal.ONE), ItemUnit.PIECE);
 					return item2;
 				} else if (newItem.getUnit() != null && newItem.getUnit() == item2.getUnit()) {
 					// Both have same unit --> add them together
 					BigDecimal newQuantity = newItem.getQuantity().add(item2.getQuantity());
 					item2.setQuantity(newQuantity, item2.getUnit());
 					return item2; // we want to save only one item.
 				} else if (ItemUnit.MASSES.contains(newItem.getUnit()) && ItemUnit.MASSES.contains(item2.getUnit())) {
 					// Both units are masses. Convert it first to grams and then add them.
 					BigDecimal mass1 = newItem.getUnit()==ItemUnit.GRAM ? newItem.getQuantity():newItem.getQuantity().multiply(THOUSAND);
 					BigDecimal mass2 = item2.getUnit()==ItemUnit.GRAM ? item2.getQuantity():item2.getQuantity().multiply(THOUSAND);
 					BigDecimal finalMass = mass1.add(mass2);
 					ItemUnit unit = ItemUnit.GRAM;
 					if (finalMass.compareTo(THOUSAND) > 0){
 						finalMass = finalMass.divide(THOUSAND);
 						unit = ItemUnit.KILO_GRAM;
 					}
 					item2.setQuantity(finalMass, unit);
 					return item2;
 				} else {
 					throw new ItemException();
 				}
 			}
 		}
 		return newItem;
 	}
 
 	/**
 	 * Removes an item from this shopping list
 	 * 
 	 * @param item
 	 * @param list
 	 */
 	public void removeItemFromList(Item item, ShoppingList list) {
 		if (item == null || list == null)
 			throw new IllegalArgumentException("null is not allowed");
 		persistenceManager.remove(item, list);
 	}
 
 	/**
 	 * Gets all Items from this shopping list.
 	 * 
 	 * @param list
 	 *            not null.
 	 * @return not null, unmodifiable.
 	 */
 	public List<Item> getItemsFor(ShoppingList list) {
 		List<Item>	items = persistenceManager.getItems(list);
 		Collections.sort(items, Comparators.ITEM_COMPARATOR);
 		return Collections.unmodifiableList(items);
 	}
 
 	/**
 	 * Gets all items-objects which are in the item table
 	 * 
 	 * @return ArrayList<Item>
 	 */
 	public List<Item> getAllItems() {
 		List<Item> items = persistenceManager.getAllItems();
 		Collections.sort(items, Comparators.ITEM_COMPARATOR);
 		return items;
 	}
 
 	/**
 	 * Adds an item to the item list or updates it if it is already in the list
 	 * 
 	 * @param item
 	 */
 	public void save(Item item) {
 		persistenceManager.save(item);
 	}
 
 	/**
 	 * Get a specific item with unique id
 	 * 
 	 * @param id
 	 * @return item with id
 	 */
 	public Item getItem(Long id) {
 		if (id == null)
 			return null;
 		return persistenceManager.getItem(id);
 	}
 	
 	/**
 	 * Gets the item with the given name.
 	 * @param name
 	 * @return null if no item with the name exists.
 	 */
 	public Item getItem(String name) {
 		return persistenceManager.getItem(name);
 	}
 
 	/**
 	 * Removes an specific item from the db
 	 * 
 	 * @param item
 	 */
 	public void remove(Item item) {
 		for (ShoppingList list : shoppingLists)
 			removeItemFromList(item, list);
 		persistenceManager.remove(item);
 
 	}
 
 	/**
 	 * Returns a list of all recipes which are saved in the database
 	 * 
 	 * @return list of recipes
 	 */
 	public List<Recipe> getRecipes() {
 		Collections.sort(recipeList, Comparators.RECIPE_COMPARATOR);
 		return Collections.unmodifiableList(recipeList);
 	}
 
 	/**
 	 * Saves all Recipes to the database
 	 * 
 	 * @param recipe
 	 */
 	public void saveRecipe(Recipe recipe) {
 		if (!recipeList.contains(recipe)) {
 			recipeList.add(recipe);
 		}
 		persistenceManager.save(recipe);
 	}
 
 	/**
 	 * Removes a Recipe from the database
 	 * 
 	 * @param recipe
 	 */
 	public void removeRecipe(Recipe recipe) {
 		recipeList.remove(recipe);
 		persistenceManager.remove(recipe);
 	}
 
 	/**
 	 * Gets the Recipe at the correct position
 	 * 
 	 * @param position
 	 * @return recipe at position x
 	 */
 	public Recipe getRecipeAt(Long id) {
 		for (Recipe recipe : recipeList)
 			if (recipe.getId() == id)
 				return recipe;
 		return null;
 	}
 
 	public void updateRecipe() {
 		recipeList = persistenceManager.readRecipes();
 	}
 }
