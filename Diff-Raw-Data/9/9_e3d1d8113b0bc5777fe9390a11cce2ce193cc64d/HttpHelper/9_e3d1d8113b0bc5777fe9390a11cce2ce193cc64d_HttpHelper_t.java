 package ch.furthermore.poorman.resttestframeworkcom.impl;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import org.apache.commons.codec.binary.Base64;
 
 class HttpHelper {
 	public byte[] httpRequest(String verb, String urlString, String data, String optUser, String optPass) { 
 		try {
 			URL url = new URL(urlString);
 			
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			try {
 				if (data != null) {
 					conn.setDoOutput(true);
 				}
 				
 				conn.setRequestMethod(verb.toUpperCase());
 				
 				if (optUser != null && !"".equals(optUser) && optPass != null && !"".equals(optPass)) {
 					addBasicAuthHeader(conn, optUser, optPass);
 				}
 				
 				conn.setRequestProperty("Content-Type", "application/xml");
 				conn.setRequestProperty("Accept", "application/xml");
 				
 				if (data != null) {
 					DataOutputStream os = new DataOutputStream(new BufferedOutputStream(conn.getOutputStream()));
 					try {
 						os.write(data.getBytes());
 					}
 					finally {
 						os.close();
 					}
 				}
 
				BufferedInputStream in;
				try {
					in = new BufferedInputStream(conn.getInputStream());
				}
				catch (IOException e) { //dirty hack
					in = new BufferedInputStream(conn.getErrorStream());
				}
				
 				try {
 					return readAllBytes(in);
 				}
 				finally {
 					in.close();
 				}
 			}
 			finally {
 				conn.disconnect();
 			}
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public byte[] readAllBytes(InputStream in) throws IOException {
 		ByteArrayOutputStream bout = new ByteArrayOutputStream();
 		try {
 			byte[] data = new byte[4096];
 			for (int len = in.read(data); len > 0; len = in.read(data)) {
 				bout.write(data, 0, len);
 			}
 			return bout.toByteArray();
 		}
 		finally {
 			bout.close();
 		}
 	}
 	
 	private void addBasicAuthHeader(HttpURLConnection conn, String user, String pass) {
 		String authString = user + ":" + pass;
 		
 		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
 		
 		conn.setRequestProperty("Authorization", "Basic " + new String(authEncBytes));
 	}
 }
