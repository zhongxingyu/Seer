 /*
  * This file is part of MinecartRevolution-Core.
  * Copyright (c) 2012 QuarterCode <http://www.quartercode.com/>
  *
  * MinecartRevolution-Core is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MinecartRevolution-Core is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MinecartRevolution-Core. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.minecartrevolution.core.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import com.quartercode.minecartrevolution.core.MinecartRevolution;
 import com.quartercode.minecartrevolution.core.exception.MinecartRevolutionException;
 import com.quartercode.quarterbukkit.api.exception.ExceptionHandler;
 
 public class ExtractionUtil {
 
     private static MinecartRevolution minecartRevolution;
 
     public static void setMinecartRevolution(MinecartRevolution minecartRevolution) {
 
         ExtractionUtil.minecartRevolution = minecartRevolution;
     }
 
     public static void extractFromJAR(URL url, File destination) {
 
         InputStream inputStream = null;
         OutputStream outputStream = null;
 
         try {
             destination.getParentFile().mkdirs();
 
             outputStream = new FileOutputStream(destination);
             outputStream.flush();
 
             URLConnection connection = url.openConnection();
             connection.connect();
 
             byte[] tempBuffer = new byte[4096];
 
             inputStream = connection.getInputStream();
             int counter;
             while ( (counter = inputStream.read(tempBuffer)) > 0) {
                 outputStream.write(tempBuffer, 0, counter);
                 outputStream.flush();
             }
         }
         catch (IOException e) {
             ExceptionHandler.exception(new MinecartRevolutionException(minecartRevolution, e, "Failed to extract files from jar"));
         }
         finally {
             if (inputStream != null) {
                 try {
                     inputStream.close();
                 }
                 catch (IOException e) {
                    ExceptionHandler.exception(new MinecartRevolutionException(minecartRevolution, e, "Failed to close input stream after attempting to extract files from jar"));
                 }
             }
 
             if (outputStream != null) {
                 try {
                     outputStream.close();
                 }
                 catch (IOException e) {
                    ExceptionHandler.exception(new MinecartRevolutionException(minecartRevolution, e, "Failed to close otuput stream after attempting to extract files from jar"));
                 }
             }
         }
     }
 
 }
