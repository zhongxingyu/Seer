 package choy.yoon.chul;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import choy.yoon.chul.State.EditState;
 import choy.yoon.chul.State.PaintStateManager;
 import choy.yoon.chul.State.StateType;
 
 public class MainActivity extends Activity implements
 		ColorPickerDialog.OnColorChangedListener {
 	private int selectedColor;
 	private PaintView paintView;
 	
 	private int progress;
 	
 	TextView indicator;
 	SeekBar seekbar;
 
 	private static final int ACTIVITY_SELECT_IMAGE = 1000;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		PaintStateManager.GetInstance().SetContext(this);
 		paintView = new PaintView(this);
 		setContentView(paintView);
 		
 		progress = 0;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.actions, menu);
 
 		return true;
 	}
 
 	public void onDraw(MenuItem item) {
 		PaintStateManager.GetInstance().OnDraw(item);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		super.onTouchEvent(event);
 		PaintStateManager.GetInstance().OnTouch(event);
 		return true;
 	}
 
 	public void onEdit(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_edit_stroke:
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			final View v = inflator.inflate(R.layout.dialog_stroke, null);
 			
 			indicator = (TextView) v.findViewById(R.id.dialog_stroke_indicator);
 			seekbar = (SeekBar) v.findViewById(R.id.dialog_stroke_seekbar);
 			
 			seekbar.setProgress(progress);
 			
 			builder.setView(v)
 					.setTitle("Set Stroke Width");
 			builder.setPositiveButton("Set",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							progress = seekbar.getProgress();
 							
							strokeChanged((int)((400.0 / 9702.0) * java.lang.Math.pow(progress, 2.0) + (9302.0 / 9702.0) * progress + 1));
 						}
 					});
 			builder.setNegativeButton("Cancel", null);
 			AlertDialog dialog = builder.create();
 
 			dialog.show();
 			
 			seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 				@Override
 				public void onStopTrackingTouch(SeekBar seekBar) {
 				}
 				
 				@Override
 				public void onStartTrackingTouch(SeekBar seekBar) {
 				}
 				
 				@Override
 				public void onProgressChanged(SeekBar seekBar, int progress,
 						boolean fromUser) {
					indicator.setText(Integer.toString((int)((400.0 / 9702.0) * java.lang.Math.pow(progress, 2.0) + (9302.0 / 9702.0) * progress + 1)));
 				}
 			});
 		default:
 			PaintStateManager.GetInstance().OnEdit(item);
 		}
 	}
 
 	public void onFill(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_fill_color:
 			new ColorPickerDialog(this, this, selectedColor).show();
 			break;
 		case R.id.action_fill_texture:
 			Intent intent = new Intent(
 					Intent.ACTION_PICK,
 					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 			startActivityForResult(intent, ACTIVITY_SELECT_IMAGE);
 
 			break;
 		}
 	}
 
 	//ColorPickerDialog에서 색을 선택하면 이 함수로 반환이 된다 (콜백)
 	@Override
 	public void colorChanged(int color) {
 		selectedColor = color;
 		EditState state = (EditState) PaintStateManager.GetInstance().GetState(
 				StateType.kStateEdit);
 		if (state.GetShape() != null) {
 			state.GetShape().SetColor(GLESHelper.GetARGB(color));
 		}
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (requestCode) {
 		//텍스쳐를 갤러리로부터 선택한다.
 		case ACTIVITY_SELECT_IMAGE:
 			if (resultCode == RESULT_OK) {
 				try {
 					Uri selectedImage = data.getData();
 					String[] filePathColumn = { MediaStore.Images.Media.DATA };
 
 					Cursor cursor = getContentResolver().query(selectedImage,
 							filePathColumn, null, null, null);
 					cursor.moveToFirst();
 
 					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
 					String filePath = cursor.getString(columnIndex);
 					cursor.close();
 
 					textureChanged(BitmapFactory.decodeFile(filePath));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 			break;
 		}
 	}
 	
 	//stroke가 선택되면 이 함수로 반환되고, 도형의 stroke를 변경한다.
 	public void strokeChanged(int stroke) {
 		EditState state = (EditState) PaintStateManager.GetInstance().GetState(
 				StateType.kStateEdit);
 		if(state.GetShape() != null) {
 			state.GetShape().SetStroke(stroke);
 		}
 	}
 
 	//텍스쳐가 선택되면 이 함수로 반환되고, 도형에 텍스쳐를 입힌다.
 	public void textureChanged(Bitmap texture) {
 		EditState state = (EditState) PaintStateManager.GetInstance().GetState(
 				StateType.kStateEdit);
 		if (state.GetShape() != null) {
 			state.GetShape().SetTexture(texture);
 		}
 	}
 }
