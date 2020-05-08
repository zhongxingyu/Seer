 package com.twormobile.mytravelasia.feed;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import com.twormobile.mytravelasia.R;
 import com.twormobile.mytravelasia.model.PoiDetails;
 import com.twormobile.mytravelasia.ui.CarouselPhotoFragment;
 import com.twormobile.mytravelasia.ui.FragmentListPagerAdapter;
 import com.twormobile.mytravelasia.ui.ZoomOutPageTransformer;
 
 import java.util.ArrayList;
 
 /**
  * A fragment which displays a Poi's details.
  *
 * @author avendael
  */
 public class PoiDetailsFragment extends Fragment {
     public static final String TAG = PoiDetailsFragment.class.getSimpleName();
     public static final String ARG_POI_DETAILS = "com.twormobile.mytravelasia.arg_poi_details";
 
     private PoiDetails mPoiDetails;
     private ViewPager mViewPager;
     private FragmentListPagerAdapter mAdapter;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.poi_details_fragment, container, false);
         Bundle args = savedInstanceState != null ? savedInstanceState : getArguments();
         TextView tvPoiName = (TextView) view.findViewById(R.id.tv_poi_name);
         TextView tvPoiAddress = (TextView) view.findViewById(R.id.tv_poi_address);
         TextView tvComments = (TextView) view.findViewById(R.id.tv_comments);
         TextView tvLikes = (TextView) view.findViewById(R.id.tv_likes);
         TextView tvTelephone = (TextView) view.findViewById(R.id.tv_telephone);
         TextView tvWebsite = (TextView) view.findViewById(R.id.tv_website);
         TextView tvEmail = (TextView) view.findViewById(R.id.tv_email);
         TextView tvDescription = (TextView) view.findViewById(R.id.tv_description);
         mViewPager = (ViewPager) view.findViewById(R.id.vp_carousel);
 
         // Prevent the ScrollView from intercepting a ViewPager flip
         mViewPager.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 mViewPager.getParent().requestDisallowInterceptTouchEvent(true);
                 return false;
             }
         });
 
         ArrayList<Fragment> fragments = new ArrayList<Fragment>();
         mAdapter = new FragmentListPagerAdapter(getActivity().getSupportFragmentManager(), fragments);
 
         fragments.add(new CarouselPhotoFragment());
         fragments.add(new CarouselPhotoFragment());
         fragments.add(new CarouselPhotoFragment());
         fragments.add(new CarouselPhotoFragment());
         fragments.add(new CarouselPhotoFragment());
 
         mAdapter.notifyDataSetChanged();
         mViewPager.setAdapter(mAdapter);
 
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             mViewPager.setPageTransformer(false, new ZoomOutPageTransformer());
         }
 
         if (args != null) {
             mPoiDetails = args.getParcelable(ARG_POI_DETAILS);
 
             tvPoiName.setText(mPoiDetails.getName());
             tvPoiAddress.setText(mPoiDetails.getAddress());
             tvComments.setText(Long.toString(mPoiDetails.getTotalComments()));
             tvLikes.setText(Long.toString(mPoiDetails.getTotalLikes()));
             tvTelephone.setText(mPoiDetails.getTelNo());
             tvWebsite.setText(mPoiDetails.getWebUrl());
             tvEmail.setText(mPoiDetails.getEmail());
             tvDescription.setText(mPoiDetails.getDescription());
         } else {
             mPoiDetails = new PoiDetails();
         }
 
         return view;
     }
 }
