 /**
  * 
  */
 package com.sapienter.jbilling.server.task;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.Date;
 import java.util.Hashtable;
 
 import junit.framework.TestCase;
 
 import com.sapienter.jbilling.server.entity.InvoiceLineDTO;
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.item.ItemDTOEx;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskWS;
 import com.sapienter.jbilling.server.user.ContactWS;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.user.UserWS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.api.JbillingAPI;
 import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
 
 /**
  * Test class to unit test the functionality of Tax applied to invoices that
  * belong to user from a configured country.
  * 
  * @author Vikas Bodani
  * @since 29-Jul-2011
  * 
  */
 public class PaymentTermPenaltyTaskTest extends TestCase {
 
     private JbillingAPI api;
     private static final String param1 = "charge_carrying_item_id";
     private static final String param2 = "penalty_after_days";
     private Integer taxItemID = null;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         api = JbillingAPIFactory.getAPI();
     }
 
     public void testPenalty() {
         try {
             System.out
                     .println("Adding a new user with contact and country set to AU.");
             Integer user1 = api.createUser(createUserWS("testPenltTsk" + System.currentTimeMillis(), 1,1));//due date 1 month or 30 days
             assertNotNull("Test fail at user creation.", user1);
 
             System.out.println("User ID: " + user1 + "\nCreating the Penalty Item.");
             taxItemID = createItem();
             assertNotNull("Penalty Item Id  should not be null.", taxItemID);
 
             System.out.println("Penalty Item ID: " + taxItemID
                     + "\nAdding a PaymentTermPenaltyTask Plugin.");
             PluggableTaskWS newTask = new PluggableTaskWS();
             newTask.setNotes("Australia GST Task.");
 
             Hashtable<String, String> params = new Hashtable<String, String>();
             params.put(param1, String.valueOf(taxItemID));// rate 10%
             params.put(param2, "14");
             newTask.setParameters(params);
             newTask.setProcessingOrder(Integer.valueOf(4));
             newTask.setTypeId(Integer.valueOf(91));
             Integer pluginInst = api.createPlugin(newTask);
             assertNotNull("Plugin id is not null.", pluginInst);
 
             System.out.println("Plugin ID: " + pluginInst
                     + "\nCreating order for user 1 & Generate invoice.");
             Integer invId = api.createOrderAndInvoice(createOrderWs(user1,1,1));//due date 1 month or 30 days
             assertNotNull("Order ID should have value.", invId);
 
             InvoiceWS invoice = api.getInvoiceWS(invId);
             InvoiceLineDTO[] lines = invoice.getInvoiceLines();
             System.out.println("Invoice ID: " + invId
                     + "\nInspecting invoices lines..");
             for (InvoiceLineDTO line : lines) {
                 System.out.println(line.getDescription());
                 if (line.getDescription() != null
                         && line.getDescription().startsWith("Tax or Penalty")) {
                    System.out.println("Penalty line found. Amount=" + line.getAmount());
                     assertTrue(line.getItemId().intValue() == taxItemID
                             .intValue());
                     System.out
                             .println("The amount should be equal to $0.5 (1 percent of 50)");
                     assertEquals(
                             "The amount should have been $0.5 (1 percent of 50).",
                            new BigDecimal("0.5").compareTo(line.getAmountAsDecimal())==0);
                 }
             }
 
             System.out.println("Successful, testPenalty");
 
             try{
                 api.deleteInvoice(invId);
                 OrderWS order=api.getLatestOrder(user1);
                 api.deleteOrder(order.getId());
                 api.deletePlugin(pluginInst);
                 api.deleteItem(taxItemID);
                 api.deleteUser(user1);
             } catch (Exception e){}
             
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     public void testNoPenalty() {
         try {
             System.out
                     .println("Adding a new user with contact and country set to non au.");
             Integer user2 = api.createUser(createUserWS("testPenltTsk" + System.currentTimeMillis(), 3,10));//10 days due date
             assertNotNull("Test fail at user creation.", user2);
 
             System.out.println("User ID: " + user2
                     + "\nPenalty Item for IN does not exists.");
             System.out.println("Penalty Plugin for AU exists, but not for IN.");
             System.out.println("Creating order for user 2 & Generate invoice.");
 
             Integer invId = api.createOrderAndInvoice(createOrderWs(user2, 3, 10));//order due date 10 days
             assertNotNull("Invoice ID should have value.", invId);
 
             InvoiceWS invoice = api.getInvoiceWS(invId);
             InvoiceLineDTO[] lines = invoice.getInvoiceLines();
             System.out.println("Invoice ID: " + invId
                     + "\nInspecting invoices lines..");
 
             assertTrue("There should have been only 1 line.", lines.length == 1);
 
             for (InvoiceLineDTO line : lines) {
                 System.out.println(line.getDescription());
                 if (line.getDescription() != null) {
                     assertFalse(line.getDescription().startsWith(
                             "Tax or Penalty"));
                 }
             }
             System.out.println("Successful, testNoPenalty");
 
             try { 
                 api.deleteInvoice(invId);
                 OrderWS order=api.getLatestOrder(user2);
                 api.deleteOrder(order.getId());
                 api.deleteUser(user2);
             } catch (Exception e){}
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     /**
      * @see #assertEquals(java.math.BigDecimal, java.math.BigDecimal)
      * 
      * @param message
      *            error message if assert fails
      * @param expected
      *            expected BigDecimal value
      * @param actual
      *            actual BigDecimal value
      */
     private void assertEquals(String message, BigDecimal expected,
             BigDecimal actual) {
         assertEquals(
                 message,
                 (Object) (expected == null ? null : expected.setScale(2,
                         RoundingMode.HALF_UP)), (Object) (actual == null ? null
                         : actual.setScale(2, RoundingMode.HALF_UP)));
     }
 
     private Integer createItem() {
         ItemDTOEx item = new ItemDTOEx();
         item.setCurrencyId(1);
         item.setPercentage(new BigDecimal(1));
         item.setPrice(item.getPercentage());
         item.setDescription("1 % Penalty Item");
         item.setEntityId(1);
         item.setNumber("PYMPEN");
         item.setTypes(new Integer[] { Integer.valueOf(22) });
         return api.createItem(item);
     }
 
     private OrderWS createOrderWs(Integer userId, Integer dueDateUnitId,
             Integer dueDateUnitValue) {
         OrderWS order = new OrderWS();
         order.setUserId(userId);
         order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         order.setPeriod(1); // once
         order.setCurrencyId(1);
         order.setActiveSince(new Date());
         order.setOrderLines(new OrderLineWS[] { createOrderLineWS() });
         order.setDueDateUnitId(dueDateUnitId);
         order.setDueDateValue(dueDateUnitValue);
         return order;
     }
 
     private OrderLineWS createOrderLineWS() {
         OrderLineWS line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setDescription("Order line");
         line.setItemId(1);
         line.setQuantity(1);
         line.setPrice(new BigDecimal("50"));
         line.setAmount(new BigDecimal("50"));
         return line;
     }
 
     private UserWS createUserWS(String userName, Integer dueDateUnitId,
             Integer dueDateUnitValue) {
         UserWS newUser = new UserWS();
         newUser.setUserId(0);
         newUser.setUserName(userName);
         newUser.setPassword("asdfasdfbc123");
         newUser.setLanguageId(1);
         newUser.setMainRoleId(5);
         newUser.setParentId(null);
         newUser.setStatusId(UserDTOEx.STATUS_ACTIVE);
         newUser.setCurrencyId(null);
         newUser.setDueDateUnitId(dueDateUnitId);
         newUser.setDueDateValue(dueDateUnitValue);
         // add a contact
         newUser.setContact(createContactWS());
 
         return newUser;
     }
 
     private ContactWS createContactWS() {
         ContactWS contact = new ContactWS();
         contact.setEmail("rtest@gmail.com");
         contact.setFirstName("Test");
         contact.setLastName("Plugin");
         contact.setType(2);
         return contact;
     }
 
 }
