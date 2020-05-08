 package edu.chalmers.dat255.group09.Alarmed.utils;
 
 import java.io.IOException;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.SeekBar;
 import edu.chalmers.dat255.group09.Alarmed.R;
 
 /**
  * Helper class which takes care of everything to do with vibration and audio
  * while creating an alarm
  * 
  * @author Adrian Bjugard
  */
 public class AudioHelper {
 	private Context context;
 	private Intent intent;
 	private MediaPlayer mediaPlayer;
 	private AudioManager audioMan;
 
 	private View volumeView;
 	private AlertDialog volumeDialog, alarmToneDialog;
 	private Map<String, String> alarmTones;
 
 	/**
 	 * Constructor for the AudioHelper object
 	 * 
 	 * @param c
 	 *            Context holding the helper
 	 * @param i
 	 *            Intent containing information about the alarm
 	 */
 	public AudioHelper(Context c, Intent i) {
 		context = c;
 		intent = i;
 
 		audioMan = (AudioManager) context
 				.getSystemService(Context.AUDIO_SERVICE);
 		mediaPlayer = new MediaPlayer();
 
 		initSettings();
 		setupAlarmToneMap();
 		createVolumeDialog();
 		createAlarmToneDialog();
 	}
 
 	private void initSettings() {
 		intent.putExtra("vibration", true);
 		intent.putExtra("volume",
 				audioMan.getStreamMaxVolume(AudioManager.STREAM_ALARM));
 		setInitialAlarmTone();
 	}
 
 	/**
 	 * Since the alarm tone dialog isn't guaranteed to spawn during the lifetime
 	 * of the CreateAlarm activity, this method sets the default value unless
 	 * one already exists in the intent (in edit mode)
 	 */
 	private void setInitialAlarmTone() {
 		String previousTone = intent.getStringExtra("toneuri");
 		if (previousTone == null) {
 			Uri tone = RingtoneManager.getActualDefaultRingtoneUri(context,
 					RingtoneManager.TYPE_ALARM);
 			if (tone == null) {
 				tone = RingtoneManager.getActualDefaultRingtoneUri(context,
 						RingtoneManager.TYPE_RINGTONE);
 			}
 			intent.putExtra("toneuri", tone.toString());
 		}
 	}
 
 	/**
 	 * Sets up a map of alarm tone URIs to their human readable titles
 	 */
 	private void setupAlarmToneMap() {
 		RingtoneManager ringMan = new RingtoneManager(context);
 
 		Cursor cur = ringMan.getCursor();
 
 		int tonesAvailable = cur.getCount();
 		if (tonesAvailable == 0) {
 			alarmTones = new HashMap<String, String>();
 			return;
 		}
 
 		Map<String, String> tones = new HashMap<String, String>();
 		while (!cur.isAfterLast() && cur.moveToNext()) {
 			String uri = cur.getString(RingtoneManager.URI_COLUMN_INDEX);
 			String title = cur.getString(RingtoneManager.TITLE_COLUMN_INDEX);
 
 			tones.put(uri, title);
 		}
 
 		MapSorter sorter = new MapSorter(tones);
 		Map<String, String> sortedMap = new TreeMap<String, String>(sorter);
 		sortedMap.putAll(tones);
 
 		alarmTones = sortedMap;
 	}
 
 	/**
 	 * Getter for alarm tone URIs as an array
 	 * 
 	 * @return String array of alarm tone URIs
 	 */
 	private String[] getAlarmToneUris() {
 		return alarmTones.keySet().toArray(new String[alarmTones.size()]);
 	}
 
 	/**
 	 * Getter for alarm tone names as an array
 	 * 
 	 * @return String array of alarm tone names
 	 */
 	private String[] getAlarmToneNames() {
 		return alarmTones.values().toArray(new String[alarmTones.size()]);
 	}
 
 	/**
 	 * Creates the volume dialog
 	 */
 	private void createVolumeDialog() {
 		int defaultVolume = audioMan
 				.getStreamMaxVolume(AudioManager.STREAM_ALARM);
 		LayoutInflater inflater = LayoutInflater.from(context);
 		volumeView = inflater.inflate(R.layout.volume_dialog, null);
 
 		SeekBar seekBar = ((SeekBar) volumeView
 				.findViewById(R.id.selector_volume));
 		CheckBox checkBox = ((CheckBox) volumeView
 				.findViewById(R.id.selector_vibration));
 
 		seekBar.setMax(defaultVolume);
 		seekBar.setProgress(intent.getIntExtra("volume", defaultVolume));
 		checkBox.setChecked(intent.getBooleanExtra("vibration", true));
 
 		volumeDialog = new AlertDialog.Builder(context)
 				.setTitle("Set volume options")
 				.setView(volumeView)
 				.setPositiveButton(android.R.string.ok,
 						new VolumeDialogListener()).create();
 	}
 
 	/**
 	 * Getter for the alarm volume and vibration selection dialog
 	 * 
 	 * @return Alarm volume and vibration selection dialog
 	 */
 	public Dialog getVolumeDialog() {
 		return volumeDialog;
 	}
 
 	/**
 	 * Creates the alarm tone selector dialog
 	 */
 	private void createAlarmToneDialog() {
 		String selectedTone = intent.getStringExtra("toneuri");
 		int selection = -1;
 		for (int i = 0; i < alarmTones.size(); i++) {
 			if (getAlarmToneUris()[i].equals(selectedTone)) {
 				selection = i;
 			}
 		}
 		final boolean noMatchFound = (selection == -1);
 
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
 				android.R.layout.simple_list_item_single_choice,
 				getAlarmToneNames());
 
 		alarmToneDialog = new AlertDialog.Builder(context)
 				.setTitle(R.string.title_alarm_tone_selector)
 				.setSingleChoiceItems(adapter, selection,
 						new AlarmToneClickListener())
 				.setNegativeButton(android.R.string.cancel,
 						new AlarmToneCancelListener())
 				.setPositiveButton(android.R.string.ok,
 						new AlarmToneOkListener()).create();
 
 		alarmToneDialog.setOnShowListener(new DialogInterface.OnShowListener() {
 			public void onShow(DialogInterface dialog) {
 				if (noMatchFound) {
 					((AlertDialog) dialog).getButton(
 							AlertDialog.BUTTON_POSITIVE).setEnabled(false);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Getter for the alarm tone selection dialog
 	 * 
 	 * @return Alarm tone selection dialog
 	 */
 	public Dialog getAlarmToneDialog() {
 		return alarmToneDialog;
 	}
 
 	/**
 	 * A listener that activates when the OK button is clicked in the volume
 	 * dialog
 	 * 
 	 * @author Adrian Bjugard
 	 * 
 	 */
 	private class VolumeDialogListener implements
 			DialogInterface.OnClickListener {
 		public void onClick(DialogInterface dialog, int i) {
 			intent.putExtra("vibration", ((CheckBox) volumeView
 					.findViewById(R.id.selector_vibration)).isChecked());
 			intent.putExtra("volume", ((SeekBar) volumeView
 					.findViewById(R.id.selector_volume)).getProgress());
 		}
 	}
 
 	/**
 	 * Listener for clicks on alarm tones, responsible for playing the alarm
 	 * sound selected, to give the user a demo
 	 * 
 	 * @author Adrian Bjugard
 	 * 
 	 */
 	private class AlarmToneClickListener implements
 			DialogInterface.OnClickListener {
 		public void onClick(DialogInterface dialog, int index) {
 			if (index != -1) {
 				((AlertDialog) dialog).getButton(
 						DialogInterface.BUTTON_POSITIVE).setEnabled(true);
 			}
 			Uri uri = Uri.parse(getAlarmToneUris()[index]);
 			try {
 				mediaPlayer.reset();
 				mediaPlayer.setDataSource(context, uri);
 				mediaPlayer.prepare();
 				mediaPlayer.start();
 			} catch (IOException e) {
 				Log.e("Sound", e.getMessage(), e);
 			}
 		}
 	}
 
 	/**
 	 * Listener for clicks on the cancel button in the alarm tone dialog, stops
 	 * any playing sound created by the dialog
 	 * 
 	 * @author Adrian Bjugard
 	 * 
 	 */
 	private class AlarmToneCancelListener implements
 			DialogInterface.OnClickListener {
 		public void onClick(DialogInterface dialog, int index) {
 			mediaPlayer.reset();
 		}
 	}
 
 	/**
 	 * Listener for clicks on the OK button in the alarm tone dialog, stops any
 	 * playing sound created by the dialog
 	 * 
 	 * @author Adrian Bjugard
 	 * 
 	 */
 	private class AlarmToneOkListener implements
 			DialogInterface.OnClickListener {
 		public void onClick(DialogInterface dialog, int index) {
 			mediaPlayer.reset();
 			int pos = ((AlertDialog) dialog).getListView()
 					.getCheckedItemPosition();
 			String tone = getAlarmToneUris()[pos].toString();
 			intent.putExtra("toneuri", tone);
 		}
 	}
 
 	/**
 	 * A comparator that sorts the alarm tone map
 	 * 
 	 * @author Adrian Bjugard
 	 * 
 	 */
 	private class MapSorter implements Comparator<String> {
 		Map<String, String> map;
 
 		public MapSorter(Map<String, String> map) {
 			this.map = map;
 		}
 
 		public int compare(String a, String b) {
 			return map.get(a).compareTo(map.get(b));
 		}
 	}
 }
