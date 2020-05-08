 package controllers;
 
 import com.neovisionaries.i18n.CountryCode;
 import forms.*;
 import io.sphere.client.model.CustomObject;
 import io.sphere.client.shop.model.*;
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import play.data.Form;
 import play.mvc.Result;
 import sphere.ShopController;
 import utils.pactas.Id;
 import utils.pactas.PactasClient;
 import utils.pactas.WebhookCallbackData;
 import views.html.index;
 import views.html.order;
 import views.html.success;
 
 import java.io.IOException;
 import java.util.Currency;
 
 import static play.data.Form.form;
 
 public class Application extends ShopController {
 
     private static final PactasClient pactas = new PactasClient();
     private static final ObjectMapper jsonMapper = new ObjectMapper()
             .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
 
     final static Form<AddToCart> addToCartForm = form(AddToCart.class);
     final static Form<SetAddress> setAddressForm = form(SetAddress.class);
     final static Form<Paymill> setPaymentForm = form(Paymill.class);
 
 
     private static Product getProduct() {
         return sphere().products().bySlug("donut-box").fetch().orNull();
     }
 
     public static Result showProduct() {
         Cart cart = sphere().currentCart().fetch();
         return ok(index.render(cart, getProduct()));
     }
 
     public static Result submitProduct() {
         // Case missing or invalid product form
         Form<AddToCart> cartForm = addToCartForm.bindFromRequest();
         if (cartForm.hasErrors()) {
             flash("error", "Please select a box and how often you want it.");
             return showProduct();
         }
         // Case invalid product
         AddToCart addToCart = cartForm.get();
         Variant variant = getProduct().getVariants().byId(addToCart.variantId).orNull();
         if (variant == null) {
             flash("error", "Product not found. Please try again.");
             return showProduct();
         }
         /* Clean cart because we only allow a single product */
         Cart cart = sphere().currentCart().fetch();
         for (LineItem item : cart.getLineItems()) {
             sphere().currentCart().removeLineItem(item.getId());
         }
        cart = sphere().currentCart().addLineItem(getProduct().getId(), variant.getId(), 1);
         /* Store frequency in a custom object related to current cart */
         sphere().customObjects().set("cart-frequency", cart.getId(), addToCart.howOften);
         return redirect(routes.Application.showOrder());
     }
 
     public static Result showOrder() {
         Cart cart = sphere().currentCart().fetch();
         // Case no product selected
         if (cart.getLineItems().size() < 1) {
             return showProduct();
         }
         // Case missing frequency
         CustomObject frequency = sphere().customObjects().get("cart-frequency", cart.getId()).fetch().orNull();
         if (frequency == null) {
             flash("error", "Missing frequency of delivery. Please try selecting it again.");
             return showProduct();
         }
         // Case product in cart
         LineItem item = cart.getLineItems().get(0);
         Form<SetAddress> addressForm = setAddressForm.fill(new SetAddress(cart.getShippingAddress()));
         return ok(order.render(cart, item, frequency.getValue().asInt(), addressForm));
     }
 
     public static Result submitOrder() {
         Cart cart = sphere().currentCart().fetch();
         // Case no product selected
         if (cart.getLineItems().size() < 1) {
             return showProduct();
         }
         // Case missing frequency
         CustomObject frequency = sphere().customObjects().get("cart-frequency", cart.getId()).fetch().orNull();
         if (frequency == null) {
             flash("error", "Missing frequency of delivery. Please try selecting it again.");
             return showProduct();
         }
         // Case missing or invalid shipping address form
         Form<SetAddress> shippingForm = setAddressForm.bindFromRequest();
         if (shippingForm.hasErrors()) {
             flash("error", "Please correct the shipping address form.");
             return showOrder();
         }
         // Case missing or invalid payment form
         Form<Paymill> paymillForm = setPaymentForm.bindFromRequest();
         if (paymillForm.hasErrors()) {
             flash("error", "There was some error during payment. Please try again.");
             return showOrder();
         }
         // Case correct address and payment data
         LineItem item = cart.getLineItems().get(0);
         SetAddress setAddress = shippingForm.get();
         Paymill paymill = paymillForm.get();
         boolean successful = true;
         String text;
         try {
             // TODO Fix Pactas
             Id customerId = pactas.createCustomer(paymill.paymillToken, setAddress.getAddress());
             Id billingId = pactas.createBillingGroup();
             Id contractId = pactas.createContract(billingId.Id, customerId.Id);
             pactas.createUsageData(contractId.Id, item.getProductId(), item.getVariant().getId(), frequency.getValue().asInt());
             pactas.lockContract(contractId.Id);
             text = "Thank you for your order. Please keep in mind that this shop is for demonstration only." +
                     "Therefore we don't ship donuts in reality. Don't worry, no payments will be charged." +
                     "If we ship donuts someday in the future you'll be the first that will be informed.";
         } catch (Exception e) {
             successful = true;
             text = "Everything went correct but the subscription could not be saved...";
         }
         text = "Order is done. Please keep in mind that this shop is for demonstration only." +
                 "Therefore we don't ship donuts in reality. Don't worry, no payments will be charged." +
                 "If we ship donuts someday in the future you'll be the first that will be informed.";
         return ok(success.render(successful, text));
     }
 
     /* Method called by Pactas every time an order must be placed (weekly, monthly...) */
     public static Result executeSubscription() {
 
         // Read order data from Pactas
         String payload = request().body().asJson().toString();
         play.Logger.debug("------ Pactas webhook: " + payload);
 
         WebhookCallbackData callbackData;
         try {
             callbackData = jsonMapper.readValue(payload, new TypeReference<WebhookCallbackData>() {});
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         if (callbackData.Items.isEmpty()) {
             return badRequest("No line items in callback data");
         }
         WebhookCallbackData.LineItem lineItemToOrder = callbackData.Items.get(0);
         // TODO Read address and other information from Pactas
         Address address = new Address(CountryCode.DE);
 
         // Create cart with Pactas data
         Cart cart = sphere().client().carts().createCart(Currency.getInstance("EUR"), CountryCode.DE, Cart.InventoryMode.None).execute();
         CartUpdate cartUpdate = new CartUpdate()
                 .setShippingAddress(address)
                 .addLineItem(1, lineItemToOrder.productId(), lineItemToOrder.variantId());
         cart = sphere().client().carts().updateCart(cart.getIdAndVersion(), cartUpdate).execute();
 
         // Create order from cart
         sphere().client().orders().createOrder(cart.getIdAndVersion(), PaymentState.Paid).execute();
 
         play.Logger.debug("------ Order created!");
         return ok("Order created!");
     }
 }
