 package com.lib4.circularviewpager;
 
 import android.content.Context;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 
 public class CircularPagerAdapter extends PagerAdapter{
 
 private int[] pageIDsArray;
 private int count;
 
 public CircularPagerAdapter(final ViewPager pager, int... pageIDs) {
     super();
     int actualNoOfIDs = pageIDs.length;
     count = actualNoOfIDs + 2;
     pageIDsArray = new int[count];
     for (int i = 0; i < actualNoOfIDs; i++) {
         pageIDsArray[i + 1] = pageIDs[i];
     }
     pageIDsArray[0] = pageIDs[actualNoOfIDs - 1];
     pageIDsArray[count - 1] = pageIDs[0];
 
     pager.setOnPageChangeListener(new OnPageChangeListener() {
 
         public void onPageSelected(int position) {
        	
             int pageCount = getCount();
            Log.e("Position"," "+position+" Page count: "+pageCount);

            if (position == pageCount-1){
              pager.setCurrentItem(1,false);
          }
            if(position==0){
            	pager.setCurrentItem(pageCount-2,false);
             }
            
         }
 
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
             // TODO Auto-generated method stub
        	
        	 Log.e("Position"," "+position+" positionOffset  : "+positionOffset);
         }
 
         public void onPageScrollStateChanged(int state) {
             // TODO Auto-generated method stub
         }
     });
 }
 
 public int getCount() {
     return count;
 }
 
 public Object instantiateItem(View container, int position) {
     LayoutInflater inflater = (LayoutInflater) container.getContext()
             .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     int pageId = pageIDsArray[position];
     View view = inflater.inflate(pageId, null);
     ((ViewPager) container).addView(view, 0);
     return view;
 }
 
 @Override
 public void destroyItem(View container, int position, Object object) {
     ((ViewPager) container).removeView((View) object);
 }
 
 @Override
 public void finishUpdate(View container) {
     // TODO Auto-generated method stub
 }
 
 @Override
 public boolean isViewFromObject(View view, Object object) {
     return view == ((View) object);
 }
 
 @Override
 public void restoreState(Parcelable state, ClassLoader loader) {
     // TODO Auto-generated method stub
 }
 
 @Override
 public Parcelable saveState() {
     // TODO Auto-generated method stub
     return null;
 }
 
 @Override
 public void startUpdate(View container) {
     // TODO Auto-generated method stub
 }

@Override
public float getPageWidth(int position){
	return 0.8f;
}
 }
