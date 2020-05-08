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
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.instrument_looper);
         
         m_background = (TableLayout) findViewById(R.id.LooperTable);
         
         // initialize sound manager
         m_soundManager = new SoundManager(getBaseContext());
         
         // connect buttons to corresponding sounds
         m_buttonSound = new HashMap<Integer, Integer>();
        m_buttonSound.put(R.id.LooperDrum1, m_soundManager.addSound(R.raw.bassdrumogg));
         m_buttonSound.put(R.id.LooperDrum2, m_soundManager.addSound(R.raw.warpdrumogg));
         m_buttonSound.put(R.id.LooperDrum3, m_soundManager.addSound(R.raw.filterdrumogg));
         m_buttonSound.put(R.id.LooperDrum4, m_soundManager.addSound(R.raw.bassdrumchorusogg));
        m_buttonSound.put(R.id.LooperSnare1, m_soundManager.addSound(R.raw.hithatdelayogg));
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
     	ToggleButton button = (ToggleButton) view;
     	
     	if (button.isChecked())
     		m_soundManager.playSound(m_buttonSound.get(button.getId()), true);
     	else
     		m_soundManager.stopSound(m_buttonSound.get(button.getId()));
     	
    	int color = Color.argb(255, 0, (int) numberButtonPressed()*102/20,(int) numberButtonPressed()*255/20);
     	m_background.setBackgroundColor(color);
     }

	private int numberButtonPressed() {
		int number = 0;
		
		for (Map.Entry<Integer, Integer> entry : m_buttonSound.entrySet())
			if (((ToggleButton) findViewById(entry.getKey())).isChecked())
				++number;
		
		return number;
	}
 }
