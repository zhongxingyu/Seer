 /*
  * Made with all the love in the world
  * by scireum in Remshalden, Germany
  *
  * Copyright by scireum GmbH
  * http://www.scireum.de - info@scireum.de
  */
 
 package sirius.web.health.console;
 
 import sirius.kernel.di.std.Register;
 import sirius.kernel.nls.NLS;
 import sirius.web.http.WebServer;
 
 /**
  * Console command which reports statistics for the web server
  *
  * @author Andreas Haufler (aha@scireum.de)
  * @since 2014/01
  */
 @Register(name = "http")
 public class HTTPCommand implements Command {
 
 
     @Override
     public void execute(Output output, String... params) throws Exception {
         output.apply("%-20s %10s", "NAME", "VALUE");
         output.separator();
         output.apply("%-20s %10s", "Bytes In", NLS.formatSize(WebServer.getBytesIn()));
         output.apply("%-20s %10s", "Bytes Out", NLS.formatSize(WebServer.getBytesOut()));
         output.apply("%-20s %10d", "Packets In", WebServer.getMessagesIn());
         output.apply("%-20s %10d", "Packets Out", WebServer.getMessagesOut());
         output.apply("%-20s %10d", "Connects", WebServer.getConnections());
         output.apply("%-20s %10d", "Blocked Connects", WebServer.getBlockedConnections());
         output.apply("%-20s %10d", "Requests", WebServer.getRequests());
         output.apply("%-20s %10d", "Chunks", WebServer.getChunks());
         output.apply("%-20s %10d", "Keepalives", WebServer.getKeepalives());
         output.apply("%-20s %10d", "Open Connections", WebServer.getOpenConnections().get());
         output.apply("%-20s %10d", "Idle Timeouts", WebServer.getIdleTimeouts());
         output.apply("%-20s %10d", "Client Errors", WebServer.getClientErrors());
         output.apply("%-20s %10d", "Server Errors", WebServer.getServerErrors());
        output.apply("%-20s %10d", "Avg. Response Time", NLS.toUserString(WebServer.getAvgResponseTime()) + " ms");
         output.separator();
     }
 
     @Override
     public String getName() {
         return "http";
     }
 
     @Override
     public String getDescription() {
         return "Reports statistics for the web server";
     }
 }
