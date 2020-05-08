 package it.uninsubria.generic.web;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.simpleframework.http.Request;
 import org.simpleframework.http.Response;
 import org.simpleframework.http.core.Container;
 
 public class StaticAssetContainer implements Container {
 	public static Map<String, String> contentTypes = new HashMap<String, String>();
 	
 	public static String WORKING_DIRECTORY = System.getProperty("user.dir");
 	public static File ASSETS_DIRECTORY = new File(WORKING_DIRECTORY, "public");
 	private final Container nextContainer;
 	
 	public StaticAssetContainer(Container nextContainer) {
 		contentTypes.put("css", "text/css");
 		contentTypes.put("png", "image/png");
 		contentTypes.put("txt", "text/plain");
 		contentTypes.put("html", "text/html");
		
 		this.nextContainer = nextContainer;
 	}
 
 	@Override
 	public void handle(Request request, Response response) {
 		System.out.println("Url richiesta: " + request.getPath());
 		File toHandle = new File(ASSETS_DIRECTORY, request.getPath().toString());
 		if (toHandle.exists() && !toHandle.isDirectory()) {
 			addContentType(toHandle, response);
 			addContentLength(toHandle, response);
 			copy(toHandle, response);
 		} else {
 			nextContainer.handle(request, response);
 		}
 	}
 
 	private void addContentType(File toHandle, Response response) {
 		response.add("Content-Type", contentTypes.get(suffix(toHandle)));
 	}
 
 	private String suffix(File file) {
 		String path = file.getAbsolutePath();
 		return path.substring(path.lastIndexOf(".")+1);
 	}
 
 	private void addContentLength(File toHandle, Response response) {
 		response.setContentLength((int) toHandle.length());
 	}
 
 	private void copy(File file, Response response) {
 		InputStream inputStream = null;
 		OutputStream outputStream = null;
 		try {
 			inputStream = new FileInputStream(file);
 			outputStream = response.getOutputStream();
 			copy(inputStream, outputStream);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			close(inputStream);
 			close(outputStream);
 		}
 		
 	}
 
 	private void close(OutputStream outputStream) {
 		try {
 			if (null != outputStream)
 				outputStream.close();
 		} catch (IOException ignored) {
 		}
 		
 	}
 
 	private void close(InputStream inputStream) {
 		if (null != inputStream) {
 			try {
 				inputStream.close();
 			} catch (IOException ignored) {}
 		}
 	}
 
 	private void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
 		byte[] buffer = new byte[8192];
 		int read = inputStream.read(buffer);
 		while (read > 0) {
 			outputStream.write(buffer, 0, read);
 			read = inputStream.read(buffer);
 		}
 	}
 
 }
