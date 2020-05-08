 package com.github.davidmoten.logan;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NavigableSet;
 import java.util.TimeZone;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.logging.Logger;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.base.Supplier;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.ListMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimaps;
 import com.google.common.collect.Sets;
 
 public class Data {
 
 	private static final int DEFAULT_MAX_SIZE = 1000000;
 
 	private static Logger log = Logger.getLogger(Data.class.getName());
 
 	private TreeMap<Long, Collection<LogEntry>> map;
 	private ListMultimap<Long, LogEntry> facade;
 	private final TreeSet<String> keys = Sets.newTreeSet();
 	private final TreeSet<String> sources = Sets.newTreeSet();
 
 	private final int maxSize;
 	private final AtomicLong counter = new AtomicLong();
 
 	public Data() {
 		this(DEFAULT_MAX_SIZE, false);
 	}
 
 	public Data(int maxSize, boolean loadDummyData) {
 		this.maxSize = maxSize;
 		map = Maps.newTreeMap();
 		facade = Multimaps.newListMultimap(map, new Supplier<List<LogEntry>>() {
 			@Override
 			public List<LogEntry> get() {
 				return Lists.newArrayList();
 			}
 		});
 		if (loadDummyData)
 			for (int i = 0; i < 10000; i++)
 				add(createRandomLogEntry(i));
 	}
 
 	private static LogEntry createRandomLogEntry(int i) {
 		Map<String, String> map = Maps.newHashMap();
 		String sp1 = Math.random() * 100 + "";
 		map.put("specialNumber", sp1);
 		String sp2 = Math.random() * 50 + "";
 		map.put("specialNumber2", sp2);
 		boolean processing = Math.random() > 0.5;
 		map.put("processing", processing + "");
 		map.put(Field.MSG, "processing=" + processing + ",specialNumber=" + sp1
 				+ ",specialNumber2=" + sp2);
 
 		return new LogEntry(System.currentTimeMillis()
 				- TimeUnit.MINUTES.toMillis(i), map);
 	}
 
 	/**
 	 * Adds a {@link LogEntry} to the data.
 	 * 
 	 * @param entry
 	 * @return this
 	 */
 	public synchronized Data add(LogEntry entry) {
 		facade.put(entry.getTime(), entry);
 		for (Entry<String, String> pair : entry.getProperties().entrySet())
 			if (isNumeric(pair.getValue()))
 				keys.add(pair.getKey());
 		if (facade.size() % 10000 == 0 && facade.size() < maxSize)
 			log.info("data size=" + facade.size());
 		if (facade.size() > maxSize)
 			facade.removeAll(map.firstKey());
 		String source = entry.getSource();
 		if (source != null)
 			sources.add(source);
 		incrementCounter();
 		return this;
 	}
 
 	private boolean isNumeric(String s) {
 		try {
 			Double.parseDouble(s);
 			return true;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 	}
 
 	public synchronized Iterable<LogEntry> find(final long startTime,
 			final long finishTime) {
 
 		return new Iterable<LogEntry>() {
 
 			@Override
 			public Iterator<LogEntry> iterator() {
 				return createIterator(startTime, finishTime);
 			}
 
 		};
 	}
 
 	private synchronized Iterator<LogEntry> createIterator(
 			final long startTime, final long finishTime) {
 
 		return new Iterator<LogEntry>() {
 
 			Long t = map.ceilingKey(startTime);
 			Long last = map.floorKey(finishTime);
 			Iterator<LogEntry> it = null;
 
 			@Override
 			public boolean hasNext() {
 				if (it == null || !it.hasNext())
 					return last != null && t != null && t <= last;
 				else
 					return it.hasNext();
 			}
 
 			@Override
 			public LogEntry next() {
 				while (it == null || !it.hasNext()) {
 					it = map.get(t).iterator();
 					t = map.higherKey(t);
 				}
 				return it.next();
 			}
 
 			@Override
 			public void remove() {
 				throw new RuntimeException("not implemented");
 			}
 
 		};
 
 	}
 
 	public synchronized Buckets execute(final BucketQuery query) {
 
 		Iterable<LogEntry> entries = find(query.getStartTime().getTime(),
 				query.getFinishTime());
 
 		Iterable<LogEntry> filtered = entries;
 
 		// filter by field
 		if (query.getField().isPresent()) {
 			filtered = Iterables.filter(filtered, new Predicate<LogEntry>() {
 				@Override
 				public boolean apply(LogEntry entry) {
 					String value = entry.getProperties().get(
 							query.getField().get());
 					return value != null;
 				}
 			});
 		}
 
 		// filter by source
 		if (query.getSource().isPresent())
			filtered = Iterables.filter(entries, new Predicate<LogEntry>() {
 				@Override
 				public boolean apply(LogEntry entry) {
 					String src = entry.getProperties().get(Field.SOURCE);
 					return query.getSource().get().equals(src);
 				}
 			});
 
 		// filter by text
 		if (query.getText().isPresent())
			filtered = Iterables.filter(entries, new Predicate<LogEntry>() {
 				@Override
 				public boolean apply(LogEntry entry) {
 					String searchFor = query.getText().get();
 					return contains(entry, Field.MSG, searchFor)
 							|| contains(entry, Field.LEVEL, searchFor)
 							|| contains(entry, Field.METHOD, searchFor)
 							|| contains(entry, Field.SOURCE, searchFor)
 							|| contains(entry, Field.THREAD_NAME, searchFor);
 				}
 			});
 
 		Buckets buckets = new Buckets(query);
 		for (LogEntry entry : filtered) {
 			if (query.getField().isPresent()) {
 				String s = entry.getProperties().get(query.getField().get());
 				try {
 					double d = Double.parseDouble(s);
 					buckets.add(entry.getTime(), d);
 				} catch (NumberFormatException e) {
 					// ignored value because non-numeric
 				}
 			} else
 				// just count the entries
 				buckets.add(entry.getTime(), 1);
 		}
 
 		return buckets;
 	}
 
 	private static boolean contains(LogEntry entry, String field,
 			String searchFor) {
 		String s = entry.getProperties().get(field);
 		if (s == null)
 			return false;
 		else
 			return s.contains(searchFor);
 	}
 
 	public synchronized long getNumEntries() {
 		return facade.size();
 	}
 
 	public synchronized long getNumEntriesAdded() {
 		return counter.get();
 	}
 
 	public NavigableSet<String> getKeys() {
 		return keys;
 	}
 
 	public Iterable<String> getLogs(long startTime, long finishTime) {
 		return Iterables.transform(find(startTime, finishTime),
 				new Function<LogEntry, String>() {
 					@Override
 					public String apply(LogEntry entry) {
 						StringBuilder s = new StringBuilder();
 						DateFormat df = new SimpleDateFormat(
 								"yyyy-MM-dd HH:mm:ss.SSS");
 						df.setTimeZone(TimeZone.getTimeZone("UTC"));
 						s.append(df.format(new Date(entry.getTime())));
 						s.append(' ');
 						s.append(entry.getProperties().get(Field.LEVEL));
 						s.append(' ');
 						s.append(entry.getProperties().get(Field.LOGGER));
 						s.append(" - ");
 						s.append(entry.getProperties().get(Field.MSG));
 						return s.toString();
 					}
 				});
 	}
 
 	public NavigableSet<String> getSources() {
 		return sources;
 	}
 
 	public Date oldestTime() {
 		if (map.size() == 0)
 			return null;
 		else
 			return new Date(map.firstKey());
 	}
 
 	private synchronized void incrementCounter() {
 		if (counter.incrementAndGet() % 1000 == 0)
 			log.info(counter + " log lines processed");
 	}
 
 }
