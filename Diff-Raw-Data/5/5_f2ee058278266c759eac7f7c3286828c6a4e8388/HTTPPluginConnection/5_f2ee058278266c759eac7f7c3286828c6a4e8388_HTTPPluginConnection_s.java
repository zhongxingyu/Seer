 // Copyright (C) 2002 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.plugin.http;
 
 import HTTPClient.NVPair;
 
 
 /**
  * Interface that script can use to control HTTP connections.
  *
  * <p><em>Most of the documentation for this class has been copied
  * verbatim from the HTTPClient documentation.</em></p>
  * 
  * @author Philip Aston
  * @version $Revision$
  * @see HTTPPluginControl
  **/
 public interface HTTPPluginConnection
 {
     /**
      * Set whether redirects should be automatically followed.
      *
      * @param followRedirects <code>true</code> => follow redirects.
      */
     void setFollowRedirects(boolean followRedirects);
 
     /**
      * Set whether cookies will be used.
      *
     * @param followRedirects <code>true</code> => use cookies.
      */
    void setUseCookies(boolean followRedirects);
 
 
     /**
      * Sets the default http headers to be sent with each request.
      *
      * <p> The actual headers sent are determined as follows: for each
      * header specified in multiple places a value given as part of
      * the request takes priority over any default values set by this
      * method, which in turn takes priority over any built-in default
      * values. A different way of looking at it is that we start off
      * with a list of all headers specified with the request, then add
      * any default headers set by this method which aren't already in
      * our list, and finally add any built-in headers which aren't yet
      * in the list. There is one exception to this rule:
      * <code>Content-length</code> header is always ignored; and when
      * posting form-data any <code>Content-type</code> is ignored in
      * favor of the built-in
      * <code>application/x-www-form-urlencoded</code> (however it will
      * be overriden by any content-type header specified as part of
      * the request).</p>
      *
      * <p>Typical headers you might want to set here are
      * <code>Accept</code> and its <code>Accept-*</code> relatives,
      * <code>Connection</code>, <code>From</code>,
      * <code>User-Agent</code>, etc.</p>
      *
      * @param defaultHeaders an array of header-name/value pairs (do
      * not give the separating ':').
      */
     void setDefaultHeaders(NVPair[] defaultHeaders);
 
     /**
      * Sets the timeout to be used for creating connections and
      * reading responses.
      *
      * <p>Setting the timeout to anything other than <code>0</code>
      * will cause additional threads to be spawned for each HTTP
      * request made.</p>
      *
      * <p>When a timeout expires the operation will throw an
      * <code>InterruptedIOException</code>.</p>
      *
      * <P>When creating new sockets the timeout will limit the time spent
      * doing the host name translation and establishing the connection with
      * the server.</p>
      *
      * <P>The timeout also influences the reading of the response
      * headers. However, it does not specify a how long, for example,
      * {@link HTTPClient.HTTPResponse#getStatusCode} may take, as
      * might be assumed. Instead it specifies how long a read on the
      * socket may take. If the response dribbles in slowly with
      * packets arriving quicker than the timeout then the method will
      * complete normally. I.e. the exception is only thrown if nothing
      * arrives on the socket for the specified time. Furthermore, the
      * timeout only influences the reading of the headers, not the
      * reading of the body.</p>
      *
      * <P>Read timeouts are associated with responses, so that you may
      * change this value before each request and it won't affect the
      * reading of responses to previous requests.</p>
      *
      * @param timeout the time in milliseconds. A time of 0 means wait
      *             indefinitely.
     */
     void setTimeout(int timeout);
 	
     /**
      * Adds an authorization entry for the "basic" authorization
      * scheme to the list. If an entry already exists for the "basic"
      * scheme and the specified realm then it is overwritten.
      *
      * @param realm The realm.
      * @param user  The user name.
      * @param password The password.
      */
     void addBasicAuthorization(String realm, String user, String password);
 
     /**
      * Remove an authorization entry for the "basic" authorization
      * scheme.
      *
      * @param realm The realm.
      * @param user  The user name.
      * @param password The password.
      */
     void removeBasicAuthorization(String realm, String user, String password);
 
     /**
      * Remove all authorization entry for the "basic" authorization
      * scheme.
      */
     void clearAllBasicAuthorizations();
 
     /**
      * Remove an authorization entry for the "digest" authorization
      * scheme.
      *
      * @param realm The realm.
      * @param user  The user name.
      * @param password The password.
      */
     void addDigestAuthorization(String realm, String user, String password);
 
     /**
      * Remove an authorization entry for the "digest" authorization
      * scheme.
      *
      * @param realm The realm.
      * @param user  The user name.
      * @param password The password.
      */
     void removeDigestAuthorization(String realm, String user, String password);
 
     /**
      * Remove all authorization entry for the "digest" authorization
      * scheme.
      */
     void clearAllDigestAuthorizations();
 
     /**
      * Set the proxy server to use. A null or empty string
      * <code>host</code> parameter disables the proxy.
      *
      * <P>Note that if you set a proxy for the connection using this
      * method, and a request made over this connection is redirected
      * to a different server, then the connection used for new server
      * will <em>not</em> pick this proxy setting, but instead will use
      * the default proxy settings. The default proxy setting can be
      * set using
      * <code>HTTPPluginControl.getConnectionDefaults().setProxyServer()</code>.
      *
      * @param  host    The host on which the proxy server resides.
      * @param  port    The port the proxy server is listening on.
      */
     void setProxyServer(String host, int port);
 }
