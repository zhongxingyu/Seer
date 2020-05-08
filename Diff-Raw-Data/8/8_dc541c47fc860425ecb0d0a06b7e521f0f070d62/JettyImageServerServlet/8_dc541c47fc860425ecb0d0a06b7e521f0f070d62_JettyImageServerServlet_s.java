 /**
  * 
  */
 package net.niconomicon.tile.source.app.sharing.server.jetty;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.niconomicon.tile.source.app.Ref;
 
 /**
  * @author niko
  * 
  */
 public class JettyImageServerServlet extends HttpServlet {
 
 	Map<String, String> imaginaryMap;
 	Set<String> knownImages;
 
 	File css;
 
 	public JettyImageServerServlet() {
 		knownImages = new HashSet<String>();
 		String cssLocation = "net/niconomicon/tile/source/app/sharing/server/jetty/index.css";
 		URL url = this.getClass().getClassLoader().getResource(cssLocation);
 		css = new File(url.getFile());
 	}
 
 	public void addImages(Collection<String> documents) {
 		knownImages.clear();
 		knownImages.addAll(documents);
 
 		Map<String, String> refs = Ref.generateIndexFromFileNames(knownImages);
 		// for caching
 		Ref.extractThumbsAndMiniToTmpFile(refs);
 		imaginaryMap = refs;
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		String request = req.getRequestURI();
 		System.out.println("URI : " + request);
 
 		if (request.equals("/" + Ref.sharing_jsonRef) || request.equals(Ref.URI_jsonRef)) {
 			String k = "/" + Ref.sharing_jsonRef;
 
			System.out.println("should be returning the Displayable Feed [" + imaginaryMap.get(request).length() + "]");
 			try {
 				sendString(imaginaryMap.get(k), resp);
 				return;
 			} catch (Exception ex) {
 				resp.sendError(500, "The server encountered an error while trying to send the content for request [" + request + "]");
 				return;
 			}
 		}
 		if (request.compareTo("/index.css") == 0) {
			System.out.println("should be returning the css.");
 			try {
 				sendCSS(resp);
 				return;
 			} catch (Exception ex) {
 				resp.sendError(500, "The server encountered an error while trying to send the requested content for request [" + request + "]");
 				return;
 			}
 		}
 
 		if (request.equals("/") || request.equals(Ref.URI_htmlRef)) {
 			request = Ref.URI_htmlRef;
			System.out.println("should be returning the html list [" + imaginaryMap.get(request).length() + "]");
 			try {
 				String resolvedAddressItem = Ref.app_handle_item + req.getScheme() + "://" + req.getLocalAddr() + ":" + req.getLocalPort();
 				String resolvedAddressList = Ref.app_handle_list + req.getScheme() + "://" + req.getLocalAddr() + ":" + req.getLocalPort();
 				System.out.println("resolved Address item : " + resolvedAddressItem);
 				System.out.println("resolved Address list : " + resolvedAddressList);
 				String htmlListing = imaginaryMap.get(request).replaceAll(Ref.app_handle_item, resolvedAddressItem);
 				htmlListing = htmlListing.replaceAll(Ref.app_handle_list, resolvedAddressList);
 				sendString(htmlListing, resp);
 				return;
 			} catch (Exception ex) {
 				resp.sendError(500, "The server encountered an error while trying to send the requested content for request [" + request + "]");
 				return;
 			}
 		}
 
 		if (null == imaginaryMap || !imaginaryMap.containsKey(request) || imaginaryMap.get(request) == null) {
 			resp.sendError(404, "The server could not find or get access to [" + request + "]");
 			return;
 		}
 		String string = imaginaryMap.get(request);
 		System.out.println("String from the imaginary map : [" + string + "]");
 		File f = new File(string);
 		if (f.exists()) {
 			try {
 				sendFile(f, resp);
 			} catch (Exception ex) {
 				resp.sendError(
 						500,
 						"The server encountered an error while trying to send the requested file [ " + f.getName() + "] for request [" + request + "]");
 				return;
 			}
 		} else {
 			resp.sendError(404, "The server could not find or get access to [" + f.getName() + "]");
 			return;
 		}
 	}
 
 	public static void sendString(String s, HttpServletResponse response) throws Exception {
 		byte[] bytes = s.getBytes();
 		response.setStatus(HttpServletResponse.SC_OK);
 		response.setContentLength(bytes.length);
 		response.getOutputStream().write(bytes);
 		response.flushBuffer();
 	}
 
 	public static void sendFile(File f, HttpServletResponse response) throws Exception {
 		long len = f.length();
 		response.setStatus(HttpServletResponse.SC_OK);
 		response.setContentLength((int) len);
 		int bufferSize = response.getBufferSize();
 		byte[] buff = new byte[bufferSize];
 		InputStream in;
 		in = new FileInputStream(f);
 		int nread;
 		while ((nread = in.read(buff)) > 0) {
 			response.getOutputStream().write(buff, 0, nread);
 		}
 		in.close();
 		response.getOutputStream().flush();
 	}
 
 	public void sendCSS(HttpServletResponse response) throws Exception {
 
 		long len = css.length();
 		response.setStatus(HttpServletResponse.SC_OK);
 		response.setContentLength((int) len);
 		int bufferSize = response.getBufferSize();
 		byte[] buff = new byte[bufferSize];
 		InputStream in;
 		in = new FileInputStream(css);
 		int nread;
 		while ((nread = in.read(buff)) > 0) {
 			response.getOutputStream().write(buff, 0, nread);
 		}
 		in.close();
 		response.getOutputStream().flush();
 	}
 
 }
