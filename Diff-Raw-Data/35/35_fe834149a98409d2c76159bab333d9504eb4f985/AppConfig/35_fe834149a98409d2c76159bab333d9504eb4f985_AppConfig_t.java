 package name.felixbecker.brokenaop.configuration;
 
 import javax.sql.DataSource;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.ComponentScan.Filter;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.EnableAspectJAutoProxy;
 import org.springframework.context.annotation.FilterType;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.Database;
 import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 @Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
 @EnableTransactionManagement
 @ComponentScan(basePackages = "name.felixbecker.brokenaop", excludeFilters = @Filter(type = FilterType.ANNOTATION, value = Configuration.class))
 public class AppConfig {
 	
 	@Bean
 	public LocalContainerEntityManagerFactoryBean entityManagerFactory(){
 		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
 		vendorAdapter.setDatabase(Database.H2);
 		vendorAdapter.setGenerateDdl(true);
 		
 		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
 		factory.setJpaVendorAdapter(vendorAdapter);
 		factory.setPackagesToScan("name.felixbecker.brokenaop.entities");
 		factory.setDataSource(dataSource());
 		
 		return factory;
 	}
 	
 	@Bean
 	public DataSource dataSource() {
 		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
 	}
 	
 	@Bean
 	public PlatformTransactionManager txManager() {
 		return new JpaTransactionManager();
 	}
 }
