 package duke.cs.fall2012.catseverywhere;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ImageUpload extends Activity {
 	private static final int PICK_IMAGE = 1;
 	private ImageView imgView;
 	private Button upload;
 	private EditText caption;
 	private Bitmap bitmap;
 	private ProgressDialog dialog;
 	private HttpEntity myResEntity;
 	private TextView tv, res;
 	private String filePath;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.imageupload);
 
 		imgView = (ImageView) findViewById(R.id.ImageView);
 		upload = (Button) findViewById(R.id.Upload);
 		caption = (EditText) findViewById(R.id.Caption);
 		
 		tv = (TextView)findViewById(R.id.tv);
         res = (TextView)findViewById(R.id.res);
 		
 		
 		upload.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				if (bitmap == null) {
 					Toast.makeText(getApplicationContext(),
 							"Please select image", Toast.LENGTH_SHORT).show();
 				} else {
 					dialog = ProgressDialog.show(ImageUpload.this, "Uploading",
 							"Please wait...", true);
 					//new ImageUploadTask().execute();
 					new Thread(new Runnable() {
 						public void run() {
 							doFileUpload(filePath);
 						}
 					}).start();
 				}
 			}
 		});
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.imageupload_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.ic_menu_gallery:
 			try {
 				Intent intent = new Intent();
 				intent.setType("image/*");
 				intent.setAction(Intent.ACTION_GET_CONTENT);
 				startActivityForResult(
 						Intent.createChooser(intent, "Select Picture"),
 						PICK_IMAGE);
 			} catch (Exception e) {
 				Toast.makeText(getApplicationContext(),
 						e.getMessage(),
 						Toast.LENGTH_LONG).show();
 				Log.e(e.getClass().getName(), e.getMessage(), e);
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case PICK_IMAGE:
 			if (resultCode == Activity.RESULT_OK) {
 				Uri selectedImageUri = data.getData();
 				//String filePath = null;
 
 				try {
 					// OI FILE Manager
 					String filemanagerstring = selectedImageUri.getPath();
 
 					// MEDIA GALLERY
 					String selectedImagePath = getPath(selectedImageUri);
 					System.out.println("IMAGE PATH: " +  selectedImagePath);
 					if (selectedImagePath != null) {
 						filePath = selectedImagePath;
 					} else if (filemanagerstring != null) {
 						filePath = filemanagerstring;
 					} else {
 						Toast.makeText(getApplicationContext(), "Unknown path",
 								Toast.LENGTH_LONG).show();
 						Log.e("Bitmap", "Unknown path");
 					}
 
 					if (filePath != null) {
 						decodeFile(filePath);
 					} else {
 						bitmap = null;
 					}
 				} catch (Exception e) {
 					Toast.makeText(getApplicationContext(), "Internal error",
 							Toast.LENGTH_LONG).show();
 					Log.e(e.getClass().getName(), e.getMessage(), e);
 				}
 			}
 			break;
 		default:
 		}
 	}
 
 	class ImageUploadTask extends AsyncTask<Void, Void, String> {
 		@Override
 		protected String doInBackground(Void... unsued) {
 			try {
 				HttpClient httpClient = new DefaultHttpClient();
 				HttpContext localContext = new BasicHttpContext();
 				HttpPost httpPost = new HttpPost(
 						getString(R.string.WebServiceURL)
 								+ "/cfc/iphonewebservice.cfc?method=TestUploadPhoto");
 
 				MultipartEntity entity = new MultipartEntity(
 						HttpMultipartMode.BROWSER_COMPATIBLE);
 
 				ByteArrayOutputStream bos = new ByteArrayOutputStream();
 				bitmap.compress(CompressFormat.JPEG, 100, bos);
 				byte[] data = bos.toByteArray();
 				entity.addPart("returnformat", new StringBody("json"));
 				entity.addPart("uploaded", new ByteArrayBody(data,
 						"myImage.jpg"));
 				entity.addPart("photoCaption", new StringBody(caption.getText()
 						.toString()));
 				httpPost.setEntity(entity);
 				HttpResponse response = httpClient.execute(httpPost,
 						localContext);
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(
 								response.getEntity().getContent(), "UTF-8"));
 
 				String sResponse = reader.readLine();
 				return sResponse;
 			} catch (Exception e) {
 				if (dialog.isShowing())
 					dialog.dismiss();
 				Toast.makeText(getApplicationContext(),
 						e.getMessage(),
 						Toast.LENGTH_LONG).show();
 				Log.e(e.getClass().getName(), e.getMessage(), e);
 				return null;
 			}
 
 			// (null);
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... unsued) {
 
 		}
 
 		@Override
 		protected void onPostExecute(String sResponse) {
 			try {
 				if (dialog.isShowing())
 					dialog.dismiss();
 
 				if (sResponse != null) {
 					JSONObject JResponse = new JSONObject(sResponse);
 					int success = JResponse.getInt("SUCCESS");
 					String message = JResponse.getString("MESSAGE");
 					if (success == 0) {
 						Toast.makeText(getApplicationContext(), message,
 								Toast.LENGTH_LONG).show();
 					} else {
 						Toast.makeText(getApplicationContext(),
 								"Photo uploaded successfully",
 								Toast.LENGTH_SHORT).show();
 						caption.setText("");
 					}
 				}
 			} catch (Exception e) {
 				Toast.makeText(getApplicationContext(),
 						e.getMessage(),
 						Toast.LENGTH_LONG).show();
 				Log.e(e.getClass().getName(), e.getMessage(), e);
 			}
 		}
 	}
 
 	public String getPath(Uri uri) {
 		String[] projection = { MediaStore.Images.Media.DATA };
 		Cursor cursor = managedQuery(uri, projection, null, null, null);
 		if (cursor != null) {
 			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
 			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
 			int column_index = cursor
 					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 			cursor.moveToFirst();
 			return cursor.getString(column_index);
 		} else
 			return null;
 	}
 
 	public void decodeFile(String filePath) {
 		// Decode image size
 		BitmapFactory.Options o = new BitmapFactory.Options();
 		o.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(filePath, o);
 
 		// The new size we want to scale to
 		final int REQUIRED_SIZE = 1024;
 
 		// Find the correct scale value. It should be the power of 2.
 		int width_tmp = o.outWidth, height_tmp = o.outHeight;
 		int scale = 1;
 		while (true) {
 			if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
 				break;
 			width_tmp /= 2;
 			height_tmp /= 2;
 			scale *= 2;
 		}
 
 		// Decode with inSampleSize
 		BitmapFactory.Options o2 = new BitmapFactory.Options();
 		o2.inSampleSize = scale;
 		bitmap = BitmapFactory.decodeFile(filePath, o2);
 
 		imgView.setImageBitmap(bitmap);
 
 	}
 	
 	private void doFileUpload(String filePath){
 		 
         File file1 = new File(filePath);
         
         String urlString = "http://squashysquash.com/CatsEverywhere/uploadfile.php";
         try
         {
              HttpClient client = new DefaultHttpClient();
              HttpPost post = new HttpPost(urlString);
              HttpContext localContext = new BasicHttpContext();
              FileBody bin1 = new FileBody(file1);
              MultipartEntity reqEntity = new MultipartEntity();
              reqEntity.addPart("uploadedFile", bin1);
              reqEntity.addPart("id", new StringBody(getId(file1)));
              
              //UPDATE THIS TO ADD OWNER, LOCATION, KEYWORD DATA TO DB
              //reqEntity.addPart("owner", null);
              //reqEntity.addPart("location", null);
              //reqEntity.addPart("keywords", null);
              
              post.setEntity(reqEntity);
              HttpResponse response = client.execute(post, localContext);
              System.out.println("PHP RESPONSE: " + response);
              myResEntity = response.getEntity();
              
              final String response_str = EntityUtils.toString(myResEntity);
              if (myResEntity != null) {
                  Log.i("RESPONSE",response_str);
                  runOnUiThread(new Runnable(){
                         public void run() {
                              try {
                                // ((TextView) myResEntity).setTextColor(Color.GREEN);
                                // res.setText("n Response from server : n " + response_str);
                                 Toast.makeText(getApplicationContext(),"Upload Complete. Check the server uploads directory.", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                             } catch (Exception e) {
                                 e.printStackTrace();
                             }
                            }
                     });
              }
         }
         catch (Exception ex){
              Log.e("Debug", "error: " + ex.getMessage(), ex);
         }
       }
 	
 	public String getId(File photo) {
 		/*
 		 * Create an id for photo to use as id in database.  uses hashcode to create
 		 * unique id for each file
 		*/
 		return Integer.toString(photo.hashCode());
 	}
 }
