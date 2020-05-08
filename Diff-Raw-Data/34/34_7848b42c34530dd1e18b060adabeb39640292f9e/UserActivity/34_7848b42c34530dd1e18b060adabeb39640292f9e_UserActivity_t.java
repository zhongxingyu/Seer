 /**
  * 
  */
 package org.vimeoid.activity.user.item;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import org.vimeoid.R;
 import org.vimeoid.activity.base.ApiPagesReceiver;
 import org.vimeoid.activity.user.QuickApiTask;
 import org.vimeoid.activity.user.SingleItemActivity;
 import org.vimeoid.adapter.LActionItem;
 import org.vimeoid.adapter.SectionedActionsAdapter;
 import org.vimeoid.connection.advanced.Methods;
 import org.vimeoid.dto.advanced.Album;
 import org.vimeoid.dto.advanced.Channel;
 import org.vimeoid.dto.advanced.Contact;
 import org.vimeoid.dto.advanced.PagingData;
 import org.vimeoid.dto.advanced.PortraitsData;
 import org.vimeoid.dto.advanced.SubscriptionData;
 import org.vimeoid.dto.advanced.User;
 import org.vimeoid.dto.advanced.SubscriptionData.SubscriptionType;
 import org.vimeoid.util.ApiParams;
 import org.vimeoid.util.Dialogs;
 import org.vimeoid.util.Invoke;
 import org.vimeoid.util.PagingData_;
 import org.vimeoid.util.Utils;
 
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.activity.user.item</dd>
  * </dl>
  *
  * <code>UserActivity</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Sep 24, 2010 11:05:50 PM 
  *
  */
 public class UserActivity extends SingleItemActivity<User> {
     
     public static final String TAG = "UserActivity";
     
     private static final int LOAD_PORTRAITS_TASK = 1;
     private static final int LOAD_ALBUMS_TASK = 2;
     private static final int LOAD_CHANNELS_TASK = 3;
     
     private static final int LOAD_SUBSCRIPTIONS_TASK = 4;
     private static final int LOAD_CONTACTS_TASK = 5;
     
     private LActionItem albumAction;
     private LActionItem channelAction;
     private LActionItem subscribeLikesAction;
     private LActionItem subscribeUploadsAction;
     private LActionItem subscribeAppearsAction;
     private LActionItem addContactAction;
     private LActionItem isMutualAction;
     
     private Set<SubscriptionType> subscriptionsStatus; // Likes / Uploads / Appears
     private Boolean isContact;
     private Boolean isMutual;
     
     private final ApiPagesReceiver<JSONObject> subscriptionsReceiver;
     private final ApiPagesReceiver<JSONObject> contactsReceiver;
     
     public UserActivity() {
         super(R.layout.view_single_user);
         
         subscriptionsReceiver = new SubscriptionsReceiver() {
             @Override public void onComplete() {
                 final long subjectUserId = getSubjectUserId();
                 
                 subscriptionsStatus = new HashSet<SubscriptionType>();
                 if (subscriptions.data.get(SubscriptionType.LIKES).contains(subjectUserId)) { 
                     subscriptionsStatus.add(SubscriptionType.LIKES);
                 }
                 if (subscriptions.data.get(SubscriptionType.UPLOADS).contains(subjectUserId)) { 
                     subscriptionsStatus.add(SubscriptionType.UPLOADS);
                 }
                 if (subscriptions.data.get(SubscriptionType.APPEARS).contains(subjectUserId)) { 
                     subscriptionsStatus.add(SubscriptionType.APPEARS);
                 }
                 initSubscriptionAction(subscribeLikesAction, SubscriptionType.LIKES);
                 initSubscriptionAction(subscribeUploadsAction, SubscriptionType.UPLOADS);
                 initSubscriptionAction(subscribeAppearsAction, SubscriptionType.APPEARS);
             }
         };
         
         contactsReceiver = new ContactsReceiver() {
             @Override public void onComplete() {
                 UserActivity.this.isContact = this.isContact;
                 UserActivity.this.isMutual = this.isMutual;
                 initAddContactAction(addContactAction);
                 initIsMutualAction(isMutualAction);
             }
         };
     }
 
     @Override
     protected void prepare(Bundle extras) {
         super.prepare(extras);        
         
         final long subjectUserId = getSubjectUserId();
         
         secondaryTasks.add(LOAD_PORTRAITS_TASK, Methods.people.getPortraitUrls, new ApiParams().add("user_id", String.valueOf(subjectUserId)));
         secondaryTasks.add(LOAD_ALBUMS_TASK, Methods.albums.getAll, new ApiParams().add("user_id", String.valueOf(subjectUserId)).add("per_page", "1"));
         secondaryTasks.add(LOAD_CHANNELS_TASK, Methods.channels.getAll, new ApiParams().add("user_id", String.valueOf(subjectUserId)).add("per_page", "1"));
         
         if (getCurrentUserId() != subjectUserId) {
             
             if (extras.containsKey(Invoke.Extras.SUBSCRIPTIONS_STATUS) && 
                 (extras.get(Invoke.Extras.SUBSCRIPTIONS_STATUS) != null)) {
                 String[] types = extras.getStringArray(Invoke.Extras.SUBSCRIPTIONS_STATUS);
                 if (types != null) subscriptionsStatus = SubscriptionType.fromArray(types);
             } else {
                 secondaryTasks.addListTask(LOAD_SUBSCRIPTIONS_TASK, Methods.people.getSubscriptions, 
                                                                     new ApiParams().add("types", 
                                                     SubscriptionType.list(new SubscriptionType[] { SubscriptionType.LIKES, 
                                                                                                    SubscriptionType.UPLOADS, 
                                                                                                    SubscriptionType.APPEARS })), 
                                            subscriptionsReceiver, -1, 50);
                 // TODO: secondaryTasks.add(taskId, apiMethod, params); (infinite task)
             }
             
             
             if (extras.containsKey(Invoke.Extras.IS_MUTUAL) && 
                 (extras.get(Invoke.Extras.IS_MUTUAL) != null)) {
                 isMutual = (Boolean)extras.getBoolean(Invoke.Extras.IS_MUTUAL);
             }            
             
             if (extras.containsKey(Invoke.Extras.IS_CONTACT) && 
                 (extras.get(Invoke.Extras.IS_CONTACT) != null)) {
                 isContact = (Boolean)extras.get(Invoke.Extras.IS_CONTACT);
             } else {
                 // TODO: secondaryTasks.add(taskId, apiMethod, params); (infinite task)
                 secondaryTasks.addListTask(LOAD_CONTACTS_TASK, Methods.contacts.getAll, 
                                                                new ApiParams().add("user_id", String.valueOf(subjectUserId)), 
                                            contactsReceiver, -1, 50);
             }
             
         }        
         
     }
 
     @Override
     protected User extractFromJson(JSONObject jsonObj) throws JSONException {
         return User.collectFromJson(jsonObj);
     }
 
     @Override
     protected SectionedActionsAdapter fillWithActions(SectionedActionsAdapter actionsAdapter, final User user) {
         
         final long subjectUserId = getSubjectUserId();
         final long currentUserId = getCurrentUserId();
 
     	// ========================= STATISTICS ================================
         int statsSection = actionsAdapter.addSection(getString(R.string.statistics));
         // number of videos
         final LActionItem videoAction = actionsAdapter.addAction(statsSection, R.drawable.video, 
                                  Utils.format(getString(R.string.num_of_videos), "num", String.valueOf(user.videosCount)));
         if (user.videosCount > 0) {
             videoAction.onClick =  new OnClickListener() {
                 @Override public void onClick(View v) { Invoke.User_.selectVideosBy(UserActivity.this, user); };
             };
         }
         // number of albums
         albumAction = actionsAdapter.addAction(statsSection, R.drawable.album, 
                       Utils.format(getString(R.string.num_of_albums), "num", "?"));
         albumAction.onClick =  new OnClickListener() {
             @Override public void onClick(View v) { 
                 Invoke.User_.selectAlbumsOf(UserActivity.this, user);
             };
         };
         // number of channels
         channelAction = actionsAdapter.addAction(statsSection, R.drawable.channel, 
                         Utils.format(getString(R.string.num_of_channels), "num", "?"));
         channelAction.onClick =  new OnClickListener() {
                 @Override public void onClick(View v) { Invoke.User_.selectChannelsOf(UserActivity.this, user); };
         };
         // number of contacts
         final LActionItem contactAction = actionsAdapter.addAction(statsSection, R.drawable.contact, 
                         Utils.format(getString(R.string.num_of_contacts), "num", String.valueOf(user.contactsCount)));
         if (user.contactsCount > 0) {
             contactAction.onClick =  new OnClickListener() {
                 @Override public void onClick(View v) { Invoke.User_.selectContactsOf(UserActivity.this, user); };
             };
         }
         // number of appearances
         final LActionItem appearanceAction = actionsAdapter.addAction(statsSection, R.drawable.appearance, 
                         Utils.format(getString(R.string.num_of_appearances), "num", String.valueOf(user.videosAppearsIn)));
         if (user.videosAppearsIn > 0) {
             appearanceAction.onClick =  new OnClickListener() {
                 @Override public void onClick(View v) { Invoke.User_.selectApperancesOf(UserActivity.this, user); };
             };
         }
         // number of likes
         final LActionItem likeAction = actionsAdapter.addAction(statsSection, R.drawable.like, 
                         Utils.format(getString(R.string.num_of_likes), "num", String.valueOf(user.videosLiked)));
         if (user.videosLiked > 0) { 
             likeAction.onClick =  new OnClickListener() {
                 @Override public void onClick(View v) { Invoke.User_.selectLikesOf(UserActivity.this, user); };
             };
         }
         // subscriptions
         final LActionItem subscriptionAction = actionsAdapter.addAction(statsSection, R.drawable.subscribe, 
                                                                                      getString(R.string.subscriptions));
         subscriptionAction.onClick =  new OnClickListener() {
             @Override public void onClick(View v) { Invoke.User_.selectSubsriptionsOf(UserActivity.this, user); };
         };        
         
         // ========================= INFORMATION ===============================
         int infoSection = actionsAdapter.addSection(getString(R.string.information));
         // location
         if (user.location.length() > 0) {
             actionsAdapter.addAction(infoSection, R.drawable.location,
                     Utils.format(getString(R.string.location_is), "place", user.location));                 
         }
         // created on
         actionsAdapter.addAction(infoSection, R.drawable.duration,
                                  Utils.format(getString(R.string.created_on), "time", user.createdOn));
         // from staff
         if (user.fromStaff) {
             actionsAdapter.addAction(infoSection, R.drawable.staff, getString(R.string.user_from_staff));    
         }
         // plus member
         if (user.isPlusMember) {
             actionsAdapter.addAction(infoSection, R.drawable.plus, getString(R.string.user_is_plus_member));    
         }        
         // TODO: add websites URLs
         // TODO: is online
         // TODO: is mutual, is added
         
         if (currentUserId != subjectUserId) {
         	
             // is mutual
             isMutualAction = actionsAdapter.addAction(infoSection, R.drawable.unknown_status, R.string.mutual);            
 
             if (isMutual != null) initIsMutualAction(isMutualAction);
         }
         
         // ========================= OPERATIONS ================================
          
         if (currentUserId != subjectUserId) {
         	
         	
             int operationsSection = actionsAdapter.addSection(getString(R.string.operations));
             
             // subscribe
             subscribeLikesAction = actionsAdapter.addAction(operationsSection, R.drawable.unknown_status, R.string.subscribe_likes);            
             subscribeUploadsAction = actionsAdapter.addAction(operationsSection, R.drawable.unknown_status, R.string.subscribe_uploads);
             subscribeAppearsAction = actionsAdapter.addAction(operationsSection, R.drawable.unknown_status, R.string.subscribe_appearences);
             
             if (subscriptionsStatus != null) {
                   
                 initSubscriptionAction(subscribeLikesAction, SubscriptionType.LIKES);
                 initSubscriptionAction(subscribeUploadsAction, SubscriptionType.UPLOADS);
                 initSubscriptionAction(subscribeAppearsAction, SubscriptionType.APPEARS);
                 
             } 
 
             // add contact            
             addContactAction = actionsAdapter.addAction(operationsSection, R.drawable.unknown_status, R.string.add_contact);            
 
             if (isContact != null) initAddContactAction(addContactAction);
             
         }
         
         // TODO: user activity (did / happened)
         // TODO: user groups
         // TODO: add search somewhere
         // TODO: mutual contacts
         
         return actionsAdapter;
     }
     
     @Override
     protected void onItemReceived(User user) {
         ((TextView)findViewById(R.id.userBio)).setText((user.biography.length() > 0) 
                                                        ? Html.fromHtml(user.biography)
                                                        : getString(R.string.no_biography_supplied));
         
         super.onItemReceived(user);
     }
     
     @Override
     public void onSecondaryTaskPerfomed(int taskId, JSONObject result) throws JSONException {
         switch (taskId) {
             case LOAD_PORTRAITS_TASK: {
                 final ImageView uploaderPortrait = (ImageView)findViewById(R.id.userPortrait);
                 final String mediumPortraitUrl = result.getJSONObject(PortraitsData.FieldsKeys.MULTIPLE_KEY)
                                                        .getJSONArray(PortraitsData.FieldsKeys.SINGLE_KEY)
                                                        .getJSONObject(2).getString(PortraitsData.FieldsKeys.URL);
                 imageLoader.displayImage(mediumPortraitUrl, uploaderPortrait);
             }; break;
             case LOAD_ALBUMS_TASK: {
                 final int albumsCount = result.getJSONObject(Album.FieldsKeys.MULTIPLE_KEY)
                                               .getInt(Album.FieldsKeys.TOTAL);                
                 Log.d(TAG, "got albums count, its " + albumsCount);
                 albumAction.title = Utils.format(getString(R.string.num_of_albums), "num", String.valueOf(albumsCount));
                 if (albumsCount == 0) albumAction.onClick = null;
                 getActionsList().invalidateViews();
             }; break;
             case LOAD_CHANNELS_TASK: {
                 final int channelsCount = result.getJSONObject(Channel.FieldsKeys.MULTIPLE_KEY)
                                                 .getInt(Channel.FieldsKeys.TOTAL);                
                 Log.d(TAG, "got channels count, its " + channelsCount);
                 channelAction.title = Utils.format(getString(R.string.num_of_channels), "num", String.valueOf(channelsCount));
                 if (channelsCount == 0) channelAction.onClick = null;
                 getActionsList().invalidateViews();
             }; break;            
         }
     }
     
     private LActionItem initSubscriptionAction(final LActionItem actionItem, final SubscriptionType type) {
         
         actionItem.icon = subscriptionsStatus.contains(type) ? R.drawable.subscribe : R.drawable.subscribe_not;        
 
         actionItem.onClick = new OnClickListener() {
             @Override public void onClick(View v) {
                 final long subjectUserId = getSubjectUserId();
                 
                 new QuickApiTask(UserActivity.this, subscriptionsStatus.contains(type) 
                                                     ? Methods.people.removeSubscription
                                                     : Methods.people.addSubscription) {
 
                     @Override
                     protected void onOk() {
                         if (subscriptionsStatus.contains(type))
                            { subscriptionsStatus.remove(type); }
                         else { subscriptionsStatus.add(type); };
                         
                         // TODO: add icon to toast
                         
                         Dialogs.makeToast(UserActivity.this, getString(
                                 subscriptionsStatus.contains(type) 
                                 ? R.string.subscribed
                                 : R.string.unsubscribed));
                         
                         actionItem.icon = subscriptionsStatus.contains(type) 
                                           ? R.drawable.subscribe
                                           : R.drawable.subscribe_not;
                         
                     }
 
                     @Override
                     protected int onError() { return R.string.failed_to_subscribe; }                            
                     
                 }.execute(new ApiParams().add("user_id", String.valueOf(subjectUserId)).add("types", type.toString()));
             }
         };
         
         //Utils.forceInvalidate(getActionsList(), actionItem.position);
         
         return actionItem;
         
     }
     
     private LActionItem initAddContactAction(final LActionItem actionItem) {
         
         actionItem.icon = isContact ? R.drawable.contact : R.drawable.contact_not;
         actionItem.title = getString(isContact ? R.string.remove_contact : R.string.add_contact);
 
         actionItem.onClick = new OnClickListener() {
             @Override public void onClick(View v) {
                 final long subjectUserId = getSubjectUserId();
                 
                 new QuickApiTask(UserActivity.this, isContact 
                                                     ? Methods.people.removeContact
                                                     : Methods.people.addContact) {
 
                    @Override protected void onOk() {
                         isContact = !isContact;
                         
                         // TODO: add icon to toast
                         
                         Dialogs.makeToast(UserActivity.this, getString(
                                 isContact ? R.string.added_contact
                                           : R.string.removed_contact));
                         
                         actionItem.icon =  
                                 isContact ? R.drawable.contact
                                           : R.drawable.contact_not;
                         actionItem.title = getString(
                                 isContact ? R.string.remove_contact 
                                           : R.string.add_contact);
                         
                        Log.d(TAG, "isContact: " + isContact +
                                   "result: " + (isContact ? R.string.added_contact
                                                           : R.string.removed_contact) +
                                   "action title: " + actionItem.title);
                        
                        Utils.forceInvalidate(getActionsList(), actionItem.position);
                     }
 
                     @Override
                    protected int onError() { return R.string.failed_to_perform; }                            
                     
                 }.execute(new ApiParams().add("user_id", String.valueOf(subjectUserId)));
             }
         };        
         //Utils.forceInvalidate(getActionsList(), actionItem.position);
         
         return actionItem;
         
     }
     
     private LActionItem initIsMutualAction(final LActionItem actionItem) {
         actionItem.icon = isMutual ? R.drawable.mutual : R.drawable.mutual_not; 
         actionItem.title = getString(isMutual ? R.string.mutual : R.string.not_mutual);
         //Utils.forceInvalidate(getActionsList(), actionItem.position);
         return actionItem;
     }      
     
     protected abstract static class SubscriptionsReceiver implements ApiPagesReceiver<JSONObject> {
         
         protected SubscriptionData subscriptions = new SubscriptionData();
         protected int received = 0;
 
         @Override
         public void addSource(JSONObject page) throws Exception {
             received += 
                 page.getJSONObject(Contact.FieldsKeys.MULTIPLE_KEY)
                     .getInt(PagingData.FieldsKeys.ON_THIS_PAGE);
             SubscriptionData.passTo(page, subscriptions);
         }
 
         @Override public int getCount() { return received; }
 
         @Override
         public PagingData_ getCurrentPagingData(JSONObject lastPage) throws JSONException {
             return PagingData.collectFromJson(lastPage, SubscriptionData.FieldsKeys.MULTIPLE_KEY);
         }
         
     }  
     
     protected abstract class ContactsReceiver implements ApiPagesReceiver<JSONObject> {
         
         protected boolean isContact = false;
         protected boolean isMutual = false;
         protected int received = 0;
 
         @Override
         public void addSource(JSONObject page) throws Exception {
             final long subjectUserId = getSubjectUserId();
             
             received += 
                 page.getJSONObject(Contact.FieldsKeys.MULTIPLE_KEY)
                     .getInt(PagingData.FieldsKeys.ON_THIS_PAGE);
             if (Contact.contactWithIdExist(page, subjectUserId)) {
                 isContact = true;
                 if (Contact.checkIsMutualById(page, subjectUserId)) isMutual = true;                
             }            
         }
 
         @Override public int getCount() { return received; }
 
         @Override
         public PagingData_ getCurrentPagingData(JSONObject lastPage) throws JSONException {
             return PagingData.collectFromJson(lastPage, Contact.FieldsKeys.MULTIPLE_KEY);
         }
         
     }     
 
 }
