 /**
  * Ti.Paypal Module
  * Copyright (c) 2010-2013 by Appcelerator, Inc. All Rights Reserved.
  * Please see the LICENSE included with this distribution for details.
  */
 
 package ti.modules.titanium.paypal;
 
 import java.math.BigDecimal;
 import java.util.HashMap;
 
 import org.appcelerator.kroll.KrollDict;
 import org.appcelerator.kroll.annotations.Kroll;
 import org.appcelerator.titanium.TiC;
 import org.appcelerator.titanium.proxy.TiViewProxy;
 import org.appcelerator.kroll.common.Log;
 import org.appcelerator.titanium.view.TiUIView;
 
 import com.paypal.android.MEP.PayPal;
 import com.paypal.android.MEP.PayPalAdvancedPayment;
 import com.paypal.android.MEP.PayPalInvoiceData;
 import com.paypal.android.MEP.PayPalInvoiceItem;
 import com.paypal.android.MEP.PayPalPayment;
 import com.paypal.android.MEP.PayPalPreapproval;
 import com.paypal.android.MEP.PayPalReceiverDetails;
 
 import ti.modules.titanium.ui.widget.TiView;
 import android.app.Activity;
 
 @Kroll.proxy(creatableInModule = PaypalModule.class)
 public class PaypalButtonProxy extends TiViewProxy {
 
 	private static final String LCAT = "PaypalButtonProxy";
 
 	public PaypalButtonProxy() {
 		super();
 	}
 
 	@Override
 	public TiUIView createView(final Activity activity) {
 		final TiUIView view = new TiView(this);
 		view.getLayoutParams().autoFillsHeight = true;
 		view.getLayoutParams().autoFillsWidth = true;
 		final TiViewProxy proxy = this;
 
 		// Initialize the button. We'll do it in a separate thread because it requires communication with the server which may take some time
 		// depending on the connection strength/speed.
 		Thread libraryInitializationThread = new Thread() {
 			public void run() {
 				final PaypalButton button = new PaypalButton(proxy);
 				activity.runOnUiThread(new Runnable() {
 					public void run() {
 						view.add(button);
 					}
 				});
 			}
 		};
 		libraryInitializationThread.start();
 		return view;
 	}
 
 	@Override
	public boolean fireEvent(String eventName, Object data) {
 		if (eventName.equals(TiC.EVENT_CLICK)) {
 			onClick();
 		}
		return super.fireEvent(eventName, data);
 	}
 
 	private void onClick() {
 		KrollDict buttonProperties = this.getProperties();
 		if (buttonProperties.containsKey("transactionType")) {
 			Log.e(LCAT, "WARNING: 'transactionType' IS NOT A VALID PROPERTY! You need to use the 'paymentType' property on the PAYMENT DICTIONARY! "
 					+ "See docs or example for more info.");
 		}
 
 		if (buttonProperties.containsKey("payment")) {
 			KrollDict paymentProperties = buttonProperties.getKrollDict("payment");
 			PayPalPayment newPayment = createPaymentFromDict(paymentProperties);
 			PaypalModule.getInstance().executePayment(this, newPayment);
 		} else if (buttonProperties.containsKey("advancedPayment")) {
 			KrollDict paymentProperties = buttonProperties.getKrollDict("advancedPayment");
 			PayPalAdvancedPayment newPayment = createAdvancedPaymentFromDict(paymentProperties);
 			PaypalModule.getInstance().executeAdvancedPayment(this, newPayment);
 		} else if (buttonProperties.containsKey("preapproval")) {
 			KrollDict paymentProperties = buttonProperties.getKrollDict("preapproval");
 			PayPalPreapproval newPayment = createPreapprovalFromDict(paymentProperties);
 			PaypalModule.getInstance().executePreapproval(this, newPayment);
 		} else {
 			throw new IllegalArgumentException("The PayPal Button requires either the payment or advancedPayment property to be set!");
 		}
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private PayPalPayment createPaymentFromDict(HashMap paymentPropertiesIn) {
 		KrollDict paymentProperties = new KrollDict(paymentPropertiesIn);
 		PayPalPayment payment = new PayPalPayment();
 
 		// the require parameters
 		if (paymentProperties.containsKey("paymentType")) {
 			payment.setPaymentType(paymentProperties.getInt("paymentType"));
 		} else if (paymentProperties.containsKey("transactionType")) {
 			payment.setPaymentType(paymentProperties.getInt("transactionType"));
 			Log.e(LCAT, "WARNING: 'transactionType' HAS BEEN RENAMED TO 'paymentType'! See docs or example for more info.");
 		} else {
 			payment.setPaymentType(PayPal.PAYMENT_TYPE_GOODS);
 		}
 		payment.setSubtotal(new BigDecimal(paymentProperties.optString("subtotal", "0.00")));
 		payment.setCurrencyType(paymentProperties.optString("currency", "USD"));
 		payment.setRecipient(paymentProperties.getString("recipient"));
 
 		// the optional parameters
 		if (paymentProperties.containsKey("paymentSubtype"))
 			payment.setPaymentSubtype(paymentProperties.getInt("paymentSubtype"));
 		if (paymentProperties.containsKey("merchantName"))
 			payment.setMerchantName(paymentProperties.getString("merchantName"));
 		if (paymentProperties.containsKey("description"))
 			payment.setDescription(paymentProperties.getString("description"));
 		if (paymentProperties.containsKey("customID"))
 			payment.setCustomID(paymentProperties.getString("customID"));
 		if (paymentProperties.containsKey("ipnUrl"))
 			payment.setIpnUrl(paymentProperties.getString("ipnUrl"));
 		if (paymentProperties.containsKey("memo"))
 			payment.setMemo(paymentProperties.getString("memo"));
 
 		PayPalInvoiceData invoice = createInvoiceFromDict(paymentProperties);
 		payment.setInvoiceData(invoice);
 
 		return payment;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private PayPalPreapproval createPreapprovalFromDict(HashMap propsIn) {
 		KrollDict props = new KrollDict(propsIn);
 		PayPalPreapproval payment = new PayPalPreapproval();
 
 		if (props.containsKey("type")) {
 			payment.setType(props.getInt("type"));
 		}
 
 		payment.setCurrencyType(props.optString("currency", "USD"));
 
 		if (props.containsKey("dayOfMonth"))
 			payment.setDayOfMonth(props.getInt("dayOfMonth"));
 		if (props.containsKey("dayOfWeek"))
 			payment.setDayOfWeek(props.getInt("dayOfWeek"));
 		if (props.containsKey("isApproved"))
 			payment.setIsApproved(props.getBoolean("isApproved"));
 		if (props.containsKey("paymentPeriod"))
 			payment.setPaymentPeriod(props.getInt("paymentPeriod"));
 		if (props.containsKey("pinRequired"))
 			payment.setPinRequired(props.getBoolean("pinRequired"));
 
 		if (props.containsKey("maxAmountPerPayment"))
 			payment.setMaxAmountPerPayment(new BigDecimal(props.optString("maxAmountPerPayment", "0.00")));
 		if (props.containsKey("maxNumberOfPayments"))
 			payment.setMaxNumberOfPayments(props.getInt("maxNumberOfPayments"));
 		if (props.containsKey("maxNumberOfPaymentsPerPeriod"))
 			payment.setMaxNumberOfPaymentsPerPeriod(props.getInt("maxNumberOfPaymentsPerPeriod"));
 		if (props.containsKey("maxTotalAmountOfAllPayments"))
 			payment.setMaxTotalAmountOfAllPayments(new BigDecimal(props.optString("maxTotalAmountOfAllPayments", "0.00")));
 
 		if (props.containsKey("merchantName"))
 			payment.setMerchantName(props.getString("merchantName"));
 		if (props.containsKey("ipnUrl"))
 			payment.setIpnUrl(props.getString("ipnUrl"));
 		if (props.containsKey("memo"))
 			payment.setMemo(props.getString("memo"));
 
 		return payment;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private PayPalAdvancedPayment createAdvancedPaymentFromDict(HashMap paymentPropertiesIn) {
 		KrollDict paymentProperties = new KrollDict(paymentPropertiesIn);
 		PayPalAdvancedPayment payment = new PayPalAdvancedPayment();
 
 		if (paymentProperties.containsKey("merchantName"))
 			payment.setMerchantName(paymentProperties.getString("merchantName"));
 		if (paymentProperties.containsKey("ipnUrl"))
 			payment.setIpnUrl(paymentProperties.getString("ipnUrl"));
 		if (paymentProperties.containsKey("memo"))
 			payment.setMemo(paymentProperties.getString("memo"));
 
 		payment.setCurrencyType(paymentProperties.optString("currency", "USD").toUpperCase());
 
 		Object[] rawPayments = (Object[]) paymentProperties.get("payments");
 		for (Object obj : rawPayments) {
 			KrollDict rawPayment = new KrollDict((HashMap) obj);
 			PayPalReceiverDetails details = new PayPalReceiverDetails();
 
 			details.setIsPrimary(rawPayment.optBoolean("isPrimary", false));
 
 			if (rawPayment.containsKey("paymentType")) {
 				details.setPaymentType(rawPayment.getInt("paymentType"));
 			} else if (rawPayment.containsKey("transactionType")) {
 				details.setPaymentType(rawPayment.getInt("transactionType"));
 				Log.e(LCAT, "WARNING: 'transactionType' HAS BEEN RENAMED TO 'paymentType'! See docs or example for more info.");
 			} else {
 				details.setPaymentType(PayPal.PAYMENT_TYPE_GOODS);
 			}
 			details.setSubtotal(new BigDecimal(rawPayment.optString("subtotal", "0.00")));
 			details.setRecipient(rawPayment.getString("recipient"));
 
 			if (rawPayment.containsKey("paymentSubtype"))
 				details.setPaymentSubtype(rawPayment.getInt("paymentSubtype"));
 			if (rawPayment.containsKey("customID"))
 				details.setCustomID(rawPayment.getString("customID"));
 
 			PayPalInvoiceData invoice = createInvoiceFromDict(rawPayment);
 			details.setInvoiceData(invoice);
 
 			payment.getReceivers().add(details);
 		}
 
 		return payment;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private PayPalInvoiceData createInvoiceFromDict(HashMap paymentPropertiesIn) {
 		KrollDict paymentProperties = new KrollDict(paymentPropertiesIn);
 		PayPalInvoiceData invoice = new PayPalInvoiceData();
 
 		if (paymentProperties.containsKey("tax"))
 			invoice.setTax(new BigDecimal(paymentProperties.getString("tax")));
 		if (paymentProperties.containsKey("shipping"))
 			invoice.setShipping(new BigDecimal(paymentProperties.getString("shipping")));
 
 		if (paymentProperties.containsKey("invoiceItems")) {
 			Object[] rawItems = (Object[]) paymentProperties.get("invoiceItems");
 			for (Object obj : rawItems) {
 				KrollDict rawItem = new KrollDict((HashMap) obj);
 				PayPalInvoiceItem item = new PayPalInvoiceItem();
 				item.setName(rawItem.getString("name"));
 				item.setID(rawItem.getString("itemID"));
 				item.setTotalPrice(new BigDecimal(rawItem.optString("totalPrice", "0.00")));
 				item.setUnitPrice(new BigDecimal(rawItem.optString("itemPrice", "0.00")));
 				item.setQuantity(rawItem.optInt("itemCount", 1));
 
 				invoice.add(item);
 			}
 		}
 
 		return invoice;
 	}
 
 }
