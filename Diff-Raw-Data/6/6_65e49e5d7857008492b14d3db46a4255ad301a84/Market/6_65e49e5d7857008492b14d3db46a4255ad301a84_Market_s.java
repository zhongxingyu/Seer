 package controllers;
 
 
 import models.Order;
 import models.Security;
 import models.Trade;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @WebServlet("/market")
 public class Market extends HttpServlet {
     public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
     public static final String JDBC_URL = "jdbc:mysql://89.253.85.33/test";
     public static final String JDBC_USER = "root";
     public static final String JDBC_PASSWORD = "123654789";
     private Connection connection;
     private PreparedStatement ps;
 
     public Market() {
         try {
             Class.forName(JDBC_DRIVER);
             connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (ClassNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public void doGet(HttpServletRequest request, HttpServletResponse response) {
         List<Trade> trades = new ArrayList<Trade>();
         String error = "";
 
         try {
             Map<String, String> p;
             p = checkAndConvertParameters(request.getParameterMap());
 
             String action = p.get("action");
 
 
             if (action == null) {
             } else if (action.equals("addSecurity")) {
                 addSecurity(p.get("security"));
             } else if (action.equals("addOrder")) {
                 addOrder(new Order(Integer.parseInt(p.get("amount")), Double.parseDouble(p.get("price")), p.get("buyOrSell"), p.get("security"), p.get("customer")));
             } else if (action.equals("viewTrades")) {
                 trades = viewTrades(p.get("security"));
             } else {
                 throw new Exception("Request error: Unknown action: " + action);
             }
 
         } catch (Exception e) {
             error = e.toString();
             e.printStackTrace();
         } finally {
             request.setAttribute("trades", trades);
             request.setAttribute("error", error);
 
             try {
                 request.setAttribute("securities", getSecurities());
 
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/market.jsp").forward(request, response);
             } catch (ServletException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             } catch (SQLException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
             if (ps != null) {
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         }
     }
 
     private void addSecurity(String security) throws SQLException {
         ps = connection.prepareStatement("INSERT INTO securities(name) VALUES(?)");
         ps.setString(1, security);
         ps.executeUpdate();
     }
 
     private List<Trade> viewTrades(String security) throws SQLException {
         ps = connection.prepareStatement("SELECT * FROM trades WHERE security =? ORDER BY dt");
         ps.setString(1, security);
         ResultSet rs = ps.executeQuery();
         List<Trade> trades = new ArrayList<Trade>();
         while (rs.next()) {
             Trade trade = new Trade(rs.getString(2), rs.getDouble(3), rs.getInt(4), rs.getTimestamp(5), rs.getString(6), rs.getString(7));
             trades.add(trade);
         }
         return trades;
     }
 
     private void addOrder(Order order) throws Exception {
         checkSecurityExistence(order.getSecurity());
         Order matchingOrder = getAndRemoveMatchingOrder(order);
         if (matchingOrder != null) {
             persistTrade(createTrade(order, matchingOrder));
         } else {
             persistOrder(order);
         }
     }
 
     private void persistOrder(Order order) throws SQLException {
         assert order.getAmount() > 0;
         assert order.getPrice() >= 0;
         ps = connection.prepareStatement("INSERT INTO orders(security, type, price, amount, customer) VALUES(?,?,?,?,?)");
         ps.setString(1, order.getSecurity());
         ps.setString(2, order.getBuyOrSell());
         ps.setDouble(3, order.getPrice());
         ps.setInt(4, order.getAmount());
         ps.setString(5, order.getCustomer());
 
         ps.executeUpdate();
     }
 
     private Trade createTrade(Order order, Order matchingorder) {
         String security = order.getSecurity();
         Double price = order.getPrice();
         int amount = matchingorder.getAmount();
         Timestamp currentTime = getCurrentTime();
         String buyer;
         String seller;
         if (order.getBuyOrSell().equals("b")) {
             buyer = order.getCustomer();
             seller = matchingorder.getCustomer();
 
         } else {
             buyer = matchingorder.getCustomer();
             seller = order.getCustomer();
         }
         return new Trade(security, price, amount, currentTime, buyer, seller);
     }
 
     private void persistTrade(Trade trade) throws SQLException {
         ps = connection.prepareStatement("INSERT INTO trades(security, price, amount, dt, buyer, seller) VALUES(?,?,?,?,?,?)");
         ps.setString(1, trade.getSecurity());
         ps.setDouble(2, trade.getPrice());
         ps.setInt(3, trade.getAmount());
         ps.setTimestamp(4, trade.getCurrentTime());
         ps.setString(5, trade.getBuyer());
         ps.setString(6, trade.getSeller());
 
         ps.executeUpdate();
     }
 
     private Order getAndRemoveMatchingOrder(Order order) throws SQLException {
        ps = connection.prepareStatement("SELECT * FROM orders WHERE (security =? AND type =? AND price =?)", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
         ps.setString(1, order.getSecurity());
         ps.setString(2, getOppositeType(order.getBuyOrSell()));
         ps.setDouble(3, order.getPrice());
 
         ResultSet rs = ps.executeQuery();
         Order matchingOrder = null;
         if (rs.next()) {
             matchingOrder = new Order(rs.getInt("amount"), rs.getDouble("price"), rs.getString("type"), rs.getString("security"), rs.getString("customer"));
             int matchAmount = Math.min(matchingOrder.getAmount(), order.getAmount());
             if(matchAmount == matchingOrder.getAmount()){
                 rs.deleteRow();
             }else{
                 rs.updateInt("amount", matchingOrder.getAmount()-matchAmount);
                 rs.updateRow();
                 matchingOrder.setAmount(matchAmount);
             }
             if(matchAmount != order.getAmount()){
                 order.setAmount(order.getAmount()-matchAmount);
                 persistOrder(order);
             }
         }
         return matchingOrder;
     }
 
     private String getOppositeType(String buyOrSell) {
         if (buyOrSell.equals("B")) {
             return "S";
         } else if (buyOrSell.equals("S")) {
             return "B";
         }
         return null;
     }
 
     private void checkSecurityExistence(String security) throws Exception {
         ps = connection.prepareStatement("SELECT name FROM securities WHERE name =?");
         ps.setString(1, security);
         ResultSet rs = ps.executeQuery();
         if (!rs.next()) {
             throw new Exception("Security not in database!");
         }
     }
 
     private Map<String, String> checkAndConvertParameters(Map<String, String[]> parameters) {
         Map<String, String> convertedMap = new HashMap<String, String>();
         for (Map.Entry<String, String[]> paramSet : parameters.entrySet()) {
             assert !paramSet.getValue()[0].equals("");
             convertedMap.put(paramSet.getKey(), paramSet.getValue()[0]);
         }
         return convertedMap;
     }
 
     private Timestamp getCurrentTime() {
         return new Timestamp(new java.util.Date().getTime());
     }
 
     public List<Security> getSecurities() throws SQLException {
         List<Security> securities = new ArrayList<Security>();
 
         ps = connection.prepareStatement("SELECT name FROM securities");
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
             securities.add(new Security(rs.getString("name")));
         }
         return securities;
     }
 }
 
 
 
