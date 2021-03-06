 package com.mattfeury.saucillator.android;
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnTouchListener;
 import android.widget.Toast;
 
 /*
  * Main activity for the App. This will spin up the visuals and the audio engine. 
  * The audio engine gets its own thread.
  */
 public class SauceEngine extends Activity implements OnTouchListener {
     private static final String TAG = "Sauce";
     private SauceView view;
 
     //defaults
     private int delayRate = UGen.SAMPLE_RATE / 4;
     private int lag = (int)(DEFAULT_LAG * 100);
     private int note = 0;
     private int octave = 4;
 
     //music shtuffs
     public int[] scale = Theory.pentatonicScale;
 
     public final static int MOD_RATE_MAX = 20;
     public final static int MOD_DEPTH_MAX = 1000;
     public final static float DEFAULT_LAG = 0.5f;
     public static int TRACKPAD_GRID_SIZE = 12;
 
     private boolean init = false;
     
     //synth elements
     private Dac dac;
     private Oscillator oscA, oscB;
     private ExpEnv envA, envB;
     private Delay ugDelay;
     private Looper looper;
 
     // which finger ID corresponds to which instrument
     private int fingerA = -1;
     private int fingerB = -1;
     private int fingerC = -1;
 
     MediaPlayer secretSauce;
     private Vibrator vibrator;
     private boolean canVibrate = false;
     private int VIBRATE_SPEED = 100; //in ms
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         Log.i(TAG, "Brewing sauce...");
 
         super.onCreate(savedInstanceState);
 
         secretSauce = MediaPlayer.create(this, R.raw.sauceboss);
 
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
         view = (SauceView)findViewById(R.id.sauceview);
         view.setOnTouchListener(this);
         vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         
         if (vibrator != null)
           canVibrate = true;
         
         Thread t = new Thread() {
       	  public void run() {
       	    try {
               //default instruments chosen here
               oscA = InstrumentManager.getInstrument(getAssets(), "SingingSaw");//new Sine();
               oscB = InstrumentManager.getInstrument(getAssets(), "Sine");//new Square();
       	    	envA = new ExpEnv();
               //envB = new ExpEnv();
       	    	dac = new Dac();
       	    	ugDelay = new Delay(delayRate);
       	    	looper = new Looper();
 
       	    	looper.chuck(dac);
       	    	ugDelay.chuck(looper);
 
       	    	envA.chuck(ugDelay);
               //envB.chuck(ugDelay);
 
               //TODO these should get chucked to different envelopes but it seems to cause issues
       	    	oscA.chuck(envA);
       	    	oscB.chuck(envA);
 
       	    	envA.setFactor(ExpEnv.medFactor);
       	    	envA.setActive(true);
       	    	//envB.setFactor(ExpEnv.medFactor);
       	    	//envB.setActive(true);
       	    	dac.open();
               init = true;
               Log.i(TAG, "Sauce ready.");
 
       	      while (true) {
         	    	dac.tick();
       	      }
       	    }
       	    catch(Exception ex) {
       	    	ex.printStackTrace();
       	    	Log.e(TAG, "bad time " + ex.toString());
       	    	dac.close();
       	    }
       	  }
       	};
       	
       t.start();
     }
 
     protected void onDestroy() {
     	android.os.Process.killProcess(android.os.Process.myPid());
     }
     
     // Maintains landscape mode
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
       setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
     }
 
     /**
      * That main goodness. Handles touch events and gets properties of them to change the oscillators
      */
     @Override
     public boolean onTouch(View v, MotionEvent event) {
       if (! init) return false;
 
       int maxHeight = v.getMeasuredHeight();
       int maxWidth = v.getMeasuredWidth();
 
       int action = event.getAction();
       int actionCode = action & MotionEvent.ACTION_MASK;
       
       if (actionCode == MotionEvent.ACTION_UP && dac.isPlaying()) { //last finger lifted. stop playback
         oscA.stop();
         oscB.stop();
         
         fingerA = -1;
         fingerB = -1;
         fingerC = -1;
 
         view.clearFingers();
         return true;
       }
 
       int pointerCount = event.getPointerCount();
       if (pointerCount == 5) {
         secretSauce.start(); //the secret sauce
         return true;
       }
     
       /*
        * Loop through each finger.
        * We go backwards because of the buttons. If a button is pressed and held, the next finger call
        * will be a POINTER_DOWN call, but the first button will interpret it first since it has a smaller index. That's bad.
        */
       for (int i = pointerCount - 1; i > -1; i--) {
         int id = event.getPointerId(i);
         float y = event.getY(i);
         float x = event.getX(i);
 
         //which osc does this finger correspond to?
         if (view.isInPad(x,y)) {
           Oscillator osc = null;
           //if this is this finger OR this finger is not set and this finger is also not the other fingers
           if (id == fingerA || (fingerA == -1 && fingerB != id && fingerC != id)) {
             osc = this.oscA;
             fingerA = id;
           } else if (id == fingerB || (fingerB == -1 && fingerC != id)) {
             osc = this.oscB;
             fingerB = id;
           } else if (fingerC == -1) {
           	fingerC = id;
           }
 
           int controllerWidth = (int) (maxWidth * SauceView.controllerWidth); 
 
           if (osc != null) { 
           	//finger down
             if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_MOVE) {
               view.updateOrCreateFinger(id, event.getX(i), event.getY(i), event.getSize(i), event.getPressure(i));
   
               //play if we were stopped
               if(! osc.isPlaying())
                 osc.togglePlayback();
               
               updateFrequency(id, (int)((maxHeight - y) / maxHeight * TRACKPAD_GRID_SIZE));
               updateAmplitude(id, (x - controllerWidth) / (maxWidth - controllerWidth));
             } else {
               //finger up. kill the osc
               final int upId = event.getActionIndex();
               Oscillator upOsc;
               if (upId == fingerA) {
               	upOsc = this.oscA;
               	fingerA = -1;
               } else if (upId == fingerB) {
               	upOsc = this.oscB;
               	fingerB = -1;
               } else {
               	return false;
               }
               
               view.removeFinger(upId);
   
               if(upOsc.isPlaying())
                 upOsc.togglePlayback();
             }
           } else if (id == fingerC) {
 //            final int upId = event.getActionIndex();
             if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_MOVE)
               view.updateOrCreateFinger(id, event.getX(i), event.getY(i), event.getSize(i), event.getPressure(i));
 //            else
 //              view.removeFinger(upId);
 
             oscA.setModRate((int)((x - controllerWidth) / (maxWidth - controllerWidth) * MOD_RATE_MAX));
             oscA.setModDepth((int)((maxHeight - y) / maxHeight * MOD_DEPTH_MAX));
             oscB.setModRate((int)((x - controllerWidth) / (maxWidth - controllerWidth) * MOD_RATE_MAX));
             oscB.setModDepth((int)((maxHeight - y) / maxHeight * MOD_DEPTH_MAX));          	
           }
         } else {
           //controller buttons
           //TODO FIXME there needs to be a more abstracted way to do these
 
           int buttonHeight = maxHeight / SauceView.numButtons;
 
           final int upId = event.getActionIndex();
           if ((actionCode == MotionEvent.ACTION_POINTER_DOWN &&
               upId != fingerA && upId != fingerB)
               || actionCode == MotionEvent.ACTION_DOWN) {
             
             if (canVibrate)
               vibrator.vibrate(VIBRATE_SPEED);
             
           	if (y <= buttonHeight) {
               //Toggle Looper Button
               boolean isRecording = looper.toggleRecording();
               if (isRecording)
               	view.focusLooper();
               else
               	view.unfocusLooper();
                   
             } else if (y <= buttonHeight * 2) {
               //Undo Looper Button
               looper.undo();
              view.unfocusLooper();
             } else {
               //Reset Looper Button
              looper.reset();
              view.unfocusLooper();
             }   	
           }
         }
       }
 
       return true; // indicate event was handled
     }
 
     /**
      * Oscillator handlers
      */
     public void updateAmplitude(int key, float amp) {
       Oscillator osc = null;
       if (key == fingerA) 
         osc = this.oscA;
       else if (key == fingerB)
         osc = this.oscB;
 
       if (osc != null)
         osc.setAmplitude(amp);
     }      
     public void updateFrequency(int key, int offset) {
       Oscillator osc = null;
       if (key == fingerA) 
         osc = this.oscA;
       else if (key == fingerB)
         osc = this.oscB;
 
       if (osc != null)
         osc.setFreqByOffset(scale, offset);
     }    
 
     // Update oscillators based on the settings parameters.
     private void updateOscSettings() {
       float newFreq = Theory.getFrequencyForNote(note + 1, octave);
       oscA.setBaseFreq(newFreq);
       oscB.setBaseFreq(newFreq);
 
       if (delayRate == 0) {
         ugDelay.updateRate(1); //i'm a hack. FIXME rate of 0 doesn't work
       } else {
         ugDelay.updateRate(delayRate);
       }
       
       oscA.setLag(lag / 100f);
       oscB.setLag(lag / 100f);
     }
     
     /**
      * Settings handlers
      */
     private boolean launchSettings() {
     	Intent intent = new Intent(SauceEngine.this, Settings.class);
     	intent.putExtra("octave", octave);
     	intent.putExtra("note", note);
     	intent.putExtra("file name", WavWriter.filePrefix);
     	intent.putExtra("delay rate", delayRate);
     	intent.putExtra("lag", lag);
     	intent.putExtra("visuals", view.getVisuals());
     	startActivityForResult(intent, 0);
     	return true;
     }
     // Called when settings activity ends. Updates proper params
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (requestCode == 0 && data != null) {
         Bundle extras = data.getExtras();
 
         if (extras != null) {
           WavWriter.filePrefix = extras.getString("file name");
           note = extras.getInt("note");
           octave = extras.getInt("octave");
           lag = extras.getInt("lag");
           delayRate = extras.getInt("delay rate");
           view.setVisuals(extras.getBoolean("visuals"));
           updateOscSettings();
         }
       }
     }
 
     /**
      * Menu handlers
      */
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getGroupId()) {
     		case R.id.instrumentsA:
     			return instrumentSelection(item, 0);
     		case R.id.instrumentsB:
     			return instrumentSelection(item, 1);
     		case R.id.scales:
     			return scaleSelection(item);
     		default:
     	}
     	switch (item.getItemId()) {
     		case R.id.quit:
     			onDestroy();
     			return true;
     		case R.id.settings:
     			return launchSettings();
     		case R.id.record:
     			return record(item);
     		default:
     	}
       return false;
     }
 
     private boolean record(MenuItem item) {
       boolean isRecording = dac.toggleRecording();
     	if (isRecording) {
     	    item.setTitle("Stop Recording");
     	    item.setIcon(R.drawable.ic_grey_rec);
             Toast.makeText(this, "Recording.", Toast.LENGTH_SHORT).show();
     	}
     	else {
 		    item.setTitle("Record");
 		    item.setIcon(R.drawable.ic_rec);
 	        Toast.makeText(this, "Stopped Recording.", Toast.LENGTH_SHORT).show();
 		    	
 	        if(WavWriter.getLastFile() == null)
 	          return false;
 		    	
 	        Intent intent = new Intent(Intent.ACTION_SEND).setType("audio/*");
 		    	intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(WavWriter.getLastFile()));
 		    	startActivity(Intent.createChooser(intent, "Share to"));
     	}
       return true;
     }
 
     private boolean scaleSelection(MenuItem item) {
     	if (item.isChecked()) {
     		return true;
     	}
     	item.setChecked(true);
       int scaleId = item.getItemId();
 
       switch (scaleId) {
         case R.id.pentatonic: //pentatonic
           scale = Theory.pentatonicScale;
           break;
         case R.id.major: //major
           scale = Theory.majorScale;
           break;
         case R.id.minor: //minor
           scale = Theory.minorScale;
           break;
         case R.id.blues: //blues
           scale = Theory.minorBluesScale;
           break;
         case R.id.chromatic: //chromatic
           scale = Theory.chromaticScale;
           break;
         default:
       }
     	return false;
     }
 
     private boolean instrumentSelection(MenuItem item, int oscNum) {
     	if (item.isChecked()) {
     		return true;
     	}
     	item.setChecked(true);
       int instrumentId = item.getItemId();
 
       Oscillator oldOsc;
       if (oscNum == 0) {
         oldOsc = this.oscA;
         this.oscA.unchuck(envA);        
       } else if (oscNum == 1) {
         oldOsc = this.oscB;
         this.oscB.unchuck(envA);
       } else {
       	return false;
       }
       //TODO destroy old maybe? make sure it gets garbage collected
 
       switch (instrumentId) {
         case R.id.singingsaw: //singing saw
           oldOsc = new SingingSaw();
           break;
         case R.id.sine: //sine
           oldOsc = new Sine();
           break;
         case R.id.square: //square
         	oldOsc = new Square(1.0f);
           break;
         case R.id.saw: //saw
           oldOsc = new Saw(1.0f);
           break;
         default:
           oldOsc = new Sine();
       }
 
       if (oscNum == 0) {
         this.oscA = oldOsc;
         this.oscA.chuck(envA);
       } else if (oscNum == 1) {
         this.oscB = oldOsc;
         this.oscB.chuck(envA);
       }
       
       //FIXME this is a hack for now so that the instrument specific settings (lag & base freq)
       //FIXME get reset on every instrument. We should abstract as InstrumentSettings and pass them around that way.
       updateOscSettings();
     	
     	return false;
     }
 }
