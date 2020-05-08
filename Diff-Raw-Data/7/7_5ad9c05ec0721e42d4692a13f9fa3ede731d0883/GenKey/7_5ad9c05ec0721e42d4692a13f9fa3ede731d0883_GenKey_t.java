 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.genkey;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.security.GeneralSecurityException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.KeyStore;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.Signature;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateFactory;
 
 import org.joe_e.Immutable;
 import org.joe_e.Token;
 import org.joe_e.array.ByteArray;
 import org.joe_e.charset.ASCII;
 import org.joe_e.file.Filesystem;
 import org.ref_send.promise.Do;
 import org.ref_send.promise.Eventual;
 import org.ref_send.promise.Receiver;
 import org.ref_send.promise.Vat;
 import org.waterken.base32.Base32;
 import org.waterken.db.Database;
 import org.waterken.db.Root;
 import org.waterken.db.Transaction;
 import org.waterken.dns.editor.Registrar;
 import org.waterken.menu.Menu;
 import org.waterken.net.http.HTTPD;
 import org.waterken.remote.http.HTTP;
 import org.waterken.remote.http.VatInitializer;
 import org.waterken.server.Settings;
 import org.waterken.syntax.Exporter;
 import org.waterken.thread.Loop;
 import org.waterken.uri.Header;
 import org.waterken.uri.URI;
 
 /**
  * A self-signed certificate generator.
  */
 /* package */ final class
 GenKey {
    private GenKey() { /**/ }
     
     static private final String defaultSuffix = ".yurl.net";
     static private final String defaultRegistrar =
         "https://sha-256-w4tp67lcenwhmxdxdb75pmikgd.yurl.net" +
         "/-/dns/#s=4eug7rn53hzrhz";
     
     /**
      * The argument string is:
      * <ol>
      *  <li>cryptography strength expressed as hash length in bits</li>
      *  <li>hash algorithm name</li>
      *  <li>hostname suffix</li>
      *  <li>redirectory URL</li>
      * </ol>
      * @param args  command line arguments
      */
     static public void
     main(final String[] args) throws Exception {
         final int strength = 0 < args.length ? Integer.parseInt(args[0]) : 80;
         final String alg = 1 < args.length ? args[1] : "SHA-256";
         final String suffix = 2 < args.length ? args[2] : defaultSuffix;
         final String registrar = 3 < args.length ? args[3] : defaultRegistrar;
         final int keysize =
             80 >= strength
                 ? 1024
             : 112 >= strength
                 ? 2048
             : 128 >= strength
                 ? 3072
             : 4096;
         System.out.println("Generating RSA key pair...");
         System.out.println("with keysize: " + keysize);
 
         final KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
         g.initialize(keysize);
         final KeyPair p = g.generateKeyPair();
         final String hostname= fingerprint(strength,alg,p.getPublic()) + suffix;
         System.out.println("for hostname: " + hostname);
                 
         final Certificate cert = certify(hostname, p);
         System.out.println("Storing self-signed certificate...");
         store(p.getPrivate(), cert);
         
         System.out.println("Registering the hostname...");
         register(hostname, registrar);
     }
     
     static private String
     fingerprint(final int strength, final String alg,
                 final PublicKey key) throws NoSuchAlgorithmException {
         return Header.toLowerCase(alg) + "-" + Base32.encode(
             MessageDigest.getInstance(alg).digest(key.getEncoded())).
                 substring(0, strength / 5 + (0 != strength % 5 ? 1 : 0));
     }
     
     static private Certificate
     certify(final String hostname,
             final KeyPair p) throws GeneralSecurityException {
         
         // produce the DER encoded CN field
         final byte[] cn; {
             final byte[] hostnameBytes = ASCII.encode(hostname);
             final DER out = new DER(11 + hostnameBytes.length);
             out.writeValue(hostnameBytes);
             out.writeLen();
             out.writeByte(0x13);
             out.writeByte(0x03);
             out.writeByte(0x04);
             out.writeByte(0x55);
             out.writeByte(0x03);
             out.writeByte(0x06);
             out.writeLen();
             out.writeByte(0x30);
             out.writeLen();
             out.writeByte(0x31);
             cn = out.toByteArray();
         }
                 
         // produce the subject Name
         final byte[] subject; {
             final DER out = new DER(2 + cn.length);
             out.writeValue(cn);
             out.writeLen();
             out.writeByte(0x30);
             subject = out.toByteArray();
         }
 
         // produce the constant fields
         final byte[] version = { (byte)0xa0, 0x03, 0x02, 0x01, 0x02 };
         final byte[] serialNumber = { 0x02, 0x04, 0x00, 0x00, 0x00, 0x01 };
         final byte[] signatureAlgorithm = {
             0x30, 0x0d, 0x06, 0x09, 0x2a, (byte)0x86, 0x48, (byte)0x86,
             (byte)0xf7, 0x0d, 0x01, 0x01, 0x05, 0x05, 0x00
         };
         final byte[] issuer = subject;
         
         // produce the validity
         final byte[] validity; {
             final byte[] start = ASCII.encode("071007000059Z");
             final byte[] end = ASCII.encode("491231235959Z");
             validity = new byte[2 + 2 + start.length + 2 + end.length];
             int i = 0;
             validity[i++] = 0x30;
             validity[i++] = (byte)(validity.length - 2);
             validity[i++] = 0x17;
             validity[i++] = (byte)start.length;
             System.arraycopy(start, 0, validity, i, start.length);
             i += start.length;
             validity[i++] = 0x17;
             validity[i++] = (byte)end.length;
             System.arraycopy(end, 0, validity, i, end.length);
             i += end.length;
         }
         
         // produce the tbsCertificate
         final byte[] tbsCertificate; {
             final byte[] subjectPublicKeyInfo = p.getPublic().getEncoded();
             final DER out = new DER(4 +
                                     version.length +
                                     serialNumber.length +
                                     signatureAlgorithm.length +
                                     issuer.length +
                                     validity.length +
                                     subject.length +
                                     subjectPublicKeyInfo.length);
             out.writeValue(subjectPublicKeyInfo);
             out.writeValue(subject);
             out.writeValue(validity);
             out.writeValue(issuer);
             out.writeValue(signatureAlgorithm);
             out.writeValue(serialNumber);
             out.writeValue(version);
             out.writeLen();
             out.writeByte(0x30);
             tbsCertificate = out.toByteArray();
         }
         
         // calculate the signature
         final Signature SHA1withRSA = Signature.getInstance("SHA1withRSA");
         SHA1withRSA.initSign(p.getPrivate());
         SHA1withRSA.update(tbsCertificate);
         final byte[] signatureBitstring = SHA1withRSA.sign();
         final byte[] signature; {
             final DER out = new DER(4 + signatureBitstring.length);
             out.writeValue(signatureBitstring);
             out.writeByte(0x00);    // number of padding bits in signature
             out.writeLen();
             out.writeByte(0x03);
             signature = out.toByteArray();
         }
         
         // produce the certificate
         final byte[] certificate; {
             final DER out = new DER(4 +
                                     tbsCertificate.length +
                                     signatureAlgorithm.length +
                                     signature.length);
             out.writeValue(signature);
             out.writeValue(signatureAlgorithm);
             out.writeValue(tbsCertificate);
             out.writeLen();
             out.writeByte(0x30);
             certificate = out.toByteArray();
         }
         
         // parse the certificate
         final CertificateFactory cf = CertificateFactory.getInstance("X.509");
         final Certificate cert =
             cf.generateCertificate(new ByteArrayInputStream(certificate));
         cert.verify(p.getPublic());     // sanity check
         
         return cert;
     }
     
     static private void
     store(final PrivateKey key,
           final Certificate cert) throws GeneralSecurityException, IOException {
         final char[] password = "nopass".toCharArray();
         final KeyStore certs = KeyStore.getInstance(KeyStore.getDefaultType());
         certs.load(null, password);
         certs.setKeyEntry("mykey", key, password, new Certificate[] { cert });
         final OutputStream fout = Filesystem.writeNew(Settings.keys);
         certs.store(fout, password);
         fout.close();
     }
     
     static private void
     register(final String hostname,
              final String redirectoryURL) throws Exception {
         final String top = VatInitializer.create(
             Settings.db(""), "dns", "http://localhost/", null,
             org.waterken.dns.editor.HostMaker.class,
             ByteArray.array(new byte[] { '[', ']' }));
         Settings.db(URI.path(top)).enter(Database.update,
                                          new Transaction<Immutable>() {
             public Immutable
             apply(final Root local) throws Exception {
                 final HTTP.Exports exports =
                     local.fetch(null, VatInitializer.exports);
                 claim(exports._, exports.export(), hostname, (Registrar)exports.
                     connect(null).apply(redirectoryURL, null, Registrar.class));
                 return new Token();
             }
         }).call();
     }
     
     static protected void
     claim(final Eventual _, final Exporter export,
           final String hostname, final Registrar redirectory_){
         class StoreRegistration extends Do<Vat<Menu<ByteArray>>,Void>
                                 implements Serializable {
             static private final long serialVersionUID = 1L;
 
             public Void
             fulfill(final Vat<Menu<ByteArray>> master) throws Exception{
                 Settings.config.init("registration", master, export);
 
                 class StoreUpdater extends Do<Receiver<ByteArray>,Void>
                                    implements Serializable {
                     static private final long serialVersionUID = 1L;
 
                     public Void
                     fulfill(final Receiver<ByteArray> a) throws Exception {
                         Settings.config.init("updateDNS", a, export);
 
                         final HTTPD https = Settings.config.read("https");
                         final String port=443==https.port ? "" : ":"+https.port;
                         System.out.println("Restart your server and visit:");
                         System.out.println("https://" + hostname + port + "/");
                         
                         _.destruct.apply(null);
                         Loop.pool.shutdown();
                         
                         return null;
                     }
                 }
                 _.when(master.top.grow(), new StoreUpdater());
                 return null;
             }
         }
         _.when(redirectory_.claim(hostname), new StoreRegistration());
     }
     
     static private class
     DER {
         private byte[] buffer;
         private int i;
         
         DER(final int estimate) {
             buffer = new byte[estimate];
             i = buffer.length;
         }
         
         byte[]
         toByteArray() {
             if (0 != i) {
                 final int n = buffer.length - i;
                 System.arraycopy(buffer, i, buffer = new byte[n], i = 0, n);
             }
             return buffer;
         }
         
         void
         writeByte(int b) {
             if (0 == i) {
                 i = buffer.length;
                 System.arraycopy(buffer, 0, buffer = new byte[2 * i], i, i);
             }
             buffer[--i] = (byte)b;
         }
         
         void
         writeValue(final byte[] v) {
             if (v.length > i) {
                 final int n = buffer.length - i;
                 final int l = 2 * (v.length + n);
                 System.arraycopy(buffer, i, buffer = new byte[l], i = l - n, n);
             }
             System.arraycopy(v, 0, buffer, i -= v.length, v.length);
         }
         
         void
         writeLen() {
             final int length = buffer.length - i;
             if (length <= 0x7F) {
                 writeByte(length);
             } else {
                 final int bytes =
                     length <= 0xFF
                         ? 1
                     : length <= 0xFFFF
                         ? 2
                     : length <= 0xFFFFFF
                         ? 3
                     : 4;
                for (int j = 0; j != bytes; ++j) {
                    writeByte(length >> (j * Byte.SIZE));
                 }
                 writeByte(0x80 | bytes);
             }
         }
     }
 }
