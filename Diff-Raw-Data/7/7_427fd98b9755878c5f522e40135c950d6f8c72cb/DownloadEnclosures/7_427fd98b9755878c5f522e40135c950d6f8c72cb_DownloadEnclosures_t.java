 package com.munzenberger.feed.handler;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.util.Date;
 
 import com.munzenberger.feed.parser.rss.Enclosure;
 import com.munzenberger.feed.parser.rss.Item;
 import com.munzenberger.feed.ui.MessageDispatcher;
 
 public class DownloadEnclosures implements ItemHandler {
 
 	private String targetDir = ".";
 	
	private boolean overwriteExisting = false;
 	
 	public void setTargetDir(String targetDir) {
 		this.targetDir = targetDir;
 	}
 	
	public void setOverwriteExisting(String overwrite) {
		this.overwriteExisting = Boolean.valueOf(overwrite);
 	}
 	
 	public void process(Item item, MessageDispatcher dispatcher) throws ItemHandlerException {
 		for (Enclosure e : item.getEnclosures()) {
 			File file = process(e.getUrl(), dispatcher);
 			
 			Date date = DateParser.parse(item.getPubDate(), dispatcher);
 			if (date != null) {
 				file.setLastModified(date.getTime());
 			}
 		}
 	}
 	
 	protected File process(String downloadURL, MessageDispatcher dispatcher) throws ItemHandlerException {
 		File file = getLocalFile( downloadURL );
 		
 		URL url;
 		
 		try {
 			url = new URL(downloadURL);
 		} catch (MalformedURLException ex) {
 			throw new ItemHandlerException(ex);
 		}
 		
 		try {
 			download(url, file, dispatcher);
 		}
 		catch (IOException ex) {
 			if (!file.delete()) {
 				dispatcher.info("Could not delete file: " + file);
 			}
 			throw new ItemHandlerException(ex);
 		}
 		
 		return file;
 	}
 	
 	public File getLocalFile(String url) throws ItemHandlerException {
 		String filePath = url;
 		filePath = filePath.substring( filePath.lastIndexOf("/") + 1 );
 		
 		int i = filePath.indexOf('?');
 		int paramsHash = 0;
 		if (i > 0) {
 			paramsHash = filePath.substring(i).hashCode();
 			filePath = filePath.substring(0, i);
 		}
 		
 		filePath = targetDir + System.getProperty("file.separator") + filePath;
 	
 		File file = new File(filePath);
 		try {
 			
 			boolean newFileCreated = file.createNewFile();
 			
 			if (!newFileCreated && paramsHash != 0) {
 				int j = filePath.lastIndexOf('.');
 				filePath = filePath.substring(0, j) + "-" + String.valueOf(paramsHash) + filePath.substring(j);
 				file = new File(filePath);
 				newFileCreated = file.createNewFile();
 			}
 			
 			if (!newFileCreated && !this.overwriteExisting) {
 				throw new ItemHandlerException("File " + file + " already exists, skipping download of " + url);
 			}
 		}
 		catch (IOException ex) {
 			throw new ItemHandlerException("Could not create file", ex);
 		}
 		
 		return file;
 	}
 	
 	protected void download(URL url, File file, MessageDispatcher dispatcher) throws IOException {
 		long time = System.currentTimeMillis();		
 		long bytes = 0;
 		
 		InputStream in = url.openStream();
 		OutputStream out = new FileOutputStream(file);
 		
 		byte[] buffer = new byte[1024 * 16];  // 16 KB buffer
 		int read = in.read(buffer);
 		while (read > 0) {
 			bytes += read;
 			out.write(buffer, 0, read);
 			read = in.read(buffer);
 		}
 		
 		in.close();
 		out.flush();
 		out.close();
 		
 		time = System.currentTimeMillis() - time;
 		dispatcher.info(url + " -> " + file + " (" + formatBytes(bytes) + " in " + formatTime(time) + ")");
 	}
 	
 	private String formatBytes(long bytes) {
 		double b = Long.valueOf(bytes).doubleValue();
 		String d = "bytes";
 		
 		if (b > 1024d) {
 			b = b / 1024d;
 			d = "KB";
 		}
 		
 		if (b > 1024d) {
 			b = b / 1024d;
 			d = "MB";
 		}
 		
 		DecimalFormat format = new DecimalFormat("#,###.##");
 		
 		return format.format(b) + " " + d;
 	}
 	
 	private String formatTime(long millis) {
 		double t = Long.valueOf(millis).doubleValue();
 		String d = "ms";
 		
 		if (t > 1000d) {
 			t = t / 1000d;
 			d = "sec";
 		}
 		
 		if (t > 60d) {
 			t = t / 60d;
 			d = "min";
 		}
 		
 		if (t > 60d) {
 			t = t / 60d;
 			d = "hr";
 		}
 		
 		DecimalFormat format = new DecimalFormat("#,###.##");
 		
 		return format.format(t) + " " + d;
 	}
 }
