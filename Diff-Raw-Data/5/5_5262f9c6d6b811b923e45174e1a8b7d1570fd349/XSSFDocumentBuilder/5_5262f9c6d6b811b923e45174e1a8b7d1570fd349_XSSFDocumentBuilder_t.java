 package org.bh.plugin.excelexport;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.CreationHelper;
 import org.apache.poi.ss.usermodel.Font;
 import org.apache.poi.ss.usermodel.RichTextString;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.IPeriodicalValuesDTO;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DistributionMap;
 import org.bh.data.types.IValue;
 import org.bh.platform.i18n.BHTranslator;
 import org.bh.platform.i18n.ITranslator;
 
 public class XSSFDocumentBuilder {
 
 	/**
 	 * Logger for this class
 	 */
 	private static final Logger log = Logger
 			.getLogger(XSSFDocumentBuilder.class);
 
 	private static ITranslator trans = BHTranslator.getInstance();
 
 	public enum Keys {
 		TITLE, SCENARIO_SHEET, CREATEDAT, DATEFORMAT, PERIOD_SHEET, RESULT_SHEET, ;
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 	}
 
 	Workbook wb;
 	CreationHelper crh;
 
 	Font titleFont;
 	Font sect1Font;
 	Font sect2Font;
 	CellStyle std;
 
 	public void newDocument() {
 		wb = new XSSFWorkbook();
 		crh = wb.getCreationHelper();
 
 		titleFont = wb.createFont();
 		titleFont.setFontName("Arial");
 		titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
 		titleFont.setFontHeightInPoints((short) 20);
 
 		sect1Font = wb.createFont();
 		sect1Font.setFontName("Arial");
 		sect1Font.setFontHeightInPoints((short) 16);
 
 		sect2Font = wb.createFont();
 		sect2Font.setFontName("Arial");
 		sect2Font.setFontHeightInPoints((short) 14);
 
 		// default cell style
 		std = wb.createCellStyle();
 		std.setWrapText(true);
 	}
 
 	void buildScenarioSheet(DTOScenario scenario) {
 		Sheet sheet;
 		Row row;
 		Row row2;
 		Row tRow;
 		Cell cell;
 		CellStyle date;
 		RichTextString str;
 
 		// cell style for date
 		date = wb.createCellStyle();
 		date.setDataFormat(crh.createDataFormat().getFormat(
 				trans.translate(Keys.DATEFORMAT)));
 		date.setWrapText(true);
 
 		// scenario data sheet
 		sheet = wb.createSheet(trans.translate(Keys.SCENARIO_SHEET));
 		sheet.setColumnWidth(0, 8000);
 		sheet.setColumnWidth(1, 3500);
 
 		// sheet content
 		row = sheet.createRow(0);
 
 		str = crh.createRichTextString(trans.translate(Keys.TITLE) + " - "
 				+ scenario.get(DTOScenario.Key.NAME));
 		str.applyFont(titleFont);
 
 		row.createCell(0).setCellValue(str);
 
 		row2 = sheet.createRow(1);
 		str = crh.createRichTextString(trans.translate(Keys.CREATEDAT));
 		row2.createCell(0).setCellValue(str);
 
 		cell = row2.createCell(1);
 		cell.setCellValue(new Date());
 		cell.setCellStyle(date);
 
 		int j = 4;
 		for (Iterator<Entry<String, IValue>> i = scenario.iterator(); i
 				.hasNext(); j++) {
 			Map.Entry<String, IValue> val = i.next();
 			tRow = sheet.createRow(j);
 
 			str = crh.createRichTextString(trans.translate(val.getKey()));
 			cell = tRow.createCell(0);
 			cell.setCellStyle(std);
 			cell.setCellValue(str);
 
 			str = crh.createRichTextString(val.getValue().toString());
 			cell = tRow.createCell(1);
 			cell.setCellStyle(std);
 			cell.setCellValue(val.getValue().toString());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	void buildPeriodSheet(DTOScenario scenario) {
 		Sheet sheet;
 		Cell cell;
 		Row row;
 		RichTextString str;
 		int rowCnt = 0;
 
 		// period data sheet
 		sheet = wb.createSheet(trans.translate(Keys.PERIOD_SHEET));
 		sheet.setColumnWidth(0, 6500);
 		sheet.setColumnWidth(1, 3000);
 
 		// sheet content
 		row = sheet.createRow(rowCnt);
 
 		str = crh.createRichTextString(trans.translate(Keys.PERIOD_SHEET));
 		str.applyFont(titleFont);
 		row.createCell(0).setCellValue(str);
 
 		for (DTOPeriod d : scenario.getChildren()) {
 			rowCnt = rowCnt + 2;
 			str = crh
 					.createRichTextString(d.get(DTOPeriod.Key.NAME).toString());
 			str.applyFont(sect1Font);
 			row = sheet.createRow(rowCnt);
 			row.createCell(0).setCellValue(str);
 
 			for (IPeriodicalValuesDTO pv : d.getChildren()) {
 				for (Iterator<Entry<String, IValue>> i = pv.iterator(); i
 						.hasNext();) {
 					Map.Entry<String, IValue> val = i.next();
 					row = sheet.createRow(++rowCnt);
 
 					str = crh.createRichTextString(trans
 							.translate(val.getKey()));
 					cell = row.createCell(0);
 					cell.setCellStyle(std);
 					cell.setCellValue(str);
 
 					str = crh.createRichTextString(val.getValue().toString());
 					cell = row.createCell(1);
 					cell.setCellStyle(std);
 					cell.setCellValue(str);
 				}
 			}
 		}
 
 	}
 
 	void buildResultSheet(Map<String, Calculable[]> resultMap) {
 		Sheet sheet;
 		Row row;
 		Cell cell;
 		RichTextString str;
 		int rowCnt = 0;
 
 		// result map sheet
 		sheet = wb.createSheet(trans.translate(Keys.RESULT_SHEET));
 		sheet.setColumnWidth(0, 6000);
 		sheet.setColumnWidth(1, 6000);
 		
 		//sheet content
 		row = sheet.createRow(rowCnt++);
 
 		str = crh.createRichTextString(trans.translate(Keys.RESULT_SHEET));
 		str.applyFont(titleFont);
 		row.createCell(0).setCellValue(str);
 
 		for (Entry<String, Calculable[]> e : resultMap.entrySet()) {
 			Calculable[] val = e.getValue();
 			if (val.length >= 1) {
 				row = sheet.createRow(++rowCnt);
 				str = crh.createRichTextString(trans.translate(e.getKey()));
 				cell = row.createCell(0);
 				cell.setCellStyle(std);
 				cell.setCellValue(str);
 				if (val[0] != null) {
 					str = crh.createRichTextString(val[0].toString());
 					cell= row.createCell(1);
 					cell.setCellStyle(std);
 					cell.setCellValue(str);
 				}
 			}
 			if (val.length > 1) {
 				for (int i = 1; i < val.length; i++) {
 					if (val[i] != null) {
 						row = sheet.createRow(++rowCnt);
 						str = crh.createRichTextString(val[i].toString());
 						cell= row.createCell(1);
 						cell.setCellStyle(std);
 						cell.setCellValue(str);
 					}
 				}
 			}
 		}
 	}
 
 	void buildResultSheet(DistributionMap distMap) {
 		Sheet sheet;
 		Row row;
 		Cell cell;
 		RichTextString str;
 		int rowCnt = 0;
 
 		// distribution map sheet
 		sheet = wb.createSheet(trans.translate(Keys.RESULT_SHEET));
 		sheet.setColumnWidth(0, 6000);
 		sheet.setColumnWidth(1, 6000);
 		
 		// sheet content
 		row = sheet.createRow(rowCnt++);
 
 		str = crh.createRichTextString(trans.translate(Keys.RESULT_SHEET));
 		str.applyFont(titleFont);
 		row.createCell(0).setCellValue(str);
 
 		for (Iterator<Entry<Double, Integer>> i = distMap.iterator(); i
 				.hasNext();) {
 			row = sheet.createRow(++rowCnt);
 			Entry<Double, Integer> val = i.next();
 
 			str = crh.createRichTextString(val.getKey().toString());
 			cell = row.createCell(0);
 			cell.setCellStyle(std);
 			cell.setCellValue(str);
 
 			str = crh.createRichTextString(val.getValue().toString());
 			cell = row.createCell(1);
 			cell.setCellStyle(std);
 			cell.setCellValue(str);
 		}
 	}
 
 	void closeDocument(String path) {
 		FileOutputStream fos;
 		try {
 			fos = new FileOutputStream(path);
 			wb.write(fos);
 			fos.close();
 		} catch (FileNotFoundException e) {
 			log.error(e);
 		} catch (IOException e) {
 			log.error(e);
 		}
 	}
 }
