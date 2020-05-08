 package org.freshwaterlife.fishlink.metadatacreator;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Name;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.freshwaterlife.fishlink.xlwrap.XLWrapMapException;
 
 /**
  *
  * @author Christian
  */
 public class CYAB_Workbook {
 
     Workbook poiWorkbook;
 
     public CYAB_Workbook(){
         poiWorkbook = new HSSFWorkbook();
     }
     
     CYAB_Sheet getSheet(String name) {
         Sheet sheet = poiWorkbook.getSheet(name);
         if (sheet == null) {
             sheet = poiWorkbook.createSheet(name);
         }
         return new CYAB_Sheet(sheet);
     }
 
     void write(String filePath) throws XLWrapMapException {
         FileOutputStream fileOut;
         try {
             fileOut = new FileOutputStream(filePath);
         } catch (IOException ex) {
            throw new XLWrapMapException("Unable to find + filePath", ex);
         }
         try {
             poiWorkbook.write(fileOut);
         } catch (IOException ex) {
            throw new XLWrapMapException("Unable to write + filePath", ex);
         }
         try {
             fileOut.close();
         } catch (IOException ex) {
            throw new XLWrapMapException("Unable to close + filePath", ex);
         }
 
     }
 
     Name createName() {
         return poiWorkbook.createName();
     }
 
 }
