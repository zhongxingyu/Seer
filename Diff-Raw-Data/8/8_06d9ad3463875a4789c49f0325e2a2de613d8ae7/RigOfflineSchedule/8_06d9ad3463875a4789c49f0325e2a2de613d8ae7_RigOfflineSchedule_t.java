 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2011, University of Technology, Sydney
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
  * @date 29th January 2011
  */
 package au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 /**
  * Rig off-line entity which contains the times the rig should be put off-line. 
  */
 @Entity
 @Table(name = "rig_offline_schedule")
 public class RigOfflineSchedule implements Serializable
 {
     /** Serializable class. */
     private static final long serialVersionUID = -3898298550014627814L;
     
     /** Primary key. */
     private Long id;
     
     /** Whether this offline period is enabled. */
     private boolean active;
     
     /** Rig this off-line period refers to. */
     private Rig rig;
     
     /** The time at which this off-line period starts. */
     private Date startTime;
     
     /** The time at which this on-line period ends. */
     private Date endTime;
     
     /** The reason for being off-line. */
     private String reason;
 
     public RigOfflineSchedule()
     {
         /* Bean constructor. */
     }
     
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     @Column(name = "id", unique = true, nullable = false)
     public Long getId()
     {
         return this.id;
     }
 
     public void setId(Long id)
     {
         this.id = id;
     }
 
     @Column(name = "active", nullable = false)
     public boolean isActive()
     {
         return this.active;
     }
 
     public void setActive(boolean active)
     {
         this.active = active;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "rig_id", nullable = false)
     public Rig getRig()
     {
         return this.rig;
     }
 
     public void setRig(Rig rig)
     {
         this.rig = rig;
     }
 
    @Column(name = "start_time", nullable = false)
     public Date getStartTime()
     {
         return this.startTime;
     }
     
     public void setStartTime(Date start)
     {
         this.startTime = start;
     }
 
    @Column(name = "end_time", nullable = false)
     public Date getEndTime()
     {
         return this.endTime;
     }
 
     public void setEndTime(Date end)
     {
         this.endTime = end;
     }
 
    @Column(name = "reason", nullable = false, length = 255)
     public String getReason()
     {
         return this.reason;
     }
 
     public void setReason(String reason)
     {
         this.reason = reason;
     }
 }
