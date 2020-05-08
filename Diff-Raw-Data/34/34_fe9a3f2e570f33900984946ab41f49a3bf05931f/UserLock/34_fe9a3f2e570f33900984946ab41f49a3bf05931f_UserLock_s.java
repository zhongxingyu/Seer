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
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 /**
  * UserLock entity class which maps to the user_lock table.
  * <p />
  */
 @Entity
@Table(name = "user_lock", catalog = "sahara")
 public class UserLock implements java.io.Serializable
 {
     /** Serializable class. */
     private static final long serialVersionUID = -7652820485119689900L;
     
     /** Record primary key. */
     private Long id;
     
     /** User the lock refers too. */
     private User user;
     
     /** Resource permission the lock refers to. */
     private ResourcePermission resourcePermission;
     
     /** Whether the lock is active. */
     private boolean isLocked;
     
     /** The key to unlock the lock. */
     private String lockKey;
 
     public UserLock()
     {
         /** Bean style constructor. */
     }
 
     public UserLock(final User user,
             final ResourcePermission resourcePermission,
             final boolean isLocked, final String lockKey)
     {
         this.user = user;
         this.resourcePermission = resourcePermission;
         this.isLocked = isLocked;
         this.lockKey = lockKey;
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
     @JoinColumn(name = "user_id", nullable = false)
     public User getUser()
     {
         return this.user;
     }
 
     public void setUser(final User user)
     {
         this.user = user;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "resource_permission_id", nullable = false)
     public ResourcePermission getResourcePermission()
     {
         return this.resourcePermission;
     }
 
     public void setResourcePermission(
             final ResourcePermission resourcePermission)
     {
         this.resourcePermission = resourcePermission;
     }
 
     @Column(name = "is_locked", nullable = false)
     public boolean isIsLocked()
     {
         return this.isLocked;
     }
 
     public void setIsLocked(final boolean isLocked)
     {
         this.isLocked = isLocked;
     }
 
     @Column(name = "lock_key", nullable = false, length = 50)
     public String getLockKey()
     {
         return this.lockKey;
     }
 
     public void setLockKey(final String lockKey)
     {
         this.lockKey = lockKey;
     }
 
 }
