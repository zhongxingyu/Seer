 package fr.itinerennes.database;
 
 import java.util.List;
 
 import org.osmdroid.util.BoundingBoxE6;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.SearchManager;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.provider.BaseColumns;
 
 import fr.itinerennes.R;
 import fr.itinerennes.TypeConstants;
 import fr.itinerennes.database.Columns.BookmarksColumns;
 import fr.itinerennes.database.Columns.MarkersColumns;
 
 /**
  * Fetch markers from the database.
  * 
  * @author Olivier Boudet
  */
 public class MarkerDao implements MarkersColumns {
 
     /** The event logger. */
     private static final Logger LOGGER = LoggerFactory.getLogger(MarkerDao.class);
 
     /** The context. */
     private final Context context;
 
     /** The database helper. */
     private final DatabaseHelper dbHelper;
 
     /** SQL query used to fetch suggestions from the database. */
     private String getSuggestionsStatement;
 
     /** Intent data id used when the line "search an address" is clicked in suggestions. */
     public static final String NOMINATIM_INTENT_DATA_ID = "nominatim";
 
     /**
      * Constructor.
      * 
      * @param databaseHelper
      *            the itinerennes context
      */
     public MarkerDao(final Context context, final DatabaseHelper databaseHelper) {
 
         this.context = context;
         dbHelper = databaseHelper;
 
     }
 
     /**
      * Fetch markers contained in the given bounding box from the database.
      * 
      * @param bbox
      *            the bounding box in which fetch markers
      * @param types
      *            the types of markers to retrieve
      * @return the list of Marker
      */
     public final Cursor getMarkers(final BoundingBoxE6 bbox, final List<String> types) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarkers.start - bbox={}", bbox);
         }
 
         if (types.size() == 0) {
             // no visible marker type is selected, we return nothing at all
             return null;
         }
 
         final String tables = String.format("%s m left join %s b on m.%s=b.%s", MARKERS_TABLE_NAME,
                 Columns.BookmarksColumns.BOOKMARKS_TABLE_NAME, ID, Columns.BookmarksColumns.ID);
 
         final String[] columns = new String[] { String.format("m.%s", ID),
                 String.format("m.%s", TYPE), String.format("m.%s", LABEL), LONGITUDE, LATITUDE,
                 String.format("b.%s is not null  AS bookmarked", ID) };
 
         final StringBuilder selection = new StringBuilder();
 
         selection.append(String.format("%s >= ? AND %s <= ? AND %s >= ? AND %s <= ?", LONGITUDE,
                 LONGITUDE, LATITUDE, LATITUDE));
 
         selection.append(" AND ( ");
         // filter on visible types
         for (int i = 0; i < types.size(); i++) {
             selection.append(String.format(" %s m.%s = '%s'", (i > 0) ? "OR" : "", TYPE,
                     types.get(i)));
         }
         selection.append(")");
 
         final String[] selectionArgs = new String[] { String.valueOf(bbox.getLonWestE6()),
                 String.valueOf(bbox.getLonEastE6()), String.valueOf(bbox.getLatSouthE6()),
                 String.valueOf(bbox.getLatNorthE6()) };
 
         final Cursor c = query(tables, selection.toString(), selectionArgs, columns, null,
                 "m._id ASC");
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarkers.end - count={}", (c != null) ? c.getCount() : 0);
         }
         return c;
     }
 
     /**
      * Fetch a single MarkerOverlayItem from the database based on an unique android id.
      * 
      * @param id
      *            unique id of the marker
      * @return the Marker
      */
     public final Cursor getMarker(final String id) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarker.start - id={}", id);
         }
 
         final String[] columns = new String[] { String.format("m.%s", ID),
                 String.format("m.%s", TYPE), String.format("m.%s", LABEL), LONGITUDE, LATITUDE,
                 String.format("b.%s is not null  AS bookmarked", ID) };
 
         final String selection = String.format("m.%s = ?", BaseColumns._ID);
 
         final String[] selectionArgs = new String[] { id };
 
         final Cursor c = query(selection, selectionArgs, columns);
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarkers.end - count={}", (c != null) ? c.getCount() : 0);
         }
         return c;
     }
 
     /**
      * Fetch a single MarkerOverlayItem from the database based on a stop id and his type.
      * 
      * @param id
      *            id of the marker
      * @param type
      *            type of the marker
      * @return the Marker
      */
     public final Cursor getMarker(final String id, final String type) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarker.start - id={}, type={}", id, type);
         }
 
         final String[] columns = new String[] { String.format("m.%s", ID),
                 String.format("m.%s", TYPE), String.format("m.%s", LABEL), LONGITUDE, LATITUDE,
                 String.format("b.%s is not null AS bookmarked", ID) };
 
         final String selection = String.format("m.%s = ?", ID);
 
         final String[] selectionArgs = new String[] { id };
 
         final Cursor c = query(selection, selectionArgs, columns);
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarkers.end - count={}", (c != null) ? c.getCount() : 0);
         }
 
         return c;
     }
 
     /**
      * Fetch all markers from the database having the same label than the marker identified by its
      * unique android id .
      * 
      * @param id
      *            unique id of the marker
      * @return all markers with same label
      */
     public final Cursor getMarkersWithSameLabel(final String id) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarkersWithSameLabel.start - id={}", id);
         }
 
         final String[] selectionArgs = new String[] { id, id };
 
         final Cursor c = dbHelper
                 .getReadableDatabase()
                 .rawQuery(
                         String.format(
                                 "SELECT * FROM %s WHERE label=(SELECT %s FROM %s where %s = ?) AND type=(SELECT %s FROM %s WHERE %s = ?)",
                                 MarkersColumns.MARKERS_TABLE_NAME, MarkersColumns.LABEL,
                                 MarkersColumns.MARKERS_TABLE_NAME, MarkersColumns._ID,
                                 MarkersColumns.TYPE, MarkersColumns.MARKERS_TABLE_NAME,
                                 MarkersColumns._ID), selectionArgs);
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarkersWithSameLabel.end - count={}", (c != null) ? c.getCount() : 0);
         }
         return c;
     }
 
     /**
      * Search markers containing the given string. This method returns only one row with same label
      * and same type.
      * 
      * @param query
      *            string to search in markers' label
      * @return search results
      */
     public final Cursor searchMarkers(final String query) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("searchMarkers.start - query={}", query);
         }
 
         final String selection = String.format("%s LIKE ?", Columns.MarkersColumns.LABEL);
         final String[] columns = new String[] { BaseColumns._ID, Columns.MarkersColumns.ID,
                 Columns.MarkersColumns.TYPE, Columns.MarkersColumns.LABEL,
                 Columns.MarkersColumns.LONGITUDE, Columns.MarkersColumns.LATITUDE };
 
         final String[] selectionArgs = new String[] { "%" + query + "%" };
 
         final String groupBy = String.format("%s, %s", Columns.MarkersColumns.LABEL,
                 Columns.MarkersColumns.TYPE);
 
         final Cursor c = query(MARKERS_TABLE_NAME, selection, selectionArgs, columns, groupBy, null);
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("searchMarkers.end - query={}");
         }
         return c;
     }
 
     /**
      * Fetches suggestions for searches. All markers containing the query in the label will be
      * fetched.
      * 
      * @param query
      *            string to search in markers' label
      * @return search results
      */
     public final Cursor getSuggestions(final String query) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getSuggestions.start - query={}", query);
         }
 
         final String[] selectionArgs = new String[] { "%" + query + "%" };
 
         if (getSuggestionsStatement == null) {
 
             getSuggestionsStatement = buildSuggestionsQuery();
         }
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug(String.format("Marker query : %s", getSuggestionsStatement));
         }
 
         final Cursor c = dbHelper.getReadableDatabase().rawQuery(getSuggestionsStatement,
                 selectionArgs);
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getSuggestions.end");
         }
         return c;
     }
 
     /**
      * This method creates a select statement used to fetch suggestions data from the database.
      * 
      * @return string representing the sql select statement.
      */
     private String buildSuggestionsQuery() {
 
         final StringBuffer sql = new StringBuffer();
 
         sql.append(String.format("SELECT 'B', m.%s as %s,", BaseColumns._ID, BaseColumns._ID));
 
         // building select clause part for the suggest_icon_id column
         sql.append(String.format(" CASE WHEN m.%s = '%s' THEN '%s'", Columns.MarkersColumns.TYPE,
                 TypeConstants.TYPE_BUS, R.drawable.ic_mapbox_bus));
         sql.append(String.format(" WHEN m.%s = '%s' THEN '%s'", Columns.MarkersColumns.TYPE,
                 TypeConstants.TYPE_BIKE, R.drawable.ic_mapbox_bike));
         sql.append(String.format(" WHEN m.%s = '%s' THEN '%s'", Columns.MarkersColumns.TYPE,
                 TypeConstants.TYPE_SUBWAY, R.drawable.ic_mapbox_subway));
         sql.append(String.format(" END AS %s,", SearchManager.SUGGEST_COLUMN_ICON_1));
 
         sql.append(String.format(" m.%s AS %s,", Columns.MarkersColumns.LABEL,
                 SearchManager.SUGGEST_COLUMN_TEXT_1));
         sql.append(String.format(" m.%s AS %s", BaseColumns._ID,
                 SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID));
 
         sql.append(String.format(" ,CASE WHEN b.%s is not null = 1 THEN '%s' END AS %s", ID,
                 android.R.drawable.btn_star_big_on, SearchManager.SUGGEST_COLUMN_ICON_2));
 
         sql.append(String.format(" FROM %s m LEFT JOIN %s b ON m.%s=b.%s", MARKERS_TABLE_NAME,
                 BookmarksColumns.BOOKMARKS_TABLE_NAME, ID, BookmarksColumns.ID));
 
         sql.append(String.format(" WHERE m.%s LIKE ?", Columns.MarkersColumns.LABEL));
 
         sql.append(" GROUP BY suggest_text_1, m.type, suggest_icon_2");
 
         sql.append(String.format(
                 " UNION ALL select 'A', 'nominatim','%s' as %s,'%s','%s' as %s,''",
                 R.drawable.ic_osm, SearchManager.SUGGEST_COLUMN_ICON_1, context.getResources()
                         .getString(R.string.search_address), NOMINATIM_INTENT_DATA_ID,
                 SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID));
 
         sql.append(String.format(" ORDER BY 1,m.%s", Columns.MarkersColumns.LABEL));
 
         return sql.toString();
     }
 
     /**
      * Queries the database.
      * 
      * @param selection
      *            the where clause
      * @param selectionArgs
      *            parameters for the selection
      * @param columns
      *            columns to retrieve
      * @return results
      */
     private Cursor query(final String selection, final String[] selectionArgs,
             final String[] columns) {
 
         final String tables = String.format("%s m left join %s b on m.%s=b.%s", MARKERS_TABLE_NAME,
                 Columns.BookmarksColumns.BOOKMARKS_TABLE_NAME, ID, Columns.BookmarksColumns.ID);
 
         return query(tables, selection, selectionArgs, columns, null, null);
 
     }
 
     /**
      * Queries the database.
      * 
      * @param tables
      *            the from clause
      * @param selection
      *            the where clause
      * @param selectionArgs
      *            parameters for the selection
      * @param columns
      *            columns to retrieve
      * @param orderBy
      *            order by clause
      * @return results
      */
     private Cursor query(final String tables, final String selection, final String[] selectionArgs,
             final String[] columns, final String groupBy, final String orderBy) {
 
         final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
 
         builder.setTables(tables);
 
        // if (LOGGER.isDebugEnabled()) {
        // LOGGER.debug(String.format("Marker query : %s", builder.buildQuery(columns, selection,
        // selectionArgs, null, null, orderBy, null)));
        // }
 
         final Cursor c = builder.query(dbHelper.getReadableDatabase(), columns, selection,
                 selectionArgs, groupBy, null, orderBy);
 
         if (c == null) {
             return null;
         } else if (!c.moveToFirst()) {
             c.close();
             return null;
         }
         return c;
 
     }
 
 }
