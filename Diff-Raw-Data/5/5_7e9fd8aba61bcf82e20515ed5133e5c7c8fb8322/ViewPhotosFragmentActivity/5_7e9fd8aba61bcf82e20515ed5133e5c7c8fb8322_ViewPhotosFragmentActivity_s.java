 package com.cesar.yourlifealbum.ui.activities;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 
 import com.cesar.yourlifealbum.R;
 import com.cesar.yourlifealbum.application.AppConstants;
 import com.cesar.yourlifealbum.components.adapters.ViewPagerAdapter;
 import com.cesar.yourlifealbum.data.db.models.Photo;
import com.cesar.yourlifealbum.ui.fragments.ViewPhotosFragment;
 import com.viewpagerindicator.CirclePageIndicator;
 
 public class ViewPhotosFragmentActivity extends FragmentActivity {
 
    private final String CLASS_NAME = ViewPhotosFragment.class.getSimpleName();
 
     private ViewPager mViewPager;
     private CirclePageIndicator mCircleIndicator;
     private ViewPagerAdapter mPagerAdapter;
 
     private ArrayList<Photo> mPhotoList;
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Intent intent = getIntent();
         if (intent != null) {
             Bundle bundle = intent.getBundleExtra(AppConstants.Messages.BUNDLE);
             mPhotoList = bundle
                     .getParcelableArrayList(AppConstants.Messages.DAY_PHOTOS);
         }
 
         setContentView(R.layout.view_photos_layout);
 
         mViewPager = (ViewPager) findViewById(R.id.view_pager);
         mCircleIndicator = (CirclePageIndicator) findViewById(R.id.circle_indicator);
 
         mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                 mPhotoList);
         mViewPager.setAdapter(mPagerAdapter);
         mCircleIndicator.setViewPager(mViewPager);
     }
 
 }
