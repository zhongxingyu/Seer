 package com.example.ivcam.demo;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class GreyScale2Dview extends Activity implements OnClickListener {
 	private Button mCaptureButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_grey_scale2_dview);
 		mCaptureButton = (Button) findViewById(R.id.capture_button);
 		mCaptureButton.setOnClickListener(this);
 		set2DImageView();
 	}
 
 	private void set2DImageView() {
 		InputStream stream = getResources().openRawResource(R.raw.hand0_0000);
 		byte[] rawData = new byte[614400];
 		try {
 			stream.read(rawData, 0, 614400);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		int[] colors = new int[640 * 480];
 		int j = 0;
 		int i = 0;
 		for (; i < rawData.length;) {
			colors[j++] = rawData[i] + (rawData[i + 1] << 8);
 			i += 2;
 		}
 		Bitmap bmpGrayscale = Bitmap.createBitmap(colors, 640, 480,
				Bitmap.Config.RGB_565);
 		ImageView image = (ImageView) findViewById(R.id.gray_scale_2d_image);
 		image.setImageBitmap(bmpGrayscale);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.capture_button:
 			Intent grey3DView = new Intent(this, Grey3DView.class);
 			startActivity(grey3DView);
 			break;
 		}
 	}
 }
