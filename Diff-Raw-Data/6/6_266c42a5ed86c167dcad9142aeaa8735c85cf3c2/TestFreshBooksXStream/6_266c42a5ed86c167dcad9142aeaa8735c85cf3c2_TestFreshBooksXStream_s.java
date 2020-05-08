 package com.freshbooks.test;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.freshbooks.CustomXStream;
 import com.freshbooks.model.Client;
 import com.freshbooks.model.Clients;
 import com.freshbooks.model.Expense;
 import com.freshbooks.model.Expenses;
 import com.freshbooks.model.Invoice;
 import com.freshbooks.model.Invoices;
 import com.freshbooks.model.Request;
 import com.freshbooks.model.Response;
 import com.thoughtworks.xstream.XStream;
 
 public class TestFreshBooksXStream extends Assert {
 
     @Test
     public void loadSaveInvoice1() {
         XStream xs = new CustomXStream();
         Response response = (Response)xs.fromXML(getClass().getResourceAsStream("test_invoice_response_1.xml"));
         String str = xs.toXML(response);
         System.out.println(str);
     }
     
     /**
      * Make sure that changes to the API schema don't break our parser; just
      * ignore any tag that doesn't match an attribute on the object.
      */
     @Test
     public void loadSaveInvoiceWithExtraTags1() {
         XStream xs = new CustomXStream();
        Response response = (Response)xs.fromXML(getClass().getResourceAsStream("test_invoice_response_extra_tags_1.xml"));
         String str = xs.toXML(response);
         System.out.println(str);
     }
     
     @Test
     public void loadSaveClient() {
         XStream xs = new CustomXStream();
         Request request = (Request)xs.fromXML(getClass().getResourceAsStream("test_client_create_request_2.xml"));
         String str = xs.toXML(request);
         System.out.println(str);
         Client c = request.getClient();
         assertEquals("Jane", c.getFirstName());
         assertEquals("Doe", c.getLastName());
         assertEquals("janedoe@freshbooks.com", c.getEmail());
         assertEquals("New York", c.getState());
         assertEquals("New York", c.getCity());
         assertEquals("United States", c.getCountry());
         assertEquals("553132", c.getCode());
         assertEquals("", c.getSecondaryStreet1());
         assertEquals("", c.getSecondaryStreet2());
         assertEquals("", c.getSecondaryCity());
         assertEquals("", c.getSecondaryState());
         assertEquals("", c.getSecondaryCountry());
         assertEquals("", c.getSecondaryCode());
     }
     
     @Test
     public void loadSaveClientList() {
         XStream xs = new CustomXStream();
         Response response = (Response)xs.fromXML(getClass().getResourceAsStream("test_client_list_response_1.xml"));
         String str = xs.toXML(response);
         System.out.println(str);
         Clients clients = response.getClients();
         assertEquals(1, clients.getPage());
         assertEquals(15, clients.getPerPage());
         assertEquals(3, clients.getPages());
         assertEquals(42, clients.getTotal());
         assertEquals(2, clients.size());
         Client c1 = clients.getClients().get(0);
         assertEquals("Jane", c1.getFirstName());
         assertEquals("janedoe", c1.getUsername());
         Client c2 = clients.getClients().get(1);
         assertEquals("John", c2.getFirstName());
         assertEquals("johndoe", c2.getUsername());
     }
     @Test
     public void loadSaveInvoiceList() {
         XStream xs = new CustomXStream();
         Response response = (Response)xs.fromXML(getClass().getResourceAsStream("test_invoice_list_response_1.xml"));
         String str = xs.toXML(response);
         System.out.println(str);
         Invoices invoices = response.getInvoices();
         assertEquals(2, invoices.getContents().size());
         Invoice i1 = invoices.get(0);
         assertEquals(344, i1.getId().intValue());
         assertEquals("FB00004", i1.getNumber());
         assertEquals(3, i1.getClientId().intValue());
         assertEquals("", i1.getRecurringId());
         assertEquals("ABC Corp", i1.getOrganization());
         assertEquals("draft", i1.getStatus());
         assertEquals(new Double(45.6), i1.getAmount());
         assertEquals(new Double(45.6), i1.getAmountOutstanding());
         assertEquals(new GregorianCalendar(2007, Calendar.JUNE, 23).getTime(), i1.getDate());
         Invoice i2 = invoices.get(1);
         assertEquals(287, i2.getId().intValue());
         assertEquals("FB00001", i2.getNumber());
         assertEquals(3, i2.getClientId().intValue());
         assertEquals("19", i2.getRecurringId());
         assertEquals("ABC Corp", i2.getOrganization());
         assertEquals("draft", i2.getStatus());
         assertEquals(new Double(93.41), i2.getAmount());
         assertEquals(new Double(93.41), i2.getAmountOutstanding());
         assertEquals(new GregorianCalendar(2007, Calendar.MARCH, 17).getTime(), i2.getDate());
     }
     
     @Test
     public void loadSaveExpenseList() {
         XStream xs = new CustomXStream();
         Response response = (Response)xs.fromXML(getClass().getResourceAsStream("test_expense_list_response_1.xml"));
         String str = xs.toXML(response);
         System.out.println(str);
         Expenses expenses = response.getExpenses();
         assertEquals(2, expenses.size());
         assertEquals(1, expenses.getPage());
         assertEquals(10, expenses.getPerPage());
         assertEquals(4, expenses.getPages());
         assertEquals(47, expenses.getTotal());
         Expense e1 = expenses.get(0);
         assertEquals(430L, e1.getId().longValue());
         assertEquals("1", e1.getStaffId());
         assertEquals(5, e1.getCategoryId().intValue());
         assertEquals("10", e1.getProjectId());
         assertEquals(10, e1.getClientId().intValue());
         assertEquals(new Double(29.95), e1.getAmount());
         assertEquals(new GregorianCalendar(2008, Calendar.NOVEMBER, 1).getTime(), e1.getDate());
         assertEquals("Hardware.", e1.getNotes());
         assertEquals("1", e1.getStatus());
         Expense e2 = expenses.get(1);
         assertEquals(433, e2.getId().intValue());
         assertEquals("2", e2.getStaffId());
         assertEquals(5, e2.getCategoryId().intValue());
         assertEquals("10", e2.getProjectId());
         assertEquals(10, e2.getClientId().intValue());
         assertEquals(new Double(29.95), e2.getAmount());
         assertEquals(new GregorianCalendar(2008, Calendar.NOVEMBER, 1).getTime(), e2.getDate());
         assertEquals("Software package.", e2.getNotes());
         assertEquals("1", e2.getStatus());
     }
 }
