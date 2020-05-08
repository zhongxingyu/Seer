 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.genkey;
 
 import java.io.ByteArrayInputStream;
 import java.io.OutputStream;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.KeyStore;
 import java.security.MessageDigest;
 import java.security.Signature;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateFactory;
 
 import org.joe_e.file.Filesystem;
 import org.ref_send.promise.eventual.Do;
 import org.ref_send.promise.eventual.Eventual;
 import org.ref_send.promise.eventual.Task;
 import org.ref_send.var.Variable;
 import org.waterken.base32.Base32;
 import org.waterken.dns.Resource;
 import org.waterken.dns.editor.DomainMaster;
 import org.waterken.dns.editor.redirectory.Redirectory;
 import org.waterken.net.http.HTTPD;
 import org.waterken.server.Config;
 import org.waterken.server.Proxy;
 
 /**
  * A self-signed certificate generator.
  */
 final class
 GenKey {
 
     private
     GenKey() {}
     
     /**
      * The argument string is:
      * <ol>
      *  <li>cryptography strength expressed as hash length in bits</li>
      *  <li>hostname suffix</li>
      *  <li>redirectory URL</li>
      * </ol>
      * @param args  command line arguments
      */
     static public void
     main(final String[] args) throws Exception {
         final int strength = 0 < args.length ? Integer.parseInt(args[0]) : 80;
         final String suffix = 1 < args.length ? args[1] : ".yurl.net";
         final int keysize =
             80 >= strength
                 ? 1024
             : 112 >= strength
                 ? 2048
             : 128 >= strength
                 ? 3072
             : 4096;
 
         System.err.println("Generating RSA key pair...");
         System.err.println("with keysize: " + keysize);
         System.err.println("under domain: " + suffix);
 
         // generate a new key pair
         final KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
         g.initialize(keysize);
         final KeyPair p = g.generateKeyPair();
         final byte[] subjectPublicKeyInfo = p.getPublic().getEncoded();
         
         // produce the DER encoded CN field
         final String fingerprint;
         final byte[] cn; {
             
             // calculate the hostname
             final MessageDigest SHA1 = MessageDigest.getInstance("SHA-1");
             final byte[] hash = SHA1.digest(subjectPublicKeyInfo);
             final byte[] guid = new byte[strength / Byte.SIZE];
             System.arraycopy(hash, 0, guid, 0, guid.length);
             fingerprint = "y-" + Base32.encode(guid);
             final byte[] hostname = (fingerprint + suffix).getBytes("US-ASCII");
             
             System.err.println("fingerprint: " + fingerprint);
 
             final DER out = new DER(11 + hostname.length);
             out.writeValue(hostname);
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
             final byte[] start = "071007000059Z".getBytes("US-ASCII");
             final byte[] end = "491231235959Z".getBytes("US-ASCII");
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
         final Certificate cert = cf.generateCertificate(
                 new ByteArrayInputStream(certificate));
         cert.verify(p.getPublic());     // sanity check
         
         // store the private key and certificate
         System.err.println("Storing self-signed certificate...");
         final char[] password = "nopass".toCharArray();
         final KeyStore certs = KeyStore.getInstance(KeyStore.getDefaultType());
         certs.load(null, password);
         certs.setKeyEntry("mykey", p.getPrivate(), password,
                           new Certificate[] { cert });
         final OutputStream fout = Filesystem.writeNew(Config.keys);
         certs.store(fout, password);
         fout.close();
         
         // register the public key
         System.err.println("Registering the public key...");
         Proxy.init();
         final Eventual _ = Config.browser._;
         final String redirectoryURL = 2 < args.length
             ? args[2]
         : "https://y-hzpaiycw7dur5zcyena5qzq.yurl.net/-/dns/#redirectory";
         _.enqueue.run(new Task() {
            public void
            run() throws Exception {
                final Redirectory redirectory_ = (Redirectory)Config.browser.
                    connect.run(Redirectory.class, redirectoryURL);
                _.when(redirectory_.register(fingerprint),
                       new Do<DomainMaster,Void>() {
                    public Void
                    fulfill(final DomainMaster master) throws Exception {
                        Config.init("registration", master);
                        
                        // setup an IP updater
                        _.when(master.answers.grow(),
                               new Do<Variable<Resource>,Void>() {
                            public Void
                            fulfill(final Variable<Resource> a) throws Exception{
                                Config.init("ip", a);
                                
                               final int portN =
                            	   Config.read(HTTPD.class, "https").port;
                                final String port= 443 == portN ? "" : ":"+portN;
                                System.err.println(
                                    "Restart your server and visit:");
                                System.out.println(
                                    "https://" + fingerprint+suffix+port +"/");
                                return null;
                            }
                        });
                        
                        return null;
                    }
                });
            }
         });
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
                 for (int i = 0; i != bytes; ++i) {
                     writeByte(length >> (i * Byte.SIZE));
                 }
                 writeByte(0x80 | bytes);
             }
         }
     }
 }
