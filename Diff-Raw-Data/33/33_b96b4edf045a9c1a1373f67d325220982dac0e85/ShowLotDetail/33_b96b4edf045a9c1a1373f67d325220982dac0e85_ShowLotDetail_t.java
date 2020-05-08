 package com.luckywc.jossstick;
 
 import java.text.DecimalFormat;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ShowLotDetail extends Activity{
 	private int mRanIndex=0;
 	private ImageView mLogImage;
 	private TextView lotTitle;
 	private TextView lotDesc;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);		
 		setContentView(R.layout.activity_show_detail);
 		mRanIndex=getIntent().getExtras().getInt("RanIndex");
 		mLogImage=(ImageView)findViewById(R.id.lotImage);
 		lotTitle=(TextView)findViewById(R.id.lotTitle);
		lotDesc=(TextView)findViewById(R.id.lotDesc);	
 		
		String [] titleS=getResources().getStringArray(R.array.lot_names);
 		String lotIdx=new DecimalFormat("00").format(mRanIndex+1);
 		this.mLogImage.setImageBitmap(Utility.getImageFromAssetsFile(this,lotIdx+".gif"));
 		this.lotDesc.setText(Utility.getStringFromAssetsFile(this,lotIdx+".txt"));
		this.lotTitle.setText(titleS[mRanIndex]);
 	}
 }
