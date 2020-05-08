 package com.artivisi.penagihan.dao;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.dataset.CompositeDataSet;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.operation.DatabaseOperation;
 import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.artivisi.penagihan.domain.Nasabah;
 import com.artivisi.penagihan.domain.PenagihanService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath*:com/artivisi/**/applicationContext.xml")
 public class NasabahDaoTest {
 
 	@Autowired
 	private PenagihanService penagihanService;
 
 	@Autowired
 	private DataSource dataSource;
 
 	@Before
 	public void hapusDataTest() throws Exception {
 		IDataSet[] daftarDataset = new IDataSet[] { new FlatXmlDataSetBuilder()
 				.build(new File("src/test/resources/dbunit/nasabah.xml")) };
 
 		Connection conn = dataSource.getConnection();
 		DatabaseOperation.CLEAN_INSERT.execute(new DatabaseConnection(conn),
 				new CompositeDataSet(daftarDataset));
 
 		conn.close();
 	}
 
 	@Test
 	public void encryptJasypt() {
 		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
 		encryptor.setPassword("test123");
 		encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
 		String encryptedText = encryptor.encrypt("admin");
 		System.out.println("Encrypted Password : [" + encryptedText + "]");
 
 		String decrypted = encryptor.decrypt(encryptedText);
 		System.out.println("Decrypted Password : [" + decrypted + "]");
 	}
 
 	@Test
 	public void testInsertNasabah() throws Exception {
 		System.out.println("Tes Konfigurasi Spring Hibernate");
 
 		Nasabah n = new Nasabah();
 		n.setNomer("N-001");
 		n.setNama("Endy Muhardin");
 		penagihanService.simpan(n);
 
 		String sqlCheck = "select * from m_nasabah where nomer = ?";
 		Connection conn = dataSource.getConnection();
 		PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
 		psCheck.setString(1, "N-001");
 		ResultSet rs = psCheck.executeQuery();
 
 		Assert.assertTrue(rs.next()); // pastikan query menghasilkan data
 		Assert.assertNotNull(rs.getString("id"));
 		Assert.assertEquals("Endy Muhardin", rs.getString("nama"));
 
 		conn.close();
 	}
 
 	@Test
 	public void testCariNasabahById() {
 		// cari data yang ada
 		Nasabah n = penagihanService.cariNasabahById("abcd1234");
 		Assert.assertNotNull(n);
 		Assert.assertEquals("Anton", n.getNama());
 
 		// cari data yang tidak ada
 		Assert.assertNull(penagihanService.cariNasabahById("xxx321"));
 	}
 
 	@Test
 	public void testCariNasabahByNomer() {
 		// cari data yang ada
 		Nasabah n = penagihanService.cariNasabahByNomer("N-123");
 		Assert.assertNotNull(n);
 		Assert.assertEquals("Anton", n.getNama());
 
 		// cari data yang tidak ada
 		Assert.assertNull(penagihanService.cariNasabahByNomer("X-321"));
 	}
 	
 	@Test
 	public void testHitungSemuaNasabah(){
 		Assert.assertEquals(Long.valueOf(1), Long.valueOf(penagihanService.hitungSemuaNasabah()));
 	}
 	
 	@Test
 	public void testCariSemuaNasabah(){
 		List<Nasabah> hasil = penagihanService.cariSemuaNasabah(0, 10);
 		Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(hasil.size()));
 		Nasabah n = hasil.get(0);
 		Assert.assertNotNull(n);
 		Assert.assertEquals("Anton", n.getNama());
 	}
 }
