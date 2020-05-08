 package com.mpower.test.dao.ibatis;
 
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
import com.mpower.dao.interfaces.PicklistDao;

 /**
  * @version 1.0
  */
 public class IBatisPicklistDaoTest extends AbstractIBatisTest {
 
     private PicklistDao picklistDao;
 
     @BeforeMethod
     public void setup() {
         picklistDao = (PicklistDao)super.applicationContext.getBean("picklistDAO");
     }
 
     @Test
     public void testReadPicklist() throws Exception {
 
//        Picklist picklist = picklistDao.readPicklistById("maritalStatus");
         
 
     }
 
 
 
 
 }
