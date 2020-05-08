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
 
 import model.BarcodePrinter;
 import model.Item;
 import model.NonEmptyString;
 import model.Product;
 import model.ProductContainer;
 import model.ProductManager;
 import model.ProductQuantity;
 import model.Unit;
 
 /**
  * Controller class for the add item batch view.
  */
 public class AddItemBatchController extends Controller implements IAddItemBatchController {
 	ArrayList<ArrayList<ItemData>> items;
 	ArrayList<ProductData> products;
 	ProductContainer container;
 
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
 		// Phase 2: not using the scanner
 		getView().setUseScanner(false);
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
 		ProductData selectedProduct = getView().getSelectedProduct();
 		ItemData selectedItem = getView().getSelectedItem();
 		String productBarcode = getView().getBarcode();
 		ProductManager productManager = getProductManager();
 		Product product = null;
 		ProductData productData = null;
 		if (productManager.containsProduct(productBarcode)) {
 			product = productManager.getByBarcode(productBarcode);
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
 		} else {
 			getView().displayAddProductView();
 			product = productManager.getByBarcode(productBarcode);
 			if (product == null)
 				return;
 			productData = addProduct(product);
 		}
 
 		Date entryDate = getView().getEntryDate();
 		int count = Integer.parseInt(productData.getCount());
 		int itemCount = Integer.parseInt(getView().getCount());
 		count += itemCount;
 		productData.setCount("" + count);
 
 		for (int i = 0; i < itemCount; i++) {
 			Item item = new Item(product, container, entryDate, getItemManager());
 
 			BarcodePrinter.getInstance().addItemToBatch(item);
 
 			ItemData id = DataWrapper.wrap(item);
 			items.get(products.indexOf(productData)).add(id);
 		}
 		// Clear the view for the next item!
 		getView().setBarcode("");
 		getView().setCount("1");
 		refreshItems();
 		refreshProducts();
 		enableComponents();
 		if (selectedProduct != null)
 			getView().selectProduct(selectedProduct);
 		if (selectedItem != null)
 			getView().selectItem(selectedItem);
 		getView().giveBarcodeFocus();
 	}
 
 	/**
 	 * This method is called when the "Product Barcode" field in the add item batch view is
 	 * changed by the user.
 	 */
 	@Override
 	public void barcodeChanged() {
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
 		// refreshProducts();
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
 		getView().enableUndo(false);
 		getView().enableRedo(false);
 		boolean isValidCount = true;
 		try {
 			int count = Integer.parseInt(getView().getCount());
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
