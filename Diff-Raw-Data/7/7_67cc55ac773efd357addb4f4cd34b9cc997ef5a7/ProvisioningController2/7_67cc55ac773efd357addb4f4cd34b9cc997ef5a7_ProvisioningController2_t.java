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
 
 package com.janrain.backplane2.server.provision;
 
 import com.janrain.backplane2.server.Grant;
 import com.janrain.backplane2.server.config.*;
 import com.janrain.backplane2.server.dao.DaoFactory;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.SuperSimpleDB;
 import com.janrain.commons.supersimpledb.message.AbstractMessage;
 import com.janrain.crypto.HmacHashUtils;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 import static com.janrain.backplane2.server.config.Backplane2Config.SimpleDBTables.*;
 
 /**
  * Controller handling the API calls for backplane customer configuration provisioning.
  *
  * @author Johnny Bufu
  */
 @Controller
 @RequestMapping(value="/v2/provision/*")
 @SuppressWarnings({"UnusedDeclaration"})
 public class ProvisioningController2 {
 
     // - PUBLIC
 
     @RequestMapping(value = "/bus/list", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, Map<String, String>> busList(@RequestBody ListRequest listRequest) throws AuthException {
         bpConfig.checkAdminAuth(listRequest.getAdmin(), listRequest.getSecret());
         return doList(bpConfig.getTableName(BP_BUS_CONFIG), BusConfig2.class, listRequest.getEntities());
     }
 
     @RequestMapping(value = "/user/list", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, Map<String, String>> userList(@RequestBody ListRequest listRequest) throws AuthException {
         bpConfig.checkAdminAuth(listRequest.getAdmin(), listRequest.getSecret());
         return doList(bpConfig.getTableName(BP_BUS_OWNERS), User.class, listRequest.getEntities());
     }
 
     @RequestMapping(value = "/client/list", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, Map<String, String>> clientList(@RequestBody ListRequest listRequest) throws AuthException {
         bpConfig.checkAdminAuth(listRequest.getAdmin(), listRequest.getSecret());
         return doList(bpConfig.getTableName(BP_CLIENTS), Client.class, listRequest.getEntities());
     }
 
     @RequestMapping(value = "/bus/delete", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> busDelete(@RequestBody ListRequest deleteRequest) throws AuthException {
         bpConfig.checkAdminAuth(deleteRequest.getAdmin(), deleteRequest.getSecret());
         return doDelete(bpConfig.getTableName(BP_BUS_CONFIG), BusConfig2.class, deleteRequest.getEntities());
     }
 
     @RequestMapping(value = "/user/delete", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> userDelete(@RequestBody ListRequest deleteRequest) throws AuthException {
         bpConfig.checkAdminAuth(deleteRequest.getAdmin(), deleteRequest.getSecret());
         return doDelete(bpConfig.getTableName(BP_BUS_OWNERS), User.class, deleteRequest.getEntities());
     }
 
     @RequestMapping(value = "/client/delete", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> clientDelete(@RequestBody ListRequest deleteRequest) throws AuthException {
         bpConfig.checkAdminAuth(deleteRequest.getAdmin(), deleteRequest.getSecret());
         return doDelete(bpConfig.getTableName(BP_CLIENTS), Client.class, deleteRequest.getEntities());
     }
 
     @RequestMapping(value = "/bus/update", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> busUpdate(@RequestBody BusUpdateRequest updateRequest) throws AuthException {
         return doUpdate(bpConfig.getTableName(BP_BUS_CONFIG), BusConfig2.class, updateRequest);
     }
 
     @RequestMapping(value = "/user/update", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> userUpdate(@RequestBody UserUpdateRequest updateRequest) throws AuthException {
         return doUpdate(bpConfig.getTableName(BP_BUS_OWNERS), User.class, updateRequest);
     }
 
     @RequestMapping(value = "/client/update", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> clientUpdate(@RequestBody ClientUpdateRequest updateRequest) throws AuthException {
         logger.debug("client updateRequest: '" + updateRequest + "'");
         return doUpdate(bpConfig.getTableName(BP_CLIENTS), Client.class, updateRequest);
     }
 
     @RequestMapping(value = "/grant/list", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, Map<String, String>> grantList(@RequestBody ListRequest listRequest) throws AuthException {
         bpConfig.checkAdminAuth(listRequest.getAdmin(), listRequest.getSecret());
 
         Map<String,Map<String,String>> result = new LinkedHashMap<String, Map<String, String>>();
 
         for(String clientId : listRequest.getEntities()) {
             try {
                 Map<String,String> grantsList = new HashMap<String, String>();
                 for(Grant grant : daoFactory.getGrantDao().retrieveGrants(clientId, null)) {
                     grantsList.put(grant.getIdValue(), grant.getBusesAsString());
                 }
                 result.put(clientId, grantsList);
             } catch (final SimpleDBException e) {
                 result.put(clientId, new HashMap<String, String>() {{put("error", e.getMessage());}});
             }
         }
         return result; 
     }
 
     @RequestMapping(value = "/grant/add", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> grantAdd(@RequestBody GrantRequest grantRequest) throws AuthException {
         logger.debug("grant add request: '" + grantRequest + "'");
         return doGrant(grantRequest, true);
     }
 
     @RequestMapping(value = "/grant/revoke", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> gantRevoke(@RequestBody GrantRequest grantRequest) throws AuthException {
         logger.debug("grant revoke request: '" + grantRequest + "'");
         return doGrant(grantRequest, false);
     }
 
     /**
      * Handle auth errors
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final AuthException e, HttpServletResponse response) {
         logger.error("Provisioning authentication error: " + e.getMessage());
         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
         return new HashMap<String,String>() {{
             put(ERR_MSG_FIELD, e.getMessage());
         }};
     }
 
     /**
      * Handle all other errors
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final Exception e, HttpServletResponse response) {
         logger.error("Error handling provisioning request", bpConfig.getDebugException(e));
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         return new HashMap<String,String>() {{
             put(ERR_MSG_FIELD, e.getMessage());
         }};
     }
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(ProvisioningController2.class);
 
     private static final String BACKPLANE_UPDATE_SUCCESS = "BACKPLANE_UPDATE_SUCCESS";
     private static final String BACKPLANE_DELETE_SUCCESS = "BACKPLANE_DELETE_SUCCESS";
    private static final String GRANT_UPDATE_SUCCESS = "GRANT_UPDATE_SUCCESS";
     private static final String BACKPLANE_ENTRY_NOT_FOUND = "BACKPLANE_ENTRY_NOT_FOUND";
     private static final String ERR_MSG_FIELD = "ERR_MSG";
     private static final String CONFIG_NOT_FOUND = "CONFIG_NOT_FOUND";
 
     @Inject
     private Backplane2Config bpConfig;
 
     @Inject
     private SuperSimpleDB superSimpleDb;
     
     @Inject
     private DaoFactory daoFactory;
             
 
     private <T extends AbstractMessage> Map<String, Map<String, String>> doList(String tableName, Class<T> entityType, List<String> entityNames) {
 
         if (entityNames.size() == 0) return doListAll(tableName, entityType);
 
         final Map<String,Map<String,String>> result = new LinkedHashMap<String, Map<String, String>>();
         for(String entityName : entityNames) {
             T config = null;
             Exception thrown = null;
             try {
                 config = superSimpleDb.retrieve(tableName, entityType, entityName);
             } catch (Exception e) {
                 thrown = e;
             }
             final String errMgs = thrown != null ? thrown.getMessage() : config == null ? CONFIG_NOT_FOUND : null;
 
             result.put(entityName,
                 errMgs != null ? new HashMap<String, String>() {{ put(ERR_MSG_FIELD, errMgs); }} :
                 config);
         }
         return result;
     }
 
     private <T extends AbstractMessage> Map<String, Map<String, String>> doListAll(String tableName, Class<T> entityType) {
         Map<String,Map<String,String>> result = new LinkedHashMap<String, Map<String, String>>();
         try {
             for(T config :  superSimpleDb.retrieve(tableName, entityType)) {
                 result.put(config.getIdValue(), config);
             }
         } catch (final Exception e) {
             result.put(ERR_MSG_FIELD, new HashMap<String, String>() {{ put(ERR_MSG_FIELD, e.getMessage()); }});
         }
         return result;
     }
 
     private <T extends AbstractMessage> Map<String, String> doDelete(String tableName, Class<T> entityType, List<String> entityNames) {
         Map<String,String> result = new LinkedHashMap<String, String>();
         for(String entityName : entityNames) {
             String deleteStatus = BACKPLANE_DELETE_SUCCESS;
             try {
                 if (superSimpleDb.retrieve(tableName, entityType, entityName) == null) {
                     deleteStatus = BACKPLANE_ENTRY_NOT_FOUND;
                 } else {
                     daoFactory.getDaoByObjectType(entityType).delete(entityName);
                 }
             } catch (Exception e) {
                 deleteStatus = e.getMessage();
             }
             result.put(entityName, deleteStatus);
         }
         return result;
     }
 
     private <T extends AbstractMessage> Map<String, String> doUpdate(String tableName, Class<T> entityType, UpdateRequest<T> updateRequest) throws AuthException {
         bpConfig.checkAdminAuth(updateRequest.getAdmin(), updateRequest.getSecret());
         validateConfigs(entityType, updateRequest);
         return updateConfigs(tableName, entityType, updateRequest.getConfigs());
     }
 
     private <T extends AbstractMessage> void validateConfigs(Class<T> entityType, UpdateRequest<T> updateRequest) {
         for(T config : updateRequest.getConfigs()) {
             config.validate();
         }
     }
 
     private <T extends AbstractMessage> Map<String, String> updateConfigs(String tableName, Class<T> customerConfigType, List<T> bpConfigs) {
         Map<String,String> result = new LinkedHashMap<String, String>();
         for(T config : bpConfigs) {
             if (config instanceof User) {
                 // hash the new user password
                 User user = (User) config;
                 user.put(User.Field.PWDHASH.getFieldName(), HmacHashUtils.hmacHash(user.get(User.Field.PWDHASH)));
             }
             String updateStatus = BACKPLANE_UPDATE_SUCCESS;
             try {
                 superSimpleDb.store(tableName, customerConfigType, config);
             } catch (Exception e) {
                 updateStatus = e.getMessage();
             }
             result.put(config.getIdValue(), updateStatus);
         }
         return result;
     }
 
     private Map<String, String> doGrant(GrantRequest grantRequest, boolean addRevoke) throws AuthException {
         Map<String,String> result = new LinkedHashMap<String, String>();
         bpConfig.checkAdminAuth(grantRequest.getAdmin(), grantRequest.getSecret());
         for(Map.Entry<String,String> newGrantEntry : grantRequest.getGrants().entrySet()) {
             String clientId = newGrantEntry.getKey();
             String buses = newGrantEntry.getValue();
             try {
                 if (null == daoFactory.getClientDAO().retrieveClient(clientId)) {
                     result.put(clientId, "invalid client_id");
                 } else {
                     if (addRevoke) {
                         addGrant(grantRequest.getAdmin(), clientId, buses);
                        result.put(clientId, "GRANT_UPDATE_SUCCESS");
                     } else {
                         daoFactory.getGrantDao().revokeBuses(clientId, buses);
                        result.put(clientId, "GRANT_UPDATE_SUCCESS");
                     }
                 }
             } catch (Exception e) {
                 result.put(clientId, e.getMessage());
             }
         }
         return result;
     }
 
     private void addGrant(String issuer, String clientId, String buses) throws SimpleDBException {
         Grant grant = new Grant(issuer, clientId, buses);
         grant.setCodeUsedNow();
         daoFactory.getGrantDao().persist(grant);
     }
 
     // type helper classes for JSON mapper
     private static class BusUpdateRequest extends UpdateRequest<BusConfig2> {}
     private static class UserUpdateRequest extends UpdateRequest<User> {}
     private static class ClientUpdateRequest extends UpdateRequest<Client> {}
 }
