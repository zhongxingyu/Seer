 /*
  * Xomios Project
  * Copyright (c) 2007 Xomios Contributors
  * 
  * This file is released under a modified BSD license. For more information,
  * see the LICENSE file included in this distribution.
  */
 
 package org.xomios.connectivity.net;
 
 
 /**
  * A IP version 4 address with an option port number associated with it
  * 
  * @author Christopher Thunes <cthunes@xomios.brewtab.com>
  * @author Noah Fontes <nfontes@xomios.brewtab.com>
  */
 public class IPv4Address extends NetworkAddress {
 
	/**
	 * TODO This belongs somewhere else -- ports are not associated with
	 * IP addresses.
	 */
	protected static final int MAX_PORT = 65535;
 	protected int port = -1;
 
 	/**
 	 * Create a new address object bound to the specified address
 	 * 
 	 * @param addr The IP to associate this object with the host
 	 * @throws AddressFormatException addr is not a properly formatted IPv4
 	 *             address
 	 */
 	public IPv4Address ( String addr ) throws AddressFormatException {
 		this.setAddress( addr );
 	}
 
 	/**
 	 * Create a new address object bound to the specified address
 	 * 
 	 * @param addr The IP to associate this object with the host
 	 * @throws AddressFormatException addr does not represent a valid IPv4
 	 *             address
 	 */
 	public IPv4Address ( byte[] addr ) throws AddressFormatException {
 		this.setAddress( addr );
 	}
 
 	/**
 	 * Create a new address object bound to the specified address
 	 * 
 	 * @param addr The IP to associate this object with the host
 	 * @param port Port number to associate this object with
 	 * @throws AddressFormatException addr does not represent a valid IPv4
 	 *             address
 	 * @throws IllegalArgumentException port is not inside the valid port range
 	 */
 	public IPv4Address ( String addr, int port ) throws AddressFormatException, IllegalArgumentException {
 		this.setAddress( addr );
 		this.setPort( port );
 	}
 
 	/**
 	 * Create a new address object bound to the specified address
 	 * 
 	 * @param addr The IP to associate this object with the host
 	 * @param port Port number to associate this object with
 	 * @throws AddressFormatException addr does not represent a valid IPv4
 	 *             address
 	 * @throws IllegalArgumentException port is not inside the valid port range
 	 */
 	public IPv4Address ( byte[] addr, int port ) throws AddressFormatException, IllegalArgumentException {
 		this.setAddress( addr );
 		this.setPort( port );
 	}
 
 	/**
 	 * @see org.xomios.connectivity.net.NetworkAddress#setAddress(java.lang.String)
 	 */
 	@Override
 	public void setAddress ( String addr ) throws AddressFormatException {
 		String[] octets = addr.split( "\\." );
 		byte[] address = new byte[4];
 
 		if ( octets.length != 4 ) {
 			throw new AddressFormatException( addr + " is not a valid address" );
 		}
 
 		for ( int i = 0; i < 4; i++ ) {
 			try {
 				address[i] = (byte) ( Integer.valueOf( octets[i] ) & ( (byte)0xff ) );
 			}
 			catch ( NumberFormatException e ) {
 				throw new AddressFormatException( addr + " is not a valid address" );
 			}
 		}
 
 		this.address = address;
 	}
 
 	/**
 	 * @see org.xomios.connectivity.net.NetworkAddress#setAddress(byte[])
 	 */
 	@Override
 	public void setAddress ( byte[] addr ) throws AddressFormatException {
 		if ( addr.length != 4 ) {
 			throw new AddressFormatException( "byte[] of length " + String.valueOf( addr.length ) + " is not valid" );
 		}
 
 		this.address = addr;
 	}
 
 	/**
 	 * @see org.xomios.connectivity.net.NetworkAddress#toString()
 	 */
 	@Override
 	public String toString ( ) {
 		StringBuffer buf = new StringBuffer();
 
 		for ( int i = 0; i < 4; i++ ) {
 			buf.append( this.address[i] & 0xff );
 			if ( i != 3 ) {
 				buf.append( "." );
 			}
 		}
 
 		return buf.toString();
 	}
 
 	/**
 	 * @see org.xomios.connectivity.net.NetworkPort#getPort()
 	 */
 	public int getPort ( ) {
 		return this.port;
 	}
 
 	/**
 	 * @see org.xomios.connectivity.net.NetworkPort#setPort(int)
 	 */
 	public void setPort ( int p ) throws IllegalArgumentException {
 		if ( p < 0 || p > this.getMaxPortNumber() ) {
 			throw new IllegalArgumentException( "Invalid port number specified" );
 		}
 
 		this.port = p;
 	}
 
 	/**
 	 * @see org.xomios.connectivity.net.NetworkPort#getMaxPortNumber()
 	 */
 	public int getMaxPortNumber ( ) {
		return IPv4Address.MAX_PORT;
 	}
 
 	/**
 	 * @see org.xomios.connectivity.net.NetworkPort#portSet()
 	 */
 	public boolean portSet ( ) {
 		if ( this.port == -1 ) {
 			return false;
 		}
 
 		return true;
 	}
 
 }
