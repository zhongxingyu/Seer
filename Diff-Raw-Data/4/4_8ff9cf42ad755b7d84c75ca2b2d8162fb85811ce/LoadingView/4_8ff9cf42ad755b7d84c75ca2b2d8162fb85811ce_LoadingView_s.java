 /*
  * 
  * 
  *  
  * 
  */
 
 package com.fsu.kevinfriedpig;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.os.AsyncTask;  
 import android.os.Bundle;  
 import android.util.Log;
 import android.view.WindowManager;
 import android.widget.ProgressBar;  
 import android.widget.TextView;  
 import android.widget.Toast;
   
 
 public class LoadingView extends Activity {
 
 	static int 		fileCnt 	= 0, 
 				parentCount = 117876, //# of lines in parentVector.txt
 				n2sCount 	= 117876, //# of lines in n2sFile.txt
 				s2nCount	= 117876; //# of lines in s2nFile.txt
 	TextView 	tvProgress1,
 				tvProgress2,
 				tvProgress3;
 	ProgressBar	pbProgress1,
 				pbProgress2,
 				pbProgress3;
 	AssetManager	amInput;
 	Boolean 	parentBl = false,
 				parentStartedBl = false,
 				s2nBl = false,
 				s2nStartedBl = false,
 				n2sBl = false,
 				n2sStartedBl = false;
 	static Boolean		closeView = false;
 	ParentLoad	pl;
 	s2nLoad		s2nl;
 	n2sLoad		n2sl;
 
 	static Vector<Integer> parentVect = new Vector<Integer>( parentCount );
 	static Vector<String>	n2sVect = new Vector<String>( n2sCount );
 	static Hashtable <String, Integer> s2n = new Hashtable <String, Integer>( s2nCount );
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.loading_view);
 
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
 
 		tvProgress1=(TextView)findViewById(R.id.tvProgress1);
 		tvProgress2=(TextView)findViewById(R.id.tvProgress2);
 		tvProgress3=(TextView)findViewById(R.id.tvProgress3);
 		pbProgress1=(ProgressBar)findViewById(R.id.pbProgress1);
 		pbProgress2=(ProgressBar)findViewById(R.id.pbProgress2);
 		pbProgress3=(ProgressBar)findViewById(R.id.pbProgress3);
 		amInput = this.getAssets();
 //		checkForClose();
 		
 		pl = new ParentLoad();
 		s2nl = new s2nLoad();
 		n2sl = new n2sLoad();
 				
 		Log.w("onCreate","before loadFiles");
 		loadFiles();
 		Log.w("onCreate","after loadFiles");		
 	}
 	
 	@Override
 	public void onBackPressed(){
 		Toast.makeText(getBaseContext(), "Please wait for loading to finish!", Toast.LENGTH_SHORT).show();
 	}
 
 
 	/*
 	 *- starts the separate load file methods on separate threads
 	 *    and runs a continuous loop until they all finish
 	 * -upon complete loading, calls the next view
 	 */
 	private void loadFiles(){
 
 			Log.w("LoadingView","loadFiles, before thread calls");
 			pl.execute();
 			s2nl.execute();
 			n2sl.execute();
 			Log.w("LoadingView","loadFiles, after thread calls");
 
 	}
 
 	public static Hashtable <String, Integer> getS2N ()
 	{
 		return s2n;
 	}
 
 	public static Vector<Integer> getParentVector ()
 	{
 		return parentVect;
 	}
 
 	public static Vector<String> getN2S ()
 	{
 		return n2sVect;
 	}
 
 	public static String number2string (int n)
 	{ 
 		return n2sVect.get(n);
 	}
 
 	public static int string2number (String s)
 	{
 		Log.w("string2number", "Testing our n2s vector where n = 2 n2s[2] = " + n2sVect.get(2));
 		Log.w("string2number", "Passing in string s = " + s + " for s2n[s] = " + s2n.get(s));
 		return s2n.get(s);
 	}
 
 	public static int getParent (int n)
 	{
 		return parentVect.get(n);
 	}
 
 	/////////////////////////////////////////////////////////////////
 	/*
 	 * loads the parentVector.txt into a Vector<Integer> parentVector  
 	 */
 	/////////////////////////////////////////////////////////////////
 	private class ParentLoad extends AsyncTask< Void, Integer, Void >{
 
 		/*
 		 * Loads parentVector.txt
 		 * (non-Javadoc)
 		 * @see android.os.AsyncTask#doInBackground(Params[])
 		 */
 		@Override
 		protected Void doInBackground(Void... params) {
 
 			Log.w("LoadView", "ParentVector, entered successfully");
 			String line = "";
 	        BufferedReader reader;
 	        InputStream is = null;
 	        int cnt = 0;
 	        float fileSize = (float)s2nCount;
 	        ++fileCnt;
 
 	        Log.w("LoadView", "ParentVector, after decl.");
 
 	        /*
 	         * open parentVector.txt 
 	         */
 	        try {
 				is = amInput.open("parentVector.txt");
 			} catch (IOException e2) {
 				Log.w("ParentLoad", "doInBackground, open parentVect.txt failed");
 				e2.printStackTrace();
 			} 
 	        reader = new BufferedReader(new InputStreamReader(is));
 
 	        /*
 	         * read in each line, convert to int, and store in vector
 	         */
 	    	Log.w("LoadView", "ParentLoad, before file read");
 	       try {
 			while( (line = reader.readLine()) != null ){
 				//publish update to UI thread for progress
 				++cnt;
 				float count = (float)cnt;
 				float cur = ( count / fileSize );
 				int current = (int)( cur * 100 );
 				if ( ((current % 2) == 0) && current <= 100){
 					if ( pl.isCancelled() )
 						return null;
 				    publishProgress(current);
 				}
 				int value = Integer.parseInt(line);
 				parentVect.add( value );
 			}
 
 	       } catch (IOException e1) {
 	    	   Log.w("ParentLoad", "doInBackground, IOException from readline");
 	    	   e1.printStackTrace();
 	       }
 
 
 	       /*
 	        * close parentVector.txt
 	        */
 	       try {
 	    	   reader.close();
 	       } catch (IOException e) {
 	    	   e.printStackTrace();
 	       }
 
 	          return null;  
 		}//doInBackground() for ParentLoad
 
 
 		//when publishProgress is called
 		@Override  
 	    protected void onProgressUpdate(Integer... values)  
 	    {  
 	        //Update the progress at the UI if progress value is smaller than 100  
 	        if(values[0] <= 100)  
 	        {
 	            tvProgress1.setText("File 1 of 3: " + Integer.toString(values[0]) + "%");  
 	            pbProgress1.setProgress(values[0]);  
 	        }  
 	    }//onProgressUpdate()
 
 
 	        //After executing the code in the thread  
 		@Override  
 		protected void onPostExecute(Void result)  
 		{  
 			parentBl = true;
 			newView();
 		}  
 
 	}// private class ParentLoad extends AsyncTask< Void, Void, Void >		
 
 
 
 
 	//////////////////////////////////////////////////////////	
 	/*
 	 * loads the n2sVector.txt into a Vector<String> n2sVector  
 	 */
 	//////////////////////////////////////////////////////////
 	private class n2sLoad extends AsyncTask< Void, Integer, Void >{
 
 		/*
 		 * Loads n2sFile.txt
 		 * (non-Javadoc)
 		 * @see android.os.AsyncTask#doInBackground(Params[])
 		 */
 		@Override
 		protected Void doInBackground(Void... params) {
 
 			Log.w("LoadView", "n2sLoad, entered successfully");
 			String line2 = "";
 	        BufferedReader reader2;
 	        InputStream is = null;
 	        int cnt = 0;
 	        float fileSize = (float)s2nCount;
 	        ++fileCnt;
 
 	        Log.w("LoadView", "n2sFile, after decl.");
 
 	        /*
 	         * open n2sFile.txt 
 	         */
 	        try {
 				is = amInput.open("n2sFile.txt");
 			} catch (IOException e2) {
 				Log.w("n2sVector", "doInBackground, open n2sFile.txt failed");
 				e2.printStackTrace();
 			} 
 	        reader2 = new BufferedReader(new InputStreamReader(is));
 
 	        /*
 	         * read in each line and store in vector
 	         */
 	    	Log.w("LoadView", "n2sFile, before file read");
 	       try {
 			while( (line2 = reader2.readLine()) != null ){
 				//publish update to UI thread for progress
 				++cnt;
 				float count = (float)cnt;
 				float cur = ( count / fileSize );
 				int current = (int)( cur * 100 );
 				if ( ((current % 2) == 0) && current <= 100){
 					if ( n2sl.isCancelled() )
 						return null;
                     publishProgress(current);
 				}
 				n2sVect.add( line2 );
 				//Log.w("LoadView", "n2sVect adding " + line2 + "so that n2sVect[" + cnt + "] = " + n2sVect.get(cnt));
 			}
 
 		} catch (IOException e1) {
 			Log.w("n2sLoad", "doInBackground, IOException from readline");
 			e1.printStackTrace();
 		}
 
 
 	       /*
 	        * close n2sFile.txt
 	        */
 	       try {
 	    	   reader2.close();
 	       } catch (IOException e) {
 	    	   e.printStackTrace();
 	    	   Log.w("n2sLoad", "doInBackground, IOException from file.close()");
 	       }
 
 	          return null;  
 		}//doInBackground for n2sLoad
 
 
 		//when publishProgress is called
 		@Override  
 	    protected void onProgressUpdate(Integer... values)  
 	    {  
 	        //Update the progress at the UI if progress value is smaller than 100  
 	        if(values[0] <= 100)  
 	        {
 	            tvProgress3.setText("File 3 of 3: " + Integer.toString(values[0]) + "%");  
 	            pbProgress3.setProgress(values[0]);  
 	        }  
 	    }//onProgressUpdate()
 
 
 		//After executing the code in the thread  
 		@Override  
 		protected void onPostExecute(Void result)  
 		{  
 			n2sBl = true;
 			newView();
 		}  
 
 	}// private  class n2sLoad extends AsyncTask< Void, void, Void >		
 
 
 
 
 	/////////////////////////////////////////////////////////////////
 	/*
 	* loads the s2nFile.txt into a Hashtable<String, Integer> s2n  
 	*/
 	/////////////////////////////////////////////////////////////////
 	private class s2nLoad extends AsyncTask< Void, Integer, Void >{
 
 	/*
 	* Loads s2nFile.txt
 	* (non-Javadoc)
 	* @see android.os.AsyncTask#doInBackground(Params[])
 	*/
 	@Override
 	protected Void doInBackground(Void... params) {
 		Log.w("LoadView", "s2nLoad, entered successfully");
 		int cnt = 0;
 
 		String line = "";
 		BufferedReader reader3;
 		InputStream is = null;
 		float fileSize = (float)s2nCount;
 		++fileCnt;
 
 		Log.w("LoadView", "s2nFile, after decl.");
 
 		/*
 		* open s2nFile.txt 
 		*/
 		try {
 			is = amInput.open("s2nFile.txt");
 		} catch (IOException e2) {
 			Log.w("s2nLoad", "doInBackground, open s2nFile.txt failed");
 			e2.printStackTrace();
 		} 
 		reader3 = new BufferedReader(new InputStreamReader(is));
 
 		Log.w("LoadView", "s2nFile, before file read");
 		try {
 			while( (line = reader3.readLine()) != null ){
 
 				//publish update to UI thread for progress
 
 				float count = (float)cnt;
 				float cur = ( count / fileSize );
 				int current = (int)( cur * 100 );
 				if ( ((current % 2) == 0) && current <= 100){
 					if ( s2nl.isCancelled() )
 						return null;
 					publishProgress(current);
 				}
 
 				//store string and int 
 				 s2n.put( line, cnt );
 				 ++cnt;
 			}
 
 		} catch (IOException e1) {
 			Log.w("s2nLoad", "doInBackground, IOException from readline");
 			e1.printStackTrace();
 		}
 
 		/*
 		* close s2nFile.txt
 		*/
 		try {
 			reader3.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;  
 	}
 
 	//when publishProgress is called
 	@Override  
     protected void onProgressUpdate(Integer... values)  
     {  
         //Update the progress at the UI if progress value is smaller than 100  
         if(values[0] <= 100)  
         {
             tvProgress2.setText("File 2 of 3: " + Integer.toString(values[0]) + "%");  
             pbProgress2.setProgress(values[0]);  
         }  
     }//onProgressUpdate()
 
 
 	//After executing the code in the thread  
 	@Override  
 	protected void onPostExecute(Void result)  
 	{  
 		s2nBl = true;
 		newView();
 	}  
 
 	}// private class s2nLoad extends AsyncTask< Void, Integer, Void >		
 
 
 	
 	/*
 	 * test for finished asynctask file loading
 	 * if all loaded open searchView
 	 */
 	private void newView(){
 
 		Log.w("loadView", "newView, entered successfully");
 		if ( s2nBl && n2sBl && parentBl ){
 
 			Intent searchIntent = new Intent(getBaseContext(), SearchView.class);
 			Log.w("newView", "before start activity for result");
 	        startActivityForResult(searchIntent, 1); 
 	        Log.w("newView", "after start activity for result");
 		}
 		else
 			Log.w("LoadView", "newView, all 3 files are not loaded yet.");
 	}
 	
 //	static void prepareToClose(){
 //		closeView = true;
 //	}
 //	
 //	void checkForClose(){
 //		if(closeView)
 //			LoadingView.super.finish();
 //	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == RESULT_OK){
 			pl.cancel(true);
 			s2nl.cancel(true);
 			n2sl.cancel(true);
 
			super.finish();
 		}
 			
 	}
 
 	@Override
 	protected void onDestroy() {
 		pl.cancel(true);
 		s2nl.cancel(true);
 		n2sl.cancel(true);
 		super.onDestroy();
 	}
 	
 
 }
