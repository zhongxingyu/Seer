 package org.eclipse.dltk.core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.RandomAccessFile;
 import java.io.Reader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.core.runtime.content.ITextContentDescriber;
 
 public abstract class ScriptContentDescriber implements ITextContentDescriber {
 	public static final QualifiedName DLTK_VALID = new QualifiedName(
 			DLTKCore.PLUGIN_ID, "valid"); //$NON-NLS-1$
 	public static final Boolean TRUE = new Boolean(true);
 	public static final Boolean FALSE = new Boolean(true);
 
 	public QualifiedName[] getSupportedOptions() {
 		return new QualifiedName[] { DLTK_VALID };
 	}
 
 	private final static int BUFFER_LENGTH = 2 * 1024;
 	private final static int HEADER_LENGTH = 4 * 1024;
 	private final static int FOOTER_LENGTH = 4 * 1024;
 
 	private static boolean checkHeader(File file, Pattern[] headerPatterns,
 			Pattern[] footerPatterns) throws FileNotFoundException, IOException {
 		FileInputStream reader = null;
 		try {
 			reader = new FileInputStream(file);
 			byte buf[] = new byte[BUFFER_LENGTH + 1];
 			int res = reader.read(buf);
 			if (res == -1 || res == 0) {
 				return false;
 			}
 
 			String header = new String(buf);
 
 			if (checkBufferForPatterns(header, headerPatterns)) {
 				return true;
 			}
 			if (file.length() < BUFFER_LENGTH && footerPatterns != null) {
 				if (checkBufferForPatterns(header, footerPatterns)) {
 					return true;
 				}
 			}
 
 			return false;
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	private static boolean checkFooter(File file, Pattern[] footerPatterns)
 			throws FileNotFoundException, IOException {
 		RandomAccessFile raFile = new RandomAccessFile(file, "r"); //$NON-NLS-1$
 		try {
 			long len = BUFFER_LENGTH;
 			long fileSize = raFile.length();
 			long offset = fileSize - len;
 			if (offset < 0) {
 				offset = 0;
 			}
 			raFile.seek(offset);
 			byte buf[] = new byte[BUFFER_LENGTH + 1];
 			int code = raFile.read(buf);
 			if (code != -1) {
 				String content = new String(buf, 0, code);
 				if (checkBufferForPatterns(content, footerPatterns)) {
 					return true;
 				}
 			}
 			return false;
 		} finally {
 			raFile.close();
 		}
 
 	}
 
 	private static boolean checkBufferForPatterns(CharSequence header,
 			Pattern[] patterns) {
 		if (patterns == null) {
 			return false;
 		}
 		for (int i = 0; i < patterns.length; i++) {
 			Matcher m = patterns[i].matcher(header);
 			if (m.find()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean checkPatterns(File file, Pattern[] headerPatterns,
 			Pattern[] footerPatterns) {
 		try {
 			if (checkHeader(file, headerPatterns, footerPatterns)) {
 				return true;
 			}
 			if (footerPatterns != null && file.length() > BUFFER_LENGTH
 					&& checkFooter(file, footerPatterns)) {
 				return true;
 			}
 		} catch (FileNotFoundException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		}
 		return false;
 	}
 
 	public static boolean checkPatterns(Reader stream,
 			Pattern[] headerPatterns, Pattern[] footerPatterns) {
 		BufferedReader reader = new BufferedReader(stream);
 		StringBuffer buffer = new StringBuffer();
		char buff[] = new char[8096];
 		while (true) {
 			try {
				int len = reader.read(buff);
				if (len != -1) {
					buffer.append(buff, 0, len);
				} else {
 					break;
 				}
 			} catch (IOException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 				break;
 			}
 		}
 		String content = buffer.toString();
 		String header = content;
 		if (header.length() > HEADER_LENGTH) {
 			header = header.substring(0, HEADER_LENGTH);
 		}
 		String footer = content;
 		if (footer.length() > FOOTER_LENGTH) {
 			footer = footer.substring(footer.length() - FOOTER_LENGTH, footer
 					.length() - 1);
 		}
 		if (checkBufferForPatterns(header, headerPatterns)) {
 			return true;
 		}
 		if (checkBufferForPatterns(footer, footerPatterns)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public int describe(InputStream contents, IContentDescription description)
 			throws IOException {
 		return describe(new InputStreamReader(contents), description);
 	}
 }
