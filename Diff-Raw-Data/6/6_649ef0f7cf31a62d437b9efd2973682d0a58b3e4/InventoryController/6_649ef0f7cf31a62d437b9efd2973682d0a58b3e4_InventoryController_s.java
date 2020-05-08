 package gui.inventory;
 
 import gui.common.Controller;
 import gui.item.ItemData;
 import gui.product.ProductData;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import model.Item;
 import model.Product;
 import model.ProductContainer;
import model.ProductContainerManager;
 import model.ProductGroup;
 import model.StorageUnit;
 
 /**
  * Controller class for inventory view.
  */
 public class InventoryController extends Controller implements IInventoryController {
 	private final Random rand = new Random();
 
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
 		ProductContainer container = (ProductContainer) containerData.getTag();
 		if (container == null)
 			throw new IllegalStateException("ProductContainer must have a tag.");
 
 		container.add(productToAdd);
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
 		// Always enabled, since it is only called when the Item context menu is displayed
 		// See Functional Spec p22
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Product" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canEditProduct() {
 		// Always enabled, since it is only called when the Product context menu is displayed
 		// See Functional Spec p21
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Product Group" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canEditProductGroup() {
 		// Always enabled per Functional Spec p16
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Storage Unit" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canEditStorageUnit() {
 		// Always enabled per Functional Spec p14
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Remove Item" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canRemoveItem() {
 		// Always enabled per Functional Spec p26
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Remove Items" menu item should be enabled.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean canRemoveItems() {
 		// Always enabled per Functional Spec p26
 		return true;
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
 	 * @post !productManager.contains(old(getView().getSelectedProduct().getTag()))
 	 */
 	@Override
 	public void deleteProduct() {
 		if (!canDeleteProduct()) {
 			throw new IllegalStateException("Unable to delete Product");
 		}
 
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
 	 * @post !productContainerManager.contains(PREVIOUS
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
 	 * @post !productContainerManager.contains(PREVIOUS
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
 	 * @post itemManager.contains(old(getView().getSelectedItem().getTag()))
 	 */
 	@Override
 	public void editItem() {
 		if (!canEditItem()) {
 			throw new IllegalStateException("Unable to edit Item");
 		}
 		getView().displayEditItemView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Product" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if (!canEditProduct())
 	 * 
 	 * @pre canEditProduct()
 	 * @post productManager.contains(old(getView().getSelectedProduct().getTag()))
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
 	 * @post productContainerManager.contains(PREVIOUS
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
 	 * @post productContainerManager.contains(PREVIOUS
 	 *       getView().getSelectedProductContainer().getTag())
 	 */
 	@Override
 	public void editStorageUnit() {
 		if (!canEditStorageUnit()) {
 			throw new IllegalStateException("Unable to edit Storage Unit");
 		}
 		getView().displayEditStorageUnitView();
 		loadValues();
 	}
 
 	/**
 	 * This method is called when the selected item changes.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public void itemSelectionChanged() {
 		// Shouldn't really be anything to do here, but I'm not sure.
 		return;
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
 			throw new IllegalArgumentException("ItemData should not be null.");
 		if (containerData == null)
 			throw new IllegalArgumentException("ProductContainerData should not be null.");
 
 		ProductContainer targetContainer = (ProductContainer) containerData.getTag();
 		if (targetContainer == null)
 			throw new IllegalStateException("Target product container must have a tag.");
 
 		// note: the currently-selected ProductContainer is the source
 		getSelectedProductContainerTag().moveIntoContainer(getSelectedItemTag(),
 				targetContainer);
 	}
 
 	/**
 	 * This method is called when the selected item container changes.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public void productContainerSelectionChanged() {
 		// TODO: load productDataList from Model
 		List<ProductData> productDataList = new ArrayList<ProductData>();
 		ProductContainerData selectedContainer = getView().getSelectedProductContainer();
 		if (selectedContainer != null) {
 			int productCount = rand.nextInt(20) + 1;
 			for (int i = 1; i <= productCount; ++i) {
 				ProductData productData = new ProductData();
 				productData.setBarcode(getRandomBarcode());
 				int itemCount = rand.nextInt(25) + 1;
 				productData.setCount(Integer.toString(itemCount));
 				productData.setDescription("Item " + i);
 				productData.setShelfLife("3 months");
 				productData.setSize("1 pounds");
 				productData.setSupply("10 count");
 
 				productDataList.add(productData);
 			}
 		}
 		getView().setProducts(productDataList.toArray(new ProductData[0]));
 
 		// TODO: Load itemDataList from Model
 		List<ItemData> itemDataList = new ArrayList<ItemData>();
 		getView().setItems(itemDataList.toArray(new ItemData[0]));
 	}
 
 	/**
 	 * This method is called when the selected product changes.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public void productSelectionChanged() {
 		// TODO: Load itemDataList from Model
 		List<ItemData> itemDataList = new ArrayList<ItemData>();
 		ProductData selectedProduct = getView().getSelectedProduct();
 		if (selectedProduct != null) {
 			Date now = new Date();
 			GregorianCalendar cal = new GregorianCalendar();
 			int itemCount = Integer.parseInt(selectedProduct.getCount());
 			for (int i = 1; i <= itemCount; ++i) {
 				cal.setTime(now);
 				ItemData itemData = new ItemData();
 				itemData.setBarcode(getRandomBarcode());
 				cal.add(Calendar.MONTH, -rand.nextInt(12));
 				itemData.setEntryDate(cal.getTime());
 				cal.add(Calendar.MONTH, 3);
 				itemData.setExpirationDate(cal.getTime());
 				itemData.setProductGroup("Some Group");
 				itemData.setStorageUnit("Some Unit");
 
 				itemDataList.add(itemData);
 			}
 		}
 		getView().setItems(itemDataList.toArray(new ItemData[0]));
 	}
 
 	/**
 	 * This method is called when the user selects the "Remove Item" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if !canRemoveItem()
 	 * 
 	 * @pre canRemoveItem()
 	 * @pre getSelectedItemTag() != null
 	 * @post !itemManager.contains(old(getView().getSelectedItem().getTag()))
 	 */
 	@Override
 	public void removeItem() {
 		if (!canRemoveItem()) {
 			throw new IllegalStateException("Unable to remove Item");
 		}
 
 		getItemManager().unmanage(getSelectedItemTag());
 	}
 
 	/**
 	 * This method is called when the user selects the "Remove Items" menu item.
 	 * 
 	 * @throws IllegalStateException
 	 *             if(!canRemoveItems())
 	 * 
 	 * @pre canRemoveItems()
 	 * @post itemManager no longer contains any of the items matching those removed by the
 	 *       user.
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
 
 	private void deleteSelectedProductContainer() {
 		ProductContainer selectedSU = getSelectedProductContainerTag();
 		assert (selectedSU != null);
 
 		getProductContainerManager().unmanage(selectedSU);
 
 		loadValues();
 	}
 
 	private String getRandomBarcode() {
 		Random rand = new Random();
 		StringBuilder barcode = new StringBuilder();
 		for (int i = 0; i < 12; ++i) {
 			barcode.append(((Integer) rand.nextInt(10)).toString());
 		}
 		return barcode.toString();
 	}
 
 	private Item getSelectedItemTag() {
 		ItemData selectedItem = getView().getSelectedItem();
 		assert (selectedItem != null);
 
 		Item selectedTag = (Item) selectedItem.getTag();
 		assert (selectedTag != null);
 
 		return selectedTag;
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
 		Iterator<ProductGroup> productGroupIterator = container.getProductGroupIterator();
 		while (productGroupIterator.hasNext()) {
 			ProductGroup child = productGroupIterator.next();
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
		ProductContainerManager manager = getView().getProductContainerManager();
		Iterator<StorageUnit> storageUnitIterator = manager.getStorageUnitIterator();
 		while (storageUnitIterator.hasNext()) {
 			ProductContainer pc = storageUnitIterator.next();
 			root = loadProductContainerData(root, pc);
 		}
 		getView().setProductContainers(root);
 	}
 }
