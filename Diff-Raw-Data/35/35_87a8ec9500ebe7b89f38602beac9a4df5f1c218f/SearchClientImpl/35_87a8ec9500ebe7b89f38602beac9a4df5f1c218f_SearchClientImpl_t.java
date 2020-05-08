 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.boundary.wsclients;
 
 import java.util.List;
 import javax.xml.namespace.QName;
 import org.sola.services.boundary.wsclients.exception.WebServiceClientException;
 import org.sola.services.boundary.wsclients.exception.WebServiceClientExceptionType;
 import org.sola.webservices.search.GenericResult;
 import org.sola.webservices.search.QueryForSelect;
 import org.sola.webservices.search.ResultForSelectionInfo;
 import org.sola.webservices.search.SOLAFault;
 import org.sola.webservices.search.Search;
 import org.sola.webservices.search.SearchService;
 import org.sola.webservices.search.UnhandledFault;
 import org.sola.webservices.transferobjects.search.BaUnitSearchParamsTO;
 import org.sola.webservices.transferobjects.search.BaUnitSearchResultTO;
 import org.sola.webservices.transferobjects.search.BrSearchParamsTO;
 import org.sola.webservices.transferobjects.search.BrSearchResultTO;
 import org.sola.webservices.transferobjects.search.PropertyVerifierTO;
 import org.sola.webservices.transferobjects.search.ApplicationSearchParamsTO;
 import org.sola.webservices.transferobjects.search.ApplicationSearchResultTO;
 import org.sola.webservices.transferobjects.search.PartySearchParamsTO;
 import org.sola.webservices.transferobjects.search.PartySearchResultTO;
 import org.sola.webservices.transferobjects.search.SourceSearchParamsTO;
 import org.sola.webservices.transferobjects.search.SourceSearchResultTO;
 import org.sola.webservices.transferobjects.search.UserSearchAdvancedResultTO;
 import org.sola.webservices.transferobjects.search.UserSearchParamsTO;
 import org.sola.webservices.transferobjects.search.UserSearchResultTO;
 import org.sola.webservices.transferobjects.search.ApplicationLogResultTO;
 import org.sola.webservices.transferobjects.search.CadastreObjectSearchResultTO;
 
 /**
  * Implementation class for the {@linkplain SearchClient} interface. 
  * @author amcdowell
  */
 public class SearchClientImpl extends AbstractWSClientImpl implements SearchClient {
 
     private static final String NAMESPACE_URI = "http://webservices.sola.org/search";
     private static final String LOCAL_PART = "search-service";
     private static final String SERVICE_NAME = "Search.";
 
     public SearchClientImpl(String url) {
         super(url, new QName(NAMESPACE_URI, LOCAL_PART));
     }
 
     private Search getPort() {
         return getPort(Search.class, SearchService.class);
     }
 
     @Override
     public boolean checkConnection() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "checkConnection";
         try {
             boolean result = getPort().checkConnection();
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return  false;
         }
     }
 
     @Override
    public PropertyVerifierTO verifyApplicationProperty(String applicationNumber, String firstPart, String lastPart)
             throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "verifyApplicationProperty";
         try {
            PropertyVerifierTO result = getPort().verifyApplicationProperty(applicationNumber, firstPart, lastPart);
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     @Override
     public List<ApplicationSearchResultTO> getUnassignedApplications() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "getUnassignedApplications";
         try {
             List<ApplicationSearchResultTO> result = getPort().getUnassignedApplications(getLanguageCode());
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     @Override
     public List<ApplicationSearchResultTO> getAssignedApplications() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "getAssignedApplications";
         try {
             List<ApplicationSearchResultTO> result = getPort().getAssignedApplications(getLanguageCode());
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     @Override
     public List<ApplicationSearchResultTO> searchApplications(ApplicationSearchParamsTO applicationSearchParamsTO)
             throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "searchApplications";
         try {
             if (applicationSearchParamsTO.getLocale() == null) {
                 applicationSearchParamsTO.setLocale(getLanguageCode());
             }
             List<ApplicationSearchResultTO> result = getPort().searchApplications(applicationSearchParamsTO);
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     @Override
     public List<ResultForSelectionInfo> select(List<QueryForSelect> queries)
             throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "select";
         try {
             for(QueryForSelect query:queries){
                 query.setLocale(this.getLanguageCode());
             }
             List<ResultForSelectionInfo> result = getPort().select(queries);
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     @Override
     public List<PartySearchResultTO> searchParties(PartySearchParamsTO searchParams)
             throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "searchParties";
         try {
             return getPort().searchParties(searchParams);
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     @Override
     public List<SourceSearchResultTO> searchSources(SourceSearchParamsTO searchParams) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "searchSources";
         try {
             if (searchParams.getLocale() == null) {
                 searchParams.setLocale(getLanguageCode());
             }
             return getPort().searchSources(searchParams);
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     @Override
     public List<UserSearchResultTO> getActiveUsers() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "getActiveUsers";
         try {
             List<UserSearchResultTO> result = getPort().getActiveUsers();
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
     
     @Override
     public List<UserSearchAdvancedResultTO> searchUsers(UserSearchParamsTO searchParams) 
             throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "searchUsers";
         try {
             List<UserSearchAdvancedResultTO> result = getPort().searchUsers(searchParams);
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
     
     @Override
     public List<ApplicationLogResultTO> getApplicationLog(String applicationId) 
             throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "getApplicationLog";
         try {
             List<ApplicationLogResultTO> result = getPort().getApplicationLog(applicationId);
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     @Override
     public List<BrSearchResultTO> searchBr(BrSearchParamsTO searchParams) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "searchBr";
         try {
             return getPort().searchBr(searchParams, getLanguageCode());
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     @Override
     public List<CadastreObjectSearchResultTO> searchCadastreObjects(
             String searchBy, String searchString) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "searchCadastreObjects";
         try {
             return getPort().searchCadastreObjects(searchBy, searchString);
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     public GenericResult test() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "test";
         try {
             return getPort().test(getLanguageCode());
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     @Override
     public List<BaUnitSearchResultTO> searchBaUnit(BaUnitSearchParamsTO searchParams) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "searchBaUnit";
         try {
             return getPort().searchBaUnit(searchParams);
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 }
