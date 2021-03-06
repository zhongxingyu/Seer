 package org.bouncycastle.asn1.test;
 
 import org.bouncycastle.asn1.ASN1InputStream;
 import org.bouncycastle.asn1.ASN1OutputStream;
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.ASN1Set;
 import org.bouncycastle.asn1.DEREncodable;
 import org.bouncycastle.asn1.DERGeneralizedTime;
 import org.bouncycastle.asn1.DERIA5String;
 import org.bouncycastle.asn1.DERObjectIdentifier;
 import org.bouncycastle.asn1.DERPrintableString;
 import org.bouncycastle.asn1.DERUTF8String;
 import org.bouncycastle.asn1.x509.X509DefaultEntryConverter;
 import org.bouncycastle.asn1.x509.X509Name;
 import org.bouncycastle.util.Arrays;
 import org.bouncycastle.util.encoders.Hex;
 import org.bouncycastle.util.test.SimpleTest;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Vector;
 
 public class X509NameTest
     extends SimpleTest
 {
     // TODO add rfc compliance test for default encoder.
    String[] subjects =
    {
        "C=AU,ST=Victoria,L=South Melbourne,O=Connect 4 Pty Ltd,OU=Webserver Team,CN=www2.connect4.com.au,E=webmaster@connect4.com.au",
        "C=AU,ST=Victoria,L=South Melbourne,O=Connect 4 Pty Ltd,OU=Certificate Authority,CN=Connect 4 CA,E=webmaster@connect4.com.au",
        "C=AU,ST=QLD,CN=SSLeay/rsa test cert",
        "C=US,O=National Aeronautics and Space Administration,SN=16+CN=Steve Schoch",
        "E=cooke@issl.atl.hp.com,C=US,OU=Hewlett Packard Company (ISSL),CN=Paul A. Cooke",
        "O=Sun Microsystems Inc,CN=store.sun.com",
        "unstructuredAddress=192.168.1.33,unstructuredName=pixfirewall.ciscopix.com,CN=pixfirewall.ciscopix.com"
     };
 
     public String getName()
     {
         return "X509Name";
     }
     
     private static X509Name fromBytes(
         byte[]  bytes) 
         throws IOException
     {
         return X509Name.getInstance(new ASN1InputStream(new ByteArrayInputStream(bytes)).readObject());
     }
 
     private DEREncodable createEntryValue(DERObjectIdentifier oid, String value)
     {
         Hashtable attrs = new Hashtable();
 
         attrs.put(oid, value);
 
         X509Name name = new X509Name(attrs);
 
         ASN1Sequence seq = (ASN1Sequence)name.getDERObject();
         ASN1Set set = (ASN1Set)seq.getObjectAt(0);
         seq = (ASN1Sequence)set.getObjectAt(0);
 
         return seq.getObjectAt(1);
     }
 
 
     private void testEncodingPrintableString(DERObjectIdentifier oid, String value)
     {
         DEREncodable converted = createEntryValue(oid, value);
         if (!(converted instanceof DERPrintableString))
         {
             fail("encoding for " + oid + " not printable string");
         }
     }
 
     private void testEncodingIA5String(DERObjectIdentifier oid, String value)
     {
         DEREncodable converted = createEntryValue(oid, value);
         if (!(converted instanceof DERIA5String))
         {
             fail("encoding for " + oid + " not IA5String");
         }
     }
 
 
     private void testEncodingGeneralizedTime(DERObjectIdentifier oid, String value)
     {
         DEREncodable converted = createEntryValue(oid, value);
         if (!(converted instanceof DERGeneralizedTime))
         {
             fail("encoding for " + oid + " not GeneralizedTime");
         }
     }
 
     public void performTest()
         throws Exception
     {
         testEncodingPrintableString(X509Name.C, "AU");
         testEncodingPrintableString(X509Name.SERIALNUMBER, "123456");
         testEncodingIA5String(X509Name.EmailAddress, "test@test.com");
         testEncodingIA5String(X509Name.DC, "test");
         testEncodingGeneralizedTime(X509Name.DATE_OF_BIRTH, "20020122122220Z");
 
         //
         // composite
         //
         Hashtable                   attrs = new Hashtable();
 
         attrs.put(X509Name.C, "AU");
         attrs.put(X509Name.O, "The Legion of the Bouncy Castle");
         attrs.put(X509Name.L, "Melbourne");
         attrs.put(X509Name.ST, "Victoria");
         attrs.put(X509Name.E, "feedback-crypto@bouncycastle.org");
 
         X509Name    name1 = new X509Name(attrs);
 
         if (!name1.equals(name1))
         {
             fail("Failed same object test");
         }
 
         X509Name    name2 = new X509Name(attrs);
 
         if (!name1.equals(name2))
         {
             fail("Failed same name test");
         }
 
         Vector  ord1 = new Vector();
 
         ord1.addElement(X509Name.C);
         ord1.addElement(X509Name.O);
         ord1.addElement(X509Name.L);
         ord1.addElement(X509Name.ST);
         ord1.addElement(X509Name.E);
 
         Vector  ord2 = new Vector();
 
         ord2.addElement(X509Name.E);
         ord2.addElement(X509Name.ST);
         ord2.addElement(X509Name.L);
         ord2.addElement(X509Name.O);
         ord2.addElement(X509Name.C);
 
         name1 = new X509Name(ord1, attrs);
         name2 = new X509Name(ord2, attrs);
 
         if (!name1.equals(name2))
         {
             fail("Failed reverse name test");
         }
 
         ord2 = new Vector();
 
         ord2.addElement(X509Name.ST);
         ord2.addElement(X509Name.ST);
         ord2.addElement(X509Name.L);
         ord2.addElement(X509Name.O);
         ord2.addElement(X509Name.C);
 
         name1 = new X509Name(ord1, attrs);
         name2 = new X509Name(ord2, attrs);
 
         if (name1.equals(name2))
         {
             fail("Failed different name test");
         }
 
         ord2 = new Vector();
 
         ord2.addElement(X509Name.ST);
         ord2.addElement(X509Name.L);
         ord2.addElement(X509Name.O);
         ord2.addElement(X509Name.C);
 
         name1 = new X509Name(ord1, attrs);
         name2 = new X509Name(ord2, attrs);
 
         if (name1.equals(name2))
         {
             fail("Failed subset name test");
         }
         
         //
         // composite test
         //
         byte[]  enc = Hex.decode("305e310b300906035504061302415531283026060355040a0c1f546865204c6567696f6e206f662074686520426f756e637920436173746c653125301006035504070c094d656c626f75726e653011060355040b0c0a4173636f742056616c65");
         ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(enc));
 
         X509Name    n = X509Name.getInstance(aIn.readObject());
 
         if (!n.toString().equals("C=AU,O=The Legion of the Bouncy Castle,L=Melbourne+OU=Ascot Vale"))
         {
             fail("Failed composite to string test");
         }
 
         n = new X509Name("C=AU, O=The Legion of the Bouncy Castle, L=Melbourne + OU=Ascot Vale");
 
         ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
         ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
 
         aOut.writeObject(n);
 
         byte[]  enc2 = bOut.toByteArray();
 
         if (!Arrays.areEqual(enc, enc2))
         {
             fail("Failed composite string to encoding test");
         }
 
         //
         // getValues test
         //
         Vector v1 = n.getValues(X509Name.O);
 
        if (v1.size() != 1 && v1.elementAt(0).equals("The Legion of the Bouncy Castle"))
         {
             fail("O test failed");
         }
 
         Vector v2 = n.getValues(X509Name.L);
 
        if (v2.size() != 1 && v2.elementAt(0).equals("Melbourne"))
         {
             fail("L test failed");
         }
 
         //
         // general subjects test
         //
         for (int i = 0; i != subjects.length; i++)
         {
             X509Name    name = new X509Name(subjects[i]);
 
             bOut = new ByteArrayOutputStream();
             aOut = new ASN1OutputStream(bOut);
 
             aOut.writeObject(name);
 
             aIn = new ASN1InputStream(new ByteArrayInputStream(bOut.toByteArray()));
 
             name = X509Name.getInstance(aIn.readObject());
 
             if (!name.toString().equals(subjects[i]))
             {
                 fail("failed regeneration test " + i);
             }
         }
 
         //
         // sort test
         //
         X509Name unsorted = new X509Name("SN=BBB + CN=AA");
 
         if (!fromBytes(unsorted.getEncoded()).toString().equals("CN=AA+SN=BBB"))
         {
             fail("failed sort test 1");
         }
 
         unsorted = new X509Name("CN=AA + SN=BBB");
 
         if (!fromBytes(unsorted.getEncoded()).toString().equals("CN=AA+SN=BBB"))
         {
             fail("failed sort test 2");
         }
 
         unsorted = new X509Name("SN=B + CN=AA");
 
         if (!fromBytes(unsorted.getEncoded()).toString().equals("SN=B+CN=AA"))
         {
             fail("failed sort test 3");
         }
 
         unsorted = new X509Name("CN=AA + SN=B");
 
         if (!fromBytes(unsorted.getEncoded()).toString().equals("SN=B+CN=AA"))
         {
             fail("failed sort test 4");
         }
 
         //
         // this is contrived but it checks sorting of sets with equal elements
         //
         unsorted = new X509Name("CN=AA + CN=AA + CN=AA");
 
         //
         // tagging test - only works if CHOICE implemented
         //
         /*
         ASN1TaggedObject tag = new DERTaggedObject(false, 1, new X509Name("CN=AA"));
 
         if (!tag.isExplicit())
         {
             fail("failed to explicitly tag CHOICE object");
         }
 
         X509Name name = X509Name.getInstance(tag, false);
 
         if (!name.equals(new X509Name("CN=AA")))
         {
             fail("failed to recover tagged name");
         }
         */
 
 
 
         DERUTF8String testString = new DERUTF8String("The Legion of the Bouncy Castle");
         byte[] encodedBytes = testString.getEncoded();
         byte[] hexEncodedBytes = Hex.encode(encodedBytes);
         String hexEncodedString = "#" + new String(hexEncodedBytes);
 
         DERUTF8String converted = (DERUTF8String)
             new X509DefaultEntryConverter().getConvertedValue(
                 X509Name.L , hexEncodedString);
 
         if (!converted.equals(testString))
         {
             fail("failed X509DefaultEntryConverter test");
         }
     }
 
     public static void main(
         String[]    args)
     {
         runTest(new X509NameTest());
     }
 }
