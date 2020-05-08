 package org.araqne.log.api;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class DailyRollingDirectoryWatchLogger extends AbstractLogger implements Reconfigurable {
 	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(DailyRollingDirectoryWatchLogger.class);
 	private boolean firstRun;
 
 	public DailyRollingDirectoryWatchLogger(LoggerSpecification spec, LoggerFactory factory) {
 		super(spec, factory);
 	}
 
 	@Override
 	protected void onStart(LoggerStartReason reason) {
 		firstRun = true;
 	}
 
 	@Override
 	public void onConfigChange(Map<String, String> oldConfigs, Map<String, String> newConfigs) {
 		firstRun = true;
 	}
 
 	@Override
 	protected void runOnce() {
 		Map<String, LastPosition> lastPositions = null;
 		try {
 			List<File> files = scanFiles();
 			List<File> oldFiles = null;
 			if (firstRun)
 				oldFiles = scanOldFiles();
 
 			lastPositions = LastPositionHelper.deserialize(getStates());
 			removeOldStates(files, oldFiles, lastPositions);
 
 			// load and build patterns
 			Matcher dirNameDateMatcher = null;
 			String s = getConfigs().get("dir_date_pattern");
 			if (s != null) {
 				dirNameDateMatcher = Pattern.compile(s).matcher("");
 			}
 
 			Matcher fileNameDateMatcher = Pattern.compile(getConfigs().get("filename_pattern")).matcher("");
 
 			// traverse files
 			String fileTag = getConfigs().get("file_tag");
 
 			if (oldFiles != null) {
 				for (File f : oldFiles) {
 					if (getStatus() == LoggerStatus.Stopping || getStatus() == LoggerStatus.Stopped)
 						break;
 
 					MultilineLogExtractor extractor = newExtractor(fileTag, f.getName());
 					processFile(lastPositions, f, extractor, dirNameDateMatcher, fileNameDateMatcher);
 				}
 			}
 
 			for (File f : files) {
 				if (getStatus() == LoggerStatus.Stopping || getStatus() == LoggerStatus.Stopped)
 					break;
 
 				MultilineLogExtractor extractor = newExtractor(fileTag, f.getName());
 				processFile(lastPositions, f, extractor, dirNameDateMatcher, fileNameDateMatcher);
 			}
 
 		} finally {
 			if (lastPositions != null)
 				setStates(LastPositionHelper.serialize(lastPositions));
 
 			firstRun = false;
 		}
 	}
 
 	private MultilineLogExtractor newExtractor(String fileTag, String fileName) {
 		MultilineLogExtractor extractor = new MultilineLogExtractor(this, new Receiver(fileTag, fileName));
 
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
 
 		extractor.setCharset(charset);
 		return extractor;
 	}
 
 	protected void processFile(Map<String, LastPosition> lastPositions, File f, MultilineLogExtractor extractor,
 			Matcher dirNameDateMatcher, Matcher fileNameDateMatcher) {
 		if (!f.canRead()) {
 			slog.debug("araqne log api: cannot read file [{}], logger [{}]", f.getAbsolutePath(), getFullName());
 			return;
 		}
 
 		FileInputStream is = null;
 		String path = f.getAbsolutePath();
 		try {
 			// get date pattern-matched string from filename
 			String timestampPrefix = getTimestampPrefix(f, dirNameDateMatcher, fileNameDateMatcher);
 
 			// skip previous read part
 			long offset = 0;
 			if (lastPositions.containsKey(path)) {
 				LastPosition inform = lastPositions.get(path);
 				offset = inform.getPosition();
 				slog.trace("araqne log api: target file [{}] skip offset [{}]", path, offset);
 			}
 
 			AtomicLong lastPosition = new AtomicLong(offset);
 			File file = new File(path);
 			if (file.length() <= offset)
 				return;
 
 			is = new FileInputStream(file);
 			is.skip(offset);
 
 			extractor.extract(is, lastPosition, timestampPrefix);
 
 			slog.debug("araqne log api: updating file [{}] old position [{}] new last position [{}]", new Object[] { path,
 					offset, lastPosition.get() });
 			LastPosition inform = lastPositions.get(path);
 			if (inform == null) {
 				inform = new LastPosition(path);
 			}
 			inform.setPosition(lastPosition.get());
 			lastPositions.put(path, inform);
 		} catch (FileNotFoundException e) {
 			if (slog.isTraceEnabled())
 				slog.trace("araqne log api: [" + getName() + "] logger read failure: file not found: {}", e.getMessage());
 		} catch (Throwable e) {
 			slog.error("araqne log api: [" + getName() + "] logger read error", e);
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	private String getTimestampPrefix(File f, Matcher dirNameDateMatcher, Matcher fileNameDateMatcher) {
 		File dir = f.getParentFile();
 
 		StringBuilder sb = new StringBuilder();
 
 		if (dirNameDateMatcher != null) {
 			dirNameDateMatcher.reset(dir.getName());
 			while (dirNameDateMatcher.find()) {
 				int dirNameGroupCount = dirNameDateMatcher.groupCount();
 				for (int i = 1; i <= dirNameGroupCount; ++i)
 					sb.append(dirNameDateMatcher.group(i));
 			}
 		}
 
 		fileNameDateMatcher.reset(f.getName());
 		while (fileNameDateMatcher.find()) {
 			int fileNameGroupCount = fileNameDateMatcher.groupCount();
 			for (int i = 1; i <= fileNameGroupCount; ++i)
 				sb.append(fileNameDateMatcher.group(i));
 		}
 
 		if (sb.length() > 0)
 			return sb.toString();
 		return null;
 	}
 
 	private List<File> scanFiles() {
 		int period = Integer.valueOf(getConfigs().get("period"));
 		if (period <= 0)
 			return new ArrayList<File>(0);
 
 		Calendar c = Calendar.getInstance();
 		c.add(Calendar.DAY_OF_MONTH, -(period - 1));
 		c.set(Calendar.HOUR_OF_DAY, 0);
 		c.set(Calendar.MINUTE, 0);
 		c.set(Calendar.SECOND, 0);
 		c.set(Calendar.MILLISECOND, 0);
 		Date begin = c.getTime();
 		return scanFiles(begin, new Date());
 	}
 
 	private List<File> scanOldFiles() {
 		// load begin and end dates
 		Date begin = null;
 		Date end = null;
 
 		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
 		String s = getConfigs().get("old_dir_scan_from");
 		if (s != null)
 			begin = df.parse(s, new ParsePosition(0));
 
 		s = getConfigs().get("old_dir_scan_to");
 		if (s != null)
 			end = df.parse(s, new ParsePosition(0));
 
 		if (begin == null || end == null)
 			return new ArrayList<File>(0);
 
 		return scanFiles(begin, end);
 	}
 
 	private List<File> scanFiles(Date begin, Date end) {
 		if (begin == null)
 			throw new IllegalArgumentException("begin should not be null");
 
 		if (end == null)
 			throw new IllegalArgumentException("end should not be null");
 
 		List<File> l = new ArrayList<File>();
 
 		File basePath = new File(getConfigs().get("base_path"));
 		if (slog.isDebugEnabled()) {
			slog.debug("araqne log api: scan files [{}], period [{} ~ {}]", basePath.getAbsolutePath());
 		}
 
 		File[] dirs = basePath.listFiles();
 
 		if (dirs == null)
 			return new ArrayList<File>();
 
 		DirDateParser dirDateParser = new DirDateParser(getConfigs());
 
 		Matcher fileNameMatcher = Pattern.compile(getConfigs().get("filename_pattern")).matcher("");
 
 		for (File dir : dirs) {
 			if (!dir.isDirectory())
 				continue;
 
 			String dirName = dir.getName();
 			Date date = dirDateParser.parse(dirName);
 			if (date == null) {
 				if (slog.isDebugEnabled())
 					slog.debug("araqne log api: logger [{}] cannot parse date from directory name [{}]", getFullName(), dirName);
 
 				continue;
 			}
 
 			if (date.before(begin) || date.after(end)) {
 				if (slog.isDebugEnabled())
 					slog.debug("araqne log api: logger [{}] skip [{}] directory (out of range)", getFullName(), dirName);
 
 				continue;
 			}
 
 			File[] files = dir.listFiles();
 			if (files == null) {
 				if (slog.isDebugEnabled())
 					slog.debug("araqne log api: logger [{}] skip [{}] directory (no files)", getFullName(), dirName);
 
 				continue;
 			}
 
 			for (File f : files) {
 				fileNameMatcher.reset(f.getName());
 				if (fileNameMatcher.matches())
 					l.add(f);
 			}
 		}
 
 		return l;
 	}
 
 	private void removeOldStates(List<File> files, List<File> oldFiles, Map<String, LastPosition> lastPositions) {
 		List<String> deleteKeys = new ArrayList<String>();
 		HashSet<String> targetFileSet = new HashSet<String>();
 
 		for (File f : files)
 			targetFileSet.add(f.getAbsolutePath());
 
 		if (oldFiles != null) {
 			for (File f : oldFiles)
 				targetFileSet.add(f.getAbsolutePath());
 		}
 
 		for (String key : lastPositions.keySet()) {
 			if (!targetFileSet.contains(key))
 				deleteKeys.add(key);
 		}
 
 		for (String key : deleteKeys)
 			lastPositions.remove(key);
 	}
 
 	private static class DirDateParser {
 		private Pattern dirDatePattern;
 		private SimpleDateFormat dirDateFormat;
 		private Matcher dirDateMatcher;
 
 		public DirDateParser(Map<String, String> configs) {
 			dirDateFormat = new SimpleDateFormat("yyyyMMdd");
 
 			String s = configs.get("dir_date_pattern");
 			if (s != null) {
 				dirDatePattern = Pattern.compile(s);
 				dirDateMatcher = dirDatePattern.matcher("");
 			}
 
 			s = configs.get("dir_date_format");
 			if (s != null) {
 				try {
 					dirDateFormat = new SimpleDateFormat(s);
 				} catch (Throwable t) {
 					// ignore invalid date format
 				}
 			}
 		}
 
 		public Date parse(String s) {
 			if (dirDateMatcher != null) {
 				StringBuilder sb = new StringBuilder(s.length());
 				dirDateMatcher.reset(s);
 				while (dirDateMatcher.find()) {
 					int count = dirDateMatcher.groupCount();
 					if (count > 0) {
 						for (int i = 1; i <= count; ++i)
 							sb.append(dirDateMatcher.group(i));
 					}
 				}
 				s = sb.toString();
 			}
 
 			return dirDateFormat.parse(s, new ParsePosition(0));
 		}
 	}
 
 	private class Receiver extends AbstractLogPipe {
 		private String fileTag;
 		private String fileName;
 
 		public Receiver(String fileTag, String fileName) {
 			this.fileTag = fileTag;
 			this.fileName = fileName;
 		}
 
 		@Override
 		public void onLog(Logger logger, Log log) {
 			if (fileTag != null)
 				log.getParams().put(fileTag, fileName);
 			write(log);
 		}
 
 		@Override
 		public void onLogBatch(Logger logger, Log[] logs) {
 			if (fileTag != null) {
 				for (Log log : logs) {
 					log.getParams().put(fileTag, fileName);
 				}
 			}
 			writeBatch(logs);
 		}
 	}
 }
