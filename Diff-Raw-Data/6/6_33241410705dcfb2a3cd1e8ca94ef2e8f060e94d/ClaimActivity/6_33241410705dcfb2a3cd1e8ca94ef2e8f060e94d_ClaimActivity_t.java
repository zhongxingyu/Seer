 package com.papagiannis.tuberun;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.papagiannis.tuberun.claims.Claim;
 import com.papagiannis.tuberun.claims.ClaimStore;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.app.Dialog;
 import android.app.TabActivity;
 import android.content.DialogInterface;
 import android.database.DataSetObserver;
 import android.os.Bundle;
 import android.text.Editable;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.TabHost;
 import android.widget.TabHost.TabContentFactory;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 
 public class ClaimActivity extends TabActivity {
 
 	private static final String LIST1_TAB_TAG = "Overview";
 	private static final String LIST2_TAB_TAG = "Journey";
 	private static final String LIST3_TAB_TAG = "Delay";
 	private static final String LIST4_TAB_TAG = "Personal";
 	private static final String LIST5_TAB_TAG = "Ticket";
 
 	private TabHost tabHost;
 
 	Claim claim;
 	ClaimStore store;
 	SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy");
 	SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
 	SimpleDateFormat durationFormat = new SimpleDateFormat("mm");
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.claim);
 
 		tabHost = getTabHost();
 
 		// add views to tab host
 		tabHost.addTab(tabHost.newTabSpec(LIST1_TAB_TAG).setIndicator(LIST1_TAB_TAG)
 				.setContent(new TabContentFactory() {
 					public View createTabContent(String arg0) {
 						return (LinearLayout) findViewById(R.id.overview_tab);
 					}
 				}));
 		tabHost.addTab(tabHost.newTabSpec(LIST2_TAB_TAG).setIndicator(LIST2_TAB_TAG)
 				.setContent(new TabContentFactory() {
 					public View createTabContent(String arg0) {
 						return (LinearLayout) findViewById(R.id.journey_tab);
 					}
 				}));
 		tabHost.addTab(tabHost.newTabSpec(LIST3_TAB_TAG).setIndicator(LIST3_TAB_TAG)
 				.setContent(new TabContentFactory() {
 					public View createTabContent(String arg0) {
 						return (LinearLayout) findViewById(R.id.delay_tab);
 					}
 				}));
 		tabHost.addTab(tabHost.newTabSpec(LIST4_TAB_TAG).setIndicator(LIST4_TAB_TAG)
 				.setContent(new TabContentFactory() {
 					public View createTabContent(String arg0) {
 						return (LinearLayout) findViewById(R.id.personal_tab);
 					}
 				}));
 		tabHost.addTab(tabHost.newTabSpec(LIST5_TAB_TAG).setIndicator(LIST5_TAB_TAG)
 				.setContent(new TabContentFactory() {
 					public View createTabContent(String arg0) {
 						return (LinearLayout) findViewById(R.id.ticket_tab);
 					}
 				}));
 
 		store = ClaimStore.getInstance();
 		Bundle extras = getIntent().getExtras();
 		int index = Integer.parseInt(extras.getString("index"));
 		claim = store.getAll(this).get(index);
 
 		setupViewReferences();
 		setupViewHandlers();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		store.storeToFile(this);
 	}
 
 	// Views
 	private Spinner ticketSpinner;
 	private View submitButton;
 	private View oysterLayout;
 	private View tflLayout;
 	private View railLayout;
 	private EditText infoEdit;
 	private TextView resultView;
 	private Button journeyStartDate;
 	private Spinner journeyStartStation;
 	private Spinner journeyLineUsed;
 	private Spinner journeyEndStation;
 	private Spinner delayAtStation;
 	private Spinner delayStation1;
 	private Spinner delayStation2;
 	private RadioButton delayAt;
 	private RadioButton delayBetween;
 	private Button delayWhen;
 	private Button delayDuration;
 	private Spinner personalTitle;
 	private EditText personalSurname;
 	private EditText personalName;
 	private EditText personalLine1;
 	private EditText personalLine2;
 	private EditText personalCity;
 	private EditText personalPostcode;
 	private EditText personalPhone;
 	private EditText personalEmail;
 	private EditText personalPhotocard;
 
 	private void setupViewReferences() {
 		oysterLayout = findViewById(R.id.oyster_layout);
 		tflLayout = findViewById(R.id.tfl_layout);
 		railLayout = findViewById(R.id.rail_layout);
 		submitButton = findViewById(R.id.submit_button);
 		ticketSpinner = (Spinner) findViewById(R.id.ticket_type_spinner);
 		infoEdit = (EditText) findViewById(R.id.claim_info);
 		resultView = (TextView) findViewById(R.id.claim_result);
 		journeyStartDate = (Button) findViewById(R.id.claim_journey_startdate);
 		journeyStartStation = (Spinner) findViewById(R.id.claim_journey_startstation);
 		journeyEndStation = (Spinner) findViewById(R.id.claim_journey_endstation);
 		journeyLineUsed = (Spinner) findViewById(R.id.claim_journey_lineused);
 		delayAtStation = (Spinner) findViewById(R.id.claim_delay_atstation);
 		delayStation1 = (Spinner) findViewById(R.id.claim_delay_station1);
 		delayStation2 = (Spinner) findViewById(R.id.claim_delay_station2);
 		delayAt = (RadioButton) findViewById(R.id.claim_delay_at);
 		delayBetween = (RadioButton) findViewById(R.id.claim_delay_between);
 		delayWhen = (Button) findViewById(R.id.claim_delay_when);
 		delayDuration = (Button) findViewById(R.id.claim_delay_duration);
 		personalTitle = (Spinner) findViewById(R.id.claim_personal_title);
 		personalSurname = (EditText) findViewById(R.id.claim_personal_surname);
 		personalName = (EditText) findViewById(R.id.claim_personal_name);
 		personalLine1 = (EditText) findViewById(R.id.claim_personal_line1);
 		personalLine2 = (EditText) findViewById(R.id.claim_personal_line2);
 		personalCity = (EditText) findViewById(R.id.claim_personal_city);
 		personalPostcode = (EditText) findViewById(R.id.claim_personal_postcode);
 		personalPhone = (EditText) findViewById(R.id.claim_personal_phone);
 		personalEmail = (EditText) findViewById(R.id.claim_personal_email);
 		personalPhotocard = (EditText) findViewById(R.id.claim_personal_photocard);
 	}
 
 	private void setupViewHandlers() {
 		int i = 0;
 		for (i = 0; i < ticketSpinner.getAdapter().getCount(); i++) {
 			if (ticketSpinner.getAdapter().getItem(i).equals(claim.ticket_type))
 				break;
 		}
		if (i == ticketSpinner.getAdapter().getCount()) i=0; //the default if the claim is new
 		ticketSpinner.setSelection(i);
 		ticketSpinner.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
 				claim.ticket_type = (String) ticketSpinner.getItemAtPosition(position);
 				oysterLayout.setVisibility(claim.getTicketOysterVisibility());
 				tflLayout.setVisibility(claim.getTicketTflVisibility());
 				railLayout.setVisibility(claim.getTicketRailVisibility());
 			}
 		});
 
 		infoEdit.setText(claim.user_notes);
 		infoEdit.addTextChangedListener(new SimpleTextWatcher() {
 			@Override
 			public void afterTextChanged(Editable e) {
 				claim.user_notes = e.toString();
 			}
 		});
 
 		resultView.setText(claim.getResult());
 
 		journeyStartDate.setText(dateFormat.format(claim.journey_started));
 		journeyStartDate.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showDialog(v.getId());
 			}
 		});
 
 		final List<String> stations = StationDetails.FetchTubeStationsClaims();
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stations);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		journeyStartStation.setAdapter(adapter);
 		journeyStartStation.setSelection(stations.indexOf(claim.journey_startstation));
 		journeyStartStation.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
 				claim.journey_startstation = stations.get(position);
 			}
 		});
 
 		journeyEndStation.setAdapter(adapter);
 		journeyEndStation.setSelection(stations.indexOf(claim.journey_endstation));
 		journeyEndStation.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
 				claim.journey_endstation = stations.get(position);
 			}
 		});
 
 		final List<String> lines = LinePresentation.getLinesStringListClaims();
 		ArrayAdapter<String> lines_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lines);
 		lines_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		journeyLineUsed.setAdapter(lines_adapter);
 		journeyLineUsed.setSelection(stations.indexOf(claim.journey_lineused));
 		journeyLineUsed.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
 				claim.journey_lineused = stations.get(position);
 			}
 		});
 
 		//////////// delay tab /////////////////////
 		delayWhen.setText(timeFormat.format(claim.delay_when));
 		delayWhen.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showDialog(v.getId());
 			}
 		});
 		
 		delayDuration.setText(durationFormat.format(claim.delay_duration)+" minutes");
 		delayDuration.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showDialog(v.getId());
 			}
 		});
 		
 		updateJourneySpinners();
 
 		//I don't use a buttongroup, instead I crate and manage the group manually
 		final List<RadioButton> radioButtons = new ArrayList<RadioButton>();
 		radioButtons.add(delayAt);
 		radioButtons.add(delayBetween);
 		for (RadioButton button : radioButtons) {
 			button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				@Override
 				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 					if (isChecked)	claim.setDelayAt( buttonView.getId()==delayAt.getId() && isChecked );
 					updateJourneySpinners();
 				}
 			});
 		}
 
 		delayAtStation.setAdapter(adapter);
 		delayAtStation.setSelection(stations.indexOf(claim.getDelayAtStation()));
 		delayAtStation.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
 				claim.setDelayAtstation(stations.get(position));
 				delayStation1.setSelection(0);
 				delayStation2.setSelection(0);
 
 			}
 		});
 
 		delayStation1.setAdapter(adapter);
 		delayStation1.setSelection(stations.indexOf(claim.getDelayStation1()));
 		delayStation1.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
 				claim.setDelayStation1(stations.get(position));
 				delayAtStation.setSelection(0);
 				delayStation2.setSelection(0);
 			}
 		});
 
 		delayStation2.setAdapter(adapter);
 		delayStation2.setSelection(stations.indexOf(claim.getDelayStation2()));
 		delayStation2.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
 				claim.setDelayStation2(stations.get(position));
 				delayAtStation.setSelection(0);
 				delayStation1.setSelection(0);
 			}
 		});
 		
 		////////////personal tab /////////////////////
 		final String[] titles = getResources().getStringArray(R.array.claim_title_spinner);
 		int j=0;
		for (int ii=0;ii<titles.length;ii++) {
			if (titles[ii].equals(claim.personal_title)) { j=ii; break; }
 		}
 		personalTitle.setSelection(j);
 		personalTitle.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
 				claim.personal_title=titles[position];
 			}
 		});
 		
 		personalSurname.setText(claim.personal_surname);
 		personalSurname.addTextChangedListener(new SimpleTextWatcher() {
 			@Override
 			public void afterTextChanged(Editable e) {
 				claim.personal_surname = e.toString();
 			}
 		});
 		
 		personalName.setText(claim.personal_name);
 		personalName.addTextChangedListener(new SimpleTextWatcher() {
 			@Override
 			public void afterTextChanged(Editable e) {
 				claim.personal_name = e.toString();
 			}
 		});
 		
 		personalLine1.setText(claim.personal_address1);
 		personalLine1.addTextChangedListener(new SimpleTextWatcher() {
 			@Override
 			public void afterTextChanged(Editable e) {
 				claim.personal_address1 = e.toString();
 			}
 		});
 		
 		personalLine2.setText(claim.personal_address2);
 		personalLine2.addTextChangedListener(new SimpleTextWatcher() {
 			@Override
 			public void afterTextChanged(Editable e) {
 				claim.personal_address2 = e.toString();
 			}
 		});
 		
 		personalCity.setText(claim.personal_city);
 		personalCity.addTextChangedListener(new SimpleTextWatcher() {
 			@Override
 			public void afterTextChanged(Editable e) {
 				claim.personal_city = e.toString();
 			}
 		});
 		
 		personalPostcode.setText(claim.personal_postcode);
 		personalPostcode.addTextChangedListener(new SimpleTextWatcher() {
 			@Override
 			public void afterTextChanged(Editable e) {
 				claim.personal_postcode = e.toString();
 			}
 		});
 		
 		personalPhone.setText(claim.personal_phone);
 		personalPhone.addTextChangedListener(new SimpleTextWatcher() {
 			@Override
 			public void afterTextChanged(Editable e) {
 				claim.personal_phone = e.toString();
 			}
 		});
 		
 		personalEmail.setText(claim.personal_email);
 		personalEmail.addTextChangedListener(new SimpleTextWatcher() {
 			@Override
 			public void afterTextChanged(Editable e) {
 				claim.personal_email = e.toString();
 			}
 		});
 		
 		personalPhotocard.setText(claim.personal_photocard);
 		personalPhotocard.addTextChangedListener(new SimpleTextWatcher() {
 			@Override
 			public void afterTextChanged(Editable e) {
 				claim.personal_photocard = e.toString();
 			}
 		});
 
 	}
 
 	private void updateJourneySpinners() {
 		if (claim.isDelayAtStation()) {
 			delayAtStation.setEnabled(true);
 			delayStation1.setEnabled(false);
 			delayStation2.setEnabled(false);
 			delayAt.setChecked(true);
 			delayBetween.setChecked(false);
 		} else {
 			delayStation1.setEnabled(true);
 			delayStation2.setEnabled(true);
 			delayAtStation.setEnabled(false);
 			delayAt.setChecked(false);
 			delayBetween.setChecked(true);
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 
 		if (journeyStartDate.getId() == id) {
 			Date d = claim.journey_started;
 			return new DatePickerDialog(this, new OnDateSetListener() {
 				@Override
 				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 					claim.journey_started = new Date(year - 1900, monthOfYear, dayOfMonth);
 					journeyStartDate.setText(dateFormat.format(claim.journey_started));
 				}
 			}, d.getYear() + 1900, d.getMonth(), d.getDate());
 		}
 		else if (delayWhen.getId() == id) {
 			Date d = claim.delay_when;
 			return new TimePickerDialog(this, new OnTimeSetListener() {
 				@Override
 				public void onTimeSet(TimePicker view, int h, int m) {
 					claim.delay_when=new Date();
 					claim.delay_when.setHours(h);
 					claim.delay_when.setMinutes(m);
 					delayWhen.setText(timeFormat.format(claim.delay_when));
 				}
 			},d.getHours(),d.getMinutes(),true);
 		}
 		else if (delayDuration.getId() == id) {
 			final CharSequence[] items = {"15", "20", "25", "30", "40", "50", "59+"};
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Delay duration (minutes)");
 			builder.setItems(items, new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int item) {
 			    	claim.delay_duration=new Date();
 					claim.delay_duration.setHours(0);
 					claim.delay_duration.setMinutes(Integer.parseInt(items[item].subSequence(0, 2).toString()));
 					delayDuration.setText(durationFormat.format(claim.delay_duration) + " minutes");
 			    }
 			});
 			return builder.create();
 			
 		}
 
 		return null;
 	}
 
 }
