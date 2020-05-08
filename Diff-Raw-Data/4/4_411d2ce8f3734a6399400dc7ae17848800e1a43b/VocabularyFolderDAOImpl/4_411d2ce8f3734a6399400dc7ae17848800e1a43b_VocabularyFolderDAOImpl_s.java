 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either exprevf or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Content Registry 3
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev or Zero Technologies are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Juhan Voolaid
  */
 
 package eionet.meta.dao.mysql;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import eionet.meta.dao.IVocabularyFolderDAO;
 import eionet.meta.dao.domain.RegStatus;
 import eionet.meta.dao.domain.VocabularyFolder;
 
 /**
  * Vocabualary folder DAO.
  *
  * @author Juhan Voolaid
  */
 @Repository
 public class VocabularyFolderDAOImpl extends GeneralDAOImpl implements IVocabularyFolderDAO {
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyFolder> getVocabularyFolders(String userName) {
 
         Map<String, Object> params = new HashMap<String, Object>();
 
         StringBuilder sql = new StringBuilder();
         sql.append("select VOCABULARY_FOLDER_ID, IDENTIFIER, LABEL, REG_STATUS, WORKING_COPY, ");
         sql.append("WORKING_USER, DATE_MODIFIED, USER_MODIFIED, CHECKEDOUT_COPY_ID ");
         sql.append("from T_VOCABULARY_FOLDER ");
         sql.append("where ");
         if (StringUtils.isBlank(userName)) {
             sql.append("WORKING_COPY=FALSE ");
         } else {
             sql.append("(WORKING_COPY=FALSE or WORKING_USER=:workingUser) ");
             params.put("workingUser", userName);
         }
         sql.append("order by IDENTIFIER ");
 
         List<VocabularyFolder> items =
                 getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("IDENTIFIER"));
                         vf.setLabel(rs.getString("LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                         vf.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                         return vf;
                     }
                 });
 
         return items;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyFolder> getVocabularyFolderVersions(String continuityId, int vocabularyFolderId, String userName) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("continuityId", continuityId);
         params.put("vocabularyFolderId", vocabularyFolderId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select VOCABULARY_FOLDER_ID, IDENTIFIER, LABEL, REG_STATUS, WORKING_COPY, ");
         sql.append("WORKING_USER, DATE_MODIFIED, USER_MODIFIED, CHECKEDOUT_COPY_ID ");
         sql.append("from T_VOCABULARY_FOLDER ");
         sql.append("where CONTINUITY_ID = :continuityId and VOCABULARY_FOLDER_ID != :vocabularyFolderId ");
         if (StringUtils.isBlank(userName)) {
             sql.append("and WORKING_COPY=FALSE ");
         } else {
             sql.append("and (WORKING_COPY=FALSE or WORKING_USER=:workingUser) ");
             params.put("workingUser", userName);
         }
 
         List<VocabularyFolder> items =
                 getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("IDENTIFIER"));
                         vf.setLabel(rs.getString("LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                         vf.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                         return vf;
                     }
                 });
 
         return items;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int createVocabularyFolder(VocabularyFolder vocabularyFolder) {
         String sql =
                 "insert into T_VOCABULARY_FOLDER (IDENTIFIER,  LABEL, REG_STATUS, CONTINUITY_ID, "
                         + "WORKING_COPY, WORKING_USER, DATE_MODIFIED, USER_MODIFIED, CHECKEDOUT_COPY_ID, CONCEPT_IDENTIFIER_NUMERIC, BASE_URI) "
                         + "values (:identifier,  :label, :regStatus, :continuityId, :workingCopy, :workingUser, now(), :userModified, "
                         + ":checkedOutCopyId, :numericConceptIdentifiers, :baseUri)";
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("identifier", vocabularyFolder.getIdentifier());
         parameters.put("label", vocabularyFolder.getLabel());
         parameters.put("regStatus", vocabularyFolder.getRegStatus().toString());
         parameters.put("continuityId", vocabularyFolder.getContinuityId());
         parameters.put("workingCopy", vocabularyFolder.isWorkingCopy());
         parameters.put("workingUser", vocabularyFolder.getWorkingUser());
         parameters.put("userModified", vocabularyFolder.getUserModified());
         parameters.put("checkedOutCopyId",
                 vocabularyFolder.getCheckedOutCopyId() <= 0 ? null : vocabularyFolder.getCheckedOutCopyId());
         parameters.put("numericConceptIdentifiers", vocabularyFolder.isNumericConceptIdentifiers());
         parameters.put("baseUri", vocabularyFolder.getBaseUri());
 
         getNamedParameterJdbcTemplate().update(sql, parameters);
         return getLastInsertId();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyFolder getVocabularyFolder(String identifier, boolean workingCopy) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("identifier", identifier);
         params.put("workingCopy", workingCopy);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select VOCABULARY_FOLDER_ID, IDENTIFIER, LABEL, REG_STATUS, WORKING_COPY, BASE_URI, ");
         sql.append("WORKING_USER, DATE_MODIFIED, USER_MODIFIED, CHECKEDOUT_COPY_ID, CONTINUITY_ID, CONCEPT_IDENTIFIER_NUMERIC ");
         sql.append("from T_VOCABULARY_FOLDER ");
         sql.append("where IDENTIFIER=:identifier and WORKING_COPY=:workingCopy");
 
         VocabularyFolder result =
                 getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("IDENTIFIER"));
                         vf.setLabel(rs.getString("LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                         vf.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                         vf.setContinuityId(rs.getString("CONTINUITY_ID"));
                         vf.setNumericConceptIdentifiers(rs.getBoolean("CONCEPT_IDENTIFIER_NUMERIC"));
                         vf.setBaseUri(rs.getString("BASE_URI"));
                         return vf;
                     }
                 });
 
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void updateVocabularyFolder(VocabularyFolder vocabularyFolder) {
         String sql =
                 "update T_VOCABULARY_FOLDER set IDENTIFIER = :identifier,  LABEL = :label, REG_STATUS = :regStatus, CHECKEDOUT_COPY_ID = :checkedOutCopyId, "
                         + "WORKING_COPY = :workingCopy, WORKING_USER = :workingUser, DATE_MODIFIED = now(), USER_MODIFIED = :userModified,"
                         + " CONCEPT_IDENTIFIER_NUMERIC = :numericConceptIdentifiers, BASE_URI = :baseUri "
                         + "where VOCABULARY_FOLDER_ID = :vocabularyFolderId";
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("vocabularyFolderId", vocabularyFolder.getId());
         parameters.put("identifier", vocabularyFolder.getIdentifier());
         parameters.put("label", vocabularyFolder.getLabel());
         parameters.put("regStatus", vocabularyFolder.getRegStatus().toString());
         parameters.put("workingCopy", vocabularyFolder.isWorkingCopy());
         parameters.put("workingUser", vocabularyFolder.getWorkingUser());
         parameters.put("userModified", vocabularyFolder.getUserModified());
         parameters.put("checkedOutCopyId",
                 vocabularyFolder.getCheckedOutCopyId() <= 0 ? null : vocabularyFolder.getCheckedOutCopyId());
         parameters.put("numericConceptIdentifiers", vocabularyFolder.isNumericConceptIdentifiers());
         parameters.put("baseUri", vocabularyFolder.getBaseUri());
 
         getNamedParameterJdbcTemplate().update(sql, parameters);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteVocabularyFolders(List<Integer> ids) {
         String sql = "delete from T_VOCABULARY_FOLDER where VOCABULARY_FOLDER_ID in (:ids)";
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("ids", ids);
 
         getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyFolder getVocabularyFolder(int vocabularyFolderId) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("id", vocabularyFolderId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select VOCABULARY_FOLDER_ID, IDENTIFIER, LABEL, REG_STATUS, WORKING_COPY, BASE_URI, ");
         sql.append("WORKING_USER, DATE_MODIFIED, USER_MODIFIED, CHECKEDOUT_COPY_ID, CONTINUITY_ID, CONCEPT_IDENTIFIER_NUMERIC ");
         sql.append("from T_VOCABULARY_FOLDER ");
         sql.append("where VOCABULARY_FOLDER_ID=:id");
 
         VocabularyFolder result =
                 getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("IDENTIFIER"));
                         vf.setLabel(rs.getString("LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                         vf.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                         vf.setContinuityId(rs.getString("CONTINUITY_ID"));
                         vf.setNumericConceptIdentifiers(rs.getBoolean("CONCEPT_IDENTIFIER_NUMERIC"));
                         vf.setBaseUri(rs.getString("BASE_URI"));
                         return vf;
                     }
                 });
 
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyFolder getVocabularyWorkingCopy(int checkedOutCopyId) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("checkedOutCopyId", checkedOutCopyId);
 
         StringBuilder sql = new StringBuilder();
        sql.append("select VOCABULARY_FOLDER_ID, IDENTIFIER, LABEL, REG_STATUS, WORKING_COPY, BASE_URI ");
        sql.append("WORKING_USER, DATE_MODIFIED, USER_MODIFIED, CHECKEDOUT_COPY_ID, CONTINUITY_ID, CONCEPT_IDENTIFIER_NUMERIC");
         sql.append("from T_VOCABULARY_FOLDER ");
         sql.append("where CHECKEDOUT_COPY_ID=:checkedOutCopyId");
 
         VocabularyFolder result =
                 getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("IDENTIFIER"));
                         vf.setLabel(rs.getString("LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                         vf.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                         vf.setContinuityId(rs.getString("CONTINUITY_ID"));
                         vf.setNumericConceptIdentifiers(rs.getBoolean("CONCEPT_IDENTIFIER_NUMERIC"));
                         vf.setBaseUri(rs.getString("BASE_URI"));
                         return vf;
                     }
                 });
 
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isUniqueFolderIdentifier(String identifier, int... excludedVocabularyFolderIds) {
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("identifier", identifier);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select count(VOCABULARY_FOLDER_ID) from T_VOCABULARY_FOLDER ");
         sql.append("where IDENTIFIER = :identifier ");
         if (excludedVocabularyFolderIds != null && excludedVocabularyFolderIds.length > 0) {
             sql.append("and VOCABULARY_FOLDER_ID not in (:excludedVocabularyFolderIds)");
 
             List<Integer> excluded = new ArrayList<Integer>();
             for (int i : excludedVocabularyFolderIds) {
                 excluded.add(i);
             }
             parameters.put("excludedVocabularyFolderIds", excluded);
         }
 
         int result = getNamedParameterJdbcTemplate().queryForInt(sql.toString(), parameters);
 
         return result == 0;
     }
 
 }
