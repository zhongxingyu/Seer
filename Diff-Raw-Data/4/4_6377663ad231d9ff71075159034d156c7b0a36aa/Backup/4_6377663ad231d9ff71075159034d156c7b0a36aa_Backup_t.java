 /**
  * Copyright (C) 2009 Patrick Estarian
  * 
  * FriendFeed Backup - downloads the feeds and any thumbnail, image, or
  * other files attached to the feed to your local file system.
  * 
  *  
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package org.patrickestarian.ff;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.InetSocketAddress;
 import java.net.Proxy;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Unmarshaller;
 
 import org.patrickestarian.ff.schema.Entry;
 import org.patrickestarian.ff.schema.EntryType;
 import org.patrickestarian.ff.schema.Feed;
 import org.patrickestarian.ff.schema.From;
 import org.patrickestarian.ff.schema.Thumbnail;
 
 import sun.misc.BASE64Encoder;
 
 public class Backup {
 	private static int RAW_HTML = 1;
 	private static int ENTRIES_PER_FEED = 100;
 	private static int MAX_COMMENTS = 10000;
 	private static int MAX_LIKES = 10000;
 	private static String ROOT = "/backup/ff";
 	private static String PROXY = null;
 	private static String CREDENTIALS = null;
 	private static HashSet<String> DownloadedFiles = new HashSet<String>();
 	private static List<Entry> AllEntries = new ArrayList<Entry>();
 	private static HashSet<String> EntryIDs = new HashSet<String>();
 	private static HashSet<String> EntryDirs = new HashSet<String>();
 	private static Feed UserFeed;
 	private static boolean VERBOSE = false;
 	private static boolean FORCE_DOWNLOAD = false;
 	private static String CONFIG_FILE = "config.properties";
 	private static long StartTime = System.currentTimeMillis();
 	
 
 	private static HashMap<String, String> CONTENT_TYPES = new HashMap<String, String>();
 
 	static {
 		CONTENT_TYPES.put("text/html", ".html");
 		CONTENT_TYPES.put("text/plain", ".txt");
 		CONTENT_TYPES.put("text/richtext", ".doc");
 		CONTENT_TYPES.put("text/css", ".css");
 		CONTENT_TYPES.put("image/gif", ".gif");
 		CONTENT_TYPES.put("image/x-png", ".pmg");
 		CONTENT_TYPES.put("image/jpeg", ".jpg");
 		CONTENT_TYPES.put("image/tiff", ".tif");
 		CONTENT_TYPES.put("image/vnd.svf", ".svf");
 		CONTENT_TYPES.put("vector/x-svf", ".svf");
 		CONTENT_TYPES.put("audio/x-wav", ".wav");
 		CONTENT_TYPES.put("audio/x-mpeg", ".mp3");
 		CONTENT_TYPES.put("audio/x-mpeg-2", ".mp2");
 		CONTENT_TYPES.put("video/mpeg", ".mpg");
 		CONTENT_TYPES.put("video/quicktime", ".mov");
 		CONTENT_TYPES.put("video/x-msvideo", ".avi");
 		CONTENT_TYPES.put("video/x-sgi-movie", ".mov");
 		CONTENT_TYPES.put("application/xml", ".xml");
 		CONTENT_TYPES.put("application/pdf", ".pdf");
 		CONTENT_TYPES.put("application/x-pdf", ".pdf");
 		CONTENT_TYPES.put("application/msword", ".doc");
 		CONTENT_TYPES.put("application/zip", ".zip");
 		CONTENT_TYPES.put("application/octet-stream", ".exe");
 	}
 
 	// feedinfo:
 	// http://friendfeed-api.com/v2/feedinfo/patrickestarian?format=xml
 	//
 	// feedlist:
 	// http://friendfeed-api.com/v2/feedlist?format=xml
 	//
 
 	public Backup() {
 		try {
 			Properties config = new Properties();
 	        File configFile = new File(CONFIG_FILE);
 	        if (!configFile.exists()) {
 	        	System.err.println("ERROR: could not find " + CONFIG_FILE);
 	        	return;
 	        }
 	        
 	        FileInputStream fis = new FileInputStream(configFile);
 	        config.load(fis);
 	        
 	        String userID = config.getProperty("user_id");
 	        if (userID == null  ||  userID.trim().equals("")) {
 	        	System.err.println("Please set the user_id in " + CONFIG_FILE);
 	        	return;
 	        }
 	        
 	        String remoteKey = config.getProperty("remote_key");
 	        if (remoteKey != null &&  !remoteKey.trim().equals("")) {
 	        	CREDENTIALS = userID + ":" + remoteKey;
 	        }
 	        
 	        try {
 	        	MAX_COMMENTS = Integer.parseInt(config.getProperty("max_comments"));
 			} catch (Exception e) {
 			}
 	        
 			try {
 	        	MAX_LIKES = Integer.parseInt(config.getProperty("max_likes"));
 			} catch (Exception e) {
 			}
 			
 	        PROXY = config.getProperty("proxy");
 	        if (PROXY != null  &&  !PROXY.trim().equals("")) {
 	        	PROXY = PROXY.trim();
 	        } else {
 	        	PROXY = null;
 	        }
 	        
	        ROOT = config.getProperty("backup_dir");
	        if (ROOT == null  ||  ROOT.trim().equals("")) {
 	        	System.err.println("Please set the backup_dir in " + CONFIG_FILE);
 	        	return;
 	        }
 	        
 	        String verbose = config.getProperty("verbose");
 	        if (verbose != null  &&  verbose.trim().equalsIgnoreCase("true")) {
 	        	VERBOSE = true;
 	        }
 
 	        String forceDownload = config.getProperty("force_download");
 	        if (forceDownload != null  &&  forceDownload.trim().equalsIgnoreCase("true")) {
 	        	FORCE_DOWNLOAD = true;
 	        }
 
 			loadAllUserFeedsFromFF(userID);
 	        
 	        //downloadMedia("http://img.youtube.com/vi/6ixwKBwfT38/2.jpg", "/backup/ff/youtube.jpg");
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void loadAllUserFeedsFromFF(String userID) throws Exception {
 
 		loadUserFeedsFromFF(userID);
 		
 		if (CREDENTIALS != null) {
 			loadDirectFeedsFromFF(userID);
 			loadDiscussionFeedsFromFF(userID);
 		}
 
 		UserFeed.setEntry(AllEntries);
 
 		for (Entry entry : AllEntries) {
 			downloadEntry(userID, entry);
 		}
 	}
 
 	public void loadUserFeedsFromFF(String userID) throws Exception {
 		String userHome = ROOT + "/" + userID;
 		File userHomeDir = new File(userHome);
 		userHomeDir.mkdirs();
 
 		int start = 0;
 		while (true) {
 			String from = "http://friendfeed-api.com/v2/feed/" + userID + "?format=xml&raw=" + RAW_HTML + "&maxcomments=" + MAX_COMMENTS + "&maxlikes=" + MAX_LIKES + "&num=" + ENTRIES_PER_FEED + "&start=" + start;
 			String fileName = userID + "-" + toStringWithZeros(start) + "-" + toStringWithZeros(start + ENTRIES_PER_FEED - 1) + ".xml";
 
 			String filePath = downloadXML(from, userHome + "/" + fileName);
 
 			Feed feed = loadXML(filePath);
 			if (feed == null) {
 				start += ENTRIES_PER_FEED;
 				continue;
 			}
 
 			if (UserFeed == null) {
 				UserFeed = new Feed();
 				UserFeed.setDescription(feed.getDescription());
 				UserFeed.setId(feed.getId());
 				UserFeed.setName(feed.getName());
 				UserFeed.setSupId(feed.getSupId());
 				UserFeed.setType(feed.getType());
 			}
 
 			List<Entry> entries = feed.getEntry();
 			if (entries == null || entries.size() == 0) {
 				File emptyFeed = new File(filePath);
 				emptyFeed.delete();
 				System.out.println("deleted\tempty_feed");
 				break;
 			}
 			
 			System.out.println("done");
 			
 			for (Entry entry : entries) {
 				String feedID = feed.getId();
 				if (EntryIDs.contains(feedID)) {
 					continue;
 				}
 
 				entry.setEntryType(EntryType.Mine);
 
 				AllEntries.add(entry);
 				EntryIDs.add(entry.getId());
 			}
 
 			start += ENTRIES_PER_FEED;
 		}
 	}
 
 	public void loadDirectFeedsFromFF(String userID) throws Exception {
 		String userHome = ROOT + "/" + userID;
 		File userHomeDir = new File(userHome);
 		userHomeDir.mkdirs();
 
 		String firstFeedID = "";
 		String lastFeedID = "";
 		int start = 0;
 		while (true) {
 			String from = "http://friendfeed-api.com/v2/feed/filter/direct?format=xml&raw=" + RAW_HTML + "&maxcomments=" + MAX_COMMENTS + "&maxlikes=" + MAX_LIKES + "&num=" + ENTRIES_PER_FEED + "&start=" + start;
 			String fileName = "direct-" + toStringWithZeros(start) + "-" + toStringWithZeros(start + ENTRIES_PER_FEED - 1) + ".xml";
 
 			String filePath = downloadXML(from, userHome + "/" + fileName);
 
 			Feed feed = loadXML(filePath);
 			if (feed == null) {
 				start += ENTRIES_PER_FEED;
 				continue;
 			}
 
 			boolean endOfFeed = false;
 			List<Entry> entries = feed.getEntry();
 			if (entries == null || entries.size() == 0) {
 				endOfFeed = true;
 			} else {
 				firstFeedID = entries.get(0).getId();
 				if (firstFeedID.equals(lastFeedID)) { // reached the end of the feed. it's repeating the same thing now
 					endOfFeed = true;
 				}
 			}
 
 			if (endOfFeed) {
 				System.out.println("deleted\trepeated_feed");
 				File repeatedFeed = new File(filePath);
 				repeatedFeed.delete();
 				break;
 			}
 			
 			System.out.println("done");
 			
 			lastFeedID = firstFeedID;
 
 			for (Entry entry : entries) {
 				String feedID = feed.getId();
 				if (EntryIDs.contains(feedID)) {
 					continue;
 				}
 
 				entry.setEntryType(EntryType.Direct);
 
 				AllEntries.add(entry);
 				EntryIDs.add(entry.getId());
 			}
 
 			start += ENTRIES_PER_FEED;
 		}
 	}
 
 	public void loadDiscussionFeedsFromFF(String userID) throws Exception {
 		String userHome = ROOT + "/" + userID;
 		File userHomeDir = new File(userHome);
 		userHomeDir.mkdirs();
 
 		String firstFeedID = "";
 		String lastFeedID = "";
 		int start = 0;
 		while (true) {
 			String from = "http://friendfeed-api.com/v2/feed/filter/discussions?format=xml&raw=" + RAW_HTML + "&maxcomments=" + MAX_COMMENTS + "&maxlikes=" + MAX_LIKES + "&num=" + ENTRIES_PER_FEED + "&start=" + start;
 			String fileName = "discussions-" + toStringWithZeros(start) + "-" + toStringWithZeros(start + ENTRIES_PER_FEED - 1) + ".xml";
 
 			String filePath = downloadXML(from, userHome + "/" + fileName);
 
 			Feed feed = loadXML(filePath);
 			if (feed == null) {
 				start += ENTRIES_PER_FEED;
 				continue;
 			}
 
 			boolean endOfFeed = false;
 			List<Entry> entries = feed.getEntry();
 			if (entries == null || entries.size() == 0) {
 				endOfFeed = true;
 			} else {
 				firstFeedID = entries.get(0).getId();
 				if (firstFeedID.equals(lastFeedID)) { // reached the end of the feed. it's repeating the same thing now
 					endOfFeed = true;
 				}
 			}
 
 			if (endOfFeed) {
 				System.out.println("deleted\trepeated_feed");
 				File repeatedFeed = new File(filePath);
 				repeatedFeed.delete();
 				break;
 			}
 
 			System.out.println("done");
 			
 			lastFeedID = firstFeedID;
 
 			for (Entry entry : entries) {
 				String feedID = feed.getId();
 				if (EntryIDs.contains(feedID)) {
 					continue;
 				}
 
 				From fromElement = entry.getFrom();
 				if (fromElement.getPrivate() != null) {
 					entry.setEntryType(EntryType.Direct);
 				} else {
 					String fromUser = fromElement.getId();
 					if (fromUser.equals(userID)) {
 						entry.setEntryType(EntryType.Mine);
 					} else {
 						entry.setEntryType(EntryType.Discussions);
 					}
 				}
 
 				AllEntries.add(entry);
 				EntryIDs.add(entry.getId());
 			}
 
 			start += ENTRIES_PER_FEED;
 		}
 	}
 
 	public void downloadEntry(String userID, Entry entry) throws Exception {
 
 		String entryName = entry.getUrl();
 		entryName = entryName.substring(entryName.lastIndexOf('/') + 1);
 		if (entryName.length() > 32) {
 			entryName = entryName.substring(0, 32);
 		}
 
 		int t = 0;
 		String entryDirName = entryName;
 		while (EntryDirs.contains(entryDirName)) {
 			entryDirName = entryName + "_" + toStringWithZeros(++t, 2);
 		}
 		EntryDirs.add(entryDirName);
 
 		String entryDir = ROOT + "/" + userID + "/" + entryDirName;
 
 		HashMap<String, String> downloads = new HashMap<String, String>();
 		
 		List<Thumbnail> thumbnails = entry.getThumbnail();
 		List<org.patrickestarian.ff.schema.File> files = entry.getFile();
 		
 		t = 0;
 		for (Thumbnail thumbnail : thumbnails) {
 			String thumbnailURL = thumbnail.getUrl();
 			if (thumbnailURL.indexOf("friendfeed-media") > 0) {
 				String thumbnailFileName = "thumbnail_" + toStringWithZeros(++t, 2) + ".jpg";
 				String urlFilePath = entryDir + "/" + thumbnailFileName;
 				//downloadMedia(thumbnailLink, linkFilePath);
 				downloads.put(thumbnailURL, urlFilePath);
 			}
 
 			String thumbnailLink = thumbnail.getLink();
 			if (thumbnailLink.indexOf("friendfeed-media") > 0  ||  thumbnailLink.toLowerCase().endsWith(".jpg")) {
 				//downloadMedia(thumbnailURL, entryDir);
 				downloads.put(thumbnailLink, entryDir);
 			}
 		}
 
 		for (org.patrickestarian.ff.schema.File file : files) {
 			String fileURL = file.getUrl();
 			//downloadMedia(fileURL, entryDir);
 			downloads.put(fileURL, entryDir);
 		}
 
 		if (downloads.size() == 0) {
 			return;
 		}
 		
 		File entryDirFile = new File(entryDir);
 		if (!entryDirFile.exists()) {
 			entryDirFile.mkdirs();
 		}
 		
 		Collection<String> froms = downloads.keySet();
 		for (String from : froms) {
 			String to = downloads.get(from);
 			downloadMedia(from, to);
 		}
 	}
 
 	public Feed loadXML(String xmlFilePath) throws Exception {
 		File file = new File(xmlFilePath);
 		if (!file.exists()) {
 			return null;
 		}
 
 		JAXBContext jc = JAXBContext.newInstance("org.patrickestarian.ff.schema");
 		Unmarshaller u = jc.createUnmarshaller();
 		Feed feed = (Feed) u.unmarshal(file);
 
 		return feed;
 	}
 
 	public String downloadXML(String from, String to) throws Exception {
 		URL url = new URL(from);
 
 		String path;
 		String host = url.getHost();
 
 		Socket socket;
 		if (PROXY == null) {
 			int port = url.getPort();
 			if (port < 0) {
 				port = 80;
 			}
 			socket = new Socket(url.getHost(), port);
 			path = url.getPath() + "?" + url.getQuery();
 		} else {
 			String proxyHost = null;
 			int proxyPort = -1;
 			try {
 				int idx = PROXY.indexOf(":");
 				proxyHost = PROXY.substring(0, idx);
 				String sport = PROXY.substring(idx + 1);
 				proxyPort = Integer.valueOf(sport);
 			} catch (Exception e) {
 			}
 			if (proxyPort < 0) {
 				new Exception("Invalid proxy");
 			}
 			socket = new Socket(proxyHost, proxyPort);
 			path = url.toString();
 		}
 
 		ArrayList<String> requestHeaders = new ArrayList<String>();
 		requestHeaders.add("GET " + path + " HTTP/1.1");
 		requestHeaders.add("Host: " + host);
 		requestHeaders.add("Accept: */*");
 
 		if (PROXY != null) {
 			requestHeaders.add("Proxy-Connection: Keep-Alive");
 		}
 
 		if (CREDENTIALS != null) {
 			String encoding = new BASE64Encoder().encode(CREDENTIALS.getBytes());
 			requestHeaders.add("Authorization: Basic " + encoding);
 		}
 
 		Writer writer = new OutputStreamWriter(socket.getOutputStream(), "UTF8");
 		for (String requestHeader : requestHeaders) {
 			if (VERBOSE) {
 				System.out.println("> " + requestHeader);
 			}
 			writer.write(requestHeader);
 			writer.write("\r\n");
 		}
 		writer.write("\r\n");
 		writer.flush();
 
 		InputStream in = socket.getInputStream();
 		BufferedInputStream bin = new BufferedInputStream(in);
 
 		int contentLength = -1;
 		String line;
 		while ((line = readLine(bin)) != null) {
 			if (VERBOSE) {
 				System.out.println("< " + line);
 			}
 			String header = line.toLowerCase();
 			if (header.startsWith("content-length:")) {
 				try {
 					String cl = line.substring("content-length:".length() + 1);
 					contentLength = Integer.parseInt(cl);
 				} catch (Exception e) {
 				}
 			}
 		}
 
 		String filePath = to;
 
 		System.out.print("Downloading\t" + from + "\t" + contentLength + "\t" + filePath + "\t");
 
 		String dir = filePath.substring(0, filePath.lastIndexOf('/'));
 		File dirFile = new File(dir);
 		if (!dirFile.exists()) {
 			dirFile.mkdirs();
 		}
 
 		FileOutputStream out = new FileOutputStream(filePath);
 		byte[] data = new byte[4096];
 		int blockSize = data.length;
 		int bytesRead = 0;
 		int totalBytesRead = 0;
 		while (totalBytesRead < contentLength) {
 			bytesRead = contentLength - totalBytesRead;
 			if (blockSize > bytesRead) {
 				blockSize = bytesRead;
 			}
 			bytesRead = bin.read(data, 0, blockSize);
 			totalBytesRead += bytesRead;
 			if (bytesRead == -1) {
 				break;
 			}
 			out.write(data, 0, bytesRead);
 		}
 
 		bin.close();
 		in.close();
 		writer.close();
 		socket.close();
 		out.flush();
 		out.close();
 
 		if (totalBytesRead != contentLength) {
 			//throw new IOException("Only read " + totalBytesRead + " bytes; Expected " + contentLength + " bytes");
 			System.out.println("failed");
 		}
 		
 		return filePath;
 	}
 
 	public String downloadMedia(String from, String to) throws Exception {
 		URL url = new URL(from);
 
 		HttpURLConnection uc;
 		if (PROXY == null) {
 			uc = (HttpURLConnection) url.openConnection();
 		} else {
 			String server = null;
 			int iport = -1;
 			try {
 				int idx = PROXY.indexOf(":");
 				server = PROXY.substring(0, idx);
 				String sport = PROXY.substring(idx + 1);
 				iport = Integer.valueOf(sport);
 			} catch (Exception e) {
 			}
 			if (iport < 0) {
 				throw new Exception("Invalid proxy");
 			}
 			SocketAddress socketAddress = new InetSocketAddress(server, iport);
 			Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddress);
 			uc = (HttpURLConnection) url.openConnection(proxy);
 		}
 
 		String contentType = uc.getContentType();
 		int contentLength = uc.getContentLength();
 		String contentDisposition = null;
 
 		Map<String, List<String>> headers = uc.getHeaderFields();
 		for (String headerName : headers.keySet()) {
 			if (headerName != null && headerName.equalsIgnoreCase("Content-Disposition")) {
 				contentDisposition = headers.get(headerName).toString();
 				break;
 			}
 		}
 
 		String toDir;
 		File toFile = new File(to);
 		String fileName = toFile.getName();
 		if (toFile.isDirectory()) {
 			toDir = to;
 			toFile = null;
 			
 			int idx = -1;
 			try {
 				idx = contentDisposition.indexOf("filename");
 			} catch (Exception e) {
 			}
 
 			if (idx < 0) {
 				String extension = CONTENT_TYPES.get(contentType);
 				int tries = 0;
 				String tempPath;
 				while (true) {
 					tempPath = toDir + "/" + fileName + "_" + toStringWithZeros(++tries, 2);
 					if (extension != null) {
 						tempPath += extension;
 					}
 					toFile = new File(tempPath);
 					long lastModified = toFile.lastModified();
 					boolean oldFile = lastModified < StartTime;
 					if (!toFile.exists()  ||  oldFile) {
 						fileName = toFile.getName();
 						break;
 					}
 				}
 			} else {
 				int s = contentDisposition.indexOf("\"") + 1;
 				int e = contentDisposition.indexOf("\"", s);
 				fileName = contentDisposition.substring(s, e);
 			}
 
 		} else {
 			toDir = to.substring(0, to.lastIndexOf('/'));
 		}
 		
 		String filePath = toDir + "/" + fileName;
 		
 		System.out.print("Downloading\t" + from + "\t" + contentLength + "\t" + filePath + "\t");
 
 		if (toFile == null) {
 			toFile = new File(filePath);
 		}
 		long oldFileSize = -1;
 		if (toFile.exists()) {
 			oldFileSize = toFile.length();
 		}
 
 		boolean alreadyDownloaded = DownloadedFiles.contains(filePath);
 		if (alreadyDownloaded  || ((oldFileSize == contentLength) && !FORCE_DOWNLOAD)) {
 			System.out.println("skipped\talready_exists");
 			return filePath;
 		}
 
 		int httpResponse = uc.getResponseCode();
 		if (httpResponse != HttpURLConnection.HTTP_OK) {
 			if (httpResponse == HttpURLConnection.HTTP_NOT_FOUND) {
 				System.out.println("skipped\t404_not_found");
 			} else {
 				System.out.println("skipped\thttp_error_" + httpResponse);
 			}
 			return filePath;
 		}
 
 		InputStream in = uc.getInputStream();
 		BufferedInputStream bin = new BufferedInputStream(in);
 		FileOutputStream out = new FileOutputStream(filePath);
 		byte[] data = new byte[65535];
 		int blockSize = data.length;
 		int bytesRead = 0;
 		int totalBytesRead = 0;
 		while (totalBytesRead < contentLength) {
 			bytesRead = contentLength - totalBytesRead;
 			if (blockSize > bytesRead) {
 				blockSize = bytesRead;
 			}
 			bytesRead = bin.read(data, 0, blockSize);
 			totalBytesRead += bytesRead;
 			if (bytesRead == -1) {
 				break;
 			}
 			out.write(data, 0, bytesRead);
 		}
 
 		bin.close();
 		in.close();
 		out.flush();
 		out.close();
 
 		DownloadedFiles.add(filePath);
 
 		if (totalBytesRead != contentLength) {
 			//throw new IOException("Only read " + totalBytesRead + " bytes; Expected " + contentLength + " bytes");
 			System.out.println("skipped\terror_only_" + totalBytesRead + "_bytes_sent");
 		} else {
 			System.out.println("done.");
 		}
 		
 		return filePath;
 	}
 
 	private String readLine(InputStream inputStream) throws IOException {
 		int SIZE = 1024;
 		boolean IS_UTF8 = true;
 		char[] carr = new char[SIZE];
 		int idx = 0;
 		int x = 0;
 		while (true) {
 			int b1 = inputStream.read();
 			if (b1 == -1) {
 				break;
 			}
 			if (IS_UTF8) {
 				if (b1 < 0x80) { // 0vvvvvvv
 					x = b1;
 				} else if (b1 < 0xE0) { // 110vvvvv 10vvvvvv
 					int b2 = inputStream.read();
 					x = ((b1 & 0x1F) << 6) + (b2 & 0x3F);
 				} else if (b1 < 0xF0) { // 1110vvvv 10vvvvvv 10vvvvvv
 					int b2 = inputStream.read();
 					int b3 = inputStream.read();
 					x = ((b1 & 0xF) << 12) + ((b2 & 0x3F) << 6) + (b3 & 0x3F);
 				} else if (b1 > 0xF8) { // 11110vvv 10vvvvvv 10vvvvvv 10vvvvvv
 					int b2 = inputStream.read();
 					int b3 = inputStream.read();
 					int b4 = inputStream.read();
 					x = ((b2 & 0x7) << 18) + ((b2 & 0x3F) << 12) + ((b3 & 0x3F) << 6) + (b4 & 0x3F);
 				} else {
 					//throw new IllegalArgumentException("Invalid UTF-8 character");
 					return null;
 				}
 			}
 
 			if (x == 0xA) { // 0xA = '\n'
 				if (carr[idx - 1] == '\r') {
 					idx--;
 				}
 				break;
 			}
 
 			char c = (char) x;
 			if (c == '\n') {
 				break;
 			}
 
 			if (idx == SIZE) {
 				char[] tcarr = new char[idx + SIZE];
 				System.arraycopy(carr, 0, tcarr, 0, idx);
 				carr = tcarr;
 			}
 			carr[idx++] = c;
 		}
 
 		if (idx == 0) {
 			return null;
 		}
 
 		return new String(carr, 0, idx);
 	}
 
 	public String toStringWithZeros(int number) {
 		String result = toStringWithZeros(number, 4);
 		return result;
 	}
 
 	public String toStringWithZeros(int number, int digits) {
 		String result = String.valueOf(number);
 		while (result.length() < digits) {
 			result = "0" + result;
 		}
 		return result;
 	}
 
 	public static void main(String[] args) {
 		if (args.length > 0) {
 			CONFIG_FILE = args[0];
 		}
 		new Backup();
 	}
 
 }
