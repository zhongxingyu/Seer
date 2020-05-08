 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.splash.importer;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.splash.swing.Cell;
 import org.amanzi.splash.utilities.CSVParser;
 import org.amanzi.splash.utilities.NeoSplashUtil;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.neo4j.api.core.Transaction;
 
 import com.eteks.openjeks.format.CellFormat;
 
 
 /**
  * Importer of CVS data
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 public class CSVImporter extends AbstractImporter {
 
     /**
      * Constructor
      * 
      * @param containerPath path to Project that will contain imported Spreadsheet
      * @param fileName name of imported File
      * @param stream content of imported File
      */
     public CSVImporter(IPath containerPath, String fileName, InputStream stream, long fileSize) {
         super(containerPath, fileName, stream, fileSize);
     }
 
     @Override
     public void run(IProgressMonitor monitor) throws InvocationTargetException {
         monitor.beginTask("Importing CSV data into Splash", 100);
         
         //create a Spreadsheet
         createSpreadsheet();
         
         Transaction tx = NeoUtils.beginTransaction();
         try {            
             BufferedReader reader = new BufferedReader(new InputStreamReader(fileContent));
             String line;
             
             line = reader.readLine();
             
             int i=0;
             int j=0;
             
             // detecting type of separator;
             char sep=';';
             if (line.contains(";") == true){
                 sep = ';';
             }else if (line.contains("\t")){
                 sep = '\t';
             }else if (line.contains(",")){
                 sep = ',';
             }
             
             CSVParser parser = new CSVParser(sep);
             int perc = 0;
             long totalBytes = fileSize;
             long bytesRead = 0;
             int prevPerc = 0;
             CellFormat defaultFormat = new CellFormat();
             while (line != null  && line.lastIndexOf(sep) > 0){
                 NeoSplashUtil.logn("loading line #" + i);
 
                 List<String> list = parser.parse(line);
                 Iterator<String> it = list.iterator();
                 j = 0;
                 while (it.hasNext()) {
                     Cell cell = new Cell(i, j, "", (String) it.next(), defaultFormat);
                     //save a cell
                     saveCell(cell);
                     j++;
                 }
 
                 monitor.setTaskName("Loading record #" + i);
                 bytesRead += line.length();
                 perc = (int)(100.0 * (float)bytesRead / (float)totalBytes);
                 if (perc > prevPerc) {                    
                     monitor.worked(perc - prevPerc);
                     prevPerc = perc;
                 }
 
                 line = reader.readLine();
                 
                 i++;
                 
                 //update transaction each 1000 rows
                 if ((i % 20) == 0) {
                     tx = updateTransaction(tx);
                 }
             }
             tx.success();
         } catch (IOException e) {
             tx.failure();
             throw new InvocationTargetException(e);
         }
         finally {           
             tx.finish();
         }
     }
     
 }
