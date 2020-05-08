 /**
  * Copyright 2014 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.log.api.nio;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.nio.file.FileVisitResult;
 import java.nio.file.FileVisitor;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.araqne.log.api.AbstractLogPipe;
 import org.araqne.log.api.AbstractLogger;
 import org.araqne.log.api.LastPosition;
 import org.araqne.log.api.LastPositionHelper;
 import org.araqne.log.api.Log;
 import org.araqne.log.api.Logger;
 import org.araqne.log.api.LoggerFactory;
 import org.araqne.log.api.LoggerSpecification;
 import org.araqne.log.api.LoggerStopReason;
 import org.araqne.log.api.MultilineLogExtractor;
 import org.araqne.log.api.Reconfigurable;
 
 public class NioRecursiveDirectoryWatchLogger extends AbstractLogger implements Reconfigurable {
 	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(NioRecursiveDirectoryWatchLogger.class.getName());
 	private String basePath;
 	private Pattern fileNamePattern;
 	private Pattern dirPathPattern;
 	private boolean recursive;
 	private String fileTag;
 
 	private Receiver receiver = new Receiver();
 
 	/**
 	 * NOTE: must be separate thread for accurate event processing
 	 */
 	private ChangeDetector detector;
 
 	private MultilineLogExtractor extractor;
 
 	private boolean walkTreeRequired = true;
 
 	public NioRecursiveDirectoryWatchLogger(LoggerSpecification spec, LoggerFactory factory) {
 		super(spec, factory);
 
 		if (slog.isDebugEnabled())
 			slog.debug("araqne-logapi-nio: recursive dirwatcher used nio");
 
 		applyConfig();
 	}
 
 	private void applyConfig() {
 		basePath = getConfigs().get("base_path");
 
 		String fileNameRegex = getConfigs().get("filename_pattern");
 		fileNamePattern = Pattern.compile(fileNameRegex);
 
 		// optional
 		String dirNameRegex = getConfigs().get("dirpath_pattern");
 		if (dirNameRegex != null)
 			dirPathPattern = Pattern.compile(dirNameRegex);
 
 		extractor = new MultilineLogExtractor(this, receiver);
 
 		// optional
 		String dateExtractRegex = getConfigs().get("date_pattern");
 		if (dateExtractRegex != null)
 			extractor.setDateMatcher(Pattern.compile(dateExtractRegex).matcher(""));
 
 		// optional
 		String dateLocale = getConfigs().get("date_locale");
 		if (dateLocale == null)
 			dateLocale = "en";
 
 		// optional
 		String dateFormatString = getConfigs().get("date_format");
 		String timeZone = getConfigs().get("timezone");
 		if (dateFormatString != null)
 			extractor.setDateFormat(new SimpleDateFormat(dateFormatString, new Locale(dateLocale)), timeZone);
 
 		// optional
 		String newlogRegex = getConfigs().get("newlog_designator");
 		if (newlogRegex != null)
 			extractor.setBeginMatcher(Pattern.compile(newlogRegex).matcher(""));
 
 		String newlogEndRegex = getConfigs().get("newlog_end_designator");
 		if (newlogEndRegex != null)
 			extractor.setEndMatcher(Pattern.compile(newlogEndRegex).matcher(""));
 
 		// optional
 		String charset = getConfigs().get("charset");
 		if (charset == null)
 			charset = "utf-8";
 
 		// optional
 		String recursive = getConfigs().get("recursive");
 		this.recursive = ((recursive != null) && (recursive.compareToIgnoreCase("true") == 0));
 
 		// optional
 		this.fileTag = getConfigs().get("file_tag");
 
 		extractor.setCharset(charset);
 	}
 
 	@Override
 	protected void onStart() {
 		detector = new ChangeDetector();
 		detector.start();
 	}
 
 	@Override
 	protected void onStop(LoggerStopReason reason) {
 		detector.close();
 		walkTreeRequired = true;
 	}
 
 	@Override
 	protected void runOnce() {
 		if (detector.deadThread)
 			throw new IllegalStateException("dead file watcher, logger[ [" + getFullName() + "]");
 
 		if (walkTreeRequired) {
 			try {
 				Map<String, LastPosition> lastPositions = LastPositionHelper.deserialize(getStates());
 				Path root = FileSystems.getDefault().getPath(basePath);
 				Files.walkFileTree(root, new InitialRunner(root, lastPositions));
 
 				// mark deleted files
 				for (String path : new ArrayList<String>(lastPositions.keySet())) {
 					markDeletedFile(lastPositions, new File(path));
 				}
 
 				setStates(LastPositionHelper.serialize(lastPositions));
 				walkTreeRequired = false;
 			} catch (IOException e) {
 				throw new IllegalStateException("failed to initial run, logger [" + getFullName() + "]", e);
 			}
 		}
 
 		Map<String, LastPosition> lastPositions = LastPositionHelper.deserialize(getStates());
 
 		try {
 			List<File> changedFiles = new ArrayList<File>(detector.getChangedFiles());
 			Collections.sort(changedFiles);
 			for (File f : changedFiles) {
 				processFile(lastPositions, f);
 			}
 
 			List<File> deletedFiles = new ArrayList<File>(detector.getDeletedFiles());
 			Collections.sort(deletedFiles);
 			for (File f : deletedFiles) {
 				markDeletedFile(lastPositions, f);
 			}
 		} finally {
 			setStates(LastPositionHelper.serialize(lastPositions));
 		}
 	}
 
 	private void markDeletedFile(Map<String, LastPosition> lastPositions, File f) {
 		if (f.exists())
 			return;
 
 		String path = f.getAbsolutePath();
 		LastPosition lp = lastPositions.get(path);
 		if (lp == null)
 			return;
 		if (lp.getLastSeen() == null) {
 			lp.setLastSeen(new Date());
 			slog.debug("araqne-logapi-nio: logger [{}] marked deleted file [{}] state", getFullName(), f.getAbsolutePath());
 		} else {
 			long limitTime = lp.getLastSeen().getTime() + 3600000L;
 			if (limitTime <= System.currentTimeMillis()) {
 				lastPositions.remove(path);
 				slog.debug("araqne-logapi-nio: logger [{}] removed deleted file [{}] from states", getFullName(),
 						f.getAbsolutePath());
 			}
 		}
 	}
 
 	protected void processFile(Map<String, LastPosition> lastPositions, File file) {
 		String path = file.getAbsolutePath();
 		FileInputStream is = null;
 		try {
 			String dateFromFileName = getDateString(file);
 
 			// skip previous read part
 			long offset = 0;
 			if (lastPositions.containsKey(path)) {
 				LastPosition inform = lastPositions.get(path);
 				offset = inform.getPosition();
 				slog.trace("araqne-logapi-nio: target file [{}] skip offset [{}]", path, offset);
 			}
 
 			AtomicLong lastPosition = new AtomicLong(offset);
 			if (file.length() <= offset)
 				return;
 
 			receiver.filename = file.getName();
 			is = new FileInputStream(file);
 			is.skip(offset);
 
 			extractor.extract(is, lastPosition, dateFromFileName);
 
 			slog.debug("araqne-logapi-nio: updating file [{}] old position [{}] new last position [{}]", new Object[] { path,
 					offset, lastPosition.get() });
 			LastPosition inform = lastPositions.get(path);
 			if (inform == null) {
 				inform = new LastPosition(path);
 			}
 			inform.setPosition(lastPosition.get());
 			lastPositions.put(path, inform);
 		} catch (FileNotFoundException e) {
 			if (slog.isTraceEnabled())
 				slog.trace("araqne-logapi-nio: logger [" + getFullName() + "] read failure: file not found: {}", e.getMessage());
 		} catch (Throwable e) {
 			slog.error("araqne-logapi-nio: logger [" + getFullName() + "] read error", e);
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	private String getDateString(File f) {
 		StringBuffer sb = new StringBuffer(f.getAbsolutePath().length());
 		String dirPath = f.getParentFile().getAbsolutePath();
 		if (dirPathPattern != null) {
 			Matcher dirNameDateMatcher = dirPathPattern.matcher(dirPath);
 			while (dirNameDateMatcher.find()) {
 				int dirNameGroupCount = dirNameDateMatcher.groupCount();
 				if (dirNameGroupCount > 0) {
 					for (int i = 1; i <= dirNameGroupCount; ++i) {
 						sb.append(dirNameDateMatcher.group(i));
 					}
 				}
 			}
 		}
 
 		String fileName = f.getName();
 		Matcher fileNameDateMatcher = fileNamePattern.matcher(fileName);
 		while (fileNameDateMatcher.find()) {
 			int fileNameGroupCount = fileNameDateMatcher.groupCount();
 			if (fileNameGroupCount > 0) {
 				for (int i = 1; i <= fileNameGroupCount; ++i) {
 					sb.append(fileNameDateMatcher.group(i));
 				}
 			}
 		}
 		String date = sb.toString();
 		return date.isEmpty() ? null : date;
 	}
 
 	private class Receiver extends AbstractLogPipe {
 		private String filename;
 
 		@Override
 		public void onLog(Logger logger, Log log) {
 			if (fileTag != null)
 				log.getParams().put(fileTag, filename);
 			write(log);
 		}
 
 		@Override
 		public void onLogBatch(Logger logger, Log[] logs) {
 			if (fileTag != null) {
 				for (Log log : logs) {
 					log.getParams().put(fileTag, filename);
 				}
 			}
 			writeBatch(logs);
 		}
 	}
 
 	private class InitialRunner implements FileVisitor<Path> {
 		private Path root;
 		private Map<String, LastPosition> lastPositions;
 
 		public InitialRunner(Path root, Map<String, LastPosition> lastPositions) {
 			this.root = root;
 			this.lastPositions = lastPositions;
 		}
 
 		@Override
 		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
 			if (!dir.equals(root)) {
 				if (!recursive
 						|| (dirPathPattern != null && !dir.equals(root) && !dirPathPattern
 								.matcher(dir.toFile().getAbsolutePath()).find()))
 					return FileVisitResult.SKIP_SUBTREE;
 			}
 
 			return FileVisitResult.CONTINUE;
 		}
 
 		@Override
 		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
 			File f = file.toFile();
			if (dirPathPattern.matcher(f.getParentFile().getAbsolutePath()).find()
					&& fileNamePattern.matcher(f.getName()).matches())
 				processFile(lastPositions, f);
 			return FileVisitResult.CONTINUE;
 		}
 
 		@Override
 		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
 			return FileVisitResult.CONTINUE;
 		}
 
 		@Override
 		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
 			return FileVisitResult.CONTINUE;
 		}
 	}
 
 	private class ChangeDetector extends Thread implements FileEventListener {
 		private FileEventWatcher evtWatcher;
 		private Set<File> changedFiles = new HashSet<File>();
 		private Set<File> deletedFiles = new HashSet<File>();
 		private volatile boolean doStop;
 		private volatile boolean deadThread;
 		private Object changedLock = new Object();
 		private Object deletedLock = new Object();
 
 		public ChangeDetector() {
 			super("File Watcher [" + getFullName() + "]");
 		}
 
 		public Set<File> getChangedFiles() {
 			synchronized (changedLock) {
 				Set<File> copied = new HashSet<File>(changedFiles);
 				changedFiles.clear();
 				return copied;
 			}
 		}
 
 		public Set<File> getDeletedFiles() {
 			synchronized (deletedLock) {
 				Set<File> copied = new HashSet<File>(deletedFiles);
 				deletedFiles.clear();
 				return copied;
 			}
 		}
 
 		@Override
 		public void run() {
 			try {
 				slog.info("araqne-logapi-nio: starting file watcher for logger [{}]", getFullName());
 				this.evtWatcher = new FileEventWatcher(basePath, fileNamePattern, recursive);
 				evtWatcher.addListener(detector);
 
 				while (!doStop) {
 					evtWatcher.poll(100);
 				}
 			} catch (IOException e) {
 				slog.error("araqne-logapi-nio: cannot poll file event for logger [" + getFullName() + "]", e);
 			} finally {
 				evtWatcher.removeListener(this);
 				evtWatcher.close();
 				slog.info("araqne-logapi-nio: stopping file watcher for logger [{}]", getFullName());
 				deadThread = true;
 			}
 		}
 
 		public void close() {
 			doStop = true;
 			slog.debug("araqne-logapi-nio: closing change detector of logger [{}]", getFullName());
 		}
 
 		@Override
 		public void onCreate(File file) {
 			if (file.isFile() && dirPathPattern != null && !dirPathPattern.matcher(file.getParentFile().getAbsolutePath()).find())
 				return;
 
 			synchronized (changedLock) {
 				changedFiles.add(file);
 			}
 
 			if (slog.isDebugEnabled())
 				slog.debug("araqne-logapi-nio: logger [{}] detect created file [{}]", getFullName(), file.getAbsolutePath());
 		}
 
 		@Override
 		public void onDelete(File file) {
 			synchronized (changedLock) {
 				changedFiles.remove(file);
 			}
 
 			synchronized (deletedLock) {
 				deletedFiles.add(file);
 			}
 
 			if (slog.isDebugEnabled())
 				slog.debug("araqne-logapi-nio: logger [{}] detect deleted file [{}]", getFullName(), file.getAbsolutePath());
 		}
 
 		@Override
 		public void onModify(File file) {
 			if (file.isFile() && dirPathPattern != null && !dirPathPattern.matcher(file.getParentFile().getAbsolutePath()).find())
 				return;
 
 			synchronized (changedLock) {
 				changedFiles.add(file);
 			}
 
 			if (slog.isDebugEnabled())
 				slog.debug("araqne-logapi-nio: logger [{}] detect modified file [{}]", getFullName(), file.getAbsolutePath());
 		}
 	}
 
 	@Override
 	public void onConfigChange(Map<String, String> oldConfigs, Map<String, String> newConfigs) {
 		if (isRunning())
 			throw new IllegalStateException("logger is running");
 
 		applyConfig();
 	}
 }
