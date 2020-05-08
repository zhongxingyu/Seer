 package com.kosbrother.fragments;
 
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import com.ifixit.android.sectionheaders.Section;
 import com.ifixit.android.sectionheaders.SectionHeadersAdapter;
 import com.ifixit.android.sectionheaders.SectionListView;
 import com.novel.reader.ArticleActivity;
 import com.novel.reader.BookmarkActivity;
 import com.novel.reader.NovelIntroduceActivity;
 import com.novel.reader.R;
 import com.novel.reader.api.NovelAPI;
 import com.novel.reader.entity.Bookmark;
 import com.taiwan.imageload.ImageLoader;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MyBookmarkFragment extends Fragment{
 	
 	private SectionListView bookmarkListView;
 	private int isRecent;
 	private ArrayList<String> arrayKey;
 	private ArrayList<Bookmark> bookmarks;
 	private ArrayList<Bookmark> deleteBookmarks;
 	private TreeMap<String, ArrayList<Bookmark>> bookmarksMap;
 	public static int BOOKMARK_VIEW = 1;
 	public static int RECENT_READ_VIEW = 2;
 	public boolean isShowDeleteCallbackAction = false;
 	private Activity mActivity;
 	
 	@Override
 	  public void onAttach(Activity activity) {
 	    super.onAttach(activity);
 	    mActivity= activity;
 	  }
 	
 	public static MyBookmarkFragment newInstance(int isRecent) {
 
 		MyBookmarkFragment fragment = new MyBookmarkFragment();
         Bundle bdl = new Bundle();
         bdl.putInt("IS_RECNET", isRecent);
         fragment.setArguments(bdl);
         return fragment;
 
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         isRecent = getArguments().getInt("IS_RECNET");
     }
     
     @Override
 	public void onResume() {
         super.onResume();
         new LoadDataTask().execute();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         View myFragmentView = inflater.inflate(R.layout.layout_bookmark, container, false);
         bookmarkListView = (SectionListView) myFragmentView.findViewById(R.id.bookmark_listview);
         bookmarkListView.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
         
         return myFragmentView;
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
     }
 
     class LoadDataTask extends AsyncTask<Integer, Integer, String> {
 
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
         }
 
         @Override
         protected String doInBackground(Integer... params) {
             fetchData();
             return "progress end";
         }
 
         @Override
         protected void onProgressUpdate(Integer... progress) {
             super.onProgressUpdate(progress);
         }
 
         @Override
         protected void onPostExecute(String result) {
             setListAdatper();
         }
 
     }
     
     private void setListAdatper() {
         SectionHeadersAdapter adapter = new SectionHeadersAdapter();
 
         for (int i = 0; i < arrayKey.size(); i++) {
         	adapter.addSection(new BookmarkSectionAdapter(mActivity, bookmarksMap.get(arrayKey.get(i)), arrayKey.get(i)));
         }
         bookmarkListView.setAdapter(adapter);
         bookmarkListView.getListView().setOnItemClickListener(adapter);
         bookmarkListView.getListView().setOnItemLongClickListener(adapter);
     }
 
     private void fetchData() {
         if (isRecent == RECENT_READ_VIEW)
             bookmarks = NovelAPI.getAllRecentReadBookmarks(mActivity);
         else
             bookmarks = NovelAPI.getAllBookmarks(mActivity);
 
         if (bookmarks.size() == 0)
            bookmarks.add(new Bookmark(0, 0, 0, 0, getResources().getString(R.string.my_bookmark_none), "", "", false));
         bookmarksMap = getBookmarksMap(bookmarks);
     }
 
     private TreeMap<String, ArrayList<Bookmark>> getBookmarksMap(ArrayList<Bookmark> bs) {
         arrayKey = new ArrayList<String>();
         TreeMap bookMap = new TreeMap<String, ArrayList<Bookmark>>();
         for (int i = 0; i < bs.size(); i++) {
             Bookmark bookmark = bs.get(i);
             // 先確認key是否存在
             if (bookMap.containsKey(bookmark.getNovelName())) {
                 // 已經有的話就把movie加進去
                 ((ArrayList<Bookmark>) bookMap.get(bookmark.getNovelName())).add(bookmark);
             } else {
                 // 沒有的話就建一個加進去
                 ArrayList<Bookmark> newBookList = new ArrayList<Bookmark>(10);
                 newBookList.add(bookmark);
                 arrayKey.add(bookmark.getNovelName());
                 bookMap.put(bookmark.getNovelName(), newBookList);
             }
         }
         return bookMap;
     }
     
     public class BookmarkSectionAdapter extends Section implements Filterable {
 
         private Context             mContext;
         private ArrayList<Bookmark> bookList;
         private ImageLoader         imageLoader;
         private String              headerString = "";
 
         public BookmarkSectionAdapter(Context context, ArrayList<Bookmark> bookList, String headerString) {
             this.mContext = context;
             this.bookList = bookList;
             this.headerString = headerString;
             imageLoader = new ImageLoader(mContext);
         }
 
         public int getCount() {
             return bookList.size();
         }
 
         public Object getItem(int position) {
             return bookList.get(position);
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(final int position, View convertView, ViewGroup parent) {
             LayoutInflater myInflater = LayoutInflater.from(mContext);
             View converView = myInflater.inflate(R.layout.listview_bookmarks, null);
 
             ImageView poster = (ImageView) converView.findViewById(R.id.bookmark_poster);
             TextView novelName = (TextView) converView.findViewById(R.id.bookmark_novel_name);
             TextView articleTitle = (TextView) converView.findViewById(R.id.bookmark_article_name);
             Button novelBtn = (Button) converView.findViewById(R.id.novel_introduce_btn);
            if (bookList.get(position).getNovelName().equals(getResources().getString(R.string.my_bookmark_none)))
                 novelBtn.setVisibility(View.GONE);
             novelBtn.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Bundle bundle = new Bundle();
                     bundle.putInt("NovelId", bookList.get(position).getNovelId());
                     bundle.putString("NovelName", bookList.get(position).getNovelName());
                     bundle.putString("NovelAuthor", "");
                     bundle.putString("NovelDescription", "");
                     bundle.putString("NovelUpdate", "");
                     bundle.putString("NovelPicUrl", bookList.get(position).getNovelPic());
                     bundle.putString("NovelArticleNum", "");
                     Intent intent = new Intent();
                     intent.putExtras(bundle);
                     intent.setClass(mContext, NovelIntroduceActivity.class);
                     startActivity(intent);
                 }
             });
             novelName.setText(bookList.get(position).getNovelName());
             articleTitle.setText(bookList.get(position).getArticleTitle());
 
             imageLoader.DisplayImage(bookList.get(position).getNovelPic(), poster);
 
             return converView;
         }
 
         @Override
         public Object getHeaderItem() {
             return headerString;
         }
 
         @Override
         public View getHeaderView(View convertView, ViewGroup parent) {
             TextView header = (TextView) convertView;
 
             if (header == null) {
                 header = new TextView(mContext);
                 header.setTextSize(16);
                 header.setPadding(12, 1, 2, 1);
                 header.setTypeface(Typeface.SERIF);
                 header.setBackgroundColor(mContext.getResources().getColor(R.color.bookmark_section_head));
                 header.setTextColor(mContext.getResources().getColor(R.color.bookmark_section_text));
             }
             header.setText(headerString);
 
             return header;
         }
 
         public Filter getFilter() {
             return null;
         }
 
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
         	if(isShowDeleteCallbackAction){
         		if (deleteBookmarks.contains((Bookmark) getItem(position))){
         			deleteBookmarks.remove((Bookmark) getItem(position));
         		}else{
         			deleteBookmarks.add((Bookmark) getItem(position));
         		}
         	}else{
         		((BookmarkActivity)MyBookmarkFragment.this.mActivity).closeActionMode();
         		
         		Bookmark bookmark = bookList.get(position);
 	              if (bookmark.getId() == 0)
 	                  return;
 	              Intent newAct = new Intent();
 	              newAct.putExtra("NovelName", bookmark.getNovelName());
 	              newAct.putExtra("ArticleTitle", bookmark.getArticleTitle());
 	              newAct.putExtra("ArticleId", bookmark.getArticleId());
 	              newAct.putExtra("ReadingRate", bookmark.getReadRate());
 	              newAct.putExtra("NovelPic", bookmark.getNovelPic());
 	              newAct.putExtra("NovelId", bookmark.getNovelId());
 	              newAct.setClass(mContext, ArticleActivity.class);
 	              startActivity(newAct);
         	}
         }
 
         @Override
         public void onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
         	if(isShowDeleteCallbackAction){
         		if (deleteBookmarks.contains((Bookmark) getItem(position))){
         			deleteBookmarks.remove((Bookmark) getItem(position));
         		}else{
         			deleteBookmarks.add((Bookmark) getItem(position));
         		}
         	}else{
         		((BookmarkActivity)MyBookmarkFragment.this.mActivity).showCallBackAction();
         		isShowDeleteCallbackAction = true;
         		deleteBookmarks = new ArrayList<Bookmark>();
         	}
         	
         }
         
         
     }
 
 	public void deleteAndReload() {
 		isShowDeleteCallbackAction = false;
 		if(deleteBookmarks!=null)
 			NovelAPI.deleteBookmarks(deleteBookmarks, this.mActivity);
 		new LoadDataTask().execute();
 	}
 	
 	public void resetIsShowDeleteCallbackAction(){
 		isShowDeleteCallbackAction = false;
 		new LoadDataTask().execute();
 	}
 
     
 
 }
