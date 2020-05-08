 package com.carolineleung.clickcontrols.styled.applist;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.carolineleung.clickcontrols.R;
 
 public class AppListAdapter extends ArrayAdapter<AppEntry> {
 	private LayoutInflater mInflater;
 
 	public AppListAdapter(Context context) {
 		super(context, android.R.layout.simple_list_item_2);
 		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	public void setData(List<AppEntry> appEntriesData) {
 		clear();
 		if (appEntriesData != null) {
 			for (AppEntry appEntry : appEntriesData) {
 				add(appEntry);
 			}
 		}
 	}
 
 	/**
 	 * Populate the items into the list
 	 */
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view;
 		if (convertView != null) {
 			view = convertView;
 		} else {
 			view = mInflater.inflate(R.layout.app_list_item_icon_text, parent, false);
 		}
 		final AppEntry item = getItem(position);
 		TextView textView = (TextView) view.findViewById(R.id.app_entry_text);
 		textView.setText(item.getLabel());
		item.getIcon().setBounds(0, 0, 75, 75);
 		textView.setCompoundDrawables(item.getIcon(), null, null, null);
 
 		ImageButton accessoryButton = (ImageButton) view.findViewById(R.id.app_setting_button);
		accessoryButton.setImageResource(R.drawable.ad_btn_radio_off_pressed_holo_light);
 		accessoryButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(getContext(), "Clicked " + item.getLabel(), Toast.LENGTH_SHORT).show();
 			}
 		});
 		return view;
 	}
 }
