 package com.financial.pyramid.service.impl;
 
 import com.financial.pyramid.domain.Operation;
 import com.financial.pyramid.domain.User;
 import com.financial.pyramid.paypal.PayPal;
 import com.financial.pyramid.paypal.PayPalPropeties;
 import com.financial.pyramid.service.ApplicationConfigurationService;
 import com.financial.pyramid.service.OperationsService;
 import com.financial.pyramid.service.PayPalService;
 import com.financial.pyramid.service.beans.PayPalDetails;
 import com.financial.pyramid.service.beans.PayPalResponse;
 import com.financial.pyramid.service.beans.Receiver;
 import com.financial.pyramid.service.exception.PayPalException;
 import com.financial.pyramid.settings.Setting;
 import com.financial.pyramid.utils.HTTPClient;
 import com.financial.pyramid.utils.Session;
 import com.google.gdata.util.common.base.Pair;
 import com.google.gson.Gson;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * User: dbudunov
  * Date: 13.08.13
  * Time: 19:50
  */
 @Service
 public class PayPalServiceImpl implements PayPalService {
 
     @Autowired
     ApplicationConfigurationService configurationService;
 
     @Autowired
     OperationsService operationsService;
 
     @Override
     public String processPayment(PayPalDetails payPalDetails) throws PayPalException {
         PayPalResponse payPalResponse = processPayPalRequest(payPalDetails, true);
         return PayPalPropeties.PAY_PAL_PAYMENT_URL + "?cmd=_ap-payment&paykey=" + payPalResponse.payKey;
     }
 
     @Override
     public boolean processTransfer(PayPalDetails details) throws PayPalException {
         PayPalResponse payPalResponse = processPayPalRequest(details, false);
         return isTransactionCompleted(payPalResponse.payKey, PayPalPropeties.PAY_PAL_PAY_KEY);
     }
 
     @Override
     public boolean isTransactionCompleted(String transactionId) throws PayPalException {
         boolean result = false;
         String token = configurationService.getParameter(Setting.PAY_PAL_API_TOKEN);
         String url = PayPalPropeties.PAY_PAL_PAYMENT_URL + "?cmd=_notify-synch&tx=" + transactionId + "&at=" + token;
         try {
             List<String> response = HTTPClient.sendRequest(url);
             PayPalResponse payPalResponse = new Gson().fromJson(response.get(0), PayPalResponse.class);
            result = response.toString().contains("COMPLETED") && response.toString().contains("SUCCESS");
             if (result) {
                 operationsService.update(payPalResponse.trackingId, result);
             }
         } catch (Exception e) {
             throw new PayPalException(e.getMessage());
         }
         return result;
     }
 
     @Override
     public boolean isTransactionCompleted(String uid, String type) throws PayPalException {
         boolean result = false;
         try {
             String url = PayPal.getPaymentDetailsUrl(uid, type);
             List<String> response = HTTPClient.sendRequestWithHeaders(url, getHeaders(), RequestMethod.GET.name());
             PayPalResponse payPalResponse = new Gson().fromJson(response.get(0), PayPalResponse.class);
            result = payPalResponse.status != null && payPalResponse.status.equals("COMPLETED");
             if (result) {
                 operationsService.update(payPalResponse.trackingId, result);
             }
         } catch (Exception e) {
             throw new PayPalException(e.getMessage());
         }
         return result;
     }
 
     private PayPalResponse processPayPalRequest(PayPalDetails payPalDetails, boolean isPurchasePayment) {
         PayPalResponse payPalResponse;
         try {
             updatePayPalDetails(payPalDetails);
             String url = isPurchasePayment ? PayPal.getPaymentUrl(payPalDetails) : PayPal.getTransferUrl(payPalDetails);
             List<String> response = HTTPClient.sendRequestWithHeaders(url, getHeaders(), RequestMethod.GET.name());
             payPalResponse = new Gson().fromJson(response.get(0), PayPalResponse.class);
             logOperation(payPalDetails, payPalResponse);
         } catch (Exception e) {
             throw new PayPalException(e.getMessage());
         }
         return payPalResponse;
     }
 
     @Override
     public void updatePayPalDetails(PayPalDetails details) {
         details.currencyCode = configurationService.getParameter(Setting.PAY_PAL_CURRENCY);
         details.actionType = details.actionType == null ? PayPalPropeties.PAY_PAL_ACTION_TYPE : details.actionType;
         details.returnUrl = details.returnUrl == null ? PayPalPropeties.PAY_PAL_RETURN_URL : details.returnUrl;
         details.cancelUrl = details.cancelUrl == null ? PayPalPropeties.PAY_PAL_CANCEL_URL : details.cancelUrl;
         details.currencyCode = details.currencyCode == null ? PayPalPropeties.PAY_PAL_CURRENCY : details.currencyCode;
         details.feesPayer = details.feesPayer == null ? PayPalPropeties.PAY_PAL_FEES_PAYER : details.feesPayer;
         details.globalId = PayPal.generateTransactionUID(details);
 
         List<Receiver> receivers = new ArrayList<Receiver>();
         Receiver receiver = new Receiver();
         receiver.amount = details.amount;
         receiver.email = details.receiverEmail;
         receivers.add(receiver);
         details.receiverList = receivers;
     }
 
     private void logOperation(PayPalDetails payPalDetails, PayPalResponse payPalResponse) {
         String result = isSuccessfulPayment(payPalResponse.responseEnvelope.ack) ? "Success" : "Failed";
 
         Operation operation = new Operation();
         operation.setMemo(payPalDetails.memo);
         operation.setType(payPalDetails.actionType);
         operation.setPayer(payPalDetails.senderEmail);
         operation.setGlobalId(payPalDetails.globalId);
         operation.setDate(new Date(System.currentTimeMillis()));
         operation.setPayee(payPalDetails.receiverList.get(0).email);
         operation.setAmount(Double.valueOf(payPalDetails.receiverList.get(0).amount));
         operation.setSuccess(isSuccessfulPayment(payPalResponse.responseEnvelope.ack));
         operation.setResult(result);
 
         User user = Session.getCurrentUser();
         operation.setUserId(user.getId());
 
         String errorString = "";
         if (payPalResponse.error != null && payPalResponse.error.size() > 0) {
             for (int i = 0; i < payPalResponse.error.size(); i++) {
                 errorString += payPalResponse.error.get(i).message;
             }
         }
         if (!errorString.isEmpty()) {
             operation.setError(errorString);
         }
         operationsService.save(operation);
     }
 
     private List<Pair<String, String>> getHeaders() {
         List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
         String securityUserId = configurationService.getParameter(Setting.PAY_PAL_API_USERNAME);
         String securityPassword = configurationService.getParameter(Setting.PAY_PAL_API_PASSWORD);
         String securitySignature = configurationService.getParameter(Setting.PAY_PAL_API_SIGNATURE);
         String applicationId = configurationService.getParameter(Setting.PAY_PAL_API_APPLICATION_ID);
         headers.add(new Pair<String, String>("X-PAYPAL-SECURITY-USERID", securityUserId));
         headers.add(new Pair<String, String>("X-PAYPAL-SECURITY-PASSWORD", securityPassword));
         headers.add(new Pair<String, String>("X-PAYPAL-SECURITY-SIGNATURE", securitySignature));
         headers.add(new Pair<String, String>("X-PAYPAL-REQUEST-DATA-FORMAT", "NV"));
         headers.add(new Pair<String, String>("X-PAYPAL-RESPONSE-DATA-FORMAT", "JSON"));
         headers.add(new Pair<String, String>("X-PAYPAL-APPLICATION-ID", applicationId));
         return headers;
     }
 
     public boolean isSuccessfulPayment(String response) {
         return response.contains("Success") || response.equals("Success");
     }
 
     public boolean isSuccessfulPayment(List<String> response) {
         return response.toString().contains("Success") || response.toString().equals("Success");
     }
 }
