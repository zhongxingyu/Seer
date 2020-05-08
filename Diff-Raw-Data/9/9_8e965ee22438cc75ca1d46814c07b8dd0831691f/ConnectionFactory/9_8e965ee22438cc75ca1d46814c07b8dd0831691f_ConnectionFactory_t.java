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
 package nl.surfnet.bod.support;
 
 import java.util.UUID;
 
 import nl.surfnet.bod.domain.Connection;
 import nl.surfnet.bod.domain.Reservation;
 
 import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
 import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
 import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;
 
 public class ConnectionFactory {
 
   private int desiredBandwidth;
   private String requesterNSA = "nsa:requester:surfnet.nl";
   private String providerNSA = "nsa:surfnet.nl";
   private String connectionId = UUID.randomUUID().toString();
   private String sourceStpId = "source port";
   private String destinationStpId = "destination port";
   private ConnectionStateType currentState = ConnectionStateType.INITIAL;
   private Reservation reservation = new ReservationFactory().create();
   private final String globalReservationId = UUID.randomUUID().toString();
   private final ServiceParametersType serviceParameters = new ServiceParametersType();
  private String protectionType = "PROTECTED";
   private long id = 0L;
 
   public Connection create() {
     Connection connection = new Connection();
 
     connection.setId(id);
     connection.setDesiredBandwidth(desiredBandwidth);
     connection.setRequesterNsa(requesterNSA);
     connection.setProviderNsa(providerNSA);
     connection.setConnectionId(connectionId);
     connection.setSourceStpId(sourceStpId);
     connection.setDestinationStpId(destinationStpId);
     connection.setCurrentState(currentState);
     connection.setReservation(reservation);
     connection.setGlobalReservationId(globalReservationId);
     connection.setServiceParameters(serviceParameters);
    connection.setProtectionType(protectionType);
 
     ServiceTerminationPointType sourceStp = new ServiceTerminationPointType();
     sourceStp.setStpId(sourceStpId);
     ServiceTerminationPointType dstStp = new ServiceTerminationPointType();
     dstStp.setStpId(destinationStpId);
 
     PathType pathType = new PathType();
     pathType.setDestSTP(dstStp);
     pathType.setSourceSTP(sourceStp);
     connection.setPath(pathType);
 
     return connection;
   }
 
   public ConnectionFactory setReservation(Reservation reservation) {
     this.reservation = reservation;
     return this;
   }
 
   public ConnectionFactory setDestinationStpId(String destinationStpId) {
     this.destinationStpId = destinationStpId;
     return this;
   }
 
   public ConnectionFactory setSourceStpId(String sourceStpId) {
     this.sourceStpId = sourceStpId;
     return this;
   }
 
  public ConnectionFactory setProtectionType(String protectionType) {
    this.protectionType = protectionType;
    return this;
  }

   public ConnectionFactory setConnectionId(String connectionId) {
     this.connectionId = connectionId;
     return this;
   }
 
   public ConnectionFactory setProviderNSA(String providerNSA) {
     this.providerNSA = providerNSA;
     return this;
   }
 
   public ConnectionFactory setRequesterNSA(String requesterNSA) {
     this.requesterNSA = requesterNSA;
     return this;
   }
 
   public ConnectionFactory setDesiredBandwidth(int desiredBandwidth) {
     this.desiredBandwidth = desiredBandwidth;
     return this;
   }
 
   public ConnectionFactory setCurrentState(ConnectionStateType currentState) {
     this.currentState = currentState;
     return this;
   }
 
   public ConnectionFactory setId(long id) {
     this.id = id;
     return this;
   }
 }
