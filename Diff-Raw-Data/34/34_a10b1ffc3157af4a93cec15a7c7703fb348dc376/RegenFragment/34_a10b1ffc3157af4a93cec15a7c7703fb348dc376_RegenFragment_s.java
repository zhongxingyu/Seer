 package de.naglfar.regenradar;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import de.naglfar.regenradar.MainActivity.RadarTime;
 
 public final class RegenFragment extends SherlockFragment {
 
 	private static int TIMER_DELAY = 1000;
 
 	private boolean started = false;
 	private ArrayList<RadarTime> images;
 	private SeekBar seekBar;
 	private ImageButton btn_playpause;
 
 	private ImageView imageView;
 	private TextView textView;
 
 	private Integer currentPosition;
 
 	class TimeState {
 		private Bitmap bitmap;
 		private String label;
 		public TimeState(Bitmap bitmap, String label) {
 			this.bitmap = bitmap;
 			this.label = label;
 		}
 		public Bitmap getBitmap() {
 			return bitmap;
 		}
 		public String getLabel() {
 			return label;
 		}
 	}
 	ArrayList<TimeState> timeStates;
 
 	public static RegenFragment newInstance(ArrayList<RadarTime> values) {
 		Log.v("RegenFragment", "New instance!");
 		RegenFragment fragment = new RegenFragment();
 		fragment.images = values;
 
 		return fragment;
 	}
 
 	public void setValue(ArrayList<RadarTime> values) {
 		this.images = values;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (savedInstanceState != null) {
 			RadarTime[] values = (RadarTime[]) savedInstanceState.getParcelableArray("images");
 			images = new ArrayList<RadarTime>(Arrays.asList(values));
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		View view = inflater.inflate(R.layout.activity_main_page, null);
 
 		seekBar = (SeekBar) view.findViewById(R.id.regen_seekBar);
 		btn_playpause = (ImageButton) view.findViewById(R.id.regen_btn_playpause);
 
 		imageView = (ImageView)view.findViewById(R.id.regen_imageView);
 		textView = (TextView) view.findViewById(R.id.regen_textView);
 
 
 		// FIXME
 		start();
 
 		return view;
 	}
 
 	public void start() {
 		//Log.v("Fragment", "Start!");
 
 		started = true;
 		if (images != null && images.size() > 0) {
 
 			timeStates = new ArrayList<RegenFragment.TimeState>();
 
 			seekBar.setMax(images.size()-1);
 
 			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 				private Integer start = null;
 
 				@Override
 				public void onStartTrackingTouch(SeekBar seekBar) {
 					start = seekBar.getProgress();
 				}
 
 				@Override
 				public void onStopTrackingTouch(SeekBar seekBar) {
 					start = null;
 				}
 
 				@Override
 				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 					if (start != null) {
 						timerRunnable.setRun(false);
 						setState(progress);
 					}
 				}
 			});
 
 			btn_playpause.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					boolean set = !timerRunnable.run;
 					timerRunnable.setRun(set);
 					if (set == true) {
 						timerHandler.postDelayed(timerRunnable, 0);
 					}
 				}
 			});
 
 			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
 
 			for (int i = 0; i < images.size(); i += 1) {
 				Bitmap bmp = null;
 				File imgFile = new File(getActivity().getCacheDir() + File.separator + images.get(i).name);
 				if (imgFile.exists()) {
 					bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
 				} else {
 					Log.v("RegenFragment", "Missing Image!! "+ images.get(i).name);
 				}
 				String label = sdf.format(images.get(i).date);
 				TimeState ts = new TimeState(bmp, label);
 				timeStates.add(ts);
 			}
 
 			Callback cb = new Callback() {
 				@Override
 				public void onCallBack() {
 					setNextState();
 				}
 			};
 
 			setState(0);
 
 			if (images.size() > 1) {
 				btn_playpause.setImageResource(R.drawable.ic_pause);
 				if (timerRunnable != null) {
 					timerRunnable.setRun(false);
 					timerRunnable = null;
 				}
 				timerRunnable = new TimerRunnable(cb);
				timerRunnable.setRun(true);
 				timerHandler.postDelayed(timerRunnable, TIMER_DELAY);
 			} else {
 				seekBar.setVisibility(View.GONE);
 				btn_playpause.setVisibility(View.GONE);
 				cb.onCallBack();
 			}
 		} else {
 			seekBar.setVisibility(View.GONE);
 			btn_playpause.setVisibility(View.GONE);
 		}
 	}
 
 	public void stop() {
 		//Log.v("Fragment", "Stop!");
 		started = false;
 		if (timerRunnable != null) {
 			timerRunnable.setRun(false);
 			timerRunnable = null;
 			setState(0);
 		}
 	}
 
 	public boolean getStarted() {
 		return started;
 	}
 
 	public void setState(int position) {
 		currentPosition = position;
 
 		//Log.v("CURRENTSTATE", ""+position+"-"+images.size());
 
 		TimeState ts = timeStates.get(position);
 		imageView.setImageBitmap(ts.getBitmap());
 		textView.setText(ts.getLabel());
 
 		seekBar.setProgress(position);
 	}
 	public void setNextState() {
 		if (currentPosition == null || currentPosition+1 >= timeStates.size()) {
 			setState(0);
 		} else {
 			setState(currentPosition+1);
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		RadarTime[] values = new RadarTime[images.size()];
 		images.toArray(values);
 		outState.putParcelableArray("images", values);
 	}
 
	/*
 	@Override
 	public void onPause() {
 		super.onPause();
		Log.v("Fragment", "PAUSE");
 		if (timerRunnable != null) {
 			timerRunnable.setRun(false);
 		}
 	}
 	@Override
 	public void onResume() {
 		super.onResume();
		Log.v("Fragment", "RESUME");
 		if (timerRunnable != null  && timerRunnable.run == false) {
 			timerRunnable.setRun(true);
 			timerHandler.postDelayed(timerRunnable, 0);
 		}
 	}
	*/
 
 	private Handler timerHandler = new Handler();
 	private TimerRunnable timerRunnable;
 	class TimerRunnable implements Runnable {
 		Callback cb;
 		Boolean run = true;
 		public TimerRunnable(Callback cb) {
 			this.cb = cb;
 		}
 		public void setRun(Boolean run) {
 			this.run = run;
 			if (run == true) {
 				btn_playpause.setImageResource(R.drawable.ic_pause);
 			} else {
 				btn_playpause.setImageResource(R.drawable.ic_play);
 			}
 		}
 		@Override
 		public void run() {
 			if(run == true) {
 				cb.onCallBack();
 				timerHandler.postDelayed(this, TIMER_DELAY);
 			}
 		}
 	}
 	interface Callback {
 		abstract void onCallBack();
 	}
 
 }
