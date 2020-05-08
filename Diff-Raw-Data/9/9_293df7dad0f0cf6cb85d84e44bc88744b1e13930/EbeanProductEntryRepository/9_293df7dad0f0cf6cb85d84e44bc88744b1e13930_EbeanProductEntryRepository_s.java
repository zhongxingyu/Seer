 package com.shopservice.dao;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.SqlRow;
 import com.shopservice.domain.ClientSettings;
 import com.shopservice.domain.ProductEntry;
 
 import java.util.*;
 
 import static com.shopservice.MServiceInjector.injector;
 
 public class EbeanProductEntryRepository implements ProductEntryRepository {
 
     private ClientSettingsRepository clientSettingsRepository = injector.getInstance(ClientSettingsRepository.class);
 
     @Override
     public List<ProductEntry> findSelected(String clientSettingsId, int groupId) throws Exception {
         List<SqlRow> rows = Ebean.createSqlQuery("SELECT product_entry.* FROM product_entry " +
                 "JOIN group2product ON group2product.product_entry_id = product_entry.id AND group2product.product_group_id = ? " +
                 "WHERE client_settings_id = ?")
                 .setParameter(1, groupId)
                 .setParameter(2, clientSettingsId)
                 .findList();
 
         List<ProductEntry> entries = new ArrayList<ProductEntry>();
         for (SqlRow row : rows)
             entries.add( new ProductEntry(row) );
 
         return entries;
     }
 
     @Override
     public void add(String clientsId, Collection<ProductEntry> productsToAdd) throws Exception {
         ClientSettings clientSettings = clientSettingsRepository.findById(clientsId);
         clientSettings.productEntries.addAll(productsToAdd);
         Ebean.save(clientSettings);    }
 
     @Override
     public void delete(Collection<ProductEntry> productsToDelete) throws Exception {
         for (ProductEntry productEntry: productsToDelete){
            Ebean.delete(ProductEntry.class, productEntry);
         }
     }
 
     @Override
     public List<ProductEntry> getWithChecked(String clientId, String categoryId, int groupId) throws Exception {
         List<SqlRow> rows = Ebean.createSqlQuery("SELECT product_entry.*, group2product.id IS NOT NULL AS checked FROM product_entry " +
                 "LEFT JOIN group2product ON group2product.product_entry_id = product_entry.id AND group2product.product_group_id = ? " +
                 "WHERE client_settings_id = ? AND category_id = ? ")
                 .setParameter(1, groupId)
                 .setParameter(2, clientId)
                 .setParameter(3, categoryId)
                 .findList();
 
         List<ProductEntry> entries = new ArrayList<ProductEntry>();
         for (SqlRow row : rows)
             entries.add(new ProductEntry(row));
 
         return entries;
     }
 
     @Override
     public Map<String, Integer> getCountPerCategory(String clientId, String groupId) {
         List<SqlRow> rows = Ebean.createSqlQuery("SELECT category_id, count(product_entry.id) AS total FROM product_entry " +
                 "JOIN group2product ON group2product.product_entry_id = product_entry.id " +
                 "WHERE client_settings_id = ? AND product_group_id = ? " +
                 "GROUP BY category_id")
                 .setParameter(1, clientId)
                 .setParameter(2, groupId)
                 .findList();
 
         Map<String, Integer> result = new HashMap<String, Integer>();
 
         for (SqlRow row : rows)
             result.put(row.getString("category_id"), row.getInteger("total") );
 
         return result;
     }
 
     @Override
     public Set<ProductEntry> get(String clientId, String categoryId) {
         return Ebean.find(ProductEntry.class)
                 .where()
                 .eq("client_settings_id", clientId)
                 .eq("category_id",categoryId)
                 .findSet();
     }
 }
