 package com.orangeleap.tangerine.controller;
 
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.NewAddressAware;
 import com.orangeleap.tangerine.domain.NewEmailAware;
 import com.orangeleap.tangerine.domain.NewPhoneAware;
 import com.orangeleap.tangerine.domain.PaymentSource;
 import com.orangeleap.tangerine.domain.PaymentSourceAware;
 import com.orangeleap.tangerine.domain.communication.AbstractCommunicationEntity;
 import com.orangeleap.tangerine.domain.communication.Address;
 import com.orangeleap.tangerine.domain.communication.Email;
 import com.orangeleap.tangerine.domain.communication.Phone;
 import com.orangeleap.tangerine.service.AddressService;
 import com.orangeleap.tangerine.service.EmailService;
 import com.orangeleap.tangerine.service.PaymentSourceService;
 import com.orangeleap.tangerine.service.PhoneService;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 public abstract class NewTangerineConstituentAttributesFormController extends NewTangerineFormController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
 	@Resource(name="addressService")
 	protected AddressService addressService;
 
 	@Resource(name="phoneService")
 	protected PhoneService phoneService;
 
 	@Resource(name="emailService")
 	protected EmailService emailService;
 
     @Resource(name="paymentSourceService")
     protected PaymentSourceService paymentSourceService;
 
     protected boolean bindAddress = true;
     protected boolean bindPhone = true;
     protected boolean bindEmail = true;
 	protected boolean bindPaymentSource = true;
 
     /**
      * If you do not want to bind to Addresses, set to false.  Default is true
      * @param bindAddress
      */
     public void setBindAddress(boolean bindAddress) {
         this.bindAddress = bindAddress;
     }
 
     /**
      * If you do not want to bind to Phones, set to false.  Default is true
      * @param bindPhone
      */
     public void setBindPhone(boolean bindPhone) {
         this.bindPhone = bindPhone;
     }
 
     /**
      * If you do not want to bind to Emails, set to false.  Default is true
      * @param bindEmail
      */
     public void setBindEmail(boolean bindEmail) {
         this.bindEmail = bindEmail;
     }
 
 	/**
 	 * If you do not want to bind to PaymentSources, set to false.  Default is true
 	 * @param bindPaymentSource
 	 */
 	public void setBindPaymentSource(boolean bindPaymentSource) {
 	    this.bindPaymentSource = bindPaymentSource;
 	}
 
     @SuppressWarnings("unchecked")
     protected void addConstituentToReferenceData(HttpServletRequest request, Map refData) {
         Constituent constituent = getConstituent(request);
         if (constituent != null) {
             refData.put(StringConstants.CONSTITUENT, getConstituent(request));
         }
     }
 
 	@Override
 	protected void convertFormToDomain(HttpServletRequest request, TangerineForm form, Map<String, Object> paramMap) throws Exception {
 		form.setFieldMap(new TreeMap<String, Object>());
 
 		ServletRequestDataBinder binder = new ServletRequestDataBinder(form.getDomainObject());
 		initBinder(request, binder);
 
 		MutablePropertyValues propertyValues = new MutablePropertyValues();
 		BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(form.getDomainObject());
 
 		convertAddress(request, form);
 		convertPhone(request, form);
 		convertEmail(request, form);
 		convertPaymentSource(request, form);
 
 		for (Object obj : paramMap.keySet()) {
 			String escapedFormFieldName = obj.toString();
 			String fieldName = TangerineForm.unescapeFieldName(escapedFormFieldName);
 			Object paramValue;
 
 			if (beanWrapper.getPropertyType(fieldName) != null && beanWrapper.getPropertyType(fieldName).isArray()) {
 				paramValue = request.getParameterValues(escapedFormFieldName);
 			}
 			else {
 				paramValue = request.getParameter(escapedFormFieldName);
 			}
 
 			if (bindAddress && fieldName.startsWith(StringConstants.ADDRESS)) {
 				if (!"address.id".equals(fieldName) && ((NewAddressAware) form.getDomainObject()).getAddress() != null) {
 					form.addField(escapedFormFieldName, paramValue);
 					propertyValues.addPropertyValue(fieldName, paramValue);
 				}
 			}
 			else if (bindPhone && fieldName.startsWith(StringConstants.PHONE)) {
 				if (!"phone.id".equals(fieldName) && ((NewPhoneAware) form.getDomainObject()).getPhone() != null) {
 					form.addField(escapedFormFieldName, paramValue);
 					propertyValues.addPropertyValue(fieldName, paramValue);
 				}
 			}
 			else if (bindEmail && fieldName.startsWith(StringConstants.EMAIL)) {
 				if (!"email.id".equals(fieldName) && ((NewEmailAware) form.getDomainObject()).getEmail() != null) {
 					form.addField(escapedFormFieldName, paramValue);
 					propertyValues.addPropertyValue(fieldName, paramValue);
 				}
 			}
 			else if (bindPaymentSource && fieldName.startsWith(StringConstants.PAYMENT_SOURCE)) {
 				if (!"paymentSource.id".equals(fieldName) && ((PaymentSourceAware) form.getDomainObject()).getPaymentSource() != null) {
 					form.addField(escapedFormFieldName, paramValue);
 					propertyValues.addPropertyValue(fieldName, paramValue);
 				}
 			}
 			else {
 				form.addField(escapedFormFieldName, paramValue);
 				propertyValues.addPropertyValue(fieldName, paramValue);
 			}

 		}
 		binder.bind(propertyValues);
 	}
 
 
     @SuppressWarnings("unchecked")
     @Override
     protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
         Map refData = new HashMap();
         this.addConstituentToReferenceData(request, refData);
 
         refDataAddresses(request, command, errors, refData);
         refDataPhones(request, command, errors, refData);
         refDataEmails(request, command, errors, refData);
 	    refDataPaymentSources(request, command, errors, refData);
         return refData;
     }
 
     @SuppressWarnings("unchecked")
     protected void refDataAddresses(HttpServletRequest request, Object command, Errors errors, Map refData) {
         if (bindAddress) {
             List<Address> addresses = addressService.filterValid(this.getConstituentId(request));
             Address selectedAddress = null;
 	        if (((TangerineForm) command).getDomainObject() instanceof NewAddressAware) {
 		        selectedAddress = ((NewAddressAware)((TangerineForm) command).getDomainObject()).getAddress();
             }
             if (addresses != null) {
                 filterInactiveNotSelected(selectedAddress, addresses);
             }
             refData.put(StringConstants.ADDRESSES, addresses);
         }
     }
 
     @SuppressWarnings("unchecked")
     protected void refDataEmails(HttpServletRequest request, Object command, Errors errors, Map refData) {
         if (bindEmail) {
             List<Email> emails = emailService.filterValid(this.getConstituentId(request));
 	        Email selectedEmail = null;
 		    if (((TangerineForm) command).getDomainObject() instanceof NewEmailAware) {
 			    selectedEmail = ((NewEmailAware)((TangerineForm) command).getDomainObject()).getEmail();
 	        }
             if (emails != null) {
                 filterInactiveNotSelected(selectedEmail, emails);
             }
             refData.put(StringConstants.EMAILS, emails);
         }
     }
 
     @SuppressWarnings("unchecked")
     protected void refDataPhones(HttpServletRequest request, Object command, Errors errors, Map refData) {
         if (bindPhone) {
             List<Phone> phones = phoneService.filterValid(this.getConstituentId(request));
             Phone selectedPhone = null;
 	        if (((TangerineForm) command).getDomainObject() instanceof NewPhoneAware) {
 		        selectedPhone = ((NewPhoneAware)((TangerineForm) command).getDomainObject()).getPhone();
             }
             if (phones != null) {
                 filterInactiveNotSelected(selectedPhone, phones);
             }
             refData.put(StringConstants.PHONES, phones);
         }
     }
 
 	@SuppressWarnings("unchecked")
 	protected void refDataPaymentSources(HttpServletRequest request, Object command, Errors errors, Map refData) {
 	    if (bindPaymentSource) {
 	        PaymentSource selectedPaymentSource = null;
 	        if (((TangerineForm) command).getDomainObject() instanceof PaymentSourceAware) {
 	            selectedPaymentSource = ((PaymentSourceAware)((TangerineForm) command).getDomainObject()).getPaymentSource();
 	        }
 	        Map<String, List<PaymentSource>> paymentSources = paymentSourceService.groupPaymentSources(this.getConstituentId(request), selectedPaymentSource);
 	        refData.put(StringConstants.PAYMENT_SOURCES, paymentSources);
 	    }
 	}
 
     private static <T extends AbstractCommunicationEntity> void filterInactiveNotSelected(T command, List<T> masterList) {
         for (Iterator<T> iterator = masterList.iterator(); iterator.hasNext();) {
             T entity = iterator.next();
             if (command == null && entity.isInactive()) {
                 iterator.remove();
             }
             else if (command != null && entity.isInactive() && !entity.getId().equals(command.getId())) {
                 iterator.remove();
             }
         }
     }
 
 	protected void convertAddress(HttpServletRequest request, TangerineForm form) {
 		if (bindAddress) {
 			final String escapedFormFieldName = TangerineForm.escapeFieldName("address.id");
 			final String addressId = request.getParameter(escapedFormFieldName);
 			form.addField(escapedFormFieldName, addressId);
 
 			if (addressId != null && NumberUtils.isNumber(addressId)) {
 				final long id = Long.parseLong(addressId);
 				NewAddressAware aware = (NewAddressAware) form.getDomainObject();
 				if (id == -1) { // 'None' address option selected
 					aware.setAddress(null);
 				}
 				else if (id == 0) { // 'New' address option selected
 					aware.setAddress(new Address(getConstituentId(request)));
 				}
 				else { // 'Existing' address option selected
 					final Address address = addressService.readById(id);
 					if (address == null) {
 						throw new IllegalArgumentException("Invalid address ID = " + id);
 					}
 					aware.setAddress(address);
 				}
 			}
 		}
 	}
 
 	protected void convertPhone(HttpServletRequest request, TangerineForm form) {
 		if (bindPhone) {
 			final String escapedFormFieldName = TangerineForm.escapeFieldName("phone.id");
 			final String phoneId = request.getParameter(escapedFormFieldName);
 			form.addField(escapedFormFieldName, phoneId);
 
 			if (phoneId != null && NumberUtils.isNumber(phoneId)) {
 				final long id = Long.parseLong(phoneId);
 				NewPhoneAware aware = (NewPhoneAware) form.getDomainObject();
 				if (id == -1) { // 'None' phone option selected
 					aware.setPhone(null);
 				}
 				else if (id == 0) { // 'New' phone option selected
 					aware.setPhone(new Phone(getConstituentId(request)));
 				}
 				else { // 'Existing' phone option selected
 					final Phone phone = phoneService.readById(id);
 					if (phone == null) {
 						throw new IllegalArgumentException("Invalid phone ID = " + id);
 					}
 					aware.setPhone(phone);
 				}
 			}
 		}
 	}
 
 	protected void convertEmail(HttpServletRequest request, TangerineForm form) {
 		if (bindEmail) {
 			final String escapedFormFieldName = TangerineForm.escapeFieldName("email.id");
 			final String emailId = request.getParameter(escapedFormFieldName);
 			form.addField(escapedFormFieldName, emailId);
 
 			if (emailId != null && NumberUtils.isNumber(emailId)) {
 				final long id = Long.parseLong(emailId);
 				NewEmailAware aware = (NewEmailAware) form.getDomainObject();
 				if (id == -1) { // 'None' email option selected
 					aware.setEmail(null);
 				}
 				else if (id == 0) { // 'New' email option selected
 					aware.setEmail(new Email(getConstituentId(request)));
 				}
 				else { // 'Existing' email option selected
 					final Email email = emailService.readById(id);
 					if (email == null) {
 						throw new IllegalArgumentException("Invalid email ID = " + id);
 					}
 					aware.setEmail(email);
 				}
 			}
 		}
 	}
 
 	protected void convertPaymentSource(HttpServletRequest request, TangerineForm form) {
 		if (bindPaymentSource) {
 			final String escapedFormFieldName = TangerineForm.escapeFieldName("paymentSource.id");
 			final String paymentSourceId = request.getParameter(escapedFormFieldName);
 			form.addField(escapedFormFieldName, paymentSourceId);
 
 			if (paymentSourceId != null && NumberUtils.isNumber(paymentSourceId)) {
 				final long id = Long.parseLong(paymentSourceId);
 				PaymentSourceAware aware = (PaymentSourceAware) form.getDomainObject();
 				if (id == -1) { // 'None' payment source option selected
 					aware.setPaymentSource(null);
 				}
 				else if (id == 0) { // 'New' payment source option selected
 					aware.setPaymentSource(new PaymentSource(getConstituent(request)));
 				}
 				else { // 'Existing' payment source option selected
 					final PaymentSource paymentSource = paymentSourceService.readPaymentSource(id);
 					if (paymentSource == null) {
 						throw new IllegalArgumentException("Invalid payment source ID = " + id);
 					}
 					aware.setPaymentSource(paymentSource);
 				}
 			}
 		}
 	}
 
     @SuppressWarnings("unchecked")
     @Override
     protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
         if (isFormSubmission(request) && !errors.hasErrors() && bindPaymentSource && command instanceof PaymentSourceAware) {
             PaymentSourceAware aware = (PaymentSourceAware) command;
             if (aware.getPaymentSource() != null && aware.getPaymentSource().isNew()) {
                 Map<String, Object> conflictingSources = paymentSourceService.checkForSameConflictingPaymentSources(aware);
                 String useConflictingName = request.getParameter("useConflictingName");
                 if (!"true".equals(useConflictingName)) {
                     Set<String> nameSources = (Set<String>) conflictingSources.get("names");
                     if (nameSources != null && !nameSources.isEmpty()) {
                         ModelAndView mav = showForm(request, response, errors);
                         mav.addObject("conflictingNames", nameSources);
                         return mav;
                     }
                 }
                 List<PaymentSource> dateSources = (List<PaymentSource>) conflictingSources.get("dates");
                 if (dateSources != null && !dateSources.isEmpty()) {
                     PaymentSource src = dateSources.get(0); // should only be 1
                     src.setCreditCardExpirationMonth(aware.getPaymentSource().getCreditCardExpirationMonth());
                     src.setCreditCardExpirationYear(aware.getPaymentSource().getCreditCardExpirationYear());
                     aware.setPaymentSource(src);
                 }
             }
         }
         return super.processFormSubmission(request, response, command, errors);
     }
 }
