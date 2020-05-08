 package ar.edu.it.itba.pdc.v2.implementations.utils;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
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
 	private boolean generatingKeep = false;
 	private String keepReadingBytesHexa;
 	private Configurator configurator;
 
 	public DecoderImpl(int buffSize) {
 		headers = new HTTPPacket();
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
 		String read = null;
 		read = new String(data).substring(0, count);
 		String[] lines = read.split("\r\n");
 		boolean found = false;
 		ByteBuffer buffer = ByteBuffer.allocate(count - headers.getReadBytes());
 		for (int i = 0; i < lines.length; i++) {
 			if (found) {
 				if (i < lines.length - 1)
 					lines[i] += "\r\n";
 				buffer.put(lines[i].getBytes());
 			}
 			if (lines[i].equals("")) {
 				found = true;
 			}
 
 		}
 
 		return buffer.array();
 	}
 
 	public boolean keepReading() {
 		return read;
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
 
 	public synchronized byte[] getRotatedImage() {
 		Transformations im = new Transformations();
 		// InputStream is = null;
 		// try {
 		// is = new BufferedInputStream(new FileInputStream((fileName)));
 		// } catch (FileNotFoundException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// }
 
 		byte[] modified = im.rotate(fileName, 180);
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
 		String read = null;
 		read = new String(bytes).substring(0, count);
 		String[] lines = read.split("\r\n");
 
 		for (String line : lines) {
 			if (line.equals("")) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public synchronized void analize(byte[] bytes, int count) {
 		if (!headers.contentExpected()) {
 			keepReadingBytes = 0;
 			read = false;
 			return;
 		}
 		String[] array = headers.getHeader("Content-Type").split(";");
 		String charset;
 		if (array.length >= 2)
 			charset = array[1].split("=")[1];
 		else
 			charset = "ISO-8859-1";
 		CharBuffer charBuf = Charset.forName(charset).decode(
 				ByteBuffer.wrap(bytes, 0, count));
 		String converted = new String(charBuf.array());
 		if (isChunked()) {
 			String[] chunks = null;
 			chunks = converted.split("\r\n");
 			for (int j = 0; j < chunks.length; j++) {
 				if (keepReadingBytes == 0) {
 					Integer sizeLine = null;
 					try {
 						sizeLine = Integer.parseInt(chunks[j], 16);
 						keepReadingBytesHexa = chunks[j];
 					} catch (NumberFormatException e) {
 						sizeLine = 0;
 					}
 					if (sizeLine == 0) {
 						read = false;
 					}
 					keepReadingBytes = sizeLine;
 					generatingKeep = true;
 				} else {
 					if (generatingKeep && j == 0) {
 						try {
 							Integer.parseInt(chunks[0], 16);
 							keepReadingBytesHexa += chunks[0];
 							keepReadingBytes = Integer.parseInt(
 									keepReadingBytesHexa, 16);
 							continue;
 						} catch (NumberFormatException e) {
 						}
 					}
 					keepReadingBytes -= chunks[j].getBytes().length;
 					if (keepReadingBytes < 0) {
 						System.out
 								.println("ESTO NO DEBERIA PASAR, keepReadingBytes <0");
 					}
 					generatingKeep = false;
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
 		generatingKeep = false;
 		keepReadingBytesHexa = "";
 		isImage = false;
 		isText = false;
 	}
 
 	public void parseHeaders(byte[] data, int count) {
 		headers.parseHeaders(data, count);
 	}
 
 	public HTTPHeaders getHeaders() {
 		return headers;
 	}
 
 	public RebuiltHeader rebuildHeaders() {
 		Map<String, String> allHeaders = headers.getAllHeaders();
 		String sb = "";
 
 		allHeaders.remove("Accept-Encoding");
 		allHeaders.remove("Proxy-Connection");
 		allHeaders.put("Accept-Encoding", "identity");
 		sb += allHeaders.get("Method") + " ";
 		sb += allHeaders.get("RequestedURI") + " ";
 		sb += allHeaders.get("HTTPVersion") + "\r\n";
 
 		for (String key : allHeaders.keySet()) {
 			if (!key.equals("Method") && !key.equals("RequestedURI")
 					&& !key.equals("HTTPVersion"))
 				sb += (key + ":" + allHeaders.get(key) + "\r\n");
 		}
 		// allHeaders.put("Via", " mu0Proxy");
 		// sb += "Via:" + allHeaders.get("Via") + "\r\n";
 		sb += ("\r\n");
 		return new RebuiltHeader(sb.getBytes(), sb.length());
 	}
 
 	public void setConfigurator(Configurator configurator) {
 		this.configurator = configurator;
 
 	}
 
 	public RebuiltHeader generateBlockedHeader(String cause) {
 		HTTPHeaders newHeaders = new HTTPPacket();
 		if (cause.equals("URI")) {
 			newHeaders.addHeader("StatusCode", "666");
 			newHeaders.addHeader("Reason", "Blocked URL");
 		} else if (cause.equals("CONTENT-TYPE")) {
 			newHeaders.addHeader("StatusCode", "777");
 			newHeaders.addHeader("Reason", "Blocked MediaType");
 		} else if (cause.equals("IP")) {
 			newHeaders.addHeader("StatusCode", "888");
 			newHeaders.addHeader("Reason", "Blocked IP");
 		} else if (cause.equals("MAXSIZE")) {
 			newHeaders.addHeader("StatusCode", "999");
 			newHeaders.addHeader("Reason", "Blocked File Size");
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
 		if (cause.equals("URI")) {
 			html = "<!DOCTYPE HTML PUBLIC ''-//IETF//DTD HTML 2.0//EN'>"
 					+ "<html><head>" + "<title>666 URL bloqueada</title>"
 					+ "</head><body>" + "<h1>URL Bloqueada</h1>"
 					+ "<p>Su proxy bloqueo esta url<br />" + "</p>"
 					+ "</body></html>";
 
 		} else if (cause.equals("CONTENT-TYPE")) {
 			html = "<!DOCTYPE HTML PUBLIC ''-//IETF//DTD HTML 2.0//EN'>"
 					+ "<html><head>" + "<title>777 MediaType bloqueada</title>"
 					+ "</head><body>" + "<h1>MediaType Bloqueada</h1>"
 					+ "<p>Su proxy bloqueo este tipo de archivos<br />"
 					+ "</p>" + "</body></html>";
 
 		} else if (cause.equals("IP")) {
 			html = "<!DOCTYPE HTML PUBLIC ''-//IETF//DTD HTML 2.0//EN'>"
 					+ "<html><head>" + "<title>888 IP bloqueada</title>"
 					+ "</head><body>" + "<h1>IP Bloqueada</h1>"
 					+ "<p>Su proxy bloqueo esta IP<br />" + "</p>"
 					+ "</body></html>";
 
 		} else if (cause.equals("MAXSIZE")) {
 			html = "<!DOCTYPE HTML PUBLIC ''-//IETF//DTD HTML 2.0//EN'>"
 					+ "<html><head>"
 					+ "<title>999 Tamano de archivo bloqueado</title>"
 					+ "</head><body>" + "<h1>Tamano de archivo Bloqueada</h1>"
 					+ "<p>Su proxy bloqueo archivos de este tamano<br />"
 					+ "</p>" + "</body></html>";
 
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
 			if (!key.equals("Method") && !key.equals("RequestedURI")
 					&& !key.equals("HTTPVersion"))
 				sb += (key + ":" + allHeaders.get(key) + "\r\n");
 		}
 		// allHeaders.put("Via", " mu0Proxy");
 		// sb += "Via:" + allHeaders.get("Via") + "\r\n";
 		sb += ("\r\n");
 		return new RebuiltHeader(sb.getBytes(), sb.length());
 	}
 
 }
