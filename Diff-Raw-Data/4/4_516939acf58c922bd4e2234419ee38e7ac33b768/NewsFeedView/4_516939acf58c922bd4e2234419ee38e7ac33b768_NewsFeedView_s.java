 package com.faceme_android;
 
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.example.faceme_android.ApplicationData;
 import com.example.faceme_android.CharacterSelectionActivity;
 import com.example.faceme_android.R;
 import com.example.faceme_android.RateAndCommentActivity;
 import com.example.faceme_android.Tools;
 
 public class NewsFeedView {
 
 	private Context mContext;
 	private View mContentView;
 	private LayoutInflater mLayoutInFlater;
 	
 	private ListView mNewsList;
 	private ArrayAdapter<NewsEntity> mListAdapter;
 	
 	private ApplicationData mApplicationData;
 	private View mLoadingView;
 	private OnClickListener playListener;
 	
 	
 	public NewsFeedView(Context context) {
 		this.mContext = context;
 		mApplicationData = (ApplicationData) context.getApplicationContext();
 		
 		
 		mLayoutInFlater = LayoutInflater.from(context);
 		this.mContentView = mLayoutInFlater.inflate(R.layout.multitab_subtab_news, null);
 		this.mLoadingView = mContentView.findViewById(R.id.loadingView);
 		
 		mNewsList = (ListView) mContentView.findViewById(R.id.listView_newslist);
 		mListAdapter = new NewsAdapter(mApplicationData.mNews);
 		mNewsList.setAdapter(mListAdapter);
 		mNewsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View viewClicked, int position,
 					long id) {
 				mApplicationData.currentNews = mApplicationData.mNews.get(position);
 				Intent intent =new Intent(mContext, RateAndCommentActivity.class);
 
 				mContext.startActivity(intent);
 			}
 		});
 		
		Button btn_refresh = (Button) mContentView.findViewById(R.id.button_refresh);
 		btn_refresh.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				mApplicationData.loadNewsFeed(mListAdapter);
 			}
 		});
 //		mNewsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 //
 //			@Override
 //			public void onItemClick(AdapterView<?> parent, View viewClicked, int postion,
 //					long id) {
 //				// TODO Auto-generated method stub
 //				PosterEntity currentPoster=mApplicationData.mPosters.get(postion);
 //				mApplicationData.currentPoster = currentPoster;
 //
 //				Intent intent;
 //				//if(postion ==0)
 //				//intent =new Intent(PosterSelectionActivity.this, CharacterSelectionActivityXiaofei.class);
 //				//else
 //				//intent =new Intent(PosterSelectionActivity.this, CharacterSelectionActivitySuperMan.class);
 //				intent =new Intent(mContext, CharacterSelectionActivity.class);
 //				mContext.startActivity(intent);
 //			}
 //		});
 		
 		if(mApplicationData.mNews.size() != 0){
 			hideLoadingView();
 		}
 	}
 
 	public void hideLoadingView(){
 		mLoadingView.setVisibility(View.GONE);
 	}
 	
 	public Context getContext() {
 		return mContext;
 	}
 
 	public View getContentView() {
 		return mContentView;
 	}
 
 	public ArrayAdapter getAdapter(){
 		return mListAdapter;
 	}
 
 	
 	public class NewsAdapter extends ArrayAdapter<NewsEntity>{
 		
 		public NewsAdapter(List<NewsEntity> newsList){
 			super(mContext, R.layout.cellitem_poster, newsList);
 		}
 
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			View templateView;
 			templateView = convertView;
 			if(templateView==null){
 				templateView=mLayoutInFlater.inflate(R.layout.cellitem_newsfeed, parent, false);
 			}
 			NewsEntity currentNews = mApplicationData.mNews.get(position);
 			
 			TextView userNameText = (TextView)templateView.findViewById(R.id.label_name);
 			TextView dataText = (TextView)templateView.findViewById(R.id.label_posterDate);
 			
 			ImageView newsImageView = (ImageView)templateView.findViewById(R.id.imageView_newsImage);
 
 			String userName = mApplicationData.mUserFaceCache.get(currentNews.userfaces.get(0)).getUserID();
 			userNameText.setText(userName);
 			dataText.setText("");
 			
 			if(currentNews.cosplayBmp == null){
 				Tools.synthesisPoster(currentNews, mApplicationData);
 			}
 			
 			newsImageView.setImageBitmap(currentNews.newsBmp);
 			long userfaceID = currentNews.userfaces.get(0);
 			long characterfaceID = mApplicationData.mUserFaceCache.get(userfaceID).id;
 			mApplicationData.mUserFaceCache.get(userfaceID);
 			
 			Button btn_play = (Button)templateView.findViewById(R.id.button_play);
 			btn_play.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					Intent intent;
 					intent =new Intent(mContext, CharacterSelectionActivity.class);
 					mApplicationData.playWithNews = true;
 					mApplicationData.currentNews = mApplicationData.mNews.get(position);		
 					mContext.startActivity(intent);
 				}
 			});
 			
 			return templateView;
 		}
 		
 		@Override
 		public void notifyDataSetChanged(){
 			hideLoadingView();
 			super.notifyDataSetChanged();
 		}
 	}
 }
