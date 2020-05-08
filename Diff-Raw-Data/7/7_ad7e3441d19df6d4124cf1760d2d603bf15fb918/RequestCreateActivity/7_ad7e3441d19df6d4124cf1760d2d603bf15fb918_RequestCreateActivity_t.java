 package com.lake.tahoe.activities;
 
 import android.location.Location;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;

 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.maps.android.ui.IconGenerator;
 import com.lake.tahoe.R;
 import com.lake.tahoe.models.Request;
 import com.lake.tahoe.models.User;
 import com.lake.tahoe.utils.ActivityUtil;
 import com.lake.tahoe.utils.Currency;
 import com.lake.tahoe.utils.MapUtil;
 import com.lake.tahoe.utils.PushUtil;
 import com.lake.tahoe.views.CurrencyTextWatcher;
 import com.lake.tahoe.views.DynamicActionBar;
 import com.lake.tahoe.widgets.SpeechBubble;
 import com.parse.ParsePush;
 
 
 public class RequestCreateActivity extends GoogleLocationServiceActivity {
 
 	GoogleMap map;
 	Marker marker;
 	Request request;
 	TextView title;
 	TextView amt;
 	TextView description;
 
 	DynamicActionBar actionBar;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_request_create);
 
 		actionBar = new DynamicActionBar(RequestCreateActivity.this);
 
 		amt = (TextView) findViewById(R.id.rewardText);
 		title = (TextView) findViewById(R.id.wantText);
 		description = (TextView) findViewById(R.id.anythingElseText);
 		amt.addTextChangedListener(new CurrencyTextWatcher());
 
 		actionBar.setTitle(getString(R.string.create_a_request));
 
 		actionBar.setAcceptAction(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				createRequest();
 			}
 		});
 
 		actionBar.setLeftAction(R.drawable.ic_action_vendor_mode, new View.OnClickListener() {
 			@Override public void onClick(View v) {
 				convertToVendor();
 			}
 		});
 	}
 
 	private void convertToVendor() {
 		User user = User.getCurrentUser();
 		user.setType(User.Type.VENDOR);
 		toggleBlocker(true);
 		user.saveAndPublish(new PushUtil.HandlesPublish() {
 			@Override public void onPublished(ParsePush push) {
 				ActivityUtil.startRequestMapActivity(RequestCreateActivity.this);
 				ActivityUtil.transitionFade(RequestCreateActivity.this);
 			}
 			@Override public void onError(Throwable t) {
 				RequestCreateActivity.this.onError(t);
 			}
 		});
 	}
 
 	public void createRequest() {
 
 		CharSequence titleText = title.getText();
 		CharSequence amtText = amt.getText();
		CharSequence descriptionText = description.getText();
 
 		if (titleText == null || amtText == null ||
 				titleText.toString().equals("") || amtText.toString().equals("")) {
 			showMessage(getString(R.string.missing_required_fields));
 			return;
 		}
 
 		request = new Request(Request.State.OPEN);
 		request.setTitle(titleText.toString());
 
 		int amount = Currency.getAmountInCents(amtText.toString());
 		if (amount > 0) request.setCents(amount);
 
 		if (descriptionText != null)
 			request.setDescription(descriptionText.toString());
 
 		request.setClient(User.getCurrentUser());
 		toggleBlocker(true);
 		request.saveAndPublish(new PushUtil.HandlesPublish() {
 			@Override public void onPublished(ParsePush push) {
 				ActivityUtil.startRequestOpenActivity(RequestCreateActivity.this);
 				ActivityUtil.transitionRight(RequestCreateActivity.this);
 			}
 			@Override public void onError(Throwable t) {
 				RequestCreateActivity.this.onError(t);
 			}
 		});
 
 	}
 
 	protected void onGooglePlayServicesReady() {
 		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
 		map = fragment.getMap();
 		map.setMyLocationEnabled(true);
 		map.getUiSettings().setZoomControlsEnabled(false);
 		map.getUiSettings().setMyLocationButtonEnabled(false);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		super.onLocationChanged(location);

 		IconGenerator iconGenerator = new IconGenerator(this);
 		LatLng position = MapUtil.locationToLatLng(location);
 		if (marker == null) {
 			marker = map.addMarker(MapUtil.getSpeechBubbleMarkerOptions(
 				position,
 				getResources().getString(R.string.you),
 				iconGenerator, SpeechBubble.ColorType.PURPLE
 			));
 		} else {
 			marker.setPosition(position);
 		}
 		MapUtil.panAndZoomToLocation(map, location, MapUtil.DEFAULT_ZOOM_LEVEL);
 	}
 
 	@Override
 	protected void onGooglePlayServicesError(Throwable t) {
 		onError(t);
 	}
 
 	@Override
 	protected void onLocationTrackingFailed(Throwable t) {
 		onError(t);
 
 	}
 }
