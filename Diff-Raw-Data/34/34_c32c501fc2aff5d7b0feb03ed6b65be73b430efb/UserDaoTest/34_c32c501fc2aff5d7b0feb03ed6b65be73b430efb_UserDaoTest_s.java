 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.training.dao;
 
 import com.artivisi.training.domain.Phone;
 import com.artivisi.training.domain.User;
 import java.io.File;
 import java.sql.Connection;
 import java.util.List;
 import javax.sql.DataSource;
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.operation.DatabaseOperation;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  *
  * @author endy
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:konfig-spring.xml")
 public class UserDaoTest {
     
     @Autowired private UserDao userDao;
     @Autowired @Qualifier("dataSource1") 
     private DataSource dataSource;
     
     @Before
     public void resetIsiDatabase() throws Exception {
         Connection conn = dataSource.getConnection();
         DatabaseOperation.CLEAN_INSERT
                 .execute(new DatabaseConnection(conn), 
                 new FlatXmlDataSetBuilder()
                 .build(new File("src/test/resources/sample-data.xml")));
         conn.close();
     }
         
     
     @Test
     public void testInsert() throws Exception {
         User u = new User();
         u.setUsername("endy");
         u.setPassword("123");
         
         u.getDaftarEmail().add("endy.muhardin@gmail.com");
         
         // object phone
         Phone p = new Phone();
         p.setKeterangan("Telepon Kantor");
         p.setNomer("021-8661859");
         
         // tambahkan ke user
         u.getDaftarTelepon().add(p);
 
         Assert.assertNull(u.getId());
         
         // Tinggal dipakai, tidak perlu repot inisialisasi dan inject
         userDao.simpan(u);
         
         // cek hasil insert
         Assert.assertNotNull(u.getId());
     }
     
     @Test
     public void testCariByUsername(){
         User u = userDao.cariByUsername("dadang");
         Assert.assertNotNull(u);
         Assert.assertEquals("dadang", u.getUsername());
         Assert.assertEquals("123", u.getPassword());
         
         Assert.assertNull(userDao.cariByUsername("doni"));
     }
     
     @Test
     public void testHitungJumlahUser(){
         Long hasil = userDao.hitungSemuaUser();
         Assert.assertTrue("Jumlah user harusnya 2", hasil == 1);
     }
     
     @Test
     public void testDelete(){
         User u = userDao.cariByUsername("dadang");
         Assert.assertNotNull(u);
         
         userDao.hapus(u);
         
         User ux = userDao.cariByUsername("dadang");
         Assert.assertNull(ux);
     }
     
     @Test
     public void testCariUserByRole(){
         List<User> hasil1 = userDao.cariUserDenganNamaRole("admin");
         Assert.assertEquals(new Integer(0), new Integer(hasil1.size()));
         
         List<User> hasil2 = userDao.cariUserDenganNamaRole("sta");
         Assert.assertEquals(new Integer(1), new Integer(hasil2.size()));
     }
     
 }
