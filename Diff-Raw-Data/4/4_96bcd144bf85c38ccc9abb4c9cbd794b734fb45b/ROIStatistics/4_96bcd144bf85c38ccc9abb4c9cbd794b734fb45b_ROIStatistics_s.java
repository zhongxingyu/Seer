 package plugins.adufour.roi;
 
 import icy.file.FileUtil;
 import icy.image.IntensityInfo;
 import icy.plugin.abstract_.Plugin;
 import icy.roi.ROI;
 import icy.roi.ROI2D;
 import icy.roi.ROIUtil;
 import icy.sequence.Sequence;
 
 import java.awt.Color;
 import java.awt.geom.Rectangle2D;
 
 import org.apache.poi.hssf.usermodel.HSSFPalette;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.xssf.usermodel.XSSFCellStyle;
 import org.apache.poi.xssf.usermodel.XSSFColor;
 
 import plugins.adufour.blocks.tools.roi.ROIBlock;
 import plugins.adufour.blocks.util.VarList;
 import plugins.adufour.vars.lang.VarROIArray;
 import plugins.adufour.vars.lang.VarWorkbook;
 
 public class ROIStatistics extends Plugin implements ROIBlock
 {
     private final int COL_NAME          = 0;
     private final int COL_X             = 1;
     private final int COL_Y             = 2;
     private final int COL_WIDTH         = 3;
     private final int COL_HEIGHT        = 4;
     private final int COL_SURFACE       = 5;
     private final int COL_VOLUME        = 6;
     private final int COL_MIN_INTENSITY = 7;
     private final int COL_AVG_INTENSITY = 8;
     private final int COL_MAX_INTENSITY = 9;
     
     VarROIArray       rois              = new VarROIArray("Regions of interest");
     VarWorkbook       book              = new VarWorkbook("Workbook", (Workbook) null);
     
     @Override
     public void declareInput(VarList inputMap)
     {
         inputMap.add(rois);
     }
     
     @Override
     public void declareOutput(VarList outputMap)
     {
         outputMap.add(book);
     }
     
     @Override
     public void run()
     {
         Workbook wb = book.getValue();
         if (wb == null) book.setValue(wb = new HSSFWorkbook());
         
         wb.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
         
         HSSFPalette palette = null;
         if (wb instanceof HSSFWorkbook)
         {
             palette = ((HSSFWorkbook) wb).getCustomPalette();
         }
         
         int roiID = 1;
         
         for (ROI roi : rois)
         {
             Color roiColor = roi.getColor();
             CellStyle style = wb.createCellStyle();
             
             style.setFillPattern(CellStyle.SOLID_FOREGROUND);
             
             if (palette != null)
             {
                 style.setFillForegroundColor(palette.findSimilarColor(roiColor.getRed(), roiColor.getGreen(), roiColor.getBlue()).getIndex());
             }
             else
             {
                 ((XSSFCellStyle) style).setFillForegroundColor(new XSSFColor(roiColor));
             }
             
             for (Sequence sequence : roi.getSequences())
             {
                 int sizeC = sequence.getSizeC();
                 
                 String sheetName = FileUtil.getFileName(sequence.getFilename());
                 if (sheetName == null) sheetName = sequence.getName();
                 
                 Sheet sheet = wb.getSheet(sheetName);
                 if (sheet == null) sheet = wb.createSheet(sheetName);
                 
                 Row header = sheet.getRow(0);
                 if (header == null)
                 {
                     header = sheet.createRow(0);
                     header.getCell(COL_NAME).setCellValue("Name");
                     header.getCell(COL_X).setCellValue("X");
                     header.getCell(COL_Y).setCellValue("Y");
                     header.getCell(COL_WIDTH).setCellValue("Width");
                     header.getCell(COL_HEIGHT).setCellValue("Height");
                     header.getCell(COL_SURFACE).setCellValue("Surface");
                     header.getCell(COL_VOLUME).setCellValue("Volume");
                     if (sizeC == 0)
                     {
                         header.getCell(COL_MIN_INTENSITY).setCellValue("Min. intensity");
                         header.getCell(COL_AVG_INTENSITY).setCellValue("Avg. intensity");
                         header.getCell(COL_MAX_INTENSITY).setCellValue("Max. intensity");
                     }
                     else
                     {
                         for(int c = 0; c < sizeC; c++)
                         {
                             header.getCell(COL_MIN_INTENSITY + 3 * c).setCellValue("Min. (ch. " + c + ")");
                             header.getCell(COL_AVG_INTENSITY + 3 * c).setCellValue("Avg. (ch. " + c + ")");
                             header.getCell(COL_MAX_INTENSITY + 3 * c).setCellValue("Max. (ch. " + c + ")");
                         }
                     }
                 }
                 
                 Row row = sheet.createRow(roiID);
                 
                 Cell name = row.getCell(COL_NAME);
                 name.setCellValue(roi.getName());
                 name.setCellStyle(style);
                 
                 if (roi instanceof ROI2D)
                 {
                     ROI2D r2 = (ROI2D) roi;
                     
                     Rectangle2D bounds = r2.getBounds2D();
                     
                     row.getCell(COL_X).setCellValue(bounds.getX());
                     row.getCell(COL_Y).setCellValue(bounds.getY());
                     row.getCell(COL_WIDTH).setCellValue(bounds.getWidth());
                     row.getCell(COL_HEIGHT).setCellValue(bounds.getHeight());
                     
                     if (sizeC > 1 && r2.getC() == -1)
                     {
                         ROI2D copy = (ROI2D) r2.getCopy();
                         for (int c = 0; c < sizeC; c++)
                         {
                             copy.setC(c);
                             IntensityInfo info = ROIUtil.getIntensityInfo(sequence, copy);
                             row.getCell(COL_MIN_INTENSITY + 3 * c).setCellValue(info.minIntensity);
                             row.getCell(COL_AVG_INTENSITY + 3 * c).setCellValue(info.meanIntensity);
                             row.getCell(COL_MAX_INTENSITY + 3 * c).setCellValue(info.maxIntensity);
                         }
                         copy.delete();
                     }
                     else
                     {
                         IntensityInfo info = ROIUtil.getIntensityInfo(sequence, roi);
                         row.getCell(COL_MIN_INTENSITY).setCellValue(info.minIntensity);
                         row.getCell(COL_AVG_INTENSITY).setCellValue(info.meanIntensity);
                         row.getCell(COL_MAX_INTENSITY).setCellValue(info.maxIntensity);
                     }
                 }
                 
                 row.getCell(COL_VOLUME).setCellValue(roi.getVolume());
                 row.getCell(COL_SURFACE).setCellValue(roi.getPerimeter());
                 
             }
             
             roiID++;
         }
         
         // this is mandatory since sheet creation cannot be detected
         book.valueChanged(book, null, wb);
     }
 }
