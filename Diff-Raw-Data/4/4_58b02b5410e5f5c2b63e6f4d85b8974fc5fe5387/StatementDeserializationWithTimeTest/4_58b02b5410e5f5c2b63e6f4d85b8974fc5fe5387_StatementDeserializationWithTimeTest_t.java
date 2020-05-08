 /*
  * Copyright (c) 2013 Allogy Interactive.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.allogy.coffeecan.statements;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.util.UUID;
 
 import static com.allogy.coffeecan.statements.LoadFromFileHelper.loadResource;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 public class StatementDeserializationWithTimeTest
 {
     private ObjectMapper objectMapper;
     private DateTimeFormatter dateTimeFormatter;
 
     @Before
     public void setUp()
     {
         objectMapper = new ObjectMapper();
         dateTimeFormatter = ISODateTimeFormat.dateTime();
     }
 
     private DateTime getDateTime(String knownTimestampString)
     {
         return dateTimeFormatter.parseDateTime(knownTimestampString).
                 withZone(DateTimeZone.UTC);
     }
 
     @Test
     public void jackson_is_able_to_deserialize_time_values() throws IOException
     {
         InputStream jsonInputStream = loadResource("TinCanAPIExampleAccountWithTimes");
 
         Statement statement = objectMapper.readValue(jsonInputStream, Statement.class);
 
         assertThat(statement, notNullValue());
 
         assertThat(statement.getActor(), notNullValue());
         assertThat(statement.getVerb(), notNullValue());
         assertThat(statement.getObject(), notNullValue());
 
         String knownTimestampString = "2012-07-05T18:30:32.360Z";
         DateTime expectedTimestamp = getDateTime(knownTimestampString);
         String knownStoredString = "2012-07-05T18:30:33.540Z";
         DateTime expectedStored = getDateTime(knownStoredString);
 
         assertThat(statement.getTimestamp(), is(expectedTimestamp));
         assertThat(statement.getStored(), is(expectedStored));
     }
 
     @Test
     public void jackson_serializes_time_values_with_correct_format() throws IOException
     {
         String timestampString = "2012-07-05T18:30:32.360Z";
         DateTime timestamp = getDateTime(timestampString);
         String storedString = "2012-07-05T18:32:12.360Z";
         DateTime stored = getDateTime(storedString);
 
        Statement statement = new Statement(new Agent(), new Verb(UUID.randomUUID().toString()), new Activity(UUID.randomUUID().toString()));
         statement.setTimestamp(timestamp);
         statement.setStored(stored);
 
         String jsonString = objectMapper.writeValueAsString(statement);
         assertThat(jsonString, notNullValue());
 
         JsonNode rootNode = objectMapper.readTree(jsonString);
         assertThat(rootNode, notNullValue());
 
         JsonNode timestampNode = rootNode.get("timestamp");
         assertThat(timestampNode, notNullValue());
         assertThat(timestampNode.textValue(), is(timestampString));
 
         JsonNode storedNode = rootNode.get("stored");
         assertThat(storedNode, notNullValue());
         assertThat(storedNode.textValue(), is(storedString));
     }
 }
