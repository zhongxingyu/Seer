 package com.michalkazior.simplemusicplayer;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import com.michalkazior.simplemusicplayer.Player.State;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnClickListener;
 import android.view.View.OnCreateContextMenuListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 /**
  * Main activity.
  * 
  * The user may manage the list of enqueued songs, play, pause.
  */
 public class Main extends Activity {
 	private Button playButton, skipButton;
 	private TextView songTime;
 	private SeekBar songSeekBar;
 	private ListView enqueuedSongs;
 	private Song[] songs = {};
 	private Player.State state = State.IS_STOPPED;
 	private Song selectedSong = null;
 	private boolean doNotStopService = false;
 
 	/*
 	 * Position of the seekbar before user started draging.
 	 * 
 	 * -1 means the user is not dragging the seekbar.
 	 */
 	private int oldSeekBarPosition = -1;
 
 	/**
 	 * Context menu for enqueued list items
 	 */
 	private enum ContextMenu {
 		PLAY_NOW {
 			@Override
 			public void call(Main m) {
 				if (m.songs.length > 0 && m.songs[0] != m.selectedSong) {
 					m.sendBroadcast(Player.Remote.Request.RemoveSong.getIntent().putExtra("song",
 							m.selectedSong));
 					m.sendBroadcast(Player.Remote.Request.EnqueueSong
 							.getIntent()
 							.putExtra("song", m.selectedSong)
 							.putExtra("index", 0));
 				}
 			}
 
 			@Override
 			public int getLabelId() {
 				return R.string.context_menu_play_now;
 			}
 		},
 		PLAY_NEXT {
 			@Override
 			public void call(Main m) {
 				m.sendBroadcast(Player.Remote.Request.RemoveSong.getIntent().putExtra("song",
 						m.selectedSong));
 				m.sendBroadcast(Player.Remote.Request.EnqueueSong
 						.getIntent()
 						.putExtra("song", m.selectedSong)
 						.putExtra("index", 1));
 			}
 
 			@Override
 			public int getLabelId() {
 				return R.string.context_menu_play_next;
 			}
 		},
 		REMOVE {
 			@Override
 			public void call(Main m) {
 				m.sendBroadcast(Player.Remote.Request.RemoveSong.getIntent().putExtra("song",
 						m.selectedSong));
 			}
 
 			@Override
 			public int getLabelId() {
 				return R.string.context_menu_remove;
 			}
 		},
 		MOVE_UP {
 			@Override
 			public void call(Main m) {
 				m.sendBroadcast(Player.Remote.Request.MoveSong
 						.getIntent()
 						.putExtra("song", m.selectedSong)
 						.putExtra("offset", -1));
 			}
 
 			@Override
 			public int getLabelId() {
 				return R.string.context_menu_move_up;
 			}
 		},
 		MOVE_DOWN {
 			@Override
 			public void call(Main m) {
 				m.sendBroadcast(Player.Remote.Request.MoveSong
 						.getIntent()
 						.putExtra("song", m.selectedSong)
 						.putExtra("offset", 1));
 			}
 
 			@Override
 			public int getLabelId() {
 				return R.string.context_menu_move_down;
 			}
 		},
 		CLONE {
 			@Override
 			public void call(Main m) {
 				m.sendBroadcast(Player.Remote.Request.EnqueueSong.getIntent().putExtra("song",
 						m.selectedSong));
 			}
 
 			@Override
 			public int getLabelId() {
 				return R.string.context_menu_clone;
 			}
 		};
 		public abstract void call(Main m);
 
 		public abstract int getLabelId();
 
 		public static final void generate(Menu menu) {
 			for (ContextMenu item : ContextMenu.values()) {
 				menu.add(0, item.ordinal(), 0, item.getLabelId());
 			}
 		}
 
 		public static final void run(Main main, MenuItem item) {
 			values()[item.getItemId()].call(main);
 		}
 	};
 
 	/**
 	 * Option menu (pops up on Menu button press)
 	 */
 	private enum OptionMenu {
 		REMOVE_ALL {
 			@Override
 			public int getLabelId() {
 				return R.string.option_menu_remove_all;
 			}
 
 			@Override
 			public void call(Main m) {
 				new AlertDialog.Builder(m)
 						.setMessage(R.string.dialog_are_you_sure)
 						.setPositiveButton(R.string.dialog_yes, new DialogOk(m))
 						.setNegativeButton(R.string.dialog_no, null)
 						.show();
 			}
 
 			class DialogOk implements DialogInterface.OnClickListener {
 				private Main m;
 
 				public DialogOk(Main m) {
 					super();
 					this.m = m;
 				}
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					for (int i = 1; i < m.songs.length; ++i) {
 						m.sendBroadcast(Player.Remote.Request.RemoveSong.getIntent().putExtra(
 								"song", m.songs[i]));
 					}
 				}
 			};
 		},
 		SHUFFLE {
 			@Override
 			public int getLabelId() {
 				return R.string.option_menu_shuffle;
 			}
 
 			@Override
 			public void call(Main m) {
 				new AlertDialog.Builder(m)
 						.setMessage(R.string.dialog_are_you_sure)
 						.setPositiveButton(R.string.dialog_yes, new DialogOk(m))
 						.setNegativeButton(R.string.dialog_no, null)
 						.show();
 			}
 
 			class DialogOk implements DialogInterface.OnClickListener {
 				private Main m;
 
 				public DialogOk(Main m) {
 					super();
 					this.m = m;
 				}
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					/*
 					 * Remove all songs, shuffle locally and then re-add.
 					 */
 					ArrayList<Song> songs = new ArrayList<Song>();
 
 					for (int i = 1; i < m.songs.length; ++i) {
 						m.sendBroadcast(Player.Remote.Request.RemoveSong.getIntent().putExtra(
 								"song", m.songs[i]));
 						songs.add(m.songs[i]);
 					}
 
 					Collections.shuffle(songs);
 
 					for (Song song : songs) {
 						m.sendBroadcast(Player.Remote.Request.EnqueueSong.getIntent().putExtra(
 								"song", song));
 					}
 				}
 			};
 		},
 		ENQUEUE_NEW {
 			@Override
 			public int getLabelId() {
 				return R.string.option_menu_enqueue_new;
 			}
 
 			@Override
 			public void call(Main m) {
 				m.startActivity(new Intent(m, Songlist.class));
 			}
 		};
 		public abstract int getLabelId();
 
 		public abstract void call(Main m);
 
 		public static void generate(Menu menu) {
 			for (OptionMenu item : values()) {
 				menu.add(0, item.ordinal(), 0, item.getLabelId());
 			}
 		}
 
 		public static void run(Main main, MenuItem item) {
 			values()[item.getItemId()].call(main);
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		playButton = (Button) findViewById(R.id.playButton);
 		skipButton = (Button) findViewById(R.id.skipButton);
 		songTime = (TextView) findViewById(R.id.songTime);
 		songSeekBar = (SeekBar) findViewById(R.id.songSeekBar);
 		enqueuedSongs = (ListView) findViewById(R.id.enqueuedSongs);
 
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				int duration = intent.getIntExtra("duration", 0);
 				int position = intent.getIntExtra("position", 0);
 				state = (State) intent.getSerializableExtra("state");
 
 				switch (state) {
 					case IS_STOPPED:
 						songSeekBar.setMax(0);
 						songSeekBar.setProgress(0);
 						songTime.setText("");
 						break;
 					case IS_PLAYING:
 						songTime.setText(String.format("%d:%02d / %d:%02d (%d%%)",
 								(position / 1000) / 60, (position / 1000) % 60,
 								(duration / 1000) / 60, (duration / 1000) % 60,
 								Math.round(100 * position / duration)));
 						if (oldSeekBarPosition == -1) {
 							songSeekBar.setMax(duration);
 							songSeekBar.setProgress(position);
 						}
 						playButton.setText(R.string.button_pause);
 						break;
 					case IS_PAUSED:
 						playButton.setText(R.string.button_play);
 						break;
 				}
 			}
 		}, Player.Remote.Reply.State.getIntentFilter());
 
 		/*
 		 * Update enqueued songs listview upon Reply.EnqueuedSongs
 		 */
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				songs = Player.parcelableArrayToSongs(intent.getParcelableArrayExtra("songs"));
 				enqueuedSongs.setAdapter(new MainSongAdapter(
 						getApplicationContext(),
 						R.layout.listitem,
 						songs));
 			}
 		}, Player.Remote.Reply.EnqueuedSongs.getIntentFilter());
 
 		/*
 		 * If the service isn't running yet, the broadcast will be ignored.
 		 */
 		sendBroadcast(Player.Remote.Request.GetAvailableSongs.getIntent());
 		sendBroadcast(Player.Remote.Request.GetEnqueuedSongs.getIntent());
 		sendBroadcast(Player.Remote.Request.GetState.getIntent());
 		startService(new Intent(this, Player.class));
 
 		playButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				switch (state) {
 					case IS_PLAYING:
 						sendBroadcast(Player.Remote.Request.Stop.getIntent());
 						break;
 					case IS_STOPPED:
 					case IS_PAUSED:
 						sendBroadcast(Player.Remote.Request.Play.getIntent());
 						break;
 				}
 			}
 		});
 
 		skipButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				sendBroadcast(Player.Remote.Request.PlayNext.getIntent());
 			}
 		});
 
 		/*
 		 * We send a seek request when the user has lifted his finger.
 		 */
 		songSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			private int lastProgress = 0;
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				oldSeekBarPosition = -1;
 				sendBroadcast(Player.Remote.Request.Seek.getIntent().putExtra("position",
 						lastProgress));
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				oldSeekBarPosition = seekBar.getProgress();
 			}
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				if (fromUser) {
 					lastProgress = progress;
 				}
 			}
 		});
 
 		enqueuedSongs.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
 			@Override
 			public void onCreateContextMenu(android.view.ContextMenu menu, View v,
 					ContextMenuInfo menuInfo) {
 				AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 				selectedSong = (Song) enqueuedSongs.getItemAtPosition(info.position);
 				ContextMenu.generate(menu);
 			}
 		});
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		ContextMenu.run(this, item);
 		return super.onContextItemSelected(item);
 	}
 
 	@Override
 	protected void onDestroy() {
 		/*
 		 * When the playback is stopped we destroy the backend service.
 		 * 
 		 * This means we'll lose enqueued songs list of course.
 		 */
 		switch (state) {
 			case IS_ON_HOLD_BY_CALL:
 			case IS_ON_HOLD_BY_HEADSET:
 			case IS_PAUSED:
 			case IS_STOPPED:
 				if (!doNotStopService) stopService(new Intent(this, Player.class));
 				break;
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		OptionMenu.generate(menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		OptionMenu.run(this, item);
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		/*
 		 * If the user does not explicitly close (with the Back button) the UI,
 		 * we cannot kill the Service.
 		 * 
		 * Otherwise the Player Service will be stopped when screen orientation
		 * restarts the Main Activity.
 		 */
 		doNotStopService = true;
 		super.onSaveInstanceState(outState);
 	}
 
 	private class MainSongAdapter extends SongAdapter {
 		public MainSongAdapter(Context context, int textViewResourceId, Song[] objects) {
 			super(context, textViewResourceId, objects);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = super.getView(position, convertView, parent);
 			if (v != null) {
 				if (position == 0) v.setBackgroundDrawable(getResources().getDrawable(
 						R.drawable.listitem_selector_first));
 				else v.setBackgroundDrawable(getResources().getDrawable(
 						R.drawable.listitem_selector));
 			}
 			return v;
 		}
 	};
 }
