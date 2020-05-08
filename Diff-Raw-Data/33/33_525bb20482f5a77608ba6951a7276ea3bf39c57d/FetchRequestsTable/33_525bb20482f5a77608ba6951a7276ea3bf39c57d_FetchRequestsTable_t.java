 package net.kokkeli.data.db;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import com.almworks.sqlite4java.SQLiteConnection;
 import com.almworks.sqlite4java.SQLiteConstants;
 import com.almworks.sqlite4java.SQLiteException;
 import com.almworks.sqlite4java.SQLiteJob;
 import com.almworks.sqlite4java.SQLiteQueue;
 import com.almworks.sqlite4java.SQLiteStatement;
 
 import net.kokkeli.data.FetchRequest;
 import net.kokkeli.data.FetchStatus;
 import net.kokkeli.data.Track;
 
 /**
  * Table representing fetch_requests table in database
  * @author Hekku2
  */
 public class FetchRequestsTable {
     private static final String TABLENAME = "fetch_requests";
     private static final String ALLREQUESTS = "SELECT * FROM " + TABLENAME;
     private static final String DATEFORMAT = "yyyy-MM-dd hh:mm:ss";
     private static final SimpleDateFormat formatter = new SimpleDateFormat(DATEFORMAT);
     
     private static final int IDCOLUMN = 0;
     private static final int LOCATIONCOLUMN = 1;
     private static final int HANDLERCOLUMN = 2;
     private static final int DESTINATIONFILECOLUMN = 3;
     private static final int LASTUPDATEDCOLUMN = 4;
     private static final int STATUSCOLUMN = 5;
     private static final int TRACKCOLUMN = 6;
     
     private final SQLiteQueue queue;
     
     /**
      * Database location
      * @param queue Queue
      */
     public FetchRequestsTable(SQLiteQueue queue){
         this.queue = queue;
     }
 
     /**
      * Fetches all fetch requests from database.
      * Note: Only track id is included
      * @return
      * @throws DatabaseException
      */
     public ArrayList<FetchRequest> get() throws DatabaseException {
         ArrayList<FetchRequest> requests = queue.execute(new SQLiteJob<ArrayList<FetchRequest>>() {
             @Override
             protected ArrayList<FetchRequest> job(SQLiteConnection connection) throws SQLiteException {
                 ArrayList<FetchRequest> requests = new ArrayList<FetchRequest>();
                 
                 SQLiteStatement st = connection.prepare(ALLREQUESTS);
                 
                 try {
                     while (st.step()) {
                         FetchRequest request = new FetchRequest();
                         
                         request.setId(st.columnLong(IDCOLUMN));
                         request.setLocation(st.columnString(LOCATIONCOLUMN));
                         request.setHandler(st.columnString(HANDLERCOLUMN));
                         request.setDestinationFile(st.columnString(DESTINATIONFILECOLUMN));
                         
                         request.setLastUpdated(formatter.parse(st.columnString(LASTUPDATEDCOLUMN)));
                         request.setStatus(getStatus(st.columnInt(STATUSCOLUMN)));
                         request.setTrack(new Track(st.columnLong(TRACKCOLUMN)));
                         
                         requests.add(request);
                     }
                 } catch (ParseException e) {
                     throw new SQLiteException(SQLiteConstants.SQLITE_MISMATCH, "There was an invalid timestamp format in some column.");
                 } finally {
                     st.dispose();
                 }
                 return requests;
             }
         }).complete();
         
         if (requests == null){
             throw new DatabaseException("Getting fetch requests failed.");
         }
         
         return requests;
     }
     
     /**
      * Oldest undhandled request or null
      * @param handler Handler
      * @return Oldest unhandled request or null.
      * @throws DatabaseException Thrown if there is a problem with the database.
      */
     public FetchRequest oldestUnhandledfetchRequest(final String handler) throws DatabaseException {
         FetchRequest request = queue.execute(new SQLiteJob<FetchRequest>() {
             @Override
             protected FetchRequest job(SQLiteConnection connection) throws SQLiteException {
                String command = String.format("%s WHERE FetchStatus = %s AND Handler = '%s' ORDER BY LastUpdated DESC LIMIT 1;", ALLREQUESTS, FetchStatus.WAITING.getStatus(), handler);
                 SQLiteStatement st = connection.prepare(command);
                 
                 FetchRequest request = null;
                 try {
                     while (st.step()) {
                         request = new FetchRequest();
                         
                         request.setId(st.columnLong(IDCOLUMN));
                         request.setLocation(st.columnString(LOCATIONCOLUMN));
                         request.setHandler(st.columnString(HANDLERCOLUMN));
                         request.setDestinationFile(st.columnString(DESTINATIONFILECOLUMN));
                         
                         request.setLastUpdated(formatter.parse(st.columnString(LASTUPDATEDCOLUMN)));
                         request.setStatus(getStatus(st.columnInt(STATUSCOLUMN)));
                         request.setTrack(new Track(st.columnLong(TRACKCOLUMN)));
                     }
                 } catch (ParseException e) {
                     throw new SQLiteException(SQLiteConstants.SQLITE_MISMATCH, "There was an invalid timestamp format in some column.");
                 } finally {
                     st.dispose();
                 }
                 
                 return request;
             }}).complete();
         
         return request;
     }
     
     /**
      * Inserts fetch request to database. 
      * @param item Fetch request
      * @return Added fetchrequest with new id.
      * @throws DatabaseException Thrown if there is a problem with the database.
      */
     public FetchRequest insert(final FetchRequest item) throws DatabaseException {
         Long id = queue.execute(new SQLiteJob<Long>() {
             @Override
             protected Long job(SQLiteConnection connection) throws SQLiteException {
                 SQLiteStatement st = connection.prepare(createInsertString(item));
                 
                 try {
                     st.stepThrough();
                 } finally {
                     st.dispose();
                 }
                 
                 return connection.getLastInsertId();
             }
           }).complete();
 
         if (id == null){
             throw new DatabaseException("Inserting fetch request failed.");
         }
         
         item.setId(id);
         return item;
     }
     
     /**
      * Updates fetchrequest to wanted status
      * @param id Id of track
      * @param status New status
      * @throws DatabaseException Thrown if there is a problem with the database.
      */
     public void update(final long id, final FetchStatus status) throws DatabaseException {
         queue.execute(new SQLiteJob<Object>() {
             @Override
             protected Object job(SQLiteConnection connection) throws SQLiteException {
                 SQLiteStatement st = connection.prepare(createStatusUpdateString(id, status));
                 try {
                     st.stepThrough();
                 } finally {
                     st.dispose();
                 }
                 return null;
             }
           });
     }
     
     /**
      * Removes fetch requests with given status
      * @param status Status
      * @throws DatabaseException Thrown if there is a problem with the database
      */
     public void removeWithStatus(final FetchStatus status) throws DatabaseException {
         queue.execute(new SQLiteJob<Object>() {
             @Override
             protected Object job(SQLiteConnection connection) throws SQLiteException {
                 SQLiteStatement st = connection.prepare(createStatusRemove(status));
                 try {
                     st.stepThrough();
                 } finally {
                     st.dispose();
                 }
                 return null;
             }
           });
     }
     
     public void remove(final long requestId) {
         queue.execute(new SQLiteJob<Object>() {
             @Override
             protected Object job(SQLiteConnection connection) throws SQLiteException {
                 SQLiteStatement st = connection.prepare(createRemove(requestId));
                 try {
                     st.stepThrough();
                 } finally {
                     st.dispose();
                 }
                 return null;
             }
           });
     }
     
     /**
      * Create remove command that removes request with given id
      * @param requestId Request id
      * @return Create String
      */
     private static String createRemove(long requestId) {
         return String.format("DELETE FROM %s WHERE Id=%s;", TABLENAME, requestId);
     }
 
     /**
      * Creates command that removes all requests with given status
      * @param status Status
      * @return Created string
      */
     private static String createStatusRemove(FetchStatus status) {
         return String.format("DELETE FROM %s WHERE FetchStatus=%s;", TABLENAME, status.getStatus());
     }
 
     /**
      * Creates update string for fetch status
      * @param id Id to update
      * @param status New status
      * @return Created string
      */
     private static String createStatusUpdateString(long id, FetchStatus status) {
         return String.format("UPDATE %s SET FetchStatus=%s WHERE Id=%s;",TABLENAME, status.getStatus(), id);
     }
 
     /**
      * Creates insert string for fetch request
      * @param item Fetch request to insert
      * @return Insert String
      */
     private static String createInsertString(FetchRequest item) {
         return String.format("INSERT INTO %s(Location, Handler, DestinationFile, LastUpdated, FetchStatus, Track) VALUES ('%s', '%s', '%s', '%s', %s, %s); ",
                 TABLENAME,
                 item.getLocation(),
                 item.getHandler(),
                 item.getDestinationFile(),
                 formatter.format(item.getLastUpdated()),
                 item.getStatus().getStatus(),
                 item.getTrack().getId());
     }
 
     /**
      * Returns FectchStatus with given id);
      * @param id Id of status
      * @return FectchStatus with given id.
      * @throws IndexOutOfBoundsException Thrown if the is no such status
      */
     private static FetchStatus getStatus(int status){
         for (FetchStatus possible : FetchStatus.values()) {
             if (possible.getStatus() == status){
                 return possible;
             }
         }
         throw new IndexOutOfBoundsException(String.format("There was no fetch status with given Id (%s).", status));
     }
 }
