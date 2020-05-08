 /****************************************************************************
  * Copyright (C) 2013 HS Coburg.
  * All rights reserved.
  * Contact: ecsec GmbH (info@ecsec.de)
  *
  * This file is part of the Open eCard App.
  *
  * GNU General Public License Usage
  * This file may be used under the terms of the GNU General Public
  * License version 3.0 as published by the Free Software Foundation
  * and appearing in the file LICENSE.GPL included in the packaging of
  * this file. Please review the following information to ensure the
  * GNU General Public License version 3.0 requirements will be met:
  * http://www.gnu.org/copyleft/gpl.html.
  *
  * Other Usage
  * Alternatively, this file may be used in accordance with the terms
  * and conditions contained in a signed written agreement between
  * you and ecsec GmbH.
  *
  ***************************************************************************/
 
 package org.openecard.plugins.phrplugin.crypto;
 
 import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
 import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
 import iso.std.iso_iec._24727.tech.schema.EstablishContext;
 import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
 import iso.std.iso_iec._24727.tech.schema.ListIFDs;
 import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.cert.X509Certificate;
 import org.openecard.common.ClientEnv;
 import org.openecard.common.ECardConstants;
 import org.openecard.common.enums.EventType;
 import org.openecard.common.interfaces.Dispatcher;
 import org.openecard.common.sal.state.CardStateMap;
 import org.openecard.common.sal.state.SALStateCallback;
 import org.openecard.common.util.FileUtils;
 import org.openecard.common.util.StringUtils;
 import org.openecard.crypto.common.sal.GenericCryptoSigner;
 import org.openecard.gui.swing.SwingDialogWrapper;
 import org.openecard.gui.swing.SwingUserConsent;
 import org.openecard.ifd.scio.IFD;
 import org.openecard.plugins.phrplugin.CardUtils;
 import org.openecard.plugins.phrplugin.PHRPluginAction;
 import org.openecard.plugins.phrplugin.PHRPluginProperies;
 import org.openecard.plugins.ws.PHRMarshaller;
 import org.openecard.recognition.CardRecognition;
 import org.openecard.sal.TinySAL;
 import org.openecard.sal.protocol.genericcryptography.GenericCryptoProtocolFactory;
 import org.openecard.sal.protocol.pincompare.PINCompareProtocolFactory;
 import org.openecard.transport.dispatcher.MessageDispatcher;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 import org.w3c.dom.Document;
 
 
 /**
  * Test if signature generation and validation works.
  * This is done for a software certificate and for a hardware certificate (AUTN of eGK).
  * The hardware test is disabled by default because an inserted card is needed.
  * 
  * @author Dirk Petrautzki <dirk.petrautzki@hs-coburg.de>
  */
 public class SignatureUtilTest {
 
     private static final byte[] ESIGN_CARD_APPLICATION = StringUtils.toByteArray("A000000167455349474E");
 
     private static ClientEnv env;
     private static TinySAL instance;
     private static CardStateMap states;
     private static IFD ifd;
     static ConnectionHandleType cHandle;
     static Dispatcher dispatcher;
 
     /**
      * Test signature generation and validation for a software certificate.
      * 
      * @throws Throwable
      */
     @Test(enabled = true)
     public void testSoftwareCertificate() throws Throwable {
 	// validate an already signed example document
 	InputStream is = FileUtils.resolveResourceAsStream(SignatureUtilTest.class, "crypto/SignedCapabilityList.xml");
 	PHRMarshaller phrMarshaller = new PHRMarshaller();
 	Document d = phrMarshaller.str2doc(is);
 	boolean valid = false;
 	valid = SignatureUtil.validate(d);
 	Assert.assertTrue(valid);
 
 	// load file into keystore and get private key and certificate
 	KeyStore keyStore = KeyStore.getInstance("PKCS12");
 	PHRPluginProperies.loadProperties();
 	InputStream fis2 = FileUtils.resolveResourceAsStream(PHRPluginAction.class,
		"ID4Health_EPA-Client_SSL.p12");
	char[] password = "test123.".toCharArray();
 	keyStore.load(fis2, password);
 	fis2.close();
 	KeyStore.PasswordProtection param = new KeyStore.PasswordProtection(password);
	String alias = "id4health epa-client ssl";
 	KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
 		alias, param);
 	PrivateKey myPrivateKey = pkEntry.getPrivateKey();
 	X509Certificate softCert = (X509Certificate) keyStore.getCertificateChain(alias)[0];
 
 	// sign document and validate the signature
 	Document assertion = buildToBeSignedDocument();
 	boolean signSuccessfull = SignatureUtil.sign(assertion, softCert, myPrivateKey);
 	Assert.assertTrue(signSuccessfull);
 	valid = SignatureUtil.validate(assertion);
 	Assert.assertTrue(valid);
     }
 
     /**
      * Test signature generation and validation for a hardware certificate (AUTN eGK).
      * 
      * @throws Throwable
      */
     @Test(enabled = false)
     public void testHardwareCertificate() throws Throwable {
 	// setup minimalistic test client
 	setUpClient();
 
 	// sign document and validate the signature
 	boolean valid = false;
 	Document assertion = buildToBeSignedDocument();
 	GenericCryptoSigner signer = new GenericCryptoSigner(dispatcher, cHandle, "PrK.CH.AUTN");
 	boolean signSuccessfull = SignatureUtil.sign(assertion, signer);
 	Assert.assertTrue(signSuccessfull);
 	valid = SignatureUtil.validate(assertion);
 	Assert.assertTrue(valid);
     }
 
     /**
      * Sets up a minimalistic test client, that will only know about the card in the first slot of the first reader.
      * 
      * @throws Exception
      */
     private void setUpClient() throws Exception {
 	env = new ClientEnv();
 	dispatcher = new MessageDispatcher(env);
 	env.setDispatcher(dispatcher);
 	ifd = new IFD();
 	ifd.setGUI(new SwingUserConsent(new SwingDialogWrapper()));
 	env.setIFD(ifd);
 	states = new CardStateMap();
 
 	EstablishContextResponse ecr = env.getIFD().establishContext(new EstablishContext());
 	CardRecognition cr = new CardRecognition(ifd, ecr.getContextHandle());
 	ListIFDs listIFDs = new ListIFDs();
 
 	listIFDs.setContextHandle(ecr.getContextHandle());
 	ListIFDsResponse listIFDsResponse = ifd.listIFDs(listIFDs);
 	RecognitionInfo recognitionInfo = cr.recognizeCard(listIFDsResponse.getIFDName().get(0), new BigInteger("0"));
 	SALStateCallback salCallback = new SALStateCallback(cr, states);
 
 	cHandle = new ConnectionHandleType();
 	cHandle.setContextHandle(ecr.getContextHandle());
 	cHandle.setRecognitionInfo(recognitionInfo);
 	cHandle.setIFDName(listIFDsResponse.getIFDName().get(0));
 	cHandle.setSlotIndex(new BigInteger("0"));
 
 	salCallback.signalEvent(EventType.CARD_RECOGNIZED, cHandle);
 	instance = new TinySAL(env, states);
 	env.setSAL(instance);
 
 	instance.addProtocol(ECardConstants.Protocol.GENERIC_CRYPTO, new GenericCryptoProtocolFactory());
 	instance.addProtocol(ECardConstants.Protocol.PIN_COMPARE, new PINCompareProtocolFactory());
 	org.openecard.plugins.phrplugin.CardUtils.authenticatePINHome(cHandle, dispatcher, states);
 	cHandle = CardUtils.connectToCardApplication(cHandle, ESIGN_CARD_APPLICATION, dispatcher);
     }
 
     /**
      * Build an example document that will be used for signature testing.
      * 
      * @return {@link Document}
      * @throws Throwable
      */
     private Document buildToBeSignedDocument() throws Throwable {
 	int issuedTokenTimeout = 172800000; // 48 hours
 	String subjectCCHAUTN = "CN=851221afcae8226c55625b309208d58823b81ec0,OU=Institutionsknz,O=Herausgeber,C=DE";
 	String subjectPseudonymCCHAUTN = "2K/UlIm3RWuKN2X458Gfdgjkl345hf872ED2qw754F3heqwztTGUB3h";
 	String providerURL = "https://ehealth-g1.fokus.fraunhofer.de/epa";
 	String subjectCHCIOSIG = "1-1x25sd-dsds"; // SMC-B-TelematikID from
 						  // Admission/Admissions/ProfessionInfo/registrationNumber
 	String recordId = "sdfgjhg562456hj256jh2g56jh2g56jh";
 	String policyIdReference = "urn:fhg:fokus:epa:names:xacml:2.0:default:policyid:read-write-all";
 
 	Document assertion = IssueAccessAssertionContract.buildAccessAssertion(issuedTokenTimeout, subjectCHCIOSIG,
 		subjectCCHAUTN, providerURL, subjectPseudonymCCHAUTN, recordId, policyIdReference);
 	return assertion;
     }
 
 }
