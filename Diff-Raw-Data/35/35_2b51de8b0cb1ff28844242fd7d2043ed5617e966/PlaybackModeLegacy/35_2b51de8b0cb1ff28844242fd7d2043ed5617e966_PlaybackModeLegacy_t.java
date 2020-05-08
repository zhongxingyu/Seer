 package com.MusicalSketches;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.MusicalSketches.datarep.Note;
 import com.MusicalSketches.datarep.NoteFrequencies;
 import com.MusicalSketches.datarep.Song;
 
 public class PlaybackModeLegacy extends Activity {
 	Song song;
 	int notesOnScreen = 0;
 	int state = 0;
 	int sampleRate = 8000;
 	byte[] generatedSnd;
 	int numSamples;
	int screen = 0;
 	AudioTrack audioTrack;
 	ImageButton b;
 	int xloc;
 	ImageView arrow;
 	ViewGroup group;
 	ImageButton right_button;
 	ImageButton left_button;
 	Timer arrowTimer;
 	int[] arrowLocations = new int[] { 110, 210, 310, 410, 510, 610 };
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.playback_mode);
 
 		song = (Song) getIntent().getSerializableExtra("song object");
 		arrow = (ImageView) findViewById(R.id.arrow_indicator);
 		right_button = (ImageButton) findViewById(R.id.right_arrow);
 		left_button = (ImageButton) findViewById(R.id.left_arrow);
 
 		addClefMeterKey(song);
 		genTone();
 
 		ImageButton play_pause = (ImageButton) findViewById(R.id.play_pause_button);
 		ImageButton rewind_button = (ImageButton) findViewById(R.id.rewind_button);
 		ImageButton forward_button = (ImageButton) findViewById(R.id.forward_button);
 		b = (ImageButton) findViewById(R.id.play_pause_button);
 		group = (ViewGroup) findViewById(R.id.edit_layout);
 
 		play_pause.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (state == 0) {
 					state = 1;
 					b.setImageResource(R.drawable.pause);
 					audioTrack.play();
 					initArrowTimer();
 				} else {
 					state = 0;
 					b.setImageResource(R.drawable.play);
 					audioTrack.pause();
 				}
 			}
 		});
 
 		rewind_button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				audioTrack.stop();
 				makeTrack();
 			}
 		});
 
 		forward_button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				audioTrack.setPlaybackHeadPosition(audioTrack
 						.getPlaybackHeadPosition());
 			}
 		});
 
 		uploadFromSong(song);
 	}
 
 	public void uploadFromSong(Song s) {
 		int g = 0;
 		ViewGroup group = (ViewGroup) findViewById(R.id.edit_layout);
		for (int loc = 0; loc < EditModeLegacy.MAX_NOTES_ONSCREEN; loc++) {
			Note n = s.getNoteNum(EditModeLegacy.MAX_NOTES_ONSCREEN*screen + loc);
 			Log.d("", "adding note");
 			double freq = n.getPitch();
 			double l = n.getLength();
 			String str = NoteFrequencies.freqToString.get(freq);
 			Log.d("", str);
 			str = str.substring(0, 2);
 			Log.d("", str);
 			double y = 0;
 			for (int i = 0; i < NoteFrequencies.staff_notes.length; i++) {
 				if (NoteFrequencies.staff_notes[i].equals(str)) {
 					y = NoteFrequencies.staff_lines[i] - 30;
 				}
 			}
 			if (l == 0.125) {
 				ImageView img = new ImageView(getApplicationContext());
 				img.setImageResource(R.drawable.eigth_note_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.removeView(img);
 				group.addView(img);
 			} else if (l == 0.25) {
 				ImageView img = new ImageView(getApplicationContext());
 				img.setImageResource(R.drawable.quarter_note_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.removeView(img);
 				group.addView(img);
 			} else if (l == 0.5) {
 				ImageView img = new ImageView(getApplicationContext());
 				img.setImageResource(R.drawable.half_note_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.removeView(img);
 				group.addView(img);
 			}
 			double x = 20 + (g + 1) * 60;
 			Log.d("", "" + x);
 			str = NoteFrequencies.freqToString.get(freq);
 			Log.d("", "str value: " + str);
 			if (str.endsWith("sharp")) {
 				ImageView img = new ImageView(getApplicationContext());
 				img.setImageResource(R.drawable.sharp_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.addView(img);
 			} else if (str.endsWith("flat")) {
 				ImageView img = new ImageView(getApplicationContext());
 				img.setImageResource(R.drawable.flat_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.addView(img);
 			} else if (str.endsWith("natural")) {
 				ImageView img = new ImageView(getApplicationContext());
 				img.setImageResource(R.drawable.natural_transparent);
 				img.setAdjustViewBounds(true);
 				img.setMaxHeight(65);
 				img.setMaxWidth(50);
 				img.setVisibility(0);
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				img.setLayoutParams(params);
 				group.removeView(img);
 				group.addView(img);
 			}
 			g++;
 		}
 	}
 
 	public enum playback_menu_options {
 		CLOSE, EDIT,
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.playback_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.playback_close:
 			Toast.makeText(this, "Bye!", Toast.LENGTH_SHORT).show();
 			audioTrack.stop();
 			finish();
 			break;
 		case R.id.playback_edit:
 			Toast.makeText(this, "As you wish...", Toast.LENGTH_SHORT).show();
 			audioTrack.stop();
 			finish();
 			break;
 		}
 		return false;
 	}
 
 	public void addClefMeterKey(Song s) {
 		int clef = s.getClef();
 		if (clef == 1) {
 			((ImageView) findViewById(R.id.clef_image))
 					.setImageResource(R.drawable.treble_clef);
 		}
 		((TextView) findViewById(R.id.meter_text)).setText("" + s.getMeterTop()
 				+ "\n" + s.getMeterBottom());
 	}
 
 	public void initArrowTimer() {
 		int g = 0;
 		arrowTimer = new Timer();
 		long time = 0;
 		group.removeView(arrow);
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		params.topMargin = (int) 50;
 		params.leftMargin = 80;
 		arrow.setLayoutParams(params);
 		group.addView(arrow);
 		arrow.setVisibility(0);
 		for (Note n : song.getNotes().getNotes()) {
			time += n.getLength() * 1000;
 			g++;
 			final int x = 20 + (g + 1) * 60;
 			arrowTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					class ArrowRun implements Runnable {
 						int x;

 						public ArrowRun(int x) {
 							this.x = x;
 						}
 
 						@Override
 						public void run() {
 							group.removeView(arrow);
 							RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 									RelativeLayout.LayoutParams.WRAP_CONTENT,
 									RelativeLayout.LayoutParams.WRAP_CONTENT);
 							params.topMargin = (int) 50;
 							params.leftMargin = x;
 							arrow.setLayoutParams(params);
 							group.addView(arrow);
 							arrow.setVisibility(0);
 						}

 					}
 					runOnUiThread(new ArrowRun(x));
 				}
 			}, time);
 		}
 	}
 
 	void genTone() {
 		double duration = 0; // seconds
 		for (Note n : song.getNotes().getNotes()) {
 			duration += n.getLength(); // TODO tempo!!
 		}
 		numSamples = (int) (duration * sampleRate);
 		double[] sample = new double[numSamples];
 		generatedSnd = new byte[2 * numSamples];
 		int j = 0;
 		for (Note n : song.getNotes().getNotes()) {
 			Log.d("", "Pitch: " + n.getPitch());
 			double freqOfTone = n.getPitch(); // hz
 			int numThisNote = (int) (n.getLength() * sampleRate);
 			Log.d("", "J is: " + j);
 			// fill out the array
 			for (int i = 0; i < numThisNote; ++i) {
 				sample[j] = Math.sin(2 * Math.PI * i
 						/ (sampleRate / freqOfTone));
 				j++;
 			}
 		}
 
 		// convert to 16 bit pcm sound array
 		// assumes the sample buffer is normalised.
 		int idx = 0;
 		for (final double dVal : sample) {
 			// scale to maximum amplitude
 			final short val = (short) ((dVal * 32767));
 			// in 16 bit wav PCM, first byte is the low order byte
 			generatedSnd[idx++] = (byte) (val & 0x00ff);
 			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
 		}
 		Log.wtf("", "Buffer size in bytes: " + numSamples);
 		makeTrack();
 	}
 
 	public void makeTrack() {
 		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
 				AudioFormat.CHANNEL_OUT_DEFAULT,
 				AudioFormat.ENCODING_PCM_16BIT, 2 * numSamples,
 				AudioTrack.MODE_STATIC);
 		audioTrack.write(generatedSnd, 0, generatedSnd.length);
 		audioTrack.setPlaybackRate(8000);
 	}
 }
