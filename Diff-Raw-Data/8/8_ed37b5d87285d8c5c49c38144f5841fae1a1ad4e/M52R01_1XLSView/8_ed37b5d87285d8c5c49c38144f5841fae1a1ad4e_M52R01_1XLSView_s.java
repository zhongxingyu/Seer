 package biz.thaicom.eBudgeting.view;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.DataFormat;
 import org.apache.poi.ss.usermodel.Font;
 import org.apache.poi.ss.usermodel.IndexedColors;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.util.CellRangeAddress;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
 import biz.thaicom.eBudgeting.models.bgt.ObjectiveBudgetProposal;
 import biz.thaicom.eBudgeting.models.pln.Objective;
 import biz.thaicom.security.models.ThaicomUserDetail;
 
 public class M52R01_1XLSView extends AbstractPOIExcelView {
 
 	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:sss");
 	
 	@Override
 	protected Workbook createWorkbook() {
 		return new XSSFWorkbook();
 	}
 
 	@Override
 	protected void buildExcelDocument(Map<String, Object> model,
 			Workbook workbook, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		
 		ThaicomUserDetail currentUser = (ThaicomUserDetail) model.get("currentUser");
 		
         Map<String, CellStyle> styles = createStyles(workbook);
 
         
 		List<Objective> objectiveList = (List<Objective>) model.get("objectiveList");
 		Integer fiscalYear = (Integer) model.get("fiscalYear");
 		Sheet sheet = workbook.createSheet("sheet1");
 
 		Row firstRow = sheet.createRow(0);
 		Cell cell11 = firstRow.createCell(0);
 		cell11.setCellValue("รายงานตรวจสอบการบันทึกเงินระดับกิจกรรม ปี " + fiscalYear + " หน่วยงาน " + currentUser.getWorkAt().getName());
 		cell11.setCellStyle(styles.get("title"));
 		Cell cell12 = firstRow.createCell(1);
 
 		Row subFirstRow = sheet.createRow(1);
 		Cell subCell11 = subFirstRow.createCell(0);
 		subCell11.setCellValue("ผู้จัดทำรายงาน " + 
 				currentUser.getPerson().getFirstName() + " " +	currentUser.getPerson().getLastName() + 
 				" เวลาที่จัดทำรายงาน " +  sdf.format(new Date()) + "น.");
 		
 		Row secondRow = sheet.createRow(3);
 		Cell cell201 = secondRow.createCell(0);
 		cell201.setCellValue("กิจกรรม");
 		cell201.setCellStyle(styles.get("header"));
 		Cell cell202 = secondRow.createCell(1);
 		cell202.setCellValue("งบบุคลากร");
 		cell202.setCellStyle(styles.get("header"));
 		Cell cell203 = secondRow.createCell(2);
 		cell203.setCellStyle(styles.get("header"));
 		Cell cell204 = secondRow.createCell(3);
 		cell204.setCellStyle(styles.get("header"));
 		Cell cell205 = secondRow.createCell(4);
 		cell205.setCellStyle(styles.get("header"));
 		sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 4));
 		Cell cell206 = secondRow.createCell(5);
 		cell206.setCellValue("งบดำเนินงาน");
 		cell206.setCellStyle(styles.get("header"));
 		Cell cell207 = secondRow.createCell(6);
 		cell207.setCellStyle(styles.get("header"));
 		Cell cell208 = secondRow.createCell(7);
 		cell208.setCellStyle(styles.get("header"));
 		Cell cell209 = secondRow.createCell(8);
 		cell209.setCellStyle(styles.get("header"));
 		sheet.addMergedRegion(new CellRangeAddress(3, 3, 5, 8));
 		Cell cell210 = secondRow.createCell(9);
 		cell210.setCellValue("งบลงทุน");
 		cell210.setCellStyle(styles.get("header"));
 		Cell cell211 = secondRow.createCell(10);
 		sheet.addMergedRegion(new CellRangeAddress(3, 3, 9, 10));
 		Cell cell212 = secondRow.createCell(11);
 		cell212.setCellValue("เงินอุดหนุน");
 		cell212.setCellStyle(styles.get("header"));
 		Cell cell213 = secondRow.createCell(12);
 		cell213.setCellValue("รายจ่ายอื่น");
 		cell213.setCellStyle(styles.get("header"));
 		Cell cell214 = secondRow.createCell(13);
 		cell214.setCellValue("รวมเงินงบประมาณ");
 		cell214.setCellStyle(styles.get("header"));
 		
 		Row thirdRow = sheet.createRow(4);
 		Cell cell301 = thirdRow.createCell(0);
 		cell301.setCellStyle(styles.get("header"));
 		sheet.addMergedRegion(new CellRangeAddress(3, 4, 0, 0));
 		Cell cell302 = thirdRow.createCell(1);
 		cell302.setCellValue("เงินเดือน");
 		cell302.setCellStyle(styles.get("header"));
 		Cell cell303 = thirdRow.createCell(2);
 		cell303.setCellValue("ค่าจ้างประจำ");
 		cell303.setCellStyle(styles.get("header"));
 		Cell cell304 = thirdRow.createCell(3);
 		cell304.setCellValue("ค่าจ้างชั่วคราว");
 		cell304.setCellStyle(styles.get("header"));
 		Cell cell305 = thirdRow.createCell(4);
 		cell305.setCellValue("ค่าตอบแทนพนักงาน");
 		cell305.setCellStyle(styles.get("header"));
 		Cell cell306 = thirdRow.createCell(5);
 		cell306.setCellValue("ค่าตอบแทน");
 		cell306.setCellStyle(styles.get("header"));
 		Cell cell307 = thirdRow.createCell(6);
 		cell307.setCellValue("ค่าใช้สอย");
 		cell307.setCellStyle(styles.get("header"));
 		Cell cell308 = thirdRow.createCell(7);
 		cell308.setCellValue("ค่าวัสดุ");
 		cell308.setCellStyle(styles.get("header"));
 		Cell cell309 = thirdRow.createCell(8);
 		cell309.setCellValue("ค่าสาธารณูปโภค");
 		cell309.setCellStyle(styles.get("header"));
 		Cell cell310 = thirdRow.createCell(9);
 		cell310.setCellValue("ค่าครุภัณฑ์");
 		cell310.setCellStyle(styles.get("header"));
 		Cell cell311 = thirdRow.createCell(10);
 		cell311.setCellValue("ค่าที่ดินและสิ่งก่อสร้าง");
 		cell311.setCellStyle(styles.get("header"));
 		Cell cell312 = thirdRow.createCell(11);
 		cell312.setCellStyle(styles.get("header"));
 		sheet.addMergedRegion(new CellRangeAddress(3, 4, 11, 11));
 		Cell cell313 = thirdRow.createCell(12);
 		cell313.setCellStyle(styles.get("header"));
 		sheet.addMergedRegion(new CellRangeAddress(3, 4, 12, 12));
 		Cell cell314 = thirdRow.createCell(13);
 		cell314.setCellStyle(styles.get("header"));
 		sheet.addMergedRegion(new CellRangeAddress(3, 4, 13, 13));
 		
 		int i = 5;
 		int row = 4;
 		for (Objective o:objectiveList) {
 			
 			row = printRow(o, row+1, sheet, styles);
 			
 		}
 
 		sheet.setColumnWidth(0, 15000);
 		sheet.setColumnWidth(1, 3000);
 		sheet.setColumnWidth(2, 3000);
 		sheet.setColumnWidth(3, 3000);
 		sheet.setColumnWidth(4, 3000);
 		sheet.setColumnWidth(5, 3000);
 		sheet.setColumnWidth(6, 3000);
 		sheet.setColumnWidth(7, 3000);
 		sheet.setColumnWidth(8, 3000);
 		sheet.setColumnWidth(9, 3000);
 		sheet.setColumnWidth(10, 3500);
 		sheet.setColumnWidth(11, 3000);
 		sheet.setColumnWidth(12, 3000);
 		sheet.setColumnWidth(13, 4000);
 	}
 	
 	private int printRow(Objective o, int i, Sheet sheet, Map<String, CellStyle> styles) {
 		if(o == null ) return i;
 		Map<Long, Long> budgetSumMap = new HashMap<Long, Long>();
 		
 		//เงินเดือน
 		budgetSumMap.put(2L,0L);
 		//ค่าจ้างประจำ
 		budgetSumMap.put(24L,0L);
 		//ค่าจ้างชั่วคราว
 		budgetSumMap.put(39L,0L);
 		//พนักงานราชการ
 		budgetSumMap.put(44L,0L);
 		// ตอบแทน
 		budgetSumMap.put(48L, 0L);
 		// ใช้สอย
 		budgetSumMap.put(60L, 0L);
 		// วัสดุ
 		budgetSumMap.put(85L, 0L);
 		// สาธารณูปโภค
 		budgetSumMap.put(108L, 0L);
 		// ครุภัณฑ์
 		budgetSumMap.put(119L, 0L);
 		// สิ่งก่อสร้าง
 		budgetSumMap.put(479L, 0L);
 		// อุดหนุน
 		budgetSumMap.put(780L, 0L);
 		// รายจ่ายอื่น
 		budgetSumMap.put(786L, 0L);
 
 		
 		if(o.getFilterObjectiveBudgetProposals().size() > 0) {
 			for(ObjectiveBudgetProposal p : o.getFilterObjectiveBudgetProposals()){
 				
 				logger.debug("p.getBudgetType.getId() : " + p.getBudgetType().getId());
 				Long bId = p.getBudgetType().getId();
 				if(budgetSumMap.get(bId) != null) {
 						//logger.debug("*******************************" + parentPath + "  contain? " + key.toString() + " check if = " + parentPath.contains("."+key.toString()+"."));
 
 						Long newSum = budgetSumMap.get(bId) + p.getAmountRequest();
 						budgetSumMap.put(bId, newSum);
 							logger.debug(" put in key "+ bId + " value: " + newSum);
 					
 				}
 			}
 		}
 		
 		Row rows = sheet.createRow(i);
 		Cell c01 = rows.createCell(0);
 		String s="";
 		for(int j=0;j<o.getParentLevel()-3;j++) {
 			s+= "         ";
 		}
 		
 		c01.setCellValue(s+ "- <" + o.getCode() + ">" + o.getName());
 		c01.setCellStyle(styles.get("cellleft"));
 		
 		Cell c02 = rows.createCell(1);
 		c02.setCellValue(budgetSumMap.get(2L));
 		c02.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c03 = rows.createCell(2);
 		c03.setCellValue(budgetSumMap.get(24L));
 		c03.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c04 = rows.createCell(3);
 		c04.setCellValue(budgetSumMap.get(39L));
 		c04.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c05 = rows.createCell(4);
 		c05.setCellValue(budgetSumMap.get(44L));
 		c05.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c06 = rows.createCell(5);
 		c06.setCellValue(budgetSumMap.get(48L));
 		c06.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c07 = rows.createCell(6);
 		c07.setCellValue(budgetSumMap.get(60L));
 		c07.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c08 = rows.createCell(7);
 		c08.setCellValue(budgetSumMap.get(85L));
 		c08.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c09 = rows.createCell(8);
 		c09.setCellValue(budgetSumMap.get(108L));
 		c09.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c10 = rows.createCell(9);
 		c10.setCellValue(budgetSumMap.get(119L));
 		c10.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c11 = rows.createCell(10);
 		c11.setCellValue(budgetSumMap.get(479L));
 		c11.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c12 = rows.createCell(11);
 		c12.setCellValue(budgetSumMap.get(780L));
 		c12.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c13 = rows.createCell(12);
 		c13.setCellValue(budgetSumMap.get(786L));
 		c13.setCellStyle(styles.get("cellnumber"));
 		
 		Cell c14 = rows.createCell(13);
 		c14.setCellType(HSSFCell.CELL_TYPE_FORMULA);
 		c14.setCellFormula("SUM(B"+(i+1)+":M"+(i+1)+")");
 		c14.setCellStyle(styles.get("cellnumber"));
 		
 		
 		// now print each child
 		for(Objective child : o.getChildren()){
 			i = printRow(child, i+1, sheet, styles);
 		}
 		
 		return i;
 		
 	}
 	
 	
     private static Map<String, CellStyle> createStyles(Workbook wb){
         Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
 
         short borderColor = IndexedColors.GREY_50_PERCENT.getIndex();
 
         CellStyle style;
         DataFormat format = wb.createDataFormat();
         Font titleFont = wb.createFont();
         titleFont.setFontName("Tahoma");
         titleFont.setFontHeightInPoints((short)12);
         titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
         style = wb.createCellStyle();
         style.setAlignment(CellStyle.ALIGN_LEFT);
         style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
         style.setFont(titleFont);
         styles.put("title", style);
 
         Font headFont = wb.createFont();
         headFont.setFontName("Tahoma");
         headFont.setFontHeightInPoints((short)11);
         headFont.setColor(IndexedColors.WHITE.getIndex());
         headFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
         style = wb.createCellStyle();
         style.setAlignment(CellStyle.ALIGN_CENTER);
         style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
         style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
         style.setFillPattern(CellStyle.SOLID_FOREGROUND);
         style.setFont(headFont);
         style.setBorderRight(CellStyle.BORDER_THIN);
         style.setRightBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderLeft(CellStyle.BORDER_THIN);
         style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderTop(CellStyle.BORDER_THIN);
         style.setTopBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderBottom(CellStyle.BORDER_THIN);
         style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
         style.setWrapText(true);
         styles.put("header", style);
 
         Font groupFont = wb.createFont();
         groupFont.setFontName("Tahoma");
         groupFont.setFontHeightInPoints((short)11);
         groupFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
         style = wb.createCellStyle();
         style.setAlignment(CellStyle.ALIGN_LEFT);
         style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
         style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
         style.setFillPattern(CellStyle.SOLID_FOREGROUND);
         style.setWrapText(true);
         style.setFont(groupFont);
         style.setBorderRight(CellStyle.BORDER_THIN);
         style.setRightBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderLeft(CellStyle.BORDER_THIN);
         style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderTop(CellStyle.BORDER_THIN);
         style.setTopBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderBottom(CellStyle.BORDER_THIN);
         style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
         styles.put("groupleft", style);
 
         style = wb.createCellStyle();
         style.setAlignment(CellStyle.ALIGN_RIGHT);
         style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
         style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
         style.setFillPattern(CellStyle.SOLID_FOREGROUND);
         style.setWrapText(true);
         style.setFont(groupFont);
         style.setBorderRight(CellStyle.BORDER_THIN);
         style.setRightBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderLeft(CellStyle.BORDER_THIN);
         style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderTop(CellStyle.BORDER_THIN);
         style.setTopBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderBottom(CellStyle.BORDER_THIN);
         style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
         styles.put("groupright", style);
 
         style = wb.createCellStyle();
         style.setAlignment(CellStyle.ALIGN_RIGHT);
         style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
         style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
         style.setFillPattern(CellStyle.SOLID_FOREGROUND);
         style.setWrapText(true);
         style.setDataFormat(format.getFormat("#,##0"));
         style.setFont(groupFont);
         style.setBorderRight(CellStyle.BORDER_THIN);
         style.setRightBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderLeft(CellStyle.BORDER_THIN);
         style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderTop(CellStyle.BORDER_THIN);
         style.setTopBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderBottom(CellStyle.BORDER_THIN);
         style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
         styles.put("groupnumber", style);
 
         style = wb.createCellStyle();
         style.setAlignment(CellStyle.ALIGN_LEFT);
         style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
         style.setWrapText(true);
         style.setBorderRight(CellStyle.BORDER_THIN);
         style.setRightBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderLeft(CellStyle.BORDER_THIN);
         style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderTop(CellStyle.BORDER_THIN);
         style.setTopBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderBottom(CellStyle.BORDER_THIN);
         style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
         styles.put("cellleft", style);
 
         style = wb.createCellStyle();
         style.setAlignment(CellStyle.ALIGN_CENTER);
         style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
         style.setWrapText(true);
         style.setBorderRight(CellStyle.BORDER_THIN);
         style.setRightBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderLeft(CellStyle.BORDER_THIN);
         style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderTop(CellStyle.BORDER_THIN);
         style.setTopBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderBottom(CellStyle.BORDER_THIN);
         style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
         styles.put("cellcenter", style);
 
         style = wb.createCellStyle();
         style.setAlignment(CellStyle.ALIGN_RIGHT);
         style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
         style.setWrapText(true);
         style.setBorderRight(CellStyle.BORDER_THIN);
         style.setRightBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderLeft(CellStyle.BORDER_THIN);
         style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderTop(CellStyle.BORDER_THIN);
         style.setTopBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderBottom(CellStyle.BORDER_THIN);
         style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
         styles.put("cellright", style);
 
         style = wb.createCellStyle();
         style.setAlignment(CellStyle.ALIGN_RIGHT);
         style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
         style.setWrapText(true);
         style.setDataFormat(format.getFormat("#,##0"));
         style.setBorderRight(CellStyle.BORDER_THIN);
         style.setRightBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderLeft(CellStyle.BORDER_THIN);
         style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderTop(CellStyle.BORDER_THIN);
         style.setTopBorderColor(IndexedColors.BLACK.getIndex());
         style.setBorderBottom(CellStyle.BORDER_THIN);
         style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
         styles.put("cellnumber", style);
 
         return styles;
     }
 
 }
