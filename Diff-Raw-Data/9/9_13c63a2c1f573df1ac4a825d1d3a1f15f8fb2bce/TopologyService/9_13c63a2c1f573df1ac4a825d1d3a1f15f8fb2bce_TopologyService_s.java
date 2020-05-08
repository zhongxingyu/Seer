 package amp.topology;
 
 import com.bazaarvoice.dropwizard.assets.ConfiguredAssetsBundle;
import com.berico.dropwizard.SpringService;
 import com.yammer.dropwizard.config.Bootstrap;
 
 public class TopologyService extends SpringService<TopologyConfiguration>
 {
     public static void main( String[] args ) throws Exception
     {
     		new TopologyService().run(args);
     }
     
 	@Override
 	public void initialize(Bootstrap<TopologyConfiguration> bootstrap) {
 		
 		super.initialize(bootstrap);
 		
 		bootstrap.setName("global-topology-service");
 		
 		bootstrap.addBundle(
 			new ConfiguredAssetsBundle("/assets/", "/"));
 	}
 }
