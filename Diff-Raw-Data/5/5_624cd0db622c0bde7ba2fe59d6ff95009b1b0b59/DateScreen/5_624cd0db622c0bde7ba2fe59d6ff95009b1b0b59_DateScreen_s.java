 package com.daemgahe.historicalWeather;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class DateScreen extends Activity 
 {
 	private String theIcao;
 	//private Button submit;
 	private TextView debug;
 	//private String myURL;
 	
 	private DatePicker startDateField;
 	private DatePicker endDateField;
 	
 	private DateFormat df;
 	private Calendar calendar;
 	private Date startDate;
 	private Date endDate;
 	
 	private String startyear;
 	private String startmonth;
 	private String startday;
 	
 	private String endyear;
 	private String endmonth;
 	private String endday;
 	
 	private String completeURL;
 	private String graphData;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.date);
         
         startDateField = (DatePicker) findViewById(R.id.start_date);
         df = new SimpleDateFormat("yyyy-MM-dd");
         endDate = new Date();
         calendar = Calendar.getInstance();
         calendar.setTime(endDate);
         calendar.roll(Calendar.YEAR, -1);
         startDate = calendar.getTime();
         String startDateValue = df.format(startDate);
         //startDateField.setText(startDateValue);
         endDateField = (DatePicker) findViewById(R.id.end_date);
         String endDateValue = df.format(endDate);
         //endDateField.setText(endDateValue);
         
         calendar.setTime(startDate);
         startyear = String.format("%04d", calendar.get(Calendar.YEAR));
         startmonth = String.format("%02d", calendar.get(Calendar.MONTH));
         startday = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
         calendar.setTime(endDate);
         endyear = String.format("%04d", calendar.get(Calendar.YEAR));
         endmonth = String.format("%02d", calendar.get(Calendar.MONTH));
         endday = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
         
         startDateField.init(Integer.parseInt(startyear), Integer.parseInt(startmonth), Integer.parseInt(startday), null);
         endDateField.init(Integer.parseInt(endyear), Integer.parseInt(endmonth), Integer.parseInt(endday), null);        
         
         // Create a bundle to get the intent parameters
         Bundle bundle = this.getIntent().getExtras();
         theIcao = bundle.getString("icao");
         
         debug = (TextView) findViewById(R.id.debugTextView);
         Button submitButton = (Button) findViewById(R.id.submitButton);
         // Button dataButton = (Button) findViewById(R.id.dataButton);
        
         submitButton.setOnClickListener
         (new View.OnClickListener() 
         {	
         	@Override
 			public void onClick(View v) 
         	{
                 String startDateValue1 = String.format("%04d-%02d-%02d", startDateField.getYear(), startDateField.getMonth(), startDateField.getDayOfMonth());
                 String endDateValue1 = String.format("%04d-%02d-%02d", endDateField.getYear(), endDateField.getMonth(), endDateField.getDayOfMonth());
 
                 try {
 					startDate = df.parse(startDateValue1);
 	                endDate = df.parse(endDateValue1);
 				} catch (java.text.ParseException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 					return;
 				}
                 calendar.setTime(startDate);
                 startyear = String.format("%04d", calendar.get(Calendar.YEAR));
                startmonth = String.format("%02d", calendar.get(Calendar.MONTH));
                 startday = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
                 calendar.setTime(endDate);
                 endyear = String.format("%04d", calendar.get(Calendar.YEAR));
                endmonth = String.format("%02d", calendar.get(Calendar.MONTH));
                 endday = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
                 
         		  //debug.setText(completeURL);
         		try {
 					getData();
 					//debug.setText(graphData);
 					GoToGraphScreen();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
         	}
         });
         
         /*
         dataButton.setOnClickListener
         (new View.OnClickListener() 
         {	
         	public void onClick(View v) 
         	{
         		  //debug.setText(completeURL);
         		try {
 					getData();
 					//debug.setText(graphData);
 					//GoToDataScreen();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
         	}
         });
         */
         
     }
     
     /*
     protected void GoToDataScreen()
     {
     	Bundle bundle = new Bundle();
     	bundle.putString("graph", graphData);
     	Intent dateToGraph = new Intent(this, DataScreen.class);
     	dateToGraph.putExtras(bundle);
     	startActivityForResult(dateToGraph,0);
     }
     */
     
     protected void GoToGraphScreen()
     {
     	Bundle bundle = new Bundle();
     	bundle.putString("graph", graphData);
     	bundle.putLong("startDate", startDate.getTime());
     	bundle.putLong("endDate", endDate.getTime());
     	Intent dateToGraph = new Intent(this, GraphScreen.class);
     	dateToGraph.putExtras(bundle);
     	startActivityForResult(dateToGraph,0);
     }
     
     public void getData() throws Exception
     {
         // Prepare different parts of the URL
         String host = "http://www.wunderground.com/history/airport/";
         String file = "CustomHistory.html?";
         String urlEnd = "&req_city=NA&req_state=NA&req_statename=NA&format=1";
         
         // Form URL
         completeURL = String.format("%s%s/%s/%s/%s/%sdayend=%s&monthend=%s&yearend=%s%s",host,theIcao,startyear,startmonth,startday,file,endday,endmonth,endyear,urlEnd);
         debug.setText(completeURL);
         
     	BufferedReader in = null;
     	try
     	{
     		HttpClient client = new DefaultHttpClient ();
     		HttpGet request = new HttpGet();
     		request.setURI(new URI(completeURL));
     		HttpResponse response = client.execute(request);
     		in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 			StringBuffer sb = new StringBuffer("");
 			String line = "";
 			String NL = System.getProperty("line.separator");
 			while ((line = in.readLine()) != null)
 			{
 				sb.append(line + NL);
 				Log.v("Weather Graph", line+NL);
 			}
 			in.close();
 			graphData = new String(sb.toString());
     	} finally 
     	{
     		if (in != null) 
     		{
     			try
     			{
     				in.close();
     			} catch (IOException e)
     			{
     				e.printStackTrace();
     			}
     		}
     	} 
     }  
 }
 
