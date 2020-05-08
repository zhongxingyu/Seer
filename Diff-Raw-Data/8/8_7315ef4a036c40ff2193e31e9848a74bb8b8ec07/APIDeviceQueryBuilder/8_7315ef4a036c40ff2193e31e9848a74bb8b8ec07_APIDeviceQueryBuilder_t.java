 package com.testdroid.api;
 
 import com.testdroid.api.model.APIDevice;
 import org.apache.commons.lang.StringUtils;
 
 /**
  *
  * @author kajdus
  */
 public class APIDeviceQueryBuilder extends APIQueryBuilder {
     private Long[] labelIds;
     private Long[] deviceGroupIds;
     private APIDevice.DeviceFilter[] deviceFilters;
     
     public APIDeviceQueryBuilder filterWithLabelIds(Long... labelIds) {
         this.labelIds = labelIds;
         return this;
     }
     
     public APIDeviceQueryBuilder filterWithDeviceGroupIds(Long... deviceGroupIds) {
         this.deviceGroupIds = deviceGroupIds;
         return this;
     }
     
     public APIDeviceQueryBuilder filterWithDeviceFilters(APIDevice.DeviceFilter... deviceFilters) {
         this.deviceFilters = deviceFilters;
         return this;
     }
     
     @Override
     public APIDeviceQueryBuilder offset(int offset) {
         return (APIDeviceQueryBuilder) super.offset(offset);
     }
     
     @Override
     public APIDeviceQueryBuilder limit(int limit) {
         return (APIDeviceQueryBuilder) super.limit(limit);
     }
     
     @Override
     public APIDeviceQueryBuilder search(String search) {
         return (APIDeviceQueryBuilder) super.search(search);
     }
     
     @Override
     public APIDeviceQueryBuilder sort(Class<? extends APIEntity> type, APISort.SortItem... sortItems) {
         return (APIDeviceQueryBuilder) sort(type, sortItems);
     }
 
     @Override
     protected String build(String uri) {
         String superResult = super.build(uri);
         String thisResult = superResult + (superResult.contains("?") ? "&" : "?");
         if(labelIds != null && labelIds.length > 0) {
            thisResult += "label_id[]=" + StringUtils.join(labelIds, "&label_id=") + "&";
         }
         if(deviceGroupIds != null && deviceGroupIds.length > 0) {
            thisResult += "device_group_id[]=" + StringUtils.join(deviceGroupIds, "&device_group_id=") + "&";
         }
         if(deviceFilters != null && deviceFilters.length > 0) {
            thisResult += "device_filter[]=" + StringUtils.join(deviceFilters, "&device_filter=") + "&";
         }
         return thisResult;
     }
 }
