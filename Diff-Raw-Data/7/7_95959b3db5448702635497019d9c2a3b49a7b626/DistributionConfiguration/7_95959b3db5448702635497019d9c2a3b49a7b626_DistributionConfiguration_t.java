 package org.glite.authz.pap.distribution;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.glite.authz.pap.common.PAP;
 import org.glite.authz.pap.common.PAPConfiguration;
 import org.glite.authz.pap.distribution.exceptions.AliasNotFoundException;
 import org.glite.authz.pap.distribution.exceptions.DistributionConfigurationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DistributionConfiguration {
 
     private static final String CONFIGURATION_STANZA = "distribution-configuration";
     private static DistributionConfiguration instance = null;
 
     private static final Logger log = LoggerFactory.getLogger(DistributionConfiguration.class);
     private static final String REMOTE_PAPS_STANZA = "remote-paps";
 
     public static DistributionConfiguration getInstance() {
         if (instance == null)
             instance = new DistributionConfiguration();
         return instance;
     }
 
     private static String aliasKey(String papAlias) {
         return REMOTE_PAPS_STANZA + "." + papAlias;
     }
     
     private static String dnKey(String papAlias) {
         return aliasKey(papAlias) + "." + "dn";
     }
 
     private static String hostnameKey(String papAlias) {
         return aliasKey(papAlias) + "." + "hostname";
     }
     
     private static String minKeyLengthKey() {
         return CONFIGURATION_STANZA + "." + "min-key-length";
     }
     
     private static String minKeyLengthKey(String papAlias) {
         return aliasKey(papAlias) + "." + "min-key-length";
     }
     
     private static String papOrderKey() {
         return CONFIGURATION_STANZA + "." + "pap-order";
     }
     
     private static String pathKey(String papAlias) {
         return aliasKey(papAlias) + "." + "path";
     }
 
     private static String pollIntervallKey() {
         return CONFIGURATION_STANZA + "." + "poll-interval";
     }
 
     private static String portKey(String papAlias) {
         return aliasKey(papAlias) + "." + "port";
     }
 
     private static String protocolKey(String papAlias) {
         return aliasKey(papAlias) + "." + "protocol";
     }
 
     private static String visibilityPublicKey(String papAlias) {
         return aliasKey(papAlias) + "." + "public";
     }
 
     private PAPConfiguration papConfiguration;
 
     public DistributionConfiguration() {
         papConfiguration = PAPConfiguration.instance();
     }
     
     public String[] getPAPOrderArray() throws AliasNotFoundException {
 
 		String[] papOrderArray = papConfiguration.getStringArray(papOrderKey());
 
 		if (papOrderArray == null)
 			papOrderArray = new String[0];
 
 		for (String alias : papOrderArray) {
 			if (!aliasExists(alias))
 				throw new DistributionConfigurationException(
 						"Undefined PAP alias \"" + alias + "\" in \"pap-order\"");
 		}
 
 		return papOrderArray;
 	}
 
     public long getPollIntervallInMilliSecs() {
 
         long pollIntervalInSecs = papConfiguration.getLong(pollIntervallKey());
         log.info("Polling interval for remote PAPs is set to: " + pollIntervalInSecs + " seconds");
 
         return pollIntervalInSecs * 1000;
 
     }
     
     public List<PAP> getRemotePAPList() {
 
     	List<String> aliasList = getAliasList();
     	if (aliasList.isEmpty())
     		log.info("No remote PAPs has been defined");
 
     	List<PAP> papList = new LinkedList<PAP>();
 
         for (String papAlias : aliasList) {
             PAP pap = getPAPFromProperties(papAlias);
             log.info("Adding remote PAP: " + pap);
             papList.add(pap);
         }
 
         return papList;
     }
     
     public void removePAP(String papAlias) {
 
 		String[] oldAliasOrderArray = getPAPOrderArray();
 		int newArraySize = oldAliasOrderArray.length - 1;
 		String[] newAliasOrderArray = new String[newArraySize];
 
 		for (int i = 0, j = 0; i < oldAliasOrderArray.length; i++) {
 
 			String aliasItem = oldAliasOrderArray[i];
 
 			if (!(aliasItem.equals(papAlias))) {
 				if (j < newArraySize) {
 					newAliasOrderArray[j] = aliasItem;
 					j++;
 				}
 			}
 		}
 
 		clearPAPProperties(papAlias);
 		savePAPOrder(newAliasOrderArray);
 		papConfiguration.saveStartupConfiguration();
 	}
     
     public void savePAP(PAP pap) {
         setPAPProperties(pap);
         papConfiguration.saveStartupConfiguration();
     }
     
     public void savePAPOrder(String[] aliasArray) throws AliasNotFoundException {
     	
 
         if (aliasArray == null) {
            papConfiguration.clearDistributionProperty(papOrderKey());
         	papConfiguration.saveStartupConfiguration();
         	return;
         }
         
         if (aliasArray.length == 0) {
            papConfiguration.clearDistributionProperty(papOrderKey());
         	papConfiguration.saveStartupConfiguration();
             return;
         }
         
         if (!aliasExists(aliasArray[0]))
         	throw new AliasNotFoundException("Unknown alias \"" + aliasArray[0] + "\"");
         
         StringBuilder sb = new StringBuilder(aliasArray[0]);
         
         for (int i=1; i<aliasArray.length; i++) {
         	
         	if (!aliasExists(aliasArray[i]))
             	throw new AliasNotFoundException("Unknown alias \"" + aliasArray[i] + "\"");
         	
             sb.append(", " + aliasArray[i]);
         }
         
         log.info("Setting new PAP order to: " + sb.toString());
         
        papConfiguration.clearDistributionProperty(papOrderKey());
         papConfiguration.setDistributionProperty(papOrderKey(), aliasArray);
         papConfiguration.saveStartupConfiguration();
     }
     
     private void clearPAPProperties(String papAlias) {
         papConfiguration.clearDistributionProperty(dnKey(papAlias));
         papConfiguration.clearDistributionProperty(hostnameKey(papAlias));
         papConfiguration.clearDistributionProperty(portKey(papAlias));
         papConfiguration.clearDistributionProperty(pathKey(papAlias));
         papConfiguration.clearDistributionProperty(protocolKey(papAlias));
         papConfiguration.clearDistributionProperty(visibilityPublicKey(papAlias));
     }
     
     private List<String> getAliasList() throws AliasNotFoundException {
 
 		Set<String> aliasSet = getAliasSet();
 
 		List<String> aliasList = new ArrayList<String>(aliasSet.size());
 
 		// get an ordered list of PAP aliases
 		String[] aliasOrderArray = getPAPOrderArray();
 		for (String alias : aliasOrderArray) {
 			if (aliasSet.remove(alias)) {
 				aliasList.add(alias);
 			} else {
 				throw new AliasNotFoundException("BUG alias not found: \"" + alias + "\"");
 			}
 		}
 
 		// order can be partially defined so get the remaining aliases
 		for (String alias : aliasSet) {
 			aliasList.add(alias);
 		}
 
 		return aliasList;
 	}
     
 	@SuppressWarnings("unchecked")
 	private Set<String> getAliasSet() {
     	
     	Set<String> aliasSet = new HashSet<String>();
 
     	Iterator iterator = papConfiguration.getKeys(REMOTE_PAPS_STANZA);
 		while (iterator.hasNext()) {
 			
 			String key = (String) iterator.next();
 			
 			int firstAliasChar = key.indexOf('.') + 1;
 			int lastAliasChar = key.indexOf('.', firstAliasChar);
 			
 			String alias = key.substring(firstAliasChar, lastAliasChar);
 			
 			aliasSet.add(alias);
 		}
 		
 		return aliasSet;
     }
     
     private PAP getPAPFromProperties(String papAlias) {
     	
     	String dn = papConfiguration.getString(dnKey(papAlias));
         if (dn == null) {
             throw new DistributionConfigurationException("DN is not set for remote PAP \""
                     + papAlias + "\"");
         }
 
         String hostname = papConfiguration.getString(hostnameKey(papAlias));
         if (hostname == null)
             throw new DistributionConfigurationException("Hostname is not set for remote PAP \""
                     + papAlias + "\"");
         
         String port = papConfiguration.getString(portKey(papAlias));
         String path = papConfiguration.getString(pathKey(papAlias));
         String protocol = papConfiguration.getString(protocolKey(papAlias));
         boolean visibilityPublic = papConfiguration.getBoolean(visibilityPublicKey(papAlias));
         
         // port, path and protocol can be null or empty
         return new PAP(papAlias, dn, hostname, port, path, protocol, visibilityPublic);
     }
     
     private boolean isEmpty(String s) {
     	if (s == null)
     		return true;
     	if (s.length() == 0)
     		return true;
     	return false;
     }
     
     private boolean aliasExists(String alias) {
     	
     	if (isEmpty(alias))
     		return false;
     	
     	String value = papConfiguration.getString(dnKey(alias));
     	
     	if (isEmpty(value))
     		return false;
     	
     	return true;
     }
     
     private void setPAPProperties(PAP pap) {
     	
     	String papAlias = pap.getAlias();
         
         papConfiguration.setDistributionProperty(dnKey(papAlias), pap.getDn());
         papConfiguration.setDistributionProperty(hostnameKey(papAlias), pap.getHostname());
         papConfiguration.setDistributionProperty(portKey(papAlias), pap.getPort());
         papConfiguration.setDistributionProperty(pathKey(papAlias), pap.getPath());
         papConfiguration.setDistributionProperty(protocolKey(papAlias), pap.getProtocol());
         papConfiguration.setDistributionProperty(visibilityPublicKey(papAlias), pap.isVisibilityPublic());
     }
     
 }
