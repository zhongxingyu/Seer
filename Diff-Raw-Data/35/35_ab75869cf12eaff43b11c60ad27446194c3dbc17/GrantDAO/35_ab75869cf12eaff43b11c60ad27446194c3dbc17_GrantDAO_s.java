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
 
 import com.janrain.backplane2.server.*;
 import com.janrain.backplane2.server.config.Backplane2Config;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.SuperSimpleDB;
 import com.janrain.oauth2.TokenException;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.TimerMetric;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.*;
 import java.util.concurrent.Callable;
 import java.util.concurrent.TimeUnit;
 
 import static com.janrain.backplane2.server.config.Backplane2Config.SimpleDBTables.BP_GRANT;
 
 /**
  * @author Tom Raney
  */
 
 public class GrantDAO extends DAO<Grant> {
 
     GrantDAO(SuperSimpleDB superSimpleDB, Backplane2Config bpConfig, DaoFactory daoFactory) {
         super(superSimpleDB, bpConfig);
         this.daoFactory = daoFactory;
     }
 
     @Override
     public void persist(Grant grant) throws SimpleDBException {
         superSimpleDB.store(bpConfig.getTableName(BP_GRANT), Grant.class, grant, true);
     }
 
     /** Tokens issued against the deleted grant will also be revoked/deleted */
     @Override
     public void delete(String id) throws SimpleDBException {
         daoFactory.getTokenDao().revokeTokenByGrant(id);
         superSimpleDB.delete(bpConfig.getTableName(BP_GRANT), id);
         logger.info("Deleted grant (and revoked tokens): " + id);
     }
 
     private void update(Grant grant, Grant updated) throws SimpleDBException {
         daoFactory.getTokenDao().revokeTokenByGrant(updated.getIdValue());
         superSimpleDB.update(bpConfig.getTableName(BP_GRANT), Grant.class, grant, updated);
         logger.info("Updated grant (and revoked tokens): " + updated.getIdValue());
     }
 
     public Grant retrieveGrant(String grantId) throws SimpleDBException {
         return superSimpleDB.retrieve(bpConfig.getTableName(BP_GRANT), Grant.class, grantId);
     }
 
     /**
      * Activate authorization_code grant, mark code used, return updated grant.
      *
      * @return the updated grant, never null
      *
      * @throws TokenException if:
      *          1) no grant was found for the supplied code, or
      *          2) the grant found was not an authorization_code grant, or
      *          3) the grant was not issued  to the supplied authenticatedClientId, or
      *          4) the grant/grant code has expired, or
      *          5) the grant's code had already been used (in this case the grant is also invalidated/revoked), or
      *
      * @throws SimpleDBException on any error while interacting with SimpleDB
      */
     public Grant getAndActivateCodeGrant(final String codeId, String authenticatedClientId) throws SimpleDBException, TokenException {
 
         Grant existing;
         try {
             existing = v2grantActivateTimer.time(new Callable<Grant>() {
                     @Override
                     public Grant call() throws Exception {
                         return superSimpleDB.retrieve(bpConfig.getTableName(BP_GRANT), Grant.class, codeId);
                     }
                 });
         } catch (SimpleDBException sdbe) {
             throw sdbe;
         } catch (Exception e) {
             throw new SimpleDBException(e);
         }
 
         GrantState updatedState = GrantState.ACTIVE;
         if ( existing == null) {
             logger.warn("No grant found for code: " + codeId);
             throw new TokenException("Invalid code: " + codeId);
         } else if ( GrantType.AUTHORIZATION_CODE != existing.getType() || existing.isExpired() ||
                     StringUtils.isBlank(authenticatedClientId) ||
                     ! authenticatedClientId.equals(existing.get(Grant.GrantField.ISSUED_TO_CLIENT_ID)) ||
                     GrantState.INACTIVE != existing.getState()) {
             logger.error("Invalid grant for code: " + codeId);
             updatedState = GrantState.REVOKED;
         }
 
         Grant updated = new Grant.Builder(existing, updatedState).buildGrant(); // no expiration
         superSimpleDB.update(bpConfig.getTableName(BP_GRANT), Grant.class, existing, updated);
 
         logger.info( "Grant status: " + updatedState.toString().toLowerCase() + " for code: " + codeId + ", client_id: " + authenticatedClientId);
 
         if (updatedState.isActive()) {
             return updated;
         } else {
             throw new TokenException("Invalid code: " + codeId);
         }
     }
 
     public void deleteByBuses(@NotNull List<String> busesToDelete) throws SimpleDBException, TokenException {
         // todo: consider multiple 'select .. like .. ' queries/clauses
         Scope deleteBusesScope = new Scope(Scope.getEncodedScopesAsString(BackplaneMessage.Field.BUS, busesToDelete));
         for(Grant grant : superSimpleDB.retrieveAll(bpConfig.getTableName(BP_GRANT), Grant.class)) {
             Set<String> grantBuses = grant.getAuthorizedScope().getScopeFieldValues(BackplaneMessage.Field.BUS);
             if (grantBuses == null) continue;
             for(String bus : grantBuses) {
                 if (busesToDelete.contains(bus)) {
                     revokeBuses(grant, deleteBusesScope);
                 }
             }
         }
     }
 
     /**
      * Retrieve all active grants issued to the supplied clientId
      * that match any of the fields that require authorization in the supplied scope.
      *
      * If none of the fields in the supplied scope require authorization, or if the supplied scope is null,
      * returns all grants issued to clientId, keyed on a single map entry representing the full scope
      * that includes the filter-only, no-authorization-required supplied scope.
      *
      * @return  map of authorized scopes backed-by grants
      * @throws SimpleDBException
      */
     public @NotNull Map<Scope,Set<Grant>> retrieveClientGrants(final String clientId, @Nullable Scope scope) throws SimpleDBException, TokenException {
 
         List<Grant> clientActiveGrants;
         try {
             clientActiveGrants = v2grantClientsTimer.time(new Callable<List<Grant>>() {
                 @Override
                 public List<Grant> call() throws Exception {
                     return superSimpleDB.retrieveWhere(bpConfig.getTableName(BP_GRANT), Grant.class,
                             Grant.GrantField.ISSUED_TO_CLIENT_ID.getFieldName() + "='" + clientId + "' AND " +
                             Grant.GrantField.STATE.getFieldName() + "='" + GrantState.ACTIVE.toString() + "'", true);
                 }
             });
         } catch (SimpleDBException sdbe) {
             throw sdbe;
         } catch (Exception e) {
             throw new SimpleDBException(e);
         }
 
         Map<Scope,Set<Grant>> result = new LinkedHashMap<Scope, Set<Grant>>();
 
         if (scope == null || ! scope.isAuthorizationRequired()) {
             Set<Grant> selectedGrants = new LinkedHashSet<Grant>();
             Map<BackplaneMessage.Field,LinkedHashSet<String>> authorizedScopesMap = new LinkedHashMap<BackplaneMessage.Field, LinkedHashSet<String>>();
             for (Grant grant : clientActiveGrants) {
                 if (grant.isExpired()) continue;
                 selectedGrants.add(grant);
                 Scope.addScopes(authorizedScopesMap, grant.getAuthorizedScope().getScopeMap());
             }
             if (scope != null) {
                 Scope.addScopes(authorizedScopesMap, scope.getScopeMap()); // add request filter-only scopes
             }
             result.put(new Scope(authorizedScopesMap), selectedGrants);
         } else {
             for(Scope authReqScope : scope.getAuthReqScopes()) {
                 final Scope testScope;
                 if (authReqScope.getScopeMap().containsKey(BackplaneMessage.Field.CHANNEL)) {
                     String channel = authReqScope.getScopeMap().get(BackplaneMessage.Field.CHANNEL).iterator().next();
                     testScope = new Scope(BackplaneMessage.Field.BUS, daoFactory.getTokenDao().getBusForChannel(channel));
                 } else {
                     testScope = authReqScope;
                 }
                 for(Grant grant : clientActiveGrants) {
                     if (grant.isExpired()) continue;
                     if (grant.getAuthorizedScope().containsScope(testScope)) {
                         Set<Grant> backingGrants = result.get(authReqScope);
                         if (backingGrants == null) {
                             backingGrants = new LinkedHashSet<Grant>();
                             result.put(authReqScope, backingGrants);
                         }
                         backingGrants.add(grant);
                     }
                 }
             }
         }
 
         return result;
     }
 
     /**
      *  Revokes buses across the provided grants.
      *  Not atomic, best effort.
      *  Stops on first error and reports error, even though some grants may have been updated.
      */
    public void revokeBuses(Set<Grant> grants, String buses) throws SimpleDBException, BackplaneServerException, TokenException {
         Scope busesToRevoke = new Scope(Scope.getEncodedScopesAsString(BackplaneMessage.Field.BUS, buses));
         boolean changes = false;
         for (Grant grant : grants) {
            changes = changes || revokeBuses(grant, busesToRevoke);
        }
        if (!changes) {
            throw new BackplaneServerException("No grants found to revoke for buses: " + buses);
         }
     }
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(GrantDAO.class);
 
     private final DaoFactory daoFactory;
 
     private final TimerMetric v2grantActivateTimer = Metrics.newTimer(GrantDAO.class, "v2_sdb_grant_activate", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
     private final TimerMetric v2grantClientsTimer = Metrics.newTimer(GrantDAO.class, "v2_sdb_grant_clients", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
     private boolean revokeBuses(Grant grant, Scope busesToRevoke) throws SimpleDBException {
         Scope grantScope = grant.getAuthorizedScope();
         Scope updatedScope = Scope.revoke(grantScope, busesToRevoke);
         if (updatedScope.equals(grantScope)) return false;
         if (!updatedScope.isAuthorizationRequired()) {
             logger.info("Revoked all buses from grant: " + grant.getIdValue());
             delete(grant.getIdValue());
         } else {
             Grant updated = new Grant.Builder(grant, grant.getState()).scope(updatedScope).buildGrant();
             update(grant, updated);
             logger.info("Buses updated updated for grant: " + updated.getIdValue() + " remaining scope: '" + updated.getAuthorizedScope() + "'");
         }
         return true;
     }
 }
