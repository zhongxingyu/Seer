 /*
  * Sonar Erlang Plugin
  * Copyright (C) 2012 Tamás Kende
  * kende.tamas@gmail.com
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.erlang.testmetrics.eunit;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 public class TestCase {
 
 	Double time;
 	String name;
 	TestFailure failure;
 	
 	public TestCase(Double time, String name, TestFailure failure) {
 		super();
 		this.time = time;
 		this.name = name;
 		this.failure = failure;
 	}
 
 	public TestCase() {
 		super();
 	}
 
 	public Double getTime() {
 		return time;
 	}
 
 	public void setTime(Double time) {
 		this.time = time;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public TestFailure getFailure() {
 		return failure;
 	}
 
 	public void setFailure(TestFailure failure) {
 		this.failure = failure;
 	}
 	
 	public StringBuilder appendXml(StringBuilder sb) {
 	    sb
 	        .append("<testcase status=\"")
 	        .append(getStatus())
 	        .append("\" time=\"")
 	        .append(time)
 	        .append("\" name=\"")
 	        .append(StringEscapeUtils.escapeXml(name))
 	        .append("\"");
 
 	    if (isErrorOrFailure()) {
 	      sb
 	          .append(">")
 	          .append(isError() ? "<error message=\"" : "<failure message=\"")
 	          .append(StringEscapeUtils.escapeXml(failure.reason))
 	          .append("\">")
 	          .append("<![CDATA[")
 	          .append(StringEscapeUtils.escapeXml(""))
 	          .append("]]>")
 	          .append(isError() ? "</error>" : "</failure>")
 	          .append("</testcase>");
 	    } else {
 	      sb.append("/>");
 	    }
 	    return sb;
 	  }
 
 	private String getStatus() {
 		if(isErrorOrFailure()){
 			if(isError()){
				return "ERROR";
 			} else {
				return "FAILED";
 			}
 		}
		return "PASSED";
 	}
 
 	private boolean isError() {
 		return !failure.type.contains("failed");
 	}
 
 	private boolean isErrorOrFailure() {
 		return failure!=null;
 	}
 	
 	
 }
