 package com.episode6.android.common.ui.widget;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 public class HandyListView extends ListView {
 	
 	public interface OnSizeChangedListener {
 		public void onSizeChanged(int w, int h, int oldw, int oldh);
 	}
 	
 	private View mEmptyListView = null;
 	private View mLoadingListView = null;
 	private OnSizeChangedListener mSizeListener = null;
 
 	public HandyListView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		initHandyListView();
 	}
 
 	public HandyListView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initHandyListView();
 	}
 
 	public HandyListView(Context context) {
 		super(context);
 		initHandyListView();
 	}
 	
 	private void initHandyListView() {
 		
 	}
 
 	@Override
 	public void setAdapter(ListAdapter adapter) {
 		if (mLoadingListView != null)
 			mLoadingListView.setVisibility(View.GONE);
 		setVisibility(View.VISIBLE);
		super.setEmptyView(mEmptyListView);
 		mEmptyListView = null;
		super.setAdapter(adapter);
 		
 	}
 
 	@Override
 	public View getEmptyView() {
 		if (mEmptyListView != null)
 			return mEmptyListView;
 		return super.getEmptyView();
 	}
 
 	@Override
 	public void setEmptyView(View emptyView) {
 		mEmptyListView = emptyView;
 		if (mEmptyListView != null)
 			mEmptyListView.setVisibility(View.GONE);
 	}
 	
 	public void setLoadingListView(View loadingView) {
 		mLoadingListView = loadingView;
 		if (mLoadingListView != null) {
 			setVisibility(View.GONE);
 			mLoadingListView.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
 		mSizeListener = listener;
 	}
 	
 	@Override
 	public void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
 		if (mSizeListener != null)
 			mSizeListener.onSizeChanged(w, h, oldw, oldh);
 	}
 
 	
 }
