 /*
  * Copyright (C) 2010 The AdoHive Team
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package de.unistuttgart.iste.se.adohive.controller.test;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import de.unistuttgart.iste.se.adohive.controller.IAdoHiveManager;
 import de.unistuttgart.iste.se.adohive.controller.IFinancialCategoryManager;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IFinancialCategory;
 import de.unistuttgart.iste.se.adohive.model.test.IndependentTestDataProvider;
 
 /**
  * 
  * @author Felix
  *
  * inherit test classes for implementations of IFinancialCategoryManager
  * from this abstract class
  */
 public abstract class IFinancialCategoryManagerTest extends IAdoHiveManagerTest<IFinancialCategory> {
 
 	@Override
 	protected int getItemId(IFinancialCategory item) {
 		return item.getId();
 	}
 
 	@Override
 	protected List<Object> getItemKeys(IFinancialCategory item) {
 		ArrayList<Object> list = new ArrayList<Object>();
 		list.add(item.getId());
 		return list;
 	}
 
 	@Override
 	public IFinancialCategory newE()  throws AdoHiveException {
 		return getTestDataProvider().newIFinancialCategory();
 	}
 	
 	@Override
 	protected boolean itemClassHasId() {
 		return true;
 	}
 
 	@Override
 	protected void modifyItem(IFinancialCategory item) {
 		if (item.getYear() == 1984) {
 			item.setYear((short) 1337);
 		} else {
 			item.setYear((short) 1984);
 		}
 		
 		int arraylength = IndependentTestDataProvider.getRandom().nextInt(20);
 		item.setCostUnits(IndependentTestDataProvider.randomIntArray(arraylength));
 		item.setBudgetCosts(IndependentTestDataProvider.randomIntArray(arraylength));
 		
 	}
 	
 	@Override
 	protected IAdoHiveManager<IFinancialCategory> getInstance() {
 		try {
 			getController().getFinancialCategoryManager().clear();
 		} catch (AdoHiveException e) {
 			fail("financial category manager could not be cleaned!");
 		}
 		return getController().getFinancialCategoryManager();
 	}
 	
 	@Test
 	public void testFinancialCategorySpecials() throws AdoHiveException{
 		IFinancialCategoryManager instance = (IFinancialCategoryManager) getInstance();
 		IFinancialCategory i1 = newE();
 		IFinancialCategory i2 = newE();
 		i1.setCostUnits(new Integer[]{1});
 		i2.setCostUnits(new Integer[]{1});
 		instance.add(i1);
 		instance.add(i2);
 		assertEquals(2,instance.getAll().size());
 		i2.setCostUnits(new Integer[]{1,2});
 		instance.update(i2);
 		i1.setCostUnits(new Integer[]{1,2});
 		instance.update(i1);
 		List<IFinancialCategory> getall = instance.getAll();
 		assertEquals(2,getall.size());
 		assertEquals(2,getall.get(0).getCostUnits().length);
 		assertEquals(2,getall.get(0).getBudgetCosts().length);
 	}
 }
