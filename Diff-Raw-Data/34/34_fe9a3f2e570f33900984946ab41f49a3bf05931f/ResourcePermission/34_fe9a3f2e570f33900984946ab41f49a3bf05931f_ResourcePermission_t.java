 /**
   * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 6th January 2010
  */
 
 package au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities;
 
 // Generated 06/01/2010 5:09:20 PM by Hibernate Tools 3.2.5.Beta
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 /**
  * ResourcePermission entity class which maps to the resource_permission table.
  * <p />
  * The resource permission class specifies the resources a user class may 
  * access including the constraints that are placed on the sessions
  * that were granting with this resource permission.
  */
 @Entity
 @Table(name = "resource_permission")
 public class ResourcePermission implements java.io.Serializable
 {
     /** Serializable class. */
     private static final long serialVersionUID = -2292524825260205119L;
     
     /** Primary key. */
     private Long id;
     
     /** Rig type this resource permission refers to if it is a permission
      *  granting access to a rig type. */
     private RigType rigType;
     
     /** User class this resource permission refers to. */
     private UserClass userClass;
     
     /** Rig this resource permission refers to if it is a permission granting 
      *  access to a specific rig. */
     private Rig rig;
     
     /** Request capabilities this resource permission refers to if it is
      *  a permission granting access to a specific request capabilities. */
     private RequestCapabilities requestCapabilities;
     
    /** The resource permission resource type, either RIG, TYPE or CAP 
     *  for a rig permission, rig type permission or rig capabilities
     *  permission respectively. */
     private String type;
     
     /** The maximum guaranteed session duration in seconds. */
     private int sessionDuration;
     
     /** The extension duration if a session is extended. */
     private int extensionDuration;
     
     /** The number of times a session is allowed to be extended. */
     private short allowedExtensions;
     
     /** Timeout to terminate queue entrances if no presence is detected within
      *  this period */
     private int queueActivityTimeout;
     
    /** Timeout to terminate sessions if no presence is detected within this
      *  period. */
     private int sessionActivityTimeout;
     
     private Set<UserLock> userLocks = new HashSet<UserLock>(0);
     private Set<Session> sessionsForResourcePermissionId = new HashSet<Session>(0);
 
     public ResourcePermission()
     {
         /* Bean style constructor. */
     }
 
     public ResourcePermission(final UserClass userClass, final String type,
             final int sessionDuration, final int extensionDuration,
             final short allowedExtensions, final int queueActivityTimeout,
             final int sessionActivityTimeout)
     {
         this.userClass = userClass;
         this.type = type;
         this.sessionDuration = sessionDuration;
         this.extensionDuration = extensionDuration;
         this.allowedExtensions = allowedExtensions;
         this.queueActivityTimeout = queueActivityTimeout;
         this.sessionActivityTimeout = sessionActivityTimeout;
     }
 
     public ResourcePermission(final RigType rigType, final UserClass userClass,
             final Rig rig, final RequestCapabilities requestCapabilities,
             final String type, final int sessionDuration,
             final int extensionDuration, final short allowedExtensions,
             final int queueActivityTimeout, final int sessionActivityTimeout,
             final Set<UserLock> userLocks,
             final Set<Session> sessionsForResourcePermissionId)
     {
         this.rigType = rigType;
         this.userClass = userClass;
         this.rig = rig;
         this.requestCapabilities = requestCapabilities;
         this.type = type;
         this.sessionDuration = sessionDuration;
         this.extensionDuration = extensionDuration;
         this.allowedExtensions = allowedExtensions;
         this.queueActivityTimeout = queueActivityTimeout;
         this.sessionActivityTimeout = sessionActivityTimeout;
         this.userLocks = userLocks;
         this.sessionsForResourcePermissionId = sessionsForResourcePermissionId;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "id", unique = true, nullable = false)
     public Long getId()
     {
         return this.id;
     }
 
     public void setId(final Long id)
     {
         this.id = id;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "type_id")
     public RigType getRigType()
     {
         return this.rigType;
     }
 
     public void setRigType(final RigType rigType)
     {
         this.rigType = rigType;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_class_id", nullable = false)
     public UserClass getUserClass()
     {
         return this.userClass;
     }
 
     public void setUserClass(final UserClass userClass)
     {
         this.userClass = userClass;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "name_id")
     public Rig getRig()
     {
         return this.rig;
     }
 
     public void setRig(final Rig rig)
     {
         this.rig = rig;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "request_caps_id")
     public RequestCapabilities getRequestCapabilities()
     {
         return this.requestCapabilities;
     }
 
     public void setRequestCapabilities(
             final RequestCapabilities requestCapabilities)
     {
         this.requestCapabilities = requestCapabilities;
     }
 
     @Column(name = "type", nullable = false, length = 5)
     public String getType()
     {
         return this.type;
     }
 
     public void setType(final String type)
     {
         this.type = type;
     }
 
     @Column(name = "session_duration", nullable = false)
     public int getSessionDuration()
     {
         return this.sessionDuration;
     }
 
     public void setSessionDuration(final int sessionDuration)
     {
         this.sessionDuration = sessionDuration;
     }
 
     @Column(name = "extension_duration", nullable = false)
     public int getExtensionDuration()
     {
         return this.extensionDuration;
     }
 
     public void setExtensionDuration(final int extensionDuration)
     {
         this.extensionDuration = extensionDuration;
     }
 
     @Column(name = "allowed_extensions", nullable = false)
     public short getAllowedExtensions()
     {
         return this.allowedExtensions;
     }
 
     public void setAllowedExtensions(final short allowedExtensions)
     {
         this.allowedExtensions = allowedExtensions;
     }
 
     @Column(name = "queue_activity_timeout", nullable = false)
     public int getQueueActivityTimeout()
     {
         return this.queueActivityTimeout;
     }
 
     public void setQueueActivityTimeout(final int queueActivityTimeout)
     {
         this.queueActivityTimeout = queueActivityTimeout;
     }
 
     @Column(name = "session_activity_timeout", nullable = false)
     public int getSessionActivityTimeout()
     {
         return this.sessionActivityTimeout;
     }
 
     public void setSessionActivityTimeout(final int sessionActivityTimeout)
     {
         this.sessionActivityTimeout = sessionActivityTimeout;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "resourcePermission")
     public Set<UserLock> getUserLocks()
     {
         return this.userLocks;
     }
 
     public void setUserLocks(final Set<UserLock> userLocks)
     {
         this.userLocks = userLocks;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "resourcePermissionByResourcePermissionId")
     public Set<Session> getSessionsForResourcePermissionId()
     {
         return this.sessionsForResourcePermissionId;
     }
 
     public void setSessionsForResourcePermissionId(final Set<Session> sessionsForResourcePermissionId)
     {
         this.sessionsForResourcePermissionId = sessionsForResourcePermissionId;
     }
 }
