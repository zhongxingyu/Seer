 package no.uib.info331.geomusic;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import no.uib.info331.geomusic.utils.FetchArtistAsyncTask;
 
 import de.umass.lastfm.Artist;
import de.umass.lastfm.ImageSize;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ArtistInfoActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_artist_info);
         
         Intent intent = getIntent();
         String artist = intent.getStringExtra("ArtistName");
         new FetchArtistAsyncTask(this).execute(artist);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_artist_info, menu);
         return true;
     }
 
 	public void showArtistInfo(Artist result) {
 
 		TextView artistName = (TextView)findViewById(R.id.artist_name);
 		artistName.setText(result.getName());
 		
 		ImageView artistImage = (ImageView)findViewById(R.id.artist_image);
 		
 		try {
	        InputStream is = (InputStream) new URL(result.getImageURL(ImageSize.LARGE)).getContent();
 	        Drawable d = Drawable.createFromStream(is, "Last.fm");
 	        artistImage.setImageDrawable(d);
 	    } catch (Exception e) {
 	        Log.d("ArtistInfoActivity", "Could not get artist image from Last.fm");
 	    }
 		
 		TextView artistSummary = (TextView)findViewById(R.id.artist_summary_text);
 		artistSummary.setText(result.getWikiSummary());
 	}
 }
