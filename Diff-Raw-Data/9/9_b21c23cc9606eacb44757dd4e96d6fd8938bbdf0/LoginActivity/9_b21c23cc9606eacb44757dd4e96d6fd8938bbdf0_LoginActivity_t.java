 package knaps.hacker.school;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.MapBuilder;
 import com.google.analytics.tracking.android.Tracker;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 
 import knaps.hacker.school.data.HSData;
 import knaps.hacker.school.data.HSDatabaseHelper;
 import knaps.hacker.school.data.HSParser;
 import knaps.hacker.school.models.Student;
 import knaps.hacker.school.networking.DownloadTaskFragment;
 import knaps.hacker.school.utils.Constants;
 import knaps.hacker.school.networking.ImageDownloads;
 import knaps.hacker.school.utils.SharedPrefsUtil;
 
 public class LoginActivity extends BaseFragmentActivity implements View.OnClickListener,
         DownloadTaskFragment.TaskCallbacks {
 
     View mLoadingView;
     EditText mEmailView;
     EditText mPasswordView;
     boolean mHasData;
     private View mPasswordWarning;
     private Button mLoginButton;
     private DownloadTaskFragment mDownloadFragment;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
 
         mLoginButton = (Button) findViewById(R.id.button);
         mLoginButton.setOnClickListener(this);
 
         mLoadingView = findViewById(R.id.loading_view);
         mLoadingView.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 return true;
             }
         });
         mEmailView = (EditText) findViewById(R.id.editEmail);
         mPasswordView = (EditText) findViewById(R.id.editPassword);
         mPasswordWarning = findViewById(R.id.textPasswordNotice);
         final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.showSoftInput(mEmailView, InputMethodManager.SHOW_IMPLICIT);
 
         FragmentManager fm = getSupportFragmentManager();
         mDownloadFragment = (DownloadTaskFragment) fm.findFragmentByTag("download_task");
 
         final HSDatabaseHelper dbHelper = new HSDatabaseHelper(this);
         final SQLiteDatabase db = dbHelper.getReadableDatabase();
         long numHackerSchoolers = 0;
         try {
             numHackerSchoolers = DatabaseUtils.queryNumEntries(db, HSData.HSer.TABLE_NAME);
         }
         finally {
             db.close();
         }
         if (mDownloadFragment != null && mDownloadFragment.isTaskRunning()) {
             freezeViews();
         }
         else if (numHackerSchoolers > 0) {
             mHasData = true;
             Button goToGameButton = (Button) findViewById(R.id.buttonGame);
             goToGameButton.setVisibility(View.VISIBLE);
             goToGameButton.setOnClickListener(this);
             Button viewAllButton = (Button) findViewById(R.id.buttonBrowse);
             viewAllButton.setVisibility(View.VISIBLE);
             viewAllButton.setOnClickListener(this);
             mLoginButton.setText("Refresh Data?");
             mEmailView.setVisibility(View.GONE);
             mPasswordView.setVisibility(View.GONE);
             mPasswordWarning.setVisibility(View.GONE);
             new LoadUserData().execute();
 
         }
     }
 
     private void freezeViews() {
         mEmailView.setEnabled(false);
         mPasswordView.setEnabled(false);
         mLoginButton.setEnabled(false);
         mLoadingView.setVisibility(View.VISIBLE);
     }
 
     private void unFreezeViews() {
         mLoadingView.setVisibility(View.GONE);
         mLoginButton.setEnabled(true);
         mEmailView.setEnabled(true);
         mPasswordView.setEnabled(true);
     }
 
 //    @Override
 //    public boolean onCreateOptionsMenu(Menu menu) {
 //        // Inflate the menu; this adds items to the action bar if it is present.
 //        getMenuInflater().inflate(R.menu.login, menu);
 //        return true;
 //    }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.button:
                 if (mHasData && mPasswordView.getVisibility() == View.GONE) {
                     mPasswordView.setVisibility(View.VISIBLE);
                     mEmailView.setVisibility(View.VISIBLE);
                     mPasswordWarning.setVisibility(View.VISIBLE);
                     mLoginButton.setText("Login");
                     mEmailView.setText(SharedPrefsUtil.getUserEmail(this));
                 }
                 else if (!"".equals(mEmailView.getText().toString()) && !"".equals(mPasswordView.getText().toString())) {
                     final String email = mEmailView.getText().toString();
                     final String password = mPasswordView.getText().toString();
                     mDownloadFragment = new DownloadTaskFragment(email, password, mHasData);
                     getSupportFragmentManager().beginTransaction().add(mDownloadFragment, "download_task").commit();
                     SharedPrefsUtil.saveUserEmail(this, email);
                 }
                 break;
             case R.id.buttonGame:
                 Intent playGame = new Intent(LoginActivity.this, GuessThatHSActivity.class);
                 playGame.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                 startActivity(playGame);
                 break;
             case R.id.buttonBrowse:
                 Intent browse = new Intent(LoginActivity.this, HSListActivity.class);
                 browse.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                 startActivity(browse);
                 break;
             case R.id.textName:
                 // open a dialog to show your highest score :0
                 final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage(getHighScores().toString());
                 builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 });
                 final AlertDialog dialog = builder.create();
                 dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                 dialog.show();
                 break;
             default:
                 // no default
         }
     }
 
     private StringBuilder getHighScores() {
         StringBuilder sb = new StringBuilder("High Score: ");
         sb.append(String.format("%.2f%%", (double) SharedPrefsUtil.getHighScore(this)));
         sb.append("\n");
         sb.append("Lifetime Score: ");
         sb.append(SharedPrefsUtil.getAllTimeScore(this));
         sb.append("\n");
        sb.append("Total Faces Guessed: ");
         sb.append(SharedPrefsUtil.getTries(this));
         return sb;
     }
 
     @Override
     public void onPreExecute() {
         freezeViews();
     }
 
     @Override
     public void onPostExecute(String result) {
         unFreezeViews();
         if (result == null) {
             // navigate to the game page
             Intent intent = new Intent(LoginActivity.this, GuessThatHSActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
             startActivity(intent);
         } else {
             Toast.makeText(LoginActivity.this, "Login error. " + result, Toast.LENGTH_LONG).show();
         }
     }
 
     @Override
     public void onCancelled() {
 
     }
 
    class LoadUserData extends AsyncTask<Void, Void, Student> implements ImageDownloads.ImageDownloadCallback{
 
        @Override
        protected Student doInBackground(Void... params) {
            // get student data
            Student student = new HSDatabaseHelper(LoginActivity.this).getLoggedInStudent(LoginActivity.this);
            // load the image
            new ImageDownloads.HSGetImageTask(student.mImageUrl, LoginActivity.this, this).execute();
            return student;
        }
 
        @Override
        protected void onPostExecute(Student student) {
            if (student != null) {
                TextView user = (TextView) findViewById(R.id.textName);
                user.setText(getString(R.string.hi) + " " + student.mName.split(" ")[0]);
            }
        }
 
        @Override
        public void onPreImageDownload() {
            // Nothing
        }
 
        @Override
        public void onImageDownloaded(Bitmap bitmap) {
            final TextView user = (TextView) findViewById(R.id.textName);
            final DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int dimens = Math.round(dm.density * 15);
            final Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, dimens, dimens, true));
            user.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
 
            user.setOnClickListener(LoginActivity.this);
        }
 
        @Override
        public void onImageFailed() {
             // don't do anything, yo
        }
    }
 }
