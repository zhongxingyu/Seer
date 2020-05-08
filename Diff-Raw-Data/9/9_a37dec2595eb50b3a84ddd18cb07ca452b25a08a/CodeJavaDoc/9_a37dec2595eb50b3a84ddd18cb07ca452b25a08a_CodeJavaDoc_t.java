 package com.github.podal.codejavadoc;
 
 import static com.github.podal.codejavadoc.CodeConstants.END_STRING;
 import static com.github.podal.codejavadoc.CodeConstants.START_STRING;
 import static com.github.podal.codejavadoc.util.FileUtil.file;
 import static com.github.podal.codejavadoc.util.StringUtil.cutLast;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.github.podal.codejavadoc.util.FileUtil.Encoding;
 import com.github.podal.codejavadoc.util.FileUtil.LineCallback;
 import com.github.podal.codejavadoc.util.MapUtil;
 
 public class CodeJavaDoc {
 
 	private static final FileFilter JAVAFILE_FILTER = new FileFilter() {
 		@Override
 		public boolean accept(File file) {
 			return file.isDirectory() || file.getName().endsWith(".java");
 		}
 	};
 
 	public static interface Log {
 
 		void log(String string);
 
 	}
 
 	public static interface HandleFile {
 
 		void handleFile(final File tmpDir, File file, File createdFile);
 
 	}
 
 	public static void main(String[] args) throws IOException {
 		String fileName = null;
 		final String encoding;
 
 		if (args.length == 1) {
 			fileName = args[0];
 			encoding = "utf-8";
 		} else if (args.length == 3 && args[0].equals("-e")) {
 			encoding = args[1];
 			fileName = args[2];
 		} else {
 			encoding = null;
 		}
 
 		if (fileName == null) {
 			printUsage();
 		} else {
 			File[] files = { new File(fileName) };
 			File tmpFile = new File("tmp");
 			if (tmpFile.exists() && !tmpFile.isDirectory()) {
 				throw new RuntimeException("Can't create tmp dir.");
 			} else if (!tmpFile.exists()) {
 				tmpFile.mkdir();
 			}
 			doCodeToJavaDoc(files, new Encoding() {
 
 				@Override
 				public String getEncoding(File file) {
 					return encoding;
 				}
 			}, tmpFile, new Log() {
 
 				@Override
 				public void log(String line) {
 					System.out.println(line);
 				}
 			});
 		}
 	}
 
 	private static void printUsage() {
 		System.out.println("Usage: " + CodeJavaDoc.class.getName() + " [-e <encoding>] src_dir");
 		System.out.println("note: if encoding is omitted UTF-8 is used by default");
 	}
 
	public static void doCodeToJavaDoc(File[] files, Encoding encoding, final File tmpDir, Log log) throws IOException,
			FileNotFoundException {
 		ClassInfoFetcher fetcher = new ClassInfoFetcher();
 		for (File file : files) {
 			file(file, encoding, fetcher, JAVAFILE_FILTER);
 		}
 		updateJavaFiles(encoding, tmpDir, log, fetcher, new HandleFile() {
 
 			@Override
 			public void handleFile(File tmpDir, File file, File createdFile) {
 				File tmpFile = new File(tmpDir, file.getName() + "." + System.currentTimeMillis());
 				if (!file.renameTo(tmpFile)) {
 					throw new RuntimeException("Error renaming file " + file + " to " + tmpFile);
 				} else if (!createdFile.renameTo(file)) {
 					throw new RuntimeException("Error renaming file " + createdFile + " to " + file);
 				}
 			}
 		});
 	}
 
 	public static void updateJavaFiles(Encoding encoding, final File tmpDir, Log log, ClassInfoFetcher fetcher,
 			HandleFile handleFile) throws IOException {
 		final Map<String, CodeSection> includeMethods = fetcher.getIncludedMethodsCodeInfo(encoding);
 		for (Entry<String, List<JavaDocSection>> en : fetcher.getJavaDocMap().entrySet()) {
 			final List<JavaDocSection> value = en.getValue();
 			doFile(encoding, tmpDir, log, handleFile, includeMethods, value);
 		}
 	}
 
 	public static void doFile(Encoding encoding, final File tmpDir, Log log, HandleFile handleFile,
 			final Map<String, CodeSection> includeMethods, final List<JavaDocSection> value)
 			throws UnsupportedEncodingException, FileNotFoundException, IOException {
 		File file = value.get(0).getFile();
 		if (log != null) {
 			log.log(file.toString());
 		}
 		File createdFile = new File(tmpDir, file.getName() + "." + System.currentTimeMillis());
 		final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(createdFile),
 				encoding.getEncoding(file)));
 		final Map<String, String> result = new LinkedHashMap<String, String>();
 		final Set<JavaDocSection> set = new HashSet<JavaDocSection>();
 		file(file, encoding, new LineCallback() {
 			@Override
 			public void line(File file, int lineCount, String line) {
 				JavaDocSection jdSection = getSection(lineCount, value);
 				if (jdSection == null) {
 					out.println(line);
 				} else if (!set.contains(jdSection)) {
 					CodeSection cSection = includeMethods.get(jdSection.getIncludeClassName());
					if (cSection == null) { // Can't find method keep lines. 
						out.println(line);
					} else if (!cSection.getMd5().equals(jdSection.getMd5())) {
 						result.put(jdSection.getIncludeClassName(), "Update");
 						out.printf(START_STRING, jdSection.getIncludeClassName(), cSection.getMd5());
 						for (String row : cSection.getRows()) {
 							out.println(row);
 						}
 						out.printf(END_STRING);
 						set.add(jdSection);
 					} else {
 						result.put(jdSection.getIncludeClassName(), "Keep");
 						out.println(line);
 					}
 				}
 			}
 		});
 		if (log != null) {
 			for (Entry<String, String> en2 : result.entrySet()) {
 				log.log("\t" + en2.getKey() + " [" + en2.getValue() + "]");
 			}
 		}
 		out.close();
 		if (MapUtil.findFirstKeyToValue(result, "Update") != null) {
 			handleFile.handleFile(tmpDir, file, createdFile);
 		}
 	}
 
 	private static JavaDocSection getSection(int lineCount, List<JavaDocSection> sections) {
 		for (JavaDocSection section : sections) {
 			if (lineCount >= section.getStart() && lineCount <= section.getEnd()) {
 				return section;
 			}
 		}
 		return null;
 	}
 
 	public static String removeMethodName(String name) {
 		return cutLast(name, '.');
 	}
 
 }
