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
  * Copyright 2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.common.business.fedora.resources;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URL;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.springframework.jdbc.core.ResultSetExtractor;
 import org.springframework.jdbc.core.support.JdbcDaoSupport;
 import org.springframework.jmx.export.annotation.ManagedAttribute;
 import org.springframework.jmx.export.annotation.ManagedOperation;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import de.escidoc.core.aa.service.interfaces.UserGroupHandlerInterface;
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.resources.interfaces.FilterInterface;
 import de.escidoc.core.common.business.fedora.resources.interfaces.FilterInterface.OrderBy;
 import de.escidoc.core.common.business.fedora.resources.interfaces.ResourceCacheInterface;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.service.BeanLocator;
 import de.escidoc.core.common.util.service.UserContext;
 import de.escidoc.core.common.util.xml.XmlUtility;
 
 /**
  * Abstract super class for all resource caches (item, container, context,
  * organizational unit).
  * 
  * @author Andr&eacute; Schenk
  */
 public abstract class DbResourceCache extends JdbcDaoSupport
     implements ResourceCacheInterface {
 
     /**
      * SQL statements.
      */
     private static final String DELETE_ALL_PROPERTIES =
         "DELETE FROM list.property WHERE resource_id IN (SELECT id FROM list.{0})";
 
     private static final String DELETE_ALL_RESOURCES = "DELETE FROM list.{0}";
 
     private static final String DELETE_PROPERTIES =
         "DELETE FROM list.property WHERE resource_id = ?";
 
     private static final String INSERT_PROPERTY =
         "INSERT INTO list.property (resource_id, local_path, value, position) "
             + "VALUES (?, ?, ?, ?)";
 
     private static final String GET_PROPERTIES =
         "SELECT DISTINCT local_path FROM"
             + " (SELECT l1.local_path FROM list.property l1, list.property l2"
             + "  WHERE l1.resource_id=l2.resource_id AND l2.local_path=''type''"
             + "  AND l2.value=''{0}'' GROUP BY l1.local_path) AS paths";
 
     private static final String RESOURCE_EXISTS =
         "SELECT id FROM list.{0} WHERE id = ?";
 
     private static final Map<String, String> CACHE_TYPES =
         new HashMap<String, String>();
 
     /**
      * Mapping from cache instance to name space URI of the resource
      */
     static {
         CACHE_TYPES.put("DbContainerCache", Constants.CONTAINER_NAMESPACE_URI);
         CACHE_TYPES.put("DbContentModelCache",
             Constants.CONTENT_MODEL_NAMESPACE_URI);
         CACHE_TYPES.put("DbContentRelationCache",
             Constants.CONTENT_RELATION_NAMESPACE_URI);
         CACHE_TYPES.put("DbContextCache", Constants.CONTEXT_NAMESPACE_URI);
         CACHE_TYPES.put("DbItemCache", Constants.ITEM_NAMESPACE_URI);
         CACHE_TYPES.put("DbOrganizationalUnitCache",
             Constants.ORGANIZATIONAL_UNIT_NAMESPACE_URI);
     }
 
     /**
      * Logging goes there.
      */
     private static AppLogger logger = new AppLogger(
         DbResourceCache.class.getName());
 
     /**
      * SQL date formats.
      */
     private final SimpleDateFormat dateFormat1 = new SimpleDateFormat(
         "yyyy-MM-dd'T'HH:mm:ss.S'Z'");
 
     private final SimpleDateFormat dateFormat2 = new SimpleDateFormat(
         "yyyy-MM-dd'T'HH:mm:ss'Z'");
 
     /**
      * Enable / disable the resource cache.
      */
     private boolean enabled = true;
 
     protected ResourceType resourceType = null;
 
     private String selfUrl = null;
 
     /**
      * The value of list.property is truncated to this length to prevent
      * "index value too long" errors.
      */
     private final int databaseIndexPrefixLength;
 
     private final TransformerFactory tf = TransformerFactory.newInstance();
 
     /**
      * Create a new resource cache object.
      * 
      * @throws IOException
      *             Thrown if reading the configuration failed.
      */
     public DbResourceCache() throws IOException {
         databaseIndexPrefixLength =
             (int) EscidocConfiguration
                 .getInstance()
                 .getAsLong(
                     EscidocConfiguration.ESCIDOC_CORE_DATASOURCE_INDEX_PREFIX_LENGTH);
 
         String[] className = getClass().toString().split(" ");
         if ((className != null) && (className.length == 2)) {
             String enabled =
                 EscidocConfiguration.getInstance().get(
                     className[1] + ".enabled");
 
             if (enabled != null) {
                 this.enabled = Boolean.valueOf(enabled);
             }
         }
         setSelfUrl(EscidocConfiguration.getInstance().get(
             EscidocConfiguration.ESCIDOC_CORE_SELFURL));
     }
 
     // begin implementation of ResourceCacheInterface
 
     /**
      * Store a resource in the database cache.
      * 
      * @param id
      *            resource id
      * @param restXml
      *            complete resource as REST XML
      * @param soapXml
      *            complete resource as SOAP XML
      * 
      * @throws SystemException
      *             The resource could not be stored.
      */
     public void add(final String id, final String restXml, final String soapXml)
         throws SystemException {
         resourceCreated(id, restXml, soapXml);
     }
 
     /**
      * Delete all resources of the current type and their properties from the
      * database.
      */
     public void clear() {
         getJdbcTemplate().update(
             MessageFormat.format(DELETE_ALL_PROPERTIES, resourceType
                 .name().toLowerCase()));
         getJdbcTemplate().update(
             MessageFormat.format(DELETE_ALL_RESOURCES, resourceType
                 .name().toLowerCase()));
     }
 
     /**
      * Check if the resource exists in the database cache.
      * 
      * @param id
      *            resource id
      * 
      * @return true if the resource exists
      */
     public boolean exists(final String id) {
         Object queryResult =
             getJdbcTemplate().query(
                 MessageFormat.format(RESOURCE_EXISTS, resourceType
                     .name().toLowerCase()), new Object[] { id },
                 new ResultSetExtractor() {
                     public Object extractData(final ResultSet rs)
                         throws SQLException {
                         Object result = null;
 
                         if (rs.next()) {
                             result = rs.getObject(1);
                         }
                         return result;
                     }
                 });
 
         return queryResult != null;
     }
 
     /**
      * Get a list of resource id's depending on the given parameters "user" and
      * "filter".
      * 
      * @param userId
      *            user id
      * @param filter
      *            object containing all filter values
      * 
      * @return list of resource id's
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      */
     public List<String> getIds(final String userId, final FilterInterface filter)
         throws InvalidSearchQueryException, SystemException {
         List<String> result = new LinkedList<String>();
         BufferedReader reader = null;
 
         try {
             StringWriter writer = new StringWriter();
 
             getResourceIds(writer, userId, filter);
             reader = new BufferedReader(new StringReader(writer.toString()));
 
             String line = null;
 
             while ((line = reader.readLine()) != null) {
                 result.add(line);
             }
         }
         catch (IOException e) {
             throw new SystemException(e);
         }
         finally {
             if (reader != null) {
                 try {
                     reader.close();
                 }
                 catch (IOException e) {
                 }
             }
         }
         return result;
     }
 
     /**
      * Get the number of records for that query.
      * 
      * @param userId
      *            user id
      * @param filter
      *            object containing all the necessary parameters
      * 
      * @return number of resources for that query
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      */
     public long getNumberOfRecords(
         final String userId, final FilterInterface filter)
         throws InvalidSearchQueryException, SystemException {
         long result = 0;
         String statement = getSql("COUNT(*)", userId, filter);
 
         logger.info("filter: " + filter);
         logger.info("count resources from: " + statement);
 
         if (statement.length() > 0) {
             Long queryResult =
                 (Long) getJdbcTemplate().query(statement,
                     new ResultSetExtractor() {
                         public Object extractData(final ResultSet rs)
                             throws SQLException {
                             Object result = null;
 
                             if (rs.next()) {
                                 result = rs.getObject(1);
                             }
                             return result;
                         }
                     });
 
             if (queryResult != null) {
                 result = queryResult.longValue();
             }
         }
         return result;
     }
 
     /**
      * Get all property names that are currently stored in the database for the
      * current resource type.
      * 
      * @return all property names for the current resource type
      */
     public Set<String> getPropertyNames() {
         final Set<String> result = new TreeSet<String>();
 
         getJdbcTemplate().query(
             MessageFormat.format(GET_PROPERTIES, resourceType
                 .name().toLowerCase()), new ResultSetExtractor() {
                 public Object extractData(final ResultSet rs)
                     throws SQLException {
                     while (rs.next()) {
                         result.add(rs.getString(1));
                     }
                     return null;
                 }
             });
         return result;
     }
 
     /**
      * Get a list of resource ids and write it to the given writer.
      * 
      * @param output
      *            writer to which the resource id list will be written
      * @param userId
      *            user id
      * @param filter
      *            object containing all the necessary parameters
      * 
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     public void getResourceIds(
         final Writer output, final String userId, final FilterInterface filter)
         throws InvalidSearchQueryException, WebserverSystemException {
         getResourceList(output, "id", userId, filter, null);
     }
 
     /**
      * Get a list of resources and write it to the given writer.
      * 
      * @param output
      *            writer to which the resource list will be written
      * @param userId
      *            user id
      * @param filter
      *            object containing all the necessary parameters
      * @param format
      *            output format (may by null for the old behavior)
      * 
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      * @throws SystemException
      *             Thrown if a framework internal error occurs.
      */
     public void getResourceList(
         final Writer output, final String userId, final FilterInterface filter,
         final String format) throws InvalidSearchQueryException,
         SystemException {
         getResourceList(output, (UserContext.isRestAccess() ? "rest" : "soap")
             + "_content", userId, filter, format);
     }
 
     /**
      * Ask whether or not the resource cache is enabled.
      * 
      * @return true if the resource cache is currently enabled
      */
     @ManagedAttribute(description = "Ask whether or not the resource cache is enabled.")
     public boolean isEnabled() {
         return enabled;
     }
 
     /**
      * Remove a resource from the database cache.
      * 
      * @param id
      *            resource id .
      * @throws SystemException
      *             The resource could not be removed
      */
     public void remove(final String id) throws SystemException {
         resourceDeleted(id);
     }
 
     // end implementation of ResourceCacheInterface
 
     // begin implementation of ResourceListener
 
     /**
      * Store a resource in the database cache.
      * 
      * @param id
      *            resource id
      * @param restXml
      *            complete resource as REST XML
      * @param soapXml
      *            complete resource as SOAP XML
      * 
      * @throws SystemException
      *             The resource could not be stored.
      */
     public synchronized void resourceCreated(
         final String id, final String restXml, final String soapXml)
         throws SystemException {
         if (enabled) {
             storeProperties(getProperties(id, restXml));
             storeResource(id, restXml, soapXml);
         }
     }
 
     /**
      * Delete a resource from the database cache.
      * 
      * @param id
      *            resource id
      * 
      * @throws SystemException
      *             The resource could not be deleted.
      */
     public synchronized void resourceDeleted(final String id)
         throws SystemException {
         if (enabled) {
             deleteProperties(id);
             deleteResource(id);
         }
     }
 
     /**
      * Replace a resource in the database cache.
      * 
      * @param id
      *            resource id
      * @param restXml
      *            complete resource as REST XML
      * @param soapXml
      *            complete resource as SOAP XML
      * 
      * @throws SystemException
      *             The resource could not be deleted and newly created.
      */
     public synchronized void resourceModified(
         final String id, final String restXml, final String soapXml)
         throws SystemException {
         resourceDeleted(id);
         resourceCreated(id, restXml, soapXml);
     }
 
     // end implementation of ResourceListener
 
     /**
      * Add the AA filters to the given SQL statement.
      * 
      * @param resourceType
      *            resource type
      * @param statement
      *            SQL statement
      * @param userId
      *            user id
      * @param groupIds
      *            list of all group id's the user belongs to
      * @param userGrants
      *            list of all user grants the user belongs to
      * @param userGroupGrants
      *            list of all user group grants the user belongs to
      * @param hierarchicalContainers
      *            list of all containers the user may access
      * 
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     private void addAccessRights(
         final ResourceType resourceType, final StringBuffer statement,
         final String userId, final Set<String> groupIds,
         final Set<String> userGrants, final Set<String> userGroupGrants,
         final Set<String> hierarchicalContainers)
         throws WebserverSystemException {
         AccessRights accessRights = getAccessRights();
         List<String> statements = new LinkedList<String>();
 
         for (String roleId : accessRights.getRoleIds(resourceType)) {
             final String rights =
                 accessRights.getAccessRights(resourceType, roleId, userId,
                     groupIds, userGrants, userGroupGrants,
                     hierarchicalContainers);
 
             if ((rights != null) && (rights.length() > 0)) {
                 logger.info("OR access rights for (" + userId + "," + roleId
                     + "): " + rights);
                 statements.add(rights);
             }
         }
 
         // all matching access rights for the login user are ORed
         statement.append('(');
         for (int index = 0; index < statements.size(); index++) {
             if (index > 0) {
                 statement.append(" OR ");
             }
             statement.append('(');
             statement.append(statements.get(index));
             statement.append(')');
         }
         statement.append(')');
     }
 
     /**
      * Add LIMIT and OFFSET to the given SQL statement.
      * 
      * @param statement
      *            the SQL statement
      * @param filter
      *            object containing all the necessary parameters
      */
     private void addLimitAndOffset(
         final StringBuffer statement, final FilterInterface filter) {
         if (statement != null) {
             if (filter.getLimit() > 0) {
                 statement.append(" LIMIT ");
                 statement.append(filter.getLimit());
             }
             if (filter.getOffset() > 0) {
                 statement.append(" OFFSET ");
                 statement.append(filter.getOffset());
             }
         }
     }
 
     /**
      * Add ORDER BY to the given SQL statement.
      * 
      * @param statement
      *            the SQL statement
      * @param filter
      *            object containing all the necessary parameters
      * @param type
      *            may be count(*), "id" for resource ids or "content" for full
      *            resources
      */
     private void addOrderBy(
         final StringBuffer statement, final FilterInterface filter,
         final String type) {
         if (statement != null) {
             if (filter.getOrderBy().size() > 0) {
                 StringBuffer s = new StringBuffer();
 
                 s.append("SELECT ");
                 s.append(type);
 
                 int index = 1;
 
                 for (OrderBy orderBy : filter.getOrderBy()) {
                     // add ORDER BY column
                     s.append(",(ARRAY(SELECT value FROM list.property WHERE ");
                     s.append("resource_id=r.id AND local_path='");
                     s.append(orderBy.attribute);
                     s.append("'ORDER BY position)) AS order_by");
                     s.append(index++);
                 }
                 s.append(' ');
 
                 // delete "SELECT "
                 statement.delete(0, "SELECT ".length());
                 // delete type
                 statement.delete(0, type.length() + 1);
 
                 statement.insert(0, s);
 
                 // add ORDER BY statement
                 statement.append(" ORDER BY ");
                 index = 1;
                 for (OrderBy orderBy : filter.getOrderBy()) {
                     if (index > 1) {
                         statement.append(',');
                     }
 
                     // add order attribute
                     statement.append("order_by");
                     statement.append(index++);
 
                     // add order direction
                     statement.append(' ');
                     statement.append(orderBy.direction);
                 }
             }
         }
     }
 
     /**
      * Delete all properties and meta data of a given resource.
      * 
      * @param id
      *            resource id
      */
     private void deleteProperties(final String id) {
         getJdbcTemplate().update(DELETE_PROPERTIES, new Object[] { id });
     }
 
     /**
      * Delete a resource from the database cache.
      * 
      * @param id
      *            resource id
      */
     protected abstract void deleteResource(final String id);
 
     /**
      * Disable the resource cache.
      */
     @ManagedOperation(description = "Disable the resource cache.")
     public void disableCache() {
         enabled = false;
     }
 
     /**
      * Enable the resource cache.
      */
     @ManagedOperation(description = "Enable the resource cache.")
     public void enableCache() {
         enabled = true;
     }
 
     /**
      * Get the AccessRights Spring bean.
      * 
      * @return AccessRights bean
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     private AccessRights getAccessRights() throws WebserverSystemException {
         return (AccessRights) BeanLocator.getBean(BeanLocator.AA_FACTORY_ID,
             "resource.DbAccessRights");
     }
 
     /**
      * Get the part of the query which represents the access restrictions.
      * 
      * @param resourceTypes
      *            list of resource types which are allowed for this request
      * @param userId
      *            user id
      * @param filter
      *            object containing all the necessary parameters
      * 
     * @return subquery representing the access restrictions
      * @throws InvalidSearchQueryException
      *             Thrown if the given search query could not be translated into
      *             a SQL query.
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     public String getFilterQuery(
         final Set<ResourceType> resourceTypes, final String userId,
         final FilterInterface filter) throws InvalidSearchQueryException,
         WebserverSystemException {
         StringBuffer result = new StringBuffer();
 
         if ((filter.getObjectType() == null)
             || (resourceTypes.contains(filter.getObjectType()))) {
             for (ResourceType resourceType : resourceTypes) {
                 if (result.length() > 0) {
                     result.append(" OR ");
                 }
                 result.append('(');
                 // add AA filters
                 Set<String> userGrants = getUserGrants(userId);
                 Set<String> userGroupGrants = getUserGroupGrants(userId);
 
                 addAccessRights(resourceType, result, userId,
                     retrieveGroupsForUser(userId), userGrants, userGroupGrants,
                     getHierarchicalContainers(userGrants, userGroupGrants));
                 logger.info("AA filters: " + result);
 
                 // all restricting access rights from another user are ANDed
                 if (filter.getUserId() != null) {
                     userGrants = getUserGrants(filter.getUserId());
                     userGroupGrants = getUserGroupGrants(filter.getUserId());
 
                     String rights =
                         getAccessRights().getAccessRights(
                             resourceType,
                             filter.getRoleId(),
                             filter.getUserId(),
                             retrieveGroupsForUser(filter.getUserId()),
                             userGrants,
                             userGroupGrants,
                             getHierarchicalContainers(userGrants,
                                 userGroupGrants));
 
                     if ((rights != null) && (rights.length() > 0)) {
                         logger.info("AND restricting access rights from "
                             + "another user (1): " + rights);
                         result.append(" AND ");
                         result.append(rights);
                     }
                 }
                 result.append(')');
             }
         }
         else {
             result.append("FALSE");
         }
         return result.toString();
     }
 
     /**
      * This method is empty and will be overridden in a subclass.
      * 
      * @param userGrants
      *            list of all user grants the user belongs to
      * @param userGroupGrants
      *            list of all user group grants the user belongs to
      * 
      * @return nothing here
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     protected Set<String> getHierarchicalContainers(
         final Set<String> userGrants, final Set<String> userGroupGrants)
         throws WebserverSystemException {
         return null;
     }
 
     /**
      * Get the namespace URI for the current cache object.
      * 
      * @return namespace URI for the current cache object
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     private String getNamespaceUri() throws WebserverSystemException {
         String result = null;
         String className = getClass().getName();
         String[] packages = className.split("\\.");
 
         result = CACHE_TYPES.get(packages[packages.length - 1]);
         if (result == null) {
             result = "";
         }
         return result;
     }
 
     /**
      * Get all properties for a given resource from the item XML.
      * 
      * @param id
      *            resource id
      * @param xml
      *            resource XML
      * 
      * @return property map for this resource
      * @throws SystemException
      *             The XML could not be parsed.
      */
     protected List<Property> getProperties(final String id, final String xml)
         throws SystemException {
         List<Property> result = null;
 
         try {
             // XSL transformation to reduce the amount of properties to be
             // stored
             String xsltUrl =
                 selfUrl + "/xsl/filtering/" + resourceType.getLabel() + ".xsl";
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             Transformer t =
                 tf.newTransformer(new StreamSource(new URL(xsltUrl)
                     .openStream()));
 
             t.transform(new StreamSource(new StringReader(xml)),
                 new StreamResult(out));
 
             // parse XML
             SAXParserFactory spf = SAXParserFactory.newInstance();
 
             spf.setFeature("http://xml.org/sax/features/namespaces", true);
 
             SAXParser parser = spf.newSAXParser();
             FilterHandler handler = new FilterHandler(id);
 
             parser.parse(
                 new ByteArrayInputStream(out.toString(
                     XmlUtility.CHARACTER_ENCODING).getBytes(
                     XmlUtility.CHARACTER_ENCODING)), handler);
             result = handler.getProperties();
         }
         catch (Exception e) {
             logger.error("XSL transformation failed", e);
             throw new SystemException(e.getMessage());
         }
         return result;
     }
 
     /**
      * Get the resource with the given id and write it to the given writer.
      * 
      * @param output
      *            writer to which the resource id list will be written
      * @param id
      *            resource id
      * @param userId
      *            user id
      * 
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     public void getResource(
         final Writer output, final String id, final String userId)
         throws WebserverSystemException {
 
         getResource(output, (UserContext.isRestAccess() ? "rest" : "soap")
             + "_content", id, userId, retrieveGroupsForUser(userId), null,
             null, null);
     }
 
     /**
      * Get the resource with the given id and write it to the given writer.
      * 
      * @param output
      *            writer to which the resource id list will be written
      * @param type
      *            may be "rest_content" or "soap_content"
      * @param id
      *            resource id
      * @param userId
      *            user id
      * @param groupIds
      *            list of all user groups the user belongs to
      * @param userGrants
      *            list of all user grants the user belongs to
      * @param userGroupGrants
      *            list of all user group grants the user belongs to
      * @param hierarchicalContainers
      *            list of all containers the user may access
      * 
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     protected void getResource(
         final Writer output, final String type, final String id,
         final String userId, final Set<String> groupIds,
         final Set<String> userGrants, final Set<String> userGroupGrants,
         final Set<String> hierarchicalContainers)
         throws WebserverSystemException {
         StringBuffer statement = new StringBuffer();
 
         // add AA filters
         addAccessRights(resourceType, statement, userId, groupIds, userGrants,
             userGroupGrants, hierarchicalContainers);
 
         statement.insert(0, "SELECT r." + type + " FROM list."
             + resourceType.name().toLowerCase() + " WHERE id = '" + id + "'");
         logger.info("create resource from: " + statement);
         getJdbcTemplate().query(statement.toString(), new ResultSetExtractor() {
             public Object extractData(final ResultSet rs) throws SQLException {
                 try {
                     if (rs.next()) {
                         output.write(rs.getString(1));
                         output.write("\n");
                     }
                 }
                 catch (IOException e) {
                     throw new SQLException(e.getMessage());
                 }
                 return null;
             }
         });
     }
 
     /**
      * Get a list of resources and write it to the given writer.
      * 
      * @param output
      *            writer to which the resource list will be written
      * @param type
      *            may be "id" for resource ids or "content" for full resources
      * @param userId
      *            user id
      * @param filter
      *            object containing all the necessary parameters
      * @param format
      *            output format (may by null for the old behavior)
      * 
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     protected void getResourceList(
         final Writer output, final String type, final String userId,
         final FilterInterface filter, final String format)
         throws InvalidSearchQueryException, WebserverSystemException {
         String statement = getSql("r." + type, userId, filter);
 
         logger.info("filter: " + filter);
         logger.info("create list from: " + statement);
 
         if (statement.length() > 0) {
             getJdbcTemplate().query(statement, new ResultSetExtractor() {
                 public Object extractData(final ResultSet rs)
                     throws SQLException {
                     try {
                         boolean srwResponse =
                             (format != null)
                                 && (format.equalsIgnoreCase("srw"));
                         int recordPosition = 0;
 
                         while (rs.next()) {
                             if (srwResponse) {
                                 output.write("<zs:record>");
                                 output.write("<zs:recordSchema>");
                                 output.write(getNamespaceUri());
                                 output.write("</zs:recordSchema>");
                                 output.write("<zs:recordPacking>");
                                 output.write("xml");
                                 output.write("</zs:recordPacking>");
                                 output.write("<zs:recordData>");
                             }
                             output.write(rs.getString(1));
                             output.write("\n");
                             if (srwResponse) {
                                 output.write("</zs:recordData>");
                                 output.write("<zs:recordPosition>"
                                     + (++recordPosition)
                                     + "</zs:recordPosition>");
                                 output.write("</zs:record>");
                             }
                         }
                     }
                     catch (Exception e) {
                         logger.error("getting records from database failed", e);
                         throw new SQLException(e.getMessage());
                     }
                     return null;
                 }
             });
         }
     }
 
     /**
      * Create the SQL statement from the given filter and additional parameters.
      * 
      * @param type
      *            may be count(*), "id" for resource ids or "content" for full
      *            resources
      * @param userId
      *            user id
      * @param filter
      *            object containing all the necessary parameters
      * 
      * @return complete SQL statement
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     private String getSql(
         final String type, final String userId, final FilterInterface filter)
         throws InvalidSearchQueryException, WebserverSystemException {
         StringBuffer result =
             new StringBuffer(getFilterQuery(new HashSet<ResourceType>() {
                 private static final long serialVersionUID =
                     8846475141073243876L;
 
                 {
                     try {
                         add(resourceType);
                     }
                     catch (Exception e) {
                         logger.error("", e);
                     }
                 }
             }, userId, filter));
        final String filterQuery = filter.toSqlString();
 
         // (access rights) AND (filter criteria)
         logger.info("AND filter criteria: " + filterQuery);
         result.insert(0, '(');
         result.append(") AND ");
         if ((filterQuery != null) && (filterQuery.length() > 0)) {
             result.append("(" + filterQuery + ")");
         }
         else {
             result.append("TRUE");
         }
 
         result.insert(0, "SELECT " + type + " FROM list."
             + resourceType.name().toLowerCase() + " r WHERE ");
 
         if (!type.toUpperCase().startsWith("COUNT")) {
             // add sorting
             addOrderBy(result, filter, type);
 
             // add paging
             addLimitAndOffset(result, filter);
         }
         return result.toString();
     }
 
     /**
      * Create a time stamp from the given string. Two different date formats are
      * supported.
      * 
      * @param property
      *            string containing a date
      * 
      * @return the time stamp object
      * @throws ParseException
      *             The given string cannot be parsed.
      */
     protected Timestamp getTimestamp(final String property)
         throws ParseException {
         Timestamp result = null;
 
         if (property != null) {
             java.util.Date date = null;
 
             try {
                 synchronized (dateFormat1) {
                     date = dateFormat1.parse(property);
                 }
             }
             catch (ParseException e) {
                 synchronized (dateFormat2) {
                     date = dateFormat2.parse(property);
                 }
             }
             result = new Timestamp(date.getTime());
         }
         return result;
     }
 
     /**
      * Get the TripleStoreUtility Spring bean.
      * 
      * @return TripleStoreUtility Spring bean
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     protected TripleStoreUtility getTripleStoreUtility()
         throws WebserverSystemException {
         return (TripleStoreUtility) BeanLocator.getBean(
             BeanLocator.COMMON_FACTORY_ID, "business.TripleStoreUtility");
     }
 
     /**
      * This method is empty and will be overridden in a subclass.
      * 
      * @param userId
      *            user id
      * 
      * @return nothing here
      */
     protected Set<String> getUserGrants(final String userId) {
         return null;
     }
 
     /**
      * This method is empty and will be overridden in a subclass.
      * 
      * @param userId
      *            user id
      * 
      * @return nothing here
      */
     protected Set<String> getUserGroupGrants(final String userId) {
         return null;
     }
 
     /**
      * Get the UserGroupHandler Spring bean.
      * 
      * @return UserGroupHandler Spring bean
      * @throws WebserverSystemException
      *             Thrown if a framework internal error occurs.
      */
     private UserGroupHandlerInterface getUserGroupHandler()
         throws WebserverSystemException {
         return (UserGroupHandlerInterface) BeanLocator.getBean(
             BeanLocator.AA_FACTORY_ID, "service.UserGroupHandler");
     }
 
     /**
      * wrapper for method from UserGroupHandler which returns an empty set in
      * case of an error.
      * 
      * @param userId
      *            user id
      * @return set of user groups or empty set
      */
     protected Set<String> retrieveGroupsForUser(final String userId) {
         Set<String> result = new HashSet<String>();
 
         if ((userId != null) && (userId.length() > 0)) {
             try {
                 result = getUserGroupHandler().retrieveGroupsForUser(userId);
             }
             catch (Exception e) {
                 logger.error("", e);
             }
         }
         return result;
     }
 
     /**
      * Set the URL to the eSciDocCore framework.
      * 
      * This method is necessary because this class will also be used in the
      * eSciDocCoreAdmin project and there is no EscidocConfiguration available.
      * 
      * @param selfUrl
      *            URL to the eSciDocCore framework
      */
     protected void setSelfUrl(final String selfUrl) {
         if ((selfUrl != null) && (selfUrl.endsWith("/"))) {
             this.selfUrl = selfUrl.substring(0, selfUrl.length() - 1);
         }
         else {
             this.selfUrl = selfUrl;
         }
     }
 
     /**
      * Store the resource properties and meta data in a separate database table.
      * 
      * @param properties
      *            resource properties and meta data
      */
     protected void storeProperties(final List<Property> properties) {
         for (Property property : properties) {
             getJdbcTemplate().update(
                 INSERT_PROPERTY,
                 new Object[] {
                     property.resourceId,
                     property.localPath,
                     property.value.substring(
                         0,
                         Math.min(property.value.length(),
                             databaseIndexPrefixLength)).toLowerCase(),
                     property.position });
         }
     }
 
     /**
      * Store the resource in the database cache.
      * 
      * @param id
      *            resource id
      * @param restXml
      *            complete resource as REST XML
      * @param soapXml
      *            complete resource as SOAP XML
      * 
      * @throws SystemException
      *             A date string cannot be parsed.
      */
     protected abstract void storeResource(
         final String id, final String restXml, final String soapXml)
         throws SystemException;
 
     /**
      * SAX event handler to get all filter criteria from an item.
      * 
      * @author SCHE
      */
     private class FilterHandler extends DefaultHandler {
         private final String resourceId;
 
         private StringBuffer characters = new StringBuffer();
 
         private List<String> localPath = new LinkedList<String>();
 
         private List<Property> properties = new LinkedList<Property>();
 
         private Map<String, Property> latestProperties =
             new HashMap<String, Property>();
 
         /**
          * Create a new FilterHandler.
          * 
          * @param resourceId
          *            resource id
          */
         public FilterHandler(final String resourceId) {
             this.resourceId = resourceId;
             properties.add(new Property(resourceId, "type", resourceType
                 .getLabel(), 0));
         }
 
         /**
          * Receive notification of character data inside an element.
          * 
          * @param ch
          *            The characters.
          * @param start
          *            The start position in the character array.
          * @param length
          *            The number of characters to use from the character array.
          * 
          * @see org.xml.sax.helpers.DefaultHandler#characters(char [], int, int)
          */
         public void characters(
             final char[] ch, final int start, final int length) {
             characters.append(new String(ch, start, length).trim());
         }
 
         /**
          * Receive notification of the end of an element.
          * 
          * @param uri
          *            The Namespace URI, or the empty string if the element has
          *            no Namespace URI or if Namespace processing is not being
          *            performed.
          * @param localName
          *            The local name (without prefix), or the empty string if
          *            Namespace processing is not being performed.
          * @param qName
          *            The qualified name (with prefix), or the empty string if
          *            qualified names are not available.
          * 
          * @see org.xml.sax.helpers.DefaultHandler#endElement (String, String,
          *      String)
          */
         public void endElement(
             final String uri, final String localName, final String qName) {
             String localPath = getLocalPath();
 
             this.localPath.remove(this.localPath.size() - 1);
 
             String parentPath = getLocalPath();
 
             if (characters.length() > 0) {
                 // store element
                 int position = latestProperties.get(parentPath).position;
                 Property property =
                     new Property(resourceId, localPath, characters.toString(),
                         position);
 
                 properties.add(property);
                 characters.setLength(0);
             }
         }
 
         /**
          * Get the path to the extracted data.
          * 
          * @return path to the extracted data
          */
         private String getLocalPath() {
             StringBuffer result = new StringBuffer();
 
             for (int index = 1; index < localPath.size(); index++) {
                 result.append('/');
                 result.append(localPath.get(index));
             }
             return result.toString();
         }
 
         /**
          * Get a list of all properties which were extracted from the XML
          * stream.
          * 
          * @return list of all properties
          */
         public List<Property> getProperties() {
             return properties;
         }
 
         /**
          * Receive notification of ignorable whitespace in element content.
          * 
          * @param ch
          *            The whitespace characters.
          * @param start
          *            The start position in the character array.
          * @param length
          *            The number of characters to use from the character array.
          * 
          * @throws SAXException
          *             Any SAX exception, possibly wrapping another exception.
          * 
          * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace (char [],
          *      int, int)
          */
         public void ignorableWhitespace(
             final char[] ch, final int start, final int length)
             throws SAXException {
         }
 
         /**
          * Receive notification of the start of an element.
          * 
          * @param uri
          *            The Namespace URI, or the empty string if the element has
          *            no Namespace URI or if Namespace processing is not being
          *            performed.
          * @param localName
          *            The local name (without prefix), or the empty string if
          *            Namespace processing is not being performed.
          * @param qName
          *            The qualified name (with prefix), or the empty string if
          *            qualified names are not available.
          * @param attributes
          *            The attributes attached to the element. If there are no
          *            attributes, it shall be an empty Attributes object.
          * 
          * @see org.xml.sax.helpers.DefaultHandler#startElement (String, String,
          *      String, Attributes)
          */
         public void startElement(
             final String uri, final String localName, final String qName,
             final Attributes attributes) {
             localPath.add(localName);
 
             // update latest properties to get the correct "position"
             String localPath = getLocalPath();
             Property latestProperty = latestProperties.get(localPath);
             int position = 1;
 
             if (latestProperty != null) {
                 position = latestProperty.position + 1;
             }
             latestProperties.remove(localPath);
             latestProperties.put(localPath, new Property(resourceId, localPath,
                 "", position));
 
             if (attributes != null) {
                 for (int index = 0; index < attributes.getLength(); index++) {
                     final String attrLocalName = attributes.getLocalName(index);
 
                     if ((attrLocalName != null) && (attrLocalName.length() > 0)) {
                         String attrValue = attributes.getValue(index);
                         final String attrQName = attributes.getQName(index);
 
                         if (attrLocalName.equalsIgnoreCase("href")) {
                             // store resource id
                             int i = attrValue.lastIndexOf('/');
 
                             if ((i > 0) && (i < attrValue.length() - 1)) {
                                 attrValue = attrValue.substring(i + 1);
                                 // check for resource id
                                 if (attrValue.indexOf(':') > 0) {
                                     properties
                                         .add(new Property(resourceId, localPath
                                             + "/id", attrValue, position));
                                 }
                             }
                         }
                         else if ((attrQName == null)
                             || (!attrQName.startsWith("xlink"))) {
                             // store attribute
                             properties.add(new Property(resourceId, localPath
                                 + "/" + attrLocalName, attrValue, position));
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Container for a property with its resource id and a key/value pair.
      * 
      * @author SCHE
      */
     protected class Property {
         public final String resourceId;
 
         public final String localPath;
 
         public final String value;
 
         public final int position;
 
         /**
          * Create a new Property object.
          * 
          * @param resourceId
          *            resource id
          * @param localPath
          *            path to the property
          * @param value
          *            property value
          * @param position
          *            position of the property value
          */
         protected Property(final String resourceId, final String localPath,
             final String value, final int position) {
             this.resourceId = resourceId;
             this.localPath = localPath;
             this.value = value;
             this.position = position;
         }
     }
 }
