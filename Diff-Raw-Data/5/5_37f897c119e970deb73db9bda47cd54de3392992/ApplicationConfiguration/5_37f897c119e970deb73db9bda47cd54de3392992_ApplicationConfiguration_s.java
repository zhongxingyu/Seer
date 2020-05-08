 package ed.demo.c3p0;
 
 import java.sql.SQLException;
 
 import javax.sql.DataSource;
 
 import org.hibernate.SessionFactory;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
 import org.springframework.orm.hibernate4.HibernateTransactionManager;
 import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import com.mchange.v2.c3p0.DataSources;
 
 @Configuration
@ComponentScan({"ed.demo.c3p0.dao"})
 @EnableTransactionManagement
 public class ApplicationConfiguration {
 	@Bean
 	public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
 		HibernateTransactionManager htm = new HibernateTransactionManager();
 		htm.setSessionFactory(sessionFactory);
 		return htm;
 	}
 
 	@Bean
 	public LocalSessionFactoryBean sessionFactory(DataSource pooledDatabase) {
 		LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean();
 		lsfb.setDataSource(pooledDatabase);
		lsfb.setPackagesToScan(new String[] {"ed.demo.c3p0.*"});
 		return lsfb;
 	}
 
 	@Bean(destroyMethod = "close")
 	public DataSource pooledDatabase(DataSource database) throws SQLException {
 		return DataSources.pooledDataSource(database);
 	}
 
 	@Bean(destroyMethod = "shutdown")
 	public DataSource database() {
 		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
 		EmbeddedDatabase db = builder.setType(EmbeddedDatabaseType.HSQL).setName("Demo").addScript("create-schemas.sql").build();
 		return db;
 	}
 }
