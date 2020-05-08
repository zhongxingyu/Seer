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
    private ImageButton mBtn0;
    private ImageButton mBtn1;
    private ImageButton mBtn2;
    private ImageButton mBtn3;
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
    	mBtn1 = (ImageButton) root.findViewById(R.id.dockBtn0);
    	mBtn1.setTag(0);
    	mBtn0 = (ImageButton) root.findViewById(R.id.dockBtn1);
    	mBtn0.setTag(1);
    	mBtn2 = (ImageButton) root.findViewById(R.id.dockBtn2);
    	mBtn2.setTag(2);
    	mBtn3 = (ImageButton) root.findViewById(R.id.dockBtn3);
    	mBtn3.setTag(3);
     	
     	mBtnCenter = (ImageButton) root.findViewById(R.id.dockBtnCenter);
     	mBtnCenter.setTag(POS_CENTER);
     	
    	mBtn0.setOnClickListener(this);
    	mBtn1.setOnClickListener(this);
    	mBtn2.setOnClickListener(this);
    	mBtn3.setOnClickListener(this);
     	
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
