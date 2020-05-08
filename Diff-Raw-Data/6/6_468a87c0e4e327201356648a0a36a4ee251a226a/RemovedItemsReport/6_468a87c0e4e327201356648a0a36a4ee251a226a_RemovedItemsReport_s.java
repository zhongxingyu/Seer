 package model.report;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Map;
 import java.util.Set;
 
 import model.Item;
 import model.ItemManager;
 import model.Product;
 import model.report.builder.ReportBuilder;
 
 @SuppressWarnings("serial")
 public class RemovedItemsReport extends Report {
 	private final ItemManager itemManager;
 
 	/**
 	 * Set up an empty RemovedItemsReport.
 	 * 
 	 * @param itemManager
 	 *            Manager to use for gathering removed Item data
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public RemovedItemsReport(ItemManager itemManager) {
 		this.itemManager = itemManager;
 	}
 
 	/**
 	 * Construct a completed RemovedItemsReport, starting from the last time the report was
 	 * run.
 	 * 
 	 * @param builder
 	 *            ReportBuilder to use
 	 * 
 	 * @pre true
 	 * @post (new Date()).getTime() - getLastRunTime().getTime() < 1000
 	 */
 	public void construct(ReportBuilder builder) {
 		construct(builder, getLastRunTime());
 	}
 
 	/**
 	 * Construct a completed RemovedItemsReport, starting from the given startDate.
 	 * 
 	 * @param builder
 	 *            ReportBuilder to use
 	 * @param startDate
 	 *            Start date for reporting period
 	 * 
 	 * @pre true
 	 * @post (new Date()).getTime() - getLastRunTime().getTime() < 1000
 	 */
 	public void construct(ReportBuilder builder, Date startDate) {
 		updateLastRunTime();
 
 		builder.addDocumentTitle("Removed Items");
 		builder.startTable(Arrays.asList("Description", "Size", "Product Barcode", "Removed",
 				"Current Supply"));
 
 		Map<Product, Set<Item>> removedItemsByProduct = itemManager.getRemovedItemsByProduct();
 		for (Product product : removedItemsByProduct.keySet()) {
 			int removedCount = 0;
 			Set<Item> removedItems = removedItemsByProduct.get(product);
 			for (Item item : removedItems) {
 				if (item.getExitTime().after(startDate)) {
 					removedCount++;
 				}
 			}
 			if (removedCount > 0) {
 				builder.addTableRow(Arrays.asList(product.getDescription(), ""
 						+ product.getSize().toString(), product.getBarcode(), ""
						+ removedCount, "" + product.getItems().size()));
 			}
 		}
 
 		try {
 			File file = builder.print("removedItems");
 			java.awt.Desktop.getDesktop().open(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}

 }
