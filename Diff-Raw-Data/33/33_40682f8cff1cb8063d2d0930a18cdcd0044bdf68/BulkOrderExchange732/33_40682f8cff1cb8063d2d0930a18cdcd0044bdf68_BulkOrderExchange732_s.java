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
 
 package com.sapienter.jbilling.tieto;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 
 import junit.framework.TestCase;
 
 import com.sapienter.jbilling.server.entity.CreditCardDTO;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.user.ContactWS;
 import com.sapienter.jbilling.server.user.UserWS;
 import com.sapienter.jbilling.server.util.api.JbillingAPI;
 import com.sapienter.jbilling.server.util.api.JbillingAPIException;
 import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
 
 /**
  * Unit test for the Tieto - TAG requirement:
  * #731 - "Request for createUser example".
  */
 public class BulkOrderExchange732 extends TestCase {
 
     public static final Integer SIM_PERIODIC_ITEM_ID = 3401;
     public static final Integer SIM_ONE_TIME_ITEM_ID = 3404;
     public static final Integer SIM_PLAN_ITEM_ID = 3500;
 
     public BulkOrderExchange732() {
         super();
     }
 
     public BulkOrderExchange732(String name) {
         super(name);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Creates parent customer and a SIM child account.
      */
     public void testCreateUser() {
         
         System.out.println("#732 - \"Bulk order exchange\"");
 
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             /* Create the parent customer with credit card info. */
 
             UserWS customer = generateUser("test-customer-732");
             // user is a parent
             customer.setIsParent(true);
             // add a credit card
             customer.setCreditCard(generateCreditCard("4111111111111152"));
 
             // make the call and save the returned user id
             System.out.println("Creating parent user ...");
             customer.setUserId(api.createUser(customer));
 
             // check the user was created
             assertNotNull("The user was not created", customer.getUserId());
             System.out.println("User was created. Id: " + 
                     customer.getUserId());
 
 
             /* Create the SIM child user account (MSISDN 12345678912345). */
 
             UserWS sim = generateUser("12345678912345");
             sim.setIsParent(new Boolean(false));
             sim.setParentId(customer.getUserId());
             // customer receives invoices
             sim.setInvoiceChild(Boolean.FALSE); 
 
             // make the call and save the returned user id
             System.out.println("Creating SIM user ...");
             sim.setUserId(api.createUser(sim));
 
             // check the user was created
             assertNotNull("The sim user was not created", sim.getUserId());
             System.out.println("SIM user was created. Id: " + 
                     sim.getUserId());
 
 
             /* New main monthly order (with plan) to SIM account */
 
             OrderWS simMainSubscriptionOrder = generateOrder(
                     sim.getUserId(), SIM_PLAN_ITEM_ID);
             // make this the main subscription order
             simMainSubscriptionOrder.setIsCurrent(1);
 
             // make the call
             System.out.println("Creating main subscription order for SIM account.");
             Integer simOrderId = api.createOrder(simMainSubscriptionOrder);
             assertNotNull("The order was not created", simOrderId);
 
 
             /* Update monthly order of customer account with new SIM
                peroidic charge */
 
             // Note: since this customer doesn't have an existing 
             // monthly subscription order, a new one will be created now.
             // If the order already exists and the id is known, 
             // fetch with getOrder(), update or create a new line, 
             // then call updateOrder(). If the id isn't know, use
             // getOrderByPeriod(userId, 2) (which returns ids) then 
             // getOrder() for each id and check for getIsCurrent == 1 &&
             // getStatus == 1
             
             OrderWS customerMainSubscriptionOrder = generateOrder(
                     customer.getUserId(), SIM_PERIODIC_ITEM_ID);
             // make this the main subscription order
             customerMainSubscriptionOrder.setIsCurrent(1);
 
             // make the call
             System.out.println("Creating main subscription order for customer account.");
             Integer customerOrderId = api.createOrder(
                     customerMainSubscriptionOrder);
             assertNotNull("The order was not created", customerOrderId);
 
 
             /* Update current order of customer account with new SIM
                one-time charge */
 
             // Create order line for update. 
             OrderLineWS newLine = new OrderLineWS();
             newLine.setTypeId(1); // Item line type
             newLine.setItemId(SIM_ONE_TIME_ITEM_ID);
             newLine.setQuantity(new Integer(1));
             // take the price and description from the item
             newLine.setUseItem(new Boolean(true));
 
             /* Updating current order. */
             System.out.println("Updating customer current order.");
             api.updateCurrentOrder(customer.getUserId(), 
                     new OrderLineWS[] { newLine }, null, new Date(), 
                     "New SIM card supply fee");
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     /**
      * Sets up and returns new user object with given username.
      */
     public static UserWS generateUser(String username)
             throws JbillingAPIException {
         UserWS newUser = new UserWS();
         newUser.setUserName(username);
         newUser.setPassword("123qwe");
         newUser.setLanguageId(1); // Language "1" = "English"
         newUser.setMainRoleId(5); // Role "5" = "Customer"
         newUser.setStatusId(1);  // Status "1" = "Active"
 
         // add contact info
         ContactWS contact = new ContactWS();
         contact.setOrganizationName("Test Business Name");
         contact.setEmail("joe.bloggs@test.com");
         contact.setAddress1("1 Main Rd.");
         contact.setCity("BigCity");
         contact.setStateProvince("SomeState");
         contact.setPostalCode("2345");
         contact.setPhoneNumber("123456789");
         newUser.setContact(contact);
 
         return newUser;
     } 
 
     /**
      * Sets up and returns new credit card object.
      */
     public static CreditCardDTO generateCreditCard(String number) 
             throws JbillingAPIException {
         CreditCardDTO cc = new CreditCardDTO();
         cc.setName("Joe Bloggs");
         cc.setNumber(number);
 
         // valid credit card must have a future expiry date to be valid for payment processing
         Calendar expiry = Calendar.getInstance();
         expiry.set(Calendar.YEAR, expiry.get(Calendar.YEAR) + 1);
         cc.setExpiry(expiry.getTime());        
 
         return cc;
     }
 
     /** 
      * Sets up and returns new order object for given user and item.
      */
     public static OrderWS generateOrder(Integer userId, Integer itemId) 
             throws JbillingAPIException {
         OrderWS newOrder = new OrderWS();
         newOrder.setUserId(userId);
         newOrder.setBillingTypeId(1); // pre-paid order
         newOrder.setPeriod(2); // monthly
         newOrder.setCurrencyId(1);
         newOrder.setActiveSince(new Date());
 
         // add the order line
         OrderLineWS line = new OrderLineWS();
         line.setTypeId(1); // Item line type
         line.setQuantity(1);
         line.setItemId(itemId); // id of item being purchased
         line.setUseItem(true); // take the description from the item
         newOrder.setOrderLines(new OrderLineWS[] {line});
 
         return newOrder;
     }
 }
