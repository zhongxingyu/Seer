 package ar.edu.it.itba.pdc.v2.implementations.utils;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.util.Map;
 
 import ar.edu.it.itba.pdc.v2.implementations.HTML;
 import ar.edu.it.itba.pdc.v2.implementations.RebuiltHeader;
 import ar.edu.it.itba.pdc.v2.interfaces.Configurator;
 import ar.edu.it.itba.pdc.v2.interfaces.Decoder;
 import ar.edu.it.itba.pdc.v2.interfaces.HTTPHeaders;
 
 public class DecoderImpl implements Decoder {
 
 	private boolean read = true;
 	private int index = 0;
 	private HTTPHeaders headers = null;
 	private String fileName;
 	private int keepReadingBytes = 0;
 	private boolean isImage = false;
 	private boolean isText = false;
 	private boolean generatingKeep = true;
 	private Configurator configurator;
 	private byte[] aux;
 	private byte[] aux2 = new byte[20];
 	int auxIndex = 0;
 
 	private boolean BUILDING_NUMBER = true;
 	private boolean N_EXPECTED = false;
 	private boolean SECN_EXPECTED = false;
 	private boolean R_EXPECTED = false;
 	private boolean SECR_EXPECTED = false;
 	private boolean READING_CONTENT = false;
 	private boolean FINISHED = false;
 
 	public DecoderImpl(int buffSize) {
 		headers = new HTTPPacket();
 		aux = new byte[100];
 	}
 
 	public void decode(byte[] bytes, int count) {
 
 		// headers.parse(bytes, count);
 
 		String length;
 
 		if (headers.getHeader("Method").contains("GET")) {
 			read = false;
 		}
 
 		length = headers.getHeader("Content-Length");
 		// remove spaces
 		if (length != null) {
 			length = length.replaceAll(" ", "");
 			int expectedRead = Integer.parseInt(length);
 			if (expectedRead >= headers.getReadBytes()) {
 				if (!headers.contentExpected())
 					read = false;
 				read = true;
 			} else {
 				read = false;
 			}
 		} else {
 			String transferEncoding = headers.getHeader("Transfer-Encoding");
 			if (transferEncoding != null
 					&& transferEncoding.contains("chunked")) {
 
 			}
 		}
 
 	}
 
 	public byte[] getExtra(byte[] data, int count) {
 		byte[] bytes = new byte[count - headers.getReadBytes()];
 		int i = 0;
 		boolean R_EXPECTED = true;
 		boolean N_EXPECTED = false;
 		boolean SECR_EXPECTED = false;
 		boolean SECN_EXPECTED = false;
 		for (int j = 0; j < count; j++) {
 			if (R_EXPECTED && data[j] == '\r') {
 				R_EXPECTED = false;
 				N_EXPECTED = true;
 			} else if (N_EXPECTED && data[j] == '\n') {
 				N_EXPECTED = false;
 				SECR_EXPECTED = true;
 			} else if (N_EXPECTED) {
 				N_EXPECTED = false;
 				R_EXPECTED = true;
 			} else if (SECR_EXPECTED && data[j] == '\r') {
 				SECR_EXPECTED = false;
 				SECN_EXPECTED = true;
 			} else if (SECR_EXPECTED) {
 				SECR_EXPECTED = false;
 				R_EXPECTED = true;
 			} else if (SECN_EXPECTED && data[j] == '\n') {
 				j++;
 				for (i = 0; i < count - headers.getReadBytes() && j < count; i++) {
 					bytes[i] = data[j++];
 				}
 				return bytes;
 			} else {
 				SECN_EXPECTED = false;
 				R_EXPECTED = true;
 			}
 
 		}
 
 		return bytes;
 	}
 
 	public boolean keepReading() {
 		return read;
 	}
 
 	public boolean contentExpected() {
 		return headers.contentExpected();
 	}
 
 	private boolean isChunked() {
 		return (headers.getHeader("Transfer-Encoding") != null)
 				&& (headers.getHeader("Transfer-Encoding").contains("chunked"));
 	}
 
 	public int getBufferSize() {
 		return index;
 	}
 
 	public String getHeader(String header) {
 		return headers.getHeader(header);
 	}
 
 	private void analizeMediaType() {
 		if (headers.getHeader("Content-Type") != null) {
 			isImage = headers.getHeader("Content-Type").contains("image/");
 			isText = headers.getHeader("Content-Type").contains("text/plain");
 		}
 	}
 
 	public boolean applyTransformations() {
 		this.analizeMediaType();
 		return configurator.applyTransformation() && (isImage || isText);
 	}
 
 	public void applyRestrictions(byte[] bytes, int count,
 			HTTPHeaders requestHeaders) {
 
 		this.analizeMediaType();
 
 		if (isImage && configurator.applyRotations()) {
 			if (fileName == null) {
 				String path[] = requestHeaders.getHeader("RequestedURI").split(
 						"/");
 				File f = new File("/tmp/prueba");
 				f.mkdir();
 				if (path[path.length - 1].length() < 10)
 					fileName = "/tmp/prueba/"
 							+ String.valueOf(System.currentTimeMillis())
 							+ Thread.currentThread().getId()
 							+ path[path.length - 1];
 				else {
 
 					fileName = "/tmp/prueba/"
 							+ path[path.length - 1].substring(0, 6)
 							+ String.valueOf(System.currentTimeMillis())
 							+ Thread.currentThread().getId()
 							+ "."
 							+ headers.getHeader("Content-Type").split("/")[1]
 									.split(";")[0];
 				}
 			}
 			try {
 				FileOutputStream fw = new FileOutputStream(fileName, true);
 				fw.write(bytes, 0, count);
 				fw.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} else if (isText && configurator.applyTextTransformation()) {
 			if (fileName == null) {
 				String path[] = requestHeaders.getHeader("RequestedURI").split(
 						"/");
 				File f = new File("/tmp/prueba");
 				f.mkdir();
 				if (path[path.length - 1].length() < 10)
 					fileName = "/tmp/prueba/" + path[path.length - 1] + ".txt";
 				else {
 					fileName = "/tmp/prueba/"
 							+ path[path.length - 1].substring(0, 6) + "."
 							+ "txt";
 				}
 			}
 			try {
 				FileOutputStream fw = new FileOutputStream(fileName, true);
 				fw.write(bytes, 0, count);
 				fw.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public boolean isImage() {
 		return isImage;
 	}
 
 	public boolean isText() {
 		return isText;
 	}
 
 	public byte[] getRotatedImage() throws IOException {
 		Transformations im = new Transformations();
 		// InputStream is = null;
 		// try {
 		// is = new BufferedInputStream(new FileInputStream((fileName)));
 		// } catch (FileNotFoundException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// }
 
 		byte[] modified = im.rotate(fileName, 180);
 		if (modified == null) {
 			return null;
 		}
 		// try {
 		// is.close();
 		// } catch (IOException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// }
 		fileName = null;
 		return modified;
 	}
 
 	public byte[] getTransformed() {
 		Transformations im = new Transformations();
 		InputStream is = null;
 		try {
 			System.out.println(fileName);
 			is = new BufferedInputStream(new FileInputStream((fileName)));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		byte[] modified = null;
 		try {
 			modified = im.transformL33t(is);
 		} catch (UnsupportedEncodingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
 			is.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		fileName = null;
 		return modified;
 	}
 
 	public void applyTransformations(byte[] bytes, int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void applyFilters() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public boolean completeHeaders(byte[] bytes, int count) {
 		boolean R_EXPECTED = true;
 		boolean N_EXPECTED = false;
 		boolean SECR_EXPECTED = false;
 		boolean SECN_EXPECTED = false;
 		for (int j = 0; j < count; j++) {
 			if (R_EXPECTED && bytes[j] == '\r') {
 				R_EXPECTED = false;
 				N_EXPECTED = true;
 			} else if (N_EXPECTED && bytes[j] == '\n') {
 				N_EXPECTED = false;
 				SECR_EXPECTED = true;
 			} else if (N_EXPECTED) {
 				N_EXPECTED = false;
 				R_EXPECTED = true;
 			} else if (SECR_EXPECTED && bytes[j] == '\r') {
 				SECR_EXPECTED = false;
 				SECN_EXPECTED = true;
 			} else if (SECR_EXPECTED) {
 				SECR_EXPECTED = false;
 				R_EXPECTED = true;
 			} else if (SECN_EXPECTED && bytes[j] == '\n') {
 				return true;
 			} else {
 				SECN_EXPECTED = false;
 				R_EXPECTED = true;
 			}
 
 		}
 		return false;
 
 	}
 
 	public void analize(byte[] bytes, int count) {
 
 		if (!headers.contentExpected()) {
 			keepReadingBytes = 0;
 			read = false;
 			return;
 		}
 		if (isChunked()) {
 			for (int j = 0; j < count; j++) {
 				if (BUILDING_NUMBER && !N_EXPECTED) {
 					if (bytes[j] == '0' && auxIndex == 0) {
 						FINISHED = true;
 						R_EXPECTED = true;
 						N_EXPECTED = false;
 						BUILDING_NUMBER = false;
 					} else if (bytes[j] == '\r') {
 						N_EXPECTED = true;
 					} else {
 						aux[auxIndex++] = bytes[j];
 					}
 				} else if (BUILDING_NUMBER && N_EXPECTED) {
 					if (bytes[j] != '\n' && bytes[j] != '0') {
 						System.out.println("NO DEBERIA PASAR");
 					}
 					Integer sizeLine = null;
 					try {
 						sizeLine = Integer.parseInt(
 								new String(aux, 0, auxIndex), 16);
 					} catch (NumberFormatException e) {
 						sizeLine = 0;
 					}
 					if (sizeLine == 0) {
 						read = false;
 						FINISHED = true;
 						BUILDING_NUMBER = false;
 						N_EXPECTED = false;
 						continue;
 					}
 					keepReadingBytes = sizeLine;
 					auxIndex = 0;
 					READING_CONTENT = true;
 					BUILDING_NUMBER = false;
 					N_EXPECTED = false;
 					R_EXPECTED = false;
 				} else if (READING_CONTENT) {
 					keepReadingBytes -= 1;
 					if (keepReadingBytes == 0) {
 						READING_CONTENT = false;
 						BUILDING_NUMBER = false;
 						R_EXPECTED = true;
 						N_EXPECTED = false;
 					}
 					if (keepReadingBytes < 0) {
 						System.out.println("OUCH");
 					}
 				} else if (R_EXPECTED && !FINISHED) {
 					R_EXPECTED = false;
 					N_EXPECTED = true;
 				} else if (N_EXPECTED && !FINISHED) {
 					N_EXPECTED = false;
 					BUILDING_NUMBER = true;
 				} else if (FINISHED) {
 					// read = false;
 					// auxIndex = 0;
 					if (R_EXPECTED && bytes[j] == '\r') {
 						R_EXPECTED = false;
 						N_EXPECTED = true;
 					} else if (N_EXPECTED) {
 						N_EXPECTED = false;
 						SECR_EXPECTED = true;
 					} else if (SECR_EXPECTED && bytes[j] == '\r') {
 						SECR_EXPECTED = false;
 						SECN_EXPECTED = true;
 					} else if (SECR_EXPECTED) {
 						SECR_EXPECTED = false;
 						R_EXPECTED = true;
 					} else if (SECN_EXPECTED) {
 						read = false;
 						auxIndex = 0;
 					}
 
 				}
 			}
 		} else if (headers.getHeader("Content-Length") != null) {
 			if (keepReadingBytes == 0) {
 				keepReadingBytes = Integer.parseInt(headers.getHeader(
 						"Content-Length").replaceAll(" ", ""));
 			}
 			keepReadingBytes -= count;
 			if (keepReadingBytes == 0)
 				read = false;
 		} else {
 			read = true;
 		}
 	}
 
 	public void reset() {
 		read = true;
 		index = 0;
 		headers = new HTTPPacket();
 		fileName = null;
 		keepReadingBytes = 0;
 		generatingKeep = true;
 		isImage = false;
 		isText = false;
 		BUILDING_NUMBER = true;
 		N_EXPECTED = false;
 		R_EXPECTED = false;
 		READING_CONTENT = false;
 		FINISHED = false;
 		auxIndex = 0;
 	}
 
 	public void parseHeaders(byte[] data, int count) {
 		headers.parseHeaders(data, count);
 	}
 
 	public HTTPHeaders getHeaders() {
 		return headers;
 	}
 
 	public void setConfigurator(Configurator configurator) {
 		this.configurator = configurator;
 
 	}
 
 	public RebuiltHeader generateBlockedHeader(String cause) {
 		HTTPHeaders newHeaders = new HTTPPacket();
 		if (cause.equals("455")) {
 			newHeaders.addHeader("StatusCode", "455");
 			newHeaders.addHeader("Reason", "Blocked URL");
 		} else if (cause.equals("456")) {
 			newHeaders.addHeader("StatusCode", "456");
 			newHeaders.addHeader("Reason", "Blocked MediaType");
 		} else if (cause.equals("453")) {
 			newHeaders.addHeader("StatusCode", "453");
 			newHeaders.addHeader("Reason", "Blocked IP");
 		} else if (cause.equals("451")) {
 			newHeaders.addHeader("StatusCode", "451");
 			newHeaders.addHeader("Reason", "Blocked File Size");
 		} else if (cause.equals("452")) {
 			newHeaders.addHeader("StatusCode", "452");
 			newHeaders.addHeader("Reason", "All Blocked");
 		} else if (cause.equals("500")) {
 			newHeaders.addHeader("StatusCode", "500");
 			newHeaders.addHeader("Reason", "Internal Server Error");
 		} else if (cause.equals("400")) {
 			newHeaders.addHeader("StatusCode", "400");
 			newHeaders.addHeader("Reason", "Bad Request");
 		}
 		newHeaders.addHeader("HTTPVersion", "HTTP/1.1");
 		newHeaders.addHeader("Via", " mu0");
 		newHeaders.addHeader("Content-Type", " text/html; charset=iso-8859-1");
 		newHeaders.addHeader("Connection", " close");
 
 		Map<String, String> allHeaders = newHeaders.getAllHeaders();
 		String sb = "";
 
 		sb += allHeaders.get("HTTPVersion") + " ";
 		sb += allHeaders.get("StatusCode") + " ";
 		sb += allHeaders.get("Reason") + "\r\n";
 
 		for (String key : allHeaders.keySet()) {
 			if (!key.equals("HTTPVersion") && !key.equals("StatusCode")
 					&& !key.equals("Reason"))
 				sb += (key + ":" + allHeaders.get(key) + "\r\n");
 		}
 		sb += ("\r\n");
 		return new RebuiltHeader(sb.getBytes(), sb.length());
 	}
 
 	public HTML generateBlockedHTML(String cause) {
 		String html = "";
 		if (cause.equals("455")) {
 			html = "<!DOCTYPE HTML PUBLIC ''-//IETF//DTD HTML 2.0//EN'>"
 					+ "<html><head>" + "<title>455 URL bloqueada</title>"
 					+ "</head><body>" + "<h1>URL Bloqueada</h1>"
 					+ "<p>Su proxy bloqueo esta url<br />" + "</p>"
 					+ "</body></html>";
 
 		} else if (cause.equals("456")) {
 			html = "<h1>MediaType Bloqueada</h1>"
 					+ "<p>Su proxy bloqueo este tipo de archivos<br />"
 					+ "</p>";
 
 		} else if (cause.equals("453")) {
 			html = "<!DOCTYPE HTML PUBLIC ''-//IETF//DTD HTML 2.0//EN'>"
 					+ "<html><head>" + "<title>453 IP bloqueada</title>"
 					+ "</head><body>" + "<h1>IP Bloqueada</h1>"
 					+ "<p>Su proxy bloqueo esta IP<br />" + "</p>"
 					+ "</body></html>";
 
 		} else if (cause.equals("451")) {
 			html = "<!DOCTYPE HTML PUBLIC ''-//IETF//DTD HTML 2.0//EN'>"
 					+ "<html><head>"
 					+ "<title>451 Tamano de archivo bloqueado</title>"
 					+ "</head><body>" + "<h1>Tamano de archivo Bloqueada</h1>"
 					+ "<p>Su proxy bloqueo archivos de este tamano<br />"
 					+ "</p>" + "</body></html>";
 
 		} else if (cause.equals("452")) {
 			html = "<!DOCTYPE HTML PUBLIC ''-//IETF//DTD HTML 2.0//EN'>"
 					+ "<html><head>" + "<title>452 Se bloqueo todo</title>"
 					+ "</head><body>" + "<h1>Todo bloqueado</h1>"
 					+ "<p>Su proxy bloqueo todo<br />" + "</p>"
 					+ "</body></html>";
 
 		} else if (cause.equals("500")) {
 			html = "<!DOCTYPE HTML PUBLIC ''-//IETF//DTD HTML 2.0//EN'>"
 					+ "<html><head>"
 					+ "<title>500 Internal Server Error</title>"
 					+ "</head><body>" + "<h1>Internal Server Error</h1>"
 					+ "<p>Internal Server Error<br />" + "</p>"
 					+ "</body></html>";
 
 		} else if (cause.equals("400")) {
 			html = "<!DOCTYPE HTML PUBLIC ''-//IETF//DTD HTML 2.0//EN'>"
 					+ "<html><head>" + "<title>400 Bad Request</title>"
 					+ "</head><body>" + "<h1>Bad Request</h1>"
 					+ "<p>Bad Request<br />" + "</p>" + "</body></html>";
 
 		}
 		return new HTML(html.getBytes(), html.length());
 	}
 
 	public RebuiltHeader modifiedContentLength(int contentLength) {
 		Map<String, String> allHeaders = headers.getAllHeaders();
 		String sb = "";
 
 		allHeaders.remove("Accept-Encoding");
 		allHeaders.remove("Proxy-Connection");
 		allHeaders.put("Accept-Encoding", "identity");
 		allHeaders.remove("Content-Length");
 		allHeaders.remove("Transfer-Encoding");
 		allHeaders.put("Content-Length", String.valueOf(contentLength));
 		sb += allHeaders.get("HTTPVersion") + " ";
 		sb += allHeaders.get("StatusCode") + " ";
 		sb += allHeaders.get("Reason") + "\r\n";
 
 		for (String key : allHeaders.keySet()) {
 			if (!key.equals("HTTPVersion") && !key.equals("StatusCode")
 					&& !key.equals("Reason"))
 				sb += (key + ":" + allHeaders.get(key) + "\r\n");
 		}
 		// allHeaders.put("Via", " mu0Proxy");
 		// sb += "Via:" + allHeaders.get("Via") + "\r\n";
 		sb += ("\r\n");
 		return new RebuiltHeader(sb.getBytes(), sb.length());
 	}
 
 	public RebuiltHeader rebuildHeaders() {
 		Map<String, String> allHeaders = headers.getAllHeaders();
 		try {
 			URL url = new URL(allHeaders.get("RequestedURI"));
			String path = url.getPath();
			if(url.getQuery() != null)
				path += "?" + url.getQuery();
			allHeaders.put("RequestedURI", path);
 		} catch (MalformedURLException e) {
 		}
 		final StringBuilder sb = new StringBuilder();
 
 		if (requestImage()) {
 			allHeaders.remove("Accept-Encoding");
 		}
 		allHeaders.remove("Proxy-Connection");
 		allHeaders.put("Connection", "keep-alive");
 		sb.append(allHeaders.get("Method")).append(" ");
 		sb.append(allHeaders.get("RequestedURI")).append(" ");
 		sb.append(allHeaders.get("HTTPVersion")).append("\r\n");
 
 		for (String key : allHeaders.keySet()) {
 			if (!key.equals("Method") && !key.equals("RequestedURI")
 					&& !key.equals("HTTPVersion"))
 				sb.append(key).append(": ").append(allHeaders.get(key))
 						.append("\r\n");
 		}
 		if (requestImage()) {
 			sb.append("Accept-Encoding: identity\r\n");
 		}
 		// sb.append("Via: mu0-Proxy\r\n");
 		sb.append("\r\n");
 		return new RebuiltHeader(sb.toString().getBytes(), sb.toString()
 				.length());
 	}
 
 	public RebuiltHeader rebuildResponseHeaders() {
 		Map<String, String> allHeaders = headers.getAllHeaders();
 		StringBuilder sb = new StringBuilder();
 
 		 allHeaders.remove("Connection");
 		// allHeaders.put("Connection", "keep-alive");
 		// allHeaders.put("Connection", "close");
 		sb.append(allHeaders.get("HTTPVersion")).append(" ");
 		sb.append(allHeaders.get("StatusCode")).append(" ");
 		sb.append(allHeaders.get("Reason")).append("\r\n");
 		sb.append("Connection: keep-alive\r\n");
 
 		for (String key : allHeaders.keySet()) {
 			if (!key.equals("HTTPVersion") && !key.equals("StatusCode")
 					&& !key.equals("Reason"))
 				sb.append(key).append(": ")
 						.append(allHeaders.get(key) + "\r\n");
 		}
 		// sb.append("Via: mu0-Proxy\r\n");
 		sb.append("\r\n");
 		return new RebuiltHeader(sb.toString().getBytes(), sb.toString()
 				.length());
 	}
 
 	private boolean requestImage() {
 		String accept = headers.getHeader("Accept");
 		if (accept != null)
 			return accept.contains("image");
 		return false;
 	}
 
 }
