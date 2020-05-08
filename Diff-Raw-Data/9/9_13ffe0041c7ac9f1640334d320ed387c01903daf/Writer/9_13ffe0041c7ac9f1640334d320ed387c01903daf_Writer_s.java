 package de.lmu.ifi.dbs.sendsor;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Calendar;
 
 import android.content.Context;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Klasse zum Schreiben der Accelerometerdaten
  * @author walonka
  * @version 0.5
  *
  */
 public class Writer{
 	private static final String TAG = "SendsorActivity";
 	private Context context;
 	private BufferedWriter out;
 	private File filepath = Environment.getExternalStorageDirectory();
 	private String filename;
 	private String activity="";
 	final static String lineSeparator = System.getProperty("line.separator");
 	
 	/**
 	 * Konstruktor der Writerklasse
 	 * @param filename Dateiname (wo soll die Datenbank gespeichert werden?)
 	 */
 	public Writer (String filename){
 		this.context=SendsorActivity.getContext();
 		this.filename=filename;
 		
 		boolean initializised = false;
 		try{
 			BufferedReader reader = new BufferedReader(new FileReader(new File(filepath, filename)));
 			if (reader.readLine()!=null){
 				initializised=true;
 			}
 			else{
 			}
 			reader.close();
 		}
 		catch(Exception e){
 			Log.v(TAG, e.getMessage());
 		}
 		try {
 			out = new BufferedWriter(new FileWriter(new File(filepath, filename),true),768);
 			Log.v(TAG, "Datenschreiber gestartet");
 			
 		} catch (IOException e) {
 			Log.v(TAG, e.getMessage());
 		}
 		Toast.makeText(SendsorActivity.getContext(), "Writer gestartet", 5).show();
 
 	}
 	
 	/**
 	 * Methode zum Schreiben von Accelerometerwerten
 	 * @param x X-Beschleunigung
 	 * @param y Y-Beschleunigung
 	 * @param z Z-Beschleunigung
 	 */
 	public void writeData(float x, float y, float z){
 		try {
 			
 			x/=9.81;
 			x*=64;
 			y/=9.81;
 			y*=64;
 			z/=9.81;
 			z*=64;
 			Calendar c = Calendar.getInstance();
 			/*
 			long ms = System.currentTimeMillis();
 			String milli = Long.toString(ms).substring(10);
 			Date d = new Date(ms);
 			//String.format
 			//out.write(System.currentTimeMillis()+";"+x+";"+y+";"+z+lineSeparator);*/
 			//out.write(d.getYear()+"-"+d.getMonth()+"-"+d.getDay()+" "+d.getHours()+":"+d.getMinutes()+":"+d.getSeconds()+"."+milli+","+(int)x+","+(int)y+","+(int)z+lineSeparator);
			out.write(c.get(Calendar.YEAR)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.DATE)+" "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+"."+c.get(Calendar.MILLISECOND)+","+(int)x+","+(int)y+","+(int)z+","+activity+lineSeparator);
 			//out.write(System.currentTimeMillis()+","+(int)x+","+(int)y+","+(int)z+","+activity+lineSeparator);
 		} catch (IOException e) {
 			Log.v(TAG, e.getMessage());
 		}
 	}
 	
 	/**
 	 * Methode zum kompletten Schreiben aller noch im Puffer befindlichen Daten
 	 */
 	public void flushout(){
 		try {
 			out.flush();
 			Log.v(TAG, "Puffer geschrieben");
 		} catch (IOException e) {
 			Log.v(TAG, e.getMessage());
 		}
 	}
 	
 	public void stopWriting(){
 		try {
 			out.flush();
 			out.close();
 			Singleton.killWriter();
 			Toast.makeText(SendsorActivity.getContext(), "Writer gestopt", 5).show();
 		} catch (IOException e) {
 			Log.v(TAG, e.getMessage());
 		}
 	}
 	
 	public void setActivity(String activity){
 		this.activity=activity;
 	}
 }
