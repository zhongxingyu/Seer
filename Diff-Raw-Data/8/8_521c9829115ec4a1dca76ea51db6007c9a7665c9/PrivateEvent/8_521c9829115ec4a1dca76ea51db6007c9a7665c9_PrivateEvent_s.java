 package ai.ilikeplaces.entities;
 
import ai.ilikeplaces.doc.BIDIRECTIONAL;
import ai.ilikeplaces.doc.CREATED_BY;
import ai.ilikeplaces.doc.License;
import ai.ilikeplaces.doc.WARNING;
 import ai.ilikeplaces.util.EntityLifeCycleListener;
 
 import javax.persistence.*;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ravindranath Akila
  * Date: Jan 12, 2010
  * Time: 8:19:40 PM
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @CREATED_BY(who = {PrivateEvent.class},
         note = "We need to return this entity to the user for CRUD, hence cascade creation not possible.")
 @Entity
 @EntityListeners(EntityLifeCycleListener.class)
 public class PrivateEvent {
 
     public Long privateEventId;
 
     public String privateEventName;
 
     public String privateEventInfo;
 
     public String privateEventStartDate;
 
     public String privateEventEndDate;
 
     public Boolean extendedAccess;
 
     public Wall privateEventWall;
 
     public List<HumansPrivateEvent> privateEventOwners;
     final static public String privateEventOwnersCOL = "privateEventOwners";
 
     public List<HumansPrivateEvent> privateEventViewers;
     final static public String privateEventViewersCOL = "privateEventViewers";
 
     public List<HumansPrivateEvent> privateEventInvites;
     final static public String privateEventInvitesCOL = "privateEventInvites";
 
     public List<HumansPrivateEvent> privateEventRejects;
     final static public String privateEventRejectsCOL = "privateEventRejects";
 
     public PrivateLocation privateLocation;
     final static public String privateLocationCOL = "privateLocation";
 
     public Location location;
     final static public String locationCOL = "location";
 
     public Album privateEventAlbum;
     final static public String privateEventAlbumCOL = "privateEventAlbum";
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public Long getPrivateEventId() {
         return privateEventId;
     }
 
     public void setPrivateEventId(final Long privateEventId) {
         this.privateEventId = privateEventId;
     }
 
     @Transient
     public PrivateEvent setPrivateEventIdR(final Long privateEventId) {
         setPrivateEventId(privateEventId);
         return this;
     }
 
     public String getPrivateEventName() {
         return privateEventName;
     }
 
     public void setPrivateEventName(String privateEventName) {
         this.privateEventName = privateEventName;
     }
 
     @Transient
     public PrivateEvent setPrivateEventNameR(String privateEventName) {
         setPrivateEventName(privateEventName);
         return this;
     }
 
     public String getPrivateEventInfo() {
         return privateEventInfo;
     }
 
     public void setPrivateEventInfo(String privateEventInfo) {
         this.privateEventInfo = privateEventInfo;
     }
 
     @Transient
     public PrivateEvent setPrivateEventInfoR(String privateEventInfo) {
         setPrivateEventInfo(privateEventInfo);
         return this;
     }
 
     public String getPrivateEventStartDate() {
         return privateEventStartDate;
     }
 
     public void setPrivateEventStartDate(String privateEventStartDate) {
         this.privateEventStartDate = privateEventStartDate;
     }
 
     @Transient
     public PrivateEvent setPrivateEventStartDateR(String privateEventStartDate) {
         setPrivateEventStartDate(privateEventStartDate);
         return this;
     }
 
     public String getPrivateEventEndDate() {
         return privateEventEndDate;
     }
 
     public void setPrivateEventEndDate(String privateEventEndDate) {
         this.privateEventEndDate = privateEventEndDate;
     }
 
     @Transient
     public PrivateEvent setPrivateEventEndDateR(String privateEventEndDate) {
         setPrivateEventEndDate(privateEventEndDate);
         return this;
     }
 
     public Boolean isExtendedAccess() {
         return extendedAccess;
     }
 
     public void setExtendedAccess(Boolean extendedAccess) {
         this.extendedAccess = extendedAccess;
     }
 
     @Transient
     public PrivateEvent setExtendedAccessR(Boolean extendedAccess) {
         setExtendedAccess(extendedAccess);
         return this;
     }
 
     @BIDIRECTIONAL(ownerside = BIDIRECTIONAL.OWNING.IS)
     @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
     public PrivateLocation getPrivateLocation() {
         return privateLocation;
     }
 
     public void setPrivateLocation(PrivateLocation privateLocation) {
         this.privateLocation = privateLocation;
     }
 
     @Transient
     public PrivateEvent setPrivateLocationR(PrivateLocation privateLocation) {
         this.privateLocation = privateLocation;
         return this;
     }
 
     @BIDIRECTIONAL(ownerside = BIDIRECTIONAL.OWNING.IS)
     @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
     public Location getLocation() {
         return location;
     }
 
     public void setLocation(Location location) {
         this.location = location;
     }
 
     @Transient
     public PrivateEvent setLocationR(Location location) {
         setLocation(location);
         return this;
     }
 
     @BIDIRECTIONAL(ownerside = BIDIRECTIONAL.OWNING.IS)
     @OneToOne(cascade = CascadeType.ALL)
     public Album getPrivateEventAlbum() {
         return privateEventAlbum;
     }
 
     public void setPrivateEventAlbum(Album privateEventAlbum) {
         this.privateEventAlbum = privateEventAlbum;
     }
 
     @Transient
     public PrivateEvent setPrivateEventAlbumR(Album privateEventAlbum) {
         this.privateEventAlbum = privateEventAlbum;
         return this;
     }
 
     @WARNING(warning = "Owner because once an event needs to be deleted, deleting this entity is easier if owner." +
             "If this entity is not the owner, individual owner viewer accepteee rejectee will have to delete their events individually.")
     @BIDIRECTIONAL(ownerside = BIDIRECTIONAL.OWNING.IS)
     @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
     public List<HumansPrivateEvent> getPrivateEventInvites() {
         return privateEventInvites;
     }
 
     public void setPrivateEventInvites(final List<HumansPrivateEvent> privateEventInvites) {
         this.privateEventInvites = privateEventInvites;
     }
 
     @WARNING(warning = "Owner because once an event needs to be deleted, deleting this entity is easier if owner." +
             "If this entity is not the owner, individual owner viewer accepteee rejectee will have to delete their events individually.")
     @BIDIRECTIONAL(ownerside = BIDIRECTIONAL.OWNING.IS)
     @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
     public List<HumansPrivateEvent> getPrivateEventViewers() {
         return privateEventViewers;
     }
 
     public void setPrivateEventViewers(final List<HumansPrivateEvent> privateEventViewers) {
         this.privateEventViewers = privateEventViewers;
     }
 
     @WARNING(warning = "Owner because once an event needs to be deleted, deleting this entity is easier if owner." +
             "If this entity is not the owner, individual owner viewer accepteee rejectee will have to delete their events individually.")
     @BIDIRECTIONAL(ownerside = BIDIRECTIONAL.OWNING.IS)
     @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
     public List<HumansPrivateEvent> getPrivateEventOwners() {
         return privateEventOwners;
     }
 
     public void setPrivateEventOwners(final List<HumansPrivateEvent> privateEventOwners) {
         this.privateEventOwners = privateEventOwners;
     }
 
     @WARNING(warning = "Owner because once an event needs to be deleted, deleting this entity is easier if owner." +
             "If this entity is not the owner, individual owner viewer accepteee rejectee will have to delete their events individually.")
     @BIDIRECTIONAL(ownerside = BIDIRECTIONAL.OWNING.IS)
     @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
     public List<HumansPrivateEvent> getPrivateEventRejects() {
         return privateEventRejects;
     }
 
     public void setPrivateEventRejects(final List<HumansPrivateEvent> privateEventRejects) {
         this.privateEventRejects = privateEventRejects;
     }
 
    @BIDIRECTIONAL(ownerside = BIDIRECTIONAL.OWNING.NOT)
     @OneToOne(cascade = CascadeType.ALL)
     public Wall getPrivateEventWall() {
         return privateEventWall;
     }
 
     public void setPrivateEventWall(final Wall privateEventWall) {
         this.privateEventWall = privateEventWall;
     }
 
     @Transient
     public PrivateEvent setPrivateEventWallR(final Wall privateEventWall) {
         this.privateEventWall = privateEventWall;
         return this;
     }
 
     @Override
     public String toString() {
         return "PrivateEvent{" +
                 ", privateEventId=" + privateEventId +
                 ", privateLocation=" + privateLocation +
                 '}';
     }
 }
