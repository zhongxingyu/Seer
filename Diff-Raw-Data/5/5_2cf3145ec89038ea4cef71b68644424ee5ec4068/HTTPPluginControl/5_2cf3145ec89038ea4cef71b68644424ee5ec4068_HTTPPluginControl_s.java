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
 
 import HTTPClient.ParseException;
 import HTTPClient.ProtocolNotSuppException;
 import HTTPClient.URI;
 
 import net.grinder.common.GrinderException;
 import net.grinder.engine.process.PluginRegistry;
 import net.grinder.plugininterface.PluginProcessContext;
 
 
 /**
  * Facade through which the script can control the behaviour of the
  * HTTP plug-in.
  *
  * @author Philip Aston
  * @version $Revision$
  **/
 public final class HTTPPluginControl
 {
     private static final PluginProcessContext s_processContext;
 
     static
     {
 	try {
 	    s_processContext =
 		PluginRegistry.getInstance().register(HTTPPlugin.class);
 	}
 	catch (GrinderException e) {
 	    throw new RuntimeException("Failed to register HTTPPlugin: " +
 				       e.getMessage());
 	}
     }
 
     /**
     * Get an {@link net.grinder.http.plugin.HTTPPluginConnnection}
      * that can be used to set the default behaviour of new
      * connections.
      * @return An <code>HTTPPluginConnection</code>.
      */
     public static final HTTPPluginConnection getConnectionDefaults()
     {
 	return HTTPPluginConnectionDefaults.getConnectionDefaults();
     }
 
     /**
     * Get an {@link net.grinder.http.plugin.HTTPPluginConnnection}
      * for a particular URL.
      *
      * <p>This method will throw a GrinderException if not called from
      * a worker thread.</p>
      * @param url An absolute URL that specifies the connection.
      * @return a <code>HTTPPluginConnection</code> value
      * @exception GrinderException If an error occurs.
      * @exception ParseException If <code>url</coder> can not be parsed.
      * @exception ProtocolNotSuppException If <code>url</code>
      * specifies an unsupported protocol.
      */
     public static final HTTPPluginConnection getThreadConnection(String url)
 	throws GrinderException, ParseException, ProtocolNotSuppException
     {
 	final HTTPPluginThreadState threadState =
 	    (HTTPPluginThreadState)s_processContext.getPluginThreadListener();
 	    
 	return threadState.getConnectionWrapper(new URI(url));
     }
 }
