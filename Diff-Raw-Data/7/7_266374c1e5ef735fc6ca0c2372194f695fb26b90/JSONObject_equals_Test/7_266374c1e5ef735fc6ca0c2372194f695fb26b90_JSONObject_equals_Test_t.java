 package org.json;
 
 import org.junit.Test;
 
 import rjson.domain.Person;
 import rjson.utils.RjsonUtil;
import zen.Assert;
 
 public class JSONObject_equals_Test {
 	@Test public void verifyStringComparisions() throws JSONException {
		JSONObject jo1 = new JSONObject(RjsonUtil.completeSerializer().toJson(Person.getFullyLoadedInstance()));
 		JSONObject jo2 = new JSONObject(RjsonUtil.completeSerializer().toJson(Person.getFullyLoadedInstance()));
		Assert.thatEquals(jo1, jo2);
 	}
 }
