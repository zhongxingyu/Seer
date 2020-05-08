 package com.orangeleap.tangerine.dao.ibatis;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.ibatis.sqlmap.client.SqlMapClient;
 import com.orangeleap.tangerine.dao.AdjustedGiftDao;
 import com.orangeleap.tangerine.domain.paymentInfo.AdjustedGift;
 import com.orangeleap.tangerine.util.StringConstants;
 
 @Repository("adjustedGiftDAO")
 public class IBatisAdjustedGiftDao extends AbstractPaymentInfoEntityDao<AdjustedGift> implements AdjustedGiftDao {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Autowired
     protected IBatisAdjustedGiftDao(SqlMapClient sqlMapClient) {
         super(sqlMapClient);
     }
 
     @Override
     public AdjustedGift maintainAdjustedGift(final AdjustedGift adjustedGift) {
         if (logger.isTraceEnabled()) {
             logger.trace("maintainAdjustedGift: adjustedGiftId = " + adjustedGift.getId());
         }
         AdjustedGift aAdjustedGift = (AdjustedGift)insertOrUpdate(adjustedGift, "ADJUSTED_GIFT");
         
         /* Delete DistributionLines first */
         getSqlMapClientTemplate().delete("DELETE_DISTRO_LINE_BY_ADJUSTED_GIFT_ID", aAdjustedGift.getId());
         /* Then insert DistributionLines */
         insertDistributionLines(aAdjustedGift, "adjustedGiftId");
         
         return aAdjustedGift;
     }
 
     @Override
     public AdjustedGift readAdjustedGiftById(final Long adjustedGiftId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readAdjustedGiftById: adjustedGiftId = " + adjustedGiftId);
         }
         final Map<String, Object> params = setupParams();
         params.put(StringConstants.ADJUSTED_GIFT_ID, adjustedGiftId);
         AdjustedGift adjustedGift = (AdjustedGift) getSqlMapClientTemplate().queryForObject("SELECT_ADJUSTED_GIFT_BY_ID", params);
         
//        loadDistributionLinesCustomFields(adjustedGift);
//        loadCustomFields(adjustedGift.getPerson());
         return adjustedGift;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<AdjustedGift> readAdjustedGiftsForOriginalGiftId(final Long originalGiftId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readAdjustedGiftsForOriginalGiftId: originalGiftId = " + originalGiftId);
         }
         final Map<String, Object> params = setupParams();
         params.put(StringConstants.ORIGINAL_GIFT_ID, originalGiftId);
         return getSqlMapClientTemplate().queryForList("SELECT_ADJUSTED_GIFTS_BY_ORIGINAL_GIFT_ID", params);
     }
 
 }
