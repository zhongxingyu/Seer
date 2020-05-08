 package com.ece.smartGallery.activity;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.UUID;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.media.ExifInterface;
 import android.media.MediaRecorder;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.ece.smartGallery.R;
 import com.ece.smartGallery.db.DatabaseHandler;
 import com.ece.smartGallery.entity.Album;
 import com.ece.smartGallery.entity.Photo;
 import com.ece.smartGallery.util.LocationService;
 
 public class EditActivity extends Activity {
 
 	private static final String LOG_TAG = "AudioRecordTest";
 	private String voiceCommentFileName = null;
 
 	private Button mRecordButton = null;
 	private MediaRecorder mRecorder = null;
 	private boolean mStartRecording = true;
 	private int albumid = -1;
 	private Photo photo = null;
 	private Intent intent;
 	private final String TAG = this.getClass().getName();
 	private final int CAMERA_REQ = 1337;
 	private Uri cameraUri;
 	private ImageView imageView;
 	private boolean alreadyCapture = false;
 	private DatabaseHandler db;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit);
 		imageView = (ImageView) findViewById(R.id.edit_image);
 		db = new DatabaseHandler(this);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		intent = getIntent();
 		// from home, add a new picture
 		if (Intent.ACTION_INSERT.equals(intent.getAction())
 				&& alreadyCapture == false) {
 			albumid = (int) intent.getIntExtra(Album.ALBUM, -1);
 			Log.d(TAG, "launch camera");
 			// start camera and then come back to add comments
 			startCamera();
 		}
 		// from display or scratch, edit an existing picture
 		else if (Intent.ACTION_EDIT.equals(intent.getAction())) {
 			photo = (Photo) intent.getSerializableExtra(Photo.PHOTO);
 			albumid = photo.getAlbumId();
 			LoadAsyncTask task = new LoadAsyncTask(imageView, photo, this);
 			task.execute();
 
 			// there exists voice comment
 			if (photo.getVoice() != null && !photo.getVoice().isEmpty()) {
 				voiceCommentFileName = photo.getVoice();
 			}
 		} else if (Intent.ACTION_SEND.equals(intent.getAction())) {
 			// send from share
 			byte[] imageBytes = intent.getByteArrayExtra(Photo.IMAGE);
 			Bitmap b = BitmapFactory.decodeByteArray(imageBytes, 0,
 					imageBytes.length);
 			this.setImage(imageView, b);
 			photo = new Photo();
 			photo.setAlbumId(1); // default
 			String imageName = UUID.randomUUID().toString() + ".jpg";
 			SavePhotoTask task = new SavePhotoTask(this, imageBytes, imageName);
 			task.execute();
 			photo.setImage(Uri.fromFile(new File(this
 					.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
 					imageName)));
 			photo.setLocation("Pittsburgh");
 			photo.setTimeStamp(System.currentTimeMillis());
 		}
 		// voice comment button
 		mRecordButton = (Button) findViewById(R.id.add_voice_button);
 		mRecordButton.setText("Start recording");
 		mRecordButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				onRecord(mStartRecording, voiceCommentFileName);
 				if (mStartRecording) {
 					mRecordButton.setText("Stop recording");
 				} else {
 					mRecordButton.setText("Start recording");
 				}
 				mStartRecording = !mStartRecording;
 			}
 		});
 	}
 
 	private void startCamera() {
 		this.alreadyCapture = true;
 		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		File path = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
 		String imageName = UUID.randomUUID().toString() + ".jpg";
 		File file = new File(path, imageName);
 		if (file.exists()) {
 			file.delete();
 		}
 		cameraUri = Uri.fromFile(file);
 		Log.d(TAG, "Request uri = " + cameraUri);
 		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
 		startActivityForResult(cameraIntent, this.CAMERA_REQ);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == this.CAMERA_REQ) {
 			if (resultCode == RESULT_OK) {
 				LoadAsyncTask task = new LoadAsyncTask(imageView, cameraUri,
 						this);
 				task.execute();
 				Log.d(TAG, "RESULT_OK returned from camera");
 				Log.d(TAG, "URI = " + cameraUri.toString());
 				photo = new Photo();
 				photo.setImage(cameraUri);
 				photo.setLocation("Pittsburgh");
 				photo.setTimeStamp(System.currentTimeMillis());
 				Log.d(TAG, "album id = " + albumid);
 				photo.setAlbumId(albumid);
 				
 				LocationService service = new LocationService(EditActivity.this);
 				if (service.canGetLocation()) {
 					photo.setLocation(service.getLocationName());
 				}
 				update(service.getLocationName());
 				
 			} else {
 				finish();
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.edit, menu);
 		return true;
 	}
	
	public void cancel(View view){
		
		finish();
	}
 
 	public void save(View view) {
 		Intent displayIntent = new Intent(this, DisplayActivity.class);
 
 		// retrieve user input
 		String input_text_comment = ((EditText) findViewById(R.id.edit_comment_input))
 				.getText().toString();
 		
 		if (input_text_comment != null && !input_text_comment.isEmpty())
 			photo.setText(input_text_comment);
 		if (voiceCommentFileName != null && !voiceCommentFileName.isEmpty())
 			photo.setVoice(voiceCommentFileName);
 		Album album;
 		if (albumid != -1) {
 			album = db.getAlbum(albumid);
 		} else {
 			album = db.getAlbum(1);
 		}
 
 		boolean success = false;
 		if (photo.getId() > 0) {
 			success = db.updatePhoto(album, photo);
 		} else {
 			success = db.addPhoto(album, photo);
 		}
 
 		if (success) {
 			displayIntent.putExtra(Photo.PHOTO, photo);
 			startActivity(displayIntent);
 			finish();
 		} else {
 			Toast.makeText(this, "fail to save photo", Toast.LENGTH_SHORT)
 					.show();
 		}
 
 	}
 
 	private void onRecord(boolean start, String voiceCommentFileName) {
 		if (start) {
 			if (voiceCommentFileName == null || voiceCommentFileName.isEmpty()) {
 				voiceCommentFileName = GetVoiceCommentPath();
 				this.photo.setVoice(voiceCommentFileName);
 			}
 			startRecording(voiceCommentFileName);
 		} else {
 			stopRecording();
 		}
 	}
 
 	private void startRecording(String voiceCommentFileName) {
 		mRecorder = new MediaRecorder();
 		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
 		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 		mRecorder.setOutputFile(voiceCommentFileName);
 		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
 		try {
 			mRecorder.prepare();
 		} catch (IOException e) {
 			Log.e(LOG_TAG, "prepare() failed");
 		}
 
 		mRecorder.start();
 	}
 
 	private void stopRecording() {
 		mRecorder.stop();
 		mRecorder.release();
 		mRecorder = null;
 	}
 
 	public String GetVoiceCommentPath() {
 		String path = Environment.getExternalStorageDirectory()
 				.getAbsolutePath();
 
 		path = path + "/" + UUID.randomUUID().toString() + ".3gp";
 		return path;
 	}
 
 	public void set_scratch(Uri uri) {
 		this.photo.setScratchURI(uri);
 	}
 
 	public void scratch(View view) {
 		Intent intent = new Intent(this, ScratchActivity.class);
 		intent.putExtra(Photo.PHOTO, photo);
 		startActivity(intent);
 		finish();
 	}
 
 	private Bitmap decodeFile(File f) {
 		try {
 			// Decode image size
 			BitmapFactory.Options o = new BitmapFactory.Options();
 			o.inJustDecodeBounds = true;
 			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
 
 			// The new size we want to scale to
 			final int REQUIRED_SIZE = 600;
 
 			// Find the correct scale value. It should be the power of 2.
 			int scale = 1;
 			while (o.outWidth / scale / 2 >= REQUIRED_SIZE
 					&& o.outHeight / scale / 2 >= REQUIRED_SIZE)
 				scale *= 2;
 
 			// Decode with inSampleSize
 			BitmapFactory.Options o2 = new BitmapFactory.Options();
 			o2.inSampleSize = scale;
 			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
 		} catch (FileNotFoundException e) {
 		}
 		return null;
 	}
 
 	private int getCameraPhotoOrientation(Context context, Uri imageUri,
 			String imagePath) {
 		int rotate = 0;
 		try {
 			context.getContentResolver().notifyChange(imageUri, null);
 			File imageFile = new File(imagePath);
 			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
 			int orientation = exif.getAttributeInt(
 					ExifInterface.TAG_ORIENTATION,
 					ExifInterface.ORIENTATION_NORMAL);
 
 			switch (orientation) {
 			case ExifInterface.ORIENTATION_ROTATE_270:
 				rotate = 270;
 				break;
 			case ExifInterface.ORIENTATION_ROTATE_180:
 				rotate = 180;
 				break;
 			case ExifInterface.ORIENTATION_ROTATE_90:
 				rotate = 90;
 				break;
 			}
 
 			Log.v(LOG_TAG, "Exif orientation: " + orientation);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return rotate;
 	}
 
 	private void setImage(ImageView view, Bitmap b) {
 		view.setImageBitmap(b);
 	}
 
 	class LoadAsyncTask extends AsyncTask<Void, Void, Void> {
 
 		private Uri uri;
 		private ImageView view;
 		private Bitmap bitmap;
 		private Context context;
 
 		LoadAsyncTask(ImageView view, Uri uri, Context context) {
 			this.uri = uri;
 			this.view = view;
 			this.context = context;
 		}
 
 		LoadAsyncTask(ImageView view, Photo photo, Context context) {
 			this.uri = photo.getImage();
 			this.view = view;
 			this.context = context;
 		}
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			File file = new File(uri.getPath());
 			Bitmap b = decodeFile(file);
 			Matrix mat = new Matrix();
 			mat.postRotate(getCameraPhotoOrientation(context, uri,
 					file.getAbsolutePath()));
 			bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(),
 					mat, true);
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void arg0) {
 			setImage(view, bitmap);
 			Log.d(LOG_TAG, "onPostExecute");
 		}
 
 	}
 
 	public void update(String location) {
 		String text = "Picture taken in: "+location ;
 		TextView tv = (TextView) findViewById(R.id.display_geolocation);
 		tv.setText(text);
 	}
 
 	class SavePhotoTask extends AsyncTask<Void, Void, Void> {
 		private byte[] bytes;
 		private String fileName;
 		private Context context;
 
 		SavePhotoTask(Context context, byte[] bytes, String fileName) {
 			this.context = context;
 			this.bytes = bytes;
 			this.fileName = fileName;
 		}
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			File photo = new File(
 					context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
 					fileName);
 
 			if (photo.exists()) {
 				photo.delete();
 			}
 
 			try {
 				FileOutputStream fos = new FileOutputStream(photo.getPath());
 
 				fos.write(bytes);
 				fos.close();
 			} catch (java.io.IOException e) {
 				Log.e("save picture", "Exception in photoCallback", e);
 			}
 
 			return (null);
 		}
 	}
 
 }
