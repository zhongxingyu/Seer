 /**
  * Copyright 2009, Frederic Bregier, and individual contributors by the @author
  * tags. See the COPYRIGHT.txt in the distribution for a full listing of
  * individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3.0 of the License, or (at your option)
  * any later version.
  *
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this software; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
  * site: http://www.fsf.org.
  */
 package goldengate.commandexec.ssl.server;
 
 import goldengate.commandexec.utils.LocalExecDefaultResult;
 import goldengate.common.crypto.ssl.GgSecureKeyStore;
 import goldengate.common.crypto.ssl.GgSslContextFactory;
 import goldengate.common.logging.GgSlf4JLoggerFactory;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.jboss.netty.logging.InternalLoggerFactory;
 
 import ch.qos.logback.classic.Level;
 
 /**
  * LocalExec server Main method.
  *
  */
 public class LocalExecSslServer {
 
     static ExecutorService threadPool;
     static ExecutorService threadPool2;
 
     /**
      * Takes 3 to 6 arguments (last 3 are optional arguments):<br>
      * - mandatory arguments: filename keystorepaswwd keypassword<br>
      * - if no more arguments are provided, it implies localhost + 9999 port and no certificates<br>
      * - optional arguments:<br>
      *  "trustfilename" "trustpassword"<br>
      *  "trustfilename" "trustpassword" "addresse" "port"<br>
      *  "trustfilename" "trustpassword" "addresse" "port" "default delay"<br>
      * @param args
      * @throws Exception
      */
     public static void main(String[] args) throws Exception {
         InternalLoggerFactory.setDefaultFactory(new GgSlf4JLoggerFactory(
                 Level.WARN));
         int port = 9999;
         InetAddress addr;
         long delay = LocalExecDefaultResult.MAXWAITPROCESS;
         String keyStoreFilename, keyStorePasswd, keyPassword;
         String trustStoreFilename = null, trustStorePasswd = null;
         byte []loop = {127,0,0,1};
         addr = InetAddress.getByAddress(loop);
         if (args.length >=3) {
             keyStoreFilename = args[0];
             keyStorePasswd = args[1];
             keyPassword = args[2];
             if (args.length >= 5) {
                 trustStoreFilename = args[3];
                 trustStorePasswd = args[4];
                 if (args.length >= 7) {
                     addr = InetAddress.getByName(args[5]);
                     port = Integer.parseInt(args[6]);
                     if (args.length > 7) {
                         delay = Long.parseLong(args[7]);
                     }
                 }
             }
         } else {
             System.err.println("Need at least 3 arguments: Filename KeyStorePswd KeyPswd");
             return;
         }
         threadPool = Executors.newCachedThreadPool();
         threadPool2 = Executors.newCachedThreadPool();
         // Configure the server.
         ServerBootstrap bootstrap = new ServerBootstrap(
                 new NioServerSocketChannelFactory(threadPool, threadPool2));
         // Load the KeyStore (No certificates)
         GgSecureKeyStore ggSecureKeyStore =
             new GgSecureKeyStore(keyStoreFilename, keyStorePasswd, keyPassword);
         if (trustStoreFilename != null) {
             // Include certificates
             ggSecureKeyStore.initTrustStore(trustStoreFilename, trustStorePasswd, true);
         } else {
            ggSecureKeyStore.initEmptyTrustStore(trustStorePasswd);
         }
         GgSslContextFactory ggSslContextFactory =
             new GgSslContextFactory(ggSecureKeyStore, true);
         // Configure the pipeline factory.
         bootstrap.setPipelineFactory(
                 new LocalExecSslServerPipelineFactory(ggSslContextFactory, delay));
 
         // Bind and start to accept incoming connections only on local address.
         bootstrap.bind(new InetSocketAddress(addr, port));
     }
 }
