 package tasktracker.view;
 
 import java.io.File;
 import java.sql.Array;
 import java.util.ArrayList;
 
 import tasktracker.model.Preferences;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.Media;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 /**
  * A class that shows all photos currently attached to task. Allows user to
  * choose to attach a pre existing photo or take a new photo.
  * 
  * @author Katherine Jasniewski
  * 
  */
 
 public class PhotoPicker extends Activity {
 
 	Intent intent;
 
 	Uri imageFileUri;
 	private ArrayList<String> imageUrls = new ArrayList<String>();
 	private ImageAdapter myAdapter = new ImageAdapter(this);
 	private GridView gridView;
 	// private DisplayImageOptions options;
 
 	public static final int PICK_PICTURE_FROM_GALLERY = 1;
 	public static final int TAKE_PICTURE = 2;
 	public static final int RETURN_PICTURES = 3;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_photo_picker_view);
 
 		// initializes buttons on layout
 		Button galleryPhoto = (Button) findViewById(R.id.galleryPhoto);
 		Button takePhoto = (Button) findViewById(R.id.takeAPhoto);
 		Button saveChanges = (Button) findViewById(R.id.saveChanges);
 
 		setupToolbarButtons();
 
 		galleryPhoto.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 
 				selectPhoto();
 			}
 		});
 
 		// Take a photo option
 		OnClickListener takephotoListener = new OnClickListener() {
 
 			public void onClick(View v) {
 				takeAPhoto();
 			}
 
 		};
 
 		takePhoto.setOnClickListener(takephotoListener);
 
 		// Save photos option
 		OnClickListener savephotoListener = new OnClickListener() {
 
 			public void onClick(View v) {
 				intent = getIntent();
 				Bundle photoBundle = new Bundle();
 				// Bitmap[] photos = myAdapter.getPhotos();
 				// ArrayList<Bitmap> photos = myAdapter.getPhotoList();
 
 				// TODO for web
 				ArrayList<byte[]> compressed = myAdapter.getCompressedPhotos();
 				Array[] bytes = new Array[compressed.size()];
 				// photoBundle.
 				int numPhotos = compressed.size();
 				intent.putExtra("numPhotos", numPhotos);
 
 				System.out.println("PPsend" + numPhotos);
 				for (int i = 0; numPhotos > i; i++) {
 					byte[] photoCompression = compressed.get(i);
 					// bytes[i] = photoCompression;
 
 					intent.putExtra("photo" + i, photoCompression);
 				}
 
 				// photoBundle.putByteArray("compressed", photoCompression);
 				String[] pathArray = new String[imageUrls.size()];
 				imageUrls.toArray(pathArray);
 				intent.putExtra("PhotoPaths", pathArray);
 				// intent.putParcelableArrayListExtra("Photos", compressed);
 				setResult(RESULT_OK, intent);
 
 				Toast.makeText(PhotoPicker.this, "Photos Saved", 2000).show();
 				finish();
 			}
 
 		};
 
 		saveChanges.setOnClickListener(savephotoListener);
 
 		gridView = (GridView) findViewById(R.id.gridView);
 
 		ArrayList<byte[]> byteArrays = new ArrayList<byte[]>();
 		int numPhotos = getIntent().getIntExtra("numPhotos", 0);
 
 		System.out.println("PPrec" + numPhotos);
 		for (int i = 0; numPhotos > i; i++) {
 			byte[] compressedPhoto = getIntent().getByteArrayExtra("photo" + i);
 			// bytes[i] = photoCompression;
 			byteArrays.add(compressedPhoto);
 		}
 		myAdapter.decompressPhotos(byteArrays);
 		gridView.setAdapter(myAdapter);
 
 	}
 
 	private void setupToolbarButtons() {
 		// Assign Buttons
 		Button buttonMyTasks = (Button) findViewById(R.id.buttonMyTasks);
 		Button buttonCreate = (Button) findViewById(R.id.buttonCreateTask);
 		Button buttonNotifications = (Button) findViewById(R.id.buttonNotifications);
 
 		Context context = getApplicationContext();
 		buttonMyTasks.setOnClickListener(new ActivityNagivator(context,
 				TaskListView.class));
 
 		buttonCreate.setOnClickListener(new ActivityNagivator(context,
 				CreateTaskView.class));
 
 		buttonNotifications.setOnClickListener(new ActivityNagivator(context,
 				NotificationListView.class));
 	}
 
 	// User can select a photo from the android gallery
 	public void selectPhoto() {
 
 		Intent intent = new Intent(Intent.ACTION_PICK,
 				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 
 		startActivityForResult(intent, PICK_PICTURE_FROM_GALLERY);
 
 	}
 
 	// TODO: Should start the camera class.
 	public void takeAPhoto() {
 
 		String folder = Environment.getExternalStorageDirectory()
 				.getAbsolutePath() + "/tmp";
 		File folderF = new File(folder);
 
 		// if file doesn't exist create it
 		if (!folderF.exists()) {
 			folderF.mkdir();
 		}
 
 		// save file with the current time
 		String imageFilePath = folder + "/" + System.currentTimeMillis()
 				+ ".jpg";
 		File imageFile = new File(imageFilePath);
 		imageFileUri = Uri.fromFile(imageFile);
 
 		// refresh
 		sendBroadcast(new Intent(
 				Intent.ACTION_MEDIA_MOUNTED,
 				Uri.parse("file://" + Environment.getExternalStorageDirectory())));
 
 		// intent has information about image
 		Intent intentC = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		intentC.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
 
 		startActivityForResult(intentC, TAKE_PICTURE);
 
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode != RESULT_OK)
 			return;
 
 		if (requestCode == PICK_PICTURE_FROM_GALLERY) {
 
 			Cursor cursor = getContentResolver().query(data.getData(),
 					new String[] { Media.DATA }, null, null, null);
 
 			if (!cursor.moveToFirst())
 				return;
 
 			String filePath = cursor.getString(cursor
 					.getColumnIndex(Media.DATA));
 			cursor.close();
 
 			this.fillData(filePath);
 
 		} else if (requestCode == TAKE_PICTURE) {
 			String path = imageFileUri.toString();
 			this.fillData(path.replace("file://", ""));
 		}
 
 	}
 
 	private void fillData(String filePath) {
 
 		this.imageUrls.add(filePath);
 		this.myAdapter.addPhoto(BitmapFactory.decodeFile(filePath));
 		this.gridView.setAdapter(myAdapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.account_menu, menu);
 
 		MenuItem account = menu.findItem(R.id.Account_menu);
 		account.setTitle(Preferences.getUsername(this));
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.logout:
 
 			Intent intent = new Intent(getApplicationContext(), Login.class);
 			finish();
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
