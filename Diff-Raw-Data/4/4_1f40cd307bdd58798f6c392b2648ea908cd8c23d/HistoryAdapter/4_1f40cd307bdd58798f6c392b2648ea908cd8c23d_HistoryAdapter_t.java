 package net.mms_projects.copy_it.adapters;
 
 import java.util.List;
 
 import net.mms_projects.copy_it.R;
 import net.mms_projects.copy_it.models.HistoryItem;
 import net.mms_projects.copy_it.models.HistoryItem.Change;
 import net.mms_projects.utils.InlineSwitch;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class HistoryAdapter extends BaseAdapter {
 
 	private LayoutInflater inflater;
 	private List<HistoryItem> items;
 
 	public HistoryAdapter(Context context, List<HistoryItem> list) {
 		this.items = list;
 		this.inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	@Override
 	public int getCount() {
 		return this.items.size();
 	}
 
 	@Override
 	public HistoryItem getItem(int position) {
 		return this.items.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		if (convertView == null)
 			view = this.inflater.inflate(R.layout.activity_history_item, null);
 
 		HistoryItem item = this.getItem(position);
 
 		TextView content = (TextView) view
 				.findViewById(R.id.history_item_content);
 		TextView date = (TextView) view.findViewById(R.id.history_item_date);
 		ImageView image = (ImageView) view
 				.findViewById(R.id.history_item_image);
 
 		content.setText(item.content);
 		InlineSwitch<Change, Integer> imageSwitch = new InlineSwitch<HistoryItem.Change, Integer>();
 		imageSwitch.addClause(Change.PULLED, R.drawable.dashboard_icon_pull);
 		imageSwitch.addClause(Change.PUSHED, R.drawable.dashboard_icon_push);
		imageSwitch.addClause(Change.SEND_TO_APP, R.drawable.history_icon_shareit_up);
		imageSwitch.addClause(Change.RECEIVED_FROM_APP, R.drawable.history_icon_shareit_down);
 		image.setImageResource(imageSwitch.runSwitch(item.change));
 
 		date.setText(item.date.toString());
 		return view;
 	}
 
 }
