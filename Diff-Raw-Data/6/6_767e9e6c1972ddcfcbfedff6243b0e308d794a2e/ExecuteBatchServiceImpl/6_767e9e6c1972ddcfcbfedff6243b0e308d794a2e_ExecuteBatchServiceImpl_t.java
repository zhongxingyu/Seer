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
 
 import com.orangeleap.tangerine.domain.AbstractCustomizableEntity;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.PostBatch;
 import com.orangeleap.tangerine.domain.PostBatchEntry;
 import com.orangeleap.tangerine.domain.paymentInfo.AdjustedGift;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.service.AdjustedGiftService;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.service.ExecuteBatchService;
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.service.PostBatchEntryService;
 import com.orangeleap.tangerine.service.PostBatchService;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import javax.annotation.Resource;
 import org.apache.commons.logging.Log;
 import org.springframework.context.support.AbstractMessageSource;
 import org.springframework.stereotype.Service;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.BindException;
 import org.springframework.validation.ObjectError;
 
 // Does not extend base class to avoid getting a transactional attribute
 @Service("executeBatchService")
 public class ExecuteBatchServiceImpl implements ExecuteBatchService {
 
 	protected final Log logger = OLLogger.getLog(getClass());
 	
 	@Resource(name = "postBatchService")
 	private PostBatchService postBatchService;
 
 	@Resource(name = "postBatchEntryService")
 	private PostBatchEntryService postBatchEntryService;
 
 	@Resource(name = "giftService")
 	private GiftService giftService;
 
 	@Resource(name = "adjustedGiftService")
 	private AdjustedGiftService adjustedGiftService;
 
 	@Resource(name = "constituentService")
 	protected ConstituentService constituentService;
 
 	@Resource(name = "tangerineUserHelper")
 	protected TangerineUserHelper tangerineUserHelper;
 
 	@Resource(name = "messageSource")
 	protected AbstractMessageSource messageSource;
 
 	// Non-transactional method
 	@Override
 	@SuppressWarnings("unchecked")
 	public PostBatch executeBatch(PostBatch batch) {
 	    if (logger.isInfoEnabled()) {
 	        logger.info("executeBatch: BEGINNING execution of batchId = " + batch.getId());
 	    }
 		batch.setCurrentlyExecuting(true);
 		batch = postBatchService.maintainBatch(batch);
 
 	    List<? extends AbstractCustomizableEntity> entries = null;
 	    final Set<Long> segmentationIds = batch.getEntrySegmentationIds();
 
 		if (StringConstants.GIFT.equals(batch.getBatchType())) {
 			// Get by giftIds if segmentationIds do not exist
 			if ( ! segmentationIds.isEmpty()) {
 				entries = giftService.readAllGiftsBySegmentationReportIds(segmentationIds);
 			}
 			else {
 				entries = giftService.readGiftsByIds(batch.getEntryGiftIds());
 			}
 		}
 		else if (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType())) {
 			// Get by adjustedGiftIds if segmentationIds do not exist
 			if ( ! segmentationIds.isEmpty()) {
 				entries = adjustedGiftService.readAllAdjustedGiftsBySegmentationReportIds(segmentationIds);
 			}
 			else {
 				entries = adjustedGiftService.readAdjustedGiftsByIds(batch.getEntryAdjustedGiftIds());
 			}
 		}
 		else if (StringConstants.CONSTITUENT.equals(batch.getBatchType())) {
 			// Get by constituentIds if segmentationIds do not exist
 			if ( ! segmentationIds.isEmpty()) {
 				entries = constituentService.readAllConstituentsBySegmentationReportIds(segmentationIds);
 			}
 			else {
 				entries = constituentService.readConstituentsByIds(batch.getEntryConstituentIds());
 			}
 		}
 
 	    final List<PostBatchEntry> errorEntries = new ArrayList<PostBatchEntry>();
 	    final List<PostBatchEntry> executedEntries = new ArrayList<PostBatchEntry>();
 	    if (entries != null) {
 	        for (AbstractCustomizableEntity entity : entries) {
 	            boolean executed;
 	            try {
 	                executed = postBatchEntryService.executeBatchEntry(batch, entity);
 	            }
 	            catch (Exception e) {
 	                executed = false;
		            if ( ! (e instanceof PostBatchEntryServiceImpl.PostBatchUpdateException)) {
			            logger.warn("executeBatch: exception occurred during saving of entity", e);
			            entity.addCustomFieldValue(StringConstants.BATCH_ERROR, resolveErrorMsg(e));
		            }
 	            }
 	            if (executed) {
 	                PostBatchEntry entry = new PostBatchEntry();
 					if (entity instanceof Gift) {
 						entry.setGiftId(entity.getId());
 					}
 					else if (entity instanceof AdjustedGift) {
 						entry.setAdjustedGiftId(entity.getId());
 					}
 					else if (entity instanceof Constituent) {
 						entry.setConstituentId(entity.getId());
 					}
 	                executedEntries.add(entry);
 	            }
 	            else {
 	                if (postBatchEntryService.hasBatchErrorsInEntity(entity)) {
 	                    errorEntries.add(copyEntityBatchErrorsToEntryErrors(entity));
 	                }
 	            }
 	        }
 	    }
 	    if ( ! errorEntries.isEmpty()) {
 	        PostBatch errorBatch = postBatchService.createErrorBatch(batch, errorEntries);
 	        if (errorBatch != null && errorBatch.getId() != null) {
 	            batch.setErrorBatchId(errorBatch.getId());
 	        }
 	    }
 	    batch.setPostBatchEntries(executedEntries); // this should contain just the executed gifts/adjustedGifts/etc
 
 	    Long thisUserId = tangerineUserHelper.lookupUserId();
 	    if (StringUtils.hasText(batch.getUpdateFieldValue(StringConstants.POSTED_DATE)) &&
 	            postBatchEntryService.isValidPostedDate(batch.getUpdateFieldValue(StringConstants.POSTED_DATE))) {
 	        batch.setPostedFields(thisUserId);
 	    }
 	    batch.setAnErrorBatch(false);  // if this was an error batch, just set to an executed batch instead
 	    batch.setExecutionFields(thisUserId);
 
 	    batch.setCurrentlyExecuting(false); // reset to not executing anymore
 	    batch = postBatchService.maintainBatch(batch);
 		if (logger.isInfoEnabled()) {
 		    logger.info("executeBatch: FINISHED execution of batchId = " + batch.getId());
 		}
 		return batch;
 	}
 
 	private PostBatchEntry copyEntityBatchErrorsToEntryErrors(AbstractCustomizableEntity entity) {
 	    final PostBatchEntry entry = new PostBatchEntry();
 
 	    if (entity instanceof Gift) {
 	        entry.setGiftId(entity.getId());
 	    }
 	    else if (entity instanceof AdjustedGift) {
 	        entry.setAdjustedGiftId(entity.getId());
 	    }
 		else if (entity instanceof Constituent) {
 		    entry.setConstituentId(entity.getId());
 	    }
 
 	    final String errors = postBatchEntryService.getBatchErrorsInEntity(entity);
 	    final String[] errorsSet = StringUtils.delimitedListToStringArray(errors, StringConstants.CUSTOM_FIELD_SEPARATOR);
 	    if (errorsSet != null) {
 	        for (String s : errorsSet) {
 	            entry.addError(s);
 	        }
 	    }
 	    return entry;
 	}
 
 	private String resolveErrorMsg(Exception e) {
 		String errorMsg = null;
 		if (e instanceof BindException) {
 			List errors = ((BindException)e).getAllErrors();
 			for (Object thisError : errors) {
 				if (thisError instanceof ObjectError) {
 					ObjectError objError = (ObjectError) thisError;
 					String message = messageSource.getMessage(objError.getCode(), objError.getArguments(), Locale.getDefault());
 					if (StringUtils.hasText(message)) {
 						if (errorMsg != null) {
 							errorMsg = new StringBuilder(errorMsg).append(" ").append(message).toString();
 						}
 						else {
 							errorMsg = message;
 						}
 					}
 				}
 			}
 		}
 		if (errorMsg == null) {
 			errorMsg = e.getMessage();
 		}
 		return errorMsg;
 	}
 }
