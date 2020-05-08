 package billsplit.ui;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 import billsplit.engine.Account;
 import billsplit.engine.BalanceChange;
 import billsplit.engine.Event;
 import billsplit.engine.Participant;
 import billsplit.engine.Transaction;
 
 import com.billsplit.R;
 
 //import edu.sfsu.cs.orange.ocr.CaptureActivity;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.NumberPicker;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.text.Editable;
 import android.text.method.KeyListener;
 
 public class EventActivity extends Activity {
 
 	public static final String ARG_ID = "ID_ARG";
 	String EventID;
 	RelativeLayout layout;
 	static Event myEvent;
 	private Collection<Participant> participants;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_event);
 		
 		participants = Event.currentEvent.getParticipants();
 		
 		myEvent = Event.currentEvent; //replace this with serializable object
 		// Show the Up button in the action bar.
 		layout = (RelativeLayout) findViewById(R.id.participantsContainer);
 		
 		// childCount = layout.getChildCount();
 		NumberPicker numParticipants = (NumberPicker) findViewById(R.id.picker_participants);
 		numParticipants.setMaxValue(20);
 		numParticipants.setMinValue(1);
 		numParticipants.setValue(myEvent.getParticipants().size());
 		
 		numParticipants
 				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
 		numParticipants
 				.setOnScrollListener(new NumberPicker.OnScrollListener() {
 					@Override
 					public void onScrollStateChange(NumberPicker picker,
 							int scrollState) {
 
 						if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE)
 							;
 						// generateParticipants(picker.getValue());
 					}
 				});
  
 		numParticipants
 				.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
 					@Override
 					public void onValueChange(NumberPicker picker, int oldVal,
 							int newVal) {
 
 						if(newVal > oldVal){
 							for(int i=0;i<newVal-oldVal;i++){
 								myEvent.addParticipant(new Participant("Person"+String.valueOf(myEvent.getParticipants().size())));
 							}
 						}
 						
 						if(newVal < oldVal){
 							
 							}for(int i=0;i<oldVal-newVal;i++){
 								Participant toBeRemoved = ((ArrayList<Participant>)myEvent.getParticipants()).get(myEvent.getParticipants().size()-1);
 								myEvent.removeParticipant(toBeRemoved);
 							}
 						
 						//generateParticipants(picker.getValue());
 						generateParticipants();
 					}
 				});
 		EventID = (String) getIntent().getSerializableExtra(ARG_ID);
 
 		TextView lblName = (TextView) findViewById(R.id.lblName);
 		
 		lblName.setText(EventID);
 		
 		//EditText txtName = (EditText)findViewById(R.id.txtName);
 		
 		//GlobalAccount acc = (GlobalAccount) getApplication();
 		
 		
 		
 		setupActionBar();
 	}
 
 	private void generateParticipants() {
 		layout.removeAllViews();
 		int i = 0;
 		for (Participant participant : participants) {
 
 			ParticipantView btnPart = new ParticipantView(getApplicationContext());
 			
 			//Button btnPart = new Button(getApplicationContext());
 			
 			/*String styledText = "<big> <font color='#008000'>"
 		            + myEvent.getParticipants().get(i).getName() + "</font> </big>" + "<br />" 
 		            + "<small>" + "$ 0.00" + "</small>";
 			btnPart.setText(Html.fromHtml(styledText));
 		    */
 			//btnPart.setText(myEvent.getParticipants().get(i).getName());
 
 			btnPart.setName(participant.getName());
 			btnPart.setAmount(participant.getBalance());
 			btnPart.setBackgroundResource(R.drawable.btn_red);
 			
 			
 			//btnPart.isCheckable = true;
 			//LinearLayout txt = (LinearLayout)btnPart.findViewById(R.id.customButtonLayout);
 			//btnPart.setClickable(true);
 			//btnPart.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
 			btnPart.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					
 					
 					final ParticipantView part = (ParticipantView)v;
 					
 					//create a layout to hold the text boxes and buttons
 					LinearLayout layout = new LinearLayout(EventActivity.this);
 					layout.setOrientation(LinearLayout.VERTICAL);
 					
 					final EditText input = new EditText(EventActivity.this);
 					input.setText(part.getName());
 					layout.addView(input);
 
 					final EditText paymentNameInput = new EditText(EventActivity.this);
 					paymentNameInput.setHint("Set payment name...");
 					paymentNameInput.setId(3536);
 					layout.addView(paymentNameInput);
 
 					final Button paymentButton = new Button(EventActivity.this);
 					paymentButton.setText("Make Payment");
 					paymentButton.setBackgroundResource(R.drawable.btn_black);
 					paymentButton.setTextColor(Color.rgb(255,255,255));
 					//paymentButton.setEnabled(false);
 					paymentButton.setOnClickListener(new Button.OnClickListener() {
 					public void onClick(View v) {
 							myEvent.getParticipantByName(part.getName().toString()).setName(input.getText().toString());
 							part.setName(input.getText().toString());
 							String paymentName = paymentNameInput.getText().toString();
 							if(paymentName != null && !paymentName.equals("")){	
 								Intent intent = new Intent(EventActivity.this, PaymentActivity.class);
 								Collection<Participant> ppns = Event.currentEvent.getParticipants();
 								int size = ppns.size();
 								if(size > 0){
 									BalanceChange newBalanceChange = new BalanceChange(paymentName, ppns);
 									newBalanceChange.setDate(new Date());
 									BalanceChange.current = newBalanceChange;
 									Event.currentEvent.addBalanceChange(newBalanceChange);
 									startActivity(intent);
									EventActivity.this.finish();	
 								}else{
 									Toast.makeText(EventActivity.this, "No participants in the event, bro", Toast.LENGTH_SHORT).show();
 								}
 							}else{
 								Toast.makeText(EventActivity.this, "Please enter payment name..", Toast.LENGTH_SHORT).show();
 							}
 					  }
 					});					
 					layout.addView(paymentButton);
 					
 					AlertDialog.Builder alert;
 					alert = new AlertDialog.Builder(EventActivity.this);
 					alert.setView(layout);
 					alert.setTitle("Person Name");
 
 					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 							myEvent.getParticipantByName(part.getName().toString()).setName(input.getText().toString());
 							part.setName(input.getText().toString());
 					  }
 					});
 
 					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					  public void onClick(DialogInterface dialog, int whichButton) {
 					    // Canceled.
 					  }
 					});
 					
 					alert.show();
 					/*
 					Button btn = (Button)v;
 					showParticipantDialog((String) btn.getText());//change to participant ID
 					*/
 				}
 			});
 			// setting image resource
 			// imageView.setImageResource(R.drawable.ic_camera);
 			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.WRAP_CONTENT,
 					RelativeLayout.LayoutParams.WRAP_CONTENT);
 			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
 					RelativeLayout.TRUE);
 			params.topMargin = i * 120;
 			i++;
 			layout.addView(btnPart, params);
 		}
 	}
 
 	protected void showParticipantDialog(String name) {
 		DialogFragment newFragment = ParticipantDetailsDialog.newInstance(name);
 		
         newFragment.show(getFragmentManager(), "dialog");
     }
 
 
 
     public static class ParticipantDetailsDialog extends DialogFragment {
         
 		static ParticipantDetailsDialog newInstance(String name) {
 			ParticipantDetailsDialog dialog = new ParticipantDetailsDialog();
 			Bundle params = new Bundle();
 			params.putString("name", name);
 			dialog.setArguments(params);
             return dialog;
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View v = inflater.inflate(R.layout.dialog_participant_details, container, false);
             View tv = v.findViewById(R.id.txtName);
            ((TextView)tv).setText(getArguments().getString("name"));
             getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
             return v;
         }
     }
 
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(false);
 
 	}
 
 	protected void doEventNameSave(String name) {
 		
 		TextView lblName = (TextView) findViewById(R.id.lblName);
 		myEvent.setName(name);
 		lblName.setText(name);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.event_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			if(!Account.getCurrentAccount().isEvent(Event.currentEvent.getName()))
 			{
 				Account.getCurrentAccount().createEvent(Event.currentEvent.getName());//  isEvent()
 			}
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void lblName_clicked(View view){
 		final EditText input = new EditText(this);
 		
 		final TextView lblName = (TextView) findViewById(R.id.lblName);
 		input.setText(lblName.getText());
 
 		AlertDialog.Builder alert;
 		alert = new AlertDialog.Builder(this);
 		alert.setView(input);
 		alert.setTitle("Event Name");
      	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 		//  String value = input.getText();
 		  myEvent.setName(input.getText().toString());
 		  lblName.setText(input.getText().toString());
 		//  alert.
 		  }
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    // Canceled.
 		  }
 		});
 		
 		alert.show();
 	}
 
 	/** Called when the user clicks the OCR button */
 	public void createTransaction(View view) {
 		// Do something in response to button
 		Intent intent = new Intent(this, NewTransactionActivity.class);
 		startActivity(intent);
 	}
 
 	public void btn_new_transaction_clicked(View view) {
 		//Get a list of participants from the current event
 		ArrayList<Participant> participants = (ArrayList<Participant>) myEvent.getParticipants();
 		//Create a new transaction
 		Transaction newTransaction = new Transaction("Transaction"+String.valueOf(myEvent.getBalanceChanges().size()+1), participants);
 		//Set the newTransaction as the current transaction
 		newTransaction.setDate(new Date());
 		BalanceChange.current = newTransaction;
 		
 		myEvent.addBalanceChange(newTransaction);
 		//Start the new transaction
 		Intent intent = new Intent(this, NewTransactionActivity.class);
 		startActivity(intent);
 	}
 
 	public void btn_existing_transactions_clicked(View view) {
 		Intent intent = new Intent(this, ExistingTransactionsActivity.class);
 		startActivity(intent);
 	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		Event.currentEvent.updateBalances();
 		generateParticipants();
 		if(myEvent.getBalanceChanges().size()>0){
 			//System.out.println(myEvent.getBalanceChanges().size());
 			NumberPicker numParticipants = (NumberPicker) findViewById(R.id.picker_participants);
 			numParticipants.setEnabled(false);
 		}
 	}
 }
