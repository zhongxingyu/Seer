 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.common.business.fedora.mptstore;
 
 import java.io.ByteArrayInputStream;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.sql.DataSource;
 
 import org.nsdl.mptstore.core.BasicTableManager;
 import org.nsdl.mptstore.core.TableManager;
 import org.nsdl.mptstore.impl.postgres.PostgresDDLGenerator;
 import org.nsdl.mptstore.rdf.URIReference;
 import org.nsdl.mptstore.util.NTriplesUtil;
 import org.springframework.jdbc.CannotGetJdbcConnectionException;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.service.UserContext;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.filter.FilterHandler;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 
 /**
  * To use is as implementation of the abstract class TripleStoreUtility register
  * this as spring.bean id="business.TripleStoreUtility".
  * 
  * @spring.bean id="business.TripleStoreUtility"
  * 
  * @author FRS
  * 
  * @common
  * 
  */
 public class MPTTripleStoreUtility extends TripleStoreUtility {
 
     private static AppLogger log =
         new AppLogger(MPTTripleStoreUtility.class.getName());
 
     private TableManager tableManager = null;
 
     private String tableWithPredicate = null;
 
     /**
      * Injects the data source.
      * 
      * @spring.property ref="fedora.triplestore.DataSource"
      * 
      * @param myDataSource
      */
     public void setMyDataSource(final DataSource myDataSource) {
         super.setDataSource(myDataSource);
     }
 
     /**
      * 
      */
     @Override
     public Map<String, String> getProperties(
         final String pid, final Collection<String> fullqualifiedNamedProperties)
         throws TripleStoreSystemException {
         Map<String, String> result = new HashMap<String, String>();
         Iterator<String> propertiesIter =
             fullqualifiedNamedProperties.iterator();
         while (propertiesIter.hasNext()) {
             String property = propertiesIter.next();
             result.put(property, getRelation(pid, property));
         }
         return result;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.common.business.fedora.TripleStoreUtility#executeQueryId
      * (java.lang.String, boolean, java.lang.String)
      */
     @Override
     public List<String> executeQueryId(
         final String id, final boolean targetIsSubject, final String predicate)
         throws TripleStoreSystemException {
         return executeQuery(false, id, targetIsSubject, predicate);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.common.business.fedora.TripleStoreUtility#executeQueryLiteral
      * (java.lang.String, boolean, java.lang.String)
      */
     @Override
     protected List<String> executeQueryLiteral(
         final String literal, final boolean targetIsSubject,
         final String predicate) throws TripleStoreSystemException {
         return executeQuery(true, literal, targetIsSubject, predicate);
     }
 
     @Override
     protected String executeQueryEarliestCreationDate()
         throws TripleStoreSystemException {
         List<String> results = new Vector<String>();
         String result = null;
         String tableName = getTableName(PROP_CREATION_DATE);
         if (tableName != null) {
             StringBuffer table = new StringBuffer(tableName);
             StringBuffer select = new StringBuffer("SELECT min(o) ");
             StringBuffer from = new StringBuffer("FROM ").append(table);
 
             select = select.append(from);
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             results = executeSqlQuery(select.toString());
 
             if (getLogger().isDebugEnabled()) {
                 if (result != null) {
                     getLogger().debug("found " + results.size() + " records");
                     for (String item : results) {
                         getLogger().debug("item: " + item);
                     }
                 }
                 else {
                     getLogger().debug("found no records");
                 }
             }
 
             if (results.size() == 1) {
                 result = results.get(0);
             }
             else {
                 String message =
                     "More than one result for earliest creation date.";
                 getLogger().error(message);
                 throw new TripleStoreSystemException(message);
             }
         }
         return result;
 
     }
 
     /**
      * 
      * @param queryByLiteral
      * @param idOrLiteral
      * @param targetIsSubject
      * @param predicate
      * @return
      * @throws TripleStoreSystemException
      */
     private List<String> executeQuery(
         final boolean queryByLiteral, final String idOrLiteral,
         final boolean targetIsSubject, final String predicate)
         throws TripleStoreSystemException {
 
         List<String> result = new Vector<String>();
         String tableName = getTableName(predicate);
         if (tableName != null) {
             StringBuffer table = new StringBuffer(tableName);
             StringBuffer select = new StringBuffer("SELECT ");
             StringBuffer from =
                 new StringBuffer("FROM ").append(table).append(" ");
             StringBuffer where = new StringBuffer("WHERE (");
 
             if (targetIsSubject) {
                 select = select.append(table).append(".s").append(" ");
                 where = where.append(table).append(".o = ");
             }
             else {
                 select = select.append(table).append(".o").append(" ");
                 where = where.append(table).append(".s = ");
             }
             if (queryByLiteral) {
                 where =
                     where
                         .append("'\"")
                         .append(
                             MPTStringUtil.escapeLiteralValueForSql(idOrLiteral))
                         .append("\"')");
             }
             else {
                 try {
                     where =
                         where.append("'").append(
                             (new URIReference("info:fedora/" + idOrLiteral))
                                 .toString()).append("')");
                 }
                 catch (URISyntaxException e) {
                     throw new TripleStoreSystemException(e.getMessage(), e);
                 }
             }
             select = select.append(from).append(where);
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             result = executeSqlQuery(select.toString());
             if (getLogger().isDebugEnabled()) {
                 if (result != null) {
                     getLogger().debug("found " + result.size() + " records");
                     for (String item : result) {
                         getLogger().debug("item: " + item);
                     }
                 }
                 else {
                     getLogger().debug("found no records");
                 }
             }
         }
         return result;
     }
 
     /**
      * 
      * @param ids
      * @param targetIsSubject
      * @param predicate
      * @return
      * @throws TripleStoreSystemException
      */
     @Override
     public List<String> executeQueryForList(
         final Collection<String> ids, final boolean targetIsSubject,
         final String predicate) throws TripleStoreSystemException {
 
         List<String> result = new Vector<String>();
         String tableName = getTableName(predicate);
         if (tableName != null) {
             StringBuffer table = new StringBuffer(tableName);
             StringBuffer select = new StringBuffer("SELECT ");
             StringBuffer from =
                 new StringBuffer("FROM ").append(table).append(" ");
             StringBuffer where = new StringBuffer("WHERE (");
             if (targetIsSubject) {
                 select = select.append(table).append(".s ");
             }
             else {
                 select = select.append(table).append(".o ");
             }
             Iterator<String> iterator = ids.iterator();
             boolean firstStep = true;
             while (iterator.hasNext()) {
                 String id = iterator.next();
                 if (firstStep) {
                     firstStep = false;
                     if (targetIsSubject) {
                         where = where.append(table).append(".o = ");
                     }
                     else {
                         where = where.append(table).append(".s = ");
                     }
                 }
                 else {
                     where = where.append(" OR ");
                     if (targetIsSubject) {
                         where = where.append(table).append(".o = ");
                     }
                     else {
                         where = where.append(table).append(".s = ");
                     }
                 }
 
                 try {
                     where =
                         where
                             .append("'").append(
                                 (new URIReference("info:fedora/" + id))
                                     .toString()).append("'");
                 }
                 catch (URISyntaxException e) {
                     throw new TripleStoreSystemException(e.getMessage(), e);
                 }
 
             }
             where = where.append(")");
             select = select.append(from).append(where);
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             result = executeSqlQuery(select.toString());
             if (getLogger().isDebugEnabled()) {
                 if (result != null) {
                     getLogger().debug("found " + result.size() + " records");
                     for (String item : result) {
                         getLogger().debug("item: " + item);
                     }
                 }
                 else {
                     getLogger().debug("found no records");
                 }
             }
         }
         return result;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.common.business.fedora.TripleStoreUtility#getRelation
      * (java.lang.String, java.lang.String, java.lang.String)
      */
     @Override
     public String getRelation(
         final String pid, final String fullQualifiedPropertyName)
         throws TripleStoreSystemException {
         String result = null;
         Connection connection = null;
         ResultSet resultSet = null;
         String query = null;
         try {
             String table = getTableName(fullQualifiedPropertyName);
             if (table == null) {
                 return result;
             }
             String querySelect = new String("SELECT ");
             String queryFrom = new String(" FROM ");
             String queryWhere = new String(" WHERE ");
 
             querySelect = querySelect + table + ".o";
             queryFrom = queryFrom + table;
             queryWhere =
                 queryWhere + "(" + table + ".s = '"
                     + new URIReference("info:fedora/" + pid).toString() + "')";
 
             query = querySelect + queryFrom + queryWhere;
             connection = getConnection();
             resultSet = connection.prepareStatement(query).executeQuery();
             if (resultSet.next()) {
                 result = getValue(resultSet.getString(1));
             }
         }
         catch (URISyntaxException e) {
             log.error(e);
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         catch (CannotGetJdbcConnectionException e) {
             log.error(e);
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         catch (SQLException e) {
             log.error(e);
             throw new TripleStoreSystemException("Failed to execute query "
                 + query, e);
         }
         catch (SystemException e) {
             log.error(e);
             throw new TripleStoreSystemException(
                 "Failed to escape forbidden xml characters ", e);
         }
         finally {
             if (connection != null) {
                 releaseConnection(connection);
             }
             if (resultSet != null) {
                 try {
                     resultSet.close();
                 }
                 catch (SQLException e) {
                     log.error(e);
                     // Ignore because the result set is already closed.
                 }
             }
         }
         return result;
     }
 
     /**
      * 
      */
     public List<String> evaluate(
         final String objectType, final Map filterMap, final String whereClause)
         throws SystemException, MissingMethodParameterException {
 
         return evaluate(objectType, filterMap, null, whereClause);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.common.business.fedora.IFTripleStoreFilterUtility#evaluate
      * (java.lang.String, java.lang.String)
      */
     public List<String> evaluate(
         final String objectType, final Map filterMap,
         final String additionalConditionTriple, final String whereClause)
         throws SystemException, MissingMethodParameterException {
 
         Map filter = (Map) filterMap.get("filter");
 
         boolean first = true;
         // String objectsToFind = objectType + "s";
         // String query = "select $object from <#ri> "
         // + "where $object
         // <http://www.nsdl.org/ontologies/relationships/objectType> '"
         // + objectType + "' ";
         String tableWithObjectType = getTableName(PROP_OBJECT_TYPE);
         if (tableWithObjectType == null) {
             return new ArrayList<String>();
         }
         String objectTypeColumn = tableWithObjectType + ".o";
         StringBuffer queryResultBuf = new StringBuffer();
         StringBuffer querySelectPartBuf = new StringBuffer();
         StringBuffer queryWherePart = new StringBuffer();
         String queryResult = null;
 
         String roleCriteria = null;
         String userCriteria = null;
         String topLevelOus = null;
         String filterCriteria = null;
 
         String tableWithIdentifier =
             getTableName("http://purl.org/dc/elements/1.1/identifier");
         if (tableWithIdentifier == null) {
             return new ArrayList<String>();
         }
         String idColumn = tableWithIdentifier + ".o";
         querySelectPartBuf.append("SELECT ");
         querySelectPartBuf.append(tableWithObjectType);
         querySelectPartBuf.append(".s FROM ");
         queryResultBuf.append(tableWithObjectType);
         // queryResultBuf.append(" WHERE ");
 
         if ("member".equalsIgnoreCase(objectType)) {
             if ((filter != null) && filter.containsKey(PROP_OBJECT_TYPE)) {
                 String queryPartObjectTypeMember =
                     objectTypeColumn + "=" + "\'<"
                         + filter.remove(PROP_OBJECT_TYPE) + ">\'";
                 queryWherePart.append(queryPartObjectTypeMember);
             }
             else {
                 String queryPartObjectTypeMember =
                     "("
                         + objectTypeColumn
                         + "="
                         + "\'<http://escidoc.de/core/01/resources/Item>\' OR "
                         + objectTypeColumn
                         + "="
                         + "\'<http://escidoc.de/core/01/resources/Container>\')";
                 queryWherePart.append(queryPartObjectTypeMember);
             }
         }
         else {
             String queryPartObjectType =
                 objectTypeColumn + "=" + "\'<" + objectType + ">\'";
             queryWherePart.append(queryPartObjectType);
         }
         if (additionalConditionTriple != null) {
             first = false;
             boolean inverse = false;
             String[] tripleParts = additionalConditionTriple.split("\\s");
             if (tripleParts.length != 3) {
                 throw new IntegritySystemException("Wrong triple");
             }
             if (tripleParts[0].startsWith("<")) {
                 inverse = true;
             }
             int length = tripleParts[1].length();
             tripleParts[1] = tripleParts[1].substring(1, length - 1);
             String additionalTableName = getTableName(tripleParts[1]);
             if (additionalTableName == null) {
                 return new ArrayList<String>();
             }
             queryResultBuf.append(" INNER JOIN ");
             queryResultBuf.append(additionalTableName);
             queryResultBuf.append(" ON ");
             queryResultBuf.append(additionalTableName);
             if (inverse) {
                 queryResultBuf.append(".o=");
             }
             else {
                 queryResultBuf.append(".s=");
             }
             queryResultBuf.append(tableWithObjectType);
             queryResultBuf.append(".s");
             queryWherePart.append(" AND ");
             queryWherePart.append(additionalTableName);
             if (inverse) {
                 queryWherePart.append(".s=");
                 queryWherePart.append("'" + tripleParts[0] + "'");
             }
             else {
                 queryWherePart.append(".o=");
                 queryWherePart.append("'" + tripleParts[2] + "'");
             }
         }
         if (filter == null) {
             queryResultBuf.append(" WHERE ");
             queryResultBuf.append(queryWherePart);
             querySelectPartBuf.append(queryResultBuf);
             queryResult = querySelectPartBuf.toString();
 
         }
         else {
             // stored for later use
             roleCriteria = (String) filter.remove("role");
             userCriteria = (String) filter.remove("user");
             if (userCriteria == null) {
                 if (roleCriteria != null) {
                     throw new MissingMethodParameterException(
                         "If role criteria is used, user id must be specified");
                 }
             }
             else {
                 // try {
                 // whereClause =
                 // getPdp().getRoleUserWhereClauseMPT(objectType,
                 // userCriteria, roleCriteria);
                 if (whereClause == null) {
                     return new ArrayList<String>(0);
                 }
 
                 if (whereClause.length() > 0) {
                     if (!first) {
                         queryResultBuf.insert(0, "(");
                         queryResultBuf.append(") INNER JOIN (");
                     }
                     else {
                         queryResultBuf.append(" INNER JOIN (");
                     }
                     queryResultBuf.append(whereClause);
                     queryResultBuf.append(") unionTable ON unionTable.temp=");
                     queryResultBuf.append(tableWithObjectType);
                     queryResultBuf.append(".s");
                 }
                 // }
                 // catch (SystemException e) {
                 // // FIXME: throw SystemException?
                 // throw new TripleStoreSystemException(
                 // "Failed to retrieve clause for user and role criteria",
                 // e);
                 // }
             }
 
             filterCriteria =
                 getQueryPartId(idColumn, (Set) filter
                     .remove(Constants.DC_IDENTIFIER_URI));
             if (!filterCriteria.equals("")) {
                 if (!first) {
                     queryResultBuf.insert(0, "(");
                     queryResultBuf.append(") INNER JOIN ");
                 }
                 else {
 
                     queryResultBuf.append(" INNER JOIN ");
                 }
                 queryResultBuf.append(tableWithIdentifier);
                 queryResultBuf.append(" ON ");
                 queryResultBuf.append(tableWithIdentifier);
                 queryResultBuf.append(".s=");
                 queryResultBuf.append(tableWithObjectType);
                 queryResultBuf.append(".s");
 
                 queryWherePart.append(" AND (");
                 queryWherePart.append(filterCriteria);
                 queryWherePart.append(")");
             }
 
             topLevelOus =
                 (String) filter.remove("top-level-organizational-units");
 
             if (!filter.isEmpty()) {
                 String[] joinPartProperties =
                     getJoinPartProperties(filter, first);
                 if (joinPartProperties == null) {
 
                     return new ArrayList<String>();
                 }
                 else {
                     queryResultBuf.insert(0, joinPartProperties[0]);
                     queryResultBuf.append(joinPartProperties[1]);
                     queryWherePart.append(getWherePartProperties(filter));
                 }
             }
 
             if (topLevelOus != null) {
                 // shouldn't have a parent
                 String tableWithParents =
                     getTableName(Constants.STRUCTURAL_RELATIONS_NS_URI
                         + "parent");
                 if (tableWithParents != null) {
                     queryWherePart.append(" AND (");
                     queryWherePart.append(tableWithObjectType);
                     queryWherePart.append(".s NOT IN (SELECT ");
                     queryWherePart.append(tableWithParents);
                     queryWherePart.append(".s FROM ");
                     queryWherePart.append(tableWithParents);
                     queryWherePart.append("))");
                 }
 
             }
             queryResultBuf.append(" WHERE ");
 
             queryResultBuf.append(queryWherePart);
 
             querySelectPartBuf.append(queryResultBuf);
             queryResult = querySelectPartBuf.toString();
         }
         if (!checkQuery(queryResult)) {
             return new ArrayList<String>();
         }
         List<String> resultList = executeSqlQuery(queryResult);
 
         return resultList;
 
     }
 
     /**
      * 
      * @param filters
      * @return
      */
     private String[] getJoinPartProperties(
         final Map<String, String> filters, final boolean begin)
         throws TripleStoreSystemException {
         StringBuffer queryPartBuffer = new StringBuffer();
 
         Iterator<String> it = filters.keySet().iterator();
         String tableWithOldPredicate = null;
         String tableWithObjectType = getTableName(PROP_OBJECT_TYPE);
         boolean first = true;
         StringBuffer openBracesBuffer = new StringBuffer();
         while (it.hasNext()) {
             String predicate = it.next();
             String tableWithPredicate = getTableName(predicate);
             if (tableWithPredicate == null) {
                 return null;
             }
             if (begin && first) {
                 queryPartBuffer.append(" INNER JOIN ");
             }
             else {
                 queryPartBuffer.append(") INNER JOIN ");
             }
             queryPartBuffer.append(tableWithPredicate);
             queryPartBuffer.append(" ON ");
             if (first) {
                 queryPartBuffer.append(tableWithObjectType);
                 queryPartBuffer.append(".s=");
             }
             else {
                 queryPartBuffer.append(tableWithOldPredicate);
                 queryPartBuffer.append(".s=");
             }
             queryPartBuffer.append(tableWithPredicate);
             queryPartBuffer.append(".s");
             // queryPartBuffer.append(")");
             if (!begin || !first) {
                 openBracesBuffer.append("(");
             }
 
             tableWithOldPredicate = tableWithPredicate;
             if (first) {
                 first = false;
             }
         }
         String openBraces = openBracesBuffer.toString();
         String queryPart = queryPartBuffer.toString();
         String[] result = new String[2];
         result[0] = openBraces;
         result[1] = queryPart;
         return result;
     }
 
     private String getWherePartProperties(final Map<String, String> filters)
         throws TripleStoreSystemException {
         if (filters.isEmpty()) {
             // just provide NO query part if there are no predicates properties
             return "";
         }
 
         StringBuffer queryPart = new StringBuffer();
 
         Iterator<String> it = filters.keySet().iterator();
         while (it.hasNext()) {
             String predicate = it.next();
             String object = null;
             String tableWithPredicate = getTableName(predicate);
             if (tableWithPredicate == null) {
                 return null;
             }
             String val = filters.get(predicate);
 
             // make URIs from given IDs or HREFs for all structural-relation
             // predicates
             if (predicate.startsWith(Constants.STRUCTURAL_RELATIONS_NS_URI)) {
                 String id = val;
                 if (val.startsWith("http://") || val.startsWith("/")) {
                     id = Utility.getId(val);
                 }
                 object = "\'<info:fedora/" + id + ">\'";
             }
             else {
                 object = MPTStringUtil.escapeLiteralValueForSql(val);
                 object = "\'\"" + val + "\"\'";
             }
             queryPart.append(" AND ");
             queryPart.append(tableWithPredicate);
             queryPart.append(".o=");
             queryPart.append(object);
 
         }
         return queryPart.toString();
     }
 
     /**
      * 
      * @param objectsToFind
      * @param filterXML
      * @return
      * @throws InvalidContentException
      * @throws XmlParserSystemException
      */
     private static Map<String, Object> getFilterMap(
         final String objectsToFind, final String filterXML)
         throws InvalidContentException, XmlParserSystemException {
 
         Map<String, Object> filter = new HashMap<String, Object>();
 
         StaxParser sp = new StaxParser();
         FilterHandler fh = new FilterHandler(sp);
         sp.addHandler(fh);
         try {
             sp.parse(new ByteArrayInputStream(filterXML
                 .getBytes(XmlUtility.CHARACTER_ENCODING)));
         }
         catch (InvalidContentException e) {
             throw e;
             // TODO check if XmlParserSystemException is the right one; the test
             // wants it
             // throw new XmlParserSystemException(e);
         }
         catch (Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
 
         // filter = fh.getRules();
         // we need offset, limit etc. not in the map (ask TTE)
         filter.put("filter", fh.getRules());
         filter.put("limit", fh.getLimit());
         filter.put("offset", fh.getOffset());
         filter.put("order-by", fh.getOrderBy());
         filter.put("sorting", fh.getSorting());
 
         if (((Map) filter.get("filter")).isEmpty()) {
             filter.put("filter", null);
         }
 
         return filter;
     }
 
     /**
      * 
      * @param columnName
      * @param idSet
      * @return
      */
     private static String getQueryPartId(
         final String columnName, final Set idSet) {
         StringBuffer queryPart = new StringBuffer();
         String queryPartString = "";
         Set objects = idSet;
         // TODO or rule for every id
         if ((objects != null) && (objects.size() > 0)) {
             Iterator it = objects.iterator();
 
             while (it.hasNext()) {
                 String id = (String) it.next();
 
                 queryPart.append(columnName + "=" + "\'\"" + id + "\"\'");
                 if (it.hasNext()) {
                     queryPart.append(" OR ");
                 }
             }
             queryPartString = queryPart.toString();
         }
 
         return queryPartString;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seede.escidoc.core.common.business.fedora.IFTripleStoreFilterUtility#
      * getMemberList(java.lang.String)
      */
     public List<String> getMemberList(final String id, final String whereClause)
         throws TripleStoreSystemException {
 
         // TODO the return type of List should be uniform
 
         String tableWithMembers =
             getTableName(Constants.STRUCTURAL_RELATIONS_NS_URI + "member");
 
         if (tableWithMembers == null) {
             return new LinkedList<String>();
         }
 
         StringBuffer queryResultBuf = new StringBuffer();
         String queryResult = null;
 
         queryResultBuf.append("SELECT ");
         queryResultBuf.append(tableWithMembers);
         queryResultBuf.append(".o FROM ");
         queryResultBuf.append(tableWithMembers);
         queryResultBuf.append(" WHERE ");
         queryResultBuf.append(tableWithMembers);
         queryResultBuf.append(".s='<info:fedora/" + id + ">'");
         queryResult = queryResultBuf.toString();
 
         if (!checkQuery(queryResult)) {
             return new ArrayList<String>();
         }
         return executeSqlQuery(queryResult);
     }
 
     /**
      * Get list of Container member. These member list is not filtered for user
      * permission! (because we have to keep out AA components from common
      * package).
      */
     public List<String> getContainerMemberList(
         final String containerId, final Map filterMap, final String whereClause)
         throws SystemException, MissingMethodParameterException {
 
         String tableWithMembers =
             getTableName(Constants.STRUCTURAL_RELATIONS_NS_URI + "member");
         StringBuffer queryResultBuf = null;
         String queryResult = null;
         String filterCriteria = null;
 
         if (filterMap == null) {
             // prepare statement for empty filter
             queryResultBuf = new StringBuffer();
             queryResultBuf.append("SELECT ");
             queryResultBuf.append(tableWithMembers);
             queryResultBuf.append(".o FROM ");
             queryResultBuf.append(tableWithMembers);
             queryResultBuf.append(" WHERE ");
             queryResultBuf.append(tableWithMembers);
             queryResultBuf.append(".s='<info:fedora/" + containerId + ">'");
             queryResult = queryResultBuf.toString();
 
         }
         else {
 
             String tableWithIdentifier = null;
             String idColumn = null;
             filterCriteria =
                 getQueryPartId(idColumn, (Set) filterMap.remove("members"));
             tableWithIdentifier =
                 getTableName("http://purl.org/dc/elements/1.1/identifier");
             idColumn = tableWithIdentifier + ".o";
             // object type filter TODO now, we only have items
             // String type = (String) filter.remove("type");
             String objectType = (String) filterMap.remove("object-type");
             // String tableWithObjectType =
             // getTableName("http://www.nsdl.org/ontologies/relationships/objectType");
             // String objectTypeColumn = tableWithObjectType + ".o";
             // String queryPartObjectType =
             // objectTypeColumn + "=" + "\'\"" + objectType + "\"\'";
             if (objectType == null) {
                 // generic
                 StringBuffer queryPartPropertiesBuffer = new StringBuffer();
                 StringBuffer queryPartJoinPropertiesBuffer = new StringBuffer();
                 String propertiesPredicateItem = null;
                 String propertiesPredicateContainer = null;
                 String columnObjectItem = null;
                 String columnObjectContainer = null;
                 String columnSubjectItem = null;
                 String columnSubjectContainer = null;
                 String tablenameFirstInChain = null;
                 String tableNameFirst = null;
                 String tableNameNext = null;
                 String tableNameItem = null;
                 String tableNameContainer = null;
                 Vector<String> tableNames = new Vector<String>();
                 // Vector<String> columnNames = new Vector<String>();
                 Iterator it = filterMap.keySet().iterator();
                 while (it.hasNext()) {
                     String key = (String) it.next();
                     String val = (String) filterMap.get(key);
                     val = MPTStringUtil.escapeLiteralValueForSql(val);
                     if ("context-type".equals(key)) {
                         key = "type";
                     }
                     // val may be href, need id
                     if (val.startsWith("http://") || val.startsWith("/")) {
                         val = val.substring(val.lastIndexOf('/'));
                     }
                     propertiesPredicateItem =
                         Constants.ITEM_PROPERTIES_NAMESPACE_URI + "/" + key;
                     propertiesPredicateContainer =
                         "http://www.escidoc.de/schemas/container/0.1/" + key;
                     tableNameItem = getTableName(propertiesPredicateItem);
                     tableNameContainer =
                         getTableName(propertiesPredicateContainer);
                     tableNameNext = tableNameItem + tableNameContainer;
                     if (tableNameFirst == null) {
                         tablenameFirstInChain = tableNameNext;
                     }
                     tableNames.add(tableNameItem + tableNameContainer);
 
                     columnObjectItem = tableNameItem + ".o";
                     columnObjectContainer = tableNameContainer + ".o";
                     columnSubjectItem = tableNameItem + ".s";
                     columnSubjectContainer = tableNameContainer + ".s";
                     // columnNames.add(columnName);
                     // query += "and $object <http://www.escidoc.de/schemas/"
                     // + objectType + "/0.1/" + key + "> '" + val + "' ";
 
                     queryPartPropertiesBuffer.append("(SELECT ");
                     queryPartPropertiesBuffer.append(columnSubjectItem);
                     queryPartPropertiesBuffer.append(" FROM ");
                     queryPartPropertiesBuffer.append(tableNameItem);
                     queryPartPropertiesBuffer.append(" WHERE ");
                     if (key.equals("context")
                         || key.equals(Elements.ELEMENT_CONTENT_MODEL)) {
                         queryPartPropertiesBuffer.append(columnObjectItem
                             + "='<info:fedora/" + val + ">'");
                     }
                     else {
                         queryPartPropertiesBuffer.append(columnObjectItem
                             + "=\'\"" + val + "\"\'");
                     }
                     queryPartPropertiesBuffer.append(" UNION ");
                     queryPartPropertiesBuffer.append(" SELECT ");
                     queryPartPropertiesBuffer.append(columnSubjectContainer);
                     queryPartPropertiesBuffer.append(" FROM ");
                     queryPartPropertiesBuffer.append(tableNameContainer);
                     queryPartPropertiesBuffer.append(" WHERE ");
                     if (key.equals("context")
                         || key.equals(Elements.ELEMENT_CONTENT_MODEL)) {
                         queryPartPropertiesBuffer.append(columnObjectContainer
                             + "='<info:fedora/" + val + ">'");
                     }
                     else {
                         queryPartPropertiesBuffer.append(columnObjectContainer
                             + "=\'\"" + val + "\"\'");
                     }
                     queryPartPropertiesBuffer.append(") ");
                     queryPartPropertiesBuffer.append(tableNameNext);
                     if (tableNameFirst != null) {
                         queryPartJoinPropertiesBuffer.append(tableNameFirst
                             + ".s=" + tableNameNext + ".s");
                     }
 
                     if (it.hasNext()) {
                         queryPartPropertiesBuffer.append(", ");
 
                         queryPartJoinPropertiesBuffer.append(" AND ");
 
                     }
                     tableNameFirst = tableNameNext;
 
                 }
                 String queryPartProperties = "";
                 String queryPartJoinProperties = "";
                 String queryPartJoinMembersAndProperties = "";
 
                 if (tableNames.size() > 0) {
                     queryPartProperties = queryPartPropertiesBuffer.toString();
                     queryPartJoinProperties =
                         queryPartJoinPropertiesBuffer.toString();
                     queryPartJoinMembersAndProperties =
                         tablenameFirstInChain + ".s=" + tableWithMembers + ".o";
                 }
                 tableWithIdentifier =
                     getTableName("http://purl.org/dc/elements/1.1/identifier");
                 idColumn = tableWithIdentifier + ".o";
 
                 String joinIdentifierAndMembers = "";
                 queryResultBuf = new StringBuffer();
                 queryResultBuf.append("SELECT ");
                 queryResultBuf.append(tableWithMembers);
                 queryResultBuf.append(".o FROM ");
                 queryResultBuf.append(tableWithMembers);
                 // queryResultBuf.append("SELECT ");
                 // queryResultBuf.append(tableWithObjectType);
                 // queryResultBuf.append(".s FROM ");
 
                 if (!filterCriteria.equals("")) {
                     queryResultBuf.append(tableWithIdentifier);
                     queryResultBuf.append(",");
                     joinIdentifierAndMembers =
                         tableWithIdentifier + ".s=" + tableWithMembers
                             + ".o AND ";
                 }
                 // queryResultBuf.append(tableWithObjectType);
                 if (tableNames.size() > 0) {
                     queryResultBuf.append(",");
                     queryResultBuf.append(queryPartProperties);
 
                 }
                 queryResultBuf.append(" WHERE ");
 
                 queryResultBuf.append(joinIdentifierAndMembers);
 
                 queryResultBuf.append(queryPartJoinMembersAndProperties);
                 queryResultBuf.append(queryPartJoinProperties);
                 if (!queryPartJoinMembersAndProperties.equals("")) {
                     queryResultBuf.append(" AND ");
                 }
                 queryResultBuf.append(tableWithMembers);
                 queryResultBuf.append(".s='<info:fedora/" + containerId + ">'");
                 if (!filterCriteria.equals("")) {
                     queryResultBuf.append(" AND (");
                     queryResultBuf.append(filterCriteria);
                     queryResultBuf.append(")");
                 }
                 queryResult = queryResultBuf.toString();
             }
             else {
                 StringBuffer queryPartPropertiesBuffer = new StringBuffer();
                 StringBuffer queryPartJoinPropertiesBuffer = new StringBuffer();
                 Iterator it = filterMap.keySet().iterator();
                 String propertiesPredicate = null;
                 String columnName = null;
                 String tablenameFirstInChain = null;
                 String tableNameFirst = null;
                 String tableNameNext = null;
                 Vector<String> tableNames = new Vector<String>();
                 // Vector<String> columnNames = new Vector<String>();
                 while (it.hasNext()) {
                     String key = (String) it.next();
                     String val = (String) filterMap.get(key);
                     val = MPTStringUtil.escapeLiteralValueForSql(val);
                     if ("context-type".equals(key)) {
                         key = "type";
                     }
                     // val may be href, need id
                     if (val.startsWith("http://") || val.startsWith("/")) {
                         val = val.substring(val.lastIndexOf('/'));
                     }
                     propertiesPredicate =
                         "http://www.escidoc.de/schemas/" + objectType + "/0.1/"
                             + key;
                     tableNameNext = getTableName(propertiesPredicate);
                     if (tableNameFirst == null) {
                         tablenameFirstInChain = tableNameNext;
                     }
                     tableNames.add(tableNameNext);
                     columnName = tableNameNext + ".o";
                     // columnNames.add(columnName);
                     // query += "and $object <http://www.escidoc.de/schemas/"
                     // + objectType + "/0.1/" + key + "> '" + val + "' ";
                     if (key.equals("context")
                         || key.equals(Elements.ELEMENT_CONTENT_MODEL)) {
                         queryPartPropertiesBuffer.append(columnName
                             + "='<info:fedora/" + val + ">'");
                     }
                     else {
                         queryPartPropertiesBuffer.append(columnName + "=\'\""
                             + val + "\"\'");
                     }
                     if (tableNameFirst != null) {
                         queryPartJoinPropertiesBuffer.append(tableNameFirst
                             + ".s=" + tableNameNext + ".s");
                     }
 
                     if (it.hasNext()) {
                         queryPartPropertiesBuffer.append(" AND ");
 
                         queryPartJoinPropertiesBuffer.append(" AND ");
 
                     }
                     tableNameFirst = tableNameNext;
                 }
                 String queryPartProperties = "";
                 String queryPartJoinProperties = "";
                 String queryPartJoinMembersAndProperties = "";
 
                 if (tableNames.size() > 0) {
                     queryPartProperties = queryPartPropertiesBuffer.toString();
                     queryPartJoinProperties =
                         queryPartJoinPropertiesBuffer.toString();
                     queryPartJoinMembersAndProperties =
                         tablenameFirstInChain + ".s=" + tableWithMembers + ".o";
                 }
                 tableWithIdentifier =
                     getTableName("http://purl.org/dc/elements/1.1/identifier");
                 idColumn = tableWithIdentifier + ".o";
 
                 String joinIdentifierAndMembers = "";
                 queryResultBuf = new StringBuffer();
                 queryResultBuf.append("SELECT ");
                 queryResultBuf.append(tableWithMembers);
                 queryResultBuf.append(".o FROM ");
 
                 if (!filterCriteria.equals("")) {
                     queryResultBuf.append(tableWithIdentifier);
                     queryResultBuf.append(",");
                     joinIdentifierAndMembers =
                         tableWithIdentifier + ".s=" + tableWithMembers
                             + ".o AND ";
                 }
                 queryResultBuf.append(tableWithMembers);
                 if (tableNames.size() > 0) {
                     Iterator<String> iterator = tableNames.iterator();
                     while (iterator.hasNext()) {
                         queryResultBuf.append(",");
                         queryResultBuf.append(iterator.next());
                     }
 
                 }
                 queryResultBuf.append(" WHERE ");
 
                 queryResultBuf.append(joinIdentifierAndMembers);
 
                 queryResultBuf.append(queryPartJoinMembersAndProperties);
                 queryResultBuf.append(queryPartJoinProperties);
                 if (!queryPartJoinMembersAndProperties.equals("")) {
                     queryResultBuf.append(" AND ");
                 }
                 queryResultBuf.append(tableWithMembers);
                 queryResultBuf.append(".s='<info:fedora/" + containerId + ">'");
                 if (!queryPartProperties.equals("")) {
                     queryResultBuf.append(" AND ");
                     queryResultBuf.append(queryPartProperties);
                 }
 
                 if (!filterCriteria.equals("")) {
                     queryResultBuf.append(" AND (");
                     queryResultBuf.append(filterCriteria);
                     queryResultBuf.append(")");
                 }
                 queryResult = queryResultBuf.toString();
             }
         }
 
         if (!checkQuery(queryResult)) {
             return new ArrayList<String>();
         }
 
         return executeSqlQuery(queryResult);
     }
 
     /**
      * 
      * @param query
      * @return
      */
     private boolean checkQuery(final String query) {
         boolean result = false;
         if ((query != null) && (query.indexOf("null.") == -1)) {
             result = true;
         }
         return result;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seede.escidoc.core.common.business.fedora.IFTripleStoreFilterUtility#
      * getContextMemberList(java.lang.String, java.lang.String)
      */
 
     public List<String> getContextMemberList(
         final String contextId, final Map filterMap, final String whereClause)
         throws SystemException, MissingMethodParameterException {
 
         // TODO check functionality
         List<String> result = null;
         result =
             evaluate("member", filterMap, "* <"
                 + Constants.STRUCTURAL_RELATIONS_NS_URI
                 + "context> <info:fedora/" + contextId + ">");
         // $parent "
         // + "and $parent <" + PROP_OBJECT_TYPE
         // + "> <http://escidoc.de/core/01/resources/Context> "
         // + "and $parent <http://purl.org/dc/elements/1.1/identifier> '"
         // + contextId + "' ");
         return result;
     }
 
     // public List<String> getContextMemberList(
     // final String contextId, final String filterParam)
     // throws SystemException, MissingMethodParameterException {
     // Map filter = null;
     // if (filterParam != null) {
     // try {
     // filter = getFilterMap(filterParam);
     // }
     // catch (Exception e) {
     // throw new XmlParserSystemException("While parse param filter.",
     // e);
     // }
     //
     // }
     // String tableWithIdentifier =
     // getTableName("http://purl.org/dc/elements/1.1/identifier");
     // String idColumn = tableWithIdentifier + ".o";
     //
     // String tableWithItemContexts =
     // getTableName(Constants.ITEM_PROPERTIES_NAMESPACE_URI + "/context");
     // String tableWithContainerContexts =
     // getTableName(Constants.CONTAINER_PROPERTIES_NAMESPACE_URI
     // + "/context");
     //
     // String queryResult = null;
     // String roleCriteria = null;
     // String userCriteria = null;
     // String filterCriteria = null;
     //
     // if ((filterParam == null) || filter == null) {
     // StringBuffer queryResultBuf = new StringBuffer();
     // queryResultBuf.append("SELECT ");
     // queryResultBuf.append(tableWithItemContexts);
     // queryResultBuf.append(".s FROM ");
     // queryResultBuf.append(tableWithItemContexts);
     // queryResultBuf.append(" WHERE ");
     // queryResultBuf.append(tableWithItemContexts);
     // queryResultBuf.append(".o='<info:fedora/");
     // queryResultBuf.append(contextId + ">'");
     // queryResultBuf.append(" UNION SELECT ");
     // queryResultBuf.append(tableWithContainerContexts);
     // queryResultBuf.append(".s FROM ");
     // queryResultBuf.append(tableWithContainerContexts);
     // queryResultBuf.append(" WHERE ");
     // queryResultBuf.append(tableWithContainerContexts);
     // queryResultBuf.append(".o='<info:fedora/");
     // queryResultBuf.append(contextId + ">'");
     // queryResult = queryResultBuf.toString();
     //
     // }
     // else {
     //
     // // ID filter; items TODO other objects
     //
     // filterCriteria =
     // getQueryPartId(idColumn, (Set) filter.remove("members"));
     //
     // // object type filter TODO now, we only have items
     // // String type = (String) filter.remove("type");
     // // if (type != null) {
     // // // query += " and $object
     // // <http://www.nsdl.org/ontologies/relationships/objectType> '"
     // // // + type + "' ";
     // // }
     // // meeting 2007-01-25 type -> object-type
     // String type = (String) filter.remove("object-type");
     //
     // // user
     // userCriteria = (String) filter.remove("user");
     // // role
     // roleCriteria = (String) filter.remove("role");
     //
     // // public-status (is property)
     // // content-model (is property)
     // // generic (including public-status and content-model)
     //
     // // only content-model, status
     // // status
     //
     // if (type == null) {
     // StringBuffer queryPartPropertiesBuffer = new StringBuffer();
     // StringBuffer queryPartJoinPropertiesBuffer = new StringBuffer();
     // Iterator it = filter.keySet().iterator();
     // String propertiesPredicateItem = null;
     // String propertiesPredicateContainer = null;
     // String columnObjectItem = null;
     // String columnObjectContainer = null;
     // String columnSubjectItem = null;
     // String columnSubjectContainer = null;
     // String tablenameFirstInChain = null;
     // String tableNameFirst = null;
     // String tableNameNext = null;
     // String tableNameItem = null;
     // String tableNameContainer = null;
     // Vector<String> tableNames = new Vector<String>();
     // // Vector<String> columnNames = new Vector<String>();
     // while (it.hasNext()) {
     // String key = (String) it.next();
     // String val = (String) filter.get(key);
     // val = MPTStringUtil.escapeLiteralValueForSql(val);
     //
     // if ((TripleStoreUtility.PROP_PUBLIC_STATUS.equals(key))
     // || (Elements.ELEMENT_CONTENT_MODEL.equals(key))) {
     //
     // // val may be href, need id
     // if (val.startsWith("http://") || val.startsWith("/")) {
     // val = val.substring(val.lastIndexOf('/'));
     // }
     // propertiesPredicateItem =
     // Constants.ITEM_PROPERTIES_NAMESPACE_URI + "/" + key;
     // propertiesPredicateContainer =
     // Constants.CONTAINER_PROPERTIES_NAMESPACE_URI + "/"
     // + key;
     // tableNameItem = getTableName(propertiesPredicateItem);
     // tableNameContainer =
     // getTableName(propertiesPredicateContainer);
     // tableNameNext = tableNameItem + tableNameContainer;
     // if (tableNameFirst == null) {
     // tablenameFirstInChain = tableNameNext;
     // }
     // tableNames.add(tableNameItem + tableNameContainer);
     //
     // columnObjectItem = tableNameItem + ".o";
     // columnObjectContainer = tableNameContainer + ".o";
     // columnSubjectItem = tableNameItem + ".s";
     // columnSubjectContainer = tableNameContainer + ".s";
     // // columnNames.add(columnName);
     // // query += "and $object
     // // <http://www.escidoc.de/schemas/"
     // // + objectType + "/0.1/" + key + "> '" + val + "' ";
     //
     // queryPartPropertiesBuffer.append("(SELECT ");
     // queryPartPropertiesBuffer.append(columnSubjectItem);
     // queryPartPropertiesBuffer.append(" FROM ");
     // queryPartPropertiesBuffer.append(tableNameItem);
     // queryPartPropertiesBuffer.append(" WHERE ");
     // if (key.equals("context")
     // || key.equals(Elements.ELEMENT_CONTENT_MODEL)) {
     // queryPartPropertiesBuffer.append(columnObjectItem
     // + "='<info:fedora/" + val + ">'");
     // }
     // else {
     // queryPartPropertiesBuffer.append(columnObjectItem
     // + "= \'\"" + val + "\"\'");
     // }
     // queryPartPropertiesBuffer.append(" UNION SELECT ");
     // queryPartPropertiesBuffer
     // .append(columnSubjectContainer);
     // queryPartPropertiesBuffer.append(" FROM ");
     // queryPartPropertiesBuffer.append(tableNameContainer);
     // queryPartPropertiesBuffer.append(" WHERE ");
     // if (key.equals("context")
     // || key.equals(Elements.ELEMENT_CONTENT_MODEL)) {
     // queryPartPropertiesBuffer
     // .append(columnObjectContainer
     // + "='<info:fedora/" + val + ">'");
     // }
     // else {
     // queryPartPropertiesBuffer
     // .append(columnObjectContainer + "=\'\"" + val
     // + "\"\'");
     // }
     // queryPartPropertiesBuffer.append(") ");
     // queryPartPropertiesBuffer.append(tableNameNext);
     // if (tableNameFirst != null) {
     // queryPartJoinPropertiesBuffer.append(tableNameFirst
     // + ".s=" + tableNameNext + ".s");
     // }
     //
     // if (it.hasNext()) {
     // queryPartPropertiesBuffer.append(", ");
     //
     // queryPartJoinPropertiesBuffer.append(" AND ");
     //
     // }
     // tableNameFirst = tableNameNext;
     // }
     //
     // }
     // String queryPartProperties = "";
     // String queryPartJoinProperties = "";
     // String queryPartJoinContextsAndProperties = "";
     // String tableWithContextes =
     // tableWithItemContexts + tableWithContainerContexts;
     // if (tableNames.size() > 0) {
     // queryPartProperties = queryPartPropertiesBuffer.toString();
     // queryPartJoinProperties =
     // queryPartJoinPropertiesBuffer.toString();
     // queryPartJoinContextsAndProperties =
     // tablenameFirstInChain + ".s=" + tableWithContextes
     // + ".s";
     // }
     //
     // String joinIdentifierAndContext = "";
     //
     // // //////////////////////
     // StringBuffer queryResultBuf = new StringBuffer();
     // queryResultBuf.append("SELECT ");
     // queryResultBuf.append(tableWithContextes + ".s");
     // queryResultBuf.append(" FROM (SELECT ");
     // queryResultBuf.append(tableWithItemContexts);
     // queryResultBuf.append(".s FROM ");
     // queryResultBuf.append(tableWithItemContexts);
     // queryResultBuf.append(" WHERE ");
     // queryResultBuf.append(tableWithItemContexts);
     // queryResultBuf.append(".o='<info:fedora/");
     // queryResultBuf.append(contextId + ">'");
     // queryResultBuf.append(" UNION SELECT ");
     // queryResultBuf.append(tableWithContainerContexts);
     // queryResultBuf.append(".s FROM ");
     // queryResultBuf.append(tableWithContainerContexts);
     // queryResultBuf.append(" WHERE ");
     // queryResultBuf.append(tableWithContainerContexts);
     // queryResultBuf.append(".o='<info:fedora/");
     // queryResultBuf.append(contextId + ">') ");
     // queryResultBuf.append(tableWithContextes);
     // if (!filterCriteria.equals("")) {
     // queryResultBuf.append(tableWithIdentifier);
     // queryResultBuf.append(",");
     // joinIdentifierAndContext =
     // tableWithIdentifier + ".s=" + tableWithContextes
     // + ".s AND ";
     // }
     // // queryResultBuf.append(tableWithObjectType);
     // if (tableNames.size() > 0) {
     // queryResultBuf.append(",");
     // queryResultBuf.append(queryPartProperties);
     //
     // }
     // if (!filterCriteria.equals("")
     // || !queryPartJoinContextsAndProperties.equals("")) {
     // queryResultBuf.append(" WHERE ");
     // }
     // queryResultBuf.append(joinIdentifierAndContext);
     // queryResultBuf.append(queryPartJoinContextsAndProperties);
     // queryResultBuf.append(queryPartJoinProperties);
     //
     // if (!filterCriteria.equals("")) {
     // queryResultBuf.append(" AND (");
     // queryResultBuf.append(filterCriteria);
     // queryResultBuf.append(")");
     // }
     // queryResult = queryResultBuf.toString();
     //
     // }
     // else {
     // // FIXME a provider for ALL schema versions dependant on the
     // // type has to be created and used here
     // // FIXME this is only a quick fix
     // String version = "/0.1/";
     // if ((Constants.ITEM_OBJECT_TYPE.equals(type))
     // || (Constants.CONTAINER_OBJECT_TYPE.equals(type))) {
     // version = "/0.3/";
     // }
     // else if (Constants.CONTEXT_OBJECT_TYPE.equals(type)) {
     // version = "/0.3/";
     // }
     //
     // String typePredicate =
     // "http://www.escidoc.de/schemas/" + type + version;
     //
     // String tableNameContext =
     // getTableName(typePredicate + "context");
     // StringBuffer queryPartPropertiesBuffer = new StringBuffer();
     // StringBuffer queryPartJoinPropertiesBuffer = new StringBuffer();
     // Iterator it = filter.keySet().iterator();
     // String propertiesPredicate = null;
     // String columnName = null;
     // String tablenameFirstInChain = null;
     // String tableNameFirst = null;
     // String tableNameNext = null;
     // Vector<String> tableNames = new Vector<String>();
     // // Vector<String> columnNames = new Vector<String>();
     // while (it.hasNext()) {
     // String key = (String) it.next();
     // String val = (String) filter.get(key);
     // val = MPTStringUtil.escapeLiteralValueForSql(val);
     // if ((Elements.ELEMENT_CONTENT_MODEL.equals(key))
     // || (TripleStoreUtility.PROP_PUBLIC_STATUS.equals(key))) {
     //
     // // val may be href, need id
     // if (val.startsWith("http://") || val.startsWith("/")) {
     // val = val.substring(val.lastIndexOf('/'));
     // }
     // // FIXME a provider for ALL schema versions dependant on
     // // the type has to be created and used here
     // // FIXME this is only a quick fix
     // version = "/0.1/";
     // if ((Constants.ITEM_OBJECT_TYPE.equals(type))
     // || (Constants.CONTAINER_OBJECT_TYPE.equals(type))) {
     // version = "/0.3/";
     // }
     // else if (Constants.CONTEXT_OBJECT_TYPE.equals(type)) {
     // version = "/0.3/";
     // }
     //
     // propertiesPredicate =
     // "http://www.escidoc.de/schemas/" + type + version
     // + key;
     // tableNameNext = getTableName(propertiesPredicate);
     // if (tableNameFirst == null) {
     // tablenameFirstInChain = tableNameNext;
     // }
     // tableNames.add(tableNameNext);
     // columnName = tableNameNext + ".o";
     // // columnNames.add(columnName);
     // // query += "and $object
     // // <http://www.escidoc.de/schemas/"
     // // + objectType + "/0.1/" + key + "> '" + val + "' ";
     // if (key.equals("context")
     // || key.equals(Elements.ELEMENT_CONTENT_MODEL)) {
     // queryPartPropertiesBuffer.append(columnName
     // + "='<info:fedora/" + val + ">'");
     // }
     // else {
     // queryPartPropertiesBuffer.append(columnName + "="
     // + "\'\"" + val + "\"\'");
     // }
     // if (tableNameFirst != null) {
     // queryPartJoinPropertiesBuffer.append(tableNameFirst
     // + ".s=" + tableNameNext + ".s");
     // }
     //
     // if (it.hasNext()) {
     // queryPartPropertiesBuffer.append(" AND ");
     //
     // queryPartJoinPropertiesBuffer.append(" AND ");
     //
     // }
     // tableNameFirst = tableNameNext;
     // }
     // }
     // String queryPartProperties = "";
     // String queryPartJoinProperties = "";
     // String queryPartJoinContextAndProperties = "";
     //
     // if (tableNames.size() > 0) {
     // queryPartProperties = queryPartPropertiesBuffer.toString();
     // queryPartJoinProperties =
     // queryPartJoinPropertiesBuffer.toString();
     // queryPartJoinContextAndProperties =
     // tablenameFirstInChain + ".s=" + tableNameContext + ".s";
     // }
     //
     // String joinIdentifierAndContext = "";
     // StringBuffer queryResultBuf = new StringBuffer();
     // queryResultBuf.append("SELECT ");
     // queryResultBuf.append(tableNameContext);
     // queryResultBuf.append(".s FROM ");
     //
     // if (!filterCriteria.equals("")) {
     // queryResultBuf.append(tableWithIdentifier);
     // queryResultBuf.append(",");
     // joinIdentifierAndContext =
     // tableWithIdentifier + ".s=" + tableNameContext
     // + ".s AND ";
     // }
     // queryResultBuf.append(tableNameContext);
     // if (tableNames.size() > 0) {
     // Iterator<String> iterator = tableNames.iterator();
     // while (iterator.hasNext()) {
     // queryResultBuf.append(",");
     // queryResultBuf.append(iterator.next());
     // }
     //
     // }
     // queryResultBuf.append(" WHERE ");
     //
     // queryResultBuf.append(joinIdentifierAndContext);
     //
     // queryResultBuf.append(queryPartJoinContextAndProperties);
     // queryResultBuf.append(queryPartJoinProperties);
     // if (!queryPartJoinContextAndProperties.equals("")) {
     // queryResultBuf.append(" AND ");
     // queryResultBuf.append(queryPartProperties);
     // queryResultBuf.append(" AND ");
     // }
     // queryResultBuf.append(tableNameContext);
     // queryResultBuf.append(".o='<info:fedora/" + contextId + ">'");
     //
     // if (!filterCriteria.equals("")) {
     // queryResultBuf.append(" AND (");
     // queryResultBuf.append(filterCriteria);
     // queryResultBuf.append(")");
     // }
     // queryResult = queryResultBuf.toString();
     // }
     // //
     // }
     // if (!checkQuery(queryResult)) {
     // return new ArrayList<String>();
     // }
     // List<String> resultList = getListFromSimpleQuerySingleCol(queryResult);
     //
     // if (!(userCriteria == null && roleCriteria == null)) {
     // resultList =
     // filterUserRole("member", roleCriteria, userCriteria, resultList);
     // }
     //
     // return resultList;
     // }
 
     // private List<String> getListFromSimpleQuerySingleCol(final String query)
     // throws TripleStoreSystemException {
     //
     // return executeQuery(query);
     // }
 
     /*
      * (non-Javadoc)
      * 
      * @seede.escidoc.core.common.business.fedora.IFTripleStoreFilterUtility#
      * getObjectRefs(java.lang.String, java.lang.String)
      */
     public String getObjectRefs(
         final String objectType, final Map filterMap, final String whereClause)
         throws SystemException, MissingMethodParameterException {
 
         List<String> list = evaluate(objectType, filterMap, whereClause);
 
         String absoluteLocalPathFirstPart = "ir";
         if (objectType.equals(Elements.ELEMENT_CONTENT_MODEL)) {
             absoluteLocalPathFirstPart = "ctm";
         }
 
         String namespacePrefix = objectType + "-ref-list";
         String schemaVersion = "0.2";
         if (objectType.equals("item")) {
             schemaVersion = "0.3";
         }
         String namespaceUri =
             "http://www.escidoc.de/schemas/" + objectType + "reflist/"
                 + schemaVersion;
         String rootElementName = objectType + "-ref-list";
         String listElementName = objectType + "-ref";
 
         String prefixedRootElement = namespacePrefix + ":" + rootElementName;
         String prefixedListElement = namespacePrefix + ":" + listElementName;
 
         String namespaceDecl =
             " xmlns:" + namespacePrefix + "=\"" + namespaceUri + "\" ";
 
         StringBuffer sb = new StringBuffer();
 
         sb.append("<");
         sb.append(prefixedRootElement);
 
         sb.append(namespaceDecl);
         if (UserContext.isRestAccess()) {
             sb.append(" xlink:title=\"list of ");
             sb.append(objectType);
             sb
                 .append(" references\" xlink:type=\"simple\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
             sb.append(" xml:base=\"");
             sb.append(XmlUtility.getEscidocBaseUrl() + "\"");
         }
         sb.append(">");
 
         Iterator<String> it = list.iterator();
         while (it.hasNext()) {
             String id = it.next();
             sb.append("<");
             sb.append(prefixedListElement);
             if (UserContext.isRestAccess()) {
                 sb.append(" xlink:href=\"/");
                 sb.append(absoluteLocalPathFirstPart);
                 sb.append("/");
                 sb.append(objectType);
                 sb.append("/");
                 sb.append(id);
                 sb.append("\" xlink:type=\"simple\"");
             }
             else {
                 sb.append(" objid=\"");
                 sb.append(id);
                 sb.append("\"");
             }
             sb.append(" />");
         }
         sb.append("</");
         sb.append(prefixedRootElement);
         sb.append(">");
         String resultListXml = sb.toString();
         return resultListXml;
     }
 
     /**
      * Get name of table for predicate.
      * 
      * @param predicate
      *            Predicate (from SPO).
      * @return name of table where predicate name is used.
      * @throws TripleStoreSystemException
      *             Thrown if request of TripleStore failed.
      */
     public String getTableName(final String predicate)
         throws TripleStoreSystemException {
         String result = null;
         if (predicate != null) {
             URIReference predicateNode = null;
             try {
                 predicateNode = new URIReference(predicate);
 
                 result = getTableManager().getTableFor(predicateNode);
                 if (result == null) {
                     reinitialize();
                     result = getTableManager().getTableFor(predicateNode);
                 }
             }
             catch (URISyntaxException e) {
                 throw new TripleStoreSystemException(e);
             }
         }
         return result;
     }
 
     /**
      * 
      * @param pid
      *            The Id of the object.
      * @param fullqualifiedPropertyName
      *            The full qualified property name.
      * 
      * @return Value of property element.
      * @throws TripleStoreSystemException
      *             If access to the triple store fails.
      */
     @Override
     public String getPropertiesElements(
         final String pid, final String fullqualifiedPropertyName)
         throws TripleStoreSystemException {
 
         // TODO refactor to getPropertiesElement
 
         String value = null;
         List<String> results =
             executeQueryId(pid, false, fullqualifiedPropertyName);
 
         Iterator<String> it = results.iterator();
         // work around for more than one dc:identifier
         while (it.hasNext()) {
             value = it.next();
             if (!fullqualifiedPropertyName
                 .equals("http://purl.org/dc/elements/1.1/identifier")) {
                 break;
             }
             else if (pid.equals(value)) {
                 break;
             }
         }
         return value;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seede.escidoc.core.common.business.fedora.IFTripleStoreFilterUtility#
      * reinitialize()
      */
     public Object reinitialize() throws TripleStoreSystemException {
         return setUpTableManager();
     }
 
     /**
      * Sets up the table manager.<br>
      * The table manager is created in order to get the current table mappings.
      * 
      * @return Returns the created table manager.
      * @throws TripleStoreSystemException
      *             If access to the triple store fails.
      */
     private TableManager setUpTableManager() throws TripleStoreSystemException {
 
         TableManager result = null;
         try {
             result =
                 new BasicTableManager(getDataSource(),
                     new PostgresDDLGenerator(), "tMap", "t");
         }
         catch (SQLException e1) {
             throw new TripleStoreSystemException(e1.getMessage());
         }
         setTableManager(result);
         return result;
     }
 
     /**
      * If entry is an URI Identifier, the method extracts the contained id, if
      * the entry is a literal value, the method removes the leading and trailing
      * quote (").
      * 
      * @param entry
      *            The entry (result of a triplestore query)
      * @return The result as described above.
      * 
      * @throws TripleStoreSystemException
      *             If access to the triple store fails.
      */
     private String getValue(final String entry)
         throws TripleStoreSystemException {
         String result = null;
         try {
             result = NTriplesUtil.unescapeLiteralValue(entry);
             if (result != null) {
                 if (result.startsWith("<info") || result.startsWith("info")) {
                     result = XmlUtility.getIdFromURI(result);
                 }
                 else if (result.startsWith("<") && result.endsWith(">")) {
                     result = result.substring(1, result.length() - 1);
                 }
                 else if (result.startsWith("\"")) {
                     result = result.substring(1, result.lastIndexOf("\""));
                 }
                 result = XmlUtility.escapeForbiddenXmlCharacters(result);
             }
         }
         catch (ParseException e) {
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         return result;
     }
 
     /**
      * Excecute query on TripleStore.
      * 
      * @param query
      *            TripleStore query
      * @return result list for request.
      * @throws TripleStoreSystemException
      *             If access to the triple store fails.
      */
     public List<String> executeSqlQuery(final String query)
         throws TripleStoreSystemException {
 
         List<String> result = new LinkedList<String>();
         Connection con = null;
         ResultSet rs = null;
         try {
             con = getConnection();
             rs = con.prepareStatement(query).executeQuery();
             while (rs.next()) {
                 String entry = getValue(rs.getString(1));
                 // entry = NTriplesUtil.unescapeLiteralValue(value);
 
                 result.add(entry);
             }
         }
         catch (CannotGetJdbcConnectionException e) {
             String msg = "Failed to get JDBC connection " + e;
             log.error(msg);
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         catch (SQLException e) {
             String msg = "Failed to execute query '" + query + "'. " + e;
             log.error(msg);
             throw new TripleStoreSystemException("Failed to execute query "
                 + query, e);
         }
         finally {
             if (con != null) {
                 releaseConnection(con);
             }
             if (rs != null) {
                 try {
                     rs.close();
                 }
                 catch (SQLException e) {
                     log.error(e);
                     // Ignore because the result set is already closed.
                 }
             }
         }
         return result;
 
     }
 
     // public Vector<String> executeQuery(final String sqlQuery)
     // throws TripleStoreSystemException {
     //
     // Vector<String> result = new Vector<String>();
     // Connection con = null;
     // ResultSet rs = null;
     // try {
     // con = getConnection();
     // rs = con.prepareStatement(sqlQuery).executeQuery();
     // while (rs.next()) {
     // String resultValue = rs.getString(1);
     // if (resultValue.startsWith("\"")) {
     // resultValue =
     // resultValue.substring(1, resultValue.length() - 1);
     // try {
     // resultValue =
     // NTriplesUtil.unescapeLiteralValue(resultValue);
     // }
     // catch (ParseException e) {
     // throw new TripleStoreSystemException(e.getMessage(), e);
     // }
     // }
     // else if (resultValue.startsWith("<")) {
     // resultValue = XmlUtility.getIdFromURI(resultValue);
     // }
     // result.add(resultValue);
     // }
     // }
     // catch (CannotGetJdbcConnectionException e) {
     // throw new TripleStoreSystemException(e.getMessage(), e);
     // }
     // catch (SQLException e) {
     // e.printStackTrace();
     // throw new TripleStoreSystemException("Failed to execute query "
     // + sqlQuery, e);
     // }
     // finally {
     // if (con != null) {
     // releaseConnection(con);
     // }
     // if (rs != null) {
     // try {
     // rs.close();
     // }
     // catch (SQLException e) {
     // // Ignore because the result set is already closed.
     // }
     // }
     //
     // }
     // return result;
     // }
 
     /**
      * @return the tableManager
      * @throws TripleStoreSystemException
      *             If access to the triple store fails.
      */
     public TableManager getTableManager() throws TripleStoreSystemException {
         if (tableManager == null) {
             setUpTableManager();
         }
         return tableManager;
     }
 
     /**
      * @param tableManager
      *            the tableManager to set
      */
     public void setTableManager(final TableManager tableManager) {
         this.tableManager = tableManager;
     }
 
     // CHECKSTYLE:JAVADOC-OFF
 
     @Override
     public String getObjectList(
         final String objectType, final Map filter, final String whereClause)
         throws InvalidContentException, TripleStoreSystemException,
         MissingMethodParameterException {
 
         throw new UnsupportedOperationException(
             "Not implemented for MPTTripleStore");
     }
 
     /**
      * Builds the starting clause of a query to the triple store to retrieve
      * objects.
      * 
      * @param targetIsSubject
      *            targetIsSubject
      * @return Returns the starting clause "SELECT PREDICATE_TABLE.S FROM " in a
      *         {@link StringBuffer}
      * @common
      */
     @Override
     public StringBuffer getRetrieveSelectClause(
         final boolean targetIsSubject, final String predicateId)
         throws TripleStoreSystemException {
         // Initialize select clause
         StringBuffer retrieveSelectClauseBuf = new StringBuffer("SELECT ");
         String creationDateTable = null;
         if (predicateId == null) {
             creationDateTable = this.tableWithPredicate;
         }
         else {
             creationDateTable = getTableName(predicateId);
         }
 
         retrieveSelectClauseBuf.append(creationDateTable);
         if (targetIsSubject) {
             retrieveSelectClauseBuf.append(".s AS temp FROM ");
         }
         else {
             retrieveSelectClauseBuf.append(".o AS temp FROM ");
         }
 
         return new StringBuffer(retrieveSelectClauseBuf);
     }
 
     /**
      * Test id a resource with provided id exists.
      * 
      * @param pid
      *            Fedora objid.
      * @throws TripleStoreSystemException
      *             If access to the triple store fails.
      */
     public boolean exists(final String pid) throws TripleStoreSystemException {
         Connection connection = null;
         ResultSet resultSet = null;
         String query = null;
         try {
             String table = getTableName(Fedora_Creation_Date_Predicate);
             if (table == null) {
                 return false;
             }
             StringBuffer queryBuffer = new StringBuffer();
             queryBuffer.append("SELECT ");
             queryBuffer.append(table);
             queryBuffer.append(".o FROM ");
             queryBuffer.append(table);
             queryBuffer.append(" WHERE (");
             queryBuffer.append(table);
             queryBuffer.append(" .s = '");
             queryBuffer.append(new URIReference("info:fedora/" + pid)
                 .toString());
             queryBuffer.append("')");
             query = queryBuffer.toString();
             connection = getConnection();
             resultSet = connection.prepareStatement(query).executeQuery();
             if (resultSet.next()) {
                 return true;
             }
             return false;
         }
         catch (URISyntaxException e) {
             log.error(e);
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         catch (CannotGetJdbcConnectionException e) {
             log.error(e);
             throw new TripleStoreSystemException(e.getMessage(), e);
         }
         catch (SQLException e) {
             log.error(e);
             throw new TripleStoreSystemException("Failed to execute query "
                 + query, e);
         }
         catch (SystemException e) {
             log.error(e);
             throw new TripleStoreSystemException(
                 "Failed to escape forbidden xml characters ", e);
         }
         finally {
             if (connection != null) {
                 releaseConnection(connection);
             }
             if (resultSet != null) {
                 try {
                     resultSet.close();
                 }
                 catch (SQLException e) {
                     log.error(e);
                     // Ignore because the result set is already closed.
                 }
             }
         }
 
     }
 
     /**
      * Builds the starting clause of a query to the triple store.
      * 
      * @param targetIsSubject
      *            Flag indicating that the target to search for is the subject (
      *            <code>true</code>) or the object (<code>false</code>) of the
      *            specified predicate.
      * @param predicateId
      *            The predicate id. If this equals to the id predicate (see
      *            de.escidoc.core.common.business.Constants.DC_IDENTIFIER_URI),
      *            the provided value of the parameter
      *            <code>targetIsSubject</code> is ignored and it is assumed it
      *            has been set to <code>true</code>.
      * @param expectedValue
      *            The value that must be matched by the specified predicate. If
      *            <code>targetIsSubject</code> is <code>true</code>, the object
      *            of the predicate must match the value. Otherwise the subject
      *            must match the value.<br/>
      *            In case of the id attribute, the expected value is ignored and
      *            $s is used for creating $s &lt;predicate&gt; $s clause part.
      * @param targetResourceType
      *            The object type of the target of the query. If this is
      *            <code>null</code>, no restriction for expected resource type
      *            is added.
      * @param contentModelTitleId
      *            The id of the predicate pointing to the title of the content
      *            model. If this is <code>null</code>, targets of any content
      *            model are searched.
      * @param contentModelTitle
      *            The content model title that the subject must match. This must
      *            not be <code>null</code>, if contentModelTitleId is not
      *            <code>null</code>.
      * 
      * 
      * @return Returns the where clause searching for the specified subjects.
      * @common
      */
     @Override
     public StringBuffer getRetrieveWhereClause(
         final boolean targetIsSubject, final String predicateId,
         final String expectedValue, final String targetResourceType,
         final String contentModelTitleId, final String contentModelTitle)
         throws TripleStoreSystemException {
         boolean isFirst = true;
 
         int braceToAddAtBeginn = 0;
         String creationDateTable = getTableName(Fedora_Creation_Date_Predicate);
         // String dcIdentifierTable = getTableName(Constants.DC_IDENTIFIER_URI);
         final String contentModelTitleTableName = getTableName(PROP_TITLE);
         final String contentModelOfObjectTableName =
             getTableName(contentModelTitleId);
         final String tableWithObjectType = getTableName(PROP_OBJECT_TYPE);
         this.tableWithPredicate = getTableName(predicateId);
         StringBuffer faultCase = new StringBuffer();
         if (this.tableWithPredicate != null) {
             faultCase.append(this.tableWithPredicate);
             faultCase.append(" WHERE ");
             faultCase.append(this.tableWithPredicate);
             faultCase.append(".s=\'\"bla\"\'");
         }
         else {
             faultCase.append(creationDateTable);
             faultCase.append(" WHERE ");
             faultCase.append(creationDateTable);
             faultCase.append(".s=\'\"bla\"\'");
             return faultCase;
         }
         final StringBuffer queryPart = new StringBuffer();
 
         final StringBuffer queryPartJoinObjectTypeWithPredicateBuffer =
             new StringBuffer();
         queryPartJoinObjectTypeWithPredicateBuffer.append(tableWithPredicate);
         queryPartJoinObjectTypeWithPredicateBuffer.append(" ON ");
         queryPartJoinObjectTypeWithPredicateBuffer.append(tableWithObjectType);
         queryPartJoinObjectTypeWithPredicateBuffer.append(".s=");
         queryPartJoinObjectTypeWithPredicateBuffer.append(tableWithPredicate);
         if (targetIsSubject) {
             queryPartJoinObjectTypeWithPredicateBuffer.append(".s");
         }
         else {
             queryPartJoinObjectTypeWithPredicateBuffer.append(".o");
         }
         String queryPartJoinObjectTypeWithPredicate =
             queryPartJoinObjectTypeWithPredicateBuffer.toString();
 
         final StringBuffer queryPartJoinContentModelWithPredicateBuffer =
             new StringBuffer();
         queryPartJoinContentModelWithPredicateBuffer.append(tableWithPredicate);
         queryPartJoinContentModelWithPredicateBuffer.append(" ON ");
         queryPartJoinContentModelWithPredicateBuffer
             .append(contentModelOfObjectTableName);
         queryPartJoinContentModelWithPredicateBuffer.append(".s=");
         queryPartJoinContentModelWithPredicateBuffer.append(tableWithPredicate);
         if (targetIsSubject) {
             queryPartJoinContentModelWithPredicateBuffer.append(".s");
         }
         else {
             queryPartJoinContentModelWithPredicateBuffer.append(".o");
         }
         String queryPartJoinContentModelWithPredicate =
             queryPartJoinContentModelWithPredicateBuffer.toString();
 
         if ((contentModelTitleId != null) && (contentModelTitle != null)) {
             if ((contentModelTitleTableName == null)
                 || (contentModelOfObjectTableName == null)) {
                 return faultCase;
             }
             isFirst = false;
             queryPart.append("(");
             queryPart.append(contentModelOfObjectTableName);
             queryPart.append(" INNER JOIN ");
             queryPart.append(contentModelTitleTableName);
             queryPart.append(" ON ");
             queryPart.append(contentModelOfObjectTableName);
             queryPart.append(".o=");
             queryPart.append(contentModelTitleTableName);
             queryPart.append(".s)");
         }
         if (targetResourceType != null) {
             if (tableWithObjectType == null) {
                 return faultCase;
             }
             if (!isFirst) {
                 braceToAddAtBeginn++;
                 queryPart.append(" INNER JOIN ");
                 queryPart.append(tableWithObjectType);
                 queryPart.append(" ON ");
                 queryPart.append(contentModelOfObjectTableName);
                 queryPart.append(".s=");
                 queryPart.append(tableWithObjectType);
                 queryPart.append(".s) INNER JOIN ");
                 queryPart.append(queryPartJoinObjectTypeWithPredicate);
 
             }
             else {
                 isFirst = false;
                 queryPart.append(tableWithObjectType);
                 queryPart.append(" INNER JOIN ");
                 queryPart.append(queryPartJoinObjectTypeWithPredicate);
             }
 
         }
         else {
             if (!isFirst) {
                 queryPart.append(" INNER JOIN ");
                 queryPart.append(queryPartJoinContentModelWithPredicate);
             }
             else {
                 queryPart.append(tableWithPredicate);
             }
         }
         queryPart.append(" WHERE ");
         if (contentModelTitle != null) {
             String contentModelTitleEscaped =
                 MPTStringUtil.escapeLiteralValueForSql(contentModelTitle);
             queryPart.append(contentModelTitleTableName);
             queryPart.append(".o=");
             queryPart.append("\'\"" + contentModelTitleEscaped + "\"\' AND ");
         }
         if (targetResourceType != null) {
             if ("member".equals(targetResourceType)) {
                 queryPart.append("(");
                 queryPart.append(tableWithObjectType);
                 queryPart.append(".o=");
                 queryPart.append("'<" + Constants.ITEM_OBJECT_TYPE + ">' OR ");
                 queryPart.append(tableWithObjectType);
                 queryPart.append(".o=");
                 queryPart.append("'<" + Constants.CONTAINER_OBJECT_TYPE
                     + ">') AND ");
             }
             else {
                 queryPart.append(tableWithObjectType);
                 queryPart.append(".o=");
                 queryPart.append("'<" + targetResourceType + ">' AND ");
             }
         }
         if (targetIsSubject) {
             queryPart.append(tableWithPredicate);
             // the query should return objid if a predicate is
             // TripleStoreUtility.Fedora_Creation_Date_Predicate,
             // therefore the dummy SQL-Query is using here:
             // select t.s from t where t.s='<info:fedora/escidoc:bla>';
             if (predicateId
                 .equals(TripleStoreUtility.Fedora_Creation_Date_Predicate)) {
                 queryPart.append(".s=");
                 queryPart.append("'<info:fedora/" + expectedValue + ">'");
             }
             else {
                 queryPart.append(".o=");
 
                 queryPart.append("'<info:fedora/" + expectedValue + ">'");
             }
 
         }
         else {
             queryPart.append(tableWithPredicate);
             queryPart.append(".s=");
             queryPart.append("'<info:fedora/" + expectedValue + ">'");
         }
         // if (!targetIsSubject
         // ||
         // predicateId.equals(TripleStoreUtility.Fedora_Creation_Date_Predicate))
         // {
         // queryPart.append(tableWithPredicate);
         // queryPart.append(".s=");
         // queryPart.append("'<info:fedora/" + expectedValue + ">'");
         // } else {
         // queryPart.append(tableWithPredicate);
         // queryPart.append(".o=");
         // queryPart.append("'<info:fedora/" + expectedValue + ">'");
         // }
 
         if (braceToAddAtBeginn == 1) {
             queryPart.insert(0, "(");
         }
 
         return queryPart;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param query
      * @return
      * @throws TripleStoreSystemException
      * @see de.escidoc.core.common.business.fedora.TripleStoreUtility
      *      #retrieve(java.lang.String)
      */
     @Override
     public List<String> retrieve(final String query)
         throws TripleStoreSystemException {
         return executeSqlQuery(query);
     }
 
     // CHECKSTYLE:JAVADOC-ON
 
     /**
      * Get the context id of the context with the given name.
      * 
      * @param name
      *            context name
      * 
      * @return context id or null, if no such context exists
      * @throws TripleStoreSystemException
      *             If access to the triple store fails.
      */
     @Override
     public String getContextForName(final String name)
         throws TripleStoreSystemException {
         String result = null;
         String titleTableName = getTableName(PROP_DC_TITLE);
         String typeTableName = getTableName(PROP_OBJECT_TYPE);
 
         if ((titleTableName != null) && (typeTableName != null)) {
             StringBuffer select = new StringBuffer();
 
             select.append("SELECT ");
             select.append(titleTableName);
             select.append(".s FROM ");
             select.append(titleTableName);
             select.append(",");
             select.append(typeTableName);
             select.append(" WHERE ");
             select.append(titleTableName);
             select.append(".s = ");
             select.append(typeTableName);
             select.append(".s AND ");
             select.append(typeTableName);
             select.append(".o = '<");
             select.append(Constants.CONTEXT_OBJECT_TYPE);
             select.append(">' AND ");
             select.append(titleTableName);
             select.append(".o = '\"");
             select.append(MPTStringUtil.escapeLiteralValueForSql(name));
             select.append("\"'");
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             List<String> res = executeSqlQuery(select.toString());
             if (getLogger().isDebugEnabled()) {
                 if (res != null) {
                     getLogger().debug("found " + res.size() + " records");
                     for (String item : res) {
                         getLogger().debug("item: " + item);
                         result = item;
                     }
                 }
                 else {
                     getLogger().debug("found no records");
                 }
             }
         }
         return result;
     }
 
     /**
      * Get all child containers of the given container.
      * 
      * @param id
      *            container id
      * @return id list of all child containers
      * @throws TripleStoreSystemException
      *             If access to the triple store fails.
      */
     @Override
     public List<String> getAllChildContainers(final String id)
         throws TripleStoreSystemException {
         List<String> result = null;
         String memberTableName = getTableName(PROP_MEMBER);
         String typeTableName = getTableName(PROP_OBJECT_TYPE);
 
         if ((memberTableName != null) && (typeTableName != null)) {
             String select =
                 MessageFormat.format(
                     "WITH RECURSIVE getChildContainers AS (SELECT {1}.s, {1}.o"
                         + " FROM {0}, {1} WHERE {0}.s={1}.o AND {0}.o=''<"
                         + Constants.CONTAINER_OBJECT_TYPE + ">'' AND {1}.s=''"
                         + "<info:fedora/" + id
                         + ">'' UNION SELECT {1}.s, {1}.o FROM {0}, {1}, "
                         + "getChildContainers WHERE {1}.s="
                         + "getChildContainers.o AND {0}.s={1}.o AND {0}.o=''<"
                         + Constants.CONTAINER_OBJECT_TYPE + ">'') SELECT o"
                         + " FROM getChildContainers;", typeTableName,
                     memberTableName);
 
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             result = executeSqlQuery(select);
             if (getLogger().isDebugEnabled()) {
                 if (result != null) {
                     getLogger().debug("found " + result.size() + " records");
                     getLogger().debug("records: " + result);
                 }
                 else {
                     getLogger().debug("found no records");
                 }
             }
         }
         return result;
     }
 
     /**
      * Get all child OUs of the given organizational unit.
      * 
      * @param id
      *            OU id
      * @return id list of all child OUs
      * @throws TripleStoreSystemException
      *             If access to the triple store fails.
      */
     @Override
     public List<String> getAllChildOUs(final String id)
         throws TripleStoreSystemException {
         List<String> result = null;
         String parentTableName = getTableName(PROP_PARENT);
 
         if (parentTableName != null) {
             String select =
                 MessageFormat.format(
                     "WITH RECURSIVE getChildOUs AS (SELECT {0}.s, {0}.o"
                        + " FROM {0} WHERE {0}.o=''<info:fedora/" + id
                        + ">'' UNION SELECT {0}.s, {0}.o FROM {0},"
                         + " getChildOUs WHERE {0}.o=getChildOUs.s)"
                         + " SELECT distinct(s) FROM getChildOUs;",
                     parentTableName);
 
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("Executing sql query '" + select + "'.");
             }
             result = executeSqlQuery(select);
             if (getLogger().isDebugEnabled()) {
                 if (result != null) {
                     getLogger().debug("found " + result.size() + " records");
                     getLogger().debug("records: " + result);
                 }
                 else {
                     getLogger().debug("found no records");
                 }
             }
         }
         return result;
     }
 }
