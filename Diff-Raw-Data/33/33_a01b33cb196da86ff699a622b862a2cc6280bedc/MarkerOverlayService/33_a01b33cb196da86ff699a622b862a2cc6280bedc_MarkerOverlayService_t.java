 package fr.itinerennes.business.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.osmdroid.util.BoundingBoxE6;
 import org.osmdroid.util.GeoPoint;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 
 import fr.itinerennes.database.Columns;
 import fr.itinerennes.database.Columns.MarkersColumns;
 import fr.itinerennes.database.DatabaseHelper;
 import fr.itinerennes.ui.views.overlays.MarkerOverlayItem;
 import fr.itinerennes.utils.ResourceResolver;
 
 /**
  * Fetch markers from the database.
  * 
  * @author Olivier Boudet
  */
 public class MarkerOverlayService extends AbstractService implements MarkersColumns {
 
     /** The event logger. */
     private static final Logger LOGGER = LoggerFactory.getLogger(MarkerOverlayService.class);
 
     /** The activity context. */
     private final Context context;
 
     /**
      * Constructor.
      * 
      * @param context
      *            the activity context
      * @param dbHelper
      *            The Database Helper
      */
     public MarkerOverlayService(final Context context, final DatabaseHelper dbHelper) {
 
         super(dbHelper);
         this.context = context;
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
     public final List<MarkerOverlayItem> getMarkers(final BoundingBoxE6 bbox,
             final List<String> types) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarkers.start - bbox={}", bbox);
         }
 
         if (types.size() == 0) {
             // no visible marker type is selected, we return nothing at all
             return null;
         }
 
         final SQLiteDatabase database = dbHelper.getReadableDatabase();
 
         final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
 
         builder.setTables(String.format("%s m left join %s b on m.%s=b.%s", MARKERS_TABLE_NAME,
                 Columns.BookmarksColumns.BOOKMARKS_TABLE_NAME, ID, Columns.BookmarksColumns.ID));
 
         final String[] columns = new String[] { String.format("m.%s", ID),
                 String.format("m.%s", TYPE), String.format("m.%s", LABEL), LONGITUDE, LATITUDE,
                 String.format("b.%s is not null", ID) };
 
         final String where = String.format("%s >= ? AND %s <= ? AND %s >= ? AND %s <= ?",
                 LONGITUDE, LONGITUDE, LATITUDE, LATITUDE);
 
         // filter on visible types
         for (int i = 0; i < types.size(); i++) {
             builder.appendWhere(String.format(" %s m.%s = '%s'", (i > 0) ? "OR" : "", TYPE,
                     types.get(i)));
         }
 
         final String[] selectionArgs = new String[] { String.valueOf(bbox.getLonWestE6()),
                 String.valueOf(bbox.getLonEastE6()), String.valueOf(bbox.getLatSouthE6()),
                 String.valueOf(bbox.getLatNorthE6()) };
 
         final Cursor c = builder.query(database, columns, where, selectionArgs, null, null, null);
 
         final List<MarkerOverlayItem> markers = new ArrayList<MarkerOverlayItem>();
         while (c.moveToNext()) {
             final MarkerOverlayItem marker = new MarkerOverlayItem();
             marker.setId(c.getString(0));
             marker.setType(c.getString(1));
             marker.setLabel(c.getString(2));
             marker.setLocation(new GeoPoint(c.getInt(4), c.getInt(3)));
             marker.setBookmarked((c.getInt(5) != 0));
             // TJHU set the default marker resource identifier
             final int iconId = ResourceResolver.getDrawableId(context,
                    String.format("icx_marker_%s", marker.getType()), 0);
             marker.setIcon(context.getResources().getDrawable(iconId));
             markers.add(marker);
         }
 
         c.close();
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarkers.end - markers={}", markers);
         }
         return markers;
     }
 
     /**
      * Fetch a single MarkerOverlayItem from the database.
      * 
      * @param id
      *            if of the marker
      * @return the Marker
      */
     public final MarkerOverlayItem getMarker(final String id) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarker.start - id={}", id);
         }
         final SQLiteDatabase database = dbHelper.getReadableDatabase();
 
         final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
 
         builder.setTables(String.format("%s m left join %s b on m.%s=b.%s", MARKERS_TABLE_NAME,
                 Columns.BookmarksColumns.BOOKMARKS_TABLE_NAME, ID, Columns.BookmarksColumns.ID));
 
         final String[] columns = new String[] { String.format("m.%s", ID),
                 String.format("m.%s", TYPE), String.format("m.%s", LABEL), LONGITUDE, LATITUDE,
                 String.format("b.%s is not null", ID) };
 
         final String where = String.format("m.%s = ?", ID);
 
         final String[] selectionArgs = new String[] { id };
 
         final Cursor c = builder.query(database, columns, where, selectionArgs, null, null, null);
 
         MarkerOverlayItem marker = null;
         if (c.moveToNext()) {
             marker = new MarkerOverlayItem();
             marker.setId(c.getString(0));
             marker.setType(c.getString(1));
             marker.setLabel(c.getString(2));
             marker.setLocation(new GeoPoint(c.getInt(4), c.getInt(3)));
             marker.setBookmarked((c.getInt(5) != 0));
             // TJHU set the default marker resource identifier
             final int iconId = ResourceResolver.getDrawableId(context,
                    String.format("icx_marker_%s", marker.getType()), 0);
             marker.setIcon(context.getResources().getDrawable(iconId));
         }
         c.close();
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getMarkers.end - marker={}", marker);
         }
         return marker;
     }
 }
