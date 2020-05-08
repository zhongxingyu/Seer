 package amp.topology;
 
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 
 //import org.hibernate.validator.constraints.NotEmpty;
 
 import com.bazaarvoice.dropwizard.assets.AssetsBundleConfiguration;
 import com.bazaarvoice.dropwizard.assets.AssetsConfiguration;
import com.berico.fallwizard.SpringConfiguration;
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 public class TopologyConfiguration extends SpringConfiguration implements AssetsBundleConfiguration {
 
 	@Valid
 	@NotNull
 	@JsonProperty
 	private final AssetsConfiguration assets = new AssetsConfiguration();
 
 	@Override
 	public AssetsConfiguration getAssetsConfiguration() {
 		return assets;
 	}
 }
