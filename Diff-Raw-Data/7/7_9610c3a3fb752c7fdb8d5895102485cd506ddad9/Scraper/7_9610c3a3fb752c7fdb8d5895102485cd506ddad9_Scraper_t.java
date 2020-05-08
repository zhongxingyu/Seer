 package com.mridang.moko.asynctasks;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.mridang.moko.R;
 import com.mridang.moko.Trend;
 import com.mridang.moko.adapters.TrendingTorrentsAdapter;
 import com.mridang.moko.animations.ExpandAnimation;
 import com.mridang.moko.scrapers.Fenopy;
 import com.mridang.moko.scrapers.Kickass;
 import com.mridang.moko.structures.Torrent;
 
 /*
  * This is the class used to list the torrents.
  * It is used a asynchronous activity so that it does not hold
  * up the main UI thread.
  */
 public class Scraper extends AsyncTask<String, Integer, ArrayList<Torrent>> {
 
 	/*
 	 * The instance of the calling class
 	 */
     private Trend objTrend = null;
     /*
      * The instance of the preferences class to fetch preferences
      */
     private SharedPreferences shpSettings = null;
     /*
      * The previous toolbar that was vewPrevious
      */
     private View vewPrevious;
     /*
      * The instance of the annimator that animates the toolbar
      */
     private ExpandAnimation objExpander;
     /*
      * The time taken to execute
      */
     private Long lngTiming;
 
 	/*
 	 * @see android.os.AsyncTask#doInBackground(Params[])
 	 */
     @SuppressWarnings("unchecked")
 	@Override
     protected ArrayList<Torrent> doInBackground(String... strQuery) {
 
         // Let's load the data from a serialized format if it already exists
         try {
 
         	DateFormat dftFormat = new SimpleDateFormat("ddMMyyyy");
         	String strFilename = dftFormat.format(new Date());
 
             if(this.objTrend.getApplicationContext().getFileStreamPath(strFilename).exists()) {
 
                 FileInputStream fisSerialize = this.objTrend.getApplicationContext().openFileInput(strFilename);
                 ObjectInputStream oisSerialize = new ObjectInputStream(fisSerialize);
                 ArrayList<Torrent> lstTorrents = (ArrayList<Torrent>) oisSerialize.readObject();
                 oisSerialize.close();
                 fisSerialize.close();
 
                 return lstTorrents;
 
             }
 
         } catch (Exception e) {
             //Do nothing
         }
 
     	ArrayList<Torrent> objTorrents = new ArrayList<Torrent>();
     	ExecutorService esrExecutor = Executors.newFixedThreadPool(2);
     	Set<Callable<ArrayList<Torrent>>> setCallables = new HashSet<Callable<ArrayList<Torrent>>>();
 
		if (this.shpSettings.getBoolean("use_kickasstorrents", true)) {
 
 	    	setCallables.add(new Callable<ArrayList<Torrent>>() {
 
 	    	    public ArrayList<Torrent> call() throws Exception {
 
 	    	    	try {
 
 	    				Log.d("asynctasks.Scraper", "Scraping Kickass");
 	    				Kickass objKickass = new Kickass(Scraper.this.objTrend);
 	    				ArrayList<Torrent> objKickassTorrents = new ArrayList<Torrent>();
 	    				objKickassTorrents = objKickass.doScrape();
 	    				return objKickassTorrents;
 
 			    	} catch (Exception e) {
 			    		Log.w("asynctask.Scraper", "Error scraping from Kickass Torrents");
 			    		EasyTracker.getTracker().trackException(e.getMessage(), e, false);
                         //TODO: Show message to the user that we were unable to query this site.
 			    	}
 
 			    	return new ArrayList<Torrent>();
 
 	    	    }
 
 	    	});
 
 		}
		
		if (this.shpSettings.getBoolean("use_fenopytorrents", true)) {
 
 	    	setCallables.add(new Callable<ArrayList<Torrent>>() {
 
 	    	    public ArrayList<Torrent> call() throws Exception {
 
 	    	    	try {
 
 	    				Log.d("asynctasks.Fenopy", "Scraping Fenopy");
 	    				Fenopy objFenopy = new Fenopy(Scraper.this.objTrend);
 	    				ArrayList<Torrent> objFenopyTorrents = new ArrayList<Torrent>();
 	    				objFenopyTorrents = objFenopy.doScrape();
 	    				return objFenopyTorrents;
 
 			    	} catch (Exception e) {
 			    		Log.w("asynctask.Scraper", "Error scraping from Fenopy Europe");
 			    		EasyTracker.getTracker().trackException(e.getMessage(), e, false);
 			    		//TODO: Show message to the user that we were unable to query this site.
 			    	}
 
 			    	return new ArrayList<Torrent>();
 
 	    	    }
 
 	    	});
 
 		}
 
 		List<Future<ArrayList<Torrent>>> lstFutures;
 
 		try {
 
 			lstFutures = esrExecutor.invokeAll(setCallables);
 
 			for(Future<ArrayList<Torrent>> futFuture : lstFutures){
 
 				if (futFuture.get().isEmpty() == false)
 				    objTorrents.addAll(futFuture.get());
 
 			}
 
 		} catch (InterruptedException e) {
 
 			e.printStackTrace();
 
 		} catch (ExecutionException e) {
 
 			e.printStackTrace();
 
 		}
 
 		esrExecutor.shutdown();
 
         // Let's save the data in a serialized format if it doesn't exist
         try {
 
         	DateFormat dftFormat = new SimpleDateFormat("ddMMyyyy");
         	String strFilename = dftFormat.format(new Date());
 
             if(!this.objTrend.getApplicationContext().getFileStreamPath(strFilename).exists()) {
                 FileOutputStream fosSerialize = this.objTrend.getApplicationContext().openFileOutput(strFilename, Context.MODE_PRIVATE);
                 ObjectOutputStream oosSerialize = new ObjectOutputStream(fosSerialize);
                 oosSerialize.writeObject(objTorrents);
                 oosSerialize.close();
                 fosSerialize.close();
 
             }
 
         } catch (Exception e) {
             //Do nothing
         }
 
 		return objTorrents;
 
     }
 
     /*
      * @see android.os.AsyncTask#onPreExecute()
      */
     @Override
     protected void onPreExecute() {
 
     	this.objTrend.showProgress();
     	this.lngTiming = System.nanoTime();
 
     }
 
     /*
      * Initializes this task
      *
      * @param  objTrend    The instance of the calling Trend class
      * @param  enmCatergory  The category which should be scraped
      */
     public Scraper(Trend objContext) {
 
         this.objTrend = objContext;
         this.shpSettings = PreferenceManager.getDefaultSharedPreferences(objContext);
 
     }
 
     /*
      * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
      */
     @Override
     protected void onPostExecute(ArrayList<Torrent> objTorrents) {
 
     	this.objTrend.hideProgress();
     	this.lngTiming = System.nanoTime() - this.lngTiming;
 
     	EasyTracker.getTracker().trackTiming("AsyncTasks", this.lngTiming, "Scraper", "Get Trending Torrents");
 
     	if (objTorrents.size() == 0) {
 
 			Toast.makeText(this.objTrend,
 					R.string.unable_to_fetch, Toast.LENGTH_LONG)
 					.show();
 
     	} else {
 
 	    	ListView lvwTorrents = (ListView) this.objTrend.findViewById(R.id.torrents);
 	    	this.objTrend.objAdapter = new TrendingTorrentsAdapter(this.objTrend, objTorrents);
 	    	lvwTorrents.setAdapter(this.objTrend.objAdapter);
 	    	//this.objTrend.invalidateOptionsMenu(); //TODO
 
 	    	lvwTorrents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 	    	    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
 
 	                if (Scraper.this.objExpander == null || Scraper.this.objExpander.hasEnded()) {
 
     	                if(vewPrevious != null) {
     	                    Scraper.this.objTrend.objAdapter.objRows.get(position).setExpanded(false);
         	                View close = vewPrevious.findViewById(R.id.toolbar);
         	                Scraper.this.objExpander = new ExpandAnimation(close, 500);
         	                close.startAnimation(Scraper.this.objExpander);
     	                }
 
     	                Scraper.this.objTrend.objAdapter.objRows.get(position).setExpanded(true);
     	                View toolbar = view.findViewById(R.id.toolbar);
     	                Scraper.this.objExpander = new ExpandAnimation(toolbar, 500);
     	                toolbar.startAnimation(Scraper.this.objExpander);
 
     	                vewPrevious = view;
 
 	                }
 
 	            }
 
 	        });
 
     	}
 
     }
 
 }
