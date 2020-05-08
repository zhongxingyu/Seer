 
 package com.george.expanablelistfragment2;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Pair;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class TestFragment extends CustomExpandableListFragment {
 
     ArrayList<Pair<String, Integer>> mGroups;
     ArrayList<String> mChildren;
     TestAdapter mAdapter;
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         final int groupSize = 31;
         mGroups = new ArrayList<Pair<String, Integer>>(groupSize);
         Random random = new Random(System.currentTimeMillis());
         for (int i = 0; i < groupSize; i++) {
             final int size = Math.abs(random.nextInt()) % 17;
             mGroups.add(new Pair<String, Integer>("Group " + i, size));
         }
         final int childSize = 17;
         mChildren = new ArrayList<String>(childSize);
         for (int i = 0; i < childSize; i++) {
             mChildren.add("Child " + i);
         }
         mAdapter = new TestAdapter();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         setListAdapter(mAdapter);
     }
 
     @Override
     public void onFlotageViewContentChanged(int groupPosition, int childPosition) {
         super.onFlotageViewContentChanged(groupPosition, childPosition);
 
         ((TextView) mFlotageView.findViewById(android.R.id.content)).setText(mAdapter
                 .getGroup(groupPosition).first);
     }
 
     class TestAdapter extends BaseExpandableListAdapter {
 
         @Override
         public int getGroupCount() {
             return mGroups.size();
         }
 
         @Override
         public int getChildrenCount(int groupPosition) {
             return mGroups.get(groupPosition).second;
         }
 
         @Override
         public Pair<String, Integer> getGroup(int groupPosition) {
             return mGroups.get(groupPosition);
         }
 
         @Override
         public Object getChild(int groupPosition, int childPosition) {
             return mChildren.get(childPosition);
         }
 
         @Override
         public long getGroupId(int groupPosition) {
             return groupPosition;
         }
 
         @Override
         public long getChildId(int groupPosition, int childPosition) {
             return childPosition;
         }
 
         @Override
         public boolean hasStableIds() {
             return true;
         }
 
         @Override
         public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                 ViewGroup parent) {
             GroupViewHolder h;
             if (convertView == null) {
                 convertView = View.inflate(getActivity(), R.layout.list_group_item, null);
                 h = new GroupViewHolder();
                 h.contentView = (TextView) convertView.findViewById(android.R.id.content);
                 convertView.setTag(h);
             } else {
                 h = (GroupViewHolder) convertView.getTag();
             }
             Pair<String, Integer> group = (Pair<String, Integer>) getGroup(groupPosition);
             h.contentView.setText(group.first);
             return convertView;
         }
 
         class GroupViewHolder {
             TextView contentView;
         }
 
         @Override
         public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                 View convertView, ViewGroup parent) {
             ChildViewHolder h;
             if (convertView == null) {
                 convertView = View.inflate(getActivity(), R.layout.list_child_item, null);
                 h = new ChildViewHolder();
                 h.contentView = (TextView) convertView.findViewById(android.R.id.content);
                 convertView.setTag(h);
             } else {
                 h = (ChildViewHolder) convertView.getTag();
             }
             h.contentView.setText((CharSequence) getChild(groupPosition, childPosition));
             return convertView;
         }
 
         class ChildViewHolder {
             TextView contentView;
         }
 
         @Override
         public boolean isChildSelectable(int groupPosition, int childPosition) {
             return false;
         }
 
     }
 }
