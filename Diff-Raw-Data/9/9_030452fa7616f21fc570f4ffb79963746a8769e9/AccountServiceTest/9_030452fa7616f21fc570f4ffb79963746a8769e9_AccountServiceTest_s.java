 package org.jz.services;
 
 import org.junit.Test;
 import org.jz.domain.CustomerId;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * @author Kristian Rosenvold
  */
 public class AccountServiceTest
 {
     private final AccountService accountService = new AccountService();
     private final CustomerId high_credit = new CustomerId( 3000 );
 
 
     private final CustomerId id = new CustomerId( 123 );
 
    private final int systemMaxCredit = 2000;
 
     @Test
     public void testGetCreditLimit()
     {
        final int creditLimit = accountService.getCreditLimit( id, systemMaxCredit );
         assertEquals( 17, creditLimit);
     }
 
     @Test
     public void testWithMax()
     {
        final int creditLimit = accountService.getCreditLimit( high_credit, systemMaxCredit );
        assertEquals( systemMaxCredit, creditLimit);
     }
 
 }
