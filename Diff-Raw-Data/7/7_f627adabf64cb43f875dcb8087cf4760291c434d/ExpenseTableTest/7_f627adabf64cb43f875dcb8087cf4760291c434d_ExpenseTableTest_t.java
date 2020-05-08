 package com.svanberg.household.web.expense;
 
 import com.svanberg.household.service.ExpenseService;
 import com.svanberg.household.web.test.SpringWicketTest;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 import static org.junit.Assert.assertNotNull;
 
 /**
  * @author Andreas Svanberg (andreass) <andreas.svanberg@mensa.se>
  */
 public class ExpenseTableTest extends SpringWicketTest
 {
 
     @Mock ExpenseService expenseService;
 
     private ExpenseTable panel;
 
     @Before
     public void setUp() throws Exception {
         panel = tester().startComponentInPage(ExpenseTable.class);
     }
 
     @Test
     public void testRenders() throws Exception {
         assertNotNull("Does not render", panel);
     }
 }
