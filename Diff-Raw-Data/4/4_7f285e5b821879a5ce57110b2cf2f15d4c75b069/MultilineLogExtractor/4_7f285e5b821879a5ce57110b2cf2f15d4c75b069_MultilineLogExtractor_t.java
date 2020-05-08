 /*
  * Copyright 2013 Eediom Inc.
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
 package org.araqne.log.api;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.regex.Matcher;
 
 /**
  * @since 2.4.6
  * @author xeraph
  * 
  */
 public class MultilineLogExtractor {
 	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(MultilineLogExtractor.class);
 	private Logger logger;
 	private String charset = "utf-8";
 	private Matcher beginMatcher;
 	private Matcher endMatcher;
 	private Matcher dateMatcher;
 	private SimpleDateFormat dateFormat;
 	private LogPipe pipe;
 
 	// XXX
 	private boolean isUTF16LE = false;
 
 	// assign current year to date
 	private Calendar yearModifier;
 
 	public MultilineLogExtractor(Logger logger, LogPipe pipe) {
 		this.logger = logger;
 		this.pipe = pipe;
 	}
 
 	public String getCharset() {
 		return charset;
 	}
 
 	public void setCharset(String charset) {
 		this.charset = charset;
 		isUTF16LE = charset.equalsIgnoreCase("UTF-16LE");
 	}
 
 	public Matcher getBeginMatcher() {
 		return beginMatcher;
 	}
 
 	public void setBeginMatcher(Matcher beginMatcher) {
 		this.beginMatcher = beginMatcher;
 	}
 
 	public Matcher getEndMatcher() {
 		return endMatcher;
 	}
 
 	public void setEndMatcher(Matcher endMatcher) {
 		this.endMatcher = endMatcher;
 	}
 
 	public Matcher getDateMatcher() {
 		return dateMatcher;
 	}
 
 	public void setDateMatcher(Matcher dateMatcher) {
 		this.dateMatcher = dateMatcher;
 	}
 
 	public SimpleDateFormat getDateFormat() {
 		return dateFormat;
 	}
 
 	public void setDateFormat(SimpleDateFormat dateFormat) {
 		setDateFormat(dateFormat, null);
 	}
 
 	public void setDateFormat(SimpleDateFormat dateFormat, String timeZone) {
 		this.dateFormat = dateFormat;
 		if (timeZone != null) {
 			if (TimeZoneMappings.getTimeZone(timeZone) != null)
 				timeZone = (String) TimeZoneMappings.getTimeZone(timeZone);
 
 			dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
 		}
 
 		if (dateFormat != null && !dateFormat.toPattern().contains("yyyy")) {
 			yearModifier = Calendar.getInstance();
 			if (timeZone != null)
 				yearModifier.setTimeZone(TimeZone.getTimeZone(timeZone));
 		}
 	}
 
 	public void extract(InputStream is, AtomicLong lastPosition) throws IOException {
 		extract(is, lastPosition, null);
 	}
 
 	public void extract(InputStream is, AtomicLong lastPosition, String dateFromFileName) throws IOException {
 		ByteArrayOutputStream logBuf = new ByteArrayOutputStream();
 
 		// last chunk of page which does not contains new line
 		ByteArrayOutputStream temp = new ByteArrayOutputStream();
 		byte[] b = new byte[512 * 1024];
 
 		while (true) {
 			if (logger != null) {
 				LoggerStatus status = logger.getStatus();
 				if (status == LoggerStatus.Stopping || status == LoggerStatus.Stopped)
 					break;
 			}
 
 			int next = 0;
 			int len = is.read(b);
 			if (len < 0)
 				break;
 
 			ArrayList<Log> output = new ArrayList<Log>(4000);
 
 			for (int i = 0; i < len; i++) {
 				if (b[i] == 0xa) {
 					if (isUTF16LE)
 						i += 1;
 					buildLogOutput(logBuf, b, next, i - next + 1, lastPosition, temp, dateFromFileName, output);
 					next = i + 1;
 				}
 			}
 
 			if (output.size() > 0)
 				pipe.onLogBatch(logger, output.toArray(new Log[0]));
 
 			// temp should be matched later (line regex test)
 			temp.write(b, next, len - next);
 		}
 	}
 
 	private void buildLogOutput(ByteArrayOutputStream logBuf, byte[] b, int offset, int length, AtomicLong lastPosition,
 			ByteArrayOutputStream temp, String dateFromFileName, List<Log> output) {
 		String log = null;
 		try {
 			log = buildLog(logBuf, b, offset, length, lastPosition, temp);
 		} catch (UnsupportedEncodingException e) {
 		}
 
 		if (log != null) {
 			int l = log.length();
			boolean cr = false;
			if (l > 2)
				cr = log.charAt(l - 2) == '\r';
 			boolean lf = log.charAt(l - 1) == '\n';
 
 			if (cr && lf)
 				log = log.substring(0, l - 2);
 			else if (lf)
 				log = log.substring(0, l - 1);
 
 			if (log.length() > 0) {
 				Date d = parseDate(log, dateFromFileName);
 
 				Map<String, Object> m = new HashMap<String, Object>();
 				m.put("line", log);
 
 				output.add(new SimpleLog(d, logger == null ? null : logger.getFullName(), m));
 			}
 		}
 	}
 
 	/**
 	 * @param buf
 	 *            the buffer which hold partial multiline log
 	 * @param b
 	 *            read block which contains new line
 	 * @param offset
 	 *            the new line offset
 	 * @param len
 	 *            the new line length
 	 * @param lastPosition
 	 *            the last position which read and written as log
 	 * @return new (multiline) log
 	 * @throws UnsupportedEncodingException
 	 */
 	private String buildLog(ByteArrayOutputStream buf, byte[] b, int offset, int len, AtomicLong lastPosition,
 			ByteArrayOutputStream temp) throws UnsupportedEncodingException {
 
 		String line = null;
 		if (temp.size() > 0) {
 			temp.write(b, offset, len);
 			line = new String(temp.toByteArray(), charset);
 		} else {
 			line = new String(b, offset, len, charset);
 		}
 
 		if (!line.endsWith("\n")) {
 			if (temp.size() == 0)
 				temp.write(b, offset, len);
 
 			return null;
 		}
 
 		if (beginMatcher != null)
 			beginMatcher.reset(line);
 
 		if (endMatcher != null)
 			endMatcher.reset(line);
 
 		if (beginMatcher == null && endMatcher == null) {
 			if (temp.size() > 0) {
 				byte[] t = temp.toByteArray();
 				buf.write(t, 0, t.length);
 				temp.reset();
 			} else {
 				buf.write(b, offset, len);
 			}
 
 			byte[] old = buf.toByteArray();
 			buf.reset();
 			lastPosition.addAndGet(old.length);
 			return new String(old, charset);
 		}
 
 		if (beginMatcher != null && beginMatcher.find()) {
 			byte[] old = buf.toByteArray();
 			String log = null;
 
 			if (old.length > 0) {
 				log = new String(old, charset);
 				lastPosition.addAndGet(old.length);
 				buf.reset();
 			}
 
 			if (temp.size() > 0) {
 				byte[] t = temp.toByteArray();
 				buf.write(t, 0, t.length);
 				temp.reset();
 			} else {
 				buf.write(b, offset, len);
 			}
 			return log;
 		} else if (endMatcher != null && endMatcher.find()) {
 			if (temp.size() > 0) {
 				byte[] t = temp.toByteArray();
 				buf.write(t, 0, t.length);
 				temp.reset();
 			} else {
 				buf.write(b, offset, len);
 			}
 			byte[] old = buf.toByteArray();
 			lastPosition.addAndGet(old.length);
 			String log = new String(old, charset);
 			buf.reset();
 			return log;
 		} else {
 			if (temp.size() > 0) {
 				byte[] t = temp.toByteArray();
 				buf.write(t, 0, t.length);
 				temp.reset();
 			} else {
 				buf.write(b, offset, len);
 			}
 		}
 
 		return null;
 	}
 
 	protected Date parseDate(String line, String dateFromFileName) {
 		if (dateFormat == null)
 			return new Date();
 
 		String s = dateFromFileName;
 
 		if (dateMatcher != null) {
 			dateMatcher.reset(line);
 
 			if (dateMatcher.find()) {
 				int count = dateMatcher.groupCount();
 				for (int i = 1; i <= count; i++) {
 					if (s == null)
 						s = dateMatcher.group(i);
 					else
 						s += dateMatcher.group(i);
 				}
 			}
 		}
 
 		if (s == null)
 			return new Date();
 
 		Date d = null;
 		try {
 			d = dateFormat.parse(s, new ParsePosition(0));
 			if (d == null)
 				return new Date();
 		} catch (NumberFormatException e) {
 			slog.debug("araqne log api: cannot parse date [{}] line [{}]", s, line);
 			return new Date();
 		}
 
 		if (yearModifier != null) {
 			int year = Calendar.getInstance().get(Calendar.YEAR);
 			yearModifier.setTime(d);
 			yearModifier.set(Calendar.YEAR, year);
 			d = yearModifier.getTime();
 		}
 
 		return d;
 	}
 
 	public static void main(String[] args) {
 		Charset forName = Charset.forName("UTF-16LE");
 		System.out.println(forName);
 	}
 
 }
