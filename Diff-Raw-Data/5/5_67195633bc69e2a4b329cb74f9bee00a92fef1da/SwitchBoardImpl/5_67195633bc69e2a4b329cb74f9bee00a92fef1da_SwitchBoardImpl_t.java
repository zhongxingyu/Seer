 /*
  * Copyright 2008 buschmais GbR
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.
  * See the License for the specific language governing permissions and
  * limitations under the License
  */
 package com.buschmais.maexo.framework.switchboard.impl;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.management.MBeanServer;
 import javax.management.MBeanServerConnection;
 import javax.management.NotificationFilter;
 import javax.management.NotificationListener;
 import javax.management.ObjectName;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Provides transparent couplings between MBeans, {@link NotificationListener}s,
  * {@link MBeanServer}s and {@link MBeanServerConnection}s by tracking the life
  * cycle of these items in the OSGi service registry.
  * <p>
  * The main idea is to register MBeans not to the MBean server directly but to
  * the OSGi service registry as an OSGi service. The <strong>MAEXO switch
  * board</strong> will take care of that all MBean services will be registered
  * (and transparently unregistered) to all known MBean servers. The same
  * mechanism is implemented for the relation between NotificationListeners and
  * MBean server connections.
  */
 public final class SwitchBoardImpl {
 
 	private static Logger logger = LoggerFactory
 			.getLogger(SwitchBoardImpl.class);
 
 	private Set<MBeanServerConnectionRegistration> mbeanServerConnections = null;
 
 	private Set<MBeanServerRegistration> mbeanServers = null;
 
 	private Set<MBeanRegistration> mbeans = null;
 
 	private Set<NotificationListenerRegistration> notificationListeners = null;
 
 	/**
 	 * Starts the switch board.
 	 */
	public synchronized void start() {
 		this.mbeanServerConnections = new HashSet<MBeanServerConnectionRegistration>();
 		this.mbeanServers = new HashSet<MBeanServerRegistration>();
 		this.mbeans = new HashSet<MBeanRegistration>();
 		this.notificationListeners = new HashSet<NotificationListenerRegistration>();
 	}
 
 	/**
 	 * Stops the switch board.
 	 * <p>
 	 * All registrations of MBeans and notification listeners are removed.
 	 */
	public synchronized void stop() {
 		for (MBeanServerConnectionRegistration mbeanServerConnectionRegistration : new HashSet<MBeanServerConnectionRegistration>(
 				this.mbeanServerConnections)) {
 			this
 					.unregisterMBeanServerConnection(mbeanServerConnectionRegistration);
 		}
 		for (MBeanServerRegistration mbeanServerRegistration : new HashSet<MBeanServerRegistration>(
 				this.mbeanServers)) {
 			this.unregisterMBeanServer(mbeanServerRegistration);
 		}
 		for (MBeanRegistration mbeanRegistration : new HashSet<MBeanRegistration>(
 				this.mbeans)) {
 			this.unregisterMBean(mbeanRegistration);
 		}
 		for (NotificationListenerRegistration notificationListenerRegistration : new HashSet<NotificationListenerRegistration>(
 				this.notificationListeners)) {
 			this.removeNotificationListener(notificationListenerRegistration);
 		}
 	}
 
 	/**
 	 * Adds a notification listener.
 	 * <p>
 	 * The notification listener is registered on all known MBean server
 	 * connections.
 	 * 
 	 * @param notificationListenerRegistration
 	 *            The notification listener registration.
 	 */
 	public synchronized void addNotificationListener(
 			NotificationListenerRegistration notificationListenerRegistration) {
 		assert (this.notificationListeners != null);
 		logger.debug("adding notification listener {}",
 				notificationListenerRegistration);
 		if (!this.notificationListeners
 				.contains(notificationListenerRegistration)) {
 			logger
 					.debug(
 							"adding notification listener {} on all known MBean server connections",
 							notificationListenerRegistration);
 			this.notificationListeners.add(notificationListenerRegistration);
 			for (MBeanServerConnectionRegistration mbeanServerConnectionRegistration : this.mbeanServerConnections) {
 				this.addNotificationListener(mbeanServerConnectionRegistration
 						.getMBeanServerConnection(),
 						notificationListenerRegistration.getObjectName(),
 						notificationListenerRegistration
 								.getNotificationListener(),
 						notificationListenerRegistration
 								.getNotificationFilter(),
 						notificationListenerRegistration.getHandback());
 			}
 		} else {
 			logger.warn("notification listener {} is already registered",
 					notificationListenerRegistration);
 		}
 	}
 
 	/**
 	 * Registers an MBean server connection instance.
 	 * <p>
 	 * The MBean server connection is associated with all known notification
 	 * listeners.
 	 * 
 	 * @param mbeanServerConnectionRegistration
 	 *            The MBean server connection registration.
 	 */
 	public synchronized void registerMBeanServerConnection(
 			MBeanServerConnectionRegistration mbeanServerConnectionRegistration) {
 		assert (this.mbeanServerConnections != null);
 		logger.debug("registering MBean server connection {}",
 				mbeanServerConnectionRegistration);
 		if (!this.mbeanServerConnections
 				.contains(mbeanServerConnectionRegistration)) {
 			logger
 					.debug(
 							"associating MBean server connection {} with all known notification listeners",
 							mbeanServerConnectionRegistration);
 			this.mbeanServerConnections.add(mbeanServerConnectionRegistration);
 			for (NotificationListenerRegistration notificationListenerRegistration : this.notificationListeners) {
 				this.addNotificationListener(mbeanServerConnectionRegistration
 						.getMBeanServerConnection(),
 						notificationListenerRegistration.getObjectName(),
 						notificationListenerRegistration
 								.getNotificationListener(),
 						notificationListenerRegistration
 								.getNotificationFilter(),
 						notificationListenerRegistration.getHandback());
 			}
 		} else {
 			logger.warn("MBean server connection {} is already registered",
 					mbeanServerConnectionRegistration);
 		}
 	}
 
 	/**
 	 * Adds a notification listener on an MBean server connection.
 	 * 
 	 * @param mbeanServerConnection
 	 *            The MBean server connection.
 	 * @param objectName
 	 *            The object name.
 	 * @param notificationListener
 	 *            The notification listener.
 	 * @param notificationFilter
 	 *            The notification filter.
 	 * @param handback
 	 *            The handback context object.
 	 */
 	private void addNotificationListener(
 			MBeanServerConnection mbeanServerConnection, ObjectName objectName,
 			NotificationListener notificationListener,
 			NotificationFilter notificationFilter, Object handback) {
 		try {
 			logger.trace(
 					"adding notification listener {} on server connection {}",
 					notificationListener, mbeanServerConnection);
 			mbeanServerConnection.addNotificationListener(objectName,
 					notificationListener, notificationFilter, handback);
 		} catch (Exception e) {
 			logger
 					.warn(
 							String
 									.format(
 											"exception while adding notification listener %s on MBean server connection %s",
 											notificationListener,
 											mbeanServerConnection), e);
 		}
 	}
 
 	/**
 	 * Registers an MBean.
 	 * <p>
 	 * The MBean is registered on all known MBean servers.
 	 * 
 	 * @param mbeanRegistration
 	 *            The MBean registration.
 	 */
 	public synchronized void registerMBean(MBeanRegistration mbeanRegistration) {
 		assert (this.mbeans != null);
 		logger.debug("registering MBean {}", mbeanRegistration);
 		if (!this.mbeans.contains(mbeanRegistration)) {
 			logger.debug("registering MBean {} on all known MBean servers",
 					mbeanRegistration);
 			this.mbeans.add(mbeanRegistration);
 			for (MBeanServerRegistration mbeanServerRegistration : this.mbeanServers) {
 				this.registerMBean(mbeanServerRegistration.getMBeanServer(),
 						mbeanRegistration.getObjectName(), mbeanRegistration
 								.getMbean());
 			}
 		} else {
 			logger.warn("MBean {} is already registered", mbeanRegistration);
 		}
 	}
 
 	/**
 	 * Registers a MBean server instance.
 	 * <p>
 	 * The MBean server instance is associated with all known MBeans.
 	 * 
 	 * @param mbeanServerRegistration
 	 *            The MBean server registration.
 	 */
 	public synchronized void registerMBeanServer(
 			MBeanServerRegistration mbeanServerRegistration) {
 		assert (this.mbeanServers != null);
 		logger.debug("registering MBean server {}", mbeanServerRegistration);
 		if (!this.mbeanServers.contains(mbeanServerRegistration)) {
 			logger.debug("associating MBean server {} with all known MBeans",
 					mbeanServerRegistration);
 			this.mbeanServers.add(mbeanServerRegistration);
 			for (MBeanRegistration mbeanRegistration : this.mbeans) {
 				this.registerMBean(mbeanServerRegistration.getMBeanServer(),
 						mbeanRegistration.getObjectName(), mbeanRegistration
 								.getMbean());
 			}
 		} else {
 			logger.warn("MBean server {} is already registered",
 					mbeanServerRegistration);
 		}
 	}
 
 	/**
 	 * Registers an MBean on a single MBean server.
 	 * 
 	 * @param mbeanServer
 	 *            The MBean server.
 	 * @param objectName
 	 *            The object name.
 	 * @param mbean
 	 *            The MBean.
 	 */
 	private void registerMBean(MBeanServer mbeanServer, ObjectName objectName,
 			Object mbean) {
 		logger.trace("registering MBean {} on MBean server {}", objectName,
 				mbeanServer);
 		try {
 			mbeanServer.registerMBean(mbean, objectName);
 		} catch (Exception e) {
 			logger
 					.warn(
 							String
 									.format(
 											"caught exception while registering MBean %s on MBean server %s",
 											objectName, mbeanServer), e);
 		}
 	}
 
 	/**
 	 * Removes a notification listener.
 	 * <p>
 	 * The notification listener is removed from all known MBean server
 	 * connections.
 	 * 
 	 * @param notificationListenerRegistration
 	 *            The notification listener registration.
 	 * 
 	 */
 	public synchronized void removeNotificationListener(
 			NotificationListenerRegistration notificationListenerRegistration) {
 		logger.debug("removing notification listener {}",
 				notificationListenerRegistration);
 		if (this.notificationListeners.remove(notificationListenerRegistration)) {
 			logger
 					.debug(
 							"removing notification listener {} from all known MBean server connections",
 							notificationListenerRegistration);
 			for (MBeanServerConnectionRegistration mbeanServerConnectionRegistration : this.mbeanServerConnections) {
 				this.removeNotificationListener(
 						mbeanServerConnectionRegistration
 								.getMBeanServerConnection(),
 						notificationListenerRegistration.getObjectName(),
 						notificationListenerRegistration
 								.getNotificationListener(),
 						notificationListenerRegistration
 								.getNotificationFilter(),
 						notificationListenerRegistration.getHandback());
 			}
 		} else {
 			logger.warn("notification listener {} is not registered",
 					notificationListenerRegistration);
 		}
 	}
 
 	/**
 	 * Unregisters an MBean server connection instance.
 	 * <p>
 	 * The MBean server connection is disassociated with all known notification
 	 * listeners.
 	 * 
 	 * @param mbeanServerConnectionRegistration
 	 *            The MBean server connection registration.
 	 */
 	public synchronized void unregisterMBeanServerConnection(
 			MBeanServerConnectionRegistration mbeanServerConnectionRegistration) {
 		logger.debug("unregistering MBean server connection {}",
 				mbeanServerConnectionRegistration);
 		if (this.mbeanServerConnections
 				.remove(mbeanServerConnectionRegistration)) {
 			logger
 					.debug(
 							"disassociating MBean server connection {} with all known notification listeners",
 							mbeanServerConnectionRegistration);
 			for (NotificationListenerRegistration notificationListenerRegistration : this.notificationListeners) {
 				this.removeNotificationListener(
 						mbeanServerConnectionRegistration
 								.getMBeanServerConnection(),
 						notificationListenerRegistration.getObjectName(),
 						notificationListenerRegistration
 								.getNotificationListener(),
 						notificationListenerRegistration
 								.getNotificationFilter(),
 						notificationListenerRegistration.getHandback());
 			}
 		} else {
 			logger.warn(
 					"MBean server connection {} is not registered, skipping",
 					mbeanServerConnectionRegistration);
 		}
 	}
 
 	/**
 	 * Removes a notification listener from a single MBean server connection.
 	 * 
 	 * @param mbeanServerConnection
 	 *            The MBean server connection.
 	 * @param objectName
 	 *            The object name.
 	 * @param notificationListener
 	 *            The notification listener.
 	 * @param notificationFilter
 	 *            The notification filter.
 	 * @param handback
 	 *            The handback context object.
 	 */
 	private void removeNotificationListener(
 			MBeanServerConnection mbeanServerConnection, ObjectName objectName,
 			NotificationListener notificationListener,
 			NotificationFilter notificationFilter, Object handback) {
 		try {
 			logger
 					.trace(
 							"removing notification listener {} from MBean server connection {}",
 							notificationListener, mbeanServerConnection);
 			mbeanServerConnection.removeNotificationListener(objectName,
 					notificationListener, notificationFilter, handback);
 		} catch (Exception e) {
 			logger
 					.warn(
 							String
 									.format(
 											"caught exception while removing notification listener %s from MBean server conncetion %s",
 											notificationListener,
 											mbeanServerConnection), e);
 		}
 	}
 
 	/**
 	 * Unregisters an MBean.
 	 * <p>
 	 * The MBean is unregistered from all known MBean servers.
 	 * 
 	 * @param mbeanRegistration
 	 *            The MBean registration.
 	 */
 	public synchronized void unregisterMBean(MBeanRegistration mbeanRegistration) {
 		logger.debug("unregistering MBean {}", mbeanRegistration);
 		if (this.mbeans.remove(mbeanRegistration)) {
 			logger.debug("unregistering MBean {} from all known MBean servers",
 					mbeanRegistration);
 			for (MBeanServerRegistration mbeanServerRegistration : this.mbeanServers) {
 				this.unregisterMBean(mbeanServerRegistration.getMBeanServer(),
 						mbeanRegistration.getObjectName());
 			}
 		} else {
 			logger.warn("MBean {} is not registered", mbeanRegistration);
 		}
 	}
 
 	/**
 	 * Unregisters an MBean server instance.
 	 * <p>
 	 * The MBean server instance is disassociated with all known MBeans.
 	 * 
 	 * @param mbeanServerRegistration
 	 *            The MBean server registration.
 	 */
 	public synchronized void unregisterMBeanServer(
 			MBeanServerRegistration mbeanServerRegistration) {
 		logger.debug("unregistering MBean server {}", mbeanServerRegistration);
 		if (this.mbeanServers.remove(mbeanServerRegistration)) {
 			logger.debug(
 					"disassociating MBean server {} with all known MBeans",
 					mbeanServerRegistration);
 			for (MBeanRegistration mbeanRegistration : this.mbeans) {
 				this.unregisterMBean(mbeanServerRegistration.getMBeanServer(),
 						mbeanRegistration.getObjectName());
 			}
 		} else {
 			logger.warn("MBean server {} is not registered, skipping",
 					mbeanServerRegistration);
 		}
 	}
 
 	/**
 	 * Unregisters an MBean from a single MBean server.
 	 * 
 	 * @param mbeanServer
 	 *            The MBean server.
 	 * @param objectName
 	 *            The object name.
 	 */
 	private void unregisterMBean(MBeanServer mbeanServer, ObjectName objectName) {
 		try {
 			logger.trace("unregistering MBean {} from server {}", objectName,
 					mbeanServer);
 			mbeanServer.unregisterMBean(objectName);
 		} catch (Exception e) {
 			logger
 					.warn(
 							String
 									.format(
 											"caught exception while unregistering MBean %s from MBean server %s",
 											objectName, mbeanServer), e);
 		}
 	}
 }
