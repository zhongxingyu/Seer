 package com.vanderbilt.people.finder;
 
 import java.util.List;
 
 import com.vanderbilt.people.finder.Provider.Constants;
 
 import android.accounts.Account;
 import android.content.AbstractThreadedSyncAdapter;
 import android.content.ContentProviderClient;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SyncResult;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 
 public class SyncAdapter extends AbstractThreadedSyncAdapter
 {
 	private final Context _context;
 	
 	private static final String TAG = "SyncAdapter";
 	
 	public SyncAdapter(Context context, boolean autoInitialize)
 	{
 		super(context, autoInitialize);
 		_context = context;
 	}
 
 	@Override
 	public void onPerformSync(Account account, Bundle extras, String authority,
 			ContentProviderClient provider, SyncResult syncResult)
 	{	
		Cursor c = _context.getContentResolver().query(Constants.CONTENT_URI,
 					new String[] { Constants.NAME, Constants.MESSAGE, Constants.SERVER_KEY,
 								   Constants.LONGITUDE, Constants.LATITUDE },
 					Constants.SERVER_KEY+"="+UserData.getId(_context), null, null);
 		
 		DataModel dataToSend = null;
		if (c.moveToFirst())
 		{
			dataToSend = new DataModel(c.getLong(c.getColumnIndex(Constants.SERVER_KEY)));
			dataToSend.setName(c.getString(c.getColumnIndex(Constants.NAME)));
			dataToSend.setStatus(c.getString(c.getColumnIndex(Constants.MESSAGE)));
			dataToSend.setLatitude(c.getDouble(c.getColumnIndex(Constants.LATITUDE)));
			dataToSend.setLongitude(c.getDouble(c.getColumnIndex(Constants.LONGITUDE)));
 		}
 		
 		// Send dirty records to server while receiving updates
 		Long returnedKey = NetworkUtilities.pushClientStatus(dataToSend);
 		Log.v(TAG, UserData.getId(_context) + " returned " + returnedKey);
 		List<DataModel> returnedItems = NetworkUtilities.getPeerUpdates(UserData.getId(_context));
 		
 		for (DataModel d : returnedItems)
 		{
 			
			c = _context.getContentResolver().query(Constants.CONTENT_URI,
 									   new String[] { Constants.SERVER_KEY },
 									   Constants.SERVER_KEY+"="+d.getKey(), null, null);
 			
 			ContentValues cv = d.toContentValues();
 			if (c.getCount() == 0)
 			{
 				Uri uri = _context.getContentResolver().insert(Constants.CONTENT_URI, cv);
 				Log.v(TAG, "Inserted: " + uri.toString());
 			}
 			else if (d.isMarkedRemoved())
 			{
 				int i = _context.getContentResolver().delete(Constants.CONTENT_URI,
 													Constants.SERVER_KEY+"="+d.getKey(), null);
 				Log.v(TAG, "Deleted " + i + "item(s).");
 			}
 			else
 			{
 				int i = _context.getContentResolver().update(Constants.CONTENT_URI, cv,
 													Constants.SERVER_KEY+"="+d.getKey(), null);
 				Log.v(TAG, "Updated " + i + "item(s).");
 			}
 		}
		
		c.close();
 	}
 }
