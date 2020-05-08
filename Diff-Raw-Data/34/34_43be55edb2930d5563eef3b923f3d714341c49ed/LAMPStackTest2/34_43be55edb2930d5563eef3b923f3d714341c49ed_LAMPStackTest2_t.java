 package gears;
 
 import static org.junit.Assert.*;
 import gears.base.Gear;
 import gears.base.Instance;
 import gears.base.Configuration;
 import gears.base.template.Templaton;
 
 import org.apache.velocity.VelocityContext;
 import org.junit.Test;
 
 
 public class LAMPStackTest2 {
 
 	public static String TEST_RESOURCES = "src/test/java/resources/";
 	public static String HOSTS = TEST_RESOURCES + "hosts.yaml";
 	public static String INFO = TEST_RESOURCES + "info.php.vm";
 	
 	
 	class PHPApp extends Gear {
 		
 		@Override
 		public void execute() {
 			// Update application repository
 			update();
 
 			// Install misc apps
 			install("-y", "php5");
 		}
 		    
 	}
 	
 	class ApacheApp extends Gear {
 		
 		@Override
 		public void execute() {
 			// Update application repository
 			update();
 			
 			// Install misc apps
 			install( "-y", "apache2 libapache2-mod-php5 php5-mcrypt" );
 	
 			// Restart Apache service, equals to "service apache2 restart"
 			restart("apache2");
 		}
 
 	}
 	
 	class MySQLApp extends Gear {
 		
 		/**
 		 * For MySQL config file
 		 * http://stackoverflow.com/questions/1167056/optimal-mysql-configuration-my-cnf
 		 */
 		public static final String MYSQL_PASS   = "mypass";
 		public static final String MYSQL_USER   = "root";
 		
 		public static final String INNODB_BUFFER_POOL_SIZE = "512M";
 		public static final String INNODB_LOG_FILE_SIZE = "128M";
 
 		@Override
 		public void execute() {
 			update();
 			
 			// Hack for automating Mysql install
 			command(String.format("echo mysql-server-5.5 mysql-server/root_password password %s | debconf-set-selections", MYSQL_PASS));
 			command(String.format("echo mysql-server-5.5 mysql-server/root_password_again password %s | debconf-set-selections", MYSQL_PASS));
 			
 			// Install misc apps
 			install( "-y", "mysql-server php5-mysql php5 php5-mcrypt" );
 		}
 
 		private void renderConfig(){
 			VelocityContext context = Templaton.getContext();
         	context.put("MYSQL_PASS", MYSQL_PASS);
         	context.put("MYSQL_USER", MYSQL_USER );
 			render(INFO, "/var/www/info.php", context);
 		}
 		    
 	}
 	
 	
 	
 	class LAMPStackServer extends Gear {
 		
 		Gear mysql  = new MySQLApp();
 		Gear php    = new PHPApp();
 		Gear apache = new ApacheApp();
 		
 		public LAMPStackServer() {
 			setConfig(new ProductionLAMP());
 		}
 
 		@Override
 		public void execute() {
 			install("web", php);
 			install("web", apache);
//			install("db", mysql);
 		}
 		
 		
 		class ProductionLAMP extends Configuration {
 			 
			private final static String SSH_KEY = "keys/id_rsa";
 			
 			public ProductionLAMP() {
				Instance instance1 = new 	Instance("10.211.55.100", SSH_KEY);
 				addInstance("web", instance1);
 				
 				Instance instance2 = new Instance("10.211.55.101", SSH_KEY);
 				addInstance("web", instance2);
 			}
 		}
 
 	}
 	
 	@Test
 	public void testLAMPServer(){
 		Gear server   = new LAMPStackServer();
 		server.execute();
 	}
 
 	
 
 }
