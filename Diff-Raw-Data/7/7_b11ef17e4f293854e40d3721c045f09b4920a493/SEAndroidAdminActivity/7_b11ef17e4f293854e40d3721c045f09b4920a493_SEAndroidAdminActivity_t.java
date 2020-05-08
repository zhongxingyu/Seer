 
 package com.android.seandroid_admin;
 
 import android.content.Context;
 import android.content.Intent;
 import android.preference.PreferenceActivity;
 import android.os.Bundle;
 import android.os.SELinux;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CompoundButton;
 import android.widget.ListAdapter;
 import android.widget.Switch;
 import android.widget.TextView;
 
 import com.android.seandroid_admin.R;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SEAndroidAdminActivity extends PreferenceActivity {
     private static final String TAG = "SEAdminActivity";
     static final boolean TRACE_LIFECYCLE = false;
     static final boolean TRACE_UPDATE = SEAndroidAdmin.TRACE_UPDATE;
 
     private List<Header> mHeaders;
 
     SEAndroidAdmin mAdmin;
 
     private View mDeviceAdminView;
     private View mSELinuxAdminView;
     private View mMMACadminView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         if (TRACE_LIFECYCLE) { Log.v(TAG, "LIFECYCLE ACTIVITY onCreate()"); }
         super.onCreate(savedInstanceState);
         mAdmin = new SEAndroidAdmin(this);
     }
 
     @Override
     public void onBuildHeaders(List<Header> headers) {
         if (TRACE_LIFECYCLE) { Log.v(TAG, "LIFECYCLE ACTIVITY onBuildHeaders()"); }
         if (!SELinux.isSELinuxEnabled()) {
             // TODO Match with rest of theme
             Log.v(TAG, "SELinux disabled");
             loadHeadersFromResource(R.xml.disabled_headers, headers);
             return;
         }
 
         loadHeadersFromResource(R.xml.enabled_headers, headers);
         updateHeaderList(headers);
     }
 
     private void updateHeaderList(List<Header> target) {
         //TODO maybe enable or disable headers here based on whether were device admin?
     }
 
    @Override
    public PreferenceActivity.Header onGetInitialHeader() {
        Header h = new PreferenceActivity.Header();
        h.fragment = SELinuxEnforcingFragment.class.getCanonicalName();
        return h;
    }

     private static class HeaderViewHolder {
         TextView title;
         Switch switch_;
         TextView summary;
     }
 
     private static class HeaderAdapter extends ArrayAdapter<Header> {
         static final int HEADER_TYPE_CATEGORY = 0;
         static final int HEADER_TYPE_NORMAL = 1;
         static final int HEADER_TYPE_SWITCH = 2;
         private static final int HEADER_TYPE_COUNT = HEADER_TYPE_SWITCH + 1;
 
         private SEAndroidAdminActivity mActivity;
         private LayoutInflater mInflater;
 
         static int getHeaderType(Header header) {
             int id = (int) header.id; // ids are integers, so downcast is okay
             switch (id) {
                 //case ???:
                 //    return HEADER_TYPE_CATEGORY;
                 case R.id.enable_device_admin:
                 case R.id.selinux_admin:
                 case R.id.mmac_admin:
                     return HEADER_TYPE_SWITCH;
                 default:
                     return HEADER_TYPE_NORMAL;
             }
         }
 
         @Override
         public int getItemViewType(int position) {
             Header header = getItem(position);
             return getHeaderType(header);
         }
 
         @Override
         public boolean areAllItemsEnabled() {
             return false; // because of categories
             //return SELinux.isSELinuxEnabled();
         }
 
         @Override
         public boolean isEnabled(int position) {
             return getItemViewType(position) != HEADER_TYPE_CATEGORY;
         }
 
         @Override
         public int getViewTypeCount() {
             return HEADER_TYPE_COUNT;
         }
 
         @Override
         public boolean hasStableIds() {
             return true;
         }
 
         public HeaderAdapter(Context context, List<Header> objects) {
             super(context, 0, objects);
             mActivity = (SEAndroidAdminActivity) context;
             mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             HeaderViewHolder holder;
             final Header header = getItem(position);
             int headerType = getHeaderType(header);
             View view = null;
 
             if (convertView == null) {
                 // New view, so start inflating views
                 holder = new HeaderViewHolder();
                 switch (headerType) {
                     case HEADER_TYPE_CATEGORY:
                         view = new TextView(getContext(), null,
                                 android.R.attr.listSeparatorTextViewStyle);
                         holder.title = (TextView) view;
                         break;
 
                     case HEADER_TYPE_SWITCH:
                         view = mInflater.inflate(R.layout.preference_header_switch_item,
                                 parent, false);
                         holder.title = (TextView) view.findViewById(
                                 com.android.internal.R.id.title);
                         holder.summary = (TextView) view.findViewById(
                                 com.android.internal.R.id.summary);
                         holder.switch_ = (Switch) view.findViewById(R.id.switchWidget);
                         break;
 
                     case HEADER_TYPE_NORMAL:
                         view = mInflater.inflate(R.layout.preference_header_item, parent,
                                 false);
                         holder.title = (TextView) view.findViewById(
                                 com.android.internal.R.id.title);
                         holder.summary = (TextView) view.findViewById(
                                 com.android.internal.R.id.summary);
                         break;
                 }
 
                 view.setTag(holder);
             } else {
                 view = convertView;
                 holder = (HeaderViewHolder) view.getTag();
             }
 
             // All view fields must be updated every time, because the view may be recycled
             switch (headerType) {
                 case HEADER_TYPE_CATEGORY:
                     holder.title.setText(header.getTitle(getContext().getResources()));
                     Log.v(TAG, "Updated view for header CATEGORY " + holder.title.getText());
                     break;
 
                 case HEADER_TYPE_SWITCH:
                     holder.switch_.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                         @Override
                         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                             mActivity.updateSwitchAssignment(header, (Switch) buttonView);
                             SEAndroidAdmin mAdmin = mActivity.mAdmin;
 
                             if (header.id == R.id.enable_device_admin) {
                                 boolean adminActive = mAdmin.isDeviceAdmin;
                                 Log.v(TAG, "Clicked Device admin: " + adminActive + " -> " + isChecked);
                                 if (isChecked != adminActive) {
                                     if (isChecked) {
                                         mAdmin.enableAdmin();
                                         // Don't need to update views because onResume() fires
                                         // after coming back from approval screen.
                                     } else {
                                         mAdmin.removeAdmin();
                                         mActivity.setDeviceAdminView(false);
                                     }
                                 }
                                 return;
 
                             } else if (header.id == R.id.selinux_admin) {
                                 boolean adminActive = mAdmin.isSELinuxAdmin;
                                 Log.v(TAG, "Clicked SELinux admin: " + adminActive + " -> " + isChecked);
                                 if (isChecked != adminActive) {
                                     boolean ret = mAdmin.mDPM.setSELinuxAdmin(mAdmin.mDeviceAdmin, isChecked);
                                     // TODO show failure with toast or something
                                     mAdmin.updateSELinuxState();
                                     mActivity.updateSELinuxView();
                                 }
                                 return;
 
                             } else if (header.id == R.id.mmac_admin) {
                                 boolean adminActive = mAdmin.isMMACadmin;
                                 Log.v(TAG, "Clicked MMAC admin: " + adminActive + " -> " + isChecked);
                                 if (isChecked != adminActive) {
                                     boolean ret = mAdmin.mDPM.setMMACadmin(mAdmin.mDeviceAdmin, isChecked);
                                     // TODO show failure  with toast or something
                                     mAdmin.updateMMACstate();
                                     mActivity.updateMMACview();
                                 }
                                 return;
                             }
                         }
                     });
                     // fallthrough to update common fields
 
                 case HEADER_TYPE_NORMAL:
                     holder.title.setText(header.getTitle(getContext().getResources()));
                     Log.v(TAG, "Updated view for header " + holder.title.getText());
                     CharSequence summary = header.getSummary(getContext().getResources());
                     if (!TextUtils.isEmpty(summary)) {
                         holder.summary.setVisibility(View.VISIBLE);
                         holder.summary.setText(summary);
                     } else {
                         holder.summary.setVisibility(View.GONE);
                     }
             }
             mActivity.updateViewAssignment(header, view);
 
             return view;
         }
 
     }
 
     @Override
     public void setListAdapter(ListAdapter adapter) {
         if (TRACE_LIFECYCLE) { Log.v(TAG, "LIFECYCLE ACTIVITY setListAdapter()"); }
         if (mHeaders == null) {
             mHeaders = new ArrayList<Header>();
             for (int i = 0; i < adapter.getCount(); i++) {
                 mHeaders.add((Header) adapter.getItem(i));
             }
         }
         super.setListAdapter(new HeaderAdapter(this, mHeaders));
     }
 
     /** Tells the activity which View is what feature and updates that view
      * with the current state. */
     private void updateViewAssignment(Header header, View view) {
         int id = (int) header.id; // ids are integers, so downcast is okay
         switch (id) {
             case R.id.enable_device_admin:
                 mDeviceAdminView = view;
                 updateDeviceAdminView();
                 break;
             case R.id.mmac_admin:
                 mMMACadminView = view;
                 updateMMACview();
                 break;
             case R.id.selinux_admin:
                 mSELinuxAdminView = view;
                 updateSELinuxView();
                 break;
         }
     }
 
     /** Tells the activity which switch belongs to what view. This was needed
      * because in the onCheckedChangeReceiver, the buttonView was different
      * from the Switch in the saved Views. Not sure why. */
     private void updateSwitchAssignment(Header header, Switch switch_) {
         int id = (int) header.id; // ids are integers, so downcast is okay
         switch (id) {
             case R.id.enable_device_admin:
                 ((HeaderViewHolder) mDeviceAdminView.getTag()).switch_ = switch_;
                 break;
             case R.id.selinux_admin:
                 ((HeaderViewHolder) mSELinuxAdminView.getTag()).switch_ = switch_;
                 break;
             case R.id.mmac_admin:
                 ((HeaderViewHolder) mMMACadminView.getTag()).switch_ = switch_;
                 break;
         }
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (TRACE_LIFECYCLE) { Log.v(TAG, "LIFECYCLE ACTIVITY onActivityResult()"); }
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == SEAndroidAdmin.REQUEST_CODE_ENABLE_ADMIN) {
             mAdmin.updateState();
         }
     }
 
     @Override
     public void onResume() {
         if (TRACE_LIFECYCLE) { Log.v(TAG, "LIFECYCLE ACTIVITY onResume()"); }
         super.onResume();
         mAdmin.updateState();
         updateDeviceAdminView();
         updateMMACview();
         updateSELinuxView();
     }
 
     /**
      * Reset the SEAndroidAdmin's device admin state to reflect adminState and
      * updates the UI.
      * 
      * Read {@link SEAndroidAdmin#setAdminState(boolean)} for more usage
      * information before using this.
      */
     private void setDeviceAdminView(boolean adminState) {
         if (SEAndroidAdmin.TRACE_UPDATE) { Log.v(TAG, "UPDATE ACTIVITY setDeviceAdminView("+adminState+")"); }
         //mAdmin.setAdminState(adminState);
 
         Switch switch_ = ((HeaderViewHolder) mDeviceAdminView.getTag()).switch_;
         switch_.setChecked(mAdmin.isDeviceAdmin);
         updateSELinuxView();
         updateMMACview();
     }
 
     /** Updates the Device Admin view to reflect current device state.
      * If you want truly current state, ask SEAndroidAdmin to update
      * the state first. */
     private void updateDeviceAdminView() {
         if (SEAndroidAdmin.TRACE_UPDATE) { Log.v(TAG, "UPDATE ACTIVITY updateDeviceAdminView()"); }
         //mAdmin.updateDeviceAdminState();
 
         if (mDeviceAdminView != null) { // else, nothing to update yet
             Switch switch_ = ((HeaderViewHolder) mDeviceAdminView.getTag()).switch_;
             switch_.setChecked(mAdmin.isDeviceAdmin);
         }
     }
 
     /** Updates the SELinux view to reflect the current device state.
      * If you want truly current state, ask SEAndroidAdmin to update
      * the state first. */
     private void updateSELinuxView() {
         if (SEAndroidAdmin.TRACE_UPDATE) { Log.v(TAG, "UPDATE ACTIVITY updateSELinuxView()"); }
         //mAdmin.updateSELinuxState();
 
         if (mSELinuxAdminView != null) { // else, nothing to update yet
             Switch switch_ = ((HeaderViewHolder) mSELinuxAdminView.getTag()).switch_;
             switch_.setEnabled(mAdmin.isDeviceAdmin);
             switch_.setChecked(mAdmin.isSELinuxAdmin);
         }
     }
 
     /** Updates the MMAC view to reflect the current device state.
      * If you want truly current state, ask SEAndroidAdmin to update
      * the state first. */
     private void updateMMACview() {
         if (SEAndroidAdmin.TRACE_UPDATE) { Log.v(TAG, "UPDATE ACTIVITY updateMMACview()"); }
         //mAdmin.updateMMACstate();
 
         if (mMMACadminView != null) { // else, nothing to update yet
             Switch switch_ = ((HeaderViewHolder) mMMACadminView.getTag()).switch_;
             switch_.setEnabled(mAdmin.isDeviceAdmin);
             switch_.setChecked(mAdmin.isMMACadmin);
         }
     }
 }
