 package com.example.criminalintent;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.text.format.DateFormat;
 import android.view.View;
 import android.widget.Button;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 
 public class DateTimeFragment extends DialogFragment {
 	
 	private static final String DATE_TIME_KEY = "date_time_key";
 	private static final String DATE_DIALOG_TAG = "date_dialog_tag";
 	private static final String TIME_DIALOG_TAG = "time_dialog_tag";
 	private static final int REQUEST_DATE = 100;
 	private static final int REQUEST_TIME = 101;
 	
 	
 	public static final String EXTRA_DATE = "DateTimeFragment.extra_date";
 	
 	private Date mDate ;
	private int mCurrentRequestCode = -1;
 	private TextView mTvDateTime;
 	private Button mChoose;
 	
	
 	public static DateTimeFragment newInstance(Date date)
 	{
 		DateTimeFragment dtf = new DateTimeFragment();
 		Bundle args = new Bundle();
 		args.putSerializable(DATE_TIME_KEY , date);
 		dtf.setArguments(args);
 		return dtf;
 	}
 	
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState)
 	{
 		super.onCreateDialog( savedInstanceState );
 		
 		View v = getActivity().getLayoutInflater().inflate( R.layout.dialog_date_time, null );
 		
 		RadioGroup rg = (RadioGroup) v.findViewById( R.id.radio_pickDateOrTime );
 		rg.setOnCheckedChangeListener( new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				switch (checkedId)
 				{
 				case R.id.radio_pickDate:
 					mCurrentRequestCode = REQUEST_DATE;
 					break;
 				case R.id.radio_pickTime:
 					mCurrentRequestCode = REQUEST_TIME;
 					break;
 				default:
 					break;
 				}
 				return;
 			}
 		});
 
 		mDate = (Date) getArguments().getSerializable( DATE_TIME_KEY );
 		mTvDateTime = (TextView) v.findViewById( R.id.textView_currentDateTime ); 
 		updateDateTimeLabel();
 		
 		mChoose = (Button) v.findViewById( R.id.radio_pickChoose );
 		mChoose.setOnClickListener( new View.OnClickListener( ) {
 			@Override
 			public void onClick(View v) {
 				startChildDialog();
 			}
 		});
 		
 		return new AlertDialog.Builder( getActivity())
 							.setView( v )
 							.setTitle(R.string.date_time_picker_title)
 							.setPositiveButton( 
 									android.R.string.ok,  
 									new DialogInterface.OnClickListener() {
 								
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									sendResult();
 								}
 							})
 							.create();
 	}
 	
 	private void sendResult()
 	{
 		if (getTargetFragment() == null)
 			return;
 		
 		Intent i = new Intent();
 		i.putExtra( EXTRA_DATE , mDate);
 		getTargetFragment().onActivityResult( getTargetRequestCode(), Activity.RESULT_OK, i);
 	}
 	
 	private void startChildDialog()
 	{
 		if (mCurrentRequestCode == REQUEST_DATE)
 		{
 			DatePickerFragment dpf = DatePickerFragment.newInstance( mDate );
 			dpf.setTargetFragment( this, REQUEST_DATE);
 			dpf.show( getActivity().getSupportFragmentManager() , DATE_DIALOG_TAG );
 		}
 		else if (mCurrentRequestCode == REQUEST_TIME)
 		{
 			TimePickerFragment dpt = TimePickerFragment.newInstance( mDate );
 			dpt.setTargetFragment( this, REQUEST_TIME);
 			dpt.show( getActivity().getSupportFragmentManager() , TIME_DIALOG_TAG );
 		}
 	}
 	
 	private void updateDateTimeLabel()
 	{
 		if (mTvDateTime != null && mDate != null)
			mTvDateTime.setText( DateFormat.format( "EEEE, MMM d, yyyy HH:mm", mDate ));
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		if (resultCode != Activity.RESULT_OK)
 			return;
 		
 		if (requestCode == REQUEST_DATE)
 		{
 			mDate = (Date ) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
 		}
 		else if (requestCode == REQUEST_TIME)
 		{
 			mDate = (Date ) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
 		}
 		
 		getArguments().putSerializable(DATE_TIME_KEY, mDate);
 		
 		updateDateTimeLabel();
 	}
 
 }
 
