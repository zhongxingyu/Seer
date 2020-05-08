 package de.kalkin.andi.filebrowser;
 
 /*
  * Copyright (c) 2009, Bahtiar `kalkin-` Gadimov
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, 
  * are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this list of 
  *   conditions and the following disclaimer.
  * 
  * - Redistributions in binary form must reproduce the above copyright notice, this list 
  *   of conditions and the following disclaimer in the documentation and/or other materials 
  *   provided with the distribution.
  * 
  * - Neither the name of the Moo Productions nor the names of its contributors may be used 
  *   to endorse or promote products derived from this software without specific prior written 
  *   permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.util.Log;
 
 /**
  * This class returns all dirs and files from a directory filtered, and sorted
  * aphabeticaly
  * 
  * @author Bahtiar `kalkin-` Gadimov
  * 
  */
 public class DirectoryManager {
 
 	protected boolean showHidden = false;
 
 	protected enum FileOperations {
 		move, copy
 	};
 
 	protected FileOperations fileOperation;
 
 	/**
 	 * Contains a file which should be moved or copied
 	 */
 	protected File tmpFile = null;
 
 	public boolean isShowHidden() {
 		return showHidden;
 	}
 
 	public void setShowHidden(boolean showHidden) {
 		this.showHidden = showHidden;
 	}
 
 	/**
 	 * Returns all dirs and files from a directory sorted by natural order and
 	 * dirs first.
 	 * 
 	 * @param directory
 	 *            The directory to display content from
 	 * @return {@link ArrayList}<Files>
 	 */
 	public ArrayList<File> getDirectoryListing(File directory) {
 		return this.getDirectoryListing(directory, null);
 	}
 
 	/**
 	 * Returns all dirs and files from a directory sorted by natural order and
 	 * dirs first. Further more string file filter may be aplied by mFilters
 	 * parameter.
 	 * 
 	 * @param directory
 	 *            The directory to display content from
 	 * @param mFilters
 	 *            File filters
 	 * @return {@link ArrayList}<Files>
 	 */
 	public ArrayList<File> getDirectoryListing(File directory, String[] mFilters) {
 
 		File[] dirs = directory.listFiles(new DirFilter(this.showHidden));
 		File[] files;
 
 		if (mFilters != null)
 			files = directory.listFiles(new FileFilter(mFilters,
 					this.showHidden));
 		else
 			files = directory.listFiles(new FileFilter(this.showHidden));
 
 		// Used for calculation the precise ArrayList lenght. Saves performance.
 		int dirEntries = 0;
 
 		if (directory.getParentFile() != null)
 			dirEntries = 1;
 
 		if (dirs == null && files == null) {
 			if (directory.getParentFile() != null) {
 				ArrayList<File> result = new ArrayList<File>(1);
 				result.add(directory.getParentFile());
 				return result;
 			} else {
 				return new ArrayList<File>(0);
 			}
 		}
 		if (dirs != null) {
 			Arrays.sort(dirs);
 			dirEntries += dirs.length;
 		}
 
 		if (files != null) {
 			Arrays.sort(files);
 			dirEntries += files.length;
 		}
 		ArrayList<File> result = new ArrayList<File>(dirEntries);
 
 		if (directory.getParentFile() != null)
 			result.add(directory.getParentFile());
 
 		for (File file : dirs) {
 			result.add(file);
 		}
 		for (File file : files) {
 			result.add(file);
 		}
 		return result;
 	}
 
	public void createDirectory(File parent, String name) {
 		File newDir = new File(parent.getAbsoluteFile(), name);
 		Log.i("FileBrowser", "Creating " + newDir.getAbsolutePath());
 		Log.i("FileBrowser", "Write rechte? " + newDir.canWrite());
 		if (!newDir.exists()) {
 			try {
			boolean result = newDir.mkdir();
			Log.i("FileBrowser", "" + result);
			} catch (SecurityException e)
			{
 				Log.w("FileBrowser", e.getMessage());
 			}
 		}
 	}
 
 	public void moveFile(File file) {
 		Log.i("FileBrowser", "Moving file in moveFile() " + file.getName());
 		this.tmpFile = file;
 		this.fileOperation = FileOperations.move;
 	}
 
 	public void copyFile(File file) {
 		Log.i("FileBrowser", "Copying file in copyFile() " + file.getName());
 		tmpFile = file;
 		fileOperation = FileOperations.copy;
 	}
 
 	public void paste(File targetDir) throws IOException {
 
 		if (tmpFile == null || fileOperation == null)
 			return;
 
 		switch (fileOperation) {
 		case copy:
 			Log.i("FileBrowser", "Copying file " + tmpFile.getName());
 			copy(tmpFile, targetDir);
 			return;
 		case move:
 			Log.i("FileBrowser", "Moving file " + tmpFile.getName());
 			move(targetDir, tmpFile);
 			return;
 		}
 
 		tmpFile = null;
 
 	}
 
	private void copy(File srcDir, File dstDir) throws IOException {
		if (srcDir.isDirectory()) {
 
			File[] children = srcDir.listFiles();
			for (int i = 0; i < children.length; i++) {
				copy(new File(srcDir, children[i].getName()), new File(srcDir,
						children[i].getName()));
			}
		} else {
			// This method is implemented in Copying a File
			copyFile(srcDir, dstDir);
 		}
 	}
 
 	private void copyFile(File src, File dst) throws IOException {
 		Log.i("FileBrowser", "copyFile src" + src);
 		Log.i("FileBrowser", "copyFile dst" + new File(dst, src.getName()));
 		InputStream in = new FileInputStream(src);
 		OutputStream out = new FileOutputStream(new File(dst, src.getName()));
 
 		// Transfer bytes from in to out
 		byte[] buf = new byte[1024];
 		int len;
 		while ((len = in.read(buf)) > 0) {
 			out.write(buf, 0, len);
 		}
 		in.close();
 		out.close();
 	}
 
 	private void move(File targetDir, File fileToMove) {
 		fileToMove.renameTo(new File(targetDir, fileToMove.getName()));
 	}
 
 	public void delete(File file) {
 		if (file.isDirectory()) {
 			File[] children = file.listFiles();
 			if (children != null) {
 				for (int i = 0; i < children.length; i++) {
 					delete(children[i]);
 				}
 			}
 		}
 		file.delete();
 	}
 }
