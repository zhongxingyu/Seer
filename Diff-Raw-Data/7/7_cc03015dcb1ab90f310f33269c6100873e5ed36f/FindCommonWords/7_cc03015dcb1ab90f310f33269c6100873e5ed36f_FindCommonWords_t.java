 /**
  * workshop for i-docs. 
  */
 package gr.elchetz.idocs;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.google.code.externalsorting.ExternalSort;
 
 /**
  * @author elchetz
  * 
  */
 public class FindCommonWords {
 
 	private static final String SORTED_SUFFIX = ".sorted";
 	private static final int lineBufferSize = 1000;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args.length < 3) {
 			System.out
 					.println("Usage: java FindCommonWords file1 file2 outFile");
 		} else {
 
 			long start = System.currentTimeMillis();
 
 			Comparator<String> comparator = new Comparator<String>() {
 				public int compare(String r1, String r2) {
 					return r1.compareTo(r2);
 				}
 			};
 
 			List<File> l;
 			try {
 				for (int i = 0; i < 2; i++) {
 					l = ExternalSort.sortInBatch(new File(args[i]), comparator);
 					ExternalSort.mergeSortedFiles(l, new File(args[i]
 							+ SORTED_SUFFIX), comparator);
 				}
 
 			} catch (IOException e1) {
 				e1.printStackTrace();
 				System.exit(1);
 			}
 
 			System.out.println("File Sorting Time: "
 					+ (System.currentTimeMillis() - start) + "ms");
 
 			start = System.currentTimeMillis();
 
 			File shortFile = new File(args[0] + SORTED_SUFFIX);
 			File longFile = new File(args[1] + SORTED_SUFFIX);
 			FindCommonWords findCommonWords = new FindCommonWords();
 
 			try {
 				BufferedWriter out = new BufferedWriter(new FileWriter(args[2]));
 				findCommonWords.findCommonWords(shortFile, longFile,
 						lineBufferSize, out);
 				out.close();
 			} catch (IOException e) {
 				System.err.println("Ooops!");
 				e.printStackTrace();
 			}
 
 			System.out.println("File Processing: "
 					+ (System.currentTimeMillis() - start) + "ms");
 
 		}
 
 	}
 
 	/**
 	 * Finds common words in two files which contain one word per line.
 	 * 
 	 * @param file1
 	 *            The first file.
 	 * @param file2
 	 *            The second file.
 	 * @param bufferSize
 	 *            The size of the {@link LineBuffer}s used.
 	 * @param writer
 	 *            The writer to use in order to write common words.
 	 * @throws IOException
 	 *             In case reading the files fails.
 	 * @since v0.1.0
 	 */
 	public void findCommonWords(File file1, File file2, int bufferSize,
 			Writer writer) throws IOException {
 		String lineSep = System.getProperty("line.separator");
		String lastWordFound = null;
 		LineBuffer lineBuff1 = new LineBufferImpl(file1, bufferSize);
 		LineBuffer lineBuff2 = new LineBufferImpl(file2, bufferSize);
 		while (lineBuff1.hasMore() || lineBuff2.hasMore()) {
 			Set<String> intersection = new TreeSet<String>(
 					lineBuff1.getContents());
 			intersection.retainAll(lineBuff2.getContents());
 			for (String word : intersection) {
				if (!word.equals(lastWordFound)) {
					writer.write(word + lineSep);
					lastWordFound = word;
				}
 			}
 			int compare = lineBuff1.compareTo(lineBuff2);
 			if (compare == 0) {
 				lineBuff1.next();
 				lineBuff2.next();
 			} else if (compare < 0) {
 				lineBuff1.next();
 			} else {
 				lineBuff2.next();
 			}
 		}
 	}
 }
