 package net.trajano.gasprices;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListAdapter;
 import android.widget.TextView;
 
 public class CityListAdapter extends BaseAdapter implements ListAdapter {
 	private final String[] cityList;
 	private final int[] cityListIds;
 	private final LayoutInflater inflater;
 
 	public CityListAdapter(final ListActivity activity) {
		super();
 		inflater = (LayoutInflater) activity
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		cityList = activity.getResources().getStringArray(R.array.city_list);
 		cityListIds = new int[cityList.length];
 		for (int i = cityList.length - 1; i >= 0; --i) {
 			final int equalIndex = cityList[i].indexOf('=');
 			Log.d("GasPrices", "processing " + cityList[i]);
 			if (equalIndex == -1) {
 				cityListIds[i] = -1;
 			} else {
 				cityListIds[i] = Integer.valueOf(cityList[i]
 						.substring(equalIndex + 1));
 				cityList[i] = cityList[i].substring(0, equalIndex);
 			}
 		}
 	}
 
 	@Override
 	public int getCount() {
 		return cityList.length;
 	}
 
 	@Override
 	public Object getItem(final int position) {
 		return cityList[position];
 	}
 
 	@Override
 	public long getItemId(final int position) {
 		return cityListIds[position];
 	}
 
 	@Override
 	public int getItemViewType(final int position) {
 		if (isEnabled(position)) {
 			return 0;
 		} else {
 			return 1;
 		}
 	}
 
 	@Override
 	public View getView(final int position, final View convertView,
 			final ViewGroup parent) {
 		final TextView view;
 		if (convertView != null) {
 			view = (TextView) convertView;
 		} else if (isEnabled(position)) {
 			view = (TextView) inflater.inflate(
 					android.R.layout.simple_expandable_list_item_1, parent,
 					false);
 		} else {
 			view = (TextView) inflater.inflate(
 					android.R.layout.simple_list_item_1, parent, false);
 		}
 		view.setText((String) getItem(position));
 		return view;
 	}
 
 	@Override
 	public int getViewTypeCount() {
 		return 2;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return true;
 	}
 
 	@Override
 	public boolean isEnabled(final int position) {
 		return cityListIds[position] != -1;
 	}
 
 }
