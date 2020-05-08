 package com.fatjay.subfunction;
 
 import java.util.ArrayList;
 
 import com.fatjay.R;
 import com.fatjay.function.hotpot;
 import com.fatjay.main.LilyActivity;
 import com.fatjay.main.userinfo;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class hotThread extends ListActivity {
 	hotpot mHotpot;
 	BaseAdapter mAdapter = new hotAdapter(this);
 	userinfo mUserinfo;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.hotlist);
 		mUserinfo = (userinfo) getApplication();
 		mHotpot = (hotpot)getParent();
 		int page = mHotpot.pageid;
 		if (mHotpot.data.get(Integer.valueOf(page))==null) {
 			((hotAdapter)mAdapter).changeData(null);
 		} else {
 			((hotAdapter)mAdapter).changeData(mHotpot.data.get(Integer.valueOf(page)));
 		}
 		setListAdapter(mAdapter);
 		getListView().setOnCreateContextMenuListener(this);
 	}
 	
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		// TODO Auto-generated method stub
 		super.onListItemClick(l, v, position, id);
 		System.out.println("clicked!");
 		String[] temp = ((hotAdapter)mAdapter).list.get(position).split("#");
 		String url = temp[0];
 		Intent startread = new Intent(hotThread.this, threadContent.class);
 		Bundle mBundle = new Bundle();
 		mBundle.putString("url", url);
 		mBundle.putString("title", temp[1]);
 		startread.putExtras(mBundle);
 		hotThread.this.startActivity(startread);
 	}
 
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		int menuInfo = ((AdapterContextMenuInfo)item.getMenuInfo()).position;
 		String[] tempStrings = ((hotAdapter)mAdapter).list.get(menuInfo).split("#");
 		Intent mIntent = null;
 		Bundle mBundle = null;
 		switch (item.getItemId()) {
 		case 0:
 			mIntent = new Intent(hotThread.this, threadList.class);
 			mBundle = new Bundle();
 			mBundle.putString("url", "http://bbs.nju.edu.cn/bbstdoc?board=" + tempStrings[3]);
 			mIntent.putExtras(mBundle);
 			hotThread.this.startActivity(mIntent);
 			break;
 		case 1:
 			String url = tempStrings[0];
 			Intent startread = new Intent(hotThread.this, threadContent.class);
 			mBundle = new Bundle();
 			mBundle.putString("url", url);
 			mBundle.putString("title", tempStrings[1]);
 			mBundle.putString("board", mUserinfo.boardname.get(tempStrings[3]));
 			startread.putExtras(mBundle);
 			hotThread.this.startActivity(startread);
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		// TODO Auto-generated method stub
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.setHeaderTitle("");
 		menu.add(0, 0, 0, "תڰ");
 		menu.add(0, 1, 1, "Ķƪ");
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	        if(keyCode == KeyEvent.KEYCODE_BACK){
 	        	LilyActivity.instance.switchActivity(0);
 	            return true;
 	        }else{
 	            return super.onKeyDown(keyCode, event);
 	        }
 	    }
 
 
 	private class hotAdapter extends BaseAdapter {
 		ArrayList<String> list = null;
 		Context context;
 		
 		public hotAdapter(Context mContext) {
 			// TODO Auto-generated constructor stub
 			context = mContext;
 		}
 		
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			if (list == null) {
 				return 0;
 			} else {
 				return list.size();
 			}
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			// TODO Auto-generated method stub
 			return arg0;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			// TODO Auto-generated method stub
 			return position;
 		}
 		
 		 public void changeData(ArrayList<String> data) { 
 			if (data == null) {
 				list = null;
 				notifyDataSetChanged();
 			} else {
 				ArrayList<String> clone = (ArrayList<String>) data.clone();
 				list = clone; 
 				notifyDataSetChanged();
 			}
 			
 		 }
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			hotView cv = null;
 			String[] strings = null;
             if (convertView == null) {
                 strings = (list.get(position)).split("#");
                 String title = strings[1];
                 String board = strings[3];
                 cv = new hotView(context, title, board);
             } else {
             	strings = (list.get(position)).split("#");
                 String title = strings[1];
                 String board = strings[3];
                 cv = (hotView) convertView;
                 cv.setTitle(title);
                 cv.setBoard(board);
             }
             return cv;
 		}
 		
 		class hotView extends LinearLayout {
 			private TextView titleTextView;
 			private TextView boardTextView;
 			
 			public hotView(Context context, String title, String board) {
 				super(context);
 				this.setOrientation(VERTICAL);
 				LinearLayout toprow_layout = new LinearLayout(context);
 				toprow_layout.setOrientation(VERTICAL);
 				
 				titleTextView = new TextView(context);
 				titleTextView.setText(title);
 				titleTextView.setTextSize(22);
 				titleTextView.setTextColor(Color.BLACK);
 				boardTextView = new TextView(context);
 				boardTextView.setText(": " + mUserinfo.boardname.get(board));
 				boardTextView.setTextSize(14);
 				
 				toprow_layout.addView(titleTextView);
 				toprow_layout.addView(boardTextView);
 				addView(toprow_layout, new LinearLayout.LayoutParams(
 	                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 			}
 			
 			public void setTitle(String title) {
 				titleTextView.setTextColor(Color.BLACK);
 				titleTextView.setText(title);
 	        }
 			
 			public void setBoard(String board) {
 				boardTextView.setText(": " + mUserinfo.boardname.get(board));
 			}
 		}
 	}
 }
