 
 package edu.duke.cabig.catrip.gui.util;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableModel;
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFCellStyle;
 import org.apache.poi.hssf.usermodel.HSSFFont;
 import org.apache.poi.hssf.usermodel.HSSFPalette;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 
 /**
  *
  * @author Sanjeev Agarwal
  */
 public class ExcelResultExporter {
     
     static  HSSFWorkbook workbook = new HSSFWorkbook();
     static int num = 0;
     private static JFrame owner ;
     
     public static void exportToExcel(JTable table, JFrame owner_){
         owner = owner_;
         if(GUIConstants.resultAvailable){
             export(table);
         }
     }
     
     
     public static void export(JTable table){
         try {
             
             String file = GUIConstants.CATRIP_HOME+File.separator+"caTRIP_Query_Results.xls";
             
             TableModel model = table.getModel();
             File f = new File(file);
             
             if (f.exists()){
                 FileInputStream fis = new FileInputStream(f);
                 workbook = new HSSFWorkbook(fis);
                 fis.close();
                 num = workbook.getNumberOfSheets();
             }
             
             
             
             HSSFSheet sheet1 = workbook.createSheet("Sheet "+num);
             sheet1.setDefaultColumnWidth((short) 20);
             
             exportTableToSheet(table, sheet1);
             
             FileOutputStream fout = new FileOutputStream(f);
             workbook.write(fout);
             fout.close();
             
             
             JOptionPane jpane = new JOptionPane();
            jpane.showMessageDialog(owner ,"The results are exported to HTML file :\n"+file);
             
             
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     
     
     
     
     
     
     
     public static void exportTableToSheet(JTable table, HSSFSheet sheet) {
         int rowCount = table.getRowCount();
         int colCount = table.getColumnCount();
         
         
         
         // set the columns also.. in the first row..
         TableModel model = table.getModel();
         for(int i=0; i<colCount;i++) {
             String colName = model.getColumnName(i);
             createHSSFCell(sheet, colName, 0, i);
         }
         
         
         int currentSheetRow = 1;
         for (int tableRowIndex = 0; tableRowIndex < rowCount; tableRowIndex++) {
             for (int tableColIndex = 0; tableColIndex < colCount; tableColIndex++) {
                 // create and format the cell in the spreadsheet
                 createAndFormatCell(table, tableRowIndex, tableColIndex, sheet, currentSheetRow);
             }
             currentSheetRow++;
         }
     }
     
     
     private static  void createAndFormatCell(JTable table, int tableRowIndex, int tableColIndex,
             HSSFSheet sheet, int currentSheetRow) {
         
         // get the cell value from the table
         Object cellValue = table.getValueAt(tableRowIndex, tableColIndex);
 //        System.out.println("XXXXX  "+cellValue.getClass().getName());
         // create the cell
         HSSFCell cell = createHSSFCell(sheet, cellValue, currentSheetRow, tableColIndex);
         
         // get the renderer component that renders the cell
         TableCellRenderer renderer = table.getCellRenderer(tableRowIndex, tableColIndex);
         Component rendererComponent = renderer.getTableCellRendererComponent(table,
                 cellValue,
                 false,
                 false,
                 tableRowIndex,
                 tableColIndex);
         
         
         if (rendererComponent instanceof JLabel) {
             // if it is a JLabel, get the label text which is the actual formatted displayed text
             // and not the raw cell value
             JLabel label = (JLabel) rendererComponent;
             cellValue = label.getText();
         }
         
 //        formatCell(cell, rendererComponent);  / do not format the cells..
         
     }
     
     
     private static  HSSFCell createHSSFCell(HSSFSheet sheet, Object value, int row, int col) {
         // create row if not yet created
         HSSFRow hssfRow = sheet.getRow(row);
         hssfRow = (hssfRow == null) ? sheet.createRow(row) : hssfRow;
         
         // create cell if not yet created
         HSSFCell cell = hssfRow.getCell((short) col);
         cell = (cell == null) ? hssfRow.createCell((short) col) : cell;
         
         // set the cell value
         String cellValue = (value == null) ? "" : value.toString();
         // check if the value can be converted to a double convert it and than add it.
         Double dobValue = null; 
         try{
           dobValue =  Double.valueOf(cellValue);
         }catch (Exception e){}
                 
         if (dobValue != null){
             cell.setCellValue(dobValue.doubleValue());
         } else {
             cell.setCellValue(cellValue);
         }
         
         
         return cell;
     }
     
     
     public static  void formatCell(HSSFCell cell, Component rendererComponent) {
         
         // create a style
         HSSFCellStyle cellStyle = workbook.createCellStyle();
         
         // set the cell color
         cellStyle.setWrapText(false);
         
         
         cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
         Color color = rendererComponent.getBackground();
         HSSFPalette palette = workbook.getCustomPalette();
         
         // maintain(increment after each use) unused color index as an instance variable
         short someUnusedColorIndex = 10;
         palette.setColorAtIndex(someUnusedColorIndex, (byte) color.getRed(),
                 (byte) color.getGreen(), (byte) color.getBlue());
         cellStyle.setFillForegroundColor(someUnusedColorIndex);
         
         
         // set the font
         Font font = rendererComponent.getFont();
         HSSFFont hssfFont = createHSSFFont(font);
         cellStyle.setFont(hssfFont);
         
         // set the border
         cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
         cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
         cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
         cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
         
         
         // don't forget to set the cell style!
         cell.setCellStyle(cellStyle);
     }
     
     
     private static  HSSFFont createHSSFFont(Font font) {
         HSSFFont hssfFont = workbook.createFont();
         hssfFont.setFontName(font.getName());
         hssfFont.setItalic(font.isItalic());
         hssfFont.setBoldweight(font.isBold() ? HSSFFont.BOLDWEIGHT_BOLD
                 : HSSFFont.BOLDWEIGHT_NORMAL);
         hssfFont.setFontHeightInPoints((short) font.getSize());
         hssfFont.setUnderline(HSSFFont.U_NONE);
         
         return hssfFont;
     }
     
     
     
     
     
 }
