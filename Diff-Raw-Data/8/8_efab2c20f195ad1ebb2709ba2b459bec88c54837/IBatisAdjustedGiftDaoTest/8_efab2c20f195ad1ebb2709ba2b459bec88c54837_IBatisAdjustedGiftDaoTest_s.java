 package com.orangeleap.tangerine.test.dao.ibatis;
 
 import com.orangeleap.tangerine.dao.AdjustedGiftDao;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.communication.Address;
 import com.orangeleap.tangerine.domain.paymentInfo.AdjustedGift;
 import com.orangeleap.tangerine.domain.paymentInfo.DistributionLine;
 import com.orangeleap.tangerine.util.OLLogger;
 import org.apache.commons.logging.Log;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Locale;
 
 public class IBatisAdjustedGiftDaoTest extends AbstractIBatisTest {
     
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
     
     private AdjustedGiftDao adjustedGiftDao;
 
     @BeforeMethod
     public void setup() {
         adjustedGiftDao = (AdjustedGiftDao)super.applicationContext.getBean("adjustedGiftDAO");
     }
 
     private void setupDistributionLines(AdjustedGift adjustedGift) {
         List<DistributionLine> lines = new ArrayList<DistributionLine>();
         
         DistributionLine line = new DistributionLine();
         line.setAmount(new BigDecimal("-9.99"));
         line.setProjectCode("foo");
         line.setAdjustedGiftId(adjustedGift.getId());
         lines.add(line);
         
         adjustedGift.setDistributionLines(lines);
     }
 
     @Test(groups = { "testMaintainAdjustedGift" }, dependsOnGroups = { "testReadAdjustedGift" })
     public void testMaintainAdjustedGift() throws Exception {
         SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
         AdjustedGift adjustedGift = new AdjustedGift();
         adjustedGift.setAdjustedAmount(new BigDecimal("9.99"));
         adjustedGift.setAdjustedPaymentRequired(false);
         adjustedGift.setAdjustedReason("No Reason");
         adjustedGift.setAdjustedStatus("Pending");
         adjustedGift.setAdjustedTransactionDate(sdf.parse("01/01/2010"));
         adjustedGift.setAdjustedType("foo");
         adjustedGift.setOriginalGiftId(600L);
         adjustedGift.setConstituent(new Constituent(100L, new Site("company1")));
         Address addr = new Address();
         addr.setId(100L);
         adjustedGift.setAddress(addr);
         setupDistributionLines(adjustedGift);
         
         adjustedGift = adjustedGiftDao.maintainAdjustedGift(adjustedGift);
         AdjustedGift readAdjustedGift = adjustedGiftDao.readAdjustedGiftById(adjustedGift.getId());
         assert readAdjustedGift.getId().equals(adjustedGift.getId());
         assert new BigDecimal("9.99").floatValue() == readAdjustedGift.getAdjustedAmount().floatValue();
         assert readAdjustedGift.isAdjustedPaymentRequired() == false;
         assert "No Reason".equals(readAdjustedGift.getAdjustedReason());
         assert "Pending".equals(readAdjustedGift.getAdjustedStatus());
         assert "01/01/2010".equals(sdf.format(readAdjustedGift.getAdjustedTransactionDate()));
         assert "foo".equals(readAdjustedGift.getAdjustedType());
         assert 100L == readAdjustedGift.getConstituent().getId();
         assert 100L == readAdjustedGift.getAddress().getId();
         assert readAdjustedGift.getDistributionLines().size() == 1;
         assert new BigDecimal("-9.99").floatValue() == readAdjustedGift.getDistributionLines().get(0).getAmount().floatValue();
         assert 600L == readAdjustedGift.getOriginalGiftId();
         
         assert readAdjustedGift.getPhone() == null;
         assert readAdjustedGift.getAuthCode() == null;
         assert readAdjustedGift.getCheckNumber() == null;
         assert readAdjustedGift.getComments() == null;
         assert readAdjustedGift.getPaymentMessage() == null;
         assert readAdjustedGift.getPaymentStatus() == null;
         assert readAdjustedGift.getTxRefNum() == null;
         assert readAdjustedGift.getPaymentType() == null;
         assert readAdjustedGift.getPaymentSource() == null;
         
         adjustedGift = readAdjustedGift;
         adjustedGift.setAdjustedPaymentRequired(true);
         adjustedGift.setTxRefNum("012345");
         adjustedGift.setPaymentMessage("I have paid");
         adjustedGift.setAuthCode("xyz");
         
         adjustedGift = adjustedGiftDao.maintainAdjustedGift(adjustedGift);
         readAdjustedGift = adjustedGiftDao.readAdjustedGiftById(adjustedGift.getId());
         assert readAdjustedGift.getId().equals(adjustedGift.getId());
         assert new BigDecimal("9.99").floatValue() == readAdjustedGift.getAdjustedAmount().floatValue();
         assert readAdjustedGift.isAdjustedPaymentRequired();
         assert "No Reason".equals(readAdjustedGift.getAdjustedReason());
         assert "Pending".equals(readAdjustedGift.getAdjustedStatus());
         assert "01/01/2010".equals(sdf.format(readAdjustedGift.getAdjustedTransactionDate()));
         assert "foo".equals(readAdjustedGift.getAdjustedType());
         assert 100L == readAdjustedGift.getConstituent().getId();
         assert 100L == readAdjustedGift.getAddress().getId();
         assert readAdjustedGift.getDistributionLines().size() == 1;
         assert new BigDecimal("-9.99").floatValue() == readAdjustedGift.getDistributionLines().get(0).getAmount().floatValue();
         assert 600L == readAdjustedGift.getOriginalGiftId();
         assert "xyz".equals(readAdjustedGift.getAuthCode());
         assert "012345".equals(readAdjustedGift.getTxRefNum());
         assert "I have paid".equals(readAdjustedGift.getPaymentMessage());
         
         assert readAdjustedGift.getPhone() == null;
         assert readAdjustedGift.getCheckNumber() == null;
         assert readAdjustedGift.getComments() == null;
         assert readAdjustedGift.getPaymentStatus() == null;
         assert readAdjustedGift.getPaymentType() == null;
         assert readAdjustedGift.getPaymentSource() == null;
         
     }
     
     @Test(groups = { "testReadAdjustedGift" })
     public void testReadAdjustedGiftById() throws Exception {
         AdjustedGift adjustedGift = adjustedGiftDao.readAdjustedGiftById(0L);
         assert adjustedGift == null;
         
         adjustedGift = adjustedGiftDao.readAdjustedGiftById(1L);
         assert adjustedGift.getId().equals(1L);
         assert adjustedGift.getAdjustedAmount().floatValue() == new BigDecimal("-160").floatValue();
         assert adjustedGift.getAdjustedReason().equals("Just Because");
         assert adjustedGift.getAdjustedStatus().equals("In Progress");
         SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
         assert "08/08/2009".equals(sdf.format(adjustedGift.getAdjustedTransactionDate()));
         assert "Partial Refund".equals(adjustedGift.getAdjustedType());
         assert adjustedGift.isAdjustedPaymentRequired();
         assert "foobar".equals(adjustedGift.getAdjustedPaymentTo());
         assert 300L == adjustedGift.getOriginalGiftId();
         assert 300f == adjustedGift.getOriginalAmount().floatValue();
         assert 200L == adjustedGift.getConstituent().getId();
         
         assert adjustedGift.getDistributionLines().size() == 2;
         for (DistributionLine line : adjustedGift.getDistributionLines()) {
             assert line.getId() == 1000L || line.getId() == 1100L;
             assert new BigDecimal("-80").floatValue() == line.getAmount().floatValue();
             assert 1L == line.getAdjustedGiftId();
         }
         
         adjustedGift = adjustedGiftDao.readAdjustedGiftById(3L);
         assert adjustedGift.getId().equals(3L);
         assert adjustedGift.getAdjustedAmount().floatValue() == new BigDecimal("-1.01").floatValue();
         assert adjustedGift.getAdjustedReason().equals("Hola");
         assert adjustedGift.getAdjustedStatus().equals("Cancelled");
         assert "08/10/2009".equals(sdf.format(adjustedGift.getAdjustedTransactionDate()));
         assert "Partial Refund".equals(adjustedGift.getAdjustedType());
         assert adjustedGift.isAdjustedPaymentRequired();
         assert "foobar".equals(adjustedGift.getAdjustedPaymentTo());
         assert 300L == adjustedGift.getOriginalGiftId();
         assert 300f == adjustedGift.getOriginalAmount().floatValue();
         assert 200L == adjustedGift.getConstituent().getId();
         
         assert adjustedGift.getDistributionLines().size() == 2;
         for (DistributionLine line : adjustedGift.getDistributionLines()) {
             assert line.getId() == 1300L || line.getId() == 1400L;
             assert 3L == line.getAdjustedGiftId();
             if (line.getId() == 1300L) {
                 assert new BigDecimal("-1").floatValue() == line.getAmount().floatValue();
             }
             if (line.getId() == 1400L) {
                 assert new BigDecimal("-.01").floatValue() == line.getAmount().floatValue();
             }
         }
     }
     
     @Test(groups = { "testReadAdjustedGift" })
     public void testReadAdjustedGiftsForOriginalGiftId() throws Exception {
         List<AdjustedGift> adjustedGifts = adjustedGiftDao.readAdjustedGiftsForOriginalGiftId(0L);
         assert adjustedGifts.isEmpty();
         
         adjustedGifts = adjustedGiftDao.readAdjustedGiftsForOriginalGiftId(300L);
         assert adjustedGifts.size() == 3;
         for (AdjustedGift adjustedGift : adjustedGifts) {
             assert adjustedGift.getId() == 1L || adjustedGift.getId() == 2L || adjustedGift.getId() == 3L;
             assert adjustedGift.getOriginalGiftId() == 300L;
             if (adjustedGift.getId() == 1L) {
                 assert adjustedGift.getDistributionLines().size() == 2;
             }
             if (adjustedGift.getId() == 2L) {
                 assert adjustedGift.getDistributionLines().size() == 1;
             }
             if (adjustedGift.getId() == 3L) {
                 assert adjustedGift.getDistributionLines().size() == 2;
             }
         }
     }
 
     @Test(groups = { "testReadAdjustedGift" })
     public void testReadTotalAdjustedAmountByConstituentId() throws Exception {
         BigDecimal amount = adjustedGiftDao.readTotalAdjustedAmountByConstituentId(200L);
         Assert.assertNotNull(amount);
         Assert.assertEquals(new BigDecimal("-181.00"), amount);
     }
 
     @Test(groups = { "testReadAdjustedGiftsBySegmentationReportIds" })
     public void testReadAdjustedGiftsBySegmentationReportIds() {
         Set<Long> reportIds = new HashSet<Long>();
         reportIds.add(50L);
        List<AdjustedGift> adjustedGifts = adjustedGiftDao.readAdjustedGiftsBySegmentationReportIds(reportIds, "createDate", "ASC", 0, 100, Locale.US);
         junit.framework.Assert.assertNotNull(adjustedGifts);
         junit.framework.Assert.assertEquals(2, adjustedGifts.size());
         for (AdjustedGift adjustedGift : adjustedGifts) {
             junit.framework.Assert.assertTrue(adjustedGift.getId() == 1L || adjustedGift.getId() == 2L);
         }
 
         reportIds = new HashSet<Long>();
         reportIds.add(0L);
        adjustedGifts = adjustedGiftDao.readAdjustedGiftsBySegmentationReportIds(reportIds, "createDate", "ASC", 0, 100, Locale.US);
         junit.framework.Assert.assertNotNull(adjustedGifts);
         junit.framework.Assert.assertTrue(adjustedGifts.isEmpty());
 
         reportIds = new HashSet<Long>();
         reportIds.add(51L);
        adjustedGifts = adjustedGiftDao.readAdjustedGiftsBySegmentationReportIds(reportIds, "createDate", "ASC", 0, 100, Locale.US);
         junit.framework.Assert.assertNotNull(adjustedGifts);
         junit.framework.Assert.assertEquals(1, adjustedGifts.size());
         junit.framework.Assert.assertEquals(new Long(3L), adjustedGifts.get(0).getId());
     }
 
     @Test(groups = { "testReadAdjustedGiftsBySegmentationReportIds" })
     public void testReadCountAdjustedGiftsBySegmentationReportIds() {
         Set<Long> reportIds = new HashSet<Long>();
         reportIds.add(50L);
         Assert.assertEquals(2, adjustedGiftDao.readCountAdjustedGiftsBySegmentationReportIds(reportIds));
 
         reportIds = new HashSet<Long>();
         reportIds.add(51L);
         Assert.assertEquals(1, adjustedGiftDao.readCountAdjustedGiftsBySegmentationReportIds(reportIds));
 
         reportIds = new HashSet<Long>();
         reportIds.add(0L);
         Assert.assertEquals(0, adjustedGiftDao.readCountAdjustedGiftsBySegmentationReportIds(reportIds));
     }
 }
