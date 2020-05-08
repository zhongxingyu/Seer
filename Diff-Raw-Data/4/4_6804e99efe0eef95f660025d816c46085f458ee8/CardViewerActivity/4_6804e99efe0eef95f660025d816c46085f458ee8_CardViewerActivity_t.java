 package org.walkingarchive.app;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class CardViewerActivity extends Activity {
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.card_viewer);
         
         String cardJson = getIntent().getExtras().getString("cardJson");
         
         JSONObject json;
         try
         {
         	json = new JSONObject(cardJson);
         	TextView cardName = (TextView) findViewById(R.id.cardName);
             cardName.setText(json.getString("name"));
            
             TextView cardType = (TextView) findViewById(R.id.cardType);
            if(json.getString("subtype").equals("null"))
             	cardType.setText(json.getString("type"));
             else
             	cardType.setText(json.getString("type") + " - " + json.getString("subtype"));
             
             TextView cardMana = (TextView) findViewById(R.id.cardMana);
             // cardMana.setText(json.getString("mana"));
             // TODO: Actually assign it a value when this is supported by the API
             
             TextView cardValue = (TextView) findViewById(R.id.cardValue);
             // TODO: Same for this, however the API does it...
             
             TextView cardRules = (TextView) findViewById(R.id.cardRules);
             cardRules.setText(json.getString("text"));
             
             TextView cardFlavorText = (TextView) findViewById(R.id.cardFlavorText);
             cardFlavorText.setText(json.getString("flavortext"));
         }
         catch(JSONException e)
         {
         	throw new RuntimeException(e);
         }
     }
 }
