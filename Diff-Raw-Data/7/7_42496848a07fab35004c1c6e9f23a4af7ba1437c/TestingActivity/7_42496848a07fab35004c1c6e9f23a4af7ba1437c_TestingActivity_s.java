 package org.SdkYoungHeads.DoIKnowYou;
 
 import java.util.UUID;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.Toast;
 
 public class TestingActivity extends Activity implements OnCheckedChangeListener {
 	private Person guessing;
	private Group group;
	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
		
		// TODO: add choices...
		// TODO: accept group to test...
		group = ((Application)getApplication()).selectedGroup;
 		setChoices();
 	}
 	
 	public void setChoices() {
 		guessing = ((Application)getApplication()).currentTester.getTestCase();
 		if (guessing == null) {
 			finish();
 		} else {
 			RadioGroup rg = (RadioGroup)findViewById(R.id.testingChoices);
 			rg.removeAllViews();
 			RadioButton rb = new RadioButton(this);
 			rb.setText(guessing.getName());
 		
 			rg.addView(rb);
 			rg.setOnCheckedChangeListener(this);
 		
 			ImageView iw = (ImageView)findViewById(R.id.imageView1);
 			Bitmap bmp = guessing.getSomePhoto();
 			if (bmp != null) {
 				iw.setImageBitmap(guessing.getSomePhoto());
 			}
 		}
 	}
 
 	public void onCheckedChanged(RadioGroup paramRadioGroup, int paramInt) {
 		RadioButton rb = (RadioButton)paramRadioGroup.findViewById(paramInt);
 		Toast.makeText(getBaseContext(), rb.getText(), 2000).show();
 		// TODO: poslat testeru, jak to vyslo
 		// TODO: poprosit tester o dalsi...
 		Tester t = ((Application)getApplication()).currentTester;
 		t.putResult(true); // TODO
 		setChoices();
 	}
 }
