 package com.example.groupproject;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 
public class MainActivity extends Activity
 {
   private Button entertainmentBtn,
                  beachLocationsBtn,
                  hotelsBtn,
                  restaurantsBtn,
                  hospitalsBtn,
                  shoppingBtn;
 	
 	/**  Guide Lines and ideas*/
 	
 	//create listener buttons for categories - about 5 categories
 	  //possibly spawn new activities as a result from button clicks to open more selections
 	
 	//add google maps for locations; all else fails, open web page to google map location.
 	
 	//add simple animations?
 	
 	//link to web pages for information
 	  //Entertainment - http://www.marinaciviccenter.com/
 						//http://www.martintheatre.com/
 			//Visual Arts Center http://www.vac.org.cn/home.html  
 	
 	  //Hospitals - 
 		//Bay Med Map http://tinyurl.com/mdv6w7y
 	    //Gulf Medical Map http://tinyurl.com/l8nuaaa
 		
 	  //Shopping - 
 		//Pier Park(long link) http://tinyurl.com/pfhv888
 		//Wal-Mart on beach http://tinyurl.com/pfhv888
 	  
 	  //Lodging - Various Map locations of lodging http://tinyurl.com/pfhv888
 		
 	  //Restaurants - Various Restaurant locations http://tinyurl.com/kx5p8k2
 	
 	
 	//main source for visitor information:  http://www.pcgov.org/visitors
 	
 	//various button categories that open more selections ( see document for categories.)  Possibly add 'about' section?
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
		entertainmentBtn = (Button) findViewById(R.id.entertainment);
 		entertainmentBtn.setOnClickListener(entertainmentListener);
 		
		beachLocationsBtn = (Button) findViewById(R.id.beachLocations);
 		beachLocationsBtn.setOnClickListener(beachLocationsListener);
 		
    restaurantsBtn = (Button) findViewById(R.id.restaurants);
    restaurantsBtn.setOnClickListener(restaurantsListener);
     
		hotelsBtn = (Button) findViewById(R.id.hotels);
 		hotelsBtn.setOnClickListener(hotelsListener);
 		
		hospitalsBtn = (Button) findViewById(R.id.hospitals);
 		hospitalsBtn.setOnClickListener(hospitalsListener);
 		
		shoppingBtn = (Button) findViewById(R.id.shopping);
		shoppingBtn.setOnClickListener(shoppingListener);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
   OnClickListener entertainmentListener = new OnClickListener()
   {
     public void onClick(View v)
     {
       WebView browser = new WebView(getApplicationContext());
       setContentView(browser);
       browser.loadUrl("http://www.pcgov.org/visitors/arts-and-entertainment");
     }
   };
 
   OnClickListener beachLocationsListener = new OnClickListener()
   {
     public void onClick(View v)
     {
     }
   };
 
   OnClickListener hotelstListener = new OnClickListener()
   {
     public void onClick(View v)
     {
     }
   };
  
  OnClickListener hotelsListener = new OnClickListener()
  {
    public void onClick(View v)
    {
    }
  };
 
   OnClickListener hospitalsListener = new OnClickListener()
   {
     public void onClick(View v)
     {
     }
   };
 
   OnClickListener restaurantsListener = new OnClickListener()
   {
     public void onClick(View v)
     {
     }
   };
 
   OnClickListener shoppingListener = new OnClickListener()
   {
     public void onClick(View v)
     {
     }
   };
 
 }
