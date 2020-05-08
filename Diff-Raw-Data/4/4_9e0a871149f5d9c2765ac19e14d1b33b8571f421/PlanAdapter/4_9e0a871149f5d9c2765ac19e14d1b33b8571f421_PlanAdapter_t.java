 package com.codingspezis.android.metalonly.player.plan;
 
 import java.util.*;
 
 import android.app.*;
 import android.content.*;
 import android.view.*;
 import android.widget.*;
 
 import com.codingspezis.android.metalonly.player.*;
 import com.codingspezis.android.metalonly.player.utils.*;
 
 public class PlanAdapter extends BaseAdapter {
 
 	private final Activity activity;
 	private final ArrayList<Item> data;
 	private static LayoutInflater inflater = null;
 	private final ImageLoader imageLoader;
 
 	public PlanAdapter(Activity a, ArrayList<Item> d) {
 		activity = a;
 		data = d;
 		inflater = (LayoutInflater) activity
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		imageLoader = new ImageLoader(a.getApplicationContext());
 	}
 
 	@Override
 	public int getCount() {
 		return data.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return data.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View v = convertView;
 
 		final Item i = data.get(position);
 		if (i != null) {
 			if (i.isSection()) {
 				SectionItem si = (SectionItem) i;
 				v = inflater.inflate(R.drawable.plan_section, null);
 				v.setOnClickListener(null);
 				v.setOnLongClickListener(null);
 				v.setLongClickable(false);
 				final TextView sectionView = (TextView) v
 						.findViewById(R.id.list_item_section_text);
 				sectionView.setText(si.getTitle());
 			} else {
 				v = inflater.inflate(R.layout.view_list_row_plan, null);
 				TextView title = (TextView) v.findViewById(R.id.txtTitle);
 				TextView mod = (TextView) v.findViewById(R.id.txtMod);
 				TextView time = (TextView) v.findViewById(R.id.txtTime);
 				TextView genre = (TextView) v.findViewById(R.id.txtGenre);
 				ImageView image = (ImageView) v.findViewById(R.id.modImage);
 				ProgressBar bar = (ProgressBar) v.findViewById(R.id.progress);
 				PlanData tmpData = data.get(position).getPlanData();
 
 				// Setting all values in listview
 				title.setText(tmpData.getTitle());
 				mod.setText(tmpData.getMod());
 				time.setText(tmpData.getTimeString());
 				genre.setText(tmpData.getGenre());
 				imageLoader.DisplayImage(tmpData.getMod(), image);

				// workaround for bottom margin bug
				bar.setProgress(100 - tmpData.getProgress());
 			}
 		}
 		return v;
 
 	}
 
 }
