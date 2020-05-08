 package com.shopservice.assemblers;
 
 import com.google.common.collect.Sets;
 import com.google.inject.Inject;
 import com.shopservice.Services;
 import com.shopservice.dao.ProductEntryRepository;
 import com.shopservice.domain.Product;
 import com.shopservice.domain.ProductEntry;
 
 import java.util.*;
 
 public class ProductAssembler {
 
     ProductEntryRepository productEntryRepository;
 
     @Inject
     public ProductAssembler(ProductEntryRepository productEntryRepository) {
         this.productEntryRepository = productEntryRepository;
     }
 
     public Collection<ProductEntry> getProducts(String clientId, String categoryId, int groupId) throws Exception {
         List<Product> products = Services.getProductDAO(clientId).getProducts(categoryId);
 
         Set<ProductEntry> productEntriesFromClient = new HashSet<ProductEntry>();
         for (Product product : products)
             productEntriesFromClient.add(new ProductEntry(product));
 
         Set<ProductEntry> productEntriesFromSettings = productEntryRepository.get(clientId, categoryId);
 
         productEntryRepository.delete(Sets.difference(productEntriesFromSettings, productEntriesFromClient));
 
         productEntryRepository.add(clientId, Sets.difference(productEntriesFromClient, productEntriesFromSettings));
 
         Map<String,ProductEntry> productEntries = new HashMap<String, ProductEntry>();
         for (ProductEntry entry : productEntryRepository.getWithChecked(clientId, categoryId, groupId) )
             productEntries.put(entry.productId, entry );
 
        for (Product product : Services.getProductDAO(clientId).getProducts(categoryId))
             fill(productEntries.get(product.id), product);
 
        return productEntries.values();
     }
 
     private void fill(ProductEntry productEntry, Product product) {
         productEntry.url = product.url;
         productEntry.price = product.price;
         productEntry.productName = product.name;
         productEntry.published = product.published;
     }
 }
