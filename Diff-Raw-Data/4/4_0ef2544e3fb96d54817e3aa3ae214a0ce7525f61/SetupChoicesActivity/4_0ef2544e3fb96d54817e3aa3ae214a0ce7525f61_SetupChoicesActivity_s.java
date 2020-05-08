 package com.adtworker.choose4u;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 
 import com.adview.AdViewLayout;
 import com.adview.AdViewTargeting;
 import com.adview.AdViewTargeting.RunMode;
 
 public class SetupChoicesActivity extends Activity {
 
 	final String img_prefix = "/mnt/sdcard/.adtwkr/";
 	final String[] imgstr = {"red.png", "green.png", "blue.png", "yellow.png"};
 	private static final int PHOTO_PICKED_WITH_DATA = 3021;
 	private static final int CAMERA_WITH_DATA = 3023;
 	private static final int PICK_FROM_GALLERY = 3024;
 
 	private int pos = 0;
 	private ImageView[] img = new ImageView[4];
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		final OnClickListener onClickFunc = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(SetupChoicesActivity.this,
 						Choose4uActivity.class);
 				startActivity(intent);
 			}
 		};
 
 		View btn_title = findViewById(R.id.btn_title);
 		View btn_start = findViewById(R.id.btn_start);
 		View btn_reset = findViewById(R.id.btn_reset);
 
 		btn_title.setOnClickListener(onClickFunc);
 		btn_start.setOnClickListener(onClickFunc);
 		btn_reset.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				new AlertDialog.Builder(SetupChoicesActivity.this)
 						.setMessage(getString(R.string.reset_msg))
 						.setIcon(android.R.drawable.ic_dialog_info)
 						.setPositiveButton(getString(R.string.ok),
 								new DialogInterface.OnClickListener() {
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										reset();
 									}
 								})
 						.setNegativeButton(getString(R.string.cancel),
 								new DialogInterface.OnClickListener() {
 									@Override
 									public void onClick(DialogInterface dialog,
 											int whichButton) {
 									}
 								}).create().show();
 			}
 		});
 
 		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
 		int width = displayMetrics.widthPixels;
 
 		LinearLayout choices = (LinearLayout) findViewById(R.id.choices);
 
 		LinearLayout[] lyt = new LinearLayout[2];
 		for (int i = 0; i < 4; i++) {
 			if (i < 2) {
 				lyt[i] = new LinearLayout(this);
 				lyt[i].setOrientation(LinearLayout.HORIZONTAL);
 			} else {
 				i = (i == 2 ? 3 : 2);
 			}
 
 			int margin = 10;
 			final int current = i;
 			img[i] = new ImageView(this);
			LayoutParams params = new LayoutParams(width / 2 - margin, width
					/ 2 - margin);
 			params.setMargins(margin, margin, margin, margin);
 			img[i].setLayoutParams(params);
 			img[i].setScaleType(ScaleType.FIT_CENTER);
 
 			Bitmap bitmap = null;
 			File file = new File(img_prefix + imgstr[i]);
 			if (!file.exists()) {
 				try {
 					bitmap = BitmapFactory.decodeStream(getAssets().open(
 							imgstr[i]));
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			} else {
 				bitmap = BitmapFactory.decodeFile(img_prefix + imgstr[i]);
 			}
 			if (bitmap != null) {
 				img[i].setImageBitmap(bitmap);
 			}
 
 			img[i].setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View arg0) {
 					String[] options = getResources().getStringArray(
 							R.array.options_array);
 					AlertDialog dialog = new AlertDialog.Builder(
 							SetupChoicesActivity.this)
 							// .setIcon(android.R.drawable.ic_dialog_info)
 							.setTitle(getString(R.string.option_setting))
 							.setItems(options, onselect).create();
 					dialog.show();
 
 				}
 
 				DialogInterface.OnClickListener onselect = new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						pos = current;
 						switch (which) {
 							case 0 :
 								onClickCamera();
 								break;
 							case 1 :
 								onClickGallery();
 								break;
 							case 2 :
 								onClickDefault();
 								break;
 						}
 					}
 
 				};
 
 			});
 
 			img[i].setOnLongClickListener(new OnLongClickListener() {
 				@Override
 				public boolean onLongClick(View v) {
 					return true;
 				}
 			});
 
 			lyt[i / 2].addView(img[i]);
 			if (i % 2 != 0)
 				choices.addView(lyt[i / 2]);
 
 			if (i == 2)
 				break;
 			if (i == 3)
 				i = 2;
 		}
 
 		LinearLayout layout = (LinearLayout) findViewById(R.id.adLayout);
 		AdViewTargeting.setRunMode(RunMode.NORMAL);
 		AdViewLayout adViewLayout = new AdViewLayout(this,
 				"SDK2012010501074409ed37r0uwufh04");
 		layout.addView(adViewLayout);
 		layout.invalidate();
 	}
 	protected void onClickCamera() {
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		startActivityForResult(intent, CAMERA_WITH_DATA);
 	}
 
 	protected void onClickGallery() {
 		Intent intent = new Intent();
 		intent.setType("image/*");
 		intent.setAction(Intent.ACTION_GET_CONTENT);
 		startActivityForResult(intent, PICK_FROM_GALLERY);
 	}
 
 	protected void onClickDefault() {
 		reset_pic(pos);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode != RESULT_OK)
 			return;
 
 		switch (requestCode) {
 			case CAMERA_WITH_DATA :
 				final Bitmap photo = data.getParcelableExtra("data");
 				if (photo != null) {
 					doCropPhoto(photo);
 				}
 				break;
 
 			case PICK_FROM_GALLERY :
 				Uri uri = data.getData();
 				try {
 					Cursor cursor = getContentResolver().query(uri, null, null,
 							null, null);
 					cursor.moveToFirst();
 					String imageFilePath = cursor.getString(1);
 					cursor.close();
 
 					BitmapFactory.Options opt = new BitmapFactory.Options();
 					opt.inJustDecodeBounds = true;
 					BitmapFactory.decodeFile(imageFilePath, opt);
 
 					int p = opt.outWidth / 200;
 					opt.inSampleSize = p;
 
 					opt.inJustDecodeBounds = false;
 					final Bitmap photo0 = BitmapFactory.decodeFile(
 							imageFilePath, opt);
 					doCropPhoto(photo0);
 
 					// if (photo0 != null) {
 					// String fileName = img_prefix + imgstr[pos];
 					// File file = new File(fileName);
 					// try {
 					// if (!file.exists())
 					// file.createNewFile();
 					//
 					// FileOutputStream os = new FileOutputStream(file,
 					// false);
 					// photo0.compress(Bitmap.CompressFormat.PNG, 100, os);
 					// os.flush();
 					// os.close();
 					//
 					// Bitmap bitmap = BitmapFactory.decodeFile(img_prefix
 					// + imgstr[pos], opt);
 					// img[pos].setImageBitmap(bitmap);
 					//
 					// } catch (Exception e) {
 					// e.printStackTrace();
 					// }
 					// }
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				break;
 
 			case PHOTO_PICKED_WITH_DATA :
 				final Bitmap photo1 = data.getParcelableExtra("data");
 				if (photo1 != null) {
 					String fileName = img_prefix + imgstr[pos];
 					File file = new File(fileName);
 					try {
 						if (!file.exists())
 							file.createNewFile();
 
 						FileOutputStream os = new FileOutputStream(file, false);
 						photo1.compress(Bitmap.CompressFormat.PNG, 100, os);
 						os.flush();
 						os.close();
 
 						Bitmap bitmap = BitmapFactory.decodeFile(img_prefix
 								+ imgstr[pos]);
 						img[pos].setImageBitmap(bitmap);
 
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				break;
 
 			default :
 
 		}
 	}
 	protected void doCropPhoto(Bitmap data) {
 		Intent intent = getCropImageIntent(data);
 		startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
 	}
 
 	public static Intent getCropImageIntent(Bitmap data) {
 		Intent intent = new Intent("com.android.camera.action.CROP");
 		intent.setType("image/*");
 		intent.putExtra("data", data);
 		intent.putExtra("crop", "true");
 		intent.putExtra("aspectX", 1);
 		intent.putExtra("aspectY", 1);
 		intent.putExtra("outputX", 180);
 		intent.putExtra("outputY", 180);
 		intent.putExtra("return-data", true);
 		return intent;
 	}
 
 	protected void reset() {
 		for (int i = 0; i < 4; i++) {
 			reset_pic(i);
 		}
 	}
 
 	protected void reset_pic(int i) {
 		File file = new File(img_prefix + imgstr[i]);
 		if (file.exists())
 			file.delete();
 
 		Bitmap bitmap = null;
 		try {
 			bitmap = BitmapFactory.decodeStream(getAssets().open(imgstr[i]));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		img[i].setImageBitmap(bitmap);
 	}
 
 	@Override
 	public boolean onKeyUp(int keycode, KeyEvent event) {
 		switch (keycode) {
 
 			case KeyEvent.KEYCODE_BACK :
 
 				new AlertDialog.Builder(this)
 						.setMessage(getString(R.string.exit_msg))
 						.setIcon(android.R.drawable.ic_dialog_info)
 						.setPositiveButton(getString(R.string.ok),
 								new DialogInterface.OnClickListener() {
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										finish();
 									}
 								})
 						.setNegativeButton(getString(R.string.cancel),
 								new DialogInterface.OnClickListener() {
 									@Override
 									public void onClick(DialogInterface dialog,
 											int whichButton) {
 									}
 								}).create().show();
 
 				return false;
 
 			default :
 		}
 
 		return super.onKeyUp(keycode, event);
 	}
 }
