 package edu.mit.mobile.android.livingpostcards.data;
 
 import android.accounts.Account;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import edu.mit.mobile.android.content.DBSortOrder;
 import edu.mit.mobile.android.content.ProviderUtils;
 import edu.mit.mobile.android.content.UriPath;
 import edu.mit.mobile.android.content.column.DBColumn;
 import edu.mit.mobile.android.content.column.DBForeignKeyColumn;
 import edu.mit.mobile.android.content.column.TextColumn;
 import edu.mit.mobile.android.locast.data.Authorable;
 import edu.mit.mobile.android.locast.data.ImageContent;
 import edu.mit.mobile.android.locast.data.MediaProcessingException;
 
 @UriPath(CardMedia.PATH)
 @DBSortOrder(CardMedia.SORT_DEFAULT)
 public class CardMedia extends ImageContent implements Authorable.Columns {
 
     public CardMedia(Cursor c) {
         super(c);
     }
 
     public static final String TYPE_DIR = "vnd.android.cursor.dir/vnd.edu.mit.mobile.android.livingpostcards.card.#.media";
     public static final String TYPE_ITEM = "vnd.android.cursor.dir/vnd.edu.mit.mobile.android.livingpostcards.card.#.media";
 
     @DBColumn(type = TextColumn.class, unique = true, notnull = true)
     public static final String COL_UUID = "uuid";
 
     @DBForeignKeyColumn(parent = Card.class)
     public static final String COL_CARD = "card";
 
     public static final Uri getCard(Uri cardMedia) {
         if (cardMedia.getLastPathSegment().matches("\\d+")) {
             return ProviderUtils.removeLastPathSegments(cardMedia, 2);
         } else {
             return ProviderUtils.removeLastPathSegment(cardMedia);
         }
     }
 
     public static final String SORT_DEFAULT = COL_CREATED_DATE + " ASC";
 
     public static final String PATH = "media";
 
 
     /**
      * Creates a new card with a random UUID
      *
      * @param cr
      * @param title
      * @return
      */
     public static ContentValues createNewCardMedia(Context context, Account account) {
 
         final ContentValues cv = new ContentValues();
 
         cv.put(CardMedia.COL_UUID, java.util.UUID.randomUUID().toString());
 
         Authorable.putAuthorInformation(context, account, cv);
 
         return cv;
     }
 
     public static CastMediaInfo addMediaToCard(Context context, Account account, Uri cardMedia,
             Uri content) throws MediaProcessingException {
         final ContentValues cv = CardMedia.createNewCardMedia(context, account);
 
         final CastMediaInfo cmi = CardMedia.addMedia(context, account, cardMedia, content, cv);
 
         return cmi;
     }
 
     public static class ItemSyncMap extends ImageContent.ItemSyncMap {
         /**
          *
          */
         private static final long serialVersionUID = 6879723724633413905L;
 
         public ItemSyncMap() {
             super();
 
             putAll(Authorable.SYNC_MAP);
             put(COL_UUID, new SyncFieldMap("uuid", SyncFieldMap.STRING));
             put("_title", new SyncLiteral("title", "untitled"));
         }
     };
 
     public static final ItemSyncMap SYNC_MAP = new ItemSyncMap();
 
     @Override
     public Uri getContentUri() {
         return Card.MEDIA.getUri(ContentUris.withAppendedId(Card.CONTENT_URI,
                 getLong(getColumnIndexOrThrow(COL_CARD))));
     }
 }
