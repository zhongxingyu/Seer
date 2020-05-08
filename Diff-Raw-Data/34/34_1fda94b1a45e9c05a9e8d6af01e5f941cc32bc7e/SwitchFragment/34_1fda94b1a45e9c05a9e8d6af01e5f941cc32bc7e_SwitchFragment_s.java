 package com.yoctopuce.examples.ysmsrelay;
 
 import android.app.Fragment;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.Switch;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 /**
  * Created by seb on 19.09.13.
  */
 public class SwitchFragment extends Fragment {
 
 
     public static final String EXTRA_SWITCH_ID = "switchIntent.switchId";
     private YSwitch mYSwitch;
     private EditText mNameEditText;
     private Switch mInvertedSwitch;
     private AutoCompleteTextView mHardwareIdTextView;
     private EditText mPulseEditText;
     private ArrayList<String> mUsableRelay= new ArrayList<String>();
 
     private BroadcastReceiver mYoctoReceiver = new YoctoUpdateReceiver() {
         @Override
         protected void newRelayList(Context ctx, String[] relayList) {
             mUsableRelay.clear();
             for (String relay :relayList){
                 mUsableRelay.add(relay);
             }
             mTextViewAdapter.notifyDataSetChanged();
         }
     };
     private ArrayAdapter<String> mTextViewAdapter;
 
 
     public static SwitchFragment newInstance(UUID switchId) {
         Bundle bundle = new Bundle();
         bundle.putSerializable(EXTRA_SWITCH_ID,switchId);
         SwitchFragment switchFragment = new SwitchFragment();
         switchFragment.setArguments(bundle);
         return switchFragment;
     }
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         UUID uuid = (UUID) getArguments().getSerializable(EXTRA_SWITCH_ID);
         mYSwitch = SwitchStore.get(getActivity()).getSwitch(uuid);
         setHasOptionsMenu(true);
     }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 if (NavUtils.getParentActivityName(getActivity()) != null) {
                     NavUtils.navigateUpFromSameTask(getActivity());
                 }
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
 
         }
     }
 
     public void onStart() {
         super.onStart();
         getActivity().registerReceiver(mYoctoReceiver,
                 new IntentFilter(YoctoUpdateReceiver.ACTION_RESP));
         YoctoService.requestRelayList(getActivity());
     }
 
     @Override
     public void onPause() {
         super.onPause();
         SwitchStore.get(getActivity()).saveSwitches();
     }
 
     @Override
     public void onStop() {
         getActivity().unregisterReceiver(mYoctoReceiver);
         super.onStop();
     }
 
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.switch_fragment, container, false);
         getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
         mNameEditText = (EditText) view.findViewById(R.id.switch_name);
         mNameEditText.setText(mYSwitch.getName());
         mNameEditText.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 
             }
 
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 mYSwitch.setName(s.toString());
             }
 
             @Override
             public void afterTextChanged(Editable s) {
             }
         });
         mInvertedSwitch = (Switch) view.findViewById(R.id.switch_inverted);
         mInvertedSwitch.setChecked(mYSwitch.isInverted());
         mInvertedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 mYSwitch.setInverted(isChecked);
             }
         });
         mPulseEditText = (EditText) view.findViewById(R.id.switch_pulse);
         mPulseEditText.setText(Integer.toString(mYSwitch.getPulse()));
         mPulseEditText.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 
             }
 
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 mYSwitch.setPulse(Integer.parseInt(s.toString()));
             }
 
             @Override
             public void afterTextChanged(Editable s) {
 
             }
         });
         mHardwareIdTextView = (AutoCompleteTextView) view.findViewById(R.id.switch_hardwareid);
         mHardwareIdTextView.setText(mYSwitch.getHardwareId());
         mTextViewAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mUsableRelay);
         mHardwareIdTextView.setAdapter(mTextViewAdapter);
         mHardwareIdTextView.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 
             }
 
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 mYSwitch.setHardwareId(s.toString());
             }
 
             @Override
             public void afterTextChanged(Editable s) {
 
             }
 
         });
         return view;
     }
 
 }
