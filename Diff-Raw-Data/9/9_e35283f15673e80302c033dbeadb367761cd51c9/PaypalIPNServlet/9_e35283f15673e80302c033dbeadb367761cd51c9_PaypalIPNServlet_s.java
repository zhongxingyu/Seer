 package org.jsc.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Enumeration;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * An implementation of a servlet that listens for connections from PayPal and 
  * logs the receipt data from the site, but only if it is valid.
  * @author Matt Jones
  */
 public class PaypalIPNServlet extends HttpServlet {
 
 //    private static final String PAYPAL_URL = "https://www.paypal.com/cgi-bin/webscr";
     private static final String PAYPAL_URL = "https://www.sandbox.paypal.com/cgi-bin/webscr";
 //    private static final String JDBC_URL = "jdbc:postgresql://localhost/jscdb";
     private static final String JDBC_URL = "jdbc:postgresql://localhost/jscdbtest";
     private static final String JDBC_USER = "jscdb";
     private static final String JDBC_PASS = "1skate2";
     private static final String JDBC_DRIVER = "org.postgresql.Driver";
     
     public void init() {
         System.out.println("PaypalIPNServlet initialized.");
     }
     
     public void doPost(HttpServletRequest request, HttpServletResponse response) {
         System.out.println("Handling POST input");
         processIpnRequest(request, response);
     }
     
     public void doGet(HttpServletRequest request, HttpServletResponse response) {
         System.out.println("Handling GET input");
         processIpnRequest(request, response);
     }        
     
     private void processIpnRequest(HttpServletRequest request, HttpServletResponse response) {
     
         Enumeration<String> en = request.getParameterNames();
         String str = "cmd=_notify-validate";
         while (en.hasMoreElements()) {
             String paramName = en.nextElement();
             String paramValue = request.getParameter(paramName);
             System.out.println("IPN SENT: " + paramName + " ==> " + paramValue);
             str = str + "&" + paramName + "="
                     + URLEncoder.encode(paramValue);
         }
 
         // post back to PayPal system to validate
         // NOTE: change http: to https: in the following URL to verify using SSL (for increased security).
         // using HTTPS requires either Java 1.4 or greater, or Java Secure Socket Extension (JSSE)
         // and configured for older versions.
         URL u;
         try {
             u = new URL(PAYPAL_URL);
             URLConnection uc = u.openConnection();
             uc.setDoOutput(true);
             uc.setRequestProperty("Content-Type",
                     "application/x-www-form-urlencoded");
             PrintWriter pw = new PrintWriter(uc.getOutputStream());
             pw.println(str);
             pw.close();
 
             BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
             String res = in.readLine();
             in.close();
 
             //check notification validation
            if (res.equals("VERIFIED")) {
                 recordTransaction(request);
            } else if (res.equals("INVALID")) {
                 System.out.println("Invalid IPN connection");
                 // TODO: log for investigation
             } else {
                 // TODO: error
             }
         } catch (MalformedURLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     private void recordTransaction(HttpServletRequest request) {
         // assign posted variables to local variables
         String txnId = request.getParameter("txn_id");
         long paymentId = new Long(request.getParameter("invoice")).longValue();
         String paymentStatus = request.getParameter("payment_status");
         double paymentGross = paramToDouble(request.getParameter("mc_gross"));
         double paypalFee = paramToDouble(request.getParameter("mc_fee"));
         double discount = paramToDouble(request.getParameter("discount"));
         double net = paymentGross - paypalFee;
         String receiverEmail = request.getParameter("receiver_email");
         String payerEmail = request.getParameter("payer_email");
         String payerId = request.getParameter("payer_id");
 
         System.out.println("Verified IPN connection");
         System.out.println("txn_id: " + txnId);
         System.out.println("paymentid: " + paymentId);
         System.out.println("payment_status: " + paymentStatus);
         System.out.println("mc_gross: " + paymentGross);
         System.out.println("discount: " + discount);
         System.out.println("mc_fee: " + paypalFee);
         System.out.println("net: " + net);
         System.out.println("receiver_email: " + receiverEmail);
         System.out.println("payer_email: " + payerEmail);
         System.out.println("payer_id: " + payerId);
 
         // TODO: check that paymentStatus=Completed
         // TODO: check that txnId has not been previously processed
         // TODO: check that receiverEmail is your Primary PayPal email
         // TODO: check that paymentAmount/paymentCurrency are correct
         // TODO: process payment
         
         // Create a SQL statement for updating the payment record
         StringBuffer sql = new StringBuffer();
         sql.append("update payment set ");
         sql.append("paypal_tx_id='").append(txnId).append("',");
         sql.append("paypal_status='").append(paymentStatus).append("'");
         if (paymentStatus.equals("Completed")) {
             sql.append(",paypal_gross=").append(paymentGross).append(",");
             sql.append("paypal_fee=").append(paypalFee).append(",");
             sql.append("discount=").append(discount).append(",");
             sql.append("paypal_net=").append(net).append(",");
             sql.append("payer_email='").append(payerEmail).append("',");
             sql.append("payer_id='").append(payerId).append("' ");
         }
         sql.append(" where paymentid=").append(paymentId);
         // payment_date DATE,         -- the date the payment was made
         System.out.println(sql.toString());
         
         // Now update the payment table record
         Connection con = getConnection();
         Statement stmt;
         try {
             stmt = con.createStatement();
             stmt.executeUpdate(sql.toString());
             stmt.close();
         } catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         /* 
          * These are most of the variables PayPal might send back in an IPN message:
          * 
         IPN SENT: last_name ==> User
         IPN SENT: item_name2 ==> 2009-2010 Session 1 FS Synchro (Sunday 9:15-10:15am)
         IPN SENT: item_name1 ==> 2009-2010 Session 1 FS Club Ice (Friday 5:15-6:15pm)
         IPN SENT: test_ipn ==> 1
         IPN SENT: txn_type ==> cart
         IPN SENT: insurance_amount ==> 0.00
         IPN SENT: mc_handling2 ==> 0.00
         IPN SENT: mc_handling1 ==> 0.00
         IPN SENT: receiver_email ==> james_1202254981_biz@gmail.com
         IPN SENT: residence_country ==> US
         IPN SENT: payment_gross ==> 150.00
         IPN SENT: payment_date ==> 00:22:52 Sep 01, 2009 PDT
         IPN SENT: quantity2 ==> 1
         IPN SENT: payment_status ==> Completed
         IPN SENT: quantity1 ==> 1
         IPN SENT: discount ==> 10.0
         IPN SENT: mc_shipping ==> 0.00
         IPN SENT: first_name ==> Test
         IPN SENT: payer_email ==> matt_1202254711_per@gmail.com
         IPN SENT: protection_eligibility ==> Ineligible
         IPN SENT: payer_id ==> RFUQYKLDCF5YC
         IPN SENT: verify_sign ==> ApGG9CnBIkerLfn5t.mhAiSCmvc0AodE3nok0GG7v1iFjEmyGLeYA.VL
         IPN SENT: payment_type ==> instant
         IPN SENT: business ==> james_1202254981_biz@gmail.com
         IPN SENT: mc_gross_2 ==> 80.00
         IPN SENT: mc_gross_1 ==> 80.00
         IPN SENT: mc_fee ==> 4.65
         IPN SENT: transaction_subject ==> Shopping Cart
         IPN SENT: mc_shipping2 ==> 0.00
         IPN SENT: notify_version ==> 2.8
         IPN SENT: mc_currency ==> USD
         IPN SENT: mc_shipping1 ==> 0.00
         IPN SENT: custom ==> 
         IPN SENT: payment_fee ==> 4.65
         IPN SENT: payer_status ==> verified
         IPN SENT: mc_handling ==> 0.00
         IPN SENT: tax ==> 0.00
         IPN SENT: charset ==> windows-1252
         IPN SENT: tax2 ==> 0.00
         IPN SENT: tax1 ==> 0.00
         IPN SENT: item_number2 ==> 10014
         IPN SENT: item_number1 ==> 10013
         IPN SENT: shipping_discount ==> 0.00
         IPN SENT: invoice ==> 50011
         IPN SENT: mc_gross ==> 150.00
         IPN SENT: receiver_id ==> 339U3JVK2X4E6
         IPN SENT: txn_id ==> 8KH71840UC462913X
         IPN SENT: shipping_method ==> Default
         IPN SENT: num_cart_items ==> 2
         */
     }
 
     /**
      * Convert a string representation of a double to a double value
      * @param doubleString the string to convert
      * @return the double value represented by the string
      */
     private double paramToDouble(String doubleString) {
         double value = 0;
         if (doubleString != null) {
             value = new Double(doubleString).doubleValue();
         }
         return value;
     }
     
     /**
      * Open a JDBC database connection.
      */
     private static Connection getConnection() {
         Connection con = null;
         
         try {
             Class.forName(JDBC_DRIVER);
         } catch(java.lang.ClassNotFoundException e) {
             System.err.print("ClassNotFoundException: ");
             System.err.println(e.getMessage());
         }
 
         try {
             con = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
         } catch(SQLException ex) {
             System.err.println("SQLException: " + ex.getMessage());
         }
         
         return con;
     }
 }
