 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.redhat.nitrate;
 
 import com.redhat.nitrate.command.TestCase;
 import com.redhat.nitrate.command.TestCaseRun;
 import com.redhat.nitrate.command.TestPlan;
 import com.redhat.nitrate.command.TestRun;
 import com.redhat.nitrate.command.Auth;
 import com.redhat.nitrate.command.Build;
 import com.redhat.nitrate.command.Product;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import redstone.xmlrpc.XmlRpcFault;
 import redstone.xmlrpc.XmlRpcStruct;
 
 /**
  *
  * @author asaleh
  */
 public class TcmsConnectionTest {
     private Integer testRunId = 36898;
     private Integer testCaseId = 154226;
     private Integer testPlanId = 5866;
     private Integer buildId = 772;
     private Integer productId = 243;
     private Integer testCaseRunId = 1039185;
 
     public TcmsConnectionTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     /**
      * Test of Auth_login method, of class TcmsConnection.
      */
     @Test
    public void testConnect() throws MalformedURLException, IOException {
              TcmsConnection connection = new TcmsConnection("https://tcms.engineering.redhat.com/xmlrpc");
               connection.setUsernameAndPassword(PrivatePassword.name, PrivatePassword.password);
               assertTrue(connection.testTcmsConnection());
     }
     
     /**
      * Test of Auth_login method, of class TcmsConnection.
      */
     @Test
     public void testLogin() throws MalformedURLException {
         try {
             TcmsConnection connection = new TcmsConnection("https://tcms.engineering.redhat.com/xmlrpc/");
             connection.setUsernameAndPassword(PrivatePassword.name, PrivatePassword.password);
             Auth.login_krbv login = new Auth.login_krbv();
             String session = login.invoke(connection);
             assertTrue(session.length() > 0);
             connection.setSession(session);
             
             
             
             connection.invoke(new Auth.logout());
         } catch (TcmsException ex) {
             Logger.getLogger(TcmsConnectionTest.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Test of testTcmsConnection method, of class TcmsConnection.
      */
     @Test
     public void testTestTcmsConnection() throws Exception {
     }
 
     /**
      * Test of setSession method, of class TcmsConnection.
      */
     @Test
     public void testSetSession() {
     }
 
     /**
      * Test of setUsernameAndPassword method, of class TcmsConnection.
      */
     @Test
     public void testSetUsernameAndPassword() {
     }
 
     /**
      * Test of hashtableToFields method, of class TcmsConnection.
      */
     @Test
     public void testHashtableToFields() throws Exception {
     }
 
     /**
      * Test of rpcStructToFields method, of class TcmsConnection.
      */
     @Test
     public void testRpcStructToFields() throws MalformedURLException  {
         try{
             TcmsConnection connection = new TcmsConnection("https://tcms.engineering.redhat.com/xmlrpc/");
             connection.setUsernameAndPassword(PrivatePassword.name, PrivatePassword.password);
             Auth.login_krbv login = new Auth.login_krbv();
             String session = login.invoke(connection);
             assertTrue(session.length() > 0);
             connection.setSession(session);
         
             // test getting TestPlan
             testRpcStructToFieldsTestPlan(connection);
         
             // test getting Build
             testRpcStructToFieldsBuild(connection);
             
             // test getting Product
             testRpcStructToFieldsProduct(connection);
             
             // test getting TestRun
             testRpcStructToFieldsTestRun(connection);
             
             // test getting TestCase
             testRpcStructToFieldsTestCase(connection);
             
             // test getting TestCaseRun
             testRpcStructToFieldsTestCaseRun(connection);
             
             connection.invoke(new Auth.logout());
         } catch (TcmsException ex){
             Logger.getLogger(TcmsConnectionTest.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
                 Logger.getLogger(TcmsConnectionTest.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             Logger.getLogger(TcmsConnectionTest.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     private void testRpcStructToFieldsTestPlan(TcmsConnection connection) 
             throws  TcmsException, IllegalAccessException, InstantiationException{
         
         TestPlan planResult;
         TestPlan.get get = new TestPlan.get();
         get.id = testPlanId;
         Object o = connection.invoke(get);
         if (o instanceof XmlRpcStruct) {
             planResult = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, TestPlan.class);
         }
         
     }
     
     private void testRpcStructToFieldsBuild(TcmsConnection connection) throws IllegalAccessException, InstantiationException, TcmsException{
        
         Build buildResult;
         Build.get get = new Build.get();
         get.id = buildId;
         Object o = connection.invoke(get);
         if (o instanceof XmlRpcStruct) {
             buildResult = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, Build.class);
         }                     
     }
     
     private void testRpcStructToFieldsProduct(TcmsConnection connection) throws IllegalAccessException, InstantiationException, TcmsException{
         
         Product p;
         Product.get get = new Product.get();
         get.id = productId;
         Object o = connection.invoke(get);
         if (o instanceof XmlRpcStruct) {
             p = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, Product.class);
         }                     
     }
     
     private void testRpcStructToFieldsTestRun(TcmsConnection connection) throws IllegalAccessException, InstantiationException, TcmsException{
         
         TestRun t;
         TestRun.get get = new TestRun.get();
         get.run_id = testRunId;
         Object o = connection.invoke(get);
         if (o instanceof XmlRpcStruct) {
             t = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, TestRun.class);
         }                     
     }
     
     private void testRpcStructToFieldsTestCase(TcmsConnection connection) throws IllegalAccessException, InstantiationException, TcmsException{
         
         TestCase t;
         TestCase.get get = new TestCase.get();
         get.case_id_integer = testCaseId;
         Object o = connection.invoke(get);
         if (o instanceof XmlRpcStruct) {
             t = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, TestCase.class);
         }                     
     }
     private void testRpcStructToFieldsTestCaseRun(TcmsConnection connection) throws IllegalAccessException, InstantiationException, TcmsException{
         
         TestCaseRun t;
         TestCaseRun.get get = new TestCaseRun.get();
         get.case_run_id = testCaseRunId;
         Object o = connection.invoke(get);
         if (o instanceof XmlRpcStruct) {
             t = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, TestCaseRun.class);
             int a = 12;
         }                     
     }
     
     
 
     /**
      * Test of fieldsToHashtable method, of class TcmsConnection.
      */
     @Test
     public void testFieldsToHashtable() throws Exception {
     }
 
     /**
      * Test of commandToString method, of class TcmsConnection.
      */
     @Test
     public void testCommandToString() throws Exception {
     }
 
     /**
      * Test of getName method, of class TcmsConnection.
      */
     @Test
     public void testGetName() {
     }
 
     /**
      * Test of fieldsToCollection method, of class TcmsConnection.
      */
     @Test
     public void testFieldsToCollection() throws Exception {
     }
 
     /**
      * Test of commandToParams method, of class TcmsConnection.
      */
     @Test
     public void testCommandToParams() throws Exception {
     }
 
     /**
      * Test of invoke method, of class TcmsConnection.
      */
     @Test
     public void testInvoke() throws Exception {
     }
 
 }
