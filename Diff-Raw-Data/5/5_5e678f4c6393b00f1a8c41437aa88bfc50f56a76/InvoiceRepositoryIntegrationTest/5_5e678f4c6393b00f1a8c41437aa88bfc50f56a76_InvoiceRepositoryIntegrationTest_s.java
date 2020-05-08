 package kabbadi.domain.db;
 
 import kabbadi.IntegrationTest;
 import kabbadi.domain.Invoice;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 public class InvoiceRepositoryIntegrationTest extends IntegrationTest {
 
     @Autowired
     private SessionFactory sessionFactory;
 
     @Autowired
     private GenericRepository<Invoice> invoiceRepository;
 
     @Test
     public void should_have_a_invoice() {
         GenericRepository<Invoice> repository = new GenericRepository<Invoice>(sessionFactory, Invoice.class);
         String invoiceNumber = "invoice27";
         addToDatabase(invoiceNumber);
 
         Invoice actualInvoice = repository.findBy(Invoice.INVOICE_NUMBER, invoiceNumber);
 
         assertThat(actualInvoice.getInvoiceNumber(), equalTo(invoiceNumber));
 
     }
 
     private void addToDatabase(String invoiceNumber) {
 
         Session currentSession = sessionFactory.getCurrentSession();
         String sql = "insert into Invoice (id, invoiceNumber, freeOfCharge, loanBasis) values (27, '" + invoiceNumber + "', 0, 0);";
         currentSession.createSQLQuery(sql).executeUpdate();
 
     }
 
     @Test
     public void should_get_an_invoice_list() {
 
         GenericRepository<Invoice> repository = new GenericRepository<Invoice>(sessionFactory, Invoice.class);
         repository.save(new Invoice());
         repository.save(new Invoice());
         repository.save(new Invoice());
 
        assertThat(repository.list().size(), equalTo(3));
 
 
     }
 }
