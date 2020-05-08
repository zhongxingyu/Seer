 package pl.edu.agh.io.coordinator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import pl.edu.agh.io.coordinator.resources.Group;
 import pl.edu.agh.io.coordinator.resources.MapItem;
 import pl.edu.agh.io.coordinator.resources.User;
 import pl.edu.agh.io.coordinator.resources.UserItem;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Context;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 
 public class LayersMenuFragment extends Fragment {
 
 	private class LayersMenuListAdapter extends BaseExpandableListAdapter {
 
 		private static final int ITEM_POSITION = 0;
 		private static final int USER_POSITION = 1;
 		private static final int GROUP_POSITION = 2;
 
 		private Context context;
 		private List<String> menuGroups;
 		private List<UserItem> items;
 		private List<User> people;
 		private List<Group> groups;
 
 		public LayersMenuListAdapter(Context context) {
 			this.context = context;
 			this.menuGroups = new ArrayList<String>();
 			this.menuGroups.add("Items");
 			this.menuGroups.add("People");
 			this.menuGroups.add("Groups");
 			this.items = new ArrayList<UserItem>();
 			this.people = new ArrayList<User>();
 			this.groups = new ArrayList<Group>();
 		}
 
 		public void setItems(Set<UserItem> items) {
 			this.items = new ArrayList<UserItem>(items);
 		}
 
 		public void setPeople(Set<User> people) {
 			this.people = new ArrayList<User>(people);
 		}
 
 		public void setGroups(Set<Group> groups) {
 			this.groups = new ArrayList<Group>(groups);
 		}
 
 		@Override
 		public boolean areAllItemsEnabled() {
 			return true;
 		}
 
 		@Override
 		public Object getChild(int groupPosition, int childPosition) {
 			if (groupPosition == ITEM_POSITION) {
 				return items.get(childPosition);
 			} else if (groupPosition == USER_POSITION) {
 				return people.get(childPosition);
 			} else if (groupPosition == GROUP_POSITION) {
 				return groups.get(childPosition);
 			} else {
 				return null;
 			}
 		}
 
 		@Override
 		public long getChildId(int groupPosition, int childPosition) {
 			return childPosition;
 		}
 
 		@Override
 		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 			Object object = getChild(groupPosition, childPosition);
 			if (convertView == null) {
 				LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = layoutInflater.inflate(R.layout.child_layout, null);
 			}
 			TextView tv = (TextView) convertView.findViewById(R.id.tvChild);
 			String text = null;
 			if (groupPosition == ITEM_POSITION) {
 				text = ((UserItem) object).getId();
 			} else if (groupPosition == USER_POSITION) {
 				text = ((User) object).getId();
 			} else if (groupPosition == GROUP_POSITION) {
 				text = ((Group) object).getId();
 			}
 			tv.setText("   " + text);
 			tv.setTextColor(Color.BLACK);
 			return convertView;
 		}
 
 		@Override
 		public int getChildrenCount(int groupPosition) {
 			if (groupPosition == ITEM_POSITION) {
 				return items.size();
 			} else if (groupPosition == USER_POSITION) {
 				return people.size();
 			} else if (groupPosition == GROUP_POSITION) {
 				return groups.size();
 			} else {
 				return 0;
 			}
 		}
 
 		@Override
 		public Object getGroup(int groupPosition) {
 			return menuGroups.get(groupPosition);
 		}
 
 		@Override
 		public int getGroupCount() {
 			return 3;
 		}
 
 		@Override
 		public long getGroupId(int groupPosition) {
 			return groupPosition;
 		}
 
 		@Override
 		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
 			String group = (String) getGroup(groupPosition);
 			if (convertView == null) {
 				LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = layoutInflater.inflate(R.layout.group_layout, null);
 			}
 			TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
 			tv.setText(group);
 			tv.setTextColor(Color.BLACK);
 			return convertView;
 		}
 
 		@Override
 		public boolean hasStableIds() {
 			return true;
 		}
 
 		@Override
 		public boolean isChildSelectable(int arg0, int arg1) {
 			return true;
 		}
 
 	}
 
 	private LayersMenuListAdapter adapter;
 	private ExpandableListView listView;
 
 	public LayersMenuFragment() {
 	}
 
 	public void setItems(Set<UserItem> items) {
 		adapter.setItems(items);
 	}
 	
 	public void setPeople(Set<User> people) {
 		adapter.setPeople(people);
 	}
 	
 	public void setGroups(Set<Group> groups) {
 		adapter.setGroups(groups);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 		adapter = new LayersMenuListAdapter(getActivity());
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View toReturn = inflater.inflate(R.layout.fragment_layers_menu, container, false);
 		listView = (ExpandableListView) toReturn.findViewById(R.id.expandableListView);
 		listView.setAdapter(adapter);
 		return toReturn;
 	}
 
 }
