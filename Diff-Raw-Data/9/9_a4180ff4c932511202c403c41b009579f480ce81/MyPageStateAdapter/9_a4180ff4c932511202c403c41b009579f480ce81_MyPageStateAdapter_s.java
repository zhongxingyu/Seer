 package com.protech.ascension.gui;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
import com.protech.ascension.chapter1.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: BNengel
  * Date: 5/10/12
  * Time: 4:15 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MyPageStateAdapter extends FragmentStatePagerAdapter {
     private int mPageCount;
     private int mChapter;
     private int currentPosition;
     private PageFragment mCurrentFragment;
 
     public MyPageStateAdapter(FragmentManager fm, int mChapter, int mPageCount) {
         super(fm);
         this.mChapter = mChapter;
         this.mPageCount = mPageCount;
     }
 
     @Override
     public Fragment getItem(int i) {
 //        if (i == 0) {
 //            mCurrentFragment = new FirstChapterPage1Fragment();
 //            return mCurrentFragment;
 //        } else if (i == 1) {
 //            mCurrentFragment = new FirstChapterPage2Fragment();
 //            return mCurrentFragment;
 //        } else if (i == 2) {
 //            mCurrentFragment = new FirstChapterPage3Fragment();
 //            return mCurrentFragment;
 //        }
         return Chapter1Fragment.newInstance(i);
     }
 
     @Override
     public int getCount() {
         return mPageCount;
     }
 
     @Override
     public CharSequence getPageTitle(int position) {
         return "Page " + (position + 1);
     }
 
     public int getTouchMode() {
         if (mCurrentFragment != null) {
             return mCurrentFragment.getTouchMode();
         }
         return TouchImageView2.NONE;
     }
 
     public void setCurrentPosition(int position) {
         currentPosition = position;
     }
 }
