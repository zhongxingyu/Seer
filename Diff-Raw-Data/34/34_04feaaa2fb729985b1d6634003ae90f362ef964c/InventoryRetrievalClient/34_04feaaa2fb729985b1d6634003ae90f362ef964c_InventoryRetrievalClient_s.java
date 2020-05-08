 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.nbi.mtosi;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
 import org.tmforum.mtop.msi.wsdl.sir.v1_0.GetServiceInventoryException;
 import org.tmforum.mtop.msi.wsdl.sir.v1_0.ServiceInventoryRetrievalHttp;
 import org.tmforum.mtop.msi.wsdl.sir.v1_0.ServiceInventoryRetrievalRPC;
 import org.tmforum.mtop.msi.xsd.sir.v1.*;
 import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.RfsList;
 import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.SapList;
 import org.tmforum.mtop.msi.xsd.sir.v1.ObjectFactory;
 import org.tmforum.mtop.sb.xsd.svc.v1.*;
 
 import com.google.common.annotations.VisibleForTesting;
 
 @Service
 public class InventoryRetrievalClient {
 
   private static final String SAP_CACHE = "sapCache";
 
   private static final String WSDL_LOCATION =
       "/mtosi/2.1/DDPs/ManageServiceInventory/IIS/wsdl/ServiceInventoryRetrieval/ServiceInventoryRetrievalHttp.wsdl";
 
   private final Logger logger = LoggerFactory.getLogger(getClass());
 
   private final ServiceInventoryRetrievalHttp service;
 
   private final String endPoint;
 
   // Quick cache solution to prevent roundtrip off two minutes, could cause
   // memory problems in future
   private final ConcurrentMap<String, SapList> sapCache = new ConcurrentHashMap<String, ServiceInventoryDataType.SapList>();
 
   @Autowired
   public InventoryRetrievalClient(@Value("${nbi.mtosi.inventory.retrieval.endpoint}") String endPoint) {
     this.endPoint = endPoint;
     this.service = new ServiceInventoryRetrievalHttp(this.getClass().getResource(WSDL_LOCATION),
         new QName("http://www.tmforum.org/mtop/msi/wsdl/sir/v1-0", "ServiceInventoryRetrievalHttp"));
   }
 
   protected RfsList getRfsInventory() {
     try {
       GetServiceInventoryResponse serviceInventory = getServiceInventoryWithRfsFilter();
 
       return serviceInventory.getInventoryData().getRfsList();
     }
     catch (GetServiceInventoryException e) {
       logger.error("Error: ", e);
       return null;
     }
   }
 
   @VisibleForTesting
   GetServiceInventoryResponse getServiceInventoryWithRfsFilter() throws GetServiceInventoryException {
     GetServiceInventoryRequest inventoryRequest = new ObjectFactory().createGetServiceInventoryRequest();
     addRfsFilter(inventoryRequest);
 
     return retrieveServiceInventory(inventoryRequest);
   }
 
   @VisibleForTesting
   GetServiceInventoryResponse getServiceInventoryWithSapFilter() throws GetServiceInventoryException {
 
     GetServiceInventoryRequest inventoryRequest = new ObjectFactory().createGetServiceInventoryRequest();
     addSapFilter(inventoryRequest);
 
     return retrieveServiceInventory(inventoryRequest);
   }
 
   @VisibleForTesting
   SapList getSapInventory() {
     try {
       if (sapCache.get(SAP_CACHE) == null) {
 
         sapCache.put(SAP_CACHE, getServiceInventoryWithSapFilter().getInventoryData().getSapList());
       }
 
       return sapCache.get(SAP_CACHE);
     }
     catch (GetServiceInventoryException e) {
       logger.error("Error: ", e);
       return null;
     }
   }
 
   private ServiceInventoryRetrievalRPC getServiceProxy() {
     ServiceInventoryRetrievalRPC proxy = service.getServiceInventoryRetrievalSoapHttp();
     ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
     return proxy;
   }
 
   private void addSapFilter(GetServiceInventoryRequest request) {
     addFilter(request, "SAP");
   }
 
   private void addRfsFilter(GetServiceInventoryRequest request) {
     addFilter(request, "RFS");
   }
 
   private void addFilter(GetServiceInventoryRequest request, String filter) {
     request.setFilter(getInventoryRequestSimpleFilter(filter));
   }
 
   private SimpleServiceFilterType getInventoryRequestSimpleFilter(String filter) {
     final SimpleServiceFilterType simpleFilter = new ObjectFactory().createSimpleServiceFilterType();
     simpleFilter.getScopeAndSelection().add(GranularityType.FULL);
     final SimpleServiceFilterType.Scope scope = new ObjectFactory().createSimpleServiceFilterTypeScope();
     scope.setServiceObjectType(filter);
     simpleFilter.getScopeAndSelection().add(scope);
 
     return simpleFilter;
   }
 
   public List<PhysicalPort> getUnallocatedPorts() {
     final List<PhysicalPort> mtosiPorts = new ArrayList<>();
 
     for (final ServiceAccessPointType sap : getSapList()) {
       // Only unlocked and enabled ports are ready to use
       if ((AdminStateType.UNLOCKED != sap.getAdminState() && (OperationalStateType.ENABLED != sap.getOperationalState()))) {
         // TODO not implemented by Cienna 1C, yet. Just log for now
         // continue;
         logger.debug("Sap has incorrect adminstate and/or operationalState: {}", sap);
       }
 
       mtosiPorts.add(createPhysicalPort(sap));
     }
     return mtosiPorts;
   }
 
   private List<ServiceAccessPointType> getRfsPorts() {
     List<ServiceAccessPointType> saps = new ArrayList<>();
 
     for (ResourceFacingServiceType rfs : getRfsInventory().getRfs()) {
       // Only unlocked and enabled ports are ready to use
       if ((AdminStateType.UNLOCKED != rfs.getAdminState() && (OperationalStateType.ENABLED != rfs.getOperationalState()))) {
         // TODO not implemented by Cienna 1C, yet. Just log for now
         // continue;
         logger.debug("Rfs has incorrect adminstate and/or operationalState: {}", rfs);
       }
       saps.addAll(rfs.getSapList());
     }
 
     return saps;
   }
 
   /**
    * Enables switching between the source retrieving the SAPs to be mapped.
    * 
    * @return List<ServiceAccessPointType>
    */
   private List<ServiceAccessPointType> getSapList() {
      return getSapInventory().getSap();
 //    return getRfsPorts();
   }
 
   private GetServiceInventoryResponse retrieveServiceInventory(GetServiceInventoryRequest inventoryRequest)
       throws GetServiceInventoryException {
     ServiceInventoryRetrievalRPC proxy = getServiceProxy();
 
     GetServiceInventoryResponse serviceInventory =
         proxy.getServiceInventory(HeaderBuilder.buildInventoryHeader(endPoint), inventoryRequest);
     return serviceInventory;
   }
 
   @VisibleForTesting
   PhysicalPort createPhysicalPort(final ServiceAccessPointType sap) {
     final String nmsSapName = sap.getName().getValue().getRdn().get(0).getValue();
 
     String managedElement = null, ptp = null, nmsPortSpeed = null, supportedServiceType = null;
     boolean isVlanRequired = false;
 
     for (final RelativeDistinguishNameType relativeDistinguishNameType : sap.getResourceRef().getRdn()) {
       if (relativeDistinguishNameType.getType().equals("ME")) {
         managedElement = relativeDistinguishNameType.getValue();
       }
       else if (relativeDistinguishNameType.getType().equals("PTP")) {
         ptp = relativeDistinguishNameType.getValue();
       }
     }
 
     for (final ServiceCharacteristicValueType serviceCharacteristicValueType : sap.getDescribedByList()) {
       final String value = serviceCharacteristicValueType.getValue();
       for (final RelativeDistinguishNameType rdn : serviceCharacteristicValueType.getSscRef().getRdn()) {
         final String rdnValue = rdn.getValue();
         if ("AdministrativeSpeedRate".equals(rdnValue)) {
           nmsPortSpeed = value;
         }
         else if ("SupportedServiceType".equals(rdnValue)) {
           supportedServiceType = value;
           isVlanRequired = determineVlanRequired(value);
         }
       }
     }
 
     final PhysicalPort physicalPort = new PhysicalPort(isVlanRequired);
    physicalPort.setNmsPortId(MtosiUtils.composeNmsPortId(managedElement, MtosiUtils.convertToLongPtP(ptp)));
     physicalPort.setNmsNeId(managedElement);
     physicalPort.setBodPortId(nmsSapName);
     physicalPort.setNmsPortSpeed(nmsPortSpeed);
     physicalPort.setNmsSapName(nmsSapName);
     physicalPort.setNocLabel(managedElement + "@" + ptp);
     physicalPort.setSupportedServiceType(supportedServiceType);
     physicalPort.setSignalingType("NA");
     logger.debug("Retrieve physicalport: {}", physicalPort);
     return physicalPort;
   }
 
   public int getUnallocatedMtosiPortCount() {
     return getSapInventory().getSap().size();
   }
 
   /**
    * Refresh cache on startup and every x minutes
    */
   @Scheduled(initialDelay = 0, cron = "0 */15 * * * *")
   public void refreshSapCache() {
     sapCache.remove(SAP_CACHE);
     getSapInventory();
     logger.debug("Sap cache refreshed");
   }
 
   @VisibleForTesting
   boolean determineVlanRequired(final String supportedServiceType) {
     return "EVPL".equals(supportedServiceType) || "EVPLAN".equals(supportedServiceType);
   }
 }
