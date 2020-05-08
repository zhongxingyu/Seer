 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane2.server.dao;
 
 import com.janrain.backplane2.server.BackplaneServerException;
 import com.janrain.backplane2.server.Grant;
 import com.janrain.backplane2.server.Scope;
 import com.janrain.backplane2.server.config.Backplane2Config;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.SuperSimpleDB;
 import com.janrain.oauth2.TokenException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.janrain.backplane2.server.config.Backplane2Config.SimpleDBTables.BP_GRANT;
 
 /**
  * @author Tom Raney
  */
 
 public class GrantDAO extends DAO {
 
     GrantDAO(SuperSimpleDB superSimpleDB, Backplane2Config bpConfig) {
         super(superSimpleDB, bpConfig);
     }
 
     @Override
     public void persist(Object grant) throws SimpleDBException {
         superSimpleDB.store(bpConfig.getTableName(BP_GRANT), Grant.class, (Grant) grant, true);
     }
 
     @Override
     public void delete(String id) throws SimpleDBException {
         superSimpleDB.delete(bpConfig.getTableName(BP_GRANT), id);
         logger.info("Deleted grant " + id);
     }
 
     public Grant retrieveGrant(String grantId) throws SimpleDBException {
         return superSimpleDB.retrieve(bpConfig.getTableName(BP_GRANT), Grant.class, grantId);
     }
 
     /**
      * Retrieve grant from simpleDB, mark code used, return grant.
      *
      * If code re-used: delete grant, log error, return null.
      */
     public Grant getGrantSetCodeUsed(String codeId) throws SimpleDBException {
         try {
             Grant existing = superSimpleDB.retrieve(bpConfig.getTableName(BP_GRANT), Grant.class, codeId);
             if (existing != null) {
                 Grant updated = new Grant(existing);
                 updated.setCodeUsedNow();
                 superSimpleDB.update(bpConfig.getTableName(BP_GRANT), Grant.class, existing, updated);
                 logger.info("marked authorization code: " + codeId + " as used");
                 return updated;
             }
             logger.warn("No grant found for code: " + codeId);
         } catch (SimpleDBException e) {
             // delete grant to invalidate all tokens backed by this grant if code was already used
             // todo: actually delete all issued tokens?
             Grant existing = superSimpleDB.retrieve(bpConfig.getTableName(BP_GRANT), Grant.class, codeId);
             if (existing.isCodeUsed()) {
                 logger.error( "Authorization code: " + codeId + " already used at " + existing.getCodeUsedDate() +
                               ", deleting grant to invalidate all related tokens");
                 delete(codeId);
             }
         }
         return null;
     }
 
     /**
      * Retrieve a list of grants that encompass the buses requested by a client
      * @param clientId
      * @param scope
      * @return
      * @throws SimpleDBException
      */
 
     public List<Grant> retrieveGrants(String clientId, @Nullable Scope scope) throws SimpleDBException {
         List<Grant> allGrants = superSimpleDB.retrieveWhere(bpConfig.getTableName(BP_GRANT), Grant.class,
                 "issued_to_client='"+ clientId + "' AND date_code_used is not null", true);
 
         // if no buses exist in requested scope, return entire list
         if (scope == null || scope.getBusesInScope().isEmpty()) {
             return allGrants;
         }
 
         ArrayList<Grant> selectedGrants = new ArrayList<Grant>();
         for ( String bus: scope.getBusesInScope()) {
             for ( Grant grant : allGrants) {
                 if ( grant.getBusesAsList().contains(bus) && ! selectedGrants.contains(grant)) {
                     selectedGrants.add(grant);
                 }
             }
         }
 
         return selectedGrants;
     }
 
     /**
      *  Revokes buses across all grants that contain them.
      *  Not atomic, best effort.
      *  Stops on first error and reports error, even though some grants may have been updated.
      */
     public void revokeBuses(String clientId, String buses) throws SimpleDBException, BackplaneServerException, TokenException {
         Scope busesToRevoke = new Scope(Scope.convertToBusScope(buses));
        List<Grant> grants = retrieveGrants(clientId, busesToRevoke);
        if (grants == null || grants.isEmpty()) {
            throw new BackplaneServerException("No grants found to revoke for buses: " + buses);
        }
        for(Grant grant : grants) {
             Grant updated = new Grant(grant);
             String remainingBuses = updated.revokeBuses(busesToRevoke.getBusesInScope());
             if(StringUtils.isEmpty(remainingBuses)) {
                 delete(grant.getIdValue());
             } else {
                 superSimpleDB.update(bpConfig.getTableName(BP_GRANT), Grant.class, grant, updated);
                 logger.info("Buses updated updated for grant: " + updated.getIdValue() + " to '" + remainingBuses + "'");
             }
         }
     }
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(GrantDAO.class);
 
 }
