 package org.cvltvre.view;
 
 import org.cvltvre.R;
 import org.cvltvre.vo.MuseumVO;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.method.ScrollingMovementMethod;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MuseumMainActivity extends Activity{
 	
 	private static MuseumVO museumVO;
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.museuminfo);
 		museumVO=null;
 		for(MuseumVO museumVOTemporal:LoadingActivity.museumVOs){
 			if(museumVOTemporal.getId().equals(this.getIntent().getExtras().get("id"))){
 				museumVO=museumVOTemporal;
 			}
 		}
 		TextView name=(TextView) findViewById(R.id.museumname);
 		name.setText(museumVO.getTitle());
 		if(museumVO.getBitmap()!=null){
 			ImageView imageView=(ImageView) findViewById(R.id.museumimage);
 			imageView.setImageBitmap(museumVO.getBitmap());
 		}
 		
 		TextView distanceView=(TextView) findViewById(R.id.museumdistance);
 		String distance=museumVO.getDistance().substring(0,museumVO.getDistance().indexOf(".")+2);
 		distanceView.setText(distance+" Km");
 		TextView phone=(TextView) findViewById(R.id.museumphone);
 		phone.setText(museumVO.getPhone());
 		TextView website=(TextView) findViewById(R.id.museumwebsite);
 		website.setText(museumVO.getWebsite());
		TextView address=(TextView) findViewById(R.id.museumaddress);
		address.setText(Html.fromHtml(museumVO.getAddress()));
 		TextView description=(TextView) findViewById(R.id.museumdescription);
 		description.setText(Html.fromHtml(museumVO.getDescription()));
 		description.setMovementMethod(new ScrollingMovementMethod());
 		
 		Button buttonCall =(Button) findViewById(R.id.call);
 		Button websiteButton =(Button) findViewById(R.id.website);
 		
 		buttonCall.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent=new Intent(android.content.Intent.ACTION_DIAL);
 				intent.setData(Uri.parse("tel:"+museumVO.getPhone()));
                 startActivity(intent);
 			}
 		});
 		
 		websiteButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_VIEW);
 				if(museumVO.getWebsite().startsWith("http")){
 					intent.setData(Uri.parse(museumVO.getWebsite()));
 				}else{
 					intent.setData(Uri.parse("http://"+museumVO.getWebsite()));	
 				}
 				startActivity(intent);
 			}
 		});
 		
 		
 	}
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		LoadingActivity.startListeners();
 	}
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		LoadingActivity.killListeners();
 	}
 
 }
