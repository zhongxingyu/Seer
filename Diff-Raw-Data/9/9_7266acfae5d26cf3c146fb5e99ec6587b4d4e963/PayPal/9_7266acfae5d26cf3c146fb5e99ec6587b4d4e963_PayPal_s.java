 package com.financial.pyramid.paypal;
 
 import com.financial.pyramid.service.beans.PayPalDetails;
 import com.financial.pyramid.utils.MD5Encoder;
 import org.joda.time.DateTime;
 
 import java.util.List;
 
 /**
  * User: dbudunov
  * Date: 02.09.13
  * Time: 21:18
  */
 public class PayPal {
     public static String getTransferUrl(PayPalDetails details) {
         return PayPalPropeties.PAY_PAL_ADAPTIVE_PAYMENT_URL
                 + "?actionType=" + PayPalPropeties.PAY_PAL_ACTION_TYPE
                 + "&cancelUrl=" + details.cancelUrl
                 + "&currencyCode=" + details.currencyCode
                 + "&receiverList.receiver(0).amount=" + details.receiverList.get(0).amount
                 + "&receiverList.receiver(0).email=" + details.receiverList.get(0).email
                 + "&receiverList.receiver(0).primary=" + details.receiverList.get(0).primary
                 + "&requestEnvelope.errorLanguage=" + getErrorLanguage(details)
                 + "&returnUrl=" + details.returnUrl
                 + "&senderEmail=" + details.senderEmail
                 + "&trackingId=" + details.globalId
                 + "&feesPayer=" + PayPalPropeties.PAY_PAL_FEES_PAYER
                + "&memo=" + details.memo
                + "&preapprovalKey=" + (details.preApprovalKey != null ? details.preApprovalKey : "");
     }
 
     public static String getPaymentUrl(PayPalDetails details) {
         return PayPalPropeties.PAY_PAL_ADAPTIVE_PAYMENT_URL
                 + "?actionType=" + PayPalPropeties.PAY_PAL_ACTION_TYPE
                 + "&cancelUrl=" + details.cancelUrl
                 + "&currencyCode=" + details.currencyCode
                 + "&receiverList.receiver(0).amount=" + details.receiverList.get(0).amount
                 + "&receiverList.receiver(0).email=" + details.receiverList.get(0).email
                 + "&receiverList.receiver(0).primary=" + details.receiverList.get(0).primary
                 + "&requestEnvelope.errorLanguage=" + getErrorLanguage(details)
                 + "&requestEnvelope.detailLevel=ReturnAll"
                 + "&returnUrl=" + details.returnUrl
                + "&memo=" + details.memo
                + "&trackingId=" + details.globalId;
     }
 
     public static String getPaymentDetailsUrl(String uid, String type) {
         String url = PayPalPropeties.PAY_PAL_PAYMENT_DETAILS_URL;
         url += "?requestEnvelope.errorLanguage=" + getErrorLanguage(new PayPalDetails());
         url += "&requestEnvelope.detailLevel=ReturnAll";
         if (type.equals(PayPalPropeties.PAY_PAL_PAY_KEY)) {
             url += "&payKey=" + uid;
         } else if (type.equals(PayPalPropeties.PAY_PAL_TRANSACTION)) {
             url += "&transactionId=" + uid;
         } else if (type.equals(PayPalPropeties.PAY_PAL_TRACKING_ID)) {
             url += "&trackingId=" + uid;
         }
         return url;
     }
 
     public static String getErrorLanguage(PayPalDetails details) {
         String errorLanguage = PayPalPropeties.PAY_PAL_DEFAULT_ERROR_LANGUAGE;
         if (details.requestEnvelope != null && details.requestEnvelope.errorLanguage != null) {
             errorLanguage = details.requestEnvelope.errorLanguage;
         }
         return errorLanguage;
     }
 
     public static String generateTransactionUID(PayPalDetails details) {
         return MD5Encoder.encode(details.senderEmail + details.receiverEmail + details.amount + new DateTime());
     }
 
 }
