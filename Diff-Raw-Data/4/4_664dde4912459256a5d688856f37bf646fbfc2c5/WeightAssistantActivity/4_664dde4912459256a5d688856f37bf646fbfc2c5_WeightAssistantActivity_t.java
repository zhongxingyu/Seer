 package at.linuxhacker.weightassistant;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.speech.tts.TextToSpeech;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 
 public class WeightAssistantActivity extends Activity implements TextToSpeech.OnInitListener {
 	private static final int ACTIVITY_ADD_ENTRY = 1;
 	private static String C_CSV_DIRNAME = "weighassistant";
 	private static String C_CSV_FILENAME = "weightassistant.csv";
 	private static int C_MAX_BACKUP_FILE_VERSIONS = 10;
 	private WeightOverviewGraph weightOverviewGraph = new WeightOverviewGraph( );
 	private WeekOverviewGraph weekOverviewGraph = new WeekOverviewGraph( );
 	private WeightMeasurmentSeries weightMeasurmentSeries;
 	private TextView size = null;
 	private TextView thisWeek = null;
 	private TextView lastWeek = null;
 	private TextView lastWeekHeader;
 	private TextView thisWeekHeader;
 	private TextToSpeech textToSpeech;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         this.textToSpeech = new TextToSpeech( this, this );
         this.weightMeasurmentSeries = new WeightMeasurmentSeries( WeightAssistantActivity.this );
         this.weightMeasurmentSeries.readAll( );
         
         this.updateDisplayData( );
         
         Button buttonNewEntry = ( Button ) findViewById( R.id.buttonAddEntry );
         buttonNewEntry.setOnClickListener( new View.OnClickListener( ) {
 			@Override
 			public void onClick( View view ) {
 				/* alt
 	        	startActivity( new Intent( WeightAssistantActivity.this,
 	        			AddEntry.class ) );
 	        	*/
 	        	startActivityForResult( new Intent( WeightAssistantActivity.this,
 	        			AddEntry.class ), WeightAssistantActivity.ACTIVITY_ADD_ENTRY );
 			}
 		} );
         
         Button buttonDisplayData = ( Button ) findViewById( R.id.buttonDisplayData );
         buttonDisplayData.setOnClickListener( new View.OnClickListener( ) {
 			@Override
 			public void onClick(View v) {
 				startActivity( new Intent( WeightAssistantActivity.this,
 						DisplayData.class ) );	
 			}
 		} );
         
         ImageButton buttonSimpleGraph = ( ImageButton ) findViewById( R.id.buttonSimpleGraph );
         buttonSimpleGraph.setOnClickListener( new View.OnClickListener( ) {
         	@Override
         	public void onClick(View v) {
         		Intent intent = null;
         		
         		weightOverviewGraph.setWeightMeasurmentSeries( weightMeasurmentSeries );
         		intent = weightOverviewGraph.execute( WeightAssistantActivity.this );
         		startActivity( intent );
 
         	}
         } );
 
         ImageButton buttonWeekOverviewGraph = ( ImageButton ) findViewById( R.id.buttonWeekGraph );
         buttonWeekOverviewGraph.setOnClickListener( new View.OnClickListener( ) {
         	@Override
         	public void onClick(View v) {
         		Intent intent = null;
         		
         		weekOverviewGraph.setWeightMeasurmentSeries( weightMeasurmentSeries );
         		intent = weekOverviewGraph.execute( WeightAssistantActivity.this );
         		startActivity( intent );
 
         	}
         } );
         
     }
     
     @Override
 	public void onInit(int status) {
 		// TextToSpeech OnInitListener
     	if( status == TextToSpeech.SUCCESS ) {
     		int result = this.textToSpeech.setLanguage( Locale.GERMAN );
     		if ( result == TextToSpeech.LANG_MISSING_DATA ||
     				result == TextToSpeech.LANG_NOT_SUPPORTED ) {
     			Toast.makeText( this, "TextToSpeech nicht verfügbar", Toast.LENGTH_LONG );
     		}
     	}	
 	}
 
 	private void fillPersonalData() {
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
         this.size = ( TextView ) findViewById( R.id.myData_size );
         this.size.setText( prefs.getString( "size", "k.A" ) );
         this.size = ( TextView ) findViewById( R.id.myData_targetWeight );
         this.size.setText( prefs.getString( "prefTargetWeight", "k.A" ) );		
         this.lastWeek = ( TextView ) findViewById( R.id.myData_lastWeek );
         this.lastWeekHeader = ( TextView ) findViewById( R.id.myData_lastWeekHeader );
         this.thisWeek = ( TextView ) findViewById( R.id.myData_thisWeek );
         this.thisWeekHeader = ( TextView ) findViewById( R.id.myData_thisWeekHeader );
         List<WeeklyStatistic> weeklyStatistic = this.weightMeasurmentSeries.getWeeklyStatisticList( );
         
         int weeks = weeklyStatistic.size( ) - 1;
         if ( weeks >= 3 ) {
         	DecimalFormat format = new DecimalFormat( "0.00" );
         	this.lastWeekHeader.setText( weeklyStatistic.get( weeks - 1 ).getWeekOfTheYear( ) );
         	this.lastWeek.setText( format.format( weeklyStatistic.get( weeks - 1 ).average )
         			+ " / " +
         			format.format( weeklyStatistic.get( weeks - 1 ).average -
         					weeklyStatistic.get( weeks - 2 ).average )
         			);
         	this.thisWeekHeader.setText( weeklyStatistic.get( weeks ).getWeekOfTheYear( ) );
         	this.thisWeek.setText( format.format( weeklyStatistic.get( weeks ).average )
         			+ " / " +
         			format.format( weeklyStatistic.get( weeks ).average -
         					weeklyStatistic.get( weeks -1  ).average )
         			);
         }
     }
 
 	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
     	if ( requestCode == WeightAssistantActivity.ACTIVITY_ADD_ENTRY ) {
     		if ( resultCode == RESULT_OK ) {
     			this.csvExport( );
     			this.weightMeasurmentSeries.readAll( );
     			this.updateDisplayData( );
     			this.speekAddEntryComment( );
     		}
     	}
     }
     
     private void speekAddEntryComment() {
     	int length = this.weightMeasurmentSeries.measurmentSeries.size( ); 
     	if ( length > 2 ) {
     		double diff = this.weightMeasurmentSeries.measurmentSeries.get( length -1 ).getWeight( ) -
     				this.weightMeasurmentSeries.measurmentSeries.get( length - 2 ).getWeight( );
     		DecimalFormat format = new DecimalFormat( "0.0" );
     		String say = "Die Differenz zu gestern beträgt " + format.format( diff ) +
     				" Kilogramm.";
     		this.textToSpeech.speak( say, TextToSpeech.QUEUE_ADD, null);
     	}
 	}
     
     private void updateWeekStatistic( ) {
     	String[] fields = { "weekStatistic_mo", "weekStatistic_di",
     			"weekStatistic_mi", "weekStatistic_do", "weekStatistic_fr", 
     			"weekStatistic_sa", "weekStatistic_so" };
     	int[] day = { Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
     			Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, 
     			Calendar.SUNDAY };
     	
     	TextView view;
     	int id;
     	List<WeeklyStatistic> weeklyList = this.weightMeasurmentSeries.getWeeklyStatisticList( );
     	int weeklyListLength = weeklyList.size( );
    	if ( weeklyListLength < 2 ) {
    	    // FIXME: this is a quick hack...
    	    return; 
    	}
     	DecimalFormat format = new DecimalFormat( "0.0" );
     	double diffPerWeek = -0.5;
     	double thisWeekDelta = 0;
     	double lastWeekAverage = weeklyList.get( weeklyListLength - 2 ).average;
     	double lastWeight = lastWeekAverage;
     	int lastWeightIndex = -1;
 
     	if ( weeklyListLength > 1 ) {
     		for ( int i = 0; i < fields.length; i++ ) {
     			id = getResources( ).getIdentifier( fields[i], "id", this.getPackageName( ) );
     			view = ( TextView ) findViewById( id );
     			MeasuringPoint point = weeklyList.get( weeklyListLength - 1 )
     					.findMeasuringPointForDayOfWeek( day[i] );
     			if ( point != null ) {
     				double actualDelta = point.getWeight( ) - lastWeight;
     				lastWeight = point.getWeight( );
     				thisWeekDelta += actualDelta;
     				view.setText( format.format( actualDelta ) );
     				if ( thisWeekDelta > ( i + 1 ) * ( diffPerWeek /  7 ) ) {
     					view.setTextColor( Color.RED );
     				} else {
     					view.setTextColor( Color.GREEN );
     				}
     				lastWeightIndex = i;
     			} else {
     				view.setText( "-" );
     				view.setTextColor( Color.WHITE );
     			}
     		}
     		
     		int daysToEndOfWeek = fields.length -1 - lastWeightIndex;
     		double diffPerDay = ( thisWeekDelta - diffPerWeek ) / daysToEndOfWeek;
     		for ( int i = lastWeightIndex + 1 ; i < fields.length; i++ ) {
     			id = getResources( ).getIdentifier( fields[i], "id", this.getPackageName( ) );
     			view = ( TextView ) findViewById( id );
     			view.setText( format.format( diffPerDay * -1 ) );
     			view.setTextColor( Color.GRAY );
     			//view.setText( "x" );
     		}
     	}
     }
 
 	public void csvImport( ) {
     	DbHelper dbHelper;
     	SQLiteDatabase db;
     	int i = 0;
     	String directoryname = Environment.getExternalStorageDirectory( ) + File.separator + WeightAssistantActivity.C_CSV_DIRNAME;
     	File directory = new File( directoryname );
     	String[] filenames = directory.list( );
     	Arrays.sort( filenames );
     	String filename = directoryname + File.separator + filenames[filenames.length - 1];	
 
     	dbHelper = new DbHelper( this );
     	db = dbHelper.getWritableDatabase( );
     	db.delete(DbHelper.TABLE, "", null);    	
     	try {
 	    	CSVReader reader = new CSVReader(
 	    			new FileReader( filename ) );
 	    	String [] nextLine;
 	    	while( ( nextLine = reader.readNext( ) ) != null ) {
 	    		ContentValues values = new ContentValues( );
 	    		values.put( DbHelper.C_DATETIME, nextLine[0] );
 	    		values.put( DbHelper.C_GEWICHT, nextLine[1] );
 	    		db.insertOrThrow( DbHelper.TABLE, null, values );
 	    		i++;
 	    	}
     	} catch ( Exception e ){
     		Toast toast = Toast.makeText( this, "Fehler: " + e.getMessage( ), Toast.LENGTH_LONG );
     		toast.show( );
         }
     	db.close( );
 
     	Toast.makeText( this, "Import von " + i + " Record von File: "
     			+ filename, Toast.LENGTH_LONG ).show( );
     	
 		this.weightMeasurmentSeries.readAll( );
 		this.updateDisplayData( );    	
     }
     public void csvExport( ) {
     	int i = 0;
     	Date now = new Date( );
     	SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd_HHmm" );
     	String filenamePrefix = new String( format.format( now ) );
     	
     	String directoryname = Environment.getExternalStorageDirectory( ) + File.separator + WeightAssistantActivity.C_CSV_DIRNAME;
     	File directory = new File( directoryname );
     	directory.mkdirs( );
     	String filename = directoryname + File.separator + filenamePrefix + "-" + WeightAssistantActivity.C_CSV_FILENAME;
     	CSVWriter writer;
     	DbHelper dbHelper = new DbHelper( this );
     	SQLiteDatabase db = dbHelper.getReadableDatabase( );
     	Cursor cursor = db.query( DbHelper.TABLE,
     			null, null, null, null, null, null );
     	cursor.moveToFirst( );
     	
     	try {
     		writer = new CSVWriter(
     				new FileWriter( filename ) );
     		while( cursor.isAfterLast( ) == false ) {
     			String[] values = { cursor.getString( 1 ), cursor.getString( 2 ) };
 	    		writer.writeNext( values );
 	    		cursor.moveToNext( );
 	    		i++;
     		}
     		writer.close( ); 
     	} catch ( Exception e ) {
     		Toast toast = Toast.makeText( this, "Fehler: " + e.getMessage( ), Toast.LENGTH_LONG );
     		toast.show( );
     	}
     	Toast toast = Toast.makeText( this, "Export von " + i + " Records ins File: "
     			+ filename, Toast.LENGTH_LONG );
     	toast.show( );
     	
     	// Cleanup Directory
     	String[] filenames = directory.list( );
     	Arrays.sort( filenames );
     	if ( filenames.length > 10 ) {
     		for ( i = 0; i <filenames.length - WeightAssistantActivity.C_MAX_BACKUP_FILE_VERSIONS;
     				i++ ) {
     			new File( directory, filenames[i] ).delete( );
     		}
     	}
 
     }
    
     private void updateDisplayData( ) {
     	// FIXME: das muss besser gehen!
     	if ( this.weightMeasurmentSeries.measurmentSeries.size( ) == 0 ) {
     		return;
     	}
     	this.fillPersonalData( );
     	this.updateWeekStatistic( );
     }
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater( );
 		inflater.inflate( R.menu.menu, menu);
 		return true;
 		// TODO Auto-generated method stub
 		//return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch ( item.getItemId( ) ) {
 		case R.id.itemPrefs:
 			startActivity( new Intent( this, PrefsActivity.class ) );
 			break;
 		case R.id.itemBackup:
 			this.csvExport( );
 			break;
 		case R.id.itemRestore:
 			this.csvImport( );
 			break;
 	}
 		return true;
 	}
 }
