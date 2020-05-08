 /*
 Copyright 2008 Flaptor (flaptor.com) 
 
 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 
 
     http://www.apache.org/licenses/LICENSE-2.0 
 
 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License.
 */
 
 package com.flaptor.util.sort;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Vector;
 
 /**
  * This class sorts a file that doesn't fit in available memory using the mergeSort algorithm.
  * 
  * This code is based on the code found in the book
  * "Developing Java Software" by Winder and Roberts
  */
 public final class MergeSort {
 
     /**
      * The sort operation
      * @param filein the name of the input unsorted file
      * @param fileout the name of the output sorted file
      * @param rInfo the RecordInformation object
      */ 
     public static void sort (File filein, File fileout, RecordInformation rInfo, SortProgressReporting progress) throws FileNotFoundException, IOException {
         // Get whatever tmp files may be left from an interrupted sort operation.
         File parent = filein.getParentFile();
         Vector<File> tmpFiles = new Vector<File>(Arrays.asList(parent.listFiles(new TmpFilesFilter())));
         // If the original unsorted file exists, it means this is a fresh sort 
         // or an interrupted sort where the block distribution didn't finish.
         if (filein.exists()) {
             // if there are leftover blocks, delete them.
             if (tmpFiles.size() > 0) {
                 cleanUpTmp (tmpFiles);
             }
             // Perform the initial dispersion into sorted files.
             tmpFiles = distributeSortedBlocks (filein, fileout.getParentFile(), rInfo, progress);
             // Delete original file, it is no longer needed and uses up space.
             filein.delete();
         }
         // if there is something to merge...
         if (tmpFiles.size() > 0) {
             // If the output file is there, it was interrupted during merge.
             // In that case, delete the output file before merging.
             if (fileout.exists()) {
                 fileout.delete();
             }
             // Merge the sorted files.
             mergeSortedBlocks (tmpFiles, fileout, rInfo, progress);
             // Cleanup.
             cleanUpTmp (tmpFiles);
         }
     }
 
     /**
      * Determine if the sort needs to be completed.
      * This does not look into the file to see if it is sorted, 
      * it only detects situations in which the sort was interrupted.
      */
     public static boolean sortIsIncomplete (File filein) {
         if (filein.exists()) return true;
         if (filein.getParentFile().listFiles(new TmpFilesFilter()).length > 0) return true;
         return false;
     }
 
     /**
      * Delete the temporary files.
      */
     private static void cleanUpTmp (Vector<File> tmpFiles) {
         for (int i = 0; i < tmpFiles.size(); i++) {
             tmpFiles.elementAt(i).delete();
         }
     }
     
     /**
      * This filter accepts only temporary files.
      */
     private static class TmpFilesFilter implements FilenameFilter {
         public boolean accept (File dir, String name) {
             return name.startsWith("tmp_");
         }
     }
 
 
     /**
      * Perform the initial dispersion of the data.
      */    
     private static Vector<File> distributeSortedBlocks (File from, File tmpDir, RecordInformation rInfo, SortProgressReporting progress) throws FileNotFoundException, IOException {
         RecordReader reader = rInfo.newRecordReader(from);
         Vector<File> files = new Vector<File>();
         Runtime.getRuntime().gc(); // free memory
         long startingMem = Runtime.getRuntime().freeMemory();
 //System.out.println("startingMem = " + Math.round(startingMem/1024/102.4f)/10.0f + " MB");
         boolean allDone = false;
         if (null != progress) progress.startSort();
         for (int i = 0; ! allDone; i++) {
             // Pull in a few records, put them into the Vector
             // that is where we are performing the internal sort
             // that creates the sorted blocks.
             Vector<Record> rec = new Vector<Record>();
             boolean memoryAvailable = true;
             while (memoryAvailable && ! allDone) {
                 Record r = null;
                 try {
                     r = reader.readRecord();
                 } catch (java.io.StreamCorruptedException e) {
                     System.out.println("Error reading records: "+e);
                 }
                 if (r == null) {
                     allDone = true;
                     break;
                 }
                 rec.addElement(r);
                 long remainingMem = Runtime.getRuntime().freeMemory();
                 memoryAvailable = (remainingMem > startingMem * 0.5f); // use up to 50% of available memory.
 //if (!memoryAvailable) System.out.println("remainingMem = " + Math.round(remainingMem/1024/102.4f)/10.0f + " MB; records = " + rec.size());
             }
             // Sort the vector
             QuicksortVector.sort(rec, rInfo.getComparator());
             // Write out the sorted vector.
             File tmpFile = new File(tmpDir, "tmp_" + i);
             files.addElement(tmpFile);
             RecordWriter writer = rInfo.newRecordWriter(tmpFile);
             for (int j = 0; j < rec.size(); j++) {
                 writer.writeRecord(rec.elementAt(j));
             }
             writer.close();
             // report progress
             if (null != progress) progress.reportSorted(rec.size());
             // free memory
             rec.clear();
             rec = null;
             Runtime.getRuntime().gc();
         }
         reader.close();
         return files;
     }
 
     /**
      * Undertake a round of merging.
      */
     private static void mergeSortedBlocks (Vector filesIn, File fileOut, RecordInformation rInfo, SortProgressReporting progress) throws FileNotFoundException, IOException {
         // Open up the set of Readers and the Writer.
         RecordReader[] readers = new RecordReader[filesIn.size()];
         for (int i = 0 ; i < readers.length ; i++) {
             readers[i] = rInfo.newRecordReader((File)filesIn.elementAt(i));
         }
         RecordWriter writer = rInfo.newRecordWriter(fileOut);
         // Initialize the records array holding the next record from each of the files.
         Record[] rec = new Record[readers.length];
         for (int j = 0; j < readers.length; j++) {
             rec[j] = readers[j].readRecord();
         }
         if (null != progress) progress.startMerge();
         long count = 0;
         while (true) {
             // Determine which is the next Record to add to the output stream.
             int index = findAppropriate(rec, rInfo.getComparator());
             // If there isn't one then we get a negative index and we can terminate the loop.
             if (index < 0) break;
             // Write the record to the destination file
             writer.writeRecord(rec[index]);
             // Draw a new Record from the file whose Record was chosen
             rec[index] = readers[index].readRecord();
             // Report progress
             if (null != progress) {
                 count++;
                if (count >= SortProgressReporting.REPORTING_PERIOD) {
                     progress.reportMerged(count);
                     count = 0;
                 }
             }
         }
         if (null != progress) progress.reportMerged(count);
         // Cleanup.
         writer.close();
         for (int i = 0; i < readers.length; i++) {
             readers[i].close();
         }
     }
 
     /**
      * Determine which Record is the one to be output next.
      *
      * @param items the array of <code>Records</code> from which to
      * select the next according to the order relation defined bu
      * <code>c</code>.
      *
      * @param c the <code>Comparator</code> defining the required
      * order relation on the <code>Record</code>s.
      *
      * @return the index in the array of the item that should be
      * chosen next.
      */
     private static int findAppropriate(final Record[] items, final Comparator c) {
         // Find first non-empty entry.
         int index = -1;
         for (int i = 0; i < items.length; i++) {
             if (items[i] != null) {
                 index = i;
                 break;
             }
         }
         // If there still are non-empty entries then do a linear search 
         // through the items to see which is the next one to select.
         if (index >= 0) {
             Record value = items[index];
             for (int i = index+1; i < items.length; i++) {
                 if (items[i] != null) {
                     if (c.compare(items[i], value) < 0) {
                         index = i;
                         value = items[i];
                     }
                 }
             }
         }
         return index;
     }
 }
 
