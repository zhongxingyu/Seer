 package de.bennir.DVBViewerController;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxStatus;
 import de.bennir.DVBViewerController.channels.DVBChannel;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.URLDecoder;
 import java.util.ArrayList;
 
 public class ChannelFragment extends SherlockListFragment {
     private static final String TAG = ChannelFragment.class.toString();
     static ListView lv;
     static ChanGroupAdapter lvAdapter;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         Log.d(TAG, "onCreateView");
         return inflater.inflate(R.layout.channel_fragment, container, false);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         Log.d(TAG, "onActivityCreated Size: " + DVBViewerControllerActivity.DVBChannels.size());
         super.onActivityCreated(savedInstanceState);
        ((DVBViewerControllerActivity) getSherlockActivity()).mContent = this;
 
         setHasOptionsMenu(true);
         getSherlockActivity().getSupportActionBar().setTitle(R.string.channels);
 
         lv = getListView();
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 if (getActivity() instanceof DVBViewerControllerActivity) {
                     DVBViewerControllerActivity.currentGroup = i;
                     DVBViewerControllerActivity act = (DVBViewerControllerActivity) getActivity();
                     act.switchContent(new ChannelGroupFragment(), DVBViewerControllerActivity.groupNames.get(i), R.drawable.ic_action_channels, true);
                 }
             }
         });
 
         lvAdapter = new ChanGroupAdapter(
                 getSherlockActivity(),
                 DVBViewerControllerActivity.groupNames.toArray(new String[DVBViewerControllerActivity.groupNames.size()])
         );
 
         addChannelsToListView();
     }
 
     public static void addChannelsToListView() {
         lvAdapter.notifyDataSetChanged();
         lv.setAdapter(lvAdapter);
         lv.invalidate();
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         MenuItem item = menu.add(2, 2, 1, R.string.refresh);
         item.setIcon(R.drawable.ic_action_refresh);
         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 ((DVBViewerControllerActivity)getSherlockActivity()).updateChannelList();
                 addChannelsToListView();
 
                 return true;
             }
         });
     }
 
     public class ChanGroupAdapter extends ArrayAdapter<String> {
         private final Context context;
         private final String[] values;
 
         public ChanGroupAdapter(Context context, String[] values) {
             super(context, R.layout.channels_group_list_item, values);
             this.context = context;
             this.values = values;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             LayoutInflater inflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
             View v = null;
 
             if (convertView != null)
                 v = convertView;
             else
                 v = inflater.inflate(R.layout.channels_group_list_item, parent,
                         false);
 
             TextView chanGroup = (TextView) v
                     .findViewById(R.id.channels_group_list_item);
             chanGroup.setTypeface(((DVBViewerControllerActivity) getActivity()).robotoCondensed);
 
             chanGroup.setText(values[position]);
 
             return v;
         }
     }
 }
