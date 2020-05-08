 /**
  * Copyright (c) 2012, 2013 SURFnet BV
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
 package nl.surfnet.bod.domain;
 
 import java.util.Collection;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 
 import nl.surfnet.bod.util.TimeStampBridge;
 
 import org.hibernate.annotations.Type;
 import org.hibernate.search.annotations.*;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSet.Builder;
 
 /**
  * Entity which represents a Reservation for a specific connection between a
  * source and a destination point on a specific moment in time.
  *
  */
 @Entity
 @Indexed
 @Analyzer(definition = "customanalyzer")
 public class Reservation implements Loggable, PersistableDomain {
 
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @DocumentId
   private Long id;
 
   @Version
   private Integer version;
 
   @Field(index = Index.YES, store = Store.YES)
   private String name;
 
   @IndexedEmbedded
   @ManyToOne
   private VirtualResourceGroup virtualResourceGroup;
 
   @Enumerated(EnumType.STRING)
   @Field
   private ReservationStatus status = ReservationStatus.REQUESTED;
 
   @Field
   private String failedReason;
 
   @Field
   private String cancelReason;
 
   @NotNull
   @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
   @IndexedEmbedded
   private ReservationEndPoint sourcePort;
 
   @NotNull
   @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
   @IndexedEmbedded
   private ReservationEndPoint destinationPort;
 
   @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
   @Field
   @FieldBridge(impl = TimeStampBridge.class)
   private DateTime startDateTime;
 
   @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
   @Field
   @FieldBridge(impl = TimeStampBridge.class)
   private DateTime endDateTime;
 
   @Field
   @Column(nullable = false)
   private String userCreated;
 
   @NotNull
   @Column(nullable = false)
   @Field
   private Long bandwidth;
 
   @Basic
   @Field
   private String reservationId;
 
   @NotNull
   @Column(nullable = false)
   @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
   private final DateTime creationDateTime;
 
   @OneToOne(mappedBy="reservation", cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
   @IndexedEmbedded
   private ConnectionV1 connectionV1;
 
   @OneToOne(mappedBy="reservation", cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
   @IndexedEmbedded
   private ConnectionV2 connectionV2;
 
   @Enumerated(EnumType.STRING)
   @Field
   private ProtectionType protectionType = ProtectionType.PROTECTED;
 
   public Reservation() {
     creationDateTime = DateTime.now();
   }
 
   @Override
   public Long getId() {
     return id;
   }
 
   public void setId(Long id) {
     this.id = id;
   }
 
   public Integer getVersion() {
     return version;
   }
 
   public void setVersion(Integer version) {
     this.version = version;
   }
 
   public VirtualResourceGroup getVirtualResourceGroup() {
     return virtualResourceGroup;
   }
 
   public ReservationStatus getStatus() {
     return status;
   }
 
   public void setStatus(ReservationStatus reservationStatus) {
     this.status = reservationStatus;
   }
 
   public ReservationEndPoint getSourcePort() {
     return sourcePort;
   }
 
   /**
    * Sets the {@link #sourcePort} and the {@link #virtualResourceGroup} related
    * to this port.
    *
    * @param sourcePort
    *          The source port to set
    * @throws IllegalStateException
    *           When the {@link #virtualResourceGroup} is already set and is not
    *           equal to the one reference by the given port
    */
   public void setSourcePort(ReservationEndPoint sourcePort) {
     deriveVirtualResourceGroup(sourcePort);
     this.sourcePort = sourcePort;
   }
 
   public ReservationEndPoint getDestinationPort() {
     return destinationPort;
   }
 
   /**
    * Sets the {@link #destinationPort} and the {@link #virtualResourceGroup}
    * related to this port.
    *
    * @param destinationPort
    *          The destinationPort port to set
    * @throws IllegalStateException
    *           When the {@link #virtualResourceGroup} is already set and is not
    *           equal to the one reference by the given port
    */
   public void setDestinationPort(ReservationEndPoint destinationPort) {
     deriveVirtualResourceGroup(destinationPort);
     this.destinationPort = destinationPort;
   }
 
   private void deriveVirtualResourceGroup(ReservationEndPoint endPoint) {
     Optional<VirtualPort> virtualPort = endPoint.getVirtualPort();
     if (virtualPort.isPresent()) {
       VirtualResourceGroup group = virtualPort.get().getVirtualResourceGroup();
       if (virtualResourceGroup != null && !virtualResourceGroup.equals(group)) {
         throw new IllegalStateException(
             "Reservation contains a sourcePort and destinationPort from a different VirtualResourceGroup");
       }
       this.virtualResourceGroup = group;
     }
   }
 
   public boolean hasConsistentVirtualResourceGroups() {
     if (sourcePort.getVirtualPort().isPresent()
         && !sourcePort.getVirtualPort().get().getVirtualResourceGroup().getAdminGroup().equals(virtualResourceGroup.getAdminGroup())) {
       return false;
     }
     if (destinationPort.getVirtualPort().isPresent()
         && !destinationPort.getVirtualPort().get().getVirtualResourceGroup().getAdminGroup().equals(virtualResourceGroup.getAdminGroup())) {
       return false;
     }
     return true;
   }
 
   /**
    *
    * @return LocalTime the time part of the {@link #startDateTime}
    */
   public LocalTime getStartTime() {
     return startDateTime == null ? null : startDateTime.toLocalTime();
   }
 
   /**
    * Sets the time part of the {@link #startDateTime}
    *
    * @param startTime
    */
   public void setStartTime(LocalTime startTime) {
 
     if (startTime == null) {
       startDateTime = null;
       return;
     }
 
     if (startDateTime == null) {
       startDateTime = new DateTime(startTime);
     }
     else {
       startDateTime = startDateTime.withTime(startTime.getHourOfDay(), startTime.getMinuteOfHour(), startTime
           .getSecondOfMinute(), startTime.getMillisOfSecond());
     }
   }
 
   public String getUserCreated() {
     return userCreated;
   }
 
   public void setUserCreated(String user) {
     this.userCreated = user;
   }
 
   /**
    *
    * @return LocalDate The date part of the {@link #getStartDateTime()}
    */
   public LocalDate getStartDate() {
     return startDateTime == null ? null : startDateTime.toLocalDate();
   }
 
   /**
    * Sets the date part of the {@link #endDateTime}
    *
    * @param startDate
    */
   public void setStartDate(LocalDate startDate) {
 
     if (startDate == null) {
       startDateTime = null;
       return;
     }
 
     if (startDateTime == null) {
       startDateTime = new DateTime(startDate.toDate());
     }
     else {
       startDateTime = startDateTime
           .withDate(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth());
     }
   }
 
   /**
    *
    * @return LocalDate the date part of the {@link #endDateTime}
    */
   public LocalDate getEndDate() {
     return endDateTime == null ? null : endDateTime.toLocalDate();
   }
 
   /**
    * Sets the date part of the {@link #endDateTime}
    *
    * @param endDate
    */
   public void setEndDate(LocalDate endDate) {
     if (endDate == null) {
       endDateTime = null;
       return;
     }
 
     if (endDateTime == null) {
       this.endDateTime = new DateTime(endDate.toDate());
     }
     else {
       endDateTime = endDateTime.withDate(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth());
     }
   }
 
   /**
    *
    * @return LocalTime The time part of the {@link #endDateTime}
    */
   public LocalTime getEndTime() {
     return endDateTime == null ? null : endDateTime.toLocalTime();
   }
 
   /**
    * Sets the time part of the {@link #endDateTime}
    *
    * @param endTime
    */
   public void setEndTime(LocalTime endTime) {
 
     if (endTime == null) {
       endDateTime = null;
       return;
     }
 
     if (endDateTime == null) {
       this.endDateTime = new DateTime(endTime);
     }
     else {
       endDateTime = endDateTime.withTime(endTime.getHourOfDay(), endTime.getMinuteOfHour(),
           endTime.getSecondOfMinute(), endTime.getMillisOfSecond());
     }
   }
 
   public DateTime getEndDateTime() {
     return endDateTime;
   }
 
   public void setEndDateTime(DateTime endDateTime) {
     this.endDateTime = endDateTime;
   }
 
   public DateTime getStartDateTime() {
     return startDateTime;
   }
 
   public void setStartDateTime(DateTime startDateTime) {
     this.startDateTime = startDateTime;
   }
 
   public Long getBandwidth() {
     return bandwidth;
   }
 
   public void setBandwidth(Long bandwidth) {
     this.bandwidth = bandwidth;
   }
 
   public String getReservationId() {
     return reservationId;
   }
 
   public void setReservationId(String reservationId) {
     this.reservationId = reservationId;
   }
 
   public DateTime getCreationDateTime() {
     return creationDateTime;
   }
 
   @Override
   public Collection<String> getAdminGroups() {
     Builder<String> builder = ImmutableSet.builder();
    if (virtualResourceGroup != null) {
      builder.add(virtualResourceGroup.getAdminGroup());
    }
     if (sourcePort.getUniPort().isPresent()) {
       builder.add(sourcePort.getUniPort().get().getPhysicalResourceGroup().getAdminGroup());
     }
     if (destinationPort.getUniPort().isPresent()) {
       builder.add(destinationPort.getUniPort().get().getPhysicalResourceGroup().getAdminGroup());
     }
     return builder.build();
   }
 
   @Override
   public String getLabel() {
     return getName();
   }
 
   public String getFailedReason() {
     return failedReason;
   }
 
   public void setFailedReason(String failedReason) {
     this.failedReason = failedReason;
   }
 
   public String getName() {
     return name;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public void setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
     this.virtualResourceGroup = virtualResourceGroup;
   }
 
   public String getCancelReason() {
     return cancelReason;
   }
 
   public void setCancelReason(String cancelReason) {
     this.cancelReason = cancelReason;
   }
 
   public Optional<ConnectionV1> getConnectionV1() {
     return Optional.<ConnectionV1>fromNullable(connectionV1);
   }
 
   public Optional<ConnectionV2> getConnectionV2() {
     return Optional.<ConnectionV2>fromNullable(connectionV2);
   }
 
   public Optional<Connection> getConnection() {
     if (connectionV1 != null) {
       return Optional.<Connection>fromNullable(connectionV1);
     } else if (connectionV2 != null) {
       return Optional.<Connection>fromNullable(connectionV2);
     } else {
       return Optional.absent();
     }
   }
 
   public void setConnectionV1(ConnectionV1 connection) {
     this.connectionV1 = connection;
     this.connectionV2 = null;
   }
 
   public void setConnectionV2(ConnectionV2 connection) {
     this.connectionV1 = null;
     this.connectionV2 = connection;
   }
 
   /**
    * @return True if this reservation was made using NSI, false otherwise
    */
   public boolean isNSICreated() {
     return connectionV1 != null || connectionV2 != null;
   }
 
   public ProtectionType getProtectionType() {
     return protectionType;
   }
 
   public void setProtectionType(ProtectionType protectionType) {
     this.protectionType = protectionType;
   }
 
   @Override
   public String toString() {
     StringBuilder builder = new StringBuilder();
     builder.append("Reservation [");
     if (id != null) {
       builder.append("id=");
       builder.append(id);
       builder.append(", ");
     }
     if (version != null) {
       builder.append("version=");
       builder.append(version);
       builder.append(", ");
     }
     if (name != null) {
       builder.append("name=");
       builder.append(name);
       builder.append(", ");
     }
     if (virtualResourceGroup != null) {
       builder.append("virtualResourceGroup=");
       builder.append(virtualResourceGroup.getName());
       builder.append(", ");
     }
     if (status != null) {
       builder.append("status=");
       builder.append(status);
       builder.append(", ");
     }
     if (failedReason != null) {
       builder.append("failedReason=");
       builder.append(failedReason);
       builder.append(", ");
     }
     if (cancelReason != null) {
       builder.append("cancelReason=");
       builder.append(cancelReason);
       builder.append(", ");
     }
     if (sourcePort != null) {
       builder.append("sourcePort=");
       builder.append(sourcePort);
       builder.append(", ");
     }
     if (destinationPort != null) {
       builder.append("destinationPort=");
       builder.append(destinationPort);
       builder.append(", ");
     }
     if (startDateTime != null) {
       builder.append("startDateTime=");
       builder.append(startDateTime);
       builder.append(", ");
     }
     if (endDateTime != null) {
       builder.append("endDateTime=");
       builder.append(endDateTime);
       builder.append(", ");
     }
     if (userCreated != null) {
       builder.append("userCreated=");
       builder.append(userCreated);
       builder.append(", ");
     }
     if (bandwidth != null) {
       builder.append("bandwidth=");
       builder.append(bandwidth);
       builder.append(", ");
     }
     if (reservationId != null) {
       builder.append("reservationId=");
       builder.append(reservationId);
       builder.append(", ");
     }
     if (creationDateTime != null) {
       builder.append("creationDateTime=");
       builder.append(creationDateTime);
       builder.append(", ");
     }
     Optional<Connection> connection = getConnection();
     if (connection.isPresent()) {
       builder.append("connection=");
       builder.append(connection.get().getLabel());
       builder.append(", ");
     }
     if (protectionType != null) {
       builder.append("protectionType=");
       builder.append(protectionType);
     }
     builder.append("]");
     return builder.toString();
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + ((id == null) ? 0 : id.hashCode());
     result = prime * result + ((version == null) ? 0 : version.hashCode());
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj) {
       return true;
     }
     if (obj == null) {
       return false;
     }
     if (getClass() != obj.getClass()) {
       return false;
     }
     Reservation other = (Reservation) obj;
     if (id == null) {
       if (other.id != null) {
         return false;
       }
     }
     else if (!id.equals(other.id)) {
       return false;
     }
     if (version == null) {
       if (other.version != null) {
         return false;
       }
     }
     else if (!version.equals(other.version)) {
       return false;
     }
     return true;
   }
 }
