 package com.example.ydp;
 
 
 
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.apache.commons.lang.ArrayUtils;
 
 
 
 import android.R.array;
 import android.R.color;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
  class MyJavaScriptInterface   
 {  
 	 
 	 String allergyvar  = "";
 	  String userName = "";
 	 ArrayList<String[]> careplanRecords;
 	  ArrayList<String[]> allergyRecords;
 	  UIFORCAREPLAN uiforcareplan;
     @SuppressWarnings("unused")  
     
    public void setUserName(String html)  
     {  
    	userName = html; 
    	
     } 
      
    public void showCareplaneHTML(String html) 
     { 
    	
    	 String[] careplandata = html.split("[$#]+");
    	 
    	// int lenght = careplandata.length;
    	// Toast.makeText(getApplicationContext(), lenght, 3000).show();
    	  
    	 for( int i=0;(i+9)<=careplandata.length;i+=9)    
    	 {
    		 String url = careplandata[i+2].toString();
 			 Log.v("url",url);
 			 String  status = url.substring(url.lastIndexOf('/')+1, url.lastIndexOf('.'));
 			 
 			 Log.v("filename",status);
 			 
 			 
 			 
    		 String[] record = {careplandata[i],careplandata[i+1],status,careplandata[i+3],careplandata[i+4],careplandata[i+5],careplandata[i+6],careplandata[i+7],careplandata[i+8]};
    		 careplanRecords.add( record);
    		 Log.v("in for loop", "data executing");
    	   
    	 }
    	
     }
     
    public void showAllergiesHTML(String html)  
      {  
       
    	String[] allergydata = html.split("[$#]+");
 // String allergyvar  = "";
 
   	 for( int i=0;i<allergydata.length;i+=4)
   	 {
   		 if(i!=0)
   		 {
   			 allergyvar += ", "; 
   		 }
   		 String[] record = {allergydata[i],allergydata[i+1],allergydata[i+2],allergydata[i+3]};
   		 
   		 allergyvar += allergydata[i];
   		 
   	    allergyRecords.add( record);
   	    
   	 }
 
   	//uiforcareplan.showAllallergy();
      }
    
 }
  
  
  
 public class UIFORCAREPLAN extends Activity implements OnItemClickListener{
 	
 	 
 	WebView wb1;
 	private ProgressDialog progressBar;
 	TextView user,pass;
 	 int loadRequest = 0;
 	Myadapter adapter;
 	Button logout;
 	MyJavaScriptInterface javeScritpInterfacee;
 	ListView lv;
 	public static TextView allallergy,titletext;
 	ImageView image;
	 RelativeLayout rlayout,titlerlayout;
 	 
 	  
 	class Myadapter extends BaseAdapter 
 	{
 
 
 		 Context context;
 		   public Myadapter(Context c) {
 		// TODO Auto-generated constructor stub
 			  this.context =c;
 	        }
 	 	
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return javeScritpInterfacee.careplanRecords.size();
 			
 		}
 
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public long getItemId(int position) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 		
 
 
 		@Override
 		public View getView(int position, View v, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			
 
 			
 			
 			
 			LayoutInflater li = getLayoutInflater();
 			v = li.inflate(R.layout.careplandataview, null);
 			 TextView t1 = (TextView)v.findViewById(R.id.cpdatatextView1);
 			 TextView t2 = (TextView)v.findViewById(R.id.cpdatatextView2);
 			 TextView t3 = (TextView)v.findViewById(R.id.cpdatatextView3);
 			 
 			 
 			
 			 
 			 String[] careplan = javeScritpInterfacee.careplanRecords.get(position);
 			
 			 if (position % 2 == 0) {
 				 
 					  v.setBackgroundColor(0x30E6EEF8);
 					}
 			 
 			 if(careplan[1].equals("undefined"))
 			 {
 				 careplan[1] = " ";
 			 }
 			 if(careplan[2].equals("undefined"))
 			 {
 				 careplan[2] = " ";
 			 }
 			 if(careplan[3].equals("undefined"))
 			 {
 				 careplan[3] = " ";
 			 }
 			 if(careplan[4].equals("undefined"))
 			 {
 				 careplan[4] = " ";
 			 }
 			 if(careplan[5].equals("undefined"))
 			 {
 				 careplan[5] = " ";
 			 }
 			 if(careplan[6].equals("undefined"))
 			 {
 				 careplan[6] = " ";
 			 }
 			 if(careplan[7].equals("undefined"))
 			 {
 				 careplan[7] = " ";
 			 }
 			 
 			 
 			 if(careplan[2].equals("Inactive"))
 			 {
 				 t1.setTextColor(0x30141823);
 				 t2.setTextColor(0x30141823);
 				 t3.setTextColor(0x30141823);
 				 
 				 
 			 }
			 
 			 
 			 t1.setText(careplan[1]);
 			 t2.setText(careplan[6]);
 			 t3.setText(careplan[7]);
 			 Log.d("Data", careplan[6]);
 			 return v;
 		}
 		
 		
 		
 
 	}
 	
 	protected void onPause()
 	   {
 	       super.onPause();
 	       System.gc();
 	   }
 	  
 	  
 	  protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
 		setContentView(R.layout.careplanhome);
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		titlerlayout =(RelativeLayout) findViewById(R.id.RelativeLayout1);
 		 titletext = (TextView) findViewById(R.id.titletextview);
 		 logout = (Button) findViewById(R.id.logoutbutton);
 		 logout.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(getApplicationContext(), HOMESCREEN.class);
 		       // intent.putExtra("finish", true);
 		        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
 		        startActivity(intent);
 		       finish();
 			}
 		});
 		  
 		  
 		 loadRequest = 0;
 		String usern = getIntent().getExtras().getString("username");
  	   String passw = getIntent().getExtras().getString("password"); 
  	  final Context myApp = this;
  	  
  	 
  	   
  	   	wb1 = (WebView)findViewById(R.id.webView1);
  	   	wb1.getSettings().setJavaScriptEnabled(true); 
  	   	wb1.getSettings().setBuiltInZoomControls(true);
 		wb1.setInitialScale(1);
 		wb1.getSettings().setLoadWithOverviewMode(true);
 		wb1.getSettings().setUseWideViewPort(true);
 		wb1.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
 		wb1.setScrollbarFadingEnabled(false);
 		
 		progressBar = ProgressDialog.show(UIFORCAREPLAN.this, "YDP page is ", "Loading...");
 
  	  
  	  /* Register a new JavaScript interface called HTMLOUT */  
 		javeScritpInterfacee = new MyJavaScriptInterface();
 		javeScritpInterfacee.careplanRecords = new ArrayList<String[]>();
 	 	javeScritpInterfacee.allergyRecords = new ArrayList<String[]>();  
 	 	
 		javeScritpInterfacee.uiforcareplan = UIFORCAREPLAN.this;
  	  wb1.addJavascriptInterface(javeScritpInterfacee, "HTMLOUT");  
  	    
  	   
 
 	wb1.loadUrl("https://yourdoctorprogram.com/qhr/Login.aspx/");
 	
 	
 	/* WebViewClient must be set BEFORE calling loadUrl! */ 
 	wb1.setWebViewClient(new WebViewClient() {
 
 		    
 		   @Override
 		   public void onPageFinished(WebView view, String url) {
 			   Log.d("page loaded","page not loaded");
 			   super.onPageFinished(view, url);
 		    	 if(loadRequest == 0){    
 		    		 
 		    	   Log.d("page loaded","pageloaded");
 		    	   
 		    	   String user = getIntent().getExtras().getString("username");
 		    	   String pass = getIntent().getExtras().getString("password");
 
 		    	   wb1.loadUrl("javascript:document.getElementById('ContentPlaceHolder_ContentPlaceHolder1_UserName').value='"+user+"';");
 		    	   wb1.loadUrl("javascript:document.getElementById('ContentPlaceHolder_ContentPlaceHolder1_txt_Password').value='"+pass+"';");
 		    	   wb1.loadUrl("javascript:document.getElementById('ContentPlaceHolder_ContentPlaceHolder1_bt_Login').click();");
 
 		    	   
 		    	   loadRequest++;
 
 		    	   
 		    	   
 		    	 }
 		    	 else if(loadRequest == 1)
 		    	   {
 					
 		    	    loadRequest++;
 		    	    wb1.loadUrl("javascript:window.HTMLOUT.setUserName(document.getElementById('TitleContent_TitleContent_lblProviderName').childNodes[0].textContent);");
 		    	    //wb1.loadUrl("javascript:window.HTMLOUT.showCareplaneHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
 		    	    
 		    	    //wb1.loadUrl("javascript:window.HTMLOUT.showCareplaneHTML('<head>'+document.getElementById('ContentPlaceHolder_MainContent_MainContent_CarePlanGridView').innerHTML+'</head>');");
 		    	    wb1.loadUrl("javascript:( function () {var carePlanTable = document.getElementById('ContentPlaceHolder_MainContent_MainContent_CarePlanGridView');var result = \"NO\";if(carePlanTable){result = \"\";var carePlanTable = document.getElementById('ContentPlaceHolder_MainContent_MainContent_CarePlanGridView');if(carePlanTable){var carePlan = carePlanTable.getElementsByTagName('tr');for(var i = 1; i < carePlan.length; i++){var carePlanRow = carePlan[i].getElementsByTagName('td');for(var j = 0; j < carePlanRow.length; j++){var childValue = carePlanRow[j].childNodes[1].textContent;if(childValue){result += childValue;result += \"$#\";}else{result += carePlanRow[j].childNodes[1].src;result += \"$#\";}}}}}window.HTMLOUT.showCareplaneHTML( result);}) ()");
 		    	    
 		    	     //wb1.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
 		    	     //wb1.loadUrl("javascript:window.HTMLOUT.setUserName(document.getElementsByTagName('html')[0].innerHTML;);"); 
 		    	     //wb1.loadUrl("javascript:window.HTMLOUT.setCareplan(function getCarePlan(){var carePlanTable = document.getElementById('ContentPlaceHolder_MainContent_MainContent_CarePlanGridView');var result = 'NO'; if(carePlanTable){ result = ''; var carePlanTable = document.getElementById('ContentPlaceHolder_MainContent_MainContent_CarePlanGridView');if(carePlanTable){ var carePlan = carePlanTable.getElementsByTagName('tr');for(var i = 1; i < carePlan.length; i++){var carePlanRow = carePlan[i].getElementsByTagName('td');for(var j = 0; j < carePlanRow.length; j++){var childValue = carePlanRow[j].childNodes[1].textContent;if(childValue){result += childValue;result += ':$#';console.log(childValue);}else{result += carePlanRow[j].childNodes[1].src;result += ':$#';console.log(carePlanRow[j].childNodes[1].src);}}}}}return result;}getCarePlan();");
 		    	     //wb1.loadUrl("javascript:window.HTMLOUT.setCareplan(document.getElementById('ContentPlaceHolder_MainContent_MainContent_CarePlanGridView')[0].innerHTML");
 		    	    wb1.loadUrl("https://yourdoctorprogram.com/qhr/CareDashboard/AllergiesEditorMaster.aspx");
 			       
 		            
 		           }
 		    	 
 		    	
 		         else if(loadRequest == 2){
 		          //String jScript = "function getAllergies(){ var allergiesTable = document.getElementById('ContentPlaceHolder_MainContent_MainContent_DataList1'); alert(1); var allergy = allergiesTable.getElementsByTagName('tr');var result = \"\";for(var i = 1; i < allergy.length; i +=3){ var allergyRow = allergy[i].getElementsByTagName('td');for(var j = 0; j < 4; j++){var childValue = allergyRow[j].childNodes[1].textContent;result += childValue;result += \":$#\";}}return result;}";
 //		          //wb1.loadUrl("javascript:window.HTMLOUT.showAllergiesHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
 //		          //wb1.loadUrl("javascript:window.HTMLOUT.showAllergiesHTML('<head>'+document.getElementById('ContentPlaceHolder_MainContent_MainContent_DataList1').innerHTML+'</head>');");
 //		          //wb1.loadUrl("javascript:window.HTMLOUT.showAllergiesHTML('<head>'"+jScript+"'</head>');");
 //		          //wb1.loadUrl("javascript:("+jScript +")()");
 	         
 		          progressBar.dismiss();
		          titlerlayout.setVisibility(RelativeLayout.VISIBLE);
 		         rlayout.setVisibility(RelativeLayout.VISIBLE);
 		          
 	          wb1.loadUrl("javascript:( function () { var allergiesTable = document.getElementById('ContentPlaceHolder_MainContent_MainContent_DataList1'); var allergy = allergiesTable.getElementsByTagName('tr');var result = \"\";for(var i = 1; i < allergy.length; i +=3){ var allergyRow = allergy[i].getElementsByTagName('td');for(var j = 0; j < 4; j++){var childValue = allergyRow[j].childNodes[1].textContent;result += childValue;result += \"$#\";}}window.HTMLOUT.showAllergiesHTML(result);} ) ()");
 		          loadRequest++;
 		          
 		          		     
 		          adapter.notifyDataSetChanged();
 			         titletext.setText(javeScritpInterfacee.userName + "'s Careplan");  
 			         showAllallergy();
 		            	}
 		    	 
 		    	 
 //		    	 adapter.notifyDataSetChanged();
 //		    	   allallergy.setText(allergyvar); 
 
 		    		    	  
 		   }
 		   
 		});
 	
 
 	
  	    
  	    lv = (ListView) findViewById(R.id.cphomelistView);
 
 		adapter = new Myadapter(this);
 		lv.setAdapter(adapter);
         lv.setOnItemClickListener(this);
         allallergy = (TextView)findViewById(R.id.cphometextView2);
         rlayout = (RelativeLayout) findViewById(R.id.cprelativelayout);
         allallergy.setText("this is my text");
         ImageButton img = (ImageButton) findViewById(R.id.imageButton1);
 		img.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 			
 				Intent allergiesintent = new Intent(getApplicationContext(),UIFORALLERGESDETAILVIEW.class);
 				
 //			image =(ImageView) findViewById(R.id.imageView1);
 //			image.setImageBitmap(null);
 			
 				
 			Arraydatasample obj = new Arraydatasample();
 				obj.arrayobj = javeScritpInterfacee.allergyRecords;
 				allergiesintent.putExtra("sampleObject", obj);				
 				allergiesintent.putExtra("userid", javeScritpInterfacee.userName);
 				//allergiesintent.putExtras(b);
 				startActivity(allergiesintent);
 				
 			}
 		});
 		
 	}
 		
 	 public void showAllallergy(){
 
 			  try {
 				Thread.sleep(4000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 	      allallergy.setText(javeScritpInterfacee.allergyvar);
 	  }
 	  
 	  void showCareplane(){
 		  
 	  }
 		
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View v, int position,
 				long arg3) {
 			// TODO Auto-generated method stub
 			
 			
 			
 			Intent careplandetailactivity = new Intent(getApplicationContext(),Careplandetailview.class);
 			//Toast.makeText(getApplicationContext(), position, 3000).show();
 			String[] careplan = javeScritpInterfacee.careplanRecords.get(position);
 			careplandetailactivity.putExtra("careplanrecord",careplan);
 			careplandetailactivity.putExtra("userid", javeScritpInterfacee.userName);
 			startActivity(careplandetailactivity);
 			
 			
 		}
 		
 
 	}	    
 
 
 	
 
 
