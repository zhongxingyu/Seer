 package net.geohhot.betterhttpserver;
 
 import java.io.*;
 import java.util.*;
 import java.net.*;
 import java.text.SimpleDateFormat;
 
 /**
 * Runs simple yet HTTP server
 * @author geohhot
 * @version 0.1
 */
 
 class betterServer {
 	private static int PORT = 8083;
 	private static String notFoundResource = "notFound.html";
 	private static File root = new File(new File(".").getAbsolutePath());
 	private static String[] imageExtensions = {"ico", "png", "jpg"};
 	private static String[] textExtensions = {"html", "xml", "txt"};
 
 	public static void main(String[] args) {	
 		System.out.println("Better HTTP server, by geohhot");
 		System.out.println("Trying to start server...");
 		try {
 			String rootDir = root.getCanonicalPath() + "/" + "root";
 			ServerSocket serv = new ServerSocket(PORT);
 			System.out.println("Server started!");
 
 			while (true) {
 				Socket inc = serv.accept();
 				InputStream inStream = inc.getInputStream();
 				OutputStream outStream = inc.getOutputStream();
 
 				Scanner in = new Scanner (inStream);
 				PrintWriter out = new PrintWriter (outStream, true);
 
 				String firstLine = "";
 				if (in.hasNextLine()) {
 					firstLine = in.nextLine();
 				} else {
 					inc.close();
 					continue;
 				}
 
 				String params[] = firstLine.split(" ");
 				String method = params[0];
 				String resource = params[1];
				String getParams = "";
				try {
					resource = params[1].substring(0, params[1].indexOf("?"));
					getParams = params[1].substring(params[1].indexOf("?")+1);
				} catch (StringIndexOutOfBoundsException e) {

				}
 				String protocol = params[2];
 
 				/// DEBUG
 				//System.out.println(method + " " + resource + " " + protocol);
 
 				if ( method.equals("GET") ) {
 					// GET requests
 					if (resource.equals("/")) {
 						resource += "index.html";
 					}
 					String replyResourcePath = rootDir + resource;
 					File replyResource = new File (replyResourcePath);
 
 					/// DEBUG
 					//System.out.println("Requested resource: "+replyResourcePath);
 
 					if (!replyResource.exists()) {
 						// file does not exist
 						// return 404 error
 						out.println(protocol+" 404 Not Found");
 						replyResourcePath = rootDir + "/" + notFoundResource;
 						replyResource = new File (replyResourcePath);
 					} 
 					else {
 						// reply code - 200 OK
 						out.println(protocol+" 200 OK");
 					}
 
 					String extension = replyResourcePath.substring (replyResourcePath.lastIndexOf("."));
 
 					// returning Content-Length
 					long length = replyResource.length();
 					out.println("Content-Length: "+length);
 					// returning name of server
 					out.println("Server: Better server (by geohhot)");
 					
 					// returning last modified date of resource
 					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 					out.println("Last-Modified: "+sdf.format(replyResource.lastModified()));
 
 					if (Arrays.asList(textExtensions).contains(extension)) {
 						// requested TEXT file
 						// return Content Type
 						out.println("Content-Type: text/"+extension);						
 					}
 					else if (Arrays.asList(imageExtensions).contains(extension)) {
 						// requested IMAGE file
 						out.println("Content-Type: image/"+extension);
 					}
 
 					out.println();
 					// starting printing resource byte by byte to output stream
 					InputStream resourceInputStream = new FileInputStream(replyResource);
 					for (long i = 0; i<length; i++) {
 						byte t;
 						t = (byte)resourceInputStream.read();
 						outStream.write(t);
 					}
 					out.println();
 					inc.close();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
