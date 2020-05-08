 package com.google.code.maven_replacer_plugin.file;
 
 import java.io.File;
 import java.io.IOException;
 
 public class FileUtils {
 	public boolean fileNotExists(String filename) {
 		if (filename == null || filename.trim().length() == 0) {
 			return true;
 		}
 		return !new File(filename).exists();
 	}
 
 	public void ensureFolderStructureExists(String file) {
 		File outputFile = new File(file);
 		if (outputFile.getParent() == null) {
 			return;
 		}
 
 		if (!outputFile.isDirectory()) {
 			File parentPath = new File(outputFile.getParent());
			if (!parentPath.exists() && !parentPath.mkdirs()) {
				throw new Error("Error creating directory.");
 			}
 		} else {
 			throw new IllegalArgumentException("OutputFile cannot be a directory: " + file);
 		}
 	}
 
 	public String readFile(String file) throws IOException {
 		return org.apache.commons.io.FileUtils.readFileToString(new File(file));
 	}
 
 	public void writeToFile(String outputFile, String content) throws IOException {
 		ensureFolderStructureExists(outputFile);
 		org.apache.commons.io.FileUtils.writeStringToFile(new File(outputFile), content);
 	}
 	
 	public String createFullPath(String... dirsAndFilename) {
 		StringBuilder fullPath = new StringBuilder();
 		for (int i=0; i < dirsAndFilename.length - 1; i++) {
 			fullPath.append(dirsAndFilename[i]);
 			fullPath.append(File.separator);
 		}
 		fullPath.append(dirsAndFilename[dirsAndFilename.length - 1]);
 		
 		return fullPath.toString();
 	}
 }
