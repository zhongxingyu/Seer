 package com.thundermoose.bio.excel;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.usermodel.WorkbookFactory;
 import org.apache.poi.ss.util.CellReference;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.thundermoose.bio.dao.DataDao;
 import com.thundermoose.bio.exceptions.DatabaseException;
 import com.thundermoose.bio.model.Control;
 import com.thundermoose.bio.model.Plate;
 import com.thundermoose.bio.model.RawData;
 import com.thundermoose.bio.model.Run;
 import com.thundermoose.bio.model.ViabilityData;
 import com.thundermoose.bio.util.Utils;
 
 public class ExcelDataReader {
 
   private static final Logger logger = Logger.getLogger(ExcelDataReader.class);
 
   private static final String PLATE_ID = "AssayPlate";
   private static final String DATA = "Data";
   private static final String IDENTIFIER = "Identifier";
   private static final String TIME_MARKER = "TimeMarker";
 
   private final Map<String, String> controls;
 
   @SuppressWarnings("serial")
   private final Map<String, String> ignored = new HashMap<String, String>() {
     {
     }
   };
 
   private DataDao dao;
 
   public ExcelDataReader(DataDao dao, List<String> controls) {
     this.dao = dao;
     this.controls = new HashMap<String, String>();
     for (String c : controls) {
       this.controls.put(c, null);
     }
   }
 
   @Transactional(propagation = Propagation.REQUIRES_NEW)
   public void readRawData(String runName, InputStream file) throws IOException {
     Workbook wb;
     try {
       wb = WorkbookFactory.create(file);
     } catch (InvalidFormatException e1) {
       throw new RuntimeException(e1);
     }
 
     Sheet sheet = wb.getSheetAt(0);
 
     Map<String, Integer> head = new HashMap<String, Integer>();
     for (Cell cell : sheet.getRow(0)) {
       head.put(cell.getStringCellValue(), cell.getColumnIndex());
     }
     if (!head.containsKey(PLATE_ID) || !head.containsKey(DATA) || !head.containsKey(IDENTIFIER) || !head.containsKey(TIME_MARKER)) {
       throw new RuntimeException("Missing required column");
     }
 
     // create new run
     long runId = dao.addRun(new Run(runName, false));
 
     // map external to internal id
     Map<String, Long> plates = new HashMap<String, Long>();
     Map<String, Integer> dupCheck = new HashMap<String, Integer>();
 
     for (Row row : sheet) {
       if (row.getRowNum() == 0 || row.getCell(0) == null) {
         continue;
       }
 
       int index = -1;
       try {
         index = head.get(PLATE_ID);
         String plateName = row.getCell(index).getStringCellValue();
 
         // get plate, or create if necessary
         if (!plates.containsKey(plateName)) {
           plates.put(plateName, dao.addPlate(new Plate(runId, plateName)));
         }
         long plateId = plates.get(plateName);
 
         double time;
         index = head.get(TIME_MARKER);
         if (row.getCell(index).getCellType() == Cell.CELL_TYPE_STRING) {
           time = Double.parseDouble(row.getCell(index).getStringCellValue());
         } else {
           time = row.getCell(index).getNumericCellValue();
         }
 
         index = head.get(IDENTIFIER);
         String ident = row.getCell(index).getStringCellValue();
 
         index = head.get(DATA);
         float data = (float) row.getCell(index).getNumericCellValue();
 
         // track neg/pos
         if (controls.containsKey(ident)) {
           dao.addRawDataControl(new Control(-1, plateId, ident, time, data, new Date()));
         } else if (ignored.containsKey(ident)) {
           // do nothing
         } else {
           String d = plateId + "_" + ident + "_" + time;
           if (!dupCheck.containsKey(d)) {
             dupCheck.put(d, 1);
             try {
               dao.addRawData(new RawData(plateId, ident, time, data));
             } catch (Exception e) {
               throw new DatabaseException("Duplicate data found");
             }
           }
         }
       } catch (Exception e) {
         throw new RuntimeException("Error at cell " + CellReference.convertNumToColString(index) +
                row.getRowNum() + 1 + " [" + e.getMessage() + "]. Please check the data and try again.", e);
       }
     }
 
   }
 
   public void readLinkedViability(long runId, InputStream file) throws IOException {
     readViability(file, runId, null);
   }
 
   public void readIndependentViability(String runName, InputStream file) throws IOException {
     readViability(file, null, runName);
   }
 
   @Transactional(propagation = Propagation.REQUIRES_NEW)
   private void readViability(InputStream file, Long runId, String runName) throws IOException {
     Workbook wb;
     try {
       wb = WorkbookFactory.create(file);
     } catch (InvalidFormatException e1) {
       throw new RuntimeException(e1);
     }
 
 
     Sheet sheet = wb.getSheetAt(0);
 
     Map<String, Integer> head = new HashMap<String, Integer>();
     for (Cell cell : sheet.getRow(0)) {
       head.put(cell.getStringCellValue(), cell.getColumnIndex());
     }
     if (!head.containsKey(PLATE_ID) || !head.containsKey(DATA) || !head.containsKey(IDENTIFIER)) {
       throw new RuntimeException("Missing required column");
     }
 
     // if this is an independent load, need to create a run
     if (runId == null) {
       runId = dao.addRun(new Run(runName, true));
     }
 
     // map external to internal id
     Map<String, Long> plates = new HashMap<String, Long>();
     Map<String, Integer> dupCheck = new HashMap<String, Integer>();
 
     for (Row row : sheet) {
       if (row.getRowNum() == 0 || row.getCell(0) == null) {
         continue;
       }
 
       int index = -1;
       try {
         index = head.get(PLATE_ID);
         String plateName = row.getCell(index).getStringCellValue();
 
         // get plate, or create if necessary
         if (!plates.containsKey(plateName)) {
           if (runName != null) {
             plates.put(plateName, dao.addPlate(new Plate(runId, plateName)));
           } else {
             plates.put(plateName, dao.getPlateByName(runId, plateName).getId());
           }
         }
         long plateId = plates.get(plateName);
 
         index = head.get(IDENTIFIER);
         String ident = row.getCell(index).getStringCellValue();
 
         index = head.get(DATA);
         float data = (float) row.getCell(index).getNumericCellValue();
 
         // track neg/pos
         if (controls.containsKey(ident)) {
           dao.addViabilityControl(new Control(plateId, ident, data, new Date()));
         } else if (ignored.containsKey(ident)) {
           // do nothing
         } else {
           String d = plateId + "_" + ident;
           if (!dupCheck.containsKey(d)) {
             dupCheck.put(d, 1);
             try {
               dao.addViabilityData(new ViabilityData(plateId, ident, data));
             } catch (Exception e) {
               logger.error(e);
               throw new DatabaseException("Duplicate data found");
             }
           }
         }
       } catch (Exception e) {
         throw new RuntimeException("Error at cell " + CellReference.convertNumToColString(index) +
                row.getRowNum() + 1 + "[" + e.getMessage() + "]. Please check the data and try again.", e);
       }
     }
 
 
   }
 
 }
