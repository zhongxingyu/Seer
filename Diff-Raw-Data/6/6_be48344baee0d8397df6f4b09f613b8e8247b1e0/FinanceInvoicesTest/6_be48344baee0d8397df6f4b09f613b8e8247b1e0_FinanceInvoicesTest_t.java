 package test;
 
 import builder.InvoiceTestBuilder;
 import forms.InvoiceForm;
 import org.junit.Test;
 
 public class FinanceInvoicesTest extends BaseTest {
 
     @Test
     public void should_be_able_to_add_finance_invoice_and_view_its_details(){
         InvoiceForm invoice = new InvoiceTestBuilder().buildFinance();
         launchKabbadi()
                 .loginWithValidCredentials()
                 .goToAddFinancePage()
                 .submit(invoice)
                 .viewInvoiceInListPage(invoice)
                 .viewFirstInvoiceDetails()
                 .confirmFinanceInvoiceData(invoice);
     }
 
     @Test
    public void should_validate_fields_before_submitting_finance_form() {
         InvoiceForm newInvoice = new InvoiceTestBuilder().withQuantity("words").buildFinance();
        launchKabbadi()
                .loginWithValidCredentials()
                 .goToAddFinancePage()
                 .submitInvalid(newInvoice)
                 .checkErrorMessage("Please enter a number");
     }
 
 }
