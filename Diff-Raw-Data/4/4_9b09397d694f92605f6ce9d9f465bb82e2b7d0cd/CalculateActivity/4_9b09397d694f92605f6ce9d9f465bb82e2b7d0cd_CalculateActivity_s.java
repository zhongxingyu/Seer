 package com.gris.ege.activity;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Locale;
 
 import com.gris.ege.R;
 import com.gris.ege.db.ResultsOpenHelper;
 import com.gris.ege.other.GlobalData;
 import com.gris.ege.other.Log;
 import com.gris.ege.other.Mail;
 import com.gris.ege.other.Task;
 import com.gris.ege.other.Utils;
 import com.gris.ege.pager.TaskFragment;
 import com.gris.ege.pager.TasksPageAdapter;
 import com.gris.ege.pager.TouchViewPager;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteDatabase;
 
 @SuppressLint("HandlerLeak")
 public class CalculateActivity extends FragmentActivity
 {
     private static final String TAG="CalculateActivity";
 
     private static final String USER_ID           = "userId";
     private static final String LESSON_ID         = "lessonId";
     private static final String START_TIME        = "startTime";
     private static final String MODE              = "mode";
     private static final String TIME_FOR_EXAM     = "timeForExam";
     private static final String VERIFICATION_PAGE = "verificationPage";
 
     private static final int TIMER_TICK  = 1;
     private static final int SELECT_PAGE = 2;
     public  static final int VERIFY_PAGE = 3;
 
     private static final int TIMER_INTERVAL=1000;
 
     public  static final int MODE_VIEW_TASK    = 0;
     public  static final int MODE_TEST_TASK    = 1;
     public  static final int MODE_VIEW_RESULT  = 2;
     public  static final int MODE_VERIFICATION = 3;
 
 
 
     private TextView         mTimeLeftTextView;
 
     private RelativeLayout   mResultsLayout;
     private TextView         mTimeTextView;
     private ProgressBar      mPercentProgressBar;
     private TextView         mPercentTextView;
 
     private TouchViewPager   mTasksPager;
     private TasksPageAdapter mTasksAdapter;
 
     private ProgressDialog   mProgressDialog=null;
 
     private long             mActivityStart=0;
 
     private int              mMode;
     private long             mUserId;
     private long             mLessonId;
 
     private long             mTimeForExam=0;
     private int              mVerificationPage=0;
 
 
 
     private Handler mHandler = new Handler()
     {
         @Override
         public void handleMessage(Message msg)
         {
             switch (msg.what)
             {
                 case TIMER_TICK:
                     onTimerTick();
                 break;
                 case SELECT_PAGE:
                     mTasksPager.setCurrentItem(msg.arg1, false);
                 break;
                 case VERIFY_PAGE:
                     onVerifyPage();
                 break;
             }
         }
     };
 
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_calculate);
 
         // Initialize variables
         ResultsOpenHelper aResultsHelper=new ResultsOpenHelper(this);
 
         if (savedInstanceState==null)
         {
             SharedPreferences aSettings=getSharedPreferences(GlobalData.PREFS_NAME, 0);
             String aUserName=aSettings.getString(GlobalData.OPTION_USER_NAME, "");
 
             mUserId   = aResultsHelper.getOrCreateUserId(aUserName);
             mLessonId = aResultsHelper.getOrCreateLessonId(GlobalData.selectedLesson.getId());
         }
         else
         {
             mUserId   = savedInstanceState.getLong(USER_ID, 0);
             mLessonId = savedInstanceState.getLong(USER_ID, 0);
         }
 
         // Get controls
         mTimeLeftTextView   = (TextView)      findViewById(R.id.timeLeftTextView);
         mResultsLayout      = (RelativeLayout)findViewById(R.id.resultsLayout);
         mTimeTextView       = (TextView)      findViewById(R.id.timeTextView);
         mPercentProgressBar = (ProgressBar)   findViewById(R.id.percentProgressBar);
         mPercentTextView    = (TextView)      findViewById(R.id.percentTextView);
         mTasksPager         = (TouchViewPager)findViewById(R.id.tasksPager);
 
         // Initialize controls
         Intent aIntent=getIntent();
         Bundle aExtras=aIntent.getExtras();
 
         if (aExtras.containsKey(GlobalData.TASK_ID))
         {
             mMode=MODE_VIEW_TASK;
             setTitle(getString(R.string.title_activity_calculate_tasks, GlobalData.selectedLesson.getName()));
 
             mTasksAdapter=new TasksPageAdapter(getSupportFragmentManager(), GlobalData.tasks);
 
             if (savedInstanceState==null)
             {
                 int aTaskId=aExtras.getInt(GlobalData.TASK_ID);
                 Log.v(TAG, "Start calculation for task: "+String.valueOf(aTaskId));
 
                 Message aSelectPageMessage=new Message();
                 aSelectPageMessage.what=SELECT_PAGE;
                 aSelectPageMessage.arg1=aTaskId;
                 mHandler.sendMessage(aSelectPageMessage);
             }
 
             updateControlsVisibility();
         }
         else
         if (aExtras.containsKey(GlobalData.TASKS_COUNT))
         {
             setTitle(getString(R.string.title_activity_calculate_testing, GlobalData.selectedLesson.getName()));
 
             int aTaskCount=aExtras.getInt(GlobalData.TASKS_COUNT);
 
             if (savedInstanceState==null)
             {
                 Log.v(TAG, "Start calculation for tasks:");
                 mActivityStart    = SystemClock.uptimeMillis();
                 mMode             = MODE_TEST_TASK;
             }
             else
             {
                 mActivityStart    = savedInstanceState.getLong(START_TIME,        SystemClock.uptimeMillis());
                 mMode             = savedInstanceState.getInt( MODE,              MODE_TEST_TASK);
                 mTimeForExam      = savedInstanceState.getLong(TIME_FOR_EXAM,     0);
                 mVerificationPage = savedInstanceState.getInt( VERIFICATION_PAGE, 0);
             }
 
             ArrayList<Task> aSelectedTasks=new ArrayList<Task>();
 
             for (int i=0; i<aTaskCount; ++i)
             {
                 int aTaskId=aExtras.getInt(GlobalData.TASK_ID+"_"+String.valueOf(i));
 
                 if (savedInstanceState==null)
                 {
                     Log.v(TAG, "Task № "+String.valueOf(aTaskId));
                 }
 
                 Task aTask=GlobalData.tasks.get(aTaskId);
                 aSelectedTasks.add(aTask);
             }
 
             mTasksAdapter=new TasksPageAdapter(getSupportFragmentManager(), aSelectedTasks);
 
             updateControlsVisibility();
         }
         else
         if (aExtras.containsKey(GlobalData.RESULT_ID))
         {
             mMode=MODE_VIEW_RESULT;
             setTitle(getString(R.string.title_activity_calculate_results, GlobalData.selectedLesson.getName()));
 
             long aResultId=aExtras.getLong(GlobalData.RESULT_ID);
 
             if (savedInstanceState==null)
             {
                 Log.v(TAG, "View result № "+String.valueOf(aResultId));
             }
 
             long aTime=aResultsHelper.getTime(aResultId);
             int aPercent=aResultsHelper.getPercent(aResultId);
 
             mTimeTextView.setText(Utils.timeToString(getString(R.string.time), aTime));
             mPercentProgressBar.setMax(100);
             mPercentProgressBar.setProgress(aPercent);
             mPercentTextView.setText(getString(R.string.percent, aPercent));
 
             ArrayList<Task> aSelectedTasks=aResultsHelper.getTasksForResult(aResultId, GlobalData.tasks);
             mTasksAdapter=new TasksPageAdapter(getSupportFragmentManager(), aSelectedTasks);
 
             updateControlsVisibility();
         }
         else
         {
             Log.e(TAG, "Unknown data received from Intent");
         }
 
         mTasksPager.setAdapter(mTasksAdapter);
 
         mTasksPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
         {
             @Override
             public void onPageSelected(int aPosition)
             {
                 mTasksPager.setCurrentPage(mTasksAdapter.getFragment(aPosition));
             }
 
             @Override
             public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
             {
             }
 
             @Override
             public void onPageScrollStateChanged(int state)
             {
             }
         });
 
         downloadXML();
         sendLogFile();
     }
 
     @Override
     protected void onPause()
     {
         mHandler.removeMessages(TIMER_TICK);
 
         super.onPause();
     }
 
     @Override
     protected void onResume()
     {
         if (mMode==MODE_TEST_TASK)
         {
             mHandler.removeMessages(TIMER_TICK);
             onTimerTick();
         }
 
         super.onResume();
     }
 
     @Override
     protected void onSaveInstanceState(Bundle aOutState)
     {
         aOutState.putLong(USER_ID,   mUserId);
         aOutState.putLong(LESSON_ID, mLessonId);
 
         if (
             mMode==MODE_TEST_TASK
             ||
             mMode==MODE_VERIFICATION
            )
         {
             aOutState.putLong(START_TIME,        mActivityStart);
             aOutState.putInt( MODE,              mMode);
             aOutState.putLong(TIME_FOR_EXAM,     mTimeForExam);
             aOutState.putInt( VERIFICATION_PAGE, mVerificationPage);
         }
 
         super.onSaveInstanceState(aOutState);
     }
 
     @Override
     public void onBackPressed()
     {
         if (mMode==MODE_TEST_TASK)
         {
             DialogFragment aFinishDialog = new DialogFragment()
             {
                 public Dialog onCreateDialog(Bundle savedInstanceState)
                 {
                     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
                     builder.setMessage(R.string.do_you_want_to_finish)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    completeTest();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dismiss();
                                }
                            });
 
                     return builder.create();
                 }
             };
 
             aFinishDialog.show(getSupportFragmentManager(), "FinishDialog");
         }
         else
         if (mMode!=MODE_VERIFICATION)
         {
             super.onBackPressed();
         }
     }
 
     public void updateControlsVisibility()
     {
         switch (mMode)
         {
             case MODE_VIEW_TASK:
                 mTimeLeftTextView.setVisibility(View.GONE);
                 mResultsLayout.setVisibility(View.GONE);
             break;
             case MODE_TEST_TASK:
             case MODE_VERIFICATION:
                 mTimeLeftTextView.setVisibility(View.VISIBLE);
                 mResultsLayout.setVisibility(View.GONE);
             break;
             case MODE_VIEW_RESULT:
                 mTimeLeftTextView.setVisibility(View.GONE);
                 mResultsLayout.setVisibility(View.VISIBLE);
             break;
         }
     }
 
     public void createProgressDialog()
     {
         if (mProgressDialog==null)
         {
             mProgressDialog=ProgressDialog.show(this, getString(R.string.checking), getString(R.string.please_wait), true, false);
         }
     }
 
     public void removeProgressDialog()
     {
         if (mProgressDialog!=null)
         {
             mProgressDialog.dismiss();
             mProgressDialog=null;
         }
     }
 
     public void onTimerTick()
     {
         long aCurTime=SystemClock.uptimeMillis();
         long aTimeLeft=GlobalData.selectedLesson.getTime()*60*1000-(aCurTime-mActivityStart);
 
         if (aTimeLeft<0)
         {
             aTimeLeft=0;
         }
 
         mTimeLeftTextView.setText(Utils.timeToString(getString(R.string.time_left), aTimeLeft));
 
         if (aTimeLeft==0)
         {
             completeTest();
         }
         else
         {
             mHandler.sendEmptyMessageDelayed(TIMER_TICK, TIMER_INTERVAL);
         }
     }
 
     public void onVerifyPage()
     {
         if (mVerificationPage<mTasksAdapter.getCount())
         {
             createProgressDialog();
 
             mTasksPager.setCurrentItem(mVerificationPage, false);
             TaskFragment aFragment=(TaskFragment)mTasksAdapter.getFragment(mVerificationPage);
 
             aFragment.checkAnswer();
             aFragment.getTask().setAnswer(aFragment.getAnswer());
 
             ++mVerificationPage;
         }
         else
         {
             Log.d(TAG, "Saving results to database");
 
             removeProgressDialog();
 
             SQLiteDatabase aDb=null;
 
             try
             {
                 int aScore=0;
                 int aTotalScore=0;
                 ArrayList<Task> aTestTasks=mTasksAdapter.getData();
 
                 for (int i=0; i<aTestTasks.size(); ++i)
                 {
                     Task aTask=aTestTasks.get(i);
 
                     int aCurScore;
 
                     if (aTask.getCategory().charAt(0)=='A')
                     {
                         aCurScore=GlobalData.selectedLesson.getScoreA();
                     }
                     else
                     if (aTask.getCategory().charAt(0)=='B')
                     {
                         aCurScore=GlobalData.selectedLesson.getScoreB();
                     }
                     else
                     if (aTask.getCategory().charAt(0)=='C')
                     {
                         aCurScore=GlobalData.selectedLesson.getScoreC();
                     }
                     else
                     {
                         Log.e(TAG, "Invalid category \""+aTask.getCategory()+"\" for task № "+String.valueOf(aTask.getId()));
                         aCurScore=0;
                     }
 
                     if (aTask.isFinished())
                     {
                         aScore+=aCurScore;
                     }
 
                     aTotalScore+=aCurScore;
                 }
 
                 ResultsOpenHelper aResultsHelper=new ResultsOpenHelper(this);
 
                 // ------------------------------------------------------------
 
                 aDb=aResultsHelper.getWritableDatabase();
 
                 ContentValues aResultValues=new ContentValues();
                 aResultValues.put(ResultsOpenHelper.COLUMN_USER_ID,   mUserId);
                 aResultValues.put(ResultsOpenHelper.COLUMN_LESSON_ID, mLessonId);
                 aResultValues.put(ResultsOpenHelper.COLUMN_TIME,      mTimeForExam);
                 aResultValues.put(ResultsOpenHelper.COLUMN_PERCENT,   aTotalScore==0? 100 : aScore*100/aTotalScore);
 
                 long aResultId=aDb.insertOrThrow(
                                                  ResultsOpenHelper.RESULTS_TABLE_NAME,
                                                  null,
                                                  aResultValues
                                                 );
 
                 // ------------------------------------------------------------
 
                 for (int i=0; i<aTestTasks.size(); ++i)
                 {
                     ContentValues aAnswerValues=new ContentValues();
                     aAnswerValues.put(ResultsOpenHelper.COLUMN_RESULT_ID,   aResultId);
                     aAnswerValues.put(ResultsOpenHelper.COLUMN_TASK_NUMBER, aTestTasks.get(i).getId());
                     aAnswerValues.put(ResultsOpenHelper.COLUMN_ANSWER,      aTestTasks.get(i).getAnswer());
                     aAnswerValues.put(ResultsOpenHelper.COLUMN_CORRECT,     aTestTasks.get(i).isFinished()? 1 : 0);
 
                     aDb.insertOrThrow(
                                       ResultsOpenHelper.ANSWERS_TABLE_NAME,
                                       null,
                                       aAnswerValues
                                      );
                 }
 
                 // ------------------------------------------------------------
 
                 Intent aData=new Intent();
                 aData.putExtra(GlobalData.RESULT_ID, aResultId);
                 setResult(StartTestActivity.RESULT_START_TEST, aData);
             }
             catch (Exception e)
             {
                 Log.e(TAG, "Problem occurred while saving data to database", e);
             }
 
             if (aDb!=null)
             {
                 aDb.close();
             }
 
             finish();
         }
     }
 
     public void completeTest()
     {
         mTimeForExam=SystemClock.uptimeMillis()-mActivityStart;
 
         if (mTimeForExam>GlobalData.selectedLesson.getTime()*60*1000)
         {
             mTimeForExam=GlobalData.selectedLesson.getTime()*60*1000;
         }
 
         Log.d(TAG, "Complete test for "+String.valueOf(mTimeForExam)+" ms");
 
         if (mTimeForExam>180000) //3*60*1000
         {
             mMode=MODE_VERIFICATION;
 
             mVerificationPage=0;
             mTasksPager.setCurrentItem(mVerificationPage, false);
 
             mHandler.sendEmptyMessage(VERIFY_PAGE);
         }
         else
         {
             finish();
         }
     }
 
     public int getMode()
     {
         return mMode;
     }
 
     public long getUserId()
     {
         return mUserId;
     }
 
     public long getLessonId()
     {
         return mLessonId;
     }
 
     public Handler getHandler()
     {
         return mHandler;
     }
 
     public void downloadXML()
     {
         SharedPreferences aSettings = getSharedPreferences(GlobalData.PREFS_NAME, 0);
         String aUpdateTime          = aSettings.getString(GlobalData.OPTION_UPDATE_TIME+"_"+GlobalData.selectedLesson.getId(), "never");
 
         String aCurTimeStr=new SimpleDateFormat("DD.MM.yyyy", new Locale("en")).format(new Date());
 
         boolean aSame=aUpdateTime.equals(aCurTimeStr);
 
         Log.d(TAG, "Last time tasks for lesson \""+GlobalData.selectedLesson.getId()+"\" updated: "+aUpdateTime+
                    "; Currrent Time: "+aCurTimeStr+
                    "; Same: "+String.valueOf(aSame));
 
         if (!aSame)
         {
             Log.d(TAG, "Downloading tasks for lesson \""+GlobalData.selectedLesson.getId()+"\"");
             new DownloadXMLTask().execute();
         }
     }
 
     public void sendLogFile()
     {
     	new SendLogTask().execute();
     }
 
     private class DownloadXMLTask extends AsyncTask<Void, Void, InputStream>
     {
         @Override
         protected InputStream doInBackground(Void... aNothing)
         {
             InputStream res=null;
 
             try
             {
                 res=getXml();
             }
             catch (Exception e)
             {
                 Log.i(TAG, "Problem while downloading tasks xml file", e);
             }
 
             return res;
         }
 
         private InputStream getXml() throws IOException
         {
             // Download file
             URL aUrl=new URL(GlobalData.PATH_ON_NET+GlobalData.selectedLesson.getId()+".xml");
 
             HttpURLConnection aConnection=(HttpURLConnection)aUrl.openConnection();
             aConnection.setReadTimeout(300000);
             aConnection.setConnectTimeout(300000);
             aConnection.setRequestMethod("GET");
             aConnection.setDoInput(true);
 
             aConnection.connect();
             return aConnection.getInputStream();
         }
 
         @Override
         protected void onPostExecute(InputStream aResult)
         {
             try
             {
                 if (aResult!=null)
                 {
                     byte[] aBuffer=new byte[4096];
 
                     new File(GlobalData.PATH_ON_SD_CARD).mkdirs();
 
                     FileOutputStream aNewFile=new FileOutputStream(GlobalData.PATH_ON_SD_CARD+GlobalData.selectedLesson.getId()+".xml");
 
                     do
                     {
                         int aBytes=aResult.read(aBuffer);
 
                         if (aBytes<=0)
                         {
                             break;
                         }
 
                         aNewFile.write(aBuffer, 0, aBytes);
                     } while(true);
 
                     aNewFile.close();
                     aResult.close();
 
 
 
                     String aCurTimeStr=new SimpleDateFormat("DD.MM.yyyy", new Locale("en")).format(new Date());
 
                     SharedPreferences aSettings = getSharedPreferences(GlobalData.PREFS_NAME, 0);
                     SharedPreferences.Editor aEditor = aSettings.edit();
                     aEditor.putString(GlobalData.OPTION_UPDATE_TIME+"_"+GlobalData.selectedLesson.getId(), aCurTimeStr);
                     aEditor.commit();
 
                     Log.d(TAG, "Tasks updated for lesson \""+GlobalData.selectedLesson.getId()+"\"");
                 }
             }
             catch (Exception e)
             {
                 Log.w(TAG, "Problem while saving tasks xml file", e);
             }
         }
     }
 
     private class SendLogTask extends AsyncTask<Void, Void, Void>
     {
         @Override
         protected Void doInBackground(Void... aNothing)
         {
        	Log.e("BLYA", "Mail");

             try
             {
             	String aFileName=Log.getPreviousFile();
 
             	if (aFileName!=null)
             	{
                     Mail aMail = new Mail("betatest95@gmail.com", "e567dg9hv4bnGdgfh456");
 
                     String[] toArr = {"betatest95@yandex.com"};
                     aMail.setFrom("betatest95@gmail.com");
                     aMail.setTo(toArr);
                     aMail.setSubject("Log file for EGE v. "+getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                     aMail.setBody("This mail contains logs. Please check.");
                     aMail.addAttachment(aFileName);
                     aMail.send();
             	}
             }
             catch (Exception e)
             {
                 Log.i(TAG, "Problem while sending log file", e);
             }
 
             return null;
         }
      }
 }
