 /*
 	Copyright (c) Members of the EGEE Collaboration. 2004. 
 	See http://www.eu-egee.org/partners/ for details on the copyright
 	holders.  
 
 	Licensed under the Apache License, Version 2.0 (the "License"); 
 	you may not use this file except in compliance with the License. 
 	You may obtain a copy of the License at 
 
 	    http://www.apache.org/licenses/LICENSE-2.0 
 
 	Unless required by applicable law or agreed to in writing, software 
 	distributed under the License is distributed on an "AS IS" BASIS, 
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 	See the License for the specific language governing permissions and 
 	limitations under the License. 
  */
 package org.glite.authz.pap.authz.util;
 
 import java.security.Principal;
 
 import org.bouncycastle.asn1.DERObjectIdentifier;
 import org.bouncycastle.asn1.x509.X509Name;
 import org.bouncycastle.jce.X509Principal;
 import org.glite.security.util.DN;
 import org.glite.security.util.DNHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 /**
 * A replacement for buggy DNImpl in glite.security.util-java which doesn't comply to RFC2253
 * for DN string formatting.
 * 
 * As soon as util-java is fixed, this class will be removed in favour of the util-java
 * one.
  * 
  * @author Joni Hahkala
  *  
  *         Created on September 8, 2003, 7:21 PM
  */
 public class DNImpl implements DN {
 
 	/** Marker for the RFC2253 format. */
 	public static final int RFC2253 = 0;
 
 	/** Marker for the X500 format. */
 	public static final int X500 = 1;
 
 	/** Marker for the canonicalized format. */
 	public static final int CANON = 2;
 
 	/** Logging facility. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DNImpl.class);
 
 	/**
 	 * The DN in RFC2253 format. A cache to avoid generating the string multiple
 	 * times
 	 */
 	public String m_rfc2253String = null;
 
 	/**
 	 * The DN in X500 format. A cache to avoid generating the string multiple
 	 * times
 	 */
 	public String m_x500String = null;
 
 	/**
 	 * The DN in canonical format. A cache to avoid generating the string
 	 * multiple times
 	 */
 	public String m_canonicalString = null;
 
 	/** The array of relative distiquished names. */
 	public String[] rdns = null;
 
 	/** The array of object identifiers. */
 	public DERObjectIdentifier[] oids = null;
 
 	/** The number of fields in the DN. */
 	public int count = 0;
 	
 	/**
 	 * Creates a new instance of DN.
 	 * 
 	 * @param newOids
 	 *            The array of object identifiers.
 	 * @param newRdns
 	 *            The array or relative distinquished names.
 	 * @param newCount
 	 *            The number of fields in the DN (both oids and rdns have to
 	 *            have this number of items).
 	 */
 	public DNImpl(DERObjectIdentifier[] newOids, String[] newRdns, int newCount) {
 		oids = newOids;
 		rdns = newRdns;
 		count = newCount;
 	}
 
 	/**
 	 * Creates a new DNImpl object.
 	 * 
 	 * @param name
 	 *            Generates a new DNImpl class from the DN in the name.
 	 */
 	public DNImpl(String name) {
 		if (name.startsWith("/")) {
 			parseX500(name);
 		} else {
 			parse(name);
 		}
 	}
 
 	/**
 	 * Creates a new DNImpl object.
 	 * 
 	 * @param principal
 	 *            The Principal holding the information to generate the DN from.
 	 */
 	public DNImpl(Principal principal) {
 		X509Principal x509Principal;
 
 		if (principal instanceof X509Principal) {
 			LOGGER.debug("input is X509Principal");
 			x509Principal = (X509Principal) principal;
 		} else {
 			LOGGER.debug("input is some other principal: "
 					+ principal.getClass().getName());
 			x509Principal = new X509Principal(true, principal.getName());
 
 		}
 
 		oids = (DERObjectIdentifier[]) x509Principal.getOIDs().toArray(
 				new DERObjectIdentifier[] {});
 		rdns = (String[]) x509Principal.getValues().toArray(new String[] {});
 		count = oids.length;
 	}
 
 	/**
 	 * Creates a new DNImpl object.
 	 * 
 	 * @param x509Name
 	 *            The X509Name instance holding the information to generate the
 	 *            DN from.
 	 */
 	public DNImpl(X509Name x509Name) {
 		oids = (DERObjectIdentifier[]) x509Name.getOIDs().toArray(
 				new DERObjectIdentifier[] {});
 		rdns = (String[]) x509Name.getValues().toArray(new String[0]);
 		count = oids.length;
 
 	}
 
 	/**
 	 * Generates a X500 format string of the DN.
 	 * 
 	 * @return the X500 format string of the DN.
 	 */
 	public String getX500() {
 		if (m_x500String == null) {
 			constructX500();
 		}
 
 		return m_x500String;
 	}
 
 	/**
 	 * Generates a RFC2253 format string of the DN.
 	 * 
 	 * @return the RFC2253 format string of the DN.
 	 */
 	public String getRFC2253() {
 		if (m_rfc2253String == null) {
 			constructRFC2253(false);
 		}
 
 		return m_rfc2253String;
 	}
 
 	/**
 	 * Generates a canonical format string of the DN.
 	 * 
 	 * @return the canonical format string of the DN.
 	 */
 	public String getCanon() {
 		if (m_canonicalString == null) {
 			constructRFC2253(true);
 		}
 
 		return m_canonicalString;
 	}
 
 	/**
 	 * Contructs the X500 format string of the DN.
 	 * 
 	 * @return the X500 format string of the DN.
 	 */
 	public String constructX500() {
 		StringBuffer buf = new StringBuffer();
 
 		for (int n = 0; n < count; n++) {
 
 			buf.append('/');
 
 			buf.append((String) X509Name.DefaultSymbols.get(oids[n]));
 			buf.append('=');
 			buf.append(rdns[n]);
 		}
 
 		m_x500String = buf.toString();
 
 		return m_x500String;
 	}
 
 	/**
 	 * Contructs the RFC2253 format string of the DN.
 	 * 
 	 * @param canon
 	 *            whether to construct canonical (lowercase) version of the
 	 *            string.
 	 * 
 	 * @return the RFC2253 format string of the DN.
 	 */
 	public String constructRFC2253(boolean canon) {
 		StringBuffer buf = new StringBuffer();
 		boolean first = true;
 
 		for (int n = count -1; n >= 0; n--) {
 			if (!first) {
 				buf.append(',');
 			}
 
 			first = false;
 
 			buf.append((String) X509Name.DefaultSymbols.get(oids[n]));
 			buf.append('=');
 			buf.append(rdns[n]);
 		}
 
 		m_rfc2253String = buf.toString();
 		m_canonicalString = buf.toString().toLowerCase();
 
 		if (canon) {
 			return m_canonicalString;
 		} else {
 			return m_rfc2253String;
 		}
 	}
 
 	/**
 	 * Parses the RFC2253 format string and puts the fields into the internal
 	 * structure.
 	 * 
 	 * @param inputDN
 	 *            the string that contains the DN to parse.
 	 */
 	public void parse(String inputDN) {
 		X509Principal x509Principal = new X509Principal(true, inputDN);
 
 		oids = (DERObjectIdentifier[]) x509Principal.getOIDs().toArray(
 				new DERObjectIdentifier[] {});
 		rdns = (String[]) x509Principal.getValues().toArray(new String[0]);
 		count = oids.length;
 	}
 
 	/**
 	 * Parses the X500 format string and puts the fields into the internal
 	 * structure.
 	 * 
 	 * @param inputDN
 	 *            the string that contains the DN to parse.
 	 */
 	public void parseX500(String inputDN) {
 		String[] parts = inputDN.split("/");
 
 		if (parts.length < 2) {
 			return;
 		}
 
 		String newInput = parts[1];
 
 		for (int i = 2; i < parts.length; i++) {
 			newInput = newInput + ", " + parts[i];
 		}
 
 		X509Principal x509Principal = new X509Principal(false, newInput);
 
 		oids = (DERObjectIdentifier[]) x509Principal.getOIDs().toArray(
 				new DERObjectIdentifier[] {});
 		rdns = (String[]) x509Principal.getValues().toArray(new String[0]);
 		count = oids.length;
 	}
 
 	/**
 	 * The equals comparison of the DN with another DN. The comparison is done
 	 * using oids and rdns.
 	 * 
 	 * @param inputDN2
 	 *            The DN to compare with.
 	 * 
 	 * @return true if the DNs are equal (oids match, rdns match, count of
 	 *         fields match), false otherwise.
 	 */
 	public boolean equals(Object inputDN2) {
 		if (inputDN2 instanceof DNImpl) {
 			DNImpl dn2 = (DNImpl) inputDN2;
 
 			if (count != dn2.count) {
 				return false;
 			}
 
 			for (int n = 0; n < count; n++) {
 				if (!oids[n].equals(dn2.oids[n])) {
 					return false;
 				}
 			}
 
 			for (int n = 0; n < count; n++) {
 				if (!rdns[n].toLowerCase().equals(dn2.rdns[n].toLowerCase())) {
 					return false;
 				}
 			}
 
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Returns the RFC2253 format of the DN.
 	 * 
 	 * @return the RFC2253 format of the DN.
 	 */
 	public String toString() {
 		return getRFC2253();
 	}
 
 	/**
 	 * Returns the DN without the last CN. Throws IllegalArgumentException in
 	 * case the DN doesn't end with CN or in case the proxy checkin is used and
 	 * the DN does not end with the proxy CN.
 	 * 
 	 * @param checkProxy
 	 *            whether to check that the last CN is a proxy CN (matches
 	 *            "^((limited )*proxy|[0-9]*)$").
 	 * 
 	 * @return The DN without the last CN.
 	 */
 	public DN withoutLastCN(boolean checkProxy) {
 		if (!oids[count - 1].equals(X509Name.CN)) {
 			throw new IllegalArgumentException(
 					"Trying to remove last CN from DN that does not end in CN. DN was: "
 							+ toString());
 		}
 
 		if (checkProxy) {
 			if (!rdns[count - 1].matches("^((limited )?proxy|[0-9]*)$")) {
 				throw new IllegalArgumentException(
 						"Trying to remove the last proxy CN from DN that does not end in proxy CN. DN was: "
 								+ toString());
 			}
 		}
 
 		int newCount = count - 1;
 
 		String[] newRdns = new String[newCount];
 		DERObjectIdentifier[] newOids = new DERObjectIdentifier[newCount];
 
 		for (int n = 0; n < newCount; n++) {
 			newRdns[n] = rdns[n];
 			newOids[n] = oids[n];
 		}
 
 		return new DNImpl(newOids, newRdns, newCount);
 	}
 
 	/**
 	 * Returns the hashcode of the instance.
 	 * 
 	 * @return the hashcode.
 	 */
 	public int hashCode() {
 		return rdns.hashCode() + oids.hashCode() + count;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glite.security.util.DN#getRFC2253v2()
 	 */
 	public String getRFC2253v2() {
 		StringBuffer buf = new StringBuffer();
 		boolean first = true;
 
 		for (int n = 0; n < count; n++) {
 			if (!first) {
 				buf.append(',');
 			}
 
 			first = false;
 
 			buf.append((String) DNHandler.s_rfc2253v2Lookup.get(oids[n]));
 			buf.append('=');
 			buf.append(rdns[n]);
 		}
 
 		m_rfc2253String = buf.toString();
 
 		return m_rfc2253String;
 	}
 }
