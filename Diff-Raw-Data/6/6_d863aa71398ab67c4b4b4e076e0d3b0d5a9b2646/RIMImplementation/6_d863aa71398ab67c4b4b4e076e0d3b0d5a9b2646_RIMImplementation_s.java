 /*
  * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 only, as
  * published by the Free Software Foundation.  Oracle designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Oracle in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 2 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 2 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact Oracle, 500 Oracle Parkway, Redwood Shores
  * CA 94065 USA or visit www.oracle.com if you need additional information or
  * have any questions.
  */
 
 package com.sun.lwuit.io.impl;
 
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.microedition.io.Connection;
 
 import net.rim.device.api.io.transport.ConnectionDescriptor;
 import net.rim.device.api.io.transport.ConnectionFactory;
 import net.rim.device.api.io.transport.TransportInfo;
 import net.rim.device.api.io.transport.options.TcpCellularOptions;
 import net.rim.device.api.servicebook.ServiceBook;
 import net.rim.device.api.servicebook.ServiceRecord;
 import net.rim.device.api.util.Arrays;
 
 import com.sun.lwuit.io.NetworkManager;
 
 /**
  * Implementation targeting RIM devices to support their IAP and elaborate
  * network picking policy described
  * 
  * @author Shai Almog
  */
 public class RIMImplementation extends MIDPImpl {
 
 	private String currentAccessPoint;
//	private boolean deviceSide;
 	private int timeout = 60;
 
 	/**
 	 * @inheritDoc
 	 */
 	public boolean isAPSupported() {
 		return true;
 	}
 
 	private Vector getValidSBEntries() {
 		Vector v = new Vector();
 		ServiceBook bk = ServiceBook.getSB();
 		ServiceRecord[] recs = bk.getRecords();
 		for (int iter = 0; iter < recs.length; iter++) {
 			ServiceRecord sr = recs[iter];
 			if (sr.isValid() && !sr.isDisabled() && sr.getUid() != null
 					&& sr.getUid().length() != 0) {
 				v.addElement(sr);
 			}
 		}
 		return v;
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	public String[] getAPIds() {
 		Vector v = getValidSBEntries();
 		String[] s = new String[v.size()];
 		for (int iter = 0; iter < s.length; iter++) {
 			s[iter] = "" + ((ServiceRecord) v.elementAt(iter)).getUid();
 		}
 		return s;
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	public int getAPType(String id) {
 		Vector v = getValidSBEntries();
 		for (int iter = 0; iter < v.size(); iter++) {
 			ServiceRecord r = (ServiceRecord) v.elementAt(iter);
 			if (("" + r.getUid()).equals(id)) {
 				if (r.getUid().toLowerCase().indexOf("wifi") > -1) {
 					return NetworkManager.ACCESS_POINT_TYPE_WLAN;
 				}
 				// wap2
 				if (r.getCid().toLowerCase().indexOf("wptcp") > -1) {
 					return NetworkManager.ACCESS_POINT_TYPE_NETWORK3G;
 				}
 				return NetworkManager.ACCESS_POINT_TYPE_UNKNOWN;
 			}
 		}
 		return NetworkManager.ACCESS_POINT_TYPE_UNKNOWN;
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	public String getAPName(String id) {
 		Vector v = getValidSBEntries();
 		for (int iter = 0; iter < v.size(); iter++) {
 			ServiceRecord r = (ServiceRecord) v.elementAt(iter);
 			if (("" + r.getUid()).equals(id)) {
 				return r.getName();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	public String getCurrentAccessPoint() {
 		if (currentAccessPoint != null) {
 			return currentAccessPoint;
 		}
 		return null;
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	public void setCurrentAccessPoint(String id) {
 		currentAccessPoint = id;
 //		int t = getAPType(id);
 //		deviceSide = t == NetworkManager.ACCESS_POINT_TYPE_NETWORK3G || t == NetworkManager.ACCESS_POINT_TYPE_WLAN;
 	}
 
 	private Connection httpConnect(String url, boolean read, boolean write) throws IOException {
 		// Preferred transports, in order of preference
 		// (based on GPRS consumer preferences)
 		final int[] transports = {
 			TransportInfo.TRANSPORT_TCP_WIFI,
 			TransportInfo.TRANSPORT_BIS_B,
 			TransportInfo.TRANSPORT_WAP2,
 			TransportInfo.TRANSPORT_MDS,
 			TransportInfo.TRANSPORT_TCP_CELLULAR
 		};
 
 		// Remove any transports that are not currently available.
		for (int i = 0; i < transports.length; i++) {
 			int transport = transports[i];
 			if (!TransportInfo.isTransportTypeAvailable(transport) || !TransportInfo.hasSufficientCoverage(transport)) {
 				System.out.println("XXX remove transport: " + TransportInfo.getTransportTypeName(transport));
 				Arrays.removeAt(transports, i);
 			} else {
 				System.out.println("XXX valid transport: " + TransportInfo.getTransportTypeName(transport));
 			}
 		}
 
 		// Set options for TCP Cellular transport.
 		final TcpCellularOptions tcpOptions = new TcpCellularOptions();
 		if (!TcpCellularOptions.isDefaultAPNSet()) {
 			// TODO
 			tcpOptions.setApn("My APN");
 			tcpOptions.setTunnelAuthUsername("user");
 			tcpOptions.setTunnelAuthPassword("password");
 		}
 
 		final ConnectionFactory factory = new ConnectionFactory();
 
 		// Set ConnectionFactory options
 		if (transports.length > 0) {
 			System.out.println("XXX set preferred transports");
 			factory.setPreferredTransportTypes(transports);
 		}
 		factory.setTransportTypeOptions(TransportInfo.TRANSPORT_TCP_CELLULAR, tcpOptions);
 		factory.setAttemptsLimit(3);
 		factory.setTimeoutSupported(true);
 		factory.setConnectionTimeout(timeout);
 		factory.setEndToEndDesired(url.startsWith("https"));
 
 		int connectionMode = ConnectionFactory.ACCESS_READ;
 		if (write) {
 			connectionMode = read ? ConnectionFactory.ACCESS_READ_WRITE : ConnectionFactory.ACCESS_WRITE;
 		}
 		factory.setConnectionMode(connectionMode);
 
 		final ConnectionDescriptor cd = factory.getConnection(url);
 		if (cd != null) {
 			return cd.getConnection();
 		} else {
 			throw new IOException();
 		}
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	public Object connect(String url, boolean read, boolean write) throws IOException {
 		if (url.startsWith("http")) {
 			return httpConnect(url, read, write);
 		}
 		return super.connect(url, read, write);
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	public boolean isTimeoutSupported() {
 		return true;
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	public void setTimeout(int t) {
 		timeout = t;
 	}
 }
