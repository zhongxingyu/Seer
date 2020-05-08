 package org.araqne.logdb.metadata;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.logdb.FunctionRegistry;
 import org.araqne.logdb.MetadataCallback;
 import org.araqne.logdb.MetadataProvider;
 import org.araqne.logdb.MetadataService;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.query.parser.CommandOptions;
 import org.araqne.logdb.query.parser.ParseResult;
 import org.araqne.logdb.query.parser.QueryTokenizer;
 
 @Component(name = "logdb-thread-metadata")
 public class ThreadMetadataProvider implements MetadataProvider {
 
 	@Requires
 	private MetadataService metadataService;
 
 	@Requires
 	private FunctionRegistry functionRegistry;
 
 	@Validate
 	public void start() {
 		metadataService.addProvider(this);
 	}
 
 	@Invalidate
 	public void stop() {
 		if (metadataService != null)
 			metadataService.removeProvider(this);
 	}
 
 	@Override
 	public String getType() {
 		return "threads";
 	}
 
 	@Override
 	public void verify(QueryContext context, String queryString) {
 		if (!context.getSession().isAdmin())
 			throw new QueryParseException("no-read-permission", -1);
 
 		QueryTokenizer.parseOptions(context, queryString, 0, Arrays.asList("prettystack"), functionRegistry);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void query(QueryContext context, String queryString, MetadataCallback callback) {
 		ParseResult r = QueryTokenizer.parseOptions(context, queryString, 0, Arrays.asList("prettystack"), functionRegistry);
 		Map<String, Object> options = (Map<String, Object>) r.value;

 		boolean prettyStack = false;
 		if (options.get("prettystack") != null) {
 			prettyStack = CommandOptions.parseBoolean(options.get("prettystack").toString());
 		}
 
 		Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
 		List<Thread> threads = new ArrayList<Thread>(stackTraces.keySet());
 		Collections.sort(threads, new ThreadOrder());
 
 		for (Thread t : threads) {
 			StackTraceElement[] stacktrace = stackTraces.get(t);
 
 			Map<String, Object> m = new HashMap<String, Object>();
 			m.put("tid", t.getId());
 			m.put("name", t.getName());
			m.put("group", t.getThreadGroup() != null ? t.getThreadGroup().getName() : null);
 			m.put("state", t.getState().toString());
 			m.put("priority", t.getPriority());
 			m.put("stacktrace", prettyStack ? mergeStackTrace(stacktrace) : convertStackTrace(stacktrace));
 
 			callback.onPush(new Row(m));
 		}
 	}
 
 	private String mergeStackTrace(StackTraceElement[] stacktrace) {
 		StringBuilder sb = new StringBuilder();
 		for (StackTraceElement el : stacktrace) {
 			sb.append(String.format("%s.%s %s\n", el.getClassName(), el.getMethodName(), getFileAndLineNumber(el)));
 		}
 		return sb.toString();
 	}
 
 	private String getFileAndLineNumber(StackTraceElement el) {
 		if (el.getFileName() != null && el.getLineNumber() > 0)
 			return String.format("(%s:%d)", el.getFileName(), el.getLineNumber());
 		else if (el.getFileName() != null && el.getLineNumber() <= 0)
 			return String.format("(%s)", el.getFileName());
 		else
 			return "";
 	}
 
 	private List<Object> convertStackTrace(StackTraceElement[] stacktrace) {
 		List<Object> l = new ArrayList<Object>();
 		if (stacktrace == null)
 			return null;
 
 		for (StackTraceElement e : stacktrace) {
 			Map<String, Object> m = new HashMap<String, Object>();
 			m.put("class", e.getClassName());
 			m.put("file", e.getFileName());
 			m.put("line", e.getLineNumber());
 			m.put("method", e.getMethodName());
 			l.add(m);
 		}
 
 		return l;
 	}
 
 	private static class ThreadOrder implements Comparator<Thread> {
 		@Override
 		public int compare(Thread o1, Thread o2) {
 			return (int) (o1.getId() - o2.getId());
 		}
 	}
 }
