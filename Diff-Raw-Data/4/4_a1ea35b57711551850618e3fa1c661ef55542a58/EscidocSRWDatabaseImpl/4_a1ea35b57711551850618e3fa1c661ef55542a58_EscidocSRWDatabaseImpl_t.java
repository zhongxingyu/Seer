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
  * Copyright 2006-2007 Fachinformationszentrum Karlsruhe Gesellschaft
  * für wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Förderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 
 package de.escidoc.sb.srw;
 
 import gov.loc.www.zing.srw.ExtraDataType;
 import gov.loc.www.zing.srw.RecordType;
 import gov.loc.www.zing.srw.RecordsType;
 import gov.loc.www.zing.srw.SearchRetrieveRequestType;
 import gov.loc.www.zing.srw.SearchRetrieveResponseType;
 import gov.loc.www.zing.srw.StringOrXmlFragment;
 
 import java.io.StringReader;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 
 import javax.servlet.ServletException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.axis.MessageContext;
 import org.apache.axis.message.MessageElement;
 import org.apache.axis.message.Text;
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.axis.types.PositiveInteger;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osuosl.srw.ResolvingQueryResult;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 import org.z3950.zing.cql.CQLNode;
 import org.z3950.zing.cql.CQLParseException;
 
 import ORG.oclc.os.SRW.Record;
 import ORG.oclc.os.SRW.RecordIterator;
 import ORG.oclc.os.SRW.SRWDiagnostic;
 import de.escidoc.sb.srw.lucene.EscidocLuceneTranslator;
 
 /**
  * Class overwrites org.osuosl.srw.SRWDatabaseImpl. This is done because: -we
  * dont retrieve and store all search-hits but only the ones requested -we dont
  * use result-sets -we do sorting while querying lucene and not afterwards -get
  * dynamic index-info from available lucene-fields
  * 
  * @author MIH
  * @sb
  */
 public class EscidocSRWDatabaseImpl extends org.osuosl.srw.SRWDatabaseImpl {
 
     private static Log log = LogFactory.getLog(EscidocSRWDatabaseImpl.class);
     
     private static final int DIAGNOSTIC_CODE_EIGHT = 8;
 
     private static final int DIAGNOSTIC_CODE_SEVENTY_ONE = 71;
 
     private static final int TEN = 10;
 
     private static final int ELEVEN = 11;
 
     private static final int MILLISECONDS_PER_SECOND = 1000;
 
     /**
      * execute search -> get results. getRecordIterator from results. iterate
      * over results, get records, put records into response.
      * 
      * @param request
      *            SearchRetrieveRequestType
      * @return SearchRetrieveResponseType response
      * @throws ServletException
      *             e
      * @sb
      */
     public SearchRetrieveResponseType doRequest(
         final SearchRetrieveRequestType request) throws ServletException {
 
         SearchRetrieveResponseType response = null; // search response
         int resultSetTTL; // time result set should expire
         String recordPacking; // how record is packed (xml|string)
         String query; // Search String
         String sortKeysString; // keys to sort on
         PositiveInteger startRec; // record number to start with
         ExtraDataType extraData = null; // extra params sent with request
         ResolvingQueryResult results; // results of search, array of
         // Ids/handles
 
         try {
             MessageContext msgContext = MessageContext.getCurrentContext();
             response = new SearchRetrieveResponseType();
             response.setNumberOfRecords(new NonNegativeInteger("0"));
             startRec = request.getStartRecord();
             extraData = request.getExtraRequestData();
 
             /**
              * Get schema name and verify its supported.
              */
             String schemaName = request.getRecordSchema();
             if (schemaName == null) {
                 schemaName = "default";
             }
             log.info("recordSchema=" + schemaName);
 
             if (schemaName != null && !schemaName.equals(DEFAULT_SCHEMA)) {
                 if (!getResolver().containsSchema(schemaName)) {
                     log.error("no handler for schema " + schemaName);
                     return diagnostic(SRWDiagnostic.UnknownSchemaForRetrieval,
                         schemaName, response);
                 }
             }
 
             /**
              * RecordXPath - UNSUPPORTED
              */
             if (request.getRecordXPath() != null
                 && request.getRecordXPath().trim().length() != 0) {
                 return diagnostic(
                     DIAGNOSTIC_CODE_EIGHT, request.getRecordXPath(), response);
             }
 
             /**
              * set result set TTL
              */
             if (request.getResultSetTTL() != null) {
                 resultSetTTL = request.getResultSetTTL().intValue();
             }
             else {
                 resultSetTTL = defaultResultSetTTL;
             }
 
             /**
              * Set Record Packing
              */
             recordPacking = request.getRecordPacking();
             if (recordPacking == null) {
                 if (msgContext != null && msgContext.getProperty("sru") != null) {
                     recordPacking = "xml"; // default for sru
                 }
                 else {
                     recordPacking = "string"; // default for srw
                 }
             }
 
             /**
              * get sort keys
              */
             sortKeysString = request.getSortKeys();
             if (sortKeysString != null && sortKeysString.length() == 0) {
                 sortKeysString = null;
             }
 
             /**
              * Parse and Execute Query
              */
             query = request.getQuery();
             try {
                 log.info("search:\n"
                     + ORG.oclc.util.Util.byteArrayToString(query
                         .getBytes("UTF-8")));
             }
             catch (Exception e) {
                 log.info(e);
             }
 
             CQLNode queryRoot = null;
            //MIH: Workaround because CQLParser has Bug when handling \"
            //delete \"
            query = query.replaceAll("\\\\\"", "");
            
             if (query.matches(".*[^\\\\]\".*")) {
             	query = escapeBackslash(query);
             }
             try {
                 queryRoot = parser.parse(query);
             }
             catch (CQLParseException e) {
                 return diagnostic(SRWDiagnostic.QuerySyntaxError, e
                     .getMessage(), response);
             }
 
             String resultSetID = queryRoot.getResultSetName();
             int numRecs = defaultNumRecs;
             NonNegativeInteger maxRecs = request.getMaximumRecords();
             if (maxRecs != null) {
                 numRecs =
                     (int) java.lang.Math.min(maxRecs.longValue(),
                         maximumRecords);
             }
 
             // MIH: set maxRecs in request, because request
             // gets passed to EscidocLuceneTranslator
             // EscidocLuceneTranslator performs search and fills identifers
             request.setMaximumRecords(new NonNegativeInteger(Integer
                 .toString(numRecs)));
 
             int startPoint = 1;
             if (startRec != null) {
                 startPoint = (int) startRec.longValue();
             }
             if (resultSetID != null) {
                 // look for existing result set
                 log.info("resultSetID=" + resultSetID);
                 results = (ResolvingQueryResult) oldResultSets.get(resultSetID);
                 if (results == null) {
                     return diagnostic(SRWDiagnostic.ResultSetDoesNotExist,
                         resultSetID, response);
                 }
 
             }
             else {
                 // MIH: call overwritten method that is only available in
                 // EscidocLuceneTranslator:
                 // 3rd parameter: request (to get sortKeys,
                 // startRecord, maxRecords..)!
                 results =
                     (ResolvingQueryResult) (
                         (EscidocLuceneTranslator) getCQLTranslator())
                         .search(queryRoot, extraData, request);
 
                 results.setResolver(getResolver());
                 results.setExtraDataType(extraData);
 
                 /**
                  * if results were found save the result set and setup the timer
                  * for when it expires
                  * 
                  */
                 if (results.getNumberOfRecords() > 0 && resultSetTTL > 0) {
                     resultSetID = makeResultSetID();
                     oldResultSets.put(resultSetID, results);
                     log.info("keeping resultSet '" + resultSetID + "' for "
                         + resultSetTTL + " seconds");
                     timers.put(resultSetID, new Long(System.currentTimeMillis()
                         + (resultSetTTL * MILLISECONDS_PER_SECOND)));
                     response.setResultSetId(resultSetID);
                     response.setResultSetIdleTime(new PositiveInteger(Integer
                         .toString(resultSetTTL)));
                 }
             }
 
             int postings = (int) results.getNumberOfRecords();
             response.setNumberOfRecords(new NonNegativeInteger(Long
                 .toString(postings)));
 
             log.info("'" + query + "'==> " + postings);
 
             if (postings > 0 && startPoint > postings) {
                 diagnostic(SRWDiagnostic.FirstRecordPositionOutOfRange, null,
                     response);
             }
             if ((startPoint - 1 + numRecs) > postings) {
                 numRecs = postings - (startPoint - 1);
             }
             if (postings > 0 && numRecs == 0) {
                 response.setNextRecordPosition(new PositiveInteger("1"));
             }
             if (postings > 0 && numRecs > 0) { // render some records into SGML
                 RecordsType records = new RecordsType();
                 log.info("trying to get " + numRecs
                     + " records starting with record " + startPoint
                     + " from a set of " + postings + " records");
                 if (!recordPacking.equals("xml")
                     && !recordPacking.equals("string")) {
                     return diagnostic(
                         DIAGNOSTIC_CODE_SEVENTY_ONE, recordPacking, response);
                 }
 
                 records.setRecord(new RecordType[numRecs]);
                 Document domDoc;
                 DocumentBuilder db = null;
                 DocumentBuilderFactory dbf = null;
                 int i = -1;
                 MessageElement[] elems;
                 RecordType record;
                 StringOrXmlFragment frag;
                 if (recordPacking.equals("xml")) {
                     dbf = DocumentBuilderFactory.newInstance();
                     dbf.setNamespaceAware(true);
                     db = dbf.newDocumentBuilder();
                 }
 
                 /**
                  * One at a time, retrieve and display the requested documents.
                  */
                 RecordIterator list = null;
                 try {
                     log.info("making RecordIterator, startPoint=" + startPoint
                         + ", schemaID=" + schemaName);
                     list =
                         results.recordIterator(startPoint - 1, numRecs,
                             schemaName);
                 }
                 catch (InstantiationException e) {
                     diagnostic(SRWDiagnostic.GeneralSystemError,
                         e.getMessage(), response);
                 }
 
                 for (i = 0; list.hasNext(); i++) {
                     try {
 
                         Record rec = list.nextRecord();
 
                         /**
                          * create record container
                          */
                         record = new RecordType();
                         record.setRecordPacking(recordPacking);
                         frag = new StringOrXmlFragment();
                         elems = new MessageElement[1];
                         frag.set_any(elems);
                         record.setRecordSchema(rec.getRecordSchemaID());
 
                         if (recordPacking.equals("xml")) {
                             domDoc =
                                 db.parse(new InputSource(new StringReader(rec
                                     .getRecord())));
                             Element el = domDoc.getDocumentElement();
                             log.debug("got the DocumentElement");
                             elems[0] = new MessageElement(el);
                             log.debug("put the domDoc into elems[0]");
                         }
                         else { // string
                             Text t = new Text(rec.getRecord());
                             elems[0] = new MessageElement(t);
                         }
 
                         record.setRecordData(frag);
                         log.debug("setRecordData");
 
                         record.setRecordPosition(new PositiveInteger(Integer
                             .toString(startPoint + i)));
                         records.setRecord(i, record);
 
                     }
                     catch (NoSuchElementException e) {
                         log.error("Read beyond the end of list!!");
                         log.error(e);
                         break;
                     }
                     response.setRecords(records);
                 }
                 if (startPoint + i <= postings) {
                     response.setNextRecordPosition(new PositiveInteger(Long
                         .toString(startPoint + i)));
                 }
             }
             log.debug("exit doRequest");
             return response;
 
         }
         catch (Exception e) {
             return diagnostic(SRWDiagnostic.GeneralSystemError, e.getMessage(),
                 response);
         }
 
     }
 
     /**
      * returns info about databases for explainPlan.
      * Overwritten because schema 2.0 doesnt allow 
      * attribute indentifier in element implementation
      * 
      * @return String databaseInfo xml for explainPlan
      * @sb
      */
     public String getDatabaseInfo() {
         StringBuffer sb=new StringBuffer();
         sb.append("        <databaseInfo>\n");
         if(dbProperties!=null) {
             String t=dbProperties.getProperty("databaseInfo.title");
             if(t!=null)
                 sb.append("          <title>").append(t).append("</title>\n");
             t=dbProperties.getProperty("databaseInfo.description");
             if(t!=null)
                 sb.append("          <description>").append(t).append("</description>\n");
             t=dbProperties.getProperty("databaseInfo.author");
             if(t!=null)
                 sb.append("          <author>").append(t).append("</author>\n");
             t=dbProperties.getProperty("databaseInfo.contact");
             if(t!=null)
                 sb.append("          <contact>").append(t).append("</contact>\n");
             t=dbProperties.getProperty("databaseInfo.restrictions");
             if(t!=null)
                 sb.append("          <restrictions>").append(t).append("</restrictions>\n");
         }
         sb.append("          <implementation version='1.1'>\n");
         sb.append("            <title>OCLC Research SRW Server version 1.1</title>\n");
         sb.append("            </implementation>\n");
         sb.append("          </databaseInfo>\n");
         return sb.toString();
     }
 
 
     /**
      * returns info about metaInfo for explainPlan.
      * Overwritten because schema 2.0 needs attribute dateModified
      * if element metaInfo is set. 
      * 
      * @return String databaseInfo xml for explainPlan
      * @sb
      */
     public String getMetaInfo() {
         StringBuffer sb=new StringBuffer();
         boolean writeElement = false;
         sb.append("        <metaInfo>\n");
         if(dbProperties!=null) {
             String t=dbProperties.getProperty("metaInfo.dateModified");
             if(t!=null) {
                 sb.append("          <dateModified>").append(t).append("</dateModified>\n");
                 writeElement = true;
             }
             t=dbProperties.getProperty("metaInfo.aggregatedFrom");
             if(t!=null) {
                 sb.append("          <aggregatedFrom>").append(t).append("</aggregatedFrom>\n");
             }
             t=dbProperties.getProperty("metaInfo.dateAggregated");
             if(t!=null) {
                 sb.append("          <dateAggregated>").append(t).append("</dateAggregated>\n");
             }
         }
         sb.append("          </metaInfo>\n");
         if (writeElement) {
             return sb.toString();
         } else {
             return "";
         }
     }
 
 
     /**
      * returns info about indices in this database for explainPlan. Dynamically
      * reads all fields from lucene-index and appends them to the explainPlan if
      * prefix of fieldName (string that ends with a dot) is defined as
      * contextSet in properties (contextSet...). Dynamically adds sortKeywords
      * by selecting all fields that start with a prefix as given in property
      * sortSet.
      * 
      * @return String indexInfo xml for explainPlan
      * @sb
      */
     public String getIndexInfo() {
         Enumeration enumer = dbProperties.propertyNames();
         Hashtable sets = new Hashtable();
         String index, indexSet, prop;
         StringBuffer sb = new StringBuffer("");
         StringTokenizer st;
         HashMap contextSets = new HashMap();
         String sortSet = dbProperties.getProperty("sortSet");
         while (enumer.hasMoreElements()) {
             prop = (String) enumer.nextElement();
             // MIH: extract contextSetName
             // compare with fieldNames in LuceneIndex
             // if fieldName starts with <name>. that is contained in contextSets
             // then this field belongs to a contextSet
             if (prop.startsWith("contextSet.")) {
                 contextSets.put(prop.substring(ELEVEN), "");
             }
             if (prop.startsWith("qualifier.")) {
                 st = new StringTokenizer(prop.substring(TEN));
                 index = st.nextToken();
                 st = new StringTokenizer(index, ".");
                 if (st.countTokens() == 1) {
                     indexSet = "local";
                 }
                 else {
                     indexSet = st.nextToken();
                 }
                 index = st.nextToken();
                 if (sets.get(indexSet) == null) { // new set
                     sets.put(indexSet, indexSet);
                 }
                 sb
                     .append("          <index>\n")
                     .append("            <title>").append(indexSet).append('.')
                     .append(index).append("</title>\n").append(
                         "            <map>\n").append(
                         "              <name set=\"").append(indexSet).append(
                         "\">").append(index).append("</name>\n").append(
                         "              </map>\n").append(
                         "            </index>\n");
             }
         }
         Collection fieldList =
             ((EscidocLuceneTranslator) getCQLTranslator()).getFieldList();
         indexSet = null;
         index = null;
         if (fieldList != null) {
             StringBuffer sortKeywords = new StringBuffer("");
             for (Iterator iter = fieldList.iterator(); iter.hasNext();) {
                 String fieldName = (String) iter.next();
                 String[] parts = fieldName.split("\\.");
                 if (parts != null && parts.length > 1) {
                     String indexName =
                         fieldName.replaceFirst(parts[0] + "\\.", "");
                     if (contextSets.get(parts[0]) != null) {
                         if (sets.get(parts[0]) == null) {
                             sets.put(parts[0], parts[0]);
                         }
                         // get title from properties
                         String title =
                             dbProperties
                                 .getProperty("description." + fieldName);
                         if (title == null || title.equals("")) {
                             title = fieldName;
                         }
                         sb
                             .append("          <index>\n").append(
                                 "            <title>").append(title).append(
                                 "</title>\n").append("            <map>\n")
                             .append("              <name set=\"").append(
                                 parts[0]).append("\">").append(indexName)
                             .append("</name>\n").append(
                                 "              </map>\n").append(
                                 "            </index>\n");
                     }
                     if (sortSet != null && !sortSet.equals("")
                         && sortSet.equals(parts[0])) {
                         sortKeywords.append("          <sortKeyword>").append(
                             fieldName).append("</sortKeyword>\n");
                     }
                 }
             }
             if (sortKeywords.length() > 0) {
                 sb.append(sortKeywords);
             }
         }
         if (sets != null && !sets.isEmpty()) {
             StringBuffer setsBuf = new StringBuffer("");
             for (Iterator iter = sets.keySet().iterator(); iter.hasNext();) {
                 String setName = (String) iter.next();
                 setsBuf.append("          <set identifier=\"").append(
                     dbProperties.getProperty("contextSet." + setName)).append(
                     "\" name=\"").append(setName).append("\"/>\n");
             }
             sb.insert(0, setsBuf);
         }
         sb.insert(0, "         <indexInfo>\n");
         sb.append("          </indexInfo>\n");
         return sb.toString();
     }
 
 }
