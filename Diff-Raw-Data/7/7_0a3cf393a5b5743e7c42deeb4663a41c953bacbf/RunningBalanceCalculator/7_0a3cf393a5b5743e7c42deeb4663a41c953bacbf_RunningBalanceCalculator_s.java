 package kabbadi.domain;
 
 import kabbadi.service.InvoiceService;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 public class RunningBalanceCalculator {
     private final InvoiceService invoiceService;
 
     public RunningBalanceCalculator(InvoiceService invoiceService) {
         this.invoiceService = invoiceService;
     }
 
     public BigDecimal calculateStartingFrom(Invoice currentInvoice) {
         return runningBalanceCalculation(currentInvoice);
     }
 
     private BigDecimal calculatePreviousBalance(String bondNumber, Location location) {
         String previousBondNumber = new PreviousBondNumberConverter(bondNumber).getPreviousBondNumber();
         Invoice previousInvoice = invoiceService.findByPreviousBondNumber(previousBondNumber, location);
 
         if (previousInvoice == null) {
             return new BigDecimal(0);
         }
 
         return runningBalanceCalculation(previousInvoice);
     }
 
     private BigDecimal runningBalanceCalculation(Invoice currentInvoice) {
         return calculatePreviousBalance(currentInvoice.getBondNumber(), currentInvoice.getLocation())
                .subtract(currentInvoice.getAmountSTPIApproval())
                .add(currentInvoice.getCgApprovedInINR());
     }
 
     public Invoice injectInto(Invoice invoice) {
         if(invoice == null)
             return null;
 
         invoice.setRunningBalanceCalculator(this);
         return invoice;
     }
 
     public List<Invoice> injectInto(List<Invoice> invoices) {
         if(invoices == null)
             return null;
 
         for (Invoice invoice : invoices) {
             injectInto(invoice);
         }
 
         return invoices;
     }
 }
