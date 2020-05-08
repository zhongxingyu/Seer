 package org.lastbamboo.common.amazon.ec2;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.commons.lang.StringUtils;
 import org.lastbamboo.common.http.client.HttpClientGetRequester;
 import org.lastbamboo.common.util.Base64;
 import org.lastbamboo.common.util.CandidateProvider;
 import org.lastbamboo.common.util.Pair;
 import org.lastbamboo.common.util.UriUtils;
 import org.lastbamboo.common.util.xml.XPathUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Class for accessing addresses of our EC2 servers. 
  */
 public class AmazonEc2CandidateProvider 
     implements CandidateProvider<InetAddress>
     {
     
     private static final Logger LOG = 
         LoggerFactory.getLogger(AmazonEc2CandidateProvider.class);
 
     private String m_accessKey;
     private String m_accessKeyId;
     
     private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
     
     /**
      * Creates a new {@link AmazonEc2CandidateProvider} instance using the 
      * specified Amazon access key and access key ID.
      * 
      * @param accessKeyId The access key ID.
      * @param accessKey The access key.
      */
     public AmazonEc2CandidateProvider(final String accessKeyId, 
         final String accessKey)
         {
         m_accessKeyId = accessKeyId;
         m_accessKey = accessKey;
         }
     
     /**
      * Returns the {@link InetSocketAddress}es of the instances with the
      * specified group ID.
      * 
      * @param groupId The group ID of the instances to look for.
      * @return A {@link Collection} of {@link InetAddress}es of all
      * instances matching the specified group ID.
      */
     public Collection<InetAddress> getInstanceAddresses(final String groupId)
         {
         if (StringUtils.isBlank(m_accessKeyId) || 
             StringUtils.isBlank(m_accessKey))
             {
             throw new IllegalArgumentException("Keys not set");
             }
         final HttpClientGetRequester requester = new HttpClientGetRequester();
         final List<Pair<String, String>> params = 
             new LinkedList<Pair<String,String>>();
         
         params.add (UriUtils.pair ("Action", "DescribeInstances"));
         params.add (UriUtils.pair ("AWSAccessKeyId", m_accessKeyId));
         params.add (UriUtils.pair ("SignatureVersion", 1));
         
         final String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
         final SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
         sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
         final String date = sdf.format(new Date());
         LOG.debug("Using date: {}", date);
         params.add (UriUtils.pair ("Timestamp", date));
         params.add (UriUtils.pair ("Version", "2007-08-29"));
         
         
         final String sig = calculateRfc2104Hmac(params);
         
         // Note the signature is just another parameter -- it doesn't need
         // to be alphabetized because it's not, of course, used in 
         // calculating itself.
         params.add(UriUtils.pair("Signature", sig));
         
         try
             {
             // The trailing slash is necessary in the address.
             final String body = 
                 requester.request("https://ec2.amazonaws.com/", params);
             LOG.debug("Received body");
             return extractInetAddresses(groupId, body);
             }
         catch (final Exception e)
             {
             LOG.error("Error accessing server data", e);
             return Collections.emptySet();
             }
         }
     
     private Collection<InetAddress> extractInetAddresses(
         final String groupId, final String body)
         {
         final List<InetAddress> addresses = new LinkedList<InetAddress>();
         try
             {
             final XPathUtils xPath = XPathUtils.newXPath(body);
             
             // Use XPath to select only the servers in the desired group.
             String path = 
                 "/DescribeInstancesResponse/reservationSet/" +
                 "item[groupSet/item[groupId='"+groupId+"']]/instancesSet/item/";
             
             // We can't use the public address if we're running on EC2 internally -- we
             // need to use the private one behind the NAT.
             if (AmazonEc2Utils.onEc2())
                 {
                 path += "privateDnsName";
                 }
             else
                 {
                 path += "dnsName";
                 }
             final NodeList nodes = xPath.getNodes(path);
             for (int i = 0; i < nodes.getLength(); i++)
                 {
                 final Node node = nodes.item(i);
                 final String urlString = node.getTextContent();
                 
                 // When instances are shutting down, they'll still appear here, but with
                 // blank addresses.  We have to make sure we only return public addresses.
                 if (StringUtils.isBlank(urlString))
                     {
                     LOG.debug("Not using blank address");
                     continue;
                     }
                 try
                     {
                     final InetAddress address = InetAddress.getByName(urlString);
                     addresses.add(address);
                     }
                 catch (final UnknownHostException e)
                     {
                     LOG.warn("Unknown host: "+urlString, e);
                     }
                 }
             }
         catch (final SAXException e)
             {
             LOG.error("SAX error", e);
             }
         catch (final IOException e)
             {
             LOG.error("IO error!", e);
             }
         catch (final XPathExpressionException e)
             {
             LOG.error("XPath error!!", e);
             }
         
         // Give them out in just a random order for now.
         Collections.shuffle(addresses);
         return addresses;
         }
 
     private String calculateRfc2104Hmac(
         final List<Pair<String, String>> params)
         {
         final Comparator<Pair<String, String>> comparator =  
             new Comparator<Pair<String,String>>()
             {
 
             public int compare(
                 final Pair<String, String> param1, 
                 final Pair<String, String> param2)
                 {
                 // Amazon orders without case.
                 return param1.getFirst().compareToIgnoreCase(param2.getFirst());
                 }
             };
         Collections.sort(params, comparator);
         final StringBuilder sb = new StringBuilder();
         for (final Pair<String, String> param : params)
             {
             sb.append(param.getFirst());
             sb.append(param.getSecond());
             }
         final String urlString = sb.toString();
         LOG.debug("Using string: {}", urlString);
         return calculateRfc2104Hmac(urlString);
         }
 
     /**
      * Computes RFC 2104-compliant HMAC signature.
      * 
      * @param data The data to be signed.
      * @param key The signing key.
      * @return The base64-encoded RFC 2104-compliant HMAC signature.
      */
     private String calculateRfc2104Hmac(final String data)
         {
         // get an hmac_sha1 key from the raw key bytes
         final SecretKeySpec signingKey = 
             new SecretKeySpec(this.m_accessKey.getBytes(),
                 HMAC_SHA1_ALGORITHM);
         try
             {
             final Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
             mac.init(signingKey);
 
             // compute the hmac on input data bytes
             byte[] rawHmac = mac.doFinal(data.getBytes());
 
             // base64-encode the hmac
             return Base64.encodeBytes(rawHmac);
             }
         catch (final NoSuchAlgorithmException e)
             {
             LOG.error("No algorithm", e);
             throw new RuntimeException("Bad algorithm", e);
             }
         catch (final InvalidKeyException e)
             {
             LOG.error("Bad key", e);
             throw new RuntimeException("Bad key", e);
             }
         }
     
     public InetAddress getCandidate()
         {
         final Collection<InetAddress> addresses = 
             getInstanceAddresses("sip-turn");
         return addresses.iterator().next();
         }
 
     public Collection<InetAddress> getCandidates()
         {
         return getInstanceAddresses("sip-turn");
         }
     }
