 package net.gumbercules.loot;
 
 import java.util.Arrays;
 import java.util.List;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
import android.util.Log;
 
 public class AccountProvider extends ContentProvider
 {
 	public static final Uri CONTENT_URI = Uri.parse("content://net.gumbercules.loot.accountprovider");
 	public static final String ACCOUNT_ITEM_MIME = "vnd.android.cursor.item/vnd.gumbercules.account";
 	public static final String ACCOUNT_DIR_MIME = "vnd.android.cursor.dir/vnd.gumbercules.account";
 
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
 			return ACCOUNT_DIR_MIME;
 		
 		if (Integer.getInteger(object) != null)
 			return ACCOUNT_ITEM_MIME;
 		
 		return null;
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
 			sortOrder = "priority asc";
 			
 		List<String> path = uri.getPathSegments();
		String query = "select " + Arrays.toString(projection).replaceAll("[\\[\\]]", "") +
				" from accounts where ";
 		if (type.equals(ACCOUNT_ITEM_MIME))
 		{
			query += "id = " + path.get(0) + " and " + selection +
					" order by " + sortOrder;
 		}
 		else if (type.equals(ACCOUNT_DIR_MIME))
 		{
			query += selection + " order by " + sortOrder;
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
