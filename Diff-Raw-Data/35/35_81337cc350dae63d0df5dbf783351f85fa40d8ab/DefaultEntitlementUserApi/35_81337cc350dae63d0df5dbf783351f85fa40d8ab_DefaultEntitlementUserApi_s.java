 /*
  * Copyright 2010-2011 Ning, Inc.
  *
  * Ning licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.ning.billing.entitlement.api.user;
 
 import java.util.List;
 import java.util.UUID;
 import org.joda.time.DateTime;
 import com.google.inject.Inject;
 import com.ning.billing.ErrorCode;
 import com.ning.billing.catalog.api.CatalogApiException;
 import com.ning.billing.catalog.api.CatalogService;
 import com.ning.billing.catalog.api.Plan;
 import com.ning.billing.catalog.api.PlanPhase;
 import com.ning.billing.catalog.api.PlanPhaseSpecifier;
 import com.ning.billing.catalog.api.PriceListSet;
 import com.ning.billing.catalog.api.Product;
 import com.ning.billing.entitlement.api.user.Subscription.SubscriptionState;
 import com.ning.billing.entitlement.api.user.SubscriptionFactory.SubscriptionBuilder;
 import com.ning.billing.entitlement.engine.addon.AddonUtils;
 import com.ning.billing.entitlement.engine.dao.EntitlementDao;
 import com.ning.billing.entitlement.exceptions.EntitlementError;
 import com.ning.billing.util.clock.Clock;
 import com.ning.billing.util.clock.DefaultClock;
 
 public class DefaultEntitlementUserApi implements EntitlementUserApi {
 
     private final Clock clock;
     private final EntitlementDao dao;
     private final CatalogService catalogService;
     private final SubscriptionApiService apiService;
     private final AddonUtils addonUtils;
 
     @Inject
     public DefaultEntitlementUserApi(Clock clock, EntitlementDao dao, CatalogService catalogService,
             SubscriptionApiService apiService, AddonUtils addonUtils) {
         super();
         this.clock = clock;
         this.apiService = apiService;
         this.dao = dao;
         this.catalogService = catalogService;
         this.addonUtils = addonUtils;
     }
 
     @Override
     public SubscriptionBundle getBundleFromId(UUID id) {
         return dao.getSubscriptionBundleFromId(id);
     }
 
     @Override
     public Subscription getSubscriptionFromId(UUID id) {
         return dao.getSubscriptionFromId(id);
     }
 
     @Override
     public SubscriptionBundle getBundleForKey(String bundleKey) {
         return dao.getSubscriptionBundleFromKey(bundleKey);
     }
 
     @Override
     public List<SubscriptionBundle> getBundlesForAccount(UUID accountId) {
         return dao.getSubscriptionBundleForAccount(accountId);
     }
 
     @Override
     public List<Subscription> getSubscriptionsForKey(String bundleKey) {
         return dao.getSubscriptionsForKey(bundleKey);
     }
 
 
     @Override
     public List<Subscription> getSubscriptionsForBundle(UUID bundleId) {
         return dao.getSubscriptions(bundleId);
     }
 
     @Override
     public SubscriptionBundle createBundleForAccount(UUID accountId, String bundleName)
     throws EntitlementUserApiException {
         SubscriptionBundleData bundle = new SubscriptionBundleData(bundleName, accountId);
         return dao.createSubscriptionBundle(bundle);
     }
 
 
     @Override
     public Subscription createSubscription(UUID bundleId, PlanPhaseSpecifier spec, DateTime requestedDate) throws EntitlementUserApiException {
         try {
             String realPriceList = (spec.getPriceListName() == null) ? PriceListSet.DEFAULT_PRICELIST_NAME : spec.getPriceListName();
             DateTime now = clock.getUTCNow();
             requestedDate = (requestedDate != null) ? DefaultClock.truncateMs(requestedDate) : now;
             if (requestedDate != null && requestedDate.isAfter(now)) {
                 throw new EntitlementUserApiException(ErrorCode.ENT_INVALID_REQUESTED_DATE, requestedDate.toString());
             }
             DateTime effectiveDate = requestedDate;
 
             Plan plan = catalogService.getFullCatalog().findPlan(spec.getProductName(), spec.getBillingPeriod(), realPriceList, requestedDate);
 
             PlanPhase phase = plan.getAllPhases()[0];
             if (phase == null) {
                 throw new EntitlementError(String.format("No initial PlanPhase for Product %s, term %s and set %s does not exist in the catalog",
                         spec.getProductName(), spec.getBillingPeriod().toString(), realPriceList));
             }
 
             SubscriptionBundle bundle = dao.getSubscriptionBundleFromId(bundleId);
             if (bundle == null) {
                 throw new EntitlementUserApiException(ErrorCode.ENT_CREATE_NO_BUNDLE, bundleId);
             }
 
             DateTime bundleStartDate = null;
             SubscriptionData baseSubscription = (SubscriptionData) dao.getBaseSubscription(bundleId);
             switch(plan.getProduct().getCategory()) {
             case BASE:
                 if (baseSubscription != null) {
                     throw new EntitlementUserApiException(ErrorCode.ENT_CREATE_BP_EXISTS, bundleId);
                 }
                 bundleStartDate = requestedDate;
                 break;
             case ADD_ON:
                 if (baseSubscription == null) {
                     throw new EntitlementUserApiException(ErrorCode.ENT_CREATE_NO_BP, bundleId);
                 }
                 checkAddonCreationRights(baseSubscription, plan);
                 bundleStartDate = baseSubscription.getStartDate();
                 break;
             default:
                 throw new EntitlementError(String.format("Can't create subscription of type %s",
                         plan.getProduct().getCategory().toString()));
             }
 
             SubscriptionData subscription = apiService.createPlan(new SubscriptionBuilder()
                 .setId(UUID.randomUUID())
                 .setBundleId(bundleId)
                 .setCategory(plan.getProduct().getCategory())
                 .setBundleStartDate(bundleStartDate)
                 .setStartDate(effectiveDate),
             plan, spec.getPhaseType(), realPriceList, requestedDate, effectiveDate, now);
 
             return subscription;
         } catch (CatalogApiException e) {
             throw new EntitlementUserApiException(e);
         }
     }
 
 
     private void checkAddonCreationRights(SubscriptionData baseSubscription, Plan targetAddOnPlan)
         throws EntitlementUserApiException, CatalogApiException {
 
         if (baseSubscription.getState() != SubscriptionState.ACTIVE) {
             throw new EntitlementUserApiException(ErrorCode.ENT_CREATE_AO_BP_NON_ACTIVE, targetAddOnPlan.getName());
         }
 
         if (addonUtils.isAddonIncluded(baseSubscription, targetAddOnPlan)) {
             throw new EntitlementUserApiException(ErrorCode.ENT_CREATE_AO_ALREADY_INCLUDED,
                     targetAddOnPlan.getName(), baseSubscription.getCurrentPlan().getProduct().getName());
         }
 
         if (!addonUtils.isAddonAvailable(baseSubscription, targetAddOnPlan)) {
             throw new EntitlementUserApiException(ErrorCode.ENT_CREATE_AO_NOT_AVAILABLE,
                     targetAddOnPlan.getName(), baseSubscription.getCurrentPlan().getProduct().getName());
         }
     }
 
 	@Override
 	public DateTime getNextBillingDate(UUID accountId) {
 		List<SubscriptionBundle> bundles = getBundlesForAccount(accountId);
 		DateTime result = null;
 		for(SubscriptionBundle bundle : bundles) {
 			List<Subscription> subscriptions = getSubscriptionsForBundle(bundle.getId());
 			for(Subscription subscription : subscriptions) {
 				DateTime chargedThruDate = subscription.getChargedThroughDate();
 				if(result == null ||
 						(chargedThruDate != null && chargedThruDate.isBefore(result))) {
 					result = subscription.getChargedThroughDate();
 				}
 			}
 		}
 		return result;
 	}
 }
