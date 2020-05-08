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
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.UnrecoverableEntryException;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateException;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import com.google.common.collect.Maps;
 
 import me.footlights.core.Preferences;
 import me.footlights.core.data.Link;
 
 
 /** Stores crypto keys. */
 public class Keychain
 {
 	public static Keychain create() { return new Keychain(); }
 
 	public void store(Fingerprint fingerprint, SigningIdentity identity)
 	{
 		if (privateKeys.containsKey(fingerprint))
 			assert identity.equals(privateKeys.get(fingerprint));
 
 		privateKeys.put(fingerprint, identity);
 	}
 
 	public void store(SecretKey key)
 	{
 		if (secretKeys.containsKey(key.getFingerprint()))
 			assert Arrays.equals(
 					key.keySpec.getEncoded(),
 					secretKeys.get(key.getFingerprint()).keySpec.getEncoded());
 
 		secretKeys.put(key.getFingerprint(), key);
 	}
 
 	public void store(Fingerprint fingerprint, SecretKey key)
 	{
 		secretKeys.put(fingerprint, key);
 	}
 
 	public Link getLink(Fingerprint fingerprint) throws NoSuchElementException
 	{
 		SecretKey key = secretKeys.get(fingerprint);
 		if (key == null)
 			throw new NoSuchElementException("Symmetric key for '" + fingerprint + "'");
 
 		return key.createLinkBuilder()
 			.setFingerprint(fingerprint)
 			.build();
 	}
 
 
 	/** Merge a KeyStore file into this Keychain. */
 	public void importKeystoreFile(InputStream input)
 		throws CertificateException, IOException, KeyStoreException,
 		       NoSuchAlgorithmException, UnrecoverableEntryException
 	{
 		importKeystoreFile(input, PREFERENCES.getString("crypto.keystore.type"));
 	}
 
 	/** Merge a KeyStore file into this Keychain. */
 	public void importKeystoreFile(InputStream input, String type)
 		throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException,
 		       UnrecoverableEntryException
 	{
 		final char[] password = getPassword().toCharArray();
 
 		KeyStore store = KeyStore.getInstance(type);
 		store.load(input, password);
 
 		Enumeration<String> aliases = store.aliases();
 		while (aliases.hasMoreElements())
 		{
 			final String alias = aliases.nextElement();
 			final String[] parts = alias.split(ALIAS_SEPARATOR);
 			if (parts.length != 3)
 				throw new KeyStoreException("Expected 'type:algorithm:hash', got '" + alias + "'");
 
 			final EntryType t;
 			try { t = EntryType.valueOf(parts[0].toUpperCase()); }
 			catch (IllegalArgumentException e)
 			{
 				throw new KeyStoreException("Invalid keystore entry type '" + parts[0] + "'");
 			}
 
 			final String fingerprintAlgorithm = parts[1];
 			final Fingerprint fingerprint =
 				Fingerprint.decode(alias.substring(alias.indexOf(ALIAS_SEPARATOR) + 1));
 
 			switch (t)
 			{
 				case PRIVATE:
 					PrivateKey key = (PrivateKey) store.getKey(alias, password);
 					Certificate cert = store.getCertificate(alias);
 					if (cert == null)
 						throw new CertificateException(
 								"No certificate stored for private key " + alias);
 
 					store(fingerprint, SigningIdentity.wrap(key, cert));
 					break;
 
 				case SECRET:
 					javax.crypto.SecretKey secretKey =
 						(javax.crypto.SecretKey) store.getKey(alias, password);
 
 					store(
 						SecretKey.newGenerator()
 							.setAlgorithm(secretKey.getAlgorithm())
 							.setFingerprintAlgorithm(fingerprintAlgorithm)
 							.setBytes(secretKey.getEncoded())
 							.generate());
 					break;
 			}
 		}
 	}
 
 
 	/** Save a Keychain to a KeyStore file. */
 	void exportKeystoreFile(OutputStream out)
 		throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException,
 		       UnrecoverableEntryException
	{
 		exportKeystoreFile(out, PREFERENCES.getString("crypto.keystore.type"));
	}
 
 	private Keychain() {}
 
 	/** Save a Keychain to a KeyStore file. */
 	void exportKeystoreFile(OutputStream out, String type)
 		throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException,
 		       UnrecoverableEntryException
 	{
 		final String password = getPassword();
 
 		KeyStore store = KeyStore.getInstance(type);
 		store.load(null, password.toCharArray());
 
 		final KeyStore.ProtectionParameter protection =
 			new KeyStore.PasswordProtection(password.toCharArray());
 
 		for (Fingerprint fingerprint : privateKeys.keySet())
 		{
 			SigningIdentity identity = privateKeys.get(fingerprint);
 			PrivateKey key = identity.getPrivateKey();
 			Certificate certChain[] = { identity.getCertificate() };
 
 			store.setEntry(
 					"private:" + fingerprint.encode(),
 					new KeyStore.PrivateKeyEntry(key, certChain),
 					protection);
 		}
 
 		for (final SecretKey secret : secretKeys.values())
 			store.setEntry(
 					"secret:" + secret.getFingerprint().encode(),
 					new KeyStore.SecretKeyEntry(secret.keySpec),
 					protection);
 
 		store.store(out, password.toCharArray());
 	}
 
 
 	// Object override
 	@Override public boolean equals(Object other)
 	{
 		if (!(other instanceof Keychain)) return false;
 
 		Keychain o = (Keychain) other;
 
 		if (privateKeys.size() != o.privateKeys.size()) return false;
 		for (Fingerprint fingerprint : privateKeys.keySet())
 		{
 			if (!o.privateKeys.containsKey(fingerprint)) return false;
 
 			SigningIdentity id = privateKeys.get(fingerprint);
 			SigningIdentity otherID = o.privateKeys.get(fingerprint);
 			if (!id.equals(otherID)) return false;
 		}
 
 		if (secretKeys.size() != o.secretKeys.size()) return false;
 		for (Fingerprint fingerprint : secretKeys.keySet())
 		{
 			if (!o.secretKeys.containsKey(fingerprint)) return false;
 
 			SecretKey key = secretKeys.get(fingerprint);
 			SecretKey otherKey = o.secretKeys.get(fingerprint);
 			if (!key.equals(otherKey)) return false;
 		}
 
 		return true;
 	}
 
 
 	/** Kinds of things that we store in a Java Keystore. */
 	private enum EntryType
 	{
 		/** A certificate. */
 		CERT,
 
 		/** A private key for a public/private keypair. */
 		PRIVATE,
 
 		/** A secret, symmetric key. */
 		SECRET,
 	}
 
 
 	/** Retrieve the keystore password from somewhere trustworthy (the user?). */
 	private final String getPassword() { return "fubar"; }
 
 
 	/** Separates the algorithm and hash in 'algorithm:hash'. */
 	private static final String	ALIAS_SEPARATOR  = ":";
 
 	/** Footlights-wide preferences. */
 	private static final Preferences PREFERENCES = Preferences.getDefaultPreferences();
 
 	/** Public/private keypairs. */
 	private final Map<Fingerprint, SigningIdentity> privateKeys = Maps.newHashMap();
 
 	/** Secret keys for decrypting blocks */
 	private final Map<Fingerprint, SecretKey> secretKeys = Maps.newHashMap();
 }
