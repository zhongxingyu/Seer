 /*
  * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystems.core.util;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.error.OSSMultiException;
 
 /**
  * Collection of methods to make work with files easier.
  * 
  * @author bastafidli
  */
public final class FileUtils
 {
    // Configuration settings ///////////////////////////////////////////////////
    
    /**
     * Specifies what directory should be used to store generated and other 
     * temporary files and directories. This directory should be different from 
     * the regular operating system temporary directory since some of the 
     * subsystems may want to delete all content from this directory during 
     * restart.
     */
    public static final String TEMPORARY_DIRECTORY_PATH = "oss.file.path.temp";
    
    // Constants ////////////////////////////////////////////////////////////////
    
    /**
     * The default directory where temporary files should be created. 
     */
    public static final String TEMPORARY_DIRECTORY_PATH_DEFAULT 
                                  = GlobalConstants.getTempDirectory() +  "osstemp";
    
    /**
     * Default 10 digit file storage distribution array. This means that if I 
     * want to name file as 10 digit number e.g. number 123 as 0000000123 or 
     * number 123456789 as 01234567890. Then the path constructed from number 
     * 1234567890 using distribution 2/2/2/4 would be 12/34/56/0123456789 
     */
    public static final int[] DEFAULT_STRORAGE_TREE_DISTRIBUTION = {2, 2, 2, 4};
    
    /**
     * How big buffer to use to process files.
     */
    public static final int BUFFER_SIZE = 65536;
    
    // Cached values ////////////////////////////////////////////////////////////
 
    /**
     * Logger for this class
     */
    private static Logger s_logger = Log.getInstance(FileUtils.class);
    
    // Constructors /////////////////////////////////////////////////////////////
    
    /** 
     * Private constructor since this class cannot be instantiated
     */
    private FileUtils(
    )
    {
       // Do nothing
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Move file to a new location. If the destination is on different volume,
     * this file will be copied and then original file will be deleted.
     * If the destination already exists, this method renames it with different
     * name and leaves it in that directory and moves the new file along side 
     * the renamed one.
     * 
     * @param flCurrent - file to move
     * @param strNewName - new location including file name
     * @throws IOException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    public static void moveFile(
       File   flCurrent, 
       String strNewName
    ) throws IOException,
             OSSException
    {
       // Next check if the destination file exists
       File flDestination;
          
       flDestination = new File(strNewName);
       FileUtils.moveFile(flCurrent, flDestination);
    }
    
    /**
     * Move file to a new location. If the destination is on different volume,
     * this file will be copied and then original file will be deleted.
     * If the destination already exists, this method renames it with different
     * name and leaves it in that directory and moves the new file along side 
     * the renamed one.
     * 
     * @param flCurrent - file to move
     * @param flDestination - destination file
     * @throws IOException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    public static void moveFile(
       File flCurrent, 
       File flDestination
    ) throws IOException,
             OSSException
    {
       // Make sure that the source exist, it might be already moved from 
       // a directory and we just don't know about it
       if (flCurrent.exists())
       {
          // Next check if the destination file exists
          if (flDestination.exists())
          {
             // If the destination exists, that means something went wrong
             // Rename the destination file under temporaty name and try to  
             // move the new file instead of it
             s_logger.log(Level.WARNING, "Destination file {0} exists. Renaming"
                          + " as duplicate.", flDestination.getAbsolutePath());
             renameToTemporaryName(flDestination, "old");
          }
       
          // Make sure the directory exists and if not create it
          File flFolder;
          
          flFolder = flDestination.getParentFile();
          if ((flFolder != null) && (!flFolder.exists()))
          {
             if (!flFolder.mkdirs())
             {
                // Do not throw the exception if the directory already exists
                // because it was created meanwhile for example by a different 
                // thread
                if (!flFolder.exists())
                {
                   throw new IOException("Cannot create directory " + flFolder);
                }
             }
          }
          
          // Now everything should exist so try to rename the file first
          // After testing, this renames files even between volumes C to H 
          // so we don't have to do anything else on Windows but we still
          // have to handle erro on Unix 
          if (!flCurrent.renameTo(flDestination))
          {
             // Try to copy and delete since the rename doesn't work on Solaris
             // between file systems
             copyFile(flCurrent, flDestination);
             
             // Now delete the file
             if (!flCurrent.delete())
             {
                // Delete the destination file first since we haven't really moved
                // the file
                flDestination.delete();
                throw new IOException("Cannot delete already copied file " + flCurrent);
             }
          }
       }   
    }
  
    /**
     * Copy the current file to the destination file.
     * 
     * @param flCurrent - source file
     * @param flDestination - destination file
     * @throws IOException - an error has occurred
     * @throws OSSException - an error has occurred
     */  
    public static void copyFile(
       File flCurrent,
       File flDestination
    ) throws IOException,
             OSSException
    {
      // Make sure the directory exists and if not create it
      File flFolder;
      
      flFolder = flDestination.getParentFile();
      if ((flFolder != null) && (!flFolder.exists()))
      {
         if (!flFolder.mkdirs())
         {
            // Do not throw the exception if the directory already exists
            // because it was created meanwhile for example by a different 
            // thread
            if (!flFolder.exists())
            {
               throw new IOException("Cannot create directory " + flFolder);
            }
         }
      }
 
       // FileChannel srcChannel = null;
       // FileChannel dstChannel = null;
       FileInputStream finInput = null;
       
       //MHALAS: This code is not working reliably on Solaris 8 with 1.4.1_01
       // Getting exceptions from native code
       /*
       // Create channel on the source
       srcChannel = new FileInputStream(flCurrent).getChannel();
       // Create channel on the destination
       dstChannel = new FileOutputStream(flDestination).getChannel();
  
       // Copy file contents from source to destination
       dstChannel.transferFrom(srcChannel, 0, srcChannel.size());   
          
       Don't forget to close the channels if you enable this code again
       */
       try
       {
          finInput = new FileInputStream(flCurrent);
       }
       catch (IOException ioExec)
       {
          if (finInput != null)
          {
             try
             {
                finInput.close();
             }
             catch (Throwable thr)
             {
                throw new OSSMultiException(ioExec, thr);
             }
          }
          throw ioExec;
       }
 
       FileUtils.copyStreamToFile(finInput, flDestination);
    }
    
    /**
     * Rename the file to temporary name with given prefix
     * 
     * @param flFileToRename - file to rename
     * @param strPrefix - prefix to use
     * @throws IOException - an error has occurred
     */
    public static void renameToTemporaryName(
       File   flFileToRename,
       String strPrefix
    ) throws IOException
    {
       assert strPrefix != null : "Prefix cannot be null.";
       
       String        strParent;
       StringBuilder sbBuffer = new StringBuilder();
       File          flTemp;
       int           iIndex = 0;
       
       strParent = flFileToRename.getParent();
 
       // Generate new name for the file in a deterministic way
       do
       {
          iIndex++;
          sbBuffer.delete(0, sbBuffer.length());
          if (strParent != null) 
          {
             sbBuffer.append(strParent);
             sbBuffer.append(File.separatorChar);
          }
          
          sbBuffer.append(strPrefix);
          sbBuffer.append("_");
          sbBuffer.append(iIndex);
          sbBuffer.append("_");
          sbBuffer.append(flFileToRename.getName());
                
          flTemp = new File(sbBuffer.toString());
       }      
       while (flTemp.exists());
       
       // Now we should have unique name
       if (!flFileToRename.renameTo(flTemp))
       {
          throw new IOException("Cannot rename " + flFileToRename.getAbsolutePath()
                                + " to " + flTemp.getAbsolutePath());
       }
    }
 
    /** 
     * Delete all files and directories in directory but do not delete the
     * directory itself.
     * 
     * @param strDir - string that specifies directory to delete
     * @return boolean - true if the operation was successful false otherwise
     */
    public static boolean deleteDirectoryContent(
       String strDir
    )
    {
       return ((strDir != null) && (strDir.length() > 0)) 
               ? deleteDirectoryContent(new File(strDir)) : false;
    }
 
    /** 
     * Delete all files and directories in directory but do not delete the
     * directory itself.
     * 
     * @param fDir - directory to delete
     * @return boolean - true if the operation was successful false otherwise
     */
    public static boolean deleteDirectoryContent(
       File fDir
    )
    {
       boolean bRetval = false;
 
       if (fDir != null && fDir.isDirectory()) 
       {
          File[] files = fDir.listFiles();
    
          if (files != null)
          {
             bRetval = true;
             boolean dirDeleted;
             
             for (int index = 0; index < files.length; index++)
             {
                if (files[index].isDirectory())
                {
                   // TODO: Performance: Implement this as a queue where you add to
                   // the end and take from the beginning, it will be more efficient
                   // than the recursion
                   dirDeleted = deleteDirectoryContent(files[index]);
                   if (dirDeleted)
                   {
                      bRetval = bRetval && files[index].delete();
                   }
                   else
                   {
                      bRetval = false;
                   }
                }
                else
                {
                   bRetval = bRetval && files[index].delete();
                }
             }
          }
       }
 
       return bRetval;
    }
 
    /**
     * Deletes all files and subdirectories under the specified directory 
     * including the specified directory
     * 
     * @param strDir - string that specifies directory to be deleted
     * @return boolean - true if directory was successfully deleted
     */
    public static boolean deleteDir(
       String strDir
    ) 
    {
       return ((strDir != null) && (strDir.length() > 0)) 
                 ? deleteDir(new File(strDir)) : false;
    }
    
    /**
     * Deletes all files and subdirectories under the specified directory 
     * including the specified directory
     * 
     * @param fDir - directory to be deleted
     * @return boolean - true if directory was successfully deleted
     */
    public static boolean deleteDir(
       File fDir
    ) 
    {
       boolean bRetval = false;
       if (fDir != null && fDir.exists())
       {
          bRetval = deleteDirectoryContent(fDir);
          if (bRetval)
          {
             bRetval = bRetval && fDir.delete();         
          }
       }
       return bRetval;
    }
    
    /**
     * Compare binary files. Both files must be files (not directories) and exist.
     * 
     * @param first  - first file
     * @param second - second file
     * @return boolean - true if files are binary equal
     * @throws IOException - an error has occurred
     */
    public static boolean isFileBinaryEqual(
       File first,
       File second
    ) throws IOException
    {
       // TODO: Test: Missing test
       boolean bReturn = false;
       
       if ((first.exists()) && (second.exists()) 
          && (first.isFile()) && (second.isFile()))
       {
          if (first.getCanonicalPath().equals(second.getCanonicalPath()))
          {
             bReturn = true;
          }
          else
          {
             FileInputStream firstInput;
             FileInputStream secondInput;
             BufferedInputStream bufFirstInput = null;
             BufferedInputStream bufSecondInput = null;
 
             try
             {            
                firstInput = new FileInputStream(first); 
                secondInput = new FileInputStream(second);
                bufFirstInput = new BufferedInputStream(firstInput, BUFFER_SIZE); 
                bufSecondInput = new BufferedInputStream(secondInput, BUFFER_SIZE);
    
                int firstByte;
                int secondByte;
                
                while (true)
                {
                   firstByte = bufFirstInput.read();
                   secondByte = bufSecondInput.read();
                   if (firstByte != secondByte)
                   {
                      break;
                   }
                   if ((firstByte < 0) && (secondByte < 0))
                   {
                      bReturn = true;
                      break;
                   }
                }
             }
             finally
             {
                try
                {
                   if (bufFirstInput != null)
                   {
                      bufFirstInput.close();
                   }
                }
                finally
                {
                   if (bufSecondInput != null)
                   {
                      bufSecondInput.close();
                   }
                }
             }
          }
       }
       
       return bReturn;
    }
    
    /**
     * Create list of files in directory. Members if list are File structures.
     * 
     * @param directory  - File with directory
     * @param maximum    - maximum size of output list
     * @param olderThan  - file last modification time have to be before this date
     * @return List - list of File objects
     */
   public static List listFiles(
       File directory, 
       int  maximum,
       Date olderThan
    )
    {
      List lRetval = null;
       if (directory != null && directory.isDirectory())
       {
          File[] files;
          // TODO: Improve: I don't think this works, since the listFiles always
          // returns all files. We can throw an exception and then catch it here
          // and just let the filter remember the files which accepted to limit
          // the amount of data searched
          files = directory.listFiles(new MaximumFileFilter(maximum, olderThan));
          
          if (files != null)
          {
             lRetval = Arrays.asList(files);
          }
       }      
       return lRetval;
    }
    
    /**
     * Function converts parameters to full path and name in tree file storage
     * using the default tree schema. For example id 123456.tif can be converted 
     * to 00\12\34\00123456.tif
     * 
     * @param lID              - id (decimal number) part of file name
     * @param rootDirPath      - root directory for file storage tree
     * @param idExtension      - extension after id part of name and before file extension
     *                           can be empty or null
     * @param fileExtension    - file extension - with dot
     * @return String          - string with path and name for file 
     */
    public static String getFileStoragePath(
       long   lID,
       String rootDirPath,
       String idExtension,
       String fileExtension
    )
    {
       return getFileStoragePath(lID, rootDirPath, idExtension, fileExtension, 
                                 DEFAULT_STRORAGE_TREE_DISTRIBUTION);
    }
    
    /**
     * Function converts parameters to full path and name in tree file storage.
     * For example as id 123456.tif can be converted to 00\12\34\00123456.tif
     * 
     * @param lID              - id (decimal number) part of file name
     * @param rootDirPath      - root directory for file storage tree path. 
     *                           It has to end with separator char
     * @param idExtension      - extension after id part of name and before file 
     *                           extension can be empty or null
     * @param fileExtension    - file extension - with dot
     * @param treeDistribution - tree distribution array. arrai of int > 0. 
     * @return String          - string with path and name for file 
     */
    public static String getFileStoragePath(
       long   lID,
       String rootDirPath,
       String idExtension,
       String fileExtension,
       int[]  treeDistribution
    )
    {
       int           iDigit = 0;
       StringBuilder idString = new StringBuilder(Long.toString(lID));
       StringBuilder retval = new StringBuilder(rootDirPath);
       
       for (int index = 0; index < treeDistribution.length; index++)
       {
          if (GlobalConstants.ERROR_CHECKING)
          {
             assert treeDistribution[index] > 0 
                    : "value <= 0 in tree distribution array";
          }
          iDigit += treeDistribution[index];
       }
 
       if (GlobalConstants.ERROR_CHECKING)
       {
          assert idString.length() <= iDigit : "ID to big for three capacity";
       }
       
       // append leading 0 characters
       idString.insert(0, NumberUtils.ZEROCHARS, 0, iDigit - idString.length());
       
       // add subdirs to path
       int currentCount = 0;
       // Ignore the last element since thats just there to tell us what should
       // be the total length of the desired file name
       for (int index = 0; index < treeDistribution.length - 1; index++)
       {
          retval.append(idString.subSequence(
                                    currentCount,
                                    currentCount + treeDistribution[index]));
          currentCount += treeDistribution[index];
          retval.append(File.separator);
       }
       
       // add file name part of path - complete id with leading 0 chars
       retval.append(idString);
       
       // add id extension if any
       if (idExtension != null && idExtension.length() > 0)
       {
          retval.append(idExtension);
       }
       // add file extension with leading dot
       retval.append(fileExtension);
       
       return retval.toString();
    }
 
    /**
     * Function will create TwoObjectStruct from full file path and name string
     * 
     * @param filePath - file path and name
     * @return TwoObjectStruct - file directory as first member and file name as 
     *                           second member
     */
    public static TwoObjectStruct getFilenameSplit(
       String filePath
    )
    {
       return new TwoObjectStruct(
             filePath.substring(0, filePath.lastIndexOf(File.separator) + 1),
             filePath.substring(filePath.lastIndexOf(File.separator) + 1));
    }
    
    /**
     * Get path which represents temporary directory. It is guarantee that it 
     * ends with \ (or /).
     * 
     * @return String - get the configured temporary path to use
     */
    public static String getTemporaryDirectory(
    )
    {
       // Load the temporary path
       Properties prpSettings;
       String     strTempDirectory;
       
       prpSettings = Config.getInstance().getProperties();
 
       strTempDirectory = PropertyUtils.getStringProperty(
                               prpSettings, TEMPORARY_DIRECTORY_PATH, 
                               TEMPORARY_DIRECTORY_PATH_DEFAULT, 
                               "Directory to store temporary files");
       
       if ((!strTempDirectory.endsWith(File.separator)) 
          // On Windows it is allowed to end with / since java will shield it
          && (GlobalConstants.isWindows() && (!strTempDirectory.endsWith("/"))))
       {
          strTempDirectory += File.separator;
          // Log this as finest since one config log was already printed
          s_logger.log(Level.FINEST,TEMPORARY_DIRECTORY_PATH + " = {0}", 
                       strTempDirectory);
       }
 
       return strTempDirectory;
    }
    
    /**
     * Method constructs and returns repository path. If this constructed path 
     * does not exist, it will be automatically created.
     *
     * @param strPrefix - prefix to use to create directory name 
     * @param strTemporarySubDirID - string value used to create subdirectory 
     *                               of the parent directory
     * @param bUseSession - if true then current session will be user as 
     *                      part of unique path
     * @return String - full path to the directory
     * @throws IOException - an error has occurred 
     */
    public static String createTemporarySubdirectory(
       String strPrefix,
       String strTemporarySubDirID,
       boolean bUseSession 
    ) throws IOException
    {
       StringBuilder sbRepositoryPath = new StringBuilder();
       File          fRepositoryPath;
       
       sbRepositoryPath.append(getTemporaryDirectory()); 
       if (bUseSession)
       {
          sbRepositoryPath.append(CallContext.getInstance().getCurrentSession()); 
          sbRepositoryPath.append(File.separator);
       }
       sbRepositoryPath.append(strPrefix);
       sbRepositoryPath.append(strTemporarySubDirID);
       sbRepositoryPath.append(File.separator);
       
       fRepositoryPath = new File(sbRepositoryPath.toString());
 
       if (!fRepositoryPath.exists())
       {
          // all non-existent ancestor directories are automatically created
          if (!fRepositoryPath.mkdirs())
          {
             // Do not throw the exception if the directory already exists
             // because it was created meanwhile for example by a different 
             // thread
             if (!fRepositoryPath.exists())
             {
                // Directory creation failed
                throw new IOException(
                   "Error occurred during creating temporary directory.");
             }
          }
       }
       else if (bUseSession)
       {
          // Unique directory already exist but it hasn't
          throw new IOException("Cannot create unique temporary directory because" 
                                + " it already exists.");
       }
 
       return sbRepositoryPath.toString();
    }
 
    /**
     * Copy any input stream to output file. Once the data will be copied
     * the stream will be closed.
     * 
     * @param input  - InputStream to copy from
     * @param output - File to copy to
     * @throws IOException - an error has occurred
     * @throws OSSMultiException - an error has occurred
     */
    public static void copyStreamToFile(
       InputStream input,
       File        output
    ) throws IOException, 
             OSSMultiException
    {
       FileOutputStream foutOutput = null;
 
       // open input file as stream safe - it can throw some IOException
       try
       {
          foutOutput = new FileOutputStream(output);
       }
       catch (IOException ioExec)
       {
          if (foutOutput != null)
          {
             try
             {
                foutOutput.close();
             }
             catch (IOException ioExec2)
             {
                throw new OSSMultiException(ioExec, ioExec2);
             }
          }            
          
          throw ioExec;
       }
 
       // all streams including os are closed in copyStreamToStream function 
       // in any case
       FileUtils.copyStreamToStream(input, foutOutput);
    }
 
    /**
     * Copy any input stream to output stream. Once the data will be copied
     * both streams will be closed.
     * 
     * @param input  - InputStream to copy from
     * @param output - OutputStream to copy to
     * @throws IOException - an error has occurred
     * @throws OSSMultiException - an error has occurred
     */
    public static void copyStreamToStream(
       InputStream input,
       OutputStream output
    ) throws IOException, 
             OSSMultiException
    {
       InputStream is = null;
       OutputStream os = null;
       int                 ch;
 
       try
       {
          if (input instanceof BufferedInputStream)
          {
             is = input;
          }
          else
          {
             is = new BufferedInputStream(input);
          }
          if (output instanceof BufferedOutputStream)
          {
             os = output;
          }
          else
          {
             os = new BufferedOutputStream(output);
          }
    
          while ((ch = is.read()) != -1)
          {
             os.write(ch);
          }
          os.flush();
       }
       finally
       {
          IOException exec1 = null;
          IOException exec2 = null;
          try
          {
             // because this close can throw exception we do next close in 
             // finally statement
             if (os != null)
             {
                try
                {
                   os.close();
                }
                catch (IOException exec)
                {
                   exec1 = exec;
                }
             }
          }
          finally
          {
             if (is != null)
             {
                try
                {
                   is.close();
                }
                catch (IOException exec)
                {
                   exec2 = exec;
                }
             }
          }
          if ((exec1 != null) && (exec2 != null))
          {
             throw new OSSMultiException(exec1, exec2);
          }
          else if (exec1 != null)
          {
             throw exec1;
          }
          else if (exec2 != null)
          {
             throw exec2;
          }
       }
    }
 }
