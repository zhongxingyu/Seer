 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Data Dictionary
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev or Zero Technologies are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Enriko Käsper
  */
 
 package eionet.meta.dao.mysql;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.displaytag.properties.SortOrderEnum;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import eionet.meta.dao.ISiteCodeDAO;
 import eionet.meta.dao.domain.SiteCodeStatus;
 import eionet.meta.dao.domain.VocabularyConcept;
 import eionet.meta.dao.domain.VocabularyType;
 import eionet.meta.service.data.SiteCode;
 import eionet.meta.service.data.SiteCodeFilter;
 import eionet.meta.service.data.SiteCodeResult;
 
 /**
  * Site Code DAO implementation.
  *
  * @author Enriko Käsper
  */
 @Repository
 public class SiteCodeDAOImpl extends GeneralDAOImpl implements ISiteCodeDAO {
 
     /**
      * {@inheritDoc}
      */
     @Override
     public SiteCodeResult searchSiteCodes(SiteCodeFilter filter) {
 
         Map<String, Object> params = new HashMap<String, Object>();
         String sql = getSiteCodesSql(filter, params);
 
         List<SiteCode> resultList = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<SiteCode>() {
             @Override
             public SiteCode mapRow(ResultSet rs, int rowNum) throws SQLException {
                 SiteCode sc = new SiteCode();
                 sc.setId(rs.getInt("vc.VOCABULARY_CONCEPT_ID"));
                 sc.setIdentifier(rs.getString("vc.IDENTIFIER"));
                 sc.setLabel(rs.getString("vc.LABEL"));
                 sc.setDefinition(rs.getString("vc.DEFINITION"));
                 sc.setNotation(rs.getString("vc.NOTATION"));
                 sc.setSiteCodeId(rs.getInt("sc.SITE_CODE_ID"));
                 sc.setStatus(SiteCodeStatus.valueOf(rs.getString("sc.STATUS")));
                 sc.setCountryCode(rs.getString("sc.CC_ISO2"));
                 sc.setDateCreated(rs.getTimestamp("sc.DATE_CREATED"));
                 sc.setUserCreated(rs.getString("sc.USER_CREATED"));
                 sc.setDateAllocated(rs.getTimestamp("sc.DATE_ALLOCATED"));
                 sc.setUserAllocated(rs.getString("sc.USER_ALLOCATED"));
                 sc.setInitialSiteName(rs.getString("sc.INITIAL_SITE_NAME"));
                 sc.setYearsDeleted(rs.getString("sc.YEARS_DELETED"));
                 sc.setYearsDisappeared(rs.getString("sc.YEARS_DISAPPEARED"));
                 return sc;
             }
         });
 
         String totalSql = "SELECT FOUND_ROWS()";
         int totalItems = getJdbcTemplate().queryForInt(totalSql);
 
         SiteCodeResult result = new SiteCodeResult(resultList, totalItems, filter);
 
         return result;
     }
 
     /**
      * Returns SiteCode search SQL and also populates the parameters map.
      *
      * @param filter
      * @param params
      * @return
      */
     private String getSiteCodesSql(SiteCodeFilter filter, Map<String, Object> params) {
         StringBuilder sql = new StringBuilder();
         sql.append("select SQL_CALC_FOUND_ROWS sc.SITE_CODE_ID, sc.VOCABULARY_CONCEPT_ID, sc.STATUS, sc.CC_ISO2, "
                 + "sc.DATE_CREATED, sc.USER_CREATED, vc.VOCABULARY_CONCEPT_ID, vc.IDENTIFIER, vc.LABEL, "
                + "vc.DEFINITION, vc.NOTATION, sc.DATE_ALLOCATED, sc.USER_ALLOCATED, sc.INITIAL_SITE_NAME "
                + " sc.YEARS_DELETED, sc.YEARS_DISAPPEARED ");
         sql.append("from T_SITE_CODE sc, T_VOCABULARY_CONCEPT vc where sc.VOCABULARY_CONCEPT_ID=vc.VOCABULARY_CONCEPT_ID ");
 
         if (StringUtils.isNotEmpty(filter.getSiteName())) {
             params.put("text", "%" + filter.getSiteName() + "%");
             sql.append("and vc.LABEL like :text ");
         }
         if (StringUtils.isNotEmpty(filter.getIdentifier())) {
             params.put("identifier", filter.getIdentifier());
             sql.append("and vc.IDENTIFIER like :identifier ");
         }
         if (StringUtils.isNotEmpty(filter.getUserAllocated())) {
             params.put("userAllocated", filter.getUserAllocated());
             sql.append("and sc.USER_ALLOCATED like :userAllocated ");
         }
         if (filter.getDateAllocated() != null) {
             params.put("dateAllocated", filter.getDateAllocated());
             sql.append("and sc.DATE_ALLOCATED = :dateAllocated ");
         }
         if (filter.getStatus() != null) {
             params.put("status", filter.getStatus().toString());
             sql.append("and sc.STATUS = :status ");
         } else if (filter.isAllocatedUsedStatuses()) {
             params.put("statuses", Arrays.asList(SiteCodeFilter.ALLOCATED_USED_STATUSES));
             sql.append("and sc.STATUS IN (:statuses) ");
         }
         if (filter.getCountryCode() != null) {
             params.put("countryCode", filter.getCountryCode());
             sql.append("and sc.CC_ISO2 = :countryCode ");
         }
 
         // sorting
         if (StringUtils.isNotEmpty(filter.getSortProperty())) {
             if (filter.getSortProperty().equals("identifier")) {
                 sql.append("order by IDENTIFIER + 0");
             } else {
                 sql.append("order by " + filter.getSortProperty());
             }
             if (SortOrderEnum.ASCENDING.equals(filter.getSortOrder())) {
                 sql.append(" ASC ");
             } else {
                 sql.append(" DESC ");
             }
         } else {
             sql.append("order by IDENTIFIER + 0 ");
         }
         if (filter.isUsePaging()) {
             sql.append("LIMIT ").append(filter.getOffset()).append(",").append(filter.getPageSize());
         }
 
         return sql.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void insertSiteCodesFromConcepts(List<VocabularyConcept> vocabularyConcepts, String userName) {
 
         StringBuilder sql = new StringBuilder();
         sql.append("insert into T_SITE_CODE (VOCABULARY_CONCEPT_ID, USER_CREATED, DATE_CREATED) ");
         sql.append("values (:vocabularyConceptId, :userName, :dateCreated)");
 
         Date dateCreated = new Date();
         @SuppressWarnings("unchecked")
         Map<String, Object>[] batchValues = new HashMap[vocabularyConcepts.size()];
 
         for (int i = 0; i < vocabularyConcepts.size(); i++) {
             Map<String, Object> params = new HashMap<String, Object>();
             params.put("vocabularyConceptId", vocabularyConcepts.get(i).getId());
             params.put("userName", userName);
             params.put("dateCreated", dateCreated);
             batchValues[i] = params;
         }
 
         getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void allocateSiteCodes(List<SiteCode> freeSiteCodes, String countryCode, String userName, String[] siteNames,
             Date allocationTime) {
 
         StringBuilder sql = new StringBuilder();
         sql.append("update T_SITE_CODE set CC_ISO2 = :country, INITIAL_SITE_NAME = :siteName, STATUS = :status, "
                 + "DATE_ALLOCATED = :dateAllocated, USER_ALLOCATED = :userAllocated ");
         sql.append("where SITE_CODE_ID = :siteCodeId");
 
         @SuppressWarnings("unchecked")
         Map<String, Object>[] batchValues = new HashMap[siteNames.length];
 
         for (int i = 0; i < freeSiteCodes.size(); i++) {
             Map<String, Object> params = new HashMap<String, Object>();
             params.put("siteCodeId", freeSiteCodes.get(i).getSiteCodeId());
             params.put("country", countryCode);
             params.put("status", SiteCodeStatus.ALLOCATED.name());
             params.put("dateAllocated", allocationTime);
             params.put("userAllocated", userName);
             if (siteNames.length > i && siteNames[i] != null) {
                 params.put("siteName", siteNames[i]);
             } else {
                 params.put("siteName", "");
             }
             batchValues[i] = params;
         }
         getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);
 
         // update place-holder value in concept label to <allocated>
         StringBuilder sqlForConcepts = new StringBuilder();
         sqlForConcepts.append("update T_VOCABULARY_CONCEPT set LABEL = :label where VOCABULARY_CONCEPT_ID IN "
                 + " (select VOCABULARY_CONCEPT_ID from T_SITE_CODE where STATUS = :status AND "
                 + "DATE_ALLOCATED = :dateAllocated AND USER_ALLOCATED = :userAllocated )");
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("status", SiteCodeStatus.ALLOCATED.name());
         parameters.put("dateAllocated", allocationTime);
         parameters.put("userAllocated", userName);
         parameters.put("label", "<" + SiteCodeStatus.ALLOCATED.name().toLowerCase() + ">");
         getNamedParameterJdbcTemplate().update(sqlForConcepts.toString(), parameters);
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getSiteCodeVocabularyFolderId() {
 
         StringBuilder sql = new StringBuilder();
         sql.append("select min(VOCABULARY_FOLDER_ID) from T_VOCABULARY_FOLDER where VOCABULARY_TYPE = :type");
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("type", VocabularyType.SITE_CODE.name());
 
         return getNamedParameterJdbcTemplate().queryForInt(sql.toString(), params);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getFeeSiteCodeAmount() {
         StringBuilder sql = new StringBuilder();
         sql.append("select count(SITE_CODE_ID) from T_SITE_CODE where STATUS = :status");
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("status", SiteCodeStatus.AVAILABLE.name());
 
         return getNamedParameterJdbcTemplate().queryForInt(sql.toString(), params);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getCountryUnusedAllocations(String countryCode, boolean withoutInitialName) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("countryCode", countryCode);
         params.put("status", SiteCodeStatus.ALLOCATED.name());
 
         StringBuilder sql = new StringBuilder();
         sql.append("select count(SITE_CODE_ID) from T_SITE_CODE where STATUS = :status ");
         sql.append("and CC_ISO2 = :countryCode ");
         if (withoutInitialName){
             sql.append("and (INITIAL_SITE_NAME is null or INITIAL_SITE_NAME = '') ");
         }
 
         return getNamedParameterJdbcTemplate().queryForInt(sql.toString(), params);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getCountryUsedAllocations(String countryCode) {
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("countryCode", countryCode);
         params.put("statuses", Arrays.asList(SiteCodeFilter.ALLOCATED_USED_STATUSES));
 
         StringBuilder sql = new StringBuilder();
         sql.append("select count(SITE_CODE_ID) from T_SITE_CODE where STATUS in (:statuses) ");
         sql.append("and CC_ISO2 = :countryCode");
 
         return getNamedParameterJdbcTemplate().queryForInt(sql.toString(), params);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean siteCodeFolderExists() {
         StringBuilder sql = new StringBuilder();
         sql.append("select count(VOCABULARY_FOLDER_ID) from T_VOCABULARY_FOLDER where VOCABULARY_TYPE = :type");
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("type", VocabularyType.SITE_CODE.name());
 
         return getNamedParameterJdbcTemplate().queryForInt(sql.toString(), params) > 0;
     }
 
 }
