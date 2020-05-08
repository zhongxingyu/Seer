 package beans.config;
 
 import utils.Utils;
 
 import java.io.File;
 
 /**
  * User: guym
  * Date: 12/13/12
  * Time: 3:12 PM
  */
 public class ServerConfig {
 
     public PoolConfiguration pool = new PoolConfiguration();
 
     public BootstrapConfiguration bootstrap = new BootstrapConfiguration();
 
     public DefaultAdmin admin = new DefaultAdmin();
 
     @Config( ignoreNullValues = true )
     public long sessionTimeoutMillis = Utils.parseTimeToMillis( "15mn" );
 
     public static class PoolConfiguration{
         @Config( ignoreNullValues = true )
         public boolean coldInit = false ;
         @Config( ignoreNullValues = true )
         public int minNode = 2;
         @Config( ignoreNullValues = true )
         public int maxNodes = 5;
         @Config( ignoreNullValues = true )
         public long expirationTimeMillis = Utils.parseTimeToMillis("60mn");
 
     }
 
     public static class BootstrapConfiguration{
         public String serverNamePrefix="cloudify_pool_server";
         public String zoneName="az-1.region-a.geo-l";
         public String keyPair="cloudify";
         public String securityGroup="default";
         public String flavorId="102";
         public String imageId="1358";
         public SshConfiguration ssh = new SshConfiguration();
         public String apiKey="<HP cloud Password>";
         public String username="<tenant>:<user>";
         public String cloudProvider="hpcloud-compute";
         public File script;
     }
 
     public static class SshConfiguration{
         public String user="root";
         @Config( ignoreNullValues = true )
         public int port=22;
        public File privateKey= Utils.getFileByRelativePath( "/bin/hpcloud.pem" );
     }
 
     public static class DefaultAdmin{
         public String username = "admin@cloudifysource.org";
         public String password = "admin1324";
     }
 }
