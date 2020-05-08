 package chvck.colourMate.activities;
 
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.LayerDrawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import chvck.colourMate.R;
 
 public class CompareColours extends ColourActivity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.compare);
 	    
 	    ImageView origColourView = (ImageView) findViewById(R.id.originalColourView);
 	    
 	    LinearLayout ll = (LinearLayout) findViewById(R.id.ll_compare);
 
         Bundle extras = getIntent().getExtras();
         int[] newColours = extras.getIntArray("colours");
 	    final int origColour = extras.getInt("origColour");
 	    
	    setViewColour(origColourView, origColour);   
	    origColourView.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
			    use(origColour);
			}
        }); 
 	    
 	    //ll.addView(origColourView);
 	    
 	    for (final int newColour : newColours) {
 	    	ImageView imageView = new ImageView(this);
 	    	setViewColour(imageView, newColour);
 	    	
 	    	imageView.setOnClickListener(new OnClickListener() {
 		    	public void onClick(View v) {
 				    use(newColour);
 				}
 	        });
 	    	ll.addView(imageView);
 	    }
 	}
 	
 	private void setViewColour(ImageView view, int colour) {
 		view.setBackgroundColor(colour);
     	Drawable backgroundRes = view.getBackground();
 		Drawable drawableRes = this.getResources().getDrawable(R.drawable.white_background);
 		Drawable[] drawableLayers = { backgroundRes, drawableRes };
 		LayerDrawable ld = new LayerDrawable(drawableLayers);
 		view.setBackgroundDrawable(ld);
 	}
 	
 	private void use(int colour) {
 		Intent intent = new Intent();
 		intent.setClass(this, chvck.colourMate.activities.SelectedColour.class);
 		intent.putExtra("colour", colour);
 		startActivityForResult(intent, 0);
 	}
 }
