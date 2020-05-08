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
 package com.orangeleap.tangerine.web.flow.batch;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.CommunicationHistory;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.PaymentSource;
 import com.orangeleap.tangerine.domain.PostBatch;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.customization.CustomField;
 import com.orangeleap.tangerine.domain.customization.FieldDefinition;
 import com.orangeleap.tangerine.domain.customization.Picklist;
 import com.orangeleap.tangerine.domain.customization.PicklistItem;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.domain.paymentInfo.AbstractPaymentInfoEntity;
 import com.orangeleap.tangerine.domain.paymentInfo.AdjustedGift;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.service.AdjustedGiftService;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.service.PicklistItemService;
 import com.orangeleap.tangerine.service.PostBatchService;
 import com.orangeleap.tangerine.service.customization.FieldService;
 import com.orangeleap.tangerine.service.customization.PageCustomizationService;
 import com.orangeleap.tangerine.type.FieldType;
 import com.orangeleap.tangerine.type.PageType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineMessageAccessor;
 import com.orangeleap.tangerine.web.common.SortInfo;
 import com.orangeleap.tangerine.web.common.TangerineListHelper;
 import com.orangeleap.tangerine.web.customization.tag.fields.SectionFieldTag;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.ExtTypeHandler;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.FieldHandler;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.FieldHandlerHelper;
 import com.orangeleap.tangerine.web.flow.AbstractAction;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.stereotype.Component;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.StringUtils;
 import org.springframework.webflow.execution.RequestContext;
 
 @Component("editBatchAction")
 public class EditBatchAction extends AbstractAction {
 
     protected final Log logger = OLLogger.getLog(getClass());
 
     public static final String PARAM_PREFIX = "param-";
     public static final String BATCH_FIELDS = "BatchFields";
     public static final String PREVIOUS_STEP = "previousStep";
     public static final String ACCESSIBLE_STEPS = "accessibleSteps";
     public static final String STEP_1_GRP = "step1Grp";
     public static final String STEP_2_GRP = "step2Grp";
     public static final String STEP_3_GRP = "step3Grp";
     public static final String STEP_4_GRP = "step4Grp";
     public static final String STEP_5_GRP = "step5Grp";
 
     @Resource(name = "postBatchService")
     protected PostBatchService postBatchService;
 
 	@Resource(name = "constituentService")
 	protected ConstituentService constituentService;
 
     @Resource(name = "giftService")
     protected GiftService giftService;
 
     @Resource(name = "adjustedGiftService")
     protected AdjustedGiftService adjustedGiftService;
 
     @Resource(name = "pageCustomizationService")
     protected PageCustomizationService pageCustomizationService;
 
     @Resource(name = "picklistItemService")
     protected PicklistItemService picklistItemService;
 
     @Resource(name = "tangerineListHelper")
     protected TangerineListHelper tangerineListHelper;
 
     @Resource(name = "fieldService")
     protected FieldService fieldService;
 
 	@Resource(name = "fieldHandlerHelper")
 	protected FieldHandlerHelper fieldHandlerHelper;
 
     protected PostBatch getBatchFromFlowScope(final RequestContext flowRequestContext) {
         return (PostBatch) getFlowScopeAttribute(flowRequestContext, StringConstants.BATCH);
     }
     
     private String resolveSegmentationFieldName(String key) {
         String resolvedName = key;
         if (StringConstants.NAME.equals(key)) {
             resolvedName = "reportName";
         }
         else if ("desc".equals(key)) {
             resolvedName = "reportComment";
         }
         else if ("count".equals(key)) {
             resolvedName = "resultCount";
         }
         else if ("lastDt".equals(key)) {
             resolvedName = "lastRunDateTime";
         }
         else if ("lastUser".equals(key)) {
             resolvedName = "lastRunByUserName";
         }
         return resolvedName;
     }
 
     private void determineStepToSave(final RequestContext flowRequestContext) {
         final String previousStep = getRequestParameter(flowRequestContext, PREVIOUS_STEP);
         if (STEP_1_GRP.equals(previousStep)) {
             saveBatchInfo(flowRequestContext);
         }
         else if (STEP_2_GRP.equals(previousStep)) {
             savePickedSegmentationIds(flowRequestContext);
         }
         else if (STEP_4_GRP.equals(previousStep)) {
             saveBatchUpdateFields(flowRequestContext);
         }
     }
 
     private void saveBatchInfo(final RequestContext flowRequestContext) {
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         final String batchDesc = getRequestParameter(flowRequestContext, StringConstants.BATCH_DESC);
         final String batchType = getRequestParameter(flowRequestContext, StringConstants.BATCH_TYPE);
 	    final boolean isForTouchPoints = StringConstants.TOUCH_POINT.equals(getRequestParameter(flowRequestContext, StringConstants.CRITERIA_FIELDS));
 
         /**
          * Check if the batchType is different from what was previously entered during the flow - if so, reset the previously selected segmentation IDs and
          * the updated fields
          */
         if (batch.getBatchType() != null && StringUtils.hasText(batch.getBatchType()) &&
                 StringUtils.hasText(batchType) && ! batchType.equals(batch.getBatchType())) {
             batch.clearPostBatchEntries();
             batch.clearUpdateFields();
         }
 	    if (isForTouchPoints != batch.isForTouchPoints()) {
 		    batch.clearUpdateFields();
 	    }
 
 		batch.setBatchDesc(batchDesc);
 		batch.setBatchType(batchType);
 	    batch.setForTouchPoints(isForTouchPoints);
     }
 
     private void savePickedSegmentationIds(final RequestContext flowRequestContext) {
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         final String pickedIds = getRequestParameter(flowRequestContext, "pickedIds");
         final String notPickedIds = getRequestParameter(flowRequestContext, "notPickedIds");
         syncPickedSegmentationIds(flowRequestContext, batch, pickedIds, notPickedIds);
     }
 
     protected void saveBatchUpdateFields(final RequestContext flowRequestContext) {
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         final HttpServletRequest request = getRequest(flowRequestContext);
         final Map<String, Object> enteredParams = findEnteredParameters(request);
 
         BeanWrapper bean = createDefaultEntity(batch);
         batch.clearUpdateFields();
 
         for (String thisKey : enteredParams.keySet()) {
 	        String fieldDefinitionId = resolveFieldDefinitionId(batch, thisKey);
 	        FieldDefinition fieldDef = fieldService.resolveFieldDefinition(fieldDefinitionId);
 	        if (fieldDef != null && bean.isReadableProperty(fieldDef.getFieldName())) {
 		        // the updateField key will be the fieldDefinitionId (adjustedGift.status) which will need to be resolved to the fieldName (adjustedGift.adjustedStatus)
		        String value = ( enteredParams.get(thisKey) == null ? null : enteredParams.get(thisKey).toString() );
		        if ( (! batch.isForTouchPoints() && StringUtils.hasText(value)) || batch.isForTouchPoints()) {
			        batch.addUpdateField(thisKey, value); // update the batch; allow null values only for touch point fields
		        }
 	        }
         }
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap step1FindBatchInfo(final RequestContext flowRequestContext) {
         String batchIdStr = getRequestParameter(flowRequestContext, "batchId");
         Long batchId = null;
         if (NumberUtils.isDigits(batchIdStr)) {
             batchId = new Long(getRequestParameter(flowRequestContext, "batchId"));
         }
         if (logger.isTraceEnabled()) {
             logger.trace("step1FindBatchInfo: batchId = " + batchId);
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch); // TODO: do as annotation
         determineStepToSave(flowRequestContext);
 
         final ModelMap model = new ModelMap();
         PostBatch batch = getBatchFromFlowScope(flowRequestContext);
 
         model.put(StringConstants.SUCCESS, Boolean.TRUE);
         final Map<String, String> dataMap = new HashMap<String, String>();
         model.put(StringConstants.DATA, dataMap);
 
         /* Create a new batch if one doesn't already exist in this scope or the ID differs */
         if (batch == null || (batchId != null && ! batchId.equals(batch.getId()))) {
             batch = postBatchService.readBatchCreateIfNull(batchId);
             
             if (batch.isNew()) {
                 batch.setBatchDesc(StringConstants.EMPTY);
                 batch.setBatchType(StringConstants.GIFT); // default batch type is gift
 	            batch.setForTouchPoints(false); // default criteria fields is NOT touch points
             }
 
             // add only the PostBatch to the view scope and remove from the returnMap
             setFlowScopeAttribute(flowRequestContext, batch, StringConstants.BATCH);
         }
         dataMap.put(StringConstants.BATCH_DESC, batch.getBatchDesc());
         dataMap.put(StringConstants.BATCH_TYPE, batch.getBatchType());
 	    dataMap.put(StringConstants.CRITERIA_FIELDS, batch.isForTouchPoints() ? StringConstants.TOUCH_POINT : StringConstants.NOT_TOUCH_POINT);
 
         model.put(ACCESSIBLE_STEPS, determineAccessibleSteps(flowRequestContext));
         return model;
     }
 
     @SuppressWarnings("unchecked")
     private void syncPickedSegmentationIds(final RequestContext flowRequestContext, final PostBatch batch, final String pickedIdsStr, final String notPickedIdsStr) {
 
         /**
          * Because users can pick/unpick IDs across multiple grid pages in the step 2 grid, we need to keep a running tab of which segmentation
          * IDs the users selected
          */
         Set<Long> pickedSegmentationIds = batch.getEntrySegmentationIds();
         Set<String> pickedIds = StringUtils.commaDelimitedListToSet(pickedIdsStr);
         Set<String> notPickedIds = StringUtils.commaDelimitedListToSet(notPickedIdsStr);
 
         Collection<String> commonIds = CollectionUtils.intersection(pickedIds, notPickedIds); // there should not be any IDs BOTH picked and not-picked, but check just in case
         if (commonIds != null && ! commonIds.isEmpty()) {
             // If there are Picked and Not Picked Ids, just assume they are picked
             notPickedIds = new TreeSet<String>(CollectionUtils.subtract(notPickedIds, commonIds));
         }
 
         /* Add any new 'pickedIds' */
         for (String thisPickedId : pickedIds) {
             if (NumberUtils.isDigits(thisPickedId)) {
                 if ( ! pickedSegmentationIds.contains(new Long(thisPickedId))) {
                     pickedSegmentationIds.add(new Long(thisPickedId));
                 }
             }
         }
 
         Iterator<Long> pickedSegIter = pickedSegmentationIds.iterator();
 
         /* Remove any 'not picked' segmentations */
         if ( ! notPickedIds.isEmpty()) {
             while (pickedSegIter.hasNext()) {
                 Long segmentationId =  pickedSegIter.next();
                 if (notPickedIds.contains(segmentationId.toString())) {
                     pickedSegIter.remove();
                 }
             }
         }
         batch.clearAddAllPostBatchEntriesForSegmentations(pickedSegmentationIds);
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap step2FindSegmentations(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("step2FindSegmentations:");
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch); // TODO: do as annotation
         determineStepToSave(flowRequestContext);
         
         final ModelMap model = new ModelMap();
         final SortInfo sortInfo = getSortInfo(flowRequestContext);
         
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
 
         Set<Long> pickedSegmentationIds = batch.getEntrySegmentationIds(); 
         model.put("pickedSegmentationsCount", pickedSegmentationIds == null ? 0 : pickedSegmentationIds.size());
         model.put(StringConstants.ROWS, postBatchService.findSegmentationsForBatchType(batch, pickedSegmentationIds, batch.getBatchType(),
                 resolveSegmentationFieldName(sortInfo.getSort()), sortInfo.getDir(),
                 sortInfo.getStart(), sortInfo.getLimit()));
         model.put(StringConstants.TOTAL_ROWS, postBatchService.findTotalSegmentations(batch.getBatchType())); 
 
         model.put(ACCESSIBLE_STEPS, determineAccessibleSteps(flowRequestContext));
         return model;
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap step3FindRowsForSegmentations(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("step3FindRowsForSegmentations:");
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
         determineStepToSave(flowRequestContext);
 
         final ModelMap model = new ModelMap();
         final HttpServletRequest request = getRequest(flowRequestContext);
         final SortInfo sortInfo = getSortInfo(flowRequestContext);
 
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
 
         Set<Long> pickedSegmentationIds = batch.getEntrySegmentationIds();
 
         /* Clear out and add all segmentations */
         batch.clearAddAllPostBatchEntriesForSegmentations(pickedSegmentationIds); // This needs to be done on load for step 3
         createJsonModel(request, batch, model, sortInfo);
         model.put(StringConstants.SUCCESS, Boolean.TRUE);
         
         model.put(ACCESSIBLE_STEPS, determineAccessibleSteps(flowRequestContext));
         return model;
     }
 
     @SuppressWarnings("unchecked")
     private void createJsonModel(HttpServletRequest request, PostBatch batch, Map model, SortInfo sort) {
         /* MetaData */
         final Map<String, Object> metaDataMap = tangerineListHelper.initMetaData(sort.getStart(), sort.getLimit());
         metaDataMap.put(StringConstants.LIMIT, sort.getLimit());
 
 	    final String pageType;
 	    if (batch.isForTouchPoints()) {
 		    if (StringConstants.GIFT.equals(batch.getBatchType())) {
 		        pageType = "batchGiftConstituentList";
 		    }
 		    else {
 			    pageType = "batchAdjustedGiftConstituentList";
 		    }
 	    }
 	    else {
 		    pageType = batch.getBatchType() + "List";
 	    }
         final List<SectionField> allFields = tangerineListHelper.findSectionFields(pageType);
 
         final BeanWrapper bw = createDefaultEntity(batch);
         final List<Map<String, Object>> fieldList = new ArrayList<Map<String, Object>>();
 
         addIdConstituentIdFields(fieldList, bw);
         for (SectionField sectionFld : allFields) {
             final Map<String, Object> fieldMap = new HashMap<String, Object>();
             String escapedFieldName = TangerineForm.escapeFieldName(sectionFld.getFieldPropertyName());
             fieldMap.put(StringConstants.NAME, escapedFieldName);
             fieldMap.put(StringConstants.MAPPING, escapedFieldName);
             String extType = ExtTypeHandler.findExtDataType(bw.getPropertyType(sectionFld.getFieldPropertyName()));
             fieldMap.put(StringConstants.TYPE, extType);
             fieldMap.put(StringConstants.HEADER, sectionFld.getFieldDefinition().getDefaultLabel());
 
             if (StringConstants.DATE.equals(extType)) {
                 String format;
                 if (FieldType.CC_EXPIRATION.equals(sectionFld.getFieldType()) || FieldType.CC_EXPIRATION_DISPLAY.equals(sectionFld.getFieldType())) {
                     format = "m-d-Y";
                 }
                 else {
                     format = StringConstants.EXT_DATE_TIME_FORMAT;
                 }
                 fieldMap.put(StringConstants.DATE_FORMAT, format);
             }
             fieldList.add(fieldMap);
         }
         metaDataMap.put(StringConstants.FIELDS, fieldList);
 
         final Map<String, String> sortInfoMap = new HashMap<String, String>();
         if ( ! StringUtils.hasText(sort.getSort()) || ! bw.isReadableProperty(TangerineForm.unescapeFieldName(sort.getSort()))) {
             final List<SectionField> fieldsExceptId = pageCustomizationService.getFieldsExceptId(allFields);
             sort.setSort(TangerineForm.escapeFieldName(fieldsExceptId.get(0).getFieldPropertyName()));
             sort.setDir(SectionFieldTag.getInitDirection(fieldsExceptId));
         }
 
         sortInfoMap.put(StringConstants.FIELD, sort.getSort());
         sortInfoMap.put(StringConstants.DIRECTION, sort.getDir());
         metaDataMap.put(StringConstants.SORT_INFO, sortInfoMap);
 
         model.put(StringConstants.META_DATA, metaDataMap);
 
         Set<Long> reportIds = batch.getEntrySegmentationIds();
         List rowObjects = null;
         int totalRows = 0;
         if ( ! reportIds.isEmpty()) {
 	        sort.setSort(TangerineForm.unescapeFieldName(sort.getSort())); // unescape for the DB
 	        if (StringConstants.CONSTITUENT.equals(batch.getBatchType())) {
 	        }
             else if (StringConstants.GIFT.equals(batch.getBatchType())) {
                 totalRows = giftService.readCountGiftsBySegmentationReportIds(reportIds);
                 rowObjects = giftService.readGiftsBySegmentationReportIds(reportIds, sort, request.getLocale());
             }
             else if (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType())) {
                 totalRows = adjustedGiftService.readCountAdjustedGiftsBySegmentationReportIds(reportIds);
                 rowObjects = adjustedGiftService.readAdjustedGiftsBySegmentationReportIds(reportIds, sort, request.getLocale());
             }
         }
         model.put(StringConstants.TOTAL_ROWS, totalRows);
 
         List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();
         tangerineListHelper.addListFieldsToMap(request, allFields, rowObjects, rowList, false, false); // this needs to be 'allFields' to include the 'id' property
         addConstituentIdsToRows(rowList, rowObjects);
         model.put(StringConstants.ROWS, rowList);
     }
 
     protected void addIdConstituentIdFields(final List<Map<String, Object>> fieldList, final BeanWrapper bw) {
         final Map<String, Object> idMap = new HashMap<String, Object>();
         idMap.put(StringConstants.NAME, StringConstants.ID);
         idMap.put(StringConstants.MAPPING, StringConstants.ID);
         idMap.put(StringConstants.TYPE, ExtTypeHandler.EXT_INT);
         idMap.put(StringConstants.HEADER, TangerineMessageAccessor.getMessage(StringConstants.ID));
         fieldList.add(idMap);
 
         if (bw.isReadableProperty(StringConstants.CONSTITUENT) || bw.isReadableProperty(StringConstants.CONSTITUENT_ID)) {
             final Map<String, Object> constituentIdMap = new HashMap<String, Object>();
             constituentIdMap.put(StringConstants.NAME, StringConstants.CONSTITUENT_ID);
             constituentIdMap.put(StringConstants.MAPPING, StringConstants.CONSTITUENT_ID);
             constituentIdMap.put(StringConstants.TYPE, ExtTypeHandler.EXT_STRING);
             constituentIdMap.put(StringConstants.HEADER, TangerineMessageAccessor.getMessage(StringConstants.CONSTITUENT_ID));
             fieldList.add(constituentIdMap);
         }
     }
 
     protected void addConstituentIdsToRows(final List<Map<String, Object>> rowList, final List rows) {
         for (Map<String, Object> objectMap : rowList) {
             Long id = (Long) objectMap.get(StringConstants.ID);
             for (Object thisRow : rows) {
                 BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(thisRow);
                 if (bw.isReadableProperty(StringConstants.ID) && ((Long) bw.getPropertyValue(StringConstants.ID)).equals(id)) {
                     Long constituentId = null;
                     if (bw.isReadableProperty(StringConstants.CONSTITUENT_ID) && bw.getPropertyValue(StringConstants.CONSTITUENT_ID) != null) {
                         constituentId = (Long) bw.getPropertyValue(StringConstants.CONSTITUENT_ID);
                     }
                     else if (bw.isReadableProperty(StringConstants.CONSTITUENT_DOT_ID) && bw.getPropertyValue(StringConstants.CONSTITUENT_DOT_ID) != null) {
                         constituentId = (Long) bw.getPropertyValue(StringConstants.CONSTITUENT_DOT_ID); 
                     }
                     if (constituentId != null) {
                         objectMap.put(StringConstants.CONSTITUENT_ID, constituentId);
                         break;
                     }
                 }
             }
         }
     }
 
     /**
      * Create a mock entity object for introspection
      * @param batch the batch to create a mock default entity object for
      * @return a BeanWrapper of the mock entity object
      */
     protected BeanWrapper createDefaultEntity(PostBatch batch) {
 	    BeanWrapper bw;
 	    if (batch.isForTouchPoints()) {
 		    bw = PropertyAccessorFactory.forBeanPropertyAccess(new CommunicationHistory(new Constituent(0L, new Site())));
 	    }
 	    else if (StringConstants.CONSTITUENT.equals(batch.getBatchType())) {
 		    bw = PropertyAccessorFactory.forBeanPropertyAccess(new Constituent(0L, new Site()));
 	    }
 	    else {
 		    AbstractPaymentInfoEntity entity = null;
 		    if (StringConstants.GIFT.equals(batch.getBatchType())) {
 				entity = new Gift();
 			}
 			else if (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType())) {
 				entity = new AdjustedGift();
 			}
 			Constituent constituent = new Constituent(0L, new Site());
 			entity.setConstituent(constituent);
 			entity.setPaymentSource(new PaymentSource(constituent));
 		    bw = PropertyAccessorFactory.forBeanPropertyAccess(entity);
 	    }
         return bw;
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap step4FindBatchUpdateFields(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("step4FindBatchUpdateFields:");
         }
 	    final ModelMap model = findUpdateFields(flowRequestContext);
         model.put(ACCESSIBLE_STEPS, determineAccessibleSteps(flowRequestContext));
         return model;
     }
 
 	protected ModelMap findUpdateFields(final RequestContext flowRequestContext) {
 		tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
 		determineStepToSave(flowRequestContext);
 
 		final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
 		final ModelMap model = new ModelMap();
 
 		if (batch.isForTouchPoints()) {
 			findTouchPointUpdateFields(batch, model);
 		}
 		else {
 		    findBatchUpdateFields(batch, model);
 		}
 		model.put(StringConstants.SUCCESS, Boolean.TRUE);
 		return model;
 	}
 
     @SuppressWarnings("unchecked")
     protected void findBatchUpdateFields(final PostBatch batch, final ModelMap model) {
         final String picklistNameId = new StringBuilder(batch.getBatchType()).append(BATCH_FIELDS).toString();
         final Picklist picklist = picklistItemService.getPicklist(picklistNameId);
         final List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
         if (picklist != null) {
             for (PicklistItem item : picklist.getActivePicklistItems()) {
                 // the defaultDisplayValue will be the fieldDefinitionId like 'adjustedGift.status' which we need to resolve to the fieldName like 'adjustedGift.adjustedStatus'
                 String fieldDefinitionId = new StringBuilder(batch.getBatchType()).append(".").append(item.getDefaultDisplayValue()).toString();
 	            FieldDefinition fieldDef = fieldService.resolveFieldDefinition(fieldDefinitionId);
                 if (fieldDef != null) {
 	                FieldType fieldType = fieldDef.getFieldType();
 	                if (FieldType.PICKLIST.equals(fieldType)) {  // TODO: MULTI_PICKLIST, CODE, etc?
 		                findPicklistData(fieldDef.getFieldName(), model, item.getDefaultDisplayValue(), false);
 	                }
 	                final Map<String, Object> map = new HashMap<String, Object>();
 	                map.put(StringConstants.NAME, item.getDefaultDisplayValue());
 	                map.put("desc", fieldDef.getDefaultLabel());
 	                map.put(StringConstants.TYPE, fieldType.name().toLowerCase());
 
 	                String updateFieldValue = batch.getUpdateFieldValue(item.getDefaultDisplayValue());
 	                map.put(StringConstants.VALUE, updateFieldValue != null ? updateFieldValue : StringConstants.EMPTY);
                     map.put(StringConstants.SELECTED, batch.getUpdateFieldValue(item.getDefaultDisplayValue()) != null);
                     returnList.add(map);
                 }
             }
         }
         model.put(StringConstants.ROWS, returnList);
         model.put(StringConstants.TOTAL_ROWS, returnList.size());
     }
 
 	@SuppressWarnings("unchecked")
 	protected void findTouchPointUpdateFields(final PostBatch batch, final ModelMap model) {
 		final List<SectionField> allFields = tangerineListHelper.findSectionFields(StringConstants.COMMUNICATION_HISTORY);
 
 		final Map<String, String> dataMap = new HashMap<String, String>();
 		model.put(StringConstants.DATA, dataMap);
 		model.put(StringConstants.SUCCESS, Boolean.TRUE);
 
 		for (SectionField thisField : allFields) {
 			final FieldDefinition fieldDef = thisField.getFieldDefinition();
 			final FieldType fieldType = fieldDef.getFieldType();
 			if ( ! "address.id".equals(thisField.getFieldPropertyName()) &&
 					! "phone.id".equals(thisField.getFieldPropertyName()) &&
 			        ! "email.id".equals(thisField.getFieldPropertyName())) {
 
 				String value = batch.getUpdateFieldValue(fieldDef.getFieldName());
 
 				if (FieldType.PICKLIST.equals(fieldType)) {  // TODO: MULTI_PICKLIST, CODE, etc?
 					findPicklistData(fieldDef.getFieldName(), model, fieldDef.getFieldName(), true);
 				}
 				else if (FieldType.QUERY_LOOKUP.equals(fieldType)) {
 					FieldHandler handler = fieldHandlerHelper.lookupFieldHandler(fieldType);
 					model.put(new StringBuilder(fieldDef.getFieldName()).append("-QueryLookup").toString(),
 							handler.resolveExtData(thisField, batch.getUpdateFieldValue(fieldDef.getFieldName())));
 				}
 				else if (FieldType.DATE.equals(fieldType) && StringUtils.hasText(value)) {
 					// need to reformat date from yyyy/MM/dd HH:mm:ss to MM/dd/yyyy format
 					try {
 						value = new SimpleDateFormat(StringConstants.MM_DD_YYYY_FORMAT).format(DateUtils.parseDate(value, new String[] { StringConstants.YYYY_MM_DD_HH_MM_SS_FORMAT_1 }));
 					}
 					catch (ParseException pe) {
 						logger.warn("findTouchPointUpdateFields: could not parse date = " + value);
 					}
 				}
 				dataMap.put(fieldDef.getFieldName(), value);
 			}
 			else if (dataMap.get(StringConstants.CORRESPONDENCE_FOR_CUSTOM_FIELD) == null) {
 				// CorrespondenceFor is a Picklist TODO: what if its not?
 				findPicklistData(StringConstants.CORRESPONDENCE_FOR_CUSTOM_FIELD, model, StringConstants.CORRESPONDENCE_FOR_CUSTOM_FIELD, true);
 				dataMap.put(StringConstants.CORRESPONDENCE_FOR_CUSTOM_FIELD, batch.getUpdateFieldValue(StringConstants.CORRESPONDENCE_FOR_CUSTOM_FIELD));
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void findPicklistData(String fieldName, final ModelMap model, final String key, boolean addReferenceValue) {
 		final Picklist referencedPicklist = picklistItemService.getPicklist(fieldName);
 		if (referencedPicklist != null) {
 			final List<Map<String, String>> referencedItemList = new ArrayList<Map<String, String>>();
 			for (PicklistItem referencedItem : referencedPicklist.getActivePicklistItems()) {
 				final Map<String, String> referencedItemMap = new HashMap<String, String>();
 				referencedItemMap.put("itemName", referencedItem.getItemName());
 				referencedItemMap.put("displayVal", referencedItem.getDefaultDisplayValue());
 				if (addReferenceValue) {
 					referencedItemMap.put("refVal", referencedItem.getReferenceValue());
 				}
 				referencedItemList.add(referencedItemMap);
 			}
 			model.put(new StringBuilder(key).append("-Picklist").toString(), referencedItemList);
 		}
 	}
 
     @SuppressWarnings("unchecked")
     public ModelMap step5ReviewUpdates(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("step5ReviewUpdates:");
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
         determineStepToSave(flowRequestContext);
 
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         final SortInfo sortInfo = getSortInfo(flowRequestContext);
 
 	    boolean wasDisplayedId = false;
 	    if (sortInfo.getSort().equals(StringConstants.DISPLAYED_ID)) {
 			wasDisplayedId = true;
 	    }
 
         final HttpServletRequest request = getRequest(flowRequestContext);
         final ModelMap model = new ModelMap();
 
         final Set<Long> segmentationReportIds = batch.getEntrySegmentationIds();
         final List<Map<String, Object>> rowValues = new ArrayList<Map<String, Object>>();
 
         initReviewUpdateFields(batch, model, sortInfo);
 
         List rows = null;
         int totalRows = 0;
 
         unescapeSortField(sortInfo);
 
         if (StringConstants.GIFT.equals(batch.getBatchType())) {
             rows = giftService.readGiftsBySegmentationReportIds(segmentationReportIds, sortInfo, request.getLocale());
             totalRows = giftService.readCountGiftsBySegmentationReportIds(segmentationReportIds);
         }
         else if (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType())) {
             rows = adjustedGiftService.readAdjustedGiftsBySegmentationReportIds(segmentationReportIds, sortInfo, request.getLocale());
             totalRows = adjustedGiftService.readCountAdjustedGiftsBySegmentationReportIds(segmentationReportIds);
         }
         contrastUpdatedValues(batch, rows, rowValues, batch.getUpdateFields());
 
         model.put(StringConstants.ROWS, rowValues);
         model.put(StringConstants.TOTAL_ROWS, totalRows);
 
         model.put(ACCESSIBLE_STEPS, determineAccessibleSteps(flowRequestContext));
 
 	    if (wasDisplayedId) {
 		    // Pretty ugly code to put back displayedId as the sort field
 		    ( (Map) ( (Map)model.get(StringConstants.META_DATA) ).
 				    get(StringConstants.SORT_INFO)).put(StringConstants.FIELD, StringConstants.DISPLAYED_ID);
 	    }
         return model;
     }
 
     @SuppressWarnings("unchecked")
     protected void initReviewUpdateFields(final PostBatch batch, final ModelMap model, final SortInfo sortInfo) {
         final Map<String, Object> metaDataMap = tangerineListHelper.initMetaData(sortInfo.getStart(),
                 sortInfo.getLimit()); // double the rows because of old & new values will be displayed
 
         final BeanWrapper bean = createDefaultEntity(batch);
 
         initSortInfoMetaData(bean, sortInfo, metaDataMap);
 
         final List<Map<String, Object>> fieldList = new ArrayList<Map<String, Object>>();
         Map<String, Object> fieldMap = new HashMap<String, Object>();
         fieldMap.put(StringConstants.NAME, StringConstants.TYPE);
         fieldMap.put(StringConstants.MAPPING, StringConstants.TYPE);
         fieldMap.put(StringConstants.TYPE, ExtTypeHandler.EXT_STRING);
         fieldMap.put(StringConstants.HEADER, TangerineMessageAccessor.getMessage(StringConstants.TYPE));
         fieldList.add(fieldMap);
 
         fieldMap = new HashMap<String, Object>();
         fieldMap.put(StringConstants.NAME, StringConstants.DISPLAYED_ID);
         fieldMap.put(StringConstants.MAPPING, StringConstants.DISPLAYED_ID);
         fieldMap.put(StringConstants.TYPE, ExtTypeHandler.EXT_INT);
         fieldMap.put(StringConstants.HEADER, TangerineMessageAccessor.getMessage(StringConstants.ID));
         fieldList.add(fieldMap);
 
         initBatchUpdateFields(batch, fieldList);
         metaDataMap.put(StringConstants.FIELDS, fieldList);
         model.put(StringConstants.META_DATA, metaDataMap);
     }
 
     protected void unescapeSortField(final SortInfo sortInfo) {
         sortInfo.setSort(TangerineForm.unescapeFieldName(sortInfo.getSort())); // convert from customFieldMap-tsb-bank-teb -> customFieldMap[bank]
     }
 
     protected void initSortInfoMetaData(final BeanWrapper bean, final SortInfo sortInfo, final Map<String, Object> metaDataMap) {
         final Map<String, String> sortInfoMap = new HashMap<String, String>();
         if ( ! bean.isReadableProperty(TangerineForm.unescapeFieldName(sortInfo.getSort()))) {  // If the sort key is not one of the bean's properties, use the ID as default
             sortInfo.setSort(StringConstants.ID);
         }
         sortInfoMap.put(StringConstants.FIELD, sortInfo.getSort());
         sortInfoMap.put(StringConstants.DIRECTION, sortInfo.getDir());
         metaDataMap.put(StringConstants.SORT_INFO, sortInfoMap);
     }
 
     protected void initBatchUpdateFields(final PostBatch batch, List<Map<String, Object>> fieldList) {
         BeanWrapper bean = createDefaultEntity(batch);
 
         // the enteredParam.key/defaultDisplayValue will be the fieldDefinitionId like 'adjustedGift.status' which we need to resolve to the fieldName like 'adjustedGift.adjustedStatus'
         for (String thisKey : batch.getUpdateFields().keySet()) {
 	        String fieldDefinitionId = resolveFieldDefinitionId(batch, thisKey);
             FieldDefinition fieldDef = fieldService.resolveFieldDefinition(fieldDefinitionId);
             if (fieldDef != null) {
                 Map<String, Object> fieldMap = new HashMap<String, Object>();
                 String escapedFieldName = TangerineForm.escapeFieldName(fieldDef.getFieldName());
                 fieldMap.put(StringConstants.NAME, escapedFieldName);
                 fieldMap.put(StringConstants.MAPPING, escapedFieldName);
 
                 String propertyName = fieldDef.getFieldName();
                 if (bean.getPropertyValue(propertyName) instanceof CustomField) {
                     propertyName += StringConstants.DOT_VALUE;
                 }
                 String extType;
                 if (fieldDef.getFieldType().equals(FieldType.PICKLIST)) {
                     extType = ExtTypeHandler.EXT_STRING;
                 }
                 else {
                     extType = ExtTypeHandler.findExtDataType(bean.getPropertyType(propertyName));
                 }
                 fieldMap.put(StringConstants.TYPE, extType);
                 fieldMap.put(StringConstants.HEADER, fieldDef.getDefaultLabel());
 
                 if (ExtTypeHandler.EXT_DATE.equals(extType)) {
                     String format;
 //                        if (FieldType.CC_EXPIRATION.equals(fieldDef.getFieldType()) || FieldType.CC_EXPIRATION_DISPLAY.equals(fieldDef.getFieldType())) {
 //                            format = "Y-m-d"; // TODO: put back CC?
 //                        }
 //                        else {
                         format = "Y-m-d H:i:s";
 //                        }
                     fieldMap.put(StringConstants.DATE_FORMAT, format);
                 }
                 fieldList.add(fieldMap);
             }
         }
     }
 
     protected void contrastUpdatedValues(final PostBatch batch, final List rows, final List<Map<String, Object>> rowValues,
                                        final Map<String, String> updateFields) {
         if (rows != null) {
             for (Object thisRow : rows) {
                 BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(thisRow);
                 final Map<String, Object> oldRowMap = new HashMap<String, Object>();
                 final Map<String, Object> newRowMap = new HashMap<String, Object>();
                 oldRowMap.put(StringConstants.TYPE, TangerineMessageAccessor.getMessage("before"));
                 newRowMap.put(StringConstants.TYPE, TangerineMessageAccessor.getMessage("after"));
                 oldRowMap.put(StringConstants.ID, "old-" + bw.getPropertyValue(StringConstants.ID));
                 newRowMap.put(StringConstants.ID, "new-" + bw.getPropertyValue(StringConstants.ID));
 
                 oldRowMap.put(StringConstants.DISPLAYED_ID, bw.getPropertyValue(StringConstants.ID));
                 newRowMap.put(StringConstants.DISPLAYED_ID, bw.getPropertyValue(StringConstants.ID));
 
                 for (Map.Entry<String, String> fieldEntry : updateFields.entrySet()) {
                     String key = fieldEntry.getKey();
                     // the batchType + key is the fieldDefinitionId; we need to resolve the fieldName
                     String fieldDefinitionId = new StringBuilder(batch.getBatchType()).append(".").append(key).toString();
 	                FieldDefinition fieldDef = fieldService.resolveFieldDefinition(fieldDefinitionId);
                     String fieldName = fieldDef.getFieldName();
                     String propertyName = fieldName;
                     if (bw.getPropertyValue(propertyName) instanceof CustomField) {
                         propertyName += StringConstants.DOT_VALUE;
                     }
                     String escapedFieldName = TangerineForm.escapeFieldName(fieldName);
 
                     if (fieldDef.getFieldType().equals(FieldType.PICKLIST)) {   // TODO: MULTI_PICKLIST, CODE, etc?
                         Object oldValObj = bw.getPropertyValue(propertyName);
                         String oldVal = oldValObj == null ? null : oldValObj.toString();
                         String newVal = fieldEntry.getValue();
                         final Picklist referencedPicklist = picklistItemService.getPicklist(fieldDef.getFieldName());
                         if (referencedPicklist != null) {
                             for (PicklistItem referencedItem : referencedPicklist.getActivePicklistItems()) {
                                 if (oldVal != null && referencedItem.getItemName().equals(oldVal)) {
                                     oldVal = referencedItem.getDefaultDisplayValue();
                                 }
                                 if (referencedItem.getItemName().equals(newVal)) {
                                     newVal = referencedItem.getDefaultDisplayValue();
                                 }
                             }
                         }
                         oldRowMap.put(escapedFieldName, oldVal);
                         newRowMap.put(escapedFieldName, newVal);
 
                     }
                     else {
                         oldRowMap.put(escapedFieldName, bw.getPropertyValue(propertyName));
                         newRowMap.put(escapedFieldName, fieldEntry.getValue());
                     }
                 }
                 rowValues.add(oldRowMap); // 1 row for the old value
                 rowValues.add(newRowMap); // 1 row for the new value
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap saveBatch(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("saveBatch:");
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
 	    determineStepToSave(flowRequestContext);
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         final PostBatch savedBatch = postBatchService.maintainBatch(batch);
         setFlowScopeAttribute(flowRequestContext, savedBatch, StringConstants.BATCH);
 
         final ModelMap model = new ModelMap();
         model.put(StringConstants.BATCH_ID, savedBatch.getId());
         model.put(StringConstants.SUCCESS, Boolean.TRUE);
         return model;
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap cancelBatch(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("cancelBatch:");
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
         final ModelMap model = new ModelMap();
         model.put(StringConstants.SUCCESS, Boolean.TRUE);
         return model;
     }
 
     @SuppressWarnings("unchecked")
     protected List<String> determineAccessibleSteps(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("determineAccessibleSteps:");
         }
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         final Set<String> accessibleSteps = new TreeSet<String>();
         accessibleSteps.add(STEP_1_GRP); // step 1 always accessible
 
         if (StringUtils.hasText(batch.getBatchType())) {
             accessibleSteps.add(STEP_2_GRP);
         }
 
         if (accessibleSteps.contains(STEP_2_GRP) && ! batch.getEntrySegmentationIds().isEmpty()) {
             accessibleSteps.add(STEP_3_GRP);
         }
 
         int totalRows = 0;
         if (StringConstants.GIFT.equals(batch.getBatchType()) && ! batch.getEntrySegmentationIds().isEmpty()) {
             totalRows = giftService.readCountGiftsBySegmentationReportIds(batch.getEntrySegmentationIds());
         }
         else if (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType()) && ! batch.getEntrySegmentationIds().isEmpty()) {
             totalRows = adjustedGiftService.readCountAdjustedGiftsBySegmentationReportIds(batch.getEntrySegmentationIds());
         }
         if (accessibleSteps.contains(STEP_3_GRP) && totalRows > 0) {
             accessibleSteps.add(STEP_4_GRP);
         }
 
         if (accessibleSteps.contains(STEP_4_GRP) && ! batch.getUpdateFields().isEmpty() && ! batch.isForTouchPoints()) {
             accessibleSteps.add(STEP_5_GRP);
         }
         return new ArrayList<String>(accessibleSteps);
     }
 
     private Map<String, Object> findEnteredParameters(final HttpServletRequest request) {
         final Enumeration paramNames = request.getParameterNames();
         final Map<String, Object> paramMap = new HashMap<String, Object>();
         while (paramNames.hasMoreElements()) {
             String thisParamName = (String) paramNames.nextElement();
             if (thisParamName.startsWith(PARAM_PREFIX)) {
                 String name = thisParamName.replaceFirst(PARAM_PREFIX, StringConstants.EMPTY);
                 if (StringUtils.hasText(name)) {
                     paramMap.put(name, request.getParameter(thisParamName));
                 }
             }
         }
         return paramMap;
     }
 
 	protected String resolveFieldDefinitionId(PostBatch batch, String key) {
 		return new StringBuilder( batch.isForTouchPoints() ? StringConstants.COMMUNICATION_HISTORY : batch.getBatchType() ).
 				append(".").append(key).toString();
 	}
 }
