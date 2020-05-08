 package cc.hughes.droidchatty;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.text.Html;
 import android.text.Spanned;
 import android.text.util.Linkify;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class SingleThreadView extends ListActivity {
 	
 	static final String THREAD_ID = "THREAD_ID";
 	static final String THREAD_CONTENT = "THREAD_CONTENT";
 	static final String THREAD_AUTHOR = "THREAD_AUTHOR";
 	static final String THREAD_POSTED = "THREAD_POSTED";
 	
 	private int _currentThreadId;
 	private int _rootThreadId;
 	private ProgressDialog _progressDialog = null;
 	private ArrayList<Thread> _posts = null;
 	private ThreadAdapter _adapter;
 	
 	private Runnable _retrieveThreads = new Runnable()
 	{
     	public void run()
     	{
     		getPosts();
     	}
 	};
     
     private Runnable _displayThreads = new Runnable()
     {
     	public void run()
     	{
     		if (_posts != null && _posts.size() > 0)
     		{
     			// remove the elements, and add in all the new ones
     			_adapter.clear();
     			for (Thread t : _posts)
     			{
     				_adapter.add(t);
     			}
     			_progressDialog.dismiss();
     			_adapter.notifyDataSetChanged();
     		}
     	}
     };
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.thread_view);
 		
 		_posts = new ArrayList<Thread>();
 		_adapter = new ThreadAdapter(this, R.layout.thread_row, _posts);
 		setListAdapter(_adapter);
 		
 		final ListView lv = getListView();
 		lv.setOnItemClickListener(new OnItemClickListener()
 		{
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 			{
 				displayPost(_posts.get(position));
 				lv.setSelection(position);
 			}
 		});
 		
 		Bundle extras = getIntent().getExtras();
 		_rootThreadId = extras.getInt(THREAD_ID);
 		
 		String content = extras.getString(THREAD_CONTENT);
 		String author = extras.getString(THREAD_AUTHOR);
 		String posted = extras.getString(THREAD_POSTED);
 		
 		Thread thread = new Thread();
 		thread.setThreadID(_rootThreadId);
 		thread.setContent(content);
 		thread.setPostedTime(posted);
 		thread.setUserName(author);
 		
 		displayPost(thread);
 		startRefresh();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.thread_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch(item.getItemId())
 		{
 		case R.id.refresh:
 			startRefresh();
 			return true;
 		case R.id.copyUrl:
 			copyCurrentPostUrl();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	private void copyCurrentPostUrl()
 	{
 		String url = "http://www.shacknews.com/chatty?id=" + _currentThreadId;
 		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
 		clipboard.setText(url);
 	}
 	
 	private void displayPost(Thread thread)
 	{
 		TextView tvAuthor = (TextView)findViewById(R.id.textUserName);
 		TextView tvContent = (TextView)findViewById(R.id.textContent);
 		TextView tvPosted = (TextView)findViewById(R.id.textPostedTime);
 		
 		tvAuthor.setText(thread.getUserName());
 		tvPosted.setText(thread.getPostedTime());
 		tvContent.setText(fixContent(thread.getContent()));
 		Linkify.addLinks(tvContent, Linkify.ALL);
 		tvContent.setClickable(false);
 		
 		_currentThreadId = thread.getThreadID();
 	}
 	
     private void startRefresh()
     {
         java.lang.Thread thread = new java.lang.Thread(null, _retrieveThreads, "Background");
         thread.start();
         _progressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving posts...", true);
     }
 	
 	private void getPosts()
 	{
     	try
     	{
 			_posts = ShackApi.getThread(_rootThreadId);
     	} catch (Exception ex)
     	{
     		ex.printStackTrace();
     	}
     	
     	runOnUiThread(_displayThreads);
 	}
 	
 	private Spanned fixContent(String content)
 	{
 		// convert shack's css into real font colors since Html.fromHtml doesn't supporty css of any kind
 		content = content.replaceAll("<span class=\"jt_red\">(.*?)</span>", "<font color=\"#ff0000\">$1</font>");
 		content = content.replaceAll("<span class=\"jt_green\">(.*?)</span>", "<font color=\"#8dc63f\">$1</font>");
 		content = content.replaceAll("<span class=\"jt_pink\">(.*?)</span>", "<font color=\"#f49ac1\">$1</font>");
         content = content.replaceAll("<span class=\"jt_olive\">(.*?)</span>", "<font color=\"#808000\">$1</font>");
         content = content.replaceAll("<span class=\"jt_fuchsia\">(.*?)</span>", "<font color=\"#c0ffc0\">$1</font>");
         content = content.replaceAll("<span class=\"jt_yellow\">(.*?)</span>", "<font color=\"#ffde00\">$1</font>");
         content = content.replaceAll("<span class=\"jt_blue\">(.*?)</span>", "<font color=\"#44aedf\">$1</font>");
         content = content.replaceAll("<span class=\"jt_lime\">(.*?)</span>",  "<font color=\"#c0ffc0\">$1</font>");
         content = content.replaceAll("<span class=\"jt_orange\">(.*?)</span>", "<font color=\"#f7941c\">$1</font>");
         content = content.replaceAll("<span class=\"jt_bold\">(.*?)</span>", "<b>$1</b>");
         content = content.replaceAll("<span class=\"jt_italic\">(.*?)</span>", "<i>$1</i>");
         content = content.replaceAll("<span class=\"jt_underline\">(.*?)</span>", "<u>$1</u>");
        content = content.replaceAll("<span class=\"jt_strike\">(.*?)</span>", "<del>1</del>");
 		return Html.fromHtml(content);
 	}
 
 	class ThreadAdapter extends ArrayAdapter<Thread> {
 		
 		private ArrayList<Thread> items;
 		
 		public ThreadAdapter(Context context, int textViewResourceId, ArrayList<Thread> items)
 		{
 			super(context, textViewResourceId, items);
 			this.items = items;
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent)
 		{
 			View v = convertView;
 			if (v == null)
 			{
 				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.thread_row, null);
 			}
 			
 			// get the thread to display and populate all the data into the layout
 			Thread t = items.get(position);
 			if (t != null)
 			{
 				TextView tvContent = (TextView)v.findViewById(R.id.textPreview);
 				if (tvContent != null)
 				{
 					tvContent.setPadding(15 * t.getLevel(), 0, 0, 0);
 					tvContent.setText(fixContent(t.getContent()));
 				}
 			}
 			
 			
 			return v;
 		}
 		private Spanned fixContent(String content)
 		{
 			// convert shack's css into real font colors since Html.fromHtml doesn't supporty css of any kind
 			content = content.replaceAll("<span class=\"jt_red\">(.*?)</span>", "<font color=\"#ff0000\">$1</font>");
 			content = content.replaceAll("<span class=\"jt_green\">(.*?)</span>", "<font color=\"#8dc63f\">$1</font>");
 			content = content.replaceAll("<span class=\"jt_pink\">(.*?)</span>", "<font color=\"#f49ac1\">$1</font>");
 	        content = content.replaceAll("<span class=\"jt_olive\">(.*?)</span>", "<font color=\"#808000\">$1</font>");
 	        content = content.replaceAll("<span class=\"jt_fuchsia\">(.*?)</span>", "<font color=\"#c0ffc0\">$1</font>");
 	        content = content.replaceAll("<span class=\"jt_yellow\">(.*?)</span>", "<font color=\"#ffde00\">$1</font>");
 	        content = content.replaceAll("<span class=\"jt_blue\">(.*?)</span>", "<font color=\"#44aedf\">$1</font>");
 	        content = content.replaceAll("<span class=\"jt_lime\">(.*?)</span>",  "<font color=\"#c0ffc0\">$1</font>");
 	        content = content.replaceAll("<span class=\"jt_orange\">(.*?)</span>", "<font color=\"#f7941c\">$1</font>");
 	        content = content.replaceAll("<span class=\"jt_bold\">(.*?)</span>", "<b>$1</b>");
 	        content = content.replaceAll("<span class=\"jt_italic\">(.*?)</span>", "<i>$1</i>");
 	        content = content.replaceAll("<span class=\"jt_underline\">(.*?)</span>", "<u>$1</u>");
	        content = content.replaceAll("<span class=\"jt_strike\">(.*?)</span>", "<del>1</del>");
 	        content = content.replaceAll("<br />", "&nbsp;");
 			return Html.fromHtml(content);
 		}
 	}
 }
