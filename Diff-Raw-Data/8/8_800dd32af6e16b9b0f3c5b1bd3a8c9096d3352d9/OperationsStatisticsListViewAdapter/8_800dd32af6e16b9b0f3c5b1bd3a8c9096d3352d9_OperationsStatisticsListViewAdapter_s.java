 package com.dzebsu.acctrip.adapters;
 
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.dzebsu.acctrip.R;
 import com.dzebsu.acctrip.operations.GraphStatActivity;
 import com.dzebsu.acctrip.operations.StatisticsFragment.SortItemPair;
 
 public class OperationsStatisticsListViewAdapter extends BaseExpandableListAdapter {
 
 	private String[] sortCategories;
 
 	private Map<String, List<SortItemPair>> sortedValues;
 
 	private LayoutInflater inflater;
 
 	private Context ctx;
 
 	private long eventId;
 
 	public OperationsStatisticsListViewAdapter(Context ctx, long eventId, String[] sortCategories,
 			Map<String, List<SortItemPair>> sortedValues) {
 		super();
 		this.eventId = eventId;
 		this.sortCategories = sortCategories;
 		this.sortedValues = sortedValues;
 		this.ctx = ctx;
 		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	static class ChildViewHolder {
 
 		public TextView title = null;
 
 		public TextView value = null;
 	}
 
 	static class GroupViewHolder {
 
 		public TextView title = null;
 
 		public ImageButton graph = null;
 	}
 
 	@Override
 	public Object getChild(int groupPosition, int childPosition) {
 		return sortedValues.get(sortCategories[groupPosition]).get(childPosition);
 	}
 
 	public void setSortCategories(String[] sortCategories) {
 		this.sortCategories = sortCategories;
 	}
 
 	public void setSortedValues(Map<String, List<SortItemPair>> sortedValues) {
 		this.sortedValues = sortedValues;
 		this.notifyDataSetChanged();
 	}
 
 	@Override
 	public long getChildId(int groupPosition, int childPosition) {
 		return 0;
 	}
 
 	@Override
 	public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView,
 			ViewGroup parent) {
 		View rowView = convertView;
 		if (rowView == null) {
 			rowView = inflater.inflate(R.layout.row_statistics_pair, parent, false);
 			ChildViewHolder childViewHolder = new ChildViewHolder();
 			childViewHolder.title = (TextView) rowView.findViewById(R.id.row_stat_title);
 			childViewHolder.value = (TextView) rowView.findViewById(R.id.row_stat_value);
 			rowView.setTag(childViewHolder);
 		}
 		ChildViewHolder holder = (ChildViewHolder) rowView.getTag();
 		holder.title.setText(sortedValues.get(sortCategories[groupPosition]).get(childPosition).getTitle());
 		holder.value.setText(sortedValues.get(sortCategories[groupPosition]).get(childPosition).getValue());
 		return rowView;
 	}
 
 	@Override
 	public int getChildrenCount(int groupPosition) {
 		return sortedValues.get(sortCategories[groupPosition]).size();
 	}
 
 	@Override
 	public Object getGroup(int groupPosition) {
 		return sortedValues.get(sortCategories[groupPosition]);
 	}
 
 	@Override
 	public int getGroupCount() {
 		return sortCategories.length;
 	}
 
 	@Override
 	public long getGroupId(int groupPosition) {
 		return groupPosition;
 	}
 
 	@Override
 	public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
 		View rowView = convertView;
 		if (rowView == null) {
 			rowView = inflater.inflate(R.layout.statistics_group_item, parent, false);
 			GroupViewHolder groupViewHolder = new GroupViewHolder();
 			groupViewHolder.title = (TextView) rowView.findViewById(R.id.stat_group_item_title);
 			groupViewHolder.graph = (ImageButton) rowView.findViewById(R.id.stat_group_item_graph_btn);
 			rowView.setTag(groupViewHolder);
 		}
 
 		GroupViewHolder holder = (GroupViewHolder) rowView.getTag();
 		holder.title.setText(sortCategories[groupPosition]);

 		holder.graph.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				showChartActivity(groupPosition);
 
 			}
 		});
 		return rowView;
 	}
 
 	protected void showChartActivity(int groupPosition) {
 
 		Intent intent = new Intent(ctx, GraphStatActivity.class);
 		intent.putExtra(GraphStatActivity.INTENT_KEY_SORT_CATEGORY, groupPosition);
 		intent.putExtra(GraphStatActivity.INTENT_KEY_EVENT_ID, eventId);
 		ctx.startActivity(intent);

		/*
		 * TimeGraph graph = new TimeGraph(); Intent intent =
		 * graph.getIntent(ctx); ctx.startActivity(intent);
		 */
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return false;
 	}
 
 	@Override
 	public boolean isChildSelectable(int arg0, int arg1) {
 		return false;
 	}
 
 }
