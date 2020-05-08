 package com.alvarosantisteban.berlincurator;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.TextView;
 
 /**
  * An adapter for the ExpandableList that enables the population through an ArrayList
  * 
  * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
  *
  */
 public class ListAdapter extends BaseExpandableListAdapter {
 	
 	private Context context;
 	/**
 	 * The ArrayList used to get the information 
 	 */
 	private ArrayList<HeaderInfo> websitesList;
 	  
 	public ListAdapter(Context context, ArrayList<HeaderInfo> websiteList) {
 		 this.context = context;
 		 this.websitesList = websiteList;
 	}
 	  
 	/**
 	 * Gets the event from the given position within the given group (site)
 	 * 
 	 * @param groupPosition the position of the group that the event resides in
 	 * @param childPosition the position of the event with respect to other events in the group
 	 * 
 	 * @return the event
 	 */
 	public Object getChild(int groupPosition, int childPosition) {
 		ArrayList<Event> eventsList = websitesList.get(groupPosition).getEventsList();
 		return eventsList.get(childPosition);
 	}
 	 
 	/**
 	 * Gets the id of the event, which is its position
 	 * 
 	 * @param groupPosition the position of the group that the event resides in
 	 * @param childPosition the position of the event with respect to other events in the group
 	 * 
 	 * @return the id of the event, which is its position 
 	 */
 	public long getChildId(int groupPosition, int childPosition) {
 		return childPosition;
 	}
 	 
 	/**
 	 * Gets a View that displays the data for the given event within the given group.
 	 * 
 	 * @param groupPosition the position of the group that the event resides in
 	 * @param childPosition the position of the event with respect to other events in the group
 	 * @param isLastChild Whether the event is the last event within the group
 	 * @param convertView the old view to reuse, if possible. 
 	 * @param parent the parent that this view will eventually be attached to 
 	 * 
 	 * @return the View corresponding to the event at the specified position 
 	 */
 	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 
 		Event detailInfo = (Event) getChild(groupPosition, childPosition);
 		if (convertView == null) {
 			LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = infalInflater.inflate(R.layout.child_row, null);
 		}
 	    
 		// Write the information of the event in the way we want 
 		TextView sequence = (TextView) convertView.findViewById(R.id.sequence);
 		sequence.setText(detailInfo.getSequence().trim() + ") ");
 		TextView childItem = (TextView) convertView.findViewById(R.id.childItem);
 		childItem.setText(Html.fromHtml(detailInfo.getName().trim())); // <------------------------ CAMBIADO
 	   
 		return convertView;
 	}
 	 
 	/**
 	 * Gets the number of events in a specified group.
 	 * 
 	 * @param groupPosition the position of the group for which the events count should be returned
 	 * 
 	 * @return the number of events in the specified group
 	 */
 	public int getChildrenCount(int groupPosition) {
 		ArrayList<Event> eventsList = websitesList.get(groupPosition).getEventsList();
 		return eventsList.size();
 	 
 	}
 	 
 	/**
 	 * Gets the data associated with the given group.
 	 * 
 	 * @param the position of the group
 	 * 
 	 * @return the ArrayList of HeaderInfo for the specified group 
 	 */
 	public Object getGroup(int groupPosition) {
 		return websitesList.get(groupPosition);
 	}
 	 
 	/**
 	 * Gets the number of groups
 	 * 
 	 * @return the number of groups
 	 */
 	public int getGroupCount() {
 		return websitesList.size();
 	}
 	 
 	/**
 	 * Gets the ID for the group at the given position. The ID is the position itself.
 	 * 
 	 * @param groupPosition the position of the group for which the ID is wanted
 	 * 
 	 * @return the ID associated with the group. It is the position itself.
 	 */
 	public long getGroupId(int groupPosition) {
 		return groupPosition;
 	}
 	 
 	/**
 	 * Gets a View that displays the given group. 
 	 * 
 	 * @param groupPosition the position of the group for which the View is returned
 	 * @param isExpanded whether the group is expanded or collapsed
 	 * @param convertView the old view to reuse, if possible. 
 	 * @param parent the parent that this view will eventually be attached to
 	 * 
 	 * @return the View corresponding to the group at the specified position 
 	 */
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
 		HeaderInfo headerInfo = (HeaderInfo) getGroup(groupPosition);
		if (convertView == null) {
 			LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = inf.inflate(R.layout.group_heading, null);
		}
 			   
 		TextView heading = (TextView) convertView.findViewById(R.id.heading);
 		String singPl = " events";
 		if (headerInfo.getEventsNumber() == 1){
 			singPl =" event";
 		}
 		heading.setText(headerInfo.getName().trim() +" - " +headerInfo.getEventsNumber() +singPl);
 		// If there are no events, make the header's color gray
 		if(headerInfo.getEventsNumber() == 0){
 			heading.setTextColor(Color.GRAY);
 		}
 		return convertView;
 	}
 	 
 	/**
 	 * Indicates whether the child and group IDs are stable across changes to the underlying data.
 	 * 
 	 * @return always true
 	 */
 	public boolean hasStableIds() {
 		return true;
 	}
 	 
 	/**
 	 * Whether the child at the specified position is selectable.
 	 * 
 	 * @return always true
 	 */
 	public boolean isChildSelectable(int groupPosition, int childPosition) {
 		return true;
 	}
 }
