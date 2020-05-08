 /*
  *Chris Card
  *Nathan Harvey
  *10/27/12
  *This class contains the code for retrieving and saving users input to a new task or modification of an old task 
  */
 package csci422.CandN.to_dolist;
 
 import java.util.Date;
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import java.text.DateFormat;
 import java.text.ParseException;
 
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.Toast;
 
 public class DetailForm extends Activity {
 	public static final String tag = "todoDetail";
 
 	private ImageButton[] priors = new ImageButton[4];
	private EditText datepick;
 	private ToDoHelper helper;
 	private Cursor cur = null;
 	private Spinner pickList;
 	private EditText taskName;
 	private EditText notes;
 	private String[] Listnames = {"Main","Homework","Shopping"};
 	private SeekBar completion;
 	/** -1 is ?, 0 is a dot, 1 is one !, 2 is two !!  */
 	private int priority = 0;
 	private Date dueDate;
 	private DateFormat dateFormat;
 
 	private String id = "";//id needs to be acceable to whole class to it can be used with todo helper
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.detail_form);
 
 		completion = (SeekBar) findViewById(R.id.completion);
 		priors[0] = (ImageButton) findViewById(R.id.Priorityq);
 		priors[1] = (ImageButton) findViewById(R.id.Priority0);
 		priors[2] = (ImageButton) findViewById(R.id.Priority1);
 		priors[3] = (ImageButton) findViewById(R.id.Priority2);
		datepick = (EditText)findViewById(R.id.dueDatePicker);
 		dueDate = new Date(0);//current time
 		dateFormat = DateFormat.getDateTimeInstance();
 		pickList = ((Spinner) findViewById(R.id.pickList));
 		taskName = ((EditText) findViewById(R.id.taskName));
 		notes = ((EditText) findViewById(R.id.notes));
 		loadCurrent();
 		//pickList = ((ExpandableListView) findViewById(R.id.pickList));
 
 		ArrayAdapter<CharSequence> adpt = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, Listnames);
 		pickList.setAdapter(adpt);
 	}
 
 	@SuppressWarnings("deprecation")
 	private void loadCurrent() {
 		helper=new ToDoHelper(this);
 		String id = getIntent().getStringExtra("csci422.CandN.to_dolist.curItem");
 		if(id.length()==0)return;
 		cur = helper.getById(id);
 		cur.moveToFirst();//need to set cursor to the beginning 
 		taskName.setText(helper.getTitle(cur));
 		//TODO address
 		notes.setText(helper.getNotes(cur));
 		try {
 			dueDate = dateFormat.parse(helper.getDate(cur));
 		} catch (ParseException e) {
 			Log.e(tag, "Can't parse the date.");
 		}
 		completion.setProgress(helper.getState(cur));
 		priority = helper.getPriority(cur);
 		priors[priority+1].setBackgroundResource(R.drawable.widget_frame);
 	}
 
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		saveStuff(null);
 	}
 	public void saveStuff(View v){
 		Log.v(tag, "Progress: "+completion.getProgress());
 		Log.v(tag, completion.getKeyProgressIncrement()+" was done with keys");
 		Log.v(tag, "Secondary progress: "+completion.getSecondaryProgress());
 		Log.v(tag, "Thumb offset: "+completion.getThumbOffset());
 		Log.v(tag, "Max is: "+completion.getMax());
 		int state = completion.getProgress();
 		//float percent= completion.getProgress()/((float)completion.getMax());
 		try {
 			dueDate = dateFormat.parse("");//TODO change to read from the field
 		} catch (ParseException e) {
 			Log.e(tag, "Can't parse the date.");
 		}
 		if(cur==null){//make a new one
 			helper.insert(taskName.getText().toString(), "", notes.getText().toString(), dateFormat.format(dueDate), state, priority);
 		}else {//edit current
 			helper.update(id, taskName.getText().toString(), "", notes.getText().toString(), dateFormat.format(dueDate), state, priority);
 		}
 		this.finish();
 	}
 	public void priq(View v){priority=-1;clr(v);}
 	public void prin(View v){priority=0;clr(v);}
 	public void prio(View v){priority=1;clr(v);}
 	public void prit(View v){priority=2;clr(v);}
 
 	/**
 	 * Clears the backgrounds for all priority buttons except one.
 	 * @param v the view to give an active background.
 	 */
 	public void clr(View v){
 		Log.d(tag, "Priority: "+priority);
 		for(ImageButton b : priors){
 			b.setBackgroundResource(R.drawable.priorityblank);
 		}
 		v.setBackgroundResource(R.drawable.widget_frame);
 	}
 
 	public void deleteTask(View v){
 		Toast.makeText(this, "Not implemented", Toast.LENGTH_LONG).show();
 	}
 	
 	public void openCal(View v){
 		//TODO implement
 	}
 	
 	public void openMaps(View v){
 		//TODO implement
 	}
 
 
 }
