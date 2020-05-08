 package com.herokuapp.maintainenator;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.content.DialogInterface;
 import android.location.Location;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.herokuapp.maintainenator.utils.ExtAudioRecorder;
 import com.herokuapp.maintainenator.utils.ImageTextAdapter;
 
 public class IndoorFormFragment extends Fragment implements OnItemSelectedListener, OnLongClickListener {
 
     private static final String AUDIO_DIR = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_MUSIC + File.separator;
     private static final String TAG = "IndoorFormFragment";
     private static final String PHOTO_PATH_SEPARATOR = "##";
     private static final String END = "\r\n";
     private static final String BOUNDARY = "1q2w3e4r5t";
     private static final String TWO_HYPHENS = "--";
 
     private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
 
     private static final int HIGEST_FLOOR = 7;
     private static final int MAX_PHOTO_NUM = 3;
     private static final int[] FLOOR = {4, 7, 3, 5};
     private List<String> floorArray;
     private Spinner buildingSpinner;
     private Spinner floorSpinner;
     private ImageView imageView;
     private EditText descriptionText;
     private EditText roomText;
     private EditText extraLocation;
     private static int longClickedId;
     private int buildingSelectedTime;
     private String[] photoAudioArray;
 
     //Audio
     private ExtAudioRecorder extAudioRecorder;
     private MediaPlayer mediaPlayer;
     private Button recordButton;
     private ImageView audioView;
     private String audioFilePath;
     private boolean sendAudioFile;
     private AlertDialog removeAudioDialog;
 
     private FormActivity parentActivity;
 
     @Override
     public void onCreate (Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         buildingSelectedTime = 0;
         // Add ONE more entry for audio.
         photoAudioArray = new String[MAX_PHOTO_NUM + 1];
         floorArray = new ArrayList<String>(HIGEST_FLOOR);
         floorArray.add("Base");
         for(int i=1; i<=HIGEST_FLOOR; i++) {
             floorArray.add(i + "F");
         }
 
         parentActivity = (FormActivity) getActivity();
     }
 
     @Override
     public void onDestroy() {
         if (mediaPlayer != null) {
             mediaPlayer.release();
         }
         super.onDestroy();
     }
 
     @Override
     public void onResume() {
         super.onResume();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View layout = (LinearLayout) inflater.inflate(R.layout.fragment_indoor_form, container, false);
 
         layout.findViewById(R.id.indoor_submit).setOnClickListener(new IndoorSubmitClickListener());
         descriptionText = (EditText) layout.findViewById(R.id.indoor_description);
         roomText = (EditText) layout.findViewById(R.id.room_text);
         extraLocation = (EditText) layout.findViewById(R.id.indoor_location_optional);
         imageView = (ImageView) layout.findViewById(R.id.indoor_image_view1);
         imageView.setOnLongClickListener(this);
         buildingSpinner = (Spinner) layout.findViewById(R.id.building_spinner);
         buildingSpinner.setOnItemSelectedListener(this);
         buildingSpinner.setAdapter(new ImageTextAdapter(getActivity(), R.layout.building_row));
 
         floorSpinner = (Spinner) layout.findViewById(R.id.floor_spinner);
         floorSpinner.setOnItemSelectedListener(this);
 
         //Audio
         recordButton = (Button) layout.findViewById(R.id.indoor_audio);
         recordButton.setOnTouchListener(new ButtonTouchListener());
         audioView = (ImageView) layout.findViewById(R.id.indoor_audio_record);
         audioView.setOnLongClickListener(this);
         return layout;
     }
 
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
         if (parent.getId() == R.id.building_spinner) {
             synchronized(this) {
                 buildingSelectedTime++;
             }
             Log.d(getClass().getSimpleName(), "Building spinner selected.");
             ArrayAdapter<String> floorAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, floorArray.subList(0, FLOOR[pos]+1));
             floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             floorSpinner.setAdapter(floorAdapter);
         }
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> parent) {
     }
 
     @Override
     public boolean onLongClick(View v) {
         if (v.getId() == R.id.indoor_audio_record) {
             if (sendAudioFile) {
                 generateRemoveAudioDialog().show();
             }
         } else {
             longClickedId = v.getId();
             PhotoActionDialogFragment photoActionDialog = new PhotoActionDialogFragment();
             photoActionDialog.show(getActivity().getFragmentManager(), "photo_action");
         }
         return true;
     }
 
     int getBuildingSelectedTime() {
         synchronized(this) {
             return buildingSelectedTime;
         }
     }
 
     static int getLongClickedId() {
         return longClickedId;
     }
 
     void setPhotoPath(int viewId, String path) {
         if (viewId == R.id.indoor_image_view1) {
             photoAudioArray[0] = path;
         } else if (viewId == R.id.indoor_image_view2) {
             photoAudioArray[1] = path;
         } else if (viewId == R.id.indoor_image_view3) {
             photoAudioArray[2] = path;
         }
     }
 
     String generateMultipartForm() {
         // Just text info
         StringBuilder sb = new StringBuilder();
         sb.append(TWO_HYPHENS + BOUNDARY + END);
         sb.append("Content-Disposition: form-data; name=\"description\"" + END + END + descriptionText.getText().toString() + END);
         sb.append(TWO_HYPHENS + BOUNDARY + END);
         sb.append("Content-Disposition: form-data; name=\"building\"" + END + END + buildingSpinner.getSelectedItem().toString() + END);
         sb.append(TWO_HYPHENS + BOUNDARY + END);
         sb.append("Content-Disposition: form-data; name=\"floor\"" + END + END + floorSpinner.getSelectedItem().toString() + END);
         sb.append(TWO_HYPHENS + BOUNDARY + END);
         sb.append("Content-Disposition: form-data; name=\"room\"" + END + END + roomText.getText().toString() + END);
         // Add indoor status
         sb.append(TWO_HYPHENS + BOUNDARY + END);
         sb.append("Content-Disposition: form-data; name=\"indoor\"" + END + END + "checked" + END);
         String extra = extraLocation.getText().toString();
         if (!extra.isEmpty()) {
             sb.append(TWO_HYPHENS + BOUNDARY + END);
             sb.append("Content-Disposition: form-data; name=\"location\"" + END + END + extra + END);
         }
         Location latestLocation = ((FormActivity) getActivity()).getLatestLocation();
         if (latestLocation != null) {
             sb.append(TWO_HYPHENS + BOUNDARY + END);
             sb.append("Content-Disposition: form-data; name=\"latitude\"" + END + END + latestLocation.getLatitude() + END);
             sb.append(TWO_HYPHENS + BOUNDARY + END);
             sb.append("Content-Disposition: form-data; name=\"longitude\"" + END + END + latestLocation.getLongitude() + END);
         }
         return sb.toString();
     }
 
     private String joinPhotoPath() {
         StringBuilder sb = new StringBuilder();
         boolean first = true;
         for (int index=0; index<MAX_PHOTO_NUM; index++) {
             if (photoAudioArray[index] != null && !photoAudioArray[index].isEmpty()) {
                 if (first) {
                     first = false;
                     sb.append(photoAudioArray[index]);
                 } else {
                     sb.append(PHOTO_PATH_SEPARATOR);
                     sb.append(photoAudioArray[index]);
                 }
             }
         }
         Log.d(TAG, sb.toString());
         return sb.toString();
     }
 
     private AlertDialog generateRemoveAudioDialog() {
         if (removeAudioDialog == null) {
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             builder.setMessage("Delete this audio message?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             sendAudioFile = false;
                             audioView.setImageBitmap(null);
                             audioView.setOnClickListener(null);
                             audioView.setClickable(false);
                         }
                     })
                     .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                         }
                     });
             removeAudioDialog = builder.create();
         }
         return removeAudioDialog;
     }
 
     private class ButtonTouchListener implements OnTouchListener {
         @Override
         public boolean onTouch(View v, MotionEvent event) {
             int action = event.getAction();
             if (action == MotionEvent.ACTION_DOWN) {
                 Log.d(TAG, "Start recording...");
                 Date date = new Date();
                 audioFilePath = AUDIO_DIR + "audio-" + DATE_FORMAT.format(date) + ".wav";
                 extAudioRecorder = parentActivity.getAudioRecorder();
                 extAudioRecorder.setOutputFile(audioFilePath);
                 extAudioRecorder.prepare();
                 extAudioRecorder.start();
             } else if (action == MotionEvent.ACTION_UP) {
                 Log.d(TAG, "Stop recording...");
                 extAudioRecorder.stop();
                 sendAudioFile = true;
                 if (!audioView.isClickable()) {
                     audioView.setOnClickListener(new AudioPlayClickListener());
                 }
                 audioView.setImageResource(R.drawable.audio_record_icon);
             }
             // No more actions for following receivers.
             return true;
         }
     }
 
     private class AudioPlayClickListener implements OnClickListener {
         @Override
         public void onClick(View v) {
             if (sendAudioFile) {
                 if (mediaPlayer == null) {
                     mediaPlayer = new MediaPlayer();
                     mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                         @Override
                         public void onCompletion(MediaPlayer mp) {
                             mp.reset();
                         }
                     });
                 }
                try {
                    mediaPlayer.setDataSource(audioFilePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    Log.d(TAG, ioe.getMessage());
                 }
             }
         }
     }
 
     private class IndoorSubmitClickListener implements OnClickListener {
 
         boolean checkData() {
             if (descriptionText.getText().toString().isEmpty() || roomText.getText().toString().isEmpty()) {
                 return false;
             }
             if (photoAudioArray[0] != null || photoAudioArray[1] != null || photoAudioArray[2] != null) {
                 return true;
             }
             //TODO
 //            ((FormActivity) getActivity()).buildAlertDialog().show();
             Log.d(getClass().getSimpleName(), "returning...");
             return true;
         }
 
         @Override
         public void onClick(View v) {
             if (checkData()) {
                 photoAudioArray[3] = audioFilePath;
                 ((FormActivity) getActivity()).new UploadMultipartTask().execute(photoAudioArray);
                 DatabaseHandler db = new DatabaseHandler(((FormActivity) getActivity()).getApplicationContext());
                 String location = buildingSpinner.getSelectedItem().toString() + ", " + floorSpinner.getSelectedItem().toString() + ", " + roomText.getText().toString();
                 String description = descriptionText.getText().toString() + ", " + extraLocation.getText().toString();;
                 History indoorReport = new History(description, location);
                 indoorReport.setPhotosPath(joinPhotoPath());
                 indoorReport.setAudioPath(audioFilePath);
                 db.addReport(indoorReport);
                 db.close();
                 Log.d(TAG, "Add indoor report to database " + indoorReport);
             } else {
                 Toast.makeText(getActivity(), "Missing info.", Toast.LENGTH_LONG).show();
             }
         }
     }
 }
