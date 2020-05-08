 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.formula.FormulaParseException;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.DataFormat;
 import org.apache.poi.ss.usermodel.IndexedColors;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.util.CellRangeAddress;
 
 
 public class TemplateBuilder {
 	//fields
 	static Calendar todayNow = Calendar.getInstance();
 	static Workbook wb = new HSSFWorkbook();
 	static Sheet sheetForByAcc = wb.createSheet("Forecast By Account");
 	static Sheet sheetMix = wb.createSheet("Mix");
 	static Sheet sheet1 = wb.createSheet("Judged Forecast");
 	static FileOutputStream fileOut;
 	static int rCountFBA = 0;
 	static int previousLastRow = 0;
 	static int oldPreviousLastRow = 0;
 	static int rCountMix = 0;
 	static int previousLastRowMix = 1;
 	static int rCountFore = 0;
 	static int previousLastRowFore = 0;
 	
 	/**
 	 * Forecast by account BEFORE CSAM judgment, should be the first tab in the file
 	 * @param weeklyMapsSF
 	 * @param isFirst
 	 * @param isLast
 	 */
 	public static void FrcstByAccntBySubF(String name, Map<Integer, TreeMap<String, Integer>> weeklyMapsSF, boolean isFirst, boolean isLast) {
 		Row row;
 		
 		//create header only if isFirst
 		if (isFirst) {
 			
 			row = sheetForByAcc.createRow(rCountFBA);
 			rCountFBA++;
 			row.createCell(0).setCellValue("Customer Name");
 			row.createCell(1).setCellValue("Apple Part Nr");
 			row.createCell(2).setCellValue("Week 1 Forecast");
 			row.createCell(3).setCellValue("Week 2 Forecast");
 			row.createCell(4).setCellValue("Week 3 Forecast");
 			row.createCell(5).setCellValue("Week 4 Forecast");
 			row.createCell(6).setCellValue("Week 5 Forecast");
 			row.createCell(7).setCellValue("Week 6 Forecast");
 			row.createCell(8).setCellValue("Week 7 Forecast");
 			row.createCell(9).setCellValue("Week 8 Forecast");
 			row.createCell(10).setCellValue("Week 9 Forecast");
 			row.createCell(11).setCellValue("Week 10 Forecast");
 			row.createCell(12).setCellValue("Week 11 Forecast");
 			row.createCell(13).setCellValue("Week 12 Forecast");
 			row.createCell(14).setCellValue("Week 13 Forecast");
 			row.createCell(15).setCellValue("Week 14 Forecast");
 			for( Cell c : row) {
 				sheetForByAcc.autoSizeColumn(c.getColumnIndex());
 			}
 		}	
 		//create SubFamily column
 		for(String k : weeklyMapsSF.get(1).keySet()) { //could pick any of the weeks, using wk 1 here
 			row = sheetForByAcc.createRow(rCountFBA);
 			row.createCell(0).setCellValue(name);
 			row.createCell(1).setCellValue(k);
 			rCountFBA++;
 		}
 		//add mix data
 		for (int wk = 1; wk < weeklyMapsSF.size() + 1; wk++) {
 			try{
 				for(Row r : sheetForByAcc) {
 					if (r.getRowNum() > previousLastRow) {
 						r.createCell(1 + wk).setCellValue(weeklyMapsSF.get(wk).get(r.getCell(1).getStringCellValue()));
 					}
 					
 				}
 			} catch (NullPointerException e) {
 				//e.printStackTrace();
 			}
 			
 		}
 		
 		sheetForByAcc.autoSizeColumn(1); //re-autosize this column after adding content
 		
 		//update the previousLastRow, subtract 1 since we had added 1 and didn't "use" the row yet
 		oldPreviousLastRow = previousLastRow;
 		previousLastRow = rCountFBA - 1;
 			
 	}
 	
 	/**
 	 * Mix sheet, should be the second tab in the file. Should include column for CSA judgment
 	 * @param mixTemp
 	 */
 	public static void createMixTemplate(String name, Map<Integer, TreeMap<String, Double>> weeklyMapsSku, boolean isFirst, boolean isLast) {
 		Row row;
 		DataFormat df;
 		CellStyle percentageStyle, percentageStyle2;
 		
 		//create format styles
 		df = wb.createDataFormat();
 		percentageStyle = wb.createCellStyle();
 		percentageStyle.setDataFormat(df.getFormat("0.00%"));
 		
 		percentageStyle2 = wb.createCellStyle();
 		percentageStyle2.setDataFormat(df.getFormat("0.00%"));
 		percentageStyle2.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
 		percentageStyle2.setFillPattern(CellStyle.SOLID_FOREGROUND);
 		percentageStyle2.setBorderRight(CellStyle.BORDER_THICK);
 		
 		//create header, merge header cells, add row bellow header (subheader)
 		if(isFirst) {
 			row = sheetMix.createRow(rCountMix);
 			rCountMix++;
 			row.createCell(0).setCellValue("Customer Name");
 			row.createCell(1).setCellValue("Apple Part Nr");
 			row.createCell(2).setCellValue("Week 1");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 2, 4));
 			row.createCell(5).setCellValue("Week 2");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 5, 7));
 			row.createCell(8).setCellValue("Week 3");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 8, 10));
 			row.createCell(11).setCellValue("Week 4");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 11, 13));
 			row.createCell(14).setCellValue("Week 5");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 14, 16));
 			row.createCell(17).setCellValue("Week 6");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 17, 19));
 			row.createCell(20).setCellValue("Week 7");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 20, 22));
 			row.createCell(23).setCellValue("Week 8");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 23, 25));
 			row.createCell(26).setCellValue("Week 9");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 26, 28));
 			row.createCell(29).setCellValue("Week 10");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 29, 31));
 			row.createCell(32).setCellValue("Week 11");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 32, 34));
 			row.createCell(35).setCellValue("Week 12");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 35, 37));
 			row.createCell(38).setCellValue("Week 13");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 38, 40));
 			row.createCell(41).setCellValue("Week 14");
 			sheetMix.addMergedRegion(new CellRangeAddress(rCountMix-1, rCountMix-1, 41, 43));
 			for( Cell c : row) {
 				sheetMix.autoSizeColumn(c.getColumnIndex());
 			}
 			row = sheetMix.createRow(rCountMix);
 			rCountMix++;
 			for(int wk = 1 ; wk < 15 ; wk++) {
 				row.createCell(2 + (wk - 1)*3).setCellValue("Actual");
				if( wk < 5) {
					row.createCell(3 + (wk - 1)*3).setCellValue("AVG " + wk + " wks");
				} else {
					row.createCell(3 + (wk - 1)*3).setCellValue("AVG 5 wks");
				}
 				row.createCell(4 + (wk - 1)*3).setCellValue("Judged");
 			}
 		}
 		//create SKU column
 		for(String k : weeklyMapsSku.get(1).keySet()) { //could pick any of the weeks, using wk 1 here
 			row = sheetMix.createRow(rCountMix);
 			row.createCell(0).setCellValue(name);
 			row.createCell(1).setCellValue(k);
 			rCountMix++;
 		}
 		//add mix data
 		for (int wk = 1; wk < weeklyMapsSku.size() + 1; wk++) {
 			try{
 				for(Row r : sheetMix) {
 					if (r.getRowNum() > previousLastRowMix) {
 						Cell c = r.createCell(2 + (wk-1)*3);
 						c.setCellValue(weeklyMapsSku.get(wk).get(r.getCell(1).getStringCellValue()));
 						c.setCellStyle(percentageStyle);
 						
 						//calculate averages
 						c =r.createCell(3 + (wk - 1)*3);
 						try {
 							if(wk < 6) {
 								double soma = 0;
 								for (int n = 1; n < wk + 1; n++) {
 									soma += r.getCell(2 + (n - 1)*3).getNumericCellValue();
 								}
 								c.setCellValue(soma/wk);
 							} else {
 								double soma = 0;
 								for (int n = wk - 4; n < wk + 1; n++) {
 									soma += r.getCell(2 + (n - 1)*3).getNumericCellValue();
 								}
 								c.setCellValue(soma/5);
 							}
 						} catch (IllegalStateException e) {
 							c.setCellValue(0);
 						}
 						c.setCellStyle(percentageStyle);
 						
 						//Judged values = averages
 						c =r.createCell(4 + (wk - 1)*3);
 						try {
 							if(wk < 6) {
 								double soma = 0;
 								for (int n = 1; n < wk + 1; n++) {
 									soma += r.getCell(2 + (n - 1)*3).getNumericCellValue();
 								}
 								c.setCellValue(soma/wk);
 							} else {
 								double soma = 0;
 								for (int n = wk - 4; n < wk + 1; n++) {
 									soma += r.getCell(2 + (n - 1)*3).getNumericCellValue();
 								}
 								c.setCellValue(soma/5);
 							}
 						} catch (IllegalStateException e) {
 							c.setCellValue(0);
 						}
 						c.setCellStyle(percentageStyle2);
 						
 						
 					}
 				}
 			} catch (NullPointerException e) {
 				//e.printStackTrace();
 			} 
 		}
 		//freeze panes
 		sheetMix.autoSizeColumn(1); //re-autosize this column after adding content
 		sheetMix.createFreezePane(2, 2);
 				
 		//update the previousLastRowMix, subtract 1 since we had added 1 and didn't "use" the row yet
 		previousLastRowMix = rCountMix - 1;
 		
 	}
 	
 	/**
 	 * Forecast by account after CSAM judgment, should be the last (3rd) tab in the file
 	 */
 	public static void createTemplate(String name, Map<Integer, TreeMap<String, Double>> weeklyMapsSku, boolean isFirst, boolean isLast) {
 		Row row;
 		int firstRow, lastRow;
 		
 		//set firstRow, note that we refer to the Forecast By Account sheet (previousLastRow)
 		if(isFirst) {
 			firstRow = 2;
 		} else {
 			firstRow = oldPreviousLastRow + 2; //we don't want 0 based here
 		}
 		
 		lastRow = previousLastRow + 1; //we don't want 0 based here
 		Cell c;
 		
 		DataFormat df;
 		CellStyle integ;
 		//create format styles
 		df = wb.createDataFormat();
 		integ = wb.createCellStyle();
 		integ.setDataFormat(df.getFormat("0"));
 		
 		CellStyle style1 = wb.createCellStyle();
 		CellStyle style2 = wb.createCellStyle();
 		CellStyle style3 = wb.createCellStyle();
 		CellStyle style4 = wb.createCellStyle();
 		
 		//set styles, but not using yet
 		style1.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
 	    style1.setFillPattern(CellStyle.SOLID_FOREGROUND);
 	    style2.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
 	    style2.setFillPattern(CellStyle.SOLID_FOREGROUND);
 	    style3.setFillForegroundColor(IndexedColors.CORAL.getIndex());
 	    style3.setFillPattern(CellStyle.SOLID_FOREGROUND);
 	    style4.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
 	    style4.setFillPattern(CellStyle.SOLID_FOREGROUND);
 		
 		//create header
 	    if(isFirst) {
 	    	row = sheet1.createRow(rCountFore);
 	    	rCountFore++;
 			row.createCell(0).setCellValue("Customer Name");
 			row.createCell(1).setCellValue("Apple Part Nr");
 			row.createCell(2).setCellValue("Date Updated");
 			row.createCell(3).setCellValue("Region Code");
 			row.createCell(4).setCellValue("Instock Percentage");
 			row.createCell(5).setCellValue("Store Count");
 			row.createCell(6).setCellValue("DC On Hand");
 			row.createCell(7).setCellValue("Target WOS");
 			row.createCell(8).setCellValue("Current Week Trend");
 			row.createCell(9).setCellValue("Forecast Quarter");
 			row.createCell(10).setCellValue("Week 1 Forecast");
 			row.createCell(11).setCellValue("Week 2 Forecast");
 			row.createCell(12).setCellValue("Week 3 Forecast");
 			row.createCell(13).setCellValue("Week 4 Forecast");
 			row.createCell(14).setCellValue("Week 5 Forecast");
 			row.createCell(15).setCellValue("Week 6 Forecast");
 			row.createCell(16).setCellValue("Week 7 Forecast");
 			row.createCell(17).setCellValue("Week 8 Forecast");
 			row.createCell(18).setCellValue("Week 9 Forecast");
 			row.createCell(19).setCellValue("Week 10 Forecast");
 			row.createCell(20).setCellValue("Week 11 Forecast");
 			row.createCell(21).setCellValue("Week 12 Forecast");
 			row.createCell(22).setCellValue("Week 13 Forecast");
 			row.createCell(23).setCellValue("Week 14 Forecast");
 			row.createCell(24).setCellValue("Week 1 JST");
 			row.createCell(25).setCellValue("Week 2 JST");
 			row.createCell(26).setCellValue("Week 3 JST");
 			row.createCell(27).setCellValue("Week 4 JST");
 			row.createCell(28).setCellValue("Week 5 JST");
 			row.createCell(29).setCellValue("Week 6 JST");
 			row.createCell(30).setCellValue("Week 7 JST");
 			row.createCell(31).setCellValue("Week 8 JST");
 			row.createCell(32).setCellValue("Week 9 JST");
 			row.createCell(33).setCellValue("Week 10 JST");
 			row.createCell(34).setCellValue("Week 11 JST");
 			row.createCell(35).setCellValue("Week 12 JST");
 			row.createCell(35).setCellValue("Week 13 JST");
 			row.createCell(36).setCellValue("Week 14 JST");
 			row.createCell(37).setCellValue("Week 1 Req");
 			row.createCell(38).setCellValue("Week 2 Req");
 			row.createCell(39).setCellValue("Week 3 Req");
 			row.createCell(40).setCellValue("Week 4 Req");
 			row.createCell(41).setCellValue("Week 5 Req");
 			row.createCell(42).setCellValue("Week 6 Req");
 			row.createCell(43).setCellValue("Week 7 Req");
 			row.createCell(44).setCellValue("Week 8 Req");
 			row.createCell(45).setCellValue("Week 9 Req");
 			row.createCell(46).setCellValue("Week 10 Req");
 			row.createCell(47).setCellValue("Week 11 Req");
 			row.createCell(48).setCellValue("Week 12 Req");
 			row.createCell(49).setCellValue("Week 13 Req");
 			row.createCell(50).setCellValue("Week 14 Req");
 			for( Cell c1 : row) {
 				sheet1.autoSizeColumn(c1.getColumnIndex());
 			}
 	    }
 		
 		//create SKU column
 				for(String k : weeklyMapsSku.get(1).keySet()) { //could pick any of the weeks, using wk 1 here
 					row = sheet1.createRow(rCountFore);
 					row.createCell(0).setCellValue(name);
 					row.createCell(1).setCellValue(k);
 					rCountFore++;
 				}
 		
 		//add mix*subFforecast
 		for (int wk = 1; wk < weeklyMapsSku.size() + 1; wk++) {
 			try{
 				for(Row r : sheet1) {
 					if (r.getRowNum() > previousLastRowFore) {
 						String subF = GTAFunctions.skuSubF.get(r.getCell(1).getStringCellValue());
 						
 						switch ( wk) {
 						case 1: String formula1 = "Mix!E" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 							c = r.createCell(24);
 							c.setCellFormula(formula1);
 							c.setCellStyle(integ);
 							break;
 						
 						case 2: String formula2 = "Mix!H" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(25);
 						c.setCellFormula(formula2);
 						c.setCellStyle(integ);
 						break;
 						
 						case 3: String formula3 = "Mix!K" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(26);
 						c.setCellFormula(formula3);
 						c.setCellStyle(integ);
 						break;
 						
 						case 4: String formula4 = "Mix!N" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(27);
 						c.setCellFormula(formula4);
 						c.setCellStyle(integ);
 						break;
 						
 						case 5: String formula5 = "Mix!Q" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(28);
 						c.setCellFormula(formula5);
 						c.setCellStyle(integ);
 						break;
 						
 						case 6: String formula6 = "Mix!T" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(29);
 						c.setCellFormula(formula6);
 						c.setCellStyle(integ);
 						break;
 						
 						case 7: String formula7 = "Mix!W" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(30);
 						c.setCellFormula(formula7);
 						c.setCellStyle(integ);
 						break;
 						
 						case 8: String formula8 = "Mix!Z" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(31);
 						c.setCellFormula(formula8);
 						c.setCellStyle(integ);
 						break;
 						
 						case 9: String formula9 = "Mix!AC" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(32);
 						c.setCellFormula(formula9);
 						c.setCellStyle(integ);
 						break;
 						
 						case 10: String formula10 = "Mix!AF" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(33);
 						c.setCellFormula(formula10);
 						c.setCellStyle(integ);
 						break;
 						
 						case 11: String formula11 = "Mix!AI" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(34);
 						c.setCellFormula(formula11);
 						c.setCellStyle(integ);
 						break;
 						
 						case 12: String formula12 = "Mix!AL" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(35);
 						c.setCellFormula(formula12);
 						c.setCellStyle(integ);
 						break;
 						
 						case 13: String formula13 = "Mix!AO" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(36);
 						c.setCellFormula(formula13);
 						c.setCellStyle(integ);
 						break;
 						
 						case 14: String formula14 = "Mix!AR" + (r.getRowNum()+2) + "*VLOOKUP(\"" + subF + "\", 'Forecast By account'!B" + firstRow + ":P" + lastRow + ", " + (wk + 1) + ", 0)";
 						c = r.createCell(37);
 						c.setCellFormula(formula14);
 						c.setCellStyle(integ);
 						break;
 					}
 					}
 				}
 			} catch (NullPointerException e) {
 				
 			} catch (FormulaParseException e) {
 				
 			}
 		}
 		//freeze panes and re-autosize this column after adding content
 		sheet1.autoSizeColumn(1);
 		sheet1.createFreezePane(2, 1);
 		
 		//update the previousLastRowMix, subtract 1 since we had added 1 and didn't "use" the row yet
 		previousLastRowFore = rCountFore - 1;
 		
 		//create and save the xls file
 		if(isLast) {
 			wb.setActiveSheet(1);
 			try {
 				fileOut = new FileOutputStream(GTAGUI.inputPath.getParent() + "/ForecastMix_" + String.valueOf((todayNow.get(Calendar.MONTH)+1)) + String.valueOf(todayNow.get(Calendar.DAY_OF_MONTH)) + 
 						String.valueOf(todayNow.get(Calendar.HOUR_OF_DAY)) + String.valueOf(todayNow.get(Calendar.MINUTE)) + ".xls");
 				wb.write(fileOut);
 				fileOut.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 				GTAGUI.generalMessage("Error saving Template file: " + e.getMessage());
 			} catch (IOException e) {
 				e.printStackTrace();
 				GTAGUI.generalMessage("Error saving Template file" + e.getMessage());
 			}
 		}
 	}
 	
 }
