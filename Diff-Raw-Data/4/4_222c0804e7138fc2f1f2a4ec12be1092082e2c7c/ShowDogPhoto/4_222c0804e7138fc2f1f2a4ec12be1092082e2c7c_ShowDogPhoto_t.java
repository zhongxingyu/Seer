 package mx.ferreyra.dogapp;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Base64;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ShowDogPhoto extends Activity {
 
     private final String PHOTO_ID = "photoId";
     private ImageView dogPhoto;
     private TextView dogPhotoFoot;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_show_dog_photo);
 
         // Load view controls
         dogPhoto     = (ImageView)findViewById(R.id.dog_photo);
         dogPhotoFoot = (TextView)findViewById(R.id.dog_photo_foot);
 
         // Check parameters for activity
         Bundle extras = getIntent().getExtras();
         if(extras == null) {
             // No parameters found
             // TODO Implement a treatment for this stage
         } else {
             // Parameters found
             Integer photoId = (Integer)extras.get(PHOTO_ID);
 
             if(photoId == null || photoId < 0) {
                 // Not valid photo id
                 // TODO Implement a treatment for this stage
             }
 
             // Invoke webservices using asynctask
             ShowDogPhotoTask task = new ShowDogPhotoTask(this);
             task.execute(new Integer[] {photoId});
         }
     }
 
     public void arrayToView(String[][] array) {
         // Load photo
         byte[] bytes = Base64.decode(array[0][3], Base64.DEFAULT);
         InputStream is = new ByteArrayInputStream(bytes);
         Bitmap bmp = BitmapFactory.decodeStream(is);
         dogPhoto.setImageBitmap(bmp);
 
        // Load photo foot
        dogPhotoFoot.setText(array[0][6]);
     }
 
     private class ShowDogPhotoTask extends AsyncTask<Integer, Integer, String[][]> {
 
         private final Context context;
         private ProgressDialog dialog;
 
         public ShowDogPhotoTask(Context context) {
             this.context = context;
         }
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             dialog = new ProgressDialog(context);
             dialog.setMessage(context.getString(R.string.please_wait_signing_up));
             dialog.show();
         }
 
         @Override
         protected String[][] doInBackground(Integer... params) {
             WsDogUtils ws = new WsDogUtils();
 
             // Query web service to find photo
             String[][] result = null;
             try {
                 result = ws.getFotosMascotaByIdFoto(params[0]);
             } catch (IOException e) {
                 Log.e(DogUtil.DEBUG_TAG, e.getMessage(), e);
             } catch (XmlPullParserException e) {
                 Log.e(DogUtil.DEBUG_TAG, e.getMessage(), e);
             }
 
             return result;
         }
 
         @Override
         protected void onPostExecute(String[][] result) {
             super.onPostExecute(result);
             // Stop and hide dialog
             dialog.dismiss();
 
             // Check result
             if(result == null) {
                 // Something wrong happened
                 // TODO finish this flow
             } else {
                 // Load data into view
                 arrayToView(result);
             }
         }
     }
 }
