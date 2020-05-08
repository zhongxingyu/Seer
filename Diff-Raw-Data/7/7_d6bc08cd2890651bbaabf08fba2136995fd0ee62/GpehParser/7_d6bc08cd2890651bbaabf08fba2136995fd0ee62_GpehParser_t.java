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
 
 package org.amanzi.awe.gpeh;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.zip.GZIPInputStream;
 
 import org.amanzi.awe.gpeh.parser.Events;
 import org.amanzi.awe.gpeh.parser.GPEHFileNameWrapper;
 import org.amanzi.awe.gpeh.parser.Parameters;
 import org.amanzi.awe.gpeh.parser.internal.GPEHEvent;
 import org.amanzi.neo.loader.core.CommonConfigData;
 import org.amanzi.neo.loader.core.ProgressEventImpl;
 import org.amanzi.neo.loader.core.parser.CommonFilesParser;
 import org.amanzi.neo.loader.core.saver.ISaver;
 import org.kc7bfi.jflac.io.BitInputStream;
 
 /**
  * TODO Purpose of
  * <p>
  * </p>
  * 
  * @author TsAr
  * @since 1.0.0
  */
 public class GpehParser extends CommonFilesParser<GpehTransferData, CommonConfigData> {
     /** double PERCENTAGE_FIRE field */
     private static final double PERCENTAGE_FIRE = 0.05;
     private Set<Integer> possibleIds;
 
     @Override
     protected GpehTransferData getFinishData() {
         return null;
     }
 
     @Override
     public void init(CommonConfigData properties, ISaver<GpehTransferData> saver) {
         super.init(properties, saver);
         possibleIds = new HashSet<Integer>();
         Set<Events> evntSet = (Set<Events>)properties.getAdditionalProperties().get(GpehImportWizardPage.SELECTED_EVENT);
         if (evntSet != null) {
             for (Events events : evntSet) {
                 possibleIds.add(events.getId());
             }
         }
 
     }
 
     @Override
     protected List<FileElement> getElementList() {
         CommonConfigData prop = getProperties();
         String descr = getDescriptionFormat();
         if (prop.getFileToLoad() == null) {
         	List<File> fileToLoad = null;
         	if (prop.getRoot() == null) {
         		fileToLoad = getAllFilesMulti(prop.getMultiRoots());
         	}
         	else {
         		fileToLoad = getAllFiles(prop.getRoot());
         	}
             prop.setFileToLoad(fileToLoad);
         }
         Collections.sort(prop.getFileToLoad(), new Comparator<File>() {
 
             @Override
             public int compare(File o1, File o2) {
                 return o1.getName().compareTo(o2.getName());
             }
 
         });
         List<FileElement> result = new LinkedList<FileElement>();
         for (File file : prop.getFileToLoad()) {
             result.add(new FileElement(file, descr));
         }
         return result;
     }
 
     @Override
     protected boolean parseElement(FileElement element) {
         if (element.getFile().getName().contains("Mp0")) {
             return false;
         }
         if (possibleIds.isEmpty()) {
             return false;
         }
         GPEHFileNameWrapper timeWrapper = new GPEHFileNameWrapper(element.getFile().getName());
         if (!timeWrapper.isValid()) {
             System.err.println(String.format("Can't parse file %s. incorrect name format", element.getFile().getName()));
             return false;
         }
 
         Calendar cl = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cl.clear();        
         cl.set(Calendar.YEAR, timeWrapper.getYear());
        cl.set(Calendar.MONTH, timeWrapper.getMonth() - 1);
        cl.set(Calendar.DAY_OF_MONTH, timeWrapper.getDay());        
         long timestampOfDay = cl.getTimeInMillis();
         cl.set(Calendar.HOUR, timeWrapper.getHhStart());
         cl.set(Calendar.MINUTE, timeWrapper.getMmStart());
         long timestamp = cl.getTimeInMillis();
         //Kasnitskij_V:
         String meContext = timeWrapper.getMeContext();
         
         InputStream inputStream = null;
         try {
             inputStream = new FileInputStream(element.getFile());
             long fileSize = element.getFile().length();
             if (element.getFile().getName().endsWith("gz")) {
                 inputStream = new GZIPInputStream(inputStream);
                 fileSize = getFileSize(element.getFile());
             }
 
             BitInputStream bitStream = new BitInputStream(inputStream);
             double persentageOld = 0;
             while (true) {
                 try {
                     int recordLen = bitStream.readRawUInt(16) - 3;
                     int recordType = bitStream.readRawUInt(8);
 
                     GPEHEvent result = new GPEHEvent();
                     switch (recordType) {
                     case 4:
                         try {
                             org.amanzi.awe.gpeh.parser.internal.GPEHParser.parseEvent(bitStream, result, recordLen, possibleIds,
                                     timeWrapper);
                         
                             if (result.getEvent() != null) {
                                 GpehTransferData data = new GpehTransferData();
                                 data.put(GpehTransferData.EVENT, result.getEvent());
                                 data.put(GpehTransferData.TIMESTAMP_OF_DAY, timestampOfDay);
                                 data.put(GpehTransferData.TIMESTAMP, timestamp);
                                 // Kasnitskij_V:
                                 
                                 data.put(GpehTransferData.ME_CONTEXT, meContext);
                                 for (Map.Entry<Parameters, Object> entry : result.getEvent().getProperties().entrySet()) {
                                     data.put(entry.getKey().name(), entry.getValue());
                                 }
                                 getSaver().save(data);
                             }
                         }
                         catch (Exception e) {
                             e.printStackTrace();
                         }
                         break;
                     case 7:
                         
 //                        org.amanzi.awe.gpeh.parser.internal.GPEHParser.pareseFooter(bitStream, result);
                         break;
                     case 6: 
 //                        org.amanzi.awe.gpeh.parser.internal.GPEHParser.pareseError(bitStream, result);
                         break;
                     case 0:
                         break;
                     case 1:
                         break;
                     case 2:
                         break;
                     case 8: 
                         break;
                     default:
                         // wrong file format!
                         //throw new IllegalArgumentException("Incorrect value: " + recordType);
                         break;
                     }
                     int processedSize = bitStream.getTotalBytesRead();
                     double persentage = (double)processedSize / fileSize;
                     if (persentage - persentageOld > PERCENTAGE_FIRE) {
                         persentageOld = persentage;
                         if (fireSubProgressEvent(element, new ProgressEventImpl(String.format(getDescriptionFormat(), element
                                 .getFile().getName()), persentage))) {
                             return true;
                         }
                     }
 
                 } catch (IOException e) {
                     return false;
                 }
             }
 
         } catch (IOException e) {
             // TODO: handle
             e.printStackTrace();
         } finally {
             closeStream(inputStream);
         }
         return false;
     }
 
     /**
      * Close stream.
      * 
      * @param inputStream the input stream
      */
     protected void closeStream(Closeable inputStream) {
         if (inputStream != null) {
             try {
                 inputStream.close();
             } catch (IOException e) {
                 e.printStackTrace(getPrintStream());
             }
         }
     }
 
     /**
      * Gets the file size for *.gz files
      * 
      * @param file the file
      * @return the file size
      * @throws IOException Signals that an I/O exception has occurred.
      */
     private static long getFileSize(File file) throws IOException {
         RandomAccessFile raf = new RandomAccessFile(file, "r");
         raf.seek(raf.length() - 4);
         int b4 = raf.read();
         int b3 = raf.read();
         int b2 = raf.read();
         int b1 = raf.read();
         int val = (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
         raf.close();
 
         return val;
     }
 
     @Override
     protected GpehTransferData getInitData(CommonConfigData properties) {
         GpehTransferData data = new GpehTransferData();
         data.put(GpehTransferData.PROJECT, properties.getProjectName());
         data.put(GpehTransferData.DATASET, properties.getDbRootName());
         String outputDirectory = (String)properties.getAdditionalProperties().get(GpehTransferData.OUTPUT_DIRECTORY);
         if (outputDirectory != null) {
             data.put(GpehTransferData.OUTPUT_DIRECTORY, outputDirectory);
         }
         return data;
     }
 }
