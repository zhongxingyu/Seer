 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.service.impl;
 
 import com.orangeleap.tangerine.domain.AbstractEntity;
 import com.orangeleap.tangerine.domain.AddressAware;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.EmailAware;
 import com.orangeleap.tangerine.domain.PaymentSource;
 import com.orangeleap.tangerine.domain.PaymentSourceAware;
 import com.orangeleap.tangerine.domain.PhoneAware;
 import com.orangeleap.tangerine.domain.communication.Address;
 import com.orangeleap.tangerine.domain.communication.Email;
 import com.orangeleap.tangerine.domain.communication.Phone;
 import com.orangeleap.tangerine.domain.paymentInfo.AbstractPaymentInfoEntity;
 import com.orangeleap.tangerine.service.AddressService;
 import com.orangeleap.tangerine.service.AuditService;
 import com.orangeleap.tangerine.service.EmailService;
 import com.orangeleap.tangerine.service.PaymentSourceService;
 import com.orangeleap.tangerine.service.PhoneService;
 import com.orangeleap.tangerine.util.AES;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineMessageAccessor;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.validation.BindException;
 
 import javax.annotation.Resource;
 import java.text.SimpleDateFormat;
 
 public abstract class AbstractPaymentService extends AbstractTangerineService {
 
     /**
      * Logger for this class and subclasses
      */
     protected final Log logger = OLLogger.getLog(getClass());
 
     @Resource(name = "auditService")
     protected AuditService auditService;
 
     @Resource(name = "addressService")
     protected AddressService addressService;
 
     @Resource(name = "phoneService")
     protected PhoneService phoneService;
 
     @Resource(name = "emailService")
     protected EmailService emailService;
 
     @Resource(name = "paymentSourceService")
     protected PaymentSourceService paymentSourceService;
 
     public void maintainEntityChildren(AbstractEntity entity, Constituent constituent) throws BindException {
         if (entity instanceof AddressAware) {
             AddressAware addressAware = (AddressAware) entity;
             maintainAddressChild(addressAware, constituent);
         }
         if (entity instanceof PhoneAware) {
             PhoneAware phoneAware = (PhoneAware) entity;
             maintainPhoneChild(phoneAware, constituent);
         }
         if (entity instanceof EmailAware) {
             EmailAware emailAware = (EmailAware) entity;
             maintainEmailChild(emailAware, constituent);
         }
         if (entity instanceof PaymentSourceAware) {
             maintainPaymentSourceChild(entity, constituent);
         }
     }
 
     private void maintainAddressChild(AddressAware addressAware, Constituent constituent) throws BindException {
 	    if (addressAware.getAddress() != null && addressAware.getAddress().isNew()) {
 		    Address newAddress = addressAware.getAddress();
 		    newAddress.setConstituentId(constituent.getId());
 
 		    Address existingAddress = addressService.alreadyExists(newAddress);
 		    if (existingAddress != null) {
 		        addressAware.setAddress(existingAddress);
 		    }
 		    else {
                 if (StringUtils.trimToNull(newAddress.getAddressLine1()) != null) {
                 	addressAware.setAddress(addressService.save(newAddress));
                 }
             }
 	    }
     }
 
     private void maintainPhoneChild(PhoneAware phoneAware, Constituent constituent) throws BindException {
 	    if (phoneAware.getPhone() != null && phoneAware.getPhone().isNew()) {
 		    Phone newPhone = phoneAware.getPhone();
 		    newPhone.setConstituentId(constituent.getId());
 
 		    Phone existingPhone = phoneService.alreadyExists(newPhone);
 		    if (existingPhone != null) {
 		        phoneAware.setPhone(existingPhone);
 		    }
 		    else {
 		    	if (StringUtils.trimToNull(newPhone.getNumber()) != null) {
 		    		phoneAware.setPhone(phoneService.save(newPhone));
 		    	}
             }
 	    }
     }
 
     private void maintainEmailChild(EmailAware emailAware, Constituent constituent) throws BindException {
 	    if (emailAware.getEmail() != null && emailAware.getEmail().isNew()) {
 		    Email newEmail = emailAware.getEmail();
 		    newEmail.setConstituentId(constituent.getId());
 
 		    Email existingEmail = emailService.alreadyExists(newEmail);
 		    if (existingEmail != null) {
 		        emailAware.setEmail(existingEmail);
 		    }
 		    else {
 		    	if (StringUtils.trimToNull(newEmail.getEmailAddress()) != null) {
 		    		emailAware.setEmail(emailService.save(newEmail));
 		    	}
             }
 	    }
     }
 
     private void maintainPaymentSourceChild(AbstractEntity entity, Constituent constituent) throws BindException {
         PaymentSourceAware paymentSourceAware = (PaymentSourceAware) entity;
         if (PaymentSource.CASH.equals(paymentSourceAware.getPaymentType()) ||
                 PaymentSource.CHECK.equals(paymentSourceAware.getPaymentType())) {
             paymentSourceAware.setPaymentSource(null);
         }
         else if (PaymentSource.ACH.equals(paymentSourceAware.getPaymentType()) ||
                 PaymentSource.CREDIT_CARD.equals(paymentSourceAware.getPaymentType())) {
             if (paymentSourceAware.getPaymentSource() != null) {
 
 	            paymentSourceAware.setPaymentType(paymentSourceAware.getPaymentSource().getPaymentType());
 	            if (paymentSourceAware.getPaymentSource().isNew()) {
 					PaymentSource newPaymentSource = paymentSourceAware.getPaymentSource();
 					newPaymentSource.setConstituent(constituent);
 
 					if (entity instanceof AddressAware) {
 						newPaymentSource.setFromAddressAware((AddressAware) entity);
 					}
 					if (entity instanceof PhoneAware) {
 						newPaymentSource.setFromPhoneAware((PhoneAware) entity);
 					}
 
                     paymentSourceAware.setPaymentSource(paymentSourceService.maintainPaymentSource(newPaymentSource));
 	            }
             }
         }
     }
 
     protected String getPaymentDescription(AbstractPaymentInfoEntity entity) {
         StringBuilder sb = new StringBuilder();
 
         if (PaymentSource.ACH.equals(entity.getPaymentType())) {
             sb.append(TangerineMessageAccessor.getMessage("achNumberColon"));
             sb.append(" ").append(entity.getPaymentSource().getAchAccountNumberDisplay());
         }
         else if (PaymentSource.CREDIT_CARD.equals(entity.getPaymentType())) {
             sb.append(TangerineMessageAccessor.getMessage("creditCardNumberColon"));
             sb.append(" ").append(entity.getPaymentSource().getCreditCardType()).append(" ");
             sb.append(entity.getPaymentSource().getCreditCardNumberDisplay());
             sb.append(" ");
             sb.append(entity.getPaymentSource().getCreditCardExpirationMonth());
             sb.append(" / ");
             sb.append(entity.getPaymentSource().getCreditCardExpirationYear());
             sb.append(" ");
             sb.append(entity.getPaymentSource().getCreditCardHolderName());
         }
         else if (PaymentSource.CHECK.equals(entity.getPaymentType())) {
             sb.append("\n");
             sb.append(TangerineMessageAccessor.getMessage("checkNumberColon"));
             sb.append(" ");
             sb.append(entity.getCheckNumber());
             if (entity.getCheckDate() != null) {
                 sb.append(" ");
                 sb.append(TangerineMessageAccessor.getMessage("checkDateColon"));
                sb.append(" ");
                sb.append(TangerineMessageAccessor.getMessage("checkAccountNumberColon"));
                 sb.append(" ").append(new SimpleDateFormat(StringConstants.MM_DD_YYYY_FORMAT).format(entity.getCheckDate())); // TODO: the right date format based on locale
             }
             if (StringUtils.isNotBlank(entity.getCheckAccountNumber())) {
                 sb.append(" ").append(AES.decryptAndMask(entity.getCheckAccountNumber()));
             }
         }
 	    if (entity.getAddress() != null && !entity.getAddress().isNew()) {
 		    Address address = entity.getAddress();
             if (address != null) {
                 sb.append("\n");
                 sb.append(TangerineMessageAccessor.getMessage("addressColon"));
                 sb.append(" ");
                 sb.append(StringUtils.trimToEmpty(address.getAddressLine1()));
                 sb.append(" ").append(StringUtils.trimToEmpty(address.getAddressLine2()));
                 sb.append(" ").append(StringUtils.trimToEmpty(address.getAddressLine3()));
                 sb.append(" ").append(StringUtils.trimToEmpty(address.getCity()));
                 String state = StringUtils.trimToEmpty(address.getStateProvince());
                 sb.append((state.length() == 0 ? "" : (", " + state))).append(" ");
                 sb.append(" ").append(StringUtils.trimToEmpty(address.getCountry()));
                 sb.append(" ").append(StringUtils.trimToEmpty(address.getPostalCode()));
             }
         }
 	    if (entity.getPhone() != null && !entity.getPhone().isNew()) {
             Phone phone = entity.getPhone();
             if (phone != null) {
                 sb.append("\n");
                 sb.append(TangerineMessageAccessor.getMessage("phoneColon"));
                 sb.append(" ");
                 sb.append(StringUtils.trimToEmpty(phone.getNumber()));
             }
         }
         return sb.toString();
     }
 }
