 package com.makewithmoto.example;
 
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import ioio.lib.api.Uart;
 import ioio.lib.api.PwmOutput;
 import ioio.lib.api.AnalogInput;
 import ioio.lib.api.DigitalOutput;
 import ioio.lib.api.DigitalInput;
 import ioio.lib.api.exception.ConnectionLostException;
 import ioio.lib.util.BaseIOIOLooper;
 import ioio.lib.util.IOIOLooper;
 import ioio.lib.util.android.IOIOActivity;
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.makewithmoto.makr.views.PlotView;
 import com.makewithmoto.makr.views.PlotView.Plot;
 
  /*
  * Example using the MOIO board 
  */
 
 @SuppressLint("NewApi")
 public class ActivityMOIO extends IOIOActivity {
 	
 	// this is the file that is accessed to turn the MOIO on and off
 	private static final String MAKR_ENABLE = "/sys/class/makr/makr/5v_enable";
 	
 	Button uartTxSend;
 	EditText uartTxData;
 	RadioButton ledon, ledoff;
 	TextView buttonread, uartReceive;
 	SeekBar pwmcontrol;
 	ActionBar actionBar;
 	PlotView graphView;
 	Plot p1;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_ioio);
 		
 		// turn the MOIO on
 		enable(true);
 		
 		//uartReceive = (TextView) findViewById(R.id.);
		uartTxSend = (Button) findViewById(R.id.button1);
		uartTxData = (EditText) findViewById(R.id.editText1);
 		pwmcontrol = (SeekBar) findViewById(R.id.seekBar1);
 		buttonread = (TextView) findViewById(R.id.pushbuttonTextView);
 		ledon = (RadioButton) findViewById(R.id.ledon);
 		ledoff = (RadioButton) findViewById(R.id.ledoff);
 		graphView = (PlotView) findViewById(R.id.plotView1);
 		p1 = graphView.new Plot(Color.RED);
 		graphView.addPlot(p1);
 		graphView.setLimits(-10, 10); //TODO fix this, setLimits doesn't work
 		
 		actionBar = getActionBar();
 		actionBar.setDisplayUseLogoEnabled(false);
 		actionBar.setHomeButtonEnabled(false);
 		actionBar.setLogo(null);
 		actionBar.setTitle("MakeWithMoto");
 
 	}
 
 	class Looper extends BaseIOIOLooper {
 		
 		/* create instances for each interface */
 		private DigitalOutput led_;
 		private AnalogInput analogin_;
 		private DigitalInput pushbutton_;
 		private PwmOutput pwm_;
 		private Uart sendreceive_;
 		private InputStream rx_;
 		private OutputStream tx_;
 
 		/**
 		 * Called every time a connection with MOIO has been established.
 		 * Typically used to open pins.
 		 */
 		@Override
 		protected void setup() throws ConnectionLostException {
 			
 			// notifies the user when connected to the MOIO, displays a toast that reads 'READY!!!'
 			on_notify();
 			
 			// initialize all of the interface options with specific pin numbers
 			analogin_ = ioio_.openAnalogInput(31); //pin31, must not be more than 3.3V	
 			led_ = ioio_.openDigitalOutput(0, true); // start with the on board LED off
 			pushbutton_ = ioio_.openDigitalInput(1, DigitalInput.Spec.Mode.PULL_UP); //pin1 is digital input, use pullup so that button can be connected to ground
 			pwm_ = ioio_.openPwmOutput(2, 50); //pin2 with 50Hz frequency - if using a servo, a greater frequency will create jitter
 			sendreceive_ = ioio_.openUart(3, 4, 9600, Uart.Parity.NONE, Uart.StopBits.ONE ); //pin3 tx, pin4 rx, baud rate 9600 
 			rx_ = sendreceive_.getInputStream();
 			tx_ = sendreceive_.getOutputStream();
 		}
 		
 		/**
 		 * Called repetitively while the MOIO is connected.
 		 */
 		@Override
 		public void loop() throws ConnectionLostException, InterruptedException {
 			
 			// DIGITAL OUT - blinks LED on button press
 			led_.write(!ledon.isChecked());
 			
 			// ANALOG IN - reads a voltage between 0 and 3.3V
 			graph(analogin_.getVoltage());
 			
 			// DIGITAL IN - reads a push button press
 			read_button(pushbutton_.read());
 			
 			// PWM OUT - outputs pulse width modulated waveform
 			pwm_.setPulseWidth(1000 + (pwmcontrol.getProgress()*10)); // gives a value between 1000 and 2000 for servo control
 			
 			// UART transmit
 			// uartTxData.getText().toString();
 			
 			// UART receive
 			
 			// this slows down the loop to save process time
 			/*try {
 				Thread.sleep(50);
 			} catch (InterruptedException e) {
 			}*/
 	
 			//Log.d(TAG, "analog volts " + volts); // debug, prints to logcat
 			
 		}
 	}
 	
 	// turns the MOIO on or off
     public void enable(boolean value)
     {
         BufferedWriter writer = null;
         try {
             FileOutputStream fos = new FileOutputStream(MAKR_ENABLE);
             OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
             writer = new BufferedWriter(osw);
             if (value)
                 writer.write("on\n");
             else
                 writer.write("off\n");
         } catch (FileNotFoundException e) {
         } catch (IOException e) {
         } finally {
             if (writer != null) {
                 try { writer.close(); } catch (IOException e) { }
             }
         }
     }
 	
 	// plots analog in values - must be in UI thread
 	private void graph(final float volts) {
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				graphView.setValue(p1, volts);
 			}
 		});
 	}
 	
 	//TODO possibly slows the connection process slightly
 	// used to notify once a connection to the MOIO has been made
 	private void on_notify() {
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				Toast.makeText(ActivityMOIO.this, "READY!!!", Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 	
 	// reads button press and prints 'active!' when button is hit
 	private void read_button(boolean value) {
 		String pushbuttontxt;
 		if (!value) {
 			pushbuttontxt = getString(R.string.pushbuttonstring) + " active!";
 		} else {
 			pushbuttontxt = getString(R.string.pushbuttonstring);
 		}
 		setText(pushbuttontxt);
 	}
 	
 	// needed to print button read - must be in UI thread
 	private void setText(final String str1) {
 		runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				buttonread.setText(str1);
 			}
 		});
 	}
 
 	// a method to create the MOIO thread
 	protected IOIOLooper createIOIOLooper() {
 		return new Looper();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onDestroy() {
 		// turn the MOIO off
 		enable(false);
 		super.onDestroy();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 	}
 
 }
