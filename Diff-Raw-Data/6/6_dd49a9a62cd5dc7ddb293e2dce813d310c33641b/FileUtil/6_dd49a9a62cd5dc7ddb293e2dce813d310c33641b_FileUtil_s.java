 /*  Copyright (C) 2012  Nicholas Wright
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.dozedoff.commonj.file;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 
 public class FileUtil {
 	
 	public static File WorkingDir(){
 		return new File( System.getProperty("user.dir") );
 	}
 
 	public static List<String> pathTokenList(String path){
 		LinkedList<String> resultList = new LinkedList<String>();
 		
		Scanner scanner = new Scanner(path).useDelimiter("\\\\");
 		//to handle network paths
 		if(path.startsWith("\\\\")){
 			int endPos = path.indexOf("\\", 2);
 			resultList.add(path.substring(0, endPos));
 			scanner.next();
 			scanner.next();
 		}
 		
 		while(scanner.hasNext()){
 			resultList.add(scanner.next());
 		}
 		
 		//check if the last entry (filename) has an extension, if so, split them
 		if(resultList.peekLast() != null && resultList.peekLast().contains(".")){
 			String lastEntry = resultList.pollLast();
 			int dotPosition = lastEntry.lastIndexOf(".");
 			
 			String filename = lastEntry.substring(0, dotPosition);
 			String extension = lastEntry.substring(dotPosition);
 			
 			resultList.add(filename);
 			resultList.add(extension);
 		}
 		
 		return resultList;
 		
 	}
 	
 	/**
 	 * Move a directory, sub-directories and files to the new location.
 	 * src\a , dst will result in dst\a
 	 * 
 	 * @param source the path of the source directory
 	 * @param destination the path of the destination directory
 	 * @throws IOException
 	 */
 	public static void moveDirectory(Path source, Path destination) throws IOException{
 		Files.walkFileTree(source, new DirectoryMover(source, destination));
 	}
 	
 	public static void moveFileWithStructure(Path source, Path dstDirectory) throws IOException{
 		Path relativeSource = source.getRoot().relativize(source);
 		Path destinationPath = dstDirectory.resolve(relativeSource);
 		
 		Files.createDirectories(destinationPath.getParent());
 		
 		Files.move(source, destinationPath);
 	}
 	
 	static public String convertDirPathToString(Path directory){
 		if(directory == null){
 			return null;
 		}else if((directory.getRoot() != null) && (directory.getRoot().equals(directory))){
 			return directory.toString().toLowerCase();
 		}else{
 			return directory.toString().toLowerCase()+"\\";
 		}
 	}
 	
 	/**
 	 * Remove the root component from the path, returning a relative path.
 	 * If the path is already relative, it will not be changed.
 	 * C:\temp\   becomes \temp\
 	 * @param path path to remove root from
 	 * @return a relative path
 	 */
 	static public Path removeDriveLetter(Path path){
 		if(path == null){
 			return null;
 		}
 		
 		if(path.isAbsolute()){
 			return Paths.get(path.getRoot().relativize(path).toString());
 		}else{
 			return path;
 		}
 	}
 	
 	static public String removeDriveLetter(String path) {
 		if (path == null) {
 			return null;
 		}
 
 		Path relPath = removeDriveLetter(Paths.get(path));
 		String filename = relPath.getFileName().toString();
 
 		if (filename.contains(".")) {
 			return relPath.toString();
 		} else {
 			return relPath.toString() + "\\";
 		}
 	}
 	
 	static class DirectoryMover extends SimpleFileVisitor<Path>{
 		Path currMoveDir, dstDir, srcDir;
 		
 		public DirectoryMover(Path srcDir, Path dstDir) {
 			this.dstDir = dstDir;
 			this.srcDir = srcDir.getParent();
 		}
 		
 		@Override
 		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
 			File f = new File(dstDir.toFile(),srcDir.relativize(dir).toString());
 			f.mkdirs(); // create new directory with identical name in the destination directory
 			currMoveDir = f.toPath();
 			return super.preVisitDirectory(dir, attrs);
 		}
 		
 		@Override
 		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
 			Files.move(file, currMoveDir.resolve(file.getFileName())); // move files
 			return super.visitFile(file, attrs);
 		}
 		
 		@Override
 		public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
 			Files.delete(arg0); // delete the source directory when done
 			return super.postVisitDirectory(arg0, arg1);
 		}
 	}
 }
