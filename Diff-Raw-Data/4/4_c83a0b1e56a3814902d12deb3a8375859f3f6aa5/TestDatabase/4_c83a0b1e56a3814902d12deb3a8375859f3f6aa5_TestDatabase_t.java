 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011 CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Level;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.craftfire.commons.database.DataField;
 import com.craftfire.commons.database.DataManager;
 import com.craftfire.commons.database.DataRow;
 import com.craftfire.commons.database.DataType;
 import com.craftfire.commons.util.ValueType;
 
 public class TestDatabase {
     private static final String table = "typetest";
     private static final String wrtable = "writetest";
     private static DataManager datamanager;
     private static String user = "sa";
     private static String password = "";
     private static int randomInt = new Random().nextInt(1000);
 
     @BeforeClass
     public static void init() {
         datamanager = new DataManager(DataType.H2, user, password);
         datamanager.getLogger().getLogger().setLevel(Level.OFF); // Turn off logging temporarily so we won't be spammed with red warnings.
         datamanager.setDatabase("test");
         datamanager.setDirectory("./target/test-classes/");
         datamanager.setTimeout(0);
         datamanager.setKeepAlive(true);
         datamanager.setPrefix("");
     }
 
     @Test
     public void testSettings() {
         assertEquals(user, datamanager.getUsername());
         assertEquals(password, datamanager.getPassword());
         assertEquals("test", datamanager.getDatabase());
         assertEquals("./target/test-classes/", datamanager.getDirectory());
         assertEquals(0, datamanager.getTimeout());
         assertTrue(datamanager.isKeepAlive());
         assertEquals("", datamanager.getPrefix());
         assertTrue(datamanager.hasConnection());
         System.out.println("DataManager started " + (System.currentTimeMillis() / 1000 - datamanager.getStartup()) + " seconds ago.");
     }
 
     @Test
     public void testReconnect() throws SQLException {
         datamanager.reconnect();
         assertTrue(datamanager.isConnected());
         assertTrue(datamanager.getConnection().isValid(1));
     }
 
     @Test
     public void testExist() {
         assertTrue(datamanager.tableExist(table));
         assertFalse(datamanager.tableExist("thisTableShouldNeverExist"));
         assertTrue(datamanager.exist(table, "ID", 1));
         assertFalse(datamanager.exist(table, "id", 8));
         assertFalse(datamanager.exist("thisTableShouldNeverExist", "id", 8));
     }
 
     @Test
     public void getLastID() {
         assertEquals(1, datamanager.getLastID("ID", table));
         assertEquals(0, datamanager.getLastID("I", "empty"));
         assertEquals(0, datamanager.getLastID("I", "thisTableShouldNeverExist"));
         assertEquals(1, datamanager.getLastID("ID", table, "`char` = '8.88'"));
         assertEquals(0, datamanager.getLastID("ID", table, "`char` = 'alice has a cat'"));
         assertEquals(0, datamanager.getLastID("ID", "thisTableShouldNeverExist", "`char` = 'alice has a cat'"));
     }
 
     @Test
     public void testCount() {
         assertEquals(1, datamanager.getCount(table));
         assertEquals(0, datamanager.getCount(table, "`char` = 'alice has a cat'"));
         assertEquals("SELECT COUNT(*) FROM `" + table + "` WHERE `char` = 'alice has a cat' LIMIT 1", datamanager.getLastQuery());
         assertTrue(datamanager.getQueriesCount() >= 2);
         assertTrue(datamanager.getQueries().containsValue("SELECT COUNT(*) FROM `" + table + "` WHERE `char` = 'alice has a cat' LIMIT 1"));
         assertEquals(0, datamanager.getCount("thisTableShouldNeverExist", "`char` = 'alice has a cat'"));
         assertEquals(0, datamanager.getCount("thisTableShouldNeverExist"));
     }
 
     @Test
     public void testInsert() throws SQLException {
         int prevId = datamanager.getLastID("id", wrtable);
         Date now = new Date();
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("TXT", "commons" + randomInt);
         data.put("x", randomInt + 1);
         data.put("d", now);
         data.put("b", null);
         datamanager.insertFields(data, wrtable);
         int id = datamanager.getLastID("id", wrtable);
         assertTrue(id > prevId);
         assertEquals(randomInt + 1, datamanager.getIntegerField(wrtable, "x", "`id` = '" + id + "'"));
         assertEquals("commons" + randomInt, datamanager.getStringField(wrtable, "txt", "`id` = '" + id + "'"));
         assertEquals(now.getTime(), datamanager.getDateField(wrtable, "d", "`id` = '" + id + "'").getTime());
         assertTrue(datamanager.getField(ValueType.UNKNOWN, wrtable, "b", "`id` = '" + id + "'").isNull());
     }
 
     @Test
     public void testUpdate() throws SQLException {
         String oldString = datamanager.getStringField(wrtable, "txt", "`id` = '1'");
         String testString = "crafttest" + (randomInt + 2);
         Date oldDate = datamanager.getDateField(wrtable, "d", "`id` = '1'");
         Date testDate = new Date(1356120741);
         int oldX = datamanager.getIntegerField(wrtable, "x", "`id` = '1'");
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("txt", testString);
         data.put("d", testDate);
         data.put("x", null);
         datamanager.updateFields(data, wrtable, "`id` = '1'");
         assertEquals(testString, datamanager.getStringField(wrtable, "txt", "`id` = '1'"));
         assertTrue(datamanager.getField(ValueType.UNKNOWN, wrtable, "x", "`id` = '1'").isNull());
         assertEquals(testDate, datamanager.getDateField(wrtable, "d", "`id` = '1'"));
         datamanager.updateField(wrtable, "txt", oldString, "`id` = '1'");
         datamanager.updateField(wrtable, "d", oldDate, "`id` = '1'");
         datamanager.executeQueryVoid("UPDATE `" + wrtable + "` SET `x` = '" + oldX + "' WHERE `id` = '1'");
         assertEquals(oldString, datamanager.getStringField(wrtable, "txt", "`id` = '1'"));
         assertEquals(oldDate, datamanager.getDateField(wrtable, "d", "`id` = '1'"));
     }
 
     @Test
     public void testIncrease() throws SQLException {
         int oldValue = datamanager.getIntegerField(wrtable, "x", "`id` = '1'");
         datamanager.increaseField(wrtable, "x", "`id` = '1'");
         assertEquals(oldValue + 1, datamanager.getIntegerField(wrtable, "x", "`id` = '1'"));
     }
 
     @Test
     public void testUpdateBlob() {
         String old = datamanager.getBinaryField(wrtable, "b", "`id` = '1'");
         String test = "I love JUnit Test Cases!";
         datamanager.updateBlob(wrtable, "b", "`id` = '1'", test);
         assertEquals(test, datamanager.getBinaryField(wrtable, "b", "`id` = '1'"));
         datamanager.updateBlob(wrtable, "b", "`id` = '1'", old);
         assertEquals(old, datamanager.getBinaryField(wrtable, "b", "`id` = '1'"));
     }
 
     @Test
     public void testGetKindaFieldEmptyTable() {
         String name = "i";
         String table = "empty";
         assertFalse(datamanager.getBooleanField(table, name, "1"));
         assertNull(datamanager.getBinaryField(table, name, "1"));
         assertNull(datamanager.getBlobField(table, "b", "1"));
         assertNull(datamanager.getDateField(table, name, "1"));
         assertEquals(0, datamanager.getDoubleField(table, name, "1"), 0);
         assertEquals(0, datamanager.getIntegerField(table, name, "1"));
         assertNull(datamanager.getStringField(table, name, "1"));
         assertFalse(datamanager.getBooleanField("SELECT `i` FROM `" + table + "`"));
         assertNull(datamanager.getBinaryField("SELECT `i` FROM `" + table + "`"));
         assertNull(datamanager.getBlobField("SELECT `b` FROM `" + table + "`"));
         assertNull(datamanager.getDateField("SELECT `i` FROM `" + table + "`"));
         assertEquals(0, datamanager.getDoubleField("SELECT `i` FROM `" + table + "`"), 0);
         assertEquals(0, datamanager.getIntegerField("SELECT `i` FROM `" + table + "`"));
         assertNull(datamanager.getStringField("SELECT `i` FROM `" + table + "`"));
     }
 
     @Test
     public void testGetKindaFieldRawQuery() {
         assertTrue(datamanager.getBooleanField("SELECT `bool` FROM `" + table + "`"));
         assertNotNull(datamanager.getBinaryField("SELECT `bin` FROM `" + table + "`"));
         assertNotNull(datamanager.getBlobField("SELECT `blob` FROM `" + table + "`"));
         assertNotNull(datamanager.getDateField("SELECT `date` FROM `" + table + "`"));
         assertThat(datamanager.getDoubleField("SELECT `double` FROM `" + table + "`"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField("SELECT `int` FROM `" + table + "`"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField("SELECT `vchar` FROM `" + table + "`"));
     }
 
     @Test
     public void testNonZeroTimeout() {
         long time;
         datamanager.close(true);
         datamanager.setTimeout(1);
         assertTrue(datamanager.hasConnection());
         time = System.currentTimeMillis();
         assertTrue(datamanager.getStartup() * 1000 < time);
         try {
             Thread.sleep(1000);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         datamanager.close();
         assertTrue(datamanager.hasConnection());
         assertTrue(datamanager.getStartup() * 1000 > time);
         datamanager.close(true);
         datamanager.setTimeout(0);
     }
 
     @Test
     public void testBInt() throws SQLException, ParseException, IOException {
         final String name = "bint";
         final BigInteger expected = new BigInteger("487250340273948");
        final Date expectedDate = new Date(487250340273948L);
         final byte[] expectedBytes = { 0, 1, -69, 38, -49, 114, -33, 28 };
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertEquals(expected, field.getBigInt());
         assertTrue(field.getBool());
         assertArrayEquals(expectedBytes, field.getBytes());
         assertEquals(expectedDate, field.getDate());
         assertEquals(new BigDecimal(expected), field.getDecimal());
         assertEquals(expected.doubleValue(), field.getDouble(), 0);
         assertEquals(expected.floatValue(), field.getFloat(), 0);
         assertEquals(expected.intValue(), field.getInt());
         assertEquals(expected.longValue(), field.getLong());
         assertEquals("487250340273948", field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertEquals(expected, field.getBigInt());
         assertTrue(field.getBool());
         assertArrayEquals(expectedBytes, field.getBytes());
         assertEquals(expectedDate, field.getDate());
         assertEquals(new BigDecimal(expected), field.getDecimal());
         assertEquals(expected.doubleValue(), field.getDouble(), 0);
         assertEquals(expected.floatValue(), field.getFloat(), 0);
         assertEquals(expected.intValue(), field.getInt());
         assertEquals(expected.longValue(), field.getLong());
         assertEquals("487250340273948", field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertEquals(new String(expectedBytes), datamanager.getBinaryField(table, name, "1"));
         // assertNotNull(datamanager.getBlobField(table, name, "1"));
         InputStream stream = datamanager.getBlobField(table, name, "1").getBinaryStream();
         assertEquals(new String(expectedBytes), CraftCommons.convertStreamToString(stream));
         assertEquals(expected.doubleValue(), datamanager.getDoubleField(table, name, "1"), 0);
         assertEquals("487250340273948", datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertEquals(expected, row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertArrayEquals(expectedBytes, row.getBinaryField(name));
         assertEquals(expectedDate, row.getDateField(name));
         assertEquals(new BigDecimal(expected), row.getDecimalField(name));
         assertEquals(expected.doubleValue(), row.getDoubleField(name), 0);
         assertEquals(expected.floatValue(), row.getFloatField(name), 0);
         assertEquals(expected.intValue(), row.getIntField(name));
         assertEquals(expected.longValue(), row.getLongField(name), 0);
         assertEquals("487250340273948", row.getStringField(name));
     }
 
     @Test
     public void testBin() throws SQLException {
         final String name = "bin";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testBlob() throws SQLException {
         final String name = "blob";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertNotNull(field.getBlob());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertNotNull(field.getBlob());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertNotNull(row.getBlobField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testBool() throws SQLException {
         final String name = "bool";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testChar() throws SQLException {
         final String name = "char";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testClob() throws SQLException {
         final String name = "clob";
 
         if (datamanager.getField(ValueType.UNKNOWN, table, name, "1").getDate() == null) {
             datamanager.updateField(table, name, DateFormat.getDateTimeInstance().format(new Date()), "1");
             // Just for debug purposes, to see what is the locale of host.
             System.out.println(DateFormat.getDateInstance().format(new Date()));
             System.out.println(DateFormat.getDateTimeInstance().format(new Date()));
             System.out.println(DateFormat.getTimeInstance().format(new Date()));
             System.out.println(DateFormat.getInstance().format(new Date()));
         }
 
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         if (field.getDate() == null) {
             // Just for debug purposes, to see what is the locale of host.
             System.out.println(DateFormat.getDateInstance().format(new Date()));
             System.out.println(DateFormat.getDateTimeInstance().format(new Date()));
             System.out.println(DateFormat.getTimeInstance().format(new Date()));
             System.out.println(DateFormat.getInstance().format(new Date()));
         }
         assertNotNull(field.getDate());
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testDate() throws SQLException {
         final String name = "date";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getDateField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testDecimal() throws SQLException {
         final String name = "dec";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testDouble() throws SQLException {
         final String name = "double";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testFloat() throws SQLException {
         final String name = "float";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testVCharI() throws SQLException {
         final String name = "vchari";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testInt() throws SQLException {
         final String name = "int";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testLVBin() throws SQLException {
         final String name = "lvbin";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testLVChar() throws SQLException {
         final String name = "lvchar";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testNumeric() throws SQLException {
         final String name = "numeric";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testReal() throws SQLException {
         final String name = "real";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testSInt() throws SQLException {
         final String name = "sint";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testTime() throws SQLException {
         final String name = "time";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getDateField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testTimeStamp() throws SQLException {
         final String name = "timestamp";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getDateField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testTInt() throws SQLException {
         final String name = "tint";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testVBin() throws SQLException {
         final String name = "vbin";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
 
     @Test
     public void testVChar() throws SQLException {
         final String name = "vchar";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
 
         // DataRow.getField()
         DataField field = row.get(name);
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getString());
 
         // DataManager.getField()
         field = datamanager.getField(ValueType.UNKNOWN, table, name, "1");
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getString());
 
         // DataManager.get<Kinda>Field()
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getStringField(table, name, "1"));
 
         // DataRow.get<Kinda>Field()
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getStringField(name));
     }
 
     /*
     public void testTemplate() throws SQLException {
         final String name = "";
         DataRow row = datamanager.getResults("SELECT `" + name + "` FROM `" + table + "` LIMIT 1").getFirstResult();
     
         // DataRow.getField()
         DataField field = row.get(name);
         assertNotNull(field.getBigInt());
         assertNotNull(field.getBlob());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
     
         // DataManager.getField()
         field = datamanager.getField(FieldType.UNKNOWN, table, name, "1");
         assertNotNull(field.getBigInt());
         assertNotNull(field.getBlob());
         assertTrue(field.getBool());
         assertNotNull(field.getBytes());
         assertNotNull(field.getDate());
         assertNotNull(field.getDecimal());
         assertThat(field.getDouble(), not(equalTo(0d)));
         assertThat(field.getFloat(), not(equalTo(0f)));
         assertThat(field.getInt(), not(equalTo(0)));
         assertThat(field.getLong(), not(equalTo(0L)));
         assertNotNull(field.getString());
     
         // DataManager.get<Kinda>Field()
         assertTrue(datamanager.getBooleanField(table, name, "1"));
         assertNotNull(datamanager.getBinaryField(table, name, "1"));
         assertNotNull(datamanager.getBlobField(table, name, "1"));
         assertNotNull(datamanager.getDateField(table, name, "1"));
         assertThat(datamanager.getDoubleField(table, name, "1"), not(equalTo(0d)));
         assertThat(datamanager.getIntegerField(table, name, "1"), not(equalTo(0)));
         assertNotNull(datamanager.getStringField(table, name, "1"));
     
         // DataRow.get<Kinda>Field()
         assertNotNull(row.getBigIntField(name));
         assertNotNull(row.getBlobField(name));
         assertTrue(row.getBoolField(name));
         assertNotNull(row.getBinaryField(name));
         assertNotNull(row.getDateField(name));
         assertNotNull(row.getDecimalField(name));
         assertThat(row.getDoubleField(name), not(equalTo(0d)));
         assertThat(row.getFloatField(name), not(equalTo(0f)));
         assertThat(row.getIntField(name), not(equalTo(0)));
         assertThat(row.getLongField(name), not(equalTo(0L)));
         assertNotNull(row.getStringField(name));
     }
     */
 }
