 package info.dreamingfish123.wavetransdemo;
 
 import info.dreamingfish123.WaveTransProto.codec.Constant;
 import info.dreamingfish123.WaveTransProto.codec.Util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioRecord;
 import android.media.AudioTrack;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
 
 	private final static String TAG = "MAIN";
 	private AudioTrack sender = null;
 	private RecordTestRunnable recordTestRunnable = null;
 	private boolean isRecording = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// int minBuf = AudioTrack.getMinBufferSize(44100,
 		// AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
 		// Log.d(TAG, "min buffer size:" + minBuf);
 		// sender = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
 		// AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT,
 		// minBuf * 10, AudioTrack.MODE_STREAM);
 		// sender.play();
 
 		setEvents();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onDestroy() {
 		// sender.stop();
 		// sender.release();
 		if (sender != null) {
 			sender.stop();
 			sender.release();
 		}
 		stopRecord();
 		super.onDestroy();
 	}
 
 	private void setEvents() {
 		Button playButton = (Button) findViewById(R.id.button1);
 		playButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				playSample();
 			}
 		});
 
 		final Button recordButton = (Button) findViewById(R.id.Button01);
 		recordButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (isRecording) {
 					isRecording = false;
 					stopRecord();
 					recordButton.setText("Start Record");
 				} else {
 					isRecording = true;
 					startRecord();
 					recordButton.setText("Stop Record");
 				}
 			}
 		});
 	}
 
 	private void playSample() {
 		try {
 			InputStream is = getResources().getAssets().open("sample.wav");
 			byte[] wavein = new byte[Constant.WAVEOUT_BUF_SIZE];
 			int read = is.read(wavein);
 			is.close();
 
 			Log.d(TAG, "buffer size:" + (read - Constant.WAVE_HEAD_LEN));
 			if (sender != null) {
 				sender.release();
 			}
 
 			sender = new AudioTrack(AudioManager.STREAM_MUSIC,
 					Constant.WAVE_RATE_INHZ, AudioFormat.CHANNEL_OUT_MONO,
 					AudioFormat.ENCODING_PCM_8BIT,
 					Constant.WAVEOUT_BUF_SIZE * 2, AudioTrack.MODE_STATIC);
 			sender.write(wavein, Constant.WAVE_HEAD_LEN, read
 					- Constant.WAVE_HEAD_LEN);
 			sender.play();
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void startRecord() {
 		if (recordTestRunnable == null) {
 			recordTestRunnable = new RecordTestRunnable();
 			Thread thread = new Thread(recordTestRunnable);
 			thread.start();
 		}
 	}
 
 	private void stopRecord() {
 		if (recordTestRunnable != null) {
 			recordTestRunnable.isRunning = false;
 			recordTestRunnable = null;
 		}
 	}
 
 	class RecordTestRunnable implements Runnable {
 
 		public AudioRecord recorder = null;
 		int bufferSize;
 		public boolean isRunning = false;
 		public String fileName = null;
 
 		@Override
 		public void run() {
 			isRunning = true;
 
 			bufferSize = AudioRecord
 					.getMinBufferSize(Constant.WAVE_RATE_INHZ,
 							AudioFormat.CHANNEL_IN_MONO,
 							AudioFormat.ENCODING_PCM_16BIT);
 			System.out.println("BufferSize:" + bufferSize);
 			byte[] buffer = new byte[bufferSize];
 			FileOutputStream fos = null;
 			int readSize = 0;
 			fileName = Environment.getExternalStorageDirectory() + "/wavein_"
 					+ Calendar.getInstance().getTimeInMillis();
 			File file = new File(fileName + ".raw");
 			try {
 				fos = new FileOutputStream(file);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 				return;
 			}
 
 			startRecord();
 			while (isRunning) {
 				readSize = recorder.read(buffer, 0, bufferSize);
 				if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
 					try {
 						fos.write(buffer, 0, readSize);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			stopRecord();
 
 			try {
 				fos.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			writeWaveFile();
 		}
 
 		public void startRecord() {
 			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
 					Constant.WAVE_RATE_INHZ, AudioFormat.CHANNEL_IN_MONO,
 					AudioFormat.ENCODING_PCM_16BIT, bufferSize);
 			recorder.startRecording();
 		}
 
 		public void stopRecord() {
 			if (recorder != null) {
 				recorder.stop();
 				recorder.release();
 				recorder = null;
 			}
 		}
 
 		public void writeWaveFile() {
 			FileInputStream fis = null;
 			FileOutputStream fos = null;
 			byte[] buffer = new byte[bufferSize];
 			byte[] header = new byte[Constant.WAVE_HEADER.length];
 			System.arraycopy(Constant.WAVE_HEADER, 0, header, 0,
 					Constant.WAVE_HEADER.length);
 			int readSize = 0;
 			try {
 				fis = new FileInputStream(fileName + ".raw");
 				fos = new FileOutputStream(fileName + ".wav");
 				int dataLen = (int) fis.getChannel().size();
 				Util.int2byte(dataLen, header, Constant.WAVE_DATA_LEN_OFFSET);
 				Util.int2byte(dataLen + 36, header,
 						Constant.WAVE_FILE_LEN_OFFSET);
 				header[34] = 0x10;
 				fos.write(header);
 
 				readSize = fis.read(buffer);
 				while (readSize != -1) {
 					fos.write(buffer, 0, readSize);
 					readSize = fis.read(buffer);
 				}
 
 				fis.close();
 				fos.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	class PlayTestRunnable implements Runnable {
 
 		public boolean isRunning = false;
 		public InputStream is = null;
 		public int repeatTimes = 1;
 		public long sleepInterval = 0l;
 		byte[] wavein = new byte[Constant.WAVEOUT_BUF_SIZE];
 
 		public PlayTestRunnable(InputStream inputStream, int repeat, long sleep) {
 			is = inputStream;
 			repeatTimes = repeat;
 			sleepInterval = sleep;
 		}
 
 		@Override
 		public void run() {
 			isRunning = true;
 
 			while (isRunning) {
 
 			}
 		}
 
 		public void prepareWavein() {
 
 		}
 
 	}
 
 }
