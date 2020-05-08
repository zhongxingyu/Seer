 package my.subject.HelloIOIOuart;
 
 //import ioio.examples.hello.MainActivity.IOIOThread;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import my.subject.HelloIOIOuart.R;
 //import ioio.lib.api.DigitalInput;
 import ioio.lib.api.IOIO;
 import ioio.lib.api.DigitalOutput;
 import ioio.lib.api.Uart;
 //import ioio.lib.api.Uart.Parity;
 //import ioio.lib.api.Uart.StopBits;
 import ioio.lib.api.exception.ConnectionLostException;
 import ioio.lib.util.AbstractIOIOActivity;
 import android.os.Bundle;
 //import android.util.Log;
 import android.widget.ToggleButton;
 
 /**
  * This is the main activity of the HelloIOIO example application.
  * 
  * It displays a toggle button on the screen, which enables control of the
  * on-board LED. This example shows a very simple usage of the IOIO, by using
  * the {@link AbstractIOIOActivity} class. For a more advanced use case, see the
  * HelloIOIOPower example.
  */
 public class HelloIOIOuartActivity extends AbstractIOIOActivity {
 	private ToggleButton button_;
 	private final String Str = "Hello, I'm VIFAM...OK VIFAM, Your number is 7.\n";
 //	private final String Str2 = "\nWipe away all your tears. Together we will conquer fear!\n";
 	private final String msg = "IOIO meets MIDI.";
 	private int bitmap[] = {0x1F, 0x01, 0x02, 0x04, 0x08, 0x08, 0x08};		// "7"
 
 	/**
 	 * Called when the activity is first created. Here we normally initialize
 	 * our GUI.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		button_ = (ToggleButton) findViewById(R.id.button);
 	}
 
 	/**
 	 * This is the thread on which all the IOIO activity happens. It will be run
 	 * every time the application is resumed and aborted when it is paused. The
 	 * method setup() will be called right after a connection with the IOIO has
 	 * been established (which might happen several times!). Then, loop() will
 	 * be called repetitively until the IOIO gets disconnected.
 	 */
 	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
 		private static final int PIN_TX = 14;		// it should be 5V tolerant, if necessary
 		private static final int PIN_SERCON_TX = 39;
 		private static final int PIN_EXTLED = 15;	// used as GPIO
 		/** The on-board LED. */
 		private DigitalOutput led_1, led_2;
 		private Uart uart_1, uart_2;
 		private OutputStream out, scon;
 
 		/**
 		 * Called every time a connection with IOIO has been established.
 		 * Typically used to open pins.
 		 * 
 		 * @throws ConnectionLostException
 		 *             When IOIO connection is lost.
 		 * 
 		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
 		 */
 		@Override
 		protected void setup() throws ConnectionLostException {
 			led_1 = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
 			led_2 = ioio_.openDigitalOutput(PIN_EXTLED, true);
 //    		uart_1 = ioio_.openUart(null, new DigitalOutput.Spec(PIN_TX, DigitalOutput.Spec.Mode.OPEN_DRAIN), 31250, Uart.Parity.NONE, Uart.StopBits.ONE);
     		uart_1 = ioio_.openUart(IOIO.INVALID_PIN, PIN_TX, 31250, Uart.Parity.NONE, Uart.StopBits.ONE);
     		out = uart_1.getOutputStream();
     		uart_2 = ioio_.openUart(IOIO.INVALID_PIN, PIN_SERCON_TX, 9600, Uart.Parity.NONE, Uart.StopBits.ONE);
     		scon = uart_2.getOutputStream();
     		
 //    		try {
 //				scon.write( new String("Hello, I'm VIFAM...OK VIFAM, Your number is 7.").getBytes() );
 //			} catch (IOException e) {
 //				// TODO Auto-generated catch block
 //				//e.printStackTrace();
 //			}
 //			sendMIDImsg(0x90, 90, 127);	// note on
 //			//sendMIDImsg(0x80, 90, 127);	// note off
 //
     		initSoundModule();
 			displayMessage(msg);
 			displayDots(0);
 		}
 
 		/**
 		 * Called repetitively while the IOIO is connected.
 		 * 
 		 * @throws ConnectionLostException
 		 *             When IOIO connection is lost.
 		 * 
 		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
 		 */
 		@Override
 		protected void loop() throws ConnectionLostException {
 			led_1.write(!button_.isChecked()); delay(10);
 			led_2.write(!button_.isChecked()); delay(10);
 //			try {
 //				sleep(10);
 //			} catch (InterruptedException e) {
 //			}
 			
     		try {
 				scon.write( Str.getBytes() );
 				//out.write( Str2.getBytes() );
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				//e.printStackTrace();
 			}
 			sendMIDImsg(0x90, 90, 127);	// note on
 			delay(500);
 			sendMIDImsg(0x80, 90, 127);	// note off
 			delay(500);
 		}
 		
 		private void serOut(int i) {
 			if ( out != null ) {
 				try {
 					out.write(i);
 				} catch (IOException e) {
 					//Log.d(TAG, "IOException in serOut()");
 				} catch (Exception e) {
 					//Log.d(TAG, "Something weird occurred in serOut()");
 				}
 			}
 		}
 		
 		private void sendMIDImsg(int stat, int data1, int data2) {
 			serOut(stat);
 			serOut(data1);
 			serOut(data2);
 		}
 		
 		private void sendMIDImsg2(int data1, int data2) {
 			serOut(data1);
 			serOut(data2);
 		}
 		
 		private void vsendMIDImsg(int... args) {
 			for (int i : args)
 				serOut(i);
 		}
 		
 		private void delay(int wait) {
 			try {
 				Thread.sleep(wait);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				//e.printStackTrace();
 				//Log.d(TAG, "InterruptedException in delay()");
 			}			
 		}
 		
 		private void initSoundModule() {
 			vsendMIDImsg(0xF0, 0x41, 0x10, 0x42, 0x12, 0x40, 0, 0x7F, 0 ,0x41, 0xF7);	// GS reset
 			delay(100);
 //			sendMIDImsg(0xB0, 0, 8);		// "Sine-wave" @ SC-55mk2
 //			sendMIDImsg(0xB0, 0x20, 0);
 //			sendMIDImsg2(0xC0, 80);
 			sendMIDImsg(0xB0, 0, 0);		// "flute" @ SC-55mk2
 			sendMIDImsg(0xB0, 0x20, 0);
 			sendMIDImsg2(0xC0, 73);
 		}
 		
 		protected void displayMessage(String msg) {
 			int csum, b, dat, b1 = 0x10, b2 = 0, b3 = 0;
 			String str;
 			
 			str = msg.concat("                                ").substring(0, 32);
 			vsendMIDImsg(0xF0, 0x41, 0x10, 0x45, 0x12, b1, b2, b3);
 			dat = 0;
 			for (int i = 0; i <32; i++) {
 				b = str.charAt(i);
 				serOut(b);
 			    dat += b;  
 			}
 			csum = 128 - ((b1 + b2 + b3 + dat) & 0x7F);
 			sendMIDImsg2(csum, 0xF7);
 			delay(50);
 		}
 		
 		protected void displayDots(int curPos) {
			int csum, dat, b = 0, b1 = 0x10, b2 = 0, b3 = 0;
 			
 			vsendMIDImsg(0xF0, 0x41, 0x10, 0x45, 0x12, b1, b2, b3);
			dat = 0;
 			for (int i = 1; i <=64; i++) {
 				if ( i < 8 )
 					b =  bitmap[i-1];
 				if ( ( i == 8) || ( i > 9) )
 					b = 0;
 				if ( i == 9 )
 					b = 0x20 >> (curPos+1);
 				serOut(b);
 				dat += b;  
 			}
 			  
 			csum = 128 - ((b1 + b2 + b3 + dat) & 0x7F);
 			sendMIDImsg2(csum, 0xF7);
 			delay(50);		
 		}
 
 
 				
 	} // end of class IOIOThread
 
 	/**
 	 * A method to create our IOIO thread.
 	 * 
 	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
 	 */
 	@Override
 	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
 		return new IOIOThread();
 	}
 }
