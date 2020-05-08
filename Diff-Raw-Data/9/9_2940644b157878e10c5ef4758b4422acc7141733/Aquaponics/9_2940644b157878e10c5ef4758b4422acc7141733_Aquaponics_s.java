 package edu.hawaii.systemh.android.aquaponics;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import edu.hawaii.systemh.android.R;
 import edu.hawaii.systemh.android.menu.Menu;
 import edu.hawaii.systemh.android.systemdata.SystemData;
 
 /**
  * Activity to start the aquaponics page.
  * 
  * @author Group H
  * 
  */
 public class Aquaponics extends Activity {
 
     SeekBar temp;
     SeekBar ph;
     SeekBar level;
     SeekBar nutrients;
 
     TextView tempValue;
     TextView phValue;
     TextView levelValue;
     TextView nutrientsValue;
     
     TextView tempData;
     TextView phData;
     TextView ecData;
     TextView oxygenData;
     TextView levelData;
     TextView circulationData;
     TextView turbidityData;
     TextView deadFishData;
     
     Spinner feeds;
     
     Button feed;
     
     // Store new value to change
     int newTemp = 0;
     int fish = 0;
     String newPh = "0";
     String newWaterLevel = "0";
     String newNutrients = "0";
     double newFeedAmnt = 0;
    
     /**
      * Called when the activity is first created.
      * 
      * @param savedInstanceState
      *            - A mapping from String values to various Parcelable types.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // requesting to turn the title OFF
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         // making it full screen
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         setContentView(R.layout.aquaponics);
 
         /** Get Data Values for Aquaponic System **/
         final SystemData aquaponics = new SystemData("aquaponics");
         
         newTemp = (int) aquaponics.getTemp();
         tempData = (TextView) findViewById(R.id.tempDataValue);
         tempData.setText(newTemp + "\u00b0C");
         setTextColorStatus(tempData, newTemp, 28, 30, 20, 45);
         
         newPh = String.valueOf(aquaponics.getPh());
         phData = (TextView) findViewById(R.id.phDataValue);
         phData.setText(newPh);
         setTextColorStatus(phData, (int) (aquaponics.getPh() * 10), 70, 79, 66, 82);
         
         ecData = (TextView) findViewById(R.id.ecDataValue);
         ecData.setText(aquaponics.getElectricalConductivity() + " \u00b5s/cm");
         setTextColorStatus(ecData, (int) (aquaponics.getElectricalConductivity() * 100), 
                 1100, 1900, 900, 2100);
         
         oxygenData = (TextView) findViewById(R.id.oxygenDataValue);
         oxygenData.setText(aquaponics.getOxygen() + " mg/l");
         setTextColorStatus(oxygenData, (int) (aquaponics.getOxygen() * 100), 400, 500, 350, 650);
         
         newWaterLevel = (String.valueOf(aquaponics.getWaterLevel()));
         levelData = (TextView) findViewById(R.id.levelDataValue);
         levelData.setText(newWaterLevel + " in");
         setTextColorStatus(levelData, aquaponics.getWaterLevel(), 38, 45, 24, 60);
         
         circulationData = (TextView) findViewById(R.id.circulationDataValue);
         circulationData.setText(aquaponics.getCirculation() + " gpm");
         setTextColorStatus(circulationData, (int) (aquaponics.getCirculation() * 100), 
                 6500, 9500, 5000, 11000);
         
         turbidityData = (TextView) findViewById(R.id.turbidityDataValue);
         turbidityData.setText(aquaponics.getTurbidity() + " NTUs");
         setTextColorStatus(turbidityData, (int) (aquaponics.getTurbidity() * 100), 
                 0, 10000, 0, 11000);
         
         deadFishData = (TextView) findViewById(R.id.deadFishDataValue);
         deadFishData.setText(String.valueOf(aquaponics.getDeadFish()));
         setTextColorStatus(deadFishData, aquaponics.getDeadFish(), 0, 0, 0, 0);
         
         /** Water Temperature Slider Control**/
         temp = (SeekBar) this.findViewById(R.id.tempSeekbar);
         temp.setMax(110);
         temp.setProgress(newTemp);
         
         tempValue = (TextView) this.findViewById(R.id.tempValue);
         tempValue.setText(String.valueOf(newTemp));
         
         temp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress,
                     boolean fromUser) {
                 
                 newTemp = progress;
                 tempValue.setText(String.valueOf(newTemp));
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
                 // TODO Auto-generated method stub
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
                
                 aquaponics.setAquaponicsTemp(newTemp);
             }
         });
 
         /** PH Level Slider Control**/
         ph = (SeekBar) this.findViewById(R.id.phSeekBar);
         ph.setMax(140);
         ph.setProgress((int) (aquaponics.getPh() * 10));
         //ph.setProgress(10);
         
         phValue = (TextView) this.findViewById(R.id.phValue);
         phValue.setText(newPh);
         
         ph.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress,
                     boolean fromUser) {
                 
                 newPh = String.valueOf((float) progress / 10);
                 phValue.setText(newPh);
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
                 // TODO Auto-generated method stub
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
                 
                 aquaponics.setPh(Double.parseDouble(newPh));
             }
         });
 
         /** Water Level Slider Control**/
         level = (SeekBar) this.findViewById(R.id.levelSeekBar);
         level.setMax(10000);
         level.setProgress((int) (aquaponics.getWaterLevel() * 100));
         
         levelValue = (TextView) this.findViewById(R.id.levelValue);
         levelValue.setText(String.valueOf(newWaterLevel));
         
         level.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress,
                     boolean fromUser) {
                 
                 newWaterLevel = String.valueOf((float) progress / 100);
                 levelValue.setText(String.valueOf(newWaterLevel));
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
                 // TODO Auto-generated method stub
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
                 
                aquaponics.setWaterLevel(Double.parseDouble(newWaterLevel));
             }
         });
 
         /** Water Nutrients Slider Control **/
         nutrients = (SeekBar) this.findViewById(R.id.nutrientsSeekBar);
         nutrients.setMax(10000);
         nutrients.setProgress(0);
         
         nutrientsValue = (TextView) this.findViewById(R.id.nutrientsValue);
         nutrients
         .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
             @Override
             public void onProgressChanged(SeekBar seekBar,
                     int progress, boolean fromUser) {
                
                 newNutrients = String.valueOf((float) progress / 100);
                 nutrientsValue.setText(String.valueOf(newNutrients));
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
                 // TODO Auto-generated method stub
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
                 
                 aquaponics.setNutrients(Double.parseDouble(newNutrients));
             }
         });
         
         /** Feed Fish **/
         feeds = (Spinner) findViewById(R.id.feedSpinner);
         ArrayAdapter<CharSequence> adapter =
             ArrayAdapter.createFromResource(this, R.array.feedAmount, R.layout.spinner);
         adapter.setDropDownViewResource(R.layout.spinner_dropdown);
         feeds.setAdapter(adapter);
         feeds.setOnItemSelectedListener(new MyOnItemSelectedListener());
         
         feed = (Button) findViewById(R.id.feedFishButton);
         feed.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 aquaponics.feedFish(newFeedAmnt);
                 
             }
         });
   
     }
 
     /**
      * The listener for the feed amount choices.
      * 
      * @author Group H
      * 
      */
     public class MyOnItemSelectedListener implements OnItemSelectedListener {
 
       @Override
       public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 
         // update the components according to the selected feed amount.
        newFeedAmnt = Double.parseDouble(String.valueOf(feeds.getSelectedItem()));   
       }
 
       @Override
       public void onNothingSelected(AdapterView<?> parent) {
         // Do nothing.
       }
     }
     
     /**
      * Set text color depending on the range of the value.
      * 
      * @param text The TextView
      * @param value The current value
      * @param minWarn Min value for Warning (gold)
      * @param maxWarn Max value for Warning (gold)
      * @param minAlrt Min value for Alert (red)
      * @param maxAlrt Max value for Alert (red)
      */
     public void setTextColorStatus(TextView text, int value, int minWarn, 
             int maxWarn, int minAlrt, int maxAlrt) {
         
         
         if (value < minAlrt || value > maxAlrt) {
             text.setTextColor(Color.RED);
         } 
         else if ((value < minWarn && value > minAlrt) || (value > maxWarn && value < maxAlrt)) {
             text.setTextColor(0xFFFFD700);
         }
         else {
             text.setTextColor(Color.GREEN);
         }
         
     }
     
     
     /**
      * Take the user to the menu.
      * 
      * @param view The view.
      */
     public void showMenu(View view) {
         Intent intent = new Intent(Intent.ACTION_VIEW);
         intent.setClassName(this, Menu.class.getName());
         startActivity(intent);
     }
 
     /**
      * Destroys this activity when onStop is called.
      */
     @Override
     protected void onStop() {
         finish();
         super.onDestroy();
     }
 
 }
