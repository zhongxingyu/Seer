 /**
  * Copyright (C) Kamosoft 2010
  */
 package com.kamosoft.happycontacts.birthday;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kamosoft.happycontacts.Constants;
 import com.kamosoft.happycontacts.Log;
 import com.kamosoft.happycontacts.R;
 import com.kamosoft.happycontacts.dao.DbAdapter;
 
 /**
  * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
  *
  * @since 28 avr. 2010
  * @version $Id$
  */
 public class PickBirthdayActivity
     extends Activity
     implements Constants, OnClickListener
 {
     private DbAdapter mDb;
 
     private String mContactName;
 
     private Long mContactId;
 
     private boolean mUpdate;
 
     private DatePicker mDatePicker;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate( Bundle savedInstanceState )
     {
         if ( Log.DEBUG )
         {
             Log.v( "PickBirthdayActivity: start onCreate" );
         }
         super.onCreate( savedInstanceState );
         setContentView( R.layout.pickbirthday );
 
         mDb = new DbAdapter( this );
         mDb.open( false );
 
         mContactId = getIntent().getExtras().getLong( CONTACTID_INTENT_KEY );
         mContactName = getIntent().getExtras().getString( CONTACTNAME_INTENT_KEY );
 
         Date date = mDb.getBirthday( mContactId );
         mUpdate = date != null;
 
         mDatePicker = (DatePicker) findViewById( R.id.birthday_picker );
         TextView textView = (TextView) findViewById( R.id.pick_birthday_label );
 
         if ( mUpdate )
         {
             /* contact has already a birthday, this is an update */
             textView.setText( getString( R.string.pick_birthday_update_label, mContactName ) );
             Calendar calendar = Calendar.getInstance();
             calendar.setTime( date );
             mDatePicker.updateDate( calendar.get( Calendar.YEAR ), calendar.get( Calendar.MONTH ),
                                     calendar.get( Calendar.DAY_OF_MONTH ) );
         }
         else
         {
             /* contact has no birthday, this is an insert */
             textView.setText( getString( R.string.pick_birthday_label, mContactName ) );
         }
 
         Button button = (Button) findViewById( R.id.ok );
         button.setOnClickListener( this );
 
         if ( Log.DEBUG )
         {
             Log.v( "PickBirthdayActivity: start onCreate" );
         }
     }
 
     /**
      * @see android.view.View.OnClickListener#onClick(android.view.View)
      */
     @Override
     public void onClick( View view )
     {
         String month = String.valueOf( mDatePicker.getMonth() + 1 );
         if ( month.length() == 1 )
         {
             month = "0" + month;
         }
        String day = String.valueOf( mDatePicker.getDayOfMonth() );
        if ( day.length() == 1 )
        {
            day = "0" + day;
        }
        String birthday = day + "/" + month;
         String birthyear = String.valueOf( mDatePicker.getYear() );
         if ( mUpdate )
         {
             if ( !mDb.updateBirthday( mContactId, mContactName, birthday, birthyear ) )
             {
                 String error = "Error while updating birthday for " + mContactName + ", " + birthday + ", " + birthyear;
                 Log.e( error );
                 Toast.makeText( this, error, Toast.LENGTH_SHORT ).show();
             }
             else
             {
                 Toast.makeText( this, R.string.birthday_updated, Toast.LENGTH_SHORT ).show();
             }
         }
         else
         {
             if ( !mDb.insertBirthday( mContactId, mContactName, birthday, birthyear ) )
             {
                 String error =
                     "Error while inserting birthday for " + mContactName + ", " + birthday + ", " + birthyear;
                 Log.e( error );
                 Toast.makeText( this, error, Toast.LENGTH_SHORT ).show();
             }
             else
             {
                 Toast.makeText( this, R.string.birthday_added, Toast.LENGTH_SHORT ).show();
             }
         }
         Intent intent = new Intent( this, BirthdayActivity.class );
         intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
         startActivity( intent );
     }
 
 }
