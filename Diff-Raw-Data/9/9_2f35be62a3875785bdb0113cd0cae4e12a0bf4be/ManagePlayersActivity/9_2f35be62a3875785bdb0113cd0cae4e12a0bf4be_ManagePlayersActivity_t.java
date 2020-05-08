 package org.cniska.foosball.android;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ManagePlayersActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
 
     private static final String TAG = "ManagePlayersActivity";
 
     // Inner classes
     // ----------------------------------------
 
     private static class PlayerAdapter extends SimpleAdapter {
 
         private static final String TAG = "ManagePlayersActivity.PlayerAdapter";
 
 		private Context mContext;
 
         /**
          * Creates a new adapter.
          * @param context The context.
          * @param data A list of maps.
          */
         public PlayerAdapter(Context context, ArrayList<HashMap<String, String>> data) {
             super(context, data, R.layout.manage_players_item,
 					new String[] { COLUMN_ID, COLUMN_NAME },
 					new int[] { R.id.column_id, R.id.column_name });
 
 			mContext = context;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View view = super.getView(position, convertView, parent);
 
 			ImageButton deleteButton = (ImageButton) view.findViewById(R.id.button_delete);
 			deleteButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View view) {
					View parent = (View) view.getParent().getParent();
 					TextView idColumn = (TextView) parent.findViewById(R.id.column_id);
 					final String id = (String) idColumn.getText();
 
 					// Ask the user to confirm the delete.
 					new AlertDialog.Builder(mContext)
 							.setMessage(R.string.dialog_message_delete)
 							.setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									mContext.getContentResolver().delete(
 											Uri.withAppendedPath(DataContract.Players.CONTENT_URI, id),
 											null, null);
 
 									// Show a toast to the user telling him that the player has been deleted.
 									Toast.makeText(mContext, R.string.toast_player_deleted, 1000).show();
 								}
 							})
 							.setNegativeButton(R.string.dialog_button_no, new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									dialog.cancel();
 								}
 							})
 							.create()
 							.show();
 				}
 			});
 
             return view;
         }
 
 
 	}
 
 	private class Data {
 		public long id;
 		public String name;
 	}
 
     // Static variables
     // ----------------------------------------
 
 	private static final String COLUMN_ID = "id";
 	private static final String COLUMN_NAME = "name";
 
     // Member variables
     // ----------------------------------------
 
     private PlayerAdapter mAdapter;
     private ListView mListView;
     private ArrayList<Data> mData;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		setHomeButtonEnabled(true);
 		setActionBarTitle(R.string.title_manage_players);
         setContentView(R.layout.manage_players);
         mListView = (ListView) findViewById(R.id.manage_players_list);
 		TextView emptyView = (TextView) findViewById(R.id.manage_players_list_empty);
 		mListView.setEmptyView(emptyView);
         getSupportLoaderManager().initLoader(0, null, this);
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.manage_players, menu);
 		return true;
 	}
 
     @Override
     public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
         return new CursorLoader(getApplicationContext(), DataContract.Players.CONTENT_URI,
 				new String[] { DataContract.Players._ID, DataContract.Players.NAME },
 				null, null, "_id ASC");
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
 		if (cursor != null) {
 			mData = new ArrayList<Data>(cursor.getCount());
 
 			if (cursor.moveToFirst()) {
 				while (!cursor.isAfterLast()) {
 					Data item = new Data();
 					item.id = cursor.getLong(cursor.getColumnIndex(DataContract.Players._ID));
 					item.name = cursor.getString(cursor.getColumnIndex(DataContract.Players.NAME));
 					mData.add(item);
 					cursor.moveToNext();
 				}
 			}
 
 			updateListView();
 		}
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> cursorLoader) {
     }
 
     /**
      * Updates the list view by rebuilding the data for the adapter.
      */
     private void updateListView() {
         ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
 
         for (int i = 0, len = mData.size(); i < len; i++) {
             Data item = mData.get(i);
             HashMap<String, String> map = new HashMap<String, String>();
             map.put(COLUMN_ID, String.valueOf(item.id));
             map.put(COLUMN_NAME, item.name);
             data.add(map);
         }
 
         mAdapter = new PlayerAdapter(this, data);
         mListView.setAdapter(mAdapter);
     }
 }
