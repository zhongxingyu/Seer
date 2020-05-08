 package cc.hughes.droidchatty;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
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
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class SingleThreadView extends ListActivity {
 
     static final String THREAD_ID = "THREAD_ID";
     static final String THREAD_CONTENT = "THREAD_CONTENT";
     static final String THREAD_AUTHOR = "THREAD_AUTHOR";
     static final String THREAD_POSTED = "THREAD_POSTED";
     
     static final int POST_REPLY = 1;
 
     private int _currentThreadId = 0;
     private int _rootThreadId;
     private ProgressDialog _progressDialog = null;
     private ArrayList<Post> _posts = null;
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
                 for (Post t : _posts)
                 {
                     _adapter.add(t);
                 }
                 _progressDialog.dismiss();
                 _adapter.notifyDataSetChanged();
 
                 if (_currentThreadId == 0)
                 {
                     int index = findPostIndex(_posts, _rootThreadId);
                     displayPost(_posts.get(index), index);
                 }
             }
         }
         
         private int findPostIndex(ArrayList<Post> posts, int threadId)
         {
             int len = posts.size();
             for (int i = 0; i < len; i++)
             {
                 if (posts.get(i).getPostId() == threadId)
                     return i;
             }
             
             // uh, what?
             Log.w("DroidChatty", "Couldn't find post matching thread id #" + threadId);
             return 0;
         }
         
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.thread_view);
         
         // setup the lists and adapters
         _posts = getSavedPosts();
         _adapter = new ThreadAdapter(this, R.layout.thread_row, _posts);
         setListAdapter(_adapter);
         
         // setup listening for clicks
         final ListView lv = getListView();
         lv.setOnItemClickListener(new OnItemClickListener()
         {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id)
             {
                 displayPost(_posts.get(position), position);
             }
         });
 
         // find out why this thing was launched and load the required data
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
         String action = intent.getAction();
         Uri uri = intent.getData();
         
         if (extras != null) // launched from another activity, load all the info
         {
             _rootThreadId = extras.getInt(THREAD_ID);
             
             // if there is no content don't display anything at first, just refresh the data
             if (extras.containsKey(THREAD_CONTENT))
             {
                 String content = extras.getString(THREAD_CONTENT);
                 String author = extras.getString(THREAD_AUTHOR);
                 String posted = extras.getString(THREAD_POSTED);
     
                 Post post = new Post(_rootThreadId, author, content, posted, 0);
                 displayPost(post, 0);
             }
         }
         else if (action != null && action.equals(Intent.ACTION_VIEW) && uri != null) // launched from URI
         {
             String id = uri.getQueryParameter("id");
             if (id == null)
             {
                 ErrorDialog.display(this, "Error", "Invalid URL found.");
                 finish();
                 return;
             }
             
             _currentThreadId = 0;
             _rootThreadId = Integer.parseInt(id);
         }
         
         TextView tvContent = (TextView)findViewById(R.id.textContent);
         tvContent.setMovementMethod(LinkMovementMethod.getInstance());
         registerForContextMenu(tvContent);
         
         // if we don't already have the list of posts, fetch them now
         if (_posts.isEmpty())
             startRefresh();
     }
     
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo)
     {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.post_context, menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
             case R.id.copyUrl:
                 copyCurrentPostUrl();
                 return true;
             default:
                 return super.onContextItemSelected(item);
         }
     }
 
     @SuppressWarnings("unchecked")
     private ArrayList<Post> getSavedPosts()
     {
         final Object data = getLastNonConfigurationInstance();
 
         if (data == null)
             return new ArrayList<Post>();
         return (ArrayList<Post>)data;
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
             case R.id.reply:
             	postReply();
             	return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     private void postReply()
     {
         Intent i = new Intent(this, ComposePostView.class);
         i.putExtra(SingleThreadView.THREAD_ID, _currentThreadId);
         startActivityForResult(i, POST_REPLY);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         switch (requestCode)
         {
             case POST_REPLY:
                 if (resultCode == RESULT_OK)
                 {
                     // read the resulting thread id from the post
                     int postId = data.getExtras().getInt(THREAD_ID);
                     
                     // reset the current thread, force root post to be our new post
                     // and re-pull this thread
                     _currentThreadId = 0;
                     _rootThreadId = postId;
                     startRefresh();
                 }
                 break;
             default:
                 break;
         }
     }
 
     private void copyCurrentPostUrl()
     {
         String url = "http://www.shacknews.com/chatty?id=" + _currentThreadId;
         ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
         clipboard.setText(url);
     }
 
     private void displayPost(Post post, int position)
     {
         // unhighlight old selected, highlight new selected
         // should be able to do this with a selector, but damned if I could get it to work
         int previousPosition = _adapter.getSelectedPosition();
         ListView lv = getListView();
         _adapter.setSelectedPosition(position);
         _adapter.fixBackgroundColor(lv, previousPosition);
         _adapter.fixBackgroundColor(lv, position);
         
         TextView tvAuthor = (TextView)findViewById(R.id.textUserName);
         TextView tvContent = (TextView)findViewById(R.id.textContent);
         TextView tvPosted = (TextView)findViewById(R.id.textPostedTime);
 
         tvAuthor.setText(post.getUserName());
         tvAuthor.setTextColor(User.getColor(post.getUserName()));
         tvPosted.setText(post.getPosted());
         tvContent.setText(PostFormatter.formatContent(post, tvContent, true));
         
         // if this is the first time loaded, make sure the post being displayed is visible
         if (_currentThreadId == 0)
             lv.setSelection(position);
 
         _currentThreadId = post.getPostId();
     }
 
     @Override
     public Object onRetainNonConfigurationInstance()
     {
         return _posts;
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
             _posts = ShackApi.getPosts(_rootThreadId);
         } catch (Exception ex)
         {
            Log.e("DroidChatty", "Error fetching posts", ex);
             _progressDialog.dismiss();
             runOnUiThread(new ErrorDialog(this, "Error", "Error fetching posts."));
             return;
         }
 
         runOnUiThread(_displayThreads);
     }
 
     class ThreadAdapter extends ArrayAdapter<Post> {
 
         private ArrayList<Post> items;
         private int selectedPosition;
 
         public int getSelectedPosition()
         {
             return selectedPosition;
         }
 
         public void setSelectedPosition(int position)
         {
             selectedPosition = position;
         }
 
         public ThreadAdapter(Context context, int textViewResourceId, ArrayList<Post> items)
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
             Post t = items.get(position);
             if (t != null)
             {
                 TextView tvContent = (TextView)v.findViewById(R.id.textPreview);
                 if (tvContent != null)
                 {
                     tvContent.setPadding(15 * t.getLevel(), 0, 0, 0);
                     tvContent.setText(t.getPreview());
                 }
 
                 fixBackgroundColor(v, position);
             }
 
             return v;
         }
 
         public void fixBackgroundColor(ListView view, int position)
         {
             View v = view.getChildAt(position - view.getFirstVisiblePosition());
             if (v != null)
                 fixBackgroundColor(v, position);
         }
 
         public void fixBackgroundColor(View view, int position)
         {
             RelativeLayout preview = (RelativeLayout)view.findViewById(R.id.previewLayout);
             if (position == selectedPosition)
                 preview.setBackgroundColor(Color.rgb(0x22, 0x55, 0xdd));
             else
                 preview.setBackgroundColor(Color.TRANSPARENT);
         }
 
     }
 }
