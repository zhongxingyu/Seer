 package org.droidstack;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import net.sf.stackwrap4j.StackWrapper;
 import net.sf.stackwrap4j.entities.Question;
 import net.sf.stackwrap4j.enums.Order;
 import net.sf.stackwrap4j.http.HttpClient;
 import net.sf.stackwrap4j.query.FavoriteQuery;
 import net.sf.stackwrap4j.query.QuestionQuery;
 import net.sf.stackwrap4j.query.SearchQuery;
 import net.sf.stackwrap4j.query.UnansweredQuery;
 import net.sf.stackwrap4j.query.UserQuestionQuery;
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
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class Questions extends Activity {
 	
 	public final static String TYPE_ALL = "all";
 	public final static String TYPE_UNANSWERED = "unanswered";
 	public final static String TYPE_USER = "user";
 	public final static String TYPE_FAVORITES = "favorites";
 	public final static String TYPE_SEARCH = "search";
 	
 	private StackWrapper mAPI;
 	private String mQueryType;
 	private String mEndpoint;
 	private String mSiteName;
 	private int mPage = 1;
 	private int mPageSize;
 	private int mUserID = 0;
 	private String mUserName;
 	private String mInTitle;
 	private String mTagged;
 	private String mNotTagged;
 	private boolean mNoMoreQuestions = false;
 	private boolean mIsRequestOngoing = true;
 	private int mSort = -1;
 	private Order mOrder = Order.DESC;
 	private boolean mIsStartedForResult = false;
 	
 	private Resources mResources;
 	private Context mContext;
 	
 	private List<Question> mQuestions;
 	private ArrayAdapter<Question> mAdapter;
 	private ListView mListView;
 	private ArrayAdapter<CharSequence> mSortAdapter;
 	private ArrayAdapter<CharSequence> mOrderAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.questions);
 		
 		HttpClient.setTimeout(Const.NET_TIMEOUT);
 		mResources = getResources();
 		mContext = (Context) this;
 		mPageSize = getPreferences(Context.MODE_PRIVATE).getInt(Const.PREF_PAGESIZE, Const.DEF_PAGESIZE);
 		
		if (getIntent().getAction().equals(Intent.ACTION_PICK)) mIsStartedForResult = true;
 		
 		Uri data = getIntent().getData();
 		mQueryType = data.getPathSegments().get(0);
 		mEndpoint = data.getQueryParameter("endpoint");
 		mSiteName = data.getQueryParameter("name");
 		
 		String titlePrefix = "";
 		if (mSiteName != null) titlePrefix = mSiteName + ": ";
 		if (mQueryType.equals(TYPE_ALL)) {
 			setTitle(titlePrefix + getString(R.string.title_all_questions));
 			mSortAdapter = ArrayAdapter.createFromResource(this, R.array.q_sort_all, android.R.layout.simple_spinner_item);
 		}
 		else if (mQueryType.equals(TYPE_UNANSWERED)) {
 			setTitle(titlePrefix + getString(R.string.title_all_unanswered));
 			mSortAdapter = ArrayAdapter.createFromResource(this, R.array.q_sort_unanswered, android.R.layout.simple_spinner_item);
 		}
 		else if (mQueryType.equals(TYPE_USER)) {
 			mUserID = Integer.parseInt(data.getQueryParameter("uid"));
 			mUserName = data.getQueryParameter("uname");
 			if (mUserName == null) mUserName = "#" + String.valueOf(mUserID);
 			setTitle(titlePrefix + getString(R.string.title_user_questions).replace("%s", mUserName));
 			mSortAdapter = ArrayAdapter.createFromResource(this, R.array.q_sort_user, android.R.layout.simple_spinner_item);
 		}
 		else if (mQueryType.equals(TYPE_FAVORITES)) {
 			mUserID = Integer.parseInt(data.getQueryParameter("uid"));
 			mUserName = data.getQueryParameter("uname");
 			if (mUserName == null) mUserName = "#" + String.valueOf(mUserID);
 			setTitle(titlePrefix + getString(R.string.title_user_favorites).replace("%s", mUserName));
 			mSortAdapter = ArrayAdapter.createFromResource(this, R.array.q_sort_favorites, android.R.layout.simple_spinner_item);
 		}
 		else if (mQueryType.equals(TYPE_SEARCH)) {
 			mInTitle = data.getQueryParameter("intitle");
 			mTagged = data.getQueryParameter("tagged");
 			mNotTagged = data.getQueryParameter("nottagged");
 			setTitle(titlePrefix + getString(R.string.title_search_results));
 			mSortAdapter = ArrayAdapter.createFromResource(this, R.array.q_sort_search, android.R.layout.simple_spinner_item);
 		}
 		
 		mAPI = new StackWrapper(mEndpoint, Const.APIKEY);
 		mSortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		mOrderAdapter = ArrayAdapter.createFromResource(this, R.array.q_order, android.R.layout.simple_spinner_item);
 		mOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		mQuestions = new ArrayList<Question>();
 		mAdapter = new QuestionsListAdapter<Question>(mContext, 0, mQuestions);
 		mListView = (ListView)findViewById(R.id.questions);
 		mListView.setAdapter(mAdapter);
 		mListView.setOnItemClickListener(onQuestionClicked);
 		mListView.setOnScrollListener(onQuestionsScrolled);
 		
 		getQuestions();
 		
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		if (mIsRequestOngoing == false) {
 	    	getMenuInflater().inflate(R.menu.questions, menu);
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
     		final Spinner order = (Spinner)v.findViewById(R.id.spinner_order);
     		order.setAdapter(mOrderAdapter);
     		b.setView(v);
     		b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					mSort = sort.getSelectedItemPosition();
 					if (order.getSelectedItemPosition() == 0) mOrder = Order.DESC;
 					else mOrder = Order.ASC;
 					mNoMoreQuestions = false;
 					mQuestions.clear();
 					mPage = 1;
 					getQuestions();
 				}
 			});
     		b.setNegativeButton(android.R.string.cancel, null);
     		b.create().show();
     		break;
     	}
     	return false;
     }
 	
 	private void getQuestions() {
 		mIsRequestOngoing = true;
 		setProgressBarIndeterminateVisibility(true);
 		new GetQuestionsAsync().execute();
 	}
 	
 	private class GetQuestionsAsync extends AsyncTask<Void, Void, List<Question>> {
 		private Exception mException;
 		
 		@Override
 		protected List<Question> doInBackground(Void... queries) {
 			try {
 				if (mQueryType.equals(TYPE_ALL)) {
 					QuestionQuery query = new QuestionQuery();
 					query.setBody(false).setPageSize(mPageSize).setPage(mPage);
 					query.setOrder(mOrder);
 					if (mSort > -1) {
 						switch(mSort) {
 						case 0: query.setSort(QuestionQuery.Sort.activity()); break;
 						case 1: query.setSort(QuestionQuery.Sort.votes()); break;
 						case 2: query.setSort(QuestionQuery.Sort.creation()); break;
 						case 3: query.setSort(QuestionQuery.Sort.featured()); break;
 						case 4: query.setSort(QuestionQuery.Sort.hot()); break;
 						case 5: query.setSort(QuestionQuery.Sort.week()); break;
 						case 6: query.setSort(QuestionQuery.Sort.month()); break;
 						}
 					}
 					return mAPI.listQuestions(query);
 				}
 				else if (mQueryType.equals(TYPE_UNANSWERED)) {
 					UnansweredQuery query = new UnansweredQuery();
 					query.setBody(false).setPageSize(mPageSize).setPage(mPage);
 					query.setOrder(mOrder);
 					if (mSort > -1) {
 						switch(mSort) {
 						case 0: query.setSort(UnansweredQuery.Sort.creation()); break;
 						case 1: query.setSort(UnansweredQuery.Sort.votes()); break;
 						}
 					}
 					return mAPI.listUnansweredQuestions(query);
 				}
 				else if (mQueryType.equals(TYPE_USER)) {
 					UserQuestionQuery query = new UserQuestionQuery();
 					query.setBody(false).setPageSize(mPageSize).setPage(mPage);
 					query.setIds(mUserID);
 					query.setOrder(mOrder);
 					if (mSort > -1) {
 						switch(mSort) {
 						case 0: query.setSort(UserQuestionQuery.Sort.activity()); break;
 						case 1: query.setSort(UserQuestionQuery.Sort.views()); break;
 						case 2: query.setSort(UserQuestionQuery.Sort.creation()); break;
 						case 3: query.setSort(UserQuestionQuery.Sort.votes()); break;
 						}
 					}
 					return mAPI.getQuestionsByUserId(query);
 				}
 				else if (mQueryType.equals(TYPE_FAVORITES)) {
 					FavoriteQuery query = new FavoriteQuery();
 					query.setBody(false).setPageSize(mPageSize).setPage(mPage);
 					query.setIds(mUserID);
 					query.setOrder(mOrder);
 					if (mSort > -1) {
 						switch(mSort) {
 						case 0: query.setSort(FavoriteQuery.Sort.activity()); break;
 						case 1: query.setSort(FavoriteQuery.Sort.views()); break;
 						case 2: query.setSort(FavoriteQuery.Sort.creation()); break;
 						case 3: query.setSort(FavoriteQuery.Sort.added()); break;
 						case 4: query.setSort(FavoriteQuery.Sort.votes()); break;
 						}
 					}
 					return mAPI.getFavoriteQuestionsByUserId(query);
 				}
 				else if (mQueryType.equals(TYPE_SEARCH)) {
 					SearchQuery query = new SearchQuery();
 					query.setPageSize(mPageSize).setPage(mPage);
 					if (mInTitle != null) query.setInTitle(mInTitle);
 					if (mTagged != null) query.setTags(mTagged.replace(' ', ';'));
 					if (mNotTagged != null) query.setNotTagged(mNotTagged.replace(' ', ';'));
 					query.setOrder(mOrder);
 					if (mSort > -1) {
 						switch(mSort) {
 						case 0: query.setSort(SearchQuery.Sort.activity()); break;
 						case 1: query.setSort(SearchQuery.Sort.views()); break;
 						case 2: query.setSort(SearchQuery.Sort.creation()); break;
 						case 3: query.setSort(SearchQuery.Sort.votes()); break;
 						}
 					}
 					return mAPI.search(query);
 				}
 			}
 			catch (Exception e) {
 				mException = e;
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(List<Question> result) {
 			setProgressBarIndeterminateVisibility(false);
 			mIsRequestOngoing = false;
 			if (mException != null) {
 				new AlertDialog.Builder(mContext)
 					.setTitle(R.string.title_error)
 					.setMessage(R.string.questions_fetch_error)
 					.setCancelable(false)
 					.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							finish();
 						}
 					}).create().show();
 				Log.e(Const.TAG, "Failed to get questions", mException);
 			}
 			else {
 				if (result.size() < mPageSize) mNoMoreQuestions = true;
 				mQuestions.addAll(result);
 				mAdapter.notifyDataSetChanged();
 				if (mQuestions.size() == 0) {
 					findViewById(R.id.empty).setVisibility(View.VISIBLE);
 					mListView.setVisibility(View.GONE);
 				}
 			}
 		}
 	}
 	
 	private class QuestionsListAdapter<E> extends ArrayAdapter<E> {
 		
 		LayoutInflater inflater;
 		private LinearLayout.LayoutParams tagLayout;
 		
 		private class ViewHolder {
 			public TextView title;
 			public TextView score;
 			public TextView answers;
 			public TextView answerLabel;
 			public TextView views;
 			public TextView bounty;
 			public LinearLayout tags;
 		}
 		
 		public QuestionsListAdapter(Context context, int textViewResourceId,
 				List<E> objects) {
 			super(context, textViewResourceId, objects);
 			inflater = getLayoutInflater();
 			tagLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
 			tagLayout.setMargins(0, 0, 5, 0);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			Question q = (Question) getItem(position);
 			View v;
 			TextView tagView;
 			ViewHolder h;
 			
 			if (convertView == null) {
 				v = inflater.inflate(R.layout.question_item, null);
 				h = new ViewHolder();
 				h.title = (TextView) v.findViewById(R.id.title);
 				h.score = (TextView) v.findViewById(R.id.votesN);
 				h.answers = (TextView) v.findViewById(R.id.answersN);
 				h.answerLabel = (TextView) v.findViewById(R.id.answersL);
 				h.views = (TextView) v.findViewById(R.id.viewsN);
 				h.bounty = (TextView) v.findViewById(R.id.bounty);
 				h.tags = (LinearLayout) v.findViewById(R.id.tags);
 				v.setTag(h);
 			}
 			else {
 				v = convertView;
 				h = (ViewHolder) convertView.getTag();
 			}
 			
 			h.title.setText(q.getTitle());
 			h.score.setText(String.valueOf(q.getScore()));
 			h.answers.setText(String.valueOf(q.getAnswerCount()));
 			h.views.setText(String.valueOf(q.getViewCount()));
 			
 			h.bounty.setVisibility(View.GONE);
 			if (q.getBountyAmount() > 0 && new Date(q.getBountyClosesDate()).before(new Date())) {
 				h.bounty.setText("+" + String.valueOf(q.getBountyAmount()));
 				h.bounty.setVisibility(View.VISIBLE);
 			}
 			
 			h.tags.removeAllViews();
 			for (String tag: q.getTags()){
 				tagView = (TextView) inflater.inflate(R.layout.tag, null);
 				tagView.setText(tag);
 				tagView.setOnClickListener(onTagClicked);
 				h.tags.addView(tagView, tagLayout);
 			}
 			
 			if (q.getAnswerCount() == 0) {
 				h.answers.setBackgroundResource(R.color.no_answers_bg);
 				h.answerLabel.setBackgroundResource(R.color.no_answers_bg);
 				h.answers.setTextColor(mResources.getColor(R.color.no_answers_text));
 				h.answerLabel.setTextColor(mResources.getColor(R.color.no_answers_text));
 			}
 			else {
 				h.answers.setBackgroundResource(R.color.some_answers_bg);
 				h.answerLabel.setBackgroundResource(R.color.some_answers_bg);
 				if (q.getAcceptedAnswerId() > 0) {
 					h.answers.setTextColor(mResources.getColor(R.color.answer_accepted_text));
 					h.answerLabel.setTextColor(mResources.getColor(R.color.answer_accepted_text));
 				}
 				else {
 					h.answers.setTextColor(mResources.getColor(R.color.some_answers_text));
 					h.answerLabel.setTextColor(mResources.getColor(R.color.some_answers_text));
 				}
 			}
 			
 			return v;
 		}
 		
 	}
 	
 	private OnClickListener onTagClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			String tag = ((TextView)v).getText().toString();
 			Log.d(Const.TAG, "Tag clicked: " + tag);
 		}
 	};
 	
 	private OnItemClickListener onQuestionClicked = new OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			Question q = mQuestions.get(position);
 			if (!mIsStartedForResult) {
 				Intent i = new Intent(mContext, ViewQuestion.class);
 				String uri = "droidstack://question" +
 					"?endpoint=" + Uri.encode(mEndpoint) +
 					"&qid=" + Uri.encode(String.valueOf(q.getPostId()));
 				i.setData(Uri.parse(uri));
 				startActivity(i);
 			}
 			else {
 				Intent i = new Intent();
 				i.putExtra("id", q.getPostId());
 				i.putExtra("title", q.getTitle());
 				i.putExtra("tags", q.getTags().toArray());
 				i.putExtra("score", q.getScore());
 				i.putExtra("answers", q.getAnswerCount());
 				i.putExtra("views", q.getViewCount());
 				i.putExtra("accepted", q.getAcceptedAnswerId());
 				setResult(RESULT_OK, i);
 				finish();
 			}
 		}
 	};
 	
 	private OnScrollListener onQuestionsScrolled = new OnScrollListener() {
 		
 		@Override
 		public void onScrollStateChanged(AbsListView view, int scrollState) {
 			// not used
 		}
 		
 		@Override
 		public void onScroll(AbsListView view, int firstVisibleItem,
 				int visibleItemCount, int totalItemCount) {
 			if (mIsRequestOngoing == false && mNoMoreQuestions == false && firstVisibleItem + visibleItemCount == totalItemCount) {
 				mPage++;
 				getQuestions();
 			}
 		}
 	};
 	
 }
