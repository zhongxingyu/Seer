 /*
  * Copyright (C) 2011 The Android Open Source Project
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
 
 package com.android.contacts.calllog;
 
 import com.android.common.widget.GroupingListAdapter;
 import com.android.contacts.ContactPhotoManager;
 import com.android.contacts.PhoneCallDetails;
 import com.android.contacts.PhoneCallDetailsHelper;
 import com.android.contacts.R;
 import com.android.contacts.util.ExpirableCache;
 import com.android.contacts.util.UriUtils;
 import com.google.common.annotations.VisibleForTesting;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.CallLog.Calls;
 import android.provider.ContactsContract.CommonDataKinds.SipAddress;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.PhoneLookup;
 import android.telephony.PhoneNumberUtils;
 import android.text.TextUtils;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 
 import java.util.LinkedList;
 
 /**
  * Adapter class to fill in data for the Call Log.
  */
 public class CallLogAdapter extends GroupingListAdapter
         implements Runnable, ViewTreeObserver.OnPreDrawListener, CallLogGroupBuilder.GroupCreator {
     /** Interface used to initiate a refresh of the content. */
     public interface CallFetcher {
         public void fetchCalls();
     }
 
     /** The time in millis to delay starting the thread processing requests. */
     private static final int START_PROCESSING_REQUESTS_DELAY_MILLIS = 1000;
 
     /** The size of the cache of contact info. */
     private static final int CONTACT_INFO_CACHE_SIZE = 100;
 
     private final Context mContext;
     private final String mCurrentCountryIso;
     private final CallFetcher mCallFetcher;
 
     /**
      * A cache of the contact details for the phone numbers in the call log.
      * <p>
      * The content of the cache is expired (but not purged) whenever the application comes to
      * the foreground.
      */
     private ExpirableCache<String, ContactInfo> mContactInfoCache;
 
     /**
      * List of requests to update contact details.
      * <p>
      * Each request is made of a phone number to look up, and the contact info currently stored in
      * the call log for this number.
      * <p>
      * The requests are added when displaying the contacts and are processed by a background
      * thread.
      */
     private final LinkedList<Pair<String, ContactInfo>> mRequests;
 
     private volatile boolean mDone;
     private boolean mLoading = true;
     private ViewTreeObserver.OnPreDrawListener mPreDrawListener;
     private static final int REDRAW = 1;
     private static final int START_THREAD = 2;
 
     private boolean mFirst;
     private Thread mCallerIdThread;
 
     /** Instance of helper class for managing views. */
     private final CallLogListItemHelper mCallLogViewsHelper;
 
     /** Helper to set up contact photos. */
     private final ContactPhotoManager mContactPhotoManager;
     /** Helper to parse and process phone numbers. */
     private PhoneNumberHelper mPhoneNumberHelper;
     /** Helper to group call log entries. */
     private final CallLogGroupBuilder mCallLogGroupBuilder;
 
     /** Can be set to true by tests to disable processing of requests. */
     private volatile boolean mRequestProcessingDisabled = false;
 
     /** Listener for the primary action in the list, opens the call details. */
     private final View.OnClickListener mPrimaryActionListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             IntentProvider intentProvider = (IntentProvider) view.getTag();
             if (intentProvider != null) {
                 mContext.startActivity(intentProvider.getIntent(mContext));
             }
         }
     };
     /** Listener for the secondary action in the list, either call or play. */
     private final View.OnClickListener mSecondaryActionListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             IntentProvider intentProvider = (IntentProvider) view.getTag();
             if (intentProvider != null) {
                 mContext.startActivity(intentProvider.getIntent(mContext));
             }
         }
     };
 
     @Override
     public boolean onPreDraw() {
         if (mFirst) {
             mHandler.sendEmptyMessageDelayed(START_THREAD,
                     START_PROCESSING_REQUESTS_DELAY_MILLIS);
             mFirst = false;
         }
         return true;
     }
 
     private Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case REDRAW:
                     notifyDataSetChanged();
                     break;
                 case START_THREAD:
                     startRequestProcessing();
                     break;
             }
         }
     };
 
     public CallLogAdapter(Context context, CallFetcher callFetcher,
             String currentCountryIso, String voicemailNumber) {
         super(context);
 
         mContext = context;
         mCurrentCountryIso = currentCountryIso;
         mCallFetcher = callFetcher;
 
         mContactInfoCache = ExpirableCache.create(CONTACT_INFO_CACHE_SIZE);
         mRequests = new LinkedList<Pair<String,ContactInfo>>();
         mPreDrawListener = null;
 
         Resources resources = mContext.getResources();
         CallTypeHelper callTypeHelper = new CallTypeHelper(resources);
 
         mContactPhotoManager = ContactPhotoManager.getInstance(mContext);
         mPhoneNumberHelper = new PhoneNumberHelper(resources, voicemailNumber);
         PhoneCallDetailsHelper phoneCallDetailsHelper = new PhoneCallDetailsHelper(
                 resources, callTypeHelper, mPhoneNumberHelper);
         mCallLogViewsHelper =
                 new CallLogListItemHelper(
                         phoneCallDetailsHelper, mPhoneNumberHelper, resources);
         mCallLogGroupBuilder = new CallLogGroupBuilder(this);
     }
 
     /**
      * Requery on background thread when {@link Cursor} changes.
      */
     @Override
     protected void onContentChanged() {
         mCallFetcher.fetchCalls();
     }
 
     void setLoading(boolean loading) {
         mLoading = loading;
     }
 
     @Override
     public boolean isEmpty() {
         if (mLoading) {
             // We don't want the empty state to show when loading.
             return false;
         } else {
             return super.isEmpty();
         }
     }
 
     public ContactInfo getContactInfo(String number) {
         return mContactInfoCache.getPossiblyExpired(number);
     }
 
     public void startRequestProcessing() {
         if (mRequestProcessingDisabled) {
             return;
         }
 
         mDone = false;
         mCallerIdThread = new Thread(this, "CallLogContactLookup");
         mCallerIdThread.setPriority(Thread.MIN_PRIORITY);
         mCallerIdThread.start();
     }
 
     /**
      * Stops the background thread that processes updates and cancels any pending requests to
      * start it.
      * <p>
      * Should be called from the main thread to prevent a race condition between the request to
      * start the thread being processed and stopping the thread.
      */
     public void stopRequestProcessing() {
         // Remove any pending requests to start the processing thread.
         mHandler.removeMessages(START_THREAD);
         mDone = true;
         if (mCallerIdThread != null) mCallerIdThread.interrupt();
     }
 
     public void invalidateCache() {
         mContactInfoCache.expireAll();
         // Let it restart the thread after next draw
         mPreDrawListener = null;
     }
 
     /**
      * Enqueues a request to look up the contact details for the given phone number.
      * <p>
      * It also provides the current contact info stored in the call log for this number.
      * <p>
      * If the {@code immediate} parameter is true, it will start immediately the thread that looks
      * up the contact information (if it has not been already started). Otherwise, it will be
      * started with a delay. See {@link #START_PROCESSING_REQUESTS_DELAY_MILLIS}.
      */
     @VisibleForTesting
     void enqueueRequest(String number, ContactInfo callLogInfo, boolean immediate) {
         Pair<String, ContactInfo> request = new Pair<String, ContactInfo>(number, callLogInfo);
         synchronized (mRequests) {
             if (!mRequests.contains(request)) {
                 mRequests.add(request);
                 mRequests.notifyAll();
             }
         }
         if (mFirst && immediate) {
             startRequestProcessing();
             mFirst = false;
         }
     }
 
     /**
      * Determines the contact information for the given SIP address.
      * <p>
      * It returns the contact info if found.
      * <p>
      * If no contact corresponds to the given SIP address, returns {@link ContactInfo#EMPTY}.
      * <p>
      * If the lookup fails for some other reason, it returns null.
      */
     private ContactInfo queryContactInfoForSipAddress(String sipAddress) {
         final ContactInfo info;
 
         // TODO: This code is duplicated from the
         // CallerInfoAsyncQuery class.  To avoid that, could the
         // code here just use CallerInfoAsyncQuery, rather than
         // manually running ContentResolver.query() itself?
 
         // We look up SIP addresses directly in the Data table:
         Uri contactRef = Data.CONTENT_URI;
 
         // Note Data.DATA1 and SipAddress.SIP_ADDRESS are equivalent.
         //
         // Also note we use "upper(data1)" in the WHERE clause, and
         // uppercase the incoming SIP address, in order to do a
         // case-insensitive match.
         //
         // TODO: May also need to normalize by adding "sip:" as a
         // prefix, if we start storing SIP addresses that way in the
         // database.
         String selection = "upper(" + Data.DATA1 + ")=?"
                 + " AND "
                 + Data.MIMETYPE + "='" + SipAddress.CONTENT_ITEM_TYPE + "'";
         String[] selectionArgs = new String[] { sipAddress.toUpperCase() };
 
         Cursor dataTableCursor =
                 mContext.getContentResolver().query(
                         contactRef,
                         null,  // projection
                         selection,  // selection
                         selectionArgs,  // selectionArgs
                         null);  // sortOrder
 
         if (dataTableCursor != null) {
             if (dataTableCursor.moveToFirst()) {
                 info = new ContactInfo();
 
                 // TODO: we could slightly speed this up using an
                 // explicit projection (and thus not have to do
                 // those getColumnIndex() calls) but the benefit is
                 // very minimal.
 
                 // Note the Data.CONTACT_ID column here is
                 // equivalent to the PERSON_ID_COLUMN_INDEX column
                 // we use with "phonesCursor" below.
                 long contactId = dataTableCursor.getLong(
                         dataTableCursor.getColumnIndex(Data.CONTACT_ID));
                 String lookupKey = dataTableCursor.getString(
                         dataTableCursor.getColumnIndex(Data.LOOKUP_KEY));
                 info.lookupUri = Contacts.getLookupUri(contactId, lookupKey);
                 info.name = dataTableCursor.getString(
                         dataTableCursor.getColumnIndex(Data.DISPLAY_NAME));
                 // "type" and "label" are currently unused for SIP addresses
                 info.type = SipAddress.TYPE_OTHER;
                 info.label = null;
 
                 // And "number" is the SIP address.
                 // Note Data.DATA1 and SipAddress.SIP_ADDRESS are equivalent.
                 info.number = dataTableCursor.getString(dataTableCursor.getColumnIndex(Data.DATA1));
                 info.normalizedNumber = null;  // meaningless for SIP addresses
                 info.photoId = dataTableCursor.getLong(
                         dataTableCursor.getColumnIndex(Data.PHOTO_ID));
             } else {
                 info = ContactInfo.EMPTY;
             }
             dataTableCursor.close();
         } else {
             // Failed to fetch the data, ignore this request.
             info = null;
         }
         return info;
     }
 
     /**
      * Determines the contact information for the given phone number.
      * <p>
      * It returns the contact info if found.
      * <p>
      * If no contact corresponds to the given phone number, returns {@link ContactInfo#EMPTY}.
      * <p>
      * If the lookup fails for some other reason, it returns null.
      */
     private ContactInfo queryContactInfoForPhoneNumber(String number) {
         final ContactInfo info;
 
         // "number" is a regular phone number, so use the
         // PhoneLookup table:
         Cursor phonesCursor =
                 mContext.getContentResolver().query(
                     Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                             Uri.encode(number)),
                             PhoneQuery._PROJECTION, null, null, null);
         if (phonesCursor != null) {
             if (phonesCursor.moveToFirst()) {
                 info = new ContactInfo();
                 long contactId = phonesCursor.getLong(PhoneQuery.PERSON_ID);
                 String lookupKey = phonesCursor.getString(PhoneQuery.LOOKUP_KEY);
                 info.lookupUri = Contacts.getLookupUri(contactId, lookupKey);
                 info.name = phonesCursor.getString(PhoneQuery.NAME);
                 info.type = phonesCursor.getInt(PhoneQuery.PHONE_TYPE);
                 info.label = phonesCursor.getString(PhoneQuery.LABEL);
                 info.number = phonesCursor.getString(PhoneQuery.MATCHED_NUMBER);
                 info.normalizedNumber = phonesCursor.getString(PhoneQuery.NORMALIZED_NUMBER);
                 info.photoId = phonesCursor.getLong(PhoneQuery.PHOTO_ID);
             } else {
                 info = ContactInfo.EMPTY;
             }
             phonesCursor.close();
         } else {
             // Failed to fetch the data, ignore this request.
             info = null;
         }
         return info;
     }
 
     /**
      * Queries the appropriate content provider for the contact associated with the number.
      * <p>
      * Upon completion it also updates the cache in the call log, if it is different from
      * {@code callLogInfo}.
      * <p>
      * The number might be either a SIP address or a phone number.
      * <p>
      * It returns true if it updated the content of the cache and we should therefore tell the
      * view to update its content.
      */
     private boolean queryContactInfo(String number, ContactInfo callLogInfo) {
         final ContactInfo info;
 
         // Determine the contact info.
         if (PhoneNumberUtils.isUriNumber(number)) {
             // This "number" is really a SIP address.
             info = queryContactInfoForSipAddress(number);
         } else {
             info = queryContactInfoForPhoneNumber(number);
         }
 
         if (info == null) {
             // The lookup failed, just return without requesting to update the view.
             return false;
         }
 
         // Check the existing entry in the cache: only if it has changed we should update the
         // view.
         ContactInfo existingInfo = mContactInfoCache.getPossiblyExpired(number);
         boolean updated = !info.equals(existingInfo);
         if (updated) {
             // The formattedNumber is computed by the UI thread when needed. Since we updated
             // the details of the contact, set this value to null for now.
             info.formattedNumber = null;
         }
         // Store the data in the cache so that the UI thread can use to display it. Store it
         // even if it has not changed so that it is marked as not expired.
         mContactInfoCache.put(number, info);
         // Update the call log even if the cache it is up-to-date: it is possible that the cache
         // contains the value from a different call log entry.
         updateCallLogContactInfoCache(number, info, callLogInfo);
         return updated;
     }
 
     /*
      * Handles requests for contact name and number type
      * @see java.lang.Runnable#run()
      */
     @Override
     public void run() {
         boolean needNotify = false;
         while (!mDone) {
             String number = null;
             ContactInfo callLogInfo = null;
             synchronized (mRequests) {
                 if (!mRequests.isEmpty()) {
                     Pair<String, ContactInfo> request = mRequests.removeFirst();
                     number = request.first;
                     callLogInfo  = request.second;
                 } else {
                     if (needNotify) {
                         needNotify = false;
                         mHandler.sendEmptyMessage(REDRAW);
                     }
                     try {
                         mRequests.wait(1000);
                     } catch (InterruptedException ie) {
                         // Ignore and continue processing requests
                         Thread.currentThread().interrupt();
                     }
                 }
             }
             if (!mDone && number != null && queryContactInfo(number, callLogInfo)) {
                 needNotify = true;
             }
         }
     }
 
     @Override
     protected void addGroups(Cursor cursor) {
         mCallLogGroupBuilder.addGroups(cursor);
     }
 
     @VisibleForTesting
     @Override
     public View newStandAloneView(Context context, ViewGroup parent) {
         LayoutInflater inflater =
                 (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View view = inflater.inflate(R.layout.call_log_list_item, parent, false);
         findAndCacheViews(view);
         return view;
     }
 
     @VisibleForTesting
     @Override
     public void bindStandAloneView(View view, Context context, Cursor cursor) {
         bindView(view, cursor, 1);
     }
 
     @VisibleForTesting
     @Override
     public View newChildView(Context context, ViewGroup parent) {
         LayoutInflater inflater =
                 (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View view = inflater.inflate(R.layout.call_log_list_item, parent, false);
         findAndCacheViews(view);
         return view;
     }
 
     @VisibleForTesting
     @Override
     public void bindChildView(View view, Context context, Cursor cursor) {
         bindView(view, cursor, 1);
     }
 
     @VisibleForTesting
     @Override
     public View newGroupView(Context context, ViewGroup parent) {
         LayoutInflater inflater =
                 (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View view = inflater.inflate(R.layout.call_log_list_item, parent, false);
         findAndCacheViews(view);
         return view;
     }
 
     @VisibleForTesting
     @Override
     public void bindGroupView(View view, Context context, Cursor cursor, int groupSize,
             boolean expanded) {
         bindView(view, cursor, groupSize);
     }
 
     private void findAndCacheViews(View view) {
         // Get the views to bind to.
         CallLogListItemViews views = CallLogListItemViews.fromView(view);
         views.primaryActionView.setOnClickListener(mPrimaryActionListener);
         views.secondaryActionView.setOnClickListener(mSecondaryActionListener);
         view.setTag(views);
     }
 
     /**
      * Binds the views in the entry to the data in the call log.
      *
      * @param view the view corresponding to this entry
      * @param c the cursor pointing to the entry in the call log
      * @param count the number of entries in the current item, greater than 1 if it is a group
      */
     private void bindView(View view, Cursor c, int count) {
         final CallLogListItemViews views = (CallLogListItemViews) view.getTag();
         final int section = c.getInt(CallLogQuery.SECTION);
 
         // This might be a header: check the value of the section column in the cursor.
         if (section == CallLogQuery.SECTION_NEW_HEADER
                 || section == CallLogQuery.SECTION_OLD_HEADER) {
             views.listItemView.setVisibility(View.GONE);
             views.bottomDivider.setVisibility(View.GONE);
             views.listHeaderTextView.setVisibility(View.VISIBLE);
             views.listHeaderTextView.setText(
                     section == CallLogQuery.SECTION_NEW_HEADER
                             ? R.string.call_log_new_header
                             : R.string.call_log_old_header);
             // Nothing else to set up for a header.
             return;
         }
         // Default case: an item in the call log.
         views.listItemView.setVisibility(View.VISIBLE);
         views.bottomDivider.setVisibility(isLastOfSection(c) ? View.GONE : View.VISIBLE);
         views.listHeaderTextView.setVisibility(View.GONE);
 
         final String number = c.getString(CallLogQuery.NUMBER);
         final long date = c.getLong(CallLogQuery.DATE);
         final long duration = c.getLong(CallLogQuery.DURATION);
         final int callType = c.getInt(CallLogQuery.CALL_TYPE);
         final String formattedNumber;
         final String countryIso = c.getString(CallLogQuery.COUNTRY_ISO);
 
         final ContactInfo cachedContactInfo = getContactInfoFromCallLog(c);
 
         views.primaryActionView.setTag(
                 IntentProvider.getCallDetailIntentProvider(
                         this, c.getPosition(), c.getLong(CallLogQuery.ID), count));
         // Store away the voicemail information so we can play it directly.
         if (callType == Calls.VOICEMAIL_TYPE) {
             String voicemailUri = c.getString(CallLogQuery.VOICEMAIL_URI);
             final long rowId = c.getLong(CallLogQuery.ID);
             views.secondaryActionView.setTag(
                     IntentProvider.getPlayVoicemailIntentProvider(rowId, voicemailUri));
         } else if (!TextUtils.isEmpty(number)) {
             // Store away the number so we can call it directly if you click on the call icon.
             views.secondaryActionView.setTag(
                     IntentProvider.getReturnCallIntentProvider(number));
         } else {
             // No action enabled.
             views.secondaryActionView.setTag(null);
         }
 
         // Lookup contacts with this number
         ExpirableCache.CachedValue<ContactInfo> cachedInfo =
                 mContactInfoCache.getCachedValue(number);
         ContactInfo info = cachedInfo == null ? null : cachedInfo.getValue();
        if (!mPhoneNumberHelper.canPlaceCallsTo(number)
                || mPhoneNumberHelper.isVoicemailNumber(number)) {
            // If this is a number that cannot be dialed, there is no point in looking up a contact
            // for it.
            info = ContactInfo.EMPTY;
            formattedNumber = null;
        } else if (cachedInfo == null) {
             mContactInfoCache.put(number, ContactInfo.EMPTY);
             // Use the cached contact info from the call log.
             info = cachedContactInfo;
             // The db request should happen on a non-UI thread.
             // Request the contact details immediately since they are currently missing.
             enqueueRequest(number, cachedContactInfo, true);
             // Format the phone number in the call log as best as we can.
             formattedNumber = formatPhoneNumber(info.number, info.normalizedNumber, countryIso);
             info.formattedNumber = formattedNumber;
         } else {
             if (cachedInfo.isExpired()) {
                 // The contact info is no longer up to date, we should request it. However, we
                 // do not need to request them immediately.
                 enqueueRequest(number, cachedContactInfo, false);
             } else  if (!callLogInfoMatches(cachedContactInfo, info)) {
                 // The call log information does not match the one we have, look it up again.
                 // We could simply update the call log directly, but that needs to be done in a
                 // background thread, so it is easier to simply request a new lookup, which will, as
                 // a side-effect, update the call log.
                 enqueueRequest(number, cachedContactInfo, false);
             }
 
             if (info != ContactInfo.EMPTY) {
                 // Format and cache phone number for found contact.
                 if (info.formattedNumber == null) {
                     info.formattedNumber =
                             formatPhoneNumber(info.number, info.normalizedNumber, countryIso);
                 }
                 formattedNumber = info.formattedNumber;
             } else {
                 // Use the cached contact info from the call log.
                 info = cachedContactInfo;
                 // Format the phone number in the call log as best as we can.
                 formattedNumber = formatPhoneNumber(info.number, info.normalizedNumber, countryIso);
                 info.formattedNumber = formattedNumber;
             }
         }
 
         final Uri lookupUri = info.lookupUri;
         final String name = info.name;
         final int ntype = info.type;
         final String label = info.label;
         final long photoId = info.photoId;
         final int[] callTypes = getCallTypes(c, count);
         final String geocode = c.getString(CallLogQuery.GEOCODED_LOCATION);
         final PhoneCallDetails details;
         if (TextUtils.isEmpty(name)) {
             details = new PhoneCallDetails(number, formattedNumber, countryIso, geocode,
                     callTypes, date, duration);
         } else {
             // We do not pass a photo id since we do not need the high-res picture.
             details = new PhoneCallDetails(number, formattedNumber, countryIso, geocode,
                     callTypes, date, duration, name, ntype, label, lookupUri, null);
         }
 
         final boolean isNew = CallLogQuery.isNewSection(c);
         // New items also use the highlighted version of the text.
         final boolean isHighlighted = isNew;
         mCallLogViewsHelper.setPhoneCallDetails(views, details, isHighlighted);
         setPhoto(views, photoId, lookupUri);
 
         // Listen for the first draw
         if (mPreDrawListener == null) {
             mFirst = true;
             mPreDrawListener = this;
             view.getViewTreeObserver().addOnPreDrawListener(this);
         }
     }
 
     /** Returns true if this is the last item of a section. */
     private boolean isLastOfSection(Cursor c) {
         if (c.isLast()) return true;
         final int section = c.getInt(CallLogQuery.SECTION);
         if (!c.moveToNext()) return true;
         final int nextSection = c.getInt(CallLogQuery.SECTION);
         c.moveToPrevious();
         return section != nextSection;
     }
 
     /** Checks whether the contact info from the call log matches the one from the contacts db. */
     private boolean callLogInfoMatches(ContactInfo callLogInfo, ContactInfo info) {
         // The call log only contains a subset of the fields in the contacts db.
         // Only check those.
         return TextUtils.equals(callLogInfo.name, info.name)
                 && callLogInfo.type == info.type
                 && TextUtils.equals(callLogInfo.label, info.label);
     }
 
     /** Stores the updated contact info in the call log if it is different from the current one. */
     private void updateCallLogContactInfoCache(String number, ContactInfo updatedInfo,
             ContactInfo callLogInfo) {
         final ContentValues values = new ContentValues();
         boolean needsUpdate = false;
 
         if (callLogInfo != null) {
             if (!TextUtils.equals(updatedInfo.name, callLogInfo.name)) {
                 values.put(Calls.CACHED_NAME, updatedInfo.name);
                 needsUpdate = true;
             }
 
             if (updatedInfo.type != callLogInfo.type) {
                 values.put(Calls.CACHED_NUMBER_TYPE, updatedInfo.type);
                 needsUpdate = true;
             }
 
             if (!TextUtils.equals(updatedInfo.label, callLogInfo.label)) {
                 values.put(Calls.CACHED_NUMBER_LABEL, updatedInfo.label);
                 needsUpdate = true;
             }
             if (!UriUtils.areEqual(updatedInfo.lookupUri, callLogInfo.lookupUri)) {
                 values.put(Calls.CACHED_LOOKUP_URI, UriUtils.uriToString(updatedInfo.lookupUri));
                 needsUpdate = true;
             }
             if (!TextUtils.equals(updatedInfo.normalizedNumber, callLogInfo.normalizedNumber)) {
                 values.put(Calls.CACHED_NORMALIZED_NUMBER, updatedInfo.normalizedNumber);
                 needsUpdate = true;
             }
             if (!TextUtils.equals(updatedInfo.number, callLogInfo.number)) {
                 values.put(Calls.CACHED_MATCHED_NUMBER, updatedInfo.number);
                 needsUpdate = true;
             }
             if (updatedInfo.photoId != callLogInfo.photoId) {
                 values.put(Calls.CACHED_PHOTO_ID, updatedInfo.photoId);
                 needsUpdate = true;
             }
         } else {
             // No previous values, store all of them.
             values.put(Calls.CACHED_NAME, updatedInfo.name);
             values.put(Calls.CACHED_NUMBER_TYPE, updatedInfo.type);
             values.put(Calls.CACHED_NUMBER_LABEL, updatedInfo.label);
             values.put(Calls.CACHED_LOOKUP_URI, UriUtils.uriToString(updatedInfo.lookupUri));
             values.put(Calls.CACHED_MATCHED_NUMBER, updatedInfo.number);
             values.put(Calls.CACHED_NORMALIZED_NUMBER, updatedInfo.normalizedNumber);
             values.put(Calls.CACHED_PHOTO_ID, updatedInfo.photoId);
             needsUpdate = true;
         }
 
         if (!needsUpdate) {
             return;
         }
 
         StringBuilder where = new StringBuilder();
         where.append(Calls.NUMBER);
         where.append(" = ?");
 
         mContext.getContentResolver().update(Calls.CONTENT_URI_WITH_VOICEMAIL, values,
                 where.toString(), new String[]{ number });
     }
 
     /** Returns the contact information as stored in the call log. */
     private ContactInfo getContactInfoFromCallLog(Cursor c) {
         ContactInfo info = new ContactInfo();
         info.lookupUri = UriUtils.parseUriOrNull(c.getString(CallLogQuery.CACHED_LOOKUP_URI));
         info.name = c.getString(CallLogQuery.CACHED_NAME);
         info.type = c.getInt(CallLogQuery.CACHED_NUMBER_TYPE);
         info.label = c.getString(CallLogQuery.CACHED_NUMBER_LABEL);
         String matchedNumber = c.getString(CallLogQuery.CACHED_MATCHED_NUMBER);
         info.number = matchedNumber == null ? c.getString(CallLogQuery.NUMBER) : matchedNumber;
         info.normalizedNumber = c.getString(CallLogQuery.CACHED_NORMALIZED_NUMBER);
         info.photoId = c.getLong(CallLogQuery.CACHED_PHOTO_ID);
         // TODO: This could be added to the call log cached values as well.
         info.formattedNumber = null;  // Computed on demand.
         return info;
     }
 
     /**
      * Returns the call types for the given number of items in the cursor.
      * <p>
      * It uses the next {@code count} rows in the cursor to extract the types.
      * <p>
      * It position in the cursor is unchanged by this function.
      */
     private int[] getCallTypes(Cursor cursor, int count) {
         int position = cursor.getPosition();
         int[] callTypes = new int[count];
         for (int index = 0; index < count; ++index) {
             callTypes[index] = cursor.getInt(CallLogQuery.CALL_TYPE);
             cursor.moveToNext();
         }
         cursor.moveToPosition(position);
         return callTypes;
     }
 
     private void setPhoto(CallLogListItemViews views, long photoId, Uri contactUri) {
         views.quickContactView.assignContactUri(contactUri);
         mContactPhotoManager.loadPhoto(views.quickContactView, photoId);
     }
 
     /**
      * Sets whether processing of requests for contact details should be enabled.
      * <p>
      * This method should be called in tests to disable such processing of requests when not
      * needed.
      */
     public void disableRequestProcessingForTest() {
         mRequestProcessingDisabled = true;
     }
 
     public void injectContactInfoForTest(String number, ContactInfo contactInfo) {
         mContactInfoCache.put(number, contactInfo);
     }
 
     @Override
     public void addGroup(int cursorPosition, int size, boolean expanded) {
         super.addGroup(cursorPosition, size, expanded);
     }
 
     /**
      * Format the given phone number
      *
      * @param number the number to be formatted.
      * @param normalizedNumber the normalized number of the given number.
      * @param countryIso the ISO 3166-1 two letters country code, the country's
      *        convention will be used to format the number if the normalized
      *        phone is null.
      *
      * @return the formatted number, or the given number if it was formatted.
      */
     private String formatPhoneNumber(String number, String normalizedNumber,
             String countryIso) {
         if (TextUtils.isEmpty(number)) {
             return "";
         }
         // If "number" is really a SIP address, don't try to do any formatting at all.
         if (PhoneNumberUtils.isUriNumber(number)) {
             return number;
         }
         if (TextUtils.isEmpty(countryIso)) {
             countryIso = mCurrentCountryIso;
         }
         return PhoneNumberUtils.formatNumber(number, normalizedNumber, countryIso);
     }
 
     /*
      * Get the number from the Contacts, if available, since sometimes
      * the number provided by caller id may not be formatted properly
      * depending on the carrier (roaming) in use at the time of the
      * incoming call.
      * Logic : If the caller-id number starts with a "+", use it
      *         Else if the number in the contacts starts with a "+", use that one
      *         Else if the number in the contacts is longer, use that one
      */
     public String getBetterNumberFromContacts(String number) {
         String matchingNumber = null;
         // Look in the cache first. If it's not found then query the Phones db
         ContactInfo ci = mContactInfoCache.getPossiblyExpired(number);
         if (ci != null && ci != ContactInfo.EMPTY) {
             matchingNumber = ci.number;
         } else {
             try {
                 Cursor phonesCursor = mContext.getContentResolver().query(
                         Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number),
                         PhoneQuery._PROJECTION, null, null, null);
                 if (phonesCursor != null) {
                     if (phonesCursor.moveToFirst()) {
                         matchingNumber = phonesCursor.getString(PhoneQuery.MATCHED_NUMBER);
                     }
                     phonesCursor.close();
                 }
             } catch (Exception e) {
                 // Use the number from the call log
             }
         }
         if (!TextUtils.isEmpty(matchingNumber) &&
                 (matchingNumber.startsWith("+")
                         || matchingNumber.length() > number.length())) {
             number = matchingNumber;
         }
         return number;
     }
 }
