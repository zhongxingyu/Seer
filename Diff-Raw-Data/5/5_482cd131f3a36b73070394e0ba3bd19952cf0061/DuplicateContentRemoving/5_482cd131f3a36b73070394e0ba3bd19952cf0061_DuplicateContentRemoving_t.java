 /*
  * Copyright (C) 2000 - 2011 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of
  * the GPL, you may redistribute this Program in connection withWriter Free/Libre
  * Open Source Software ("FLOSS") applications as described in Silverpeas's
  * FLOSS exception.  You should have recieved a copy of the text describing
  * the FLOSS exception, and it is also available here:
  * "http://www.silverpeas.org/legal/licensing"
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.silverpeas.migration.contentmanagement;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import org.silverpeas.dbbuilder.Console;
 import org.silverpeas.dbbuilder.dbbuilder_dl.DbBuilderDynamicPart;
 
 /**
  * DB migration to remove any duplicate content instances in the database underlying at Silverpeas.
  * The table sb_contentmanager_content table can contain duplicate rows with the same
  * contentInstanceId and internalContentId columns value but with a different silverContentId column
  * value. This means some contents in Silverpeas are linked with more than one content object and
  * this shouldn't occur.
  */
 public class DuplicateContentRemoving extends DbBuilderDynamicPart {
 
   /**
    * SQL statement for querying the count of duplicated contents that are classified on the PdC.
    * Among the classified contents, one or more redundant instances of theses contents can be not
    * classified on the PdC. The Silverpeas contents are persisted into the sb_contentmanager_content
    * table. A content belongs to a Silverpeas component instance that is refered by the column
    * contentInstanceId in the sb_contentmanager_content table. The classification of the Silverpeas
    * contents are persisted into the sb_classifyengine_classify table.
    */
   private static final String DUPLICATE_CLASSIFIED_CONTENT_COUNT_QUERY =
       "select count(distinct c1.internalContentId) from sb_contentmanager_content c1 where "
           + "1 < (select count(c2.internalContentId) from sb_contentmanager_content c2 where "
           + "c2.contentInstanceId=c1.contentInstanceId and c2.internalContentId = c1.internalContentId) "
           + "and c1.silverContentId in (select objectid from sb_classifyengine_classify)";
   /**
    * SQL statement for querying the count of all duplicated contents. A content can be made up of
    * two or more redundant instances. The Silverpeas contents are persisted into the
    * sb_contentmanager_content table. A content belongs to a Silverpeas component instance that is
    * refered by the column contentInstanceId in the sb_contentmanager_content table.
    */
   private static final String DUPLICATE_CONTENT_COUNT_QUERY =
       "select count(distinct c1.internalContentId) from sb_contentmanager_content c1 where "
           + "1 < (select count(c2.internalContentId) from sb_contentmanager_content c2 where "
           + "c2.contentInstanceId=c1.contentInstanceId and c2.internalContentId = c1.internalContentId) ";
   /**
    * SQL statement for querying the redundant instances of duplicated contents. A content can be
    * made up of two or more redundant instances. The Silverpeas contents are persisted into the
    * sb_contentmanager_content table. A content belongs to a Silverpeas component instance that is
    * refered by the column contentInstanceId in the sb_contentmanager_content table. This statement
    * is used for querying redundant instances that weren't deleted by the two below SQL statements.
    */
   private static final String REDUNDANT_INSTANCE_OF_DUPLICATE_CONTENT_QUERY =
       "select c1.silverContentId, c1.internalContentId, c1.contentInstanceId from sb_contentmanager_content c1 where "
           + "1 < (select count(c2.internalContentId) from sb_contentmanager_content c2 where "
           + "c2.contentInstanceId=c1.contentInstanceId and c2.internalContentId = c1.internalContentId) "
           + "and c1.silverContentId < (select max(c3.silverContentId) from sb_contentmanager_content c3 where c1.contentInstanceId=c3.contentInstanceId and c1.internalContentId=c3.internalContentId and c1.silverContentId != c3.silverContentId)";
   /**
    * SQL statement for querying in the sb_contentmanager_content table all the unclassified
    * redundant instances of duplicate contents. Theses contents can have one instance that is
    * classified on the PdC; theses aren't fetched. Only the instance of duplicate content with the
    * higher silver content identifier (thus the more recent silver object registered) are kept as
    * the single valid content instance. Theses queried contents is for their deletion.
    */
   private static final String UNCLASSIFIED_REDUNDANT_CONTENT_INSTANCES_TO_DELETE =
       "select c1.silverContentId from "
           + "sb_contentmanager_content c1 where "
           + "1 < (select count(c2.internalContentId) from sb_contentmanager_content c2 where c2.contentInstanceId=c1.contentInstanceId and c2.internalContentId = c1.internalContentId) "
           + "and c1.silverContentId not in (select objectid from sb_classifyengine_classify)"
           + "and c1.silverContentId < (select max(c3.silverContentId) from sb_contentmanager_content c3 where c1.contentInstanceId=c3.contentInstanceId and c1.internalContentId=c3.internalContentId)";
   /**
    * SQL statement for querying in the sb_contentmanager_content table the unclassified redundant
    * instances of the duplicate contents that were not taken into account by the previous request.
    * Theses one are classified redundant instances with a lower silver content identifier. Theses
    * queried contents is for their deletion.
    */
   private static final String UNCLASSIFIED_REDUNDANT_CONTENT_INSTANCE_WITH_HIGHER_ID_TO_DELETE =
       "select c1.silverContentId from "
           + "sb_contentmanager_content c1 where "
           + "1 < (select count(c2.internalContentId) from sb_contentmanager_content c2 where c2.contentInstanceId=c1.contentInstanceId and c2.internalContentId = c1.internalContentId) "
           + "and c1.silverContentId not in (select objectid from sb_classifyengine_classify)";
   /**
    * SQL statement for deleting explicitly some given contents in the sb_contentmanager_content
    * table.
    */
   private static final String CONTENT_INSTANCE_DELETION = "delete from "
       + "sb_contentmanager_content where silverContentId in ({0})";
   /**
    * SQL statement for deleting explicitly the classification of some given contents in the
    * sb_classifyengine_classify table. This statement will be use in the exceptional case where two
    * instances of a duplicate content are classified; in this case, the redundant instance isn't
    * deleted by the above statement and it is then necessary to delete its classification before
    * deleting it.
    */
   private static final String CONTENT_INSTANCE_CLASSIFICATION_DELETION = "delete from "
       + "sb_classifyengine_classify where objectId in ({0})";
 
   /**
    * Migrates the sb_contentmanager_content table by removing all duplicated Silverpeas contents
    * @throws Exception if an error occurs while migrating the sb_contentmanager_content table.
    */
   public void migrate() throws Exception {
     Console console = getConsole();
     if (console == null) {
       console = new Console();
     }
     Connection connection = getConnection();
     boolean autocommit = connection.getAutoCommit();
     if (autocommit) {
       connection.setAutoCommit(false);
     }
 
     int duplicateContentCount = executeQuery(DUPLICATE_CONTENT_COUNT_QUERY).get(0);
     String duplicateContents = "Number of duplicate content: " + duplicateContentCount;
     console.printMessageln(duplicateContents);
     System.out.println();
     System.out.println(duplicateContents);
 
     int classifiedContents = executeQuery(DUPLICATE_CLASSIFIED_CONTENT_COUNT_QUERY).get(0);
     console.printMessageln("Number of duplicate content that are classified on the PdC: "
         + classifiedContents);
 
     console.printMessageln(
         "Delete the unclassified redundant instances of duplicate contents");
     List<Integer> contentsToDelete =
         executeQuery(UNCLASSIFIED_REDUNDANT_CONTENT_INSTANCES_TO_DELETE);
     int deletedContents1 = executeDeletion(CONTENT_INSTANCE_DELETION, contentsToDelete);
     assertEquals(contentsToDelete.size(), deletedContents1);
     console.printMessageln("-> number of redundant instances deleted: " + deletedContents1);
 
     console.printMessageln(
         "Delete the rest of unclassified redundant instances of duplicate contents");
     contentsToDelete =
         executeQuery(UNCLASSIFIED_REDUNDANT_CONTENT_INSTANCE_WITH_HIGHER_ID_TO_DELETE);
     int deletedContents2 = executeDeletion(CONTENT_INSTANCE_DELETION, contentsToDelete);
     assertEquals(contentsToDelete.size(), deletedContents2);
     console.printMessageln("-> number of redundant instances deleted: " + deletedContents2);
 
     console.printMessageln(
         "Delete the exceptional redundant instances of duplicate classified content");
     int deletedContents3 = deleteRedundantClassifiedInstances();
     console.printMessageln("-> number of redundant instances deleted: " + deletedContents3);
 
     String deletedContents = "Total number of deleted redundant instances: " + (deletedContents1
         + deletedContents2
         + deletedContents3);
     console.printMessageln(deletedContents);
     System.out.println();
     System.out.println(deletedContents);
 
     connection.commit();
     connection.setAutoCommit(autocommit);
   }
 
   // private int executeQuery(String query) throws SQLException {
   // Connection connection = getConnection();
   // Statement statement = connection.createStatement();
   // ResultSet resultSet = statement.executeQuery(query);
   // resultSet.next();
   // return resultSet.getInt(1);
   // }
   private List<Integer> executeQuery(String query) throws SQLException {
     List<Integer> result = new ArrayList<Integer>();
     Connection connection = getConnection();
     Statement statement = connection.createStatement();
     ResultSet resultSet = statement.executeQuery(query);
     while (resultSet.next()) {
       result.add(resultSet.getInt(1));
     }
     return result;
   }
 
   private int executeDeletion(String query, final List<Integer> objectsToDelete)
       throws SQLException {
     if (objectsToDelete.isEmpty()) {
       return 0;
     }
     Connection connection = getConnection();
     Statement statement = connection.createStatement();
     StringBuilder parameterBuilder = new StringBuilder();
     for (Integer anObjectToDelete : objectsToDelete) {
       parameterBuilder.append(anObjectToDelete).append(',');
     }
     String sqlRequest = MessageFormat.format(query, parameterBuilder.toString().substring(0,
         parameterBuilder.length() - 1));
     return statement.executeUpdate(sqlRequest);
   }
 
   private int deleteRedundantClassifiedInstances() throws SQLException {
     Connection connection = getConnection();
     Statement statement = connection.createStatement();
     ResultSet rs = statement.executeQuery(REDUNDANT_INSTANCE_OF_DUPLICATE_CONTENT_QUERY);
     // As it should have only a few (or no) results from the query above, we can execute the
     // deletion
     // for each of the retrieved result.
     List<Integer> contentsToDelete = new ArrayList<Integer>();
     while (rs.next()) {
       int silverContentId = rs.getInt("silverContentId");
       contentsToDelete.add(silverContentId);
     }
    executeDeletion(CONTENT_INSTANCE_CLASSIFICATION_DELETION, contentsToDelete);
    int deletedCount = executeDeletion(CONTENT_INSTANCE_DELETION, contentsToDelete);
     assertEquals(contentsToDelete.size(), deletedCount);
     return deletedCount;
   }
 
   private static void assertEquals(int expected, int actual) {
     if (expected != actual) {
       throw new AssertionError("Expected deletion: " + expected + ", actual deletion: " + actual);
     }
   }
 }
