 package se.sveaekonomi.webpay.integration.order.handle;
 
 import se.sveaekonomi.webpay.integration.config.ConfigurationProvider;
 import se.sveaekonomi.webpay.integration.util.constant.COUNTRYCODE;
 import se.sveaekonomi.webpay.integration.webservice.handleorder.CloseOrder;
 
 /**
  * Close an already created order  
  * @author klar-sar
  *
  */
 public class CloseOrderBuilder {
     private COUNTRYCODE countryCode;
     private Long orderId;
     private String orderType;
     private final ConfigurationProvider config;
         
     public CloseOrderBuilder(ConfigurationProvider config) {
     	this.config = config;
     }
     
     public ConfigurationProvider getConfig() {
     	return this.config;
     }
     
     public Long getOrderId() {
         return orderId;
     }
     
     /**
      * Required
      * @param orderId
      * @return CloseOrderBuilder
      */
     public CloseOrderBuilder setOrderId(Long orderId) {
         this.orderId = orderId;
         return this;
     }
     
     public String getOrderType() {
         return orderType;
     }
     
     public CloseOrderBuilder setCountryCode(COUNTRYCODE countryCode){
     	this.countryCode = countryCode;
     	return this;
     }
     
     public COUNTRYCODE getCountryCode() {
     	return this.countryCode;
     }
     
    /**
     * Required
     * @param orderType
     * @return CloseOrderBuilder
     */
     public CloseOrderBuilder setOrderType(String orderType) {
         this.orderType = orderType;
         return this;
     }
 
     public CloseOrder closeInvoiceOrder() {
         orderType = "Invoice";
         return new CloseOrder(this);
     }
 
     public CloseOrder closePaymentPlanOrder() {
         orderType = "PaymentPlan";
         return new CloseOrder(this);
     }
 }
