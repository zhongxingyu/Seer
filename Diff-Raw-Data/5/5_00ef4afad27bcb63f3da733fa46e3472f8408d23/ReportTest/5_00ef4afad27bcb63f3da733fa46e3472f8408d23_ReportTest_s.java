 /*
  * Copyright 2011 Damien Bourdette
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.dbourdette.otto.report;
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Interval;
 import org.junit.Before;
 import org.junit.Test;
 
 import junit.framework.Assert;
 
 /**
  * @author damien bourdette
  * @version \$Revision$
  */
 public class ReportTest {
 
     private Report report;
 
     private final DateTime now = new DateTime();
 
     private static final String USER_LOGIN = "user login";
 
     private static final String USER_LOGOUT = "user logout";
 
     @Before
     public void init() {
         report = new Report();
         report.ensureColumnExists(USER_LOGIN);
     }
 
     @Test
     public void graph() {
         DateTime end = new DateTime();
         DateTime start = end.minusDays(1);
 
         Report report = new Report().rows(new Interval(start, end));
 
         Assert.assertEquals(start, report.getStartDate());
     }
 
     @Test
     public void ensureColumnExists() {
         report.ensureColumnExists(USER_LOGIN);
         report.ensureColumnExists(USER_LOGIN);
 
         Assert.assertTrue(report.hasColumn(USER_LOGIN));
         Assert.assertEquals(1, report.getColumnCount());
     }
 
     @Test
     public void hasColumn() {
         Assert.assertTrue(report.hasColumn(USER_LOGIN));
         Assert.assertFalse(report.hasColumn("not a column"));
     }
 
     @Test
     public void setRows() {
         report.setRows(new Interval(now.minusHours(1), now));
 
         Assert.assertEquals("There should be 12 rows", 12, report.getRowCount());
         Assert.assertEquals("Third row has incorrect bounds", now.minusMinutes(50), report.getRowStartDate(2));
     }
 
     @Test
     public void getStartDate() {
         report.setRows(new Interval(now.minusHours(1), now));
 
         Assert.assertEquals(now.minusHours(1), report.getStartDate());
     }
 
     @Test
     public void getEndDate() {
         report.setRows(new Interval(now.minusHours(1), now));
 
         Assert.assertEquals(now, report.getEndDate());
     }
 
     @Test
     public void addRowsAutomaticallyComputeInterval() {
         report.setRows(new Interval(now.minusHours(1), now));
 
         Assert.assertEquals("There should be 12 rows", 12, report.getRowCount());
         Assert.assertEquals("Third row has incorrect bounds", now.minusMinutes(50), report.getRowStartDate(2));
     }
 
     @Test
     public void setValue() {
         report.setRows(new Interval(now.minusMinutes(10), now));
 
         report.setValue(USER_LOGIN, now.minusMinutes(6), 2);
         report.setValue(USER_LOGIN, now.minusMinutes(2), 10);
 
         Assert.assertEquals("First cell should contains 2", (Integer) 2, report.getValue(USER_LOGIN, 0));
         Assert.assertEquals("Second cell should contains 10", (Integer) 10, report.getValue(USER_LOGIN, 1));
     }
 
     @Test
     public void setDefaultValue() {
         report.setRows(new Interval(now.minusMinutes(10), now));
 
         report.setDefaultValue(null);
 
         Assert.assertNull("Value should be null", report.getValue(USER_LOGIN, 0));
 
         report.setDefaultValue(1);
 
         Assert.assertEquals("Value should be 1", (Integer) 1, report.getValue(USER_LOGIN, 0));
     }
 
     @Test
     public void increaseValue() {
         report.setRows(new Interval(now.minusMinutes(10), now));
 
         report.increaseValue(USER_LOGIN, now.minusMinutes(2));
 
         Assert.assertEquals("Cell should contains 1", (Integer) 1, report.getValue(USER_LOGIN, 1));
 
         report.increaseValue(USER_LOGIN, now.minusMinutes(2), 10);
 
         Assert.assertEquals("Cell should contains 11", (Integer) 11, report.getValue(USER_LOGIN, 1));
 
         report.increaseValue(USER_LOGIN, now.minusMinutes(2), null);
 
         Assert.assertEquals("Cell should contains 11", (Integer) 11, report.getValue(USER_LOGIN, 1));
     }
 
     @Test
     public void cumulate() {
         report.setRows(new Interval(now.minusMinutes(10), now));
 
         report.increaseValue(USER_LOGIN, now.minusMinutes(2));
         report.increaseValue(USER_LOGIN, now.minusMinutes(3), 10);
         report.increaseValue(USER_LOGIN, now.minusMinutes(7), 2);
 
         report.cumulate(USER_LOGIN);
 
         Assert.assertEquals("Cell should contains 2", (Integer) 2, report.getValue(USER_LOGIN, 0));
         Assert.assertEquals("Cell should contains 13", (Integer) 13, report.getValue(USER_LOGIN, 1));
     }
 
     @Test
     public void top() {
         report.setRows(new Interval(now.minusMinutes(10), now));
 
         report.dropColumn(USER_LOGIN);
         report.ensureColumnsExists("col1", "col2", "col3");
 
         report.increaseValue("col1", now.minusMinutes(2), 4);
         report.increaseValue("col2", now.minusMinutes(3), 10);
         report.increaseValue("col3", now.minusMinutes(7), 2);
         report.increaseValue("col1", now.minusMinutes(7), 7);
 
         report.top(2);
 
         Assert.assertEquals("There should be only 2 columns", 2, report.getColumnCount());
         Assert.assertEquals("Cell should contains 4", (Integer) 4, report.getValue("col1", 1));
         Assert.assertEquals("Cell should contains 10", (Integer) 10, report.getValue("col2", 1));
     }
 
     @Test
     public void sortAlphabetically() {
         report.setRows(new Interval(now.minusMinutes(10), now));
 
         report.dropColumn(USER_LOGIN);
         report.ensureColumnsExists("col2", "col3", "col1");
 
         report.sortAlphabetically();
 
         Assert.assertEquals("first column is col1", "col1", report.getColumnTitles().get(0));
         Assert.assertEquals("last column is col3", "col3", report.getColumnTitles().get(2));
     }
 
     @Test
     public void sortBySum() throws IOException {
         report.ensureColumnExists(USER_LOGOUT);
 
         DateTime dateTime = new DateTime(2010, 10, 10, 0, 0, 0, 0, DateTimeZone.forID("+02:00"));
 
         report.setDefaultValue(0);
         report.setRows(new Interval(dateTime.minusMinutes(15), dateTime));
 
         report.setValue(USER_LOGIN, dateTime.minusMinutes(6), 2);
         report.setValue(USER_LOGIN, dateTime.minusMinutes(2), 2);
         report.setValue(USER_LOGOUT, dateTime.minusMinutes(2), 5);
 
         Assert.assertEquals(USER_LOGIN, report.getColumnTitles().get(0));
 
         report.sortBySum();
 
         Assert.assertEquals(USER_LOGOUT, report.getColumnTitles().get(0));
     }
 
     @Test
     public void toCsv() throws IOException {
         DateTime dateTime = new DateTime(2010, 10, 10, 0, 0, 0, 0);
 
         report.setDefaultValue(0);
 
         report.setRows(new Interval(dateTime.minusMinutes(15), dateTime));
 
         report.setValue(USER_LOGIN, dateTime.minusMinutes(6), 2);
         report.setValue(USER_LOGIN, dateTime.minusMinutes(2), 10);
 
         String expected = IOUtils.toString(getClass().getResourceAsStream("ReportTest-toCsv.txt"));
 
         expected = StringUtils.replace(expected, "\r\n", "\n");
 
         Assert.assertEquals("toCsv is incorrect", expected, report.toCsv());
     }
 
     @Test
    public void toGoogleJs() throws IOException {
         DateTime dateTime = new DateTime(2010, 10, 10, 0, 0, 0, 0, DateTimeZone.forID("+02:00"));
 
         report.setDefaultValue(0);
         report.setRows(new Interval(dateTime.minusMinutes(15), dateTime));
 
         report.setValue(USER_LOGIN, dateTime.minusMinutes(6), 2);
         report.setValue(USER_LOGIN, dateTime.minusMinutes(2), 10);
 
         String expected = IOUtils.toString(getClass().getResourceAsStream("ReportTest-toGoogleJs.txt"));
 
         expected = StringUtils.replace(expected, "\r\n", "\n");
 
        Assert.assertEquals("toGoogleJs is incorrect", expected, report.toGoogleJs("chart_div", null, null));
     }
 
     @Test
     public void toGoogleImageParams() throws IOException {
         report.ensureColumnExists(USER_LOGOUT);
 
         DateTime dateTime = new DateTime(2010, 10, 10, 0, 0, 0, 0, DateTimeZone.forID("+02:00"));
 
         report.setDefaultValue(0);
         report.setRows(new Interval(dateTime.minusMinutes(15), dateTime));
 
         report.setValue(USER_LOGIN, dateTime.minusMinutes(6), 2);
         report.setValue(USER_LOGIN, dateTime.minusMinutes(2), 10);
         report.setValue(USER_LOGOUT, dateTime.minusMinutes(2), 5);
 
         Map<String, String> params = report.toGoogleImageParams(800, 400);
 
         Assert.assertEquals("t:0,2,10|0,0,5", params.get("chd"));
     }
 }
