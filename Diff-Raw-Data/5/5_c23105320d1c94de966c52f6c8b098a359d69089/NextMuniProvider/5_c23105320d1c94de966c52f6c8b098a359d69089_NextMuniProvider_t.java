 package info.yasskin.droidmuni;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.MatrixCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.util.Log;
 
 public class NextMuniProvider extends ContentProvider {
   private static final long ONE_DAY = 24 * 3600 * 1000;
   private static final long ONE_MONTH = 30 * ONE_DAY;
 
   public static final String AUTHORITY =
       "info.yasskin.droidmuni.nextmuniprovider";
   public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
 
   public static final Uri ROUTES_URI = Uri.withAppendedPath(CONTENT_URI,
       "routes");
   public static final Uri DIRECTIONS_URI = Uri.withAppendedPath(CONTENT_URI,
       "directions");
   public static final Uri STOPS_URI =
       Uri.withAppendedPath(CONTENT_URI, "stops");
   public static final Uri PREDICTIONS_URI = Uri.withAppendedPath(CONTENT_URI,
       "predictions");
 
   private static final int NEXT_MUNI_ROUTES = 0;
   private static final int NEXT_MUNI_ROUTE_ID = 1;
   private static final int NEXT_MUNI_DIRECTIONS = 2;
   private static final int NEXT_MUNI_STOPS = 4;
   private static final int NEXT_MUNI_PREDICTIONS = 5;
 
   private static final UriMatcher sURLMatcher = new UriMatcher(
       UriMatcher.NO_MATCH);
 
   static {
     sURLMatcher.addURI(AUTHORITY, "routes", NEXT_MUNI_ROUTES);
     sURLMatcher.addURI(AUTHORITY, "routes/#", NEXT_MUNI_ROUTE_ID);
     sURLMatcher.addURI(AUTHORITY, "directions/*", NEXT_MUNI_DIRECTIONS);
     sURLMatcher.addURI(AUTHORITY, "stops/*/*", NEXT_MUNI_STOPS);
     sURLMatcher.addURI(AUTHORITY, "predictions/#", NEXT_MUNI_PREDICTIONS);
   }
 
   @Override
   public String getType(Uri uri) {
     switch (sURLMatcher.match(uri)) {
     case NEXT_MUNI_ROUTES:
       return "vnd.android.cursor.dir/vnd.yasskin.route";
     case NEXT_MUNI_ROUTE_ID:
       return "vnd.android.cursor.item/vnd.yasskin.route";
     case NEXT_MUNI_DIRECTIONS:
       return "vnd.android.cursor.dir/vnd.yasskin.direction";
     case NEXT_MUNI_STOPS:
       return "vnd.android.cursor.dir/vnd.yasskin.stop";
     case NEXT_MUNI_PREDICTIONS:
       return "vnd.android.cursor.dir/vnd.yasskin.prediction";
     default:
       throw new IllegalArgumentException("Unknown URI " + uri);
     }
   }
 
  // TODO private final DefaultHttpClient mClient = new DefaultHttpClient();
   private boolean m_someone_fetching_routes = false; // Guarded by db.
   // The next field is set in onCreate() and never modified again.
   private Db db;
 
   @Override
   public boolean onCreate() {
     Context context = getContext();
     db = new Db(context);
     Globals.EXECUTOR.execute(new Runnable() {
       public void run() {
         // Prime the routes list eagerly so it's more likely it'll
         // be ready by the time we need it. Don't, however, block onCreate()
         // until it finishes since that'll block the UI thread even when we
         // already have the routes list.
         try {
           tryFetchRoutes(REFETCH_ROUTES_BLOCK);
         } catch (Exception e) {
           Log.e("DroidMuni", "tryFetchRoutes failed", e);
         }
       }
     });
     return true;
   }
 
   private static final int REFETCH_ROUTES_BLOCK = 0;
   private static final int REFETCH_ROUTES_NOBLOCK = 1;
 
   /**
    * If the database doesn't already have the list of routes, requests the list
    * from NextMUNI. Uses m_someone_fetching_routes to make sure we only send one
    * request at a time. If we start a call while another thread is fetching, and
    * they fail, we fail too in order to bound the maximum blocking time to a
    * single request timeout.
    * 
    * @param block_on_refetch_routes
    *          REFETCH_ROUTES_BLOCK or REFETCH_ROUTES_NOBLOCK, depending on
    *          whether this call should block on refetching the routes. The
    *          method always blocks when fetching the routes for the first time.
    * 
    * @return The routes, or null if this or a concurrent request failed.
    */
   private void tryFetchRoutes(int block_on_refetch_routes) {
     final long oldest_acceptable_routes = System.currentTimeMillis() - ONE_DAY;
     if (db.hasRoutes()) {
       if (db.routesNewerThan(oldest_acceptable_routes)) {
         // If we already have new-enough routes, return without doing any work.
         return;
       }
       if (block_on_refetch_routes == REFETCH_ROUTES_NOBLOCK) {
         // If our routes exist but are too old, and the caller doesn't want to
         // block, spawn this task into the background pool and
         // return immediately.
         Globals.EXECUTOR.execute(new Runnable() {
           public void run() {
             try {
               tryFetchRoutes(REFETCH_ROUTES_BLOCK);
             } catch (Exception e) {
               Log.e("DroidMuni", "tryFetchRoutes failed", e);
             }
           }
         });
         return;
       }
       // Otherwise our routes exist and are too old, and we're already in a
       // background thread, so we can block. Continue into the main function.
     }
 
     final boolean routes_outdated;
     final boolean someone_was_fetching_routes;
     synchronized (db) {
       routes_outdated = !db.routesNewerThan(oldest_acceptable_routes);
       if (routes_outdated) {
         someone_was_fetching_routes = m_someone_fetching_routes;
         while (m_someone_fetching_routes) {
           try {
             db.wait();
           } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             return;
           }
         }
         if (someone_was_fetching_routes) {
           return;
         } else {
           m_someone_fetching_routes = true;
         }
       }
     }
     try {
       if (routes_outdated) {
         getRoutes();
       }
     } finally {
       synchronized (db) {
         m_someone_fetching_routes = false;
         db.notifyAll();
       }
     }
   }
 
   /**
    * Downloads the list of routes.
    * 
    * @return true if the HTTP call succeeded.
    */
   private Boolean getRoutes() {
     RouteListParser parser =
         getAndParse(NextMuniUriBuilder.buildRouteListUri("sf-muni").toString(),
             RouteListParser.class);
     if (parser == null) {
       return false;
     }
     db.setRoutes(parser.getRoutes());
     return true;
   }
 
   @Override
   public Cursor query(Uri uri, String[] projection, String selection,
       String[] selectionArgs, String sortOrder) {
     switch (sURLMatcher.match(uri)) {
     case NEXT_MUNI_ROUTES:
       tryFetchRoutes(REFETCH_ROUTES_NOBLOCK);
       String[] COLUMNS = { "_id", "tag", "description" };
       Cursor result =
           db.getReadableDatabase().query("Routes", COLUMNS, null, null, null,
               null, "upstream_index");
       if (result.getCount() == 0) {
         result.close();
         return null;
       }
       return result;
     case NEXT_MUNI_DIRECTIONS:
       return queryDirections("sf-muni", uri.getPathSegments().get(1));
     case NEXT_MUNI_STOPS:
       return queryStops("sf-muni", uri.getPathSegments().get(1),
           uri.getPathSegments().get(2));
     case NEXT_MUNI_PREDICTIONS:
       return queryPredictions("sf-muni", uri.getPathSegments().get(1));
     default:
       throw new IllegalArgumentException("Unknown URI " + uri);
     }
   }
 
   /**
    * Requests route data from NextBus. If the route data could be parsed,
    * returns the successful Parser. Otherwise, returns null.
    * 
    * @param agency_tag
    * @param route_tag
    * @return
    * @throws IllegalStateException
    */
   private RouteConfigParser getAndParseRoute(String agency_tag, String route_tag) {
     Uri request_uri =
         NextMuniUriBuilder.buildRouteDetailsUri(agency_tag, route_tag);
     return getAndParse(request_uri.toString(), RouteConfigParser.class);
   }
 
   /**
    * Requests a URI from NextBus, parses it with the specified parser, and
    * returns the parser if it succeeded.
    * 
    * @param request_uri
    * @return
    * @throws IllegalStateException
    */
   private <ParserT extends Parser> ParserT getAndParse(String request_uri,
       Class<ParserT> parserT) {
     Log.i("DroidMuni", "Requesting " + request_uri);
     HttpGet dir_request = new HttpGet(request_uri);
     InputStream get_response;
     try {
      DefaultHttpClient mClient = new DefaultHttpClient();

       HttpResponse response = mClient.execute(dir_request);
       // TODO(jyasskin): Figure out how best to guarantee that the
       // response gets closed.
       get_response = response.getEntity().getContent();
     } catch (ClientProtocolException e) {
       Log.e("DroidMuni", "Cannot get " + request_uri, e);
       dir_request.abort();
       return null;
     } catch (IOException e) {
       Log.e("DroidMuni", "Cannot get " + request_uri, e);
       dir_request.abort();
       return null;
     }
 
     ParserT parser;
     try {
       parser = (ParserT) parserT.newInstance();
     } catch (IllegalAccessException e) {
       throw new IllegalArgumentException(
           "Passed " + parserT.getName()
               + " to getAndParse(), without an accessible constructor", e);
     } catch (InstantiationException e) {
       throw new IllegalArgumentException(
           "Passed " + parserT.getName()
               + " to getAndParse(), which cannot be constructed", e);
     }
     parser.parse(get_response);
     switch (parser.getResult()) {
     case SUCCESS:
       return parser;
     case NOT_DONE:
       Log.e("DroidMuni", "Parser didn't finish?!?");
       break;
     case IO_ERROR:
     case PARSE_ERROR:
       Log.e("DroidMuni", "Failed to parse response");
       break;
     }
     return null;
   }
 
   /**
    * Fills in the database with details for the specified route.
    * 
    * @param agency_tag
    *          "sf-muni" (Eventually, maybe, the agency whose route we're
    *          querying for.)
    * @param route
    *          The route to query directions and stops for.
    * @param update_time_on_failure
    *          When getting the route fails, we set its "last update" time back
    *          to this value so the next query will try again.
    */
   private void fillDbForRoute(String agency_tag, Db.Route route) {
     final SQLiteDatabase tables = db.getWritableDatabase();
     tables.beginTransaction();
     try {
       long last_update =
           DatabaseUtils.longForQuery(tables,
               "SELECT last_direction_update_ms FROM Routes WHERE _id == ?",
               new String[] { route.id + "" });
       if (last_update >= System.currentTimeMillis() - ONE_DAY) {
         // Someone else updated it first. Skip the work.
         return;
       }
       RouteConfigParser parser = getAndParseRoute(agency_tag, route.tag);
       if (parser == null) {
         return;
       }
       synchronized (db) {
         for (int i = 0; i < parser.getStops().size(); i++) {
           db.addStop(parser.getStops().valueAt(i), route.id);
         }
       }
 
       final Map<String, Db.Direction> dir_map = parser.getDirections();
       db.setDirections(route.id, dir_map);
 
       // Record that the directions and stops are now up to date.
       ContentValues values = new ContentValues(1);
       values.put("last_direction_update_ms", System.currentTimeMillis());
       tables.update("Routes", values, "_id = ?", new String[] { route.id + "" });
 
       tables.setTransactionSuccessful();
     } finally {
       tables.endTransaction();
     }
   }
 
   /**
    * If our cache is out of date, requeries NextMuni's website for direction and
    * stop data on the_route.
    */
   private void maybeUpdateRouteData(final String agency_tag,
       final Db.Route the_route) {
     final long now = System.currentTimeMillis();
     final long last_directions_update = the_route.directions_updated_ms;
     if (last_directions_update < now - ONE_MONTH) {
       // The data is too old, so block until we can update it.
       fillDbForRoute(agency_tag, the_route);
     } else if (last_directions_update < now - ONE_DAY) {
       // The data is a little stale, so update it in the background, but
       // return quickly with the cached data.
       Globals.EXECUTOR.execute(new Runnable() {
         public void run() {
           fillDbForRoute(agency_tag, the_route);
         }
       });
     }
   }
 
   Cursor queryDirections(final String agency_tag, final String route_tag) {
     final Db.Route the_route = db.getRoute(route_tag);
     maybeUpdateRouteData(agency_tag, the_route);
 
     // Now use the local cache to return the directions list.
     Cursor result =
         db.getReadableDatabase().rawQuery(
             "SELECT Directions._id AS _id, Routes.tag AS route_tag,"
                 + " Directions.tag AS tag, Directions.title AS title"
                 + " FROM Directions INNER JOIN Routes"
                 + " ON (Directions.route_id == Routes._id)"
                 + " WHERE Routes.tag == ? AND use_for_ui != 0"
                 + " ORDER BY Directions.tag ASC", new String[] { route_tag });
     if (result.getCount() == 0) {
       result.close();
       return null;
     }
     return result;
   }
 
   private Cursor queryStops(String agency_tag, String route_tag,
       String direction_tag) {
     final Db.Route the_route = db.getRoute(route_tag);
     maybeUpdateRouteData(agency_tag, the_route);
 
     Cursor result =
         db.getReadableDatabase().rawQuery(
             "SELECT Stops._id AS _id, Routes.tag AS route_tag,"
                 + " Directions.tag AS direction_tag, Stops._id AS stop_id,"
                 + " Stops.title AS title, latitude AS lat, longitude AS lon"
                 + " FROM Routes JOIN Directions"
                 + " ON (Routes._id == Directions.route_id)"
                 + " JOIN DirectionStops"
                 + " ON (Directions._id == DirectionStops.direction)"
                 + " JOIN Stops ON (DirectionStops.stop == Stops._id)"
                 + " WHERE Routes.tag == ? AND Directions.tag == ?"
                 + " ORDER BY stop_order ASC",
             new String[] { route_tag, direction_tag });
     if (result.getCount() == 0) {
       result.close();
       return null;
     }
     return result;
   }
 
   private Cursor queryPredictions(String agency_tag, String stop_id) {
     Uri prediction_uri = null;
     prediction_uri = NextMuniUriBuilder.buildPredictionUri(agency_tag, stop_id);
 
     PredictionsParser parser =
         getAndParse(prediction_uri.toString(), PredictionsParser.class);
     if (parser == null) {
       return null;
     }
 
     List<Db.Prediction> predictions = parser.getPredictions();
     Collections.sort(predictions);
 
     HashMap<String, String> direction_tag2title =
         parser.getDirectionTag2Title();
 
     String[] columns =
         { "_id", "route_tag", "direction_tag", "direction_title", "stop_id",
          "predicted_time" };
     MatrixCursor result = new MatrixCursor(columns);
     int id = 0;
     for (Db.Prediction prediction : predictions) {
       String direction_name = direction_tag2title.get(prediction.direction_tag);
       if (direction_name == null) {
         direction_name = prediction.direction_tag;
       }
 
       MatrixCursor.RowBuilder row = result.newRow();
       row.add(id++);
       row.add(prediction.route_tag);
       row.add(prediction.direction_tag);
       row.add(direction_name);
       row.add(stop_id);
       row.add(prediction.predicted_time);
     }
     return result;
   }
 
   @Override
   public Uri insert(Uri uri, ContentValues values) {
     throw new UnsupportedOperationException("Cannot insert into NextMUNI");
   }
 
   @Override
   public int update(Uri uri, ContentValues values, String selection,
       String[] selectionArgs) {
     throw new UnsupportedOperationException("Cannot update NextMUNI");
   }
 
   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs) {
     throw new UnsupportedOperationException("Cannot delete from NextMUNI");
   }
 }
