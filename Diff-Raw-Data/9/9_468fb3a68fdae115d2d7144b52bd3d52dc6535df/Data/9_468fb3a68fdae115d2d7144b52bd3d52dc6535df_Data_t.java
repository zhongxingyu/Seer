 package com.abstracttech.ichiban.data;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.abstracttech.ichiban.R;
import com.abstracttech.ichiban.views.graphs.GraphType;
 
 import android.content.res.Resources;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.View;
 
 /**
  * @author andraz
  *
  */
 public class Data {
 	public static final float _MAX_RPM = 40f;
 	public static final float _SPEED_TO_RPM_RATIO=4f;
 	public static final float _MAX_SPEED = _MAX_RPM * _SPEED_TO_RPM_RATIO;
 	private static ArrayList<String> data=null;
 	private static int index=0; //current data line
 	private static int rate; //internal update rate
 
 	private static Timer timer; //timer for running updates
 	private static long started,stopped; //time, for calculating elapsed time
 	private static boolean isAutoupdating=false;
 	private static List<View> clients = new ArrayList<View>(); //subscribers
 
 	private static float x,y,z, rpm, turnRatio; //current data line, updates on specified interval
 	private static float locX, locY, locZ, locRpm, locTurn; //real time data line
 
 	//statistic data
 	public static SpeedData speedData=new SpeedData();
 	public static AccData accData=new AccData();
 	public static PathData pathData=new PathData();
 
 	public static Vibrate vibrator;
 
 	//current data line from bluetooth
 	private static String btLine=null;
 

 	/**graph indexes are as follows
 	 * center screen: 0
 	 *right screen
 	 * up left: 1, up right 2, botom 3
 	 */
 	public static GraphType[] graphs= { GraphType.SPEED, GraphType.ACCELERATION, GraphType.SPEED, GraphType.PATH };
	public static int[] graphID = {R.id.graf0, R.id.grafPospesek,R.id.grafHitrost, R.id.grafPot};
 
 	/**
 	 * load local data
 	 * @param res system resources, use getResources()
 	 * @throws IOException
 	 */
 	public static void loadCSV(Resources res) throws IOException {
 		InputStream inputStream = res.openRawResource(R.raw.data);
 		rate=300;
 		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
 		data=new ArrayList<String>();
 
 		String line;
 		while ((line = reader.readLine()) != null) {
 			data.add(line);
 		}
 	}
 
 	/**
 	 * load local data
 	 * @param res system resources, use getResources()
 	 * @throws IOException
 	 */
 	public static void loadDenseCSV(Resources res) throws IOException {
 		InputStream inputStream = res.openRawResource(R.raw.data35);
 		rate=35;
 		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
 		data=new ArrayList<String>();
 
 		String line;
 		while ((line = reader.readLine()) != null) {
 			data.add(line);
 		}
 	}
 
 	public static void destroyData()
 	{
 		data=null;
 	}
 
 	public static boolean hasLocalData()
 	{
 		return data!=null;
 	}
 
 	/**
 	 * fetches new line of data
 	 * @return new line of data
 	 */
 	private static String getNextLine()
 	{
 		int size=data.size();
 		if(size==0)
 			return null;
 		int retIndex=index;
 		if((index+1)<size)
 			index++;
 		else
 			index=0;
 		return data.get(retIndex);
 	}
 
 	/**
 	 * load next line of data
 	 */
 	private static void updateInternal()
 	{
 		try {
 			String line=null;
 
 			//if there is local data, load it, else use bluetooth
 			if(data!=null)
 				line=getNextLine();
 			else
 				line=btLine;
 
 			String[] values=line.split(",");	
 			locX=Float.parseFloat(values[0]);
 			locY=Float.parseFloat(values[1]);
 			locZ=Float.parseFloat(values[2]); 
 			locRpm=Float.parseFloat(values[3]);
 			locTurn=Float.parseFloat(values[4]);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Log.e("ICHIBAN", "something wrong with data");
 		}
 	}
 
 	/**
 	 * update cycle
 	 * updates internal data
 	 * and calls update functions for some elements
 	 */
 	public static void update()
 	{
 		x=locX;
 		y=locY;
 		z=locZ;
 		rpm=locRpm;
 		turnRatio=locTurn;
 
 		speedData.update();
 		accData.update();
 		pathData.update();
 
 		//notify clients
 		for(View v : clients)
 			v.postInvalidate();
 
 				//update vibrations
 				vibrator.update();
 	}
 
 	/**
 	 * update via bluetooth
 	 * @param line data line retrieved
 	 */
 	public static void btUpdate(String line)
 	{
 		btLine=line;
 		updateInternal();
 		update();
 	}
 
 	/**
 	 * automaticaly update data in specified period
 	 * @param interval in miliseconds
 	 */
 	public static void startAutoupdate(int period)
 	{
 		if(isAutoupdating)
 			return;
 
 		timer = new Timer();
 
 		//realtime internal updates
 		timer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				updateInternal(); 
 			}
 		}, 0, rate);
 
 		//public updates
 		timer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				update(); 
 			}
 		}, 0, period);
 		isAutoupdating=true;
 
 		//for calculating running time
 		if(started==0)
 			started=System.currentTimeMillis();
 		else
 			started+=System.currentTimeMillis()-stopped;
 	}
 
 	/** stops automatic self-updates
 	 * 
 	 */
 	public static void stopAutoupdate()
 	{
 		if(timer!=null)
 			timer.cancel();
 		isAutoupdating=false;
 		stopped=System.currentTimeMillis();
 	}
 
 	/**
 	 * Invalidates subscribed view on data change.
 	 * all customs views that show data are subscribed to this
 	 * @param v view to be invalidated
 	 */
 	public static void subscribe(View v){
 		clients.add(v);
 	}
 
 	public static float getX() {
 		return x;
 	}
 
 	public static float getY() {
 		return y;
 	}
 
 	public static float getZ() {
 		return z;
 	}
 
 	public static float getRpm() {
 		return rpm;
 	}
 
 	public static float getTurnRatio() {
 		return turnRatio;
 	}
 
 	public static float getXPercentage() {
 		return x;
 	}
 
 	public static float getYPercentage() {
 		return y;
 	}
 
 	public static float getZPercentage() {
 		return z;
 	}
 
 	public static float getRpmPercentage() {
 		return rpm / _MAX_RPM;
 	}
 
 	public static float getTurnRatioPercentage() {
 		return (turnRatio - 5) / 10.f;
 	}
 
 	/**
 	 * @return started time in milicseconds
 	 */
 	public static long getStarted() {
 		return started;
 	}
 
 	/**
 	 * @return running time in milicseconds
 	 */
 	public static long getRunningTime() {
 		return System.currentTimeMillis()-started;
 	}
 
 	public static boolean isAutoupdating(){
 		return isAutoupdating;
 	}
 	
 	/**
 	 * @return internal update rate in miliseconds
 	 */
 	public static int getRate(){
 		return rate;
 	}
 }
