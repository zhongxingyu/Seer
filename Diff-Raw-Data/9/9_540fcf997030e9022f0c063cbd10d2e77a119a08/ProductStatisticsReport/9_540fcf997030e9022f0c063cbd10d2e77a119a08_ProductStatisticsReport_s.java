 package model.report;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import model.Item;
 import model.ItemManager;
 import model.Product;
 import model.ProductManager;
 import model.report.builder.ReportBuilder;
 
 @SuppressWarnings("serial")
 public class ProductStatisticsReport extends Report {
 	private ItemManager itemManager;
 	private ProductManager productManager;
 	private final long millisPerDay = 86400000;
 
 	private List<String> headers = Arrays.asList("Description", "Barcode", "Size",
 			"3-Month Supply", "Supply: Cur/Avg", "Supply: Min/Max", "Supply Used/Added",
 			"Shelf Life", "Used Age: Avg/Max", "Cur Age: Avg/Max");
 
 	/**
 	 * Set up an empty ProductStatisticsReport.
 	 * 
 	 * @param itemManager
 	 *            Manager to use for gathering removed Item data
 	 * @param productManager
 	 *            Manager to use for gathering Product data
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public ProductStatisticsReport(ItemManager itemManager, ProductManager productManager) {
 		super();
 		this.itemManager = itemManager;
 		this.productManager = productManager;
 		reportName = "product-statistics";
 	}
 
 	/**
 	 * Construct a completed ProductStatisticsReport where the reporting period is the number
 	 * of months specified
 	 * 
 	 * @param builder
 	 *            ReportBuilder to use
 	 * @param months
 	 *            Reporting Period
 	 * 
 	 * @pre true
 	 * @post (new Date()).getTime() - getLastRunTime().getTime() < 1000
 	 */
 	@SuppressWarnings("deprecation")
 	public void construct(ReportBuilder builder, int months) {
 		updateLastRunTime();
 		Date startPeriod = new Date();
 		startPeriod.setMonth(startPeriod.getMonth() - months);
 
 		builder.addDocumentTitle("Product Report (" + months + " Months)");
 		builder.startTable(headers);
 
 		Map<String, Product> products = new TreeMap<String, Product>();
 
 		for (Product product : productManager.getProducts()) {
 			products.put(product.getDescription(), product);
 		}
 
 		DecimalFormat twoDForm = new DecimalFormat("#.##");
 
 		for (Product product : products.values()) {
 			if (startPeriod.before(product.getCreationDate())) {
 				startPeriod = product.getCreationDate();
 			}
 			List<String> row = new ArrayList<String>();
 
 			// description
 			row.add(product.getDescription());
 
 			// barcode
 			row.add(product.getBarcode());
 
 			// product size
 			row.add(product.getSize().toString());
 
 			// Three month supply
 			row.add(Integer.toString(product.getThreeMonthSupply()));
 
 			// Supply: Current/Average
 			row.add(getCurrentSupply(product) + "/"
 					+ twoDForm.format(getAverageSupply(product, startPeriod)));
 
 			// Supply: Max/Min
 			row.add(getMinSupply(product, startPeriod) + "/"
 					+ getMaxSupply(product, startPeriod));
 
 			// Supply: Used/Added
 			row.add(getUsedItemCount(product, startPeriod) + "/"
 					+ getAddedItemCount(product, startPeriod));
 
 			// Shelf life
 			row.add(product.getShelfLife() + " months");
 
 			// UsedAge: Average/Max
 			row.add(twoDForm.format(averageUsedAge(product, startPeriod)) + "/"
 					+ maxUsedAge(product, startPeriod));
 
 			// CurrentAge: Average/Max
 			row.add(twoDForm.format(averageCurrentAge(product)) + "/" + maxCurrentAge(product));
 			builder.addTableRow(row);
 		}
 		try {
 			File file = builder.print(getFileName());
 			java.awt.Desktop.getDesktop().open(file);
 		} catch (IOException e) {
 			System.out.println("Not able to open!! " + getFileName());
 		}
 	}
 
 	private double averageCurrentAge(Product product) {
 		Set<Item> items = itemManager.getItemsByProduct(product);
 		double totalDays = 0;
 		int totalItems = items.size();
 
 		if (totalItems == 0)
 			return 0;
 
 		Date today = new Date();
 		for (Item item : items) {
 			long diff = today.getTime() - item.getEntryDate().getTime();
 			totalDays += (diff / millisPerDay) + 1;
 		}
 		return totalDays / totalItems;
 	}
 
 	private double averageUsedAge(Product product, Date startPeriod) {
 		double daysStored = 0;
 		Set<Item> usedItems = itemManager.getRemovedItemsByProduct().get(product);
		if (usedItems != null) {
 			for (Item usedItem : usedItems) {
 				if (usedItem.getExitTime().after(startPeriod)) {
 					long timeStored = usedItem.getExitTime().getTime()
 							- usedItem.getEntryDate().getTime();
 					daysStored += (timeStored / millisPerDay) + 1;
 				}
 			}
 		} else {
 			return 0;
 		}
 
 		return (daysStored / getUsedItemCount(product, startPeriod));
 	}
 
 	private int getAddedItemCount(Product product, Date startDate) {
 		Set<Item> items = itemManager.getItemsByProduct(product);
 		int count = 0;
 
 		Set<Item> removed = itemManager.getRemovedItemsByProduct().get(product);
 		if (removed != null) {
 			for (Item item : removed) {
 				if (item.getEntryDate().after(startDate)
 						|| item.getEntryDate().getDate() == startDate.getDate())
 					count++;
 			}
 		}
 
 		for (Item item : items) {
 			if (item.getEntryDate().after(startDate)
 					|| item.getEntryDate().getDate() == startDate.getDate())
 				count++;
 		}
 		return count;
 	}
 
 	private double getAverageSupply(Product product, Date startDate) {
 		// yesterday's count
 		int current = getInitialCount(product, startDate);
 		double count = 0;
 
 		Date lastChange = startDate;
 		Map<Date, Integer> history = getHistory(product, startDate);
 		Collection<Date> dates = history.keySet();
 
 		// recreate the usage
 		for (Date d : dates) {
 			int change = history.get(d);
 			long days = getDaysDifference(lastChange, d);
 			count += (days * current);
 			current += change;
 			lastChange = d;
 		}
 
 		Date today = new Date();
 		if (lastChange.before(today)) {
 			long days = getDaysDifference(lastChange, today) + 1; // for end of day
 			count += (days * current);
 		}
 
 		return count / (getDaysDifference(startDate, today) + 1);
 	}
 
 	private int getCurrentSupply(Product product) {
 		return itemManager.getItemsByProduct(product).size();
 	}
 
 	private long getDaysDifference(Date first, Date second) {
 		if (first.equals(second))
 			return 1;
 		if (first.after(second))
 			throw new IllegalArgumentException("Second date must be after first date");
 		long timeDiff = second.getTime() - first.getTime();
 		return (timeDiff / millisPerDay);
 	}
 
 	private Map<Date, Integer> getHistory(Product product, Date startDate) {
 		Map<Date, Integer> dateItemMap = new TreeMap<Date, Integer>();
 		Set<Item> removed = itemManager.getRemovedItemsByProduct().get(product);
 		// build sorted list of history
 		if (removed != null) {
 			for (Item item : removed) {
 				if (item.getExitTime().after(startDate)) { // if item was removed this period
 					if (dateItemMap.containsKey(item.getExitTime())) {
 						int oldCount = dateItemMap.get(item.getExitTime());
 						dateItemMap.put(item.getExitTime(), --oldCount);
 					} else {
 						dateItemMap.put(item.getExitTime(), -1);
 					}
 				}
 				if (item.getEntryDate().after(startDate)) {
 					if (dateItemMap.containsKey(item.getEntryDate())) {
 						int oldCount = dateItemMap.get(item.getEntryDate());
 						dateItemMap.put(item.getEntryDate(), ++oldCount);
 					} else {
 						dateItemMap.put(item.getEntryDate(), 1);
 					}
 				}
 			}
 		}
 
 		for (Item item : itemManager.getItemsByProduct(product)) {
 			if (!item.getEntryDate().before(startDate)) {
 				if (dateItemMap.containsKey(item.getEntryDate())) {
 					int oldCount = dateItemMap.get(item.getEntryDate());
 					dateItemMap.put(item.getEntryDate(), ++oldCount);
 				} else {
 					dateItemMap.put(item.getEntryDate(), 1);
 				}
 			}
 		}
 
 		return dateItemMap;
 	}
 
 	/**
 	 * get yesterday's total
 	 * 
 	 * @param product
 	 * @param startPeriod
 	 * @return
 	 */
 	private int getInitialCount(Product product, Date startPeriod) {
 		Date today = new Date();
 		today.setDate(today.getDate() - 1);
 		Set<Item> items = itemManager.getItemsByProduct(product);
 		Set<Item> removed = itemManager.getRemovedItemsByProduct().get(product);
 		int currentCount = items.size();
 		int removedDuringPeriod = 0;
 		int addedDuringPeriod = 0;
 
 		if (removed != null) {
 			for (Item item : removed) {
 				if (item.getExitTime().after(startPeriod))
 					removedDuringPeriod++;
 				if (item.getEntryDate().after(startPeriod))
 					addedDuringPeriod++;
 			}
 		}
 
 		if (items != null) {
 			for (Item item : items) {
 				if (item.getEntryDate().after(startPeriod))
 					addedDuringPeriod++;
 			}
 		}
 		return currentCount - addedDuringPeriod + removedDuringPeriod;
 	}
 
 	private int getMaxSupply(Product product, Date startDate) {
 		int current = getInitialCount(product, startDate);
 		int max = current;
 		Map<Date, Integer> history = getHistory(product, startDate);
 		Collection<Date> dates = history.keySet();
 
 		// recreate the usage
 		for (Date d : dates) {
 			current += history.get(d);
 			max = Math.max(current, max);
 		}
 
 		return max;
 	}
 
 	private int getMinSupply(Product product, Date startDate) {
 		int current = getInitialCount(product, startDate);
 		int min = current;
 		Map<Date, Integer> history = getHistory(product, startDate);
 		Collection<Date> dates = history.keySet();
 
 		// recreate the usage
 		for (Date d : dates) {
 			current += history.get(d);
 			min = Math.min(current, min);
 		}
 
 		return min;
 	}
 
 	private int getUsedItemCount(Product product, Date startDate) {
 		Set<Item> usedItems = itemManager.getRemovedItemsByProduct().get(product);
 		int count = 0;
 		if (usedItems != null) {
 			for (Item item : usedItems) {
 				if (item.getExitTime().after(startDate))
 					count++;
 			}
 		}
 		return count;
 	}
 
 	private int maxCurrentAge(Product product) {
 		Set<Item> items = itemManager.getItemsByProduct(product);
 		int maxDays = 0;
 		Date now = new Date();
 		for (Item item : items) {
 			int daysInStorage = (int) ((now.getTime() - item.getEntryDate().getTime()) / millisPerDay) + 1;
 			if (daysInStorage > maxDays)
 				maxDays = daysInStorage;
 		}
 		return maxDays;
 	}
 
 	private long maxUsedAge(Product product, Date startPeriod) {
 		long maxDaysStored = 0;
 		Set<Item> usedItems = itemManager.getRemovedItemsByProduct().get(product);
 		if (usedItems != null) {
 			for (Item usedItem : usedItems) {
 				if (usedItem.getExitTime().after(startPeriod)) {
 					long timeStored = usedItem.getExitTime().getTime()
 							- usedItem.getEntryDate().getTime();
 					long daysStored = (timeStored / millisPerDay) + 1;
 					maxDaysStored = Math.max(maxDaysStored, daysStored);
 				}
 			}
 		} else {
 			return 0;
 		}
 
 		return maxDaysStored;
 	}
 
 }
