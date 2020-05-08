 package com.tihonchik.lenonhonor360.ui.user;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Point;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.TextUtils.TruncateAt;
 import android.util.TypedValue;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TableRow.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.tihonchik.lenonhonor360.R;
 import com.tihonchik.lenonhonor360.custom.ResizableImageView;
 import com.tihonchik.lenonhonor360.listeners.BlogDetailOnClickListener;
 import com.tihonchik.lenonhonor360.models.BlogEntry;
 import com.tihonchik.lenonhonor360.ui.BaseFragment;
 import com.tihonchik.lenonhonor360.util.AppUtils;
 import com.tihonchik.lenonhonor360.util.BlogEntryUtils;
 
 public class BlogDisplayFragment extends BaseFragment {
 
 	private static final int SDK_VERSION = android.os.Build.VERSION.SDK_INT;
 	private ResizableImageView _newBlogImage;
 
 	class loadContentTask extends AsyncTask<String, Void, Drawable> {
 		TextView progressText;
 
 		@Override
 		protected Drawable doInBackground(String... args) {
 			try {
 				return AppUtils.getImageFromURL(args[0]);
 			} catch (IOException exception) {
 				return null;
 			}
 		}
 
 		@SuppressWarnings("deprecation")
 		@Override
 		protected void onPostExecute(Drawable result) {
 			if (result != null) {
 				if (SDK_VERSION < android.os.Build.VERSION_CODES.JELLY_BEAN) {
 					_newBlogImage.setBackgroundDrawable(result);
 				} else {
 					_newBlogImage.setBackground(result);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		List<BlogEntry> entries = BlogEntryUtils.getAllBlogEntries();
 		if (entries == null || entries.size() == 0) {
 			return null;
 		}
 
		String image = "http://placehold.it/320x240";
 		if (entries.get(0).getImages() != null
 				&& !"".equals(entries.get(0).getImages().size() > 0)) {
 			image = entries.get(0).getImages().get(0);
 		}
 
 		new loadContentTask().execute(image);
 
 		ViewGroup rootView = (ViewGroup) inflater.inflate(
 				R.layout.blog_display, container, false);
 
 		/*
 		 * Setup new blog entry with image
 		 */
 		_newBlogImage = (ResizableImageView) rootView
 				.findViewById(R.id.new_blog_image);
 		TextView newBlogTitle = (TextView) rootView
 				.findViewById(R.id.new_blog_title);
 		newBlogTitle.setTextColor(R.color.lh360Clouds);
 		newBlogTitle.setTypeface(null, Typeface.BOLD);
 		newBlogTitle.setText(entries.get(0).getTitle());
 		TextView newBlogText = (TextView) rootView
 				.findViewById(R.id.new_blog_text);
 		newBlogText.setText(BlogEntryUtils.replaceHTMLTags(entries.get(0)
 				.getBlog().replaceAll("<br>", " ").replaceAll(" +", " ")));
 
 		ImageView newBlog = (ImageView) rootView
 				.findViewById(R.id.btn_new_blog);
 		BlogDetailOnClickListener mBlogDetailListener = new BlogDetailOnClickListener(
 				getActivity(), entries.get(0));
 		newBlog.setOnClickListener(mBlogDetailListener);
 
 		/*
 		 * Setup up the table with older blog entries
 		 */
 		TableLayout tableLayout = (TableLayout) rootView
 				.findViewById(R.id.older_posts);
 
 		Display display = getActivity().getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		double blogTextWidthPercentage = size.x * 0.75;
 
 		for (int i = 1; i < entries.size(); i++) {
 
 			TableRow tableRow = new TableRow(getActivity());
 			tableRow.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			tableRow.setGravity(Gravity.CENTER_VERTICAL);
 			ImageView bulletImage = new ImageView(getActivity());
 			bulletImage.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
 			bulletImage.setImageResource(R.drawable.icon_bullet);
 			tableRow.addView(bulletImage);
 
 			LinearLayout innerLayout = new LinearLayout(getActivity());
 			innerLayout.setLayoutParams(new LayoutParams(
 					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2f));
 			innerLayout.setOrientation(LinearLayout.VERTICAL);
 
 			TextView rowTitle = new TextView(getActivity());
 			rowTitle.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
 			rowTitle.setTextColor(R.color.lh360Clouds);
 			rowTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
 			rowTitle.setTypeface(null, Typeface.BOLD);
 			rowTitle.setEllipsize(TruncateAt.END);
 			rowTitle.setWidth((int) blogTextWidthPercentage);
 			rowTitle.setText(entries.get(i).getTitle());
 
 			TextView rowText = new TextView(getActivity());
 			rowText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
 					LayoutParams.WRAP_CONTENT, 1f));
 			rowText.setTextColor(R.color.lh360Clouds);
 			rowText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
 			rowText.setEllipsize(TruncateAt.END);
 			rowText.setMaxLines(1);
 			rowText.setWidth((int) blogTextWidthPercentage);
 			rowText.setText(BlogEntryUtils.replaceHTMLTags(entries.get(i)
 					.getBlog()));
 
 			innerLayout.addView(rowTitle);
 			innerLayout.addView(rowText);
 			tableRow.addView(innerLayout);
 
 			ImageView readMore = new ImageView(getActivity());
 			readMore.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
 			readMore.setImageResource(R.drawable.read_more);
 			mBlogDetailListener = new BlogDetailOnClickListener(getActivity(),
 					entries.get(i));
 			readMore.setOnClickListener(mBlogDetailListener);
 			tableRow.addView(readMore);
 
 			tableLayout.addView(tableRow);
 		}
 		return rootView;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// TODO: write onResume method to load correct elements
 		Activity a = getActivity();
 		if (a == null) {
 			return;
 		}
 	}
 }
