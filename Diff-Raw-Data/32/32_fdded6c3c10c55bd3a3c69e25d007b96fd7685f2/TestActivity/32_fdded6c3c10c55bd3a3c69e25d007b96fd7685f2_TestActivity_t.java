 package com.davidjennes.ElectroJam;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TableLayout;
 import android.widget.ToggleButton;
 
 public class TestActivity extends Activity {
 	private SoundManager m_soundManager;
 	private Map<Integer, Integer> m_buttonSound;
     private TableLayout m_background;
    private int m_totalPressed;
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.instrument_looper);
         
         m_background = (TableLayout) findViewById(R.id.LooperTable);
        m_totalPressed = 0;
         
         // initialize sound manager
         m_soundManager = new SoundManager(getBaseContext());
         
         // connect buttons to corresponding sounds
         m_buttonSound = new HashMap<Integer, Integer>();
        m_buttonSound.put(R.id.LooperDrum1, m_soundManager.addSound(R.raw.bass_egg_snare_test));
         m_buttonSound.put(R.id.LooperDrum2, m_soundManager.addSound(R.raw.warpdrumogg));
         m_buttonSound.put(R.id.LooperDrum3, m_soundManager.addSound(R.raw.filterdrumogg));
         m_buttonSound.put(R.id.LooperDrum4, m_soundManager.addSound(R.raw.bassdrumchorusogg));
        m_buttonSound.put(R.id.LooperSnare1, m_soundManager.addSound(R.raw.clap_egg_test));
         m_buttonSound.put(R.id.LooperSnare2, m_soundManager.addSound(R.raw.hithatfuzzogg));
         m_buttonSound.put(R.id.LooperSnare3, m_soundManager.addSound(R.raw.snareclapogg));
         m_buttonSound.put(R.id.LooperSnare4, m_soundManager.addSound(R.raw.snareclapreverbogg));
     }
     
     public void onDestroy() {
         super.onDestroy();
         
         for (Map.Entry<Integer, Integer> entry : m_buttonSound.entrySet())
     		m_soundManager.stopSound(entry.getValue());
     }
 
     public void buttonClick(View view) {
    	if (view == null)
    		return;
    	
     	ToggleButton button = (ToggleButton) view;
    	m_totalPressed += button.isChecked() ? 1 : -1;
     	
    	// play or stop
     	if (button.isChecked())
     		m_soundManager.playSound(m_buttonSound.get(button.getId()), true);
     	else
     		m_soundManager.stopSound(m_buttonSound.get(button.getId()));
     	
    	// change background depending on number of sounds
    	int color = Color.argb(255, 0, (int) m_totalPressed * 102 / 20, (int) m_totalPressed * 255 / 20);
     	m_background.setBackgroundColor(color);
     }
 }
