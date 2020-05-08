 /*===========================================================================
   Copyright (C) 2010 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Helper methods for manipulating files. 
  */
 public final class FileUtil {
 
 	/**
 	 * Search all {@link File}s recursively that pass the {@link FilenameFilter}. Adapted from
 	 * http://snippets.dzone.com/posts/show/1875
 	 * @param directory root directory
 	 * @param filter {@link FilenameFilter} used to filter the File candidates
 	 * @param recurse do we recurse or not?
 	 * @return an array of {@link File}s (File[])
 	 */
 	public static File[] getFilteredFilesAsArray(File directory, FilenameFilter filter, boolean recurse) {
 		Collection<File> files = FileUtil.getFilteredFiles(directory, filter, recurse);
 		File[] arr = new File[files.size()];
 		return files.toArray(arr);
 	}
 
 	/**
 	 * Search all {@link File}s recursively that pass the {@link FilenameFilter}. Adapted from
 	 * http://snippets.dzone.com/posts/show/1875
 	 * @param directory root directory
 	 * @param filter {@link FilenameFilter} used to filter the File candidates
 	 * @param recurse do we recurse or not?
 	 * @return {@link Collection} of {@link File}s
 	 */
 	public static Collection<File> getFilteredFiles(File directory, FilenameFilter filter, boolean recurse) {
 		// List of files / directories
 		List<File> files = new LinkedList<File>();
 	
 		// Get files / directories in the directory
 		File[] entries = directory.listFiles();
 	
		if (entries == null) {
			return files;
		}
		
 		// Go over entries
 		for (File entry : entries) {
 			// If there is no filter or the filter accepts the
 			// file / directory, add it to the list
 			if (filter == null || filter.accept(directory, entry.getName())) {
 				files.add(entry);
 			}
 	
 			// If the file is a directory and the recurse flag
 			// is set, recurse into the directory
 			if (recurse && entry.isDirectory()) {
 				files.addAll(getFilteredFiles(entry, filter, recurse));
 			}
 		}
 	
 		// Return collection of files
 		return files;
 	}
 
 }
