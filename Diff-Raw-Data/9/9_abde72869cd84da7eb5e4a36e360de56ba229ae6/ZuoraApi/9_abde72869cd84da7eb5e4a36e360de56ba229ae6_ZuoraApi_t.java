 /*
  * Copyright 2010-2013 Ning, Inc.
  *
  *  Ning licenses this file to you under the Apache License, version 2.0
  *  (the "License"); you may not use this file except in compliance with the
  *  License.  You may obtain a copy of the License at:
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  *  License for the specific language governing permissions and limitations
  *  under the License.
  */
 
 package com.ning.killbill.zuora.zuora;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 import javax.annotation.Nullable;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.osgi.service.log.LogService;
 
 import com.ning.billing.catalog.api.Currency;
 import com.ning.billing.payment.api.PaymentMethodPlugin;
 import com.ning.killbill.zuora.method.CreditCardProperties;
 import com.ning.killbill.zuora.method.PaymentMethodProperties;
 import com.ning.killbill.zuora.method.PaypalProperties;
 import com.ning.killbill.zuora.util.Either;
 import com.ning.killbill.zuora.zuora.setup.ZuoraConfig;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Strings;
 import com.zuora.api.RatePlanChargeData;
 import com.zuora.api.RatePlanData;
 import com.zuora.api.SaveResult;
 import com.zuora.api.SubscribeOptions;
 import com.zuora.api.SubscribeRequest;
 import com.zuora.api.SubscribeResult;
 import com.zuora.api.SubscriptionData;
 import com.zuora.api.object.Account;
 import com.zuora.api.object.Contact;
 import com.zuora.api.object.Invoice;
 import com.zuora.api.object.InvoiceAdjustment;
 import com.zuora.api.object.InvoicePayment;
 import com.zuora.api.object.Payment;
 import com.zuora.api.object.PaymentMethod;
 import com.zuora.api.object.ProductRatePlanCharge;
 import com.zuora.api.object.RatePlan;
 import com.zuora.api.object.RatePlanCharge;
 import com.zuora.api.object.Refund;
 import com.zuora.api.object.RefundInvoicePayment;
 import com.zuora.api.object.Subscription;
 
 public class ZuoraApi {
 
     public static final String ZUORA_ACCOUNT_ID_KEY = "zuoraAccountId";
     private static final String PAYPAL_GATEWAY = "PAYPAL";
     private final static String DEFAULT_BATCH = "Batch17";
     private final static String DEFAULT_PAYMENT_TERMS = "Due Upon Receipt";
 
     private final ZuoraConfig config;
     private final StringTemplateLoader stringTemplateLoader;
     private final AtomicReference<ProductRatePlanCharge> killBillPlan = new AtomicReference<ProductRatePlanCharge>();
     private final LogService logService;
 
     public ZuoraApi(ZuoraConfig config, final LogService logService) {
         this.config = config;
         this.logService = logService;
         this.stringTemplateLoader = new StringTemplateLoader(ZuoraApi.class, logService);
     }
 
     //
     //                              ACCOUNT
     //
     public Either<ZuoraError, Account> getByAccountName(ZuoraConnection connection, String accountName) {
         final String query = stringTemplateLoader.load("getAccountByAccountName")
                                                  .define("accountName", accountName)
                                                  .build();
         final Either<ZuoraError, Account> accountOrError = connection.querySingle(query);
 
         if (accountOrError.isLeft()) {
             return Either.left(accountOrError.getLeft());
         } else if (accountOrError.getRight() == null) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "No account found for accountName " + accountName));
         } else {
             return Either.right(accountOrError.getRight());
         }
     }
 
     public Either<ZuoraError, Account> getAccountById(ZuoraConnection connection, String id) {
         final String query = stringTemplateLoader.load("getAccountById")
                                                  .define("id", id)
                                                  .build();
         final Either<ZuoraError, Account> accountOrError = connection.querySingle(query);
 
         if (accountOrError.isLeft()) {
             return Either.left(accountOrError.getLeft());
         } else if (accountOrError.getRight() == null) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "No account found for id " + id));
         } else {
             return Either.right(accountOrError.getRight());
         }
     }
 
 
     // STEPH should return iFrame URL instead
     public Either<ZuoraError, String> createPaymentProviderAccount(ZuoraConnection connection, com.ning.billing.account.api.Account inputAccount) {
         String defaultAccountName = StringUtils.substring("AccountName : " + inputAccount.getEmail(), 0, 50);
 
         String zuoraAccountId = null;
         Either<ZuoraError, Account> accountOrError = getByAccountName(connection, inputAccount.getExternalKey());
 
         if (accountOrError.isRight() && accountOrError.getRight().getId() != null) {
             zuoraAccountId = accountOrError.getRight().getId();
         } else {
 
             logService.log(LogService.LOG_INFO, String.format("Creating zuora account %s", inputAccount.getExternalKey()));
 
             try {
                 final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
                 final Account zuoraAccount = objectFactory.createAccount();
 
                 zuoraAccount.setAccountNumber(inputAccount.getExternalKey());
                 zuoraAccount.setAllowInvoiceEdit(false);
                 zuoraAccount.setAutoPay(false);
                 zuoraAccount.setBatch(DEFAULT_BATCH);
                 zuoraAccount.setBillCycleDay(ZuoraDateUtils.dayOfMonth());
                 zuoraAccount.setCurrency(inputAccount.getCurrency() == null ? String.valueOf(Currency.USD) : String.valueOf(inputAccount.getCurrency()));
                 zuoraAccount.setName(inputAccount.getName() == null ? defaultAccountName : inputAccount.getName());
                 zuoraAccount.setPaymentTerm(DEFAULT_PAYMENT_TERMS);
                 zuoraAccount.setStatus("Draft");
                 zuoraAccount.setInvoiceDeliveryPrefsEmail(false);
 
                 final Either<ZuoraError, String> accountIdOrError = connection.createWithId(zuoraAccount);
 
                 if (accountIdOrError.isRight()) {
                     zuoraAccountId = accountIdOrError.getRight();
                 } else {
                     return Either.left(new ZuoraError(accountIdOrError.getLeft().getType(), accountIdOrError.getLeft().getMessage()));
                 }
             } catch (Exception ex) {
                 return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
             }
         }
 
         updateAccountWithNewContact(connection, zuoraAccountId, inputAccount);
 
         return Either.right(zuoraAccountId);
     }
 
     private String getLastName(com.ning.billing.account.api.Account account) {
         return StringUtils.defaultIfEmpty(StringUtils.substring(account.getName(), account.getFirstNameLength() + 1), account.getEmail());
     }
 
     public Either<ZuoraError, Void> updateAccountWithNewContact(ZuoraConnection connection, String zuoraAccountId, com.ning.billing.account.api.Account inputAccount) {
 
         try {
 
             logService.log(LogService.LOG_INFO, String.format("Updating zuora account %s with new contact", inputAccount.getExternalKey()));
 
             final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
             final Contact contact = objectFactory.createContact();
             contact.setAccountId(zuoraAccountId);
             contact.setFirstName(getFirstName(inputAccount));
             contact.setLastName(getLastName(inputAccount));
             contact.setWorkPhone(inputAccount.getPhone());
             contact.setAddress1(inputAccount.getAddress1());
             contact.setAddress2(inputAccount.getAddress2());
             contact.setCity(inputAccount.getCity());
             contact.setCountry(Strings.emptyToNull(inputAccount.getCountry()));
             contact.setState(inputAccount.getStateOrProvince());
             contact.setPostalCode(inputAccount.getPostalCode());
             // We don't want any email, we manage them in Killbill
             //contact.setWorkEmail(inputAccount.getEmail());
             final Either<ZuoraError, SaveResult> contactOrError = connection.createWithResult(contact);
 
             if (contactOrError.isLeft()) {
                 logService.log(LogService.LOG_INFO, String.format("Could not save contact information for account %s: %s", zuoraAccountId, contactOrError.getLeft().toString()));
                 return Either.left(contactOrError.getLeft());
             }
 
             final Account updateAccount = objectFactory.createAccount();
             updateAccount.setId(zuoraAccountId);
             updateAccount.setInvoiceDeliveryPrefsEmail(false);
             updateAccount.setBillToId(contactOrError.getRight().getId());
             updateAccount.setSoldToId(contactOrError.getRight().getId());
             updateAccount.setStatus("Active");
             updateAccount.setName(inputAccount.getName());
 
             final Either<ZuoraError, String> accountIdOrError = connection.update(updateAccount);
             if (accountIdOrError.isLeft()) {
                 return Either.left(accountIdOrError.getLeft());
             } else {
                 return Either.right(null);
             }
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
     public Either<ZuoraError, Void> setDefaultPaymentMethod(ZuoraConnection connection, String accountKey, PaymentMethod paymentMethod) {
 
         try {
 
             logService.log(LogService.LOG_INFO, String.format("Setting default payment method for account  %s with paymentMethod %s", accountKey, paymentMethod.getId()));
 
             Either<ZuoraError, Account> zuoraAccountOrError = getByAccountName(connection, accountKey);
 
             if (zuoraAccountOrError.isLeft()) {
                 logService.log(LogService.LOG_WARNING, String.format("Could not retrieve zuora account for %s", accountKey));
                 return Either.left(zuoraAccountOrError.getLeft());
             }
 
             if (zuoraAccountOrError.isRight() && zuoraAccountOrError.getRight() == null) {
                 return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, "Could not retrieve zuora account for " + accountKey));
             }
             Account zuoraAccount = zuoraAccountOrError.getRight();
             return setDefaultPaymentMethod(connection, zuoraAccount, paymentMethod);
 
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
 
     public Either<ZuoraError, Void> updatePaymentProviderAccountExistingContact(ZuoraConnection connection, com.ning.billing.account.api.Account account) {
 
         final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
 
         try {
 
             logService.log(LogService.LOG_INFO, String.format("Updating zuora contact %s", account.getExternalKey()));
 
             Either<ZuoraError, Account> zuoraAccountOrError = getByAccountName(connection, account.getExternalKey());
 
             if (zuoraAccountOrError.isLeft()) {
                 logService.log(LogService.LOG_WARNING, String.format("Could not retrieve zuora account for %s", account));
                 return Either.left(zuoraAccountOrError.getLeft());
             }
 
             if (zuoraAccountOrError.isRight() && zuoraAccountOrError.getRight() == null) {
                 return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, "Could not retrieve zuora account for " + account));
             }
 
             Account zuoraAccount = zuoraAccountOrError.getRight();
 
             final Contact contact = objectFactory.createContact();
             contact.setId(zuoraAccountOrError.getRight().getBillToId());
             contact.setFirstName(getFirstName(account));
             contact.setLastName(getLastName(account));
             contact.setWorkPhone(account.getPhone());
             contact.setAddress1(account.getAddress1());
             contact.setAddress2(account.getAddress2());
             contact.setCity(account.getCity());
             contact.setCountry(Strings.emptyToNull(account.getCountry()));
             contact.setState(account.getStateOrProvince());
             contact.setPostalCode(account.getPostalCode());
             // We don't want any email, we manage them in Killbill
             //contact.setWorkEmail(account.getEmail());
 
             final Either<ZuoraError, String> contactOrError = connection.update(contact);
 
             if (contactOrError.isLeft()) {
                 logService.log(LogService.LOG_WARNING, String.format("Could not update contact information for account %s", account));
                 return Either.left(contactOrError.getLeft());
             }
 
             final Account updateAccount = objectFactory.createAccount();
             updateAccount.setId(zuoraAccount.getId());
             updateAccount.setName(account.getName());
 
 
             final Account accountToUpdate = objectFactory.createAccount();
             accountToUpdate.setId(zuoraAccount.getId());
             accountToUpdate.setName(account.getName());
 
             final Either<ZuoraError, String> accountIdOrError = connection.update(accountToUpdate);
             if (accountIdOrError.isLeft()) {
                 return Either.left(accountIdOrError.getLeft());
             } else {
                 return Either.right(null);
             }
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
     public void deleteAccount(ZuoraConnection connection, Account account) {
         connection.delete(Collections.singletonList(account));
 
     }
 
     private String getFirstName(com.ning.billing.account.api.Account account) {
         final Integer firstNameLength = Objects.firstNonNull(account.getFirstNameLength(), 0);
         return StringUtils.defaultIfEmpty(StringUtils.substring(account.getName(), 0, firstNameLength), "");
     }
 
     //
     //                           INVOICE
     //
     public Either<ZuoraError, List<Invoice>> getInvoicesForAccount(final ZuoraConnection connection, final Account account, @Nullable final DateTime from, @Nullable final DateTime to) {
         // We need to round down the to, invoice date in Zuora is in the form 2011-09-29T00:00:00.000-08:00
         final String toDate = to == null ? "" : to.toLocalDate().toDateTimeAtStartOfDay(DateTimeZone.forID("Pacific/Pitcairn")).toString();
 
         final String query;
         if (from == null && to != null) {
             query = stringTemplateLoader.load("getPostedInvoicesForAccountTo")
                                         .define("accountId", account.getId())
                                         .define("invoiceDateTo", toDate)
                                         .build();
         } else if (from != null && to != null) {
             query = stringTemplateLoader.load("getPostedInvoicesForAccountFromTo")
                                         .define("accountId", account.getId())
                                         .define("invoiceDateTo", toDate)
                                         .define("invoiceDateFrom", from.toString())
                                         .build();
         } else {
             throw new UnsupportedOperationException();
         }
 
         final Either<ZuoraError, List<Invoice>> invoicesOrError = connection.query(query);
         if (invoicesOrError.isLeft()) {
             return Either.left(invoicesOrError.getLeft());
         } else {
             return Either.right(invoicesOrError.getRight());
         }
     }
 
     public Either<ZuoraError, String> getInvoiceContent(final ZuoraConnection connection, final Account account, final String invoiceNumber) {
         final String query = stringTemplateLoader.load("getInvoiceContent")
                                                  .define("invoiceNumber", invoiceNumber)
                                                  .build();
         final Either<ZuoraError, Invoice> invoicesOrError = connection.querySingle(query);
         if (invoicesOrError.isLeft()) {
             return Either.left(invoicesOrError.getLeft());
         } else {
             final Invoice invoice = invoicesOrError.getRight();
             if (invoice == null || !invoice.getAccountId().equals(account.getId())) {
                 return Either.right(null);
             } else {
                 return Either.right(invoice.getBody());
             }
         }
     }
 
     public Either<ZuoraError, Payment> getLastPaymentForInvoice(final ZuoraConnection connection, final String invoiceId) {
         final String query = stringTemplateLoader.load("getInvoicePaymentsForInvoice")
                                                  .define("invoiceId", invoiceId)
                                                  .build();
         final Either<ZuoraError, List<InvoicePayment>> invoicePaymentsOrError = connection.query(query);
         if (invoicePaymentsOrError.isLeft()) {
             return Either.left(invoicePaymentsOrError.getLeft());
         } else {
             final List<InvoicePayment> invoicePayments = invoicePaymentsOrError.getRight();
             Payment result = null;
             for (final InvoicePayment ip : invoicePayments) {
                 final Either<ZuoraError, Payment> paymentOrError = getPaymentById(connection, ip.getPaymentId());
                 if (paymentOrError.isLeft()) {
                     return Either.left(paymentOrError.getLeft());
                 } else {
                     final Payment payment = paymentOrError.getRight();
                     if (result == null || result.getCreatedDate().isBefore(payment.getCreatedDate())) {
                         result = payment;
                     }
                 }
             }
 
             return Either.right(result);
         }
     }
 
     //
     //                           PAYMENT
     //
     public Either<ZuoraError, Payment> getPaymentById(ZuoraConnection connection, String paymentId) {
         final String query = stringTemplateLoader.load("getPaymentFromId")
                                                  .define("id", paymentId)
                                                  .build();
         final Either<ZuoraError, Payment> paymentOrError = connection.querySingle(query);
 
         if (paymentOrError.isLeft()) {
             return Either.left(paymentOrError.getLeft());
         } else if (paymentOrError.getRight() == null) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "No payment found for id " + paymentId));
         } else {
             return Either.right(paymentOrError.getRight());
         }
     }
 
     public Either<ZuoraError, Payment> getPaymentForKillbillPayment(ZuoraConnection connection, String accountKey, String kbPaymentId) {
 
         Either<ZuoraError, Account> zuoraAccountOrError = getByAccountName(connection, accountKey);
         if (zuoraAccountOrError.isLeft()) {
             logService.log(LogService.LOG_WARNING, String.format("Could not retrieve zuora account for %s", accountKey));
             return Either.left(zuoraAccountOrError.getLeft());
         }
 
         String accountId = zuoraAccountOrError.getRight().getId();
         Either<ZuoraError, List<Payment>> paymentAccounts = getProcessedPaymentsForAccount(connection, accountId);
         if (paymentAccounts.isLeft()) {
             return Either.left(paymentAccounts.getLeft());
         }
 
         for (Payment cur : paymentAccounts.getRight()) {
             if (cur.getComment() != null && cur.getComment().equals(kbPaymentId)) {
                 return Either.right(cur);
             }
         }
         return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "cannot find zuora payment"));
     }
 
 
     public Either<ZuoraError, List<Payment>> getProcessedPaymentsForAccount(ZuoraConnection connection, String accountId) {
         final String query = stringTemplateLoader.load("getProcessedPaymentsForAccount")
                                                  .define("accountId", accountId)
                                                  .build();
         final Either<ZuoraError, List<Payment>> paymentOrError = connection.query(query);
         if (paymentOrError.isLeft()) {
             return Either.left(paymentOrError.getLeft());
         } else if (paymentOrError.getRight() == null) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "No payment found for accountId " + accountId));
         } else {
             return Either.right(paymentOrError.getRight());
         }
     }
 
 
     private Either<ZuoraError, String> createInvoiceAdjustement(ZuoraConnection connection, String invoiceId, BigDecimal adjustmentAmount, DateTime adjDate) {
 
         logService.log(LogService.LOG_INFO, String.format("Creating invoiceAdjustment for invoiceId = %s, amount = %s", invoiceId, adjustmentAmount));
 
         final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
         InvoiceAdjustment adj = objectFactory.createInvoiceAdjustment();
         adj.setType("Credit");
         adj.setInvoiceId(invoiceId);
         adj.setAmount(adjustmentAmount);
         adj.setAdjustmentDate(adjDate);
 
         Either<ZuoraError, String> adjOrError = connection.createWithId(adj);
         return adjOrError;
     }
 
 
     private Either<ZuoraError, Payment> createPayment(ZuoraConnection connection,
                                                       String accountName,
                                                       String kbPaymentId,
                                                       String accountId,
                                                       String invoiceId,
                                                       String paymentMethodId,
                                                       BigDecimal amount) {
         try {
 
             logService.log(LogService.LOG_INFO, String.format("Creating payment for account %s, amount = %s, paymentMethodId = %s, kbPaymentId = %s, ",
                                                               invoiceId, amount, paymentMethodId, kbPaymentId));
 
             final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
             final Payment payment = objectFactory.createPayment();
 
             payment.setAccountId(accountId);
             payment.setInvoiceId(invoiceId);
             payment.setPaymentMethodId(paymentMethodId);
             payment.setAppliedInvoiceAmount(amount);
             payment.setEffectiveDate(ZuoraDateUtils.now());
             payment.setComment(kbPaymentId);
             payment.setStatus("Processed");
             payment.setType("Electronic");
 
 
             Either<ZuoraError, String> paymentOrError = connection.createWithId(payment);
 
             // zuora may return success with error in status field
             if (paymentOrError.isRight()) {
                 Either<ZuoraError, Payment> paymentOrErrorFromGet = getPaymentById(connection, paymentOrError.getRight());
                 return paymentOrErrorFromGet;
             } else {
                 logService.log(LogService.LOG_WARNING, String.format("Failed to create payment for account %s : %s ", accountName, paymentOrError.getLeft().getMessage()));
                 return Either.left(paymentOrError.getLeft());
             }
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
     public Either<ZuoraError, Payment> processPayment(final ZuoraConnection connection,
                                                       final String accountName,
                                                       final BigDecimal paymentAmount,
                                                       final String kbPaymentId) {
         try {
 
             logService.log(LogService.LOG_INFO, String.format("Processing payment for %s, amount = %s kbPaymentId = %s ", accountName, paymentAmount, kbPaymentId));
 
             final Either<ZuoraError, Account> accountOrError = getByAccountName(connection, accountName);
 
             if (accountOrError.isLeft()) {
                 return Either.left(accountOrError.getLeft());
             }
             final Account zuoraAccount = accountOrError.getRight();
 
             Either<ZuoraError, ProductRatePlanCharge> chargeOrError = getRatePlanCharge(connection);
 
             if (chargeOrError.isLeft()) {
                 return Either.left(chargeOrError.getLeft());
             }
 
             Either<ZuoraError, String> subscriptionOrError = createOrGetSubscription(connection,
                                                                                      zuoraAccount,
                                                                                      chargeOrError.getRight(),
                                                                                      paymentAmount,
                                                                                      kbPaymentId);
             if (subscriptionOrError.isLeft()) {
                 logService.log(LogService.LOG_WARNING, String.format("Failed to create subscription for account %s : %s ", accountName, subscriptionOrError.getLeft().getMessage()));
                 return Either.left(subscriptionOrError.getLeft());
             }
 
             Either<ZuoraError, String> invoiceIdOrError = createOrGetInvoice(connection, zuoraAccount.getId(), kbPaymentId);
             if (invoiceIdOrError.isLeft()) {
                 logService.log(LogService.LOG_WARNING, String.format("Failed to create invoice for account %s : %s ", accountName, invoiceIdOrError.getLeft().getMessage()));
                 return Either.left(invoiceIdOrError.getLeft());
             }
 
             final String invoiceId = invoiceIdOrError.getRight();
             final String paymentMethodId = zuoraAccount.getDefaultPaymentMethodId();
 
             return createPayment(connection, accountName, kbPaymentId, zuoraAccount.getId(), invoiceId, paymentMethodId, paymentAmount);
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
 
     //
     //                        PAYMENT METHODS
     //
     public Either<ZuoraError, List<PaymentMethod>> getPaymentMethodsForAccount(ZuoraConnection connection, Account account) {
         String query = stringTemplateLoader.load("getPaymentMethods")
                                            .define("accountId", account.getId())
                                            .build();
         Either<ZuoraError, List<PaymentMethod>> paymentMethodsOrError = connection.query(query);
 
         if (paymentMethodsOrError.isLeft()) {
             return Either.left(paymentMethodsOrError.getLeft());
         } else {
 
             // We check that payment gateway is correctly set on the GET, this is a bit dirty...
             final List<PaymentMethod> paymentMethods = paymentMethodsOrError.getRight();
             final String defaultAccountPaymentMethodId = account.getDefaultPaymentMethodId();
             final String currentPaymentGateway = account.getPaymentGateway();
             final boolean currentAutoPayStatus = account.isAutoPay();
 
             boolean updatedPaymentGateway = false;
             if (defaultAccountPaymentMethodId != null) {
                 PaymentMethod defaultPaymentMethod = null;
                 for (PaymentMethod cur : paymentMethods) {
                     if (cur.getId().equals(defaultAccountPaymentMethodId)) {
                         defaultPaymentMethod = cur;
                         break;
                     }
                 }
                 if (defaultPaymentMethod == null) {
                     logService.log(LogService.LOG_WARNING, String.format("Failed to get default payment method %s got account  %s",
                                                                          defaultAccountPaymentMethodId, account.getAccountNumber()));
                 } else {
 
                     final String expectedPaymentGateway = getPaymentGateway(account, defaultPaymentMethod);
                     if (!expectedPaymentGateway.equals(currentPaymentGateway)) {
                         updatedPaymentGateway = true;
                         updateAccountForAutoPayAndPaymentGatewayIfNeeded(account.getAccountNumber(), account.getId(), expectedPaymentGateway, currentAutoPayStatus, connection);
                     }
                 }
             }
             if (!updatedPaymentGateway && currentAutoPayStatus) {
                 updateAccountForAutoPayAndPaymentGatewayIfNeeded(account.getAccountNumber(), account.getId(), null, currentAutoPayStatus, connection);
             }
             return Either.right(paymentMethodsOrError.getRight());
         }
     }
 
     private void updateAccountForAutoPayAndPaymentGatewayIfNeeded(final String accountName, final String accountId, final String newPaymentGateway,
                                                                   final boolean updateAutoPay, final ZuoraConnection connection) {
 
         if (newPaymentGateway != null || updateAutoPay) {
 
             final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
             final Account updatedAccount = objectFactory.createAccount();
 
             updatedAccount.setId(accountId);
             if (newPaymentGateway != null) {
                 logService.log(LogService.LOG_INFO, String.format("Updating account %s -> gateway = %s ", accountName, newPaymentGateway));
                 updatedAccount.setPaymentGateway(newPaymentGateway);
             }
             if (updateAutoPay) {
                 logService.log(LogService.LOG_INFO, String.format("Updating account %s -> AUTO_PAY = false ", accountName));
                 updatedAccount.setAutoPay(false);
             }
 
             Either<ZuoraError, String> updateResultOrError = connection.update(updatedAccount);
             if (updateResultOrError.isLeft()) {
                 logService.log(LogService.LOG_WARNING, String.format("Failed to update account  %s", accountName));
             }
         }
     }
 
     public Either<ZuoraError, PaymentMethod> getPaymentMethodById(ZuoraConnection connection, String paymentMethodId) {
         final String query = stringTemplateLoader.load("getPaymentMethod")
                                                  .define("paymentMethodId", paymentMethodId)
                                                  .build();
         final Either<ZuoraError, PaymentMethod> paymentMethodOrError = connection.querySingle(query);
 
         if (paymentMethodOrError.isLeft()) {
             return Either.left(paymentMethodOrError.getLeft());
         } else if (paymentMethodOrError.getRight() == null) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "No payment method found for id " + paymentMethodId));
         } else {
             return Either.right(paymentMethodOrError.getRight());
         }
     }
 
     public Either<ZuoraError, PaymentMethod> addPaymentMethod(ZuoraConnection connection,
                                                               String accountName,
                                                               final PaymentMethodPlugin paymentMethodProps,
                                                               boolean setDefault) {
 
         try {
 
             logService.log(LogService.LOG_INFO, String.format("Adding paymentMethod for account %s, default = %s",
                                                               accountName, setDefault));
 
             final Either<ZuoraError, Account> accountOrError = getByAccountName(connection, accountName);
 
             if (accountOrError.isLeft()) {
                 return Either.left(accountOrError.getLeft());
             }
 
             final String paymentMethodType = paymentMethodProps.getValueString(PaymentMethodProperties.TYPE);
             if (PaypalProperties.TYPE_VALUE.equals(paymentMethodType)) {
                 return addPaypalPaymentMethod(connection, accountOrError.getRight(), paymentMethodProps, setDefault);
             } else if (CreditCardProperties.TYPE_VALUE.equals(paymentMethodType)) {
                 return addCreditCardPaymentMethod(connection, accountOrError.getRight(), paymentMethodProps, setDefault);
             } else {
                 return Either.left(new ZuoraError(ZuoraError.ERROR_UNSUPPORTED, "Payment method " + paymentMethodType + " not supported by the Zuora plugin"));
             }
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
     private Either<ZuoraError, PaymentMethod> addPaypalPaymentMethod(ZuoraConnection connection,
                                                                      Account account,
                                                                      final PaymentMethodPlugin paymentMethodProps,
                                                                      boolean setDefault) {
         final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
 
         try {
 
             final Account zuoraAccount = objectFactory.createAccount();
 
             zuoraAccount.setId(account.getId());
             zuoraAccount.setPaymentGateway(PAYPAL_GATEWAY);
 
             final Either<ZuoraError, String> accountIdOrError = connection.update(zuoraAccount);
 
             if (accountIdOrError.isLeft()) {
                 return Either.left(accountIdOrError.getLeft());
             }
 
             final PaymentMethod paymentMethod = objectFactory.createPaymentMethod();
             paymentMethod.setAccountId(account.getId());
             paymentMethod.setType(PaypalProperties.TYPE_VALUE);
             paymentMethod.setPaypalBaid(paymentMethodProps.getValueString(PaypalProperties.BAID));
             paymentMethod.setPaypalEmail(paymentMethodProps.getValueString(PaypalProperties.EMAIL));
 
             final Either<ZuoraError, String> errorOrId = connection.createWithId(paymentMethod);
 
             if (errorOrId.isRight() && setDefault) {
                 paymentMethod.setId(errorOrId.getRight());
                 final Either<ZuoraError, Void> setPaymentMethodError = setDefaultPaymentMethod(connection, account, paymentMethod);
                 if (setPaymentMethodError.isLeft()) {
                     return Either.left(setPaymentMethodError.getLeft());
                 }
             }
 
             if (errorOrId.isLeft()) {
                 return Either.left(errorOrId.getLeft());
             } else {
                 return Either.right(paymentMethod);
             }
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
 
     public Either<ZuoraError, Void> deletePaymentMethod(ZuoraConnection connection, String accountKey, String paymentMethodId) {
         final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
 
         try {
 
             Either<ZuoraError, Account> zuoraAccountOrError = getByAccountName(connection, accountKey);
             if (zuoraAccountOrError.isLeft()) {
                 logService.log(LogService.LOG_WARNING, String.format("Could not retrieve zuora account for {}", accountKey));
                 return Either.left(zuoraAccountOrError.getLeft());
             }
 
             logService.log(LogService.LOG_INFO, String.format("Deleting paymentMethod for account %s, paymentMethodId = %s",
                                                               accountKey, paymentMethodId));
 
             if (paymentMethodId.equals(zuoraAccountOrError.getRight().getDefaultPaymentMethodId())) {
                 final Either<ZuoraError, String> otherPaymentMethodOrError = getOtherPaymentMethod(connection);
                 if (otherPaymentMethodOrError.isLeft()) {
                     return Either.left(otherPaymentMethodOrError.getLeft());
                 }
 
                 final Account updatedAccount = objectFactory.createAccount();
                 updatedAccount.setId(zuoraAccountOrError.getRight().getId());
                 updatedAccount.setDefaultPaymentMethodId(otherPaymentMethodOrError.getRight());
                 Either<ZuoraError, String> updateResultOrError = connection.update(updatedAccount);
                 if (updateResultOrError.isLeft()) {
                     return new Either.Left<ZuoraError, Void>(updateResultOrError.getLeft());
                 }
 
             }
 
             PaymentMethod paymentMethod = objectFactory.createPaymentMethod();
             paymentMethod.setId(paymentMethodId);
             final Either<ZuoraError, Void> voidOrError = connection.delete(Arrays.asList(paymentMethod));
 
             if (voidOrError.isRight()) {
                 return Either.right(null);
             } else {
                 return Either.left(voidOrError.getLeft());
             }
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
     private Either<ZuoraError, String> getOtherPaymentMethod(ZuoraConnection connection) {
 
         final String query = stringTemplateLoader.load("getOtherPaymentMethod")
                                                  .build();
 
         final Either<ZuoraError, PaymentMethod> pmOrError = connection.querySingle(query);
         if (pmOrError.isLeft()) {
             return Either.left(pmOrError.getLeft());
         } else {
             return Either.right(pmOrError.getRight().getId());
         }
     }
 
 
     // This method is only used for testing, in live system we don't pass credit card numbers
     private Either<ZuoraError, PaymentMethod> addCreditCardPaymentMethod(ZuoraConnection connection,
                                                                          Account account,
                                                                          PaymentMethodPlugin creditCardPaymentMethod,
                                                                          final boolean setDefault) {
 
         final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
 
         try {
             final Account zuoraAccount = objectFactory.createAccount();
             zuoraAccount.setId(account.getId());
 
             final Either<ZuoraError, String> accountIdOrError = connection.update(zuoraAccount);
 
             if (accountIdOrError.isLeft()) {
                 return Either.left(accountIdOrError.getLeft());
             }
 
             PaymentMethod paymentMethod = createCreditCardPaymentMethod(connection, account.getId(), creditCardPaymentMethod);
             final Either<ZuoraError, String> errorOrId = connection.createWithId(paymentMethod);
             if (errorOrId.isRight() && setDefault) {
                 paymentMethod.setId(errorOrId.getRight());
                 final Either<ZuoraError, Void> setPaymentMethodError = setDefaultPaymentMethod(connection, account, paymentMethod);
                 if (setPaymentMethodError.isLeft()) {
                     return Either.left(setPaymentMethodError.getLeft());
                 }
             }
 
             if (errorOrId.isLeft()) {
                 return Either.left(errorOrId.getLeft());
             } else {
                 return Either.right(paymentMethod);
             }
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
     public static String[] parseExpirationDate(String expirationDate) {
         return (expirationDate != null) ? expirationDate.split("-") : null;
     }
 
 
     private PaymentMethod createCreditCardPaymentMethod(ZuoraConnection connection, String accountId, PaymentMethodPlugin creditCardPaymentMethod) {
 
         final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
         final PaymentMethod paymentMethod = objectFactory.createPaymentMethod();
 
         if (creditCardPaymentMethod.getExternalPaymentMethodId() != null) {
             // UPDATE
             paymentMethod.setId(creditCardPaymentMethod.getExternalPaymentMethodId());
         } else {
             // CREATE
             paymentMethod.setType(CreditCardProperties.TYPE_VALUE);
             paymentMethod.setAccountId(accountId);
             paymentMethod.setCreditCardType(creditCardPaymentMethod.getValueString(CreditCardProperties.CARD_TYPE));
             paymentMethod.setCreditCardNumber(creditCardPaymentMethod.getValueString(CreditCardProperties.MASK_NUMBER));
         }
 
         if (creditCardPaymentMethod.getValueString(CreditCardProperties.CARD_HOLDER_NAME) != null) {
             paymentMethod.setCreditCardHolderName(creditCardPaymentMethod.getValueString(CreditCardProperties.CARD_HOLDER_NAME));
         }
         String[] tokens = parseExpirationDate(creditCardPaymentMethod.getValueString(CreditCardProperties.EXPIRATION_DATE));
         if (tokens != null && tokens.length == 2) {
             paymentMethod.setCreditCardExpirationYear(Integer.parseInt(tokens[0]));
             paymentMethod.setCreditCardExpirationMonth(Integer.parseInt(tokens[1]));
         }
         if (creditCardPaymentMethod.getValueString(CreditCardProperties.ADDRESS1) != null) {
             paymentMethod.setCreditCardAddress1(creditCardPaymentMethod.getValueString(CreditCardProperties.ADDRESS1));
         }
         if (creditCardPaymentMethod.getValueString(CreditCardProperties.ADDRESS2) != null) {
             paymentMethod.setCreditCardAddress2(creditCardPaymentMethod.getValueString(CreditCardProperties.ADDRESS2));
         }
         if (creditCardPaymentMethod.getValueString(CreditCardProperties.CITY) != null) {
             paymentMethod.setCreditCardCity(creditCardPaymentMethod.getValueString(CreditCardProperties.CITY));
         }
         if (creditCardPaymentMethod.getValueString(CreditCardProperties.POSTAL_CODE) != null) {
             paymentMethod.setCreditCardPostalCode(creditCardPaymentMethod.getValueString(CreditCardProperties.POSTAL_CODE));
         }
         if (creditCardPaymentMethod.getValueString(CreditCardProperties.STATE) != null) {
             paymentMethod.setCreditCardState(creditCardPaymentMethod.getValueString(CreditCardProperties.STATE));
         }
         if (creditCardPaymentMethod.getValueString(CreditCardProperties.COUNTRY) != null) {
             paymentMethod.setCreditCardCountry(creditCardPaymentMethod.getValueString(CreditCardProperties.COUNTRY));
         }
         return paymentMethod;
     }
 
     public Either<ZuoraError, PaymentMethod> updateCreditCardPaymentMethod(ZuoraConnection connection, Account account, PaymentMethodPlugin creditCardPaymentMethod) {
 
         try {
             PaymentMethod paymentMethod = createCreditCardPaymentMethod(connection, account.getId(), creditCardPaymentMethod);
             Either<ZuoraError, String> result = connection.update(paymentMethod);
             if (result.isLeft()) {
                 return Either.left(result.getLeft());
             }
             if (creditCardPaymentMethod.isDefaultPaymentMethod()) {
                 final Either<ZuoraError, Void> setPaymentMethodError = setDefaultPaymentMethod(connection, account, paymentMethod);
                 if (setPaymentMethodError.isLeft()) {
                     return Either.left(setPaymentMethodError.getLeft());
                 }
             }
             return Either.right(paymentMethod);
 
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
     public Either<ZuoraError, List<Subscription>> getSubscriptionsForAccount(ZuoraConnection connection, String accountId) {
 
         final String query = stringTemplateLoader.load("getSubscriptionsForAccount")
                                                  .define("accountId", accountId)
                                                  .build();
 
         Either<ZuoraError, List<Subscription>> subscriptionsOrError = connection.query(query);
         return subscriptionsOrError;
     }
 
 
     public Either<ZuoraError, List<Invoice>> getPostedInvoicesForAccount(ZuoraConnection connection, String accountId) {
 
         final String query = stringTemplateLoader.load("getPostedInvoicesForAccount")
                                                  .define("accountId", accountId)
                                                  .build();
 
         Either<ZuoraError, List<Invoice>> invoicesOrError = connection.query(query);
         return invoicesOrError;
     }
 
 
     //
     //                       OTHER STUFF
     //
     public Either<ZuoraError, ProductRatePlanCharge> getRatePlanCharge(ZuoraConnection connection) {
         ProductRatePlanCharge productRatePlanCharge = killBillPlan.get();
 
         if (productRatePlanCharge == null) {
             Either<ZuoraError, ProductRatePlanCharge> chargeOrError = loadRatePlanCharge(connection);
 
             if (chargeOrError.isRight()) {
                 killBillPlan.set(chargeOrError.getRight());
             }
             return chargeOrError;
         } else {
             return Either.right(productRatePlanCharge);
         }
     }
 
     public Either<ZuoraError, ProductRatePlanCharge> loadRatePlanCharge(ZuoraConnection connection) {
         final String query = stringTemplateLoader.load("getRatePlanCharge")
                                                  .define("name", config.getRatePlanChargeName())
                                                  .build();
 
         final Either<ZuoraError, ProductRatePlanCharge> ratePlanChargeOrError = connection.querySingle(query);
 
         if (ratePlanChargeOrError.isLeft()) {
             return Either.left(ratePlanChargeOrError.getLeft());
         } else if (ratePlanChargeOrError.getRight() == null) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "No rate plan charge found with name " + config.getRatePlanChargeName()));
         } else {
             return Either.right(ratePlanChargeOrError.getRight());
         }
     }
 
     public Either<ZuoraError, Refund> createRefund(ZuoraConnection connection,
                                                  String paymentId,
                                                  String kbPaymentId,
                                                  BigDecimal refundAmount) {
 
         final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
         final Refund refund = objectFactory.createRefund();
 
         logService.log(LogService.LOG_INFO, String.format("Creating refund for payment %s, amount = %s, kbPaymentId = %s",
                                                           paymentId, refundAmount, kbPaymentId));
 
         try {
 
             refund.setAmount(refundAmount);
             refund.setPaymentId(paymentId);
             refund.setType("Electronic");
 
             Either<ZuoraError, String> refundOrError = connection.createWithId(refund);
             if (refundOrError.isLeft()) {
                 return Either.left(refundOrError.getLeft());
             } else {
 
                 Either<ZuoraError, String> invoiceOrError = getInvoiceIdForPayment(connection, paymentId, kbPaymentId);
                 if (invoiceOrError.isLeft()) {
                     logService.log(LogService.LOG_WARNING, String.format("Cannot find invoice for kbPayment %s", kbPaymentId));
                     return Either.left(invoiceOrError.getLeft());
                 }
 
                 Either<ZuoraError, String> adjOrError = createInvoiceAdjustement(connection, invoiceOrError.getRight(), refundAmount, new DateTime());
                 if (adjOrError.isLeft()) {
                     logService.log(LogService.LOG_WARNING, String.format("Failed to adjust for refundId = %d, payment = %s, invoice = %s ",
                                                                          refundOrError.getRight(), paymentId, invoiceOrError.getRight()));
                     return Either.left(adjOrError.getLeft());
                 }
                 return getRefundById(connection, refundOrError.getRight());
             }
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
     private Either<ZuoraError, Refund> getRefundById(final ZuoraConnection connection, final String refundId) {
 
         final String query = stringTemplateLoader.load("getRefundFromId")
                                                  .define("id", refundId)
                                                  .build();
         final Either<ZuoraError, Refund> refundOrError = connection.querySingle(query);
 
         if (refundOrError.isLeft()) {
             return Either.left(refundOrError.getLeft());
         } else if (refundOrError.getRight() == null) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "No refund found for id " + refundId));
         } else {
             return Either.right(refundOrError.getRight());
         }
     }
 
 
     public Either<ZuoraError, String> getInvoiceIdForPayment(ZuoraConnection connection,
                                                              String paymentId,
                                                              String kbPaymentId) {
 
         if (config.shouldCheckForStatePayment()) {
             Either<ZuoraError, String> invoiceOrError = getInvoiceForPayment(connection, kbPaymentId);
             if (invoiceOrError.isLeft()) {
                 logService.log(LogService.LOG_WARNING, String.format("Cannot find invoice for kbPayment %s", kbPaymentId));
                 return Either.left(invoiceOrError.getLeft());
             }
             return Either.right(invoiceOrError.getRight());
         } else {
             Either<ZuoraError, List<InvoicePayment>> invoicePaymentsOrError = getInvoicePayments(connection, paymentId);
             if (invoicePaymentsOrError.isLeft()) {
                 logService.log(LogService.LOG_WARNING, String.format("Cannot find InvoicePayment for payment %s", paymentId));
                 return Either.left(invoicePaymentsOrError.getLeft());
             }
             final List<InvoicePayment> invoicePayments = invoicePaymentsOrError.getRight();
             if (invoicePayments.size() > 1) {
                 logService.log(LogService.LOG_ERROR, String.format("Found more than one InvoicePayment for payment %s", paymentId));
                 return Either.left(new ZuoraError(ZuoraError.ERROR_UNSUPPORTED, "Unepected list of InvoicePayment greater than 1"));
             }
             return Either.right(invoicePayments.get(0).getInvoiceId());
         }
     }
 
 
     public Either<ZuoraError, List<RefundInvoicePayment>> getRefundsForPayment(ZuoraConnection connection, String paymentId) {
 
         try {
 
             List<RefundInvoicePayment> okResult = new ArrayList<RefundInvoicePayment>();
 
             Either<ZuoraError, List<InvoicePayment>> invPayOrError = getInvoicePayments(connection, paymentId);
             if (invPayOrError.isLeft()) {
                 return Either.left(invPayOrError.getLeft());
             }
             for (InvoicePayment cur : invPayOrError.getRight()) {
                 Either<ZuoraError, List<RefundInvoicePayment>> refOrError = getRefundInvoicePayment(connection, cur.getId());
                 if (refOrError.isLeft()) {
                     return Either.left(refOrError.getLeft());
                 }
                 okResult.addAll(refOrError.getRight());
             }
             return Either.right(okResult);
 
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
     private Either<ZuoraError, List<RefundInvoicePayment>> getRefundInvoicePayment(ZuoraConnection connection, String invoicePaymentId) {
 
         try {
             final String query = stringTemplateLoader.load("getRefundInvoicePayment")
                                                      .define("invoicePaymentId", invoicePaymentId)
                                                      .build();
             return connection.query(query);
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
 
     }
 
 
     private Either<ZuoraError, List<InvoicePayment>> getInvoicePayments(ZuoraConnection connection, String paymentId) {
 
         try {
             final String query = stringTemplateLoader.load("getInvoicePayments")
                                                      .define("paymentId", paymentId)
                                                      .build();
 
             final Either<ZuoraError, List<InvoicePayment>> invpayOrError = connection.query(query);
             if (invpayOrError.isLeft()) {
                 return Either.left(invpayOrError.getLeft());
             } else {
                 return Either.right(invpayOrError.getRight());
             }
         } catch (Exception ex) {
             return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
         }
     }
 
 
     private Either<ZuoraError, String> createOrGetSubscription(ZuoraConnection connection,
                                                                Account account,
                                                                ProductRatePlanCharge productRatePlanCharge,
                                                                BigDecimal newPrice,
                                                                String paymentId) {
 
         Either<ZuoraError, String> subscriptionOrError = getSubscriptionForPayment(connection, paymentId);
         if (subscriptionOrError.isRight()) {
             if (subscriptionOrError.getRight() != null) {
                 logService.log(LogService.LOG_WARNING, String.format("Found existing z_subscription %s for kbPaymentId %s", subscriptionOrError.getRight(), paymentId));
                 return subscriptionOrError;
             } else {
                 return createSubscription(connection, account, productRatePlanCharge, newPrice, paymentId);
             }
         }
         return Either.left(subscriptionOrError.getLeft());
     }
 
 
     private Either<ZuoraError, String> getSubscriptionForPayment(ZuoraConnection connection, String paymentId) {
 
         if (!config.shouldCheckForStatePayment()) {
             return Either.right(null);
         }
 
         final String query = stringTemplateLoader.load("getSubscriptionByKillbilPaymentId")
                                                  .define("paymentId", paymentId)
                                                  .build();
 
         final Either<ZuoraError, Subscription> subscriptionOrError = connection.querySingle(query);
         if (subscriptionOrError.isLeft()) {
             return Either.left(subscriptionOrError.getLeft());
         } else {
             return Either.right(subscriptionOrError.getRight() != null ? subscriptionOrError.getRight().getId() : null);
         }
     }
 
     private Either<ZuoraError, String> createSubscription(ZuoraConnection connection,
                                                           Account account,
                                                           ProductRatePlanCharge productRatePlanCharge,
                                                           BigDecimal newPrice,
                                                           String paymentId) {
         final com.zuora.api.ObjectFactory apiFactory = connection.getApiFactory();
         final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
         final DateTime zuoraEffectiveDate = ZuoraDateUtils.toDateTime(new DateTime().minusDays(1));
         final int duration = 1;
 
 
         Subscription zuoraSubscription = objectFactory.createSubscription();
         zuoraSubscription.setName(paymentId);
         zuoraSubscription.setAutoRenew(false);
         zuoraSubscription.setRenewalTerm(0);
         zuoraSubscription.setContractAcceptanceDate(zuoraEffectiveDate);
         zuoraSubscription.setContractEffectiveDate(zuoraEffectiveDate);
         zuoraSubscription.setInitialTerm(duration);
         zuoraSubscription.setTermStartDate(zuoraEffectiveDate);
         zuoraSubscription.setKillbillC(paymentId);
 
         //        zuoraSubscription.setAppIDC("1234");
 
         // Populate all required fields for RatePlan object. Use a valid ID for ProductRatePlanId.
         RatePlan zuoraRatePlan = objectFactory.createRatePlan();
         zuoraRatePlan.setProductRatePlanId(productRatePlanCharge.getProductRatePlanId());
 
         // Pass in a valid RatePlanChargeData object. Again, if no RatePlanChargeData objects are passed,
         // the default RatePlanCharges will copied from the RatePlan object whose ID is passed in the RatePlan object in step 5.2.
         RatePlanCharge zuoraRatePlanCharge = objectFactory.createRatePlanCharge();
         zuoraRatePlanCharge.setProductRatePlanChargeId(productRatePlanCharge.getId());
         zuoraRatePlanCharge.setRatePlanId(zuoraRatePlan.getId());
         zuoraRatePlanCharge.setPrice(newPrice);
         zuoraRatePlanCharge.setTriggerEvent("SpecificDate");
         zuoraRatePlanCharge.setTriggerDate(zuoraEffectiveDate);
 
         RatePlanChargeData zuoraRatePlanChargeData = apiFactory.createRatePlanChargeData();
         zuoraRatePlanChargeData.setRatePlanCharge(zuoraRatePlanCharge);
 
         RatePlanData zuoraRatePlanData = apiFactory.createRatePlanData();
         zuoraRatePlanData.setRatePlan(zuoraRatePlan);
         zuoraRatePlanData.getRatePlanChargeData().add(zuoraRatePlanChargeData);
 
         // Populate all the required fields for the SubscriptionData object
         SubscriptionData zuoraSubscriptionData = new SubscriptionData();
 
         zuoraSubscriptionData.setSubscription(zuoraSubscription);
 
         SubscribeOptions opts = apiFactory.createSubscribeOptions();
 
         opts.setGenerateInvoice(false);
         opts.setProcessPayments(false);
 
         final Account zuoraAccount = objectFactory.createAccount();
         zuoraAccount.setId(account.getId());
 
         // Create a SubscribeRequest object and set all the objects created in the previous steps.
         SubscribeRequest zuoraSubscribeRequest = new SubscribeRequest();
 
         zuoraSubscribeRequest.setSubscribeOptions(opts);
         zuoraSubscribeRequest.setSubscriptionData(zuoraSubscriptionData);
         zuoraSubscribeRequest.setAccount(zuoraAccount);
         zuoraSubscriptionData.getRatePlanData().add(zuoraRatePlanData);
 
         Either<ZuoraError, SubscribeResult> result = connection.subscribe(zuoraSubscribeRequest);
         if (result.isLeft()) {
             return Either.left(result.getLeft());
         } else {
             return Either.right(result.getRight().getSubscriptionId());
         }
     }
 
     private Either<ZuoraError, String> createOrGetInvoice(ZuoraConnection connection,
                                                           String accountId, String kbPaymentId) {
 
         Either<ZuoraError, String> invoiceOrError = getInvoiceForPayment(connection, kbPaymentId);
         if (invoiceOrError.isRight()) {
             if (invoiceOrError.getRight() != null) {
                 logService.log(LogService.LOG_WARNING, String.format("Found existing z_invoice %s for kbPaymentId %s", invoiceOrError.getRight(), kbPaymentId));
                 return invoiceOrError;
             } else {
                 return createInvoice(connection, accountId, kbPaymentId);
             }
         }
         return Either.left(invoiceOrError.getLeft());
     }
 
     private Either<ZuoraError, String> getInvoiceForPayment(ZuoraConnection connection,
                                                             String kbPaymentId) {
 
         if (!config.shouldCheckForStatePayment()) {
             return Either.right(null);
         }
 
         final String query = stringTemplateLoader.load("getInvoiceByKillbillPaymentId")
                                                  .define("paymentId", kbPaymentId)
                                                  .build();
 
         final Either<ZuoraError, Invoice> invoiceOrError = connection.querySingle(query);
         if (invoiceOrError.isLeft()) {
             return Either.left(invoiceOrError.getLeft());
         } else {
             return Either.right(invoiceOrError.getRight() != null ? invoiceOrError.getRight().getId() : null);
         }
     }
 
     private Either<ZuoraError, String> createInvoice(ZuoraConnection connection,
                                                      String accountId, String kbPaymentId) {
         final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
 
         Invoice invoice = objectFactory.createInvoice();
         invoice.setAccountId(accountId);
         invoice.setIncludesOneTime(true);
         invoice.setIncludesRecurring(false);
         invoice.setIncludesUsage(false);
         DateTime date = new DateTime();
         invoice.setInvoiceDate(date);
         invoice.setTargetDate(date);
 
         Either<ZuoraError, String> errorOrId = connection.createWithId(invoice);
         if (errorOrId.isLeft()) {
             return Either.left(errorOrId.getLeft());
         }
 
         invoice = objectFactory.createInvoice();
         invoice.setId(errorOrId.getRight());
         invoice.setKillbillC(kbPaymentId);
         invoice.setStatus("Posted");
 
         return connection.update(invoice);
     }
 
 
     private Either<ZuoraError, Void> setDefaultPaymentMethod(ZuoraConnection connection, Account account, PaymentMethod paymentMethod)
             throws Exception {
 
         Either<ZuoraError, Void> result = new Either.Right<ZuoraError, Void>(null);
         if (account.getDefaultPaymentMethodId() != null && account.getDefaultPaymentMethodId().equals(paymentMethod.getId())) {
             return result;
         }
 
         final com.zuora.api.object.ObjectFactory objectFactory = connection.getObjectFactory();
         final Account updatedAccount = objectFactory.createAccount();
 
         updatedAccount.setId(account.getId());
         updatedAccount.setDefaultPaymentMethodId(paymentMethod.getId());
         updatedAccount.setPaymentGateway(getPaymentGateway(account, paymentMethod));
 
         Either<ZuoraError, String> updateResultOrError = connection.update(updatedAccount);
         if (updateResultOrError.isLeft()) {
             return new Either.Left<ZuoraError, Void>(updateResultOrError.getLeft());
         }
         return result;
     }
 
 
     private String getPaymentGateway(final Account account, final PaymentMethod paymentMethod) {
         return getPaymentGateway(account, paymentMethod.getType());
     }
 
     private String getPaymentGateway(final Account account, final String type) {
         if ("paypal".equalsIgnoreCase(type)) {
             return PaymentGateway.PAYPAL.name();
         }
         if (config.isOverrideCreditcardGateway()) {
             return config.getOverrideCreditcardGateway();
         } else {
             return PaymentGateway.valueOf(account.getCurrency()).name();
 
         }
     }
 
     enum PaymentGateway {
         PAYPAL,
         AUD,
         BRL,
         CAD,
         EUR,
         GBP,
         MXN,
         USD;
     }
 }
