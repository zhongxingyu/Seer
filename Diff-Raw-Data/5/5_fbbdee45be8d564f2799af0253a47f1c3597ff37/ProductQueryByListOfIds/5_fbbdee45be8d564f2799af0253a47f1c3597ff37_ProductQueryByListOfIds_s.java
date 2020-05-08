 package com.shopservice.queries;
 
 import com.shopservice.Services;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 public class ProductQueryByListOfIds extends ProductQuery {
 
     private List<String> productIds;
 
     public ProductQueryByListOfIds(String clientId, List<String> productIds) {
         super(clientId);
         this.productIds = productIds;
     }
 
     @Override
     public String getRawSql() {
         String[] abc = new String[productIds.size()];
         Arrays.fill(abc, "?");
 
         return Services.queries.getProductQueryByListOfIds(clientId)
                 .replace("?", Arrays.asList(abc).toString().replace("[","").replace("]","") );
     }
 
     @Override
     public void prepare(PreparedStatement statement) throws SQLException {
         for (int i=0; i<productIds.size(); i++)
             statement.setObject( i+1, productIds.get(i) );
     }
 }
