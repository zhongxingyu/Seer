 package se.mah.kd330a.project.home;
 
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 
 import se.mah.kd330a.project.framework.MainActivity;
 //import com.handmark.pulltorefresh.library.PullToRefreshBase;
 //import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
 //import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
 import se.mah.kd330a.project.home.data.RSSFeed;
 import se.mah.kd330a.project.itsl.Article;
 import se.mah.kd330a.project.itsl.FeedManager;
 import se.mah.kd330a.project.itsl.ListPagerAdapter;
 import se.mah.kd330a.project.schedule.view.FragmentScheduleWeekPager;
 import se.mah.kd330a.project.R;
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FragmentHome extends Fragment implements FeedManager.FeedManagerDoneListener
 {
 
 	private NextClassWidget nextClass;
 	private ViewGroup rootView;
 	private RSSFeed newsFeed;
 	private ObjectInputStream in = null;
 	private FileInputStream fis = null;
 	private boolean profileRegistered = false;
 	private FeedManager ITSLfeedManager;
 
 	public FragmentHome()
 	{
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		ITSLfeedManager = new FeedManager(this, getActivity().getApplicationContext());
 		if (!ITSLfeedManager.loadCache())
 		{
 			ITSLfeedManager.reset();
 			ITSLfeedManager.processFeeds();
 		}
 		
 		try
 		{
 			nextClass = new NextClassWidget();
 			profileRegistered = nextClass.getTodaysClasses();
 		}
 		catch (Exception e)
 		{
 			Log.e("FragmentHome", e.toString());
 		}
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		rootView = (ViewGroup) inflater.inflate(R.layout.fragment_screen_home, container, false);
 		setNextClassWidget(rootView);
 		setNewsFeedMah(rootView);
 		setLastItslPost(rootView);
 		return rootView;
 
 	}
 
 	private void setNewsFeedMah(ViewGroup rootView)
 	{
 		try
 		{
 			fis = getActivity().openFileInput("filename");
 			in = new ObjectInputStream(fis);
 			newsFeed = (RSSFeed) in.readObject();
 			in.close();
 			fis.close();
 			Log.i("setNewsFeedMah", Integer.toString(newsFeed.getItemCount()));
 		}
 		catch (Exception ex)
 		{
 			Log.e("FragmentHome", "Error in get method");
 		}
 
 		try
 		{
 			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 			LinearLayout newsFeedMahWidget = (LinearLayout) rootView.findViewById(R.id.news_feed_widget);
 			for (int i = 0; i < 1; i++)
 			{
 				TextView title = (TextView) newsFeedMahWidget.findViewById(R.id.text_latest_news_heading);
 
 				title.setText(newsFeed.getItem(i).getTitle());
 				TextView description = (TextView) newsFeedMahWidget.findViewById(R.id.text_latest_news_description);
 
 				description.setText(newsFeed.getItem(i).getDescription());
 
 			}
 		}
 		catch (Exception ex)
 		{
 			Log.e("FragmentHome", "Error in get method");
 		}
 
 	}
 
 	private void setNextClassWidget(ViewGroup rootView)
 	{
 		LinearLayout nextClassWidget = (LinearLayout) rootView.findViewById(R.id.next_class_widget);
 		SharedPreferences sharedPref = this.getActivity().getSharedPreferences("courseName", Context.MODE_PRIVATE);
 		String courseName = sharedPref.getString(nextClass.getCourseName(), nextClass.getCourseName());
 		if (profileRegistered)
 		{
 			TextView textNextClassName = (TextView) nextClassWidget.findViewById(R.id.text_next_class_name);
 			textNextClassName.setText(courseName);
 			TextView textNextClassDate = (TextView) nextClassWidget.findViewById(R.id.text_next_class_date);
 			textNextClassDate.setText(nextClass.getDate());
 			TextView textNextClassStartTime = (TextView) nextClassWidget.findViewById(R.id.text_next_class_start_time);
 			textNextClassStartTime.setText(nextClass.getStartTime());
 			TextView textNextClassEndTime = (TextView) nextClassWidget.findViewById(R.id.text_next_class_end_time);
 			textNextClassEndTime.setText(nextClass.getEndTime());
 			TextView textNextClassLocation = (TextView) nextClassWidget.findViewById(R.id.text_next_class_location);
 			textNextClassLocation.setText(nextClass.getLocation());
 		}
 		else
 		{
 			TextView textNextClassDate = (TextView) nextClassWidget.findViewById(R.id.text_next_class_date);
 			textNextClassDate.setText("No classes");
 		}
 
 	}
 
 	private SharedPreferences getSharedPreferences(String string, int modePrivate)
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private void setLastItslPost(ViewGroup rootView)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	private class UpdateDataTask extends AsyncTask<Void, Void, String[]>
 	{
 
 		@Override
 		protected String[] doInBackground(Void... params)
 		{
 			// Simulates a background job.
 			try
 			{
 				Thread.sleep(4000);
 			}
 			catch (InterruptedException e)
 			{
 
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(String[] result)
 		{
 			// Do some stuff here
 
 			// Call onRefreshComplete when the list has been refreshed.
 			// mPullRefreshScrollView.onRefreshComplete();
 			super.onPostExecute(result);
 		}
 	}
 
 	@Override
 	public void onFeedManagerDone(FeedManager fm, ArrayList<Article> articles)
 	{
 		try
 		{
			//View widget = (View)rootView.findViewById(R.id.itslearning_widget);
			View widget = rootView;
 			Article a = articles.get(0);
 			((TextView)widget.findViewById(R.id.text_itsl_title)).setText(a.getArticleHeader());
 			((TextView)widget.findViewById(R.id.text_itsl_date)).setText(a.getArticleDate());
 			((TextView)widget.findViewById(R.id.text_itsl_content)).setText(a.getArticleText());
 		}
 		catch(Exception e)
 		{
 			Log.i("FragmentHome", "onFeedManagerDone(): " + e.toString());
 		}
 	}
 
 	@Override
 	public void onFeedManagerProgress(FeedManager fm, int progress, int max)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 }
