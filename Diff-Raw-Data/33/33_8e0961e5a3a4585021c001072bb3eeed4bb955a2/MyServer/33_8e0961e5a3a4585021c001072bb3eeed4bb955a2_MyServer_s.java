 package myserver.server;
 
 import java.util.*;
 import java.io.*;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.*;
 import java.util.concurrent.*;
 import java.util.zip.GZIPOutputStream;
 
 import myserver.fastcgi.FastCgi;
 import myserver.util.Util;
 
 
 /**
  * MyServer class
  * @author Fan Yang
  *
  */
 public class MyServer {
 	
 	private final String HOST;
 	private final int PORT;
 	private final String DOC_ROOT;
 	private final String INDEX_PAGE;
 	private final String ERROR_PAGE;
 	private final int REQUEST_LENGTH;
 	private final long FILE_CACHE;
 	private final int FILE_SIZE;
 	private final int THREAD_POOL;
 	private final int BACKLOG;
 	
 
 	private AsynchronousServerSocketChannel serverSocketChannel;
 	
 	Map<String, byte[]> files = new HashMap<String, byte[]>();
 	Map<String, byte[]> headers = new HashMap<String, byte[]>();
 	List<String> filePathesList = new LinkedList<String>();
 	long currentCache = 0;
 	
 	private FastCgi fastCgi;
 	
 
 	/**
 	 * Init MyServer
 	 * @param properties
 	 * @throws Exception
 	 */
 	public MyServer(Properties properties) throws Exception {
 		
 		HOST = properties.getProperty("HOST");
 		PORT = Integer.valueOf(properties.getProperty("PORT"));
 		DOC_ROOT = properties.getProperty("DOC_ROOT");
 		INDEX_PAGE = properties.getProperty("INDEX_PAGE");
 		ERROR_PAGE = properties.getProperty("ERROR_PAGE");
 		REQUEST_LENGTH = Integer.valueOf(properties.getProperty("REQUEST_LENGTH"));
 		FILE_CACHE = Long.valueOf(properties.getProperty("FILE_CACHE"));
 		FILE_SIZE = Integer.valueOf(properties.getProperty("FILE_SIZE"));
 		THREAD_POOL = Integer.valueOf(properties.getProperty("THREAD_POOL"));
 		BACKLOG = Integer.valueOf(properties.getProperty("BACKLOG"));
 		
 		
 		generateHttpHeaders();
 		files.put(INDEX_PAGE, Util.concateByteArray(headers.get("html"), loadFile(INDEX_PAGE)));
 		String errorHeader = "HTTP/1.1 404 Not Found\nContent-Encoding: gzip\nContent-Type: text/html\n\n";
 		files.put(ERROR_PAGE, Util.concateByteArray(errorHeader.getBytes(), loadFile(ERROR_PAGE)));
 		
 		serverSocketChannel = AsynchronousServerSocketChannel
 				.open(AsynchronousChannelGroup.withCachedThreadPool(
 						Executors.newCachedThreadPool(), THREAD_POOL))
 				.bind(new InetSocketAddress(HOST, PORT), BACKLOG);
 		
 		fastCgi = new FastCgi(properties);
 		System.out.println("Server start on " + HOST + ":" + PORT);
 	}
 
 
 	public void start() throws InterruptedException,
 			ExecutionException, TimeoutException {
 		
 		serverSocketChannel.accept(null, new ServerSocketCompletionHandler());
 		
 		while (true) {
 			Thread.sleep(Long.MAX_VALUE);
 		}
 
 	}
 	
 	
 	/**
 	 * Process request when serversocket accept
 	 * @author Fan Yang
 	 *
 	 */
 	private class ServerSocketCompletionHandler 
 		implements CompletionHandler<AsynchronousSocketChannel, Object> {
 
 
 		@Override
 		public void completed(AsynchronousSocketChannel socket, Object attachment) {
 			
 			serverSocketChannel.accept(null, this);
 			
 			ByteBuffer requestBuffer = ByteBuffer.allocate(REQUEST_LENGTH);
 
 			try {
 				
 				int requestLength = socket.read(requestBuffer).get();
 				
 				//Parse HTTP request
 				if (requestLength > 0) {
 					String requestString = new String(requestBuffer.array(), 0, requestLength);
 					
 					int firstIndex, lastIndex;
 					firstIndex = requestString.indexOf('\n');
 					if (firstIndex == -1) return;
 					String firstLine = requestString.substring(0, firstIndex);
 					
 					firstIndex = firstLine.indexOf(' ');
 					lastIndex = firstLine.lastIndexOf(' ');
 					if (firstIndex < 0 || lastIndex < 0 || firstIndex >= lastIndex) return;
 					String queryUri = firstLine.substring(firstIndex+1, lastIndex);
 
 					firstIndex = queryUri.indexOf('?');
 					String filePath = "";
 					String query = "";
 					if (firstIndex < 0) {
 						filePath = queryUri;
 					} else {
 						filePath = queryUri.substring(0, firstIndex);
 						query = queryUri.substring(firstIndex + 1);
 					}
 					if (filePath.equals("/")) filePath = INDEX_PAGE;
 					
 					//read request file
 					byte[] fileBytes = null;
 					
 					if ((fileBytes = files.get(filePath)) == null) {
 						try {
 							String fileExt = "";
 							lastIndex = filePath.lastIndexOf('.');
 							fileExt = filePath.substring(lastIndex+1);
 							
 							//support FastCgi
 							if (fileExt.equals("php")) {
 								socket.write(ByteBuffer.wrap(
 										fastCgi.requestFastCgi(filePath, query)
 										));
 								return;
 							}
 							
 							if (!headers.containsKey(fileExt)) fileExt = "txt";
 							
							fileBytes = loadFile(filePath);
							files.put(filePath, Util.concateByteArray(headers.get(fileExt), fileBytes));
							currentCache += fileBytes.length;
							filePathesList.add(filePath);
							
							//Cache algorithm: FIFO
							while(currentCache > FILE_CACHE) {
								currentCache -= files.remove(filePathesList.remove(0)).length;
 							}
 							
						} catch (FileNotFoundException ex) {
							fileBytes = files.get(ERROR_PAGE);
 						}
 					}
 					
 					socket.write(ByteBuffer.wrap(fileBytes));
 				}
 				
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					socket.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 		}
 
 		
 		@Override
 		public void failed(Throwable exc, Object attachment) {
 			System.out.println("Failed: " + exc);
 			
 		}
 		
 	}
 	
 	
 	/**
 	 * load file from disk into byte array
 	 * @param fileName
 	 * @return
 	 * @throws Exception
 	 */
 	private byte[] loadFile(String fileName) throws Exception {
 		
 		byte[] fileBuffer = new byte[FILE_SIZE];
 		int fileLength = 0;
		
		FileInputStream fis = new FileInputStream(DOC_ROOT + fileName);
 		fileLength = fis.read(fileBuffer);
 		fis.close();
 		
 		//Save Gzip compressed file
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		GZIPOutputStream gos = new GZIPOutputStream(baos);
 		gos.write(fileBuffer, 0, fileLength);
 		gos.finish();
 		
 		byte[] fileBytes = baos.toByteArray();
 		gos.close();
 		baos.close();
 
 		return fileBytes;
 	}
 	
 	
 	/**
 	 * generate http headers for different types
 	 */
 	private void generateHttpHeaders() {
 		
 		String[] fileTypes = {"html", "css", "js", "png", "jpg", "gif", "ico", "txt"};
 		
 		for (String fileType : fileTypes) {
 			String typeString;
 			
 			switch (fileType) {
 			case "html":
 				typeString = "text/html";
 				break;
 			case "css":
 				typeString = "text/css";
 				break;
 			case "js":
 				typeString = "application/javascript";
 				break;
 			case "png":
 				typeString = "image/png";
 				break;
 			case "jpg":
 				typeString = "image/jpeg";
 				break;
 			case "gif":
 				typeString = "image/gif";
 				break;
 			case "ico":
 				typeString = "image/x-icon";
 				break;
 			case "txt":
 				typeString = "text/plain";
 				break;	
 			default:
 				typeString = "text/plain";
 				break;
 			}
 			
 			String httpHeader = 
 					"HTTP/1.1 200 OK\nContent-Encoding: gzip\nContent-Type: " 
 							+ typeString + "\n\n";
 			
 			headers.put(fileType, httpHeader.getBytes());
 		}
 		
 	}
 	
 	
 
 }
 
 
 
 
