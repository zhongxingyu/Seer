 package org.jboss.jmx.adaptor.snmp.deployer;
 
 import javax.management.MBeanServer;
 import javax.management.MBeanServerInvocationHandler;
 
 import org.apache.log4j.Logger;
 import org.jboss.deployers.spi.DeploymentException;
 import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
 import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
 import org.jboss.jmx.adaptor.snmp.agent.SnmpAgentServiceMBean;
 import org.jboss.jmx.adaptor.snmp.deployer.metadata.SnmpMetaData;
 import org.jboss.mx.util.MBeanServerLocator;
 
 /**
  * 
  * @author jean.deruelle@gmail.com
  */
 public class SnmpFilesDeployer extends  AbstractSimpleVFSRealDeployer<SnmpMetaData> {
 	private static Logger logger = Logger.getLogger(SnmpFilesDeployer.class);
 	
 	public SnmpFilesDeployer() {
 		super(SnmpMetaData.class);
 	}
 	
 	
 	
 	@Override
 	public void deploy(VFSDeploymentUnit unit, SnmpMetaData snmp)
 			throws DeploymentException {
 		if(snmp != null) {
 			MBeanServer server = MBeanServerLocator.locateJBoss();	
 			try {
 				SnmpAgentServiceMBean snmpAgentServiceMBean = MBeanServerInvocationHandler.newProxyInstance(server, SnmpAgentServiceMBean.OBJECT_NAME, SnmpAgentServiceMBean.class, true);
 				if(snmpAgentServiceMBean != null) {
 					if(snmp.getAttributesMetaData() != null) {
 						snmpAgentServiceMBean.addAttributeMappings(snmp.getAttributesMetaData().getManagedBeans());
 					}
 					if(snmp.getNotificationsMetaData() != null) {
 						snmpAgentServiceMBean.addNotifications(snmp.getNotificationsMetaData().getMappings());
 					}
 				}
 			} catch (Exception e) {
 				logger.warn("cannot access the snmp agent service for unit " + unit.getRelativePath());
 			}
 		}
 	}
 	
 	@Override
 	public void undeploy(VFSDeploymentUnit unit, SnmpMetaData snmp) {
 		if(snmp != null) {
 			try {
 				MBeanServer server = MBeanServerLocator.locateJBoss();		
 				SnmpAgentServiceMBean snmpAgentServiceMBean = MBeanServerInvocationHandler.newProxyInstance(server, SnmpAgentServiceMBean.OBJECT_NAME, SnmpAgentServiceMBean.class, true);
 				if(snmpAgentServiceMBean != null) {
 					if(snmp.getAttributesMetaData() != null) {
 						snmpAgentServiceMBean.removeAttributeMappings(snmp.getAttributesMetaData().getManagedBeans());
 					}
 					if(snmp.getNotificationsMetaData() != null) {
 						snmpAgentServiceMBean.removeNotifications(snmp.getNotificationsMetaData().getMappings());
 					}
 				}
 			} catch (Exception e) {
 				logger.warn("cannot access the snmp agent service for unit " + unit.getRelativePath());
 			}
 		}
 	}
 
 }
