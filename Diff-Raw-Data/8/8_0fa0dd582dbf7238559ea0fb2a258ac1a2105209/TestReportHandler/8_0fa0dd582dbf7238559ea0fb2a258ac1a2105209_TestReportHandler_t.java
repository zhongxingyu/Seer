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
 
 import org.apache.commons.lang.StringUtils;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class TestReportHandler extends DefaultHandler {
 	TestSuite testSuite = new TestSuite();
 	String tmpValue = "";
 	private TestCase tmpTestCase;
 	private TestFailure tmpTestFailure;
 
 	@Override
 	public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
 		if ("testsuite".equals(elementName)) {
 			testSuite.setErrors(Integer.valueOf(attributes.getValue("errors")));
 			testSuite.setTests(Integer.valueOf(attributes.getValue("tests")));
 			testSuite.setFailures(Integer.valueOf(attributes.getValue("failures")));
 			testSuite.setSkipped(Integer.valueOf(attributes.getValue("skipped")));
 			testSuite.setTime(Double.valueOf(attributes.getValue("time")));
 			testSuite.setName(attributes.getValue("name"));
 		} else if ("testcase".equals(elementName)) {
 			TestCase testCase = new TestCase();
 			testCase.setTime(Double.valueOf(attributes.getValue("time")));
 			testCase.setName(attributes.getValue("name"));
 			testSuite.addTestCase(testCase);
 			tmpTestCase = testCase;
 		} else if ("failure".equals(elementName)) {
 			TestFailure testFailure = new TestFailure();
 			testFailure.setType(attributes.getValue("type"));
 			tmpTestCase.setFailure(testFailure);
 			tmpTestFailure = testFailure;
		} else if ("error".equals(elementName)) {
			TestFailure testFailure = new TestFailure();
			testFailure.setType(attributes.getValue("type"));
			tmpTestCase.setFailure(testFailure);
			tmpTestFailure = testFailure;
 		}
 		
 		tmpValue = "";
 	}
 
 	@Override
 	public void endElement(String s, String s1, String element) throws SAXException {
 		if (StringUtils.equalsIgnoreCase("failure", element)) {
 			tmpTestFailure.setReason(StringUtils.trim(tmpValue));
		} else if (StringUtils.equalsIgnoreCase("error", element)) {
			tmpTestFailure.setReason(StringUtils.trim(tmpValue));
 		}
 	}
 
 	@Override
 	public void characters(char[] ac, int i, int j) throws SAXException {
 		tmpValue = tmpValue + new String(ac, i, j);
 	}
 }
