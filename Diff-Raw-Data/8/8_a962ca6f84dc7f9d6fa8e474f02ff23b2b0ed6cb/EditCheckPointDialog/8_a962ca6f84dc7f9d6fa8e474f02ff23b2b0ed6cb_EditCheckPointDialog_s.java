 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) Copyright 2012: Daniel Kvist, Henrik Hugo, Gustaf Werlinder, Patrik Thitusson, Markus Schutzer
  */
 package se.team05.dialog;
 
 import java.io.IOException;
 
 import se.team05.R;
 import se.team05.activity.MediaSelectorActivity;
 import se.team05.content.SoundManager;
 import se.team05.overlay.CheckPoint;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * This is the dialog that pops up when a checkpoint is created or touched. The
  * Dialog has settings such as name & radius and buttons for deleting and saving
  * the checkpoint.
  * 
  * @author Patrik Thituson & Daniel Kvist
  * 
  */
 public class EditCheckPointDialog extends Dialog implements View.OnClickListener, OnSeekBarChangeListener
 {
 	public interface Callbacks
 	{
 		public void onDeleteCheckPoint(long checkPointId);
 
 		public void onSaveCheckPoint(CheckPoint checkPoint);
 	}
 
 	private static final String TAG = "Personal Trainer";
 	public static final int MODE_ADD = 0;
 	public static final int MODE_EDIT = 1;
 
 	private Callbacks callBack;
 	private CheckPoint checkPoint;
 	private TextView nameTextField;
 	private TextView radiusTextField;
 	private Button recordButton;
 	private Activity parentActivity;
 	private SoundManager soundManager;
 	private int mode;
 	private SeekBar seekBar;
 
 	/**
 	 * The constructor
 	 * 
 	 * @param context
 	 * @param checkPoint
 	 * @param mode
 	 */
 	public EditCheckPointDialog(Context context, CheckPoint checkPoint, int mode)
 	{
 		super(context);
 		this.callBack = (Callbacks) context;
 		this.checkPoint = checkPoint;
 		this.parentActivity = (Activity) context;
 		this.soundManager = new SoundManager(context);
 		this.mode = mode;
 		setCanceledOnTouchOutside(false);
 	}
 
 	/**
 	 * Initate the dialog with layout from xml file and sets the default values
 	 * on the attributes
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dialog_edit_checkpoint);
 		setTitle("Edit CheckPoint");
 		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 
 		Button deleteButton = (Button) findViewById(R.id.delete_button);
 		deleteButton.setOnClickListener(this);
 		findViewById(R.id.save_button).setOnClickListener(this);
 
 		if (mode == MODE_ADD)
 		{
 			deleteButton.setText("Cancel");
 		}
 
 		nameTextField = ((TextView) findViewById(R.id.name));
 		nameTextField.setText(checkPoint.getName());
 
 		radiusTextField = ((TextView) findViewById(R.id.radius_text));
 
 		seekBar = ((SeekBar) findViewById(R.id.seekBar1));
 		seekBar.setOnSeekBarChangeListener(this);
 		seekBar.setProgress(checkPoint.getRadius());
 
 		recordButton = (Button) findViewById(R.id.record_button);
 		recordButton.setOnClickListener(this);
 
 		((Button) findViewById(R.id.select_button)).setOnClickListener(this);
 	}
 
 	/**
 	 * Handles the clicks from the buttons in the dialog. Record button starts a
 	 * new recording, select button starts a new activity and waits for a result
 	 * when the user has selected the media. The delete and save buttons calls
 	 * back to the dialogs owner with onDelete and onSave.
 	 */
 	@Override
 	public void onClick(View v)
 	{
 		switch (v.getId())
 		{
 			case R.id.record_button:
 				if (!soundManager.isRecording())
 				{
 					startRecording();
 				}
 				else
 				{
 					stopRecording();
 				}
 				break;
 			case R.id.select_button:
 				stopRecording();
 				Intent intent = new Intent(parentActivity, MediaSelectorActivity.class);
 				intent.putParcelableArrayListExtra(MediaSelectorActivity.EXTRA_SELECTED_ITEMS, checkPoint.getTracks());
 				parentActivity.startActivityForResult(intent, MediaSelectorActivity.REQUEST_MEDIA);
 				break;
 			case R.id.delete_button:
 				stopRecording();
 				callBack.onDeleteCheckPoint(checkPoint.getId());
 				dismiss();
 				break;
 			case R.id.save_button:
 				stopRecording();
 				int radius = Integer.parseInt(radiusTextField.getText().toString());
 				checkPoint.setName(nameTextField.getText().toString());
 				checkPoint.setRadius(radius);
 				callBack.onSaveCheckPoint(checkPoint);
 				dismiss();
 				break;
 		}
 	}
 
 	/**
 	 * Records a new sound if a recording is not taking place and stops the
 	 * current recording if a recording is being made. The method also changes
 	 * the text of the "record" button to allow the user for easy interaction
 	 * while recording.
 	 */
 	private void startRecording()
 	{
 		if (!soundManager.isRecording())
 		{
 			try
 			{
 				Toast.makeText(getContext(),
 						getContext().getString(R.string.you_are_now_recording_speak_into_the_microphone),
 						Toast.LENGTH_LONG).show();
 				soundManager.startRecording();
 				recordButton.setText(R.string.stop_recording);
 			}
 			catch (IOException e)
 			{
 				Log.e(TAG, "Could not start recording: " + e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Stops the current recording if there is one taking place.
 	 */
	private void stopRecording()
 	{
 		if (soundManager.isRecording())
 		{
 			soundManager.stopRecording();
 			recordButton.setText(R.string.record);
 		}
 	}
 
 	/**
 	 * Sets the radius in the textfield when the seekbar is changed
 	 */
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
 	{
 		radiusTextField.setText("" + progress);
 	}
 
 	/**
 	 * Unused method
 	 */
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar)
 	{
 	}
 
 	/**
 	 * Unused method
 	 */
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar)
 	{
 	}
 
 	/**
 	 * Uses callback to delete the a checkpoint if it is created when the back
 	 * button is pressed if the dialog is in edit mode the back button is
 	 * unchanged
 	 */
 	@Override
 	public void onBackPressed()
 	{
 		if (mode == MODE_ADD)
 		{
 			callBack.onDeleteCheckPoint(checkPoint.getId());
 		}
 
 		super.onBackPressed();
 	}
 
 	/**
 	 * Gets a checkpoint with the current values in the edit text views.
 	 * 
 	 * @return the checkpoint with the new values.
 	 */
 	public CheckPoint getCheckPoint()
 	{
 		checkPoint.setName(nameTextField.getText().toString());
 		checkPoint.setRadius(seekBar.getProgress());
 		return checkPoint;
 	}
 }
