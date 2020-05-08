 package org.lightmare.utils;
 
 import org.lightmare.config.Configuration;
 
 /**
  * Utility class for JNDI names
  * 
  * @author levan
  * 
  */
 public class NamingUtils {
 
     public static final String USER_TRANSACTION_NAME = "java:comp/UserTransaction";
 
     public static final String CONNECTION_NAME_PREF = "java:comp/env/";
 
     public static final String EJB_NAME_PREF = "ejb:";
 
     private static final String DS_JNDI_FREFIX = "java:/";
 
     private static final String EJB_NAME_DELIM = "\\";
 
     private static final String EJB_APP_DELIM = "!";
 
     private static final int BEAN_NAMES_INDEX = 1;
 
     private static final int INTERFACE_IDEX = 0;
 
     private static final int BEAN_INDEX = 1;
 
     // Error messages
    public static final String COULD_UNBIND_NAME = "Could not unbind jndi name %s cause %s";
 
     /**
      * Descriptor class which contains EJB bean class name and its interface
      * class name
      * 
      * @author levan
      * 
      */
     public static class BeanDescriptor {
 
 	private String beanName;
 
 	private String interfaceName;
 
 	public BeanDescriptor(String beanName, String interfaceName) {
 
 	    this.beanName = beanName;
 	    this.interfaceName = interfaceName;
 	}
 
 	public String getBeanName() {
 	    return beanName;
 	}
 
 	public void setBeanName(String beanName) {
 	    this.beanName = beanName;
 	}
 
 	public String getInterfaceName() {
 	    return interfaceName;
 	}
 
 	public void setInterfaceName(String interfaceName) {
 	    this.interfaceName = interfaceName;
 	}
     }
 
     /**
      * Creates JNDI name prefixes for EJB objects
      * 
      * @param jndiName
      * @return
      */
     public static String createJpaJndiName(String jndiName) {
 
 	return String.format("%s%s", Configuration.JPA_NAME, jndiName);
     }
 
     /**
      * Converts passed JNDI name to JPA name
      * 
      * @param jndiName
      * @return {@link String}
      */
     public static String formatJpaJndiName(String jndiName) {
 
 	String name = jndiName.replace(Configuration.JPA_NAME,
 		StringUtils.EMPTY_STRING);
 
 	return name;
     }
 
     /**
      * Creates EJB names from passed JNDI name
      * 
      * @param jndiName
      * @return {@link String}
      */
     public static String createEjbJndiName(String jndiName) {
 
 	return String.format("%s%s", Configuration.EJB_NAME, jndiName);
     }
 
     /**
      * Converts passed JNDI name to bean name
      * 
      * @param jndiName
      * @return {@link String}
      */
     public static String formatEjbJndiName(String jndiName) {
 
 	String name = jndiName.replace(Configuration.EJB_NAME,
 		StringUtils.EMPTY_STRING);
 
 	return name;
     }
 
     /**
      * Clears JNDI prefix "java:/" for data source name
      * 
      * @param jndiName
      * @return {@link String}
      */
     public static String clearDataSourceName(String jndiName) {
 
 	String clearName;
 	if (ObjectUtils.available(jndiName)
 		&& jndiName.startsWith(DS_JNDI_FREFIX)) {
 	    clearName = jndiName.replace(DS_JNDI_FREFIX,
 		    StringUtils.EMPTY_STRING);
 	} else {
 	    clearName = jndiName;
 	}
 
 	return clearName;
     }
 
     /**
      * Adds JNDI prefix "java:/" to data source name
      * 
      * @param clearName
      * @return {@link String}
      */
     public static String toJndiDataSourceName(String clearName) {
 
 	String jndiName;
 	if (ObjectUtils.available(clearName)
 		&& ObjectUtils.notTrue(clearName.contains(DS_JNDI_FREFIX))) {
 	    jndiName = StringUtils.concat(DS_JNDI_FREFIX, clearName);
 	} else {
 	    jndiName = clearName;
 	}
 
 	return jndiName;
     }
 
     /**
      * Parses bean JNDI name for lookup bean
      * 
      * @param jndiName
      * @return {@link BeanDescriptor}
      */
     public static BeanDescriptor parseEjbJndiName(String jndiName) {
 
 	String pureName = jndiName.substring(Configuration.EJB_NAME_LENGTH);
 	String[] formatedNames = pureName.split(EJB_NAME_DELIM);
 	String beanNames = formatedNames[BEAN_NAMES_INDEX];
 	String[] beanDescriptors = beanNames.split(EJB_APP_DELIM);
 
 	String interfaceName = beanDescriptors[INTERFACE_IDEX];
 	String beanName = beanDescriptors[BEAN_INDEX];
 
 	BeanDescriptor descriptor = new BeanDescriptor(beanName, interfaceName);
 
 	return descriptor;
     }
 }
