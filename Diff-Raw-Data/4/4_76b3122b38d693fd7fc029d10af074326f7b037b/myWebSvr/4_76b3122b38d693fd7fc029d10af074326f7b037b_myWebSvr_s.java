 package FRC_Score_Sys.WebServer;
 
 import FRC_Score_Sys.EventInfo;
 import FRC_Score_Sys.MatchListObj;
 import FRC_Score_Sys.TeamRankObj;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.w3c.dom.Document;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.*;
 
 import java.io.StringWriter;
 
 import org.w3c.dom.Element;
 
 public class myWebSvr extends NanoHTTPD {
 	private Logger logger = LoggerFactory.getLogger(myWebSvr.class);
 
 	private String RankXML = "";
 	private String PMatchXML = "";
 	private String AMatchXML = "";
 	private String EventXML = "";
 
 	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 	DocumentBuilder docBuilder;
 
 	public void SetRankData(List<TeamRankObj> newRanks){
 		Document doc = docBuilder.newDocument();
 		Element rootElement = doc.createElement("RANKINGS");
 		doc.appendChild(rootElement);
 		int i = 0;
 		for(TeamRankObj r : newRanks){
 			i++;
 			Element ranknode = r.XMLVersion(String.valueOf(i), doc);
 			rootElement.appendChild(ranknode);
 		}
 
 
 		DOMSource domSource = new DOMSource(doc);
 		StringWriter writer = new StringWriter();
 		StreamResult result = new StreamResult(writer);
 		TransformerFactory tf = TransformerFactory.newInstance();
 		Transformer transformer;
 		try {
 			transformer = tf.newTransformer();
 			transformer.transform(domSource, result);
 			RankXML = writer.toString();
 			logger.debug("Rank XML Updated to: {}", RankXML);
 		} catch (TransformerConfigurationException e) {
 			// TODO Auto-generated catch block
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 		}
 	}
 
 	public void SetMatchData(List<MatchListObj> Matches, String which){
 		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("RANKINGS");
 		doc.appendChild(rootElement);
 		int i = 0;
 		for(MatchListObj mlo : Matches){
 			i++;
 			Element matchnode = mlo.XMLVersion(doc);
 			rootElement.appendChild(matchnode);
 		}
 
 
 		DOMSource domSource = new DOMSource(doc);
 		StringWriter writer = new StringWriter();
 		StreamResult result = new StreamResult(writer);
 		TransformerFactory tf = TransformerFactory.newInstance();
 		Transformer transformer;
 		try {
 			transformer = tf.newTransformer();
 			transformer.transform(domSource, result);
 			if(which.equals("p")){
 				PMatchXML = writer.toString();
 				logger.debug("PlayedMatch XML Updated to: {}", PMatchXML);
 			} else {
 				AMatchXML = writer.toString();
 				logger.debug("AllMatch XML Updated to: {}", AMatchXML);
 			}
 		} catch (TransformerConfigurationException e) {
 			// TODO Auto-generated catch block
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 		}
 	}
 
 	public void SetEventData(EventInfo EventData){
 		Document doc = docBuilder.newDocument();
 		Element rootElement = doc.createElement("EVENTINFO");
 		doc.appendChild(rootElement);
 		
 		Element eventnode = doc.createElement("EVENT");
 		rootElement.appendChild(eventnode);
 		
 		Element a = doc.createElement("EVENTNAME");
 		a.appendChild(doc.createTextNode(EventData.EventName));
 		eventnode.appendChild(a);
 		
 		a = doc.createElement("EVENTVENUE");
 		a.appendChild(doc.createTextNode(EventData.EventVenue));
 		eventnode.appendChild(a);
 		
 		a = doc.createElement("EVENTLOCATION");
 		a.appendChild(doc.createTextNode(EventData.EventLocation));
 		eventnode.appendChild(a);
 
 
 		DOMSource domSource = new DOMSource(doc);
 		StringWriter writer = new StringWriter();
 		StreamResult result = new StreamResult(writer);
 		TransformerFactory tf = TransformerFactory.newInstance();
 		Transformer transformer;
 		try {
 			transformer = tf.newTransformer();
 			transformer.transform(domSource, result);
 			EventXML = writer.toString();
 			logger.debug("Event XML Updated to: {}", EventXML);
 		} catch (TransformerConfigurationException e) {
 			// TODO Auto-generated catch block
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 		}
 	}
 	
 	/**
 	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
 	 */
 	private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {
 		private static final long	serialVersionUID	= 1L;
 
 		{
 			put("css", "text/css");
 			put("htm", "text/html");
 			put("html", "text/html");
 			put("xml", "text/xml");
 			put("txt", "text/plain");
 			put("asc", "text/plain");
 			put("gif", "image/gif");
 			put("jpg", "image/jpeg");
 			put("jpeg", "image/jpeg");
 			put("png", "image/png");
 			put("mp3", "audio/mpeg");
 			put("m3u", "audio/mpeg-url");
 			put("mp4", "video/mp4");
 			put("ogv", "video/ogg");
 			put("flv", "video/x-flv");
 			put("mov", "video/quicktime");
 			put("swf", "application/x-shockwave-flash");
 			put("js", "application/javascript");
 			put("pdf", "application/pdf");
 			put("doc", "application/msword");
 			put("ogg", "application/x-ogg");
 			put("zip", "application/octet-stream");
 			put("exe", "application/octet-stream");
 			put("class", "application/octet-stream");
 		}};
 
 		private File rootDir;
 
 		public myWebSvr(String host, int port, File wwwroot) {
 			super(host, port);
 			try {
 				docBuilder = docFactory.newDocumentBuilder();
 			} catch (ParserConfigurationException e) {
 				// TODO Handle this with logger
 			}
 
 			if(!wwwroot.isDirectory()){
 				logger.error("Can not find wwwroot directory. Webserver will not operate properly.");
 			}
 			this.rootDir = wwwroot;
 		}
 
 		public File getRootDir() {
 			return rootDir;
 		}
 
 		/**
 		 * URL-encodes everything between "/"-characters. Encodes spaces as '%20' instead of '+'.
 		 */
 		private String encodeUri(String uri) {
 			String newUri = "";
 			StringTokenizer st = new StringTokenizer(uri, "/ ", true);
 			while (st.hasMoreTokens()) {
 				String tok = st.nextToken();
 				if (tok.equals("/"))
 					newUri += "/";
 				else if (tok.equals(" "))
 					newUri += "%20";
 				else {
 					try {
 						newUri += URLEncoder.encode(tok, "UTF-8");
 					} catch (UnsupportedEncodingException ignored) {
 					}
 				}
 			}
 			return newUri;
 		}
 
 		/**
 		 * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all headers and HTTP parameters.
 		 */
 		public Response serveFile(String uri, Map<String, String> header, File homeDir) {
 			Response res = null;
 
 			// Make sure we won't die of an exception later
 			if (!homeDir.isDirectory())
 				res = new Response(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "INTERNAL ERRROR: serveFile(): given homeDir is not a directory.");
 
 			if (res == null) {
 				// Remove URL arguments
 				uri = uri.trim().replace(File.separatorChar, '/');
 				if (uri.indexOf('?') >= 0)
 					uri = uri.substring(0, uri.indexOf('?'));
 
 				// Prohibit getting out of current directory
 				if (uri.startsWith("src/main") || uri.endsWith("src/main") || uri.contains("../"))
 					res = new Response(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Won't serve ../ for security reasons.");
 			}
 
 			File f = new File(homeDir, uri);
 			if (res == null && !f.exists())
 				res = new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
 
 			// List the directory, if necessary
 			if (res == null && f.isDirectory()) {
 				// Browsers get confused without '/' after the
 				// directory, send a redirect.
 				if (!uri.endsWith("/")) {
 					uri += "/";
 					res = new Response(Response.Status.REDIRECT, NanoHTTPD.MIME_HTML, "<html><body>Redirected: <a href=\"" + uri + "\">" + uri
 							+ "</a></body></html>");
 					res.addHeader("Location", uri);
 				}
 
 				if (res == null) {
 					// First try index.html and index.htm
 					if (new File(f, "index.html").exists())
 						f = new File(homeDir, uri + "/index.html");
 					else if (new File(f, "index.htm").exists())
 						f = new File(homeDir, uri + "/index.htm");
 					// No index file, list the directory if it is readable
 					else if (f.canRead()) {
 						String[] files = f.list();
 						String msg = "<html><body><h1>Directory " + uri + "</h1><br/>";
 
 						if (uri.length() > 1) {
 							String u = uri.substring(0, uri.length() - 1);
 							int slash = u.lastIndexOf('/');
 							if (slash >= 0 && slash < u.length())
 								msg += "<b><a href=\"" + uri.substring(0, slash + 1) + "\">..</a></b><br/>";
 						}
 
 						if (files != null) {
 							for (int i = 0; i < files.length; ++i) {
 								File curFile = new File(f, files[i]);
 								boolean dir = curFile.isDirectory();
 								if (dir) {
 									msg += "<b>";
 									files[i] += "/";
 								}
 
 								msg += "<a href=\"" + encodeUri(uri + files[i]) + "\">" + files[i] + "</a>";
 
 								// Show file size
 								if (curFile.isFile()) {
 									long len = curFile.length();
 									msg += " &nbsp;<font size=2>(";
 									if (len < 1024)
 										msg += len + " bytes";
 									else if (len < 1024 * 1024)
 										msg += len / 1024 + "." + (len % 1024 / 10 % 100) + " KB";
 									else
 										msg += len / (1024 * 1024) + "." + len % (1024 * 1024) / 10 % 100 + " MB";
 
 									msg += ")</font>";
 								}
 								msg += "<br/>";
 								if (dir)
 									msg += "</b>";
 							}
 						}
 						msg += "</body></html>";
 						res = new Response(msg);
 					} else {
 						res = new Response(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: No directory listing.");
 					}
 				}
 			}
 
 			try {
 				if (res == null) {
 					// Get MIME type from file name extension, if possible
 					String mime = null;
 					int dot = f.getCanonicalPath().lastIndexOf('.');
 					if (dot >= 0)
 						mime = MIME_TYPES.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
 					if (mime == null)
 						mime = NanoHTTPD.MIME_HTML; //MIME_DEFAULT_BINARY;
 
 					// Calculate etag
 					String etag = Integer.toHexString((f.getAbsolutePath() + f.lastModified() + "" + f.length()).hashCode());
 
 					// Support (simple) skipping:
 					long startFrom = 0;
 					long endAt = -1;
 					String range = header.get("range");
 					if (range != null) {
 						if (range.startsWith("bytes=")) {
 							range = range.substring("bytes=".length());
 							int minus = range.indexOf('-');
 							try {
 								if (minus > 0) {
 									startFrom = Long.parseLong(range.substring(0, minus));
 									endAt = Long.parseLong(range.substring(minus + 1));
 								}
 							} catch (NumberFormatException ignored) {
 							}
 						}
 					}
 
 					// Change return code and add Content-Range header when skipping is requested
 					long fileLen = f.length();
 					if (range != null && startFrom >= 0) {
 						if (startFrom >= fileLen) {
 							res = new Response(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
 							res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
 							res.addHeader("ETag", etag);
 						} else {
 							if (endAt < 0)
 								endAt = fileLen - 1;
 							long newLen = endAt - startFrom + 1;
 							if (newLen < 0)
 								newLen = 0;
 
 							final long dataLen = newLen;
 							FileInputStream fis = new FileInputStream(f) {
 								@Override
 								public int available() throws IOException {
 									return (int) dataLen;
 								}
 							};
 							fis.skip(startFrom);
 
 							res = new Response(Response.Status.PARTIAL_CONTENT, mime, fis);
 							res.addHeader("Content-Length", "" + dataLen);
 							res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
 							res.addHeader("ETag", etag);
 						}
 					} else {
 						//if (etag.equals(header.get("if-none-match")))
 						//	res = new Response(Response.Status.NOT_MODIFIED, mime, "");
 						if(true) {
 							FileInputStream fis = new FileInputStream(f);
 							if(f.getName().endsWith("xml")) {
 								// This is a call to XML I have stored in memory. //////////////////////////////////////
 								String data = "";
 								String Filename = f.getName(); 
 								switch(Filename){
 									case "rankings.xml":
 										data = RankXML;
 										break;
 									case "results.xml":
 										data = PMatchXML;
 										break;
 									case "matches.xml":
 										data = AMatchXML;
 										break;
 									case "eventinfo.xml":
 										data = EventXML;
 										break;
 								}
 								InputStream is = new ByteArrayInputStream(data.getBytes());
 								res = new Response(Response.Status.OK, mime, is);
 							} else if(f.getName().endsWith("htm") || f.getName().endsWith("html")){
 								// This is text to be filtered
 								StringBuffer fileContent = new StringBuffer("");
 								byte[] buffer = new byte[1024];
 								while (fis.read(buffer) != -1) {
 									fileContent.append(new String(buffer));
 								}            		
 								String result = String.valueOf(fileContent);
 								result = FilterHTML(result);
 								InputStream is = new ByteArrayInputStream(result.getBytes());
 								res = new Response(Response.Status.OK, mime, is);
 							} else {
 								// this is not (imgs etc.)
 								res = new Response(Response.Status.OK, mime, fis);
 							}
 							//res.addHeader("Content-Length", "" + fileLen);
 							res.addHeader("ETag", etag);
 						}
 					}
 				}
 			} catch (IOException ioe) {
 				res = new Response(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
 			}
 
 			res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requestes
 			return res;
 		}
 
 		@Override
 		public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
 			logger.debug("{} '{}'", method, uri);
 
 			Iterator<String> e = header.keySet().iterator();
 			while (e.hasNext()) {
 				String value = e.next();
 				logger.debug("  HDR: '{}' = '{}'", value, header.get(value));
 			}
 			e = parms.keySet().iterator();
 			while (e.hasNext()) {
 				String value = e.next();
 				logger.debug("  PRM: '{}' = '{}'", value, parms.get(value));
 			}
 			e = files.keySet().iterator();
 			while (e.hasNext()) {
 				String value = e.next();
 				logger.debug("  UPLOADED: '{}' = '{}'", value, files.get(value));
 			}
 
 			return serveFile(uri, header, getRootDir());
 		}
 
 		private String FilterHTML(String input){
 			String out = input;
 			out = out.replace("{{hello}}", "Hello World!");
 			return out;
 		}
 }
