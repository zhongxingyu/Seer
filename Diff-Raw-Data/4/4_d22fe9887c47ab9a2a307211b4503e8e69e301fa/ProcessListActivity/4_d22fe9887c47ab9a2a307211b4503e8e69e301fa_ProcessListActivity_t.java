 package com.tstine.marvinas;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.support.v4.app.ListFragment;
 import android.support.v7.app.ActionBarActivity;
 import android.support.v7.app.ActionBar;
 import android.support.v4.app.Fragment;
 import android.content.Context;
 import android.os.Bundle;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ProcessListActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {
 
     /**
      * The serialization (saved instance state) Bundle key representing the
      * current dropdown position.
      */
     private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
     private List<Request> mData = new ArrayList<Request>();
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_process_list);
 
         // Set up the action bar to show a dropdown list.
         final ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
         // Set up the dropdown list navigation in the action bar.
         actionBar.setListNavigationCallbacks(
             // Specify a SpinnerAdapter to populate the dropdown list.
             new ArrayAdapter<String>(
                 actionBar.getThemedContext(),
                 android.R.layout.simple_list_item_1,
                 android.R.id.text1,
                 new String[] {
                     getString(R.string.title_section1),
                 }),
             this);
         mData.add(0, (Request)getIntent().getSerializableExtra(Const.REQUEST_EXTRA_KEY));
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         // Restore the previously serialized current dropdown position.
         if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
             getSupportActionBar().setSelectedNavigationItem(
                 savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         // Serialize the current dropdown position.
         outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
             getSupportActionBar().getSelectedNavigationIndex());
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.process_list, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle action bar item clicks here. The action bar will
         // automatically handle clicks on the Home/Up button, so long
         // as you specify a parent activity in AndroidManifest.xml.
         int id = item.getItemId();
         if (id == R.id.action_settings) {
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public boolean onNavigationItemSelected(int position, long id) {
         // When the given dropdown item is selected, show its contents in the
         // container view.
         ListFragment frag = ProcessListFragment.newInstance(position+1, mData);
         getSupportFragmentManager().beginTransaction()
             .replace(R.id.container, frag )
             .commit();
         return true;
     }
 
     /**
      * A placeholder fragment containing a simple view.
      */
     public static class ProcessListFragment extends ListFragment {
         /**
          * The fragment argument representing the section number for this
          * fragment.
          */
         private static final String ARG_SECTION_NUMBER = "section_number";
         private List<Request> mData = null;
 
         /**
          * Returns a new instance of this fragment for the given section
          * number.
          */
         public static ProcessListFragment newInstance(int sectionNumber, List<Request> data) {
             ProcessListFragment fragment = new ProcessListFragment(data);
             Bundle args = new Bundle();
             args.putInt(ARG_SECTION_NUMBER, sectionNumber);
             fragment.setArguments(args);
             return fragment;
         }
 
         public ProcessListFragment(List<Request> data ) {
             this.mData = data;
         }
 
         public void onActivityCreated(Bundle savedInstanceState){
             super.onActivityCreated(savedInstanceState);
             setListAdapter(new MyAdapter(getActivity(), this.mData));
             this.setListShown(true);
         }
 
     }
     public static class MyAdapter extends BaseAdapter{
         private List<Request> mData = null;
         private Context mCtx;
         public MyAdapter( Context ctx, List<Request> data ){
             super();
             mData = data;
             mCtx = ctx;
         }
         @Override
         public int getCount(){return mData.size();}
         @Override
         public Request getItem( int position ){return mData.get(position);}
 
         @Override
         public long getItemId(int position) {
             return 0;
         }
         @Override
         public View getView(int position, View convertView,
                             ViewGroup parent ){
             if( convertView != null )
                 return convertView;
 
             Request request = getItem(position);
             LayoutInflater inflater =
                 (LayoutInflater)mCtx.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
             View row = inflater.inflate(R.layout.request_row, null);
             ImageView img = (ImageView) row.findViewById( R.id.image);
             Bitmap bmp = BitmapFactory.decodeFile(request.getImagePath());
             if( bmp == null)
                Log.d(Const.TAG, "bmp is null");
             img.setImageBitmap(BitmapFactory.decodeFile(request.getImagePath()));
 
             //img.setImageResource(R.drawable.ic_launcher);
             TextView message = (TextView) row.findViewById( R.id.message );
             message.setText( request.getMessage() );
             TextView time = (TextView) row.findViewById(R.id.date);
             time.setText(request.getTimestamp("EEE MMM F yyyy h:m:s a"));
             return row;
         }
 
     }
 }
