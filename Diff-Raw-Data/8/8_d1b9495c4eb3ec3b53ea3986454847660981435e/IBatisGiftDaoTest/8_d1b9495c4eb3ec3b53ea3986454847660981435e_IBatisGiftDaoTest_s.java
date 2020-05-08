 package com.orangeleap.tangerine.test.dao.ibatis;
 
 import com.orangeleap.tangerine.dao.GiftDao;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.PaymentSource;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.communication.Address;
 import com.orangeleap.tangerine.domain.communication.Phone;
 import com.orangeleap.tangerine.domain.paymentInfo.Commitment;
 import com.orangeleap.tangerine.domain.paymentInfo.DistributionLine;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.type.GiftEntryType;
 import com.orangeleap.tangerine.type.GiftType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import junit.framework.Assert;
 import org.apache.commons.logging.Log;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 public class IBatisGiftDaoTest extends AbstractIBatisTest {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
     private GiftDao giftDao;
 
     @BeforeMethod
     public void setup() {
         giftDao = (GiftDao)super.applicationContext.getBean("giftDAO");
     }
 
     public static void testGiftId100(Gift gift) {
         assert gift != null;
         assert 100L == gift.getId();
         assert "Thank you for your support".equals(gift.getComments());
         assert gift.getTransactionDate() != null;
         assert Commitment.STATUS_PENDING.equals(gift.getGiftStatus());
         assert 1000 == gift.getAmount().intValue();
         assert 1000 == gift.getDeductibleAmount().intValue();
         assert PaymentSource.CASH.equals(gift.getPaymentType());
         assert GiftEntryType.MANUAL.equals(gift.getEntryType());
         assert gift.getDonationDate() == null;
         assert gift.getPostmarkDate() == null;
         assert gift.getAuthCode() == null;
         assert gift.getTxRefNum() == null;
         assert gift.getPaymentStatus() == null;
         assert gift.getPaymentMessage() == null;
         assert gift.getCheckNumber() == null;
         assert GiftType.MONETARY_GIFT.equals(gift.getGiftType());
 
         assert gift.getConstituent() != null && gift.getConstituent().getId() == 100L;
         assert "Billy Graham Ministries".equals(gift.getConstituent().getOrganizationName());
         assert "Graham".equals(gift.getConstituent().getLastName());
         assert "Billy".equals(gift.getConstituent().getFirstName());
 
         assert gift.getAddress() != null && gift.getAddress().getId() == 100L;
         assert "3726 THIRD ST".equals(gift.getAddress().getAddressLine1());
         assert "Dallas".equals(gift.getAddress().getCity());
         assert "TX".equals(gift.getAddress().getStateProvince());
         assert "75554".equals(gift.getAddress().getPostalCode());
         assert "US".equals(gift.getAddress().getCountry());
         assert gift.getAddress().getAddressLine2() == null;
         assert gift.getAddress().getAddressLine3() == null;
 
         assert gift.getPhone() != null && gift.getPhone().getId() == 100L;
         assert "214-443-6829".equals(gift.getPhone().getNumber());
         assert gift.getPhone().getCreateDate() != null;
         assert gift.getPhone().getUpdateDate() != null;
         assert 100L == gift.getPhone().getConstituentId();
 
         assert gift.getPaymentSource() == null;
     }
 
     public static void testGiftId300(Gift gift) {
         assert "Straight Cash Homey".equals(gift.getComments());
         assert gift.getTransactionDate() != null;
         assert 300 == gift.getAmount().intValue();
         assert 300 == gift.getDeductibleAmount().intValue();
         assert PaymentSource.ACH.equals(gift.getPaymentType());
         assert GiftEntryType.MANUAL.equals(gift.getEntryType());
         assert gift.getDonationDate() == null;
         assert gift.getPostmarkDate() == null;
         assert gift.getAuthCode() == null;
         assert gift.getTxRefNum() == null;
         assert gift.getPaymentStatus() == null;
         assert gift.getPaymentMessage() == null;
         assert gift.getCheckNumber() == null;
 
         assert gift.getDistributionLines() != null && gift.getDistributionLines().size() == 4;
         for (DistributionLine line : gift.getDistributionLines()) {
             assert line.getId() >= 100L && line.getId() <= 400L;
             assert 300L == line.getGiftId();
             assert 200L == line.getConstituent().getId();
             switch (line.getId().intValue()) {
                 case 100:
                     assert 150 == line.getAmount().intValue();
                     assert 50 == line.getPercentage().intValue();
                     assert "01000".equals(line.getProjectCode());
                     assert line.getMotivationCode() == null;
                     assert line.getOther_motivationCode() == null;
                     assert line.getPledgeId() == null;
                     break;
                 case 200:
                     assert 75 == line.getAmount().intValue();
                     assert 25 == line.getPercentage().intValue();
                     assert line.getProjectCode() == null;
                     assert line.getMotivationCode() == null;
                     assert "whoa".equals(line.getOther_motivationCode());
                     assert line.getPledgeId() == null;
                     break;
                 case 300:
                     assert 37.5 == line.getAmount().floatValue();
                     assert 12.5 == line.getPercentage().floatValue();
                     assert line.getProjectCode() == null;
                     assert "foo".equals(line.getMotivationCode());
                     assert line.getOther_motivationCode() == null;
                     assert line.getPledgeId() == null;
                     break;
                 case 400:
                     assert 37.5 == line.getAmount().floatValue();
                     assert 12.5 == line.getPercentage().floatValue();
                     assert line.getProjectCode() == null;
                     assert line.getMotivationCode() == null;
                     assert line.getOther_motivationCode() == null;
                     assert line.getPledgeId() == null;
                     break;
                 default:
                     assert false == true;
             }
         }
         assert gift.getAddress() == null;
         assert gift.getPhone() == null;
         assert gift.getPaymentSource() == null;
     }
 
     public static void testGiftId400(Gift gift) {
         assert "Rollin".equals(gift.getComments());
         assert gift.getTransactionDate() != null;
         assert 99999 == gift.getAmount().intValue();
         assert 99999== gift.getDeductibleAmount().intValue();
         assert PaymentSource.CREDIT_CARD.equals(gift.getPaymentType());
         assert GiftEntryType.MANUAL.equals(gift.getEntryType());
         assert gift.getDonationDate() == null;
         assert gift.getPostmarkDate() == null;
         assert gift.getAuthCode() == null;
         assert gift.getTxRefNum() == null;
         assert gift.getPaymentStatus() == null;
         assert gift.getPaymentMessage() == null;
         assert gift.getCheckNumber() == null;
 
         assert gift.getAddress() != null && gift.getAddress().getId() == 100L;
         assert "3726 THIRD ST".equals(gift.getAddress().getAddressLine1());
         assert "Dallas".equals(gift.getAddress().getCity());
         assert "TX".equals(gift.getAddress().getStateProvince());
         assert "75554".equals(gift.getAddress().getPostalCode());
         assert "US".equals(gift.getAddress().getCountry());
         assert gift.getAddress().getAddressLine2() == null;
         assert gift.getAddress().getAddressLine3() == null;
 
         IBatisPaymentSourceDaoTest.testId200(gift.getPaymentSource());
 
         assert gift.getPhone() == null;
         assert gift.getDistributionLines() != null && gift.getDistributionLines().isEmpty();
     }
 
     private void setupDistributionLines(Gift gift) {
         List<DistributionLine> lines = new ArrayList<DistributionLine>();
 
         DistributionLine line = new DistributionLine();
         line.setAmount(new BigDecimal(100.50));
         line.setProjectCode("foo");
         line.setGiftId(gift.getId());
         lines.add(line);
 
         line = new DistributionLine();
         line.setAmount(new BigDecimal(125.50));
         line.setMotivationCode("bar");
         line.setGiftId(gift.getId());
         lines.add(line);
 
         gift.setDistributionLines(lines);
     }
 
     @Test(groups = { "testMaintainGift" }, dependsOnGroups = { "testReadGift" })
     public void testMaintainGift() throws Exception {
         // Insert
         Gift gift = new Gift();
         SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
         gift.setDonationDate(sdf.parse("10/31/1999"));
         gift.setEntryType(GiftEntryType.AUTO);
         gift.setAmount(new BigDecimal(125.50));
         gift.setCurrencyCode("JPY");
         gift.setComments("foobar!!");
         gift.setTxRefNum("0101010101");
         gift.setGiftType(GiftType.MONETARY_GIFT);
         gift.setGiftStatus(Commitment.STATUS_IN_PROGRESS);
         Address addr = new Address();
         addr.setId(100L);
         gift.setAddress(addr);
         PaymentSource src = new PaymentSource();
         src.setId(100L);
         gift.setPaymentSource(src);
         Site site = new Site("company1");
         Constituent constituent = new Constituent();
         constituent.setId(100L);
         constituent.setSite(site);
         gift.setConstituent(constituent);
         setupDistributionLines(gift);
         gift.addAssociatedPledgeId(500L);
         gift.addAssociatedPledgeId(700L);
 
         gift = giftDao.maintainGift(gift);
         assert gift.getId() > 0;
 
         Gift readGift = giftDao.readGiftById(gift.getId());
         assert readGift != null;
         assert gift.getId().equals(readGift.getId());
         assert Commitment.STATUS_IN_PROGRESS.equals(readGift.getGiftStatus());
         assert sdf.parse("10/31/1999").equals(readGift.getDonationDate());
         assert GiftEntryType.AUTO.equals(readGift.getEntryType());
         assert 125.5 == readGift.getAmount().floatValue();
         assert "JPY".equals(readGift.getCurrencyCode());
         assert "foobar!!".equals(readGift.getComments());
         assert "0101010101".equals(readGift.getTxRefNum());
         assert GiftType.MONETARY_GIFT.equals(readGift.getGiftType());
         assert readGift.getAddress() != null && 100L == readGift.getAddress().getId();
         assert readGift.getPhone() == null;
         assert readGift.getPaymentSource() != null && 100L == readGift.getPaymentSource().getId();
         assert readGift.getConstituent() != null && 100L == readGift.getConstituent().getId();
         Assert.assertNotNull("readGift distributionLines is null", readGift.getDistributionLines());
         Assert.assertEquals("readGift distributionLines size is " + readGift.getDistributionLines().size(), 2, readGift.getDistributionLines().size());
         assert readGift.getRecurringGiftId() == null;
         assert readGift.getPostmarkDate() == null;
         assert StringConstants.EMPTY.equals(readGift.getAuthCode());
         assert readGift.getPaymentStatus().isEmpty();
         assert readGift.getPaymentMessage() == null;
         assert readGift.getCheckNumber() == null;
 
         for (DistributionLine line : readGift.getDistributionLines()) {
             assert readGift.getId().equals(line.getGiftId());
             assert line.getConstituent().getId() == 100L;
             if (100.5 == line.getAmount().floatValue()) {
                 assert "foo".equals(line.getProjectCode());
                 assert line.getMotivationCode() == null;
                 assert line.getOther_motivationCode() == null;
                 assert line.getPledgeId() == null;
             }
             else if (125.5 == line.getAmount().floatValue()) {
                 assert "bar".equals(line.getMotivationCode());
                 assert line.getProjectCode() == null;
                 assert line.getOther_motivationCode() == null;
                 assert line.getPledgeId() == null;
             }
             else {
                 assert false == true;
             }
         }
         assert readGift.getAssociatedPledgeIds() != null && readGift.getAssociatedPledgeIds().size() == 2;
         for (Long associatedPledgeId : readGift.getAssociatedPledgeIds()) {
             assert associatedPledgeId == 500L || associatedPledgeId == 700L;
         }
 
         // Update
         gift = readGift;
         gift.setPaymentMessage("Hi mom");
         gift.setCheckNumber("111");
         gift.setEntryType(GiftEntryType.MANUAL);
         gift.setCurrencyCode("USD");
         gift.setGiftStatus(Commitment.STATUS_FULFILLED);
 
         gift.setAddress(null);
         Phone phone = new Phone();
         phone.setId(100L);
         gift.setPhone(phone);
         for (DistributionLine line : gift.getDistributionLines()) {
             if (100.5 == line.getAmount().floatValue()) {
                 line.setProjectCode(null);
                 line.setOther_motivationCode("sss");
                 line.setAmount(new BigDecimal(0.5));
             }
         }
         gift.setAssociatedPledgeIds(new ArrayList<Long>());
         gift.addAssociatedPledgeId(700L);
         gift = giftDao.maintainGift(gift);
         readGift = giftDao.readGiftById(gift.getId());
         assert readGift != null;
         assert gift.getId().equals(readGift.getId());
         assert Commitment.STATUS_FULFILLED.equals(readGift.getGiftStatus());
         assert sdf.parse("10/31/1999").equals(readGift.getDonationDate());
         assert GiftEntryType.MANUAL.equals(readGift.getEntryType());
         assert GiftType.MONETARY_GIFT.equals(readGift.getGiftType());
         assert 125.5 == readGift.getAmount().floatValue();
         assert "USD".equals(readGift.getCurrencyCode());
         assert "foobar!!".equals(readGift.getComments());
         assert "0101010101".equals(readGift.getTxRefNum());
         assert readGift.getAddress() == null;
         assert readGift.getPhone() != null && readGift.getPhone().getId() == 100L;
         assert readGift.getPaymentSource() != null && 100L == readGift.getPaymentSource().getId();
         assert readGift.getConstituent() != null && 100L == readGift.getConstituent().getId();
         Assert.assertNotNull("readGift distributionLines is null", readGift.getDistributionLines());
         Assert.assertEquals("readGift distributionLines size is " + readGift.getDistributionLines().size(), 2, readGift.getDistributionLines().size());
         assert readGift.getPostmarkDate() == null;
         assert StringConstants.EMPTY.equals(readGift.getAuthCode());
         assert readGift.getPaymentStatus().isEmpty();
         assert "Hi mom".equals(readGift.getPaymentMessage());
        assert readGift.getCheckNumber().equals("111");
 
         for (DistributionLine line : readGift.getDistributionLines()) {
             assert readGift.getId().equals(line.getGiftId());
             if (0.5 == line.getAmount().floatValue()) {
                 assert line.getProjectCode() == null;
                 assert line.getMotivationCode() == null;
                 assert "sss".equals(line.getOther_motivationCode());
                 assert line.getPledgeId() == null;
             }
             else if (125.5 == line.getAmount().floatValue()) {
                 assert "bar".equals(line.getMotivationCode());
                 assert line.getProjectCode() == null;
                 assert line.getOther_motivationCode() == null;
                 assert line.getPledgeId() == null;
             }
             else {
                 Assert.assertEquals("amount = " + line.getAmount().floatValue(), false, true);
             }
         }
         assert readGift.getAssociatedPledgeIds() != null && readGift.getAssociatedPledgeIds().size() == 1;
         for (Long associatedPledgeId : readGift.getAssociatedPledgeIds()) {
             assert associatedPledgeId == 700L;
         }
 
         gift = readGift;
         gift.setAssociatedPledgeIds(null);
         gift.addAssociatedRecurringGiftId(300L);
         gift = giftDao.maintainGift(gift);
         readGift = giftDao.readGiftById(gift.getId());
         assert gift.getId().equals(readGift.getId());
         assert readGift.getAssociatedPledgeIds() != null && readGift.getAssociatedPledgeIds().isEmpty();
         assert readGift.getAssociatedRecurringGiftIds() != null && readGift.getAssociatedRecurringGiftIds().size() == 1 && readGift.getAssociatedRecurringGiftIds().get(0) == 300L;
 
         gift = readGift;
         gift.setAssociatedPledgeIds(null);
         gift.setAssociatedRecurringGiftIds(new ArrayList<Long>());
         gift = giftDao.maintainGift(gift);
         readGift = giftDao.readGiftById(gift.getId());
         assert gift.getId().equals(readGift.getId());
         assert readGift.getAssociatedPledgeIds() != null && readGift.getAssociatedPledgeIds().isEmpty();
         assert readGift.getAssociatedRecurringGiftIds() != null && readGift.getAssociatedRecurringGiftIds().isEmpty();
     }
 
     @Test(groups = { "testReadGift" })
     public void testReadGiftById() throws Exception {
         Gift gift = giftDao.readGiftById(0L);
         assert gift == null;
 
         gift = giftDao.readGiftById(100L);
         testGiftId100(gift);
         assert gift.getDistributionLines() != null && gift.getDistributionLines().isEmpty();
         Assert.assertNotNull("Expected associated pledgeIds to be not null", gift.getAssociatedPledgeIds());
         Assert.assertTrue("Expected associated pledgeIds to be empty", gift.getAssociatedPledgeIds().isEmpty());
         Assert.assertNotNull("Expected associated recurringGiftIds to be not null", gift.getAssociatedRecurringGiftIds());
         Assert.assertTrue("Expected associated recurringGiftIds to be empty", gift.getAssociatedRecurringGiftIds().isEmpty());
 
         gift = giftDao.readGiftById(300L);
         testGiftId300(gift);
         Assert.assertNotNull("Expected associated pledgeIds to be not null", gift.getAssociatedPledgeIds());
         Assert.assertEquals("Expected associated pledgeIds to be size 2", 2, gift.getAssociatedPledgeIds().size());
         for (Long associatedPledgeId : gift.getAssociatedPledgeIds()) {
             assert associatedPledgeId == 700L || associatedPledgeId == 800L;
         }
         Assert.assertNotNull("Expected associated recurringGiftIds to be not null", gift.getAssociatedRecurringGiftIds());
         Assert.assertTrue("Expected associated recurringGiftIds to be empty", gift.getAssociatedRecurringGiftIds().isEmpty());
 
         gift = giftDao.readGiftById(400L);
         testGiftId400(gift);
         assert gift.getAssociatedPledgeIds() != null && gift.getAssociatedPledgeIds().size() == 4;
         for (Long associatedPledgeId : gift.getAssociatedPledgeIds()) {
             assert associatedPledgeId == 500L || associatedPledgeId == 600L || associatedPledgeId == 700L || associatedPledgeId == 800L;
         }
         Assert.assertNotNull("Expected associated recurringGiftIds to be not null", gift.getAssociatedRecurringGiftIds());
         Assert.assertTrue("Expected associated recurringGiftIds to be empty", gift.getAssociatedRecurringGiftIds().isEmpty());
 
         gift = giftDao.readGiftById(200L);
         assert 200L == gift.getId();
         Assert.assertNotNull("Expected associated pledgeIds to be not null", gift.getAssociatedPledgeIds());
         Assert.assertTrue("Expected associated pledgeIds to be empty", gift.getAssociatedPledgeIds().isEmpty());
         assert gift.getAssociatedRecurringGiftIds() != null && gift.getAssociatedRecurringGiftIds().size() == 1;
         assert gift.getAssociatedRecurringGiftIds().get(0) == 100L;
 
         gift = giftDao.readGiftById(600L);
         assert 600L == gift.getId();
         Assert.assertNotNull("Expected associated pledgeIds to be not null", gift.getAssociatedPledgeIds());
         Assert.assertTrue("Expected associated pledgeIds to be empty", gift.getAssociatedPledgeIds().isEmpty());
         assert gift.getAssociatedRecurringGiftIds() != null && gift.getAssociatedRecurringGiftIds().size() == 1;
         assert gift.getAssociatedRecurringGiftIds().get(0) == 400L;
     }
 
     @Test(groups = { "testReadGift" })
     public void testReadLimitedGiftsByIds() throws Exception {
         Set<Long> giftIds = new HashSet<Long>();
         giftIds.add(100L);
         giftIds.add(400L);
         List<Gift> gifts = giftDao.readLimitedGiftsByIds(giftIds, StringConstants.ID, StringConstants.DESC, 0, 100, Locale.getDefault());
         Assert.assertNotNull(gifts);
         Assert.assertEquals(gifts.size(), 2);
         Assert.assertEquals(gifts.get(0).getId(), new Long(400L));
         Assert.assertEquals(gifts.get(1).getId(), new Long(100L));
         testGiftId400(gifts.get(0));
         testGiftId100(gifts.get(1));
     }
 
     @Test(groups = { "testReadGift" })
     public void testReadGiftsByConstituentId() throws Exception {
         List<Gift> gifts = giftDao.readMonetaryGiftsByConstituentId(0L);
         assert gifts != null && gifts.isEmpty();
 
         gifts = giftDao.readMonetaryGiftsByConstituentId(200L);
         assert gifts != null && gifts.size() == 2;
 
         for (Gift gift : gifts) {
             assert gift.getId() == 300L || gift.getId() == 400L;
 
             assert "Painters, Inc.".equals(gift.getConstituent().getOrganizationName());
             assert "Picasso".equals(gift.getConstituent().getLastName());
             assert "Pablo".equals(gift.getConstituent().getFirstName());
             assert gift.getConstituent().getMiddleName() == null;
             assert "Sr".equals(gift.getConstituent().getSuffix());
             assert "company1".equals(gift.getConstituent().getSite().getName());
             assert GiftType.MONETARY_GIFT.equals(gift.getGiftType());
 
             switch (gift.getId().intValue()) {
                 case 300:
                     testGiftId300(gift);
                     break;
                 case 400:
                     testGiftId400(gift);
                     break;
                 default:
                     assert false == true;
             }
         }
     }
 
     @Test(groups = { "testReadGift" })
     public void testReadAllGiftsBySite() throws Exception {
         List<Gift> gifts = giftDao.readAllGiftsBySite();
         Assert.assertNotNull(gifts);
         Assert.assertFalse(gifts.isEmpty());
     }
 
     @Test(groups = { "testAnalyze" })
     public void testAnalyzeMajorDonor() throws Exception {
         SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy kk:mm");
         assert 1025d == giftDao.analyzeMajorDonor(100L, sdf.parse("06/05/2007 00:00"), sdf.parse("06/07/2007 00:00"));
         assert 0 == giftDao.analyzeMajorDonor(100L, sdf.parse("01/01/1990 00:00"), sdf.parse("01/02/1990 00:00"));
         assert 1000d == giftDao.analyzeMajorDonor(100L, sdf.parse("06/06/2007 00:00"), sdf.parse("06/07/2007 00:00"));
     }
 
     @Test(groups = { "testAnalyze" })
     public void testAnalyzeLapsedDonor() throws Exception {
         SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy kk:mm");
         List<Constituent> constituents = giftDao.analyzeLapsedDonor(sdf.parse("06/05/2007 00:00"), sdf.parse("06/07/2007 00:00"));
         assert constituents != null && constituents.size() == 1 && constituents.get(0).getId() == 100L;
 
         constituents = giftDao.analyzeLapsedDonor(sdf.parse("06/03/2007 00:00"), sdf.parse("06/07/2007 00:00"));
         assert constituents != null && constituents.size() == 2;
         for (Constituent constituent : constituents) {
             assert constituent.getId() == 100L || constituent.getId() == 200L;
             switch (constituent.getId().intValue()) {
                 case 100:
                     assert "Billy Graham Ministries".equals(constituent.getOrganizationName());
                     assert "Graham".equals(constituent.getLastName());
                     assert "Billy".equals(constituent.getFirstName());
                     break;
                 case 200:
                     assert "Painters, Inc.".equals(constituent.getOrganizationName());
                     assert "Picasso".equals(constituent.getLastName());
                     assert "Pablo".equals(constituent.getFirstName());
                     break;
                 default:
                     Assert.assertEquals("Id = " + constituent.getId(), true, false);
             }
         }
     }
 
     @Test(groups = { "testReadGiftsBySegmentationReportIds" })
     public void testReadGiftsBySegmentationReportIds() {
         Set<Long> reportIds = new HashSet<Long>();
         reportIds.add(1L);
         List<Gift> gifts = giftDao.readGiftsBySegmentationReportIds(reportIds, "transactionDate", "ASC", 0, 100, Locale.US);
         Assert.assertNotNull(gifts);
         Assert.assertEquals(4, gifts.size());
         for (Gift gift : gifts) {
             Assert.assertTrue(gift.getId() == 100L || gift.getId() == 200L || gift.getId() == 300L || gift.getId() == 400L);
         }
 
         reportIds = new HashSet<Long>();
         reportIds.add(0L);
         gifts = giftDao.readGiftsBySegmentationReportIds(reportIds, "transactionDate", "ASC", 0, 100, Locale.US);
         Assert.assertNotNull(gifts);
         Assert.assertTrue(gifts.isEmpty());
         
         reportIds = new HashSet<Long>();
         reportIds.add(2L);
         gifts = giftDao.readGiftsBySegmentationReportIds(reportIds, "transactionDate", "ASC", 0, 100, Locale.US);
         Assert.assertNotNull(gifts);
         Assert.assertEquals(1, gifts.size());
         Assert.assertEquals(new Long(500L), gifts.get(0).getId());
 
         reportIds = new HashSet<Long>();
         reportIds.add(3L);
         gifts = giftDao.readGiftsBySegmentationReportIds(reportIds, "transactionDate", "ASC", 0, 100, Locale.US);
         Assert.assertNotNull(gifts);
         Assert.assertEquals(1, gifts.size());
         Assert.assertEquals(new Long(600L), gifts.get(0).getId());
     }
 
     @Test(groups = { "testSearchGifts" })
     public void testSearchGifts() throws Exception {
     	Map<String, Object> params = new HashMap<String, Object>();
         params.put("constituent.firstName", "Pablo");
         params.put("constituent.accountNumber", 2000000L);
         params.put("amount", new BigDecimal(300.00));
 
         List<Gift> gifts = giftDao.searchGifts(params);
         assert gifts != null && gifts.size() == 1;
         for (Gift gift : gifts) {
             assert gift.getConstituent().getFirstName().equals("Pablo");
             assert gift.getAmount().compareTo(new BigDecimal(300.00)) == 0;
         }
 
         gifts = giftDao.searchGifts(params, "constituent.accountNumber", "ASC", 0, 100, Locale.US);
         assert gifts != null && gifts.size() == 1;
         for (Gift gift : gifts) {
             assert gift.getConstituent().getFirstName().equals("Pablo");
             assert gift.getAmount().compareTo(new BigDecimal(300.00)) == 0;
         }
 
         params = new HashMap<String, Object>();
         gifts = giftDao.searchGifts(params, "constituent.accountNumber", "ASC", 0, 100, Locale.US);
         assert gifts != null && ! gifts.isEmpty();
 
         params = new HashMap<String, Object>();
         gifts = giftDao.searchGifts(params);
         assert gifts != null && ! gifts.isEmpty();
 
         params = new HashMap<String, Object>();
         params.put("constituent.firstName", "BOMBAY");
         params.put("constituent.accountNumber", 2000000L);
         params.put("amount", new BigDecimal(300.00));
         gifts = giftDao.searchGifts(params, "constituent.accountNumber", "ASC", 0, 100, Locale.US);
         Assert.assertTrue("gifts.size = " + gifts.size(), gifts != null && gifts.isEmpty());
 
         gifts = giftDao.searchGifts(params);
         Assert.assertTrue("gifts.size = " + gifts.size(), gifts != null && gifts.isEmpty());
     }
 }
