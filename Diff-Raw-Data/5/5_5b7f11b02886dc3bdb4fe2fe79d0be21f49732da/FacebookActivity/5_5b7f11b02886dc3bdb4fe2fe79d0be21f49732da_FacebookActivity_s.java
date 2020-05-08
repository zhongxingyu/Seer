 /**
  * Copyright (C) Kamosoft 2010
  */
 package com.kamosoft.happycontacts.facebook;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kamosoft.happycontacts.Constants;
 import com.kamosoft.happycontacts.Log;
 import com.kamosoft.happycontacts.R;
 import com.kamosoft.happycontacts.birthday.BirthdayActivity;
 import com.kamosoft.happycontacts.dao.DbAdapter;
 import com.kamosoft.happycontacts.model.SocialNetworkUser;
 
 /**
  * @author <a href="mailto:thomas.bruyelle@accor.com">tbruyelle</a>
  *
  * @since 26 mars 2010
  * @version $Id$
  */
 public class FacebookActivity
     extends ListActivity
     implements Constants, android.content.DialogInterface.OnClickListener
 {
     private static final int START_SYNC_MENU_ID = Menu.FIRST;
 
     private static final int LOGOUT_MENU_ID = START_SYNC_MENU_ID + 1;
 
     private static final int STORE_SYNC_MENU_ID = LOGOUT_MENU_ID + 1;
 
     private static final int DELETEALL_MENU_ID = STORE_SYNC_MENU_ID + 1;
 
     private static final int DELETEALL_DIALOG_ID = 1;
 
     private static final int CONFIRM_STORE_DIALOG_ID = 2;
 
     private static final int LOGIN_ACTIVITY_RESULT = 1;
 
     public static final int PICK_CONTACT_ACTIVITY_RESULT = 2;
 
     private DbAdapter mDb;
 
     private SocialUserArrayAdapter mArrayAdapter;
 
     private ArrayList<SocialNetworkUser> mUserList;
 
     private ProgressDialog mProgressDialog;
 
     private SocialNetworkUser mCurrentUser;
 
     private int mCurrentPosition;
 
     private TextView mSyncCounter;
 
     /**
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected void onCreate( Bundle savedInstanceState )
     {
         if ( Log.DEBUG )
         {
             Log.v( "FaceBookActivity: onCreate()  start" );
         }
         super.onCreate( savedInstanceState );
         setContentView( R.layout.facebook_sync );
 
         mSyncCounter = (TextView) findViewById( R.id.sync_counter );
 
         mDb = new DbAdapter( this );
 
         if ( Log.DEBUG )
         {
             Log.v( "FaceBookActivity: onCreate() end" );
         }
     }
 
     @Override
     protected void onResume()
     {
         if ( Log.DEBUG )
         {
             Log.v( "FaceBookActivity: start onResume" );
         }
         super.onResume();
 
         mDb.open( false );
 
         if ( isLoggedIn() )
         {
             if ( Log.DEBUG )
             {
                 Log.v( "FaceBookActivity: onCreate user logged" );
             }
             fillList();
         }
         else
         {
             if ( Log.DEBUG )
             {
                 Log.v( "FaceBookActivity: onCreate start login" );
             }
             startActivityForResult( new Intent( this, FacebookLoginActivity.class ), LOGIN_ACTIVITY_RESULT );
         }
         if ( Log.DEBUG )
         {
             Log.v( "FaceBookActivity: end onResume" );
         }
     }
 
     /**
      * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
      */
     @Override
     protected void onListItemClick( ListView l, View v, int position, long id )
     {
         super.onListItemClick( l, v, position, id );
         SocialNetworkUser user = mArrayAdapter.getItem( position );
         if ( user.birthday == null )
         {
             /* nothing todo if no birthday */
             Toast.makeText( this, R.string.friend_no_birthday, Toast.LENGTH_SHORT ).show();
             return;
         }
         mCurrentUser = user;
         mCurrentPosition = position;
         new SocialUserDialog( this, user, mDb ).show();
     }
 
     //
     //    private int getPositionFromUserId( String userId )
     //    {
     //        int position = 0;
     //        for ( SocialNetworkUser user : mUserList )
     //        {
     //            if ( user.uid.equals( userId ) )
     //            {
     //                return position;
     //            }
     //            position++;
     //        }
     //        return 0;
     //    }
 
     /**
      * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
      */
     @Override
     protected void onActivityResult( int requestCode, int resultCode, Intent data )
     {
         super.onActivityResult( requestCode, resultCode, data );
 
         if ( resultCode != Activity.RESULT_OK )
         {
             Toast.makeText( this, R.string.facebook_login_error, Toast.LENGTH_SHORT );
             this.finish();
             return;
         }
 
         switch ( requestCode )
         {
             case LOGIN_ACTIVITY_RESULT:
                 fillList();
                 break;
 
             case PICK_CONTACT_ACTIVITY_RESULT:
                 Toast.makeText(
                                 this,
                                 getString( R.string.link_contact_done, mCurrentUser.name,
                                            data.getStringExtra( CONTACTNAME_INTENT_KEY ) ), Toast.LENGTH_LONG ).show();
                 setIntent( data );
                 break;
         }
     }
 
     /**
      * 
      */
     public void fillList()
     {
         /* check intent contactId to update contact link and then scroll to the right item */
         if ( getIntent().hasExtra( CONTACTID_INTENT_KEY ) )
         {
             long id = getIntent().getLongExtra( CONTACTID_INTENT_KEY, 0 );
             String contactName = getIntent().getStringExtra( CONTACTNAME_INTENT_KEY );
             if ( contactName != null && mCurrentUser != null )
             {
                 mCurrentUser.setContactId( id );
                 mCurrentUser.setContactName( contactName );
                 mDb.updateSyncResult( mCurrentUser );
                 getIntent().removeExtra( CONTACTNAME_INTENT_KEY );
             }
         }
         mUserList = mDb.fetchAllSyncResults();
         mSyncCounter.setText( String.valueOf( mUserList.size() ) );
 
         mArrayAdapter = new SocialUserArrayAdapter( this, R.layout.socialnetworkuser, mUserList );
         setListAdapter( mArrayAdapter );
         if ( mCurrentPosition > 0 )
         {
             getListView().setSelection( mCurrentPosition );
         }
     }
 
     public ProgressDialog getProgressDialog()
     {
         return this.mProgressDialog;
     }
 
     public void finishSync( List<SocialNetworkUser> users )
     {
         if ( users == null || users.isEmpty() )
         {
             Toast.makeText( this, "No Facebook friends found !", Toast.LENGTH_SHORT );
         }
         else
         {
             /* record results in database */
             mDb.insertSyncResults( users );
             mProgressDialog.dismiss();
             fillList();
         }
     }
 
     /**
      * 
      */
     private void sync()
     {
         if ( Log.DEBUG )
         {
             Log.v( "FaceBookActivity: Start sync" );
         }
         mProgressDialog = ProgressDialog.show( this, "", this.getString( R.string.loading_friends ), true );
         new FacebookContactSync( this ).execute();
     }
 
     @Override
     protected void onStop()
     {
         if ( Log.DEBUG )
         {
             Log.v( "BirthdayActivity: start onStop" );
         }
         super.onStop();
         //        if ( mDb != null )
         //        {
         //            mDb.close();
         //        }
         if ( Log.DEBUG )
         {
             Log.v( "BirthdayActivity: end onStop" );
         }
     }
 
     /**
      * @return
      */
     private boolean isLoggedIn()
     {
         SharedPreferences settings = this.getSharedPreferences( APP_NAME, 0 );
         String session_key = settings.getString( "session_key", null );
         String secret = settings.getString( "secret", null );
         String uid = settings.getString( "uid", null );
 
         return session_key != null && secret != null && uid != null;
     }
 
     private class StoreAsyncTask
         extends AsyncTask<Void, Integer, Void>
     {
         private int counter;
 
         private boolean update;
 
         public StoreAsyncTask( boolean update )
         {
             this.update = update;
             this.counter = 0;
         }
 
         /**
          * @see android.os.AsyncTask#doInBackground(Params[])
          */
         @Override
         protected Void doInBackground( Void... voids )
         {
             for ( SocialNetworkUser user : mUserList )
             {
                 if ( user.birthday == null || user.getContactName() == null )
                 {
                     /* we don't store if no birthday, not linked to a contact or has already a birthday*/
                     continue;
                 }
                 boolean hasBirthday = mDb.hasBirthday( user.getContactId() );
                 if ( !update && hasBirthday )
                 {
                     /* we don't store if update option is off and if contact has already a birthday */
                     continue;
                 }
                 publishProgress( ++counter );
                 /* facebook birthday date has format MMMM dd, yyyy or MMMM dd */
                 String birthday = null, birthyear = null;
                 try
                 {
                     Date date = FB_birthdayFull.parse( user.birthday );
                     birthday = dayDateFormat.format( date );
                     birthyear = yearDateFormat.format( date );
                 }
                 catch ( ParseException e )
                 {
                     try
                     {
                         Date date = FB_birthdaySmall.parse( user.birthday );
                         birthday = dayDateFormat.format( date );
                         birthyear = null;
                     }
                     catch ( ParseException e1 )
                     {
                         Log.e( "unable to parse date " + user.birthday );
                         continue;
                     }
                 }
                 if ( hasBirthday && update )
                 {
                     if ( !mDb.updateBirthday( user.getContactId(), user.getContactName(), birthday, birthyear ) )
                     {
                         Log.e( "Error while updating birthday " + user.toString() );
                     }
                 }
                 else
                 {
                     if ( mDb.insertBirthday( user.getContactId(), user.getContactName(), birthday, birthyear ) == -1 )
                     {
                         Log.e( "Error while inserting birthday " + user.toString() );
                     }
                 }
             }
             return null;
         }
 
         /**
          * @see android.os.AsyncTask#onProgressUpdate(Progress[])
          */
         @Override
         protected void onProgressUpdate( Integer... values )
         {
             mProgressDialog.setMessage( FacebookActivity.this.getString( R.string.inserting_birthdays, values[0] ) );
         }
 
         /**
          * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
          */
         @Override
         protected void onPostExecute( Void voids )
         {
             mProgressDialog.dismiss();
             Toast.makeText( FacebookActivity.this, getString( R.string.inserting_birthdays_done, counter ),
                             Toast.LENGTH_SHORT ).show();
             Intent intent = new Intent( FacebookActivity.this, BirthdayActivity.class );
             intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
             startActivity( intent );
         }
     }
 
     /**
      * 
      */
     public void store( boolean update )
     {
         if ( mUserList != null && !mUserList.isEmpty() )
         {
             mProgressDialog = ProgressDialog.show( this, "", getString( R.string.inserting_birthdays, 0 ), true );
             new StoreAsyncTask( update ).execute();
         }
         else
         {
             Toast.makeText( this, R.string.no_syncresults, Toast.LENGTH_SHORT ).show();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu( Menu menu )
     {
         super.onCreateOptionsMenu( menu );
         menu.add( 0, START_SYNC_MENU_ID, 0, R.string.start_sync ).setIcon( R.drawable.fb );
         menu.add( 0, LOGOUT_MENU_ID, 0, R.string.logout ).setIcon( R.drawable.ic_menu_close_clear_cancel );
         menu.add( 0, STORE_SYNC_MENU_ID, 0, R.string.store_birthdays ).setIcon( R.drawable.ic_menu_save );
         menu.add( 0, DELETEALL_MENU_ID, 0, R.string.deleteall ).setIcon( R.drawable.ic_menu_delete );
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected( MenuItem item )
     {
         switch ( item.getItemId() )
         {
             case START_SYNC_MENU_ID:
                 sync();
                 return true;
             case STORE_SYNC_MENU_ID:
                 Cursor cursor = mDb.fetchAllBirthdays();
                 boolean existingBirthdays = cursor.getCount() > 0;
                 cursor.close();
                 if ( existingBirthdays )
                 {
                     showDialog( CONFIRM_STORE_DIALOG_ID );
                 }
                 else
                 {
                     store( false );
                 }
                 return true;
             case LOGOUT_MENU_ID:
                 logout();
                 return true;
             case DELETEALL_MENU_ID:
                 showDialog( DELETEALL_DIALOG_ID );
                 return true;
         }
         return super.onOptionsItemSelected( item );
     }
 
     /**
      * 
      */
     private void logout()
     {
         if ( isLoggedIn() )
         {
             SharedPreferences settings = this.getSharedPreferences( APP_NAME, 0 );
             Editor editor = settings.edit();
             editor.remove( "session_key" );
             editor.remove( "secret" );
             editor.remove( "uid" );
             editor.commit();
             Toast.makeText( this, R.string.logout_ok, Toast.LENGTH_SHORT ).show();
         }
         else
         {
             Toast.makeText( this, R.string.logout_ko, Toast.LENGTH_SHORT ).show();
         }
         this.finish();
     }
 
     /**
      * @see android.app.Activity#onCreateDialog(int)
      */
     @Override
     protected Dialog onCreateDialog( int id )
     {
         switch ( id )
         {
             case DELETEALL_DIALOG_ID:
                 AlertDialog.Builder builder = new AlertDialog.Builder( this );
                 builder.setMessage( R.string.confirm_deleteall ).setCancelable( false ).setPositiveButton( R.string.ok,
                                                                                                            this ).setNegativeButton(
                                                                                                                                      R.string.cancel,
                                                                                                                                      this );
                 return builder.create();
 
             case CONFIRM_STORE_DIALOG_ID:
                 return new ConfirmStoreDialog( this );
 
         }
         return null;
     }
 
     /**
      * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
      */
     @Override
     public void onClick( DialogInterface dialog, int which )
     {
         switch ( which )
         {
             case DialogInterface.BUTTON_NEGATIVE:
                 dialog.dismiss();
                 return;
             case DialogInterface.BUTTON_POSITIVE:
                 mDb.deleteAllSyncResults();
                 fillList();
                 return;
         }
     }
 }
