 package com.intelliworx.service.company;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.intelliworx.persistence.address.IAddressDTO;
 import com.intelliworx.persistence.company.ICompanyDTO;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 // ensures database 'roll back' occurs when test ends.
 @ContextConfiguration(locations = { "classpath:spring/applicationContext.xml",
 		"classpath:spring/spring-dataAccessContext.xml",
 		"classpath:spring/beanRefFactory.xml", "classpath:spring/sql-error-codes.xml", "classpath:mybatis/*.*" })
 public class TestCompanyService {
 
 	@Autowired
 	private ICompanyService companyService;
 	@Autowired
 	private BeanFactory beanFactory;
 
 	@Before
 	public void setUp() {
 		// ApplicationContext context = new ClassPathXmlApplicationContext(
 		// new String[] { "spring/applicationContext.xml",
 		// "spring/spring-dataAccessContext.xml",
 		//
 		// companyService = (ICompanyService) beanFactory
 		// .getBean("companyService");
 	}
 
 	@Test
 	public void testFindCompanyByPrimaryKey() {
 
 		final int companyId = 1;
 		ICompanyDTO company = companyService.find(companyId);
 		assertNotNull(company);
 
 		assertEquals("INTELLIWORX LIMITED", company.getName());
 		assertEquals("123435", company.getRegistrationNumber());
 		assertEquals("INTWX", company.getCode());
 		assertNull(company.getDateModified());
 
 		LocalDate incorporationDate = company.getIncorporationDate();
 		int incYear = incorporationDate.getYear();
 		int incMonth = incorporationDate.getMonthOfYear();
 		int incDay = incorporationDate.getDayOfMonth();
 
 		assertEquals(2011, incYear);
 		assertEquals(9, incMonth);
 		assertEquals(23, incDay);
 
 		LocalDateTime dateCreated = company.getDateCreated();
 		int createdYear = dateCreated.getYear();
 		int createdMonth = dateCreated.getMonthOfYear();
 		int createdDay = dateCreated.getDayOfMonth();
 
 		assertEquals(2012, createdYear);
 		assertEquals(9, createdMonth);
		assertEquals(15, createdDay);
 
 		IAddressDTO address = company.getAddress();
 		assertNotNull(address);
 
 		assertEquals(1, address.getPrimaryKey().intValue());
 		assertEquals("2 Haytor Rise", address.getAddress1());
 		assertEquals("Coventry", address.getAddress2());
 		assertEquals("CV2 3LE", address.getPostalCode());
 		assertNull(address.getAddress3());
 		assertNull(address.getAddress4());
 		assertNull(address.getAddress5());
 		assertEquals("info@intelliworx.co.uk", address.getEmailAddress());
 		assertEquals("07547032355", address.getTelephone());
 		assertNull(address.getDateModified());
 	}
 
 	@Test
 	public void testFindCompanyByCode() {
 
 		final String companyCode = "INTWX";
 		ICompanyDTO company = companyService.find(companyCode);
 		assertNotNull(company);
 
 		assertEquals(1, company.getPrimaryKey().intValue());
 		assertEquals("INTELLIWORX LIMITED", company.getName());
 		assertEquals("123435", company.getRegistrationNumber());
 		assertEquals(companyCode, company.getCode());
 		assertNull(company.getDateModified());
 
 		LocalDate incorporationDate = company.getIncorporationDate();
 		int incYear = incorporationDate.getYear();
 		int incMonth = incorporationDate.getMonthOfYear();
 		int incDay = incorporationDate.getDayOfMonth();
 
 		assertEquals(2011, incYear);
 		assertEquals(9, incMonth);
 		assertEquals(23, incDay);
 
 		LocalDateTime dateCreated = company.getDateCreated();
 		int createdYear = dateCreated.getYear();
 		int createdMonth = dateCreated.getMonthOfYear();
 		int createdDay = dateCreated.getDayOfMonth();
 
 		assertEquals(2012, createdYear);
 		assertEquals(9, createdMonth);
		assertEquals(15, createdDay);
 
 		IAddressDTO address = company.getAddress();
 		assertNotNull(address);
 
 		assertEquals(1, address.getPrimaryKey().intValue());
 		assertEquals("2 Haytor Rise", address.getAddress1());
 		assertEquals("Coventry", address.getAddress2());
 		assertEquals("CV2 3LE", address.getPostalCode());
 		assertNull(address.getAddress3());
 		assertNull(address.getAddress4());
 		assertNull(address.getAddress5());
 		assertEquals("info@intelliworx.co.uk", address.getEmailAddress());
 		assertEquals("07547032355", address.getTelephone());
 		assertNull(address.getDateModified());
 	}
 
 	@Test
 	@Transactional
 	public void testAddCompany() {
 		ICompanyDTO companyDTO = beanFactory
 				.getBean(com.intelliworx.persistence.company.ICompanyDTO.class);
 		companyDTO.setName("My Test Company");
 		companyDTO.setRegistrationNumber("TestReg1");
 		companyDTO.setCode("Test1234");
 		companyDTO.setIncorporationDate(new LocalDate("1996-01-20"));
 
 		IAddressDTO address = beanFactory
 				.getBean(com.intelliworx.persistence.address.IAddressDTO.class);
 		address.setAddress1("421 Acadia Drive");
 		address.setAddress2("Hamilton");
 		address.setAddress3("Ontario");
 		address.setAddress4("East Coast");
 		address.setAddress5("Canada");
 		address.setPostalCode("L8W 2R4");
 		address.setEmailAddress("myname@mycompany.com");
 		address.setTelephone("99999999999");
 		address.setFax("1111111111");
 
 		companyDTO.setAddress(address);
 		companyService.save(companyDTO);
 
 		Integer addressKey = address.getPrimaryKey();
 		Integer companyKey = companyDTO.getPrimaryKey();
 
 		assertNotNull(addressKey);
 		assertNotNull(companyKey);
 
 		assertEquals(0, address.getOptCount());
 		assertEquals(0, companyDTO.getOptCount());
 
 		assertEquals(addressKey, companyDTO.getAddress().getPrimaryKey());
 
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(
 				(DataSource) beanFactory.getBean("dataSource"));
 		Map<String, Object> companyResult = jdbcTemplate
 				.queryForMap("SELECT * FROM COMPANY WHERE ID = "
 						+ companyKey.intValue());
 		Map<String, Object> addressResult = jdbcTemplate
 				.queryForMap("SELECT * FROM ADDRESS WHERE ID = "
 						+ addressKey.intValue());
 
 		assertEquals(companyDTO.getPrimaryKey(),
 				(Integer) companyResult.get("ID"));
 		assertEquals("My Test Company", (String) companyResult.get("NAME"));
 		assertEquals(address.getPrimaryKey(),
 				(Integer) companyResult.get("ADDRESS_ID"));
 		assertEquals("Test1234", (String) companyResult.get("CODE"));
 		assertEquals("TestReg1", (String) companyResult.get("REG_NO"));
 		assertEquals("1996-01-20", (String) companyResult.get("INC_DATE"));
 		assertNull((Integer) companyResult.get("TX_SCHEME_ID"));
 		assertNotNull((String) companyResult.get("DATE_CREATED"));
 		assertNull((String) companyResult.get("DATE_MODIFIED"));
 		assertEquals(0, ((Integer) companyResult.get("OPT_COUNT")).intValue());
 
 		assertEquals(address.getPrimaryKey(), (Integer) addressResult.get("ID"));
 		assertEquals("421 Acadia Drive",
 				(String) addressResult.get("ADDRESS_1"));
 		assertEquals("Hamilton", (String) addressResult.get("ADDRESS_2"));
 		assertEquals("Ontario", (String) addressResult.get("ADDRESS_3"));
 		assertEquals("East Coast", (String) addressResult.get("ADDRESS_4"));
 		assertEquals("Canada", (String) addressResult.get("ADDRESS_5"));
 		assertEquals("L8W 2R4", (String) addressResult.get("POSTAL_CODE"));
 		assertEquals("myname@mycompany.com",
 				(String) addressResult.get("EMAIL"));
 		assertEquals("99999999999", (String) addressResult.get("TELEPHONE"));
 		assertEquals("1111111111", (String) addressResult.get("FAX"));
 		assertNotNull((String) addressResult.get("DATE_CREATED"));
 		assertNull((String) addressResult.get("DATE_MODIFIED"));
 		assertEquals(0, ((Integer) addressResult.get("OPT_COUNT")).intValue());
 	}
 
 	@Test
 	@Transactional
 	public void testUpdateCompany() {
 		ICompanyDTO company = companyService.find(1);
 		company.setName("Modified Name");
 		int currentOptCount = company.getOptCount();
 
 		companyService.save(company);
 
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(
 				(DataSource) beanFactory.getBean("dataSource"));
 		Map<String, Object> companyResult = jdbcTemplate
 				.queryForMap("SELECT * FROM COMPANY WHERE ID = 1");
 
 		String modifiedName = (String) companyResult.get("NAME");
 
 		assertEquals("Modified Name", modifiedName);
 		assertEquals(currentOptCount + 1,
 				((Integer) companyResult.get("OPT_COUNT")).intValue());
 
 		// TODO ensure address is not updated
 
 	}
 
 	@Test
 	@Transactional
 	public void testAddDuplicateCompanyRegNo() {
 
 		ICompanyDTO companyDTO = beanFactory
 				.getBean(com.intelliworx.persistence.company.ICompanyDTO.class);
 		companyDTO.setName("My Test Company");
 		companyDTO.setRegistrationNumber("123435");
 		companyDTO.setCode("Test1234");
 		companyDTO.setIncorporationDate(new LocalDate("1996-01-20"));
 
 		IAddressDTO address = beanFactory
 				.getBean(com.intelliworx.persistence.address.IAddressDTO.class);
 		address.setAddress1("421 Acadia Drive");
 		address.setAddress2("Hamilton");
 		address.setAddress3("Ontario");
 		address.setAddress4("East Coast");
 		address.setAddress5("Canada");
 		address.setPostalCode("L8W 2R4");
 		address.setEmailAddress("myname@mycompany.com");
 		address.setTelephone("99999999999");
 		address.setFax("1111111111");
 
 		companyDTO.setAddress(address);
 		try {
 			companyService.save(companyDTO);
 			fail("Exception not thrown");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	@Transactional
 	public void testDeleteCompany() {
 
 	}
 
 	@Test
 	@Transactional
 	public void testChangeCompanyAddress() {
 
 	}
 
 }
