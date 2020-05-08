 package com.withparadox2.whut.library.search;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.view.GestureDetector;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnGroupClickListener;
 import android.widget.ExpandableListView.OnGroupCollapseListener;
 import android.widget.ExpandableListView.OnGroupExpandListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.IntentAction;
 import com.withparadox2.whut.LiXianHomeActivity;
 import com.withparadox2.whut.R;
 import com.withparadox2.whut.dao.WhutGlobal;
 
 public class SearchBookActivity extends Activity implements OnScrollListener{
 	
 	private HttpSearchThread myThread;
 	private UpdaetUIHandler myHandler;
 	private ExpandableListView expandableListView;
 	private MyExpandableListAdapter myExpandableAdapter;
 	private ActionBar actionbar;
 	private Button searchBookButton;
 	private EditText searchBookEdittext;
 	
 	public static int page = 1;
 	private boolean loadingBooksFlag = false;
 	private boolean childExpandedFlag = false;//if expanded then lastItem - 2, else lastItem - 1
 	private int lastChildExpandedPosition = -1;
 	
 	private View moreView;
 	private int lastItem;
 
 	public final static int GET_HTML = 1;
 	public final static int UPDATE_GROUP = 2;
 	public final static int NO_BOOKS = 3;
 	public final static int UPDATE_CHILD = 4;
 	
 	public final static int UPDATE_GROUP_THREAD = 1;
 	public static final int UPDATE_CHILD_THREAD = 2;
 	
 	private int clickPointX;
 	private int clickPointY;
 	
 	private GestureDetector myGestureDetector;
 	private View onTouchView;
 	
 	private BookListDatabase mDbHelper;
 	
 	private Map<Integer, Boolean> addToDatabaseFlag = new HashMap<Integer, Boolean>();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.book_expandable_hold);
 		moreView = getLayoutInflater().inflate(R.layout.footer, null);
 		expandableListView = (ExpandableListView)findViewById(R.id.expandable_listview);
 		searchBookButton = (Button)findViewById(R.id.search_book_button);
 		actionbar = (ActionBar)findViewById(R.id.search_book_actionbar);
 		actionbar.setHomeAction(new IntentAction(this, LiXianHomeActivity.createIntent(this), R.drawable.ic_actionbar_whut));
 		actionbar.setDisplayHomeAsUpEnabled(true);
 	    actionbar.setTitle("ͼ");
 		searchBookEdittext = (EditText)findViewById(R.id.search_book_edittext);
 		expandableListView.setGroupIndicator(null);
 		myExpandableAdapter = new MyExpandableListAdapter();
 		expandableListView.addFooterView(moreView);
 		expandableListView.setAdapter(myExpandableAdapter);
 		expandableListView.setOnGroupClickListener(new MyOnGroupClickListener());
 		expandableListView.setOnGroupCollapseListener(new MyOnGroupCollapseListener());
 		expandableListView.setOnGroupExpandListener(new MyOnGroupExpandListener());
 		expandableListView.setOnScrollListener(this);
 		myHandler = new UpdaetUIHandler(Looper.myLooper());
 		mDbHelper = new BookListDatabase(this);
 		
 		searchBookButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				InputMethodManager inputManager = (InputMethodManager)
                         getSystemService(Context.INPUT_METHOD_SERVICE); 
 
 				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
 
 				WhutGlobal.BOOKLIST.clear();
 				WhutGlobal.CHILDLIST.clear();
 				WhutGlobal.CLICK_GROUP_FLAG.clear();
 				WhutGlobal.SEARCH_TITLE = searchBookEdittext.getText().toString();
				page = 1;
 				expandableListView.scrollTo(0, 0);
 				myExpandableAdapter.notifyDataSetChanged();
 				myThread = new HttpSearchThread(myHandler, page);
 				WhutGlobal.WhichAction = UPDATE_GROUP_THREAD;
 				myThread.start();
 			}
 		});
 	}
 	
 	
 	private void updateData(){
 		myExpandableAdapter.notifyDataSetChanged();
 		moreView.setVisibility(View.GONE);
 		loadingBooksFlag = false;
 	}
 	
 	
 	class UpdaetUIHandler extends Handler{
 		public UpdaetUIHandler(Looper myLooper){
 			super(myLooper);
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			// TODO Auto-generated method stub
 			switch(msg.arg1){
 			case GET_HTML:
 				Toast.makeText(SearchBookActivity.this, "", Toast.LENGTH_SHORT).show();
 				break;
 			case NO_BOOKS:
 				Toast.makeText(SearchBookActivity.this, "δҵ鼮", Toast.LENGTH_SHORT).show();
 				moreView.setVisibility(View.GONE);
 				loadingBooksFlag = false;
 				break;
 			case UPDATE_GROUP:	
 				updateData();
 				break;
 			case UPDATE_CHILD://update child
 				myExpandableAdapter.notifyDataSetChanged();
 				break;
 				
 			}
 		}
 		
 		
 	}
 	
 	class MyOnGroupClickListener implements OnGroupClickListener{
 
 		@Override
 		public boolean onGroupClick(ExpandableListView parent, View v,
 				int groupPosition, long id) {
 			// TODO Auto-generated method stub
 			if(!WhutGlobal.CLICK_GROUP_FLAG.get(groupPosition)){
 				WhutGlobal.BOOK_CODE = WhutGlobal.BOOKLIST.get(groupPosition)[4];
 				WhutGlobal.BOOK_CODE_POS = groupPosition;
 				WhutGlobal.WhichAction = UPDATE_CHILD_THREAD;
 				myThread = new HttpSearchThread(myHandler);
 				myThread.start();
 				WhutGlobal.CLICK_GROUP_FLAG.set(groupPosition, true);
 			}
 		
 //			System.out.println("");
 			return false;
 			
 		}
 		
 	}
 	
 	class MyOnGroupCollapseListener implements OnGroupCollapseListener{
 
 		@Override
 		public void onGroupCollapse(int groupPosition) {
 			// TODO Auto-generated method stub
 			if(lastChildExpandedPosition == groupPosition){
 				lastChildExpandedPosition = -1;
 				childExpandedFlag = false;
 //				System.out.println("Collapse fired");
 			}
 		}
 		
 	}
 	
 	class MyOnGroupExpandListener implements OnGroupExpandListener{
 
 		@Override
 		public void onGroupExpand(int groupPosition) {
 			// TODO Auto-generated method stub
 			
 			if(lastChildExpandedPosition != -1&&groupPosition!=lastChildExpandedPosition){
 				expandableListView.collapseGroup(lastChildExpandedPosition);
 				System.out.println("Expand fired");
 			}
 			lastChildExpandedPosition = groupPosition;
 		}
 		
 	}
 	
 	
 	
 	class MyExpandableListAdapter extends BaseExpandableListAdapter{
 
 		@Override
 		public Object getChild(int arg0, int arg1) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public long getChildId(int arg0, int arg1) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getChildView(int groupPosition, int childPosition,
                 boolean isLastChild, View convertView, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			ChildViewHolder viewHolder = null;
 			if(convertView == null){
 				convertView = LayoutInflater.from(SearchBookActivity.this).inflate(R.layout.book_expandable_child, null);
 	            viewHolder = new ChildViewHolder();
 	            viewHolder.bookLocation = (TextView)convertView.findViewById(R.id.book_location);
 				viewHolder.bookCode = (TextView)convertView.findViewById(R.id.book_code);
 				convertView.setTag(viewHolder);
 			}else{
 				viewHolder = (ChildViewHolder) convertView.getTag();
 			}
 			viewHolder.bookLocation.setText(WhutGlobal.CHILDLIST.get(groupPosition).get(childPosition)[1]);
 			viewHolder.bookCode.setText(WhutGlobal.CHILDLIST.get(groupPosition).get(childPosition)[0]);
 			
 			return convertView;
 		}
 
 		@Override
 		public int getChildrenCount(int arg0) {
 			// TODO Auto-generated method stub
 			return WhutGlobal.CHILDLIST.get(arg0).size();
 		}
 
 		@Override
 		public Object getGroup(int arg0) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public int getGroupCount() {
 			// TODO Auto-generated method stub
 			return WhutGlobal.BOOKLIST.size();
 		}
 
 		@Override
 		public long getGroupId(int arg0) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getGroupView(int groupPosition, boolean isExpanded,
                 View convertView, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			GroupViewHolder viewHolder = null;
 			if(convertView == null){
 				convertView = LayoutInflater.from(SearchBookActivity.this).inflate(R.layout.book_expandable_group, null);
 	            viewHolder = new GroupViewHolder();
 	            viewHolder.bookTitle = (TextView)convertView.findViewById(R.id.book_title);
 				viewHolder.author = (TextView)convertView.findViewById(R.id.author);
 				viewHolder.publisher = (TextView)convertView.findViewById(R.id.publisher);
 				viewHolder.bookCode = (TextView)convertView.findViewById(R.id.book_code);
 				viewHolder.childFlag = (TextView)convertView.findViewById(R.id.get_child_flag);
 				viewHolder.addToDbButton = (Button)convertView.findViewById(R.id.add_book_button);
 				convertView.setTag(viewHolder);
 			}else{
 				viewHolder = (GroupViewHolder) convertView.getTag();
 			}
 			viewHolder.author.setText("    :  "+WhutGlobal.BOOKLIST.get(groupPosition)[1]);
 			viewHolder.bookTitle.setText("    :  "+WhutGlobal.BOOKLIST.get(groupPosition)[0]);
 			viewHolder.publisher.setText(":  "+WhutGlobal.BOOKLIST.get(groupPosition)[2]);
 			viewHolder.bookCode.setText(":  "+WhutGlobal.BOOKLIST.get(groupPosition)[3]);
 			viewHolder.childFlag.setBackgroundColor(addToDatabaseFlag.get(groupPosition)==null ? Color.WHITE : 
 				(addToDatabaseFlag.get(groupPosition) ? Color.RED : Color.WHITE));
 			viewHolder.addToDbButton.setOnClickListener(new AddClickListener(groupPosition));
 			return convertView;
 		}
 
 		@Override
 		public boolean hasStableIds() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isChildSelectable(int arg0, int arg1) {
 			// TODO Auto-generated method stub
 			return true;
 		}
 		
 		public class GroupViewHolder{
 			TextView bookTitle;
 			TextView author;
 			TextView publisher;
 			TextView bookCode;
 			TextView childFlag;
 			Button addToDbButton;
 		}
 		
 		public class ChildViewHolder{
 			TextView bookCode;
 			TextView bookLocation;
 		}
 		
 		public class AddClickListener implements OnClickListener{
 
 			int groupPosition;
 			public AddClickListener(int groupPosition) {
 				this.groupPosition = groupPosition;
 			}
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				System.out.println("click"+groupPosition);
 				if(mDbHelper.checkWhetherAddBefore(groupPosition)){
 					mDbHelper.addGroupAndChildByPosition(groupPosition);
 					addToDatabaseFlag.put(groupPosition, true);
 				}else{
 					Toast.makeText(SearchBookActivity.this, "Ѿӹ", Toast.LENGTH_SHORT).show();
 					mDbHelper.deleteGroupAndChildByPosition(groupPosition);
 					addToDatabaseFlag.put(groupPosition, false);
 				}
 				myExpandableAdapter.notifyDataSetChanged();
 				
 			}
 			
 		}
 	}
 	
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		// TODO Auto-generated method stub
 		   lastItem = firstVisibleItem + visibleItemCount - 1; 
 //		System.out.println(childExpandedFlag+"===="+firstVisibleItem+"===="+visibleItemCount+"===="+lastItem);
 	}
 
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		// TODO Auto-generated method stub
 		int size;
 		if(childExpandedFlag){
 			 size = WhutGlobal.BOOKLIST.size()+WhutGlobal.CHILDLIST.get(lastChildExpandedPosition).size();
 		}else{
 			size = WhutGlobal.BOOKLIST.size();
 		}
 		if(lastItem == size  && scrollState == this.SCROLL_STATE_IDLE){ 
             moreView.setVisibility(view.VISIBLE);
             if(!loadingBooksFlag){
 	            loadMoreBooks();
         }
     }
 		
 	}
 	
 	private void loadMoreBooks(){
 		WhutGlobal.WhichAction = UPDATE_GROUP_THREAD;
 		loadingBooksFlag = true;
 		page = page + 1;
 		myThread = new HttpSearchThread(myHandler, page);
 		myThread.start();
 	}
 
 }
