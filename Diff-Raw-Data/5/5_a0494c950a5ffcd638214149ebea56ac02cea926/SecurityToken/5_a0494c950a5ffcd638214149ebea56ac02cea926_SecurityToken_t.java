 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ****************************************************************************/
 
 package org.nchelp.meteor.security;
 
 import java.net.UnknownHostException;
 import java.security.PrivateKey;
 import java.security.cert.X509Certificate;
 import java.util.Date;
 
 import org.apache.xml.security.Init;
 import org.apache.xml.security.c14n.CanonicalizationException;
 import org.apache.xml.security.c14n.Canonicalizer;
 import org.apache.xml.security.c14n.InvalidCanonicalizerException;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.security.saml.Assertion;
 import org.nchelp.meteor.security.saml.AssertionFactory;
 import org.nchelp.meteor.security.saml.SAMLUtil;
 import org.nchelp.meteor.security.saml.Statement;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.ParameterException;
 import org.nchelp.meteor.util.exception.ParsingException;
 import org.nchelp.meteor.util.exception.SignatureException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
 * This is the object wrapper for the security that will be 
 * passed from access provider to index provider to data provider
 * 
 * @version   $Revision$ $Date$
 * @since     Meteor1.0
 * 
 */
 public class SecurityToken {
 
 	public static final String roleFAA = "FAA";
 	public static final String roleBORROWER = "BORROWER";
 	public static final String roleAPCSR = "APCSR";
 
 	/**
 	 * @deprecated Please use SecurityToken.roleBORROWER instead
 	 */
 	public static final String roleSTUDENT = "STUDENT";
 
 	private final Logger log = Logger.create(this.getClass());
 
 	private String userid = "";
 	private String institutionID = "";
 	private int defaultLevel;
 	private int currentLevel = 0;
 	private String authenticationProcessID = "";
 
 	// Default it to Student role since that should have
 	// less permissions
 	private String role = "";
 
 	private Assertion assertion;
 	private static final String SECURITY_DOMAIN = "nchelp.org/meteor";
 	private boolean signed = false;
 
 	// These variables must *never* be serialized
 	// It is a huge security risk to do so.
 	private transient PrivateKey privateKey;
 	private transient X509Certificate certificate;
 	private transient Document signedToken;
 
 	public SecurityToken() {
 		this.defaultLevel = 1;
 
 	}
 
 	/**
 	 * Method SecurityToken.
 	 * @param token XML representing a SecurityToken object
 	 */
 	public SecurityToken(String token) throws ParsingException {
 		// This will be a directory service lookup
 		this.defaultLevel = 1;
 
 		try {
 			Document doc = XMLParser.parseXML(token);
 			this.init(doc);
 		} catch (ParsingException e) {
 			log.error(e);
 			throw e;
 		}
 
 	}
 
 	public SecurityToken(Document token) {
 		// This will be a directory service lookup
 		this.defaultLevel = 1;
 
 		this.init(token);
 	}
 
 	private void init(Document doc) {
 		try {
 			if (Signature.isSigned(doc)) {
 				this.signedToken = doc;
 				this.signed = true;
 			}
 		} catch (SignatureException e) {
 			// then the doc isn't signed right!
 			this.signed = false;
 		}
 
 		assertion = new Assertion();
 		Element rootNode = doc.getDocumentElement();
 		NodeList nl = rootNode.getChildNodes();
 
 		for (int n = 0; n < nl.getLength(); n++) {
 			Node node = nl.item(n);
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				if ("Assertion".equals(node.getLocalName())) {
 					assertion.deserialize((Element) node);
 					break;
 				}
 			}
 		}
 
 		this.role = assertion.getAttributeValue("Role", SECURITY_DOMAIN);
 		this.currentLevel =
 			Integer.parseInt(
 				assertion.getAttributeValue("Level", SECURITY_DOMAIN));
 		this.authenticationProcessID =
 			assertion.getAttributeValue(
 				"AuthenticationProcessID",
 				SECURITY_DOMAIN);
 		this.userid =
 			assertion.getAttributeValue("UserHandle", SECURITY_DOMAIN);
 		Statement statement = assertion.getStatement(0);
 		try {
 			this.institutionID =
 				statement.getSubject().getNameIdentifier(0).getName();
 		} catch (NullPointerException e) {
 			// Don't need to do anything here
 			// If there is a problem getting to the name
 			// then we can just assume that it isn't there 
 		}
 	}
 
 	/**
 	 * Method getDefaultAuthLevel.
 	 * @return int Default Level
 	 */
 	public int getDefaultAuthLevel() {
 		return this.defaultLevel;
 	}
 
 	public int getCurrentAuthLevel() {
 		return this.currentLevel;
 	}
 
 	public void setCurrentAuthLevel(int newLevel) throws SignatureException {
 		if (this.signed) {
 			throw new SignatureException("Attributes cannot be modified after document is signed");
 		}
 
 		if (newLevel > this.currentLevel) {
 			this.currentLevel = newLevel;
 		}
 	}
 
 	/**
 	 * Gets the mode of the person inquiring.
 	 * @return Returns a String
 	 */
 	public String getRole() {
 		return role;
 	}
 
 	/**
 	 * Sets the mode of the person inquiring.
 	 * @param mode The mode to set
 	 */
 	public void setRole(String role)
 		throws ParameterException, SignatureException {
 		if (this.signed) {
 			throw new SignatureException("Attributes cannot be modified after document is signed");
 		}
 
 		if (roleFAA.equalsIgnoreCase(role)) {
 			this.role = roleFAA;
 		} else if (roleBORROWER.equalsIgnoreCase(role)) {
 			this.role = roleBORROWER;
 		} else {
 			throw new ParameterException("Invalid Role: " + role);
 		}
 	}
 
 	/**
 	 * Gets the userid.
 	 * @return Returns a String
 	 */
 	public String getUserid() {
 		return userid;
 	}
 
 	/**
 	 * Sets the userid.
 	 * @param userid The userid to set
 	 */
 	public void setUserid(String userid) throws SignatureException {
 		if (this.signed) {
 			throw new SignatureException("Attributes cannot be modified after document is signed");
 		}
 
 		this.userid = userid;
 	}
 
 	/**
 	 * Gets the authenticationProcessID.
 	 * @return Returns a String
 	 */
 	public String getAuthenticationProcessID() {
 		return authenticationProcessID;
 	}
 
 	/**
 	 * Sets the authenticationProcessID.
 	 * @param authenticationProcessID The authenticationProcessID to set
 	 */
 	public void setAuthenticationProcessID(String authenticationProcessID)
 		throws SignatureException {
 		if (this.signed) {
 			throw new SignatureException("Attributes cannot be modified after document is signed");
 		}
 		this.authenticationProcessID = authenticationProcessID;
 	}
 
 	/**
 	 * Set the X.509 Private Key into this object.  If the private key
 	 * is set, then when this object is serialized the assertion
 	 * will be signed.
 	 * <b>Note:</b> There is no getPrivateKey() method.  That is by
 	 * design.  For security purposes, there should not ever be a way 
 	 * to get that back out of this object
 	 * 
 	 * @param privateKey
 	 * @throws SignatureException
 	 */
 	public void setPrivateKey(PrivateKey privateKey)
 		throws SignatureException {
 		if (this.signed) {
 			throw new SignatureException("Attributes cannot be modified after document is signed");
 		}
 		this.privateKey = privateKey;
 	}
 
 	public void setCertificate(X509Certificate certificate)
 		throws SignatureException {
 		if (this.signed) {
 			throw new SignatureException("Attributes cannot be modified after document is signed");
 		}
 		this.certificate = certificate;
 	}
 
 	public void sign() {
 		this.signed = true;
 	}
 
 	public boolean signed() {
 		return this.signed;
 	}
 
 	public Document toXML() throws SignatureException {
 		if (this.signedToken == null) {
 			if (this.assertion == null)
 				assertion = this.createAssertion();
 
 			this.signedToken = SAMLUtil.newDocument();
 			Element e = this.signedToken.createElement("AssertionSpecifier");
 			this.signedToken.appendChild(e);
 
 			assertion.serialize(e);
 
 			if (this.signed || this.privateKey != null) {
 				Signature.sign(
 					this.signedToken,
 					this.privateKey,
 					this.certificate);
 			}
 		}
 
 		return this.signedToken;
 	}
 
 	public String toString() {
 		Document doc = null;
 
 		try {
 			doc = this.toXML();
 		} catch (SignatureException e) {
 			log.error("Error in SecurityToken.toString()", e);
 			return "";
 		}
 
 		//ByteArrayOutputStream os = new ByteArrayOutputStream();
 		//XMLUtils.outputDOMc14nWithComments(doc, os);
 		//String strToken = os.toString();
 
 		String strToken = null;
 
 		try {
 			Init.init();
 			Canonicalizer c =
 				Canonicalizer.getInstance(
 					Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS);
 			strToken = new String(c.canonicalizeSubtree(doc));
 		} catch (InvalidCanonicalizerException e) {
			log.error("InvalidCanonicalizerException", e);
 		} catch (CanonicalizationException e) {
			log.error("CanonicalizationException", e);
 		}
 
 		return strToken;
 	}
 
 	private Assertion createAssertion() {
 		String hostAddress = null;
 		String hostDomain = null;
 
 		try {
 			hostAddress = java.net.InetAddress.getLocalHost().getHostAddress();
 			hostDomain = java.net.InetAddress.getLocalHost().getHostName();
 		} catch (UnknownHostException e) {
 			// If this throws an exception, then just use null
 		}
 
 		Assertion assertion =
 			AssertionFactory.newInstance(
 				new String(new Long(System.currentTimeMillis()).toString()),
 				SECURITY_DOMAIN,
 				new Date(),
 				this.institutionID,
 				SECURITY_DOMAIN,
 				"http://nchelp.org",
 				new Date(),
 				hostAddress,
 				hostDomain);
 
 		assertion.setAttributeValue("Role", SECURITY_DOMAIN, this.role);
 		assertion.setAttributeValue(
 			"Level",
 			SECURITY_DOMAIN,
 			Integer.toString(this.currentLevel));
 		assertion.setAttributeValue(
 			"AuthenticationProcessID",
 			SECURITY_DOMAIN,
 			this.authenticationProcessID);
 		assertion.setAttributeValue(
 			"UserHandle",
 			SECURITY_DOMAIN,
 			this.userid);
 
 		return assertion;
 	}
 
 	/**
 	 * Returns the institutionID.
 	 * @return String
 	 */
 	public String getInstitutionID() {
 		return institutionID;
 	}
 
 	/**
 	 * Sets the institutionID.
 	 * @param institutionID The institutionID to set
 	 */
 	public void setInstitutionID(String institutionID)
 		throws SignatureException {
 		if (this.signed) {
 			throw new SignatureException("Attributes cannot be modified after document is signed");
 		}
 
 		this.institutionID = institutionID;
 	}
 
 	/**
 	 * Returns the assertion.
 	 * @return Assertion
 	 */
 	public Assertion getAssertion() {
 		return assertion;
 	}
 
 	/**
 	 * Sets the assertion.
 	 * @param assertion The assertion to set
 	 */
 	public void setAssertion(Assertion assertion) {
 		this.assertion = assertion;
 	}
 
 }
