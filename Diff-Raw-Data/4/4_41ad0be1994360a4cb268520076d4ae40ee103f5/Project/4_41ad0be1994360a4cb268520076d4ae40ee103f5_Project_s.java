 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2012, University of Technology, Sydney
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
  * @date 25th October 2012
  */
 package au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  * Project entity that stores information about research projects.
  */
 @Entity
 @Table(name = "project")
 public class Project implements Serializable
 {
 	/** Serializable class. */
 	private static final long serialVersionUID = -7188227812725104056L;
 	
 	/** Record primary key. */
 	private Long id;
 	
 	/** The creator of the project. */
 	private User user;
 	
 	/** Identifier of the project. */
 	private String activity;
 	
 	/** User class containing the permissions of the */
 	private UserClass userClass;
 	
 	/** When this project was created. */
 	private Date creationTime;
 
 	/** The time the project metadata was last updated. */
 	private Date lastUpdate;
 	
 	/** When this project was published. */
 	private Date publishTime;
 	
 	/** Whether this project is shared to Research Data Commons (RDC). */
 	private boolean isShared;
 	
 	/** Whether this project is open access or contact supervisor. */
 	private boolean isOpen;
 	
 	/** Whether to auto publish collections as sessions are run. */
 	private boolean autoPublishCollections;
 	
 	/** This projects metadata. */
 	private Set<ProjectMetadata> metadata = new HashSet<ProjectMetadata>(0);
 	
 	/** This projects collections. */
 	private Set<Collection> collections = new HashSet<Collection>(0);
 	
 	public Project()
 	{
 	    /* Bean style constructor. */
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
 
     @ManyToOne(fetch = FetchType.EAGER)
     @JoinColumn(name = "users_id", nullable = false)
     public User getUser()
     {
         return this.user;
     }
 
     public void setUser(User user)
     {
         this.user = user;
     }
 
     @Column(name = "activity", unique = true, nullable = false)
     public String getActivity()
     {
         return this.activity;
     }
 
     public void setActivity(String activity)
     {
         this.activity = activity;
     }
 
     @JoinColumn(name = "user_class_id", nullable = false)
     @ManyToOne(fetch = FetchType.EAGER) 
     public UserClass getUserClass()
     {
         return this.userClass;
     }
 
     public void setUserClass(UserClass userClass)
     {
         this.userClass = userClass;
     }
 
     @Temporal(TemporalType.DATE)
     @Column(name = "creation_time", nullable = false)
     public Date getCreationTime()
     {
         return this.creationTime;
     }
 
     public void setCreationTime(Date creationTime)
     {
         this.creationTime = creationTime;
     }
 
     @Temporal(TemporalType.DATE)
     @Column(name = "last_update", nullable = false)
     public Date getLastUpdate()
     {
         return this.lastUpdate;
     }
 
     public void setLastUpdate(Date lastUpdate)
     {
         this.lastUpdate = lastUpdate;
     }
 
     @Temporal(TemporalType.DATE)
     @Column(name = "publish_time", nullable = true)
     public Date getPublishTime()
     {
         return this.publishTime;
     }
 
     public void setPublishTime(Date publishTime)
     {
         this.publishTime = publishTime;
     }
 
     @Column(name = "is_shared", nullable = false)
     public boolean isShared()
     {
         return this.isShared;
     }
 
     public void setShared(boolean isShared)
     {
         this.isShared = isShared;
     }
 
     @Column(name = "is_open", nullable = false)
     public boolean isOpen()
     {
         return this.isOpen;
     }
 
     public void setOpen(boolean isOpen)
     {
         this.isOpen = isOpen;
     }
     
    @Column(name = "auto_publish_collections")
    public boolean autoPublishCollections()
     {
         return this.autoPublishCollections;
     }
 
     public void setAutoPublishCollections(boolean autoPublishCollections)
     {
         this.autoPublishCollections = autoPublishCollections;
     }
 
     @OneToMany(mappedBy = "project", fetch = FetchType.EAGER)
     public Set<ProjectMetadata> getMetadata()
     {
         return this.metadata;
     }
 
     public void setMetadata(Set<ProjectMetadata> metadata)
     {
         this.metadata = metadata;
     }
     
     @OneToMany(mappedBy = "project", fetch = FetchType.EAGER)
     public Set<Collection> getCollections()
     {
         return this.collections;
     }
 
     public void setCollections(Set<Collection> collections)
     {
         this.collections = collections;
     }
 }
