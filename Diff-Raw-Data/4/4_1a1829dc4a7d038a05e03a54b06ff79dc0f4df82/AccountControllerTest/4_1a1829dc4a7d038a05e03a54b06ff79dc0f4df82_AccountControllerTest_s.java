 /*
  * Copyright (C) 2012 Alexandre Thomazo
  *
  * This file is part of BankIt.
  *
  * BankIt is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * BankIt is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with BankIt. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.alexlg.bankit.controllers;
 
 import static org.junit.Assert.assertEquals;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.alexlg.bankit.dao.AbstractDaoTest;
 import org.alexlg.bankit.dao.CostDao;
 import org.alexlg.bankit.dao.OperationDao;
 import org.alexlg.bankit.db.Category;
 import org.alexlg.bankit.db.Operation;
 import org.joda.time.LocalDate;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  * Test class for the account controller.
  * 
  * @author Alexandre Thomazo
  */
 public class AccountControllerTest extends AbstractDaoTest {
 
 	@Autowired
 	private AccountController accountController;
 	
 	@Autowired
 	private OperationDao operationDao;
 	
 	@Autowired
 	private CostDao costDao;
 	
 	/**
 	 * Test the building of the list containing MonthOps
 	 */
 	@Test
 	public void testBuildFutureOps() throws Exception {
 		LocalDate day = new LocalDate(2012, 8, 25);
 		Calendar calDay = Calendar.getInstance();
 		calDay.setTime(day.toDate());
 		
 		Set<MonthOps> futureOps = accountController.buildFutureOps(day,
 				operationDao.getFuture(calDay), 
 				costDao.getList(), 
 				new BigDecimal("22.12"),
 				1);
 		
 		//checking
 		assertEquals("nb month", 2, futureOps.size());
 		
 		int i = 0;
 		for (MonthOps monthOps : futureOps) {
 			Set<Operation> ops = monthOps.getOps();
 			if (i == 0) {
 				//current month
 				assertEquals("nb op", 2, ops.size());
 				int j = 0;
 				for (Operation op : ops) {
 					String label = null;
 					switch (j) {
 						case 0: 
 							label = "Impots Revenu";
 							assertEquals("date", new LocalDate(2012, 8, 27), new LocalDate(op.getOperationDate()));
 							break;
 							
 						case 1: label = "ASSURANCE"; break;
 					}
 					assertEquals("label", label, op.getLabel());
 					j++;
 				}
 			} else if (i == 1) {
 				//next month
 				assertEquals("nb op", 4, ops.size());
 				int j = 0;
 				for (Operation op : ops) {
 					String label = null;
 					int opDay = 0;
 					switch (j) {
 						case 0: label = "VIR LOYER"; opDay = 1; break;
 						case 1: label = "PRLV Assurance Auto"; opDay = 3; break;
 						case 2: label = "PRLV Free Mobile"; opDay = 24; break;
 						case 3: label = "VIR SALAIRE"; opDay = 27; break;
 					}
 					assertEquals("label", label, op.getLabel());
 					assertEquals("date", new LocalDate(2012, 9, opDay), new LocalDate(op.getOperationDate()));
 					j++;
 				}
 			}
 			
 			i++;
 		}
 	}
 	
 	@Test
 	public void testBuildCategories() throws Exception {
 		LocalDate day = new LocalDate(2012, 8, 25);
 		Map<Date, Map<Category, BigDecimal>> categories = accountController.buildCategories(day, 1);
 		
 		//we expected a map with the following structure
 		// - 2012-07 :
 		//		- Carburant : -73.07
 		// - 2012-08 :
 		//		- Alimentation : -140.39
 		//		- Communications : -49.98
 		
 		//as we already test the DAO method for month summary, we just check the size
 		
 		assertEquals("Categories size", 2, categories.size());
 		
 		int i = 0;
 		for (Entry<Date, Map<Category, BigDecimal>> entry : categories.entrySet()) {
 			LocalDate month = new LocalDate(entry.getKey());
 			Map<Category, BigDecimal> cat = entry.getValue();
 			
 			if (i == 0) {
 				assertEquals("First month", 7, month.getMonthOfYear());
 				assertEquals("First year", 2012, month.getYear());
 				assertEquals("First size", 1, cat.size());
 			} else if (i == 1) {
 				assertEquals("Second month", 8, month.getMonthOfYear());
 				assertEquals("Second year", 2012, month.getYear());
				assertEquals("Second size", 2, cat.size());
 			}
 			
 			i++;
 		}
 	}
 }
