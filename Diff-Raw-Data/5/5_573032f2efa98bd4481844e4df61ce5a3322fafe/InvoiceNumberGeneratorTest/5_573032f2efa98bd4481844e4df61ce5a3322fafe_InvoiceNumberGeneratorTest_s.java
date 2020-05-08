 package org.businessmanager.service;
 
 import java.util.Calendar;
 
 import junit.framework.Assert;
 
 import org.businessmanager.domain.Invoice;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 @ContextConfiguration("/test-context.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 @TransactionConfiguration(defaultRollback = true)
 @Transactional
 public class InvoiceNumberGeneratorTest {
 
 	@Autowired
 	private InvoiceNumberGenerator generator;
 	
 	@Autowired
 	private InvoiceService invoiceService;
 	
 	@Test
 	public void testDefaultNumber() {
 		Long number = generator.getNextInvoiceNumber();
 		Assert.assertEquals(Long.valueOf(100000), number);
 	}
 	
 	@Test
 	public void testNumberGenerator() {
		Invoice invoice = new Invoice(10L, Calendar.getInstance());
 		invoiceService.saveInvoice(invoice);
 		
 		Long nextInvoiceNumber = generator.getNextInvoiceNumber();
		Assert.assertEquals(Long.valueOf(11), nextInvoiceNumber);
 	}
 }
