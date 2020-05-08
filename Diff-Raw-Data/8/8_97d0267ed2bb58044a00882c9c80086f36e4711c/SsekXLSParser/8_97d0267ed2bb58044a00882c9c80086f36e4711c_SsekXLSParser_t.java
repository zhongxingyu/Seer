 package com.nomura.cims2;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.poifs.filesystem.POIFSFileSystem;
 import org.apache.poi.ss.usermodel.*;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.util.Iterator;
 
 /**
  * This java program is used to read the data from a Excel file and display them
  * on the console output.
  *
  * @author arkadiy
  */
 public class SsekXLSParser {
 
     /**
      * Creates a new instance of POIExcelReader
      */
     public SsekXLSParser() {
     }
 
     /**
      * This method is used to display the Excel content to command line.
      *
     * @param excelFilePath
      */
     @SuppressWarnings("unchecked")
    public void displayFromExcel(String excelFilePath) {
         InputStream inputStream = null;
 
         try {
            inputStream = new FileInputStream(excelFilePath);
         } catch (FileNotFoundException e) {
             System.out.println("File not found in the specified path.");
             e.printStackTrace();
         }
 
         POIFSFileSystem fileSystem = null;
 
         try {
             fileSystem = new POIFSFileSystem(inputStream);
 
             Workbook workBook = new HSSFWorkbook(fileSystem);
             Sheet sheet = workBook.getSheetAt(0);
             Iterator<Row> rows = sheet.rowIterator();
 
             while (rows.hasNext()) {
                 Row row = rows.next();
 
 // display row number in the console.
                 System.out.println("Row No.: " + row.getRowNum());
 
 // once get a row its time to iterate through cells.
                 Iterator<Cell> cells = row.cellIterator();
 
                 while (cells.hasNext()) {
                     Cell cell = cells.next();
 
                     System.out.println("Cell Type.: " + cell.getCellType());
 
 /*
  * Now we will get the cell type and display the values
  * accordingly.
  */
                     switch (cell.getCellType()) {
                         case Cell.CELL_TYPE_NUMERIC: {
 
                             // cell type numeric.
                             System.out.println("Numeric value: " + cell.getNumericCellValue());
 
                             break;
                         }
 
                         case Cell.CELL_TYPE_STRING: {
 
                             // cell type string.
                             RichTextString richTextString = cell.getRichStringCellValue();
 
                             System.out.println("String value: " + richTextString.getString());
 
                             break;
                         }
 
                         default: {
 
                             // types other than String and Numeric.
                             System.out.println("Type not supported.");
 
                             break;
                         }
                     }
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * The main executable method to test displayFromExcel method.
      *
      * @param args
      */
 
     public static void main(String[] args) {
         SsekXLSParser poiExample = new SsekXLSParser();
         String xlsPath = "d://temp//test.xls";
 
         poiExample.displayFromExcel(xlsPath);
     }
 }
