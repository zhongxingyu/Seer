 /*
  * Copyright (c) 2011, Code Aurora Forum. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *        * Redistributions of source code must retain the above copyright
  *            notice, this list of conditions and the following disclaimer.
  *        * Redistributions in binary form must reproduce the above copyright
  *            notice, this list of conditions and the following disclaimer in the
  *            documentation and/or other materials provided with the distribution.
  *        * Neither the name of Code Aurora nor
  *            the names of its contributors may be used to endorse or promote
  *            products derived from this software without specific prior written
  *            permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NON-INFRINGEMENT ARE DISCLAIMED.    IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.quicinc.fmradio;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Handler;
 import android.os.Message;
 import android.os.RemoteException;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.HashMap;
 
 public class FMStats extends Activity  {
 
     /*Data structure for band*/
     private class Band {
 
         public int lFreq;
         public int hFreq;
         public int Spacing;
     }
     /* Data structure for Result*/
     private class Result {
 
       private String mFreq;
       private String mRSSI;
       private String mIoC;
       private String mIntDet;
 
 
       public void setFreq(String aFreq) {
          this.mFreq = aFreq;
       }
 
       public String getFreq() {
          return mFreq;
       }
 
       public void setRSSI(String aRSSI) {
          this.mRSSI = aRSSI;
       }
 
       public String getRSSI() {
          return mRSSI;
       }
 
       public void setIoC(String aIoC) {
          this.mIoC = aIoC;
       }
 
       public String getIoC() {
          return mIoC;
       }
 
       public void setIntDet(String aIntDet) {
          this.mIntDet = aIntDet;
       }
 
       public String getIntDet() {
          return mIntDet;
       }
     };
 
     /*constant column header*/
     Result mColumnHeader = new Result();
 
     boolean mTestRunning = false;
     MyOnItemSelectedListener mSpinnerListener = new MyOnItemSelectedListener();
     int  mTestSelected = 0;
     boolean mIsSearching = false;
     private static String LOGTAG = "FMStats";
     private static IFMRadioService mService = null;
     private Thread mMultiUpdateThread =null;
     private static final int STATUS_UPDATE =1;
     private static final int STATUS_DONE =2;
     private static final int STOP_ROW_ID =200;
     private static final int NEW_ROW_ID = 300;
     private int mStopIds = STOP_ROW_ID;
     private int mNewRowIds = NEW_ROW_ID;
 
     private static final int CUR_FREQ_TEST =0;
     private static final int CUR_MULTI_TEST = 1;
     private static final int SEARCH_TEST =2;
     private static final int SWEEP_TEST =3;
     private Band mBand =null;
     private Band mSync = null;
 
     private FileOutputStream mFileCursor =null;
     private String mCurrentFileName = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.fmstats);
         Spinner spinner = (Spinner) findViewById(R.id.spinner);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.test_summary, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
         spinner.setOnItemSelectedListener(mSpinnerListener);
 
 
         Button RunButton = (Button)findViewById(R.id.Runbutton);
         RunButton.setOnClickListener(mOnRunListener);
         long  curTime = System.currentTimeMillis();
         mCurrentFileName = "FMStats_".concat(Long.toString(curTime).concat(".txt"));
         Log.e(LOGTAG,"Filename is "+mCurrentFileName);
         try {
             mFileCursor = openFileOutput(mCurrentFileName, Context.MODE_PRIVATE);
             if(null != mFileCursor) {
                Log.e(LOGTAG, "location of the file is"+getFilesDir());
             }
         } catch (IOException e) {
              e.printStackTrace();
             Log.e(LOGTAG,"Couldn't create the file to writeLog");
             mCurrentFileName = null;
         }
 
         if (false == bindToService(this, osc))
         {
            Log.d(LOGTAG, "onCreate: Failed to Start Service");
         }
         else
         {
            Log.d(LOGTAG, "onCreate: Start Service completed successfully");
         }
 
 	/*Initialize the column header with
 	constant values*/
 	mColumnHeader.setFreq("Freq");
 	mColumnHeader.setRSSI("RMSSI");
 	mColumnHeader.setIoC("IoC");
 	mColumnHeader.setIntDet("IntDet");
     }
 
     public void onDestroy() {
         if(null != mFileCursor ) {
 	    try {
 		mFileCursor.close();
             } catch (IOException e) {
 
                 e.printStackTrace();
             }
         }
 	/*Stop the thread by interrupting it*/
 	if(mMultiUpdateThread != null) {
 		mMultiUpdateThread.interrupt();
 		mMultiUpdateThread = null;
 	}
 	/*Stop the search/scan if there is an ongoing*/
         if(SEARCH_TEST == mTestSelected)
         {
 		Log.d(LOGTAG, "Stop Search\n");
 		try {
                     mService.cancelSearch();
                 } catch (RemoteException e) {
                      e.printStackTrace();
                 }
         }
 
         unbindFromService(this);
         Log.d(LOGTAG, "onDestroy: unbindFromService completed");
         mService = null;
         super.onDestroy();
     }
 
     private View.OnClickListener mOnRunListener = new View.OnClickListener() {
        public void onClick(View v) {
           if(false == mTestRunning)
           {
               clearPreviousTestResults();
               mTestRunning = true;
               runCurrentTest();
           }
           else
           {
               mTestRunning = false;
               /*Set it back to ready to Run*/
               SetButtonState(true);
 		/*Stop the thread by interrupting it*/
 	      if(mMultiUpdateThread != null) {
 	            mMultiUpdateThread.interrupt();
 		    mMultiUpdateThread = null;
 	      }
 
               if(SEARCH_TEST == mTestSelected )
               {
                   try {
                      mService.cancelSearch();
                  } catch (RemoteException e) {
                      e.printStackTrace();
                  }
               }
           }
 
           }
        };
 
    private void clearPreviousTestResults()
    {
        TableLayout tl = (TableLayout) findViewById(R.id.maintable);
        tl.removeAllViewsInLayout();
        mNewRowIds = NEW_ROW_ID;
    }
 
 
     private void SetButtonState(boolean state)
     {
         // Get the TableRow
         Button RunButton = (Button)findViewById(R.id.Runbutton);
         ProgressBar  pbar = (ProgressBar) findViewById(R.id.progressbar);
         /*Update the state of the button based on
         state*/
         if( state )
         {
             RunButton.setText(R.string.test_run);
             pbar.setVisibility(View.INVISIBLE);
         }
         else
         {
             RunButton.setText("Stop Test");
             pbar.setVisibility(View.VISIBLE);
         }
     }
 
     public class MyOnItemSelectedListener implements OnItemSelectedListener {
 
 
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
             Log.d("Table","onItemSelected is hit with "+pos);
             mTestSelected = pos;
 
         }
 
         public void onNothingSelected(AdapterView<?> parent) {
             // Do Nothing
         }
     }
 
     private void createResult(Result aRes) {
         // Get the TableLayout
         TableLayout tl = (TableLayout) findViewById(R.id.maintable);
 
          /* Create a new row to be added. */
         mNewRowIds++;
         TableRow tr2 = new TableRow(getApplicationContext());
         int width = getWindowManager().getDefaultDisplay().getWidth();
         tr2.setLayoutParams(new LayoutParams(
                      LayoutParams.FILL_PARENT,
                      LayoutParams.WRAP_CONTENT));
         tr2.setId(mNewRowIds);
         /* Create a Button to be the row-content. */
         TextView colFreq = new TextView(getApplicationContext());
         colFreq.setText(aRes.getFreq());
         colFreq.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
         colFreq.setWidth(width/4);
                 /* Add Button to row. */
         tr2.addView(colFreq);
 
         TextView colRMSSI = new TextView(getApplicationContext());
         colRMSSI.setText(aRes.getRSSI());
         colRMSSI.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
         colRMSSI.setWidth(width/4);
         tr2.addView(colRMSSI);
 
         TextView colIoC = new TextView(getApplicationContext());
         colIoC.setText(aRes.getIoC());
         colIoC.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
         colIoC.setWidth(width/4);
         tr2.addView(colIoC);
 
         TextView colIntDet = new TextView(getApplicationContext());
         colIntDet.setText(aRes.getIntDet());
         colIntDet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
         colIntDet.setWidth(width/4);
         tr2.addView(colIntDet);
           /* Add row to TableLayout. */
         tl.addView(tr2,new TableLayout.LayoutParams(
              LayoutParams.FILL_PARENT,
              LayoutParams.WRAP_CONTENT));
         if(null != mFileCursor )
         {
              try {
              StringBuilder tempStr = new StringBuilder();
              tempStr.append(aRes.getFreq());
              tempStr.append('\t');
              tempStr.append(aRes.getRSSI());
              tempStr.append('\t');
              tempStr.append(aRes.getIoC());
              tempStr.append('\t');
              tempStr.append(aRes.getIntDet());
              tempStr.append('\n');
              String testStr = new String(tempStr);
              mFileCursor.write(testStr.getBytes());
 	} catch ( IOException ioe) {
 
                 ioe.printStackTrace();
            }
         }
     }
 
 
     private void runCurrentTest(){
         Log.d(LOGTAG, "The test being run is" +mTestSelected);
 
         //get test summary
         String[] szTestInformation = getResources().getStringArray(
                         R.array.test_summary);
         final StringBuilder szbTestHeader = new StringBuilder();
         szbTestHeader.append("running test:").append(szTestInformation[mTestSelected]);
         String szTestHeader = new String(szbTestHeader);
         if(null != mFileCursor )
         {
             try {
                 mFileCursor.write(szTestHeader.getBytes());
                 mFileCursor.write('\n');
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         }
         switch(mTestSelected){
             case CUR_FREQ_TEST:
                 Log.d(LOGTAG,"Current Freq test is going to run");
                 int freq = FmSharedPreferences.getTunedFrequency();
                 Result res = GetFMStatsForFreq(freq);
                 createResult(mColumnHeader);
                 createResult(res);
                 mTestRunning = false;
                 break;
             case CUR_MULTI_TEST:
                 /*Set it to ready to Stop*/
                 SetButtonState(false);
                 createResult(mColumnHeader);
 
                 if (mMultiUpdateThread == null)
                 {
                    mMultiUpdateThread = new Thread(null, getMultipleResults,
                                                           "MultiResultsThread");
                 }
                 /* Launch dummy thread to simulate the transfer progress */
                 Log.d(LOGTAG, "Thread State: " + mMultiUpdateThread.getState());
                 if (mMultiUpdateThread.getState() == Thread.State.TERMINATED)
                 {
                     mMultiUpdateThread = new Thread(null, getMultipleResults,
                                                           "MultiResultsThread");
                 }
                 /* If the thread state is "new" then the thread has not yet started */
                 if (mMultiUpdateThread.getState() == Thread.State.NEW)
                 {
                     mMultiUpdateThread.start();
                 }
                 // returns and UI in different thread.
                 break;
             case SEARCH_TEST:
                 try {
                     mService.tune(FmSharedPreferences.getLowerLimit());
                     Log.d(LOGTAG, "start scanning\n");
                     mIsSearching = mService.scan(0);
                 } catch (RemoteException e) {
 
                     e.printStackTrace();
                 }
 
                 if(mIsSearching)
                 {
                     /*Set it to Ready to Stop*/
                     SetButtonState(false);
                     createResult(mColumnHeader);
                     Log.d(LOGTAG, "Created the results and cancel UI\n");
                 }
                 else
                 {
                     mTestRunning = false;
                 }
                 break;
             case SWEEP_TEST:
                 int Spacing = FmSharedPreferences.getChSpacing();
                 int lowerFreq = FmSharedPreferences.getLowerLimit();
                 int higherFreq = FmSharedPreferences.getUpperLimit();
                 /* Set it to Ready to stop*/
                 SetButtonState(false);
                 createResult(mColumnHeader);
                 getFMStatsInBand(lowerFreq,higherFreq,Spacing);
                 break;
         }
     }
 
     /* Thread processing */
     private Runnable getMultipleResults = new Runnable() {
        public void run() {
             /*Collect the data for the current frequency
             20 times*/
             int freq = FmSharedPreferences.getTunedFrequency();
 
             for (int i=0; i<20; i++)
             {
                 try
                 {
                     Thread.sleep(500);
                     Message updateUI = new Message();
                     updateUI.what = STATUS_UPDATE;
                     updateUI.obj = (Object)GetFMStatsForFreq(freq);
                     mUIUpdateHandlerHandler.sendMessage(updateUI);
 
 		} catch (InterruptedException e)
 		{
 			/*break the loop*/
 			break;
 		}
             }
             mTestRunning = false;
             Message updateStop = new Message();
             updateStop.what = STATUS_DONE;
             mUIUpdateHandlerHandler.sendMessage(updateStop);
        }
     };
 
     private void getFMStatsInBand(int lFreq, int hFreq, int Spacing)
     {
         if( null == mBand) {
             mBand = new Band();
         }
         mBand.lFreq = lFreq;
         mBand.hFreq = hFreq;
         if(Spacing == 0)
         {
             mBand.Spacing = 200; // 200KHz
         }
         else if( Spacing == 1)
         {
             mBand.Spacing = 100; // 100KHz
         }
         else
         {
             mBand.Spacing = 50;
         }
 
         if (mMultiUpdateThread == null)
         {
            mMultiUpdateThread = new Thread(null, getSweepResults,
                                                   "MultiResultsThread");
         }
         /* Launch he dummy thread to simulate the transfer progress */
         Log.d(LOGTAG, "Thread State: " + mMultiUpdateThread.getState());
         if (mMultiUpdateThread.getState() == Thread.State.TERMINATED)
         {
             mMultiUpdateThread = new Thread(null, getSweepResults,
                                                   "MultiResultsThread");
         }
         /* If the thread state is "new" then the thread has not yet started */
         if (mMultiUpdateThread.getState() == Thread.State.NEW)
         {
             mMultiUpdateThread.start();
         }
 
     }
 
     /* Thread processing */
     private Runnable getSweepResults = new Runnable() {
        public void run() {
             for (int i = mBand.lFreq; i<= mBand.hFreq; i += mBand.Spacing)
             {
                     try {
                         mService.tune(i);
                         mSync = new Band();
                         synchronized(mSync) {
                             mSync.wait(); //wait till notified
                         }
                         mSync = null;
 
                     Message updateUI = new Message();
                     updateUI.what = STATUS_UPDATE;
                     updateUI.obj = (Object)GetFMStatsForFreq(i);;
                     mUIUpdateHandlerHandler.sendMessage(updateUI);
                     Log.d(LOGTAG,"highFerq is "+mBand.hFreq);
                 }
                  catch (RemoteException e) {
 			Log.d(LOGTAG, "SweepResults:Tune failed\n");
 		 }
 
                 catch (InterruptedException e) {
 			/*Stop the thrad*/
 			break;
                 }
             }
             mTestRunning = false;
             Message updateStop = new Message();
             updateStop.what = STATUS_DONE;
             mUIUpdateHandlerHandler.sendMessage(updateStop);
        }
     };
 
     private Result GetFMStatsForFreq(int freq)
     {
         Result result =new Result();
         Log.d(LOGTAG,"freq is "+freq);
         result.setFreq(Integer.toString(freq));
         byte nRssi =0;
         int nIoC = 0;
         int nInfDet = 0;
         if(null != mService) {
         try {
 
               nRssi = (byte)mService.getRssi();
 
         } catch (RemoteException e)
         {
             e.printStackTrace();
         }
 
         try {
             nIoC = mService.getIoC();
 
         } catch (RemoteException e)
         {
             e.printStackTrace();
         }
 
         try {
             nInfDet = mService.getInfDet();
         } catch (RemoteException e)
         {
             e.printStackTrace();
 	        }
         }
 
         result.setRSSI(Byte.toString(nRssi));
         result.setIoC(Integer.toString(nIoC));
         result.setIntDet(Integer.toString(nInfDet));
 
         return result;
     }
 
 
     private Handler mUIUpdateHandlerHandler = new Handler() {
             public void handleMessage(Message msg) {
                switch (msg.what)
                {
                case STATUS_UPDATE:
 
                    Result myRes = (Result) msg.obj;
                    Log.d(LOGTAG,"Status update is" +myRes.mFreq);
                    createResult(myRes);
                    break;
                case STATUS_DONE:
                    SetButtonState(true);
                   break;
                }
             }
     };
 
     public static IFMRadioService sService = null;
     private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();
 
     public static boolean bindToService(Context context) {
        Log.e(LOGTAG, "bindToService: Context");
        return bindToService(context, null);
     }
 
     public static boolean bindToService(Context context, ServiceConnection callback) {
        Log.e(LOGTAG, "bindToService: Context with serviceconnection callback");
        context.startService(new Intent(context, FMRadioService.class));
        ServiceBinder sb = new ServiceBinder(callback);
        sConnectionMap.put(context, sb);
        return context.bindService((new Intent()).setClass(context,
                                                           FMRadioService.class), sb, 0);
     }
 
     public static void unbindFromService(Context context) {
        ServiceBinder sb = (ServiceBinder) sConnectionMap.remove(context);
        Log.e(LOGTAG, "unbindFromService: Context");
        if (sb == null)
        {
           Log.e(LOGTAG, "Trying to unbind for unknown Context");
           return;
        }
        context.unbindService(sb);
        if (sConnectionMap.isEmpty())
        {
           // presumably there is nobody interested in the service at this point,
           // so don't hang on to the ServiceConnection
           sService = null;
        }
     }
 
     private static class ServiceBinder implements ServiceConnection
     {
        ServiceConnection mCallback;
        ServiceBinder(ServiceConnection callback) {
           mCallback = callback;
        }
 
        public void onServiceConnected(ComponentName className, android.os.IBinder service) {
           sService = IFMRadioService.Stub.asInterface(service);
           if (mCallback != null)
           {
              Log.e(LOGTAG, "onServiceConnected: mCallback");
              mCallback.onServiceConnected(className, service);
           }
        }
 
        public void onServiceDisconnected(ComponentName className) {
           if (mCallback != null)
           {
              mCallback.onServiceDisconnected(className);
           }
           sService = null;
        }
     }
 
 
     private ServiceConnection osc = new ServiceConnection() {
           public void onServiceConnected(ComponentName classname, IBinder obj) {
              mService = IFMRadioService.Stub.asInterface(obj);
              Log.e(LOGTAG, "ServiceConnection: onServiceConnected: ");
              if (mService != null)
              {
                 try
                 {
                    mService.registerCallbacks(mServiceCallbacks);
 
                 } catch (RemoteException e)
                 {
                    e.printStackTrace();
                 }
                 return;
              } else
              {
                 Log.e(LOGTAG, "IFMRadioService onServiceConnected failed");
              }
              finish();
           }
           public void onServiceDisconnected(ComponentName classname) {
           }
        };
 
 
        private IFMRadioServiceCallbacks.Stub  mServiceCallbacks = new IFMRadioServiceCallbacks.Stub()
        {
           public void onEnabled()
           {
              Log.d(LOGTAG, "mServiceCallbacks.onEnabled :");
           }
 
           public void onDisabled()
           {
              Log.d(LOGTAG, "mServiceCallbacks.onDisabled :");
           }
 
           public void onTuneStatusChanged()
           {
              Log.d(LOGTAG, "mServiceCallbacks.onTuneStatusChanged :");
              mHandler.post(mTuneComplete);
           }
 
           public void onProgramServiceChanged()
           {
              Log.d(LOGTAG, "mServiceCallbacks.onProgramServiceChanged :");
           }
 
           public void onRadioTextChanged()
           {
              Log.d(LOGTAG, "mServiceCallbacks.onRadioTextChanged :");
           }
 
           public void onAlternateFrequencyChanged()
           {
              Log.d(LOGTAG, "mServiceCallbacks.onAlternateFrequencyChanged :");
           }
 
           public void onSignalStrengthChanged()
           {
              Log.d(LOGTAG, "mServiceCallbacks.onSignalStrengthChanged :");
           }
 
           public void onSearchComplete()
           {
              Log.d(LOGTAG, "mServiceCallbacks.onSearchComplete :");
              /*Stop the update*/
              Message updateStop = new Message();
              updateStop.what = STATUS_DONE;
              mUIUpdateHandlerHandler.sendMessage(updateStop);
           }
           public void onSearchListComplete()
           {
              Log.d(LOGTAG, "mServiceCallbacks.onSearchListComplete :");
 
           }
 
           public void onMute(boolean bMuted)
           {
              Log.d(LOGTAG, "mServiceCallbacks.onMute :" + bMuted);
           }
 
           public void onAudioUpdate(boolean bStereo)
           {
              Log.d(LOGTAG, "mServiceCallbacks.onAudioUpdate :" + bStereo);
           }
 
           public void onStationRDSSupported(boolean bRDSSupported)
           {
              Log.d(LOGTAG, "mServiceCallbacks.onStationRDSSupported :" + bRDSSupported);
           }
       };
       /* Radio Vars */
      final Handler mHandler = new Handler();
 
      final Runnable mTuneComplete = new Runnable(){
          public void run(){
              if((null != mMultiUpdateThread) &&(null != mSync))
              {
                  synchronized(mSync){
                      mSync.notify();
                  }
              }
             if(mTestSelected == SEARCH_TEST) {
                 /* On every Tune Complete generate the result for the current
                 Frequency*/
                 Message updateUI = new Message();
                 updateUI.what = STATUS_UPDATE;
                 int freq = FmSharedPreferences.getTunedFrequency();
                 updateUI.obj = (Object)GetFMStatsForFreq(freq);;
                 mUIUpdateHandlerHandler.sendMessage(updateUI);
             }
          }
      };
  }
