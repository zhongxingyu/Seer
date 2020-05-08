 package amazenite.lockit;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Vector;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 
 public class Lockit extends Activity {	
 	Spinner NumberofGestures;
 	
 	boolean enabled	= false;
 	boolean visible = true;
 	int chosenColor = 0xff33CCCC;
 	
 	
 	/** Called when the user clicks the get image button */
 	public void viewPictures(View view) {
 	    // Do something in response to button
 		final Intent intent = new Intent(this, OpenImages.class);
 		startActivity(intent);
 	}
 	
 	/** Lockscreen Test Points */
 	public void debugLockscreen(View view) {
 	    // debug the lockscreen
 		final Intent intent = new Intent(this, LockScreen.class);
 		startActivity(intent);
 	}
 	/** Lockscreen Gesture Visibility */
 	public void toggleVisible(View view) {
 	    // toggle gesture visibility
 		visible = !visible;
 		saveStatus(""+visible,"togVisible");	
 		Log.d("IS IT VISIBLE?",""+visible);
 	}
 	/** Lockscreen Gesture Color Picker */
 	public void setColors(View view){
 		final Intent intent = new Intent(this, ColorSelection.class);
 		startActivity(intent);
 	}
 	
 	public void saveStatus(String Stats,String fileName){
 		
 		try {
         	FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
 
 	        	try {
 					fos.write(Stats.getBytes());
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
         	try {
 	        		fos.close();
 	        		fos = null;
 	        	} 
 	        	catch (IOException e) {
 	        		e.printStackTrace();
 	        	}
         	} 
         catch (FileNotFoundException e1) {
         	e1.printStackTrace();
         	}
 		
 		
 	}
 	
 	  public void saveColor(int color)
       {
       	
       	String pickedColor = ""+color;
       	try {
           	FileOutputStream fos = openFileOutput("pickedColor", Context.MODE_PRIVATE);
 
   	        	try {
   					fos.write(pickedColor.getBytes());
   				} catch (IOException e) {
   					// TODO Auto-generated catch block
   					e.printStackTrace();
   				}
 
           	try {
   	        		fos.close();
   	        		fos = null;
   	        	} 
   	        	catch (IOException e) {
   	        		e.printStackTrace();
   	        	}
           	} 
           catch (FileNotFoundException e1) {
           	e1.printStackTrace();
           	}
       }
 	
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		getPreview();
 	}
 	
 	
 	public void getPreview(){
 		//pictureSettings();
 	    ImageView img = (ImageView) findViewById(R.id.preview);
 	    if(img != null)
 	    {
 		    File file = getBaseContext().getFileStreamPath("lockimg");
 		    String internalPath = "data/data/files/lockimg";
 		    if (file.exists()) {
 		    	 internalPath = file.getAbsolutePath();
 		        	Drawable d = Drawable.createFromPath(internalPath);
 		         if(d!=null)
 		         {
 		        	 img.setImageDrawable(d);
 		        	 img.invalidate();
 		         }
 		    }
 		    else{
 		    	Toast.makeText(Lockit.this, "" + "Unable To Find File", Toast.LENGTH_SHORT).show();
 		    	Vector<Integer> defaultPic = new Vector<Integer>();
 		    	defaultPic.add(R.drawable.ic_launcher);
 		    	if(defaultPic.get(0) != null)
 		    	{
 			    	saveImage2(defaultPic, 0);
 		    	}
 		    }
 	    }
     }
 
 	
 	public void setPoints(View view)
 	{
 
 		Intent intent = new Intent(this, SetPoints.class);
 		startActivity(intent);
 	}
 	
 	 public void saveImage2(Vector<Integer> images, int num)
 	    {
 	    	BitmapFactory.Options o = new BitmapFactory.Options();
 		    o.inJustDecodeBounds = true;
 	        final int size = 70;
 	        int scale = 2;
 	        while(o.outWidth/scale/2 >= size && o.outHeight/scale/2 >= size)
 	        {
 	        	scale *=2;
 	        }
 	     	BitmapFactory.Options o2 = new BitmapFactory.Options();
 	     	o2.inSampleSize=scale;
 	     	if(images.get(num) == null)
 	     	{
 	     		Log.d("open images", "image null");
 	     	}
 	     	Bitmap samplePic = BitmapFactory.decodeResource(getResources(), images.get(num), o2);
 	 		if(samplePic != null)
 	 		{
 		 		 try {
 			        	FileOutputStream fos = openFileOutput("lockimg", Context.MODE_PRIVATE);
 			        	samplePic.compress(CompressFormat.JPEG, 100, fos);
 				        	try {
 				        		fos.close();
 				        		fos = null;
 				        	} 
 				        	catch (IOException e) {
 				        		e.printStackTrace();
 				        	}
 				        	samplePic.recycle();
 			        	} 
 				        catch (FileNotFoundException e1) {
 				        	e1.printStackTrace();
 				  }
 	 		}
 	}
 	    
 	public void enablePicPw(View view)
 	{
	    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 
 		
 	    Intent intent = new Intent(this, ScreenReceiver.class);
 	    
 		enabled = !enabled;
 		String status = "" + enabled;
 		
 		intent.putExtra("status", status);	
 	    
 	    
 		PendingIntent sender = PendingIntent.getBroadcast(this, 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);	
 		saveStatus(""+enabled,"enablePicPw");	
 		Log.d("Is It Enabled?: ",status);
 		
	   alarmManager.set(AlarmManager.RTC_WAKEUP,100000000, sender);
 
 		
 	}
 
 	
 	public void pictureSettings()
 	{
 		//
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_lockit);
 		addItemsToSpinner();
 		addListenerOnSpinnerItemSelection();
 		setSpinner();
 		getPreview();
 		File file = getBaseContext().getFileStreamPath("togVisible");
 	    if (!(file.exists())) //if it doesn't exist
 	    {
 	    	saveStatus(""+visible,"togVisible");	
 	    }
 		File file2 = getBaseContext().getFileStreamPath("pickedColor");
 	    if (!(file2.exists())) //if it doesn't exist
 	    {
 	    	saveColor(chosenColor);	
 	    }
 		saveStatus(""+enabled,"enablePicPw");	
 	}
 	
 	public void addItemsToSpinner()
 	{
 		NumberofGestures = (Spinner) findViewById(R.id.spinner);
 		List<String> list = new ArrayList<String>();
 		list.add("2");
 		list.add("3");
 		list.add("4");
 		list.add("5");
 		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
 		android.R.layout.simple_spinner_item, list);
 		dataAdapter.setDropDownViewResource
 
 		(android.R.layout.simple_spinner_dropdown_item);
 		NumberofGestures.setAdapter(dataAdapter);
 	}
 	
 	  public void addListenerOnSpinnerItemSelection() {
 		  NumberofGestures = (Spinner) findViewById(R.id.spinner);
 		  NumberofGestures.setOnItemSelectedListener(new CustomOnItemSelectedListener());
 	  }
 	  
 	  public void setSpinner()
 	  {
 			try {
 				File file = getBaseContext().getFileStreamPath("numGestures");
 				Scanner sc = new Scanner(new File(file.getAbsolutePath()));
 				String line = sc.nextLine();
 				int num = Integer.parseInt(line);
 				if(num != 0)
 				{
 					NumberofGestures.setSelection(num-2);
 				}
 			}
 	        catch (FileNotFoundException e1) {
 	        	e1.printStackTrace();
 	  		  	NumberofGestures.setSelection(1);
 	        }
 			
 	  }
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_lockit, menu);
 		return true;
 	}
 }
