 package net.es.oscars.pss.dragon.util;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.text.DecimalFormat;
 import java.util.List;
 import java.util.ArrayList;
 
 import edu.internet2.hopi.dragon.*;
 import org.ogf.schema.network.topology.ctrlplane.*;
 
 import org.apache.log4j.Logger;
 
 import net.es.oscars.api.soap.gen.v06.ResDetails;
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import net.es.oscars.utils.topology.*;
 import net.es.oscars.pss.beans.PSSException;
 import net.es.oscars.pss.util.URNParser;
 import net.es.oscars.pss.util.URNParserResult;
 import net.es.oscars.pss.util.ClassFactory;
 import net.es.oscars.pss.api.DeviceAddressResolver;
 
 
 public class DRAGONUtils {
     private static Logger log = Logger.getLogger(DRAGONUtils.class);
 
     public static String getEndPoint(ResDetails res, boolean reverse)
             throws PSSException{
         String epUrn = null;
         try {
             CtrlPlanePathContent path = res.getReservedConstraint().getPathInfo().getPath();
             String localDomainId = PathTools.getLocalDomainId();
             CtrlPlaneLinkContent ingLink = PathTools.getIngressLink(localDomainId, path);
             CtrlPlaneLinkContent egrLink = PathTools.getEgressLink(localDomainId, path);
             if (reverse)
                 epUrn = egrLink.getId();
             else
                 epUrn = ingLink.getId();
         } catch (Exception e) {
             throw new PSSException("unable to resolve end point urn from resvDetails");
         }
         return epUrn;
     }
 
     public static String getDeviceId(ResDetails res, boolean reverse)
             throws PSSException{
         String epUrn = getEndPoint(res, reverse);
         URNParserResult urnParser = URNParser.parseTopoIdent(epUrn);
         return urnParser.getNodeId();
     }
 
     public static String getDeviceAddress(String deviceId) throws PSSException {
         DeviceAddressResolver res = ClassFactory.getInstance().getDeviceResolver();
         if (res == null){
             throw new PSSException("unable to load device resolver");
         }
         String deviceAddress = res.getDeviceAddress(deviceId);
         return deviceAddress;
     }
 
     public static String getUrnField(String urn, String field) {
         int start = urn.indexOf(field+"=");
         if (start == -1)
             return null;
         start += field.length()+1;
         int end = urn.indexOf(':', start);
         if (end == -1 )
             end = urn.length();
         return urn.substring(start, end);
     }
 
     public static InetAddress getVlsrRouterId(String urn) throws PSSException{
         String nodeTopoId = getUrnField(urn, "node");
         InetAddress routerId = null;
         DeviceAddressResolver res = ClassFactory.getInstance().getDeviceResolver();
         if (res == null){
             throw new PSSException("unable to load device address resolver");
         }
         String vlsrAddress = null;
         try {
             vlsrAddress = res.getDeviceAddress(nodeTopoId+".vlsr");
         } catch (PSSException e) {
             vlsrAddress = nodeTopoId;
         }
         try {
             routerId = InetAddress.getByName(vlsrAddress);
         } catch (UnknownHostException e) {
             throw  new PSSException("unable to locate VLSR RouterID for "
                 + nodeTopoId + " (hint: try adding " + nodeTopoId
                 + ".vlsr in config-device-addresses.yaml");
         }
         return routerId;
     }
 
     public static DragonLocalID getLocalId(String urn, int vtag, boolean hasNarb)
             throws PSSException {
         String nodeTopoId = getUrnField(urn, "node");
         String portTopoId = getUrnField(urn, "port");
         boolean tagged = (vtag > 0);
         String type = null;
         int number = 0;
 
         /* Get Type */
         if(portTopoId.indexOf('P') == 0){
             log.info("untagged port local-id (override)");
             type = DragonLocalID.UNTAGGED_PORT;
             String strNum = portTopoId.substring(1);
             number = getLocalIdNum(urn, false);
             log.info("local-id value " + number);
         }else if(portTopoId.indexOf('G') == 0){
             log.info("untagged group local-id (override)");
             type = DragonLocalID.UNTAGGED_PORT_GROUP;
             number = Math.abs(vtag);
             log.info("local-id value " + number);
         }else if(portTopoId.indexOf('T') == 0){
             log.info("tagged group local-id (override)");
             type = DragonLocalID.TAGGED_PORT_GROUP;
             number = Math.abs(vtag);
             log.info("local-id value " + number);
         }else if(portTopoId.indexOf('S') == 0){
             log.info("subnet-interface local-id");
             type = DragonLocalID.SUBNET_INTERFACE;
             number = Integer.parseInt(portTopoId.substring(1));
             log.info("local-id value " + number);
         }else if(tagged){
             log.info("tagged local-id");
             type = DragonLocalID.TAGGED_PORT_GROUP;
             number = vtag;
             log.info("local-id value " + number);
         }else{
             log.info("untagged local-id");
             /* Get number */
             number = getLocalIdNum(urn, false);
             type = DragonLocalID.UNTAGGED_PORT;
             log.info("local-id value " + number);
         }
         return new DragonLocalID(number, type);
     }
 
     public static int getLocalIdNum(String urn, boolean matchAny) throws PSSException{
         String portTopoId = portTopoId = getUrnField(urn, "port");
         String[] componentList = portTopoId.split("-");
         int number = 0;
 
         /* If just a number return the number...*/
         if(componentList.length == 1){
             try{
                 number = Integer.parseInt(componentList[0]);
                 return number;
             }catch(Exception e){}
         }
 
         /* If in form K-M-N return local ID value... */
         if(componentList.length == 3){
             //Remove try block when E type no longer needed
             try{
                 int k = Integer.parseInt(componentList[0]);
                 int m = Integer.parseInt(componentList[1]);
                 int n = Integer.parseInt(componentList[2]);
 
                 number = (k << 12) + (m << 8) + n;
                 return number;
             }catch(Exception e){}
         }
 
         /* If doesn't match any of the above but isn't required to be an
            ethernet type return -1 */
         if(matchAny || portTopoId.indexOf('G') == 0){
             return -1;
         }
 
         /* Throw exception because ethernet type required but given something
            else */
         throw new PSSException("Port ID must be in form L or K-M-N where" +
             " L = (K << 12) + (M << 8) + N");
     }
 
     public static boolean escapeLocalIdCreation(String urn) {
         String portTopoId = getUrnField(urn, "port");
         if(portTopoId.indexOf('E') == 0 || portTopoId.indexOf('G') == 0
            || portTopoId.indexOf('U') == 0 || portTopoId.indexOf('S') == 0)
             return true;
         return false;
     }
 
 
     public static String prepareBandwidth(int bw, CtrlPlanePathContent path) throws PSSException{
         String bwString = null;
         DecimalFormat df = new DecimalFormat("#.###");
         boolean isSonet = false;
 
         List<CtrlPlaneHopContent> hops = path.getHop();
         for (CtrlPlaneHopContent hop: hops) {
             try {
                 if (!PathTools.getLocalDomainId().equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, NMWGParserUtil.DOMAIN_TYPE)))) {
                     continue;
                 }
             } catch (OSCARSServiceException e) {
                 throw new PSSException("Malformed path hop object: id=" + hop.getId());
             }
             CtrlPlaneLinkContent link = hop.getLink();
             if(link == null){ continue; }
             if(link.getSwitchingCapabilityDescriptors().getSwitchingcapType().equalsIgnoreCase("tdm")){
                 isSonet = true;
                 break;
             }
         }
 
         if(isSonet){
             int stsVal = (int)(bw/(49.536));
             double bwVal = (double)stsVal * 49.536;
             bwString = "eth" + df.format(bwVal) + "M";
         }else{
              bwString = "eth" + df.format((double)bw) + "M";
         }
         return bwString;
     }
 
     public static ArrayList<String> getPathEro(CtrlPlanePathContent path, boolean isSubnet)
             throws PSSException{
         ArrayList<String> ero = new ArrayList<String>();
        List<CtrlPlaneHopContent> hops = null;
        try {
            hops = PathTools.getLocalHops(path, PathTools.getLocalDomainId());
        } catch (OSCARSServiceException e1) {
            throw new PSSException("Error extracting local path: " + e1.getMessage());
        }
 
         int ctr = 1; // don't add first hop
         // don't care about the last edge hop
         while (ctr < hops.size()-1) {
             CtrlPlaneHopContent hop = hops.get(ctr);
             try {
                 if (!PathTools.getLocalDomainId().equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop, NMWGParserUtil.DOMAIN_TYPE)))) {
                     continue;
                 }
             } catch (OSCARSServiceException e) {
                 throw new PSSException("Malformed path hop object: id=" + hop.getId());
             }
             CtrlPlaneLinkContent link = hop.getLink();
             CtrlPlanePortContent port = hop.getPort();
             String portId = getUrnField(link.getId(), "port");
             String linkId = getUrnField(link.getId(), "link");
             if (portId == null || linkId == null) {
                 throw new PSSException("Malformed hop link urn:" + link.getId());
             }
             /* Skip hops that are DTLs and not subnets or vice versa (XOR) */
             if (((!isSubnet) && portId.startsWith("DTL")) || (isSubnet &&
                 (!portId.startsWith("DTL")))) {
                 ctr++;
                 continue;
             }
 
             /* Verify the link ID is an IPv4 address */
             try {
                 InetAddress.getByName(linkId);
             } catch (UnknownHostException e) {
                 throw  new PSSException("Invalid link id " + linkId +
                     ". Link IDs must be IP addresses on IDCs running DRAGON.");
             }
             ero.add(linkId);
             log.info((isSubnet ? "SUBNET" : "") + "ERO: " + linkId);
             ctr++;
         }
         if(ero.size() == 0){
             return null;
         }
         return ero;
     }
 
 }
