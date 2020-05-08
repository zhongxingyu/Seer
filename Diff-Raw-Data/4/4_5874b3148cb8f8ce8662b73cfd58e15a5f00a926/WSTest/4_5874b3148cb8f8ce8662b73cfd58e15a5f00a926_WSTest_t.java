 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /*
  * Created on Dec 18, 2003
  *
  * Copyright Sapienter Enterprise Software
  */
 package com.sapienter.jbilling.server.item;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.api.JbillingAPI;
 import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
 
 /**
  * @author Emil
  */
 public class WSTest  extends TestCase {
       
 
     public void testCreate() {
         try {
         	JbillingAPI api = JbillingAPIFactory.getAPI();
             /*
              * Create
              */
             ItemDTOEx newItem = new ItemDTOEx();
             newItem.setDescription("an item from ws");
             newItem.setPrice(new BigDecimal("29.5"));
             
             
             Integer types[] = new Integer[1];
             types[0] = new Integer(1);
             newItem.setTypes(types);
             newItem.setPriceManual(new Integer(0));
             
             System.out.println("Creating item ...");
             Integer ret = api.createItem(newItem);
             assertNotNull("The item was not created", ret);
             System.out.println("Done!");
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
     
     public void testPricingRules() {
     	
     	try {
     		System.out.println("Testing Pricing Rules");
     		JbillingAPI api = JbillingAPIFactory.getAPI();
     		
     		// Tests item pricing for user "gandalf" (id 2)
     		PricingField pf = new PricingField("newPrice", new BigDecimal("50.0"));
     		ItemDTOEx it = api.getItem(new Integer(1), new Integer(2), new PricingField[] { pf });
             assertEquals(new BigDecimal("50.0"), it.getPrice());
     		System.out.println("Pricing field test passed");
     		
     		// Tests access to an item of a different entity
     		boolean passed = false;
         	try {
         		// Try to get item 4 (should fail because the user is on entity 1 and 
         		// the item is on entity 2).
         		it = api.getItem(new Integer(4), new Integer(2), new PricingField[] { pf });
         	} catch (Exception e) {
         		passed = true;
         	}
 
         	if (!passed) {
         		fail("Security check failed, should not access Item from another entity");
         	} else {
         		System.out.println("Security check passed for item retrieval");
         	}
         	System.out.println("Done!");
         	
     	} catch (Exception e) {
     		e.printStackTrace();
     		fail("Exception caught: " + e);
     	}
     }
     
     private OrderWS prepareOrder() {
     	
     	OrderWS newOrder = new OrderWS();
         newOrder.setUserId(new Integer(2)); 
         newOrder.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         newOrder.setPeriod(new Integer(1)); // once
         newOrder.setCurrencyId(new Integer(1));
         
         // now add some lines
         OrderLineWS lines[] = new OrderLineWS[2];
         OrderLineWS line;
         
         line = new OrderLineWS();
         line.setPrice(new BigDecimal("10.00"));
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(1));
         line.setAmount(new BigDecimal("10.00"));
         line.setDescription("Fist line");
         line.setItemId(new Integer(1));
         lines[0] = line;
         
         // this is an item line
         line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(1));
         line.setItemId(new Integer(2));
         // take the description from the item
         line.setUseItem(new Boolean(true));
         lines[1] = line;
         
         newOrder.setOrderLines(lines);
         return newOrder;
     }
     
     public void testOrderRating() {
     	
     	try {
     		System.out.println("Testing Order Rating");
     		JbillingAPI api = JbillingAPIFactory.getAPI();
     		
     		// Tests item pricing for user "gandalf" (id 2)
     		PricingField add = new PricingField("add", new BigDecimal("10.0"));
     		PricingField subtract = new PricingField("subtract", new BigDecimal("1.0"));
     		
     		System.out.println("Testing pricing fields on order rating 1");
     		// rate an order, use "add" pricing field rule (adds 10 to price in all items of order)
     		OrderWS newOrder = prepareOrder();
             newOrder.setPricingFields(PricingField.setPricingFieldsValue(new PricingField[] { add }));
             OrderWS order = api.rateOrder(newOrder);
             OrderLineWS[] l = order.getOrderLines();
             assertNotNull(l);
             assertTrue(l.length == 2);
             assertEquals(new BigDecimal("20.00"), l[0].getPriceAsDecimal());
             assertEquals(new BigDecimal("40.00"), l[1].getPriceAsDecimal());
             
             System.out.println("Testing pricing fields on order rating 2");
             // rate the same order, but using "subtract" (minus 1 to price in all items of order)
             newOrder = prepareOrder();
             newOrder.setPricingFields(PricingField.setPricingFieldsValue(new PricingField[] { subtract }));
             order = api.rateOrder(newOrder);
             l = order.getOrderLines();
             assertNotNull(l);
             assertEquals(l.length, 2);
             assertEquals(new BigDecimal("9.00"), l[0].getPriceAsDecimal());
             assertEquals(new BigDecimal("29.00"), l[1].getPriceAsDecimal());
 
     		System.out.println("Testing double rating with both orders in one shot");
     		// rate an order, use "add" pricing field rule (adds 10 to price in all items of order)
     		OrderWS newOrder1 = prepareOrder();
             newOrder1.setPricingFields(PricingField.setPricingFieldsValue(new PricingField[] { add }));
 
             OrderWS newOrder2 = prepareOrder();
             newOrder2.setPricingFields(PricingField.setPricingFieldsValue(new PricingField[] { subtract }));
 
             OrderWS orders[] = api.rateOrders(new OrderWS[] {newOrder1, newOrder2});
             l = orders[0].getOrderLines();
             assertNotNull(l);
             assertTrue(l.length == 2);
             assertEquals(new BigDecimal("20.00"), l[0].getPriceAsDecimal());
             assertEquals(new BigDecimal("40.00"), l[1].getPriceAsDecimal());
 
 
             l = orders[1].getOrderLines();
             assertNotNull(l);
             assertEquals(l.length, 2);
             assertEquals(new BigDecimal("9.00"), l[0].getPriceAsDecimal());
             assertEquals(new BigDecimal("29.00"), l[1].getPriceAsDecimal());
 
     		System.out.println("Done!");
         	
     	} catch (Exception e) {
     		e.printStackTrace();
     		fail("Exception caught: " + e);
     	}
     }
 
     public void testGetAllItems() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
         
             /*
              * Get all items
              */             
             System.out.println("Getting all items");
             ItemDTOEx[] items =  api.getAllItems();
             Arrays.sort(items, new Comparator<ItemDTOEx>() {
             	public int compare(ItemDTOEx i1, ItemDTOEx i2) {
             		return i1.getId().compareTo(i2.getId());
             	}
             });
             assertNotNull("The items were not retrieved", items);
             assertEquals("Wrong number of items", 19, items.length);
 
             assertEquals("Description", "Lemonade - 1 per day monthly pass", items[0].getDescription());
             assertEquals("Price", new BigDecimal("10"), items[0].getPrice());
             assertEquals("Price List", new BigDecimal("10"), (getCurrencyPrice(items[0].getPrices(), 1).getPrice()));
             assertEquals("ID", new Integer(1), items[0].getId());
             assertEquals("Number", "DP-1", items[0].getNumber());
             assertEquals("Type 1", new Integer(1), items[0].getTypes()[0]);
 
             assertEquals("Description", "Lemonade - all you can drink monthly", items[1].getDescription());
             assertEquals("Price", new BigDecimal("20"), items[1].getPrice());
             assertEquals("Price List", new BigDecimal("20"), (getCurrencyPrice(items[1].getPrices(), 1).getPrice()));
             assertEquals("ID", new Integer(2), items[1].getId());
             assertEquals("Number", "DP-2", items[1].getNumber());
             assertEquals("Type 1", new Integer(1), items[1].getTypes()[0]);
 
             assertEquals("Description", "Coffee - one per day - Monthly", items[2].getDescription());
             assertEquals("Price", new BigDecimal("15"), items[2].getPrice());
             assertEquals("Price List", new BigDecimal("15"), (getCurrencyPrice(items[2].getPrices(), 1).getPrice()));
             assertEquals("ID", new Integer(3), items[2].getId());
             assertEquals("Number", "DP-3", items[2].getNumber());
             assertEquals("Type 1", new Integer(1), items[2].getTypes()[0]);
 
             assertEquals("Description", "10% Elf discount.", items[3].getDescription());
             assertEquals("Percentage", new BigDecimal("-10.00"), items[3].getPercentage());
             assertEquals("ID", new Integer(14), items[3].getId());
             assertEquals("Number", "J-01", items[3].getNumber());
             assertEquals("Type 12", new Integer(12), items[3].getTypes()[0]);
 
             assertEquals("Description", "Cancel fee", items[4].getDescription());
             assertEquals("Price", new BigDecimal("5"), items[4].getPrice());
             assertEquals("ID", new Integer(24), items[4].getId());
             assertEquals("Number", "F-1", items[4].getNumber());
             assertEquals("Type 22", new Integer(22), items[4].getTypes()[0]);
 
             // item at index 5 tested in testCurrencyConvert() below
 
             // this is alwyas the last item
             int lastItem = items.length - 1;
             assertEquals("Description", "an item from ws", items[lastItem].getDescription());
             assertEquals("Price", new BigDecimal("29.5"), items[lastItem].getPrice());
             assertEquals("Price List", new BigDecimal("29.5"), (getCurrencyPrice(items[lastItem].getPrices(), 1).getPrice()));
             assertEquals("Type 1", new Integer(1), items[lastItem].getTypes()[0]);
 
             System.out.println("Done!");
             
             
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
     
     public void testUpdateItem() {
     
     	try {
     		JbillingAPI api = JbillingAPIFactory.getAPI();
 	    	
     		System.out.println("Getting item");
 	    	ItemDTOEx item = api.getItem(new Integer(1), new Integer(2), new PricingField[] {} );
 	    	String description = item.getDescription();
 	    	Integer prMan = item.getPriceManual();
 	    	String number = item.getNumber();
	    	BigDecimal price = new BigDecimal(item.getPrice());
	    	BigDecimal perc = new BigDecimal(item.getPercentage());
 	    	String promo = item.getPromoCode();
 	
 	    	System.out.println("Changing properties");
 	    	item.setDescription("Another description");
 	    	item.setPriceManual(new Integer(1));
 	    	item.setNumber("NMR-01");
 	    	item.setPrice(new BigDecimal("1.00"));
 	    	
 	    	System.out.println("Updating item");
 	    	api.updateItem(item);
 	    
 	    	ItemDTOEx itemChanged = api.getItem(new Integer(1), new Integer(2), new PricingField[] {} );
 	    	assertEquals(itemChanged.getDescription(), "Another description");
 	    	assertEquals(itemChanged.getPriceManual(), new Integer(1));
 	    	assertEquals(itemChanged.getNumber(), "NMR-01");
 	    	assertEquals(itemChanged.getPrice(), price);
 	    	assertEquals(itemChanged.getPercentage(), perc);
 	    	assertEquals(itemChanged.getPromoCode(), promo);
 	    	System.out.println("Done!");
 	    
 	    	System.out.println("Restoring initial item state.");
 	    	item.setDescription(description);
 	    	item.setPriceManual(prMan);
 	    	item.setNumber(number);
 	    	api.updateItem(item);
 	    	System.out.println("Done!");
 
     	} catch (Exception e) {
     		e.printStackTrace();
     		fail("Exception caught:" + e);
     	}
 	}
 
     public void testCurrencyConvert() {
     	try {
     		JbillingAPI api = JbillingAPIFactory.getAPI();
 
             ItemDTOEx item = api.getItem(new Integer(240), new Integer(2), new PricingField[] {} );
 
             assertEquals("Price in USD", 1, item.getCurrencyId().intValue());
             assertEquals("Converted price AUD->USD", new BigDecimal("10.0"), item.getPrice());
             assertEquals("Price List size", 2, item.getPrices().size());
 
             ItemPriceDTOEx priceUSD = getCurrencyPrice(item.getPrices(), 1);
             assertEquals("USD currency", priceUSD.getCurrencyId().intValue(), 1);
             assertEquals("USD price", priceUSD.getPrice(), null);
 
             ItemPriceDTOEx priceAUD = getCurrencyPrice(item.getPrices(), 11);
             assertEquals("AUD currency", priceAUD.getCurrencyId().intValue(), 11);
             assertEquals("AUD price", priceAUD.getPrice(), new BigDecimal("15.0"));
 
     	} catch (Exception e) {
     		e.printStackTrace();
     		fail("Exception caught:" + e);
     	}
     }
 
     private ItemPriceDTOEx getCurrencyPrice(List prices, int currencyId) {
         Iterator iter = prices.iterator();
         while (iter.hasNext()) {
             ItemPriceDTOEx itemPrice = (ItemPriceDTOEx) iter.next();
             if (itemPrice.getCurrencyId().intValue() == currencyId) {
                 return itemPrice;
             }
         }
         return null;
     }
 
     public void testGetAllItemCategories() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         ItemTypeWS[] types = api.getAllItemCategories();
 
         assertEquals("6 item types", 6, types.length);
 
         assertEquals(1, types[0].getId().intValue());
         assertEquals("Drink passes", types[0].getDescription());
     }
 
     public void testCreateItemCategory() throws Exception {
         try {
 
             String description = "Ice creams (WS test)";
 
             System.out.println("Getting API...");
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             ItemTypeWS itemType = new ItemTypeWS();
             itemType.setDescription(description);
             itemType.setOrderLineTypeId(1);
 
             System.out.println("Creating item category '" + description + "'...");
             Integer itemTypeId = api.createItemCategory(itemType);
             assertNotNull(itemTypeId);
             System.out.println("Done.");
 
             System.out.println("Getting all item categories...");
             ItemTypeWS[] types = api.getAllItemCategories();
 
             boolean addedFound = false;
             for(int i = 0; i < types.length; ++i) {
                 if(description.equals(types[i].getDescription())) {
                     System.out.println("Test category was found. Creation was completed successfully.");
                     addedFound = true;
                     break;
                 }
             }
             assertTrue("Ice cream not found.", addedFound);
             System.out.println("Test completed!");
 
         } catch (Exception e) {
     		e.printStackTrace();
     		fail("Exception caught:" + e);
     	}
     }
 
     public void testUpdateItemCategory() throws Exception {
         try {
 
             Integer categoryId;
             String originalDescription;
             String description = "Drink passes (WS test)";
 
             System.out.println("Getting API...");
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             System.out.println("Getting all item categories...");
             ItemTypeWS[] types = api.getAllItemCategories();
 
             System.out.println("Changing description...");
             categoryId = types[0].getId();
             originalDescription = types[0].getDescription();
             types[0].setDescription(description);
             api.updateItemCategory(types[0]);
 
             System.out.println("Getting all item categories...");
             types = api.getAllItemCategories();
             System.out.println("Verifying description has changed...");
             for(int i = 0; i < types.length; ++i) {
                 if(categoryId.equals(types[i].getId())) {
                     assertEquals(description, types[i].getDescription());
 
                     System.out.println("Restoring description...");
                     types[i].setDescription(originalDescription);
                     api.updateItemCategory(types[i]);
                     break;
                 }
             }
 
             System.out.println("Test completed!");
 
         } catch (Exception e) {
     		e.printStackTrace();
     		fail("Exception caught:" + e);
     	}
     }
 
     public void testGetItemsByCategory() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         final Integer DRINK_ITEM_CATEGORY_ID = 2;
 
         ItemDTOEx[] items = api.getItemByCategory(DRINK_ITEM_CATEGORY_ID);
 
         assertEquals("1 item in category 2", 1, items.length);
         assertEquals(4, items[0].getId().intValue());
         assertEquals("Poison Ivy juice (cold)", items[0].getDescription());
     }
 
     public static void assertEquals(BigDecimal expected, BigDecimal actual) {
         assertEquals(null, expected, actual);
     }
 
     public static void assertEquals(String message, BigDecimal expected, BigDecimal actual) {
         assertEquals(message,
                      (Object) (expected == null ? null : expected.setScale(2, RoundingMode.HALF_UP)),
                      (Object) (actual == null ? null : actual.setScale(2, RoundingMode.HALF_UP)));
     }
 }
