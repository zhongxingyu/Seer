 // Copyright 2005-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.server;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.PrintStream;
 import java.net.BindException;
 import java.net.DatagramSocket;
 import java.net.ServerSocket;
 
 import org.waterken.net.TCPDaemon;
 import org.waterken.udp.UDPDaemon;
 import org.waterken.vat.Root;
 import org.waterken.vat.Transaction;
 import org.waterken.vat.Vat;
 
 /**
  * Starts the server.
  */
 final class
 Serve {
 
     private
     Serve() {}
     
     /**
      * @param args  command line arguments
      */
     static public void
     main(String[] args) throws Exception {
         if (args.length == 0) {
             args = Config.keys.isFile()
                 ? new String[] { "http", "https" }
             : new String[] { "http" };
         }
         
         final Credentials credentials = Proxy.init();
         final String hostname =
             null != credentials ? credentials.getHostname() : "localhost";
 
         // summarize the configuration information
         final PrintStream err = System.err;
         Config.summarize(hostname, err);
 
         // start the network services
         for (int i = 0; i != args.length; ++i) {
             final String service = args[i];
         	try {
 	            final Object config = Config.read(Object.class, service);
 	            final Runnable task;
 	            if (config instanceof TCPDaemon) {
 	                final TCPDaemon daemon = (TCPDaemon)config;
 	                final ServerSocket listen = daemon.SSL
 	                    ? credentials.getContext().getServerSocketFactory().
 	                        createServerSocket(daemon.port, daemon.backlog)
 	                : new ServerSocket(daemon.port, daemon.backlog,
 	                				   Loopback.addr);
 	                task = new TCP(service, err, daemon,
 	                               daemon.SSL ? hostname : "localhost", listen);
 	            } else if (config instanceof UDPDaemon) {
 	                final UDPDaemon daemon = (UDPDaemon)config;
 	                task = new UDP(service, err, daemon,
 	                               new DatagramSocket(daemon.port));
 	            } else {
 	            	throw new Exception("Unrecognized service: " + service);
 	            }
 	
 	            // run the corresponding daemon
 	            new Thread(task, service).start();
 	            continue;
 	        } catch (final BindException e) {
	        	err.println("!!! Unable to use configured port for: "+ service);
 	        	err.println("Try configuring a different port number using " +
	        				"the corresponding file in the config/ folder !!!");
 	        } catch (final Exception e) {
 	        	e.printStackTrace();
 	        }
	        err.println("Aborting server");
         	System.exit(-1);
         }
         
         // ping all the persistent vats to restart any pending tasks
         ping(Config.read(File.class, "vatRootFolder"));
     }
     
     static private void
     ping(final File dir) {
         dir.listFiles(new FileFilter() {
             public boolean
             accept(final File child) {
                 if (child.isDirectory() && !child.getName().startsWith(".")) {
                     ping(child);
                 }
                 return false;
             }
         });
         try {
             Config.vats.connect(dir).enter(Vat.extend, new Transaction<Void>() {
                 public Void
                 run(final Root local) { return null; }
             });
         } catch (final Exception e) {}
     }
 }
