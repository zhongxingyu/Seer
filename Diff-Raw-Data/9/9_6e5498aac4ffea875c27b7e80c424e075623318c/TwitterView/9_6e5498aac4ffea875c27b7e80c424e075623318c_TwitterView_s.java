 package com.se.cronus.items.twitter;
 
 import java.util.ArrayList;

 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Color;
 import android.util.Pair;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;

 import com.se.cronus.MainActivity;
 import com.se.cronus.items.ItemDoc;
 import com.se.cronus.items.ItemFragmentView;
 import com.se.cronus.utils.CUtils;
 
 public class TwitterView extends ItemFragmentView {
 	
 	private ImageView sharedImg;
 	private ScrollView contentScroll;
 	private LinearLayout content;
 	private LinearLayout imageOverlay; 
 	private LinearLayout tweetLayout;
 	private TextView tweet;
 	private TextView author;
 	//private ArrayList<LinearLayout> comments;
 	private ImageView profilePic;
 	public ImageView replyButton;
 	public ImageView retweetButton;
 	public ImageView favoriteButton;
 
 	public TwitterView(ItemDoc d, Context c) {
 		super(d, c);
 		setOnClicks();
 		pullContent();
 		setStyles();
 	}
 
 	private void setStyles() {
 		this.setBackgroundColor(CUtils.TWITTER_BLUE);
 		
 //		sharedImg = new ImageView(this.getContext());
 //		contentScroll = new ScrollView(this.getContext());
 //		content.setBackgroundColor(Color.TRANSPARENT);
 		imageOverlay.setBackgroundColor(Color.CYAN);
 		tweetLayout.setBackgroundColor(Color.YELLOW);
 		
 //		author = new TextView(this.getContext());
 //		tweet = new TextView(this.getContext());
 //		comments = new ArrayList<LinearLayout>();
 //		profilePic = new ImageView(this.getContext());
 //		replyButton = new ImageView(this.getContext());
 //		retweetButton = new ImageView(this.getContext());
 //		favoriteButton = new ImageView(this.getContext());
 //		
 	}
 
 	@SuppressLint("NewApi")
 	public ItemDoc pullContent() {
 		sharedImg.setBackground(doc.getImg());
 		profilePic.setBackground(doc.getProfPic());
 		author.setText(doc.getAuthor());
 		tweet.setText(doc.getStatus());
 		for(Pair<String, String> cur : doc.getComments())
 		{
 			LinearLayout tempComment = new LinearLayout(this.getContext());
 			TextView tempAuthor = new TextView(this.getContext());
 			tempAuthor.setText(cur.first);
 			TextView tempText = new TextView(this.getContext());
 			tempText.setText(cur.second);
 			tempComment.addView(tempAuthor);
 			tempComment.addView(tempText);
 			content.addView(tempComment);
 		}
 		
		
		
 		return this.getDoc();
 	}
 
 	private void setOnClicks() {
 		clicklistener = new TwitterCntrl(this);
 		replyButton.setOnClickListener(clicklistener);
 		retweetButton.setOnClickListener(clicklistener);
 		favoriteButton.setOnClickListener(clicklistener);
 	}
 
 	@Override
 	protected void onCreate() {
 		sharedImg = new ImageView(this.getContext());
 		contentScroll = new ScrollView(this.getContext());
 		content = new LinearLayout(this.getContext());
 		imageOverlay = new LinearLayout(this.getContext());
 		tweetLayout = new LinearLayout(this.getContext());
 		author = new TextView(this.getContext());
 		tweet = new TextView(this.getContext());
 		//comments = new ArrayList<LinearLayout>();
 		profilePic = new ImageView(this.getContext());
 		replyButton = new ImageView(this.getContext());
 		retweetButton = new ImageView(this.getContext());
 		favoriteButton = new ImageView(this.getContext());
 		author = new TextView(this.getContext());
 	}
 
 	@Override
 	protected void setLayoutParams() {
 		//params for image
 		RelativeLayout.LayoutParams pImage = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, CUtils.getScreenHeight((MainActivity) this.getContext())/2);
 		pImage.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		pImage.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 		sharedImg.setLayoutParams(pImage);
 		
 		//params for the scrolling content
 		RelativeLayout.LayoutParams pContent = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 		pContent.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		pContent.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 		contentScroll.setLayoutParams(pContent);
 		
 		//add top level views
 		this.addView(sharedImg);
 		this.addView(contentScroll);
 		
 		content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		content.setOrientation(LinearLayout.VERTICAL);
 		contentScroll.addView(content);
 		
 		//now dealing with content
		imageOverlay.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,CUtils.getScreenHeight((MainActivity) this.getContext())/2));
 		imageOverlay.setVisibility(View.INVISIBLE);
 		tweetLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 		tweetLayout.setOrientation(LinearLayout.HORIZONTAL);
 		content.addView(imageOverlay);
 		content.addView(tweetLayout);
 		
 		//tweetLayout stuff
 		LinearLayout tweetInner = new LinearLayout(this.getContext());
 		tweetInner.setOrientation(LinearLayout.VERTICAL);
 		tweetInner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
 		LinearLayout innerButtons = new LinearLayout(this.getContext());
 		innerButtons.setOrientation(LinearLayout.HORIZONTAL);
 		profilePic.setLayoutParams(new LayoutParams(100, 100));//TODO, acutally crop this photo
 		tweetLayout.addView(profilePic);
 		tweetLayout.addView(tweetInner);
 		tweetInner.addView(author);
 		tweetInner.addView(tweet);
 		tweetInner.addView(innerButtons);
 		
 		//add buttons to tweetInner
 		innerButtons.addView(replyButton);
 		innerButtons.addView(retweetButton);
 		innerButtons.addView(favoriteButton);
 		
 	}
 
 	@Override
 	public void destroy() {
 		
 	}
 
 }
