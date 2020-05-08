 package org.droidstack.activity;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.jtpl.Template;
 import net.sf.stackwrap4j.StackWrapper;
 import net.sf.stackwrap4j.entities.Answer;
 import net.sf.stackwrap4j.entities.Comment;
 import net.sf.stackwrap4j.entities.Question;
 import net.sf.stackwrap4j.entities.User;
 import net.sf.stackwrap4j.http.HttpClient;
 import net.sf.stackwrap4j.query.AnswerQuery;
 import net.sf.stackwrap4j.query.QuestionQuery;
 import net.sf.stackwrap4j.utils.StackUtils;
 
 import org.droidstack.R;
 import org.droidstack.util.Const;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.AssetManager;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.TextUtils.TruncateAt;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class QuestionActivity extends Activity {
 	
 	private static final String API_PREFIX = "api.";
 	
 	private int mQuestionID;
 	private int mAnswerID;
 	private String mEndpoint;
 	private String mTemplate;
 	private int mAnswerCount;
 	private int mCurAnswer = -1;
 	private int mPage = 0;
 	private int mPageSize;
 	
 	private StackWrapper mAPI;
 	private Question mQuestion;
 	private List<Answer> mAnswers;
 	private SharedPreferences mPreferences;
 	
 	private WebView mWebView;
 	private TextView mAnswerCountView;
 	private TextView mAnswerLabel;
 	private Button mNextButton;
 	private Button mPreviousButton;
 	private ProgressBar mProgress;
 	
 	private boolean isRequestOngoing = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.question);
 		
 		HttpClient.setTimeout(Const.NET_TIMEOUT);
 		try {
 			InputStream is = getAssets().open("question.html", AssetManager.ACCESS_BUFFER);
 			StringBuilder builder = new StringBuilder();
 			int b;
 			while((b = is.read()) != -1) {
 				builder.append((char)b);
 			}
 			mTemplate = builder.toString();
 		}
 		catch (Exception e) {
 			Log.e(Const.TAG, "wtf asset load fail", e);
 			finish();
 		}
 		Uri data = getIntent().getData();
 		try {
 			mQuestionID = Integer.parseInt(data.getQueryParameter("qid"));
 		}
 		catch (Exception e) { }
 		try {
 			mAnswerID = Integer.parseInt(data.getQueryParameter("aid"));
 		}
 		catch (Exception e) { }
 		if (mQuestionID == 0 && mAnswerID == 0) {
 			Log.e(Const.TAG, "ViewQuestion: qid/aid not specified");
 			finish();
 			return;
 		}
 		mEndpoint = data.getQueryParameter("endpoint");
 		if (mEndpoint == null) {
 			Log.e(Const.TAG, "ViewQuestion: endpoint not specified");
 			finish();
 			return;
 		}
 		
 		prepareViews();
 		
 		// make the title scroll!
 		// find the title TextView
 		TextView title = (TextView) findViewById(android.R.id.title);
 		// set the ellipsize mode to MARQUEE and make it scroll only once
 		title.setEllipsize(TruncateAt.MARQUEE);
 		title.setMarqueeRepeatLimit(1);
 		// in order to start strolling, it has to be focusable and focused
 		title.setFocusable(true);
 		title.setFocusableInTouchMode(true);
 		title.requestFocus();
 		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 		mPageSize = Const.getPageSize(this);
 		mAPI = new StackWrapper(mEndpoint, Const.APIKEY);
 		if (savedInstanceState == null) {
 			mAnswers = new ArrayList<Answer>();
 			setTitle(R.string.loading);
 			new FetchQuestionTask().execute();
 		}
 		else {
 			mQuestion = (Question) savedInstanceState.getSerializable("mQuestion");
 			mAnswers = (ArrayList<Answer>) savedInstanceState.getSerializable("mAnswers");
 			mCurAnswer = savedInstanceState.getInt("mCurAnswer");
 			mPage = savedInstanceState.getInt("mPage");
 			mAnswerCount = savedInstanceState.getInt("mAnswerCount");
 			updateView();
 		}
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putSerializable("mQuestion", mQuestion);
 		outState.putSerializable("mAnswers", (ArrayList<Answer>) mAnswers);
 		outState.putInt("mCurAnswer", mCurAnswer);
 		outState.putInt("mPage", mPage);
 		outState.putInt("mAnswerCount", mAnswerCount);
 	}
 	
 	private class DWebViewClient extends WebViewClient {
 		
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 			startActivity(i);
 			return true;
 		}
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (isRequestOngoing) return false;
 		getMenuInflater().inflate(R.menu.question, menu);
     	return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		//HACK it would be better if api can provide a browsable URL
 		//but right now it looks like it lacks the feature.
 		String questionUrl = "http://" + Uri.parse(mEndpoint).getHost().replace(API_PREFIX, "");
 		questionUrl += "/questions/" + mQuestion.getPostId();
 		switch(item.getItemId()) {
 		case R.id.menu_open:
 			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(questionUrl)));
 			return true;
 		case R.id.menu_share:
 			Intent i = new Intent(Intent.ACTION_SEND);
 			i.setType("text/plain");
 			i.putExtra(Intent.EXTRA_SUBJECT, mQuestion.getTitle());
 			i.putExtra(Intent.EXTRA_TEXT, questionUrl);
 			startActivity(Intent.createChooser(i, getString(R.string.menu_share)));
 			return true;
 		}
 		return false;
 	}
 	
 	private void prepareViews() {
 		mWebView = (WebView) findViewById(R.id.content);
 		mWebView.getSettings().setJavaScriptEnabled(true);
 		mWebView.setWebViewClient(new DWebViewClient());
 		try {
 			// try to enable caching, useful for avatars
 			WebSettings.class.getMethod("setAppCacheEnabled", new Class[] { boolean.class }).invoke(mWebView.getSettings(), true);
 			WebSettings.class.getMethod("setCacheMode", new Class[] { int.class }).invoke(mWebView.getSettings(), WebSettings.LOAD_CACHE_ELSE_NETWORK);
 		}
 		catch(Exception e) {
 			// app cache not supported, must be < 2.1
 			Log.i(Const.TAG, "Unable to enable WebView caching", e);
 		}
 		mAnswerCountView = (TextView) findViewById(R.id.answer_count);
 		mAnswerLabel = (TextView) findViewById(R.id.answer_label);
 		mNextButton = (Button) findViewById(R.id.next);
 		mPreviousButton = (Button) findViewById(R.id.previous);
 		mProgress = (ProgressBar) findViewById(R.id.progress);
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		setContentView(R.layout.question);
 		prepareViews();
 		if (!isRequestOngoing) updateView();
 	}
 
 	private void updateView() {
 		setTitle(mQuestion.getTitle());
 		
 		if (mCurAnswer == -1) {
 			Template tpl = new Template(mTemplate);
 			tpl.assign("ENDPOINT", mEndpoint);
 			try {
 				for (Comment c: mQuestion.getComments()) {
 					tpl.assign("CBODY", c.getBody());
 					User owner = c.getOwner();
 					if (owner == null) {
 						tpl.assign("CAUTHOR", "?");
 						tpl.assign("CAID", "0");
 					}
 					else {
 						tpl.assign("CAUTHOR", owner.getDisplayName());
 						tpl.assign("CAID", String.valueOf(owner.getId()));
 					}
 					tpl.assign("CSCORE", String.valueOf(c.getScore()));
 					if (c.getScore() > 0) tpl.parse("main.post.comment.score");
 					tpl.parse("main.post.comment");
 				}
 			}
 			catch (Exception e) {
 				Log.e(Const.TAG, "wtf Question.getComments() error", e);
 				finish();
 			}
 			for (String tag: mQuestion.getTags()) {
 				tpl.assign("TAG", tag);
 				tpl.parse("main.post.tags.tag");
 			}
 			tpl.parse("main.post.tags");
 			User owner = mQuestion.getOwner();
 			tpl.assign("QBODY", mQuestion.getBody());
 			tpl.assign("QSCORE", String.valueOf(mQuestion.getScore()));
 			if (owner != null) {
 				tpl.assign("QAHASH", String.valueOf(owner.getEmailHash()));
 				tpl.assign("QANAME", owner.getDisplayName());
 				tpl.assign("QAREP", StackUtils.formatRep(owner.getReputation()));
 				tpl.assign("QAID", String.valueOf(owner.getId()));
 			}
 			else {
 				tpl.assign("QAHASH", "unknown");
 				tpl.assign("QANAME", "[unregistered]");
 				tpl.assign("QAREP", "0");
 				tpl.assign("QAID", "0");
 			}
 			tpl.assign("QWHEN", StackUtils.formatElapsedTime(mQuestion.getCreationDate()));
 			tpl.parse("main.post");
 			tpl.assign("FONTSIZE", mPreferences.getString(Const.PREF_FONTSIZE, Const.DEF_FONTSIZE));
 			tpl.parse("main");
 			mWebView.loadDataWithBaseURL("about:blank", tpl.out(), "text/html", "utf-8", null);
 			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 				if (mAnswerCount == 1) mAnswerLabel.setText(R.string.answer);
 				else mAnswerLabel.setText(R.string.answers);
 				mAnswerCountView.setText(String.valueOf(mAnswerCount));
 			}
 			else {
 				if (mAnswerCount == 1) mAnswerCountView.setText(mAnswerCount + " " + getString(R.string.answer));
 				else mAnswerCountView.setText(mAnswerCount + " " + getString(R.string.answers));
 			}
 			
 			mPreviousButton.setEnabled(false);
 			mNextButton.setEnabled(false);
 			if (mAnswerCount > 0) mNextButton.setEnabled(true);
 		}
 		else {
 			Template tpl = new Template(mTemplate);
 			tpl.assign("ENDPOINT", mEndpoint);
 			Answer answer = mAnswers.get(mCurAnswer);
 			try {
 				for (Comment c: answer.getComments()) {
 					tpl.assign("CBODY", c.getBody());
 					User owner = c.getOwner();
 					if (owner == null) {
 						tpl.assign("CAUTHOR", "?");
 						tpl.assign("CAID", "0");
 					}
 					else {
 						tpl.assign("CAUTHOR", owner.getDisplayName());
 						tpl.assign("CAID", String.valueOf(owner.getId()));
 					}
 					tpl.assign("CSCORE", String.valueOf(c.getScore()));
 					if (c.getScore() > 0) tpl.parse("main.post.comment.score");
 					tpl.parse("main.post.comment");
 				}
 			}
 			catch (Exception e) {
 				Log.e(Const.TAG, "wtf Answer.getComments() error", e);
 			}
 			User owner = answer.getOwner();
 			tpl.assign("QBODY", answer.getBody());
 			tpl.assign("QSCORE", String.valueOf(answer.getScore()));
 			// String.valueOf is needed because getEmailHash() returns null sometimes 
 			if (owner != null) {
 				tpl.assign("QAHASH", String.valueOf(owner.getEmailHash()));
 				tpl.assign("QANAME", owner.getDisplayName());
 				tpl.assign("QAREP", StackUtils.formatRep(owner.getReputation()));
 				tpl.assign("QAID", String.valueOf(owner.getId()));
 			}
 			else {
 				tpl.assign("QAHASH", "unknown");
 				tpl.assign("QANAME", "[unregistered]");
 				tpl.assign("QAREP", "0");
 				tpl.assign("QAID", "0");
 			}
 			tpl.assign("QWHEN", StackUtils.formatElapsedTime(answer.getCreationDate()));
 			if (answer.isAccepted()) tpl.parse("main.post.accepted");
 			tpl.parse("main.post");
 			tpl.assign("FONTSIZE", mPreferences.getString(Const.PREF_FONTSIZE, Const.DEF_FONTSIZE));
 			tpl.parse("main");
 			mWebView.loadDataWithBaseURL("about:blank", tpl.out(), "text/html", "utf-8", null);
 			
 			mPreviousButton.setEnabled(false);
 			mNextButton.setEnabled(false);
 			if (mCurAnswer > -1) mPreviousButton.setEnabled(true);
 			if (mCurAnswer < mAnswerCount-1) mNextButton.setEnabled(true);
 			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 				if (mAnswerCount == 1) mAnswerLabel.setText(R.string.answer);
 				else mAnswerLabel.setText(R.string.answers);
 				mAnswerCountView.setText((mCurAnswer+1) + "\nof\n" + mAnswerCount);
 			}
 			else {
 				mAnswerCountView.setText((mCurAnswer+1) + "/" + mAnswerCount);
 			}
 		}
 	}
 	
 	public void changePost(View target) {
 		switch(target.getId()) {
 		case R.id.next:
 			mCurAnswer++;
 			if (mCurAnswer >= mAnswers.size()) {
 				new FetchMoreAnswers().execute();
 			}
 			else {
 				updateView();
 			}
 			break;
 		case R.id.previous:
 			mCurAnswer--;
 			updateView();
 			break;
 		}
 	}
 	
 	private class FetchQuestionTask extends AsyncTask<Void, Void, Void> {
 		
 		private Exception mException;
 		
 		@Override
 		protected void onPreExecute() {
 			isRequestOngoing = true;
 			mPreviousButton.setEnabled(false);
 			mNextButton.setEnabled(false);
 			mProgress.setVisibility(View.VISIBLE);
 		}
 		
 		@Override
 		protected Void doInBackground(Void... params) {
 			try {
 				if (mQuestionID == 0) {
 					AnswerQuery aq = new AnswerQuery();
 					aq.setBody(false).setComments(false).setIds(mAnswerID);
 					Answer a = mAPI.getAnswers(aq).get(0);
 					mQuestionID = a.getQuestionId();
 				}
 				QuestionQuery query = new QuestionQuery();
 				query.setBody(true).setComments(true).setAnswers(false).setIds(mQuestionID);
 				mQuestion = mAPI.getQuestions(query).get(0);
 				mAnswerCount = mQuestion.getAnswerCount();
 			}
 			catch (Exception e) {
 				mException = e;
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			if (isFinishing()) return;
 			isRequestOngoing = false;
 			mProgress.setVisibility(View.GONE);
 			if (mException != null) {
 				new AlertDialog.Builder(QuestionActivity.this)
 					.setTitle(R.string.title_error)
 					.setMessage(R.string.question_fetch_error)
 					.setCancelable(false)
 					.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							finish();
 						}
 					}).create().show();
 				Log.e(Const.TAG, "Failed to get question", mException);
 			}
			updateView();
 		}
 		
 	}
 	
 	private class FetchMoreAnswers extends AsyncTask<Void, Void, List<Answer>> {
 		
 		private Exception mException;
 		
 		@Override
 		protected void onPreExecute() {
 			isRequestOngoing = true;
 			mPreviousButton.setEnabled(false);
 			mNextButton.setEnabled(false);
 			mProgress.setVisibility(View.VISIBLE);
 		}
 		
 		@Override
 		protected List<Answer> doInBackground(Void... params) {
 			try {
 				AnswerQuery query = new AnswerQuery();
 				query.setBody(true).setComments(true).setIds(mQuestionID);
 				query.setPageSize(mPageSize).setPage(++mPage);
 				query.setSort(AnswerQuery.Sort.votes());
 				return mAPI.getAnswersByQuestionId(query);
 			}
 			catch (Exception e) {
 				mException = e;
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(List<Answer> result) {
 			if (isFinishing()) return;
 			isRequestOngoing = false;
 			mProgress.setVisibility(View.GONE);
 			if (mException != null) {
 				new AlertDialog.Builder(QuestionActivity.this)
 					.setTitle(R.string.title_error)
 					.setMessage(R.string.more_answers_error)
 					.setNeutralButton(android.R.string.ok, null)
 					.create().show();
 				Log.e(Const.TAG, "Failed to get answers", mException);
 				mPage--;
 				mCurAnswer--;
 			}
 			else {
 				mAnswers.addAll(result);
 			}
 			updateView();
 		}
 		
 	}
 }
