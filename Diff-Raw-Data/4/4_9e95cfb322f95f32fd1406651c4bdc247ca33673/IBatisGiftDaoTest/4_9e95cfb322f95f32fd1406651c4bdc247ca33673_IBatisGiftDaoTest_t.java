 package com.mpower.test.dao.ibatis;
 
 import java.math.BigDecimal;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.mpower.dao.interfaces.AddressDao;
 import com.mpower.dao.interfaces.CommitmentDao;
 import com.mpower.dao.interfaces.ConstituentDao;
 import com.mpower.dao.interfaces.EmailDao;
 import com.mpower.dao.interfaces.GiftDao;
 import com.mpower.dao.interfaces.PhoneDao;
 import com.mpower.domain.model.Commitment;
 import com.mpower.domain.model.Gift;
 import com.mpower.domain.model.PaymentSource;
 import com.mpower.domain.model.Person;
 import com.mpower.domain.model.communication.Address;
 import com.mpower.domain.model.communication.Email;
 import com.mpower.domain.model.communication.Phone;
 
 public class IBatisGiftDaoTest extends AbstractIBatisTest {
     
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
     
     private ConstituentDao constituentDao;
     private AddressDao addressDao;
     private EmailDao emailDao;
     private PhoneDao phoneDao;
     //private PaymentSourceDao paymentSourceDao;
     private CommitmentDao commitmentDao;
     private GiftDao giftDao;
 
     @BeforeMethod
     public void setup() {
         constituentDao = (ConstituentDao)super.applicationContext.getBean("constituentDAO");
         addressDao = (AddressDao)super.applicationContext.getBean("addressDAO");
         emailDao = (EmailDao)super.applicationContext.getBean("emailDAO");
         phoneDao = (PhoneDao)super.applicationContext.getBean("phoneDAO");
         //paymentSourceDao = (PaymentSourceDao)super.applicationContext.getBean("paymentSourceDAO");
         commitmentDao = (CommitmentDao)super.applicationContext.getBean("commitmentDAO");
         giftDao = (GiftDao)super.applicationContext.getBean("giftDAO");
     }
 
     @Test(groups = { "testMaintainGift" })
     public void testMaintainGift() throws Exception {
 
     	Person person = constituentDao.readAllConstituentsBySite().get(0);
     	
     	Address address = new Address();
     	address.setAddressLine1("12345 Main");
     	address.setPostalCode("77777");
     	address.setPersonId(person.getId());
     	addressDao.maintainAddress(address);
 
     	Phone phone = new Phone();
     	phone.setNumber("777-7777");
     	phone.setPersonId(person.getId());
     	phoneDao.maintainPhone(phone);
     	
     	Email email = new Email();
     	email.setPersonId(person.getId());
     	email.setEmailAddress("asdf@asdf.com");
     	emailDao.maintainEmail(email);
     	
     	person.getAddresses().add(address);
     	person.getEmails().add(email);
     	person.getPhones().add(phone);
     	constituentDao.maintainConstituent(person);
     	
     	PaymentSource paymentSource = new PaymentSource();
    	paymentSource.setPaymentType(PaymentSource.CASH);
     	paymentSource.setPerson(person);
     	//paymentSourceDao.maintainPaymentSource(paymentSource);
     	
     	Commitment commitment = new Commitment();
     	commitment.setPerson(person);
     	commitment.setAddress(address);
     	commitment.setPhone(phone);
     	commitment.setEmail(email);
         commitment.setPaymentSource(paymentSource);
     	commitment.setAmountPerGift(new BigDecimal(150.25));
     	commitmentDao.maintainCommitment(commitment);
     	
         // Insert
         Gift gift = new Gift();
         gift.setAcknowledgmentDate(new java.util.Date());
         gift.setAddress(person.getAddresses().get(0));
         gift.setAmount(new BigDecimal(125.50));
         gift.setCurrencyCode("USD");
         gift.setComments("Gift comments....!@#$%^&*()_+<>?");
         gift.setPerson(person);
         gift.setEmail(email);
         gift.setPhone(phone);
         gift.setAddress(address);
         gift.setPaymentSource(paymentSource);
         gift.setCommitment(commitment);
         gift.setTxRefNum("123123123123123");
         
         giftDao.maintainGift(gift);
         
         // Update
         giftDao.maintainGift(gift);
         gift.setEmail(null);
         gift = giftDao.maintainGift(gift);
         
         
         // Read
         
         gift = giftDao.readGift(gift.getId());
         // assert gift.getEmail() == null;   
         
 
     }
     
  }
