 /*
  * Copyright (C) 2012 René Jeschke <rene_jeschke@yahoo.de>
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
 package com.github.rjeschke.neetutils;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.security.InvalidKeyException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 /**
  * Encoding and hashing funtions.
  *
  * @author René Jeschke (rene_jeschke@yahoo.de)
  */
 public final class Encode
 {
     private final static String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
 
     private Encode()
     {
 
     }
 
     /**
      * Calculates the MD5 hash of the given String using UTF-8.
      *
      * @param string
      *            String to hash
      * @return the hash
      * @throws EncodingException
      *             if MD5 or UTF-8 is not available
      */
     public final static byte[] md5(final String string)
     {
         try
         {
             return md5(string.getBytes("UTF-8"));
         }
         catch (final UnsupportedEncodingException e)
         {
             throw new EncodingException("No UTF-8 encoding available", e);
         }
     }
 
     /**
      * Calculates the MD5 hash of the given bytes.
      *
      * @param bytes
      *            Bytes to hash
      * @return the hash
      * @throws EncodingException
      *             if MD5 is not available
      */
     public final static byte[] md5(final byte[] bytes)
     {
         try
         {
             final MessageDigest md = MessageDigest.getInstance("MD5");
             return md.digest(bytes);
         }
         catch (final NoSuchAlgorithmException e)
         {
             throw new EncodingException("Generating MD5 hash failed", e);
         }
     }
 
     public final static byte[] sha1(final String string)
     {
         try
         {
             return sha1(string.getBytes("UTF-8"));
         }
         catch (final UnsupportedEncodingException e)
         {
             throw new EncodingException("No UTF-8 encoding available", e);
         }
     }
 
     public final static byte[] sha1(final byte[] bytes)
     {
         try
         {
             final MessageDigest md = MessageDigest.getInstance("SHA-1");
             return md.digest(bytes);
         }
         catch (final NoSuchAlgorithmException e)
         {
             throw new EncodingException("Generating SHA-1 hash failed", e);
         }
     }
 
     public final static byte[] sha256(final String string)
     {
         try
         {
             return sha256(string.getBytes("UTF-8"));
         }
         catch (final UnsupportedEncodingException e)
         {
             throw new EncodingException("No UTF-8 encoding available", e);
         }
     }
 
     public final static byte[] sha256(final byte[] bytes)
     {
         try
         {
             final MessageDigest md = MessageDigest.getInstance("SHA-256");
             return md.digest(bytes);
         }
         catch (final NoSuchAlgorithmException e)
         {
             throw new EncodingException("Generating SHA-256 hash failed", e);
         }
     }
 
     public final static byte[] sha512(final String string)
     {
         try
         {
             return sha512(string.getBytes("UTF-8"));
         }
         catch (final UnsupportedEncodingException e)
         {
             throw new EncodingException("No UTF-8 encoding available", e);
         }
     }
 
     public final static byte[] sha512(final byte[] bytes)
     {
         try
         {
             final MessageDigest md = MessageDigest.getInstance("SHA-512");
             return md.digest(bytes);
         }
         catch (final NoSuchAlgorithmException e)
         {
             throw new EncodingException("Generating SHA-512 hash failed", e);
         }
     }
 
     private final static int get24(final byte[] bytes, final int pos)
     {
         int ret = 0;
         if (pos < bytes.length)
         {
             ret |= (bytes[pos] & 255) << 16;
             if (pos + 1 < bytes.length)
             {
                 ret |= (bytes[pos + 1] & 255) << 8;
                 if (pos + 2 < bytes.length)
                 {
                     ret |= bytes[pos + 2] & 255;
                 }
             }
         }
         return ret;
     }
 
     public final static byte[] hmacSha1(final String key, final String data)
     {
         try
         {
             return hmacSha1(key.getBytes("UTF-8"), data.getBytes("UTF-8"));
         }
         catch (final UnsupportedEncodingException e)
         {
             throw new EncodingException("No UTF-8 encoding available", e);
         }
     }
 
     public final static byte[] hmacSha1(final String key, final byte[] bytes)
     {
         try
         {
             return hmacSha1(key.getBytes("UTF-8"), bytes);
         }
         catch (final UnsupportedEncodingException e)
         {
             throw new EncodingException("No UTF-8 encoding available", e);
         }
     }
 
     public final static byte[] hmacSha1(final byte[] key, final String data)
     {
         try
         {
             return hmacSha1(key, data.getBytes("UTF-8"));
         }
         catch (final UnsupportedEncodingException e)
         {
             throw new EncodingException("No UTF-8 encoding available", e);
         }
     }
 
     public final static byte[] hmacSha1(final byte[] key, final byte[] bytes)
     {
         try
         {
             final SecretKeySpec sks = new SecretKeySpec(key, "HmacSHA1");
             final Mac mac = Mac.getInstance("HmacSHA1");
             mac.init(sks);
             return mac.doFinal(bytes);
         }
         catch (NoSuchAlgorithmException | InvalidKeyException e)
         {
             throw new EncodingException("Generating HmacSHA1 hash failed", e);
         }
     }
 
     public final static byte[] hmacSha256(final String key, final String data)
     {
         try
         {
            return hmacSha256(key.getBytes("UTF-8"), data.getBytes("UTF-8"));
         }
         catch (final UnsupportedEncodingException e)
         {
             throw new EncodingException("No UTF-8 encoding available", e);
         }
     }
 
     public final static byte[] hmacSha256(final String key, final byte[] bytes)
     {
         try
         {
            return hmacSha256(key.getBytes("UTF-8"), bytes);
         }
         catch (final UnsupportedEncodingException e)
         {
             throw new EncodingException("No UTF-8 encoding available", e);
         }
     }
 
     public final static byte[] hmacSha256(final byte[] key, final String data)
     {
         try
         {
            return hmacSha256(key, data.getBytes("UTF-8"));
         }
         catch (final UnsupportedEncodingException e)
         {
             throw new EncodingException("No UTF-8 encoding available", e);
         }
     }
 
     public final static byte[] hmacSha256(final byte[] key, final byte[] bytes)
     {
         try
         {
             final SecretKeySpec sks = new SecretKeySpec(key, "HmacSHA256");
             final Mac mac = Mac.getInstance("HmacSHA256");
             mac.init(sks);
             return mac.doFinal(bytes);
         }
         catch (NoSuchAlgorithmException | InvalidKeyException e)
         {
             throw new EncodingException("Generating HmacSHA256 hash failed", e);
         }
     }
 
     public final static String base64(final byte[] bytes)
     {
         return base64(new StringBuilder(), bytes).toString();
     }
 
     public final static StringBuilder base64(final StringBuilder sb, final byte[] bytes)
     {
         int todo = bytes.length;
         int pos = 0;
         while (todo > 2)
         {
             final int in = get24(bytes, pos);
             sb.append(BASE64.charAt((in >> 18) & 63));
             sb.append(BASE64.charAt((in >> 12) & 63));
             sb.append(BASE64.charAt((in >> 6) & 63));
             sb.append(BASE64.charAt(in & 63));
             todo -= 3;
             pos += 3;
         }
         if (todo > 0)
         {
             final int in = get24(bytes, pos);
             sb.append(BASE64.charAt((in >> 18) & 63));
             sb.append(BASE64.charAt((in >> 12) & 63));
             if (todo == 2)
             {
                 sb.append(BASE64.charAt((in >> 6) & 63));
             }
             else
             {
                 sb.append('=');
             }
             sb.append('=');
         }
         return sb;
     }
 
     public final static String hex(final byte[] bytes)
     {
         return hex(new StringBuilder(), bytes).toString();
     }
 
     public final static StringBuilder hex(final StringBuilder sb, final byte[] bytes)
     {
         for (int i = 0; i < bytes.length; i++)
         {
             sb.append(String.format("%02x", bytes[i] & 255));
         }
         return sb;
     }
 
     public final static String hexu(final byte[] bytes)
     {
         return hexu(new StringBuilder(), bytes).toString();
     }
 
     public final static StringBuilder hexu(final StringBuilder sb, final byte[] bytes)
     {
         for (int i = 0; i < bytes.length; i++)
         {
             sb.append(String.format("%02X", bytes[i] & 255));
         }
         return sb;
     }
 
     public final static String url(final String url, final Charset charset)
     {
         return url(new StringBuilder(), url, charset).toString();
     }
 
     public final static StringBuilder url(final StringBuilder sb, final String url, final Charset charset)
     {
         final byte[] bytes = url.getBytes(charset);
 
         for (int i = 0; i < bytes.length; i++)
         {
             final int c = bytes[i] & 255;
 
             if (c < 33 | c > 126)
             {
                 sb.append(String.format("%%%02X", c));
             }
             else
             {
                 switch (c)
                 {
                 case '!':
                 case '#':
                 case '$':
                 case '&':
                 case '\'':
                 case '(':
                 case ')':
                 case '{':
                 case '}':
                 case '"':
                 case '*':
                 case '+':
                 case ',':
                 case '/':
                 case ':':
                 case ';':
                 case '=':
                 case '?':
                 case '@':
                 case '[':
                 case ']':
                     sb.append(String.format("%%%02X", c));
                     break;
                 default:
                     sb.append((char)c);
                     break;
                 }
             }
         }
 
         return sb;
     }
 
     public final static String urlPath(final String url, final Charset charset)
     {
         return urlPath(new StringBuilder(), url, charset).toString();
     }
 
     public final static StringBuilder urlPath(final StringBuilder sb, final String url, final Charset charset)
     {
         final List<String> comps = Strings.split(url, '/');
         for (int i = 0; i < comps.size(); i++)
         {
             if (i > 0)
             {
                 sb.append('/');
             }
             sb.append(url(comps.get(i), charset));
         }
         return sb;
     }
 
     public final static String html(final String str)
     {
         return html(new StringBuilder(), str).toString();
     }
 
     public final static StringBuilder html(final StringBuilder sb, final String str)
     {
         for (int i = 0; i < str.length(); i++)
         {
             final int ch = str.charAt(i);
             switch (ch)
             {
             case '<':
                 sb.append("&lt;");
                 break;
             case '>':
                 sb.append("&gt;");
                 break;
             case '"':
                 sb.append("&quot;");
                 break;
             case '\'':
                 sb.append("&#39;");
                 break;
             case '&':
                 sb.append("&amp;");
                 break;
             default:
                 sb.append((char)ch);
                 break;
             }
         }
 
         return sb;
     }
 }
