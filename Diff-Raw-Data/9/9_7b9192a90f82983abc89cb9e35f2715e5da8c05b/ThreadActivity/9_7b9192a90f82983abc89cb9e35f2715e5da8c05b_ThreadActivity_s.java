 package com.normalexception.forum.rx8club.activities;
 
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
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.InputType;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.SpannableStringBuilder;
 import android.text.method.LinkMovementMethod;
 import android.text.style.ImageSpan;
 import android.text.style.StyleSpan;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.TextView.BufferType;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.normalexception.forum.rx8club.MainApplication;
 import com.normalexception.forum.rx8club.R;
 import com.normalexception.forum.rx8club.enums.ThreadButtonSize;
 import com.normalexception.forum.rx8club.handler.ForumImageHandler;
 import com.normalexception.forum.rx8club.task.SubmitTask;
 import com.normalexception.forum.rx8club.utils.PreferenceHelper;
 import com.normalexception.forum.rx8club.utils.UserProfile;
 import com.normalexception.forum.rx8club.utils.Utils;
 import com.normalexception.forum.rx8club.utils.VBForumFactory;
 import com.normalexception.forum.rx8club.view.PostButtonView;
 import com.normalexception.forum.rx8club.view.ViewContents;
 
 /**
  * Activity used to display thread contents.  Within this activity a user can
  * create new posts.
  * 
  * Required Intent Parameters:
  * link - The link to the thread
  * title - The title of the thread
  * page - The page number of the thread
  */
 public class ThreadActivity extends ForumBaseActivity implements OnClickListener {
 
 	private static final String TAG = "Application:Thread";
 	public static final int ThreadIdIndex = 9000;
 	
 	private String currentPageLink;
 	private String currentPageTitle;
 	
 	private String threadNumber;
 	
 	private String pageNumber = "1";
 	
 	private String securityToken = "none";
 	private String postNumber = "none";
 	
 	private int scaledImage = 12;
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.normalexception.forum.rx8club.activities.ForumBaseActivity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString("threadnumber", threadNumber);
 		outState.putString("securitytoken", securityToken);
 		outState.putString("postnumber", postNumber);
 		outState.putString("final", finalPage);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.normalexception.forum.rx8club.activities.ForumBaseActivity#onRestoreInstanceState(android.os.Bundle)
 	 */
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		try {
 			if(savedInstanceState != null) {
 				threadNumber = savedInstanceState.getString("threadnumber");
 				securityToken = savedInstanceState.getString("securitytoken");
 				postNumber = savedInstanceState.getString("postnumber");
 				finalPage = savedInstanceState.getString("final");
 			}				
 		} catch (Exception e) {
 			Log.e(TAG, "Error Restoring Contents: " + e.getMessage());
 			BugSenseHandler.sendException(e);
 		}
 	}
 	
 	/**
 	 * Container for thread posts and thread post related information
 	 */
 	private class ThreadPost {
 		private String name, title, location, join, postcount, post, 
 					   postDate, postid;
 		public String toString() {
 			return postid + "|" + name + "|" + title + "|" + location + 
 					"|" + join + "|" + postcount + "|" + post + "|" + postDate;
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
     @SuppressWarnings("unchecked")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
     	try{
 	    	 super.onCreate(savedInstanceState);
 	         super.setTitle("RX8Club.com Forums");
 	         setContentView(R.layout.activity_thread);
 	         
 	         Log.v(TAG, "Category Activity Started");
 
 	         // Register the titlebar gui buttons
 	         this.registerGuiButtons();
 	         
 	         findViewById(R.id.previousButton).setOnClickListener(this);
 	         findViewById(R.id.nextButton).setOnClickListener(this);
 	         findViewById(R.id.paginationText).setOnClickListener(this);
 	         findViewById(R.id.submitButton).setOnClickListener(this);
 	         findViewById(R.id.firstButton).setOnClickListener(this);
 	         findViewById(R.id.lastButton).setOnClickListener(this);
 	         
 	         runOnUiThread(new Runnable() {
 		            public void run() {
 		            	findViewById(R.id.controlsRow).setVisibility(View.GONE);
 		            }
 	         });
 	         
 	         setScaledImageSizes();
 	         
 	         if(savedInstanceState == null)
 		        	constructView();
 		        else {
 		        	viewContents = (ArrayList<ViewContents>) savedInstanceState.getSerializable("contents");
 		        	updateView(viewContents);
 		        }
 	 	} catch (Exception e) {
 	 		Log.e(TAG, "Fatal Error In Thread Activity! " + e.getMessage());
 	 		BugSenseHandler.sendException(e);
 	 	}
     }
     
     /**
      * Depending on the screen DPI, we will rescale the thread
      * buttons to make sure that they are not too small or 
      * too large
      */
     private void setScaledImageSizes() {
     	switch(getResources().getDisplayMetrics().densityDpi) {
     	case DisplayMetrics.DENSITY_LOW:
     	case DisplayMetrics.DENSITY_MEDIUM:
     		this.scaledImage = ThreadButtonSize.LDPI.getValue();
     		break;
     	case DisplayMetrics.DENSITY_HIGH:
     		this.scaledImage = ThreadButtonSize.HDPI.getValue();
     		break;
     	case DisplayMetrics.DENSITY_XHIGH:
     		this.scaledImage = ThreadButtonSize.XHDPI.getValue();
     		break;
     	}
     }
     
     /**
      * Construct the thread activity view
      */
     private void constructView() {
     	loadingDialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
         
         updaterThread = new Thread("CategoryThread") {
 			public void run() {
 				currentPageLink = 
 		        		(String) getIntent().getStringExtra("link");
 				currentPageTitle = 
 						(String) getIntent().getStringExtra("title");			
 				pageNumber = 
 						(String) getIntent().getStringExtra("page");
 				if(pageNumber == null) pageNumber = "1";
 				
 				Log.v(TAG, "Grabbing link: " + currentPageLink);
 				
 				Document doc = VBForumFactory.getInstance().get(currentPageLink);
 				viewContents = new ArrayList<ViewContents>();
 				
 				final ArrayList<ThreadPost> list = getThreadContents(doc);
 				
 				viewContents.add(
 						new ViewContents(Color.BLUE, currentPageTitle, 40, "", false, true));
 		        
                	for(ThreadPost post : list) {                   		
                		String text = post.name + "\n" + post.title + "\n" + 
                				post.location + "\n" + post.join + "\n" + 
                				post.postcount + "\n\n" + "Post Date: " + post.postDate;
                		
                		viewContents.add(new ViewContents(Color.GRAY, text, 22, 
                				post.postid, false, false));
                		viewContents.add(new ViewContents(Color.DKGRAY, post.post, 23, 
                				post.postid, true, false));
                	}
 		    	
 		    	runOnUiThread(new Runnable() {
 		            public void run() {
 		            	findViewById(R.id.controlsRow).setVisibility(View.VISIBLE);
 		            }
 		    	});
 		    	
 		    	updateView(viewContents);
 		    	loadingDialog.dismiss();			
 			}
         };
         updaterThread.start();
     }
     
     /**
      * Update the view contents
      * @param contents	List of view rows
      */
     private void updateView(final ArrayList<ViewContents> contents) {
     	runOnUiThread(new Runnable() {
     		public void run() {
     			tl = (TableLayout)findViewById(R.id.myTableLayoutThread);
     			
     			int index = 0;
     			for(ViewContents view : contents) {
     				addRow(view.getClr(), view.getText(), index++, 
     						view.getPostId(), view.isHtml(), view.isSpan());
     			}
     		}
     	});
     }
     
     /**
      * Add a row to the view
      * @param clr		The background color of the row
      * @param text		The text for the row
      * @param id		The id of the row
      * @param postid 	The id of the post
      * @param html		True if the text contains html
      * @param span		True if we are creating a spannable string
      */
     private void addRow(int clr, String text, int id, String postid, boolean html, boolean span) {
     	/* Create a new row to be added. */
     	TableRow tr_head = new TableRow(this);
     	tr_head.setId(id);
     	tr_head.setBackgroundColor(clr);
     	tr_head.setWeightSum(1);
     	
     	if(clr == Color.DKGRAY)
     		tr_head.setPadding(5, 5, 5, 15);
 
     	/* Create a Button to be the row-content. */
     	TextView b = new TextView(this);
     	b.setId(ThreadActivity.ThreadIdIndex + id);
     	b.setMovementMethod(LinkMovementMethod.getInstance());
     	if(!html && text.indexOf("\n") != -1) {
     		SpannableString spanString = new SpannableString(text);
     		spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, text.indexOf("\n"), 0);
     		b.setText(spanString);
     	} else {
     		// remove quotes for now
     		text = reformatQuotes(text);
    		ForumImageHandler imageHandler = new ForumImageHandler(b, this);
    		b.setText(html? Html.fromHtml(text + "<br><br><br>", imageHandler, null) : text);
     	}
     	
     	b.setTextSize((float) PreferenceHelper.getFontSize(this));
     	b.setTextColor(Color.WHITE);
     	
     	/* Add Button to row. */
     	TableRow.LayoutParams params = new TableRow.LayoutParams(
                 ViewGroup.LayoutParams.FILL_PARENT,
                 ViewGroup.LayoutParams.FILL_PARENT,
                 1.0f);
     	params.weight = 0;
     	
     	if(span) {
 	        params.gravity = Gravity.CENTER;
     	} else {
         	// Convert dip to px
         	Resources r = getResources();
         	int px = 
         			(int)TypedValue.applyDimension(
         					TypedValue.COMPLEX_UNIT_DIP, 0, r.getDisplayMetrics());
         	b.setWidth(px);        	
         	params.weight = 1;
     	}
 
     	/* Add Button to row. */
         tr_head.addView(b, params);      
         
         // Thread title information, so add the edit button
         if(!html && !span) { 	
     		addPostButtons(tr_head, getPostUser(text), 
     				postid, id, params);
         }
 
     	/* Add row to TableLayout. */
         tl.addView(tr_head, tl.getChildCount() - 1);
     }
     
     /**
      * Add the buttons to the post title that allow the user to quote, edit, or
      * delete their posts
      * @param tr_head	The row object
      * @param user		The user of the current post 
      * @param postid	The id number of the post
      * @param id		The id number of the view object
      * @param params	The layout params
      */
     private void addPostButtons(TableRow tr_head, String user, 
     		String postid, int id, TableRow.LayoutParams params) {
     	int image = R.drawable.quote_icon;
 		int buttonType = PostButtonView.QUOTEBUTTON;
 		
 		// First time around, we add the pencil icon, then
 		// we set the image object to the X
 		for(int i = 0; i < 3; i++) {
 			if(i == 1) {
 				image = R.drawable.black_pencil_icon;
 				buttonType = PostButtonView.EDITBUTTON;
 			} else if (i == 2) {
 				image = R.drawable.black_x;
 				buttonType = PostButtonView.DELETEBUTTON;
 			}
 			
 			// If the post is not by the user, we are going
 			// to skip this iteration if it is an edit or
 			// delete button
 			if(buttonType == PostButtonView.EDITBUTTON &&
 					!isPostByUser(user))
 				continue;
 			
 			if(buttonType == PostButtonView.DELETEBUTTON && 
 					!isPostByUser(user))
 				continue;
 				
 			// Check our preferences if the user disabled any of the 
 			// buttons
 			if(buttonType == PostButtonView.EDITBUTTON && 
 					!PreferenceHelper.isShowEditButton(this))
 				continue;
 			
 			if(buttonType == PostButtonView.DELETEBUTTON && 
 					!PreferenceHelper.isShowDeleteButton(this))
 				continue;
 			
         	PostButtonView b = new PostButtonView(this, buttonType,
         			ThreadActivity.ThreadIdIndex+id+1, this.securityToken,
         			user, currentPageTitle, pageNumber);
         	SpannableStringBuilder ssb = 
         			new SpannableStringBuilder(" ");
         	
         	// We need to decode the resource, and then scale
         	// down the image
         	Bitmap scaledimg = 
         			Bitmap.createScaledBitmap(
         					BitmapFactory.decodeResource(
         							getResources(), image), 
         							scaledImage, scaledImage, true);
         	ssb.setSpan(new ImageSpan(scaledimg), 
         			0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE );
         	
         	// Set the text, padding, and add
         	b.setText(ssb, BufferType.SPANNABLE);
         	((PostButtonView)b).setPostId(postid);
         	b.setPadding(0, 15, 15, 0);
         	tr_head.addView(b, params);
     	}
     }
     
     /**
      * Get the user that posted the message
      * @param text	The thread text
      * @return		The user
      */
     private String getPostUser(String text) {
     	return text.split("\n")[0];
     }
     
     /**
      * Check if the post is by the logged in user
      * @param text	The post text
      * @return		True if the post is by the logged in user
      */
     private boolean isPostByUser(String text) {
 		// if the post is by the user, the user name should
     	// be the first string
     	String assumedUser = getPostUser(text);
     	if(UserProfile.getUsername().equals(assumedUser))
     		return true;
 		return false;
 	}
 
 	/**
      * Reformat the quotes to blockquotes since Android fromHtml does
      * not parse tables
      * @param source	The source text
      * @return			The updated source text
      */
     private String reformatQuotes(String source) {
     	String finalText = "";
 
     	StringTokenizer st = new StringTokenizer(source, "\r\n\t");
     	while (st.hasMoreTokens()) {
         	String nextTok = st.nextToken();      	
         	if(nextTok.contains("<table ")) {
         		nextTok = "<blockquote>";
         	}
         	if(nextTok.contains("</table>")) {
         		nextTok = nextTok.replace("</table>","</blockquote>");
         	}
 
         	finalText += nextTok + " ";
         }
         
         return finalText;
     }
     
     /**
      * Grab contents from the forum that the user clicked on
      * @param doc	The document parsed from the link
      * @param id	The id number of the link
      * @return		An arraylist of forum contents
      */
     public ArrayList<ThreadPost> getThreadContents(Document doc) {
     	ArrayList<ThreadPost> titles = new ArrayList<ThreadPost>();
     	
     	// Update pagination
     	updatePagination(doc);
     	
     	// Get Post Number and security token
     	securityToken = doc.select("input[name=securitytoken]").attr("value");
     	Elements pNumber = 
     			doc.select("a[href^=http://www.rx8club.com/newreply.php?do=newreply&noquote=1&p=]");
     	String pNumberHref = pNumber.attr("href");
     	postNumber = pNumberHref.substring(pNumberHref.lastIndexOf("=") + 1);
     	threadNumber = doc.select("input[name=searchthreadid]").attr("value");
     	
         Elements posts = doc.select("div[id=posts]").select("div[id^=edit]");
         for(Element post : posts) {
         	Elements innerPost = post.select("table[id^=post]");
         	
         	// User Control Panel
         	Elements userCp = innerPost.select("td[class=alt2]");
         	Elements userDetail = userCp.select("div[class=smallfont]");
         	Elements userSubDetail = null;
         	
         	try{ userSubDetail = userDetail.get(2).select("div"); }
         	catch(Exception e) { userSubDetail = userDetail.get(1).select("div"); }
     	
         	// User Information
         	ThreadPost user = new ThreadPost();
         	user.name = userCp.select("div[id^=postmenu]").text();
         	user.title = userDetail.get(0).text();
         	user.postDate = innerPost.select("td[class=thead]").get(0).text();
         	user.postid = Utils.parseInts(post.attr("id"));
         	
         	for(int i = 1; i < userSubDetail.size(); i++) {
         		switch(i) {
         		case 1:
         			break;
         		case 2:
         			user.join = userSubDetail.get(i).text();
         			break;
         		case 3:
         			user.location  = userSubDetail.get(i).text();
         			break;
         		case 4:
         			user.postcount  = userSubDetail.get(i).text();
         			break;
         		}
         	}
         	
         	// User Post Content
         	user.post = innerPost.select("td[class=alt1]").select("div[id^=post_message]").html();
         	
         	titles.add(user);
     	}
     	
     	return titles;
     }
     
     /*
      * (non-Javadoc)
      * @see com.normalexception.forum.rx8club.activities.ForumBaseActivity#enforceVariants(int, int)
      */
     @Override
     protected void enforceVariants(int myPage, int lastPage) {
     	final boolean first, prev, next, last;
     	
     	prev = myPage == 1? 		false : true;
     	first = myPage == 1? 		false : true;
     	next = lastPage > myPage? 	true : false;
     	last = myPage == lastPage? 	false : true;
     	
     	runOnUiThread(new Runnable() {
     		public void run() {
     			findViewById(R.id.previousButton).setEnabled(prev);
 				findViewById(R.id.firstButton).setEnabled(first);
 				findViewById(R.id.nextButton).setEnabled(next);
 				findViewById(R.id.lastButton).setEnabled(last);
     		}
     	});
     }
 
     /*
      * (non-Javadoc)
      * @see android.view.View.OnClickListener#onClick(android.view.View)
      */
 	@Override
 	public void onClick(View arg0) {	
 		super.onClick(arg0);
 		Intent _intent = null;
 		_intent = new Intent(ThreadActivity.this, ThreadActivity.class);
 		_intent.putExtra("title", this.currentPageTitle);
 		
 		switch(arg0.getId()) {
 			case R.id.previousButton:
 				_intent.putExtra("link", Utils.decrementPage(this.currentPageLink, this.finalPage));
 				_intent.putExtra("page", String.valueOf(Integer.parseInt(this.pageNumber) - 1));
 				this.finish();
 				break;
 			case R.id.nextButton:
 				_intent.putExtra("link", Utils.incrementPage(this.currentPageLink, this.finalPage));
 				_intent.putExtra("page", String.valueOf(Integer.parseInt(this.pageNumber) + 1));
 				this.finish();
 				break;
 			case R.id.submitButton:
 				String advert = PreferenceHelper.isAdvertiseEnabled(MainApplication.getAppContext())?
 						"\n\nPosted From RX8Club.com Android App" : "";
 				String toPost = 
 						((TextView)findViewById(R.id.postBox)).getText() + 
 						advert;
 				SubmitTask sTask = new SubmitTask(this, this.securityToken, 
 						this.threadNumber, this.postNumber,
 						toPost, this.currentPageTitle, this.pageNumber);
 				sTask.execute();
 				break;
 			case R.id.paginationText:
 				final EditText input = new EditText(this);
 				input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
 				new AlertDialog.Builder(ThreadActivity.this)
 			    .setTitle("Go To Page...")
 			    .setMessage("Enter New Page Number")
 			    .setView(input)
 			    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int whichButton) {
 			            String value = input.getText().toString(); 
 			            Intent _intent = new Intent(ThreadActivity.this, ThreadActivity.class);
 						_intent.putExtra("link", Utils.getPage(currentPageLink, value));
 						_intent.putExtra("page", value);
 						_intent.putExtra("title", currentPageTitle);
 						startActivity(_intent);
 						finish();
 			        }
 			    }).setNegativeButton("Cancel", null).show();	
 				_intent = null; // Just to make sure we dont start another activity 
 				break;
 				
 			case R.id.firstButton:
 				_intent.putExtra("link", Utils.getPage(this.currentPageLink, Integer.toString(1)));
 				_intent.putExtra("page", "1");
 				finish();
 				break;
 				
 			case R.id.lastButton:
 				_intent.putExtra("link", Utils.getPage(this.currentPageLink, this.finalPage));
 				_intent.putExtra("page", this.finalPage);
 				finish();
 				break;	
 				
 			default:
 				_intent = null;
 				break;
 		}	
 		
 		if(_intent != null)
 			startActivity(_intent);
 	}
 }
