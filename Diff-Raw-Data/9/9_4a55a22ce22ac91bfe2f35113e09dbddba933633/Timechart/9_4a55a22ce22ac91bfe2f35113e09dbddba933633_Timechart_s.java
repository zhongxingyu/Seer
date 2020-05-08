 /*
  * Copyright 2011 Future Systems
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
 package org.araqne.logdb.query.command;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryCommand;
 import org.araqne.logdb.ObjectComparator;
 import org.araqne.logdb.query.aggregator.AggregationField;
 import org.araqne.logdb.query.aggregator.AggregationFunction;
 import org.araqne.logdb.query.aggregator.PerTime;
 import org.araqne.logdb.sort.CloseableIterator;
 import org.araqne.logdb.sort.Item;
 import org.araqne.logdb.sort.ParallelMergeSorter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Timechart extends LogQueryCommand {
 	private final Logger logger = LoggerFactory.getLogger(Timechart.class);
 
 	public static class TimeSpan {
 		public int amount;
 		public TimeUnit unit;
 
 		public TimeSpan(int amount, TimeUnit unit) {
 			this.amount = amount;
 			this.unit = unit;
 		}
 	}
 
 	public static enum TimeUnit {
 		Second(Calendar.SECOND, 1000L), Minute(Calendar.MINUTE, 60 * 1000L), Hour(Calendar.HOUR_OF_DAY, 60 * 60 * 1000L), Day(
 				Calendar.DAY_OF_MONTH, 86400 * 1000L), Week(Calendar.WEEK_OF_YEAR, 7 * 86400 * 1000L), Month(Calendar.MONTH,
 				30 * 86400 * 1000L), Year(Calendar.YEAR, 365 * 86400 * 1000L);
 
 		private int calendarField;
 		private long millis;
 
 		private static final int[] mon2Spans = new int[] { 1, 3, 5, 7, 9, 11 };
 		private static final int[] mon3Spans = new int[] { 1, 4, 7, 10 };
 		private static final int[] mon4Spans = new int[] { 1, 5, 9 };
 		private static final int[] mon6Spans = new int[] { 1, 7 };
 
 		private TimeUnit(int calendarField, long millis) {
 			this.calendarField = calendarField;
 			this.millis = millis;
 		}
 
 		public static Date getKey(Date date, TimeSpan timeSpan) {
 			TimeUnit spanField = timeSpan.unit;
 
 			if (spanField == TimeUnit.Month) {
 				Calendar c = Calendar.getInstance();
 				c.setTime(date);
 				int mon = c.get(Calendar.MONTH);
 
 				if (timeSpan.amount == 2)
 					c.set(Calendar.MONTH, mon2Spans[mon / 2] - 1);
 				else if (timeSpan.amount == 3)
 					c.set(Calendar.MONTH, mon3Spans[mon / 3] - 1);
 				else if (timeSpan.amount == 4)
 					c.set(Calendar.MONTH, mon4Spans[mon / 4] - 1);
 				else if (timeSpan.amount == 6)
 					c.set(Calendar.MONTH, mon6Spans[mon / 6] - 1);
 
 				c.set(Calendar.DAY_OF_MONTH, 1);
 				c.set(Calendar.HOUR_OF_DAY, 0);
 				c.set(Calendar.MINUTE, 0);
 				c.set(Calendar.SECOND, 0);
 				c.set(Calendar.MILLISECOND, 0);
 				return c.getTime();
 			} else if (spanField == TimeUnit.Year) {
 				Calendar c = Calendar.getInstance();
 				c.setTime(date);
 				c.set(Calendar.MONTH, 0);
 				c.set(Calendar.DAY_OF_MONTH, 1);
 				c.set(Calendar.HOUR_OF_DAY, 0);
 				c.set(Calendar.MINUTE, 0);
 				c.set(Calendar.SECOND, 0);
 				c.set(Calendar.MILLISECOND, 0);
 				return c.getTime();
 			} else {
 				long time = date.getTime();
 
 				int spanAmount = timeSpan.amount;
 				time += 291600000L; // base to Monday, 00:00:00
 				time -= time % (spanField.millis * spanAmount);
 				time -= 291600000L;
 				return new Date(time);
 			}
 		}
 	}
 
 	// sort by timechart key, and merge incrementally in eof()
 	private ParallelMergeSorter sorter;
 
 	// definition of aggregation fields
 	private List<AggregationField> fields;
 
 	// clone template functions
 	private AggregationFunction[] funcs;
 
 	// flush waiting buffer. merge in memory as many as possible
 	private HashMap<TimechartKey, AggregationFunction[]> buffer;
 
 	private TimeSpan timeSpan;
 
 	// span time in milliseconds. e.g. '172,800,000' for '2d'
 	private long spanMillis;
 
 	// key field name ('by' clause of command)
 	private String keyField;
 
 	public Timechart(List<AggregationField> fields, String keyField, TimeSpan timeSpan) {
 		this.fields = fields;
 		this.keyField = keyField;
 		this.timeSpan = timeSpan;
 
 		// set up clone templates
 		this.funcs = new AggregationFunction[fields.size()];
 		for (int i = 0; i < fields.size(); i++)
 			this.funcs[i] = fields.get(i).getFunction();
 	}
 
 	public List<AggregationField> getAggregationFields() {
 		return fields;
 	}
 
 	public TimeSpan getTimeSpan() {
 		return timeSpan;
 	}
 
 	public String getKeyField() {
 		return keyField;
 	}
 
 	@Override
 	public void init() {
 		super.init();
 		this.sorter = new ParallelMergeSorter(new ItemComparer());
 		this.buffer = new HashMap<TimechartKey, AggregationFunction[]>();
 		this.spanMillis = getSpanMillis();
 
 		logger.debug("araqne logdb: span millis [{}] for query [{}]", spanMillis, logQuery);
 	}
 
 	private long getSpanMillis() {
 		Date d = new Date();
 		Calendar c = Calendar.getInstance();
 		c.setTime(d);
 		c.add(timeSpan.unit.calendarField, timeSpan.amount);
 		return c.getTimeInMillis() - d.getTime();
 	}
 
 	@Override
 	public void push(LogMap m) {
		Date time = TimeUnit.getKey((Date) m.get("_time"), timeSpan);
 		String keyFieldValue = null;
 		if (keyField != null) {
 			if (m.get(keyField) == null)
 				return;
 			keyFieldValue = m.get(keyField).toString();
 		}
 
 		// bucket is identified by truncated time and key field value. each
 		// bucket has function array.
		TimechartKey key = new TimechartKey(time, keyFieldValue);
 
 		// find or create flush waiting bucket
 		AggregationFunction[] fs = buffer.get(key);
 		if (fs == null) {
 			fs = new AggregationFunction[funcs.length];
 			for (int i = 0; i < fs.length; i++) {
 				fs[i] = funcs[i].clone();
 
 				// set span milliseconds for average evaluation per span
 				if (fs[i] instanceof PerTime)
 					((PerTime) fs[i]).setAmount(spanMillis);
 			}
 
 			buffer.put(key, fs);
 		}
 
 		// aggregate for each functions
 		for (AggregationFunction f : fs)
 			f.apply(m);
 
 		// flush if flood
 		try {
 			if (buffer.size() > 50000)
 				flush();
 		} catch (IOException e) {
 			throw new IllegalStateException("timechart sort failed, query " + logQuery, e);
 		}
 	}
 
 	@Override
 	public boolean isReducer() {
 		return true;
 	}
 
 	private void flush() throws IOException {
 		for (TimechartKey key : buffer.keySet()) {
 			AggregationFunction[] fs = buffer.get(key);
 			Object[] l = new Object[fs.length];
 			int i = 0;
 			for (AggregationFunction f : fs)
 				l[i++] = f.serialize();
 
 			sorter.add(new Item(new Object[] { key.time, key.key }, l));
 		}
 
 		buffer.clear();
 	}
 
 	@Override
 	public void eof(boolean canceled) {
 		this.status = Status.Finalizing;
 
 		CloseableIterator it = null;
 		try {
 			// last flush
 			flush();
 
 			// reclaim buffer (GC support)
 			buffer = null;
 
 			// sort
 			it = sorter.sort();
 
 			mergeAndWrite(it);
 		} catch (IOException e) {
 			throw new IllegalStateException("timechart sort failed, query " + logQuery, e);
 		} finally {
 			if (it != null) {
 				try {
 					// close and delete final sorted run file
 					it.close();
 				} catch (IOException e) {
 				}
 			}
 			super.eof(canceled);
 		}
 
 		// support sorter cache GC when query processing is ended
 		sorter = null;
 
 		super.eof(canceled);
 	}
 
 	private void mergeAndWrite(CloseableIterator it) {
 		Date lastTime = null;
 
 		// value of key field
 		String lastKeyFieldValue = null;
 		AggregationFunction[] fs = null;
 		HashMap<String, Object> output = new HashMap<String, Object>();
 
 		while (it.hasNext()) {
 			Item item = it.next();
 
 			// timechart key (truncated time & key value)
 			Object[] itemKeys = (Object[]) item.getKey();
 			Date time = (Date) itemKeys[0];
 			String keyFieldValue = (String) itemKeys[1];
 
 			// init functions at first time
 			if (fs == null) {
 				fs = new AggregationFunction[funcs.length];
 				for (int i = 0; i < funcs.length; i++)
 					fs[i] = loadFunction(item, i);
 
 				lastTime = time;
 				lastKeyFieldValue = keyFieldValue;
 				continue;
 			}
 
 			// if key value is changed
 			boolean reset = false;
 			if (lastKeyFieldValue != null && !lastKeyFieldValue.equals(keyFieldValue)) {
 				setOutputAndReset(output, fs, lastKeyFieldValue);
 				reset = true;
 			}
 
 			// until _time is changed
 			if (lastTime != null && !lastTime.equals(time)) {
 				if (!reset)
 					setOutputAndReset(output, fs, lastKeyFieldValue);
 
 				// write to next pipeline
 				output.put("_time", lastTime);
 				write(new LogMap(output));
 				output = new HashMap<String, Object>();
 
 				// change merge set
 				fs = new AggregationFunction[funcs.length];
 				for (int i = 0; i < funcs.length; i++)
 					fs[i] = loadFunction(item, i);
 
 				lastTime = time;
 				lastKeyFieldValue = keyFieldValue;
 				continue;
 			}
 
 			// merge all
 			int i = 0;
 			for (AggregationFunction f : fs) {
 				AggregationFunction f2 = loadFunction(item, i);
 				f.merge(f2);
 				i++;
 			}
 
 			lastTime = time;
 			lastKeyFieldValue = keyFieldValue;
 		}
 
 		// write last item (can be null if input count is 0)
 		if (lastTime != null) {
 			output.put("_time", lastTime);
 			setOutputAndReset(output, fs, lastKeyFieldValue);
 			write(new LogMap(output));
 		}
 	}
 
 	private void setOutputAndReset(Map<String, Object> output, AggregationFunction[] fs, String keyFieldValue) {
 		if (keyField != null) {
 			if (fs.length > 1) {
 				int i = 0;
 				for (AggregationFunction f : fs) {
 					// field name format is func:keyfieldvalue (when
 					// by-clause is provided)
 					output.put(fields.get(i).getName() + ":" + keyFieldValue, f.eval());
 					f.clean();
 					i++;
 				}
 			} else {
 				output.put(keyFieldValue, fs[0].eval());
 				fs[0].clean();
 			}
 		} else {
 			int i = 0;
 			for (AggregationFunction f : fs) {
 				output.put(fields.get(i).getName(), f.eval());
 				f.clean();
 				i++;
 			}
 		}
 	}
 
 	private AggregationFunction loadFunction(Item item, int i) {
 		Object[] l = (Object[]) ((Object[]) item.getValue())[i];
 		AggregationFunction f2 = funcs[i].clone();
 		f2.deserialize(l);
 		return f2;
 	}
 
 	private static class ItemComparer implements Comparator<Item> {
 		private ObjectComparator cmp = new ObjectComparator();
 
 		@Override
 		public int compare(Item o1, Item o2) {
 			return cmp.compare(o1.getKey(), o2.getKey());
 		}
 	}
 
 	private static class TimechartKey implements Comparable<TimechartKey> {
 		// truncated time by span amount
 		public Date time;
 
 		// value of key field ('by' clause, can be null)
 		public String key;
 
 		public TimechartKey(Date time, String key) {
 			this.time = time;
 			this.key = key;
 		}
 
 		@Override
 		public int compareTo(TimechartKey o) {
 			if (o == null)
 				return -1;
 
 			int diff = (int) (time.getTime() - o.time.getTime());
 			if (diff != 0)
 				return diff;
 
 			if (key == null && o.key != null)
 				return -1;
 			else if (key != null && o.key == null)
 				return 1;
 
 			diff = key.compareTo(o.key);
 			return diff;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((key == null) ? 0 : key.hashCode());
 			result = prime * result + ((time == null) ? 0 : time.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			TimechartKey other = (TimechartKey) obj;
 			if (key == null) {
 				if (other.key != null)
 					return false;
 			} else if (!key.equals(other.key))
 				return false;
 			if (time == null) {
 				if (other.time != null)
 					return false;
 			} else if (!time.equals(other.time))
 				return false;
 			return true;
 		}
 	}
 }
