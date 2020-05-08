 package com.twocity.bookworm;
 
 import com.twocity.bookworm.service.UpdateIntentService;
 import com.twocity.bookworm.utils.BookJsonParser;
 import com.twocity.bookworm.utils.Books;
 import com.twocity.bookworm.utils.ImageDownloader;
 import com.twocity.bookworm.utils.PreferenceUtils;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.ScaleGestureDetector.OnScaleGestureListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SearchActivity extends DashboardActivity implements OnClickListener,OnScrollListener
 {
     private final static String TAG = "BookWorm";
     
     private ProgressBar mProgressBar = null;
     private EditText mSearchEdit = null;
     private ImageButton mSearchButton = null;
     private ArrayList<Books> bookList = null;
     private ListView searchList = null;
     private SearchBookAdapter mAdapter = null;
     //private TextView mLoadMoreText = null;
     
     //search args
     private boolean loadMore = false;
     private String searchArgs = "";
     private int start_index = 1;
     private final static int INCRECEMENT = 15;
     
     protected void onCreate(Bundle savedInstanceSaved) {
         super.onCreate(savedInstanceSaved);
         setContentView(R.layout.activity_search);
         
         bookList = new ArrayList<Books>();
         initViews();
     }
     
     private void initViews() {
         mSearchEdit = (EditText) findViewById(R.id.book_search);
         mSearchEdit.setOnKeyListener(new OnKeyListener() {
             public boolean onKey(View v,int keyCode,KeyEvent event) {
                 if(keyCode == KeyEvent.KEYCODE_ENTER && 
                         event.getAction() == KeyEvent.ACTION_DOWN) {
                     start_index = 1;
                     startSearch(start_index,false);
                     return true;
                 }
                 return false;
             }
         });
         mSearchButton = (ImageButton) findViewById(R.id.search_button);
         mSearchButton.setOnClickListener(this);
         mProgressBar = (ProgressBar)findViewById(R.id.search_progressbar);
 
         
         //set list view
         searchList = (ListView)findViewById(R.id.search_book_list);
         searchList.setOnItemClickListener(myClickListener);
         mAdapter = new SearchBookAdapter(SearchActivity.this,bookList);
         
         //View footView = this.getLayoutInflater().inflate(R.layout.load_more_layout, null);
         // foot tips
         //mLoadMoreText = (TextView)footView.findViewById(R.id.load_more_textview);
         //searchList.addFooterView(footView);
         searchList.setAdapter(mAdapter);
         searchList.setOnScrollListener(this);
     }
     
     private BroadcastReceiver broadreceiver = new BroadcastReceiver() {
         
         @Override
         public void onReceive(Context context, Intent intent) {
             if(intent.getAction().equals(PreferenceUtils.ACTION_SEARCH_BOOK_COMPLETE)){
                 String json = intent.getStringExtra(PreferenceUtils.SEARCH_BOOK_JSON);
                 ArrayList<Books> fetchlist = BookJsonParser.Parser(json);
                 if(fetchlist != null){
                     updateList(fetchlist);
                 }else{
                     Toast.makeText(SearchActivity.this, R.string.update_book_failed, Toast.LENGTH_SHORT).show();
                 }
 
             }
         }
     };
     
     private void startSearch(int index,boolean loadmore){
         String nowSearchArgs = mSearchEdit.getText().toString().trim();
        if(nowSearchArgs.equals(searchArgs) && !loadmore){
             bookList.clear();
             mAdapter.notifyDataSetChanged();
        }else{
            searchArgs = nowSearchArgs;
         }

         Intent intent = new Intent(SearchActivity.this,UpdateIntentService.class);
         intent.setAction(PreferenceUtils.ACTION_SEARCH_BOOK);
         intent.putExtra(PreferenceUtils.SEARCH_ARG_Q, searchArgs);
         intent.putExtra(PreferenceUtils.SEARCH_ARG_START_INDEX,String.valueOf(index));
         SearchActivity.this.startService(intent);
         
         mProgressBar.setVisibility(View.VISIBLE);
     }
     
     private void updateList(ArrayList<Books> list){
         for(Books item:list){
             bookList.add(item);
         }
         mAdapter.notifyDataSetChanged();
         mProgressBar.setVisibility(View.INVISIBLE);
     }
     
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.search_button:
                 start_index = 1;
                 startSearch(start_index,false);
                 break;
             default:
                 break;
         }
     }
     
     @Override
     public void onScroll(AbsListView view,int firstVisible, int visibleCount, int totalCount) {
         loadMore = firstVisible + visibleCount >= totalCount;
     }
     
     @Override
     public void onScrollStateChanged(AbsListView v, int s){
         if(s == OnScrollListener.SCROLL_STATE_IDLE && loadMore){
             Log.d(TAG,"=== load more ===");
             start_index += INCRECEMENT;
             startSearch(start_index,true);
             //mLoadMoreText.setText(getResources().getString(R.string.loading));
         }
     }
 
 
     
     @Override
     protected void onStart(){
          super.onStart();
          IntentFilter filter = new IntentFilter();
          filter.addAction(PreferenceUtils.ACTION_SEARCH_BOOK_COMPLETE);
          registerReceiver(broadreceiver,filter);
     }
     @Override
     protected void onStop(){
         super.onStop();
         unregisterReceiver(broadreceiver);
     }
     
     final OnItemClickListener myClickListener = new OnItemClickListener()
     {
         @Override
         public void onItemClick(AdapterView<?> a, View view, int position, long id){
             Intent i = new Intent(SearchActivity.this,BookDetail.class);
             Books book = (Books)searchList.getAdapter().getItem(position);
             if(book != null){
                 String url = book.getBookLink();
                 i.putExtra(PreferenceUtils.BOOK_DETAIL_LINK, url);
                 i.putExtra(PreferenceUtils.BOOK_DETAIL_TITLE, book.getBookTitle());
                 startActivity(i);
                 Log.d(TAG,"=== SearchActivity to BookDetail ===");
             }
 
         }
     };
     
     final private class SearchBookAdapter extends BaseAdapter{
         
         private Context mContext;
         private LayoutInflater mInflater;
         private List<Books> mBookList = new ArrayList<Books>();
         private final ImageDownloader imageDownloader = new ImageDownloader();
         
         public SearchBookAdapter(Context context,ArrayList<Books> books){
             mContext = context;
             mBookList = books;
         }
         
         public int getCount(){
             return mBookList.size();
         }
         
         public Object getItem(int position){
             return mBookList.get(position);
         }
         
         public long getItemId(int position){
             return position;
         }
         
 //        public void addItem(Books item){
 //            mBookList.add(item);
 //        }
         
         public View getView(int position, View convertView, ViewGroup parent) {
 
             ViewHolder holder = new ViewHolder();
             if(convertView == null){
                 mInflater = LayoutInflater.from(mContext);  
                 convertView = mInflater.inflate(R.layout.bookitem, null);
                 
                 holder.coverImage = (ImageView)convertView.findViewById(R.id.book_image);
                 holder.titleText = (TextView)convertView.findViewById(R.id.book_title);
                 holder.authorText = (TextView)convertView.findViewById(R.id.book_author);
       
                 convertView.setTag(holder);  
             }else{
                 holder = (ViewHolder)convertView.getTag();
             }
             
             
             Books item = mBookList.get(position);
 
             String imagelink = item.getBookImgaeLink();
             imageDownloader.download(imagelink, (ImageView) holder.coverImage);
             holder.titleText.setText(item.getBookTitle());
             holder.authorText.setText(item.getBookAuthor());
             
             return convertView;
         }
         
         private class ViewHolder{
             TextView titleText;
             TextView authorText;
             ImageView coverImage;
         }
     }
 }
