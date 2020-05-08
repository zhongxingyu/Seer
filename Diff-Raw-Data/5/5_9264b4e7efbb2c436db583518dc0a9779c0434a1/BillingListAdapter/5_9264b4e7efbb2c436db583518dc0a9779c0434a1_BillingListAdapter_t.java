 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Adapters;
 
 import android.content.Context;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.TextView;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.BillingItem;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 public class BillingListAdapter extends BaseExpandableListAdapter {
 	private ArrayList<String> billingCaptions = new ArrayList<String>();
 	private ArrayList<String[]> billingItems = new ArrayList<String[]>();
 	private Context context;
 
 	public BillingListAdapter(Context context, ArrayList<BillingItem> billinglist) {
 		this.context = context;
 		addAll(billinglist);
 	}
 	public int getRequestID(int itemID){
 		String id = billingItems.get(itemID)[0];
 		id = id.substring(id.indexOf(":")+2);
 		return Integer.valueOf(id);
 	}
 	public String getAlgSuffix(int itemID){
 		String algSuffix = billingItems.get(itemID)[1];
 		algSuffix = algSuffix.substring(algSuffix.indexOf(":")+2);
 		return  algSuffix;
 	}
 	public String getStatus(int itemID){
 		String status = billingItems.get(itemID)[5];
 		status = status.substring(status.indexOf(":")+2);
 		return  status;
 	}
 	public void addAll(ArrayList<BillingItem> items) {
 		setupList(items);
 	}
 
 
 	private void setupList(ArrayList<BillingItem> items) {
 		billingCaptions.ensureCapacity(billingCaptions.size() + items.size());
 		for (int i = 0; i < items.size(); i++) {
 			billingCaptions.add(context.getResources().getString(R.string.tour)+" " + String.valueOf(items.get(i).getRequestid()) +" "+ items.get(i).getAlgorithm());
 		}
 
 		billingItems.ensureCapacity(billingItems.size() + items.size());
 		Double cost = 0.0;
 		String date;
 		for (int i = 0; i < items.size(); i++) {
 			date =  items.get(i).getRequestdate();
 			String DateYearDayMonth = date.substring(0, date.indexOf("T"));
 			String DateTime = date.substring(date.indexOf("T")+1,date.indexOf("."));
 			cost = ((double)items.get(i).getCost())/100;
 						
 			String[] arr = new String[6];
 			arr[0] = context.getResources().getString(R.string.requestid)+": " + items.get(i).getRequestid();
 			arr[1] = context.getResources().getString(R.string.algorithmn)+": " + items.get(i).getAlgorithm();
			arr[2] = context.getResources().getString(R.string.cost)+": " + cost + " " + context.getResources().getString(R.string.euro);
 			arr[3] = context.getResources().getString(R.string.requestdate)+": \n" + DateYearDayMonth +" "+DateTime;
			arr[4] = context.getResources().getString(R.string.duration)+": " + items.get(i).getDuration() + " "+context.getResources().getString(R.string.milliseconds);
 			arr[5] = context.getResources().getString(R.string.status)+": " + items.get(i).getStatus();
 			billingItems.add(arr);
 		}
 	}
 
 	public Object getChild(int groupPosition, int childPosition) {
 		return billingItems.get(groupPosition)[childPosition];
 	}
 
 	public long getChildId(int groupPosition, int childPosition) {
 		return childPosition;
 	}
 
 	public int getChildrenCount(int groupPosition) {
 		return billingItems.get(groupPosition).length;
 	}
 
 	public TextView getGenericView() {
 		// Layout parameters for the ExpandableListView
 		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
 				ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
 
 		TextView textView = new TextView(context);
 		textView.setLayoutParams(lp);
 		// Center the text vertically
 		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
 		// Set the text starting position
 		textView.setPadding(36, 0, 0, 0);
 		textView.setTextSize(20);
 		return textView;
 	}
 
 	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
 	                         View convertView, ViewGroup parent) {
 		TextView textView = getGenericView();
 		textView.setText(getChild(groupPosition, childPosition).toString());
 		return textView;
 	}
 
 	public Object getGroup(int groupPosition) {
 		return billingCaptions.get(groupPosition);
 	}
 
 	public int getGroupCount() {
 		return billingCaptions.size();
 	}
 
 	public long getGroupId(int groupPosition) {
 		return groupPosition;
 	}
 
 	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
 	                         ViewGroup parent) {
 		TextView textView = getGenericView();
 		textView.setText(getGroup(groupPosition).toString());
 		return textView;
 	}
 
 	public boolean isChildSelectable(int groupPosition, int childPosition) {
 		return true;
 	}
 
 	public boolean hasStableIds() {
 		return true;
 	}
 
 }
 
