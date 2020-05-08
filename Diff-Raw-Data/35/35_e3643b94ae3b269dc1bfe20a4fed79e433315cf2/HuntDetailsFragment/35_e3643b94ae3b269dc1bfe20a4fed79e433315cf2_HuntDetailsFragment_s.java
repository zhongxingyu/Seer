 package com.chaseit.fragments;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 import com.chaseit.ParseHelper;
 import com.chaseit.R;
 import com.chaseit.adapters.HuntImageAdapter;
 import com.chaseit.adapters.LocationImageAdapter;
 import com.chaseit.fragments.interfaces.HuntStartInterface;
 import com.chaseit.models.CIUser;
 import com.chaseit.models.Hunt;
 import com.chaseit.models.HuntImage;
 import com.chaseit.models.Location;
 import com.chaseit.models.UserHunt;
 import com.chaseit.models.UserHunt.HuntStatus;
 import com.chaseit.models.wrappers.HuntWrapper;
 import com.chaseit.models.wrappers.LocationWrapper;
 import com.chaseit.models.wrappers.ParseObjectWrapper;
 import com.chaseit.models.wrappers.UserHuntWrapper;
 import com.chaseit.util.Constants;
 import com.chaseit.util.Helper;
 import com.chaseit.views.TwoWayView;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.LatLngBounds.Builder;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.SaveCallback;
 
 public class HuntDetailsFragment extends Fragment {
 	private static final String tag = "Debug - com.chaseit.fragments.HuntDetailsFragment";
 	private HuntWrapper hWrapper;
 	private TextView tvHuntDetailsTitle;
 	private TextView tvHuntDetailsCreatorHandle;
 	private RatingBar rbHuntDetailsRating;
 	private TextView tvHuntDetailsLocationName;
 	private TextView tvHuntDetailsDescription;
 	private GoogleMap gmHuntsDetailsMap;
 	private Button btnHuntDetailsLaunch;
 	private TwoWayView ivHuntDetailsBanner;
 	
 	private List<HuntImage> huntImages;
 	private HuntImageAdapter huntImageAdapter;
 
 	private LocationImageAdapter locationImageAdapter;
 
 	private boolean isHuntInProgress = false;
 	private UserHunt huntInProgress;
 	private Hunt thisHunt;
 	private List<Location> huntLocations;
 	private ArrayList<LocationWrapper> huntLocationsWrapped = new ArrayList<LocationWrapper>();
 
 	private boolean isSummary;
 	
 	protected UserHuntWrapper uHuntWrapper;
 	
 	private enum Illustrate{
 		START,
 		UPTO_PROGRESS,
 		ALL
 	};
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_huntdetails, container,
 				false);
 		huntImages = new ArrayList<HuntImage>();
 		huntImageAdapter = new HuntImageAdapter(getActivity(), huntImages);
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		Bundle extras = getArguments();
 		hWrapper = (HuntWrapper) extras.getSerializable(Constants.HUNT_WRAPPER_DATA_NAME);
 		isSummary = (boolean)extras.getBoolean("showSummary");
 		String creatorName = hWrapper.getCreatorName();
 		String viewerName = CIUser.getName();
 		if(creatorName.equalsIgnoreCase(viewerName)){
 			isSummary = true;
 		}
 		
 		setupViews(getView(), hWrapper);
 		
 		//get the hunt and save it off 
 		thisHunt = ParseHelper.getHuntByObjectIdBlocking(hWrapper.getObjectId());
 		
 		//find if hunt is in progress
 		ParseHelper.getMyHuntsInProgress(new FindCallback<UserHunt>() {
 			@Override
 			public void done( List<UserHunt> myHuntsInProgress, ParseException e) {
 				if (e == null) {
 					if(myHuntsInProgress == null || myHuntsInProgress.size() == 0){
 						huntInProgress = null;
 						isHuntInProgress = false;
 					} else {
 						for (UserHunt uHunt : myHuntsInProgress) {
 							if (uHunt.getHuntObjectId().equals(hWrapper.getObjectId())) {
 								Log.d(tag, "This hunt is already in progress !");
 								isHuntInProgress = true;
 								huntInProgress = uHunt;
 							}
 						}						
 					}
 				} else {
 					Log.d(tag, e.getMessage());
 				}
 				//setup 'start click listener' only after we know if hunt is in progress or not
 				btnHuntDetailsLaunch.setOnClickListener(getHuntStartClickListener());
 			}
 		});
 		
 		//get all locations for hunt
 		ParseHelper.getLocationsforHunt(thisHunt, new FindCallback<Location>() {
 			@Override
 			public void done(List<Location> objects, ParseException e) {
 				if(e == null){
 					huntLocations = objects;
 					huntLocationsWrapped = LocationWrapper.fromLocations(objects);
 					if(isSummary){
 						locationImageAdapter = new LocationImageAdapter(getActivity(), objects);
 						ivHuntDetailsBanner.setAdapter(locationImageAdapter);
 						locationImageAdapter.notifyDataSetChanged();						
 					}
 					Illustrate illustration = Illustrate.ALL;
 					
 					if(isSummary){
 						illustration = Illustrate.ALL;
 					} else if(isHuntInProgress){
 						illustration = Illustrate.UPTO_PROGRESS;
 					} else {
 						illustration = Illustrate.START;
 					}
 					drawMarkersAndPolygon(huntLocations, illustration);
 				}
 			}
 		});
 
 	}
 	
 	/**
 	 * function to load map. If map is not created it will create it for you
 	 * */
 	private void initilizeMap() {
 		if (gmHuntsDetailsMap == null) {
 			gmHuntsDetailsMap = ((SupportMapFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.gmHuntsDetailsMap)).getMap();
 			gmHuntsDetailsMap.setMyLocationEnabled(false);
 			UiSettings mapUiSettings = gmHuntsDetailsMap.getUiSettings();
 			//make this a non interactive map
 			mapUiSettings.setAllGesturesEnabled(false);
 			if (gmHuntsDetailsMap == null) {
 				Log.d("DEBUG", "Sorry! unable to create maps");
 			}
 		}
 	}
 
 
 	private void setupViews(View view, HuntWrapper hWrapper) {
 		tvHuntDetailsTitle = (TextView) view.findViewById(R.id.tvHuntDetailsTitle);
 		tvHuntDetailsCreatorHandle = (TextView) view.findViewById(R.id.tvHuntDetailsCreatorHandle);
 		rbHuntDetailsRating = (RatingBar) view.findViewById(R.id.rbHuntDetailsRating);
 		tvHuntDetailsLocationName = (TextView) view.findViewById(R.id.tvHuntDetailsLocationName);
 		tvHuntDetailsDescription = (TextView) view.findViewById(R.id.tvHuntDetailsDescription);
 		btnHuntDetailsLaunch = (Button) view.findViewById(R.id.btnHuntDetailsLaunch);
 		ivHuntDetailsBanner = (TwoWayView) view.findViewById(R.id.ivHuntDetailsBanner);	
 		initilizeMap();
 		
 		if(!isSummary){
 			ivHuntDetailsBanner.setAdapter(huntImageAdapter);
 			//show all hunt images
 			ParseHelper.getHuntImagesGivenHunt(thisHunt, new FindCallback<HuntImage>() {
 				@Override
 				public void done(List<HuntImage> objects, ParseException e) {
 					huntImageAdapter.addAll(objects);
 					huntImageAdapter.notifyDataSetChanged();
 				}
 			});			
 		}
 
 		//setup hunt tagline
 		if (Helper.isNotEmpty(hWrapper.getName())){
 			tvHuntDetailsTitle.setText(hWrapper.getName());			
 		}
 		
 		//set hunt creator name
 		if(StringUtils.isNotBlank(hWrapper.getCreatorName())){
 			tvHuntDetailsCreatorHandle.setText(hWrapper.getCreatorName());
 		}
 		
 		if(StringUtils.isNotBlank(hWrapper.getLocality())){
 			tvHuntDetailsLocationName.setText(hWrapper.getLocality());
 		} else {
 			tvHuntDetailsLocationName.setText("Unknown");
 		}
 
 		//show hunt rating
 		//TODO : remove the minimum 3star hack :)
 		rbHuntDetailsRating.setRating(Math.max(3, Math.round(hWrapper.getAvgRating())));
 		rbHuntDetailsRating.setEnabled(false);
 		
 		//show hunt details
 		if (Helper.isNotEmpty(hWrapper.getDetails())){
 			tvHuntDetailsDescription.setText(hWrapper.getDetails());			
 		}
 
 	}
 
 	private OnClickListener getHuntStartClickListener() {
 		return new OnClickListener() {
 			private UserHunt userHunt;
 
 			@Override
 			public void onClick(View v) {
 				Log.d(tag, "start or contiue clicked");
 				if (!isHuntInProgress) {
 					userHunt = new UserHunt();
 					userHunt.setHuntObjectId(thisHunt.getObjectId());
 					userHunt.setUserObjectId(CIUser.getCurrentUser().getObjectId());
 					userHunt.setHuntStatus(HuntStatus.IN_PROGRESS);
 //					userHunt.setLastLocationLat(thisHunt.getStartLocation().getLatitude());
 //					userHunt.setLastLocationLong(thisHunt.getStartLocation().getLongitude());
 //					userHunt.setLastLocationIndex(0);
 					
 					userHunt.setLastLocationLat(0);
 					userHunt.setLastLocationLong(0);
 					userHunt.setLastLocationIndex(-1);
 
 					//check if we already have locations
 					if(huntLocations != null && huntLocations.size() > 0){
 						userHunt.setLastLocationObjectId(huntLocations.get(0).getObjectId());
 //						userHunt.setLastLocationObjectId(null);
 						userHunt.saveInBackground(new SaveCallback() {
 							@Override
 							public void done(ParseException e) {
 								if (e != null){
 									Log.d(tag,e.getMessage());													
 								}
 								Log.d(tag, "leaving done");
 								startHuntOrContinue(userHunt);
 							}
 						});
 					} else {
 //						//setup the hunt in progress for update
 //						userHunt = huntInProgress;
 						//fetch the first location only
 						ParseHelper.getLocationByHuntAndIndex(thisHunt, 0, new FindCallback<Location>() {
 							@Override
 							public void done( List<Location> locations, ParseException e) {
 								if (locations != null && locations.size() > 0){
 									userHunt.setLastLocationObjectId(locations.get(0).getObjectId());
 //									userHunt.setLastLocationObjectId("");
 									userHunt.saveInBackground(new SaveCallback() {
 										@Override
 										public void done(ParseException e) {
 											if (e != null){
 												Log.d(tag,e.getMessage());													
 											}
 											Log.d(tag, "leaving done");
 											startHuntOrContinue(userHunt);
 										}
 									});										
 								}
 							}
 						});
 					}
 				} else {
 					if(huntInProgress.getHuntStatus() == HuntStatus.COMPLETED){
 						//reset the hunt
 						huntInProgress.setHuntStatus(HuntStatus.IN_PROGRESS);
 						huntInProgress.setLastLocationIndex(-1);
 						huntInProgress.setLastLocationLat(0);
 						huntInProgress.setLastLocationLong(0);
 						if(huntLocations != null && huntLocations.size() > 0){
 							huntInProgress.setLastLocationObjectId(huntLocations.get(0).getObjectId());								
 						}
 						huntInProgress.saveInBackground(new SaveCallback() {
 							@Override
 							public void done(ParseException e) {
 								if (e != null){
 									Log.d(tag,e.getMessage());													
 								}
 								Log.d(tag, "leaving done");
 								startHuntOrContinue(huntInProgress);
 							}
 						});
 					} else {
 						startHuntOrContinue(huntInProgress);						
 					}
 				}
 			}
 		};
 	}
 
 	private void startHuntOrContinue(UserHunt userHunt) {
 		uHuntWrapper = new UserHuntWrapper(new ParseObjectWrapper(userHunt));
 		FragmentActivity activity = getActivity();
 		if (activity instanceof HuntStartInterface){
 			((HuntStartInterface) activity).startHunt(uHuntWrapper, new HuntWrapper(thisHunt), huntLocationsWrapped );			
 		}
 	}
 	
 	private void drawMarkersAndPolygon(List<Location> mapPoints, Illustrate illustrate) {
 		PolylineOptions rectOptions = new PolylineOptions();
 		Collections.sort(mapPoints);
 		Builder builder = new LatLngBounds.Builder();
 		for (Location point : mapPoints) {			
 			LatLng ll = new LatLng(point.getLocation().getLatitude(), point.getLocation().getLongitude());
 			
 			//drop marker first 
 			gmHuntsDetailsMap.addMarker(new MarkerOptions().position(ll)
 					.title((point.getIndexInHunt() + 1) + ". " + point.getLocationName())
 					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
 
 			builder.include(ll);
 			rectOptions.add(ll).width(5).color(Color.BLUE).geodesic(true);
 			gmHuntsDetailsMap.addPolyline(rectOptions);
 	
 			if(illustrate == Illustrate.START){
 				break;
 			}
 
 			if(illustrate == Illustrate.UPTO_PROGRESS && (huntInProgress.getLocationIndex() == point.getIndexInHunt())){
 				break;
 			}
 			
 			if(illustrate == Illustrate.ALL){
 				continue;
 			}
 		}
		
 		// move the camera to show chase makers and polygon
		gmHuntsDetailsMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
				builder.build(), 10));
 
 	}
 }
