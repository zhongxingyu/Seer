 package com.scunt.app;
 
 import com.scunt.app.LazyAdapter;
 
 import java.io.IOException;
 import java.util.Random;
 
 import android.app.Activity;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.View;
 import android.widget.ListView;
 
 public class ScuntApp extends Activity {
 
     ListView list;
     LazyAdapter adapter;
     public static final String TAG = "Aamir";
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
         
     // Called when the "Tasks" button is clicked on the front page
     public void tasksClicked(View view) {
         setContentView(R.layout.tasks);
         
         // Generate, hmm, 15 random tasks
         String[][] tasks = GetRandomMissionInformation(15);
         
     	// Code goes here        
         list=(ListView)findViewById(R.id.list);
         adapter=new LazyAdapter(this, tasks);
         list.setAdapter(adapter);      
     }
     
     public void useCamera (View view) {
 
     }
     
     public void initiateSkunt(View view) {
         setContentView(R.layout.specs);
     }
     
     public void tasksDetails(View view) {
     	Log.v(TAG, "Showing task details");
 
         setContentView(R.layout.mission);
     }
 
     public String[][] GetRandomMissionInformation(int totalRandom)
     {
         String[][] MissionInformationPair = { {"Knock, Knock", "Exact Change", "Awkward!", "Egypt Here We Come",
         	"Nice Stick", "Hang Em' High", "Stomach Problems", "Sleepy Time", "Woof Woof", "Tight Squeeze", "You've Got Balls",
         	"5 Pillars of Pylon", "Oldest Book", "Now That's a Rock", "Human Tic-Tac-Toe", "Small World", "Cheapest Convenience",
         	"Dance Monkeys!", "McSmile", "Yarrr Pirates", "Yellow Fever", "Rickroll in real life", "Employed?", "Write This Down",
         	"What a Year", "How Friendly","Out of State"},
         	{"Dressed up in costume, Trick or Treat at a random house and actually receive edible items.",
         		"Obtain a receipt for exactly $0.11 of gas. At lease one team member must be in photo.",
         		"Take a photo with your team and at least 2 ex-significant others.",
         		"Construct the largest human pyramid you possibly can. Must be at least 3 levels.",
         		"Arrange the most provocative photo with a hockey stick or golf club.",
         		"Take a photo with all team members high-fiving complete strangers simultaneously.",
         		"Purchase and consume a greasy burger. Point goes to highest calorie sandwich.",
         		"Take a photo with 8 or more people lying down simultaneously. At least one team member must be in photo.",
         		"Find a dog. Have each member place one or more hands on dog. Photograph.",
         		"Locate a shopping cart. Fit the entire team less one member inside. All limbs must be contained. Photograph.",
         		"Find the largest variety of sports balls possible. Photograph them all in one picture.",
         		"They're markers, they're party hats - they're pylons! Find 5 and photo.",
         		"Books have been how humanity has recorded its existence for all of time. Find one of the first.",
         		"Its a rockin' world - find the biggest rock. Must bring rock back to final rendevouz.",
         		"Battle of minds. Battle of strategy. Play Tic-tak-toe in real life, with real people.",
         		"Ideas and news comes from around the world. Find the most foreign newspaper.",
         		"Being a student is about saving cash. Find the cheapest thing in your local convenience store.",
         		"Get at least 7 people to celebrate life and dance in a public space!",
         		"Go to McDonald's. Photograph at least 3 employees giving a free smile.",
         		"Construct and wear a pirate hat made of newspaper.",
         		"Collect the most number of fast-food mustard packets as possible. Keep the evidence.",
         		"Knock on someones door and sing \"never gonna give you up\" caroling style",
         		"Photo of a team member working at a service job they're not actually employed at.",
         		"Obtain a free promotional pen, with the company's name on it. Must be obtained during competition.",
         		"Obtain a coin of any denomination from 1999", "Photo a team member pumping a complete stranger's gas.",
         		"Snap a picture of the most far-away license plate you can find. Team member must be in photo."}
         	};
     	
         String[][] randArr = new String[26][2];
         Random randomInstance = new Random();

        for (int i = 0; i < totalRandom; i++) {
        	int randomNumber = randomInstance.nextInt(26);
         	
         	randArr[i][0] = MissionInformationPair[0][randomNumber];
         	randArr[i][1] = MissionInformationPair[1][randomNumber];
         	        	
         	Log.v(TAG, "onCreate: " + i + " pick: " + randArr[i][0] + " (" + randArr[i][1] + ")");
         }
                 
     	return randArr;
     }
 }
