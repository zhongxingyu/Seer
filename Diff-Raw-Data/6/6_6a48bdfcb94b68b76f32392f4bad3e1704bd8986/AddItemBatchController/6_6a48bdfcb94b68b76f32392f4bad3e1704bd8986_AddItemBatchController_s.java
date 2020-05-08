 package gui.batches;
 
 import gui.common.Controller;
 import gui.common.DataWrapper;
 import gui.common.IView;
 import gui.inventory.ProductContainerData;
 import gui.item.ItemData;
 import gui.product.ProductData;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import model.BarcodePrinter;
 import model.Item;
 import model.Product;
 import model.ProductContainer;
 import model.ProductManager;
 import model.ProductQuantity;
 import model.Unit;
 import model.undo.AddItems;
 import model.undo.AddProduct;
 import model.undo.UndoManager;
 
 import common.NonEmptyString;
 
 /**
  * Controller class for the add item batch view.
  */
 public class AddItemBatchController extends Controller implements IAddItemBatchController {
 	/**
 	 * Task runs when barcode scanner is enabled
 	 * 
 	 * @author Matthew
 	 * 
 	 */
 	private class CreateItemTask extends TimerTask {
 
 		@Override
 		public void run() {
 			if (getView().getUseScanner() && getView().getBarcode().length() > 0)
 				addItem();
 		}
 	}
 
 	ArrayList<ArrayList<ItemData>> items;
 	ArrayList<ProductData> products;
 	ProductContainer container;
 	UndoManager undoManager;
 	private final int maxAddableItems = 1000000;
 
 	private Timer timer;
 	private CreateItemTask createItemTask;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param view
 	 *            Reference to the add item batch view.
 	 * @param target
 	 *            Reference to the storage unit to which items are being added.
 	 */
 	public AddItemBatchController(IView view, ProductContainerData target) {
 		super(view);
 		undoManager = new UndoManager();
 		timer = new Timer();
 		createItemTask = new CreateItemTask();
 
 		getView().setUseScanner(true);
 		getView().setCount("1");
 		items = new ArrayList<ArrayList<ItemData>>();
 		products = new ArrayList<ProductData>();
 		container = (ProductContainer) target.getTag();
 		getView().giveBarcodeFocus();
 		construct();
 	}
 
 	/**
 	 * This method is called when the user clicks the "Add Item" button in the add item batch
 	 * view.
 	 * 
 	 * @post The item specified by the fields on AddItemBatchView now exists in the selected
 	 *       StorageUnit
 	 */
 	@Override
 	public void addItem() {
 		String productBarcode = getView().getBarcode();
 		ProductManager productManager = getProductManager();
 		productManager.setPendingProductCommand(null);
 		Product product = null;
 
 		if (productManager.containsProduct(productBarcode)) {
 			product = productManager.getByBarcode(productBarcode);
 
 		} else {
 			getView().displayAddProductView();
 		}
 
 		Date entryDate = getView().getEntryDate();
 		int itemCount = Integer.parseInt(getView().getCount());
 
 		AddProduct addProduct = (AddProduct) productManager.getPendingProductCommand();
 		if (addProduct == null) {
 			if (product == null) // User hit "Cancel" in AddProductView; do nothing
 				return;
 			/*
 			 * addProduct = new AddProduct(product.getBarcode(), product.getDescription(),
 			 * product.getShelfLife(), product.getThreeMonthSupply(), product
 			 * .getProductQuantity().getQuantity(), product.getProductQuantity() .getUnits(),
 			 * productManager);
 			 */
 		} else
 			addProduct.setContainer(container);
 
 		AddItems addItemsCommand = new AddItems(container, addProduct, product, entryDate,
 				itemCount, productManager, getItemManager());
 
 		undoManager.execute(addItemsCommand);
 
 		updateViewAfterExecute(addItemsCommand);
 
 		// Clear the view for the next item!
 		getView().setBarcode("");
 		getView().setCount("1");
 		enableComponents();
 		getView().giveBarcodeFocus();
 	}
 
 	/**
 	 * This method is called when the "Product Barcode" field in the add item batch view is
 	 * changed by the user.
 	 */
 	@Override
 	public void barcodeChanged() {
 		if (getView().getUseScanner())
 			setTimer();
 		enableComponents();
 	}
 
 	/**
 	 * This method is called when the "Count" field in the add item batch view is changed by
 	 * the user.
 	 */
 	@Override
 	public void countChanged() {
 		enableComponents();
 	}
 
 	/**
 	 * This method is called when the user clicks the "Done" button in the add item batch view.
 	 */
 	@Override
 	public void done() {
 		getView().close();
 		if (BarcodePrinter.getInstance().hasItemsToPrint()) {
 			NonEmptyString filename = BarcodePrinter.getInstance().printBatch();
 			File file = new File(filename.toString());
 			try {
 				java.awt.Desktop.getDesktop().open(file);
 			} catch (IOException e) {
 				getView().displayErrorMessage(e.getMessage());
 			} catch (IllegalArgumentException e) {
 				getView().displayErrorMessage(e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * This method is called when the "Entry Date" field in the add item batch view is changed
 	 * by the user.
 	 */
 	@Override
 	public void entryDateChanged() {
 	}
 
 	/**
 	 * This method is called when the user clicks the "Redo" button in the add item batch view.
 	 */
 	@Override
 	public void redo() {
 		updateViewAfterExecute((AddItems) undoManager.redo());
 		enableComponents();
 	}
 
 	/**
 	 * This method is called when the selected product changes in the add item batch view.
 	 */
 	@Override
 	public void selectedProductChanged() {
 		refreshItems();
 	}
 
 	/**
 	 * This method is called when the user clicks the "Undo" button in the add item batch view.
 	 */
 	@Override
 	public void undo() {
 		updateViewAfterUndo((AddItems) undoManager.undo());
 		enableComponents();
 	}
 
 	/**
 	 * This method is called when the "Use Barcode Scanner" setting in the add item batch view
 	 * is changed by the user.
 	 */
 	@Override
 	public void useScannerChanged() {
 		if (getView().getUseScanner()) {
 			getView().setBarcode("");
 		}
 	}
 
 	private ProductData addProduct(Product product) {
 		ProductData productData = DataWrapper.wrap(product, 0);
 		products.add(productData);
 		items.add(new ArrayList<ItemData>());
 		refreshProducts();
 		return productData;
 	}
 
 	private void refreshItems() {
 		ItemData[] id = new ItemData[0];
 		int index = products.indexOf(getView().getSelectedProduct());
 		if (index >= 0)
 			getView().setItems(items.get(index).toArray(id));
 	}
 
 	private void refreshProducts() {
 		ProductData[] pd = new ProductData[0];
 		getView().setProducts(products.toArray(pd));
 	}
 
 	private void setTimer() {
 		createItemTask.cancel();
 		createItemTask = new CreateItemTask();
 		timer.schedule(createItemTask, 700l);
 	}
 
 	private void updateViewAfterExecute(AddItems addItemsCommand) {
 		ProductData selectedProduct = getView().getSelectedProduct();
 		ItemData selectedItem = getView().getSelectedItem();
 		// AddProduct addProductCommand = addItemsCommand.getAddProductCommand();
 		ProductManager productManager = getProductManager();
 		String productBarcode = null;
 		// if (addProductCommand != null)
 		// productBarcode = addProductCommand.getBarcode();
 		// else
 		productBarcode = addItemsCommand.getProduct().getBarcode();
 		int itemCount = addItemsCommand.getItemCount();
 
 		Product product = productManager.getByBarcode(productBarcode);
 		ProductData productData = null;
 		boolean found = false;
 		for (ProductData pd : products) {
 			if (pd.getBarcode().equals(product.getBarcode())) {
 				productData = pd;
 				found = true;
 			}
 		}
 		if (!found) {
 			productData = addProduct(product);
 		}
 
 		for (Item item : addItemsCommand.getAddedItems()) {
 			ItemData id = DataWrapper.wrap(item);
 			items.get(products.indexOf(productData)).add(id);
 		}
 		refreshItems();
 
 		int count = Integer.parseInt(productData.getCount()) + itemCount;
 		productData.setCount("" + count);
 		refreshProducts();
 
 		refreshProducts();
 
 		if (selectedProduct != null)
 			getView().selectProduct(selectedProduct);
 		if (selectedItem != null)
 			getView().selectItem(selectedItem);
 	}
 
 	private void updateViewAfterUndo(AddItems addItemsCommand) {
 		ProductData selectedProduct = getView().getSelectedProduct();
 		ItemData selectedItem = getView().getSelectedItem();
 		AddProduct addProductCommand = addItemsCommand.getAddProductCommand();
 		String productBarcode = null;
 		if (addProductCommand != null)
 			productBarcode = addProductCommand.getBarcode();
 		else
 			productBarcode = addItemsCommand.getProduct().getBarcode();
 		int itemCount = addItemsCommand.getItemCount();
 		ProductData productData = null;
 		for (int i = 0; i < products.size(); i++) {
 			productData = products.get(i);
 			if (productData.getBarcode().equals(productBarcode)) {
 				Set<Item> addedItems = addItemsCommand.getAddedItems();
 				ArrayList<ItemData> itemsToRemove = new ArrayList<ItemData>();
 				for (ItemData itemData : items.get(i)) {
 					Item item = (Item) itemData.getTag();
 					if (addedItems.contains(item)) {
 						itemsToRemove.add(itemData);
 					}
 				}
 				for (ItemData itemData : itemsToRemove)
 					items.get(products.indexOf(productData)).remove(itemData);
 				refreshItems();
 				break;
 			}
 		}
 		assert (productData != null);
 		int count = Integer.parseInt(productData.getCount()) - itemCount;
 		productData.setCount("" + count);
 		if (count == 0)
 			products.remove(productData);
 		refreshProducts();
 		if (selectedProduct != null)
 			getView().selectProduct(selectedProduct);
 		if (selectedItem != null)
 			getView().selectItem(selectedItem);
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
 		getView().enableUndo(undoManager.canUndo());
 		getView().enableRedo(undoManager.canRedo());
 		boolean isValidCount = true;
 		try {
 			int count = Integer.parseInt(getView().getCount());
 			if (count > maxAddableItems)
 				isValidCount = false;
 			else
 				isValidCount = ProductQuantity.isValidProductQuantity(count, Unit.COUNT);
 		} catch (NumberFormatException e) {
 			isValidCount = false;
 		}
 		getView().enableItemAction(
				Product.isValidBarcode(getView().getBarcode()) && isValidCount);
 	}
 
 	/**
 	 * Returns a reference to the view for this controller.
 	 */
 	@Override
 	protected IAddItemBatchView getView() {
 		return (IAddItemBatchView) super.getView();
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
 		refreshProducts();
 		refreshItems();
 	}
 }
