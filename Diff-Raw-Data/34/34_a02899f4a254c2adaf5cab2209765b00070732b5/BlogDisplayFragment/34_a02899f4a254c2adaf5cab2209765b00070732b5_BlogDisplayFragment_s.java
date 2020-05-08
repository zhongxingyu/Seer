 package com.tihonchik.lenonhonor360.ui.user;
 
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.NotificationCompat;
 import android.text.Html;
 import android.text.TextUtils.TruncateAt;
 import android.text.method.LinkMovementMethod;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.TableRow.LayoutParams;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.tihonchik.lenonhonor360.AppDefines;
 import com.tihonchik.lenonhonor360.R;
 import com.tihonchik.lenonhonor360.models.BlogEntry;
 import com.tihonchik.lenonhonor360.ui.BaseFragment;
 import com.tihonchik.lenonhonor360.util.AppUtils;
 import com.tihonchik.lenonhonor360.util.BlogEntryUtils;
 
 public class BlogDisplayFragment extends BaseFragment {
 
 	private static final int SDK_VERSION = android.os.Build.VERSION.SDK_INT;
 	private NotificationManager notificationManager;
 	private Notification notification;
 	private ImageView _newBlogImage;
 
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
 
 	OnClickListener mNotifcationListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			System.out.println("LH360 NOTIFICATION!!!");
 			notificationManager.notify(AppDefines.LH360_NOTIFICATION_ID,
 					notification);
 		}
 	};
 
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
 		String blogImage = BlogEntryUtils.getImageById(entries.get(0).getId());
 		if (!"".equals(blogImage)) {
 			image = blogImage;
 		}
 
 		new loadContentTask().execute(image);
 
 		ViewGroup rootView = (ViewGroup) inflater.inflate(
 				R.layout.blog_display, container, false);
 
 		/*
 		 * Setup new blog entry with image
 		 */
 		_newBlogImage = (ImageView) rootView.findViewById(R.id.new_blog_image);
 		TextView newBlogTitle = (TextView) rootView
 				.findViewById(R.id.new_blog_title);
 		newBlogTitle.setText(entries.get(0).getTitle());
 		TextView newBlogText = (TextView) rootView
 				.findViewById(R.id.new_blog_text);
 		newBlogText.setText(entries.get(0).getBlog().replaceAll("<br>", " ")
 				.replaceAll(" +", " "));
 
 		Button newBlog = (Button) rootView.findViewById(R.id.btn_new_blog);
 		BlogDetailOnClickListener mBlogDetailListener = new BlogDetailOnClickListener(entries.get(0));
 		newBlog.setOnClickListener(mBlogDetailListener);
 
 		/*
 		 * Setup up the table with older blog entries
 		 */
 		TableLayout tableLayout = (TableLayout) rootView
 				.findViewById(R.id.older_posts);
 		for (int i = 1; i < entries.size(); i++) {
 			TableRow row = new TableRow(getActivity());
 			row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 					LayoutParams.WRAP_CONTENT));
 			LinearLayout outerLayout = new LinearLayout(getActivity());
 			outerLayout.setLayoutParams(new LayoutParams(
 					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 			outerLayout.setOrientation(LinearLayout.HORIZONTAL);
 			ImageView rowImage = new ImageView(getActivity());
 			rowImage.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			rowImage.setImageResource(R.drawable.icon_bullet);
 			LinearLayout innerLayout = new LinearLayout(getActivity());
 			innerLayout.setLayoutParams(new LayoutParams(
 					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 			innerLayout.setOrientation(LinearLayout.VERTICAL);
 			TextView rowTitle = new TextView(getActivity());
 			rowTitle.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			rowTitle.setTextColor(R.color.lh360Clouds);
 			rowTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
 			rowTitle.setText(entries.get(i).getTitle());
 			TextView rowText = new TextView(getActivity());
			rowText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
 			rowText.setTextColor(R.color.lh360Black);
 			rowText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
 			rowText.setEllipsize(TruncateAt.END);
			// rowText.setMaxLines(2);
 			rowText.setText(entries.get(i).getBlog().replaceAll("<br>", " ")
 					.replaceAll(" +", " "));
 
 			ImageView readMoreImage = new ImageView(getActivity());
			readMoreImage.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			readMoreImage.setImageResource(R.drawable.read_more);
 
 			innerLayout.addView(rowTitle);
			innerLayout.addView(rowText);
 			outerLayout.addView(rowImage);
 			outerLayout.addView(innerLayout);
			outerLayout.addView(readMoreImage);
 			row.addView(outerLayout);
 			mBlogDetailListener = new BlogDetailOnClickListener(entries.get(i));
 			row.setOnClickListener(mBlogDetailListener);
 			tableLayout.addView(row);
 		}
 
 		Button newNotification = (Button) rootView
 				.findViewById(R.id.btn_new_notification);
 		newNotification.setOnClickListener(mNotifcationListener);
 
 		TextView link = (TextView) rootView.findViewById(R.id.official_link);
 		link.setText(Html
 				.fromHtml("<a href='fakehttp://lenonhonor360.com/'>lenonhonor360.com</a>"));
 		link.setMovementMethod(LinkMovementMethod.getInstance());
 
 		PendingIntent contentIntent = PendingIntent.getActivity(getActivity(),
 				0, new Intent(getActivity(), MainActivity.class),
 				Intent.FLAG_ACTIVITY_NEW_TASK);
 		NotificationCompat.Builder builder = new NotificationCompat.Builder(
 				getActivity());
 		notificationManager = (NotificationManager) getActivity()
 				.getSystemService(Context.NOTIFICATION_SERVICE);
 		notification = builder.setContentIntent(contentIntent)
 				.setSmallIcon(R.drawable.notificationicon)
 				.setTicker("Notification!").setWhen(System.currentTimeMillis())
 				.setAutoCancel(true).setContentTitle("Notification!")
 				.setContentText("LenonHonor360 Notification!").build();
 
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
 
 	public class BlogDetailOnClickListener implements OnClickListener {
 		BlogEntry blog = null;
 
 		public BlogDetailOnClickListener(BlogEntry blog) {
 			this.blog = blog;
 		}
 
 		@Override
 		public void onClick(View v) {
 			Bundle args = new Bundle();
 			args.putSerializable(AppDefines.TAG_BLOG_DISPLAY_DETAIL, blog);
 			Fragment blogDisplayFragment = new BlogDetailFragment();
 			blogDisplayFragment.setArguments(args);
 			getFragmentManager()
 					.beginTransaction()
 					.replace(R.id.body, blogDisplayFragment,
 							AppDefines.TAG_BLOG_DETAIL)
 					.addToBackStack(AppDefines.TAG_BLOG_DETAIL).commit();
 		}
 	};
 }
