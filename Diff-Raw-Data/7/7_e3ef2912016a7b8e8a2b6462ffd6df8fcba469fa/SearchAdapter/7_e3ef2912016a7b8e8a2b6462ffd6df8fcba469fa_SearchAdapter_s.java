 package com.epam.AnotherSearch;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.epam.Suggestions.ISuggestion;
 import com.epam.Suggestions.ISuggestionEvents;
 import com.epam.Suggestions.Suggestions;
 import com.epam.Suggestions.Suggestions.SuggestionIndex;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class SearchAdapter extends BaseAdapter {
 
 	public SearchAdapter(Context context)
 	{
 		super();
 		mContext = context;
 		mTask = new SuggestionUpdateTask(this);
 		
 		
 	}
 	public void setQuery(CharSequence s)
 	{
 		
 		mTask.cancel(false);
 		mTask = new SuggestionUpdateTask(this);
 		mTask.getSuggestions().setQuery(s);
		for (ISearchProcessListener listener : mSearchProcessListeners) {
			listener.onSearchStarted();
		}
 		mCount = 0;
 		notifyDataSetChanged();
 		if (s.length() > 0)
 		{
 			mTask.execute();
 		}
 		
 	}
 	
 	public int getCount() {
 				
 		return mCount;
 	}
 
 	public Object getItem(int arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public long getItemId(int pos) {
 		// TODO Auto-generated method stub
 		return pos;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LinearLayout layout = null;
 		TextView textView = null;
 		ImageView iconView = null;
 		try
 		{
 			
 		
 							
 				SuggestionIndex index = mTask.getSuggestions().findSuggestion(position);
 				if (index == null)
 				{
 					index = mTask.getSuggestions().findSuggestion(position);
 				}
 				SuggestionData data = getSuggestionData(index);
 				layout = (LinearLayout)convertView;
 				
 				if(layout == null)
 				{
 					layout = new LinearLayout(parent.getContext());
 					
 					LinearLayout.LayoutParams textViewParams = 
 		        			new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
 		        	LinearLayout.LayoutParams imageViewParams = 
 		        			new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
 		        	        	
 		        	textViewParams.weight = 0.80f;
 		        	imageViewParams.weight = 0.20f;
 		        	
 					textView = new TextView(parent.getContext());
 					iconView = new ImageView(parent.getContext());
 									iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
 					layout.addView(textView, textViewParams);
 					layout.addView(iconView, imageViewParams);
 					
 				}
 				else
 				{
 					textView = (TextView)layout.getChildAt(0);
 					iconView = (ImageView)layout.getChildAt(1);
 				}
 				textView.setText(data.getText());
 				iconView.setImageDrawable(data.getIcon());
 				if(index.isCategory())
 				{
 					layout.setBackgroundColor(Color.GRAY);
 				}
 				else
 				{
 					layout.setBackgroundColor(layout.getDrawingCacheBackgroundColor());				
 				}
 			
 		}catch (Exception e) {
 			e.printStackTrace();
 			
 		}
 		return layout;
 	}
 	
 	public void addSearchProcessListener(ISearchProcessListener listener)
 	{
 		if (!mSearchProcessListeners.contains(listener))
 			mSearchProcessListeners.add(listener);
 	}
 	
 	public void removeSearchProcessListener(ISearchProcessListener listener)
 	{
 		mSearchProcessListeners.remove(listener);
 	}
 	//Implementation
 		
 	
 	private SuggestionData getSuggestionData(SuggestionIndex index)
 	{
 		
 		SuggestionData data = new SuggestionData(null, null);
 		if (index.isCategory()){
 			data.setIcon(index.getSuggestion().getIcon());
 			data.setText(index.getSuggestion().getText() + (  
 			index.getSuggestion().getHint() != null ?  "\n" + index.getSuggestion().getHint() : "") );
 		}
 		else
 		{
 			data.setIcon(index.getSuggestion().getIcon(index.getIndex()));
 			data.setText(index.getSuggestion().getText(index.getIndex()));
 		}
 		return data;
 	}
 	
 	
 	
 	//Data
 	private SuggestionUpdateTask mTask = null;
 	private Context mContext = null;
 	private List<ISearchProcessListener> mSearchProcessListeners = 
 			new ArrayList<ISearchProcessListener>();
 	private int mCount = 0;
 	
 	private class SuggestionData
 	{
 		public SuggestionData(String text,Drawable icon)
 		{
 			setIcon(icon);
 			setText(text);
 		}
 		public Drawable getIcon() {
 			return icon;
 		}
 		public void setIcon(Drawable icon) {
 			this.icon = icon;
 		}
 		public String getText() {
 			return text;
 		}
 		public void setText(String text) {
 			this.text = text;
 		}
 		private Drawable icon;
 		private String text;
 		
 	}
 	
 	private class SuggestionUpdateTask extends AsyncTask<Void, Void, Void> 
 	implements ISuggestionEvents
 	{
 		
 
 		@Override
 		protected void onPostExecute(Void result) {
 			for (ISearchProcessListener listener : mSearchProcessListeners) {
 				listener.onSearchFinished();
 			}
 			super.onPostExecute(result);
 			publishProgress();
 		}
 
 		@Override
 		protected void onPreExecute() {
 			for (ISearchProcessListener listener : mSearchProcessListeners) {
 				listener.onSearchStarted();
 			}
 			super.onPreExecute();
 			ProgressAndCancelIfCanceled();
 		}
 
 		public SuggestionUpdateTask(SearchAdapter adapter)
 		{
 			mSearchAdapter = adapter;
 			mSuggestions = new Suggestions(mSearchAdapter.mContext);
 			mSuggestions.setSettings(new Settings());
 			
 		}
 			
 		public boolean OnReloadStart() {
 			ProgressAndCancelIfCanceled();
 			return false;
 		}
 
 		public boolean OnReloadFinished() {
 			ProgressAndCancelIfCanceled();
 			return false;
 		}
 
 		public boolean OnSuggestionLoaded(ISuggestion suggestion) {
 			
 			ProgressAndCancelIfCanceled();
 			return false;
 		}
 		public boolean OnSuggestionPreload(ISuggestion suggestion) {
 			ProgressAndCancelIfCanceled();
 			return false;
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... values) {
 			
 			super.onProgressUpdate(values);
 			if(!isCancelled())
 			{
 				synchronized (mSuggestions) {
 					mSearchAdapter.mCount = mSuggestions.getCount();	
 				}
 				
 				
 			}
 			else
 			{
 				mSearchAdapter.mCount = 0;
 			}
 			mSearchAdapter.notifyDataSetInvalidated();
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			try
 			{
 				ProgressAndCancelIfCanceled();
 				mSuggestions.addSuggestionEventsListener(this);
 				mSuggestions.reload();
 				ProgressAndCancelIfCanceled();
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 		public Suggestions getSuggestions() {
 			return mSuggestions;
 		}
 		
 		private void ProgressAndCancelIfCanceled()
 		{
 			mSuggestions.setCanceled(isCancelled());
 			if(isCancelled())
 			{
 				mSuggestions.removeSuggestionEventsListener(this);
 				onPostExecute(null);
 			}
 		    publishProgress();
 		}
 		private SearchAdapter mSearchAdapter = null;
 		private Suggestions mSuggestions = null;
 		
 	}
 }
