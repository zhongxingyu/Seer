 package com.lebk.services.test;
 
 import static org.junit.Assert.*;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.lebk.po.Product;
 import com.lebk.po.Pttype;
 import com.lebk.services.ProductService;
 import com.lebk.services.impl.ProductServiceImpl;
 import com.lebk.services.test.ProductServiceTest;
 
 /**
  * Copyright: All Right Reserved.
  * 
  * @author Lei Bo(lebk.lei@gmail.com)
  * @contact: qq 87535204
  * @date 2013-11-15
  */
 
 public class ProductServiceTest
 {
   static Logger logger = Logger.getLogger(ProductServiceTest.class);
   ProductService ps = new ProductServiceImpl();
 
   @Before
   public void setUp() throws Exception
   {
   }
 
   @After
   public void tearDown() throws Exception
   {
   }
 
   @Test
   public void testUpdateProduct()
   {
     String pName = "";
    String ptType = "测试产品类型";
     String ptColor = "测试颜色";
     String ptSize = "测试大小";
     Integer pNum = 99;
     String businessType = "入库";
     String opUser = "管理员";
     Boolean status = ps.updateProduct(pName, ptType, ptColor, ptSize, pNum, businessType, opUser);
     Assert.assertTrue("Expect update successfully, return true", status == true);
 
     status = ps.updateProduct(pName, ptType, ptColor, ptSize, pNum, businessType, opUser);
     Assert.assertTrue("Expect update successfully, return true", status == true);
 
     // 出库
     businessType = "出库";
     status = ps.updateProduct(pName, ptType, ptColor, ptSize, pNum, businessType, opUser);
     Assert.assertTrue("Expect update successfully, return true", status == true);
     // 没有足够铲平出库，返回false
     status = ps.updateProduct(pName, ptType, ptColor, ptSize, 999999999, businessType, opUser);
     Assert.assertTrue("Expect update successfully, return true", status == false);
 
   }

   @Ignore
   @Test
   public void testCleanUpAll()
   {
     String opUser = "admin";
     ps.cleanUpAll(opUser);
   }
 
   @Test
   public void testGetAllProductList()
   {
     List<Product> pl = ps.getAllProductList();
 
     for (Iterator it = pl.iterator(); it.hasNext();)
     {
       logger.info("The product is:" + (Product) it.next());
     }
   }
 
 }
