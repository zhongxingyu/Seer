 package sta.andswtch.gui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import sta.andswtch.R;
 import sta.andswtch.extensionLead.ExtensionLead;
 import sta.andswtch.extensionLead.ExtensionLeadManager;
 import sta.andswtch.gui.timepicker.Util;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.Message;
import android.text.format.Time;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AndSwtchView extends OptionsMenu implements IAndSwtchViews {
 
 	private final int TAG_ALL_ON = 9;
 	private final int TAG_ALL_OFF = 10;
 	private final int CONNECTIONTIMEOUTLIMIT = 3;
 
 	private ExtensionLeadManager extLeadManager;
 	private ExtensionLead extLead;
 	private List<LinearLayout> LL;
 	private List<TextView> names;
 	private List<ImageButton> buttons;
 	private CountDownTimer sinceLastRefreshTimer = null;
 	private TextView refreshtime;
	private Time time;
 	private boolean showToastMessages = true;
 	private boolean showNotConnectedMessage = true;
 	private int connectionTimeoutCount = 0;
 
 	private Handler handlerEvent = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case 1: {
 				for (int i = 1; i <= ExtensionLead.POWERPOINTCNT; i++) {
 					checkState(buttons.get(i - 1));
 				}
 				startRefreshTimer();
 				showNotConnectedMessage = true;
 				connectionTimeoutCount = 0;
 				break;
 			}
 			case 2: {
 				if (showToastMessages) {
 					Context context = getApplicationContext();
 					String text = (String) msg.obj;
 					int duration = Toast.LENGTH_SHORT;
 					Toast toast = Toast.makeText(context, text, duration);
 					if (text.equals(getString(R.string.errorConnectionTimeOut))
 							&& showNotConnectedMessage) {
 						toast.show();
 						connectionTimeoutCount++;
 						if (connectionTimeoutCount >= CONNECTIONTIMEOUTLIMIT) {
 							if (sinceLastRefreshTimer != null) {
 								sinceLastRefreshTimer.cancel();
 							}
 							refreshtime.setText(R.string.notConnected);
 							showNotConnectedMessage = false;
 						}
 						// if message is connection timed out, show only if
 						// boolean is set
 					} else if (!text
 							.equals(getString(R.string.errorConnectionTimeOut))) {
 						toast.show();
 						// if text is not connection timed out!
 					}
 
 				}
 
 			}
 			default: {
 				super.handleMessage(msg);
 				break;
 			}
 			}
 			// has to be done HERE (from outa space) cos of different threads
 			availabilityChecker();
 			// change the refresh time
 			setName();
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// create the extension lead
 		this.extLeadManager = ExtensionLeadManager.getInstance(this);
 		this.extLead = this.extLeadManager.getExtLeadFromView(this);
 
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.andswtch);
 
 		this.init();
 	}
 
 	private void init() {
		this.time = new Time();

 		this.names = new ArrayList<TextView>();
 		this.names.add((TextView) findViewById(R.id.TextView01));
 		this.names.add((TextView) findViewById(R.id.TextView02));
 		this.names.add((TextView) findViewById(R.id.TextView03));
 		this.names.add((TextView) findViewById(R.id.TextView04));
 		this.names.add((TextView) findViewById(R.id.TextView05));
 		this.names.add((TextView) findViewById(R.id.TextView06));
 		this.names.add((TextView) findViewById(R.id.TextView07));
 		this.names.add((TextView) findViewById(R.id.TextView08));
 
 		this.LL = new ArrayList<LinearLayout>();
 		this.LL.add((LinearLayout) findViewById(R.id.LL00));
 		this.LL.add((LinearLayout) findViewById(R.id.LL01));
 		this.LL.add((LinearLayout) findViewById(R.id.LL02));
 		this.LL.add((LinearLayout) findViewById(R.id.LL03));
 		this.LL.add((LinearLayout) findViewById(R.id.LL04));
 		this.LL.add((LinearLayout) findViewById(R.id.LL05));
 		this.LL.add((LinearLayout) findViewById(R.id.LL06));
 		this.LL.add((LinearLayout) findViewById(R.id.LL07));
 		this.LL.add((LinearLayout) findViewById(R.id.LL08));
 
 		this.buttons = new ArrayList<ImageButton>();
 		this.buttons.add((ImageButton) findViewById(R.id.Button01));
 		this.buttons.add((ImageButton) findViewById(R.id.Button02));
 		this.buttons.add((ImageButton) findViewById(R.id.Button03));
 		this.buttons.add((ImageButton) findViewById(R.id.Button04));
 		this.buttons.add((ImageButton) findViewById(R.id.Button05));
 		this.buttons.add((ImageButton) findViewById(R.id.Button06));
 		this.buttons.add((ImageButton) findViewById(R.id.Button07));
 		this.buttons.add((ImageButton) findViewById(R.id.Button08));
 
 		refreshtime = (TextView) findViewById(R.id.refreshtime);
 
 		this.availabilityChecker();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		this.connectionTimeoutCount = 0;
 		this.showToastMessages = true;
 		this.extLead = this.extLeadManager.getExtLeadFromView(this);
 
 		this.extLead.sendUpdateMessage();
 		this.extLead.startAutoRefreshRunning();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 1, 0, R.string.om_refresh);
 		menu.getItem(0).setIcon(R.drawable.ic_menu_refresh);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case 1: {
 			this.extLead.sendUpdateMessage();
 			return true;
 		}
 		default: {
 			return super.onOptionsItemSelected(item);
 		}
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		this.showToastMessages = false;
 		this.extLead.stopAutoRefreshRunning();
 	}
 
 	public Context getAppContext() {
 		return this.getApplicationContext();
 	}
 
 	public void updateActivity() {
 		Message msg = new Message();
 		msg.what = 1;
 		this.handlerEvent.sendMessage(msg);
 	}
 
 	public void showErrorMessage(String message) {
 		Message msg = new Message();
 		msg.what = 2;
 		msg.obj = message;
 		this.handlerEvent.sendMessage(msg);
 	}
 
 	public void showErrorMessage(int messageResourceId) {
 		this.showErrorMessage(this.getAppContext().getString(messageResourceId));
 	}
 
 	private void availabilityChecker() {
 		int cntUnavailables = 0;
 		// Checks if the powerpoints are enabled
 		// Makes the powerpoints visible if enabled
 		for (int i = 1; i <= ExtensionLead.POWERPOINTCNT; i++) {
 			if (this.extLead.isPowerPointEnable(i)) {
 				this.LL.get(i).setVisibility(View.VISIBLE);
 				this.names.get(i - 1)
 						.setText(this.extLead.getPowerPointName(i));
 				this.checkState(this.buttons.get(i - 1));
 			} else {
 				this.LL.get(i).setVisibility(View.GONE);
 				cntUnavailables++;
 			}
 		}
 		// Makes the progressbar invisible and switcherbuttons visible
 		if (cntUnavailables == ExtensionLead.POWERPOINTCNT) {
 			this.LL.get(0).setVisibility(View.VISIBLE);
 			findViewById(R.id.allOn).setVisibility(View.INVISIBLE);
 			findViewById(R.id.allOff).setVisibility(View.INVISIBLE);
 		} else {
 			this.LL.get(0).setVisibility(View.GONE);
 			findViewById(R.id.allOn).setVisibility(View.VISIBLE);
 			findViewById(R.id.allOff).setVisibility(View.VISIBLE);
 		}
 	}
 
 	private void startRefreshTimer() {
 
 		if (sinceLastRefreshTimer != null) {
 			sinceLastRefreshTimer.cancel();
 		}
 
 		// set the endtime for the counter, if autorefresh is disabled, set to 1
 		// hour
 		long endTimeMillis;
 		endTimeMillis = 60 * 60 * 1000; // one hour
 
 		sinceLastRefreshTimer = new CountDownTimer(endTimeMillis, 1000) {
 
 			int secondsSinceLastRefresh = 0;
 
 			public void setRefreshTime() {
 				String hour = Util.pad(secondsSinceLastRefresh / (60 * 60));
 				String min = Util
 						.pad((secondsSinceLastRefresh % (60 * 60) / 60));
 				String sec = Util.pad((secondsSinceLastRefresh % (60)));
 
 				refreshtime.setText(hour + ":" + min + ":" + sec);
 			}
 
 			@Override
 			public void onTick(long millisUntilFinished) {
 				secondsSinceLastRefresh++;
 				setRefreshTime();
 
 			}
 
 			@Override
 			public void onFinish() {
 				extLead.sendUpdateMessage();
 				// refreshtime.setText(R.string);
 			}
 		}.start();
 	}
 
 	private void setName() {
 		if (this.extLead.getName() != "")
 			this.setTitle("AndSwtch - " + this.extLead.getName());
 	}
 
 	public void onOff(View v) {
 		// tag 1 - 8 for each power point
 		// tag 9 for all on and tag 10 for all off
 		String tagString = (String) v.getTag();
 		if (tagString != null) {
 			int tag = Integer.parseInt(tagString);
 			if (tag == TAG_ALL_ON) {
 				this.extLead.sendStateAll(true);
 			} else if (tag == TAG_ALL_OFF) {
 				this.extLead.sendStateAll(false);
 			} else {
 				this.extLead.switchState(tag);
 			}
 		}
 	}
 
 	public void switchToPowerPoint(View v) {
 		Intent toLaunch = new Intent(v.getContext(), PowerPointView.class);
 		toLaunch.putExtra("powerPoint", (String) v.getTag());
 		if (this.extLead.getName() != "")
 			toLaunch.putExtra("name", this.extLead.getName());
 		startActivity(toLaunch);
 	}
 
 	private void checkState(ImageButton btn) {
 		String tagString = (String) btn.getTag();
 		if (tagString != null) {
 			int tag = Integer.parseInt(tagString);
 			if (this.extLead.isPowerPointOn(tag)) {
 				this.setOn(btn);
 			} else {
 				this.setOff(btn);
 			}
 		}
 	}
 
 	private void setOn(ImageButton btn) {
 		btn.setImageResource(R.drawable.onbutton);
 
 		// btn.setBackgroundColor(Color.GREEN);
 		// btn.setText("ON");
 	}
 
 	private void setOff(ImageButton btn) {
 		btn.setImageResource(R.drawable.offbutton);
 		// btn.setBackgroundColor(Color.RED);
 		// btn.setText("OFF");
 	}
 
 }
