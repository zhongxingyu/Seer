 /*
 	This file is part of Brick.
 
     Brick is free software: you can redistribute it and/or modify
     it under the terms of the GNU Lesser General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Brick is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public License
     along with Brick.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.irenical.brick.json;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.irenical.brick.AbstractBundle;
 import com.irenical.brick.BundleInterface;
 
 public class JSONBundle extends AbstractBundle<String> {
 
 	private final JSONObject json;
 
 	
 	public JSONBundle(String value) throws JSONException {
 		this(new JSONObject(value));
 	}
 	
 	public JSONBundle(JSONObject value) {
 		this.json = value;
 	}
 
 	@Override
 	protected BundleInterface<String> createBundle(Object value) {
 		return value instanceof JSONObject ? new JSONBundle((JSONObject) value) : null;
 	}
 	
 	@Override
 	public Set<String> getKeys() {
 		String [] names = JSONObject.getNames(json);
 		return names == null ? null : new HashSet<String>(Arrays.asList(names));
 	}
 	
 	@Override
 	public Iterable<Object> getObjects(String key) {
 		List<Object> result = new LinkedList<Object>();
 		try{
 			JSONArray array = json.optJSONArray(key);
 			if(array!=null){
 				for(int i=0;i<array.length();++i){
 					result.add(array.opt(i));
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	@Override
 	public Object getObject(String key) {
		return json.opt(key);
 	}
 	
 	@Override
 	public String toString() {
 		return json.toString();
 	}
 
 }
