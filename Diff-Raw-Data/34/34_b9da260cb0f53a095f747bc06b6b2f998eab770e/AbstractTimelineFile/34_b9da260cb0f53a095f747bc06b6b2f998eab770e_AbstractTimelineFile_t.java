 /*
  * Copyright 2010 Kazuyoshi Aizawa
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.cafeform.iumfs.twitterfs.files;
 
 import com.cafeform.iumfs.Files;
 import com.cafeform.iumfs.IumfsFile;
 import com.cafeform.iumfs.NotADirectoryException;
 import com.cafeform.iumfs.NotSupportedException;
 import com.cafeform.iumfs.twitterfs.Account;
 import com.cafeform.iumfs.twitterfs.Prefs;
 import com.cafeform.iumfs.twitterfs.SerializableList;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import twitter4j.Status;
 
 /**
  * Represents fundamental TimelineFile class.
  * All other timeline classes are descendant of this class
  * 
  *  AbstractTimelineFile
  *     +StreamTimelineFile
  *     +AbstractNormalTimelineFile 
  *         +DefaultTimelineFile
  *         +UserTimelineFIle
  * 
  * @author kaizawa
  */
 abstract public class AbstractTimelineFile extends TwitterFsFileImpl
 {
     protected static final String CONT = "(cont) ";
     protected long lastId = 1;
     protected long baseId = 0;
     protected List<Status> statusList;
     protected static final int MAX_STATUSES = Prefs.getInt("maxStatuses");
     protected boolean initialRead = true;
     static protected final int MAX_PAGES = Prefs.getInt("maxPages");
 
     public static final String BACKUP_DIR = "BackupDirectory";    
     public static final String DEFAULT_BACKUP_DIR = "/var/tmp/twitterfs";
     public static final String BACKUP_ENABLED = "UseBackup";
     public static final String DEAULT_BACKUP_ENABLED = "true";
     
 
     /**
      * Constractor for twitter file
      *
      * @param account Twitter account
      * @param pathname
      */
     public AbstractTimelineFile(
             Account account,
             String pathname)
     {
         super(account, pathname);
         this.isTimeline = true;
        String backupDir = System.getProperty(
                BACKUP_DIR, 
                DEFAULT_BACKUP_DIR);
        String backupEnabled = System.getProperty(
                BACKUP_ENABLED, 
                DEAULT_BACKUP_ENABLED);
         String backupFile = backupDir + "/" + account.getUsername() + "-" + 
                 Files.getNameFromPathName(pathname);
         try
         {
            statusList = new SerializableList<>(
                    backupFile,
                    Boolean.parseBoolean(backupEnabled));
         } 
         catch (IOException ex)
         {
             throw new IllegalStateException("Cannot create backup file "
                     + backupFile, ex);
         }
        if (Boolean.parseBoolean(backupEnabled)) 
        {
            int fileSize = 0;
            // Update file size from list
            for (Status status : statusList) 
            {
                try
                {
                    fileSize += statusToFormattedString(status).
                            getBytes("UTF-8").length;
                } catch (UnsupportedEncodingException ex)
                {
                    logger.log(Level.SEVERE, "Cannot decode string in timeline", ex);
                }
            }
            setLength(fileSize);
        }       
     }
 
     /**
      * Get Status data of requested bytes at requested offset. The base of
      * offset statts from oldest status.
      *
      * Procect list by synchronizing, not to read when time line is updated.
      *
      * TODO: CopyOnWriteArrayList would be best, but it doesn't support
      * sorting.. TODO: In current implementation, calculation of offset is very
      * costly. Should I store status as simple text data?
      *
      * @param buf
      * @param size
      * @param offset
      * @return
      */
     @Override
     public synchronized long read(ByteBuffer buf, long size, long offset)
     {
         long curr_size = 0;
         long curr_offset = 0;
         long prev_offset = 0;
         long rel_offset; // relative offset within each status 
         int page = 0;
 
         /*
          * OLD                                                         NEW
          * Status             |Status                      |Status     
          * --------offset------------------->|<----------size------------->
          *                     
          * ----prev_offset--->|
          *                    |<-rel_offset->|
          *                    
          * ------------------curr_offset------------------>|
          *                                   |<-copy_size->|
          */
         for (Status status : statusList)
         {
             try
             {
                 prev_offset = curr_offset;
                 String text = statusToFormattedString(status);
                 byte[] bytes = text.getBytes("UTF-8");
                 long copy_size = 0;
                 long status_length = bytes.length;
                 rel_offset = 0;
 
                 logger.finer("Read status_list id=" + status.getId()
                         + ", status_length=" + status_length);
 
                 logger.finest(text);
 
                 /*
                  * Calculate current offset by adding number of characters to 
                  * previous offset.
                  */
                 curr_offset += status_length;
                 logger.finer("curr_off=" + curr_offset);
                 if (curr_offset < offset)
                 {
                     logger.finer("offset not yet reached");
                     continue;
                 } else if (prev_offset >= offset)
                 {
                     logger.finer("prev_offset >= offset");
                     /*
                      * Already exceed offset. set relative offset to 0.
                      */
                     rel_offset = 0;
                 } else
                 {
                     logger.finer("prev_offset < offset");
                     /*
                      * offset is within this status.
                      * calculate relative offset.
                      */
                     rel_offset = offset - prev_offset;
                 }
                 logger.finer("rel_offset = " + rel_offset);
 
                 if (curr_size + status_length >= size)
                 {
                     /*
                      * status size is larger or equal to requested size.
                      * Copy necessary data size and won't read any more. 
                      */
                     copy_size = size - curr_size - rel_offset;
                     logger.finer("copy_Size = " + copy_size
                             + ". No need to read more Status");
                 } else
                 {
                     /*
                      * need more data... copy all status data to buffer.
                      */
                     copy_size = bytes.length - rel_offset;
                     logger.finer("copy_Size = " + copy_size + ". need to read more Status");
                 }
                 /*
                  * write to buffer
                  */
                 buf.put(bytes, (int) rel_offset, (int) copy_size);
                 curr_size += copy_size;
                 if (curr_size >= size)
                 {
                     logger.finer("currSize >= size");
                     break;
                 }
                 logger.finer("curr_size < size. continue for statement.");
             } catch (UnsupportedEncodingException ex)
             {
                 logger.log(Level.INFO, "Cannot decode text in timeline.", ex);
             }
         }
         logger.fine("curr_size = " + curr_size);
         return curr_size;
     }
 
     /**
      * Convert Status to formated text.
      *
      * @param status Status
      * @return formatted text
      */
     public static String statusToFormattedString(Status status)
     {
         // Add user name time..
         StringBuilder sb = new StringBuilder();
         Date createdDate = status.getCreatedAt();
         SimpleDateFormat simpleFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
 
         sb.append(simpleFormat.format(createdDate));
         sb.append(" ");
         sb.append(status.getUser().getScreenName());
         sb.append("[");
         sb.append(status.getUser().getName());
         sb.append("] \n");
         sb.append(status.getText());
         sb.append("\n\n");
         return sb.toString();
     }
 
     synchronized public void addStatusToList(Status status)
     {
         try
         {
             logger.finer("Read Status id=" + status.getId());
             logger.finest(statusToFormattedString(status));
             setLength(length()
                     + statusToFormattedString(status).getBytes("UTF-8").length);
             statusList.add(status);
             lastId = status.getId();
 
             logger.fine("new file_size is " + length());
              // Timelie is update. So changed mtime and ctime
             Date now = new Date();
             setMtime(now.getTime());
             setCtime(now.getTime());
         } catch (UnsupportedEncodingException ex)
         {
             logger.log(Level.SEVERE, "Cannot decode string in timeline", ex);
         }
     }
 
     @Override
     public long write(byte[] buf, long size, long offset)
     {
         throw new NotSupportedException();
     }
 
     @Override
     public long getPermission()
     {
         return (long) 0100444; // regular file      
     }
 
     @Override
     public String getUsername()
     {
         return account.getUsername();
     }
 
     @Override
     public void addFile(IumfsFile file) throws NotADirectoryException
     {
         throw new NotADirectoryException();
     }
 
     @Override
     public IumfsFile[] listFiles()
     {
         throw new NotADirectoryException();
     }
 }
