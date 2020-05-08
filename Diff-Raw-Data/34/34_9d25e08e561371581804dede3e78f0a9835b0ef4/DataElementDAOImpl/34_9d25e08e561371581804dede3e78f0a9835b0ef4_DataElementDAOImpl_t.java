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
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.jdbc.core.RowCallbackHandler;
 import org.springframework.stereotype.Repository;
 
 import eionet.meta.DDSearchEngine;
 import eionet.meta.DElemAttribute;
 import eionet.meta.dao.IDataElementDAO;
 import eionet.meta.dao.domain.Attribute;
 import eionet.meta.dao.domain.DataElement;
 import eionet.meta.service.data.DataElementsFilter;
 import eionet.meta.service.data.DataElementsResult;
 
 /**
  * Data element DAO.
  *
  * @author Juhan Voolaid
  */
 @Repository
 public class DataElementDAOImpl extends GeneralDAOImpl implements IDataElementDAO {
 
     /** Logger. */
     protected static final Logger LOGGER = Logger.getLogger(DataElementDAOImpl.class);
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DataElementsResult searchDataElements(DataElementsFilter filter) {
         boolean commonElements = filter.getElementType().equals(DataElementsFilter.COMMON_ELEMENT_TYPE);
         List<DataElement> dataElements = null;
         if (commonElements) {
             dataElements = executeCommonElementQuery(filter);
         } else {
             dataElements = executeNonCommonElementQuery(filter);
         }
 
         DataElementsResult result = new DataElementsResult();
         result.setDataElements(dataElements);
         result.setCommonElements(commonElements);
 
         return result;
     }
 
     private List<DataElement> executeCommonElementQuery(final DataElementsFilter filter) {
         Map<String, Object> params = new HashMap<String, Object>();
         StringBuilder sql = new StringBuilder();
 
        sql.append("select de.DATAELEM_ID, de.IDENTIFIER, de.SHORT_NAME, de.REG_STATUS, de.DATE, de.TYPE, de.WORKING_USER ");
         sql.append("from DATAELEM de ");
         sql.append("where ");
         sql.append("de.PARENT_NS is null ");
        sql.append("and de.WORKING_COPY = 'N' ");

         // Filter parameters
         if (StringUtils.isNotEmpty(filter.getRegStatus())) {
             sql.append("and de.REG_STATUS = :regStatus ");
             params.put("regStatus", filter.getRegStatus());
         }
         if (StringUtils.isNotEmpty(filter.getType())) {
             sql.append("and de.TYPE = :type ");
             params.put("type", filter.getType());
         }
         if (StringUtils.isNotEmpty(filter.getShortName())) {
             sql.append("and de.SHORT_NAME like :shortName ");
             params.put("shortName", "%" + filter.getShortName() + "%");
         }
         if (StringUtils.isNotEmpty(filter.getIdentifier())) {
             sql.append("and de.IDENTIFIER like :identifier ");
             params.put("identifier", "%" + filter.getIdentifier() + "%");
         }
         // attributes
         for (int i = 0; i < filter.getAttributes().size(); i++) {
             Attribute a = filter.getAttributes().get(i);
             String idKey = "attrId" + i;
             String valueKey = "attrValue" + i;
             if (StringUtils.isNotEmpty(a.getValue())) {
                 sql.append("and ");
                 sql.append("de.DATAELEM_ID in ( ");
                 sql.append("select a.DATAELEM_ID from ATTRIBUTE a WHERE ");
                 sql.append("a.M_ATTRIBUTE_ID = :" + idKey + " AND a.VALUE like :" + valueKey + " AND a.PARENT_TYPE = :parentType ");
                 sql.append(") ");
             }
             params.put(idKey, a.getId());
             String value = "%" + a.getValue() + "%";
             params.put(valueKey, value);
             params.put("parentType", DElemAttribute.ParentType.ELEMENT.toString());
         }
 
         sql.append("order by de.IDENTIFIER asc, de.DATAELEM_ID desc");
 
        // LOGGER.debug("SQL: " + sql.toString());

         final List<DataElement> dataElements = new ArrayList<DataElement>();
 
         getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {
             DataElement de;
             String curElmIdf;
 
             @Override
             public void processRow(ResultSet rs) throws SQLException {
 
                 int elmID = rs.getInt("de.DATAELEM_ID");
                 String elmIdf = rs.getString("de.IDENTIFIER");
                 // skip non-existing elements, ie trash from some erroneous situation
                 if (elmIdf == null) {
                     return;
                 }
 
                 // the following if block skips non-latest
                 if (curElmIdf != null && elmIdf.equals(curElmIdf)) {
                     if (!filter.isIncludeHistoricVersions()) {
                         return;
                     }
                 } else {
                     curElmIdf = elmIdf;
                 }
 
                 de = new DataElement();
                 de.setId(rs.getInt("de.DATAELEM_ID"));
                 de.setShortName(rs.getString("de.SHORT_NAME"));
                 de.setStatus(rs.getString("de.REG_STATUS"));
                 de.setType(rs.getString("de.TYPE"));
                 de.setModified(new Date(rs.getLong("de.DATE")));
                 de.setWorkingUser(rs.getString("de.WORKING_USER"));
 
                 dataElements.add(de);
             }
         });
 
         return dataElements;
     }
 
     private List<DataElement> executeNonCommonElementQuery(final DataElementsFilter filter) {
         Map<String, Object> params = new HashMap<String, Object>();
         StringBuilder sql = new StringBuilder();
 
         sql.append("select de.DATAELEM_ID, de.IDENTIFIER, de.SHORT_NAME, ds.REG_STATUS, de.DATE, de.TYPE, ");
         sql.append("t.SHORT_NAME as tableName, ds.IDENTIFIER as datasetName, ds.IDENTIFIER, ds.DATASET_ID, t.IDENTIFIER, t.TABLE_ID, de.WORKING_USER, de.WORKING_COPY ");
         sql.append("from DATAELEM de ");
         sql.append("left join TBL2ELEM t2e on (de.DATAELEM_ID = t2e.DATAELEM_ID) ");
         sql.append("left join DS_TABLE t on (t2e.TABLE_ID = t.TABLE_ID) ");
         sql.append("left join DST2TBL d2t on (t.TABLE_ID = d2t.TABLE_ID) ");
         sql.append("left join DATASET ds on (d2t.DATASET_ID = ds.DATASET_ID) ");
         sql.append("where ");
         sql.append("de.PARENT_NS is not null ");
         sql.append("and ds.DELETED is null ");
         sql.append("and ds.WORKING_COPY = 'N' ");
         // Filter parameters
         if (StringUtils.isNotEmpty(filter.getDataSet())) {
             sql.append("and ds.IDENTIFIER = :dataSet ");
             params.put("dataSet", filter.getDataSet());
         }
         if (StringUtils.isNotEmpty(filter.getType())) {
             sql.append("and de.TYPE = :type ");
             params.put("type", filter.getType());
         }
         if (StringUtils.isNotEmpty(filter.getShortName())) {
             sql.append("and de.SHORT_NAME like :shortName ");
             params.put("shortName", "%" + filter.getShortName() + "%");
         }
         if (StringUtils.isNotEmpty(filter.getIdentifier())) {
             sql.append("and de.IDENTIFIER like :identifier ");
             params.put("identifier", "%" + filter.getIdentifier() + "%");
         }
         // attributes
         for (int i = 0; i < filter.getAttributes().size(); i++) {
             Attribute a = filter.getAttributes().get(i);
             String idKey = "attrId" + i;
             String valueKey = "attrValue" + i;
             if (StringUtils.isNotEmpty(a.getValue())) {
                 sql.append("and ");
                 sql.append("de.DATAELEM_ID in ( ");
                 sql.append("select a.DATAELEM_ID from ATTRIBUTE a WHERE ");
                 sql.append("a.M_ATTRIBUTE_ID = :" + idKey + " AND a.VALUE like :" + valueKey + " AND a.PARENT_TYPE = :parentType ");
                 sql.append(") ");
             }
             params.put(idKey, a.getId());
             String value = "%" + a.getValue() + "%";
             params.put(valueKey, value);
             params.put("parentType", DElemAttribute.ParentType.ELEMENT.toString());
         }
 
         sql.append("order by ds.IDENTIFIER asc, ds.DATASET_ID desc, t.IDENTIFIER asc, t.TABLE_ID desc, de.IDENTIFIER asc, de.DATAELEM_ID desc");
 
         // LOGGER.debug("SQL: " + sql.toString());
 
         final List<DataElement> dataElements = new ArrayList<DataElement>();
 
         getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {
             DataElement de;
             String curDstIdf;
             String curDstID;
 
             @Override
             public void processRow(ResultSet rs) throws SQLException {
 
                 String dstID = rs.getString("ds.DATASET_ID");
                 String dstIdf = rs.getString("ds.IDENTIFIER");
                 if (dstID == null || dstIdf == null) {
                     return;
                 }
 
                 String tblID = rs.getString("t.TABLE_ID");
                 String tblIdf = rs.getString("t.IDENTIFIER");
                 // skip non-existing tables, ie trash from some erroneous situation
                 if (tblID == null || tblIdf == null) {
                     return;
                 }
 
                 int elmID = rs.getInt("de.DATAELEM_ID");
                 String elmIdf = rs.getString("de.IDENTIFIER");
                 // skip non-existing elements, ie trash from some erroneous situation
                 if (elmIdf == null) {
                     return;
                 }
 
                 // the following if block skips elements from non-latest DATASETS
                 if (curDstIdf == null || !curDstIdf.equals(dstIdf)) {
                     curDstID = dstID;
                     curDstIdf = dstIdf;
                 } else if (!filter.isIncludeHistoricVersions()) {
                     if (!curDstID.equals(dstID)) {
                         return;
                     }
                 }
 
                 de = new DataElement();
                 de.setId(rs.getInt("de.DATAELEM_ID"));
                 de.setShortName(rs.getString("de.SHORT_NAME"));
                 de.setStatus(rs.getString("ds.REG_STATUS"));
                 de.setType(rs.getString("de.TYPE"));
                 de.setModified(new Date(rs.getLong("de.DATE")));
                 de.setTableName(rs.getString("tableName"));
                 de.setDataSetName(rs.getString("datasetName"));
                 de.setWorkingUser(rs.getString("de.WORKING_USER"));
 
                 dataElements.add(de);
             }
         });
 
         return dataElements;
     }
 
     /**
      * {@inheritDoc}
      *
      * @throws SQLException
      */
     @Override
     public List<Attribute> getDataElementAttributes() throws SQLException {
         List<Attribute> result = new ArrayList<Attribute>();
         DDSearchEngine searchEngine = new DDSearchEngine(getConnection());
 
         @SuppressWarnings("rawtypes")
         Vector attrs = searchEngine.getDElemAttributes();
 
         for (int i = 0; i < attrs.size(); i++) {
             DElemAttribute attribute = (DElemAttribute) attrs.get(i);
 
             if (attribute.displayFor("CH1") || attribute.displayFor("CH2")) {
                 Attribute a = new Attribute();
                 a.setId(Integer.parseInt(attribute.getID()));
                 a.setName(attribute.getName());
                 a.setShortName(attribute.getShortName());
 
                 result.add(a);
             }
         }
         return result;
     }
 
 }
