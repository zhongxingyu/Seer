 package com.jinheyu.lite_mms;
 
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.view.*;
 import android.widget.ArrayAdapter;
 import com.jinheyu.lite_mms.data_structures.Constants;
 import com.jinheyu.lite_mms.data_structures.Department;
 import com.jinheyu.lite_mms.data_structures.WorkCommand;
 import com.jinheyu.lite_mms.netutils.BadRequest;
 import org.json.JSONException;
 
 import java.io.IOException;
 import java.util.List;
 
 public class DepartmentLeaderActivity extends WorkCommandListActivity {
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_or_off_duty, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_logout:
                 new LogoutDialog(this).show();
                 break;
            case R.id.action_off_duty:
                break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected ArrayAdapter<Department> getArrayAdapter(int resource) {
         return new ArrayAdapter<Department>(this, resource, MyApp.getCurrentUser().getDepartmentList());
     }
 
     @Override
     protected FragmentPagerAdapter getFragmentPagerAdapter(int position) {
         return new DepartmentLeaderAdapter(getSupportFragmentManager(), MyApp.getCurrentUser().getDepartmentIds()[position]);
     }
 }
 
 class DepartmentLeaderAdapter extends FragmentPagerAdapter {
 
     private int[] statuses = new int[]{Constants.STATUS_ASSIGNING, Constants.STATUS_LOCKED};
     private int departmentId;
 
     public DepartmentLeaderAdapter(FragmentManager fragmentManager, int departmentId) {
         super(fragmentManager);
         this.departmentId = departmentId;
     }
 
     @Override
     public int getCount() {
         return statuses.length;
     }
 
     @Override
     public Fragment getItem(int position) {
         return DepartmentListWorkCommandListFragment.newInstance(departmentId, statuses[position]);
     }
 
     @Override
     public long getItemId(int position) {
         return departmentId * getCount() + position;
     }
 
     @Override
     public CharSequence getPageTitle(int position) {
         return String.format("状态 %s", WorkCommand.getStatusString(statuses[position]));
     }
 }
 
 class DepartmentListWorkCommandListFragment extends WorkCommandListFragment {
 
     public static DepartmentListWorkCommandListFragment newInstance(int departmentId, int status) {
         DepartmentListWorkCommandListFragment mFragment = new DepartmentListWorkCommandListFragment();
         Bundle args = new Bundle();
         args.putIntArray(WorkCommandListFragment.ARG_SECTION_NUMBER, new int[]{departmentId, status});
         mFragment.setArguments(args);
         return mFragment;
     }
 
     @Override
     protected ActionMode.Callback getActionModeCallback() {
         return new ActionMode.Callback() {
 
             @Override
             public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                 MenuItemWrapper wrapper = new MenuItemWrapper(getActivity(), mode);
                 switch (item.getItemId()) {
                     case R.id.action_dispatch:
                         wrapper.dispatch(getCheckedWorkCommandIds(), getSymbols()[WorkCommandListFragment.DEPARTMENT_ID_INDEX]);
                         break;
                     case R.id.action_refuse:
                         wrapper.refuse(getCheckedWorkCommandIds());
                         break;
                     case R.id.action_deny_retrieve:
                         wrapper.deny_retrieve(getCheckedWorkCommandIds());
                         break;
                 }
                 return true;
             }
 
             @Override
             public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                 mode.getMenuInflater().inflate(
                         getSymbols()[WorkCommandListFragment.STATUS_INDEX] == Constants.STATUS_LOCKED ?
                         R.menu.department_leader_deny_only : R.menu.department_leader_dispatch, menu);
                 mode.setTitle(getString(R.string.please_select));
                 return true;
             }
 
             @Override
             public void onDestroyActionMode(ActionMode mode) {
                 mActionMode = null;
                 clearAllCheckedItems();
             }
 
             @Override
             public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                 return false;
             }
 
         };
     }
 
     protected void loadWorkCommandList() {
         int[] symbols = getSymbols();
         new GetWorkCommandListTask(symbols[WorkCommandListFragment.DEPARTMENT_ID_INDEX], symbols[WorkCommandListFragment.STATUS_INDEX], this).execute();
     }
 
     class GetWorkCommandListTask extends AbstractGetWorkCommandListTask {
         private int departmentId;
         private int status;
 
         public GetWorkCommandListTask(int departmentId, int status, WorkCommandListFragment listFragment) {
             super(listFragment);
             this.departmentId = departmentId;
             this.status = status;
         }
 
         @Override
         protected List<WorkCommand> getWorkCommandList() throws IOException, JSONException, BadRequest {
             return MyApp.getWebServieHandler().getWorkCommandListByDepartmentId(departmentId, status);
         }
     }
 }
