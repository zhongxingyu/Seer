 package com.change.kranti;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 import repository.IssueRepository;
 
 import java.io.File;
 
 public class CaptureIssueActivity extends Activity {
     private static int count = 0;
     private IssueRepository issueRepository;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         issueRepository = new IssueRepository(getApplicationContext());
         setContentView(R.layout.main);
     }
 
     public void captureIssueImage(View view) {
         String photoFile;
         photoFile = "Issue_" + count++ + ".jpg";
         File issueImage = new File(Environment.getExternalStorageDirectory(),photoFile);
         Intent captureIssueIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureIssueIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(issueImage));
         startActivityForResult(captureIssueIntent, 1);
     }
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1 && resultCode==Activity.RESULT_OK){
             Toast.makeText(this,"Issue Image was captured",Toast.LENGTH_LONG).show();
             ImageView issueImageView = (ImageView) findViewById(R.id.issue_image_capture);
             Bitmap issueImageBitmap = (Bitmap) data.getExtras().get("data");
             issueImageView.setImageBitmap(issueImageBitmap);
             return;
         }
         Toast.makeText(this,"Issue Image not captured !!",Toast.LENGTH_LONG).show();
     }
     public void submit(View view){
         EditText description = (EditText) findViewById(R.id.descriptionText);
         EditText title = (EditText) findViewById(R.id.titleText);
         String title1 =  title.getText().toString();
         String description1 =  description.getText().toString();
         issueRepository.createIssue(title1, description1);
     }
 }
