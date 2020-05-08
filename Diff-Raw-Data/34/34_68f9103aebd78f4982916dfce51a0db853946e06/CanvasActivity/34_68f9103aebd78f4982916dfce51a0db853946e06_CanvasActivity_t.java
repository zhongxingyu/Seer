 package com.csewannabe;
 


 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
 import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout;
import android.widget.Toast;
 
 public class CanvasActivity extends Activity {
 	MainCanvasView MainCanvas;
 	ImageButton clearButton;
 	ImageButton editButton;
 	ImageButton submitButton;
 	EditText answerBox;
 	Bitmap picture;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		RelativeLayout canvasLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams params = 
				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		
 		// Find views
 		picture = BitmapFactory.decodeResource(getResources(), R.drawable.list_background);
 		
 		MainCanvas = new MainCanvasView(this, picture);
 		clearButton = new ImageButton(this);
 		editButton = new ImageButton(this);
 		submitButton = new ImageButton(this);
 		answerBox = new EditText(this);
 		
		// Creates clear button
		clearButton.setImageDrawable(Drawable.createFromPath("R.drawable.clear.png"));
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		canvasLayout.addView(clearButton, params);
		clearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(CanvasActivity.this, "HIHIHI", Toast.LENGTH_SHORT).show();
			}
		});
		
 		
		setContentView(R.layout.canvas_activity);
 	}
 
 	
 
 }
