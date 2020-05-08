 package org.glite.authz.pap.distribution;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.glite.authz.pap.common.PAPConfiguration;
 import org.glite.authz.pap.common.Pap;
 import org.glite.authz.pap.common.utils.Utils;
 import org.glite.authz.pap.papmanagement.PapManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class has methods to read distribution information from configuration and to write back
  * these information to the configuration file. The reason for having both read and write facilities
  * is that these information can be modified through the command line.
  * 
  * @see PapManager
  * @see Pap
  */
 public class DistributionConfiguration {
 
     private static final Logger log = LoggerFactory.getLogger(DistributionConfiguration.class);
 
     private static final String PAPS_STANZA = "paps";
     private static final String PAPS_PROPERTIES_STANZA = "paps:properties";
 
     private static DistributionConfiguration instance = null;
     private PAPConfiguration papConfiguration;
 
     private DistributionConfiguration() {
         papConfiguration = PAPConfiguration.instance();
     }
 
     public static DistributionConfiguration getInstance() {
         if (instance == null)
             instance = new DistributionConfiguration();
         return instance;
     }
 
     /**
      * Returns the <code>alias</code> key part (INI configuration) of a pap. This is the first part
      * common to all the keys associated to a pap.
      * 
      * @param papAlias alias of the pap.
      * @return the key build as {@link DistributionConfiguration#PAPS_STANZA} + "." +
      *         <i>papAlias</i>
      */
     private static String aliasKey(String papAlias) {
         return PAPS_STANZA + "." + papAlias;
     }
 
     /**
      * Returns the <i>dn</i> key of a pap.
      * 
      * @param papAlias alias if the pap.
      * @return the <i>dn</i> key.
      */
     private static String dnKey(String papAlias) {
         return aliasKey(papAlias) + "." + "dn";
     }
     
     /**
      * Returns the <i>enabled</i> key of a pap.
      * 
      * @param papAlias alias if the pap.
      * @return the <i>enabled</i> key.
      */
     private static String enabledKey(String papAlias) {
         return aliasKey(papAlias) + "." + "enabled";
     }
 
     /**
      * Returns the <i>hostname</i> key of a pap.
      * 
      * @param papAlias alias if the pap.
      * @return the <i>hostname</i> key.
      */
     private static String hostnameKey(String papAlias) {
         return aliasKey(papAlias) + "." + "hostname";
     }
 
     /**
      * Returns the <i>minKeyLength</i> key for all the paps.
      * 
      * @return the <i>minKeyLength</i> key.
      */
     @SuppressWarnings("unused")
     private static String minKeyLengthKey() {
         return PAPS_STANZA + "." + "min_key_length";
     }
 
     /**
      * Returns the <i>minKeyLength</i> key of a pap.
      * 
      * @param papAlias alias if the pap.
      * @return the <i>minKeyLength</i> key.
      */
     @SuppressWarnings("unused")
     private static String minKeyLengthKey(String papAlias) {
         return aliasKey(papAlias) + "." + "min_key_length";
     }
 
     /**
      * Returns the <i>papOrdering</i> key.
      * 
      * @return the <i>papOrdering</i> key.
      */
     private static String papOrderingKey() {
         return PAPS_PROPERTIES_STANZA + "." + "ordering";
     }
 
     /**
      * Returns the <i>pathKey</i> key of a pap.
      * 
      * @param papAlias alias if the pap.
      * @return the <i>pathKey</i> key.
      */
     private static String pathKey(String papAlias) {
         return aliasKey(papAlias) + "." + "path";
     }
 
     /**
      * Returns the <i>pollInterval</i> key.
      * 
      * @return the <i>pollIntervall</i> key.
      */
     private static String pollIntervallKey() {
         return PAPS_PROPERTIES_STANZA + "." + "poll_interval";
     }
 
     /**
      * Returns the <i>port</i> key of a pap.
      * 
      * @param papAlias alias if the pap.
      * @return the <i>port</i> key.
      */
     private static String portKey(String papAlias) {
         return aliasKey(papAlias) + "." + "port";
     }
 
     /**
      * Returns the <i>protocol</i> key of a pap.
      * 
      * @param papAlias alias if the pap.
      * @return the <i>protocol</i> key.
      */
     private static String protocolKey(String papAlias) {
         return aliasKey(papAlias) + "." + "protocol";
     }
 
     /**
      * Returns the <i>type</i> key of a pap.
      * 
      * @param papAlias alias if the pap.
      * @return the <i>type</i> key.
      */
     private static String typeKey(String papAlias) {
         return aliasKey(papAlias) + "." + "type";
     }
 
     /**
      * Returns the <i>visibilityPublic</i> key of a pap.
      * 
      * @param papAlias alias if the pap.
      * @return the <i>visibilityPublic</i> key.
      */
     private static String visibilityPublicKey(String papAlias) {
         return aliasKey(papAlias) + "." + "public";
     }
 
     /**
      * Reads the list of defined paps from configuration. Paps are read following without a specific
      * order, there the returned list does <b>not</b> follow the paps ordering.
      * 
      * @return list of paps defined in the configuration (not ordered).
      * 
      * @throws DistributionConfigurationException if a configuration error was found (the reason in
      *             the exception message).
      */
     @SuppressWarnings("unchecked")
     public List<Pap> getPapList() {
 
         Set<String> aliasSet = Utils.getAliasSet((Iterator<String>) papConfiguration.getKeys(PAPS_STANZA));
 
         List<Pap> papList = new ArrayList<Pap>(aliasSet.size());
 
         for (String papAlias : aliasSet) {
 
             if (Pap.DEFAULT_PAP_ALIAS.equals(papAlias)) {
                 continue;
             }
 
             papList.add(getPapFromProperties(papAlias));
         }
         return papList;
     }
 
     /**
      * Reads the pap ordering (list of aliases) from the configuration.
      * 
      * @return an array of aliases (can be empty, i.e. no ordering defined).
      * 
      * @throws DistributionConfigurationException if the ordering contains an undefined alias and/or
      *             duplicated aliases (the reason is in the exception message).
      */
     public String[] getPapOrdering() {
 
         String[] papOrderingArray = papConfiguration.getStringArray(papOrderingKey());
 
         if (papOrderingArray == null) {
             papOrderingArray = new String[0];
         }
 
         validatePapOrdering(papOrderingArray);
 
         return papOrderingArray;
     }
 
     /**
      * Reads the <i>polling interval</i> from the configuration.
      * 
      * @return the <i>polling interval</i> in seconds.
      */
     public long getPollIntervall() {
 
         long pollIntervalInSecs = papConfiguration.getLong(pollIntervallKey());
 
         log.info("Polling interval for remote paps is set to: " + pollIntervalInSecs + " seconds");
 
         return pollIntervalInSecs;
     }
     
     /**
      * Set the polling interval into configuration.
      * 
      * @param seconds new polling interval in seconds
      */
     public void savePollInterval(long seconds) {
         papConfiguration.clearDistributionProperty(pollIntervallKey());
         papConfiguration.setDistributionProperty(pollIntervallKey(), seconds);
         papConfiguration.saveStartupConfiguration();
     }
 
     /**
      * Removes a pap from the configuration.
      * 
      * @param papAlias alias of the pap to remove.
      */
     public void removePap(String papAlias) {
 
         String[] oldPapOrdering = getPapOrdering();
 
         clearPapProperties(papAlias);
 
         int newArraySize = oldPapOrdering.length - 1;
 
         if (newArraySize >= 0) {
             String[] newPapOrdering = new String[newArraySize];
 
             for (int i = 0, j = 0; i < oldPapOrdering.length; i++) {
 
                 String aliasItem = oldPapOrdering[i];
 
                 if (!(aliasItem.equals(papAlias))) {
                     if (j < newArraySize) {
                         newPapOrdering[j] = aliasItem;
                         j++;
                     }
                 }
             }
             savePapOrdering(newPapOrdering);
         }
 
         papConfiguration.saveStartupConfiguration();
     }
 
     /**
      * Saves a pap into configuration.
      * <p>
      * If the pap already exists its information are updated with the new one.
      * 
      * @param pap the pap to be saved.
      */
     public void savePap(Pap pap) {
         setPapProperties(pap);
         papConfiguration.saveStartupConfiguration();
     }
    
     /**
      * Save the given paps ordering into configuration.
      * <p>
      * If the given array is <code>null</code> or <code>empty</code> the previous ordering (if any)
      * is cleared (i.e. no ordering defined).
      * 
      * @param aliasArray the array of aliases identifying the new ordering (can be <code>null</code>
      *            or <code>empty</code>).
      * 
      * @throws DistributionConfigurationException if the new ordering contains duplicated or unknown
      *             aliases.
      */
     public void savePapOrdering(String[] aliasArray) {
 
         if (aliasArray == null) {
             papConfiguration.clearDistributionProperty(papOrderingKey());
             papConfiguration.saveStartupConfiguration();
             return;
         }
 
         validatePapOrdering(aliasArray);
 
         if (aliasArray.length == 0) {
             papConfiguration.clearDistributionProperty(papOrderingKey());
             papConfiguration.saveStartupConfiguration();
             return;
         }
 
         StringBuilder sb = new StringBuilder(aliasArray[0]);
 
         for (int i = 1; i < aliasArray.length; i++) {
             sb.append(", " + aliasArray[i]);
         }
 
         log.info("Setting new paps ordering to: " + sb.toString());
 
         papConfiguration.clearDistributionProperty(papOrderingKey());
         papConfiguration.setDistributionProperty(papOrderingKey(), aliasArray);
         papConfiguration.saveStartupConfiguration();
     }
 
     /**
      * Checks for the existence of an alias in the configuration.
      * 
      * @param alias the alias to check.
      * @return <code>true</code> if the alias exists in the configuration, <code>false</code>
      *         otherwise.
      */
     private boolean aliasExists(String alias) {
 
         if (!Utils.isDefined(alias)) {
             return false;
         }
 
         if (Pap.DEFAULT_PAP_ALIAS.equals(alias)) {
             return true;
         }
 
         String value = papConfiguration.getString(typeKey(alias));
 
         if (!Utils.isDefined(value)) {
             return false;
         }
 
         return true;
     }
 
     /**
      * Clears the properites associated to a pap.
      * 
      * @param papAlias alias of the pap to clear.
      */
     private void clearPapProperties(String papAlias) {
         papConfiguration.clearDistributionProperty(dnKey(papAlias));
         papConfiguration.clearDistributionProperty(hostnameKey(papAlias));
         papConfiguration.clearDistributionProperty(enabledKey(papAlias));
         papConfiguration.clearDistributionProperty(portKey(papAlias));
         papConfiguration.clearDistributionProperty(pathKey(papAlias));
         papConfiguration.clearDistributionProperty(protocolKey(papAlias));
         papConfiguration.clearDistributionProperty(visibilityPublicKey(papAlias));
         papConfiguration.clearDistributionProperty(typeKey(papAlias));
     }
 
     /**
      * Returns a <code>Pap</code> reading it from configuration.
      * 
      * @param papAlias alias of the pap to get.
      * @return the retrieved <code>Pap</code>.
      * 
      * @throws DistributionConfigurationException if there is some configuration error like missing
      *             required information (specific reason is put in the exception message).
      */
     private Pap getPapFromProperties(String papAlias) {
 
         String type = papConfiguration.getString(typeKey(papAlias));
         if (type == null) {
             throw new DistributionConfigurationException("\"type\" is not set for remote pap \"" + papAlias
                     + "\"");
         }
 
         boolean isLocal = Pap.isLocal(type);
 
         String dn = papConfiguration.getString(dnKey(papAlias));
         String hostname = papConfiguration.getString(hostnameKey(papAlias));
 
         if ((hostname == null) && (!isLocal)) {
             throw new DistributionConfigurationException("\"hostname\" is not set for remote pap \""
                     + papAlias + "\"");
         }
 
         String port = papConfiguration.getString(portKey(papAlias));
         String path = papConfiguration.getString(pathKey(papAlias));
         String protocol = papConfiguration.getString(protocolKey(papAlias));
         boolean visibilityPublic = papConfiguration.getBoolean(visibilityPublicKey(papAlias));
         boolean enabled = papConfiguration.getBoolean(enabledKey(papAlias), false);
 
         Pap pap = new Pap(papAlias, isLocal, dn, hostname, port, path, protocol, visibilityPublic);
         pap.setEnabled(enabled);
         
         return pap; 
     }
 
     /**
      * Set the properties of a pap into configuration.
      * 
      * @param pap the pap to set the properties for.
      */
     private void setPapProperties(Pap pap) {
 
         String papAlias = pap.getAlias();
 
         papConfiguration.setDistributionProperty(typeKey(papAlias), pap.getTypeAsString());
         papConfiguration.setDistributionProperty(enabledKey(papAlias), pap.isEnabled());
         papConfiguration.setDistributionProperty(dnKey(papAlias), pap.getDn());
         papConfiguration.setDistributionProperty(hostnameKey(papAlias), pap.getHostname());
         papConfiguration.setDistributionProperty(portKey(papAlias), pap.getPort());
         papConfiguration.setDistributionProperty(pathKey(papAlias), pap.getPath());
         papConfiguration.setDistributionProperty(protocolKey(papAlias), pap.getProtocol());
         papConfiguration.setDistributionProperty(visibilityPublicKey(papAlias), pap.isVisibilityPublic());
     }
 
     /**
      * checks if the given alias array is a valid pap ordering.
      * @param aliasArray array of aliases to be validated.
      * 
      * @throws DistributionConfigurationException if there are unknown or duplicated aliases.
      */
     private void validatePapOrdering(String[] aliasArray) {
 
         if (aliasArray == null) {
             throw new DistributionConfigurationException("aliasArray is null");
         }
 
         Set<String> aliasSet = new HashSet<String>(aliasArray.length);
 
         for (String alias : aliasArray) {
 
             if (aliasSet.contains(alias)) {
                 throw new DistributionConfigurationException(String.format("Error in remote paps order: alias \"%s\" appears more than one time",
                                                                            alias));
             }
 
             aliasSet.add(alias);
 
             if (!aliasExists(alias)) {
                 throw new DistributionConfigurationException(String.format("Error in remote paps order: unknown alias \"%s\"",
                                                                            alias));
             }
         }
     }
 }
