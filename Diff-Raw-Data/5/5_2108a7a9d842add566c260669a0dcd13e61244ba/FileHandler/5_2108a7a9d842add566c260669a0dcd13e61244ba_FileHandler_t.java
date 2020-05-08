 package stv6.handlers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.util.Date;
 
 import stv6.http.HttpServer;
 import stv6.http.request.Request;
 import stv6.http.request.RequestHandler;
 import stv6.http.request.Response;
 
 /**
  * This handler serves FILES. By that I mean text files
  *  (For CSS, for example) and image files. It is a very
  *  simplistic implementation for images; I have a list
  *  of extensions and if the file extension matches that,
  *  it is displayed as an image.
  * 
  * @author Daniel
  *
  */
 public class FileHandler implements RequestHandler {
 	public static final String HANDLED_PAGE = "files";
 	
 	private static String imgExts[] = {"jpg", "jpeg", "png", "gif", "ico"};
//	private File theFile;
 	
 	@Override
 	public boolean handle(Request request, Response response) {
 		// skip the /files/
 		String path = request.path
 		    .replace('/', File.separatorChar)
 		    .replace('+', ' ')
 		    .replace("%20", " ").substring(7);
		File theFile = new File(path);
 		if (!theFile.exists()) {		
 			System.err.println("Cannot find: " + path + " (" +
 			        theFile.getAbsolutePath() + ")");
 			return false;
 		}
 		if (!theFile.canRead()) {
 			System.err.println("Cannot read: " + path);
 			return false;
 		}
 		
 		// can we let them use their cache?
 		if (handleCache(request, response, theFile))
 		    return true;
 		
 		String ext = getImageExt(path);
 		if (ext != null) {
 
 			response.setContentType("image/"+ext);
 			byte[] imgdata = null;
 			try {
 				imgdata = getBytesFromFile(theFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 				
 			if (imgdata == null)
 				return false;
 			
 			response.setBody(imgdata);
 			return true;
 		} else {
 			// it's just a text file; read it
 			try {		
 
 				BufferedReader reader = new BufferedReader(new FileReader(theFile));
 				
 				String line;
 				while ((line=reader.readLine()) != null)
 					response.addBody(line+"\n");				
 								
 				// close the reader and return
 				reader.close();
 				
 				if (path.endsWith(".css"))
 					response.setContentType("text/css");
 				
 				return true;
 				
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return false;
 	}
 
 	/** 
 	 * Thanks to laziness and Java Developer's Almanac
 	 * @param file
 	 * @return
 	 * @throws IOException
 	 */
     public static byte[] getBytesFromFile(File file) throws IOException {
         InputStream is = new FileInputStream(file);
     
         // Get the size of the file
         long length = file.length();
     
         // You cannot create an array using a long type.
         // It needs to be an int type.
         // Before converting to an int type, check
         // to ensure that file is not larger than Integer.MAX_VALUE.
         if (length > Integer.MAX_VALUE) {
             // File is too large.... if you're trying to serve this
         	//	file from SeriesTracker, you might be retarded :P
         	return new byte[]{};
         }
     
         // Create the byte array to hold the data
         byte[] bytes = new byte[(int)length];
     
         // Read in the bytes
         int offset = 0;
         int numRead = 0;
         while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
             offset += numRead;
         }
     
         // Ensure all the bytes have been read in
         if (offset < bytes.length) {
             throw new IOException("Could not completely read file "+file.getName());
         }
     
         // Close the input stream and return bytes
         is.close();
         return bytes;
     }
 
 	@Override
 	public String getHandledPage() {
 		return HANDLED_PAGE;
 	}
     
     /**
      * @param filePath
      * @return The image's file extension if it is an image,
      * 			else null
      */
     public static String getImageExt(String filePath) {
     	// see if it is one of our image extensions
 		String[] parts = filePath.split("\\.");
 		String ext = "";	
 		if (parts.length > 0) {			
 			// it HAS an extension!
 			ext = parts[ parts.length-1 ];
 			for (String test : imgExts) {				
 				if (test.equalsIgnoreCase(ext)) {
 					// Aha! 
 					return ext;
 				}
 			}			
 		}	
 		
 		return null;
     }
     
     /**
      * Try to let the client use their cached copy of the file. 
      *  Basically, if they sent an If-Modified-Since header, and it's
      *  <= the file's modified date, we respond with 304 Not Modified
      *  and return True. Otherwise, we set the Last-Modified header
      *  in the response and return False
      * @param request
      * @param response
      * @param theFile
      * @return
      */
     public static boolean handleCache(Request request, Response response, 
             File theFile) {
         // check when the file was changed last
         Date modified = new Date(theFile.lastModified());
         
         // read in headers... if they have a cached copy, they should use it
         request.readHeaders();
         String ifModified = request.getHeaders().getValue("If-Modified-Since");
         if (ifModified != null) {
             try {
                 Date d = HttpServer.parseDate(ifModified);
                 if (modified.compareTo(d) <= 0) {
                     // our modified date was <= their copy 
                     //  (IE, it hasn't changed); don't make
                     //  them re-fetch it!
                     response.setStatus("304 Not Modified");
                     return true;
                 }
             } catch (ParseException e) {
                 e.printStackTrace();
                 System.err.println("Couldn't parse If-Modified-Since: " + ifModified);
             }
         }
         
         // they didn't have a cache... let them know when this was changed
         //    so they can make conditional requests in the future
         response.setHeader("Last-Modified", HttpServer.formatDate(modified));
         return false;
     }
 }
