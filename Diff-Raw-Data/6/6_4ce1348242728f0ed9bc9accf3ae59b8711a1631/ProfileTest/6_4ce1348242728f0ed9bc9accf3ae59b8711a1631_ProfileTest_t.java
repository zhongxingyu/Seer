 /*
  * Copyright (c) 2012 Alvin R. de Leon. All Rights Reserved.
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
 
 package org.piraso.ui.sql;
 
 import org.piraso.api.AbstractJacksonTest;
 import org.piraso.api.GeneralPreferenceEnum;
 import org.piraso.api.Preferences;
 import org.piraso.api.sql.SQLPreferenceEnum;
 import org.piraso.ui.api.NewContextMonitorModel;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Used for profile creation
  */
 public class ProfileTest extends AbstractJacksonTest {
 
     @Test
     public void testJackson() throws Exception {
         NewContextMonitorModel expected = new NewContextMonitorModel();
 
         expected.setLoggingUrl("http://127.0.0.1/piraso/logging");
         expected.setDesc("Profile for checking SQL Queries and retrieved Data.");
 
         Preferences preferences = new Preferences();
 
         preferences.addProperty(GeneralPreferenceEnum.SCOPE_ENABLED.getPropertyName(), true);
         //preferences.addProperty(GeneralPreferenceEnum.STACK_TRACE_ENABLED.getPropertyName(), true);
         preferences.addProperty(SQLPreferenceEnum.CONNECTION_ENABLED.getPropertyName(), true);
         //preferences.addProperty(SQLPreferenceEnum.CONNECTION_METHOD_CALL_ENABLED.getPropertyName(), true);
         preferences.addProperty(SQLPreferenceEnum.PREPARED_STATEMENT_ENABLED.getPropertyName(), true);
         preferences.addProperty(SQLPreferenceEnum.VIEW_SQL_ENABLED.getPropertyName(), true);
         //preferences.addProperty(SQLPreferenceEnum.PREPARED_STATEMENT_METHOD_CALL_ENABLED.getPropertyName(), true);
         preferences.addProperty(SQLPreferenceEnum.RESULTSET_ENABLED.getPropertyName(), true);
 
         expected.setPreferences(preferences);
 
         String str = mapper.writeValueAsString(expected);
         NewContextMonitorModel actual = mapper.readValue(str, NewContextMonitorModel.class);
 
         assertEquals(expected, actual);
 
         System.out.println("JSON: " + str);
         System.out.println("REPLACE: " + str.replaceAll("\"", "&quot;"));
     }
 
     @Test
     public void testSQLFormat() throws Exception {
        System.out.println(new SQLFormatter().format("select * from dog"));
     }
 }
