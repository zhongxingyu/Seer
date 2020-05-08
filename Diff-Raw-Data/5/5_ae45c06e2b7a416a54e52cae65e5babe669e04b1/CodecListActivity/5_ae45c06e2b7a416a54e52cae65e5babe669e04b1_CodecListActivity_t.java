 package com.crayonio.mediacodecquery;
 
 import java.util.ArrayList;
 
 import android.app.ActivityOptions;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class CodecListActivity extends ListActivity implements
 		OnItemClickListener {
 
 	final static String CODEC_INDEX = "codecIndex";
 	private Typeface robotoCondensedLight;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_codec_list);
		this.setTitle(getString(R.string.title_activity_codec_list));
 		ListView myListView = getListView();
 		myListView.setOnItemClickListener(this);
 
 		robotoCondensedLight = Typeface.createFromAsset(getAssets(),
 				"RobotoCondensed-Light.ttf");
 
 		final ArrayList<CodecInfo> codecInfoList = CodecInfoList
 				.getCodecInfoList();
 		setListAdapter(new ArrayAdapter<CodecInfo>(this,
 				R.layout.codec_list_row, R.id.codecName, codecInfoList) {
 
 			@Override
 			public View getView(int position, View convertView, ViewGroup parent) {
 
 				// Must always return just a View.
 				View rowView = super.getView(position, convertView, parent);
 
 				CodecInfo entry = codecInfoList.get(position);
 
 				TextView name = (TextView) rowView.findViewById(R.id.codecName);
 				TextView fullName = (TextView) rowView
 						.findViewById(R.id.codecFullName);
 				name.setTypeface(robotoCondensedLight);
 				fullName.setTypeface(robotoCondensedLight);
 
 				name.setText(entry.getCodecName());
 				fullName.setText(entry.getFullName());
 
 				// Log.d("GetView", "Getting rowView " + position + " : " +
 				// rowView.getId());
 
 				ImageView dirImg = (ImageView) rowView
 						.findViewById(R.id.directionImage);
 
 				if (entry.isDecoder() && entry.isEncoder()) {
 					dirImg.setImageDrawable(getResources().getDrawable(
 							R.drawable.codec_both));
 					// Log.d("GetView", "Setting Image in " + position);
 				} else if (entry.isDecoder()) {
 					dirImg.setImageDrawable(getResources().getDrawable(
 							R.drawable.codec_decoder));
 				} else if (entry.isEncoder()) {
 					dirImg.setImageDrawable(getResources().getDrawable(
 							R.drawable.codec_encoder));
 				} else {
 					dirImg.setImageDrawable(null);
 				}
 
 				return rowView;
 			}
 		});
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		// Log.d("OnClick", "Clicked " + arg2 + " " + codecInfoList.get(arg2));
 		Intent intent = new Intent(this, CodecDetailsActivity.class);
 		intent.putExtra(CODEC_INDEX, arg2);
 		Bundle bndlanimation = ActivityOptions.makeCustomAnimation(
 				getApplicationContext(), R.anim.list_activity_slide_enter,
 				R.anim.list_activity_slide_exit).toBundle();
 		startActivity(intent, bndlanimation);
 	}
 }
