 package jp.ac.osaka_u.ist.sdl.ectec.analyzer.filedetector;
 
 import java.util.SortedSet;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import jp.ac.osaka_u.ist.sdl.ectec.data.FileInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.settings.MessagePrinter;
 
 /**
  * A thread class to create instances of FileInfo
  * 
  * @author k-hotta
  * 
  */
 public class FileInfoInstancesCreatingThread implements Runnable {
 
 	/**
 	 * the file path and lds of revisions when it was changed
 	 */
 	private final ConcurrentMap<String, SortedSet<ChangeOnFile>> changedFiles;
 
 	/**
 	 * the key is the id of a revision and the value is the id of the previous
 	 * revision of the key revision
 	 */
 	private final ConcurrentMap<Long, Long> revisionsMap;
 
 	/**
 	 * the instances of FileInfo
 	 */
 	private final ConcurrentMap<Long, FileInfo> files;
 
 	/**
 	 * the file paths to be analyzed
 	 */
 	private final String[] targetPaths;
 
 	/**
 	 * the index that points current processing state
 	 */
 	private final AtomicInteger index;
 
 	/**
 	 * the id of the latest revision
 	 */
 	private final long lastRevisionId;
 
 	public FileInfoInstancesCreatingThread(
 			final ConcurrentMap<String, SortedSet<ChangeOnFile>> changedFiles,
 			final ConcurrentMap<Long, Long> revisionsMap,
 			final ConcurrentMap<Long, FileInfo> files,
 			final String[] targetPaths, final AtomicInteger index,
 			final long lastRevisionId) {
 		this.changedFiles = changedFiles;
 		this.revisionsMap = revisionsMap;
 		this.files = files;
 		this.targetPaths = targetPaths;
 		this.index = index;
 		this.lastRevisionId = lastRevisionId;
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			final int currentIndex = index.getAndIncrement();
 
 			if (currentIndex >= targetPaths.length) {
 				break;
 			}
 
 			final String path = targetPaths[currentIndex];
 
 			MessagePrinter.println("\tanalyzing " + path + " ["
 					+ (currentIndex + 1) + "/" + targetPaths.length + "]");
 
 			// get the ids of revisions where this file was updated
 			final SortedSet<ChangeOnFile> changes = changedFiles.get(path);
 			if (changes == null) {
 				MessagePrinter.ePrintln("something is wrong when analyzing "
 						+ path);
 				continue;
 			}
 
 			// create new instances of FileInfo
 			ChangeOnFile currentChange = null;
 
 			for (final ChangeOnFile nextChange : changes) {
 				if (currentChange == null) {
 					currentChange = nextChange;
 					continue;
 				}
 
 				final long revision = nextChange.getChagnedRevisionId();
 				final long previousRevision = revisionsMap.get(revision);
 
 				switch (currentChange.getChangeType()) {
 				case ADD:
 					final FileInfo addedFile = new FileInfo(path,
							currentChange.getChagnedRevisionId(), revision - 1);
 					files.put(addedFile.getId(), addedFile);
 					break;
 
 				case CHANGE:
 					final FileInfo changedFile = new FileInfo(path,
							currentChange.getChagnedRevisionId(), revision - 1);
 					files.put(changedFile.getId(), changedFile);
 					break;
 
 				case DELETE:
 					break;
 				}
 
 				currentChange = nextChange;
 
 			}
 
 			// treat the last change
 			switch (currentChange.getChangeType()) {
 			case ADD:
 				final FileInfo addedFile = new FileInfo(path,
 						currentChange.getChagnedRevisionId(), lastRevisionId);
 				files.put(addedFile.getId(), addedFile);
 				break;
 
 			case CHANGE:
 				final FileInfo changedFile = new FileInfo(path,
 						currentChange.getChagnedRevisionId(), lastRevisionId);
 				files.put(changedFile.getId(), changedFile);
 				break;
 
 			case DELETE:
 				break;
 			}
 
 		}
 	}
 }
