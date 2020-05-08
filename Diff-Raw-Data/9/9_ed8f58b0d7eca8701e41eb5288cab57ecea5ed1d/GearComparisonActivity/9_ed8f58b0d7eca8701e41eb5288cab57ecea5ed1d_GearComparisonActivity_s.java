 package com.TrackApp;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 import com.TrackApp.NumberPicker;
 import com.TrackApp.Gear_Inch_Calc;
 
 public class GearComparisonActivity extends Activity{
 	@Override
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gear_comparison);
 		
 		//Set up of Front Ring Select0r
 		final NumberPicker frontRing1 = (NumberPicker)findViewById(R.id.FrontRing1);
 		frontRing1.setRange(30, 65);
 		frontRing1.setCurrent(48);
 		
 		//Set up of Cog Selector
 		final NumberPicker rearCog1 = (NumberPicker)findViewById(R.id.RearCog1);
 		rearCog1.setRange(10,23);
 		rearCog1.setCurrent(14);	
 		
 		//Set up of Front Ring Select0r
 		final NumberPicker frontRing2 = (NumberPicker)findViewById(R.id.FrontRing2);
 		frontRing2.setRange(30, 65);
 		frontRing2.setCurrent(48);
 		
 		//Set up of Cog Selector
 		final NumberPicker rearCog2 = (NumberPicker)findViewById(R.id.RearCog2);
 		rearCog2.setRange(10,23);
 		rearCog2.setCurrent(14);	
 		
 		//Cadence
 		final SeekBar cadence= (SeekBar)findViewById(R.id.GCCadenceBar);
 		cadence.setMax(200);
 		cadence.setProgress(90);
 		
 		//TextViews
 		final TextView cadence_label = (TextView)findViewById(R.id.Cadence);
 		final TextView gearinch1 = (TextView)findViewById(R.id.GearInchOne);
 		final TextView gearinch2 = (TextView)findViewById(R.id.GearInchTwo);
 		final TextView rollout1 = (TextView)findViewById(R.id.Rollout_one);
 		final TextView rollout2 = (TextView)findViewById(R.id.Rollout_two);
 		final TextView speed1 = (TextView)findViewById(R.id.Speed_1);
 		final TextView speed2 = (TextView)findViewById(R.id.Speed_2);
 		
 		//Distance Times
 		final List<TextView> Time_TextViews_1 = new ArrayList<TextView>();
 		final List<TextView> Time_TextViews_2 = new ArrayList<TextView>();
 		final List<Integer> Distance = new ArrayList<Integer>();
 		
 		final TextView twohm1 = (TextView)findViewById(R.id.two_hund_m_1_time);
 		final TextView twohm2 = (TextView)findViewById(R.id.two_hund_m_2_time);
 		Time_TextViews_1.add(twohm1);
 		Time_TextViews_2.add(twohm2);
 		Distance.add(200);
 		
 		final TextView twofifty1 = (TextView)findViewById(R.id.two_fifty_m_1_time);
 		final TextView twofifty2 = (TextView)findViewById(R.id.two_fifty_m_2_time);
 		Time_TextViews_1.add(twofifty1);
 		Time_TextViews_2.add(twofifty2);
 		Distance.add(250);
 		
 		final TextView TTT1 = (TextView)findViewById(R.id.Three_thirdy_three_m_1_time);
 		final TextView TTT2 = (TextView)findViewById(R.id.Three_thirdy_three_m_2_time);
 		Time_TextViews_1.add(TTT1);
 		Time_TextViews_2.add(TTT2);
 		Distance.add(333);
 		
 		final TextView fourhm1 = (TextView)findViewById(R.id.four_hundred_m_1_time);
 		final TextView fourhm2 = (TextView)findViewById(R.id.four_hundred_m_2_time);
 		Time_TextViews_1.add(fourhm1);
 		Time_TextViews_2.add(fourhm2);
 		Distance.add(400);
 		
 		final TextView fivehm1 = (TextView)findViewById(R.id.five_hundred_m_1_time);
 		final TextView fivehm2 = (TextView)findViewById(R.id.five_hundred_m_2_time);
 		Time_TextViews_1.add(fivehm1);
 		Time_TextViews_2.add(fivehm2);
 		Distance.add(500);
 		
 		final TextView onek1 = (TextView)findViewById(R.id.one_k_1_time);
 		final TextView onek2 = (TextView)findViewById(R.id.one_k_2_time);
 		Time_TextViews_1.add(onek1);
 		Time_TextViews_2.add(onek2);
 		Distance.add(1000);
 
 		//Listener for change to Front Ring Selector
 		frontRing1.setOnChangeListener(new NumberPicker.OnChangedListener() {
 
 			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
 				//Change Gear Inches
 				Gear_Inch_Calc from_Front = new Gear_Inch_Calc(newVal,rearCog1.getCurrent());			
 				CharSequence x;
 				Double y= from_Front.getGearInches();
 				x = (CharSequence) y.toString();
 				gearinch1.setText(x);
 				
 				//Change Rollout
 				x = (CharSequence)((Double)from_Front.getDistanceTraveled()).toString();
 				rollout1.setText(x);
 			
 				//Change Speed
 				double speed = from_Front.KPH_Speed(cadence.getProgress());
 				x  =(CharSequence)((Double)speed).toString();
 				speed1.setText(x);
 				
 				//Update Times
 				Update_Times(Time_TextViews_1, Distance, from_Front, cadence.getProgress());
 
 			}
 		});
 				
 		//Listener for change to Cog Selector
 		rearCog1.setOnChangeListener(new NumberPicker.OnChangedListener() {
 
 			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
 				//Change Gear Inches
 				Gear_Inch_Calc from_Rear = new Gear_Inch_Calc(frontRing1.getCurrent(),newVal);
 				CharSequence x;
 				Double y= from_Rear.getGearInches();
 				x = (CharSequence) y.toString();
 				gearinch1.setText(x);
 				
 				//Change Rollout
 				x = (CharSequence)((Double)from_Rear.getDistanceTraveled()).toString();
 				rollout1.setText(x);
 	
 				//Change Speed
 				double speed = from_Rear.KPH_Speed(cadence.getProgress());
 				x  =(CharSequence)((Double)speed).toString();
 				speed1.setText(x);
 				
 				
 				//Update Distance Times
 				Update_Times(Time_TextViews_1, Distance, from_Rear, cadence.getProgress());
 				
 			}
 		});		
 		
 		frontRing2.setOnChangeListener(new NumberPicker.OnChangedListener() {
 
 			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
 				//Change Gear Inches
 				Gear_Inch_Calc from_Front = new Gear_Inch_Calc(newVal,rearCog2.getCurrent());			
 				CharSequence x;
 				Double y= from_Front.getGearInches();
 				x = (CharSequence) y.toString();
 				gearinch2.setText(x);
 				
 				//Change Rollout
 				x = (CharSequence)((Double)from_Front.getDistanceTraveled()).toString();
 				rollout2.setText(x);
 				
 				//Change Speed
 				double speed = from_Front.KPH_Speed(cadence.getProgress());
 				x  =(CharSequence)((Double)speed).toString();
 				speed2.setText(x);
 				
 				
 				//Update Times
 				Update_Times(Time_TextViews_2, Distance, from_Front, cadence.getProgress());
 				
 			}
 		});
 				
 		//Listener for change to Cog Selector
 		rearCog2.setOnChangeListener(new NumberPicker.OnChangedListener() {
 
 			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
 				//Change Gear Inches
 				Gear_Inch_Calc from_Rear = new Gear_Inch_Calc(frontRing2.getCurrent(),newVal);
 				CharSequence x;
 				Double y= from_Rear.getGearInches();
 				x = (CharSequence) y.toString();
 				gearinch2.setText(x);
 				
 				//Change Rollout
 				x = (CharSequence)((Double)from_Rear.getDistanceTraveled()).toString();
 				rollout2.setText(x);
 				
 				//Change Speed
 				double speed = from_Rear.KPH_Speed(cadence.getProgress());
 				x  =(CharSequence)((Double)speed).toString();
 				speed2.setText(x);
 				
 				
 				//Update Distance Times
 				Update_Times(Time_TextViews_2, Distance, from_Rear, cadence.getProgress());
 				
 			}
 		});	
 		
 		cadence.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 			
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				Gear_Inch_Calc gear_1 = new Gear_Inch_Calc(frontRing1.getCurrent(),rearCog1.getCurrent());
				CharSequence speed_1 = (CharSequence)((Double)gear_1.KPH_Speed(seekBar.getProgress())).toString();
				speed1.setText(speed_1);
 				Update_Times(Time_TextViews_1, Distance, gear_1, cadence.getProgress());
 				
 				Gear_Inch_Calc gear_2 = new Gear_Inch_Calc(frontRing2.getCurrent(), rearCog2.getCurrent());
				CharSequence speed_2 = (CharSequence)((Double)gear_2.KPH_Speed(seekBar.getProgress())).toString();
				speed2.setText(speed_2);
 				Update_Times(Time_TextViews_2, Distance, gear_2, cadence.getProgress());
 			}
 			
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				CharSequence x = ((Integer)progress).toString();
 				cadence_label.setText(x);
 			}
 		});
 	}
 	
 	//Abstraction to update times
 	private void Update_Times(List<TextView> views, List<Integer> Distances, Gear_Inch_Calc gear, int cadence)
 	{
 		for(int i=0; i<views.size(); i++)
 		{
 			views.get(i).setText((CharSequence) gear.Pretty_Print(
 					gear.Time_To_Complete(cadence, Distances.get(i))));
 		}
 
 	}
 	
 }
