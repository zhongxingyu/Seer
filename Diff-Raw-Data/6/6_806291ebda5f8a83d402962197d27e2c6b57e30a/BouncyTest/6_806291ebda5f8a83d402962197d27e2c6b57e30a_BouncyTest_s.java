 package com.lucasian.crypt.signer.bouncy.test;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.KeyFactory;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.Security;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.security.spec.PKCS8EncodedKeySpec;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import org.bouncycastle.asn1.ASN1InputStream;
 import org.bouncycastle.asn1.ASN1ObjectIdentifier;
 import org.bouncycastle.asn1.ASN1OctetString;
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.DERObject;
 import org.bouncycastle.asn1.DERSequence;
 import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
 import org.bouncycastle.asn1.pkcs.EncryptionScheme;
 import org.bouncycastle.asn1.pkcs.PBES2Parameters;
 import org.bouncycastle.asn1.pkcs.PBKDF2Params;
 import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
 import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
 import org.bouncycastle.asn1.pkcs.RC2CBCParameter;
 import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
 import org.bouncycastle.crypto.BufferedBlockCipher;
 import org.bouncycastle.crypto.CipherParameters;
 import org.bouncycastle.crypto.PBEParametersGenerator;
 import org.bouncycastle.crypto.engines.DESedeEngine;
 import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
 import org.bouncycastle.crypto.modes.CBCBlockCipher;
 import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
 import org.bouncycastle.crypto.params.ParametersWithIV;
 import org.bouncycastle.jce.X509Principal;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.operator.ContentSigner;
 import org.bouncycastle.operator.ContentVerifier;
 import org.bouncycastle.operator.ContentVerifierProvider;
 import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
 import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
 import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
 import org.bouncycastle.util.encoders.Hex;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.lucasian.crypt.signer.CertData;
 import com.lucasian.crypt.signer.Signer;
 import com.lucasian.crypt.signer.bouncy.BouncySigner;
 
 
 public class BouncyTest {
 
	private String theCert = "/home/gerardo/Documents/mua/Certificados/soma770305e38.cer";
	private String theKey = "/home/gerardo/Documents/mua/Certificados/SOMA770305E38_1110120927.key";
	private String thePassword = "S1SqbxD1n3";
 
 	@BeforeClass
 	public static void preload() {
 		BouncyCastleProvider provider = new BouncyCastleProvider();
 		Security.addProvider(provider);
 	}
 
 	@Test
 	public void testGetDataOther() throws Exception{
 		Signer signer = new BouncySigner();		
 		CertData certData = new CertData();
 		Map<String , String> mapa = new HashMap<String, String>();
 		certData = signer.getCertData(new FileInputStream(new File(theCert)));
 		mapa = certData.getPersonData();
 		System.out.println("MAPA[" + mapa + "]");	
 		System.out.println("Serial NUmber length: " + certData.getSerialNumber().length());
 		System.out.println("Expiration Date:" + certData.getExpirationDate().toGMTString());
 		signer.verifyCert(new FileInputStream(new File(theCert)));
 	}
 	
 	@Test
 	public void testSignString() throws Exception{
 		Signer signer = new BouncySigner();		
 		String signedString = signer.sign(
 				"Anita lava la tina", 
 				new FileInputStream(new File(theCert)),
 				new FileInputStream(new File(theKey)),
 				thePassword
 				);
 		System.out.println("SIGNED STRING[" + signedString + "]");		
 		System.out.println("VALIDANDO[" + signer.validate(
 				new FileInputStream(	
 				new File(theCert)), "Anita lava la tina", signedString) + "]");
 		
 		assertTrue(signer.validate(
 				new FileInputStream(	
 				new File(theCert)), "Anita lava la tina", signedString));
 	}
 		
 	@Test
 	public void testGetData() throws Exception {
 		X509Certificate cert = readCert();
 
 		X509Principal principal = (X509Principal) cert.getSubjectDN();
 
 		Map<String, String> results = new HashMap<String, String>();
 
 		@SuppressWarnings("unchecked")
 		Vector<ASN1ObjectIdentifier> oids = principal.getOIDs();
 		@SuppressWarnings("unchecked")
 		Vector<String> value = principal.getValues();
 
 		int index = 0;
 		
 		ASN1ObjectIdentifier name = new ASN1ObjectIdentifier("2.5.4.41");
 		ASN1ObjectIdentifier curp = new ASN1ObjectIdentifier("2.5.4.5");
 		ASN1ObjectIdentifier rfc = new ASN1ObjectIdentifier("2.5.4.45");
 				
 		if(oids.contains(name)){
 			index = oids.indexOf(name);
 			results.put("name", value.get(index));
 		}
 		
 		if(oids.contains(curp)){
 			index = oids.indexOf(curp);
 			results.put("curp", value.get(index));
 		}
 		
 		if(oids.contains(rfc)){
 			index = oids.indexOf(rfc);
 			results.put("rfc", value.get(index));
 		}
 
 		//for (int i = 0; i < oids.size(); i++) {
 			// 2.5.4.41 is the name
 			// 2.5.4.5 is the CURP
 			// 2.5.4.45 is the RFC
 			//results.put(oids.get(i).getId(), value.get(i));
 			//results.put(oids.get(""), value.get(value))
 		//}
 
 		System.out.println("OIDS[" + oids + "]");
 		System.out.println("VALUES[" + value + "]");
 		System.out.println("RESULTS[" + results + "]");
 		cert.checkValidity();
 
 	}
 
 	@Test
 	public void testOtherKey() throws Exception {
 		PrivateKey pk = buildPrivateKey(new File(theKey), thePassword);
 
 		X509Certificate cert = readCert();
 		PublicKey puk = cert.getPublicKey();
 		AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder()
 				.find("SHA1withRSA");
 
 		ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA")
 				.setProvider("BC").build(pk);
 
 		String signString = "Anita lava la tina";
 
 		signer.getOutputStream().write(signString.getBytes());
 		String signedString = new String(Hex.encode(signer.getSignature()));
 		//System.out.printf("The signature of %s is %s\n", signString,
 		//signedString);
 
 		ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder()
 				.setProvider("BC").build(puk);
 
 		ContentVerifier verifier = verifierProvider.get(sigAlgId);
 		verifier.getOutputStream().write(signString.getBytes());
 	}
 
 	private PrivateKey buildPrivateKey(File file, String password)
 			throws Exception {
 		BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
 				new CBCBlockCipher(new DESedeEngine()));
 		int keySize = 192;
 
 		ASN1InputStream asn1 = new ASN1InputStream(readBytes(file));
 		DERObject der = asn1.readObject();
 		DERSequence sequence = (DERSequence) der;
 
 		// System.out.println(sequence);
 
 		PBEParametersGenerator generator = new PKCS5S2ParametersGenerator();
 
 		EncryptedPrivateKeyInfo info = new EncryptedPrivateKeyInfo(sequence);
 
 		PBES2Parameters alg = new PBES2Parameters((ASN1Sequence) info
 				.getEncryptionAlgorithm().getParameters());
 		PBKDF2Params func = (PBKDF2Params) alg.getKeyDerivationFunc()
 				.getParameters();
 		EncryptionScheme scheme = alg.getEncryptionScheme();
 
 		if (func.getKeyLength() != null) {
 			keySize = func.getKeyLength().intValue() * 8;
 		}
 
 		int iterationCount = func.getIterationCount().intValue();
 		byte[] salt = func.getSalt();
 
 		generator.init(PBEParametersGenerator.PKCS5PasswordToBytes(thePassword
 				.toCharArray()), salt, iterationCount);
 
 		CipherParameters param;
 
 		if (scheme.getAlgorithm().equals(PKCSObjectIdentifiers.RC2_CBC)) {
 			RC2CBCParameter rc2Params = new RC2CBCParameter(
 					(ASN1Sequence) scheme.getObject());
 			byte[] iv = rc2Params.getIV();
 
 			param = new ParametersWithIV(
 					generator.generateDerivedParameters(keySize), iv);
 		} else {
 			byte[] iv = ((ASN1OctetString) scheme.getObject()).getOctets();
 
 			param = new ParametersWithIV(
 					generator.generateDerivedParameters(keySize), iv);
 		}
 
 		cipher.init(false, param);
 
 		byte[] data = info.getEncryptedData();
 		byte[] out = new byte[cipher.getOutputSize(data.length)];
 		int len = cipher.processBytes(data, 0, data.length, out, 0);
 
 		len += cipher.doFinal(out, len);
 
 		ASN1InputStream asn1Out = new ASN1InputStream(out);
 
 		PrivateKeyInfo keyInfo = new PrivateKeyInfo(
 				(ASN1Sequence) asn1Out.readObject());
 
 		return KeyFactory.getInstance(
 				keyInfo.getAlgorithmId().getAlgorithm().getId(), "BC")
 				.generatePrivate(new PKCS8EncodedKeySpec(keyInfo.getEncoded()));
 	}
 
 	private byte[] readBytes(File file) throws Exception {
 		InputStream is = new FileInputStream(file);
 
 		// Get the size of the file
 		long length = file.length();
 
 		// You cannot create an array using a long type.
 		// It needs to be an int type.
 		// Before converting to an int type, check
 		// to ensure that file is not larger than Integer.MAX_VALUE.
 		if (length > Integer.MAX_VALUE) {
 			// File is too large
 		}
 
 		// Create the byte array to hold the data
 		byte[] bytes = new byte[(int) length];
 
 		// Read in the bytes
 		int offset = 0;
 		int numRead = 0;
 		while (offset < bytes.length
 				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
 			offset += numRead;
 		}
 
 		// Ensure all the bytes have been read in
 		if (offset < bytes.length) {
 			throw new IOException("Could not completely read file "
 					+ file.getName());
 		}
 
 		// Close the input stream and return bytes
 		is.close();
 		return bytes;
 	}
 
 	private X509Certificate readCert() throws Exception {
 		InputStream is = new FileInputStream(theCert);
 		X509Certificate cert;
 		try {
 			CertificateFactory cf = CertificateFactory.getInstance("X.509",
 					"BC");
 			cert = (X509Certificate) cf.generateCertificate(is);
 
 			// System.out.println(cert);
 
 		} finally {
 			if (is != null) {
 				is.close();
 			}
 
 		}
 		return cert;
 	}
 
 }
