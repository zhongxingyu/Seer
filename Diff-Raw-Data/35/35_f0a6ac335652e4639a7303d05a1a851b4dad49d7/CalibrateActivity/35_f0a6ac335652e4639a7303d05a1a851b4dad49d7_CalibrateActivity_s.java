 package uk.org.smithfamily.mslogger.activity;
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;
 import uk.org.smithfamily.mslogger.ecuDef.TableManager;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import android.app.Activity;
 import android.content.*;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 /**
  * Activity that calibrate the TPS of the Megasquirt
  */
 public class CalibrateActivity extends Activity
 {
 	private static final String	THROTTLEFACTOR_INC	= "throttlefactor.inc";
 
 	private static final String	HEADER	=          
 		"; MSLogger-generated Linear Throttle Calibration\n" +
         "; Written on %s\n" +
         ";\n" +
         ";   Low ADC = %d    High ADC = %d\n" +
         ";\n" +
         "THROTTLEFACTOR:\n" +
         "\t\t\t; ADC" ;
 
 	private int			minTPS	= 256;
 	private int			maxTPS	= 0;
 	private TextView	minValView;
 	private TextView	curValView;
 	private TextView	maxValView;
 	private ProgressBar	tpsDisplay;
 
 	/**
 	 * 
 	 * @param savedInstanceState
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.calibrate);
 
 		Button button = (Button) findViewById(R.id.ConfirmButton);
 		button.setOnClickListener(new OnClickListener()
 		{
 
 			@Override
 			public void onClick(View v)
 			{
 				saveValues();
 				finish();
 			}
 		});
 		minValView = (TextView) findViewById(R.id.MinValue);
 		curValView = (TextView) findViewById(R.id.CurrentValue);
 		maxValView = (TextView) findViewById(R.id.MaxValue);
 		tpsDisplay = (ProgressBar) findViewById(R.id.TPSPosition);
 
 		IntentFilter dataFilter = new IntentFilter(Megasquirt.NEW_DATA);
		registerReceiver(new BroadcastReceiver()
		{

			@Override
			public void onReceive(Context context, Intent intent)
			{
				int currentTPS = ApplicationSettings.INSTANCE.getEcuDefinition().getCurrentTPS();
				updateWithRaw(currentTPS);
			}
		}, dataFilter);

 	}
 
 	/**
 	 * 
 	 * @param raw
 	 */
 	private void updateWithRaw(int raw)
 	{
 		if (raw < minTPS)
 		{
 			minTPS = raw;
 		}
 		if (raw > maxTPS)
 		{
 			maxTPS = raw;
 		}
 		
 		minValView.setText(Integer.toString(minTPS));
 		curValView.setText(Integer.toString(raw));
 		maxValView.setText(Integer.toString(maxTPS));
 		tpsDisplay.setMax(Math.abs(maxTPS - minTPS));
 		tpsDisplay.setProgress(Math.abs(raw - minTPS));
 	}
 
 	/**
 	 * 
 	 */
 	private void saveValues()
 	{
 		File dataFile = new File(ApplicationSettings.INSTANCE.getDataDir(),THROTTLEFACTOR_INC);
 		
 		try
 		{
 			PrintWriter pw = new PrintWriter(new FileWriter(dataFile));
 			String now = SimpleDateFormat.getDateTimeInstance().format(new Date());
 			String header = String.format(HEADER,now,minTPS,maxTPS);
 			
 			pw.println(header);
 			
 			for (int x = 0; x < 256; x++)
 			{
 				pw.println(String.format("\tDB\t%3dT\t; %3d", getPercentage(x),x));
 			}
 			pw.close();
 			TableManager.INSTANCE.flushTable(THROTTLEFACTOR_INC);
 		}
 		catch (IOException e)
 		{
 			DebugLogManager.INSTANCE.logException(e);
 		}
 	}
 
 	/**
 	 * 
 	 * @param x
 	 * @return
 	 */
 	private int getPercentage(int x)
 	{
 		if (x <= minTPS)
 		{
 			return 0;
 		}
 		if (x >= maxTPS)
 		{
 			return 100;
 		}
 		
 		return (x - minTPS) * 100 / (maxTPS - minTPS);
 	}
 }
