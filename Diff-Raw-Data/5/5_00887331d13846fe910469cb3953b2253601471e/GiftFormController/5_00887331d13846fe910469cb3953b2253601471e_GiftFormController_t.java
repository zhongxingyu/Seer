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
 
 package com.orangeleap.tangerine.controller.gift;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.AbstractEntity;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.domain.paymentInfo.Pledge;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.service.PicklistItemService;
 import com.orangeleap.tangerine.service.PledgeService;
 import com.orangeleap.tangerine.service.RecurringGiftService;
 import com.orangeleap.tangerine.type.PaymentType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.List;
 import java.util.Map;
 
 public class GiftFormController extends AbstractGiftController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
     @Resource(name = "pledgeService")
     protected PledgeService pledgeService;
 
     @Resource(name = "recurringGiftService")
     protected RecurringGiftService recurringGiftService;
 
 	@Resource(name = "picklistItemService")
 	protected PicklistItemService picklistItemService;
 
     @Override
     protected AbstractEntity findEntity(HttpServletRequest request) {
         Gift gift = giftService.readGiftByIdCreateIfNull(getConstituent(request), request.getParameter(StringConstants.GIFT_ID));
         clearPaymentSourceFields(gift);
         clearAddressFields(gift);
         clearPhoneFields(gift);
 	    return gift;
     }
 
     private boolean isEnteredGift(Gift gift) {
     	return gift != null && !gift.isNew();
     }
 
     private boolean canReprocessGift(Gift gift) {
    	return isEnteredGift(gift) && (Gift.PAY_STATUS_DECLINED.equals(gift.getPaymentStatus()) || Gift.PAY_STATUS_ERROR.equals(gift.getPaymentStatus()));
     }
 
     private boolean showGiftView(Gift gift) {
     	return isEnteredGift(gift) &&
 			    ((PaymentType.OTHER.getPaymentName().equals(gift.getPaymentType()) && Gift.STATUS_PAID.equals(gift.getGiftStatus())) || 
 			    PaymentType.CASH.getPaymentName().equals(gift.getPaymentType()) ||
 			    PaymentType.CHECK.getPaymentName().equals(gift.getPaymentType()) ||
     			((PaymentType.ACH.getPaymentName().equals(gift.getPaymentType()) || PaymentType.CREDIT_CARD.getPaymentName().equals(gift.getPaymentType())) &&
     					Gift.STATUS_PAID.equals(gift.getGiftStatus()) && Gift.PAY_STATUS_APPROVED.equals(gift.getPaymentStatus())));
     }
 
 	@Override
 	protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
 		ModelAndView mav = super.showForm(request, response, errors);
         TangerineForm form = (TangerineForm) formBackingObject(request);
 		Gift gift = (Gift) form.getDomainObject();
 
         if (showGiftView(gift)) {
             String redirectUrl = appendGiftParameters(request, giftViewUrl, gift);
             if ("true".equalsIgnoreCase(request.getParameter(StringConstants.SAVED))) {
                 redirectUrl = appendSaved(redirectUrl);
             }
         	mav = new ModelAndView(redirectUrl);
         }
 		return mav;
 	}
 
     @SuppressWarnings("unchecked")
     @Override
     protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
         Map refMap = super.referenceData(request, command, errors);
 
         String selectedPledgeId = request.getParameter("selectedPledgeId");
         String selectedRecurringGiftId = request.getParameter("selectedRecurringGiftId");
         if (NumberUtils.isDigits(selectedPledgeId)) {
             Pledge pledge = pledgeService.readPledgeById(Long.parseLong(selectedPledgeId));
             refMap.put("associatedPledge", pledge);
         }
         else if (NumberUtils.isDigits(selectedRecurringGiftId)) {
             RecurringGift recurringGift = recurringGiftService.readRecurringGiftById(Long.parseLong(selectedRecurringGiftId));
             refMap.put("associatedRecurringGift", recurringGift);
         }
 
         TangerineForm form = (TangerineForm) command;
 	    Gift gift = (Gift) form.getDomainObject();
         if (canReprocessGift(gift)) {
         	refMap.put("allowReprocess", Boolean.TRUE);
         }
         return refMap;
     }
 
 	@Override
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
         binder.registerCustomEditor(List.class, "associatedPledgeIds", new AssociationEditor());
         binder.registerCustomEditor(List.class, "associatedRecurringGiftIds", new AssociationEditor());
     }
 
 	@Override
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException formErrors) throws Exception {
 		TangerineForm form = (TangerineForm) command;
 		Gift gift = (Gift) form.getDomainObject();
         checkAssociations(gift);
 
         boolean saved = true;
         try {
         	if ("true".equals(request.getParameter("doReprocess")) && canReprocessGift(gift)) {
         		gift = giftService.reprocessGift(gift);
         	}
         	else if (gift.isNew()) {
         		gift = giftService.maintainGift(gift);
         	}
             else {
                 gift = giftService.editGift(gift);
             }
         }
         catch (BindException domainErrors) {
             saved = false;
 	        bindDomainErrorsToForm(request, formErrors, domainErrors, form, gift);
         }
 
         ModelAndView mav;
         if (saved) {
             mav = new ModelAndView(super.appendSaved(appendGiftParameters(request, getSuccessView(), gift)));
         }
         else {
 			checkAssociations(gift);
 			mav = showForm(request, formErrors, getFormView());
         }
         return mav;
     }
 
     protected void checkAssociations(Gift gift) {
         giftService.checkAssociatedPledgeIds(gift);
         giftService.checkAssociatedRecurringGiftIds(gift);
     }
 }
