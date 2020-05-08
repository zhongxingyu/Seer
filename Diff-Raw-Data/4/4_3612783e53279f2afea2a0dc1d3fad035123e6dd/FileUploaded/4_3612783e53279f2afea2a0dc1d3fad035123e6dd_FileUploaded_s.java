 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mti.webshare.model;
 
 
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 import javax.persistence.*;
 
 /**
  *
  * @author yoan
  * EPITA MTI 2013
  * 
  */
 
 @Entity
 @Table (name="file")
 public class FileUploaded implements Serializable
 {
     @Id
     @GeneratedValue
     private int id;
     
     @Column(name="name")
     private String name;
     
     @Column(name="public")
     private Boolean isPublic;
     
     @Column(name="path")
     private String path;
     
     @Column(name="isDir")
     private Boolean isDir;
     
     @Column(name="deleted")
     private Boolean deleted;
     
    @Column(name="parent_id")
    private int parent_id;
 
     public int getParent_id() {
         return parent_id;
     }
 
     public void setParent_id(int parent_id) {
         this.parent_id = parent_id;
     }
     
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "file_id", cascade= CascadeType.ALL)
     private Set<Event> events = new HashSet<Event>(0);
 
     public Set<Event> getEvents() {
         return events;
     }
 
     public void setEvents(Set<Event> events) {
         this.events = events;
     }
     
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "pk.file", cascade= CascadeType.ALL)
     private Set<UserFile> userFile = new HashSet<UserFile>(0);
 
     public Set<UserFile> getUserFile() {
         return userFile;
     }
 
     public void setUserFile(Set<UserFile> userFile) {
         this.userFile = userFile;
     }
 
     public Boolean getDeleted() {
         return deleted;
     }
 
     public void setDeleted(Boolean deleted)
     {
         this.deleted = deleted;
     }
 
     public int getId()
     {
         return id;
     }
 
     public void setId(int id)
     {
         this.id = id;
     }
 
     public Boolean getIsDir()
     {
         return isDir;
     }
 
     public void setIsDir(Boolean isDir)
     {
         this.isDir = isDir;
     }
 
     public Boolean getIsPublic()
     {
         return isPublic;
     }
 
     public void setIsPublic(Boolean isPublic)
     {
         this.isPublic = isPublic;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getPath()
     {
         return path;
     }
 
     public void setPath(String path)
     {
         this.path = path;
     }
     
 }
