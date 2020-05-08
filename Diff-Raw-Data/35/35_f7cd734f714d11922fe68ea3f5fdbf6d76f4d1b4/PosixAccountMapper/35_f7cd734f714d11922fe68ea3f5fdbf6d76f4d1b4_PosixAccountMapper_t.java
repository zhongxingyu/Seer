 /*
  * Copyright 2008 EGEE Collaboration
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.glite.authz.common.obligation.provider.gridmap.posix;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.glite.authz.common.obligation.provider.gridmap.AccountMapper;
 import org.glite.authz.common.obligation.provider.gridmap.FQAN;
 import org.glite.authz.common.obligation.provider.gridmap.GridMap;
 import org.glite.authz.common.obligation.provider.gridmap.GridMapKey;
 import org.glite.authz.common.obligation.provider.gridmap.X509DistinguishedName;
 import org.glite.authz.common.obligation.provider.gridmap.GridMap.GridMapKeyMatchFunction;
 import org.joda.time.DateTime;
 import org.jruby.ext.posix.Group;
 import org.jruby.ext.posix.Passwd;
 import org.opensaml.util.storage.AbstractExpiringObject;
 import org.opensaml.util.storage.StorageService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** An account mapper that maps {@link X509DistinguishedName} and {@link FQAN}s in to {@link PosixAccount}s. */
 @ThreadSafe
 public class PosixAccountMapper implements AccountMapper<PosixAccount> {
 
     /** Class logger. */
     private final Logger log = LoggerFactory.getLogger(PosixAccountMapper.class);
 
     /** The root of the partitions used to store mappings to a POSIX account, {@value} . */
     private static final String ACCOUNT_MAPPING_PARTITION = PosixAccountMapper.class.getCanonicalName();
 
     /** The name of partition used to map a subject identifier to a POSIX account, {@value} . */
     private static final String SUBJECT_TO_ACCOUNT_PARTITION = ACCOUNT_MAPPING_PARTITION + "/subAcct";
 
     /** The name of the partition used to map a POSIX log in name to POSIX account, {@value} . */
     private static final String LOGINNAME_TO_ACCOUNT_PARTITION = ACCOUNT_MAPPING_PARTITION + "/loginAcct";
 
     /** The grid map used to map DN's and FQANs to a POSIX UIDs. */
     private GridMap uidGridMap;
 
     /** The grid map used to map DN's and FQANs to a set of POSIX GIDs. */
     private GridMap gidGridMap;
 
     /** Service used to store account mappings. */
     private StorageService<String, PosixAccountMapping> storageService;
 
     /** Length of time, in milliseconds, an account mapping is kept. */
     private long accountMappingLifetime;
 
     /**
      * Constructor.
      * 
      * @param uidMap grid map used to map a subject to a POSIX UID
      * @param gidMap grid map used to map a subject to POSIX GIDs
      * @param mappingStore backing store used to persist mappings
      */
     public PosixAccountMapper(GridMap uidMap, GridMap gidMap, StorageService<String, PosixAccountMapping> mappingStore) {
         uidGridMap = uidMap;
         gidGridMap = gidMap;
         storageService = mappingStore;
     }
 
     /**
      * Constructor.
      * 
      * @param uidMap grid map used to map a subject to a POSIX UID
      * @param gidMap grid map used to map a subject to POSIX GIDs
      * @param mappingStore backing store used to persist mappings
      * @param mappingLifetime minimum length of time, in milliseconds, an account mapping is persisted
      */
     public PosixAccountMapper(GridMap uidMap, GridMap gidMap, StorageService<String, PosixAccountMapping> mappingStore,
             long mappingLifetime) {
         this(uidMap, gidMap, mappingStore);
         accountMappingLifetime = mappingLifetime;
     }
 
     /** {@inheritDoc} */
     public PosixAccount mapToAccount(String subjectid, List<? extends GridMapKey> keys) {
         if (keys == null || keys.isEmpty()) {
             return null;
         }
 
         PosixAccountMapping mapping = storageService.get(SUBJECT_TO_ACCOUNT_PARTITION, subjectid);
         PosixAccount posixAccount = null;
         if (mapping != null && !mapping.isExpired()) {
             posixAccount = mapping.getAccount();
            log.debug("An existing account mapping maps subject {} to an account, using it.", subjectid);
         }
 
         if (posixAccount == null) {
            log.debug("No existing account mapping for subject {}, attempting to create a new one", subjectid);
             Passwd account = mapToPosixAccount(subjectid, keys);
             if (account == null) {
                 log.debug("Unable to map subject {} to a POSIX account", subjectid);
                 return null;
             }
 
             List<Long> gids = null;
             if (keys.size() > 1) {
                 // remaining keys determine GIDs
                 gids = mapToGIDs(keys);
             }
 
             posixAccount = new PosixAccount(account.getLoginName(), account.getUID(), gids);
             mapping = new PosixAccountMapping(subjectid, posixAccount, accountMappingLifetime);
             storageService.put(SUBJECT_TO_ACCOUNT_PARTITION, subjectid, mapping);
             storageService.put(LOGINNAME_TO_ACCOUNT_PARTITION, posixAccount.getUsername(), mapping);
         }
 
         log.debug("Subject {} has been mapped to {}", subjectid, posixAccount);
         return posixAccount;
     }
 
     /**
      * Maps a set of subject attributes to a POSIX account.
      * 
      * @param subjectid ID of the subject
      * @param subjectAttributes attributes describing the subject
      * 
      * @return the POSIX account to which the subject was mapped or null
      */
     private Passwd mapToPosixAccount(String subjectid, List<? extends GridMapKey> subjectAttributes) {
         if(subjectAttributes == null || subjectAttributes.isEmpty() || uidGridMap == null || uidGridMap.getMapEntries().isEmpty()){
             return null;
         }
         
         GridMapKeyMatchFunction matchFunction;
         Passwd account;
         for (GridMapKey subjectAttribute : subjectAttributes) {
             for (GridMap.Entry mapEntry : uidGridMap.getMapEntries()) {
                 matchFunction = uidGridMap.getKeyMatchFunctions().get(mapEntry.getKey().getClass());
                 if (matchFunction != null && matchFunction.matches(mapEntry.getKey(), subjectAttribute)) {
                     account = selectAccount(mapEntry.getIds());
                     if (account != null) {
                         return account;
                     }
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Selects an account to which a subject is mapped. The account selected is the first account in the provided list
      * that exists and is not already in use. In the event that a candidate account name starts with a period ('.') it
      * will be treated as a "pool" account. Such account names will be expanded in a set of 999 account names where the
      * poolname is appended with 001 - 999.
      * 
      * @param possibleAccounts the list of possible account
      * 
      * @return the account selected
      */
     private Passwd selectAccount(List<String> possibleAccounts) {
         if (possibleAccounts == null || possibleAccounts.isEmpty()) {
             return null;
         }
         log.debug("Selecting account from possible accounts: {}", possibleAccounts);
 
         Passwd account = null;
         String basePoolAccountName;
         String poolAccountName;
         for (String possibleAccount : possibleAccounts) {
             if (possibleAccount.startsWith(".")) {
                 log.debug("Account candidiate {} is a pool account, checking expanded account names.", possibleAccount);
                 basePoolAccountName = possibleAccount.substring(1);
                 for (int i = 1; i < 999; i++) {
                     poolAccountName = basePoolAccountName + to3DigitString(i);
                     if (!storageService.contains(LOGINNAME_TO_ACCOUNT_PARTITION, poolAccountName)) {
                         account = PosixUtil.getAccountByName(poolAccountName);
                         if (account != null) {
                             return account;
                         }
                     }
                 }
             } else {
                 log.debug("Account candidate is not a pool account, performing direct name comparison.",
                         possibleAccount);
                 account = PosixUtil.getAccountByName(possibleAccount);
                 if (account != null) {
                     return account;
                 }
             }
         }
 
         return null;
     }
     
 
     /**
      * Converts an integer to a 3-digit string. If the integer is less than 100 it is left padded with 0s in order to
      * make it 3-digits.
      * 
      * @param integer the integer to convert, must be in the range 0 - 999
      * 
      * @return the 3-digit string for the integer
      */
     private String to3DigitString(int integer) {
         if (integer < 0 || integer > 999) {
             throw new IllegalArgumentException("Integer must be in the range 0 - 999");
         }
 
         if (integer < 10) {
             return new String("00" + integer);
         }
 
         if (integer < 100) {
             return new String("0" + integer);
         }
 
         return Integer.toString(integer);
     }
 
     /**
      * Maps a set of {@link FQAN}s to a list of GIDs.
      * 
      * @param subjectAttributes keys to map to GIDs
      * 
      * @return the set of GIDs for the account with the primary GID being the first in the list
      */
     private List<Long> mapToGIDs(List<? extends GridMapKey> subjectAttributes) {
         if (subjectAttributes == null || subjectAttributes.isEmpty()) {
             return null;
         }
         log.debug("Selecting primary and secondary GIDs based on the possible keys: {}", subjectAttributes);
         ArrayList<Long> gids = new ArrayList<Long>();
 
         GridMapKeyMatchFunction matchFunction = gidGridMap.getKeyMatchFunctions().get(FQAN.class);
         List<String> groupNames;
         Group group;
         for (GridMapKey subjectAttribute : subjectAttributes) {
             if (!(subjectAttribute instanceof FQAN)) {
                 log.debug("Key {} is not an FQAN, skipping it", subjectAttribute);
                 continue;
             }
 
             for (GridMap.Entry mapEntry : gidGridMap.getMapEntries()) {
                 if (matchFunction.matches(mapEntry.getKey(), subjectAttribute)) {
                     groupNames = mapEntry.getIds();
                     if (groupNames == null || groupNames.isEmpty()) {
                         log.debug("Key {} did not map to a set of group names", subjectAttribute);
                         continue;
                     }
                     for (String groupName : groupNames) {
                         group = PosixUtil.getGroupByName(groupName);
                         if (group != null) {
                             log.debug("Adding GID {}, resolved from group name {}, to list of GIDs", group.getGID(),
                                     groupName);
                             gids.add(new Long(group.getGID()));
                         } else {
                             log.debug("Group name {} does not resolve to a POSIX group on this system", groupName);
                         }
                     }
                 }
             }
         }
 
         // Remove any nulls or duplicate GID entries from the list
         log.debug("Removing any duplicate GIDs");
         ArrayList<Long> visitedGIDs = new ArrayList<Long>();
         Iterator<Long> gidIter = gids.iterator();
         Long gid;
         while (gidIter.hasNext()) {
             gid = gidIter.next();
             if (gid == null || visitedGIDs.contains(gid)) {
                 gidIter.remove();
             }
             visitedGIDs.add(gid);
         }
 
         return gids;
     }
 
     /** A mapping from a set of subject information to a POSIX account. */
     private static class PosixAccountMapping extends AbstractExpiringObject {
 
         /** Serial version UID. */
         private static final long serialVersionUID = -4740264321308516550L;
 
         /** ID of the subject mapped to the POSIX account. */
         private String subjectId;
 
         /** POSIX account to which subject is mapped. */
         private PosixAccount account;
 
         /**
          * Constructor.
          * 
          * @param subjectId ID of the subject mapped to the POSIX account
          * @param account POSIX account to which subject is mapped
          * @param lifetime lifetime of the account, in milliseconds
          */
         public PosixAccountMapping(String subjectId, PosixAccount account, long lifetime) {
             super(new DateTime().plus(lifetime));
             this.subjectId = subjectId;
             this.account = account;
         }
 
         /**
          * Gets the ID of the subject mapped to the POSIX account.
          * 
          * @return ID of the subject mapped to the POSIX account
          */
         public String getSubjectId() {
             return subjectId;
         }
 
         /**
          * Gets the POSIX account to which subject is mapped.
          * 
          * @return POSIX account to which subject is mapped
          */
         public PosixAccount getAccount() {
             return account;
         }
     }
 }
