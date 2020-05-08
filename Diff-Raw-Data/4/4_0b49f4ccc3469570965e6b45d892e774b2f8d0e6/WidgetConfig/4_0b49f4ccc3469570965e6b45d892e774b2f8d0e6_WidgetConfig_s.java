 package ru.elifantiev.fga.widget;
 
 import ru.elifantiev.fga.R;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class WidgetConfig extends Activity
 {
     private SeekBar refreshBar;
     private TextView refreshLbl;
     private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
 
     @Override
     public void onCreate( Bundle savedInstanceState )
     {
         super.onCreate( savedInstanceState );
 
         // Sent negative result in order to cancel widget placement
         setResult( RESULT_CANCELED );
 
         setContentView( R.layout.config );
 
         /* find refresh Seek Bar & refresh LBL */
         refreshBar = (SeekBar) findViewById( R.id.config_refresh );
         refreshLbl = (TextView) findViewById( R.id.config_refresh_lbl );
         refreshBar.setOnSeekBarChangeListener( new seekListener() );
 
         // update refreshLbl with current refreshBar progress
         if ( refreshBar.getProgress() == 0 )
         {
             refreshLbl.setText( getString( R.string.config_refresh_none ) );
         }
         else
         {
             refreshLbl.setText( String.format( getString( R.string.config_refresh_lbl ), refreshBar
                     .getProgress() ) );
         }
 
         // set btnAction
         Button setBtn = (Button) findViewById( R.id.config_set );
         setBtn.setOnClickListener( new btnClickListener() );
 
         Button setBtnW = (Button) findViewById( R.id.config_set_white );
         setBtnW.setOnClickListener( new btnClickListener() );
 
         // Find the widget id from intent
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
         if ( extras != null )
         {
             appWidgetId = extras.getInt( AppWidgetManager.EXTRA_APPWIDGET_ID,
                     AppWidgetManager.INVALID_APPWIDGET_ID );
         }
 
         // Check if Activity called for widget, else finish
         if ( appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID )
         {
             finish();
         }
     }
 
     private class btnClickListener implements OnClickListener
     {
 
         @Override
         public void onClick( View v )
         {
             final Context context = WidgetConfig.this;
 
             // save config to db
             CheckBox widgetType = (CheckBox) findViewById( R.id.config_type );
 
            int widgetUpdateType = widgetType.isChecked() ? WidgetUpdater.WIDGET_UPDATE_MAIN
                    : WidgetUpdater.WIDGET_UPDATE_RND;
 
             int widgetStyle = (v.getId() == R.id.config_set_white) ? WidgetUpdater.WIDGET_STYLE_WHITE
                     : WidgetUpdater.WIDGET_STYLE_BLACK;
 
             ContentValues values = new ContentValues();
             values.put( DBHelper.WIDGET_ID, appWidgetId );
             values.put( DBHelper.WIDGET_TYPE, widgetUpdateType );
             values.put( DBHelper.WIDGET_REFRESH, refreshBar.getProgress() );
             values.put( DBHelper.WIDGET_STYLE, widgetStyle );
             values.put( DBHelper.WIDGET_URL, getString( R.string.adviceUrl ) );
 
             SQLiteDatabase db = new DBHelper( context ).getWritableDatabase();
             db.insert( DBHelper.WIDGET_TABLE, null, values );
             db.close();
             values.clear();
 
             PendingIntent pendingWidgetUpdate = WidgetConfig.getPendingItent( context, appWidgetId );
 
             // set Alarm Manager to call pending Intent
             AlarmManager alarmManager = (AlarmManager) context
                     .getSystemService( Context.ALARM_SERVICE );
             alarmManager.setRepeating( AlarmManager.ELAPSED_REALTIME,
                     SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_HOUR
                             * refreshBar.getProgress(), pendingWidgetUpdate );
 
             // Pass back original widgetId
             Intent resultValue = new Intent();
             resultValue.putExtra( AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId );
             setResult( RESULT_OK, resultValue );
 
             finish();
         }
     }
 
     private class seekListener implements OnSeekBarChangeListener
     {
         @Override
         public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
         {
             if ( progress == 0 )
             {
                 refreshLbl.setText( getString( R.string.config_refresh_none ) );
             }
             else
             {
                 refreshLbl.setText( String.format( getString( R.string.config_refresh_lbl ),
                         progress ) );
             }
         }
 
         @Override
         public void onStartTrackingTouch( SeekBar seekBar )
         {}
 
         @Override
         public void onStopTrackingTouch( SeekBar seekBar )
         {}
     }
 
     public static PendingIntent getPendingItent( final Context context, final int widgetId )
     {
         Intent updaterIntent = new Intent();
         updaterIntent.setAction( AppWidgetManager.ACTION_APPWIDGET_UPDATE );
         updaterIntent.putExtra( AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { widgetId } );
         updaterIntent.setData( Uri.withAppendedPath( Uri.parse( "fga://widget/id/" ), String
                 .valueOf( widgetId ) ) );
 
         // create PendingIntent that will broadcast widget update
         PendingIntent pendingWidgetUpdate = PendingIntent.getBroadcast( context, 0, updaterIntent,
                 PendingIntent.FLAG_UPDATE_CURRENT );
 
         return pendingWidgetUpdate;
     }
 }
