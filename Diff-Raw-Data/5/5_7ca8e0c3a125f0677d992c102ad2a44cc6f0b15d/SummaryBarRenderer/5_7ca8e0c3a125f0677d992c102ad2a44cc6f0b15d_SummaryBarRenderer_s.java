 package net.i2p.router.web;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 
 import net.i2p.router.RouterContext;
 
 /**
  *  Refactored from summarynoframe.jsp to save ~100KB
  *
  */
 public class SummaryBarRenderer {
     private RouterContext _context;
     private SummaryHelper _helper;
 
     public SummaryBarRenderer(RouterContext context, SummaryHelper helper) {
         _context = context;
         _helper = helper;
     }
 
     /**
      *  Note - ensure all links in here are absolute, as the summary bar may be displayed
      *         on lower-level directory errors.
      */
     public void renderSummaryHTML(Writer out) throws IOException {
         StringBuilder buf = new StringBuilder(8*1024);
 
         buf.append("<a href=\"/index.jsp\" target=\"_top\"><img src=\"/themes/console/images/i2plogo.png\" alt=\"")
            .append(_("I2P Router Console"))
            .append("\" title=\"")
            .append(_("I2P Router Console"))
            .append("\"></a><hr>");
 
         File lpath = new File(_context.getBaseDir(), "docs/toolbar.html");
         // you better have target="_top" for the links in there...
         if (lpath.exists()) {
             ContentHelper linkhelper = new ContentHelper();
             linkhelper.setPage(lpath.getAbsolutePath());
             linkhelper.setMaxLines("100");
             buf.append(linkhelper.getContent());
         } else {
             buf.append("<h3><a href=\"/configclients.jsp\" target=\"_top\" title=\"")
                .append(_("Configure startup of clients and webapps (services); manually start dormant services"))
                .append("\">")
                .append(_("I2P Services"))
                .append("</a></h3>\n" +
 
                        "<hr><table>" +
 
                        "<tr><td><a href=\"/susidns/index.jsp\" target=\"_blank\" title=\"")
                .append(_("Manage your I2P hosts file here (I2P domain name resolution)"))
                .append("\">")
                .append(_("Addressbook"))
                .append("</a>\n" +
 
                        "<a href=\"/i2psnark/\" target=\"_blank\" title=\"")
                .append(_("Built-in anonymous BitTorrent Client"))
                .append("\">")
                .append(_("Torrents"))
                .append("</a>\n" +
 
                        "<a href=\"/susimail/susimail\" target=\"blank\" title=\"")
                .append(_("Anonymous webmail client"))
                .append("\">")
                .append(_("Webmail"))
                .append("</a>\n" +
 
                        "<a href=\"http://127.0.0.1:7658/\" target=\"_blank\" title=\"")
                .append(_("Anonymous resident webserver"))
                .append("\">")
                .append(_("Webserver"))
                .append("</a></td></tr></table>\n" +
 
                        "<hr><h3><a href=\"/config.jsp\" target=\"_top\" title=\"")
                .append(_("Configure I2P Router"))
                .append("\">")
                .append(_("I2P Internals"))
                .append("</a></h3><hr>\n" +
 
                        "<table><tr><td>\n" +
 
                        "<a href=\"/tunnels.jsp\" target=\"_top\" title=\"")
                .append(_("View existing tunnels and tunnel build status"))
                .append("\">")
                .append(_("Tunnels"))
                .append("</a>\n" +
 
                        "<a href=\"/peers.jsp\" target=\"_top\" title=\"")
                .append(_("Show all current peer connections"))
                .append("\">")
                .append(_("Peers"))
                .append("</a>\n" +
 
                        "<a href=\"/profiles.jsp\" target=\"_top\" title=\"")
                .append(_("Show recent peer performance profiles"))
                .append("\">")
                .append(_("Profiles"))
                .append("</a>\n" +
 
                        "<a href=\"/netdb.jsp\" target=\"_top\" title=\"")
                .append(_("Show list of all known I2P routers"))
                .append("\">")
                .append(_("NetDB"))
                .append("</a>\n" +
 
                        "<a href=\"/logs.jsp\" target=\"_top\" title=\"")
                .append(_("Health Report"))
                .append("\">")
                .append(_("Logs"))
                .append("</a>\n" +
 
                        "<a href=\"/jobs.jsp\" target=\"_top\" title=\"")
                .append(_("Show the router's workload, and how it's performing"))
                .append("\">")
                .append(_("Jobs"))
                .append("</a>\n" +
 
                        "<a href=\"/graphs.jsp\" target=\"_top\" title=\"")
                .append(_("Graph router performance"))
                .append("\">")
                .append(_("Graphs"))
                .append("</a>\n" +
 
                        "<a href=\"/stats.jsp\" target=\"_top\" title=\"")
                .append(_("Textual router performance statistics"))
                .append("\">")
                .append(_("Stats"))
                .append("</a></td></tr></table>\n");
 
             out.write(buf.toString());
             buf.setLength(0);
         }
 
 
 
         buf.append("<hr><h3><a href=\"/help.jsp\" target=\"_top\" title=\"")
            .append(_("I2P Router Help"))
            .append("\">")
            .append(_("General"))
            .append("</a></h3><hr>" +
                    "<h4><a title=\"")
            .append(_("Your unique I2P router identity is"))
            .append(' ')
            .append(_helper.getIdent())
            .append(", ")
            .append(_("never reveal it to anyone"))
            .append("\" href=\"/netdb.jsp?r=.\" target=\"_top\">")
            .append(_("Local Identity"))
            .append("</a></h4><hr>\n" +
 
                    "<table><tr><td align=\"left\">" +
                    "<b>")
            .append(_("Version"))
            .append(":</b></td>" +
                    "<td align=\"right\">")
            .append(_helper.getVersion())
            .append("</td></tr>\n" +
 
                    "<tr title=\"")
            .append(_("How long we've been running for this session"))
            .append("\">" +
                    "<td align=\"left\"><b>")
            .append(_("Uptime"))
            .append(":</b></td>" +
                    "<td align=\"right\">")
            .append(_helper.getUptime())
            .append("</td></tr></table>\n" +
 
                    "<hr><h4><a href=\"/config.jsp#help\" target=\"_top\" title=\"")
            .append(_("Help with configuring your firewall and router for optimal I2P performance"))
            .append("\">")
            .append(_helper.getReachability())
            .append("</a></h4><hr>\n");
 
 
         if (_helper.updateAvailable() || _helper.unsignedUpdateAvailable()) {
             // display all the time so we display the final failure message
            buf.append("<br>").append(UpdateHandler.getStatus());
             if ("true".equals(System.getProperty("net.i2p.router.web.UpdateHandler.updateInProgress"))) {
                 // nothing
             } else if(
                       // isDone() is always false for now, see UpdateHandler
                       // ((!update.isDone()) &&
                       _helper.getAction() == null &&
                       _helper.getUpdateNonce() == null &&
                       ConfigRestartBean.getRestartTimeRemaining() > 12*60*1000) {
                 long nonce = _context.random().nextLong();
                 String prev = System.getProperty("net.i2p.router.web.UpdateHandler.nonce");
                 if (prev != null)
                     System.setProperty("net.i2p.router.web.UpdateHandler.noncePrev", prev);
                 System.setProperty("net.i2p.router.web.UpdateHandler.nonce", nonce+"");
                 String uri = _helper.getRequestURI();
                buf.append("<form action=\"").append(uri).append("\" method=\"GET\">\n");
                 buf.append("<input type=\"hidden\" name=\"updateNonce\" value=\"").append(nonce).append("\" >\n");
                 if (_helper.updateAvailable()) {
                     buf.append("<button type=\"submit\" name=\"updateAction\" value=\"signed\" >")
                        .append(_("Download"))
                        .append(' ')
                        .append(_helper.getUpdateVersion())
                        .append(' ')
                        .append(_("Update"))
                        .append("</button>\n");
                 }
                 if (_helper.unsignedUpdateAvailable()) {
                     buf.append("<button type=\"submit\" name=\"updateAction\" value=\"Unsigned\" >")
                        .append(_("Download Unsigned"))
                        .append("<br>")
                        .append(_("Update"))
                        .append(' ')
                        .append(_helper.getUnsignedUpdateVersion())
                        .append("</button>\n");
                 }
                 buf.append("</form>\n");
             }
         }
 
 
 
 
         buf.append("<p>")
            .append(ConfigRestartBean.renderStatus(_helper.getRequestURI(), _helper.getAction(), _helper.getConsoleNonce()))
 
            .append("</p><hr><h3><a href=\"/peers.jsp\" target=\"_top\" title=\"")
            .append(_("Show all current peer connections"))
            .append("\">")
            .append(_("Peers"))
            .append("</a></h3><hr>\n" +
 
                    "<table>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Active"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getActivePeers())
            .append('/')
            .append(_helper.getActiveProfiles())
            .append("</td></tr>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Fast"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getFastPeers())
            .append("</td></tr>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("High capacity"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getHighCapacityPeers())
            .append("</td></tr>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Integrated"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getWellIntegratedPeers())
            .append("</td></tr>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Known"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getAllPeers())
            .append("</td></tr>\n" +
 
                    "</table><hr>\n");
 
 
         out.write(buf.toString());
         buf.setLength(0);
 
 
         boolean anotherLine = false;
         if (_helper.showFirewallWarning()) {
             buf.append("<h4><a href=\"/config.jsp\" target=\"_top\" title=\"")
                .append(_("Help with firewall configuration"))
                .append("\">")
                .append(_("Check NAT/firewall"))
                .append("</a></h4>");
             anotherLine = true;
         }
 
         boolean reseedInProgress = Boolean.valueOf(System.getProperty("net.i2p.router.web.ReseedHandler.reseedInProgress")).booleanValue();
         // If showing the reseed link is allowed
         if (_helper.allowReseed()) {
             if (reseedInProgress) {
                 // While reseed occurring, show status message instead
                 buf.append("<i>").append(System.getProperty("net.i2p.router.web.ReseedHandler.statusMessage","")).append("</i><br>");
             } else {
                 // While no reseed occurring, show reseed link
                 long nonce = _context.random().nextLong();
                 String prev = System.getProperty("net.i2p.router.web.ReseedHandler.nonce");
                 if (prev != null) System.setProperty("net.i2p.router.web.ReseedHandler.noncePrev", prev);
                 System.setProperty("net.i2p.router.web.ReseedHandler.nonce", nonce+"");
                 String uri = _helper.getRequestURI();
                 buf.append("<p><form action=\"").append(uri).append("\" method=\"GET\">\n");
                 buf.append("<input type=\"hidden\" name=\"reseedNonce\" value=\"").append(nonce).append("\" >\n");
                 buf.append("<button type=\"submit\" value=\"Reseed\" >").append(_("Reseed")).append("</button></form></p>\n");
             }
             anotherLine = true;
         }
         // If a new reseed ain't running, and the last reseed had errors, show error message
         if (!reseedInProgress) {
             String reseedErrorMessage = System.getProperty("net.i2p.router.web.ReseedHandler.errorMessage","");
             if (reseedErrorMessage.length() > 0) {
                 buf.append("<i>").append(reseedErrorMessage).append("</i><br>");
                 anotherLine = true;
             }
         }
         if (anotherLine)
             buf.append("<hr>");
 
 
         buf.append("<h3><a href=\"/config.jsp\" title=\"")
            .append(_("Configure router bandwidth allocation"))
            .append("\" target=\"_top\">")
            .append(_("Bandwidth in/out"))
            .append("</a></h3><hr>" +
                    "<table>\n" +
 
                    "<tr><td align=\"left\"><b>1s:</b></td><td align=\"right\">")
            .append(_helper.getInboundSecondKBps())
            .append('/')
            .append(_helper.getOutboundSecondKBps())
            .append("K/s</td></tr>\n" +
 
                    "<tr><td align=\"left\"><b>5m:</b></td><td align=\"right\">")
            .append(_helper.getInboundFiveMinuteKBps())
            .append('/')
            .append(_helper.getOutboundFiveMinuteKBps())
            .append("K/s</td></tr>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Total"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getInboundLifetimeKBps())
            .append('/')
            .append(_helper.getOutboundLifetimeKBps())
            .append("K/s</td></tr>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Used"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getInboundTransferred())
            .append('/')
            .append(_helper.getOutboundTransferred())
            .append("</td></tr></table>\n" +
 
                    "<hr><h3><a href=\"/tunnels.jsp\" target=\"_top\" title=\"")
            .append(_("View existing tunnels and tunnel build status"))
            .append("\">")
            .append(_("Tunnels in/out"))
            .append("</a></h3><hr>" +
                    "<table>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Exploratory"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getInboundTunnels())
            .append('/')
            .append(_helper.getOutboundTunnels())
            .append("</td></tr>\n" +
 
                   "<tr><td align=\"left\"><b>")
            .append(_("Client"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getInboundClientTunnels())
            .append('/')
            .append(_helper.getOutboundClientTunnels())
            .append("</td></tr>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Participating"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getParticipatingTunnels())
            .append("</td></tr>\n" +
 
                    "</table><hr><h3><a href=\"/jobs.jsp\" target=\"_top\" title=\"")
            .append(_("What's in the router's job queue?"))
            .append("\">")
            .append(_("Congestion"))
            .append("</a></h3><hr>" +
                    "<table>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Job lag"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getJobLag())
            .append("</td></tr>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Message delay"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getMessageDelay())
            .append("</td></tr>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Tunnel lag"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getTunnelLag())
            .append("</td></tr>\n" +
 
                    "<tr><td align=\"left\"><b>")
            .append(_("Backlog"))
            .append(":</b></td><td align=\"right\">")
            .append(_helper.getInboundBacklog())
            .append("</td></tr>\n" +
 
                    "</table><hr><h4>")
            .append(_(_helper.getTunnelStatus()))
            .append("</h4><hr>\n")
            .append(_helper.getDestinations());
 
 
 
         out.write(buf.toString());
     }
 
     /** translate a string */
     private String _(String s) {
         return Messages.getString(s, _context);
     }
 }
