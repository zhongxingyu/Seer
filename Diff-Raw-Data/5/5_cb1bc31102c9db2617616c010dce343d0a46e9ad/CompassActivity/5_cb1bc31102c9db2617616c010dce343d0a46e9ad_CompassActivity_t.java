 package fi.jamk.e6379;
 
 import java.text.DecimalFormat;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.widget.Gallery;
 import android.widget.Gallery.LayoutParams;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class CompassActivity extends Activity implements LocationListener {
 	private LinearLayout linearLayout;
 	private TextView textView;
 	private ImageView image;
 	private float angle;
 	private float distanceToTarget;
 	private Location target;
 	private Location currentLocation;
 	
 	public void onCreate( Bundle bundle ) {
 		super.onCreate( bundle );
 		setContentView(R.layout.compasslayout);
 
 		Intent callingIntent = getIntent();
 
 		double targetLongitude = callingIntent.getDoubleExtra("targetLongitude", -1);
 		double targetLatitude = callingIntent.getDoubleExtra("targetLatitude", -1);
 		double currentLongitude = callingIntent.getDoubleExtra("currentLongitude", -1); 
 		double currentLatitude = callingIntent.getDoubleExtra("currentLatitude", -1);
 		currentLocation = new Location("");
 		currentLocation.setLongitude(currentLongitude);
 		currentLocation.setLatitude(currentLatitude);
 
 		angle = -1;
 		distanceToTarget = -1;
 		
 		if( targetLongitude >= 0 && targetLongitude >= 0 ) {
 			target = new Location("");
 			target.setLatitude(targetLatitude);
 			target.setLongitude(targetLongitude);
 			calculateDistanceAndBearing();
 		}
 		else {
 			target = null;
 		}
 		
 		image = (ImageView)findViewById(R.id.compassImage);
 		updateArrow();
 		image.setScaleType( ScaleType.CENTER );
 		image.setAdjustViewBounds( true );
 		
 		textView = (TextView)findViewById(R.id.compassDistanceToTarget);
 		textView.setGravity( Gravity.CENTER_HORIZONTAL );
 		updateDistanceText();
 		
 		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, this);
 	}
 	
 	public void calculateDistanceAndBearing() {
 		if( target != null && currentLocation != null ) {
 			distanceToTarget = currentLocation.distanceTo(target);
 			angle = currentLocation.bearingTo(target);		
 		}
 		else {
 			distanceToTarget = -1;
 			angle = -1;
 		}
 	}
 	public void updateDistanceText() {
 		DecimalFormat format = new DecimalFormat("#.##");
		if( distanceToTarget < 1000 )
 			textView.setText( Double.valueOf(format.format(distanceToTarget)) + " m");
		else if( distanceToTarget >= 1000 )
 			textView.setText( Double.valueOf(format.format(distanceToTarget/1000)) + " km" );
 		else
 			textView.setText( R.string.message_nolocationfix_text );
 	}
 	
 	public void updateArrow() {
 		Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.compassarrow );
 		int width = bitmapOrg.getWidth();
 		int height = bitmapOrg.getHeight();
 		Matrix matrix = new Matrix();
 		//matrix.postScale(1, 1);
 		matrix.setRotate(angle);
 		Bitmap rotatedBitmap = Bitmap.createBitmap( bitmapOrg, 0, 0, width, height, matrix, true);
 		BitmapDrawable bmd = new BitmapDrawable(rotatedBitmap);
 		image.setImageDrawable(bmd);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		currentLocation = location;
 		calculateDistanceAndBearing();
 		updateArrow();
 		updateDistanceText();
 	}
 
 	@Override
 	public void onProviderDisabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		// TODO Auto-generated method stub
 		
 	}
 }
