 package de.age.logtool;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
import de.age.logtool.exceptions.IllegalLogfileListenerException;
 import de.age.logtool.util.TrackingLogfileListener;
 
public class StreamingLogfileReader {
 	private static final int BUFFER_SIZE = 1024;
 	
 	private final StringBuilder savedResult = new StringBuilder();
 	private final char[] buffer;
 	private long position;
 	private long lastModification;
	private long lastSize;
 	private final List<LogfileListener> listeners;
 	private final File file;
 
 	public StreamingLogfileReader(File file) {
		if (file == null) {
			throw new NullPointerException("File must not be <null>");
		}
 		this.file = file;
 		buffer = new char[BUFFER_SIZE];
 		lastModification = -1;
		lastSize = 0;
 		position = 0;
 		listeners = new ArrayList<LogfileListener>();
 	}
 
	public void checkFileForUpdates() {
		if (file.exists() && fileWasModified()) {
			lastModification = file.lastModified();
			lastSize = file.length();
 			readNewDataFromFile();
 		}
 	}
 
	private boolean fileWasModified() {
		long newModificationTime = file.lastModified();
		long newSize = file.length();
		return newModificationTime > lastModification || newSize > lastSize;
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
		if (listener == null) {
			throw IllegalLogfileListenerException.nullListener();
		}
 		this.listeners.add(listener);
 	}
 }
