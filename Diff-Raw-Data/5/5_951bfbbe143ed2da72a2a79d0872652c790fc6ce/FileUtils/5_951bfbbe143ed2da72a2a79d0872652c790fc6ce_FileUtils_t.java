 package hemera.core.utility;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.JarInputStream;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 import java.util.zip.ZipEntry;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 /**
  * <code>FileUtils</code> defines the singleton utility
  * that provides various file operation methods.
  *
  * @author Yi Wang (Neakor)
  * @version 1.0.0
  */
 public enum FileUtils {
 	/**
 	 * The singleton instance.
 	 */
 	instance;
 
 	/**
 	 * Delete the target. If the target is a directory,
 	 * all the children directories and files are also
 	 * deleted.
 	 * @param target The <code>String</code> target.
 	 * @return <code>true</code> if target is deleted.
 	 * <code>false</code> otherwise.
 	 */
 	public boolean delete(final String target) {
 		final File file = new File(target);
 		if (!file.exists()) return false;
 		if (file.isDirectory()) {
 			final String[] children = file.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean success = this.delete(new File(target, children[i]).getAbsolutePath());
 				if (!success) return false;
 			}
 		}
 		return file.delete();
 	}
 	
 	/**
 	 * Copy the source file to the target file.
 	 * @param src The source <code>File</code> to copy.
 	 * @param target The target <code>File</code> to
 	 * copy to. This is not the target directory, but
 	 * the actual file.
 	 * @throws IOException If copying file failed.
 	 */
 	public void copyFile(final File src, final File target) throws IOException {
 		final InputStream input = new FileInputStream(src);
 		final OutputStream output = new FileOutputStream(target); 
 		final byte[] buffer = new byte[8192];
 		while (true) {
 			final int count = input.read(buffer);
 			if (count <= 0) break;
 			else output.write(buffer, 0, count);
 		}
 		input.close();
 		output.close();
 	}
 
 	/**
 	 * Copy the contents and the structure of the source
 	 * directory to the target location only including
 	 * the files with specified extension.
 	 * @param srcPath The <code>String</code> path to
 	 * the source to be copied.
 	 * @param targetPath The <code>String</code> path to
 	 * the target to copy to.
 	 * @param extension The <code>String</code> extension
 	 * to check. <code>null</code> if all files should be
 	 * included.
 	 * @throws IOException If file processing failed.
 	 */
 	public void copyFolder(final String srcPath, final String targetPath, final String extension) throws IOException{
 		final File src = new File(srcPath);
 		final File target = new File(targetPath);
 		// If source is a directory iterate all of the contents.
 		if (src.isDirectory()){
 			if (!target.exists()) target.mkdirs();
 			final String files[] = src.list();
 			for (int i = 0; i < files.length; i++) {
 				final File srcFile = new File(src, files[i]);
 				final File destFile = new File(target, files[i]);
 				this.copyFolder(srcFile.getAbsolutePath(), destFile.getAbsolutePath(), extension);
 			}
 		}
 		// If source is a file, copy the file.
 		else {
 			// Check extension.
 			final String validExtension = this.getValidExtension(extension);
 			final String srcName = src.getName();
 			if (!srcName.toLowerCase().endsWith(validExtension)) return;
 			// Copy.
 			this.copyFile(src, target);
 		}
 	}
 
 	/**
 	 * Read the contents of the file as a single string
 	 * value.
 	 * @param file The <code>File</code> to read.
 	 * @return The <code>String</code> of the file
 	 * contents.
 	 * @throws IOException If any file processing failed.
 	 */
 	public String readAsString(final File file) throws IOException {
 		BufferedReader input = null;
 		final StringBuilder builder = new StringBuilder();
 		try {
 			input = new BufferedReader(new FileReader(file));
 			final char[] buffer = new char[1024];
 			while (true) {
 				final int count = input.read(buffer);
 				if (count <= 0) break;
 				else {
 					final String str = String.valueOf(buffer, 0, count);
 					builder.append(str);
 				}
 			}
 		} finally {
 			if (input != null) input.close();
 		}
 		return builder.toString();
 	}
 
 	/**
 	 * Read the given file and parse it into a XML
 	 * document.
 	 * @param file The <code>File</code> to read.
 	 * @return The <code>Document</code> instance.
 	 * @throws IOException If reading file failed.
 	 * @throws SAXException If parsing file failed.
 	 * @throws ParserConfigurationException If
 	 * parsing file failed.
 	 */
 	public Document readAsDocument(final File file) throws IOException, SAXException, ParserConfigurationException {
 		return this.readAsDocument(new FileInputStream(file));
 	}
 
 	/**
 	 * Read the given input stream and parse it into
 	 * a XML document.
 	 * @param stream The <code>InputStream</code>
 	 * to read.
 	 * @return The <code>Document</code> instance.
 	 * @throws IOException If reading file failed.
 	 * @throws SAXException If parsing file failed.
 	 * @throws ParserConfigurationException If
 	 * parsing file failed.
 	 */
 	public Document readAsDocument(final InputStream stream) throws IOException, SAXException, ParserConfigurationException {
 		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		final DocumentBuilder builder = factory.newDocumentBuilder();
 		return builder.parse(stream);
 	}
 
 	/**
 	 * Write all the entries of the Jar file to the
 	 * specified directory.
 	 * @param jarFile The <code>File</code> to retrieve
 	 * entries from.
 	 * @param path The <code>String</code> directory
 	 * to write the entries to.
 	 * @return The <code>List</code> of all the entry
 	 * <code>File</code>. <code>null</code> if there
 	 * are none.
 	 * @throws IOException If any file processing failed.
 	 */
 	public List<File> writeAll(final File jarFile, final String path) throws IOException {
 		return this.writeAll(jarFile, path, null);
 	}
 
 	/**
 	 * Write all the entries of the Jar file to the
 	 * specified directory excluding the ones included
 	 * in the given list.
 	 * @param jarFile The <code>File</code> to retrieve
 	 * entries from.
 	 * @param path The <code>String</code> directory
 	 * to write the entries to.
 	 * @param exclusion The <code>List</code> of all
 	 * <code>File</code> to exclude.
 	 * @return The <code>List</code> of all the entry
 	 * <code>File</code>. <code>null</code> if there
 	 * are none.
 	 * @throws IOException If any file processing failed.
 	 */
 	public List<File> writeAll(final File jarFile, final String path, final List<File> exclusion) throws IOException {
 		final int size = (exclusion==null) ? 0 : exclusion.size();
 		final List<File> list = new ArrayList<File>();
 		final JarFile jar = new JarFile(jarFile);
 		// Iterate over all entries.
 		JarInputStream input = null;
 		try {
 			input = new JarInputStream(new FileInputStream(jarFile));
 			ZipEntry entry = input.getNextEntry();
 			while (entry != null) {
 				// Check exclusion.
 				// Use explicit slash here since this is within the Jar.
 				final String entryName = entry.getName();
 				boolean contains = false;
 				final int index = entryName.lastIndexOf("/")+1;
 				final String entryFilename = entryName.substring(index);
 				for (int i = 0; i < size; i++) {
 					if (exclusion.get(i).getName().equalsIgnoreCase(entryFilename)) {
 						contains = true;
 						break;
 					}
 				}
 				// Only write if doesn't contain.
 				if (!contains) {
 					final File file = FileUtils.instance.writeToFile(jar, entry.getName(), path);
 					list.add(file);
 				}
 				entry = input.getNextEntry();
 			}
 		} finally {
 			if (input != null) input.close();
 		}
 		if (list.isEmpty()) return null;
 		return list;
 	}
 
 	/**
 	 * Write the given content as a file at the given
 	 * target location. If the given target is an
 	 * existing file, the contents of that file will
 	 * be over-written.
 	 * @param content The <code>String</code> content.
 	 * @param target The <code>String</code> target
 	 * file to write to.
 	 * @return The <code>File</code> written to.
 	 * @throws IOException If any file processing failed.
 	 */
 	public File writeAsString(final String content, final String target) throws IOException {
 		// Delete existing.
 		final File file = new File(target);
 		file.delete();
 		file.createNewFile();
 		// Write contents.
 		BufferedWriter writer = null;
 		try {
 			writer = new BufferedWriter(new FileWriter(file));
 			writer.write(content);
 		} finally {
 			if (writer != null) writer.close();
 		}
 		return file;
 	}
 
 	/**
 	 * Write the Jar entry with specified name from
 	 * the given Jar file to the target location.
 	 * <p>
 	 * This method creates the necessary directories
 	 * for the target file.
 	 * @param jar The <code>JarFile</code> to retrieve
 	 * the entry from.
 	 * @param entryName The <code>String</code> name of
 	 * the entry, which is also the name of the output
 	 * file.
 	 * @param path The <code>String</code> directory
 	 * to write the file to.
 	 * @return The created <code>File</code>. If the
 	 * operation failed, <code>null</code>.
 	 * @throws IOException If any file processing failed.
 	 */
 	public File writeToFile(final JarFile jar, final String entryName, final String path) throws IOException {
 		final String filePath = this.getValidDir(path); 
 		// Create necessary directories.
 		final File dir = new File(filePath);
 		dir.mkdirs();
 		// Retrieve the entry.
 		final ZipEntry entry = jar.getEntry(entryName);
 		// Delete and create target file.
 		// Explicitly use slash here since entry path is platform independent.
 		final int index = entryName.lastIndexOf("/")+1;
 		final String targetName = entryName.substring(index);
 		final String targetPath = filePath + targetName;
 		final File target = new File(targetPath);
 		target.delete();
 		target.createNewFile();
 		// Write to file.
 		FileOutputStream output = null;
 		InputStream input = null;
 		try {
 			output = new FileOutputStream(target);
 			input = jar.getInputStream(entry);
 			final byte[] buffer = new byte[8192];
 			while (true) {
 				final int count = input.read(buffer);
 				if (count <= 0) break;
 				else output.write(buffer, 0, count);
 			}
 		} finally {
 			if (output != null) output.close();
 			if (input != null) input.close();
 		}
 		return target;
 	}
 
 	/**
 	 * Write the given XML document to the target.
 	 * The old file will be over-written.
 	 * @param document The <code>Document</code> to
 	 * be written.
 	 * @param target The <code>String</code> target
 	 * file to write to.
 	 * @return The <code>File</code> instance.
 	 * @throws IOException If file processing failed.
 	 * @throws TransformerException If writing the
 	 * XML document failed.
 	 */
 	public File writeDocument(final Document document, final String target) throws IOException, TransformerException {
 		// Prepare the DOM document for writing.
 		final Source source = new DOMSource(document);
 		// Prepare the output file.
 		final File file = new File(target);
 		file.delete();
 		file.createNewFile();
 		final Result result = new StreamResult(file);
 		// Write the DOM document to the file.
 		final Transformer transformer = TransformerFactory.newInstance().newTransformer();
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 		transformer.transform(source, result);
 		return file;
 	}
 
 	/**
 	 * Create a new Jar file using the given files and
 	 * save the Jar file at the target location.
 	 * <p>
 	 * This method includes an empty manifest file in
 	 * the final Jar file.
 	 * @param files The <code>List</code> of all the
 	 * <code>File</code> to be jarred.
 	 * @param target The <code>String</code> path to
 	 * store the new Jar file.
 	 * @return The new Jar <code>File</code>.
 	 * @throws IOException If any file processing failed.
 	 */
 	public File jarFiles(final List<File> files, final String target) throws IOException {
 		return this.jarFiles(files, target, new Manifest());
 	}
 
 	/**
 	 * Create a new Jar file using the given files and
 	 * save the Jar file at the target location.
 	 * @param files The <code>List</code> of all the
 	 * <code>File</code> to be jarred.
 	 * @param target The <code>String</code> path to
 	 * store the new Jar file.
 	 * @param manifest The <code>Manifest</code> to be
 	 * included in the Jar file.
 	 * @return The new Jar <code>File</code>.
 	 * @throws IOException If any file processing failed.
 	 */
 	public File jarFiles(final List<File> files, final String target, final Manifest manifest) throws IOException {
 		// Delete and create new file.
 		final File jarfile = new File(target);
 		jarfile.delete();
 		jarfile.createNewFile();
 		// Create a new output stream for the Jar file.
 		JarOutputStream output = null;
 		try {
 			output = new JarOutputStream(new FileOutputStream(jarfile), manifest);
 			// Add all files as Jar entries.
 			final int size = files.size();
 			for (int i = 0; i < size; i++) {
 				final File file = files.get(i);
 				// Extract the initial path to exclude from the Jar structure.
 				String filePath = null;
 				if (file.isDirectory()) filePath = FileUtils.instance.getValidDir(file.getAbsolutePath());
 				else filePath = file.getAbsolutePath();
 				final int index = filePath.lastIndexOf("/")+1;
 				final String initPath = filePath.substring(0, index);
 				// Write entry.
 				this.writeJarEntry(file, output, initPath);
 			}
 		} finally {
 			if (output != null) output.close();
 		}
 		return jarfile;
 	}
 
 	/**
 	 * Write the given source file as a Jar entry into
 	 * the given Jar output stream.
 	 * @param source The source <code>File</code> to be
 	 * written as a Jar entry into the given target.
 	 * @param target The <code>JarOutputStream</code>
 	 * to write to.
 	 * @param initPath The initial path to exclude from
 	 * the directory structure within the Jar.
 	 * @throws IOException If any file processing failed.
 	 */
 	private void writeJarEntry(final File source, final JarOutputStream target, final String initPath) throws IOException {
 		if (source.isDirectory()) {
 			// Use explicit slash here since this is within Jar.
 			final String sourcePath = FileUtils.instance.getValidDir(source.getAbsolutePath());
 			String sourceName = sourcePath.replace(initPath, "").replace("\\", "/");
 			if (!sourceName.isEmpty()) {
 				if (!sourceName.endsWith("/")) sourceName += "/";
 				final JarEntry entry = new JarEntry(sourceName);
 				entry.setTime(source.lastModified());
 				target.putNextEntry(entry);
 				target.closeEntry();
 			}
 			// Write children.
 			final File[] children = source.listFiles();
 			for (int i = 0; i < children.length; i++) {
 				this.writeJarEntry(children[i], target, initPath);
 			}
 		} else {
 			BufferedInputStream input = null;
 			try {
 				// Write the file contents.
 				final JarEntry entry = new JarEntry(source.getAbsolutePath().replace(initPath, "").replace("\\", "/"));
 				entry.setTime(source.lastModified());
 				target.putNextEntry(entry);
 				input = new BufferedInputStream(new FileInputStream(source));
 				final byte[] buffer = new byte[8192];
 				while (true) {
 					final int count = input.read(buffer);
 					if (count <= 0) break;
 					else target.write(buffer, 0, count);
 				}
 				target.closeEntry();
 			} finally {
 				if (input != null) input.close();
 			}
 		}
 	}
 
 	/**
 	 * Retrieve the valid directory path string of the
 	 * given path.
 	 * @param path The <code>String</code> path.
 	 * @return The valid <code>String</code> directory
 	 * path with the proper separator.
 	 */
 	public String getValidDir(final String path) {
 		if (path.endsWith(File.separator)) return path;
 		else return path + File.separator;
 	}
 
 	/**
 	 * Retrieve the current executing Jar file path
 	 * on the local file system.
 	 * @return The current Jar <code>File</code>.
 	 */
 	public File getCurrentJarFile() {
 		// Retrieve the resource of this class.
 		// Explicitly use slash here since resource path is platform independent.
 		final String classResource = this.getClass().getName().replace(".", "/") + ".class";
 		final URL classUrl = this.getClass().getClassLoader().getResource(classResource);
 		final String classUrlPath = classUrl.getFile();
 		// Only use the actual path portion excluding any descriptor.
 		// Explicitly use slash here since resource path is platform independent.
 		final int start = classUrlPath.indexOf("/");
 		final int index = classUrlPath.lastIndexOf("/")+1;
 		final String path = classUrlPath.substring(start, index);
 		// Find the Jar file portion.
 		final int jarIndex = path.indexOf("!");
 		final String jarPath = path.substring(0, jarIndex).replace("/", File.separator);
 		return new File(jarPath);
 	}
 
 	/**
 	 * Retrieve the current execution Jar file directory.
 	 * @return The <code>String</code> directory path.
 	 */
 	public String getCurrentJarDirectory() {
 		final File jar = this.getCurrentJarFile();
 		final String jarPath = jar.getAbsolutePath();
 		final int index = jarPath.lastIndexOf(File.separator);
 		return jarPath.substring(0, index+1);
 	}
 
 	/**
 	 * Retrieve all files from the specified directory
 	 * including all sub-directories.
 	 * @param rootDir The <code>String</code> directory
 	 * to search from.
 	 * @return The <code>List</code> of all the
 	 * <code>File</code>. <code>null</code> if there
 	 * are none.
 	 */
 	public List<File> getFiles(final String rootDir) {
 		return this.getFiles(rootDir, null);
 	}
 
 	/**
 	 * Retrieve the files that have the file extension
 	 * matching the given one, from the specified
 	 * directory including all sub-directories 
 	 * @param rootDir The <code>String</code> directory
 	 * to search from.
 	 * @param extension The <code>String</code> file
 	 * extension to search for. <code>null</code> if
 	 * all file types should be retrieved.
 	 * @return The <code>List</code> of all matching
 	 * <code>File</code>. <code>null</code> if there
 	 * are none.
 	 */
 	public List<File> getFiles(final String rootDir, final String extension) {
 		final String validExtension = this.getValidExtension(extension);
 		// Check root directory is a directory.
 		final File root = new File(rootDir);
 		if (!root.isDirectory()) return null;
 		// Recursively find all files.
 		final List<File> files = new ArrayList<File>();
 		final File[] list = root.listFiles();
 		for (int i = 0; i < list.length; i++) {
 			final File file = list[i];
 			// Recursive.
 			if (file.isDirectory()) {
				final List<File> results = this.getFiles(file.getAbsolutePath(), extension);
 				files.addAll(results);
 			} else {
 				// Check extension.
 				if (extension == null) files.add(file);
 				else if (file.getAbsolutePath().toLowerCase().endsWith(validExtension)) files.add(file);
 			}
 		}
 		return files;
 	}
 
 	/**
 	 * Retrieve the valid extension including the dot
 	 * separator.
 	 * @param extension The <code>String</code> value
 	 * to parse.
 	 * @return The <code>String</code> valid extension.
 	 * This value is never <code>null</code>. If the
 	 * given value is <code>null</code>, then an empty
 	 * string is returned.
 	 */
 	public String getValidExtension(final String extension) {
		if (extension == null || extension.trim().isEmpty()) return "";
 		else {
 			if (extension.startsWith(".")) return extension.toLowerCase();
 			else return "."+extension.toLowerCase();
 		}
 	}
 }
