 package model;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Observable;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 /**
  * Manages a persistent store of history entries across application launchs
  * 
  */
 public class History extends Observable {
 
	private static final String DATE_FORMAT = "Y/M/d H:m:s:S Z";
 
 	private SortedSet<HistoryEntry> history;
 	private SimpleDateFormat dateFormat;
 	private Path file;
 
 	public History(Path file) {
 		this.file = file;
 		history = new TreeSet<HistoryEntry>();
		dateFormat = new SimpleDateFormat(History.DATE_FORMAT);
 	}
 
 	public void addQuery(String query) {
 		HistoryEntry hq = new HistoryEntry(query, new Date());
 		history.add(hq);
 		setChanged();
 		notifyObservers(hq);
 	}
 
 	public SortedSet<HistoryEntry> getQueries() {
 		return history;
 	}
 
 	public void clear() {
 		history.clear();
 		setChanged();
 		notifyObservers();
 	}
 
 	public void save() throws IOException {
 		Files.createDirectories(file.getParent());
         Files.write(file, JSONValue.toJSONString(toJSON()).getBytes());
 	}
 
 	public void load() throws IOException {
 		fromJSON(new String(Files.readAllBytes(file)));
 	}
 
 	@SuppressWarnings("unchecked")
 	private JSONArray toJSON() {
 		JSONArray c = new JSONArray();
 		JSONObject o;
 		for (HistoryEntry hq : history) {
 			o = new JSONObject();
 			o.put("query", hq.getQuery());
 			o.put("time", dateFormat.format(hq.getTime()));
 			c.add(o);
 		}
 		return c;
 	}
 
 	private void fromJSON(String json) {
 		JSONArray c = (JSONArray)JSONValue.parse(json);
 		JSONObject o;
 		String query = null;
 		Date time = null;
 		for (Object oc : c) {
 			o = (JSONObject)oc;
 			query = (String) o.get("query");
 			try {
 				time = dateFormat.parse((String) o.get("time"));
 			} catch (ParseException e) {
 				System.err.printf("Error parsing time '%s' of query '%s': %s\n",  time, query, e.getMessage());
 			}
 			history.add(new HistoryEntry(query, time));
 		}
 	}
 
 }
