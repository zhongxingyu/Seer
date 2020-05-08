 import java.util.HashSet;
 import java.util.Set;
 
 import com.sleepycat.db.Cursor;
 import com.sleepycat.db.Database;
 import com.sleepycat.db.DatabaseEntry;
 import com.sleepycat.db.LockMode;
 import com.sleepycat.db.OperationStatus;
 
 
 public class DateQuerySearch extends QuerySearch {
 	
 	private QueryDate date;
 	private String searchText;
 	
 	public DateQuerySearch() {}
 	
 	public Set<Integer> get(QueryDate date) {
 		Set<Integer> ids = new HashSet<Integer>();
 		this.date = date;
 		
 		if (date == null) {
 			System.err.println("ERROR: Date was null");
 			return ids;
 		}
 		
 		Database db = DatabaseManager.getInstance().getDates();
 		
 		String key = String.format("%s", date.getDate());
 		this.searchText = key;
 		ids.addAll(get(db, key));
 		
 		return ids;
 	}
 
 	@Override
 	public OperationStatus first(Cursor cursor, DatabaseEntry key,
 			DatabaseEntry data) {
 		OperationStatus retVal = OperationStatus.NOTFOUND;
 		
 		try {
 			retVal = cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);
 		} catch (Exception ex) {
 			ex.getMessage();
 		}
 		
 		return retVal;
 	}
 
 	@Override
 	public OperationStatus next(Cursor cursor, DatabaseEntry key,
 			DatabaseEntry data) {
 		OperationStatus retVal = OperationStatus.NOTFOUND;
 		
 		try {
 			if (this.date.getSearchFor() == QueryDate.SearchDate.SINCE)
 				retVal = cursor.getNext(key, data, LockMode.DEFAULT);
 			else if (this.date.getSearchFor() == QueryDate.SearchDate.UNTIL)
 				retVal = cursor.getPrev(key, data, LockMode.DEFAULT);
 		} catch (Exception ex) {
 			ex.getMessage();
 		}
 		
 		return retVal;
 	}
 
 	@Override
 	public Integer getId(DatabaseEntry key, DatabaseEntry data) {
 		Integer id = null;
 		
		String keyString = new String(key.getData());
 		String dataString = new String(data.getData());
 		
		if (!keyString.equals(this.searchText)) {
			try {
				id = new Integer(dataString);
			} catch (Exception ex) {
				id = null;
			}
 		}
 		
 		return id;
 	}
 
 }
