 package org.ocha.hdx.exporter.hierarchical;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.poi.ss.util.CellRangeAddress;
 import org.apache.poi.ss.util.WorkbookUtil;
 import org.apache.poi.xssf.usermodel.XSSFRow;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.ocha.hdx.exporter.Exporter_XLSX;
 import org.ocha.hdx.exporter.QueryData;
 import org.ocha.hdx.exporter.country.ExporterCountryQueryData;
 import org.ocha.hdx.exporter.helper.WFPReportRow;
 import org.ocha.hdx.service.ExporterService;
 
 public class HierarchicalEntityMultiIndicatorTypesExporter_XLSX extends Exporter_XLSX<QueryData> {
 
 	List<WFPReportRow> rows;
 
 	long minYear = Long.MAX_VALUE;
 	long maxYear = Long.MIN_VALUE;
 
 	public HierarchicalEntityMultiIndicatorTypesExporter_XLSX(final ExporterService exporterService, final List<WFPReportRow> rows) {
 		super(exporterService);
 		this.rows = rows;
 		calculateBoundaries();
 
 		// TODO Auto-generated constructor stub
 	}
 
 	public XSSFWorkbook export(final XSSFWorkbook workbook, final ExporterCountryQueryData queryData) throws Exception {
 		final String safeName = WorkbookUtil.createSafeSheetName("");
 		final XSSFSheet sheet = workbook.createSheet(safeName);
 
 		// Define the headers
 
 		{
 			final ArrayList<Object> headers = new ArrayList<Object>();
 
 			headers.add("Administrative Unit");
 			headers.add(" ");
 			headers.add(" ");
 
 			headers.add("2007");
 			headers.add(" ");
 			headers.add(" ");
 
 			headers.add("2008");
 			headers.add(" ");
 			headers.add(" ");
 
 			headers.add("2009");
 			headers.add(" ");
 			headers.add(" ");
 
 			headers.add("2010");
 			headers.add(" ");
 			headers.add(" ");
 
 			headers.add("2011");
 			headers.add(" ");
 			headers.add(" ");
 
 			headers.add("2012");
 			headers.add(" ");
 			headers.add(" ");
 
 			headers.add("2013");
 			headers.add(" ");
 			headers.add(" ");
 
 			createColumnHeaderCells(sheet, headers);
 
 			sheet.addMergedRegion(new CellRangeAddress(0, // mention first row here
 					0, // mention last row here, it is 1 as we are doing a column wise merging
 					0, // mention first column of merging
 					2 // mention last column to include in merge
 			));
 
 			sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 5));
 			sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 8));
 			sheet.addMergedRegion(new CellRangeAddress(0, 0, 9, 11));
 			sheet.addMergedRegion(new CellRangeAddress(0, 0, 12, 14));
 			sheet.addMergedRegion(new CellRangeAddress(0, 0, 15, 17));
 			sheet.addMergedRegion(new CellRangeAddress(0, 0, 18, 20));
 			sheet.addMergedRegion(new CellRangeAddress(0, 0, 21, 23));
 			sheet.addMergedRegion(new CellRangeAddress(0, 0, 24, 26));
 
 		}
 		final ArrayList<Object> headers2 = new ArrayList<Object>();
 		headers2.add("Country");
 		headers2.add("Adm0");
 		headers2.add("Adm1");
 
 		headers2.add("Poor");
 		headers2.add("Borderline");
 		headers2.add("Acceptable");
 
 		headers2.add("Poor");
 		headers2.add("Borderline");
 		headers2.add("Acceptable");
 
 		headers2.add("Poor");
 		headers2.add("Borderline");
 		headers2.add("Acceptable");
 
 		headers2.add("Poor");
 		headers2.add("Borderline");
 		headers2.add("Acceptable");
 
 		headers2.add("Poor");
 		headers2.add("Borderline");
 		headers2.add("Acceptable");
 
 		headers2.add("Poor");
 		headers2.add("Borderline");
 		headers2.add("Acceptable");
 
 		headers2.add("Poor");
 		headers2.add("Borderline");
 		headers2.add("Acceptable");
 
 		final XSSFRow titleRow = sheet.createRow(1);
 
 		createColumnHeaderCells(sheet, titleRow, headers2);
 
 		// Fill with the data
 		// We start right just after the headers row
 		int rowIndex = 2;
 
 		for (final WFPReportRow wfpPreportRow : rows) {
 			int cellIndex = 0;
 			final XSSFRow xssfRow = sheet.createRow(rowIndex);
 			rowIndex++;
 
 			createCell(xssfRow, cellIndex++, wfpPreportRow.getHighestLevelAsString());
 			createCell(xssfRow, cellIndex++, wfpPreportRow.getMediumLevelAsString());
 			createCell(xssfRow, cellIndex++, wfpPreportRow.getLowestEntityAsString());
 
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2007, "PVF040"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2007, "PVF050"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2007, "WFP_ACCEPTABLE"));
 
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2008, "PVF040"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2008, "PVF050"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2008, "WFP_ACCEPTABLE"));
 
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2009, "PVF040"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2009, "PVF050"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2009, "WFP_ACCEPTABLE"));
 
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2010, "PVF040"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2010, "PVF050"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2010, "WFP_ACCEPTABLE"));
 
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2011, "PVF040"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2011, "PVF050"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2011, "WFP_ACCEPTABLE"));
 
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2012, "PVF040"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2012, "PVF050"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2012, "WFP_ACCEPTABLE"));
 
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2013, "PVF040"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2013, "PVF050"));
 			addValueToRow(xssfRow, cellIndex++, wfpPreportRow.getValue(2013, "WFP_ACCEPTABLE"));
 
 		}
 
 		for (int i = 0; i < headers2.size(); i++) {
 
 			sheet.autoSizeColumn(i);
 		}
 
 		return workbook;
 	}
 
 	private void addValueToRow(final XSSFRow xssfRow, final int position, final Double value) {
 		if (null != value) {
 			createNumCell(xssfRow, position, value);
 		} else {
 			createCell(xssfRow, position, " ");
 		}
 	}
 
 	private void calculateBoundaries() {
		for (final WFPReportRow wfpPreportRow : rows) {
			minYear = 10;
			maxYear = 10;
		}
 
 	}
 
 }
