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
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.boundary.wsclients;
 
 import java.util.List;
 import org.sola.services.boundary.wsclients.exception.WebServiceClientException;
 import org.sola.webservices.search.GenericResult;
 import org.sola.webservices.search.QueryForSelect;
 import org.sola.webservices.search.ResultForSelectionInfo;
 import org.sola.webservices.transferobjects.search.ApplicationSearchParamsTO;
 import org.sola.webservices.transferobjects.search.ApplicationSearchResultTO;
 import org.sola.webservices.transferobjects.search.PartySearchParamsTO;
 import org.sola.webservices.transferobjects.search.PartySearchResultTO;
 import org.sola.webservices.transferobjects.search.PropertyVerifierTO;
 import org.sola.webservices.transferobjects.search.SourceSearchParamsTO;
 import org.sola.webservices.transferobjects.search.SourceSearchResultTO;
 import org.sola.webservices.transferobjects.search.UserSearchAdvancedResultTO;
 import org.sola.webservices.transferobjects.search.UserSearchParamsTO;
 import org.sola.webservices.transferobjects.search.UserSearchResultTO;
 import org.sola.webservices.transferobjects.search.ApplicationLogResultTO;
 import org.sola.webservices.transferobjects.search.BaUnitSearchParamsTO;
 import org.sola.webservices.transferobjects.search.BaUnitSearchResultTO;
 import org.sola.webservices.transferobjects.search.BrSearchParamsTO;
 import org.sola.webservices.transferobjects.search.BrSearchResultTO;
 import org.sola.webservices.transferobjects.search.CadastreObjectSearchResultTO;
 
 /**
  * Interface for the Search Service. Implemented by {@linkplain SearchClientImpl}
  * and {@linkplain mock.MockSearchClient}
  * @author amcdowell
  */
 public interface SearchClient extends AbstractWSClient {
 
     boolean checkConnection() throws WebServiceClientException;
 
     List<ApplicationSearchResultTO> getAssignedApplications() throws WebServiceClientException;
 
     List<ApplicationSearchResultTO> getUnassignedApplications() throws WebServiceClientException;
 
     List<ApplicationSearchResultTO> searchApplications(ApplicationSearchParamsTO applicationSearchParamsTO) throws WebServiceClientException;
 
    PropertyVerifierTO verifyApplicationProperty(String firstPart, String lastPart) throws WebServiceClientException;
 
     public List<ResultForSelectionInfo> select(List<QueryForSelect> queries)
             throws WebServiceClientException;
 
     List<PartySearchResultTO> searchParties(PartySearchParamsTO searchParams) throws WebServiceClientException;
 
     List<SourceSearchResultTO> searchSources(SourceSearchParamsTO searchParams) throws WebServiceClientException;
 
     List<UserSearchResultTO> getActiveUsers() throws WebServiceClientException;
 
     List<UserSearchAdvancedResultTO> searchUsers(UserSearchParamsTO searchParams) throws WebServiceClientException;
 
     List<ApplicationLogResultTO> getApplicationLog(String applicationId); 
     
     List<BrSearchResultTO> searchBr(BrSearchParamsTO searchParams) throws WebServiceClientException;
     
     List<BaUnitSearchResultTO> searchBaUnit(BaUnitSearchParamsTO searchParams) throws WebServiceClientException;
     
     List<CadastreObjectSearchResultTO> searchCadastreObjects(
             String searchBy, String searchString) throws WebServiceClientException;
     
     GenericResult test() throws WebServiceClientException;
 }
