 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.nsi;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 import java.util.Calendar;
 
 import javax.annotation.Resource;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import nl.surfnet.bod.domain.*;
 import nl.surfnet.bod.nsi.ws.NsiConstants;
 import nl.surfnet.bod.nsi.ws.v1sc.ConnectionServiceProvider;
 import nl.surfnet.bod.repo.*;
 import nl.surfnet.bod.support.*;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ogf.schemas.nsi._2011._10.connection._interface.GenericAcknowledgmentType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;
 import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
 import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;
 import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 
 import com.google.common.collect.Lists;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-integration.xml",
     "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
 @TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
 public class ConnectionServiceProviderTestIntegration extends AbstractTransactionalJUnit4SpringContextTests {
 
   private static MockHttpServer requesterEndpoint = new MockHttpServer(NsiReservationFactory.PORT);
 
   @Resource(name = "nsiProvider_v1_sc")
   private ConnectionServiceProvider nsiProvider;
 
   @Resource
   private VirtualPortRepo virtualPortRepo;
 
   @Resource
   private PhysicalPortRepo physicalPortRepo;
 
   @Resource
   private VirtualResourceGroupRepo virtualResourceGroupRepo;
 
   @Resource
   private PhysicalResourceGroupRepo physicalResourceGroupRepo;
 
   @Resource
   private InstituteRepo instituteRepo;
 
   private final String correlationId = "urn:uuid:f32cc82e-4d87-45ab-baab-4b7011652a2e";
   private final String virtualResourceGroupName = "nsi:group";
   private VirtualPort sourceVirtualPort;
   private VirtualPort destinationVirtualPort;
 
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
     requesterEndpoint.addResponse("/bod/nsi/requester", new ClassPathResource(
         "web/services/nsi/mockNsiReservationFailedResponse.xml"));
     requesterEndpoint.startServer();
   }
 
   @AfterClass
   public static void tearDownAfterClass() throws Exception {
     requesterEndpoint.stopServer();
   }
 
   @Before
   public void setup() {
     VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory()
       .setName(virtualResourceGroupName)
       .create();
     virtualResourceGroup = virtualResourceGroupRepo.save(virtualResourceGroup);
 
     Institute institute = instituteRepo.findAll().get(0);
     PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().setInstitute(institute).create();
     physicalResourceGroup = physicalResourceGroupRepo.save(physicalResourceGroup);
 
     PhysicalPort savedSourcePp = physicalPortRepo.save(
         new PhysicalPortFactory().setPhysicalResourceGroup(physicalResourceGroup).create());
     PhysicalPort savedDestinationPp = physicalPortRepo.save(
         new PhysicalPortFactory().setPhysicalResourceGroup(physicalResourceGroup).create());
 
     VirtualPort sourcePort = new VirtualPortFactory()
       .setMaxBandwidth(100)
       .setPhysicalPort(savedSourcePp)
       .setVirtualResourceGroup(virtualResourceGroup).create();
     sourceVirtualPort = virtualPortRepo.save(sourcePort);
 
     VirtualPort destinationPort = new VirtualPortFactory()
       .setMaxBandwidth(100)
       .setPhysicalPort(savedDestinationPp)
       .setVirtualResourceGroup(virtualResourceGroup)
       .create();
     destinationVirtualPort = virtualPortRepo.save(destinationPort);
 
     virtualResourceGroup.setVirtualPorts(Lists.newArrayList(sourceVirtualPort, destinationVirtualPort));
     virtualResourceGroup.setSurfconextGroupId("some:surf:conext:group:id");
     virtualResourceGroup = virtualResourceGroupRepo.save(virtualResourceGroup);
   }
 
   @Test
   public void shouldReturnGenericAcknowledgement() throws Exception {
     final XMLGregorianCalendar startTime = DatatypeFactory.newInstance().newXMLGregorianCalendar();
     startTime.setDay(Calendar.getInstance().get(Calendar.DATE) + 7);
     startTime.setMonth(Calendar.getInstance().get(Calendar.MONTH));
     startTime.setYear(Calendar.getInstance().get(Calendar.YEAR) + 1);
 
     final XMLGregorianCalendar endTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(
         startTime.toGregorianCalendar());
     endTime.setDay(startTime.getDay() + 1);
 
     final PathType path = new PathType();
 
     final ServiceTerminationPointType dest = new ServiceTerminationPointType();
    dest.setStpId(NsiConstants.NS_NETWORK + ":" + sourceVirtualPort.getId());
     path.setDestSTP(dest);
 
     final ServiceTerminationPointType source = new ServiceTerminationPointType();
    source.setStpId(NsiConstants.NS_NETWORK + ":" + destinationVirtualPort.getId());
     path.setSourceSTP(source);
 
     final ReserveRequestType reservationRequest = new NsiReservationFactory().setScheduleStartTime(startTime)
         .setScheduleEndTime(endTime).setCorrelationId(correlationId).setProviderNsa(NsiConstants.URN_PROVIDER_NSA)
         .setPath(path).createReservation();
 
     // send reserve request
     final GenericAcknowledgmentType genericAcknowledgmentType = nsiProvider.reserve(reservationRequest);
 
     assertThat(genericAcknowledgmentType.getCorrelationId(), is(correlationId));
 
     // send provision request
     final ProvisionRequestType provisionRequestType = new ProvisionRequestType();
     provisionRequestType.setCorrelationId(correlationId);
     provisionRequestType.setReplyTo(reservationRequest.getReplyTo());
     final GenericRequestType genericRequestType = new GenericRequestType();
     genericRequestType.setProviderNSA(reservationRequest.getReserve().getProviderNSA());
     genericRequestType.setRequesterNSA(reservationRequest.getReserve().getRequesterNSA());
     genericRequestType.setConnectionId(reservationRequest.getReserve().getReservation().getConnectionId());
     provisionRequestType.setProvision(genericRequestType);
 
 
     final GenericAcknowledgmentType provisionAck = nsiProvider.provision(provisionRequestType);
     assertThat(provisionAck.getCorrelationId(), is(correlationId));
   }
 }
