 /**
  * 
  */
 package org.vimeoid.activity.guest;
 
 import org.vimeoid.R;
 import org.vimeoid.activity.base.SingleItemActivity_;
 import org.vimeoid.adapter.SectionedActionsAdapter;
 import org.vimeoid.connection.ApiCallInfo;
 import org.vimeoid.connection.simple.VimeoProvider;
 import org.vimeoid.util.Dialogs;
 import org.vimeoid.util.Invoke;
 import org.vimeoid.util.SimpleItem;
 import org.vimeoid.util.Utils;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.activity.guest</dd>
  * </dl>
  *
  * <code>SingleItemActivity</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Sep 16, 2010 6:41:44 PM 
  *
  */
 public abstract class SingleItemActivity<ItemType extends SimpleItem> extends SingleItemActivity_<ItemType> {
     
     // private static final String TAG = "SingleItemActivity";
     
     protected final String[] projection;
     
     protected Uri contentUri;
     protected ApiCallInfo callInfo;
     
     public SingleItemActivity(int mainView, String[] projection) {
         super(mainView);
         this.projection = projection;
     }
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         contentUri = getIntent().getData();      
         
         super.onCreate(savedInstanceState);
     }
     
     protected abstract SectionedActionsAdapter fillWithActions(final SectionedActionsAdapter actionsAdapter, final ItemType item);
     
     protected abstract ItemType extractFromCursor(Cursor cursor, int position);    
     
     protected void initTitleBar(ImageView subjectIcon, TextView subjectTitle, ImageView resultIcon) {
         callInfo = VimeoProvider.collectCallInfo(contentUri);        
         
         subjectIcon.setImageResource(Utils.drawableByContent(callInfo.subjectType));
         subjectTitle.setText(getIntent().hasExtra(Invoke.Extras.SUBJ_TITLE) ? getIntent().getStringExtra(Invoke.Extras.SUBJ_TITLE) : callInfo.subject);
         resultIcon.setImageResource(getIntent().getIntExtra(Invoke.Extras.RES_ICON, R.drawable.info));
     }
     
     @Override
     protected void queryItem() {
         new ApiTask(getContentResolver(), projection) {
 
             @Override protected void onPreExecute() {
                 super.onPreExecute();                
                showProgressBar();            
             }
             
             @Override protected void onAnswerReceived(Cursor cursor) {
             	if (cursor.getCount() > 1) throw new IllegalStateException("There must be the only one item returned");
                 onItemReceived(extractFromCursor(cursor, 0));
                 hideProgressBar();
             }
             
             @Override protected void onPostExecute(Cursor cursor) {
                 super.onPostExecute(cursor);
                 hideProgressBar();
             }
             
             @Override
             protected void onAnyError(Exception e, String message) {
                 super.onAnyError(e, message);
                 Dialogs.makeExceptionToast(SingleItemActivity.this, message, e);
                 hideProgressBar();                
             }
             
        };
     }
 
 }
