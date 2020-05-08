 /*
 * Copyright (c) 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.pirules.gcontactsync.android;
 
 import org.pirules.gcontactsync.android.model.contact.ContactEntry;
 import org.pirules.gcontactsync.android.model.group.GroupEntry;
 
 import android.view.LayoutInflater;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 
 import android.widget.TextView;
 
 import android.view.View;
 import android.view.ViewGroup;
 
 import android.widget.BaseExpandableListAdapter;
 
 /**
  * @author Josh Geenen
  *
  */
 public class GCSExpandableListAdapter extends BaseExpandableListAdapter {
   
  Context context;
   
   public ArrayList<GroupEntry> groups;
   
   public GCSExpandableListAdapter(Context context) {
     this.context = context;
    //this.groups = new ArrayList<GroupEntry>();
   }
   
   public void setGroups(ArrayList<GroupEntry> groups) {
     this.groups = groups;
   }
 
   /* (non-Javadoc)
    * @see android.widget.ExpandableListAdapter#getChild(int, int)
    */
   @Override
   public Object getChild(int groupPosition, int childPosition) {
     GroupEntry group = groups.get(groupPosition);
     ContactEntry contact = group != null && group.contacts != null ? group.contacts.get(childPosition) : null;
     return contact != null ? contact.toString() : "";
   }
 
   /* (non-Javadoc)
    * @see android.widget.ExpandableListAdapter#getChildId(int, int)
    */
   @Override
   public long getChildId(int groupPosition, int childPosition) {
     return childPosition;
   }
 
   /* (non-Javadoc)
    * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
    */
   @Override
   public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View convertView, ViewGroup parent) {
     String childName = getChild(groupPosition, childPosition).toString();
     if (convertView == null) {
       LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       convertView = inflater.inflate(R.layout.expandable_child_layout, null);
     }
     TextView textView = (TextView)convertView.findViewById(R.id.tvChildName);
     if (textView != null) {
       textView.setText(childName);
     }
     return convertView;
   }
 
   /* (non-Javadoc)
    * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
    */
   @Override
   public int getChildrenCount(int groupPosition) {
     GroupEntry group = groups.get(groupPosition);
     return group != null && group.contacts != null ? group.contacts.size() : 0;
   }
 
   /* (non-Javadoc)
    * @see android.widget.ExpandableListAdapter#getGroup(int)
    */
   @Override
   public Object getGroup(int groupPosition) {
     GroupEntry group = groups.get(groupPosition);
     return group != null ? group.toString() : "";
   }
 
   /* (non-Javadoc)
    * @see android.widget.ExpandableListAdapter#getGroupCount()
    */
   @Override
   public int getGroupCount() {
     return groups.size();
   }
 
   /* (non-Javadoc)
    * @see android.widget.ExpandableListAdapter#getGroupId(int)
    */
   @Override
   public long getGroupId(int groupPosition) {
     return groupPosition;
   }
 
   /* (non-Javadoc)
    * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
    */
   @Override
   public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
     String groupName = getGroup(groupPosition).toString();
     if (convertView == null) {
       LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       convertView = inflater.inflate(R.layout.expandable_group_layout, null);
     }
     TextView textView = (TextView)convertView.findViewById(R.id.tvGroupName);
     if (textView != null) {
       textView.setText(groupName);
     }
     return convertView;
   }
 
   /* (non-Javadoc)
    * @see android.widget.ExpandableListAdapter#hasStableIds()
    */
   @Override
   public boolean hasStableIds() {
     return true;
   }
 
   /* (non-Javadoc)
    * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
    */
   @Override
   public boolean isChildSelectable(int groupPosition, int childPosition) {
     return true;
   }
 }
