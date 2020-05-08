 package com.slobodastudio.discussions.ui.fragments;
 
 import com.slobodastudio.discussions.ApplicationConstants;
 import com.slobodastudio.discussions.R;
 import com.slobodastudio.discussions.data.model.Attachment;
 import com.slobodastudio.discussions.data.model.SelectedPoint;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
 import com.slobodastudio.discussions.ui.ExtraKey;
 import com.slobodastudio.discussions.ui.activities.BaseActivity;
 import com.slobodastudio.discussions.ui.activities.YoutubeActivity;
 import com.slobodastudio.discussions.ui.view.MediaList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.BaseColumns;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.ImageColumns;
 import android.provider.MediaStore.Images.Media;
 import android.provider.MediaStore.MediaColumns;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 public class PointMediaTabFragment extends SherlockListFragment {
 
 	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
 	private static final int PICK_CAMERA_PHOTO = 0x03;
 	private static final int PICK_IMAGE_REQUEST = 0x02;
 	private static final int PICK_PDF_REQUEST = 0x04;
 	private static final int PICK_YOUTUBE_REQUEST = 0x05;
 	private static final String TAG = PointMediaTabFragment.class.getSimpleName();
 	private boolean footerButtonsEnabled;
 	private final AttachmentsCursorLoader mAttachmentsCursorLoader;
 	private MediaList mediaList;
 	private TextView mPointNameTextView;
 	private SelectedPoint mSelectedPoint;
 	private NewAttachment newAttachment;
 	private Uri tempCameraFileUri;
 
 	public PointMediaTabFragment() {
 
 		mAttachmentsCursorLoader = new AttachmentsCursorLoader();
 	}
 
 	/** Converts an intent into a {@link Bundle} suitable for use as fragment arguments. */
 	public static Bundle intentToFragmentArguments(final Intent intent) {
 
 		Bundle arguments = new Bundle();
 		if (intent == null) {
 			return arguments;
 		}
 		if (!intent.hasExtra(ExtraKey.DISCUSSION_ID)) {
 			throw new IllegalStateException("intent was without discussion id");
 		}
 		if (!intent.hasExtra(ExtraKey.POINT_ID)) {
 			throw new IllegalStateException("intent was without point id");
 		}
 		if (!intent.hasExtra(ExtraKey.PERSON_ID)) {
 			throw new IllegalStateException("intent was without person id");
 		}
 		if (!intent.hasExtra(ExtraKey.TOPIC_ID)) {
 			throw new IllegalStateException("intent was without topic id");
 		}
 		if (intent.getAction() == null) {
 			throw new IllegalStateException("intent was without action string");
 		}
 		int discussionId = intent.getIntExtra(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
 		int personId = intent.getIntExtra(ExtraKey.PERSON_ID, Integer.MIN_VALUE);
 		int topicId = intent.getIntExtra(ExtraKey.TOPIC_ID, Integer.MIN_VALUE);
 		int pointId = intent.getIntExtra(ExtraKey.POINT_ID, Integer.MIN_VALUE);
 		SelectedPoint point = new SelectedPoint();
 		point.setDiscussionId(discussionId);
 		point.setPersonId(personId);
 		point.setTopicId(topicId);
 		point.setPointId(pointId);
 		arguments.putParcelable(ExtraKey.SELECTED_POINT, point);
 		boolean viewEnabled = Intent.ACTION_EDIT.equals(intent.getAction());
 		arguments.putBoolean(ExtraKey.VIEW_ENABLED, viewEnabled);
 		return arguments;
 	}
 
 	public static void requestYoutubeAttachment(final Activity activity) {
 
 		Intent intent = new Intent(activity, YoutubeActivity.class);
 		activity.startActivityForResult(intent, PICK_YOUTUBE_REQUEST);
 	}
 
 	private static AdapterContextMenuInfo castAdapterContextMenuInfo(final ContextMenuInfo contextMenuInfo) {
 
 		try {
 			// Casts the incoming data object into the type for AdapterView objects.
 			return (AdapterContextMenuInfo) contextMenuInfo;
 		} catch (ClassCastException e) {
 			// If the menu object can't be cast, logs an error.
 			throw new RuntimeException("bad menuInfo: " + contextMenuInfo, e);
 		}
 	}
 
 	private static boolean isSdCardMounted() {
 
 		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
 	}
 
 	@Override
 	public void onActivityCreated(final Bundle savedInstanceState) {
 
 		super.onActivityCreated(savedInstanceState);
 		registerForContextMenu(getListView());
 		setListShown(false);
 		initFromArguments();
 		addAttachmentsHeader();
 		if (footerButtonsEnabled) {
 			addAttachmentsFooter();
 		}
 		mediaList = new MediaList(getActivity(), getListView());
 		mediaList.setPositionOffset(1);
 		initAttachmentsLoader();
 	}
 
 	@Override
 	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
 
 		Log.d(TAG, "[onActivityresult] requestCode: " + requestCode);
 		if (Activity.RESULT_OK == resultCode) {
 			switch (requestCode) {
 				case PICK_CAMERA_PHOTO:
 					handleCameraResult(data);
 					break;
 				case PICK_IMAGE_REQUEST:
 					if ((data.getData() != null)) {
 						newAttachment = new NewAttachment(PICK_IMAGE_REQUEST, data.getData());
 					} else {
 						newAttachment = null;
 					}
 					break;
 				case PICK_PDF_REQUEST:
 					if ((data.getData() != null)) {
 						newAttachment = new NewAttachment(PICK_PDF_REQUEST, data.getData());
 					} else {
 						newAttachment = null;
 					}
 					break;
 				case PICK_YOUTUBE_REQUEST:
 					if ((data.getData() != null)) {
 						newAttachment = new NewAttachment(PICK_YOUTUBE_REQUEST, data.getData());
 					} else {
 						newAttachment = null;
 					}
 					break;
 				default:
 					break;
 			}
 		}
 		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
 	}
 
 	@Override
 	public boolean onContextItemSelected(final MenuItem item) {
 
 		switch (item.getItemId()) {
 			case R.id.menu_delete:
 				onActionDeleteAttachment(item);
 				return true;
 			default:
 				return super.onContextItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
 
 		super.onCreateContextMenu(menu, v, menuInfo);
 		AdapterContextMenuInfo info = castAdapterContextMenuInfo(menuInfo);
 		Cursor cursor = (Cursor) mediaList.getAdapter().getItem(info.position - 1);
 		int textIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.TITLE);
 		menu.setHeaderTitle(cursor.getString(textIndex));
 		android.view.MenuInflater inflater = getActivity().getMenuInflater();
 		inflater.inflate(R.menu.context_attachments, menu);
 	}
 
 	public void onServiceConnected() {
 
 		if (newAttachment != null) {
 			switch (newAttachment.type) {
 				case PICK_CAMERA_PHOTO:
 					onAttachSourceAdded(newAttachment.uri, Attachments.AttachmentType.JPG);
 					break;
 				case PICK_IMAGE_REQUEST:
 					onAttachSourceAdded(newAttachment.uri, Attachments.AttachmentType.JPG);
 					break;
 				case PICK_PDF_REQUEST:
 					onAttachSourceAdded(newAttachment.uri, Attachments.AttachmentType.PDF);
 					break;
 				case PICK_YOUTUBE_REQUEST:
 					onAttachSourceAdded(newAttachment.uri, Attachments.AttachmentType.YOUTUBE);
 					break;
 				default:
 					break;
 			}
 			newAttachment = null;
 		}
 	}
 
 	private void addAttachmentsFooter() {
 
 		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
 				Context.LAYOUT_INFLATER_SERVICE);
 		View footerView = layoutInflater.inflate(R.layout.layout_media_footer, null, false);
 		setSelectAttachClickListener(footerView);
 		getListView().addFooterView(footerView);
 	}
 
 	private void addAttachmentsHeader() {
 
 		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
 				Context.LAYOUT_INFLATER_SERVICE);
 		View headerView = layoutInflater.inflate(R.layout.list_header_point_name, null, false);
 		mPointNameTextView = (TextView) headerView.findViewById(R.id.list_header_point_name);
 		getListView().addHeaderView(headerView);
 	}
 
 	private void handleCameraResult(final Intent data) {
 
 		if (data == null) {
 			newAttachment = new NewAttachment(PICK_CAMERA_PHOTO, tempCameraFileUri);
 		} else if (data.getData() != null) {
 			newAttachment = new NewAttachment(PICK_CAMERA_PHOTO, data.getData());
 		} else {
 			newAttachment = null;
 		}
 	}
 
 	private void initAttachmentsLoader() {
 
 		Bundle args = new Bundle();
 		args.putInt(ExtraKey.POINT_ID, mSelectedPoint.getPointId());
 		getLoaderManager().initLoader(AttachmentsCursorLoader.ATTACHMENTS_ID, args, mAttachmentsCursorLoader);
 		getLoaderManager().initLoader(AttachmentsCursorLoader.POINT_NAME_ID, args, mAttachmentsCursorLoader);
 	}
 
 	private void initFromArguments() {
 
 		Bundle arguments = getArguments();
 		if (arguments == null) {
 			throw new NullPointerException("You are trying to instantiate fragment without arguments");
 		}
 		if (!arguments.containsKey(ExtraKey.SELECTED_POINT)) {
 			throw new IllegalStateException("fragment was called without selected point extra");
 		}
 		if (!arguments.containsKey(ExtraKey.VIEW_ENABLED)) {
 			throw new IllegalStateException("fragment was called without view enabled extra");
 		}
 		mSelectedPoint = arguments.getParcelable(ExtraKey.SELECTED_POINT);
 		footerButtonsEnabled = arguments.getBoolean(ExtraKey.VIEW_ENABLED);
 	}
 
 	private boolean isIntentAvailable(final Intent intent) {
 
 		final PackageManager packageManager = getActivity().getPackageManager();
 		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
 				PackageManager.MATCH_DEFAULT_ONLY);
 		return list.size() > 0;
 	}
 
 	private void onActionDeleteAttachment(final MenuItem item) {
 
 		AdapterContextMenuInfo info = castAdapterContextMenuInfo(item.getMenuInfo());
 		Cursor cursor = (Cursor) mediaList.getAdapter().getItem(info.position - 1);
 		int columnIndex = cursor.getColumnIndexOrThrow(Comments.Columns.ID);
 		int attachmentId = cursor.getInt(columnIndex);
 		((BaseActivity) getActivity()).getServiceHelper().deleteAttachment(attachmentId, mSelectedPoint);
 	}
 
 	private void onAttachSourceAdded(final Uri uri, final int attachmentType) {
 
 		Attachment attachment = new Attachment();
 		attachment.setPersonId(mSelectedPoint.getPersonId());
 		attachment.setPointId(mSelectedPoint.getPointId());
 		attachment.setFormat(attachmentType);
 		((BaseActivity) getActivity()).getServiceHelper().insertAttachment(attachment, mSelectedPoint, uri);
 	}
 
 	private void requestCameraPhoto(final Activity activity) {
 
 		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		if (!isIntentAvailable(cameraIntent)) {
 			showCameraNeedToBeInstalledDialog();
 			return;
 		}
 		if (!isSdCardMounted()) {
 			showSdCardUnmountedDialog();
 			return;
 		}
 		File imageDirectory = Environment.getExternalStorageDirectory();
 		String path = imageDirectory.toString().toLowerCase();
 		String name = imageDirectory.getName().toLowerCase();
 		ContentValues values = new ContentValues();
 		values.put(MediaColumns.TITLE, "Camera " + new SimpleDateFormat().format(new Date()));
 		values.put(ImageColumns.BUCKET_ID, path.hashCode());
 		values.put(ImageColumns.BUCKET_DISPLAY_NAME, name);
 		values.put(MediaColumns.MIME_TYPE, "image/jpeg");
 		values.put(ImageColumns.DESCRIPTION, "Image capture by camera");
 		String filePathString = new File(imageDirectory, "test.jpg").getAbsolutePath();
 		values.put("_data", filePathString);
 		tempCameraFileUri = activity.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
 		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraFileUri);
 		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
 		activity.startActivityForResult(cameraIntent, PICK_CAMERA_PHOTO);
 	}
 
 	private void requestImageAttachment(final Activity activity) {
 
 		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 		intent.setType("image/jpeg");
 		if (isIntentAvailable(intent)) {
 			if (isSdCardMounted()) {
 				activity.startActivityForResult(intent, PICK_IMAGE_REQUEST);
 			} else {
 				showSdCardUnmountedDialog();
 			}
 		} else {}
 	}
 
 	private void requestPdfAttachment(final Activity activity) {
 
 		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 		intent.setType("application/pdf");
 		if (isIntentAvailable(intent)) {
 			activity.startActivityForResult(intent, PICK_PDF_REQUEST);
 		} else {
 			showFileExplorerNeedToBeInstalledDialog();
 		}
 	}
 
 	private void setSelectAttachClickListener(final View container) {
 
 		Button attachPdfButton = (Button) container.findViewById(R.id.btn_select_attach);
 		attachPdfButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(final View v) {
 
 				showAddAttachmentSelection();
 			}
 		});
 	}
 
 	private void showAddAttachmentSelection() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setTitle(R.string.dialog_title_attach);
 		builder.setItems(R.array.add_attachemt_types, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(final DialogInterface dialog, final int item) {
 
 				switch (item) {
 					case 0:
 						requestImageAttachment(getActivity());
 						break;
 					case 1:
 						requestCameraPhoto(getActivity());
 						break;
 					case 2:
 						requestPdfAttachment(getActivity());
 						break;
 					case 3:
 						requestYoutubeAttachment(getActivity());
 						break;
 					default:
 						break;
 				}
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	private void showCameraNeedToBeInstalledDialog() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setMessage(R.string.dialog_text_camera_need_install_first).setCancelable(true)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(final DialogInterface dialog, final int id) {
 
 						dialog.cancel();
 					}
 				});
 		builder.create().show();
 	}
 
 	private void showFileExplorerNeedToBeInstalledDialog() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setMessage(R.string.dialog_text_file_explorer_need_install_first).setCancelable(true)
 				.setPositiveButton(R.string.button_title_go_to_market, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(final DialogInterface dialog, final int id) {
 
 						String marketUrl = "market://search?q=file+explorer&c=apps";
 						Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse(marketUrl));
 						market.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 						getActivity().startActivity(market);
 					}
 				}).setNegativeButton(R.string.button_title_cancel, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(final DialogInterface dialog, final int id) {
 
 						dialog.cancel();
 					}
 				});
 		builder.create().show();
 	}
 
 	private void showSdCardUnmountedDialog() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setMessage(R.string.dialog_text_sdcard_unmounted).setCancelable(true).setPositiveButton(
				android.R.string.ok, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(final DialogInterface dialog, final int id) {
 
 						dialog.cancel();
 					}
 				});
 		builder.create().show();
 	}
 
 	private class AttachmentsCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {
 
 		private static final int ATTACHMENTS_ID = 0x00;
 		private static final int POINT_NAME_ID = 0x01;
 
 		@Override
 		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {
 
 			if (!arguments.containsKey(ExtraKey.POINT_ID)) {
 				throw new IllegalArgumentException("Loader was called without point id");
 			}
 			int myPointId = arguments.getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
 			if (DEBUG) {
 				Log.d(TAG, "[onCreateLoader] point id: " + myPointId);
 			}
 			switch (loaderId) {
 				case ATTACHMENTS_ID: {
 					String where = Attachments.Columns.POINT_ID + "=?";
 					String[] args = new String[] { String.valueOf(myPointId) };
 					return new CursorLoader(getActivity(), Attachments.CONTENT_URI, null, where, args, null);
 				}
 				case POINT_NAME_ID: {
 					String where = Points.Columns.ID + "=?";
 					String[] args = new String[] { String.valueOf(myPointId) };
 					String[] projection = new String[] { BaseColumns._ID, Points.Columns.NAME };
 					return new CursorLoader(getActivity(), Points.CONTENT_URI, projection, where, args, null);
 				}
 				default:
 					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
 			}
 		}
 
 		@Override
 		public void onLoaderReset(final Loader<Cursor> loader) {
 
 			switch (loader.getId()) {
 				case ATTACHMENTS_ID:
 					mediaList.getAdapter().swapCursor(null);
 					break;
 				case POINT_NAME_ID:
 					mPointNameTextView.setText("");
 					break;
 				default:
 					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
 			}
 		}
 
 		@Override
 		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
 
 			if (DEBUG) {
 				Log.d(TAG, "[onLoadFinished] cursor count: " + data.getCount() + ", id: " + loader.getId());
 			}
 			switch (loader.getId()) {
 				case ATTACHMENTS_ID:
 					mediaList.getAdapter().swapCursor(data);
 					if (isResumed()) {
 						setListShown(true);
 					} else {
 						setListShownNoAnimation(true);
 					}
 					break;
 				case POINT_NAME_ID:
 					if (data.moveToFirst()) {
 						int nameColumnIndex = data.getColumnIndexOrThrow(Points.Columns.NAME);
 						String name = data.getString(nameColumnIndex);
 						mPointNameTextView.setText(name);
 					}
 					break;
 				default:
 					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
 			}
 		}
 	}
 
 	private class NewAttachment {
 
 		int type;
 		Uri uri;
 
 		public NewAttachment(final int type, final Uri uri) {
 
 			super();
 			this.type = type;
 			this.uri = uri;
 		}
 	}
 }
