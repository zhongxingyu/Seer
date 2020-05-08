 package org.droidstack;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.stackwrap4j.StackWrapper;
 import net.sf.stackwrap4j.entities.Answer;
 import net.sf.stackwrap4j.enums.Order;
 import net.sf.stackwrap4j.http.HttpClient;
 import net.sf.stackwrap4j.query.AnswerQuery;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class Answers extends Activity {
 	
 	public final static String TYPE_USER = "user";
 	
 	private StackWrapper mAPI;
 	private String mQueryType;
 	private String mEndpoint;
 	private String mSiteName;
 	private int mPage = 1;
 	private int mPageSize;
 	private int mUserID = 0;
 	private String mUserName;
 	private boolean mNoMoreAnswers = false;
 	private boolean mIsRequestOngoing = true;
 	private int mSort = -1;
 	private Order mOrder = Order.DESC;
 	private boolean mIsStartedForResult = false;
 	
 	private List<Answer> mAnswers;
 	private ArrayAdapter<Answer> mAdapter;
 	private ListView mListView;
 	
 	private Context mContext;
 	private Resources mResources;
 
 	private ArrayAdapter<CharSequence> mSortAdapter;
 	private ArrayAdapter<CharSequence> mOrderAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.answers);
 		
 		HttpClient.setTimeout(Const.NET_TIMEOUT);
 		mContext = this;
 		mResources = getResources();
 		mPageSize = getPreferences(Context.MODE_PRIVATE).getInt(Const.PREF_PAGESIZE, Const.DEF_PAGESIZE);
 		
		if (getIntent().getAction().equals(Intent.ACTION_PICK)) mIsStartedForResult = true; 
 		
 		Uri data = getIntent().getData();
 		mQueryType = data.getPathSegments().get(0);
 		mEndpoint = data.getQueryParameter("endpoint");
 		mSiteName = data.getQueryParameter("name");
 		
 		String titlePrefix = "";
 		if (mSiteName != null) titlePrefix = mSiteName + ": ";
 		if (mQueryType.equals(TYPE_USER)) {
 			mUserID = Integer.parseInt(data.getQueryParameter("uid"));
 			mUserName = data.getQueryParameter("uname");
 			if (mUserName == null) mUserName = "#" + String.valueOf(mUserID);
 			setTitle(titlePrefix + getString(R.string.title_user_answers).replace("%s", mUserName));
 			mSortAdapter = ArrayAdapter.createFromResource(this, R.array.a_sort_user, android.R.layout.simple_spinner_item);
 		}
 		
 		mAPI = new StackWrapper(mEndpoint, Const.APIKEY);
 		mAnswers = new ArrayList<Answer>();
 		mAdapter = new AnswersListAdapter<Answer>(mContext, 0, mAnswers);
 		mListView = (ListView) findViewById(R.id.answers);
 		mListView.setAdapter(mAdapter);
 		mListView.setOnScrollListener(onAnswersScrolled);
 		mListView.setOnItemClickListener(onAnswerClicked);
 		
 		mSortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		mOrderAdapter = ArrayAdapter.createFromResource(this, R.array.q_order, android.R.layout.simple_spinner_item);
 		mOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		
 		getAnswers();
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		if (mIsRequestOngoing == false) {
 	    	getMenuInflater().inflate(R.menu.answers, menu);
 	    	return true;
 		}
 		return false;
     }
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.menu_sort:
     		AlertDialog.Builder b = new AlertDialog.Builder(this);
     		b.setTitle(R.string.menu_sort);
     		View v = getLayoutInflater().inflate(R.layout.menu_sort, null);
     		final Spinner sort = (Spinner)v.findViewById(R.id.spinner_sort); 
     		sort.setAdapter(mSortAdapter);
     		if (mSort > -1) {
     			sort.setSelection(mSort);
     		}
     		final Spinner order = (Spinner) v.findViewById(R.id.spinner_order);
     		order.setAdapter(mOrderAdapter);
     		if (mOrder.equals(Order.DESC)) order.setSelection(0);
     		else order.setSelection(1);
     		b.setView(v);
     		b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					mSort = sort.getSelectedItemPosition();
 					if (order.getSelectedItemPosition() == 0) mOrder = Order.DESC;
 					else mOrder = Order.ASC;
 					mNoMoreAnswers = false;
 					mAnswers.clear();
 					mPage = 1;
 					getAnswers();
 				}
 			});
     		b.setNegativeButton(android.R.string.cancel, null);
     		b.create().show();
     		break;
     	}
     	return false;
     }
 	
 	private void getAnswers() {
 		new GetAnswersTask().execute();
 	}
 	
 	private class GetAnswersTask extends AsyncTask<Void, Void, List<Answer>> {
 		
 		private Exception mException;
 		
 		@Override
 		protected void onPreExecute() {
 			setProgressBarIndeterminateVisibility(true);
 			mIsRequestOngoing = true;
 		}
 		
 		@Override
 		protected List<Answer> doInBackground(Void... params) {
 			try {
 				if (mQueryType.equals(TYPE_USER)) {
 					AnswerQuery query = new AnswerQuery();
 					query.setBody(false).setPageSize(mPageSize).setPage(mPage);
 					query.setIds(mUserID);
 					query.setOrder(mOrder);
 					if (mSort > -1) {
 						switch(mSort) {
 						case 0: query.setSort(AnswerQuery.Sort.activity()); break;
 						case 1: query.setSort(AnswerQuery.Sort.views()); break;
 						case 2: query.setSort(AnswerQuery.Sort.creation()); break;
 						case 3: query.setSort(AnswerQuery.Sort.votes()); break;
 						}
 					}
 					return mAPI.getAnswersByUserId(query);
 				}
 			}
 			catch (Exception e) {
 				mException = e;
 			}
 			return null;
 		}
 		@Override
 		protected void onPostExecute(List<Answer> result) {
 			setProgressBarIndeterminateVisibility(false);
 			mIsRequestOngoing = false;
 			if (mException != null) {
 				new AlertDialog.Builder(mContext)
 					.setTitle(R.string.title_error)
 					.setMessage(R.string.answers_fetch_error)
 					.setCancelable(false)
 					.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							finish();
 						}
 					}).create().show();
 				Log.e(Const.TAG, "Failed to get answers", mException);
 			}
 			else {
 				if (result.size() < mPageSize) mNoMoreAnswers = true;
 				mAnswers.addAll(result);
 				mAdapter.notifyDataSetChanged();
 				if (mAnswers.size() == 0) {
 					findViewById(R.id.empty).setVisibility(View.VISIBLE);
 					mListView.setVisibility(View.GONE);
 				}
 			}
 		}
 		
 	}
 	
 	private class AnswersListAdapter<E> extends ArrayAdapter<E> {
 		
 		private LayoutInflater inflater;
 		
 		private class ViewHolder {
 			public TextView score;
 			public TextView title;
 		}
 		
 		public AnswersListAdapter(Context context, int textViewResourceId,
 				List<E> objects) {
 			super(context, textViewResourceId, objects);
 			inflater = getLayoutInflater();
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			Answer a = (Answer) getItem(position);
 			View v;
 			ViewHolder h;
 			
 			if (convertView == null) {
 				v = inflater.inflate(R.layout.answer_item, null);
 				h = new ViewHolder();
 				h.score = (TextView) v.findViewById(R.id.score);
 				h.title = (TextView) v.findViewById(R.id.title);
 				v.setTag(h);
 			}
 			else {
 				v = convertView;
 				h = (ViewHolder) v.getTag();
 			}
 			
 			h.score.setText(String.valueOf(a.getScore()));
 			h.title.setText(a.getTitle());
 			
 			if (a.isAccepted()) {
 				h.score.setBackgroundResource(R.color.score_max_bg);
 				h.score.setTextColor(mResources.getColor(R.color.score_max_text));
 			}
 			else if (a.getScore() == 0) {
 				h.score.setBackgroundResource(R.color.score_neutral_bg);
 				h.score.setTextColor(mResources.getColor(R.color.score_neutral_text));
 			}
 			else if (a.getScore() > 0) {
 				h.score.setBackgroundResource(R.color.score_high_bg);
 				h.score.setTextColor(mResources.getColor(R.color.score_high_text));
 			}
 			else {
 				h.score.setBackgroundResource(R.color.score_low_bg);
 				h.score.setTextColor(mResources.getColor(R.color.score_low_text));
 			}
 			
 			return v;
 		}
 		
 	}
 	
 	private OnItemClickListener onAnswerClicked = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			Answer a = mAnswers.get(position);
 			if (!mIsStartedForResult) {
 				Intent i = new Intent(mContext, ViewQuestion.class);
 				String uri = "droidstack://question" +
 					"?endpoint=" + Uri.encode(mEndpoint) +
 					"&qid=" + Uri.encode(String.valueOf(a.getQuestionId()));
 				i.setData(Uri.parse(uri));
 				startActivity(i);
 			}
 			else {
 				Intent i = new Intent();
 				i.putExtra("id", a.getPostId());
 				i.putExtra("qid", a.getQuestionId());
 				i.putExtra("title", a.getTitle());
 				i.putExtra("score", a.getScore());
 				i.putExtra("accepted", a.isAccepted());
 				setResult(RESULT_OK, i);
 				finish();
 			}
 		}
 		
 	};
 	
 	private OnScrollListener onAnswersScrolled = new OnScrollListener() {
 		
 		@Override
 		public void onScrollStateChanged(AbsListView view, int scrollState) {
 			// not used
 		}
 		
 		@Override
 		public void onScroll(AbsListView view, int firstVisibleItem,
 				int visibleItemCount, int totalItemCount) {
 			if (mIsRequestOngoing == false && mNoMoreAnswers == false && firstVisibleItem + visibleItemCount == totalItemCount) {
 				mPage++;
 				getAnswers();
 			}
 		}
 	};
 	
 }
