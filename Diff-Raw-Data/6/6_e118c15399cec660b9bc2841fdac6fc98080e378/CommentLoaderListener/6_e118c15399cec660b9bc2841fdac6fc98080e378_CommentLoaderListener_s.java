 package de.raptor2101.GalDroid.Activities.Listeners;
 
 import java.text.DateFormat;
 import java.util.List;
 import java.util.Locale;
 
 import de.raptor2101.GalDroid.R;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;
 import de.raptor2101.GalDroid.WebGallery.Tasks.CommentLoaderTaskListener;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class CommentLoaderListener implements CommentLoaderTaskListener {
 	private final ViewGroup mRootView;
 	private final ProgressBar mProgressBar;
 	
 	public CommentLoaderListener(ViewGroup rootView, ProgressBar progressBar) {
 		mProgressBar = progressBar;
 		mRootView = rootView;
 	}
 	
 	public void onLoadingStarted() {
 		mRootView.removeAllViews();
 		
 		mProgressBar.setVisibility(View.VISIBLE);
 		
 	}
 
 	public void onLoadingProgress(int elementCount, int maxCount) {
 		mProgressBar.setMax(maxCount);
 		mProgressBar.setProgress(elementCount);
 		
 	}
 
 	public void onLoadingCompleted(List<GalleryObjectComment> comments) {
 		Context context = mRootView.getContext();
 		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		for(GalleryObjectComment comment:comments) {
 			View commentView = inflater.inflate(R.layout.comment_entry, null);
 			
 			TextView textAuthor = (TextView) commentView.findViewById(R.id.textCommentAuthor);
 			TextView textDate = (TextView) commentView.findViewById(R.id.textCommentPosted);
 			TextView textMessage = (TextView) commentView.findViewById(R.id.textCommentMessage);
 			
			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM,Locale.getDefault());
			
 			textAuthor.setText(comment.getAuthorName());
 			textDate.setText(dateFormat.format(comment.getCreateDate()));
 			textMessage.setText(comment.getMessage());
 			
 			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
 			params.setMargins(0, 0, 0, 10);
 			commentView.setLayoutParams(params);
 			
 			mRootView.addView(commentView);
 		}
 		
 		mProgressBar.setVisibility(View.GONE);
 		mRootView.setVisibility(View.VISIBLE);
 	}
 
 	public void onLoadingCanceled() {
 		mProgressBar.setVisibility(View.GONE);
 		mRootView.setVisibility(View.VISIBLE);
 	}
 	
 };
