 package com.gris.ege.pager;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import com.gris.ege.R;
 import com.gris.ege.activity.CalculateActivity;
 import com.gris.ege.db.ResultsOpenHelper;
 import com.gris.ege.other.GlobalData;
 import com.gris.ege.other.Task;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.text.InputType;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewAnimator;
 
 public class TaskFragment extends Fragment implements OnClickListener
 {
     private static final String TAG="TaskFragment";
 
     private static final int PAGE_DOWNLOAD    = 0;
     private static final int PAGE_RETRY       = 1;
     private static final int PAGE_IMAGE       = 2;
 
 
 
     private TextView       mTaskHeaderView;
     private TextView       mTaskStatusView;
     private ViewAnimator   mTaskViewAnimator;
     private Button         mRetryButton;
     private ImageView      mTaskImageView;
     private TextView       mAnswerTextView;
     private RelativeLayout mBottomLayout;
     private EditText       mAnswerEditText;
     private Button         mAnswerButton;
 
     private Task mTask;
 
 
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         Bundle aArgs=getArguments();
 
         if (aArgs!=null)
         {
             mTask=GlobalData.tasks.get(aArgs.getInt(GlobalData.TASK_ID));
         }
         else
         {
             mTask=GlobalData.tasks.get(0);
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater aInflater, ViewGroup aContainer, Bundle aSavedInstanceState)
     {
         View aView=aInflater.inflate(R.layout.task_page_item, aContainer, false);
 
         // Get controls
         mTaskHeaderView   = (TextView)      aView.findViewById(R.id.taskHeaderTextView);
         mTaskStatusView   = (TextView)      aView.findViewById(R.id.taskStatusTextView);
         mTaskViewAnimator = (ViewAnimator)  aView.findViewById(R.id.taskViewAnimator);
         mRetryButton      = (Button)        aView.findViewById(R.id.retryButton);
         mTaskImageView    = (ImageView)     aView.findViewById(R.id.taskImageView);
         mAnswerTextView   = (TextView)      aView.findViewById(R.id.answerTextView);
         mBottomLayout     = (RelativeLayout)aView.findViewById(R.id.bottomLayout);
         mAnswerEditText   = (EditText)      aView.findViewById(R.id.answerEditText);
         mAnswerButton     = (Button)        aView.findViewById(R.id.answerButton);
 
         // Set listeners
         mRetryButton.setOnClickListener(this);
         mAnswerButton.setOnClickListener(this);
 
         // Initialize controls
         mTaskHeaderView.setText(getString(R.string.task_header, mTask.getCategory(), mTask.getId()+1));
         updateStatus();
 
         switch (getCalculateActivity().getMode())
         {
             case CalculateActivity.MODE_VIEW_TASK:
                 mAnswerTextView.setVisibility(View.GONE);
             break;
             case CalculateActivity.MODE_TEST_TASK:
             case CalculateActivity.MODE_VERIFICATION:
                 mAnswerTextView.setVisibility(View.GONE);
                 mAnswerButton.setVisibility(View.GONE);
             break;
             case CalculateActivity.MODE_VIEW_RESULT:
                 mBottomLayout.setVisibility(View.GONE);
                 mAnswerTextView.setText(getString(R.string.answer, mTask.getAnswer()));
             break;
         }
 
         downloadImage();
 
         if (mTask.getCategory().charAt(0)=='A')
         {
             mAnswerEditText.setRawInputType(
                                             InputType.TYPE_CLASS_NUMBER
                                             |
                                             InputType.TYPE_NUMBER_FLAG_SIGNED
                                             |
                                             InputType.TYPE_NUMBER_FLAG_DECIMAL
                                            );
 
             mAnswerEditText.setSingleLine(true);
         }
         else
         if (mTask.getCategory().charAt(0)=='B')
         {
             mAnswerEditText.setRawInputType(
                                             InputType.TYPE_CLASS_NUMBER
                                             |
                                             InputType.TYPE_NUMBER_FLAG_SIGNED
                                             |
                                             InputType.TYPE_NUMBER_FLAG_DECIMAL
                                            );
 
             mAnswerEditText.setSingleLine(true);
         }
         else
         if (mTask.getCategory().charAt(0)=='C')
         {
             mAnswerEditText.setRawInputType(
                                             InputType.TYPE_CLASS_TEXT
                                             |
                                             InputType.TYPE_TEXT_VARIATION_NORMAL
                                             |
                                             InputType.TYPE_TEXT_FLAG_MULTI_LINE
                                            );
 
             mAnswerEditText.setSingleLine(false);
         }
         else
         {
             Log.e(TAG, "Invalid category \""+mTask.getCategory()+"\" for task № "+String.valueOf(mTask.getId()));
         }
 
         return aView;
     }
 
     public void updateStatus()
     {
         if (
             getCalculateActivity().getMode()==CalculateActivity.MODE_TEST_TASK
             ||
             getCalculateActivity().getMode()==CalculateActivity.MODE_VERIFICATION
            )
         {
             mTaskStatusView.setVisibility(View.GONE);
         }
         else
         {
             mTaskStatusView.setVisibility(View.VISIBLE);
 
             if (mTask.isFinished())
             {
                 mTaskStatusView.setText(getString(getCalculateActivity().getMode()==CalculateActivity.MODE_VIEW_TASK? R.string.finished : R.string.correct));
                 mTaskStatusView.setTextColor(getResources().getColor(R.color.good));
             }
             else
             {
                 mTaskStatusView.setText(getString(getCalculateActivity().getMode()==CalculateActivity.MODE_VIEW_TASK? R.string.not_finished : R.string.not_correct));
                 mTaskStatusView.setTextColor(getResources().getColor(R.color.bad));
             }
         }
     }
 
     public void downloadImage()
     {
         new DownloadImageTask().execute();
     }
 
     // Only allowed in MODE_VIEW_TASK and MODE_VERIFICATION
     public void checkAnswer(boolean aCorrect)
     {
         if (getCalculateActivity().getMode()==CalculateActivity.MODE_VIEW_TASK)
         {
             Toast.makeText(getActivity(), aCorrect? R.string.correct : R.string.not_correct, Toast.LENGTH_SHORT).show();
         }
 
         if (aCorrect)
         {
             new ResultsOpenHelper(getActivity()).setTaskFinished(
                                                                  getCalculateActivity().getUserId(),
                                                                  getCalculateActivity().getLessonId(),
                                                                  mTask.getId()
                                                                 );
             mTask.setFinished(true);
             updateStatus();
 
             if (getCalculateActivity().getMode()==CalculateActivity.MODE_VIEW_TASK)
             {
                 InputMethodManager imm=(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(mAnswerEditText.getWindowToken(), 0);
             }
         }
 
         if (getCalculateActivity().getMode()==CalculateActivity.MODE_VERIFICATION)
         {
             getCalculateActivity().getHandler().sendEmptyMessage(CalculateActivity.VERIFY_PAGE);
         }
     }
 
     // Only allowed in MODE_VIEW_TASK and MODE_VERIFICATION
     public void checkAnswer()
     {
         if (mTask.getCategory().charAt(0)=='A')
         {
             checkAnswer(mTask.getAnswer().equalsIgnoreCase(getAnswer()));
         }
         else
         if (mTask.getCategory().charAt(0)=='B')
         {
             checkAnswer(mTask.getAnswer().equalsIgnoreCase(getAnswer()));
         }
         else
         if (mTask.getCategory().charAt(0)=='C')
         {
             if (
                 getCalculateActivity().getMode()==CalculateActivity.MODE_VERIFICATION
                 &&
                 getAnswer().trim().equals("")
                )
             {
                 checkAnswer(false);
             }
             else
             {
                 DialogFragment aCheckDialog = new DialogFragment()
                 {
                     public Dialog onCreateDialog(Bundle savedInstanceState)
                     {
                         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
                        builder.setMessage(getString(R.string.is_it_correct, mTask.getAnswer()))
                                .setPositiveButton(R.string.correct, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        checkAnswer(true);
                                    }
                                })
                                .setNegativeButton(R.string.not_correct, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        checkAnswer(false);
                                    }
                                })
                                .setCancelable(false);
 
                         return builder.create();
                     }
                 };
 
                 aCheckDialog.show(getFragmentManager(), "CheckDialog");
             }
         }
         else
         {
             Log.e(TAG, "Invalid category \""+mTask.getCategory()+"\" for task № "+String.valueOf(mTask.getId()));
         }
     }
 
     @Override
     public void onClick(View v)
     {
         switch (v.getId())
         {
             case R.id.retryButton:
                 downloadImage();
             break;
             case R.id.answerButton:
                 checkAnswer(true);
             break;
         }
     }
 
     public String getAnswer()
     {
         return mAnswerEditText.getText().toString();
     }
 
     public Task getTask()
     {
         return mTask;
     }
 
     public CalculateActivity getCalculateActivity()
     {
         return (CalculateActivity)getActivity();
     }
 
     private class DownloadImageTask extends AsyncTask<Void, Void, Drawable>
     {
         @Override
         protected void onPreExecute()
         {
             mTaskViewAnimator.setDisplayedChild(PAGE_DOWNLOAD);
         }
 
         @Override
         protected Drawable doInBackground(Void... aNothing)
         {
             Drawable res=null;
 
             try
             {
                 res=getImage();
             }
             catch (Exception e)
             {
                 Log.w(TAG, "Problem while loading image", e);
             }
 
             return res;
         }
 
         private Drawable getImage() throws IOException
         {
             String aFileName=GlobalData.selectedLesson.getId()+"/"+String.valueOf(mTask.getId()+1)+".png";
 
             if (new File(GlobalData.PATH_ON_SD_CARD+aFileName).exists())
             {
                 Drawable aDrawable=Drawable.createFromPath(GlobalData.PATH_ON_SD_CARD+aFileName);
 
                 if (aDrawable!=null)
                 {
                     return aDrawable;
                 }
                 else
                 {
                     Log.w(TAG, "Invalid file on sdcard: "+GlobalData.PATH_ON_SD_CARD+aFileName);
                 }
             }
 
             // Download file
             URL aUrl=new URL(GlobalData.PATH_ON_NET+aFileName);
 
             HttpURLConnection aConnection=(HttpURLConnection)aUrl.openConnection();
             aConnection.setReadTimeout(10000);
             aConnection.setConnectTimeout(15000);
             aConnection.setRequestMethod("GET");
             aConnection.setDoInput(true);
 
             // Download file
             aConnection.connect();
             InputStream in=aConnection.getInputStream();
 
             InputStream res=in;
 
             try
             {
                 byte[] aBuffer=new byte[4096];
 
                 new File(GlobalData.PATH_ON_SD_CARD+GlobalData.selectedLesson.getId()).mkdirs();
                 new File(GlobalData.PATH_ON_SD_CARD+".nomedia").createNewFile();
 
                 FileOutputStream aNewFile=new FileOutputStream(GlobalData.PATH_ON_SD_CARD+aFileName);
 
                 do
                 {
                     int aBytes=in.read(aBuffer);
 
                     if (aBytes<=0)
                     {
                         break;
                     }
 
                     aNewFile.write(aBuffer, 0, aBytes);
                 } while(true);
 
                 aNewFile.close();
                 in.close();
 
                 res=new FileInputStream(GlobalData.PATH_ON_SD_CARD+aFileName);
             }
             catch (Exception e)
             {
                 Log.w(TAG, "Problem while saving image on sd card", e);
             }
 
             Drawable aDrawable=Drawable.createFromStream(res, null);
             res.close();
 
             if (aDrawable!=null)
             {
                 return aDrawable;
             }
             else
             {
                 Log.w(TAG, "Invalid file on sdcard after downloading: "+GlobalData.PATH_ON_SD_CARD+aFileName);
             }
 
             return null;
         }
 
         @Override
         protected void onPostExecute(Drawable aResult)
         {
             if (aResult!=null)
             {
                 mTaskImageView.setImageDrawable(aResult);
                 mTaskViewAnimator.setDisplayedChild(PAGE_IMAGE);
             }
             else
             {
                 mTaskViewAnimator.setDisplayedChild(PAGE_RETRY);
             }
         }
     }
 }
