 /* Copyright (c) 2009 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz;
 
 import java.io.*;
 import java.util.*;
 
 import org.fiz.Dataset;
 import org.fiz.test.*;
 
 /**
  * Junit tests for the Dataset class.
  */
 
 public class DatasetTest extends junit.framework.TestCase {
     Dataset d;
 
     public void setUp() {
         d = new Dataset();
     }
 
     public void test_DSArrayList() {
         assertTrue("empty constructor", new Dataset.DSArrayList()
                    instanceof ArrayList);
         assertTrue("int constructor", new Dataset.DSArrayList(5)
                    instanceof ArrayList);
         assertTrue("collection constructor",
                    new Dataset.DSArrayList(new HashSet()) instanceof ArrayList);
     }
 
     public void test_MissingValueError() {
         Dataset.MissingValueError e = new Dataset.MissingValueError("keyName");
         assertEquals("exception message",
                 "couldn't find dataset element \"keyName\"", e.getMessage());
         assertEquals("name of missing key", "keyName", e.getMissingKey());
     }
 
     public void test_SyntaxError() {
         Error e = new Dataset.SyntaxError(null, null);
         assertEquals("exception message",
                 "syntax error in dataset", e.getMessage());
         e = new Dataset.SyntaxError(null, "something bad happened");
         assertEquals("exception message",
                 "syntax error in dataset: something bad happened",
                 e.getMessage());
         e = new Dataset.SyntaxError("file_name", "operator error");
         assertEquals("exception message",
                 "syntax error in dataset (file \"file_name\"): " +
                 "operator error", e.getMessage());
     }
 
     public void test_UnsupportedFormatError() {
         Error e = new Dataset.UnsupportedFormatError("file_name.foo");
         assertEquals("exception message",
                 "couldn't recognize format of dataset file \"file_name.foo\"",
                 e.getMessage());
     }
 
     public void test_UnknownFileFormatError() {
         Error e = new Dataset.UnknownFileFormatError("simple message");
         assertEquals("simple message", e.getMessage());
     }
 
     // Constructor Dataset(Object... keysAndValues)
 
     public void test_constructor_keysAndValuesObjects_basics() {
         Dataset d = new Dataset("a", "123", "b", "abcde",
                 "b", new Dataset("name", "Alice", "age", "23"),
                 "b", new Dataset("child", new Dataset("name", "Bill")),
                 "c", new Dataset("state", "California"));
         assertEquals("dataset contents", "a: 123\n" +
                 "b:\n" +
                 "  - abcde\n" +
                 "  - age:  23\n" +
                 "    name: Alice\n" +
                 "  - child:\n" +
                 "        name: Bill\n" +
                 "c:\n" +
                 "    state: California\n", d.toString());
     }
     public void test_constructor_keysAndValuesObjects_extraKey() {
         Dataset d = new Dataset("a", new Dataset("x", "1"), "b");
         assertEquals("dataset contents", "a:\n" +
                 "    x: 1\n", d.toString());
     }
     // Constructor Dataset(HashMap contents, String fileName)
 
     @SuppressWarnings("unchecked")
     public void test_constructor_hashMapAndString() {
         HashMap map = new HashMap();
         map.put("name44", "value66");
         Dataset d = new Dataset(map, "file123");
         assertEquals("value from dataset", "value66", d.getString("name44"));
     }
 
     public void test_newFileInstance_searchForExtension() {
         TestUtil.writeFile("test.yml", "first: abc\nsecond: def\n");
         Dataset d = Dataset.newFileInstance("test");
         assertEquals("dataset value", "abc", d.get("first"));
         TestUtil.deleteTree("test.yml");
     }
 
     public void test_newFileInstance_cantFindExtension() {
         boolean gotException = false;
         try {
             Dataset d = Dataset.newFileInstance("a/b/bogus");
         }
         catch (FileNotFoundError e) {
             assertEquals("exception message",
                     "couldn't open dataset file \"a/b/bogus\": couldn't "
                     + "find a file with a supported extension",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_newFileInstance_ymlExtension() {
         TestUtil.writeFile("test.yml", "first: abc\nsecond: def\n");
         Dataset d = Dataset.newFileInstance("test");
         assertEquals("check value", "abc", d.get("first"));
         TestUtil.deleteTree("test.yml");
     }
     public void test_newFileInstance_yamlExtension() {
         TestUtil.writeFile("test.yaml", "first: 24\nsecond: 35\n");
         Dataset d = Dataset.newFileInstance("test");
         assertEquals("check value", 24, d.get("first"));
         TestUtil.deleteTree("test.yaml");
     }
     public void test_newFileInstance_xmlExtension() {
         TestUtil.writeFile("test.xml",
                 "<message><first>123</first><second>456</second></message>");
         Dataset d = Dataset.newFileInstance("test");
         assertEquals("check value", "123", d.get("first"));
         TestUtil.deleteTree("test.xml");
     }
     public void test_newFileInstance_unknownExtension() {
         boolean gotException = false;
         try {
             Dataset d = Dataset.newFileInstance("bogus_44.xyz");
         }
         catch (Dataset.UnsupportedFormatError e) {
             assertEquals("exception message",
                     "couldn't recognize format of dataset file " +
                     "\"bogus_44.xyz\"", e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 
     public void test_newFileInstanceFromPath_basics() {
         (new File("_test1_")).mkdir();
         TestUtil.writeFile("_test1_/test.yaml", "first: 24\nsecond: 35\n");
         Dataset d = Dataset.newFileInstanceFromPath("test",
                 new String[] {"_test1_"}, Dataset.Quantity.ALL);
         assertEquals("check value", 24, d.get("first"));
         TestUtil.deleteTree("_test1_");
     }
     public void test_newFileInstanceFromPath_firstOnly() {
         (new File("_test1_")).mkdir();
         (new File("_test1_/child")).mkdir();
         TestUtil.writeFile("_test1_/test.yaml", "first: 24\nsecond: 35\n");
         TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
         Dataset d = Dataset.newFileInstanceFromPath("test",
                 new String[] {"_test1_", "_test1_/child"},
                 Dataset.Quantity.FIRST_ONLY);
         assertEquals("class name for result", "YamlDataset",
                 d.getClass().getSimpleName());
         assertEquals("first value should exist", 24, d.get("first"));
         assertEquals("third value shouldn't exist", null, d.checkString("third"));
         TestUtil.deleteTree("_test1_");
     }
     public void test_newFileInstanceFromPath_compound() {
         (new File("_test1_")).mkdir();
         (new File("_test1_/child")).mkdir();
         (new File("_test1_/child2")).mkdir();
         TestUtil.writeFile("_test1_/test.yaml", "first: 24\nsecond: 35\n");
         TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
         TestUtil.writeFile("_test1_/child2/test.yaml",
                 "first: 99\nfourth: 777\n");
         Dataset d = Dataset.newFileInstanceFromPath("test",
                 new String[] {"_test1_", "_test1_/child", "_test1_/child2"},
                 Dataset.Quantity.ALL);
         assertEquals("class name for result", "CompoundDataset",
                 d.getClass().getSimpleName());
         assertEquals("value from first dataset", "24", d.getString("first"));
         assertEquals("value from second dataset", "value", d.getString("third"));
         assertEquals("value from last dataset", "777", d.getString("fourth"));
         TestUtil.deleteTree("_test1_");
     }
     public void test_newFileInstanceFromPath_FileNotFoundError() {
         (new File("_test1_")).mkdir();
         (new File("_test1_/child")).mkdir();
         TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
         Dataset d = Dataset.newFileInstanceFromPath("test",
                 new String[] {"_test1_", "_test1_/child"},
                 Dataset.Quantity.ALL);
         assertEquals("dataset value", "value", d.checkString("third"));
         TestUtil.deleteTree("_test1_");
     }
     public void test_newFileInstanceFromPath_datasetNotFound() {
         (new File("_test1_")).mkdir();
         (new File("_test1_/child")).mkdir();
         boolean gotException = false;
         try {
             Dataset d = Dataset.newFileInstanceFromPath("sample",
                     new String[] {"_test1_", "_test1_/child"},
                     Dataset.Quantity.FIRST_ONLY);
         }
         catch (FileNotFoundError e) {
             assertEquals("exception message",
                     "couldn't find dataset file \"sample\" in path "
                     + "(\"_test1_\", \"_test1_/child\")",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
         TestUtil.deleteTree("_test1_");
     }
     public void test_newFileInstanceFromPath_dontReturnCompoundForOneDataset() {
         (new File("_test1_")).mkdir();
         (new File("_test1_/child")).mkdir();
         TestUtil.writeFile("_test1_/child/test.yaml", "third: value\n");
         Dataset d = Dataset.newFileInstanceFromPath("test",
                 new String[] {"_test1_", "_test1_/child"},
                 Dataset.Quantity.ALL);
         assertEquals("class name for result", "YamlDataset",
                 d.getClass().getSimpleName());
         TestUtil.deleteTree("_test1_");
     }
 
     public void test_newSerializedInstance() {
         Dataset d = new Dataset("name", "Alice", "age", "64",
                 "child", new Dataset("name", "Bob"));
         String s = d.serialize();
         Dataset out = Dataset.newSerializedInstance(s);
         assertEquals("dataset contents", "age:  64\n" +
                 "child:\n" +
                 "    name: Bob\n" +
                 "name: Alice\n", out.toString());
     }
 
     public void test_add() {
         d.add("a", 2);
         d.add("a", 1);
 
         ArrayList<Integer> list = d.getIntList("a");
         assertEquals("length", 2, list.size());
         assertEquals("first", 2, (int) list.get(0));
         assertEquals("second", 1, (int) list.get(1));
     }
 
     public void test_addPath() {
         d.addPath("b.a", 2);
         d.addPath("b.a", 3);
 
         ArrayList<Integer> list = d.getDataset("b").getIntList("a");
         assertEquals("length", 2, list.size());
         assertEquals("first", 2, (int) list.get(0));
         assertEquals("second", 3, (int) list.get(1));
     }
 
     public void test_addSerializedData_missingOpenParen() {
         boolean gotException = false;
         try {
             Dataset out = new Dataset();
             out.addSerializedData("xyz", 0);
         }
         catch (Dataset.SyntaxError e) {
             assertEquals("exception message",
                     "syntax error in dataset: serialized dataset didn't " +
                     "start with \"(\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_addSerializedData_pastEndOfString() {
         boolean gotException = false;
         try {
             Dataset out = new Dataset();
             out.addSerializedData("(((", 3);
         }
         catch (Dataset.SyntaxError e) {
             assertEquals("exception message",
                     "syntax error in dataset: serialized dataset didn't " +
                     "start with \"(\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_addSerializedData_emptyInput() {
         Dataset out = new Dataset("weight", "125");
         assertEquals("return value", 2, out.addSerializedData("()", 0));
         assertEquals("contents of dataset", "weight: 125\n", out.toString());
     }
     public void test_addSerializedData_missingValue() {
         boolean gotException = false;
         try {
             Dataset out = new Dataset();
             out.addSerializedData("(4.name", 0);
         }
         catch (Dataset.SyntaxError e) {
             assertEquals("exception message",
                     "syntax error in dataset: no value for element " +
                     "\"name\" in serialized dataset",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_addSerializedData_singleNestedDataset() {
         String source = "(3.age2.24\n" +
                 "5.child(4.name5.Alice\n" +
                 "3.age3.100)\n" +
                 "9.eye color5.brown)";
         Dataset out = new Dataset();
         out.addSerializedData(source, 0);
         assertEquals("contents of dataset",
                 "age:       24\n" +
                 "child:\n" +
                 "    age:  100\n" +
                 "    name: Alice\n" +
                 "eye color: brown\n", out.toString());
     }
     public void test_addSerializedData_parensInValue() {
         String source = "(3.age2.24\n" +
                 "5.child16.(4.name5.Alice\n" +
                 "))";
         Dataset out = new Dataset();
         out.addSerializedData(source, 0);
         assertEquals("contents of dataset",
                 "age:   24\n" +
                 "child: \"(4.name5.Alice\\n)\"\n", out.toString());
     }
     public void test_addSerializedData_dataEndsInChildren() {
         boolean gotException = false;
         try {
             Dataset out = new Dataset();
             out.addSerializedData("(5.child(1.a2.xx)", 0);
         }
         catch (Dataset.SyntaxError e) {
             assertEquals("exception message",
                     "syntax error in dataset: serialized dataset not " +
                     "terminated by \")\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_addSerializedData_missingCloseParen() {
         boolean gotException = false;
         try {
             Dataset out = new Dataset();
             out.addSerializedData("(4.name5.Alice", 0);
         }
         catch (Dataset.SyntaxError e) {
             assertEquals("exception message",
                     "syntax error in dataset: serialized dataset not " +
                     "terminated by \")\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 
     public void test_addSerializedData_noStartArgument() {
         Dataset out = new Dataset("weight", "125");
         out.addSerializedData("()");
         assertEquals("contents of dataset", "weight: 125\n", out.toString());
     }
 
     public void test_check() {
         d.set("a", "b");
         assertEquals("key present", "b", d.check("a"));
         assertEquals("key present found val", true, d.found);
         assertEquals("no such key", null, d.check("b"));
         assertEquals("no such key found val", false, d.found);
     }
 
     public void test_checkBool() {
         d.set("d", true);
         assertEquals("key present", true, d.checkBool("d"));
         assertEquals("key present found val", true, d.found);
         assertEquals("no such key", false, d.checkBool("a"));
         assertEquals("no such key found val", false, d.found);
     }
 
     public void test_checkDataset() {
         Dataset d2 = new Dataset();
         d.set("f", d2);
         assertEquals("key present", d2, d.checkDataset("f"));
         assertEquals("key present found val", true, d.found);
         assertEquals("no such key", null, d.checkDataset("a"));
         assertEquals("no such key found val", false, d.found);
     }
     public void test_checkDouble() {
         d.set("c", 5.1);
         assertEquals("key present", 5.1, d.checkDouble("c"));
         assertEquals("key present found val", true, d.found);
         assertEquals("no such key", Double.MIN_VALUE, d.checkDouble("a"));
         assertEquals("no such key found val", false, d.found);
     }
 
     public void test_checkInt() {
         d.set("b", 5);
         assertEquals("key present", 5, d.checkInt("b"));
         assertEquals("key present found val", true, d.found);
         assertEquals("no such key", Integer.MIN_VALUE, d.checkInt("a"));
         assertEquals("no such key found val", false, d.found);
     }
 
     public void test_checkString() {
         d.set("e", "hi");
         assertEquals("key present", "hi", d.checkString("e"));
         assertEquals("key present found val", true, d.found);
         assertEquals("no such key", null, d.checkString("a"));
         assertEquals("no such key found val", false, d.found);
     }
 
     public void test_clear() {
         Dataset d = YamlDataset.newStringInstance(
                 "a: a_value\nb: b_value\nnested:\n  x: x_value\n");
         d.clear();
         assertEquals("contents of cleared dataset", "", d.toString());
     }
 
     public void test_clone_basics() {
         Dataset d = YamlDataset.newStringInstance(
                 "a: a_value\n" +
                 "b: b_value\n" +
                 "level1:\n" +
                 "  level2:\n" +
                 "    c: c_value\n" +
                 "    d: d_value\n" +
                 "list:\n" +
                 "  - name: Alice\n" +
                 "  - name: Bill\n" +
                 "  - name: Carol\n" +
                 "    age: 28\n");
         Dataset clone = d.clone();
 
         // Modify the original dataset and make sure that the
         // modifications don't affect the clone.
         d.set("b", "111");
         d.getDataset("level1.level2").set("d", "222");
         d.getDataset("list").set("name", "333");
         d.getDatasetList("list").get(2).set("age", "444");
         assertEquals("cloned dataset",
                 "a: a_value\n" +
                 "b: b_value\n" +
                 "level1:\n" +
                 "    level2:\n" +
                 "        c: c_value\n" +
                 "        d: d_value\n" +
                 "list:\n" +
                 "  - name: Alice\n" +
                 "  - name: Bill\n" +
                 "  - age:  28\n" +
                 "    name: Carol\n", clone.toString());
     }
 
     public void test_clone_dest() {
         Dataset d1 = new Dataset("a", 1, "b", 2);
         Dataset d2 = new Dataset("a", 3, "c", 4);
 
         d2.clone(d1);
 
         assertEquals(d1.toString(), new Dataset("a", 3, "b", 2, "c", 4).toString());
     }
 
     public void test_clone_copyFileName() {
         Dataset d = new Dataset("name", "Alice");
         d.fileName = "xyzzy";
         Dataset clone = d.clone();
         assertEquals("file name in clone", "xyzzy", clone.fileName);
     }
 
     public void test_containsKey() {
         Dataset d = new Dataset("first", "abc", "second", "def");
         assertEquals("first value exists", true, d.containsKey("first"));
         assertEquals("second value exists", true, d.containsKey("second"));
         assertEquals("third value doesn't exist", false,
                 d.containsKey("third"));
     }
 
     public void test_copyFrom() {
         Dataset source = YamlDataset.newStringInstance(
                 "a:\n" +
                 "  name: Alice\n" +
                 "  age: 24\n" +
                 "c: xyzzy\n");
         Dataset dest = YamlDataset.newStringInstance(
                 "a: a_value\n" +
                 "b: b_value\n" +
                 "c:\n" +
                 "    d: d_value\n");
         dest.copyFrom(source);
 
         // Modify the source dataset and make sure that the
         // modifications don't affect the destination.
         source.getDataset("a").set("name", "333");
         assertEquals("cloned dataset",
                 "a:\n" +
                 "    age:  24\n" +
                 "    name: Alice\n" +
                 "b: b_value\n" +
                 "c: xyzzy\n", dest.toString());
     }
 
     public void test_delete() {
         Dataset d = new Dataset("first", "12345", "second", "6789");
         d.delete("first");
         assertEquals("dataset contents", "second: 6789\n", d.toString());
     }
 
     public void test_delete_path() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  level2:\n" +
                 "    first: 1\n" +
                 "    second: 2\n");
         d.delete("level1.level2.first");
         assertEquals("dataset contents", "level1:\n" +
                 "    level2:\n" +
                 "        second: 2\n", d.toString());
     }
     public void test_delete_pathNonexistentPath() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  level2:\n" +
                 "    first: 1\n" +
                 "    second: 2\n");
         d.delete("level3.level4.first");
         d.delete("level1.level2.bogus");
         assertEquals("dataset contents", "level1:\n" +
                 "    level2:\n" +
                 "        first:  1\n" +
                 "        second: 2\n", d.toString());
     }
 
     public void test_get() {
         Object o = new Object();
         d.set("a", o);
 
         assertEquals(o, d.get("a"));
 
         try {
             d.get("bogus");
             fail("Exception not thrown");
         } catch (Dataset.MissingValueError e) {
             assertEquals("couldn't find dataset element \"bogus\"",
                          e.getMessage());
         }
     }
 
     public void test_getBool() {
         d.set("d", true);
         assertEquals("bool value", true, d.getBool("d"));
 
         d.set("e", 0);
         assertEquals("int value", false, d.getBool("e"));
 
         try {
             d.get("bogus");
             fail("Exception not thrown for bogus key");
         } catch (Dataset.MissingValueError e) {
             assertEquals("couldn't find dataset element \"bogus\"",
                          e.getMessage());
         }
 
         try {
             d.set("a", new Dataset("a", "b"));
             d.getBool("a");
             fail("Except not thrown for bad conversion");
         } catch (Dataset.InvalidConversionError e) {
             assertEquals("couldn't convert object of class \"org.fiz.Dataset\" and " +
                          "value \"a: b\n\" to class \"boolean\"", e.getMessage());
         }
     }
 
     public void test_getDataset() {
         Dataset nested = new Dataset();
         d.set("h", nested);
 
         assertEquals(nested, d.getDataset("h"));
 
         try {
             d.get("bogus");
             fail("Exception not thrown for bogus key");
         } catch (Dataset.MissingValueError e) {
             assertEquals("couldn't find dataset element \"bogus\"",
                          e.getMessage());
         }
 
         try {
             d.set("a", 5);
             d.getDataset("a");
             fail("Except not thrown for bad conversion");
         } catch (Dataset.InvalidConversionError e) {
             assertEquals("couldn't convert object of class \"java.lang.Integer\" and " +
                          "value \"5\" to class \"Dataset\"", e.getMessage());
         }
     }
 
     public void test_getDouble() {
         d.set("c", 5.2);
         assertEquals("double value", 5.2, d.getDouble("c"));
 
         d.set("b", "1.6");
         assertEquals("string value", 1.6, d.getDouble("b"));
 
         try {
             d.get("bogus");
             fail("Exception not thrown for bogus key");
         } catch (Dataset.MissingValueError e) {
             assertEquals("couldn't find dataset element \"bogus\"",
                          e.getMessage());
         }
 
         try {
             d.set("d", "abc");
             d.getDouble("d");
             fail("Except not thrown for bad conversion");
         } catch (Dataset.InvalidConversionError e) {
             assertEquals("couldn't convert object of class \"java.lang.String\" and " +
                          "value \"abc\" to class \"double\"", e.getMessage());
         }
     }
 
     public void test_getInt() {
         d.set("b", 5);
         assertEquals("int value", 5, d.getInt("b"));
 
         d.set("c", "6");
         assertEquals("string value", 6, d.getInt("c"));
 
         try {
             d.get("bogus");
             fail("Exception not thrown for bogus key");
         } catch (Dataset.MissingValueError e) {
             assertEquals("couldn't find dataset element \"bogus\"",
                          e.getMessage());
         }
 
         try {
             d.set("d", "abc");
             d.getInt("d");
             fail("Except not thrown for bad conversion");
         } catch (Dataset.InvalidConversionError e) {
             assertEquals("couldn't convert object of class \"java.lang.String\" and " +
                          "value \"abc\" to class \"int\"", e.getMessage());
         }
     }
 
     public void test_getString() {
         d.set("f", "xyz");
         assertEquals("string value", "xyz", d.getString("f"));
 
         d.set("g", false);
         assertEquals("string value", "false", d.getString("g"));
 
         try {
             d.get("bogus");
             fail("Exception not thrown for bogus key");
         } catch (Dataset.MissingValueError e) {
             assertEquals("couldn't find dataset element \"bogus\"",
                          e.getMessage());
         }
     }
 
     public void test_getList() {
         d.add("a", "x");
         d.add("a", 1);
 
         ArrayList<Object> list = d.getList("a");
         assertEquals("length", 2, list.size());
         assertEquals("first", "x", list.get(0));
         assertEquals("second", 1, (int) ((Integer) list.get(1)));
 
         assertEquals("empty array", 0, d.getList("b").size());
     }
 
     public void test_getBoolList() {
         d.add("c", true);
         d.add("c", "false");
 
         ArrayList<Boolean> list = d.getBoolList("c");
         assertEquals("length", 2, list.size());
         assertEquals("first", true, (boolean) list.get(0));
         assertEquals("second", false, (boolean) list.get(1));
 
         assertEquals("empty array", 0, d.getBoolList("b").size());
 
         try {
             d.set("c", new Dataset("a", "b"));
             d.getBoolList("c");
             fail("Except not thrown for bad conversion");
         } catch (Dataset.InvalidConversionError e) {
             assertEquals("couldn't convert object of class \"org.fiz.Dataset\" and " +
                          "value \"a: b\n\" to class \"boolean\"", e.getMessage());
         }
     }
 
     public void test_getDatasetList() {
         Dataset a = new Dataset();
         Dataset b = new Dataset();
         d.add("e", a);
         d.add("e", b);
 
         ArrayList<Dataset> list = d.getDatasetList("e");
         assertEquals("length", 2, list.size());
         assertEquals("first", a, list.get(0));
         assertEquals("second", b, list.get(1));
 
         assertEquals("empty array", 0, d.getDatasetList("b").size());
 
         try {
             d.set("e", 5);
             d.getDatasetList("e");
             fail("Except not thrown for bad conversion");
         } catch (Dataset.InvalidConversionError e) {
             assertEquals("couldn't convert object of class \"java.lang.Integer\" and " +
                          "value \"5\" to class \"Dataset\"", e.getMessage());
         }
     }
 
 
     public void test_getDoubleList() {
         d.add("f", 5.2);
         d.add("f", "3.5");
 
         ArrayList<Double> list = d.getDoubleList("f");
         assertEquals("length", 2, list.size());
         assertEquals("first", 5.2, (double) list.get(0));
         assertEquals("second", 3.5, (double) list.get(1));
 
         assertEquals("empty array", 0, d.getDoubleList("a").size());
 
         try {
             d.set("f", "hi");
             d.getDoubleList("f");
             fail("Except not thrown for bad conversion");
         } catch (Dataset.InvalidConversionError e) {
             assertEquals("couldn't convert object of class \"java.lang.String\" and " +
                          "value \"hi\" to class \"double\"", e.getMessage());
         }
     }
 
     public void test_getIntList() {
         d.add("b", 5);
         d.add("b", "3");
 
         ArrayList<Integer> list = d.getIntList("b");
         assertEquals("length", 2, list.size());
         assertEquals("first", 5, (int) list.get(0));
         assertEquals("second", 3, (int) list.get(1));
 
         assertEquals("empty array", 0, d.getIntList("c").size());
 
         try {
             d.set("b", "hi");
             d.getIntList("b");
             fail("Except not thrown for bad conversion");
         } catch (Dataset.InvalidConversionError e) {
             assertEquals("couldn't convert object of class \"java.lang.String\" and " +
                          "value \"hi\" to class \"int\"", e.getMessage());
         }
     }
 
     public void test_getStringList() {
         d.add("d", "y");
         d.add("d", "2");
 
         ArrayList<String> list = d.getStringList("d");
         assertEquals("length", 2, list.size());
         assertEquals("first", "y", list.get(0));
         assertEquals("second", "2", list.get(1));
 
         assertEquals("empty array", 0, d.getStringList("b").size());
     }
 
     public void test_serialize_withOut() {
         StringBuilder out = new StringBuilder("abc");
         Dataset d = new Dataset("first", "12345");
         d.serialize(out);
         assertEquals("result", "abc(5.first5.12345)", out.toString());
     }
 
     public void test_serialize_noArgs() {
         Dataset.sortOutput = true;
         Dataset d = new Dataset("first", "12345", "second", "x");
         assertEquals("result", "(5.first5.12345\n" +
                 "6.second1.x)", d.serialize());
     }
 
     public void test_serialize_basics() {
         Dataset.sortOutput = true;
         Dataset d = new Dataset("name", "Alice", "age", "36",
                 "child", new Dataset("name", "Bill"));
         assertEquals("serialized result", "(3.age2.36\n" +
                 "5.child(4.name4.Bill)\n" +
                 "4.name5.Alice)", d.serialize());
     }
     public void test_serialize_listOfChildren() {
         Dataset.sortOutput = true;
         Dataset d = new Dataset("child", new Dataset("name", "Bill"));
         d.add("child", new Dataset("name", "Carol"));
        d.add("child", new Dataset("name", "David"));
         assertEquals("serialized result",
                "(5.child(4.name4.Bill)(4.name5.Carol)(4.name5.David))",
                 d.serialize());
     }
     public void test_serialize_emptyDataset() {
         Dataset d = new Dataset();
         assertEquals("serialized result", "()",
                 d.serialize());
     }
 
     public void test_toJavascript_basics() {
         Dataset.sortOutput = true;
         StringBuilder out = new StringBuilder("var obj = ");
         Dataset d = new Dataset("name", "Alice", "age", 32);
         d.toJavascript(out);
         assertEquals("generated Javascript",
                 "var obj = {age: 32, name: \"Alice\"}",
                 out.toString());
     }
 
     public void test_toJavascript_emptyDataset() {
         Dataset.sortOutput = true;
         StringBuilder out = new StringBuilder("var obj = ");
         Dataset d = new Dataset();
         d.toJavascript(out);
         assertEquals("generated Javascript", "var obj = {}", out.toString());
     }
     public void test_toJavascript_debugEnabled() {
         Dataset.sortOutput = true;
         StringBuilder out = new StringBuilder();
         Dataset d = YamlDataset.newStringInstance(
                 "  d: Third\n" +
                 "  a: First\n" +
                 "  c:\n" +
                 "    ee: First\n" +
                 "    nn: Third\n" +
                 "    ss: Fifth\n" +
                 "    pp: Fourth\n" +
                 "    kk: Second\n");
         d.toJavascript(out);
         assertEquals("generated Javascript",
                 "{a: \"First\", c: {ee: \"First\", kk: \"Second\", " +
                 "nn: \"Third\", pp: \"Fourth\", ss: \"Fifth\"}, " +
                 "d: \"Third\"}",
                 out.toString());
     }
 
     public void test_set() {
         d.set("a", 1);
         d.set("a", 2);
 
         assertEquals(2, d.getInt("a"));
     }
 
     public void test_setPath() {
         d.setPath("a.b", "x");
         d.setPath("a.b", "y");
 
         assertEquals("y", d.getDataset("a").getString("b"));
     }
 
     public void test_toJavascript_nestedDatasets() {
         Dataset.sortOutput = true;
         StringBuilder out = new StringBuilder();
         Dataset d = YamlDataset.newStringInstance("child:\n" +
                 "  name: Alice\n" +
                 "  child:\n" +
                 "    name: Bill\n" +
                 "    age: 16\n");
         d.toJavascript(out);
         assertEquals("generated Javascript",
                 "{child: {child: {age: 16, name: \"Bill\"}, " +
                 "name: \"Alice\"}}",
                 out.toString());
     }
 
     public void test_toJavascript_listOfChildren() {
         Dataset.sortOutput = true;
         StringBuilder out = new StringBuilder();
         Dataset d = YamlDataset.newStringInstance("child:\n" +
                 "  - name: Alice\n" +
                 "  - name: Bill\n" +
                 "  - name: Carol\n" +
                 "    age: 16\n");
         d.toJavascript(out);
         assertEquals("generated Javascript",
                 "{child: [{name: \"Alice\"}, {name: \"Bill\"}, " +
                 "{age: 16, name: \"Carol\"}]}",
                 out.toString());
     }
 
     public void test_toJavascript_quoteStringCharacters() {
         Dataset.sortOutput = true;
         StringBuilder out = new StringBuilder();
         Dataset d = new Dataset("value", "xyz&<\n\"\'\0");
         d.toJavascript(out);
         assertEquals("generated Javascript",
                 "{value: \"xyz&<\\n\\\"'\\x00\"}",
                 out.toString());
     }
 
     public void test_toString() {
         Dataset d = new Dataset("a", new Dataset("b", "c"));
         assertEquals(d.toString(), YamlDataset.writeString(d));
     }
 
     public void test_writeFile() {
         boolean gotException = false;
         Dataset d = new Dataset();
         try {
             d.writeFile("foo", "simple comment");
         }
         catch (Dataset.UnknownFileFormatError e) {
             assertEquals("exception message",
                     "class Dataset doesn't know how to write datasets " +
                     "to files",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 
     public void test_appendValue_null() {
         assertEquals("hi", Dataset.appendValue(null, "hi"));
     }
 
     public void test_appendValue_object() {
         ArrayList ret = (ArrayList) Dataset.appendValue("hi", "bye");
         assertEquals("[hi, bye]", ret.toString());
     }
 
     public void test_appendValue_array() {
         ArrayList array = new Dataset.DSArrayList();
         array.add("hi");
         array.add("bye");
         ArrayList ret = (ArrayList) Dataset.appendValue(array, "hello");
         assertEquals("[hi, bye, hello]", ret.toString());
     }
 
     // no tests needed for toString
     public void test_getEncodedString_missingDot() {
         boolean gotException = false;
         try {
             IntBox end = new IntBox();
             Dataset.getEncodedString("x.144", 2, end);
         }
         catch (Dataset.SyntaxError e) {
             assertEquals("exception message",
                     "syntax error in dataset: missing \".\" in " +
                     "serialized dataset",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_getEncodedString_computeLength() {
         IntBox end = new IntBox();
         String s = Dataset.getEncodedString("23.aaaaabbbbbcccccdddddeeeeefffff",
                 0, end);
         assertEquals("end index", 26, end.value);
         assertEquals("string value", "aaaaabbbbbcccccdddddeee", s);
     }
     public void test_getEncodedString_negativeLength() {
         boolean gotException = false;
         try {
             IntBox result = new IntBox();
             Dataset.getEncodedString("19999999999.", 0, result);
         }
         catch (Dataset.SyntaxError e) {
             assertEquals("exception message",
                     "syntax error in dataset: serialized dataset has " +
                     "improper length \"19999999999\"",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
     public void test_getEncodedString_lengthTooBig() {
         boolean gotException = false;
         try {
             IntBox end = new IntBox();
             Dataset.getEncodedString("6.12345", 0, end);
         }
         catch (Dataset.SyntaxError e) {
             assertEquals("exception message",
                     "syntax error in dataset: unexpected end of " +
                     "serialized dataset",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 
     public void test_lookup() {
         Dataset d = new Dataset("a", "b", "c", new Dataset("d", "e"),
                                 "f", "g", "f", "h");
 
         assertEquals("bogus value, all", 0,
                      ((ArrayList)d.lookup("bogus", Dataset.Quantity.ALL)).size());
         assertEquals("list of values, all", "[g, h]",
                      d.lookup("f", Dataset.Quantity.ALL).toString());
         assertEquals("one value, all", "[b]",
                      d.lookup("a", Dataset.Quantity.ALL).toString());
         assertEquals("bogus value, first", null,
                      d.lookup("bogus", Dataset.Quantity.FIRST_ONLY));
         assertEquals("list of values, first", "g",
                      d.lookup("f", Dataset.Quantity.FIRST_ONLY));
         assertEquals("one value, first", "b",
                      d.lookup("a", Dataset.Quantity.FIRST_ONLY));
         assertEquals("path", "e", d.lookup("c.d", Dataset.Quantity.FIRST_ONLY));
     }
 
     public void test_lookupParent_noDotsInPath() {
         Dataset d = new Dataset("name", "value1");
         Dataset.ParentInfo info = d.lookupParent("xyz", false);
         assertEquals("last name", "xyz", info.lastName);
         assertEquals("contents of parent", "value1", info.parent.get("name"));
     }
 
     public void test_lookupParent_multipleNamesInPath() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  level2:\n" +
                 "    level3:\n" +
                 "      name: Alice\n");
         Dataset.ParentInfo info = d.lookupParent("level1.level2.level3.foo",
                 false);
         assertEquals("last name", "foo", info.lastName);
         assertEquals("contents of parent", "Alice", info.parent.get("name"));
     }
 
     public void test_lookupParent_listInPath() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  level2:\n" +
                 "    - name: Alice\n" +
                 "    - name: Bob\n" +
                 "    - name: Carol\n");
         Dataset.ParentInfo info = d.lookupParent("level1.level2.foo",
                 true);
         assertEquals("last name", "foo", info.lastName);
         assertEquals("contents of parent", "Alice", info.parent.get("name"));
     }
 
     public void test_lookupParent_missingAncestors_noCreate() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  name: Alice\n");
         Dataset.ParentInfo info = d.lookupParent("level1.level2.level3.foo",
                 false);
         assertEquals("return value", null, info);
     }
     public void test_lookupParent_stringInPath_noCreate() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  level2: string_value\n");
         Dataset.ParentInfo info = d.lookupParent("level1.level2.level3.foo",
                 false);
         assertEquals("return value", null, info);
     }
     @SuppressWarnings("unchecked")
     public void test_lookupParent_missingAncestors_create() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  name: Alice\n");
         Dataset.ParentInfo info = d.lookupParent("level1.level2.level3.foo",
                 true);
         assertEquals("last name", "foo", info.lastName);
 
         // Add a unique value to the parent map, then check that this value
         // appears in the right place in the overall dataset.
         info.parent.set("value", "12345");
         assertEquals("overall dataset contents", "level1:\n" +
                 "    level2:\n" +
                 "        level3:\n" +
                 "            value: 12345\n" +
                 "    name: Alice\n", d.toString());
     }
     @SuppressWarnings("unchecked")
     public void test_lookupParent_stringInPath_create() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  level2: Alice\n");
         Dataset.ParentInfo info = d.lookupParent("level1.level2.level3.foo",
                 true);
         assertEquals("last name", "foo", info.lastName);
 
         // Add a unique value to the parent map, then check that this value
         // appears in the right place in the overall dataset.
         info.parent.set("value", "12345");
         assertEquals("overall dataset contents", "level1:\n" +
                 "    level2:\n" +
                 "        level3:\n" +
                 "            value: 12345\n", d.toString());
     }
 
     public void test_lookupPath_sanityCheck() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  - level2: Alice\n" +
                 "  - level2: Bill\n");
         Dataset.DSArrayList<Object> results =new Dataset.DSArrayList<Object>();
         d.lookupPath("a.level1.level2", 2,
                 Dataset.Quantity.ALL, results);
         assertEquals("string value", "Alice, Bill",
                 StringUtil.join(results, ", "));
     }
     public void test_lookupPath_noSuchPath() {
         Dataset d = YamlDataset.newStringInstance(
                 "city: San Francisco\n");
         assertEquals("result is null", null, d.lookupPath("a.state", 2,
                 Dataset.Quantity.FIRST_ONLY,
                 null));
     }
     public void test_lookupPath_lastElementInPath() {
         Dataset d = YamlDataset.newStringInstance(
                 "city: San Francisco\n");
         Dataset.DSArrayList<Object> results = new Dataset.DSArrayList<Object>();
         Object out = d.lookupPath("a.city", 2,
                 Dataset.Quantity.FIRST_ONLY,
                 null);
         assertEquals("type of result", "String",
                 out.getClass().getSimpleName());
         assertEquals("result value", "San Francisco", (String) out);
     }
     public void test_lookupPath_nextObjectIsHashMap() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  level2: Alice\n");
         assertEquals("string value", "Alice",
                 d.lookupPath("level1.level2", 0,
                 Dataset.Quantity.FIRST_ONLY, null));
     }
     public void test_lookupPath_nextObjectIsArrayList_firstOnly() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  - level2: Alice\n" +
                 "  - level2: Bob\n" +
                 "  - level2: Carol\n");
         Object out = d.lookupPath("level1.level2", 0,
                 Dataset.Quantity.FIRST_ONLY, null);
         assertEquals("type of result", "String",
                 out.getClass().getSimpleName());
         assertEquals("string value", "Alice", (String) out);
     }
     public void test_lookupPath_nextObjectIsArrayList_collectMany() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  - level2: Alice\n" +
                 "  - level2: Bob\n" +
                 "  - level2: Carol\n" +
                 "  - state: California\n" +
                 "    level2:\n" +
                 "      age: 48\n" +
                 "      weight:182\n");
         Object out = d.lookupPath("level1.level2", 0,
                 Dataset.Quantity.ALL, null);
         assertEquals("type of result", "DSArrayList",
                 out.getClass().getSimpleName());
         assertEquals("string value", "Alice, Bob, Carol, age:    48\n" +
                 "weight: 182\n",
                 StringUtil.join((ArrayList) out, ", "));
     }
     public void test_lookupPath_nextObjectIsArrayList_useExistingList() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  - level2: Alice\n" +
                 "  - level2: Bob\n" +
                 "  - level2: Carol\n");
         Dataset.DSArrayList<Object> out = new Dataset.DSArrayList<Object>();
         out.add("Connecticut");
         d.lookupPath("level1.level2", 0,
                 Dataset.Quantity.ALL, out);
         assertEquals("string value", "Connecticut, Alice, Bob, Carol",
                 StringUtil.join((ArrayList) out, ", "));
     }
     public void test_lookupPath_nextObjectIsArrayList_foundNothing() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1:\n" +
                 "  - level2: Alice\n" +
                 "  - level2: Bob\n" +
                 "  - level2: Carol\n");
         assertEquals("result is null", null,
                      d.lookupPath("level1.bogus", 0,
                 Dataset.Quantity.ALL, null));
     }
     public void test_lookupPath_nextObjectIsString() {
         Dataset d = YamlDataset.newStringInstance(
                 "level1: value\n");
         assertEquals("result is null", null,
                 d.lookupPath("level1.bogus", 0,
                 Dataset.Quantity.ALL, null));
     }
 
 }
