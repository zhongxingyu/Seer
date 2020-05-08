 package com.mobilewiki;
 
 import android.app.Activity;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.Html.ImageGetter;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PreviewActivity extends Activity{
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.preview_edit); // TODO layout 
     }
     
 	private ImageGetter imgGetter = new ImageGetter() {
 		public Drawable getDrawable(String source) {
 			Drawable drawable = getResources().getDrawable(
 					R.drawable.borat);
 			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
 					drawable.getIntrinsicHeight());
 			return drawable;
 		}
 	};
     
     @Override
     public void onStart(){
     	 super.onStart();
          Bundle parameters = getIntent().getExtras();
          String previewText = null;
          if (null != parameters) {
         	 previewText = parameters.getString("PREVIEW_TEXT");
          }
          else {
          }
          
          TextView article_cont = (TextView) findViewById(R.id.article_content);
         
          article_cont.setText(Html.fromHtml(previewText, imgGetter, null));
          
     }
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preview, menu); 
         return true;
     } 
     
    /*
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.preview_art: 
 	        	Toast.makeText(this, "This is just a preview!", Toast.LENGTH_LONG).show();
 	           	return super.onOptionsItemSelected(item);
 	        case R.id.save_art:
 	        	Toast.makeText(this, "Article saved!", Toast.LENGTH_SHORT).show(); 
 	        	finish();
 	        	return super.onOptionsItemSelected(item);
 	        case R.id.cancel_edit:
 	        	finish();
 	        	return super.onOptionsItemSelected(item);
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
	*/
 }
