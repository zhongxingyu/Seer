 package de.age.logtool;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
import java.util.TimerTask;
 
 import de.age.logtool.util.TrackingLogfileListener;
 
public class StreamingLogfileReader extends TimerTask {
 	private static final int BUFFER_SIZE = 1024;
 	
 	private final StringBuilder savedResult = new StringBuilder();
 	private final char[] buffer;
 	private long position;
 	private long lastModification;
 	private final List<LogfileListener> listeners;
 	private final File file;
 
 	public StreamingLogfileReader(File file) {
 		this.file = file;
 		buffer = new char[BUFFER_SIZE];
 		lastModification = -1;
 		position = 0;
 		listeners = new ArrayList<LogfileListener>();
 	}
 
	@Override
	public void run() {
		long modificationTime = file.lastModified();
		if (file.exists() && modificationTime > lastModification) {
			lastModification = modificationTime;
 			readNewDataFromFile();
 		}
 	}
 
 	private void readNewDataFromFile() {
 		Reader reader = null;
 		try {
 			reader = new FileReader(file);
 			reader.skip(position);
 			int read = reader.read(buffer);
 			while (read >= 0) {
 				position = position + read;
 				savedResult.append(buffer, 0, read);
 				read = reader.read(buffer);
 			}
 			int endOfLine = savedResult.indexOf("\n");
 			while (endOfLine >= 0) {
 				fireEvent(savedResult.substring(0, endOfLine));
 				savedResult.delete(0, endOfLine + 1);
 				endOfLine = savedResult.indexOf("\n");
 			}
 		} catch (IOException e) {
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	private void fireEvent(String substring) {
 		for (LogfileListener listener : listeners) {
 			listener.lineRead(substring);
 		}
 	}
 
 	public void addLogfileListener(TrackingLogfileListener listener) {
 		this.listeners.add(listener);
 	}
 }
