 package com.mili.xiaominglui.app.vello.ui;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.android.deskclock.widget.ActionableToastBar;
 import com.android.deskclock.widget.swipeablelistview.SwipeableListView;
 import com.atermenji.android.iconictextview.IconicTextView;
 import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
 import com.mili.xiaominglui.app.vello.R;
 import com.mili.xiaominglui.app.vello.config.VelloConfig;
 import com.mili.xiaominglui.app.vello.data.factory.MiliDictionaryJsonParser;
 import com.mili.xiaominglui.app.vello.data.model.Definition;
 import com.mili.xiaominglui.app.vello.data.model.Definitions;
 import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
 import com.mili.xiaominglui.app.vello.data.model.Phonetics;
 import com.mili.xiaominglui.app.vello.data.model.Phoneticss;
 import com.mili.xiaominglui.app.vello.data.model.WordCard;
 import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
 import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
 import com.mili.xiaominglui.app.vello.util.AccountUtils;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.TimeZone;
 
 public class ReviewViewFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {
 	private static final String TAG = ReviewViewFragment.class.getSimpleName();
 	
 	private static final String KEY_DELETED_WORD = "deletedWord";
 	private static final String KEY_UNDO_SHOWING = "undoShowing";
 	
 	private WordCard mDeletedWord;
 	private boolean mUndoShowing = false;
 	private ActionableToastBar mUndoBar;
 
 	private SwipeableListView mWordsList;
 	private WordCardAdapter mAdapter;
 	private ViewGroup mRootView;
 	private onStatusChangedListener mListener;
 
 	private String mCurFilter = "";
 	private boolean mIsSearching = false;
 	
 	private SwipeableListView.OnItemSwipeListener mReviewSwipeListener = new SwipeableListView.OnItemSwipeListener() {
         
         @Override
         public void onSwipe(View view) {
             final WordCardAdapter.ItemHolder itemHolder = (WordCardAdapter.ItemHolder) view.getTag();
             // if wordcard expanded, do NOT mark reviewed plus
             if (!mAdapter.isWordExpanded(itemHolder.wordcard)) {
                 asyncMarkDeleteWord(itemHolder.wordcard);
                 mListener.onWordReviewed();
             } else {
                 // review failed
                 asyncDeleteWordCache(itemHolder.wordcard);
             }
         }
     };
     
 	public interface onStatusChangedListener {
 		public void onModeChanged(int modeColor);
 		public void onAllReviewed();
 		public void onWordReviewed();
 	}
 	
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			mListener = (onStatusChangedListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString() + " must implement onStatusChangedListener");
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		if (savedInstanceState != null) {
 			mDeletedWord = savedInstanceState.getParcelable(KEY_DELETED_WORD);
 			mUndoShowing = savedInstanceState.getBoolean(KEY_UNDO_SHOWING);
 		}
 		
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putParcelable(KEY_DELETED_WORD, mDeletedWord);
 		outState.putBoolean(KEY_UNDO_SHOWING, mUndoShowing);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_review, null);
 		
 		mWordsList = (SwipeableListView) mRootView.findViewById(R.id.words_list);
 		mAdapter = new WordCardAdapter(getActivity(), null, mWordsList);
 		mWordsList.setAdapter(mAdapter);
 		mWordsList.setVerticalScrollBarEnabled(true);
 		mWordsList.setOnCreateContextMenuListener(this);
 
 		mWordsList.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View view, MotionEvent event) {
 				hideUndoBar(true, event);
 				return false;
 			}
 		});
 
 		mUndoBar = (ActionableToastBar) mRootView.findViewById(R.id.undo_bar);
 
 		if (mUndoShowing) {
 			mUndoBar.show(new ActionableToastBar.ActionClickedListener() {
 				@Override
 				public void onActionClicked() {
 					asyncUnmarkDeleteWord(mDeletedWord);
 					mDeletedWord = null;
 					mUndoShowing = false;
 				}
 			}, 0, getResources().getString(R.string.word_reviewed), true,
 					R.string.word_reviewed_undo, true);
 		}
 		return mRootView;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		getLoaderManager().initLoader(0, null, this);
 	}
 	
 	public class WordCardAdapter extends CursorAdapter {
 		private final Context mContext;
 		private final LayoutInflater mFactory;
 		private final ListView mList;
 
 		private final HashSet<Integer> mExpanded = new HashSet<Integer>();
 		private final int[] mWordCardBackgroundColor = { R.color.bg_color_new,
 				R.color.bg_color_1st, R.color.bg_color_2nd,
 				R.color.bg_color_3rd, R.color.bg_color_4th,
 				R.color.bg_color_5th, R.color.bg_color_6th,
 				R.color.bg_color_7th, R.color.bg_color_8th };
 
 		public class ItemHolder {
 			// views for optimization
 			LinearLayout wordCardItem;
 			IconicTextView iconicLifeCount;
 			TextView textViewLifeCount;
 			TextView textViewKeyword;
 
 			View expandArea;
 			View infoArea;
 
 			LinearLayout linearLayoutPhoneticArea;
 			LinearLayout linearLayoutDefinitionArea;
 
 			View hairLine;
 
 			// Other states
 			WordCard wordcard;
 			IcibaWord word;
 			String idList;
 			Phoneticss p;
 			Definitions d;
 		}
 
 		// Used for scrolling an expanded item in the list to make sure it is
 		// fully visible.
 		private int mScrollWordId = -1;
 		private final Runnable mScrollRunnable = new Runnable() {
 
 			@Override
 			public void run() {
 				if (mScrollWordId != -1) {
 					View v = getViewById(mScrollWordId);
 					if (v != null) {
 						Rect rect = new Rect(v.getLeft(), v.getTop(),
 								v.getRight(), v.getBottom());
 						mList.requestChildRectangleOnScreen(v, rect, false);
 					}
 					mScrollWordId = -1;
 				}
 			}
 		};
 
 		public WordCardAdapter(Context context, int[] expandedIds, ListView list) {
 			super(context, null, 0);
 			mContext = context;
 			mFactory = LayoutInflater.from(context);
 			mList = list;
 
 			if (expandedIds != null) {
 				buildHashSetFromArray(expandedIds, mExpanded);
 			}
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (!getCursor().moveToPosition(position)) {
 				// May happen if the last word was deleted and the cursor
 				// refreshed while the
 				// list is updated.
 				Log.v(TAG, "couldn't move cursor to position " + position);
 				return null;
 			}
 			View v;
 			if (convertView == null) {
 				v = newView(mContext, getCursor(), parent);
 			} else {
 				// Do a translation check to test for animation. Change this to
 				// something more
 				// reliable and robust in the future.
 				if (convertView.getTranslationX() != 0
 						|| convertView.getTranslationY() != 0) {
 					// view was animated, reset
 					v = newView(mContext, getCursor(), parent);
 				} else {
 					v = convertView;
 				}
 			}
 			bindView(v, mContext, getCursor());
 			return v;
 		}
 
 		@Override
 		public void bindView(View view, Context context, Cursor cursor) {
 			final WordCard wordcard = new WordCard(cursor);
 			final ItemHolder itemHolder = (ItemHolder) view.getTag();
 			itemHolder.wordcard = wordcard;
 //			itemHolder.word = IcibaWordXmlParser.parse(wordcard.desc);
 			itemHolder.word = MiliDictionaryJsonParser.parse(wordcard.desc);
 			itemHolder.iconicLifeCount.setIcon(FontAwesomeIcon.CHECK);
 			itemHolder.iconicLifeCount.setTextColor(Color.GRAY);
 			itemHolder.iconicLifeCount.setVisibility(mIsSearching ? View.GONE : View.VISIBLE);
 			itemHolder.idList = itemHolder.wordcard.idList;
 			int positionList = AccountUtils.getVocabularyListPosition(mContext,
 					itemHolder.idList);
 			if (mIsSearching) {
 				itemHolder.wordCardItem.setBackgroundResource(R.color.bg_dictionary_mode);
 			} else {
 				itemHolder.wordCardItem.setBackgroundResource(mWordCardBackgroundColor[positionList]);
 			}
 
 			itemHolder.textViewLifeCount.setText(String.valueOf(positionList) + "/9");
 			itemHolder.textViewLifeCount.setTextColor(Color.GRAY);
 			itemHolder.textViewLifeCount.setVisibility(mIsSearching ? View.GONE : View.VISIBLE);
 
 			itemHolder.textViewKeyword.setText(itemHolder.wordcard.name);
 
 			itemHolder.expandArea
 					.setVisibility(isWordExpanded(wordcard) ? View.VISIBLE
 							: View.GONE);
 			itemHolder.infoArea
 					.setVisibility(!isWordExpanded(wordcard) || mIsSearching ? View.VISIBLE
 							: View.GONE);
 			itemHolder.infoArea.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View view) {
 					expandWord(itemHolder);
 					itemHolder.wordCardItem.post(mScrollRunnable);
 				}
 			});
 
 			if (isWordExpanded(wordcard) && !mIsSearching) {
 				expandWord(itemHolder);
 			}
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			final View view = mFactory.inflate(R.layout.word_card_item, parent,
 					false);
 
 			// standard view holder optimization
 			final ItemHolder holder = new ItemHolder();
 			holder.wordCardItem = (LinearLayout) view
 					.findViewById(R.id.word_card_item);
 			holder.iconicLifeCount = (IconicTextView) view
 					.findViewById(R.id.life_sign);
 			holder.textViewLifeCount = (TextView) view
 					.findViewById(R.id.life_count);
 			holder.textViewKeyword = (TextView) view.findViewById(R.id.keyword);
 			holder.expandArea = view.findViewById(R.id.expand_area);
 			holder.infoArea = view.findViewById(R.id.info_area);
 			holder.linearLayoutPhoneticArea = (LinearLayout) view
 					.findViewById(R.id.phonetics_area);
 			holder.linearLayoutDefinitionArea = (LinearLayout) view
 					.findViewById(R.id.definition_area);
 			holder.hairLine = view.findViewById(R.id.hairline);
 
 			view.setTag(holder);
 			return view;
 		}
 
 		/**
 		 * Expands the word for studying.
 		 * 
 		 * @param itemHolder
 		 *            The item holder instance.
 		 */
 		private void expandWord(ItemHolder itemHolder) {
 			itemHolder.expandArea.setVisibility(View.VISIBLE);
 			itemHolder.expandArea
 					.setOnClickListener(new View.OnClickListener() {
 						@Override
 						public void onClick(View view) {
 							// When action mode is on - simulate long click
 							// doLongClick(view);
 						}
 					});
 			itemHolder.infoArea.setVisibility(View.GONE);
 
 			if (!mIsSearching) {
 				mExpanded.add(itemHolder.wordcard.idInLocalDB);
 			}
 
 			bindExpandArea(itemHolder, itemHolder.wordcard);
 			// Scroll the view to make sure it is fully viewed
 			mScrollWordId = itemHolder.wordcard.idInLocalDB;
 		}
 
 		private void bindExpandArea(final ItemHolder itemHolder,
 				final WordCard wordcard) {
 			// Views in here are not bound until the item is expanded.
 			itemHolder.p = itemHolder.word.phonetics;
 			itemHolder.linearLayoutPhoneticArea.removeAllViews();
 			for (Phonetics phonetics : itemHolder.p) {
 				View phoneticsView = LayoutInflater.from(mContext).inflate(
 						R.layout.phonetics_item, null);
 				LinearLayout phoneticsGroup = (LinearLayout) phoneticsView
 						.findViewById(R.id.phonetics_group);
 				((TextView) phoneticsView.findViewById(R.id.phonetics_symbol))
 						.setText("[" + phonetics.symbol + "]");
 				((IconicTextView) phoneticsView
 						.findViewById(R.id.phonetics_sound))
 						.setIcon(FontAwesomeIcon.VOLUME_UP);
 				((IconicTextView) phoneticsView
 						.findViewById(R.id.phonetics_sound))
 						.setTextColor(Color.GRAY);
 				itemHolder.linearLayoutPhoneticArea.addView(phoneticsGroup);
 			}
 
 			itemHolder.d = itemHolder.word.definition;
 			itemHolder.linearLayoutDefinitionArea.removeAllViews();
 			for (Definition definition : itemHolder.d) {
 				View definitionView = LayoutInflater.from(mContext).inflate(
 						R.layout.definition_item, null);
 				LinearLayout definiitionGroup = (LinearLayout) definitionView
 						.findViewById(R.id.definition_group);
 				((TextView) definitionView.findViewById(R.id.pos))
 						.setText(definition.pos);
 				((TextView) definitionView.findViewById(R.id.definiens))
 						.setText(definition.definiens);
 				itemHolder.linearLayoutDefinitionArea.addView(definiitionGroup);
 			}
 		}
 
 		private boolean isWordExpanded(WordCard wordcard) {
 			return mExpanded.contains(wordcard.idInLocalDB);
 		}
 
 		private View getViewById(int id) {
 			for (int i = 0; i < mList.getCount(); i++) {
 				View v = mList.getChildAt(i);
 				if (v != null) {
 					ItemHolder h = (ItemHolder) (v.getTag());
 					if (h != null && h.wordcard.idInLocalDB == id) {
 						return v;
 					}
 				}
 			}
 			return null;
 		}
 
 		public int[] getExpandedArray() {
 			final int[] ids = new int[mExpanded.size()];
 			int index = 0;
 			for (int id : mExpanded) {
 				ids[index] = id;
 				index++;
 			}
 			return ids;
 		}
 
         public void clearExpandedArray() {
             mExpanded.clear();
         }
 
 		private void buildHashSetFromArray(int[] ids, HashSet<Integer> set) {
 			for (int id : ids) {
 				set.add(id);
 			}
 		}
 	}
 
 	@SuppressLint("SimpleDateFormat")
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		// show all open WordCards whose due time bigger than mobile local now
 		// time && syncInNext mark not set
 		ProviderCriteria criteria = new ProviderCriteria();
 		if (TextUtils.isEmpty(mCurFilter)) {
 			// Review Mode
 		    mIsSearching = false;
 		    mListener.onModeChanged(VelloConfig.REVIEW_MODE_ACTION_BAR_COLOR);
 		    mWordsList.enableSwipe(true);
 		    mWordsList.setOnItemSwipeListener(mReviewSwipeListener);
 			criteria.addSortOrder(DbWordCard.Columns.DUE, true);
 			Calendar rightNow = Calendar.getInstance();
 
 			SimpleDateFormat format = new SimpleDateFormat(
 					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 			long rightNowUnixTime = rightNow.getTimeInMillis();
 			long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
 			String now = format.format(new Date(rightNowUnixTimeGMT));
 			criteria.addLt(DbWordCard.Columns.DUE, now, true);
 			criteria.addNe(DbWordCard.Columns.SYNCINNEXT, "true");
 		} else {
 			// Dictionary Mode
 		    mIsSearching = true;
 		    mListener.onModeChanged(VelloConfig.DICTIONARY_MODE_ACTION_BAR_COLOR);
 		    mWordsList.enableSwipe(false);
 		    mWordsList.setOnItemSwipeListener(null);
 		    mWordsList.setEmptyView(null);
 		    criteria.addLike(DbWordCard.Columns.NAME, mCurFilter + "%");
 		}
 		return new CursorLoader(getActivity(), DbWordCard.CONTENT_URI,
 				DbWordCard.PROJECTION, criteria.getWhereClause(),
 				criteria.getWhereParams(), criteria.getOrderClause());
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 	    mAdapter.clearExpandedArray();
 		mAdapter.swapCursor(data);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loder) {
 		mAdapter.swapCursor(null);
 	}
 	
 	private void asyncDeleteWordCache(WordCard wordcard) {
 		Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI, wordcard.idInLocalDB);
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "uri---" + uri.toString() + " is to be deleted");
 		}
 		getActivity().getContentResolver().delete(uri, null, null);
 	}
 	
 	private void asyncUnmarkDeleteWord(final WordCard wordcard) {
 		ContentValues cv = new ContentValues();
 		cv.put(DbWordCard.Columns.CLOSED.getName(), wordcard.closed);
 		cv.put(DbWordCard.Columns.DUE.getName(), wordcard.due);
 		cv.put(DbWordCard.Columns.ID_LIST.getName(), wordcard.idList);
 		cv.put(DbWordCard.Columns.SYNCINNEXT.getName(), "false");
 		Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI,
 				wordcard.idInLocalDB);
 		getActivity().getContentResolver().update(uri, cv, null, null);
 	}
 
 	@SuppressLint("SimpleDateFormat")
 	private void asyncMarkDeleteWord(final WordCard wordcard) {
 		final AsyncTask<WordCard, Void, Void> deleteTask = new AsyncTask<WordCard, Void, Void>() {
 
 			@Override
 			protected Void doInBackground(WordCard... wordcards) {
 				for (final WordCard wordcard : wordcards) {
 					ContentValues cv = new ContentValues();
 					int positionList = AccountUtils.getVocabularyListPosition(
 							getActivity(), wordcard.idList);
 					if (positionList == VelloConfig.VOCABULARY_LIST_POSITION_8TH) {
 						cv.put(DbWordCard.Columns.CLOSED.getName(), "true");
 					} else {
 						Calendar rightNow = Calendar.getInstance();
 						long rightNowUnixTime = rightNow.getTimeInMillis();
 						long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
 						long delta = VelloConfig.VOCABULARY_LIST_DUE_DELTA[positionList];
 						long dueUnixTime = rightNowUnixTimeGMT + delta;
 
 						SimpleDateFormat format = new SimpleDateFormat(
 								"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 						Date dueDate = new Date(dueUnixTime);
 						String stringDueDate = format.format(dueDate);
 						cv.put(DbWordCard.Columns.DUE.getName(), stringDueDate);
 
 						String newIdList = AccountUtils.getVocabularyListId(
 								getActivity(), positionList + 1);
 						cv.put(DbWordCard.Columns.ID_LIST.getName(), newIdList);
 					}
 					cv.put(DbWordCard.Columns.SYNCINNEXT.getName(), "true");
 					Uri uri = ContentUris.withAppendedId(
 							DbWordCard.CONTENT_URI, wordcard.idInLocalDB);
 					getActivity().getContentResolver().update(uri, cv, null, null);
 				}
 				return null;
 			}
 		};
 		mDeletedWord = wordcard;
 		mUndoShowing = true;
 		deleteTask.execute(wordcard);
 		mUndoBar.show(new ActionableToastBar.ActionClickedListener() {
 			@Override
 			public void onActionClicked() {
 				asyncUnmarkDeleteWord(wordcard);
 				mDeletedWord = null;
 				mUndoShowing = false;
 			}
 		}, 0, getResources().getString(R.string.word_reviewed), true,
 				R.string.word_reviewed_undo, true);
 	}
 
 	private void hideUndoBar(boolean animate, MotionEvent event) {
 		if (mUndoBar != null) {
 			if (event != null && mUndoBar.isEventInToastBar(event)) {
 				// Avoid touches inside the undo bar.
 				return;
 			}
 			mUndoBar.hide(animate);
 		}
 		mDeletedWord = null;
 		mUndoShowing = false;
 	}
 	
 	void onQueryTextChange(String newText) {
 		mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
 		getLoaderManager().restartLoader(0, null, this);
 	}
 }
