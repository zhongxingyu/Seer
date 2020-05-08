 package de.hpi.fgis.util;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 /**
  * various file utility methods
  * @author tongr
  *
  */
 public class FileUtil {
 	public static enum Compression {
 		NONE, GZIP
 //		SNAPPY,
 		;
 	}
 	
 	public class ReadStream implements Closeable {
 		private final BufferedReader reader;
 		public ReadStream(BufferedReader reader) {
 			this.reader = reader;
 		}
 		public BufferedReader getBufferedReader() {
 			return this.reader;
 		}
 		
 		public String readLine() {
 			return FileUtil.this.readLine(this.reader);
 		}
 
 		public void close() {
 			FileUtil.this.close(this.reader);
 		}
 	}
 	private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
 	public String fileSeparator = System.getProperties().getProperty("file.separator");
 	public String lineSeparator = System.getProperties().getProperty("line.separator");
 
 	/**
 	 * recursively scans the specified path for a file name regular expression and returns the path to every file
 	 * @param rootFolderPath the root folder to be scanned
 	 * @param filePattern the file pattern to be found
 	 * @return the set of file names
 	 */
 	public Set<String> scan(String rootFolderPath, String filePattern) {
 		return this.scan(rootFolderPath, filePattern, true);
 	}
 	/**
 	 * scans the specified path for a file name regular expression and returns the path to every file
 	 * @param rootFolderPath the root folder to be scanned
 	 * @param filePattern the file pattern to be found
 	 * @param recurively whether the scanner should scan recursively or not
 	 * @return the set of file names
 	 */
 	public Set<String> scan(String rootFolderPath, String filePattern, boolean recurively) {
 		HashSet<String> files = new HashSet<String>();
 		this.scan(new File(rootFolderPath), filePattern, recurively, files);
 		
 		return files;
 	}
 	/**
 	 * scans the specified path file for a file name regular expression and returns the path to every file
 	 */
 	protected void scan(File folder, String filePattern, boolean recurively, Set<String> resultSet) {
 		if(folder.exists() && folder.isDirectory()) {
 			for(File child : folder.listFiles()) {
 				// file name matches
 				if(child.getName().matches(filePattern)) {
 					resultSet.add(child.getPath());
 				}
 				if(recurively && child.isDirectory()) {
 					// scan sub directories
 					scan(child, filePattern, recurively, resultSet);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * This is a performance optimization setting for the two phase merge sort process
 	 */
 	public int maxChunkRows = 80000;
 	/**
 	 * This is a performance disk space optimization setting -> zipped temp files need less disk 
 	 * space but the process needs mor CPU
 	 */
 	public Compression tempFileCompression = Compression.NONE; 
 
 	public void delete(String path, String fileName) {
 		File outputFile = new File(path, fileName);
 		
 		if(outputFile.exists()) {
 			// delete actually existing file
 			if(outputFile.isDirectory()) {
 				// first delete containing files recursively
 				for(String child : outputFile.list()) {
 					this.delete(outputFile.toString(), child);
 				}
 			}
 			outputFile.delete();
 		}
 	}
 	public PrintStream append(String path, String fileName) {
 		return this.write(path, fileName, true, Compression.NONE);
 	}
 	public PrintStream appendZipped(String path, String fileName) {
 		return this.write(path, fileName, true, Compression.GZIP);
 	}
 	public PrintStream override(String path, String fileName) {
 		return this.write(path, fileName, false, Compression.NONE);
 	}
 	public PrintStream backupAndOverride(String path, String fileName) {
 		File tmp = new File(path, fileName);
 		// backup existing file
 		if(tmp.exists()) {
 			File backup = new File(path, tmp.getName() + ".backup");
 			if(backup.exists()) {
 				backup.delete();
 			}
 			tmp.renameTo(backup);
 		}
 		return override(path, fileName);
 	} 
 	public PrintStream overrideZipped(String path, String fileName) {
 		return this.write(path, fileName, false, Compression.GZIP);
 	}
 	public PrintStream write(String path, String fileName, boolean append, Compression compression) {
 		File outputFile = new File(path, fileName);
 		
 		if(!outputFile.exists() && outputFile.getParentFile()!=null) {
 			// to be sure that the directory structure is actually there
 			outputFile.getParentFile().mkdirs();
 		}
 		
 		try {
 			OutputStream file = new FileOutputStream(outputFile, append);
 			
 			switch(compression) {
 			case GZIP:
 				file = new GZIPOutputStream(file);
 				break;
 			case NONE:
 				// no compression
 				break;
 			default:
 				throw new IllegalArgumentException("Unknown compression type " + compression);
 			}
 			
 			return new PrintStream( new BufferedOutputStream(file), false);
 		} catch (IOException e) {
 			logger.severe("Unable to open file \"" + outputFile + "\"!");
 			logger.throwing(this.getClass().getName(), "write", e);
 			throw new IllegalStateException("Unable to open file \"" + outputFile + "\"!", e);
 		}
 	}
 	public ReadStream read(String path, String fileName) {
 		return this.read(path, fileName, Compression.NONE);
 	}
 	public ReadStream readZipped(String path, String fileName) {
 		return this.read(path, fileName, Compression.GZIP);
 	}
 	/**
 	 * parses a csv file and returns an iterator over all rows containing all of its cells (split by the regex)
 	 */
 	public Iterable<String[]> parseCSVFile(final String path, final String fileName, final String delimiterRegex) {
 		return new Iterable<String[]>() {
 			public Iterator<String[]> iterator() {
 				return new Iterator<String[]>() {
 					private final ReadStream reader = read(path, fileName);
 					private String[] nextRow;
 					{   fetchNext();  }
 					private void fetchNext() {
 						String line;
 						while((line = reader.readLine())!=null) {
 							if(!line.trim().isEmpty()) {
 								nextRow = line.split(delimiterRegex);
 								return;
 							}
 						}
 						nextRow = null;
 					}
 					public boolean hasNext() {
 						return nextRow!=null;
 					}
 					public String[] next() {
 						if(nextRow==null) {
 							throw new NoSuchElementException();
 						}
 						final String[] result = nextRow;
 						fetchNext();
 						return result;
 					}
 					public void remove() {
 						throw new IllegalStateException("Not (yet) implemented!");
 					}
 				};
 			}
 		};
 	}
 	
 	public ReadStream read(String path, String fileName, Compression compression) {
 		File inputFile = new File(path, fileName);
 		
 		if(!inputFile.exists()) {
 			throw new IllegalArgumentException("Unable to open \"" + inputFile + "\"! File does not exist.");
 		}
 		
 		try {
			InputStream file;
 			
 			switch(compression) {
 			case GZIP:
				file = new GZIPInputStream(new FileInputStream(inputFile));
 				break;
 			case NONE:
 				// no compression
				file = new FileInputStream(inputFile);
 				break;
 			default:
 				throw new IllegalArgumentException("Unknown compression type " + compression);
 			}
 			
 			return new ReadStream(new BufferedReader(new InputStreamReader(file)));
 		} catch (IOException e) {
 			logger.severe("Unable to open file \"" + inputFile + "\"!");
 			logger.throwing(this.getClass().getName(), "read", e);
 			throw new IllegalStateException("Unable to open file \"" + inputFile + "\"!", e);
 		}
 	}
 	
 	public String readLine(BufferedReader reader) {
 		try {
 			return reader.readLine();
 		} catch (IOException e) {
 			logger.severe("Unable to read from stream!");
 			logger.throwing(this.getClass().getName(), "readLine", e);
 			throw new IllegalStateException("Unable to read from stream!", e);
 		}
 	}
 	
 	public void close(BufferedReader reader) {
 		try {
 			reader.close();
 		} catch (IOException e) {
 			logger.severe("Unable to close stream!");
 			logger.throwing(this.getClass().getName(), "close", e);
 		}
 	}
 	
 	/**
 	 * Two phase merge sort of a text file
 	 * inspired by http://www.codeodor.com/index.cfm/2007/5/14/Re-Sorting-really-BIG-files---the-Java-source-code/1208
 	 * @param path the path to the file
 	 * @param fileName the name of the file
 	 * @param columnDelimiter the delimiter between the columns in the file
 	 * @param columnIndex the index of the column used to sort
 	 * @return the number of sorted lines
 	 *
 	 */
 	public int sort(String path, String fileName, String columnDelimiter, int columnIndex) {
 		return this.sort(path, fileName, columnDelimiter, columnIndex, Compression.NONE);
 	}
 	
 	/**
 	 * Two phase merge sort of a zipped text file
 	 * inspired by http://www.codeodor.com/index.cfm/2007/5/14/Re-Sorting-really-BIG-files---the-Java-source-code/1208
 	 * @param path the path to the file
 	 * @param fileName the name of the file
 	 * @param columnDelimiter the delimiter between the columns in the file
 	 * @param columnIndex the index of the column used to sort
 	 * @return the number of sorted lines
 	 *
 	 */
 	public int sortZipped(String path, String fileName, String columnDelimiter, int columnIndex) {
 		return this.sort(path, fileName, columnDelimiter, columnIndex, Compression.GZIP);
 	}
 	
 	/**
 	 * Two phase merge sort
 	 * inspired by http://www.codeodor.com/index.cfm/2007/5/14/Re-Sorting-really-BIG-files---the-Java-source-code/1208
 	 * @param path the path to the file
 	 * @param fileName the name of the file
 	 * @param columnDelimiter the delimiter between the columns in the file
 	 * @param columnIndex the index of the column used to sort
 	 * @param gZipped if true, the input file will be interpreted as gZipped
 	 * @return the number of sorted lines
 	 *
 	 */
 	public int sort(String path, String fileName, String columnDelimiter, int columnIndex, Compression compression) {
 		
 		File inputFile = new File(path, fileName);
 		
 		// check the file for existence
 		if(!inputFile.exists()) {
 			throw new IllegalArgumentException("Unable to sort \"" + inputFile + "\"! File does not exist.");
 		}
 		
 		String filePathWOEnding = inputFile.toString();
 		String fileEnding = "";
 		
 		// file ending is actually there
 		if(inputFile.getName().lastIndexOf('.') > 0) {
 			filePathWOEnding = inputFile.toString().substring(0, inputFile.toString().lastIndexOf('.'));
 			fileEnding = inputFile.toString().substring(inputFile.toString().lastIndexOf('.'));
 		}
 		
 
 		return externalSort(filePathWOEnding, fileEnding, columnDelimiter, columnIndex, compression);
 	}
 	
 	
 	/**
 	 * 
 	 * sort the given file 
 	 * @param filePathWOEnding the file name w/o extension 
 	 * @param fileEnding the file extension  of the file
 	 * @param delimiter the delimiter of the columns
 	 * @param indexToCompare index of the column to use as sort criteria
 	 * @param compression the compression method
 	 * @return the number of sorted lines
 	 */
 	protected int externalSort(final String filePathWOEnding, final String fileEnding, final String delimiter, final int indexToCompare, Compression compression) {
 		int numFiles = 0;
 		int lineNumber = 0;
 		try {
 			final String filename = filePathWOEnding + fileEnding;
 			final long fileSize = new File(filename).length();
 			ReadStream reader = this.read(null, filePathWOEnding + fileEnding, compression);
 			String[] row = null;
 			ArrayList<String[]> xKRows = new ArrayList<String[]>(maxChunkRows);
 			long lineOffset = 0;
 
 
 			ProgressReport report = new ProgressReport("Sorting file").setReport(1048576).setMax(fileSize).setUnit("bytes");
 
 			do {
 				// get 50k rows -(10m sample)-> 8min
 				// get 60k rows -(10m sample)-> 8min
 				// get 70k rows -(10m sample)-> 8min
 				// get 100k rows -(10m sample)-> 10min
 				for (int i = 0; i < maxChunkRows; i++) {
 					// progress feedback
 					if (lineNumber % 1000000 == 0) {
 						report.set(lineOffset);
 					}
 					
 					String line = reader.readLine();
 					if (line == null) {
 						row = null;
 						break;
 					}
 					final StringTokenizer tokens = new StringTokenizer(line, delimiter);
 					final ArrayList<String> elements = new ArrayList<String>();
 					while(tokens.hasMoreTokens()) {
 						elements.add(tokens.nextToken());
 					}
 					row = elements.toArray(new String[elements.size()]);
 					if (row.length > indexToCompare)
 						xKRows.add(row);
 					lineNumber++;
 					lineOffset += line.length() + 2; // zzgl. \n
 				}
 				// sort the rows
 				xKRows = sort(xKRows, indexToCompare);
 
 				if(xKRows.size()>0) {
 					// write to disk
 					PrintStream chunkWriter = this.write(null, filePathWOEnding+ "_chunk" + numFiles + fileEnding, false, tempFileCompression);
 					for (int i = 0; i < xKRows.size(); i++) {
 						String line = flattenArray(xKRows.get(i), delimiter);
 						if(line!=null && !line.isEmpty()) {
 							chunkWriter.println(line);
 						}
 					}
 					chunkWriter.close();
 					numFiles++;
 					xKRows.clear();
 				}
 			} while (row != null);
 
 			report.finish();
 			
 			mergeFiles(filePathWOEnding, fileEnding, numFiles, delimiter, indexToCompare, lineNumber, compression);
 
 			reader.close();
 
 		} finally {
 			// cleanup
 			deleteChunkFiles(filePathWOEnding, fileEnding, numFiles);
 		}
 
 		return lineNumber;
 	}
 	
 	protected String flattenArray(final String[] a, final String separator) {
 
 		if(a==null) {
 			return null;
 		}
 		if(a.length==0) {
 			return "";
 		}
 		String result = a[0];
 		for (int i = 1; i < a.length; i++)
 			result += separator + a[i];
 		return result;
 	}
 
 	// sort an arrayList of arrays based on the ith column
 	protected ArrayList<String[]> sort(ArrayList<String[]> arr,
 			int index) {
 		ArrayList<String[]> left = new ArrayList<String[]>();
 		ArrayList<String[]> right = new ArrayList<String[]>();
 		if (arr.size() <= 1)
 			return arr;
 		else {
 			int middle = arr.size() / 2;
 			for (int i = 0; i < middle; i++)
 				left.add(arr.get(i));
 			for (int j = middle; j < arr.size(); j++)
 				right.add(arr.get(j));
 			left = sort(left, index);
 			right = sort(right, index);
 			return merge(left, right, index);
 
 		}
 
 	}
 
 	// merge the the results for mergeSort back together
 	protected ArrayList<String[]> merge(ArrayList<String[]> left,
 			ArrayList<String[]> right, int index) {
 		ArrayList<String[]> result = new ArrayList<String[]>(left.size()
 				+ right.size());
 		while (left.size() > 0 && right.size() > 0) {
 			if (left.get(0)[index].compareTo(right.get(0)[index]) <= 0) {
 				result.add(left.get(0));
 				left.remove(0);
 			} else {
 				result.add(right.get(0));
 				right.remove(0);
 			}
 		}
 		if (left.size() > 0) {
 			result.addAll(left);
 		}
 		if (right.size() > 0) {
 			result.addAll(right);
 		}
 		return result;
 	}
 
 	protected void deleteChunkFiles(String filePathWOEnding, String fileEnding, int numFiles) {
 		ProgressReport report = new ProgressReport("Deleting chunk files").setReport(100).setMax(numFiles).setUnit("files");
 		for (int i = 0; i < numFiles; i++) {
 			this.delete(null, filePathWOEnding + "_chunk" + i + fileEnding);
 			report.inc();
 		}
 		report.finish();
 	}
 	protected class ChunkFileManager {
 		private final int numOfFiles;
 		private final ArrayList<ReadStream> mergefbr;
 		private final ArrayList<String[]> filerows = new ArrayList<String[]>();
 		private final int minColumnCount;
 		private int emptyFileCount = 0;
 		private final String delimiter;
 
 		public ChunkFileManager(final String filePathWOEnding, final String fileEnding, final String delimiter, final int numFiles, final int minColumnCount) {
 			ProgressReport report = new ProgressReport("Opening chunk files").setReport(1000).setMax(numFiles).setUnit("files");
 
 			this.mergefbr =  new ArrayList<ReadStream>(numFiles);
 			this.minColumnCount = minColumnCount;
 			this.delimiter = delimiter;
 			this.numOfFiles = numFiles;
 	
 		
 			for (int i = 0; i < numFiles; i++) {
 				this.mergefbr.add(FileUtil.this.read(null, filePathWOEnding+ "_chunk" + i + fileEnding, tempFileCompression));
 				this.filerows.add(null);
 				this.nextRow(i);
 				report.inc();
 			}
 
 			report.finish();
 		}
 		public String[] nextRow(final int fileIndex) {
 			// move to next line
 			if(this.mergefbr.get(fileIndex)!=null) {
 				String line = this.mergefbr.get(fileIndex).readLine();
 				
 				// eof
 				if(line==null) {
 					this.closeFile(fileIndex);
 					return null;
 				}
 				final StringTokenizer tokens = new StringTokenizer(line, delimiter);
 				final ArrayList<String> elements = new ArrayList<String>();
 				while(tokens.hasMoreTokens()) {
 					elements.add(tokens.nextToken());
 				}
 				final String[] row = elements.toArray(new String[elements.size()]);
 				// illegal row
 				if(row.length<this.minColumnCount)
 					this.nextRow(fileIndex);
 				else
 					this.filerows.set(fileIndex, row);
 			}
 			// get current line
 			return this.currentRow(fileIndex);
 		}
 		public String[] currentRow(final int fileIndex) {
 			final String[] row = this.filerows.get(fileIndex);
 			if(row!=null && row.length<this.minColumnCount)
 				logger.log(Level.WARNING, "unknown sort column for: " + Arrays.toString(row));
 			return row;
 		}
 		
 		private void closeFile(final int fileIndex) {
 			if(this.filerows.get(fileIndex)!= null) {
 				this.mergefbr.get(fileIndex).close();
 				this.mergefbr.set(fileIndex, null);
 				this.filerows.set(fileIndex, null);
 				this.emptyFileCount++;
 			}
 		}
 		
 		public void closeAllFiles() {
 			for (int i = 0; i < this.numOfFiles; i++) {
 				this.closeFile(i);
 			}
 		}
 		
 		public int getFileCount() {
 			return this.numOfFiles;
 		}
 		public boolean isOneFileFilled() {
 			return this.emptyFileCount<this.numOfFiles;
 		}
 	}
 
 	protected void mergeFiles(final String filePathWOEnding, final String fileEnding, int numFiles, final String delimiter, int compareIndex, int numOfRows, Compression outFilecompression) {
 		// ArrayList<FileReader> mergefr = new ArrayList<FileReader>();
 		ChunkFileManager chunkFiles = new ChunkFileManager(filePathWOEnding, fileEnding, delimiter, numFiles, compareIndex+1);
 		try {
 			PrintStream writer = this.write(null, filePathWOEnding + fileEnding, false, outFilecompression);
 
 			ProgressReport report = new ProgressReport("Merging files").setReport(1048576).setMax(numOfRows).setUnit("lines");
 
 			String[] row;
 			// int cnt = 0;
 			while (chunkFiles.isOneFileFilled()) {
 				String min = null;
 				int minIndex = -1;
 
 				// check which one is min
 				for (int i = 0; i < chunkFiles.getFileCount(); i++) {
 					row = chunkFiles.currentRow(i);
 					if (min != null) {
 						if (row != null && row[compareIndex].compareTo(min) < 0) {
 							minIndex = i;
 							min = row[compareIndex];
 						}
 					} else if (row != null) {
 							minIndex = i;
 							min = row[compareIndex];
 					}
 					
 				}
 
 				if (minIndex < 0) {
 					// all files are empty
 					break;
 				} else {
 					// write to the sorted file
 					writer.println(flattenArray(chunkFiles.currentRow(minIndex), delimiter));
 
 					// get another row from the file that had the min
 					chunkFiles.nextRow(minIndex);
 				}
 				
 				report.inc();
 			}
 
 			report.finish();
 
 			// close the output file
 			writer.close();
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			System.exit(-1);
 		} finally {
 			chunkFiles.closeAllFiles();
 		}
 	}
 }
