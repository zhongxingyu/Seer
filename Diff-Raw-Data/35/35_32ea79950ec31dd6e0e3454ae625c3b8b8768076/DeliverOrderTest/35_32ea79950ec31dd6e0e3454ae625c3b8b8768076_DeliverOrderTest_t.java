 package se.sveaekonomi.webpay.integration.webservice.handleorder;
 
 import static org.junit.Assert.assertEquals;
 
 import javax.xml.bind.ValidationException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import se.sveaekonomi.webpay.integration.WebPay;
 import se.sveaekonomi.webpay.integration.order.handle.DeliverOrderBuilder;
 import se.sveaekonomi.webpay.integration.order.row.Item;
 import se.sveaekonomi.webpay.integration.response.webservice.DeliverOrderResponse;
 import se.sveaekonomi.webpay.integration.util.constant.COUNTRYCODE;
 import se.sveaekonomi.webpay.integration.util.constant.DISTRIBUTIONTYPE;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaDeliverOrder;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaRequest;
 
 public class DeliverOrderTest {
  private DeliverOrderBuilder order;
     
     @Before
     public void setUp() {
         order = WebPay.deliverOrder();
     }  
     
     @Test
     public void testBuildRequest() {
        DeliverOrderBuilder request = order
         .setOrderId(54086L);
        assertEquals(54086L, request.getOrderId());
     }
     
     @Test
     public void testDeliverInvoice() throws ValidationException {
          
     	SveaRequest<SveaDeliverOrder> request = order.addOrderRow(Item.orderRow()
             .setArticleNumber(1)
             .setQuantity(2)
             .setAmountExVat(100.00)
             .setDescription("Specification")
             .setName("Prod")
             .setUnit("st")
             .setVatPercent(25)
             .setDiscountPercent(0))
 
         .addFee(Item.shippingFee()
             .setShippingId("33")
             .setName("shipping")
             .setDescription("Specification")
             .setAmountExVat(50)
             .setUnit("st")
             .setVatPercent(25)
             .setDiscountPercent(0))
         
         .addDiscount(Item.fixedDiscount()
            .setAmountIncVat(10))  
                    
         .setInvoiceDistributionType(DISTRIBUTIONTYPE.Post)
         .setOrderId(54086L)
         .setNumberOfCreditDays(1)
        .setCreditInvoice("id")
         .setCountryCode(COUNTRYCODE.SE)
         .deliverInvoiceOrder()        
             .prepareRequest();   
         
         //First order row is a product
         assertEquals("1", request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(0).ArticleNumber);
         assertEquals("Prod: Specification", request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(0).Description);
         assertEquals(100.00, request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(0).PricePerUnit, 0);
         assertEquals(2, request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(0).NumberOfUnits);
         assertEquals("st", request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(0).Unit);
         assertEquals(25, request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(0).VatPercent, 0);
         assertEquals(0, request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(0).DiscountPercent);
         //Second order row is shipment
         assertEquals("33", request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(1).ArticleNumber);
         assertEquals("shipping: Specification", request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(1).Description);
         assertEquals(50, request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(1).PricePerUnit, 0);
         assertEquals(1, request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(1).NumberOfUnits);
         assertEquals("st", request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(1).Unit);
         assertEquals(25, request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(1).VatPercent, 0);
         assertEquals(0, request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(1).DiscountPercent);
         //discount
         assertEquals(-8.0, request.request.deliverOrderInformation.deliverInvoiceDetails.OrderRows.get(2).PricePerUnit, 0);
         
         assertEquals(1, request.request.deliverOrderInformation.deliverInvoiceDetails.NumberofCreditDays);
         assertEquals("Post", request.request.deliverOrderInformation.deliverInvoiceDetails.InvoiceDistributionType);
         assertEquals(true, request.request.deliverOrderInformation.deliverInvoiceDetails.IsCreditInvoice);
         assertEquals("id", request.request.deliverOrderInformation.deliverInvoiceDetails.InvoiceIdToCredit);
         assertEquals("54086", request.request.deliverOrderInformation.sveaOrderId);
         assertEquals("Invoice", request.request.deliverOrderInformation.orderType);  
     }
     
     @Test
     public void testDeliverPaymentPlanOrder() throws ValidationException {
         SveaRequest<SveaDeliverOrder> request = order        
         .setOrderId(54086L)
         .setCountryCode(COUNTRYCODE.SE)
         .deliverPaymentPlanOrder()  
         .prepareRequest();
         
         assertEquals("54086", request.request.deliverOrderInformation.sveaOrderId);
         assertEquals("PaymentPlan", request.request.deliverOrderInformation.orderType);
     }
     
     @Test
     public void testDeliverPaymentPlanOrderDoRequest() throws Exception {
     	DeliverOrderResponse response =
     			WebPay.deliverOrder()
     		.addOrderRow(Item.orderRow()
     			.setArticleNumber(1)
     			.setQuantity(2)
     			.setAmountExVat(100.00)
     			.setDescription("Specification")
     			.setName("Prod")
     			.setUnit("st")
     			.setVatPercent(25)
     			.setDiscountPercent(0))  
     		.setOrderId(54086L)
     		.setInvoiceDistributionType(DISTRIBUTIONTYPE.Post)
     		.setCountryCode(COUNTRYCODE.SE)
     		.deliverInvoiceOrder()
     			.doRequest();    
 
     	 response.getErrorMessage();
     }
 }
