 package yuuki.file;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.zip.ZipFile;
 
 /**
  * Loads a file directly into a byte array.
  */
 public class ByteArrayLoader extends ResourceLoader {
 	
 	/**
 	 * Creates a new ByteArrayLoader for resource files at the specified
 	 * location.
 	 * 
 	 * @param directory The directory containing the resource files to be
 	 * loaded.
 	 */
 	public ByteArrayLoader(File directory) {
 		super(directory);
 	}
 	
 	/**
 	 * Creates a new ByteArrayLoader for resource files in the given ZIP file.
 	 *
 	 * @param archive The ZIP file containing the resource files to be loaded.
 	 * @param zipRoot The root within the ZIP file of all files to be loaded.
 	 */
 	public ByteArrayLoader(ZipFile archive, String zipRoot) {
 		super(archive, zipRoot);
 	}
 	
 	/**
 	 * Loads the data from a file.
 	 * 
 	 * @param resource The location of the file to load, relative to the
 	 * resource root.
 	 * 
 	 * @return A byte array with the bytes from the file.
 	 * 
 	 * @throws ResourceNotFoundException If the resource does not exist.
 	 * @throws IOException If an IOException occurs.
 	 */
 	public byte[] load(String resource) throws ResourceNotFoundException,
 	IOException {
		InputStream s = getStream(resource);
 		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 		int n = 0;
 		while ((n = s.read()) != -1) {
 			buffer.write(n);
 		}
 		buffer.flush();
 		buffer.close();
 		advanceProgress(1.0);
 		return buffer.toByteArray();
 	}
 	
 }
