 import java.security.MessageDigest;
 import java.sql.Connection;
 import java.util.Date;
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import org.askil.eshift.user.User;
 import org.askil.eshift.user.UserRole;
 import org.askil.eshift.user.UserRoleEnum;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.tool.hbm2ddl.SchemaExport;
 import org.hibernate.tool.hbm2ddl.Target;
 import org.jboss.util.Base64;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
 
 /**
  * Created by IntelliJ IDEA.
  * User: rgaskill
  * Date: 12/9/11
  * Time: 12:32 PM
  */
 public class CreateSchema {
     
     public static final void main(String[] args){
         ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/Config.xml");
         ctx.start();
         
         DataSource ds = (DataSource) ctx.getBean("myDataSource");
         LocalSessionFactoryBuilder config = new LocalSessionFactoryBuilder(ds);
        config.addPackage("com.askill");
        config.scanPackages("com.askill");
         Properties hibernateProperties = new Properties();
        
         /*
          * test 3
          */
         
         hibernateProperties.setProperty("hibernate.dialect","org.hibernate.dialect.HSQLDialect");
         hibernateProperties.setProperty("hibernate.show_sql","true");
         config.addProperties(hibernateProperties);
         
         try {
         	Connection con = ds.getConnection();
         	con.prepareStatement("drop schema eshift if exists cascade").execute();
         	con.prepareStatement("create schema eshift authorization sa").execute();
 			SchemaExport export = new SchemaExport(config, con);
 			export.create(Target.BOTH);
 			con.commit();
 			con.close();
 			
 			SessionFactory sf = (SessionFactory) ctx.getBean("mySessionFactory");
 			Session session = sf.openSession();
 			
 			User adminUser = new User();
 			adminUser.setEmail("roarke.gaskill@gmail.com");
 			adminUser.setFirstName("Roarke");
 			adminUser.setLastName("Gaskill");
 			MessageDigest md5 = MessageDigest.getInstance("MD5");
 			String password = "password";
 			md5.update(password.getBytes());
 			String hashword = Base64.encodeBytes(md5.digest());
 			adminUser.setPassword(hashword);
 			adminUser.setCtrlDate(new Date());
 			adminUser.setCtrlUser("system");
 			
 			UserRole role = new UserRole();
 			role.setUserRole(UserRoleEnum.admin.toString());
 			
 //			HashSet<UserRole> userRoles = new HashSet<>();
 //			userRoles.add(role);
 //			adminUser.setUserRoles(userRoles);
 			
 			session.save(role);
 			
 			adminUser.getUserRoles().add(role);
 			session.save(adminUser);
 			session.flush();
 			
 			session.close();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 //        System.out.println(user);
     }
     
 }
