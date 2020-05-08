 package se.team05.dialog;
 
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
  */
 
 //import com.example.testmap.CheckPoint;
 //import com.example.testmap.CheckPointOverlay;
 //import com.example.testmap.R;
 //import com.example.testmap.R.id;
 //import com.example.testmap.R.layout;
 
 import java.io.IOException;
 
 import se.team05.R;
 import se.team05.activity.MediaSelectorActivity;
 import se.team05.content.SoundManager;
 import se.team05.overlay.CheckPoint;
 import se.team05.overlay.CheckPointOverlay;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 /**
  * This is the dialog that pops up when a checkpoint is created or touched. The
  * Dialog has settings such as name & radius and buttons for deleting and saving
  * the checkpoint.
  * 
  * @author Patrik Thituson & Daniel Kvist
  * @version 1.0
  * 
  */
 public class EditCheckPointDialog extends Dialog implements View.OnClickListener, OnSeekBarChangeListener
 {
 	public interface Callbacks
 	{
 		public void onDelete();
 
 	}
 
 	private final int REQUEST_MEDIA = 0;
 
 	private CheckPointOverlay callBack;
 	private CheckPoint checkPoint;
 	private TextView nameTextField;
 	private TextView radiusTextField;
 	private Button recordButton;
 
 	public EditCheckPointDialog(Context context, CheckPointOverlay callback, CheckPoint checkPoint)
 	{
 		super(context);
 		this.callBack = callback;
 		this.checkPoint = checkPoint;
 		setCancelable(false); // Possible change to
 								// setCanceledOnTouchOutside(false)
 	}
 
 	/**
 	 * Initate the dialog with layout from xml file and sets the default values
 	 * on the attributes
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edit_checkpoint_dialog);
 		setTitle("Edit CheckPoint");
 
 		findViewById(R.id.delete_button).setOnClickListener(this);
 		findViewById(R.id.save_button).setOnClickListener(this);
 
 		nameTextField = (TextView) findViewById(R.id.name);
 		nameTextField.setText(checkPoint.getName());
 
 		SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
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
 				recordSound();
 				break;
 			case R.id.select_button:
 				Context context = getContext();
 				Intent intent = new Intent(context, MediaSelectorActivity.class);
 				((Activity) context).startActivityForResult(intent, REQUEST_MEDIA);
 				break;
 			case R.id.delete_button:
 				callBack.onDelete();
 				dismiss();
 				break;
 			case R.id.save_button:
 				checkPoint.setName(nameTextField.getText().toString());
				radiusTextField = (TextView) findViewById(R.id.radius_text);
 				String radiusString = radiusTextField.getText().toString();
 				int radius = Integer.parseInt(radiusString);
 				checkPoint.setRadius(radius);
 				dismiss();
 				break;
 			default:
 				break;
 		}
 	}
 
 	/**
 	 * Records a new sound if a recording is not taking place and stops the
 	 * current recording if a recording is being made. The method also changes
 	 * the text of the "record" button to allow the user for easy interaction
 	 * while recording.
 	 */
 	private void recordSound()
 	{
 		SoundManager soundManager = new SoundManager(getContext());
 		if (soundManager.isRecording())
 		{
 			try
 			{
 				soundManager.startRecording();
 				recordButton.setText("Stop recording");
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		else
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
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 }
