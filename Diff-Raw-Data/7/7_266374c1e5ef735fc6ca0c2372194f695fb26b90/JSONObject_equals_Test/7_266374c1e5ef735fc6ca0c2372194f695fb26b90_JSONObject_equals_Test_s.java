 package org.json;
 
 import org.junit.Test;
 
 import rjson.domain.Person;
 import rjson.utils.RjsonUtil;
 
 public class JSONObject_equals_Test {
 	@Test public void verifyStringComparisions() throws JSONException {
		JSONObject jo1 = new JSONObject(RjsonUtil.completeSerializer().toJson(Person.getFullyLoadedInstanceWithNullAddress()));
 		JSONObject jo2 = new JSONObject(RjsonUtil.completeSerializer().toJson(Person.getFullyLoadedInstance()));
		boolean equals = jo1.equals(jo2);
 	}
 }
