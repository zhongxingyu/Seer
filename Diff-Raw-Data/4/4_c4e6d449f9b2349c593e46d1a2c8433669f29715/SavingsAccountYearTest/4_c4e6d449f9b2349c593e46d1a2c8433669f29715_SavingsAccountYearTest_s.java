 package com.adamhaynes.finances;
 
import org.junit.Ignore;
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Adam.Haynes
  * Date: 31/07/12
  * Time: 12:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SavingsAccountYearTest{
 
     @Test
     public void startingBalance(){
         SavingsAccountYear account = new SavingsAccountYear(10000, 10);
         assertEquals(10000, account.startingBalance());
     }
 
     @Test
     public void endingBalance(){
         SavingsAccountYear account = new SavingsAccountYear(10000, 10);
         assertEquals(11000, account.endingBalance());
     }
 
     @Test
     public void nextYear(){
         SavingsAccountYear thisYear = new SavingsAccountYear(10000, 10);
         assertEquals(thisYear.endingBalance(), thisYear.nextYear().startingBalance());
     }
 
     @Test
    public void nextyearsInterestRateIsEqualToNextYearsInterestRate(){
         SavingsAccountYear thisYear = new SavingsAccountYear(10000, 10);
         assertEquals(thisYear.interestRate(), thisYear.nextYear().interestRate());
 
     }
 
 
 }
