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
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 /**
  */
 public class ZippedFileCollection implements FileCollection {
 
     private Map<String, ZipEntry> filesByRelativePathAndName = new TreeMap<String, ZipEntry>();
     private FileFilterRuleSet includedFilesRuleSet;
     private ZipFile zipFile;
 
 	private String relativeDir = "";
 
     public ZippedFileCollection(String zipFileName, FileFilterRuleSet fileFilterRuleSet) throws IOException {
 
         this(new ZipFile(zipFileName), fileFilterRuleSet);
     }
 
 	public ZippedFileCollection(File file) throws IOException {
 		this(new ZipFile(file), new FileFilterRuleSet().setIncludeFilesWithNameMask("*.*"));
 	}
 
 	public ZippedFileCollection(ZipFile zipFile, FileFilterRuleSet fileFilterRuleSet) {
         this.includedFilesRuleSet = fileFilterRuleSet;
         this.zipFile = zipFile;
		if(fileFilterRuleSet.getBaseDir() != null) {
			this.relativeDir = FileSupport.convertToUnixStylePath(fileFilterRuleSet.getBaseDir());
		}
 		refreshFiles();
     }
 
 	public ZippedFileCollection(ZipFile zipFile, String relativeDir, FileFilterRuleSet fileFilterRuleSet) {
 		this.includedFilesRuleSet = fileFilterRuleSet;
 		this.zipFile = zipFile;
 		if(relativeDir != null && !"".equals(relativeDir)) {
 			this.relativeDir = FileSupport.convertToUnixStylePath(relativeDir);
 			if(!this.relativeDir.endsWith("/")) {
 				this.relativeDir += "/";
 			}
 		}
 		fileFilterRuleSet.setBaseDir(this.relativeDir);
 		refreshFiles();
 	}
 
 	@Override
     public List<String> getFileNames() {
         return new ArrayList<String>(filesByRelativePathAndName.keySet());
     }
 
     @Override
     public byte[] getFileByName(String fileName) throws IOException {
         return FileSupport.getBinaryFromZip(relativeDir + fileName, zipFile);
     }
 
     @Override
     public String getFileContentsByName(String fileName) throws IOException {
         return FileSupport.getTextFileFromZip(relativeDir + fileName, zipFile);
     }
 
 	@Override
 	public FileFilterRuleSet getFileFilter() {
 		return includedFilesRuleSet;
 	}
 
 	@Override
 	public boolean containsFile(String fileName) {
		boolean retval = filesByRelativePathAndName.containsKey(FileSupport.convertToUnixStylePath(fileName));
		if(!retval) {
//			System.out.println(FileSupport.convertToUnixStylePath(fileName) + " NOT FOUND in " + filesByRelativePathAndName.keySet());
		}
		return retval;
 	}
 
 	@Override
 	public void refreshFiles() {
         filesByRelativePathAndName.clear();
 		rootDir = new Directory("ROOT");
 
         List<ZipEntry> zipEntries = FileSupport.getContentsFromZipFile(zipFile, includedFilesRuleSet);
 
         for (ZipEntry zipEntry : zipEntries) {
             String relativePathAndName = FileSupport.convertToUnixStylePath(zipEntry.getName());
 
 			if(relativePathAndName.startsWith(relativeDir)) {
 				relativePathAndName = relativePathAndName.substring(relativeDir.length());
 				filesByRelativePathAndName.put(relativePathAndName, zipEntry);
 				rootDir.addFile(relativePathAndName);
 			}
 		}
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
 
 
 	@Override
 	public String getDescription() {
 		return "file: '" + zipFile.getName() + "' subdirectory: '" + relativeDir + "'";
 	}
 
 	@Override
 	public int size() {
 		return filesByRelativePathAndName.size();
 	}
 
 
 	Directory rootDir = new Directory("ROOT");
 
 }
