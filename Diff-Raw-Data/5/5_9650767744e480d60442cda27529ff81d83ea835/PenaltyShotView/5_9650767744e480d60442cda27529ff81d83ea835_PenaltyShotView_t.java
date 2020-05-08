 package wbs.nn;
 
 import java.io.File;
 
 import GoalKeeperCheatSheet.Asker;
 import GoalKeeperCheatSheet.GoalKeeperCheatSheetNeuronalNetwork;
 import GoalKeeperCheatSheet.PenaltyShot;
 import GoalKeeperCheatSheet.PenaltyShotImporter;
 import GoalKeeperCheatSheet.Starter;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class PenaltyShotView extends Activity {
 
 	protected int mPos1;
 	protected int mPos2;
 	protected int mPos3;
 	protected int mPos4;
 	protected int mPos5;
 	protected int mPos6;
 	protected int mPos7;
 	protected int mPos8;
 	protected String mSelection1;
 	protected String mSelection2;
 	protected String mSelection3;
 	protected String mSelection4;
 	protected String mSelection5;
 	protected String mSelection6;
 	protected String mSelection7;
 	protected String mSelection8;
 	protected ArrayAdapter<CharSequence> mAdapter1;
 	protected ArrayAdapter<CharSequence> mAdapter2;
 	protected ArrayAdapter<CharSequence> mAdapter3;
 	protected ArrayAdapter<CharSequence> mAdapter4;
 	protected ArrayAdapter<CharSequence> mAdapter5;
 	protected ArrayAdapter<CharSequence> mAdapter6;
 	protected ArrayAdapter<CharSequence> mAdapter7;
 	protected ArrayAdapter<CharSequence> mAdapter8;
 	
 	private GoalKeeperCheatSheetNeuronalNetwork NN;
 	
 	public static final int DEFAULT_POSITION = 0;
 	public static final String PREFERENCES_FILE = "SpinnerPrefs";
 	public static final String PROPERTY_DELIMITER = "=";
 	public static final String POSITION_KEY = "Position";
 	public static final String POSITION_MARKER = POSITION_KEY + PROPERTY_DELIMITER;
 	public static final String SELECTION_KEY = "Selection";
 	public static final String SELECTION_MARKER = SELECTION_KEY + PROPERTY_DELIMITER;
 	
 	private Handler handlerEvent = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 				case 0: {
 					ProgressBar pB = (ProgressBar) findViewById(R.id.progressBar1);
 					pB.setVisibility(View.VISIBLE);
 					break;
 				}
 				case 1: {
 					ProgressBar pB = (ProgressBar) findViewById(R.id.progressBar1);
 					pB.setVisibility(View.GONE);
 					break;
 				}
 				case 2: {
 					TableLayout tl = (TableLayout) findViewById(R.id.tL02);
 					LinearLayout ll = (LinearLayout) findViewById(R.id.configure);
 					tl.setVisibility(View.VISIBLE);
 					ll.setVisibility(View.VISIBLE);
 					break;
 				}
 				case 9: {
 					String err = (String) msg.obj;
 					Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();
 					break;
 				}
 				default: {
 					break;
 				}
 			}
 		}
 	};
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.main);
 		
 		//1
 		Spinner spinner1 = (Spinner) findViewById(R.id.Spinner01);
 		this.mAdapter1 = ArrayAdapter.createFromResource(this, R.array.Anlauflaenge,
 				android.R.layout.select_dialog_singlechoice);
 		spinner1.setAdapter(this.mAdapter1);
 		OnItemSelectedListener spinnerListener1 = new myOnItemSelectedListener(
 				this, this.mAdapter1);
 		spinner1.setOnItemSelectedListener(spinnerListener1);
 		
 		//2
 		Spinner spinner2 = (Spinner) findViewById(R.id.Spinner02);
 		this.mAdapter2 = ArrayAdapter.createFromResource(this, R.array.Richtung,
 				android.R.layout.select_dialog_singlechoice);
 		spinner2.setAdapter(this.mAdapter2);
 		OnItemSelectedListener spinnerListener2 = new myOnItemSelectedListener(
 				this, this.mAdapter2);
 		spinner2.setOnItemSelectedListener(spinnerListener2);
 		
 		//3
 		Spinner spinner3 = (Spinner) findViewById(R.id.Spinner03);
 		this.mAdapter3 = ArrayAdapter.createFromResource(this, R.array.Verzoegerung,
 				android.R.layout.select_dialog_singlechoice);
 		spinner3.setAdapter(this.mAdapter3);
 		OnItemSelectedListener spinnerListener3 = new myOnItemSelectedListener(
 				this, this.mAdapter3);
 		spinner3.setOnItemSelectedListener(spinnerListener3);
 		
 		//4
 		Spinner spinner4 = (Spinner) findViewById(R.id.Spinner04);
 		this.mAdapter4 = ArrayAdapter.createFromResource(this, R.array.Gefoult,
 				android.R.layout.select_dialog_singlechoice);
 		spinner4.setAdapter(this.mAdapter4);
 		OnItemSelectedListener spinnerListener4 = new myOnItemSelectedListener(
 				this, this.mAdapter4);
 		spinner4.setOnItemSelectedListener(spinnerListener4);
 		
 		//5
 		Spinner spinner5 = (Spinner) findViewById(R.id.Spinner05);
 		this.mAdapter5 = ArrayAdapter.createFromResource(this, R.array.Ort,
 				android.R.layout.select_dialog_singlechoice);
 		spinner5.setAdapter(this.mAdapter5);
 		OnItemSelectedListener spinnerListener5 = new myOnItemSelectedListener(
 				this, this.mAdapter5);
 		spinner5.setOnItemSelectedListener(spinnerListener5);
 		
 		//6
 		Spinner spinner6 = (Spinner) findViewById(R.id.Spinner06);
 		this.mAdapter6 = ArrayAdapter.createFromResource(this, R.array.Kurve,
 				android.R.layout.select_dialog_singlechoice);
 		spinner6.setAdapter(this.mAdapter6);
 		OnItemSelectedListener spinnerListener6 = new myOnItemSelectedListener(
 				this, this.mAdapter6);
 		spinner6.setOnItemSelectedListener(spinnerListener6);
 		
 		//7
 		Spinner spinner7 = (Spinner) findViewById(R.id.Spinner07);
 		this.mAdapter7 = ArrayAdapter.createFromResource(this, R.array.Wichtig,
 				android.R.layout.select_dialog_singlechoice);
 		spinner7.setAdapter(this.mAdapter7);
 		OnItemSelectedListener spinnerListener7 = new myOnItemSelectedListener(
 				this, this.mAdapter2);
 		spinner7.setOnItemSelectedListener(spinnerListener7);
 		
 		//8
 		Spinner spinner8 = (Spinner) findViewById(R.id.Spinner08);
 		this.mAdapter8 = ArrayAdapter.createFromResource(this, R.array.Zuschauer,
 				android.R.layout.select_dialog_singlechoice);
 		spinner8.setAdapter(this.mAdapter8);
 		OnItemSelectedListener spinnerListener8 = new myOnItemSelectedListener(
 				this, this.mAdapter8);
 		spinner8.setOnItemSelectedListener(spinnerListener8);
 		
 		//hiddenNodes, default_epsilon, default_learningRate
 		this.NN = new GoalKeeperCheatSheetNeuronalNetwork(16, 1.0, 0.0);
 	}
 	
 	public class myOnItemSelectedListener implements OnItemSelectedListener {
 		
 		ArrayAdapter<CharSequence> mLocalAdapter;
 		Activity mLocalContext;
 		
 		public myOnItemSelectedListener(Activity c, ArrayAdapter<CharSequence> ad) {
 			this.mLocalContext = c;
 			this.mLocalAdapter = ad;
 		}
 
 		public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) {
 			if (parent.getAdapter().equals(PenaltyShotView.this.mAdapter1)) {
 				PenaltyShotView.this.mPos1 = pos;
 				PenaltyShotView.this.mSelection1 = parent.getItemAtPosition(pos).toString();
 				TextView resultText1 = (TextView) findViewById(R.id.Spinner01Result);
 				resultText1.setText(PenaltyShotView.this.mSelection1);
 			}
 			else if (parent.getAdapter().equals(PenaltyShotView.this.mAdapter2)){
 				PenaltyShotView.this.mPos2 = pos;
 				PenaltyShotView.this.mSelection2 = parent.getItemAtPosition(pos).toString();
 				TextView resultText2 = (TextView) findViewById(R.id.Spinner02Result);
 				resultText2.setText(PenaltyShotView.this.mSelection2);
 			}
 			else if (parent.getAdapter().equals(PenaltyShotView.this.mAdapter3)){
 				PenaltyShotView.this.mPos3 = pos;
 				PenaltyShotView.this.mSelection3 = parent.getItemAtPosition(pos).toString();
 				TextView resultText3 = (TextView) findViewById(R.id.Spinner03Result);
 				resultText3.setText(PenaltyShotView.this.mSelection3);
 			}
 			else if (parent.getAdapter().equals(PenaltyShotView.this.mAdapter4)){
 				PenaltyShotView.this.mPos4 = pos;
 				PenaltyShotView.this.mSelection4 = parent.getItemAtPosition(pos).toString();
 				TextView resultText4 = (TextView) findViewById(R.id.Spinner04Result);
 				resultText4.setText(PenaltyShotView.this.mSelection4);
 			}
 			else if (parent.getAdapter().equals(PenaltyShotView.this.mAdapter5)){
 				PenaltyShotView.this.mPos5 = pos;
 				PenaltyShotView.this.mSelection5 = parent.getItemAtPosition(pos).toString();
 				TextView resultText5 = (TextView) findViewById(R.id.Spinner05Result);
 				resultText5.setText(PenaltyShotView.this.mSelection5);
 			}
 			else if (parent.getAdapter().equals(PenaltyShotView.this.mAdapter6)){
 				PenaltyShotView.this.mPos6 = pos;
 				PenaltyShotView.this.mSelection6 = parent.getItemAtPosition(pos).toString();
 				TextView resultText6 = (TextView) findViewById(R.id.Spinner06Result);
 				resultText6.setText(PenaltyShotView.this.mSelection6);
 			}
 			else if (parent.getAdapter().equals(PenaltyShotView.this.mAdapter7)){
 				PenaltyShotView.this.mPos7 = pos;
 				PenaltyShotView.this.mSelection7 = parent.getItemAtPosition(pos).toString();
 				TextView resultText7 = (TextView) findViewById(R.id.Spinner07Result);
 				resultText7.setText(PenaltyShotView.this.mSelection7);
 			}
 			else if (parent.getAdapter().equals(PenaltyShotView.this.mAdapter8)){
 				PenaltyShotView.this.mPos8 = pos;
 				PenaltyShotView.this.mSelection8 = parent.getItemAtPosition(pos).toString();
 				TextView resultText8 = (TextView) findViewById(R.id.Spinner08Result);
 				resultText8.setText(PenaltyShotView.this.mSelection8);
 			}
 		}
 		
 		public void onNothingSelected(AdapterView<?> parent) {
 			// do nothing
 		}
 		
 	}
 
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		if (!readInstanceState(this))
 			setInitialState();
 		
 		Spinner restoreSpinner1 = (Spinner) findViewById(R.id.Spinner01);
 		restoreSpinner1.setSelection(getSpinnerPosition(1));
 		
 		Spinner restoreSpinner2 = (Spinner) findViewById(R.id.Spinner02);
 		restoreSpinner2.setSelection(getSpinnerPosition(2));
 		
 		Spinner restoreSpinner3 = (Spinner) findViewById(R.id.Spinner02);
 		restoreSpinner3.setSelection(getSpinnerPosition(3));
 		
 		Spinner restoreSpinner4 = (Spinner) findViewById(R.id.Spinner02);
 		restoreSpinner4.setSelection(getSpinnerPosition(4));
 		
 		Spinner restoreSpinner5 = (Spinner) findViewById(R.id.Spinner02);
 		restoreSpinner5.setSelection(getSpinnerPosition(5));
 		
 		Spinner restoreSpinner6 = (Spinner) findViewById(R.id.Spinner02);
 		restoreSpinner6.setSelection(getSpinnerPosition(6));
 		
 		Spinner restoreSpinner7 = (Spinner) findViewById(R.id.Spinner02);
 		restoreSpinner7.setSelection(getSpinnerPosition(7));
 		
 		Spinner restoreSpinner8 = (Spinner) findViewById(R.id.Spinner02);
 		restoreSpinner8.setSelection(getSpinnerPosition(8));
 		
		// hide the ask menu & the configure menu 
		TableLayout tl = (TableLayout) findViewById(R.id.tL02);
 		LinearLayout ll = (LinearLayout) findViewById(R.id.configure);
		tl.setVisibility(View.GONE);
 		ll.setVisibility(View.GONE);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		
 		if (!writeInstanceState(this)) {
 			Toast.makeText(this, "Failed to write state!", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	public void setInitialState() {
 		this.mPos1 = DEFAULT_POSITION;
 		this.mPos2 = DEFAULT_POSITION;
 		this.mPos3 = DEFAULT_POSITION;
 		this.mPos4 = DEFAULT_POSITION;
 		this.mPos5 = DEFAULT_POSITION;
 		this.mPos6 = DEFAULT_POSITION;
 		this.mPos7 = DEFAULT_POSITION;
 		this.mPos8 = DEFAULT_POSITION;
 	}
 	
 	public boolean readInstanceState(Context c) {
 		SharedPreferences p = c.getSharedPreferences(
 				PenaltyShotView.PREFERENCES_FILE, MODE_WORLD_READABLE);
 		
 		this.mPos1 = p.getInt(POSITION_KEY + "1", PenaltyShotView.DEFAULT_POSITION);
 		this.mSelection1 = p.getString(SELECTION_KEY + "1", "");
 		
 		this.mPos2 = p.getInt(POSITION_KEY + "2", PenaltyShotView.DEFAULT_POSITION);
 		this.mSelection2 = p.getString(SELECTION_KEY + "2", "");
 		
 		this.mPos3 = p.getInt(POSITION_KEY + "3", PenaltyShotView.DEFAULT_POSITION);
 		this.mSelection3 = p.getString(SELECTION_KEY + "3", "");
 		
 		this.mPos4 = p.getInt(POSITION_KEY + "4", PenaltyShotView.DEFAULT_POSITION);
 		this.mSelection4 = p.getString(SELECTION_KEY + "4", "");
 		
 		this.mPos5 = p.getInt(POSITION_KEY + "5", PenaltyShotView.DEFAULT_POSITION);
 		this.mSelection5 = p.getString(SELECTION_KEY + "5", "");
 		
 		this.mPos6 = p.getInt(POSITION_KEY + "6", PenaltyShotView.DEFAULT_POSITION);
 		this.mSelection6 = p.getString(SELECTION_KEY + "6", "");
 		
 		this.mPos7 = p.getInt(POSITION_KEY + "7", PenaltyShotView.DEFAULT_POSITION);
 		this.mSelection7 = p.getString(SELECTION_KEY + "7", "");
 		
 		this.mPos8 = p.getInt(POSITION_KEY + "8", PenaltyShotView.DEFAULT_POSITION);
 		this.mSelection8 = p.getString(SELECTION_KEY + "8", "");
 		
 		TextView filePath = (TextView) findViewById(R.id.filePath);
 		TextView epsilon = (TextView) findViewById(R.id.epsilon);
 		TextView learningRate = (TextView) findViewById(R.id.learningRate);
 		  
 		filePath.setText(p.getString("FilePath", "gruppe_ca6_t2.csv"));
 		epsilon.setText(p.getString("Epsilon", "1.0"));
 		learningRate.setText(p.getString("LearningRate", "0.0"));
 		
 		return (p.contains(POSITION_KEY));
 	}
 
 	public boolean writeInstanceState(Context c) {
 		SharedPreferences p = c.getSharedPreferences(
 				PenaltyShotView.PREFERENCES_FILE, MODE_WORLD_READABLE);
 
 		SharedPreferences.Editor e = p.edit();
 
 		e.putInt(POSITION_KEY + "1", this.mPos1);
 		e.putString(SELECTION_KEY + "1", this.mSelection1);
 		
 		e.putInt(POSITION_KEY + "2", this.mPos2);
 		e.putString(SELECTION_KEY + "2", this.mSelection2);
 		
 		e.putInt(POSITION_KEY + "3", this.mPos2);
 		e.putString(SELECTION_KEY + "3", this.mSelection2);
 		
 		e.putInt(POSITION_KEY + "4", this.mPos2);
 		e.putString(SELECTION_KEY + "4", this.mSelection2);
 		
 		e.putInt(POSITION_KEY + "5", this.mPos2);
 		e.putString(SELECTION_KEY + "5", this.mSelection2);
 		
 		e.putInt(POSITION_KEY + "6", this.mPos2);
 		e.putString(SELECTION_KEY + "6", this.mSelection2);
 		
 		e.putInt(POSITION_KEY + "7", this.mPos2);
 		e.putString(SELECTION_KEY + "7", this.mSelection2);
 		
 		e.putInt(POSITION_KEY + "8", this.mPos2);
 		e.putString(SELECTION_KEY + "8", this.mSelection2);
 		
 		//persist the edit text fields
 		TextView filePath = (TextView) findViewById(R.id.filePath);
 		TextView epsilon = (TextView) findViewById(R.id.epsilon);
 		TextView learningRate = (TextView) findViewById(R.id.learningRate);
 		  
 		e.putString("FilePath", filePath.getText().toString());
 		e.putString("Epsilon", epsilon.getText().toString());
 		e.putString("LearningRate", learningRate.getText().toString());
 		
 		return (e.commit());
 	}
 
 	public int getSpinnerPosition(int i) {
 		if      (i == 1) return this.mPos1;
 		else if (i == 2) return this.mPos2;
 		else if (i == 3) return this.mPos3;
 		else if (i == 4) return this.mPos4;
 		else if (i == 5) return this.mPos5;
 		else if (i == 6) return this.mPos6;
 		else if (i == 7) return this.mPos7;
 		else             return this.mPos8;
 	}
 	
 	public void start(View v) {
 		if(this.isExternalStorageAvail()) {
 			EditText filePath = (EditText) findViewById(R.id.filePath);
 			File f = new File(Environment.getExternalStorageDirectory() + "/" + filePath.getText().toString());
 			
 			EditText epsilon = (EditText) findViewById(R.id.epsilon);
 			EditText learningRate = (EditText) findViewById(R.id.learningRate);
 			try {
 			    double e = Double.parseDouble(epsilon.getText().toString());
 				double l = Double.parseDouble(learningRate.getText().toString());
 				this.NN.setEpsilon(e);
 				this.NN.setLearningRate(l);
 				new Starter(this, this.NN, f);
 			} catch (Exception e) {
 				e.printStackTrace();
 				Toast.makeText(this, "Wrong Epsilon/LearningRate Input.", Toast.LENGTH_LONG).show();
 			}
 		}
 		else {
 			Toast.makeText(this, "External Storage not available.", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	public boolean isExternalStorageAvail() {
 		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
 	}
 	
 	public void setProgressBar(int i) {
 		this.handlerEvent.sendEmptyMessage(i);
 	}
 	
 	public void setAskAndConfig() {
 		this.handlerEvent.sendEmptyMessage(2);
 	}
 	
 	public void showError(String string) {
 		Message msg = new Message();
 		msg.what = 9;
 		msg.obj = string;
 		this.handlerEvent.sendMessage(msg);
 	}
 	
 	public void ask(View v) {
 		TextView tv1 = (TextView) findViewById(R.id.Spinner01Result);
 		TextView tv2 = (TextView) findViewById(R.id.Spinner02Result);
 		TextView tv3 = (TextView) findViewById(R.id.Spinner03Result);
 		TextView tv4 = (TextView) findViewById(R.id.Spinner04Result);
 		TextView tv5 = (TextView) findViewById(R.id.Spinner05Result);
 		TextView tv6 = (TextView) findViewById(R.id.Spinner06Result);
 		TextView tv7 = (TextView) findViewById(R.id.Spinner07Result);
 		TextView tv8 = (TextView) findViewById(R.id.Spinner08Result);
 		
 		try {
 			double runUpLength = PenaltyShotImporter.getRunUpLength(tv1.getText().toString());
 			double runUpDirection = PenaltyShotImporter.getRunUpDirection(tv2.getText().toString());
 			double delay = PenaltyShotImporter.getDelay(tv3.getText().toString());
 			double fouled = PenaltyShotImporter.getFouled(tv4.getText().toString());
 			double place = PenaltyShotImporter.getPlace(tv5.getText().toString());
 			double ownFanBlock = PenaltyShotImporter.getOwnBlock(tv6.getText().toString());
 			double importance = PenaltyShotImporter.getImportance(tv7.getText().toString());
 			double spectators = PenaltyShotImporter.getSpectators(tv8.getText().toString());
 			
 			PenaltyShot ps = new PenaltyShot(runUpLength, runUpDirection, delay, fouled, place, ownFanBlock, importance, spectators, 0.0, 0.0);
 			
 			Asker asker = new Asker(this.NN, ps);
 			String response = asker.ask();
 			
 			TextView result = (TextView) findViewById(R.id.result);
 			result.setText(response);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			Toast.makeText(this, "Wrong Input Data.", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 }
