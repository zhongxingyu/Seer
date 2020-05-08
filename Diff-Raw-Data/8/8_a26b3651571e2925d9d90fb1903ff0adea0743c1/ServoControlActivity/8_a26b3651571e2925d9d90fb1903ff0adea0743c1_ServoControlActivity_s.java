 package net.mitchtech.ioio;
 
 import ioio.lib.api.PwmOutput;
 import ioio.lib.api.exception.ConnectionLostException;
 import ioio.lib.util.AbstractIOIOActivity;
 import net.mitchtech.ioio.servocontrol.R;
 import android.os.Bundle;
 import android.widget.SeekBar;
 
 public class ServoControlActivity extends AbstractIOIOActivity {
 	private final int PAN_PIN = 3;
 	private final int TILT_PIN = 6;
 	
 	private final int PWM_FREQ = 100;
 	
 	private SeekBar mPanSeekBar;
 	private SeekBar mTiltSeekBar;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		mPanSeekBar = (SeekBar) findViewById(R.id.panSeekBar);
 		mTiltSeekBar = (SeekBar) findViewById(R.id.tiltSeekBar);
 		
 		enableUi(false);
 	}
 
 	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
 		private PwmOutput panPwmOutput;
 		private PwmOutput tiltPwmOutput;
 
 		public void setup() throws ConnectionLostException {
 			try {
				panPwmOutput = ioio_.openPwmOutput(PAN_PIN, PWM_FREQ);
				tiltPwmOutput = ioio_.openPwmOutput(TILT_PIN, PWM_FREQ);
 				enableUi(true);
 			} catch (ConnectionLostException e) {
 				enableUi(false);
 				throw e;
 			}
 		}
 
 		public void loop() throws ConnectionLostException {
 			try {
 				panPwmOutput.setPulseWidth(500 + mPanSeekBar.getProgress() * 2);
 				tiltPwmOutput.setPulseWidth(500 + mTiltSeekBar.getProgress() * 2);
 				sleep(10);
 			} catch (InterruptedException e) {
 				ioio_.disconnect();
 			} catch (ConnectionLostException e) {
 				enableUi(false);
 				throw e;
 			}
 		}
 	}
 	
 	@Override
 	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
 		return new IOIOThread();
 	}
 
 	private void enableUi(final boolean enable) {
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				mPanSeekBar.setEnabled(enable);
 				mTiltSeekBar.setEnabled(enable);
 			}
 		});
 	}
 
}
