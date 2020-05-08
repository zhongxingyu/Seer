 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.controller;
 
 import static com.zapta.apps.maniana.util.Assertions.check;
 
 import java.io.InputStream;
 
 import javax.annotation.Nullable;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.TextView;
 
 import com.zapta.apps.maniana.R;
 import com.zapta.apps.maniana.annotations.MainActivityScope;
 import com.zapta.apps.maniana.backup.RestoreBackupDialog;
 import com.zapta.apps.maniana.backup.RestoreBackupDialog.Action;
 import com.zapta.apps.maniana.backup.RestoreBackupDialog.RestoreBackupDialogListener;
 import com.zapta.apps.maniana.editors.ItemTextEditor;
 import com.zapta.apps.maniana.editors.ItemVoiceEditor;
 import com.zapta.apps.maniana.help.HelpUtil;
 import com.zapta.apps.maniana.help.PopupMessageActivity;
 import com.zapta.apps.maniana.help.PopupMessageActivity.MessageKind;
 import com.zapta.apps.maniana.main.MainActivityResumeAction;
 import com.zapta.apps.maniana.main.MainActivityState;
 import com.zapta.apps.maniana.menus.ItemMenuEntry;
 import com.zapta.apps.maniana.menus.MainMenuEntry;
 import com.zapta.apps.maniana.model.AppModel;
 import com.zapta.apps.maniana.model.ItemColor;
 import com.zapta.apps.maniana.model.ItemModel;
 import com.zapta.apps.maniana.model.ItemModelReadOnly;
 import com.zapta.apps.maniana.model.OrganizePageSummary;
 import com.zapta.apps.maniana.model.PageKind;
 import com.zapta.apps.maniana.model.PushScope;
 import com.zapta.apps.maniana.notifications.NotificationUtil;
 import com.zapta.apps.maniana.persistence.ModelDeserialization;
 import com.zapta.apps.maniana.persistence.ModelPersistence;
 import com.zapta.apps.maniana.persistence.PersistenceMetadata;
 import com.zapta.apps.maniana.services.MidnightTicker;
 import com.zapta.apps.maniana.services.ShakeImpl;
 import com.zapta.apps.maniana.services.Shaker;
 import com.zapta.apps.maniana.services.Shaker.ShakerListener;
 import com.zapta.apps.maniana.settings.PreferenceKind;
 import com.zapta.apps.maniana.settings.SettingsActivity;
 import com.zapta.apps.maniana.settings.ShakerAction;
 import com.zapta.apps.maniana.util.AttachmentUtil;
 import com.zapta.apps.maniana.util.CalendarUtil;
 import com.zapta.apps.maniana.util.FileUtil;
 import com.zapta.apps.maniana.util.FileUtil.FileReadResult;
 import com.zapta.apps.maniana.util.IdGenerator;
 import com.zapta.apps.maniana.util.LogUtil;
 import com.zapta.apps.maniana.view.AppView;
 import com.zapta.apps.maniana.view.AppView.ItemAnimationType;
 import com.zapta.apps.maniana.widget.BaseWidgetProvider;
 
 /**
  * The controller class. Contains main app logic. Interacts with the model (data) and view
  * (display).
  * 
  * @author Tal Dayan
  */
 @MainActivityScope
 public class Controller implements ShakerListener {
 
     // Adding a task with this exact text turns on the debug mode.
     // Debug mode exposes few commands that may be useful for developers. When enabled,
     // debug mode commands are available via the 'Debug' entry in the main menu.
     // Note that debug commands can change any time as the developers needs change.
     // Also, we don't bother to translate debug mode strings. They are always in
     // English.
     private static final String DEBUG_MODE_TASK_CODE = "#d#";
 
     private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
 
     /** The app context. Provide access to the model, view and services. */
     private final MainActivityState mMainActivityState;
 
     private final ItemMenuCache mItemMenuCache;
 
     /** Used to detect first app resume to trigger the startup animation. */
     private int mOnAppResumeCount = 0;
 
     /** Indicates when the resume operation should populate the model with new user data. */
     private boolean mPopulateNewUserSampleDataOnResume = false;
 
     /**
      * Used to determine if the resume is from own sub activity (e.g. voice or help) as opposed to
      * app re-entry.
      */
     private boolean mInSubActivity = false;
 
     @Nullable
     private Shaker mOptionalShaker = null;
 
     /**
      * Preallocated temp object. Used to reduce object alloctation.
      */
     private final OrganizePageSummary mTempSummary = new OrganizePageSummary();
 
     public Controller(MainActivityState mainActivityState) {
         mMainActivityState = mainActivityState;
         mItemMenuCache = new ItemMenuCache(mainActivityState);
     }
 
     /** Called by the view when user clicks on item's text area */
     public void onItemTextClick(PageKind pageKind, int itemIndex) {
         mMainActivityState.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
         showItemMenu(pageKind, itemIndex);
     }
 
     /** Called the view when user clicks on item's color swatch area */
     public final void onItemColorClick(final PageKind pageKind, final int itemIndex) {
         mMainActivityState.services().maybePlayStockSound(AudioManager.FX_KEYPRESS_SPACEBAR, false);
         final ItemModel item = mMainActivityState.model().getItemForMutation(pageKind, itemIndex);
         item.setColor(item.getColor().nextCyclicColor());
         mMainActivityState.view().updatePage(pageKind);
     }
 
     /** Called by the view when user clicks on item's arrow/lock area */
     public final void onItemArrowClick(final PageKind pageKind, final int itemIndex) {
         // If item locked, show item menu, allowing to unlock it.
         if (mMainActivityState.model().getItemReadOnly(pageKind, itemIndex).isLocked()) {
             showItemMenu(pageKind, itemIndex);
             return;
         }
 
         // Here when item is not locked. Animate and move to other page and do the actual
         // move in the model at the end of the animation.
         mMainActivityState.services().maybePlayStockSound(AudioManager.FX_KEYPRESS_RETURN, false);
         mMainActivityState.view().startItemAnimation(pageKind, itemIndex,
                 AppView.ItemAnimationType.MOVING_ITEM_TO_OTHER_PAGE, 0, new Runnable() {
                     @Override
                     public void run() {
                         moveItemToOtherPage(pageKind, itemIndex);
                     }
                 });
     }
 
     /**
      * Move a model item to the other page.
      * 
      * @param pageKind the source page.
      * @param itemIndex item index in the source page.
      */
     private final void moveItemToOtherPage(PageKind pageKind, int itemIndex) {
         // Remove item from current page.
         final ItemModel item = mMainActivityState.model().removeItem(pageKind, itemIndex);
         // Insert at the beginning of the other page.
         final PageKind otherPageKind = pageKind.otherPageKind();
         mMainActivityState.model().insertItem(otherPageKind, 0, item);
 
         mMainActivityState.model().clearAllUndo();
         maybeAutoSortPage(otherPageKind, false, false);
         mMainActivityState.view().updatePages();
 
         // The item is inserted at the top of the other page. Scroll there so it is visible
         // if the user flips to the other page.
         // NOTE(tal): This must be done after updateAlLPages(), otherwise it is ignored.
         mMainActivityState.view().scrollToItem(otherPageKind, 0);
     }
 
     /** Called by the view when the user drag an item within the page */
     public final void onItemMoveInPage(final PageKind pageKind, final int sourceItemIndex,
             final int destinationItemIndex) {
         final ItemModel itemModel = mMainActivityState.model()
                 .removeItem(pageKind, sourceItemIndex);
         // NOTE(tal): if source index < destination index, the item removal above affect the index
         // of the destination by 1. Despite that, we don't compensate for it as this acieve a more
         // intuitive behavior and allow to move an item to the end of the list.
         mMainActivityState.model().insertItem(pageKind, destinationItemIndex, itemModel);
         mMainActivityState.view().updatePage(pageKind);
         mMainActivityState.view().getRootView().post(new Runnable() {
             @Override
             public void run() {
                 maybeAutosortPageWithItemOfInterest(pageKind, destinationItemIndex);
             }
         });
 
     }
 
     /** Called when the activity is paused */
     public final void onMainActivityPause() {
         if (mOptionalShaker != null) {
             mOptionalShaker.pause();
         }
         // Close any leftover dialogs. This provides a more intuitive user experience.
         mMainActivityState.popupsTracker().closeAllLeftOvers();
         flushModelChanges(false);
     }
 
     /** If model is dirty then persist and update widgets. */
     private final void flushModelChanges(boolean alwaysUpdateAllWidgets) {
         // If state is dirty persist data so we don't lose it if the app will not resumed.
         final boolean modelWasDirty = mMainActivityState.model().isDirty();
         if (modelWasDirty) {
             final PersistenceMetadata metadata = new PersistenceMetadata(mMainActivityState
                     .services().getAppVersionCode(), mMainActivityState.services()
                     .getAppVersionName());
             // NOTE(tal): this clears the dirty bit.
             ModelPersistence.writeModelFile(mMainActivityState, mMainActivityState.model(),
                     metadata);
             check(!mMainActivityState.model().isDirty());
             onBackupDataChange();
 
             // Use this opportunity also to garbage collect old attachment files.
             AttachmentUtil.garbageCollectAttachmentFile(mMainActivityState.context());
         }
         if (modelWasDirty || alwaysUpdateAllWidgets) {
             updateAllWidgets();
         }
     }
 
     /** Called when the main activity is resumed, including after app creation. */
     public final void onMainActivityResume(MainActivityResumeAction resumeAction,
             @Nullable Intent resumeIntent) {
         // This may leave undo items in case we cleanup completed tasks.
         maybeHandleDateChange();
 
         NotificationUtil.clearPendingItemsNotification(mMainActivityState.context());
 
         // Keep the midnight ticker going, just in case.
         MidnightTicker.scheduleMidnightTicker(mMainActivityState.context());
 
         ++mOnAppResumeCount;
 
         // We suppress the population of new user sample tasks if the first resume is with certain
         // actions.
         // It seems to be more intuitive this way.
         if (mPopulateNewUserSampleDataOnResume) {
             if (resumeAction != MainActivityResumeAction.RESTORE_FROM_BABKUP_FILE) {
                 populateModelWithSampleTasks(mMainActivityState.model());
                 startPopupMessageSubActivity(MessageKind.NEW_USER);
             }
             mMainActivityState.model().setLastPushDateStamp(
                     mMainActivityState.dateTracker().getDateStampString());
             mMainActivityState.model().setDirty();
             mPopulateNewUserSampleDataOnResume = false;
         }
 
         // Typically we reset the view to default position (both pages are scrolled
        // to the top, Today page is shown) when the app is resumed. We preserve the page and 
         // scroll only when it is resumed from a sub activity (e.g. settings or startup message)
         // with no resume action.
         final boolean preserveView = mInSubActivity && resumeAction.isNone();
 
         if (!preserveView) {
             // Force both pages to be scrolled to top. More intuitive this way.
             mMainActivityState.view().scrollToItem(PageKind.TOMOROW, 0);
             mMainActivityState.view().scrollToItem(PageKind.TODAY, 0);
 
             // Force showing today's page. This is done with animation or immediately
             // depending on settings. In case of an actual resume action, we skip
             // the animation to save user's time.
             final boolean isAppStartup = (mOnAppResumeCount == 1);
             final boolean doAnimation = isAppStartup && resumeAction.allowsAnimations()
                     && mMainActivityState.prefTracker().getStartupAnimationPreference();
             if (doAnimation) {
                 // Show initial animation
                 mMainActivityState.view().setCurrentPage(PageKind.TOMOROW, -1);
                 mMainActivityState.view().getRootView().postDelayed(new Runnable() {
                     @Override
                     public void run() {
                         mMainActivityState.view().setCurrentPage(PageKind.TODAY, 800);
                     }
                 }, 500);
             } else {
                 // No animation. Jump directly to Today.
                 mMainActivityState.view().setCurrentPage(
                         resumeAction.isForceTomorowPage() ? PageKind.TOMOROW : PageKind.TODAY, 0);
             }
         }
 
         // Reset the in sub activity tracking .
         mInSubActivity = false;
 
         maybeAutoSortPages(true, true);
 
         // Dispatch optional resume action by simulating user clicks. By the logic
         // above, if resumeAction is not NONE, here the TODAY page is already displayed and
         // is scrolled all the way to the top.
         switch (resumeAction) {
             case ADD_NEW_ITEM_BY_TEXT:
                 onAddItemByTextButton(PageKind.TODAY);
                 break;
             case ADD_NEW_ITEM_BY_VOICE:
                 onAddItemByVoiceButton(PageKind.TODAY);
                 break;
             case RESTORE_FROM_BABKUP_FILE:
                 onRestoreBackupFromFileClick(resumeIntent);
                 break;
             case NONE:
             case SHOW_TODAY_PAGE:
             case FORCE_TODAY_PAGE:
             case FORCE_TOMORROW_PAGE:
             default:
                 // Do nothing
         }
 
         resumeShaker();
     }
 
     /**
      * Called when main activity is resumed. It updates the shaker state based on current settings.
      */
     private final void resumeShaker() {
         if (mMainActivityState.prefTracker().getShakerEnabledPreference()) {
             final boolean installShaker = (mOptionalShaker == null);
             if (installShaker) {
                 mOptionalShaker = new ShakeImpl(mMainActivityState.context(), this);
             }
             final boolean shakerSupported = mOptionalShaker.resume(mMainActivityState.prefTracker()
                     .getShakerSensitivityPreference());
             if (installShaker && !shakerSupported) {
                 mMainActivityState.services().toast(
                         mMainActivityState.str(R.string.shaking_service_not_available));
             }
         } else {
             if (mOptionalShaker != null) {
                 mOptionalShaker.pause();
                 mOptionalShaker = null;
             }
         }
     }
 
     /** Populate given model with new user's sample tasks. */
     private final void populateModelWithSampleTasks(AppModel model) {
         // Today's page
         final long ts = System.currentTimeMillis();
         model.appendItem(PageKind.TODAY, new ItemModel(ts, IdGenerator.getFreshId(),
                 mMainActivityState.str(R.string.sample_tast_text_11), false, false, ItemColor.NONE));
         model.appendItem(PageKind.TODAY, new ItemModel(ts, IdGenerator.getFreshId(),
                 mMainActivityState.str(R.string.sample_tast_text_12), false, false, ItemColor.NONE));
         model.appendItem(PageKind.TODAY, new ItemModel(ts, IdGenerator.getFreshId(),
                 mMainActivityState.str(R.string.sample_tast_text_13), false, false, ItemColor.NONE));
         model.appendItem(PageKind.TODAY, new ItemModel(ts, IdGenerator.getFreshId(),
                 mMainActivityState.str(R.string.sample_tast_text_14), false, false, ItemColor.RED));
         model.appendItem(PageKind.TODAY, new ItemModel(ts, IdGenerator.getFreshId(),
                 mMainActivityState.str(R.string.sample_tast_text_15), false, false, ItemColor.BLUE));
         model.appendItem(PageKind.TODAY, new ItemModel(ts, IdGenerator.getFreshId(),
                 mMainActivityState.str(R.string.sample_tast_text_16), false, false, ItemColor.NONE));
 
         // Tommorow's page
         model.appendItem(PageKind.TOMOROW, new ItemModel(ts, IdGenerator.getFreshId(),
                 mMainActivityState.str(R.string.sample_tast_text_21), false, false, ItemColor.NONE));
     }
 
     /** Update date and if needed push model items from Tomorow to Today. */
     private void maybeHandleDateChange() {
         // Sample and cache the current date.
         mMainActivityState.dateTracker().updateDate();
 
         // TODO: filter out redundant view date changes? (do not update unless date changed)
         mMainActivityState.view().onDateChange();
 
         // A quick check for the normal case where the last push was today.
         final String modelPushDateStamp = mMainActivityState.model().getLastPushDateStamp();
         final String trackerTodayDateStamp = mMainActivityState.dateTracker().getDateStampString();
         if (trackerTodayDateStamp.equals(modelPushDateStamp)) {
             return;
         }
 
         // Determine if to expire all locks
         final PushScope pushScope = mMainActivityState.dateTracker().computePushScope(
                 modelPushDateStamp,
                 mMainActivityState.prefTracker().reader().getLockExpierationPeriodPreference());
 
         if (pushScope == PushScope.NONE) {
             // Not expected because of the quick check above
             LogUtil.error("*** Unexpected condition, pushScope=NONE,"
                     + " modelTimestamp=%s, trackerDateStamp=%s", modelPushDateStamp,
                     trackerTodayDateStamp);
         } else {
             final boolean expireAllLocks = (pushScope == PushScope.ALL);
             final boolean deleteCompletedItems = mMainActivityState.prefTracker().reader()
                     .getAutoDailyCleanupPreference();
             LogUtil.info("Model push scope: %s, auto_cleanup=%s", pushScope, deleteCompletedItems);
             mMainActivityState.model().pushToToday(expireAllLocks, deleteCompletedItems);
             // Not bothering to test if anything changed. Always updating. This happens only once a
             // day.
             mMainActivityState.model().clearAllUndo();
             mMainActivityState.view().updatePages();
         }
 
         mMainActivityState.model().setLastPushDateStamp(
                 mMainActivityState.dateTracker().getDateStampString());
     }
 
     /** Show the popup menu for a given item. Item is assumed to be already visible. */
     private final void showItemMenu(PageKind pageKind, int itemIndex) {
         final ItemModelReadOnly item = mMainActivityState.model().getItemReadOnly(pageKind,
                 itemIndex);
 
         // Done vs ToDo based on item isCompleted status.
         final ItemMenuEntry doneAction = item.isCompleted() ? mItemMenuCache.getToDoAction()
                 : mItemMenuCache.getDoneAction();
 
         // Edit.
         final ItemMenuEntry editAction = mItemMenuCache.getEditAction();
 
         // Lock vs Unlock based on item isLocked status.
         final ItemMenuEntry lockAction = item.isLocked() ? mItemMenuCache.getUnlockAction()
                 : mItemMenuCache.getLockAction();
 
         // Delete.
         final ItemMenuEntry deleteAction = mItemMenuCache.getDeleteAction();
 
         // Action list
         final ItemMenuEntry actions[] = {
             doneAction,
             editAction,
             lockAction,
             deleteAction
         };
 
         mMainActivityState.view().setItemViewHighlight(pageKind, itemIndex, true);
         mMainActivityState.view().showItemMenu(pageKind, itemIndex, actions,
                 ItemMenuCache.DISMISS_WITH_NO_SELECTION_ID);
     }
 
     /** Called when the user made a selection from an item popup menu. */
     public void onItemMenuSelection(final PageKind pageKind, final int itemIndex, int actionId) {
         // In case of dismissal with no selection we don't clear the undo buffer.
         if (actionId != ItemMenuCache.DISMISS_WITH_NO_SELECTION_ID) {
             clearPageUndo(pageKind);
         }
 
         // Clear the item-highlighted state when the menu was shown.
         mMainActivityState.view().setItemViewHighlight(pageKind, itemIndex, false);
 
         // Handle the action
         switch (actionId) {
             case ItemMenuCache.DISMISS_WITH_NO_SELECTION_ID: {
                 return;
             }
 
             case ItemMenuCache.DONE_ACTION_ID: {
                 mMainActivityState.services().maybePlayApplauseSoundClip(AudioManager.FX_KEY_CLICK,
                         false);
                 final ItemModel item = mMainActivityState.model().getItemForMutation(pageKind,
                         itemIndex);
                 item.setIsCompleted(true);
                 // NOTE(tal): we assume that the color flag is not needed once an item is completed.
                 // This is a usability heuristic. Not required otherwise.
                 item.setColor(ItemColor.NONE);
                 mMainActivityState.view().updatePage(pageKind);
                 maybeAutosortPageWithItemOfInterest(pageKind, itemIndex);
                 return;
             }
 
             case ItemMenuCache.TODO_ACTION_ID: {
                 mMainActivityState.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
                 final ItemModel item = mMainActivityState.model().getItemForMutation(pageKind,
                         itemIndex);
                 item.setIsCompleted(false);
                 // mApp.view().updateSingleItemView(pageKind, itemIndex);
                 mMainActivityState.view().updatePage(pageKind);
                 maybeAutosortPageWithItemOfInterest(pageKind, itemIndex);
                 return;
             }
 
             // Edit
             case ItemMenuCache.EDIT_ACTION_ID: {
                 mMainActivityState.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
                 final ItemModel item = mMainActivityState.model().getItemForMutation(pageKind,
                         itemIndex);
                 ItemTextEditor.startEditor(mMainActivityState,
                         mMainActivityState.str(R.string.editor_title_Edit_Task), item.getText(),
                         item.getColor(), new ItemTextEditor.ItemEditorListener() {
                             @Override
                             public void onDismiss(String finalString, ItemColor finalColor) {
                                 // NOTE: at this point, finalString is also cleaned of leading or
                                 // trailing
                                 if (finalString.length() == 0) {
                                     startItemDeletionWithAnination(pageKind, itemIndex);
                                     if (mMainActivityState.prefTracker()
                                             .getVerboseMessagesEnabledPreference()) {
                                         mMainActivityState.services().toast(
                                                 R.string.editor_Empty_task_deleted);
                                     }
                                 } else {
                                     item.setText(finalString);
                                     item.setColor(finalColor);
                                     mMainActivityState.model().setDirty();
                                     mMainActivityState.view().updatePage(pageKind);
                                     // Highlight the modified item for a short time, to provide
                                     // the user with an indication of the modified item.
                                     briefItemHighlight(pageKind, itemIndex, 700);
                                 }
                             }
                         });
                 return;
             }
 
             case ItemMenuCache.LOCK_ACTION_ID:
             case ItemMenuCache.UNLOCK_ACTION_ID: {
                mMainActivityState.services().maybePlayStockSound(
                        AudioManager.FX_KEYPRESS_STANDARD, false);
 
                 final ItemModel item = mMainActivityState.model().getItemForMutation(pageKind,
                         itemIndex);
                 item.setIsLocked(actionId == ItemMenuCache.LOCK_ACTION_ID);
                 // mApp.view().updateSingleItemView(pageKind, itemIndex);
                 mMainActivityState.view().updatePage(pageKind);
                 // If lock and in Today page, we also move it to the Tomorrow page, with an
                 // animation.
                 if (pageKind == PageKind.TODAY && actionId == ItemMenuCache.LOCK_ACTION_ID) {
                     // We do a short delay before the animation to let the use see the icon change
                     // to lock before the item is moved to the other page.
                     mMainActivityState.view().startItemAnimation(pageKind, itemIndex,
                             AppView.ItemAnimationType.MOVING_ITEM_TO_OTHER_PAGE, 200,
                             new Runnable() {
                                 @Override
                                 public void run() {
                                     moveItemToOtherPage(pageKind, itemIndex);
                                 }
                             });
                 } else {
                     maybeAutosortPageWithItemOfInterest(pageKind, itemIndex);
                 }
                 return;
             }
 
             case ItemMenuCache.DELETE_ACTION_ID: {
                 mMainActivityState.services().maybePlayStockSound(AudioManager.FX_KEYPRESS_DELETE,
                         false);
                 startItemDeletionWithAnination(pageKind, itemIndex);
                 return;
             }
         }
 
         throw new RuntimeException("Unknown menu action: " + actionId);
     }
 
     /**
      * Start item deletion from the current page. The item is deleted after a short animation and
      * the page view is then updated.
      */
     private final void startItemDeletionWithAnination(final PageKind pageKind, final int itemIndex) {
         mMainActivityState.view().startItemAnimation(pageKind, itemIndex,
                 AppView.ItemAnimationType.DELETING_ITEM, 0, new Runnable() {
                     @Override
                     public void run() {
                         // This runs at the end of the animation.
                         mMainActivityState.model().removeItemWithUndo(pageKind, itemIndex);
                         mMainActivityState.view().updatePage(pageKind);
                     }
                 });
     }
 
     /** Highlight the given item for a brief time. The item is assumed to already be visible. */
     private final void briefItemHighlight(final PageKind pageKind, final int itemIndex, int millis) {
         mMainActivityState.view().setItemViewHighlight(pageKind, itemIndex, true);
         mMainActivityState.view().getRootView().postDelayed(new Runnable() {
             @Override
             public void run() {
                 mMainActivityState.view().setItemViewHighlight(pageKind, itemIndex, false);
             }
         }, millis);
     }
 
     /** Called by the app view when the user clicks on the Undo button. */
     public final void onUndoButton(PageKind pageKind) {
         mMainActivityState.services().maybePlayStockSound(AudioManager.FX_KEYPRESS_RETURN, false);
         final int itemRestored = mMainActivityState.model().applyUndo(pageKind);
         maybeAutoSortPage(pageKind, false, false);
         mMainActivityState.view().updatePage(pageKind);
         if (itemRestored == 1) {
             mMainActivityState.services().toast(R.string.undo_Restored_one_deleted_task);
         } else {
             mMainActivityState.services().toast(R.string.undo_Restored_d_deleted_tasks,
                     itemRestored);
         }
     }
 
     /** Called by the app view when the user clicks on the Add Item button. */
     public final void onAddItemByTextButton(final PageKind pageKind) {
         clearPageUndo(pageKind);
         mMainActivityState.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
         ItemTextEditor.startEditor(mMainActivityState, mMainActivityState
                 .str(R.string.editor_title_New_Task), "", mMainActivityState.prefTracker()
                 .getDefaultItemColorPreference(), new ItemTextEditor.ItemEditorListener() {
             @Override
             public void onDismiss(String finalString, ItemColor finalColor) {
                 maybeAddNewItem(finalString, finalColor, pageKind, true);
             }
         });
     }
 
     public final void onAddItemByVoiceButton(final PageKind pageKind) {
         mMainActivityState.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
         mInSubActivity = true;
         ItemVoiceEditor.startVoiceEditor(mMainActivityState.mainActivity(),
                 VOICE_RECOGNITION_REQUEST_CODE);
     }
 
     /** Add a new task from text editor or voice recognition. */
     private final void maybeAddNewItem(final String text, ItemColor color, final PageKind pageKind,
             boolean upperCaseIt) {
         String cleanedValue = text.trim();
         if (cleanedValue.length() == 0) {
             return;
         }
 
         // Look for special string to enable debug mode.
         if (cleanedValue.equals(DEBUG_MODE_TASK_CODE)) {
             mMainActivityState.debugController().setDebugMode(true);
             // Do not add the item.
             return;
         }
 
         if (upperCaseIt) {
             cleanedValue = cleanedValue.substring(0, 1).toUpperCase() + cleanedValue.substring(1);
         }
 
         ItemModel item = new ItemModel(System.currentTimeMillis(), IdGenerator.getFreshId(),
                 cleanedValue, false, false, color);
 
         final int insertionIndex = newItemInsertionIndex(pageKind);
         mMainActivityState.model().insertItem(pageKind, insertionIndex, item);
         mMainActivityState.view().updatePage(pageKind);
         mMainActivityState.view().scrollToItem(pageKind, insertionIndex);
 
         // We perform the highlight only after the view has been
         // stabilized from the scroll (since item views are reused during the scroll).
         mMainActivityState.view().getRootView().post(new Runnable() {
             @Override
             public void run() {
                 briefItemHighlight(pageKind, insertionIndex, 700);
             }
         });
     }
 
     // Return the insertion index of a new item. The new item is assumed to be
     // non locked and non completed. The returned index complies with add-to-top
     // and auto-sort preference settings.
     private final int newItemInsertionIndex(PageKind pageKind) {
         // If adding at top, always adding at index 0.
         if (mMainActivityState.prefTracker().getAddToTopPreference()) {
             return 0;
         }
 
         // If adding at bottom and no sorting then adding at n.
         final int n = mMainActivityState.model().getPageItemCount(pageKind);
         if (!mMainActivityState.prefTracker().getAutoSortPreference()) {
             return n;
         }
 
         // Here when adding at bottom and using auto sort. Scan from the end and skip
         // all completed and locked items.
         int i;
         for (i = n - 1; i >= 0; i--) {
             final ItemModelReadOnly item = mMainActivityState.model().getItemReadOnly(pageKind, i);
             if (!item.isCompleted() && !item.isLocked()) {
                 break;
             }
         }
         return i + 1;
     }
 
     /** Handle the case where the app is responding to a restore from file action. */
     private final void onRestoreBackupFromFileClick(Intent resumeIntent) {
         // NOTE: main activity already qualified this to have the expected content type.
         final AppModel newModel;
         try {
             final Uri uri = resumeIntent.getData();
             final InputStream in = mMainActivityState.context().getContentResolver()
                     .openInputStream(uri);
             FileReadResult readResult = FileUtil.readFileToString(in, uri.toString());
             if (!readResult.outcome.isOk()) {
                 mMainActivityState.services().toast(
                         R.string.backup_restore_Failed_to_read_backup_file);
                 return;
             }
             // TODO: test that the file size is reasonable
             // TODO: test that the file looks like maniana file
             PersistenceMetadata resultMetadata = new PersistenceMetadata();
             newModel = new AppModel();
             ModelDeserialization.deserializeModel(newModel, resultMetadata, readResult.content);
         } catch (Throwable e) {
             LogUtil.error(e, "Error while trying to restore data");
             mMainActivityState.services().toast(
                     R.string.backup_restore_Error_loading_the_backup_file);
             return;
         }
 
         final RestoreBackupDialogListener listener = new RestoreBackupDialogListener() {
             @Override
             public void onSelection(Action action) {
                 onRestoreBackupFromFileConfirm(action, newModel);
             }
         };
 
         RestoreBackupDialog.startDialog(mMainActivityState, listener, mMainActivityState.model()
                 .projectedImportStats(newModel));
     }
 
     private final void onRestoreBackupFromFileConfirm(Action action, AppModel newModel) {
         switch (action) {
             case REPLACE:
                 mMainActivityState.model().restoreBackup(newModel);
                 mMainActivityState.services().toast(R.string.backup_restore_Task_list_replaced);
                 break;
             case MERGE:
                 mMainActivityState.model().mergeFrom(newModel);
                 mMainActivityState.services().toast(R.string.backup_restore_Task_list_merged);
                 break;
             case CANCEL:
             default:
                 mMainActivityState.services().toast(R.string.backup_restore_Task_list_not_changed);
                 // Do nothing
                 return;
         }
 
         maybeAutoSortPages(false, false);
         mMainActivityState.view().updatePages();
     }
 
     public final void onActivityResult(int requestCode, int resultCode, Intent intent) {
         switch (requestCode) {
             case VOICE_RECOGNITION_REQUEST_CODE: {
                 onVoiceActivityResult(resultCode, intent);
                 break;
             }
             default:
                 LogUtil.warning("Unknown onActivityResult requestCode: %s", requestCode);
         }
     }
 
     /** Handle the result of add-new-item by voice recongition. */
     private final void onVoiceActivityResult(int resultCode, Intent intent) {
         // Prevents the main activity from scrolling to top of pages as we do when resuming from
         // an external activity.
         mInSubActivity = true;
 
         if (resultCode != Activity.RESULT_OK) {
             return;
         }
 
         ItemVoiceEditor.startSelectionDialog(mMainActivityState.context(), intent,
                 new OnItemClickListener() {
                     @Override
                     public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                         final TextView itemTextView = (TextView) arg1;
                         maybeAddNewItem(itemTextView.getText().toString(), mMainActivityState
                                 .prefTracker().getDefaultItemColorPreference(), mMainActivityState
                                 .view().getCurrentPageKind(), true);
                     }
                 });
     }
 
     /** Called by the app view when the user click or long press the clean page button. */
     public final void onCleanPageButton(final PageKind pageKind, boolean isLongPress) {
         final boolean deleteCompletedItems = isLongPress;
 
         // NOTE: reusing mTempSummary.
         mMainActivityState.model().organizePageWithUndo(pageKind, deleteCompletedItems, -1,
                 mTempSummary);
 
         mMainActivityState.services().maybePlayStockSound(
                 (mTempSummary.completedItemsDeleted > 0) ? AudioManager.FX_KEYPRESS_DELETE
                         : AudioManager.FX_KEY_CLICK, false);
 
         mMainActivityState.view().updatePage(pageKind);
 
         // Display optional message to the user
         @Nullable
         final String message = constructPageCleanMessage(mTempSummary);
         if (message != null) {
             mMainActivityState.services().toast(message);
         }
     }
 
     /** Called from shake detector. */
     public final void onShake() {
         // If we have open dialogs ignore this shake event.
         if (mMainActivityState.popupsTracker().count() > 0) {
             LogUtil.info("Shake ignored (dialog opened)");
             return;
         }
 
         // Handle the shake event
         final PageKind currentPage = mMainActivityState.view().getCurrentPageKind();
         final ShakerAction action = mMainActivityState.prefTracker().reader()
                 .getShakerActionPreference();
         switch (action) {
             case NEW_ITEM_BY_TEXT:
                 onAddItemByTextButton(currentPage);
                 break;
             case NEW_ITEM_BY_VOICE:
                 onAddItemByVoiceButton(currentPage);
                 break;
             case CLEAN:
                 onCleanPageButton(currentPage, true);
                 break;
             case QUIT:
                 // if (!mApp.pref().getVerboseMessagesEnabledPreference()) {
                 // mApp.services().toast("Shake action: quit");
                 // }
                 mMainActivityState.mainActivity().finish();
                 break;
             default:
                 mMainActivityState.services().toast("Unknown action: " + action);
         }
     }
 
     /**
      * Compose the message to show to the user after a page cleanup operation.
      * 
      * @param summary the page cleanup summary.
      * @return the message or null if no message should me shown.
      */
     @Nullable
     private final String constructPageCleanMessage(OrganizePageSummary summary) {
         if (!mMainActivityState.prefTracker().getVerboseMessagesEnabledPreference()) {
             return null;
         }
 
         // Deleted at least one completed tasks
         if (summary.completedItemsDeleted > 0) {
             if (summary.completedItemsDeleted == 1) {
                 return mMainActivityState.str(R.string.organize_outcome_Deleted_one_completed_task);
             }
             return mMainActivityState.str(R.string.organize_outcome_Deleted_d_completed_tasks,
                     summary.completedItemsDeleted);
         }
 
         // Here when completed tasks not deleted
         if (summary.orderChanged) {
             // Here when not deleted but reordered
             if (summary.completedItemsFound == 0) {
                 return mMainActivityState.str(R.string.organize_outcome_Tasks_reordered);
             }
             if (summary.completedItemsFound == 1) {
                 return mMainActivityState
                         .str(R.string.organize_outcome_Tasks_reordered_Long_press_to_delete_one_completed_task);
             }
             return mMainActivityState
                     .str(R.string.organize_outcome_Tasks_reordered_Long_press_to_delete_d_completed_tasks,
                             summary.completedItemsFound);
         }
 
         // Here when not deleted and not reordred.
         if (summary.completedItemsFound > 0) {
             // Here if found completed items.
             if (summary.completedItemsFound == 1) {
                 return mMainActivityState
                         .str(R.string.organize_outcome_Page_already_organized_Long_press_to_delete_one_completed_task);
             }
             return mMainActivityState
                     .str(R.string.organize_outcome_Page_already_organized_Long_press_to_delete_d_completed_tasks,
                             summary.completedItemsFound);
         }
 
         return mMainActivityState.str(R.string.organize_outcome_Page_already_organized);
     }
 
     /** Called to show the main menu. */
     public final boolean onMenuButton() {
         mMainActivityState.view().showMainMenu();
         return true;
     }
 
     /** Called to launch the calendar */
     public final void onCalendarLaunchClick() {
         if (mMainActivityState.prefReader().getCalendarLaunchPreference()) {
             mMainActivityState.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
             // TODO: should we use startSubActivity() here?
             if (!mMainActivityState.services().startActivity(
                     CalendarUtil.constructGoogleCalendarIntent())) {
                 // NOTE: the protocol to launch the google calendar depend on the OS version.
                 // We include it in the error message to help with diagnostic. If needed
                 // more information, add a debug menu command to diagnose the calendar
                 // launching.
                 mMainActivityState.services().toast("Failed launching Google calendar (%d).",
                         android.os.Build.VERSION.SDK_INT);
             }
         }
     }
 
     /** Called by the framework when the user makes a main menu selection. */
     public final void onMainMenuSelection(MainMenuEntry entry) {
         switch (entry) {
             case HELP:
                 final Intent helpIntent = HelpUtil.helpPageIntent(mMainActivityState.context(),
                         false);
                 mMainActivityState.services().startActivity(helpIntent);
                 break;
             case SETTINGS:
                 startSubActivity(SettingsActivity.class);
                 break;
             case ABOUT:
                 startPopupMessageSubActivity(MessageKind.ABOUT);
                 break;
             case DEBUG:
                 mMainActivityState.debugController().startMainDialog();
                 break;
             default:
                 throw new RuntimeException("Unknown main menu action id: " + entry);
         }
     }
 
     /** Handle back button event or return false if not used. */
     public final boolean onBackButton() {
         // If the current page is not today, we still the back key event and switch back to the
         // today page. Otherwise we use the default back behavior.
         final PageKind currentPage = mMainActivityState.view().getCurrentPageKind();
         if (currentPage != PageKind.TODAY) {
             mMainActivityState.view().setCurrentPage(PageKind.TODAY, -1);
             return true;
         }
         return false;
     }
 
     /**
      * Called by the app preferences client when app preferences changed.
      * 
      * @param id the id of the preference item that was changed.
      */
     public final void onPreferenceChange(PreferenceKind id) {
         onBackupDataChange();
 
         switch (id) {
             case PAGE_ICON_SET:
                 mMainActivityState.view().onPageIconSetPreferenceChange();
                 break;
 
             case PAGE_ITEM_FONT:
             case PAGE_ITEM_FONT_SIZE:
             case PAGE_ITEM_ACTIVE_TEXT_COLOR:
             case PAGE_ITEM_COMPLETED_TEXT_COLOR:
                 mMainActivityState.view().onPageItemFontVariationPreferenceChange();
                 break;
 
             case PAGE_BACKGROUND_PAPER:
             case PAGE_PAPER_COLOR:
             case PAGE_BACKGROUND_SOLID_COLOR:
                 mMainActivityState.view().onPageBackgroundPreferenceChange();
                 break;
 
             case PAGE_TITLE_FONT:
             case PAGE_TITLE_FONT_SIZE:
             case PAGE_TITLE_TODAY_COLOR:
             case PAGE_TITLE_TOMORROW_COLOR:
                 mMainActivityState.view().onPageTitlePreferenceChange();
                 break;
 
             case PAGE_ITEM_DIVIDER_COLOR:
                 mMainActivityState.view().onItemDividerColorPreferenceChange();
                 break;
 
             case AUTO_SORT:
                 maybeAutoSortPages(true, true);
                 // If auto sort got enabled, this may affect the list widgets and thus
                 // we force widget updated. In the other direction it's not required.
                 flushModelChanges(mMainActivityState.prefTracker().getAutoSortPreference());
                 break;
 
             case ADD_TO_TOP:
             case DEFAULT_ITEM_COLOR:
             case SOUND_ENABLED:
             case APPLAUSE_LEVEL:
             case DAILY_NOTIFICATION:
 
             case NOTIFICATION_LED:
             case LOCK_PERIOD:
             case VERBOSE_MESSAGES:
             case STARTUP_ANIMATION:
                 // Nothing to do here. We query these preferences on the fly.
                 break;
 
             case AUTO_DAILY_CLEANUP:
                 // This setting may affect the widget on next update but by itself, its
                 // change event does require widget update (?).
                 break;
 
             case SHAKER_ENABLED:
             case SHAKER_ACTION:
             case SHAKER_SENSITIVITY:
                 // Nothing to do here. The controller will update the shaker next time
                 // it will be resumed. Since setting is done in a separate activity,
                 // the controller is paused when setting is changed.
                 break;
 
             // NOTE: calendar launch change triggers widget update in case the widget
             // date display is enabled.
             case CALENDAR_LAUNCH:
             case WIDGET_BACKGROUND_PAPER:
             case WIDGET_PAPER_COLOR:
             case WIDGET_BACKGROUND_COLOR:
             case WIDGET_ITEM_FONT:
             case WIDGET_ITEM_TEXT_COLOR:
             case WIDGET_ITEM_FONT_SIZE:
             case WIDGET_AUTO_FIT:
             case WIDGET_SHOW_COMPLETED_ITEMS:
             case WIDGET_ITEM_COMPLETED_TEXT_COLOR:
             case WIDGET_SHOW_TOOLBAR:
             case WIDGET_SHOW_DATE:
             case WIDGET_SINGLE_LINE:
                 // NOTE: This covers the case where the user changes widget settings and presses the
                 // Home button immediately, going back to the widgets. The widget update at
                 // onAppPause() is not triggered in this case because the main activity is already
                 // paused.
                 flushModelChanges(true);
                 break;
 
             case BACKUP_EMAIL:
                 // Nothing to do here.
                 break;
 
             case DEBUG_MODE:
                 // Nothing to do here.
                 break;
 
             default:
                 throw new RuntimeException("Unknown preference: " + id);
         }
     }
 
     /** Inform backup manager about change in persisted model data of app settings */
     private final void onBackupDataChange() {
         LogUtil.info("Backup data changed");
         mMainActivityState.services().backupManager().dataChanged();
     }
 
     /** Force a widget update with the current */
     private final void updateAllWidgets() {
         BaseWidgetProvider.updateAllWidgetsFromModel(mMainActivityState.context(),
                 mMainActivityState.model(), mMainActivityState.dateTracker().sometimeToday());
     }
 
     /** Called by the main activity when it is created. */
     public final void onMainActivityCreated(MainActivityStartupKind startupKind) {
         // NOTE: at this point the model has not been processed yet for potential
         // task move/cleanup due to date change. This is done later in the
         // onMainActivityResume() event.
 
         mPopulateNewUserSampleDataOnResume = false;
 
         switch (startupKind) {
             case NORMAL:
             case NEW_VERSION_SILENT:
                 // Model is assume to be clean here.
                 break;
             case NEW_USER:
                 // At this point we don't want to perist the model, we will do it
                 // in the resume method after it will be populated with sample data.
                 mMainActivityState.model().setClean();
                 mPopulateNewUserSampleDataOnResume = true;
                 break;
             case NEW_VERSION_ANNOUNCE:
                 // Mark the model for writing to avoid this what's up splash the next time.
                 mMainActivityState.model().setDirty();
                 startPopupMessageSubActivity(MessageKind.WHATS_NEW);
                 break;
             case MODEL_DATA_ERROR:
                 mMainActivityState.model().clear();
                 mMainActivityState.services().toast(
                         mMainActivityState.str(R.string.launch_error_Error_loading_task_list)
                                 + " (" + startupKind.toString().toLowerCase() + ")");
             default:
                 LogUtil.error("Unknown startup message type: ", startupKind);
         }
     }
 
     private final void startPopupMessageSubActivity(MessageKind messageKind) {
         startSubActivity(PopupMessageActivity.intentFor(mMainActivityState.context(), messageKind));
     }
 
     private final void startSubActivity(Class<? extends Activity> cls) {
         final Intent intent = new Intent(mMainActivityState.context(), cls);
         startSubActivity(intent);
     }
 
     private final void startSubActivity(Intent intent) {
         // TODO: should we assert or print an error message that mInSubActivity is false here?
         mInSubActivity = true;
         mMainActivityState.services().startActivity(intent);
     }
 
     /** Called by the main activity when it is destroyed. */
     public final void onMainActivityDestroy() {
     }
 
     /** Clear undo buffer of given model page. */
     private final void clearPageUndo(PageKind pageKind) {
         mMainActivityState.model().clearPageUndo(pageKind);
         mMainActivityState.view().updateUndoButton(pageKind);
     }
 
     private final boolean maybeAutoSortPage(PageKind pageKind, boolean updateViewIfSorted,
             boolean showMessageIfSorted) {
         if (mMainActivityState.prefTracker().getAutoSortPreference()) {
             // NOTE: reusing temp summary mmeber.
             mMainActivityState.model().organizePageWithUndo(pageKind, false, -1, mTempSummary);
             if (mTempSummary.orderChanged) {
                 if (updateViewIfSorted) {
                     mMainActivityState.view().updatePage(pageKind);
                 }
                 if (showMessageIfSorted
                         && mMainActivityState.prefTracker().getVerboseMessagesEnabledPreference()) {
                     mMainActivityState.services().toast(R.string.Auto_sorted);
                 }
                 return true;
             }
         }
         return false;
     }
 
     private final boolean maybeAutoSortPages(boolean updateViewIfSorted, boolean showMessageIfSorted) {
         // NOTE: avoiding '||' operator short circuit to make sure both pages are sorted.
         final boolean sorted1 = maybeAutoSortPage(PageKind.TODAY, updateViewIfSorted, false);
         final boolean sorted2 = maybeAutoSortPage(PageKind.TOMOROW, updateViewIfSorted, false);
         final boolean sorted = sorted1 || sorted2;
         // NOTE: suppressing message if showing a sub activity (e.g. SettingActivity).
         if (sorted && showMessageIfSorted
                 && mMainActivityState.prefTracker().getVerboseMessagesEnabledPreference()
                 && !mInSubActivity) {
             mMainActivityState.services().toast(R.string.Auto_sorted);
         }
         return sorted;
     }
 
     /**
      * @param pageKind the page
      * @param itemOfInteresttOriginalIndex if >= 0, the pre sort index of the item to highlight post
      *        sort.
      */
     private final void maybeAutosortPageWithItemOfInterest(final PageKind pageKind,
             final int itemOfInteresttOriginalIndex) {
         if (!mMainActivityState.prefTracker().getAutoSortPreference()
                 || mMainActivityState.model().isPageSorted(pageKind)) {
             return;
         }
 
         mMainActivityState.view().startItemAnimation(pageKind, itemOfInteresttOriginalIndex,
                 ItemAnimationType.SORTING_ITEM, 0, new Runnable() {
                     @Override
                     public void run() {
                         // NOTE: reusing temp summary member
                         mMainActivityState.model().organizePageWithUndo(pageKind, false,
                                 itemOfInteresttOriginalIndex, mTempSummary);
                         mMainActivityState.view().updatePage(pageKind);
                         if (mMainActivityState.prefTracker().getVerboseMessagesEnabledPreference()) {
                             mMainActivityState.services().toast(R.string.Auto_sorted);
                         }
                         if (mTempSummary.itemOfInterestNewIndex >= 0) {
                             // After the animation, briefly highlight the item at the new
                             // location.
                             mMainActivityState.view().getRootView().post(new Runnable() {
                                 @Override
                                 public void run() {
                                     briefItemHighlight(pageKind,
                                             mTempSummary.itemOfInterestNewIndex, 300);
                                 }
                             });
 
                         }
                     }
                 });
     }
 }
