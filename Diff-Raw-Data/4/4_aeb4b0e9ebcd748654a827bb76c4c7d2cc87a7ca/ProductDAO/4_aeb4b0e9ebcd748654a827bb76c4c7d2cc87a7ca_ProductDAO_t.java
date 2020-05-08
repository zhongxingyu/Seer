 package com.etechies.server.dao;
 import com.etechies.server.beans.*;
 import com.etechies.server.dbagent.DBAgent;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /**
  *
  * @author Aman
  */
 public class ProductDAO {
     
     DBAgent dba=new DBAgent();
     
     public ArrayList<Product> getProductList (String category){
         
         String[] param = {category};
         ArrayList<Product> cdlist = new ArrayList<Product>();
        //Product cd = new Product();
         
         try {
             ResultSet rs = dba.getQueryResult("get_products_by_category",param);
                 while(rs.next()){
                Product cd = new Product();
                 cd.cdId=rs.getString("cdid");
                 cd.category=rs.getString("category");
                 cd.price=rs.getDouble("price");
                 cd.title=rs.getString("title");
                    cdlist.add(cd);
             }
         } catch (SQLException ex) {
             System.out.println("MySql Error" + ex);
         }
         return cdlist;
     }
     
         
 //      public ArrayList<Product> getProductList(String categoryId){
 //        ResultSet rs;
 //        ArrayList<Product> products = new ArrayList<Product>();
 //        String[] category = {categoryId};
 //        try { 
 //        rs = getQueryResult("get_products_by_category", category);
 //          while (rs.next()) {
 //            Product p = new Product();
 //            p.title = rs.getString("title");
 //            p.price = rs.getDouble("price");
 //            p.category = rs.getString("category");
 //            products.add(p);
 //            } 
 //        } catch (SQLException e){
 //            System.out.println(e);
 //        }
 //        return products;
 //    }
     
     
     
     
     
     
     
 //    public ArrayList<Product> getProductList (){
 //        
 //       // String[] param = {category};
 //        ArrayList<Product> cdlist=null;
 //        Product cd=new Product();
 //        ResultSet rs = dba.getQueryResult("get_products",null);
 //        try {
 //            while(rs.next()){
 //            cd.cdId=rs.getString("cdid");
 //            cd.category=rs.getString("category");
 //            cd.price=rs.getDouble("price");
 //            cd.title=rs.getString("title");
 //               cdlist.add(cd);
 //            }
 //        } catch (SQLException ex) {
 //            System.out.println("MySql Error" + ex);
 //        }
 //        return cdlist;
 //    }
     
 
 }
