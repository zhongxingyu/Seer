 package net.frontlinesms.plugins.payment.service.dummycash;
 
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.creditsms.plugins.paymentview.data.domain.IncomingPayment;
 import org.creditsms.plugins.paymentview.data.domain.OutgoingPayment;
 import org.creditsms.plugins.paymentview.data.domain.OutgoingPayment.Status;
 import org.creditsms.plugins.paymentview.data.repository.IncomingPaymentDao;
 
 import net.frontlinesms.data.domain.PersistableSettings;
 import net.frontlinesms.plugins.payment.service.PaymentJob;
 import net.frontlinesms.plugins.payment.service.PaymentJobProcessor;
 import net.frontlinesms.plugins.payment.service.PaymentService;
 import net.frontlinesms.plugins.payment.service.PaymentServiceException;
 import net.frontlinesms.serviceconfig.ConfigurableService;
 import net.frontlinesms.serviceconfig.ConfigurableServiceProperties;
 import net.frontlinesms.serviceconfig.PasswordString;
 import net.frontlinesms.serviceconfig.StructuredProperties;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 @ConfigurableServiceProperties(name="DummyCash", icon="/icons/dummycash.png")
 public class DummyCashPaymentService implements PaymentService {
 	/** Prefix attached to every property name. */
 	private static final String PROPERTY_PREFIX = "plugins.payment.dummycash.";
 
 	static final String PROPERTY_USERNAME = PROPERTY_PREFIX + "username";
 	static final String PROPERTY_PASSWORD = PROPERTY_PREFIX + "password";
 	static final String PROPERTY_SERVER_URL = PROPERTY_PREFIX + "server.url";
 	static final String PROPERTY_OUTGOING_ENABLED = PROPERTY_PREFIX + "outgoing.enabled";
 	static final String PROPERTY_BALANCE = PROPERTY_PREFIX + "balance";
 
 	private PersistableSettings settings;
 
 	private DummyCashHttpJobber httpJobber;
 
 	private PaymentJobProcessor jobProcessor;
 
 	private IncomingPaymentDao incomingDao;
 
 	public StructuredProperties getPropertiesStructure() {
 		StructuredProperties defaultSettings = new StructuredProperties();
 		defaultSettings.put(PROPERTY_USERNAME, "Nathan");
 		defaultSettings.put(PROPERTY_PASSWORD, new PasswordString("secret"));
 		defaultSettings.put(PROPERTY_SERVER_URL, "http://localhost:8080/dummycash");
 		defaultSettings.put(PROPERTY_OUTGOING_ENABLED, true);
 		defaultSettings.put(PROPERTY_BALANCE, new BigDecimal(0));
 		return defaultSettings;
 	}
 
 	public PersistableSettings getSettings() {
 		return settings;
 	}
 
 	public void setSettings(PersistableSettings settings) {
 		this.settings = settings;
 	}
 
 	public Class<? extends ConfigurableService> getSuperType() {
 		return PaymentService.class;
 	}
 
 	public void makePayment(final OutgoingPayment payment)
 			throws PaymentServiceException {
 		jobProcessor.queue(new PaymentJob() {
 			public void run() {
 				try {
 					String response = httpJobber.get(getServerUrl() + "/send/",
 							"u", getUsername(),
 							"p", getPassword().getValue(),
 							"to", payment.getClient().getPhoneNumber(),
 							"amount", payment.getAmountPaid().toString());
 					if(response.equals("OK")) {
 						payment.setStatus(Status.CONFIRMED);
 					} else {
 						payment.setStatus(Status.ERROR);
 					}
 				} catch(Exception ex) {
 					payment.setStatus(Status.ERROR);
 					// TODO log the exception
 				}
 			}
 		});
 	}
 
 	public void checkBalance() throws PaymentServiceException {
 		jobProcessor.queue(new PaymentJob() {
 			public void run() {
 				try {
 					String response = httpJobber.get(getServerUrl() + "/balance/",
 							"u", getUsername(),
 							"p", getPassword().getValue());
 					settings.set(PROPERTY_BALANCE, new BigDecimal(response));
 				} catch(Exception ex) {
 					// TODO log the exception
 				}
 			}
 		});
 	}
 
 	public BigDecimal getBalanceAmount() {
 		return getPropertyValue(PROPERTY_BALANCE, BigDecimal.class);
 	}
 
 	public void startService() throws PaymentServiceException {
 		this.jobProcessor = new PaymentJobProcessor(this);
 		this.jobProcessor.start();
 	}
 
 	public void stopService() {
 		this.jobProcessor.stop();
 	}
 	
 	private String getUsername() {
 		return getPropertyValue(PROPERTY_USERNAME, String.class);
 	}
 
 	private PasswordString getPassword() {
 		return getPropertyValue(PROPERTY_PASSWORD, PasswordString.class);
 	}
 	
 	private String getServerUrl() {
 		return getPropertyValue(PROPERTY_SERVER_URL, String.class);
 	}
 
 	public boolean isOutgoingPaymentEnabled() {
 		return getPropertyValue(PROPERTY_OUTGOING_ENABLED, Boolean.class);
 	}
 
 	public void setOutgoingPaymentEnabled(boolean outgoingEnabled) {
 		this.settings.set(PROPERTY_OUTGOING_ENABLED, outgoingEnabled);
 	}
 	
 	/**
 	 * @param key The key of the property
 	 * @param clazz The class of the property's value
 	 * @param <T> The class of the property's value
 	 * @return The property value, either the one stored on db (if any) or the default value.
 	 * FIXME must be a cleaner way to implement than this...!  e.g. settings.get(KEY, String.class)
 	 */
 	protected <T extends Object> T getPropertyValue(String key, Class<T> clazz) {
 		return PersistableSettings.getPropertyValue(getPropertiesStructure(), settings, key, clazz);
 	}
 
 	public void setHttpJobber(DummyCashHttpJobber httpJobber) {
 		this.httpJobber = httpJobber;
 	}
 
	public void queueJob(WaitingJob waitingJob) {
		jobProcessor.queue(waitingJob);
 	}
 
 	void doCheckForIncomingPayments() throws DummyCashServerCommsException {
 		String response = httpJobber.get(getServerUrl() + "/incoming/",
 				"u", getUsername(),
 				"p", getPassword().getValue());
 		
 		JSONArray a = JSONArray.fromObject(response);
 		for(int i=0; i<a.size(); ++i) {
 			JSONObject o = a.getJSONObject(i);
 			try {
 				IncomingPayment p = new IncomingPayment();
 				p.setServiceSettings(this.getSettings());
 				p.setPaymentBy(o.getString("sender"));
 				p.setAmountPaid(new BigDecimal(o.getString("amount")));
 				p.setTimePaid(parseDate(o.getString("date")));
 
 				this.incomingDao.saveIncomingPayment(p);
 			} catch(Exception ex) {
 				// TODO log this
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	private Date parseDate(String dateString) throws ParseException {
 		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse(dateString);
 	}
 
 	public void setIncomingDao(IncomingPaymentDao dao) {
 		this.incomingDao = dao;
 	}
 }
