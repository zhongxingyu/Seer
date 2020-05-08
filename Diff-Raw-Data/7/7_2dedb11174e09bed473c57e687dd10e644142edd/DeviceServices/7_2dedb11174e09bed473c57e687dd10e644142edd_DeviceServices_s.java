 package com.spatialize.io;
 
 import hk.com.quantum.zonemgr.ZoneManagerService;
 import hk.com.quantum.zonemgr.response.TagListResponse;
 import hk.com.quantum.zonemgr.response.model.Tag;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.annotation.Resource;
 
 public class DeviceServices {
 
 	@Resource
 	private ZoneManagerService zmservice;
 	
 	public Collection<Tag> getAvailableSensors() {
 		TagListResponse resp = zmservice.tagList();
 		Collection<String> tagIds = new ArrayList<String>();
 		for (Tag t: resp.tags.values()) {
 			tagIds.add(t.id);
 		}
 		resp = zmservice.tagPrint(tagIds.toArray(new String[0]));
		return resp.tags.values();
 	}
 }
