 /*
  * TeleStax, Open Source Cloud Communications.
  * Copyright 2012 and individual contributors by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.mobicents.commons.congestion;
 
 import java.lang.management.ManagementFactory;
 import java.lang.management.OperatingSystemMXBean;
 
 import javolution.util.FastList;
 
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @author jean.deruelle@gmail.com
  *
  */
 public class CPUProcessCongestionMonitor implements CongestionMonitor {
 	private static final Logger logger = Logger.getLogger(CPUProcessCongestionMonitor.class);
 
 	private static final String SOURCE = "CPU";
 
 	private final FastList<CongestionListener> listeners = new FastList<CongestionListener>();
 
 	private OperatingSystemMXBean osMBean;
 	private int  availableProcessors;
     private long lastSystemTime      = 0;
     private long lastProcessCpuTime  = 0;
 	
 	private volatile double percentageOfCPUUsed;
 
 	private volatile boolean cpuTooHigh = false;
 
 	private double backToNormalCPUThreshold;
 
 	private double cpuThreshold;
 
 	public CPUProcessCongestionMonitor() {
 		osMBean = ManagementFactory.getOperatingSystemMXBean();
 		availableProcessors = osMBean.getAvailableProcessors();
 		baselineCounters();
 	}
 
 	/**
 	 * @param backToNormalPercentageOfCPUUsed
 	 *            the backToNormalPercentageOfCPUUsed to set
 	 */
 	public void setBackToNormalCPUThreshold(double backToNormalCPUThreshold) {
 		this.backToNormalCPUThreshold = backToNormalCPUThreshold;
 		if (logger.isInfoEnabled()) {
 			logger.info("Back To Normal CPU threshold set to " + backToNormalCPUThreshold + "%");
 		}
 	}
 
 	/**
 	 * @return the backToNormalPercentageOfCPUThreshold
 	 */
 	public double getBackToNormalCPUThreshold() {
 		return backToNormalCPUThreshold;
 	}
 
 	/**
 	 * @param cpuThreshold
 	 *            the cpuThreshold to set
 	 */
 	public void setCPUThreshold(double cpuThreshold) {
 		this.cpuThreshold = cpuThreshold;
 		if (logger.isInfoEnabled()) {
 			logger.info("CPU threshold set to " + this.cpuThreshold + "%");
 		}
 	}
 
 	/**
 	 * @return the cpuThreshold
 	 */
 	public double getCPUThreshold() {
 		return cpuThreshold;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.mobicents.commons.congestion.CongestionMonitor#monitor()
 	 */
 	@Override
 	public void monitor() {
 		if(osMBean == null) {
 			return;
 		}
 
 		percentageOfCPUUsed = getCpuUsage();
		if(logger.isDebugEnabled()) {
			logger.debug("CPU Process Load Average = " + percentageOfCPUUsed);
 		}
 		if (this.cpuTooHigh) {
 			if (this.percentageOfCPUUsed < this.backToNormalCPUThreshold) {
 				logger.warn("CPU used: " + percentageOfCPUUsed + "% < to the back to normal CPU threshold : " + this.backToNormalCPUThreshold);
 				this.cpuTooHigh = false;
 
 				// Lets notify the listeners
 				for (FastList.Node<CongestionListener> n = listeners.head(), end = listeners.tail(); (n = n.getNext()) != end;) {
 					CongestionListener listener = n.getValue();
 					listener.onCongestionFinish(SOURCE);
 				}
 			}
 		} else {
 			if (this.percentageOfCPUUsed > cpuThreshold) {
 				logger.warn("CPU used: " + percentageOfCPUUsed + "% > to the CPU threshold : " + this.cpuThreshold);
 				this.cpuTooHigh = true;
 
 				// Lets notify the listeners
 				for (FastList.Node<CongestionListener> n = listeners.head(), end = listeners.tail(); (n = n.getNext()) != end;) {
 					CongestionListener listener = n.getValue();
 					listener.onCongestionStart(SOURCE);
 				}
 			}
 		}
 
 	}
 	
 
 	public synchronized double getCpuUsage() {
 		long systemTime = System.nanoTime();
 		long processCpuTime = 0;
 
 		if (osMBean instanceof OperatingSystemMXBean) {
 			processCpuTime = ((com.sun.management.OperatingSystemMXBean) osMBean)
 					.getProcessCpuTime();
 		}
 
 		double cpuUsage = (double) (processCpuTime - lastProcessCpuTime)
 				/ (systemTime - lastSystemTime);
 
 		lastSystemTime = systemTime;
 		lastProcessCpuTime = processCpuTime;
 
 		return cpuUsage / availableProcessors;
 	}
 
 	private void baselineCounters() {
 		lastSystemTime = System.nanoTime();
 
 		if (osMBean instanceof OperatingSystemMXBean) {
 			lastProcessCpuTime = ((com.sun.management.OperatingSystemMXBean) osMBean)
 					.getProcessCpuTime();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.mobicents.commons.congestion.CongestionMonitor#addCongestionListener(org.mobicents.commons.congestion.CongestionListener)
 	 */
 	@Override
 	public void addCongestionListener(CongestionListener listener) {
 		this.listeners.add(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.mobicents.commons.congestion.CongestionMonitor#removeCongestionListener(org.mobicents.commons.congestion.CongestionListener)
 	 */
 	@Override
 	public void removeCongestionListener(CongestionListener listener) {
 		this.listeners.remove(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.mobicents.commons.congestion.CongestionMonitor#getSource()
 	 */
 	@Override
 	public String getSource() {
 		return SOURCE;
 	}
 
 }
