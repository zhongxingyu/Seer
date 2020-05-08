 package eu.hack4europe.postcard;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import eu.hack4europe.europeana4j.EuropeanaItem;
 
 public class DescriptionActivity extends Activity implements View.OnClickListener {
 
     public static final String MIME_TYPE = "image/jpeg";
 
     private ImageView image;
     private TextView title;
     private TextView description;
     private Button shareButton;
 
     private final PostcardBitmapLoader loader = new PostcardBitmapLoader();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.description);
 
         image = (ImageView) findViewById(R.id.image);
         title = (TextView) findViewById(R.id.title);
         description = (TextView) findViewById(R.id.description);
         shareButton = (Button) findViewById(R.id.shareButton);
 
         PostcardBitmapLoader pbl = new PostcardBitmapLoader();
         PostcardModel model = PostcardApplication.getInstance().getModel();
         pbl.loadAsync(model.getSelectedItem(), image);
 
         title.setText(model.getSelectedItem().getTitle());
         description.setText(model.getSelectedItem().getDescription());
 
         shareButton.setOnClickListener(this);
     }
 
     private void shareIt() {
 		if(!PostcardApplication.getInstance().haveInternet(DescriptionActivity.this)){
 			return;		
 		}
         PostcardModel model = PostcardApplication.getInstance().getModel();
         EuropeanaItem item = model.getSelectedItem();
 
         Bitmap bitmap = loader.load(item);
         Context context = getApplicationContext();
         String uri = MediaStore.Images.Media.insertImage(
                 context.getContentResolver(),
                 bitmap,
                 item.getTitle(),
                 item.getDescription()
         );
 
         Log.i("postcard", "sending image " + uri);
 
         Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
         sharingIntent.setType(MIME_TYPE);
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSubject) + model.getLoadedCity());
        sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareText) + " ");
         sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));
 
         startActivity(Intent.createChooser(sharingIntent, "Select a Way to Send"));
     }
 
     @Override
     public void onClick(View view) {
         if (view == shareButton) {
             shareIt();
         }
     }
 }
