 package cn.hit.sqat.dao;
 
 import cn.hit.sqat.beans.NewSaleBean;
 import cn.hit.sqat.beans.SalesBean;
 import cn.hit.sqat.info.Database;
 import cn.hit.sqat.login.Login;
 import cn.hit.sqat.login.UserType;
 
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Emil
  * Date: 2013-06-06
  * Time: 21:50
  * To change this template use File | Settings | File Templates.
  */
 public class SalesDAO {
 
     public static List<SalesBean> getSales(UserType userType, String userId, java.util.Date date) {
         List<SalesBean> sales = new LinkedList<SalesBean>();
         Database.connect();
         ResultSet rs;
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         int year = cal.get(Calendar.YEAR);
         int month = cal.get(Calendar.MONTH) + 1;
         if (userType  == UserType.SALESMAN) {
             rs = Database.query("SELECT s.date, gp.description, s.quantity, s.quantity*p.price, gc.name, c.name " +
                     "FROM Sales s, Gunsmith gc, City c, Gunpart gp, Production p " +
                     "WHERE s.salesmanid = " + userId + " " +
                     "AND gp.gunpartid = s.gunpartid " +
                     "AND gc.gunsmithid = s.gunsmithid " +
                     "AND c.cityid = s.cityid " +
                     "AND p.gunsmithid = s.gunsmithid " +
                     "AND p.gunpartid = s.gunpartid " +
                     "AND YEAR(s.date) = '" + year + "' " +
                     "AND MONTH(s.date) = '" + month + "' " +
                     "GROUP BY gp.description " +
                     "ORDER BY s.date DESC");
         } else {
             rs = Database.query("SELECT s.date, gp.description, s.quantity, s.quantity*p.price, sm.name, c.name " +
                     "FROM Sales s, Salesman sm, City c, Gunpart gp, Production p " +
                     "WHERE s.gunsmithid = " + userId + " " +
                     "AND gp.gunpartid = s.gunpartid " +
                     "AND sm.salesmanid = s.salesmanid " +
                     "AND c.cityid = s.cityid " +
                     "AND p.gunsmithid = s.gunsmithid " +
                     "AND p.gunpartid = s.gunpartid " +
                     "AND YEAR(s.date) = '" + year + "' " +
                     "AND MONTH(s.date) = '" + month + "' " +
                     "ORDER BY s.date DESC");
         }
         try {
             while (rs.next()) {
                 SalesBean salesBean = new SalesBean();
                 salesBean.setDate(rs.getDate(1));
                 salesBean.setPart(rs.getString(2));
                 salesBean.setQuantity(rs.getInt(3));
                 salesBean.setValue(rs.getInt(4));
                 salesBean.setActor(rs.getString(5));
                 salesBean.setCity(rs.getString(6));
                 sales.add(salesBean);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         Database.disconnect();
         return sales;
     }
 
     public static List<SalesBean> getSales(UserType userType, String userId, int gunPartId, java.util.Date date) {
         List<SalesBean> sales = new LinkedList<SalesBean>();
         Database.connect();
         ResultSet rs;
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         int year = cal.get(Calendar.YEAR);
         int month = cal.get(Calendar.MONTH) + 1;
         if (userType == UserType.SALESMAN) {
             rs = Database.query("SELECT s.date, gp.description, s.quantity, s.quantity*p.price, gc.name, c.name " +
                     "FROM Sales s, Gunsmith gc, City c, Gunpart gp, Production p " +
                     "WHERE s.salesmanid = " + userId + " " +
                     "AND s.gunpartid = " + gunPartId + " " +
                     "AND gp.gunpartid = s.gunpartid " +
                     "AND gc.gunsmithid = s.gunsmithid " +
                     "AND c.cityid = s.cityid " +
                     "AND p.gunsmithid = s.gunsmithid " +
                     "AND p.gunpartid = s.gunpartid " +
                     "AND YEAR(s.date) = '" + year + "' " +
                     "AND MONTH(s.date) = '" + month + "' " +
                     "ORDER BY s.date DESC");
         } else {
             rs = Database.query("SELECT s.date, gp.description, s.quantity, s.quantity*p.price, sm.name, c.name " +
                     "FROM Sales s, Salesman sm, City c, Gunpart gp, Production p " +
                     "WHERE s.gunsmithid = " + userId + " " +
                     "AND s.gunpartid = " + gunPartId + " " +
                     "AND gp.gunpartid = s.gunpartid " +
                     "AND sm.salesmanid = s.salesmanid " +
                     "AND c.cityid = s.cityid " +
                     "AND p.gunsmithid = s.gunsmithid " +
                     "AND p.gunpartid = s.gunpartid " +
                     "AND YEAR(s.date) = '" + year + "' " +
                     "AND MONTH(s.date) = '" + month + "' " +
                     "ORDER BY s.date DESC");
         }
 
         try {
             while (rs.next()) {
                 SalesBean salesBean = new SalesBean();
                 salesBean.setDate(rs.getDate(1));
                 salesBean.setPart(rs.getString(2));
                 salesBean.setQuantity(rs.getInt(3));
                 salesBean.setValue(rs.getInt(4));
                 salesBean.setActor(rs.getString(5));
                 salesBean.setCity(rs.getString(6));
                 sales.add(salesBean);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         Database.disconnect();
         return sales;
     }
 
     public static List<Integer> getSalesTotal(String salesmanId, int gunPartId, java.util.Date date) {
         List<Integer> quantity = new LinkedList<Integer>();
         Database.connect();
         ResultSet rs = null;
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         int year = cal.get(Calendar.YEAR);
         int month = cal.get(Calendar.MONTH) + 1;
         if (gunPartId != 0) {
             rs = Database.query("SELECT sum(s.quantity*p.price), sum(s.quantity) " +
                     "FROM Sales s, Production p " +
                     "WHERE s.salesmanid = " + salesmanId + " " +
                     "AND s.gunpartid = " + gunPartId + " " +
                     "AND s.gunpartid = p.gunpartid " +
                     "AND s.gunsmithid = p.gunsmithid " +
                     "AND YEAR(s.date) = " + year + " " +
                     "AND MONTH(s.date) = " + month + " " +
                     "GROUP BY (s.gunpartid)");
         } else {
             rs = Database.query("SELECT sum(s.quantity*p.price), sum(s.quantity)" +
                     "FROM Sales s, Production p " +
                     "WHERE s.salesmanid = " + salesmanId + " " +
                     "AND s.gunpartid = p.gunpartid " +
                     "AND s.gunsmithid = p.gunsmithid " +
                     "AND YEAR(s.date) = " + year + " " +
                     "AND MONTH(s.date) = " + month + " " +
                     "GROUP BY (s.salesmanid)");
         }
         try {
             if (rs.next()) {
                 quantity.add(rs.getInt(1));
                 quantity.add(rs.getInt(2));
             } else {
                 quantity.add(0);
                 quantity.add(0);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         Database.disconnect();
         return quantity;
     }
 
 
     public NewSaleBean getNewSale() {
         NewSaleBean newSale = new NewSaleBean();
         Database.connect();
         ResultSet rs = Database.query("SELECT s.date, gp.description, s.quantity, s.quantity*p.price, sm.name, c.name " +
                 "FROM Sales s, Salesman sm, City c, Gunpart gp, Production p " +
                 "WHERE s.gunsmithid = " + " " +
                 "AND gp.gunpartid = s.gunpartid " +
                 "AND sm.salesmanid = s.salesmanid " +
                 "AND c.cityid = s.cityid " +
                 "AND p.gunsmithid = s.gunsmithid " +
                 "AND p.gunpartid = s.gunpartid " +
                 "AND YEAR(s.date) = YEAR(NOW()) " +
                 "AND MONTH(s.date) = MONTH(NOW()) " +
                 "ORDER BY s.date DESC");
         return null;
     }
 
     public static boolean checkValidSale(String gunsmith, Date date, Map<Integer, Integer> parts) {
         final Calendar c = Calendar.getInstance();
         c.setTime(date);
 
         final int year = c.get(Calendar.YEAR), month = 1 + c.get(Calendar.MONTH);
         final String query = "SELECT s.gunpartid, p.monthlylimit - SUM(s.quantity) FROM sales s, production p " +
                 "WHERE p.gunsmithid = s.gunsmithid AND p.gunpartid = s.gunpartid AND " +
                 "s.gunsmithid = " + gunsmith +
                 " AND YEAR(s.date) = " + year +
                 " AND MONTH(s.date) = " + month +
                 " GROUP BY s.gunpartid";
 
         final ResultSet rs = Database.query(query);
         try {
             while(rs.next()) {
                final int partId = Integer.parseInt(rs.getString(0));
                 if(parts.containsKey(partId)) {
                     final int soldParts = parts.get(partId);
                    if(soldParts > Integer.parseInt(rs.getString(1)))
                         return false;
                 }
             }
 
             return true;
         } catch(final SQLException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     public static boolean createNewSale(String salesman, String gunsmith, int city, Date date, Map<Integer, Integer> parts) {
         Database.connect();
 
         if(!checkValidSale(gunsmith, date, parts)) {
             Database.disconnect();
             return false;
         }
 
         boolean result = true;
         java.sql.Date sqlDate = new Date(date.getTime());
         Iterator<Map.Entry<Integer, Integer>> it = parts.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry<Integer, Integer> entry = it.next();
             if (entry.getValue() > 0) {
                 result = Database.insert(String.format("INSERT INTO Sales VALUES ('%s', '%s', '%s', '%s', '%s', '%s')",
                         salesman, entry.getKey(), gunsmith, city, entry.getValue(), sqlDate));
                 System.out.println(String.format("INSERT INTO Sales VALUES ('%s', '%s, '%s', '%s', '%s', '%s')",
                         salesman, entry.getKey(), gunsmith, city, entry.getValue(), sqlDate));
             }
         }
         Database.disconnect();
         return result;
     }
 }
