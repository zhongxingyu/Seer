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
 
 package com.ning.killbill.zuora.api;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.ning.billing.payment.api.PaymentMethodPlugin;
 import com.ning.killbill.zuora.method.CreditCardProperties;
 import com.ning.killbill.zuora.zuora.ZuoraApi;
 
 public class ZuoraPaymentMethodPlugin implements PaymentMethodPlugin {
 
     private final String paymentMethodId;
     private boolean isDefault;
     private List<PaymentMethodKVInfo> properties;
     
     public ZuoraPaymentMethodPlugin(final String paymentMethodId, boolean isDefault, final List<PaymentMethodKVInfo> properties) {
         this.paymentMethodId = paymentMethodId;
         this.isDefault = isDefault;
         this.properties = properties;
     }
     
     public ZuoraPaymentMethodPlugin(final String paymentMethodId, boolean isDefault) {
         this.paymentMethodId = paymentMethodId;
         this.isDefault = isDefault;
         this.properties = new LinkedList<PaymentMethodPlugin.PaymentMethodKVInfo>();
     }
 
     @Override
     public String getExternalPaymentMethodId() {
         return paymentMethodId;
     }
 
     @Override
     public boolean isDefaultPaymentMethod() {
         return isDefault;
     }
 
     @Override
     public List<PaymentMethodKVInfo> getProperties() {
         return properties;
     }
     
     public void addProperty(String key, Object value, boolean isEditable) {
         properties.add(new PaymentMethodKVInfo(key, value, isEditable));
     }
 
 
     @Override
     public String getValueString(String key) {
         if (properties == null) {
             return null;
         }
         for (PaymentMethodKVInfo cur : properties) {
             if (cur.getKey().equals(key)) {
                 return cur.getValue() != null ? cur.getValue().toString() : null;
             }
         }
         return null;
     }
 
 
     @Override
     public String getType() {
         return getValueString(CreditCardProperties.TYPE_VALUE);
     }
 
     @Override
     public String getCCName() {
         return getValueString(CreditCardProperties.CARD_HOLDER_NAME);
     }
 
     @Override
     public String getCCType() {
         return getValueString(CreditCardProperties.CARD_TYPE);
     }
 
     @Override
    public String getCCExprirationMonth() {
         final String [] parts = ZuoraApi.parseExpirationDate(getValueString(CreditCardProperties.EXPIRATION_DATE));
         return parts != null && parts.length == 2 ? parts[1] : null;
     }
 
     @Override
    public String getCCExprirationYear() {
         final String [] parts = ZuoraApi.parseExpirationDate(getValueString(CreditCardProperties.EXPIRATION_DATE));
         return parts != null && parts.length == 2 ? parts[0] : null;
     }
 
     @Override
     public String getCCLast4() {
         return getValueString(CreditCardProperties.MASK_NUMBER);
     }
 
     @Override
     public String getAddress1() {
         return getValueString(CreditCardProperties.ADDRESS1);
     }
 
     @Override
     public String getAddress2() {
         return getValueString(CreditCardProperties.ADDRESS2);
     }
 
     @Override
     public String getCity() {
         return getValueString(CreditCardProperties.CITY);
     }
 
     @Override
     public String getState() {
         return getValueString(CreditCardProperties.STATE);
     }
 
     @Override
     public String getZip() {
         return getValueString(CreditCardProperties.POSTAL_CODE);
     }
 
     @Override
     public String getCountry() {
         return getValueString(CreditCardProperties.COUNTRY);
     }
 
 }
