 package model;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.Map.Entry;
 
 import exceptions.BadResponseException;
 import exceptions.BadServerException;
 
 public class HttpResponseImpl extends HttpMsg {
 
 	private int statusCode;
 	private String reasonPhrase;
 	private String pVersion;
 	private byte[] entityBody;
 	private boolean completed = false;
 	// private int limit;
 	private boolean contentByClosedConnection = false;
 
 	public HttpResponseImpl(final InputStream in) throws BadResponseException {
 		super(in);
 
 		final String[] requestLine = this.readLine().split(" ", 3);
 
 		this.parseFirstLine(requestLine);
 
 		final String empty = "";
 		String s = null;
 		while (!empty.equals(s = this.readLine())) {
 			final String[] headerLine = s.split(":", 2);
 			this.appendHeader(headerLine[0].trim(), headerLine[1].trim());
 		}
 
 		if ("close".equals(this.getHeader("Proxy-Connection"))
 				|| "close".equals(this.getHeader("Connection"))) {
 			this.contentByClosedConnection = true;
 		}
 	}
 
 	public String getProtocolVersion() {
 		return this.pVersion;
 	}
 
 	public String getReasonPhrase() {
 		return this.reasonPhrase;
 	}
 
 	public int getStatusCode() {
 		return this.statusCode;
 	}
 
 	@Override
 	void parseFirstLine(final String[] line) {
 
 		if (line.length != 2 && line.length != 3) {
 			throw new BadResponseException();
 		}
 
 		final String pVersion = line[0];
 		if (!"HTTP/1.1".equals(pVersion) && !"HTTP/1.0".equals(pVersion)) {
 			throw new BadResponseException("Protocolo no soportado.");
 		}
 		this.pVersion = pVersion;

 		try {
 			this.statusCode = Integer.valueOf(line[1]);
 		} catch (final NumberFormatException e) {
 			throw new BadResponseException();
 		}
 
 		if (line.length == 3) {
 			this.reasonPhrase = (line[2]);
 		} else {
 			this.reasonPhrase = "";
 		}
 
 	}
 
 	// public void setLengthLimit(final Integer limit) {
 	// this.limit = limit;
 	// }
 
 	public byte[] getEntityBody() {
 		if (!this.completed) {
 			final ByteArrayOutputStream out = new ByteArrayOutputStream();
 			this.writeBodyStream(out);
 			this.entityBody = out.toByteArray();
 		}
 		return this.entityBody;
 	}
 
 	void write(final OutputStream out, final int b) {
 
 		try {
 			out.write(b);
 		} catch (final IOException e) {
 			System.out.println("PROBLEMA DE CONEXION CON EL CLIENTE");// TODO
 																		// Auto-generated
 																		// catch
 																		// block
 			e.printStackTrace();
 		}
 
 	}
 
 	void write(final OutputStream out, final byte[] bytes) {
 
 		try {
 			out.write(bytes);
 		} catch (final IOException e) {
 			System.out.println("PROBLEMA DE CONEXION CON EL CLIENTE");// TODO
 																		// Auto-generated
 																		// catch
 																		// block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void writeStream(final OutputStream out) {
 
 		byte[] bytes;
 
 		try {
 			bytes = (this.getProtocolVersion() + " " + this.getStatusCode()
 					+ " " + this.getReasonPhrase() + "\r\n").getBytes();
 			out.write(bytes);
 
 			for (final Entry<String, List<String>> e : this.getHeaders()
 					.entrySet()) {
 				bytes = (e.getKey() + ": " + e.getValue().get(0) + "\r\n")
 						.getBytes();
 				// TODO: Chequear esto
 				out.write(bytes);
 			}
 			bytes = "\r\n".getBytes();
 			out.write(bytes);
 			this.writeBodyStream(out);
 		} catch (final IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 	@Override
 	void writeBodyStream(final OutputStream out) {
 
 		if (this.getHeader("Content-Length") != null) {
 			int clength = Integer.valueOf(this.getHeader("Content-Length"));
 
 			while (--clength >= 0) {
 				final int c = this.read();
 				if (c == -1) {
 					throw new BadServerException();
 				}
 				this.write(out, c);
 			}
 		} else if (this.contentByClosedConnection) {
 			int c;
 			while ((c = this.read()) != -1) {
 				this.write(out, c);
 			}
 		}
 
 	}
 
 	@Override
 	String getHost() {
 		return this.getHeader("Host");
 	}
 
 	@Override
 	public String toString() {
 		final StringBuffer b = new StringBuffer();
 
 		b.append(this.getProtocol() + " " + this.getStatusCode() + " "
 				+ this.getReasonPhrase() + "\r\n");
 		for (final Entry<String, List<String>> e : this.getHeaders().entrySet()) {
 			for (final String headerValue : e.getValue()) {
 				b.append(e.getKey() + ": " + headerValue + "\r\n");
 			}
 		}
 		b.append("\r\n");
 		return b.toString();
 	}
 }
