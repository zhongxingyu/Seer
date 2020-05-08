 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.training.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author endy
  */
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 @Entity
 @Table(name="t_role")
 public class Role {
     
     @Id
     @GeneratedValue
     private Integer id;
     
     @Column(nullable=false, unique=true)
     private String kode;
     private String nama;
     
     @XmlElementWrapper(name = "daftarPermission")
    @XmlElement(name = "permission")
     @ManyToMany(fetch= FetchType.EAGER)
     @JoinTable(
             name="t_role_permission",
             joinColumns=@JoinColumn(name="id_role", nullable=false), 
             inverseJoinColumns=@JoinColumn(name="id_permission", nullable=false)
     )
     private List<Permission> daftarPermission 
             = new ArrayList<Permission>();
     
     @XmlElementWrapper(name = "daftarUser")
    @XmlElement(name = "user")
     @OneToMany(mappedBy="role")
     private List<User> daftarUser 
             = new ArrayList<User>();
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getKode() {
         return kode;
     }
 
     public void setKode(String kode) {
         this.kode = kode;
     }
 
     public String getNama() {
         return nama;
     }
 
     public void setNama(String nama) {
         this.nama = nama;
     }
 
     public List<Permission> getDaftarPermission() {
         return daftarPermission;
     }
 
     public void setDaftarPermission(List<Permission> daftarPermission) {
         this.daftarPermission = daftarPermission;
     }
 
     public List<User> getDaftarUser() {
         return daftarUser;
     }
 
     public void setDaftarUser(List<User> daftarUser) {
         this.daftarUser = daftarUser;
     }
     
 }
