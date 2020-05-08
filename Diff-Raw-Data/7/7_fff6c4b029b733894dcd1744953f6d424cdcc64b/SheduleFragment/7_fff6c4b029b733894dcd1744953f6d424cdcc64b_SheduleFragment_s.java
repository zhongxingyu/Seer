 package com.mh.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 import com.mh.R;
 import com.mh.util.MyDBHelper;
 import com.mh.util.MyDateHelper;
 import com.mh.util.NumHelper;
 
 public class SheduleFragment extends Fragment implements OnItemClickListener {
 	private static final String TAG = SheduleFragment.class.getClass()
 			.getSimpleName();
 	private static final int EDIT = 332;
 	private static final int COPY = 235;
 	private static final int DEL = 8983;
 	private static final int PASTE = 328;
 	private static boolean COPY_OK = false;
 	private static final int NUMCLASS_DAY = 12;
 
 	private static String subjectCOPY = "";
 	private static String placeCOPY = "";
 	private static String teacherCOPY = "";
 	private static String infoCOPY = "";
 
 	public static final String ARG_WEEK = "week";
 	private ListView listView;
 	public int currentWeekDayNum;
 	ArrayList<Map<String, String>> listItem = new ArrayList<Map<String, String>>();
 	SimpleAdapter listItemAdapter;
 	MyDBHelper dbHelper;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_collection_object,
 				container, false);
 		Bundle args = getArguments();
 		int week = args.getInt(ARG_WEEK);
 
 		listView = (ListView) rootView.findViewById(R.id.classList);
 		listView.setOnCreateContextMenuListener(this);
 		listView.setOnItemClickListener(this);
 
 		currentWeekDayNum = week;
 		dbHelper = new MyDBHelper(this.getActivity());
 		listItemAdapter = new SimpleAdapter(this.getActivity(),
 				listItem, R.layout.class_list, new String[] { "ItemTitle",
 						"ItemText" },
 				new int[] { R.id.ItemTitle, R.id.ItemText });
 		listView.setAdapter(listItemAdapter);
 		this.refreshDateInListView(currentWeekDayNum);
 		return rootView;
 	}
 
 	@Override
 	public void onResume() {
 		Log.v(TAG, "onResume");
 		super.onResume();
 		this.refreshDateInListView(currentWeekDayNum);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int location,
 			long arg3) {
 		String sequence = "第" + NumHelper.numToString(location + 1) + "节";
 		String week = NumHelper.numToWeek(currentWeekDayNum);
 		Cursor cursor = dbHelper.open().query(week, sequence);
 		dbHelper.close();
 		if (cursor != null && cursor.getCount() > 0) {
 			String subject = cursor.getString(3);
 			String place = cursor.getString(4);
 			String teacher = cursor.getString(5);
 			String time = cursor.getString(6);
 			String info = cursor.getString(7);
 			String str = sequence + "\n" + "课程名：" + subject + "\n" + "教室："
 					+ place + "\n" + "老师：" + teacher + "\n" + "时间：" + time
 					+ "\n" + "备注：" + info;
 			new AlertDialog.Builder(this.getActivity()).setTitle(week)
 					.setMessage(str).create().show();
 		} else {
 			Toast.makeText(this.getActivity(), "当前没有课程", Toast.LENGTH_SHORT)
 					.show();
 		}
 	}
 
 	public void refreshDateInListView(int weekNum) {
 		listItem.clear();
 		// 此处需要从数据库中读取课程表数据
 		for (int i = 1; i <= NUMCLASS_DAY; i++) {
 			Map<String, String> map = new HashMap<String, String>();
 			String sequence = "第" + NumHelper.numToString(i) + "节";
 			map.put("ItemTitle", sequence);
 
 			Cursor cursor = dbHelper.open().query(NumHelper.numToWeek(weekNum),
 					sequence);
 			dbHelper.close();
 			if (cursor != null && cursor.getCount() > 0) {
 				int count = cursor.getCount();
 				Log.v(TAG, "count = " + count);
 
 				String subject = cursor.getString(3);
 				String place = cursor.getString(4);
 				String str = subject + "(" + place + ")";
 				map.put("ItemText", str);
 			}
 			listItem.add(map);
 		}
 		listItemAdapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		Log.v(TAG, "onContextItemSelected, currentWeekDayNum : "
 				+ currentWeekDayNum);
 		if (currentWeekDayNum == item.getGroupId()) {
 			String weekName = MyDateHelper.getWeekString(currentWeekDayNum);
 			// 第几节
 			int selectPosition = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
 			String num = NumHelper.numToString(selectPosition + 1);
 			String sequence = "第" + num + "节";
 			if (item.getItemId() == EDIT) {
 				Intent intent = new Intent();
 				intent.setClass(this.getActivity(), EditShedule.class);
 				intent.putExtra("weekName", weekName);
 				intent.putExtra("position", selectPosition);
 				startActivity(intent);
 			}
 
 			if (item.getItemId() == COPY) {
 				Cursor cursor = dbHelper.open().query(weekName, sequence);
 				if (cursor != null && cursor.getCount() > 0) {
 					subjectCOPY = cursor.getString(3);
 					placeCOPY = cursor.getString(4);
 					teacherCOPY = cursor.getString(5);
 					infoCOPY = cursor.getString(7);
 					COPY_OK = true;
 					Toast.makeText(this.getActivity(), "已复制",
 							Toast.LENGTH_SHORT).show();
 
 				}
 
 			}
 			if (item.getItemId() == DEL) {
 				Log.v(TAG, "weekName = " + weekName);
 				Log.v(TAG, "sequence = " + sequence);
 				dbHelper.open().delete(weekName, sequence);
 				dbHelper.close();
 				refreshDateInListView(currentWeekDayNum);
 				Toast.makeText(this.getActivity(), "删除成功", Toast.LENGTH_LONG)
 						.show();
 			}
 			if (item.getItemId() == PASTE) {
 				// 复制过来的时间不合适，所以startTime 为 空串
 				String startTime = "";
 				dbHelper.open().insert(weekName, sequence, subjectCOPY,
 						placeCOPY, teacherCOPY, startTime, infoCOPY);
 				dbHelper.close();
 				Toast.makeText(this.getActivity(), "粘贴成功", Toast.LENGTH_SHORT)
 						.show();
 				refreshDateInListView(currentWeekDayNum);
 				COPY_OK = false;
 				subjectCOPY = "";
 				placeCOPY = "";
 				teacherCOPY = "";
 				infoCOPY = "";
 			}
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		Log.v(TAG, "onCreateContextMenu");
 		menu.setHeaderTitle("请选择");
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		int selectPosition = info.position;
 		String num = NumHelper.numToString(selectPosition + 1);
 		String sequence = "第" + num + "节";
 		String weekName = MyDateHelper.getWeekString(currentWeekDayNum);
 		Cursor cursor = dbHelper.open().query(weekName, sequence);
 		// 如果此记录有值，则另有复制和删除的选项
 		if (cursor != null && cursor.getCount() > 0) {
 			menu.add(currentWeekDayNum, COPY, 1, "复制");
 			menu.add(currentWeekDayNum, DEL, 2, "删除");
 		}
 
 		menu.add(currentWeekDayNum, EDIT, 0, "编辑");
 		// 如果COPY_OK是真，则有复制的选项
 		if (COPY_OK) {
 			menu.add(currentWeekDayNum, PASTE, 3, "粘贴");
 		}
 	}
 
 }
