 package com.jonmort.plugin.aoso;
 
 import com.atlassian.activeobjects.external.ActiveObjects;
 import com.atlassian.sal.api.user.UserManager;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import net.java.ao.Query;
 
 import java.util.Arrays;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public class StorageServiceImpl implements StorageService {
     private final ActiveObjects ao;
     private final UserManager userManager;
 
     public StorageServiceImpl(ActiveObjects ao, UserManager userManager) {
         this.userManager = userManager;
         this.ao = checkNotNull(ao);
     }
 
     public void store(Storage storage) {
         storage.save();
     }
 
     public Storage get(String scope, String dataId) {
         return ao.get(Storage.class, createIdentifierString(scope, dataId));
     }
 
     public Storage create(String scope, String dataId, String data) {
         final Storage storage = ao.create(Storage.class, ImmutableMap.of("identifier", (Object) createIdentifierString(scope, dataId)));
         storage.setData(data);
         storage.save();
         return storage;
     }
 
     public void remove(Storage storage) {
         ao.delete(storage);
     }
 
     public Iterable<Storage> search(String scope, String searchString, int noResults, int offset) {
        String identifierRegex = String.format("%s/%s%%", scope, searchString);
         Query query = Query.select().where("IDENTIFIER LIKE ?", identifierRegex)
                 .limit(noResults)
                 .offset(offset);
         Storage[] result = ao.find(Storage.class, query);
         return ImmutableList.of(result);
     }
 
     private String createIdentifierString(String scope, String dataId) {
         if ("user".equals(scope)) {
             return String.format("%s/%s/%s", scope, userManager.getRemoteUsername(), dataId);
         }
         return String.format("%s/%s",  scope, dataId);
     }
 }
