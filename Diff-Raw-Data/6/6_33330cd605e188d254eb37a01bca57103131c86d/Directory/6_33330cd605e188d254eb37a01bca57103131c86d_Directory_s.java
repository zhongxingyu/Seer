 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client.ui.assetpanel;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 public class Directory {
 
     private static final FileFilter DIRECTORY_FILTER = new DirectoryFileFilter();
 
     private List<PropertyChangeListener> listenerList = new CopyOnWriteArrayList<PropertyChangeListener>();  
     
     private File directory;
 
     private Directory parent;
     private List<Directory> subdirs;
     private List<File> files;
 
     private FilenameFilter fileFilter;
     
     public Directory(File directory) {
         this (directory, null);
     }
     public Directory(File directory, FilenameFilter fileFilter) {
         if (directory.exists() && !directory.isDirectory()) {
             throw new IllegalArgumentException(directory + " is not a directory");
         }
         
         this.directory = directory;
         this.fileFilter = fileFilter;
     }
 
     public void addPropertyChangeListener(PropertyChangeListener listener) {
         if (!listenerList.contains(listener)) {
             listenerList.add(listener);
         }
     }
     
     public void removePropertyChangeListener(PropertyChangeListener listener) {
         listenerList.remove(listener);
     }
     
     public boolean equals(Object o) {
         if (!(o instanceof Directory)) {
             return false;
         }
         
         return directory.equals(((Directory)o).directory);
     }
     
     public File getPath() {
         return directory;
     }
     
     public void refresh() {
     	subdirs = null;
     	files = null;
     }
     
     public List<Directory> getSubDirs() {
         load();
         return subdirs;
     }
     
     public List<File> getFiles() {
         load();
         return files;
     }
     
     public Directory getParent() {
         return parent;
     }
     
     private void load() {
         if (files == null && subdirs == null) {

             files = Collections.unmodifiableList(Arrays.asList(directory.listFiles(fileFilter)));
             File [] subdirList = directory.listFiles(DIRECTORY_FILTER);
             subdirs = new ArrayList<Directory>();
             for (int i = 0; i < subdirList.length; i++) {
                 Directory newDir = newDirectory(subdirList[i], fileFilter); 
                 newDir.parent = this;
                 subdirs.add(newDir);
             }
             Collections.sort(subdirs, new Comparator<Directory>() {
             	public int compare(Directory d1, Directory d2) {
             		String name1 = d1.getPath().getName();
             		String name2 = d2.getPath().getName();
             		
             		return String.CASE_INSENSITIVE_ORDER.compare(name1, name2);
             	}
             });
             subdirs = Collections.unmodifiableList(subdirs);
         }
     }
     
     private static class DirectoryFileFilter implements FileFilter {
         
         public boolean accept(File pathname) {
             return pathname.isDirectory();
         }
     }
     
     protected Directory newDirectory(File directory, FilenameFilter fileFilter) {
     	return new Directory(directory, fileFilter);
     }
     
     protected void firePropertyChangeEvent(PropertyChangeEvent event) {
         
     	// Me
         for (PropertyChangeListener listener : listenerList) {
             listener.propertyChange(event);
         }
         
         // Propagate up
         if (parent != null) {
         	parent.firePropertyChangeEvent(event);
         }
     }
     
     public static final Comparator<Directory> COMPARATOR = new Comparator<Directory>() {
     	public int compare(Directory o1, Directory o2) {
     		String filename1 = o1.getPath().getName();
     		String filename2 = o2.getPath().getName();
     		return filename1.compareToIgnoreCase(filename2);
     	}
     };
 }
