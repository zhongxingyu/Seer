 package com.sbasite.activity;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 
 import com.esri.core.geometry.Point;
 import com.esri.core.tasks.ags.identify.IdentifyResult;
 import com.sbasite.R;
 import com.sbasite.model.Site;
 import com.sbasite.task.LoadImageAsyncTask;
 import com.sbasite.task.LoadImageAsyncTask.LoadImageAsyncTaskResponder;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class SiteDetailActivity extends Activity implements LoadImageAsyncTaskResponder {
 	
 	//private static final String TAG = SiteDetailActivity.class.getSimpleName();
 	private IdentifyResult site;
 	private Button emailButton;
 	private TextView siteNameTextView;
 	private TextView siteCodeTextView;
 	private TextView siteAddress1TextView;
 	private TextView siteAddress2TextView;
 	private TextView siteLayerTextView;
 	private TextView siteStatusTextView;
 	private TextView siteCoordinatesTextView;
 	private TextView siteTypeTextView;
 	private TextView siteHeightTextView;
 	private TextView siteElevationTextView;
 	private TextView siteMTATextView;
 	private TextView siteBTATextView;
 	private ImageView siteImage;
 	protected ProgressDialog progressDialog;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		site = (IdentifyResult) getIntent().getSerializableExtra("SITE");
 		setContentView(R.layout.sitedetailactivity);
 	}
 
 //	@Override
 //	public void onBackPressed() {
 //		super.onBackPressed();
 //		site = null;
 //		finish();
 //	}
 
 	private void setupViews() {
 		siteNameTextView = (TextView)findViewById(R.id.TextView_SiteName);
 		siteCodeTextView = (TextView)findViewById(R.id.TextView_SiteCode);
 		siteAddress1TextView = (TextView)findViewById(R.id.TextView_SiteAddress1);
 		siteAddress2TextView = (TextView)findViewById(R.id.TextView_SiteAddress2);
 		siteLayerTextView = (TextView)findViewById(R.id.TextView_SiteLayer);
 		siteStatusTextView = (TextView)findViewById(R.id.TextView_SiteStatus);
 		siteCoordinatesTextView = (TextView)findViewById(R.id.TextView_SiteCoordinates);
 		siteTypeTextView = (TextView)findViewById(R.id.TextView_SiteType);
 		siteHeightTextView = (TextView)findViewById(R.id.TextView_SiteHeight);
 		siteElevationTextView = (TextView)findViewById(R.id.TextView_SiteElevation);
 		siteMTATextView = (TextView)findViewById(R.id.TextView_SiteMTA);
 		siteBTATextView = (TextView)findViewById(R.id.TextView_SiteBTA);
         
         siteImage = (ImageView)findViewById(R.id.ImageView_SiteThumbnail);
         siteImage.setVisibility(View.INVISIBLE);
         
 		
 		emailButton = (Button) findViewById(R.id.Button_SubmitInquiry);
         emailButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 				emailIntent.setType("text/plain");
 				String[] recipients = new String[]{(String)site.getAttributes().get("Email"), "siteinquiry@sbasite.com"};
 				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
 				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, (String)site.getAttributes().get("SiteName"));
 				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, site.getAttributes().toString());
 				startActivity(Intent.createChooser(emailIntent, "Send mail..."));
 			}
         });
         
         refreshViews();
 		loadSiteThumbnail();
 	}
 
 	@Override
 	  protected void onResume() {
 	    super.onResume();
	    setupViews();
 	}
 	
 	private void refreshViews() {
 		Map<String, Object> attributes = site.getAttributes();
 		Point point = (Point)site.getGeometry();
 		siteNameTextView.setText((String)attributes.get("SiteName"));
 		siteCodeTextView.setText((String)attributes.get("SiteCode"));
 		siteAddress1TextView.setText((String)attributes.get("Address1"));
 		siteAddress2TextView.setText((String)attributes.get("City") + ", " + (String)attributes.get("State") + " " + (String)attributes.get("Zip"));
 		siteLayerTextView.setText(site.getLayerName());
 		siteStatusTextView.setText((String)attributes.get("Status"));
 		siteCoordinatesTextView.setText(point.getX() + ", " + point.getY());
 		siteTypeTextView.setText((String)attributes.get("Type"));
 		siteHeightTextView.setText((String)attributes.get("Height"));
 		siteElevationTextView.setText((String)attributes.get("AGL"));
 		siteMTATextView.setText((String)attributes.get("MtaName"));
 		siteBTATextView.setText((String)attributes.get("BtaName"));
 	}
 	
 	private void loadSiteThumbnail() {
 		try {
 			String urlString = String.format("http://map.sbasite.com/Mobile/GetImage?SiteCode=%s&width=600&height=600", (String)site.getAttributes().get("SiteCode"));
 			URL requestURL = new URL(urlString);
 			new LoadImageAsyncTask(this).execute(requestURL);
 		} catch (MalformedURLException e) {
 			
 		}
 	}
 
 	public void imageLoading() {
 		
 	}
 
 	public void imageLoadCancelled() {
 		
 	}
 
 	public void imageLoaded(Drawable drawable) {
 		
 		if (null == drawable) {
 			siteImage.setVisibility(View.GONE);
 		} else {
 			siteImage.setImageDrawable(drawable);
 			siteImage.setVisibility(View.VISIBLE);
 			siteImage.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 				     Intent intent = new Intent(SiteDetailActivity.this, SiteImageViewActivity.class);
 				     intent.putExtra("SiteCode", (String)site.getAttributes().get("SiteCode"));
 				     startActivity(intent);
 				}
 			});
 		}
 	}
 	
 }
