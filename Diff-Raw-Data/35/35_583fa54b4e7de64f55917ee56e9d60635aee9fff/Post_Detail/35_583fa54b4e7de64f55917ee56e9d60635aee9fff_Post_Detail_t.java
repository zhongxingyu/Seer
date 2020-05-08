 package de.hska.shareyourspot.android.activites;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.android.gms.maps.model.LatLng;
 
 import de.hska.shareyourspot.android.R;
 import de.hska.shareyourspot.android.domain.Comment;
 import de.hska.shareyourspot.android.domain.Comments;
 import de.hska.shareyourspot.android.domain.Parties;
 import de.hska.shareyourspot.android.domain.Party;
 import de.hska.shareyourspot.android.domain.Post;
 import de.hska.shareyourspot.android.domain.User;
 import de.hska.shareyourspot.android.helper.AlertHelper;
 import de.hska.shareyourspot.android.helper.UserStore;
 import de.hska.shareyourspot.android.restclient.RestClient;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class Post_Detail extends Activity {
 	
 	private UserStore uStore = new UserStore();
 	private Context ctx = this;
 	private RestClient restClient = new RestClient();
 	protected Post post; 
 	public final String postId = "postId";
 	public final String longitude = "longitude";
 	public final String latitude = "latitude";
 	public String imageUrl = "http://hskaebusiness.square7.ch/ShareYourSpot/";
 	public String imageEnd = ".jpg";
 	
 	private ListView listComments;
 	private ArrayList<String> shownComments;
 	private List<Comment>foundComments = new ArrayList<Comment>();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		long postIdent = getIntent().getLongExtra(postId, 0);
 		setContentView(R.layout.activity_post_detail);
 		this.post = this.restClient.getPost(postIdent);
 		
 		if(this.post != null)
 		{
 			ImageView imageView = (ImageView) findViewById(R.id.imageViewPostDetail);
 			new DownloadImageTask(imageView).execute(imageUrl + this.post.getPostId() + imageEnd);
 			TextView spotterName = (TextView) findViewById(R.id.textView_spotter);
 		spotterName.setText("# Shared by " + this.post.getCreatedByUser().getName());
 		
 		TextView spotText = (TextView) findViewById(R.id.textView_spot_text);
 		spotText.setText(this.post.getText());
 		}
 		else
 		{
 			new AlertHelper(ctx, R.string.dataLoadFailureTitle,
 					R.string.dataLoadFailureText, "Next Try").fireAlert();
 		}
 		
 		this.foundComments = new ArrayList<Comment>();
 		this.shownComments = new ArrayList<String>();
 		this.listComments = (ListView) findViewById(R.id.listView_comments);
 		List<Comment> comments = new ArrayList<Comment>();
 		if(this.post.getComments()!=null){
 		comments = this.post.getComments();
 		}
 		if (comments != null) {
 
 			this.foundComments = comments;
 
 			for (Comment comment : this.foundComments) {
 				if (comment.getText() != null) {
 					this.shownComments.add(comment.getCreatedByUsername() + ": " + comment.getText());
 				}
 			}
 		}
 //		else{
 //		this.meineListe.add("");}
 
 		ListAdapter listenAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, this.shownComments);
 
 		this.listComments.setAdapter(listenAdapter);
 
 		this.listComments.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				String item = ((TextView) view).getText().toString();
 
 				Toast.makeText(getBaseContext(), item, Toast.LENGTH_LONG)
 						.show();
 
 				
 			}
 		});
 		
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.post_detail, menu);
 		return true;
 	}
 	
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.post__detail, menu);
 //		return true;
 //	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	  switch (item.getItemId()) {
 	  case R.id.action_logout:
 		  	uStore.logout(ctx);
 	        finish();
 	    break;
 	    
 	  case android.R.id.home:
 		  	onBackPressed();
 	        finish();
 	    break;
 	  
 	  default:
 	    break;
 	  }
 
 	  return true;
 	} 
 	
 		
 	
 	 
 		public void startMap(View view) {
 			//TODO Karlruhe gegen Postdatenersetzen
 			LatLng test = new LatLng(49.014,  8.4043);
 			Intent intent = new Intent(this, GoogleMaps.class);
 			intent.putExtra(this.longitude, test.longitude);
 			intent.putExtra(this.latitude,  test.latitude);
 			startActivity(intent);
 		}
 	    	
 	
 	
 	
 	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 	    ImageView bmImage;
 
 	    public DownloadImageTask(ImageView bmImage) {
 	        this.bmImage = bmImage;
 	    }
 
 	    protected Bitmap doInBackground(String... urls) {
 	        String urldisplay = urls[0];
 	        Bitmap mIcon11 = null;
 	        try {
 	            InputStream in = new java.net.URL(urldisplay).openStream();
 	            mIcon11 = BitmapFactory.decodeStream(in);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        return mIcon11;
 	    }
 
 	    protected void onPostExecute(Bitmap result) {
 	        bmImage.setImageBitmap(result);
 	    }
 	}
 	
 	public void addComment(View view){
 		EditText editText = (EditText) findViewById(R.id.editText_enter_text);
 		Comment comment = new Comment();
 		comment.setText(editText.getText().toString());
 		long postIdent = getIntent().getLongExtra(postId, 0);
 		comment.setPostId(Long.valueOf(postIdent));
 		
 		RatingBar ratingBar= (RatingBar)findViewById(R.id.ratingBar_post_detail);
 		Double dbl = (double) ratingBar.getRating();
 		comment.setRating(dbl);
 		
 		User user = null;
 		try {
 			user = uStore.getUser(ctx);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		comment.setCreatedByUsername(user.getName());
 		
 		this.restClient.addComment(comment);
 		
		editText.setText("");
		ratingBar.setRating(0);
//		Intent intent = new Intent(this, Post_Detail.class);
//		intent.putExtra(this.postId, postIdent);
		recreate();
//		startActivity(intent);
 				
 	}
 
 }
