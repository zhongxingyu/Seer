 package au.edu.qut.inn372.greenhat.activity;
 
 import java.text.DecimalFormat;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.widget.TextView;
 
 public class PowerGeneration extends Activity{
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.power_generation);
         generateView();
 
         
 	}   
 	
 	public void generateView() {
     	TextView dailyField = (TextView)findViewById(R.id.TextViewDailyField);
     	TextView annualField = (TextView)findViewById(R.id.TextViewAnnualField);
     	TextView quaterlyField = (TextView)findViewById(R.id.TextViewQuaterlyField);
     	TextView netField = (TextView)findViewById(R.id.TextViewNetField);
         TextView quaterlyNet = (TextView)findViewById(R.id.TextViewQuaterlyNetField);
         TextView annualNet = (TextView)findViewById(R.id.TextViewAnnualNetField);
         
     	
     	Intent intent = getIntent();
         String solarPower = intent.getStringExtra(BasicInputActivity.EXTRA_MESSAGE);
         String daily = intent.getStringExtra(BasicInputActivity.EXTRA_MESSAGE2);
         
         
         DecimalFormat df = new DecimalFormat("#.##");
 
         
         dailyField.setText(""+df.format(new Double(solarPower)));
         annualField.setText(""+df.format(new Double(solarPower)*365));
        quaterlyField.setText(""+df.format(new Double(solarPower)*365/4));
         netField.setText(""+(df.format(new Double(solarPower).doubleValue() - new Double(daily).doubleValue())));
        annualNet.setText(""+(df.format((new Double(solarPower).doubleValue() - new Double(daily).doubleValue())*365)));
        quaterlyNet.setText(""+(df.format((new Double(solarPower).doubleValue() - new Double(daily).doubleValue())*365/4)));
     }	
 	
 }
