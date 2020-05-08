 /*
  * Copyright (C) 2012-2014 Gregory S. Meiste  <http://gregmeiste.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.meiste.greg.ptw.tab;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.meiste.greg.ptw.GAE;
 import com.meiste.greg.ptw.GAE.GaeListener;
 import com.meiste.greg.ptw.PTW;
 import com.meiste.greg.ptw.PlayerAdapter;
 import com.meiste.greg.ptw.PlayerHistory;
 import com.meiste.greg.ptw.QuestionAlarm;
 import com.meiste.greg.ptw.QuestionsRaceAdapter;
 import com.meiste.greg.ptw.R;
 import com.meiste.greg.ptw.Race;
 import com.meiste.greg.ptw.Util;
 
 public final class Questions extends TabFragment implements GaeListener {
 
     public final static String QCACHE = "question_cache";
     public final static String ACACHE = "answer_cache";
     public final static String CACACHE = "correct_answer_cache";
     public final static String CACHE_PREFIX = Calendar.getInstance().get(Calendar.YEAR) + "_race";
 
     private boolean mSetupNeeded;
     private boolean mChanged = false;
     private Race mRaceNext;
     private Race mRaceSelected = null;
     private boolean mFailedConnect = false;
     private boolean mSending = false;
     private long mSubFragmentTime = 0;
     private long mAccountSetupTime = 0;
     private Spinner mRaceSpinner = null;
     private QuestionsRaceAdapter mRaceAdapter;
 
     @Override
     public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
         mRaceNext = Race.getNext(getActivity(), false, true);
         mSetupNeeded = GAE.isAccountSetupNeeded(getActivity());
         mChanged = false;
         mRaceAdapter = new QuestionsRaceAdapter(getActivity());
         mAccountSetupTime = Util.getAccountSetupTime(getActivity());
 
         if (mRaceSelected == null) {
             if (mRaceNext != null) {
                 mRaceSelected = mRaceNext;
             } else if (mRaceAdapter.getCount() > 0) {
                 mRaceSelected = mRaceAdapter.getItem(mRaceAdapter.getCount()-1);
             }
         }
 
         if (mSetupNeeded) {
             return Util.getAccountSetupView(getActivity(), inflater, container);
         } else if (mRaceSelected == null) {
             return inflater.inflate(R.layout.questions_no_race, container, false);
         }
 
         final View v = inflater.inflate(R.layout.questions, container, false);
         mRaceSpinner = (Spinner) v.findViewById(R.id.race_spinner);
         if (mRaceSpinner != null) {
             mRaceSpinner.setAdapter(mRaceAdapter);
             mRaceSpinner.setSelection(mRaceAdapter.getPosition(mRaceSelected));
             mRaceSpinner.setOnItemSelectedListener(new RaceSelectedListener());
         }
 
         setSubFragment();
         return v;
     }
 
     public void setSubFragment() {
         mSubFragmentTime = System.currentTimeMillis();
 
         boolean selectEnable = true;
         final Fragment f;
         final String ftag;
 
         if (mSending) {
             selectEnable = false;
             ftag = QuestionsConnecting.class.getName();
             f = QuestionsConnecting.getInstance(getChildFragmentManager(), ftag);
         } else if (mFailedConnect) {
             mFailedConnect = false;
             ftag = QuestionsConnectFail.class.getName();
             f = QuestionsConnectFail.getInstance(getChildFragmentManager(), ftag);
         } else if (mRaceSelected.inProgress() || !mRaceSelected.isFuture()) {
             SharedPreferences cache = getActivity().getSharedPreferences(QCACHE, Activity.MODE_PRIVATE);
             final String qjson = cache.getString(CACHE_PREFIX + mRaceSelected.getId(), null);
             if (qjson == null) {
                 selectEnable = false;
                 ftag = QuestionsConnecting.class.getName();
                 f = QuestionsConnecting.getInstance(getChildFragmentManager(), ftag);
                 GAE.getInstance(getActivity()).getPage(this, "questions");
             } else {
                 cache = getActivity().getSharedPreferences(ACACHE, Activity.MODE_PRIVATE);
                 final String ajson = cache.getString(CACHE_PREFIX + mRaceSelected.getId(), null);
                 if (ajson == null) {
                     ftag = qjson;
                     f = QuestionsForm.getInstance(getChildFragmentManager(), qjson);
                 } else {
                     cache = getActivity().getSharedPreferences(CACACHE, Activity.MODE_PRIVATE);
                     final String cajson = cache.getString(CACHE_PREFIX + mRaceSelected.getId(), null);
                     if (cajson == null) {
                         final int raceAfterNum = new PlayerAdapter(getActivity()).getRaceAfterNum();
                         if (raceAfterNum >= mRaceSelected.getId()) {
                             // Standings are available for race, so correct answers
                             // should be available for download.
                             getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
                             selectEnable = false;
                             GAE.getInstance(getActivity()).getPage(
                                     new CorrectAnswersListener(mRaceSelected.getId()),
                                     "answers?year=" + mRaceSelected.getStartYear() +
                                     "&race_id=" + mRaceSelected.getId());
                         }
                     }
 
                     ftag = QuestionsAnswers.getTag(qjson, ajson, cajson);
                     f = QuestionsAnswers.getInstance(getChildFragmentManager(), qjson, ajson, cajson);
                 }
             }
         } else {
             ftag = QuestionsNotYet.getTag(mRaceSelected);
             f = QuestionsNotYet.getInstance(getChildFragmentManager(), mRaceSelected);
         }
 
         getChildFragmentManager().beginTransaction().replace(R.id.race_questions, f, ftag).commit();
 
         if (mRaceSpinner != null) {
             mRaceSpinner.setEnabled(selectEnable);
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         final IntentFilter filter = new IntentFilter(PTW.INTENT_ACTION_SCHEDULE);
         filter.addAction(PTW.INTENT_ACTION_RACE_ALARM);
         filter.addAction(PTW.INTENT_ACTION_HISTORY);
         getActivity().registerReceiver(mBroadcastReceiver, filter);
 
         if ((mRaceSelected != null) && mRaceSelected.inProgress()) {
             QuestionAlarm.clearNotification(getActivity().getApplicationContext());
         }
 
         // Check if user changed their account status
         mChanged |= mSetupNeeded != GAE.isAccountSetupNeeded(getActivity());
         // Check if user has switched accounts
         mChanged |= mAccountSetupTime != Util.getAccountSetupTime(getActivity());
         // Check if user submitted answers on a different device
        mChanged |= mSubFragmentTime < PlayerHistory.getTime(getActivity());
         if (mRaceNext != null) {
            // See if race questions are now available but weren't previously
            mChanged |= (mSubFragmentTime < mRaceNext.getQuestionTimestamp()) && mRaceNext.inProgress();
             // Check if questions form needs to disappear because race started
             mChanged |= !mRaceNext.isFuture();
         }
 
         if (mChanged) {
             Util.log("Questions: onResume: notifyChanged");
             resetRaceSelected();
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
         getActivity().unregisterReceiver(mBroadcastReceiver);
     }
 
     @Override
     public boolean isChanged() {
         return mChanged;
     }
 
     @Override
     protected void notifyChanged() {
         mChanged = true;
         super.notifyChanged();
     }
 
     private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(final Context context, final Intent intent) {
             Util.log("Questions.onReceive: " + intent.getAction());
             mChanged = true;
             resetRaceSelected();
         }
     };
 
     private void resetRaceSelected() {
         // Workaround to ensure the spinner is set back to next race
         if ((mRaceSpinner != null) && (mRaceAdapter.getCount() > 0) &&
                 (mRaceSpinner.getSelectedItemPosition() != (mRaceAdapter.getCount() - 1))) {
             // Making new selection causes notifyChanged to be called by listener
             mRaceSpinner.setSelection(mRaceAdapter.getCount() - 1);
         } else {
             notifyChanged();
         }
     }
 
     private class RaceSelectedListener implements OnItemSelectedListener {
         @Override
         public void onItemSelected(final AdapterView<?> parent, final View v, final int pos, final long id) {
             final Race race = (Race) parent.getItemAtPosition(pos);
             if (mRaceSelected.getId() != race.getId()) {
                 mRaceSelected = race;
                 Util.log("Questions: Selected race = " + mRaceSelected.getId());
                 if (mChanged) {
                     notifyChanged();
                 } else {
                     setSubFragment();
                 }
             }
         }
 
         @Override
         public void onNothingSelected(final AdapterView<?> parent) {}
     }
 
     private class CorrectAnswersListener implements GaeListener {
         private final int raceId;
 
         public CorrectAnswersListener(final int id) {
             raceId = id;
         }
 
         @Override
         public void onFailedConnect(final Context context) {
             Util.log("CorrectAnswersListener: onFailedConnect");
 
             // Check for null in case race alarm has fired, changing the view
             if (mRaceSpinner != null) {
                 mRaceSpinner.setEnabled(true);
             }
 
             // Verify application wasn't closed before callback returned
             if (getSherlockActivity() != null) {
                 getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
             }
         }
 
         @Override
         public void onGet(final Context context, final String json) {
             Util.log("CorrectAnswersListener: onGet: " + json);
 
             final SharedPreferences cache = context.getSharedPreferences(CACACHE, Activity.MODE_PRIVATE);
             cache.edit().putString(CACHE_PREFIX + raceId, json).apply();
 
             // Check for null in case race alarm has fired, changing the view
             if (mRaceSpinner != null) {
                 mRaceSpinner.setEnabled(true);
             }
 
             // Verify application wasn't closed before callback returned
             if (getSherlockActivity() != null) {
                 getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
                 setSubFragment();
             }
         }
 
         @Override
         public void onConnectSuccess(final Context context, final String json) {}
 
         @Override
         public void onLaunchIntent(final Intent launch) {}
     }
 
     public void onSubmitAnswers(final String json) {
         mSending = true;
         setSubFragment();
 
         GAE.getInstance(getActivity()).postPage(this, "questions", json);
         EasyTracker.getTracker().sendEvent("Questions", "button", "send", (long) 0);
     }
 
     @Override
     public void onFailedConnect(final Context context) {
         Util.log("Questions: onFailedConnect");
 
         // Verify application wasn't closed before callback returned
         if (getActivity() != null) {
             mSending = false;
             mFailedConnect = true;
             setSubFragment();
         }
     }
 
     @Override
     public void onGet(final Context context, final String json) {
         Util.log("Questions: onGet: " + json);
 
         final SharedPreferences cache = context.getSharedPreferences(QCACHE, Activity.MODE_PRIVATE);
         cache.edit().putString(CACHE_PREFIX + mRaceNext.getId(), json).apply();
 
         // Verify application wasn't closed before callback returned
         if (getActivity() != null) {
             setSubFragment();
         }
     }
 
     @Override
     public void onConnectSuccess(final Context context, final String json) {
         Util.log("Questions: onConnectSuccess: " + json);
         Toast.makeText(context, R.string.questions_success, Toast.LENGTH_SHORT).show();
 
         final SharedPreferences cache = context.getSharedPreferences(ACACHE, Activity.MODE_PRIVATE);
         cache.edit().putString(CACHE_PREFIX + mRaceNext.getId(), json).apply();
 
         context.sendBroadcast(new Intent(PTW.INTENT_ACTION_ANSWERS));
 
         // Verify application wasn't closed before callback returned
         if (getActivity() != null) {
             mSending = false;
             setSubFragment();
         }
     }
 
     @Override
     public void onLaunchIntent(final Intent launch) {}
 }
