 package nu.wasis.stunden.plugins.tagentries;
 
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import nu.wasis.stunden.exception.InvalidConfigurationException;
 import nu.wasis.stunden.model.Day;
 import nu.wasis.stunden.model.Entry;
 import nu.wasis.stunden.model.WorkPeriod;
 import nu.wasis.stunden.plugin.ProcessPlugin;
 import nu.wasis.stunden.plugins.tagentries.config.StundenTagEntriesPluginConfiguration;
 
import org.apache.log4j.Logger;

 @PluginImplementation
 public class StundenTagEntriesPlugin implements ProcessPlugin {
 
 	private static final Logger LOG = Logger.getLogger(StundenTagEntriesPlugin.class);
 	
 	@Override
 	public WorkPeriod process(final WorkPeriod workPeriod, final Object configuration) {
 		if (null == configuration || !(configuration instanceof StundenTagEntriesPluginConfiguration)) {
 			throw new InvalidConfigurationException("Configuration null or wrong type. You probably need to fix your configuration file.");
 		}
 		
 		final StundenTagEntriesPluginConfiguration myConfig = (StundenTagEntriesPluginConfiguration) configuration;
 		
 		for (final Day day : workPeriod.getDays()) {
 			for (final Entry entry : day.getEntries()) {
 				final String projectName = entry.getProject().getName().toLowerCase();
 				for (final java.util.Map.Entry<String, String> association : myConfig.getTagAssociations().entrySet()) {
 					if (projectName.contains(association.getKey().toLowerCase())) {
 						LOG.debug("Tagging " + entry + " with `" + association.getValue() + "'.");
 						entry.getTags().add(association.getValue());
 					}
 				}
 			}
 		}
 		return workPeriod;
 	}
 
 	@Override
 	public Class<?> getConfigurationClass() {
 		return StundenTagEntriesPluginConfiguration.class;
 	}
 
 }
