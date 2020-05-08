 package com.example.phat_am;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class SampleListFragment extends SherlockListFragment {
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.list, null);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		List<Item> items = new ArrayList<Item>();
 		String category = getResources().getString(R.string.category_title);
 		String[] list_category_array  = getResources().getStringArray(R.array.list_category_array);
 		final Integer[] Icons_category = {
 											R.drawable.ic_menu_category,
 											R.drawable.ic_menu_top,
 											R.drawable.ic_menu_new,
 											R.drawable.ic_menu_random,
 											R.drawable.ic_menu_people,
 											R.drawable.ic_menu_contact
 					
 											};
 		//Start to add category to list view navigation
 		items.add(new Header(category));
 		for (int i =0; i< list_category_array.length; i++)
 		{
			items.add(new ListItem(list_category_array[i], Icons_category[i], getActivity()));
 		}
 		
 		String setting = getResources().getString(R.string.setting_title);
 		String[] list_setting_array  = getResources().getStringArray(R.array.list_setting_array);
 		final Integer[] Icons_setting = {
 				R.drawable.ic_menu_setting,
 				R.drawable.ic_menu_delete
 
 				};
 		//Start to add setting to list view navigation
 		items.add(new Header(setting));
 		for (int i =0; i< list_setting_array.length; i++)
 		{
		items.add(new ListItem(list_setting_array[i], Icons_setting[i], getActivity()));
 		}
 		TwoTextArrayAdapter adapter = new TwoTextArrayAdapter(getActivity(), items);
 		getListView().setDivider(null);
 		setListAdapter(adapter);
 		
 	}
 
 //	private class SampleItem {
 //		public String tag;
 //		public int iconRes;
 //
 //		public SampleItem(String tag, int iconRes) {
 //			this.tag = tag;
 //			this.iconRes = iconRes;
 //		}
 //	}
 
 //	public class SampleAdapter extends ArrayAdapter<SampleItem> {
 //
 //		public SampleAdapter(Context context) {
 //			super(context, 0);
 //		}
 //
 //		@Override
 //		public View getView(int position, View convertView, ViewGroup parent) {
 //			if (convertView == null) {
 //				convertView = LayoutInflater.from(getContext()).inflate(
 //						R.layout.row, null);
 //			}
 //			ImageView icon = (ImageView) convertView
 //					.findViewById(R.id.row_icon);
 //			icon.setImageResource(getItem(position).iconRes);
 //			TextView title = (TextView) convertView
 //					.findViewById(R.id.row_title);
 //			title.setText(getItem(position).tag);
 //
 //			return convertView;
 //		}
 
 //	}
 }
