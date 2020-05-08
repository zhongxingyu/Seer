 package sk.ayazi.glstnzastupovanie;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 import android.support.v7.app.ActionBarActivity;
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Build;
 
 @SuppressLint("SimpleDateFormat")
 public class Zastupovanie extends ActionBarActivity {
 	private final Zastup z=new Zastup();
 	private final int[] cols={R.id.row1_1,
 			 R.id.row1_2,
 			 R.id.row1_3,
 			 R.id.row1_4,
 			 R.id.row1_5,
 			 R.id.row1_6,
 			 R.id.row2_1,
 			 R.id.row2_2,
 			 R.id.row2_3,
 			 R.id.row2_4,
 			 R.id.row2_5,
 			 R.id.row2_6,
 			 R.id.row3_1,
 			 R.id.row3_2,
 			 R.id.row3_3,
 			 R.id.row3_4,
 			 R.id.row3_5,
 			 R.id.row3_6,
 			 R.id.row4_1,
 			 R.id.row4_2,
 			 R.id.row4_3,
 			 R.id.row4_4,
 			 R.id.row4_5,
 			 R.id.row4_6,
 			 R.id.row5_1,
 			 R.id.row5_2,
 			 R.id.row5_3,
 			 R.id.row5_4,
 			 R.id.row5_5,
 			 R.id.row5_6,
 			 R.id.row6_1,
 			 R.id.row6_2,
 			 R.id.row6_3,
 			 R.id.row6_4,
 			 R.id.row6_5,
 			 R.id.row6_6 };
 	private final int[] seps={R.id.sep1,R.id.sep2,R.id.sep3,R.id.sep4,R.id.sep5,R.id.sep6};
 	private final int[] rows={R.id.tableRow1,R.id.tableRow2,R.id.tableRow3,R.id.tableRow4,R.id.tableRow5,R.id.tableRow6};
 	public String trieda;
 	private Date datum;
 	AsyncTask<?, ?, ?> at;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.activity_zastupovanie);
 		Intent i=getIntent();
 		trieda=i.getStringExtra(MainActivity.TRIEDA);
 		getApplicationContext().getSharedPreferences("sk.ayazi.glstnzastupovanie", Context.MODE_PRIVATE).edit().putString("sk.ayazi.glstnzastupovanie.trieda",i.getStringExtra(MainActivity.TRIEDA)).commit();
 		getApplicationContext().getSharedPreferences("sk.ayazi.glstnzastupovanie", Context.MODE_PRIVATE).edit().putString("sk.ayazi.glstnzastupovanie.datum",i.getStringExtra(MainActivity.DATUM)).commit();
 		if(i.getStringExtra(MainActivity.DATUM).equals("zajtra")){
 			at=new GetNext();
 			((GetNext) at).execute();
 		}else if(i.getStringExtra(MainActivity.DATUM).equals("latest")){
 			at=new GetLatest();
 			((GetLatest) at).execute();
 		}else{fill(i.getStringExtra(MainActivity.DATUM),trieda);}
 		// Show the Up button in the action bar.
 		setupActionBar();
 	}
 	
 	private void fill(final String date, final String trieda){
 		   	at=new GetZast();
 			((GetZast)at).execute(date,trieda);		
 	}
 	
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.zastupovanie, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int itemId = item.getItemId();
 		if (itemId == android.R.id.home) {
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		else if(itemId==R.id.action_obed){
 			if(at!=null){at.cancel(true);}
 			openObed();
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	private void openObed(){
 		Intent i=new Intent(this,ObedActivity.class);
 		if(datum!=null){
 			i.putExtra("date", datum);
 			
 		}
 		else if(at instanceof GetZast){
 			try {//hack to get current date with 0 time
				i.putExtra("date", new SimpleDateFormat("dMMyyyy").parse(new SimpleDateFormat("dMMyyyy").format(new Date())));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}//dnesny obed
 		else{i.putExtra("date",z.getNextDay());}
 		startActivity(i);
 	}
 	
 	boolean isNetworkAvailable() {
 	    ConnectivityManager connectivityManager 
 	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 	    return activeNetworkInfo != null;
 	}
 	
 	
 	
 	private class GetZast extends AsyncTask<String,Void,Iterator<String[]>>{
 		@Override
 		protected void onPreExecute (){
 			 setProgressBarIndeterminateVisibility(true);
 		}
 		private String date;
 		private Iterator<String[]> ntait=new ArrayList<String[]>().iterator();
 		@SuppressLint("SimpleDateFormat")
 		@Override
 		protected Iterator<String[]> doInBackground(String... arg) {
 			try {
 				date=arg[0];
 				datum=new SimpleDateFormat("yyyyMMdd").parse(date);
 				if(!isNetworkAvailable()){
 					return ntait;
 				}
 				z.load(arg[0]);
 				ArrayList<String[]> ar=z.getTable(arg[1]);
 				Iterator<String[]> it=ar.iterator();
 				return it;
 			} catch (IOException e) {
 				// handled in onPostExecuted, by it==null
 				return null;
 			} catch (ParseException e) {
 				e.printStackTrace();
 				return null;
 			}
 			
 		}
 		protected void onPostExecute (Iterator<String[]> it){
 			if(it==null){
 				setProgressBarIndeterminateVisibility(false);
 				new AlertDialog.Builder(Zastupovanie.this)
 				.setTitle("Chyba")
 				.setMessage("Zastupovanie nenajdene")
 				.setPositiveButton("Sp", new DialogInterface.OnClickListener() {
 
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	  	startActivity(new Intent(Zastupovanie.this,MainActivity.class));
 				    }
 				})
 				.setNeutralButton("Obed", new DialogInterface.OnClickListener() {
 
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	openObed();
 				    }
 				}).create()
 				.show()
 				;
 				return;
 			}
 			if(it==ntait){
 				//network not available
 				setProgressBarIndeterminateVisibility(false);
 				new AlertDialog.Builder(Zastupovanie.this)
 				.setTitle("Chyba")
 				.setMessage("Pripojenie nie je dostupné")
 				.setPositiveButton("Späť", new DialogInterface.OnClickListener() {
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	startActivity(new Intent(Zastupovanie.this,MainActivity.class));
 				    }
 				}).setNeutralButton("Obed", new DialogInterface.OnClickListener() {
 
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	openObed();
 				    }
 				}).create()
 				.show();
 				return;
 			}
 			TextView tv;
 			String[] r;
 			int i=0;
 			if(!it.hasNext()){
 				//zobraz ziadne relevantne zaznamy
 				String[] zrz={"Žiadne relevantné záznamy","","","","","",""};
 				ArrayList<String[]> ar=new ArrayList<String[]>();
 				ar.add(zrz);
 				it=ar.iterator();
 			}
 			TableLayout tl=(TableLayout) findViewById(R.id.tl);
 			while(i++<6){
 				if(it.hasNext()){
 					r=it.next();
 					for(int k=0;k<6;k++){
 						tv=(TextView) findViewById(cols[i*6+k-6]);
 						tv.setText(r[k]);
 						tv.setGravity(Gravity.CENTER);
 					}
 				}else {
 					tl.removeView(findViewById(rows[i-1]));
 					if(i!=6) tl.removeView(findViewById(seps[i-1]));
 				}
 			}
 			//oznam
 			tv=(TextView) findViewById(R.id.oznam);
 			tv.setText(z.getOznam());
 			//title
 			try {
 				datum=new SimpleDateFormat("d.M.yyyy").parse(date.substring(6)+"."+date.substring(4,6)+"."+date.substring(0,4));
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				
 			}
 			setTitle("Zastupovanie " + date.substring(6)+"."+date.substring(4,6)+"."+date.substring(0,4));
 			//progressbar
 			setProgressBarIndeterminateVisibility(false);
 		}
 	}
 	
 	private class GetNext extends AsyncTask<Void,Void,String>{
 		@Override
 		protected void onPreExecute (){
 			 setProgressBarIndeterminateVisibility(true);
 		}
 		private String NA="NA";
 		
 		@Override
 		protected String doInBackground(Void... param) {
 			
 			try {
 				if(!isNetworkAvailable()){
 					return NA;
 				}
 				return z.getNextAvailable();
 				} catch (IOException e) {
 				// handled in onPostExecuted, by it==null
 				return null;
 			}
 			
 		}
 		protected void onPostExecute (String res){
 			if(res==null){
 				setProgressBarIndeterminateVisibility(false);
 				new AlertDialog.Builder(Zastupovanie.this)
 				.setTitle("Chyba")
 				.setMessage("Vyskytla sa chyba")
 				.setPositiveButton("Späť", new DialogInterface.OnClickListener() {
 
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	  	startActivity(new Intent(Zastupovanie.this,MainActivity.class));
 				    }
 				})
 				.setNeutralButton("Obed", new DialogInterface.OnClickListener() {
 
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	openObed();
 				    }
 				}).create()
 				.show()
 				;
 				return;
 			}
 			if(res==NA){
 				
 				setProgressBarIndeterminateVisibility(false);
 				new AlertDialog.Builder(Zastupovanie.this)
 				.setTitle("Chyba")
 				.setMessage("Pripojenie nie je dostupne")
 				.setPositiveButton("Späť", new DialogInterface.OnClickListener() {
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	startActivity(new Intent(Zastupovanie.this,MainActivity.class));
 				    }
 				}).setNeutralButton("Obed", new DialogInterface.OnClickListener() {
 
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	openObed();
 				    }
 				})
 				.create()
 				.show();
 				return;
 			}
 			Date d=new Date(System.currentTimeMillis());
 			Calendar c=Calendar.getInstance();
 			c.setTime(d);
 			String month=String.valueOf(
 					(c.get(Calendar.MONTH)+1)<10?("0"+(c.get(Calendar.MONTH)+1)):
 				(c.get(Calendar.MONTH)+1));
 			String day=String.valueOf(
 					(c.get(Calendar.DATE))<10?("0"+(c.get(Calendar.DATE))):
 				(c.get(Calendar.DATE)));
 	    	String da= ""+c.get(Calendar.YEAR)+month+day;
 	    	if(da.equals(res)){
 	    		new AlertDialog.Builder(Zastupovanie.this)
 				.setTitle("Nedostupné")
 				.setMessage("Novšie zastupovanie neexistuje")
 				.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	  	startActivity(new Intent(Zastupovanie.this,MainActivity.class));
 				    }
 				})
 				.create()
 				.show()
 				;
 	    		setProgressBarIndeterminateVisibility(false);
 				return;
 	    	}else{
 	    		fill(res,trieda);
 	    	}
 	    	
 			
 		}
 		
 	}
 	
 	private class GetLatest extends AsyncTask<Void,Void,String>{
 		@Override
 		protected void onPreExecute (){
 			 setProgressBarIndeterminateVisibility(true);
 		}
 		private String NA="NA";
 		
 		@Override
 		protected String doInBackground(Void... param) {
 			
 			try {
 				if(!isNetworkAvailable()){
 					return NA;
 				}
 				return z.getLatest();
 				} catch (IOException e) {
 				// handled in onPostExecuted, by it==null
 				return null;
 			}
 			
 		}
 		protected void onPostExecute (String res){
 			if(isCancelled()){return;}
 			if(res==null){
 				setProgressBarIndeterminateVisibility(false);
 				new AlertDialog.Builder(Zastupovanie.this)
 				.setTitle("Chyba")
 				.setMessage("Vyskytla sa chyba")
 				.setPositiveButton("Späť", new DialogInterface.OnClickListener() {
 
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	  	startActivity(new Intent(Zastupovanie.this,MainActivity.class));
 				    }
 				}).setNeutralButton("Obed", new DialogInterface.OnClickListener() {
 
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	openObed();
 				    }
 				})
 				.create()
 				.show()
 				;
 				return;
 			}
 			if(res==NA){
 				
 				setProgressBarIndeterminateVisibility(false);
 				new AlertDialog.Builder(Zastupovanie.this)
 				.setTitle("Chyba")
 				.setMessage("Pripojenie nie je dostupne")
 				.setPositiveButton("Späť", new DialogInterface.OnClickListener() {
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	startActivity(new Intent(Zastupovanie.this,MainActivity.class));
 				    }
 				}).setNeutralButton("Obed", new DialogInterface.OnClickListener() {
 
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	openObed();
 				    }
 				})
 				.create()
 				.show();
 				return;
 			}
 			Date d=new Date(System.currentTimeMillis());
 			Calendar c=Calendar.getInstance();
 			c.setTime(d);
 			String month=String.valueOf(
 					(c.get(Calendar.MONTH)+1)<10?("0"+(c.get(Calendar.MONTH)+1)):
 				(c.get(Calendar.MONTH)+1));
 			String day=String.valueOf(
 					(c.get(Calendar.DATE))<10?("0"+(c.get(Calendar.DATE))):
 				(c.get(Calendar.DATE)));
 	    	String da= ""+c.get(Calendar.YEAR)+month+day;
 	    	if(da.equals(res)){
 	    		new AlertDialog.Builder(Zastupovanie.this)
 				.setTitle("Nedostupné")
 				.setMessage("Novšie zastupovanie neexistuje")
 				.setPositiveButton("Close", new DialogInterface.OnClickListener() {
 
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				    	  	startActivity(new Intent(Zastupovanie.this,MainActivity.class));
 				    }
 				})
 				.create()
 				.show()
 				;
 	    		setProgressBarIndeterminateVisibility(false);
 				return;
 	    	}else{
 	    		fill(res,trieda);
 	    	}
 	    	
 			
 		}
 		
 	}
 }
