 package com.example.roboarmapp;
 
 import android.R.drawable;
 import android.app.Activity;
 import android.content.res.ColorStateList;
 import android.graphics.Color;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageView;
 import android.widget.Switch;
 
 public class LockAxis extends Activity {
 	
 	private MediaPlayer mp = null;
 	private CheckBox xBox;
 	private CheckBox yBox;
 	private CheckBox zBox;
 	private CheckBox rollBox;
 	private CheckBox pitchBox;
 	private ImageView rollImg;
 	private ImageView pitchImg;
 	private Switch mySwitch;
 	private ImageView axis;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_lock_axis);
 		// Show the Up button in the action bar.
 		//getActionBar().setDisplayHomeAsUpEnabled(true);
 		xBox = (CheckBox)findViewById(R.id.lockXAxis);
 		yBox = (CheckBox)findViewById(R.id.lockYAxis);
 		zBox = (CheckBox)findViewById(R.id.lockZAxis);
 		rollBox = (CheckBox)findViewById(R.id.lockRoll);
 		pitchBox = (CheckBox)findViewById(R.id.lockPitch);
 		
 		axis = (ImageView)findViewById(R.id.axis);
 		
 		mySwitch = (Switch)findViewById(R.id.lock_switch);
 		if (MainActivity.armMode) {
 			mySwitch.setChecked(false);
 		} else {
 			mySwitch.setChecked(true);
 		}
 		mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			
 		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		        if (isChecked) { // wrist mode
 		        	if (MainActivity.doSound) {
 		        		if (mp != null) {
 		        			mp.release();
 		        		}
 						mp = MediaPlayer.create(getApplicationContext(), R.raw.wrist_mode);
 						mp.start();
 					}
 		        	setWristMode();
 		        } else { // arm mode
 		        	if (MainActivity.doSound) {
 		        		if (mp != null) {
 		        			mp.release();
 		        		}
 						mp = MediaPlayer.create(getApplicationContext(), R.raw.arm_mode);
 						mp.start();
 					}
 		        	setArmMode();
 		        }
 		    }
 		});
 		rollImg = (ImageView)findViewById(R.id.roll);
 		pitchImg = (ImageView)findViewById(R.id.pitch);
 		
 		if (MainActivity.xBoxChecked) {
 			xBox.setChecked(true);
 		} else {
 			xBox.setChecked(false);
 		}
 		if (MainActivity.yBoxChecked) {
 			yBox.setChecked(true);
 		} else {
 			yBox.setChecked(false);
 		}
 		if (MainActivity.zBoxChecked) {
 			zBox.setChecked(true);
 		} else {
 			zBox.setChecked(false);
 		}
 		if (MainActivity.rollBoxChecked) {
 			rollBox.setChecked(true);
 		} else {
 			rollBox.setChecked(false);
 		}
 		if (MainActivity.pitchBoxChecked) {
 			pitchBox.setChecked(true);
 		} else {
 			pitchBox.setChecked(false);
 		}
 		xBox.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (((CheckBox) v).isChecked()) {
 					MainActivity.xAxisLocked = true;
 					MainActivity.xBoxChecked = true;
 				} else {
 					MainActivity.xAxisLocked = false;
 					MainActivity.xBoxChecked = false;
 				}
 			}
 		});
 		yBox.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (((CheckBox) v).isChecked()) {
 					MainActivity.yAxisLocked = true;
 					MainActivity.yBoxChecked = true;
 				} else {
 					MainActivity.yAxisLocked = false;
 					MainActivity.yBoxChecked = false;
 				}
 			}
 		});
 		zBox.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (((CheckBox) v).isChecked()) {
 					MainActivity.zAxisLocked = true;
 					MainActivity.zBoxChecked = true;
 				} else {
 					MainActivity.zAxisLocked = false;
 					MainActivity.zBoxChecked = false;
 				}
 			}
 		});
 		rollBox.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (((CheckBox) v).isChecked()) {
 					MainActivity.rollLocked = true;
 					MainActivity.rollBoxChecked = true;
 				} else {
 					MainActivity.rollLocked = false;
 					MainActivity.rollBoxChecked = false;
 				}
 			}
 		});
 		pitchBox.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (((CheckBox) v).isChecked()) {
 					MainActivity.pitchLocked = true;
 					MainActivity.pitchBoxChecked = true;
 				} else {
 					MainActivity.pitchLocked = false;
 					MainActivity.pitchBoxChecked = false;
 				}
 			}
 		});
 		
 		if (MainActivity.armMode) {
 			setArmMode();
 		} else {
 			setWristMode();
 		}
 	}
 
 	/*@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_lock_axis, menu);
 		return true;
 	}*/
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	/** Called when the user clicks the Done button */
     public void doneButton(View view) {
     	finish();
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         if ((keyCode == KeyEvent.KEYCODE_BACK))
         {
             finish();
         }
         return super.onKeyDown(keyCode, event);
     }
     
     private void setWristMode() {
     	//xBox.setChecked(true);
     	xBox.setEnabled(false);
     	xBox.setTextColor(Color.GRAY);
     	MainActivity.xAxisLocked = true;
     	
     	//yBox.setChecked(true);
     	yBox.setEnabled(false);
     	yBox.setTextColor(Color.GRAY);
     	MainActivity.yAxisLocked = true;
     	
     	//zBox.setChecked(true);
     	zBox.setEnabled(false);
     	zBox.setTextColor(Color.GRAY);
     	MainActivity.zAxisLocked = true;
     	
     	rollBox.setEnabled(true);
     	rollBox.setTextColor(Color.WHITE);
     	if (rollBox.isChecked()) {
     		MainActivity.rollLocked = true;
     	} else {
     		MainActivity.rollLocked = false;
     	}
     	
     	pitchBox.setEnabled(true);
     	pitchBox.setTextColor(Color.WHITE);
     	if (pitchBox.isChecked()) {
     		MainActivity.pitchLocked = true;
     	} else {
     		MainActivity.pitchLocked = false;
     	}
     	
     	rollImg.setOnTouchListener(new OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 
 				switch (event.getAction()) {
 					case MotionEvent.ACTION_UP: {
 						if (!rollBox.isChecked()) {
 							MainActivity.rollLocked = true;
							MainActivity.rollBoxChecked = true;
 							rollBox.setChecked(true);
 						} else {
 							MainActivity.rollLocked = false;
							MainActivity.rollBoxChecked = false;
 							rollBox.setChecked(false);
 						}
 					}
 				}
 				return true;
 			}
 		});
 		
 		pitchImg.setOnTouchListener(new OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 
 				switch (event.getAction()) {
 					case MotionEvent.ACTION_UP: {
 						if (!pitchBox.isChecked()) {
 							MainActivity.pitchLocked = true;
							MainActivity.pitchBoxChecked = true;
 							pitchBox.setChecked(true);
 						} else {
 							MainActivity.pitchLocked = false;
							MainActivity.pitchBoxChecked = false;
 							pitchBox.setChecked(false);
 						}
 					}
 				}
 				return true;
 			}
 		});
 		
 		axis.setImageResource(R.drawable.white_axis_dbl_gray);
     	rollImg.setImageResource(R.drawable.roll_icon);
     	pitchImg.setImageResource(R.drawable.pitch_icon);
     	
     	MainActivity.armMode = false;
     }
     
     private void setArmMode() {
     	xBox.setEnabled(true);
     	xBox.setTextColor(Color.WHITE);
     	if (xBox.isChecked()) {
     		MainActivity.xAxisLocked = true;
     	} else {
     		MainActivity.xAxisLocked = false;
     	}
     	
     	yBox.setEnabled(true);
     	yBox.setTextColor(Color.WHITE);
     	if (yBox.isChecked()) {
     		MainActivity.yAxisLocked = true;
     	} else {
     		MainActivity.yAxisLocked = false;
     	}
     	
     	zBox.setEnabled(true);
     	zBox.setTextColor(Color.WHITE);
     	if (zBox.isChecked()) {
     		MainActivity.zAxisLocked = true;
     	} else {
     		MainActivity.zAxisLocked = false;
     	}
     	
     	//rollBox.setChecked(true);
     	rollBox.setEnabled(false);
     	rollBox.setTextColor(Color.GRAY);
     	MainActivity.rollLocked = true;
     	
     	//pitchBox.setChecked(true);
     	pitchBox.setEnabled(false);
     	pitchBox.setTextColor(Color.GRAY);
     	MainActivity.pitchLocked = true;
     	
     	rollImg.setOnTouchListener(null);
     	pitchImg.setOnTouchListener(null);
     	
     	axis.setImageResource(R.drawable.white_axis_dbl);
     	rollImg.setImageResource(R.drawable.roll_icon_gray);
     	pitchImg.setImageResource(R.drawable.pitch_icon_gray);
     	
     	MainActivity.armMode = true;
     }
 
 }
