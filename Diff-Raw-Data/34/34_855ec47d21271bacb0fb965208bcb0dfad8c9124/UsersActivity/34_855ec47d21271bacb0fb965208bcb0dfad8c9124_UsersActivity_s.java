 package org.vimeoid.activity.user.list;
 
 import net.londatiga.android.QuickAction;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.vimeoid.activity.user.ApiTask;
 import org.vimeoid.activity.user.ItemsListActivity;
 import org.vimeoid.adapter.JsonObjectsAdapter;
 import org.vimeoid.adapter.user.UsersDataProvider;
 import org.vimeoid.adapter.user.UsersListAdapter;
 import org.vimeoid.connection.advanced.Methods;
 import org.vimeoid.dto.advanced.Contact;
 import org.vimeoid.dto.advanced.User;
 import org.vimeoid.util.ApiParams;
 import org.vimeoid.util.Invoke;
 
 import android.view.View;
 
 /**
  * 
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid</dd>
  * </dl>
  *
  * <code>Videos</code>
  *
  * <p>Activity that shows a Vimeo Items list (Video, User, Channel, Album ...) to a user
  * that has logged in</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Sep 3, 2010 11:55:11 PM 
  *
  */
 
 public class UsersActivity extends ItemsListActivity<User> implements UsersDataProvider {
     
     @Override
     protected JsonObjectsAdapter<User> createAdapter() {
         // FIXME: separate activity for contacts
         return new UsersListAdapter(Contact.FieldsKeys.MULTIPLE_KEY, this, getLayoutInflater(), this) {
             @Override protected User[] extractItems(JSONObject jsonObject) throws JSONException {
                 return Contact.collectListFromJson(jsonObject);
             }            
         };
     }
     
     @Override
     protected void onItemSelected(User user) { 
         Invoke.User_.selectUser(this, user);
     }
     
     @Override
     protected QuickAction createQuickActions(final int position, final User user, View view) {
         // TODO: add ability to watch albums / channels where video located
         
         /* final Resources resources = getResources();
         
         QuickAction qa = new QuickAction(view);
         if (video.uploaderId != currentUserId) {
             qa.addActionItem(getString(R.string.qa_later), resources.getDrawable(video.isWatchLater 
                                                                                  ? R.drawable.watchlater_white 
                                                                                  : R.drawable.watchlater_white_not), 
                 new QActionClickListener() {            
                     @Override public void onClick(View v, final QActionItem item) {
                         switchWatchLaterStatus(position, video, item);
                     }
                 });
         }
         qa.addActionItem(getString(R.string.qa_info), getResources().getDrawable(R.drawable.info), 
                 new QActionClickListener() {            
                     @Override public void onClick(View v, QActionItem item) {
                         Invoke.User_.selectVideo(UsersActivity.this, video);
                     }
                 });
         qa.addActionItem(getString(R.string.qa_author), getResources().getDrawable(R.drawable.contact), 
                 new QActionClickListener() {            
                     @Override public void onClick(View v, QActionItem item) {
                         Invoke.User_.selectUploader(UsersActivity.this, video);
                     }
                 });
         qa.addActionItem(getString(R.string.qa_comments), getResources().getDrawable(R.drawable.comment), 
                 new QActionClickListener() {            
                     @Override public void onClick(View v, QActionItem item) {
                         Invoke.User_.selectCommentsOf(UsersActivity.this, video);
                     }
                 }); */
         return null;
     }
 
     @Override
    public void requestData(View view, final int position, final User user) {
         // people.getInfo : location, videos count, contacts count,
         // channels.getAll: channels count
         // albums.getAll: albums count
         // people.getSubscriptions: subscriptions status
        final long subjectUserId = getSubjectUserId();
        
         new ApiTask(Methods.people.getInfo) {
             @Override public void onAnswerReceived(JSONObject jsonObj) throws JSONException {
                 final JSONObject userObj = jsonObj.getJSONObject(User.FieldsKeys.SINGLE_KEY);
                 user.location = userObj.getString(User.FieldsKeys.LOCATION);
                 user.videosCount = userObj.getLong(User.FieldsKeys.NUM_OF_VIDEOS);
                user.contactsCount = userObj.getLong(User.FieldsKeys.NUM_OF_CONTACTS);                
                getListView().getChildAt(position).invalidate();
             }
        }.execute(new ApiParams().add("user_id", String.valueOf(subjectUserId))); 
     };
         
     /* private void switchWatchLaterStatus(final int position, final Video video, final QActionItem item) {
         final Resources resources = getResources();
         final VideosListAdapter adapter = ((VideosListAdapter)getAdapter());
         
         new QuickApiTask(UsersActivity.this, video.isWatchLater 
                 ? Methods.albums.removeFromWatchLater 
                 : Methods.albums.addToWatchLater) {
 
             @Override
             public void onOk() {
                 video.isWatchLater = !video.isWatchLater;
                 final Video changedVideo = adapter.switchWatchLater(getListView(), position);
                 Dialogs.makeToast(UsersActivity.this, getString(changedVideo.isWatchLater 
                                                    ? R.string.added_to_watchlater
                                                    : R.string.removed_from_watchlater));
                 item.setIcon(resources.getDrawable(changedVideo.isWatchLater 
                                      ? R.drawable.watchlater_white 
                                      : R.drawable.watchlater_white_not));
                 item.invalidate();
             }
 
             @Override
             protected int onError() { return R.string.failed_to_modify_watch_later; }
 
         }.execute(new ApiParams().add("video_id", String.valueOf(video.id)));
         
     } */    
        
 }
