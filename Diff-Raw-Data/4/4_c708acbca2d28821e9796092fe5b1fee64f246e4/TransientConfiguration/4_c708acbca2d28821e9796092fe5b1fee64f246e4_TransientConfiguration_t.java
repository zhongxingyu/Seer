 package gov.nih.nci.cabig.ctms.tools.configuration;
 
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * {@link Configuration} implementation which does not persist changes
  * on {@link #set}.  Also provides methods for copying into and out of
  * another (presumably more permanent) {@link Configuration}.
  *
  * @author Rhett Sutphin
  */
 public class TransientConfiguration extends AbstractConfiguration {
     private Map<String, ConfigurationEntry> entries;
     private ConfigurationProperties properties;
 
     public TransientConfiguration(ConfigurationProperties properties) {
         this.properties = properties;
         entries = new HashMap<String, ConfigurationEntry>();
     }
 
     public ConfigurationProperties getProperties() {
         return properties;
     }
 
     /**
      * Create a transient copy of the source configuration.
      * @param source
      */
    public static Configuration create(Configuration source) {
         TransientConfiguration copy = new TransientConfiguration(source.getProperties());
         copy.copyFrom(source);
        return copy;
     }
 
     public void copyFrom(Configuration source) {
         for (ConfigurationProperty<?> property : source.getProperties().getAll()) {
             if (source.isSet(property)) {
                 this.set((ConfigurationProperty<Object>) property, source.get(property));
             }
         }
     }
 
     /**
      * Copies values from this configuration to the target.  The list of properties
      * to copy is taken from the target.
      */
     public void copyTo(Configuration target) {
         for (ConfigurationProperty<?> property : target.getProperties().getAll()) {
             if (target.isSet(property)) {
                 target.set((ConfigurationProperty<Object>) property, this.get(property));
             } else {
                 target.reset(property);
             }
         }
     }
 
     @Override
     protected <V> ConfigurationEntry getEntry(ConfigurationProperty<V> property) {
         return entries.get(property.getKey());
     }
 
     @Override
     protected void store(ConfigurationEntry entry) {
         entries.put(entry.getKey(), entry);
     }
 
     @Override
     protected void remove(ConfigurationEntry entry) {
         entries.remove(entry.getKey());
     }
 }
