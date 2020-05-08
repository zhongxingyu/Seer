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
 package nl.surfnet.bod.nsi.ws.v1sc;
 
 import static nl.surfnet.bod.domain.ReservationStatus.FAILED;
 import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.verifyZeroInteractions;
 import nl.surfnet.bod.domain.Connection;
 import nl.surfnet.bod.domain.NsiRequestDetails;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.service.ReservationStatusChangeEvent;
 import nl.surfnet.bod.support.ConnectionFactory;
 import nl.surfnet.bod.support.ReservationFactory;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
 
 import com.google.common.base.Optional;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ConnectionServiceProviderListenerTest {
 
   @InjectMocks
   private ConnectionServiceProviderListener subject;
 
   @Mock
   private ConnectionServiceProviderWs connectionServiceProviderMock;
 
   @Test
   public void absentNsiRequestDetailsShouldDoNothing() {
     Reservation reservation = new ReservationFactory().create();
     ReservationStatusChangeEvent event =
         new ReservationStatusChangeEvent(REQUESTED, reservation, Optional.<NsiRequestDetails>absent());
 
     subject.onStatusChange(event);
 
     verifyZeroInteractions(connectionServiceProviderMock);
   }
 
   @Test
   public void reserveFailedWithReason() {
     String failedReason = "No available bandwidth";
     Optional<NsiRequestDetails> requestDetails = Optional.of(new NsiRequestDetails("http://localhost/reply", "123456789"));
 
     Connection connection = new ConnectionFactory().setCurrentState(ConnectionStateType.RESERVING).create();
     Reservation reservation = new ReservationFactory()
       .setStatus(FAILED)
       .setConnection(connection)
       .setFailedReason(failedReason).create();
     ReservationStatusChangeEvent event =
         new ReservationStatusChangeEvent(REQUESTED, reservation, requestDetails);
 
     subject.onStatusChange(event);
 
     verify(connectionServiceProviderMock).reserveFailed(
         connection, requestDetails.get(), Optional.of(failedReason));
   }
 
 }
