 package fi.mikuz.boarder.gui;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.ConcurrentModificationException;
 import java.util.ListIterator;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.RectF;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.webkit.MimeTypeMap;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.thoughtworks.xstream.XStream;
 
 import fi.mikuz.boarder.R;
 import fi.mikuz.boarder.app.BoarderActivity;
 import fi.mikuz.boarder.component.Slot;
 import fi.mikuz.boarder.component.soundboard.BoardHistory;
 import fi.mikuz.boarder.component.soundboard.GraphicalSound;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboard;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboardHolder;
 import fi.mikuz.boarder.util.AutoArrange;
 import fi.mikuz.boarder.util.FileProcessor;
 import fi.mikuz.boarder.util.IconUtils;
 import fi.mikuz.boarder.util.SoundPlayerControl;
 import fi.mikuz.boarder.util.XStreamUtil;
 import fi.mikuz.boarder.util.dbadapter.BoardsDbAdapter;
 import fi.mikuz.boarder.util.editor.BoardHistoryProvider;
 import fi.mikuz.boarder.util.editor.EditorOrientation;
 import fi.mikuz.boarder.util.editor.GraphicalSoundboardProvider;
 import fi.mikuz.boarder.util.editor.ImageDrawing;
 import fi.mikuz.boarder.util.editor.SoundNameDrawing;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 public class BoardEditor extends BoarderActivity { //TODO destroy god object
 	private String TAG = "GraphicalSoundboardEditor";
 	
 	private static EditorOrientation editorOrientation;
 	
 	public GraphicalSoundboard mGsb;
 	private GraphicalSoundboardProvider mGsbp;
 	private BoardHistory mBoardHistory;
 	private BoardHistoryProvider mBoardHistoryProvider;
 	
 	private static final int LISTEN_BOARD = 0;
 	private static final int EDIT_BOARD = 1;
 	private int mMode = LISTEN_BOARD;
 	
 	private static final int DRAG_TEXT = 0;
 	private static final int DRAG_IMAGE = 1;
 	private int mDragTarget = DRAG_TEXT;
 	
 	private static final int EXPLORE_SOUND = 0;
 	private static final int EXPLORE_BACKGROUD = 1;
 	private static final int EXPLORE_SOUND_IMAGE = 2;
 	private static final int CHANGE_NAME_COLOR = 3;
 	private static final int CHANGE_INNER_PAINT_COLOR = 4;
 	private static final int CHANGE_BORDER_PAINT_COLOR = 5;
 	private static final int CHANGE_BACKGROUND_COLOR = 6;
 	private static final int CHANGE_SOUND_PATH = 7;
 	private static final int EXPLORE_SOUND_ACTIVE_IMAGE = 8;
 	
 	private int mCopyColor = 0;
 	
 	private Paint mSoundImagePaint;
 	private GraphicalSound mDragSound = null;
 	private boolean mDrawDragSound = false;
 	private float mInitialNameFrameX = 0;
 	private float mInitialNameFrameY = 0;
 	private float mInitialImageX = 0;
 	private float mInitialImageY = 0;
 	private float mNameFrameDragDistanceX = -1;
 	private float mNameFrameDragDistanceY = -1;
 	private float mImageDragDistanceX = -1;
 	private float mImageDragDistanceY = -1;
 	private long mClickTime = 0;
 	private long mLastTrackballEvent = 0;
 	private DrawingThread mThread;
 	private Menu mMenu;
 	private DrawingPanel mPanel;
 	boolean mCanvasInvalidated = false;
 	
 	boolean mPanelInitialized = false;
 	AlertDialog mResolutionAlert;
 	
 	private boolean mMoveBackground = false;
 	private float mBackgroundLeftDistance = 0;
 	private float mBackgroundTopDistance = 0;
 	
 	final Handler mHandler = new Handler();
 	ProgressDialog mWaitDialog;
 	boolean mClearBoardDir = false;
 	
 	private File mSbDir = SoundboardMenu.mSbDir;
 	private String mBoardName = null;
 	
 	private AlertDialog mSoundImageDialog;
 	private TextView mSoundImageWidthText;
 	private TextView mSoundImageHeightText;
 	private EditText mSoundImageWidthInput;
 	private EditText mSoundImageHeightInput;
 	
 	private AlertDialog mBackgroundDialog;
 	private TextView mBackgroundWidthText;
 	private TextView mBackgroundHeightText;
 	private EditText mBackgroundWidthInput;
 	private EditText mBackgroundHeightInput;
 	private float mWidthHeightScale;
 	
 	int mNullCanvasCount = 0;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
         
         mBoardHistoryProvider = new BoardHistoryProvider();
         
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			mBoardName = extras.getString(BoardsDbAdapter.KEY_TITLE);
 			setTitle(mBoardName);
 			
 			mGsbp = new GraphicalSoundboardProvider(mBoardName);
 			initEditorBoard();
 			
 			if (mGsb.getSoundList().isEmpty()) {
 				mMode = EDIT_BOARD;
 			}
 		} else {
 			mMode = EDIT_BOARD;
 			
 			mGsb = new GraphicalSoundboard();
 			
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		  	alert.setTitle("Set board name");
 
 		  	final EditText input = new EditText(this);
 		  	alert.setView(input);
 
 		  	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		  		public void onClick(DialogInterface dialog, int whichButton) {
 		  				String inputText = input.getText().toString();
 		  				
 		  				if (inputText.contains("\n")) {
 		  					mBoardName = inputText.substring(0, inputText.indexOf("\n"));
 		  				} else if (inputText.equals("")) {
 		  					finish();
 		  				}else {
 		  					mBoardName = inputText;
 		  				}
 		  				
 		  				mGsbp = new GraphicalSoundboardProvider(mBoardName);
 		  				initEditorBoard();
 		  				
 		  				setTitle(mBoardName);
 		  				save();
 		  		}
 		  	});
 		  	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  		@Override
 		  		public void onClick(DialogInterface dialog, int whichButton) {
 		  			finish();
 	  		}
 		  	});
 		  	alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
 				@Override
 				public void onCancel(DialogInterface dialog) {
 					finish();
 				}
 		  	});
 		  	
 		  	alert.show();
 		  	
 		}
         
         mSoundImagePaint = new Paint();
         mSoundImagePaint.setColor(Color.WHITE);
         mSoundImagePaint.setAntiAlias(true);
         mSoundImagePaint.setTextAlign(Align.LEFT);
         
         File icon = new File(mSbDir, mBoardName + "/icon.png");
         if (icon.exists()) {
 			Bitmap bitmap = ImageDrawing.decodeFile(this.getApplicationContext(), icon);
             Drawable drawable = new BitmapDrawable(getResources(), IconUtils.resizeIcon(this, bitmap, (40/6)));
         	this.getActionBar().setLogo(drawable);
         }
         
         mPanel = new DrawingPanel(this);
         ViewTreeObserver vto = mPanel.getViewTreeObserver();
         vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
             	if (!mPanelInitialized) {
             		mPanelInitialized = true;
             		issueResolutionConversion();
             	}
             }
         });
 	}
 	
 	public void initEditorBoard() {
 		int rotation = getWindowManager().getDefaultDisplay().getRotation();
 		GraphicalSoundboard newGsb = mGsbp.getBoardForRotation(rotation);
 		editorOrientation = new EditorOrientation();
 		editorOrientation.setCurrentOrientation(rotation);
 		
 		if (!mGsbp.orientationChangeAllowed()) {
 			if (newGsb.getScreenOrientation() == GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT) {
 				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 			} else if (newGsb.getScreenOrientation() == GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE) {
 				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 			} else {
 				Log.wtf(TAG, "Unknown screen orientation");
 			}
 		}
 			
 		initBoard(newGsb);
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		menu.clear();
 		mMenu = menu;
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.board_editor_bottom, menu);
 	    
 	    if (mMode == EDIT_BOARD) {
 	    	menu.setGroupVisible(R.id.edit_mode, false);
 	    } else {
 	    	menu.setGroupVisible(R.id.listen_mode, false);
 	    }
 	    
 	    return super.onCreateOptionsMenu(menu);
     }
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
 		
         switch(item.getItemId()) {
         	case R.id.menu_listen_mode:
         		mMode = LISTEN_BOARD;
         		this.onCreateOptionsMenu(mMenu);
         		return true;
         		
         	case R.id.menu_edit_mode:
         		mMode = EDIT_BOARD;
         		this.onCreateOptionsMenu(mMenu);
         		return true;
         		
         	case R.id.menu_undo:
         		mBoardHistory.undo(this);
         		return true;
         	
         	case R.id.menu_redo:
         		mBoardHistory.redo(this);
         		return true;
         		
         	case R.id.menu_add_sound:
         		Intent i = new Intent(this, FileExplorer.class);
         		i.putExtra(FileExplorer.EXTRA_ACTION_KEY, FileExplorer.ACTION_ADD_GRAPHICAL_SOUND);
         		i.putExtra(FileExplorer.EXTRA_BOARD_NAME_KEY, mBoardName);
             	startActivityForResult(i, EXPLORE_SOUND);
             	return true;
             	
         	case R.id.menu_paste_sound:
         		GraphicalSound pasteSound = SoundboardMenu.mCopiedSound;
         		if (pasteSound == null) {
         			Toast.makeText(this, "Nothing copied", Toast.LENGTH_LONG).show();
         		} else {
         			if (mGsb.getAutoArrange()) {
         				if (placeToFreeSlot(pasteSound)) {
         					mGsb.getSoundList().add(pasteSound);
         				}
         			} else {
         				placeToFreeSpace(pasteSound);
         				mGsb.getSoundList().add(pasteSound);
         			}
         			mBoardHistory.createHistoryCheckpoint(mGsb);
         		}
             	return true;
             	
             case R.id.menu_save_board:
             	save();
                 return true;
                 
             case R.id.menu_convert_board:
             	
             	AlertDialog.Builder convertBuilder = new AlertDialog.Builder(this);
           	  	convertBuilder.setTitle("Convert");
           	  	convertBuilder.setMessage("Clear board directory?");
       	  	
 	          	convertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						mClearBoardDir = true;
 						initializeConvert();
 					}
 	          		
 	          	});
 
 	          	convertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 		          	public void onClick(DialogInterface dialog, int whichButton) {
 		          		mClearBoardDir = false;
 		          		initializeConvert();
 	          	    }
 	          	});
 	          	convertBuilder.setCancelable(false);
 	          	convertBuilder.show();
 			    
             	return true;
             
             case R.id.menu_play_pause:
             	SoundPlayerControl.togglePlayPause();
             	return true;
             	
             case R.id.menu_notification:
             	SoundboardMenu.updateNotification(this, mBoardName, mBoardName);
             	return true;
             	
             case R.id.menu_take_screenshot:
             	
             	Bitmap bitmap = Bitmap.createBitmap(mPanel.getWidth(), mPanel.getHeight(), Bitmap.Config.ARGB_8888);
             	Canvas canvas = new Canvas(bitmap);
             	mPanel.onDraw(canvas);
 	            Toast.makeText(getApplicationContext(), FileProcessor.saveScreenshot(bitmap, mBoardName), Toast.LENGTH_LONG).show();
 				
             	return true;
             	
             case R.id.menu_board_settings:
             	
             	final CharSequence[] items = {"Sound", "Background", "Icon", "Screen orientation", "Auto-arrange", "Reset position"};
 
             	AlertDialog.Builder setAsBuilder = new AlertDialog.Builder(BoardEditor.this);
             	setAsBuilder.setTitle("Board settings");
             	setAsBuilder.setItems(items, new DialogInterface.OnClickListener() {
             	    public void onClick(DialogInterface dialog, int item) {
             	    	if (item == 0) {
             	    		LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
             	    			getSystemService(LAYOUT_INFLATER_SERVICE);
                         	View layout = inflater.inflate(R.layout.graphical_soundboard_editor_alert_board_sound_settings,
                         	    (ViewGroup) findViewById(R.id.alert_settings_root));
                         	
                         	final CheckBox checkPlaySimultaneously = 
                       	  		(CheckBox) layout.findViewById(R.id.playSimultaneouslyCheckBox);
                       	  	checkPlaySimultaneously.setChecked(mGsb.getPlaySimultaneously());
                       	  	
                       	  	final EditText boardVolumeInput = (EditText) layout.findViewById(R.id.boardVolumeInput);
                   	  			boardVolumeInput.setText(mGsb.getBoardVolume()*100 + "%");
                   	  			
                   	  		AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
                       	  	builder.setView(layout);
                       	  	builder.setTitle("Board settings");
                   	  	
             	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             	          		public void onClick(DialogInterface dialog, int whichButton) {
             	          			boolean notifyIncorrectValue = false;
             	          			
             	          			mGsb.setPlaySimultaneously(checkPlaySimultaneously.isChecked());
             	          			
             	          			Float boardVolumeValue = null;
             	          			try {
             	          				String boardVolumeString = boardVolumeInput.getText().toString();
             	          				if (boardVolumeString.contains("%")) {
             	          					boardVolumeValue = Float.valueOf(boardVolumeString.substring(0, 
             	          							boardVolumeString.indexOf("%"))).floatValue()/100;
             	          				} else {
             	          					boardVolumeValue = Float.valueOf(boardVolumeString).floatValue()/100;
             	          				}
             	          				
             	          				
             	          				if (boardVolumeValue >= 0 && boardVolumeValue <= 1 && boardVolumeValue != null) {
             	          					mGsb.setBoardVolume(boardVolumeValue);
             		          			} else {
             		          				notifyIncorrectValue = true;
             		          			}
             	          			} catch(NumberFormatException nfe) {
             	          				notifyIncorrectValue = true;
             	          			}
             	          			
             	          			if (notifyIncorrectValue == true) {
             	          				Toast.makeText(getApplicationContext(), "Incorrect value", 
             	          						Toast.LENGTH_SHORT).show();
             	          			}
             	          		}
             	          	});
 
             	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             		          	public void onClick(DialogInterface dialog, int whichButton) {
             	          	    }
             	          	});
             	          	
             	          	builder.show();
                         	
             	    	} else if (item == 1) {
             	    		LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
         	    				getSystemService(LAYOUT_INFLATER_SERVICE);
             	    		View layout = inflater.inflate(R.layout.
             	    			graphical_soundboard_editor_alert_board_background_settings,
                     	        (ViewGroup) findViewById(R.id.alert_settings_root));
             	    		
             	    		final CheckBox checkUseBackgroundImage = 
                       	  		(CheckBox) layout.findViewById(R.id.useBackgroundFileCheckBox);
                   	  		checkUseBackgroundImage.setChecked(mGsb.getUseBackgroundImage());
                       	
                       		final Button backgroundColorButton =
                       			(Button) layout.findViewById(R.id.backgroundColorButton);
                       		backgroundColorButton.setOnClickListener(new OnClickListener() {
                 				public void onClick(View v) {
                 					Intent i = new Intent(BoardEditor.this, ColorChanger.class);
                 			    	i.putExtra("parentKey", "changeBackgroundColor");
                 			    	i.putExtras(XStreamUtil.getSoundboardBundle(mGsb));
                 			    	startActivityForResult(i, CHANGE_BACKGROUND_COLOR);
                 				}
                       		});
                       		
                       		final Button toggleMoveBackgroundFileButton =
                       			(Button) layout.findViewById(R.id.toggleMoveBackgroundFileButton);
                       		if (mMoveBackground) {
                       			toggleMoveBackgroundFileButton.setText("Move Background (file) : Yes");
                       		} else {
                       			toggleMoveBackgroundFileButton.setText("Move Background (file) : No");
                       		}
                       		toggleMoveBackgroundFileButton.setOnClickListener(new OnClickListener() {
                 				public void onClick(View v) {
                 					mMoveBackground = mMoveBackground ? false : true;
                 					if (mMoveBackground) {
                 	          			toggleMoveBackgroundFileButton.setText("Move Background (file) : Yes");
                 	          		} else {
                 	          			toggleMoveBackgroundFileButton.setText("Move Background (file) : No");
                 	          		}
                 				}
                       		});
                       	  	
                       	  	final Button backgroundFileButton = 
                       	  		(Button) layout.findViewById(R.id.backgroundFileButton);
                       	  	backgroundFileButton.setOnClickListener(new OnClickListener() {
             					public void onClick(View v) {
             						selectBackgroundFile();
             					}
                       	  	});
                       	  	
                       	  	mBackgroundWidthText = (TextView) layout.findViewById(R.id.backgroundWidthText);
                       	  	mBackgroundHeightText = (TextView) layout.findViewById(R.id.backgroundHeightText);
                       	  	
                       	  	if (mGsb.getBackgroundImage() != null) {
 	                      	  	mBackgroundWidthText.setText("Width (" + mGsb.getBackgroundImage().getWidth() + ")");
 	            				mBackgroundHeightText.setText("Height (" + mGsb.getBackgroundImage().getHeight() + ")");
                       	  	}
                       	  	
                       	  	mBackgroundWidthInput = (EditText) layout.findViewById(R.id.backgroundWidthInput);
                       	  	mBackgroundWidthInput.setText(Float.toString(mGsb.getBackgroundWidth()));
                       	  	
                       	  	mBackgroundHeightInput = (EditText) layout.findViewById(R.id.backgroundHeightInput);
                     	  	mBackgroundHeightInput.setText(Float.toString(mGsb.getBackgroundHeight()));
 
                     	  	final CheckBox scaleWidthHeight = 
                     	  			(CheckBox) layout.findViewById(R.id.scaleWidthHeightCheckBox);
                     	  	scaleWidthHeight.setChecked(true);
                     	  	
                     	  	scaleWidthHeight.setOnClickListener(new OnClickListener() {
                     	  		public void onClick(View v) {
                     	  			try {
                     	  				// Calculate a new scale
 		              	  				mWidthHeightScale = Float.valueOf(mBackgroundWidthInput.getText().toString()).floatValue() 
 		              	  									/ Float.valueOf(mBackgroundHeightInput.getText().toString()).floatValue();
 		              	  			} catch(NumberFormatException nfe) {Log.e(TAG, "Unable to calculate width and height scale", nfe);}
                     	  		}
                     	  	});
                     	  	mWidthHeightScale = mGsb.getBackgroundWidth() / mGsb.getBackgroundHeight();
 
                       	  	mBackgroundWidthInput.setOnKeyListener(new OnKeyListener() {
             					public boolean onKey(View v, int keyCode, KeyEvent event) {
             						if (scaleWidthHeight.isChecked()) {
             							try {
             								float value = Float.valueOf(
             										mBackgroundWidthInput.getText().toString()).floatValue();
             								mBackgroundHeightInput.setText(
             										Float.valueOf(value/mWidthHeightScale).toString());
             							} catch(NumberFormatException nfe) {}
             						}
             						return false;
             					}
                       	  	});
                       	  	
                       	 	mBackgroundHeightInput.setOnKeyListener(new OnKeyListener() {
             					public boolean onKey(View v, int keyCode, KeyEvent event) {
             						if (scaleWidthHeight.isChecked()) {
             							try {
             								float value = Float.valueOf(
             										mBackgroundHeightInput.getText().toString()).floatValue();
             								mBackgroundWidthInput.setText(
             										Float.valueOf(value*mWidthHeightScale).toString());
             							} catch(NumberFormatException nfe) {}
             						}
             						return false;
             					}
                     	  	});
                       	 	
                       	 	AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
                       	  	builder.setView(layout);
                       	  	builder.setTitle("Board settings");
                   	  	
             	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             	          		public void onClick(DialogInterface dialog, int whichButton) {
             	          			
             	          			boolean notifyIncorrectValue = false;
             	          			mGsb.setUseBackgroundImage(checkUseBackgroundImage.isChecked());
             	          			
             	          			try {
             	          				mGsb.setBackgroundWidth(Float.valueOf(mBackgroundWidthInput.getText().toString()).floatValue());
             	          				mGsb.setBackgroundHeight(Float.valueOf(mBackgroundHeightInput.getText().toString()).floatValue());
             	          			} catch(NumberFormatException nfe) {
             	          				notifyIncorrectValue = true;
             	          			}
             	          			
             	          			if (notifyIncorrectValue == true) {
             	          				Toast.makeText(getApplicationContext(), "Incorrect value", 
             	          						Toast.LENGTH_SHORT).show();
             	          			}
             	          			mBoardHistory.createHistoryCheckpoint(mGsb);
             	          		}
             	          	});
 
             	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             		          	public void onClick(DialogInterface dialog, int whichButton) {
             	          	    }
             	          	});
             	          	
             	          	builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
 								public void onCancel(DialogInterface dialog) {
 									mBackgroundDialog = null;
 								}
 							});
             	          	
             	          	mBackgroundDialog = builder.create();
             	          	mBackgroundDialog.show();
             	    	} else if (item == 2) {
             	    		AlertDialog.Builder resetBuilder = new AlertDialog.Builder(
 		                			BoardEditor.this);
 		                	resetBuilder.setTitle("Change board icon");
 		                	resetBuilder.setMessage("You can change icon for this board.\n\n" +
 		                			"You need a png image:\n " + mSbDir + "/" + mBoardName + "/" + "icon.png\n\n" +
 		                			"Recommended size is about 80x80 pixels.");
 		                	AlertDialog resetAlert = resetBuilder.create();
 		                	resetAlert.show();
             	    	} else if (item == 3) {
             	    		final CharSequence[] items = {"Portrait", "Landscape", "Hybrid (beta)"};
 
 		                	AlertDialog.Builder orientationBuilder = new AlertDialog.Builder(BoardEditor.this);
 		                	orientationBuilder.setTitle("Select orientation");
 		                	orientationBuilder.setItems(items, new DialogInterface.OnClickListener() {
 		                		public void onClick(DialogInterface dialog, int item) {
 		                			if (item == 0 && mGsbp.getOrientationMode() != GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_PORTRAIT) {
 		                				if (mGsbp.getOrientationMode() == GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_HYBRID) {
 		                					useOrientationAndAskToRemoveUnusedAlert(GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT);
 		                				} else {
 		                					if (mGsbp.boardWithOrientationExists(GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT)) {
 		                						orientationTurningConflictActionAlert(GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT);
 		                					} else {
 		                						orientationTurningAlert(GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT);
 		                					}
 		                				}
 		                			} else if (item == 1 && mGsbp.getOrientationMode() != GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_LANDSCAPE) {
 		                				if (mGsbp.getOrientationMode() == GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_HYBRID) {
 		                					useOrientationAndAskToRemoveUnusedAlert(GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE);
 		                				} else {
 		                					if (mGsbp.boardWithOrientationExists(GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE)) {
 		                						orientationTurningConflictActionAlert(GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE);
 		                					} else {
 		                						orientationTurningAlert(GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE);
 		                					}
 		                				}
 		                			} else if (item == 2 && mGsbp.getOrientationMode() != GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_HYBRID) {
 		                				hybridAlert();
 		                			}
 		                		}
 		                	});
 		                	
 		                	AlertDialog orientationAlert = orientationBuilder.create();
 		                	orientationAlert.show();
             	    	} else if (item == 4) {
             	    	//Auto-arrange
             	    	LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
     	    				getSystemService(LAYOUT_INFLATER_SERVICE);
         	    		View layout = inflater.inflate(R.layout.
         	    			graphical_soundboard_editor_alert_auto_arrange,
                 	        (ViewGroup) findViewById(R.id.alert_settings_root));
         	    		
         	    		final CheckBox checkEnableAutoArrange = 
                   	  		(CheckBox) layout.findViewById(R.id.enableAutoArrange);
         	    		checkEnableAutoArrange.setChecked(mGsb.getAutoArrange());
                   	  	
         	    		final EditText columnsInput = (EditText) layout.findViewById(R.id.columnsInput);
         	    		columnsInput.setText(Integer.toString(mGsb.getAutoArrangeColumns()));
                   	  	
         	    		final EditText rowsInput = (EditText) layout.findViewById(R.id.rowsInput);
         	    		rowsInput.setText(Integer.toString(mGsb.getAutoArrangeRows()));
                   	 	
                   	 	AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
                   	  	builder.setView(layout);
                   	  	builder.setTitle("Board settings");
               	  	
         	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
         	          		public void onClick(DialogInterface dialog, int whichButton) {
         	          			try {
         	          				int columns = Integer.valueOf(
         	          						columnsInput.getText().toString()).intValue();
         	          				int rows = Integer.valueOf(
         	          						rowsInput.getText().toString()).intValue();
         	          				
         	          				if (mGsb.getSoundList().size() <= columns*rows || !checkEnableAutoArrange.isChecked()) {
         	          					if (mGsb.getAutoArrange() != checkEnableAutoArrange.isChecked() ||
         	          							mGsb.getAutoArrangeColumns() != columns ||
         	          							mGsb.getAutoArrangeRows() != rows) {
         	          						
         	          						mGsb.setAutoArrange(checkEnableAutoArrange.isChecked());
 	            	          				mGsb.setAutoArrangeColumns(columns);
 	            	          				mGsb.setAutoArrangeRows(rows);
         	          					}
         	          				} else {
         	          					Toast.makeText(getApplicationContext(), "Not enought slots", 
             	          						Toast.LENGTH_SHORT).show();
         	          				}
         	          				mBoardHistory.createHistoryCheckpoint(mGsb);
         	          			} catch(NumberFormatException nfe) {
         	          				Toast.makeText(getApplicationContext(), "Incorrect value", 
         	          						Toast.LENGTH_SHORT).show();
         	          			}
         	          		}
         	          	});
 
         	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
         		          	public void onClick(DialogInterface dialog, int whichButton) {
         	          	    }
         	          	});
         	          	
         	          	builder.show();
             	    	} else if (item == 5) {
             	    		ArrayList<String> itemArray = new ArrayList<String>();
             	    		itemArray.add("> Background image");
 		    	    		for (GraphicalSound sound : mGsb.getSoundList()) {
 		    	    			itemArray.add(sound.getName());
 		    	    		}
 		    	    		CharSequence[] items = itemArray.toArray(new CharSequence[itemArray.size()]);
 		    	    		
 		    	    		AlertDialog.Builder resetBuilder = new AlertDialog.Builder(
 		                			BoardEditor.this);
 		                	resetBuilder.setTitle("Reset position");
 		                	resetBuilder.setItems(items, new DialogInterface.OnClickListener() {
 		                	    public void onClick(DialogInterface dialog, int item) {
 		                	    	if (item == 0) { // Background
 		                	    		mGsb.setBackgroundX(0);
 		                	    		mGsb.setBackgroundY(0);
 		                	    		mBoardHistory.createHistoryCheckpoint(mGsb);
 		                	    	} else { // Sound
 		                	    		GraphicalSound sound = mGsb.getSoundList().get(item);
 			                	    	sound.setNameFrameX(50);
 			        	    			sound.setNameFrameY(50);
 			        	    			sound.generateImageXYFromNameFrameLocation();
 			        	    			mBoardHistory.createHistoryCheckpoint(mGsb);
 		                	    	}
 		                	    }
 		                	});
 		                	AlertDialog resetAlert = resetBuilder.create();
 		                	resetAlert.show();
 		    	    		
             	    	}
             	    }
             	});
             	AlertDialog setAsAlert = setAsBuilder.create();
             	setAsAlert.show();
             	
             	return true;
             	
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 	public void initBoard(GraphicalSoundboard gsb) {
 		loadBoard(gsb);
 		
 		int boardId = gsb.getId();
 		BoardHistory boardHistory = mBoardHistoryProvider.getBoardHistory(boardId);
 		
 		if (boardHistory == null) {
 			boardHistory = mBoardHistoryProvider.createBoardHistory(boardId, gsb);
 		}
 		
 		this.mBoardHistory = boardHistory;
 	}
 	
 	public void loadBoard(GraphicalSoundboard gsb) {
 		GraphicalSoundboard.loadImages(this.getApplicationContext(), gsb);
 		mGsb = gsb;
 	}
 	
 	private void orientationTurningConflictActionAlert(final int screenOrientation) {
 		String orientationName = GraphicalSoundboard.getOrientationName(screenOrientation);
 		String oppositeOrientationName = GraphicalSoundboard.getOppositeOrientationName(screenOrientation);
 		
 		AlertDialog.Builder orientationWarningBuilder = new AlertDialog.Builder(BoardEditor.this);
 		orientationWarningBuilder.setTitle("Conflicting board");
 			orientationWarningBuilder.setMessage(
 		  			"A board for " + orientationName + " orientation already exists. You can either use it or remove it.\n\n" +
 		  			"By removing it you can turn " + oppositeOrientationName + " board to " + orientationName + " orientation.\n\n");
 	  	orientationWarningBuilder.setPositiveButton("Remove board", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsbp.deleteBoardWithOrientation(screenOrientation);
 				orientationTurningAlert(screenOrientation);
 			}
 	  	});
 	  	orientationWarningBuilder.setNegativeButton("Use board", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsbp.setOrientationMode(screenOrientation);
 				useOrientationAndAskToRemoveUnusedAlert(screenOrientation);
 			}
 	  	});
 	  	AlertDialog orientationWarningAlert = orientationWarningBuilder.create();
     	orientationWarningAlert.show();
 	}
 	
 	private void orientationTurningAlert(final int screenOrientation) {
 		AlertDialog.Builder orientationWarningBuilder = new AlertDialog.Builder(
 			BoardEditor.this);
 		orientationWarningBuilder.setTitle("Changing orientation");
 			orientationWarningBuilder.setMessage(
 		  			"Changing screen orientation will delete all position data if you don't " +
 		  			"select deny.\n\n" +
 		  			"Proceed?");
 	  	orientationWarningBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsb.setBackgroundX(0);
 				mGsb.setBackgroundY(0);
 	    		for(GraphicalSound sound : mGsb.getSoundList()) {
 	    			sound.setNameFrameX(50);
 	    			sound.setNameFrameY(50);
 	    			sound.generateImageXYFromNameFrameLocation();
 	    		}
 	    		mGsbp.setOrientationMode(screenOrientation);
 	    		mGsb.setScreenOrientation(screenOrientation);
 	    		finishBoard();
 			}
 	  	});
 	  	orientationWarningBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 	  	});
 	  	orientationWarningBuilder.setNeutralButton("Deny", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsbp.setOrientationMode(screenOrientation);
 				mGsb.setScreenOrientation(screenOrientation);
 				finishBoard();
 			}
 	  	});
 	  	AlertDialog orientationWarningAlert = orientationWarningBuilder.create();
     	orientationWarningAlert.show();
 	}
 	
 	private void hybridAlert() {
 		AlertDialog.Builder orientationWarningBuilder = new AlertDialog.Builder(
 			BoardEditor.this);
 		orientationWarningBuilder.setTitle("Hybrid mode");
 			orientationWarningBuilder.setMessage(
 		  			"Hybrid mode allows you to switch between portrait and landscape by turning the screen.\n\n" +
 		  			"However both orientations must be created and maintained separately.\n\n" +
 		  			"Proceed?");
 	  	orientationWarningBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsbp.setOrientationMode(GraphicalSoundboardHolder.OrientationMode.ORIENTATION_MODE_HYBRID);
 	          	finishBoard();
 			}
 	  	});
 	  	orientationWarningBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 	  	});
 	  	AlertDialog orientationWarningAlert = orientationWarningBuilder.create();
     	orientationWarningAlert.show();
 	}
 	
 	private void useOrientationAndAskToRemoveUnusedAlert(final int screenOrientation) {
 		final int oppositeOrientation = GraphicalSoundboard.getOppositeOrientation(screenOrientation);
 		
 		AlertDialog.Builder orientationWarningBuilder = new AlertDialog.Builder(
 			BoardEditor.this);
 		orientationWarningBuilder.setTitle("Unused board");
 			orientationWarningBuilder.setMessage(
 		  			"Do you want to delete unused board in " +  GraphicalSoundboard.getOrientationName(oppositeOrientation) + " orientation?\n\n");
 	  	orientationWarningBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				//mThread.setRunning(false); // TODO handle board deleting better
 				mGsb = mGsbp.getBoard(screenOrientation);
 				mGsbp.deleteBoardWithOrientation(oppositeOrientation);
 				mGsbp.setOrientationMode(screenOrientation);
 	    		finishBoard();
 			}
 	  	});
 	  	orientationWarningBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				mGsbp.setOrientationMode(screenOrientation);
 	    		finishBoard();
 			}
 	  	});
 	  	AlertDialog orientationWarningAlert = orientationWarningBuilder.create();
     	orientationWarningAlert.show();
 	}
 	
 	private void finishBoard() {
 		try {
 			BoardEditor.this.finish();
 		} catch (Throwable e) {
 			Log.e(TAG, "Error closing board", e);
 		}
 	}
 	
 	private void selectBackgroundFile() {
 		Intent i = new Intent(this, FileExplorer.class);
 		i.putExtra(FileExplorer.EXTRA_ACTION_KEY, FileExplorer.ACTION_SELECT_BACKGROUND_FILE);
 		i.putExtra(FileExplorer.EXTRA_BOARD_NAME_KEY, mBoardName);
     	startActivityForResult(i, EXPLORE_BACKGROUD);
 	}
 	
 	private void selectImageFile() {
 		Intent i = new Intent(this, FileExplorer.class);
 		i.putExtra(FileExplorer.EXTRA_ACTION_KEY, FileExplorer.ACTION_SELECT_SOUND_IMAGE_FILE);
 		i.putExtra(FileExplorer.EXTRA_BOARD_NAME_KEY, mBoardName);
     	startActivityForResult(i, EXPLORE_SOUND_IMAGE);
 	}
 	
 	private void selectActiveImageFile() {
 		Intent i = new Intent(this, FileExplorer.class);
 		i.putExtra(FileExplorer.EXTRA_ACTION_KEY, FileExplorer.ACTION_SELECT_SOUND_ACTIVE_IMAGE_FILE);
 		i.putExtra(FileExplorer.EXTRA_BOARD_NAME_KEY, mBoardName);
     	startActivityForResult(i, EXPLORE_SOUND_ACTIVE_IMAGE);
 	}
 	
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
         
         switch(requestCode) {
 	        case EXPLORE_SOUND:
 	        	
 	        	if (resultCode == RESULT_OK) {
 		        	Bundle extras = intent.getExtras();
 		        	XStream xstream = XStreamUtil.graphicalBoardXStream();
 		        	
 		        	GraphicalSound sound = (GraphicalSound) xstream.fromXML(extras.getString(FileExplorer.ACTION_ADD_GRAPHICAL_SOUND));
 		        	sound.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.sound));
 		        	sound.setAutoArrangeColumn(0);
 		        	sound.setAutoArrangeRow(0);
 		        	if (mGsb.getAutoArrange()) {
 		        		if (placeToFreeSlot(sound)) {
 		        			mGsb.getSoundList().add(sound);
    	    			 	}
 		        	} else {
 		        		placeToFreeSpace(sound);
 		        		mGsb.getSoundList().add(sound);
 		        	}
 		        	mBoardHistory.createHistoryCheckpoint(mGsb);
 	        	}
 	        	break;
 	        	
 	        case EXPLORE_BACKGROUD:
 	        	
 	        	if (resultCode == RESULT_OK) {
 		        	Bundle extras = intent.getExtras();
 		        	File background = new File(extras.getString(FileExplorer.ACTION_SELECT_BACKGROUND_FILE));
 		        	mGsb.setBackgroundImagePath(background);
 		        	mGsb.setBackgroundImage(ImageDrawing.decodeFile(this.getApplicationContext(), mGsb.getBackgroundImagePath()));
 		        	mGsb.setBackgroundWidth(mGsb.getBackgroundImage().getWidth());
 		        	mGsb.setBackgroundHeight(mGsb.getBackgroundImage().getHeight());
 		        	mGsb.setBackgroundX(0);
 					mGsb.setBackgroundY(0);
 					mBoardHistory.createHistoryCheckpoint(mGsb);
 	        	}
	        	if (mBackgroundDialog != null) {
 	        		mBackgroundWidthText.setText("Width (" + mGsb.getBackgroundImage().getWidth() + ")");
 					mBackgroundHeightText.setText("Height (" + mGsb.getBackgroundImage().getHeight() + ")");
 					mBackgroundWidthInput.setText(Float.toString(mGsb.getBackgroundWidth()));
 					mBackgroundHeightInput.setText(Float.toString(mGsb.getBackgroundHeight()));
 	        	}
 	        	break;
 	        
 	        case EXPLORE_SOUND_IMAGE:
 	        	
 	        	if (resultCode == RESULT_OK) {
 		        	Bundle extras = intent.getExtras();
 		        	File image = new File(extras.getString(FileExplorer.ACTION_SELECT_SOUND_IMAGE_FILE));
 		        	mDragSound.setImagePath(image);
 		        	mDragSound.setImage(ImageDrawing.decodeFile(this.getApplicationContext(), mDragSound.getImagePath()));
 	        	}
 	        	if (mSoundImageDialog != null) {
 	        		mSoundImageWidthText.setText("Width (" + mDragSound.getImage().getWidth() + ")");
 					mSoundImageHeightText.setText("Height (" + mDragSound.getImage().getHeight() + ")");
 					mSoundImageWidthInput.setText(Float.toString(mDragSound.getImage().getWidth()));
 					mSoundImageHeightInput.setText(Float.toString(mDragSound.getImage().getHeight()));
 	        	}
 	        	break;
 	        	
 	        case EXPLORE_SOUND_ACTIVE_IMAGE:
 	        	
 	        	if (resultCode == RESULT_OK) {
 		        	Bundle extras = intent.getExtras();
 		        	File image = new File(extras.getString(FileExplorer.ACTION_SELECT_SOUND_ACTIVE_IMAGE_FILE));
 		        	mDragSound.setActiveImagePath(image);
 		        	mDragSound.setActiveImage(ImageDrawing.decodeFile(this.getApplicationContext(), mDragSound.getActiveImagePath()));
 	        	}
 	        	break;
 	        	
 	        case CHANGE_NAME_COLOR:
 	        	
 	        	if (resultCode == RESULT_OK) {
 		        	Bundle extras = intent.getExtras();
 		        	if (extras.getBoolean("copyKey")) {
 		        		mCopyColor = CHANGE_NAME_COLOR;
 		        	} else {
 		        		mDragSound.setNameTextColorInt(extras.getInt("colorKey"));
 		        	}
 	        	}
 	        	break;
 	        	
 	        case CHANGE_INNER_PAINT_COLOR:
 	
 				if (resultCode == RESULT_OK) {
 					Bundle extras = intent.getExtras();
 		        	if (extras.getBoolean("copyKey")) {
 		        		mCopyColor = CHANGE_INNER_PAINT_COLOR;
 		        	} else {
 		        		mDragSound.setNameFrameInnerColorInt(extras.getInt("colorKey"));
 		        	}
 				}
 				break;
 				
 	        case CHANGE_BORDER_PAINT_COLOR:
 	        	
 	        	if (resultCode == RESULT_OK) {
 	        		Bundle extras = intent.getExtras();
 		        	if (extras.getBoolean("copyKey")) {
 		        		mCopyColor = CHANGE_BORDER_PAINT_COLOR;
 		        	} else {
 		        		mDragSound.setNameFrameBorderColorInt(extras.getInt("colorKey"));
 		        	}
 	        	}
 	        	break;
 	        	
 	        case CHANGE_BACKGROUND_COLOR:
 	        	
 	        	if (resultCode == RESULT_OK) {
 	        		Bundle extras = intent.getExtras();
 		        	mGsb.setBackgroundColor(extras.getInt("colorKey"));
 		        	mBoardHistory.createHistoryCheckpoint(mGsb);
 	        	}
 	        	break;
 	        	
 	        case CHANGE_SOUND_PATH:
 	        	if (resultCode == RESULT_OK) {
                 	
                 	LayoutInflater removeInflater = (LayoutInflater) 
                 			BoardEditor.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                 	View removeLayout = removeInflater.inflate(
                 			R.layout.graphical_soundboard_editor_alert_remove_sound,
                 	        (ViewGroup) findViewById(R.id.alert_remove_sound_root));
               	  	
               	  	final CheckBox removeFileCheckBox = 
               	  		(CheckBox) removeLayout.findViewById(R.id.removeFile);
               	  	removeFileCheckBox.setText(" DELETE " + mDragSound.getPath().getAbsolutePath());
               	  	
               	  	AlertDialog.Builder removeBuilder = new AlertDialog.Builder(
               	  		BoardEditor.this);
               	  	removeBuilder.setView(removeLayout);
               	  	removeBuilder.setTitle("Changing sound");
           	  	
               	  	removeBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
     	          	  	public void onClick(DialogInterface dialog, int whichButton) {
     	          	  		if (removeFileCheckBox.isChecked() == true) {
     	          	  			mDragSound.getPath().delete();
     	          	  		}
 	    	          	  	Bundle extras = intent.getExtras();
 				        	mDragSound.setPath(new File(extras.getString(FileExplorer.ACTION_CHANGE_SOUND_PATH)));
     	          	    }
     	          	});
 
               	  	removeBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
     		          	public void onClick(DialogInterface dialog, int whichButton) {
     	          	    }
     	          	});
     	          	
               	  	removeBuilder.setCancelable(false);
               	  	removeBuilder.show();
 	        	}
 	        	break;
 	        
 	        default:
 	        	break;
         }
     }
 	
 	private void initializeConvert() {
 		mWaitDialog = ProgressDialog.show(BoardEditor.this, "", "Please wait", true);
 		
 		Thread t = new Thread() {
 			public void run() {
 				Looper.prepare();
 				try {
 					if (mClearBoardDir) {
 						cleanDirectory(new File(mSbDir, mBoardName).listFiles());
 					}
 					
 					FileProcessor.convertGraphicalBoard(BoardEditor.this, mBoardName, mGsb);
 					save();
 				} catch (IOException e) {
 					Log.e(TAG, "Error converting board", e);
 				}
 				mHandler.post(mUpdateResults);
 	        }
 	    }; 
 	    t.start();
 	}
 	
 	private void cleanDirectory(File[] files) {
 		for (File file : files) {
 			
 			if (file.isDirectory()) {
 				cleanDirectory(file.listFiles());
 				if (file.listFiles().length == 0) {
 					Log.d(TAG, "Deleting empty directory " + file.getAbsolutePath());
 					file.delete();
 				}
 			} else {
 				
 				boolean boardUsesFile = false;
 
 				if (file.getName().equals("graphicalBoard") == true || file.getName().equals("icon.png") == true) {
 					boardUsesFile = true;
 				}
 
 				try {
 					if (file.getName().equals(mGsb.getBackgroundImagePath().getName())) boardUsesFile = true;
 				} catch (NullPointerException e) {}
 
 				for (GraphicalSound sound : mGsb.getSoundList()) {
 					if (boardUsesFile) break;
 
 					try {
 						if (sound.getPath().getAbsolutePath().equals(file.getAbsolutePath())) boardUsesFile = true;
 					} catch (NullPointerException e) {}
 
 					try {
 						if (sound.getImagePath().getAbsolutePath().equals(file.getAbsolutePath())) boardUsesFile = true;
 					} catch (NullPointerException e) {}
 
 					try {
 						if (sound.getActiveImagePath().getAbsolutePath().equals(file.getAbsolutePath())) boardUsesFile = true;
 					} catch (NullPointerException e) {}
 				}
 
 				if (boardUsesFile == false) {
 					Log.d(TAG, "Deleting unused file " + file.getAbsolutePath());
 					file.delete();
 				}
 			}
 		}
 	}
 	
 	final Runnable mUpdateResults = new Runnable() {
         public void run() {
         	mWaitDialog.dismiss();
         }
     };
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
     	int originalOrientation = editorOrientation.getCurrentOrientation();
     	
     	int rotation = -1;
     	if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
     		rotation = Surface.ROTATION_0;
 		} else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			rotation = Surface.ROTATION_90;
 		}
     	
     	editorOrientation.setCurrentOrientation(rotation);
     	int currentOrientation = editorOrientation.getCurrentOrientation();
 
     	if (!(originalOrientation == currentOrientation)) {
     		GraphicalSoundboard.unloadImages(mGsb);
     		mGsbp.overrideBoard(mGsb);
     		GraphicalSoundboard newOrientationGsb = mGsbp.getBoardForRotation(rotation);
     		initBoard(newOrientationGsb);
     		
     		// Set resolution handling stuff for the new orientation
     		if (mResolutionAlert != null) {
     			mResolutionAlert.dismiss();
     		}
     		mPanelInitialized = false;
     	}
     	
     	super.onConfigurationChanged(newConfig);
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	setContentView(mPanel);
     }
 	
 	@Override
     protected void onSaveInstanceState(Bundle outState) {
     	super.onSaveInstanceState(outState);
     }
     
     @Override
     protected void onPause() {
     	save();
     	super.onPause();
     }
     
     @Override
     protected void onDestroy() {
     	GraphicalSoundboard.unloadImages(mGsb);
     	super.onDestroy();
     }
     
     private void save() {
     	if (mBoardName != null) {
     		try {
     			GraphicalSoundboard gsb = GraphicalSoundboard.copy(mGsb);
     			if (mDragSound != null && mDrawDragSound == true) gsb.getSoundList().add(mDragSound); // Sound is being dragged
         		mGsbp.saveBoard(mBoardName, gsb);
         		Log.v(TAG, "Board " + mBoardName + " saved");
     		} catch (IOException e) {
     			Log.e(TAG, "Unable to save " + mBoardName, e);
     		}
     	}
     }
     
     private void initializeDrag(MotionEvent event, GraphicalSound sound) {
     	if (mMode == LISTEN_BOARD) {
     		if (sound.getPath().getAbsolutePath().equals(SoundboardMenu.mPauseSoundFilePath)) { 
     			SoundPlayerControl.togglePlayPause();
     		} else {
     			if (sound.getSecondClickAction() == GraphicalSound.SECOND_CLICK_PLAY_NEW) {
     				SoundPlayerControl.playSound(mGsb.getPlaySimultaneously(), sound.getPath(), sound.getVolumeLeft(), 
                 			sound.getVolumeRight(), mGsb.getBoardVolume());
     			} else if (sound.getSecondClickAction() == GraphicalSound.SECOND_CLICK_PAUSE) {
     				SoundPlayerControl.pauseSound(mGsb.getPlaySimultaneously(), sound.getPath(), sound.getVolumeLeft(), 
                 			sound.getVolumeRight(), mGsb.getBoardVolume());
     			} else if (sound.getSecondClickAction() == GraphicalSound.SECOND_CLICK_STOP) {
     				SoundPlayerControl.stopSound(mGsb.getPlaySimultaneously(), sound.getPath(), sound.getVolumeLeft(), 
                 			sound.getVolumeRight(), mGsb.getBoardVolume());
     			}
     			mCanvasInvalidated = true;
     		}
 		} else {
 			mDragSound = sound;
 			mDrawDragSound = true;
 			mInitialNameFrameX = sound.getNameFrameX();
 			mInitialNameFrameY = sound.getNameFrameY();
 			mInitialImageX = sound.getImageX();
 			mInitialImageY = sound.getImageY();
 			mGsb.getSoundList().remove(sound);
 			
 			mNameFrameDragDistanceX = event.getX() - sound.getNameFrameX();
 			mNameFrameDragDistanceY = event.getY() - sound.getNameFrameY();
 			mImageDragDistanceX = event.getX() - sound.getImageX();
 			mImageDragDistanceY = event.getY() - sound.getImageY();
 		}
     }
     
     private void copyColor(GraphicalSound sound) {
 		switch(mCopyColor) {
 			case CHANGE_NAME_COLOR:
 				mDragSound.setNameTextColorInt(sound.getNameTextColor());
 				break;
 			case CHANGE_INNER_PAINT_COLOR:
 				mDragSound.setNameFrameInnerColorInt(sound.getNameFrameInnerColor());
 				break;
 			case CHANGE_BORDER_PAINT_COLOR:
 				mDragSound.setNameFrameBorderColorInt(sound.getNameFrameBorderColor());
 				break;
 		}
 		mCopyColor = 0;
 		mBoardHistory.createHistoryCheckpoint(mGsb);
 	}
     
     void moveSound(float X, float Y) {
 		if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_TEXT) {
 			mDragSound.setNameFrameX(X-mNameFrameDragDistanceX);
 			mDragSound.setNameFrameY(Y-mNameFrameDragDistanceY);
 		}
 		if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_IMAGE) {
 			mDragSound.setImageX(X-mImageDragDistanceX);
 			mDragSound.setImageY(Y-mImageDragDistanceY);
 		}
 		mGsb.getSoundList().add(mDragSound);
 		mDrawDragSound = false;
 	}
 	
 	public void moveSoundToSlot(GraphicalSound sound, int column, int row, float imageX, float imageY, float nameX, float nameY) {
 		
 		int width = mPanel.getWidth();
 		int height = mPanel.getHeight();
 		
 		float middlePointX = width/mGsb.getAutoArrangeColumns()/2;
 		float middlePointY = height/mGsb.getAutoArrangeRows()/2;
 			
 		float lowPointX;
 		float highPointX;
 		float lowPointY;
 		float highPointY;
 		
 		boolean moveName = false;
 		boolean moveImage = false;
 		
 		SoundNameDrawing soundNameDrawing = new SoundNameDrawing(sound);
 		float nameFrameWidth = soundNameDrawing.getNameFrameRect().width();
 		float nameFrameHeight = soundNameDrawing.getNameFrameRect().height();
 		
 		if (sound.getHideImageOrText() == GraphicalSound.HIDE_TEXT) {
 			lowPointX = imageX;
 			highPointX = imageX+sound.getImageWidth();
 			lowPointY = imageY;
 			highPointY = imageY+sound.getImageHeight();
 			moveImage = true;
 		} else if (sound.getHideImageOrText() == GraphicalSound.HIDE_IMAGE) {
 			lowPointX = nameX;
 			highPointX = nameX+nameFrameWidth;
 			lowPointY = nameY;
 			highPointY = nameY+nameFrameHeight;
 			moveName = true;
 		} else {
 			lowPointX = imageX < nameX ? imageX : nameX;
 			highPointX = imageX+sound.getImageWidth() > nameX+nameFrameWidth ? 
 					imageX+sound.getImageWidth() : nameX+nameFrameWidth;
 			lowPointY = imageY < nameY ? imageY : nameY;
 			highPointY = imageY+sound.getImageHeight() > nameY+nameFrameHeight ? 
 					imageY+sound.getImageHeight() : nameY+nameFrameHeight;
 			moveImage = true;
 			moveName = true;
 		}
 		
 		float xPoint = (highPointX-lowPointX)/2;
 		float imageDistanceX = imageX-(lowPointX+xPoint);
 		float nameDistanceX =  nameX-(lowPointX+xPoint);
 		
 		float yPoint = (highPointY-lowPointY)/2;
 		float imageDistanceY = imageY-(lowPointY+yPoint);
 		float nameDistanceY =  nameY-(lowPointY+yPoint);
 		
 		float slotX = column*(width/mGsb.getAutoArrangeColumns());
 		float slotY = row*(height/mGsb.getAutoArrangeRows());
 		
 		if (moveImage) {
 			sound.setImageX(slotX+middlePointX+imageDistanceX);
 			sound.setImageY(slotY+middlePointY+imageDistanceY);
 		}
 		
 		if (moveName) {
 			sound.setNameFrameX(slotX+middlePointX+nameDistanceX);
 			sound.setNameFrameY(slotY+middlePointY+nameDistanceY);
 		}
 		
 		sound.setAutoArrangeColumn(column);
 		sound.setAutoArrangeRow(row);
 		mBoardHistory.createHistoryCheckpoint(mGsb);
 	}
 	
 	public boolean placeToFreeSlot(GraphicalSound sound) {
 		try {
     		Slot slot = AutoArrange.getFreeSlot(mGsb.getSoundList(), mGsb.getAutoArrangeColumns(), mGsb.getAutoArrangeRows());
     		moveSoundToSlot(sound, slot.getColumn(), slot.getRow(), sound.getImageX(), sound.getImageY(), sound.getNameFrameX(), sound.getNameFrameY());
     		return true;
     	} catch (NullPointerException e) {
     		Toast.makeText(getApplicationContext(), "No slot available", Toast.LENGTH_SHORT).show();
     		return false;
     	}
 	}
 	
 	public void placeToFreeSpace(GraphicalSound sound) {
 		boolean spaceAvailable = true;
 		
 		float freeSpaceX = 0;
 		float freeSpaceY = 0;
 		
 		int width = mPanel.getWidth();
 		int height = mPanel.getHeight();
 		
 		while (freeSpaceY + sound.getImageHeight() < height) {
 			spaceAvailable = true;
 			for (GraphicalSound spaceEater : mGsb.getSoundList()) {
 				if (((freeSpaceX >= spaceEater.getImageX() && freeSpaceX <= spaceEater.getImageX()+spaceEater.getImageWidth()) || 
 						freeSpaceX+sound.getImageWidth() >= spaceEater.getImageX() && freeSpaceX+sound.getImageWidth() <= spaceEater.getImageX()+spaceEater.getImageWidth()) &&
 						(freeSpaceY >= spaceEater.getImageY() && freeSpaceY <= spaceEater.getImageY()+spaceEater.getImageHeight() ||
 								freeSpaceY+sound.getImageHeight() >= spaceEater.getImageY() && freeSpaceY+sound.getImageHeight() <= spaceEater.getImageY()+spaceEater.getImageHeight())) {
 					spaceAvailable = false;
 					break;
 				}
 			}
 			if (spaceAvailable) {
 				sound.setImageX(freeSpaceX);
 				sound.setImageY(freeSpaceY);
 				sound.generateNameFrameXYFromImageLocation();
 				break;
 			}
 			freeSpaceX = freeSpaceX + 5;
 			if (freeSpaceX + sound.getImageWidth() >= width) {
 				freeSpaceX = 0;
 				freeSpaceY = freeSpaceY + 5;
 			}
 		}
 		if (!spaceAvailable) {
 			sound.setNameFrameX(10);
 			sound.setNameFrameY(sound.getImageHeight()+10);
 			sound.generateImageXYFromNameFrameLocation();
 		}
 	}
 	
 	public boolean onTrackballEvent (MotionEvent event) {
 		if (mMode == EDIT_BOARD && event.getAction() == MotionEvent.ACTION_MOVE && mDragSound != null && 
 				(mLastTrackballEvent == 0 || System.currentTimeMillis() - mLastTrackballEvent > 500)) {
 			mLastTrackballEvent = System.currentTimeMillis();
 			
 			int movementX = 0;
 			int movementY = 0;
 			
 			if (event.getX() > 0) {
 				movementX = 1;
 			} else if (event.getX() < 0) {
 				movementX = -1;
 			}else if (event.getY() > 0) {
 				movementY = 1;
 			} else if (event.getY() < 0) {
 				movementY = -1;
 			}
 			
 			if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_TEXT) {
 				mDragSound.setNameFrameX(mDragSound.getNameFrameX() + movementX);
 				mDragSound.setNameFrameY(mDragSound.getNameFrameY() + movementY);
 			}
 			if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_IMAGE) {
 				mDragSound.setImageX(mDragSound.getImageX() + movementX);
 				mDragSound.setImageY(mDragSound.getImageY() + movementY);
 			}
 			mBoardHistory.setHistoryCheckpoint(mGsb);
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 	
 	public void issueResolutionConversion() {
 		Thread t = new Thread() {
 			public void run() {
 				Looper.prepare();
 				
 				final int windowWidth = mPanel.getWidth();
 				final int windowHeight = mPanel.getHeight();
 
 				if (mGsb.getScreenHeight() == 0 || mGsb.getScreenWidth() == 0) {
 					mGsb.setScreenWidth(windowWidth);
 					mGsb.setScreenHeight(windowHeight);
 				} else if (mGsb.getScreenWidth() != windowWidth || mGsb.getScreenHeight() != windowHeight) {
 					Log.v(TAG, "Soundoard resolution has changed. X: " + mGsb.getScreenWidth() + " -> " + windowWidth + " - Y: " + mGsb.getScreenHeight() + " -> " + windowHeight);
 
 					AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
 					builder.setTitle("Display size");
 					builder.setMessage("Display size used to make this board differs from your display size.\n\n" +
 							"You can resize this board to fill your display or " +
 							"fit this board to your display. Fit looks accurately like the original one.\n\n" +
 							"(Ps. In 'Edit board' mode you can undo this.)");
 
 					builder.setPositiveButton("Resize", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							Log.v(TAG, "Resizing board");
 							float xScale = (float) windowWidth/(float) (mGsb.getScreenWidth());
 							float yScale = (float) windowHeight/(float) (mGsb.getScreenHeight());
 
 							float avarageScale = xScale+(yScale-xScale)/2;
 							Log.v(TAG, "X scale: \"" + xScale + "\"" + ", old width: \""+mGsb.getScreenWidth() + "\", new width: \"" + windowWidth + "\"");
 							Log.v(TAG, "Y scale: \"" + yScale + "\"" + ", old height: \""+mGsb.getScreenHeight() + "\", new height: \"" + windowHeight + "\"");
 							Log.v(TAG, "Avarage scale: \"" + avarageScale + "\"");
 
 							mGsb.setBackgroundX(mGsb.getBackgroundX()*xScale);
 							mGsb.setBackgroundY(mGsb.getBackgroundY()*yScale);
 							mGsb.setBackgroundWidth(mGsb.getBackgroundWidth()*xScale);
 							mGsb.setBackgroundHeight(mGsb.getBackgroundHeight()*yScale);
 
 							for (GraphicalSound sound : mGsb.getSoundList()) {
 
 								sound = SoundNameDrawing.getScaledTextSize(sound, avarageScale);
 
 								sound.setNameFrameX(sound.getNameFrameX()*xScale);
 								sound.setNameFrameY(sound.getNameFrameY()*yScale);
 
 								sound.setImageX(sound.getImageX()*xScale);
 								sound.setImageY(sound.getImageY()*yScale);
 								sound.setImageWidth(sound.getImageWidth()*avarageScale);
 								sound.setImageHeight(sound.getImageHeight()*avarageScale);
 
 								if (sound.getLinkNameAndImage()) sound.generateNameFrameXYFromImageLocation();
 							}
 
 							mGsb.setScreenWidth(windowWidth);
 							mGsb.setScreenHeight(windowHeight);
 						}
 					});
 
 					builder.setNeutralButton("Fit", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							Log.v(TAG, "Fitting board");
 
 							float xScale = (float) (windowWidth)/(float) (mGsb.getScreenWidth());
 							float yScale = (float) (windowHeight)/(float) (mGsb.getScreenHeight());
 
 							boolean xFillsDisplay = xScale < yScale;
 							float applicableScale = (xScale < yScale) ? xScale : yScale;
 
 							float hiddenAreaSize;
 
 							if (xFillsDisplay) {
 								hiddenAreaSize = ((float) windowHeight-(float) mGsb.getScreenHeight()*applicableScale)/2;
 							} else {
 								hiddenAreaSize = ((float) windowWidth-(float) mGsb.getScreenWidth()*applicableScale)/2;
 							}
 
 							Log.v(TAG, "X scale: \"" + xScale + "\"" + ", old width: \""+mGsb.getScreenWidth() + "\", new width: \"" + windowWidth + "\"");
 							Log.v(TAG, "Y scale: \"" + yScale + "\"" + ", old height: \""+mGsb.getScreenHeight() + "\", new height: \"" + windowHeight + "\"");
 							Log.v(TAG, "Applicable scale: \"" + applicableScale + "\"");
 							Log.v(TAG, "Hidden area size: \"" + hiddenAreaSize + "\"");
 
 							mGsb.setBackgroundWidth(mGsb.getBackgroundWidth()*applicableScale);
 							mGsb.setBackgroundHeight(mGsb.getBackgroundHeight()*applicableScale);
 
 							if (xFillsDisplay) {
 								mGsb.setBackgroundX(mGsb.getBackgroundX()*applicableScale);
 								mGsb.setBackgroundY(hiddenAreaSize+mGsb.getBackgroundY()*applicableScale);
 							} else {
 								mGsb.setBackgroundX(hiddenAreaSize+mGsb.getBackgroundX()*applicableScale);
 								mGsb.setBackgroundY(mGsb.getBackgroundY()*applicableScale);
 							}
 
 							for (GraphicalSound sound : mGsb.getSoundList()) {
 
 								sound = SoundNameDrawing.getScaledTextSize(sound, applicableScale);
 
 								if (xFillsDisplay) {
 									sound.setNameFrameX(sound.getNameFrameX()*applicableScale);
 									sound.setNameFrameY(hiddenAreaSize+sound.getNameFrameY()*applicableScale);
 									sound.setImageX(sound.getImageX()*applicableScale);
 									sound.setImageY(hiddenAreaSize+sound.getImageY()*applicableScale);
 								} else {
 									Log.w(TAG, "sound: " + sound.getName());
 									Log.w(TAG, "hiddenAreaSize: " + hiddenAreaSize + " sound.getNameFrameX(): " + sound.getNameFrameX() + " applicableScale: " + applicableScale);
 									Log.w(TAG, "hiddenAreaSize+sound.getNameFrameX()*applicableScale: " + (hiddenAreaSize+sound.getNameFrameX()*applicableScale));
 									sound.setNameFrameX(hiddenAreaSize+sound.getNameFrameX()*applicableScale);
 									sound.setNameFrameY(sound.getNameFrameY()*applicableScale);
 									sound.setImageX(hiddenAreaSize+sound.getImageX()*applicableScale);
 									sound.setImageY(sound.getImageY()*applicableScale);
 								}
 
 								sound.setImageWidth(sound.getImageWidth()*applicableScale);
 								sound.setImageHeight(sound.getImageHeight()*applicableScale);
 
 								if (sound.getLinkNameAndImage()) sound.generateNameFrameXYFromImageLocation();
 							}
 
 							GraphicalSound blackBar1 = new GraphicalSound();
 							blackBar1.setNameFrameInnerColor(255, 0, 0, 0);
 
 							GraphicalSound blackBar2 = new GraphicalSound();
 							blackBar2.setNameFrameInnerColor(255, 0, 0, 0);
 
 							if (xFillsDisplay) {
 								blackBar1.setName("I hide top of the board.");
 								blackBar2.setName("I hide bottom of the board.");
 								blackBar1.setPath(new File(SoundboardMenu.mTopBlackBarSoundFilePath));
 								blackBar2.setPath(new File(SoundboardMenu.mBottomBlackBarSoundFilePath));
 								blackBar1.setNameFrameY(hiddenAreaSize);
 								blackBar2.setNameFrameY((float) windowHeight-hiddenAreaSize);
 							} else {
 								blackBar1.setName("I hide left side of the board.");
 								blackBar2.setName("I hide right side of the board.");
 								blackBar1.setPath(new File(SoundboardMenu.mLeftBlackBarSoundFilePath));
 								blackBar2.setPath(new File(SoundboardMenu.mRightBlackBarSoundFilePath));
 								blackBar1.setNameFrameX(hiddenAreaSize);
 								blackBar2.setNameFrameX((float) windowWidth-hiddenAreaSize);
 							}
 
 							mGsb.addSound(blackBar1);
 							mGsb.addSound(blackBar2);
 
 							mGsb.setScreenWidth(windowWidth);
 							mGsb.setScreenHeight(windowHeight);
 						}
 					});
 
 					builder.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							mGsb.setScreenWidth(windowWidth);
 							mGsb.setScreenHeight(windowHeight);
 						}
 					});
 					
 					mResolutionAlert = builder.create();
 					mResolutionAlert.setOnDismissListener(new OnDismissListener() {
 						public void onDismiss(DialogInterface dialog) {
 							mResolutionAlert = null;
 							mBoardHistory.createHistoryCheckpoint(mGsb);
 						}
 					});
 
 					mResolutionAlert.show();
 				}
 				Looper.loop();
 				Looper.myLooper().quit();
 			}
 		};
 		t.start();
 	}
 	
 	class DrawingPanel extends SurfaceView implements SurfaceHolder.Callback {
 		
 		public DrawingPanel(Context context) {
 			super(context);
             getHolder().addCallback(this);
             mThread = new DrawingThread(getHolder(), this);
 		}
 		
 		@Override
 		public boolean onTouchEvent(MotionEvent event) {
 			if (mThread == null) return false;
 			synchronized (mThread.getSurfaceHolder()) {
 				if(event.getAction() == MotionEvent.ACTION_DOWN){
 					
 					if (mMoveBackground) {
 						mBackgroundLeftDistance = event.getX() - mGsb.getBackgroundX();
 						mBackgroundTopDistance = event.getY() - mGsb.getBackgroundY();
 					} else {
 						ListIterator<GraphicalSound> iterator = mGsb.getSoundList().listIterator();
 						while (iterator.hasNext()) {iterator.next();}
 						while (iterator.hasPrevious()) {
 							GraphicalSound sound = iterator.previous();
 							String soundPath = sound.getPath().getAbsolutePath();
 							SoundNameDrawing soundNameDrawing = new SoundNameDrawing(sound);
 							float nameFrameX = sound.getNameFrameX();
 							float nameFrameY = sound.getNameFrameY();
 							float nameFrameWidth = soundNameDrawing.getNameFrameRect().width();
 							float nameFrameHeight = soundNameDrawing.getNameFrameRect().height();
 							if (event.getX() >= sound.getImageX() && 
 									event.getX() <= sound.getImageWidth() + sound.getImageX() &&
 									event.getY() >= sound.getImageY() &&
 									event.getY() <= sound.getImageHeight() + sound.getImageY())  {
 								if (mCopyColor != 0) {
 									copyColor(sound);
 								} else {
 									mDragTarget = DRAG_IMAGE;
 									initializeDrag(event, sound);
 								}
 								break;
 							} else if ((event.getX() >= sound.getNameFrameX() && 
 									event.getX() <= nameFrameWidth + sound.getNameFrameX() &&
 									event.getY() >= sound.getNameFrameY() && 
 									event.getY() <= nameFrameHeight + sound.getNameFrameY()) ||
 									
 									soundPath.equals(SoundboardMenu.mTopBlackBarSoundFilePath) && event.getY() <= nameFrameY ||
 									soundPath.equals(SoundboardMenu.mBottomBlackBarSoundFilePath) && event.getY() >= nameFrameY ||
 									soundPath.equals(SoundboardMenu.mLeftBlackBarSoundFilePath) && event.getX() <= nameFrameX ||
 									soundPath.equals(SoundboardMenu.mRightBlackBarSoundFilePath) && event.getX() >= nameFrameX) {
 								if (mCopyColor != 0) {
 									copyColor(sound);
 								} else {
 									mDragTarget = DRAG_TEXT;
 									initializeDrag(event, sound);
 								}
 								break;
 							}
 						}
 						
 						mClickTime = Calendar.getInstance().getTimeInMillis();
 					}
 						
 				} else if(event.getAction() == MotionEvent.ACTION_MOVE){
 					if (mMoveBackground) {
 						mGsb.setBackgroundX(event.getX() - mBackgroundLeftDistance);
 						mGsb.setBackgroundY(event.getY() - mBackgroundTopDistance);
 					} else if (mDrawDragSound == true) {
 						if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_TEXT) {
 							mDragSound.setNameFrameX(event.getX()-mNameFrameDragDistanceX);
 							mDragSound.setNameFrameY(event.getY()-mNameFrameDragDistanceY);
 						}
 						if (mDragSound.getLinkNameAndImage() || mDragTarget == DRAG_IMAGE) {
 							mDragSound.setImageX(event.getX() - mImageDragDistanceX);
 							mDragSound.setImageY(event.getY() - mImageDragDistanceY);
 						}
 					}
 					
 				} else if(event.getAction() == MotionEvent.ACTION_UP){
 					if (mMoveBackground) {
 						mGsb.setBackgroundX(event.getX() - mBackgroundLeftDistance);
 						mGsb.setBackgroundY(event.getY() - mBackgroundTopDistance);
 						mBoardHistory.createHistoryCheckpoint(mGsb);
 					} else if (mDrawDragSound == true && Calendar.getInstance().getTimeInMillis()-mClickTime < 200) {
 						mClickTime = 0;
 						mDragSound.setNameFrameX(mInitialNameFrameX);
 			  			mDragSound.setNameFrameY(mInitialNameFrameY);
 			  			mDragSound.setImageX(mInitialImageX);
 			  			mDragSound.setImageY(mInitialImageY);
 			  			mGsb.getSoundList().add(mDragSound);
 			  			mDrawDragSound = false;
 			  			invalidate();
 						
 						final CharSequence[] items = {"Info", "Name settings", "Image settings", "Sound settings",
 								"Copy sound", "Remove sound", "Set as..."};
 
 				    	AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(BoardEditor.this);
 				    	optionsBuilder.setTitle("Options");
 				    	optionsBuilder.setItems(items, new DialogInterface.OnClickListener() {
 				    	    public void onClick(DialogInterface dialog, int item) {
 				    	    	
 				    	    	if (item == 0) {
 				    	    		SoundNameDrawing soundNameDrawing = new SoundNameDrawing(mDragSound);
 				    	    		AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
 				    	    		builder.setTitle("Sound info");
 				                	builder.setMessage("Name:\n"+mDragSound.getName()+
 				                					"\n\nSound path:\n"+mDragSound.getPath()+
 				                					"\n\nImage path:\n"+mDragSound.getImagePath()+
 				                					"\n\nActive image path:\n"+mDragSound.getActiveImagePath()+
 				                					"\n\nName and image linked:\n"+mDragSound.getLinkNameAndImage()+
 				                					"\n\nHide image or text:\n"+mDragSound.getHideImageOrText()+
 				                					"\n\nImage X:\n"+mDragSound.getImageX()+
 				                					"\n\nImage Y:\n"+mDragSound.getImageY()+
 				                					"\n\nImage width:\n"+mDragSound.getImageWidth()+
 				                					"\n\nImage height:\n"+mDragSound.getImageHeight()+
 				                					"\n\nName frame X:\n"+mDragSound.getNameFrameX()+
 				                					"\n\nName frame Y:\n"+mDragSound.getNameFrameY()+
 				                					"\n\nName frame width:\n"+soundNameDrawing.getNameFrameRect().width()+
 				                					"\n\nName frame height:\n"+soundNameDrawing.getNameFrameRect().height()+
 				                					"\n\nAuto arrange column:\n"+mDragSound.getAutoArrangeColumn()+
 				                					"\n\nAuto arrange row:\n"+mDragSound.getAutoArrangeRow()+
 				                					"\n\nSecond click action:\n"+mDragSound.getSecondClickAction()+
 				                					"\n\nLeft volume:\n"+mDragSound.getVolumeLeft()+
 				                					"\n\nRight volume:\n"+mDragSound.getVolumeRight()+
 				                					"\n\nName frame border color:\n"+mDragSound.getNameFrameBorderColor()+
 				                					"\n\nName frame inner color:\n"+mDragSound.getNameFrameInnerColor()+
 				                					"\n\nName text color:\n"+mDragSound.getNameTextColor()+
 				                					"\n\nName text size:\n"+mDragSound.getNameSize()+
 				                					"\n\nShow name frame border paint:\n"+mDragSound.getShowNameFrameBorderPaint()+
 				                					"\n\nShow name frame inner paint:\n"+mDragSound.getShowNameFrameBorderPaint());
 				                					
 				                	builder.show();
 				    	    	} else if (item == 1) {
 				    	    		
 					    	    	LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
 				    	    			getSystemService(LAYOUT_INFLATER_SERVICE);
 				                	View layout = inflater.inflate(
 				                			R.layout.graphical_soundboard_editor_alert_sound_name_settings, 
 				                			(ViewGroup) findViewById(R.id.alert_settings_root));
 				                	
 				                	final EditText soundNameInput = 
 				              	  		(EditText) layout.findViewById(R.id.soundNameInput);
 				              	  	soundNameInput.setText(mDragSound.getName());
 				              	  	
 				              	  	final EditText soundNameSizeInput = 
 				              	  		(EditText) layout.findViewById(R.id.soundNameSizeInput);
 				              	  	soundNameSizeInput.setText(Float.toString(mDragSound.getNameSize()));
 				              	  	
 				              	  	final CheckBox checkShowSoundName = 
 				              	  		(CheckBox) layout.findViewById(R.id.showSoundNameCheckBox);
 				              	  	checkShowSoundName.setChecked(mDragSound.getHideImageOrText() != GraphicalSound.HIDE_TEXT);
 				              	  	
 				              	  	final CheckBox checkShowInnerPaint = 
 				              	  		(CheckBox) layout.findViewById(R.id.showInnerPaintCheckBox);
 				              	  	checkShowInnerPaint.setChecked(mDragSound.getShowNameFrameInnerPaint());
 				              	  	
 				              	  	final CheckBox checkShowBorderPaint = 
 				              	  		(CheckBox) layout.findViewById(R.id.showBorderPaintCheckBox);
 				              	  	checkShowBorderPaint.setChecked(mDragSound.getShowNameFrameBorderPaint());
 				              	  	
 				              	  	final Button nameColorButton = 
 				              	  		(Button) layout.findViewById(R.id.nameColorButton);
 				              	  	nameColorButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						mDragSound.setName(soundNameInput.getText().toString());
 				    			  			mDragSound.generateImageXYFromNameFrameLocation();
 				    			  			
 				    			  			Intent i = new Intent(BoardEditor.this, ColorChanger.class);
 				    		        		i.putExtra("parentKey", "changeNameColor");
 				    		        		i.putExtras(XStreamUtil.getSoundBundle(mDragSound, mGsb));
 				    		            	startActivityForResult(i, CHANGE_NAME_COLOR);
 				    					}
 				              	  	});
 				              	  	
 				              	  	final Button innerPaintColorButton = 
 				              	  		(Button) layout.findViewById(R.id.innerPaintColorButton);
 				              	  	innerPaintColorButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						mDragSound.setName(soundNameInput.getText().toString());
 				    			  			mDragSound.generateImageXYFromNameFrameLocation();
 				    			  			
 				    			  			Intent i = new Intent(BoardEditor.this, ColorChanger.class);
 				    		        		i.putExtra("parentKey", "changeinnerPaintColor");
 				    		        		i.putExtras(XStreamUtil.getSoundBundle(mDragSound, mGsb));
 				    		            	startActivityForResult(i, CHANGE_INNER_PAINT_COLOR);
 				    					}
 				              	  	});
 				              	  	
 				              	  	final Button borderPaintColorButton = 
 				              	  		(Button) layout.findViewById(R.id.borderPaintColorButton);
 				              	  	borderPaintColorButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						mDragSound.setName(soundNameInput.getText().toString());
 				    			  			mDragSound.generateImageXYFromNameFrameLocation();
 				    			  			
 				    			  			Intent i = new Intent(BoardEditor.this, ColorChanger.class);
 				    		        		i.putExtra("parentKey", "changeBorderPaintColor");
 				    		        		i.putExtras(XStreamUtil.getSoundBundle(mDragSound, mGsb));
 				    		            	startActivityForResult(i, CHANGE_BORDER_PAINT_COLOR);
 				    					}
 				              	  	});
 				              	  	
 				              	  	AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
 				              	  	builder.setView(layout);
 				              	  	builder.setTitle("Name settings");
 				          	  	
 				    	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				    	          		public void onClick(DialogInterface dialog, int whichButton) {
 				    	          			boolean notifyIncorrectValue = false;
 				    	          			
 				    	          			if (checkShowSoundName.isChecked() == false) {
 				    	          				mDragSound.setHideImageOrText(GraphicalSound.HIDE_TEXT);
 				    	          			} else if (checkShowSoundName.isChecked() && 
 				    	          				mDragSound.getHideImageOrText() == GraphicalSound.HIDE_TEXT) {
 				    	          				mDragSound.setHideImageOrText(GraphicalSound.SHOW_ALL);
 				    	          				mDragSound.generateImageXYFromNameFrameLocation();
 				    	          			}
 				    	          			mDragSound.setShowNameFrameInnerPaint(checkShowInnerPaint.isChecked());
 				    	          			mDragSound.setShowNameFrameBorderPaint(checkShowBorderPaint.isChecked());
 				    	          			
 				    	          			mDragSound.setName(soundNameInput.getText().toString());
 				    			  			
 				    			  			try {
 				    			  				mDragSound.setNameSize(Float.valueOf(
 				    			  						soundNameSizeInput.getText().toString()).floatValue());
 				    	          			} catch(NumberFormatException nfe) {
 				    	          				notifyIncorrectValue = true;
 				    	          			}
 				    	          			
 				    	          			if (mDragSound.getLinkNameAndImage()) {
 				    			  				mDragSound.generateImageXYFromNameFrameLocation();
 				    			  			}
 				    	          			
 				    	          			if (notifyIncorrectValue == true) {
 				    	          				Toast.makeText(getApplicationContext(), "Incorrect value", 
 				    	          						Toast.LENGTH_SHORT).show();
 				    	          			}
 				    	          			
 				    	          			mBoardHistory.createHistoryCheckpoint(mGsb);
 				    	          		}
 				    	          	});
 	
 				    	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				    		          	public void onClick(DialogInterface dialog, int whichButton) {
 				    	          	    }
 				    	          	});
 				    	          	
 				    	          	builder.show();
 		    						
 				    	    	} else if (item == 2) {
 				    	    		
 				    	    		LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
 				    	    			getSystemService(LAYOUT_INFLATER_SERVICE);
 				                	View layout = inflater.inflate(
 				                			R.layout.graphical_soundboard_editor_alert_sound_image_settings, 
 				                			(ViewGroup) findViewById(R.id.alert_settings_root));
 
 				              	  	
 				              	  	final CheckBox checkShowSoundImage = 
 				              	  		(CheckBox) layout.findViewById(R.id.showSoundImageCheckBox);
 				              	  	checkShowSoundImage.setChecked(mDragSound.getHideImageOrText() != GraphicalSound.HIDE_IMAGE);
 				              	  	
 				              	  	mSoundImageWidthText = (TextView) layout.findViewById(R.id.soundImageWidthText);
 				              	  	mSoundImageWidthText.setText("Width (" + mDragSound.getImage().getWidth() + ")");
 				            	  	
 				            	  	mSoundImageHeightText = (TextView) layout.findViewById(R.id.soundImageHeightText);
 				            	  	mSoundImageHeightText.setText("Height (" + mDragSound.getImage().getHeight() + ")");
 				              	  	
 				              	  	mSoundImageWidthInput = (EditText) layout.findViewById(R.id.soundImageWidthInput);
 				              	  	mSoundImageWidthInput.setText(Float.toString(mDragSound.getImageWidth()));  	
 				              	  	
 				              	  	mSoundImageHeightInput = (EditText) layout.findViewById(R.id.soundImageHeightInput);
 				              	  	mSoundImageHeightInput.setText(Float.toString(mDragSound.getImageHeight()));
 				              	  	
 
 				              	  	final CheckBox scaleWidthHeight = 
 				              	  			(CheckBox) layout.findViewById(R.id.scaleWidthHeightCheckBox);
 				              	  	scaleWidthHeight.setChecked(true);
 
 				              	  	scaleWidthHeight.setOnClickListener(new OnClickListener() {
 				              	  		public void onClick(View v) {
 				              	  			try {
 				              	  			// Calculate a new scale
 				              	  				mWidthHeightScale = Float.valueOf(mSoundImageWidthInput.getText().toString()).floatValue() 
 				              	  									/ Float.valueOf(mSoundImageHeightInput.getText().toString()).floatValue();
 				              	  			} catch(NumberFormatException nfe) {Log.e(TAG, "Unable to calculate width and height scale", nfe);}
 				              	  		}
 				              	  	});
 				              	  	mWidthHeightScale = mDragSound.getImageWidth() / mDragSound.getImageHeight();
 
 				              	  	mSoundImageWidthInput.setOnKeyListener(new OnKeyListener() {
 				              	  		public boolean onKey(View v, int keyCode, KeyEvent event) {
 				              	  			if (scaleWidthHeight.isChecked()) {
 				              	  				try {
 				              	  					float value = Float.valueOf(mSoundImageWidthInput.getText().toString()).floatValue();
 				              	  					mSoundImageHeightInput.setText(Float.valueOf(value/mWidthHeightScale).toString());
 				              	  				} catch(NumberFormatException nfe) {}
 				              	  			}
 				              	  			return false;
 				              	  		}
 				              	  	});
 				              	  	
 				              	  	mSoundImageHeightInput.setOnKeyListener(new OnKeyListener() {
 										public boolean onKey(View v, int keyCode, KeyEvent event) {
 											if (scaleWidthHeight.isChecked()) {
 												try {
 													float value = Float.valueOf(mSoundImageHeightInput.getText().toString()).floatValue();
 													mSoundImageWidthInput.setText(Float.valueOf(value*mWidthHeightScale).toString());
 												} catch(NumberFormatException nfe) {}
 											}
 											return false;
 										}
 				              	  	});
 				              	  	
 				              	  	final Button revertSizeButton = (Button) layout.findViewById(R.id.revertImageSizeButton);
 				              	  	revertSizeButton.setOnClickListener(new OnClickListener() {
 				              	  		public void onClick(View v) {
 				              	  			mSoundImageWidthInput.setText(Float.valueOf(mDragSound.getImageWidth()).toString());
 				              	  			mSoundImageHeightInput.setText(Float.valueOf(mDragSound.getImageHeight()).toString());
 				              	  			mWidthHeightScale = mDragSound.getImageWidth() / mDragSound.getImageHeight();
 				              	  		}
 				              	  	});
 				              	  	
 				              	  	final Button setSoundImageButton = (Button) layout.findViewById(R.id.setSoundImageButton);
 				              	  	setSoundImageButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						selectImageFile();
 				    					}
 				              	  	});
 				              	  	
 				              	  	final Button resetSoundImageButton = (Button) layout.findViewById(R.id.resetSoundImageButton);
 				              	  	resetSoundImageButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						Bitmap defaultSound = BitmapFactory.decodeResource(getResources(), R.drawable.sound);
 				    						String soundWidth = Integer.toString(defaultSound.getWidth());
 				    						String soundHeight = Integer.toString(defaultSound.getHeight());
 				    						mDragSound.setImage(defaultSound);
 				    						mDragSound.setImagePath(null);
 				    						mSoundImageWidthInput.setText(soundWidth);
 				              	  			mSoundImageHeightInput.setText(soundHeight);
 				              	  			mSoundImageWidthText.setText("Width (" + soundWidth + ")");
 				              	  			mSoundImageHeightText.setText("Height (" + soundHeight + ")");
 				    					}
 				              	  	});
 				              	  	
 				              	  	final Button setSoundActiveImageButton = (Button) layout.findViewById(R.id.setSoundActiveImageButton);
 				              	  	setSoundActiveImageButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						selectActiveImageFile();
 				    					}
 				              	  	});
 				              	  	
 				              	  	final Button resetSoundActiveImageButton = (Button) layout.findViewById(R.id.resetSoundActiveImageButton);
 				              	  	resetSoundActiveImageButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						mDragSound.setActiveImage(null);
 				    						mDragSound.setActiveImagePath(null);
 				    					}
 				              	  	});
 				              	  	
 				              	  	AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
 				              	  	builder.setView(layout);
 				              	  	builder.setTitle("Image settings");
 				          	  	
 				    	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				    	          		public void onClick(DialogInterface dialog, int whichButton) {
 				    	          			boolean notifyIncorrectValue = false;
 				    	          			
 				    	          			if (checkShowSoundImage.isChecked() == false) {
 				    	          				mDragSound.setHideImageOrText(GraphicalSound.HIDE_IMAGE);
 				    	          			} else if (checkShowSoundImage.isChecked() && 
 				    	          				mDragSound.getHideImageOrText() == GraphicalSound.HIDE_IMAGE) {
 				    	          				mDragSound.setHideImageOrText(GraphicalSound.SHOW_ALL);
 				    	          			}
 				    	          			
 				    	          			try {
 				    	          				mDragSound.setImageWidth(Float.valueOf(
 				    	          						mSoundImageWidthInput.getText().toString()).floatValue());
 				    	          				mDragSound.setImageHeight(Float.valueOf(
 				    	          						mSoundImageHeightInput.getText().toString()).floatValue());	
 				    	          			} catch(NumberFormatException nfe) {
 				    	          				notifyIncorrectValue = true;
 				    	          			}
 				    	          			mDragSound.generateImageXYFromNameFrameLocation();
 				    	          			
 				    	          			if (notifyIncorrectValue == true) {
 				    	          				Toast.makeText(getApplicationContext(), "Incorrect value", Toast.LENGTH_SHORT).show();
 				    	          			}
 				    	          			mBoardHistory.createHistoryCheckpoint(mGsb);
 				    	          		}
 				    	          	});
 
 				    	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				    		          	public void onClick(DialogInterface dialog, int whichButton) {
 				    	          	    }
 				    	          	});
 				    	          	
 				    	          	builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
 										@Override
 										public void onCancel(DialogInterface dialog) {
 											mSoundImageDialog = null;
 										}
 									});
 				    	          	
 				    	          	mSoundImageDialog = builder.create();
 				    	          	mSoundImageDialog.show();
 				    	    	} else if (item == 3) {
 				    	    		
 				    	    		LayoutInflater inflater = (LayoutInflater) BoardEditor.this.
 			    	    				getSystemService(LAYOUT_INFLATER_SERVICE);
 				    	    		View layout = inflater.inflate(
 			                			R.layout.graphical_soundboard_editor_alert_sound_settings, 
 			                			(ViewGroup) findViewById(R.id.alert_settings_root));
 				    	    		
 				    	    		final CheckBox linkNameAndImageCheckBox = 
 				              	  		(CheckBox) layout.findViewById(R.id.linkNameAndImageCheckBox);
 				    	    		linkNameAndImageCheckBox.setChecked(mDragSound.getLinkNameAndImage());
 				    	    		
 				    	    		final Button changeSoundPathButton = 
 				              	  		(Button) layout.findViewById(R.id.changeSoundPathButton);
 				    	    		changeSoundPathButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						Intent i = new Intent(BoardEditor.this, FileExplorer.class);
 				    						i.putExtra(FileExplorer.EXTRA_ACTION_KEY, FileExplorer.ACTION_CHANGE_SOUND_PATH);
 				    						i.putExtra(FileExplorer.EXTRA_BOARD_NAME_KEY, mBoardName);
 				    						startActivityForResult(i, CHANGE_SOUND_PATH);
 				    					}
 				              	  	});
 				    	    		
 				    	    		final Button secondClickActionButton = 
 				              	  		(Button) layout.findViewById(R.id.secondClickActionButton);
 				    	    		secondClickActionButton.setOnClickListener(new OnClickListener() {
 				    					public void onClick(View v) {
 				    						final CharSequence[] items = {"Play new", "Pause", "Stop"};
 						                	AlertDialog.Builder secondClickBuilder = new AlertDialog.Builder(
 						                			BoardEditor.this);
 						                	secondClickBuilder.setTitle("Action");
 						                	secondClickBuilder.setItems(items, new DialogInterface.OnClickListener() {
 						                	    public void onClick(DialogInterface dialog, int item) {
 						                	    	if (item == 0) {
 						                	    		mDragSound.setSecondClickAction(GraphicalSound.SECOND_CLICK_PLAY_NEW);
 						                	    	} else if (item == 1) {
 						                	    		mDragSound.setSecondClickAction(GraphicalSound.SECOND_CLICK_PAUSE);
 						                	    	} else if (item == 2) {
 						                	    		mDragSound.setSecondClickAction(GraphicalSound.SECOND_CLICK_STOP);
 						                	    	}
 						                	    }
 						                	});
 						                	AlertDialog secondClickAlert = secondClickBuilder.create();
 						                	secondClickAlert.show();
 				    					}
 				              	  	});
 				    	    		
 				    	    		final EditText leftVolumeInput = (EditText) layout.findViewById(R.id.leftVolumeInput);
 				            	  	leftVolumeInput.setText(Float.toString(mDragSound.getVolumeLeft()*100) + "%");
 				            	  	final EditText rightVolumeInput = (EditText) layout.findViewById(R.id.rightVolumeInput);
 				            	  	rightVolumeInput.setText(Float.toString(mDragSound.getVolumeRight()*100) + "%");
 				    	    		
 				    	    		AlertDialog.Builder builder = new AlertDialog.Builder(BoardEditor.this);
 				              	  	builder.setView(layout);
 				              	  	builder.setTitle("Sound settings");
 				          	  	
 				    	          	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				    	          		public void onClick(DialogInterface dialog, int whichButton) {
 				    	          			mDragSound.setLinkNameAndImage(linkNameAndImageCheckBox.isChecked());
 				    	          			if (mDragSound.getLinkNameAndImage()) {
 					    	          			mDragSound.generateImageXYFromNameFrameLocation();
 				    	          			}
 				    	          			
 				    	          			boolean notifyIncorrectValue = false;
 				    	          			Float leftVolumeValue = null;
 				    	          			try {
 				    	          				String leftVolumeString = leftVolumeInput.getText().toString();
 				    	          				if (leftVolumeString.contains("%")) {
 				    	          					leftVolumeValue = Float.valueOf(leftVolumeString.substring(0, 
 				    	          							leftVolumeString.indexOf("%"))).floatValue()/100;
 				    	          				} else {
 				    	          					leftVolumeValue = Float.valueOf(leftVolumeString).floatValue()/100;
 				    	          				}
 				    	          				
 				    	          				
 				    	          				if (leftVolumeValue >= 0 && leftVolumeValue <= 1 && leftVolumeValue != null) {
 				    	          					mDragSound.setVolumeLeft(leftVolumeValue);
 				    		          			} else {
 				    		          				notifyIncorrectValue = true;
 				    		          			}
 				    	          			} catch(NumberFormatException nfe) {
 				    	          				notifyIncorrectValue = true;
 				    	          			}
 				    	          			
 				    	          			Float rightVolumeValue = null;
 				    	          			try {
 				    	          				String rightVolumeString = rightVolumeInput.getText().toString();
 				    	          				if (rightVolumeString.contains("%")) {
 				    	          				rightVolumeValue = Float.valueOf(rightVolumeString.substring(0, 
 				    	          						rightVolumeString.indexOf("%"))).floatValue()/100;
 				    	          				} else {
 				    	          					rightVolumeValue = Float.valueOf(rightVolumeString).floatValue()/100;
 				    	          				}
 				    	          				
 				    	          				if (rightVolumeValue >= 0 && rightVolumeValue <= 1 && rightVolumeValue != null) {
 				    	          					mDragSound.setVolumeRight(rightVolumeValue);
 				    		          			} else {
 				    		          				notifyIncorrectValue = true;
 				    		          			}
 				    	          			} catch(NumberFormatException nfe) {
 				    	          				notifyIncorrectValue = true;
 				    	          			}
 				    	          			
 				    	          			if (notifyIncorrectValue == true) {
 				    	          				Toast.makeText(getApplicationContext(), "Incorrect value", Toast.LENGTH_SHORT).show();
 				    	          			}
 				    	          			mBoardHistory.createHistoryCheckpoint(mGsb);
 				    	          		}
 				    	          	});
 
 				    	          	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				    		          	public void onClick(DialogInterface dialog, int whichButton) {
 				    	          	    }
 				    	          	});
 				    	          	
 				    	          	builder.show();
 				    	    	} else if (item == 4) {
 				    	    		SoundboardMenu.mCopiedSound = (GraphicalSound) mDragSound.clone();
 				    	    		
 				    	    	} else if (item == 5) {
 				                	
 				                	LayoutInflater removeInflater = (LayoutInflater) 
 				                			BoardEditor.this.getSystemService(LAYOUT_INFLATER_SERVICE);
 				                	View removeLayout = removeInflater.inflate(
 				                			R.layout.graphical_soundboard_editor_alert_remove_sound,
 				                	        (ViewGroup) findViewById(R.id.alert_remove_sound_root));
 				              	  	
 				              	  	final CheckBox removeFileCheckBox = 
 				              	  		(CheckBox) removeLayout.findViewById(R.id.removeFile);
 				              	  	removeFileCheckBox.setText(" DELETE " + mDragSound.getPath().getAbsolutePath());
 				              	  	
 				              	  	AlertDialog.Builder removeBuilder = new AlertDialog.Builder(
 				              	  		BoardEditor.this);
 				              	  	removeBuilder.setView(removeLayout);
 				              	  	removeBuilder.setTitle("Removing " + mDragSound.getName());
 				          	  	
 				              	  	removeBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				    	          	  	public void onClick(DialogInterface dialog, int whichButton) {
 				    	          	  		if (removeFileCheckBox.isChecked() == true) {
 				    	          	  			mDragSound.getPath().delete();
 				    	          	  		}
 				    	          	  		mGsb.getSoundList().remove(mDragSound);
 				    	          	  		mBoardHistory.createHistoryCheckpoint(mGsb);
 				    	          	    }
 				    	          	});
 
 				              	  	removeBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				    		          	public void onClick(DialogInterface dialog, int whichButton) {
 				    	          	    }
 				    	          	});
 				    	          	
 				              	  	removeBuilder.show();
 				    	    	} else if (item == 6) {
 				    	    		final CharSequence[] items = {"Ringtone", "Notification", "Alerts"};
 
 				                	AlertDialog.Builder setAsBuilder = new AlertDialog.Builder(
 				                			BoardEditor.this);
 				                	setAsBuilder.setTitle("Set as...");
 				                	setAsBuilder.setItems(items, new DialogInterface.OnClickListener() {
 				                	    public void onClick(DialogInterface dialog, int item) {
 				                	    	String filePath = mDragSound.getPath().getAbsolutePath();
 
 				                	    	ContentValues values = new ContentValues();
 				                	    	values.put(MediaStore.MediaColumns.DATA, filePath);
 				                        	values.put(MediaStore.MediaColumns.TITLE, mDragSound.getName());
 				                        	
 				                        	values.put(MediaStore.MediaColumns.MIME_TYPE, MimeTypeMap.getSingleton().getMimeTypeFromExtension(
 				                        			filePath.substring(filePath.lastIndexOf('.'+1))));
 				                        	values.put(MediaStore.Audio.Media.ARTIST, "Artist");
 				                	    	
 				                	    	int selectedAction = 0;
 				                	    	if (item == 0) {
 				                	    		selectedAction = RingtoneManager.TYPE_RINGTONE;
 				                	    		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
 				                            	values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
 				                            	values.put(MediaStore.Audio.Media.IS_ALARM, false);
 				                            	values.put(MediaStore.Audio.Media.IS_MUSIC, false);
 				                	    	} else if (item == 1) {
 				                	    		selectedAction = RingtoneManager.TYPE_NOTIFICATION;
 				                	    		values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
 				                            	values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
 				                            	values.put(MediaStore.Audio.Media.IS_ALARM, false);
 				                            	values.put(MediaStore.Audio.Media.IS_MUSIC, false);
 				                	    	} else if (item == 2) {
 				                	    		selectedAction = RingtoneManager.TYPE_ALARM;
 				                	    		values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
 				                            	values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
 				                            	values.put(MediaStore.Audio.Media.IS_ALARM, true);
 				                            	values.put(MediaStore.Audio.Media.IS_MUSIC, false);
 				                	    	}
 				                        	
 				                        	Uri uri = MediaStore.Audio.Media.getContentUriForPath(filePath);
 				                        	getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + filePath + "\"", null);
 				                        	Uri newUri = BoardEditor.this.getContentResolver().insert(uri, values);
 				                        	
 				                        	RingtoneManager.setActualDefaultRingtoneUri(BoardEditor.this, selectedAction, newUri);
 				                        	
 				                	    }
 				                	});
 				                	AlertDialog setAsAlert = setAsBuilder.create();
 				                	setAsAlert.show();
 				    	    	}
 				            	
 				    	    }
 				    	});
 				    	AlertDialog optionsAlert = optionsBuilder.create();
 				    	optionsAlert.show();
 				    	
 				    	invalidate();
 					} else if (mDrawDragSound == true) {
 						if (mGsb.getAutoArrange()) {
 							
 							int width = mPanel.getWidth();
 							int height = mPanel.getHeight();
       						
       						int column = -1, i = 0;
       						while (column == -1) {
       							if (event.getX() >= i*(width/mGsb.getAutoArrangeColumns()) && event.getX() <= (i+1)*(width/(mGsb.getAutoArrangeColumns()))) {
       								column = i;
         						}
       							if (i > 1000) {
       								Log.e(TAG, "column fail");
       								mDragSound.getAutoArrangeColumn();
       								break;
       							}
       							i++;
       						}
       						i = 0;
       						int row = -1;
       						while (row == -1) {
       							if (event.getY() >= i*(height/mGsb.getAutoArrangeRows()) && event.getY() <= (i+1)*(height/(mGsb.getAutoArrangeRows()))) {
       								row = i;
         						}
       							if (i > 1000) {
       								Log.e(TAG, "row fail");
       								mDragSound.getAutoArrangeRow();
       								break;
       							}
       							i++;
       						}
       						
       						GraphicalSound swapSound = null;
       						for (GraphicalSound sound : mGsb.getSoundList()) {
       							if (sound.getAutoArrangeColumn() == column && sound.getAutoArrangeRow() == row) {
       								swapSound = sound;
       							}
       						}
       						
       						if (column == mDragSound.getAutoArrangeColumn() && row == mDragSound.getAutoArrangeRow()) {
       							moveSound(event.getX(), event.getY());
       						} else {
       							try {
       								moveSoundToSlot(swapSound, mDragSound.getAutoArrangeColumn(), mDragSound.getAutoArrangeRow(), 
       										swapSound.getImageX(), swapSound.getImageY(), swapSound.getNameFrameX(), swapSound.getNameFrameY());
       							} catch (NullPointerException e) {}
       							moveSoundToSlot(mDragSound, column, row, mInitialImageX, mInitialImageY, mInitialNameFrameX, mInitialNameFrameY);
       							mGsb.addSound(mDragSound);
       							mDrawDragSound = false;
       						}
   							
 						} else {
 							moveSound(event.getX(), event.getY());
 						}
 						mBoardHistory.createHistoryCheckpoint(mGsb);
 						
 					}
 				}
 				
 				return true;
 			}
 		}
 		
 		@Override
         public void onDraw(Canvas canvas) {
 			if (canvas == null) {
 				Log.w(TAG, "Got null canvas");
 				mNullCanvasCount++;
 				
 				// Drawing thread is still running while the activity is destroyed (surfaceCreated was probably called after surfaceDestroyed).
 				// Reproduce by killing the editor immediately after it is created.
 				// It's difficult to kill the thread properly while supporting different orientations and closing of screen.
 				if (mNullCanvasCount > 5) {
 			    	Log.e(TAG, "Drawing thread was not destroyed properly");
 			    	mThread.setRunning(false);
 			    	mThread = null;
 				}
 			} else {
 				mNullCanvasCount = 0;
 				super.dispatchDraw(canvas);
 				
 				canvas.drawColor(mGsb.getBackgroundColor());
 				
 				if (mGsb.getUseBackgroundImage() == true && mGsb.getBackgroundImagePath().exists()) {
 					RectF bitmapRect = new RectF();
 					bitmapRect.set(mGsb.getBackgroundX(), mGsb.getBackgroundY(), 
 							mGsb.getBackgroundWidth() + mGsb.getBackgroundX(), mGsb.getBackgroundHeight() + mGsb.getBackgroundY());
 					
 					Paint bgImage = new Paint();
 					bgImage.setColor(mGsb.getBackgroundColor());
 					
 					try {
 						canvas.drawBitmap(mGsb.getBackgroundImage(), null, bitmapRect, bgImage);
 					} catch(NullPointerException npe) {
 						Log.e(TAG, "Unable to draw image " + mGsb.getBackgroundImagePath().getAbsolutePath());
 						mGsb.setBackgroundImage(BitmapFactory.decodeResource(getResources(), R.drawable.sound));
 					}
 				}
 				
 				try {
 					ArrayList<GraphicalSound> drawList = new ArrayList<GraphicalSound>();
 					drawList.addAll(mGsb.getSoundList());
 					if (mDrawDragSound) drawList.add(mDragSound);
 					
 					for (GraphicalSound sound : drawList) {
 						Paint barPaint = new Paint();
 						barPaint.setColor(sound.getNameFrameInnerColor());
 						String soundPath = sound.getPath().getAbsolutePath();
 						if (soundPath.equals(SoundboardMenu.mTopBlackBarSoundFilePath)) {
 							canvas.drawRect(0, 0, canvas.getWidth(), sound.getNameFrameY(), barPaint);
 						} else if (soundPath.equals(SoundboardMenu.mBottomBlackBarSoundFilePath)) {
 							canvas.drawRect(0, sound.getNameFrameY(), canvas.getWidth(), canvas.getHeight(), barPaint);
 						} else if (soundPath.equals(SoundboardMenu.mLeftBlackBarSoundFilePath)) {
 							canvas.drawRect(0, 0, sound.getNameFrameX(), canvas.getHeight(), barPaint);
 						} else if (soundPath.equals(SoundboardMenu.mRightBlackBarSoundFilePath)) {
 							canvas.drawRect(sound.getNameFrameX(), 0, canvas.getWidth(), canvas.getHeight(), barPaint);
 						} else {
 							if (sound.getHideImageOrText() != GraphicalSound.HIDE_TEXT) {
 								float NAME_DRAWING_SCALE = SoundNameDrawing.NAME_DRAWING_SCALE;
 								
 								
 								canvas.scale(1/NAME_DRAWING_SCALE, 1/NAME_DRAWING_SCALE);
 								SoundNameDrawing soundNameDrawing = new SoundNameDrawing(sound);
 								
 								Paint nameTextPaint = soundNameDrawing.getBigCanvasNameTextPaint();
 								Paint borderPaint = soundNameDrawing.getBorderPaint();
 								Paint innerPaint = soundNameDrawing.getInnerPaint();
 								
 								RectF bigCanvasNameFrameRect = soundNameDrawing.getBigCanvasNameFrameRect();
 								
 								if (sound.getShowNameFrameInnerPaint() == true) {
 							    	canvas.drawRoundRect(bigCanvasNameFrameRect, 2*NAME_DRAWING_SCALE, 2*NAME_DRAWING_SCALE, innerPaint);
 							    }
 								
 								if (sound.getShowNameFrameBorderPaint()) {
 									canvas.drawRoundRect(bigCanvasNameFrameRect, 2*NAME_DRAWING_SCALE, 2*NAME_DRAWING_SCALE, borderPaint);
 								}
 							    
 								int i = 0;
 							    for (String row : sound.getName().split("\n")) {
 						    		canvas.drawText(row, (sound.getNameFrameX()+2)*NAME_DRAWING_SCALE, 
 						    				sound.getNameFrameY()*NAME_DRAWING_SCALE+(i+1)*sound.getNameSize()*NAME_DRAWING_SCALE, nameTextPaint);
 						    		i++;
 							    }
 							    canvas.scale(NAME_DRAWING_SCALE, NAME_DRAWING_SCALE);
 							}
 						    
 						    if (sound.getHideImageOrText() != GraphicalSound.HIDE_IMAGE) {
 							    RectF imageRect = new RectF();
 							    imageRect.set(sound.getImageX(), 
 										sound.getImageY(), 
 										sound.getImageWidth() + sound.getImageX(), 
 										sound.getImageHeight() + sound.getImageY());
 								
 							    try {
 							    	if (SoundPlayerControl.isPlaying(sound.getPath()) && sound.getActiveImage() != null) {
 							    		try {
 							    			canvas.drawBitmap(sound.getActiveImage(), null, imageRect, mSoundImagePaint);
 							    		} catch(NullPointerException npe) {
 							    			Log.e(TAG, "Unable to draw active image for sound " + sound.getName());
 											sound.setActiveImage(null);
 							    			canvas.drawBitmap(sound.getImage(), null, imageRect, mSoundImagePaint);
 							    		}
 							    		
 							    	} else {
 							    		canvas.drawBitmap(sound.getImage(), null, imageRect, mSoundImagePaint);
 							    	}
 								} catch(NullPointerException npe) {
 									Log.e(TAG, "Unable to draw image for sound " + sound.getName());
 									BugSenseHandler.log(TAG, npe);
 									sound.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.sound));
 								}
 						    }
 						    
 						    if (mGsb.getAutoArrange() && sound == mDragSound) {
 						    	int width = mPanel.getWidth();
 								int height = mPanel.getHeight();
 								
 								Paint linePaint = new Paint();
 								Paint outerLinePaint = new Paint(); {
 								linePaint.setColor(Color.WHITE);
 								outerLinePaint.setColor(Color.YELLOW);
 								outerLinePaint.setStrokeWidth(3);
 								}
 								
 						    	for (int i = 1; i < mGsb.getAutoArrangeColumns(); i++) {
 						    		float X = i*(width/mGsb.getAutoArrangeColumns());
 						    		canvas.drawLine(X, 0, X, height, outerLinePaint);
 						    		canvas.drawLine(X, 0, X, height, linePaint);
 						    	}
 						    	for (int i = 1; i < mGsb.getAutoArrangeRows(); i++) {
 						    		float Y = i*(height/mGsb.getAutoArrangeRows());
 						    		canvas.drawLine(0, Y, width, Y, outerLinePaint);
 						    		canvas.drawLine(0, Y, width, Y, linePaint);
 						    	}
 						    }
 						}
 					}
 				} catch(ConcurrentModificationException cme) {
 					Log.w(TAG, "Sound list modification while iteration");
 				}
 			}
 
 		}
 		
 		public void surfaceChanged(SurfaceHolder holder, int format, int width,
 								   int height) {
 		}
 		
 		public void surfaceCreated(SurfaceHolder holder) {
 			try {
 				mThread.setRunning(true);
 				mThread.start();
 			} catch (NullPointerException e) {
 				mThread = new DrawingThread(getHolder(), this);
 				mThread.setRunning(true);
 				mThread.start();
 			}
 			Log.d(TAG, "Surface created");
 		}
 		
 		public void surfaceDestroyed(SurfaceHolder holder) {
             mThread.setRunning(false);
             mThread = null;
             Log.d(TAG, "Surface destroyed");
 		}
 		
 	}
 	
 	class DrawingThread extends Thread {
         private SurfaceHolder mSurfaceHolder;
         private boolean mRun = false;
 		
         public DrawingThread(SurfaceHolder surfaceHolder, DrawingPanel panel) {
             mSurfaceHolder = surfaceHolder;
             mPanel = panel;
         }
 		
         public void setRunning(boolean run) {
             mRun = run;
         }
 		
         public SurfaceHolder getSurfaceHolder() {
             return mSurfaceHolder;
         }
 		
         @Override
         public void run() {
             while (mRun) {
             	Canvas c;
                 c = null;
                 try {
                     c = mSurfaceHolder.lockCanvas(null);
                     synchronized (mSurfaceHolder) {
                         mPanel.onDraw(c);
                     }
                 } finally {
                     if (c != null) {
                         mSurfaceHolder.unlockCanvasAndPost(c);
                     }
                 }
                 try {
             		if (mMode == EDIT_BOARD && (mDrawDragSound || mMoveBackground )) {
             			Thread.sleep(10);
             		} else if (mMode == EDIT_BOARD && mDrawDragSound == false && mMoveBackground == false) {
             			
             			for (int i = 0; i <= 5; i++) {
             				Thread.sleep(100);
             				if (mDrawDragSound || mMoveBackground) {
             					break;
             				}
             			}
             		} else if (mMode == LISTEN_BOARD) {
             			for (int i = 0; i <= 30; i++) {
             				Thread.sleep(20);
             				if (mMode == EDIT_BOARD || mCanvasInvalidated == true) {
             					mCanvasInvalidated = false;
             					break;
             				}
             			}
             		} else {
             			Log.e(TAG, "Undefined redraw rate state");
             			Thread.sleep(1000);
             		}
 				} catch (InterruptedException e) {}
             }
         }
     }
 
 }
