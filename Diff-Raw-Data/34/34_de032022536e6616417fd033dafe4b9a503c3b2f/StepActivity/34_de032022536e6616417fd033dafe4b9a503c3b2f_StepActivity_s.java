 package com.raulete.evoluzzion;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 
 import com.raulete.evoluzzion.models.Jigsaw;
 import com.raulete.evoluzzion.models.Step;
 
 public class StepActivity extends Activity {
 
 	public static String STEP_ID = "step_id";
 	private long step_id;
 	private Step step;
 	private Jigsaw jigsaw;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.step_layout);
         setStepId();
     }
 	
 	public void onResume(){
 		super.onResume();
 		populateStep();
 	}
 	
 	public void shareStep(View view){
 		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 		sharingIntent.setType("image/jpeg");
 		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getStringToShare());
 		sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(step.image_uri));  
 		startActivity(Intent.createChooser(sharingIntent,"Share"));
 	}
 	
 	public String getStringToShare(){
 		String share = "";
 		if(step.comment.equals("")){
 			share = jigsaw.name + " - " + step.name + ". #Evoluzzion";
 		}else{
 			share = jigsaw.name + " - " + step.name + ". " + step.comment + " #Evoluzzion";
 		}
 		return share;
 	}
 	
 	public void populateStep(){
 		step.populateItem(this);
 	}
 	
 	public void setStepId(){
 		Intent intent = getIntent();
 		step_id = intent.getLongExtra(STEP_ID, 0);
 		step = new Step(this);
 		step.read(step_id);
 		jigsaw = new Jigsaw(this);
 		jigsaw.read(step.jigsaw_id);
 	}
 }
