 /*
  *  OpenSDI Manager
  *  Copyright (C) 2013 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package it.geosolutions.opensdi.utils;
 
 import it.geosolutions.opensdi.dto.GeobatchRunInfo;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Utilities class for GeoBatchRunInfo operations
  * 
  * @author adiaz
  */
 public class GeoBatchRunInfoUtils {
 
 /**
  * Separator for composite descriptors
  */
 public static final String SEPARATOR = System.getProperty("file.separator");
 
 /**
  * Format for day "dd/MM/yyyy"
  */
 public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
 
 /**
  * Format for time "HH:mm:ss"
  */
 public static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
 
 /**
  * Generate description for a compositeId. Known composite id format it's:
  * (path, fileName, operation)
  * 
  * @param compositeId String array with parameters to be concatenated in one
  *        String separated by {@link GeoBatchRunInfoUtils#SEPARATOR}
  * @return concatenated String. Frequently 'path/fileName/operation'
  */
 public static String generateDecription(String... compositeId) {
     String description = "";
     if (compositeId != null && compositeId.length > 0) {
         description += compositeId[0];
         for (int i = 1; i < compositeId.length; i++) {
            if (!description.endsWith(SEPARATOR)) {
                description += SEPARATOR;
            }
            description += compositeId[i];
         }
     }
     return description;
 }
 
 /**
  * Obtain composite identifier from a description
  * 
  * @param description with composite id joined (Frequently
  *        'path/fileName/operation')
  * @return composite identifier. With the known format return: {path, fileName,
  *         operation}, otherwise return <code>description</code> splitted by
  *         {@link GeoBatchRunInfoUtils#SEPARATOR}
  */
 public static String[] getCompositeId(String description) {
     String[] compositeId = null;
     if (description != null) {
         String[] splitted = description.split(SEPARATOR);
         if (splitted.length > 3) {
             // Common compositeId it's {path, fileName, operation}
             compositeId = new String[3];
             int i;
             compositeId[0] = new String();
             for (i = 0; i < splitted.length - 2; i++) {
                 compositeId[0] += splitted[i] + SEPARATOR;
             }
             compositeId[1] = splitted[i++];
             compositeId[2] = splitted[i++];
         } else {
             // if have less than 3, it isn't known format, return the parse
             compositeId = splitted;
         }
     }
 
     return compositeId;
 }
 
 /**
  * Obtain a String representation of a Date
  * 
  * @param origin date
  * @return time with {@link GeoBatchRunInfoUtils#TIME_FORMAT} if origin is in
  *         today or date with {@link GeoBatchRunInfoUtils#DATE_FORMAT} otherwise
  */
 public static String getDate(Date origin) {
     Date today = new Date();
     String result = null;
     if (origin != null) {
         result = DATE_FORMAT.format(origin);
         if (result.equals(DATE_FORMAT.format(today))) {
             // It's today, we show time!!
             result = TIME_FORMAT.format(origin);
         }
     }
     return result;
 }
 
 /**
  * Obtain file name of a run information
  * 
  * @param runInfo
  * @param removeExtension flag to remove the extension
  * @return second component of the id if <code>runInformation.compositeId</code>
  *         ({path, fileName, operation}') or
  *         <code>runInformation.internalUid</code> ('path/fileName/operation')
  *         have known format
  */
 public static String getFileName(GeobatchRunInfo runInfo,
         boolean removeExtension) {
     String fileName = null;
     if (runInfo != null) {
         String[] compositeId = runInfo.getCompositeId();
         if (runInfo.getCompositeId() == null
                 || runInfo.getCompositeId().length < 2
                 && runInfo.getInternalUid() != null) {
             // try to generate from the internal uid
             compositeId = getCompositeId(runInfo.getInternalUid());
         }
         if (compositeId != null) {
             // Known format: fileName it's the second component of the
             // compositeId
             fileName = compositeId[1];
         }
     }
 
     // we need to remove the extension
     if (fileName != null && removeExtension && fileName.contains(".")) {
         fileName = fileName.substring(0, fileName.lastIndexOf("."));
     }
 
     return fileName;
 }
 
 /**
  * Obtain run time directory
  * 
  * @return default directory if userName it's null or user directory otherwise
  */
 public static String getRunDir(String defaultDirectory, String userName) {
     String dir = null;
     // Add separator if needed
     if (defaultDirectory != null
             && defaultDirectory.lastIndexOf(SEPARATOR) < defaultDirectory
                     .length()) {
         defaultDirectory += SEPARATOR;
     }
     // get user directory
     if (userName != null) {
         dir = defaultDirectory + userName + SEPARATOR;
         File checkDir = new File(dir);
         if (!checkDir.exists()) {
             // create
             checkDir.mkdir();
         }
     } else {
         dir = defaultDirectory;
     }
     return dir;
 
 }
 
 }
