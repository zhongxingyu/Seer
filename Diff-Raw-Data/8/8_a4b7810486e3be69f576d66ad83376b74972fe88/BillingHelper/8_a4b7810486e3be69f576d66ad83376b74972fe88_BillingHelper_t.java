 /*******************************************************************************
  * Copyright 2013 One Platform Foundation
  *
  *       Licensed under the Apache License, Version 2.0 (the "License");
  *       you may not use this file except in compliance with the License.
  *       You may obtain a copy of the License at
  *
  *           http://www.apache.org/licenses/LICENSE-2.0
  *
  *       Unless required by applicable law or agreed to in writing, software
  *       distributed under the License is distributed on an "AS IS" BASIS,
  *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *       See the License for the specific language governing permissions and
  *       limitations under the License.
  ******************************************************************************/
 
 package org.onepf.life;
 
 import android.content.Intent;
 import android.util.Log;
 import com.amazon.inapp.purchasing.PurchasingManager;
 import org.onepf.life.amazon.AmazonHelper;
 import org.onepf.life.google.GooglePlayHelper;
 import org.onepf.life.samsung.SamsungHelper;
 import org.onepf.life.tstore.TstoreHelper;
 
 /**
  * User: Boris Minaev
  * Date: 27.03.13
  * Time: 19:35
  */
 
 public class BillingHelper {
     private BasePurchaseHelper currentHelper;
 
     public BillingHelper(GameActivity activity) {
         new GooglePlayHelper(activity, this);
         new AmazonHelper(activity, this);
         new SamsungHelper(activity, this);
         new TstoreHelper(activity, this);
     }
 
     public boolean updateHelper(BasePurchaseHelper helper) {
         if (currentHelper == null || currentHelper.getPriority() < helper.getPriority()) {
             currentHelper = helper;
             return true;
         }
         return false;
     }
 
     public void onBuyChanges() {
        Log.d(GameActivity.TAG, "onBuyChanges in BillingHelper");
         if (currentHelper != null) {
             currentHelper.onBuyChanges();
         }
     }
 
     public void onBuyOrangeCells() {
        Log.d(GameActivity.TAG, "onBuyOrangeCells in BillingHelper");
         if (currentHelper != null) {
             currentHelper.onBuyOrangeCells();
         }
     }
 
     public void onBuyFigures() {
        Log.d(GameActivity.TAG, "onBuyFigures in BillingHelper");
         if (currentHelper != null) {
             currentHelper.onBuyFigures();
         }
     }
 
     public void onResume() {
         if (currentHelper != null) {
             if (currentHelper.getMarket() == Market.AMAZON_APP_STORE) {
                 PurchasingManager.initiateGetUserIdRequest();
             }
         }
     }
 
     public boolean isMainMarket(BasePurchaseHelper helper) {
         return helper == currentHelper;
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (currentHelper != null) {
             if (currentHelper.getMarket() == Market.GOOGLE_PLAY) {
                 currentHelper
                         .onActivityResult(requestCode, resultCode, data);
             }
         }
     }
 }
