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
 import com.kosbrother.lyric.entity.YoutubeVideo;
 import com.taiwan.imageload.ListVideoAdapter;
 
 public class SingerVideoFragment extends Fragment {
 	
 	public  int myPage = 0;
 	private Boolean checkLoad = true;
 	private LinearLayout progressLayout;
 	private LinearLayout reloadLayout;
 	private LoadMoreListView myList;
 	private Button buttonReload;
 	private ListVideoAdapter mdapter;
 	private ArrayList<YoutubeVideo> mVideos = new ArrayList<YoutubeVideo>();
 	private ArrayList<YoutubeVideo> moreVideos = new ArrayList<YoutubeVideo>();
	private static String singerName;
 	
     public static SingerVideoFragment newInstance(String singer_name) {
 
    	SingerVideoFragment fragment = new SingerVideoFragment();
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
         	
         	ArrayList<YoutubeVideo> theVideos= LyricAPI.getYoutubeVideos(singerName, myPage);
         	if(theVideos!=null && theVideos.size()!=0){
         	 for(int i=0; i<theVideos.size(); i++){
         		if(theVideos.get(i).getTitle()!="null" && theVideos.get(i).getThumbnail()!="null"){
         		 mVideos.add(theVideos.get(i));
         		}
         	 }
         	}
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             // TODO Auto-generated method stub
             super.onPostExecute(result);
             progressLayout.setVisibility(View.GONE);
                        
             if(mVideos !=null && mVideos.size()!=0){
           	  try{
           		mdapter = new ListVideoAdapter(getActivity(), mVideos);
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
         	
         	moreVideos.clear();
         	moreVideos = LyricAPI.getYoutubeVideos(singerName, myPage);
         	if(moreVideos!= null){
 	        	for(int i=0; i<moreVideos.size();i++){
 	        		if(moreVideos.get(i).getTitle()!="null" && moreVideos.get(i).getThumbnail()!="null"){
 	           		 	mVideos.add(moreVideos.get(i));
 	           		}
 	            }
         	}
         	
         	
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             // TODO Auto-generated method stub
             super.onPostExecute(result);
             
             if(moreVideos!= null && moreVideos.size()!=0){
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
