 package ru.imobilco.builder.ant.css;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import ru.imobilco.builder.ant.FileEntity;
 
 public class CSSCatalog {
 	private File parentFile;
 	private File webRoot;
 	private String combinedCSS;
 	private List<FileEntity> catalogFiles;
 	private String encoding = "UTF-8";
 	
 	private static final Pattern reImport = Pattern.compile("@import\\s+(?:url\\(\\s*)?[\"']?([\\w\\\\\\/\\-\\:\\.]*?_[\\w\\.\\-]+\\.css)[\"']?\\)?\\s*\\;?");
 	
 	public CSSCatalog(File parentFile, File webRoot) {
 		this.webRoot = webRoot;
 		this.setParentFile(parentFile);
 	}
 	
 	public CSSCatalog(File parentFile) {
 		this(parentFile, parentFile.getParentFile());
 	}
 	
 	public CSSCatalog(String parentFile) {
 		this(new File(parentFile));
 	}
 	
 	/**
 	 * Recursive function for parsing CSS files and storing all import'ed files
 	 * in internal catalog
 	 * @param ctx
 	 * @return CSS file content with imports replaced by their file values
 	 * @throws IOException
 	 */
 	private String createCatalog(File ctx) throws IOException {
 		StringBuffer sb = new StringBuffer();
 		String content = readFile(ctx);
 		Matcher matcher = reImport.matcher(content);
 		while (matcher.find()) {
 			String filteredContent = addToCatalog(ctx, matcher.group(1));
 			if (filteredContent == null)
 				filteredContent = matcher.group(0);
 			
			matcher.appendReplacement(sb, Matcher.quoteReplacement(filteredContent));
 		}
 		
 		matcher.appendTail(sb);
 		return sb.toString();
 	}
 	
 	private String addToCatalog(File ctx, String child) throws IOException {
 		File baseDir = ctx.getParentFile();
 		
 		File tmpFile = new File(child);
 		if (tmpFile.isAbsolute()) {
 			String[] arr = child.split(File.separator, 2);
 			child = arr[arr.length - 1];
 			baseDir = webRoot;
 		}
 		
 		File childFile = new File(baseDir, child);
 		
 		if (childFile.exists() && getCatalogIndex(childFile) == -1) {
 			catalogFiles.add(new FileEntity(baseDir.getAbsolutePath(), child));
 			return createCatalog(childFile);
 		}
 		
 		return null;
 	}
 	
 	public int getCatalogIndex(File file) {
 		for (int i = 0; i < catalogFiles.size(); i++) {
 			if (catalogFiles.get(i).getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
 				return i;
 			}
 		}
 		
 		return -1;
 	}
 	
 	private String readFile(File file) throws IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), getEncoding()));
 		
 		String line = null;
 		StringBuilder stringBuilder = new StringBuilder();
 		String ls = System.getProperty("line.separator");
 		while ((line = reader.readLine()) != null) {
 			stringBuilder.append(line);
 			stringBuilder.append(ls);
 		}
		
		reader.close();
 		return stringBuilder.toString();
 	}
 
 	public File getParentFile() {
 		return parentFile;
 	}
 
 	public void setParentFile(File parentFile) {
 		this.parentFile = parentFile;
 		catalogFiles = new ArrayList<FileEntity>();
 		
 		combinedCSS = "";
 		try {
 			// add parent file to catalog, staring at web root
 			catalogFiles.add(new FileEntity(parentFile.getParent(), parentFile.getName()));
 			combinedCSS = createCatalog(parentFile);
 		} catch (Exception e) {}
 	}
 	
 	public String getCombinedCSS() {
 		return combinedCSS;
 	}
 	
 	public List<FileEntity> getCatalogFiles() {
 		return catalogFiles;
 	}
 
 	public String getEncoding() {
 		return encoding;
 	}
 
 	public void setEncoding(String encoding) {
 		this.encoding = encoding;
 	}
 }
