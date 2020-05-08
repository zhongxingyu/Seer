 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.training.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 /**
  *
  * @author endy
  */
 @Entity @Table(name="t_role")
 @SequenceGenerator(name = "idgen", sequenceName = "t_role_id_seq", allocationSize=1)
 public class Role {
     @Id 
     @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idgen")
     private Integer id;
     private String kode;
     private String nama;
     
     @OneToMany(mappedBy="role")
     private List<User> daftarUser
             = new ArrayList<User>();
     
     @ManyToMany
     @JoinTable(
             name="t_role_permission", 
             joinColumns=@JoinColumn(name="id_role"), 
             inverseJoinColumns=@JoinColumn(name="id_permission")
     )
     private List<Permission> daftarPermission
             = new ArrayList<Permission>();
 
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

    public List<User> getDaftarUser() {
        return daftarUser;
    }

    public void setDaftarUser(List<User> daftarUser) {
        this.daftarUser = daftarUser;
    }

    public List<Permission> getDaftarPermission() {
        return daftarPermission;
    }

    public void setDaftarPermission(List<Permission> daftarPermission) {
        this.daftarPermission = daftarPermission;
    }
     
     
 }
