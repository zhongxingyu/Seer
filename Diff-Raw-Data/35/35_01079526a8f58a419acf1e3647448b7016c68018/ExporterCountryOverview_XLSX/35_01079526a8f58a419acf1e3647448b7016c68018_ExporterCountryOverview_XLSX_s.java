 package org.ocha.hdx.exporter.country;
 
 import java.util.List;
 
 import org.apache.poi.ss.util.WorkbookUtil;
 import org.apache.poi.xssf.usermodel.XSSFRow;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.ocha.hdx.exporter.Exporter;
 import org.ocha.hdx.exporter.Exporter_XLSX;
 import org.ocha.hdx.service.ExporterService;
 
 /**
  * Exporter for a country - overview.
  * 
  * @author bmichiels
  * 
  */
 public class ExporterCountryOverview_XLSX extends Exporter_XLSX<ExporterCountryQueryData> {
 
 	public ExporterCountryOverview_XLSX(final ExporterService exporterService) {
 		super(exporterService);
 	}
 
 	public ExporterCountryOverview_XLSX(final Exporter<XSSFWorkbook, ExporterCountryQueryData> exporter) {
 		super(exporter);
 	}
 
 	@Override
 	public XSSFWorkbook export(final XSSFWorkbook workbook, final ExporterCountryQueryData queryData) {
 
 		final List<Object[]> data = exporterService.getCountryOverviewData(queryData);
 
 		/* TODO i18n */
 
 		// Creating the sheet
 		final String safeName = WorkbookUtil.createSafeSheetName("Overview");
 		final XSSFSheet sheet = workbook.createSheet(safeName);
 
 		// Assign the headers to the title row
		final String[] headers = { "Indicator ID", "Indicator name", "Value", "Last updated", "Source dataset" };
 		createHeaderCells(sheet, headers);
 
 		// Fill with the data
 		// Sample : CD010 Wikipedia: geography http://en.wikipedia.org/wiki/Colombia#Geography 2014-01-22 wikipedia
 		for (int i = 0; i < data.size(); i++) {
 			final Object[] element = data.get(i);
 
 			// Create the row (not overwriting the headers :-) )
 			final XSSFRow row = sheet.createRow(i + 1);
 
 			// "Indicator ID" (always set)
 			createCell(row, (short) 0, element[0].toString());
 
 			// Other infos may be unavailable if there are no values for the indicator
 			if (1 < element.length) {
 
 				// "Indicator name"
 				createCell(row, (short) 1, element[1].toString());
 
 				// "Value"
 				final String value = element[2].toString();
 				// Value can be a URL
 				if ((null != value) && value.startsWith("http://")) {
 					try {
 						createUrlCell(row, (short) 2, value, value);
 					} catch (final IllegalArgumentException e) {
 						// Some url are considered not valid, even if browsers accept them.
 						// example : http://en.wikipedia.org/wiki/United_States#Geography.2C_climate.2C_and_environment
 						createCell(row, (short) 2, value);
 					}
 				}
 				// or a simple string
 				else {
 					createCell(row, (short) 2, value);
 				}
 
 				// "Last updated"
				createCell(row, (short) 3, element[3].toString());

 				// "Source dataset"
				createCell(row, (short) 4, element[4].toString());
 			}
 		}
 
 		// Freeze the headers
 		// Freeze the 2 first columns
 		sheet.createFreezePane(2, 1, 2, 1);
 
 		// Auto size the columns, except column C, which has a fixed width
 		for (int i = 0; i < headers.length; i++) {
			if (2 == i) {
 				sheet.setColumnWidth(i, 5000);
 			} else {
 				sheet.autoSizeColumn(i);
 			}
 		}
 
 		return super.export(workbook, queryData);
 	}
 }
