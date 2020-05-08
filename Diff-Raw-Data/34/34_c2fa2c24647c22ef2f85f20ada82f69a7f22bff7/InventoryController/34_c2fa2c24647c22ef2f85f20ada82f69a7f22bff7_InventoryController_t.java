 package gui.inventory;
 import static ch.lambdaj.Lambda.*;
 import gui.common.*;
 import gui.item.*;
 import gui.product.*;
 
 import java.util.*;
 
 import org.joda.time.DateTime;
 
 import model.common.Barcode;
 import model.common.ModelFacade;
 import model.common.Size;
 import model.common.Size.Unit;
 import model.item.Item;
 import model.item.ItemVault;
 import model.product.Product;
 import model.product.ProductVault;
 import model.productcontainer.*;
 
 /**
  * Controller class for inventory view.
  */
 public class InventoryController extends Controller 
 									implements IInventoryController, Observer {
 
 	ModelFacade bm  = new ModelFacade();
 	/**
 	 * Constructor.
 	 *  
 	 * @param view Reference to the inventory view
 	 */
 	public InventoryController(IInventoryView view) {
 		super(view);
 		this.addSampleItems();
 		construct();
 		
         StorageUnitVault.getInstance().addObserver(this);
         ProductGroupVault.getInstance().addObserver(this);
         ProductVault.getInstance().addObserver(this);
         ItemVault.getInstance().addObserver(this);
 	}
 
 	/**
 	 * Returns a reference to the view for this controller.
 	 */
 	@Override
 	protected IInventoryView getView() {
 		return (IInventoryView)super.getView();
 	}
 	
 	
 	private ProductContainerData currentlySelectedPC;
 	private ProductData currentlySelectedP;
 	private int currentlySelectedPId = -1;
 	private int currentlySelectedPCId = -1;
 	
 	public void setCurrentlySelectedProduct(int i){
 		this.currentlySelectedPId = i;
 	}
 	public void setCurrentlySelectedProductContainer(int i){
 		this.currentlySelectedPCId = i;
 	}
 	/**
 	 * Loads data into the controller's view.
 	 * 
 	 *  {@pre None}
 	 *  
 	 *  {@post The controller has loaded data into its view}
 	 */
 	@Override
 	protected void loadValues() {
 		ProductContainerData root = new ProductContainerData();
 		//currentlySelectedPC = getView().getSelectedProductContainer();
 		//if(currentlySelectedPC != null)
 		//	currentlySelectedPCId = ((Number) currentlySelectedPC.getTag()).intValue();
 		
 		//Get all available storage units
 		ArrayList<StorageUnit> storageUnits = new ArrayList<StorageUnit>();
 		storageUnits = bm.storageUnitVault.findAll("Deleted = false");
 		//For each storage unit add all it's children productGroups
 		for(StorageUnit su : storageUnits){
 			ProductContainerData tempSU = new ProductContainerData(su.getName());
 			if(su.getId() == this.currentlySelectedPCId)
 				this.currentlySelectedPC = tempSU;
             //tempSU.setTag(su.getId());
 			root.addChild(addChildrenProductContainers(su,tempSU));
 		}
 		
 		getView().setProductContainers(root);
 		if(currentlySelectedPC != null)
 			getView().selectProductContainer(currentlySelectedPC);
 		if(getView().getSelectedProductContainer() != null)
 			this.productContainerSelectionChanged();
 	}
 
 	private void addSampleItems(){
 		StorageUnit su1 = new StorageUnit();
         su1.setName("Unit A");
         StorageUnit su2 = new StorageUnit();
         su2.setName("Unit B");
         StorageUnit su3 = new StorageUnit();
         su3.setName("Unit C");
         
         su1.validate();
         su2.validate();
         su3.validate();
         su1.save();
         su2.save();
         su3.save();
         
         
         ProductGroup pg1 = new ProductGroup();
         pg1.setName("Group A");
         pg1.setParentId(su3.getId());
         //pg1.setRootParentId(su3.getId());
         pg1.set3MonthSupply(new Size(3, Size.Unit.oz));
         
         ProductGroup pg2 = new ProductGroup();
         pg2.setName("Group B");
         pg2.setParentId(su1.getId());
         //pg2.setRootParentId(su1.getId());
         pg2.set3MonthSupply(new Size(3, Size.Unit.oz));
         
         
         pg1.validate();
         pg2.validate();
         
         pg1.save();
         pg2.save();
         
         ProductGroup pg3 = new ProductGroup();
         pg3.setName("Group C");
         pg3.setParentId(pg2.getId());
         //pg3.setRootParentId(su1.getId());
         pg3.set3MonthSupply(new Size(3, Size.Unit.oz));
         pg3.validate();
         pg3.save();
         
         
         Product p1 = new Product();
         p1.setStorageUnitId(su1.getId());
         p1.set3MonthSupply(3);
         p1.setBarcode(new Barcode("1111"));
         p1.setContainerId(pg2.getId());
         p1.setDescription("This is such a great description");
         p1.setShelfLife(5);
         p1.setCreationDate(new DateTime());
         p1.setSize(new Size(123,Unit.oz));
         p1.validate();
         p1.save();
         
         Item i1 = new Item();
         //i1.setBarcode(new Barcode());
         i1.setEntryDate(new DateTime());
         i1.setExitDate(new DateTime());
         i1.setExpirationDate(new DateTime());
         i1.setProductId(p1.getId());
         i1.validate();
         i1.save();
         
         Item i2 = new Item();
         //i1.setBarcode(new Barcode());
         i2.setEntryDate(new DateTime());
         i2.setExitDate(new DateTime());
         i2.setExpirationDate(new DateTime());
         i2.setProductId(p1.getId());
         i2.validate();
         i2.save();
         
 	}
 	/*
 	 * Add all children to pc, recursive call
 	 */
 	private ProductContainerData addChildrenProductContainers(ProductContainer pc, ProductContainerData pcData){
 		//Get list of all productGroups in PC
 		ArrayList<ProductGroup> productGroups = new ArrayList<ProductGroup>();
 		productGroups = bm.productGroupVault.findAll("ParentIdString = "+pc.getId());
 		
 		//Loop through each product group and add it to PC
 		for(ProductGroup pg : productGroups){
 			
 			//Create a new data object from the product group
 			ProductContainerData tempPC = new ProductContainerData(pg.getName());
 			pcData.addChild(addChildrenProductContainers(pg,tempPC));
 			
 			if(pg.getId() == this.currentlySelectedPCId)
 				this.currentlySelectedPC = tempPC;
 		}
 		pcData.setTag(pc.getId());
 		return pcData;
 	}
 	
 	
 	
 	/**
 	 * Sets the enable/disable state of all components in the controller's view.
 	 * A component should be enabled only if the user is currently
 	 * allowed to interact with that component.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post The enable/disable state of all components in the controller's view
 	 * have been set appropriately.}
 	 */
 	@Override
 	protected void enableComponents() {
 		return;
 	}
 	
 	//
 	// IInventoryController overrides
 	//
 
 	/**
 	 * Returns true if and only if the "Add Storage Unit" menu item should be enabled.
 	 * 
 	 * This is always enabled
 	 */
 	@Override
 	public boolean canAddStorageUnit() {
 		return true;
 	}
 	
 	/**
 	 * Returns true if and only if the "Add Items" menu item should be enabled.
 	 * 
 	 * This is always enabled
 	 */
 	@Override
 	public boolean canAddItems() {
 		return true;
 	}
 	
 	/**
 	 * Returns true if and only if the "Transfer Items" menu item should be enabled.
 	 * 
 	 * This is always enabled
 	 */
 	@Override
 	public boolean canTransferItems() {
 		return true;
 	}
 	
 	/**
 	 * Returns true if and only if the "Remove Items" menu item should be enabled.
 	 * 
 	 * This is always enabled
 	 */
 	@Override
 	public boolean canRemoveItems() {
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Delete Storage Unit" menu item should be enabled.
 	 * 
 	 * *Can only be deleted if there are no items
 	 */
 	@Override
 	public boolean canDeleteStorageUnit() {
 		ProductContainerData selectedContainerData = getView().getSelectedProductContainer();
 		
 		if (selectedContainerData != null) {	
 			int id = -1;
 			if(selectedContainerData.getTag() != null)
 			  id = ((Number) selectedContainerData.getTag()).intValue();
 			ProductGroup selectedProductGroup = bm.productGroupVault.get(id);
 			StorageUnit selectedStorageUnit = bm.storageUnitVault.get(id);
 			if(selectedProductGroup!=null){
 				return selectedProductGroup.isDeletable().getStatus();
 			} else {
 				return selectedStorageUnit.isDeletable().getStatus();
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * This method is called when the user selects the "Delete Storage Unit" menu item.
 	 * 
 	 * Must delete it's self as well as all sum children
 	 */
 	@Override
 	public void deleteStorageUnit() {
 		ProductContainerData selectedContainerData = getView().getSelectedProductContainer();
 		int id = -1;
 		if(selectedContainerData.getTag() != null)
 		  id = ((Number) selectedContainerData.getTag()).intValue();
 		StorageUnit selectedStorageUnit = bm.storageUnitVault.get(id);
 		
 		selectedStorageUnit.delete();
 		selectedStorageUnit.save();
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Storage Unit" menu item should be enabled.
 	 * 
 	 * This is always enabled
 	 */
 	@Override
 	public boolean canEditStorageUnit() {
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Add Product Group" menu item should be enabled.
 	 * 
 	 * This is always enabled
 	 */
 	@Override
 	public boolean canAddProductGroup() {
 		return true;
 	}
 
 	/**
 	 * Returns true if and only if the "Delete Product Group" menu item should be enabled.
 	 * 
 	 * * Same as storage unit
 	 */
 	@Override
 	public boolean canDeleteProductGroup() {
 		return this.canDeleteStorageUnit();
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Product Group" menu item should be enabled.
 	 * 
 	 * This is always enabled
 	 */
 	@Override
 	public boolean canEditProductGroup() {
 		return true;
 	}
 	
 	/**
 	 * This method is called when the user selects the "Delete Product Group" menu item.
 	 */
 	@Override
 	public void deleteProductGroup() {
 		ProductContainerData selectedContainerData = getView().getSelectedProductContainer();
 		int id = -1;
 		if(selectedContainerData.getTag() != null)
 		  id = ((Number) selectedContainerData.getTag()).intValue();
 		ProductGroup selectedProductGroup = bm.productGroupVault.get(id);
 		
 		selectedProductGroup.delete();
 		selectedProductGroup.save();
 	}
 
 	private Random rand = new Random();
 	
 	private String getRandomBarcode() {
 		Random rand = new Random();
 		StringBuilder barcode = new StringBuilder();
 		for (int i = 0; i < 12; ++i) {
 			barcode.append(((Integer)rand.nextInt(10)).toString());
 		}
 		return barcode.toString();
 	}
 
 	/**
 	 * This method is called when the selected item container changes.
 	 */
 	@Override
 	public void productContainerSelectionChanged() {
 		List<ProductData> productDataList = new ArrayList<ProductData>();		
 		ProductContainerData selectedContainerData = getView().getSelectedProductContainer();
 		
 		
 		if (selectedContainerData != null) {
 			//Get list of all productGroups in PC
 			ArrayList<Product> products = new ArrayList<Product>();
 
 			int id = -1;
 			if(selectedContainerData.getTag() != null)
 			  id = ((Number) selectedContainerData.getTag()).intValue();
 			ProductGroup selectedProductGroup = bm.productGroupVault.get(id);
 			StorageUnit selectedStorageUnit = bm.storageUnitVault.get(id);
 			
 			
 			this.currentlySelectedPCId = id;
 			
 			//Is a storage unit or a product group selected
 			if(selectedStorageUnit != null){
 				products = bm.productVault.findAll("StorageUnitId = "+selectedStorageUnit.getId());
 				getView().setContextUnit(selectedStorageUnit.getName());
 				getView().setContextGroup("");
 				getView().setContextSupply("");
 			}
 			else if(selectedProductGroup != null){
 				products = bm.productVault.findAll("ContainerId = "+selectedProductGroup.getId());
 				getView().setContextUnit(selectedProductGroup.getStorageUnit().getName());
 				getView().setContextGroup(selectedProductGroup.getName());
 				getView().setContextSupply(selectedProductGroup.get3MonthSupply().toString());
 			} else {
 				products = bm.productVault.findAll("Deleted = false");
 				getView().setContextUnit("All");
 				getView().setContextGroup("");
 				getView().setContextSupply("");
 			}
 			
 			
 			for(Product tempProduct : products){
 				
 					
 				ProductData productData = new ProductData();			
 				productData.setBarcode(tempProduct.getBarcodeString());
 				productData.setCount(Integer.toString(tempProduct.getItemCount()));
 				productData.setDescription(tempProduct.getDescription());
 				productData.setShelfLife(Integer.toString(tempProduct.getShelfLife()));
 				productData.setSize(tempProduct.getSize().toString());
 				productData.setSupply(Integer.toString(tempProduct.get3MonthSupply()));
 				productData.setTag(tempProduct.getId());
 				productDataList.add(productData);
 				
 				if(tempProduct.getId() == this.currentlySelectedPId)
 					this.currentlySelectedP = productData;
 			}
 		}
 		getView().setItems(new ItemData[0]);
 		getView().setProducts(productDataList.toArray(new ProductData[0]));
 		if(currentlySelectedP != null){
 			getView().selectProduct(this.currentlySelectedP);
 			if(getView().getSelectedProduct() != null)
 				this.productSelectionChanged();
 		}
 			
 		
 	}
 
 	/**
 	 * This method is called when the selected item changes.
 	 */
 	@Override
 	public void productSelectionChanged() {
 		List<ItemData> itemDataList = new ArrayList<ItemData>();		
 		ProductData selectedProductData = getView().getSelectedProduct();
 		int id = -1;
 		if(selectedProductData.getTag() != null)
 		  id = ((Number) selectedProductData.getTag()).intValue();
 		Product selectedProduct = bm.productVault.get(id);
 		
 		this.currentlySelectedPId = id;
 		
 		
 		if (selectedProduct != null) {
 			ArrayList<Item> items = new ArrayList<Item>();
 			items = bm.itemVault.findAll("ProductId = "+id);
 			for(Item tempItem : items){
 				ItemData itemData = new ItemData();
				itemData.setBarcode(tempItem.getBarcodeString());
 				itemData.setEntryDate(tempItem.getEntryDate().toDate());
 				itemData.setExpirationDate(tempItem.getExpirationDate().toDate());
 				
 				Product pro = tempItem.getProduct();
 				ProductContainer cd = pro.getContainer();
 				if(cd!=null)
 					itemData.setProductGroup(cd.getName());
 				itemData.setStorageUnit(tempItem.getProduct().getStorageUnit().getName());
 				itemData.setTag(tempItem.getId());
 				itemDataList.add(itemData);
 			}
 		}
 		getView().setItems(itemDataList.toArray(new ItemData[0]));
 	}
 
 	/**
 	 * This method is called when the selected item changes.
 	 */
 	@Override
 	public void itemSelectionChanged() {
 		return;
 	}
 
 	/**
 	 * Returns true if and only if the "Delete Product" menu item should be enabled.
 	 */
 	@Override
 	public boolean canDeleteProduct() {
 		if(this.getView().getSelectedProduct() == null)
 			return false;
 		ProductData selectedProductData = getView().getSelectedProduct();
 		int id = -1;
 		if(selectedProductData.getTag() != null)
 		  id = ((Number) selectedProductData.getTag()).intValue();	
 		Product selectedProduct = bm.productVault.get(id);
 		return selectedProduct.isDeleteable().getStatus();
 	}
 
 	/**
 	 * This method is called when the user selects the "Delete Product" menu item.
 	 */
 	@Override
 	public void deleteProduct() {
 		ProductData selectedProductData = getView().getSelectedProduct();
 		int id = -1;
 		if(selectedProductData.getTag() != null)
 		  id = ((Number) selectedProductData.getTag()).intValue();
 		Product selectedProduct = bm.productVault.get(id);
 		
 		selectedProduct.delete();
 		selectedProduct.save();
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Item" menu item should be enabled.
 	 * 
 	 * 
 	 */
 	@Override
 	public boolean canEditItem() {
 		if(this.getView().getSelectedItem() == null)
 			return false;
 		return true;
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Item" menu item.
 	 */
 	@Override
 	public void editItem() {
 		getView().displayEditItemView();
 	}
 
 	/**
 	 * Returns true if and only if the "Remove Item" menu item should be enabled.
 	 */
 	@Override
 	public boolean canRemoveItem() {
 		if(this.getView().getSelectedItem() == null)
 			return false;
 		return true;
 	}
 
 	/**
 	 * This method is called when the user selects the "Remove Item" menu item.
 	 */
 	@Override
 	public void removeItem() {
 		ItemData selectedItemData = getView().getSelectedItem();
 		int id = -1;
 		if(selectedItemData.getTag() != null)
 		  id = ((Number) selectedItemData.getTag()).intValue();
 		Item selectedItem = bm.itemVault.get(id);
 		
 		selectedItem.delete();
 		selectedItem.save();
 	}
 
 	/**
 	 * Returns true if and only if the "Edit Product" menu item should be enabled.
 	 */
 	@Override
 	public boolean canEditProduct() {
 		if(this.getView().getSelectedProduct() == null)
 			return false;
 		return true;
 	}
 
 	/**
 	 * This method is called when the user selects the "Add Product Group" menu item.
 	 */
 	@Override
 	public void addProductGroup() {
 		getView().displayAddProductGroupView();
 	}
 	
 	/**
 	 * This method is called when the user selects the "Add Items" menu item.
 	 */
 	@Override
 	public void addItems() {
 		getView().displayAddItemBatchView();
 	}
 	
 	/**
 	 * This method is called when the user selects the "Transfer Items" menu item.
 	 */
 	@Override
 	public void transferItems() {
 		getView().displayTransferItemBatchView();
 	}
 	
 	/**
 	 * This method is called when the user selects the "Remove Items" menu item.
 	 */
 	@Override
 	public void removeItems() {
 		getView().displayRemoveItemBatchView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Add Storage Unit" menu item.
 	 */
 	@Override
 	public void addStorageUnit() {
 		getView().displayAddStorageUnitView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Product Group" menu item.
 	 */
 	@Override
 	public void editProductGroup() {
 		getView().displayEditProductGroupView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Storage Unit" menu item.
 	 */
 	@Override
 	public void editStorageUnit() {
 		getView().displayEditStorageUnitView();
 	}
 
 	/**
 	 * This method is called when the user selects the "Edit Product" menu item.
 	 */
 	@Override
 	public void editProduct() {
 		getView().displayEditProductView();
 	}
 	
 	/**
 	 * This method is called when the user drags a product into a
 	 * product container.
 	 * 
 	 * @param productData Product dragged into the target product container
 	 * @param containerData Target product container
 	 */
 	@Override
 	public void addProductToContainer(ProductData productData, 
 										ProductContainerData containerData) {	
 		int id = -1;
 		if(productData.getTag() != null)
 		  id = ((Number) productData.getTag()).intValue();
 		Product selectedProduct = bm.productVault.get(id);
 		
 		id = -1;
 		if(containerData.getTag() != null)
 		  id = ((Number) containerData.getTag()).intValue();
 		ProductGroup selectedProductGroup = bm.productGroupVault.get(id);
 		StorageUnit selectedStorageUnit = bm.storageUnitVault.get(id);
 		
 		this.currentlySelectedPC = null;
 		this.currentlySelectedPCId = id;
 		if(selectedProductGroup!=null){
 			bm.MoveProduct(selectedProductGroup.getStorageUnit(), selectedProductGroup, selectedProduct);
 		} else {
 			bm.MoveProduct(selectedStorageUnit, selectedStorageUnit, selectedProduct);
 		}
 		
 	}
 
 	/**
 	 * This method is called when the user drags an item into
 	 * a product container.
 	 * 
 	 * @param itemData Item dragged into the target product container
 	 * @param containerData Target product container
 	 */
 	@Override
 	public void moveItemToContainer(ItemData itemData,
 									ProductContainerData containerData) {
 		int id = -1;
 		if(itemData.getTag() != null)
 		  id = ((Number) itemData.getTag()).intValue();
 		Item selectedItem = bm.itemVault.get(id);
 		
 		id = -1;
 		if(containerData.getTag() != null)
 		  id = ((Number) containerData.getTag()).intValue();
 		ProductGroup selectedProductGroup = bm.productGroupVault.get(id);
 		StorageUnit selectedStorageUnit = bm.storageUnitVault.get(id);
 		
 		if(selectedProductGroup!=null){
 			bm.MoveItem(selectedProductGroup.getStorageUnit(), selectedItem);
 		} else {
 			bm.MoveItem(selectedStorageUnit, selectedItem);
 		}
 		
 	}
 
     /**
      * This method is called when the observed vaults are changes
      *
      * @param o Vault that is observed
      * @param arg Hint
      */
     @Override
     public void update(Observable o, Object arg) {
     	this.loadValues();
     }
 }
 
