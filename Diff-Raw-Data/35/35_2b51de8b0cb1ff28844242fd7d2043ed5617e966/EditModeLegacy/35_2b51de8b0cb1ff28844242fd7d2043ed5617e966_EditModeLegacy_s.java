 package com.MusicalSketches;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.media.AudioRecord.OnRecordPositionUpdateListener;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.MusicalSketches.datarep.Note;
 import com.MusicalSketches.datarep.NoteFrequencies;
 import com.MusicalSketches.datarep.Song;
 
 public class EditModeLegacy extends Activity {
 	enum placement_objects {
 		left_note, middle_note, right_note, sharp, flat, natural
 	}
 
 	enum states {
 		wait, selected_to_place
 	}
 
 	ViewGroup group;
 	ImageButton left_note;
 	ImageButton middle_note;
 	ImageButton right_note;
 	ImageButton sharp_button;
 	ImageButton natural_button;
 	ImageButton flat_button;
 	TextView meter_disp;
 	ImageButton trash_button;
 	ImageView clef_image;
 	ImageView music_score;
 	ImageButton right_button;
 	ImageButton left_button;
	static final int MAX_NOTES_ONSCREEN = 10;
 	Song song = null;
 
 	states state = states.wait;
 
 	View selected_view;
 	boolean button_clicked = false; // tells whether the selected item is a
 									// button or placed piece
 	placement_objects type_selected;
 	View[] notes = new View[MAX_NOTES_ONSCREEN];
 	View[] annotations = new View[MAX_NOTES_ONSCREEN];
 	int screen_number = 0; // this increments as you screen to the right. min =
 							// 0.
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edit_mode);
 		meter_disp = (TextView) findViewById(R.id.meter_text);
 		group = (ViewGroup) findViewById(R.id.edit_layout);
 		left_note = (ImageButton) findViewById(R.id.left_note);
 		middle_note = (ImageButton) findViewById(R.id.middle_note);
 		right_note = (ImageButton) findViewById(R.id.right_note);
 		ImageButton more_notes = (ImageButton) findViewById(R.id.more_notes);
 		Button rests_button = (Button) findViewById(R.id.rests_button);
 		ImageButton record_button = (ImageButton) findViewById(R.id.record_button);
 		Button dynamics_button = (Button) findViewById(R.id.dynamics_button);
 		ImageButton slurs_button = (ImageButton) findViewById(R.id.slurs_button);
 		sharp_button = (ImageButton) findViewById(R.id.sharp_button);
 		flat_button = (ImageButton) findViewById(R.id.flat_button);
 		natural_button = (ImageButton) findViewById(R.id.natural_button);
 		music_score = (ImageView) findViewById(R.id.music_score);
 		trash_button = (ImageButton) findViewById(R.id.trash_can);
 		clef_image = (ImageView) findViewById(R.id.clef_image);
 		right_button = (ImageButton) findViewById(R.id.right_arrow);
 		left_button = (ImageButton) findViewById(R.id.left_arrow);
 
 		song = (Song) getIntent().getSerializableExtra("song object");
 
 		addClefMeterKey(song);
 		
 		class left_arrow_button_click implements OnClickListener {
 			@Override
 			public void onClick(View v) {
 				generateScreen(screen_number - 1);
 			}
 		}
 		
 		class right_arrow_button_click implements OnClickListener {
 			@Override
 			public void onClick(View v) {
 				generateScreen(screen_number + 1);
 			}
 		}
 
 		class eigth_click implements OnClickListener {
 
 			@Override
 			public void onClick(View arg0) {
 				if (state == states.wait) {
 					state = states.selected_to_place;
 					button_clicked = true;
 					selected_view = select(R.drawable.eigth_note_transparent);
 					((ImageButton) arg0)
 							.setImageResource(R.drawable.eigth_note_inverted);
 					type_selected = placement_objects.left_note;
 				} else {
 					state = states.wait;
 					unclickAll();
 				}
 			}
 		}
 		;
 		class quarter_click implements OnClickListener {
 
 			@Override
 			public void onClick(View arg0) {
 				if (state == states.wait) {
 					state = states.selected_to_place;
 					button_clicked = true;
 					selected_view = select(R.drawable.quarter_note_transparent);
 					((ImageButton) arg0)
 							.setImageResource(R.drawable.quarter_note_inverted);
 					type_selected = placement_objects.middle_note;
 				} else {
 					state = states.wait;
 					unclickAll();
 				}
 			}
 		}
 		;
 		class half_click implements OnClickListener {
 
 			@Override
 			public void onClick(View arg0) {
 				if (state == states.wait) {
 					button_clicked = true;
 					state = states.selected_to_place;
 					selected_view = select(R.drawable.half_note_transparent);
 					((ImageButton) arg0)
 							.setImageResource(R.drawable.half_note_inverted);
 					type_selected = placement_objects.right_note;
 				} else {
 					state = states.wait;
 					unclickAll();
 				}
 			}
 		}
 		;
 		class sharp_click implements OnClickListener {
 
 			@Override
 			public void onClick(View arg0) {
 				if (state == states.wait) {
 					button_clicked = true;
 					state = states.selected_to_place;
 					selected_view = select(R.drawable.sharp_transparent);
 					((ImageButton) arg0)
 							.setImageResource(R.drawable.sharp_inverted);
 					type_selected = placement_objects.sharp;
 				} else {
 					state = states.wait;
 					unclickAll();
 				}
 			}
 		}
 		;
 		class natural_click implements OnClickListener {
 
 			@Override
 			public void onClick(View arg0) {
 				if (state == states.wait) {
 					button_clicked = true;
 					state = states.selected_to_place;
 					selected_view = select(R.drawable.natural_transparent);
 					((ImageButton) arg0)
 							.setImageResource(R.drawable.natural_inverted);
 					type_selected = placement_objects.natural;
 				} else {
 					state = states.wait;
 					unclickAll();
 				}
 			}
 		}
 		;
 		class flat_click implements OnClickListener {
 
 			@Override
 			public void onClick(View arg0) {
 				if (state == states.wait) {
 					state = states.selected_to_place;
 					button_clicked = true;
 					selected_view = select(R.drawable.flat_transparent);
 					((ImageButton) arg0)
 							.setImageResource(R.drawable.flat_inverted);
 					type_selected = placement_objects.flat;
 				} else {
 					state = states.wait;
 					unclickAll();
 				}
 			}
 		}
 		;
 
 		class trash_click implements OnClickListener {
 
 			@Override
 			public void onClick(View arg0) {
 				if (state == states.selected_to_place) {
 					Log.d("",
 							"Selected view null? "
 									+ Boolean.toString(selected_view == null));
 					// at this point we could be placing a new element OR
 					// deleting an old one
 					if (button_clicked) {
 						button_clicked = false;
 					} else {
 						if (selected_view != null) {
 							group.removeView(selected_view);
 							for (int i = 0; i < notes.length; i++) {
 								if (notes[i] == selected_view) {
 									notes[i] = null;
 								}
 							}
 							for (int i = 0; i < annotations.length; i++) {
 								if (annotations[i] == selected_view) {
 									annotations[i] = null;
 								}
 							}
 						}
 					}
 					state = states.wait;
 					unclickAll();
 				} else {
 					Toast.makeText(getApplicationContext(), "Delete piece?",
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		}
 		;
 
 		left_note.setOnClickListener(new eigth_click());
 		middle_note.setOnClickListener(new quarter_click());
 		right_note.setOnClickListener(new half_click());
 		natural_button.setOnClickListener(new natural_click());
 		flat_button.setOnClickListener(new flat_click());
 		sharp_button.setOnClickListener(new sharp_click());
 		trash_button.setOnClickListener(new trash_click());
 		left_button.setOnClickListener(new left_arrow_button_click());
 		right_button.setOnClickListener(new right_arrow_button_click());
 
 		dynamics_button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				createDynamicsDialog();
 			}
 		});
 
 		clef_image.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				createClefDialog();
 			}
 		});
 
 		class score_touch implements OnTouchListener {
 			public boolean onTouch(float event_x, float event_y) {
 				if (notes[0] != null) {
 					Log.d("", notes[0].getHeight() + ", notes 0 height");
 				}
 				Log.d("", "Processing down click");
 				Log.d("", "Location: " + event_x + " " + event_y);
 				if (state == states.selected_to_place) {
 					Log.d("", "Processing placement click");
 					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 							RelativeLayout.LayoutParams.WRAP_CONTENT,
 							RelativeLayout.LayoutParams.WRAP_CONTENT);
 					params.topMargin = (int) event_y - 30;
 					// figure out where click occurred
 					int x = (int) event_x;
 					int y = (int) event_y;
 					// see if that's on an old image
 					for (int i = 0; i < notes.length; i++) {
 						if (notes[i] == null) {
 							continue;
 						}
 						Rect hitSpace = new Rect();
 						notes[i].getHitRect(hitSpace);
 						if (hit(x, y, hitSpace)) {
 							// hit!
 							if (type_selected == placement_objects.flat
 									|| type_selected == placement_objects.natural
 									|| type_selected == placement_objects.sharp) {
 								// need to find an annotation spot
 								params.leftMargin = 20 + (i + 1) * 60;
 								// remove any previous annotation there
 								if (annotations[i] != null) {
 									group.removeView(annotations[i]);
 								}
 								annotations[i] = selected_view;
 								selected_view.setLayoutParams(params);
 								group.removeView(selected_view);
 								group.addView(selected_view);
 								selected_view.setTag(type_selected);
 							}
 							state = states.wait;
 							unclickAll();
 							return true;
 						}
 					}
 					for (int i = 0; i < annotations.length; i++) {
 						if (annotations[i] == null) {
 							continue;
 						}
 						Rect hitSpace = new Rect();
 						annotations[i].getHitRect(hitSpace);
 						if (hit(x, y, hitSpace)) {
 							// hit!
 							unclickAll();
 							state = states.wait;
 							return true;
 						}
 					}
 					// placing a piece.
 					group.removeView(selected_view);
 					for (int i = 0; i < notes.length; i++) {
 						if (notes[i] == selected_view) {
 							notes[i] = null;
 						}
 					}
 					for (int i = 0; i < annotations.length; i++) {
 						if (annotations[i] == selected_view) {
 							annotations[i] = null;
 						}
 					}
 					if (type_selected == placement_objects.flat
 							|| type_selected == placement_objects.natural
 							|| type_selected == placement_objects.sharp) {
 						state = states.wait;
 						unclickAll();
 						return true;
 					} else {
 						// need to find a note spot
 						if (event_y > 257 || event_y < 120) {
 							state = states.wait;
 							unclickAll();
 							// outside the score...
 							return true;
 						}
 						int i = nextNote();
 						if (i < 0) {
 							// here's where we need a new screen. we should
 							// generate the new screen
 							// then process the click as normal?
 							//generateScreen(screen_number + 1);
 							return false;
 						}
 						int staff_line = 0;
 						for (int j = 0; j < NoteFrequencies.staff_lines.length; j++) {
 							if (event_y < NoteFrequencies.staff_lines[j]) {
 								staff_line = j;
 								continue;
 							}
 						}
 						params.topMargin = NoteFrequencies.staff_lines[staff_line] - 30;
 						params.leftMargin = 30 + (i + 1) * 60;
 						Log.d("", "Setting notes[" + i + "]");
 						notes[i] = selected_view;
 					}
 					selected_view.setLayoutParams(params);
 					group.addView(selected_view);
 					selected_view.setTag(type_selected);
 					state = states.wait;
 					unclickAll();
 					return true;
 				} else {
 					// waiting. this click is probably to pick up a piece.
 					// figure out where click occurred
 					button_clicked = false;
 					int x = (int) event_x;
 					int y = (int) event_y;
 					// see if that's on an old image
 					for (int i = 0; i < notes.length; i++) {
 						if (notes[i] == null) {
 							continue;
 						}
 						Rect hitSpace = new Rect();
 						notes[i].getHitRect(hitSpace);
 						if (hit(x, y, hitSpace)) {
 							// hit!
 							((ImageView) notes[i])
 									.setBackgroundColor(Color.RED);
 							selected_view = notes[i];
 							Log.d("", "Selected view null (selectin)? "
 									+ Boolean.toString(selected_view == null));
 							type_selected = (placement_objects) notes[i]
 									.getTag();
 							state = states.selected_to_place;
 							return true;
 						}
 					}
 					for (int i = 0; i < annotations.length; i++) {
 						if (annotations[i] == null) {
 							continue;
 						}
 						Rect hitSpace = new Rect();
 						annotations[i].getHitRect(hitSpace);
 						if (hit(x, y, hitSpace)) {
 							// hit!
 							((ImageView) annotations[i])
 									.setBackgroundColor(Color.RED);
 							selected_view = annotations[i];
 							type_selected = placement_objects.sharp;
 							state = states.selected_to_place;
 							return true;
 						}
 					}
 				}
 				return false;
 			}
 
 			@Override
 			public boolean onTouch(View v1, MotionEvent event) {
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					return onTouch(event.getX(), event.getY());
 				}
 				return false;
 			}
 		}
 		;
 
 		music_score.setOnTouchListener(new score_touch());
 		generateScreen(0);
 	}
 
 	private void generateScreen(int number) {
 		// plan here is to save all the shit onscreen
 		// then redisplay an overlapping note and arrows as necessary.
 		// NB: savePage() must occur before reassigning screen_number
 		this.savePage();
 		Toast.makeText(getApplicationContext(), "Song length: " + song.size(), Toast.LENGTH_SHORT).show();
 		this.screen_number = number;
 		if (this.screen_number > 0) {
 			this.left_button.setVisibility(View.VISIBLE);
 			this.clef_image.setVisibility(View.INVISIBLE);
 			this.meter_disp.setVisibility(View.INVISIBLE);
 		} else {
 			this.left_button.setVisibility(View.INVISIBLE);
 			this.clef_image.setVisibility(View.VISIBLE);
 			this.meter_disp.setVisibility(View.VISIBLE);
 		}
 		// show the right button if there are more notes off the page.
 		if (this.song.size() >= (this.screen_number+1) * MAX_NOTES_ONSCREEN) {
 			this.right_button.setVisibility(View.VISIBLE);
 		} else {
 			this.right_button.setVisibility(View.INVISIBLE);
 		}
 		// before displaying anything, we hardcore clear the screen.
 		unclickAll();
 		for (View v : notes) {
 			group.removeView(v);
 		}
 		for (View v : annotations) {
 			group.removeView(v);
 		}
 		this.notes = new View[MAX_NOTES_ONSCREEN];
 		this.annotations = new View[MAX_NOTES_ONSCREEN];
 		
 		// we do this by duplicating and hopefully replacing all of the
 		// uploadFromSong code.
 		int g = 0;
 		while (g < MAX_NOTES_ONSCREEN) {
 			Note n = this.song.getNoteNum(g + this.screen_number
 					* MAX_NOTES_ONSCREEN);
 			if (n == null) {
 				break;
 			}
 			Log.d("", "adding note");
 			double freq = n.getPitch();
 			double l = n.getLength();
 			double x = 20 + (g + 1) * 60;
 			Log.d("", "" + x);
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
 				left_note.performClick();
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				notes[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			} else if (l == 0.25) {
 				middle_note.performClick();
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				notes[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			} else if (l == 0.5) {
 				right_note.performClick();
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				notes[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			}
 			str = NoteFrequencies.freqToString.get(freq);
 			Log.d("", "str value: " + str);
 			if (str.endsWith("sharp")) {
 				Log.wtf("", "Clicking sharp");
 				sharp_button.performClick();
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				annotations[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			} else if (str.endsWith("flat")) {
 				flat_button.performClick();
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				annotations[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			} else if (str.endsWith("natural")) {
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				annotations[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			}
 			g++;
 		}
 	}
 
 	@Deprecated
 	// Replace with generateScreen
 	public void uploadFromSong(Song s) {
 		int g = 0;
 		for (Note n : s.getNotes().getNotes()) {
 			Log.d("", "adding note");
 			double freq = n.getPitch();
 			double l = n.getLength();
 			double x = 20 + (g + 1) * 60;
 			Log.d("", "" + x);
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
 				left_note.performClick();
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				notes[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			} else if (l == 0.25) {
 				middle_note.performClick();
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				notes[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			} else if (l == 0.5) {
 				right_note.performClick();
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				notes[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			}
 			str = NoteFrequencies.freqToString.get(freq);
 			Log.d("", "str value: " + str);
 			if (str.endsWith("sharp")) {
 				Log.wtf("", "Clicking sharp");
 				sharp_button.performClick();
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				annotations[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			} else if (str.endsWith("flat")) {
 				flat_button.performClick();
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				annotations[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			} else if (str.endsWith("natural")) {
 				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 						RelativeLayout.LayoutParams.WRAP_CONTENT,
 						RelativeLayout.LayoutParams.WRAP_CONTENT);
 				params.topMargin = (int) y - 30;
 				params.leftMargin = 20 + (g + 1) * 60;
 				// remove any previous annotation there
 				annotations[g] = selected_view;
 				selected_view.setLayoutParams(params);
 				group.removeView(selected_view);
 				group.addView(selected_view);
 				selected_view.setTag(type_selected);
 				state = states.wait;
 				unclickAll();
 			}
 			g++;
 		}
 	}
 
 	public void unclickAll() {
 		Log.d("", "Unclicking all");
 		left_note.setImageResource(R.drawable.eigth_note);
 		middle_note.setImageResource(R.drawable.quarter_note);
 		right_note.setImageResource(R.drawable.half_note);
 		sharp_button.setImageResource(R.drawable.sharp);
 		flat_button.setImageResource(R.drawable.flat);
 		natural_button.setImageResource(R.drawable.natural);
 		selected_view = null;
 		type_selected = null;
 		for (View v : notes) {
 			if (v != null) {
 				v.setBackgroundColor(Color.TRANSPARENT);
 			}
 		}
 		for (View v : annotations) {
 			if (v != null) {
 				v.setBackgroundColor(Color.TRANSPARENT);
 			}
 		}
 	}
 
 	public View select(int res) {
 		state = states.selected_to_place;
 		ImageView img = new ImageView(getApplicationContext());
 		img.setImageResource(res);
 		Log.d("", "Selected_view: " + res);
 		img.setAdjustViewBounds(true);
 		img.setMaxHeight(65);
 		img.setMaxWidth(50);
 		img.setVisibility(0);
 		return img;
 	}
 
 	public int nextAnnotation() {
 		for (int i = 0; i < this.annotations.length; i++) {
 			if (annotations[i] == null) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	public int nextNote() {
 		for (int i = 0; i < this.notes.length; i++) {
 			if (notes[i] == null) {
 				if (i == this.notes.length-1) {
 					this.right_button.setVisibility(View.VISIBLE);
 				}
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	public boolean hit(int x, int y, Rect hitSpace) {
 		if (x > hitSpace.left && x < hitSpace.right && y < hitSpace.bottom
 				&& y > hitSpace.top) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onBackPressed() {
		save();
 		finish();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
		save();
 	}
 
 	@Deprecated
 	// Replace with savePage
 	public void save() {
 		// put information into song
 		if (song != null) {
 			song = new Song(song.getClef(), song.getMeterTop(),
 					song.getMeterBottom(), song.getTempo(), song.getKey(),
 					song.getTitle(), new Date());
 		} else {
 			song = new Song();
 		}
 		for (int i = 0; i < this.notes.length; i++) {
 			Note n = getNoteFromIndex(i);
 			Log.d("",
 					"note at index: " + i + " is null? "
 							+ Boolean.toString(n == null));
 			if (n != null) {
 				Log.d("", "note at index: " + i + " is " + n.getName());
 				song.addNote(n);
 			}
 		}
 		// put song into return intent
 		Intent i = new Intent();
 		i.putExtra("song object", song);
 		setResult(0, i);
 	}
 
 	public void savePage() {
 		if (song != null) {
 		} else {
 			song = new Song();
 		}
 		for (int i = 0; i < notes.length; i++) {
 			Note n = getNoteFromIndex(i);
 			Log.d("",
 					"note at index: " + i + " is null? "
 							+ Boolean.toString(n == null));
 			if (n != null) {
 				Log.d("", "note at index: " + i + " is " + n.getName());
 				song.setNote(MAX_NOTES_ONSCREEN * this.screen_number + i, n);
 			}
 		}
 		// put song into return intent
 		Intent i = new Intent();
 		i.putExtra("song object", song);
 		setResult(0, i);
 	}
 
 	public void addClefMeterKey(Song s) {
 		if (s == null) {
 			return;
 		}
 		int clef = s.getClef();
 		if (clef == 1) {
 			((ImageView) findViewById(R.id.clef_image))
 					.setImageResource(R.drawable.treble_clef);
 		}
 		meter_disp.setText("" + s.getMeterTop() + "\n" + s.getMeterBottom());
 	}
 
 	public enum edit_menu_options {
 		DELETE, SAVE, CLOSE, PLAY,
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.edit_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.edit_close:
 			Toast.makeText(this, "Closing!", Toast.LENGTH_SHORT).show();
			save();
 			finish();
 			break;
 		case R.id.edit_play:
			save();
 			Toast.makeText(this, "Play...", Toast.LENGTH_SHORT).show();
 			Intent next = new Intent(EditModeLegacy.this,
 					PlaybackModeLegacy.class);
 			next.putExtra("song object", song);
 			startActivity(next);
 			break;
 		case R.id.edit_save:
 			Toast.makeText(this, "Save...", Toast.LENGTH_SHORT).show();
			save();
 			break;
 		case R.id.edit_delete:
 			Toast.makeText(this, "Deleting...", Toast.LENGTH_SHORT).show();
 			createAreYouSure();
 			break;
 		}
 		return false;
 	}
 
 	public void createAreYouSure() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Are you sure??")
 				.setCancelable(true)
 				.setPositiveButton("Yes!",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								Intent i = new Intent();
 								i.putExtra("song object", song);
 								setResult(1, i);// 1 is delete
 								finish();
 							}
 						})
 				.setNegativeButton("No!",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public void createDynamicsDialog() {
 		final CharSequence[] dynamics = { "pp", "p", "mp", "mf", "f", "ff" };
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Choose New Dynamic");
 		builder.setItems(dynamics, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				((Button) findViewById(R.id.dynamics_button))
 						.setText(dynamics[item]);
 			}
 		});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 
 	}
 
 	public void createClefDialog() {
 		final CharSequence[] clefs = { "Bass", "Treble" };
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Choose New Clef");
 		builder.setItems(clefs, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				song.setClef(item);
 				if (item == 0) {
 					((ImageView) findViewById(R.id.clef_image))
 							.setImageResource(R.drawable.bass_clef);
 				} else {
 					((ImageView) findViewById(R.id.clef_image))
 							.setImageResource(R.drawable.treble_clef);
 				}
 			}
 		});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public Note getNoteFromIndex(int i) {
 		double pitch = 0;
 		double length = 0;
 		String name = "";
 		String annotation = "";
 		if (notes[i] != null) {
 			if (annotations[i] != null) {
 				if (((ImageView) annotations[i]).getTag() == placement_objects.flat) {
 					annotation = "flat";
 				} else if (((ImageView) annotations[i]).getTag() == placement_objects.sharp) {
 					annotation = "sharp";
 				}
 			}
 			name = NoteFrequencies.staff_notes[0] + annotation;
 			for (int j = 0; j < NoteFrequencies.staff_lines.length; j++) {
 				if (NoteFrequencies.staff_lines[j] > ((ImageView) notes[i])
 						.getTop() + 25) {
 					name = NoteFrequencies.staff_notes[j] + annotation;
 				}
 			}
 			Log.d("", "Note name: " + name);
 			if (!NoteFrequencies.frequencyMap.containsKey(name)) {
 				pitch = 440;
 			} else {
 				pitch = NoteFrequencies.frequencyMap.get(name).doubleValue();
 			}
 			Log.d("", "Type of note: " + ((ImageView) notes[i]).getTag());
 			if (((ImageView) notes[i]).getTag() == placement_objects.left_note) {
 				length = 0.125;
 			} else if (((ImageView) notes[i]).getTag() == placement_objects.middle_note) {
 				length = 0.25;
 			} else if (((ImageView) notes[i]).getTag() == placement_objects.right_note) {
 				length = 0.5;
 			}
 			return new Note(pitch, length, name);
 		}
 		return null;
 	}
 }
