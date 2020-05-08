 package se.sveaekonomi.webpay.integration.hosted.payment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.ValidationException;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import se.sveaekonomi.webpay.integration.exception.SveaWebPayException;
 import se.sveaekonomi.webpay.integration.hosted.HostedOrderRowBuilder;
 import se.sveaekonomi.webpay.integration.hosted.helper.ExcludePayments;
 import se.sveaekonomi.webpay.integration.hosted.helper.HostedRowFormatter;
 import se.sveaekonomi.webpay.integration.hosted.helper.HostedXmlBuilder;
 import se.sveaekonomi.webpay.integration.hosted.helper.PaymentForm;
 import se.sveaekonomi.webpay.integration.order.create.CreateOrderBuilder;
 import se.sveaekonomi.webpay.integration.order.validator.HostedOrderValidator;
 import se.sveaekonomi.webpay.integration.order.validator.IdentityValidator;
 import se.sveaekonomi.webpay.integration.util.constant.COUNTRYCODE;
 import se.sveaekonomi.webpay.integration.util.constant.LANGUAGECODE;
 import se.sveaekonomi.webpay.integration.util.constant.PAYMENTMETHOD;
 import se.sveaekonomi.webpay.integration.util.constant.PAYMENTTYPE;
 
 /*******************************************************************************
  * Description of HostedPayment: Parent to CardPayment, DirectPayment,
  * PayPagePayment and PaymentMethodPayment classes. Prepares an order and
  * creates a payment form to integrate on web page. Uses XmlBuilder to turn
  * formatted order into xml format.
  * 
  * @author klar-sar
  * *****************************************************************************/
 public abstract class HostedPayment<T extends HostedPayment<T>> {
 
 	protected CreateOrderBuilder createOrderBuilder;
 	protected ArrayList<HostedOrderRowBuilder> rowBuilder;
 	protected List<String> excludedPaymentMethods;
 	private Long amount;
 	private Long vat;
 	protected String returnUrl;
 	protected String cancelUrl;
 	protected String callbackUrl;
 	protected ExcludePayments excluded;
 	protected String languageCode;
 
 	public HostedPayment(CreateOrderBuilder createOrderBuilder) {
 		this.createOrderBuilder = createOrderBuilder;
 		rowBuilder = new ArrayList<HostedOrderRowBuilder>();
 		excluded = new ExcludePayments();
 		excludedPaymentMethods = new ArrayList<String>();
 		returnUrl = "";
 	}
 
 	public CreateOrderBuilder getCreateOrderBuilder() {
 		return createOrderBuilder;
 	}
 
 	public ArrayList<HostedOrderRowBuilder> getRowBuilder() {
 		return rowBuilder;
 	}
 
 	public List<String> getExcludedPaymentMethods() {
 		return excludedPaymentMethods;
 	}
 
 	public Long getAmount() {
 		return amount;
 	}
 
 	public Long getVat() {
 		return vat;
 	}
 
 	public String getReturnUrl() {
 		return returnUrl;
 	}
 
 	/**
 	 * Required
 	 * 
 	 * @param returnUrl
 	 * @return HostedPayment
 	 */
 	public T setReturnUrl(String url) {
 		returnUrl = url;
 		return getGenericThis();
 	}
 
 	public String getCancelUrl() {
 		return cancelUrl;
 	}
 
 	public T setCancelUrl(String url) {
 		cancelUrl = url;
 		return getGenericThis();
 	}
 	
 	public T setCallbacklUrl(String url) {
 		callbackUrl = url;
 		return getGenericThis();
 	}
 	
 	public String getCallbacklUrl() {
 		return callbackUrl;
 	}
 
 	public T setPayPageLanguageCode(LANGUAGECODE languageCode) {
 		this.languageCode = languageCode.toString();
 		return getGenericThis();
 	}
 
 	public String getPayPageLanguageCode() {
 		return languageCode;
 	}
 
 	public String validateOrder() {
 		String errors = "";
 		if (this.returnUrl.equals("")) {
 			errors += "MISSING VALUE - Return url is required, setReturnUrl(...).\n";
 		}
 
 		HostedOrderValidator validator = new HostedOrderValidator();
 		// Check if payment method is EU country, PaymentMethod: INVOICE or PAYMENTPLAN
 		if (getClass().equals(PaymentMethodPayment.class)) {
			if (((PaymentMethodPayment) this).getPaymentMethod() == PAYMENTMETHOD.INVOICE || 
					((PaymentMethodPayment) this).getPaymentMethod() == PAYMENTMETHOD.PAYMENTPLAN) {
 				if (this.createOrderBuilder.getCountryCode().equals(COUNTRYCODE.NL)) {
 					errors += new IdentityValidator().validateNLIdentity(createOrderBuilder);
 				}
 				else if (this.createOrderBuilder.getCountryCode().equals(COUNTRYCODE.DE)) {
 					errors += new IdentityValidator().validateDEIdentity(createOrderBuilder);
 				}
 			}
 		}
 
 		errors += validator.validate(this.createOrderBuilder);
 		return errors;
 
 	}
 
 	public void calculateRequestValues() {
 		String errors = "";
 		errors = validateOrder();
 
 		if (!errors.equals("")) {
 			throw new SveaWebPayException("Validation failed", new ValidationException(errors));
 		}
 
 		HostedRowFormatter formatter = new HostedRowFormatter();
 
 		rowBuilder = formatter.formatRows(createOrderBuilder);
 		amount = formatter.getTotalAmount();
 		vat = formatter.getTotalVat();
 		configureExcludedPaymentMethods();
 	}
 
 	public PaymentForm getPaymentForm() {
 		calculateRequestValues();
 		HostedXmlBuilder xmlBuilder = new HostedXmlBuilder();
 		String xml = xmlBuilder.getXml(this);
 
 		PaymentForm form = new PaymentForm();
 		form.setXmlMessage(xml);
 
 		form.setMerchantId(createOrderBuilder.getConfig().getMerchantId(PAYMENTTYPE.HOSTED, createOrderBuilder.getCountryCode()));
 		form.setSecretWord(createOrderBuilder.getConfig().getSecretWord(PAYMENTTYPE.HOSTED, createOrderBuilder.getCountryCode()));
 
 		if (this.createOrderBuilder.getCountryCode() != null) {
 			form.setSubmitMessage(this.createOrderBuilder.getCountryCode());
 		}
 		else {
 			form.setSubmitMessage(COUNTRYCODE.SE);
 		}
 
 		form.setPayPageUrl(createOrderBuilder.getConfig().getEndPoint(PAYMENTTYPE.HOSTED));
 		form.setForm();
 		form.setHtmlFields();
 
 		return form;
 	}
 
 	protected abstract T configureExcludedPaymentMethods();
 
 	public abstract XMLStreamWriter getPaymentSpecificXml(XMLStreamWriter xmlw) throws Exception;
 
 	protected void writeSimpleElement(XMLStreamWriter xmlw, String name, String value) throws XMLStreamException {
 		if (value != null) {
 			xmlw.writeStartElement(name);
 			xmlw.writeCharacters(value);
 			xmlw.writeEndElement();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private T getGenericThis() {
 		return (T) this;
 	}
 }
