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
  * Copyright 2007-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.common.business.fedora;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.TripleStoreConnector;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidTripleStoreOutputFormatException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidTripleStoreQueryException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import de.escidoc.core.common.util.list.ListSorting;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.service.UserContext;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import org.nsdl.mptstore.util.NTriplesUtil;
 
 import javax.sql.DataSource;
 import javax.xml.stream.FactoryConfigurationError;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
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
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * To use is as implementation of the abstract class TripleStoreUtility register
  * this as spring.bean id="business.TripleStoreUtility".
  * 
  * spring.bean id="business.TripleStoreUtility"
  * 
  * @author FRS
  * 
  * @common
  * 
  */
 public class SpoItqlTripleStoreUtility extends TripleStoreUtility {
 
     private static final Pattern PATTERN_WHITESPACE_DOT =
         Pattern.compile("\\s\\.");
 
     private static final Pattern PATTERN_WHITESPACES = Pattern.compile("\\s+");
 
     private static final String SELECT_VAR = "$s";
 
     /**
      * Constructor.
      * 
      * @throws TripleStoreSystemException
      *             A configuration parameter could not be read.
      */
     public SpoItqlTripleStoreUtility() throws TripleStoreSystemException {
 
         final String fedoraUrl;
         try {
             fedoraUrl =
                 EscidocConfiguration.getInstance().get(
                     EscidocConfiguration.FEDORA_URL);
         }
         catch (final Exception e) {
             final String errorMsg =
                 "Failed to retrieve configuration parameter "
                     + EscidocConfiguration.FEDORA_URL;
             log.error(errorMsg, e);
             throw new TripleStoreSystemException(e);
         }
         fedoraRdfXmlUrl = fedoraUrl + HTTP_QUERY_BASE_ITQL_RDF_XML;
         fedoraItqlNtriplesUrl = fedoraUrl + HTTP_QUERY_BASE_ITQL_NTRIPLES;
         fedoraItqlCsvUrl = fedoraUrl + HTTP_QUERY_BASE_ITQL_CSV;
         fedoraSpoNtriplesUrl = fedoraUrl + HTTP_QUERY_BASE_SPO_NTRIPLES;
 
         // Initialize select clause
         final StringBuffer retrieveSelectClauseBuf =
             new StringBuffer("select ");
         retrieveSelectClauseBuf.append(SELECT_VAR);
         retrieveSelectClauseBuf.append(" from <#ri> where ");
         retrieveSelectClause = retrieveSelectClauseBuf.toString();
     }
 
     /**
      * Injects the data source.
      * 
      * @spring.property ref="fedora.triplestore.DataSource"
      * @param driverManagerDataSource
      */
     public void setMyDataSource(final DataSource myDataSource) {
         super.setDataSource(myDataSource);
     }
 
     private static final String HTTP_QUERY_BASE_SPO_NTRIPLES =
         "/risearch?" + "type=triples&" + "flush=false&" + "lang=spo&"
             + "format=ntriples&" + "limit=0&" + "distinct=off&" + "stream=off&"
             + "query=";
 
     private static final String HTTP_QUERY_BASE_ITQL_NTRIPLES =
         "/risearch?" + "type=triples&" + "flush=false&" + "lang=itql&"
             + "format=ntriples&" + "limit=0&" + "distinct=off&" + "stream=off&"
             + "query=";
 
     private static final String HTTP_QUERY_BASE_ITQL_RDF_XML =
         "/risearch?" + "type=triples&" + "flush=false&" + "lang=itql&"
             + "format=RDF/XML&" + "limit=0&" + "distinct=on&" + "stream=off&"
             + "query=";
 
     private static final String HTTP_QUERY_BASE_ITQL_CSV =
         "/risearch?" + "type=tuples&" + "flush=false&" + "lang=itql&"
             + "format=CSV&" + "limit=0&" + "distinct=on&" + "stream=off&"
             + "query=";
 
     private static AppLogger log =
         new AppLogger(SpoItqlTripleStoreUtility.class.getName());
 
     private final String fedoraRdfXmlUrl;
 
     private final String fedoraItqlCsvUrl;
 
     private final String fedoraItqlNtriplesUrl;
 
     private final String fedoraSpoNtriplesUrl;
 
     /**
      * Enumeration of formats used in requestItql.
      * 
      */
     private enum Format {
         RDF_XML, N_TRIPLES, CSV
     };
 
     private String requestItqlNTriples(
         final String itqlQuery, final String template)
         throws TripleStoreSystemException, InvalidTripleStoreQueryException,
         InvalidTripleStoreOutputFormatException {
         return requestITQL(itqlQuery, template, Format.N_TRIPLES);
     }
 
     @Override
     protected String executeQueryEarliestCreationDate()
         throws TripleStoreSystemException {
         throw new TripleStoreSystemException("not implemented");
     }
 
     private String requestITQL(
         final String itqlQuery, final String template, final Format format)
         throws TripleStoreSystemException, InvalidTripleStoreQueryException,
         InvalidTripleStoreOutputFormatException {
         String result = null;
 
         try {
             StringBuffer queryAddress;
 
             if (format == Format.RDF_XML) {
                 queryAddress = new StringBuffer(fedoraRdfXmlUrl);
             }
             else if (format == Format.N_TRIPLES) {
                 queryAddress = new StringBuffer(fedoraItqlNtriplesUrl);
             }
             else if (format == Format.CSV) {
                 queryAddress = new StringBuffer(fedoraItqlCsvUrl);
             }
             else {
                 queryAddress = new StringBuffer(fedoraItqlNtriplesUrl);
             }
 
             queryAddress.append(URLEncoder.encode(itqlQuery,
                 XmlUtility.CHARACTER_ENCODING));
 
             if (template != null) {
                 queryAddress.append("&template=");
                 queryAddress.append(URLEncoder.encode(template,
                     XmlUtility.CHARACTER_ENCODING));
             }
 
             result = doRequest(queryAddress.toString());
         }
         catch (final UnsupportedEncodingException e) {
             throw new TripleStoreSystemException(e);
         }
 
         return result;
     }
 
     private String requestSPO(final String spoQuery)
         throws InvalidTripleStoreQueryException,
         InvalidTripleStoreOutputFormatException, TripleStoreSystemException {
         String result = null;
 
         String queryAddress = null;
         try {
             queryAddress =
                 fedoraSpoNtriplesUrl
                     + URLEncoder
                         .encode(spoQuery, XmlUtility.CHARACTER_ENCODING);
         }
         catch (final UnsupportedEncodingException e) {
             throw new TripleStoreSystemException(e);
         }
         result = doRequest(queryAddress);
 
         return result;
     }
 
     private static String doRequest(final String address)
         throws TripleStoreSystemException, InvalidTripleStoreQueryException,
         InvalidTripleStoreOutputFormatException {
         String result = null;
 
         log.debug(StringUtility.format("doRequest",
             address));
 
         try {
             final URL url = new URL(address);
             final URLConnection con = url.openConnection();
             final InputStream in = con.getInputStream();
             final ByteArrayOutputStream out = new ByteArrayOutputStream();
 
             try {
                 final byte[] buf = new byte[4096];
                 int len;
                 while ((len = in.read(buf)) > 0) {
                     out.write(buf, 0, len);
                 }
             }
             finally {
                 try {
                     in.close();
                 }
                 catch (final IOException e) {
                     log.warn("Could not close result inputstream.");
                 }
             }
 
             result = out.toString(XmlUtility.CHARACTER_ENCODING); // con.getContentEncoding());
         }
         catch (final MalformedURLException e) {
             throw new TripleStoreSystemException(e);
         }
         catch (final IOException e) {
             throw new TripleStoreSystemException(e);
         }
 
         if (result != null) {
             if (result.startsWith("<html")) {
                 final Pattern p =
                     Pattern.compile(TripleStoreConnector.QUERY_ERROR);
                 final Matcher m = p.matcher(result);
 
                 final Pattern p1 =
                     Pattern.compile(TripleStoreConnector.PARSE_ERROR);
                 final Matcher m1 = p1.matcher(result);
 
                 final Pattern p2 =
                     Pattern.compile(TripleStoreConnector.FORMAT_ERROR);
                 final Matcher m2 = p2.matcher(result);
                 if (m.find()) {
                     log.error(result);
                     result =
                         XmlUtility.CDATA_START + result + XmlUtility.CDATA_END;
                     if (m1.find()) {
                         throw new InvalidTripleStoreQueryException(result);
                     }
                     else if (m2.find()) {
                         throw new InvalidTripleStoreOutputFormatException(
                             result);
                     }
                 }
                 else {
                     log.error("Request failed:\n" + result);
                     result =
                         XmlUtility.CDATA_START + result + XmlUtility.CDATA_END;
                     throw new TripleStoreSystemException(
                         "Request to triplestore failed." + result);
                 }
             }
         }
 
         return result;
     }
 
     // CHECKSTYLE:JAVADOC-OFF
 
     /**
      * See Interface for functional description.
      * 
      * @param id
      * @param targetIsSubject
      * @param predicate
      * @return
      * @throws TripleStoreSystemException
      * @see de.escidoc.core.common.business.fedora.TripleStoreUtility
      *      #executeQueryId(java.lang.String, boolean, java.lang.String)
      * @common
      */
     @Override
     public List<String> executeQueryId(
         final String id, final boolean targetIsSubject, final String predicate)
         throws TripleStoreSystemException {
         return executeQuery(false, id, targetIsSubject, predicate);
     }
 
     // CHECKSTYLE:JAVADOC-ON
 
     protected List<String> executeQuery(
         final boolean queryByLiteral, final String idOrLiteral,
         final boolean targetIsSubject, final String predicate)
         throws TripleStoreSystemException {
 
         final List<String> result = new ArrayList<String>();
 
         String source = null;
         if (queryByLiteral) {
             source =
                 "\""
                     + idOrLiteral.replaceAll("\\\\", "\\\\\\\\").replaceAll(
                         "\"", "\\\\\"") + "\"";
         }
         else {
             source = "<info:fedora/" + idOrLiteral + ">";
         }
 
         try {
 
             // get the triples in n-triples
             String spoQuery = null;
             if (targetIsSubject) {
                 spoQuery = "* <" + predicate + ">  " + source;
             }
             else {
                 spoQuery = source + " <" + predicate + "> *";
             }
             final String response = requestSPO(spoQuery);
             final String[] triples = response.split("\\s\\.");
             for (int i = 0; i < triples.length; i++) {
                 final String[] tripleParts = triples[i].trim().split("\\ +", 3);
                 String entry = null;
                 if (targetIsSubject) {
                     if (tripleParts.length > 0 && tripleParts[0].length() > 0) {
                         entry = tripleParts[0];
                     }
                 }
                 else {
                     if (tripleParts.length > 2) {
                         entry = tripleParts[2];
                     }
                 }
 
                 if (entry != null) {
                     if (entry.startsWith("<info")) {
                         entry = XmlUtility.getIdFromURI(entry);
                     }
                     else if (entry.startsWith("\"")) {
                         final int pos = entry.lastIndexOf('"');
 
                         if (pos > 1) {
                             // FIXME ? entry is just a '"'? is that an error?
                             // (FRS)
                             entry = entry.substring(1, pos);
                         }
                     }
                     // TODO search for unescape method NOT in MPTStore API
                     result.add(XmlUtility
                         .escapeForbiddenXmlCharacters(NTriplesUtil
                             .unescapeLiteralValue(entry)));
                 }
             }
         }
         catch (final InvalidTripleStoreOutputFormatException e) {
             throw new TripleStoreSystemException(e);
         }
         catch (final InvalidTripleStoreQueryException e) {
             throw new TripleStoreSystemException(e);
         }
         catch (final FactoryConfigurationError e) {
             throw new TripleStoreSystemException(e);
         }
         catch (final ParseException e) {
             throw new TripleStoreSystemException(
                 "While unescaping literal value: ", e);
         }
 
         return result;
     }
 
     // CHECKSTYLE:JAVADOC-OFF
 
     /**
      * See Interface for functional description.
      * 
      * @param literal
      * @param targetIsSubject
      * @param predicate
      * @return
      * @throws TripleStoreSystemException
      * @see de.escidoc.core.common.business.fedora.TripleStoreUtility
      *      #executeQueryLiteral(java.lang.String, boolean, java.lang.String)
      * @common
      */
     @Override
     protected List<String> executeQueryLiteral(
         final String literal, final boolean targetIsSubject,
         final String predicate) throws TripleStoreSystemException {
         return executeQuery(true, literal, targetIsSubject, predicate);
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param pid
      * @param property
      * @param namespaceUri
      * @return
      * @throws TripleStoreSystemException
      * @see de.escidoc.core.common.business.fedora.TripleStoreUtility
      *      #getRelation(java.lang.String, java.lang.String, java.lang.String)
      * @common
      */
     @Override
     public String getRelation(
         final String pid, final String fullqualifiedPropertyName)
         throws TripleStoreSystemException {
         String result = null;
 
         try {
             final String entry =
                 executeQueryId(pid, false, fullqualifiedPropertyName).get(0);
             result = entry;
         }
         catch (final ArrayIndexOutOfBoundsException e) {
         }
         return result;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param pid
      * @param properties
      * @param namespaceUri
      * @return
      * @throws TripleStoreSystemException
      * @see de.escidoc.core.common.business.fedora.TripleStoreUtility
      *      #getProperties(java.lang.String, java.util.Collection,
      *      java.lang.String)
      * @common
      */
     @Override
     public Map<String, String> getProperties(
         final String pid, final Collection<String> fullqualifiedNamedProperties)
         throws TripleStoreSystemException {
 
         StringBuffer query = new StringBuffer("select $p $v from <#ri> ");
         final String template = "<info:fedora/" + pid + "> $p $v";
 
         String propertyName;
         final Iterator<String> propertiesIterator =
             fullqualifiedNamedProperties.iterator();
         // first part has "where" then "or"
         if (propertiesIterator.hasNext()) {
             propertyName = propertiesIterator.next();
             query.append("where (<info:fedora/");
             query.append(pid);
             query.append("> $p $v and <info:fedora/");
             query.append(pid);
             query.append("> <");
             query.append(propertyName);
             query.append("> $v) ");
         }
         while (propertiesIterator.hasNext()) {
             propertyName = propertiesIterator.next();
             query.append("or (<info:fedora/");
             query.append(pid);
             query.append("> $p $v and <info:fedora/");
             query.append(pid);
             query.append("> <");
             query.append(propertyName);
             query.append("> $v) ");
         }
 
         String response = null;
         try {
             response = requestItqlNTriples(query.toString(), template);
         }
         catch (final InvalidTripleStoreQueryException e) {
             throw new TripleStoreSystemException(e);
         }
         catch (final InvalidTripleStoreOutputFormatException e) {
             throw new TripleStoreSystemException(e);
         }
 
         final Map<String, String> result = new HashMap<String, String>();
 
         // split N-Triples response
         final String[] triples = PATTERN_WHITESPACE_DOT.split(response);
         // final String[] triples = response.split("\\s\\.");
         for (int i = 0; i < triples.length; i++) {
             // final String[] tripleParts =
             // PATTERN_BLANKS.split(triples[i].trim(), 3);
             final String[] tripleParts = triples[i].trim().split("\\ +", 3);
             if (tripleParts.length > 2) {
                 final String property = tripleParts[1];
                 String entry = tripleParts[2];
 
                 // propertyName =
                 // property.substring(property.lastIndexOf('/') + 1, property
                 // .length() - 1);
                 propertyName = property.substring(1, property.length() - 1);
 
                 if (entry != null) {
                     if (entry.startsWith("<info")) {
                         entry = XmlUtility.getIdFromURI(entry);
                     }
                     else if (entry.startsWith("\"")) {
                         entry = entry.substring(1, entry.lastIndexOf('"'));
                         // remove every escaping backslash
                         // entry = entry.replaceAll("\\\\([^\\\\])", "$1");
                         try {
                             entry = NTriplesUtil.unescapeLiteralValue(entry);
                         }
                         catch (final ParseException e) {
                             throw new TripleStoreSystemException(
                                 "While unescaping literal value: ", e);
                         }
                     }
                     result.put(propertyName, XmlUtility
                         .escapeForbiddenXmlCharacters(entry));
 
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Retrieves values from the triple store using the provided query.
      * 
      * @param query
      *            The query to execute.
      * @return Returns the result list of the query.
      * @throws TripleStoreSystemException
      *             Thrown in case of an internal triple store error.
      * @common
      */
     @Override
     public List<String> retrieve(final String query)
         throws TripleStoreSystemException {
 
         final String response;
         try {
             response = requestITQL(query, SELECT_VAR, Format.CSV);
         }
         catch (final Exception e) {
             throw new TripleStoreSystemException(e);
         }
         List<String> result = null;
         final String[] triples = PATTERN_WHITESPACES.split(response);
         if (triples.length > 1) {
             result = new ArrayList<String>();
             for (int i = 1; i < triples.length; i++) {
                 final String entry = triples[i];
                 if (entry != null) {
                     if (entry.startsWith("info:")) {
                         result.add(XmlUtility.getIdFromURI(entry));
                     }
                     else if (entry.startsWith("\"")) {
                         result.add(entry.substring(1, entry.lastIndexOf('"')));
                     }
                     else {
                         result.add(entry);
                     }
                 }
             }
         }
         return result;
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
         final String contentModelTitleId, final String contentModelTitle) {
 
         final StringBuffer whereClause = new StringBuffer("(");
         boolean isFirst = true;
         // if resource type is 'member' look for items AND container
 
         if ("member".equals(targetResourceType)) {
             isFirst = false;
             whereClause.append("(");
             whereClause.append(SELECT_VAR);
             whereClause.append(" <");
             whereClause.append(PROP_OBJECT_TYPE);
             whereClause.append("> <");
             whereClause.append(Constants.ITEM_OBJECT_TYPE);
             whereClause.append("> or ");
             whereClause.append(SELECT_VAR);
             whereClause.append(" <");
             whereClause.append(PROP_OBJECT_TYPE);
             whereClause.append("> <");
             whereClause.append(Constants.CONTAINER_OBJECT_TYPE);
             whereClause.append(">)");
         }
         // Will be resource type in filter
         // http://escidoc.de/core/01/resources/Container
         // or "container"
         else if (targetResourceType != null) {
             isFirst = false;
             whereClause.append(SELECT_VAR);
             whereClause.append(" <");
             whereClause.append(PROP_OBJECT_TYPE);
             whereClause.append("> <");
             whereClause.append(targetResourceType);
             whereClause.append("> ");
         }
 
         // StringBuffer contentModelQueryPart = new StringBuffer();
         if (contentModelTitleId != null) {
             if (!isFirst) {
                 whereClause.append(" and ");
             }
             isFirst = false;
             whereClause.append(SELECT_VAR);
             whereClause.append(" <");
             whereClause.append(contentModelTitleId);
             whereClause.append("> $ctm");
             whereClause.append(" and $ctm ");
             whereClause.append(PROP_TITLE);
             whereClause.append(" '");
             whereClause.append(contentModelTitle);
             whereClause.append("' ");
         }
 
         if (!isFirst) {
             whereClause.append(" and ");
         }
         isFirst = false;
         if (targetIsSubject || predicateId.equals(Constants.DC_IDENTIFIER_URI)) {
             // TODO is the or clause necessary?
             whereClause.append("(");
             whereClause.append(SELECT_VAR);
             whereClause.append(" <");
             whereClause.append(predicateId);
             whereClause.append("> '");
             whereClause.append(expectedValue);
             whereClause.append("' or ");
             whereClause.append(SELECT_VAR);
             whereClause.append(" <");
             whereClause.append(predicateId);
             whereClause.append("> <info:fedora/");
             whereClause.append(expectedValue);
             whereClause.append(">) ");
         }
         else {
             whereClause.append("<info:fedora/");
             whereClause.append(expectedValue);
             whereClause.append("> <");
             whereClause.append(predicateId);
             whereClause.append("> ");
             whereClause.append(SELECT_VAR);
         }
         whereClause.append(")");
         return whereClause;
     }
 
     /**
      * Builds the starting clause of a query to the triple store to retrieve
      * objects.
      * 
      * @param targetIsSubject
      *            targetIsSubject
      * @return Returns the starting clause "select $s from <#ri> where " in a
      *         {@link StringBuffer}
      * @common
      */
     @Override
     public StringBuffer getRetrieveSelectClause(
         final boolean targetIsSubject, final String predicateId) {
 
         return new StringBuffer(retrieveSelectClause);
     }
 
     // CHECKSTYLE:JAVADOC-ON
 
     private String getQueryPartProperties(final Map<String, String> filters) {
         if (filters.isEmpty()) {
             // just provide NO query part if there are no predicates properties
             return "";
         }
 
         final StringBuffer queryPart = new StringBuffer();
 
         final Iterator<String> it = filters.keySet().iterator();
         while (it.hasNext()) {
             final String predicate = it.next();
             String object;
 
             final String val = filters.get(predicate);
 
             // make URIs from given IDs or HREFs for all structural-relation
             // predicates
             if (predicate.startsWith(Constants.STRUCTURAL_RELATIONS_NS_URI)) {
                 String id = val;
                 if (val.startsWith("http://") || val.startsWith("/")) {
                     id = Utility.getId(val);
                 }
                 object = "<info:fedora/" + id + ">";
             }
             else {
                 object = "'" + val + "'";
             }
 
             queryPart.append(" and $s <" + predicate + "> " + object + " ");
         }
         return queryPart.toString();
     }
 
     // CHECKSTYLE:JAVADOC-OFF
 
     /**
      * See Interface for functional description.
      * 
      * @param objectType
      * @param filterXml
      * @return
      * @throws InvalidContentException
      * @throws TripleStoreSystemException
      * @see de.escidoc.core.common.business.fedora.TripleStoreUtility#getObjectList(java.lang.String,
      *      java.lang.String)
      */
     @Override
     public String getObjectList(
         final String objectType, final Map filterMap, final String whereClause)
         throws InvalidContentException, TripleStoreSystemException,
         MissingMethodParameterException {
 
         String rdfObjectList = null;
         Map filters = null;
         boolean ordered = false;
 
         filters = (Map) filterMap.get("filter");
         if (filterMap.get("order-by") != null) {
             ordered = true;
         }
 
         final String template = "$s $p $o";
         final StringBuffer itqlQuery = new StringBuffer("select $s ");
         if (ordered) {
             itqlQuery.append(" $order");
         }
         itqlQuery.append(" subquery(");
         itqlQuery.append(getObjectListSubQuery());
         itqlQuery.append(" )");
         itqlQuery.append(" from <#ri> where ");
         // restrict to object-type
         if ("member".equalsIgnoreCase(objectType)) {
             if ((filters != null) && filters.containsKey(PROP_OBJECT_TYPE)) {
                 itqlQuery.append("$s <" + PROP_OBJECT_TYPE + "> <"
                     + filters.remove(PROP_OBJECT_TYPE) + "> ");
             }
             else {
                 itqlQuery.append("($s <" + PROP_OBJECT_TYPE
                     + "> <http://escidoc.de/core/01/resources/Item> "
                     + "or $s <" + PROP_OBJECT_TYPE
                     + "> <http://escidoc.de/core/01/resources/Container>) ");
             }
         }
         else {
             itqlQuery.append("$s <" + PROP_OBJECT_TYPE + "> <" + objectType
                 + "> ");
         }
 
         if (filters != null) {
             itqlQuery.append(getQueryPartId((Set) filters
                 .remove(Constants.DC_IDENTIFIER_URI)));
 
             if (whereClause.length() > 0) {
                 itqlQuery.append(" and " + whereClause);
             }
 
             final String topLevelOus =
                 (String) filters.remove("top-level-organizational-units");
 
             itqlQuery.append(getQueryPartProperties(filters));
 
             if (topLevelOus != null) {
                 // shouldn't have a parent
                 itqlQuery.append("minus $s <"
                     + Constants.STRUCTURAL_RELATIONS_NS_URI
                     + "parent> $parent ");
             }
         }
 
         final StringBuffer querySuffix = new StringBuffer();
         if (ordered) {
             querySuffix.append(" and $s <");
             querySuffix.append(filterMap.get("order-by"));
             querySuffix.append("> $order order by $order");
             if (filterMap.get("sorting") == ListSorting.DESCENDING) {
                 querySuffix.append(" desc");
             }
             else {
                 querySuffix.append(" asc");
             }
         }
         else {
             querySuffix.append(" order by");
         }
         querySuffix.append(" $s");
         if (filterMap.get("sorting") == ListSorting.DESCENDING) {
             querySuffix.append(" desc");
         }
         else {
             querySuffix.append(" asc");
         }
         querySuffix.append(" limit ");
         querySuffix.append(filterMap.get("limit"));
         querySuffix.append(" offset ");
         querySuffix.append(filterMap.get("offset"));
         querySuffix.append(" ");
 
         if (querySuffix != null) {
             itqlQuery.append(querySuffix);
         }
 
         try {
             rdfObjectList =
                 requestITQL(itqlQuery.toString(), template, Format.RDF_XML);
         }
         catch (final Exception e) {
             throw new TripleStoreSystemException(e);
         }
 
         return rdfObjectList;
     }
 
     // CHECKSTYLE:JAVADOC-ON
 
     private String getQueryPartId(final Set<String> ids) {
         if (ids == null || ids.isEmpty()) {
             return "";
         }
 
         final StringBuffer queryPart = new StringBuffer("and ( ");
 
         final Iterator<String> it = ids.iterator();
         if (it.hasNext()) {
             queryPart.append("$s <" + Constants.DC_IDENTIFIER_URI + "> '"
                 + it.next() + "' ");
         }
         while (it.hasNext()) {
             queryPart.append("or $s <" + Constants.DC_IDENTIFIER_URI + "> '"
                 + it.next() + "' ");
         }
 
         queryPart.append(" ) ");
         return queryPart.toString();
     }
 
     @Override
     public List<String> executeQueryForList(
         final Collection<String> ids, final boolean targetIsSubject,
         final String predicate) throws TripleStoreSystemException {
         // not implemented
         return new LinkedList<String>();
     }
 
     // /**
     // *
     // * @param objectType
     // * @param filterMap
     // * @return
     // * @throws TripleStoreSystemException
     // * @throws XmlParserSystemException
     // * @throws MissingMethodParameterException
     // */
     // public List<String> evaluate(
     // final String objectType, final Map filterMap, final String whereClause)
     // throws TripleStoreSystemException, XmlParserSystemException,
     // MissingMethodParameterException {
     //
     // return evaluate(objectType, filterMap, null, whereClause);
     // }
 
     /**
      * Pattern used to convert the object type retrieved from the triple store.
      */
     private static final Pattern PATTERN_CONVERT_TRIPLESTORE_OBJECT_TYPE =
         Pattern.compile("(" + Constants.RESOURCES_NS_URI
             + "){0,1}([A-Z])([^A-Z]*)");
 
     private static final int GROUP_NUMBER_TAILING_CHARACTERS = 3;
 
     private static final int GROUP_NUMBER_FIRST_CHARACTER = 2;
 
     private final String retrieveSelectClause;
 
     /**
      * @deprecated is not longer needed
      * @param objectType
      * @return
      */
     @Deprecated
     private String getObjectsToFind(final String objectType) {
         final Matcher matcher =
             PATTERN_CONVERT_TRIPLESTORE_OBJECT_TYPE.matcher(objectType);
         final StringBuffer result = new StringBuffer();
         if (matcher.find()) {
             boolean hasNext;
             do {
                 result.append(matcher
                     .group(GROUP_NUMBER_FIRST_CHARACTER).toLowerCase());
                 result.append(matcher.group(GROUP_NUMBER_TAILING_CHARACTERS));
                 hasNext = matcher.find(matcher.end());
                 if (hasNext) {
                     result.append('-');
                 }
             }
             while (hasNext);
         }
         result.append("s");
         return result.toString();
     }
 
     /**
      * 
      * @param objectType
      * @param filterXML
      * @param additionalQueryPart
      * @param whereClause
      * @return
      * @throws TripleStoreSystemException
      * @throws XmlParserSystemException
      * @throws MissingMethodParameterException
      */
     public List<String> evaluate(
         final String objectType, final Map filterMap,
         final String additionalQueryPart, final String whereClause)
         throws TripleStoreSystemException, XmlParserSystemException,
         MissingMethodParameterException {
 
         final List<String> result = new ArrayList<String>();
         // String objectsToFind = getObjectsToFind(objectType);
 
         final String template = SELECT_VAR;
         final StringBuffer query = getRetrieveSelectClause(false, null);
 
         Map filter = (Map) filterMap.get("filter");
 
         if ("member".equalsIgnoreCase(objectType)) {
             if ((filter != null) && filter.containsKey(PROP_OBJECT_TYPE)) {
                 query.append("$s <" + PROP_OBJECT_TYPE + "> <"
                     + filter.remove(PROP_OBJECT_TYPE) + "> ");
             }
             else {
                 query.append("($s <" + PROP_OBJECT_TYPE
                     + "> <http://escidoc.de/core/01/resources/Item> "
                     + "or $s <" + PROP_OBJECT_TYPE
                     + "> <http://escidoc.de/core/01/resources/Container>) ");
             }
         }
         else {
             query.append("$s <" + PROP_OBJECT_TYPE + "> <" + objectType + "> ");
         }
 
         if (additionalQueryPart != null) {
             query.append(additionalQueryPart + " ");
 
         }
 
         // stored later for later use
         String roleCriteria = null;
         String userCriteria = null;
         String topLevelOus = null;
 
         if (filter != null) {
             // given IDs
             query.append(getQueryPartId((Set) filter
                 .remove(Constants.DC_IDENTIFIER_URI)));
 
             // // TODO remove mapping of old filter names
             // // ##########################
             // // object type
             // if (filter.containsKey("object-type")) {
             // filter.put(PROP_OBJECT_TYPE, filter.remove("object-type"));
             // }
             // Iterator keyIter = filter.keySet().iterator();
             // Map mappedKeys = new HashMap();
             // List toDelete = new Vector();
             // while (keyIter.hasNext()) {
             // String key = (String) keyIter.next();
             // if (!key.startsWith("http:")) {
             // String nsUri = Constants.PROPERTIES_NS_URI;
             //
             // if (key.equals("context")
             // || key.equals(Elements.ELEMENT_CONTENT_MODEL)
             // || key.equals("organizational-unit")
             // || key.equals("created-by")
             // || key.equals("modified-by")) {
             // nsUri = Constants.STRUCTURAL_RELATIONS_NS_URI;
             // }
             // toDelete.add(key);
             // mappedKeys.put(nsUri + key, filter.get(key));
             // }
             // }
             // Iterator delIter = toDelete.iterator();
             // while (delIter.hasNext()) {
             // filter.remove(delIter.next());
             // }
             // filter.putAll(mappedKeys);
             //
             // // unused filters
             // filter.remove("related");
             // // end TODO
             // ###############################################################
 
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
 
                 if (whereClause == null) {
                     return new ArrayList<String>(0);
                 }
                 if (whereClause.length() > 0) {
                     query.append(" and (" + whereClause + ")");
                 }
             }
 
             topLevelOus =
                 (String) filter.remove("top-level-organizational-units");
 
             // generic
             query.append(getQueryPartProperties(filter));
         }
 
         if (topLevelOus != null) {
             // shouldn't have a parent
             query.append("minus $s <" + Constants.STRUCTURAL_RELATIONS_NS_URI
                 + "parent> $parent ");
         }
 
         String response;
         try {
             final String q = query.toString();
             response = requestITQL(q, template, Format.CSV);
         }
         catch (final Exception e) {
             throw new TripleStoreSystemException(e);
         }
         final String[] ids = response.split("\\s+");
         for (int i = 1; i < ids.length; i++) {
             final String id = ids[i].trim();
             result.add(id.substring(id.lastIndexOf('/') + 1));
         }
         return result;
     }
 
     /**
      * 
      * @param containerId
      * @param filterParam
      * @return
      * @throws MissingMethodParameterException
      * @throws TripleStoreSystemException
      * @throws XmlParserSystemException
      */
     public List<String> getContainerMemberList(
         final String containerId, final Map filterMap, final String whereClause)
         throws MissingMethodParameterException, TripleStoreSystemException,
         XmlParserSystemException {
 
         // TODO check functionality
         List<String> result = null;
         result =
             evaluate("member", filterMap, "and $parent <"
                 + Constants.STRUCTURAL_RELATIONS_NS_URI + "member> $s "
                 + "and $parent <" + PROP_OBJECT_TYPE
                 + "> <http://escidoc.de/core/01/resources/Container> "
                 + "and $parent <http://purl.org/dc/elements/1.1/identifier> '"
                 + containerId + "' ", whereClause);
         return result;
     }
 
     /**
      * 
      */
     public List<String> getContextMemberList(
         final String contextId, final Map filterMap, final String whereClause)
         throws TripleStoreSystemException, XmlParserSystemException,
         MissingMethodParameterException {
 
         // TODO check functionality
         List<String> result = null;
         result =
             evaluate("member", filterMap, "and ($s <"
                 + Constants.STRUCTURAL_RELATIONS_NS_URI
                 + "context> <info:fedora/" + contextId + ">) ", whereClause);
         // $parent "
         // + "and $parent <" + PROP_OBJECT_TYPE
         // + "> <http://escidoc.de/core/01/resources/Context> "
         // + "and $parent <http://purl.org/dc/elements/1.1/identifier> '"
         // + contextId + "' ");
         return result;
     }
 
     /**
      * 
      */
     public List<String> getMemberList(final String id, final String whereClause)
         throws TripleStoreSystemException {
         // TODO check functionality
         List<String> result = null;
         try {
             result =
                 evaluate("member", new HashMap(), " and <info:fedora/" + id
                     + "> <" + Constants.STRUCTURAL_RELATIONS_NS_URI
                     + "member> $s ", whereClause);
         }
         catch (final MissingMethodParameterException e) {
             // no filter no MissingMethodParameterException
             throw new TripleStoreSystemException(
                 "Unexpected exception: should not occure if evaluate is called without filter param.",
                 e);
         }
         catch (final XmlParserSystemException e) {
             // no filter no XmlParserSystemException
             throw new TripleStoreSystemException(
                 "Unexpected exception: should not occure if evaluate is called without filter param.",
                 e);
         }
         return result;
     }
 
     /**
      * 
      */
     public String getObjectRefs(
         final String objectType, final Map filterMap, final String whereClause)
         throws SystemException, MissingMethodParameterException {
 
         final List<String> list =
             evaluate(objectType, filterMap, null, whereClause);
         final String resourcesName = getObjectsToFind(objectType);
         final String resourceName =
             resourcesName.substring(0, resourcesName.length() - 1);
 
         String absoluteLocalPathFirstPart = "ir";
         if (resourceName.equals(Elements.ELEMENT_CONTENT_MODEL)) {
             absoluteLocalPathFirstPart = "ctm";
         }
 
         final String namespacePrefix = resourceName + "-ref-list";
         String schemaVersion = "0.2";
         if (resourceName.equals("item")) {
             schemaVersion = "0.4";
         }
         final String namespaceUri =
             "http://www.escidoc.de/schemas/" + resourceName + "reflist/"
                 + schemaVersion;
         final String rootElementName = resourceName + "-ref-list";
         final String listElementName = resourceName + "-ref";
 
         final String prefixedRootElement =
             namespacePrefix + ":" + rootElementName;
         final String prefixedListElement =
             namespacePrefix + ":" + listElementName;
 
         final String namespaceDecl =
             " xmlns:" + namespacePrefix + "=\"" + namespaceUri + "\" ";
 
         final StringBuffer sb = new StringBuffer();
 
         sb.append("<");
         sb.append(prefixedRootElement);
 
         sb.append(namespaceDecl);
         if (UserContext.isRestAccess()) {
             sb.append(" xlink:title=\"list of ");
             sb.append(resourceName);
             sb
                 .append(" references\" xlink:type=\"simple\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
             sb.append(" xml:base=\"");
             sb.append(XmlUtility.getEscidocBaseUrl() + "\"");
         }
         sb.append(">");
 
         final Iterator<String> it = list.iterator();
         while (it.hasNext()) {
             final String id = it.next();
             sb.append("<");
             sb.append(prefixedListElement);
             if (UserContext.isRestAccess()) {
                 sb.append(" xlink:href=\"/");
                 sb.append(absoluteLocalPathFirstPart);
                 sb.append("/");
                 sb.append(resourceName);
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
        return sb.toString();
     }
 
     public Object reinitialize() throws TripleStoreSystemException {
         // nothing to do
         return null;
     }
 
     private static String getObjectListSubQuery() {
         final StringBuffer sb =
             new StringBuffer("select $s $p $o from <#ri> where $s $p $o ");
         sb.append("and (");
         // add conditional or-clause for each possible property
         sb
             .append("    ($s $p $o and $s <http://purl.org/dc/elements/1.1/identifier> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/title> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/relation> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/contributor> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/coverage> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/date> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/creator> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/description> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/format> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/language> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/rights> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/publisher> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/source> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/subject> $o)");
         sb
             .append(" or ($s $p $o and $s <http://purl.org/dc/elements/1.1/type> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/public-status> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/created-by-title> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/context-title> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/public-status> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/active> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/content-model-title> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/creation-date> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/email> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/grant-remark> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/has-children> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/login-name> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/modified-by-title> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/pid> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/revocation-date> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/revocation-remark> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/type> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/version/date> $o )");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/version/status> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/version/pid> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/version/number> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/version/comment> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/release/number> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/release/date> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/properties/release/pid> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/affiliation> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/assigned-on> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/child> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/container> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/content-model> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/context> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/created-by> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/item> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/lock-owner> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/member> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/modified-by> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/organizational-unit> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/parent> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/person> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/revoked-by> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/role> $o)");
         sb
             .append(" or ($s $p $o and $s <http://escidoc.de/core/01/structural-relations/component> $o)");
         sb
             .append(" or ($s $p $o and $s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> $o)");
         sb.append(")");
         return sb.toString();
     }
 
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
         throw new UnsupportedOperationException(
             "Not implemented for SpoItqlTripleStore");
     }
 
     public boolean exists(final String pid) throws TripleStoreSystemException {
         throw new UnsupportedOperationException(
             "Not implemented for SpoItqlTripleStore");
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
         throw new UnsupportedOperationException(
         "Not implemented for SpoItqlTripleStore");
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
         throw new UnsupportedOperationException(
         "Not implemented for SpoItqlTripleStore");
     }
 }
