 package madisonmay.sensordebugger;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /**
  * Created by evan on 9/15/13.
  */
 public class SnapshotDetailActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_snapshot_detail);
 
         Intent intent = getIntent();
 
         String fileName = intent.getStringExtra("file");
 
         TextView title = (TextView) findViewById(R.id.snapshotTitle);
        TextView snapshotText = (TextView) findViewById(R.id.snapshotText);
 
         title.setText(fileName);
         StringBuilder fileText = new StringBuilder();
         try{
             FileInputStream fis = openFileInput(fileName);
             InputStreamReader inputStreamReader = new InputStreamReader(fis);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
             String line;
             while ((line = bufferedReader.readLine()) != null){
                 fileText.append(line);
                 fileText.append('\n');
             }
 
         }catch (IOException e){
             Log.e("IOException", e.getMessage());
         }
 
        snapshotText.setText(fileText.toString());
 
     }
 }
