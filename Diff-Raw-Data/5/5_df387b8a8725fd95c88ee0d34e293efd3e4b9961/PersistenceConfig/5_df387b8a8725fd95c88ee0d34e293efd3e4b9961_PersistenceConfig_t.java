 package mdettlaff.cloudreader.persistence;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
 
import com.google.common.base.Objects;

 @Configuration
 @PropertySource("classpath:/jdbc.properties")
 public class PersistenceConfig {
 
 	private @Value("${jdbc.databaseUrl}") String databaseUrl;
 	private @Value("${jdbc.driverClassName}") String driverClassName;
 
 	@Autowired
 	private DataSource dataSource;
 
 	@Bean
 	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
 		return new PropertySourcesPlaceholderConfigurer();
 	}
 
 	@Bean
 	public JpaTransactionManager transactionManager() throws Exception {
 		return new JpaTransactionManager(entityManagerFactory().getObject());
 	}
 
 	@Bean
 	public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws Exception {
 		LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
 		result.setDataSource(dataSource);
 		result.setPackagesToScan("mdettlaff.cloudreader.domain");
 		result.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
 		return result;
 	}
 
 	@Bean
 	public DataSource dataSource() throws URISyntaxException {
		URI uri = new URI(Objects.firstNonNull(System.getenv("DATABASE_URL"), databaseUrl));
 		BasicDataSource dataSource = new BasicDataSource();
 		dataSource.setDriverClassName(driverClassName);
 		dataSource.setUrl("jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath());
 		dataSource.setUsername(uri.getUserInfo().split(":")[0]);
 		dataSource.setPassword(uri.getUserInfo().split(":")[1]);
 		return dataSource;
 	}
 }
