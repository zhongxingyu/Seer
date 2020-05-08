 package net.gumbercules.loot;
 
 import java.util.Arrays;
 import java.util.List;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 
 public class TransactionProvider extends ContentProvider
 {
 	public static final Uri CONTENT_URI = Uri.parse("content://net.gumbercules.loot.transactionprovider");
 	public static final String TRANSACTION_ITEM_MIME = "vnd.android.cursor.item/vnd.gumbercules.transaction";
 	public static final String TRANSACTION_DIR_MIME = "vnd.android.cursor.dir/vnd.gumbercules.transaction";
 	public static final String INTEGER_ITEM_MIME = "vnd.android.cursor.item/vnd.gumbercules.integer";
 	public static final String TAG_DIR_MIME = "vnd.android.cursor.dir/vnd.gumbercules.tag";
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs)
 	{
 		return 0;
 	}
 
 	@Override
 	public String getType(Uri uri)
 	{
 		if (!uri.getScheme().equals("content"))
 			return null;
 		
 		List<String> path = uri.getPathSegments();
 		int size = path.size();
 		
 		String object = null;
 		if (size > 0)
 			object = path.get(0);
 		else
 			return null;
 		
 		boolean tag = false;
 		if (object.equals("transaction"))
 			tag = false;
 		else if (object.equals("tag"))
 			tag = true;
 		else
 			return null;
 		
 		if (size == 2)
 		{
 			object = path.get(1);
 			if (Integer.getInteger(object) != null)
 			{
 				if (tag)
 					return null;
 				else
 					return TRANSACTION_ITEM_MIME;
 			}
 			
 			if (object.equals("count"))
 				return INTEGER_ITEM_MIME;
 			else
 				return null;
 		}
 		
 		return (tag ? TAG_DIR_MIME : TRANSACTION_DIR_MIME);
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values)
 	{
 		return null;
 	}
 
 	@Override
 	public boolean onCreate()
 	{
 		return false;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder)
 	{
 		SQLiteDatabase lootDb = Database.getDatabase();
 		String type = getType(uri);
 		
 		if (type == null)
 			return null;
 		
 		if (projection == null)
 			projection = new String[] {"*"};
 			
 		if (selection == null)
 			selection = "1=1";
 		
 		if (sortOrder == null)
 			sortOrder = "id asc";
 			
 		List<String> path = uri.getPathSegments();
		String query = null;
 		if (type.equals(TRANSACTION_ITEM_MIME))
 		{
			query = "select " + Arrays.toString(projection) + " from transaction, tags " +
					"where id = " + path.get(2) + " and trans_id = id and " + 
 					selection + " order by " + sortOrder;
 		}
 		else if (type.equals(TRANSACTION_DIR_MIME))
 		{
			query = "select " + Arrays.toString(projection) + " from transaction, tags " +
					"where trans_id = id and " + selection + " order by " + sortOrder;
 		}
 		else if (type.equals(TAG_DIR_MIME))
 		{
 			if (sortOrder.equals("id asc"))
 				sortOrder = "name asc";
 			
			query = "select " + Arrays.toString(projection) + " from tags " +
					"where " + selection + " order by " + sortOrder; 
 		}
 		else if (type.equals(INTEGER_ITEM_MIME))
 		{
 			String table = path.get(1);
 			if (table.equals("tag"))
 				table = "tags";
 			
 			query = "select count(*) from " + table + " where " + selection;
 		}
 		
 		return lootDb.rawQuery(query, null);
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs)
 	{
 		return 0;
 	}
 
 }
