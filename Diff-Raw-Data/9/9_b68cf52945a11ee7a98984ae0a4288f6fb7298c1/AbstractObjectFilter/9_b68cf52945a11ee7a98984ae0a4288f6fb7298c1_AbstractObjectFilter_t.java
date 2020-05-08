 /*
  * Copyright (c) 2012 Joe Rowley
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.mobileobservinglog.objectSearch;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.TreeMap;
 import java.util.Set;
 
 import android.content.Context;
 
 public abstract class AbstractObjectFilter implements ObjectFilter {
 	protected boolean filterIsSet;
 	protected boolean multiSelect;
 	protected String title;
 	protected TreeMap<String, Boolean> filters;
 	
 	public AbstractObjectFilter() {
 		filters = new TreeMap<String, Boolean>();
 	}
 
 	public String getSearchDescription() {
 		String retVal = "";
 		
 		Set<String> filterKeys = filters.keySet();
 		String theRest = "";
 		for(String key : filterKeys) {
 			if(filters.get(key)) {
 				if(theRest.length() != 0) {
 					theRest = theRest.concat(", ");
 				}
 				theRest = theRest.concat(key);
 			}
 		}
		if(filterIsSet && theRest.length() > 0) {
			retVal = title + ": " + theRest;
		}
		
 		return retVal;
 	}
 
 	public abstract String getSqlString();
 
 	public void resetFilter() {
 		resetValues();
 		filterIsSet = false;
 	}
 	
 	protected abstract void resetValues();
 
 	public boolean isSet() {
 		return filterIsSet;
 	}
 
 	public void setFilter(TreeMap<String, Boolean> filters) {
 		Set<String> keys = filters.keySet();
 		for(String key : keys) {
 			if(this.filters.containsKey(key)) {
 				//If it's not multiselect, we must first clear the current filters before setting this one
 				if(!multiSelect) {
 					resetFilter();
 				}
 				
 				if(!filterIsSet) {
 					filterIsSet = true;
 				}
 				
 				this.filters.put(key, filters.get(key));
 			}
 		}
 	}
 	
 	public ArrayList<ObjectFilterInformation> getFilterInfo() {
 		ArrayList<ObjectFilterInformation> retVal = new ArrayList<ObjectFilterInformation>();
 		retVal.add(new ObjectFilterInformation(title, filters, multiSelect));
 		return retVal;
 	}
 }
