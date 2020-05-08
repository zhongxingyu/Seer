 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.bulk_import;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import com.treasure_data.bulk_import.prepare_parts.MultiThreadPrepareProcessor;
 import com.treasure_data.bulk_import.prepare_parts.PrepareConfiguration;
 import com.treasure_data.bulk_import.prepare_parts.PrepareProcessor;
 import com.treasure_data.bulk_import.upload_parts.MultiThreadUploadProcessor;
 import com.treasure_data.bulk_import.upload_parts.UploadConfiguration;
 import com.treasure_data.bulk_import.upload_parts.UploadProcessor;
 import com.treasure_data.client.TreasureDataClient;
 import com.treasure_data.client.bulkimport.BulkImportClient;
 
 public class Main {
     private static final Logger LOG = Logger.getLogger(Main.class.getName());
     /**
      * > td bulk_import:prepare_parts2
      * usage:
      *   $ java BulkImportTool prepare_parts <files...>
      * example:
      *   $ java BulkImportTool prepareParts logs/*.csv \
      *         -Dtd.bulk_import.prepare_parts.format=csv \
      *         -Dtd.bulk_import.prepare_parts.columns=time,uid,price,count \
      *         -Dtd.bulk_import.prepare_parts.columntypes=long,string,long,int \
      *         -Dtd.bulk_import.prepare_parts.time_column=time \
      *         -Dtd.bulk_import.prepare_parts.output_dir=./parts/
      * description:
      *   Convert files into part file format
      * options:
      *   -f, --format NAME                source file format [csv]
      *   -h, --columns NAME,NAME,...      column names (use --column-header instead if the first line has column names)
      *       --column-types TYPE,TYPE,... column types [string, long, int]
      *   -H, --column-header              first line includes column names
      *   -t, --time-column NAME           name of the time column
      *       --time-value TIME            long value of the time column
      *   -s, --split-size SIZE_IN_KB      size of each parts (default: 16384)
      *   -o, --output DIR                 output directory
      *
      * @param args
      * @param props
      * @throws Exception
      */
     public static void prepareParts(final String[] args, Properties props)
             throws Exception {
         if (args.length < 2) {
             throw new IllegalArgumentException("File names not specified");
         }
 
         LOG.info(String.format("Start %s command", Configuration.CMD_PREPARE_PARTS));
 
         final String[] fileNames = new String[args.length - 1];
         for (int i = 0; i < args.length - 1; i++) {
             fileNames[i] = args[i + 1];
         }
 
         final PrepareConfiguration conf = new PrepareConfiguration();
         conf.configure(props);
 
         MultiThreadPrepareProcessor proc = new MultiThreadPrepareProcessor(conf);
         proc.registerWorkers();
         proc.startWorkers();
 
         // scan files that are uploaded
         new Thread(new Runnable() {
             public void run() {
                 for (int i = 0; i < fileNames.length; i++) {
                     try {
                         PrepareProcessor.Task task = new PrepareProcessor.Task(fileNames[i]);
                         MultiThreadPrepareProcessor.addTask(task);
                     } catch (Throwable t) {
                         LOG.severe("Error occurred During 'addTask' method call");
                         LOG.throwing("Main", "addTask", t);
                     }
                 }
 
                 // end of file list
                 try {
                     MultiThreadPrepareProcessor.addFinishTask(conf);
                 } catch (Throwable t) {
                     LOG.severe("Error occurred During 'addFinishTask' method call");
                     LOG.throwing("Main", "addFinishTask", t);
                 }
             }
         }).start();
 
         proc.joinWorkers();
 
         for (PrepareProcessor.ErrorInfo e : proc.getErrors()) {
            // TODO should handle errors
             e.error.printStackTrace();
         }
     }
 
     /**
      * > td bulk_import:upoad_parts2
      * usage:
      *   $ td bulk_import:upload_parts <name> <files...>
      * example:
      *   $ td bulk_import:upload_parts parts/* --parallel 4
      * description:
      *   Upload or re-upload files into a bulk import session
      * options:
      *   -P, --prefix NAME       add prefix to parts name
      *   -s, --use-suffix COUNT  use COUNT number of . (dots) in the source file name to the parts name
      *       --auto-perform      perform bulk import job automatically
      *       --parallel NUM      perform uploading in parallel (default: 2; max 8)
      */
     public static void uploadParts(final String[] args, Properties props)
             throws Exception {
         if (args.length < 3) {
             throw new IllegalArgumentException("File names not specified");
         }
 
         LOG.info(String.format("Start %s command", Configuration.CMD_UPLOAD_PARTS));
 
         final String sessionName = args[1];
         final String[] fileNames = new String[args.length - 2];
         for (int i = 0; i < args.length - 2; i++) {
             fileNames[i] = args[i + 2];
         }
 
         final UploadConfiguration conf = new UploadConfiguration();
         conf.configure(props);
 
         MultiThreadUploadProcessor proc = new MultiThreadUploadProcessor(conf);
         proc.registerWorkers();
         proc.startWorkers();
 
         // scan files that are uploaded
         new Thread(new Runnable() {
             public void run() {
                 for (int i = 0; i < fileNames.length; i++) {
                     try {
                         long size = new File(fileNames[i]).length();
                         UploadProcessor.Task task = new UploadProcessor.Task(
                                 sessionName, fileNames[i], size);
                         MultiThreadUploadProcessor.addTask(task);
                     } catch (Throwable t) {
                         LOG.severe("Error occurred During 'addTask' method call");
                         LOG.throwing("Main", "addTask", t);
                     }
                 }
 
                 // end of file list
                 try {
                     MultiThreadUploadProcessor.addFinishTask(conf);
                 } catch (Throwable t) {
                     LOG.severe("Error occurred During 'addFinishTask' method call");
                     LOG.throwing("Main", "addFinishTask", t);
                 }
             }
         }).start();
 
         proc.joinWorkers();
 
         // TODO need strict error handling
         MultiThreadUploadProcessor.processAfterUploading(new BulkImportClient(
                 new TreasureDataClient(conf.getProperties())), conf,
                 sessionName);
     }
 
     public static void prepareAndUploadParts(final String[] args, Properties props)
             throws Exception {
         if (args.length < 3) {
             throw new IllegalArgumentException("File names not specified");
         }
 
         LOG.info(String.format("Start %s and %s commands",
                 Configuration.CMD_UPLOAD_PARTS, Configuration.CMD_PREPARE_PARTS));
 
         final String sessionName = args[1];
         final String[] fileNames = new String[args.length - 2];
         for (int i = 0; i < args.length - 2; i++) {
             fileNames[i] = args[i + 2];
         }
 
         final UploadConfiguration conf = new UploadConfiguration();
         conf.configure(props);
 
         MultiThreadPrepareProcessor prepareProc = new MultiThreadPrepareProcessor(conf);
         prepareProc.registerWorkers();
         prepareProc.startWorkers();
 
         MultiThreadUploadProcessor uploadProc = new MultiThreadUploadProcessor(conf);
         uploadProc.registerWorkers();
         uploadProc.startWorkers();
 
         // scan files that are uploaded
         new Thread(new Runnable() {
             public void run() {
                 for (int i = 0; i < fileNames.length; i++) {
                     try {
                         PrepareProcessor.UploadTask task =
                                 new PrepareProcessor.UploadTask(sessionName, fileNames[i]);
                         MultiThreadPrepareProcessor.addTask(task);
                     } catch (Throwable t) {
                         LOG.severe("Error occurred During 'addTask' method call");
                         LOG.throwing("Main", "addTask", t);
                     }
                 }
 
                 // end of file list
                 try {
                     MultiThreadPrepareProcessor.addFinishTask(conf);
                 } catch (Throwable t) {
                     LOG.severe("Error occurred During 'addFinishTask' method call");
                     LOG.throwing("Main", "addFinishTask", t);
                 }
             }
         }).start();
 
         prepareProc.joinWorkers();
 
         MultiThreadUploadProcessor.addFinishTask(conf);
         uploadProc.joinWorkers();
 
         // TODO need strict error handling
         MultiThreadUploadProcessor.processAfterUploading(new BulkImportClient(
                 new TreasureDataClient(conf.getProperties())), conf,
                 sessionName);
     }
 
     public static void main(final String[] args) throws Exception {
         if (args.length < 1) {
             throw new IllegalArgumentException("Command not specified");
         }
 
         String commandName = args[0];
         Properties props = System.getProperties();
         if (commandName.equals(Configuration.CMD_PREPARE_PARTS)) {
             prepareParts(args, props);
         } else if (commandName.equals(Configuration.CMD_UPLOAD_PARTS)) {
             if (!includePrepareProcessing(props)) {
                 uploadParts(args, props);
             } else {
                 prepareAndUploadParts(args, props);
             }
         } else {
             throw new IllegalArgumentException(
                     "Not support command: " + commandName);
         }
     }
 
     private static boolean includePrepareProcessing(Properties props) {
         // TODO FIXME #MN this method should be considered more...
         for (Iterator<Object> keyIter = props.keySet().iterator(); keyIter.hasNext(); ) {
             String key = (String) keyIter.next();
             if (key.startsWith("td.bulk_import.prepare_parts.")) {
                 return true;
             }
         }
         return false;
     }
 
 }
