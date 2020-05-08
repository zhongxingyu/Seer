 /*
  * Copyright (c) Members of the EGEE Collaboration. 2004. 
  * See http://www.eu-egee.org/partners/ for details on the copyright
  * holders.  
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *     http://www.apache.org/licenses/LICENSE-2.0 
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 package org.glite.ce.creamapi.delegationmanagement;
 
 import java.util.Calendar;
 import java.util.List;
 
 public interface DelegationManagerInterface {
     public static final String DELEGATION_DATASOURCE_NAME = "datasource_delegationdb";
     public static final String DELEGATION_TABLE = "DELEGATION";
     public static final String DELEGATION_REQUEST_TABLE = "DELEGATION_REQUEST";
 
     public void delete(Delegation delegation) throws DelegationException, DelegationManagerException;
 
     public void delete(DelegationRequest delegationRequst) throws DelegationException, DelegationManagerException;
 
     public int deleteDelegationRequestsToDate(Calendar timestamp) throws DelegationException, DelegationManagerException;
 
     public Delegation getDelegation(String delegationId, String dn, String localUser) throws DelegationException, DelegationManagerException;
 
     public DelegationRequest getDelegationRequest(String reqId, String dn, String localUser) throws DelegationException, DelegationManagerException;
 
     public List<DelegationRequest> getDelegationRequests(String dn, String localUser) throws DelegationException, DelegationManagerException;
 
     public List<Delegation> getDelegations(Calendar expirationTime) throws DelegationException, DelegationManagerException;
 
 //    public List<DelegationRequest> getExpiredDelegationRequests() throws DelegationException, DelegationManagerException;
 
     public List<Delegation> getDelegations(String dn, String localUser) throws DelegationException, DelegationManagerException;
 
     /**
      * This method returns the delegation suffix of the database identified by
      * the datasource parameter.
      * 
      * @param datasourceName
      *            the datasource of the database
      * @return DelegationSuffix the delegation suffix. If an exception occurs,
      *         the method returns null.
      */
     public String getDelegationSuffix() throws DelegationException, DelegationManagerException;
 
     public List<Delegation> getExpiredDelegations() throws DelegationException, DelegationManagerException;
 
     public void insert(Delegation delegation) throws DelegationException, DelegationManagerException;
 
     public void insert(DelegationRequest delegationRequst) throws DelegationException, DelegationManagerException;
     
     public void terminate();
 
     public void update(Delegation delegation) throws DelegationException, DelegationManagerException;
 
     public void update(DelegationRequest delegationRequest) throws DelegationException, DelegationManagerException;
 }
