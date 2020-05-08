 package com.bc.scraper;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.xssf.usermodel.XSSFCell;
 import org.apache.poi.xssf.usermodel.XSSFRow;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.bc.scraper.vikingdirect.VikingDirectProductPage;
 
 public class VikingScraper {
 
 	public static void main(String[] args) throws Exception {
 
 		String baseDir = "";
 		String seedFileName = baseDir + "vikings_seed.xlsx";
 		Map<String, String> seedProducts = loadSeedProducts(seedFileName);
 		Document doc;
 		Map<String, VikingDirectProductPage> resultMap = new HashMap<String, VikingDirectProductPage>();
 		int count = 0;
 		int totalUrls = seedProducts.size();
 		for (String sku : seedProducts.keySet()) {
 			count++;
 			System.out.println("Status:(" + count + "/" + totalUrls + ")");
 
 			String url = seedProducts.get(sku);
 			VikingDirectProductPage vikingDirectProductPage = new VikingDirectProductPage();
 			resultMap.put(sku, vikingDirectProductPage);
 			try {
 				doc = Jsoup.connect(url).get();
 				try {
 					Elements titleDiv = doc.select("div#skuHeading > h1.fn");
 					String title = titleDiv.text();
 					vikingDirectProductPage.setName(title);
 				} catch (Exception e) {
 				}
 				try {
 					Elements skuDiv = doc.select("div#skuHeading > div.item_sku");
 					String[] vikingSkuTemp = skuDiv.text().split(":");
 					String vikingSku = vikingSkuTemp[1].trim();
 					vikingDirectProductPage.setSku(vikingSku);
 				} catch (Exception e) {
 				}
 				try {
 					Element p1 = doc.getElementById("priceNoTax0");
 					String listPrice = p1.text();
 					vikingDirectProductPage.setListPrice(listPrice);
 				} catch (Exception e) {
 				}
 				String lastBmsmListPrice = null;
 				try {
 					Element p3 = doc.getElementById("priceNoTax2");
 					lastBmsmListPrice = p3.text();
 					vikingDirectProductPage.setLastBmsmListPrice(lastBmsmListPrice);
 				} catch (Exception e) {
 				}
 				if (lastBmsmListPrice == null) {
 					try {
 						Element p2 = doc.getElementById("priceNoTax1");
 						lastBmsmListPrice = p2.text();
 						vikingDirectProductPage.setLastBmsmListPrice(lastBmsmListPrice);
 					} catch (Exception e) {
 					}
 				}
 				try {
 					Element q1 = doc.getElementById("priceQty0");
 					String[] qty1Array = q1.text().split("-");
 					String qty1 = qty1Array[0].trim();
 					vikingDirectProductPage.setQty1(qty1);
 				} catch (Exception e) {
 				}
 				String bmsmQtyLast = null;
 				try {
 					Element q3 = doc.getElementById("priceQty2");
 					bmsmQtyLast = extractMaxQty(q3.text());
 					vikingDirectProductPage.setBmsmQtyLast(bmsmQtyLast);
 				} catch (Exception e) {
 				}
 				if (bmsmQtyLast == null) {
 					try {
 						Element q2 = doc.getElementById("priceQty1");
 						bmsmQtyLast = extractMaxQty(q2.text());
 						vikingDirectProductPage.setBmsmQtyLast(bmsmQtyLast);
 					} catch (Exception e) {
 					}
 				}
 				try {
 					Elements specialOfferDiv = doc.getElementsByClass("dealBoxHeadingContent");
 					String heading = specialOfferDiv.text();
 					boolean promo = heading.equalsIgnoreCase("Offre Spciale");
 					if(promo){
 						vikingDirectProductPage.setPromoListPrice(vikingDirectProductPage.getListPrice());
 					}
 
 				} catch (Exception e) {
 				}
 				vikingDirectProductPage.setUrl(url);
 				// System.out.println(vikingDirectProductPage);
 			} catch (Exception e) {
 				// e.printStackTrace();
 			}
 		}
 		DateFormat dateFormat = new SimpleDateFormat("MM_dd_HH_mm_ss");
 		Date date = new Date();
 		String outputFileName = "viking_cm_data_" + dateFormat.format(date) + ".xlsx";
 		dumpVikingCMData(resultMap, outputFileName);
 	}
 
 	private static String extractMaxQty(String bmsmQtyLast) {
 		try {
 			String maxQty = null;
 			String[] temp = bmsmQtyLast.split("\\+");
 			maxQty = temp[0].trim();
 			if (isNumeric(maxQty))
 				return maxQty;
 			else {
 				temp = bmsmQtyLast.split("-");
 				maxQty = temp[1].trim();
 				return maxQty;
 			}
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 		return null;
 
 	}
 
 	private static Map<String, String> loadSeedProducts(String seedFileName) throws Exception {
 		int totalRows = 0, badUPCCount = 0, badProductIdCount = 0;
 		Map<String, String> products = new LinkedHashMap<String, String>();
 
 		XSSFWorkbook wb = readFile(seedFileName);
 
 		for (int k = 0; k < 1; k++) {
 
 			XSSFSheet sheet = wb.getSheetAt(k);
 			int rows = sheet.getPhysicalNumberOfRows();
 			totalRows = rows - 1;
 
 			for (int r = 1; r < rows; r++) {
 				try {
 					XSSFRow row = sheet.getRow(r);
 					if (row == null) {
 						System.out.println("Ignoring Empty Row: " + r);
 						continue;
 					}
 
 					XSSFCell cell0 = row.getCell(0);// SKU
 					XSSFCell cell1 = row.getCell(1);// Viking URL
 					if (cell0 != null) {
 						String sku = cell0.getStringCellValue();
 						if (cell1 != null) {
 							String url = cell1.getStringCellValue();
 							products.put(sku, url);
 						}
 					}
 				} catch (Exception ex) {
 					ex.printStackTrace();
 					System.out.println("row = " + r + " has invalid data.");
 					badUPCCount++;
 					throw ex;
 				}
 			}
 		}
 
 		System.out.println("Valid Seed Products = " + products.size());
 		System.out.println("Bad UPCs =            " + badUPCCount);
 		System.out.println("No RSProduct Ids =      " + badProductIdCount);
 		System.out.println("                     -----");
 		System.out.println("Total Seed Products   = " + totalRows);
 
 		return products;
 	}
 
 	private static void dumpVikingCMData(Map<String, VikingDirectProductPage> cmData, String outputFileName) throws IOException {
 
 		Workbook wb = new XSSFWorkbook();
 		FileOutputStream fileOut = new FileOutputStream(outputFileName);
 
 		Sheet mainSheet = wb.createSheet("Viking CM Data");
 
 		int rowCount = 0;
 		Row row = mainSheet.createRow(rowCount++);
 
 		row.createCell(0).setCellValue("Staples SKU");
 		row.createCell(1).setCellValue("Viking Title");
 		row.createCell(2).setCellValue("URL");
 		row.createCell(3).setCellValue("Viking SKU");
 		row.createCell(4).setCellValue("BMSM Qty 1st");
 		row.createCell(5).setCellValue("Online List Price Each (ex VAT)");
 		row.createCell(6).setCellValue("BMSM Qty last");
 		row.createCell(7).setCellValue("Online BMSM last tier Price Each (ex VAT)");
 		row.createCell(8).setCellValue("Promo Price");
 
 		for (String sku : cmData.keySet()) {
 			VikingDirectProductPage vikingDirectProductPage = cmData.get(sku);
 			row = mainSheet.createRow(rowCount++);
 			row.createCell(0).setCellValue(sku);
 			row.createCell(1).setCellValue(vikingDirectProductPage.getName());
 			row.createCell(2).setCellValue(vikingDirectProductPage.getUrl());
 			row.createCell(3).setCellValue(vikingDirectProductPage.getSku());
 			row.createCell(4).setCellValue(vikingDirectProductPage.getQty1());
 			row.createCell(5).setCellValue(vikingDirectProductPage.getListPrice());
 			row.createCell(6).setCellValue(vikingDirectProductPage.getBmsmQtyLast());
 			row.createCell(7).setCellValue(vikingDirectProductPage.getLastBmsmListPrice());
			row.createCell(8).setCellValue(vikingDirectProductPage.getPromoListPrice());
 		}
 
 		autoResizeColumns(mainSheet);
 
 		wb.write(fileOut);
 		fileOut.close();
 	}
 
 	private static XSSFWorkbook readFile(String filename) throws IOException {
 		return new XSSFWorkbook(new FileInputStream(filename));
 	}
 
 	private static void autoResizeColumns(Sheet mainSheet) {
		for (int i = 0; i < 9; i++) {
 			mainSheet.autoSizeColumn(i);
 		}
 	}
 
 	private static boolean isNumeric(String str) {
 		try {
 			Double.parseDouble(str);
 		} catch (NumberFormatException nfe) {
 			return false;
 		}
 		return true;
 	}
 }
