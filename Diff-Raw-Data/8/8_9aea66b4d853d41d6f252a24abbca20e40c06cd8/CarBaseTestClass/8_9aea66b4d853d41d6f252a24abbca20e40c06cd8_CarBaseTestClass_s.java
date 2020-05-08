package com.hanaden.demo.jpaspringmvc.persistence.test.hibernate;
 
 import com.hanaden.demo.jpaspringmvc.domain.CarVo;
 import com.hanaden.demo.jpaspringmvc.persistence.CarDao;
 import com.hanaden.demo.jpaspringmvc.domain.hibernate.CarVoHibernate;
 import java.util.List;
 import javax.annotation.Resource;
 import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
 import org.apache.log4j.Logger;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author hanasaki
  */
 public class CarBaseTestClass {
 
     @Resource(name = "beanCarDao")
     private CarDao carDao;
     private static final Logger logger = Logger.getLogger(CarBaseTestClass.class);
     protected long pk;
 
     @Before
     @Transactional(value = "beanTransactionManagerBasicTest", propagation = Propagation.REQUIRES_NEW)
     public void init() throws Throwable {
         Assert.assertNotNull(carDao);
         //        
         pk = 0;
 
         //
         CarVo car = new CarVoHibernate();
         car.setManufacturer("company");
         car.setModel("model");
         car.setYear(234);
 
         //
 //        Assert.assertNull("PK should be null : car.pk = " + car.getId(), car.getId());
         car = carDao.save(car);
         pk = car.getId();
         Assert.assertNotNull("PK should be !null : pk = " + pk, pk); // same as car
 
         //
         List<CarVo> cars = carDao.getCars();
 
         //
         logger.debug((ReflectionToStringBuilder.toString(car)));
     }
 
 //    @Test
     public void listCarsTest() {
         List<CarVo> cars = carDao.getCars();
         //
         Assert.assertNotNull(cars);
         Assert.assertEquals(1, cars.size());
     }
 
 //    @Test
     public void getCarTest() {
         CarVo car = carDao.getCar(pk);
         //
         Assert.assertEquals(pk, car.getId());
         // Assert.assertEquals("Boxster", car.getModel());
     }
 
     @Test
     public void dummyTest() {
     }
 
     /**
      * @param carDao the carDao to set
      */
     public void setCarDao(CarDao carDao) {
         this.carDao = carDao;
     }
 }
