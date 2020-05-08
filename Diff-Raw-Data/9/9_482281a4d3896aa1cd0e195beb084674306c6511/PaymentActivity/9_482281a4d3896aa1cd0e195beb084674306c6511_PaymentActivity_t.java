 package com.xteam.raincheque;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.InputType;
 import android.text.format.DateFormat;
 import android.text.format.Time;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class PaymentActivity extends Activity 
 {
 	Button bAddParticipants;
 	Button bAddPayers;
 	Button bDone;	
 	TextView tvList;
 	EditText etLabel;
 	ArrayList<AccountRecord> tempAccountList;
 	ArrayList<Integer> ids;
 	ArrayList<PerIdPayment> payments;
 	ArrayList<PerIdPayment> debts;
 	int subtotal = 0, participantCount = 0;
 	
 	private class PerIdPayment
 	{
 		int id;
 		int payment;
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.payment_activity);
 		tempAccountList = new ArrayList<AccountRecord>();
 		payments = new ArrayList<PerIdPayment>();
 		debts = new ArrayList<PerIdPayment>();
 		ids = new ArrayList<Integer>();
 		int i = 0;
 		for(AccountRecord a:RainChequeApplication.currentSession.accountList)
 		{
 			ids.add(new Integer(i));
 			AccountRecord temp = new AccountRecord();
 			temp.name = a.name;
 			temp.paid = a.paid;
 			temp.settlement = a.settlement;
 			temp.worth = a.worth;
 			tempAccountList.add(temp);
 			i++;
 		}
 		bAddParticipants = (Button)findViewById(R.id.buttonAddInvolvedParticipants);
 		bAddParticipants.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
 	        	builder.setTitle(getString(R.string.select_payee));
 	        	String []names = new String[RainChequeApplication.currentSession.accountList.size()];
 	        	boolean []checks = new boolean[RainChequeApplication.currentSession.accountList.size()];
 	        	int i = 0;
 	        	for(AccountRecord a:RainChequeApplication.currentSession.accountList)
 	        	{
 	        		names[i] = a.name;
 	        		checks[i] = false;
 	        		i++;
 	        	}
 	        	builder.setMultiChoiceItems(names, checks, new DialogInterface.OnMultiChoiceClickListener()
 	        	{
 
 					@Override
 					public void onClick(DialogInterface dialog, int which, boolean isChecked) 
 					{
 						
 					}	        		
 	        	});
 	        	builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() 
 	        	{ 
 	        	    public void onClick(DialogInterface dialog, int which) 
 	        	    {
 	        	    	ListView list = ((AlertDialog)dialog).getListView();	        	    	
 	        	    	ArrayList<String> participants = new ArrayList<String>();
 	        	    	for (int i=0; i<list.getCount(); i++) 
 	        	    	{
 	        	    		boolean checked = list.isItemChecked(i);
 	        	    		if(checked)
 	        	    		{
 	        	    			PerIdPayment pid = new PerIdPayment();
 	        	    			pid.id = i;
 	        	    			debts.add(pid);
 	        	    			participantCount++;
 	        	    			participants.add(RainChequeApplication.currentSession.accountList.get(i).name);
 	        	    		}	        	    		
 	        	    	}
 	        	    	String selected = new String();
 	        	    	for(int i = 0;i<participants.size();i++)
 	        	    	{
 	        	    		selected = selected.concat(participants.get(i));
 	        	    		if(i==participants.size()-2)
 	        	    		{
 	        	    			selected = selected.concat(" and ");
 	        	    		}
 	        	    		else if(i==participants.size()-1)
 	        	    		{
 	        	    			
 	        	    		}
 	        	    		else
 	        	    		{
 	        	    			selected = selected.concat(", ");
 	        	    		}	        	    			
 	        	    	}
 	        	    	tvList.setText(selected);
 	        	    }
 	        	});
 	        	builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() 
 	        	{
 	        	    public void onClick(DialogInterface dialog, int which) 
 	        	    {
 	        	        dialog.cancel();
 	        	    }
 	        	});
 	        	builder.show();
 			}			
 		});
 		tvList = (TextView)findViewById(R.id.listOfInvolvedParticipants);
 		etLabel = (EditText)findViewById(R.id.editTextLabel);		
 		bDone = (Button)findViewById(R.id.bDonePayments);
 		bDone.setOnClickListener(new OnClickListener()
 		{
 
 			@Override
 			public void onClick(View v) 
 			{
 				if(tvList.getText().toString().contentEquals(getString(R.string.selected_participants)) || ids.size() == RainChequeApplication.currentSession.accountList.size())
 				{
 					AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
 		        	builder.setTitle(getString(R.string.oops));
 		        	TextView tv = new TextView(PaymentActivity.this);
 		        	tv.setText(R.string.none_selected);
 		        	tv.setTextAppearance(PaymentActivity.this, android.R.style.TextAppearance_Large);
     	        	tv.setPadding(30, 30, 30, 30);
 		        	builder.setView(tv);
 		        	builder.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() 
 		        	{
 		        	    public void onClick(DialogInterface dialog, int which) 
 		        	    {
 		        	        dialog.cancel();
 		        	    }
 		        	});
 		        	builder.show();
 		        	return;
 				}
 				else
 				{
 					for(PerIdPayment pid: debts)
 					{
 						RainChequeApplication.currentSession.accountList.get(pid.id).worth += subtotal / participantCount;
 					}
 					for(PerIdPayment pip: payments)					
 					{
 						RainChequeApplication.currentSession.accountList.get(pip.id).paid += pip.payment;
 						String payer = RainChequeApplication.currentSession.accountList.get(pip.id).name;
						String logText = String.format(getString(R.string.payment_statement), payer, pip.payment, etLabel.getText().toString(), tvList.getText().toString());
 	        	    	LogEntry logEntry = new LogEntry();
 	        	    	logEntry.entry = logText;
 	        	    	Time now = new Time();
 	        	    	now.setToNow();
 	        	    	String date = DateFormat.format("d MMMM yyyy", Calendar.getInstance()).toString();
 	        	    	logEntry.time = now.format(date + " at %I:%M:%S");				        	    	
 	        	    	RainChequeApplication.currentSession.sessionLog.add(logEntry); 
 	        	    	RainChequeApplication.writeAccountsToFile(getApplicationContext());
 					}
 					for(SessionRecord s:RainChequeApplication.sessionList)
         			{
         				if(s.sessionID==RainChequeApplication.currentSession.sessionID)
         				{
         					s.sessionLog = RainChequeApplication.currentSession.sessionLog;
         					s.accountList = RainChequeApplication.currentSession.accountList;
         					break;
         				}
         			}
 					onBackPressed();
 					
 				}
 			}
 			
 		});
 		bAddPayers = (Button)findViewById(R.id.buttonAddPayingParticipants);
 		bAddPayers.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				if(tempAccountList.size()==0)
 				{
 					AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
 		        	builder.setTitle(getString(R.string.oops));
 		        	TextView tv = new TextView(PaymentActivity.this);
 		        	tv.setText(R.string.all_selected);
 		        	tv.setTextAppearance(PaymentActivity.this, android.R.style.TextAppearance_Large);
     	        	tv.setPadding(30, 30, 30, 30);
 		        	builder.setView(tv);
 		        	builder.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() 
 		        	{
 		        	    public void onClick(DialogInterface dialog, int which) 
 		        	    {
 		        	        dialog.cancel();
 		        	    }
 		        	});
 		        	builder.show();
 		        	return;
 				}	
 				AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
 	        	builder.setTitle(getString(R.string.select_payer));
 	        	builder.setAdapter(new ParticipantListAdapter(getApplicationContext(), tempAccountList.toArray()), new DialogInterface.OnClickListener()
 	        	{
 					@Override
 					public void onClick(DialogInterface dialog, int which) 
 					{
 						final AccountRecord a = tempAccountList.get(which);
 						final PerIdPayment payment = new PerIdPayment();
 						payment.id = ids.get(which);
 						tempAccountList.remove(which);
 						ids.remove(which);
 						AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
 			        	builder.setTitle(getString(R.string.enter_amount));
 			        	final EditText input = new EditText(PaymentActivity.this);	        	
 			        	input.setInputType(InputType.TYPE_CLASS_NUMBER);
 			        	input.setPadding(30, 30, 30, 30);
 			        	builder.setView(input);
 			        	builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() 
 			        	{ 
 			        	    public void onClick(DialogInterface dialog, int which) 
 			        	    {
 			        	    	LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			        	    	View v = vi.inflate(R.layout.session_row, null);
 			        	    	LinearLayout lila = (LinearLayout)findViewById(R.id.listOfPayers); 
 			        	    	lila.addView(v);			        	    	
 			        	    	TextView payer = (TextView)findViewById(R.id.sessionName);
 			        	    	payer.setText(a.name);
 			        	    	payer.setId(1);
 			        	    	TextView contribution = (TextView)findViewById(R.id.sessionMembers);
 			        	    	payment.payment = Integer.parseInt(input.getText().toString());
 			        	    	subtotal += Integer.parseInt(input.getText().toString());
 			        	    	contribution.setText(getString(R.string.paid) + " " + input.getText().toString());
 			        	    	contribution.setId(1);
 			        	    	payments.add(payment);
 			        	    }
 			        	});
 			        	builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() 
 			        	{
 			        	    public void onClick(DialogInterface dialog, int which) 
 			        	    {
 			        	        dialog.cancel();
 			        	    }
 			        	});
 
 			        	builder.show();
 						
 					}
 	        		
 	        	});
 	        	builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() 
 	        	{
 	        	    public void onClick(DialogInterface dialog, int which) 
 	        	    {
 	        	        dialog.cancel();
 	        	    }
 	        	});
 	        	builder.show();
 			}			
 		});
 	}
 
 }
