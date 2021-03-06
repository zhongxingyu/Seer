 /*
  * Copyright (C) 2010 The Android Open Source Project
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
  * limitations under the License
  */
 
 package com.android.contacts.views.editor;
 
 import com.android.contacts.JoinContactActivity;
 import com.android.contacts.R;
 import com.android.contacts.model.ContactsSource;
 import com.android.contacts.model.Editor;
 import com.android.contacts.model.Editor.EditorListener;
 import com.android.contacts.model.EntityDelta;
 import com.android.contacts.model.EntityDelta.ValuesDelta;
 import com.android.contacts.model.EntityDeltaList;
 import com.android.contacts.model.EntityModifier;
 import com.android.contacts.model.GoogleSource;
 import com.android.contacts.model.Sources;
 import com.android.contacts.ui.ViewIdGenerator;
 import com.android.contacts.ui.widget.AggregationSuggestionView;
 import com.android.contacts.ui.widget.BaseContactEditorView;
 import com.android.contacts.ui.widget.ContactEditorView;
 import com.android.contacts.ui.widget.GenericEditorView;
 import com.android.contacts.ui.widget.PhotoEditorView;
 import com.android.contacts.util.EmptyService;
 import com.android.contacts.util.WeakAsyncTask;
 import com.android.contacts.views.ContactLoader;
 import com.android.contacts.views.GroupMetaDataLoader;
 import com.android.contacts.views.editor.AggregationSuggestionEngine.Suggestion;
 import com.google.android.collect.Lists;
 
 import android.accounts.Account;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.LoaderManager;
 import android.app.LoaderManager.LoaderCallbacks;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.ContentProviderOperation;
 import android.content.ContentProviderOperation.Builder;
 import android.content.ContentProviderResult;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Entity;
 import android.content.Intent;
 import android.content.Loader;
 import android.content.OperationApplicationException;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.graphics.Bitmap;
 import android.graphics.Rect;
 import android.media.MediaScannerConnection;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.RemoteException;
 import android.os.SystemClock;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.AggregationExceptions;
 import android.provider.ContactsContract.CommonDataKinds.Email;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.CommonDataKinds.StructuredName;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.Groups;
 import android.provider.ContactsContract.RawContacts;
 import android.provider.MediaStore;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewStub;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 public class ContactEditorFragment extends Fragment implements
         SplitContactConfirmationDialogFragment.Listener, PickPhotoDialogFragment.Listener,
         SelectAccountDialogFragment.Listener, ModifyPhotoDialogFragment.Listener,
         AggregationSuggestionEngine.Listener {
 
     private static final String TAG = "ContactEditorFragment";
 
     private static final int LOADER_DATA = 1;
     private static final int LOADER_GROUPS = 2;
 
     private static final String KEY_URI = "uri";
     private static final String KEY_ACTION = "action";
     private static final String KEY_EDIT_STATE = "state";
     private static final String KEY_RAW_CONTACT_ID_REQUESTING_PHOTO = "photorequester";
     private static final String KEY_VIEW_ID_GENERATOR = "viewidgenerator";
     private static final String KEY_CURRENT_PHOTO_FILE = "currentphotofile";
     private static final String KEY_QUERY_SELECTION = "queryselection";
     private static final String KEY_CONTACT_ID_FOR_JOIN = "contactidforjoin";
 
     /**
      * Modes that specify what the AsyncTask has to perform after saving
      */
     private interface SaveMode {
         /**
          * Close the editor after saving
          */
         public static final int CLOSE = 0;
 
         /**
          * Reload the data so that the user can continue editing
          */
         public static final int RELOAD = 1;
 
         /**
          * Split the contact after saving
          */
         public static final int SPLIT = 2;
 
         /**
          * Join another contact after saving
          */
         public static final int JOIN = 3;
     }
 
     private interface Status {
         /**
          * The loader is fetching data
          */
         public static final int LOADING = 0;
 
         /**
          * Not currently busy. We are waiting for the user to enter data
          */
         public static final int EDITING = 1;
 
         /**
          * The data is currently being saved. This is used to prevent more auto-saves (they shouldn't
          * overlap)
          */
         public static final int SAVING = 2;
 
         /**
          * Prevents any more savings (this is used if Save/Close or Revert was executed by the user)
          */
         public static final int CLOSING = 3;
     }
 
     private static final int REQUEST_CODE_JOIN = 0;
     private static final int REQUEST_CODE_CAMERA_WITH_DATA = 1;
     private static final int REQUEST_CODE_PHOTO_PICKED_WITH_DATA = 2;
 
     private Bitmap mPhoto = null;
     private long mRawContactIdRequestingPhoto = -1;
     private long mRawContactIdRequestingPhotoAfterLoad = -1;
 
     private final EntityDeltaComparator mComparator = new EntityDeltaComparator();
 
     private static final int ICON_SIZE = 96;
 
     private static final File PHOTO_DIR = new File(
             Environment.getExternalStorageDirectory() + "/DCIM/Camera");
 
     private Cursor mGroupMetaData;
 
     /**
      * A delay in milliseconds used for bringing aggregation suggestions to
      * the visible part of the screen. The reason this has to be done after
      * a delay is a race condition with the soft keyboard.  The keyboard
      * may expand to display its own autocomplete suggestions, which will
      * reduce the visible area of the screen.  We will yield to the keyboard
      * hoping that the delay is sufficient.  If not - part of the
      * suggestion will be hidden, which is not fatal.
      */
     private static final int AGGREGATION_SUGGESTION_SCROLL_DELAY = 200;
 
     private File mCurrentPhotoFile;
 
     private Context mContext;
     private String mAction;
     private Uri mLookupUri;
     private String mMimeType;
     private Bundle mIntentExtras;
     private Listener mListener;
 
     private String mQuerySelection;
 
     private long mContactIdForJoin;
 
     private LinearLayout mContent;
     private EntityDeltaList mState;
 
     private ViewIdGenerator mViewIdGenerator;
 
     private long mLoaderStartTime;
 
     private int mStatus;
 
     private AggregationSuggestionEngine mAggregationSuggestionEngine;
 
     public ContactEditorFragment() {
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         mContext = activity;
     }
 
     @Override
     public void onStop() {
         super.onStop();
         if (mAggregationSuggestionEngine != null) {
             mAggregationSuggestionEngine.quit();
         }
         // If anything was left unsaved, save it now but keep the editor open.
         if (!getActivity().isChangingConfigurations() && mStatus == Status.EDITING) {
             save(false);
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
         final View view = inflater.inflate(R.layout.contact_editor_fragment, container, false);
 
         mContent = (LinearLayout) view.findViewById(R.id.editors);
 
         setHasOptionsMenu(true);
 
         // If we are in an orientation change, we already have mState (it was loaded by onCreate)
         if (mState != null) {
             bindEditors();
         }
 
         return view;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         Log.d(TAG, "onActivityCreated(" + savedInstanceState + ")");
 
         // Handle initial actions only when existing state missing
         final boolean hasIncomingState = savedInstanceState != null;
 
         if (!hasIncomingState) {
             if (Intent.ACTION_EDIT.equals(mAction)) {
                 if (mListener != null) mListener.setTitleTo(R.string.editContact_title_edit);
                 getLoaderManager().initLoader(LOADER_DATA, null, mDataLoaderListener);
             } else if (Intent.ACTION_INSERT.equals(mAction)) {
                 if (mListener != null) mListener.setTitleTo(R.string.editContact_title_insert);
 
                 doAddAction(true);
             } else throw new IllegalArgumentException("Unknown Action String " + mAction +
                     ". Only support " + Intent.ACTION_EDIT + " or " + Intent.ACTION_INSERT);
         }
     }
 
     @Override
     public void onStart() {
         getLoaderManager().initLoader(LOADER_GROUPS, null, mGroupLoaderListener);
         super.onStart();
     }
 
     public void load(String action, Uri lookupUri, String mimeType, Bundle intentExtras) {
         mAction = action;
         mLookupUri = lookupUri;
         mMimeType = mimeType;
         mIntentExtras = intentExtras;
     }
 
     public void setListener(Listener value) {
         mListener = value;
     }
 
     @Override
     public void onCreate(Bundle savedState) {
         if (savedState != null) {
             // Restore mUri before calling super.onCreate so that onInitializeLoaders
             // would already have a uri and an action to work with
             mLookupUri = savedState.getParcelable(KEY_URI);
             mAction = savedState.getString(KEY_ACTION);
         }
 
         super.onCreate(savedState);
 
         if (savedState == null) {
             // If savedState is non-null, onRestoreInstanceState() will restore the generator.
             mViewIdGenerator = new ViewIdGenerator();
         } else {
             // Read state from savedState. No loading involved here
             mState = savedState.<EntityDeltaList> getParcelable(KEY_EDIT_STATE);
             mRawContactIdRequestingPhoto = savedState.getLong(
                     KEY_RAW_CONTACT_ID_REQUESTING_PHOTO);
             mViewIdGenerator = savedState.getParcelable(KEY_VIEW_ID_GENERATOR);
             String fileName = savedState.getString(KEY_CURRENT_PHOTO_FILE);
             if (fileName != null) {
                 mCurrentPhotoFile = new File(fileName);
             }
             mQuerySelection = savedState.getString(KEY_QUERY_SELECTION);
             mContactIdForJoin = savedState.getLong(KEY_CONTACT_ID_FOR_JOIN);
             mStatus = Status.EDITING;
         }
     }
 
     public void setData(ContactLoader.Result data) {
         // If we have already loaded data, we do not want to change it here to not confuse the user
         if (mState != null) {
             Log.v(TAG, "Ignoring background change. This will have to be rebased later");
             return;
         }
 
         // Build Filter mQuerySelection
         final ArrayList<Entity> entities = data.getEntities();
         final StringBuilder sb = new StringBuilder(RawContacts._ID + " IN(");
         final int count = entities.size();
         for (int i = 0; i < count; i++) {
             if (i > 0) {
                 sb.append(',');
             }
             sb.append(entities.get(i).getEntityValues().get(RawContacts._ID));
         }
         sb.append(")");
         mQuerySelection = sb.toString();
         mState = EntityDeltaList.fromIterator(entities.iterator());
 
         // Merge in Extras from Intent
         final boolean hasExtras = mIntentExtras != null && mIntentExtras.size() > 0;
         final boolean hasState = mState.size() > 0;
         if (hasExtras && hasState) {
             // Find source defining the first RawContact found
             final EntityDelta state = mState.get(0);
             final String accountType = state.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
             final Sources sources = Sources.getInstance(mContext);
             final ContactsSource source = sources.getInflatedSource(accountType,
                     ContactsSource.LEVEL_CONSTRAINTS);
             EntityModifier.parseExtras(mContext, source, state, mIntentExtras);
         }
         bindEditors();
     }
 
     private void selectAccountAndCreateContact(boolean isNewContact) {
         final ArrayList<Account> accounts = Sources.getInstance(mContext).getAccounts(true);
         // No Accounts available.  Create a phone-local contact.
         if (accounts.isEmpty()) {
             createContact(null, isNewContact);
             return;  // Don't show a dialog.
         }
 
         // In the common case of a single account being writable, auto-select
         // it without showing a dialog.
         if (accounts.size() == 1) {
             createContact(accounts.get(0), isNewContact);
             return;  // Don't show a dialog.
         }
 
         final SelectAccountDialogFragment dialog = new SelectAccountDialogFragment(isNewContact);
         dialog.setTargetFragment(this, 0);
         dialog.show(getFragmentManager(), SelectAccountDialogFragment.TAG);
     }
 
     /**
      * @param account may be null to signal a device-local contact should
      *     be created.
      * @param prefillFromIntent If this is set, the intent extras will be used to prefill the fields
      */
     private void createContact(Account account, boolean prefillFromIntent) {
         final Sources sources = Sources.getInstance(mContext);
         final ContentValues values = new ContentValues();
         if (account != null) {
             values.put(RawContacts.ACCOUNT_NAME, account.name);
             values.put(RawContacts.ACCOUNT_TYPE, account.type);
         } else {
             values.putNull(RawContacts.ACCOUNT_NAME);
             values.putNull(RawContacts.ACCOUNT_TYPE);
         }
 
         // Parse any values from incoming intent
         EntityDelta insert = new EntityDelta(ValuesDelta.fromAfter(values));
         final ContactsSource source = sources.getInflatedSource(
                 account != null ? account.type : null,
                 ContactsSource.LEVEL_CONSTRAINTS);
         EntityModifier.parseExtras(mContext, source, insert,
                 prefillFromIntent ? mIntentExtras : null);
 
         // Ensure we have some default fields
         EntityModifier.ensureKindExists(insert, source, Phone.CONTENT_ITEM_TYPE);
         EntityModifier.ensureKindExists(insert, source, Email.CONTENT_ITEM_TYPE);
 
         if (mState == null) {
             // Create state if none exists yet
             mState = EntityDeltaList.fromSingle(insert);
         } else {
             // Add contact onto end of existing state
             mState.add(insert);
         }
 
         bindEditors();
     }
 
     private void bindEditors() {
         // Sort the editors
         Collections.sort(mState, mComparator);
 
         // Remove any existing editors and rebuild any visible
         mContent.removeAllViews();
 
         final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                 Context.LAYOUT_INFLATER_SERVICE);
         final Sources sources = Sources.getInstance(mContext);
         int size = mState.size();
         for (int i = 0; i < size; i++) {
             // TODO ensure proper ordering of entities in the list
             final EntityDelta entity = mState.get(i);
             final ValuesDelta values = entity.getValues();
             if (!values.isVisible()) continue;
 
             final String accountType = values.getAsString(RawContacts.ACCOUNT_TYPE);
             final ContactsSource source = sources.getInflatedSource(accountType,
                     ContactsSource.LEVEL_CONSTRAINTS);
             final long rawContactId = values.getAsLong(RawContacts._ID);
 
             final BaseContactEditorView editor;
             if (!source.readOnly) {
                 editor = (BaseContactEditorView) inflater.inflate(R.layout.item_contact_editor,
                         mContent, false);
             } else {
                 editor = (BaseContactEditorView) inflater.inflate(
                         R.layout.item_read_only_contact_editor, mContent, false);
             }
             final PhotoEditorView photoEditor = editor.getPhotoEditor();
             final boolean sourceReadOnly = source.readOnly;
             photoEditor.setEditorListener(new EditorListener() {
                 @Override
                 public void onRequest(int request) {
                     if (!hasValidState()) return;
 
                     if (request == EditorListener.REQUEST_PICK_PHOTO) {
                         if (editor.hasSetPhoto()) {
                             // There is an existing photo, offer to remove, replace, or promote to
                             // primary
                             final ModifyPhotoDialogFragment fragment =
                                     new ModifyPhotoDialogFragment(sourceReadOnly, rawContactId);
                             fragment.setTargetFragment(ContactEditorFragment.this, 0);
                             fragment.show(getFragmentManager(), ModifyPhotoDialogFragment.TAG);
                         } else if (!sourceReadOnly) {
                             // No photo set and not read-only, try to set the photo
                             doPickPhotoAction(rawContactId);
                         }
                     }
                 }
 
                 @Override
                 public void onDeleted(Editor removedEditor) {
                 }
             });
 
             mContent.addView(editor);
             editor.setState(entity, source, mViewIdGenerator);
 
             if (editor instanceof ContactEditorView) {
                 final ContactEditorView rawContactEditor = (ContactEditorView) editor;
                 final GenericEditorView nameEditor = rawContactEditor.getNameEditor();
                 nameEditor.setEditorListener(new EditorListener() {
 
                     @Override
                     public void onRequest(int request) {
                         onContactNameChange(request, rawContactEditor, nameEditor);
                     }
 
                     @Override
                     public void onDeleted(Editor removedEditor) {
                     }
                 });
 
                 // If the user has already decided to join with a specific contact,
                 // trigger a refresh of the aggregation suggestion view to redisplay
                 // the selection.
                 if (mContactIdForJoin != 0) {
                     acquireAggregationSuggestions(rawContactEditor);
                 }
             }
         }
 
         bindGroupMetaData();
 
         // Show editor now that we've loaded state
         mContent.setVisibility(View.VISIBLE);
 
         // Refresh Action Bar as the visibility of the join command
         // Activity can be null if we have been detached from the Activity
         final Activity activity = getActivity();
         if (activity != null) activity.invalidateOptionsMenu();
     }
 
     private void bindGroupMetaData() {
         if (mGroupMetaData == null) {
             return;
         }
 
         int editorCount = mContent.getChildCount();
         for (int i = 0; i < editorCount; i++) {
             BaseContactEditorView editor = (BaseContactEditorView) mContent.getChildAt(i);
             editor.setGroupMetaData(mGroupMetaData);
         }
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
         inflater.inflate(R.menu.edit, menu);
     }
 
     @Override
     public void onPrepareOptionsMenu(Menu menu) {
         menu.findItem(R.id.menu_split).setVisible(mState != null && mState.size() > 1);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_done:
                 return doSaveAction(SaveMode.CLOSE);
             case R.id.menu_discard:
                 return doRevertAction();
             case R.id.menu_add_raw_contact:
                 return doAddAction(false);
             case R.id.menu_delete:
                 return doDeleteAction();
             case R.id.menu_split:
                 return doSplitContactAction();
             case R.id.menu_join:
                 return doJoinContactAction();
         }
         return false;
     }
 
     private boolean doAddAction(boolean isNewContact) {
         // Load Accounts async so that we can present them
         selectAccountAndCreateContact(isNewContact);
 
         return true;
     }
 
     /**
      * Delete the entire contact currently being edited, which usually asks for
      * user confirmation before continuing.
      */
     private boolean doDeleteAction() {
         if (!hasValidState())
             return false;
 
         // TODO: Make sure Insert turns into Edit if/once it is autosaved
         if (Intent.ACTION_INSERT.equals(mAction)) {
             if (mListener != null) mListener.onReverted();
         } else {
             if (mListener != null) mListener.onDeleteRequested(mLookupUri);
         }
         return true;
     }
 
     /**
      * Pick a specific photo to be added under the currently selected tab.
      */
     /* package */ boolean doPickPhotoAction(long rawContactId) {
         if (!hasValidState()) return false;
 
         mRawContactIdRequestingPhoto = rawContactId;
         final PickPhotoDialogFragment dialogFragment = new PickPhotoDialogFragment();
         dialogFragment.setTargetFragment(this, 0);
         dialogFragment.show(getFragmentManager(), PickPhotoDialogFragment.TAG);
 
         return true;
     }
 
     private boolean doSplitContactAction() {
         if (!hasValidState()) return false;
 
         final SplitContactConfirmationDialogFragment dialog =
                 new SplitContactConfirmationDialogFragment();
         dialog.setTargetFragment(this, 0);
         dialog.show(getFragmentManager(), SplitContactConfirmationDialogFragment.TAG);
         return true;
     }
 
     private boolean doJoinContactAction() {
         return doSaveAction(SaveMode.JOIN);
     }
 
     /**
      * Constructs an intent for picking a photo from Gallery, cropping it and returning the bitmap.
      */
     public static Intent getPhotoPickIntent() {
         Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
         intent.setType("image/*");
         intent.putExtra("crop", "true");
         intent.putExtra("aspectX", 1);
         intent.putExtra("aspectY", 1);
         intent.putExtra("outputX", ICON_SIZE);
         intent.putExtra("outputY", ICON_SIZE);
         intent.putExtra("return-data", true);
         return intent;
     }
 
     /**
      * Check if our internal {@link #mState} is valid, usually checked before
      * performing user actions.
      */
     private boolean hasValidState() {
         return mState != null && mState.size() > 0;
     }
 
     /**
      * Create a file name for the icon photo using current time.
      */
     private String getPhotoFileName() {
         Date date = new Date(System.currentTimeMillis());
         SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
         return dateFormat.format(date) + ".jpg";
     }
 
     /**
      * Constructs an intent for capturing a photo and storing it in a temporary file.
      */
     public static Intent getTakePickIntent(File f) {
         Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
         intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
         return intent;
     }
 
     /**
      * Sends a newly acquired photo to Gallery for cropping
      */
     protected void doCropPhoto(File f) {
         try {
             // Add the image to the media store
             MediaScannerConnection.scanFile(
                     mContext,
                     new String[] { f.getAbsolutePath() },
                     new String[] { null },
                     null);
 
             // Launch gallery to crop the photo
             final Intent intent = getCropImageIntent(Uri.fromFile(f));
             startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKED_WITH_DATA);
         } catch (Exception e) {
             Log.e(TAG, "Cannot crop image", e);
             Toast.makeText(mContext, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
         }
     }
 
     /**
      * Constructs an intent for image cropping.
      */
     public static Intent getCropImageIntent(Uri photoUri) {
         Intent intent = new Intent("com.android.camera.action.CROP");
         intent.setDataAndType(photoUri, "image/*");
         intent.putExtra("crop", "true");
         intent.putExtra("aspectX", 1);
         intent.putExtra("aspectY", 1);
         intent.putExtra("outputX", ICON_SIZE);
         intent.putExtra("outputY", ICON_SIZE);
         intent.putExtra("return-data", true);
         return intent;
     }
 
     /**
      * Saves or creates the contact based on the mode, and if successful
      * finishes the activity.
      */
     private boolean doSaveAction(int saveMode) {
         if (!hasValidState()) {
             return false;
         }
 
         // If we are about to close the editor - there is no need to refresh the data
         if (saveMode == SaveMode.CLOSE) {
             getLoaderManager().stopLoader(LOADER_DATA);
         }
 
         mStatus = Status.SAVING;
         final PersistTask task = new PersistTask(this, saveMode);
         task.execute(mState);
 
         return true;
     }
 
     /**
      * Asynchronously saves the changes made by the user. This can be called even if nothing
      * has changed
      */
     public void save(boolean closeAfterSave) {
         doSaveAction(closeAfterSave ? SaveMode.CLOSE : SaveMode.RELOAD);
     }
 
     private boolean doRevertAction() {
         // When this Fragment is closed we don't want it to auto-save
         mStatus = Status.CLOSING;
         if (mListener != null) mListener.onReverted();
 
         return true;
     }
 
     private void onSaveCompleted(boolean success, int saveMode, Uri contactLookupUri) {
         Log.d(TAG, "onSaveCompleted(" + success + ", " + saveMode + ", " + contactLookupUri);
         switch (saveMode) {
             case SaveMode.CLOSE:
                 final Intent resultIntent;
                 final int resultCode;
                 if (success && contactLookupUri != null) {
                     final String requestAuthority =
                             mLookupUri == null ? null : mLookupUri.getAuthority();
 
                     final String legacyAuthority = "contacts";
 
                     resultIntent = new Intent();
                     if (legacyAuthority.equals(requestAuthority)) {
                         // Build legacy Uri when requested by caller
                         final long contactId = ContentUris.parseId(Contacts.lookupContact(
                                 mContext.getContentResolver(), contactLookupUri));
                         final Uri legacyContentUri = Uri.parse("content://contacts/people");
                         final Uri legacyUri = ContentUris.withAppendedId(
                                 legacyContentUri, contactId);
                         resultIntent.setData(legacyUri);
                     } else {
                         // Otherwise pass back a lookup-style Uri
                         resultIntent.setData(contactLookupUri);
                     }
 
                     resultCode = Activity.RESULT_OK;
                 } else {
                     resultCode = Activity.RESULT_CANCELED;
                     resultIntent = null;
                 }
                 // It is already saved, so prevent that it is saved again
                 mStatus = Status.CLOSING;
                 if (mListener != null) mListener.onSaveFinished(resultCode, resultIntent);
                 break;
             case SaveMode.RELOAD:
                 if (success && contactLookupUri != null) {
                     // If this was in INSERT, we are changing into an EDIT now.
                     // If it already was an EDIT, we are changing to the new Uri now
                     mState = null;
                     load(Intent.ACTION_EDIT, contactLookupUri, mMimeType, null);
                     mStatus = Status.LOADING;
                     getLoaderManager().restartLoader(LOADER_DATA, null, mDataLoaderListener);
                 }
                 break;
             case SaveMode.SPLIT:
                 if (mListener != null) {
                     mListener.onAggregationChangeFinished(contactLookupUri);
                 } else {
                     Log.d(TAG, "No listener registered, can not call onSplitFinished");
                 }
                 mStatus = Status.EDITING;
                 break;
 
             case SaveMode.JOIN:
                 if (success) {
                     showJoinAggregateActivity(contactLookupUri);
                 }
                 mStatus = Status.EDITING;
                 break;
         }
     }
 
     /**
      * Shows a list of aggregates that can be joined into the currently viewed aggregate.
      *
      * @param contactLookupUri the fresh URI for the currently edited contact (after saving it)
      */
     private void showJoinAggregateActivity(Uri contactLookupUri) {
         if (contactLookupUri == null) {
             return;
         }
 
         mContactIdForJoin = ContentUris.parseId(contactLookupUri);
         final Intent intent = new Intent(JoinContactActivity.JOIN_CONTACT);
         intent.putExtra(JoinContactActivity.EXTRA_TARGET_CONTACT_ID, mContactIdForJoin);
         startActivityForResult(intent, REQUEST_CODE_JOIN);
     }
 
     private interface JoinContactQuery {
         String[] PROJECTION = {
                 RawContacts._ID,
                 RawContacts.CONTACT_ID,
                 RawContacts.NAME_VERIFIED,
         };
 
         String SELECTION = RawContacts.CONTACT_ID + "=? OR " + RawContacts.CONTACT_ID + "=?";
 
         int _ID = 0;
         int CONTACT_ID = 1;
         int NAME_VERIFIED = 2;
     }
 
     /**
      * Performs aggregation with the contact selected by the user from suggestions or A-Z list.
      */
     private void joinAggregate(final long contactId) {
         final ContentResolver resolver = mContext.getContentResolver();
 
         // Load raw contact IDs for all raw contacts involved - currently edited and selected
         // in the join UIs
         Cursor c = resolver.query(RawContacts.CONTENT_URI,
                 JoinContactQuery.PROJECTION,
                 JoinContactQuery.SELECTION,
                 new String[]{String.valueOf(contactId), String.valueOf(mContactIdForJoin)}, null);
 
         long rawContactIds[];
         long verifiedNameRawContactId = -1;
         try {
             rawContactIds = new long[c.getCount()];
             for (int i = 0; i < rawContactIds.length; i++) {
                 c.moveToNext();
                 long rawContactId = c.getLong(JoinContactQuery._ID);
                 rawContactIds[i] = rawContactId;
                 if (c.getLong(JoinContactQuery.CONTACT_ID) == mContactIdForJoin) {
                     if (verifiedNameRawContactId == -1
                             || c.getInt(JoinContactQuery.NAME_VERIFIED) != 0) {
                         verifiedNameRawContactId = rawContactId;
                     }
                 }
             }
         } finally {
             c.close();
         }
 
         // For each pair of raw contacts, insert an aggregation exception
         ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
         for (int i = 0; i < rawContactIds.length; i++) {
             for (int j = 0; j < rawContactIds.length; j++) {
                 if (i != j) {
                     buildJoinContactDiff(operations, rawContactIds[i], rawContactIds[j]);
                 }
             }
         }
 
         // Mark the original contact as "name verified" to make sure that the contact
         // display name does not change as a result of the join
         Builder builder = ContentProviderOperation.newUpdate(
                     ContentUris.withAppendedId(RawContacts.CONTENT_URI, verifiedNameRawContactId));
         builder.withValue(RawContacts.NAME_VERIFIED, 1);
         operations.add(builder.build());
 
         // Apply all aggregation exceptions as one batch
         try {
             resolver.applyBatch(ContactsContract.AUTHORITY, operations);
         } catch (RemoteException e) {
             Log.e(TAG, "Failed to apply aggregation exception batch", e);
             Toast.makeText(mContext, R.string.contactSavedErrorToast, Toast.LENGTH_LONG).show();
         } catch (OperationApplicationException e) {
             Log.e(TAG, "Failed to apply aggregation exception batch", e);
             Toast.makeText(mContext, R.string.contactSavedErrorToast, Toast.LENGTH_LONG).show();
         }
 
         Toast.makeText(mContext, R.string.contactsJoinedMessage, Toast.LENGTH_LONG).show();
 
         // We pass back the Uri of the previous Contact (pre-join). While this is not correct,
         // the provider will be able to later figure out the correct new aggregate
         if (mListener != null) {
             mListener.onAggregationChangeFinished(mLookupUri);
         } else {
             Log.d(TAG, "Listener is null. Can not call onAggregationChangeFinished");
         }
     }
 
     /**
      * Construct a {@link AggregationExceptions#TYPE_KEEP_TOGETHER} ContentProviderOperation.
      */
     private void buildJoinContactDiff(ArrayList<ContentProviderOperation> operations,
             long rawContactId1, long rawContactId2) {
         Builder builder =
                 ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI);
         builder.withValue(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER);
         builder.withValue(AggregationExceptions.RAW_CONTACT_ID1, rawContactId1);
         builder.withValue(AggregationExceptions.RAW_CONTACT_ID2, rawContactId2);
         operations.add(builder.build());
     }
 
     public static interface Listener {
         /**
          * Contact was not found, so somehow close this fragment. This is raised after a contact
          * is removed via Menu/Delete (unless it was a new contact)
          */
         void onContactNotFound();
 
         /**
          * Contact was split or joined, so we can close now.
          * @param newLookupUri The lookup uri of the new contact that should be shown to the user.
          * The editor tries best to chose the most natural contact here.
          */
         void onAggregationChangeFinished(Uri newLookupUri);
 
         /**
          * User was presented with an account selection and couldn't decide.
          */
         void onAccountSelectorAborted();
 
         /**
          * User has tapped Revert, close the fragment now.
          */
         void onReverted();
 
         /**
          * Set the Title (e.g. of the Activity)
          */
         void setTitleTo(int resourceId);
 
         /**
          * Contact was saved and the Fragment can now be closed safely.
          */
         void onSaveFinished(int resultCode, Intent resultIntent);
 
         /**
          * User decided to delete the contact.
          */
         void onDeleteRequested(Uri lookupUri);
     }
 
     private class EntityDeltaComparator implements Comparator<EntityDelta> {
         /**
          * Compare EntityDeltas for sorting the stack of editors.
          */
         @Override
         public int compare(EntityDelta one, EntityDelta two) {
             // Check direct equality
             if (one.equals(two)) {
                 return 0;
             }
 
             final Sources sources = Sources.getInstance(mContext);
             String accountType = one.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
             final ContactsSource oneSource = sources.getInflatedSource(accountType,
                     ContactsSource.LEVEL_SUMMARY);
             accountType = two.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
             final ContactsSource twoSource = sources.getInflatedSource(accountType,
                     ContactsSource.LEVEL_SUMMARY);
 
             // Check read-only
             if (oneSource.readOnly && !twoSource.readOnly) {
                 return 1;
             } else if (twoSource.readOnly && !oneSource.readOnly) {
                 return -1;
             }
 
             // Check account type
             boolean skipAccountTypeCheck = false;
             boolean oneIsGoogle = oneSource instanceof GoogleSource;
             boolean twoIsGoogle = twoSource instanceof GoogleSource;
             if (oneIsGoogle && !twoIsGoogle) {
                 return -1;
             } else if (twoIsGoogle && !oneIsGoogle) {
                 return 1;
             } else if (oneIsGoogle && twoIsGoogle){
                 skipAccountTypeCheck = true;
             }
 
             int value;
             if (!skipAccountTypeCheck) {
                 if (oneSource.accountType == null) {
                     return 1;
                 }
                 value = oneSource.accountType.compareTo(twoSource.accountType);
                 if (value != 0) {
                     return value;
                 }
             }
 
             // Check account name
             ValuesDelta oneValues = one.getValues();
             String oneAccount = oneValues.getAsString(RawContacts.ACCOUNT_NAME);
             if (oneAccount == null) oneAccount = "";
             ValuesDelta twoValues = two.getValues();
             String twoAccount = twoValues.getAsString(RawContacts.ACCOUNT_NAME);
             if (twoAccount == null) twoAccount = "";
             value = oneAccount.compareTo(twoAccount);
             if (value != 0) {
                 return value;
             }
 
             // Both are in the same account, fall back to contact ID
             Long oneId = oneValues.getAsLong(RawContacts._ID);
             Long twoId = twoValues.getAsLong(RawContacts._ID);
             if (oneId == null) {
                 return -1;
             } else if (twoId == null) {
                 return 1;
             }
 
             return (int)(oneId - twoId);
         }
     }
 
     /**
      * Returns the contact ID for the currently edited contact or 0 if the contact is new.
      */
     protected long getContactId() {
         for (EntityDelta rawContact : mState) {
             Long contactId = rawContact.getValues().getAsLong(RawContacts.CONTACT_ID);
             if (contactId != null) {
                 return contactId;
             }
         }
         return 0;
     }
 
 
     private void onContactNameChange(int request, final ContactEditorView rawContactEditor,
             GenericEditorView nameEditor) {
 
         switch (request) {
             case EditorListener.EDITOR_FORM_CHANGED:
                 if (nameEditor.hasShortAndLongForms()) {
                     if (nameEditor.areOptionalFieldsVisible()) {
                         switchFromFullNameToStructuredName(nameEditor);
                     } else {
                         switchFromStructuredNameToFullName(nameEditor);
                     }
                 }
                 break;
 
             case EditorListener.FIELD_CHANGED:
                 if (nameEditor.hasShortAndLongForms()) {
                     if (nameEditor.areOptionalFieldsVisible()) {
                         eraseFullName(nameEditor.getValues());
                     } else {
                         eraseStructuredName(nameEditor.getValues());
                     }
                 }
                 acquireAggregationSuggestions(rawContactEditor);
                 break;
         }
     }
 
     private void switchFromFullNameToStructuredName(GenericEditorView nameEditor) {
         ValuesDelta values = nameEditor.getValues();
 
         String displayName = values.getAsString(StructuredName.DISPLAY_NAME);
         if (displayName == null) {
             displayName = "";
         }
 
         Uri uri = ContactsContract.AUTHORITY_URI.buildUpon().appendPath("complete_name")
                 .appendQueryParameter(StructuredName.DISPLAY_NAME, displayName).build();
         Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{
                 StructuredName.PREFIX,
                 StructuredName.GIVEN_NAME,
                 StructuredName.MIDDLE_NAME,
                 StructuredName.FAMILY_NAME,
                 StructuredName.SUFFIX,
         }, null, null, null);
 
         try {
             if (cursor.moveToFirst()) {
                 eraseFullName(values);
                 values.put(StructuredName.PREFIX, cursor.getString(0));
                 values.put(StructuredName.GIVEN_NAME, cursor.getString(1));
                 values.put(StructuredName.MIDDLE_NAME, cursor.getString(2));
                 values.put(StructuredName.FAMILY_NAME, cursor.getString(3));
                 values.put(StructuredName.SUFFIX, cursor.getString(4));
             }
         } finally {
             cursor.close();
         }
     }
 
     private void switchFromStructuredNameToFullName(GenericEditorView nameEditor) {
         ValuesDelta values = nameEditor.getValues();
 
         Uri.Builder builder = ContactsContract.AUTHORITY_URI.buildUpon().appendPath(
                 "complete_name");
         appendQueryParameter(builder, values, StructuredName.PREFIX);
         appendQueryParameter(builder, values, StructuredName.GIVEN_NAME);
         appendQueryParameter(builder, values, StructuredName.MIDDLE_NAME);
         appendQueryParameter(builder, values, StructuredName.FAMILY_NAME);
         appendQueryParameter(builder, values, StructuredName.SUFFIX);
         Uri uri = builder.build();
         Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{
                 StructuredName.DISPLAY_NAME,
         }, null, null, null);
 
         try {
             if (cursor.moveToFirst()) {
                 eraseStructuredName(values);
                 values.put(StructuredName.DISPLAY_NAME, cursor.getString(0));
             }
         } finally {
             cursor.close();
         }
     }
 
     private void eraseFullName(ValuesDelta values) {
         values.putNull(StructuredName.DISPLAY_NAME);
     }
 
     private void eraseStructuredName(ValuesDelta values) {
         values.putNull(StructuredName.PREFIX);
         values.putNull(StructuredName.GIVEN_NAME);
         values.putNull(StructuredName.MIDDLE_NAME);
         values.putNull(StructuredName.FAMILY_NAME);
         values.putNull(StructuredName.SUFFIX);
     }
 
     private void appendQueryParameter(Uri.Builder builder, ValuesDelta values, String field) {
         String value = values.getAsString(field);
         if (!TextUtils.isEmpty(value)) {
             builder.appendQueryParameter(field, value);
         }
     }
 
     /**
      * Triggers an asynchronous search for aggregation suggestions.
      */
     public void acquireAggregationSuggestions(ContactEditorView rawContactEditor) {
         if (mAggregationSuggestionEngine == null) {
             mAggregationSuggestionEngine = new AggregationSuggestionEngine(getActivity());
             mAggregationSuggestionEngine.setContactId(getContactId());
             mAggregationSuggestionEngine.setListener(this);
             mAggregationSuggestionEngine.start();
         }
 
         GenericEditorView nameEditor = rawContactEditor.getNameEditor();
         mAggregationSuggestionEngine.onNameChange(nameEditor.getValues());
     }
 
     @Override
     public void onAggregationSuggestionChange() {
         // We may have chosen some contact to join with but then changed the contact name.
         // Let's drop the obsolete decision to join.
         setSelectedAggregationSuggestion(0, null);
         updateAggregationSuggestionView();
     }
 
     protected void updateAggregationSuggestionView() {
         ViewStub stub = (ViewStub)mContent.findViewById(R.id.aggregation_suggestion_stub);
         if (stub != null) {
             stub.inflate();
             final View view = mContent.findViewById(R.id.aggregation_suggestion);
             mContent.postDelayed(new Runnable() {
 
                 @Override
                 public void run() {
                     requestAggregationSuggestionOnScreen(view);
                 }
             }, AGGREGATION_SUGGESTION_SCROLL_DELAY);
         }
 
         View view = mContent.findViewById(R.id.aggregation_suggestion);
 
         List<Suggestion> suggestions = null;
         int count = mAggregationSuggestionEngine.getSuggestedContactCount();
         if (count > 0) {
             suggestions = mAggregationSuggestionEngine.getSuggestions();
 
             if (mContactIdForJoin != 0) {
                 Suggestion chosenSuggestion = null;
                 for (Suggestion suggestion : suggestions) {
                     if (suggestion.contactId == mContactIdForJoin) {
                         chosenSuggestion = suggestion;
                         break;
                     }
                 }
 
                 if (chosenSuggestion != null) {
                     suggestions = Lists.newArrayList(chosenSuggestion);
                 } else {
                     // If the contact we wanted to join with is no longer suggested,
                     // forget our decision to join with it.
                     setSelectedAggregationSuggestion(0, null);
                 }
             }
 
             count = suggestions.size();
         }
 
         if (count == 0) {
             view.setVisibility(View.GONE);
             return;
         }
 
         TextView title = (TextView) view.findViewById(R.id.aggregation_suggestion_title);
         if (mContactIdForJoin != 0) {
             title.setText(R.string.aggregation_suggestion_joined_title);
         } else {
             title.setText(getActivity().getResources().getQuantityString(
                     R.plurals.aggregation_suggestion_title, count));
         }
 
         LinearLayout itemList = (LinearLayout) view.findViewById(R.id.aggregation_suggestions);
         itemList.removeAllViews();
 
         LayoutInflater inflater = getActivity().getLayoutInflater();
 
         for (Suggestion suggestion : suggestions) {
             AggregationSuggestionView suggestionView =
                     (AggregationSuggestionView) inflater.inflate(
                             R.layout.aggregation_suggestions_item, null);
             suggestionView.setLayoutParams(
                     new LinearLayout.LayoutParams(
                             LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
             suggestionView.setListener(new AggregationSuggestionView.Listener() {
 
                 @Override
                 public void onJoinAction(long contactId, List<Long> rawContactIds) {
                     mState.setJoinWithRawContacts(rawContactIds);
                     // If we are in the edit mode (indicated by a non-zero contact ID),
                     // join the suggested contact, save all changes, and stay in the editor.
                     if (getContactId() != 0) {
                         doSaveAction(SaveMode.RELOAD);
                     } else {
                         mContactIdForJoin = contactId;
                         updateAggregationSuggestionView();
                     }
                 }
             });
             suggestionView.bindSuggestion(suggestion, mContactIdForJoin == 0 /* showJoinButton */);
             itemList.addView(suggestionView);
         }
         view.setVisibility(View.VISIBLE);
     }
 
     /**
      * Scrolls the editor if necessary to reveal the aggregation suggestion that is
      * shown below the name editor. Makes sure that the currently focused field
      * remains visible.
      */
     private void requestAggregationSuggestionOnScreen(final View view) {
         Rect rect = getRelativeBounds(mContent, view);
         View focused = mContent.findFocus();
         if (focused != null) {
             rect.union(getRelativeBounds(mContent, focused));
         }
         mContent.requestRectangleOnScreen(rect);
     }
 
     /**
      * Computes bounds of the supplied view relative to its ascendant.
      */
     private Rect getRelativeBounds(View ascendant, View view) {
         Rect rect = new Rect();
         rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
 
         View parent = (View) view.getParent();
         while (parent != ascendant) {
             rect.offset(parent.getLeft(), parent.getTop());
             parent = (View) parent.getParent();
         }
         return rect;
     }
 
     protected void setSelectedAggregationSuggestion(long contactId, List<Long> rawContactIds) {
         mContactIdForJoin = contactId;
         mState.setJoinWithRawContacts(rawContactIds);
     }
 
     // TODO: There has to be a nicer way than this WeakAsyncTask...? Maybe call a service?
     /**
      * Background task for persisting edited contact data, using the changes
      * defined by a set of {@link EntityDelta}. This task starts
      * {@link EmptyService} to make sure the background thread can finish
      * persisting in cases where the system wants to reclaim our process.
      */
     public static class PersistTask extends
             WeakAsyncTask<EntityDeltaList, Void, Integer, ContactEditorFragment> {
         private static final int PERSIST_TRIES = 3;
 
         private static final int RESULT_UNCHANGED = 0;
         private static final int RESULT_SUCCESS = 1;
         private static final int RESULT_FAILURE = 2;
 
         private final Context mContext;
 
         private WeakReference<ProgressDialog> mProgress;
 
         private int mSaveMode;
         private Uri mContactLookupUri = null;
 
         public PersistTask(ContactEditorFragment target, int saveMode) {
             super(target);
             mSaveMode = saveMode;
             mContext = target.mContext;
         }
 
         /** {@inheritDoc} */
         @Override
         protected void onPreExecute(ContactEditorFragment target) {
             mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(target.getActivity(),
                     null, target.getActivity().getText(R.string.savingContact)));
 
             // Before starting this task, start an empty service to protect our
             // process from being reclaimed by the system.
             mContext.startService(new Intent(mContext, EmptyService.class));
         }
 
         /** {@inheritDoc} */
         @Override
         protected Integer doInBackground(ContactEditorFragment target, EntityDeltaList... params) {
             final ContentResolver resolver = mContext.getContentResolver();
 
             EntityDeltaList state = params[0];
 
             // Trim any empty fields, and RawContacts, before persisting
             final Sources sources = Sources.getInstance(mContext);
             EntityModifier.trimEmpty(state, sources);
 
             // Attempt to persist changes
             int tries = 0;
             Integer result = RESULT_FAILURE;
             while (tries++ < PERSIST_TRIES) {
                 try {
                     // Build operations and try applying
                     final ArrayList<ContentProviderOperation> diff = state.buildDiff();
                     ContentProviderResult[] results = null;
                     if (!diff.isEmpty()) {
                         results = resolver.applyBatch(ContactsContract.AUTHORITY, diff);
                     }
 
                     final long rawContactId = getRawContactId(state, diff, results);
                     if (rawContactId != -1) {
                         final Uri rawContactUri = ContentUris.withAppendedId(
                                 RawContacts.CONTENT_URI, rawContactId);
 
                         // convert the raw contact URI to a contact URI
                         mContactLookupUri = RawContacts.getContactLookupUri(resolver,
                                 rawContactUri);
                         Log.d(TAG, "Looked up RawContact Uri " + rawContactUri +
                                 " into ContactLookupUri " + mContactLookupUri);
                     } else {
                         Log.w(TAG, "Could not determine RawContact ID after save");
                     }
                     result = (diff.size() > 0) ? RESULT_SUCCESS : RESULT_UNCHANGED;
                     break;
 
                 } catch (RemoteException e) {
                     // Something went wrong, bail without success
                     Log.e(TAG, "Problem persisting user edits", e);
                     break;
 
                 } catch (OperationApplicationException e) {
                     // Version consistency failed, re-parent change and try again
                     Log.w(TAG, "Version consistency failed, re-parenting: " + e.toString());
                     final EntityDeltaList newState = EntityDeltaList.fromQuery(resolver,
                             target.mQuerySelection, null, null);
                     state = EntityDeltaList.mergeAfter(newState, state);
                 }
             }
 
             return result;
         }
 
         private long getRawContactId(EntityDeltaList state,
                 final ArrayList<ContentProviderOperation> diff,
                 final ContentProviderResult[] results) {
             long rawContactId = state.findRawContactId();
             if (rawContactId != -1) {
                 return rawContactId;
             }
 
 
             // we gotta do some searching for the id
             final int diffSize = diff.size();
             for (int i = 0; i < diffSize; i++) {
                 ContentProviderOperation operation = diff.get(i);
                 if (operation.getType() == ContentProviderOperation.TYPE_INSERT
                         && operation.getUri().getEncodedPath().contains(
                                 RawContacts.CONTENT_URI.getEncodedPath())) {
                     return ContentUris.parseId(results[i].uri);
                 }
             }
             return -1;
         }
 
         /** {@inheritDoc} */
         @Override
         protected void onPostExecute(ContactEditorFragment target, Integer result) {
             Log.d(TAG, "onPostExecute(something," + result + "). mSaveMode=" + mSaveMode);
             if (result == RESULT_SUCCESS && mSaveMode != SaveMode.JOIN) {
                 Toast.makeText(mContext, R.string.contactSavedToast, Toast.LENGTH_SHORT).show();
             } else if (result == RESULT_FAILURE) {
                 Toast.makeText(mContext, R.string.contactSavedErrorToast, Toast.LENGTH_LONG).show();
             }
 
             final ProgressDialog progress = mProgress.get();
             if (progress != null && progress.isShowing()) {
                 try {
                     progress.dismiss();
                 } catch (Exception e) {
                     // this can happen if our view has already been closed. this can safely be
                     // ignored
                 }
             }
 
             // Stop the service that was protecting us
             mContext.stopService(new Intent(mContext, EmptyService.class));
 
             target.onSaveCompleted(result != RESULT_FAILURE, mSaveMode, mContactLookupUri);
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putParcelable(KEY_URI, mLookupUri);
         outState.putString(KEY_ACTION, mAction);
 
         if (hasValidState()) {
             // Store entities with modifications
             outState.putParcelable(KEY_EDIT_STATE, mState);
         }
 
         outState.putLong(KEY_RAW_CONTACT_ID_REQUESTING_PHOTO, mRawContactIdRequestingPhoto);
         outState.putParcelable(KEY_VIEW_ID_GENERATOR, mViewIdGenerator);
         if (mCurrentPhotoFile != null) {
             outState.putString(KEY_CURRENT_PHOTO_FILE, mCurrentPhotoFile.toString());
         }
         outState.putString(KEY_QUERY_SELECTION, mQuerySelection);
         outState.putLong(KEY_CONTACT_ID_FOR_JOIN, mContactIdForJoin);
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         // Ignore failed requests
         if (resultCode != Activity.RESULT_OK) return;
         switch (requestCode) {
             case REQUEST_CODE_PHOTO_PICKED_WITH_DATA: {
                 // As we are coming back to this view, the editor will be reloaded automatically,
                 // which will cause the photo that is set here to disappear. To prevent this,
                 // we remember to set a flag which is interpreted after loading.
                 // This photo is set here already to reduce flickering.
                 mPhoto = data.getParcelableExtra("data");
                 setPhoto(mRawContactIdRequestingPhoto, mPhoto);
                 mRawContactIdRequestingPhotoAfterLoad = mRawContactIdRequestingPhoto;
                 mRawContactIdRequestingPhoto = -1;
 
                 break;
             }
             case REQUEST_CODE_CAMERA_WITH_DATA: {
                 doCropPhoto(mCurrentPhotoFile);
                 break;
             }
             case REQUEST_CODE_JOIN: {
                 if (data != null) {
                     final long contactId = ContentUris.parseId(data.getData());
                     joinAggregate(contactId);
                 }
                 break;
             }
         }
     }
 
     /**
      * Sets the photo stored in mPhoto and writes it to the RawContact with the given id
      */
     private void setPhoto(long rawContact, Bitmap photo) {
         BaseContactEditorView requestingEditor = null;
         for (int i = 0; i < mContent.getChildCount(); i++) {
             final View childView = mContent.getChildAt(i);
             if (childView instanceof BaseContactEditorView) {
                 final BaseContactEditorView editor = (BaseContactEditorView) childView;
                 if (editor.getRawContactId() == rawContact) {
                     requestingEditor = editor;
                     break;
                 }
             }
         }
 
         if (requestingEditor != null) {
             requestingEditor.setPhotoBitmap(photo);
         } else {
             Log.w(TAG, "The contact that requested the photo is no longer present.");
         }
     }
 
     public Uri getLookupUri() {
         return mLookupUri;
     }
 
     /**
      * The listener for the data loader
      */
     private final LoaderManager.LoaderCallbacks<ContactLoader.Result> mDataLoaderListener =
             new LoaderCallbacks<ContactLoader.Result>() {
         @Override
         public Loader<ContactLoader.Result> onCreateLoader(int id, Bundle args) {
             mLoaderStartTime = SystemClock.elapsedRealtime();
             return new ContactLoader(mContext, mLookupUri);
         }
 
         @Override
         public void onLoadFinished(Loader<ContactLoader.Result> loader, ContactLoader.Result data) {
             final long loaderCurrentTime = SystemClock.elapsedRealtime();
             Log.v(TAG, "Time needed for loading: " + (loaderCurrentTime-mLoaderStartTime));
            if (data == ContactLoader.Result.NOT_FOUND) {
                 // Item has been deleted
                 Log.i(TAG, "No contact found. Closing activity");
                 if (mListener != null) mListener.onContactNotFound();
                 return;
             }
 
             mStatus = Status.EDITING;
             final long setDataStartTime = SystemClock.elapsedRealtime();
             setData(data);
             final long setDataEndTime = SystemClock.elapsedRealtime();
 
             // If we are coming back from the photo trimmer, this will be set.
             if (mRawContactIdRequestingPhotoAfterLoad != -1) {
                 setPhoto(mRawContactIdRequestingPhotoAfterLoad, mPhoto);
                 mRawContactIdRequestingPhotoAfterLoad = -1;
                 mPhoto = null;
             }
             Log.v(TAG, "Time needed for setting UI: " + (setDataEndTime-setDataStartTime));
         }
     };
 
     /**
      * The listener for the group meta data loader
      */
     private final LoaderManager.LoaderCallbacks<Cursor> mGroupLoaderListener =
             new LoaderCallbacks<Cursor>() {
 
         @Override
         public CursorLoader onCreateLoader(int id, Bundle args) {
             return new GroupMetaDataLoader(mContext);
         }
 
         @Override
         public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
             mGroupMetaData = data;
             bindGroupMetaData();
         }
     };
 
     @Override
     public void onSplitContactConfirmed() {
         mState.markRawContactsForSplitting();
         doSaveAction(SaveMode.SPLIT);
     }
 
     /**
      * Launches Camera to take a picture and store it in a file.
      */
     @Override
     public void onTakePhotoChosen() {
         try {
             // Launch camera to take photo for selected contact
             PHOTO_DIR.mkdirs();
             mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());
             final Intent intent = getTakePickIntent(mCurrentPhotoFile);
 
             startActivityForResult(intent, REQUEST_CODE_CAMERA_WITH_DATA);
         } catch (ActivityNotFoundException e) {
             Toast.makeText(mContext, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
         }
     }
 
     /**
      * Launches Gallery to pick a photo.
      */
     @Override
     public void onPickFromGalleryChosen() {
         try {
             // Launch picker to choose photo for selected contact
             final Intent intent = getPhotoPickIntent();
             startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKED_WITH_DATA);
         } catch (ActivityNotFoundException e) {
             Toast.makeText(mContext, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
         }
     }
 
     /**
      * Account was chosen in the selector. Create a RawContact for this account now
      */
     @Override
     public void onAccountChosen(Account account, boolean isNewContact) {
         createContact(account, isNewContact);
     }
 
     /**
      * The account selector has been aborted. If we are in "New" mode, we have to close now
      */
     @Override
     public void onAccountSelectorCancelled() {
         if (!hasValidState() && mListener != null) {
             mListener.onAccountSelectorAborted();
         }
     }
 
     /**
      * User has chosen to set the selected photo as the (super) primary photo
      */
     @Override
     public void onUseAsPrimaryChosen(long rawContactId) {
         // Set the IsSuperPrimary for each editor
         int count = mContent.getChildCount();
         for (int i = 0; i < count; i++) {
             final View childView = mContent.getChildAt(i);
             if (childView instanceof BaseContactEditorView) {
                 final BaseContactEditorView editor = (BaseContactEditorView) childView;
                 final PhotoEditorView photoEditor = editor.getPhotoEditor();
                 photoEditor.setSuperPrimary(editor.getRawContactId() == rawContactId);
             }
         }
     }
 
     /**
      * User has chosen to remove a picture
      */
     @Override
     public void onRemovePictureChose(long rawContactId) {
         // find the correct editor and remove it's photo
         final int editorCount = mContent.getChildCount();
         for (int i = 0; i < editorCount; i++) {
             final View child = mContent.getChildAt(i);
             if (child instanceof BaseContactEditorView) {
                 final BaseContactEditorView editor =
                     (BaseContactEditorView) child;
                 if (editor.getRawContactId() == rawContactId) {
                     editor.setPhotoBitmap(null);
                     break;
                 }
             }
         }
     }
 
     /**
      * User has chosen to change a picture
      */
     @Override
     public void onChangePictureChosen(long rawContactId) {
         // Pick a new photo for the contact
         doPickPhotoAction(rawContactId);
     }
 }
