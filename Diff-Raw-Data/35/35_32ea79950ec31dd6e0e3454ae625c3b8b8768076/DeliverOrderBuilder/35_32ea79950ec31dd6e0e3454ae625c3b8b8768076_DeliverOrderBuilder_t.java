 package se.sveaekonomi.webpay.integration.order.handle;
 
 import javax.xml.bind.ValidationException;
 
 import se.sveaekonomi.webpay.integration.config.ConfigurationProvider;
 import se.sveaekonomi.webpay.integration.order.OrderBuilder;
 import se.sveaekonomi.webpay.integration.order.validator.HandleOrderValidator;
 import se.sveaekonomi.webpay.integration.util.constant.DISTRIBUTIONTYPE;
 import se.sveaekonomi.webpay.integration.webservice.handleorder.HandleOrder;
 
 /**
  * 
  * @author klar-sar
  *
  */
 public class DeliverOrderBuilder extends OrderBuilder<DeliverOrderBuilder> {
     private HandleOrderValidator validator;
     
     private long orderId;
     private String orderType;
     private String distributionType;
     private String invoiceIdToCredit;
     private Integer numberOfCreditDays;
     
     public DeliverOrderBuilder(ConfigurationProvider config) {
         this.config = config;
     }
     
     public HandleOrderValidator getValidator() {
         return validator;
     }
     
     public DeliverOrderBuilder setValidator(HandleOrderValidator validator) {
         this.validator = validator;
         return this;
     }
     
     public long getOrderId() {
         return orderId;
     }
     
     public DeliverOrderBuilder setOrderId(long orderId) {
         this.orderId = orderId;
         return this;
     }
     
     public String getOrderType() {
         return orderType;
     }
     
     public void setOrderType(String orderType) {
         this.orderType = orderType;
     }
     
     public String getInvoiceDistributionType() {
         return distributionType;
     }
     
     public DeliverOrderBuilder setInvoiceDistributionType(DISTRIBUTIONTYPE type) {
         this.distributionType = type.toString();
         return this;
     }
     
 /*    public DeliverOrderBuilder setInvoiceDistributionTypeAsEmail() {
         this.distributionType = "Email";
         return this;
     }*/
     
    public String getCreditInvoice() {
         return invoiceIdToCredit;
     }
     
    public DeliverOrderBuilder setCreditInvoice(String invoiceId) {
        this.invoiceIdToCredit = invoiceId;
         return this;
     }
 
     public Integer getNumberOfCreditDays() {
         return numberOfCreditDays;
     }
     
     public DeliverOrderBuilder setNumberOfCreditDays(int numberOfCreditDays) {
         this.numberOfCreditDays = numberOfCreditDays;
         return this;
     }
     
     /**
      * Updates the invoice order with additional information and prepares it for delivery.
      * Will automatically match all order rows that are to be delivered with those which was sent
      * when creating the invoice order.
      * @return HandleOrder
      * @throws ValidationException
      */
     public HandleOrder deliverInvoiceOrder() throws ValidationException {
         orderType = "Invoice";
         return new HandleOrder(this);
     }
 
     /**
      * Prepares the PaymentPlan order for delivery.
      * @return HandleOrder
      * @throws ValidationException
      */
     public HandleOrder deliverPaymentPlanOrder() throws ValidationException {
         orderType = "PaymentPlan";
         return new HandleOrder(this);
     }
     
     
 }
