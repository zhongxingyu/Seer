 package com.alcoholcompass;
 
 import java.util.List;
 
 import android.R.interpolator;
 import android.content.Intent;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.view.animation.RotateAnimation;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
import android.widget.TextView;
 
 import com.alcoholcompass.data.LocationService;
 import com.alcoholcompass.data.Place;
 import com.alcoholcompass.data.WebService;
 
 public class NavigatorFragment extends Fragment{
 	
 	private ImageView imageViewArrow;
	private TextView textViewPlaceName, textViewPlaceDistance;
 	private Button buttonMore, buttonNavigation;
 	private ListView listViewPlaces;
 	private int lastArrowDegrees;
 	private boolean isArrowTurning;
 	
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_navigation, container, false);
 		
 		//Views, Buttons zuweisen
 		imageViewArrow = (ImageView) view.findViewById(R.id.imageViewArrow);
 		listViewPlaces = (ListView) view.findViewById(R.id.listViewPlaces);
 		buttonMore = (Button) view.findViewById(R.id.buttonShowMore);
 		buttonNavigation = (Button) view.findViewById(R.id.buttonNavigation);
		textViewPlaceName = (TextView) view.findViewById(R.id.textViewPlaceName);
		textViewPlaceDistance = (TextView) view.findViewById(R.id.textViewPlaceDistance);
 		
 		final LocationService service = LocationService.getInstance(getActivity());
 		Location loc = service.currentLocation();
 		WebService.loadPlaces(loc, new WebService.PlacesHandler() {
 			
 			@Override
 			public void success(List<Place> places) {
 				listViewPlaces.setAdapter(new PlacesListAdapter(getActivity().getApplicationContext(), places));
 			}
 			
 			@Override
 			public void failure() {}
 		});
 				
 		LocationService.getInstance(getActivity()).addListener(new LocationService.LocationListener() {
 			@Override
 			public void onLocationUpdate() {
 				int degree = service.arrowAngleTo(50.778104, 6.060867);
 				setArrow(degree);
 			}
 		});
 		
 		buttonMore.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				togglePlacesList();
 			}
 		});
 		
 		buttonNavigation.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent navi = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:ll=" + 50.7777075 + "," + 6.0608821 ));
 				startActivity(navi);
 			}
 		});
 		
 		return view;
 	}
 	
 	private void togglePlacesList(){
 		if(listViewPlaces.getVisibility() == View.INVISIBLE){
 			displayPlacesList();
 		}else{
 			hidePlacesList();
 		}
 	}
 	
 	private void displayPlacesList(){
 		
 		listViewPlaces.setVisibility(View.VISIBLE);
 		Animation slide_in = AnimationUtils.loadAnimation(getActivity(), R.anim.top_down_slide);
 		listViewPlaces.startAnimation(slide_in);
 		buttonMore.setText(R.string.less);
 	}
 	
 	private void hidePlacesList(){
 		Animation slide_out = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_up_slide);
 		slide_out.setAnimationListener(new AnimationListener() {
 			
 			@Override
 			public void onAnimationStart(Animation animation) {}
 			
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 			
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				listViewPlaces.setVisibility(View.INVISIBLE);
 				buttonMore.setText(R.string.more);
 			}
 		});
 		
 		listViewPlaces.startAnimation(slide_out);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		init();
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		LocationService service = LocationService.getInstance(getActivity());
 		service.onResume();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		LocationService service = LocationService.getInstance(getActivity());
 		service.onPause();
 	}
 	
 	private void init(){
 		lastArrowDegrees = 0;
 		isArrowTurning = false;
 	}
 	
 	private void setArrow(int degrees){
 		
 		if (getActivity() == null || isArrowTurning) return;
 		
 		degrees = degrees % 360;
 		final int newDegrees = degrees;
 		if(degrees == lastArrowDegrees) return;
 		isArrowTurning = true;
 		
 		imageViewArrow.setRotation(0);
 		
 		//Animation erstellen
 		RotateAnimation animation = new RotateAnimation(lastArrowDegrees, newDegrees,
 				Animation.RELATIVE_TO_SELF, 0.5f,
 				Animation.RELATIVE_TO_SELF, 0.5f);
 		animation.setFillEnabled(true);
 		animation.setFillBefore(true);
 		animation.setDuration(500);
 		animation.setInterpolator(getActivity().getApplicationContext(), interpolator.accelerate_decelerate);
 		
 		animation.setAnimationListener(new AnimationListener() {
 			
 			@Override
 			public void onAnimationStart(Animation animation) {}
 			
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 			
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				lastArrowDegrees = newDegrees;
 				imageViewArrow.setRotation(newDegrees);
 				isArrowTurning = false;
 			}
 		});
 		
 		imageViewArrow.startAnimation(animation);
 		
 	}
 	
 }
