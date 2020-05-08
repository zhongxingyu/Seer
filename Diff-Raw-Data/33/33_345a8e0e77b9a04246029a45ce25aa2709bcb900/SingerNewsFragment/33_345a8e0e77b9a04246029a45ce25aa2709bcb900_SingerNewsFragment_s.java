 package com.kosbrother.fragment;
 
 import java.util.ArrayList;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.costum.android.widget.LoadMoreListView;
 import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
 import com.kosbrother.lyric.R;
 import com.kosbrother.lyric.api.LyricAPI;
 import com.kosbrother.lyric.entity.SingerNews;
 import com.taiwan.imageload.ListNewsAdapter;
 
 
 public class SingerNewsFragment extends Fragment {
 	
 	public  int myPage = 0;
 	private Boolean checkLoad = true;
 	private LinearLayout progressLayout;
 	private LinearLayout reloadLayout;
 	private LoadMoreListView myList;
 	private Button buttonReload;
 	private ListNewsAdapter mdapter;
 	private ArrayList<SingerNews> mSingerNews;
 	private ArrayList<SingerNews> moreSingerNews = new ArrayList<SingerNews>();
 //	private static int singerId;
	private static String singerName;
 	
     public static SingerNewsFragment newInstance(String singer_name) {
 
    	SingerNewsFragment fragment = new SingerNewsFragment();
//    	singerId = singer_id;
    	singerName = singer_name;
        return fragment;

     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         View myFragmentView = inflater.inflate(R.layout.loadmore, container, false);
         progressLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_progress);
     	reloadLayout = (LinearLayout) myFragmentView.findViewById(R.id.layout_reload);
     	buttonReload = (Button) myFragmentView.findViewById(R.id.button_reload);
     	myList = (LoadMoreListView) myFragmentView.findViewById(R.id.news_list);
     	myList.setOnLoadMoreListener(new OnLoadMoreListener() {
 			public void onLoadMore() {
 				// Do the work to load more items at the end of list
 				if(checkLoad){
 					myPage = myPage +1;
 					new LoadMoreTask().execute();
 				}else{
 					myList.onLoadMoreComplete();
 				}
 			}
 		});
     	
     	buttonReload.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 progressLayout.setVisibility(View.VISIBLE);
                 reloadLayout.setVisibility(View.GONE);
                 new DownloadChannelsTask().execute();
             }
         });
         
         if (mdapter != null) {
             progressLayout.setVisibility(View.GONE);
             myList.setAdapter(mdapter);
         } else {
             new DownloadChannelsTask().execute();
         }
         
         return myFragmentView;
     }
     
     private class DownloadChannelsTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             // TODO Auto-generated method stub
             super.onPreExecute();
             
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             // TODO Auto-generated method stub        	
         	mSingerNews = LyricAPI.getNews(singerName, myPage);
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             // TODO Auto-generated method stub
             super.onPostExecute(result);
             progressLayout.setVisibility(View.GONE);
                        
             if(mSingerNews !=null && mSingerNews.size()!=0){
           	  try{
           		mdapter = new ListNewsAdapter(getActivity(), mSingerNews);
           		myList.setAdapter(mdapter);
           	  }catch(Exception e){
           		 
           	  }
             }else{
             	reloadLayout.setVisibility(View.VISIBLE);
             }
 
         }
     }
     
     private class LoadMoreTask extends AsyncTask {
 
         @Override
         protected Object doInBackground(Object... params) {
             // TODO Auto-generated method stub
         	
         	moreSingerNews.clear();
         	moreSingerNews = LyricAPI.getNews(singerName, myPage);
         	if(moreSingerNews!= null){
 	        	for(int i=0; i<moreSingerNews.size();i++){	        		
 	        		mSingerNews.add(moreSingerNews.get(i));
 	            }
         	}
         	
         	
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             // TODO Auto-generated method stub
             super.onPostExecute(result);
             
             if(moreSingerNews!= null && moreSingerNews.size()!=0){
             	mdapter.notifyDataSetChanged();	                
             }else{
                 checkLoad= false;
                 Toast.makeText(getActivity(), "no more data", Toast.LENGTH_SHORT).show();            	
             }       
           	myList.onLoadMoreComplete();
           	
           	
         }
     }
     
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
     }
 }
