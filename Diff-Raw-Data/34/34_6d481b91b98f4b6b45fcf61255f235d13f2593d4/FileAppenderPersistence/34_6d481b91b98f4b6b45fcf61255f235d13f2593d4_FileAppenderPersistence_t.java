 package events.persistence;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import wheel.io.files.Directory;
 import core.events.eventslist.EventTransaction;
 import events.PersistenceManager;
 
 public class FileAppenderPersistence implements PersistenceManager {
 
 	private final Directory directory;
	private static final String fileName = "timer.dat";
 	private List<EventTransaction> eventsFormFile = null;
 
 	public FileAppenderPersistence(final Directory directory) {
 		this.directory = directory;
 	}
 
 	@Override
 	public List<EventTransaction> getEventTransactions() {
 
 		if (eventsFormFile == null)
 			eventsFormFile = getEventsFormFile();
 		
 		return eventsFormFile;
 			
 	}
 
 	public List<EventTransaction> getEventsFormFile() {
 		boolean dataFileStillDoesNotExist = !directory.fileExists(fileName);
 		if (dataFileStillDoesNotExist)
 			return new ArrayList<EventTransaction>();
 		
 		InputStream in = openInStreamOrCry();
 
 		try{
 			return readEvents(in);			
 		}  finally {
 			closeOrCry(in);
 		}
 	}
 
 	private List<EventTransaction> readEvents(InputStream in) {
 		List<EventTransaction> list = new ArrayList<EventTransaction>();
 		
 		Collection<EventTransaction> event = null;
 		while ((event = readEventsOrCry(in))!= null)
 			list.addAll(event);
 		return list;
 	}
 
 	@SuppressWarnings("unchecked")
 	private Collection<EventTransaction> readEventsOrCry(InputStream in) {
 		
 		Object readObject = readObjectOrCry(in);
 
 		if (readObject == null)
 			return null;
 		
 		if (readObject instanceof List)
 			return (Collection<EventTransaction>) readObject;
 		
 		return Arrays.asList((EventTransaction) readObject);
 	}
 
 	private Object readObjectOrCry(InputStream in) {
 		Object readObject = null;
 		
 		try {
 			readObject = new ObjectInputStream(in).readObject();
 		}catch (EOFException e) {
 			return null;
 		}catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return readObject;
 	}
 
 	private InputStream openInStreamOrCry() {
 		InputStream in;
 		try {
 			in = directory.openFile(fileName);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		return in;
 	}
 
 	@Override
 	public void writeEvent(EventTransaction event) {
 		
 		OutputStream out = openOutStreamOrCry();	
 		
 		try {			
 			writeObjectOrCry(event, out);	
 		} finally {
 			closeOrCry(out);
 		}
 		
 	}
 
 	private void closeOrCry(java.io.Closeable out) {
 		try {
 			out.close();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		
 	}
 
 	private void writeObjectOrCry(EventTransaction event, OutputStream out) {
 		try {
 			new ObjectOutputStream(out).writeObject(event);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private OutputStream openOutStreamOrCry() {
 		OutputStream out = null; 
 		try {
 			out = directory.openFileForAppend(fileName);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		return out;
 	}
 
 }
