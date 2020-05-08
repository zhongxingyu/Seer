 /**
  * Copyright (C) 2013 HalZhang
  */
 
 package tk.dalang.gaminder.fragments;
 
 import java.text.SimpleDateFormat;
 
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import tk.dalang.gaminder.R;
 import tk.dalang.gaminder.elements.Game;
 import tk.dalang.gaminder.utils.CalendarUtils;
 import tk.dalang.gaminder.utils.DBManager;
 import tk.dalang.gaminder.utils.DateUtils;
 import tk.dalang.gaminder.utils.MyCalendar;
 import tk.dalang.gaminder.utils.WebParserUtils;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.handmark.pulltorefresh.library.PullToRefreshListView;
 import com.infteh.comboseekbar.ComboSeekBar;
 
 /**
  * GameReminder
  * <p>
  * Sina NBA TV
  * Data Source: http://nba.sports.sina.com.cn/showtv.php
  * </p>
  * 
  * @author <a href="http://weibo.com/iDalang">dalang</a>
  * @version Apr, 11 2013
  */
 public class SinaListFragment extends AbsBaseListFragment implements OnItemLongClickListener {
 
     private static final String LOG_TAG = SinaListFragment.class.getSimpleName();
 
     private SinaTask mSinaTask;
 
     private final String mSinaURL = "http://nba.sports.sina.com.cn/showtv.php";
 
 	private List<Game> mNbaData = new ArrayList<Game>();
 
     private SinaAdapter mAdapter;
 	private MyCalendar mCalendars[];
     private String mSelectedCalendarId = "0";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mAdapter = new SinaAdapter();
 		try {
 			 mCalendars = CalendarUtils.getCalendars(getActivity());
 		} catch (Exception e) {
 			Log.d("CALENDAR", "get calendar error");
 		}
    }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         getListView().setOnItemLongClickListener(this);
         setListAdapter(mAdapter);
         if (mSinaTask == null && mAdapter.isEmpty()) {
             mSinaTask = new SinaTask(SinaTask.TYPE_REFRESH);
             mSinaTask.execute(mSinaURL);
             getPullToRefreshListView().setRefreshing(true);
         }
     }
 
     @Override
     public void onDestroyView() {
         super.onDestroyView();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         if (mSinaTask != null) {
             mSinaTask.cancel(true);
             mSinaTask = null;
         }
     }
 
     @Override
     protected void onPullDownListViewRefresh(PullToRefreshListView refreshListView) {
         super.onPullDownListViewRefresh(refreshListView);
         EasyTracker.getTracker().sendEvent("ui_action", "pull_down_list_view_refresh",
                 "sina_list_fragmentt_pull_down_list_view_refresh", 0L);
         if (mSinaTask != null) {
             return;
         }
         mSinaTask = new SinaTask(SinaTask.TYPE_REFRESH);
         mSinaTask.execute(mSinaURL);
     }
 
     @Override
     protected void onPullUpListViewRefresh(PullToRefreshListView refreshListView) {
         super.onPullDownListViewRefresh(refreshListView);
         EasyTracker.getTracker().sendEvent("ui_action", "pull_up_list_view_refresh",
                 "sina_list_fragmentt_pull_up_list_view_refresh", 0L);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         EasyTracker.getTracker().sendEvent("ui_action", "list_item_click",
                 "sina_list_fragmentt_list_item_click", 0L);
         // TODO check why -1 here!!!
         Game entity = (Game) mAdapter.getItem(position - 1);
     }
 
 	private class SqliteTask extends AsyncTask<Game, Void, Boolean> {
         private DBManager mDBMgr = null;
 
         @Override  
         protected void onPreExecute() {  
             super.onPreExecute();  
             mDBMgr = new DBManager(getActivity());  
         }
         
 		@Override
 		protected Boolean doInBackground(Game... params) {
 			if (params[0] != null) {
 				int updated_rows = mDBMgr.updateEvent(params[0]);
 				if (updated_rows > 0)
 					return true;
 			}
 			return false;
 		}
 
         @Override
         protected void onPostExecute(Boolean result) {
             mDBMgr.closeDB();
             mDBMgr = null;
             super.onPostExecute(result);
         }
 	}
     private class SinaTask extends AsyncTask<String, Void, Boolean> {
 
         public static final int TYPE_REFRESH = 1;
         public static final int TYPE_LOADMORE = 2;
 
         private int mType = 0;
         private DBManager mDBMgr = null;
         public SinaTask(int type) {
             mType = type;
         }
 
         @Override  
         protected void onPreExecute() {  
             super.onPreExecute();  
             mDBMgr = new DBManager(getActivity());  
         }
         
         @Override
         protected Boolean doInBackground(String... params) {
             try {
 				if (mType == TYPE_REFRESH && mNbaData.size() > 0) {
 					mNbaData.clear();
 				}
 
 				if (mDBMgr.isEmpty()) {
 					List<Game> tmpList = WebParserUtils.parserSinaHtml(params[0]);
 					if (tmpList != null && tmpList.size() > 0) {
 						mDBMgr.addList(tmpList);
 					}
 				} else {
 					Date curDate = new Date();
 		        	Date dt = mDBMgr.timeStamp();
 		        	SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(getActivity());
 			    	String autoFresh =pre.getString(getResources().getString(R.string.pref_key_auto_fresh), "0");
 			    	int int_autoFresh = Integer.parseInt(autoFresh);
 			    	Log.d(LOG_TAG, curDate.toString());
 			    	curDate.setDate(curDate.getDate() - int_autoFresh);
 			    	Log.d(LOG_TAG, curDate.toString());
 					if (null != dt && !dt.after(curDate)) {
 						List<Game> tmpList = WebParserUtils.parserSinaHtml(params[0]);
 						if (tmpList != null && tmpList.size() > 0) {
 							mDBMgr.addList(tmpList);
 						}
 					}
 				}
 				// TODO make sure db insert will not add redundant item
 				mDBMgr.query(null, mNbaData);
                 return true;
             } catch (Exception e) {
                 // Log.e(LOG_TAG, "", e);
                 EasyTracker.getTracker().sendException("SinaTask", e, false);
                 return false;
             }
 
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             if (result) {
                 onDataFirstLoadComplete();
                 mAdapter.notifyDataSetChanged();
             } else {
                 Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
             }
             mSinaTask = null;
             getPullToRefreshListView().getLoadingLayoutProxy().setLastUpdatedLabel(
                     DateUtils.getLastUpdateLabel(getActivity()));
             getPullToRefreshListView().onRefreshComplete();
             mDBMgr.closeDB();
             mDBMgr = null;
             super.onPostExecute(result);
         }
 
         @Override
         protected void onCancelled() {
             getPullToRefreshListView().onRefreshComplete();
             mSinaTask = null;
             mDBMgr.closeDB();
             mDBMgr = null;
             super.onCancelled();
         }
 
     }
 
     @Override
     public int getContentViewId() {
         return R.layout.ptr_list_layout;
     }
 
     private class SinaAdapter extends BaseAdapter {
 
         @Override
         public int getCount() {
             return mNbaData.size();
         }
 
         @Override
         public Object getItem(int position) {
             return mNbaData.get(position);
         }
 
         @Override
         public long getItemId(int position) {
             return position;
         }
 
         @Override
         public View getView(final int position, View convertView, ViewGroup parent) {
             ViewHolder holder;
             if (convertView == null) {
                 holder = new ViewHolder();
                 convertView = LayoutInflater.from(getActivity()).inflate(R.layout.nba_list_item,
                         null);
 				holder.guest_tlogoImg = (ImageView)convertView.findViewById(R.id.guest_tlogoImg);
 				holder.home_tlogoImg = (ImageView)convertView.findViewById(R.id.home_tlogoImg);
 				holder.dateTxt = (TextView)convertView.findViewById(R.id.dateTxt);
 				holder.timeTxt = (TextView)convertView.findViewById(R.id.timeTxt);
 				holder.statusTxt = (TextView)convertView.findViewById(R.id.statusTxt);
 				holder.typeTxt = (TextView)convertView.findViewById(R.id.typeTxt);
 				holder.channelsTxt = (TextView)convertView.findViewById(R.id.channelsTxt);
 				holder.markBtn = (ImageButton)convertView.findViewById(R.id.markBtn);
                 convertView.setTag(holder);
             } else {
                 holder = (ViewHolder) convertView.getTag();
             }
             
             Game game = mNbaData.get(position);
 			holder.guest_tlogoImg.setBackgroundResource(WebParserUtils.getTeamLogoId((String)game.guest));
 			holder.home_tlogoImg.setBackgroundResource(WebParserUtils.getTeamLogoId((String)game.host));
 			holder.channelsTxt.setText((String)game.chanls);
			Date beginDate = game.dt;
			String datestr = new SimpleDateFormat("MM-dd", Locale.US).format(beginDate);
			String timestr = new SimpleDateFormat("HH:mm", Locale.US).format(beginDate);
 			holder.dateTxt.setText(datestr);
 			holder.timeTxt.setText(timestr);
 
			Calendar cal = Calendar.getInstance();
			cal.setTime(beginDate);
			cal.add(Calendar.MINUTE, 135); // 目前時間加2.25小時   
			Date endDate = cal.getTime();
			Date curDate = new Date();
			if (curDate.after(endDate))
 				holder.statusTxt.setText("完场");
			else if (curDate.before(beginDate))
 				holder.statusTxt.setText("未赛");
			//else if (curDate.after(beginDate) && curDate.before(endDate))
			else
				holder.statusTxt.setText("比赛中");
				
 			
 			String [] types = getResources().getStringArray(R.array.nba_game_types);
 			holder.typeTxt.setText(types[game.type]);
 
 			if (game.isEvent) {
 				holder.markBtn.setImageResource(R.drawable.star_orange_full);
 			} else {
 				holder.markBtn.setImageResource(R.drawable.star_blank);
 			}
 
 			holder.markBtn.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					if (!mNbaData.get(position).isEvent) {
 						settingDialog(position);
 					} else {
 						if (CalendarUtils.deleteEvent(getActivity(), mNbaData.get(position).eventId)) {
 							mNbaData.get(position).isEvent = false;
 							mNbaData.get(position).eventId = 0;							
 							SqliteTask updateTask = new SqliteTask();
 							updateTask.execute(mNbaData.get(position));
 							notifyDataSetChanged();
 						}
 					}
 				}
 			});		
 			return convertView;
 		} 		
 	}
 
 	public final class ViewHolder {
 		public ImageView guest_tlogoImg;
 		public ImageView home_tlogoImg;
 		public TextView dateTxt;
 		public TextView timeTxt;
 		public TextView statusTxt;
 		public TextView typeTxt;
 		public TextView channelsTxt;
 		public ImageButton markBtn;
 	}
 
 	public void settingDialog(final int position){
 
 		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	    final View layout = inflater.inflate(R.layout.add_event_dialog, (ViewGroup) getActivity().findViewById(R.id.dialogLayout));
 	    final TextView hintTxt = (TextView)layout.findViewById(R.id.seekbar_hint);
 		final LinearLayout ll = (LinearLayout)layout.findViewById(R.id.time_comboseekbar);
 		ComboSeekBar csb;
 		final List<String> labels = Arrays.asList(getResources().getStringArray(R.array.reminder_times));
 		final String seekbar_hint = getResources().getString(R.string.seekbar_hint); 
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setTitle(getResources().getString(R.string.reminder_dialog_title))
 		.setView(layout)
 		.setPositiveButton(getResources().getString(R.string.add_event), new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				try {
 //					ComboSeekBar mycsb = (ComboSeekBar)layout.findViewById(R.id.time_comboseekbar);
 					ComboSeekBar mycsb = (ComboSeekBar)ll.getChildAt(0);
 					int minutes = 0;
 					if (mycsb != null) {
 						String str = labels.get(mycsb.getSelectedId());
 						Log.d(LOG_TAG, "!!!!!" + str);
 						String [] ss = str.split(" ");
 						Log.d(LOG_TAG, "!!!!!" + ss[0]);
 						Log.d(LOG_TAG, "!!!!!" + ss[1]);
 						
 						if (ss[1].equals("分钟")) {
 							minutes = Integer.parseInt(ss[0]);
 						} else if (ss[1].equals("小时")) {
 							minutes = Integer.parseInt(ss[0]) * 60;
 						} else if (ss[1].equals("天")) {
 							minutes = Integer.parseInt(ss[0]) * 1440;
 						}
 					}
 					Log.d(LOG_TAG, "####" + minutes);
 					if (CalendarUtils.addEvent(getActivity(), mNbaData.get(position), minutes, mSelectedCalendarId)) {
 						SqliteTask updateTask = new SqliteTask();
 						updateTask.execute(mNbaData.get(position));
 						mAdapter.notifyDataSetChanged();
 					} 
 				} catch (Exception e) {
 					Log.e(LOG_TAG, "add event ERROR");
 				}
 			}
 		}).show();
 
 
         csb = new ComboSeekBar(getActivity());
         csb.SetStyle(Color.BLACK, 25, true);
 
         csb.setAdapter(labels);
         csb.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				String seekbar_hintFinal = String.format(seekbar_hint, labels.get(arg2));
 				hintTxt.setText(seekbar_hintFinal);
 			}
         	
         });
         ll.addView(csb);
 
 		Spinner spinner_calender = (Spinner)layout.findViewById(R.id.spinner_calendar);
 		if (mCalendars != null) {
 			ArrayAdapter<?> l_arrayAdapter = new ArrayAdapter<Object>(getActivity().getApplicationContext(), R.layout.spinner_item, mCalendars);
 			populateCalendarSpinner(spinner_calender, l_arrayAdapter);
 		}
 	}
 
 	 private void populateCalendarSpinner(Spinner spinner_calender, ArrayAdapter<?> l_arrayAdapter) {
 	    	if(l_arrayAdapter == null)
 	    		return;
 	    	l_arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
 	    	spinner_calender.setAdapter(l_arrayAdapter);
 	    	
 	    	SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(getActivity());
 	    	String default_calendar_id =pre.getString(getResources().getString(R.string.pref_key_default_calendar), "0");
 
 	    	for(int i = 0; i < l_arrayAdapter.getCount(); i++)
 	    	{
 	    		MyCalendar cal = (MyCalendar) l_arrayAdapter.getItem(i);
 	    		if (default_calendar_id.equals(cal.id)) {
 	    			spinner_calender.setSelection(i);
 	    			break;
 	    		}
 	    	}
 	    	spinner_calender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 	    		@Override
 	    		public void onItemSelected(AdapterView<?> p_parent, View p_view,
 	    				int p_pos, long p_id) {
 	    			mSelectedCalendarId = mCalendars[(int)p_id].id;
 	    		}
 	    		@Override
 	    		public void onNothingSelected(AdapterView<?> arg0) {}
 	    	});
 	    }
 
     @Override
     public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
         EasyTracker.getTracker().sendEvent("ui_action", "list_item_long_click",
                 "sina_list_fragmentt_list_item_long_click", 0L);
         Game entity = (Game) mAdapter.getItem(position - 1);
 //        openDiscuss(entity);
         return true;
     }
 
     
 }
