 package com.manning.sdmia.ch06.directory.web;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import junit.framework.Assert;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.osgi.framework.ServiceReference;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;
import org.springframework.osgi.test.platform.OsgiPlatform;
 
 import com.manning.sdmia.directory.domain.Contact;
 import com.manning.sdmia.directory.service.ContactService;
 
 /**
  * 
  * @author acogoluegnes
  *
  */
 public class EnterpriseWebSoaTest extends AbstractConfigurableBundleCreatorTests {
 	
 	/** Dummy property to dynamically add the import-package */
 	private BasicDataSource basicDataSource;
 	
 	private DataSource dataSource;
 	
 	private static final String SPRING_OSGI_GROUP = "org.springframework.osgi";
 
 	public void testIntegration() throws Exception {
 		// waits a little for the datasource service (from test) to be imported by dao impl bundle
 		Thread.sleep(1000);
 		
 		// creates table and inject data
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		jdbcTemplate.batchUpdate(new String [] {
 				"CREATE SEQUENCE seq_directory_contact_idcontact START WITH 1 INCREMENT BY 1",
 				"create table directory_contact(idcontact integer not null, firstname varchar(255), lastname varchar(255), primary key (idcontact))",
 				"insert into directory_contact (idcontact,firstname,lastname) values (NEXT VALUE FOR seq_directory_contact_idcontact,'arnaud','cogoluegnes')"						
 		});
 		
 		ServiceReference serviceRef = bundleContext.getServiceReference(ContactService.class.getName());
 		
 		assertNotNull("Service Reference is null",serviceRef);
 		
 		ContactService service = (ContactService) bundleContext.getService(serviceRef);
 		assertNotNull("Cannot find the business service",service);
 		
 		List<Contact> contacts = service.getContacts();
 
 		assertEquals(1,contacts.size());				
 		
 		Assert.assertTrue(getTextResponse("http://localhost:8080/directory/contacts.htm").contains("arnaud cogoluegnes"));
 		
 	}	
 	
 	@Override
	protected OsgiPlatform createPlatform() {
		// needed starting from Spring DM 2.0.0: Spring 3.0 annotation handlers needs some XSLT
		// and the default transformer factory implementation is resolved from the META-INF/services/jstl.osgi
		// the following forces the default implementation; which is in the JDK
		OsgiPlatform osgiPlatform = super.createPlatform();
		osgiPlatform.getConfigurationProperties().setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		return osgiPlatform;
	}
	
	@Override
 	protected String[] getTestBundlesNames() {
 		List<String> col = new ArrayList<String>();
 
 		// Servlet/JSP artifacts
 		col.add(SPRING_OSGI_GROUP + ", servlet-api.osgi, 2.4-SNAPSHOT");
 		col.add(SPRING_OSGI_GROUP + ", jsp-api.osgi, 2.0-SNAPSHOT");
 
 		// JSP compiler
 		col.add(SPRING_OSGI_GROUP + ", jasper.osgi, 5.5.23-SNAPSHOT");
 		col.add(SPRING_OSGI_GROUP + ", commons-el.osgi, 1.0-SNAPSHOT");
 
 		// standard tag library
 		col.add(SPRING_OSGI_GROUP+", jstl.osgi, 1.1.2-SNAPSHOT");
 
 		// web container
 		col.add(SPRING_OSGI_GROUP + ", catalina.osgi, 5.5.23-SNAPSHOT");
 		col.add(SPRING_OSGI_GROUP + ", catalina.start.osgi, 1.0.0");
 
 		// Spring DM web extender
 		col.add(SPRING_OSGI_GROUP + ", spring-osgi-web," + getSpringDMVersion());
 		col.add(SPRING_OSGI_GROUP + ", spring-osgi-web-extender," + getSpringDMVersion());
 		col.add("net.sourceforge.cglib, com.springsource.net.sf.cglib, 2.1.3");
 
 		// database access
 		col.add("com.manning.sdmia.ch06, commons-pool.osgi, 1.3.0");
 		col.add("com.manning.sdmia.ch06, commons-dbcp.osgi, 1.2.2");
 		col.add("com.h2database, h2, 1.1.115");
 		col.add("org.springframework, org.springframework.jdbc, "+getSpringVersion());
 		col.add("org.springframework, org.springframework.transaction, "+getSpringVersion());
 		
 		// Spring Web
 		col.add("org.springframework, org.springframework.web, "+getSpringVersion());
 		col.add("org.springframework, org.springframework.web.servlet, "+getSpringVersion());
		col.add("org.springframework, org.springframework.context.support, "+getSpringVersion());
 		
 		// the application
 		col.add("com.manning.sdmia.ch06, directory-domain, 1.0.0");
 		col.add("com.manning.sdmia.ch06, directory-service, 1.0.0");
 		col.add("com.manning.sdmia.ch06, directory-soa-service-impl, 1.0.0");
 		col.add("com.manning.sdmia.ch06, directory-web, 1.0.0");
 		
 		return (String[]) col.toArray(new String[col.size()]);
 	}
 	
 	@Override
 	protected String[] getConfigLocations() {
 		return new String [] {"/com/manning/sdmia/ch06/directory/web/DirectoryWebSoaTest-context.xml"};
 	}
 	
 	public void setDataSource(DataSource dataSource) {
 		this.dataSource = dataSource;
 	}
 	
 	@Override
 	protected boolean shouldWaitForSpringBundlesContextCreation() {	
 		// differs spring context creation for the test context to be included, so that
 		// that DAO impl bundle does not wait for datasource service
 		return false;
 	}
 	
 	private String getTextResponse(String address) throws Exception {
 		URL url = new URL(address);
 		HttpURLConnection con = (HttpURLConnection) url.openConnection();
 		con.setUseCaches(false);
 		BufferedReader in = null;
 		try {
 			con.connect();
 			assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
 			in = new BufferedReader(
                     new InputStreamReader(
                     con.getInputStream())
             );
 			StringBuilder builder = new StringBuilder();
 			String inputLine = null;
 			while((inputLine = in.readLine()) != null) {
 				builder.append(inputLine);
 			}
 			return builder.toString();
 		}
 		finally {
 			if(in != null) {
 				in.close();
 			}
 			con.disconnect();
 		}
 	}
 }
