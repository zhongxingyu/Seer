 /**
  * Swift Ticket -- Back End
  *
  * Copyright (C) 2013, Jonathan Gillett, Daniel Smullen, and Rayan Alfaheid
  * All rights reserved.
  *
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
 
 
 public class DailyTransactionsTest
 {
     /* Objects used for executing the test suite */
     private DailyTransactions transactions;
     
     /* Parameters used for the tests */
     private static final String logoutTransaction = "00_administrator___AA_000000.00";
     private static final String createTransaction = "01_newuser_________BS_000000.00";
     private static final String deleteTransaction = "02_buyer___________BS_000000.00";
     private static final String addcreditTransaction = "06_buyer___________BS_000010.00";
     private static final String refundTransaction = "05_buyer___________seller__________000005.00";
     private static final String sellTransaction = "03_The_Goosebumps______administrator___023_012.00";
     private static final String buyTransaction = "04_The_Godfather_______seller__________004_010.00";
     
     @Rule
     public final StandardErrorStreamLog log = new StandardErrorStreamLog();
     
     /**
      * Test method for {@link DailyTransactions#getTransactions()}.
      * 
      * Tests the getTransaction method in the DailyTransactions class, by 
      * executing the method. This method is a simple accessor method 
      * with only one return call.
      * @throws IOException 
      * @throws FatalError 
      */
     @Test
     public void testGetTransactions() throws FatalError, IOException
     {
         transactions = new DailyTransactions("files/DailyTransactionsTestGetTransactions.dtf");
         
         /* Verify that the transaction was loaded into memory */
         for (Transaction transaction : transactions.getTransactions())
         {
             assertEquals(logoutTransaction, transaction.getTransaction());
         }
     }
 
     /**
      * Test method for {@link DailyTransactions#load()}.
      * 
      * Tests the load method in the DailyTransactions class, by executing the 
      * DailyTransactions constructor, providing a nonexistent daily transaction 
      * file. Verify that a File IO Exception is thrown and caught. A Fatal Error 
      * exception must be thrown.
      * @throws IOException 
      */
     @Test
     public void testLoad1() throws IOException
     {
         /* Verify that a fatal error ExceptionCodes.DTF_NOT_FOUND is thrown */
         try
         {
             transactions = new DailyTransactions("files/InvalidFileName.dtf");
         }
         catch (FatalError e)
         {
             assertEquals(ExceptionCodes.DTF_NOT_FOUND.toString(), e.getMessage());
         }
     }
     
     /**
      * Test method for {@link DailyTransactions#load()}.
      * 
      * Tests the load method in the DailyTransactions class, by executing the 
      * DailyTransactions constructor, providing a valid daily transaction file 
      * with a valid transaction. Verify that the entry is added to the merged 
      * list of transactions.
      * @throws IOException 
      * @throws FatalError 
      */
     @Test
     public void testLoad2() throws FatalError, IOException
     {
         transactions = new DailyTransactions("files/DailyTransactionsTestGetTransactions.dtf");
         
         /* Verify that the transaction was loaded into memory */
         for (Transaction transaction : transactions.getTransactions())
         {
             assertEquals(logoutTransaction, transaction.getTransaction());
         }
     }
     
     /**
      * Test method for {@link DailyTransactions#load()}.
      * 
      * Tests the load method in the DailyTransactions class, by executing 
      * the DailyTransactions constructor, providing a valid daily transaction 
      * file with an invalid transaction. A Fatal Error exception must be thrown.
      * @throws IOException 
      * @throws FatalError 
      */
     @Test
     public void testLoad3() throws FatalError, IOException
     {
         /* Verify that a fatal error ExceptionCodes.CORRUPT_DTF is thrown */
         try
         {
             transactions = new DailyTransactions("files/DailyTransactionsTestLoad3.dtf");
         }
         catch (FatalError e)
         {
             assertEquals(ExceptionCodes.CORRUPT_DTF.toString(), e.getMessage());
         }
     }
     
     /**
      * Test method for {@link DailyTransactions#parse()}.
      * 
      * Tests the parse method for the DailyTransactions class by executing the 
      * DailyTransactions constructor, providing a valid merged daily transaction 
      * file, containing a logout transaction. Verify that the transaction is 
      * added to the collection of transactions successfully.
      * @throws IOException 
      * @throws FatalError 
      */
     @Test
     public void testParse1() throws FatalError, IOException
     {
         transactions = new DailyTransactions("files/DailyTransactionsTestParse1.dtf");
         
         /* Verify that the delete transaction was parsed and is in memory */
         for (Transaction transaction : transactions.getTransactions())
         {
             assertEquals(logoutTransaction, transaction.getTransaction());
         }
     }
     
     /**
      * Test method for {@link DailyTransactions#parse()}.
      * 
      * Tests the parse method for the DailyTransactions class by executing 
      * the DailyTransactions constructor, providing a valid merged daily 
      * transaction file, containing a create transaction. Verify that the 
      * transaction is added to the collection of transactions successfully.
      * @throws IOException 
      * @throws FatalError 
      */
     @Test
     public void testParse2() throws FatalError, IOException
     {
         transactions = new DailyTransactions("files/DailyTransactionsTestParse2.dtf");
         
         /* Verify that the delete transaction was parsed and is in memory */
         for (Transaction transaction : transactions.getTransactions())
         {
             assertEquals(createTransaction, transaction.getTransaction());
         }
     }
     
     /**
      * Test method for {@link DailyTransactions#parse()}.
      * 
      * Tests the parse method for the DailyTransactions class by executing 
      * the DailyTransactions constructor, providing a valid merged daily 
      * transaction file, containing a delete transaction. Verify that the 
      * transaction is added to the collection of transactions successfully.
      * @throws IOException 
      * @throws FatalError 
      */
     @Test
     public void testParse3() throws FatalError, IOException
     {
         transactions = new DailyTransactions("files/DailyTransactionsTestParse3.dtf");
         
         /* Verify that the delete transaction was parsed and is in memory */
         for (Transaction transaction : transactions.getTransactions())
         {
             assertEquals(deleteTransaction, transaction.getTransaction());
         }
     }
     
     /**
      * Test method for {@link DailyTransactions#parse()}.
      * 
      * Tests the parse method for the DailyTransactions class by executing the 
      * DailyTransactions constructor, providing a valid merged daily transaction 
      * file, containing a addcredit transaction. Verify that the transaction is 
      * added to the collection of transactions successfully.
      * @throws IOException 
      * @throws FatalError 
      */
     @Test
     public void testParse4() throws FatalError, IOException
     {
         transactions = new DailyTransactions("files/DailyTransactionsTestParse4.dtf");
         
         /* Verify that the addcredit transaction was parsed and is in memory */
         for (Transaction transaction : transactions.getTransactions())
         {
             assertEquals(addcreditTransaction, transaction.getTransaction());
         }
     }
     
     /**
      * Test method for {@link DailyTransactions#parse()}.
      * 
      * Tests the parse method for the DailyTransactions class by executing 
      * the DailyTransactions constructor, providing a valid merged daily 
      * transaction file, containing a refund transaction. Verify that the 
      * transaction is added to the collection of transactions successfully.
      * @throws IOException 
      * @throws FatalError 
      */
     @Test
     public void testParse5() throws FatalError, IOException
     {
         transactions = new DailyTransactions("files/DailyTransactionsTestParse5.dtf");
         
         /* Verify that the refund transaction was parsed and is in memory */
         for (Transaction transaction : transactions.getTransactions())
         {
             assertEquals(refundTransaction, transaction.getTransaction());
         }
     }
     
     /**
      * Test method for {@link DailyTransactions#parse()}.
      * 
      * Tests the parse method for the DailyTransactions class by executing 
      * the DailyTransactions constructor, providing a valid merged daily 
      * transaction file, containing a sell transaction. Verify that the 
      * transaction is added to the collection of transactions successfully.
      * @throws IOException 
      * @throws FatalError 
      */
     @Test
     public void testParse6() throws FatalError, IOException
     {
         transactions = new DailyTransactions("files/DailyTransactionsTestParse6.dtf");
         
         /* Verify that the sell transaction was parsed and is in memory */
         for (Transaction transaction : transactions.getTransactions())
         {
             assertEquals(sellTransaction, transaction.getTransaction());
         }
     }
     
     /**
      * Test method for {@link DailyTransactions#parse()}.
      * 
      * Tests the parse method for the DailyTransactions class by executing 
      * the DailyTransactions constructor, providing a valid merged daily 
      * transaction file, containing a buy transaction. Verify that the 
      * transaction is added to the collection of transactions successfully.
      * @throws IOException 
      * @throws FatalError 
      */
     @Test
     public void testParse7() throws FatalError, IOException
     {
         transactions = new DailyTransactions("files/DailyTransactionsTestParse7.dtf");
         
         /* Verify that the buy transaction was parsed and is in memory */
        for (Transaction transaction : transactions.getTransactions())
        {
            assertEquals(buyTransaction, transaction.getTransaction());
        }
     }
 }
