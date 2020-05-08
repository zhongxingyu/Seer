 package com.dnsalias.sanja.simplecarbocalc;
 
 import android.app.SearchManager;
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.util.Log;
 
 public class SimpleCarboCalcSuggestionProvider extends ContentProvider
 {
 	private static final String LOGTAG = "SimpleCarboCalcProvider";
 	
 	public static String AUTHORITY= "com.dnsalias.sanja.SimpleCarboCalc";
 	public static final Uri CONTENT_URI= Uri.parse("content://" + AUTHORITY + "/products");
 	
 	private ProdListOpenHelper mDB;
 	
 	private static final int SEARCH_SUGGEST= 1;
 	private static final UriMatcher sURIMatcher = buildUriMatcher();
 	
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs)
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public String getType(Uri uri)
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values)
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean onCreate()
 	{
 		mDB= new ProdListOpenHelper(getContext());
 		Log.v(LOGTAG, "inited");
 		return true;
 	}
 
 	private static UriMatcher buildUriMatcher()
 	{
 		UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
 		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
         matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
         return matcher;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder)
 	{
 		switch (sURIMatcher.match(uri))
 		{
 		case SEARCH_SUGGEST:
 			Log.v(LOGTAG, "SEARCH_SUGGEST " + (selectionArgs[0] == null ? "<NULL>" : selectionArgs[0]));
 			return getSuggestions(selectionArgs[0]);
 		}
 		return null;
 	}
 
 	private Cursor getSuggestions(String query)
 	{
 		if (query != null && query.length() == 0)
 			query= null;
 		String[] fields = new String[] {
 				ProdList.PROD__ID,
 				ProdList.PROD_NAME + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1,
 				ProdList.PROD__ID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};
 		SQLiteDatabase db = mDB.getReadableDatabase();
 		
 		Cursor res= db.query(ProdList.PRODLIST_TABLE_NAME, fields, (query == null ? null : ProdList.PROD_NAMES + " MATCH ?"),
 				(query == null ? null : new String[] { query.toLowerCase() + "*" }),
 				null, null, null);
 		return res;
 	}
 	
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs)
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
