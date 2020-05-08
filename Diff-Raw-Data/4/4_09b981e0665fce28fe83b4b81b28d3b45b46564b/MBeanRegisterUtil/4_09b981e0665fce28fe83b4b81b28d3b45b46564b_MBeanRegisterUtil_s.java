 package org.configureme.mbean.util;
 
 import net.anotheria.util.log.LogMessageUtil;
 import org.apache.log4j.Logger;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 import java.lang.management.ManagementFactory;
 
 /**
  * Util for register mBeans in {@link MBeanServer}.
  *
  * @author asamoilich
  */
 public final class MBeanRegisterUtil {
 	/**
 	 * Logger util.
 	 */
 	private static final Logger log = Logger.getLogger(MBeanRegisterUtil.class);
 	/**
 	 * {@link MBeanServer} server.
 	 */
 	private static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
 
 	/**
 	 * Register mBean object in {@link MBeanServer}.
 	 *
 	 * @param object     provided object
 	 * @param parameters additional parameters
 	 */
 	public static void regMBean(final Object object, final String... parameters) {
 		try {
 			final String name = buildObjectName(object, parameters);
 			ObjectName objectName = new ObjectName(name);
 			if (mbs.isRegistered(objectName))
 				return;
 			mbs.registerMBean(object, objectName);
 		} catch (MalformedObjectNameException e) {
 			log.error(LogMessageUtil.failMsg(e, object));
 		} catch (InstanceAlreadyExistsException e) {
 			log.error(LogMessageUtil.failMsg(e, object, object));
 		} catch (MBeanRegistrationException e) {
 			log.error(LogMessageUtil.failMsg(e, object, object));
 		} catch (NotCompliantMBeanException e) {
 			log.error(LogMessageUtil.failMsg(e, object, object));
 		}
 	}
 
 	/**
 	 * Return object name with which will be register in {@link MBeanServer}.
 	 *
 	 * @param object     provided object
 	 * @param parameters additional parameters
 	 * @return object name
 	 */
 	private static String buildObjectName(final Object object, final String... parameters) {
 		StringBuilder objectName = new StringBuilder();
 		objectName.append(object.getClass().getPackage().getName());
 		objectName.append(":type=");
 		objectName.append(object.getClass().getName());
 		if (parameters.length > 0) {
 			objectName.append("(");
 			for (String parameter : parameters)
 				objectName.append(parameter).append(",");
 			objectName.deleteCharAt(objectName.length() - 1);
 			objectName.append(")");
 		}
 		return objectName.toString();
 	}
 
 	/**
 	 * Private constructor.
 	 */
 	private MBeanRegisterUtil() {
 		throw new IllegalAccessError("Can't be initialise");
 	}
 }
