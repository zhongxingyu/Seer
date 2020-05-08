 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.epics.connection;
 
 import gda.epics.connection.EpicsController.MonitorType;
 import gov.aps.jca.CAException;
 import gov.aps.jca.Channel;
 import gov.aps.jca.event.ConnectionEvent;
 import gov.aps.jca.event.ConnectionListener;
 import gov.aps.jca.event.MonitorListener;
 import gov.aps.jca.event.PutEvent;
 import gov.aps.jca.event.PutListener;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * EPICS channel (connection, monitor) manager.
  */
 public class EpicsChannelManager implements ConnectionListener, PutListener {
 	private static final Logger logger = LoggerFactory.getLogger(EpicsChannelManager.class);
 	/**
 	 * EPICS controller.
 	 */
 	protected EpicsController controller;
 
 	/**
 	 * Listener to be informed about initialization completion.
 	 */
 	private InitializationListener initializationListener;
 
 	/**
 	 * Map of all handled channels.
 	 */
 	protected Map<String, Channel> channels;
 
 	/**
 	 * Monitors to be installed.
 	 */
 	protected Map<Channel, MonitorListener> monitoredChannels;
 
 	/**
 	 * Map of initial values of the channels.
 	 */
 	protected Map<String, Object> initialValues;
 	/**
 	 * Type of monitors.
 	 */
 	protected Map<MonitorListener, MonitorType> monitorTypes;
 	/**
 	 * Map of all critical (non-optional) channels to be connected.
 	 */
 	protected Set<Channel> unconnectedCriticalChannels;
 	protected Set<Channel> connectedCriticalChannels;
 
 	/**
 	 * Initialization status. (sync on unconnectedCriticalChannels)
 	 */
 	protected boolean initialized = false;
 
 	/**
 	 * Destruction status. (sync on channels)
 	 */
 	protected boolean destroyed = false;
 
 	/**
 	 * Creation phase completed flag. (sync on unconnectedCriticalChannels)
 	 */
 	protected boolean creationPhaseCompleted = false;
 
 	/**
 	 * Default constructor.
 	 */
 	public EpicsChannelManager() {
 		this(null);
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param listener
 	 *            initialization listener.
 	 */
 	public EpicsChannelManager(InitializationListener listener) {
 		this.initializationListener = listener;
 
 		controller = EpicsController.getInstance();
 		channels = new HashMap<String, Channel>();
 		unconnectedCriticalChannels = new HashSet<Channel>();
 		connectedCriticalChannels = new HashSet<Channel>();
 		monitoredChannels = new HashMap<Channel, MonitorListener>();
 		monitorTypes = new HashMap<MonitorListener, MonitorType>();
 		initialValues = new HashMap<String, Object>();
 	}
 
 	/**
 	 * Create channel.
 	 * 
 	 * @param pvName
 	 * @param monitorListener
 	 * @param optional
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName, MonitorListener monitorListener, boolean optional) throws CAException {
 		return createChannel(pvName, monitorListener, null, optional);
 		/*
 		 * synchronized (channels) { if (destroyed) throw new IllegalStateException("Channel manager destroyed.");
 		 * Channel channel = controller.createChannel(pvName, this); channels.put(pvName, channel); if (!optional) {
 		 * synchronized (unconnectedCriticalChannels) { unconnectedCriticalChannels.add(channel); } } if
 		 * (monitorListener != null) { synchronized (monitoredChannels) { monitoredChannels.put(channel,
 		 * monitorListener); } } return channel; }
 		 */
 	}
 
 	/**
 	 * Create channel with Monitor type.
 	 * 
 	 * @param pvName
 	 * @param monitorListener
 	 * @param monitorType
 	 * @param optional
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName, MonitorListener monitorListener, MonitorType monitorType,
 			boolean optional) throws CAException {
 		synchronized (channels) {
 			if (destroyed)
 				throw new IllegalStateException("Channel manager destroyed.");
 
 			Channel channel = controller.createChannel(pvName, this);
 
 			channels.put(pvName, channel);
 
 			if (!optional) {
 				synchronized (unconnectedCriticalChannels) {
 					unconnectedCriticalChannels.add(channel);
 				}
 			}
 
 			if (monitorListener != null) {
 				synchronized (monitoredChannels) {
 					monitoredChannels.put(channel, monitorListener);
 				}
 			}
 
 			if (monitorListener != null && monitorType != null) {
 				synchronized (monitorTypes) {
 					monitorTypes.put(monitorListener, monitorType);
 				}
 			}
 
 			return channel;
 		}
 	}
 
 	/**
 	 * Create channel.
 	 * 
 	 * @param pvName
 	 * @param monitorListener
 	 * @param optional
 	 * @param initialValue
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName, MonitorListener monitorListener, boolean optional, double initialValue)
 			throws CAException {
 		synchronized (initialValues) {
 			initialValues.put(pvName, initialValue);
 		}
 		return createChannel(pvName, monitorListener, optional);
 	}
 
 	/**
 	 * Create channel.
 	 * 
 	 * @param pvName
 	 * @param monitorListener
 	 * @param optional
 	 * @param initialValue
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName, MonitorListener monitorListener, boolean optional, float initialValue)
 			throws CAException {
 		synchronized (initialValues) {
 			initialValues.put(pvName, initialValue);
 		}
 		return createChannel(pvName, monitorListener, optional);
 	}
 
 	/**
 	 * Create channel.
 	 * 
 	 * @param pvName
 	 * @param monitorListener
 	 * @param optional
 	 * @param initialValue
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName, MonitorListener monitorListener, boolean optional, int initialValue)
 			throws CAException {
 		synchronized (initialValues) {
 			initialValues.put(pvName, initialValue);
 		}
 		return createChannel(pvName, monitorListener, optional);
 	}
 
 	/**
 	 * Create channel.
 	 * 
 	 * @param pvName
 	 * @param monitorListener
 	 * @param optional
 	 * @param initialValue
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName, MonitorListener monitorListener, boolean optional, short initialValue)
 			throws CAException {
 		synchronized (initialValues) {
 			initialValues.put(pvName, initialValue);
 		}
 		return createChannel(pvName, monitorListener, optional);
 	}
 
 	/**
 	 * Create channel.
 	 * 
 	 * @param pvName
 	 * @param monitorListener
 	 * @param optional
 	 * @param initialValue
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName, MonitorListener monitorListener, boolean optional, String initialValue)
 			throws CAException {
 		synchronized (initialValues) {
 			initialValues.put(pvName, initialValue);
 		}
 		return createChannel(pvName, monitorListener, optional);
 	}
 
 	/**
 	 * Create channel.
 	 * 
 	 * @param pvName
 	 * @param monitorListener
 	 * @param optional
 	 * @param initialValue
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName, MonitorListener monitorListener, boolean optional, double[] initialValue)
 			throws CAException {
 		synchronized (initialValues) {
 			initialValues.put(pvName, initialValue);
 		}
 		return createChannel(pvName, monitorListener, optional);
 	}
 
 	/**
 	 * Create channel.
 	 * 
 	 * @param pvName
 	 * @param monitorListener
 	 * @param optional
 	 * @param initialValue
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName, MonitorListener monitorListener, boolean optional, int[] initialValue)
 			throws CAException {
 		synchronized (initialValues) {
 			initialValues.put(pvName, initialValue);
 		}
 		return createChannel(pvName, monitorListener, optional);
 	}
 
 	/**
 	 * Creates channel without monitor listener.
 	 * 
 	 * @param pvName
 	 * @param optional
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName, boolean optional) throws CAException {
 		return createChannel(pvName, null, optional);
 	}
 
 	/**
 	 * Creates (optional) channel without monitor listener.
 	 * 
 	 * @param pvName
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName) throws CAException {
 		return createChannel(pvName, null, true);
 	}
 
 	/**
 	 * Creates (optional) channel with monitor.
 	 * 
 	 * @param pvName
 	 * @param listener
 	 * @return JCA channel instance.
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvName, MonitorListener listener) throws CAException {
 		return createChannel(pvName, listener, true);
 	}
 
 	@Override
 	public void connectionChanged(ConnectionEvent event){
 		if (!event.isConnected()) {
 			logger.info("Channel - " + event.getSource() + " connection callback but NOT connected.");
 			return;
 		}
 
 		Channel channel = (Channel) event.getSource();
 
 		synchronized (initialValues) {
 			Object initialValue = initialValues.get(channel.getName());
 			if (initialValue != null)
 				try {
 					setInitialValue(channel, initialValue);
 				} catch (InterruptedException e) {
 					logger.error("Cannot set initial values ",e);
 				}
 		}
 
 		MonitorListener listener;
 		synchronized (monitoredChannels) {
 			listener = monitoredChannels.remove(channel);
 		}
 
 		MonitorType monitorType = null;
 		if (listener != null) {
 			synchronized (monitorTypes) {
 				monitorType = monitorTypes.get(listener);
 			}
 		}
 
 		// install monitor if necessary
 		if (listener != null) {
 			if (monitorType != null) {
 				try {
 					controller.setMonitor(channel, listener, monitorType);
 				} catch (Throwable th) {
 					// we failed to install monitor...
 					synchronized (monitoredChannels) {
 						monitoredChannels.put(channel, listener);
 						logger.error("Monitor - " + listener + " can NOT be added to channel - " + channel.getName(),th);
 					}
 				}
 			} else {
 				try {
 					controller.setMonitor(channel, listener, MonitorType.NATIVE);
 				} catch (Throwable th) {
 					// we failed to install monitor...
 					synchronized (monitoredChannels) {
 						monitoredChannels.put(channel, listener);
 						logger.error("Monitor - " + listener + " can NOT be added to channel - " + channel.getName(),th);
 					}
 				}
 			}
 
 		}
 
 		// remove it from critical non-conected channels
 		synchronized (unconnectedCriticalChannels) {
 			unconnectedCriticalChannels.remove(channel);
 			checkInitializationCompletion();
 		}
 	}
 
 	/**
 	 * Set a value.
 	 * 
 	 * @param channel
 	 * @param initialValue
 	 * @throws InterruptedException 
 	 */
 	private void setInitialValue(Channel channel, Object initialValue) throws InterruptedException {
 		try {
 			if (initialValue instanceof Double)
 				controller.caput(channel, ((Double) initialValue).doubleValue());
 			else if (initialValue instanceof Float)
 				controller.caput(channel, ((Float) initialValue).floatValue());
 			else if (initialValue instanceof Integer)
 				controller.caput(channel, ((Integer) initialValue).intValue());
 			else if (initialValue instanceof Short)
 				controller.caput(channel, ((Short) initialValue).shortValue());
 			else if (initialValue instanceof String)
 				controller.caput(channel, (String) initialValue);
 			else if (initialValue instanceof double[])
 				controller.caput(channel, (double[]) initialValue);
 			else if (initialValue instanceof int[])
 				controller.caput(channel, (int[]) initialValue);
 			else if (initialValue instanceof float[])
 				controller.caput(channel, (float[]) initialValue);
 			else if (initialValue instanceof short[])
 				controller.caput(channel, (short[]) initialValue);
 			else if (initialValue instanceof String[])
 				controller.caput(channel, (String[]) initialValue);
 			/*
 			 * else if (initialValue instanceof float[]) controller.caput(channel, (float[])initialValue); else if
 			 * (initialValue instanceof short[]) controller.caput(channel, (short[])initialValue); else if (initialValue
 			 * instanceof String[]) controller.caput(channel, (String[])initialValue);
 			 */
 			else
 				throw new IllegalArgumentException("unsupported class type: " + initialValue.getClass());
 		} catch (CAException e) {
 			logger.error("set initial value {} failed.", initialValue);
 		}
 	}
 
 	private final void checkInitializationCompletion() {
 		boolean notify = false;
 		// not needed since called from sync already, but here so that the method can be reused
 		synchronized (unconnectedCriticalChannels) {
			if (!initialized && creationPhaseCompleted && checkAllChannelsConnected()) {
 				notify = true;
 				initialized = true;
 				unconnectedCriticalChannels.notifyAll();
 			}
 		}
 		// notify that all critical channels have been connected
 		if (notify) {
 			notifyInitalizationCompleted();
 		}
 	}
 
	private boolean checkAllChannelsConnected() {
		if (unconnectedCriticalChannels.size() == 0){
			return true;
		}
		
		// can get to this point and the unconnected channels may have connected by now
		for (Channel channel : unconnectedCriticalChannels){
			if (channel.getConnectionState() != Channel.CONNECTED){
				boolean connected = false;
				// give it a chance to connect, but try to minimise the wait
				for (int i = 0; i < 40; i++){
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						break;
					}
					if (channel.getConnectionState() == Channel.CONNECTED){
						connected = true;
						break;
					}
				}
				if (!connected){
					return false;
				}
			}
		}
		return true;
	}

 	/**
 	 * Notify about initialization completion.
 	 */
 	private void notifyInitalizationCompleted() {
 		if (initializationListener != null) {
 			try {
 				controller.execute(new Runnable() {
 					@Override
 					public void run() {
 						try {
 							initializationListener.initializationCompleted();
 						} catch( Exception e) {
 							logger.error(e.getMessage(),e);
 						}
 					}
 				});
 			} catch (Throwable th) {
 				logger.error(th.getMessage(),th);
 			}
 		}
 	}
 
 	/**
 	 * Checks if initialized (all critical channels are connected), if not waits until done or for timeoutInMs ms.
 	 * 
 	 * @param timeoutInMs
 	 * @return initialization status
 	 */
 	public boolean tryInitialize(long timeoutInMs) {
 		synchronized (unconnectedCriticalChannels) {
 			if (initialized)
 				return true;
 
 			if (timeoutInMs >= 0) {
 				try {
 					unconnectedCriticalChannels.wait(timeoutInMs);
 				} catch (InterruptedException e) {
 					// noop
 				}
 			}
 
 			return initialized;
 		}
 	}
 
 	/**
 	 * Default (convenience) put listener - only prints out a message.
 	 * 
 	 * @see gov.aps.jca.event.PutListener#putCompleted(gov.aps.jca.event.PutEvent)
 	 */
 	@Override
 	public void putCompleted(PutEvent pev) {
 		Channel ch = (Channel) pev.getSource();
 
 		if (pev.getStatus().isSuccessful()) {
 			logger.info("Put to {} returns {}", ch.getName(), pev.getStatus().getMessage());
 		} else if (pev.getStatus().isError()) {
 			logger.error("Put to {} returns {}", ch.getName(), pev.getStatus().getMessage());
 		} else if (pev.getStatus().isFatal()) {
 			logger.error("Put to {} returns {}", ch.getName(), pev.getStatus().getMessage());
 		} else {
 			logger.info("Put to {} returns {}", ch.getName(), pev.getStatus().getMessage());
 		}
 	}
 
 	/**
 	 * Get channel (if created by this instance).
 	 * 
 	 * @param pvName
 	 * @return channel instance or <code>null</code>
 	 */
 	public Channel getChannel(String pvName) {
 		synchronized (channels) {
 			return channels.get(pvName);
 		}
 	}
 
 	/**
 	 * Map of all handled channels
 	 * 
 	 * @return map of all handled channels.
 	 */
 	public Map<String, Channel> getChannels() {
 		return Collections.unmodifiableMap(Collections.synchronizedMap(channels));
 	}
 
 	/**
 	 * Destroy all channels.
 	 */
 	public void destroy() {
 		synchronized (channels) {
 			if (destroyed)
 				return;
 			destroyed = true;
 
 			// this is thread and exception safe
 			Iterator<Channel> iter = channels.values().iterator();
 			while (iter.hasNext())
 				iter.next().dispose();
 		}
 
 	}
 
 	/**
 	 * Notify channel manager that creation phase has completed, so that it can issue a initializatonCompleted callback.
 	 * NOTE: this is needed not to issue the notification before all channels of a particular device are registered to
 	 * the channel manager.
 	 */
 	public void creationPhaseCompleted() {
 		synchronized (unconnectedCriticalChannels) {
 			if (creationPhaseCompleted)
 				return;
 			creationPhaseCompleted = true;
 
 			// already all connected check
 			checkInitializationCompletion();
 		}
 	}
 }
