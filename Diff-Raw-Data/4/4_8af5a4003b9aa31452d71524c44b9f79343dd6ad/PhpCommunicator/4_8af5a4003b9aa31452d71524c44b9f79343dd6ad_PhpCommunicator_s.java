 package com.sk;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.URLDecoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 
 import org.apache.commons.codec.binary.Base64;
 
 import com.google.gson.JsonObject;
 import com.sk.stat.PersonStatistics;
 import com.sk.stat.StatisticsController;
 import com.sk.util.PersonalDataStorage;
 
 public class PhpCommunicator implements Runnable {
 
 	private final Socket sock;
 	private final SearchController searcher;
 	private MessageDigest digest;
 
 	private final String receiveShake = "GrabName";
 	private final String sendShake = "NameGrabber";
 
 	public PhpCommunicator(Socket sock, SearchController searcher) {
 		this.sock = sock;
 		this.searcher = searcher;
 		try {
 			this.digest = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void run() {
 		System.out.println("Talking to socket");
 		try {
 			if (sock.isClosed() || sock.isInputShutdown() || sock.isOutputShutdown() || !sock.isConnected()) {
 				System.out.println("Bad socket on start");
 				return;
 			}
 			sock.setKeepAlive(true);
 			BufferedReader read = new BufferedReader(new InputStreamReader(sock.getInputStream()));
 			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())));
 			String line = read.readLine();
 			System.out.println("Received handshake " + line);
 			String[] parts = line.split("[|]");
 			if (parts.length != 2 || parts[0].length() < 5) {
 				System.out.println("Bad input handshake");
 				sock.close();
 				return;
 			}
 			if (!Arrays.equals(digest.digest((parts[0] + receiveShake).getBytes()), Base64.decodeBase64(parts[1])))
 				return;
 			String curTime = System.currentTimeMillis() + "";
 			out.println(curTime + "|" + Base64.encodeBase64String(digest.digest((curTime + sendShake).getBytes())));
 			out.flush();
 			String request = read.readLine();
 			System.out.println("Received names " + request);
 			String[] names = request.split("[|]");
 			if (names.length != 2) {
 				System.out.println("Bad names separation");
 				sock.close();
 				return;
 			}
 			JsonObject result = new JsonObject();
 			String first = URLDecoder.decode(names[0], "UTF-8"), last = URLDecoder.decode(names[1], "UTF-8");
 			long start = System.currentTimeMillis();
 			if (!searcher.lookForName(first, last)) {
 				result.addProperty("error", "Could not find names");
 			} else {
 				PersonalDataStorage store = searcher.getDataStorage();
 				if (store == null) {
 					result.addProperty("error", "Failed to get data storage");
 				} else {
 					System.out.printf("Found %d results in %d millis%n", store.size(), System.currentTimeMillis()
 							- start);
 					PersonStatistics stat = StatisticsController.get().generateStat(first, last, store.toArray());
 					if (stat == null) {
 						result.addProperty("error", "Failed to generate statistics");
 					} else {
 						result.add("stat", Driver.getGson().toJsonTree(stat, PersonStatistics.class));
 					}
 				}
 			}
 			out.println(result);
 			out.flush();
 			sock.close();
		} catch (IOException ex) {
 			ex.printStackTrace();
 		} finally {
 			System.out.println("Done talking");
 		}
 	}
 }
