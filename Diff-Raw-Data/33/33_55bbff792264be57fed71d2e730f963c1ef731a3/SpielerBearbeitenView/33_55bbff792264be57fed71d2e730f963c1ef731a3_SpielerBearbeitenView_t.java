 package com.dhbw.dvst.views;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.dhbw.dvst.R;
 
 public class SpielerBearbeitenView extends RelativeLayout{
 	
 	/**
 	 * The interface to send events from the view to the controller
 	 */
 	public static interface ViewListener {
 		public boolean onTastaturVerstecken(int keyCode, KeyEvent event);
 		public void onSpielerSpeichern(EditText et_name, Object selectedColour, Object selectedForm);
 	}
 
 	/**
 	 * The listener reference for sending events
 	 */
 	private ViewListener viewListener;
 	private EditText et_name;
	public EditText getEt_name() {
		return et_name;
	}

	public void setEt_name(EditText et_name) {
		this.et_name = et_name;
	}

 	private Button btn_speichern;
 	private Spinner spin_farbe;
 	private Spinner spin_form;
	
 	public void setViewListener(ViewListener viewListener) {
 		this.viewListener = viewListener;
 	}
 	
 	/**
 	 * Constructor for xml layouts
 	 */
 	public SpielerBearbeitenView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	/**
 	 * Find our references to the objects in the xml layout
 	 */
 	@Override
 	protected void onFinishInflate() {
 		super.onFinishInflate();
 		
 		et_name = (EditText) findViewById(R.id.et_name);
 		btn_speichern = (Button) findViewById(R.id.btn_erstellen);
 		
 		TextView spielerSettingsHeading = (TextView) findViewById(R.id.hd_spieler);
		spielerSettingsHeading.setText(R.string.spieler_bearbeiten);
 		
 		spin_farbe = (Spinner) findViewById(R.id.spin_farbe);
 		spin_form = (Spinner) findViewById(R.id.spin_figur);
 		
 		et_name.setOnKeyListener(new OnKeyListener() {
 		    public boolean onKey(View v, int keyCode, KeyEvent event) {
 		        return viewListener.onTastaturVerstecken(keyCode, event);
 		    }
 		});
 		
 		btn_speichern.setOnClickListener(new View.OnClickListener() {
 	        public void onClick(View v) {
 	        	viewListener.onSpielerSpeichern(et_name, spin_farbe.getSelectedItem(), spin_form.getSelectedItem());        	
 	        }
 	    });
 	}
 }
