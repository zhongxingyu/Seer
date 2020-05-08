 package model.report;
 
 import java.util.Arrays;
 import java.util.Date;
 
 import model.Item;
 import model.Product;
 import model.ProductContainer;
 import model.ProductContainerManager;
 import model.StorageUnit;
 import model.report.builder.ReportBuilder;
 import model.visitor.InventoryVisitor;
 
 @SuppressWarnings("serial")
 public class ExpiredItemsReport extends Report implements InventoryVisitor {
 	private ReportBuilder builder;
 	private ProductContainerManager productContainerManager;
 
 	/**
 	 * Set up an empty ExpiredItemsReport.
 	 * 
 	 * @param productContainerManager
 	 *            Manager to use for finding Items and their expiration dates
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public ExpiredItemsReport(ProductContainerManager productContainerManager) {
 		this.productContainerManager = productContainerManager;
 	}
 
 	/**
 	 * Construct a completed ExpiredItemsReport, showing all Items in the system that have
 	 * passed their expiration date.
 	 * 
 	 * @param builder
 	 *            ReportBuilder to use
 	 * 
 	 * @pre true
 	 * @post (new Date()).getTime() - getLastRunTime().getTime() < 1000
 	 */
 	public void construct(ReportBuilder builder) {
 		updateLastRunTime();
 
 		// Store the builder for use by the Visitor pattern
 		this.builder = builder;
 
 		builder.addDocumentTitle("Expired Items");
 		builder.startTable(Arrays.asList("Description", "Storage Unit", "Product Group",
 				"Entry Date", "Expire Date", "Item Barcode"));
 		for (StorageUnit su : productContainerManager.getStorageUnits()) {
 			su.accept(this);
 		}
 
 		this.builder = null;
 	}
 
 	/**
 	 * Gathers reporting data about an Item.
 	 * 
 	 * @param item
 	 *            Item to visit
 	 * 
 	 * @pre builder != null
 	 * @pre item != null
 	 * @post true
 	 */
 	@Override
 	public void visit(Item item) {
 		if (builder == null) {
 			throw new NullPointerException("visit(Item) called outside a construct operation");
 		}
 		if (item == null) {
 			throw new NullPointerException("Null Item item");
 		}
 
		if (item.getExpirationDate().before(new Date())) {
 			builder.addTableRow(Arrays.asList(item.getProduct().getDescription(),
 					item.getStorageUnitName(), item.getProductGroupName(),
 					formatForReport(item.getEntryDate()),
 					formatForReport(item.getExpirationDate()), item.getBarcode()));
 		}
 	}
 
 	/**
 	 * Gathers reporting data about a Product.
 	 * 
 	 * @param product
 	 *            Product to visit
 	 * 
 	 * @pre builder != null
 	 * @pre product != null
 	 * @post true
 	 */
 	@Override
 	public void visit(Product product) {
 		if (builder == null) {
 			throw new NullPointerException("visit(Item) called outside a construct operation");
 		}
 		if (product == null) {
 			throw new NullPointerException("Null Product product");
 		}
 
 		// Do nothing
 	}
 
 	/**
 	 * Gathers reporting data about a ProductContainer.
 	 * 
 	 * @param productContainer
 	 *            ProductContainer to visit
 	 * 
 	 * @pre builder != null
 	 * @pre productContainer != null
 	 * @post true
 	 */
 	@Override
 	public void visit(ProductContainer productContainer) {
 		if (builder == null) {
 			throw new NullPointerException("visit(Item) called outside a construct operation");
 		}
 		if (productContainer == null) {
 			throw new NullPointerException("Null ProductContainer productContainer");
 		}
 
 		// Do nothing
 	}
 }
