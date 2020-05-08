 /*
  * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
  *
  * This file is part of Iglu.
  *
  * Iglu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Iglu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ijsberg.iglu.util.io;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  */
 public class FSFileCollection implements FileCollection {
 
     private String baseDir;
 //    private String absSourceRoot;
     private Charset charSet;
 
 //    private List<File> files;
     private Map<String, File> filesByRelativePathAndName = new TreeMap<String, File>();
     private FileFilterRuleSet includedFilesRuleSet;
 
     public FSFileCollection(String baseDir, FileFilterRuleSet fileFilterRuleSet) {
 
         this.baseDir = FileSupport.convertToUnixStylePath(baseDir);
 //        this.absSourceRoot = FileSupport.convertToUnixStylePath(baseDir + "/" + relativeSourceRoot);
 
         this.includedFilesRuleSet = fileFilterRuleSet;
 
 		fileFilterRuleSet.setBaseDir(this.baseDir);
 
         refreshFiles();
     }
 
     @Override
     public List<String> getFileNames() {
         refreshFiles();
         return new ArrayList<String>(filesByRelativePathAndName.keySet());
     }
 
     @Override
     public byte[] getFileByName(String fileName) throws IOException {
         return FileSupport.getBinaryFromFS(filesByRelativePathAndName.get(fileName));
     }
 
     public File getActualFileByName(String fileName) throws IOException {
         return filesByRelativePathAndName.get(fileName);
     }
 
     @Override
     public String getFileContentsByName(String fileName) throws IOException {
         return new String(getFileByName(fileName));
     }
 
 	@Override
 	public FileFilterRuleSet getFileFilter() {
 		return includedFilesRuleSet;
 	}
 
 	@Override
 	public boolean containsFile(String fileName) {
 		return filesByRelativePathAndName.containsKey(FileSupport.convertToUnixStylePath(fileName));
 	}
 
 	@Override
 	public Directory getRootDirectory() {
 		return rootDir;
 	}
 
 	@Override
 	public void setFileFilter(FileFilterRuleSet fileFilter) {
 		this.includedFilesRuleSet = fileFilter;
 		refreshFiles();
 	}
 
 	public void refreshFiles() {
 
         filesByRelativePathAndName.clear();
 		rootDir = new Directory("ROOT");
 
         List<File> files = FileSupport.getFilesInDirectoryTree(baseDir, includedFilesRuleSet);
 
         for (File file : files) {
             String relativePathAndName = FileSupport.convertToUnixStylePath(file.getPath()).substring(
                     baseDir.length());
 			if(relativePathAndName.startsWith("/")) {
 				relativePathAndName = relativePathAndName.substring(1);
 			}
			filesByRelativePathAndName.put(relativePathAndName, file);
 			rootDir.addFile(relativePathAndName);
         }
     }
 
 	@Override
 	public String getDescription() {
 		return "directory: '" + baseDir + "'";
 	}
 
 	@Override
 	public int size() {
 		return filesByRelativePathAndName.size();
 	}
 
 	Directory rootDir = new Directory("ROOT");
 
 
 
 
 
 }
