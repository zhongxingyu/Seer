 import java.io.* ;
 import java.net.* ;
 import java.util.* ;
 import com.google.gson.*;
 class HttpResponse {
     final static String CRLF = "\r\n";
     DataOutputStream os;
 	int statuscode;
 	String statusmsg;
 	Map<String, String> headers;
 	Template bodyTemplate;
 	FileInputStream bodyFis;
 	String bodyString;
 	public HttpResponse(Socket socket) throws IOException {
 		final DataOutputStream os = new DataOutputStream(socket.getOutputStream());
 		this.os = os;
 		headers = new HashMap<String, String>();
 		setStatus(200, "OK");
 		setHeader("Server", "lucos");
 	}
 	public void setHeader(String key, String value) {
 		headers.put(key, value);
 	}
 	public void setFileName(String fileName) {
 		setHeader("Content-type", contentType(fileName));
 	}
 	private void clearBody() {
 		bodyFis = null;
 		bodyTemplate = null;
 		bodyString = null;
 	}
 	private void setStatus(int statuscode, String statusmsg) {
 		this.statuscode = statuscode;
 		this.statusmsg = statusmsg;
 	}
 	public void setBody(Template template) {
 		clearBody();
 		bodyTemplate = template;
 		setFileName(template.getFileName());
 	}
 	public void setBody(FileInputStream fis) {
 		clearBody();
 		bodyFis = fis;
 	}
 	private void setBody(String string) {
 		clearBody();
 		bodyString = string;
 		setHeader("Content-type", "text/plain");
 	}
 	public void setError(int statuscode, String errormsg) {
 		setStatus(statuscode, errormsg);
 		try {
 			Template errorTemplate = new Template("error", "html");
 			errorTemplate.setData("errormsg", errormsg);
 			setBody(errorTemplate);
 		} catch (IOException e) {
 			setBody(errormsg);
 		}
 	}
 	public void redirect(String url) {
 		clearBody();
 		setStatus(307, "Redirect");
 		setHeader("Location", url);
 	}
 	public void notFound(String type) {
 		String msg = type + " Not Found";
 		setError(404, msg);
 	}
 	public void notFound() {
 		notFound("File");
 	}
 	public void setJson(Object output) {
 		clearBody();
 		Gson gson = new Gson();
 		bodyString = gson.toJson(output);
 		setHeader("Content-type", "application/json");
 	}
 	
     private static String contentType(String fileName) {
         if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
             return "text/html";
         }
         if(fileName.endsWith(".xhtml")) {
             return "application/xhtml+xml";
         }
         if(fileName.endsWith(".png")) {
             return "image/png";
         }
         if(fileName.endsWith(".gif")) {
             return "image/gif";
         }
         if(fileName.endsWith(".jpg")) {
             return "image/jpeg";
         }
         if(fileName.endsWith(".css")) {
             return "text/css";
         }
         if(fileName.endsWith(".js")) {
             return "text/javascript";
         }
         if(fileName.endsWith(".mp3")) {
             return "audio/mpeg";
         }
         if(fileName.endsWith("manifest")) {
             return "text/cache-manifest";
         }
        if(fileName.endsWith("ico")) {
            return "image/x-icon";
        }
         return "application/octet-stream";
     }
     private void sendHeaders() throws IOException {
         os.writeBytes("HTTP/1.1 "+ statuscode +" "+ statusmsg + CRLF);
         Iterator iter = headers.entrySet().iterator();
         while (iter.hasNext()) {
             Map.Entry header = (Map.Entry)iter.next();
             os.writeBytes(header.getKey()+": "+header.getValue() + CRLF);
         }
         os.writeBytes(CRLF);
     }
 	
 	public void send() throws IOException {
 		sendHeaders();
 		if (bodyTemplate != null) {
 			os.writeBytes(bodyTemplate.toString());
 		} else if (bodyString != null) {
 			os.writeBytes(bodyString);
 		} else if (bodyFis != null) {
 			
 			// Construct a 1K buffer to hold bytes on their way to the socket.
 			byte[] buffer = new byte[1024];
 			int bytes = 0;
 			
 			// Copy requested file into the socket's output stream.
 			while((bytes = bodyFis.read(buffer)) != -1 ) {
 				os.write(buffer, 0, bytes);
 			}
 			bodyFis.close();
 		}
 		os.close();
 	}
 }
