 package com.scurab.gwt.rlw.client.dialog;
 
 import java.util.HashMap;
 
 import com.scurab.gwt.rlw.client.DataServiceAsync;
 import com.scurab.gwt.rlw.shared.SharedParams;
 
 public class DeviceFilterDialog extends FilterDialog {
 
     private DataServiceAsync mDataService;
     private DeviceFilterView mFilterView;
 
     public DeviceFilterDialog(String appName, DataServiceAsync service, OnOkListener listener) {
         super(appName, service, listener);
     }
 
     @Override
     protected IsFilterWidget onCreateView(String appName, DataServiceAsync service) {
         mFilterView = new DeviceFilterView(appName, service);
         return mFilterView;
     }
 
     @Override
     public HashMap<String, Object> getFilterValues() {
         HashMap<String, Object> filters = new HashMap<String, Object>();
         insertValue(filters, SharedParams.DEVICE_ID, mFilterView.mDeviceID.getValue());
         insertValue(filters, SharedParams.DEVICE_APPVERSION, mFilterView.mAppVersion.getValue());
         insertValue(filters, SharedParams.DEVICE_UUID, mFilterView.mUUID.getValue());
         insertValue(filters, SharedParams.DEVICE_OWNER, mFilterView.mOwner.getValue());
         insertValue(filters, SharedParams.DEVICE_BRAND, getListBoxValue(mFilterView.mBrand));
         insertValue(filters, SharedParams.DEVICE_MODEL, getListBoxValue(mFilterView.mModel));
         insertValue(filters, SharedParams.DEVICE_PLATFORM, getListBoxValue(mFilterView.mPlatform));
        insertValue(filters, SharedParams.DEVICE_OSVERSION, getListBoxValue(mFilterView.mOSVersion));
         insertValue(filters, SharedParams.DEVICE_RESOLUTION, getListBoxValue(mFilterView.mResolution));
         insertValue(filters, SharedParams.DEVICE_CREATED, mFilterView.mCreated.getValue());
         insertValue(filters, SharedParams.DEVICE_UPDATED, mFilterView.mUpdated.getValue());
         return filters;
     }
 
     @Override
     public void refreshData() {
         mFilterView.refreshData();
     }
 
 }
