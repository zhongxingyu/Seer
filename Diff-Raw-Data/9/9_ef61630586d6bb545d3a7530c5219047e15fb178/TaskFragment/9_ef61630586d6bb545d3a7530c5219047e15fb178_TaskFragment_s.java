 package com.example.newspinproj;
 
 //import android.app.ListFragment;
 import java.util.ArrayList;
 
 
 import Model.DataContainer;
 import Model.Task;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TaskFragment extends ListFragment {
 	private View view = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		ArrayList<Task> copy;
 		Bundle bundle = getArguments();
 		if (bundle == null) {
 			copy = DataContainer.getTasks();
 
 			if (copy == null) {
 				copy = new ArrayList<Task>();
 			}
 		} else {
 			String date = bundle.getString("date");
 			String group = bundle.getString("group");
 			
 			if(date != null && group != null) {
 				copy = DataContainer.getTasksbyGroupandDay(group, date);
 			} else if (date != null) {
 				copy = DataContainer.getListByDay(date);
 			} else if (group != null) {
 				copy = DataContainer.getTasksByGroup(group);
 			} else {
 				copy = new ArrayList<Task>();
 			}
 		}
 
 		ArrayList<Task> values = new ArrayList<Task>(copy);
 
 		System.out.println(values.toString());
 		setListAdapter(new MobileArrayAdapter(this.getActivity(), values));
 
 	}
 
 	static TaskFragment newInstance(int num) {
 		TaskFragment f = new TaskFragment();
 		// Supply num input as an argument.
 		Bundle args = new Bundle();
 		args.putInt("num", num);
 		f.setArguments(args);
 		return f;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
 
 		if (true || view == null)
 			view = inflater.inflate(R.layout.task_fragment, container, false);
 
 		return view;
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 
 		// get selected items
 		/*
 		 * String selectedValue = ((Task) getListAdapter().getItem(position))
 		 * .getname();
 		 */
 		// Toast.makeText(this.getActivity(), selectedValue,
 		// Toast.LENGTH_SHORT).show();
 
 		String selectedValue = ((Task) getListAdapter().getItem(position))
 				.getname();
 		String selectedTime = (String) ((Task) getListAdapter().getItem(
 				position)).getTime();
 		String selectedgroup = (String) ((Task) getListAdapter().getItem(
 				position)).getGroup();
 		String selectedutid = (String) ((Task) getListAdapter().getItem(
 				position)).getutid();
 		String selectedmem = ((Task) getListAdapter().getItem(position))
 				.getunfilteredunames();
 
 		//
 
 		Intent i = new Intent(getActivity(), TaskDetailsActivity.class);
 		i.putExtra("taskname", selectedValue);
 		i.putExtra("tasktime", selectedTime);
 		i.putExtra("taskgroup", selectedgroup);
 		i.putExtra("people", selectedmem);
 		i.putExtra("utid", selectedutid);
 		startActivity(i);
 
 	}
 
 	/*
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 	    //No call for super(). Bug on API Level > 11.
 	}*/
 	
 	@Override
 	public void onDestroyView() {
 		// super.onDestroyView();
 		ViewGroup parentViewGroup = (ViewGroup) view.getParent();
 		if (null != parentViewGroup) {
 			parentViewGroup.removeView(view);
 			// view=null;
 		}
 
 		super.onDestroyView();
 	}
 
 	/*
 	 * @Override public void onAttach(Activity activity) {
 	 * super.onAttach(activity);
 	 * 
 	 * Toast.makeText(activity.getApplicationContext(),
 	 * String.valueOf(this.getId()), Toast.LENGTH_SHORT).show();
 	 * 
 	 * }
 	 */
 
 	/*
 	 * @Override public void onStop() { super.onStop();
 	 * System.out.println("Tasks stopped"); }
 	 * 
 	 * @Override public void onPause() { super.onStop();
 	 * System.out.println("Tasks paused"); }
 	 * 
 	 * @Override public void onResume() { System.out.println("Tasks resumed");
 	 * 
 	 * super.onResume(); }
 	 */
 
 	private class MobileArrayAdapter extends ArrayAdapter<Task> {
 		private final Context context;
 		private final ArrayList<Task> values;
 
 		public MobileArrayAdapter(Context context, ArrayList<Task> values2) {
 			super(context, R.layout.listviewlayout, values2);
 			this.context = context;
 			this.values = values2;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			// System.out.println("got a value set");
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			View rowView = inflater.inflate(R.layout.listviewlayout, parent,
 					false);
 			TextView taskNameView = (TextView) rowView
 					.findViewById(R.id.taskName);
 			TextView taskTimeView = (TextView) rowView
 					.findViewById(R.id.taskTime);
 			TextView taskGroupView = (TextView) rowView
 					.findViewById(R.id.taskGroup);
 			ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
 			taskNameView.setText(values.get(position).getname());
 			taskTimeView.setText(values.get(position).getTime());
 			taskGroupView.setText(values.get(position).getGroup());
 
			//Boolean isLeader = DataContainer.getFullLeaderNames(groupName).contains(name);
 			//Boolean isLeader = name.startsWith("M");
			Boolean isLeader = true;
			ImageView leaderIcon = (ImageView) view.findViewById(R.id.star);
 			leaderIcon.setVisibility(isLeader ? View.VISIBLE : View.INVISIBLE);
 			
 			
 			
 			
 			
 			/*
 			 * Typeface tf = Typeface.defaultFromStyle(Typeface.ITALIC);
 			 * taskTimeView.setTypeface(tf);
 			 */
 
 			// Change icon based on name
 			String s = values.get(position).getname();
 
 			// System.out.println(s);
 
 			if (s.equals("Take out the trash")) {
 				imageView.setImageResource(R.drawable.trash);
 			} else if (s.equals("Dish duty")) {
 				imageView.setImageResource(R.drawable.dishes);
 			} else if (s.equals("Clean-up")) {
 				imageView.setImageResource(R.drawable.cleanup);
 			} else if (s.equals("Water the plants")) {
 				imageView.setImageResource(R.drawable.plants);
 			} else if (s.equals("Feed the pets")) {
 				imageView.setImageResource(R.drawable.pet);
 			} else {
 				imageView.setImageResource(R.drawable.custom);
 			}
 
 			return rowView;
 		}
 	}
 }
