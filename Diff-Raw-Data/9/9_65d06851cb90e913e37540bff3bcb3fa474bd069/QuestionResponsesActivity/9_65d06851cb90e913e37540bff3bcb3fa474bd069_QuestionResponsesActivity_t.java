 package me.unsharable.activities;
 
 import java.io.File;
 import java.util.List;
 
 import me.unsharable.models.Game;
 import me.unsharable.models.Question;
 import me.unshareable.R;
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class QuestionResponsesActivity extends Activity{
 	
 	private Uri imageUri;
 
 	public void takePhoto(View view) {
 	    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
 	    File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
 	    intent.putExtra(MediaStore.EXTRA_OUTPUT,
 	            Uri.fromFile(photo));
 	    imageUri = Uri.fromFile(photo);
 	    startActivityForResult(intent, 0);
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    super.onActivityResult(requestCode, resultCode, data);
 	    switch (requestCode) {
 	    case 0:
 	        if (resultCode == Activity.RESULT_OK) {
 	            Uri selectedImage = imageUri;
 	            getContentResolver().notifyChange(selectedImage, null);
 	            ImageView imageView = (ImageView) findViewById(R.id.myimgview); //view to upload
 	            ContentResolver cr = getContentResolver();
 	            Bitmap bitmap;
 	            try {
 	                 bitmap = android.provider.MediaStore.Images.Media
 	                 .getBitmap(cr, selectedImage);
 
 	                imageView.setImageBitmap(bitmap);
 	                Toast.makeText(this, selectedImage.toString(),
 	                        Toast.LENGTH_LONG).show();
 	            } catch (Exception e) {
 	                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
 	                        .show();
 	                Log.e("Camera", e.toString());
 	            }
 	        }
 	    }
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_response);
 
 		List<Question> questions = Game.getAllQuestions();
 		ListView lv = (ListView) findViewById(R.id.questionList);
 		ArrayAdapter<Question> questionAdapter = 
 				new ArrayAdapter<Question>(this, android.R.layout.simple_list_item_1, questions);
 		lv.setAdapter(questionAdapter);
 	}
 }
