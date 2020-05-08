 package c301.AdventureBook;
 
 import c301.AdventureBook.Controllers.LibraryManager;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.Models.Story;
 
 import com.example.adventurebook.R;
 import com.example.adventurebook.R.layout;
 import com.example.adventurebook.R.menu;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Base64;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ViewStoryActivity extends Activity {
 	LibraryManager lManagerInst; // Controller for the Library
 	StoryManager sManagerInst; // Controller for a story
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.view_story);
 
 		
 		populateData();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_story, menu);
 		return true;
 	}
 	
 	public void populateData(){
 
 		sManagerInst = StoryManager.getInstance();
 		sManagerInst.initContext(this);
 		
 		Story currentStory = sManagerInst.getStory();
 		
 		ImageView imageView = (ImageView)findViewById(R.id.storyThumnail);
 
 		if (currentStory.getImageByte() == null) {
 			imageView.setImageResource(R.drawable.default_image);
 		} else {
 			byte[] decodedString = Base64.decode(currentStory.getImageByte(), Base64.DEFAULT);
 			Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
 			imageView.setImageBitmap(decodedByte);
 		}
 		TextView titleText = (TextView) findViewById(R.id.storyTitle);
 		titleText.setText(currentStory.getTitle());
 
 		TextView authorText = (TextView)findViewById(R.id.authorTV);
 		authorText.setText(currentStory.getAuthor());
 
 		//TextView dateText = (TextView)findViewById(R.id.dateCreatedTV);
 		//dateText.setText(currentStory.getDate());
 		
		EditText storyDescription = (EditText) findViewById(R.id.storyDescription);
 		storyDescription.setText(currentStory.getDescription());
 	}
 	
 	public void launchViewPageActivity(View v){
 		Intent i = new Intent(this, ViewPageActivity.class);
 		startActivity(i);
 	}
 
 }
