 /*
  * Copyright 2011 qorron
  * Contact: https://github.com/qorron
  * 
  * This file is part of 666up!
  * 
  * 666up! is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * 666up! is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with 666up!.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package org.qless.up666;
 
 import java.io.File;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.text.ClipboardManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 /**
  * @author quattro Main activity, displays a list of previously uploaded images
  */
 public class UploadsListActivity extends ListActivity {
 	public static final int MENU_CAMERA_ID = Menu.FIRST;
 	public static final int MENU_GALLERY_ID = Menu.FIRST + 7;
 	public static final int MENU_PREFERENCES_ID = Menu.FIRST + 1;
 	public static final int MENU_ABOUT_ID = Menu.FIRST + 2;
 	public static final int MENU_EDIT_ID = Menu.FIRST + 3;
 	public static final int MENU_SEE_ORIGINAL_ID = Menu.FIRST + 4;
 	public static final int MENU_DELETE_ID = Menu.FIRST + 5;
 	public static final int MENU_SHARE_ID = Menu.FIRST + 6;
 
 	private static final int ACTIVITY_CAPTURE_IMAGE = 0;
 	private static final int ACTIVITY_PICK_IMAGE = 2;
 	private static final int ACTIVITY_EDIT = 1;
 
 	private static final int SOURCE_CAMERA = 0;
 	private static final int SOURCE_GALLERY = 1;
 
 	private Uri imageUri;
 
 	private UploadsDbAdapter mDbHelper;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (savedInstanceState != null) {
 			imageUri = savedInstanceState.getParcelable("uri");
 		}
 
 		setContentView(R.layout.uploads_list);
		mDbHelper = new UploadsDbAdapter(this);
		mDbHelper.open();
 		fillData();
 		registerForContextMenu(getListView());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 
 		menu.add(0, MENU_CAMERA_ID, 1, R.string.menu_camera).setIcon(
 				android.R.drawable.ic_menu_camera);
 		menu.add(0, MENU_GALLERY_ID, 2, R.string.menu_gallery).setIcon(
 				android.R.drawable.ic_menu_gallery);
 		menu.add(0, MENU_ABOUT_ID, 3, R.string.menu_about).setIcon(
 				android.R.drawable.ic_menu_info_details);
 		// menu.add(0, MENU_PREFERENCES_ID, 3, R.string.menu_preferences).setIcon(
 		// android.R.drawable.ic_menu_preferences);
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_CAMERA_ID:
 			startGetImageIntent(SOURCE_CAMERA);
 			return true;
 		case MENU_GALLERY_ID:
 			startGetImageIntent(SOURCE_GALLERY);
 			return true;
 		case MENU_ABOUT_ID:
 			showAbout();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View,
 	 * int, long)
 	 */
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		String[] urlComment = mDbHelper.fetchUploadUrlAndComment(id);
 		if (urlComment != null) {
 			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 			clipboard.setText(urlComment[0]);
 			Context context = getApplicationContext();
 			CharSequence text = getString(R.string.copyToast);
 			int duration = Toast.LENGTH_SHORT;
 			Toast toast = Toast.makeText(context, text, duration);
 			toast.show();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View,
 	 * android.view.ContextMenu.ContextMenuInfo)
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, MENU_SHARE_ID, 0, R.string.share);
 		menu.add(0, MENU_EDIT_ID, 1, R.string.menu_edit);
 		menu.add(0, MENU_SEE_ORIGINAL_ID, 2, R.string.menu_see_original);
 		menu.add(0, MENU_DELETE_ID, 3, R.string.menu_delete);
 		menu.setHeaderTitle(R.string.contextTitle);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		Intent i;
 		Cursor upload = mDbHelper.fetchUpload(info.id);
 		startManagingCursor(upload);
 
 		switch (item.getItemId()) {
 		case MENU_SHARE_ID:
 			i = new Intent(android.content.Intent.ACTION_SEND);
 			i.setType("text/plain");
 			i.putExtra(
 					Intent.EXTRA_SUBJECT,
 					upload.getString(upload.getColumnIndex(UploadsDbAdapter.KEY_COMMENT))
 							.equals("") ? getString(R.string.share_subject) : upload
 							.getString(upload.getColumnIndex(UploadsDbAdapter.KEY_COMMENT)));
 			i.putExtra(Intent.EXTRA_TEXT,
 					upload.getString(upload.getColumnIndex(UploadsDbAdapter.KEY_URL)));
 			startActivity(Intent.createChooser(i, getString(R.string.share_title)));
 			return true;
 
 		case MENU_SEE_ORIGINAL_ID:
 			i = new Intent(android.content.Intent.ACTION_VIEW);
 			File file = new File(upload.getString(upload
 					.getColumnIndex(UploadsDbAdapter.KEY_FILENAME)));
 			i.setDataAndType(
 					Uri.fromFile(file),
 					upload.getString(upload.getColumnIndex(UploadsDbAdapter.KEY_MIMETYPE)) != null ? upload
 							.getString(upload.getColumnIndex(UploadsDbAdapter.KEY_MIMETYPE))
 							: "image/*");
 			startActivity(i);
 			return true;
 		case MENU_EDIT_ID:
 			i = new Intent(this, UploadActivity.class);
 			i.putExtra(UploadsDbAdapter.KEY_ROWID, info.id);
 			startActivityForResult(i, ACTIVITY_EDIT);
 			return true;
 		case MENU_DELETE_ID:
 			mDbHelper.deleteUpload(info.id);
 			fillData();
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
 	 */
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putParcelable("uri", imageUri);
 	}
 
 	/**
 	 * reads out all uploads from the database and fills the list
 	 */
 	private void fillData() {
 		// Get all of the uploads from the database and create the item list
 		Cursor c = mDbHelper.fetchAllUploads();
 
 		startManagingCursor(c);
 
 		String[] from = new String[] { UploadsDbAdapter.KEY_COMMENT,
 				UploadsDbAdapter.KEY_UPLOAD_DATE, UploadsDbAdapter.KEY_THUMBNAIL };
 		int[] to = new int[] { R.id.headlineTextView, R.id.additionalTextView,
 				R.id.thumbnailImageView };
 
 		// Now create an array adapter and set it to display using our row
 		SimpleCursorAdapter uploads = new SimpleCursorAdapter(this, R.layout.upload_row, c, from,
 				to);
 
 		uploads.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 				if (columnIndex == cursor.getColumnIndex(UploadsDbAdapter.KEY_THUMBNAIL)) {
 
 					ImageView thumbnailImage = (ImageView) view;
 					byte[] image = cursor.getBlob(cursor
 							.getColumnIndex(UploadsDbAdapter.KEY_THUMBNAIL));
 					if (image != null) {
 						thumbnailImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0,
 								image.length));
 					} else {
 						thumbnailImage.setImageResource(android.R.drawable.ic_menu_gallery);
 					}
 
 					return true;
 				}
 				return false;
 			}
 		});
 
 		setListAdapter(uploads);
 	}
 
 	/**
 	 * launch the camera to take a picture for immediate upload
 	 */
 	private void startGetImageIntent(int source) {
 		switch (source) {
 		case SOURCE_CAMERA: {
 			// define the file-name to save photo taken by Camera activity
 			String fileName = "new-photo-name.jpg";
 			// create parameters for Intent with filename
 			ContentValues values = new ContentValues();
 			values.put(MediaStore.Images.Media.TITLE, fileName);
 			values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
 			// imageUri is the current activity attribute, define and save it for
 			// later usage (also in onSaveInstanceState)
 			imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
 					values);
 			// create new Intent
 			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
 			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
 
 			startActivityForResult(intent, ACTIVITY_CAPTURE_IMAGE);
 			break;
 		}
 		case SOURCE_GALLERY: {
 			Intent intent = new Intent(Intent.ACTION_PICK);
 			intent.setType("image/*");
 			startActivityForResult(intent, ACTIVITY_PICK_IMAGE);
 			break;
 		}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case ACTIVITY_PICK_IMAGE: {
 			imageUri = data.getData();
 			upload();
 			return;
 		}
 		case ACTIVITY_CAPTURE_IMAGE:
 
 			if (resultCode == RESULT_OK) {
 				if (imageUri == null) {
 					Log.d("imageUri", "null!");
 				} else {
 					upload();
 				}
 
 			} else if (resultCode == RESULT_CANCELED) {
 				Toast.makeText(this, getString(R.string.noPhotoToast), Toast.LENGTH_SHORT);
 			} else {
 				Toast.makeText(this, getString(R.string.noPhotoToast), Toast.LENGTH_SHORT);
 			}
 			return;
 		case ACTIVITY_EDIT:
 
 			fillData();
 			return;
 		}
 	}
 
 	/**
 	 * shows a dialog with information about the program
 	 */
 	private void showAbout() {
 		final AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setIcon(android.R.drawable.ic_dialog_info);
 		b.setTitle(R.string.introductionTitle);
 		b.setMessage(R.string.introductionBody);
 		b.setNeutralButton("Ok", null);
 		b.show();
 	}
 
 	/**
 	 * uploads an image specified in the field imageUri
 	 */
 	private void upload() {
 		Bundle b = new Bundle();
 		b.putParcelable(Intent.EXTRA_STREAM, imageUri);
 		Intent i = new Intent(this, UploadActivity.class);
 		i.putExtras(b);
 		i.setAction(Intent.ACTION_SEND);
 		startActivity(i);
 	}
 
 }
