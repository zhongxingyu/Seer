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
 
 import android.content.Context;
 
 public class LoggedObjectFilter extends AbstractObjectFilter {
 	public LoggedObjectFilter() {
 		title = "Logged State";
 		multiSelect = true;
 	}
 
 	@Override
 	public String getSqlString() {
 		String retVal = "";
 		
 		if(filters.get(LoggedFilterTypes.LOGGED.toString())) {
 			retVal = retVal.concat("logged = 'true'");
 		}
 		if(filters.get(LoggedFilterTypes.NOT_LOGGED.toString())) {
 			if(retVal.length() != 0) {
 				retVal = retVal.concat(" OR ");
 			}
			retVal = retVal.concat("logged IS NOT 'true'"); //this value may be null or false (initialized to null)
 		}
 		
 		return retVal;
 	}
 
 	@Override
 	public void resetValues() {
 		for(LoggedFilterTypes type : LoggedFilterTypes.values()) {
 			filters.put(type.toString(), false);
 		}
 	}
 
 	public enum LoggedFilterTypes {
 		LOGGED("Logged"), NOT_LOGGED("Not Logged");
 		
 		private String filterText;
 		
 		LoggedFilterTypes(String text) {
 			this.filterText = text;
 		}
 		
 		public String toString() {
 			return filterText;
 		}
 	}
 }
