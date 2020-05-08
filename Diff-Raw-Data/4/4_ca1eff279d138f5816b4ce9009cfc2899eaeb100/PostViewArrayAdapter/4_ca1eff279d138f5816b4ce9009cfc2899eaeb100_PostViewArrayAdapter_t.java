 package com.normalexception.forum.rx8club.view.threadpost;
 
 /************************************************************************
  * NormalException.net Software, and other contributors
  * http://www.normalexception.net
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  ************************************************************************/
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.text.Html;
import android.text.util.Linkify;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.normalexception.forum.rx8club.Log;
 import com.normalexception.forum.rx8club.R;
 import com.normalexception.forum.rx8club.activities.pm.NewPrivateMessageActivity;
 import com.normalexception.forum.rx8club.activities.thread.EditPostActivity;
 import com.normalexception.forum.rx8club.handler.ForumImageHandler;
 
 /**
  * Custom adapter to handle PostView objects
  */
 public class PostViewArrayAdapter extends ArrayAdapter<PostView> {
 	private Context activity;
 	private List<PostView> data;
 	private final String TAG = "PostViewArrayAdapter";
 
 	/**
 	 * Custom adapter to handle PostView's
 	 * @param context				The source context
 	 * @param textViewResourceId	The resource id
 	 * @param objects				The list of objects
 	 */
 	public PostViewArrayAdapter(Context context, int textViewResourceId,
 			List<PostView> objects) {
 		super(context, textViewResourceId, objects);
 		activity = context;
 		data = objects;
 	}
 	
 	 /*
      * (non-Javadoc)
      * @see android.widget.ArrayAdapter#getItem(int)
      */
     @Override  
     public PostView getItem(int position) {     
         return data.get(position);  
     } 
     
     /*
 	 * (non-Javadoc)
 	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
 	 */
 	public View getView(int position, View convertView, ViewGroup parent) {		
 		View vi = convertView;
         if(vi == null) {
         	LayoutInflater vinf =
                     (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             vi = vinf.inflate(R.layout.view_newreply, null);
         }
         
         final PostView cv = data.get(position);
         
         ((TextView)vi.findViewById(R.id.nr_username)).setText(cv.getUserName());
         ((TextView)vi.findViewById(R.id.nr_userTitle)).setText(cv.getUserTitle());
         ((TextView)vi.findViewById(R.id.nr_userPosts)).setText(cv.getUserPostCount());
         ((TextView)vi.findViewById(R.id.nr_userJoin)).setText(cv.getJoinDate());
         ((TextView)vi.findViewById(R.id.nr_postDate)).setText(cv.getPostDate());
         
         TextView postText = ((TextView)vi.findViewById(R.id.nr_postText));
         ForumImageHandler fih = new ForumImageHandler(postText, activity);        
         postText.setText(Html.fromHtml(cv.getUserPost(), fih, null));
         
         // Display the right items if the user is logged in
         setUserIcons(vi, cv.isLoggedInUser());
         
         // Set click listeners
         ((ImageView)vi.findViewById(R.id.nr_quoteButton)).setOnClickListener(new OnClickListener() {
         	@Override
             public void onClick(View arg0) {
         		Log.d(TAG, "Quote Clicked");
				String txt = Html.fromHtml(cv.getUserPost()).toString();
 				String finalText = String.format("[quote=%s]%s[/quote]",
 						cv.getUserName(), txt);
 				((TextView)((Activity) activity).findViewById(R.id.postBox)).setText(finalText);
 				((TextView)((Activity) activity).findViewById(R.id.postBox)).requestFocus();
         	}
         });
         
         ((ImageView)vi.findViewById(R.id.nr_editButton)).setOnClickListener(new OnClickListener() {
         	@Override
         	public void onClick(View arg0) {
         		Log.d(TAG, "Edit Clicked");
         		Intent _intent = new Intent(activity, EditPostActivity.class); 
         		_intent.putExtra("postid", cv.getPostId());
 				_intent.putExtra("securitytoken", cv.getToken());
 				activity.startActivity(_intent);
         	}
         });
         
         ((ImageView)vi.findViewById(R.id.nr_pmButton)).setOnClickListener(new OnClickListener() {
         	@Override
         	public void onClick(View arg0) {
         		Log.d(TAG, "PM Clicked");
         		Intent _intent =  new Intent(activity, NewPrivateMessageActivity.class);
 				_intent.putExtra("user", cv.getUserName());
 				activity.startActivity(_intent);
         	}
         });
     	
         final boolean isFirstPost = (position == 0);
         ((ImageView)vi.findViewById(R.id.nr_deleteButton)).setOnClickListener(new OnClickListener() {
         	@Override
         	public void onClick(View arg0) {
         		final Intent _intent = new Intent(activity, EditPostActivity.class);     		
 				DialogInterface.OnClickListener dialogClickListener = 
 					new DialogInterface.OnClickListener() {
 				    @Override
 				    public void onClick(DialogInterface dialog, int which) {
 				        switch (which){
 				        case DialogInterface.BUTTON_POSITIVE:  
 				        	_intent.putExtra("postid", cv.getPostId());
 				        	_intent.putExtra("securitytoken", cv.getToken());
 				        	_intent.putExtra("delete", true);
 				        	_intent.putExtra("deleteThread", isFirstPost && cv.isLoggedInUser());
 				        	activity.startActivity(_intent);
 				            break;
 				        }
 				    }
 				};
 	
 				AlertDialog.Builder builder = 
 						new AlertDialog.Builder(activity);
 				builder
 					.setMessage("Are you sure you want to delete your post?")
 					.setPositiveButton("Yes", dialogClickListener)
 				    .setNegativeButton("No", dialogClickListener)
 				    .show();
         	}
         });
         
         return vi;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.widget.BaseAdapter#areAllItemsEnabled()
 	 */
 	@Override
 	public boolean  areAllItemsEnabled() {
 	    return false;			
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.widget.BaseAdapter#isEnabled(int)
 	 */
 	@Override
 	public boolean isEnabled(int position) {
 	        return false;
 	}
 	
 	/**
 	 * If the post is by the logged in user, make sure that they can see the edit and 
 	 * delete buttons
 	 * @param vi				The thread view object
 	 * @param isLoggedInUser	True if post is by logged user
 	 */
 	private void setUserIcons(View vi, boolean isLoggedInUser) {
 		((ImageView)vi.findViewById(R.id.nr_quoteButton))
 			.setVisibility(View.VISIBLE);
 		
 		((ImageView)vi.findViewById(R.id.nr_pmButton))
 			.setVisibility(View.VISIBLE);
 		
 		((ImageView)vi.findViewById(R.id.nr_editButton))
 			.setVisibility(isLoggedInUser? View.VISIBLE : View.GONE);
 		
 		((ImageView)vi.findViewById(R.id.nr_deleteButton))
 			.setVisibility(isLoggedInUser? View.VISIBLE : View.GONE);
 	}
 }
