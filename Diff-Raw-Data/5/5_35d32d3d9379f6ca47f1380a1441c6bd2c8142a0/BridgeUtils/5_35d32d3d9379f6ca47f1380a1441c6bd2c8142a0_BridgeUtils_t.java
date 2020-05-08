 package net.es.oscars.pss.bridge.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import org.apache.log4j.Logger;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
 
 import net.es.oscars.api.soap.gen.v06.PathInfo;
 import net.es.oscars.api.soap.gen.v06.ResDetails;
 import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
 import net.es.oscars.pss.beans.PSSException;
 import net.es.oscars.pss.bridge.beans.DeviceBridge;
 import net.es.oscars.pss.util.URNParser;
 import net.es.oscars.pss.util.URNParserResult;
 import net.es.oscars.utils.topology.PathTools;
 
 public class BridgeUtils {
     private static Logger log = Logger.getLogger(BridgeUtils.class);
     
     public static List<String> getDeviceIds(ResDetails resDetails) throws PSSException {
         ArrayList<String> result = new ArrayList<String>();
         ReservedConstraintType rc = resDetails.getReservedConstraint();
         PathInfo pi = rc.getPathInfo();
 
         List<CtrlPlaneHopContent> hops;
         try {
             hops = PathTools.getLocalHops(pi.getPath(), PathTools.getLocalDomainId());
         } catch (OSCARSServiceException ex) {
             throw new PSSException(ex);
         }
 
 
         for (CtrlPlaneHopContent hop : hops) {
             CtrlPlaneLinkContent link = hop.getLink();
             String linkId = link.getId();
 
             URNParserResult res = URNParser.parseTopoIdent(linkId);
             if (res.getDomainId().equals(PathTools.getLocalDomainId())) {
                 if (!result.contains(res.getNodeId())) {
                     result.add(res.getNodeId());
                 }
             
             }
         }
         
         return result;
 
     }
     
     public static DeviceBridge getDeviceBridge(String deviceId, ResDetails resDetails) throws PSSException {
         DeviceBridge result = new DeviceBridge();
         
         ReservedConstraintType rc = resDetails.getReservedConstraint();
         PathInfo pi = rc.getPathInfo();
         
         String portA = null;
         String portZ = null;
         String vlanA = null;
         String vlanZ = null;
         String fullPath = "";
         List<CtrlPlaneHopContent> hops;
         try {
             hops = PathTools.getLocalHops(pi.getPath(), PathTools.getLocalDomainId());
 
         } catch (OSCARSServiceException ex) {
             throw new PSSException(ex);
         }
 
         for (CtrlPlaneHopContent hop : hops) {
             CtrlPlaneLinkContent link = hop.getLink();
             String linkId = link.getId();
             fullPath += "\n"+linkId;
         }
         for (CtrlPlaneHopContent hop : hops) {
             CtrlPlaneLinkContent link = hop.getLink();
             String linkId = link.getId();
 
             URNParserResult res = URNParser.parseTopoIdent(linkId);
             if (res.getNodeId() != null && res.getNodeId().equals(deviceId)) {
                 if (portA == null) {
                     portA = res.getPortId();
                    vlanA = link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
                 } else if (portZ == null) {
                     portZ = res.getPortId();
                    vlanZ = link.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
                 } else {
                     throw new PSSException("more than two hops on device "+deviceId+" for path "+fullPath);
                 }
             }
 
         }
         if (portA == null) {
             throw new PSSException("zero hops were on device "+deviceId+" for path "+fullPath);
         } else if (portZ == null) {
             throw new PSSException("only one hop was on device "+deviceId+" for path "+fullPath);
         }
         log.debug("device "+deviceId+" "+portA+":"+vlanA+" - "+portZ+":"+vlanZ);
         result.setDeviceId(deviceId);
         result.setPortA(portA);
         result.setPortZ(portZ);
         result.setVlanA(vlanA);
         result.setVlanZ(vlanZ);
 
         return result;
         
     }
 }
