 package com.zrd.zr.letuwb;
 
 import com.zrd.zr.letuwb.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class AboutActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.about);
 		
 		String sVersionName;
 		try {
 			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
 			sVersionName = info.versionName;
 		} catch (NameNotFoundException e) {
 			// TODO Auto-generated catch block
 			sVersionName = "";
 			e.printStackTrace();
 		}
 		
 		TextView textVersion = (TextView) findViewById(R.id.tvVersion);
 		textVersion.setText(getString(R.string.about_version) + sVersionName);
 		
 		Button btnOfficialSite = (Button) findViewById(R.id.btnOfficialSite);
 		String landingURL = EntranceActivity.URL_SITE + "landing";
 		btnOfficialSite.setText(
 			Html.fromHtml(
 				String.format("<a href='%s'>%s</a>", landingURL, landingURL)
 			)
 		);
 		btnOfficialSite.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Uri uri = Uri.parse(EntranceActivity.URL_SITE + "landing");
 				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                 startActivity(intent);
 			}
 			
 		});
 		
 		Button btnContact = (Button) findViewById(R.id.btnContact);
 		btnContact.setText(
 			Html.fromHtml(
 				"<a href='mailto:ralphchiu1@gmail.com'>ralphchiu1@gmail.com</a>"
 			)
 		);
 		btnContact.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(Intent.ACTION_SEND);
 				String[] tos = {"ralphchiu1@gmail.com"};
 				intent.putExtra(Intent.EXTRA_EMAIL, tos);
 				intent.setType("text/plain");
 				startActivity(intent);
 			}
 			
 		});
 		
 		ImageView ivShare = (ImageView)findViewById(R.id.ivShare);
 		ivShare.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("text/*");
 				intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sharing_title));
                 intent.putExtra(
                 	Intent.EXTRA_TEXT, 
                 	getString(R.string.sharing_title)
                	+ "\"" + getString(R.string.app_name) + "\" "
                 		+ getString(R.string.sharing_content)
                 );
                 startActivity(Intent.createChooser(intent, getTitle()));
 			}
 			
 		});
 	}
 
 }
