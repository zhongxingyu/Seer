 /*
  * Copyright 2011 Jonathan Anderson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package me.footlights.core.crypto;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.InvalidKeyException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.PrivateKey;
 import java.security.SecureRandom;
 import java.security.Signature;
 import java.security.SignatureException;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateException;
 import java.util.Date;
 
 import javax.security.auth.x500.X500Principal;
 
 import org.bouncycastle.util.Arrays;
 import org.bouncycastle.x509.X509V1CertificateGenerator;
 
 import me.footlights.core.Preferences;
 import me.footlights.core.MissingParameterException;
 
 
 /** An identity under our control, which can be used to sign things. */
 class SigningIdentity extends Identity
 {
 	byte[] sign(Fingerprint fingerprint)
 		throws InvalidKeyException, NoSuchAlgorithmException, SignatureException
 	{
 		String hashAlgorithm = fingerprint.getAlgorithm().getAlgorithm();
 		String algorithm = signatureAlgorithm(hashAlgorithm, privateKey.getAlgorithm());
 
 		Signature s = Signature.getInstance(algorithm);
 		s.initSign(privateKey);
 		s.initVerify(getCertificate());
 
 		s.update(fingerprint.copyBytes());
 
 		return s.sign();
 	}
 
 	public static SigningIdentity wrap(PrivateKey key, Certificate cert)
 	{
 		return new SigningIdentity(key, cert);
 	}
 
 	/** Extract the private key for storage (must ONLY be done within the crypto package!). */
 	PrivateKey getPrivateKey() { return privateKey; }
 
 	public static Generator newGenerator() { return new Generator(); }
 	public static class Generator
 	{
 		public Generator setPrincipalName(String name) throws IOException
 		{
 			principal = new X500Principal("CN=" + name);
 			return this;
 		}
 
 		public Generator setPublicKeyType(String t) { publicKeyType = t; return this; }
 		public Generator setHashAlgorithm(String a) { hashAlgorithm = a; return this; }
 		public Generator setKeyLength(int l) { keyLength = l; return this; }
 		public Generator setValiditySeconds(int v) { validity = v; return this; }
 
 		public SigningIdentity generate()
 			throws CertificateException, InvalidKeyException, MissingParameterException,
 			       NoSuchAlgorithmException, NoSuchProviderException,
 			       SignatureException
 		{
 			if (principal == null) throw new MissingParameterException("principal name");
 
 			SecureRandom random = new SecureRandom(); // TODO: specify random?
 
 			KeyPairGenerator gen = KeyPairGenerator.getInstance(publicKeyType);
 			gen.initialize(keyLength, random);
 			KeyPair keyPair = gen.generateKeyPair();
 
 			X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
 			certGen.setPublicKey(keyPair.getPublic());
 			certGen.setSignatureAlgorithm(signatureAlgorithm(hashAlgorithm, publicKeyType));
 
 			certGen.setIssuerDN(principal);
 			certGen.setSubjectDN(principal);
 			certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
 
 			Date now = new Date();
 			certGen.setNotBefore(now);
 			certGen.setNotAfter(new Date(now.getTime() + validity));
 
 			Certificate cert = certGen.generate(keyPair.getPrivate(), random);
 
 			return new SigningIdentity(keyPair.getPrivate(), cert);
 		}
 
 		/** The self-declared identity for the certificate. */
 		private X500Principal principal;
 
 		private String publicKeyType = preferences.getString("crypto.asym.algorithm").get();
 		private String hashAlgorithm = preferences.getString("crypto.hash.algorithm").get();
 
 		private int keyLength = preferences.getInt("crypto.asym.keylen").get();
 		private int validity = preferences.getInt("crypto.cert.validity").get();
 	}
 
 
 	private static String signatureAlgorithm(String hashAlgorithm, String publicKeyType)
 		throws NoSuchAlgorithmException
 	{
 		if (hashAlgorithm.isEmpty())
 			throw new NoSuchAlgorithmException("Hash algorithm not specified");
 
 		if (publicKeyType.isEmpty())
 			throw new NoSuchAlgorithmException("Hash algorithm not specified");
 
 		return hashAlgorithm.replaceAll("-", "") + "with" + publicKeyType;
 	}
 
 	private SigningIdentity(PrivateKey key, Certificate cert)
 	{
 		super(cert);
 		this.privateKey = key;
 	}


 	@Override public boolean equals(Object other)
 	{
 		if (!super.equals(other)) return false;
 
 		if (!(other instanceof SigningIdentity)) return false;
 		SigningIdentity o = (SigningIdentity) other;
 
 		return Arrays.areEqual(privateKey.getEncoded(), o.privateKey.getEncoded());
 	}
 
 	/** Footlights-wide preferences. */
 	private static Preferences preferences = Preferences.getDefaultPreferences();
 
 	private final PrivateKey privateKey;
 }
