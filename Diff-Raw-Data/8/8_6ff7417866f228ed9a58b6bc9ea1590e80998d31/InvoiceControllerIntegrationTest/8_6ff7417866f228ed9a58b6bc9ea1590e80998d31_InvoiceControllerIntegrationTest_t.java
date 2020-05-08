 package kabbadi.controller;
 
 import kabbadi.IntegrationTest;
 import kabbadi.domain.Invoice;
 import kabbadi.domain.builder.InvoiceTestBuilder;
 import kabbadi.domain.db.GenericRepository;
 import kabbadi.service.InvoiceService;
 import org.hibernate.SessionFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.servlet.ModelAndView;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 
 public class InvoiceControllerIntegrationTest extends IntegrationTest {
 
     @Autowired
     private SessionFactory sessionFactory;
     private InvoiceController controller;
     private InvoiceService invoiceService;
 
     @Before
     public void setup() {
         this.invoiceService = buildInvoiceService();
         this.controller = buildInvoiceController(invoiceService);
     }
 
     @Test
     public void should_add_a_new_invoice_in_the_database() throws Exception {
         String invoiceNumber = "123456";
        controller.add(invoiceWith(invoiceNumber), "admin");
         assertThat(invoiceService.findBy(invoiceNumber).getInvoiceNumber(), equalTo(invoiceNumber));
     }
 
     @Test
     public void should_not_add_an_invoice_without_mandatory_fields() throws Exception {
         String invoiceNumber = "";
        controller.add(invoiceWith(invoiceNumber), "admin");
         assertThat(invoiceService.findBy(invoiceNumber), nullValue());
     }
 
     private Invoice invoiceWith(String invoiceNumber) {
         return new InvoiceTestBuilder().withInvoiceNumber(invoiceNumber).build();
     }
 
     @Test
     public void should_show_new_invoice_form() throws Exception {
         ModelAndView createView = controller.create();
         assertThat(createView.getViewName(), equalTo("invoice/edit"));
     }
 
     @Test
     public void should_show_the_single_invoice_view_form() {
         Integer invoiceId = 22;
        controller.add(invoiceWith("22"), "");
         ModelAndView singleInvoiceView = controller.viewDetails(invoiceId);
         assertThat(singleInvoiceView.getViewName(), equalTo("invoice/view"));
     }
 
     @Test
     public void should_list_the_invoices() {
         ModelAndView listView = controller.list();
         assertThat(listView.getViewName(), equalTo("invoice/list"));
     }
 
     private InvoiceController buildInvoiceController(InvoiceService invoiceService) {
         return new InvoiceController(invoiceService);
     }
 
     private InvoiceService buildInvoiceService() {
         return new InvoiceService(new GenericRepository<Invoice>(sessionFactory, Invoice.class));
 
     }
 }
