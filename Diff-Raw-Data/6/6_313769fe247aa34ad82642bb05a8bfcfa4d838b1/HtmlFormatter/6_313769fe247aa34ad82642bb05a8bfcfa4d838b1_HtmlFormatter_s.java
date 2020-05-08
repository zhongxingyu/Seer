 package eu.uberdust.formatter;
 
 import eu.uberdust.caching.Cachable;
 import eu.uberdust.formatter.exception.NotImplementedException;
 import eu.uberdust.formatter.util.Tag;
 import eu.wisebed.wisedb.model.*;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Implements the {@link Formatter} Interface and converts Wisedb Objects to Html.
  *
  * @author amaxilat
  */
 public class HtmlFormatter implements Formatter {
     /**
      * LOGGER.
      */
     private static final Logger LOGGER = Logger.getLogger(HtmlFormatter.class);
     /**
      * Singleton Instance.
      */
     private static HtmlFormatter instance = new HtmlFormatter();
     /**
      * Base Url to use with url links.
      */
     private static String baseUrl = "";
     /**
      * Starting tag for table.
      */
     private static final String S_TABLE = "<table>";
     /**
      * Closing tag for table.
      */
     private static final String E_TABLE = "</table>";
     /**
      * Starting tag for table row.
      */
     private static final String S_ROW = "<tr>";
     /**
      * Closing tag for table row.
      */
     private static final String E_ROW = "</tr>\n";
     /**
      * Starting tag for table header cell.
      */
     private static final String S_TH = "<th>";
     /**
      * Closing tag for table header cell.
      */
     private static final String E_TH = "</th>";
     /**
      * Starting tag for table body cell.
      */
     private static final String S_TD = "<td>";
     /**
      * Closing tag for table body cell.
      */
     private static final String E_TD = "</td>";
 
     /**
      * Returns a {@link HtmlFormatter} instance.
      *
      * @return the {@link HtmlFormatter} instance.
      */
     public static HtmlFormatter getInstance() {
         return instance;
     }
 
     @Override
     public void setBaseUrl(final String baseUrl) {
         LOGGER.debug("baseUrl=" + baseUrl);
         this.baseUrl = baseUrl;
     }
 
     public HtmlFormatter() {
         LOGGER.debug("new instance");
     }
 
     @Override
     public final String formatTestbed(final Testbed testbed) throws NotImplementedException {
         throw new NotImplementedException();
     }
 
 
     @Override
     @Cachable
     public String formatNode(final Node node) throws NotImplementedException {
 
         LOGGER.info("formatNode");
         System.out.println("formatNode");
 
         final StringBuilder output = new StringBuilder();
         output.append(S_TABLE);
         output.append(S_ROW).append(tdCell("Node ID")).append(tdCell(urlLink(node))).append(E_ROW);
         output.append(S_ROW).append(tdCell("GeoRSS Feed"))
                 .append(tdCell(urlLink(
                         new StringBuilder()
                                 .append("/rest/testbed/").append(node.getSetup().getId())
                                 .append("/node/").append(node.getName())
                                 .append("/georss").toString()
                         , "GeoRSS Feed"
                 )))
                 .append(tdCell(new StringBuilder().append("<a href='http://maps.google.com/maps?q=").append(baseUrl).append("/rest/testbed/").append(node.getSetup().getId()).append("/node/").append(node.getName()).append("/georss' > View on Google Maps </a>").toString()
                 )).append(E_ROW);
         output.append(S_ROW).append(tdCell("KML Feed"))
                 .append(tdCell(urlLink(new StringBuilder()
                         .append("/rest/testbed/").append(node.getSetup().getId())
                         .append("/node/").append(node.getName())
                         .append("/kml").toString()
                         , "Kml Feed"
                 )))
                 .append(tdCell(new StringBuilder()
                         .append("<a href='http://maps.google.com/maps?q=").append(baseUrl)
                         .append("/rest/testbed/").append(node.getSetup().getId())
                         .append("/node/").append(node.getName()).append("/kml' > View on Google Maps </a>").toString()
                 )).append(E_ROW);
         output.append(S_ROW).append(tdCell("Rdf description"))
                 .append(tdCell(urlLink(new StringBuilder()
                         .append("/rest/testbed/").append(node.getSetup().getId())
                         .append("/node/").append(node.getName())
                         .append("/rdf").toString()
                         , "Rdf Description"
                 ))).append(E_ROW);
 
 
         output.append(E_TABLE);
 
         return output.toString();
     }
 
     @Override
     @Cachable
     public String formatLink(final Link link) throws NotImplementedException {
         final StringBuilder output = new StringBuilder();
         output.append(S_TABLE);
         output.append(S_ROW).append(tdCell(urlLink(
                 link
         )));
         output.append(E_ROW);
         output.append(S_ROW).append(tdCell("Source ID")).append(tdCell(urlLink(link.getSource())
         )).append(E_ROW);
         output.append(S_ROW).append(tdCell("Target ID")).append(tdCell(urlLink(link.getTarget())
         )).append(E_ROW);
         output.append(E_TABLE);
         return output.toString();
     }
 
     @Override
     @Cachable
     public final String formatCapability(final Testbed testbed, final Capability capability)
             throws NotImplementedException {
         final StringBuilder output = new StringBuilder();
         output.append(S_TABLE);
         output.append(S_ROW).append(thCell("Capability ID"));
         output.append(thCell(urlLink(
                 new StringBuilder()
                         .append("/rest/testbed/").append(testbed.getId())
                         .append("/capability/").append(capability.getName()).toString(),
                 capability.getName()
         ))).append(E_ROW);
 
         output.append(S_ROW).append(tdCell("Capability Semantic Description"));
         output.append(tdCell(capability.getDescription())).append(E_ROW);
         output.append(S_ROW).append(thCell("List All Readings for the capability in:")).append(E_ROW);
 
         output.append(S_ROW).append(tdCell(urlLink(
                 new StringBuilder()
                         .append("/rest/testbed/").append(testbed.getId())
                         .append("/capability/").append(capability.getName())
                         .append("/tabdelimited").toString()
                 , "Tab Delimited Format"
         ))).append(E_ROW);
 
 
         return output.toString();
     }
 
     @Override
     public final String formatTestbeds(final List<Testbed> testbeds, final Map<String, Long> nodesCount,
                                        final Map<String, Long> linksCount) throws NotImplementedException {
         final StringBuilder output = new StringBuilder();
 
         output.append("<p> Available Testbeds: (" + testbeds.size() + ") </p>");
 
         output.append(S_TABLE);
 
         int count = 1;
         for (final Testbed testbed : testbeds) {
             output.append(S_ROW).append(tdCell(String.valueOf(count)));
             output.append(tdCell(urlLink(testbed)));
             output.append(tdCell("Nodes (" + nodesCount.get(testbed.getName()) + ")"));
             output.append(tdCell("Links (" + linksCount.get(testbed.getName()) + ")"));
             output.append(E_ROW);
             count++;
 
         }
         output.append(E_TABLE);
         return output.toString();
     }
 
     @Override
     public final String formatNodeReadings(final List<NodeReading> nodeReadings) {
         System.out.println("formatNodeReadings");
         final StringBuilder output = new StringBuilder();
 
         output.append("<table id=\"information\"").append(S_ROW);
         output.append(thCell("Timestamp"));
         output.append(thCell("Reading"));
         output.append(E_ROW);
 
         for (final NodeReading nr : nodeReadings) {
             output.append(S_ROW);
             output.append(tdCell(nr.getTimestamp().toString()));
             if (nr.getReading() != null) {
                 output.append(tdCell(nr.getReading().toString()));
             } else {
                 output.append(tdCell(nr.getStringReading()));
             }
             output.append(E_ROW);
         }
 
         output.append(E_TABLE);
         return output.toString();
     }
 
     @Override
     public String formatLastNodeReadings(List<NodeCapability> nodeCapabilities) throws NotImplementedException {
         LOGGER.info("formatLastNodeReadings");
         final StringBuilder output = new StringBuilder();
         output.append("<h2>Nodes</h2>").append(NEW_LINE);
         output.append("<table class='readings'>");
         output.append(S_ROW).append(thCell("Node")).append(thCell("Capability")).append(thCell("Timestamp"));
         output.append(thCell("Reading")).append(E_ROW);
         List<NodeCapability> perNodeCapabilities = new ArrayList<NodeCapability>();
         boolean outdated = true;
 
         Node node = nodeCapabilities.get(0).getNode();
         StringBuilder nodeOutput = new StringBuilder();
         int size = 0;
 
         for (NodeCapability nodeCapability : nodeCapabilities) {
             if (nodeCapability.getNode().equals(node)) {
                 outdated = outdated && isOutdated(nodeCapability);
                 size++;
                 nodeOutput.append(S_ROW);
                 nodeOutput.append(tdCell(urlLink(nodeCapability)));
 
                 nodeOutput.append(tdCell(String.valueOf(nodeCapability.getLastNodeReading().getTimestamp().toString())));
                 if (nodeCapability.getLastNodeReading().getReading() != null) {
                     nodeOutput.append(tdCell(nodeCapability.getLastNodeReading().getReading().toString(), "reading"));
                 } else if (nodeCapability.getLastNodeReading().getStringReading() != null) {
                     nodeOutput.append(tdCell(nodeCapability.getLastNodeReading().getStringReading(), "reading"));
                 }
                 nodeOutput.append(E_ROW);
 
             } else {
                 nodeOutput.insert(0,
                         new StringBuilder().append("<td  class='firstrow' rowspan=").append(size + 1).append("> ")
                                 .append(urlLink(node)).append("</td>").toString());
 
                 if (outdated) {
                     nodeOutput.insert(0, "<tr class='outdated'>");
                 } else {
                     nodeOutput.insert(0, "<tr class='uptodate'>");
                 }
                 nodeOutput.insert(0, "<tr><td colspan=4><hr></td></tr>");
                 outdated = true;
                 size = 0;
 
                nodeOutput.append(E_ROW);
                 output.append(nodeOutput.toString());
                 nodeOutput=new StringBuilder();
                 node=nodeCapability.getNode();
 
                 size++;
                 nodeOutput.append(S_ROW);
                 nodeOutput.append(tdCell(urlLink(nodeCapability)));
 
                 nodeOutput.append(tdCell(String.valueOf(nodeCapability.getLastNodeReading().getTimestamp().toString())));
                 if (nodeCapability.getLastNodeReading().getReading() != null) {
                     nodeOutput.append(tdCell(nodeCapability.getLastNodeReading().getReading().toString(), "reading"));
                 } else if (nodeCapability.getLastNodeReading().getStringReading() != null) {
                     nodeOutput.append(tdCell(nodeCapability.getLastNodeReading().getStringReading(), "reading"));
                 }
                 nodeOutput.append(E_ROW);
             }
         }
 
         output.append(E_TABLE);
         return output.toString();
     }
 
     public String formatLastLinkReadings(List<LinkCapability> linkCapabilities) throws NotImplementedException {
         LOGGER.info("formatLastLinkReadings");
         final StringBuilder output = new StringBuilder();
         output.append("<h2>Links</h2>").append(NEW_LINE);
         output.append("<table class='readings'>");
         output.append(S_ROW).append(thCell("Link")).append(thCell("Capability")).append(thCell("Timestamp"));
         output.append(thCell("Reading")).append(E_ROW);
         boolean outdated = true;
 
         Link link= linkCapabilities.get(0).getLink();
         StringBuilder nodeOutput = new StringBuilder();
         int size = 0;
 
         for (LinkCapability linkCapability : linkCapabilities) {
             if (linkCapability.getLink().equals(link)) {
                 outdated = outdated && isOutdated(linkCapability);
                 size++;
                 nodeOutput.append(S_ROW);
                 nodeOutput.append(tdCell(urlLink(linkCapability)));
 
                 nodeOutput.append(tdCell(String.valueOf(linkCapability.getLastLinkReading().getTimestamp().toString())));
                 if (linkCapability.getLastLinkReading().getReading() != null) {
                     nodeOutput.append(tdCell(linkCapability.getLastLinkReading().getReading().toString(), "reading"));
                 } else if (linkCapability.getLastLinkReading().getStringReading() != null) {
                     nodeOutput.append(tdCell(linkCapability.getLastLinkReading().getStringReading(), "reading"));
                 }
                 nodeOutput.append(E_ROW);
 
             } else {
                 nodeOutput.insert(0,
                         new StringBuilder().append("<td  class='firstrow' rowspan=").append(size + 1).append("> ")
                                 .append(urlLink(link)).append("</td>").toString());
 
                 if (outdated) {
                     nodeOutput.insert(0, "<tr class='outdated'>");
                 } else {
                     nodeOutput.insert(0, "<tr class='uptodate'>");
                 }
                 nodeOutput.insert(0, "<tr><td colspan=4><hr></td></tr>");
                 outdated = true;
                 size = 0;
 
                 nodeOutput.append(E_ROW);
                 output.append(nodeOutput.toString());
                 nodeOutput=new StringBuilder();
                 link=linkCapability.getLink();
 
                 size++;
                 nodeOutput.append(S_ROW);
                 nodeOutput.append(tdCell(urlLink(linkCapability)));
 
                 nodeOutput.append(tdCell(String.valueOf(linkCapability.getLastLinkReading().getTimestamp().toString())));
                 if (linkCapability.getLastLinkReading().getReading() != null) {
                     nodeOutput.append(tdCell(linkCapability.getLastLinkReading().getReading().toString(), "reading"));
                 } else if (linkCapability.getLastLinkReading().getStringReading() != null) {
                     nodeOutput.append(tdCell(linkCapability.getLastLinkReading().getStringReading(), "reading"));
                 }
                 nodeOutput.append(E_ROW);
             }
         }
         
 
         output.append(E_TABLE);
         return output.toString();
     }
 
     private boolean isOutdated(NodeCapability capability) {
         if (capability.getLastNodeReading().getTimestamp() != null) {
             return (System.currentTimeMillis() - capability.getLastNodeReading().getTimestamp().getTime()) > 86400000;
         }
         return false;
     }
 
     private boolean isOutdated(LinkCapability capability) {
         if (capability.getLastLinkReading().getTimestamp() != null) {
             return (System.currentTimeMillis() - capability.getLastLinkReading().getTimestamp().getTime()) > 86400000;
         }
         return false;
     }
 
     @Override
     public final String formatNodeReading(final LastNodeReading nodeReading) throws NotImplementedException {
         throw new NotImplementedException();
     }
 
 
     @Override
     public final String describeNodeCapabilities(final List<NodeCapability> capabilities) throws NotImplementedException {
         if (capabilities.isEmpty()) {
             return "<p style=\"color : red\">No capabilities found!</p>";
         } else {
             final StringBuilder output = new StringBuilder();
             output.append(S_TABLE);
             output.append(S_ROW).append(thCell("Capabilities (" + capabilities.size() + ")"));
             final StringBuilder innerTable = new StringBuilder();
             innerTable.append(S_TABLE);
             for (final NodeCapability nCap : capabilities) {
                 innerTable.append(S_ROW);
                 innerTable.append(tdCell(urlLink(
                         "/rest/testbed/" + nCap.getNode().getSetup().getId()
                                 + "/capability/" + nCap.getCapability().getName()
                         , nCap.getCapability().getName()
                 )));
                 final String basicUrl = "/rest/testbed/" + nCap.getNode().getSetup().getId()
                         + "/node/" + nCap.getNode().getName()
                         + "/capability/" + nCap.getCapability().getName();
                 innerTable.append(tdCell(urlLink(
                         basicUrl + "/html/limit/10"
                         , "HTML")));
                 innerTable.append(tdCell(urlLink(
                         basicUrl + "/tabdelimited/limit/10"
                         , "Tab Delimited")));
                 innerTable.append(tdCell(urlLink(
                         basicUrl + "/json/limit/10"
                         , "JSON")));
                 innerTable.append(tdCell(urlLink(
                         basicUrl + "/wiseml/limit/10"
                         , "WiseML")));
                 innerTable.append(tdCell(urlLink(
                         basicUrl + "/latestreading"
                         , "Last Reading Tab")));
                 innerTable.append(tdCell(urlLink(
                         basicUrl + "/latestreading/json"
                         , "Last Reading JSON")));
                 innerTable.append(tdCell(urlLink(
                         basicUrl + "/chart/limit/10"
                         , "Chart")));
                 innerTable.append(tdCell(urlLink(
                         basicUrl + "/live"
                         , "Live")));
                 innerTable.append(E_ROW);
             }
             innerTable.append(E_TABLE);
             output.append(tdCell(innerTable.toString())).append(E_ROW);
             output.append(E_TABLE);
             return output.toString();
         }
     }
 
     @Override
     public final String formatLinkCapabilities(final List<LinkCapability> linkCapabilities) throws NotImplementedException {
         if (linkCapabilities.isEmpty()) {
             return "<p style=\"color : red\">No capabilities found!</p>";
         } else {
             final StringBuilder output = new StringBuilder();
             output.append(S_TABLE);
             output.append(S_ROW).append(thCell("Capabilities"));
             output.append(thCell(String.valueOf(linkCapabilities.size()))).append(E_ROW);
             for (final LinkCapability capability : linkCapabilities) {
 
                 output.append(S_ROW);
                 output.append(tdCell(urlLink(capability)));
                 output.append(E_ROW);
             }
             output.append(E_TABLE);
             return output.toString();
         }
     }
 
     @Override
     @Cachable
     public final String formatCapabilities(final Testbed testbed, final List<Capability> capabilities)
             throws NotImplementedException {
         if (capabilities.isEmpty()) {
             return "<p style=\"color : red\">No capabilities found!</p>";
         } else {
             final StringBuilder output = new StringBuilder();
             output.append(S_TABLE);
             output.append(S_ROW).append(tdCell(
                     urlLink("/rest/testbed/" + testbed.getSetup().getId() + "/capability", "Capabilities")
                             + " (" + capabilities.size() + ") ["
                             + urlLink("/rest/testbed/" + testbed.getSetup().getId() + "/capability/raw", "raw") + ", "
                             + urlLink("/rest/testbed/" + testbed.getSetup().getId() + "/capability/json", "json")
                             + "]"
 
             )).append(E_ROW);
             for (final Capability capability : capabilities) {
 
                 output.append(S_ROW);
                 output.append(tdCell(urlLink(
                         "/rest/testbed/" + testbed.getId() + "/capability/" + capability.getName()
                         , capability.getName())));
                 output.append(E_ROW);
             }
             output.append(E_TABLE);
             return output.toString();
         }
     }
 
     @Override
     @Cachable
     public final String formatNodes(final List<Node> nodes) throws NotImplementedException {
         LOGGER.info("formatNodes");
 
 
         final StringBuilder output = new StringBuilder();
         output.append(S_TABLE);
         if (nodes.isEmpty()) {
             return "<p style=\"color : red\">No nodes found!</p>";
         } else {
             output.append(S_ROW).append(tdCell(
                     urlLink("/rest/testbed/" + nodes.get(0).getSetup().getId() + "/node", "Nodes")
                             + " (" + nodes.size() + ") ["
                             + urlLink("/rest/testbed/" + nodes.get(0).getSetup().getId() + "/node/raw", "raw") + ", "
                             + urlLink("/rest/testbed/" + nodes.get(0).getSetup().getId() + "/node/json", "json")
                             + "]"
 
             )).append(E_ROW);
             for (final Node node : nodes) {
                 output.append(S_ROW).append(tdCell(urlLink(
                         "/rest/testbed/" + node.getSetup().getTestbed().getId() + "/node/" + node.getName() + "/"
                         , node.getName()
                 ))).append(E_ROW);
             }
         }
         output.append(E_TABLE);
         return output.toString();
     }
 
     @Override
     public final String formatUniqueLastNodeReadings(final List<NodeCapability> nodeCapabilities)
             throws NotImplementedException {
         throw new NotImplementedException();
     }
 
     @Override
     @Cachable
     public final String formatLinks(final List<Link> links) throws NotImplementedException {
         final StringBuilder output = new StringBuilder();
         output.append(S_TABLE);
         if (links.isEmpty()) {
             return "<p style=\"color : red\">No links found!</p>";
         } else {
             output.append(S_ROW).append(tdCell(
                     urlLink("/rest/testbed/" + links.get(0).getSetup().getId() + "/link", "Links")
                             + " (" + links.size() + ") ["
                             + urlLink("/rest/testbed/" + links.get(0).getSetup().getId() + "/link/raw", "raw") + ", "
                             + urlLink("/rest/testbed/" + links.get(0).getSetup().getId() + "/link/json", "json")
                             + "]"
 
             )).append(E_ROW);
             for (final Link link : links) {
                 output.append(S_ROW).append(tdCell(urlLink(
                         "/rest/testbed/" + link.getSetup().getTestbed().getId() + "/link/"
                                 + link.getSource().getName() + "/" + link.getTarget().getName()
                         , "[" + link.getSource().getName() + "," + link.getTarget().getName() + "]"
                 ))).append(E_ROW);
             }
         }
         output.append(E_TABLE);
         return output.toString();
     }
 
     @Override
     public final String formatLastReadings(final List<LastNodeReading> lastNodeReadings,
                                            final List<LastLinkReading> lastLinkReadings)
             throws NotImplementedException {
         throw new NotImplementedException();
     }
 
 
     @Override
     public final String describeNode(final Node node, final String requestURL, final String requestURI,
                                      final String nodeDescription, final Position nodePos)
             throws NotImplementedException {
         throw new NotImplementedException();
     }
 
 
     @Override
     public final String describeTestbed(final Testbed testbed, final String requestURL, final String requestURI,
                                         final List<Node> nodes, final Map<Node, String> descriptionMap,
                                         final Map<Node, List<NodeCapability>> capabilityMap,
                                         final Map<Node, Position> originMap) throws NotImplementedException {
         throw new NotImplementedException();
     }
 
     @Override
     public final String showTestbed(final Testbed testbed, List<Node> nodes, final List<Link> links,
                                     final List<Capability> capabilities) throws NotImplementedException {
         final StringBuilder output = new StringBuilder();
         output.append(S_TABLE);
 
         output.append(S_ROW).append(tdCell("Testbed ID"))
                 .append(tdCell(String.valueOf(testbed.getId()))).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed Description"))
                 .append(tdCell(String.valueOf(testbed.getDescription()))).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed Name"))
                 .append(tdCell(String.valueOf(testbed.getName()))).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed URN prefix"))
                 .append(tdCell(String.valueOf(testbed.getUrnPrefix()))).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed Timezone"))
                 .append(tdCell(String.valueOf(testbed.getTimeZone().getDisplayName()))).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed URL"))
                 .append(tdCell(String.valueOf(testbed.getUrl()))).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed SNAA URL"))
                 .append(tdCell(String.valueOf(testbed.getSnaaUrl()))).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed RS URL"))
                 .append(tdCell(String.valueOf(testbed.getRsUrl()))).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed Session Management URL "))
                 .append(tdCell(String.valueOf(testbed.getSessionUrl()))).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed Federated Testbed"))
                 .append(tdCell(String.valueOf(testbed.getFederated()))).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed Status Page"))
                 .append(tdCell(urlLink("/rest/testbed/" + testbed.getId() + "/status", "status page"))).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed GeoRSS feed"))
                 .append(tdCell(urlLink(
                         "/rest/testbed/" + testbed.getId() + "/georss"
                         , "GeoRss Feed")
                         + "   <a href='http://maps.google.com/maps?q="
                         + baseUrl + "/rest/testbed/" + testbed.getId() + "/georss"
                         + "'>View On Google Maps</a>"
                 )).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed KML feed"))
                 .append(tdCell(urlLink(
                         "/rest/testbed/" + testbed.getId() + "/kml"
                         , "Kml Feed")
                         + "   <a href='http://maps.google.com/maps?q="
                         + baseUrl + "/rest/testbed/" + testbed.getId() + "/kml"
                         + "'>View On Google Maps</a>"
                 )).append(E_ROW);
         output.append(S_ROW).append(tdCell("Testbed WiseML"))
                 .append(tdCell(urlLink("/rest/testbed/" + testbed.getId() + "/wiseml", "WiseML"))).append(E_ROW);
 
         output.append(E_TABLE);
         output.append(S_TABLE);
         output.append(S_ROW);
         output.append("<td style=\"vertical-align:top\"> " + formatNodes(nodes) + "</td>");
         output.append("<td style=\"vertical-align:top\"> " + formatLinks(links) + "</td>");
         output.append("<td style=\"vertical-align:top\"> " + formatCapabilities(testbed, capabilities) + "</td>");
         output.append(E_ROW);
         output.append(E_TABLE);
 
         return output.toString();
     }
 
     /**
      * Creates an HTML table header cell with opening and closing tags.
      *
      * @param contents the text to add inside the cell.
      * @return the HTML complete tag.
      */
     private String thCell(final String contents) {
         return thCell(contents, "");
     }
 
     /**
      * Creates an HTML table header cell with opening and closing tags.
      *
      * @param contents the text to add inside the cell.
      * @return the HTML complete tag.
      */
     private String thCell(final String contents, final String className) {
         Tag thTag = new Tag("th");
         thTag.addParameter("class", className);
         thTag.add(contents);
         return thTag.toString();
     }
 
     /**
      * Creates an HTML table body cell with opening and closing tags.
      *
      * @param contents the text to add inside the cell.
      * @return the HTML complete tag.
      */
     private String tdCell(final String contents) {
         return tdCell(contents, "");
     }
 
     /**
      * Creates an HTML table body cell with opening and closing tags.
      *
      * @param contents the text to add inside the cell.
      * @return the HTML complete tag.
      */
     private String tdCell(final String contents, final String className) {
         Tag tdTag = new Tag("td");
         tdTag.addParameter("class", className);
         tdTag.add(contents);
         return tdTag.toString();
     }
 
     /**
      * Creates an HTML link tag.
      *
      * @param url  the url to point to.
      * @param name the text to display.
      * @return the complete tag.
      */
     private String urlLink(final String url, final String name) {
         Tag aTag = new Tag("a");
         aTag.add(name);
         aTag.addParameter("href", baseUrl + url);
         return aTag.toString();
     }
 
     private String urlLink(final Node node) {
         Tag aTag = new Tag("a");
         aTag.add(node.getName());
         aTag.addParameter("href", new StringBuilder().append(baseUrl).append("/rest/testbed/").append(node.getSetup().getId()).append("/node/").append(node.getName()).append("/").toString());
         return aTag.toString();
     }
 
     private String urlLink(final Link link) {
         Tag aTag = new Tag("a");
         aTag.add(new StringBuilder().append("[").append(link.getSource().getName()).append(",").append(link.getTarget().getName()).append("]").toString());
         aTag.addParameter("href", new StringBuilder().append(baseUrl).append("/rest/testbed/").append(link.getSetup().getId()).append("/link/").append(link.getSource().getName()).append("/").append(link.getTarget().getName()).toString());
         return aTag.toString();
     }
 
     private String urlLink(NodeCapability nodeCapability) {
         Tag aTag = new Tag("a");
         aTag.add(nodeCapability.getCapability().getName());
         aTag.addParameter("href", new StringBuilder().append(baseUrl).append("/rest/testbed/").append(nodeCapability.getNode().getSetup().getId()).append("/capability/").append(nodeCapability.getCapability().getName()).toString());
         return aTag.toString();
     }
 
     private String urlLink(LinkCapability linkCapability) {
         Tag aTag = new Tag("a");
         aTag.add(linkCapability.getCapability().getName());
         aTag.addParameter("href", new StringBuilder().append(baseUrl).append("/rest/testbed/").append(linkCapability.getLink().getSource().getSetup().getId()).append("/capability/").append(linkCapability.getCapability().getName()).toString());
         return aTag.toString();
     }
 
     private String urlLink(Testbed testbed) {
         Tag aTag = new Tag("a");
         aTag.add(testbed.getName());
         aTag.addParameter("href", new StringBuilder().append(baseUrl).append("/rest/testbed/").append(testbed.getId()).toString());
         return aTag.toString();
     }
 
 
 }
