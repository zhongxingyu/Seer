 /*
  Dubsar Dictionary Project
  Copyright (C) 2010-11 Jimmy Dee
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package com.dubsar_dictionary.Dubsar;
 
 import java.lang.ref.WeakReference;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.BaseColumns;
import android.text.method.ScrollingMovementMethod;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 
 /**
  * 
  * Sense activity
  *
  */
 public class SenseActivity extends DubsarActivity {
 	public static final String SENSE_SYNONYM_IDS = "sense_synonym_ids";
 	public static final String SENSE_SYNONYM_NAMES = "sense_synonym_names";
 	public static final String SENSE_SYNONYM_MARKERS = "sense_synonym_markers";
 	public static final String SENSE_VERB_FRAME_IDS = "sense_verb_frame_ids";
 	public static final String SENSE_VERB_FRAMES = "sense_verb_frames";
 	public static final String SENSE_SAMPLE_IDS = "sense_sample_ids";
 	public static final String SENSE_SAMPLES = "sense_samples";
 
 	private TextView mTitle=null;
 	private TextView mBanner=null;
 	private TextView mGlossView=null;
 	private ExpandableListView mLists=null;
 	private MenuItem mWordMenuItem=null;
 	private MenuItem mSynsetMenuItem=null;
 	
 	private volatile int mWordId=0;
 	private volatile int mSynsetId=0;
 	private volatile String mNameAndPos=null;
 	private volatile String mGloss=null;
 	private volatile String mSubtitle=null;
 	private volatile String mPos=null;
 	
 	private int mSynonymCount=0;
 	private int mVerbFrameCount=0;
 	private int mSampleCount=0;
 	private int mPointerCount=0;
 	private volatile Cursor mResult;
 	private volatile SenseExpandableListAdapter mAdapter=null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState, R.layout.sense);
 		
 		mTitle = (TextView)findViewById(R.id.sense_title);
 		mBanner = (TextView)findViewById(R.id.sense_banner);
 		mGlossView = (TextView)findViewById(R.id.sense_gloss);
 		mLists = (ExpandableListView)findViewById(R.id.sense_lists);
		
		mGlossView.setMovementMethod(new ScrollingMovementMethod());
 				
 		setupFonts();
 		
 		Intent intent = getIntent();
 		Uri uri = intent.getData();
 		Bundle extras = intent.getExtras();
 		if (extras != null) {
 			mNameAndPos = extras.getString(DubsarContentProvider.SENSE_NAME_AND_POS);
 			if (mNameAndPos != null) mTitle.setText(mNameAndPos);
 		}
 		
 		if (savedInstanceState != null) {
 			retrieveInstanceState(savedInstanceState);
 		}
 		
 		if (mResult != null) {
 			populateData();
 		}
 		else {		
 			if (!checkNetwork()) return;
 			new SenseQuery(this).execute(uri);
 		}
 	}
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.sense_options_menu, menu);
         mWordMenuItem = menu.findItem(R.id.word);
         mSynsetMenuItem = menu.findItem(R.id.synset);
         
         if (mWordId != 0 || mSynsetId != 0) {
         	mWordMenuItem.setEnabled(true);
         	mSynsetMenuItem.setEnabled(true);
         }
        return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
     		case R.id.home:
     			startMainActivity();
     			return true;
             case R.id.search:
                 onSearchRequested();
                 return true;
             case R.id.word:
             	requestWord();
             	return true;
             case R.id.synset:
             	requestSynset();
             	return true;
             default:
                 return false;
         }
     }
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		saveState(outState);
 	}
     
 	protected void reportError(String error) {
 		super.reportError(error);
 		
 		mTitle.setText(error);
         mTitle.setBackgroundResource(R.drawable.rounded_orange_rectangle);
 
 		mBanner.setVisibility(View.GONE);
 		mGlossView.setVisibility(View.GONE);
 		mLists.setVisibility(View.GONE);
 	}
 	
 	protected void setupFonts() {
 		setBoldItalicTypeface(mBanner);
 	}
 
 	protected void requestWord() {    	
     	Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
     	wordIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS, mNameAndPos);
 
     	Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
                                         DubsarContentProvider.WORDS_URI_PATH + 
                                         "/" + mWordId);
         wordIntent.setData(data);
         startActivity(wordIntent);
 	}
 	
 	protected void requestSynset() {
 		Intent synsetIntent = new Intent(getApplicationContext(), SynsetActivity.class);
 		Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
 				DubsarContentProvider.SYNSETS_URI_PATH + "/" + mSynsetId);
 		
 		synsetIntent.setData(data);
 		startActivity(synsetIntent);
 	} 
 	
 	protected void setWordId(int wordId) {
 		mWordId = wordId;
 	}
 	
 	protected void setSynsetId(int synsetId) {
 		mSynsetId = synsetId;
 	}
 	
 	protected void retrieveInstanceState(Bundle inState) {
 
 		/* Seems to do this without me. Plus, this crashes with a NPE inside
 		 * expandGroup().
 		boolean[] expanded = inState.getBooleanArray(EXPANDED);
 		for (int j=0; j<expanded.length; ++j) {
 			if (expanded[j]) {
 				mLists.expandGroup(j);
 			}
 		}
 		 */
 			
 		mSubtitle = inState.getString(DubsarContentProvider.SENSE_SUBTITLE);
 		if (mSubtitle == null) return;
 		
 		mPos = inState.getString(DubsarContentProvider.SENSE_POS);
 		mNameAndPos = inState.getString(DubsarContentProvider.SENSE_NAME_AND_POS);
 		mGloss = inState.getString(DubsarContentProvider.SENSE_GLOSS);
 		mWordId = inState.getInt(DubsarContentProvider.SENSE_WORD_ID);
 		mSynsetId = inState.getInt(DubsarContentProvider.SENSE_SYNSET_ID);
 		
 		setupResultCursor(inState);
 		unbundleSynonyms(inState);
 		unbundleVerbFrames(inState);
 		unbundleSamples(inState);
 		unbundlePointers(inState);
 	}
 	
 	protected void saveState(Bundle outState) {
 		if (mResult == null) return;
 		
 		if (mAdapter != null) {
 			outState.putBooleanArray(EXPANDED, mAdapter.getExpanded());
 		}
 		outState.putString(DubsarContentProvider.SENSE_SUBTITLE, mSubtitle);
 		outState.putString(DubsarContentProvider.SENSE_GLOSS, mGloss);
 		outState.putString(DubsarContentProvider.SENSE_POS, mPos);
 		outState.putString(DubsarContentProvider.SENSE_NAME_AND_POS, mNameAndPos);
 		outState.putInt(DubsarContentProvider.SENSE_WORD_ID, mWordId);
 		outState.putInt(DubsarContentProvider.SENSE_SYNSET_ID, mSynsetId);
 		
 		bundleSynonyms(outState);
 		bundleVerbFrames(outState);
 		bundleSamples(outState);
 		bundlePointers(outState);
 	}
 	
 	protected void saveResults(Cursor result) {
 		int posColumn = result.getColumnIndex(DubsarContentProvider.SENSE_POS);
 		int nameAndPosColumn = result.getColumnIndex(DubsarContentProvider.SENSE_NAME_AND_POS);
 		int subtitleColumn = result.getColumnIndex(DubsarContentProvider.SENSE_SUBTITLE);
 		int glossColumn = result.getColumnIndex(DubsarContentProvider.SENSE_GLOSS);
 		int wordIdColumn = result.getColumnIndex(DubsarContentProvider.SENSE_WORD_ID);
 		int synsetIdColumn = result.getColumnIndex(DubsarContentProvider.SENSE_SYNSET_ID);
 		
 		int synonymCountColumn = result.getColumnIndex(DubsarContentProvider.SENSE_SYNONYM_COUNT);
 		int verbFrameCountColumn = result.getColumnIndex(DubsarContentProvider.SENSE_VERB_FRAME_COUNT);
 		int sampleCountColumn = result.getColumnIndex(DubsarContentProvider.SENSE_SAMPLE_COUNT);
 		int pointerCountColumn = result.getColumnIndex(DubsarContentProvider.POINTER_COUNT);
 
 		result.moveToFirst();
 		mPos = result.getString(posColumn);
 		mNameAndPos = result.getString(nameAndPosColumn);
 		mSubtitle = result.getString(subtitleColumn);
 		mGloss = result.getString(glossColumn);
 		mWordId = result.getInt(wordIdColumn);
 		mSynsetId = result.getInt(synsetIdColumn);
 		
 		mSynonymCount = result.getInt(synonymCountColumn);
 		mVerbFrameCount = result.getInt(verbFrameCountColumn);
 		mSampleCount = result.getInt(sampleCountColumn);
 		mPointerCount = result.getInt(pointerCountColumn);
 
 		mResult = result;
 	}
 	
 	protected void populateData() {
 		mTitle.setText(mNameAndPos);
 		mBanner.setText(mSubtitle);
 		mGlossView.setText(mGloss);
 		
 		hideLoadingSpinner();
 		
 		// set up the expandable list view
 		mAdapter = new SenseExpandableListAdapter(this, mResult);
 		mLists.setAdapter(mAdapter);
 		mLists.setVisibility(View.VISIBLE);
 
 		if (mWordMenuItem == null || mSynsetMenuItem == null) return;
 		
 		mWordMenuItem.setEnabled(true);
 		mSynsetMenuItem.setEnabled(true);
 	}
 	
 	protected void bundleSynonyms(Bundle outState) {
 		int synonymCountColumn = 
 				mResult.getColumnIndex(DubsarContentProvider.SENSE_SYNONYM_COUNT);
 		int idColumn = mResult.getColumnIndex(BaseColumns._ID);
 		int nameColumn = mResult.getColumnIndex(DubsarContentProvider.SENSE_SYNONYM);
 		int markerColumn = mResult.getColumnIndex(DubsarContentProvider.SENSE_SYNONYM_MARKER);
 		
 		mResult.moveToFirst();
 		mSynonymCount = mResult.getInt(synonymCountColumn);
 		
 		int[] ids = new int[mSynonymCount];
 		String[] names = new String[mSynonymCount];
 		String[] markers = new String[mSynonymCount];
 		
 		outState.putInt(DubsarContentProvider.SENSE_SYNONYM_COUNT, mSynonymCount);
 		for (int j=0; j<mSynonymCount; ++j) {
 			mResult.moveToPosition(j);
 			
 			ids[j] = mResult.getInt(idColumn);
 			names[j] = mResult.getString(nameColumn);
 			markers[j] = mResult.getString(markerColumn);
 		}
 		
 		outState.putIntArray(SENSE_SYNONYM_IDS, ids);
 		outState.putStringArray(SENSE_SYNONYM_NAMES, names);
 		outState.putStringArray(SENSE_SYNONYM_MARKERS, markers);
 	}
 	
 	protected void bundleVerbFrames(Bundle outState) {
 		int verbFrameCountColumn = mResult.getColumnIndex(DubsarContentProvider.SENSE_VERB_FRAME_COUNT);
 		int idColumn = mResult.getColumnIndex(BaseColumns._ID);
 		int verbFrameColumn = mResult.getColumnIndex(DubsarContentProvider.SENSE_VERB_FRAME);
 		
 		mResult.moveToFirst();
 		mVerbFrameCount = mResult.getInt(verbFrameCountColumn);
 		
 		int[] ids = new int[mVerbFrameCount];
 		String[] frames = new String[mVerbFrameCount];
 		
 		outState.putInt(DubsarContentProvider.SENSE_VERB_FRAME_COUNT, mVerbFrameCount);
 		for (int j=0; j<mVerbFrameCount; ++j) {
 			mResult.moveToPosition(mSynonymCount+j);
 			ids[j] = mResult.getInt(idColumn);
 			frames[j] = mResult.getString(verbFrameColumn);
 		}
 		
 		outState.putIntArray(SENSE_VERB_FRAME_IDS, ids);
 		outState.putStringArray(SENSE_VERB_FRAMES, frames);
 	}
 	
 	protected void bundleSamples(Bundle outState) {
 		int sampleCountColumn = mResult.getColumnIndex(DubsarContentProvider.SENSE_SAMPLE_COUNT);
 		int idColumn = mResult.getColumnIndex(BaseColumns._ID);
 		int sampleColumn = mResult.getColumnIndex(DubsarContentProvider.SENSE_SAMPLE);
 		
 		mResult.moveToFirst();
 		mSampleCount = mResult.getInt(sampleCountColumn);
 		
 		int[] ids = new int[mSampleCount];
 		String[] samples = new String[mSampleCount];
 		
 		outState.putInt(DubsarContentProvider.SENSE_SAMPLE_COUNT, mSampleCount);
 		for (int j=0; j<mSampleCount; ++j) {
 			mResult.moveToPosition(mSynonymCount+mVerbFrameCount+j);
 			ids[j] = mResult.getInt(idColumn);
 			samples[j] = mResult.getString(sampleColumn);
 		}
 		
 		outState.putIntArray(SENSE_SAMPLE_IDS, ids);
 		outState.putStringArray(SENSE_SAMPLES, samples);	
 	}
 	
 	protected void bundlePointers(Bundle outState) {
 		int pointerCountColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_COUNT);
 		int idColumn = mResult.getColumnIndex(BaseColumns._ID);
 		int ptypeColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_TYPE);
 		int targetTypeColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_TARGET_TYPE);
 		int targetIdColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_TARGET_ID);
 		int targetTextColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_TARGET_TEXT);
 		int targetGlossColumn = mResult.getColumnIndex(DubsarContentProvider.POINTER_TARGET_GLOSS);
 
 		mResult.moveToFirst();
 		mPointerCount = mResult.getInt(pointerCountColumn);
 		
 		int ids[] = new int[mPointerCount];
 		String ptypes[] = new String[mPointerCount];
 		String targetTypes[] = new String[mPointerCount];
 		int targetIds[] = new int[mPointerCount];
 		String targetTexts[] = new String[mPointerCount];
 		String targetGlosses[] = new String[mPointerCount];
 		
 		outState.putInt(DubsarContentProvider.POINTER_COUNT, mPointerCount);
 		for (int j=0; j<mPointerCount; ++j) {
 			mResult.moveToPosition(mSynonymCount+mVerbFrameCount+mSampleCount+j);
 			
 			ids[j] = mResult.getInt(idColumn);
 			ptypes[j] = mResult.getString(ptypeColumn);
 			targetTypes[j] = mResult.getString(targetTypeColumn);
 			targetIds[j] = mResult.getInt(targetIdColumn);
 			targetTexts[j] = mResult.getString(targetTextColumn);
 			targetGlosses[j] = mResult.getString(targetGlossColumn);
 		}
 		
 		outState.putIntArray(POINTER_IDS, ids);
 		outState.putStringArray(POINTER_TYPES, ptypes);
 		outState.putStringArray(POINTER_TARGET_TYPES, targetTypes);
 		outState.putIntArray(POINTER_TARGET_IDS, targetIds);
 		outState.putStringArray(POINTER_TARGET_TEXTS, targetTexts);
 		outState.putStringArray(POINTER_TARGET_GLOSSES, targetGlosses);
 	}
 	
 	protected void unbundlePointers(Bundle inState) {
 		if (mPointerCount <= 0) return;
 		
 		int[] ids = inState.getIntArray(POINTER_IDS);
 		String[] ptypes = inState.getStringArray(POINTER_TYPES);
 		String[] targetTypes = inState.getStringArray(POINTER_TARGET_TYPES);
 		int[] targetIds = inState.getIntArray(POINTER_TARGET_IDS);
 		String[] targetTexts = inState.getStringArray(POINTER_TARGET_TEXTS);
 		String[] targetGlosses = inState.getStringArray(POINTER_TARGET_GLOSSES);
 		
 		MatrixCursor.RowBuilder builder;
 		MatrixCursor cursor = (MatrixCursor)mResult;
 		for (int j=0; j<mPointerCount; ++j) {
 			builder = cursor.newRow();
 			buildRowBase(ids[j], builder);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(ptypes[j]);
 			builder.add(targetTypes[j]);
 			builder.add(new Integer(targetIds[j]));
 			builder.add(targetTexts[j]);
 			builder.add(targetGlosses[j]);
 		}
 	}
 
 	protected void unbundleSynonyms(Bundle inState) {
 		if (mSynonymCount <= 0) return;
 		
 		int[] ids = inState.getIntArray(SENSE_SYNONYM_IDS);
 		String[] names = inState.getStringArray(SENSE_SYNONYM_NAMES);
 		String[] markers = inState.getStringArray(SENSE_SYNONYM_MARKERS);
 		
 		MatrixCursor.RowBuilder builder;
 		MatrixCursor cursor = (MatrixCursor)mResult;
 		for (int j=0; j<mSynonymCount; ++j) {
 			builder = cursor.newRow();
 			buildRowBase(ids[j], builder);
 			builder.add(names[j]);
 			builder.add(markers[j]);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 		}
 	}
 	
 	protected void unbundleVerbFrames(Bundle inState) {
 		if (mVerbFrameCount <= 0) return;
 		
 		int[] ids = inState.getIntArray(SENSE_VERB_FRAME_IDS);
 		String[] frames = inState.getStringArray(SENSE_VERB_FRAMES);
 		
 		MatrixCursor.RowBuilder builder;
 		MatrixCursor cursor = (MatrixCursor)mResult;
 		for (int j=0; j<mVerbFrameCount; ++j) {
 			builder = cursor.newRow();
 			buildRowBase(ids[j], builder);
 			builder.add(null);
 			builder.add(null);
 			builder.add(frames[j]);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 		}
 	}
 	
 	protected void unbundleSamples(Bundle inState) {
 		if (mSampleCount <= 0) return;
 		
 		int[] ids = inState.getIntArray(SENSE_SAMPLE_IDS);
 		String[] samples = inState.getStringArray(SENSE_SAMPLES);
 		
 		MatrixCursor.RowBuilder builder;
 		MatrixCursor cursor = (MatrixCursor)mResult;
 		for (int j=0; j<mSampleCount; ++j) {
 			builder = cursor.newRow();
 			buildRowBase(ids[j], builder);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(samples[j]);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 			builder.add(null);
 		}
 	}
 	
 	protected MatrixCursor setupResultCursor(Bundle inState) {
 		mSynonymCount = inState.getInt(DubsarContentProvider.SENSE_SYNONYM_COUNT);
 		mVerbFrameCount = inState.getInt(DubsarContentProvider.SENSE_VERB_FRAME_COUNT);
 		mSampleCount = inState.getInt(DubsarContentProvider.SENSE_SAMPLE_COUNT);
 		mPointerCount = inState.getInt(DubsarContentProvider.POINTER_COUNT);
 		
 		int totalCount = mSynonymCount + mVerbFrameCount + mSampleCount + mPointerCount;
 		
 		String[] columns = new String[] { 
 			BaseColumns._ID,
 			DubsarContentProvider.SENSE_WORD_ID,
 			DubsarContentProvider.SENSE_SYNSET_ID,
 			DubsarContentProvider.SENSE_POS,
 			DubsarContentProvider.SENSE_NAME_AND_POS,
 			DubsarContentProvider.SENSE_GLOSS,
 			DubsarContentProvider.SENSE_SUBTITLE,
 			DubsarContentProvider.SENSE_SYNONYM_COUNT,
 			DubsarContentProvider.SENSE_VERB_FRAME_COUNT,
 			DubsarContentProvider.SENSE_SAMPLE_COUNT,
 			DubsarContentProvider.POINTER_COUNT,
 			DubsarContentProvider.SENSE_SYNONYM,
 			DubsarContentProvider.SENSE_SYNONYM_MARKER,
 			DubsarContentProvider.SENSE_VERB_FRAME,
 			DubsarContentProvider.SENSE_SAMPLE,
 			DubsarContentProvider.POINTER_TYPE,
 			DubsarContentProvider.POINTER_TARGET_TYPE,
 			DubsarContentProvider.POINTER_TARGET_ID,
 			DubsarContentProvider.POINTER_TARGET_TEXT,
 			DubsarContentProvider.POINTER_TARGET_GLOSS
 		};
 
 		MatrixCursor cursor = new MatrixCursor(columns, totalCount > 0 ? totalCount : 1);
 		mResult = cursor;
 		return cursor;
 	}
 	
 	protected void buildRowBase(int id, MatrixCursor.RowBuilder builder) {
 		builder.add(new Integer(id));
 		builder.add(new Integer(mWordId));
 		builder.add(new Integer(mSynsetId));
 		builder.add(mPos);
 		builder.add(mNameAndPos);
 		builder.add(mGloss);
 		builder.add(mSubtitle);
 		builder.add(new Integer(mSynonymCount));
 		builder.add(new Integer(mVerbFrameCount));
 		builder.add(new Integer(mSampleCount));
 		builder.add(new Integer(mPointerCount));
 	}
 	
 	static class SenseQuery extends AsyncTask<Uri, Void, Cursor> {
 
 		private WeakReference<SenseActivity> mActivityReference;
 		
 		public SenseQuery(SenseActivity activity) {
 			mActivityReference = new WeakReference<SenseActivity>(activity);
 		};
 		
 		public SenseActivity getActivity() {
 			return mActivityReference != null ? mActivityReference.get() : null;
 		}
 
 		@Override
 		protected Cursor doInBackground(Uri... params) {
 			if (getActivity() == null) return null;
 			return getActivity().managedQuery(params[0], null, null, null, null);
 		}
 
 		@Override
 		protected void onPostExecute(Cursor result) {
 			super.onPostExecute(result);
 			
 			if (isCancelled()) {
 				return;
 			}
 			
 			if (getActivity() == null) return;
 			
 			if (result == null) {
 	        	getActivity().reportError(getActivity().getString(R.string.search_error));
 			}
 			else {
 				getActivity().saveResults(result);
 				getActivity().populateData();
 			}
 		}
 		
 	}
 
 }
