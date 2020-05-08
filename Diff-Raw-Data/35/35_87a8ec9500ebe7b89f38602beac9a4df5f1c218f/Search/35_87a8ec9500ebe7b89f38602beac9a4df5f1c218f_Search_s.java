 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations
  * (FAO). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this
  * list of conditions and the following disclaimer. 2. Redistributions in binary
  * form must reproduce the above copyright notice,this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
  * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.boundary.ws;
 
 import java.util.List;
 import javax.annotation.Resource;
 import javax.ejb.EJB;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.xml.ws.WebServiceContext;
 import org.sola.services.boundary.transferobjects.search.*;
 import org.sola.services.common.ServiceConstants;
 import org.sola.services.common.contracts.GenericTranslator;
 import org.sola.services.common.faults.SOLAAccessFault;
 import org.sola.services.common.faults.SOLAFault;
 import org.sola.services.common.faults.UnhandledFault;
 import org.sola.services.common.webservices.AbstractWebService;
 import org.sola.services.ejb.search.businesslogic.SearchEJBLocal;
 import org.sola.services.ejb.search.repository.entities.*;
 import org.sola.services.ejb.search.spatial.QueryForSelect;
 import org.sola.services.ejb.search.spatial.ResultForSelectionInfo;
 
 @WebService(serviceName = "search-service", targetNamespace = ServiceConstants.SEARCH_WS_NAMESPACE)
 public class Search extends AbstractWebService {
 
     @EJB
     SearchEJBLocal searchEJB;
     @Resource
     private WebServiceContext wsContext;
 
     /**
      * Dummy method to check the web service instance is working
      */
     @WebMethod(operationName = "CheckConnection")
     public boolean CheckConnection() {
         return true;
     }
 
     @WebMethod(operationName = "VerifyApplicationProperty")
     public PropertyVerifierTO VerifyApplicationProperty(
            @WebParam(name = "firstPart") String firstPart,
            @WebParam(name = "lastPart") String lastPart) throws SOLAFault, UnhandledFault {
 
        final String firstPartTmp = firstPart;
        final String lastPartTmp = lastPart;
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
             @Override
             public void run() {
                 PropertyVerifier propertyVerifier =
                        searchEJB.getPropertyVerifier(firstPartTmp, lastPartTmp);
                 result[0] = GenericTranslator.toTO(
                         propertyVerifier, PropertyVerifierTO.class);
             }
         });
 
         return (PropertyVerifierTO) result[0];
     }
 
     @WebMethod(operationName = "GetUnassignedApplications")
     public List<ApplicationSearchResultTO> GetUnassignedApplications(
             @WebParam(name = "locale") String locale) throws SOLAFault, UnhandledFault {
 
         final String localeTmp = locale;
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 List<ApplicationSearchResult> appList = searchEJB.getUnassignedApplications(localeTmp);
                 result[0] = GenericTranslator.toTOList(
                         appList, ApplicationSearchResultTO.class);
             }
         });
 
         return (List<ApplicationSearchResultTO>) result[0];
     }
 
     @WebMethod(operationName = "GetAssignedApplications")
     public List<ApplicationSearchResultTO> GetAssignedApplications(@WebParam(name = "locale") String locale)
             throws SOLAFault, UnhandledFault {
 
         final String localeTmp = locale;
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 List<ApplicationSearchResult> appList = searchEJB.getAssignedApplications(localeTmp);
                 result[0] = GenericTranslator.toTOList(
                         appList, ApplicationSearchResultTO.class);
             }
         });
 
         return (List<ApplicationSearchResultTO>) result[0];
     }
 
     @WebMethod(operationName = "SearchApplications")
     public List<ApplicationSearchResultTO> SearchApplications(
             @WebParam(name = "ApplicationSearchParamsTO") ApplicationSearchParamsTO paramsTO)
             throws SOLAFault, UnhandledFault {
 
         final ApplicationSearchParamsTO paramsTOTmp = paramsTO;
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 ApplicationSearchParams params = GenericTranslator.fromTO(paramsTOTmp, ApplicationSearchParams.class, null);
                 List<ApplicationSearchResult> appList = searchEJB.searchApplications(params);
                 result[0] = GenericTranslator.toTOList(
                         appList, ApplicationSearchResultTO.class);
             }
         });
 
         return (List<ApplicationSearchResultTO>) result[0];
     }
 
     @WebMethod(operationName = "Select")
     public List<ResultForSelectionInfo> Select(List<QueryForSelect> queries)
             throws SOLAFault, UnhandledFault {
 
         final List<QueryForSelect> queriesTmp = queries;
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 result[0] = searchEJB.getSpatialResultFromSelection(queriesTmp);
             }
         });
 
         return (List<ResultForSelectionInfo>) result[0];
     }
 
     @WebMethod(operationName = "SearchParties")
     public List<PartySearchResultTO> searchParties(
             @WebParam(name = "searchParams") PartySearchParamsTO searchParams)
             throws SOLAFault, UnhandledFault {
 
         final PartySearchParamsTO searchParamsTmp = searchParams;
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 PartySearchParams params = GenericTranslator.fromTO(searchParamsTmp,
                         PartySearchParams.class, null);
                 result[0] = GenericTranslator.toTOList(
                         searchEJB.searchParties(params), PartySearchResultTO.class);
             }
         });
 
         return (List<PartySearchResultTO>) result[0];
     }
 
     @WebMethod(operationName = "SearchSources")
     public List<SourceSearchResultTO> searchSources(
             @WebParam(name = "searchParams") SourceSearchParamsTO searchParams)
             throws SOLAFault, UnhandledFault {
 
         final SourceSearchParamsTO searchParamsTmp = searchParams;
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 SourceSearchParams params = GenericTranslator.fromTO(searchParamsTmp,
                         SourceSearchParams.class, null);
                 result[0] = GenericTranslator.toTOList(
                         searchEJB.searchSources(params), SourceSearchResultTO.class);
 
             }
         });
 
         return (List<SourceSearchResultTO>) result[0];
     }
 
     @WebMethod(operationName = "GetActiveUsers")
     public List<UserSearchResultTO> getActiveUsers() throws SOLAFault, UnhandledFault {
 
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 List<UserSearchResult> activeUsers = searchEJB.getActiveUsers();
                 result[0] = GenericTranslator.toTOList(activeUsers, UserSearchResultTO.class);
 
             }
         });
 
         return (List<UserSearchResultTO>) result[0];
     }
 
     @WebMethod(operationName = "searchUsers")
     public List<UserSearchAdvancedResultTO> searchUsers(@WebParam(name = "searchParams") UserSearchParamsTO searchParams) throws SOLAFault, UnhandledFault {
 
         final UserSearchParamsTO searchParamsTmp = searchParams;
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 UserSearchParams params = GenericTranslator.fromTO(searchParamsTmp,
                         UserSearchParams.class, null);
                 List<UserSearchResult> users = searchEJB.searchUsers(params);
                 result[0] = GenericTranslator.toTOList(users, UserSearchAdvancedResultTO.class);
 
             }
         });
 
         return (List<UserSearchAdvancedResultTO>) result[0];
     }
 
     @WebMethod(operationName = "getApplicationLog")
     public List<ApplicationLogResultTO> getApplicationLog(@WebParam(name = "applicationId") String applicationId) throws SOLAFault, UnhandledFault {
 
         final String applicationIdTmp = applicationId;
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 List<ApplicationLogResult> log = searchEJB.getApplicationLog(applicationIdTmp);
                 result[0] = GenericTranslator.toTOList(log, ApplicationLogResultTO.class);
 
             }
         });
 
         return (List<ApplicationLogResultTO>) result[0];
     }
 
     @WebMethod(operationName = "SearchBr")
     public List<BrSearchResultTO> searchBr(@WebParam(name = "searchParams") BrSearchParamsTO searchParams,
             @WebParam(name = "lang") String lang)
             throws SOLAFault, UnhandledFault, SOLAAccessFault {
 
         final Object[] params = {searchParams, lang};
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 BrSearchParamsTO searchParams = (BrSearchParamsTO) params[0];
                 String lang = params[1] == null ? null : params[1].toString();
                 if (searchParams != null) {
                     result[0] = GenericTranslator.toTOList(
                             searchEJB.searchBr(GenericTranslator.fromTO(searchParams, BrSearchParams.class, null), lang),
                             BrSearchResultTO.class);
                 }
             }
         });
 
         return (List<BrSearchResultTO>) result[0];
     }
 
     @WebMethod(operationName = "searchBaUnit")
     public List<BaUnitSearchResultTO> searchBaUnit(@WebParam(name = "searchParams") BaUnitSearchParamsTO searchParams)
             throws SOLAFault, UnhandledFault, SOLAAccessFault {
 
         final Object[] params = {searchParams};
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 BaUnitSearchParamsTO searchParams = (BaUnitSearchParamsTO) params[0];
                 if (searchParams != null) {
                     result[0] = GenericTranslator.toTOList(
                             searchEJB.searchBaUnits(GenericTranslator.fromTO(
                             searchParams, BaUnitSearchParams.class, null)),
                             BaUnitSearchResultTO.class);
                 }
             }
         });
 
         return (List<BaUnitSearchResultTO>) result[0];
     }
 
     @WebMethod(operationName = "SearchCadastreObjects")
     public List<CadastreObjectSearchResultTO> SearchCadastreObjects(
             @WebParam(name = "searchBy") String searchBy,
             @WebParam(name = "searchString") String searchString)
             throws SOLAFault, UnhandledFault, SOLAAccessFault {
 
         final String searchByTmp = searchBy;
         final String searchStringTmp = searchString;
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 result[0] = GenericTranslator.toTOList(
                         searchEJB.searchCadastreObjects(searchByTmp, searchStringTmp),
                         CadastreObjectSearchResultTO.class);
             }
         });
 
         return (List<CadastreObjectSearchResultTO>) result[0];
     }
 
     @WebMethod(operationName = "Test")
     public GenericResult Test(@WebParam(name = "lang") String lang)
             throws SOLAFault, UnhandledFault, SOLAAccessFault {
 
         final String langTmp = lang;
         final Object[] result = {null};
 
         runGeneralMethod(wsContext, new Runnable() {
 
             @Override
             public void run() {
                 result[0] = searchEJB.getGenericResultList("test", null);
             }
         });
 
         return (GenericResult) result[0];
     }
 }
