 package cc.hughes.droidchatty;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.List;
 
 import android.R.color;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.style.ForegroundColorSpan;
 import android.util.Log;
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
 
 public class ThreadView extends ListActivity {
 
     static final int POST_THREAD = 1;
     
     private ProgressDialog _progressDialog = null;
     private ArrayList<Thread> _threads = null;
     private ThreadAdapter _adapter;
 
     private Runnable _retrieveThreads = new Runnable()
     {
         public void run()
         {
             getThreads();
         }
     };
 
     private Runnable _displayThreads = new Runnable()
     {
         public void run()
         {
             if (_threads != null && _threads.size() > 0)
             {
                 // remove the elements, and add in all the new ones
                 _adapter.clear();
                 for (Thread t : _threads)
                 {
                     _adapter.add(t);
                 }
                 _progressDialog.dismiss();
                 _adapter.notifyDataSetChanged();
             }
         }
     };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         // setup the list
         _threads = getSavedThreads();
         _adapter = new ThreadAdapter(this, R.layout.row, _threads);
         setListAdapter(_adapter);
 
         // listen for clicks
         ListView lv = getListView();
         lv.setOnItemClickListener(new OnItemClickListener()
         {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id)
             {
                 displayThread(_threads.get(position));
             }
         });
 
         // let's get this thing going already!
         if (_threads.isEmpty())
             startRefresh();
     }
 
     @SuppressWarnings("unchecked")
     private ArrayList<Thread> getSavedThreads()
     {
         final Object data = getLastNonConfigurationInstance();
         if (data == null)
             return new ArrayList<Thread>();
         return (ArrayList<Thread>)data;
     }
 
     public Object onRetainNonConfigurationInstance()
     {
         return _threads;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
             case R.id.refresh:
                 startRefresh();
                 return true;
             case R.id.add:
                 post();
                 return true;
             case R.id.settings:
                 showSettings();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void post()
     {
         Intent i = new Intent(this, ComposePostView.class);
         startActivityForResult(i, POST_THREAD);
     }
     
     private void showSettings()
     {
         Intent i = new Intent(this, PreferenceView.class);
         startActivity(i);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         switch (requestCode)
         {
             case POST_THREAD:
                 if (resultCode == RESULT_OK)
                 {
                     // read the resulting thread id from the post
                     int postId = data.getExtras().getInt(SingleThreadView.THREAD_ID);
                     
                     // hey, thats the same thing I just wrote!
                     Intent i = new Intent(this, SingleThreadView.class);
                     i.putExtra(SingleThreadView.THREAD_ID, postId);
                     startActivity(i);
                 }
                 break;
             default:
                 break;
         }
     }
 
     private void startRefresh()
     {
         java.lang.Thread thread = new java.lang.Thread(null, _retrieveThreads, "Backgroundl");
         thread.start();
         _progressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving threads...", true, true);
     }
 
     private void displayThread(Thread thread)
     {
         Intent i = new Intent(this, SingleThreadView.class);
         i.putExtra(SingleThreadView.THREAD_ID, thread.getThreadId());
         i.putExtra(SingleThreadView.THREAD_AUTHOR, thread.getUserName());
         i.putExtra(SingleThreadView.THREAD_POSTED, thread.getPosted());
         i.putExtra(SingleThreadView.THREAD_CONTENT, thread.getContent());
         startActivity(i);
     }
 
     private void getThreads()
     {
         try
         {
             _threads = ShackApi.getThreads();
 
             // set the number of replies that are new
             Hashtable<Integer, Integer> counts = getPostCounts();
             for (Thread t : _threads)
             {
                 if (counts.containsKey(t.getThreadId()))
                     t.setReplyCountPrevious(counts.get(t.getThreadId()));
             }
 
             storePostCounts(counts, _threads);
         } catch (Exception ex)
         {
            Log.e("DroidChatty", ex.getMessage());
             _progressDialog.dismiss();
             runOnUiThread(new ErrorDialog(this, "Error", "Error fetching threads."));
             return;
         }
 
         runOnUiThread(_displayThreads);
     }
 
     private Hashtable<Integer, Integer> getPostCounts()
     {
         Hashtable<Integer, Integer> counts = new Hashtable<Integer, Integer>();
 
         if (getFileStreamPath("post_count.cache").exists())
         {
             // look at that, we got a file
             try {
                 FileInputStream input = openFileInput("post_count.cache");
                 try
                 {
                     DataInputStream in = new DataInputStream(input);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                     String line = reader.readLine();
                     while (line != null)
                     {
                         if (line.length() > 0)
                         {
                             String[] parts = line.split("=");
                             counts.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                         }
                         line = reader.readLine();
                     }
                 }
                 finally
                 {
                     input.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         return counts;
     }
 
     final static int POST_CACHE_HISTORY = 1000;
     private void storePostCounts(Hashtable<Integer, Integer> counts, ArrayList<Thread> threads) throws IOException
     {
         // update post counts for threads viewing right now
         for (Thread t : threads)
             counts.put(t.getThreadId(), t.getReplyCount());
 
         List<Integer> postIds = Collections.list(counts.keys());
         Collections.sort(postIds);
 
         // trim to last 1000 posts
         if (postIds.size() > POST_CACHE_HISTORY)
             postIds.subList(postIds.size() - POST_CACHE_HISTORY, postIds.size() - 1);
 
         FileOutputStream output = openFileOutput("post_count.cache", MODE_PRIVATE);
         try
         {
             DataOutputStream out = new DataOutputStream(output);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
 
             for (Integer postId : postIds)
             {
                 writer.write(postId + "=" + counts.get(postId));
                 writer.newLine();
             }
             writer.flush();
         }
         finally
         {
             output.close();
         }
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
                 v = vi.inflate(R.layout.row, null);
             }
 
             // get the thread to display and populate all the data into the layout
             Thread t = items.get(position);
             if (t != null)
             {
                 TextView tvUserName = (TextView)v.findViewById(R.id.textUserName);
                 TextView tvContent = (TextView)v.findViewById(R.id.textContent);
                 TextView tvPosted = (TextView)v.findViewById(R.id.textPostedTime);
                 TextView tvReplyCount = (TextView)v.findViewById(R.id.textReplyCount);
                 if (tvUserName != null)
                     tvUserName.setText(t.getUserName());
                 if (tvContent != null)
                     tvContent.setText(t.getPreview());
                 if (tvPosted != null)
                     tvPosted.setText(t.getPosted());
                 if (tvReplyCount != null)
                     tvReplyCount.setText(formatReplyCount(t));
 
                 // special highlight for shacknews posts
                 if (t.getUserName().equalsIgnoreCase("Shacknews"))
                     v.setBackgroundColor(Color.rgb(0x19, 0x26, 0x35));
                 else
                     v.setBackgroundColor(color.background_dark);
 
                 // special highlight for employee and mod names
                 tvUserName.setTextColor(User.getColor(t.getUserName()));
             }
             return v;
         }
 
         private Spanned formatReplyCount(Thread thread)
         {
             String first = "(" + thread.getReplyCount();
             String second = "";
             String third = ")";
 
             if (thread.getReplyCount() > thread.getReplyCountPrevious())
             {
                 int new_replies = thread.getReplyCount() - thread.getReplyCountPrevious();
                 second = " +" + new_replies;
             }
 
             SpannableString formatted = new SpannableString(first + second + third);
             if (second.length() > 0)
                 formatted.setSpan(new ForegroundColorSpan(Color.GREEN), first.length(), first.length() + second.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             return formatted;
         }
     }
 }
