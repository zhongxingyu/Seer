 /*
  * Copyright (c) 1998-2006 Caucho Technology -- all rights reserved
  *
  * This file is part of Resin(R) Open Source
  *
  * Each copy or derived work must preserve the copyright notice and this
  * notice unmodified.
  *
  * Resin Open Source is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * Resin Open Source is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
  * of NON-INFRINGEMENT.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Resin Open Source; if not, write to the
  *
  *   Free Software Foundation, Inc.
  *   59 Temple Place, Suite 330
  *   Boston, MA 02111-1307  USA
  *
  * @author Nam Nguyen
  */
 
 package com.caucho.quercus.lib.curl;
 
 import com.caucho.quercus.QuercusModuleException;
 import com.caucho.quercus.UnimplementedException;
 
 import com.caucho.quercus.env.Env;
 import com.caucho.quercus.env.ArrayValue;
 import com.caucho.quercus.env.BooleanValue;
 import com.caucho.quercus.env.Callback;
 import com.caucho.quercus.env.DefaultValue;
 import com.caucho.quercus.env.LongValue;
 import com.caucho.quercus.env.NullValue;
 import com.caucho.quercus.env.StringValue;
 import com.caucho.quercus.env.StringValueImpl;
 import com.caucho.quercus.env.Value;
 
 import com.caucho.quercus.lib.file.BinaryInput;
 import com.caucho.quercus.lib.file.BinaryOutput;
 import com.caucho.quercus.lib.file.FileModule;
 
 import com.caucho.quercus.module.AbstractQuercusModule;
 import com.caucho.quercus.module.NotNull;
 import com.caucho.quercus.module.Optional;
 import com.caucho.quercus.module.ReturnNullAsFalse;
 import com.caucho.quercus.module.Reference;
 
 import com.caucho.util.L10N;
 import com.caucho.util.QDate;
 import com.caucho.vfs.Path;
 import com.caucho.vfs.ReadStream;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.Map;
 
 public class CurlModule
   extends AbstractQuercusModule
 {
   private static final Logger log
     = Logger.getLogger(CurlModule.class.getName());
   private static final L10N L = new L10N(CurlModule.class);
 
   public static final int CURLOPT_AUTOREFERER                 = 1;
   public static final int CURLOPT_COOKIESESSION               = 2;
 //  public static final int CURLOPT_DNS_USE_GLOBAL_CACHE        = 3;
 //  public static final int CURLOPT_DNS_CACHE_TIMEOUT           = 4;
 //  public static final int CURLOPT_FTPSSLAUTH                  = 5;
   public static final int CURLOPT_PORT                        = 6;
   public static final int CURLOPT_FILE                        = 7;
   public static final int CURLOPT_INFILE                      = 8;
   public static final int CURLOPT_INFILESIZE                  = 9;
   public static final int CURLOPT_URL                         = 10;
   public static final int CURLOPT_PROXY                       = 11;
   public static final int CURLOPT_VERBOSE                     = 12;
   public static final int CURLOPT_HEADER                      = 13;
   public static final int CURLOPT_HTTPHEADER                  = 14;
   public static final int CURLOPT_NOPROGRESS                  = 15;
   public static final int CURLOPT_NOBODY                      = 16;
   public static final int CURLOPT_FAILONERROR                 = 17;
   public static final int CURLOPT_UPLOAD                      = 18;
   public static final int CURLOPT_POST                        = 19;
 //  public static final int CURLOPT_FTPLISTONLY                 = 20;
 //  public static final int CURLOPT_FTPAPPEND                   = 21;
   public static final int CURLOPT_NETRC                       = 22;
   public static final int CURLOPT_FOLLOWLOCATION              = 23;
 //  public static final int CURLOPT_FTPASCII                    = 24;
   public static final int CURLOPT_PUT                         = 25;
   public static final int CURLOPT_MUTE                        = 26;
   public static final int CURLOPT_USERPWD                     = 27;
   public static final int CURLOPT_PROXYUSERPWD                = 28;
   public static final int CURLOPT_RANGE                       = 29;
   public static final int CURLOPT_TIMEOUT                     = 30;
   public static final int CURLOPT_POSTFIELDS                  = 31;
   public static final int CURLOPT_REFERER                     = 32;
   public static final int CURLOPT_USERAGENT                   = 33;
 //  public static final int CURLOPT_FTPPORT                     = 34;
 //  public static final int CURLOPT_FTP_USE_EPSV                = 35;
   public static final int CURLOPT_LOW_SPEED_LIMIT             = 36;
   public static final int CURLOPT_LOW_SPEED_TIME              = 37;
 //  public static final int CURLOPT_RESUME_FROM                 = 38;
   public static final int CURLOPT_COOKIE                      = 39;
 //  public static final int CURLOPT_SSLCERT                     = 40;
 //  public static final int CURLOPT_SSLCERTPASSWD               = 41;
   public static final int CURLOPT_WRITEHEADER                 = 42;
 //  public static final int CURLOPT_SSL_VERIFYHOST              = 43;
   public static final int CURLOPT_COOKIEFILE                  = 44;
 //  public static final int CURLOPT_SSLVERSION                  = 45;
   public static final int CURLOPT_TIMECONDITION               = 46;
   public static final int CURLOPT_TIMEVALUE                   = 47;
   public static final int CURLOPT_CUSTOMREQUEST               = 48;
   public static final int CURLOPT_STDERR                      = 49;
 //  public static final int CURLOPT_TRANSFERTEXT                = 50;
   public static final int CURLOPT_RETURNTRANSFER              = 51;
 //  public static final int CURLOPT_QUOTE                       = 52;
 //  public static final int CURLOPT_POSTQUOTE                   = 53;
 //  public static final int CURLOPT_INTERFACE                   = 54;
 //  public static final int CURLOPT_KRB4LEVEL                   = 55;
   public static final int CURLOPT_HTTPPROXYTUNNEL             = 56;
 //  public static final int CURLOPT_FILETIME                    = 57;
   public static final int CURLOPT_WRITEFUNCTION               = 58;
   public static final int CURLOPT_READFUNCTION                = 59;
   public static final int CURLOPT_PASSWDFUNCTION              = 60;
   public static final int CURLOPT_HEADERFUNCTION              = 61;
   public static final int CURLOPT_MAXREDIRS                   = 62;
   public static final int CURLOPT_MAXCONNECTS                 = 63;
   public static final int CURLOPT_CLOSEPOLICY                 = 64;
   public static final int CURLOPT_FRESH_CONNECT               = 65;
   public static final int CURLOPT_FORBID_REUSE                = 66;
 //  public static final int CURLOPT_RANDOM_FILE                 = 67;
 //  public static final int CURLOPT_EGDSOCKET                   = 68;
   public static final int CURLOPT_CONNECTTIMEOUT              = 69;
 //  public static final int CURLOPT_SSL_VERIFYPEER              = 70;
 //  public static final int CURLOPT_CAINFO                      = 71;
 //  public static final int CURLOPT_CAPATH                      = 72;
   public static final int CURLOPT_COOKIEJAR                   = 73;
 //  public static final int CURLOPT_SSL_CIPHER_LIST             = 74;
   public static final int CURLOPT_BINARYTRANSFER              = 75;
   public static final int CURLOPT_NOSIGNAL                    = 76;
   public static final int CURLOPT_PROXYTYPE                   = 77;
   public static final int CURLOPT_BUFFERSIZE                  = 78;
   public static final int CURLOPT_HTTPGET                     = 79;
   public static final int CURLOPT_HTTP_VERSION                = 80;
 //  public static final int CURLOPT_SSLKEY                      = 81;
 //  public static final int CURLOPT_SSLKEYTYPE                  = 82;
 //  public static final int CURLOPT_SSLKEYPASSWD                = 83;
 //  public static final int CURLOPT_SSLENGINE                   = 84;
 //  public static final int CURLOPT_SSLENGINE_DEFAULT           = 85;
 //  public static final int CURLOPT_SSLCERTTYPE                 = 86;
 //  public static final int CURLOPT_CRLF                        = 87;
   public static final int CURLOPT_ENCODING                    = 88;
   public static final int CURLOPT_PROXYPORT                   = 89;
   public static final int CURLOPT_UNRESTRICTED_AUTH           = 90;
 //  public static final int CURLOPT_FTP_USE_EPRT                = 91;
   public static final int CURLOPT_HTTP200ALIASES              = 92;
   public static final int CURLOPT_HTTPAUTH                    = 93;
   public static final int CURLAUTH_BASIC                      = 1;
   public static final int CURLAUTH_DIGEST                     = 2;
   public static final int CURLAUTH_GSSNEGOTIATE               = 4;
   public static final int CURLAUTH_NTLM                       = 8;
   public static final int CURLAUTH_ANY                        = 15;
   public static final int CURLAUTH_ANYSAFE                    = 14;
   public static final int CURLOPT_PROXYAUTH                   = 100;
   public static final int CURLCLOSEPOLICY_LEAST_RECENTLY_USED = 101;
   public static final int CURLCLOSEPOLICY_LEAST_TRAFFIC       = 102;
   public static final int CURLCLOSEPOLICY_SLOWEST             = 103;
   public static final int CURLCLOSEPOLICY_CALLBACK            = 104;
   public static final int CURLCLOSEPOLICY_OLDEST              = 105;
   public static final int CURLINFO_EFFECTIVE_URL              = 106;
   public static final int CURLINFO_HTTP_CODE                  = 107;
   public static final int CURLINFO_HEADER_OUT                 = 108;
   public static final int CURLINFO_HEADER_SIZE                = 109;
   public static final int CURLINFO_REQUEST_SIZE               = 110;
   public static final int CURLINFO_TOTAL_TIME                 = 111;
   public static final int CURLINFO_NAMELOOKUP_TIME            = 112;
   public static final int CURLINFO_CONNECT_TIME               = 113;
   public static final int CURLINFO_PRETRANSFER_TIME           = 114;
   public static final int CURLINFO_SIZE_UPLOAD                = 115;
   public static final int CURLINFO_SIZE_DOWNLOAD              = 116;
   public static final int CURLINFO_SPEED_DOWNLOAD             = 117;
   public static final int CURLINFO_SPEED_UPLOAD               = 118;
   public static final int CURLINFO_FILETIME                   = 119;
 //  public static final int CURLINFO_SSL_VERIFYRESULT           = 120;
   public static final int CURLINFO_CONTENT_LENGTH_DOWNLOAD    = 121;
   public static final int CURLINFO_CONTENT_LENGTH_UPLOAD      = 122;
   public static final int CURLINFO_STARTTRANSFER_TIME         = 123;
   public static final int CURLINFO_CONTENT_TYPE               = 124;
   public static final int CURLINFO_REDIRECT_TIME              = 125;
   public static final int CURLINFO_REDIRECT_COUNT             = 126;
 //  public static final int CURL_VERSION_IPV6                   = 127;
 //  public static final int CURL_VERSION_KERBEROS4              = 128;
   public static final int CURL_VERSION_SSL                    = 129;
   public static final int CURL_VERSION_LIBZ                   = 130;
   public static final int CURLVERSION_NOW                     = 131;
   public static final int CURLE_OK                            = 132;
   public static final int CURLE_UNSUPPORTED_PROTOCOL          = 133;
   public static final int CURLE_FAILED_INIT                   = 134;
   public static final int CURLE_URL_MALFORMAT                 = 135;
   public static final int CURLE_URL_MALFORMAT_USER            = 136;
   public static final int CURLE_COULDNT_RESOLVE_PROXY         = 137;
   public static final int CURLE_COULDNT_RESOLVE_HOST          = 138;
   public static final int CURLE_COULDNT_CONNECT               = 139;
 //  public static final int CURLE_FTP_WEIRD_SERVER_REPLY        = 140;
 //  public static final int CURLE_FTP_ACCESS_DENIED             = 141;
 //  public static final int CURLE_FTP_USER_PASSWORD_INCORRECT   = 142;
 //  public static final int CURLE_FTP_WEIRD_PASS_REPLY          = 143;
 //  public static final int CURLE_FTP_WEIRD_USER_REPLY          = 144;
 //  public static final int CURLE_FTP_WEIRD_PASV_REPLY          = 145;
 //  public static final int CURLE_FTP_WEIRD_227_FORMAT          = 146;
 //  public static final int CURLE_FTP_CANT_GET_HOST             = 147;
 //  public static final int CURLE_FTP_CANT_RECONNECT            = 148;
 //  public static final int CURLE_FTP_COULDNT_SET_BINARY        = 149;
   public static final int CURLE_PARTIAL_FILE                  = 150;
 //  public static final int CURLE_FTP_COULDNT_RETR_FILE         = 151;
 //  public static final int CURLE_FTP_WRITE_ERROR               = 152;
 //  public static final int CURLE_FTP_QUOTE_ERROR               = 153;
   public static final int CURLE_HTTP_NOT_FOUND                = 154;
   public static final int CURLE_WRITE_ERROR                   = 155;
   public static final int CURLE_MALFORMAT_USER                = 156;
 //  public static final int CURLE_FTP_COULDNT_STOR_FILE         = 157;
   public static final int CURLE_READ_ERROR                    = 158;
   public static final int CURLE_OUT_OF_MEMORY                 = 159;
   public static final int CURLE_OPERATION_TIMEOUTED           = 160;
 //  public static final int CURLE_FTP_COULDNT_SET_ASCII         = 161;
 //  public static final int CURLE_FTP_PORT_FAILED               = 162;
 //  public static final int CURLE_FTP_COULDNT_USE_REST          = 163;
 //  public static final int CURLE_FTP_COULDNT_GET_SIZE          = 164;
   public static final int CURLE_HTTP_RANGE_ERROR              = 165;
   public static final int CURLE_HTTP_POST_ERROR               = 166;
   public static final int CURLE_SSL_CONNECT_ERROR             = 167;
 //  public static final int CURLE_FTP_BAD_DOWNLOAD_RESUME       = 168;
   public static final int CURLE_FILE_COULDNT_READ_FILE        = 169;
 //  public static final int CURLE_LDAP_CANNOT_BIND              = 170;
 //  public static final int CURLE_LDAP_SEARCH_FAILED            = 171;
   public static final int CURLE_LIBRARY_NOT_FOUND             = 172;
   public static final int CURLE_FUNCTION_NOT_FOUND            = 173;
   public static final int CURLE_ABORTED_BY_CALLBACK           = 174;
   public static final int CURLE_BAD_FUNCTION_ARGUMENT         = 175;
   public static final int CURLE_BAD_CALLING_ORDER             = 176;
   public static final int CURLE_HTTP_PORT_FAILED              = 177;
   public static final int CURLE_BAD_PASSWORD_ENTERED          = 178;
   public static final int CURLE_TOO_MANY_REDIRECTS            = 179;
 //  public static final int CURLE_UNKNOWN_TELNET_OPTION         = 180;
 //  public static final int CURLE_TELNET_OPTION_SYNTAX          = 181;
   public static final int CURLE_OBSOLETE                      = 182;
 //  public static final int CURLE_SSL_PEER_CERTIFICATE          = 183;
   public static final int CURLE_GOT_NOTHING                   = 184;
 //  public static final int CURLE_SSL_ENGINE_NOTFOUND           = 185;
 //  public static final int CURLE_SSL_ENGINE_SETFAILED          = 186;
   public static final int CURLE_SEND_ERROR                    = 187;
   public static final int CURLE_RECV_ERROR                    = 188;
   public static final int CURLE_SHARE_IN_USE                  = 189;
 //  public static final int CURLE_SSL_CERTPROBLEM               = 190;
 //  public static final int CURLE_SSL_CIPHER                    = 191;
 //  public static final int CURLE_SSL_CACERT                    = 192;
   public static final int CURLE_BAD_CONTENT_ENCODING          = 193;
 //  public static final int CURLE_LDAP_INVALID_URL              = 194;
   public static final int CURLE_FILESIZE_EXCEEDED             = 195;
 //  public static final int CURLE_FTP_SSL_FAILED                = 196;
 //  public static final int CURLFTPAUTH_DEFAULT                 = 197;
 
   // Additional constants
   public static final int CURL_TIMECOND_IFMODSINCE            = 198;
   public static final int CURL_TIMECOND_IFUNMODSINCE          = 199;
   public static final int CURL_HTTP_VERSION_NONE              = 200;
   public static final int CURL_HTTP_VERSION_1_0               = 201;
   public static final int CURL_HTTP_VERSION_1_1               = 202;
   public static final int CURLPROXY_HTTP                      = 203;
   public static final int CURLPROXY_SOCKS5                    = 204;
 
   public String []getLoadedExtensions()
   {
     return new String[] { "curl" };
   }
 
   /**
    * Closes this cURL object.
    *
    * @param env
    * @param curl
    */
   public static void curl_close(Env env,
                               @NotNull CurlResource curl)
   {
     if (curl == null)
       return;
 
     curl.close();
   }
 
   /**
    * Returns a copy of this resource.
    *
    * @param env
    * @param curl
    */
   public static CurlResource curl_copy_handle(Env env,
                               @NotNull CurlResource curl)
   {
     return curl.clone();
   }
 
   /**
    * Returns the error code from the last operation.
    *
    * @param env
    * @param curl
    */
  public static LongValue curl_errno(Env env,
                               @NotNull CurlResource curl)
   {
     return LongValue.create(curl.getErrorCode());
   }
 
   /**
    * Returns the error string from the last operation.
    *
    * @param env
    * @param curl
    */
  public static StringValue curl_error(Env env,
                               @NotNull CurlResource curl)
   {
     return new StringValueImpl(curl.getError());
   }
 
   /**
    * @param env
    * @param curl
    */
   public static Value curl_exec(Env env,
                               @NotNull CurlResource curl)
   {
     return curl.execute(env);
   }
 
   /**
    * Returns information about the last request.
    *
    * @param env
    * @param curl
    * @param option type of information to return
    */
   public static Value curl_getinfo(Env env,
                               @NotNull CurlResource curl,
                               @Optional Value option)
   {
 //    if (option instanceof DefaultValue)
 //     return curl.getAllInfo();
 
     return getInfo(env, curl, option.toInt());
   }
 
   private static Value getInfo(Env env,
                               CurlResource curl,
                               int option)
   {
     switch (option) {
       case CURLINFO_EFFECTIVE_URL:
         return new StringValueImpl(curl.getURL());
       case CURLINFO_HTTP_CODE:
         return LongValue.create(curl.getResponseCode());
       case CURLINFO_FILETIME:
         break;
       case CURLINFO_TOTAL_TIME:
         break;
       case CURLINFO_NAMELOOKUP_TIME:
         break;
       case CURLINFO_CONNECT_TIME:
         break;
       case CURLINFO_PRETRANSFER_TIME:
         break;
       case CURLINFO_STARTTRANSFER_TIME:
         break;
       case CURLINFO_REDIRECT_TIME:
         break;
       case CURLINFO_SIZE_UPLOAD:
         break;
       case CURLINFO_SIZE_DOWNLOAD:
         break;
       case CURLINFO_SPEED_DOWNLOAD:
         break;
       case CURLINFO_SPEED_UPLOAD:
         break;
       case CURLINFO_HEADER_SIZE:
         return LongValue.create(curl.getHeader().length());
       case CURLINFO_HEADER_OUT:
         return curl.getHeader();
       case CURLINFO_REQUEST_SIZE:
         break;
       case CURLINFO_CONTENT_LENGTH_DOWNLOAD:
         return LongValue.create(curl.getContentLength());
       case CURLINFO_CONTENT_LENGTH_UPLOAD:
         break;
       case CURLINFO_CONTENT_TYPE:
         String type = curl.getContentType();
 
         if (type == null)
           return NullValue.NULL;
 
         return new StringValueImpl(type);
       default:
         env.warning(L.l("Unknown CURL getinfo option"));
     }
 
     return NullValue.NULL;
   }
 
   /**
    * Returns a cURL handle.
    *
    * @param env
    * @param url
    */
   public static CurlResource curl_init(Env env,
                               @Optional String url)
   {
     CurlResource curl = new CurlResource();
 
     if (url.length() > 0)
       setURL(curl, url);
       //curl.setURL(url);
 
     return curl;
   }
 
   /**
    * Sets the url and extracts username/password from url.
    * Format: [protocol://]?[username:password@]?host
    */
   private static void setURL(CurlResource curl, String url)
   {
     int atSignIndex = url.indexOf('@');
 
     if (atSignIndex < 0) {
       curl.setURL(url);
       return;
     }
 
     int j = url.indexOf("://");
 
     String protocol;
     if (j < 0) {
       protocol = "http://";
       j = 0;
     }
     else {
       j += 3;
       protocol = url.substring(0, j);
     }
 
     int colonIndex = url.indexOf(':', j);
 
     if (colonIndex < 0)
       return;
 
     curl.setUsername(url.substring(j, colonIndex++));
     curl.setPassword(url.substring(colonIndex, atSignIndex++));
     curl.setURL(protocol + url.substring(atSignIndex));
   }
 
   /**
    * XXX: not documented by PHP
    *
    * @param env
    * @param curls
    * @param curl
    */
   public static LongValue curl_multi_add_handle(Env env,
                               Value curls,
                               Value curl)
   {
     throw new UnimplementedException("curl_multi_add_handle");
   }
 
   /**
    * XXX: not documented by PHP
    *
    * @param env
    * @param curls
    */
   public static LongValue curl_multi_close(Env env,
                               Value curls)
   {
     throw new UnimplementedException("curl_multi_close");
   }
 
   /**
    * XXX: not documented by PHP
    *
    * @param env
    * @param curls
    * @param stillRunning
    */
   public static LongValue curl_multi_exec(Env env,
                               Value curls,
                               @Reference Value stillRunning)
   {
     throw new UnimplementedException("curl_multi_exec");
   }
 
   /**
    * XXX: not documented by PHP
    *
    * @param env
    * @param curl
    */
   public static StringValue curl_multi_getcontent(Env env,
                               Value curl)
   {
     throw new UnimplementedException("curl_multi_getcontent");
   }
 
   /**
    * XXX: not documented by PHP
    *
    * @param env
    * @param curls
    */
   public static ArrayValue curl_multi_info_read(Env env,
                               Value curls)
   {
     throw new UnimplementedException("curl_multi_info_read");
   }
 
   /**
    * XXX: not documented by PHP
    *
    * @param env
    */
   public static Value curl_multi_init(Env env)
   {
     throw new UnimplementedException("curl_multi_init");
   }
 
   /**
    * XXX: not documented by PHP
    *
    * @param env
    * @param curls
    * @param curl
    */
   public static LongValue curl_multi_remove_handle(Env env,
                               Value curls,
                               Value curl)
   {
     throw new UnimplementedException("curl_multi_remove_handle");
   }
 
   /**
    * XXX: not documented by PHP
    *
    * @param env
    * @param curls
    * @param timeout
    */
   public static LongValue curl_multi_select(Env env,
                               Value curls,
                               @Optional Value timeout)
   {
     throw new UnimplementedException("curl_multi_select");
   }
 
   /**
    * Sets an array of options.
    *
    * @param env
    * @param curl
    * @param options
    */
   public static BooleanValue curl_setopt_array(Env env,
                               @NotNull CurlResource curl,
                               ArrayValue options)
   {
     for (Map.Entry<Value,Value> entry: options.entrySet()) {
       if (setOption(env, curl, entry.getKey().toInt(), entry.getValue()))
         return BooleanValue.FALSE;
     }
 
     return BooleanValue.TRUE;
   }
 
   /**
    * Sets a cURL option.
    *
    * @param env
    * @param curl
    * @param option
    * @param value
    *
    * @return true if successful
    */
   public static BooleanValue curl_setopt(Env env,
                               @NotNull CurlResource curl,
                               int option,
                               Value value)
   {
     if (setOption(env, curl, option, value))
       return BooleanValue.TRUE;
     else
       return BooleanValue.FALSE;
   }
 
   private static boolean setOption(Env env,
                               CurlResource curl,
                               int option,
                               Value value)
   {
     int i;
 
     switch (option) {
       //
       // booleans
       //
       case CURLOPT_AUTOREFERER:
         //XXX
         break;
       case CURLOPT_COOKIESESSION:
         curl.setCookie(null);
         break;
       case CURLOPT_FAILONERROR:
         curl.setFailOnError(value.toBoolean());
         break;
       case CURLOPT_FOLLOWLOCATION:
         curl.setIsFollowingRedirects(value.toBoolean());
         break;
       case CURLOPT_HEADER:
         curl.setIsReturningHeader(value.toBoolean());
         break;
       case CURLOPT_HTTPGET:
         curl.setRequestMethod("GET");
         break;
       case CURLOPT_HTTPPROXYTUNNEL:
         curl.setIsProxying(value.toBoolean());
         break;
       case CURLOPT_MUTE:
         curl.setIsVerbose(! value.toBoolean());
         break;
       case CURLOPT_NETRC:
         //username:password file
         //XXX
         break;
       case CURLOPT_NOBODY:
         curl.setIsReturningBody(false);
         break;
       case CURLOPT_NOPROGRESS:
         //XXX
         break;
       case CURLOPT_POST:
         curl.setRequestMethod("POST");
         break;
       case CURLOPT_PUT:
         curl.setRequestMethod("PUT");
         break;
       case CURLOPT_RETURNTRANSFER:
         curl.setIsReturningData(value.toBoolean());
         break;
       case CURLOPT_UNRESTRICTED_AUTH:
         //XXX
         break;
       case CURLOPT_UPLOAD:
         if (value.toBoolean())
           curl.setRequestMethod("PUT");
         break;
       case CURLOPT_VERBOSE:
         curl.setIsVerbose(value.toBoolean());
         break;
 
       //
       // ints
       //
       case CURLOPT_BUFFERSIZE:
         //XXX
         break;
       case CURLOPT_CONNECTTIMEOUT:
         curl.setConnectTimeout(value.toInt() * 1000);
         break;
       case CURLOPT_HTTP_VERSION:
         if (value.toInt() == CURL_HTTP_VERSION_1_0) {
           env.stub("cURL HTTP/1.0 not specifically supported");
         }
         break;
       case CURLOPT_HTTPAUTH:
         // get authentication method from server instead
 /*
         int method = value.toInt();
 
         if ((method & CURLAUTH_BASIC) == CURLAUTH_BASIC)
           curl.setAuthenticationMethod(CURLAUTH_BASIC);
         else if ((method & CURLAUTH_DIGEST) == CURLAUTH_DIGEST)
           curl.setAuthenticationMethod(CURLAUTH_DIGEST);
         else
           env.stub("cURL Http authentication method not supported");
 */
         break;
       case CURLOPT_INFILESIZE:
         curl.setUploadFileSize(value.toInt());
         break;
       case CURLOPT_LOW_SPEED_LIMIT:
         //XXX
         break;
       case CURLOPT_LOW_SPEED_TIME:
         //XXX
         break;
       case CURLOPT_MAXCONNECTS:
         //XXX
         break;
       case CURLOPT_PORT:
         curl.setPort(value.toInt());
         break;
       case CURLOPT_PROXYAUTH:
         //XXX
         break;
       case CURLOPT_PROXYPORT:
         curl.setProxyPort(value.toInt());
         break;
       case CURLOPT_PROXYTYPE:
         switch (value.toInt()) {
           case CURLPROXY_HTTP:
             curl.setProxyType("HTTP");
             break;
           case CURLPROXY_SOCKS5:
             curl.setProxyType("SOCKS");
             break;
           default:
             env.warning(L.l("unknown curl proxy type"));
         }
         break;
       case CURLOPT_TIMECONDITION:
         switch (value.toInt()) {
           case CURL_TIMECOND_IFMODSINCE:
             curl.setIfModifiedSince(true);
             break;
           case CURL_TIMECOND_IFUNMODSINCE:
             curl.setIfModifiedSince(false);
             break;
           default:
             env.warning(L.l("invalid CURLOPT_TIMECONDITION option"));
         }
         break;
       case CURLOPT_TIMEOUT:
         curl.setReadTimeout(value.toInt() * 1000);
         break;
       case CURLOPT_TIMEVALUE:
         long time = value.toInt() * 1000L;
         String format = "%a, %d %b %Y %H:%M:%S %Z";
 
         curl.setModifiedTime(QDate.formatGMT(time, format));
         break;
 
       //
       // strings
       //
       case CURLOPT_COOKIE:
         curl.setCookie(value.toString());
         break;
       case CURLOPT_COOKIEFILE:
         // XXX: Netscape cookie format support
         ReadStream in = null;
 
         try {
           Path path = env.getPwd().lookup(value.toString());
 
           if (path.exists()) {
             in = path.openRead();
 
             StringBuilder sb = new StringBuilder();
 
             int ch;
             while ((ch = in.read()) >= 0) {
               sb.append((char)ch);
             }
 
             curl.setCookie(sb.toString());
           }
         }
         catch (IOException e) {
           throw new QuercusModuleException(e);
         }
         finally {
           if (in != null)
             in.close();
         }
         break;
       case CURLOPT_COOKIEJAR:
         //XXX: Netscape cookie file format
         curl.setCookieFilename(value.toString());
         break;
       case CURLOPT_CUSTOMREQUEST:
         curl.setRequestMethod(value.toString());
         break;
       case CURLOPT_ENCODING:
         String encoding = value.toString();
         if (encoding.length() == 0)
           encoding = "gzip, deflate, identity";
         curl.setRequestProperty("Accept-Encoding", encoding);
         break;
       case CURLOPT_POSTFIELDS:
         curl.setRequestMethod("POST");
         curl.setPostBody(value.toBinaryValue(env));
         break;
       case CURLOPT_PROXY:
         curl.setIsProxying(true);
         curl.setProxyURL(value.toString());
         break;
       case CURLOPT_PROXYUSERPWD:
         String proxyUserPwd = value.toString();
         i = proxyUserPwd.indexOf(':');
 
         if (i >= 0)
           curl.setProxyUsername(proxyUserPwd.substring(0, i));
 
         curl.setProxyPassword(proxyUserPwd.substring(i + 1));
         break;
       case CURLOPT_RANGE:
         curl.setRequestProperty("Range", "bytes=" + value.toString());
         break;
       case CURLOPT_REFERER:
         curl.setRequestProperty("Referer", value.toString());
         break;
       case CURLOPT_URL:
         setURL(curl, value.toString());
         //curl.setURL(value.toString());
         break;
       case CURLOPT_USERAGENT:
         curl.setRequestProperty("User-Agent", value.toString());
         break;
       case CURLOPT_USERPWD:
         String userpwd = value.toString();
         i = userpwd.indexOf(':');
 
         if (i >= 0)
           curl.setUsername(userpwd.substring(0, i));
 
         curl.setPassword(userpwd.substring(i + 1));
         break;
 
       //
       // arrays
       //
       case CURLOPT_HTTP200ALIASES:
         //XXX: nonstandard HTTP replies like "FOO HTTP/1.1 OK"
         break;
       case CURLOPT_HTTPHEADER:
         ArrayValue array = value.toArrayValue(env);
 
         for (Map.Entry<Value,Value> entry: array.entrySet()) {
           curl.setRequestProperty(entry.getKey().toString(),
                                      entry.getValue().toString());
         }
         break;
 
       //
       // fopen stream resources
       //
       case CURLOPT_FILE:
         Object outputFile = value.toJavaObject();
 
         if (outputFile instanceof BinaryOutput)
           curl.setOutputFile((BinaryOutput)outputFile);
         break;
       case CURLOPT_INFILE:
         Object uploadFile = value.toJavaObject();
 
         if (uploadFile instanceof BinaryInput)
           curl.setUploadFile((BinaryInput)uploadFile);
         break;
       case CURLOPT_STDERR:
         //XXX
         break;
       case CURLOPT_WRITEHEADER:
         Object outputHeaderFile = value.toJavaObject();
 
         if (outputHeaderFile instanceof BinaryOutput)
           curl.setOutputHeaderFile((BinaryOutput)outputHeaderFile);
         break;
 
       //
       // callback functions
       //   - nobody really knows how to use them
       //
       case CURLOPT_HEADERFUNCTION:
         curl.setHeaderFunction(env.createCallback(value));
         if (true)
           throw new UnimplementedException("cURL callback support");
         break;
       case CURLOPT_PASSWDFUNCTION:
         curl.setPasswordFunction(env.createCallback(value));
         if (true)
           throw new UnimplementedException("cURL callback support");
         break;
       case CURLOPT_READFUNCTION:
         curl.setReadFunction(env.createCallback(value));
         if (true)
           throw new UnimplementedException("cURL callback support");
         break;
       case CURLOPT_WRITEFUNCTION:
         curl.setWriteFunction(env.createCallback(value));
         if (true)
           throw new UnimplementedException("cURL callback support");
         break;
 
       default:
         env.warning(L.l("CURL option unknown or unimplemented"));
         log.log(Level.FINE, L.l("CURL option unknown orunimplemented"));
         return false;
     }
 
     return true;
   }
 
   /**
    * Returns the version of this cURL implementation.
    *
    * @param env
    * @param version
    */
   public static ArrayValue curl_version(Env env,
                               @Optional Value version)
   {
     throw new UnimplementedException("curl_version");
   }
 
 }
