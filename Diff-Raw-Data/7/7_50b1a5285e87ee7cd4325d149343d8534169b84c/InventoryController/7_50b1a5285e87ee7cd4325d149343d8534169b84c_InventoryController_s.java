 package gui.inventory;
 
 import gui.common.Controller;
 import gui.common.DataWrapper;
 import gui.item.ItemData;
 import gui.modellistener.ItemListener;
 import gui.modellistener.ProductContainerListener;
 import gui.modellistener.ProductListener;
 import gui.product.ProductData;
 
 import java.util.Iterator;
 import java.util.Set;
 
 import model.Item;
 import model.ItemManager;
 import model.Product;
 import model.ProductContainer;
 import model.ProductGroup;
 import model.StorageUnit;
 
 /**
  * Controller class for inventory view.
  */
 public class InventoryController extends Controller implements IInventoryController {
 	private final ProductContainerListener productContainerListener;
 	private final ProductListener productListener;
 	private final ItemListener itemListener;
 
 	// private final InventoryListener inventoryListener;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param view
 	 *            Reference to the inventory view
 	 * 
 	 * @pre view != null
 	 * @post true
 	 */
 	public InventoryController(IInventoryView view) {
 		super(view);
 		// Attach InventoryListener here too
 		productContainerListener = new ProductContainerListener(getView(),
 				getProductContainerManager());
 		itemListener = new ItemListener(getView(), getItemManager());
 		productListener = new ProductListener(getView(), getProductManager());
 		// inventoryListener = new InventoryListener(getView());
 		construct();
 	}
 
 	/**
 	 * This method is called when the user selects the "Add Items" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if(!canAddItems())
 	 * 
 	 * @pre canAddItems()
 	 * @post true
 	 */
 	@Override
 	public void addItems() {
 		if (!canAddItems()) {
 			throw new IllegalStateException("Unable to add Items");
 		}
 		getView().displayAddItemBatchView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Add Product Group" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if(!canAddProductGroup())
 	 * 
 	 * @pre canAddProductGroup()
 	 * @post true
 	 */
 	@Override
 	public void addProductGroup() {
 		if (!canAddProductGroup()) {
 			throw new IllegalStateException("Unable to add Product Groups");
 		}
 		getView().displayAddProductGroupView();
 	}
 
 	/**
 	 * This method is called when the user drags a product into a product container.
 	 * 
 	 * @param productData
 	 *            Product dragged into the target product container
 	 * @param containerData
 	 *            Target product container
 	 * @throws IllegalArgumentException
 	 *             if either parameter is null
 	 * @throws IllegalStateException
 	 *             if either parameter's getTag() == null
 	 * 
 	 * @pre productData != null && productData.getTag() != null
 	 * @pre containerData != null && containerData.getTag() != null
 	 * @post containerData.getChildCount() == old(getChildCount()) + 1
 	 */
 	@Override
 	public void addProductToContainer(ProductData productData,
 			ProductContainerData containerData) {
 		if (productData == null)
 			throw new IllegalArgumentException("ProductData should not be null");
 		if (containerData == null)
 			throw new IllegalArgumentException("ProductContainerData should not be null");
 
 		Product productToAdd = (Product) productData.getTag();
 		if (productToAdd == null)
 			throw new IllegalStateException("Product must have a tag.");
 		ProductContainer targetContainer = (ProductContainer) containerData.getTag();
 		if (targetContainer == null)
 			throw new IllegalStateException("ProductContainer must have a tag.");
 
 		productToAdd.moveToContainer(targetContainer, getItemManager());
 	}
 
 	/**
 	 * This method is called when the user selects the "Add Storage Unit" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if(!canAddStorageUnit())
 	 * 
 	 * @pre canAddStorageUnit()
 	 * @post true
 	 */
 	@Override
 	public void addStorageUnit() {
 		if (!canAddStorageUnit()) {
 			throw new IllegalStateException("Unable to add Storage Units");
 		}
 		getView().displayAddStorageUnitView();
 	}
 
 	/**
 	 * Returns true if and only if the "Add Items" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canAddItems() {
 		// Always enabled per Functional Spec p17
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Add Product Group" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canAddProductGroup() {
 		// Always enabled per Functional Spec p15
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Add Storage Unit" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canAddStorageUnit() {
 		// Always enabled per Functional Spec p14
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Delete Product" menu item should be enabled.
 	 * 
 	 * @pre if(getView().getSelectedProductContainer() != null)
 	 *      getView().getSelectedProductContainer().getTag() != null
 	 * @pre if(getView().getSelectedProductContainer() != null) getSelectedProductTag() != null
 	 * @post true
 	 * 
 	 */
 	@Override
 	public boolean canDeleteProduct() {
 		// 3 cases depending on getView().getSelectedProductContainer().
 		// See Functional Spec p21-22
 
 		// case 1: No product container is selected
 		if (getView().getSelectedProductContainer() == null)
 			return false;
 
 		ProductContainer containerTag = getSelectedProductContainerTag();
 		if (getView().getSelectedProduct() == null)
 			return false;
 
 		Product productTag = getSelectedProductTag();
 
 		// case 2: selected product container is the root node
 		if (containerTag == null) // root 'Storage Units' is assigned 'null' for its tag
 			return productTag.canRemove();
 
 		// case 3: selected product container is a child StorageUnit or ProductGroup
 		else
 			return containerTag.canRemove(productTag);
 	}
 
 	/**
 	 * Returns true if and only if the "Delete Product Group" menu item should be enabled.
 	 * 
 	 * @pre getView().getSelectedProductContainer() != null
 	 * @pre getView().getSelectedProductContainer().getTag() instanceof ProductGroup
 	 * @post true
 	 */
 	@Override
 	public boolean canDeleteProductGroup() {
 		// Enabled only if getView().getSelectedProductContainer() does not contain any
 		// items (including it's sub Product Groups)
 		// See Functional Spec p17
 
 		return getSelectedProductContainerTag().canRemove();
 	}
 
 	/**
 	 * Returns true if and only if the "Delete Storage Unit" menu item should be enabled.
 	 * 
 	 * @pre getView().getSelectedProductContainer() != null
 	 * @pre getView().getSelectedProductContainer().getTag() instanceof StorageUnit
 	 * @post true
 	 */
 	@Override
 	public boolean canDeleteStorageUnit() {
 		// Enabled only if getView().getSelectedProductContainer() does not contain any
 		// items (including it's Product Groups)
 		// See Functional Spec p15
 
 		if (getView().getSelectedProductContainer() == null)
 			return false;
 
 		return getSelectedProductContainerTag().canRemove();
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Item" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canEditItem() {
 		return getView().getSelectedItem() != null;
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Product" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canEditProduct() {
 		return getView().getSelectedProduct() != null;
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Product Group" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canEditProductGroup() {
 		ProductContainerData pcData = getView().getSelectedProductContainer();
 		return pcData != null && (pcData.getTag() instanceof ProductGroup);
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Storage Unit" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canEditStorageUnit() {
 		ProductContainerData pcData = getView().getSelectedProductContainer();
 		return pcData != null && (pcData.getTag() instanceof StorageUnit);
 	}
 
 	/**
 	 * Returns true if and only if the "Remove Item" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canRemoveItem() {
 		ItemData id = getView().getSelectedItem();
 		return id != null && id.getTag() != null;
 	}
 
 	/**
 	 * Returns true if and only if the "Remove Items" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canRemoveItems() {
 		ProductContainerData pcData = getView().getSelectedProductContainer();
 		return pcData.getTag() == null;
 	}
 
 	/**
 	 * Returns true if and only if the "Transfer Items" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canTransferItems() {
 		// Always enabled per Functional Spec p24
 		return true;
 	}
 
 	/**
 	 * This method is called when the user selects the "Delete Product" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canDeleteProduct())
 	 * 
 	 * @pre canDeleteProduct()
 	 * @pre getSelectedProductTag() != null
 	 * @post !getProductManager().contains(old(getView().getSelectedProduct().getTag()))
 	 */
 	@Override
 	public void deleteProduct() {
 		if (!canDeleteProduct()) {
 			throw new IllegalStateException("Unable to delete Product");
 		}
 		ProductContainer parent = (ProductContainer) getView().getSelectedProductContainer()
 				.getTag();
 		parent.remove(getSelectedProductTag());
 
 		getProductManager().unmanage(getSelectedProductTag());
 	}
 
 	/**
 	 * This method is called when the user selects the "Delete Product Group" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canDeleteProductGroup())
 	 * 
 	 * @pre canDeleteProductGroup()
 	 * @pre getSelectedProductContainerTag() != null
 	 * @post !getProductContainerManager().contains(PREVIOUS
 	 *       getView().getSelectedProductContainer().getTag())
 	 */
 	@Override
 	public void deleteProductGroup() {
 		if (!canDeleteProductGroup()) {
 			throw new IllegalStateException("Unable to delete Product Group");
 		}
 
 		deleteSelectedProductContainer();
 	}
 
 	/**
 	 * This method is called when the user selects the "Delete Storage Unit" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canDeleteStorageUnit())
 	 * 
 	 * @pre canDeleteStorageUnit()
 	 * @pre getSelectedProductContainerTag() != null
 	 * @post !getProductContainerManager().contains(PREVIOUS
 	 *       getView().getSelectedProductContainer().getTag())
 	 */
 	@Override
 	public void deleteStorageUnit() {
 		if (!canDeleteStorageUnit()) {
 			throw new IllegalStateException("Unable to delete Storage Unit");
 		}
 
 		deleteSelectedProductContainer();
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Item" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canEditItem())
 	 * 
 	 * @pre canEditItem()
 	 * @post getItemManager().contains(old(getView().getSelectedItem().getTag()))
 	 */
 	@Override
 	public void editItem() {
 		ItemData selectedItem = getView().getSelectedItem();
 		if (!canEditItem()) {
 			throw new IllegalStateException("Unable to edit Item");
 		}
 		getView().displayEditItemView();
 		getView().selectItem(selectedItem);
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Product" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canEditProduct())
 	 * 
 	 * @pre canEditProduct()
 	 * @post getProductManager().contains(old(getView().getSelectedProduct().getTag()))
 	 */
 	@Override
 	public void editProduct() {
 		if (!canEditProduct()) {
 			throw new IllegalStateException("Unable to edit Product");
 		}
 		getView().displayEditProductView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Product Group" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canEditProductGroup)
 	 * 
 	 * @pre canEditProductGroup()
 	 * @post getProductContainerManager().contains(PREVIOUS
 	 *       getView().getSelectedProductContainer().getTag())
 	 */
 	@Override
 	public void editProductGroup() {
 		if (!canEditProductGroup()) {
 			throw new IllegalStateException("Unable to edit Product Group");
 		}
 		getView().displayEditProductGroupView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Storage Unit" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canEditStorageUnit())
 	 * 
 	 * @pre canEditStorageUnit()
 	 * @post getProductContainerManager().contains(PREVIOUS
 	 *       getView().getSelectedProductContainer().getTag())
 	 */
 	@Override
 	public void editStorageUnit() {
 		if (!canEditStorageUnit()) {
 			throw new IllegalStateException("Unable to edit Storage Unit");
 		}
 		getView().displayEditStorageUnitView();
 	}
 
 	/**
 	 * This method is called when the selected item changes.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public void itemSelectionChanged() {
 		// The only possible change is in the context menus available
 		enableComponents();
 	}
 
 	/**
 	 * This method is called when the user drags an item into a product container.
 	 * 
 	 * @param itemData
 	 *            Item dragged into the target product container
 	 * @param containerData
 	 *            Target product container
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if either parameter is null
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if either parameter is null
 	 * @throws IllegalStateException
 	 *             if the target product container doesn't have a tag
 	 * 
 	 * @pre itemData != null
 	 * @pre containerData != null
 	 * @pre getView().getSelectedProductContainer() != null
 	 * @pre getView().getSelectedProductContainer().getTag().contains(itemData.getTag())
 	 * @pre !containerData.getTag().contains(itemData.getTag())
 	 * @post !old(getView().getSelectedProductContainer().getTag().contains(itemData.getTag()))
 	 * @post containerData.getTag().contains(itemData.getTag())
 	 * 
 	 */
 	@Override
 	public void moveItemToContainer(ItemData itemData, ProductContainerData containerData) {
 		if (itemData == null)
 			throw new NullPointerException("ItemData should not be null.");
 		if (containerData == null)
 			throw new NullPointerException("ProductContainerData should not be null.");
 
 		ProductContainer targetContainer = (ProductContainer) containerData.getTag();
 		if (targetContainer == null)
 			throw new NullPointerException("Target product container must have a tag.");
 
		// note: the currently-selected ProductContainer is the source
		// getSelectedProductContainerTag().moveIntoContainer(getSelectedItemTag(),
		// targetContainer);

 		Item firstItem = (Item) itemData.getTag();
 		StorageUnit su = getProductContainerManager().getRootStorageUnitForChild(
 				targetContainer);
 		StorageUnit thisSu = getProductContainerManager().getRootStorageUnitForChild(
 				getSelectedProductContainerTag());
 		if (!su.equals(thisSu)) {
 			thisSu.moveIntoContainer(firstItem, su);
 		}
 	}
 
 	/**
 	 * This method is called when the selected item container changes.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public void productContainerSelectionChanged() {
 		productListener.updateProducts(true);
 	}
 
 	/**
 	 * This method is called when the selected product changes.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public void productSelectionChanged() {
 		itemListener.updateItems(true);
 	}
 
 	/**
 	 * This method is called when the user selects the "Remove Item" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if !canRemoveItem()
 	 * 
 	 * @pre canRemoveItem()
 	 * @pre getSelectedItemTag() != null
 	 * @post !getItemManager().contains(old(getView().getSelectedItem().getTag()))
 	 */
 	@Override
 	public void removeItem() {
 		if (!canRemoveItem()) {
 			throw new IllegalStateException("Unable to remove Item");
 		}
 
 		ItemData itemData = getView().getSelectedItem();
 		if (itemData == null)
 			throw new NullPointerException("ItemData object should not be null");
 		Item item = (Item) itemData.getTag();
 		if (item == null)
 			throw new NullPointerException("Item object should not be null");
 		Product itemsProduct = item.getProduct();
 		if (itemsProduct == null)
 			throw new NullPointerException("Item should always have a product");
 		ItemManager itemManager = getItemManager();
 		ProductContainer container = item.getContainer();
 		container.remove(item, itemManager);
 
 		// update view
 		Set<Item> pcItems = container.getItemsForProduct(itemsProduct);
 		ItemData[] viewItems = new ItemData[pcItems.size()];
 		Iterator<Item> it = pcItems.iterator();
 		int counter = 0;
 		while (it.hasNext())
 			viewItems[counter++] = DataWrapper.wrap(it.next());
 		getView().setItems(viewItems);
 	}
 
 	/**
 	 * This method is called when the user selects the "Remove Items" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if(!canRemoveItems())
 	 * 
 	 * @pre canRemoveItems()
 	 * @post itemManager (from getItemManager() ) no longer contains any of the items matching
 	 *       those removed by the user.
 	 */
 	@Override
 	public void removeItems() {
 		if (!canRemoveItems()) {
 			throw new IllegalStateException("Unable to remove Items");
 		}
 		getView().displayRemoveItemBatchView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Transfer Items" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if(!canTransferItems())
 	 * 
 	 * @pre canTransferItems()
 	 * @post true
 	 */
 	@Override
 	public void transferItems() {
 		if (!canTransferItems()) {
 			throw new IllegalStateException("Unable to edit Storage Unit");
 		}
 		getView().displayTransferItemBatchView();
 	}
 
 	/** 
 	 * 
 	 */
 	public void updateView() {
 		// TODO: Implement me all in one place from InventoryListener so we can reduce code
 		// duplication!
 	}
 
 	private void deleteSelectedProductContainer() {
 		ProductContainer selectedSU = getSelectedProductContainerTag();
 		assert (selectedSU != null);
 
 		getProductContainerManager().unmanage(selectedSU);
 	}
 
 	private ProductContainer getSelectedProductContainerTag() {
 		ProductContainerData selectedPC = getView().getSelectedProductContainer();
 		assert (selectedPC != null);
 
 		ProductContainer selectedTag = (ProductContainer) selectedPC.getTag();
 		assert (selectedTag != null);
 
 		return selectedTag;
 	}
 
 	private Product getSelectedProductTag() {
 		ProductData selectedProduct = getView().getSelectedProduct();
 		assert (selectedProduct != null);
 
 		Product selectedTag = (Product) selectedProduct.getTag();
 		assert (selectedTag != null);
 
 		return selectedTag;
 	}
 
 	private ProductContainerData loadProductContainerData(ProductContainerData parentData,
 			ProductContainer container) {
 		ProductContainerData pcData = new ProductContainerData(container.getName());
 		pcData.setTag(container);
 		parentData.addChild(pcData);
 		for (ProductGroup child : container.getProductGroups()) {
 			pcData = loadProductContainerData(pcData, child);
 		}
 		return parentData;
 	}
 
 	/**
 	 * Sets the enable/disable state of all components in the controller's view. A component
 	 * should be enabled only if the user is currently allowed to interact with that component.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post The enable/disable state of all components in the controller's view have been set
 	 * appropriately.}
 	 */
 	@Override
 	protected void enableComponents() {
 		return;
 	}
 
 	/**
 	 * Returns a reference to the view for this controller.
 	 */
 	@Override
 	protected IInventoryView getView() {
 		return (IInventoryView) super.getView();
 	}
 
 	/**
 	 * Loads data into the controller's view.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post The controller has loaded data into its view}
 	 */
 	@Override
 	protected void loadValues() {
 		ProductContainerData root = new ProductContainerData();
 		root.setTag(null);
 		Set<StorageUnit> storageUnits = getProductContainerManager().getStorageUnits();
 		for (StorageUnit su : storageUnits) {
 			root = loadProductContainerData(root, su);
 		}
 		getView().setProductContainers(root);
 	}
 }
