 package ms.client.filesys;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.Observer;
 import java.util.Vector;
 
 /**
  * Updates its subscribers whenever a filesystem change occurred in the
  * specified directory.
  * 
  * @author MS
  */
 public class DirectoryObserver extends Thread {
 
 	private static final int POLLING_INTERVAL = 2000;
 	private File _observedDirectory;
 	private File[] _lastDirectorySnapshot;
 	private Vector<Observer> _observers;
 
 	/**
 	 * @param directory
 	 *            Chose the directory that should be observed.
 	 */
 	public DirectoryObserver(String directory) {
 		_observedDirectory = new File(directory);
 		takeDirectorySnapshot();
 		_observers = new Vector<Observer>();
 	}
 
 	/**
 	 * Put the specified subscriber `o' on the list of objects which will be
 	 * updated in case of a file system change.
 	 * 
 	 * @param subscriber
 	 *            will be notified of further file system changes
 	 */
 	public void subscribe(Observer subscriber) {
 		_observers.add(subscriber);
 	}
 
 	/**
 	 * Remove the specified subscriber from the list of objects which will be
 	 * updated in case of a file system change.
 	 * 
 	 * @param subscriber
 	 *            will be removed from the list of change-interested clients.
 	 */
 	public void unsubscribe(Observer subscriber) {
 		_observers.remove(subscriber);
 	}
 	
	@Override
 	public void run() {
 		while (true) {
 			try {
 				sleep(POLLING_INTERVAL);
 				poll();
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 	public void poll() {
 		if (directoryChanged(_observedDirectory.listFiles()))
 			updateAll();
 
 		takeDirectorySnapshot();
 	}
 
 	private void updateAll() {
 		Iterator<Observer> it = _observers.iterator();
 		while (it.hasNext())
 			it.next().update(null, null);
 	}
 
 	private void takeDirectorySnapshot() {
 		_lastDirectorySnapshot = _observedDirectory.listFiles();
 	}
 
 	private boolean directoryChanged(File[] newFileList) {
 		for (int i = 0; i < _lastDirectorySnapshot.length; i++) {
 			if (!_lastDirectorySnapshot[i].getName().equalsIgnoreCase(newFileList[i].getName()))
 				return true;
 		}
 		return false;
 	}
 }
