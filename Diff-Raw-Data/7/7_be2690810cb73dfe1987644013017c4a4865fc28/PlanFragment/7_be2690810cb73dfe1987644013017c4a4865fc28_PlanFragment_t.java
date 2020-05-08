 package com.papagiannis.tuberun;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 import com.papagiannis.tuberun.fetchers.Observer;
 import com.papagiannis.tuberun.fetchers.PlanFetcher;
 import com.papagiannis.tuberun.plan.Point;
 
 public class PlanFragment extends Fragment implements Observer,
 		OnClickListener, OnCheckedChangeListener {
 	private final PlanFragment self = this;
 	static final int SELECT_PAST_DESTINATION_DIALOG = -9;
 	static final int LOCATION_SERVICE_FAILED = -8;
 	private final static int SELECT_TRAVEL_DATE = -7;
 	private final static int SELECT_ALTERNATIVE = -6;
 	public final static int SET_HOME_DIALOG = -4;
 	private final static int ADD_HOME_ERROR = -5;
 	private final static int PLAN_ERROR_DIALOG = -3;
 	private final static int ERROR_DIALOG = -2;
 	private final static int LOCATION_DIALOG = -1;
 	private final static int WAIT_DIALOG = 0;
 
 	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
 	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
 			"EEEE, dd/MM/yyyy");
 
 	PlanActivity planActivity;
 
 	PlanFetcher fetcher = new PlanFetcher(PlanActivity.getPlan());
 
 	LinearLayout go_layout;
 	Button history_button;
 	LinearLayout advanced_layout;
 	Button advanced_button;
 	LinearLayout previous_layout;
 	TextView previous_textview;
 	EditText destination_edittext;
 	CheckBox fromcurrent_checkbox;
 	RadioGroup destination_radiogroup;
 	RadioButton tostation_radiobutton;
 	RadioButton topoi_radiobutton;
 	RadioButton topostcode_radiobutton;
 	RadioButton toaddress_radiobutton;
 	RadioGroup from_radiogroup;
 	EditText from_edittext;
 	RadioButton fromstation_radiobutton;
 	RadioButton frompoi_radiobutton;
 	RadioButton frompostcode_radiobutton;
 	RadioButton fromaddress_radiobutton;
 	RadioButton departtime_radiobutton;
 	RadioButton arrivetime_radiobutton;
 	RadioButton departtimenow_radiobutton;
 	RadioButton departtimelater_radiobutton;
 	Button departtimelater_button;
 	Button arrivetime_button;
 	CheckBox use_tube_checkbox;
 	CheckBox use_bus_checkbox;
 	CheckBox use_dlr_checkbox;
 	CheckBox use_rail_checkbox;
 	CheckBox use_boat_checkbox;
 	CheckBox traveldate_checkbox;
 	Button traveldate_button;
 	LinearLayout from_layout;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = null;
 		try {
 			v = inflater.inflate(R.layout.plan_fragment, null);
 			createReferences(v);
 			create();
 		} catch (Exception e) {
 			Log.w("PlanFragment", e);
 		}
 		return v;
 	}
 
 	private void createReferences(View v) {
 		go_layout = (LinearLayout) v.findViewById(R.id.go_layout);
 		destination_edittext = (EditText) v
 				.findViewById(R.id.destination_edittext);
 		destination_radiogroup = (RadioGroup) v
 				.findViewById(R.id.destination_radiogroup);
 		topoi_radiobutton = (RadioButton) v
 				.findViewById(R.id.topoi_radiobutton);
 		tostation_radiobutton = (RadioButton) v
 				.findViewById(R.id.tostation_radiobutton);
 		toaddress_radiobutton = (RadioButton) v
 				.findViewById(R.id.toaddress_radiobutton);
 		topostcode_radiobutton = (RadioButton) v
 				.findViewById(R.id.topostcode_radiobutton);
 		advanced_button = (Button) v.findViewById(R.id.advanced_button);
 		advanced_layout = (LinearLayout) v.findViewById(R.id.adv2anced_layout);
 		// previous_layout = (LinearLayout)
 		// v.findViewById(R.id.previous_layout);
 		// previous_textview = (TextView)
 		// v.findViewById(R.id.previous_textview);
 		fromcurrent_checkbox = (CheckBox) v
 				.findViewById(R.id.fromcurrent_checkbox);
 		from_radiogroup = (RadioGroup) v.findViewById(R.id.from_radiogroup);
 		from_edittext = (EditText) v.findViewById(R.id.from_edittext);
 		frompoi_radiobutton = (RadioButton) v
 				.findViewById(R.id.frompoi_radiobutton);
 		fromstation_radiobutton = (RadioButton) v
 				.findViewById(R.id.fromstation_radiobutton);
 		fromaddress_radiobutton = (RadioButton) v
 				.findViewById(R.id.fromaddress_radiobutton);
 		frompostcode_radiobutton = (RadioButton) v
 				.findViewById(R.id.frompostcode_radiobutton);
 		departtime_radiobutton = (RadioButton) v
 				.findViewById(R.id.departtime_radiobutton);
 		arrivetime_radiobutton = (RadioButton) v
 				.findViewById(R.id.arrivetime_radiobutton);
 		departtimenow_radiobutton = (RadioButton) v
 				.findViewById(R.id.departtimenow_radiobutton);
 		departtimelater_radiobutton = (RadioButton) v
 				.findViewById(R.id.departtimelater_radiobutton);
 		departtimelater_button = (Button) v
 				.findViewById(R.id.departtimelater_button);
 		arrivetime_button = (Button) v.findViewById(R.id.arrivetime_button);
 		use_boat_checkbox = (CheckBox) v.findViewById(R.id.useboat_checkbox);
 		use_bus_checkbox = (CheckBox) v.findViewById(R.id.usebus_checkbox);
 		use_dlr_checkbox = (CheckBox) v.findViewById(R.id.usedlr_checkbox);
 		use_rail_checkbox = (CheckBox) v.findViewById(R.id.userail_checkbox);
 		use_tube_checkbox = (CheckBox) v.findViewById(R.id.usetube_checkbox);
 		traveldate_checkbox = (CheckBox) v
 				.findViewById(R.id.traveldate_checkbox);
 		traveldate_button = (Button) v.findViewById(R.id.traveldate_button);
 		from_layout = (LinearLayout) v.findViewById(R.id.from_layout);
 		history_button = (Button) v.findViewById(R.id.history_button);
 	}
 
 	private void create() {
 		go_layout.setOnClickListener(this);
 		go_layout.setVisibility(View.GONE);
 
 		history_button.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				showDialog(SELECT_PAST_DESTINATION_DIALOG);
 
 			}
 		});
 
 		// Listeners to store the values of the editboxes
 		from_edittext.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				PlanActivity.getPlan().setStartingString(s.toString());
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 			}
 		});
 		destination_edittext.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				PlanActivity.getPlan().setDestination(s.toString());
 				if (s!=null && !s.toString().trim().equals("")) {
 					go_layout.setVisibility(View.VISIBLE);
 				}
 				else {
 					go_layout.setVisibility(View.GONE);
 				}
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 			}
 		});
 
 		// listener for the selectDate buttons
 		OnClickListener l = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(v.getId());
 			}
 		};
 		departtimelater_button.setOnClickListener(l);
 		arrivetime_button.setOnClickListener(l);
 
 		// I don't use a buttongroup, instead I create and manage the group
 		// manually
 		final List<RadioButton> constraintRadioButtons = new ArrayList<RadioButton>();
 		constraintRadioButtons.add(departtime_radiobutton);
 		constraintRadioButtons.add(arrivetime_radiobutton);
 		for (RadioButton button : constraintRadioButtons) {
 			button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				@Override
 				public void onCheckedChanged(CompoundButton buttonView,
 						boolean isChecked) {
 					// store the value in the plan
 					if (isChecked)
 						PlanActivity.getPlan().setTimeConstraint(
 								buttonView.getId() == departtime_radiobutton
 										.getId());
 
 					// fix the other buttons in the group
 					if (isChecked) {
 						for (RadioButton b : constraintRadioButtons) {
 							if (buttonView.getId() != b.getId())
 								b.setChecked(false);
 						}
 					}
 					// render them enabled or disabled
 					if (buttonView.getId() == departtime_radiobutton.getId()) {
 						departtimenow_radiobutton.setEnabled(isChecked
 								&& !traveldate_checkbox.isChecked());
 						departtimelater_radiobutton.setEnabled(isChecked);
 						departtimelater_button.setEnabled(isChecked);
 					}
 					if (buttonView.getId() == arrivetime_radiobutton.getId()) {
 						arrivetime_button.setEnabled(isChecked);
 						departtimelater_button
 								.setEnabled(departtimelater_radiobutton
 										.isChecked()
 										&& departtimelater_radiobutton
 												.isEnabled());
 					}
 				}
 			});
 		}
 
 		// I don't use a buttongroup, instead I create and manage the group
 		// manually
 		final List<RadioButton> departAtRadioButtons = new ArrayList<RadioButton>();
 		departAtRadioButtons.add(departtimenow_radiobutton);
 		departAtRadioButtons.add(departtimelater_radiobutton);
 		for (RadioButton button : departAtRadioButtons) {
 			button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				@Override
 				public void onCheckedChanged(CompoundButton buttonView,
 						boolean isChecked) {
 					// store the value in the plan
 					if (isChecked)
 						PlanActivity.getPlan().setTimeDepartureNow(
 								buttonView.getId() == departtimenow_radiobutton
 										.getId());
 
 					// fix the other buttons in the group
 					if (isChecked) {
 						for (RadioButton b : departAtRadioButtons) {
 							if (buttonView.getId() != b.getId())
 								b.setChecked(false);
 						}
 					}
 					if (buttonView.getId() == departtimelater_radiobutton
 							.getId()) {
 						departtimelater_button.setEnabled(isChecked);
 					}
 				}
 			});
 		}
 
 		// Setup handlers for the checkbox
 		fromcurrent_checkbox.setOnCheckedChangeListener(this);
 
 		// Setup handlers for the more/less button
 		advanced_layout.setVisibility(View.GONE);
 		advanced_button.setOnClickListener(new OnClickListener() {
 			private boolean isAdvanced = false;
 
 			@Override
 			public void onClick(View v) {
 				isAdvanced = !isAdvanced;
 				int advanced_visibility = (isAdvanced) ? View.VISIBLE
 						: View.GONE;
 				advanced_layout.setVisibility(advanced_visibility);
 				advanced_button.setText(!isAdvanced ? "More>>" : "<<Less");
 				advanced_button.setVisibility(View.GONE);
 			}
 		});
 
 		use_boat_checkbox.setOnCheckedChangeListener(this);
 		use_rail_checkbox.setOnCheckedChangeListener(this);
 		use_bus_checkbox.setOnCheckedChangeListener(this);
 		use_dlr_checkbox.setOnCheckedChangeListener(this);
 		use_tube_checkbox.setOnCheckedChangeListener(this);
 
 		topoi_radiobutton.setOnCheckedChangeListener(this);
 		toaddress_radiobutton.setOnCheckedChangeListener(this);
 		topostcode_radiobutton.setOnCheckedChangeListener(this);
 		tostation_radiobutton.setOnCheckedChangeListener(this);
 
 		frompoi_radiobutton.setOnCheckedChangeListener(this);
 		fromaddress_radiobutton.setOnCheckedChangeListener(this);
 		frompostcode_radiobutton.setOnCheckedChangeListener(this);
 		fromstation_radiobutton.setOnCheckedChangeListener(this);
 
 		traveldate_checkbox
 				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 					@Override
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						if (isChecked) {
 							traveldate_button.setEnabled(true);
 							departtimenow_radiobutton.setEnabled(false);
 							if (departtimenow_radiobutton.isChecked())
 								departtimelater_radiobutton.setChecked(true);
 						} else {
 							PlanActivity.getPlan().setTravelDate(null);
 							departtimenow_radiobutton.setEnabled(true);
 							traveldate_button.setText("Select travel date");
 							traveldate_button.setEnabled(false);
 						}
 
 					}
 				});
 		traveldate_button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(SELECT_TRAVEL_DATE);
 			}
 		});
 
 	}
 
 	Destination dnew_home;
 
 	void restoreDestination(Destination d) {
 		final boolean restoreToUI = true;
 		if (d == null || d.getDestination().equals(""))
 			return;
 		if (restoreToUI)
 			destination_edittext.setText(d.getDestination());
 		else
 			PlanActivity.getPlan().setDestination(d.getDestination());
 		Point type = d.getType();
 		switch (type) {
 		case ADDRESS:
 			if (restoreToUI)
 				toaddress_radiobutton.setChecked(true);
 			else
 				PlanActivity.getPlan().setDestinationType(Point.ADDRESS);
 			break;
 		case POI:
 			if (restoreToUI)
 				topoi_radiobutton.setChecked(true);
 			else
 				PlanActivity.getPlan().setDestinationType(Point.POI);
 			break;
 		case POSTCODE:
 			if (restoreToUI)
 				topostcode_radiobutton.setChecked(true);
 			else
 				PlanActivity.getPlan().setDestinationType(Point.POSTCODE);
 			break;
 		case STATION:
 			if (restoreToUI)
 				tostation_radiobutton.setChecked(true);
 			else
 				PlanActivity.getPlan().setDestinationType(Point.STATION);
 			break;
 		}
 	}
 
 	private Dialog getAddHomeDialog() {
 		ArrayList<Destination> h = planActivity.store.getAll(getActivity());
 		if (h.size() > 0) {
 			final String[] items = new String[h.size()+1];
 			items[0]="Past destinations:";
 			for (int i = 0; i < h.size(); i++) {
 				items[i+1] = h.get(i).getDestination();
 			}
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Set Your Home Address");
 			builder.setItems(items, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int item) {
 					if (item==0) return;
 					Destination dest = planActivity.store.get(item-1,
 							getActivity());
 					Destination d = new Destination(dest.getDestination(), dest
 							.getType());
 					d.setHome(true);
 					planActivity.store.setHome(d, getActivity());
 					planActivity.updateHomeButton();
 				}
 			});
 			
 			return builder.create();
 		}
 		else {
 			AlertDialog.Builder builder = new AlertDialog.Builder(planActivity);
 			builder.setTitle("Home Address Not Set")
 					.setMessage(
 							"You may only set your home address after having attempted to plan a journey.")
 					.setCancelable(true).setPositiveButton("OK", null);
 			return builder.create();
 		}
 	}
 
 	private Dialog getHistoryDialog() {
 		ArrayList<Destination> h = planActivity.store.getAll(getActivity());
 		final String[] items = new String[h.size()];
 		for (int i = 0; i < h.size(); i++) {
 			items[i] = h.get(i).getDestination();
 		}
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setTitle("Past destinations");
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				Destination dest = planActivity.store.get(item, getActivity());
 				Destination d = new Destination(dest.getDestination(), dest
 						.getType());
 				restoreDestination(d);
 			}
 		});
 		Destination d = planActivity.store.getHome(getActivity());
 		boolean existsHome = d != null && !d.getDestination().equals("")
 				&& d.isHome();
 		if (existsHome) {
 			builder.setNeutralButton("Erase Home Address", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					planActivity.store.eraseHome(getActivity());
 					planActivity.updateHomeButton();
 				}
 			});
 		} 
 		return builder.create();
 	}
 
 	private Dialog wait_dialog;
 	private boolean is_location_dialog = false;
 	boolean is_wait_dialog = false;
 
 	@SuppressWarnings("deprecation")
 	protected Dialog showDialog(int id) {
 		Dialog ret = null;
 		if (id == LOCATION_SERVICE_FAILED) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(planActivity);
 			builder.setTitle("Location Service Failed")
 					.setMessage(
 							"Does you device support location services? Turn them on in the settings.")
 					.setCancelable(true).setPositiveButton("OK", null);
 			ret = builder.create();
 		} else if (id == LOCATION_DIALOG) {
 			ProgressDialog d = new ProgressDialog(getActivity());
 			d.setTitle("Fetching your location");
 			d.setCancelable(true);
 			d.setIndeterminate(true);
 			is_location_dialog = true;
 			updateLocationDialog(d, planActivity.location_textview.getText(),
 					planActivity.getCurrentLocation().getAccuracy() + "");
 			d.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							is_location_dialog = false;
 							wait_dialog.cancel();
 							if (!PlanActivity.getPlan().isValid()) {
 								showDialog(PLAN_ERROR_DIALOG);
 								return;
 							}
 							planActivity.stopLocationUpdates();
 							showDialog(WAIT_DIALOG);
 							planActivity.storeDestination();
 							requestPlan();
 						}
 					});
 			d.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							is_location_dialog = false;
 							wait_dialog.cancel();
 						}
 					});
 			ret = d;
 		} else if (id == WAIT_DIALOG) {
 			is_wait_dialog = true;
 			ProgressDialog p = new ProgressDialog(getActivity());
 			p.setMessage("Please wait...");
 			p.setTitle("Fetching travel plans");
 			p.setOnCancelListener(new OnCancelListener() {
 				@Override
 				public void onCancel(DialogInterface dialog) {
 					wait_dialog.cancel();
 					is_wait_dialog = false;
 					fetcher.clearCallbacks();
 					fetcher.abort();
 				}
 			});
 			p.setCancelable(true);
 			p.setIndeterminate(true);
 			ret = p;
 		} else if (id == SELECT_ALTERNATIVE) {
 			if (showAlternativeDestinations) {
 				String[] d = {};
 				final String[] items = PlanActivity.getPlan()
 						.getAlternativeDestinations().toArray(d);
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						getActivity());
 				builder.setTitle("Pick an alternative destination");
 				builder.setItems(items, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 						showAlternativeDestinations = false;
 						PlanActivity.getPlan().setDestination(items[item]);
 						PlanActivity.getPlan().clearAlternativeDestinations();
 						destination_edittext.setText(items[item]);
 						if (showAlternativeOrigins)
 							showDialog(SELECT_ALTERNATIVE);
 						else {
 							wait_dialog.cancel();
 							showDialog(WAIT_DIALOG);
 							planActivity.storeDestination();
 							requestPlan();
 						}
 					}
 				});
 				builder.setOnCancelListener(new OnCancelListener() {
 					@Override
 					public void onCancel(DialogInterface dialog) {
 						// self.removeDialog(SELECT_ALTERNATIVE); // prevent
 						// caching
 					}
 				});
 				AlertDialog alert = builder.create();
 				ret = alert;
 				showAlternativeDestinations = false;
 			} else if (showAlternativeOrigins) {
 				String[] d = {};
 				final String[] items = PlanActivity.getPlan()
 						.getAlternativeOrigins().toArray(d);
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						getActivity());
 				builder.setTitle("Pick an alternative origin");
 				builder.setItems(items, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 						showAlternativeOrigins = false;
 						// self.removeDialog(SELECT_ALTERNATIVE); // prevent
 						// caching
 						PlanActivity.getPlan().setStartingString(items[item]);
 						PlanActivity.getPlan().clearAlternativeOrigins();
 						from_edittext.setText(items[item]);
 						if (showAlternativeDestinations)
 							showDialog(SELECT_ALTERNATIVE);
 						else {
 							wait_dialog.cancel();
 							showDialog(WAIT_DIALOG);
 							planActivity.storeDestination();
 							requestPlan();
 						}
 					}
 				});
 				builder.setOnCancelListener(new OnCancelListener() {
 					@Override
 					public void onCancel(DialogInterface dialog) {
 						// self.removeDialog(SELECT_ALTERNATIVE); // prevent
 						// caching
 					}
 				});
 				AlertDialog alert = builder.create();
 				showAlternativeOrigins = false;
 				ret = alert;
 			}
 		} else if (id == ERROR_DIALOG || id == PLAN_ERROR_DIALOG
 				|| id == ADD_HOME_ERROR) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			String title = "";
 			switch (id) {
 			case ERROR_DIALOG:
 				title = "Error";
 				break;
 			case PLAN_ERROR_DIALOG:
 				title = "Planning error";
 				break;
 			case ADD_HOME_ERROR:
 				title = "Home address not set";
 				break;
 			}
 			builder.setTitle(title)
 					.setMessage(
 							id == ADD_HOME_ERROR ? "Use one of the house icons next to past destinations to set it."
 									: PlanActivity.getPlan().getError()
 											+ fetcher.getErrors())
 					.setCancelable(false)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									wait_dialog.cancel();
 									// self.removeDialog(id); // prevent caching
 								}
 							});
 			ret = builder.create();
 		} else if (id == SET_HOME_DIALOG) {
 			ret = getAddHomeDialog();
 		} else if (departtimelater_button.getId() == id) {
 			Date d = PlanActivity.getPlan().getTimeDepartureLater();
 			if (d == null)
 				d = new Date();
 			ret = new TimePickerDialog(getActivity(),
 					new TimePickerDialog.OnTimeSetListener() {
 						@Override
 						public void onTimeSet(TimePicker view, int h, int m) {
 							Date d = new Date();
 							d.setHours(h);
 							d.setMinutes(m);
 							PlanActivity.getPlan().setTimeDepartureLater(d);
 							departtimelater_button.setText(timeFormat.format(d));
 						}
 					}, d.getHours(), d.getMinutes(), true);
 		} else if (arrivetime_button.getId() == id) {
 			Date d = PlanActivity.getPlan().getTimeArrivalLater();
 			if (d == null)
 				d = new Date();
 			ret = new TimePickerDialog(getActivity(),
 					new TimePickerDialog.OnTimeSetListener() {
 						@Override
 						public void onTimeSet(TimePicker view, int h, int m) {
 							Date d = new Date();
 							d.setHours(h);
 							d.setMinutes(m);
 							PlanActivity.getPlan().setTimeArrivalLater(d);
 							arrivetime_button.setText(timeFormat.format(d));
 						}
 					}, d.getHours(), d.getMinutes(), true);
 		} else if (id == SELECT_TRAVEL_DATE) {
 			Date d = PlanActivity.getPlan().getTravelDate();
 			if (d == null)
 				d = new Date();
 			ret = new DatePickerDialog(getActivity(),
 					new DatePickerDialog.OnDateSetListener() {
 						@Override
 						public void onDateSet(DatePicker view, int year,
 								int monthOfYear, int dayOfMonth) {
 							Date now = new Date();
 							now.setYear(year - 1900);
 							now.setMonth(monthOfYear);
 							now.setDate(dayOfMonth);
 							PlanActivity.getPlan().setTravelDate(now);
 							traveldate_button.setText(dateFormat.format(now));
 						}
 					}, d.getYear() + 1900, d.getMonth(), d.getDate());
 		} else if (id == SELECT_PAST_DESTINATION_DIALOG) {
 			ret = getHistoryDialog();
 		}
 		wait_dialog = ret;
 		ret.show();
 		return ret;
 	}
 
 	void updateLocationDialog(ProgressDialog pd,
 			CharSequence previous_location, CharSequence accuracy) {
 		if (pd == null && is_location_dialog && wait_dialog != null)
 			pd = (ProgressDialog) wait_dialog;
 		if (pd != null) {
 			if (previous_location.equals(""))
 				previous_location = "(...)";
 			pd.setMessage("Your current location is required to plan a journey.\n\n"
 					+ "Location="
 					+ previous_location
 					+ "\n"
 					+ "Accuracy="
 					+ accuracy
 					+ "m\n\n"
 					+ "Press OK when the accuracy is acceptable.");
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == go_layout.getId()) {
 			// if the accuracy is not great wait more in a dialogue
 			Point type = PlanActivity.getPlan().getStartingType();
 			Location location = PlanActivity.getPlan().getStartingLocation();
 			if (type == Point.LOCATION
 					&& (location == null || location.getAccuracy() > 50))
 				showDialog(LOCATION_DIALOG);
 			else {
 				if (!PlanActivity.getPlan().isValid()) {
 					showDialog(PLAN_ERROR_DIALOG);
 					return;
 				}
 				planActivity.stopLocationUpdates();
 				showDialog(WAIT_DIALOG);
 				planActivity.storeDestination();
 				requestPlan();
 			}
 		}
 	}
 
 	private boolean showAlternativeDestinations = false;
 	private boolean showAlternativeOrigins = false;
 
 	private void requestPlan() {
 		showAlternativeDestinations = false;
 		showAlternativeOrigins = false;
 		PlanActivity.getPlan().clearAlternativeDestinations();
 		PlanActivity.getPlan().clearAlternativeOrigins();
 
 		fetcher.clearCallbacks();
 		fetcher = new PlanFetcher(PlanActivity.getPlan());
 		fetcher.registerCallback(this);
 		fetcher.update();
 	}
 
 	@Override
 	public void update() {
 		is_wait_dialog = false;
 		wait_dialog.dismiss();
 		planActivity.removeDialog(WAIT_DIALOG);
 		if (!fetcher.isErrorResult()) {
 			PlanActivity.setPlan(fetcher.getResult());
 			if (PlanActivity.getPlan().hasAlternatives()) {
 				showAlternativeDestinations = PlanActivity.getPlan()
 						.getAlternativeDestinations().size() > 0;
 				showAlternativeOrigins = PlanActivity.getPlan()
 						.getAlternativeOrigins().size() > 0;
 				showDialog(SELECT_ALTERNATIVE);
 			} else {
 				Intent i = new Intent(getActivity(), RouteResultsActivity.class);
 				startActivity(i);
 			}
 		} else {
 			showDialog(ERROR_DIALOG);
 		}
 	}
 
 	private void setFromViewsEnabled(boolean isEnabled) {
 		from_edittext.setEnabled(isEnabled);
 		fromstation_radiobutton.setEnabled(isEnabled);
 		frompoi_radiobutton.setEnabled(isEnabled);
 		fromaddress_radiobutton.setEnabled(isEnabled);
 		frompostcode_radiobutton.setEnabled(isEnabled);
 		if (isEnabled)
 			from_layout.setVisibility(View.VISIBLE);
 		else
 			from_layout.setVisibility(View.GONE);
 	}
 
 	@Override
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		int bid = buttonView.getId();
 		if (isChecked) {
 			if (bid == toaddress_radiobutton.getId()) {
 				PlanActivity.getPlan().setDestinationType(Point.ADDRESS);
 			} else if (bid == topoi_radiobutton.getId()) {
 				PlanActivity.getPlan().setDestinationType(Point.POI);
 			} else if (bid == topostcode_radiobutton.getId()) {
 				PlanActivity.getPlan().setDestinationType(Point.POSTCODE);
 			} else if (bid == tostation_radiobutton.getId()) {
 				PlanActivity.getPlan().setDestinationType(Point.STATION);
 			} else if (bid == fromaddress_radiobutton.getId()) {
 				PlanActivity.getPlan().setStartingType(Point.ADDRESS);
 			} else if (bid == frompoi_radiobutton.getId()) {
 				PlanActivity.getPlan().setStartingType(Point.POI);
 			} else if (bid == frompostcode_radiobutton.getId()) {
 				PlanActivity.getPlan().setStartingType(Point.POSTCODE);
 			} else if (bid == fromstation_radiobutton.getId()) {
 				PlanActivity.getPlan().setStartingType(Point.STATION);
 			}
 		}
 
 		if (bid == use_boat_checkbox.getId()) {
 			PlanActivity.getPlan().setUseBoat(isChecked);
 		} else if (bid == use_rail_checkbox.getId()) {
			PlanActivity.getPlan().setUseRail(isChecked);
 		} else if (bid == use_bus_checkbox.getId()) {
 			PlanActivity.getPlan().setUseBuses(isChecked);
		} else if (bid == use_tube_checkbox.getId()) {
			PlanActivity.getPlan().setUseTube(isChecked);
 		} else if (bid == use_dlr_checkbox.getId()) {
 			PlanActivity.getPlan().setUseDLR(isChecked);
 		} else if (bid == fromcurrent_checkbox.getId()) {
 			if (isChecked) {
 				PlanActivity.getPlan().setStartingType(Point.LOCATION);
 				planActivity.requestLocationUpdates();
 				planActivity.location_layout.setVisibility(View.VISIBLE);
 				setFromViewsEnabled(false);
 			} else {
 				updatePlanFromType();
 				planActivity.stopLocationUpdates();
 				planActivity.location_layout.setVisibility(View.GONE);
 				setFromViewsEnabled(true);
 			}
 		}
 
 	}
 
 	private void updatePlanFromType() {
 		int selected = from_radiogroup.getCheckedRadioButtonId();
 		if (selected == fromaddress_radiobutton.getId())
 			PlanActivity.getPlan().setStartingType(Point.ADDRESS);
 		else if (selected == frompoi_radiobutton.getId())
 			PlanActivity.getPlan().setStartingType(Point.POI);
 		else if (selected == fromstation_radiobutton.getId())
 			PlanActivity.getPlan().setStartingType(Point.STATION);
 		else if (selected == frompostcode_radiobutton.getId())
 			PlanActivity.getPlan().setStartingType(Point.POSTCODE);
 		else
 			PlanActivity.getPlan().setStartingType(Point.NONE);
 	}
 
 	private void updatePlanDestinationType(boolean isChecked) {
 		if (!isChecked)
 			return;
 		int selected = destination_radiogroup.getCheckedRadioButtonId();
 		if (selected == toaddress_radiobutton.getId())
 			PlanActivity.getPlan().setDestinationType(Point.ADDRESS);
 		else if (selected == topoi_radiobutton.getId())
 			PlanActivity.getPlan().setDestinationType(Point.POI);
 		else if (selected == tostation_radiobutton.getId())
 			PlanActivity.getPlan().setDestinationType(Point.STATION);
 		else if (selected == topostcode_radiobutton.getId())
 			PlanActivity.getPlan().setDestinationType(Point.POSTCODE);
 		else
 			PlanActivity.getPlan().setStartingType(Point.NONE);
 	}
 
 }
