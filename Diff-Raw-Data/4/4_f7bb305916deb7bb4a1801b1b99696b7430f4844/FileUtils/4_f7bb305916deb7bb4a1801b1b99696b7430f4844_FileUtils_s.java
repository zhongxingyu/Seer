 /*
  * Copyright © 2012-2013 Visual Illusions Entertainment.
  *  
  * This file is part of VIUtils.
  *
  * VIUtils is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * VIUtils is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with VIUtils.
  * If not, see http://www.gnu.org/licenses/lgpl.html
  */
 package net.visualillusionsent.utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 /**
  * Provides static methods to help with {@link File} manipulations
  * 
  * @since 1.0
  * @version 1.0
  * @author Jason (darkdiplomat)
  */
 public final class FileUtils {
     private static int BUFFER = 512;
 
     /**
      * Removes a line from a {@link File}
      * 
      * @param filepath
      *            the path to the {@link File} to have a line removed
      * @param line
      *            the line to be removed
      * @throws UtilityException
      * <br>
      *             if filepath is null or empty
      * @see #removeLines(File, String...)
      */
     public static final void removeLine(String filepath, String line) throws UtilityException {
         if (filepath == null) {
             throw new UtilityException("arg.null", "String filepath");
         }
         else if (filepath.trim().isEmpty()) {
             throw new UtilityException("arg.empty", "String filepath");
         }
         removeLines(new File(filepath), line);
     }
 
     /**
      * Removes a line from a {@link File}
      * 
      * @param filepath
      *            the path to the {@link File} to have a line removed
      * @param lines
      *            the lines to be removed
      * @throws UtilityException
      * <br>
      *             if filepath is null or empty
      * @see #removeLines(File, String...)
      */
     public static final void removeLines(String filepath, String... lines) throws UtilityException {
         if (filepath == null) {
             throw new UtilityException("arg.null", "String filepath");
         }
         else if (filepath.trim().isEmpty()) {
             throw new UtilityException("arg.empty", "String filepath");
         }
         removeLines(new File(filepath), lines);
     }
 
     /**
      * Removes a line from a {@link File}
      * 
      * @param file
      *            the {@link File} to have a line removed
      * @param line
      *            the line to be removed
      * @throws UtilityException
      * @see #removeLines(File, String...)
      */
     public static final void removeLine(File file, String line) throws UtilityException {
         removeLines(file, line);
     }
 
     /**
      * Removes a line from a {@link File}
      * 
      * @param file
      *            the {@link File} to have lines removed
      * @param lines
      *            the lines to be removed
      * @throws UtilityException
      * <br>
      *             if the {@link File} is null or does not exist<br>
      *             if the {@link File} can not be read<br>
      *             if the {@link File} can not be written to<br>
      *             if lines are null or an empty array<br>
      *             if some other IOException occurrs
      */
     public static final void removeLines(File file, String... lines) throws UtilityException {
         if (file == null) {
             throw new UtilityException("arg.null", "File file");
         }
         else if (lines == null) {
             throw new UtilityException("arg.null", "String... lines");
         }
         else if (lines.length < 1) {
             throw new UtilityException("arg.empty", "String... lines");
         }
         else if (!file.isFile()) {
             throw new UtilityException("file.err.exist", file.getName());
         }
         else if (!file.canRead()) {
             throw new UtilityException("file.err.read", file.getName());
         }
         else if (!file.canWrite()) {
             throw new UtilityException("file.err.write", file.getName());
         }
 
         UtilityException ue = null;
         BufferedReader breader = null;
         PrintWriter pwriter = null;
 
         try {
             List<String> listlines = Arrays.asList(lines);
             List<String> outLines = new ArrayList<String>();
 
             breader = new BufferedReader(new FileReader(file));
 
             String inLine = null;
             while ((inLine = breader.readLine()) != null) {
                 if (!listlines.contains(inLine)) {
                     outLines.add(inLine);
                 }
             }
 
             pwriter = new PrintWriter(new FileWriter(file, false));
             for (String outLine : outLines) {
                 pwriter.println(outLine);
                 pwriter.flush();
             }
 
         }
         catch (IOException ex) {
             ue = new UtilityException("file.ioe", file.getName());
         }
         finally {
             pwriter.close();
             try {
                 breader.close();
             }
             catch (IOException e) {}
             if (ue != null) {
                 throw ue;
             }
         }
     }
 
     /**
      * Clones a {@link File}
      * 
      * @param toClone
      *            the filepath to the {@link File} to clone
      * @param clone
      *            the filepath to clone to
      * @throws UtilityException
      * <br>
      *             if toClone is null or empty<br>
      *             if clone is null or empty<br>
      * @see #cloneFile(File, File)
      */
     public static final void cloneFile(String toClone, String clone) throws UtilityException {
         if (toClone == null) {
             throw new UtilityException("arg.null", "String toClone");
         }
         else if (toClone.trim().isEmpty()) {
             throw new UtilityException("arg.empty", "String toClone");
         }
         else if (clone == null) {
             throw new UtilityException("arg.null", "String clone");
         }
         else if (clone.trim().isEmpty()) {
             throw new UtilityException("arg.empty", "String clone");
         }
         else if (toClone.equals(clone)) {
             throw new UtilityException("toClone equals clone");
         }
         cloneFile(new File(toClone), clone);
     }
 
     /**
      * Clones a {@link File}
      * 
      * @param toClone
      *            the {@link File} to clone
      * @param clone
      *            the filepath to clone to
      * @throws UtilityException
      * <br>
      *             if toClone is null<br>
      *             if clone is null or empty
      * @see #cloneFile(File, File)
      */
     public static final void cloneFile(File toClone, String clone) throws UtilityException {
         if (toClone == null) {
             throw new UtilityException("arg.null", "File toClone");
         }
         else if (clone == null) {
             throw new UtilityException("arg.null", "String clone");
         }
         else if (clone.trim().isEmpty()) {
             throw new UtilityException("arg.empty", "String clone");
         }
         cloneFile(toClone, new File(clone));
     }
 
     /**
      * Clones a {@link File}
      * 
      * @param toClone
      *            the {@link File} to clone
      * @param clone
      *            the {@link File} to clone to
      * @throws UtilityException
      * <br>
      *             if toClone is null, does not exist, is not a file, or can not be read<br>
      *             if clone is null, path matches toClone, or is a directory<br>
      *             if some other IOException occurrs
      */
     public static final void cloneFile(File toClone, File clone) throws UtilityException {
         if (toClone == null) {
             throw new UtilityException("arg.null", "File file");
         }
         else if (clone == null) {
             throw new UtilityException("arg.null", "File clone");
         }
         else if (clone.isDirectory()) {
             throw new UtilityException("file.err.dir", clone.getName());
         }
         else if (toClone.getPath().equals(clone.getPath())) {
             throw new UtilityException("file.err.path", toClone.getName(), clone.getName());
         }
         else if (!toClone.isFile()) {
             throw new UtilityException("file.err.exist", toClone.getName());
         }
         else if (!toClone.canRead()) {
             throw new UtilityException("file.err.read", toClone.getName());
         }
 
         UtilityException ue = null;
         FileInputStream instream = null;
         FileOutputStream outstream = null;
 
         try {
             instream = new FileInputStream(toClone);
             outstream = new FileOutputStream(clone);
 
             int inByte;
             while ((inByte = instream.read()) != -1) {
                 outstream.write(inByte);
                 outstream.flush();
             }
         }
         catch (IOException ex) {
             ue = new UtilityException("file.err.ioe", clone.getName());
         }
         finally {
             try {
                 instream.close();
                 outstream.close();
             }
             catch (IOException e) {}
             if (ue != null) {
                 throw ue;
             }
         }
     }
 
     /**
      * Clones a {@link File} from a {@link JarFile}
      * 
      * @param jarPath
      *            the path to the {@link JarFile}
      * @param fileToMove
      *            the file to be cloned (ie: resources/README.txt)
     * @oaram pathTo
     *        the path to clone the file to
      * @throws UtilityException
      * <br>
      *             if an IOException occurrs
      */
     public static final void cloneFileFromJar(String jarPath, String fileToMove, String pathTo) throws UtilityException {
         JarFile jar = null;
         FileOutputStream out = null;
         UtilityException toThrow = null;
         try {
             jar = new JarFile(jarPath);
             JarEntry entry = jar.getJarEntry(fileToMove);
             InputStream in = jar.getInputStream(entry);
             out = new FileOutputStream(pathTo);
             byte[] buffer = new byte[BUFFER];
             int readBytes = 0;
             while ((readBytes = in.read(buffer, 0, buffer.length)) != -1) {
                 out.write(buffer, 0, readBytes);
             }
         }
         catch (IOException e) {
             toThrow = new UtilityException("Exception occured while moving file from Jar...", e);
         }
         finally {
             try {
                 if (out != null) {
                     out.close();
                 }
                 if (jar != null) {
                     jar.close();
                 }
             }
             catch (IOException e) {}
             if (toThrow != null) {
                 throw toThrow;
             }
         }
     }
 }
