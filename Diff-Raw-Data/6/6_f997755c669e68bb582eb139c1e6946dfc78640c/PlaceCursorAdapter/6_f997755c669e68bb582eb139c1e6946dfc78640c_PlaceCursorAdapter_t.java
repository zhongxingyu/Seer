 package edu.vanderbilt.vm.guide.ui.adapter;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 import android.location.Location;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import edu.vanderbilt.vm.guide.R;
 import edu.vanderbilt.vm.guide.db.GuideDBConstants;
 import edu.vanderbilt.vm.guide.util.DBUtils;
 import edu.vanderbilt.vm.guide.util.Geomancer;
 
 public class PlaceCursorAdapter extends BaseAdapter {
 
 	private final Cursor mCursor;
 	private final Context mContext;
 	private int mIdColIx;
 	private int mNameColIx;
 	private int mCatColIx;
 	private int mLatColIx;
 	private int mLngColIx;
 	
 	/**
 	 * Do not call this constructor.
 	 * @throws Exception
 	 */
 	public PlaceCursorAdapter() throws Exception {
 		throw new Exception("Do not call this constructor");
 	}
 
 	public PlaceCursorAdapter(Context context, Cursor cursor) {
 		mContext = context;
 		mCursor = cursor;
 		
 		mIdColIx = mCursor.getColumnIndex(GuideDBConstants.PlaceTable.ID_COL);
 		if(mIdColIx == -1) {
 			throw new SQLiteException("Cursor does not have an id column");
 		}
 		mNameColIx = mCursor.getColumnIndex(GuideDBConstants.PlaceTable.NAME_COL);
 		if(mNameColIx == -1) {
 			throw new SQLiteException("Cursor does not have a name column");
 		}
 		mCatColIx = mCursor.getColumnIndex(GuideDBConstants.PlaceTable.CATEGORY_COL);
 		if(mCatColIx == -1) {
 			throw new SQLiteException("Cursor does not have a category column");
 		}
 		mLatColIx = mCursor.getColumnIndex(GuideDBConstants.PlaceTable.LATITUDE_COL);
 		if(mLatColIx == -1) {
 			throw new SQLiteException("Cursor does not have a latitude column");
 		}
 		mLngColIx = mCursor.getColumnIndex(GuideDBConstants.PlaceTable.LONGITUDE_COL);
 		if(mLngColIx == -1) {
 			throw new SQLiteException("Cursor does not have a longitude column");
 		}
 		
 	}
 
 	@Override
 	public int getCount() {
 		return mCursor.getCount();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		checkPosition(position);
 		mCursor.moveToPosition(position);
 		return DBUtils.getPlaceFromCursor(mCursor);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		checkPosition(position);
 		mCursor.moveToPosition(position);
 		return mCursor.getInt(mIdColIx);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		checkPosition(position);
 		LinearLayout layout;
 		if (convertView == null) {
 			layout = (LinearLayout) LayoutInflater.from(mContext).inflate(
 					R.layout.place_list_item, null);
 			layout.setTag(layout);
 		} else {
 			layout = (LinearLayout) convertView.getTag();
 		}
 		mCursor.moveToPosition(position);
 		((TextView) layout.findViewById(R.id.placelist_item_title))
 			.setText(mCursor.getString(mNameColIx));
 		
 		((ImageView) layout.findViewById(R.id.placelist_item_thunbnail))
 			.setImageResource(R.drawable.home);
 		
 		Location tmp = new Location("Temp");
 		tmp.setLatitude(Double.parseDouble(mCursor.getString(mLatColIx)));
 		tmp.setLongitude(Double.parseDouble(mCursor.getString(mLngColIx)));
 		
		int dist = (int) tmp.distanceTo(Geomancer.getDeviceLocation());
		
 		((TextView) layout.findViewById(R.id.placelist_item_distance))
			.setText(Integer.toString(dist) + " m");
 		
 		return layout;
 	}
 	
 	private void checkPosition(int position) {
 		if (position < 0 || position >= mCursor.getCount()) {
 			throw new IndexOutOfBoundsException("Position " + position
 					+ " is invalid for a cursor with " + getCount() + "rows.");
 		}
 	}
 
 }
