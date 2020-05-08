 /**
  * douzifly @2013-6-6
  * github.com/douzifly
  * douzifly@gmail.com
  */
 package com.imtech.ask.ui;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 
 import com.imtech.ask.R;
 
 /**
  * @author douzifly
  *
  */
 public class DockFragment extends Fragment implements IDockFragment, OnClickListener{
     
     private DockListener mLisenter;
    private ImageButton mBtnAsk;
    private ImageButton mBtnNews;
    private ImageButton mBtnMsg;
    private ImageButton mBtnUser;
     private ImageButton mBtnCenter;
     final static int POS_CENTER = -1;
     private View mBar;
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_dock, null);
         setupView(v);
         return v;
     }
     
     private void setupView(View root){
    	mBtnAsk = (ImageButton) root.findViewById(R.id.dockBtnAsk);
    	mBtnAsk.setTag(0);
    	mBtnNews = (ImageButton) root.findViewById(R.id.dockBtnNews);
    	mBtnNews.setTag(1);
    	mBtnMsg = (ImageButton) root.findViewById(R.id.dockBtnMsg);
    	mBtnMsg.setTag(2);
    	mBtnUser = (ImageButton) root.findViewById(R.id.dockBtnUser);
    	mBtnUser.setTag(3);
     	
     	mBtnCenter = (ImageButton) root.findViewById(R.id.dockBtnCenter);
     	mBtnCenter.setTag(POS_CENTER);
     	
    	mBtnAsk.setOnClickListener(this);
    	mBtnNews.setOnClickListener(this);
    	mBtnMsg.setOnClickListener(this);
    	mBtnUser.setOnClickListener(this);
     	
     	mBtnCenter.setOnClickListener(this);
     	
     	mBar = root.findViewById(R.id.dockBar);
     	
     }
 
     @Override
     public void setDockListener(DockListener l) {
         mLisenter = l;
     }
 
     @Override
     public void setSelected(int position) {
     }
 
 	@Override
 	public void onClick(View v) {
 		int pos = (Integer) v.getTag();
 		if(pos == POS_CENTER){
 			if(mLisenter != null){
 				mLisenter.onDockCenterItemClicked();
 			}
 		}else{
 			if(mLisenter != null){
 				mLisenter.onDockItemClicked(pos);
 			}
 		}
 	}
 
 	@Override
 	public int getBarHeight() {
 		return mBar.getHeight();
 	}
     
 }
