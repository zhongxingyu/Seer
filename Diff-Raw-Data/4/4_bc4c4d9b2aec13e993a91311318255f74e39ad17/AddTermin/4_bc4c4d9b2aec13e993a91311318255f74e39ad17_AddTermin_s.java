 /**
  * 
  */
 package de.eahapp.gui.termin;
 
 import java.util.Calendar;
 
 import org.holoeverywhere.widget.DatePicker;
 import org.holoeverywhere.widget.TimePicker;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 
 import de.eahapp.R;
import de.eahapp.SQLiteHelper;
 import de.eahapp.SQLiteHelperUser;
 import de.eahapp.Utils;
 import de.eahapp.gui.ANotifyingScreen;
 
 /**
  * @author chris
  *
  */
 public class AddTermin extends  ANotifyingScreen  {
 	private TimePicker timePicker1;
 	private TimePicker timePicker2;
 	private EditText editTextBeschreibung;
 	private EditText editTextOrt;
 	private EditText editTextPerson;
 	private DatePicker datePickerDatum;
     private Context con;
     private Calendar cal=Calendar.getInstance();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Bundle b = getIntent().getExtras();
 		ActionBar navigation_bar =  getSupportActionBar();
 		//Setting standart navigation bar view
 		navigation_bar.setTitle (this.getResources().getString(R.string.add_termin_title));
 		navigation_bar.setSubtitle(this.getResources().getString(R.string.add_termin_subtitle));
 		if(b==null){
 			terminAdd();
 		}else{
 			terminEdit(b);
 		}
 		
 
 	}
 
 	private void terminEdit(Bundle b) {
 		String name = b.getString("name");
 		String beschreibung = b.getString("beschreibung");
 		String person = b.getString("person");
 		String ort = b.getString("ort");
 		String date1 =  b.getString("date1");
 		String date2 =  b.getString("date2");
 		final int id = Integer.valueOf(b.getString("id"));
 		cal.setTimeInMillis(Long.valueOf(date1));
 		int year=cal.get(Calendar.YEAR);
 		int month=cal.get(Calendar.MONTH);
 		int day=cal.get(Calendar.DAY_OF_MONTH);
 		int hour=cal.get(Calendar.HOUR_OF_DAY);
 		int min=cal.get(Calendar.MINUTE);
 		cal.setTimeInMillis(Long.valueOf(date2));
 		int hour2=cal.get(Calendar.HOUR_OF_DAY);
 		int min2=cal.get(Calendar.MINUTE);
 		setContentView(R.layout.addtermin);
 
 		timePicker1 = (TimePicker) this.findViewById(R.id.timePickerVon);
 		timePicker1.setIs24HourView(true);
 		timePicker2 = (TimePicker) this.findViewById(R.id.timePickerBis);
 		timePicker2.setIs24HourView(true);
 		editTextBeschreibung = (EditText) this.findViewById(R.id.editTextBeschreibung);
 		editTextBeschreibung.setText(name);
 		editTextOrt = (EditText) this.findViewById(R.id.editTextOrt);
 		editTextOrt.setText(ort);
 		editTextPerson = (EditText) this.findViewById(R.id.editTextPerson);
 		editTextPerson.setText(person);
 		datePickerDatum = (DatePicker) this.findViewById(R.id.datePickerDatum);
 		// set date and time 
 		timePicker1.setCurrentHour(hour);
 		timePicker1.setCurrentMinute(min);
 		timePicker2.setCurrentHour(hour2);
 		timePicker2.setCurrentMinute(min2);
 		datePickerDatum.updateDate(year, month, day);
 		Button btn = (Button) findViewById(R.id.buttonSave);
 	    con = this.getApplicationContext();
 		btn.setOnClickListener(new View.OnClickListener() {
 			@Override
 		    public void onClick(View v) {
 				String Type = "custom";
 				cal.set(datePickerDatum.getYear(), datePickerDatum.getMonth(), datePickerDatum.getDayOfMonth(), timePicker1.getCurrentHour(), timePicker1.getCurrentMinute(), 0);
 				long date1 = cal.getTimeInMillis();
 				cal.set(datePickerDatum.getYear(), datePickerDatum.getMonth(), datePickerDatum.getDayOfMonth(), timePicker2.getCurrentHour(), timePicker2.getCurrentMinute(), 0);
 				long date2 = cal.getTimeInMillis();
 				SQLiteHelperUser db = new SQLiteHelperUser(con,Utils.getAppFilespath(con));
 				String ort = editTextOrt.getText().toString();
 				String beschreibung = editTextBeschreibung.getText().toString();
 				String person = editTextPerson.getText().toString();
 				if(date1>date2){
 					Toast.makeText(getApplicationContext(),"Fehler Zeit 'von' ist kleiner als Zeit 'bis'", Toast.LENGTH_LONG).show();
 					db.close();
 				}else{
 					db.editTermin(id, date1, date2, ort, person, beschreibung, beschreibung, Type);
 					db.close();
 					back();
 				}
 		    }
 		});
 		
 	}
 
 	private void terminAdd() {
 
 		int year=cal.get(Calendar.YEAR);
 		int month=cal.get(Calendar.MONTH);
 		int day=cal.get(Calendar.DAY_OF_MONTH);
 		int hour=cal.get(Calendar.HOUR_OF_DAY);
 		int min=cal.get(Calendar.MINUTE);
 		
 		setContentView(R.layout.addtermin);
 		timePicker1 = (TimePicker) this.findViewById(R.id.timePickerVon);
 		timePicker1.setIs24HourView(true);
 		timePicker2 = (TimePicker) this.findViewById(R.id.timePickerBis);
 		timePicker2.setIs24HourView(true);
 		editTextBeschreibung = (EditText) this.findViewById(R.id.editTextBeschreibung);
 		editTextOrt = (EditText) this.findViewById(R.id.editTextOrt);
 		editTextPerson = (EditText) this.findViewById(R.id.editTextPerson);
 		datePickerDatum = (DatePicker) this.findViewById(R.id.datePickerDatum);
 		// set date and time 
 		timePicker1.setCurrentHour(hour);
 		timePicker1.setCurrentMinute(min);
 		timePicker2.setCurrentHour(hour+1);
 		timePicker2.setCurrentMinute(min);
 		datePickerDatum.updateDate(year, month, day);
 		Button btn = (Button) findViewById(R.id.buttonSave);
 	    con = this.getApplicationContext();
 		btn.setOnClickListener(new View.OnClickListener() {
 			@Override
 		    public void onClick(View v) {
 				String Type = "custom";
 				cal.set(datePickerDatum.getYear(), datePickerDatum.getMonth(), datePickerDatum.getDayOfMonth(), timePicker1.getCurrentHour(), timePicker1.getCurrentMinute(), 0);
 				long date1 = cal.getTimeInMillis();
 				cal.set(datePickerDatum.getYear(), datePickerDatum.getMonth(), datePickerDatum.getDayOfMonth(), timePicker2.getCurrentHour(), timePicker2.getCurrentMinute(), 0);
 				long date2 = cal.getTimeInMillis();
				SQLiteHelper db = new SQLiteHelper(con,Utils.getAppFilespath(con));
 				String ort = editTextOrt.getText().toString();
 				String beschreibung = editTextBeschreibung.getText().toString();
 				String person = editTextPerson.getText().toString();
 				if(date1>date2){
 					Toast.makeText(getApplicationContext(),"Fehler Zeit 'von' ist kleiner als Zeit 'bis'", Toast.LENGTH_LONG).show();
 					db.close();
 				}else{
 					db.addTerminUser("userEintrag", date1, date2, ort, "", beschreibung, "", Type);
 					db.close();
 					back();
 				}
 		    }
 		});
 		
 	}
 
 	protected void back() {
 		this.startActivity(new Intent(this, Termin_Day.class));
 	}
 
 	@Override
 	public void refreshGUI(String arg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean doInbackground() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean reloadGUI() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean setProgress() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
