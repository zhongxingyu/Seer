 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.chime.data;
 
 import com.paxxis.chime.client.common.Cursor;
 import com.paxxis.chime.client.common.InstanceId;
 import com.paxxis.chime.client.common.User;
 import com.paxxis.chime.client.common.UserMessage;
 import com.paxxis.chime.client.common.UserMessagesBundle;
 import com.paxxis.chime.database.DataSet;
 import com.paxxis.chime.database.DatabaseConnection;
 import com.paxxis.chime.database.IDataValue;
 import com.paxxis.chime.service.Tools;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  *
  * @author Robert Englander
  */
 public class UserMessageUtils {
     private static final int ROWLIMIT = 1000;
 
     private UserMessageUtils() {
     }
 
     public static User deleteMessages(User user, User userInstance, List<UserMessage> list, DatabaseConnection database) throws Exception {
         StringBuilder buffer = new StringBuilder("delete from Chime.MessageJournal where user_id = '"
                 + userInstance.getId().getValue() + "'");
 
         if (list != null && list.size() > 0) {
             String sep = "";
             buffer.append(" and id in (");
             for (UserMessage msg : list) {
                 buffer.append(sep).append("'").append(msg.getId().getValue()).append("'");
                 sep = ",";
             }
             buffer.append(")");
         }
 
         try {
             database.startTransaction();
             database.executeStatement(buffer.toString());
             database.commitTransaction();
         } catch (Exception e) {
             database.rollbackTransaction();
             throw e;
         }
 
         User result = UserUtils.getUserById(userInstance.getId(), user, database);
         return result;
     }
     
     public static UserMessagesBundle getMessages(User user, Cursor cursor, DatabaseConnection database) throws Exception {
 
         String sql = "SELECT * FROM Chime.MessageJournal where user_id = '" +
                         user.getId().getValue() + "' order by timestamp ";
 
         int start = 1;
         int end = 999999;
         int total = 0;
         if (cursor != null) {
             start = cursor.getFirst() + 1;
             end = start + cursor.getMax() - 1;
             sql += Tools.getLimitClause(database, ROWLIMIT);
         }
 
         List<UserMessage> results = new ArrayList<UserMessage>();
         int fetchNumber = start;
         boolean limited = false;
         DataSet dataSet = database.getDataSet(sql, true);
         boolean fetchAgain = dataSet.absolute(start);
         while (fetchAgain) {
             IDataValue id = dataSet.getFieldValue("id");
             IDataValue subject = dataSet.getFieldValue("subject");
             IDataValue message = dataSet.getFieldValue("message");
             IDataValue timestamp = dataSet.getFieldValue("timestamp");
             IDataValue seen = dataSet.getFieldValue("seen");
 
 
             UserMessage msg = new UserMessage(InstanceId.create(id.asString()), subject.asString(), message.asString(),
                     timestamp.asDate(), seen.asString().equals("Y"));
             results.add(msg);
 
             if (fetchNumber == end)
             {
                 fetchAgain = false;
             }
             else
             {
                 fetchAgain = dataSet.next();
             }
 
             if (!fetchAgain && cursor != null)
             {
                 dataSet.last();
                 total = dataSet.getRowNumber();
 
                 if (total > ROWLIMIT)
                 {
                     total = ROWLIMIT;
                     limited = true;
                 }
             }
 
             fetchNumber++;
         }
 
         dataSet.close();
 
         sql = "SELECT max(timestamp) latest FROM Chime.MessageJournal where user_id = '" +
                         user.getId().getValue() + "' ";
 
         dataSet = database.getDataSet(sql, true);
         Date latestUpdate = null;
         if (dataSet.next()) {
             latestUpdate = dataSet.getFieldValue("latest").asDate();
         }
 
        dataSet.close();

         // create the return cursor
         Cursor newCursor = null;
 
         if (cursor != null)
         {
             newCursor = new Cursor(cursor.getFirst(), results.size(), cursor.getMax(), total, limited);
         }
 
         return new UserMessagesBundle(results, newCursor, latestUpdate);
     }
 }
