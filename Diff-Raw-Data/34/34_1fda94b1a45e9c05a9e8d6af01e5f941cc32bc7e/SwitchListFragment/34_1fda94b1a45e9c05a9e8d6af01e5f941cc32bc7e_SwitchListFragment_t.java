 package com.yoctopuce.examples.ysmsrelay;
 
 import android.app.ListFragment;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.CompoundButton;
 import android.widget.ListView;
 import android.widget.Switch;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 
 /**
  * Created by seb on 19.09.13.
  */
 public class SwitchListFragment extends ListFragment {
 
     private static final String TAG = "SwitchListFragment";
     private ArrayList<YSwitch> mYSwitches;
 
     private BroadcastReceiver mYoctoReceiver = new YoctoUpdateReceiver() {
 
         @Override
         protected void newRelayList(Context context, String[] relayList) {
             ((SwitchAdapter)getListAdapter()).notifyDataSetChanged();
         }
 
         @Override
         protected void onSwitchChange(Context context, YSwitch s) {
             Log.w(TAG, "relay " + s.getHardwareId() + " changed ");
             ((SwitchAdapter)getListAdapter()).notifyDataSetChanged();
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
         mYSwitches = SwitchStore.get(getActivity()).getYSwitches();
         SwitchAdapter switchAdapter = new SwitchAdapter(mYSwitches);
         setListAdapter(switchAdapter);
     }
 
 
 
    @Override
     public void onStart() {
         super.onStart();
         getActivity().registerReceiver(mYoctoReceiver,
                 new IntentFilter(YoctoUpdateReceiver.ACTION_RESP));
         YoctoService.startBgService(getActivity());
     }
    
    
     @Override
	public void onResume() {
		super.onResume();
        YoctoService.requestRefresh(getActivity());
	}



	@Override
     public void onStop() {
         YoctoService.stopBgService(getActivity());
         getActivity().unregisterReceiver(mYoctoReceiver);
         super.onStop();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = super.onCreateView(inflater, container, savedInstanceState);
         View listView = view.findViewById(android.R.id.list);
         registerForContextMenu(listView);
         return view;
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         YSwitch ySwitch = (YSwitch) getListAdapter().getItem(position);
         Intent intent = new Intent(getActivity(),SwitchActivitiy.class);
         intent.putExtra(SwitchFragment.EXTRA_SWITCH_ID, ySwitch.getUUID());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivityForResult(intent,0);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         inflater.inflate(R.menu.switch_list_option_menu,menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_item_new_switch:
                 YSwitch s = new YSwitch();
                 SwitchStore.get(getActivity()).addSwitch(s);
                 Intent intent = new Intent(getActivity(),SwitchActivitiy.class);
                 intent.putExtra(SwitchFragment.EXTRA_SWITCH_ID, s.getUUID());
                 startActivityForResult(intent, 0);
                 return true;
             default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         getActivity().getMenuInflater().inflate(R.menu.switch_list_context_menu,menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case  R.id.menu_item_delete_switch:
                 AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                 int position = info.position;
 
                 SwitchAdapter switchAdapter = (SwitchAdapter) getListAdapter();
                 YSwitch ySwitch = (YSwitch) switchAdapter.getItem(position);
                 SwitchStore.get(getActivity()).deleteSwitch(ySwitch);
                 switchAdapter.notifyDataSetChanged();
             default:
                 return super.onContextItemSelected(item);
         }
 
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         ((SwitchAdapter)getListAdapter()).notifyDataSetChanged();
     }
     private class SwitchAdapter extends ArrayAdapter<YSwitch>
     {
 
         public SwitchAdapter(ArrayList<YSwitch> switches) {
             super(getActivity(),android.R.layout.simple_list_item_1, switches);
         }
 
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if(convertView== null){
                 convertView = getActivity().getLayoutInflater()
                         .inflate(R.layout.switch_list_item, null);
             }
             final YSwitch s = getItem(position);
 
             TextView name = (TextView) convertView.findViewById(R.id.switch_list_item_name);
             name.setText(s.getName());
             Switch onoff = (Switch) convertView.findViewById(R.id.switch_list_item_onoff);
             onoff.setEnabled(s.isOnline());
             onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     s.setOn(getActivity(),isChecked);
                 }
             });
             boolean on = s.isOn();
             onoff.setChecked(on);
             return convertView;
         }
 
 
     }
 
 
 }
