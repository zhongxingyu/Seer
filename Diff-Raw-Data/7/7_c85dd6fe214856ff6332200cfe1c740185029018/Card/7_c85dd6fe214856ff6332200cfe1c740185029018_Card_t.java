 package edu.mit.mobile.android.livingpostcards.data;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.accounts.Account;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import edu.mit.mobile.android.content.DBSortOrder;
 import edu.mit.mobile.android.content.ForeignKeyManager;
 import edu.mit.mobile.android.content.ProviderUtils;
 import edu.mit.mobile.android.content.UriPath;
 import edu.mit.mobile.android.content.column.DBColumn;
 import edu.mit.mobile.android.content.column.IntegerColumn;
 import edu.mit.mobile.android.content.column.TextColumn;
 import edu.mit.mobile.android.locast.data.AbsResourcesSync;
 import edu.mit.mobile.android.locast.data.Authorable;
 import edu.mit.mobile.android.locast.data.JsonSyncableItem;
 import edu.mit.mobile.android.locast.data.Locatable;
 import edu.mit.mobile.android.locast.data.OrderedList.SyncMapItem;
 import edu.mit.mobile.android.locast.data.PrivatelyAuthorable;
import edu.mit.mobile.android.locast.data.SyncMap;
 import edu.mit.mobile.android.locast.data.Titled;
 import edu.mit.mobile.android.locast.net.NetworkProtocolException;
 
 @UriPath(Card.PATH)
 @DBSortOrder(Card.SORT_DEFAULT)
 public class Card extends JsonSyncableItem implements PrivatelyAuthorable.Columns, Titled.Columns,
         Locatable.Columns {
 
     public Card(Cursor c) {
         super(c);
     }
 
     // ///////////////////////////////////////////
     // columns
 
     @DBColumn(type = TextColumn.class, unique = true, notnull = true)
     public static final String COL_UUID = "uuid";
 
     public static final int DEFAULT_TIMING = 300;
 
     /**
      * The amount of time between frames when the card is animated. In millisecond units.
      */
     @DBColumn(type = IntegerColumn.class, notnull = true, defaultValueInt = DEFAULT_TIMING)
     public static final String COL_TIMING = "timing";
 
     @DBColumn(type = TextColumn.class)
     public static final String COL_ANIMATED_RENDER = "anim_render";
 
     @DBColumn(type = TextColumn.class)
     public static final String COL_COVER_PHOTO = "cover_photo";
 
     @DBColumn(type = TextColumn.class)
     public static final String COL_THUMBNAIL = "thumbnail";
 
     @DBColumn(type = TextColumn.class)
     public static final String COL_MEDIA_URL = "media_url";
 
     public static final ForeignKeyManager MEDIA = new ForeignKeyManager(CardMedia.class);
 
     // ////////////////////////////////////////////////////////
 
     /**
      * Creates a new card with a random UUID. Cards are marked DRAFT by default.
      *
      * @param cr
      * @param title
      * @return
      */
     public static Uri createNewCard(Context context, Account account, String title) {
 
         final ContentValues cv = new ContentValues();
 
         cv.put(Card.COL_UUID, java.util.UUID.randomUUID().toString());
 
         cv.put(Card.COL_TITLE, title);
 
         cv.put(Card.COL_DRAFT, true);
 
         Authorable.putAuthorInformation(context, account, cv);
 
         final Uri card = context.getContentResolver().insert(Card.CONTENT_URI, cv);
 
         return card;
     }
 
     public static class CardResources extends AbsResourcesSync {
 
         @Override
         protected void fromResourcesJSON(Context context, Uri localUri, ContentValues cv,
                 JSONObject item) throws NetworkProtocolException, JSONException {
             addToContentValues(cv, "animated_render", item, COL_ANIMATED_RENDER, null, false);
             addToContentValues(cv, "cover_photo", item, COL_COVER_PHOTO, null, false);
             addToContentValues(cv, "thumbnail", item, COL_THUMBNAIL, null, false);
 
         }
     }
 
    @Override
    public SyncMap getSyncMap() {
        return SYNC_MAP;
    }

     public static class ItemSyncMap extends JsonSyncableItem.ItemSyncMap {
         /**
          *
          */
         private static final long serialVersionUID = -1061563089458927973L;
 
         public ItemSyncMap() {
             super();
 
             putAll(Titled.SYNC_MAP);
             putAll(PrivatelyAuthorable.SYNC_MAP);
             putAll(Locatable.SYNC_MAP);
 
             put(COL_TIMING, new SyncFieldMap("frame_delay", SyncFieldMap.INTEGER));
 
             put(COL_UUID, new SyncFieldMap("uuid", SyncFieldMap.STRING));
 
             put("_resources", new CardResources());
 
             put(COL_MEDIA_URL,
                     new SyncChildRelation("photos", new SyncChildRelation.SimpleRelationship(
                             CardMedia.PATH), SyncMapItem.SYNC_FROM));
         }
     };
 
     public static final ItemSyncMap SYNC_MAP = new ItemSyncMap();
 
     public static final String SORT_DEFAULT = COL_CREATED_DATE + " DESC";
 
     public static final String PATH = "card";
 
     public static final Uri CONTENT_URI = ProviderUtils.toContentUri(CardProvider.AUTHORITY, PATH);
 
     @Override
     public Uri getContentUri() {
         return CONTENT_URI;
     }
 }
