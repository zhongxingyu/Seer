 /**
  * JAFER Toolkit Poject.
  * Copyright (C) 2002, JAFER Toolkit Project, Oxford University.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package org.jafer.zserver.operations;
 
 import org.jafer.zserver.Session;
 import org.jafer.interfaces.Databean;
 import org.jafer.util.Config;
 import org.jafer.query.QueryException;
 import org.jafer.exception.JaferException;
 import org.jafer.zserver.operations.OperationException;
 
 import java.util.logging.Level;
 
 import org.w3c.dom.Node;
 
 import z3950.v3.*;
 import asn1.*;
 
 class Results {
     public String databaseName = null;
     public int noOfResults = 0;
     JaferException diagnostic = null;
 }
 
 /**
  * <p>Runs a Z39.50 search on database(s) - resultSets stored in associated Session.
  * Uses locking on resultSets (databeans) if client has requested concurrent operations.</p>
  * @author Antony Corfield; Matthew Dovey; Colin Tatham
  * @version 1.0
  */
 public class Search extends Operation {
 
   private PDU pduRequest;
   private PDU pduResponse;
 
   public Search(Session session, PDU pduRequest) {
 
     super(session, "search");
     this.pduRequest = pduRequest;
     this.pduResponse = new PDU();
   }
 
   public PDU runOp() throws Exception {
 
     pduResponse.c_searchResponse = new SearchResponse();
     pduResponse.c_searchResponse.s_referenceId = pduRequest.c_searchRequest.s_referenceId;
     pduResponse.c_searchResponse.s_resultCount = new ASN1Integer(0);
     pduResponse.c_searchResponse.s_searchStatus = new ASN1Boolean(false);
     pduResponse.c_searchResponse.s_nextResultSetPosition = new ASN1Integer(0);
     pduResponse.c_searchResponse.s_numberOfRecordsReturned = new ASN1Integer(0);
     pduResponse.c_searchResponse.s_presentStatus = new PresentStatus();
     pduResponse.c_searchResponse.s_presentStatus.value = new ASN1Integer(pduResponse.c_searchResponse.s_presentStatus.E_failure);
 
     String resultSetName;
     if (pduRequest.c_searchRequest.s_resultSetName == null)
       resultSetName = "default";
     else {
       resultSetName = pduRequest.c_searchRequest.s_resultSetName.value.get();
       if (getSession().containsDatabean(resultSetName) && !pduRequest.c_searchRequest.s_replaceIndicator.get()) {
         // diagnostic - resultSetName exists and replace = false
         throw new OperationException(
           getName() + " " + Config.getBib1Diagnostic(21) + " (" + " resultSetName: " + resultSetName + ")", getDiagnostic(21, null));
       }
     }
 
     String databases[] = new String[pduRequest.c_searchRequest.s_databaseNames.length];
     for (int n = 0; n < pduRequest.c_searchRequest.s_databaseNames.length; n++)
       databases[n] = pduRequest.c_searchRequest.s_databaseNames[n].value.value.get();
 
     Results[] results = search(pduRequest.c_searchRequest.s_query.c_type_1, databases, resultSetName);
 
     int total = 0;
 
     ASN1Any[] targets = new ASN1Any[results.length];
     for (int i=0; i<results.length; i++) {
       DatabaseName dbName = new DatabaseName();
       dbName.value = new InternationalString();
       dbName.value.value = new ASN1GeneralString(results[i].databaseName);
       ASN1Any[] details = new ASN1Any[3];
       details[0] = dbName;
       details[1] = new ASN1Integer(results[i].noOfResults);
      if (results[i].diagnostic != null) {
          if (results[i].diagnostic.hasDiagnostic()) {
              details[2] = new ASN1Integer(results[i].diagnostic.getDiagCondition());
          } else {
              details[2] = new ASN1Integer(2);
          }
       } else {
           details[2] = new ASN1Null();
       }
       // details[3] = resultsetname could go here
       targets[i] = new ASN1Sequence(details);
       total += results[i].noOfResults;
     }
     /** @todo check client Version supported before adding additionalSearchInfo ??
         do anything with resultSetName? */
     OtherInformation1 oi = new OtherInformation1();
     oi.s_information = new OtherInformation_information();
     oi.s_information.c_externallyDefinedInfo = new ASN1External();
     oi.s_information.c_externallyDefinedInfo.c_singleASN1type = new ASN1Sequence(targets);
 
     pduResponse.c_searchResponse.s_additionalSearchInfo = new OtherInformation();
     pduResponse.c_searchResponse.s_additionalSearchInfo.value = new OtherInformation1[]{oi};
     ///////////////////////////////////////////////////////////////////
 
     pduResponse.c_searchResponse.s_resultCount = new ASN1Integer(total);
     pduResponse.c_searchResponse.s_searchStatus = new ASN1Boolean(true);
     pduResponse.c_searchResponse.s_presentStatus.value = new ASN1Integer(pduResponse.c_searchResponse.s_presentStatus.E_success);
 
     return pduResponse;
   }
 
   private Results[] search(RPNQuery rpnQuery, String[] databases, String resultSetName) throws OperationException {
 
     // handles a type-1 query only
     if (pduRequest.c_searchRequest.s_query.c_type_1 == null) {
       throw new OperationException(
           getName() + " " + Config.getBib1Diagnostic(107), getDiagnostic(107, null));
     }
 
     org.jafer.interfaces.Search databean = null;
     try {
       databean = getDatabean(resultSetName);
     } catch (Exception ex) {
       // diagnostic - error obtaining bean
       throw new OperationException(
           getName() + " " + Config.getBib1Diagnostic(28) + "; " + ex.toString(), getDiagnostic(28, null), ex);
     }
 
     Results[] results = new Results[databases.length];
 
     try {
       databean.setDatabases(databases);
       databean.submitQuery(rpnQuery);
       for (int i=0; i<databases.length; i++) {
           results[i] = new Results();
           results[i].databaseName = databases[i];
           results[i].noOfResults = databean.getNumberOfResults(databases[i]);
           results[i].diagnostic = databean.getSearchException(databases[i]);
       }
     }  catch (JaferException ex) {
       // changed from 100 = (unspecified) error to getting info (if there is any) from JaferException
       throw new OperationException(
           getName() + " " + ex.getMessage(), getDiagnostic(ex.getDiagCondition(), ex.getAddInfo()), ex);
     }
 
     try {
       getSession().freeDatabean(resultSetName);
     } catch (JaferException ex) {
       // diagnostic - error obtaining bean
       throw new OperationException(
           getName() + " " + Config.getBib1Diagnostic(28) + "; " + ex.toString(), getDiagnostic(28, null), ex);
     }
 
     return results;
   }
 
   private org.jafer.interfaces.Search getDatabean(String resultSetName) throws OperationException {
 
     org.jafer.interfaces.Search databean = null;
     try {
       if (getSession().containsDatabean(resultSetName))
         databean = (org.jafer.interfaces.Search)getSession().getDatabean(resultSetName);
       else {
         databean = (org.jafer.interfaces.Search)getSession().getDatabean();
         getSession().setDatabean(resultSetName, databean);
       }
 
       getSession().lockDatabean(resultSetName);
     } catch (Exception ex) {
       // diagnostic - error obtaining bean
       throw new OperationException(
           getName() + " " + Config.getBib1Diagnostic(28) + "; " + ex.toString(), getDiagnostic(28, null), ex);
     }
     return databean;
   }
 
   public PDU getDiagnostic(int condition, String addInfo) {
 
     pduResponse.c_searchResponse.s_records = new Records();
     pduResponse.c_searchResponse.s_records.c_nonSurrogateDiagnostic = getDiagnostic(new DefaultDiagFormat(), condition, addInfo);
     pduResponse.c_searchResponse.s_searchStatus = new ASN1Boolean(false);
     pduResponse.c_searchResponse.s_resultSetStatus = new ASN1Integer(SearchResponse.E_none);
     return pduResponse;
   }
 }
