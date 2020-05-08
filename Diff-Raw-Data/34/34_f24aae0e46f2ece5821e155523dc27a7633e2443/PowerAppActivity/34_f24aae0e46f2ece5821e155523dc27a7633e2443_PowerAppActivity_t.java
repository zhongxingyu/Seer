 package spivey.app.practice0001;
 
 import java.io.IOException;
 import java.util.List;
 
 import spivey.app.practice0001.*;
 //import spivey.app.practice0001.R;
 //import spivey.app.practice0001.House;
 import android.util.Log;
 //import spivey.app.practice0001.DatabaseHandler;
 //import spivey.app.practice0001.Appliance;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class PowerAppActivity extends Activity implements OnClickListener {
     /** Called when the activity is first created. */
 //	public static int house_count = 0;
 //	public static House houses[] = new House[10];  // Max 10 houses during first iteration of the program
 
 	
 	Button B1=null;
 	Button B3=null;
 	Button ServiceEnter=null;
 	Button Main=null;
 	Button buttonMain=null;
 	Button buttonAmp=null;
 	Button buttonApp=null;
 	Button reset=null;
	EditText housenameEdit;
	EditText serviceamountEdit;
 	
  /*   
 	public static House new_house() throws IOException {
 //		read_input new_input = new read_input();
 		int serAMP = 0;
 
 //		serAMP = new_input.read_input_int();
 		House H = new House();
 		H.Service = serAMP;
 		house_count++;
 		for (int i = 0; i < 10; i++)
 		{
 			H.appliances[i] = new Appliance();
 		}
 		
 		return H;
     }   */
   
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         B1=(Button)findViewById(R.id.button1);
         B3=(Button)findViewById(R.id.button3);
     //    B1.setOnClickListener(this);
         B1.setPressed(false);
  //       B3.setOnClickListener(this);
         B3.setPressed(false);
         reset=(Button)findViewById(R.id.Reset);
         reset.setPressed(false);
         


        
         
         Context aContext = getBaseContext();
 
 
         
         B1.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View v) {
            //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
             //    startActivity(myintent2);
             	
             	setContentView(R.layout.button1);
                housenameEdit =(EditText)findViewById(R.id.HouseName_text);
                serviceamountEdit = (EditText)findViewById(R.id.ServiceAmount);
             	ServiceEnter=(Button)findViewById(R.id.ServiceEnter);
                 ServiceEnter.setOnClickListener(enterHandler);
                 ServiceEnter.setPressed(false);
                 
             }
         });
         
         B3.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View v) {
            //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
             //    startActivity(myintent2);
             	setContentView(R.layout.ampdisplay);
             	
             	buttonMain=(Button)findViewById(R.id.buttonMain);
             	buttonMain.setOnClickListener(exitOneHandler);
             	buttonMain.setPressed(false);
 
             }
         });
         
         reset.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View v) {
            //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
             //    startActivity(myintent2);
             	setContentView(R.layout.resetdisplay);
             	
             	buttonMain=(Button)findViewById(R.id.buttonMain);
             	buttonMain.setOnClickListener(exitOneHandler);
             	buttonMain.setPressed(false);
 
             }
         });
 /*     
         Main=(Button)findViewById(R.id.buttonMain);
         Main.setOnClickListener(this);
         Main.setPressed(false);        */
     }
     
 /*    public void onClick(View v){
     	
     	
     	switch (v.getId()) {
         case R.id.button1: 
             ServiceEnter=(Button)findViewById(R.id.ServiceEnter);
             ServiceEnter.setOnClickListener(enterHandler);
             ServiceEnter.setPressed(false);
        	try {
 			houses[house_count] = new_house();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
          break;
         case R.id.button3:
         	setContentView(R.layout.ampdisplay);
         	
         	buttonMain=(Button)findViewById(R.id.buttonMain);
         	buttonMain.setOnClickListener(exitOneHandler);
         	buttonMain.setPressed(false);
          break;
      }
 
     } */
 
     
     private OnClickListener enterHandler = new OnClickListener() {
     	public void onClick(View v){
     		if(ServiceEnter.isPressed()){
             	//save text entry to serAmp in new house function
             	setContentView(R.layout.houseconfig);
             	
             	buttonMain=(Button)findViewById(R.id.buttonMain);
       //      	buttonMain.setOnClickListener(exitOneHandler);
             	buttonMain.setPressed(false);
             	
             	buttonApp=(Button)findViewById(R.id.buttonApp);
       //      	buttonMain.setOnClickListener(exitOneHandler);
             	buttonApp.setPressed(false);
             	
             	buttonAmp=(Button)findViewById(R.id.buttonAmp);
       //      	buttonMain.setOnClickListener(exitOneHandler);
             	buttonAmp.setPressed(false);
             	
             	//Database
             	//TODO
             	/*
             	private DatabaseHandler exitOneHandler = new OnClickListener() {
                 	public void onClick(View v){
             	DatabaseHandler db = new DatabaseHandler(this);
             	 */
                 /**
                  * CRUD Operations
                  * */
                 // Inserting Contacts get from edittext
             	
             	DatabaseHandler db = new DatabaseHandler(getBaseContext());
                 Log.v("Insert: ", "Inserting ..");
                 db.addHouse(new House(1, housenameEdit.getText().toString(),Integer.parseInt(serviceamountEdit.getText().toString())));
                 //db.addHouse(new House(2,"House2", 2200));
                 //db.addHouse(new House(3,"House3", 3000));
                 //db.addHouse(new House(4,"House4", 1500));
          
                 // Reading all contacts
                 Log.d("Reading: ", "Reading all contacts..");
                 List<House> contacts = db.getAllHouses();       
          
                 for (House cn : contacts) {
                     String log = "HouseId: "+cn.getID()+" ,Name: " + cn.getName() + " ,Service Amps: " + cn.getService();
                         // Writing Contacts to log
                 Log.v("Name: ", log);
                }
 
            	

             	
                 
                 buttonAmp.setOnClickListener(new View.OnClickListener() {
 
                     public void onClick(View v) {
                    //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
                     //    startActivity(myintent2);
                     	setContentView(R.layout.ampdisplay);
                     	
                     	buttonMain=(Button)findViewById(R.id.buttonMain);
                     	buttonMain.setOnClickListener(exitOneHandler);
                     	buttonMain.setPressed(false);
                     }
                 });
                 
                 buttonApp.setOnClickListener(new View.OnClickListener() {
 
                     public void onClick(View v) {
                    //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
                     //    startActivity(myintent2);
                     	setContentView(R.layout.appdisplay);
                     	
                     	buttonMain=(Button)findViewById(R.id.buttonMain);
                     	buttonMain.setOnClickListener(exitOneHandler);
                     	buttonMain.setPressed(false);
 
                     }
                 });
                 
                 buttonMain.setOnClickListener(new View.OnClickListener() {
 
                     public void onClick(View v) {
                    //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
                     //    startActivity(myintent2);
                     	setContentView(R.layout.main);
                     	
                         B1=(Button)findViewById(R.id.button1);
                         B3=(Button)findViewById(R.id.button3);
                     //    B1.setOnClickListener(this);
                         B1.setPressed(false);
                  //       B3.setOnClickListener(this);
                         B3.setPressed(false);
                         reset=(Button)findViewById(R.id.Reset);
                         reset.setPressed(false);
                         
                         
                         B1.setOnClickListener(new View.OnClickListener() {
 
                             public void onClick(View v) {
                            //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
                             //    startActivity(myintent2);
                             	setContentView(R.layout.button1);
                                housenameEdit =(EditText)findViewById(R.id.HouseName_text);
                                serviceamountEdit = (EditText)findViewById(R.id.ServiceAmount);
                             	ServiceEnter=(Button)findViewById(R.id.ServiceEnter);
                                 ServiceEnter.setOnClickListener(enterHandler);
                                 ServiceEnter.setPressed(false);
                                
                            	

 
                             }
                         });
                         
                         B3.setOnClickListener(new View.OnClickListener() {
 
                             public void onClick(View v) {
                            //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
                             //    startActivity(myintent2);
                             	setContentView(R.layout.ampdisplay);
                             	
                             	buttonMain=(Button)findViewById(R.id.buttonMain);
                             	buttonMain.setOnClickListener(exitOneHandler);
                             	buttonMain.setPressed(false);
 
                             }
                         });
                         reset.setOnClickListener(new View.OnClickListener() {
 
                             public void onClick(View v) {
                            //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
                             //    startActivity(myintent2);
                             	setContentView(R.layout.resetdisplay);
                             	
                             	buttonMain=(Button)findViewById(R.id.buttonMain);
                             	buttonMain.setOnClickListener(exitOneHandler);
                             	buttonMain.setPressed(false);
 
                             }
                         });
                     }
                 });
 
 
             }
     	}
     };
     
     
     
     private OnClickListener exitOneHandler = new OnClickListener() {
     	public void onClick(View v){
     		if(buttonMain.isPressed()){
             	//save text entry to serAmp in new house function
             	setContentView(R.layout.main);
                 B1=(Button)findViewById(R.id.button1);
                 B3=(Button)findViewById(R.id.button3);
                 B1.setPressed(false);
                 B3.setPressed(false);
             	
                 reset=(Button)findViewById(R.id.Reset);
                 reset.setPressed(false);
                 
                 B1.setOnClickListener(new View.OnClickListener() {
 
                     public void onClick(View v) {
                    //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
                     //    startActivity(myintent2);
                     	setContentView(R.layout.button1);
                        housenameEdit =(EditText)findViewById(R.id.HouseName_text);
                        serviceamountEdit = (EditText)findViewById(R.id.ServiceAmount);
                     	ServiceEnter=(Button)findViewById(R.id.ServiceEnter);
                         ServiceEnter.setOnClickListener(enterHandler);
                         ServiceEnter.setPressed(false);
 
                     }
                 });
                 
                 B3.setOnClickListener(new View.OnClickListener() {
 
                     public void onClick(View v) {
                    //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
                     //    startActivity(myintent2);
                     	setContentView(R.layout.ampdisplay);
                     	
                     	buttonMain=(Button)findViewById(R.id.buttonMain);
                     	buttonMain.setOnClickListener(exitOneHandler);
                     	buttonMain.setPressed(false);
 
                     }
                 });
                 reset.setOnClickListener(new View.OnClickListener() {
 
                     public void onClick(View v) {
                    //     Intent myintent2 = new Intent(PowerAppActivity.this,PowerAppActivity.class);
                     //    startActivity(myintent2);
                     	setContentView(R.layout.resetdisplay);
                     	
                     	buttonMain=(Button)findViewById(R.id.buttonMain);
                     	buttonMain.setOnClickListener(exitOneHandler);
                     	buttonMain.setPressed(false);
 
                     }
                 });
                 
                 
                 
                 
             }
     	}
     };
     
     
     
     private OnClickListener myClickHandler = new OnClickListener() {
     	public void onClick(View v){
     		if(B1.isPressed()){
             	setContentView(R.layout.button1);
                housenameEdit =(EditText)findViewById(R.id.HouseName_text);
                serviceamountEdit = (EditText)findViewById(R.id.ServiceAmount);
                 ServiceEnter=(Button)findViewById(R.id.ServiceEnter);
                 ServiceEnter.setOnClickListener(enterHandler);
                 ServiceEnter.setPressed(false);
             }
     	}
     };
     
     private OnClickListener ampDisHandler = new OnClickListener() {
     	public void onClick(View v){
     		if(B3.isPressed()){
             	setContentView(R.layout.ampdisplay);
             	
             	buttonMain=(Button)findViewById(R.id.buttonMain);
             	buttonMain.setOnClickListener(exitOneHandler);
             	buttonMain.setPressed(false);
             	/*
                 ServiceEnter=(Button)findViewById(R.id.ServiceEnter);
                 ServiceEnter.setOnClickListener(enterHandler);
                 ServiceEnter.setPressed(false); */
             }
     	}
     };
 
 
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		
 	}
 
     
 /*    
     public void onStart(){
     	
     }
     
     public void onResume() {
     	
     }
     
     public void onPause() {
     	
     }
     
     public void onStop() {
     	
     }
     
     public void onDestroy() {
     	
     }
     
     public void onRestart() {
     	
     }
 */
 }
