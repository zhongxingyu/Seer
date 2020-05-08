 /**
  * Copyright 2013 Huining (Thomas) Feng (tfeng@berkeley.edu)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.bacoder.parser.javaproperties.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 
 import org.antlr.v4.runtime.ANTLRInputStream;
 import org.antlr.v4.runtime.BailErrorStrategy;
 import org.antlr.v4.runtime.CommonTokenStream;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.bacoder.parser.javaproperties.JavaPropertiesLexer;
 import com.bacoder.parser.javaproperties.JavaPropertiesParser;
 import com.bacoder.parser.javaproperties.JavaPropertiesParser.PropertiesContext;
 import com.bacoder.parser.javaproperties.adapter.PropertiesAdapter;
 import com.bacoder.parser.javaproperties.adapter.PropertiesAdapters;
 import com.bacoder.parser.javaproperties.api.KeyValue;
 import com.bacoder.parser.javaproperties.api.Properties;
 
 @Test
 public class TestSimpleProperties {
 
   public void testColons() {
     String input = "# Comment: colon\nkey1:value1\nkey2 :value2\nkey3: value3\nkey4 : value4\nkey5:value5:";
     Properties properties = parse(input);
 
     verify(properties, input);
 
     Assert.assertEquals(properties.getComments().size(), 1);
     Assert.assertEquals(properties.getComments().get(0).getText(), "# Comment: colon");
     Assert.assertEquals(properties.getComments().get(0).getStartLine(), 1);
     Assert.assertEquals(properties.getComments().get(0).getStartColumn(), 0);
     Assert.assertEquals(properties.getComments().get(0).getEndLine(), 1);
     Assert.assertEquals(properties.getComments().get(0).getEndColumn(), "# Comment: colon".length() - 1);
   }
 
   public void testEquals() {
     String input = "# Comment\nkey1=value1\nkey2 = value2\nkey3= value3\nkey4 =value4";
     Properties properties = parse(input);
 
     verify(properties, input);
 
     Assert.assertEquals(properties.getComments().size(), 1);
     Assert.assertEquals(properties.getComments().get(0).getText(), "# Comment");
     Assert.assertEquals(properties.getComments().get(0).getStartLine(), 1);
     Assert.assertEquals(properties.getComments().get(0).getStartColumn(), 0);
     Assert.assertEquals(properties.getComments().get(0).getEndLine(), 1);
     Assert.assertEquals(properties.getComments().get(0).getEndColumn(), "# Comment".length() - 1);
   }
 
   public void testEqualsWithSpaces() {
     String input = "  # Comment\n  key1 = value1 \n key2 = value2  \n";
     Properties properties = parse(input);
 
     verify(properties, input);
 
     Assert.assertEquals(properties.getComments().size(), 1);
     Assert.assertEquals(properties.getComments().get(0).getText(), "# Comment");
     Assert.assertEquals(properties.getComments().get(0).getStartLine(), 1);
     Assert.assertEquals(properties.getComments().get(0).getStartColumn(), 2);
     Assert.assertEquals(properties.getComments().get(0).getEndLine(), 1);
     Assert.assertEquals(properties.getComments().get(0).getEndColumn(), "# Comment".length() + 1);
   }
 
   public void testEqualsWithTabs() {
     String input = "\t\t#\tComment\n\t\tkey1\t=\tvalue1\t\n\tkey2\t=\tvalue2\t\t\n";
     Properties properties = parse(input);
 
     verify(properties, input);
 
     Assert.assertEquals(properties.getComments().size(), 1);
     Assert.assertEquals(properties.getComments().get(0).getText(), "#\tComment");
     Assert.assertEquals(properties.getComments().get(0).getStartLine(), 1);
     Assert.assertEquals(properties.getComments().get(0).getStartColumn(), 2);
     Assert.assertEquals(properties.getComments().get(0).getEndLine(), 1);
     Assert.assertEquals(properties.getComments().get(0).getEndColumn(), "#\tComment".length() + 1);
   }
 
   public void testSpaces() {
     String input = "key1 value1\nkey2  value2\nkey3\tvalue3\nkey4\t value4\n";
     Properties properties = parse(input);
 
     verify(properties, input);
 
     Assert.assertEquals(properties.getComments().size(), 0);
   }
 
   public void testSpecialCharacters() {
     String input = "key1 = value1\f\nkey2 = value2\\\f\f\\\t\nkey3 = value3\\\nkey4 = value4\nkey5\\\t\\\n\\\r\\\n = value5";
     Properties properties = parse(input);
     verify(properties, input);
   }
 
   public void testUnicode() {
     String input = "键1 = 值1\n键\\\t2 = 值2\n键\\\t3 = 值3\t 完 \nkey4 = val\\u1234ue4";
     Properties properties = parse(input);
     verify(properties, input);
   }
 
   public void testEmptyLines() {
     String input = "\n\n\r\n";
     Properties properties = parse(input);
     verify(properties, input);
   }
 
   public void testPoundSigns() {
     String input = "property#1 = value1\n  property\\#2 = value#2";
     Properties properties = parse(input);
     verify(properties, input);
   }
 
   public void testLineReturns() {
    String input = "property\\\n1 = value\\\n1\nproperty\\\n2 = value\\\n  2\nproperty\\\n  3 = value\\\n  3\nproperty4 = value\n   4";
     Properties properties = parse(input);
     verify(properties, input);
   }
 
   private JavaPropertiesParser getParser(String input) {
     JavaPropertiesLexer lexer = new JavaPropertiesLexer(new ANTLRInputStream(input));
     CommonTokenStream tokenStream = new CommonTokenStream(lexer);
     JavaPropertiesParser parser = new JavaPropertiesParser(tokenStream);
     parser.setErrorHandler(new BailErrorStrategy());
     return parser;
   }
 
   private Properties parse(String input) {
     JavaPropertiesParser parser = getParser(input);
     PropertiesContext context = parser.properties();
     PropertiesAdapters adapters = new PropertiesAdapters();
     Properties properties = adapters.getAdapter(PropertiesAdapter.class).adapt(context);
     return properties;
   }
 
   private void verify(Properties properties, String input) {
     java.util.Properties expected = new java.util.Properties();
     try {
       expected.load(
           new InputStreamReader(
               new ByteArrayInputStream(input.getBytes("UTF-8")), Charset.forName("UTF-8")));
     } catch (Exception e) {
       throw new RuntimeException("Unable to load properties" + e);
     }
 
     for (KeyValue keyValue : properties.getKeyValues()) {
       String key = keyValue.getKey().getSanitizedText();
       Assert.assertTrue(expected.containsKey(key),
           String.format("Key \"%s\" does not exist in expected Java properties", key));
       String value = keyValue.getValue().getSanitizedText();
       Assert.assertEquals(value, expected.get(key),
           String.format("Value \"%s\" does not match expected Java property value \"%s\"", value,
               expected.get(key)));
     }
 
     Assert.assertEquals(properties.getKeyValues().size(), expected.size());
   }
 }
